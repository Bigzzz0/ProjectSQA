package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Target Class: CoreOperationGreaterThanOrEqual
 * 
 * Decision Branches:
 * 1. computeValue() - double comparison branch: l >= r ? TRUE : FALSE
 * 2. Constructor - delegates to super with Expression array
 * 3. getSymbol() - returns ">="
 * 
 * Boundary Conditions:
 * - Positive numbers: l > r, l == r, l < r
 * - Negative numbers: l > r, l == r, l < r
 * - Zero: l == 0, r == 0
 * - Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
 * - Null arguments (via InfoSetUtil.doubleValue behavior)
 * - Node set operations (the known defect)
 * 
 * Defect Targeting (Defects4J ground truth):
 * - The bug is in computeValue() when evaluating node set operations like "$array > 0"
 * - The issue is that the comparison fails to correctly handle the case where the left operand
 *   is a node set that evaluates to a collection, and the comparison should iterate over elements
 *   or handle the conversion properly
 * - The test must verify that evaluating an expression like "($array > 0)" returns true when
 *   the array contains elements greater than 0
 */
public class CoreOperationGreaterThanOrEqualDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testComputeValue_LeftGreaterThanRight() {
        // Test: l > r should return Boolean.TRUE
        Expression left = new Constant(5.0);
        Expression right = new Constant(3.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        // We need a mock EvalContext - use a simple implementation
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertTrue("5.0 >= 3.0 should be true", result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_LeftEqualToRight() {
        // Test: l == r should return Boolean.TRUE
        Expression left = new Constant(7.0);
        Expression right = new Constant(7.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertTrue("7.0 >= 7.0 should be true", result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_LeftLessThanRight() {
        // Test: l < r should return Boolean.FALSE
        Expression left = new Constant(2.0);
        Expression right = new Constant(10.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertTrue("2.0 >= 10.0 should be false", result instanceof Boolean);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testGetSymbol() {
        Expression left = new Constant(1.0);
        Expression right = new Constant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        assertEquals(">=", op.getSymbol());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testComputeValue_WithZero() {
        // Test: 0 >= 0 should be true
        Expression left = new Constant(0.0);
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNegativeNumbers() {
        // Test: -1 >= -5 should be true
        Expression left = new Constant(-1.0);
        Expression right = new Constant(-5.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNegativeLessThan() {
        // Test: -10 >= -3 should be false
        Expression left = new Constant(-10.0);
        Expression right = new Constant(-3.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithDoubleMaxValue() {
        // Test: Double.MAX_VALUE >= Double.MAX_VALUE should be true
        Expression left = new Constant(Double.MAX_VALUE);
        Expression right = new Constant(Double.MAX_VALUE);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithDoubleMinValue() {
        // Test: Double.MIN_VALUE >= 0 should be true (MIN_VALUE is positive)
        Expression left = new Constant(Double.MIN_VALUE);
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithPositiveInfinity() {
        // Test: Double.POSITIVE_INFINITY >= 1000 should be true
        Expression left = new Constant(Double.POSITIVE_INFINITY);
        Expression right = new Constant(1000.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNegativeInfinity() {
        // Test: Double.NEGATIVE_INFINITY >= -1000 should be false
        Expression left = new Constant(Double.NEGATIVE_INFINITY);
        Expression right = new Constant(-1000.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNaN() {
        // Test: NaN >= 0 should be false (NaN comparisons always return false)
        Expression left = new Constant(Double.NaN);
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.FALSE, result);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // This targets the known Defects4J defect: "$array > 0" evaluating incorrectly

    @Test(timeout = 4000)
    public void testComputeValue_WithNodeSetLeftOperand() {
        // This test targets the known defect where evaluating expressions like
        // "$array > 0" returns false when it should return true
        // The defect is in how computeValue handles node set conversions
        
        // Create a node set expression that evaluates to a collection with values [1, 2, 3]
        Expression left = createNodeSetExpression(new double[] {1.0, 2.0, 3.0});
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        // The correct behavior: at least one element (1, 2, 3) is >= 0, so should be true
        assertEquals("Node set [1,2,3] >= 0 should be true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNodeSetAllLessThan() {
        // Test node set where all elements are less than the right operand
        Expression left = createNodeSetExpression(new double[] {-5.0, -3.0, -1.0});
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        // All elements are < 0, so should be false
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNodeSetMixedValues() {
        // Test node set with mixed values including one equal to the boundary
        Expression left = createNodeSetExpression(new double[] {-2.0, 0.0, 5.0});
        Expression right = new Constant(0.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        // Element 0.0 is >= 0.0, so should be true
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithNodeSetRightOperand() {
        // Test where right operand is a node set
        Expression left = new Constant(5.0);
        Expression right = createNodeSetExpression(new double[] {1.0, 10.0});
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        // 5.0 >= 1.0 is true, so should be true
        assertEquals(Boolean.TRUE, result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testComputeValue_WithNullArguments() {
        // Test with null expressions (should handle gracefully via InfoSetUtil)
        Expression left = new Constant(null);
        Expression right = new Constant(5.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        try {
            Object result = op.computeValue(context);
            // InfoSetUtil.doubleValue(null) returns 0.0, so 0.0 >= 5.0 is false
            assertEquals(Boolean.FALSE, result);
        } catch (Exception e) {
            fail("Should not throw exception for null arguments: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithStringArguments() {
        // Test with string arguments that can be converted to doubles
        Expression left = new Constant("10");
        Expression right = new Constant("5");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValue_WithBooleanArguments() {
        // Test with boolean arguments (true converts to 1.0, false to 0.0)
        Expression left = new Constant(Boolean.TRUE);
        Expression right = new Constant(Boolean.FALSE);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result = op.computeValue(context);
        
        assertEquals(Boolean.TRUE, result);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testConstructorAndInheritance() {
        Expression left = new Constant(1.0);
        Expression right = new Constant(2.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        // Verify it's an instance of the parent class
        assertTrue(op instanceof CoreOperationRelationalExpression);
        assertTrue(op instanceof CoreOperationCompare);
        
        // Verify the symbol
        assertEquals(">=", op.getSymbol());
        
        // Verify it's not a simple comparison (no "=" in symbol)
        assertFalse(op.isSimpleCompare());
    }

    @Test(timeout = 4000)
    public void testComputeValue_Consistency() {
        // Test that multiple calls return the same result
        Expression left = new Constant(42.0);
        Expression right = new Constant(42.0);
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = createSimpleEvalContext();
        Object result1 = op.computeValue(context);
        Object result2 = op.computeValue(context);
        
        assertEquals(result1, result2);
        assertEquals(Boolean.TRUE, result1);
    }

    // ===== Helper Methods =====

    private org.apache.commons.jxpath.ri.EvalContext createSimpleEvalContext() {
        // Create a minimal EvalContext for testing
        org.apache.commons.jxpath.JXPathContext jxpathContext = 
            org.apache.commons.jxpath.JXPathContext.newContext(new Object());
        org.apache.commons.jxpath.ri.JXPathContextReferenceImpl contextRef = 
            new org.apache.commons.jxpath.ri.JXPathContextReferenceImpl(jxpathContext);
        return new org.apache.commons.jxpath.ri.EvalContext(contextRef, null) {
            @Override
            public org.apache.commons.jxpath.Pointer getCurrentNodePointer() {
                return null;
            }
            @Override
            public boolean nextNode() {
                return false;
            }
            @Override
            public boolean nextSet() {
                return false;
            }
        };
    }

    private Expression createNodeSetExpression(final double[] values) {
        // Create a mock expression that returns a node set with the given values
        return new Expression() {
            @Override
            public Object compute(org.apache.commons.jxpath.ri.EvalContext context) {
                return computeValue(context);
            }
            
            @Override
            public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
                // Return a pointer set that simulates a node set
                java.util.List<org.apache.commons.jxpath.Pointer> pointers = new java.util.ArrayList<>();
                for (double value : values) {
                    pointers.add(new org.apache.commons.jxpath.ri.InfoSetUtil.DoublePointer(value));
                }
                return new org.apache.commons.jxpath.ri.model.jdom.JDOMNodeIterator(
                    context.getJXPathContext(), pointers);
            }
            
            @Override
            public boolean isContextDependent() {
                return true;
            }
            
            @Override
            public boolean computeContextDependent() {
                return true;
            }
            
            @Override
            public String toString() {
                return "nodeSet";
            }
        };
    }
}