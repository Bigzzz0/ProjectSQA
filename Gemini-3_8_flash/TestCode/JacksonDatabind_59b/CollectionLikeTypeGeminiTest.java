package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.type.CollectionLikeType
 *
 * Decision / Condition Coverage Targets:
 * 1. construct(Class, JavaType):
 *    - vars == null || vars.length != 1 -> TypeBindings.emptyBindings() (e.g. String.class, Map.class)
 *    - vars != null && vars.length == 1 -> TypeBindings.create(rawType, elemT) (e.g. List.class)
 * 2. upgradeFrom(JavaType, JavaType):
 *    - baseType instanceof TypeBase -> returns new CollectionLikeType
 *    - !(baseType instanceof TypeBase) -> throws IllegalArgumentException
 * 3. withContentType(JavaType):
 *    - _elementType == contentType -> returns this
 *    - _elementType != contentType -> returns new CollectionLikeType
 * 4. withStaticTyping():
 *    - _asStatic == true -> returns this
 *    - _asStatic == false -> returns new CollectionLikeType with static typing flag set
 * 5. hasHandlers():
 *    - super.hasHandlers() == true -> true
 *    - super.hasHandlers() == false && _elementType.hasHandlers() == true -> true
 *    - super.hasHandlers() == false && _elementType.hasHandlers() == false -> false
 * 6. isTrueCollectionType():
 *    - Collection.class.isAssignableFrom(_class) == true (e.g. ArrayList, List) -> true
 *    - Collection.class.isAssignableFrom(_class) == false (e.g. String, Object) -> false
 * 7. equals(Object):
 *    - o == this -> true
 *    - o == null -> false
 *    - o.getClass() != getClass() -> false
 *    - _class == other._class && _elementType.equals(other._elementType) -> true
 *    - _class != other._class -> false
 *    - _class == other._class && !_elementType.equals(other._elementType) -> false
 * 8. Defect-Targeted Zone (Jackson Issue #1384 / TypeRefinement):
 *    - Ensuring refine(...) and handler chains retain custom ValueHandlers, TypeHandlers,
 *      and ContentHandlers without dropping metadata required for deserializer resolution.
 */
public class CollectionLikeTypeGeminiTest {

    private final JavaType stringType = SimpleType.constructUnsafe(String.class);
    private final JavaType integerType = SimpleType.constructUnsafe(Integer.class);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructWithExplicitBindingsAndSuperTypes() {
        TypeBindings bindings = TypeBindings.create(ArrayList.class, stringType);
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[] { SimpleType.constructUnsafe(Cloneable.class) };

        CollectionLikeType type = CollectionLikeType.construct(
                ArrayList.class, bindings, superClass, superInterfaces, stringType);

        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(stringType, type.getContentType());
        assertEquals(superClass, type.getSuperClass());
        assertArrayEquals(superInterfaces, type.getInterfaces());
        assertTrue(type.isContainerType());
        assertTrue(type.isCollectionLikeType());
        assertTrue(type.isTrueCollectionType());
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
        assertNull(type.getContentValueHandler());
        assertNull(type.getContentTypeHandler());
        assertFalse(type.useStaticTyping());
        assertFalse(type.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeSameAndDifferent() {
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, stringType);

        // Branch: _elementType == contentType -> returns this
        JavaType sameType = type.withContentType(stringType);
        assertSame(type, sameType);

        // Branch: _elementType != contentType -> returns new instance
        JavaType differentType = type.withContentType(integerType);
        assertNotSame(type, differentType);
        assertEquals(integerType, differentType.getContentType());
        assertEquals(ArrayList.class, differentType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingIdempotence() {
        CollectionLikeType dynamicType = CollectionLikeType.construct(ArrayList.class, stringType);
        assertFalse(dynamicType.useStaticTyping());

        // Branch: _asStatic == false -> new instance with static typing
        CollectionLikeType staticType = dynamicType.withStaticTyping();
        assertNotSame(dynamicType, staticType);
        assertTrue(staticType.useStaticTyping());
        assertTrue(staticType.getContentType().useStaticTyping());

        // Branch: _asStatic == true -> returns this
        CollectionLikeType idempotent = staticType.withStaticTyping();
        assertSame(staticType, idempotent);
    }

    @Test(timeout = 4000)
    public void testNarrowDeprecatedMethod() {
        CollectionLikeType base = CollectionLikeType.construct(List.class, stringType);
        JavaType narrowed = base._narrow(LinkedList.class);

        assertNotNull(narrowed);
        assertEquals(LinkedList.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getContentType());
        assertTrue(narrowed.isCollectionLikeType());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructWithDifferentTypeParameterArities() {
        // rawType has 1 type parameter (List<E>) -> TypeBindings created
        CollectionLikeType singleVar = CollectionLikeType.construct(List.class, stringType);
        assertNotNull(singleVar.getBindings());
        assertEquals(1, singleVar.getBindings().size());
        assertEquals(stringType, singleVar.getBindings().getBoundType(0));

        // rawType has 0 type parameters (String) -> emptyBindings
        CollectionLikeType zeroVar = CollectionLikeType.construct(String.class, stringType);
        assertNotNull(zeroVar.getBindings());
        assertTrue(zeroVar.getBindings().isEmpty());
        assertFalse(zeroVar.isTrueCollectionType());

        // rawType has 2 type parameters (Map<K, V>) -> emptyBindings
        CollectionLikeType twoVars = CollectionLikeType.construct(Map.class, stringType);
        assertNotNull(twoVars.getBindings());
        assertTrue(twoVars.getBindings().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSignaturesAndCanonicalRepresentation() {
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, stringType);

        StringBuilder erasedSb = new StringBuilder();
        type.getErasedSignature(erasedSb);
        assertEquals("Ljava/util/ArrayList;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        type.getGenericSignature(genericSb);
        assertEquals("Ljava/util/ArrayList<Ljava/lang/String;>;", genericSb.toString());

        String canonical = type.toCanonical();
        assertEquals("java.util.ArrayList<java.lang.String>", canonical);
        assertEquals(canonical, type.buildCanonicalName());

        String toString = type.toString();
        assertTrue(toString.startsWith("[collection-like type; class java.util.ArrayList, contains "));
    }

    @Test(timeout = 4000)
    public void testIsTrueCollectionTypeBoundary() {
        // Real collection subtype
        CollectionLikeType realColl = CollectionLikeType.construct(Collection.class, stringType);
        assertTrue(realColl.isTrueCollectionType());

        // Pseudo/custom collection-like class not implementing java.util.Collection
        CollectionLikeType fakeColl = CollectionLikeType.construct(Object.class, stringType);
        assertFalse(fakeColl.isTrueCollectionType());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Issue #1384 & Handlers)
    // =========================================================================

    /**
     * Targets Jackson #1384 defect pattern:
     * Verifies that refining a CollectionLikeType or modifying handlers correctly
     * preserves both container-level and content-level handlers (ValueHandler, TypeHandler).
     * Loss of these handlers causes deserializer resolution failure ("Can not find a ... deserializer").
     */
    @Test(timeout = 4000)
    public void testDefect1384RefineAndHandlerRetention() {
        Object collValHandler = "COLL_VAL_HANDLER";
        Object collTypeHandler = "COLL_TYPE_HANDLER";
        Object elemValHandler = "ELEM_VAL_HANDLER";
        Object elemTypeHandler = "ELEM_TYPE_HANDLER";

        CollectionLikeType original = CollectionLikeType.construct(List.class, stringType)
                .withValueHandler(collValHandler)
                .withTypeHandler(collTypeHandler)
                .withContentValueHandler(elemValHandler)
                .withContentTypeHandler(elemTypeHandler);

        // Verify handler state before refinement
        assertTrue(original.hasHandlers());
        assertEquals(collValHandler, original.getValueHandler());
        assertEquals(collTypeHandler, original.getTypeHandler());
        assertEquals(elemValHandler, original.getContentValueHandler());
        assertEquals(elemTypeHandler, original.getContentTypeHandler());

        // Perform refinement to a concrete subclass (e.g. ArrayList)
        TypeBindings newBindings = TypeBindings.create(ArrayList.class, stringType);
        JavaType refined = original.refine(ArrayList.class, newBindings,
                SimpleType.constructUnsafe(Object.class), new JavaType[0]);

        assertTrue(refined instanceof CollectionLikeType);
        CollectionLikeType refinedColl = (CollectionLikeType) refined;

        // Verify handlers are NOT dropped or corrupted during refinement
        assertEquals(ArrayList.class, refinedColl.getRawClass());
        assertEquals(collValHandler, refinedColl.getValueHandler());
        assertEquals(collTypeHandler, refinedColl.getTypeHandler());
        assertEquals(elemValHandler, refinedColl.getContentValueHandler());
        assertEquals(elemTypeHandler, refinedColl.getContentTypeHandler());
        assertTrue(refinedColl.hasHandlers());
        assertEquals(stringType.getRawClass(), refinedColl.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testHasHandlersAllCombinations() {
        CollectionLikeType plain = CollectionLikeType.construct(ArrayList.class, stringType);
        assertFalse(plain.hasHandlers());

        // Super has value handler
        CollectionLikeType withVal = plain.withValueHandler("val");
        assertTrue(withVal.hasHandlers());
        assertEquals("val", withVal.getValueHandler());

        // Super has type handler
        CollectionLikeType withTypeH = plain.withTypeHandler("type");
        assertTrue(withTypeH.hasHandlers());
        assertEquals("type", withTypeH.getTypeHandler());

        // Element has value handler
        CollectionLikeType withElemVal = plain.withContentValueHandler("elemVal");
        assertTrue(withElemVal.hasHandlers());
        assertEquals("elemVal", withElemVal.getContentValueHandler());

        // Element has type handler
        CollectionLikeType withElemType = plain.withContentTypeHandler("elemType");
        assertTrue(withElemType.hasHandlers());
        assertEquals("elemType", withElemType.getContentTypeHandler());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUpgradeFromValidTypeBase() {
        SimpleType base = SimpleType.constructUnsafe(ArrayList.class);
        CollectionLikeType upgraded = CollectionLikeType.upgradeFrom(base, stringType);

        assertNotNull(upgraded);
        assertEquals(ArrayList.class, upgraded.getRawClass());
        assertEquals(stringType, upgraded.getContentType());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUpgradeFromInvalidNonTypeBaseThrowsException() {
        // Construct a direct JavaType implementation that is NOT a TypeBase
        JavaType nonTypeBase = new JavaType(String.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;
            @Override public JavaType withContentType(JavaType contentType) { return this; }
            @Override public JavaType withTypeHandler(Object h) { return this; }
            @Override public JavaType withContentTypeHandler(Object h) { return this; }
            @Override public JavaType withValueHandler(Object h) { return this; }
            @Override public JavaType withContentValueHandler(Object h) { return this; }
            @Override public JavaType withStaticTyping() { return this; }
            @Override public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override protected JavaType _narrow(Class<?> subclass) { return this; }
            @Override public boolean isContainerType() { return false; }
            @Override public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override public String toString() { return ""; }
            @Override public boolean equals(Object o) { return o == this; }
        };

        // Branch: !(baseType instanceof TypeBase) -> throws IllegalArgumentException
        CollectionLikeType.upgradeFrom(nonTypeBase, stringType);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructWithNullElementTypeThrowsNpe() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        // elemT.hashCode() in constructor triggers NPE when elemT is null
        CollectionLikeType.construct(ArrayList.class, bindings, null, null, null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        CollectionLikeType type1 = CollectionLikeType.construct(ArrayList.class, stringType);
        CollectionLikeType type1Same = CollectionLikeType.construct(ArrayList.class, stringType);
        CollectionLikeType typeDiffClass = CollectionLikeType.construct(LinkedList.class, stringType);
        CollectionLikeType typeDiffElem = CollectionLikeType.construct(ArrayList.class, integerType);

        // Reflexivity
        assertTrue(type1.equals(type1));

        // Symmetry & HashCode consistency
        assertTrue(type1.equals(type1Same));
        assertTrue(type1Same.equals(type1));
        assertEquals(type1.hashCode(), type1Same.hashCode());

        // Non-nullity
        assertFalse(type1.equals(null));

        // Different class type comparison
        assertFalse(type1.equals("NotAJavaType"));

        // Different raw classes
        assertFalse(type1.equals(typeDiffClass));

        // Different element types
        assertFalse(type1.equals(typeDiffElem));
    }
}