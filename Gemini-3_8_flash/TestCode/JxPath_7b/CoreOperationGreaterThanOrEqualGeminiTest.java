package org.apache.commons.jxpath.ri.compiler;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThanOrEqual
 * Defect Reference: Defects4J JXPath (CoreOperationTest::testNodeSetOperations -> <$array > 0>)
 *
 * Decision / Condition Coverage:
 * 1. computeValue(EvalContext):
 *    - Branch [l >= r == true]:
 *        - Strict inequality (l > r) -> returns Boolean.TRUE
 *        - Equality boundary (l == r) -> returns Boolean.TRUE
 *    - Branch [l >= r == false]:
 *        - Strict inequality (l < r) -> returns Boolean.FALSE
 *    - IEEE 754 Floating-Point Boundaries:
 *        - NaN operands (NaN >= x, x >= NaN, NaN >= NaN) -> returns Boolean.FALSE
 *        - Infinities (+Inf >= +Inf, +Inf >= MAX_VALUE, -Inf >= -Inf, MAX_VALUE >= -Inf)
 *        - Signed Zeros (+0.0 >= -0.0, -0.0 >= +0.0) -> returns Boolean.TRUE
 *    - Type Conversions via InfoSetUtil:
 *        - String to Double ("100" >= "20", "20" >= "100", "invalid" >= 0)
 *        - Boolean to Double (true >= false -> 1.0 >= 0.0, false >= true -> 0.0 >= 1.0)
 * 2. Defect-Targeted Behavior (NodeSet / Collection Iteration):
 *    - XPath 1.0 Specification §3.4: Relational operations with a node-set evaluate to true if
 *      ANY node in the node-set satisfies the comparison with the operand.
 *    - Bug in defective implementation: Only the first node of the node-set / EvalContext is converted
 *      via InfoSetUtil.doubleValue(), causing comparisons like `[-5, 5] >= 0` to evaluate to FALSE
 *      instead of TRUE because -5.0 >= 0.0 is false.
 * 3. getSymbol():
 *    - Contract verification: Must return exact string ">=".
 * 4. Exception Guard:
 *    - computeValue with null sub-expressions -> NullPointerException.
 * ---------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import java.util.Arrays;
import java.util.Collections;

public class CoreOperationGreaterThanOrEqualGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueStrictlyGreaterThan() {
        Constant left = new Constant(Double.valueOf(10.0));
        Constant right = new Constant(Double.valueOf(5.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        Object result = op.computeValue(null);
        assertEquals("10.0 >= 5.0 must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueExactEquality() {
        Constant left = new Constant(Double.valueOf(42.0));
        Constant right = new Constant(Double.valueOf(42.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        Object result = op.computeValue(null);
        assertEquals("42.0 >= 42.0 must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStrictlyLessThan() {
        Constant left = new Constant(Double.valueOf(3.14));
        Constant right = new Constant(Double.valueOf(6.28));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        Object result = op.computeValue(null);
        assertEquals("3.14 >= 6.28 must evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueIntegerNumbers() {
        Constant left = new Constant(Integer.valueOf(100));
        Constant right = new Constant(Integer.valueOf(100));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        Object result = op.computeValue(null);
        assertEquals("100 >= 100 must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueIntegerLessThan() {
        Constant left = new Constant(Integer.valueOf(-10));
        Constant right = new Constant(Integer.valueOf(0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        Object result = op.computeValue(null);
        assertEquals("-10 >= 0 must evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValuePositiveAndNegativeInfinity() {
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant maxVal = new Constant(Double.valueOf(Double.MAX_VALUE));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(posInf, maxVal);
        assertEquals("+Infinity >= Double.MAX_VALUE must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(posInf, posInf);
        assertEquals("+Infinity >= +Infinity must be TRUE", Boolean.TRUE, op2.computeValue(null));

        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(negInf, negInf);
        assertEquals("-Infinity >= -Infinity must be TRUE", Boolean.TRUE, op3.computeValue(null));

        CoreOperationGreaterThanOrEqual op4 = new CoreOperationGreaterThanOrEqual(negInf, maxVal);
        assertEquals("-Infinity >= Double.MAX_VALUE must be FALSE", Boolean.FALSE, op4.computeValue(null));

        CoreOperationGreaterThanOrEqual op5 = new CoreOperationGreaterThanOrEqual(maxVal, negInf);
        assertEquals("Double.MAX_VALUE >= -Infinity must be TRUE", Boolean.TRUE, op5.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueSignedZeros() {
        Constant positiveZero = new Constant(Double.valueOf(0.0));
        Constant negativeZero = new Constant(Double.valueOf(-0.0));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(positiveZero, negativeZero);
        assertEquals("+0.0 >= -0.0 must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(negativeZero, positiveZero);
        assertEquals("-0.0 >= +0.0 must be TRUE", Boolean.TRUE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNaNOperands() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        Constant num = new Constant(Double.valueOf(1.0));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(nan, num);
        assertEquals("NaN >= 1.0 must be FALSE", Boolean.FALSE, op1.computeValue(null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(num, nan);
        assertEquals("1.0 >= NaN must be FALSE", Boolean.FALSE, op2.computeValue(null));

        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(nan, nan);
        assertEquals("NaN >= NaN must be FALSE", Boolean.FALSE, op3.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNumericStringsConversion() {
        Constant strGreater = new Constant("100.5");
        Constant strLesser = new Constant("20.25");

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(strGreater, strLesser);
        assertEquals("'100.5' >= '20.25' must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(strLesser, strGreater);
        assertEquals("'20.25' >= '100.5' must be FALSE", Boolean.FALSE, op2.computeValue(null));

        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(strGreater, strGreater);
        assertEquals("'100.5' >= '100.5' must be TRUE", Boolean.TRUE, op3.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNonNumericStringConvertsToNaN() {
        Constant text = new Constant("non-numeric");
        Constant zero = new Constant(Integer.valueOf(0));

        CoreOperationGreaterThanOrEqual op1 = new CoreOperationGreaterThanOrEqual(text, zero);
        assertEquals("'non-numeric' >= 0 converts to NaN >= 0 -> FALSE", Boolean.FALSE, op1.computeValue(null));

        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(zero, text);
        assertEquals("0 >= 'non-numeric' converts to 0 >= NaN -> FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueBooleanConversions() {
        // XPath 1.0 InfoSetUtil converts true -> 1.0, false -> 0.0
        Expression trueExpr = new CoreOperationGreaterThanOrEqual(new Constant(Integer.valueOf(1)), new Constant(Integer.valueOf(0)));
        Expression falseExpr = new CoreOperationGreaterThanOrEqual(new Constant(Integer.valueOf(0)), new Constant(Integer.valueOf(1)));

        CoreOperationGreaterThanOrEqual trueVsFalse = new CoreOperationGreaterThanOrEqual(trueExpr, falseExpr);
        assertEquals("true (1.0) >= false (0.0) must be TRUE", Boolean.TRUE, trueVsFalse.computeValue(null));

        CoreOperationGreaterThanOrEqual falseVsTrue = new CoreOperationGreaterThanOrEqual(falseExpr, trueExpr);
        assertEquals("false (0.0) >= true (1.0) must be FALSE", Boolean.FALSE, falseVsTrue.computeValue(null));

        CoreOperationGreaterThanOrEqual falseVsFalse = new CoreOperationGreaterThanOrEqual(falseExpr, falseExpr);
        assertEquals("false (0.0) >= false (0.0) must be TRUE", Boolean.TRUE, falseVsFalse.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Evaluating relational operations on multi-node sets.
     * XPath 1.0 §3.4 requires that $array >= value is true if ANY node satisfies the condition.
     * In defective JXPath, InfoSetUtil.doubleValue() is invoked directly on the EvalContext/NodeSet,
     * which only inspects the first element. When the first element is -5 and second is 5,
     * defective code computes -5.0 >= 0 -> false instead of checking all nodes.
     */
    @Test(timeout = 4000)
    public void testDefectNodeSetGreaterThanOrEqualWithNegativeFirstElement() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { Integer.valueOf(-5), Integer.valueOf(5) });

        Object result = context.getValue("$array >= 0");
        assertEquals("Evaluating <$array >= 0> when array contains [-5, 5] must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetRightOperandGreaterThanOrEqual() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { Integer.valueOf(10), Integer.valueOf(-2) });

        // 0 >= $array should be TRUE because 0 >= -2 is true, even though 0 >= 10 is false
        Object result = context.getValue("0 >= $array");
        assertEquals("Evaluating <0 >= $array> when array contains [10, -2] must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectCollectionMultiElementGreaterThanOrEqual() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("list", Arrays.asList(Double.valueOf(-100.0), Double.valueOf(0.0)));

        Object result = context.getValue("$list >= 0");
        assertEquals("Evaluating <$list >= 0> when list contains [-100.0, 0.0] must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectDirectAstEvaluationWithMultiNodeContext() {
        VariableReference varRef = new VariableReference(new QName("array"));
        Constant zero = new Constant(Integer.valueOf(0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(varRef, zero);

        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        jxContext.getVariables().declareVariable("array", new Integer[] { Integer.valueOf(-10), Integer.valueOf(20) });
        EvalContext rootContext = jxContext.getAbsoluteRootContext();

        Object result = op.computeValue(rootContext);
        assertEquals("Direct AST evaluation of $array >= 0 must be TRUE when second node satisfies condition",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetAllElementsFailingCondition() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { Integer.valueOf(-10), Integer.valueOf(-5) });

        Object result = context.getValue("$array >= 0");
        assertEquals("Evaluating <$array >= 0> when array contains [-10, -5] must be FALSE", Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testComputeValueNullFirstArgumentThrowsNPE() {
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(null, new Constant(Integer.valueOf(1)));
        op.computeValue(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testComputeValueNullSecondArgumentThrowsNPE() {
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(new Constant(Integer.valueOf(1)), null);
        op.computeValue(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testComputeValueBothArgumentsNullThrowsNPE() {
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(null, null);
        op.computeValue(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSymbolExactMatch() {
        Constant left = new Constant(Integer.valueOf(1));
        Constant right = new Constant(Integer.valueOf(2));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        assertEquals("getSymbol must return '>='", ">=", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testExpressionArgumentsIntegrity() {
        Constant left = new Constant(Integer.valueOf(7));
        Constant right = new Constant(Integer.valueOf(14));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        assertNotNull("Arguments array must not be null", op.getArguments());
        assertEquals("Operation must have exactly 2 arguments", 2, op.getArguments().length);
        assertSame("First argument must match constructor arg1", left, op.getArguments()[0]);
        assertSame("Second argument must match constructor arg2", right, op.getArguments()[1]);
    }

    @Test(timeout = 4000)
    public void testToStringRepresentationContainsSymbol() {
        Constant left = new Constant(Integer.valueOf(10));
        Constant right = new Constant(Integer.valueOf(20));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        String str = op.toString();
        assertNotNull("toString() must not return null", str);
        assertTrue("toString() must contain the '>=' symbol", str.contains(">="));
    }

    @Test(timeout = 4000)
    public void testOperationPrecedenceAndSymmetry() {
        Constant left = new Constant(Integer.valueOf(1));
        Constant right = new Constant(Integer.valueOf(2));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);

        assertFalse("GreaterThanOrEqual is an asymmetric relational operation", op.isSymmetric());
        assertTrue("Precedence must be a positive integer", op.getPrecedence() > 0);
    }
}