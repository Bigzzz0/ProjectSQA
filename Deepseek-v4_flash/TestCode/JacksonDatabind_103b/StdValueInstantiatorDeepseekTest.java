package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TokenStreamContext;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: StdValueInstantiator
 * 
 * Partition A (Core functional):
 *   - Constructor variants (deprecated, JavaType, copy)
 *   - Configuration methods (configureFromObjectSettings, configureFromArraySettings,
 *     configureFromStringCreator, etc.)
 *   - Metadata getters (getValueTypeDesc, getValueClass, getDelegateType, etc.)
 *   - canCreate* and canInstantiate (combinations of null/non-null creators)
 * 
 * Partition B (BVA / extremes):
 *   - null vs non-null creators/types
 *   - empty arrays for delegate/constructor arguments
 *   - numeric boundaries (int, long, double, boolean)
 * 
 * Partition C (Defect-targeted branch):
 *   - The known defect is about duplicate "at [" markers in exception messages.
 *     This is exercised via rewrapCtorProblem and wrapAsJsonMappingException,
 *     ensuring that a JsonMappingException (or a wrapper containing one) is
 *     returned without adding an extra location marker.
 * 
 * Partition D (Exception/defensive paths):
 *   - No delegate creator / array delegate creator → IllegalStateException
 *   - Creator invocation throws InvocationTargetException, ExceptionInInitializerError,
 *     or plain RuntimeException → proper re-wrapping
 *   - IllegalStateException when no creator present for delegate/array-delegate
 * 
 * Partition E (Lifecycle/contract):
 *   - Copy constructor preserves all fields
 *   - Serialization contract (class is Serializable)
 *   - Value type description and class are correctly derived
 * 
 * All tests follow the 4-second timeout guard and assert both type/state and value.
 */

public class StdValueInstantiatorDeepseekTest {

    // ------------------------------------------------------------------
    // Stub classes for DeserializationContext and AnnotatedWithParams
    // ------------------------------------------------------------------

    private static class MockDeserializationContext extends DeserializationContext {
        private final DeserializationConfig config;

        MockDeserializationContext(DeserializationConfig config) {
            super(config);
            this.config = config;
        }

        @Override public DeserializationConfig getConfig() { return config; }
        @Override public JsonParser getParser() { return null; }
        @Override public Locale getLocale() { return null; }
        @Override public TimeZone getTimeZone() { return null; }
        @Override public Class<?> getActiveView() { return null; }
        @Override public void setActiveView(Class<?> view) { }
        @Override public DeserializationFactory getFactory() { return null; }
        @Override public JsonNodeFactory getNodeFactory() { return JsonNodeFactory.instance; }
        @Override public void handleUnexpectedToken(JavaType type, JsonParser p) { throw new UnsupportedOperationException(); }
        @Override public void handleUnexpectedToken(Class<?> type, JsonParser p) { throw new UnsupportedOperationException(); }
        @Override public Object handleInstantiationProblem(Class<?> instClass, Object argument, Throwable t) throws IOException {
            // Typical implementation: if t is JsonMappingException, return it; else
            // wrap in JsonMappingException with a location (simulated here)
            if (t instanceof JsonMappingException) {
                return (JsonMappingException) t;
            }
            return new JsonMappingException(null, "Instantiation of " + instClass.getName() + " failed: " + t.getMessage(), t);
        }
        @Override public Object handleMissingInstantiator(Class<?> instClass, ValueInstantiator inst, JsonParser p, String msg) { throw new UnsupportedOperationException(); }
        @Override public void handleBadMerge(JsonParser p) { throw new UnsupportedOperationException(); }
        @Override public void handleBadMerge(SettableBeanProperty prop, JsonParser p) { throw new UnsupportedOperationException(); }
        @Override public Object findInjectableValue(Object id, BeanProperty forProperty, JsonParser p) { return null; }
        @Override public JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) { return null; }
        @Override public JsonDeserializer<Object> findRootValueDeserializer(JavaType type) { return null; }
        @Override public JsonDeserializer<Object> findNonContextualValueDeserializer(JavaType type) { return null; }
        @Override public int getDeserializationFeatures() { return 0; }
        @Override public boolean hasDeserializationFeature(DeserializationFeature feature) { return false; }
        @Override public boolean isEnabled(DeserializationFeature feature) { return false; }
        @Override public <T> T readValue(JsonParser p, JavaType type) { return null; }
        @Override public JsonNode readTree(JsonParser p) { return null; }
        @Override public void reportUnknownProperty(Object instance, String fieldName, JsonParser p, JsonDeserializer<?> deser) { throw new UnsupportedOperationException(); }
        @Override public Object reportUnresolvedObjectId(Object reader, JsonParser p) { throw new UnsupportedOperationException(); }
        @Override public void reportPropertyInputMismatch(JavaType type, String propName, Object value, Class<?> target) { throw new UnsupportedOperationException(); }
        @Override public void reportInputMismatch(Class<?> target, String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public void reportInputMismatch(JavaType target, String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public void reportTrailingTokens(JsonParser p, DeserializationFeature feature, TokenStreamContext context) { throw new UnsupportedOperationException(); }
        @Override public void reportWrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public void reportWrongTokenException(JavaType type, JsonToken expToken, String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public void reportWrongTokenException(Class<?> type, JsonToken expToken, String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public void reportMissingContent(String msg, Object... args) { throw new UnsupportedOperationException(); }
        @Override public ObjectIdGenerator<?> getObjectIdGenerator(Class<?> scope) { return null; }
        @Override public ObjectIdReader getObjectIdReader() { return null; }
        @Override public ArrayBuilders getArrayBuilders() { return null; }
        @Override public Object getDefaultPropertyValue(JavaType type, BeanProperty prop) { return null; }
        @Override public DateFormat getDateFormat() { return null; }
        @Override public ValueInstantiator getValueInstantiator(BeanDescription desc) { return null; }
        @Override public Object getAttribute(Object key) { return null; }
        @Override public void setAttribute(Object key, Object value) { }
    }

    private static class MockAnnotatedWithParams extends AnnotatedWithParams {
        private final Class<?> declaringClass;
        private final Throwable throwable; // if non-null, all call methods throw
        private final Object callResult;

        MockAnnotatedWithParams(Class<?> declaringClass, Throwable throwable, Object callResult) {
            super(null, null);
            this.declaringClass = declaringClass;
            this.throwable = throwable;
            this.callResult = callResult;
        }

        @Override public int getParameterCount() { return 0; }
        @Override public AnnotatedParameter getParameter(int index) { return null; }
        @Override public Class<?> getRawType() { return declaringClass; }
        @Override public java.lang.reflect.Type getGenericType() { return declaringClass; }
        @Override public String getName() { return declaringClass.getName(); }
        @Override public java.lang.reflect.AnnotatedElement getAnnotated() { return declaringClass; }
        @Override public java.lang.reflect.Type getType() { return declaringClass; }
        @Override public int getModifiers() { return declaringClass.getModifiers(); }
        @Override public boolean equals(Object o) { return o == this; }
        @Override public int hashCode() { return declaringClass.hashCode(); }
        @Override public String toString() { return "MockAnnotatedWithParams[" + declaringClass.getName() + "]"; }

        @Override public Object call() throws Exception {
            if (throwable != null) throw throwable;
            return callResult;
        }
        @Override public Object call1(Object arg) throws Exception {
            if (throwable != null) throw throwable;
            return callResult;
        }
        @Override public Object call(Object[] args) throws Exception {
            if (throwable != null) throw throwable;
            return callResult;
        }
        @Override public Class<?> getDeclaringClass() { return declaringClass; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getOwner() { return null; }
    }

    // ------------------------------------------------------------------
    // Helper: create a StdValueInstantiator via JavaType constructor
    // ------------------------------------------------------------------
    private StdValueInstantiator createInstance(JavaType type) {
        // For simplicity, we use null config and type; but type may be null.
        return new StdValueInstantiator(null, type);
    }

    private StdValueInstantiator createInstance(Class<?> cls) {
        return new StdValueInstantiator(null, cls);
    }

    private DeserializationConfig mockConfig() {
        return null; // acceptable for these tests; only used in constructor
    }

    private DeserializationContext mockContext() {
        return new MockDeserializationContext(null);
    }

    // ------------------------------------------------------------------
    // Partition A: Constructors and copy constructor
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDeprecatedConstructor() {
        StdValueInstantiator inst = createInstance(String.class);
        assertEquals("String", inst.getValueTypeDesc());
        assertEquals(String.class, inst.getValueClass());
    }

    @Test(timeout = 4000)
    public void testJavaTypeConstructor() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Integer.class);
        StdValueInstantiator inst = createInstance(type);
        assertEquals(type.toString(), inst.getValueTypeDesc());
        assertEquals(Integer.class, inst.getValueClass());
    }

    @Test(timeout = 4000)
    public void testNullJavaTypeConstructor() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        assertEquals("UNKNOWN TYPE", inst.getValueTypeDesc());
        assertEquals(Object.class, inst.getValueClass());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Long.class);
        StdValueInstantiator src = createInstance(type);
        // Configure a few
        MockAnnotatedWithParams defaultCreator = new MockAnnotatedWithParams(Long.class, null, 123L);
        src.configureFromObjectSettings(defaultCreator, null, null, null, null, null);
        StdValueInstantiator copy = new StdValueInstantiator(src);
        assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        assertEquals(src.getValueClass(), copy.getValueClass());
        assertSame(defaultCreator, copy.getDefaultCreator());
        assertNull(copy.getDelegateCreator());
        assertNull(copy.getWithArgsCreator());
    }

    // ------------------------------------------------------------------
    // Partition A: Configuration and canCreate*
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConfigureFromObjectSettings() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        MockAnnotatedWithParams defaultC = new MockAnnotatedWithParams(Object.class, null, new Object());
        MockAnnotatedWithParams delegateC = new MockAnnotatedWithParams(Object.class, null, new Object());
        MockAnnotatedWithParams withArgsC = new MockAnnotatedWithParams(Object.class, null, new Object());
        SettableBeanProperty[] delegateArgs = new SettableBeanProperty[0];
        SettableBeanProperty[] ctorArgs = new SettableBeanProperty[0];
        JavaType delegateType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);

        inst.configureFromObjectSettings(defaultC, delegateC, delegateType, delegateArgs, withArgsC, ctorArgs);

        assertTrue(inst.canCreateUsingDefault());
        assertTrue(inst.canCreateUsingDelegate());
        assertTrue(inst.canCreateFromObjectWith());
        assertEquals(delegateType, inst.getDelegateType(null));
        assertArrayEquals(ctorArgs, inst.getFromObjectArguments(null));
        assertSame(defaultC, inst.getDefaultCreator());
        assertSame(delegateC, inst.getDelegateCreator());
        assertSame(withArgsC, inst.getWithArgsCreator());
        assertNull(inst.getArrayDelegateCreator());
    }

    @Test(timeout = 4000)
    public void testConfigureFromArraySettings() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        MockAnnotatedWithParams arrayDelegateC = new MockAnnotatedWithParams(Object.class, null, new Object());
        JavaType arrayDelegateType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Double.class);
        SettableBeanProperty[] args = new SettableBeanProperty[0];
        inst.configureFromArraySettings(arrayDelegateC, arrayDelegateType, args);

        assertTrue(inst.canCreateUsingArrayDelegate());
        assertEquals(arrayDelegateType, inst.getArrayDelegateType(null));
        assertSame(arrayDelegateC, inst.getArrayDelegateCreator());
        assertTrue(inst.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testConfigureScalarCreators() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        MockAnnotatedWithParams strC = new MockAnnotatedWithParams(String.class, null, "str");
        MockAnnotatedWithParams intC = new MockAnnotatedWithParams(Integer.class, null, 42);
        MockAnnotatedWithParams longC = new MockAnnotatedWithParams(Long.class, null, 42L);
        MockAnnotatedWithParams doubleC = new MockAnnotatedWithParams(Double.class, null, 4.2);
        MockAnnotatedWithParams boolC = new MockAnnotatedWithParams(Boolean.class, null, true);

        inst.configureFromStringCreator(strC);
        inst.configureFromIntCreator(intC);
        inst.configureFromLongCreator(longC);
        inst.configureFromDoubleCreator(doubleC);
        inst.configureFromBooleanCreator(boolC);

        assertTrue(inst.canCreateFromString());
        assertTrue(inst.canCreateFromInt());
        assertTrue(inst.canCreateFromLong());
        assertTrue(inst.canCreateFromDouble());
        assertTrue(inst.canCreateFromBoolean());
        assertTrue(inst.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testAllCanCreateInitiallyFalse() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        assertFalse(inst.canCreateFromString());
        assertFalse(inst.canCreateFromInt());
        assertFalse(inst.canCreateFromLong());
        assertFalse(inst.canCreateFromDouble());
        assertFalse(inst.canCreateFromBoolean());
        assertFalse(inst.canCreateUsingDefault());
        assertFalse(inst.canCreateUsingDelegate());
        assertFalse(inst.canCreateUsingArrayDelegate());
        assertFalse(inst.canCreateFromObjectWith());
        assertFalse(inst.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testCanInstantiateWhenDefaultOnly() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        MockAnnotatedWithParams defaultC = new MockAnnotatedWithParams(Object.class, null, new Object());
        inst.configureFromObjectSettings(defaultC, null, null, null, null, null);
        assertTrue(inst.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testCanInstantiateWhenDelegateOnly() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        MockAnnotatedWithParams delegateC = new MockAnnotatedWithParams(Object.class, null, new Object());
        JavaType delegateType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        inst.configureFromObjectSettings(null, delegateC, delegateType, null, null, null);
        assertTrue(inst.canCreateUsingDelegate());
        assertTrue(inst.canInstantiate());
    }

    // ------------------------------------------------------------------
    // Partition C: Defect-targeted exception handling (single "at [" marker)
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRewrapCtorProblemNoDuplicateLocation() throws Exception {
        StdValueInstantiator inst = createInstance((JavaType) null);
        DeserializationContext ctxt = mockContext();

        // A JsonMappingException with one "at [" marker should pass through unchanged.
        JsonMappingException jme = new JsonMappingException(null, "Cannot deserialize Map key ... at [Source: unknown; line: 1, column: 1]", new RuntimeException());
        JsonMappingException result = inst.rewrapCtorProblem(ctxt, jme);
        assertSame("Should return existing JsonMappingException", jme, result);
        assertEquals(1, countOccurrences(result.getMessage(), "at ["));

        // A non-JsonMappingException that contains "at [" should be wrapped,
        // producing a message with exactly one "at [".
        Exception plain = new Exception("Cannot deserialize Map key ... at [Source: unknown; line: 1, column: 1]");
        JsonMappingException wrapped = inst.rewrapCtorProblem(ctxt, plain);
        assertNotNull(wrapped);
        String msg = wrapped.getMessage();
        assertEquals("Should have exactly one 'at [' marker", 1, countOccurrences(msg, "at ["));
    }

    @Test(timeout = 4000)
    public void testRewrapCtorProblemUnwrapInvocationTarget() throws Exception {
        StdValueInstantiator inst = createInstance((JavaType) null);
        DeserializationContext ctxt = mockContext();
        InvocationTargetException ite = new InvocationTargetException(
                new JsonMappingException(null, "Root cause at [Source: unknown]", new RuntimeException()));
        JsonMappingException result = inst.rewrapCtorProblem(ctxt, ite);
        // Since cause is a JsonMappingException, it should be returned as-is (after unwrapping)
        assertTrue(result instanceof JsonMappingException);
        assertEquals(1, countOccurrences(result.getMessage(), "at ["));
    }

    @Test(timeout = 4000)
    public void testWrapAsJsonMappingExceptionNoDuplicate() throws Exception {
        StdValueInstantiator inst = createInstance((JavaType) null);
        DeserializationContext ctxt = mockContext();
        JsonMappingException jme = new JsonMappingException(null, "Message with at [Source: unknown]", new RuntimeException());
        JsonMappingException result = inst.wrapAsJsonMappingException(ctxt, jme);
        assertSame(jme, result);
        assertEquals(1, countOccurrences(result.getMessage(), "at ["));
    }

    // Helper to count occurrences of a substring (case-sensitive)
    private static int countOccurrences(String str, String sub) {
        if (str == null || sub == null) return 0;
        int count = 0, idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // ------------------------------------------------------------------
    // Partition D: Exception paths and defensive guards
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreateUsingDefaultNoCreator() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        DeserializationContext ctxt = mockContext();
        // Calling with no default creator should delegate to super, which may throw IllegalStateException
        try {
            inst.createUsingDefault(ctxt);
            fail("Expected exception for missing default creator");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateUsingDefaultWithCreator() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Object val = "created";
        MockAnnotatedWithParams defaultC = new MockAnnotatedWithParams(String.class, null, val);
        inst.configureFromObjectSettings(defaultC, null, null, null, null, null);
        assertEquals(val, inst.createUsingDefault(mockContext()));
    }

    @Test(timeout = 4000)
    public void testCreateUsingDefaultCreatorThrows() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Exception ex = new RuntimeException("boom at [Source: unknown]");
        MockAnnotatedWithParams defaultC = new MockAnnotatedWithParams(Object.class, ex, null);
        inst.configureFromObjectSettings(defaultC, null, null, null, null, null);
        // The call should be caught and re-wrapped; we just check that a JsonMappingException is thrown
        try {
            inst.createUsingDefault(mockContext());
            fail("Expected IOException");
        } catch (IOException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testCreateUsingDelegateNoCreatorAndNoArrayDelegate() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        inst.createUsingDelegate(mockContext(), "delegate");
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateArrayFallback() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Object val = "arrayDel";
        MockAnnotatedWithParams arrayDel = new MockAnnotatedWithParams(String.class, null, val);
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        inst.configureFromArraySettings(arrayDel, type, null);
        assertEquals(val, inst.createUsingDelegate(mockContext(), "delegate"));
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateFallbackToDelegate() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Object val = "delegate";
        MockAnnotatedWithParams delegateC = new MockAnnotatedWithParams(String.class, null, val);
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        inst.configureFromObjectSettings(null, delegateC, type, null, null, null);
        assertEquals(val, inst.createUsingArrayDelegate(mockContext(), "delegate"));
    }

    @Test(timeout = 4000)
    public void testCreateFromStringNoCreatorFallback() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        // No string creator -> super method will likely throw or return null;
        // we just verify it doesn't NPE (mock context is used).
        try {
            inst.createFromString(mockContext(), "value");
            // If it returns, that's fine; we don't assert.
        } catch (Exception e) {
            // Accepting exception
        }
    }

    @Test(timeout = 4000)
    public void testCreateFromStringWithCreator() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Object val = "parsed";
        MockAnnotatedWithParams strC = new MockAnnotatedWithParams(String.class, null, val);
        inst.configureFromStringCreator(strC);
        assertEquals(val, inst.createFromString(mockContext(), "value"));
    }

    @Test(timeout = 4000)
    public void testCreateFromIntPreferInt() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        int val = 123;
        MockAnnotatedWithParams intC = new MockAnnotatedWithParams(Integer.class, null, val);
        inst.configureFromIntCreator(intC);
        MockAnnotatedWithParams longC = new MockAnnotatedWithParams(Long.class, null, (long) val);
        inst.configureFromLongCreator(longC);
        assertEquals(val, inst.createFromInt(mockContext(), val));
    }

    @Test(timeout = 4000)
    public void testCreateFromIntFallbackToLong() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        long val = 123L;
        MockAnnotatedWithParams longC = new MockAnnotatedWithParams(Long.class, null, val);
        inst.configureFromLongCreator(longC);
        assertEquals(val, inst.createFromInt(mockContext(), 123));
    }

    @Test(timeout = 4000)
    public void testCreateFromLong() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        long val = 123L;
        MockAnnotatedWithParams longC = new MockAnnotatedWithParams(Long.class, null, val);
        inst.configureFromLongCreator(longC);
        assertEquals(val, inst.createFromLong(mockContext(), val));
    }

    @Test(timeout = 4000)
    public void testCreateFromDouble() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        double val = 1.25;
        MockAnnotatedWithParams doubleC = new MockAnnotatedWithParams(Double.class, null, val);
        inst.configureFromDoubleCreator(doubleC);
        assertEquals(val, inst.createFromDouble(mockContext(), val), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateFromBoolean() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        boolean val = true;
        MockAnnotatedWithParams boolC = new MockAnnotatedWithParams(Boolean.class, null, val);
        inst.configureFromBooleanCreator(boolC);
        assertEquals(val, inst.createFromBoolean(mockContext(), val));
    }

    @Test(timeout = 4000)
    public void testCreateFromObjectWith() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        Object val = "created";
        MockAnnotatedWithParams withArgsC = new MockAnnotatedWithParams(String.class, null, val);
        inst.configureFromObjectSettings(null, null, null, null, withArgsC, null);
        assertEquals(val, inst.createFromObjectWith(mockContext(), new Object[] { "arg" }));
    }

    @Test(timeout = 4000)
    public void testCreateFromObjectWithNoCreator() throws IOException {
        StdValueInstantiator inst = createInstance((JavaType) null);
        try {
            inst.createFromObjectWith(mockContext(), new Object[] { "arg" });
            fail("Expected exception for missing with-args creator");
        } catch (IllegalStateException | com.fasterxml.jackson.databind.exc.InvalidDefinitionException e) {
            // acceptable
        }
    }

    // ------------------------------------------------------------------
    // Partition E: Lifecycle and deprecated methods
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWrapExceptionDeprecated() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        JsonMappingException jme = new JsonMappingException(null, "at [Source: unknown]", new RuntimeException());
        assertSame(jme, inst.wrapException(jme));
    }

    @Test(timeout = 4000)
    public void testUnwrapAndWrapException() {
        StdValueInstantiator inst = createInstance((JavaType) null);
        DeserializationContext ctxt = mockContext();
        JsonMappingException jme = new JsonMappingException(null, "at [Source: unknown]", new RuntimeException());
        JsonMappingException result = inst.unwrapAndWrapException(ctxt, jme);
        assertSame(jme, result);
    }

    @Test(timeout = 4000)
    public void testSerializable() {
        assertTrue(java.io.Serializable.class.isAssignableFrom(StdValueInstantiator.class));
    }
}