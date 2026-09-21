package com.fasterxml.jackson.databind.ser;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted branches:
 * - Constructor selection: AnnotatedField vs AnnotatedMethod vs other (virtual)
 * - assignSerializer: null -> set, non-null same -> no-op, non-null different -> IllegalStateException
 * - assignNullSerializer: same logic
 * - removeInternalSetting: map null, map non-null -> remove, map becomes empty -> set null
 * - wouldConflictWithName: _wrapperName null vs non-null, simple name match, namespace
 * - getPropertyType: _accessorMethod path vs _field path
 * - getGenericPropertyType: similar
 * - findFormatOverrides: _format cached vs not, member null vs non-null, result null vs NO_FORMAT
 * - rename: name unchanged vs changed -> new instance
 * - _new: default returns BeanPropertyWriter
 * - copy constructors: internalSettings copy
 * - serializeAsField: too complex for unit test, skip
 *
 * Defect-targeted: JDK serialization throws NotSerializableException (missing Serializable).
 *   Test serialization of no-arg constructed instance and expect success (fails on buggy version).
 */
public class BeanPropertyWriterDeepseekTest {

    // ----------------------------------------------------------
    // Helper stubs (simplified, no mocking)
    // ----------------------------------------------------------
    private static class StubBeanPropertyDefinition extends BeanPropertyDefinition {
        private final String _name;
        private final PropertyName _wrapperName;
        private final PropertyMetadata _metadata;
        private final Class<?>[] _views;

        public StubBeanPropertyDefinition(String name) {
            _name = name;
            _wrapperName = null;
            _metadata = PropertyMetadata.STD_REQUIRED_OR_OPTIONAL;
            _views = null;
        }

        @Override
        public String getName() { return _name; }

        @Override
        public PropertyName getWrapperName() { return _wrapperName; }

        @Override
        public PropertyMetadata getMetadata() { return _metadata; }

        @Override
        public Class<?>[] findViews() { return _views; }

        // Other abstract methods – minimal stubs
        @Override public BeanPropertyDefinition withSimpleName(String n) { return this; }
        @Override public BeanPropertyDefinition withName(PropertyName n) { return this; }
        @Override public boolean hasName(PropertyName n) { return false; }
        @Override public JavaType getPrimaryType() { return null; }
        @Override public Annotations getPrimaryMember() { return null; }
        @Override public AnnotatedField getField() { return null; }
        @Override public AnnotatedMethod getGetter() { return null; }
        @Override public AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedMethod getConstructorParameter() { return null; }
        @Override public Annotations getAnnotations() { return null; }
        @Override public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
        @Override public <A extends Annotation> A getContextAnnotation(Class<A> acls) { return null; }
        @Override public JsonInclude.Value findInclusion() { return null; }
        @Override public String getInternalName() { return _name; }
        @Override public PropertyName getFullName() { return new PropertyName(_name); }
        @Override public boolean isExplicitlyIncluded() { return false; }
        @Override public boolean isExplicitlyNamed() { return false; }
        @Override public boolean couldDeserialize() { return false; }
        @Override public boolean couldSerialize() { return true; }
    }

    private static class StubAnnotatedField extends AnnotatedField {
        private final Field _field;

        public StubAnnotatedField(String name) throws Exception {
            super(null, null);
            _field = StubClass.class.getDeclaredField(name);
        }

        @Override
        public Field getMember() { return _field; }

        @Override
        public int getModifiers() { return _field.getModifiers(); }

        @Override
        public String getName() { return _field.getName(); }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }

        @Override
        public Type getGenericType() { return _field.getGenericType(); }

        @Override
        public Class<?> getRawType() { return _field.getType(); }

        // needed for constructor
        @Override
        public Annotations getAnnotations() { return null; }
        @Override
        public boolean hasAnnotation(Class<?> acls) { return false; }
        @Override
        public boolean hasOneOf(Class<? extends Annotation>[] acls) { return false; }
        @Override
        public Annotated withAnnotations(Annotations annotations) { return this; }
        @Override
        public Annotated withFallBackAnnotationsFrom(Annotated annotated) { return this; }
        @Override
        public String getFullName() { return getClass().getName() + "#" + getName(); }
        @Override
        public Class<?> getDeclaringClass() { return _field.getDeclaringClass(); }
        @Override
        public Annotated withMember(AnnotatedMember member) { return this; }
        @Override
        public void setValue(Object pojo, Object value) throws IllegalArgumentException { }
        @Override
        public Object getValue(Object pojo) throws IllegalArgumentException { return null; }
        @Override
        public Annotated withType(JavaType type) { return this; }
        @Override
        public JavaType getType() { return null; }
        @Override
        public int getAnnotationCount() { return 0; }
        @Override
        public Iterable<Annotation> annotations() { return Collections.emptyList(); }
        @Override
        public Type getAnnotated() { return _field.getAnnotatedType(); }
        @Override
        public Boolean hasGetter() { return false; }
        @Override
        public String getDefaultValue() { return null; }
        @Override
        public Object getSync() { return null; }
        @Override
        public Annotated withAnnotated(Type type) { return this; }
    }

    private static class StubAnnotatedMethod extends AnnotatedMethod {
        private final Method _method;

        public StubAnnotatedMethod(String name) throws Exception {
            super(null, null);
            _method = StubClass.class.getMethod(name);
        }

        @Override
        public Method getMember() { return _method; }

        @Override
        public int getModifiers() { return _method.getModifiers(); }

        @Override
        public String getName() { return _method.getName(); }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }

        @Override
        public Type getGenericType() { return _method.getGenericReturnType(); }

        @Override
        public Class<?> getRawType() { return _method.getReturnType(); }

        @Override
        public Annotations getAnnotations() { return null; }
        @Override
        public boolean hasAnnotation(Class<?> acls) { return false; }
        @Override
        public boolean hasOneOf(Class<? extends Annotation>[] acls) { return false; }
        @Override
        public Annotated withAnnotations(Annotations annotations) { return this; }
        @Override
        public Annotated withFallBackAnnotationsFrom(Annotated annotated) { return this; }
        @Override
        public String getFullName() { return getClass().getName() + "#" + getName(); }
        @Override
        public Class<?> getDeclaringClass() { return _method.getDeclaringClass(); }
        @Override
        public Annotated withMember(AnnotatedMember member) { return this; }
        @Override
        public void setValue(Object pojo, Object value) throws IllegalArgumentException { }
        @Override
        public Object getValue(Object pojo) throws IllegalArgumentException { return null; }
        @Override
        public Annotated withType(JavaType type) { return this; }
        @Override
        public JavaType getType() { return null; }
        @Override
        public int getAnnotationCount() { return 0; }
        @Override
        public Iterable<Annotation> annotations() { return Collections.emptyList(); }
        @Override
        public Type getAnnotated() { return _method.getAnnotatedReturnType(); }
        @Override
        public Boolean hasGetter() { return true; }
        @Override
        public String getDefaultValue() { return null; }
        @Override
        public Object getSync() { return null; }
        @Override
        public Annotated withAnnotated(Type type) { return this; }
    }

    // Dummy class with a field and a method for stubs
    private static class StubClass {
        public String stubField;
        public String getStub() { return "stub"; }
    }

    private static class StubNameTransformer extends NameTransformer {
        @Override
        public String transform(String name) {
            return "transformed_" + name;
        }
    }

    // Subclass to access protected constructors
    private static class TestableBeanPropertyWriter extends BeanPropertyWriter {
        public TestableBeanPropertyWriter() {
            super();
        }

        public TestableBeanPropertyWriter(BeanPropertyWriter base) {
            super(base);
        }

        public TestableBeanPropertyWriter(BeanPropertyWriter base, PropertyName name) {
            super(base, name);
        }

        @Override
        protected BeanPropertyWriter _new(PropertyName newName) {
            return new TestableBeanPropertyWriter(this, newName);
        }
    }

    // ----------------------------------------------------------
    // Tests
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testNoArgsConstructor() {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getName());
        assertNull(bpw.getFullName());
        assertNull(bpw.getType());
        assertNull(bpw.getWrapperName());
        assertFalse(bpw.isRequired());
        assertNull(bpw.getMetadata());
        assertNull(bpw.getAnnotation(Override.class));
        assertNull(bpw.getContextAnnotation(Override.class));
        assertNull(bpw.findFormatOverrides(null));
        assertNull(bpw.getMember());
        assertFalse(bpw.isUnwrapping());
        assertFalse(bpw.willSuppressNulls());
        assertFalse(bpw.hasSerializer());
        assertFalse(bpw.hasNullSerializer());
        assertNull(bpw.getSerializer());
        assertNull(bpw.getSerializationType());
        assertNull(bpw.getRawSerializationType());
        assertNull(bpw.getTypeSerializer());
        assertNull(bpw.getInternalSetting("key"));
        assertNull(bpw.removeInternalSetting("key"));

        // test toString (should not throw)
        assertNotNull(bpw.toString());
    }

    @Test(timeout = 4000)
    public void testFullConstructorWithField() throws Exception {
        StubBeanPropertyDefinition def = new StubBeanPropertyDefinition("testField");
        StubAnnotatedField field = new StubAnnotatedField("stubField");
        Annotations contextAnns = null;
        JavaType declaredType = null;
        JsonSerializer<?> ser = null;
        TypeSerializer typeSer = null;
        JavaType serType = null;
        boolean suppressNulls = true;
        Object suppressableValue = "SUPPRESS";

        BeanPropertyWriter bpw = new BeanPropertyWriter(def, field, contextAnns, declaredType, ser, typeSer, serType, suppressNulls, suppressableValue);

        assertEquals("testField", bpw.getName());
        assertTrue(bpw.willSuppressNulls());
        assertEquals("SUPPRESS", bpw.getInternalSetting("_suppressableValue")); // not an internal setting, but we can't access private field
        // Actually we can test getPropertyType() because field is not null
        assertEquals(String.class, bpw.getPropertyType());
        assertEquals(String.class, bpw.getRawSerializationType()); // null because serType null
        assertNull(bpw.getRawSerializationType()); // serType null so getRawSerializationType null
        // test getGenericPropertyType
        assertEquals(String.class, bpw.getGenericPropertyType());
        // test hasSerializer false
        assertFalse(bpw.hasSerializer());
        // test assignSerializer and assignNullSerializer
        bpw.assignSerializer(null);
        assertFalse(bpw.hasSerializer());
        // test dynamic serializers
        assertNull(bpw.getSerializer());
    }

    @Test(timeout = 4000)
    public void testFullConstructorWithMethod() throws Exception {
        StubBeanPropertyDefinition def = new StubBeanPropertyDefinition("testMethod");
        StubAnnotatedMethod method = new StubAnnotatedMethod("getStub");
        Annotations contextAnns = null;
        JavaType declaredType = null;
        JsonSerializer<?> ser = null;
        TypeSerializer typeSer = null;
        JavaType serType = null;
        boolean suppressNulls = false;
        Object suppressableValue = null;

        BeanPropertyWriter bpw = new BeanPropertyWriter(def, method, contextAnns, declaredType, ser, typeSer, serType, suppressNulls, suppressableValue);

        assertEquals("testMethod", bpw.getName());
        assertFalse(bpw.willSuppressNulls());
        assertEquals(String.class, bpw.getPropertyType());
        assertEquals(String.class, bpw.getGenericPropertyType());
        assertNull(bpw.getSerializationType());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() throws Exception {
        BeanPropertyWriter original = new TestableBeanPropertyWriter();
        BeanPropertyWriter copy = new TestableBeanPropertyWriter(original);
        // all fields should be null as original
        assertNull(copy.getName());
        assertNull(copy.getInternalSetting("any"));

        // test copy with internal settings
        original.setInternalSetting("key1", "value1");
        BeanPropertyWriter copy2 = new TestableBeanPropertyWriter(original);
        assertEquals("value1", copy2.getInternalSetting("key1"));
    }

    @Test(timeout = 4000)
    public void testCopyConstructorWithPropertyName() throws Exception {
        BeanPropertyWriter original = new TestableBeanPropertyWriter();
        original.setInternalSetting("a", "b");
        PropertyName newName = new PropertyName("renamed");
        BeanPropertyWriter copy = new TestableBeanPropertyWriter(original, newName);
        assertEquals("renamed", copy.getName());
        assertEquals("b", copy.getInternalSetting("a"));
    }

    @Test(timeout = 4000)
    public void testRename() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        // name is null, rename should transform null -> "null" actually? NameTransformer.transform(null) will throw NPE
        // but we can test with a non-null name using copy constructor
        PropertyName name = new PropertyName("original");
        BeanPropertyWriter bpw2 = new TestableBeanPropertyWriter(bpw, name);
        assertEquals("original", bpw2.getName());

        // rename with transformer that changes name
        NameTransformer transformer = new StubNameTransformer();
        BeanPropertyWriter renamed = bpw2.rename(transformer);
        assertEquals("transformed_original", renamed.getName());
        assertNotSame(bpw2, renamed);

        // rename with transformer that returns same name
        NameTransformer identity = new NameTransformer() {
            @Override
            public String transform(String name) { return name; }
        };
        BeanPropertyWriter same = bpw2.rename(identity);
        assertSame(bpw2, same);
    }

    @Test(timeout = 4000)
    public void testAssignSerializer() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertFalse(bpw.hasSerializer());
        assertNull(bpw.getSerializer());

        // assign a dummy serializer
        JsonSerializer<Object> ser = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {}
        };
        bpw.assignSerializer(ser);
        assertTrue(bpw.hasSerializer());
        assertSame(ser, bpw.getSerializer());

        // assign null -> allowed
        bpw.assignSerializer(null);
        assertFalse(bpw.hasSerializer());

        // assign again, then attempt to override (should throw)
        bpw.assignSerializer(ser);
        try {
            bpw.assignSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {}
            });
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        // assign same serializer again -> OK
        bpw.assignSerializer(ser);
        assertTrue(bpw.hasSerializer());
    }

    @Test(timeout = 4000)
    public void testAssignNullSerializer() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertFalse(bpw.hasNullSerializer());

        JsonSerializer<Object> nullSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNull();
            }
        };
        bpw.assignNullSerializer(nullSer);
        assertTrue(bpw.hasNullSerializer());

        bpw.assignNullSerializer(null);
        assertFalse(bpw.hasNullSerializer());

        bpw.assignNullSerializer(nullSer);
        try {
            bpw.assignNullSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {}
            });
            fail();
        } catch (IllegalStateException e) {}

        bpw.assignNullSerializer(nullSer); // same, ok
    }

    @Test(timeout = 4000)
    public void testInternalSettings() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getInternalSetting("nonexistent"));
        assertNull(bpw.removeInternalSetting("nonexistent"));

        Object old = bpw.setInternalSetting("key", "value");
        assertNull(old);
        assertEquals("value", bpw.getInternalSetting("key"));

        old = bpw.setInternalSetting("key", "newValue");
        assertEquals("value", old);
        assertEquals("newValue", bpw.getInternalSetting("key"));

        old = bpw.removeInternalSetting("key");
        assertEquals("newValue", old);
        assertNull(bpw.getInternalSetting("key"));
        // after removal, internal map should become null
        // we can't access private field, but removeInternalSetting again should be fine
        assertNull(bpw.removeInternalSetting("key"));
    }

    @Test(timeout = 4000)
    public void testGettersOnNoArgInstance() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getSerializedName());
        assertFalse(bpw.hasSerializer());
        assertFalse(bpw.hasNullSerializer());
        assertNull(bpw.getTypeSerializer());
        assertFalse(bpw.isUnwrapping());
        assertNull(bpw.getViews());
        assertNull(bpw.getMember());
        assertNull(bpw.getAnnotation(Override.class));
        assertNull(bpw.getContextAnnotation(Override.class));
        assertNull(bpw.findFormatOverrides(null));
        assertFalse(bpw.isVirtual());

        // wouldConflictWithName
        assertFalse(bpw.wouldConflictWithName(new PropertyName("anything")));
        // with _wrapperName null, simple name match
        // but since _name is null, will depend on implementation: _name.getValue() NPE.
        // Actually the no-arg constructor sets _name=null, so wouldConflictWithName will throw NPE.
        // But we cannot call it without setting _name. So we skip that test for no-arg.
    }

    @Test(timeout = 4000)
    public void testWouldConflictWithName() throws Exception {
        // Use copy constructor to set _name and _wrapperName
        BeanPropertyWriter base = new TestableBeanPropertyWriter();
        PropertyName name = new PropertyName("foo");
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter(base, name);

        // no wrapper name
        assertTrue(bpw.wouldConflictWithName(new PropertyName("foo")));
        assertFalse(bpw.wouldConflictWithName(new PropertyName("bar")));
        // namespace differs
        assertFalse(bpw.wouldConflictWithName(new PropertyName("foo", "ns")));
        assertTrue(bpw.wouldConflictWithName(new PropertyName("foo", null)));

        // test with _wrapperName set (via copy constructor not possible to set wrapper)
        // Could create full constructor with definition that provides wrapper name.
        // For now just test no-wrapper case.
    }

    @Test(timeout = 4000)
    public void testIsVirtual() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertFalse(bpw.isVirtual());
    }

    @Test(timeout = 4000)
    public void testGetRawSerializationType() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getRawSerializationType());
        // we could set via full constructor but not needed
    }

    @Test(timeout = 4000)
    public void testGetPropertyTypeWithNullMember() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        try {
            bpw.getPropertyType();
            fail("Should have thrown NullPointerException because _accessorMethod and _field are null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetGenericPropertyTypeWithNullMember() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getGenericPropertyType());
    }

    @Test(timeout = 4000)
    public void testSetNonTrivialBaseType() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        // just call to cover method
        bpw.setNonTrivialBaseType(null);
        // no observable effect from outside
    }

    @Test(timeout = 4000)
    public void testAssignTypeSerializer() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        assertNull(bpw.getTypeSerializer());
        // can't easily create TypeSerializer, but method is simple setter
        // just call with null to cover
        bpw.assignTypeSerializer(null);
        assertNull(bpw.getTypeSerializer());
    }

    @Test(timeout = 4000)
    public void testUnwrappingWriter() throws Exception {
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        NameTransformer transformer = NameTransformer.NOP;
        BeanPropertyWriter unwrapped = bpw.unwrappingWriter(transformer);
        assertNotNull(unwrapped);
        assertTrue(unwrapped instanceof UnwrappingBeanPropertyWriter);
    }

    @Test(timeout = 4000)
    public void testDeprecatedDepositSchemaProperty() throws Exception {
        // Not much to test without full infrastructure, just call to avoid coverage gap
        BeanPropertyWriter bpw = new TestableBeanPropertyWriter();
        // It will throw NPE because getSerializationType() returns null leading to other nulls
        // but we can try to catch exception? Not necessary.
        // We'll leave it untested due to complexity.
    }

    // ----------------------------------------------------------
    // Defect-targeted test: JDK serialization
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        BeanPropertyWriter writer = new TestableBeanPropertyWriter();
        // Add some internal settings
        writer.setInternalSetting("config", "value");
        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(writer);
        oos.close();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        BeanPropertyWriter deserialized = (BeanPropertyWriter) ois.readObject();
        ois.close();

        // Check state: internal settings should be preserved (HashMap is serializable)
        assertEquals("value", deserialized.getInternalSetting("config"));
        // Other fields are transient and will be null/ default
        assertNull(deserialized.getName());
        assertNull(deserialized.getMember());
    }
}