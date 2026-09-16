/* [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression
 *
 * Decision / Condition Matrix:
 * 1. computeValue(EvalContext):
 *    - Calls compute(args[0].compute(context), args[1].compute(context)) in fixed version,
 *      or args[x].computeValue(context) in defective version.
 * 2. reduce(Object):
 *    - Branch: o instanceof SelfContext -> unwraps to SingleNodePointer
 *    - Branch: o instanceof Collection -> converts to iterator()
 *    - Branch: other object -> returns unaltered
 * 3. compute(Object, Object):
 *    - Branch: left instanceof InitialContext -> invokes reset()
 *    - Branch: right instanceof InitialContext -> invokes reset()
 *    - Branch: left instanceof Iterator && right instanceof Iterator -> calls findMatch()
 *    - Branch: left instanceof Iterator -> calls containsMatch(left, right)
 *    - Branch: right instanceof Iterator -> calls containsMatch(right, left)
 *    - Branch: InfoSetUtil.doubleValue(left) is NaN -> returns false
 *    - Branch: InfoSetUtil.doubleValue(right) is NaN -> returns false
 *    - Branch: compare calculation: ld == rd (0), ld < rd (-1), ld > rd (1)
 * 4. containsMatch(Iterator, Object):
 *    - Iterates over it: compute(element, value) is true -> returns true immediately
 *    - Empty iterator or no elements match -> returns false
 * 5. findMatch(Iterator, Iterator):
 *    - Collects left iterator into HashSet
 *    - Iterates right iterator: containsMatch(left.iterator(), rightElem) -> returns true
 *    - Empty left, empty right, or disjoint sets -> returns false
 * 6. getPrecedence():
 *    - Returns constant 3
 * 7. isSymmetric():
 *    - Returns constant false
 *
 * Defects4J Ground Truth Target:
 * - Empty node-set comparisons (e.g. </idonotexist >= 0> or </idonotexist <= 0>):
 *   XPath 1.0 requires existential semantics over node-sets. Any relational comparison
 *   against an empty node-set must evaluate to false. In the defective implementation,
 *   calling args[0].computeValue(context) evaluates an empty location path to null, which
 *   InfoSetUtil.doubleValue(null) converts to 0.0, causing "0.0 >= 0.0" to evaluate to true.
 */

package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Expressions to simulate controlled return values for compute / computeValue
    // -------------------------------------------------------------------------

    private static class CustomValueExpression extends Constant {
        private final Object customValue;

        public CustomValueExpression(Object value) {
            super("dummy");
            this.customValue = value;
        }

        @Override
        public Object compute(EvalContext context) {
            return customValue;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return customValue;
        }
    }

    private static class NodeSetLikeExpression extends Constant {
        private final Object computeResult;
        private final Object computeValueResult;

        public NodeSetLikeExpression(Object computeResult, Object computeValueResult) {
            super("dummy");
            this.computeResult = computeResult;
            this.computeValueResult = computeValueResult;
        }

        @Override
        public Object compute(EvalContext context) {
            return computeResult;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return computeValueResult;
        }
    }

    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        public TestRelationalExpression(Expression arg1, Expression arg2) {
            super(new Expression[] { arg1, arg2 });
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }

        @Override
        public String getSymbol() {
            return "test>";
        }
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testScalarComparisonsAllOperators() {
        Constant num3 = new Constant(new Double(3.0));
        Constant num5 = new Constant(new Double(5.0));

        // GreaterThan
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(num5, num3).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(num3, num5).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(num5, num5).computeValue(null));

        // GreaterThanOrEqual
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(num5, num3).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(num3, num5).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(num5, num5).computeValue(null));

        // LessThan
        assertEquals(Boolean.TRUE, new CoreOperationLessThan(num3, num5).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(num5, num3).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(num3, num3).computeValue(null));

        // LessThanOrEqual
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(num3, num5).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(num5, num3).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(num3, num3).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testCollectionsReducedToIterators() {
        List<Double> listLeft = Arrays.asList(10.0, 20.0);
        List<Double> listRight = Arrays.asList(15.0, 25.0);

        CustomValueExpression leftExpr = new CustomValueExpression(listLeft);
        CustomValueExpression rightExpr = new CustomValueExpression(listRight);

        // 20.0 > 15.0 -> True
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(leftExpr, rightExpr).computeValue(null));

        // Disjoint sets where left values are strictly smaller
        List<Double> smallList = Arrays.asList(1.0, 2.0);
        List<Double> largeList = Arrays.asList(10.0, 20.0);
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(
                new CustomValueExpression(smallList),
                new CustomValueExpression(largeList)
        ).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testLeftIteratorOnly() {
        List<Double> list = Arrays.asList(5.0, 15.0);
        CustomValueExpression left = new CustomValueExpression(list);
        CustomValueExpression right = new CustomValueExpression(new Double(10.0));

        // 15.0 > 10.0 -> True
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(left, right).computeValue(null));

        // None > 20.0 -> False
        CustomValueExpression highVal = new CustomValueExpression(new Double(20.0));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(left, highVal).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testRightIteratorOnly() {
        CustomValueExpression left = new CustomValueExpression(new Double(10.0));
        List<Double> list = Arrays.asList(5.0, 15.0);
        CustomValueExpression right = new CustomValueExpression(list);

        // Right iterator calls containsMatch(right, left) -> compute(element, left)
        // With right elements [5.0, 15.0] and left 10.0:
        // CoreOperationGreaterThan evaluates element > 10.0 -> 15.0 > 10.0 is true
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(left, right).computeValue(null));

        // With right elements [1.0, 2.0] and left 10.0:
        // element > 10.0 -> none match -> false
        List<Double> smallList = Arrays.asList(1.0, 2.0);
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(left, new CustomValueExpression(smallList)).computeValue(null));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNaNBoundaryConditions() {
        CustomValueExpression nanExpr = new CustomValueExpression(new Double(Double.NaN));
        CustomValueExpression strNan = new CustomValueExpression("not-a-number");
        CustomValueExpression validNum = new CustomValueExpression(new Double(100.0));

        // Left is NaN
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(nanExpr, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(nanExpr, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(strNan, validNum).computeValue(null));

        // Right is NaN
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(validNum, nanExpr).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(validNum, strNan).computeValue(null));

        // Both are NaN
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(nanExpr, nanExpr).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(strNan, nanExpr).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testInfinityAndExtremeBoundaries() {
        CustomValueExpression posInf = new CustomValueExpression(new Double(Double.POSITIVE_INFINITY));
        CustomValueExpression negInf = new CustomValueExpression(new Double(Double.NEGATIVE_INFINITY));
        CustomValueExpression maxVal = new CustomValueExpression(new Double(Double.MAX_VALUE));
        CustomValueExpression minVal = new CustomValueExpression(new Double(-Double.MAX_VALUE));

        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(posInf, maxVal).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThan(negInf, minVal).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(posInf, posInf).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(negInf, negInf).computeValue(null));

        // 0.0 vs -0.0
        CustomValueExpression posZero = new CustomValueExpression(new Double(0.0));
        CustomValueExpression negZero = new CustomValueExpression(new Double(-0.0));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(posZero, negZero).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(posZero, negZero).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(posZero, negZero).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(posZero, negZero).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyIteratorsAndCollections() {
        CustomValueExpression emptyCol = new CustomValueExpression(Collections.emptyList());
        CustomValueExpression emptyIter = new CustomValueExpression(Collections.emptyList().iterator());
        CustomValueExpression num = new CustomValueExpression(new Double(0.0));

        // Both empty
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(emptyCol, emptyCol).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(emptyIter, emptyIter).computeValue(null));

        // Left empty, right scalar
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(emptyCol, num).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(emptyIter, num).computeValue(null));

        // Left scalar, right empty
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(num, emptyCol).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(num, emptyIter).computeValue(null));

        // Left empty, right non-empty iterator
        CustomValueExpression nonEmptyIter = new CustomValueExpression(Collections.singletonList(new Double(1.0)).iterator());
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(emptyIter, nonEmptyIter).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(nonEmptyIter, emptyIter).computeValue(null));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyNodeSetOperationsDefect_UnitAST() {
        // Simulates an empty LocationPath node-set:
        // compute(context) yields an empty Iterator (EvalContext),
        // but buggy computeValue(context) yields null.
        NodeSetLikeExpression emptyNodeSet = new NodeSetLikeExpression(Collections.emptyList().iterator(), null);
        CustomValueExpression zero = new CustomValueExpression(new Double(0.0));

        CoreOperationGreaterThanOrEqual opGte = new CoreOperationGreaterThanOrEqual(emptyNodeSet, zero);
        Object resultGte = opGte.computeValue(null);
        assertEquals("Evaluating empty node-set >= 0 must be false under XPath 1.0 existential rules",
                Boolean.FALSE, resultGte);

        CoreOperationLessThanOrEqual opLte = new CoreOperationLessThanOrEqual(emptyNodeSet, zero);
        Object resultLte = opLte.computeValue(null);
        assertEquals("Evaluating empty node-set <= 0 must be false under XPath 1.0 existential rules",
                Boolean.FALSE, resultLte);
    }

    @Test(timeout = 4000)
    public void testEmptyNodeSetOperationsDefect_XPathEvaluation() {
        JXPathContext context = JXPathContext.newContext(new HashMap<String, Object>());

        // Defect: </idonotexist >= 0> expected:<false> but was:<true>
        assertEquals(Boolean.FALSE, context.getValue("/idonotexist >= 0"));
        assertEquals(Boolean.FALSE, context.getValue("/idonotexist <= 0"));
        assertEquals(Boolean.FALSE, context.getValue("/idonotexist > 0"));
        assertEquals(Boolean.FALSE, context.getValue("/idonotexist < 0"));

        assertEquals(Boolean.FALSE, context.getValue("0 >= /idonotexist"));
        assertEquals(Boolean.FALSE, context.getValue("0 <= /idonotexist"));
        assertEquals(Boolean.FALSE, context.getValue("0 > /idonotexist"));
        assertEquals(Boolean.FALSE, context.getValue("0 < /idonotexist"));
    }

    // -------------------------------------------------------------------------
    // Partition D: Context Handling & Type Reduction Branches
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSelfContextReduction() {
        JXPathContextReferenceImpl context = (JXPathContextReferenceImpl) JXPathContext.newContext("42.0");
        RootContext rootContext = context.getRootContext();
        InitialContext initialContext = new InitialContext(rootContext);
        SelfContext selfContext = new SelfContext(initialContext, new NodeTypeTest(Compiler.NODE_TYPE_NODE));

        CustomValueExpression selfExpr = new CustomValueExpression(selfContext);
        CustomValueExpression numExpr = new CustomValueExpression(new Double(40.0));

        // SelfContext should be reduced to its single node pointer (value "42.0")
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(selfExpr, numExpr);
        Object result = op.computeValue(rootContext);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testInitialContextResetBranches() {
        JXPathContextReferenceImpl context = (JXPathContextReferenceImpl) JXPathContext.newContext(new Double(50.0));
        RootContext rootContext = context.getRootContext();

        InitialContext leftInitContext = new InitialContext(rootContext);
        InitialContext rightInitContext = new InitialContext(rootContext);

        CustomValueExpression left = new CustomValueExpression(leftInitContext);
        CustomValueExpression right = new CustomValueExpression(rightInitContext);

        // Triggers:
        // if (left instanceof InitialContext) ((InitialContext) left).reset();
        // if (right instanceof InitialContext) ((InitialContext) right).reset();
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        Object result = op.computeValue(rootContext);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testFindMatchIteratorsExecution() {
        // Test findMatch where multiple elements exist and match on the second iteration
        List<Double> leftList = Arrays.asList(10.0, 30.0);
        List<Double> rightList = Arrays.asList(40.0, 20.0);

        CustomValueExpression left = new CustomValueExpression(leftList.iterator());
        CustomValueExpression right = new CustomValueExpression(rightList.iterator());

        // 30.0 > 20.0 is true -> findMatch returns true
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        assertEquals(Boolean.TRUE, op.computeValue(null));
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Contract, Precedence & Symmetry Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrecedenceAndSymmetryContracts() {
        Constant c1 = new Constant(new Double(1.0));
        Constant c2 = new Constant(new Double(2.0));

        CoreOperationGreaterThan op = new CoreOperationGreaterThan(c1, c2);
        assertEquals("Relational expression precedence must strictly be 3", 3, op.getPrecedence());
        assertFalse("Relational expression is non-symmetric", op.isSymmetric());

        TestRelationalExpression customOp = new TestRelationalExpression(c1, c2);
        assertEquals(3, customOp.getPrecedence());
        assertFalse(customOp.isSymmetric());
        assertEquals("test>", customOp.getSymbol());
    }

    @Test(timeout = 4000)
    public void testSubclassSymbols() {
        Constant c1 = new Constant(new Double(1.0));
        Constant c2 = new Constant(new Double(2.0));

        assertEquals(">", new CoreOperationGreaterThan(c1, c2).getSymbol());
        assertEquals(">=", new CoreOperationGreaterThanOrEqual(c1, c2).getSymbol());
        assertEquals("<", new CoreOperationLessThan(c1, c2).getSymbol());
        assertEquals("<=", new CoreOperationLessThanOrEqual(c1, c2).getSymbol());
    }
}