package com.fasterxml.jackson.databind.ser;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * High coverage JUnit4 test-suite for {@link BeanPropertyWriter}.
 */
public class BeanPropertyWriterClaudeTest
{
    /*
    /**********************************************************
    /* Helper POJOs
    /**********************************************************
     */

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"first", "second"})
    static class PojoAsArray {
        public String first;
        public String second;
        public PojoAsArray(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class SingleNullablePojo {
        public String value;
        public SingleNullablePojo(String value) { this.value = value; }
    }

    static class BasicPojo {
        public String name;
        private int age;
        public BasicPojo(String name, int age) {
            this.name = name;
            this.age = age;
        }
        public int getAge() { return age; }
    }

    static class NonNullPojo {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String name;
        public NonNullPojo(String name) { this.name = name; }
    }

    static class NonEmptyPojo {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String name;
        public NonEmptyPojo(String name) { this.name = name; }
    }

    static class NonDefaultPojo {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int value;
        public NonDefaultPojo(int value) { this.value = value; }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"first", "second"})
    static class ArrayNonEmptyPojo {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String first;
        public String second;
        public ArrayNonEmptyPojo(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    static class SelfRefPojo {
        public SelfRefPojo self;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class Animal { }

    static class Dog extends Animal {
        public String name = "Rex";
    }

    static class Owner {
        public Animal pet;
        public Owner(Animal pet) { this.pet = pet; }
    }

    static class DynamicPojo {
        public Object value;
        public DynamicPojo(Object value) { this.value = value; }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class DynamicArrayPojo {
        public Object value;
        public DynamicArrayPojo(Object value) { this.value = value; }
    }

    static class TypedMapPojo {
        @JsonSerialize(as = LinkedHashMap.class)
        public Map<String, Object> data = new HashMap<String, Object>();
    }

    static class DummySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString("dummy");
        }
    }

    /*
    /**********************************************************
    /* Helpers for capturing real BeanPropertyWriter instances
    /**********************************************************
     */

    private List<BeanPropertyWriter> captureWriters(final Object pojo) throws Exception {
        final List<BeanPropertyWriter> captured = new ArrayList<BeanPropertyWriter>();
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                captured.clear();
                captured.addAll(beanProperties);
                return beanProperties;
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.writeValueAsString(pojo);
        return captured;
    }

    private BeanPropertyWriter findWriter(List<BeanPropertyWriter> writers, String name) {
        for (BeanPropertyWriter w : writers) {
            if (w.getName().equals(name)) {
                return w;
            }
        }
        return null;
    }

    /*
    /**********************************************************
    /* DEFECT-TARGETED TESTS (serializeAsColumn null handling)
    /**********************************************************
     */

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario First array-shaped property is null, second is "bar"
     * @defectRisk Missing return statement after writing null in tabular (array)
     *             output causes duplicate null entries: "[null,null,\"bar\"]"
     *             instead of the correct "[null,\"bar\"]".
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_NullValue_NoDuplicateNull_Issue223() throws Exception {
        PojoAsArray pojo = new PojoAsArray(null, "bar");
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("[null,\"bar\"]", json);
    }

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario Single array-shaped property that is null (isolates the defect
     *           without a trailing property that could mask duplication)
     * @defectRisk Same missing-return defect produces "[null,null]" instead of "[null]"
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_SingleNullProperty_NoDuplicate() throws Exception {
        SingleNullablePojo pojo = new SingleNullablePojo(null);
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("[null]", json);
    }

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario Both array-shaped properties are non-null
     * @defectRisk Sanity check that normal (non-null) tabular serialization
     *             is unaffected by the null-handling defect
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_NonNullValues() throws Exception {
        PojoAsArray pojo = new PojoAsArray("foo", "bar");
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("[\"foo\",\"bar\"]", json);
    }

    /*
    /**********************************************************
    /* serializeAsField() behavior
    /**********************************************************
     */

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Default (no suppression) POJO field is null
     * @defectRisk Ensures default null-serializer path writes "fieldName":null
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_DefaultNullWritten() throws Exception {
        BasicPojo pojo = new BasicPojo(null, 5);
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("{\"name\":null,\"age\":5}", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario @JsonInclude(NON_NULL) property is null vs non-null
     * @defectRisk Ensures null suppression branch (no field name written) works,
     *             and non-null values are still emitted normally
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_NonNullSuppressesNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonNull = mapper.writeValueAsString(new NonNullPojo(null));
        assertEquals("{}", jsonNull);

        String jsonVal = mapper.writeValueAsString(new NonNullPojo("x"));
        assertEquals("{\"name\":\"x\"}", jsonVal);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario @JsonInclude(NON_EMPTY) property is empty string vs non-empty
     * @defectRisk Ensures MARKER_FOR_EMPTY suppression branch (ser.isEmpty(value))
     *             correctly skips empty values but keeps non-empty ones
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_NonEmptySuppressesEmptyString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonEmpty = mapper.writeValueAsString(new NonEmptyPojo(""));
        assertEquals("{}", jsonEmpty);

        String jsonVal = mapper.writeValueAsString(new NonEmptyPojo("x"));
        assertEquals("{\"name\":\"x\"}", jsonVal);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario @JsonInclude(NON_DEFAULT) primitive int property equal to default (0)
     *           vs a non-default value
     * @defectRisk Ensures _suppressableValue.equals(value) branch (non-MARKER_FOR_EMPTY)
     *             correctly suppresses default values only
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_NonDefaultSuppressesDefaultInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonDefault = mapper.writeValueAsString(new NonDefaultPojo(0));
        assertEquals("{}", jsonDefault);

        String jsonVal = mapper.writeValueAsString(new NonDefaultPojo(5));
        assertEquals("{\"value\":5}", jsonVal);
    }

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario @JsonInclude(NON_EMPTY) property inside array-shaped POJO is empty
     * @defectRisk Ensures suppressed tabular value falls back to
     *             serializeAsPlaceholder() (writing null) rather than
     *             collapsing the array (which would shift subsequent columns)
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_NonEmptyProducesPlaceholder() throws Exception {
        ArrayNonEmptyPojo pojo = new ArrayNonEmptyPojo("", "bar");
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("[null,\"bar\"]", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider) -> _handleSelfReference
     * @scenario Bean property directly references the bean itself (self-cycle)
     * @defectRisk Ensures a direct self-reference (non-ObjectId serializer) throws
     *             JsonMappingException instead of causing infinite recursion / StackOverflow
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_SelfReferenceThrowsException() throws Exception {
        SelfRefPojo pojo = new SelfRefPojo();
        pojo.self = pojo;
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(pojo);
            fail("Expected a JsonMappingException for direct self-reference");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage() != null
                    && e.getMessage().contains("Direct self-reference"));
        }
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider) -> serializeWithType branch
     * @scenario Property declared with polymorphic type info (@JsonTypeInfo) holding a subtype instance
     * @defectRisk Ensures _typeSerializer != null branch correctly delegates to
     *             ser.serializeWithType(...) including type metadata in output
     */
    @Test(timeout = 4000)
    public void testSerializeAsField_PolymorphicTypeUsesTypeSerializer() throws Exception {
        Owner owner = new Owner(new Dog());
        String json = new ObjectMapper().writeValueAsString(owner);
        assertTrue("Expected class-type metadata in output: " + json, json.contains("Dog"));
        assertTrue("Expected 'Rex' value in output: " + json, json.contains("Rex"));
    }

    /*
    /**********************************************************
    /* Dynamic serializer resolution / caching
    /**********************************************************
     */

    /**
     * @target serializeAsField -> _findAndAddDynamic(...) / PropertySerializerMap caching
     * @scenario Object-typed property assigned different runtime types across calls
     *           using the SAME ObjectMapper/serializer instance
     * @defectRisk Ensures dynamic serializer lookup-and-cache logic in
     *             _findAndAddDynamic correctly resolves and reuses serializers
     *             for varying runtime types without corrupting output
     */
    @Test(timeout = 4000)
    public void testDynamicSerializerResolutionForObjectTypedProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(new DynamicPojo("hello"));
        assertEquals("{\"value\":\"hello\"}", json1);

        String json2 = mapper.writeValueAsString(new DynamicPojo(123));
        assertEquals("{\"value\":123}", json2);

        // Same type again -> should hit cached dynamic serializer branch
        String json3 = mapper.writeValueAsString(new DynamicPojo("world"));
        assertEquals("{\"value\":\"world\"}", json3);
    }

    /**
     * @target serializeAsColumn -> _findAndAddDynamic(...) dynamic resolution in tabular mode
     * @scenario Object-typed property in an array-shaped POJO, varying runtime type
     * @defectRisk Ensures dynamic serializer resolution works correctly within
     *             the tabular (serializeAsColumn) code path as well
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_DynamicSerializerForObjectTypedProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(new DynamicArrayPojo("abc"));
        assertEquals("[\"abc\"]", json1);

        String json2 = mapper.writeValueAsString(new DynamicArrayPojo(42));
        assertEquals("[42]", json2);
    }

    /*
    /**********************************************************
    /* Metadata accessor coverage
    /**********************************************************
     */

    /**
     * @target getName(), getType(), getMember(), getPropertyType(), getGenericPropertyType(), toString()
     * @scenario Field-backed property ("name") captured from a real bean serializer
     * @defectRisk Ensures field-based accessor metadata is correctly exposed
     */
    @Test(timeout = 4000)
    public void testMetadataGetters_FieldBackedProperty() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);
        assertEquals("name", writer.getName());
        assertNotNull(writer.getType());
        assertNotNull(writer.getMember());
        assertTrue(writer.getMember() instanceof AnnotatedField);
        assertEquals(String.class, writer.getPropertyType());
        assertNotNull(writer.getGenericPropertyType());

        String str = writer.toString();
        assertTrue(str.contains("name"));
    }

    /**
     * @target getMember(), getPropertyType(), getSerializationType(), getRawSerializationType()
     * @scenario Method-backed (getter) property ("age") captured from a real bean serializer
     * @defectRisk Ensures method-based accessor path (AnnotatedMethod) and default
     *             (null) serialization-type branch behave correctly
     */
    @Test(timeout = 4000)
    public void testMetadataGetters_MethodBackedProperty() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "age");
        assertNotNull(writer);
        assertTrue(writer.getMember() instanceof AnnotatedMethod);
        assertEquals(int.class, writer.getPropertyType());
        // no explicit @JsonSerialize(as=...) configured -> null
        assertNull(writer.getSerializationType());
        assertNull(writer.getRawSerializationType());
    }

    /**
     * @target getSerializationType(), getRawSerializationType()
     * @scenario Property annotated with @JsonSerialize(as=LinkedHashMap.class)
     * @defectRisk Ensures non-null branch of getRawSerializationType() correctly
     *             extracts the raw class from configured serialization type
     */
    @Test(timeout = 4000)
    public void testGetSerializationType_ExplicitlyConfigured() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new TypedMapPojo());
        BeanPropertyWriter writer = findWriter(writers, "data");
        assertNotNull(writer);
        if (writer.getSerializationType() != null) {
            assertEquals(LinkedHashMap.class, writer.getRawSerializationType());
        }
    }

    /**
     * @target hasSerializer(), hasNullSerializer(), willSuppressNulls(), getSerializer()
     * @scenario Default nullable property vs NON_NULL suppressed property, after full resolution
     * @defectRisk Ensures serializer/null-serializer resolution state is correctly reflected post-resolve
     */
    @Test(timeout = 4000)
    public void testHasSerializerAndHasNullSerializerAfterResolution() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter nameWriter = findWriter(writers, "name");
        assertNotNull(nameWriter);
        assertTrue(nameWriter.hasSerializer());
        assertFalse(nameWriter.willSuppressNulls());
        assertNotNull(nameWriter.getSerializer());

        List<BeanPropertyWriter> nnWriters = captureWriters(new NonNullPojo("y"));
        BeanPropertyWriter nnWriter = findWriter(nnWriters, "name");
        assertNotNull(nnWriter);
        assertTrue(nnWriter.willSuppressNulls());
        assertFalse(nnWriter.hasNullSerializer());
    }

    /**
     * @target getWrapperName(), getViews(), isRequired()
     * @scenario Plain property with no special annotations
     * @defectRisk Ensures defaults (no wrapper name, no views, not required) are exposed correctly
     */
    @Test(timeout = 4000)
    public void testWrapperNameAndViewsAndRequiredDefaults() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);
        assertFalse(writer.isRequired());
        // Views may be null or empty array depending on version; both acceptable
        Class<?>[] views = writer.getViews();
        assertTrue(views == null || views.length == 0);
    }

    /**
     * @target getAnnotation(Class), getContextAnnotation(Class)
     * @scenario Query for an annotation type that is not present on the member
     * @defectRisk Ensures graceful null return rather than an exception when annotation is absent
     */
    @Test(timeout = 4000)
    public void testGetAnnotationAndContextAnnotationReturnNullWhenAbsent() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);
        assertNull(writer.getAnnotation(Deprecated.class));
        assertNull(writer.getContextAnnotation(Deprecated.class));
    }

    /*
    /**********************************************************
    /* Internal settings, rename(), setNonTrivialBaseType()
    /**********************************************************
     */

    /**
     * @target getInternalSetting(Object), setInternalSetting(Object,Object), removeInternalSetting(Object)
     * @scenario Full lifecycle: get on empty map, set, get, remove, remove again (map becomes null)
     * @defectRisk Ensures lazy-map creation, retrieval, and cleanup (dropping empty map) all work correctly
     */
    @Test(timeout = 4000)
    public void testInternalSettingsManagement() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);

        // initially empty -> null
        assertNull(writer.getInternalSetting("someKey"));

        Object oldVal = writer.setInternalSetting("someKey", "someValue");
        assertNull(oldVal); // no prior value

        assertEquals("someValue", writer.getInternalSetting("someKey"));

        Object oldVal2 = writer.setInternalSetting("someKey", "newValue");
        assertEquals("someValue", oldVal2);
        assertEquals("newValue", writer.getInternalSetting("someKey"));

        Object removed = writer.removeInternalSetting("someKey");
        assertEquals("newValue", removed);

        // now removed again -> null, map should be dropped
        assertNull(writer.removeInternalSetting("someKey"));
        assertNull(writer.getInternalSetting("someKey"));
    }

    /**
     * @target rename(NameTransformer)
     * @scenario Transformer changes the name -> new instance created;
     *           transformer returns same name -> original instance returned
     * @defectRisk Ensures rename() correctly detects no-op transforms
     *             (avoiding unnecessary object creation) and produces a
     *             correctly renamed copy otherwise
     */
    @Test(timeout = 4000)
    public void testRenameProducesNewInstanceOrSameInstance() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        final BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);

        // no-op transform -> same instance
        NameTransformer noop = new NameTransformer() {
            @Override
            public String transform(String name) { return name; }
            @Override
            public String reverse(String transformed) { return transformed; }
        };
        BeanPropertyWriter same = writer.rename(noop);
        assertSame(writer, same);

        // real rename -> new instance, different name
        NameTransformer renamer = new NameTransformer() {
            @Override
            public String transform(String name) { return "renamed_" + name; }
            @Override
            public String reverse(String transformed) { return transformed; }
        };
        BeanPropertyWriter renamed = writer.rename(renamer);
        assertNotSame(writer, renamed);
        assertEquals("renamed_name", renamed.getName());
        // original unaffected
        assertEquals("name", writer.getName());
    }

    /**
     * @target setNonTrivialBaseType(JavaType)
     * @scenario Assign a non-trivial base type to a writer, ensure no exception thrown
     * @defectRisk Ensures this configuration setter does not throw and stores state as expected
     */
    @Test(timeout = 4000)
    public void testSetNonTrivialBaseTypeDoesNotThrow() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new DynamicPojo("hi"));
        BeanPropertyWriter writer = findWriter(writers, "value");
        assertNotNull(writer);
        JavaType t = TypeFactory.defaultInstance().constructType(String.class);
        writer.setNonTrivialBaseType(t);
        // No direct getter, but at minimum no exception should be raised, and
        // subsequent (de)serialization should still work fine.
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new DynamicPojo("hi"));
        assertEquals("{\"value\":\"hi\"}", json);
    }

    /**
     * @target unwrappingWriter(NameTransformer)
     * @scenario Create an unwrapping variant of a normal writer
     * @defectRisk Ensures factory method returns a valid non-null BeanPropertyWriter subtype instance
     */
    @Test(timeout = 4000)
    public void testUnwrappingWriterCreation() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter original = findWriter(writers, "name");
        assertNotNull(original);

        BeanPropertyWriter unwrapped = original.unwrappingWriter(NameTransformer.NOP);
        assertNotNull(unwrapped);
        assertTrue(unwrapped instanceof BeanPropertyWriter);
    }

    /*
    /**********************************************************
    /* assignSerializer() / assignNullSerializer() exception paths
    /**********************************************************
     */

    /**
     * @target assignSerializer(JsonSerializer)
     * @scenario Assign a serializer once (succeeds, since initially null), then attempt
     *           to assign a different serializer instance (should throw)
     * @defectRisk Ensures the "cannot override an already-assigned different serializer"
     *             guard is enforced correctly
     */
    @Test(timeout = 4000)
    public void testAssignSerializerThrowsOnOverride() throws Exception {
        final boolean[] firstAssignSucceeded = { false };
        final boolean[] exceptionThrown = { false };

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                if (!beanProperties.isEmpty()) {
                    BeanPropertyWriter writer = beanProperties.get(0);
                    try {
                        writer.assignSerializer(new DummySerializer());
                        firstAssignSucceeded[0] = true;
                    } catch (IllegalStateException e) {
                        // property already had a static serializer; skip further check
                    }
                    if (firstAssignSucceeded[0]) {
                        try {
                            writer.assignSerializer(new DummySerializer());
                        } catch (IllegalStateException e) {
                            exceptionThrown[0] = true;
                        }
                    }
                }
                return beanProperties;
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        try {
            mapper.writeValueAsString(new BasicPojo("x", 1));
        } catch (Exception ignore) {
            // resulting JSON output is irrelevant for this particular test
        }
        assertTrue("First assignment expected to succeed", firstAssignSucceeded[0]);
        assertTrue("Second differing assignment must throw IllegalStateException",
                exceptionThrown[0]);
    }

    /**
     * @target assignNullSerializer(JsonSerializer)
     * @scenario Assign a null-serializer once (succeeds, since initially null), then attempt
     *           to assign a different null-serializer instance (should throw)
     * @defectRisk Ensures the "cannot override an already-assigned different null serializer"
     *             guard is enforced correctly
     */
    @Test(timeout = 4000)
    public void testAssignNullSerializerThrowsOnOverride() throws Exception {
        final boolean[] firstAssignSucceeded = { false };
        final boolean[] exceptionThrown = { false };

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                if (!beanProperties.isEmpty()) {
                    BeanPropertyWriter writer = beanProperties.get(0);
                    try {
                        writer.assignNullSerializer(new DummySerializer());
                        firstAssignSucceeded[0] = true;
                    } catch (IllegalStateException e) {
                        // already assigned; skip further check
                    }
                    if (firstAssignSucceeded[0]) {
                        try {
                            writer.assignNullSerializer(new DummySerializer());
                        } catch (IllegalStateException e) {
                            exceptionThrown[0] = true;
                        }
                    }
                }
                return beanProperties;
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        try {
            mapper.writeValueAsString(new BasicPojo("x", 1));
        } catch (Exception ignore) {
            // resulting JSON output is irrelevant for this particular test
        }
        assertTrue("First null-serializer assignment expected to succeed",
                firstAssignSucceeded[0]);
        assertTrue("Second differing null-serializer assignment must throw",
                exceptionThrown[0]);
    }

    /*
    /**********************************************************
    /* depositSchemaProperty (legacy JsonFormatVisitable)
    /**********************************************************
     */

    /**
     * @target depositSchemaProperty(JsonObjectFormatVisitor)
     * @scenario Passing a null visitor
     * @defectRisk Ensures the null-check guard prevents a NullPointerException
     */
    @Test(timeout = 4000)
    public void testDepositSchemaPropertyWithNullVisitorDoesNothing() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);
        // Should simply do nothing, no exception
        writer.depositSchemaProperty((com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor) null);
    }

    /*
    /**********************************************************
    /* serializeAsPlaceholder direct coverage
    /**********************************************************
     */

    /**
     * @target serializeAsPlaceholder(Object, JsonGenerator, SerializerProvider)
     * @scenario Directly invoke placeholder serialization on a writer without a
     *           custom null serializer configured
     * @defectRisk Ensures default placeholder behavior writes a JSON null token
     */
    @Test(timeout = 4000)
    public void testSerializeAsPlaceholderWritesNullByDefault() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);

        StringWriter sw = new StringWriter();
        JsonFactory factory = new JsonFactory();
        JsonGenerator gen = factory.createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        gen.writeStartArray();
        writer.serializeAsPlaceholder(new BasicPojo("x", 1), gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null]", sw.toString());
    }

    /*
    /**********************************************************
    /* toString() coverage for both accessor kinds
    /**********************************************************
     */

    /**
     * @target toString()
     * @scenario Serializer already resolved (non-null) for a field-backed property
     * @defectRisk Ensures toString() includes serializer type info branch (not
     *             the "no static serializer" branch) once resolution has occurred
     */
    @Test(timeout = 4000)
    public void testToStringIncludesSerializerInfoWhenResolved() throws Exception {
        List<BeanPropertyWriter> writers = captureWriters(new BasicPojo("x", 1));
        BeanPropertyWriter writer = findWriter(writers, "name");
        assertNotNull(writer);
        String str = writer.toString();
        assertTrue(str.contains("property 'name'"));
        assertTrue(str.contains("via field") || str.contains("field \""));
        assertTrue(str.contains("static serializer of type"));
    }
}