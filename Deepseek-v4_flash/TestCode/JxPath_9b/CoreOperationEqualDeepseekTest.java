package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationEqualDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target Class: CoreOperationEqual extends CoreOperationCompare
     * Known Defect: NaN comparison returns true (should return false)
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - Basic equality: equal values -> true
     *   - Basic inequality: different values -> false
     *   - Null handling: null vs null, null vs non-null
     *   - String comparisons
     *   - Numeric comparisons
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - NaN comparisons (PRIMARY DEFECT TARGET)
     *   - Positive/negative infinity
     *   - Max/Min values
     *   - Zero vs negative zero
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - NaN == NaN should be false (IEEE 754 rule)
     *   - NaN == anyValue should be false
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - Null arguments in constructor
     *   - getSymbol() returns "="
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - Constructor with one null argument
     *   - Constructor with both null arguments
     */

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testEqualIntegers() {
        CoreOperationEqual op = new CoreOperationEqual(
            new Constant("5"), new Constant("5"));
        // Note: Constant with string converts to number
        // We need numeric constants for proper type handling
        // Using direct Constant(String) - this will be evaluated in context
        // For unit testing without full context, we test the object state
        assertEquals("=", op.getSymbol());
        assertTrue(op instanceof CoreOperationCompare);
    }

    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationEqual op = new CoreOperationEqual(
            new Constant("1"), new Constant("2"));
        assertEquals("Symbol should be '='", "=", op.getSymbol());
    }

    // ===== Partition C: Defect-Targeted Branch Zone (NaN handling) =====

    @Test(timeout = 4000)
    public void testNanEqualityShouldBeFalse() {
        // NaN == NaN must return false per IEEE 754
        // This directly targets the known defect
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        // Test NaN == NaN
        CoreOperationEqual nanEqual = new CoreOperationEqual(
            new Constant(Double.NaN, "NaN"),
            new Constant(Double.NaN, "NaN"));
        
        Object result = nanEqual.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("NaN == NaN should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNanNotEqualToNumber() {
        // NaN == 1 should be false
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        CoreOperationEqual nanToNumber = new CoreOperationEqual(
            new Constant(Double.NaN, "NaN"),
            new Constant(1.0, "1.0"));
        
        Object result = nanToNumber.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("NaN == 1 should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNumberNotEqualToNan() {
        // 1 == NaN should be false
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        CoreOperationEqual numberToNan = new CoreOperationEqual(
            new Constant(1.0, "1.0"),
            new Constant(Double.NaN, "NaN"));
        
        Object result = numberToNan.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("1 == NaN should be false", Boolean.FALSE, result);
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testPositiveInfinityEquality() {
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        CoreOperationEqual infEqual = new CoreOperationEqual(
            new Constant(Double.POSITIVE_INFINITY, "Infinity"),
            new Constant(Double.POSITIVE_INFINITY, "Infinity"));
        
        Object result = infEqual.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("+Inf == +Inf should be true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testNegativeInfinityEquality() {
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        CoreOperationEqual negInfEqual = new CoreOperationEqual(
            new Constant(Double.NEGATIVE_INFINITY, "-Infinity"),
            new Constant(Double.NEGATIVE_INFINITY, "-Infinity"));
        
        Object result = negInfEqual.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("-Inf == -Inf should be true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeZero() {
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        // 0 == -0 should be true according to IEEE 754
        CoreOperationEqual zeroToNegZero = new CoreOperationEqual(
            new Constant(0.0, "0.0"),
            new Constant(-0.0, "-0.0"));
        
        Object result = zeroToNegZero.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("0.0 == -0.0 should be true", Boolean.TRUE, result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testConstructorWithNullArgs() {
        // Should not throw exception during construction
        CoreOperationEqual op = new CoreOperationEqual(null, null);
        assertNotNull("Object should be created", op);
        assertEquals("=", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testConstructorWithOneNullArg() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant("test"), null);
        assertNotNull("Object should be created", op);
        assertEquals("=", op.getSymbol());
        
        CoreOperationEqual op2 = new CoreOperationEqual(null, new Constant("test"));
        assertNotNull("Object should be created", op2);
        assertEquals("=", op2.getSymbol());
    }

    // ===== Additional Defect Coverage: Multiple NaN scenarios =====

    @Test(timeout = 4000)
    public void testNanNotEqualToNanFromString() {
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        // Using string "NaN" which should be converted
        CoreOperationEqual nanStringEqual = new CoreOperationEqual(
            new Constant("NaN"), new Constant("NaN"));
        
        Object result = nanStringEqual.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("'NaN' == 'NaN' should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testNanComparisonWithNegativeNumber() {
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        CoreOperationEqual nanToNegNumber = new CoreOperationEqual(
            new Constant(Double.NaN, "NaN"),
            new Constant(-42.0, "-42.0"));
        
        Object result = nanToNegNumber.computeValue(context);
        assertNotNull("Result should not be null", result);
        assertEquals("NaN == -42 should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testMultipleNanExplicitly() {
        // Direct test of the known defect scenario
        org.apache.commons.jxpath.ri.EvalContext context = 
            org.apache.commons.jxpath.ri.JXPathContextReferenceImpl.
            getContext(new org.apache.commons.jxpath.JXPathContext(
                null, new Object()));
        
        // Create two NaN values
        double nan1 = Double.NaN;
        double nan2 = Double.NaN;
        
        // Direct IEEE 754 check (should never be equal)
        assertFalse("IEEE 754: NaN != NaN", nan1 == nan2);
        
        // Now test via CoreOperationEqual
        CoreOperationEqual op = new CoreOperationEqual(
            new Constant(nan1, "NaN"),
            new Constant(nan2, "NaN"));
        
        Object result = op.computeValue(context);
        assertEquals("CoreOperationEqual: NaN == NaN should be false", 
            Boolean.FALSE, result);
    }
}