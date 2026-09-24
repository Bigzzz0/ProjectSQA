package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (databind#1599 / CVE-2017-7525):
 *    - Branch: createBeanDeserializer() -> checkIllegalTypes() / security check.
 *    - Ground truth failure: testIssue1599 expects "Illegal type" in exception message when attempting
 *      to deserialize known dangerous / exploit payload classes (e.g., TemplatesImpl).
 *
 * 2. LIFECYCLE & CONFIGURATION BRANCHES:
 *    - withConfig(DeserializerFactoryConfig):
 *      * Returns 'this' when config is unchanged (_factoryConfig == config).
 *      * Creates a new BeanDeserializerFactory instance when config differs.
 *      * Throws IllegalStateException when invoked on an anonymous/custom subclass that does not override withConfig.
 *
 * 3. DESERIALIZER CREATION WORKFLOWS & DECISION LOGIC:
 *    - Custom deserializer override (_findCustomBeanDeserializer) -> immediate return.
 *    - Exception/Throwable types (type.isThrowable()) -> buildThrowableDeserializer() with initCause, ignorable
 *      suppressed/localizedMessage/message, and ThrowableDeserializer wrapping.
 *    - Abstract type materialization (type.isAbstract() && !isPrimitive() && !isEnumType()) -> materializeAbstractType()
 *      via AbstractTypeResolver.
 *    - Standard deserializers (findStdDeserializer()) modified by BeanDeserializerModifier.
 *    - Potential bean type checks (isPotentialBeanType):
 *      * Throws IllegalArgumentException for Proxy classes, primitive classes, or local non-static classes.
 *    - Builder-based deserialization (createBuilderBasedDeserializer / buildBuilderBasedDeserializer).
 *
 * 4. PROPERTY & INJECTION RESOLUTION LOGIC:
 *    - addBeanProps():
 *      * Creator properties matching / mismatch error reporting.
 *      * Field-based, setter-based, and getter-as-setter (Collection/Map) properties.
 *      * JsonIgnoreProperties and ignored properties.
 *      * AnySetter on methods and fields.
 *    - ObjectIdReader resolution: PropertyGenerator vs non-property generator.
 *    - Reference properties (back/managed references) and error on back-ref as creator parameter.
 *    - Value injection (addInjectables via JacksonInject).
 */
public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test Dummy Models & Helpers
    // =========================================================================

    public static class SimpleBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class GetterOnlyCollectionBean {
        private final List<String> items = new ArrayList<>();
        private final Map<String, String> map = new HashMap<>();

        public List<String> getItems() { return items; }
        public Map<String, String> getMap() { return map; }
    }

    public static class FieldOnlyBean {
        public String title;
    }

    public static class CustomExceptionBean extends Exception {
        private static final long serialVersionUID = 1L;
        public CustomExceptionBean() { super(); }
        public CustomExceptionBean(String msg) { super(msg); }
    }

    public interface InterfaceBean {
        String getValue();
    }

    public static class ImplBean implements InterfaceBean {
        private String value;
        public ImplBean() {}
        public ImplBean(String v) { this.value = v; }
        @Override
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @JsonIgnoreProperties({"unwanted"})
    public static class IgnoralBean {
        public String wanted;
        public String unwanted;
    }

    public static class AnySetterMethodBean {
        private final Map<String, Object> extra = new HashMap<>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() { return extra; }
    }

    public static class AnySetterFieldBean {
        @com.fasterxml.jackson.annotation.JsonAnySetter
        public Map<String, Object> extra = new HashMap<>();
    }

    public static class CreatorBean {
        private final String code;
        private String description;

        @JsonCreator
        public CreatorBean(@JsonProperty("code") String code) {
            this.code = code;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class InjectedBean {
        public String normal;
        @JacksonInject("injectId")
        public String injected;
    }

    public static class ParentRef {
        public String name;
        @JsonManagedReference
        public ChildRef child;
    }

    public static class ChildRef {
        public String info;
        @JsonBackReference
        public ParentRef parent;
    }

    public static class InvalidBackRefCreator {
        public String data;
        @JsonCreator
        public InvalidBackRefCreator(@JsonProperty("parent") @JsonBackReference ParentRef parent) {
            // Invalid: back-references are not allowed as creator parameters
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdPropertyBean {
        public int id;
        public String value;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@seq")
    public static class IdSequenceBean {
        public String value;
    }

    public static class ValueClass {
        public final int x;
        public final int y;
        ValueClass(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    public static class ValueBuilder {
        private int x;
        private int y;
        public ValueBuilder withX(int x) { this.x = x; return this; }
        public ValueBuilder withY(int y) { this.y = y; return this; }
        public ValueClass create() { return new ValueClass(x, y); }
    }

    // Custom subclass to verify 'withConfig' guard
    static class CustomFactorySubclass extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public CustomFactorySubclass(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue 1599 / CVE-2017-7525)
    // =========================================================================

    /**
     * Targets Issue 1599 (CVE-2017-7525): Deserializing known illegal/dangerous types
     * like TemplatesImpl should be blocked with an exception explicitly mentioning "Illegal type".
     */
    @Test(timeout = 4000)
    public void testDefectIssue1599IllegalTypeBlacklist() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        String illegalTypeName = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
        String payload = "[\"" + illegalTypeName + "\", {\"transletBytecodes\":[\"AA==\"]}]";

        try {
            mapper.readValue(payload, Object.class);
            fail("Expected JsonMappingException indicating illegal type");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected exception message to contain 'Illegal type', got: " + msg,
                    msg != null && msg.contains("Illegal type"));
        } catch (Exception e) {
            fail("Expected JsonMappingException with 'Illegal type' but got: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Deserializer Construction
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateBeanDeserializerForRegularBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        assertNotNull("Deserializer should be created for SimpleBean", deser);
        assertTrue(deser instanceof BeanDeserializer);

        SimpleBean result = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        assertEquals("Alice", result.getName());
        assertEquals(30, result.getAge());
    }

    @Test(timeout = 4000)
    public void testCreateThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomExceptionBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        assertNotNull("Throwable deserializer should be constructed", deser);

        String json = "{\"message\":\"something failed\",\"cause\":null,\"localizedMessage\":\"ignored\"}";
        CustomExceptionBean ex = mapper.readValue(json, CustomExceptionBean.class);
        assertEquals("something failed", ex.getMessage());
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(InterfaceBean.class, ImplBean.class);
        mod.setAbstractTypes(resolver);
        mapper.registerModule(mod);

        InterfaceBean bean = mapper.readValue("{\"value\":\"gemini\"}", InterfaceBean.class);
        assertNotNull(bean);
        assertTrue(bean instanceof ImplBean);
        assertEquals("gemini", bean.getValue());
    }

    @Test(timeout = 4000)
    public void testGetterOnlyCollectionsAndMaps() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Enables auto-detection of getters to populate existing collections/maps
        mapper.enable(MapperFeature.USE_GETTERS_AS_SETTERS);

        String json = "{\"items\":[\"a\",\"b\"],\"map\":{\"key\":\"val\"}}";
        GetterOnlyCollectionBean bean = mapper.readValue(json, GetterOnlyCollectionBean.class);
        assertEquals(2, bean.getItems().size());
        assertTrue(bean.getItems().contains("a"));
        assertEquals("val", bean.getMap().get("key"));
    }

    @Test(timeout = 4000)
    public void testFieldPropertyBinding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldOnlyBean bean = mapper.readValue("{\"title\":\"Jackson WhiteBox\"}", FieldOnlyBean.class);
        assertNotNull(bean);
        assertEquals("Jackson WhiteBox", bean.title);
    }

    @Test(timeout = 4000)
    public void testCreatorPropertyAndFallbackSetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"code\":\"C01\",\"description\":\"Test item\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertEquals("C01", bean.getCode());
        assertEquals("Test item", bean.getDescription());
    }

    @Test(timeout = 4000)
    public void testAnySetterMethodAndField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // AnySetter Method
        AnySetterMethodBean methodBean = mapper.readValue("{\"dynA\":\"v1\",\"dynB\":\"v2\"}", AnySetterMethodBean.class);
        assertEquals("v1", methodBean.getExtra().get("dynA"));
        assertEquals("v2", methodBean.getExtra().get("dynB"));

        // AnySetter Field
        AnySetterFieldBean fieldBean = mapper.readValue("{\"fieldA\":123}", AnySetterFieldBean.class);
        assertEquals(123, fieldBean.extra.get("fieldA"));
    }

    @Test(timeout = 4000)
    public void testManagedAndBackReferences() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Parent\",\"child\":{\"info\":\"Child\"}}";
        ParentRef parent = mapper.readValue(json, ParentRef.class);

        assertNotNull(parent);
        assertEquals("Parent", parent.name);
        assertNotNull(parent.child);
        assertEquals("Child", parent.child.info);
        assertSame("Back reference should point to parent", parent, parent.child.parent);
    }

    @Test(timeout = 4000)
    public void testValueInjection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectId", "InjectedValue42");

        InjectedBean bean = mapper.reader(injectables)
                .forType(InjectedBean.class)
                .readValue("{\"normal\":\"normalValue\"}");

        assertEquals("normalValue", bean.normal);
        assertEquals("InjectedValue42", bean.injected);
    }

    @Test(timeout = 4000)
    public void testObjectIdResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Property-based ObjectId
        IdPropertyBean propBean = mapper.readValue("{\"id\":101,\"value\":\"First\"}", IdPropertyBean.class);
        assertEquals(101, propBean.id);
        assertEquals("First", propBean.value);

        // Sequence-based ObjectId
        IdSequenceBean seqBean = mapper.readValue("{\"@seq\":1,\"value\":\"Second\"}", IdSequenceBean.class);
        assertEquals("Second", seqBean.value);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType valueType = mapper.constructType(ValueClass.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(valueType);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBuilderBasedDeserializer(
                ctxt, valueType, desc, ValueBuilder.class);
        assertNotNull("Builder-based deserializer should be created", deser);

        String json = "{\"x\":10,\"y\":20}";
        ValueClass value = (ValueClass) deser.deserialize(
                mapper.getFactory().createParser(json), ctxt);
        assertEquals(10, value.x);
        assertEquals(20, value.y);
    }

    @Test(timeout = 4000)
    public void testCustomBeanDeserializerOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        final SimpleBean specialInstance = new SimpleBean();
        specialInstance.setName("SpecialOverride");

        module.addDeserializer(SimpleBean.class, new StdDeserializer<SimpleBean>(SimpleBean.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return specialInstance;
            }
        });
        mapper.registerModule(module);

        SimpleBean result = mapper.readValue("{\"name\":\"any\"}", SimpleBean.class);
        assertSame(specialInstance, result);
        assertEquals("SpecialOverride", result.getName());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIgnoredPropertiesFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoralBean bean = mapper.readValue("{\"wanted\":\"yes\",\"unwanted\":\"blocked\"}", IgnoralBean.class);
        assertEquals("yes", bean.wanted);
        assertNull("Ignored property should not be populated", bean.unwanted);
    }

    @Test(timeout = 4000)
    public void testIsPotentialBeanTypeRejections() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;

        // 1. Primitive rejection
        try {
            factory.isPotentialBeanType(int.class);
            fail("Expected IllegalArgumentException for primitive type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("primitive"));
        }

        // 2. Array rejection
        try {
            factory.isPotentialBeanType(String[].class);
            fail("Expected IllegalArgumentException for array type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("array"));
        }

        // 3. Dynamic Proxy rejection
        Class<?> proxyClass = Proxy.getProxyClass(
                getClass().getClassLoader(), Serializable.class);
        try {
            factory.isPotentialBeanType(proxyClass);
            fail("Expected IllegalArgumentException for proxy type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Proxy"));
        }

        // 4. Method-local class rejection
        class LocalClass {}
        try {
            factory.isPotentialBeanType(LocalClass.class);
            fail("Expected IllegalArgumentException for local class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("local"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubtypeWithoutOverriddenWithConfigThrowsException() {
        CustomFactorySubclass customFactory = new CustomFactorySubclass(new DeserializerFactoryConfig());
        try {
            customFactory.withConfig(new DeserializerFactoryConfig());
            fail("Subtype of BeanDeserializerFactory should throw IllegalStateException when calling withConfig");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has not properly overridden method"));
        }
    }

    @Test(timeout = 4000)
    public void testBackReferenceAsCreatorParameterFails() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"parent\":{}}", InvalidBackRefCreator.class);
            fail("Binding back reference as creator parameter must report bad type definition");
        } catch (JsonMappingException e) {
            assertTrue("Message should indicate invalid back reference",
                    e.getMessage().contains("Can not bind back references as Creator parameters"));
        }
    }

    @Test(timeout = 4000)
    public void testViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        SimpleBean bean = mapper.readValue("{\"name\":\"Bob\",\"age\":25}", SimpleBean.class);
        assertEquals("Bob", bean.getName());
        assertEquals(25, bean.getAge());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Factory Mechanics
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigIdentityAndEquality() {
        BeanDeserializerFactory original = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig sameConfig = original.getFactoryConfig();

        // Same config should return identical factory instance
        DeserializerFactory sameFactory = original.withConfig(sameConfig);
        assertSame(original, sameFactory);

        // Different config creates a new instance
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = original.withConfig(newConfig);
        assertNotSame(original, newFactory);
        assertEquals(BeanDeserializerFactory.class, newFactory.getClass());
    }

    @Test(timeout = 4000)
    public void testDeserializerModifierLifecycle() throws Exception {
        final boolean[] modifierCalled = new boolean[3];

        SimpleModule mod = new SimpleModule();
        mod.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                modifierCalled[0] = true;
                return super.updateProperties(config, beanDesc, propDefs);
            }

            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                modifierCalled[1] = true;
                return super.updateBuilder(config, beanDesc, builder);
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierCalled[2] = true;
                return super.modifyDeserializer(config, beanDesc, deserializer);
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod);

        SimpleBean result = mapper.readValue("{\"name\":\"Mod\"}", SimpleBean.class);
        assertEquals("Mod", result.getName());

        assertTrue("updateProperties modifier must be triggered", modifierCalled[0]);
        assertTrue("updateBuilder modifier must be triggered", modifierCalled[1]);
        assertTrue("modifyDeserializer modifier must be triggered", modifierCalled[2]);
    }
}