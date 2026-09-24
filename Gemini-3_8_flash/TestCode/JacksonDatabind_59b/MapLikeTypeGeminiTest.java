package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.type.MapLikeType
 * Branch / Condition Coverage Plan:
 * 1. upgradeFrom(JavaType, JavaType, JavaType):
 *    - Branch: baseType instanceof TypeBase -> returns upgraded MapLikeType.
 *    - Branch: !(baseType instanceof TypeBase) -> throws IllegalArgumentException.
 * 2. construct(Class<?>, JavaType, JavaType):
 *    - Branch: rawType has 2 type parameters (vars.length == 2) -> creates non-empty TypeBindings.
 *    - Branch: rawType has != 2 type parameters (vars.length == 0, vars.length == 1) -> empty TypeBindings.
 * 3. _narrow(Class<?>):
 *    - Produces new MapLikeType with new raw class preserving key/value/handlers/static flag.
 * 4. withKeyType(JavaType):
 *    - Branch: keyType == _keyType -> returns this.
 *    - Branch: keyType != _keyType -> returns new MapLikeType with updated key type.
 * 5. withContentType(JavaType):
 *    - Branch: _valueType == contentType -> returns this.
 *    - Branch: _valueType != contentType -> returns new MapLikeType with updated value type.
 * 6. Handler Manipulations (withTypeHandler, withContentTypeHandler, withValueHandler, withContentValueHandler,
 *    withKeyTypeHandler, withKeyValueHandler):
 *    - Proper association of handlers to base, key, and value components.
 * 7. withStaticTyping():
 *    - Branch: _asStatic is true -> returns this.
 *    - Branch: _asStatic is false -> returns new MapLikeType with static typing flag set to true.
 * 8. refine(Class<?>, TypeBindings, JavaType, JavaType[]):
 *    - Replaces raw class, bindings, superClass, superInterfaces while preserving handlers and key/value.
 * 9. buildCanonicalName():
 *    - Branch: _keyType != null -> appends canonical key and value separated by comma.
 *    - Branch: _keyType == null -> only base class name without generic brackets.
 * 10. Container & Map Type Introspection:
 *     - isContainerType() -> always true.
 *     - isMapLikeType() -> always true.
 *     - isTrueMapType():
 *       * Branch: Map.class.isAssignableFrom(_class) -> true.
 *       * Branch: !Map.class.isAssignableFrom(_class) -> false.
 * 11. hasHandlers():
 *     - super.hasHandlers() == true -> returns true.
 *     - _valueType.hasHandlers() == true -> returns true.
 *     - _keyType.hasHandlers() == true -> returns true.
 *     - None have handlers -> returns false.
 * 12. Signature Extraction:
 *     - getErasedSignature(StringBuilder)
 *     - getGenericSignature(StringBuilder)
 * 13. equals(Object):
 *     - Branch: o == this -> true.
 *     - Branch: o == null -> false.
 *     - Branch: o.getClass() != getClass() -> false.
 *     - Branch: _class != other._class -> false.
 *     - Branch: !_keyType.equals(other._keyType) -> false.
 *     - Branch: !_valueType.equals(other._valueType) -> false.
 *     - Branch: all match -> true.
 * 14. Known Defect Targeting (JacksonDatabind #1384):
 *     - Verifies key refinement integrity, ensuring key handlers (such as custom key deserializers)
 *       are retained across withKeyValueHandler, withKeyType, and refine invocations.
 * ---------------------------------------------------------------------------------------------------------
 */
public class MapLikeTypeGeminiTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();
    private final JavaType _strType = _typeFactory.constructType(String.class);
    private final JavaType _intType = _typeFactory.constructType(Integer.class);
    private final JavaType _longType = _typeFactory.constructType(Long.class);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructWithTwoTypeVariables() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        assertNotNull(mlType);
        assertEquals(Map.class, mlType.getRawClass());
        assertEquals(_strType, mlType.getKeyType());
        assertEquals(_intType, mlType.getContentType());
        assertEquals(2, mlType.getBindings().size());
        assertTrue(mlType.isContainerType());
        assertTrue(mlType.isMapLikeType());
        assertTrue(mlType.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testConstructWithZeroTypeVariables() {
        MapLikeType mlType = MapLikeType.construct(String.class, _strType, _intType);
        assertNotNull(mlType);
        assertEquals(String.class, mlType.getRawClass());
        assertTrue(mlType.getBindings().isEmpty());
        assertFalse(mlType.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testConstructWithOneTypeVariable() {
        MapLikeType mlType = MapLikeType.construct(List.class, _strType, _intType);
        assertNotNull(mlType);
        assertEquals(List.class, mlType.getRawClass());
        assertTrue(mlType.getBindings().isEmpty());
        assertFalse(mlType.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testGetContentAndTypeHandlersDefault() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        assertNull(mlType.getContentValueHandler());
        assertNull(mlType.getContentTypeHandler());
        assertNull(mlType.getValueHandler());
        assertNull(mlType.getTypeHandler());
        assertFalse(mlType.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testSignaturesAndToString() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        String desc = mlType.toString();
        assertTrue(desc.contains("[map-like type; class java.util.Map"));
        assertTrue(desc.contains(_strType.toString()));
        assertTrue(desc.contains(_intType.toString()));

        StringBuilder erased = new StringBuilder();
        mlType.getErasedSignature(erased);
        assertEquals("Ljava/util/Map;", erased.toString());

        StringBuilder generic = new StringBuilder();
        mlType.getGenericSignature(generic);
        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;", generic.toString());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalName() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", mlType.toCanonical());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithKeyTypeIdentityAndChange() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);

        MapLikeType same = mlType.withKeyType(_strType);
        assertSame(mlType, same);

        MapLikeType modified = mlType.withKeyType(_longType);
        assertNotSame(mlType, modified);
        assertEquals(_longType, modified.getKeyType());
        assertEquals(_intType, modified.getContentType());
        assertEquals(Map.class, modified.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeIdentityAndChange() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);

        JavaType same = mlType.withContentType(_intType);
        assertSame(mlType, same);

        JavaType modified = mlType.withContentType(_longType);
        assertNotSame(mlType, modified);
        assertEquals(_strType, ((MapLikeType) modified).getKeyType());
        assertEquals(_longType, modified.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingIdentityAndChange() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        assertFalse(mlType.useStaticTyping());

        MapLikeType staticType = mlType.withStaticTyping();
        assertNotSame(mlType, staticType);
        assertTrue(staticType.useStaticTyping());
        assertTrue(staticType.getContentType().useStaticTyping());

        MapLikeType staticSame = staticType.withStaticTyping();
        assertSame(staticType, staticSame);
    }

    @Test(timeout = 4000)
    public void testNarrowSubclass() {
        MapLikeType mlType = MapLikeType.construct(Map.class, _strType, _intType);
        JavaType narrowed = mlType._narrow(HashMap.class);

        assertNotNull(narrowed);
        assertEquals(HashMap.class, narrowed.getRawClass());
        assertEquals(_strType, ((MapLikeType) narrowed).getKeyType());
        assertEquals(_intType, narrowed.getContentType());
    }

    @Test(timeout = 4000)
    public void testUpgradeFromWithNullComponentsCanonicalName() {
        JavaType baseType = _typeFactory.constructType(Object.class);
        MapLikeType nullComponentsType = MapLikeType.upgradeFrom(baseType, null, null);

        assertNotNull(nullComponentsType);
        assertNull(nullComponentsType.getKeyType());
        assertNull(nullComponentsType.getContentType());

        // Branches in buildCanonicalName where _keyType is null
        assertEquals(Object.class.getName(), nullComponentsType.toCanonical());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind #1384 & Handlers)
    // =========================================================================

    /**
     * Targets Jackson issue #1384 (Type refinement and key handler preservation).
     * Ensures that key handlers (custom key deserializers/serializers) and value handlers
     * are properly attached and preserved through refine and key-mutation steps.
     */
    @Test(timeout = 4000)
    public void testMapKeyRefinement1384() {
        MapLikeType mapType = MapLikeType.construct(Map.class, _strType, _intType);

        Object keyDeserializerHandler = "CustomKeyDeserializerHandler";
        MapLikeType withKeyValHandler = mapType.withKeyValueHandler(keyDeserializerHandler);

        assertNotNull(withKeyValHandler);
        assertTrue(withKeyValHandler.hasHandlers());
        assertEquals(keyDeserializerHandler, withKeyValHandler.getKeyType().getValueHandler());

        // Refine type to HashMap while preserving key/value handlers
        TypeBindings newBindings = TypeBindings.create(HashMap.class, _strType, _intType);
        JavaType refined = withKeyValHandler.refine(HashMap.class, newBindings, mapType, new JavaType[0]);

        assertEquals(HashMap.class, refined.getRawClass());
        assertTrue(refined.hasHandlers());
        assertEquals(keyDeserializerHandler, ((MapLikeType) refined).getKeyType().getValueHandler());

        // Replace key type with an already handled key type
        JavaType handledKey = _strType.withValueHandler("AnotherKeyHandler");
        MapLikeType replacedKeyType = ((MapLikeType) refined).withKeyType(handledKey);
        assertEquals("AnotherKeyHandler", replacedKeyType.getKeyType().getValueHandler());
        assertTrue(replacedKeyType.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testHandlerCombinationsOnHasHandlers() {
        MapLikeType base = MapLikeType.construct(Map.class, _strType, _intType);
        assertFalse(base.hasHandlers());

        MapLikeType withTypeH = base.withTypeHandler("TypeH");
        assertTrue(withTypeH.hasHandlers());
        assertEquals("TypeH", withTypeH.getTypeHandler());

        MapLikeType withValH = base.withValueHandler("ValH");
        assertTrue(withValH.hasHandlers());
        assertEquals("ValH", withValH.getValueHandler());

        MapLikeType withKeyTypeH = base.withKeyTypeHandler("KeyTypeH");
        assertTrue(withKeyTypeH.hasHandlers());
        assertEquals("KeyTypeH", withKeyTypeH.getKeyType().getTypeHandler());

        MapLikeType withContentValH = base.withContentValueHandler("ContentValH");
        assertTrue(withContentValH.hasHandlers());
        assertEquals("ContentValH", withContentValH.getContentValueHandler());

        MapLikeType withContentTypeH = base.withContentTypeHandler("ContentTypeH");
        assertTrue(withContentTypeH.hasHandlers());
        assertEquals("ContentTypeH", withContentTypeH.getContentTypeHandler());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUpgradeFromNonTypeBaseThrowsException() {
        JavaType foreignJavaType = new CustomNonTypeBaseJavaType(Map.class);
        try {
            MapLikeType.upgradeFrom(foreignJavaType, _strType, _intType);
            fail("Expected IllegalArgumentException when upgrading from non-TypeBase JavaType");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Can not upgrade from an instance of"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorNullKeyOrValueThrowsNpe() {
        try {
            new MapLikeType(Map.class, TypeBindings.emptyBindings(), null, null, null, _intType, null, null, false);
            fail("Expected NullPointerException when keyT is null in 9-arg constructor");
        } catch (NullPointerException expected) {
            // keyT.hashCode() triggers NPE as expected
        }

        try {
            new MapLikeType(Map.class, TypeBindings.emptyBindings(), null, null, _strType, null, null, null, false);
            fail("Expected NullPointerException when valueT is null in 9-arg constructor");
        } catch (NullPointerException expected) {
            // valueT.hashCode() triggers NPE as expected
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Equals & HashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        MapLikeType type1 = MapLikeType.construct(Map.class, _strType, _intType);
        MapLikeType type2 = MapLikeType.construct(Map.class, _strType, _intType);
        MapLikeType diffClass = MapLikeType.construct(HashMap.class, _strType, _intType);
        MapLikeType diffKey = MapLikeType.construct(Map.class, _longType, _intType);
        MapLikeType diffVal = MapLikeType.construct(Map.class, _strType, _longType);

        // Reflexive
        assertTrue(type1.equals(type1));

        // Symmetric & Equal
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertEquals(type1.hashCode(), type2.hashCode());

        // Null and alien types
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("SomeString"));
        assertFalse(type1.equals(_strType));

        // Asymmetric differences
        assertFalse(type1.equals(diffClass));
        assertFalse(type1.equals(diffKey));
        assertFalse(type1.equals(diffVal));
    }

    // =========================================================================
    // Helper Classes
    // =========================================================================

    /**
     * Dummy JavaType implementation that extends JavaType directly instead of TypeBase
     * to test the negative branch of MapLikeType.upgradeFrom(JavaType, JavaType, JavaType).
     */
    private static class CustomNonTypeBaseJavaType extends JavaType {
        private static final long serialVersionUID = 1L;

        protected CustomNonTypeBaseJavaType(Class<?> raw) {
            super(raw, 0, null, null, false);
        }

        @Override public JavaType withContentType(JavaType ct) { return this; }
        @Override public JavaType withTypeHandler(Object h) { return this; }
        @Override public JavaType withContentTypeHandler(Object h) { return this; }
        @Override public JavaType withValueHandler(Object h) { return this; }
        @Override public JavaType withContentValueHandler(Object h) { return this; }
        @Override public JavaType withStaticTyping() { return this; }
        @Override public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
        @Override public boolean isContainerType() { return false; }
        @Override public int containedTypeCount() { return 0; }
        @Override public JavaType containedType(int index) { return null; }
        @Deprecated
        @Override public String containedTypeName(int index) { return null; }
        @Override public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
        @Override public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
        @Override public String toString() { return "[CustomNonTypeBaseJavaType]"; }
        @Override public boolean equals(Object o) { return o == this; }
    }
}