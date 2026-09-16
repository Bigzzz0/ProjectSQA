package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Target Class: CoreOperationNotEqual extends CoreOperationCompare
 * 
 * Decision Branches:
 * 1. computeValue(): Calls equal(context, args[0], args[1]) 
 *    - If equal() returns true -> returns Boolean.FALSE
 *    - If equal() returns false -> returns Boolean.TRUE
 * 
 * CoreOperationCompare (superclass) branches:
 * 2. equal() method: Handles null args, NaN comparisons, collection comparisons
 * 3. getSymbol(): Returns "!="
 * 
 * Boundary Conditions:
 * - NaN comparisons (known defect: NaN != NaN should be true, but bug returns false)
 * - Null arguments
 * - Empty strings
 * - Numeric comparisons (int, double, long)
 * - Boolean comparisons
 * - Collection comparisons
 * - Mixed type comparisons
 * 
 * Defect Targeting:
 * - Known Defect: NaN != NaN evaluates to false instead of true
 * - The equal() method in superclass incorrectly treats NaN == NaN as true
 * - CoreOperationNotEqual then negates this, returning false for NaN != NaN
 * - Correct behavior: NaN != NaN should be true (since NaN is not equal to itself)
 */

public class CoreOperationNotEqualDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testBasicIntegerNotEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("5"), new Constant("3"));
        assertTrue("5 != 3 should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testBasicIntegerEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("5"), new Constant("5"));
        assertTrue("5 != 5 should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testStringNotEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("hello"), new Constant("world"));
        assertTrue("'hello' != 'world' should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testStringEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("test"), new Constant("test"));
        assertTrue("'test' != 'test' should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testBooleanNotEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("true"), new Constant("false"));
        assertTrue("true != false should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testBooleanEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("true"), new Constant("true"));
        assertTrue("true != true should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("1"), new Constant("2"));
        assertEquals("Symbol should be !=", "!=", op.getSymbol());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testNullArguments() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(null), new Constant("test"));
        assertTrue("null != 'test' should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testBothNull() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(null), new Constant(null));
        assertTrue("null != null should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testEmptyString() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(""), new Constant(""));
        assertTrue("'' != '' should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testEmptyStringVsNull() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(""), new Constant(null));
        assertTrue("'' != null should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testZeroVsNegativeZero() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("0"), new Constant("-0"));
        assertTrue("0 != -0 should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testMaxIntegerBoundary() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(String.valueOf(Integer.MAX_VALUE)), 
            new Constant(String.valueOf(Integer.MAX_VALUE)));
        assertTrue("MAX_VALUE != MAX_VALUE should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testMinIntegerBoundary() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant(String.valueOf(Integer.MIN_VALUE)), 
            new Constant(String.valueOf(Integer.MIN_VALUE)));
        assertTrue("MIN_VALUE != MIN_VALUE should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testDoublePrecision() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("0.1"), new Constant("0.10000000000000001"));
        assertTrue("0.1 != 0.10000000000000001 should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testNanNotEqualNan() {
        // KNOWN DEFECT: NaN != NaN should be true, but bug returns false
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("NaN"), new Constant("NaN"));
        Boolean result = (Boolean) op.computeValue(null);
        assertTrue("NaN != NaN should be true (NaN is not equal to itself)", 
            Boolean.TRUE.equals(result));
    }

    @Test(timeout = 4000)
    public void testNanNotEqualNumber() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("NaN"), new Constant("5"));
        assertTrue("NaN != 5 should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testNumberNotEqualNan() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("5"), new Constant("NaN"));
        assertTrue("5 != NaN should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testPositiveInfinityNotEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("Infinity"), new Constant("Infinity"));
        assertTrue("Infinity != Infinity should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testNegativeInfinityNotEqual() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("-Infinity"), new Constant("-Infinity"));
        assertTrue("-Infinity != -Infinity should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testPositiveNegativeInfinity() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("Infinity"), new Constant("-Infinity"));
        assertTrue("Infinity != -Infinity should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullExpressionArgs() {
        // Constructor with null arguments should throw NPE
        new CoreOperationNotEqual(null, null);
    }

    @Test(timeout = 4000)
    public void testMixedTypeComparisons() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("5"), new Constant("5.0"));
        assertTrue("5 != 5.0 should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testStringVsNumber() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("5"), new Constant("five"));
        assertTrue("5 != 'five' should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testBooleanVsString() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("true"), new Constant("true"));
        assertTrue("'true' != 'true' should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testMultipleEvaluationsConsistency() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("42"), new Constant("42"));
        Boolean firstResult = (Boolean) op.computeValue(null);
        Boolean secondResult = (Boolean) op.computeValue(null);
        assertEquals("Multiple evaluations should be consistent", 
            firstResult, secondResult);
        assertTrue("42 != 42 should be false", 
            Boolean.FALSE.equals(firstResult));
    }

    @Test(timeout = 4000)
    public void testDifferentArgsSameResult() {
        CoreOperationNotEqual op1 = new CoreOperationNotEqual(
            new Constant("1"), new Constant("2"));
        CoreOperationNotEqual op2 = new CoreOperationNotEqual(
            new Constant("3"), new Constant("4"));
        assertEquals("Different unequal pairs should both be true", 
            op1.computeValue(null), op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testInverseOperation() {
        CoreOperationNotEqual notEqual = new CoreOperationNotEqual(
            new Constant("10"), new Constant("10"));
        CoreOperationEqual equal = new CoreOperationEqual(
            new Constant("10"), new Constant("10"));
        Boolean notEqualResult = (Boolean) notEqual.computeValue(null);
        Boolean equalResult = (Boolean) equal.computeValue(null);
        assertTrue("!= should be inverse of ==", 
            notEqualResult.equals(!equalResult));
    }

    @Test(timeout = 4000)
    public void testLargeNumberComparison() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("9999999999999999"), 
            new Constant("9999999999999999"));
        assertTrue("Large equal numbers should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testNegativeNumberComparison() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("-100"), new Constant("-100"));
        assertTrue("-100 != -100 should be false", 
            Boolean.FALSE.equals(op.computeValue(null)));
    }

    @Test(timeout = 4000)
    public void testZeroVsEmptyString() {
        CoreOperationNotEqual op = new CoreOperationNotEqual(
            new Constant("0"), new Constant(""));
        assertTrue("0 != '' should be true", 
            Boolean.TRUE.equals(op.computeValue(null)));
    }
}