package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnionType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic (constructor, chain getters, firstPreciser/nextPreciser delegates)
 * - Partition B: BVA on getTypeIfRefinable (null, empty, NAME, GETPROP, THIS, other)
 * - Partition C: Defect-targeted: getRestrictedByTypeOfResult with null/ALL/UNKNOWN, typeof "function" true/false
 * - Partition D: Exception/defensive: null arguments, illegal node types
 * - Partition E: Lifecycle: chain append and first link propagation
 * Key defect in caseObjectType for typeof "function" false returns null instead of type
 */
public class ChainableReverseAbstractInterpreterDeepseekTest {

    private static class TestChainable extends ChainableReverseAbstractInterpreter {
        TestChainable(CodingConvention convention, JSTypeRegistry registry) {
            super(convention, registry);
        }
        @Override
        public FlowScope getPreciserScopeKnowingConditionOutcome(Node condition,
                                                                  FlowScope blindScope,
                                                                  boolean outcome) {
            return blindScope;
        }
    }

    private final CodingConvention convention = new DefaultCodingConvention();
    private final JSTypeRegistry registry = new JSTypeRegistry(convention);
    private final TestChainable interpreter = new TestChainable(convention, registry);
    private final JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    private final JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    private final JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    private final JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    private final JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    private final JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    private final JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    private final JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    private final JSType u2uCtorType = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);

    // ================== Partition A: Core Functional Logic ==================

    @Test(timeout = 4000)
    public void testConstructorAndFirstLink() {
        assertNotNull("Interpreter should not be null", interpreter);
        assertSame("firstLink should be self", interpreter, interpreter.getFirst());
        // Chain append
        TestChainable second = new TestChainable(convention, registry);
        TestChainable last = (TestChainable) interpreter.append(second);
        assertSame("append returns last link", last, second);
        assertSame("firstLink propagated", interpreter.getFirst(), interpreter);
        assertSame("second's firstLink is first link", second.getFirst(), interpreter);
        // nextLink chain
        // No direct accessor for nextLink, but can test via firstPreciserScopeKnowingConditionOutcome delegation
    }

    @Test(timeout = 4000)
    public void testFirstPreciserScopeKnowingConditionOutcome() {
        // Dummy: first link calls getPreciserScopeKnowingConditionOutcome which returns blindScope
        FlowScope dummyScope = new DummyFlowScope();
        Node dummyCondition = new Node(Token.NAME, "x");
        FlowScope result = interpreter.firstPreciserScopeKnowingConditionOutcome(dummyCondition, dummyScope, true);
        assertSame("firstPreciser delegates to first link's method", dummyScope, result);
    }

    @Test(timeout = 4000)
    public void testNextPreciserScopeKnowingConditionOutcome() {
        // With only one link, nextPreciser should return blindScope because nextLink == null
        FlowScope dummyScope = new DummyFlowScope();
        Node dummyCondition = new Node(Token.NAME, "x");
        FlowScope result = interpreter.nextPreciserScopeKnowingConditionOutcome(dummyCondition, dummyScope, true);
        assertSame("nextPreciser with no next returns blindScope", dummyScope, result);
        // With chain, nextPreciser should call getPreciser on next link
        TestChainable second = new TestChainable(convention, registry);
        interpreter.append(second);
        FlowScope result2 = interpreter.nextPreciserScopeKnowingConditionOutcome(dummyCondition, dummyScope, false);
        assertSame("nextPreciser with next link returns that link's result", dummyScope, result2);
    }

    // ================== Partition B: BVA on getTypeIfRefinable ==================

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableNullNode() {
        assertNull("null node returns null", interpreter.getTypeIfRefinable(null, new DummyFlowScope()));
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableNameWithSlot() {
        DummyFlowScope scope = new DummyFlowScope();
        JSType slotType = numberType;
        scope.setSlotType("x", slotType);
        Node node = new Node(Token.NAME, "x");
        // Register type on node too? The method checks slot first, then node's own type.
        JSType result = interpreter.getTypeIfRefinable(node, scope);
        assertEquals("Should return slot type", slotType, result);
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableNameNoSlot() {
        DummyFlowScope scope = new DummyFlowScope();
        Node node = new Node(Token.NAME, "unknown");
        node.setJSType(stringType);
        JSType result = interpreter.getTypeIfRefinable(node, scope);
        assertEquals("Should return node's JSType when slot missing", stringType, result);
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableNameNoSlotNoJSType() {
        DummyFlowScope scope = new DummyFlowScope();
        Node node = new Node(Token.NAME, "unknown");
        assertNull("Should return null when slot and node type are null", interpreter.getTypeIfRefinable(node, scope));
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableGetPropQualified() {
        DummyFlowScope scope = new DummyFlowScope();
        // Create a GETPROP node with qualified name "a.b"
        Node object = new Node(Token.NAME, "a");
        Node prop = Node.newString(Token.STRING, "b");
        Node getProp = new Node(Token.GETPROP, object, prop);
        getProp.setQualifiedName("a.b");
        scope.setSlotType("a.b", booleanType);
        JSType result = interpreter.getTypeIfRefinable(getProp, scope);
        assertEquals("Should return slot type for qualified name", booleanType, result);
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableGetPropNoQualifiedName() {
        // GETPROP without qualifiedName should return null
        Node object = new Node(Token.NAME, "a");
        Node prop = Node.newString(Token.STRING, "b");
        Node getProp = new Node(Token.GETPROP, object, prop);
        getProp.setQualifiedName(null);
        assertNull("getTypeIfRefinable returns null for GETPROP without qualified name",
                interpreter.getTypeIfRefinable(getProp, new DummyFlowScope()));
    }

    @Test(timeout = 4000)
    public void testGetTypeIfRefinableThis() {
        Node thisNode = new Node(Token.THIS);
        assertNull("getTypeIfRefinable returns null for THIS",
                interpreter.getTypeIfRefinable(thisNode, new DummyFlowScope()));
    }

    // ================== Partition C: Defect-Targeted Branch Zone ==================

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultNullTypeEqualsFalse() {
        JSType result = interpreter.getRestrictedByTypeOfResult(null, "function", false);
        assertNull("When type is null and resultEqualsValue is false, should return null", result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultNullTypeEqualsTrue() {
        JSType result = interpreter.getRestrictedByTypeOfResult(null, "number", true);
        assertEquals("When type is null and resultEqualsValue true, should return native type for string",
                numberType, result);
        // Test with unsupported typeof value
        JSType result2 = interpreter.getRestrictedByTypeOfResult(null, "symbol", true);
        assertEquals("Unsupported typeof value returns CHECKED_UNKNOWN_TYPE",
                registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE), result2);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultAllTypeNotFunction() {
        // This is the core defect scenario: ALL_TYPE, typeof != "function"
        // Expected behavior: Union of all types except function. Since ALL_TYPE includes functions,
        // the correct result should be a union of non-function types.
        JSType result = interpreter.getRestrictedByTypeOfResult(allType, "function", false);
        // The buggy implementation returns allType unchanged (via caseTopType), but that includes functions.
        // The correct implementation should filter out function types.
        // For ALL_TYPE, the visitor's caseTopType returns the topType if resultEqualsValue false.
        // But that may be incorrect because ALL_TYPE includes functions.
        // We'll assert that the result is NOT allType (to detect the defect if it is unchanged)
        assertNotNull("Result should not be null", result);
        // The expected correct result is a union: (Object|boolean|number|string|null|void) but that's complicated.
        // For simplicity, we test that the result is not equal to allType (which would be a bug)
        assertNotSame("Result should not be ALL_TYPE (bug candidate)", allType, result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultUnknownTypeFunctionTrue() {
        JSType result = interpreter.getRestrictedByTypeOfResult(unknownType, "function", true);
        // UnknownType, typeof == "function": should become U2U_CONSTRUCTOR_TYPE via caseTopType since resultEqualsValue true
        assertEquals("UNKNOWN_TYPE + typeof function true => U2U_CONSTRUCTOR_TYPE",
                u2uCtorType, result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultFunctionTypeNotFunction() {
        JSType funcType = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE); // a function type
        JSType result = interpreter.getRestrictedByTypeOfResult(funcType, "function", false);
        assertNull("When known not function, function type should be removed (null)", result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultObjectTypeNotFunction() {
        // Object type (not subtype of function), typeof != "function"
        JSType result = interpreter.getRestrictedByTypeOfResult(objectType, "function", false);
        // The buggy code returns null for all object types when value=="function" and resultEqualsValue false.
        // Correct behavior should return objectType because Object is not necessarily a function.
        // We assert that the result is not null (to detect the defect if null).
        assertNotNull("Object type should not be removed when typeof != function (defect candidate)", result);
        // Additionally, the result should be objectType itself (or a subtype that is not a function)
        // Since we cannot know the exact subtype, we assert it's not null.
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultObjectTypeIsFunction() {
        // U2U_CONSTRUCTOR_TYPE is a subtype of Object and a function type.
        JSType ctorType = u2uCtorType;
        JSType result = interpreter.getRestrictedByTypeOfResult(ctorType, "function", false);
        // Since it is a function type, should be null when not function.
        assertNull("Function subtype should be removed when typeof != function", result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultNumberTypeNotFunction() {
        JSType result = interpreter.getRestrictedByTypeOfResult(numberType, "function", false);
        // numberType should be kept because it's not function
        assertEquals("Number type should be kept", numberType, result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultNullTypeNotObject() {
        JSType result = interpreter.getRestrictedByTypeOfResult(nullType, "object", false);
        assertNull("null should be null when typeof != object (since typeof null is 'object')", result);
    }

    // ================== Partition D: Exception & Defensive Guard Paths ==================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDeclareNameInScopeInvalidNode() {
        Node invalid = new Node(Token.ADD);
        interpreter.declareNameInScope(new DummyFlowScope(), invalid, numberType);
    }

    @Test(timeout = 4000)
    public void testDeclareNameInScopeThisDoesNothing() {
        Node thisNode = new Node(Token.THIS);
        DummyFlowScope scope = new DummyFlowScope();
        interpreter.declareNameInScope(scope, thisNode, numberType);
        // No exception is fine; we just verify it doesn't throw
    }

    @Test(timeout = 4000)
    public void testDeclareNameInScopeName() {
        DummyFlowScope scope = new DummyFlowScope();
        Node nameNode = new Node(Token.NAME, "x");
        interpreter.declareNameInScope(scope, nameNode, numberType);
        assertEquals("Slot type should be set", numberType, scope.getSlot("x").getType());
    }

    @Test(timeout = 4000)
    public void testDeclareNameInScopeGetProp() {
        DummyFlowScope scope = new DummyFlowScope();
        Node object = new Node(Token.NAME, "a");
        Node prop = Node.newString(Token.STRING, "b");
        Node getProp = new Node(Token.GETPROP, object, prop);
        getProp.setQualifiedName("a.b");
        interpreter.declareNameInScope(scope, getProp, booleanType);
        JSType inferred = scope.getSlot("a.b").getType();
        assertEquals("Qualified slot should be inferred", booleanType, inferred);
    }

    // ================== Partition E: Object Lifecycle & Contract Integrity ==================

    @Test(timeout = 4000)
    public void testChainAppendMultipleLinks() {
        TestChainable second = new TestChainable(convention, registry);
        TestChainable third = new TestChainable(convention, registry);
        interpreter.append(second);
        second.append(third);
        assertSame("first link preserved", interpreter.getFirst(), interpreter);
        assertSame("second's first", interpreter, second.getFirst());
        assertSame("third's first", interpreter, third.getFirst());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAppendWithNonNullNextLink() {
        TestChainable second = new TestChainable(convention, registry);
        TestChainable third = new TestChainable(convention, registry);
        interpreter.append(second);
        // Attempt to append third to second, but second already has nextLink from first append? Actually append sets nextLink to the link being appended.
        // second's nextLink is null initially, so appending third to second is fine.
        // To trigger exception, we need a link with non-null nextLink. Create a chain that already has a next.
        TestChainable extra = new TestChainable(convention, registry);
        interpreter.append(extra); // now interpreter's nextLink is extra, extra's nextLink is null
        // Now try to append another link to extra? extra's nextLink is still null? Actually after append, extra's nextLink is null because it is the last.
        // To get non-null, we need to append something to extra first.
        TestChainable fourth = new TestChainable(convention, registry);
        extra.append(fourth); // now extra's nextLink is fourth
        // Now try to append fifth to extra, which already has nextLink -> exception
        TestChainable fifth = new TestChainable(convention, registry);
        extra.append(fifth); // This should throw IllegalArgumentException
    }

    // ================== Additional coverage for restrict visitors ==================

    @Test(timeout = 4000)
    public void testGetRestrictedWithoutUndefined() {
        UnionType union = (UnionType) registry.createUnionType(numberType, voidType, nullType);
        JSType result = interpreter.getRestrictedWithoutUndefined(union);
        assertNotNull(result);
        // Should be union of number and null (undefined removed)
        JSType expected = registry.createUnionType(numberType, nullType);
        assertTrue("Result should be equivalent to union without void", result.isEquivalentTo(expected));
    }

    @Test(timeout = 4000)
    public void testGetRestrictedWithoutNull() {
        UnionType union = (UnionType) registry.createUnionType(numberType, voidType, nullType);
        JSType result = interpreter.getRestrictedWithoutNull(union);
        JSType expected = registry.createUnionType(numberType, voidType);
        assertTrue("Result should be equivalent to union without null", result.isEquivalentTo(expected));
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultAllTypeFunctionTrue() {
        JSType result = interpreter.getRestrictedByTypeOfResult(allType, "function", true);
        // ALL_TYPE, typeof == function -> should be U2U_CONSTRUCTOR_TYPE via caseTopType
        assertEquals(u2uCtorType, result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultAllTypeNotObject() {
        JSType result = interpreter.getRestrictedByTypeOfResult(allType, "object", false);
        // Not object - should return allType unchanged (buggy for null removal? but that's separate)
        assertNotNull(result);
        assertSame("For ALL_TYPE and not object, should return ALL_TYPE", allType, result);
    }

    @Test(timeout = 4000)
    public void testGetRestrictedByTypeOfResultNoType() {
        JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
        JSType result = interpreter.getRestrictedByTypeOfResult(noType, "number", true);
        // NO_TYPE should be returned as NO_TYPE
        assertEquals(noType, result);
    }

    // Helper to simulate a minimal FlowScope
    private static class DummyFlowScope implements FlowScope {
        private java.util.Map<String, JSType> slots = new java.util.HashMap<>();

        @Override
        public StaticSlot<JSType> getSlot(String name) {
            JSType type = slots.get(name);
            if (type != null) {
                final String fName = name;
                return new StaticSlot<JSType>() {
                    @Override
                    public String getName() { return fName; }
                    @Override
                    public JSType getType() { return slots.get(fName); }
                    @Override
                    public boolean isTypeInferred() { return true; }
                };
            }
            return null;
        }

        @Override
        public StaticSlot<JSType> getOwnSlot(String name) { return getSlot(name); }

        @Override
        public FlowScope createChildFlowScope() { return this; }

        @Override
        public FlowScope createChildFlowScope(Node node) { return this; }

        @Override
        public void inferSlotType(String symbol, JSType type) {
            slots.put(symbol, type);
        }

        @Override
        public void inferQualifiedSlot(Node node, String qualifiedName, JSType origType, JSType newType) {
            slots.put(qualifiedName, newType);
        }

        public void setSlotType(String name, JSType type) {
            slots.put(name, type);
        }
    }
}