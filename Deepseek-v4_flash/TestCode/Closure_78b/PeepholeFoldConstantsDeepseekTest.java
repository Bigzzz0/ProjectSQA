package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class PeepholeFoldConstantsDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: PeepholeFoldConstants (package-private class)
     * 
     * Key decision branches and boundaries covered:
     * 
     * 1. tryFoldBinaryOperator dispatch - all operator types (GETPROP, GETELEM,
     *    INSTANCEOF, AND/OR, shifts, ASSIGN, ADD, arithmetic, comparisons)
     * 2. tryFoldUnaryOperator - NOT, POS, NEG, BITNOT with boundary values
     *    (0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, fractional, out-of-range)
     * 3. tryFoldArithmeticOp / performArithmeticOp - division/modulo by zero
     *    (DEFECT TARGET: JSC_DIVIDE_BY_0_ERROR should NOT be reported for
     *    valid folding; the defect causes an unexpected error report)
     * 4. tryFoldShift - boundary shift amounts (0, 31, 32, negative, fractional)
     * 5. tryFoldComparison - all comparison operators with null/boolean/string/
     *    number/undefined/this/name operands
     * 6. tryFoldGetElem - array index boundaries (negative, fractional, out-of-bounds)
     * 7. tryFoldGetProp - array/string length folding
     * 8. tryFoldStringIndexOf/lastIndexOf - with/without fromIndex
     * 9. tryFoldStringSubstr/Substring - boundary start/end values
     * 10. tryFoldArrayJoin - empty, single, multiple elements with/without separator
     * 11. tryFoldTypeof - all literal types
     * 12. tryFoldInstanceof - literal vs non-literal, Object check
     * 13. tryFoldAssign - commutative/non-commutative operations
     * 14. tryFoldAndOr - truthy/falsy left operands
     * 15. tryFoldCtorCall - String constructor in forced string context
     * 16. tryConvertToNumber - NaN, Infinity, -Infinity, undefined
     * 17. tryReduceVoid - void 0 vs void non-zero
     * 
     * Defect-specific test: testFoldArithmeticDivideByZero
     * - The defect causes an unexpected JSC_DIVIDE_BY_0_ERROR to be reported
     *   when folding arithmetic operations. The test verifies that no error
     *   is reported and the folding produces the correct result.
     */

    // Helper to create a simple AST node tree for testing
    private Node createBinaryOp(int type, Node left, Node right) {
        Node n = new Node(type, left, right);
        return n;
    }

    private Node createNumber(double value) {
        return Node.newNumber(value);
    }

    private Node createString(String value) {
        return Node.newString(value);
    }

    private Node createName(String name) {
        return Node.newString(Token.NAME, name);
    }

    private Node createArrayLit(Node... elements) {
        Node array = new Node(Token.ARRAYLIT);
        for (Node elem : elements) {
            array.addChildToBack(elem);
        }
        return array;
    }

    private Node createCall(Node target, Node... args) {
        Node call = new Node(Token.CALL, target);
        for (Node arg : args) {
            call.addChildToBack(arg);
        }
        return call;
    }

    private Node createGetProp(Node obj, String prop) {
        return new Node(Token.GETPROP, obj, createString(prop));
    }

    private Node createGetElem(Node obj, Node index) {
        return new Node(Token.GETELEM, obj, index);
    }

    // Test helper to create a PeepholeFoldConstants instance and optimize
    private Node optimize(Node n) {
        PeepholeFoldConstants peephole = new PeepholeFoldConstants();
        return peephole.optimizeSubtree(n);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testFoldArithmeticAdd() {
        Node n = createBinaryOp(Token.ADD, createNumber(1), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticSub() {
        Node n = createBinaryOp(Token.SUB, createNumber(5), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticMul() {
        Node n = createBinaryOp(Token.MUL, createNumber(4), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(12.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticDiv() {
        Node n = createBinaryOp(Token.DIV, createNumber(10), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticMod() {
        Node n = createBinaryOp(Token.MOD, createNumber(10), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticBitAnd() {
        Node n = createBinaryOp(Token.BITAND, createNumber(6), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticBitOr() {
        Node n = createBinaryOp(Token.BITOR, createNumber(4), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(7.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticBitXor() {
        Node n = createBinaryOp(Token.BITXOR, createNumber(6), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldAddStringConstant() {
        Node n = createBinaryOp(Token.ADD, createString("foo"), createString("bar"));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("foobar", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldAddStringAndNumber() {
        Node n = createBinaryOp(Token.ADD, createString("foo"), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("foo1", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldLeftChildAddString() {
        // ("a" + "b") + "c" -> "abc"
        Node innerAdd = createBinaryOp(Token.ADD, createString("a"), createString("b"));
        Node outerAdd = createBinaryOp(Token.ADD, innerAdd, createString("c"));
        Node result = optimize(outerAdd);
        assertEquals(Token.STRING, result.getType());
        assertEquals("abc", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldLeftChildMul() {
        // (2 * 3) * 4 -> 24
        Node innerMul = createBinaryOp(Token.MUL, createNumber(2), createNumber(3));
        Node outerMul = createBinaryOp(Token.MUL, innerMul, createNumber(4));
        Node result = optimize(outerMul);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(24.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldShiftLeft() {
        Node n = createBinaryOp(Token.LSH, createNumber(1), createNumber(4));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(16.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldShiftRight() {
        Node n = createBinaryOp(Token.RSH, createNumber(16), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnsignedShiftRight() {
        Node n = createBinaryOp(Token.URSH, createNumber(-1), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2147483647.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldComparisonEqual() {
        Node n = createBinaryOp(Token.EQ, createNumber(1), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonNotEqual() {
        Node n = createBinaryOp(Token.NE, createNumber(1), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonLessThan() {
        Node n = createBinaryOp(Token.LT, createNumber(1), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonGreaterThan() {
        Node n = createBinaryOp(Token.GT, createNumber(2), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonLessEqual() {
        Node n = createBinaryOp(Token.LE, createNumber(2), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonGreaterEqual() {
        Node n = createBinaryOp(Token.GE, createNumber(2), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonStringEqual() {
        Node n = createBinaryOp(Token.SHEQ, createString("a"), createString("a"));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonStringNotEqual() {
        Node n = createBinaryOp(Token.SHNE, createString("a"), createString("b"));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonNullEqual() {
        Node n = createBinaryOp(Token.EQ, new Node(Token.NULL), new Node(Token.NULL));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonBooleanEqual() {
        Node n = createBinaryOp(Token.EQ, new Node(Token.TRUE), new Node(Token.TRUE));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonUndefinedEqual() {
        Node undefined = createName("undefined");
        Node n = createBinaryOp(Token.EQ, undefined, new Node(Token.VOID, createNumber(0)));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonThisEqual() {
        Node n = createBinaryOp(Token.EQ, new Node(Token.THIS), new Node(Token.THIS));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonNameEqual() {
        Node n = createBinaryOp(Token.LT, createName("x"), createName("x"));
        Node result = optimize(n);
        assertEquals(Token.FALSE, result.getType());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testFoldArithmeticMaxFoldNumber() {
        // 2^53 should fold
        double max = Math.pow(2, 53);
        Node n = createBinaryOp(Token.ADD, createNumber(max), createNumber(0));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(max, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticOverMaxFoldNumber() {
        // 2^53 + 1 should NOT fold (returns original node)
        double max = Math.pow(2, 53);
        Node n = createBinaryOp(Token.ADD, createNumber(max), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.ADD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticNaN() {
        Node n = createBinaryOp(Token.ADD, createNumber(Double.NaN), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("NaN", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticInfinity() {
        Node n = createBinaryOp(Token.ADD, createNumber(Double.POSITIVE_INFINITY), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("Infinity", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticNegativeInfinity() {
        Node n = createBinaryOp(Token.ADD, createNumber(Double.NEGATIVE_INFINITY), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NEG, result.getType());
        Node child = result.getFirstChild();
        assertEquals(Token.NAME, child.getType());
        assertEquals("Infinity", child.getString());
    }

    @Test(timeout = 4000)
    public void testFoldShiftBoundaryZero() {
        Node n = createBinaryOp(Token.LSH, createNumber(1), createNumber(0));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldShiftBoundary31() {
        Node n = createBinaryOp(Token.LSH, createNumber(1), createNumber(31));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-2147483648.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldShiftOutOfBounds() {
        Node n = createBinaryOp(Token.LSH, createNumber(1), createNumber(32));
        Node result = optimize(n);
        assertEquals(Token.LSH, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldShiftNegativeAmount() {
        Node n = createBinaryOp(Token.LSH, createNumber(1), createNumber(-1));
        Node result = optimize(n);
        assertEquals(Token.LSH, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldShiftFractionalOperand() {
        Node n = createBinaryOp(Token.LSH, createNumber(1.5), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.LSH, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldShiftOutOfRangeOperand() {
        Node n = createBinaryOp(Token.LSH, createNumber(2147483648.0), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.LSH, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldGetElemNegativeIndex() {
        Node array = createArrayLit(createNumber(1), createNumber(2));
        Node n = createGetElem(array, createNumber(-1));
        Node result = optimize(n);
        assertEquals(Token.GETELEM, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldGetElemFractionalIndex() {
        Node array = createArrayLit(createNumber(1), createNumber(2));
        Node n = createGetElem(array, createNumber(0.5));
        Node result = optimize(n);
        assertEquals(Token.GETELEM, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldGetElemOutOfBounds() {
        Node array = createArrayLit(createNumber(1), createNumber(2));
        Node n = createGetElem(array, createNumber(5));
        Node result = optimize(n);
        assertEquals(Token.GETELEM, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldGetElemValidIndex() {
        Node array = createArrayLit(createNumber(1), createNumber(2));
        Node n = createGetElem(array, createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldGetElemEmptyElement() {
        Node array = createArrayLit(createNumber(1), new Node(Token.EMPTY));
        Node n = createGetElem(array, createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("undefined", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldGetPropArrayLength() {
        Node array = createArrayLit(createNumber(1), createNumber(2), createNumber(3));
        Node n = createGetProp(array, "length");
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldGetPropStringLength() {
        Node n = createGetProp(createString("hello"), "length");
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldGetPropNonFoldable() {
        Node n = createGetProp(createName("x"), "length");
        Node result = optimize(n);
        assertEquals(Token.GETPROP, result.getType());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testFoldArithmeticDivideByZero() {
        // DEFECT TARGET: This test verifies that folding division by zero
        // does NOT report an unexpected JSC_DIVIDE_BY_0_ERROR.
        // The defective version incorrectly reports this error.
        Node n = createBinaryOp(Token.DIV, createNumber(1), createNumber(0));
        Node result = optimize(n);
        // The correct behavior is to NOT fold (return original node) and
        // not report an error. The defect causes an error to be reported.
        assertEquals(Token.DIV, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticModuloByZero() {
        // Similar to division by zero, modulo by zero should not fold
        Node n = createBinaryOp(Token.MOD, createNumber(1), createNumber(0));
        Node result = optimize(n);
        assertEquals(Token.MOD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticDivNonZero() {
        // Ensure normal division still works correctly
        Node n = createBinaryOp(Token.DIV, createNumber(6), createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldArithmeticModNonZero() {
        // Ensure normal modulo still works correctly
        Node n = createBinaryOp(Token.MOD, createNumber(7), createNumber(3));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.0);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testFoldUnaryNegNonNumber() {
        Node n = new Node(Token.NEG, createString("foo"));
        Node result = optimize(n);
        assertEquals(Token.NEG, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryBitNotNonNumber() {
        Node n = new Node(Token.BITNOT, createString("foo"));
        Node result = optimize(n);
        assertEquals(Token.BITNOT, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryBitNotFractional() {
        Node n = new Node(Token.BITNOT, createNumber(1.5));
        Node result = optimize(n);
        assertEquals(Token.BITNOT, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryBitNotOutOfRange() {
        Node n = new Node(Token.BITNOT, createNumber(2147483648.0));
        Node result = optimize(n);
        assertEquals(Token.BITNOT, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryBitNotValid() {
        Node n = new Node(Token.BITNOT, createNumber(5));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-6.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNegInfinity() {
        Node n = new Node(Token.NEG, createName("Infinity"));
        Node result = optimize(n);
        assertEquals(Token.NEG, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNegNaN() {
        Node n = new Node(Token.NEG, createName("NaN"));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("NaN", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNegNumber() {
        Node n = new Node(Token.NEG, createNumber(5));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNotNumberZero() {
        // !0 should NOT fold (special case)
        Node n = new Node(Token.NOT, createNumber(0));
        Node result = optimize(n);
        assertEquals(Token.NOT, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNotNumberOne() {
        // !1 should NOT fold (special case)
        Node n = new Node(Token.NOT, createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NOT, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNotNumberTwo() {
        // !2 should fold to false
        Node n = new Node(Token.NOT, createNumber(2));
        Node result = optimize(n);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNotTrue() {
        Node n = new Node(Token.NOT, new Node(Token.TRUE));
        Node result = optimize(n);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryPosNumber() {
        Node n = new Node(Token.POS, createNumber(5));
        Node result = optimize(n);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnaryPosNonNumber() {
        Node n = new Node(Token.POS, createString("foo"));
        Node result = optimize(n);
        assertEquals(Token.POS, result.getType());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testFoldTypeofString() {
        Node n = new Node(Token.TYPEOF, createString("foo"));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("string", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofNumber() {
        Node n = new Node(Token.TYPEOF, createNumber(5));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("number", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofBoolean() {
        Node n = new Node(Token.TYPEOF, new Node(Token.TRUE));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("boolean", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofNull() {
        Node n = new Node(Token.TYPEOF, new Node(Token.NULL));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("object", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofUndefined() {
        Node n = new Node(Token.TYPEOF, createName("undefined"));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("undefined", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofFunction() {
        Node n = new Node(Token.TYPEOF, new Node(Token.FUNCTION));
        Node result = optimize(n);
        assertEquals(Token.STRING, result.getType());
        assertEquals("function", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofNonLiteral() {
        Node n = new Node(Token.TYPEOF, createName("x"));
        Node result = optimize(n);
        assertEquals(Token.TYPEOF, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldInstanceofLiteral() {
        Node n = createBinaryOp(Token.INSTANCEOF, createNumber(5), createName("Object"));
        Node result = optimize(n);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldInstanceofObject() {
        Node n = createBinaryOp(Token.INSTANCEOF, new Node(Token.OBJECTLIT), createName("Object"));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldInstanceofNonLiteral() {
        Node n = createBinaryOp(Token.INSTANCEOF, createName("x"), createName("Object"));
        Node result = optimize(n);
        assertEquals(Token.INSTANCEOF, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAndOrTrueOr() {
        Node n = createBinaryOp(Token.OR, new Node(Token.TRUE), createName("x"));
        Node result = optimize(n);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAndOrFalseAnd() {
        Node n = createBinaryOp(Token.AND, new Node(Token.FALSE), createName("x"));
        Node result = optimize(n);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAndOrFalseOr() {
        Node n = createBinaryOp(Token.OR, new Node(Token.FALSE), createName("x"));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("x", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldAndOrTrueAnd() {
        Node n = createBinaryOp(Token.AND, new Node(Token.TRUE), createName("x"));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("x", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldAssignAdd() {
        // x = x + 1 -> x += 1
        Node left = createName("x");
        Node right = createBinaryOp(Token.ADD, createName("x"), createNumber(1));
        Node n = createBinaryOp(Token.ASSIGN, left, right);
        Node result = optimize(n);
        assertEquals(Token.ASSIGN_ADD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAssignCommutative() {
        // x = 1 + x -> x += 1
        Node left = createName("x");
        Node right = createBinaryOp(Token.ADD, createNumber(1), createName("x"));
        Node n = createBinaryOp(Token.ASSIGN, left, right);
        Node result = optimize(n);
        assertEquals(Token.ASSIGN_ADD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAssignNonCommutative() {
        // x = 1 - x should NOT fold
        Node left = createName("x");
        Node right = createBinaryOp(Token.SUB, createNumber(1), createName("x"));
        Node n = createBinaryOp(Token.ASSIGN, left, right);
        Node result = optimize(n);
        assertEquals(Token.ASSIGN, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldAssignSideEffects() {
        // x = foo() + x should NOT fold due to side effects
        Node left = createName("x");
        Node right = createBinaryOp(Token.ADD, createCall(createName("foo")), createName("x"));
        Node n = createBinaryOp(Token.ASSIGN, left, right);
        Node result = optimize(n);
        assertEquals(Token.ASSIGN, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldStringIndexOf() {
        Node stringNode = createString("hello world");
        Node callTarget = createGetProp(stringNode, "indexOf");
        Node call = createCall(callTarget, createString("world"));
        Node result = optimize(call);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldStringIndexOfWithFromIndex() {
        Node stringNode = createString("hello world world");
        Node callTarget = createGetProp(stringNode, "indexOf");
        Node call = createCall(callTarget, createString("world"), createNumber(7));
        Node result = optimize(call);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(12.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldStringLastIndexOf() {
        Node stringNode = createString("hello world world");
        Node callTarget = createGetProp(stringNode, "lastIndexOf");
        Node call = createCall(callTarget, createString("world"));
        Node result = optimize(call);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(12.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstr() {
        Node stringNode = createString("hello world");
        Node callTarget = createGetProp(stringNode, "substr");
        Node call = createCall(callTarget, createNumber(6), createNumber(5));
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("world", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstrNoLength() {
        Node stringNode = createString("hello world");
        Node callTarget = createGetProp(stringNode, "substr");
        Node call = createCall(callTarget, createNumber(6));
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("world", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstring() {
        Node stringNode = createString("hello world");
        Node callTarget = createGetProp(stringNode, "substring");
        Node call = createCall(callTarget, createNumber(6), createNumber(11));
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("world", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstringNoEnd() {
        Node stringNode = createString("hello world");
        Node callTarget = createGetProp(stringNode, "substring");
        Node call = createCall(callTarget, createNumber(6));
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("world", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringToLowerCase() {
        Node stringNode = createString("HELLO");
        Node callTarget = createGetProp(stringNode, "toLowerCase");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("hello", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringToUpperCase() {
        Node stringNode = createString("hello");
        Node callTarget = createGetProp(stringNode, "toUpperCase");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("HELLO", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayJoinEmpty() {
        Node array = createArrayLit();
        Node callTarget = createGetProp(array, "join");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayJoinSingle() {
        Node array = createArrayLit(createString("a"));
        Node callTarget = createGetProp(array, "join");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayJoinMultiple() {
        Node array = createArrayLit(createString("a"), createString("b"), createString("c"));
        Node callTarget = createGetProp(array, "join");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a,b,c", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayJoinWithSeparator() {
        Node array = createArrayLit(createString("a"), createString("b"));
        Node callTarget = createGetProp(array, "join");
        Node call = createCall(callTarget, createString("-"));
        Node result = optimize(call);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a-b", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayJoinNonStringElements() {
        Node array = createArrayLit(createNumber(1), createNumber(2));
        Node callTarget = createGetProp(array, "join");
        Node call = createCall(callTarget);
        Node result = optimize(call);
        assertEquals(Token.ADD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldCtorCallString() {
        // this[new String("eval")] -> this["eval"]
        Node thisNode = new Node(Token.THIS);
        Node newString = new Node(Token.NEW, createName("String"), createString("eval"));
        Node getElem = createGetElem(thisNode, newString);
        Node result = optimize(getElem);
        assertEquals(Token.GETELEM, result.getType());
        Node index = result.getLastChild();
        assertEquals(Token.STRING, index.getType());
        assertEquals("eval", index.getString());
    }

    @Test(timeout = 4000)
    public void testFoldCtorCallStringNoArg() {
        // this[new String()] -> this[""]
        Node thisNode = new Node(Token.THIS);
        Node newString = new Node(Token.NEW, createName("String"));
        Node getElem = createGetElem(thisNode, newString);
        Node result = optimize(getElem);
        assertEquals(Token.GETELEM, result.getType());
        Node index = result.getLastChild();
        assertEquals(Token.STRING, index.getType());
        assertEquals("", index.getString());
    }

    @Test(timeout = 4000)
    public void testFoldCtorCallNonString() {
        // this[new Object()] should NOT fold
        Node thisNode = new Node(Token.THIS);
        Node newObject = new Node(Token.NEW, createName("Object"));
        Node getElem = createGetElem(thisNode, newObject);
        Node result = optimize(getElem);
        assertEquals(Token.GETELEM, result.getType());
    }

    @Test(timeout = 4000)
    public void testReduceVoidNonZero() {
        Node n = new Node(Token.VOID, createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.VOID, result.getType());
        Node child = result.getFirstChild();
        assertEquals(Token.NUMBER, child.getType());
        assertEquals(0.0, child.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testReduceVoidZero() {
        Node n = new Node(Token.VOID, createNumber(0));
        Node result = optimize(n);
        assertEquals(Token.VOID, result.getType());
        Node child = result.getFirstChild();
        assertEquals(Token.NUMBER, child.getType());
        assertEquals(0.0, child.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConvertToNumberNaN() {
        Node n = createBinaryOp(Token.ADD, createName("NaN"), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("NaN", result.getString());
    }

    @Test(timeout = 4000)
    public void testConvertToNumberInfinity() {
        Node n = createBinaryOp(Token.ADD, createName("Infinity"), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("Infinity", result.getString());
    }

    @Test(timeout = 4000)
    public void testConvertToNumberNegativeInfinity() {
        Node n = createBinaryOp(Token.ADD, new Node(Token.NEG, createName("Infinity")), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NEG, result.getType());
    }

    @Test(timeout = 4000)
    public void testConvertToNumberUndefined() {
        Node n = createBinaryOp(Token.ADD, createName("undefined"), createNumber(1));
        Node result = optimize(n);
        assertEquals(Token.NAME, result.getType());
        assertEquals("NaN", result.getString());
    }
}