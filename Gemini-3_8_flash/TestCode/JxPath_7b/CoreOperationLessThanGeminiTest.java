package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.jxpath.ri.compiler.CoreOperationLessThan
 * Target Defect: XPath 1.0 Section 3.4 Relational Node-Set Comparison
 *
 * Decision / Branch Coverage & Condition Matrix:
 * 1. computeValue(EvalContext):
 *    - Branch 1: (l < r) == true  -> returns Boolean.TRUE
 *    - Branch 2: (l < r) == false -> returns Boolean.FALSE
 *    - Boundary 1: l == r         -> returns Boolean.FALSE (strict inequality)
 *    - Boundary 2: l = -0.0, r = +0.0 -> IEEE 754 equality -> returns Boolean.FALSE
 *    - Boundary 3: NaN operand (l=NaN or r=NaN) -> returns Boolean.FALSE
 *    - Boundary 4: Infinities (NEGATIVE_INFINITY < POSITIVE_INFINITY, etc.)
 *    - Boundary 5: Double.MIN_VALUE vs Double.MAX_VALUE
 *    - String operands converted to numbers ("10" vs "2" -> 10.0 < 2.0 -> false)
 * 2. Defect Zone (Node-Set Evaluation Failure):
 *    - JXPath Bug: Overriding computeValue() directly converts args to double using
 *      InfoSetUtil.doubleValue(), which only evaluates the first element of a node-set / collection.
 *    - XPath 1.0 Specification: If an operand is a node-set, the comparison is true if
 *      THERE EXISTS any node in the node-set such that the relational comparison holds true.
 *    - Target 1: Left operand is NodeSet ($array < 0 where $array has [1, -1]) -> Expected: TRUE.
 *    - Target 2: Right operand is NodeSet (0 < $array where $array has [-5, 5]) -> Expected: TRUE.
 *    - Target 3: Both operands are NodeSets ($a < $b where $a=[10, 2], $b=[1, 5]) -> Expected: TRUE.
 * 3. getSymbol():
 *    - Verifies operator symbol string "<"
 * 4. Structural & Contract Integrity:
 *    - getArguments(), getPrecedence(), isSymmetric(), toString()
 * 5. Defensive / Guard Paths:
 *    - Null sub-expressions, null contexts.
 */
public class CoreOperationLessThanGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueStrictlyLessReturnsTrue() {
        Constant c1 = new Constant(1.5);
        Constant c2 = new Constant(2.5);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Object result = op.computeValue(null);
        assertSame(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStrictlyGreaterReturnsFalse() {
        Constant c1 = new Constant(5.0);
        Constant c2 = new Constant(3.0);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Object result = op.computeValue(null);
        assertSame(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueEqualNumbersReturnsFalse() {
        Constant c1 = new Constant(42);
        Constant c2 = new Constant(42);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Object result = op.computeValue(null);
        assertSame(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueWithNegativeNumbers() {
        Constant c1 = new Constant(-10);
        Constant c2 = new Constant(-2);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Object result = op.computeValue(null);
        assertSame(Boolean.TRUE, result);

        CoreOperationLessThan opReverse = new CoreOperationLessThan(c2, c1);
        assertSame(Boolean.FALSE, opReverse.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueViaComputeMethod() {
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(20);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Object result = op.compute(null);
        assertSame(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));
        assertEquals("<", op.getSymbol());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueSignedZeros() {
        Constant negativeZero = new Constant(-0.0);
        Constant positiveZero = new Constant(0.0);
        CoreOperationLessThan op = new CoreOperationLessThan(negativeZero, positiveZero);

        Object result = op.computeValue(null);
        assertSame(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueMinAndMaxDouble() {
        Constant minVal = new Constant(Double.MIN_VALUE);
        Constant maxVal = new Constant(Double.MAX_VALUE);
        CoreOperationLessThan op = new CoreOperationLessThan(minVal, maxVal);

        assertSame(Boolean.TRUE, op.computeValue(null));

        CoreOperationLessThan opInverted = new CoreOperationLessThan(maxVal, minVal);
        assertSame(Boolean.FALSE, opInverted.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueInfinities() {
        Constant negInf = new Constant(Double.NEGATIVE_INFINITY);
        Constant posInf = new Constant(Double.POSITIVE_INFINITY);
        Constant largeNum = new Constant(Double.MAX_VALUE);

        CoreOperationLessThan op1 = new CoreOperationLessThan(negInf, posInf);
        assertSame(Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThan op2 = new CoreOperationLessThan(posInf, negInf);
        assertSame(Boolean.FALSE, op2.computeValue(null));

        CoreOperationLessThan op3 = new CoreOperationLessThan(largeNum, posInf);
        assertSame(Boolean.TRUE, op3.computeValue(null));

        CoreOperationLessThan op4 = new CoreOperationLessThan(negInf, largeNum);
        assertSame(Boolean.TRUE, op4.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNaNComparisons() {
        Constant nan = new Constant(Double.NaN);
        Constant num = new Constant(100.0);

        CoreOperationLessThan nanLeft = new CoreOperationLessThan(nan, num);
        assertSame(Boolean.FALSE, nanLeft.computeValue(null));

        CoreOperationLessThan nanRight = new CoreOperationLessThan(num, nan);
        assertSame(Boolean.FALSE, nanRight.computeValue(null));

        CoreOperationLessThan nanBoth = new CoreOperationLessThan(nan, nan);
        assertSame(Boolean.FALSE, nanBoth.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNumericStrings() {
        Constant s1 = new Constant("10");
        Constant s2 = new Constant("2");
        CoreOperationLessThan op = new CoreOperationLessThan(s1, s2);

        // XPath converts strings to doubles: 10.0 < 2.0 -> false
        assertSame(Boolean.FALSE, op.computeValue(null));

        CoreOperationLessThan opReverse = new CoreOperationLessThan(s2, s1);
        assertSame(Boolean.TRUE, opReverse.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNonNumericString() {
        Constant str = new Constant("invalid");
        Constant num = new Constant(10);
        CoreOperationLessThan op = new CoreOperationLessThan(str, num);

        // InfoSetUtil converts non-numeric strings to NaN -> NaN < 10 -> false
        assertSame(Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNestedArithmeticExpressions() {
        CoreOperationAdd left = new CoreOperationAdd(new Expression[] { new Constant(1), new Constant(2) });
        CoreOperationAdd right = new CoreOperationAdd(new Expression[] { new Constant(2), new Constant(3) });
        CoreOperationLessThan op = new CoreOperationLessThan(left, right);

        assertSame(Boolean.TRUE, op.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Dedicated defect test targeting:
     * org.apache.commons.jxpath.ri.compiler.CoreOperationTest::testNodeSetOperations
     * Failure: Evaluating <$array > 0> expected:<true> but was:<false>
     * For "<", evaluating <$array < 0> when array has elements [1.0, -1.0].
     * The first element is 1.0 (1.0 < 0 is false), but the second element is -1.0 (-1.0 < 0 is true).
     * By XPath 1.0 section 3.4, relational comparisons on node-sets evaluate to true
     * if ANY element satisfies the comparison.
     */
    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsLeftOperandLessThanZero() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Double[] { 1.0, -1.0 });

        Object result = context.getValue("$array < 0");
        assertEquals("Evaluating <$array < 0> where $array contains -1.0 must be true per XPath 1.0",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsLeftOperandLessThanOne() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 1, -1 });

        Object result = context.getValue("$array < 1");
        assertEquals("Evaluating <$array < 1> where $array contains -1 must be true",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsRightOperand() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Double[] { -5.0, 5.0 });

        Object result = context.getValue("0 < $array");
        assertEquals("Evaluating <0 < $array> where $array contains 5.0 must be true",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsBothOperands() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("first", new Integer[] { 10, 2 });
        context.getVariables().declareVariable("second", new Integer[] { 1, 5 });

        Object result = context.getValue("$first < $second");
        assertEquals("Evaluating <$first < $second> where 2 < 5 must be true",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsWithList() {
        JXPathContext context = JXPathContext.newContext(new Object());
        List<Double> list = Arrays.asList(100.0, 20.0, 3.0);
        context.getVariables().declareVariable("list", list);

        Object result = context.getValue("$list < 10.0");
        assertEquals("Evaluating <$list < 10.0> where list contains 3.0 must be true",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsWhenNoElementsMatch() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 10, 20, 30 });

        Object result = context.getValue("$array < 5");
        assertEquals("Evaluating <$array < 5> when no element is < 5 must be false",
                Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperationsEmptyCollection() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("emptyList", new ArrayList<Integer>());

        Object result = context.getValue("$emptyList < 5");
        assertEquals("Evaluating relational comparison on empty node-set must be false",
                Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testComputeValueNullFirstArgumentThrowsNPE() {
        CoreOperationLessThan op = new CoreOperationLessThan(null, new Constant(1));
        op.computeValue(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testComputeValueNullSecondArgumentThrowsNPE() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), null);
        op.computeValue(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetArgumentsArrayIntegrity() {
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(20);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        Expression[] arguments = op.getArguments();
        assertNotNull(arguments);
        assertEquals(2, arguments.length);
        assertSame(c1, arguments[0]);
        assertSame(c2, arguments[1]);
    }

    @Test(timeout = 4000)
    public void testIsSymmetricIsFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));
        assertFalse("CoreOperationLessThan must not be symmetric", op.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testGetPrecedence() {
        CoreOperationLessThan op = new CoreOperationLessThan(new Constant(1), new Constant(2));
        // RELATIONAL_EXPRESSION_PRECEDENCE in CoreOperation is 3
        assertEquals(3, op.getPrecedence());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        Constant c1 = new Constant(7);
        Constant c2 = new Constant(9);
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        String text = op.toString();
        assertNotNull(text);
        assertTrue("toString must contain symbol '<'", text.contains("<"));
        assertTrue("toString must contain first operand", text.contains("7"));
        assertTrue("toString must contain second operand", text.contains("9"));
    }
}