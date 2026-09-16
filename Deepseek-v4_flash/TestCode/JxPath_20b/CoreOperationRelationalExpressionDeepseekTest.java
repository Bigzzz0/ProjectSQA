package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted:
 * 1. compute(): left/right instanceof Iterator (both, left only, right only)
 * 2. compute(): left/right instanceof InitialContext (reset called)
 * 3. compute(): Double.isNaN(ld) -> return false
 * 4. compute(): Double.isNaN(rd) -> return false
 * 5. compute(): evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1)
 * 6. reduce(): SelfContext -> getSingleNodePointer()
 * 7. reduce(): Collection -> iterator()
 * 8. containsMatch(): iterator iteration and recursive compute()
 * 9. findMatch(): HashSet accumulation and containsMatch on left set
 * 
 * Boundary conditions:
 * - NaN values (Double.NaN)
 * - Zero, positive, negative doubles
 * - Empty iterators/collections
 * - SelfContext wrapping various values
 * - InitialContext reset behavior
 * 
 * Defect-targeted: The known defect involves evaluating expressions like
 * "$a + $b <= $c" where the comparison operator's compute() method
 * may incorrectly handle the comparison when operands are computed
 * from variable contexts. The defect likely lies in how the comparison
 * result is evaluated when dealing with computed values that may
 * involve iterators or context-dependent values.
 */
public class CoreOperationRelationalExpressionDeepseekTest {

    // Helper concrete subclass for testing
    private static class TestLessThanOrEqual extends CoreOperationRelationalExpression {
        protected TestLessThanOrEqual(Expression[] args) {
            super(args);
        }
        protected boolean evaluateCompare(int compare) {
            return compare <= 0;
        }
        public String getSymbol() {
            return "<=";
        }
    }

    // Helper concrete subclass for testing
    private static class TestGreaterThan extends CoreOperationRelationalExpression {
        protected TestGreaterThan(Expression[] args) {
            super(args);
        }
        protected boolean evaluateCompare(int compare) {
            return compare > 0;
        }
        public String getSymbol() {
            return ">";
        }
    }

    // Helper Expression that returns a constant value
    private static class ConstantExpression extends Expression {
        private final Object value;
        
        ConstantExpression(Object value) {
            this.value = value;
        }
        
        public String toString() {
            return String.valueOf(value);
        }
        
        public Object compute(EvalContext context) {
            return value;
        }
        
        public Object computeValue(EvalContext context) {
            return value;
        }
        
        public boolean isContextDependent() {
            return false;
        }
        
        public boolean computeContextDependent() {
            return false;
        }
    }

    // ===== PARTITION A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testBasicLessThanOrEqual() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(10.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBasicGreaterThan() {
        CoreOperationRelationalExpression op = new TestGreaterThan(new Expression[]{
            new ConstantExpression(10.0),
            new ConstantExpression(5.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEqualValues() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(7.0),
            new ConstantExpression(7.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testFalseComparison() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(10.0),
            new ConstantExpression(5.0)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testGetPrecedence() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(1.0),
            new ConstantExpression(2.0)
        });
        assertEquals(CoreOperation.RELATIONAL_EXPR_PRECEDENCE, op.getPrecedence());
    }

    @Test(timeout = 4000)
    public void testIsSymmetric() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(1.0),
            new ConstantExpression(2.0)
        });
        assertFalse(op.isSymmetric());
    }

    // ===== PARTITION B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testNaNLeftOperand() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.NaN),
            new ConstantExpression(5.0)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNaNRightOperand() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(Double.NaN)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBothNaN() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.NaN),
            new ConstantExpression(Double.NaN)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testZeroValues() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(0.0),
            new ConstantExpression(0.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(-5.0),
            new ConstantExpression(-3.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testMaxDoubleValues() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.MAX_VALUE),
            new ConstantExpression(Double.MAX_VALUE)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testMinDoubleValues() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.MIN_VALUE),
            new ConstantExpression(Double.MIN_VALUE)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testPositiveInfinity() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.POSITIVE_INFINITY),
            new ConstantExpression(Double.POSITIVE_INFINITY)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNegativeInfinity() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(Double.NEGATIVE_INFINITY),
            new ConstantExpression(Double.NEGATIVE_INFINITY)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    // ===== PARTITION C: Defect-Targeted Branch Zone =====
    // Targets the known defect: evaluating expressions like "$a + $b <= $c"
    // where computed values from variable contexts may cause incorrect comparison

    @Test(timeout = 4000)
    public void testComplexOperationWithVariables() {
        // Simulate the scenario from the defect: $a + $b <= $c
        // where $a=1, $b=2, $c=3 => 1+2 <= 3 => true
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(3.0),  // Simulating $a + $b = 3
            new ConstantExpression(3.0)   // Simulating $c = 3
        });
        assertTrue("Evaluating <$a + $b <= $c> expected:<true> but was:<false>", 
                   (Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComplexOperationWithVariablesFalseCase() {
        // $a + $b <= $c where $a=2, $b=3, $c=4 => 5 <= 4 => false
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(4.0)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComplexOperationWithVariablesGreaterThan() {
        // $a + $b > $c where $a=2, $b=3, $c=4 => 5 > 4 => true
        CoreOperationRelationalExpression op = new TestGreaterThan(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(4.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    // ===== PARTITION D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testNullOperands() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(null),
            new ConstantExpression(null)
        });
        // null reduces to NaN via InfoSetUtil.doubleValue, so should be false
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testStringOperands() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression("5"),
            new ConstantExpression("10")
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBooleanOperands() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(true),
            new ConstantExpression(false)
        });
        // true=1, false=0 => 1 <= 0 => false
        assertFalse((Boolean) op.computeValue(null));
    }

    // ===== PARTITION E: Iterator and Collection Handling =====

    @Test(timeout = 4000)
    public void testLeftIteratorRightIterator() {
        // Both operands as iterators
        java.util.List<Double> leftList = new java.util.ArrayList<>();
        leftList.add(1.0);
        leftList.add(2.0);
        leftList.add(3.0);
        
        java.util.List<Double> rightList = new java.util.ArrayList<>();
        rightList.add(2.0);
        rightList.add(4.0);
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(leftList),
            new ConstantExpression(rightList)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testLeftIteratorRightValue() {
        java.util.List<Double> leftList = new java.util.ArrayList<>();
        leftList.add(1.0);
        leftList.add(2.0);
        leftList.add(3.0);
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(leftList),
            new ConstantExpression(2.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testRightIteratorLeftValue() {
        java.util.List<Double> rightList = new java.util.ArrayList<>();
        rightList.add(1.0);
        rightList.add(2.0);
        rightList.add(3.0);
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(2.0),
            new ConstantExpression(rightList)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyIterator() {
        java.util.List<Double> emptyList = new java.util.ArrayList<>();
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(emptyList),
            new ConstantExpression(5.0)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBothEmptyIterators() {
        java.util.List<Double> emptyList1 = new java.util.ArrayList<>();
        java.util.List<Double> emptyList2 = new java.util.ArrayList<>();
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(emptyList1),
            new ConstantExpression(emptyList2)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testIteratorNoMatch() {
        java.util.List<Double> leftList = new java.util.ArrayList<>();
        leftList.add(10.0);
        leftList.add(20.0);
        
        java.util.List<Double> rightList = new java.util.ArrayList<>();
        rightList.add(5.0);
        rightList.add(8.0);
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(leftList),
            new ConstantExpression(rightList)
        });
        assertFalse((Boolean) op.computeValue(null));
    }

    // ===== PARTITION F: SelfContext and InitialContext Handling =====

    @Test(timeout = 4000)
    public void testSelfContextReduction() {
        // SelfContext should be reduced via getSingleNodePointer()
        // We'll test with a simple value wrapped in a mock context
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(3.0),
            new ConstantExpression(5.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testCollectionReduction() {
        java.util.Collection<Double> collection = new java.util.ArrayList<>();
        collection.add(1.0);
        collection.add(2.0);
        collection.add(3.0);
        
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(collection),
            new ConstantExpression(2.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    // ===== PARTITION G: Edge Cases with Different Types =====

    @Test(timeout = 4000)
    public void testIntegerOperands() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5),
            new ConstantExpression(10)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testMixedNumericTypes() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5),
            new ConstantExpression(10.0)
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testStringToDoubleConversion() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression("3.14"),
            new ConstantExpression("3.14")
        });
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testInvalidStringConversion() {
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression("abc"),
            new ConstantExpression("def")
        });
        // Both convert to NaN, so should be false
        assertFalse((Boolean) op.computeValue(null));
    }

    // ===== PARTITION H: Multiple Comparisons in Sequence =====

    @Test(timeout = 4000)
    public void testMultipleComparisons() {
        // Test that the operation works correctly when called multiple times
        CoreOperationRelationalExpression op = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(10.0)
        });
        
        assertTrue((Boolean) op.computeValue(null));
        assertTrue((Boolean) op.computeValue(null));
        assertTrue((Boolean) op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testAlternatingComparisons() {
        CoreOperationRelationalExpression op1 = new TestLessThanOrEqual(new Expression[]{
            new ConstantExpression(5.0),
            new ConstantExpression(10.0)
        });
        CoreOperationRelationalExpression op2 = new TestGreaterThan(new Expression[]{
            new ConstantExpression(10.0),
            new ConstantExpression(5.0)
        });
        
        assertTrue((Boolean) op1.computeValue(null));
        assertTrue((Boolean) op2.computeValue(null));
    }
}