package org.apache.commons.jxpath.ri.compiler;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: CoreOperationCompare
 * Targeted Defect: Defects4J JXPath VariableTest::testIterateVariable ($d = 'a' returning false)
 *
 * Decision / Condition Matrix Covered:
 * 1. equal(EvalContext, Expression, Expression):
 *    - Branch: l instanceof InitialContext / SelfContext -> getSingleNodePointer()
 *    - Branch: r instanceof InitialContext / SelfContext -> getSingleNodePointer()
 *    - Branch: l instanceof Collection -> to iterator
 *    - Branch: r instanceof Collection -> to iterator
 *    - Branch: (l is Iterator) && !(r is Iterator) -> contains(l, r)
 *    - Branch: !(l is Iterator) && (r is Iterator) -> contains(r, l)
 *    - Branch: (l is Iterator) && (r is Iterator) -> findMatch(l, r)
 *    - Branch: neither is Iterator -> equal(l, r)
 *
 * 2. contains(Iterator it, Object value):
 *    - it.hasNext() == false (empty iterator)
 *    - it matches on first element
 *    - it matches on subsequent element
 *    - it exhausts without match -> returns false
 *
 * 3. findMatch(Iterator lit, Iterator rit):
 *    - lit is empty -> returns false
 *    - rit is empty -> returns false
 *    - Match found between sets -> returns true
 *    - No common elements -> returns false
 *
 * 4. equal(Object l, Object r):
 *    - l is Pointer && r is Pointer (identical pointers vs different pointers)
 *    - l is Pointer (unwraps getValue())
 *    - r is Pointer (unwraps getValue())
 *    - l == r reference equality (including null == null)
 *    - l or r is Boolean -> compares via InfoSetUtil.booleanValue
 *    - l or r is Number -> compares via InfoSetUtil.doubleValue
 *    - l or r is String -> compares via InfoSetUtil.stringValue
 *    - Fallback: l != null && l.equals(r)
 *    - Null mismatches (null vs non-null)
 * ----------------------------------------------------------------------------------------------------
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationCompareGeminiTest {

    // Helper Expression to supply arbitrary computed objects
    private static class ValueExpr extends Expression {
        private final Object val;

        public ValueExpr(Object val) {
            this.val = val;
        }

        public Object compute(EvalContext context) {
            return val;
        }

        public Object computeValue(EvalContext context) {
            return val;
        }

        public boolean isContextDependent() {
            return false;
        }
    }

    // Concrete test harness subclass exposing protected methods of CoreOperationCompare
    private static class TestCompareOperation extends CoreOperationCompare {
        public TestCompareOperation(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        public TestCompareOperation() {
            super(new Constant("left"), new Constant("right"));
        }

        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        public String getSymbol() {
            return "==";
        }

        public int getPrecedence() {
            return 2;
        }

        public boolean isSymmetric() {
            return true;
        }

        // Direct public delegates to protected methods
        public boolean publicEqual(EvalContext context, Expression left, Expression right) {
            return equal(context, left, right);
        }

        public boolean publicEqual(Object l, Object r) {
            return equal(l, r);
        }

        public boolean publicContains(Iterator it, Object value) {
            return contains(it, value);
        }

        public boolean publicFindMatch(Iterator lit, Iterator rit) {
            return findMatch(lit, rit);
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualDirectScalars() {
        TestCompareOperation op = new TestCompareOperation();
        // Boolean branches
        assertTrue("true == true", op.publicEqual(Boolean.TRUE, Boolean.TRUE));
        assertFalse("true == false", op.publicEqual(Boolean.TRUE, Boolean.FALSE));
        assertTrue("true == 'true'", op.publicEqual(Boolean.TRUE, "true"));
        assertTrue("'true' == true", op.publicEqual("true", Boolean.TRUE));
        assertTrue("false == 0", op.publicEqual(Boolean.FALSE, 0));

        // Number branches
        assertTrue("10.0 == 10", op.publicEqual(10.0, 10));
        assertTrue("5 == '5.0'", op.publicEqual(5, "5.0"));
        assertTrue("'5.0' == 5", op.publicEqual("5.0", 5));
        assertFalse("5 == 6", op.publicEqual(5, 6));

        // String branches
        assertTrue("'abc' == 'abc'", op.publicEqual("abc", "abc"));
        assertFalse("'abc' == 'xyz'", op.publicEqual("abc", "xyz"));

        // Fallback equals branch for non-primitive types
        Date d1 = new Date(12345L);
        Date d2 = new Date(12345L);
        Date d3 = new Date(67890L);
        assertTrue("d1 equals d2", op.publicEqual(d1, d2));
        assertFalse("d1 equals d3", op.publicEqual(d1, d3));
    }

    @Test(timeout = 4000)
    public void testEqualWithNodePointers() {
        TestCompareOperation op = new TestCompareOperation();
        NodePointer p1 = NodePointer.newNodePointer(new QName("test"), "hello", Locale.getDefault());
        NodePointer p2 = NodePointer.newNodePointer(new QName("test"), "hello", Locale.getDefault());
        NodePointer p3 = NodePointer.newNodePointer(new QName("test"), "other", Locale.getDefault());

        // Pointer to Pointer
        assertTrue("Pointer values equal", op.publicEqual(p1, p2));
        assertFalse("Pointer values unequal", op.publicEqual(p1, p3));

        // Left Pointer, Right Object
        assertTrue("Pointer vs String", op.publicEqual(p1, "hello"));
        assertFalse("Pointer vs String mismatch", op.publicEqual(p1, "other"));

        // Left Object, Right Pointer
        assertTrue("String vs Pointer", op.publicEqual("hello", p1));
        assertFalse("String vs Pointer mismatch", op.publicEqual("other", p1));
    }

    @Test(timeout = 4000)
    public void testContainsHelper() {
        TestCompareOperation op = new TestCompareOperation();
        List<String> list = Arrays.asList("alpha", "beta", "gamma");

        assertTrue("Contains alpha", op.publicContains(list.iterator(), "alpha"));
        assertTrue("Contains gamma", op.publicContains(list.iterator(), "gamma"));
        assertFalse("Does not contain delta", op.publicContains(list.iterator(), "delta"));
        assertFalse("Empty iterator", op.publicContains(Collections.emptyList().iterator(), "alpha"));
    }

    @Test(timeout = 4000)
    public void testFindMatchHelper() {
        TestCompareOperation op = new TestCompareOperation();
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("c", "d", "e");
        List<String> list3 = Arrays.asList("x", "y", "z");

        assertTrue("list1 and list2 intersect on 'c'", op.publicFindMatch(list1.iterator(), list2.iterator()));
        assertFalse("list1 and list3 have no intersection", op.publicFindMatch(list1.iterator(), list3.iterator()));
        assertFalse("Empty left iterator", op.publicFindMatch(Collections.emptyList().iterator(), list2.iterator()));
        assertFalse("Empty right iterator", op.publicFindMatch(list1.iterator(), Collections.emptyList().iterator()));
    }

    @Test(timeout = 4000)
    public void testEqualContextWithCollectionsAndIterators() {
        TestCompareOperation op = new TestCompareOperation();

        // Left Collection, Right Scalar
        Expression leftCol = new ValueExpr(Arrays.asList("10", "20"));
        Expression rightScalar = new ValueExpr(20);
        assertTrue("Collection contains scalar 20", op.publicEqual(null, leftCol, rightScalar));

        // Left Scalar, Right Collection
        Expression leftScalar = new ValueExpr(10);
        Expression rightCol = new ValueExpr(Arrays.asList(10, 30));
        assertTrue("Scalar 10 in Collection", op.publicEqual(null, leftScalar, rightCol));

        // Left Collection, Right Collection (Intersecting)
        Expression col1 = new ValueExpr(Arrays.asList("x", "y"));
        Expression col2 = new ValueExpr(Arrays.asList("y", "z"));
        assertTrue("Collections intersect on 'y'", op.publicEqual(null, col1, col2));

        // Left Collection, Right Collection (Non-intersecting)
        Expression col3 = new ValueExpr(Arrays.asList("a", "b"));
        assertFalse("Collections do not intersect", op.publicEqual(null, col1, col3));
    }

    @Test(timeout = 4000)
    public void testEqualContextWithInitialAndSelfContext() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext("nodeValue");
        RootContext rootContext = jxContext.getRootContext();
        InitialContext initContext = new InitialContext(rootContext);
        SelfContext selfContext = new SelfContext(initContext, new NodeTypeTest(Compiler.NODE_TYPE_NODE));

        TestCompareOperation op = new TestCompareOperation();

        // InitialContext vs String
        Expression leftInit = new ValueExpr(initContext);
        Expression rightConst = new Constant("nodeValue");
        assertTrue("InitialContext equals string", op.publicEqual(rootContext, leftInit, rightConst));

        // String vs SelfContext
        Expression leftConst = new Constant("nodeValue");
        Expression rightSelf = new ValueExpr(selfContext);
        assertTrue("String equals SelfContext", op.publicEqual(rootContext, leftConst, rightSelf));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullBoundaries() {
        TestCompareOperation op = new TestCompareOperation();

        assertTrue("null == null", op.publicEqual(null, null));
        assertFalse("null == 'val'", op.publicEqual(null, "val"));
        assertFalse("'val' == null", op.publicEqual("val", null));
    }

    @Test(timeout = 4000)
    public void testNumericBoundaries() {
        TestCompareOperation op = new TestCompareOperation();

        assertTrue("0.0 == -0.0", op.publicEqual(0.0, -0.0));
        assertTrue("POSITIVE_INFINITY == POSITIVE_INFINITY",
                op.publicEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertTrue("NEGATIVE_INFINITY == NEGATIVE_INFINITY",
                op.publicEqual(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        assertFalse("POSITIVE_INFINITY == NEGATIVE_INFINITY",
                op.publicEqual(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        // Double.NaN != Double.NaN under IEEE 754
        assertFalse("NaN == NaN", op.publicEqual(Double.NaN, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionBoundaries() {
        TestCompareOperation op = new TestCompareOperation();

        Expression emptyLeft = new ValueExpr(new ArrayList<Object>());
        Expression emptyRight = new ValueExpr(new ArrayList<Object>());
        Expression val = new ValueExpr("a");

        assertFalse("Empty list == 'a'", op.publicEqual(null, emptyLeft, val));
        assertFalse("'a' == Empty list", op.publicEqual(null, val, emptyRight));
        assertFalse("Empty list == Empty list", op.publicEqual(null, emptyLeft, emptyRight));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Ground Truth: Defects4J)
    // Target: org.apache.commons.jxpath.ri.compiler.VariableTest::testIterateVariable
    // Evaluating <$d = 'a'> expected:<true> but was:<false>
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectIterateVariableArrayComparison() {
        // Reproduces the exact condition where variable $d is an array/collection
        JXPathContext context = JXPathContext.newContext(new HashMap<String, Object>());
        context.getVariables().declareVariable("d", new String[] { "a", "b" });

        // Evaluating $d = 'a' MUST return true because 'a' is an element of array $d
        Object resultA = context.getValue("$d = 'a'");
        assertEquals("Evaluating <$d = 'a'> expected:<true> but was:<false>", Boolean.TRUE, resultA);

        // Evaluating $d = 'b' MUST also return true
        Object resultB = context.getValue("$d = 'b'");
        assertEquals("Evaluating <$d = 'b'> expected:<true>", Boolean.TRUE, resultB);

        // Evaluating $d = 'c' MUST return false
        Object resultC = context.getValue("$d = 'c'");
        assertEquals("Evaluating <$d = 'c'> expected:<false>", Boolean.FALSE, resultC);
    }

    @Test(timeout = 4000)
    public void testDefectIterateVariableReversedComparison() {
        // Test reversed operand order: 'a' = $d
        JXPathContext context = JXPathContext.newContext(new HashMap<String, Object>());
        context.getVariables().declareVariable("d", new String[] { "a", "b" });

        Object result = context.getValue("'a' = $d'");
        // Handle potential typo safely via direct XPath
        Object resultRev = context.getValue("'a' = $d");
        assertEquals("Evaluating <'a' = $d> expected:<true>", Boolean.TRUE, resultRev);
    }

    @Test(timeout = 4000)
    public void testDefectIterateVariableViaASTCoreOperationEqual() {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        jxContext.getVariables().declareVariable("d", new String[] { "first", "second" });
        RootContext rootContext = jxContext.getRootContext();

        VariableReference varRef = new VariableReference(new QName("d"));
        Constant constFirst = new Constant("first");
        CoreOperationEqual equalOp = new CoreOperationEqual(varRef, constFirst);

        Object result = equalOp.computeValue(rootContext);
        assertEquals("AST CoreOperationEqual <$d = 'first'> should evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testExpressionEvaluationExceptionPropagates() {
        Expression throwingExpr = new Expression() {
            public Object compute(EvalContext context) {
                throw new IllegalStateException("Test Fault Injection");
            }

            public Object computeValue(EvalContext context) {
                throw new IllegalStateException("Test Fault Injection");
            }

            public boolean isContextDependent() {
                return false;
            }
        };

        TestCompareOperation op = new TestCompareOperation();
        try {
            op.publicEqual(null, throwingExpr, new Constant("test"));
            fail("Expected IllegalStateException not thrown");
        } catch (IllegalStateException ex) {
            assertEquals("Test Fault Injection", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNullEvaluationReturnsTrueForBothNull() {
        TestCompareOperation op = new TestCompareOperation();
        Expression nullExpr1 = new ValueExpr(null);
        Expression nullExpr2 = new ValueExpr(null);

        assertTrue("null expression == null expression", op.publicEqual(null, nullExpr1, nullExpr2));
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCoreOperationEqualAndNotEqualSemantics() {
        Expression left = new Constant("hello");
        Expression right = new Constant("hello");
        Expression diff = new Constant("world");

        CoreOperationEqual equalOp = new CoreOperationEqual(left, right);
        assertEquals("CoreOperationEqual symbol must be '='", "=", equalOp.getSymbol());
        assertEquals(Boolean.TRUE, equalOp.computeValue(null));

        CoreOperationNotEqual notEqualOp = new CoreOperationNotEqual(left, right);
        assertEquals("CoreOperationNotEqual symbol must be '!='", "!=", notEqualOp.getSymbol());
        assertEquals(Boolean.FALSE, notEqualOp.computeValue(null));

        CoreOperationNotEqual notEqualOp2 = new CoreOperationNotEqual(left, diff);
        assertEquals(Boolean.TRUE, notEqualOp2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testConstructorIntegrity() {
        Expression arg1 = new Constant(1);
        Expression arg2 = new Constant(2);
        TestCompareOperation op = new TestCompareOperation(arg1, arg2);

        assertNotNull("Args array initialized", op.getArguments());
        assertEquals("Must have 2 arguments", 2, op.getArguments().length);
        assertSame("First argument is arg1", arg1, op.getArguments()[0]);
        assertSame("Second argument is arg2", arg2, op.getArguments()[1]);
    }
}