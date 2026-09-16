package org.apache.commons.jxpath.ri.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: CoreOperationCompare
 *
 * Decision / Condition Matrix:
 * 1. getPrecedence(): Returns 2.
 * 2. isSymmetric(): Returns true.
 * 3. equal(EvalContext, Expression, Expression):
 *    - Context unwrap:
 *      * l instanceof InitialContext -> calls reset()
 *      * l instanceof SelfContext    -> l = ((EvalContext) l).getSingleNodePointer()
 *      * r instanceof InitialContext -> calls reset()
 *      * r instanceof SelfContext    -> r = ((EvalContext) r).getSingleNodePointer()
 *    - Collection unwrap:
 *      * l instanceof Collection -> converted to Iterator
 *      * r instanceof Collection -> converted to Iterator
 *    - Iterator dispatch:
 *      * l instanceof Iterator && r instanceof Iterator -> findMatch(lit, rit)
 *      * l instanceof Iterator only                     -> contains(lit, r)
 *      * r instanceof Iterator only                     -> contains(rit, l)
 *      * Neither Iterator                               -> equal(l, r)
 * 4. contains(Iterator, Object):
 *    - Non-empty iterator with equal element -> returns true
 *    - Non-empty iterator without equal element -> returns false
 *    - Empty iterator -> returns false
 * 5. findMatch(Iterator, Iterator):
 *    - Both iterators share at least one element -> returns true
 *    - Iterators share no common elements -> returns false
 *    - Lit empty, Rit non-empty / Lit non-empty, Rit empty -> returns false
 * 6. equal(Object, Object):
 *    - Pointer handling:
 *      * l instanceof Pointer && r instanceof Pointer: l.equals(r) -> true
 *      * l instanceof Pointer && r instanceof Pointer: !l.equals(r) -> both unwrapped via getValue()
 *      * l instanceof Pointer only -> unwrapped via getValue()
 *      * r instanceof Pointer only -> unwrapped via getValue()
 *    - Reference identity (l == r):
 *      * null == null -> returns true
 *      * [DEFECT-TARGET]: l == r when l and r are Double.NaN (e.g. $nan = $nan).
 *        In XPath / IEEE 754, NaN comparison must evaluate to false.
 *        The defective code evaluates `if (l == r) return true;` before checking for NaN/Number.
 *    - Boolean comparison:
 *      * l or r is Boolean -> compares InfoSetUtil.booleanValue()
 *    - Number comparison:
 *      * l or r is Number -> compares InfoSetUtil.doubleValue()
 *    - String comparison:
 *      * l or r is String -> compares InfoSetUtil.stringValue().equals(...)
 *    - Fallback:
 *      * l != null && l.equals(r)
 */
public class CoreOperationCompareGeminiTest {

    // --- Test Doubles & Helper Classes ---

    static class TestCompareOperation extends CoreOperationCompare {
        TestCompareOperation(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        @Override
        public Object computeValue(EvalContext context) {
            return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public String getSymbol() {
            return "==";
        }
    }

    static class CustomExpression extends Constant {
        private final Object computed;

        CustomExpression(Object computed) {
            super("dummy");
            this.computed = computed;
        }

        @Override
        public Object compute(EvalContext context) {
            return computed;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return computed;
        }
    }

    static class TestPointer implements Pointer {
        private static final long serialVersionUID = 1L;
        private final Object value;
        private final String path;

        TestPointer(Object value) {
            this(value, "/test");
        }

        TestPointer(Object value, String path) {
            this.value = value;
            this.path = path;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getNode() {
            return value;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public Object getRootNode() {
            return value;
        }

        @Override
        public String asPath() {
            return path;
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TestPointer)) {
                return false;
            }
            TestPointer other = (TestPointer) o;
            return path == null ? other.path == null : path.equals(other.path);
        }

        @Override
        public int hashCode() {
            return path == null ? 0 : path.hashCode();
        }
    }

    static class DummyInitialContext extends InitialContext {
        boolean resetCalled = false;

        DummyInitialContext() {
            super(null);
        }

        @Override
        public void reset() {
            resetCalled = true;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    }

    static class DummySelfContext extends SelfContext {
        private final Pointer pointer;

        DummySelfContext(Pointer pointer) {
            super(null, null);
            this.pointer = pointer;
        }

        @Override
        public Pointer getSingleNodePointer() {
            return pointer;
        }
    }

    private TestCompareOperation createOp(Object left, Object right) {
        return new TestCompareOperation(new CustomExpression(left), new CustomExpression(right));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrecedenceAndSymmetry() {
        TestCompareOperation op = createOp("a", "b");
        assertEquals("Precedence of CoreOperationCompare must be 2", 2, op.getPrecedence());
        assertTrue("CoreOperationCompare must be symmetric", op.isSymmetric());
        assertEquals("Symbol must match custom implementation", "==", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testEqualBooleans() {
        TestCompareOperation op = createOp(Boolean.TRUE, Boolean.TRUE);
        assertTrue("true == true should be true", op.equal(Boolean.TRUE, Boolean.TRUE));
        assertTrue("false == false should be true", op.equal(Boolean.FALSE, Boolean.FALSE));
        assertFalse("true == false should be false", op.equal(Boolean.TRUE, Boolean.FALSE));
        assertFalse("false == true should be false", op.equal(Boolean.FALSE, Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testEqualNumbers() {
        TestCompareOperation op = createOp(10, 10.0);
        assertTrue("10 == 10.0 should be true", op.equal(10, 10.0));
        assertTrue("-5.5 == -5.5 should be true", op.equal(-5.5, -5.5));
        assertFalse("10 == 20 should be false", op.equal(10, 20));
        assertTrue("0.0 == -0.0 should be true", op.equal(0.0, -0.0));
    }

    @Test(timeout = 4000)
    public void testEqualStrings() {
        TestCompareOperation op = createOp("hello", "hello");
        assertTrue("'hello' == 'hello' should be true", op.equal("hello", "hello"));
        assertFalse("'hello' == 'world' should be false", op.equal("hello", "world"));
        assertTrue("Empty strings should be equal", op.equal("", ""));
    }

    @Test(timeout = 4000)
    public void testEqualMixedBooleanNumber() {
        TestCompareOperation op = createOp(Boolean.TRUE, 1);
        // In XPath: InfoSetUtil.booleanValue(1) is true; InfoSetUtil.booleanValue(0) is false
        assertTrue("true == 1 should be true", op.equal(Boolean.TRUE, 1));
        assertTrue("true == 100 should be true", op.equal(Boolean.TRUE, 100));
        assertFalse("true == 0 should be false", op.equal(Boolean.TRUE, 0));
        assertTrue("false == 0 should be true", op.equal(Boolean.FALSE, 0));
        assertTrue("1 == true should be true (symmetric)", op.equal(1, Boolean.TRUE));
        assertFalse("0 == true should be false (symmetric)", op.equal(0, Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testEqualMixedBooleanString() {
        TestCompareOperation op = createOp(Boolean.TRUE, "hello");
        // InfoSetUtil.booleanValue(non-empty string) is true; booleanValue("") is false
        assertTrue("true == 'hello' should be true", op.equal(Boolean.TRUE, "hello"));
        assertTrue("false == '' should be true", op.equal(Boolean.FALSE, ""));
        assertFalse("true == '' should be false", op.equal(Boolean.TRUE, ""));
        assertTrue("'hello' == true should be true", op.equal("hello", Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testEqualMixedNumberString() {
        TestCompareOperation op = createOp(42.0, "42");
        assertTrue("42.0 == '42' should be true", op.equal(42.0, "42"));
        assertTrue("42.5 == '42.5' should be true", op.equal(42.5, "42.5"));
        assertFalse("42.0 == '99' should be false", op.equal(42.0, "99"));
        assertFalse("42.0 == 'invalid_number' should be false", op.equal(42.0, "invalid_number"));
        assertTrue("'123.45' == 123.45 should be true", op.equal("123.45", 123.45));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullComparisons() {
        TestCompareOperation op = createOp(null, null);
        assertTrue("null == null should be true via identity", op.equal((Object) null, (Object) null));

        Object dummy = new Object();
        assertFalse("null == Object should be false", op.equal(null, dummy));
        assertFalse("Object == null should be false", op.equal(dummy, null));
    }

    @Test(timeout = 4000)
    public void testInfinities() {
        TestCompareOperation op = createOp(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertTrue("+Inf == +Inf should be true", op.equal(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertTrue("-Inf == -Inf should be true", op.equal(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        assertFalse("+Inf == -Inf should be false", op.equal(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    @Test(timeout = 4000)
    public void testCollectionsAndIterators() {
        List<String> list1 = Arrays.asList("apple", "banana", "cherry");
        List<String> list2 = Arrays.asList("banana", "date");
        List<String> list3 = Arrays.asList("fig", "grape");

        TestCompareOperation opMatch = createOp(list1, list2);
        assertTrue("Collections sharing an element should match", (Boolean) opMatch.computeValue(null));

        TestCompareOperation opNoMatch = createOp(list1, list3);
        assertFalse("Collections with no common elements should not match", (Boolean) opNoMatch.computeValue(null));

        TestCompareOperation opColValMatch = createOp(list1, "apple");
        assertTrue("Collection containing target should match", (Boolean) opColValMatch.computeValue(null));

        TestCompareOperation opColValNoMatch = createOp(list1, "mango");
        assertFalse("Collection missing target should not match", (Boolean) opColValNoMatch.computeValue(null));

        TestCompareOperation opValColMatch = createOp("cherry", list1);
        assertTrue("Target contained in collection should match (symmetric)", (Boolean) opValColMatch.computeValue(null));

        TestCompareOperation opValColNoMatch = createOp("mango", list1);
        assertFalse("Target not in collection should not match", (Boolean) opValColNoMatch.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionsAndIterators() {
        List<String> empty1 = Collections.emptyList();
        List<String> empty2 = Collections.emptyList();

        TestCompareOperation opEmptyBoth = createOp(empty1, empty2);
        assertFalse("Empty collections should not match", (Boolean) opEmptyBoth.computeValue(null));

        TestCompareOperation opEmptyOne = createOp(empty1, "test");
        assertFalse("Empty collection vs value should not match", (Boolean) opEmptyOne.computeValue(null));

        TestCompareOperation opOneEmpty = createOp("test", empty1);
        assertFalse("Value vs empty collection should not match", (Boolean) opOneEmpty.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testContainsAndFindMatchDirectly() {
        TestCompareOperation op = createOp(null, null);

        List<Integer> numbers = Arrays.asList(1, 2, 3);
        assertTrue("contains should return true when element is present", op.contains(numbers.iterator(), 2));
        assertFalse("contains should return false when element is missing", op.contains(numbers.iterator(), 99));
        assertFalse("contains on empty iterator should return false", op.contains(Collections.emptyList().iterator(), 1));

        List<Integer> other = Arrays.asList(3, 4, 5);
        assertTrue("findMatch should return true for overlapping iterators",
            op.findMatch(numbers.iterator(), other.iterator()));

        List<Integer> disjoint = Arrays.asList(7, 8, 9);
        assertFalse("findMatch should return false for disjoint iterators",
            op.findMatch(numbers.iterator(), disjoint.iterator()));
        assertFalse("findMatch with empty left iterator should return false",
            op.findMatch(Collections.emptyList().iterator(), other.iterator()));
        assertFalse("findMatch with empty right iterator should return false",
            op.findMatch(numbers.iterator(), Collections.emptyList().iterator()));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CRITICAL DEFECT DETECTION)
    // =========================================================================

    /**
     * Defects4J Target Defect: CoreOperationTest::testNan
     * Evaluating <$nan = $nan> expected:<false> but was:<true>
     *
     * In XPath, comparing NaN to NaN (or anything else) MUST return false.
     * When left and right evaluate to the exact same Double.NaN reference
     * (e.g. from variable $nan), `if (l == r)` in CoreOperationCompare erroneously
     * returned true before checking if either side is NaN.
     */
    @Test(timeout = 4000)
    public void testNanEquality_DefectTarget() {
        Double nanInstance = new Double(Double.NaN);

        TestCompareOperation op = createOp(nanInstance, nanInstance);

        // XPath specification: NaN = NaN is false, even if both refer to the exact same instance!
        assertFalse("NaN compared to NaN (same instance reference) must return false per XPath spec",
            op.equal(nanInstance, nanInstance));

        // Assert through computeValue pipeline:
        assertFalse("Expression evaluating <$nan = $nan> must yield false",
            (Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNanDistinctInstancesAndComparisons() {
        Double nan1 = new Double(Double.NaN);
        Double nan2 = new Double(Double.NaN);

        TestCompareOperation op = createOp(nan1, nan2);
        assertFalse("Distinct NaN instances must not be equal", op.equal(nan1, nan2));
        assertFalse("NaN compared to 0 must be false", op.equal(nan1, 0.0));
        assertFalse("0 compared to NaN must be false", op.equal(0.0, nan1));
        assertFalse("NaN compared to 'NaN' string must be false", op.equal(nan1, "NaN"));
    }

    // =========================================================================
    // Partition D: Context & Pointer Unwrapping Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialContextReset() {
        DummyInitialContext initCtxLeft = new DummyInitialContext();
        DummyInitialContext initCtxRight = new DummyInitialContext();

        TestCompareOperation op = createOp(initCtxLeft, "dummy");
        op.equal(null, new CustomExpression(initCtxLeft), new CustomExpression("dummy"));
        assertTrue("reset() must be invoked on left InitialContext", initCtxLeft.resetCalled);

        TestCompareOperation opRight = createOp("dummy", initCtxRight);
        opRight.equal(null, new CustomExpression("dummy"), new CustomExpression(initCtxRight));
        assertTrue("reset() must be invoked on right InitialContext", initCtxRight.resetCalled);
    }

    @Test(timeout = 4000)
    public void testSelfContextUnwrap() {
        TestPointer ptrLeft = new TestPointer("valueA", "/path/a");
        DummySelfContext selfCtxLeft = new DummySelfContext(ptrLeft);

        TestCompareOperation op = createOp(selfCtxLeft, "valueA");
        assertTrue("SelfContext on left must unwrap single node pointer and match value",
            (Boolean) op.computeValue(null));

        TestPointer ptrRight = new TestPointer("valueB", "/path/b");
        DummySelfContext selfCtxRight = new DummySelfContext(ptrRight);

        TestCompareOperation opRight = createOp("valueB", selfCtxRight);
        assertTrue("SelfContext on right must unwrap single node pointer and match value",
            (Boolean) opRight.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testPointerBranches() {
        TestCompareOperation op = createOp(null, null);

        // Case 1: Both are Pointers, and equals() returns true directly
        TestPointer p1 = new TestPointer("val1", "/shared/path");
        TestPointer p2 = new TestPointer("val1", "/shared/path");
        assertTrue("Two pointers with matching path/equals should return true immediately", op.equal(p1, p2));

        // Case 2: Both are Pointers, equals() returns false, but unwrap getValue() matches
        TestPointer p3 = new TestPointer("commonVal", "/path/one");
        TestPointer p4 = new TestPointer("commonVal", "/path/two");
        assertTrue("Two pointers with different paths but equal values should match after unwrap", op.equal(p3, p4));

        // Case 3: Both are Pointers, neither equals() nor values match
        TestPointer p5 = new TestPointer("valX", "/path/x");
        TestPointer p6 = new TestPointer("valY", "/path/y");
        assertFalse("Two pointers with different paths and different values should not match", op.equal(p5, p6));

        // Case 4: Left is Pointer, Right is raw value
        TestPointer pLeft = new TestPointer("unwrapped", "/path/left");
        assertTrue("Pointer on left should unwrap to its value", op.equal(pLeft, "unwrapped"));
        assertFalse("Pointer on left unwrapped should not match differing value", op.equal(pLeft, "other"));

        // Case 5: Right is Pointer, Left is raw value
        TestPointer pRight = new TestPointer(1234, "/path/right");
        assertTrue("Pointer on right should unwrap to its value", op.equal(1234, pRight));
        assertFalse("Pointer on right unwrapped should not match differing value", op.equal(5678, pRight));
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Fallback & Cross-Type Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectEqualsFallback() {
        TestCompareOperation op = createOp(null, null);

        // Equal custom objects that are neither Boolean, Number, nor String
        List<String> objA = Arrays.asList("a", "b");
        List<String> objB = Arrays.asList("a", "b");
        assertTrue("Equal custom objects should return true via equals()", op.equal(objA, objB));

        List<String> objC = Collections.singletonList("different");
        assertFalse("Unequal custom objects should return false", op.equal(objA, objC));
    }

    @Test(timeout = 4000)
    public void testStringAndCustomObject() {
        TestCompareOperation op = createOp(null, null);

        Object customObj = new Object() {
            @Override
            public String toString() {
                return "custom_repr";
            }
        };

        // InfoSetUtil.stringValue(customObj) produces "custom_repr"
        assertTrue("String matching custom object's toString should return true",
            op.equal("custom_repr", customObj));
        assertTrue("Custom object matching String should return true (symmetric)",
            op.equal(customObj, "custom_repr"));
        assertFalse("String not matching custom object's toString should return false",
            op.equal("unrelated", customObj));
    }

    @Test(timeout = 4000)
    public void testMixedTypeCollectionComparison() {
        // Left has Strings "10", "20"; Right has Numbers 20, 30 -> "20" == 20 should match!
        List<String> strList = Arrays.asList("10", "20");
        List<Integer> numList = Arrays.asList(20, 30);

        TestCompareOperation op = createOp(strList, numList);
        assertTrue("Cross-type collection comparison should match element via equal(element, value)",
            (Boolean) op.computeValue(null));
    }
}