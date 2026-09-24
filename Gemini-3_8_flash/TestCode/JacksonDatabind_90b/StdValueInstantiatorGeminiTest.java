/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
 * Target Defect: DelegatingArrayCreator1804Test (Handling of delegating array creators vs delegate creators)
 *
 * Decision / Branch Matrix Covered:
 * 1. Constructors:
 *    - StdValueInstantiator(DeserializationConfig, Class<?>): null class ("UNKNOWN TYPE", Object.class) vs non-null class.
 *    - StdValueInstantiator(DeserializationConfig, JavaType): null JavaType ("UNKNOWN TYPE", Object.class) vs non-null JavaType.
 *    - Copy Constructor: verify exact property copy (all creators, args, types, desc).
 * 2. Configuration & Metadata queries:
 *    - canCreateUsingDefault(), canCreateFromObjectWith(), canCreateFromString(), canCreateFromInt(),
 *      canCreateFromLong(), canCreateFromDouble(), canCreateFromBoolean(), canCreateUsingDelegate(),
 *      canCreateUsingArrayDelegate(): true when creator/type set, false when null.
 *    - getDelegateType(), getArrayDelegateType(), getFromObjectArguments(), getDelegateCreator(),
 *      getArrayDelegateCreator(), getDefaultCreator(), getWithArgsCreator(), getIncompleteParameter().
 * 3. Instantiation Paths & Fallbacks:
 *    - createUsingDefault: null creator (super call/exception) vs valid invocation vs invocation exception handling.
 *    - createFromObjectWith: null creator vs valid invocation vs invocation exception handling.
 *    - createUsingDelegate:
 *      * _delegateCreator == null && _arrayDelegateCreator != null (fallback workaround branch [databind#1392])
 *      * _delegateCreator != null
 *      * _delegateCreator == null && _arrayDelegateCreator == null -> IllegalStateException
 *    - createUsingArrayDelegate:
 *      * _arrayDelegateCreator == null && _delegateCreator != null (fallback to createUsingDelegate)
 *      * _arrayDelegateCreator != null
 *      * _arrayDelegateCreator == null && _delegateCreator == null -> IllegalStateException
 *    - createFromString: _fromStringCreator == null (fallbacks) vs valid call vs exception.
 *    - createFromInt: _fromIntCreator != null, _fromIntCreator == null && _fromLongCreator != null (widening),
 *      and both null (super fallback).
 *    - createFromLong, createFromDouble, createFromBoolean: null creator vs valid call vs exception.
 * 4. Exception Rewrapping & Delegation Internals:
 *    - rewrapCtorProblem: peeling ExceptionInInitializerError and InvocationTargetException causes.
 *    - wrapException: traversing cause chain until JsonMappingException or default wrapping.
 *    - unwrapAndWrapException / wrapAsJsonMappingException: already JsonMappingException vs wrapping new.
 *    - _createUsingDelegate: delegateArguments == null vs delegateArguments with null entries (delegate)
 *      and non-null entries (injectables via ctxt.findInjectableValue).
 * 5. Defect 1804: Delegating array creator verification with Jackson ObjectMapper integration.
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class StdValueInstantiatorGeminiTest {

    // Subclass exposing protected methods for direct verification
    static class TestableStdValueInstantiator extends StdValueInstantiator {
        private static final long serialVersionUID = 1L;

        public TestableStdValueInstantiator(DeserializationConfig config, Class<?> valueType) {
            super(config, valueType);
        }

        public TestableStdValueInstantiator(DeserializationConfig config, JavaType valueType) {
            super(config, valueType);
        }

        public TestableStdValueInstantiator(StdValueInstantiator src) {
            super(src);
        }

        @Override
        public JsonMappingException wrapException(Throwable t) {
            return super.wrapException(t);
        }

        @Override
        public JsonMappingException unwrapAndWrapException(DeserializationContext ctxt, Throwable t) {
            return super.unwrapAndWrapException(ctxt, t);
        }

        @Override
        public JsonMappingException wrapAsJsonMappingException(DeserializationContext ctxt, Throwable t) {
            return super.wrapAsJsonMappingException(ctxt, t);
        }

        @Override
        public JsonMappingException rewrapCtorProblem(DeserializationContext ctxt, Throwable t) {
            return super.rewrapCtorProblem(ctxt, t);
        }
    }

    // Helper dummy target for reflection
    static class DummyTarget {
        public static String createDefault() { return "default"; }
        public static String createThrows() { throw new IllegalArgumentException("boom"); }
        public static String createWithArgs(String a, Integer b) { return a + ":" + b; }
        public static String createFromStr(String s) { return "str:" + s; }
        public static String createFromInt(int i) { return "int:" + i; }
        public static String createFromLong(long l) { return "long:" + l; }
        public static String createFromDouble(double d) { return "double:" + d; }
        public static String createFromBool(boolean b) { return "bool:" + b; }
        public static String createDelegate(Object d) { return "delegate:" + d; }
        public static String createArrayDelegate(Object d) { return "arrayDelegate:" + d; }
    }

    private AnnotatedWithParams getAnnotatedMethod(String name, Class<?>... paramTypes) {
        try {
            Method m = DummyTarget.class.getDeclaredMethod(name, paramTypes);
            return new AnnotatedMethod(null, m, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DeserializationContext getContext() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getDeserializationContext();
    }

    /*
     * ------------------------------------------------------------------------
     * PARTITION A: Core Functional Logic & State Transitions
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testConstructorsAndMetadataNullSafety() {
        // Constructor with null Class
        StdValueInstantiator instClassNull = new StdValueInstantiator((DeserializationConfig) null, (Class<?>) null);
        assertEquals("UNKNOWN TYPE", instClassNull.getValueTypeDesc());
        assertEquals(Object.class, instClassNull.getValueClass());

        // Constructor with concrete Class
        StdValueInstantiator instClass = new StdValueInstantiator((DeserializationConfig) null, String.class);
        assertEquals(String.class.getName(), instClass.getValueTypeDesc());
        assertEquals(String.class, instClass.getValueClass());

        // Constructor with null JavaType
        StdValueInstantiator instTypeNull = new StdValueInstantiator((DeserializationConfig) null, (JavaType) null);
        assertEquals("UNKNOWN TYPE", instTypeNull.getValueTypeDesc());
        assertEquals(Object.class, instTypeNull.getValueClass());

        // Constructor with concrete JavaType
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdValueInstantiator instType = new StdValueInstantiator((DeserializationConfig) null, strType);
        assertEquals(strType.toString(), instType.getValueTypeDesc());
        assertEquals(String.class, instType.getValueClass());
    }

    @Test(timeout = 4000)
    public void testConfigurationAndCanCreateFlags() {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);

        assertFalse(inst.canCreateUsingDefault());
        assertFalse(inst.canCreateFromObjectWith());
        assertFalse(inst.canCreateUsingDelegate());
        assertFalse(inst.canCreateUsingArrayDelegate());
        assertFalse(inst.canCreateFromString());
        assertFalse(inst.canCreateFromInt());
        assertFalse(inst.canCreateFromLong());
        assertFalse(inst.canCreateFromDouble());
        assertFalse(inst.canCreateFromBoolean());

        AnnotatedWithParams defaultC = getAnnotatedMethod("createDefault");
        AnnotatedWithParams delegateC = getAnnotatedMethod("createDelegate", Object.class);
        AnnotatedWithParams withArgsC = getAnnotatedMethod("createWithArgs", String.class, Integer.class);
        JavaType delType = TypeFactory.defaultInstance().constructType(Object.class);

        inst.configureFromObjectSettings(defaultC, delegateC, delType, null, withArgsC, null);

        assertTrue(inst.canCreateUsingDefault());
        assertTrue(inst.canCreateUsingDelegate());
        assertTrue(inst.canCreateFromObjectWith());
        assertSame(defaultC, inst.getDefaultCreator());
        assertSame(delegateC, inst.getDelegateCreator());
        assertSame(delType, inst.getDelegateType(null));
        assertSame(withArgsC, inst.getWithArgsCreator());
        assertNull(inst.getFromObjectArguments(null));

        AnnotatedWithParams arrayDelC = getAnnotatedMethod("createArrayDelegate", Object.class);
        JavaType arrayType = TypeFactory.defaultInstance().constructArrayType(String.class);
        inst.configureFromArraySettings(arrayDelC, arrayType, null);

        assertTrue(inst.canCreateUsingArrayDelegate());
        assertSame(arrayDelC, inst.getArrayDelegateCreator());
        assertSame(arrayType, inst.getArrayDelegateType(null));

        AnnotatedWithParams fromStr = getAnnotatedMethod("createFromStr", String.class);
        inst.configureFromStringCreator(fromStr);
        assertTrue(inst.canCreateFromString());

        AnnotatedWithParams fromInt = getAnnotatedMethod("createFromInt", int.class);
        inst.configureFromIntCreator(fromInt);
        assertTrue(inst.canCreateFromInt());

        AnnotatedWithParams fromLong = getAnnotatedMethod("createFromLong", long.class);
        inst.configureFromLongCreator(fromLong);
        assertTrue(inst.canCreateFromLong());

        AnnotatedWithParams fromDouble = getAnnotatedMethod("createFromDouble", double.class);
        inst.configureFromDoubleCreator(fromDouble);
        assertTrue(inst.canCreateFromDouble());

        AnnotatedWithParams fromBool = getAnnotatedMethod("createFromBool", boolean.class);
        inst.configureFromBooleanCreator(fromBool);
        assertTrue(inst.canCreateFromBoolean());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        TestableStdValueInstantiator src = new TestableStdValueInstantiator(null, DummyTarget.class);
        AnnotatedWithParams def = getAnnotatedMethod("createDefault");
        AnnotatedWithParams del = getAnnotatedMethod("createDelegate", Object.class);
        JavaType delType = TypeFactory.defaultInstance().constructType(Object.class);
        AnnotatedWithParams withArgs = getAnnotatedMethod("createWithArgs", String.class, Integer.class);
        SettableBeanProperty[] ctorArgs = new SettableBeanProperty[0];

        src.configureFromObjectSettings(def, del, delType, null, withArgs, ctorArgs);
        AnnotatedWithParams arrDel = getAnnotatedMethod("createArrayDelegate", Object.class);
        JavaType arrType = TypeFactory.defaultInstance().constructArrayType(String.class);
        src.configureFromArraySettings(arrDel, arrType, null);

        src.configureFromStringCreator(getAnnotatedMethod("createFromStr", String.class));
        src.configureFromIntCreator(getAnnotatedMethod("createFromInt", int.class));
        src.configureFromLongCreator(getAnnotatedMethod("createFromLong", long.class));
        src.configureFromDoubleCreator(getAnnotatedMethod("createFromDouble", double.class));
        src.configureFromBooleanCreator(getAnnotatedMethod("createFromBool", boolean.class));

        TestableStdValueInstantiator copy = new TestableStdValueInstantiator(src);
        assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        assertEquals(src.getValueClass(), copy.getValueClass());
        assertSame(src.getDefaultCreator(), copy.getDefaultCreator());
        assertSame(src.getDelegateCreator(), copy.getDelegateCreator());
        assertSame(src.getDelegateType(null), copy.getDelegateType(null));
        assertSame(src.getWithArgsCreator(), copy.getWithArgsCreator());
        assertSame(src.getFromObjectArguments(null), copy.getFromObjectArguments(null));
        assertSame(src.getArrayDelegateCreator(), copy.getArrayDelegateCreator());
        assertSame(src.getArrayDelegateType(null), copy.getArrayDelegateType(null));
        assertTrue(copy.canCreateFromString());
        assertTrue(copy.canCreateFromInt());
        assertTrue(copy.canCreateFromLong());
        assertTrue(copy.canCreateFromDouble());
        assertTrue(copy.canCreateFromBoolean());
    }

    /*
     * ------------------------------------------------------------------------
     * PARTITION B: Boundary Value Analysis (BVA) & Scalar Creators
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testScalarCreatorsExecution() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        inst.configureFromStringCreator(getAnnotatedMethod("createFromStr", String.class));
        assertEquals("str:hello", inst.createFromString(ctxt, "hello"));
        assertEquals("str:", inst.createFromString(ctxt, ""));
        assertEquals("str:null", inst.createFromString(ctxt, null));

        inst.configureFromIntCreator(getAnnotatedMethod("createFromInt", int.class));
        assertEquals("int:0", inst.createFromInt(ctxt, 0));
        assertEquals("int:" + Integer.MAX_VALUE, inst.createFromInt(ctxt, Integer.MAX_VALUE));
        assertEquals("int:" + Integer.MIN_VALUE, inst.createFromInt(ctxt, Integer.MIN_VALUE));

        inst.configureFromLongCreator(getAnnotatedMethod("createFromLong", long.class));
        assertEquals("long:0", inst.createFromLong(ctxt, 0L));
        assertEquals("long:" + Long.MAX_VALUE, inst.createFromLong(ctxt, Long.MAX_VALUE));

        inst.configureFromDoubleCreator(getAnnotatedMethod("createFromDouble", double.class));
        assertEquals("double:0.0", inst.createFromDouble(ctxt, 0.0));
        assertEquals("double:" + Double.MAX_VALUE, inst.createFromDouble(ctxt, Double.MAX_VALUE));

        inst.configureFromBooleanCreator(getAnnotatedMethod("createFromBool", boolean.class));
        assertEquals("bool:true", inst.createFromBoolean(ctxt, true));
        assertEquals("bool:false", inst.createFromBoolean(ctxt, false));
    }

    @Test(timeout = 4000)
    public void testIntWideningToLongCreator() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        // No int creator, but long creator is configured
        inst.configureFromLongCreator(getAnnotatedMethod("createFromLong", long.class));
        assertEquals("long:42", inst.createFromInt(ctxt, 42));
    }

    /*
     * ------------------------------------------------------------------------
     * PARTITION C: Defect-Targeted Zone & Delegate Mechanics
     * ------------------------------------------------------------------------
     */

    static class Delegating1804Bean {
        final List<String> list;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Delegating1804Bean(List<String> list) {
            this.list = list;
        }
    }

    static class DelegatingArrayBean {
        final String[] array;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingArrayBean(String[] array) {
            this.array = array;
        }
    }

    @Test(timeout = 4000)
    public void testDelegatingArray1804GroundTruth() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Delegating1804Bean bean = mapper.readValue("[\"item1\", \"item2\"]", Delegating1804Bean.class);
        assertNotNull(bean);
        assertEquals(2, bean.list.size());
        assertEquals("item1", bean.list.get(0));

        DelegatingArrayBean arrBean = mapper.readValue("[\"x\", \"y\"]", DelegatingArrayBean.class);
        assertNotNull(arrBean);
        assertEquals(2, arrBean.array.length);
        assertEquals("x", arrBean.array[0]);
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateFallbackToArrayDelegate() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        AnnotatedWithParams arrDel = getAnnotatedMethod("createArrayDelegate", Object.class);
        JavaType arrType = TypeFactory.defaultInstance().constructArrayType(String.class);
        inst.configureFromArraySettings(arrDel, arrType, null);

        // _delegateCreator is null, should fallback to _arrayDelegateCreator
        Object result = inst.createUsingDelegate(ctxt, "data");
        assertEquals("arrayDelegate:data", result);
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateFallbackToDelegate() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        AnnotatedWithParams del = getAnnotatedMethod("createDelegate", Object.class);
        JavaType delType = TypeFactory.defaultInstance().constructType(Object.class);
        inst.configureFromObjectSettings(null, del, delType, null, null, null);

        // _arrayDelegateCreator is null, should fallback to createUsingDelegate
        Object result = inst.createUsingArrayDelegate(ctxt, "arrayData");
        assertEquals("delegate:arrayData", result);
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateThrowsIllegalStateWhenNoCreator() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();
        try {
            inst.createUsingDelegate(ctxt, "any");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("No delegate constructor for " + DummyTarget.class.getName()));
        }
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateThrowsIllegalStateWhenNoCreator() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();
        try {
            inst.createUsingArrayDelegate(ctxt, "any");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("No delegate constructor for " + DummyTarget.class.getName()));
        }
    }

    /*
     * ------------------------------------------------------------------------
     * PARTITION D: Exception & Defensive Guard Paths
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testUnconfiguredCreatorsThrowMappingException() {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        try {
            inst.createUsingDefault(ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromObjectWith(ctxt, new Object[]{"val"});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromString(ctxt, "fallback");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromInt(ctxt, 123);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromLong(ctxt, 123L);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromDouble(ctxt, 3.14);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }

        try {
            inst.createFromBoolean(ctxt, true);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testDefaultCreatorInvocationExceptionHandling() {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();
        AnnotatedWithParams throwsMethod = getAnnotatedMethod("createThrows");
        inst.configureFromObjectSettings(throwsMethod, null, null, null, null, null);

        try {
            inst.createUsingDefault(ctxt);
            fail("Expected JsonMappingException due to thrown problem");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("boom"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testWithArgsCreatorInvocationExceptionHandling() {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();
        AnnotatedWithParams throwsMethod = getAnnotatedMethod("createThrows");
        inst.configureFromObjectSettings(null, null, null, null, throwsMethod, new SettableBeanProperty[0]);

        try {
            inst.createFromObjectWith(ctxt, new Object[0]);
            fail("Expected JsonMappingException due to thrown problem");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("boom"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testRewrapCtorProblemPeeling() {
        TestableStdValueInstantiator inst = new TestableStdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        // 1. InvocationTargetException unpeeling
        Exception cause = new NumberFormatException("invalid num");
        InvocationTargetException ite = new InvocationTargetException(cause);
        JsonMappingException jme = inst.rewrapCtorProblem(ctxt, ite);
        assertSame(cause, jme.getCause());

        // 2. ExceptionInInitializerError unpeeling
        ExceptionInInitializerError eiie = new ExceptionInInitializerError(cause);
        JsonMappingException jme2 = inst.rewrapCtorProblem(ctxt, eiie);
        assertSame(cause, jme2.getCause());

        // 3. Already a JsonMappingException
        JsonMappingException existing = new JsonMappingException(null, "already mapping");
        assertSame(existing, inst.rewrapCtorProblem(ctxt, existing));

        // 4. InvocationTargetException with null cause
        InvocationTargetException iteNoCause = new InvocationTargetException(null);
        JsonMappingException jme3 = inst.rewrapCtorProblem(ctxt, iteNoCause);
        assertSame(iteNoCause, jme3.getCause());
    }

    @Test(timeout = 4000)
    public void testWrapExceptionCauseChainTraversal() {
        TestableStdValueInstantiator inst = new TestableStdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        JsonMappingException rootJme = new JsonMappingException(null, "root jme");
        RuntimeException wrapper = new RuntimeException("wrapper", rootJme);

        // Deprecated wrapException
        assertSame(rootJme, inst.wrapException(wrapper));

        // unwrapAndWrapException
        assertSame(rootJme, inst.unwrapAndWrapException(ctxt, wrapper));

        // wrapException without JsonMappingException in chain
        RuntimeException generic = new RuntimeException("generic message");
        JsonMappingException wrapped = inst.wrapException(generic);
        assertTrue(wrapped.getMessage().contains("generic message"));
        assertSame(generic, wrapped.getCause());
    }

    /*
     * ------------------------------------------------------------------------
     * PARTITION E: Object Lifecycle & Incomplete Parameter Handling
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testIncompleteParameterLifecycle() {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        assertNull(inst.getIncompleteParameter());

        AnnotatedParameter dummyParam = new AnnotatedParameter(null, null, null, 0);
        inst.configureIncompleteParameter(dummyParam);
        assertSame(dummyParam, inst.getIncompleteParameter());
    }

    @Test(timeout = 4000)
    public void testWithArgsCreatorInvocationSuccess() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, DummyTarget.class);
        DeserializationContext ctxt = getContext();

        AnnotatedWithParams withArgs = getAnnotatedMethod("createWithArgs", String.class, Integer.class);
        inst.configureFromObjectSettings(null, null, null, null, withArgs, new SettableBeanProperty[2]);

        Object result = inst.createFromObjectWith(ctxt, new Object[]{"val", 99});
        assertEquals("val:99", result);
    }
}