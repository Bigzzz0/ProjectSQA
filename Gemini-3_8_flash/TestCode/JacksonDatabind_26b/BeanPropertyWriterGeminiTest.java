package com.fasterxml.jackson.databind.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.BeanPropertyWriter
 * Defects4J Defect: java.io.NotSerializableException: com.fasterxml.jackson.databind.ser.BeanPropertyWriter
 *
 * 1. Defect Targeting:
 *    - BeanPropertyWriter must implement java.io.Serializable and successfully serialize/deserialize
 *      via JDK ObjectOutputStream/ObjectInputStream.
 *    - readResolve() branch: restore _field from AnnotatedField, _accessorMethod from AnnotatedMethod,
 *      and reset _dynamicSerializers when _serializer == null.
 *
 * 2. Constructors & Factories:
 *    - BeanPropertyWriter() no-arg constructor (virtual property baseline).
 *    - Full constructor with AnnotatedField, AnnotatedMethod, and virtual/other member (null field/method).
 *    - Copy constructor (base), copy constructor with PropertyName, and copy constructor with SerializedString.
 *    - rename(NameTransformer): identity branch (name unchanged) vs transformed branch (_new called).
 *    - unwrappingWriter(NameTransformer).
 *
 * 3. Configuration & State Modification:
 *    - assignTypeSerializer, setNonTrivialBaseType.
 *    - assignSerializer: happy path, redundant assign (same), and illegal re-assignment exception.
 *    - assignNullSerializer: happy path, redundant assign (same), and illegal re-assignment exception.
 *    - Internal settings map: get, set, remove, cleanup to null when empty.
 *
 * 4. Property Metadata & Schema Introspection:
 *    - getName, getFullName, getType, getWrapperName, isRequired, getMetadata, isVirtual, isUnwrapping.
 *    - getAnnotation, getContextAnnotation: null vs non-null member/annotations.
 *    - findFormatOverrides: cached format, null introspector/member, overrides present, NO_FORMAT fallback.
 *    - depositSchemaProperty(JsonObjectFormatVisitor): null visitor, required vs optional branches.
 *    - depositSchemaProperty(ObjectNode, SerializerProvider): SchemaAware serializer vs default schema fallback.
 *    - wouldConflictWithName: wrapperName branch vs simpleName match/mismatch and namespace check.
 *    - getPropertyType and getGenericPropertyType: method vs field vs null member.
 *
 * 5. Serialization Logic:
 *    - serializeAsField / serializeAsElement:
 *      * value == null: with nullSerializer vs without nullSerializer.
 *      * Dynamic serializer resolution (_serializer == null) with/without _nonTrivialBaseType.
 *      * Suppression: MARKER_FOR_EMPTY (ser.isEmpty is true/false) vs suppressableValue.equals(value).
 *      * Cycle handling (_handleSelfReference): FAIL_ON_SELF_REFERENCES enabled vs disabled,
 *        BeanSerializerBase cycle exception.
 *      * TypeSerializer: serialize vs serializeWithType.
 *    - serializeAsOmittedField: canOmitFields true vs false.
 *    - serializeAsPlaceholder: with nullSerializer vs null output.
 *    - toString formatting with method, field, virtual, and static serializer variations.
 */
public class BeanPropertyWriterGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnnotation {
        String value() default "test";
    }

    public static class SampleBean {
        @CustomAnnotation("fieldVal")
        public String textField = "fieldData";

        public SampleBean self = this;

        private Integer number = 42;

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public String getEmptyStr() {
            return "";
        }
    }

    public static class MockPropertyDefinition extends BeanPropertyDefinition {
        private final String _name;
        private final PropertyMetadata _metadata;
        private final PropertyName _wrapperName;
        private final Class<?>[] _views;

        public MockPropertyDefinition(String name) {
            this(name, PropertyMetadata.STD_OPTIONAL, null, null);
        }

        public MockPropertyDefinition(String name, PropertyMetadata metadata, PropertyName wrapperName, Class<?>[] views) {
            _name = name;
            _metadata = metadata;
            _wrapperName = wrapperName;
            _views = views;
        }

        @Override public String getName() { return _name; }
        @Override public PropertyName getFullName() { return new PropertyName(_name); }
        @Override public PropertyName getWrapperName() { return _wrapperName; }
        @Override public PropertyMetadata getMetadata() { return _metadata; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return false; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public AnnotatedMethod getGetter() { return null; }
        @Override public AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedField getField() { return null; }
        @Override public AnnotatedParameter getConstructorParameter() { return null; }
        @Override public AnnotatedMember getAccessor() { return null; }
        @Override public AnnotatedMember getMutator() { return null; }
        @Override public AnnotatedMember getNonConstructorMutator() { return null; }
        @Override public AnnotatedMember getPrimaryMember() { return null; }
        @Override public Class<?>[] findViews() { return _views; }
    }

    private static class DummyAnnotations implements Annotations, Serializable {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unchecked")
        @Override
        public <A extends Annotation> A get(Class<A> cls) {
            if (cls == CustomAnnotation.class) {
                return (A) SampleBean.class.getAnnotations()[0];
            }
            return null;
        }
        @Override public int size() { return 0; }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (JDK Serialization)
    // =========================================================================

    @Test(timeout = 4000)
    public void testJdkSerializationDefectTargeting() throws Exception {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        assertTrue("BeanPropertyWriter must implement java.io.Serializable", writer instanceof Serializable);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(writer);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object deserialized = in.readObject();
        in.close();

        assertNotNull("Deserialized instance should not be null", deserialized);
        assertTrue(deserialized instanceof BeanPropertyWriter);
    }

    @Test(timeout = 4000)
    public void testReadResolveRebuildsMembersAndSerializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedClass ac = desc.getClassInfo();

        AnnotatedField aField = null;
        for (AnnotatedField f : ac.fields()) {
            if ("textField".equals(f.getName())) {
                aField = f;
                break;
            }
        }
        assertNotNull(aField);

        MockPropertyDefinition propDef = new MockPropertyDefinition("textField");
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, aField, null,
                TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        assertNull(writer.getSerializer());
        assertFalse(writer.hasSerializer());

        Object resolved = writer.readResolve();
        assertSame(writer, resolved);
        assertEquals(String.class, writer.getPropertyType());

        AnnotatedMethod aMethod = null;
        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("getNumber".equals(m.getName())) {
                aMethod = m;
                break;
            }
        }
        assertNotNull(aMethod);

        MockPropertyDefinition propDef2 = new MockPropertyDefinition("number");
        BeanPropertyWriter writerMethod = new BeanPropertyWriter(propDef2, aMethod, null,
                TypeFactory.defaultInstance().constructType(Integer.class),
                null, null, null, false, null);

        Object resolvedMethod = writerMethod.readResolve();
        assertSame(writerMethod, resolvedMethod);
        assertEquals(Integer.class, writerMethod.getPropertyType());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullConstructorAndGettersWithFieldMember() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedField field = null;
        for (AnnotatedField f : desc.getClassInfo().fields()) {
            if ("textField".equals(f.getName())) {
                field = f;
                break;
            }
        }
        assertNotNull(field);

        PropertyName wrapper = new PropertyName("wrapper");
        Class<?>[] views = new Class<?>[] { Object.class };
        MockPropertyDefinition propDef = new MockPropertyDefinition("textField", PropertyMetadata.STD_REQUIRED, wrapper, views);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, field, new DummyAnnotations(),
                strType, null, null, strType, true, "defaultVal");

        assertEquals("textField", writer.getName());
        assertEquals(new PropertyName("textField"), writer.getFullName());
        assertEquals(wrapper, writer.getWrapperName());
        assertTrue(writer.isRequired());
        assertEquals(PropertyMetadata.STD_REQUIRED, writer.getMetadata());
        assertArrayEquals(views, writer.getViews());
        assertEquals(strType, writer.getType());
        assertEquals(strType, writer.getSerializationType());
        assertEquals(String.class, writer.getRawSerializationType());
        assertTrue(writer.willSuppressNulls());
        assertFalse(writer.isVirtual());
        assertFalse(writer.isUnwrapping());
        assertNotNull(writer.getMember());
        assertEquals(field.getAnnotated(), writer.getGenericPropertyType());

        SampleBean bean = new SampleBean();
        assertEquals("fieldData", writer.get(bean));
    }

    @Test(timeout = 4000)
    public void testFullConstructorWithMethodMember() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedMethod method = null;
        for (AnnotatedMethod m : desc.getClassInfo().memberMethods()) {
            if ("getNumber".equals(m.getName())) {
                method = m;
                break;
            }
        }
        assertNotNull(method);

        MockPropertyDefinition propDef = new MockPropertyDefinition("number");
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, method, null,
                intType, null, null, null, false, null);

        assertEquals(Integer.class, writer.getPropertyType());
        assertEquals(Integer.class, writer.getGenericPropertyType());
        assertNull(writer.getRawSerializationType());

        SampleBean bean = new SampleBean();
        assertEquals(Integer.valueOf(42), writer.get(bean));
    }

    @Test(timeout = 4000)
    public void testDefaultVirtualConstructor() {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        assertNull(writer.getName());
        assertNull(writer.getType());
        assertNull(writer.getWrapperName());
        assertNull(writer.getMetadata());
        assertNull(writer.getMember());
        assertNull(writer.getSerializer());
        assertNull(writer.getTypeSerializer());
        assertNull(writer.getViews());
        assertNull(writer.getGenericPropertyType());
        assertFalse(writer.willSuppressNulls());
        assertFalse(writer.hasSerializer());
        assertFalse(writer.hasNullSerializer());
    }

    @Test(timeout = 4000)
    public void testCopyConstructors() {
        MockPropertyDefinition propDef = new MockPropertyDefinition("prop");
        BeanPropertyWriter base = new BeanPropertyWriter(propDef, null, null,
                TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        base.setInternalSetting("key1", "val1");
        JavaType nonTrivial = TypeFactory.defaultInstance().constructType(SampleBean.class);
        base.setNonTrivialBaseType(nonTrivial);

        BeanPropertyWriter copy1 = new BeanPropertyWriter(base);
        assertEquals("prop", copy1.getName());
        assertEquals("val1", copy1.getInternalSetting("key1"));

        BeanPropertyWriter copy2 = new BeanPropertyWriter(base, new PropertyName("renamedProp"));
        assertEquals("renamedProp", copy2.getName());
        assertEquals("val1", copy2.getInternalSetting("key1"));

        BeanPropertyWriter copy3 = new BeanPropertyWriter(base, new SerializedString("serializedProp"));
        assertEquals("serializedProp", copy3.getName());
        assertEquals("val1", copy3.getInternalSetting("key1"));
    }

    @Test(timeout = 4000)
    public void testRenameAndUnwrapping() {
        MockPropertyDefinition propDef = new MockPropertyDefinition("oldName");
        BeanPropertyWriter base = new BeanPropertyWriter(propDef, null, null,
                TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        NameTransformer noop = NameTransformer.NOP;
        BeanPropertyWriter same = base.rename(noop);
        assertSame(base, same);

        NameTransformer prefix = NameTransformer.simpleTransformer("pre_", "");
        BeanPropertyWriter renamed = base.rename(prefix);
        assertNotSame(base, renamed);
        assertEquals("pre_oldName", renamed.getName());

        BeanPropertyWriter unwrapping = base.unwrappingWriter(prefix);
        assertNotNull(unwrapping);
        assertTrue(unwrapping instanceof UnwrappingBeanPropertyWriter);
    }

    @Test(timeout = 4000)
    public void testAssignSerializerAndNullSerializer() {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        JsonSerializer<Object> strSer = new StringSerializer();
        JsonSerializer<Object> nullSer = NullSerializer.instance;

        writer.assignSerializer(strSer);
        assertTrue(writer.hasSerializer());
        assertSame(strSer, writer.getSerializer());
        writer.assignSerializer(strSer); // redundant assignment allowed

        writer.assignNullSerializer(nullSer);
        assertTrue(writer.hasNullSerializer());
        writer.assignNullSerializer(nullSer); // redundant assignment allowed

        TypeSerializer typeSer = null;
        writer.assignTypeSerializer(typeSer);
        assertNull(writer.getTypeSerializer());
    }

    @Test(timeout = 4000)
    public void testInternalSettingsOperations() {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        assertNull(writer.getInternalSetting("k"));
        assertNull(writer.removeInternalSetting("k"));

        Object prev = writer.setInternalSetting("k", "v1");
        assertNull(prev);
        assertEquals("v1", writer.getInternalSetting("k"));

        prev = writer.setInternalSetting("k", "v2");
        assertEquals("v1", prev);
        assertEquals("v2", writer.getInternalSetting("k"));

        Object removed = writer.removeInternalSetting("k");
        assertEquals("v2", removed);
        assertNull(writer.getInternalSetting("k"));
        assertNull(writer.removeInternalSetting("k"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Conditions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWouldConflictWithName() {
        MockPropertyDefinition propDefNoWrapper = new MockPropertyDefinition("foo");
        BeanPropertyWriter writerNoWrapper = new BeanPropertyWriter(propDefNoWrapper, null, null, null, null, null, null, false, null);

        assertTrue(writerNoWrapper.wouldConflictWithName(new PropertyName("foo")));
        assertFalse(writerNoWrapper.wouldConflictWithName(new PropertyName("bar")));
        assertFalse(writerNoWrapper.wouldConflictWithName(new PropertyName("foo", "http://namespace")));

        MockPropertyDefinition propDefWrapper = new MockPropertyDefinition("foo", PropertyMetadata.STD_OPTIONAL, new PropertyName("wrap"), null);
        BeanPropertyWriter writerWrapper = new BeanPropertyWriter(propDefWrapper, null, null, null, null, null, null, false, null);

        assertTrue(writerWrapper.wouldConflictWithName(new PropertyName("wrap")));
        assertFalse(writerWrapper.wouldConflictWithName(new PropertyName("foo")));
    }

    @Test(timeout = 4000)
    public void testAnnotationsAndFormatOverrides() throws Exception {
        BeanPropertyWriter emptyWriter = new BeanPropertyWriter();
        assertNull(emptyWriter.getAnnotation(CustomAnnotation.class));
        assertNull(emptyWriter.getContextAnnotation(CustomAnnotation.class));
        assertNull(emptyWriter.findFormatOverrides(null));

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedField field = desc.getClassInfo().fields().iterator().next();

        MockPropertyDefinition propDef = new MockPropertyDefinition("textField");
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, field, null,
                TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        AnnotationIntrospector intr = mapper.getSerializationConfig().getAnnotationIntrospector();
        JsonFormat.Value f1 = writer.findFormatOverrides(intr);
        assertNull(f1); // no format override on field -> NO_FORMAT, returns null
        JsonFormat.Value f2 = writer.findFormatOverrides(intr); // cached lookup
        assertNull(f2);
    }

    @Test(timeout = 4000)
    public void testDepositSchemaPropertyVariants() throws Exception {
        BeanPropertyWriter optionalWriter = new BeanPropertyWriter(new MockPropertyDefinition("opt", PropertyMetadata.STD_OPTIONAL, null, null),
                null, null, TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base();
        optionalWriter.depositSchemaProperty(visitor);
        optionalWriter.depositSchemaProperty((JsonObjectFormatVisitor) null);

        BeanPropertyWriter reqWriter = new BeanPropertyWriter(new MockPropertyDefinition("req", PropertyMetadata.STD_REQUIRED, null, null),
                null, null, TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);
        reqWriter.depositSchemaProperty(visitor);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        reqWriter.assignSerializer(new StringSerializer());
        reqWriter.depositSchemaProperty(root, prov);
        assertTrue(root.has("req"));

        ObjectNode root2 = JsonNodeFactory.instance.objectNode();
        JsonSerializer<Object> notSchemaAware = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
        };
        reqWriter.assignSerializer(notSchemaAware);
        reqWriter.depositSchemaProperty(root2, prov);
        assertTrue(root2.has("req"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAssignSerializerOverrideThrowsException() {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        writer.assignSerializer(new StringSerializer());
        writer.assignSerializer(NullSerializer.instance);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAssignNullSerializerOverrideThrowsException() {
        BeanPropertyWriter writer = new BeanPropertyWriter();
        writer.assignNullSerializer(NullSerializer.instance);
        writer.assignNullSerializer(new StringSerializer());
    }

    // =========================================================================
    // Partition E: Serialization Execution Paths & Cycles
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeAsFieldAndElementWithNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedMethod getMethod = null;
        for (AnnotatedMethod m : desc.getClassInfo().memberMethods()) {
            if ("getNumber".equals(m.getName())) {
                getMethod = m;
                break;
            }
        }

        MockPropertyDefinition propDef = new MockPropertyDefinition("number");
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, getMethod, null,
                TypeFactory.defaultInstance().constructType(Integer.class),
                null, null, null, false, null);

        SampleBean bean = new SampleBean();
        bean.setNumber(null);

        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", sw.toString());

        writer.assignNullSerializer(NullSerializer.instance);
        sw = new StringWriter();
        gen = factory.createGenerator(sw);
        gen.writeStartObject();
        writer.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"number\":null}", sw.toString());

        sw = new StringWriter();
        gen = factory.createGenerator(sw);
        gen.writeStartArray();
        writer.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();
        assertEquals("[null]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAsFieldWithSuppressions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedMethod emptyMethod = null;
        for (AnnotatedMethod m : desc.getClassInfo().memberMethods()) {
            if ("getEmptyStr".equals(m.getName())) {
                emptyMethod = m;
                break;
            }
        }

        MockPropertyDefinition propDef = new MockPropertyDefinition("emptyStr");
        BeanPropertyWriter emptySuppressedWriter = new BeanPropertyWriter(propDef, emptyMethod, null,
                TypeFactory.defaultInstance().constructType(String.class),
                new StringSerializer(), null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);

        SampleBean bean = new SampleBean();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        emptySuppressedWriter.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", sw.toString());

        BeanPropertyWriter valueSuppressedWriter = new BeanPropertyWriter(propDef, emptyMethod, null,
                TypeFactory.defaultInstance().constructType(String.class),
                new StringSerializer(), null, null, false, "");

        sw = new StringWriter();
        gen = factory.createGenerator(sw);
        gen.writeStartObject();
        valueSuppressedWriter.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAsElementWithSuppressions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        AnnotatedMethod emptyMethod = null;
        for (AnnotatedMethod m : desc.getClassInfo().memberMethods()) {
            if ("getEmptyStr".equals(m.getName())) {
                emptyMethod = m;
                break;
            }
        }

        MockPropertyDefinition propDef = new MockPropertyDefinition("emptyStr");
        BeanPropertyWriter emptySuppressedWriter = new BeanPropertyWriter(propDef, emptyMethod, null,
                TypeFactory.defaultInstance().constructType(String.class),
                new StringSerializer(), null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);

        SampleBean bean = new SampleBean();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen.writeStartArray();
        emptySuppressedWriter.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();
        assertEquals("[null]", sw.toString());

        BeanPropertyWriter valueSuppressedWriter = new BeanPropertyWriter(propDef, emptyMethod, null,
                TypeFactory.defaultInstance().constructType(String.class),
                new StringSerializer(), null, null, false, "");

        sw = new StringWriter();
        gen = factory.createGenerator(sw);
        gen.writeStartArray();
        valueSuppressedWriter.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();
        assertEquals("[null]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAsOmittedField() throws Exception {
        BeanPropertyWriter writer = new BeanPropertyWriter(new MockPropertyDefinition("omittedProp"), null, null,
                TypeFactory.defaultInstance().constructType(String.class),
                null, null, null, false, null);

        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        writer.serializeAsOmittedField(new SampleBean(), gen, null);
        gen.close();
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSelfReferenceCycleDetection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);

        AnnotatedField selfField = null;
        for (AnnotatedField f : desc.getClassInfo().fields()) {
            if ("self".equals(f.getName())) {
                selfField = f;
                break;
            }
        }
        assertNotNull(selfField);

        MockPropertyDefinition propDef = new MockPropertyDefinition("self");
        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, selfField, null,
                type, null, null, null, false, null);

        SampleBean bean = new SampleBean();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        try {
            gen.writeStartObject();
            writer.serializeAsField(bean, gen, prov);
            fail("Expected JsonMappingException due to direct self-reference cycle");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Direct self-reference leading to cycle"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() throws Exception {
        BeanPropertyWriter virtualWriter = new BeanPropertyWriter();
        String strVirtual = virtualWriter.toString();
        assertTrue(strVirtual.contains("virtual"));
        assertTrue(strVirtual.contains("no static serializer"));

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SampleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);

        AnnotatedField field = desc.getClassInfo().fields().iterator().next();
        BeanPropertyWriter fieldWriter = new BeanPropertyWriter(new MockPropertyDefinition("fieldProp"), field, null,
                type, new StringSerializer(), null, null, false, null);
        String strField = fieldWriter.toString();
        assertTrue(strField.contains("field \""));
        assertTrue(strField.contains("static serializer of type"));

        AnnotatedMethod method = desc.getClassInfo().memberMethods().iterator().next();
        BeanPropertyWriter methodWriter = new BeanPropertyWriter(new MockPropertyDefinition("methodProp"), method, null,
                type, null, null, null, false, null);
        String strMethod = methodWriter.toString();
        assertTrue(strMethod.contains("via method "));
    }
}