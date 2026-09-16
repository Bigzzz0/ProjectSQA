package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: CoreOperationGreaterThan extends CoreOperationRelationalExpression
 * 
 * Decision Branches:
 * - computeValue(): Main execution path -> double comparison
 * - Branch 1: l > r returns Boolean.TRUE
 * - Branch 2: l <= r returns Boolean.FALSE
 * - getSymbol(): Returns ">"
 * 
 * Defect Analysis (Defects4J ground truth):
 * - Known failure: Evaluating <$array > 0> expected:<true> but was:<false>
 * - Root cause likely in parent class handling of node sets (arrays)
 * - CoreOperationGreaterThan.computeValue() calls args[0].computeValue(context) 
 *   which for node sets returns an iterator/pointer, not a direct numeric value
 * - Need to test with node set expressions that contain multiple elements > comparison value
 * 
 * Boundary Values:
 * - Positive: 5.0 > 3.0 -> true
 * - Negative: 2.0 > 5.0 -> false  
 * - Equal: 4.0 > 4.0 -> false
 * - Zero: 0.0 > -1.0 -> true
 * - Negative comparison: -3.0 > -5.0 -> true
 * - Integer vs double: 5 > 4.5 -> true
 * - NaN handling (if applicable)
 * - Null/undefined handling (InfoSetUtil.doubleValue converts null to 0)
 */
public class CoreOperationGreaterThanDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testCoreFunctional_TrueCase() {
        // Test basic true comparison
        Expression left = new Constant(10);
        Expression right = new Constant(5);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        // Verify symbol
        assertEquals(">", op.getSymbol());
        
        // Compute in a context
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testCoreFunctional_FalseCase() {
        // Test false comparison (left < right)
        Expression left = new Constant(3);
        Expression right = new Constant(8);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.FALSE, result);
    }
    
    @Test(timeout = 4000)
    public void testCoreFunctional_EqualCase() {
        // Test equal values -> false (not greater)
        Expression left = new Constant(7);
        Expression right = new Constant(7);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.FALSE, result);
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testBVA_NegativeValues() {
        // Test negative vs negative
        Expression left = new Constant(-3.0);
        Expression right = new Constant(-5.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testBVA_ZeroBoundary() {
        // Test zero boundaries
        Expression left = new Constant(0.0);
        Expression right = new Constant(-0.001);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testBVA_LargeValues() {
        // Test large double values
        Expression left = new Constant(Double.MAX_VALUE);
        Expression right = new Constant(Double.MIN_VALUE);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testBVA_Infinity() {
        // Test infinity handling
        Expression left = new Constant(Double.POSITIVE_INFINITY);
        Expression right = new Constant(0.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testDefectTarget_NodeSetGreaterThanValue() {
        // Reproduce the known defect: Evaluating <$array > 0> expected:<true> but was:<false>
        // This simulates a node set with elements > comparison value
        // We use a direct expression simulation to trigger the defect path
        
        // Simulate node set: create a variable reference that might evaluate to an array/iterator
        Expression expr = new VariableReference("nodeSet");
        Constant zero = new Constant(0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(expr, zero);
        
        org.apache.commons.jxpath.ri.JXPathContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        context.getVariables().declareVariable("nodeSet", new double[] {1.0, 2.0, 3.0});
        
        Object result = op.computeValue(context);
        assertEquals("Node set with all elements > 0 should return true", Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testDefectTarget_NodeSetPartialMatch() {
        // Test node set where some elements are > comparison value
        Expression expr = new VariableReference("mixedSet");
        Constant three = new Constant(3.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(expr, three);
        
        org.apache.commons.jxpath.ri.JXPathContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        context.getVariables().declareVariable("mixedSet", new double[] {1.0, 5.0, 2.0});
        
        Object result = op.computeValue(context);
        assertEquals("Node set with at least one element > 3 should return true", Boolean.TRUE, result);
    }
    
    @Test(timeout = 4000)
    public void testDefectTarget_NodeSetNoneMatch() {
        // Test node set where no elements are > comparison value
        Expression expr = new VariableReference("smallSet");
        Constant five = new Constant(5.0);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(expr, five);
        
        org.apache.commons.jxpath.ri.JXPathContext context = 
            org.apache.commons.jxpath.ri.JXPathContextFactory.newContext(null, null);
        context.getVariables().declareVariable("smallSet", new double[] {1.0, 2.0, 3.0});
        
        Object result = op.computeValue(context);
        assertEquals("Node set with no element > 5 should return false", Boolean.FALSE, result);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testObjectCreation() {
        // Test constructor and basic object state
        Expression left = new Constant(10);
        Expression right = new Constant(5);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        assertNotNull("Object should be created", op);
        assertEquals("Symbol should be >", ">", op.getSymbol());
        assertNotNull("Args should not be null", op.args);
        assertEquals("Should have 2 args", 2, op.args.length);
    }
    
    @Test(timeout = 4000)
    public void testGetSymbol() {
        // Verify getSymbol returns correct string
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new Constant(1), new Constant(2));
        assertEquals(">", op.getSymbol());
    }
}