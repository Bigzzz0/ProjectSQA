package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CoreOperationCompare.equal(EvalContext, Expression, Expression)
 * 
 * Decision branches covered:
 * 1. l instanceof InitialContext || l instanceof SelfContext -> true/false
 * 2. r instanceof InitialContext || r instanceof SelfContext -> true/false
 * 3. l instanceof Collection -> true/false
 * 4. r instanceof Collection -> true/false
 * 5. (l instanceof Iterator) && !(r instanceof Iterator) -> true/false
 * 6. !(l instanceof Iterator) && (r instanceof Iterator) -> true/false
 * 7. l instanceof Iterator && r instanceof Iterator -> true/false
 * 8. equal(Object, Object) overloaded dispatch
 * 
 * equal(Object l, Object r) branches:
 * 9. l instanceof Pointer && r instanceof Pointer -> true/false
 * 10. l instanceof Pointer -> true/false
 * 11. r instanceof Pointer -> true/false
 * 12. l == r -> true/false
 * 13. l instanceof Boolean || r instanceof Boolean -> true/false
 * 14. l instanceof Number || r instanceof Number -> true/false
 * 15. l instanceof String || r instanceof String -> true/false
 * 16. final return l != null && l.equals(r)
 * 
 * Defect targeting:
 * - VariableTest.testIterateVariable: Evaluating <$d = 'a'> expected:<true> but was:<false>
 *   This involves comparing a variable value (likely a String) against a literal.
 *   The bug is in the final return statement: when l is null and r is not null,
 *   the method incorrectly returns false even if r is also null? Actually the defect
 *   is that when l is null and r is null, it should return true but the final
 *   return returns false because l != null is false. However the defect report
 *   shows a case where a variable holding a String 'a' is compared to literal 'a'
 *   and returns false. This suggests the bug is in the String comparison branch
 *   or the pointer handling. We need to test with actual JXPathContext variable
 *   evaluation.
 */
public class CoreOperationCompareDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testEqualBasicValues() {
        // Use a concrete subclass for testing
        CoreOperationCompare op = new CoreOperationCompare(null, null) {
            @Override
            public String toString() {
                return "test";
            }
        };

        // Test equal(Object, Object) directly via reflection or subclass
        // Since equal is protected, we need a subclass that exposes it
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Basic string equality
        assertTrue("String equality", testable.exposeEqual("a", "a"));
        assertFalse("String inequality", testable.exposeEqual("a", "b"));
        
        // Number equality
        assertTrue("Number equality", testable.exposeEqual(1, 1.0));
        assertFalse("Number inequality", testable.exposeEqual(1, 2));
        
        // Boolean equality
        assertTrue("Boolean equality", testable.exposeEqual(true, Boolean.TRUE));
        assertFalse("Boolean inequality", testable.exposeEqual(true, false));
        
        // Null handling
        assertTrue("Both null", testable.exposeEqual(null, null));
        assertFalse("Left null only", testable.exposeEqual(null, "a"));
        assertFalse("Right null only", testable.exposeEqual("a", null));
    }

    @Test(timeout = 4000)
    public void testEqualWithPointers() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Create mock pointers
        Pointer p1 = new TestPointer("value1");
        Pointer p2 = new TestPointer("value1");
        Pointer p3 = new TestPointer("value2");
        
        // Same pointer object
        assertTrue("Same pointer", testable.exposeEqual(p1, p1));
        // Different pointers with same value
        assertTrue("Equal pointer values", testable.exposeEqual(p1, p2));
        // Different pointer values
        assertFalse("Different pointer values", testable.exposeEqual(p1, p3));
        
        // Pointer vs non-pointer
        assertTrue("Pointer vs value", testable.exposeEqual(p1, "value1"));
        assertTrue("Value vs pointer", testable.exposeEqual("value1", p1));
        assertFalse("Pointer vs different value", testable.exposeEqual(p1, "value2"));
    }

    @Test(timeout = 4000)
    public void testEqualWithCollectionsAndIterators() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Collection vs single value
        List<String> list = Arrays.asList("a", "b", "c");
        assertTrue("Collection contains value", testable.exposeEqual(list, "b"));
        assertFalse("Collection does not contain value", testable.exposeEqual(list, "z"));
        
        // Two collections with matching elements
        List<String> list2 = Arrays.asList("x", "b", "y");
        assertTrue("Collections have common element", testable.exposeEqual(list, list2));
        
        // Disjoint collections
        List<String> list3 = Arrays.asList("x", "y", "z");
        assertFalse("Disjoint collections", testable.exposeEqual(list, list3));
        
        // Empty collections
        List<String> empty = Collections.emptyList();
        assertFalse("Empty vs non-empty", testable.exposeEqual(empty, list));
        assertFalse("Non-empty vs empty", testable.exposeEqual(list, empty));
        assertFalse("Both empty", testable.exposeEqual(empty, Collections.emptyList()));
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testBoundaryValues() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Numeric boundaries
        assertTrue("Double max", testable.exposeEqual(Double.MAX_VALUE, Double.MAX_VALUE));
        assertTrue("Double min", testable.exposeEqual(Double.MIN_VALUE, Double.MIN_VALUE));
        assertTrue("Integer max", testable.exposeEqual(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertTrue("Integer min", testable.exposeEqual(Integer.MIN_VALUE, Integer.MIN_VALUE));
        assertTrue("Zero", testable.exposeEqual(0, 0.0));
        assertTrue("Negative zero", testable.exposeEqual(-0.0, 0.0));
        
        // String boundaries
        assertTrue("Empty string", testable.exposeEqual("", ""));
        assertFalse("Empty vs non-empty", testable.exposeEqual("", "a"));
        assertTrue("Long string", testable.exposeEqual(
            "a".repeat(1000), "a".repeat(1000)));
        
        // Boolean boundaries
        assertTrue("Boolean true", testable.exposeEqual(true, true));
        assertTrue("Boolean false", testable.exposeEqual(false, false));
        assertFalse("Boolean mixed", testable.exposeEqual(true, false));
        
        // Null handling
        assertTrue("Both null", testable.exposeEqual(null, null));
        assertFalse("Left null", testable.exposeEqual(null, ""));
        assertFalse("Right null", testable.exposeEqual("", null));
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullCollections() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Null vs empty collection
        assertFalse("Null vs empty list", testable.exposeEqual(null, Collections.emptyList()));
        assertFalse("Empty list vs null", testable.exposeEqual(Collections.emptyList(), null));
        
        // Empty iterators
        Iterator<String> emptyIter = Collections.emptyIterator();
        assertFalse("Empty iterator vs value", testable.exposeEqual(emptyIter, "a"));
        assertFalse("Value vs empty iterator", testable.exposeEqual("a", emptyIter));
        
        // Iterator with single element
        Iterator<String> singleIter = Arrays.asList("a").iterator();
        assertTrue("Single element iterator", testable.exposeEqual(singleIter, "a"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the defect from VariableTest.testIterateVariable
     * where evaluating <$d = 'a'> expected:<true> but was:<false>
     * 
     * This test simulates the scenario where a variable holds a String value
     * and is compared to a literal String. The bug causes the comparison
     * to return false incorrectly.
     */
    @Test(timeout = 4000)
    public void testVariableComparisonDefect() {
        // Create a JXPathContext with a variable
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("d", "a");
        
        // Evaluate the expression $d = 'a'
        Object result = context.getValue("$d = 'a'");
        assertTrue("Variable comparison should be true", (Boolean) result);
        
        // Also test with different variable values
        context.getVariables().declareVariable("e", "b");
        result = context.getValue("$e = 'a'");
        assertFalse("Different values should be false", (Boolean) result);
        
        // Test with numeric variable
        context.getVariables().declareVariable("num", 5);
        result = context.getValue("$num = 5");
        assertTrue("Numeric variable comparison", (Boolean) result);
    }

    @Test(timeout = 4000)
    public void testDefectWithDirectEqualCall() {
        // Reproduce the exact failure scenario using the protected method
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Simulate the variable value and literal
        Object variableValue = "a";
        Object literalValue = "a";
        
        // This should be true but might fail due to the bug
        assertTrue("Direct string comparison", 
            testable.exposeEqual(variableValue, literalValue));
        
        // Test with pointers wrapping the values
        Pointer varPointer = new TestPointer(variableValue);
        Pointer litPointer = new TestPointer(literalValue);
        assertTrue("Pointer comparison", 
            testable.exposeEqual(varPointer, litPointer));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testNullArgumentsToEqual() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Both null should return true
        assertTrue("Both null", testable.exposeEqual(null, null));
        
        // One null
        assertFalse("Left null", testable.exposeEqual(null, "a"));
        assertFalse("Right null", testable.exposeEqual("a", null));
        
        // Null in collections
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        assertTrue("Collection with null contains null", 
            testable.exposeEqual(listWithNull, null));
    }

    @Test(timeout = 4000)
    public void testMixedTypeComparisons() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // String vs Number
        assertTrue("String '1' vs number 1", testable.exposeEqual("1", 1));
        assertTrue("Number 1 vs string '1'", testable.exposeEqual(1, "1"));
        
        // Boolean vs String
        assertTrue("Boolean true vs string 'true'", 
            testable.exposeEqual(true, "true"));
        assertTrue("String 'false' vs boolean false", 
            testable.exposeEqual("false", false));
        
        // Number vs Boolean
        assertTrue("Number 1 vs boolean true", testable.exposeEqual(1, true));
        assertTrue("Number 0 vs boolean false", testable.exposeEqual(0, false));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testContainsMethod() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        List<String> list = Arrays.asList("a", "b", "c");
        assertTrue("Contains existing", testable.exposeContains(list.iterator(), "b"));
        assertFalse("Does not contain", testable.exposeContains(list.iterator(), "z"));
        
        // Empty iterator
        assertFalse("Empty iterator", 
            testable.exposeContains(Collections.emptyIterator(), "a"));
        
        // Null value in iterator
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        assertTrue("Contains null", testable.exposeContains(listWithNull.iterator(), null));
    }

    @Test(timeout = 4000)
    public void testFindMatchMethod() {
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        List<String> left = Arrays.asList("a", "b", "c");
        List<String> right = Arrays.asList("x", "b", "y");
        assertTrue("Common element", testable.exposeFindMatch(left.iterator(), right.iterator()));
        
        List<String> disjoint = Arrays.asList("x", "y", "z");
        assertFalse("Disjoint iterators", 
            testable.exposeFindMatch(left.iterator(), disjoint.iterator()));
        
        // Empty iterators
        assertFalse("Both empty", 
            testable.exposeFindMatch(Collections.emptyIterator(), Collections.emptyIterator()));
        assertFalse("Left empty", 
            testable.exposeFindMatch(Collections.emptyIterator(), right.iterator()));
        assertFalse("Right empty", 
            testable.exposeFindMatch(left.iterator(), Collections.emptyIterator()));
    }

    @Test(timeout = 4000)
    public void testEvalContextHandling() {
        // Test the full equal(EvalContext, Expression, Expression) method
        TestableCoreOperationCompare testable = new TestableCoreOperationCompare();
        
        // Create mock EvalContext and Expressions
        EvalContext mockContext = new MockEvalContext();
        Expression leftExpr = new Constant("a");
        Expression rightExpr = new Constant("a");
        
        // This tests the full method with InitialContext/SelfContext handling
        assertTrue("Equal expressions", 
            testable.exposeEqual(mockContext, leftExpr, rightExpr));
        
        // Test with different values
        Expression leftDiff = new Constant("a");
        Expression rightDiff = new Constant("b");
        assertFalse("Different expressions", 
            testable.exposeEqual(mockContext, leftDiff, rightDiff));
    }

    // ========== Helper Classes ==========

    /**
     * Testable subclass that exposes protected methods
     */
    private static class TestableCoreOperationCompare extends CoreOperationCompare {
        
        TestableCoreOperationCompare() {
            super(new Constant(""), new Constant(""));
        }
        
        boolean exposeEqual(Object l, Object r) {
            return equal(l, r);
        }
        
        boolean exposeEqual(EvalContext context, Expression left, Expression right) {
            return equal(context, left, right);
        }
        
        boolean exposeContains(Iterator it, Object value) {
            return contains(it, value);
        }
        
        boolean exposeFindMatch(Iterator lit, Iterator rit) {
            return findMatch(lit, rit);
        }
        
        @Override
        public String toString() {
            return "TestableCoreOperationCompare";
        }
    }

    /**
     * Simple Pointer implementation for testing
     */
    private static class TestPointer implements Pointer {
        private final Object value;
        
        TestPointer(Object value) {
            this.value = value;
        }
        
        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public void setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getNode() {
            return value;
        }

        @Override
        public Object getRootNode() {
            return value;
        }

        @Override
        public String asPath() {
            return String.valueOf(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestPointer)) return false;
            TestPointer other = (TestPointer) obj;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }

    /**
     * Mock EvalContext for testing
     */
    private static class MockEvalContext extends EvalContext {
        
        MockEvalContext() {
            super(null);
        }

        @Override
        public Pointer getSingleNodePointer() {
            return new TestPointer("mock");
        }

        @Override
        public boolean nextNode() {
            return false;
        }

        @Override
        public boolean nextSet() {
            return false;
        }

        @Override
        public Pointer getCurrentNodePointer() {
            return new TestPointer("mock");
        }

        @Override
        public Object getValue() {
            return "mock";
        }

        @Override
        public int getCurrentPosition() {
            return 1;
        }

        @Override
        public boolean setPosition(int position) {
            return position == 1;
        }
    }

    /**
     * Simple Expression implementation for testing
     */
    private static class Constant extends Expression {
        private final Object value;
        
        Constant(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public boolean isContextDependent() {
            return false;
        }
    }
}