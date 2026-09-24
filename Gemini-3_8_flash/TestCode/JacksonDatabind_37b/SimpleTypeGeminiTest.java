package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.type.SimpleType
 *
 * 1. Branch Coverage Targets:
 *    - construct(Class<?> cls):
 *      * Map.class.isAssignableFrom(cls) -> TRUE (throw IAE) / FALSE
 *      * Collection.class.isAssignableFrom(cls) -> TRUE (throw IAE) / FALSE
 *      * cls.isArray() -> TRUE (throw IAE) / FALSE
 *    - _narrow(Class<?> subclass):
 *      * _class == subclass -> TRUE (return this) / FALSE (return new SimpleType)
 *    - withTypeHandler(Object h):
 *      * _typeHandler == h -> TRUE (return this) / FALSE (return new SimpleType)
 *    - withValueHandler(Object h):
 *      * _valueHandler == h -> TRUE (return this) / FALSE (return new SimpleType)
 *    - withStaticTyping():
 *      * _asStatic -> TRUE (return this) / FALSE (return new SimpleType with asStatic=true)
 *    - buildCanonicalName():
 *      * count == 0 -> class.getName()
 *      * count > 0 -> loop: i == 0 (no comma) vs i > 0 (append comma)
 *    - getGenericSignature(StringBuilder):
 *      * count == 0 vs count > 0 (containedType loop with <...>;)
 *    - equals(Object o):
 *      * o == this -> TRUE
 *      * o == null -> FALSE
 *      * o.getClass() != getClass() -> FALSE
 *      * other._class != this._class -> FALSE
 *      * b1.equals(b2) -> TRUE / FALSE
 *    - Exception Paths (Simple types have no content types):
 *      * withContentType -> throws IAE
 *      * withContentTypeHandler -> throws IAE
 *      * withContentValueHandler -> throws IAE
 *
 * 2. Defect Analysis (Defects4J ground truth Objecid1083Test::testSimple):
 *    - Problem: Deserialization of Map subtype annotated with @JsonIdentityInfo
 *      (e.g., JsonMapSchema extends HashMap<String, Object>) fails with
 *      UnrecognizedPropertyException for declared POJO properties like "name"
 *      because type specialization/refinement on SimpleType / MapType improperly
 *      resolves properties or fails during bean property resolution.
 */
@SuppressWarnings("deprecation")
public class SimpleTypeGeminiTest {

    // Dummy generic classes for TypeBindings testing
    static class SingleParam<T> { }
    static class DoubleParam<T, U> { }

    // POJO Map subtype reproducing defect Objecid1083Test
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    public static class JsonMapSchemaDefect1083 extends HashMap<String, Object> {
        private static final long serialVersionUID = 1L;
        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testConstructAndBasicProperties() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
        assertFalse(type.useStaticType());
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testNarrowSameClassReturnsThis() {
        SimpleType type = SimpleType.construct(CharSequence.class);
        JavaType narrowed = type._narrow(CharSequence.class);
        assertSame("Narrowing to the same class must return the identical instance", type, narrowed);
    }

    @Test(timeout = 4000)
    public void testNarrowDifferentSubclassPreservesState() {
        SimpleType type = new SimpleType(CharSequence.class, TypeBindings.emptyBindings(),
                null, null, "vHandler", "tHandler", true);
        JavaType narrowed = type._narrow(String.class);

        assertNotSame(type, narrowed);
        assertEquals(String.class, narrowed.getRawClass());
        assertEquals("vHandler", narrowed.getValueHandler());
        assertEquals("tHandler", narrowed.getTypeHandler());
        assertTrue(narrowed.useStaticType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerTransitions() {
        SimpleType base = SimpleType.construct(Integer.class);
        assertNull(base.getTypeHandler());

        // Identity check when null
        assertSame(base, base.withTypeHandler(null));

        // Assign new handler
        Object handler = "customTypeHandler";
        SimpleType withHandler = base.withTypeHandler(handler);
        assertNotSame(base, withHandler);
        assertSame(handler, withHandler.getTypeHandler());

        // Identity check when handler already matches
        assertSame(withHandler, withHandler.withTypeHandler(handler));
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerTransitions() {
        SimpleType base = SimpleType.construct(Double.class);
        assertNull(base.getValueHandler());

        // Identity check when null
        assertSame(base, base.withValueHandler(null));

        // Assign new handler
        Object handler = new Object();
        SimpleType withHandler = base.withValueHandler(handler);
        assertNotSame(base, withHandler);
        assertSame(handler, withHandler.getValueHandler());

        // Identity check when handler already matches
        assertSame(withHandler, withHandler.withValueHandler(handler));
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingTransitions() {
        SimpleType base = SimpleType.construct(Boolean.class);
        assertFalse(base.useStaticType());

        SimpleType staticType = base.withStaticTyping();
        assertNotSame(base, staticType);
        assertTrue(staticType.useStaticType());

        // Idempotent: already static returns this
        assertSame(staticType, staticType.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNullForSimpleType() {
        SimpleType type = SimpleType.construct(Object.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull("SimpleType refinement should return null as it represents non-specialized type", refined);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testBuildCanonicalNameAndGenericSignatureWithoutBindings() {
        SimpleType type = SimpleType.construct(String.class);
        assertEquals("java.lang.String", type.toCanonical());
        assertEquals("[simple type, class java.lang.String]", type.toString());

        StringBuilder sbErased = type.getErasedSignature(new StringBuilder());
        assertEquals("Ljava/lang/String;", sbErased.toString());

        StringBuilder sbGeneric = type.getGenericSignature(new StringBuilder());
        assertEquals("Ljava/lang/String;", sbGeneric.toString());
    }

    @Test(timeout = 4000)
    public void testCanonicalAndGenericSignatureSingleTypeParameter() {
        JavaType strType = SimpleType.construct(String.class);
        TypeBindings bindings = TypeBindings.create(SingleParam.class, new JavaType[] { strType });

        SimpleType genericType = new SimpleType(SingleParam.class, bindings, null, null);
        String expectedCanonical = SingleParam.class.getName() + "<java.lang.String>";
        assertEquals(expectedCanonical, genericType.toCanonical());

        StringBuilder sbGeneric = genericType.getGenericSignature(new StringBuilder());
        String expectedGenericSig = "L" + SingleParam.class.getName().replace('.', '/') + "<Ljava/lang/String;>;";
        assertEquals(expectedGenericSig, sbGeneric.toString());
    }

    @Test(timeout = 4000)
    public void testCanonicalAndGenericSignatureMultipleTypeParameters() {
        JavaType strType = SimpleType.construct(String.class);
        JavaType intType = SimpleType.construct(Integer.class);
        TypeBindings bindings = TypeBindings.create(DoubleParam.class, new JavaType[] { strType, intType });

        SimpleType genericType = new SimpleType(DoubleParam.class, bindings, null, null);
        // Exercises the "i > 0" comma branch in buildCanonicalName
        String expectedCanonical = DoubleParam.class.getName() + "<java.lang.String,java.lang.Integer>";
        assertEquals(expectedCanonical, genericType.toCanonical());

        // Exercises loop in getGenericSignature
        StringBuilder sbGeneric = genericType.getGenericSignature(new StringBuilder());
        String expectedGenericSig = "L" + DoubleParam.class.getName().replace('.', '/') + "<Ljava/lang/String;Ljava/lang/Integer;>;";
        assertEquals(expectedGenericSig, sbGeneric.toString());
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeAllowsArraysAndMaps() {
        // constructUnsafe bypasses sanity checks in construct()
        SimpleType unsafeMap = SimpleType.constructUnsafe(Map.class);
        assertNotNull(unsafeMap);
        assertEquals(Map.class, unsafeMap.getRawClass());

        SimpleType unsafeArray = SimpleType.constructUnsafe(int[].class);
        assertNotNull(unsafeArray);
        assertEquals(int[].class, unsafeArray.getRawClass());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Objecid1083Test)
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testObjectIdOnMapSubtypeDefect1083Exact() throws Exception {
        // Targets known failure: UnrecognizedPropertyException "name" on Map subtype with @JsonIdentityInfo
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"testIdentity\"}";
        JsonMapSchemaDefect1083 result = mapper.readValue(json, JsonMapSchemaDefect1083.class);

        assertNotNull("Deserialized Map subtype must not be null", result);
        assertEquals("Declared POJO property 'name' must be correctly set on Map subtype",
                "testIdentity", result.getName());
    }

    @Test(timeout = 4000)
    public void testObjectIdOnMapSubtypeDefect1083WithAdditionalMapEntries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"schema1\",\"extraKey\":\"extraValue\"}";
        JsonMapSchemaDefect1083 result = mapper.readValue(json, JsonMapSchemaDefect1083.class);

        assertNotNull(result);
        assertEquals("schema1", result.getName());
        assertEquals("extraValue", result.get("extraKey"));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructMapThrowsException() {
        SimpleType.construct(Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructHashMapThrowsException() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructCollectionThrowsException() {
        SimpleType.construct(Collection.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructListThrowsException() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructArrayThrowsException() {
        SimpleType.construct(String[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructPrimitiveArrayThrowsException() {
        SimpleType.construct(byte[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeThrowsException() {
        SimpleType type = SimpleType.construct(Long.class);
        type.withContentType(SimpleType.construct(String.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeHandlerThrowsException() {
        SimpleType type = SimpleType.construct(Long.class);
        type.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentValueHandlerThrowsException() {
        SimpleType type = SimpleType.construct(Long.class);
        type.withContentValueHandler("handler");
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testProtectedConstructorsDirectAccess() {
        // Constructor 1: SimpleType(Class<?> cls)
        SimpleType t1 = new SimpleType(Float.class);
        assertEquals(Float.class, t1.getRawClass());

        // Constructor 2: SimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts)
        SimpleType t2 = new SimpleType(Float.class, TypeBindings.emptyBindings(), null, null);
        assertEquals(Float.class, t2.getRawClass());

        // Constructor 3: SimpleType(TypeBase base) [copy-constructor]
        SimpleType t3 = new SimpleType(t1);
        assertEquals(Float.class, t3.getRawClass());

        // Constructor 4: SimpleType(Class<?> cls, TypeBindings, JavaType, JavaType[], Object, Object, boolean)
        SimpleType t4 = new SimpleType(Float.class, TypeBindings.emptyBindings(), null, null, "v", "t", true);
        assertEquals("v", t4.getValueHandler());
        assertEquals("t", t4.getTypeHandler());
        assertTrue(t4.useStaticType());

        // Constructor 5: SimpleType(Class<?> cls, TypeBindings, JavaType, JavaType[], int, Object, Object, boolean)
        SimpleType t5 = new SimpleType(Float.class, TypeBindings.emptyBindings(), null, null, 12345, "v2", "t2", false);
        assertEquals("v2", t5.getValueHandler());
        assertEquals("t2", t5.getTypeHandler());
        assertFalse(t5.useStaticType());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        SimpleType typeA1 = SimpleType.construct(String.class);
        SimpleType typeA2 = SimpleType.construct(String.class);
        SimpleType typeB = SimpleType.construct(Integer.class);

        // Reflexive
        assertTrue(typeA1.equals(typeA1));

        // Symmetric & Consistent
        assertTrue(typeA1.equals(typeA2));
        assertTrue(typeA2.equals(typeA1));
        assertEquals(typeA1.hashCode(), typeA2.hashCode());

        // Not equal to null
        assertFalse(typeA1.equals(null));

        // Not equal to incompatible object type
        assertFalse(typeA1.equals("JustAString"));

        // Not equal when class differs
        assertFalse(typeA1.equals(typeB));

        // Not equal when generic bindings differ
        JavaType strType = SimpleType.construct(String.class);
        JavaType intType = SimpleType.construct(Integer.class);

        TypeBindings b1 = TypeBindings.create(SingleParam.class, new JavaType[] { strType });
        TypeBindings b2 = TypeBindings.create(SingleParam.class, new JavaType[] { intType });

        SimpleType gen1 = new SimpleType(SingleParam.class, b1, null, null);
        SimpleType gen2 = new SimpleType(SingleParam.class, b2, null, null);

        assertFalse(gen1.equals(gen2));
        assertFalse(gen2.equals(gen1));
    }
}