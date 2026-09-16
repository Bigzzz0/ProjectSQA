/*
 * [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression
 *
 * Decision / Condition Coverage Targets:
 * 1. getPrecedence(): Verifies constant relational operator precedence (3).
 * 2. isSymmetric(): Verifies relational operator symmetry property (false).
 * 3. compute(Object, Object) -> reduce(Object):
 *    - left / right instanceof SelfContext (unwrapping to single node pointer).
 *    - left / right instanceof Collection (converts to Iterator).
 *    - left / right instanceof InitialContext (triggers reset() invocation).
 * 4. compute(Object, Object) Iteration branches:
 *    - left is Iterator && right is Iterator -> findMatch(Iterator, Iterator).
 *    - left is Iterator && right is scalar -> containsMatch(Iterator, scalar).
 *    - left is scalar && right is Iterator -> containsMatch(Iterator, scalar).
 *    - findMatch: cartesian match succeeds (returns true) vs empty / no match (returns false).
 *    - containsMatch: element match succeeds (returns true) vs exhausted iterator (returns false).
 * 5. Value comparison logic (ld == rd ? 0 : ld < rd ? -1 : 1):
 *    - Equal values (ld == rd -> 0)
 *    - Less than (ld < rd -> -1)
 *    - Greater than (ld > rd -> 1)
 *
 * Known Defect (Defects4J Ground Truth):
 * - CoreOperationTest::testNan:
 *   When either or both operands are NaN, relational comparisons in XPath ("<", "<=", ">", ">=")
 *   must strictly evaluate to FALSE according to the XPath 1.0 specification.
 *   The defective implementation calculates:
 *     ld == rd ? 0 : ld < rd ? -1 : 1
 *   Because Double.NaN == Double.NaN is false, and Double.NaN < Double.NaN is false, it returns 1.
 *   Consequently, evaluateCompare(1) checks (1 > 0) or (1 >= 0), evaluating to TRUE instead of FALSE.
 */
package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionGeminiTest {

    // Concrete test harness subclass exposing protected methods and customizable evaluateCompare
    private static class RelationalExpressionHarness extends CoreOperationRelationalExpression {
        private final int comparisonMode; // 1: '>', 2: '>=', 3: '<', 4: '<='

        public RelationalExpressionHarness(Expression left, Expression right, int comparisonMode) {
            super(new Expression[]{left, right});
            this.comparisonMode = comparisonMode;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            switch (comparisonMode) {
                case 1: return compare > 0;
                case 2: return compare >= 0;
                case 3: return compare < 0;
                case 4: return compare <= 0;
                default: return false;
            }
        }

        @Override
        public String getSymbol() {
            return "test_rel";
        }
    }

    // Harness for wrapping direct objects as constant expressions
    private static class ObjectConstantExpression extends Expression {
        private final Object val;

        public ObjectConstantExpression(Object val) {
            this.val = val;
        }

        @Override
        public Object compute(EvalContext context) {
            return val;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return val;
        }
    }

    // Stub InitialContext to verify that reset() is safely invoked
    private static class MockInitialContext extends InitialContext {
        private boolean resetCalled = false;
        private final List<Object> values;
        private int index = 0;

        public MockInitialContext(List<Object> values) {
            super(new RootContext(null, null));
            this.values = values;
        }

        @Override
        public void reset() {
            resetCalled = true;
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < values.size();
        }

        @Override
        public Object next() {
            return values.get(index++);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOperatorPrecedenceAndSymmetry() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(10), new Constant(20), 3);
        assertEquals("Precedence must be 3", 3, expr.getPrecedence());
        assertFalse("Relational operations must not be symmetric", expr.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testBasicNumericComparisonsLessThan() {
        // 10 < 20 -> true
        RelationalExpressionHarness exprTrue = new RelationalExpressionHarness(
                new Constant(10), new Constant(20), 3);
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        // 20 < 10 -> false
        RelationalExpressionHarness exprFalse = new RelationalExpressionHarness(
                new Constant(20), new Constant(10), 3);
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));

        // 15 < 15 -> false
        RelationalExpressionHarness exprEqual = new RelationalExpressionHarness(
                new Constant(15), new Constant(15), 3);
        assertEquals(Boolean.FALSE, exprEqual.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBasicNumericComparisonsLessThanOrEqual() {
        // 10 <= 20 -> true
        RelationalExpressionHarness exprLess = new RelationalExpressionHarness(
                new Constant(10), new Constant(20), 4);
        assertEquals(Boolean.TRUE, exprLess.computeValue(null));

        // 20 <= 20 -> true
        RelationalExpressionHarness exprEqual = new RelationalExpressionHarness(
                new Constant(20), new Constant(20), 4);
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        // 25 <= 20 -> false
        RelationalExpressionHarness exprGreater = new RelationalExpressionHarness(
                new Constant(25), new Constant(20), 4);
        assertEquals(Boolean.FALSE, exprGreater.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBasicNumericComparisonsGreaterThan() {
        // 30 > 20 -> true
        RelationalExpressionHarness exprTrue = new RelationalExpressionHarness(
                new Constant(30), new Constant(20), 1);
        assertEquals(Boolean.TRUE, exprTrue.computeValue(null));

        // 20 > 30 -> false
        RelationalExpressionHarness exprFalse = new RelationalExpressionHarness(
                new Constant(20), new Constant(30), 1);
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));

        // 20 > 20 -> false
        RelationalExpressionHarness exprEqual = new RelationalExpressionHarness(
                new Constant(20), new Constant(20), 1);
        assertEquals(Boolean.FALSE, exprEqual.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBasicNumericComparisonsGreaterThanOrEqual() {
        // 30 >= 20 -> true
        RelationalExpressionHarness exprGreater = new RelationalExpressionHarness(
                new Constant(30), new Constant(20), 2);
        assertEquals(Boolean.TRUE, exprGreater.computeValue(null));

        // 20 >= 20 -> true
        RelationalExpressionHarness exprEqual = new RelationalExpressionHarness(
                new Constant(20), new Constant(20), 2);
        assertEquals(Boolean.TRUE, exprEqual.computeValue(null));

        // 10 >= 20 -> false
        RelationalExpressionHarness exprLess = new RelationalExpressionHarness(
                new Constant(10), new Constant(20), 2);
        assertEquals(Boolean.FALSE, exprLess.computeValue(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA), Collections & Iterators
    // =========================================================================

    @Test(timeout = 4000)
    public void testLeftCollectionContainsMatch() {
        List<Double> leftList = Arrays.asList(5.0, 15.0, 25.0);
        // leftList contains an element > 20 (namely 25.0), so leftList > 20 -> true
        RelationalExpressionHarness exprMatch = new RelationalExpressionHarness(
                new ObjectConstantExpression(leftList), new Constant(20), 1);
        assertEquals(Boolean.TRUE, exprMatch.computeValue(null));

        // leftList contains no element > 50 -> false
        RelationalExpressionHarness exprNoMatch = new RelationalExpressionHarness(
                new ObjectConstantExpression(leftList), new Constant(50), 1);
        assertEquals(Boolean.FALSE, exprNoMatch.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testRightCollectionContainsMatch() {
        List<Double> rightList = Arrays.asList(10.0, 20.0, 30.0);
        // 15 < rightList elements: when right is collection, containsMatch checks elements
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(15), new ObjectConstantExpression(rightList), 3);
        // recursive check compute(rightElement, leftScalar) where compare is element < 15
        // Right element 10 < 15 -> true
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testFindMatchBothIterators() {
        List<Double> leftList = Arrays.asList(10.0, 20.0);
        List<Double> rightList = Arrays.asList(15.0, 5.0);

        // left > right: exists left=10, right=5 such that 10 > 5 -> true
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new ObjectConstantExpression(leftList),
                new ObjectConstantExpression(rightList), 1);
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testFindMatchBothIteratorsNoMatch() {
        List<Double> leftList = Arrays.asList(1.0, 2.0);
        List<Double> rightList = Arrays.asList(100.0, 200.0);

        // left > right: no element in left > any in right -> false
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new ObjectConstantExpression(leftList),
                new ObjectConstantExpression(rightList), 1);
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionOperand() {
        List<Double> emptyList = Collections.emptyList();

        RelationalExpressionHarness exprLeftEmpty = new RelationalExpressionHarness(
                new ObjectConstantExpression(emptyList), new Constant(10), 1);
        assertEquals(Boolean.FALSE, exprLeftEmpty.computeValue(null));

        RelationalExpressionHarness exprRightEmpty = new RelationalExpressionHarness(
                new Constant(10), new ObjectConstantExpression(emptyList), 1);
        assertEquals(Boolean.FALSE, exprRightEmpty.computeValue(null));

        RelationalExpressionHarness exprBothEmpty = new RelationalExpressionHarness(
                new ObjectConstantExpression(emptyList),
                new ObjectConstantExpression(emptyList), 1);
        assertEquals(Boolean.FALSE, exprBothEmpty.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testStringNumericConversion() {
        // String "42" and "100" should convert to double
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant("42"), new Constant("100"), 3);
        assertEquals(Boolean.TRUE, expr.computeValue(null));

        RelationalExpressionHarness exprFalse = new RelationalExpressionHarness(
                new Constant("42"), new Constant("10"), 3);
        assertEquals(Boolean.FALSE, exprFalse.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testInitialContextResetInvoked() {
        MockInitialContext leftCtx = new MockInitialContext(Arrays.<Object>asList(10.0));
        MockInitialContext rightCtx = new MockInitialContext(Arrays.<Object>asList(5.0));

        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new ObjectConstantExpression(leftCtx),
                new ObjectConstantExpression(rightCtx), 1);
        Object result = expr.computeValue(null);

        assertTrue("left InitialContext reset() must be called", leftCtx.resetCalled);
        assertTrue("right InitialContext reset() must be called", rightCtx.resetCalled);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testInfinityBoundaries() {
        RelationalExpressionHarness exprPosInf = new RelationalExpressionHarness(
                new Constant(Double.POSITIVE_INFINITY), new Constant(Double.MAX_VALUE), 1);
        assertEquals(Boolean.TRUE, exprPosInf.computeValue(null));

        RelationalExpressionHarness exprNegInf = new RelationalExpressionHarness(
                new Constant(Double.NEGATIVE_INFINITY), new Constant(-Double.MAX_VALUE), 3);
        assertEquals(Boolean.TRUE, exprNegInf.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth: testNan)
    // =========================================================================

    /**
     * Target Defect: Evaluating <$nan > $nan> or any relational comparison with NaN
     * must strictly evaluate to false per XPath 1.0.
     * Defective code uses (ld == rd ? 0 : ld < rd ? -1 : 1), returning 1 on NaN operands,
     * causing evaluateCompare(1) to incorrectly return TRUE for '>' and '>='.
     */
    @Test(timeout = 4000)
    public void testNanGreaterThanNanMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(Double.NaN), new Constant(Double.NaN), 1); // '>'
        assertEquals("NaN > NaN must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanGreaterThanOrEqualNanMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(Double.NaN), new Constant(Double.NaN), 2); // '>='
        assertEquals("NaN >= NaN must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanGreaterThanNumberMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(Double.NaN), new Constant(0.0), 1); // '>'
        assertEquals("NaN > 0.0 must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNumberGreaterThanNanMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(0.0), new Constant(Double.NaN), 1); // '>'
        assertEquals("0.0 > NaN must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanLessThanNumberMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(Double.NaN), new Constant(0.0), 3); // '<'
        assertEquals("NaN < 0.0 must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanLessThanOrEqualNumberMustBeFalse() {
        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new Constant(Double.NaN), new Constant(0.0), 4); // '<='
        assertEquals("NaN <= 0.0 must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanInCollectionComparisonMustBeFalse() {
        List<Double> nanList = new ArrayList<Double>();
        nanList.add(Double.NaN);

        RelationalExpressionHarness expr = new RelationalExpressionHarness(
                new ObjectConstantExpression(nanList), new Constant(100.0), 1); // '>'
        assertEquals("[NaN] > 100.0 must evaluate to false", Boolean.FALSE, expr.computeValue(null));
    }
}