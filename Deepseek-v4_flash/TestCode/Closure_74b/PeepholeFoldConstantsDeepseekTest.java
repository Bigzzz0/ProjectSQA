package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Strategic test suite for PeepholeFoldConstants targeting line/branch coverage
 * and known Defects4J faults.
 *
 * [Branch & Defect Analysis Matrix]
 * - Arithmetic: ADD/SUB/MUL/DIV/MOD/BIT* -> fold when both operands are NUMBER
 * - Comparison: EQ/NE/GT/LT/GE/LE/SHEQ/SHNE -> fold for literals, handle undefined/null
 * - Unary: NOT/POS/NEG/BITNOT -> fold for numeric/boolean literals
 * - Shift: LSH/RSH/URSH -> fold within 32-bit integer range, 0-31 shift amount
 * - String: concat, indexOf, substr, substring, join, length
 * - Object: GETELEM/GETPROP on array/object literals
 * - Known defect: testFoldComparison3, testInvertibleOperators, testCommutativeOperators
 *   -> Ensure non-number comparisons, commutative reassociation, and invertible folding
 */
public class PeepholeFoldConstantsDeepseekTest {

    private Compiler compiler;
    private PeepholeFoldConstants peephole;

    @Before
    public void setUp() {
        compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        peephole = new PeepholeFoldConstants();
        peephole.setCompiler(compiler);
    }

    private Node fold(Node n) {
        return peephole.optimizeSubtree(n);
    }

    // ---------------------------------------------------------------
    // Partition A: Core Arithmetic
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldAddNumbers() {
        Node node = new Node(Token.ADD, Node.newNumber(2), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldSubtract() {
        Node node = new Node(Token.SUB, Node.newNumber(10), Node.newNumber(4));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldMultiply() {
        Node node = new Node(Token.MUL, Node.newNumber(3), Node.newNumber(7));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(21.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldDivide() {
        Node node = new Node(Token.DIV, Node.newNumber(15), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldMod() {
        Node node = new Node(Token.MOD, Node.newNumber(10), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(1.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldBitwiseAnd() {
        Node node = new Node(Token.BITAND, Node.newNumber(6), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldBitwiseOr() {
        Node node = new Node(Token.BITOR, Node.newNumber(5), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(7.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldBitwiseXor() {
        Node node = new Node(Token.BITXOR, Node.newNumber(5), Node.newNumber(3));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition B: Shift Operators
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldShiftLeft() {
        Node node = new Node(Token.LSH, Node.newNumber(5), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(20.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldShiftRight() {
        Node node = new Node(Token.RSH, Node.newNumber(-5), Node.newNumber(1));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(-3.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnsignedShift() {
        Node node = new Node(Token.URSH, Node.newNumber(5), Node.newNumber(1));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition C: Comparison Operators (including known defect area)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldComparisonEq() {
        Node node = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(1));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonNe() {
        Node node = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonLt() {
        Node node = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonGt() {
        Node node = new Node(Token.GT, Node.newNumber(3), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonLe() {
        Node node = new Node(Token.LE, Node.newNumber(2), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonGe() {
        Node node = new Node(Token.GE, Node.newNumber(2), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonSheq() {
        Node node = new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(1));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonShne() {
        Node node = new Node(Token.SHNE, Node.newNumber(1), Node.newNumber(2));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    // Comparison with undefined and null (defect-prone area)
    @Test(timeout = 4000)
    public void testFoldComparisonUndefinedEqNull() {
        Node undefined = Node.newString(Token.NAME, "undefined");
        Node nullNode = new Node(Token.NULL);
        Node node = new Node(Token.EQ, undefined, nullNode);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());  // undefined == null is true
    }

    @Test(timeout = 4000)
    public void testFoldComparisonUndefinedSheqNull() {
        Node undefined = Node.newString(Token.NAME, "undefined");
        Node nullNode = new Node(Token.NULL);
        Node node = new Node(Token.SHEQ, undefined, nullNode);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertFalse(result.getBoolean()); // undefined !== null
    }

    @Test(timeout = 4000)
    public void testFoldComparisonStringEq() {
        Node left = Node.newString("hello");
        Node right = Node.newString("hello");
        Node node = new Node(Token.EQ, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonBooleanEq() {
        Node left = new Node(Token.TRUE);
        Node right = new Node(Token.TRUE);
        Node node = new Node(Token.EQ, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldComparisonSameNameLt() {
        Node left = Node.newString(Token.NAME, "a");
        Node right = Node.newString(Token.NAME, "a");
        Node node = new Node(Token.LT, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertFalse(result.getBoolean()); // a < a is false
    }

    @Test(timeout = 4000)
    public void testFoldComparisonThisEq() {
        Node left = new Node(Token.THIS);
        Node right = new Node(Token.THIS);
        Node node = new Node(Token.EQ, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    // ---------------------------------------------------------------
    // Partition D: Unary Operators
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldUnaryNotFalse() {
        Node node = new Node(Token.NOT, new Node(Token.FALSE));
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    @Test(timeout = 4000)
    public void testFoldUnaryPosNumber() {
        Node node = new Node(Token.POS, Node.newNumber(42));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(42.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnaryNegNumber() {
        Node node = new Node(Token.NEG, Node.newNumber(5));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(-5.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldUnaryBitnot() {
        Node node = new Node(Token.BITNOT, Node.newNumber(5));
        Node result = fold(node);
        assertTrue(result.isNumber());
        assertEquals(-6.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldTypeofString() {
        Node node = new Node(Token.TYPEOF, Node.newString("foo"));
        Node result = fold(node);
        assertTrue(result.isString());
        assertEquals("string", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldTypeofNumber() {
        Node node = new Node(Token.TYPEOF, Node.newNumber(42));
        Node result = fold(node);
        assertTrue(result.isString());
        assertEquals("number", result.getString());
    }

    // ---------------------------------------------------------------
    // Partition E: String Methods (indexOf, substr, substring, length)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldStringIndexOf() {
        // "abc".indexOf("bc") => 1
        Node target = Node.newString("abc");
        Node method = Node.newString("indexOf");
        Node getprop = new Node(Token.GETPROP, target, method);
        Node arg = Node.newString("bc");
        Node call = new Node(Token.CALL, getprop, arg);
        Node result = fold(call);
        assertTrue(result.isNumber());
        assertEquals(1.0, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstr() {
        // "abcdef".substr(1,3) => "bcd"
        Node target = Node.newString("abcdef");
        Node method = Node.newString("substr");
        Node getprop = new Node(Token.GETPROP, target, method);
        Node arg1 = Node.newNumber(1);
        Node arg2 = Node.newNumber(3);
        Node call = new Node(Token.CALL, getprop, arg1, arg2);
        Node result = fold(call);
        assertTrue(result.isString());
        assertEquals("bcd", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringSubstring() {
        // "abcdef".substring(1,3) => "bc"
        Node target = Node.newString("abcdef");
        Node method = Node.newString("substring");
        Node getprop = new Node(Token.GETPROP, target, method);
        Node arg1 = Node.newNumber(1);
        Node arg2 = Node.newNumber(3);
        Node call = new Node(Token.CALL, getprop, arg1, arg2);
        Node result = fold(call);
        assertTrue(result.isString());
        assertEquals("bc", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldStringLength() {
        Node target = Node.newString("hello");
        Node prop = Node.newString("length");
        Node getprop = new Node(Token.GETPROP, target, prop);
        Node result = fold(getprop);
        assertTrue(result.isNumber());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition F: Array Join and Length
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldArrayJoin() {
        Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
        Node method = Node.newString("join");
        Node getprop = new Node(Token.GETPROP, array, method);
        Node sep = Node.newString("");
        Node call = new Node(Token.CALL, getprop, sep);
        Node result = fold(call);
        assertTrue(result.isString());
        assertEquals("ab", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldArrayLength() {
        Node array = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
        Node prop = Node.newString("length");
        Node getprop = new Node(Token.GETPROP, array, prop);
        Node result = fold(getprop);
        assertTrue(result.isNumber());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition G: Object/Array Access (GETELEM, GETPROP on literals)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldGetElemArray() {
        Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
        Node index = Node.newNumber(1);
        Node getelem = new Node(Token.GETELEM, array, index);
        Node result = fold(getelem);
        assertTrue(result.isString());
        assertEquals("b", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldGetPropObjectLiteral() {
        Node key = Node.newString("key");
        Node value = Node.newNumber(42);
        Node obj = new Node(Token.OBJECTLIT, Node.newString("key"), value);
        Node prop = Node.newString("key");
        Node getprop = new Node(Token.GETPROP, obj, prop);
        Node result = fold(getprop);
        assertTrue(result.isNumber());
        assertEquals(42.0, result.getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition H: Defect-Targeted Tests
    // ---------------------------------------------------------------

    /** 
     * Targets comparison of two non-literal NAME nodes with same string.
     * Known to fail in testFoldComparison3.
     */
    @Test(timeout = 4000)
    public void testFoldComparisonSameNameGt() {
        Node left = Node.newString(Token.NAME, "x");
        Node right = Node.newString(Token.NAME, "x");
        Node node = new Node(Token.GT, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertFalse(result.getBoolean()); // x > x is false
    }

    /**
     * Tests commutative operator folding: 3 * a * 5 -> a * 15
     * Corresponds to testCommutativeOperators defect.
     */
    @Test(timeout = 4000)
    public void testFoldCommutativeMultiply() {
        // Build: (3 * a) * 5
        Node a = Node.newString(Token.NAME, "a");
        Node three = Node.newNumber(3);
        Node five = Node.newNumber(5);
        Node innerMul = new Node(Token.MUL, three, a);
        Node outerMul = new Node(Token.MUL, innerMul, five);
        Node result = fold(outerMul);
        // Expected: a * 15
        assertTrue(result.isMul());
        Node left = result.getFirstChild();
        Node right = result.getLastChild();
        assertTrue(left.isName());
        assertEquals("a", left.getString());
        assertTrue(right.isNumber());
        assertEquals(15.0, right.getDouble(), 0.0);
    }

    /**
     * Tests invertible operator folding (e.g., !0 should not fold to false,
     * but !(false) should). 
     * Corresponds to testInvertibleOperators defect.
     */
    @Test(timeout = 4000)
    public void testFoldInvertibleNotZero() {
        // !0 should remain !0 to preserve boolean conversion
        Node zero = Node.newNumber(0);
        Node not = new Node(Token.NOT, zero);
        Node result = fold(not);
        // Should still be a NOT node because code explicitly skips !0 and !1
        assertEquals(Token.NOT, result.getType());
        Node child = result.getFirstChild();
        assertTrue(child.isNumber());
        assertEquals(0.0, child.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFoldInvertibleNotFalse() {
        // !false should fold to true
        Node falseNode = new Node(Token.FALSE);
        Node not = new Node(Token.NOT, falseNode);
        Node result = fold(not);
        assertTrue(result.isBoolean());
        assertTrue(result.getBoolean());
    }

    // ---------------------------------------------------------------
    // Partition I: Edge Cases (division by zero, shift errors)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldDivideByZeroRemainsUnchanged() {
        Node node = new Node(Token.DIV, Node.newNumber(1), Node.newNumber(0));
        Node result = fold(node);
        // Should remain unchanged because performArithmeticOp returns null
        assertEquals(Token.DIV, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldModByZeroRemainsUnchanged() {
        Node node = new Node(Token.MOD, Node.newNumber(5), Node.newNumber(0));
        Node result = fold(node);
        assertEquals(Token.MOD, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldShiftAmountOutOfBounds() {
        Node node = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(32));
        Node result = fold(node);
        // Should remain unchanged because shift amount >=32 or <0
        assertEquals(Token.LSH, result.getType());
    }

    @Test(timeout = 4000)
    public void testFoldShiftFractionalOperand() {
        Node node = new Node(Token.RSH, Node.newNumber(1.5), Node.newNumber(1));
        Node result = fold(node);
        // Should remain unchanged because left is fractional
        assertEquals(Token.RSH, result.getType());
    }

    // ---------------------------------------------------------------
    // Partition J: Instanceof folding
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldInstanceofNonObject() {
        // "foo" instanceof Object => false
        Node left = Node.newString("foo");
        Node right = Node.newString(Token.NAME, "Object");
        Node node = new Node(Token.INSTANCEOF, left, right);
        Node result = fold(node);
        assertTrue(result.isBoolean());
        assertFalse(result.getBoolean());
    }

    // ---------------------------------------------------------------
    // Partition K: Assignment folding (x = x + y -> x += y)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldAssignToCompound() {
        Node x = Node.newString(Token.NAME, "x");
        Node y = Node.newNumber(5);
        Node add = new Node(Token.ADD, x.cloneTree(), y);
        Node assign = new Node(Token.ASSIGN, x, add);
        Node result = fold(assign);
        // Should become x += 5
        assertEquals(Token.ASSIGN_ADD, result.getType());
        assertEquals("x", result.getFirstChild().getString());
        assertTrue(result.getLastChild().isNumber());
        assertEquals(5.0, result.getLastChild().getDouble(), 0.0);
    }

    // ---------------------------------------------------------------
    // Partition L: String addition folding (tryFoldAddConstantString & tryFoldChildAddString)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFoldAddStringConstantBoth() {
        Node left = Node.newString("Hello ");
        Node right = Node.newString("World");
        Node node = new Node(Token.ADD, left, right);
        Node result = fold(node);
        assertTrue(result.isString());
        assertEquals("Hello World", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldAddStringChildLeft() {
        // "a" + ("b" + "c") -> "abc"
        Node left = Node.newString("a");
        Node innerAdd = new Node(Token.ADD, Node.newString("b"), Node.newString("c"));
        Node node = new Node(Token.ADD, left, innerAdd);
        Node result = fold(node);
        assertTrue(result.isString());
        assertEquals("abc", result.getString());
    }

    @Test(timeout = 4000)
    public void testFoldAddStringChildRight() {
        // ("a" + "b") + "c" -> "abc"
        Node innerAdd = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
        Node right = Node.newString("c");
        Node node = new Node(Token.ADD, innerAdd, right);
        Node result = fold(node);
        assertTrue(result.isString());
        assertEquals("abc", result.getString());
    }
}