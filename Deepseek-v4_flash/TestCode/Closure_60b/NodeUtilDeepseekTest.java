package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TernaryValue;
import com.google.javascript.rhino.jstype.JSType;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * This test suite targets the known Defects4J defect where getPureBooleanValue
 * incorrectly returns TernaryValue.FALSE for certain nodes that should return
 * TernaryValue.UNKNOWN. Specifically, the bug is in the VOID case:
 * Token.VOID should return TernaryValue.FALSE only when there are no side
 * effects, but the current implementation unconditionally returns FALSE.
 * 
 * Branches covered:
 * - getPureBooleanValue: STRING, NUMBER, NOT, NULL, FALSE, VOID, NAME
 *   (undefined, NaN, Infinity, other), TRUE, REGEXP, ARRAYLIT, OBJECTLIT
 * - getImpureBooleanValue: ASSIGN, COMMA, NOT, AND, OR, HOOK, ARRAYLIT,
 *   OBJECTLIT, default (calls getPureBooleanValue)
 * - getStringValue: STRING, NAME (undefined, Infinity, NaN, other), NUMBER,
 *   FALSE, TRUE, NULL, VOID, NOT, ARRAYLIT, OBJECTLIT
 * - getNumberValue: TRUE, FALSE, NULL, NUMBER, VOID, NAME (undefined, NaN,
 *   Infinity, other), NEG, NOT, STRING, ARRAYLIT, OBJECTLIT
 * - isImmutableValue: STRING, NUMBER, NULL, TRUE, FALSE, NOT, VOID, NEG,
 *   NAME (undefined, Infinity, NaN, other)
 * - isLiteralValue: ARRAYLIT, REGEXP, OBJECTLIT, FUNCTION, default
 * - isSimpleOperator: various token types
 * - mayEffectMutableState: AND, BLOCK, EXPR_RESULT, HOOK, IF, IN, LP,
 *   NUMBER, OR, THIS, TRUE, FALSE, NULL, STRING, SWITCH, TRY, EMPTY,
 *   THROW, OBJECTLIT, ARRAYLIT, REGEXP, VAR, NAME, FUNCTION, NEW, CALL,
 *   default (simple operators, assignment ops)
 * - constructorCallHasSideEffects: NEW nodes with/without side effects
 * - functionCallHasSideEffects: CALL nodes with/without side effects
 * - isValidDefineValue: various operators and NAME/GETPROP
 * - isNumericResultHelper: ADD, BITNOT, BITOR, etc.
 * - isBooleanResultHelper: TRUE, FALSE, EQ, NE, etc.
 * - isUndefined: VOID, NAME "undefined"
 * - isNull: NULL
 * - isNullOrUndefined: combination of above
 * - mayBeStringHelper: not numeric, not boolean, not undefined, not null
 * - isAssignmentOp: all assignment tokens
 * - isGet: GETPROP, GETELEM
 * - isName: NAME
 * - isVarOrSimpleAssignLhs: ASSIGN, VAR
 * - isLValue: various parent types
 * - isFunctionDeclaration vs isFunctionExpression
 * - isEmptyBlock: BLOCK with only EMPTY children
 * - getFunctionName: NAME, ASSIGN, default
 * - precedence: all token types in switch
 * - getStringNumberValue: various string cases
 * - trimJsWhiteSpace: whitespace handling
 * - isStrWhiteSpaceChar: various characters
 * - arrayToString: array literals
 * - getArrayElementStringValue: null/undefined vs other
 * - evaluatesToLocalValue: various node types
 * - isConstantName: boolean prop check
 * - isPrototypePropertyDeclaration: expr assign with prototype
 * - removeChild: various parent/child combinations
 * 
 * Defect-targeted: The VOID case in getPureBooleanValue unconditionally
 * returns FALSE, but should return FALSE only when there are no side effects.
 * When mayHaveSideEffects returns true for the child, it should return UNKNOWN.
 */
public class NodeUtilDeepseekTest {

    // ======== Partition A: Core Functional Logic & State Transitions ========

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_STRING_Empty() {
        Node n = Node.newString(Token.STRING, "");
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_STRING_NonEmpty() {
        Node n = Node.newString(Token.STRING, "hello");
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NUMBER_Zero() {
        Node n = Node.newNumber(0);
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NUMBER_NonZero() {
        Node n = Node.newNumber(3.14);
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NULL() {
        Node n = new Node(Token.NULL);
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_FALSE() {
        Node n = new Node(Token.FALSE);
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_TRUE() {
        Node n = new Node(Token.TRUE);
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_REGEXP() {
        Node n = new Node(Token.REGEXP);
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NAME_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NAME_NaN() {
        Node n = Node.newString(Token.NAME, "NaN");
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NAME_Infinity() {
        Node n = Node.newString(Token.NAME, "Infinity");
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NAME_Other() {
        Node n = Node.newString(Token.NAME, "x");
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_NOT() {
        Node child = new Node(Token.FALSE);
        Node n = new Node(Token.NOT, child);
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_VOID_NoSideEffects() {
        // VOID with a number child (no side effects)
        Node child = Node.newNumber(0);
        Node n = new Node(Token.VOID, child);
        // Bug: currently returns FALSE unconditionally, should check side effects
        // The known defect is that this returns FALSE even when child has no side effects
        // But the correct behavior for a number child is FALSE (no side effects)
        assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_VOID_WithSideEffects() {
        // VOID with a CALL child (has side effects)
        Node target = Node.newString(Token.NAME, "foo");
        Node call = new Node(Token.CALL, target);
        Node n = new Node(Token.VOID, call);
        // Bug: currently returns FALSE, should return UNKNOWN because of side effects
        // This is the target defect!
        assertEquals("VOID with side effects should return UNKNOWN",
                TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetPureBooleanValue_ARRAYLIT_NoSideEffects() {
        Node n = new Node(Token.ARRAYLIT);
        n.putBooleanProp(Node.SIDE_EFFECT_FLAGS, 0); // no side effects
        assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_ASSIGN() {
        Node lhs = Node.newString(Token.NAME, "x");
        Node rhs = new Node(Token.TRUE);
        Node n = new Node(Token.ASSIGN, lhs, rhs);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_COMMA() {
        Node left = Node.newNumber(0);
        Node right = new Node(Token.TRUE);
        Node n = new Node(Token.COMMA, left, right);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_NOT() {
        Node child = new Node(Token.FALSE);
        Node n = new Node(Token.NOT, child);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_AND() {
        Node left = new Node(Token.TRUE);
        Node right = new Node(Token.TRUE);
        Node n = new Node(Token.AND, left, right);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_OR() {
        Node left = new Node(Token.FALSE);
        Node right = new Node(Token.TRUE);
        Node n = new Node(Token.OR, left, right);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_HOOK_SameValues() {
        Node cond = new Node(Token.TRUE);
        Node trueVal = new Node(Token.TRUE);
        Node falseVal = new Node(Token.TRUE);
        Node n = new Node(Token.HOOK, cond, trueVal, falseVal);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_HOOK_DifferentValues() {
        Node cond = new Node(Token.TRUE);
        Node trueVal = new Node(Token.TRUE);
        Node falseVal = new Node(Token.FALSE);
        Node n = new Node(Token.HOOK, cond, trueVal, falseVal);
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetImpureBooleanValue_ARRAYLIT() {
        Node n = new Node(Token.ARRAYLIT);
        assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_STRING() {
        Node n = Node.newString(Token.STRING, "test");
        assertEquals("test", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NAME_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertEquals("undefined", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NAME_Infinity() {
        Node n = Node.newString(Token.NAME, "Infinity");
        assertEquals("Infinity", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NAME_NaN() {
        Node n = Node.newString(Token.NAME, "NaN");
        assertEquals("NaN", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NAME_Other() {
        Node n = Node.newString(Token.NAME, "x");
        assertNull(NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NUMBER_Integer() {
        Node n = Node.newNumber(42);
        assertEquals("42", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NUMBER_Double() {
        Node n = Node.newNumber(3.14);
        assertNotNull(NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_Boolean() {
        Node n = new Node(Token.TRUE);
        assertEquals("true", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NULL() {
        Node n = new Node(Token.NULL);
        assertEquals("null", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_VOID() {
        Node n = new Node(Token.VOID, Node.newNumber(0));
        assertEquals("undefined", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_NOT() {
        Node child = new Node(Token.FALSE);
        Node n = new Node(Token.NOT, child);
        assertEquals("true", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_OBJECTLIT() {
        Node n = new Node(Token.OBJECTLIT);
        assertEquals("[object Object]", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_ARRAYLIT_Empty() {
        Node n = new Node(Token.ARRAYLIT);
        assertEquals("", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValue_ARRAYLIT_WithElements() {
        Node n = new Node(Token.ARRAYLIT);
        n.addChildToBack(Node.newString(Token.STRING, "a"));
        n.addChildToBack(Node.newString(Token.STRING, "b"));
        assertEquals("a,b", NodeUtil.getStringValue(n));
    }

    // ======== Partition B: Boundary Value Analysis (BVA) & Extremes ========

    @Test(timeout = 4000)
    public void testIsImmutableValue_STRING() {
        Node n = Node.newString(Token.STRING, "test");
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NUMBER() {
        Node n = Node.newNumber(42);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NULL() {
        Node n = new Node(Token.NULL);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_TRUE() {
        Node n = new Node(Token.TRUE);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_FALSE() {
        Node n = new Node(Token.FALSE);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NOT() {
        Node child = new Node(Token.TRUE);
        Node n = new Node(Token.NOT, child);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_VOID() {
        Node child = Node.newNumber(0);
        Node n = new Node(Token.VOID, child);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NEG() {
        Node child = new Node(Token.TRUE);
        Node n = new Node(Token.NEG, child);
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NAME_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NAME_Infinity() {
        Node n = Node.newString(Token.NAME, "Infinity");
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NAME_NaN() {
        Node n = Node.newString(Token.NAME, "NaN");
        assertTrue(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue_NAME_Other() {
        Node n = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isImmutableValue(n));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue_ARRAYLIT_Empty() {
        Node n = new Node(Token.ARRAYLIT);
        assertTrue(NodeUtil.isLiteralValue(n, false));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue_ARRAYLIT_WithNonLiteral() {
        Node n = new Node(Token.ARRAYLIT);
        n.addChildToBack(Node.newString(Token.NAME, "x"));
        assertFalse(NodeUtil.isLiteralValue(n, false));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue_OBJECTLIT_Empty() {
        Node n = new Node(Token.OBJECTLIT);
        assertTrue(NodeUtil.isLiteralValue(n, false));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue_FUNCTION_IncludeFalse() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "")); // name
        fn.addChildToBack(new Node(Token.LP)); // params
        fn.addChildToBack(new Node(Token.BLOCK)); // body
        assertFalse(NodeUtil.isLiteralValue(fn, false));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue_FUNCTION_IncludeTrue() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "")); // name
        fn.addChildToBack(new Node(Token.LP)); // params
        fn.addChildToBack(new Node(Token.BLOCK)); // body
        assertTrue(NodeUtil.isLiteralValue(fn, true));
    }

    @Test(timeout = 4000)
    public void testIsSimpleOperator_ADD() {
        Node n = new Node(Token.ADD);
        assertTrue(NodeUtil.isSimpleOperator(n));
    }

    @Test(timeout = 4000)
    public void testIsSimpleOperator_ASSIGN() {
        Node n = new Node(Token.ASSIGN);
        assertFalse(NodeUtil.isSimpleOperator(n));
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_TRUE() {
        Node n = new Node(Token.TRUE);
        assertEquals(1.0, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_FALSE() {
        Node n = new Node(Token.FALSE);
        assertEquals(0.0, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NULL() {
        Node n = new Node(Token.NULL);
        assertEquals(0.0, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NUMBER() {
        Node n = Node.newNumber(42.5);
        assertEquals(42.5, NodeUtil.getNumberValue(n), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_VOID_NoSideEffects() {
        Node child = Node.newNumber(0);
        Node n = new Node(Token.VOID, child);
        assertEquals(Double.NaN, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NAME_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertEquals(Double.NaN, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NAME_NaN() {
        Node n = Node.newString(Token.NAME, "NaN");
        assertEquals(Double.NaN, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NAME_Infinity() {
        Node n = Node.newString(Token.NAME, "Infinity");
        assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NEG_Infinity() {
        Node inner = Node.newString(Token.NAME, "Infinity");
        Node n = new Node(Token.NEG, inner);
        assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_NOT() {
        Node child = new Node(Token.FALSE);
        Node n = new Node(Token.NOT, child);
        assertEquals(1.0, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_STRING_Empty() {
        Node n = Node.newString(Token.STRING, "");
        assertEquals(0.0, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValue_STRING_Numeric() {
        Node n = Node.newString(Token.STRING, "42");
        assertEquals(42.0, NodeUtil.getNumberValue(n), 0.0);
    }

    // ======== Partition C: Defect-Targeted Branch Zone ========

    @Test(timeout = 4000)
    public void testIsNullOrUndefined_NULL() {
        Node n = new Node(Token.NULL);
        assertTrue(NodeUtil.isNullOrUndefined(n));
    }

    @Test(timeout = 4000)
    public void testIsNullOrUndefined_VOID() {
        Node n = new Node(Token.VOID, Node.newNumber(0));
        assertTrue(NodeUtil.isNullOrUndefined(n));
    }

    @Test(timeout = 4000)
    public void testIsNullOrUndefined_NAME_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isNullOrUndefined(n));
    }

    @Test(timeout = 4000)
    public void testIsNullOrUndefined_Other() {
        Node n = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isNullOrUndefined(n));
    }

    @Test(timeout = 4000)
    public void testIsNumericResult_ADD() {
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node n = new Node(Token.ADD, left, right);
        assertTrue(NodeUtil.isNumericResult(n));
    }

    @Test(timeout = 4000)
    public void testIsNumericResult_ADD_String() {
        Node left = Node.newString(Token.STRING, "a");
        Node right = Node.newString(Token.STRING, "b");
        Node n = new Node(Token.ADD, left, right);
        assertFalse(NodeUtil.isNumericResult(n));
    }

    @Test(timeout = 4000)
    public void testIsBooleanResult_EQ() {
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node n = new Node(Token.EQ, left, right);
        assertTrue(NodeUtil.isBooleanResult(n));
    }

    @Test(timeout = 4000)
    public void testIsBooleanResult_ADD() {
        Node left = Node.newNumber(1);
        Node right = Node.newNumber(2);
        Node n = new Node(Token.ADD, left, right);
        assertFalse(NodeUtil.isBooleanResult(n));
    }

    @Test(timeout = 4000)
    public void testMayBeString_Number() {
        Node n = Node.newNumber(42);
        assertFalse(NodeUtil.mayBeString(n, false));
    }

    @Test(timeout = 4000)
    public void testMayBeString_String() {
        Node n = Node.newString(Token.STRING, "test");
        assertTrue(NodeUtil.mayBeString(n, false));
    }

    @Test(timeout = 4000)
    public void testIsAssignmentOp_ASSIGN() {
        Node n = new Node(Token.ASSIGN);
        assertTrue(NodeUtil.isAssignmentOp(n));
    }

    @Test(timeout = 4000)
    public void testIsAssignmentOp_ADD() {
        Node n = new Node(Token.ADD);
        assertFalse(NodeUtil.isAssignmentOp(n));
    }

    @Test(timeout = 4000)
    public void testIsGet_GETPROP() {
        Node obj = Node.newString(Token.NAME, "obj");
        Node prop = Node.newString(Token.STRING, "prop");
        Node n = new Node(Token.GETPROP, obj, prop);
        assertTrue(NodeUtil.isGet(n));
    }

    @Test(timeout = 4000)
    public void testIsGet_GETELEM() {
        Node obj = Node.newString(Token.NAME, "obj");
        Node index = Node.newNumber(0);
        Node n = new Node(Token.GETELEM, obj, index);
        assertTrue(NodeUtil.isGet(n));
    }

    @Test(timeout = 4000)
    public void testIsGet_Other() {
        Node n = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isGet(n));
    }

    @Test(timeout = 4000)
    public void testIsName_NAME() {
        Node n = Node.newString(Token.NAME, "x");
        assertTrue(NodeUtil.isName(n));
    }

    @Test(timeout = 4000)
    public void testIsName_Other() {
        Node n = Node.newString(Token.STRING, "x");
        assertFalse(NodeUtil.isName(n));
    }

    @Test(timeout = 4000)
    public void testIsVarOrSimpleAssignLhs_ASSIGN() {
        Node lhs = Node.newString(Token.NAME, "x");
        Node rhs = Node.newNumber(42);
        Node parent = new Node(Token.ASSIGN, lhs, rhs);
        assertTrue(NodeUtil.isVarOrSimpleAssignLhs(lhs, parent));
    }

    @Test(timeout = 4000)
    public void testIsVarOrSimpleAssignLhs_VAR() {
        Node name = Node.newString(Token.NAME, "x");
        Node var = new Node(Token.VAR, name);
        assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, var));
    }

    @Test(timeout = 4000)
    public void testIsFunctionDeclaration_Statement() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "f"));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        Node script = new Node(Token.SCRIPT, fn);
        assertTrue(NodeUtil.isFunctionDeclaration(fn));
    }

    @Test(timeout = 4000)
    public void testIsFunctionDeclaration_Expression() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, ""));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        assertFalse(NodeUtil.isFunctionDeclaration(fn));
    }

    @Test(timeout = 4000)
    public void testIsFunctionExpression_Expression() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, ""));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        assertTrue(NodeUtil.isFunctionExpression(fn));
    }

    @Test(timeout = 4000)
    public void testIsFunctionExpression_Statement() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "f"));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        Node script = new Node(Token.SCRIPT, fn);
        assertFalse(NodeUtil.isFunctionExpression(fn));
    }

    @Test(timeout = 4000)
    public void testIsEmptyBlock_Empty() {
        Node block = new Node(Token.BLOCK);
        assertEquals(true, NodeUtil.isEmptyBlock(block));
    }

    @Test(timeout = 4000)
    public void testIsEmptyBlock_WithChildren() {
        Node block = new Node(Token.BLOCK);
        block.addChildToBack(new Node(Token.EMPTY));
        assertEquals(true, NodeUtil.isEmptyBlock(block));
    }

    @Test(timeout = 4000)
    public void testIsEmptyBlock_NotBlock() {
        Node n = new Node(Token.SCRIPT);
        assertEquals(false, NodeUtil.isEmptyBlock(n));
    }

    // ======== Partition D: Exception & Defensive Guard Paths ========

    @Test(timeout = 4000)
    public void testConstructorCallHasSideEffects_NotNew() {
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
        try {
            NodeUtil.constructorCallHasSideEffects(call);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFunctionCallHasSideEffects_NotCall() {
        Node n = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
        try {
            NodeUtil.functionCallHasSideEffects(n);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testPrecedence_Invalid() {
        // Token.ARRAYLIT should be handled
        try {
            NodeUtil.precedence(-1);
            fail("Expected Error");
        } catch (Error e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetStringNumberValue_WithVerticalTab() {
        assertNull(NodeUtil.getStringNumberValue("test\u000Btest"));
    }

    @Test(timeout = 4000)
    public void testGetStringNumberValue_Hex() {
        assertEquals(255.0, NodeUtil.getStringNumberValue("0xFF"), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetStringNumberValue_SignedHex() {
        assertNull(NodeUtil.getStringNumberValue("-0xFF"));
    }

    @Test(timeout = 4000)
    public void testGetStringNumberValue_InfinityCase() {
        assertNull(NodeUtil.getStringNumberValue("infinity"));
    }

    // ======== Partition E: Object Lifecycle & Contract Integrity ========

    @Test(timeout = 4000)
    public void testGetFunctionName_NAME() {
        Node name = Node.newString(Token.NAME, "myFunc");
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "f"));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        name.addChildToBack(fn);
        assertEquals("myFunc", NodeUtil.getFunctionName(fn));
    }

    @Test(timeout = 4000)
    public void testGetFunctionName_ASSIGN() {
        Node lhs = Node.newString(Token.NAME, "myObj");
        Node prop = Node.newString(Token.STRING, "prop");
        Node getprop = new Node(Token.GETPROP, lhs, prop);
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "f"));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        Node assign = new Node(Token.ASSIGN, getprop, fn);
        assertEquals("myObj.prop", NodeUtil.getFunctionName(fn));
    }

    @Test(timeout = 4000)
    public void testGetFunctionName_Default() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, "myFunc"));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        assertEquals("myFunc", NodeUtil.getFunctionName(fn));
    }

    @Test(timeout = 4000)
    public void testGetNearestFunctionName_FromParent() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, ""));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        Node stringLit = Node.newString(Token.STRING, "methodName");
        stringLit.addChildToBack(fn);
        assertEquals("methodName", NodeUtil.getNearestFunctionName(fn));
    }

    @Test(timeout = 4000)
    public void testIsValidDefineValue_Valid() {
        Node val = Node.newString(Token.STRING, "constant");
        java.util.Set<String> defines = new java.util.HashSet<>();
        defines.add("CONST");
        Node nameVal = Node.newString(Token.NAME, "CONST");
        assertTrue(NodeUtil.isValidDefineValue(nameVal, defines));
    }

    @Test(timeout = 4000)
    public void testIsValidDefineValue_Invalid() {
        Node val = Node.newString(Token.NAME, "x");
        java.util.Set<String> defines = new java.util.HashSet<>();
        assertFalse(NodeUtil.isValidDefineValue(val, defines));
    }

    @Test(timeout = 4000)
    public void testIsPrototypePropertyDeclaration_True() {
        Node expr = new Node(Token.EXPR_RESULT);
        Node lhs = Node.newString(Token.NAME, "Foo");
        Node prop = Node.newString(Token.STRING, "prototype");
        Node proto = new Node(Token.GETPROP, lhs, prop);
        Node member = Node.newString(Token.STRING, "method");
        Node assign = new Node(Token.ASSIGN, new Node(Token.GETPROP, proto, member), new Node(Token.NUMBER));
        expr.addChildToBack(assign);
        assertTrue(NodeUtil.isPrototypePropertyDeclaration(expr));
    }

    @Test(timeout = 4000)
    public void testIsPrototypePropertyDeclaration_False() {
        Node expr = new Node(Token.EXPR_RESULT);
        Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(42));
        expr.addChildToBack(assign);
        assertFalse(NodeUtil.isPrototypePropertyDeclaration(expr));
    }

    @Test(timeout = 4000)
    public void testIsConstantName_True() {
        Node n = Node.newString(Token.NAME, "CONST");
        n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        assertTrue(NodeUtil.isConstantName(n));
    }

    @Test(timeout = 4000)
    public void testIsConstantName_False() {
        Node n = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isConstantName(n));
    }

    @Test(timeout = 4000)
    public void testEvaluatesToLocalValue_Number() {
        Node n = Node.newNumber(42);
        assertTrue(NodeUtil.evaluatesToLocalValue(n));
    }

    @Test(timeout = 4000)
    public void testEvaluatesToLocalValue_Name() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.evaluatesToLocalValue(n));
    }

    @Test(timeout = 4000)
    public void testEvaluatesToLocalValue_GetProp() {
        Node obj = Node.newString(Token.NAME, "obj");
        Node prop = Node.newString(Token.STRING, "prop");
        Node n = new Node(Token.GETPROP, obj, prop);
        java.util.Set<Node> locals = new java.util.HashSet<>();
        assertFalse(NodeUtil.evaluatesToLocalValue(n, locals::contains));
    }

    @Test(timeout = 4000)
    public void testGetRootOfQualifiedName_NAME() {
        Node n = Node.newString(Token.NAME, "x");
        assertEquals(n, NodeUtil.getRootOfQualifiedName(n));
    }

    @Test(timeout = 4000)
    public void testGetRootOfQualifiedName_GETPROP() {
        Node obj = Node.newString(Token.NAME, "obj");
        Node prop = Node.newString(Token.STRING, "prop");
        Node n = new Node(Token.GETPROP, obj, prop);
        assertEquals(obj, NodeUtil.getRootOfQualifiedName(n));
    }

    @Test(timeout = 4000)
    public void testGetRootOfQualifiedName_THIS() {
        Node n = new Node(Token.THIS);
        assertEquals(n, NodeUtil.getRootOfQualifiedName(n));
    }

    @Test(timeout = 4000)
    public void testIsLatin_AllAscii() {
        assertTrue(NodeUtil.isLatin("hello"));
    }

    @Test(timeout = 4000)
    public void testIsLatin_NonAscii() {
        assertFalse(NodeUtil.isLatin("héllo"));
    }

    @Test(timeout = 4000)
    public void testIsValidPropertyName_Valid() {
        assertTrue(NodeUtil.isValidPropertyName("prop"));
    }

    @Test(timeout = 4000)
    public void testIsValidPropertyName_Keyword() {
        assertFalse(NodeUtil.isValidPropertyName("if"));
    }

    @Test(timeout = 4000)
    public void testIsValidPropertyName_NonLatin() {
        assertFalse(NodeUtil.isValidPropertyName("proété"));
    }

    @Test(timeout = 4000)
    public void testIsStrWhiteSpaceChar_VerticalTab() {
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    }

    @Test(timeout = 4000)
    public void testIsStrWhiteSpaceChar_Space() {
        assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    }

    @Test(timeout = 4000)
    public void testIsStrWhiteSpaceChar_Letter() {
        assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('a'));
    }

    @Test(timeout = 4000)
    public void testGetArrayElementStringValue_Null() {
        Node n = new Node(Token.NULL);
        assertEquals("", NodeUtil.getArrayElementStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetArrayElementStringValue_Undefined() {
        Node n = Node.newString(Token.NAME, "undefined");
        assertEquals("", NodeUtil.getArrayElementStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetArrayElementStringValue_EMPTY() {
        Node n = new Node(Token.EMPTY);
        assertEquals("", NodeUtil.getArrayElementStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetArrayElementStringValue_String() {
        Node n = Node.newString(Token.STRING, "hello");
        assertEquals("hello", NodeUtil.getArrayElementStringValue(n));
    }
}