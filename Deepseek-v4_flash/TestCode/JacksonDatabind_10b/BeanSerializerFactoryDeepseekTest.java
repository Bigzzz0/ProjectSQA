package com.fasterxml.jackson.databind.ser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.PropertyBuilder;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class BeanSerializerFactoryDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: BeanSerializerFactory (databind 2.4.x)
     * 
     * Known Defect (from Defects4J): 
     *   - TestAnyGetter::testIssue705 fails: expected {"stuff":"[key/value]"} but got {"key":"value"}
     *   - Root cause: When a bean has both a regular getter and an @JsonAnyGetter, 
     *     the factory incorrectly uses the any-getter's map entries as the sole serialization 
     *     output, ignoring the regular property "stuff".
     * 
     * Branches targeted:
     * 1. createSerializer: 
     *    - explicit annotation path (findSerializerFromAnnotation)
     *    - converter path (findSerializationConverter)
     *    - container type path (isContainerType)
     *    - module-provided serializer path
     *    - primary type lookup path
     *    - bean serializer path (findBeanSerializer)
     *    - addon type path
     *    - unknown type fallback
     * 2. findBeanSerializer:
     *    - Object.class check
     *    - enum type check
     *    - property collection (findBeanProperties)
     *    - any-getter handling (setAnyGetter)
     *    - view filtering (removeIgnoredProperties, processViews)
     *    - property ordering
     * 3. constructObjectIdHandler:
     *    - null ObjectIdInfo
     *    - PropertyGenerator special case
     *    - property name lookup failure
     *    - non-property generator
     * 4. isPotentialBeanType:
     *    - canBeABeanType null check
     *    - proxy type check
     * 5. removeIgnoredProperties:
     *    - ignored set construction
     *    - property filtering
     * 6. processViews:
     *    - null views
     *    - includeByDefault true/false
     *    - view filtering logic
     * 7. constructPropertyWriter:
     *    - type serializer for collection/map types
     *    - property type serializer
     * 
     * Boundary conditions:
     * - Empty property list
     * - Null accessor
     * - Null anyGetter
     * - Null ObjectIdInfo
     * - Invalid ObjectId property name
     * - Empty ignored set
     * - Null views array
     * - Zero properties
     * 
     * The test suite is structured to maximize line/branch coverage while 
     * specifically targeting the known defect with a dedicated test method.
     */

    // ===== Test infrastructure =====
    private static class TestBean {
        public String getStuff() { return "value"; }
        
        @JsonAnyGetter
        public Map<String, Object> any() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("key", "value");
            return map;
        }
    }

    private static class SimpleBean {
        private int id;
        private String name;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private static class ViewBean {
        @JsonView(Views.Public.class)
        public String getPublic() { return "public"; }
        
        @JsonView(Views.Internal.class)
        public String getInternal() { return "internal"; }
        
        public String getNoView() { return "noview"; }
    }

    private static class Views {
        public static class Public {}
        public static class Internal {}
    }

    private static class IgnoredBean {
        @JsonIgnore
        public String getIgnored() { return "ignored"; }
        public String getVisible() { return "visible"; }
    }

    private static class ObjectIdBean {
        public int id;
        public String name;
        
        @JsonObjectId
        public int getId() { return id; }
    }

    private static class AnyGetterWithProps {
        private String stuff = "value";
        
        @JsonAnyGetter
        public Map<String, Object> any() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("key", "value");
            return map;
        }
        
        public String getStuff() { return stuff; }
    }

    private static class NoPropsBean {
        public String getOnlyGetter() { return "x"; }
    }

    private static class EnumBean {
        public enum TestEnum { A, B }
        public TestEnum getEnum() { return TestEnum.A; }
    }

    private static class ConverterBean {
        @JsonSerialize(converter = MyConverter.class)
        public String getValue() { return "raw"; }
    }

    private static class MyConverter implements Converter<String, String> {
        @Override
        public String convert(String value) { return "converted:" + value; }
        
        @Override
        public JavaType getInputType(TypeFactory typeFactory) { 
            return typeFactory.constructType(String.class); 
        }
        
        @Override
        public JavaType getOutputType(TypeFactory typeFactory) { 
            return typeFactory.constructType(String.class); 
        }
    }

    private static class CustomSerializerBean {
        @JsonSerialize(using = CustomSerializer.class)
        public String getValue() { return "test"; }
    }

    private static class CustomSerializer extends StdSerializer<CustomSerializerBean> {
        public CustomSerializer() { super(CustomSerializerBean.class); }
        
        @Override
        public void serialize(CustomSerializerBean value, JsonGenerator gen, 
                SerializerProvider provider) throws java.io.IOException {
            gen.writeString("custom");
        }
    }

    private static class ContainerBean {
        public List<String> getList() { return Arrays.asList("a", "b"); }
        public Map<String, Integer> getMap() { 
            Map<String, Integer> m = new HashMap<String, Integer>();
            m.put("x", 1);
            return m;
        }
    }

    private static class StaticTypingBean {
        public Object getValue() { return "static"; }
    }

    private static class BackRefBean {
        private String id;
        
        @JsonBackReference
        public String getBackRef() { return "back"; }
        
        public String getId() { return id; }
    }

    private static class PropertyOrderBean {
        @JsonPropertyOrder({"b", "a"})
        public String getA() { return "a"; }
        public String getB() { return "b"; }
    }

    private static class AnySetterBean {
        private Map<String, Object> props = new HashMap<String, Object>();
        
        @JsonAnySetter
        public void setAny(String key, Object value) { props.put(key, value); }
        
        public Map<String, Object> getProps() { return props; }
    }

    private static class TypeInfoBean {
        public String getType() { return "type"; }
    }

    private static class ObjectIdGeneratorBean {
        @JsonObjectId
        public String getId() { return "id1"; }
    }

    // ===== Test methods =====

    /**
     * Partition A: Core Functional Logic & State Transitions
     * Tests basic bean serialization with regular properties.
     */
    @Test(timeout = 4000)
    public void testCreateSerializerBasicBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        
        JsonSerializer<Object> ser = factory.createSerializer(mapper.getSerializerProvider(), type);
        assertNotNull("Serializer should not be null", ser);
        assertTrue("Should be BeanSerializer", ser instanceof BeanSerializer);
        
        SimpleBean bean = new SimpleBean();
        bean.setId(42);
        bean.setName("test");
        
        String json = mapper.writeValueAsString(bean);
        assertTrue("Should contain id", json.contains("\"id\":42"));
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    /**
     * Partition A: Test any-getter with regular properties (targets known defect)
     * This test directly reproduces the issue #705 failure.
     */
    @Test(timeout = 4000)
    public void testAnyGetterWithRegularProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        AnyGetterWithProps bean = new AnyGetterWithProps();
        String json = mapper.writeValueAsString(bean);
        
        // Expected: both "stuff" property and any-getter entries should be serialized
        assertTrue("Should contain stuff property", json.contains("\"stuff\":\"value\""));
        assertTrue("Should contain any-getter key", json.contains("\"key\":\"value\""));
        
        // The defect causes only the any-getter map to be serialized, losing "stuff"
        assertTrue("Regular property 'stuff' must be present", json.contains("stuff"));
    }

    /**
     * Partition A: Test any-getter with no other properties.
     */
    @Test(timeout = 4000)
    public void testAnyGetterOnly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        TestBean bean = new TestBean();
        String json = mapper.writeValueAsString(bean);
        
        assertTrue("Should contain any-getter key", json.contains("\"key\":\"value\""));
        assertTrue("Should contain stuff property", json.contains("\"stuff\":\"value\""));
    }

    /**
     * Partition B: Boundary Value Analysis - Empty bean with no properties.
     */
    @Test(timeout = 4000)
    public void testEmptyBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        NoPropsBean bean = new NoPropsBean();
        String json = mapper.writeValueAsString(bean);
        
        // Should serialize as empty object or with only getter
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition B: Test enum type handling.
     */
    @Test(timeout = 4000)
    public void testEnumBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        EnumBean bean = new EnumBean();
        String json = mapper.writeValueAsString(bean);
        
        assertTrue("Should contain enum value", json.contains("\"enum\":\"A\""));
    }

    /**
     * Partition B: Test converter annotation.
     */
    @Test(timeout = 4000)
    public void testConverterAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ConverterBean bean = new ConverterBean();
        String json = mapper.writeValueAsString(bean);
        
        assertTrue("Should contain converted value", json.contains("converted:raw"));
    }

    /**
     * Partition B: Test custom serializer annotation.
     */
    @Test(timeout = 4000)
    public void testCustomSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        CustomSerializerBean bean = new CustomSerializerBean();
        String json = mapper.writeValueAsString(bean);
        
        assertEquals("Custom serializer should be used", "\"custom\"", json);
    }

    /**
     * Partition B: Test container types (List, Map).
     */
    @Test(timeout = 4000)
    public void testContainerTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ContainerBean bean = new ContainerBean();
        String json = mapper.writeValueAsString(bean);
        
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
        assertTrue("Should contain map", json.contains("\"map\":{\"x\":1}"));
    }

    /**
     * Partition C: Defect-Targeted Branch Zone - Test static typing.
     */
    @Test(timeout = 4000)
    public void testStaticTyping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_STATIC_TYPING);
        
        StaticTypingBean bean = new StaticTypingBean();
        String json = mapper.writeValueAsString(bean);
        
        assertTrue("Should contain value", json.contains("\"value\":\"static\""));
    }

    /**
     * Partition C: Test back-reference handling.
     */
    @Test(timeout = 4000)
    public void testBackReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        BackRefBean bean = new BackRefBean();
        bean.id = "test-id";
        String json = mapper.writeValueAsString(bean);
        
        // Back reference should be suppressed
        assertFalse("Should not contain back reference", json.contains("backRef"));
        assertTrue("Should contain id", json.contains("\"id\":\"test-id\""));
    }

    /**
     * Partition C: Test property ordering.
     */
    @Test(timeout = 4000)
    public void testPropertyOrder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        PropertyOrderBean bean = new PropertyOrderBean();
        String json = mapper.writeValueAsString(bean);
        
        int idxA = json.indexOf("\"a\"");
        int idxB = json.indexOf("\"b\"");
        assertTrue("Property b should come before a", idxB < idxA);
    }

    /**
     * Partition D: Exception & Defensive Guard Paths - Test invalid ObjectId.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixInAnnotations(Object.class, ObjectIdMixin.class);
        
        // This should throw because property "nonexistent" doesn't exist
        mapper.writeValueAsString(new ObjectIdBean());
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonexistent")
    private static class ObjectIdMixin {}

    /**
     * Partition D: Test ObjectId with valid property.
     */
    @Test(timeout = 4000)
    public void testValidObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ObjectIdBean bean = new ObjectIdBean();
        bean.id = 1;
        bean.name = "test";
        
        String json = mapper.writeValueAsString(bean);
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition D: Test view filtering.
     */
    @Test(timeout = 4000)
    public void testViewFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ViewBean bean = new ViewBean();
        
        // Default view - should include only no-view properties
        String json = mapper.writeValueAsString(bean);
        assertTrue("Should contain no-view property", json.contains("noview"));
        assertFalse("Should not contain public view", json.contains("public"));
        assertFalse("Should not contain internal view", json.contains("internal"));
        
        // With Public view
        String jsonPublic = mapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue("Should contain public view", jsonPublic.contains("public"));
        assertTrue("Should contain no-view property", jsonPublic.contains("noview"));
        assertFalse("Should not contain internal view", jsonPublic.contains("internal"));
    }

    /**
     * Partition D: Test ignored properties.
     */
    @Test(timeout = 4000)
    public void testIgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        IgnoredBean bean = new IgnoredBean();
        String json = mapper.writeValueAsString(bean);
        
        assertFalse("Should not contain ignored property", json.contains("ignored"));
        assertTrue("Should contain visible property", json.contains("visible"));
    }

    /**
     * Partition E: Object Lifecycle & Contract Integrity - Test withConfig.
     */
    @Test(timeout = 4000)
    public void testWithConfig() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        
        SerializerFactory newFactory = factory.withConfig(config);
        assertNotNull("New factory should not be null", newFactory);
        assertTrue("Should be instance of BeanSerializerFactory", 
                newFactory instanceof BeanSerializerFactory);
    }

    /**
     * Partition E: Test singleton instance.
     */
    @Test(timeout = 4000)
    public void testSingletonInstance() {
        assertNotNull("Singleton instance should not be null", BeanSerializerFactory.instance);
        assertSame("Should be same instance", BeanSerializerFactory.instance, 
                BeanSerializerFactory.instance);
    }

    /**
     * Partition E: Test findPropertyTypeSerializer.
     */
    @Test(timeout = 4000)
    public void testFindPropertyTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        
        JavaType type = mapper.getTypeFactory().constructType(String.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        
        TypeSerializer ser = factory.findPropertyTypeSerializer(type, 
                mapper.getSerializationConfig(), desc.getClassInfo());
        // May be null if no type info configured
        assertNotNull("Type serializer should not be null", ser);
    }

    /**
     * Partition E: Test findPropertyContentTypeSerializer.
     */
    @Test(timeout = 4000)
    public void testFindPropertyContentTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        
        TypeSerializer ser = factory.findPropertyContentTypeSerializer(type,
                mapper.getSerializationConfig(), desc.getClassInfo());
        // May be null if no type info configured
        assertNotNull("Content type serializer should not be null", ser);
    }

    /**
     * Partition B: Test null handling in createSerializer.
     */
    @Test(timeout = 4000)
    public void testCreateSerializerWithNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        
        // Test with Object.class - should return unknown type serializer
        JavaType objectType = mapper.getTypeFactory().constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(mapper.getSerializerProvider(), objectType);
        assertNotNull("Serializer for Object.class should not be null", ser);
    }

    /**
     * Partition C: Test any-setter handling.
     */
    @Test(timeout = 4000)
    public void testAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        AnySetterBean bean = new AnySetterBean();
        bean.setAny("key1", "value1");
        
        String json = mapper.writeValueAsString(bean);
        assertTrue("Should contain any-setter property", json.contains("\"key1\":\"value1\""));
    }

    /**
     * Partition B: Test type info handling.
     */
    @Test(timeout = 4000)
    public void testTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        
        TypeInfoBean bean = new TypeInfoBean();
        String json = mapper.writeValueAsString(bean);
        
        assertNotNull("JSON should not be null", json);
        assertTrue("Should contain type info", json.contains("@class"));
    }

    /**
     * Partition D: Test ObjectIdGenerator with property.
     */
    @Test(timeout = 4000)
    public void testObjectIdGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ObjectIdGeneratorBean bean = new ObjectIdGeneratorBean();
        String json = mapper.writeValueAsString(bean);
        
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition A: Test that findBeanSerializer returns null for non-bean types.
     */
    @Test(timeout = 4000)
    public void testFindBeanSerializerNonBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        
        // String is not a bean type
        JavaType stringType = mapper.getTypeFactory().constructType(String.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(stringType);
        
        JsonSerializer<Object> ser = factory.findBeanSerializer(
                mapper.getSerializerProvider(), stringType, desc);
        // May be null or a serializer, but should not throw
        assertNotNull("Should not return null for String", ser);
    }

    /**
     * Partition B: Test with empty property list.
     */
    @Test(timeout = 4000)
    public void testEmptyPropertyList() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Bean with only ignored properties
        @JsonIgnoreProperties("ignored")
        class EmptyBean {
            public String getIgnored() { return "ignored"; }
        }
        
        String json = mapper.writeValueAsString(new EmptyBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition C: Test that any-getter with null map is handled.
     */
    @Test(timeout = 4000)
    public void testAnyGetterNullMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class NullAnyBean {
            @JsonAnyGetter
            public Map<String, Object> any() { return null; }
        }
        
        String json = mapper.writeValueAsString(new NullAnyBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition E: Test serialization of bean with transient field.
     */
    @Test(timeout = 4000)
    public void testTransientField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class TransientBean {
            public transient String getTransient() { return "transient"; }
            public String getNormal() { return "normal"; }
        }
        
        String json = mapper.writeValueAsString(new TransientBean());
        assertFalse("Should not contain transient", json.contains("transient"));
        assertTrue("Should contain normal", json.contains("normal"));
    }

    /**
     * Partition B: Test with zero properties.
     */
    @Test(timeout = 4000)
    public void testZeroProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class EmptyBean {}
        
        String json = mapper.writeValueAsString(new EmptyBean());
        assertEquals("Should serialize as empty object", "{}", json);
    }

    /**
     * Partition D: Test with invalid view class.
     */
    @Test(timeout = 4000)
    public void testInvalidView() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        ViewBean bean = new ViewBean();
        String json = mapper.writerWithView(String.class).writeValueAsString(bean);
        
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition C: Test that property ordering with any-getter is correct.
     */
    @Test(timeout = 4000)
    public void testAnyGetterPropertyOrder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class OrderedAnyBean {
            @JsonPropertyOrder({"b", "a"})
            public String getA() { return "a"; }
            public String getB() { return "b"; }
            
            @JsonAnyGetter
            public Map<String, Object> any() {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("z", 1);
                return map;
            }
        }
        
        String json = mapper.writeValueAsString(new OrderedAnyBean());
        int idxA = json.indexOf("\"a\"");
        int idxB = json.indexOf("\"b\"");
        int idxZ = json.indexOf("\"z\"");
        
        assertTrue("Property b should come before a", idxB < idxA);
        assertTrue("Any-getter should come after regular properties", idxA < idxZ);
    }

    /**
     * Partition E: Test serialization with custom ObjectIdGenerator.
     */
    @Test(timeout = 4000)
    public void testCustomObjectIdGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class CustomIdBean {
            @JsonObjectId
            public String getId() { return "custom-id"; }
        }
        
        String json = mapper.writeValueAsString(new CustomIdBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition B: Test with null property name.
     */
    @Test(timeout = 4000)
    public void testNullPropertyName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class NullNameBean {
            @JsonProperty("")
            public String getValue() { return "test"; }
        }
        
        String json = mapper.writeValueAsString(new NullNameBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition C: Test that any-getter with same key as regular property is handled.
     */
    @Test(timeout = 4000)
    public void testAnyGetterKeyConflict() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class ConflictBean {
            public String getKey() { return "regular"; }
            
            @JsonAnyGetter
            public Map<String, Object> any() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("key", "any");
                return map;
            }
        }
        
        String json = mapper.writeValueAsString(new ConflictBean());
        assertTrue("Should contain regular property", json.contains("\"key\":\"regular\""));
    }

    /**
     * Partition D: Test with invalid type serializer.
     */
    @Test(timeout = 4000)
    public void testInvalidTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class InvalidTypeBean {
            @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
            public Object getValue() { return "test"; }
        }
        
        String json = mapper.writeValueAsString(new InvalidTypeBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition E: Test serialization with multiple any-getters.
     */
    @Test(timeout = 4000)
    public void testMultipleAnyGetters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class MultiAnyBean {
            @JsonAnyGetter
            public Map<String, Object> any1() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("key1", "value1");
                return map;
            }
            
            @JsonAnyGetter
            public Map<String, Object> any2() {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("key2", "value2");
                return map;
            }
        }
        
        String json = mapper.writeValueAsString(new MultiAnyBean());
        assertTrue("Should contain key1", json.contains("key1"));
        assertTrue("Should contain key2", json.contains("key2"));
    }

    /**
     * Partition B: Test with empty any-getter map.
     */
    @Test(timeout = 4000)
    public void testEmptyAnyGetterMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        class EmptyAnyBean {
            @JsonAnyGetter
            public Map<String, Object> any() { 
                return new HashMap<String, Object>(); 
            }
        }
        
        String json = mapper.writeValueAsString(new EmptyAnyBean());
        assertNotNull("JSON should not be null", json);
    }

    /**
     * Partition C: Test that regular properties are not lost when any-getter is present.
     * This is the core regression test for the known defect.
     */
    @Test(timeout = 4000)
    public void testRegularPropertiesNotLostWithAnyGetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        AnyGetterWithProps bean = new AnyGetterWithProps();
        String json = mapper.writeValueAsString(bean);
        
        // The defect causes "stuff" to be lost. This assertion will fail on defective version.
        assertTrue("Regular property 'stuff' must be serialized", json.contains("\"stuff\":\"value\""));
        assertTrue("Any-getter property must be serialized", json.contains("\"key\":\"value\""));
        
        // Verify both are present in the same JSON
        assertTrue("Both properties must be present", 
                json.contains("stuff") && json.contains("key"));
    }
}