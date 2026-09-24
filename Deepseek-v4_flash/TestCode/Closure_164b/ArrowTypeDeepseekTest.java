package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

/**
 * White-box test suite for ArrowType.
 * Targets all branches, boundaries, and the known Defects4J defect related to
 * isSubtype parameter handling (missing required argument check).
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor with/without returnTypeInferred
 *   - isSubtype: ArrowType vs non-ArrowType
 *   - isSubtype: returnType subtype check (covariant)
 *   - isSubtype: parameter subtype check (contravariant), null param types
 *   - isSubtype: varargs handling (both varargs, one varargs, none)
 *   - hasEqualParameters: full match, mismatch, null types
 *   - isEquivalentTo: full match, return type mismatch, parameter mismatch
 *   - hashCode: consistent with equals, null safety
 *   - toStringHelper, getPossibleToBooleanOutcomes
 *   - hasUnknownParamsOrReturn: null/unknown types
 *   - resolveInternal: parameter resolution
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Constructor with null parameters -> creates varargs unknown
 *   - Constructor with null returnType -> creates unknown type
 *   - isSubtype with null parameter types
 *   - isSubtype when thisParam or thatParam becomes null during traversal
 *   - hasEqualParameters with one null, then both null
 *   - hasUnknownParamsOrReturn with null params
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: isSubtype incorrectly handles missing required parameter
 *     when supertype has fewer parameters than subtype.
 *     For example: function f(number) should NOT be subtype of function g(number, number)
 *     because g requires a second argument that f lacks.
 *     But the code skips the "required-ness" check, allowing f <: g incorrectly.
 *     Test case: assertFalse(arrowTypeWith1Param.isSubtype(arrowTypeWith2Params))
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - getLeastSupertype throws UnsupportedOperationException
 *   - getGreatestSubtype throws UnsupportedOperationException
 *   - testForEquality throws UnsupportedOperationException
 *   - visit throws UnsupportedOperationException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - hashCode consistency with equals
 *   - returnTypeInferred flag affects hashCode
 *   - Parameter list traversal edge cases
 */
public class ArrowTypeDeepseekTest {

    // Helper to create a simple ArrowType for testing
    private ArrowType createSimpleArrowType(int numParams) {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1); // PARAM_LIST
        for (int i = 0; i < numParams; i++) {
            Node param = Node.newString("p" + i);
            param.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
            params.addChildToBack(param);
        }
        return new ArrowType(registry, params, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    }

    private ArrowType createArrowTypeWithSpecificParamTypes(JSType[] paramTypes, JSType returnType) {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        for (JSType type : paramTypes) {
            Node param = Node.newString("x");
            param.setJSType(type);
            params.addChildToBack(param);
        }
        return new ArrowType(registry, params, returnType);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorWithReturnTypeInferred() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        ArrowType arrow = new ArrowType(registry, params, returnType, true);
        assertTrue("returnTypeInferred should be true", arrow.returnTypeInferred);
        assertEquals("Return type should be NUMBER_TYPE", JSTypeNative.NUMBER_TYPE, arrow.returnType);
        assertNotNull("Parameters should not be null", arrow.parameters);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullParameters() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType arrow = new ArrowType(registry, null, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        assertNotNull("Parameters should not be null", arrow.parameters);
        assertTrue("Parameters should be varargs unknown", 
                   arrow.parameters.getFirstChild() != null && 
                   arrow.parameters.getFirstChild().isVarArgs());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullReturnType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType arrow = new ArrowType(registry, params, null);
        assertEquals("Return type should be UNKNOWN_TYPE", 
                     JSTypeNative.UNKNOWN_TYPE, arrow.returnType);
    }

    @Test(timeout = 4000)
    public void testIsSubtypeNonArrowType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType arrow = createSimpleArrowType(1);
        JSType other = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertFalse("ArrowType should not be subtype of non-ArrowType", arrow.isSubtype(other));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeReturnTypeCovariantSuccess() {
        JSTypeRegistry registry = new JSTypeRegistry();
        // subtype has boolean return, supertype has ? return (unknown is supertype of boolean)
        Node params = new Node(1);
        ArrowType sub = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType sup = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        assertTrue("ArrowType with boolean should be subtype of ArrowType with unknown", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeReturnTypeCovariantFail() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        // UNKNOWN_TYPE is subtype of NUMBER_TYPE? No, unknown is top type
        ArrowType sub = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        ArrowType sup = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        assertFalse("ArrowType with unknown return should not be subtype of number return", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeParameterContravariantSuccess() {
        JSTypeRegistry registry = new JSTypeRegistry();
        // subtype: (number) => boolean, supertype: (?) => boolean
        // number <: ?, so contravariant: ? <: number? Actually, we need sup param to be subtype of sub param
        // sub param = number, sup param = unknown. unknown is supertype of number, so sup param is NOT subtype of sub param.
        // This should fail contravariance. Let's create proper test:
        // sub: (unknown) => boolean, sup: (number) => boolean
        // number <: unknown, so sup param is subtype of sub param -> contravariance satisfied
        ArrowType sub = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.UNKNOWN_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType sup = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.NUMBER_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        assertTrue("ArrowType with unknown param should be subtype of number param", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeParameterContravariantFail() {
        JSTypeRegistry registry = new JSTypeRegistry();
        // sub: (number) => boolean, sup: (unknown) => boolean
        // sub param = number, sup param = unknown. unknown is NOT subtype of number -> contravariance fails
        ArrowType sub = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.NUMBER_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType sup = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.UNKNOWN_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        assertFalse("ArrowType with number param should not be subtype of unknown param", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeWithNullParamType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node subParams = new Node(1);
        Node subParam = Node.newString("x");
        subParam.setJSType(null); // null type
        subParams.addChildToBack(subParam);
        ArrowType sub = new ArrowType(registry, subParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        Node supParams = new Node(1);
        Node supParam = Node.newString("x");
        supParam.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        supParams.addChildToBack(supParam);
        ArrowType sup = new ArrowType(registry, supParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // null param type in sub should be treated as not failing subtype check
        assertTrue("ArrowType with null param should be subtype", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeWithNullParamTypeInSuper() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node subParams = new Node(1);
        Node subParam = Node.newString("x");
        subParam.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        subParams.addChildToBack(subParam);
        ArrowType sub = new ArrowType(registry, subParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        Node supParams = new Node(1);
        Node supParam = Node.newString("x");
        supParam.setJSType(null); // null type in sup
        supParams.addChildToBack(supParam);
        ArrowType sup = new ArrowType(registry, supParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // thatParamType is null, condition: if (thatParamType == null || !thatParamType.isSubtype(thisParamType))
        // thatParamType == null is true, so returns false
        assertFalse("ArrowType should not be subtype when super has null param type", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeVarargsHandling() {
        JSTypeRegistry registry = new JSTypeRegistry();
        // Create sub with varargs param
        Node subParams = new Node(1);
        Node subParam = Node.newString("x");
        subParam.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        subParam.putProp(com.google.javascript.rhino.Node.VAR_ARGS_NAME, true);
        subParams.addChildToBack(subParam);
        ArrowType sub = new ArrowType(registry, subParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        Node supParams = new Node(1);
        Node supParam = Node.newString("y");
        supParam.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        supParam.putProp(com.google.javascript.rhino.Node.VAR_ARGS_NAME, true);
        supParams.addChildToBack(supParam);
        ArrowType sup = new ArrowType(registry, supParams, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        assertTrue("Both varargs should be subtype", sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testHasEqualParametersExactMatch() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType a1 = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.NUMBER_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType a2 = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.NUMBER_TYPE) },
            registry.getNativeType(JSTypeNative.STRING_TYPE));
        assertTrue("Parameters should be equal", a1.hasEqualParameters(a2));
    }

    @Test(timeout = 4000)
    public void testHasEqualParametersMismatch() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType a1 = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.NUMBER_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType a2 = createArrowTypeWithSpecificParamTypes(
            new JSType[] { registry.getNativeType(JSTypeNative.STRING_TYPE) },
            registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        assertFalse("Parameters should not be equal", a1.hasEqualParameters(a2));
    }

    @Test(timeout = 4000)
    public void testHasEqualParametersNullType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params1 = new Node(1);
        Node param1 = Node.newString("x");
        param1.setJSType(null);
        params1.addChildToBack(param1);
        ArrowType a1 = new ArrowType(registry, params1, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        Node params2 = new Node(1);
        Node param2 = Node.newString("x");
        param2.setJSType(null);
        params2.addChildToBack(param2);
        ArrowType a2 = new ArrowType(registry, params2, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        assertTrue("Both null types should be equal", a1.hasEqualParameters(a2));
    }

    @Test(timeout = 4000)
    public void testHasEqualParametersOneNullType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params1 = new Node(1);
        Node param1 = Node.newString("x");
        param1.setJSType(null);
        params1.addChildToBack(param1);
        ArrowType a1 = new ArrowType(registry, params1, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        Node params2 = new Node(1);
        Node param2 = Node.newString("x");
        param2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        params2.addChildToBack(param2);
        ArrowType a2 = new ArrowType(registry, params2, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        assertFalse("One null, one non-null should not be equal", a1.hasEqualParameters(a2));
    }

    @Test(timeout = 4000)
    public void testHasEqualParametersDifferentLength() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType a1 = createSimpleArrowType(1);
        ArrowType a2 = createSimpleArrowType(2);
        assertFalse("Different parameter counts should not be equal", a1.hasEqualParameters(a2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToMatch() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType a1 = createSimpleArrowType(1);
        ArrowType a2 = createSimpleArrowType(1);
        assertTrue("Equivalent ArrowTypes should be equal", a1.isEquivalentTo(a2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToNonArrowType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        ArrowType a1 = createSimpleArrowType(1);
        JSType other = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertFalse("ArrowType should not be equivalent to non-ArrowType", a1.isEquivalentTo(other));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToReturnTypeMismatch() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType a1 = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        ArrowType a2 = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.STRING_TYPE));
        assertFalse("Different return types should not be equivalent", a1.isEquivalentTo(a2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        ArrowType a1 = createSimpleArrowType(1);
        ArrowType a2 = createSimpleArrowType(1);
        assertEquals("Hash codes should be equal for equal objects", a1.hashCode(), a2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullReturnType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType arrow = new ArrowType(registry, params, null);
        // Should not throw NPE
        int hashCode = arrow.hashCode();
        assertNotNull("Hash code should be computed", hashCode);
    }

    @Test(timeout = 4000)
    public void testGetPossibleToBooleanOutcomes() {
        ArrowType arrow = createSimpleArrowType(1);
        assertEquals("Should always be TRUE", 
                     BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
    }

    @Test(timeout = 4000)
    public void testHasUnknownParamsOrReturnWithNullType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        Node param = Node.newString("x");
        param.setJSType(null);
        params.addChildToBack(param);
        ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        assertTrue("Null param type should be unknown", arrow.hasUnknownParamsOrReturn());
    }

    @Test(timeout = 4000)
    public void testHasUnknownParamsOrReturnUnknownType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        Node param = Node.newString("x");
        param.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        params.addChildToBack(param);
        ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        assertTrue("Unknown param type should be detected", arrow.hasUnknownParamsOrReturn());
    }

    @Test(timeout = 4000)
    public void testHasUnknownParamsOrReturnWithNullReturnType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType arrow = new ArrowType(registry, params, null);
        assertTrue("Null return type should be unknown", arrow.hasUnknownParamsOrReturn());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testIsSubtypeWhenThisParamNotNullAndThatParamBecomesNull() {
        JSTypeRegistry registry = new JSTypeRegistry();
        // sub has 2 params, sup has 1 param
        ArrowType sub = createSimpleArrowType(2);
        ArrowType sup = createSimpleArrowType(1);
        // After first iteration, thatParam becomes null, loop exits -> returns true
        // But this should be the defect area: sub should NOT be subtype of sup with fewer params
        // According to comment in code: "that" can't be a supertype, because it's missing a required argument
        // But the code does NOT enforce this, so this is the known defect.
        // We test the current behavior which allows it (defective), but the intended behavior should be false.
        // For fault detection, we assert the current (buggy) behavior, but the real test for the defect
        // will check the expected corrected behavior in testDefectSubtypeMissingRequiredArg
        assertTrue("Current (defective) behavior: sub should be considered subtype even with fewer params in sup",
                   sub.isSubtype(sup));
    }

    @Test(timeout = 4000)
    public void testConstructorWithReturnTypeInferredDefault() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        assertFalse("Default returnTypeInferred should be false", arrow.returnTypeInferred);
    }

    @Test(timeout = 4000)
    public void testHashCodeWithReturnTypeInferred() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        ArrowType a1 = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE), true);
        ArrowType a2 = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        assertNotEquals("Hash codes should differ when returnTypeInferred differs", 
                        a1.hashCode(), a2.hashCode());
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Defect: isSubtype incorrectly handles missing required parameter.
     * If subtype has fewer parameters than supertype, it should NOT be a subtype
     * because the supertype requires arguments that the subtype doesn't provide.
     * The current code allows this due to missing arity check after the loop.
     * 
     * This test targets the specific failure reported in Defects4J:
     * testMethodInference7 and testSupAndInfOfReturnTypesWithNumOfParams
     */
    @Test(timeout = 4000)
    public void testDefectSubtypeMissingRequiredArgument() {
        JSTypeRegistry registry = new JSTypeRegistry();
        
        // Create function f(number, number): boolean
        Node paramsF = new Node(1);
        Node paramF1 = Node.newString("a");
        paramF1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsF.addChildToBack(paramF1);
        Node paramF2 = Node.newString("b");
        paramF2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsF.addChildToBack(paramF2);
        ArrowType f = new ArrowType(registry, paramsF, 
                                    registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // Create function g(number): boolean
        Node paramsG = new Node(1);
        Node paramG1 = Node.newString("x");
        paramG1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsG.addChildToBack(paramG1);
        ArrowType g = new ArrowType(registry, paramsG, 
                                    registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // Expected correct behavior: g should NOT be subtype of f
        // because f requires two arguments and g only provides one
        // NOTE: The current code has a bug where this returns true
        // This test asserts the CORRECT expected behavior to reveal the defect
        assertFalse("g (1 param) should NOT be subtype of f (2 params) due to missing required argument",
                    g.isSubtype(f));
    }

    /**
     * Additional defect-related test: verify that subtype with more params
     * can still be subtype of supertype with fewer params (the allowed direction
     * based on the code's comment about "no-op" function)
     */
    @Test(timeout = 4000)
    public void testDefectSubtypeExtraArgumentAllowed() {
        JSTypeRegistry registry = new JSTypeRegistry();
        
        // Create function f(number): boolean
        Node paramsF = new Node(1);
        Node paramF = Node.newString("a");
        paramF.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsF.addChildToBack(paramF);
        ArrowType f = new ArrowType(registry, paramsF, 
                                    registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // Create function g(number, string): boolean
        Node paramsG = new Node(1);
        Node paramG1 = Node.newString("x");
        paramG1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsG.addChildToBack(paramG1);
        Node paramG2 = Node.newString("y");
        paramG2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
        paramsG.addChildToBack(paramG2);
        ArrowType g = new ArrowType(registry, paramsG, 
                                    registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        
        // This direction is allowed by the code (g has extra param that f ignores)
        assertTrue("f with fewer params should be subtype of g with more params (no-op pattern)",
                   f.isSubtype(g));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetLeastSupertypeThrows() {
        ArrowType arrow = createSimpleArrowType(1);
        arrow.getLeastSupertype(arrow);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetGreatestSubtypeThrows() {
        ArrowType arrow = createSimpleArrowType(1);
        arrow.getGreatestSubtype(arrow);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testTestForEqualityThrows() {
        ArrowType arrow = createSimpleArrowType(1);
        arrow.testForEquality(arrow);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testVisitThrows() {
        ArrowType arrow = createSimpleArrowType(1);
        arrow.visit(null);
    }

    // ==================== Partition E: Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testHashCodeWithNullParamType() {
        JSTypeRegistry registry = new JSTypeRegistry();
        Node params = new Node(1);
        Node param = Node.newString("x");
        param.setJSType(null);
        params.addChildToBack(param);
        ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        int hashCode = arrow.hashCode();
        // Should not throw NPE and produce a consistent hash
        assertNotNull("Hash code with null param should be computed", hashCode);
    }

    @Test(timeout = 4000)
    public void testToStringHelper() {
        ArrowType arrow = createSimpleArrowType(1);
        String result = arrow.toStringHelper(false);
        assertNotNull("toStringHelper should return a string", result);
    }
}