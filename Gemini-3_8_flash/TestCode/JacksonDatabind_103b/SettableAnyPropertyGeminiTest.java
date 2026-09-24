package com.fasterxml.jackson.databind.deser;

/* [Branch & Defect Analysis Matrix]
 * Target Class: SettableAnyProperty
 *
 * Decision / Condition Matrix:
 * 1. Constructor Branches:
 *    - 6-arg constructor: properly initializes _property, _setter, _type, _keyDeserializer, _valueDeserializer,
 *      _valueTypeDeserializer, and sets _setterIsField = (setter instanceof AnnotatedField).
 *    - 5-arg deprecated constructor: delegates to 6-arg with _keyDeserializer = null.
 * 2. withValueDeserializer:
 *    - Creates cloned SettableAnyProperty with updated JsonDeserializer while retaining other attributes.
 * 3. fixAccess:
 *    - Verifies setter member access permissions are modified based on MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS.
 * 4. readResolve (JDK Serialization Safeguard):
 *    - _setter == null -> throws IllegalArgumentException("Missing method (broken JDK (de)serialization?)")
 *    - _setter != null && _setter.getAnnotated() == null -> throws IllegalArgumentException
 *    - _setter != null && _setter.getAnnotated() != null -> returns this
 * 5. Accessors:
 *    - getProperty(), getType(), hasValueDeserializer(), toString()
 * 6. deserializeAndSet:
 *    - _keyDeserializer == null -> key = propName
 *    - _keyDeserializer != null -> key = _keyDeserializer.deserializeKey(propName, ctxt)
 *    - UnresolvedForwardReference caught:
 *      * _valueDeserializer.getObjectIdReader() == null -> throws JsonMappingException.from(...)
 *      * _valueDeserializer.getObjectIdReader() != null -> appends AnySetterReferring to roid
 * 7. deserialize:
 *    - token == VALUE_NULL -> _valueDeserializer.getNullValue(ctxt)
 *    - _valueTypeDeserializer != null -> _valueDeserializer.deserializeWithType(...)
 *    - _valueTypeDeserializer == null -> _valueDeserializer.deserialize(...)
 * 8. set:
 *    - _setterIsField == true:
 *      * field.getValue(instance) != null -> put into map
 *      * field.getValue(instance) == null -> no-op / ignored
 *    - _setterIsField == false:
 *      * calls AnnotatedMethod.callOnWith(instance, propName, value)
 *    - Exception wrapping via _throwAsIOE:
 *      * IllegalArgumentException -> wraps in JsonMappingException with detailed actual/expected types
 *        - origMsg != null -> appends ", problem: " + origMsg
 *        - origMsg == null -> appends " (no error message provided)"
 *      * IOException -> rethrown as IOException
 *      * RuntimeException -> rethrown as RuntimeException
 *      * Checked Exception -> root cause wrapped in JsonMappingException
 * 9. AnySetterReferring:
 *    - handleResolvedForwardReference:
 *      * hasId(id) == false -> throws IllegalArgumentException
 *      * hasId(id) == true -> delegates to _parent.set(...)
 * 10. Defects4J Defect Target:
 *    - BasicExceptionTest::testLocationAddition failure condition where key deserialization exception
 *      message duplication causes duplicate 'at [' location markers in JsonMappingException.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SettableAnyPropertyGeminiTest {

    public enum ABC { A, B, C }

    public static class TestBean {
        public Map<String, Object> map = new HashMap<String, Object>();
        public Map<String, Object> nullMap = null;
        private String privateField;

        public void anySetter(String key, Object value) {
            map.put(key, value);
        }

        private void privateSetter(String key, Object value) {
            map.put(key, value);
        }
    }

    public static class BeanWithAnySetterABC {
        public Map<ABC, String> map = new HashMap<ABC, String>();

        @JsonAnySetter
        public void setAny(ABC key, String value) {
            map.put(key, value);
        }
    }

    public static class MapWithEnumKey {
        public Map<ABC, String> map;
    }

    public static class DummyObjectIdReader extends ObjectIdReader {
        public DummyObjectIdReader() {
            super(TypeFactory.defaultInstance().constructType(String.class),
                    PropertyName.construct("id"),
                    null, null, null, new SimpleObjectIdResolver());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);
        BeanProperty prop = new BeanProperty.Bogus();

        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        // 5-arg deprecated constructor
        SettableAnyProperty anyProp5 = new SettableAnyProperty(prop, am, type, valueDeser, null);
        assertSame(prop, anyProp5.getProperty());
        assertSame(type, anyProp5.getType());
        assertTrue(anyProp5.hasValueDeserializer());
        assertFalse(anyProp5._setterIsField);

        // 6-arg constructor
        KeyDeserializer keyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return key;
            }
        };
        SettableAnyProperty anyProp6 = new SettableAnyProperty(prop, am, type, keyDeser, valueDeser, null);
        assertTrue(anyProp6.hasValueDeserializer());

        // withValueDeserializer
        JsonDeserializer<Object> altDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        SettableAnyProperty withDeser = anyProp6.withValueDeserializer(altDeser);
        assertNotSame(anyProp6, withDeser);
        assertSame(altDeser, withDeser._valueDeserializer);
        assertSame(keyDeser, withDeser._keyDeserializer);
        assertSame(prop, withDeser.getProperty());

        // without value deserializer
        SettableAnyProperty noDeser = new SettableAnyProperty(prop, am, type, null, null, null);
        assertFalse(noDeser.hasValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testSetMethodSuccess() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, null);
        TestBean bean = new TestBean();

        anyProp.set(bean, "testKey", "testValue");
        assertEquals("testValue", bean.map.get("testKey"));
    }

    @Test(timeout = 4000)
    public void testSetFieldSuccessAndNullMapIgnored() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Field mapField = TestBean.class.getField("map");
        AnnotatedField afMap = new AnnotatedField(null, mapField, null);

        SettableAnyProperty anyPropMap = new SettableAnyProperty(null, afMap, type, null, null);
        assertTrue(anyPropMap._setterIsField);
        TestBean bean = new TestBean();

        anyPropMap.set(bean, "fieldKey", "fieldVal");
        assertEquals("fieldVal", bean.map.get("fieldKey"));

        // When the field itself holds null, set() must ignore it gracefully
        Field nullField = TestBean.class.getField("nullMap");
        AnnotatedField afNull = new AnnotatedField(null, nullField, null);
        SettableAnyProperty anyPropNull = new SettableAnyProperty(null, afNull, type, null, null);

        anyPropNull.set(bean, "anyKey", "anyVal");
        assertNull(bean.nullMap);
    }

    @Test(timeout = 4000)
    public void testToStringFormat() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, null);
        String str = anyProp.toString();
        assertEquals("[any property on class " + TestBean.class.getName() + "]", str);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Deserialization Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeNullToken() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "NORMAL";
            }

            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "NULL_SENTINEL";
            }
        };

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, deser, null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = anyProp.deserialize(p, ctxt);
        assertEquals("NULL_SENTINEL", result);
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "WITHOUT_TYPE";
            }

            @Override
            public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeser) {
                return "WITH_TYPE";
            }
        };

        TypeDeserializer dummyTypeDeser = new TypeDeserializer() {
            @Override public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override public String getPropertyName() { return null; }
            @Override public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override public Class<?> getDefaultImpl() { return null; }
            @Override public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return null; }
        };

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, deser, dummyTypeDeser);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"data\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object result = anyProp.deserialize(p, ctxt);
        assertEquals("WITH_TYPE", result);
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithAndWithoutKeyDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        // Case 1: _keyDeserializer == null
        SettableAnyProperty propNoKeyDeser = new SettableAnyProperty(null, am, type, null, valueDeser, null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p1 = mapper.getFactory().createParser("\"v1\"");
        p1.nextToken();
        TestBean bean1 = new TestBean();
        propNoKeyDeser.deserializeAndSet(p1, mapper.getDeserializationContext(), bean1, "k1");
        assertEquals("v1", bean1.map.get("k1"));
        p1.close();

        // Case 2: _keyDeserializer != null
        KeyDeserializer keyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return "TRANSFORMED_" + key;
            }
        };
        SettableAnyProperty propWithKeyDeser = new SettableAnyProperty(null, am, type, keyDeser, valueDeser, null);
        JsonParser p2 = mapper.getFactory().createParser("\"v2\"");
        p2.nextToken();
        TestBean bean2 = new TestBean();
        propWithKeyDeser.deserializeAndSet(p2, mapper.getDeserializationContext(), bean2, "k2");
        assertEquals("v2", bean2.map.get("TRANSFORMED_k2"));
        p2.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLocationAdditionDefectOnAnySetterEnumKey() throws Exception {
        // Targets duplicate location addition ("at [") when deserializing invalid map/any-setter key
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"INVALID_KEY\": \"val\"}", BeanWithAnySetterABC.class);
            fail("Expected JsonMappingException for invalid enum key");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message should not be null", msg);
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf("at [", idx)) != -1) {
                count++;
                idx += 4;
            }
            assertEquals("Should only get one 'at [' marker, got " + count + ", source: " + msg, 1, count);
        }
    }

    @Test(timeout = 4000)
    public void testLocationAdditionDefectOnMapEnumKey() throws Exception {
        // Exact regression pattern from BasicExceptionTest::testLocationAddition
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"map\": {\"value\": \"val\"}}", MapWithEnumKey.class);
            fail("Expected JsonMappingException for invalid enum map key");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message should not be null", msg);
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf("at [", idx)) != -1) {
                count++;
                idx += 4;
            }
            assertEquals("Should only get one 'at [' marker, got " + count + ", source: " + msg, 1, count);
        }
    }

    // =========================================================================
    // Partition D: Exception, UnresolvedForwardReference & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnresolvedForwardReferenceWithoutObjectIdReader() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        final ObjectIdGenerator.IdKey idKey = new ObjectIdGenerator.IdKey(Object.class, null, "oid1");
        final ReadableObjectId roid = new ReadableObjectId(idKey);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new UnresolvedForwardReference(p, "Unresolved test ref", p.getCurrentLocation(), roid);
            }
        };

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, deser, null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"someVal\"");
        p.nextToken();
        TestBean bean = new TestBean();

        try {
            anyProp.deserializeAndSet(p, mapper.getDeserializationContext(), bean, "testProp");
            fail("Should fail due to missing identity info");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unresolved forward reference but no identity info."));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testUnresolvedForwardReferenceWithObjectIdReaderAndReferringResolution() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        final ObjectIdGenerator.IdKey idKey = new ObjectIdGenerator.IdKey(Object.class, null, "oid1");
        final ReadableObjectId roid = new ReadableObjectId(idKey);
        final DummyObjectIdReader oir = new DummyObjectIdReader();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public ObjectIdReader getObjectIdReader() {
                return oir;
            }

            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new UnresolvedForwardReference(p, "Unresolved test ref", p.getCurrentLocation(), roid);
            }
        };

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, deser, null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("\"someVal\"");
        p.nextToken();
        TestBean bean = new TestBean();

        // deserializeAndSet registers Referring
        anyProp.deserializeAndSet(p, mapper.getDeserializationContext(), bean, "forwardKey");
        assertTrue(roid.hasReferringProperties());

        Iterator<Referring> it = roid.referringProperties();
        assertTrue(it.hasNext());
        Referring referring = it.next();

        // Branch 1: handleResolvedForwardReference with unregistered ID -> IllegalArgumentException
        try {
            referring.handleResolvedForwardReference("wrongId", "resolvedVal");
            fail("Expected IllegalArgumentException for unregistered id");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Trying to resolve a forward reference with id [wrongId]"));
        }

        // Branch 2: handleResolvedForwardReference with registered ID -> sets property
        referring.handleResolvedForwardReference("oid1", "resolvedVal");
        assertEquals("resolvedVal", bean.map.get("forwardKey"));
        p.close();
    }

    @Test(timeout = 4000)
    public void testThrowAsIOEIllegalArgumentExceptionBranches() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, null);

        // Branch 1: with non-null message and non-null value
        try {
            anyProp._throwAsIOE(new IllegalArgumentException("Custom IAE"), "propA", 12345);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Problem deserializing \"any\" property 'propA'"));
            assertTrue(msg.contains("actual type: java.lang.Integer"));
            assertTrue(msg.contains("problem: Custom IAE"));
        }

        // Branch 2: with null message and null value
        try {
            anyProp._throwAsIOE(new IllegalArgumentException((String) null), "propB", null);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Problem deserializing \"any\" property 'propB'"));
            assertTrue(msg.contains("actual type: UNKNOWN"));
            assertTrue(msg.contains("(no error message provided)"));
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsIOEOtherExceptionTypes() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod am = new AnnotatedMethod(null, method, null, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, am, type, null, null);

        // Branch 3: IOException rethrown as-is
        IOException ioe = new IOException("Disk failure");
        try {
            anyProp._throwAsIOE(ioe, "propIOE", "val");
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }

        // Branch 4: RuntimeException rethrown as-is
        IllegalStateException ise = new IllegalStateException("State error");
        try {
            anyProp._throwAsIOE(ise, "propRTE", "val");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertSame(ise, e);
        }

        // Branch 5: Checked Exception wrapped into JsonMappingException
        Exception checked = new Exception("Root checked error");
        try {
            anyProp._throwAsIOE(checked, "propChecked", "val");
            fail("Expected JsonMappingException wrapping checked exception");
        } catch (JsonMappingException e) {
            assertEquals("Root checked error", e.getMessage());
            assertSame(checked, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testSetFieldWrongInstanceTriggersThrowAsIOE() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Field mapField = TestBean.class.getField("map");
        AnnotatedField afMap = new AnnotatedField(null, mapField, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, afMap, type, null, null);

        try {
            // Passing incompatible instance string triggers Field.get() IllegalArgumentException
            anyProp.set("WrongTargetInstance", "propName", "propVal");
            fail("Expected JsonMappingException from field access mismatch");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Problem deserializing \"any\" property 'propName'"));
            assertTrue(e.getMessage().contains("actual type: java.lang.String"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Reflection Access & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFixAccessModifier() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        Field privField = TestBean.class.getDeclaredField("privateField");
        AnnotatedField afPriv = new AnnotatedField(null, privField, null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, afPriv, type, null, null);
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();

        anyProp.fixAccess(config);
        assertTrue("Private field should be accessible after fixAccess", privField.isAccessible());

        Method privMethod = TestBean.class.getDeclaredMethod("privateSetter", String.class, Object.class);
        AnnotatedMethod amPriv = new AnnotatedMethod(null, privMethod, null, null);
        SettableAnyProperty anyPropMethod = new SettableAnyProperty(null, amPriv, type, null, null);

        anyPropMethod.fixAccess(config);
        assertTrue("Private method should be accessible after fixAccess", privMethod.isAccessible());
    }

    @Test(timeout = 4000)
    public void testReadResolveLifecycleIntegrity() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);

        // Case 1: _setter == null
        SettableAnyProperty nullSetterProp = new SettableAnyProperty(null, null, type, null, null);
        try {
            nullSetterProp.readResolve();
            fail("Expected IllegalArgumentException when _setter is null");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Missing method (broken JDK (de)serialization?)"));
        }

        // Case 2: _setter != null but _setter.getAnnotated() == null
        AnnotatedMethod brokenMember = new AnnotatedMethod(null, null, null, null);
        SettableAnyProperty brokenSetterProp = new SettableAnyProperty(null, brokenMember, type, null, null);
        try {
            brokenSetterProp.readResolve();
            fail("Expected IllegalArgumentException when _setter.getAnnotated() is null");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Missing method (broken JDK (de)serialization?)"));
        }

        // Case 3: Valid _setter -> returns this
        Method method = TestBean.class.getMethod("anySetter", String.class, Object.class);
        AnnotatedMethod validMember = new AnnotatedMethod(null, method, null, null);
        SettableAnyProperty validProp = new SettableAnyProperty(null, validMember, type, null, null);
        assertSame(validProp, validProp.readResolve());
    }
}