/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
 * Defect Under Test: Defects4J databind #1595
 *   (com.fasterxml.jackson.databind.filter.IgnorePropertyOnDeserTest::testIgnoreGetterNotSetter1595)
 * Failure Symptom: junit.framework.ComparisonFailure: expected:<jack> but was:<null>
 *
 * Branch & Decision Coverage Map:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - withConfig(config): Same config (returns this) vs new config vs illegal subclass call.
 *    - createBeanDeserializer: Custom bean deserializer override path.
 *    - createBeanDeserializer: Materialize abstract types using AbstractTypeResolver.
 *    - createBeanDeserializer: Standard deserializer delegation and modifier post-processing.
 *    - createBuilderBasedDeserializer: POJO builder resolution, custom buildMethodName / prefix.
 *    - isPotentialBeanType: Valid bean classes vs primitives, arrays, local classes.
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Ignorable property sets: Empty, explicit @JsonIgnoreProperties, cached type ignorals.
 *    - AnySetter: Method-based vs Field-based any setters.
 *    - Views: View inclusion enabled vs MapperFeature.DEFAULT_VIEW_INCLUSION disabled.
 *    - Collections/Maps as setterless properties via USE_GETTERS_AS_SETTERS.
 *    - ObjectId readers: PropertyGenerator vs non-property generator (IntSequenceGenerator).
 *
 * 3. Partition C: Defect-Targeted Branch Zone (databind #1595)
 *    - addBeanProps: Handling of beanDesc.getIgnoredPropertyNames() where a property has @JsonIgnore
 *      on the getter but a valid unignored setter. The bug in BeanDeserializerFactory inadvertently
 *      registers the property name as ignorable in the builder, causing the setter to be skipped.
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - checkIllegalTypes: Blacklisted types (DEFAULT_NO_DESER_CLASS_NAMES) reject deserialization.
 *    - addObjectIdReader: Missing/unresolvable property generator name throws IllegalArgumentException.
 *    - addReferenceProperties: Creator parameters as back-references trigger reportBadTypeDefinition.
 *    - buildBeanDeserializer: Abstract types with no instantiator construct abstract deserializers.
 *
 * 5. Partition E: Object Lifecycle & Contract Integrity
 *    - BeanDeserializerModifier lifecycle: updateProperties, updateBuilder, modifyDeserializer.
 *    - ThrowableDeserializer: initCause, message, suppressed, and localizedMessage ignorals.
 *    - DEFAULT_NO_DESER_CLASS_NAMES unmodifiability check.
 * =========================================================================
 */
package com.fasterxml.jackson.databind.deser;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test POJOs and Fixtures
    // =========================================================================

    public static class SimpleBean {
        public String name;
    }

    public static class Simple1595 {
        protected String name;

        @JsonIgnore
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class UnoverriddenSubFactory extends BeanDeserializerFactory {
        public UnoverriddenSubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    public static class CustomIllegalFactory extends BeanDeserializerFactory {
        public CustomIllegalFactory() {
            super(new DeserializerFactoryConfig());
            Set<String> illegal = new HashSet<>(_cfgIllegalClassNames);
            illegal.add(SimpleBean.class.getName());
            this._cfgIllegalClassNames = illegal;
        }
    }

    public interface Animal {
        String sound();
    }

    public static class Dog implements Animal {
        public String bark = "woof";
        @Override
        public String sound() {
            return bark;
        }
    }

    public static abstract class AbstractNoCreator {
        public String name;
    }

    @JsonDeserialize(builder = POJOBuilder.class)
    public static class ValueWithBuilder {
        final int id;
        final String name;

        ValueWithBuilder(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class POJOBuilder {
        private int id;
        private String name;

        public POJOBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public POJOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ValueWithBuilder build() {
            return new ValueWithBuilder(id, name);
        }
    }

    @JsonDeserialize(builder = CustomBuilderPOJO.class)
    public static class ValueWithCustomBuilder {
        final int count;
        ValueWithCustomBuilder(int count) {
            this.count = count;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    public static class CustomBuilderPOJO {
        private int count;

        public CustomBuilderPOJO setCount(int count) {
            this.count = count;
            return this;
        }

        public ValueWithCustomBuilder create() {
            return new ValueWithCustomBuilder(count);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id;
        public PropertyIdBean link;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class SequenceIdBean {
        public String value;
        public SequenceIdBean next;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    public static class InvalidIdBean {
        public int id;
    }

    public static class AnySetterMethodBean {
        private final Map<String, Object> props = new HashMap<>();

        @JsonAnySetter
        public void set(String key, Object value) {
            props.put(key, value);
        }

        public Map<String, Object> getProps() {
            return props;
        }
    }

    public static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> props = new HashMap<>();
    }

    public static class ParentRef {
        public String name;
    }

    public static class ChildWithBackRefCreator {
        public final ParentRef parent;

        @JsonCreator
        public ChildWithBackRefCreator(@JsonProperty("parent") @JsonBackReference ParentRef parent) {
            this.parent = parent;
        }
    }

    public static class Department {
        public String name;
        @JsonManagedReference
        public List<Employee> employees = new ArrayList<>();
    }

    public static class Employee {
        public String name;
        @JsonBackReference
        public Department dept;
    }

    public static class SetterlessListBean {
        private final List<String> items = new ArrayList<>();

        public List<String> getItems() {
            return items;
        }
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @JsonView(Views.Public.class)
        public int pub;

        @JsonView(Views.Internal.class)
        public int priv;
    }

    public static class InjectedBean {
        @JacksonInject("injectedKey")
        public String injected;

        public String name;
    }

    @JsonIgnoreType
    public static class IgnoredType {
        public String data;
    }

    public static class ContainerWithIgnoredType {
        public IgnoredType ignored;
        public String value;
    }

    public static class CreatorBean {
        final String first;
        final int age;

        @JsonCreator
        public CreatorBean(@JsonProperty("first") String first, @JsonProperty("age") int age) {
            this.first = first;
            this.age = age;
        }
    }

    public static class CustomThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public int code;

        public CustomThrowable() {
            super();
        }

        public CustomThrowable(String msg) {
            super(msg);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Databind #1595)
    // =========================================================================

    /**
     * Targets Defects4J bug: IgnorePropertyOnDeserTest::testIgnoreGetterNotSetter1595
     * Getter has @JsonIgnore, but setter is normal/unignored.
     * The defective BeanDeserializerFactory treats "name" as an ignorable property
     * in addBeanProps(), causing deserialization to discard "name" and yield null.
     */
    @Test(timeout = 4000)
    public void testIgnoreGetterNotSetter1595() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Simple1595 result = mapper.readValue("{\"name\":\"jack\"}", Simple1595.class);
        assertNotNull("Deserialized instance must not be null", result);
        assertEquals("jack", result.name);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigSameAndDifferent() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig origConfig = factory.getFactoryConfig();

        // Branch: _factoryConfig == config -> return this
        DeserializerFactory sameFactory = factory.withConfig(origConfig);
        assertSame("withConfig with same instance should return this", factory, sameFactory);

        // Branch: new config -> new BeanDeserializerFactory
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotSame("withConfig with new config should return a new factory", factory, newFactory);
        assertEquals(BeanDeserializerFactory.class, newFactory.getClass());
    }

    @Test(timeout = 4000)
    public void testCustomBeanDeserializerPrecedence() throws Exception {
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(SimpleBean.class, new StdDeserializer<SimpleBean>(SimpleBean.class) {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                SimpleBean bean = new SimpleBean();
                bean.name = "CUSTOM_OVERRIDE";
                return bean;
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(mod);

        SimpleBean result = mapper.readValue("{\"name\":\"standard\"}", SimpleBean.class);
        assertNotNull(result);
        assertEquals("CUSTOM_OVERRIDE", result.name);
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Animal.class, Dog.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory factory = new BeanDeserializerFactory(config);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializerFactory(factory);

        Animal animal = mapper.readValue("{\"bark\":\"ruff!\"}", Animal.class);
        assertNotNull(animal);
        assertTrue(animal instanceof Dog);
        assertEquals("ruff!", animal.sound());
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueWithBuilder value = mapper.readValue("{\"id\":10,\"name\":\"alpha\"}", ValueWithBuilder.class);
        assertNotNull(value);
        assertEquals(10, value.id);
        assertEquals("alpha", value.name);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializationWithCustomMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueWithCustomBuilder val = mapper.readValue("{\"count\":88}", ValueWithCustomBuilder.class);
        assertNotNull(val);
        assertEquals(88, val.count);
    }

    @Test(timeout = 4000)
    public void testStdDeserializerModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == String.class) {
                    modifierCalled[0] = true;
                }
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializerFactory(new BeanDeserializerFactory(config));

        String value = mapper.readValue("\"testValue\"", String.class);
        assertEquals("testValue", value);
        assertTrue("Deserializer modifier should have processed String type", modifierCalled[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsPotentialBeanTypeValid() {
        assertTrue(BeanDeserializerFactory.instance.isPotentialBeanType(SimpleBean.class));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypePrimitiveThrows() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeArrayThrows() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeLocalClassThrows() {
        class LocalInnerClass {}
        BeanDeserializerFactory.instance.isPotentialBeanType(LocalInnerClass.class);
    }

    @Test(timeout = 4000)
    public void testAnySetterMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterMethodBean bean = mapper.readValue("{\"prop1\":\"v1\",\"prop2\":99}", AnySetterMethodBean.class);
        assertNotNull(bean);
        assertEquals("v1", bean.getProps().get("prop1"));
        assertEquals(99, bean.getProps().get("prop2"));
    }

    @Test(timeout = 4000)
    public void testAnySetterField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterFieldBean bean = mapper.readValue("{\"foo\":\"bar\",\"active\":true}", AnySetterFieldBean.class);
        assertNotNull(bean);
        assertEquals("bar", bean.props.get("foo"));
        assertEquals(Boolean.TRUE, bean.props.get("active"));
    }

    @Test(timeout = 4000)
    public void testSetterlessCollectionProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS));

        SetterlessListBean bean = mapper.readValue("{\"items\":[\"apple\",\"banana\"]}", SetterlessListBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.getItems().size());
        assertEquals("apple", bean.getItems().get(0));
        assertEquals("banana", bean.getItems().get(1));
    }

    @Test(timeout = 4000)
    public void testViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        ViewBean bean = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue("{\"pub\":100,\"priv\":200}");
        assertNotNull(bean);
        assertEquals(100, bean.pub);
        assertEquals(0, bean.priv);
    }

    @Test(timeout = 4000)
    public void testObjectIdReaderPropertyGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":555,\"link\":555}";
        PropertyIdBean bean = mapper.readValue(json, PropertyIdBean.class);
        assertNotNull(bean);
        assertEquals(555, bean.id);
        assertSame(bean, bean.link);
    }

    @Test(timeout = 4000)
    public void testObjectIdReaderSequenceGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@id\":1,\"value\":\"node\",\"next\":1}";
        SequenceIdBean bean = mapper.readValue(json, SequenceIdBean.class);
        assertNotNull(bean);
        assertEquals("node", bean.value);
        assertSame(bean, bean.next);
    }

    @Test(timeout = 4000)
    public void testInjectables() throws Exception {
        InjectableValues.Std iv = new InjectableValues.Std();
        iv.addValue("injectedKey", "resolvedInjection");

        ObjectMapper mapper = new ObjectMapper();
        InjectedBean bean = mapper.reader(iv)
                .forType(InjectedBean.class)
                .readValue("{\"name\":\"base\"}");
        assertNotNull(bean);
        assertEquals("resolvedInjection", bean.injected);
        assertEquals("base", bean.name);
    }

    @Test(timeout = 4000)
    public void testIgnoredTypeProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ContainerWithIgnoredType container = mapper.readValue(
                "{\"ignored\":{\"data\":\"skipped\"},\"value\":\"persisted\"}",
                ContainerWithIgnoredType.class);
        assertNotNull(container);
        assertNull(container.ignored);
        assertEquals("persisted", container.value);
    }

    @Test(timeout = 4000)
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"first\":\"Bob\",\"age\":25}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.first);
        assertEquals(25, bean.age);
    }

    @Test(timeout = 4000)
    public void testManagedAndBackReferences() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Engineering\",\"employees\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}";
        Department dept = mapper.readValue(json, Department.class);
        assertNotNull(dept);
        assertEquals("Engineering", dept.name);
        assertEquals(2, dept.employees.size());
        assertSame(dept, dept.employees.get(0).dept);
        assertSame(dept, dept.employees.get(1).dept);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSubtypeWithoutWithConfigThrows() {
        UnoverriddenSubFactory subFactory = new UnoverriddenSubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    @Test(timeout = 4000)
    public void testCheckIllegalTypesTriggersException() throws Exception {
        CustomIllegalFactory factory = new CustomIllegalFactory();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        try {
            factory.checkIllegalTypes(ctxt, type, beanDesc);
            fail("Expected JsonMappingException for illegal class");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
            assertTrue(e.getMessage().contains("prevented for security reasons"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidObjectIdPropertyThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"id\":1}", InvalidIdBean.class);
            fail("Expected exception for missing ObjectId property");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("can not find property with name 'missingId'"));
        }
    }

    @Test(timeout = 4000)
    public void testBackReferenceAsCreatorParameterThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"parent\":{}}", ChildWithBackRefCreator.class);
            fail("Expected JsonMappingException for back-reference used as creator param");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not bind back references as Creator parameters"));
        }
    }

    @Test(timeout = 4000)
    public void testAbstractClassWithoutCreatorThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"name\":\"abstractVal\"}", AbstractNoCreator.class);
            fail("Expected JsonMappingException for abstract type without creator");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializerModifierLifecycle() throws Exception {
        final boolean[] flags = new boolean[3]; // [updateProperties, updateBuilder, modifyDeserializer]
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                flags[0] = true;
                return propDefs;
            }

            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                flags[1] = true;
                return builder;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                flags[2] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory factory = new BeanDeserializerFactory(config);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializerFactory(factory);

        SimpleBean bean = mapper.readValue("{\"name\":\"lifecycle\"}", SimpleBean.class);
        assertEquals("lifecycle", bean.name);
        assertTrue("updateProperties callback should have run", flags[0]);
        assertTrue("updateBuilder callback should have run", flags[1]);
        assertTrue("modifyDeserializer callback should have run", flags[2]);
    }

    @Test(timeout = 4000)
    public void testThrowableDeserializationAndDecorations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"message\":\"main error\","
                + "\"cause\":{\"message\":\"nested cause\"},"
                + "\"code\":503,"
                + "\"suppressed\":[],"
                + "\"localizedMessage\":\"ignored\"}";

        CustomThrowable throwable = mapper.readValue(json, CustomThrowable.class);
        assertNotNull(throwable);
        assertEquals("main error", throwable.getMessage());
        assertEquals(503, throwable.code);
        assertNotNull(throwable.getCause());
        assertEquals("nested cause", throwable.getCause().getMessage());
    }

    @Test(timeout = 4000)
    public void testDefaultNoDeserClassNamesIntegrity() {
        Set<String> illegalSet = BeanDeserializerFactory.DEFAULT_NO_DESER_CLASS_NAMES;
        assertNotNull(illegalSet);
        assertTrue(illegalSet.contains("org.apache.commons.collections.functors.InvokerTransformer"));
        assertTrue(illegalSet.contains("org.springframework.beans.factory.ObjectFactory"));
        assertTrue(illegalSet.contains("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));

        try {
            illegalSet.add("should.fail");
            fail("Set must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected unmodifiable behavior
        }
    }

    @Test(timeout = 4000)
    public void testIsIgnorableTypeCaching() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspectClassAnnotations(ContainerWithIgnoredType.class);
        Map<Class<?>, Boolean> cache = new HashMap<>();

        boolean ignorableFirstCall = factory.isIgnorableType(config, beanDesc, IgnoredType.class, cache);
        assertTrue(ignorableFirstCall);
        assertTrue(cache.containsKey(IgnoredType.class));

        // Second call should return cached status
        boolean ignorableSecondCall = factory.isIgnorableType(config, beanDesc, IgnoredType.class, cache);
        assertTrue(ignorableSecondCall);

        boolean nonIgnorable = factory.isIgnorableType(config, beanDesc, SimpleBean.class, cache);
        assertFalse(nonIgnorable);
        assertTrue(cache.containsKey(SimpleBean.class));
    }
}