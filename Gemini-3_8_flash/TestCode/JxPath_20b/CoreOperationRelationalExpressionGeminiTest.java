/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression
 * 
 * Targeted Decision Branches & Boundary Conditions:
 * 1. computeValue(EvalContext):
 *    - Left & right evaluation via args[0].compute() and args[1].compute().
 *    - Boolean.TRUE vs Boolean.FALSE mapping.
 * 2. Precedence & Symmetry:
 *    - getPrecedence() returns RELATIONAL_EXPR_PRECEDENCE (3).
 *    - isSymmetric() returns false.
 * 3. compute(Object left, Object right):
 *    - reduce(o): SelfContext -> getSingleNodePointer(), Collection -> iterator(), other -> unmodified.
 *    - InitialContext handling: reset() called on left and/or right InitialContext.
 *    - Iterator combinations:
 *        a) left instanceof Iterator && right instanceof Iterator -> findMatch(lit, rit)
 *        b) left instanceof Iterator && !(right instanceof Iterator) -> containsMatch(left, right)
 *        c) !(left instanceof Iterator) && right instanceof Iterator -> defect zone: reversed operand comparison!
 *        d) neither is Iterator -> direct InfoSetUtil.doubleValue comparison.
 *    - NaN handling: Double.isNaN(ld) -> false; Double.isNaN(rd) -> false.
 *    - evaluateCompare(int) delegation: ld == rd (0), ld < rd (-1), ld > rd (1).
 * 4. containsMatch(Iterator it, Object value):
 *    - Empty iterator -> false.
 *    - First element matches -> returns true early.
 *    - Later element matches -> returns true.
 *    - No element matches -> false.
 * 5. findMatch(Iterator lit, Iterator rit):
 *    - Empty left iterator -> false.
 *    - Empty right iterator -> false.
 *    - Overlapping vs disjoint sets of iterator values.
 *
 * Known Defect (JXPath-149 / Defects4J):
 * - In compute(left, right), when right is an Iterator and left is a scalar (e.g., $a + $b <= $c,
 *   where left is Double and right is an InitialContext/Iterator), calling
 *   containsMatch((Iterator) right, left) erroneously inverts the comparison to `element op left`
 *   instead of `left op element`. Relational expressions are non-symmetric, causing false negatives.
 */
package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionGeminiTest {

    // Concrete test subclass to inspect evaluateCompare and drive unit tests directly
    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final int expectedComparison;

        // Constructor accepting expected comparison code (-1, 0, 1) or specific behavior
        public TestRelationalExpression(Expression left, Expression right, int expectedComparison) {
            super(new Expression[]{left, right});
            this.expectedComparison = expectedComparison;
        }

        public TestRelationalExpression(Expression[] args) {
            super(args);
            this.expectedComparison = 0;
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == expectedComparison;
        }

        @Override
        public String getSymbol() {
            return "~";
        }
    }

    // Concrete LessThanOrEqual expression matching JXPath standard implementation
    private static class ConcreteLessThanOrEqual extends CoreOperationRelationalExpression {
        public ConcreteLessThanOrEqual(Expression left, Expression right) {
            super(new Expression[]{left, right});
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }

        @Override
        public String getSymbol() {
            return "<=";
        }
    }

    // Concrete GreaterThan expression matching JXPath standard implementation
    private static class ConcreteGreaterThan extends CoreOperationRelationalExpression {
        public ConcreteGreaterThan(Expression left, Expression right) {
            super(new Expression[]{left, right});
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }

        @Override
        public String getSymbol() {
            return ">";
        }
    }

    // Constant expression stub returning an arbitrary raw object on compute()
    private static class ObjectExpr extends Expression {
        private final Object value;

        public ObjectExpr(Object value) {
            this.value = value;
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        protected boolean isContextDependent() {
            return false;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrecedenceAndSymmetry() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(1), new Constant(2), -1
        );
        assertEquals("Precedence must match RELATIONAL_EXPR_PRECEDENCE", 3, expr.getPrecedence());
        assertFalse("Relational expressions must not be symmetric", expr.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testDirectScalarEvaluationEqual() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(10), new Constant(10), 0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDirectScalarEvaluationLessThan() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(5), new Constant(10), -1
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDirectScalarEvaluationGreaterThan() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(20), new Constant(10), 1
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDirectScalarEvaluationMismatchReturnsFalse() {
        // Evaluate compare expects 0 (equal), but 5 < 10 (compare is -1)
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(5), new Constant(10), 0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLeftOperandNaNReturnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant("not-a-number"), new Constant(10), 0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testRightOperandNaNReturnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(10), new Constant("invalid-num"), 0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testBothOperandsNaNReturnsFalse() {
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant("abc"), new Constant("xyz"), 0
        );
        Object result = expr.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testExtremeDoubleValues() {
        ConcreteLessThanOrEqual expr = new ConcreteLessThanOrEqual(
                new Constant(Double.NEGATIVE_INFINITY),
                new Constant(Double.MAX_VALUE)
        );
        assertEquals(Boolean.TRUE, expr.computeValue(null));

        ConcreteLessThanOrEqual expr2 = new ConcreteLessThanOrEqual(
                new Constant(Double.MAX_VALUE),
                new Constant(Double.POSITIVE_INFINITY)
        );
        assertEquals(Boolean.TRUE, expr2.computeValue(null));

        ConcreteLessThanOrEqual expr3 = new ConcreteLessThanOrEqual(
                new Constant(0.0),
                new Constant(-0.0)
        );
        // 0.0 == -0.0 in double comparison
        assertEquals(Boolean.TRUE, expr3.computeValue(null));
    }

    // =========================================================================
    // Partition C: Collection & Iterator Interactions (containsMatch & findMatch)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLeftCollectionContainsMatch() {
        List<Double> list = Arrays.asList(100.0, 50.0, 25.0);
        // list has an element 50.0 == 50.0
        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(list), new Constant(50), 0
        );
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testLeftCollectionNoMatch() {
        List<Double> list = Arrays.asList(100.0, 200.0);
        // No element in list equals 50.0
        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(list), new Constant(50), 0
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyLeftIteratorReturnsFalse() {
        List<Double> emptyList = Collections.emptyList();
        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(emptyList.iterator()), new Constant(50), 0
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyRightIteratorReturnsFalse() {
        List<Double> emptyList = Collections.emptyList();
        TestRelationalExpression expr = new TestRelationalExpression(
                new Constant(50), new ObjectExpr(emptyList.iterator()), 0
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBothIteratorsIntersectionMatch() {
        List<Double> leftList = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> rightList = Arrays.asList(3.0, 4.0, 5.0);
        // Match exists: 3.0 == 3.0
        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(leftList.iterator()), new ObjectExpr(rightList.iterator()), 0
        );
        assertEquals(Boolean.TRUE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBothIteratorsNoMatch() {
        List<Double> leftList = Arrays.asList(1.0, 2.0);
        List<Double> rightList = Arrays.asList(3.0, 4.0);
        // No match for equals (0)
        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(leftList.iterator()), new ObjectExpr(rightList.iterator()), 0
        );
        assertEquals(Boolean.FALSE, expr.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testInitialContextOperandsReset() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(null, new NullPointer(null, null));
        InitialContext initContext1 = new InitialContext(rootContext);
        InitialContext initContext2 = new InitialContext(rootContext);

        TestRelationalExpression expr = new TestRelationalExpression(
                new ObjectExpr(initContext1), new ObjectExpr(initContext2), 0
        );
        // Context position is tested and reset during compute
        assertNotNull(expr.computeValue(rootContext));
    }

    // =========================================================================
    // Partition D: Defect-Targeted Branch Zone (JXPath-149 / Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT TEST:
     * When comparing a non-iterator left operand with an iterator right operand
     * (such as a variable or node-set on the right-hand side), the method
     * containsMatch((Iterator) right, left) executes compute(element, value),
     * which reverses the order of operands in the relational comparison:
     * evaluating (right_element op left) instead of (left op right_element).
     *
     * In this test:
     * Left operand = 2 (scalar Double from $a + $b)
     * Right operand = 3 (from variable $c in an InitialContext/Iterator)
     * Operation = <=
     * Expected: 2 <= 3 is TRUE.
     * Buggy behavior: Evaluates 3 <= 2 which is FALSE!
     */
    @Test(timeout = 4000)
    public void testDefectComplexOperationWithVariablesRightIterator() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("a", 1);
        context.getVariables().declareVariable("b", 1);
        context.getVariables().declareVariable("c", 3);

        // $a + $b evaluates to 2 (scalar), $c evaluates to InitialContext containing [3]
        Object result = context.getValue("$a + $b <= $c");
        assertEquals("Evaluating <$a + $b <= $c> where $a=1, $b=1, $c=3 must be true",
                Boolean.TRUE, result);
    }

    /**
     * Complementary defect check:
     * Left = 1, Right = Iterator([2])
     * Expression: left < right
     * Expected: 1 < 2 is TRUE.
     * Buggy behavior: Evaluates 2 < 1 which is FALSE!
     */
    @Test(timeout = 4000)
    public void testRightIteratorScalarLeftLessThan() {
        List<Double> rightList = Collections.singletonList(2.0);
        ConcreteLessThanOrEqual expr = new ConcreteLessThanOrEqual(
                new Constant(1.0),
                new ObjectExpr(rightList.iterator())
        );
        assertEquals("Scalar 1.0 <= Iterator([2.0]) must evaluate to true",
                Boolean.TRUE, expr.computeValue(null));
    }

    /**
     * Complementary defect check with GreaterThan:
     * Left = 5, Right = Iterator([2])
     * Expression: left > right
     * Expected: 5 > 2 is TRUE.
     * Buggy behavior: Evaluates 2 > 5 which is FALSE!
     */
    @Test(timeout = 4000)
    public void testRightIteratorScalarLeftGreaterThan() {
        List<Double> rightList = Collections.singletonList(2.0);
        ConcreteGreaterThan expr = new ConcreteGreaterThan(
                new Constant(5.0),
                new ObjectExpr(rightList.iterator())
        );
        assertEquals("Scalar 5.0 > Iterator([2.0]) must evaluate to true",
                Boolean.TRUE, expr.computeValue(null));
    }

    /**
     * Collection on right side via JXPathContext variable evaluation:
     * Scalar 10 < $list where $list = [5, 20]
     * 10 < 20 is true, so the overall comparison must be true.
     */
    @Test(timeout = 4000)
    public void testVariableListOnRightRelational() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("list", Arrays.asList(5, 20));

        Object result = context.getValue("10 < $list");
        assertEquals("Evaluating 10 < [5, 20] must be true since 10 < 20",
                Boolean.TRUE, result);
    }

    // =========================================================================
    // Partition E: Defensive Guards & Expression Structure
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndArgStorage() {
        Expression left = new Constant(1);
        Expression right = new Constant(2);
        TestRelationalExpression expr = new TestRelationalExpression(new Expression[]{left, right});

        assertNotNull("Arguments array must be initialized", expr.args);
        assertEquals(2, expr.args.length);
        assertSame(left, expr.args[0]);
        assertSame(right, expr.args[1]);
    }
}