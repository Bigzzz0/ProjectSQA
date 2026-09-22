package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

/**
 * Test suite for InnerClassProperty targeting:
 * - Full line and branch coverage
 * - Boundary conditions (null/empty/MAX)
 * - Defect: property index loss in copy operations (known Issue #1501)
 * - Serialization round-trip (readResolve/writeReplace)
 */
public class InnerClassPropertyDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Decision branches covered:
     *  - Constructor(InnerClassProperty, AnnotatedConstructor): null _annotated -> throw
     *  - Constructor(InnerClassProperty, JsonDeserializer): delegate.withValueDeserializer
     *  - Constructor(InnerClassProperty, PropertyName): delegate.withName
     *  - assignIndex: delegation + stored index
     *  - getPropertyIndex: delegation
     *  - getAnnotation: delegation
     *  - getMember: delegation
     *  - deserializeAndSet: token==NULL -> null value
     *  - deserializeAndSet: _valueTypeDeserializer != null -> deserializeWithType
     *  - deserializeAndSet: normal -> constructor call + deserialize into value
     *  - deserializeSetAndReturn: delegation to deserialize + setAndReturn
     *  - set: delegation
     *  - setAndReturn: delegation
     *  - readResolve: creates new InnerClassProperty with _annotated
     *  - writeReplace: if _annotated != null return this, else create new with AnnotatedConstructor
     *  - Constructor(InnerClassProperty, AnnotatedConstructor): null _annotated.getAnnotated() -> throw
     * 
     * Boundary conditions:
     *  - null constructor -> handled via inner class pattern (non-static)
     *  - null delegate -> not possible because constructor requires non-null
     *  - empty/null annotation type -> getAnnotation(any) returns null
     *  - property index: default 0, assignIndex(MAX_VALUE), reassign
     * 
     * Defect target: property index not preserved in withName/withValueDeserializer.
     */
    
    // -------------------- Helper stubs --------------------
    
    static class MockSettableBeanProperty extends SettableBeanProperty {
        private Object value;
        private int propertyIndex;
        private final JsonDeserializer<Object> valueDeserializer;
        private final JsonTypeDeserializer<?> valueTypeDeserializer;

        MockSettableBeanProperty(JsonDeserializer<Object> deser, JsonTypeDeserializer<?> typeDeser) {
            // inner property with a dummy name
            super(PropertyName.construct("test"), null, null, null, null);
            this.valueDeserializer = deser;
            this.valueTypeDeserializer = typeDeser;
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
        @Override
        public AnnotatedMember getMember() { return null; }
        @Override
        public void set(Object instance, Object value) throws IOException { this.value = value; }
        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException { set(instance, value); return value; }
        @Override
        public void assignIndex(int index) { propertyIndex = index; }
        @Override
        public int getPropertyIndex() { return propertyIndex; }
        @Override
        public MockSettableBeanProperty withName(PropertyName newName) {
            MockSettableBeanProperty copy = new MockSettableBeanProperty(valueDeserializer, valueTypeDeserializer);
            copy.propertyIndex = this.propertyIndex; // preserve index (real impl does this)
            return copy;
        }
        @Override
        public MockSettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            MockSettableBeanProperty copy = new MockSettableBeanProperty((JsonDeserializer<Object>) deser, valueTypeDeserializer);
            copy.propertyIndex = this.propertyIndex; // preserve index
            return copy;
        }
        @Override
        public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException {}
        @Override
        public Object deserializeSetAndReturn(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException { return null; }
        @Override
        public JsonDeserializer<Object> getValueDeserializer() { return valueDeserializer; }
        @Override
        public JsonTypeDeserializer<?> getValueTypeDeserializer() { return valueTypeDeserializer; }
    }

    // A non-static inner class to obtain a constructor with enclosing instance
    class Outer {
        class Inner {
            // default constructor
        }
    }

    // Simple deserializer stub for null value
    static class NullValueDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
        @Override
        public Object getNullValue(DeserializationContext ctxt) { return "nullValue"; }
    }

    // Simple deserializer for type deserializer path
    static class WithTypeDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException { return "deserialized"; }
        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, JsonTypeDeserializer<?> typeDeser) throws IOException {
            return "deserializedWithType";
        }
    }

    // Dummy JsonTypeDeserializer (cannot instantiate directly, use a stub)
    static class DummyTypeDeserializer extends JsonTypeDeserializer<Object> {
        @Override public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
        @Override public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
        @Override public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
        @Override public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
    }

    // Simple JsonParser stub that returns a given current token
    static class FixedTokenParser extends JsonParser {
        private final JsonToken token;
        FixedTokenParser(JsonToken token) { this.token = token; }
        @Override public JsonToken getCurrentToken() { return token; }
        // Required overrides (stubs)
        @Override public JsonToken nextToken() throws IOException { return null; }
        @Override public JsonToken nextValue() throws IOException { return null; }
        @Override public JsonParser skipChildren() throws IOException { return this; }
        @Override public JsonStreamContext getParsingContext() { return null; }
        @Override public String getCurrentName() throws IOException { return null; }
        @Override public String getText() throws IOException { return null; }
        @Override public char[] getTextCharacters() throws IOException { return null; }
        @Override public int getTextLength() throws IOException { return 0; }
        @Override public int getTextOffset() throws IOException { return 0; }
        @Override public Number getNumberValue() throws IOException { return null; }
        @Override public NumberType getNumberType() throws IOException { return null; }
        @Override public int getIntValue() throws IOException { return 0; }
        @Override public long getLongValue() throws IOException { return 0; }
        @Override public double getDoubleValue() throws IOException { return 0; }
        @Override public BigInteger getBigIntegerValue() throws IOException { return null; }
        @Override public float getFloatValue() throws IOException { return 0; }
        @Override public BigDecimal getDecimalValue() throws IOException { return null; }
        @Override public Object getEmbeddedObject() throws IOException { return null; }
        @Override public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return null; }
        @Override public JsonLocation getCurrentLocation() { return null; }
        @Override public JsonLocation getTokenLocation() { return null; }
        @Override public void close() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public int getCurrentTokenId() { return 0; }
        @Override public boolean hasCurrentToken() { return false; }
        @Override public boolean hasText() { return false; }
        @Override public boolean hasToken(JsonToken t) { return token == t; }
        @Override public boolean hasTokenId(int id) { return false; }
        @Override public JsonToken getLastClearedToken() { return null; }
        @Override public void clearCurrentToken() {}
        @Override public JsonToken getCurrentTokenOrNull() { return token; }
        @Override public boolean isExpectedStartArrayToken() { return false; }
        @Override public boolean isExpectedStartObjectToken() { return false; }
    }

    // Minimal DeserializationContext stub
    static class MockDeserializationContext extends DeserializationContext {
        protected MockDeserializationContext() {
            super((DeserializerFactory) null, null, (DeserializationConfig) null, null, null, null);
        }
        // Required overrides (stubs)
        @Override public JsonParser getParser() { return null; }
        @Override public Object findInjectableValue(Object valueId, Object forInstance, BeanProperty forProperty) { return null; }
        @Override public int getAttribute(Object key) { return 0; }
        @Override public DeserializationContext setAttribute(Object key, Object value) { return this; }
        @Override public JavaType constructType(Class<?> cls) { return null; }
        @Override public Class<?> getActiveView() { return null; }
        @Override public boolean hasValueDeserializerFor(JavaType type) { return false; }
        @Override public JsonDeserializer<Object> findRootValueDeserializer(JavaType type) { return null; }
        @Override public JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) { return null; }
        @Override public JsonDeserializer<Object> findNonContextualValueDeserializer(JavaType type) { return null; }
        @Override public JsonDeserializer<?> findKeyDeserializer(JavaType keyType, BeanProperty prop) { return null; }
        @Override public JsonpCharacterEscapes getCharacterEscapes() { return null; }
        @Override public int getDeserializationFeatures() { return 0; }
        @Override public boolean hasDeserializationFeatures(int featureMask) { return false; }
        @Override public int _extractState(JsonToken t) { return 0; }
        @Override public int _extractBitState(int bits) { return 0; }
        @Override public long getTimeInMillis() { return 0; }
        @Override public Locale getLocale() { return null; }
        @Override public TimeZone getTimeZone() { return null; }
        @Override public boolean isEnabled(DeserializationFeature feature) { return false; }
        @Override public boolean isEnabled(MapperFeature feature) { return false; }
        @Override public boolean isEnabled(JsonParser.Feature feature) { return false; }
        @Override public boolean isEnabled(JsonGenerator.Feature feature) { return false; }
        @Override public Base64Variant getBase64Variant() { return null; }
        @Override public String[] getArrayCompletions() { return null; }
        @Override public Object getAttribute(Object key) { return null; }
        @Override public DeserializationConfig getConfig() { return null; }
        @Override public DeserializerFactory getFactory() { return null; }
        @Override public AnnotationIntrospector getAnnotationIntrospector() { return null; }
        @Override public TypeDeserializer getTypeDeserializer(JavaType baseType) { return null; }
        @Override public JsonNode getNodeFactory() { return null; }
    }

    // -------------------- Tests --------------------

    @Test(timeout = 4000)
    public void testConstructorAndGetters() throws Exception {
        // Given: a delegate and a constructor (from inner class)
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0]; // actually Outer$Inner(Outer)
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // Expect: delegate preserved, creator set
        assertSame(delegate, prop._delegate);
        assertSame(ctor, prop._creator);
        assertNull(prop._annotated);

        // Test getAnnotation and getMember delegation
        assertNull(prop.getAnnotation(Override.class));
        assertNull(prop.getMember());
    }

    @Test(timeout = 4000)
    public void testPropertyIndexDelegation() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // Assign index on InnerClassProperty -> should delegate
        prop.assignIndex(42);
        assertEquals(42, delegate.getPropertyIndex());
        assertEquals(42, prop.getPropertyIndex());

        // Reassign
        prop.assignIndex(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, delegate.getPropertyIndex());
    }

    @Test(timeout = 4000)
    public void testWithNamePreservesIndex() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        delegate.assignIndex(7);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // When: withName
        InnerClassProperty renamed = prop.withName(PropertyName.construct("newName"));
        // Then: index must be preserved
        assertEquals(7, renamed.getPropertyIndex());
        assertSame(prop._creator, renamed._creator);
        assertNotSame(prop, renamed);
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerPreservesIndex() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        JsonDeserializer<Object> deser1 = new NullValueDeserializer();
        JsonDeserializer<Object> deser2 = new WithTypeDeserializer();
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(deser1, null);
        delegate.assignIndex(13);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // When: withValueDeserializer
        InnerClassProperty newProp = prop.withValueDeserializer(deser2);
        // Then: index must be preserved (on delegate)
        assertEquals(13, newProp.getPropertyIndex());
        assertNotSame(prop, newProp);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetNullToken() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        JsonDeserializer<Object> nullDeser = new NullValueDeserializer();
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(nullDeser, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        FixedTokenParser parser = new FixedTokenParser(JsonToken.VALUE_NULL);
        MockDeserializationContext ctxt = new MockDeserializationContext();
        Object bean = new Object();

        prop.deserializeAndSet(parser, ctxt, bean);
        // After set, delegate should have stored "nullValue"
        assertEquals("nullValue", delegate.value);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithTypeDeserializer() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        JsonDeserializer<Object> deser = new WithTypeDeserializer();
        DummyTypeDeserializer typeDeser = new DummyTypeDeserializer();
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(deser, typeDeser);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        FixedTokenParser parser = new FixedTokenParser(JsonToken.VALUE_STRING); // any non-null token
        MockDeserializationContext ctxt = new MockDeserializationContext();
        Object bean = new Object();

        prop.deserializeAndSet(parser, ctxt, bean);
        // The value from deserializeWithType should be stored
        assertEquals("deserializedWithType", delegate.value);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetNormal() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        JsonDeserializer<Object> deser = new WithTypeDeserializer(); // but _valueTypeDeserializer is null
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(deser, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        FixedTokenParser parser = new FixedTokenParser(JsonToken.VALUE_STRING);
        MockDeserializationContext ctxt = new MockDeserializationContext();
        Object bean = new Object();

        prop.deserializeAndSet(parser, ctxt, bean);
        // Normal branch: constructor called with bean, then deserialize stored value
        assertEquals("deserialized", delegate.value);
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturn() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        JsonDeserializer<Object> deser = new WithTypeDeserializer();
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(deser, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        FixedTokenParser parser = new FixedTokenParser(JsonToken.VALUE_STRING);
        MockDeserializationContext ctxt = new MockDeserializationContext();
        Object instance = new Object();

        // deserializeSetAndReturn should call setAndReturn with the result of deserialize
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);
        // Since deserialize returns "deserialized" (via normal path) and setAndReturn returns the value
        assertEquals("deserialized", result);
    }

    @Test(timeout = 4000)
    public void testSetAndSetAndReturn() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        Object bean = new Object();
        Object value = "value";

        prop.set(bean, value);
        assertSame(value, delegate.value);

        Object returned = prop.setAndReturn(bean, value);
        assertSame(value, returned);
    }

    @Test(timeout = 4000)
    public void testReadResolve() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        // Use the constructor that sets _annotated
        AnnotatedConstructor ann = new AnnotatedConstructor(null, ctor, null, null);
        InnerClassProperty original = new InnerClassProperty(delegate, ctor);
        // Manually set _annotated (normally set via serialization constructor)
        // We'll invoke readResolve on a copy that has _annotated set.
        // Create a copy via the protected constructor that takes AnnotatedConstructor
        InnerClassProperty copyWithAnn = new InnerClassProperty(original, ann);
        
        // readResolve should return a new InnerClassProperty with the same _annotated
        InnerClassProperty resolved = (InnerClassProperty) copyWithAnn.readResolve();
        assertNotNull(resolved._creator);
        assertSame(ctor, resolved._creator);
        assertSame(copyWithAnn._delegate, resolved._delegate);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithNullAnnotatedThrows() {
        // The protected constructor should throw if _annotated.getAnnotated() returns null
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        // Create an AnnotatedConstructor that returns null from getAnnotated()
        AnnotatedConstructor ann = new AnnotatedConstructor(null, null, null, null) {
            @Override
            public Constructor<?> getAnnotated() {
                return null;
            }
        };
        new InnerClassProperty(new InnerClassProperty(delegate, (Constructor<?>)null) {}, ann);
    }

    @Test(timeout = 4000)
    public void testWriteReplaceWhenAnnotatedNotNull() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        AnnotatedConstructor ann = new AnnotatedConstructor(null, ctor, null, null);
        InnerClassProperty prop = new InnerClassProperty(new InnerClassProperty(delegate, ctor), ann);
        // writeReplace should return this if _annotated != null
        Object replaced = prop.writeReplace();
        assertSame(prop, replaced);
    }

    @Test(timeout = 4000)
    public void testWriteReplaceWhenAnnotatedNull() throws Exception {
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        // _annotated is null, so writeReplace creates a new instance
        Object replaced = prop.writeReplace();
        assertTrue(replaced instanceof InnerClassProperty);
        InnerClassProperty newProp = (InnerClassProperty) replaced;
        assertNotNull(newProp._annotated);
        assertNotNull(newProp._creator);
        assertSame(ctor, newProp._creator);
    }

    // ---- Direct defect reproduction (Issue #1501) ----
    @Test(timeout = 4000)
    public void testPropertyIndexPreservationAfterWithNameAndDeserializer() throws Exception {
        // This test directly targets the defect: property index loss in copy constructors.
        // An index assigned to the original InnerClassProperty must survive withName and withValueDeserializer.
        Outer outer = new Outer();
        Constructor<?> ctor = Outer.Inner.class.getConstructors()[0];
        MockSettableBeanProperty delegate = new MockSettableBeanProperty(null, null);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // Set index on the property (delegates to delegate)
        prop.assignIndex(99);
        assertEquals(99, prop.getPropertyIndex());

        // withName must preserve index
        InnerClassProperty renamed = prop.withName(PropertyName.construct("b"));
        assertEquals("Property index lost after withName", 99, renamed.getPropertyIndex());

        // withValueDeserializer must preserve index
        JsonDeserializer<Object> deser = new NullValueDeserializer();
        InnerClassProperty withDeser = prop.withValueDeserializer(deser);
        assertEquals("Property index lost after withValueDeserializer", 99, withDeser.getPropertyIndex());

        // Chain both
        InnerClassProperty chained = prop.withName(PropertyName.construct("c")).withValueDeserializer(deser);
        assertEquals("Property index lost after chain", 99, chained.getPropertyIndex());
    }
}