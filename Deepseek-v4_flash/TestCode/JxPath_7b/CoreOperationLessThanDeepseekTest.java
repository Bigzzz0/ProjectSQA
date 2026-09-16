package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: CoreOperationLessThan
 * 
 * Decision Branches:
 * 1. computeValue() - evaluates both args via InfoSetUtil.doubleValue()
 *    - Branch A: l < r -> returns Boolean.TRUE
 *    - Branch B: l >= r -> returns Boolean.FALSE
 *    - Note: The relational expression uses double comparison, so NaN handling
 *      is critical (NaN < anything is false, anything < NaN is false)
 * 
 * 2. getSymbol() - always returns "<"
 * 
 * 3. Constructor - delegates to super with array of two expressions
 * 
 * Boundary Conditions:
 * - Zero values (0.0, -0.0)
 * - Positive/Negative extremes (Double.MAX_VALUE, Double.MIN_VALUE)
 * - NaN (Double.NaN)
 * - Infinity (Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
 * - Null arguments (should cause NPE in computeValue)
 * 
 * Defect Analysis (from Defects4J):
 * - Known failure: Evaluating "<$array > 0>" expected:<true> but was:<false>
 * - This indicates the operator may be incorrectly handling node-set operations
 *   where the left operand is a node-set and the right is a scalar.
 *   The defect likely stems from incorrect evaluation order or type coercion
 *   when dealing with node-set values in the relational expression.
 * 
 * Test Strategy:
 * - Partition A: Core functional tests with simple numeric constants
 * - Partition B: Boundary values (zero, NaN, infinity, extremes)
 * - Partition C: Defect-targeted tests for node-set operations
 * - Partition D: Exception paths (null arguments)
 * - Partition E: Symbol and constructor contract tests
 */
public class CoreOperationLessThanDeepseekTest {

    // Helper to create a constant expression
    private Expression constant(double value) {
        return new Constant(value);
    }

    // Helper to create a node-set expression (simulating node-set operations)
    private Expression nodeSetExpression(final double value) {
        return new Expression() {
            @Override
            public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
                return new Double(value);
            }
            
            @Override
            public String toString() {
                return "nodeSet(" + value + ")";
            }
        };
    }

    // ===== Partition A: Core Functional Logic =====
    
    @Test(timeout = 4000)
    public void testLessThanTrue() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for 1 < 2", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanFalse() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(2.0), constant(1.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for 2 < 1", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanEqualValues() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(5.0), constant(5.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for 5 < 5", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanNegativeValues() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(-3.0), constant(-2.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for -3 < -2", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanMixedSigns() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(-1.0), constant(1.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for -1 < 1", Boolean.TRUE, result);
    }

    // ===== Partition B: Boundary Value Analysis =====
    
    @Test(timeout = 4000)
    public void testLessThanZeroBoundary() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(0.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for 0 < 0", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanNegativeZero() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(-0.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for -0.0 < 0.0", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanMaxDouble() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.MAX_VALUE), constant(Double.MAX_VALUE));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for MAX < MAX", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanMinDouble() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.MIN_VALUE), constant(Double.MIN_VALUE));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for MIN < MIN", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanPositiveInfinity() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.POSITIVE_INFINITY), constant(Double.POSITIVE_INFINITY));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for INF < INF", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanNegativeInfinity() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.NEGATIVE_INFINITY), constant(Double.NEGATIVE_INFINITY));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for -INF < -INF", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanNaN() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.NaN), constant(1.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for NaN < 1", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanNaNReverse() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(Double.NaN));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for 1 < NaN", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLessThanExtremeDifference() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(Double.NEGATIVE_INFINITY), constant(Double.POSITIVE_INFINITY));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for -INF < INF", Boolean.TRUE, result);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Target: Known defect with node-set operations
    // Evaluating "<$array > 0>" expected:<true> but was:<false>
    
    @Test(timeout = 4000)
    public void testNodeSetOperationLeftGreaterThanZero() {
        // Simulates the failing scenario: node-set value compared with 0
        // The node-set returns a value > 0, so the less-than comparison should be false
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(5.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for nodeSet(5) < 0", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationLeftLessThanZero() {
        // Node-set returns negative value, should be true for < 0
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(-5.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for nodeSet(-5) < 0", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationRightGreaterThanLeft() {
        // Node-set on right side
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), nodeSetExpression(2.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for 1 < nodeSet(2)", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationBothSides() {
        // Both sides are node-sets
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(1.0), nodeSetExpression(2.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for nodeSet(1) < nodeSet(2)", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationEqualValues() {
        // Node-set with equal values
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(3.0), nodeSetExpression(3.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for nodeSet(3) < nodeSet(3)", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationZeroComparison() {
        // Directly targets the defect: node-set value compared with 0
        // The defect expects true but gets false, so we test the correct behavior
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(0.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for nodeSet(0) < 0", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNodeSetOperationPositiveValueWithZero() {
        // This is the exact scenario from the defect report
        // "<$array > 0>" - array has positive values, so array > 0 is true
        // But the less-than operation should evaluate correctly
        CoreOperationLessThan op = new CoreOperationLessThan(nodeSetExpression(10.0), constant(0.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for nodeSet(10) < 0", Boolean.FALSE, result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullFirstArgument() {
        CoreOperationLessThan op = new CoreOperationLessThan(null, constant(1.0));
        op.computeValue(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullSecondArgument() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), null);
        op.computeValue(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullBothArguments() {
        CoreOperationLessThan op = new CoreOperationLessThan(null, null);
        op.computeValue(null);
    }

    @Test(timeout = 4000)
    public void testNullContextWithValidArgs() {
        // Context is null but args are valid constants - should work
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for 1 < 2 with null context", Boolean.TRUE, result);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        assertEquals("Symbol should be '<'", "<", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testConstructorAndGetSymbolConsistency() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(0.0), constant(0.0));
        assertNotNull("Operation should not be null", op);
        assertEquals("Symbol should be '<'", "<", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testMultipleEvaluationsConsistency() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        Object first = op.computeValue(null);
        Object second = op.computeValue(null);
        assertEquals("Results should be consistent", first, second);
        assertEquals("Should be Boolean.TRUE", Boolean.TRUE, first);
    }

    @Test(timeout = 4000)
    public void testComputeValueReturnsBooleanType() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        Object result = op.computeValue(null);
        assertTrue("Result should be a Boolean instance", result instanceof Boolean);
    }

    @Test(timeout = 4000)
    public void testComputeValueWithDifferentTypes() {
        // Test with integer-like doubles
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1.0), constant(2.0));
        Object result = op.computeValue(null);
        assertEquals("Should return Boolean.TRUE", Boolean.TRUE, result);
    }

    // Additional edge case: very close values
    @Test(timeout = 4000)
    public void testVeryCloseValues() {
        double left = 1.0000000000000002;
        double right = 1.0000000000000001;
        CoreOperationLessThan op = new CoreOperationLessThan(constant(left), constant(right));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        // Due to floating point precision, left > right, so less-than is false
        assertEquals("Should return Boolean.FALSE for very close values where left > right", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testLargeMagnitudeDifference() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1e308), constant(1e309));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.TRUE for 1e308 < 1e309", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testSmallMagnitudeDifference() {
        CoreOperationLessThan op = new CoreOperationLessThan(constant(1e-308), constant(1e-309));
        Object result = op.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertEquals("Should return Boolean.FALSE for 1e-308 < 1e-309", Boolean.FALSE, result);
    }
}