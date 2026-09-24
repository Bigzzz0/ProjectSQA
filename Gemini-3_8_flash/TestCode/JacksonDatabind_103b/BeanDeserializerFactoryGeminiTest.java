package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
 *
 * Targeted Decision Branches & Boundaries:
 * 1. withConfig(config):
 *    - config == _factoryConfig (same instance -> return this)
 *    - config != _factoryConfig (different instance -> verifyMustOverride check, return new factory)
 *    - subclass without overriding withConfig -> throws IllegalStateException
 * 2. createBeanDeserializer:
 *    - custom deserializer present -> returns custom
 *    - type.isThrowable() -> buildThrowableDeserializer (initCause, ignorable localizedMessage/suppressed)
 *    - type.isAbstract() && !primitive && !enum -> materializeAbstractType
 *    - isPotentialBeanType checks: primitive, array, local class (throws IllegalArgumentException)
 *    - _validateSubType check execution
 * 3. createBuilderBasedDeserializer / buildBuilderBasedDeserializer:
 *    - Custom POJO builder with @JsonPOJOBuilder and custom buildMethodName
 * 4. addObjectIdReader:
 *    - Property-based generator: property exists vs. property missing (IllegalArgumentException)
 *    - Non-property generator (IntSequenceGenerator)
 * 5. constructAnySetter:
 *    - AnnotatedMethod (2-arg setter)
 *    - AnnotatedField (Map field)
 * 6. addBeanProps:
 *    - Settable properties: Setter vs. Field vs. Setterless (Collection/Map getters)
 *    - @JsonIgnoreProperties handling
 *    - Creator properties matching constructor arguments
 *    - Views handling (findViews)
 * 7. Modifiers (BeanDeserializerModifier):
 *    - updateBuilder, updateProperties, modifyDeserializer hooks
 * 8. Defect Focus (BasicExceptionTest::testLocationAddition & Exception formatting):
 *    - Ensuring exception paths from map key deserialization failures do not duplicate location markers
 *    - Throwables deserialized with causes and suppressed properties without infinite loop/duplicate path
 */
public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test POJOs and Fixtures
    // =========================================================================

    public static class SimpleBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class FieldOnlyBean {
        public String data;
        public int count;
    }

    public static class SetterlessCollectionBean {
        private final List<String> items = new ArrayList<>();
        private final Map<String, Integer> mapping = new HashMap<>();

        public List<String> getItems() { return items; }
        public Map<String, Integer> getMapping() { return mapping; }
    }

    @JsonIgnoreProperties({"secret", "internalId"})
    public static class IgnoralsBean {
        public String visible;
        public String secret;
        public String internalId;
    }

    public static class AnySetterMethodBean {
        private final Map<String, Object> other = new HashMap<>();

        @JsonAnySetter
        public void handleUnknown(String key, Object value) {
            other.put(key, value);
        }

        public Map<String, Object> getOther() { return other; }
    }

    public static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> extra = new HashMap<>();
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    public static class ValueClass {
        final int x;
        final String y;

        ValueClass(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    public static class ValueClassBuilder {
        int x;
        String y;

        public ValueClassBuilder setX(int x) { this.x = x; return this; }
        public ValueClassBuilder setY(String y) { this.y = y; return this; }
        public ValueClass create() { return new ValueClass(x, y); }
    }

    public static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;
        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class IdentifiableBean {
        public int value;
        public IdentifiableBean next;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistentProp")
    public static class BrokenIdBean {
        public int id;
    }

    public static class ParentBean {
        public String name;
        @JsonManagedReference
        public ChildBean child;
    }

    public static class ChildBean {
        public int age;
        @JsonBackReference
        public ParentBean parent;
    }

    public static class InjectableBean {
        @JacksonInject("injectedId")
        public String id;
        public String normal;
    }

    public interface AbstractService {
        String execute();
    }

    public static class ConcreteServiceImpl implements AbstractService {
        private String message;
        public ConcreteServiceImpl() {}
        public ConcreteServiceImpl(String msg) { this.message = msg; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        @Override
        public String execute() { return message; }
    }

    public static class ViewBean {
        public interface PublicView {}
        public interface PrivateView {}

        @JsonView(PublicView.class)
        public String pub;

        @JsonView(PrivateView.class)
        public String priv;
    }

    public static class CreatorBean {
        public final String first;
        public final int second;

        @JsonCreator
        public CreatorBean(@JsonProperty("first") String first, @JsonProperty("second") int second) {
            this.first = first;
            this.second = second;
        }
    }

    public enum ABC { A, B, C }

    private static class SubclassWithoutOverride extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public SubclassWithoutOverride(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigSameAndDifferent() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig config1 = factory.getFactoryConfig();
        assertSame("Passing identical config should return same instance", factory, factory.withConfig(config1));

        DeserializerFactoryConfig config2 = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config2);
        assertNotSame("Passing distinct config should return new instance", factory, newFactory);
        assertEquals(BeanDeserializerFactory.class, newFactory.getClass());
    }

    @Test(timeout = 4000)
    public void testStandardBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Alice", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test(timeout = 4000)
    public void testFieldOnlyBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldOnlyBean bean = mapper.readValue("{\"data\":\"test\",\"count\":5}", FieldOnlyBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.data);
        assertEquals(5, bean.count);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueClass value = mapper.readValue("{\"x\":42,\"y\":\"foo\"}", ValueClass.class);
        assertNotNull(value);
        assertEquals(42, value.x);
        assertEquals("foo", value.y);
    }

    @Test(timeout = 4000)
    public void testBuildThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"message\":\"something bad\",\"cause\":{\"message\":\"nested root\"}}";
        CustomException ex = mapper.readValue(json, CustomException.class);
        assertNotNull(ex);
        assertEquals("something bad", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("nested root", ex.getCause().getMessage());
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType() throws Exception {
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractService.class, ConcreteServiceImpl.class);
        module.setAbstractTypes(resolver);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        AbstractService svc = mapper.readValue("{\"message\":\"working\"}", AbstractService.class);
        assertNotNull(svc);
        assertTrue(svc instanceof ConcreteServiceImpl);
        assertEquals("working", svc.execute());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetterlessCollectionAndMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
        String json = "{\"items\":[\"a\",\"b\"],\"mapping\":{\"key\":100}}";
        SetterlessCollectionBean bean = mapper.readValue(json, SetterlessCollectionBean.class);
        assertNotNull(bean);
        assertEquals(2, bean.getItems().size());
        assertTrue(bean.getItems().contains("a"));
        assertTrue(bean.getItems().contains("b"));
        assertEquals(Integer.valueOf(100), bean.getMapping().get("key"));
    }

    @Test(timeout = 4000)
    public void testIgnoralsBeanExplicitAndUnknown() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"visible\":\"yes\",\"secret\":\"hidden\",\"internalId\":\"id123\",\"extra\":\"ignored\"}";
        IgnoralsBean bean = mapper.readValue(json, IgnoralsBean.class);
        assertNotNull(bean);
        assertEquals("yes", bean.visible);
        assertNull("secret must be ignored", bean.secret);
        assertNull("internalId must be ignored", bean.internalId);
    }

    @Test(timeout = 4000)
    public void testAnySetterMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"prop1\":\"v1\",\"prop2\":99}";
        AnySetterMethodBean bean = mapper.readValue(json, AnySetterMethodBean.class);
        assertNotNull(bean);
        assertEquals("v1", bean.getOther().get("prop1"));
        assertEquals(99, bean.getOther().get("prop2"));
    }

    @Test(timeout = 4000)
    public void testAnySetterField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"f1\":true,\"f2\":\"hello\"}";
        AnySetterFieldBean bean = mapper.readValue(json, AnySetterFieldBean.class);
        assertNotNull(bean);
        assertEquals(Boolean.TRUE, bean.extra.get("f1"));
        assertEquals("hello", bean.extra.get("f2"));
    }

    @Test(timeout = 4000)
    public void testObjectIdGenerators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonSeq = "{\"@id\":1,\"value\":10,\"next\":{\"@id\":2,\"value\":20,\"next\":1}}";
        IdentifiableBean node1 = mapper.readValue(jsonSeq, IdentifiableBean.class);
        assertNotNull(node1);
        assertEquals(10, node1.value);
        assertNotNull(node1.next);
        assertEquals(20, node1.next.value);
        assertSame("Circular reference should resolve to node1", node1, node1.next.next);

        String jsonProp = "{\"id\":77,\"name\":\"Object77\"}";
        PropertyIdBean propBean = mapper.readValue(jsonProp, PropertyIdBean.class);
        assertNotNull(propBean);
        assertEquals(77, propBean.id);
        assertEquals("Object77", propBean.name);
    }

    @Test(timeout = 4000)
    public void testBackReferenceHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Parent\",\"child\":{\"age\":5}}";
        ParentBean parent = mapper.readValue(json, ParentBean.class);
        assertNotNull(parent);
        assertEquals("Parent", parent.name);
        assertNotNull(parent.child);
        assertEquals(5, parent.child.age);
        assertSame("Child back-reference must link to parent", parent, parent.child.parent);
    }

    @Test(timeout = 4000)
    public void testInjectablesHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedId", "INJ-999");
        mapper.setInjectableValues(injectables);

        InjectableBean bean = mapper.readValue("{\"normal\":\"user-val\"}", InjectableBean.class);
        assertNotNull(bean);
        assertEquals("INJ-999", bean.id);
        assertEquals("user-val", bean.normal);
    }

    @Test(timeout = 4000)
    public void testJsonViewsFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";
        ViewBean publicBean = mapper.readerWithView(ViewBean.PublicView.class)
                .forType(ViewBean.class)
                .readValue(json);
        assertNotNull(publicBean);
        assertEquals("publicVal", publicBean.pub);
        assertNull(publicBean.priv);
    }

    @Test(timeout = 4000)
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"second\":123,\"first\":\"hello\"}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals("hello", bean.first);
        assertEquals(123, bean.second);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J Ground Truth: BasicExceptionTest::testLocationAddition
     * Deserializing map key of enum with bad string should produce a clear error
     * without duplicating "at [" location markers.
     */
    @Test(timeout = 4000)
    public void testMapKeyDeserializationErrorSingleLocation() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"value\": 123}", new TypeReference<Map<ABC, String>>() {});
            fail("Should have failed on invalid enum key");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull(msg);
            int firstAt = msg.indexOf("at [");
            assertTrue("Should contain location marker 'at ['", firstAt >= 0);
            int secondAt = msg.indexOf("at [", firstAt + 4);
            assertEquals("Should only get one 'at [' marker, but got multiple in: " + msg, -1, secondAt);
        } catch (Exception other) {
            fail("Expected JsonMappingException, got: " + other.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testBrokenObjectIdThrowsIllegalArgumentException() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"id\":1}", BrokenIdBean.class);
            fail("Should fail because nonExistentProp does not exist in BrokenIdBean");
        } catch (JsonMappingException e) {
            Throwable cause = e.getCause();
            assertTrue("Cause should be IllegalArgumentException, was " + cause,
                    cause instanceof IllegalArgumentException || e.getMessage().contains("nonExistentProp"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubclassMustOverrideWithConfig() {
        SubclassWithoutOverride badSubclass = new SubclassWithoutOverride(new DeserializerFactoryConfig());
        try {
            badSubclass.withConfig(new DeserializerFactoryConfig());
            fail("Expected IllegalStateException due to missing withConfig override");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Subtype") || e.getMessage().contains("withConfig"));
        }
    }

    @Test(timeout = 4000)
    public void testIsPotentialBeanTypeRejections() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;

        try {
            factory.isPotentialBeanType(int.class);
            fail("Primitive type must not be a potential bean type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }

        try {
            factory.isPotentialBeanType(String[].class);
            fail("Array type must not be a potential bean type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }

        class InMethodLocalClass {
            public int x;
        }

        try {
            factory.isPotentialBeanType(InMethodLocalClass.class);
            fail("Local class must not be a potential bean type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("as a Bean"));
        }
    }

    @Test(timeout = 4000)
    public void testAbstractTypeWithoutInstantiationFailsClearly() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"execute\":\"test\"}", AbstractService.class);
            fail("Should fail to instantiate abstract class without resolver or default implementation");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryModifiers() throws Exception {
        final boolean[] modifierInvoked = new boolean[]{false, false, false};
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                modifierInvoked[0] = true;
                return builder;
            }

            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                modifierInvoked[1] = true;
                return propDefs;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierInvoked[2] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) BeanDeserializerFactory.instance.withConfig(config);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializerFactory(customFactory);

        SimpleBean bean = mapper.readValue("{\"name\":\"Bob\",\"age\":25}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.getName());
        assertEquals(25, bean.getAge());

        assertTrue("updateBuilder should have been called", modifierInvoked[0]);
        assertTrue("updateProperties should have been called", modifierInvoked[1]);
        assertTrue("modifyDeserializer should have been called", modifierInvoked[2]);
    }

    @Test(timeout = 4000)
    public void testSerializationOfFactoryInstance() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(factory);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object deserialized = ois.readObject();
            assertNotNull(deserialized);
            assertEquals(BeanDeserializerFactory.class, deserialized.getClass());
        }
    }
}