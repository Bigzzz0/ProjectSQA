package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.fasterxml.jackson.databind.type.CollectionType
 *
 * 1. Branch Coverage:
 *    - withContentType(JavaType):
 *        * Branch (_elementType == contentType) -> returns 'this'
 *        * Branch (_elementType != contentType) -> returns new CollectionType instance
 *    - withStaticTyping():
 *        * Branch (_asStatic == true) -> returns 'this'
 *        * Branch (_asStatic == false) -> returns new CollectionType instance with static typing on both self & element
 *    - withTypeHandler(Object): Sets type handler on collection
 *    - withContentTypeHandler(Object): Propagates type handler down to element type
 *    - withValueHandler(Object): Sets value handler on collection
 *    - withContentValueHandler(Object): Propagates value handler down to element type
 *    - refine(Class<?>, TypeBindings, JavaType, JavaType[]): Refines base class, bindings, supers while preserving handlers & static flag
 *    - _narrow(Class<?>): Creates narrowed subclass resetting value & type handlers, keeping static flag
 *    - toString(): String representation formatting "[collection type; class <name>, contains <elem>]"
 *
 * 2. Defect Analysis (Jackson Databind #1102 / Defects4J):
 *    - Fault: Deprecated construct(Class<?> rawType, JavaType elemT) passed 'null' for TypeBindings instead
 *      of fabricating bindings via TypeBindings.create(rawType, elemT).
 *    - Consequence: When refining or resolving concrete collection subtypes, type bindings were empty/missing,
 *      causing deserialization to lose type parameter information and default to LinkedHashMap / Object.
 *    - Target Test: Partition C asserts that construct(ArrayList.class, elemT) produces non-empty TypeBindings
 *      with size = 1 and bound type equal to elemT.
 */
public class CollectionTypeGeminiTest {

    private final JavaType stringType = SimpleType.constructUnsafe(String.class);
    private final JavaType intType = SimpleType.constructUnsafe(Integer.class);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructFullParameters() {
        TypeBindings bindings = TypeBindings.create(ArrayList.class, new JavaType[]{stringType});
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[]{SimpleType.constructUnsafe(Collection.class)};

        CollectionType collType = CollectionType.construct(
                ArrayList.class, bindings, superClass, superInterfaces, stringType);

        assertNotNull(collType);
        assertEquals(ArrayList.class, collType.getRawClass());
        assertEquals(stringType, collType.getContentType());
        assertEquals(bindings, collType.getBindings());
        assertEquals(superClass, collType.getSuperClass());
        assertNotNull(collType.getInterfaces());
        assertEquals(1, collType.getInterfaces().length);
        assertEquals(superInterfaces[0], collType.getInterfaces()[0]);
        assertFalse(collType.isContainerType());
        assertTrue(collType.isCollectionLikeType());
        assertNull(collType.getValueHandler());
        assertNull(collType.getTypeHandler());
        assertFalse(collType.useStaticValues());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerAndValueHandler() {
        CollectionType base = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);

        Object typeH = "customTypeHandler";
        CollectionType withT = base.withTypeHandler(typeH);
        assertNotSame(base, withT);
        assertEquals(typeH, withT.getTypeHandler());
        assertNull(withT.getContentType().getTypeHandler());

        Object valH = "customValueHandler";
        CollectionType withV = base.withValueHandler(valH);
        assertNotSame(base, withV);
        assertEquals(valH, withV.getValueHandler());
        assertNull(withV.getContentType().getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandlerAndContentValueHandler() {
        CollectionType base = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);

        Object elemTypeH = "elemTypeHandler";
        CollectionType withCT = base.withContentTypeHandler(elemTypeH);
        assertNotSame(base, withCT);
        assertNull(withCT.getTypeHandler());
        assertEquals(elemTypeH, withCT.getContentType().getTypeHandler());

        Object elemValH = "elemValueHandler";
        CollectionType withCV = base.withContentValueHandler(elemValH);
        assertNotSame(base, withCV);
        assertNull(withCV.getValueHandler());
        assertEquals(elemValH, withCV.getContentType().getValueHandler());
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        CollectionType collType = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);
        String expected = "[collection type; class java.util.ArrayList, contains " + stringType + "]";
        assertEquals(expected, collType.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithContentTypeIdentityBranch() {
        CollectionType collType = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);

        // Branch 1: Same content type -> returns this
        assertSame(collType, collType.withContentType(stringType));

        // Branch 2: Different content type -> returns new instance with updated content type
        JavaType updated = collType.withContentType(intType);
        assertNotSame(collType, updated);
        assertEquals(intType, updated.getContentType());
        assertEquals(ArrayList.class, updated.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingIdentityBranch() {
        CollectionType collType = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);
        assertFalse(collType.useStaticValues());

        // Branch 1: currently non-static -> returns new static type
        CollectionType staticColl = collType.withStaticTyping();
        assertNotSame(collType, staticColl);
        assertTrue(staticColl.useStaticValues());
        assertTrue(staticColl.getContentType().useStaticValues());

        // Branch 2: already static -> returns this
        assertSame(staticColl, staticColl.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testHandlersPreservedAcrossOperations() {
        Object vHandler = new Object();
        Object tHandler = new Object();
        CollectionType collType = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType)
                .withValueHandler(vHandler)
                .withTypeHandler(tHandler);

        CollectionType staticTyped = collType.withStaticTyping();
        assertEquals(vHandler, staticTyped.getValueHandler());
        assertEquals(tHandler, staticTyped.getTypeHandler());

        CollectionType diffContent = (CollectionType) collType.withContentType(intType);
        assertEquals(vHandler, diffContent.getValueHandler());
        assertEquals(tHandler, diffContent.getTypeHandler());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Databind Issue #1102)
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructFabricatesTypeBindingsIssue1102() {
        // Targets known defect where construct(Class, JavaType) passed null TypeBindings,
        // causing missing type parameters when refining into concrete collection types.
        CollectionType collType = CollectionType.construct(ArrayList.class, stringType);

        assertNotNull("CollectionType must be constructed successfully", collType);
        assertEquals(ArrayList.class, collType.getRawClass());
        assertEquals(stringType, collType.getContentType());

        TypeBindings bindings = collType.getBindings();
        assertNotNull("TypeBindings must not be null for parameterized collection", bindings);
        assertFalse("TypeBindings must not be empty for ArrayList<E>", bindings.isEmpty());
        assertEquals("TypeBindings must have 1 bound parameter for ArrayList<E>", 1, bindings.size());
        assertEquals("Bound type must match the provided element type", stringType, bindings.getBoundType(0));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths / Subclassing
    // =========================================================================

    @Test(timeout = 4000)
    public void testNarrowResetsHandlersPreservingStatic() {
        CollectionType collType = CollectionType.construct(List.class,
                TypeBindings.emptyBindings(), null, null, stringType)
                .withValueHandler("valH")
                .withTypeHandler("typeH");

        JavaType narrowed = collType._narrow(LinkedList.class);
        assertEquals(LinkedList.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getContentType());
        assertNull("Narrowed type must reset valueHandler to null", narrowed.getValueHandler());
        assertNull("Narrowed type must reset typeHandler to null", narrowed.getTypeHandler());
        assertFalse(narrowed.useStaticValues());

        // Test _narrow on static type
        CollectionType staticColl = collType.withStaticTyping();
        JavaType narrowedStatic = staticColl._narrow(LinkedList.class);
        assertTrue("Narrowed type must retain static typing flag", narrowedStatic.useStaticValues());
    }

    @Test(timeout = 4000)
    public void testProtectedBaseConstructor() {
        CollectionType base = CollectionType.construct(ArrayList.class,
                TypeBindings.emptyBindings(), null, null, stringType);

        // Protected constructor invocation via package-private access
        CollectionType copy = new CollectionType(base, intType);
        assertEquals(ArrayList.class, copy.getRawClass());
        assertEquals(intType, copy.getContentType());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testRefineMethod() {
        TypeBindings bindings1 = TypeBindings.emptyBindings();
        CollectionType collType = CollectionType.construct(List.class,
                bindings1, null, null, stringType)
                .withValueHandler("val")
                .withTypeHandler("typ");

        TypeBindings bindings2 = TypeBindings.create(ArrayList.class, new JavaType[]{stringType});
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[]{SimpleType.constructUnsafe(Collection.class)};

        JavaType refined = collType.refine(ArrayList.class, bindings2, superClass, superInterfaces);

        assertTrue(refined instanceof CollectionType);
        assertEquals(ArrayList.class, refined.getRawClass());
        assertEquals(bindings2, refined.getBindings());
        assertEquals(superClass, refined.getSuperClass());
        assertEquals(1, refined.getInterfaces().length);
        assertEquals(stringType, refined.getContentType());
        assertEquals("val", refined.getValueHandler());
        assertEquals("typ", refined.getTypeHandler());
        assertFalse(refined.useStaticValues());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        TypeBindings b1 = TypeBindings.emptyBindings();
        CollectionType ct1 = CollectionType.construct(ArrayList.class, b1, null, null, stringType);
        CollectionType ct2 = CollectionType.construct(ArrayList.class, b1, null, null, stringType);
        CollectionType ctDiffElem = CollectionType.construct(ArrayList.class, b1, null, null, intType);
        CollectionType ctDiffRaw = CollectionType.construct(LinkedList.class, b1, null, null, stringType);

        assertEquals(ct1, ct2);
        assertEquals(ct1.hashCode(), ct2.hashCode());
        assertNotEquals(ct1, ctDiffElem);
        assertNotEquals(ct1, ctDiffRaw);
        assertNotEquals(ct1, null);
        assertNotEquals(ct1, "someString");
    }
}