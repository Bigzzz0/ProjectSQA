package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.type.MapType
 *
 * 1. Factory / Construction:
 *    - construct(Class, TypeBindings, JavaType, JavaType[], JavaType, JavaType): Standard 2.7+ factory.
 *    - construct(Class, JavaType, JavaType): Deprecated factory (databind#1102).
 *      * Known Defect: In defective versions, bindings are passed as null, leading to null TypeBindings
 *        and failure during refinement/type resolution in deserialization pipelines.
 *    - Protected constructor (TypeBase, JavaType, JavaType) tested via extension.
 *
 * 2. Immutable Mutators / State Branch Transitions:
 *    - withStaticTyping():
 *      * Branch A (_asStatic == true): returns 'this' identity.
 *      * Branch B (_asStatic == false): returns new MapType with key & value withStaticTyping(), asStatic=true.
 *    - withContentType(JavaType):
 *      * Branch A (_valueType == contentType): returns 'this' identity.
 *      * Branch B (_valueType != contentType): returns new MapType with new valueType.
 *    - withKeyType(JavaType):
 *      * Branch A (_keyType == keyType): returns 'this' identity.
 *      * Branch B (_keyType != keyType): returns new MapType with new keyType.
 *
 * 3. Handler Propagation:
 *    - withTypeHandler(Object)
 *    - withContentTypeHandler(Object) -> propagated to _valueType.withTypeHandler(h)
 *    - withValueHandler(Object)
 *    - withContentValueHandler(Object) -> propagated to _valueType.withValueHandler(h)
 *    - withKeyTypeHandler(Object) -> propagated to _keyType.withTypeHandler(h)
 *    - withKeyValueHandler(Object) -> propagated to _keyType.withValueHandler(h)
 *
 * 4. Refinement & Narrowing:
 *    - _narrow(Class<?>): creates narrowed MapType preserving handlers, bindings, and staticness.
 *    - refine(Class, TypeBindings, JavaType, JavaType[]): overrides type hierarchy while preserving handlers.
 *
 * 5. String & Contract Integrity:
 *    - toString(): exact format verification "[map type; class ..., ... -> ...]".
 *    - equals() / hashCode() contract consistency across permutations.
 *    - Java Object Serialization integrity (serialVersionUID = 1L).
 */
public class MapTypeGeminiTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();
    private final JavaType _strType = SimpleType.constructUnsafe(String.class);
    private final JavaType _intType = SimpleType.constructUnsafe(Integer.class);
    private final JavaType _longType = SimpleType.constructUnsafe(Long.class);

    /*
     **********************************************************
     * Partition A: Core Functional Logic & State Transitions
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testStandardConstructionAndGetters() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{_strType, _intType});
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        MapType mapType = MapType.construct(Map.class, bindings, superClass, null, _strType, _intType);

        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(_strType, mapType.getKeyType());
        assertEquals(_intType, mapType.getContentType());
        assertFalse(mapType.useStaticValues());
        assertNull(mapType.getValueHandler());
        assertNull(mapType.getTypeHandler());
        assertTrue(mapType.isMapLikeType());
        assertTrue(mapType.isContainerType());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingBranches() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType nonStatic = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);
        assertFalse(nonStatic.useStaticValues());

        // Branch 1: transitioning from false to true
        MapType staticType = nonStatic.withStaticTyping();
        assertNotSame(nonStatic, staticType);
        assertTrue(staticType.useStaticValues());
        assertTrue(staticType.getKeyType().useStaticValues());
        assertTrue(staticType.getContentType().useStaticValues());

        // Branch 2: already static -> should return 'this' identity
        MapType sameStatic = staticType.withStaticTyping();
        assertSame("Should return self when already static", staticType, sameStatic);
    }

    @Test(timeout = 4000)
    public void testWithContentTypeBranches() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{_strType, _intType});
        MapType mapType = MapType.construct(Map.class, bindings, null, null, _strType, _intType);

        // Branch 1: same content type identity
        MapType identity = (MapType) mapType.withContentType(_intType);
        assertSame("Should return this when content type is identical", mapType, identity);

        // Branch 2: different content type
        MapType modified = (MapType) mapType.withContentType(_longType);
        assertNotSame(mapType, modified);
        assertEquals(_longType, modified.getContentType());
        assertEquals(_strType, modified.getKeyType());
    }

    @Test(timeout = 4000)
    public void testWithKeyTypeBranches() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{_strType, _intType});
        MapType mapType = MapType.construct(Map.class, bindings, null, null, _strType, _intType);

        // Branch 1: same key type identity
        MapType identity = mapType.withKeyType(_strType);
        assertSame("Should return this when key type is identical", mapType, identity);

        // Branch 2: different key type
        MapType modified = mapType.withKeyType(_intType);
        assertNotSame(mapType, modified);
        assertEquals(_intType, modified.getKeyType());
        assertEquals(_intType, modified.getContentType());
    }

    /*
     **********************************************************
     * Partition B: Boundary Value Analysis & Mutators
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testTypeAndValueHandlersAssignment() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        Object typeHandler = "TypeHandlerDummy";
        Object valHandler = "ValueHandlerDummy";

        MapType withTH = base.withTypeHandler(typeHandler);
        assertNotSame(base, withTH);
        assertEquals(typeHandler, withTH.getTypeHandler());
        assertNull(base.getTypeHandler());

        MapType withVH = base.withValueHandler(valHandler);
        assertNotSame(base, withVH);
        assertEquals(valHandler, withVH.getValueHandler());
        assertNull(base.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testContentHandlersPropagation() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        Object cTypeHandler = "ContentTypeHandler";
        Object cValueHandler = "ContentValueHandler";

        MapType withCTH = base.withContentTypeHandler(cTypeHandler);
        assertNotSame(base, withCTH);
        assertEquals(cTypeHandler, withCTH.getContentType().getTypeHandler());

        MapType withCVH = base.withContentValueHandler(cValueHandler);
        assertNotSame(base, withCVH);
        assertEquals(cValueHandler, withCVH.getContentType().getValueHandler());
    }

    @Test(timeout = 4000)
    public void testKeyHandlersPropagation() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        Object kTypeHandler = "KeyTypeHandler";
        Object kValueHandler = "KeyValueHandler";

        MapType withKTH = base.withKeyTypeHandler(kTypeHandler);
        assertNotSame(base, withKTH);
        assertEquals(kTypeHandler, withKTH.getKeyType().getTypeHandler());

        MapType withKVH = base.withKeyValueHandler(kValueHandler);
        assertNotSame(base, withKVH);
        assertEquals(kValueHandler, withKVH.getKeyType().getValueHandler());
    }

    /*
     **********************************************************
     * Partition C: Defect-Targeted Branch Zone (databind#1102)
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testDeprecatedConstructMustNotProduceNullBindings() {
        // Issue #1102: MapType.construct(Class, JavaType, JavaType) in defective versions
        // passed null for TypeBindings, causing getBindings() to be null and breaking
        // subsequent type parameter resolutions.
        @SuppressWarnings("deprecation")
        MapType mapType = MapType.construct(HashMap.class, _strType, _intType);

        assertNotNull("TypeBindings MUST NOT be null on MapType instance", mapType.getBindings());
        assertEquals("MapType must have 2 bound type variables", 2, mapType.getBindings().size());
        assertEquals("Bound key type must match", _strType, mapType.getBindings().getBoundType(0));
        assertEquals("Bound value type must match", _intType, mapType.getBindings().getBoundType(1));
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructRefinementIntegrity() {
        // Refinement depends critically on non-null bindings to properly resolve supertypes
        @SuppressWarnings("deprecation")
        MapType base = MapType.construct(LinkedHashMap.class, _strType, _intType);
        TypeBindings newBindings = TypeBindings.create(TreeMap.class, new JavaType[]{_strType, _intType});
        JavaType refined = base.refine(TreeMap.class, newBindings, null, null);

        assertNotNull(refined);
        assertEquals(TreeMap.class, refined.getRawClass());
        assertNotNull(refined.getBindings());
        assertEquals(2, refined.getBindings().size());
    }

    /*
     **********************************************************
     * Partition D: Exception, Protected & Narrow Paths
     **********************************************************
     */

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testNarrowPreservesState() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(Map.class, bindings, null, null, _strType, _intType);
        MapType configured = base.withTypeHandler("TH").withValueHandler("VH").withStaticTyping();

        JavaType narrowed = configured._narrow(HashMap.class);
        assertNotNull(narrowed);
        assertTrue(narrowed instanceof MapType);
        assertEquals(HashMap.class, narrowed.getRawClass());
        assertEquals("TH", narrowed.getTypeHandler());
        assertEquals("VH", narrowed.getValueHandler());
        assertTrue(narrowed.useStaticValues());
        assertEquals(_strType, ((MapType) narrowed).getKeyType());
        assertEquals(_intType, ((MapType) narrowed).getContentType());
    }

    @Test(timeout = 4000)
    public void testRefineMethod() {
        TypeBindings bindings = TypeBindings.create(Map.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(Map.class, bindings, null, null, _strType, _intType)
                .withTypeHandler("TH")
                .withValueHandler("VH");

        TypeBindings newBindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] interfaces = new JavaType[]{SimpleType.constructUnsafe(Cloneable.class)};

        JavaType refined = base.refine(HashMap.class, newBindings, superClass, interfaces);
        assertEquals(HashMap.class, refined.getRawClass());
        assertEquals("TH", refined.getTypeHandler());
        assertEquals("VH", refined.getValueHandler());
        assertEquals(superClass, refined.getSuperClass());
        assertArrayEquals(interfaces, refined.getInterfaces().toArray(new JavaType[0]));
    }

    private static class SubclassedMapType extends MapType {
        private static final long serialVersionUID = 1L;

        public SubclassedMapType(TypeBase base, JavaType keyT, JavaType valueT) {
            super(base, keyT, valueT);
        }
    }

    @Test(timeout = 4000)
    public void testProtectedConstructorViaSubclass() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType base = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        SubclassedMapType sub = new SubclassedMapType(base, _intType, _strType);
        assertEquals(HashMap.class, sub.getRawClass());
        assertEquals(_intType, sub.getKeyType());
        assertEquals(_strType, sub.getContentType());
    }

    /*
     **********************************************************
     * Partition E: Object Lifecycle & Contract Integrity
     **********************************************************
     */

    @Test(timeout = 4000)
    public void testToStringFormat() {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType mapType = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        String expected = "[map type; class java.util.HashMap, " + _strType + " -> " + _intType + "]";
        assertEquals(expected, mapType.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        TypeBindings bindings1 = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType map1 = MapType.construct(HashMap.class, bindings1, null, null, _strType, _intType);

        TypeBindings bindings2 = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType map2 = MapType.construct(HashMap.class, bindings2, null, null, _strType, _intType);

        TypeBindings bindingsDiff = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _longType});
        MapType mapDiffVal = MapType.construct(HashMap.class, bindingsDiff, null, null, _strType, _longType);

        TypeBindings bindingsDiffKey = TypeBindings.create(HashMap.class, new JavaType[]{_intType, _intType});
        MapType mapDiffKey = MapType.construct(HashMap.class, bindingsDiffKey, null, null, _intType, _intType);

        assertEquals("Self equals", map1, map1);
        assertEquals("Symmetric equals", map1, map2);
        assertEquals("Symmetric equals", map2, map1);
        assertEquals("Hash codes must match", map1.hashCode(), map2.hashCode());

        assertFalse("Different value type must not be equal", map1.equals(mapDiffVal));
        assertFalse("Different key type must not be equal", map1.equals(mapDiffKey));
        assertFalse("Null comparison", map1.equals(null));
        assertFalse("Different type comparison", map1.equals("aString"));
    }

    @Test(timeout = 4000)
    public void testJavaSerialization() throws Exception {
        TypeBindings bindings = TypeBindings.create(HashMap.class, new JavaType[]{_strType, _intType});
        MapType original = MapType.construct(HashMap.class, bindings, null, null, _strType, _intType);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        MapType deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (MapType) ois.readObject();
        }

        assertNotNull(deserialized);
        assertEquals(original.getRawClass(), deserialized.getRawClass());
        assertEquals(original.getKeyType(), deserialized.getKeyType());
        assertEquals(original.getContentType(), deserialized.getContentType());
        assertEquals(original, deserialized);
    }
}