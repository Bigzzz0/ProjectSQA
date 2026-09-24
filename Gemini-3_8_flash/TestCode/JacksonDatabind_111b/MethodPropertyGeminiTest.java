package com.fasterxml.jackson.databind.deser.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: MethodProperty (SettableBeanProperty subclass)
 * Target Defect: Issues related to null handling in deserialization (e.g. databind#2303, databind#2023,
 *                and JDKAtomicTypesDeserTest::testNullWithinNested). Specifically, ensuring null values
 *                nested in atomic references or properties retain their proper NullValueProvider mapping
 *                and do not fail or inadvertently deserialize as unhandled/wrong null objects.
 *
 * Targeted Decision Branches in MethodProperty:
 * 1. Constructor: _skipNulls = NullsConstantProvider.isSkipper(_nullProvider)
 * 2. withName(PropertyName): Creates new MethodProperty preserving annotated method, setter, and skipNulls flag.
 * 3. withValueDeserializer(JsonDeserializer):
 *    - Branch: _valueDeserializer == deser -> return this
 *    - Branch: _valueDeserializer != deser -> return new MethodProperty with updated deser and synced _nullProvider.
 * 4. withNullProvider(NullValueProvider): Returns new instance with updated nva and updated _skipNulls.
 * 5. fixAccess(DeserializationConfig): Propagates OVERRIDE_PUBLIC_ACCESS_MODIFIERS to AnnotatedMethod.
 * 6. getAnnotation(Class<A>):
 *    - Branch: _annotated == null -> null
 *    - Branch: _annotated != null -> _annotated.getAnnotation(acls)
 * 7. getMember(): Returns _annotated.
 * 8. deserializeAndSet(JsonParser, DeserializationContext, Object):
 *    - Branch 1: p.hasToken(JsonToken.VALUE_NULL)
 *      - Sub-branch 1a: _skipNulls == true -> return directly without setting
 *      - Sub-branch 1b: _skipNulls == false -> value = _nullProvider.getNullValue(ctxt); _setter.invoke(instance, value)
 *    - Branch 2: _valueTypeDeserializer == null
 *      - deserialize normal value
 *      - Sub-branch 2a: coerced value == null && _skipNulls == true -> return directly
 *      - Sub-branch 2b: coerced value == null && _skipNulls == false -> value = _nullProvider.getNullValue(ctxt)
 *    - Branch 3: _valueTypeDeserializer != null -> deserializeWithType
 *    - Exception Path: setter throws exception -> catch and invoke _throwAsIOE(p, e, value)
 * 9. deserializeSetAndReturn(JsonParser, DeserializationContext, Object):
 *    - Mirrors branches of deserializeAndSet but handles return value of setter:
 *      - Sub-branch: setter returns null -> return original instance
 *      - Sub-branch: setter returns non-null -> return setter result
 *      - Sub-branch: _skipNulls == true on null -> return original instance
 *    - Exception Path: setter throws exception -> _throwAsIOE(p, e, value); return null
 * 10. set(Object, Object) & setAndReturn(Object, Object):
 *     - Direct invocation of _setter.invoke(instance, value)
 *     - Exception Handling: wraps reflected exceptions in IOExceptions
 * 11. readResolve(): JDK deserialization reconstitution ensuring transient _setter is restored from _annotated.
 */
public class MethodPropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomMarker {
        String value() default "test";
    }

    public static class TestBean {
        private String value;
        private Object returnedOnSet;

        @CustomMarker("annotatedMethod")
        public void setValue(String v) {
            this.value = v;
        }

        public String getValue() {
            return value;
        }

        public TestBean setFluentValue(String v) {
            this.value = v;
            return this;
        }

        public Object setReturningObject(String v) {
            this.value = v;
            return returnedOnSet;
        }

        public void throwException(String v) {
            throw new IllegalArgumentException("Forced setter error: " + v);
        }

        private void privateSetter(String v) {
            this.value = v;
        }
    }

    public static class NestedAtomicBean {
        public AtomicReference<String> ref;

        public NestedAtomicBean() { }

        public NestedAtomicBean(String initial) {
            this.ref = new AtomicReference<>(initial);
        }

        public void setRef(AtomicReference<String> ref) {
            this.ref = ref;
        }
    }

    public static class DummyPropDef extends BeanPropertyDefinition {
        private final PropertyName _name;
        private final AnnotatedMethod _method;

        public DummyPropDef(String name, AnnotatedMethod method) {
            this._name = PropertyName.construct(name);
            this._method = method;
        }

        @Override public PropertyName getFullName() { return _name; }
        @Override public String getName() { return _name.getSimpleName(); }
        @Override public PropertyName getWrapperName() { return null; }
        @Override public boolean hasName(PropertyName n) { return _name.equals(n); }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean couldDeserialize() { return true; }
        @Override public AnnotatedMethod getSetter() { return _method; }
        @Override public AnnotatedMember getPrimaryMember() { return _method; }
    }

    private MethodProperty createMethodProperty(String methodName, Class<?> paramType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType beanType = mapper.constructType(TestBean.class);
        JavaType propType = mapper.constructType(paramType);

        Method m = TestBean.class.getDeclaredMethod(methodName, paramType);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(mapper.getDeserializationConfig(), beanType);
        AnnotatedMethod am = new AnnotatedMethod(null, m, new AnnotationMap(), null);
        DummyPropDef propDef = new DummyPropDef(methodName, am);

        return new MethodProperty(propDef, propType, null, ac.getAnnotations(), am);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicSetAndGetAnnotation() throws Exception {
        MethodProperty prop = createMethodProperty("setValue", String.class);
        assertEquals("setValue", prop.getName());
        assertEquals(TestBean.class.getDeclaredMethod("setValue", String.class), prop.getMember().getMember());

        CustomMarker marker = prop.getAnnotation(CustomMarker.class);
        assertNull(marker); // Annotations empty initially unless scanned via Introspector

        TestBean bean = new TestBean();
        prop.set(bean, "Hello World");
        assertEquals("Hello World", bean.getValue());

        Object result = prop.setAndReturn(bean, "New Value");
        assertSame(bean, result);
        assertEquals("New Value", bean.getValue());
    }

    @Test(timeout = 4000)
    public void testFluentSetAndReturn() throws Exception {
        MethodProperty prop = createMethodProperty("setFluentValue", String.class);
        TestBean bean = new TestBean();

        Object returned = prop.setAndReturn(bean, "FluentVal");
        assertSame(bean, returned);
        assertEquals("FluentVal", bean.getValue());

        MethodProperty propReturnObj = createMethodProperty("setReturningObject", String.class);
        String customReturn = "DifferentInstance";
        bean.returnedOnSet = customReturn;
        Object customReturned = propReturnObj.setAndReturn(bean, "NewVal");
        assertSame(customReturn, customReturned);
        assertEquals("NewVal", bean.getValue());
    }

    @Test(timeout = 4000)
    public void testWithNameAndWithValueDeserializer() throws Exception {
        MethodProperty prop = createMethodProperty("setValue", String.class);
        PropertyName newName = new PropertyName("renamedValue");

        SettableBeanProperty renamed = prop.withName(newName);
        assertNotSame(prop, renamed);
        assertEquals("renamedValue", renamed.getName());
        assertSame(prop.getMember(), renamed.getMember());

        SettableBeanProperty sameDeser = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, sameDeser);

        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> dummyDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "dummy";
            }
        };

        SettableBeanProperty withNewDeser = prop.withValueDeserializer(dummyDeser);
        assertNotSame(prop, withNewDeser);
        assertSame(dummyDeser, withNewDeser.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testFixAccess() throws Exception {
        MethodProperty prop = createMethodProperty("privateSetter", String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig()
                .with(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);

        prop.fixAccess(config);

        TestBean bean = new TestBean();
        prop.set(bean, "AccessibleValue");
        assertEquals("AccessibleValue", bean.getValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSkipNullsTrueBehavior() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MethodProperty prop = createMethodProperty("setValue", String.class);

        SettableBeanProperty propSkipping = prop.withNullProvider(NullsConstantProvider.skipper());

        TestBean bean = new TestBean();
        bean.setValue("InitialValue");

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        propSkipping.deserializeAndSet(parser, ctxt, bean);
        assertEquals("InitialValue", bean.getValue());

        JsonParser parser2 = mapper.getFactory().createParser("null");
        parser2.nextToken();
        Object result = propSkipping.deserializeSetAndReturn(parser2, ctxt, bean);
        assertSame(bean, result);
        assertEquals("InitialValue", bean.getValue());
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetNullValueProvider() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MethodProperty prop = createMethodProperty("setValue", String.class);

        NullValueProvider customNullProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "DefaultFallback";
            }
        };

        SettableBeanProperty propWithNulls = prop.withNullProvider(customNullProvider);

        TestBean bean = new TestBean();
        bean.setValue("Prior");

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        propWithNulls.deserializeAndSet(parser, ctxt, bean);
        assertEquals("DefaultFallback", bean.getValue());

        bean.setValue("Prior2");
        JsonParser parser2 = mapper.getFactory().createParser("null");
        parser2.nextToken();
        Object ret = propWithNulls.deserializeSetAndReturn(parser2, ctxt, bean);
        assertSame(bean, ret);
        assertEquals("DefaultFallback", bean.getValue());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets JDKAtomicTypesDeserTest::testNullWithinNested defect.
     * Ensures atomic references deserialized within objects respect null tokens
     * and do not fail or misinterpret null mapping during MethodProperty invocation.
     */
    @Test(timeout = 4000)
    public void testNullWithinNestedAtomicReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = "{\"ref\":null}";
        NestedAtomicBean bean = mapper.readValue(json, NestedAtomicBean.class);

        assertNotNull("Bean should be deserialized", bean);
        assertNull("Nested AtomicReference property should be set to null", bean.ref);

        String jsonWithNullRef = "{\"ref\":\"test\"}";
        NestedAtomicBean bean2 = mapper.readValue(jsonWithNullRef, NestedAtomicBean.class);
        assertNotNull(bean2.ref);
        assertEquals("test", bean2.ref.get());

        String jsonWithInnerNull = "{\"ref\":null}";
        NestedAtomicBean bean3 = mapper.readerFor(NestedAtomicBean.class)
                .with(Nulls.AS_EMPTY)
                .readValue(jsonWithInnerNull);
        assertNotNull(bean3);
    }

    @Test(timeout = 4000)
    public void testCoercedNullSkipsOrSets() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MethodProperty prop = createMethodProperty("setValue", String.class);

        JsonDeserializer<Object> nullReturningDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        SettableBeanProperty propWithDeser = prop.withValueDeserializer(nullReturningDeser);

        NullValueProvider customNullProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "FromNullProvider";
            }
        };
        SettableBeanProperty propFull = propWithDeser.withNullProvider(customNullProvider);

        TestBean bean = new TestBean();
        JsonParser parser = mapper.getFactory().createParser("\"somethingCoercedToNull\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        propFull.deserializeAndSet(parser, ctxt, bean);
        assertEquals("FromNullProvider", bean.getValue());

        SettableBeanProperty propSkipper = propWithDeser.withNullProvider(NullsConstantProvider.skipper());
        TestBean bean2 = new TestBean();
        bean2.setValue("PreservedValue");
        JsonParser parser2 = mapper.getFactory().createParser("\"somethingCoercedToNull\"");
        parser2.nextToken();

        propSkipper.deserializeAndSet(parser2, ctxt, bean2);
        assertEquals("PreservedValue", bean2.getValue());

        Object ret = propSkipper.deserializeSetAndReturn(parser2, ctxt, bean2);
        assertSame(bean2, ret);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetThrowsWrappedException() throws Exception {
        MethodProperty prop = createMethodProperty("throwException", String.class);
        TestBean bean = new TestBean();

        try {
            prop.set(bean, "Trigger");
            fail("Expected JsonMappingException or IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Forced setter error: Trigger"));
        }

        try {
            prop.setAndReturn(bean, "TriggerReturn");
            fail("Expected JsonMappingException or IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Forced setter error: TriggerReturn"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetThrowsWrappedException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MethodProperty prop = createMethodProperty("throwException", String.class);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "Val";
            }
        };
        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser);

        TestBean bean = new TestBean();
        JsonParser parser = mapper.getFactory().createParser("\"Val\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            propWithDeser.deserializeAndSet(parser, ctxt, bean);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Forced setter error: Val"));
        }

        try {
            propWithDeser.deserializeSetAndReturn(parser, ctxt, bean);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Forced setter error: Val"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testJdkSerializationAndReadResolve() throws Exception {
        MethodProperty prop = createMethodProperty("setValue", String.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof MethodProperty);
        MethodProperty restoredProp = (MethodProperty) deserialized;

        assertEquals(prop.getName(), restoredProp.getName());
        assertEquals(prop.getType(), restoredProp.getType());

        TestBean bean = new TestBean();
        restoredProp.set(bean, "ValueAfterSerialization");
        assertEquals("ValueAfterSerialization", bean.getValue());
    }
}