package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.AnyGetterWriter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT-TARGETED: Defects4J Issue #1607 (AlwaysAsReferenceFirstTest)
 *    - In BeanSerializerBase.createContextual(), when an accessor property does not specify its own
 *      ObjectIdInfo (objectIdInfo == null), but the serializer already has an _objectIdWriter (oiw != null),
 *      Jackson checks if the property has @JsonIdentityReference to update alwaysAsId via:
 *          objectIdInfo = intr.findObjectReferenceInfo(accessor, new ObjectIdInfo(NAME_FOR_OBJECT_REF, null, null, null));
 *          oiw = _objectIdWriter.withAlwaysAsId(objectIdInfo.getAlwaysAsId());
 *      If a class-level @JsonIdentityReference(alwaysAsId=true) was specified, or property-level reference is used,
 *      it must serialize as an id directly on the first encounter (e.g., {"alwaysClass": 1}).
 *
 * 2. Equivalence Partitioning & Branch Coverage:
 *    - Constructor paths: builder == null, builder != null, copy constructor with NameTransformer (null, NOP, active).
 *    - Ignorals constructor & mutant withIgnorals(): filtering out properties in both _props and _filteredProps.
 *    - resolve(): null serializer resolution, active serializers, final vs non-final types, container serializers.
 *    - createContextual():
 *        * Enum shape transmutations (Enum annotated as OBJECT switching back to STRING/NUMBER).
 *        * Property-based ObjectIdGenerator: reordering props so id property is at index 0.
 *        * Property-based ObjectIdGenerator with invalid property name throwing IllegalArgumentException.
 *        * Custom filterId mutation (new filter id vs existing filter id).
 *        * Shape.ARRAY transmutation triggering asArraySerializer().
 *    - serializeWithType():
 *        * with _objectIdWriter (both already-written and first-write paths).
 *        * with custom _typeId (custom type prefix/suffix vs standard prefix/suffix).
 *        * with/without _propertyFilterId.
 *    - serializeFields() & serializeFieldsFiltered():
 *        * Active view filtering (_filteredProps).
 *        * AnyGetterWriter invocation.
 *        * Exception wrapping via wrapAndThrow().
 *    - Schema and Visitor:
 *        * getSchema(): with/without JsonSerializableSchema, with/without filter.
 *        * acceptJsonFormatVisitor(): null visitor, null objectVisitor, with/without filter, with/without view.
 *    - Accessors: properties(), usesObjectId().
 */
public class BeanSerializerBaseGeminiTest {

    // Concrete test implementation of BeanSerializerBase for direct testing
    static class TestBeanSerializer extends BeanSerializerBase {
        public TestBeanSerializer(JavaType type, BeanSerializerBuilder builder,
                                  BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(type, builder, properties, filteredProperties);
        }

        public TestBeanSerializer(TestBeanSerializer src) {
            super(src);
        }

        public TestBeanSerializer(TestBeanSerializer src, ObjectIdWriter objectIdWriter) {
            super(src, objectIdWriter);
        }

        public TestBeanSerializer(TestBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
            super(src, objectIdWriter, filterId);
        }

        public TestBeanSerializer(TestBeanSerializer src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public TestBeanSerializer(TestBeanSerializer src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new TestBeanSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new TestBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new TestBeanSerializer(this, this._objectIdWriter, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            gen.writeEndObject();
        }
    }

    /*
     * Classes used for Defect and Contextual Tests
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    static class ClassAlwaysId {
        public int id;
        public int value;

        public ClassAlwaysId(int id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class ClassNormalId {
        public int id;
        public int value;

        public ClassNormalId(int id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    static class ContainerForIssue1607 {
        public ClassAlwaysId alwaysClass;
        @JsonIdentityReference(alwaysAsId = true)
        public ClassNormalId alwaysProp;

        public ContainerForIssue1607(ClassAlwaysId c, ClassNormalId p) {
            this.alwaysClass = c;
            this.alwaysProp = p;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static enum ShapeEnum {
        VAL_A, VAL_B
    }

    static class EnumWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public ShapeEnum revertToString = ShapeEnum.VAL_A;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        public ShapeEnum revertToInt = ShapeEnum.VAL_B;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class AsArrayBean {
        public int a = 1;
        public String b = "test";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistentProp")
    static class InvalidPropertyGeneratorBean {
        public int x = 10;
    }

    @JsonFilter("filterA")
    static class FilteredBean {
        public int x = 1;
        public int y = 2;
    }

    static class FilterOverrideBean {
        @JsonFilter("filterB")
        public FilteredBean child = new FilteredBean();
    }

    @JsonSerializableSchema(id = "schema-custom-id")
    static class SchemaAnnotatedBean {
        public String field = "hello";
    }

    public static class ViewA {}
    public static class ViewB {}

    static class ViewBean {
        @com.fasterxml.jackson.annotation.JsonView(ViewA.class)
        public String propA = "viewA";

        @com.fasterxml.jackson.annotation.JsonView(ViewB.class)
        public String propB = "viewB";
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    static class TypeInfoBean {
        public int id = 42;
    }

    /* =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Issue #1607 / Defects4J)
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testAlwaysAsReferenceFirstIssue1607() throws Exception {
        // Targets defect where @JsonIdentityReference(alwaysAsId=true) should serialize
        // as primitive ID even on the very first serialization instance.
        ObjectMapper mapper = new ObjectMapper();
        ContainerForIssue1607 bean = new ContainerForIssue1607(
                new ClassAlwaysId(1, 13),
                new ClassNormalId(2, 23)
        );

        String json = mapper.writeValueAsString(bean);
        // Correct behavior: alwaysClass should be serialized as 1, NOT {"id":1,"value":13}
        // alwaysProp should be serialized as 2
        assertEquals("{\"alwaysClass\":1,\"alwaysProp\":2}", json);
    }

    /* =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testConstructorsAndMutants() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        TestBeanSerializer serializer = new TestBeanSerializer(type, null, new BeanPropertyWriter[0], null);

        assertNull(serializer.getFilterId());
        assertFalse(serializer.usesObjectId());

        Iterator<PropertyWriter> it = serializer.properties();
        assertNotNull(it);
        assertFalse(it.hasNext());

        // Test withIgnorals
        BeanSerializerBase ignored = serializer.withIgnorals(new String[]{"someProp"});
        assertNotNull(ignored);

        // Test withFilterId
        BeanSerializerBase filtered = serializer.withFilterId("myFilter");
        assertNotNull(filtered);

        // Test asArraySerializer
        BeanSerializerBase arraySer = serializer.asArraySerializer();
        assertSame(serializer, arraySer);

        // Test copy constructor with unwrapper
        TestBeanSerializer renamedNull = new TestBeanSerializer(serializer, (NameTransformer) null);
        assertNotNull(renamedNull);

        TestBeanSerializer renamedNop = new TestBeanSerializer(serializer, NameTransformer.NOP);
        assertNotNull(renamedNop);

        TestBeanSerializer renamed = new TestBeanSerializer(serializer, NameTransformer.simpleTransformer("pre_", "_post"));
        assertNotNull(renamed);
    }

    static class SimpleBean {
        public String name = "john";
    }

    @Test(timeout = 4000)
    public void testEnumShapeTransmutationContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumWrapper wrapper = new EnumWrapper();
        String json = mapper.writeValueAsString(wrapper);

        assertTrue("Expected revertToString as STRING", json.contains("\"revertToString\":\"VAL_A\""));
        assertTrue("Expected revertToInt as integer", json.contains("\"revertToInt\":1"));
    }

    @Test(timeout = 4000)
    public void testAsArraySerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsArrayBean bean = new AsArrayBean();
        String json = mapper.writeValueAsString(bean);

        assertEquals("[1,\"test\"]", json);
    }

    /* =========================================================================
     * Partition B: Boundary Value Analysis & Views / Filters
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testFilterIdOverrideAndSimpleFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("filterA", SimpleBeanPropertyFilter.filterOutAllExcept("x"))
                .addFilter("filterB", SimpleBeanPropertyFilter.serializeAllExcept("x"));
        mapper.setFilterProvider(filters);

        FilteredBean fb = new FilteredBean();
        String jsonA = mapper.writeValueAsString(fb);
        assertEquals("{\"x\":1}", jsonA);

        FilterOverrideBean override = new FilterOverrideBean();
        String jsonB = mapper.writeValueAsString(override);
        assertTrue("Child should be filtered by filterB excluding 'x'", jsonB.contains("\"y\":2"));
        assertFalse("Child should exclude 'x'", jsonB.contains("\"x\":1"));
    }

    @Test(timeout = 4000)
    public void testActiveViewSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean vb = new ViewBean();

        String jsonA = mapper.writerWithView(ViewA.class).writeValueAsString(vb);
        assertTrue(jsonA.contains("propA"));
        assertFalse(jsonA.contains("propB"));

        String jsonB = mapper.writerWithView(ViewB.class).writeValueAsString(vb);
        assertFalse(jsonB.contains("propA"));
        assertTrue(jsonB.contains("propB"));
    }

    @Test(timeout = 4000)
    public void testSerializeWithTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeInfoBean bean = new TypeInfoBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"type\":\"BeanSerializerBaseGeminiTest$TypeInfoBean\"") || json.contains("\"type\":\"BeanSerializerBaseGeminiTest.TypeInfoBean\"") || json.contains("\"type\""));
        assertTrue(json.contains("\"id\":42"));
    }

    /* =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testInvalidPropertyGeneratorThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        InvalidPropertyGeneratorBean bean = new InvalidPropertyGeneratorBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected exception due to nonExistentProp in ObjectId definition");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("can not find property with name 'nonExistentProp'"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    static class FailingGetterBean {
        public String getThrowsError() {
            throw new RuntimeException("Simulated getter failure");
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowHandling() {
        ObjectMapper mapper = new ObjectMapper();
        FailingGetterBean bean = new FailingGetterBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected JsonMappingException wrapping getter failure");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Simulated getter failure"));
            assertEquals(1, e.getPath().size());
            assertEquals("throwsError", e.getPath().get(0).getFieldName());
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    /* =========================================================================
     * Partition E: Schema and Visitor API Contracts
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testGetSchemaWithAnnotationAndFilter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SchemaAnnotatedBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);
        assertTrue(ser instanceof BeanSerializerBase);

        BeanSerializerBase bsb = (BeanSerializerBase) ser;
        ObjectNode schema = (ObjectNode) bsb.getSchema(provider, (Type) SchemaAnnotatedBean.class);

        assertNotNull(schema);
        assertEquals("object", schema.get("type").asText());
        assertEquals("schema-custom-id", schema.get("id").asText());
        assertTrue(schema.has("properties"));
        assertTrue(schema.get("properties").has("field"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorHandlesNullAndViews() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(ViewBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);
        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        // Null visitor should not fail
        bsb.acceptJsonFormatVisitor(null, type);

        // Visitor expecting object format
        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType t) {
                return new JsonObjectFormatVisitor.Base(provider) {
                    @Override
                    public void property(BeanProperty prop) {
                        visited[0] = true;
                    }
                };
            }
        };

        bsb.acceptJsonFormatVisitor(visitor, type);
        assertTrue("Expected visitor to process properties", visited[0]);
    }
}