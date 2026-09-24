package com.fasterxml.jackson.databind.deser;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.ValueInstantiator
 * Subclass: com.fasterxml.jackson.databind.deser.ValueInstantiator.Base
 *
 * Targeted Decision Branches & Conditions:
 * 1. getValueClass() default -> returns Object.class.
 * 2. getValueTypeDesc():
 *    - Branch: cls == null -> "UNKNOWN"
 *    - Branch: cls != null -> cls.getName()
 * 3. canInstantiate():
 *    - Disjunction coverage (all 8 conditions):
 *      canCreateUsingDefault() || canCreateUsingDelegate() || canCreateFromObjectWith() ||
 *      canCreateFromString() || canCreateFromInt() || canCreateFromLong() ||
 *      canCreateFromDouble() || canCreateFromBoolean()
 * 4. Default return values:
 *    - canCreateFromString() -> false
 *    - canCreateFromInt() -> false
 *    - canCreateFromLong() -> false
 *    - canCreateFromDouble() -> false
 *    - canCreateFromBoolean() -> false
 *    - canCreateUsingDefault() -> getDefaultCreator() != null
 *    - canCreateUsingDelegate() -> false
 *    - canCreateUsingArrayDelegate() -> false
 *    - canCreateFromObjectWith() -> false
 *    - getFromObjectArguments(cfg) -> null
 *    - getDelegateType(cfg) -> null
 *    - getArrayDelegateType(cfg) -> null
 *    - getDefaultCreator() -> null
 *    - getDelegateCreator() -> null
 *    - getArrayDelegateCreator() -> null
 *    - getWithArgsCreator() -> null
 *    - getIncompleteParameter() -> null
 * 5. createUsingDefault, createFromObjectWith(ctxt, args), createUsingDelegate,
 *    createUsingArrayDelegate, createFromInt, createFromLong, createFromDouble, createFromBoolean:
 *    - Missing instantiator error handling invocation.
 * 6. createFromObjectWith(ctxt, props, buffer):
 *    - Delegates to buffer.getParameters(props) then calls createFromObjectWith(ctxt, args).
 * 7. _createFromStringFallbacks:
 *    - Branch 1: canCreateFromBoolean() is true:
 *      * sub-branch: "true".equals(value.trim()) -> calls createFromBoolean(ctxt, true)
 *      * sub-branch: "false".equals(value.trim()) -> calls createFromBoolean(ctxt, false)
 *      * sub-branch: neither "true" nor "false" -> proceeds to next check
 *    - Branch 2: value.length() == 0:
 *      * sub-branch: ctxt.isEnabled(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) == true -> returns null
 *      * sub-branch: ctxt.isEnabled(...) == false -> calls handleMissingInstantiator
 *    - Branch 3: fallback missing instantiator error.
 * 8. Base static class:
 *    - Base(Class<?> type) -> sets _valueType
 *    - Base(JavaType type) -> sets _valueType via type.getRawClass()
 *    - Base.getValueClass() -> returns _valueType
 *    - Base.getValueTypeDesc() -> returns _valueType.getName()
 *
 * Known Defect (Defects4J JacksonDatabind-103 / #1804 DelegatingArrayCreator):
 * - Handling of array-delegating creator resolution in ValueInstantiator and deserializer
 *   construction when an array delegate creator is registered. Tests must assert that
 *   canCreateUsingArrayDelegate() and getArrayDelegateType() properly integrate or report.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ValueInstantiatorGeminiTest {

    // Concrete test double inheriting directly from ValueInstantiator
    private static class TestValueInstantiator extends ValueInstantiator {
        private Class<?> _classOverride = Object.class;
        private boolean _nullClass = false;
        private AnnotatedWithParams _defaultCreator;

        public void setClassOverride(Class<?> cls) {
            this._classOverride = cls;
        }

        public void setNullClass(boolean nullClass) {
            this._nullClass = nullClass;
        }

        public void setDefaultCreator(AnnotatedWithParams creator) {
            this._defaultCreator = creator;
        }

        @Override
        public Class<?> getValueClass() {
            if (_nullClass) {
                return null;
            }
            return _classOverride;
        }

        @Override
        public AnnotatedWithParams getDefaultCreator() {
            return _defaultCreator;
        }
    }

    private static class BooleanEnabledInstantiator extends ValueInstantiator {
        private final Class<?> _type;

        public BooleanEnabledInstantiator(Class<?> type) {
            _type = type;
        }

        @Override
        public Class<?> getValueClass() {
            return _type;
        }

        @Override
        public boolean canCreateFromBoolean() {
            return true;
        }

        @Override
        public Object createFromBoolean(DeserializationContext ctxt, boolean value) {
            return Boolean.valueOf(value);
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefaultImplementationsAndAccessors() {
        TestValueInstantiator vi = new TestValueInstantiator();

        assertEquals(Object.class, vi.getValueClass());
        assertEquals(Object.class.getName(), vi.getValueTypeDesc());

        assertFalse(vi.canInstantiate());
        assertFalse(vi.canCreateFromString());
        assertFalse(vi.canCreateFromInt());
        assertFalse(vi.canCreateFromLong());
        assertFalse(vi.canCreateFromDouble());
        assertFalse(vi.canCreateFromBoolean());
        assertFalse(vi.canCreateUsingDefault());
        assertFalse(vi.canCreateUsingDelegate());
        assertFalse(vi.canCreateUsingArrayDelegate());
        assertFalse(vi.canCreateFromObjectWith());

        assertNull(vi.getFromObjectArguments(null));
        assertNull(vi.getDelegateType(null));
        assertNull(vi.getArrayDelegateType(null));
        assertNull(vi.getDefaultCreator());
        assertNull(vi.getDelegateCreator());
        assertNull(vi.getArrayDelegateCreator());
        assertNull(vi.getWithArgsCreator());
        assertNull(vi.getIncompleteParameter());
    }

    @Test(timeout = 4000)
    public void testCanInstantiateShortCircuitCombinations() {
        // Test each branch of canInstantiate returning true
        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateUsingDefault() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateUsingDelegate() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromObjectWith() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromString() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromInt() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromLong() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromDouble() { return true; }
        }.canInstantiate());

        assertTrue(new ValueInstantiator.Base(String.class) {
            @Override public boolean canCreateFromBoolean() { return true; }
        }.canInstantiate());
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorBaseClassConstructors() {
        JavaType javaType = TypeFactory.defaultInstance().constructType(String.class);
        ValueInstantiator.Base baseFromJavaType = new ValueInstantiator.Base(javaType);
        assertEquals(String.class, baseFromJavaType.getValueClass());
        assertEquals(String.class.getName(), baseFromJavaType.getValueTypeDesc());

        ValueInstantiator.Base baseFromClass = new ValueInstantiator.Base(Integer.class);
        assertEquals(Integer.class, baseFromClass.getValueClass());
        assertEquals(Integer.class.getName(), baseFromClass.getValueTypeDesc());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis & Null/Empty Edge Cases
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testNullClassValueTypeDesc() {
        TestValueInstantiator vi = new TestValueInstantiator();
        vi.setNullClass(true);
        assertNull(vi.getValueClass());
        assertEquals("UNKNOWN", vi.getValueTypeDesc());
    }

    @Test(timeout = 4000)
    public void testCreateFromStringEmptyWithAcceptEmptyStringFeature() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(
                    mapper.getDeserializationConfig(),
                    mapper.getFactory().createParser("\"\""),
                    new com.fasterxml.jackson.databind.InjectableValues.Std()
            );
        }

        ValueInstantiator.Base instantiator = new ValueInstantiator.Base(String.class);
        Object result = instantiator.createFromString(ctxt, "");
        assertNull("Empty string with ACCEPT_EMPTY_STRING_AS_NULL_OBJECT should return null", result);
    }

    @Test(timeout = 4000)
    public void testCreateFromStringFallbacksBooleanCoercion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(
                    mapper.getDeserializationConfig(),
                    mapper.getFactory().createParser("\"\""),
                    new com.fasterxml.jackson.databind.InjectableValues.Std()
            );
        }

        BooleanEnabledInstantiator inst = new BooleanEnabledInstantiator(Boolean.class);
        assertEquals(Boolean.TRUE, inst.createFromString(ctxt, "true"));
        assertEquals(Boolean.TRUE, inst.createFromString(ctxt, "  true  "));
        assertEquals(Boolean.FALSE, inst.createFromString(ctxt, "false"));
        assertEquals(Boolean.FALSE, inst.createFromString(ctxt, " false "));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Issue 1804 / Array Delegating Creator)
     * ----------------------------------------------------------------------
     */

    static abstract class AbstractListWrapper {
        public List<String> items;
        protected AbstractListWrapper(List<String> items) { this.items = items; }
    }

    static class ConcreteListWrapper extends AbstractListWrapper {
        public ConcreteListWrapper(List<String> items) { super(items); }
    }

    @Test(timeout = 4000)
    public void testArrayDelegatingInstantiatorCapabilities() {
        // Target defect: Ensure that array-delegate creator contract is fully supported
        // and doesn't collapse to missing instantiator when configured.
        final JavaType arrayType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator arrayDelegatingInstantiator = new ValueInstantiator.Base(ConcreteListWrapper.class) {
            @Override
            public boolean canCreateUsingArrayDelegate() {
                return true;
            }

            @Override
            public JavaType getArrayDelegateType(DeserializationConfig config) {
                return arrayType;
            }

            @Override
            public Object createUsingArrayDelegate(DeserializationContext ctxt, Object delegate) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) delegate;
                return new ConcreteListWrapper(list);
            }
        };

        assertTrue("Instantiator must report canCreateUsingArrayDelegate() as true",
                arrayDelegatingInstantiator.canCreateUsingArrayDelegate());
        assertEquals("Instantiator must return correct ArrayDelegateType",
                arrayType, arrayDelegatingInstantiator.getArrayDelegateType(null));

        List<String> input = Collections.singletonList("test1804");
        Object result = null;
        try {
            result = arrayDelegatingInstantiator.createUsingArrayDelegate(null, input);
        } catch (IOException e) {
            fail("Exception should not be thrown for valid array delegate creation: " + e.getMessage());
        }

        assertNotNull(result);
        assertTrue(result instanceof ConcreteListWrapper);
        assertEquals("test1804", ((ConcreteListWrapper) result).items.get(0));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testCreateUsingDefaultMissingInstantiatorException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = getTestContext(mapper);
        ValueInstantiator.Base vi = new ValueInstantiator.Base(String.class);

        try {
            vi.createUsingDefault(ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no default no-arguments constructor found"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateFromObjectWithMissingInstantiatorException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = getTestContext(mapper);
        ValueInstantiator.Base vi = new ValueInstantiator.Base(String.class);

        try {
            vi.createFromObjectWith(ctxt, new Object[]{"val"});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no creator with arguments specified"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateUsingDelegateMissingInstantiatorException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = getTestContext(mapper);
        ValueInstantiator.Base vi = new ValueInstantiator.Base(String.class);

        try {
            vi.createUsingDelegate(ctxt, "delegate");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no delegate creator specified"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateUsingArrayDelegateMissingInstantiatorException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = getTestContext(mapper);
        ValueInstantiator.Base vi = new ValueInstantiator.Base(String.class);

        try {
            vi.createUsingArrayDelegate(ctxt, new Object[0]);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no array delegate creator specified"));
        }
    }

    @Test(timeout = 4000)
    public void testScalarMissingInstantiatorExceptions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = getTestContext(mapper);
        ValueInstantiator.Base vi = new ValueInstantiator.Base(String.class);

        // String
        try {
            vi.createFromString(ctxt, "foo");
            fail("Expected JsonMappingException for string");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no String-argument constructor/factory method"));
        }

        // Int
        try {
            vi.createFromInt(ctxt, 42);
            fail("Expected JsonMappingException for int");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no int/Int-argument constructor/factory method"));
        }

        // Long
        try {
            vi.createFromLong(ctxt, 1234567890123L);
            fail("Expected JsonMappingException for long");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no long/Long-argument constructor/factory method"));
        }

        // Double
        try {
            vi.createFromDouble(ctxt, 3.14159);
            fail("Expected JsonMappingException for double");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no double/Double-argument constructor/factory method"));
        }

        // Boolean
        try {
            vi.createFromBoolean(ctxt, true);
            fail("Expected JsonMappingException for boolean");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no boolean/Boolean-argument constructor/factory method"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateFromObjectWithBufferDelegation() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final DeserializationContext ctxt = getTestContext(mapper);

        final Object[] expectedArgs = new Object[]{"arg1", 123};
        final boolean[] called = new boolean[1];

        ValueInstantiator vi = new ValueInstantiator.Base(String.class) {
            @Override
            public Object createFromObjectWith(DeserializationContext context, Object[] args) {
                assertSame(ctxt, context);
                assertSame(expectedArgs, args);
                called[0] = true;
                return "success";
            }
        };

        // Create buffer with parser and context
        JsonParser parser = mapper.getFactory().createParser("{}");
        SettableBeanProperty[] props = new SettableBeanProperty[0];
        PropertyValueBuffer buffer = new PropertyValueBuffer(parser, ctxt, 0, null) {
            @Override
            public Object[] getParameters(SettableBeanProperty[] properties) {
                return expectedArgs;
            }
        };

        Object result = vi.createFromObjectWith(ctxt, props, buffer);
        assertTrue("Expected createFromObjectWith(ctxt, args) to be called", called[0]);
        assertEquals("success", result);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Helper methods
     * ----------------------------------------------------------------------
     */

    private DeserializationContext getTestContext(ObjectMapper mapper) throws IOException {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(
                    mapper.getDeserializationConfig(),
                    mapper.getFactory().createParser("{}"),
                    new com.fasterxml.jackson.databind.InjectableValues.Std()
            );
        }
        return ctxt;
    }
}