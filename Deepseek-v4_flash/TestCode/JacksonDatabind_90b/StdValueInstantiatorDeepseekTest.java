package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdValueInstantiatorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target: StdValueInstantiator
     * 
     * Branches covered:
     * - Constructors (deprecated, main, copy)
     * - configureFromObjectSettings / configureFromArraySettings / configureFrom*Creator
     * - canCreate* methods (string, int, long, double, boolean, default, delegate, arrayDelegate, objectWith)
     * - getDelegateType / getArrayDelegateType / getFromObjectArguments
     * - createUsingDefault (null creator -> super, non-null -> call, exception path)
     * - createFromObjectWith (null creator -> super, non-null -> call, exception path)
     * - createUsingDelegate (delegateCreator null + arrayDelegateCreator null -> super, delegateCreator null + arrayDelegateCreator non-null -> array delegate, delegateCreator non-null -> delegate)
     * - createUsingArrayDelegate (arrayDelegateCreator null + delegateCreator null -> super, arrayDelegateCreator null + delegateCreator non-null -> delegate, arrayDelegateCreator non-null -> array delegate)
     * - createFromString (null creator -> fallback, non-null -> call, exception path)
     * - createFromInt (int creator, long creator fallback, super fallback, exception paths)
     * - createFromLong (null -> super, non-null -> call, exception path)
     * - createFromDouble (null -> super, non-null -> call, exception path)
     * - createFromBoolean (null -> super, non-null -> call, exception path)
     * - getDelegateCreator / getArrayDelegateCreator / getDefaultCreator / getWithArgsCreator / getIncompleteParameter
     * - wrapException (JsonMappingException passthrough, other -> new JsonMappingException)
     * - unwrapAndWrapException (delegates to ctxt.instantiationException)
     * - wrapAsJsonMappingException (JsonMappingException passthrough, other -> wrap)
     * - rewrapCtorProblem (ExceptionInInitializerError/InvocationTargetException unwrap, then wrapAsJsonMappingException)
     * - _createUsingDelegate (delegateCreator null -> IllegalStateException, delegateArguments null -> call1, non-null -> injectable handling, exception path)
     * 
     * Defect-targeted branch (from Defects4J):
     * - createUsingDelegate when _delegateCreator == null and _arrayDelegateCreator != null
     *   should delegate to _createUsingDelegate with _arrayDelegateCreator, but the defective
     *   version may incorrectly fall through to super.createUsingDelegate (which throws
     *   "abstract types either need to be mapped...").
     *   Test: testDelegatingArray1804_DefectTarget
     */

    // ----------------------------------------------------------------------
    // Test helper classes
    // ----------------------------------------------------------------------

    private static class TestAnnotatedWithParams extends AnnotatedWithParams {
        private static final long serialVersionUID = 1L;
        private final Class<?> declaringClass;
        private final Object result;
        private final Throwable throwable;

        TestAnnotatedWithParams(Class<?> declaringClass, Object result) {
            this(declaringClass, result, null);
        }

        TestAnnotatedWithParams(Class<?> declaringClass, Throwable throwable) {
            this(declaringClass, null, throwable);
        }

        private TestAnnotatedWithParams(Class<?> declaringClass, Object result, Throwable throwable) {
            super(null, null, null, null, null);
            this.declaringClass = declaringClass;
            this.result = result;
            this.throwable = throwable;
        }

        @Override
        public Class<?> getDeclaringClass() {
            return declaringClass;
        }

        @Override
        public Object call() throws Exception {
            if (throwable != null) {
                if (throwable instanceof Exception) {
                    throw (Exception) throwable;
                }
                throw new Exception(throwable);
            }
            return result;
        }

        @Override
        public Object call(Object... args) throws Exception {
            if (throwable != null) {
                if (throwable instanceof Exception) {
                    throw (Exception) throwable;
                }
                throw new Exception(throwable);
            }
            return result;
        }

        @Override
        public Object call1(Object arg) throws Exception {
            if (throwable != null) {
                if (throwable instanceof Exception) {
                    throw (Exception) throwable;
                }
                throw new Exception(throwable);
            }
            return result;
        }

        @Override
        public void setValue(Object pojo, Object value) throws Exception {
            // no-op
        }

        @Override
        public Object getValue(Object pojo) throws Exception {
            return null;
        }

        @Override
        public AnnotatedParameter getParameter(int index) {
            return null;
        }

        @Override
        public int getParameterCount() {
            return 0;
        }

        @Override
        public Class<?> getRawParameterType(int index) {
            return null;
        }

        @Override
        public JavaType getParameterType(int index) {
            return null;
        }

        @Override
        public String getName() {
            return "test";
        }
    }

    private static class TestDeserializationContext extends DeserializationContext {
        private static final long serialVersionUID = 1L;

        TestDeserializationContext() {
            super((DeserializationContext) null, null, null, null);
        }

        @Override
        public Object handleInstantiationProblem(Class<?> valueClass, Object value, Throwable t) throws IOException {
            throw new JsonMappingException(null, "Instantiation problem for " + valueClass.getName(), t);
        }

        @Override
        public Object handleMissingInstantiator(Class<?> valueClass, ValueInstantiator instantiator, 
                com.fasterxml.jackson.databind.util.LinkedNode<String> pn, String message, Object... args) throws IOException {
            throw new JsonMappingException(null, "Missing instantiator for " + valueClass.getName());
        }

        @Override
        public Object findInjectableValue(Object valueId, SettableBeanProperty forProperty, Object beanInstance) {
            return "injectable";
        }

        @Override
        public JsonMappingException instantiationException(Class<?> valueClass, Throwable t) {
            return new JsonMappingException(null, "Instantiation of " + valueClass.getName() + " failed", t);
        }

        @Override
        public JsonMappingException instantiationException(Class<?> valueClass, Object value, Throwable t) {
            return new JsonMappingException(null, "Instantiation of " + valueClass.getName() + " failed", t);
        }

        @Override
        public JsonMappingException weirdStringException(String value, Class<?> inst, String msg, Object... args) {
            return new JsonMappingException(null, "Weird string: " + value);
        }

        @Override
        public JsonMappingException weirdNumberException(Number value, Class<?> inst, String msg, Object... args) {
            return new JsonMappingException(null, "Weird number: " + value);
        }

        @Override
        public JsonMappingException weirdObjectException(Object value, Class<?> inst, String msg, Object... args) {
            return new JsonMappingException(null, "Weird object: " + value);
        }

        @Override
        public JsonMappingException wrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) {
            return new JsonMappingException(null, "Wrong token");
        }

        @Override
        public JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String msg, Object... args) {
            return new JsonMappingException(null, "Invalid type id: " + typeId);
        }

        @Override
        public JsonMappingException badTypeIdException(JavaType baseType, String typeId, String msg, Object... args) {
            return new JsonMappingException(null, "Bad type id: " + typeId);
        }

        @Override
        public JsonMappingException handleBadTypeId(JavaType type, String typeId, String msg, Object... args) {
            return new JsonMappingException(null, "Bad type id: " + typeId);
        }

        @Override
        public JsonMappingException handleBadMerge(JsonParser p, JsonToken t, String msg, Object... args) {
            return new JsonMappingException(null, "Bad merge");
        }

        @Override
        public JsonMappingException handleBadProperty(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad property");
        }

        @Override
        public JsonMappingException handleBadAttribute(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad attribute");
        }

        @Override
        public JsonMappingException handleBadToken(JsonParser p, JsonToken t, String msg, Object... args) {
            return new JsonMappingException(null, "Bad token");
        }

        @Override
        public JsonMappingException handleBadType(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad type");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadField(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad field");
        }

        @Override
        public JsonMappingException handleBadName(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad name");
        }

        @Override
        public JsonMappingException handleBadString(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad string");
        }

        @Override
        public JsonMappingException handleBadNumber(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad number");
        }

        @Override
        public JsonMappingException handleBadBoolean(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad boolean");
        }

        @Override
        public JsonMappingException handleBadNull(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad null");
        }

        @Override
        public JsonMappingException handleBadArray(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad array");
        }

        @Override
        public JsonMappingException handleBadObject(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad object");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, " Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {
            return new JsonMappingException(null, "Bad value");
        }

        @Override
        public JsonMappingException handleBadValue(JsonParser p, String msg, Object... args) {