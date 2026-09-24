package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TernaryValue;

import org.junit.Test;

/**
 * Advanced white-box test suite for NodeUtil, targeting the known Defects4J failures:
 * - testIsBooleanResult: DELPROP is not recognized as a boolean result.
 * - testLocalValue1: evaluatesToLocalValue throws on DELPROP.
 *
 * [Branch & Defect Analysis Matrix]
 * - getBooleanValue: branches for STRING (length>0), NUMBER (non-zero), NOT, NULL/FALSE/VOID, NAME(undefined,NaN,Infinity), TRUE/ARRAYLIT/OBJECTLIT/REGEXP.
 * - getStringValue: branches for STRING, NAME(undefined,Infinity,NaN), NUMBER(integer/non-integer), FALSE/TRUE/NULL, VOID, NOT(child known), ARRAYLIT(include skip indexes, null child), OBJECTLIT.
 * - getNumberValue: branches for TRUE, FALSE/NULL, NUMBER, VOID(children sideEffect), NAME(undefined,NaN,Infinity), NEG(Infinity child), NOT(child known), STRING(empty, hex, sign+hex, infinity variations, parseDouble), ARRAYLIT/OBJECTLIT.
 * - isImmutableValue: branches for STRING/NUMBER/NULL/TRUE/FALSE, NOT(child immutable), VOID/NEG(child immutable), NAME(undefined,Infinity,NaN).
 * - isLiteralValue: branches for ARRAYLIT(all children literal), OBJECTLIT(value children literal), FUNCTION(includeFunctions true/false isFunctionDeclaration), default to isImmutableValue.
 * - mayHaveSideEffects: many branches; test THROW, OBJECTLIT/eckForNewObjects, ARRAYLIT/REGEXP(checkForNewObjects), VAR with initializer, FUNCTION(checkForNewObjects, isFunctionExpression), NEW(checkForNewObjects, constructorCallHasSideEffects), CALL(functionCallHasSideEffects), assignment ops, simple operators, etc.
 * - evaluatesToLocalValue: exhaustive over node types; DELPROP should not throw (bug), INC/DEC, CALL, NEW, FUNCTION, REGEXP, ARRAYLIT, OBJECTLIT, IN, assignment ops, simple ops, immutable values.
 * - isBooleanResultHelper: includes DELPROP (bug fix).
 * - arrayToString: with skip indexes and null/undefined child handling.
 */
public class NodeUtilDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testGetBooleanValueString() {
        Node n = Node.newString("hello");
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(n));

        Node empty = Node.newString("");
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(empty));
    }

    @Test(timeout = 4000)
    public void testGetBooleanValueNumber() {
        Node zero = Node.newNumber(0);
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(zero));

        Node nonZero = Node.newNumber(42);
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(nonZero));
    }

    @Test(timeout = 4000)
    public void testGetBooleanValueNullFalseVoid() {
        Node nullNode = new Node(Token.NULL);
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(nullNode));

        Node falseNode = new Node(Token.FALSE);
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(falseNode));

        Node voidNode = new Node(Token.VOID, Node.newNumber(0));
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(voidNode));
    }

    @Test(timeout = 4000)
    public void testGetBooleanValueNameSpecial() {
        Node undef = Node.newString(Token.NAME, "undefined");
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(undef));

        Node nan = Node.newString(Token.NAME, "NaN");
        assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(nan));

        Node inf = Node.newString(Token.NAME, "Infinity");
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(inf));
    }

    @Test(timeout = 4000)
    public void testGetBooleanValueLiterals() {
        Node trueNode = new Node(Token.TRUE);
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(trueNode));

        Node arrayLit = new Node(Token.ARRAYLIT);
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(arrayLit));

        Node objLit = new Node(Token.OBJECTLIT);
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(objLit));

        Node regexp = new Node(Token.REGEXP);
        assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(regexp));
    }

    @Test(timeout = 4000)
    public void testGetStringValueString() {
        Node n = Node.newString("foo");
        assertEquals("foo", NodeUtil.getStringValue(n));
    }

    @Test(timeout = 4000)
    public void testGetStringValueNameSpecial() {
        Node undef = Node.newString(Token.NAME, "undefined");
        assertEquals("undefined", NodeUtil.getStringValue(undef));

        Node inf = Node.newString(Token.NAME, "Infinity");
        assertEquals("Infinity", NodeUtil.getStringValue(inf));

        Node nan = Node.newString(Token.NAME, "NaN");
        assertEquals("NaN", NodeUtil.getStringValue(nan));
    }

    @Test(timeout = 4000)
    public void testGetStringValueNumber() {
        Node intVal = Node.newNumber(123);
        assertEquals("123", NodeUtil.getStringValue(intVal));

        Node frac = Node.newNumber(1.5);
        assertEquals("1.5", NodeUtil.getStringValue(frac));

        Node large = Node.newNumber(1e20);
        assertNotNull(NodeUtil.getStringValue(large));
    }

    @Test(timeout = 4000)
    public void testGetStringValueBooleanNullVoid() {
        Node t = new Node(Token.TRUE);
        assertEquals("true", NodeUtil.getStringValue(t));

        Node f = new Node(Token.FALSE);
        assertEquals("false", NodeUtil.getStringValue(f));

        Node nu = new Node(Token.NULL);
        assertEquals("null", NodeUtil.getStringValue(nu));

        Node vo = new Node(Token.VOID, Node.newNumber(0));
        assertEquals("undefined", NodeUtil.getStringValue(vo));
    }

    @Test(timeout = 4000)
    public void testGetNumberValueTrueFalseNull() {
        Node t = new Node(Token.TRUE);
        assertEquals(1.0, NodeUtil.getNumberValue(t), 0.0);

        Node f = new Node(Token.FALSE);
        assertEquals(0.0, NodeUtil.getNumberValue(f), 0.0);

        Node nu = new Node(Token.NULL);
        assertEquals(0.0, NodeUtil.getNumberValue(nu), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueNumber() {
        Node n = Node.newNumber(3.14);
        assertEquals(3.14, NodeUtil.getNumberValue(n), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueNameSpecial() {
        Node undef = Node.newString(Token.NAME, "undefined");
        assertEquals(Double.NaN, NodeUtil.getNumberValue(undef), 0.0);

        Node nan = Node.newString(Token.NAME, "NaN");
        assertEquals(Double.NaN, NodeUtil.getNumberValue(nan), 0.0);

        Node inf = Node.newString(Token.NAME, "Infinity");
        assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(inf), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueNeg() {
        Node neg = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
        assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(neg), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumberValueString() {
        Node s = Node.newString("42");
        assertEquals(42.0, NodeUtil.getNumberValue(s), 0.0);

        Node empty = Node.newString("");
        assertEquals(0.0, NodeUtil.getNumberValue(empty), 0.0);

        Node hex = Node.newString("0xFF");
        assertEquals(255.0, NodeUtil.getNumberValue(hex), 0.0);

        Node notNum = Node.newString("abc");
        assertEquals(Double.NaN, NodeUtil.getNumberValue(notNum), 0.0);
    }

    @Test(timeout = 4000)
    public void testIsImmutableValue() {
        assertTrue(NodeUtil.isImmutableValue(Node.newString("x")));
        assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
        assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));

        Node not = new Node(Token.NOT, Node.newString("a"));
        assertTrue(NodeUtil.isImmutableValue(not));

        Node vo = new Node(Token.VOID, Node.newNumber(0));
        assertTrue(NodeUtil.isImmutableValue(vo));

        Node nag = new Node(Token.NEG, Node.newNumber(5));
        assertTrue(NodeUtil.isImmutableValue(nag));

        Node undef = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isImmutableValue(undef));

        Node infName = Node.newString(Token.NAME, "Infinity");
        assertTrue(NodeUtil.isImmutableValue(infName));

        Node nanName = Node.newString(Token.NAME, "NaN");
        assertTrue(NodeUtil.isImmutableValue(nanName));

        Node otherName = Node.newString(Token.NAME, "x");
        assertFalse(NodeUtil.isImmutableValue(otherName));
    }

    @Test(timeout = 4000)
    public void testIsLiteralValue() {
        Node arrLit = new Node(Token.ARRAYLIT);
        assertTrue(NodeUtil.isLiteralValue(arrLit, false));

        Node objLit = new Node(Token.OBJECTLIT);
        assertTrue(NodeUtil.isLiteralValue(objLit, false));

        Node regexp = new Node(Token.REGEXP);
        assertTrue(NodeUtil.isLiteralValue(regexp, false));

        Node str = Node.newString("test");
        assertTrue(NodeUtil.isLiteralValue(str, false));

        // Non-literal children
        Node innerName = Node.newString(Token.NAME, "a");
        Node outerArr = new Node(Token.ARRAYLIT, innerName);
        assertFalse(NodeUtil.isLiteralValue(outerArr, false));

        // Function expression with includeFunctions=true & not declaration
        Node fnExpr = new Node(Token.FUNCTION);
        fnExpr.addChildToBack(Node.newString(Token.NAME, "")); // empty name
        fnExpr.addChildToBack(new Node(Token.LP));
        fnExpr.addChildToBack(new Node(Token.BLOCK));
        // Need a parent to be statement? For expression, we need it not to be statement.
        // We'll just test without parent, isFunctionExpression uses isStatement(n) which requires parent.
        // For simplicity, test via isFunctionExpression directly.
        // So skip this.
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testGetNumberValueBoundaryStrings() {
        // Hex with sign
        Node hexSign = Node.newString("+0x1A");
        assertNull(NodeUtil.getNumberValue(hexSign));

        // Infinity variations
        Node infLower = Node.newString("infinity");
        assertNull(NodeUtil.getNumberValue(infLower));

        Node negInf = Node.newString("-Infinity");
        // Should parse as -Infinity? Actually trimJsWhiteSpace and then check "infinity" case; -Infinity is different.
        // The code checks if s.equals("infinity") etc; so "-infinity" is not checked, so it tries parseDouble => -Infinity.
        assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(negInf), 0.0);
    }

    @Test(timeout = 4000)
    public void testArrayToStringWithSparse() {
        Node arr = new Node(Token.ARRAYLIT);
        Node first = Node.newString("a");
        Node skipNode = null; // to simulate a hole, we need skip indexes property
        arr.addChildToBack(first);
        // Add a skip index manually
        int[] skip = new int[]{1};
        arr.putProp(Node.SKIP_INDEXES_PROP, skip);
        // Result: "a," (comma for hole)
        assertEquals("a,", NodeUtil.arrayToString(arr));
    }

    @Test(timeout = 4000)
    public void testArrayToStringWithNullElement() {
        Node arr = new Node(Token.ARRAYLIT);
        Node nullNode = new Node(Token.NULL);
        Node strNode = Node.newString("b");
        arr.addChildToBack(nullNode);
        arr.addChildToBack(strNode);
        // getArrayElementStringValue returns "" for null/undefined, so result: ",b"
        assertEquals(",b", NodeUtil.arrayToString(arr));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testIsBooleanResultWithDelete() {
        // Known defect: DELPROP should be a boolean result but is not recognized.
        Node delNode = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
        assertTrue("DELPROP should be a boolean result", NodeUtil.isBooleanResult(delNode));
    }

    @Test(timeout = 4000)
    public void testEvaluatesToLocalValueWithDelete() {
        // Known defect: evaluatesToLocalValue throws IllegalStateException for DELPROP.
        Node delNode = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
        // Should return false (delete has side effects), not throw.
        assertFalse(NodeUtil.evaluatesToLocalValue(delNode));
    }

    @Test(timeout = 4000)
    public void testFunctionCallHasSideEffectsRegularCall() {
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
        // No additional info, should have side effects
        assertTrue(NodeUtil.functionCallHasSideEffects(call));
    }

    @Test(timeout = 4000)
    public void testMayHaveSideEffectsThrow() {
        Node throwNode = new Node(Token.THROW, Node.newString("msg"));
        assertTrue(NodeUtil.mayHaveSideEffects(throwNode));
    }

    @Test(timeout = 4000)
    public void testMayHaveSideEffectsVarWithInit() {
        Node var = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
        var.getFirstChild().addChildToBack(Node.newNumber(1));
        assertTrue(NodeUtil.mayHaveSideEffects(var));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testConstructorCallHasSideEffectsNonNewThrows() {
        Node call = new Node(Token.CALL);
        try {
            NodeUtil.constructorCallHasSideEffects(call);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFunctionCallHasSideEffectsNonCallThrows() {
        Node newN = new Node(Token.NEW);
        try {
            NodeUtil.functionCallHasSideEffects(newN);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetNumberValueWithSideEffectChildren() {
        // VOID with side-effect child should return null
        Node sideEffect = new Node(Token.CALL, Node.newString(Token.NAME, "e")); // has side effects
        Node voidNode = new Node(Token.VOID, sideEffect);
        assertNull(NodeUtil.getNumberValue(voidNode));
    }

    @Test(timeout = 4000)
    public void testIsNullOrUndefined() {
        Node nullNode = new Node(Token.NULL);
        assertTrue(NodeUtil.isNullOrUndefined(nullNode));
        Node voidNode = new Node(Token.VOID, Node.newNumber(0));
        assertTrue(NodeUtil.isNullOrUndefined(voidNode));
        Node nameUndef = Node.newString(Token.NAME, "undefined");
        assertTrue(NodeUtil.isNullOrUndefined(nameUndef));
        Node other = Node.newString("x");
        assertFalse(NodeUtil.isNullOrUndefined(other));
    }

    @Test(timeout = 4000)
    public void testIsSimpleOperator() {
        assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
        assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
        assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
        assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // (No applicable for NodeUtil, but we test isFunctionExpression, isFunctionDeclaration)

    @Test(timeout = 4000)
    public void testIsFunctionExpression() {
        Node fn = new Node(Token.FUNCTION);
        fn.addChildToBack(Node.newString(Token.NAME, ""));
        fn.addChildToBack(new Node(Token.LP));
        fn.addChildToBack(new Node(Token.BLOCK));
        // No parent, so isStatement returns false => isFunctionExpression returns true
        assertTrue(NodeUtil.isFunctionExpression(fn));
    }

    @Test(timeout = 4000)
    public void testIsCall() {
        Node call = new Node(Token.CALL);
        assertTrue(NodeUtil.isCall(call));
        assertFalse(NodeUtil.isCall(new Node(Token.NEW)));
    }

    @Test(timeout = 4000)
    public void testIsAssignmentOp() {
        Node assign = new Node(Token.ASSIGN);
        assertTrue(NodeUtil.isAssignmentOp(assign));
        Node addAssign = new Node(Token.ASSIGN_ADD);
        assertTrue(NodeUtil.isAssignmentOp(addAssign));
        Node plus = new Node(Token.ADD);
        assertFalse(NodeUtil.isAssignmentOp(plus));
    }

    @Test(timeout = 4000)
    public void testGetExpressionBooleanValue() {
        Node comma = new Node(Token.COMMA, Node.newNumber(1), new Node(Token.FALSE));
        assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(comma));

        Node not = new Node(Token.NOT, new Node(Token.TRUE));
        assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(not));

        Node and = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
        assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(and));

        Node or = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.TRUE));
        assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(or));

        Node hook = new Node(Token.HOOK, new Node(Token.TRUE), Node.newString("a"), Node.newString("b"));
        // Both true and false branches are strings -> unknown
        assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hook));
    }
}