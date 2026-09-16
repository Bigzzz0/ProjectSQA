package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationLessThanOrEqual
 *
 * Decision / Condition Coverage Paths:
 * 1. Constructor:
 *    - super(new Expression[] { arg1, arg2 }) properly sets args array.
 * 2. computeValue(EvalContext context):
 *    - Left <= Right : evaluates (l <= r ? Boolean.TRUE : Boolean.FALSE)
 *      - Branch T: l < r  (e.g., 1.0 <= 2.0 -> TRUE)
 *      - Branch T: l == r (e.g., 2.0 <= 2.0 -> TRUE)
 *      - Branch F: l > r  (e.g., 3.0 <= 2.0 -> FALSE)
 *    - Conversions via InfoSetUtil:
 *      - Numeric types (Integer, Double)
 *      - String conversion to numeric representation
 *      - Boolean conversion (false -> 0.0, true -> 1.0)
 *      - Boundary/Special IEEE-754:
 *        - NaN comparisons: NaN <= x, x <= NaN, NaN <= NaN (all false)
 *        - Infinities: -Inf <= +Inf (true), +Inf <= -Inf (false)
 *        - Signed zeros: -0.0 <= +0.0 (true), +0.0 <= -0.0 (true)
 *        - Double.MIN_VALUE, Double.MAX_VALUE boundaries
 * 3. getSymbol():
 *    - Returns exact operator string "<=".
 * 4. Defect Zone (Defects4J Ground Truth: CoreOperationTest::testNodeSetOperations):
 *    - When an operand is a NodeSet or Collection/Array, XPath 1.0 Section 3.4 requires
 *      existential quantification: true if ANY node in the node-set satisfies the comparison.
 *    - On the defective version, InfoSetUtil.doubleValue directly converts the collection/context
 *      to NaN, returning FALSE for comparisons that should evaluate to TRUE (e.g. $array <= 2).
 */
public class CoreOperationLessThanOrEqualGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueStrictlyLessThan() {
        Constant c1 = new Constant(1.5);
        Constant c2 = new Constant(2.5);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Type must be Boolean", Boolean.class, result.getClass());
        assertEquals("1.5 <= 2.5 should evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStrictlyEqual() {
        Constant c1 = new Constant(42.0);
        Constant c2 = new Constant(42.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Type must be Boolean", Boolean.class, result.getClass());
        assertEquals("42.0 <= 42.0 should evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStrictlyGreaterThan() {
        Constant c1 = new Constant(100.0);
        Constant c2 = new Constant(50.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Type must be Boolean", Boolean.class, result.getClass());
        assertEquals("100.0 <= 50.0 should evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testGetSymbol() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("getSymbol must return '<='", "<=", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testComputeWrapperDelegation() {
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(20);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        // compute(EvalContext) delegates to computeValue(EvalContext)
        Object result = op.compute(null);
        assertEquals("Delegated compute() should yield Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueWithNumericStrings() {
        Constant c1 = new Constant("12.5");
        Constant c2 = new Constant("12.50");
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        Object result = op.computeValue(null);
        assertEquals("String '12.5' <= '12.50' converted to numbers should be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueNegativeNumbers() {
        Constant c1 = new Constant(-10.0);
        Constant c2 = new Constant(-5.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("-10.0 <= -5.0 should be TRUE", Boolean.TRUE, op.computeValue(null));

        Constant c3 = new Constant(-5.0);
        Constant c4 = new Constant(-10.0);
        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(c3, c4);
        assertEquals("-5.0 <= -10.0 should be FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & IEEE-754 Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBvaNaNLeftOperand() {
        Constant c1 = new Constant(Double.NaN);
        Constant c2 = new Constant(10.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("NaN <= 10.0 must be FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaNaNRightOperand() {
        Constant c1 = new Constant(10.0);
        Constant c2 = new Constant(Double.NaN);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("10.0 <= NaN must be FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaBothOperandsNaN() {
        Constant c1 = new Constant(Double.NaN);
        Constant c2 = new Constant(Double.NaN);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("NaN <= NaN must be FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaNonNumericStringYieldsNaN() {
        Constant c1 = new Constant("invalid_num");
        Constant c2 = new Constant(10.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertEquals("Non-numeric string parsing to NaN <= 10.0 must be FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaEmptyStringConvertsToZero() {
        Constant c1 = new Constant("");
        Constant c2 = new Constant(0.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        // XPath 1.0 / InfoSetUtil empty string converts to 0.0
        assertEquals("'' <= 0.0 should be TRUE", Boolean.TRUE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaSignedZeros() {
        Constant negZero = new Constant(-0.0);
        Constant posZero = new Constant(0.0);

        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(negZero, posZero);
        assertEquals("-0.0 <= +0.0 must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(posZero, negZero);
        assertEquals("+0.0 <= -0.0 must be TRUE", Boolean.TRUE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaInfinities() {
        Constant negInf = new Constant(Double.NEGATIVE_INFINITY);
        Constant posInf = new Constant(Double.POSITIVE_INFINITY);

        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(negInf, posInf);
        assertEquals("-Infinity <= +Infinity must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(posInf, negInf);
        assertEquals("+Infinity <= -Infinity must be FALSE", Boolean.FALSE, op2.computeValue(null));

        CoreOperationLessThanOrEqual op3 = new CoreOperationLessThanOrEqual(posInf, posInf);
        assertEquals("+Infinity <= +Infinity must be TRUE", Boolean.TRUE, op3.computeValue(null));

        CoreOperationLessThanOrEqual op4 = new CoreOperationLessThanOrEqual(negInf, negInf);
        assertEquals("-Infinity <= -Infinity must be TRUE", Boolean.TRUE, op4.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBvaMaxAndMinValues() {
        Constant minVal = new Constant(Double.MIN_VALUE);
        Constant maxVal = new Constant(Double.MAX_VALUE);

        CoreOperationLessThanOrEqual op1 = new CoreOperationLessThanOrEqual(minVal, maxVal);
        assertEquals("Double.MIN_VALUE <= Double.MAX_VALUE must be TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(maxVal, minVal);
        assertEquals("Double.MAX_VALUE <= Double.MIN_VALUE must be FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J Ground Truth:
     * org.apache.commons.jxpath.ri.compiler.CoreOperationTest::testNodeSetOperations
     *
     * In defective JXPath, relational operations on collections/node-sets convert
     * the collection object directly via InfoSetUtil.doubleValue() which yields NaN,
     * failing the existential quantification requirement of XPath 1.0 (Section 3.4).
     */
    @Test(timeout = 4000)
    public void testDefectNodeSetVariableLessThanOrEqualConstant() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 1, 2, 3 });

        // There exists element 1 <= 2, so this must evaluate to TRUE
        Object result = context.getValue("$array <= 2");
        assertNotNull("Result should not be null", result);
        assertEquals("Evaluating <$array <= 2> with array {1, 2, 3} must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectConstantLessThanOrEqualNodeSetVariable() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 1, 2, 3 });

        // There exists element 2 <= 2 (and 2 <= 3), so this must evaluate to TRUE
        Object result = context.getValue("2 <= $array");
        assertNotNull("Result should not be null", result);
        assertEquals("Evaluating <2 <= $array> with array {1, 2, 3} must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectDirectAstEvaluationWithVariableContext() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Double[] { 1.0, 2.0, 3.0 });
        EvalContext evalContext = ((JXPathContextReferenceImpl) context).getAbsoluteRootContext();

        VariableReference varRef = new VariableReference(new QName("array"));
        Constant threshold = new Constant(1.0);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(varRef, threshold);

        Object result = op.computeValue(evalContext);
        assertEquals("Direct AST evaluation of $array <= 1.0 must be TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetWithNoElementsSatisfyingCondition() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 5, 6, 7 });

        Object result = context.getValue("$array <= 4");
        assertEquals("Evaluating <$array <= 4> with array {5, 6, 7} must be FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectEmptyNodeSetComparison() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("emptyArray", new Integer[0]);

        Object resultLhs = context.getValue("$emptyArray <= 10");
        assertEquals("Evaluating <$emptyArray <= 10> must be FALSE for empty set", Boolean.FALSE, resultLhs);

        Object resultRhs = context.getValue("10 <= $emptyArray");
        assertEquals("Evaluating <10 <= $emptyArray> must be FALSE for empty set", Boolean.FALSE, resultRhs);
    }

    @Test(timeout = 4000)
    public void testDefectBetweenTwoNodeSets() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("arrayA", new Integer[] { 10, 20 });
        context.getVariables().declareVariable("arrayB", new Integer[] { 5, 15 });

        // 10 in arrayA <= 15 in arrayB -> exists pair, must evaluate to TRUE
        Object result = context.getValue("$arrayA <= $arrayB");
        assertEquals("Evaluating <$arrayA <= $arrayB> where 10 <= 15 must be TRUE", Boolean.TRUE, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullFirstArgumentThrowsNullPointerException() {
        Constant c2 = new Constant(5);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(null, c2);

        try {
            op.computeValue(null);
            fail("Expected NullPointerException when args[0] is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testNullSecondArgumentThrowsNullPointerException() {
        Constant c1 = new Constant(5);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, null);

        try {
            op.computeValue(null);
            fail("Expected NullPointerException when args[1] is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Reflection & Hierarchy Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testHierarchyAndToStringContract() {
        Constant c1 = new Constant(3);
        Constant c2 = new Constant(7);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertTrue("Must be instance of CoreOperationRelationalExpression",
                op instanceof CoreOperationRelationalExpression);
        assertTrue("Must be instance of CoreOperation",
                op instanceof CoreOperation);

        String repr = op.toString();
        assertNotNull("toString() should not be null", repr);
        assertTrue("toString() must contain the symbol '<='", repr.contains("<="));
    }

    @Test(timeout = 4000)
    public void testIsSymmetricProperty() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        // LessThanOrEqual is not symmetric (a <= b does not imply b <= a)
        assertFalse("LessThanOrEqual must not be symmetric", op.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testPrecedenceLevel() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        assertTrue("Precedence should be a positive integer", op.getPrecedence() > 0);
    }
}