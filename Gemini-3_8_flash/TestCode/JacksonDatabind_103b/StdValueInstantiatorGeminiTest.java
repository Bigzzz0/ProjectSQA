package com.fasterxml.jackson.databind.deser.std;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
 *
 * 1. Defects4J Targeted Defect (BasicExceptionTest::testLocationAddition):
 *    - Problem: Wrapping already formatted JsonMappingException inside new JsonMappingException causes duplicated
 *      location markers ("at [...]").
 *    - Targeted Branches:
 *      * rewrapCtorProblem(ctxt, t): Must peel InvocationTargetException and ExceptionInInitializerError.
 *      * wrapAsJsonMappingException(ctxt, t): Must identify existing JsonMappingException and return directly
 *        without nesting.
 *
 * 2. Equivalence Partitions & Decision Branches:
 *    - Partition A (Core Logic & Instantiation Paths):
 *      * createUsingDefault: _defaultCreator present vs null (throws super) vs invocation failure.
 *      * createFromObjectWith: _withArgsCreator present vs null vs invocation failure.
 *      * createUsingDelegate: _delegateCreator present vs null fallback to _arrayDelegateCreator vs both null (IllegalStateException).
 *        delegateArguments null (call1) vs non-null (array handling with null delegate slots).
 *      * createUsingArrayDelegate: _arrayDelegateCreator present vs fallback to _delegateCreator vs both null.
 *      * createFromString: _fromStringCreator present vs null fallback.
 *      * createFromInt: _fromIntCreator present vs widening conversion to _fromLongCreator vs super fallback.
 *      * createFromLong: _fromLongCreator present vs super fallback.
 *      * createFromDouble: _fromDoubleCreator present vs super fallback.
 *      * createFromBoolean: _fromBooleanCreator present vs super fallback.
 *
 *    - Partition B (Boundary Value Analysis & Metadata):
 *      * Constructor handling with null Class<?> or null JavaType (desc == "UNKNOWN" / "UNKNOWN TYPE", rawClass == Object.class).
 *      * canInstantiate() truth-table across all 9 creator dimensions.
 *      * Getter / Mutator integrity: delegateType, arrayDelegateType, incompleteParameter, constructorsArguments.
 *
 *    - Partition C & D (Exception Handling & Defensive Paths):
 *      * wrapException(t): Unwraps cause hierarchy until JsonMappingException or wraps.
 *      * unwrapAndWrapException(ctxt, t): Unwraps to JsonMappingException or delegates to ctxt.instantiationException.
 *      * rewrapCtorProblem(ctxt, t): InvocationTargetException with null cause vs valid cause.
 *
 *    - Partition E (Lifecycle & Immutability/Copying):
 *      * Copy-constructor preserves all 14 internal references.
 */

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

public class StdValueInstantiatorGeminiTest {

    private ObjectMapper mapper;
    private DeserializationConfig config;
    private BeanDescription beanDesc;

    private AnnotatedConstructor defaultCtor;
    private AnnotatedConstructor stringCtor;
    private AnnotatedConstructor intCtor;
    private AnnotatedConstructor longCtor;
    private AnnotatedConstructor doubleCtor;
    private AnnotatedConstructor boolCtor;
    private AnnotatedConstructor twoArgCtor;
    private AnnotatedMethod factoryMethod;

    public static class TargetBean {
        final Object value;

        public TargetBean() {
            this.value = "default";
        }

        public TargetBean(String s) {
            if ("FAIL_STRING".equals(s)) {
                throw new IllegalArgumentException("Forced String Failure");
            }
            this.value = s;
        }

        public TargetBean(int i) {
            if (i < 0) {
                throw new IllegalArgumentException("Negative int");
            }
            this.value = i;
        }

        public TargetBean(long l) {
            if (l < 0) {
                throw new IllegalArgumentException("Negative long");
            }
            this.value = l;
        }

        public TargetBean(double d) {
            if (d < 0) {
                throw new IllegalArgumentException("Negative double");
            }
            this.value = d;
        }

        public TargetBean(boolean b) {
            this.value = b;
        }

        public TargetBean(String s, int i) {
            if (i < 0) {
                throw new IllegalArgumentException("Negative int in two-arg");
            }
            this.value = s + ":" + i;
        }

        public static TargetBean fromDelegate(Object o) {
            if ("FAIL_DELEGATE".equals(o)) {
                throw new IllegalArgumentException("Forced Delegate Failure");
            }
            return new TargetBean(String.valueOf(o));
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        beanDesc = config.introspect(mapper.constructType(TargetBean.class));

        for (AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            if (ctor.getParameterCount() == 0) {
                defaultCtor = ctor;
            } else if (ctor.getParameterCount() == 1) {
                Class<?> raw = ctor.getRawParameterType(0);
                if (raw == String.class) stringCtor = ctor;
                else if (raw == int.class) intCtor = ctor;
                else if (raw == long.class) longCtor = ctor;
                else if (raw == double.class) doubleCtor = ctor;
                else if (raw == boolean.class) boolCtor = ctor;
            } else if (ctor.getParameterCount() == 2) {
                twoArgCtor = ctor;
            }
        }

        for (AnnotatedMethod method : beanDesc.getFactoryMethods()) {
            if ("fromDelegate".equals(method.getName())) {
                factoryMethod = method;
            }
        }
    }

    private DeserializationContext createActiveContext() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();
        return mapper.getDeserializationContext().createInstance(config, parser, null);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateUsingDefaultSuccess() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(defaultCtor, null, null, null, null, null);

        assertTrue(inst.canCreateUsingDefault());
        assertTrue(inst.canInstantiate());
        Object obj = inst.createUsingDefault(ctxt);
        assertNotNull(obj);
        assertTrue(obj instanceof TargetBean);
        assertEquals("default", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testCreateFromObjectWithSuccess() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(null, null, null, null, twoArgCtor, new SettableBeanProperty[2]);

        assertTrue(inst.canCreateFromObjectWith());
        assertTrue(inst.canInstantiate());
        Object obj = inst.createFromObjectWith(ctxt, new Object[]{"val", 42});
        assertNotNull(obj);
        assertEquals("val:42", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateWithoutArgs() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        JavaType delType = mapper.constructType(Object.class);
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(null, factoryMethod, delType, null, null, null);

        assertTrue(inst.canCreateUsingDelegate());
        assertEquals(delType, inst.getDelegateType(config));
        Object obj = inst.createUsingDelegate(ctxt, "delegateVal");
        assertNotNull(obj);
        assertEquals("delegateVal", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateWithDelegateArgsArray() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        JavaType delType = mapper.constructType(Object.class);
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        // Single delegate slot (prop == null signifies the delegate argument itself)
        SettableBeanProperty[] delegateArgs = new SettableBeanProperty[]{ null };
        inst.configureFromObjectSettings(null, factoryMethod, delType, delegateArgs, null, null);

        Object obj = inst.createUsingDelegate(ctxt, "arrayPassedDelegate");
        assertNotNull(obj);
        assertEquals("arrayPassedDelegate", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateSuccess() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        JavaType arrType = mapper.constructType(Object[].class);
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromArraySettings(factoryMethod, arrType, null);

        assertTrue(inst.canCreateUsingArrayDelegate());
        assertEquals(arrType, inst.getArrayDelegateType(config));
        Object obj = inst.createUsingArrayDelegate(ctxt, "arrVal");
        assertNotNull(obj);
        assertEquals("arrVal", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testCreateFromScalarsSuccess() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        inst.configureFromStringCreator(stringCtor);
        inst.configureFromIntCreator(intCtor);
        inst.configureFromLongCreator(longCtor);
        inst.configureFromDoubleCreator(doubleCtor);
        inst.configureFromBooleanCreator(boolCtor);

        assertTrue(inst.canCreateFromString());
        assertTrue(inst.canCreateFromInt());
        assertTrue(inst.canCreateFromLong());
        assertTrue(inst.canCreateFromDouble());
        assertTrue(inst.canCreateFromBoolean());

        assertEquals("hello", ((TargetBean) inst.createFromString(ctxt, "hello")).value);
        assertEquals(10, ((TargetBean) inst.createFromInt(ctxt, 10)).value);
        assertEquals(100L, ((TargetBean) inst.createFromLong(ctxt, 100L)).value);
        assertEquals(3.14, (Double) ((TargetBean) inst.createFromDouble(ctxt, 3.14)).value, 0.001);
        assertEquals(Boolean.TRUE, ((TargetBean) inst.createFromBoolean(ctxt, true)).value);
    }

    @Test(timeout = 4000)
    public void testCreateFromIntWideningConversionToLong() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        // Only long creator configured, no int creator
        inst.configureFromLongCreator(longCtor);

        assertFalse(inst.canCreateFromInt());
        assertTrue(inst.canCreateFromLong());

        Object obj = inst.createFromInt(ctxt, 42);
        assertNotNull(obj);
        assertEquals(42L, ((TargetBean) obj).value);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsWithNullTypes() {
        StdValueInstantiator instClassNull = new StdValueInstantiator(config, (Class<?>) null);
        assertEquals(Object.class, instClassNull.getValueClass());
        assertEquals("UNKNOWN", instClassNull.getValueTypeDesc());

        StdValueInstantiator instTypeNull = new StdValueInstantiator(config, (JavaType) null);
        assertEquals(Object.class, instTypeNull.getValueClass());
        assertEquals("UNKNOWN TYPE", instTypeNull.getValueTypeDesc());
    }

    @Test(timeout = 4000)
    public void testCanInstantiateTruthMatrix() {
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        assertFalse(inst.canInstantiate());

        inst.configureFromStringCreator(stringCtor);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromIntCreator(intCtor);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromLongCreator(longCtor);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromDoubleCreator(doubleCtor);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromBooleanCreator(boolCtor);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(defaultCtor, null, null, null, null, null);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(null, factoryMethod, mapper.constructType(Object.class), null, null, null);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromArraySettings(factoryMethod, mapper.constructType(Object[].class), null);
        assertTrue(inst.canInstantiate());

        inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(null, null, null, null, twoArgCtor, null);
        assertTrue(inst.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testFallbackToDelegateWhenArrayDelegateCreatorIsNull() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        JavaType delType = mapper.constructType(Object.class);
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromObjectSettings(null, factoryMethod, delType, null, null, null);

        Object obj = inst.createUsingArrayDelegate(ctxt, "delegateFallback");
        assertNotNull(obj);
        assertEquals("delegateFallback", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testFallbackToArrayDelegateWhenDelegateCreatorIsNull() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        JavaType arrType = mapper.constructType(Object[].class);
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromArraySettings(factoryMethod, arrType, null);

        Object obj = inst.createUsingDelegate(ctxt, "arrayDelegateFallback");
        assertNotNull(obj);
        assertEquals("arrayDelegateFallback", ((TargetBean) obj).value);
    }

    @Test(timeout = 4000)
    public void testFallbacksWhenCreatorsUnset() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        try {
            inst.createUsingDefault(ctxt);
            fail("Expected JsonMappingException for default creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromObjectWith(ctxt, new Object[]{ "val" });
            fail("Expected JsonMappingException for withArgs creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromInt(ctxt, 1);
            fail("Expected JsonMappingException for int creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromLong(ctxt, 1L);
            fail("Expected JsonMappingException for long creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromDouble(ctxt, 1.0);
            fail("Expected JsonMappingException for double creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromBoolean(ctxt, true);
            fail("Expected JsonMappingException for boolean creator missing");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromString(ctxt, "non-boolean-text");
            fail("Expected JsonMappingException for string creator missing");
        } catch (JsonMappingException ignored) {}
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Location Addition / No Duplicate Wrapping)
    // =========================================================================

    /**
     * Targets Defects4J issue (BasicExceptionTest::testLocationAddition):
     * If an InvocationTargetException wraps an existing JsonMappingException,
     * rewrapCtorProblem must unwrap the ITE and return the original JsonMappingException
     * directly without creating a second layer of JsonMappingException, preventing duplicated 'at [' markers.
     */
    @Test(timeout = 4000)
    public void testDefectLocationAdditionAndNoDuplicateWrapping() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        JsonMappingException origJme = JsonMappingException.from(ctxt.getParser(), "Cannot deserialize value");
        InvocationTargetException ite = new InvocationTargetException(origJme);

        JsonMappingException processed = inst.rewrapCtorProblem(ctxt, ite);
        assertSame("Must peel InvocationTargetException and preserve the identical JsonMappingException instance",
                origJme, processed);

        ExceptionInInitializerError eiie = new ExceptionInInitializerError(origJme);
        JsonMappingException processedEiie = inst.rewrapCtorProblem(ctxt, eiie);
        assertSame("Must peel ExceptionInInitializerError and preserve the identical JsonMappingException instance",
                origJme, processedEiie);
    }

    @Test(timeout = 4000)
    public void testWrapExceptionUnwrapsNestedJsonMappingException() {
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        JsonMappingException rootJme = new JsonMappingException(null, "Root JME");
        RuntimeException outer1 = new RuntimeException("Outer 1", rootJme);
        RuntimeException outer2 = new RuntimeException("Outer 2", outer1);

        @SuppressWarnings("deprecation")
        JsonMappingException resolved = inst.wrapException(outer2);
        assertSame("wrapException must traverse cause chain and return root JsonMappingException", rootJme, resolved);
    }

    @Test(timeout = 4000)
    public void testWrapExceptionCreatesNewWhenNoJsonMappingExceptionInChain() {
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        IllegalArgumentException root = new IllegalArgumentException("Root cause error");

        @SuppressWarnings("deprecation")
        JsonMappingException resolved = inst.wrapException(root);
        assertNotNull(resolved);
        assertTrue(resolved.getMessage().contains("Instantiation of com.fasterxml.jackson.databind.deser.std.StdValueInstantiatorGeminiTest$TargetBean value failed"));
        assertSame(root, resolved.getCause());
    }

    @Test(timeout = 4000)
    public void testUnwrapAndWrapExceptionHandlesChains() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        JsonMappingException innerJme = new JsonMappingException(null, "Inner");
        Exception chain = new Exception("wrapper", innerJme);

        assertSame(innerJme, inst.unwrapAndWrapException(ctxt, chain));

        IOException ioEx = new IOException("Raw IO");
        JsonMappingException wrapped = inst.unwrapAndWrapException(ctxt, ioEx);
        assertNotNull(wrapped);
        assertSame(ioEx, wrapped.getCause());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateUsingDelegateThrowsIllegalStateExceptionWhenNoCreator() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        try {
            inst.createUsingDelegate(ctxt, "value");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("No delegate constructor for"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateThrowsIllegalStateExceptionWhenNoCreator() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        try {
            inst.createUsingArrayDelegate(ctxt, "value");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("No delegate constructor for"));
        }
    }

    @Test(timeout = 4000)
    public void testScalarCreatorsPropagateInstantiationProblems() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);
        inst.configureFromStringCreator(stringCtor);
        inst.configureFromIntCreator(intCtor);
        inst.configureFromLongCreator(longCtor);
        inst.configureFromDoubleCreator(doubleCtor);

        try {
            inst.createFromString(ctxt, "FAIL_STRING");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromInt(ctxt, -1);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromLong(ctxt, -1L);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException ignored) {}

        try {
            inst.createFromDouble(ctxt, -1.0);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException ignored) {}
    }

    @Test(timeout = 4000)
    public void testRewrapCtorProblemWithNullCause() throws Exception {
        DeserializationContext ctxt = createActiveContext();
        StdValueInstantiator inst = new StdValueInstantiator(config, TargetBean.class);

        InvocationTargetException iteNullCause = new InvocationTargetException(null);
        JsonMappingException jme = inst.rewrapCtorProblem(ctxt, iteNullCause);
        assertNotNull(jme);
        assertSame(iteNullCause, jme.getCause());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCopyConstructorAndAccessorsIntegrity() {
        JavaType delType = mapper.constructType(String.class);
        JavaType arrType = mapper.constructType(String[].class);
        SettableBeanProperty[] ctorArgs = new SettableBeanProperty[0];
        SettableBeanProperty[] delArgs = new SettableBeanProperty[0];
        SettableBeanProperty[] arrArgs = new SettableBeanProperty[0];
        AnnotatedParameter incompleteParam = stringCtor.getParameter(0);

        StdValueInstantiator src = new StdValueInstantiator(config, mapper.constructType(TargetBean.class));
        src.configureFromObjectSettings(defaultCtor, factoryMethod, delType, delArgs, twoArgCtor, ctorArgs);
        src.configureFromArraySettings(factoryMethod, arrType, arrArgs);
        src.configureFromStringCreator(stringCtor);
        src.configureFromIntCreator(intCtor);
        src.configureFromLongCreator(longCtor);
        src.configureFromDoubleCreator(doubleCtor);
        src.configureFromBooleanCreator(boolCtor);
        src.configureIncompleteParameter(incompleteParam);

        StdValueInstantiator copy = new StdValueInstantiator(src);

        assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        assertEquals(src.getValueClass(), copy.getValueClass());
        assertSame(src.getDefaultCreator(), copy.getDefaultCreator());
        assertSame(src.getWithArgsCreator(), copy.getWithArgsCreator());
        assertSame(src.getDelegateCreator(), copy.getDelegateCreator());
        assertSame(src.getDelegateType(config), copy.getDelegateType(config));
        assertSame(src.getArrayDelegateCreator(), copy.getArrayDelegateCreator());
        assertSame(src.getArrayDelegateType(config), copy.getArrayDelegateType(config));
        assertSame(src.getFromObjectArguments(config), copy.getFromObjectArguments(config));
        assertSame(src.getIncompleteParameter(), copy.getIncompleteParameter());

        assertTrue(copy.canCreateUsingDefault());
        assertTrue(copy.canCreateFromObjectWith());
        assertTrue(copy.canCreateUsingDelegate());
        assertTrue(copy.canCreateUsingArrayDelegate());
        assertTrue(copy.canCreateFromString());
        assertTrue(copy.canCreateFromInt());
        assertTrue(copy.canCreateFromLong());
        assertTrue(copy.canCreateFromDouble());
        assertTrue(copy.canCreateFromBoolean());
    }
}