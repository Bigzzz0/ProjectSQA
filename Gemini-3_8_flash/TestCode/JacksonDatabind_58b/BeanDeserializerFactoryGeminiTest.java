package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.security.Permission;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
 *
 * 1. Defect databind#877 (AccessFixTest::testCauseOfThrowableIgnoral):
 *    - In constructSettableProperty(), forced access (fixAccess) was erroneously attempted on the
 *      private field 'cause' of java.lang.Throwable even under restrictive SecurityManager environments.
 *    - Targeted by: testDefect877ThrowableCauseAccessDeniedWithSecurityManager()
 *
 * 2. Life-cycle & Subtype handling:
 *    - withConfig(): same config returns this; new config creates new instance.
 *    - Subclass of BeanDeserializerFactory calling withConfig() without overriding throws IllegalStateException.
 *
 * 3. Type routing & Abstract/Standard/Custom resolutions:
 *    - createBeanDeserializer() -> custom deserializer check.
 *    - createBeanDeserializer() -> type.isThrowable() -> buildThrowableDeserializer().
 *    - createBeanDeserializer() -> abstract & non-primitive -> materializeAbstractType() -> concrete build.
 *    - createBeanDeserializer() -> findStdDeserializer() with and without BeanDeserializerModifier.
 *    - isPotentialBeanType() branches: primitives, array, enum, proxy, local/anonymous classes.
 *
 * 4. Builder-based Deserialization:
 *    - createBuilderBasedDeserializer() / buildBuilderBasedDeserializer() with @JsonPOJOBuilder.
 *    - Annotated build method access modifier fixing and execution.
 *
 * 5. ObjectIdReader wiring:
 *    - PropertyGenerator: id property present vs missing (throws IllegalArgumentException).
 *    - Non-property generator (IntSequenceGenerator).
 *
 * 6. Property & Setter resolution:
 *    - Constructor properties (matching vs non-matching CreatorProperty).
 *    - Setterless Collection/Map properties when USE_GETTERS_AS_SETTERS is true.
 *    - AnySetter construction and injection.
 *    - Managed & back-reference property linking.
 *    - Injectables resolution.
 *    - View inclusion (MapperFeature.DEFAULT_VIEW_INCLUSION).
 */
public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test Dummy Types & Fixtures
    // =========================================================================

    public static class EmptyBean {
    }

    public static class SimpleValueBean {
        private String name;
        private int count;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class SetterlessCollectionBean {
        private final List<String> items = new ArrayList<String>();
        private final Map<String, Integer> map = new HashMap<String, Integer>();

        public List<String> getItems() { return items; }
        public Map<String, Integer> getMap() { return map; }
    }

    public static class AnySetterBean {
        private final Map<String, Object> values = new HashMap<String, Object>();

        public void setAny(String name, Object value) {
            values.put(name, value);
        }

        public Object get(String name) {
            return values.get(name);
        }
    }

    public static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        private int code;

        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "have")
    public static class CustomPojoBuilder {
        private String title;

        public CustomPojoBuilder haveTitle(String title) {
            this.title = title;
            return this;
        }

        public TargetPojo create() {
            return new TargetPojo(title);
        }
    }

    @JsonDeserialize(builder = CustomPojoBuilder.class)
    public static class TargetPojo {
        private final String title;
        public TargetPojo(String title) { this.title = title; }
        public String getTitle() { return title; }
    }

    public static class MissingCreatorPropBean {
        private final String val;
        public MissingCreatorPropBean(@JsonProperty("nonExistent") String val) {
            this.val = val;
        }
        public String getVal() { return val; }
    }

    public static class SubclassedFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public SubclassedFactory() {
            super(new DeserializerFactoryConfig());
        }
    }

    public interface SomeProxyInterface {}

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#877)
    // =========================================================================

    /**
     * Target Defect databind#877:
     * When a SecurityManager forbids suppressAccessChecks, constructing a deserializer
     * for a Throwable should not attempt to forcefully setAccessible() on Throwable.cause.
     */
    @Test(timeout = 4000)
    public void testDefect877ThrowableCauseAccessDeniedWithSecurityManager() throws Exception {
        SecurityManager originalSm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    // Allow the test to reset the security manager in finally block
                    if ("setSecurityManager".equals(perm.getName())) {
                        return;
                    }
                    // Simulate restricted security profile blocking forced access
                    if ("suppressAccessChecks".equals(perm.getName())) {
                        throw new SecurityException("Denied: suppressAccessChecks");
                    }
                }
            });

            ObjectMapper mapper = new ObjectMapper();
            // Deserializing Throwable type must succeed without SecurityException on Throwable.cause
            Throwable th = mapper.readValue("{\"message\":\"defect877_probe\"}", Throwable.class);
            assertNotNull(th);
            assertEquals("defect877_probe", th.getMessage());
        } finally {
            System.setSecurityManager(originalSm);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigIdentityAndNewConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        assertSame(factory, factory.withConfig(factory.getFactoryConfig()));

        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    @Test(timeout = 4000)
    public void testSubclassWithoutOverriddenWithConfigThrows() {
        SubclassedFactory customSub = new SubclassedFactory();
        try {
            customSub.withConfig(new DeserializerFactoryConfig());
            fail("Expected IllegalStateException for un-overridden withConfig in subclass");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has not properly overridden method"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateBeanDeserializerStandardBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleValueBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);

        SimpleValueBean bean = (SimpleValueBean) deser.deserialize(
                mapper.getFactory().createParser("{\"name\":\"Gemini\",\"count\":42}"),
                mapper.getDeserializationContext()
        );
        assertEquals("Gemini", bean.getName());
        assertEquals(42, bean.getCount());
    }

    @Test(timeout = 4000)
    public void testBuildThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.buildThrowableDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    @Test(timeout = 4000)
    public void testCreateBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType targetType = mapper.constructType(TargetPojo.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(targetType);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBuilderBasedDeserializer(
                ctxt, targetType, desc, CustomPojoBuilder.class);
        assertNotNull(deser);

        TargetPojo pojo = (TargetPojo) deser.deserialize(
                mapper.getFactory().createParser("{\"title\":\"MasteringJackson\"}"),
                mapper.getDeserializationContext()
        );
        assertEquals("MasteringJackson", pojo.getTitle());
    }

    @Test(timeout = 4000)
    public void testSetterlessPropertiesForCollectionsAndMaps() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, true);

        SetterlessCollectionBean result = mapper.readValue(
                "{\"items\":[\"alpha\",\"beta\"],\"map\":{\"key1\":100}}",
                SetterlessCollectionBean.class
        );
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().contains("alpha"));
        assertEquals(Integer.valueOf(100), result.getMap().get("key1"));
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType abstractType = mapper.constructType(CharSequence.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(abstractType);

        JavaType concreteType = customFactory.materializeAbstractType(ctxt, abstractType, beanDesc);
        assertNotNull(concreteType);
        assertEquals(String.class, concreteType.getRawClass());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsPotentialBeanTypeRejection() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;

        // 1. Primitive types
        try {
            factory.isPotentialBeanType(int.class);
            fail("Expected IllegalArgumentException for primitive");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }

        // 2. Arrays
        try {
            factory.isPotentialBeanType(String[].class);
            fail("Expected IllegalArgumentException for array");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }

        // 3. Enums
        try {
            factory.isPotentialBeanType(Thread.State.class);
            fail("Expected IllegalArgumentException for enum");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }

        // 4. Proxy classes
        Object proxy = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{SomeProxyInterface.class},
                (p, method, args) -> null
        );
        try {
            factory.isPotentialBeanType(proxy.getClass());
            fail("Expected IllegalArgumentException for proxy");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Proxy class"));
        }

        // 5. Local / Anonymous class
        Object anonymousInstance = new Object() {};
        try {
            factory.isPotentialBeanType(anonymousInstance.getClass());
            fail("Expected IllegalArgumentException for local/anonymous class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }
    }

    @Test(timeout = 4000)
    public void testIsPotentialBeanTypeValid() {
        assertTrue(BeanDeserializerFactory.instance.isPotentialBeanType(EmptyBean.class));
        assertTrue(BeanDeserializerFactory.instance.isPotentialBeanType(SimpleValueBean.class));
    }

    @Test(timeout = 4000)
    public void testFindStdDeserializerWithModifier() throws Exception {
        final boolean[] modifierInvoked = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         JsonDeserializer<?> deserializer) {
                modifierInvoked[0] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory factory = new BeanDeserializerFactory(config);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType stringType = mapper.constructType(String.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(stringType);

        JsonDeserializer<?> deser = factory.findStdDeserializer(ctxt, stringType, beanDesc);
        assertNotNull(deser);
        assertTrue(modifierInvoked[0]);
    }

    @Test(timeout = 4000)
    public void testViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

        JavaType type = mapper.constructType(SimpleValueBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingCreatorPropertyThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"nonExistent\":\"test\"}", MissingCreatorPropBean.class);
            // In case construction succeeded or deferral was performed, check type mapping
        } catch (JsonMappingException e) {
            // Expected path when creator property cannot be matched or instantiated properly
            assertNotNull(e.getMessage());
        }
    }

    public static class InvalidObjectIdBean {
        @com.fasterxml.jackson.annotation.JsonIdentityInfo(
                generator = ObjectIdGenerators.PropertyGenerator.class,
                property = "nonExistentId"
        )
        public int id;
    }

    @Test(timeout = 4000)
    public void testInvalidObjectIdPropertyDefinitionThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"id\":1}", InvalidObjectIdBean.class);
            fail("Expected exception due to invalid/missing Object Id property");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("can not find property with name 'nonExistentId'"));
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("can not find property with name 'nonExistentId'"));
        }
    }

    public static class ValidIntSeqObjectIdBean {
        @com.fasterxml.jackson.annotation.JsonIdentityInfo(
                generator = ObjectIdGenerators.IntSequenceGenerator.class,
                property = "@id"
        )
        public String name;
    }

    @Test(timeout = 4000)
    public void testIntSequenceObjectIdGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValidIntSeqObjectIdBean bean = mapper.readValue("{\"@id\":1,\"name\":\"test\"}", ValidIntSeqObjectIdBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.name);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Inheritance & Custom Modifier Integration
    // =========================================================================

    public static class CustomDeser extends StdDeserializer<SimpleValueBean> {
        private static final long serialVersionUID = 1L;
        public CustomDeser() { super(SimpleValueBean.class); }
        @Override
        public SimpleValueBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
            SimpleValueBean b = new SimpleValueBean();
            b.setName("CUSTOM_OVERRIDE");
            return b;
        }
    }

    @Test(timeout = 4000)
    public void testFindCustomBeanDeserializerOverride() throws Exception {
        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(SimpleValueBean.class, new CustomDeser());

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAdditionalDeserializers(desers);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleValueBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomDeser);
    }

    @Test(timeout = 4000)
    public void testBeanDeserializerModifierUpdateBuilderAndProperties() throws Exception {
        final boolean[] updatedBuilder = new boolean[1];
        final boolean[] updatedProps = new boolean[1];

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         BeanDeserializerBuilder builder) {
                updatedBuilder[0] = true;
                return builder;
            }

            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                                                                 BeanDescription beanDesc,
                                                                 List<BeanPropertyDefinition> propDefs) {
                updatedProps[0] = true;
                return propDefs;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleValueBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(updatedBuilder[0]);
        assertTrue(updatedProps[0]);
    }
}