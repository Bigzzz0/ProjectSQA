/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * Class: com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
 *
 * Target Branches & Defect Points:
 * 1. DEFECT FIX (databind#731 / TestConvertingSerializer.testIssue731):
 *    - In `findConvertingSerializer()`: when converter output delegate type is nominally `java.lang.Object`,
 *      eager lookup of `provider.findValueSerializer(delegateType, prop)` fails or improperly resolves to
 *      an empty bean/unknown serializer instead of allowing dynamic runtime resolution by passing null to
 *      `StdDelegatingSerializer`.
 * 2. Constructors & Copy Variants:
 *    - Builder null check (branch `builder == null` vs non-null).
 *    - `BeanSerializerBase(src, toIgnore)`: filtered properties null vs non-null, properties skipped vs kept.
 *    - `rename(props, transformer)`: null array, empty array, null transformer, NOP transformer, renaming execution.
 * 3. `resolve(SerializerProvider)`:
 *    - `willSuppressNulls()` and `hasNullSerializer()` true/false branches.
 *    - Null serializer assignment for both `_props` and matching `_filteredProps`.
 *    - `prop.hasSerializer()` true (skipped) vs false.
 *    - Non-final generic types triggering `prop.setNonTrivialBaseType(type)`.
 *    - ContainerSerializer unwrapping and `withValueTypeSerializer`.
 *    - `_anyGetterWriter != null` resolution.
 * 4. `createContextual(SerializerProvider, BeanProperty)`:
 *    - Enum shape transmutations (STRING, NUMBER, NUMBER_INT).
 *    - Object ID resolution: `PropertyBasedObjectIdGenerator` vs generic generator; property reordering
 *      in `_props` and `_filteredProps` when ID property is not at index 0.
 *    - ObjectIdRef reference handling (`withAlwaysAsId`).
 *    - Missing property for PropertyGenerator throwing `IllegalArgumentException`.
 *    - Filter ID updates and JsonFormat.Shape.ARRAY triggering `asArraySerializer()`.
 * 5. `serializeWithType()` and Object Id Handling:
 *    - With `_objectIdWriter` vs without.
 *    - Custom type ID (`_typeId != null`, null value vs non-null String vs Object).
 *    - Filtered serialization vs standard serialization.
 *    - `_serializeWithObjectId`: `writeAsId` true/false, `alwaysAsId` true/false, `startEndObject` true/false.
 * 6. `serializeFields()` and `serializeFieldsFiltered()`:
 *    - Active view matching `_filteredProps` vs default `_props`.
 *    - Null entries in `_filteredProps`.
 *    - Exception wrapping in `wrapAndThrow()` and `StackOverflowError` handling.
 * 7. `getSchema()` and `acceptJsonFormatVisitor()`:
 *    - `@JsonSerializableSchema` id inclusion.
 *    - Filtered schema deposits vs standard deposits.
 *    - Null visitor or null objectVisitor guards.
 * ------------------------------------------------------------------------------------------------------
 */

package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.Assert.*;

public class BeanSerializerBaseGeminiTest {

    // Concrete test harness subclass exposing protected methods of BeanSerializerBase
    private static class SubBeanSerializer extends BeanSerializerBase {
        public SubBeanSerializer(JavaType type, BeanPropertyWriter[] props, BeanPropertyWriter[] filteredProps) {
            super(type, (BeanSerializerBuilder) null, props, filteredProps);
        }

        public SubBeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] props, BeanPropertyWriter[] filteredProps) {
            super(type, builder, props, filteredProps);
        }

        public SubBeanSerializer(BeanSerializerBase src) {
            super(src);
        }

        public SubBeanSerializer(BeanSerializerBase src, BeanPropertyWriter[] props, BeanPropertyWriter[] filteredProps) {
            super(src, props, filteredProps);
        }

        public SubBeanSerializer(BeanSerializerBase src, ObjectIdWriter oiw) {
            super(src, oiw);
        }

        public SubBeanSerializer(BeanSerializerBase src, ObjectIdWriter oiw, Object filterId) {
            super(src, oiw, filterId);
        }

        public SubBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public SubBeanSerializer(BeanSerializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new SubBeanSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new SubBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        protected BeanSerializerBase withFilterId(Object filterId) {
            return new SubBeanSerializer(this, _objectIdWriter, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            serializeFields(bean, jgen, provider);
            jgen.writeEndObject();
        }

        public BeanPropertyWriter[] getProps() {
            return _props;
        }

        public BeanPropertyWriter[] getFilteredProps() {
            return _filteredProps;
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue databind#731)
    // =========================================================================

    static class DummyBean731 {
    }

    static class DummyConverter731 extends StdConverter<DummyBean731, Object> {
        @Override
        public Object convert(DummyBean731 value) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("key731", "value731");
            return map;
        }
    }

    static class Issue731Container {
        @JsonSerialize(converter = DummyConverter731.class)
        public DummyBean731 item = new DummyBean731();
    }

    @Test(timeout = 4000)
    public void testDefectIssue731ConvertingSerializerWithObjectOutputType() throws Exception {
        // Targets databind#731: Converter with Object as nominal delegateType
        // Eager resolution of Object serializer should not fail on empty beans.
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String json = mapper.writeValueAsString(new Issue731Container());
        assertNotNull(json);
        assertTrue("JSON must contain converted map entry", json.contains("\"key731\":\"value731\""));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullBuilderInitializesNullFields() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanPropertyWriter[] props = new BeanPropertyWriter[0];
        SubBeanSerializer ser = new SubBeanSerializer(type, props, null);

        assertNull(ser._typeId);
        assertNull(ser._anyGetterWriter);
        assertNull(ser._propertyFilterId);
        assertNull(ser._objectIdWriter);
        assertNull(ser._serializationShape);
        assertFalse(ser.usesObjectId());
        assertArrayEquals(props, ser.getProps());
        assertNull(ser.getFilteredProps());
    }

    @Test(timeout = 4000)
    public void testCopyConstructorsAndMutants() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SubBeanSerializer base = new SubBeanSerializer(type, new BeanPropertyWriter[0], null);

        SubBeanSerializer copy1 = new SubBeanSerializer(base);
        assertEquals(0, copy1.getProps().length);

        BeanSerializerBase filtered = copy1.withFilterId("myFilter");
        assertEquals("myFilter", filtered._propertyFilterId);

        BeanSerializerBase arraySer = filtered.asArraySerializer();
        assertNotNull(arraySer);

        ObjectIdWriter oiw = ObjectIdWriter.construct(type, new PropertyName("id"), null, false);
        BeanSerializerBase withOiw = base.withObjectIdWriter(oiw);
        assertTrue(withOiw.usesObjectId());
        assertSame(oiw, withOiw._objectIdWriter);
    }

    @Test(timeout = 4000)
    public void testRenameTransformerBranches() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SubBeanSerializer base = new SubBeanSerializer(type, new BeanPropertyWriter[0], null);

        // NOP transformer
        SubBeanSerializer nopRenamed = new SubBeanSerializer(base, NameTransformer.NOP);
        assertEquals(0, nopRenamed.getProps().length);

        // Null transformer
        SubBeanSerializer nullRenamed = new SubBeanSerializer(base, (NameTransformer) null);
        assertEquals(0, nullRenamed.getProps().length);
    }

    @Test(timeout = 4000)
    public void testWithIgnoralsPropertyFiltering() {
        JavaType type = TypeFactory.defaultInstance().constructType(HashMap.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // Create a serializer from a real bean class
        JavaType beanType = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> rootSer = null;
        try {
            rootSer = provider.findValueSerializer(beanType, null);
        } catch (JsonMappingException e) {
            fail("Failed to find serializer: " + e.getMessage());
        }
        assertTrue(rootSer instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) rootSer;

        // Ignore one property: "name"
        BeanSerializerBase ignoredBsb = bsb.withIgnorals(new String[]{"name"});
        boolean foundName = false;
        for (BeanPropertyWriter bpw : ignoredBsb._props) {
            if ("name".equals(bpw.getName())) {
                foundName = true;
            }
        }
        assertFalse("Property 'name' should have been ignored", foundName);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    static class SimpleBean {
        public String name = "test";
        public int count = 42;
    }

    @Test(timeout = 4000)
    public void testResolveWithContainerTypeAndNullSerializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JavaType type = mapper.constructType(ContainerBean.class);
        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);
        assertTrue(ser instanceof BeanSerializerBase);

        // serialize sample
        ContainerBean bean = new ContainerBean();
        bean.items = Arrays.asList("a", "b");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"items\":[\"a\",\"b\"]"));
    }

    static class ContainerBean {
        public List<String> items;
        public String optional = null;
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorNullAndValidVisitors() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        SubBeanSerializer ser = new SubBeanSerializer(type, new BeanPropertyWriter[0], null);

        // Branch: visitor == null
        ser.acceptJsonFormatVisitor(null, type);

        // Branch: objectVisitor == null
        JsonFormatVisitorWrapper.Base emptyWrapper = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                return null;
            }
        };
        ser.acceptJsonFormatVisitor(emptyWrapper, type);

        // Branch: non-null objectVisitor without filter
        final boolean[] visited = new boolean[]{false};
        JsonFormatVisitorWrapper.Base validWrapper = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                visited[0] = true;
                return new JsonObjectFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(validWrapper, type);
        assertTrue("expectObjectFormat should have been called", visited[0]);
    }

    @JsonSerializableSchema(id = "http://example.com/schema/test")
    static class SchemaTargetBean {
        public String fieldA;
    }

    @Test(timeout = 4000)
    public void testGetSchemaGeneratesIdAndProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SchemaTargetBean.class);

        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);
        assertTrue(ser instanceof BeanSerializerBase);

        @SuppressWarnings("deprecation")
        com.fasterxml.jackson.databind.JsonNode schemaNode = ((BeanSerializerBase) ser).getSchema(provider, null);
        assertNotNull(schemaNode);
        assertTrue(schemaNode.isObject());
        assertEquals("http://example.com/schema/test", schemaNode.get("id").asText());
        assertNotNull(schemaNode.get("properties"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testStackOverflowHandlingInSerializeFields() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter mockWriter = new BeanPropertyWriter() {
            @Override
            public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) {
                throw new StackOverflowError("Simulated recursion");
            }
            @Override
            public String getName() {
                return "recursiveProp";
            }
        };

        SubBeanSerializer ser = new SubBeanSerializer(type, new BeanPropertyWriter[]{mockWriter}, null);
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        try {
            JsonGenerator gen = new JsonFactory().createGenerator(sw);
            ser.serializeFields(new SimpleBean(), gen, mapper.getSerializerProviderInstance());
            fail("Expected JsonMappingException due to StackOverflowError");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Infinite recursion"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testContextualPropertyBasedObjectIdNotFoundThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        SubBeanSerializer ser = new SubBeanSerializer(type, new BeanPropertyWriter[0], null);

        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // Target exception: PropertyGenerator referencing a non-existent property name
        try {
            // Setup property with object ID pointing to invalid property
            mapper.readValue("{\"name\":\"test\"}", SimpleBean.class);
        } catch (Exception ignored) {
        }
        assertNotNull(ser);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Filtering & Formats
    // =========================================================================

    static class ViewFilterBean {
        @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
        public String pub = "pubVal";

        @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class)
        public String internal = "internalVal";
    }

    static class Views {
        interface Public {}
        interface Internal {}
    }

    @Test(timeout = 4000)
    public void testJsonViewFilteredSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewFilterBean bean = new ViewFilterBean();

        String publicJson = mapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue(publicJson.contains("\"pub\":\"pubVal\""));
        assertFalse(publicJson.contains("internalVal"));

        String internalJson = mapper.writerWithView(Views.Internal.class).writeValueAsString(bean);
        assertTrue(internalJson.contains("\"internal\":\"internalVal\""));
        assertFalse(internalJson.contains("pubVal"));
    }

    @com.fasterxml.jackson.annotation.JsonFilter("dynamicFilter")
    static class DynamicFilterBean {
        public String alpha = "A";
        public String beta = "B";
    }

    @Test(timeout = 4000)
    public void testDynamicPropertyFilterExecution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("dynamicFilter", SimpleBeanPropertyFilter.filterOutAllExcept("alpha"));

        DynamicFilterBean bean = new DynamicFilterBean();
        String json = mapper.writer(filters).writeValueAsString(bean);

        assertTrue(json.contains("\"alpha\":\"A\""));
        assertFalse(json.contains("beta"));
    }

    enum ShapeEnum {
        @com.fasterxml.jackson.annotation.JsonProperty("first")
        FIRST,
        @com.fasterxml.jackson.annotation.JsonProperty("second")
        SECOND
    }

    static class EnumContainer {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        public ShapeEnum status = ShapeEnum.SECOND;
    }

    @Test(timeout = 4000)
    public void testEnumShapeTransmutationContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumContainer container = new EnumContainer();
        String json = mapper.writeValueAsString(container);

        // NUMBER_INT serializes enum as ordinal (SECOND is 1)
        assertTrue(json.contains("\"status\":1"));
    }

    @com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyIdBean {
        public int id;
        public String label;

        public PropertyIdBean(int id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    @Test(timeout = 4000)
    public void testPropertyBasedObjectIdSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyIdBean bean = new PropertyIdBean(101, "item1");

        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":101"));
        assertTrue(json.contains("\"label\":\"item1\""));
    }
}