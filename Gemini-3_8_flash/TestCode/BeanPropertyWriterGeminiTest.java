package com.fasterxml.jackson.databind.ser;

import java.io.*;
import java.util.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.ser.BeanPropertyWriter
 *
 * Decision / Branch Matrix:
 * 1. Constructor:
 *    - member instanceof AnnotatedField -> _accessorMethod = null, _field assigned
 *    - member instanceof AnnotatedMethod -> _accessorMethod assigned, _field = null
 *    - member not AnnotatedField or AnnotatedMethod -> IllegalArgumentException thrown
 * 2. assignSerializer & assignNullSerializer:
 *    - Assigning when null or same instance -> success
 *    - Assigning different instance when non-null -> IllegalStateException thrown
 * 3. rename():
 *    - transformed name equals current name -> returns this (no reallocation)
 *    - transformed name differs -> returns new BeanPropertyWriter instance with new SerializedString
 * 4. Internal settings map:
 *    - get/set/remove with null map initially
 *    - remove setting reducing map size to 0 -> nulls out _internalSettings to reclaim memory
 * 5. depositSchemaProperty (JsonObjectFormatVisitor & ObjectNode):
 *    - null visitor vs non-null visitor
 *    - isRequired() true (calls objectVisitor.property) vs false (calls objectVisitor.optionalProperty)
 *    - schema node generation with SchemaAware serializer vs default schema fallback
 * 6. serializeAsField():
 *    - value == null with _nullSerializer != null vs _nullSerializer == null (suppressed)
 *    - _serializer statically known vs dynamic lookup (_dynamicSerializers)
 *    - _suppressableValue check: MARKER_FOR_EMPTY (ser.isEmpty()) vs object equality
 *    - Direct self-reference cycle detection: value == bean -> _handleSelfReference throws JsonMappingException
 * 7. serializeAsColumn() [JacksonDatabind-1 / Issue 223 Zone]:
 *    - POJO serialized as JSON Array (@JsonFormat(shape = Shape.ARRAY))
 *    - value == null: MUST write null and immediately return.
 *      Defect JacksonDatabind-1 omits 'return;', falling through and executing secondary serialization
 *      which duplicates the null or crashes with NullPointerException.
 *    - value suppressed: calls serializeAsPlaceholder()
 * 8. Serialization with TypeSerializer:
 *    - _typeSerializer != null -> calls ser.serializeWithType()
 */
public class BeanPropertyWriterGeminiTest {

    // =========================================================================
    // Supporting Dummy Types & Custom Annotations
    // =========================================================================

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomMarker {
        String value() default "test";
    }

    static class SampleBean {
        @CustomMarker("fieldValue")
        public String textField = "sample";

        private int intVal = 42;

        public int getIntVal() {
            return intVal;
        }

        public void setIntVal(int v) {
            this.intVal = v;
        }
    }

    static class ViewFilteredBean {
        public interface PublicView {}
        public interface PrivateView {}

        @JsonView(PublicView.class)
        public String pub = "public";

        @JsonView(PrivateView.class)
        public String priv = "private";
    }

    static class EmptySuppressedBean {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> items = new ArrayList<String>();

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int defaultInt = 0;
    }

    static class SelfReferencingBean {
        public SelfReferencingBean self;

        public SelfReferencingBean() {
            this.self = this;
        }
    }

    static class CustomNullSerializer extends StdSerializer<Object> {
        public CustomNullSerializer() {
            super(Object.class);
        }

        @Override
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString("CUSTOM_NULL");
        }
    }

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
    @JsonPropertyOrder({"id", "tag"})
    static class PojoAsArrayWithSuppression {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> id;
        public String tag;

        public PojoAsArrayWithSuppression(List<String> id, String tag) {
            this.id = id;
            this.tag = tag;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = PolymorphicChild.class, name = "child")})
    static class PolymorphicBase {
    }

    static class PolymorphicChild extends PolymorphicBase {
        public String name = "childVal";
    }

    static class PolymorphicHolder {
        public PolymorphicBase poly = new PolymorphicChild();
    }

    // =========================================================================
    // Helper to retrieve BeanPropertyWriter from BeanSerializerBase
    // =========================================================================

    private BeanPropertyWriter findPropertyWriter(ObjectMapper mapper, Class<?> cls, String propName) {
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        try {
            JsonSerializer<Object> ser = prov.findValueSerializer(cls, null);
            if (ser instanceof BeanSerializerBase) {
                BeanSerializerBase bser = (BeanSerializerBase) ser;
                Iterator<BeanPropertyWriter> it = bser.properties();
                while (it.hasNext()) {
                    BeanPropertyWriter bpw = it.next();
                    if (bpw.getName().equals(propName)) {
                        return bpw;
                    }
                }
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalArgumentException("Property '" + propName + "' not found on " + cls);
    }

    // =========================================================================
    // Partition A: Property Introspection & Metadata
    // =========================================================================

    @Test(timeout = 4000)
    public void testFieldAndMethodAccessorProperties() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter fieldWriter = findPropertyWriter(mapper, SampleBean.class, "textField");
        BeanPropertyWriter methodWriter = findPropertyWriter(mapper, SampleBean.class, "intVal");

        assertNotNull(fieldWriter);
        assertNotNull(methodWriter);

        // Verification of field-backed writer
        assertEquals("textField", fieldWriter.getName());
        assertEquals("textField", fieldWriter.getSerializedName().getValue());
        assertEquals(String.class, fieldWriter.getPropertyType());
        assertEquals(String.class, fieldWriter.getGenericPropertyType());
        assertNull(fieldWriter.getWrapperName());
        assertFalse(fieldWriter.isRequired());
        assertTrue(fieldWriter.getMember() instanceof AnnotatedField);
        assertNotNull(fieldWriter.getAnnotation(CustomMarker.class));
        assertEquals("fieldValue", fieldWriter.getAnnotation(CustomMarker.class).value());

        // Verification of method-backed writer
        assertEquals("intVal", methodWriter.getName());
        assertEquals(Integer.TYPE, methodWriter.getPropertyType());
        assertEquals(Integer.TYPE, methodWriter.getGenericPropertyType());
        assertTrue(methodWriter.getMember() instanceof AnnotatedMethod);
        assertNull(methodWriter.getAnnotation(CustomMarker.class));

        // toString validation
        String fieldDesc = fieldWriter.toString();
        assertTrue(fieldDesc.contains("property 'textField'"));
        assertTrue(fieldDesc.contains("field \""));

        String methodDesc = methodWriter.toString();
        assertTrue(methodDesc.contains("property 'intVal'"));
        assertTrue(methodDesc.contains("via method "));
    }

    @Test(timeout = 4000)
    public void testRenameAndUnwrapping() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        // Identity transform returns the same instance
        NameTransformer identity = NameTransformer.NOP;
        assertSame(bpw, bpw.rename(identity));

        // Prefix transform returns a new instance
        NameTransformer prefixer = NameTransformer.simpleTransformer("pre_", "");
        BeanPropertyWriter renamed = bpw.rename(prefixer);
        assertNotSame(bpw, renamed);
        assertEquals("pre_textField", renamed.getName());
        assertEquals("pre_textField", renamed.getSerializedName().getValue());

        // Unwrapping writer creation
        BeanPropertyWriter unwrapping = bpw.unwrappingWriter(prefixer);
        assertNotNull(unwrapping);
    }

    @Test(timeout = 4000)
    public void testInternalSettingsLifecycle() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        assertNull(bpw.getInternalSetting("key1"));
        assertNull(bpw.removeInternalSetting("nonExistent"));

        bpw.setInternalSetting("key1", "val1");
        assertEquals("val1", bpw.getInternalSetting("key1"));

        bpw.setInternalSetting("key2", "val2");
        assertEquals("val2", bpw.getInternalSetting("key2"));

        Object removed1 = bpw.removeInternalSetting("key1");
        assertEquals("val1", removed1);
        assertNull(bpw.getInternalSetting("key1"));

        Object removed2 = bpw.removeInternalSetting("key2");
        assertEquals("val2", removed2);
        assertNull(bpw.getInternalSetting("key2"));

        // Removing all settings cleans up internal Map reference
        assertNull(bpw.removeInternalSetting("key2"));
    }

    @Test(timeout = 4000)
    public void testSerializerAssignmentGuards() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        JsonSerializer<Object> ser1 = new StdSerializer<Object>(Object.class) {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider prov) {}
        };
        JsonSerializer<Object> ser2 = new StdSerializer<Object>(Object.class) {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider prov) {}
        };

        // Static serializer reassignment to the exact same instance is allowed
        bpw.assignSerializer(ser1);
        bpw.assignSerializer(ser1);
        assertTrue(bpw.hasSerializer());
        assertSame(ser1, bpw.getSerializer());

        // Reassignment to a different instance must throw IllegalStateException
        try {
            bpw.assignSerializer(ser2);
            fail("Expected IllegalStateException on overriding serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override serializer"));
        }

        // Null serializer assignment guard
        bpw.assignNullSerializer(ser1);
        bpw.assignNullSerializer(ser1);
        assertTrue(bpw.hasNullSerializer());

        try {
            bpw.assignNullSerializer(ser2);
            fail("Expected IllegalStateException on overriding null serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override null serializer"));
        }
    }

    @Test(timeout = 4000)
    public void testViewsIntrospection() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter pubWriter = findPropertyWriter(mapper, ViewFilteredBean.class, "pub");
        BeanPropertyWriter privWriter = findPropertyWriter(mapper, ViewFilteredBean.class, "priv");

        assertNotNull(pubWriter.getViews());
        assertEquals(1, pubWriter.getViews().length);
        assertEquals(ViewFilteredBean.PublicView.class, pubWriter.getViews()[0]);

        assertNotNull(privWriter.getViews());
        assertEquals(1, privWriter.getViews().length);
        assertEquals(ViewFilteredBean.PrivateView.class, privWriter.getViews()[0]);
    }

    // =========================================================================
    // Partition B: Standard Field Serialization
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardSerializeAsField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SampleBean bean = new SampleBean();

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"textField\":\"sample\""));
        assertTrue(json.contains("\"intVal\":42"));
    }

    @Test(timeout = 4000)
    public void testDirectSerializeAsFieldInvocation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, prov);
        jgen.writeEndObject();
        jgen.close();

        assertEquals("{\"textField\":\"sample\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAsFieldWithNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        SampleBean bean = new SampleBean();
        bean.textField = null;

        StringWriter sw = new StringWriter();
        JsonGenerator jgen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Default: null serializer is null, property is suppressed
        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, prov);
        jgen.writeEndObject();
        jgen.close();
        assertEquals("{}", sw.toString());

        // Now assign custom null serializer
        bpw.assignNullSerializer(new CustomNullSerializer());
        StringWriter sw2 = new StringWriter();
        JsonGenerator jgen2 = mapper.getFactory().createGenerator(sw2);

        jgen2.writeStartObject();
        bpw.serializeAsField(bean, jgen2, prov);
        jgen2.writeEndObject();
        jgen2.close();
        assertEquals("{\"textField\":\"CUSTOM_NULL\"}", sw2.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind-1 / Issue 223)
    // =========================================================================

    /**
     * TARGETED DEFECT TEST for JacksonDatabind-1:
     * In BeanPropertyWriter.serializeAsColumn(), when value is null, the method
     * wrote the null output but failed to return, falling through to normal
     * value serialization and outputting a duplicate null or throwing NPE.
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumn_NullValue_NoDuplicateNull_Issue223() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PojoAsArray pojo = new PojoAsArray(null, "bar");

        String json = mapper.writeValueAsString(pojo);

        // Ground-truth Defect Check:
        // Defective version produces: "[null,null,\"bar\"]"
        // Correct version produces: "[null,\"bar\"]"
        assertEquals("[null,\"bar\"]", json);
    }

    @Test(timeout = 4000)
    public void testSerializeAsColumn_NonNullValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PojoAsArray pojo = new PojoAsArray("foo", "bar");

        String json = mapper.writeValueAsString(pojo);
        assertEquals("[\"foo\",\"bar\"]", json);
    }

    @Test(timeout = 4000)
    public void testSerializeAsColumn_WithSuppressedValueCallsPlaceholder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PojoAsArrayWithSuppression pojo = new PojoAsArrayWithSuppression(new ArrayList<String>(), "active");

        String json = mapper.writeValueAsString(pojo);
        // Suppressed empty list must output placeholder 'null' to maintain column alignment
        assertEquals("[null,\"active\"]", json);
    }

    @Test(timeout = 4000)
    public void testDirectSerializeAsColumnAndPlaceholder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        SampleBean bean = new SampleBean();
        bean.textField = null;

        StringWriter sw = new StringWriter();
        JsonGenerator jgen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        jgen.writeStartArray();
        bpw.serializeAsColumn(bean, jgen, prov);
        bpw.serializeAsPlaceholder(bean, jgen, prov);
        jgen.writeEndArray();
        jgen.close();

        assertEquals("[null,null]", sw.toString());
    }

    // =========================================================================
    // Partition D: Value Suppression & Custom Serializers
    // =========================================================================

    @Test(timeout = 4000)
    public void testSuppressionOfEmptyAndDefaultValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EmptySuppressedBean bean = new EmptySuppressedBean();

        // Both items (empty list) and defaultInt (0) should be suppressed
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        // Mutate to non-empty and non-default
        bean.items.add("entry");
        bean.defaultInt = 7;

        String json2 = mapper.writeValueAsString(bean);
        assertTrue(json2.contains("\"items\":[\"entry\"]"));
        assertTrue(json2.contains("\"defaultInt\":7"));
    }

    @Test(timeout = 4000)
    public void testPolymorphicSerializationWithTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PolymorphicHolder holder = new PolymorphicHolder();

        String json = mapper.writeValueAsString(holder);
        assertTrue(json.contains("\"@type\":\"child\""));
        assertTrue(json.contains("\"name\":\"childVal\""));
    }

    // =========================================================================
    // Partition E: Dynamic Serializers & Circular Reference Guards
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectSelfReferenceCycleDetection() {
        ObjectMapper mapper = new ObjectMapper();
        SelfReferencingBean cycleBean = new SelfReferencingBean();

        try {
            mapper.writeValueAsString(cycleBean);
            fail("Expected JsonMappingException due to direct self-reference cycle");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Direct self-reference leading to cycle"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDepositSchemaPropertyViaObjectVisitor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        // Verify depositSchemaProperty with null does not throw
        bpw.depositSchemaProperty((JsonObjectFormatVisitor) null);

        final boolean[] visited = new boolean[1];
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void optionalProperty(BeanProperty prop) {
                visited[0] = true;
                assertEquals("textField", prop.getName());
            }

            @Override
            public void property(BeanProperty prop) {
                fail("Property should be optional");
            }
        };

        bpw.depositSchemaProperty(visitor);
        assertTrue(visited[0]);
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDepositSchemaPropertyViaObjectNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, SampleBean.class, "textField");

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        bpw.depositSchemaProperty(node, prov);
        assertTrue(node.has("textField"));
        assertNotNull(node.get("textField"));
    }

    @Test(timeout = 4000)
    public void testNonTrivialBaseTypeConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        BeanPropertyWriter bpw = findPropertyWriter(mapper, EmptySuppressedBean.class, "items");

        JavaType listType = mapper.constructType(List.class);
        bpw.setNonTrivialBaseType(listType);
        // Ensure no exception and configuration completes
        assertNotNull(bpw.getType());
    }

    @Test(timeout = 4000)
    public void testConstructorInvalidMemberGuard() {
        // Constructing BeanPropertyWriter with invalid member (not Field or Method) must throw
        AnnotatedConstructor invalidMember = new AnnotatedConstructor(null, null);
        Annotations annotations = new Annotations() {
            @Override
            public <A extends java.lang.annotation.Annotation> A get(Class<A> cls) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        BeanPropertyDefinition propDef = new BeanPropertyDefinition() {
            @Override
            public String getName() { return "badProp"; }
            @Override
            public PropertyName getFullName() { return new PropertyName("badProp"); }
            @Override
            public PropertyName getWrapperName() { return null; }
            @Override
            public boolean isExplicitlyIncluded() { return true; }
            @Override
            public boolean hasGetter() { return false; }
            @Override
            public boolean hasSetter() { return false; }
            @Override
            public boolean hasField() { return false; }
            @Override
            public boolean hasConstructorParameter() { return true; }
            @Override
            public AnnotatedMethod getGetter() { return null; }
            @Override
            public AnnotatedMethod getSetter() { return null; }
            @Override
            public AnnotatedField getField() { return null; }
            @Override
            public AnnotatedParameter getConstructorParameter() { return null; }
            @Override
            public AnnotatedMember getAccessor() { return null; }
            @Override
            public AnnotatedMember getMutator() { return null; }
            @Override
            public AnnotatedMember getNonConstructorMutator() { return null; }
            @Override
            public AnnotatedMember getPrimaryMember() { return null; }
        };

        JavaType stringType = SimpleType.constructUnsafe(String.class);
        try {
            new BeanPropertyWriter(propDef, invalidMember, annotations, stringType,
                    null, null, null, false, null);
            fail("Expected IllegalArgumentException for invalid member type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not pass member of type"));
        }
    }
}