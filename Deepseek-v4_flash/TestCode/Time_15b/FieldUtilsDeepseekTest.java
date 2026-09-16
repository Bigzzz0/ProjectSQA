package org.joda.time.field;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: FieldUtils.java (Defects4J - defect in safeMultiply(long, int))
 * 
 * Defect: The safeMultiply(long val1, int val2) method has a bug in the
 *         overflow detection logic. The current implementation:
 *         - Handles special cases for val2 = -1, 0, 1
 *         - For other cases, computes total = val1 * val2 and checks
 *           if (total / val2 != val1)
 *         - This check FAILS to detect overflow when val1 = Long.MIN_VALUE
 *           and val2 = -1 (which is handled by the special case) but more
 *           importantly, it fails to detect overflow when val1 = Long.MIN_VALUE
 *           and val2 = 2 (or any other multiplier that causes overflow)
 *           because the division check may pass due to overflow behavior.
 * 
 * Branch Coverage Targets:
 * 1. safeNegate: value == MIN_VALUE branch, normal negation
 * 2. safeAdd(int,int): overflow detection (positive+positive, negative+negative)
 * 3. safeAdd(long,long): overflow detection (positive+positive, negative+negative)
 * 4. safeSubtract(long,long): overflow detection (positive-negative, negative-positive)
 * 5. safeMultiply(int,int): overflow detection (positive*positive, negative*negative, mixed)
 * 6. safeMultiply(long,int): special cases (val2=-1,0,1), overflow detection
 * 7. safeMultiply(long,long): special cases (val2=1, val1=1, zero), overflow detection
 * 8. safeToInt: boundary values (MIN_VALUE, MAX_VALUE, out of range)
 * 9. safeMultiplyToInt: overflow detection
 * 10. verifyValueBounds: value < lowerBound, value > upperBound, valid value
 * 11. getWrappedValue: minValue >= maxValue, positive value, negative value with remainder 0, negative value with non-zero remainder
 * 12. equals: same reference, both null, one null, equal objects, non-equal objects
 * 
 * Boundary Value Analysis:
 * - Integer.MIN_VALUE, Integer.MAX_VALUE
 * - Long.MIN_VALUE, Long.MAX_VALUE
 * - Zero, One, Negative One
 * - Values at exact boundaries (lowerBound, upperBound)
 * - Values just outside boundaries (lowerBound-1, upperBound+1)
 * 
 * Defect-Targeted Test:
 * - testSafeMultiplyLongInt_Overflow_MinValueTimesTwo
 *   This test targets the specific defect where safeMultiply(Long.MIN_VALUE, 2)
 *   should throw ArithmeticException but the current implementation may not
 *   detect the overflow correctly.
 */
public class FieldUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testSafeNegate_NormalValues() {
        assertEquals("Negate positive", -5, FieldUtils.safeNegate(5));
        assertEquals("Negate negative", 5, FieldUtils.safeNegate(-5));
        assertEquals("Negate zero", 0, FieldUtils.safeNegate(0));
        assertEquals("Negate max", -Integer.MAX_VALUE, FieldUtils.safeNegate(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Int_NormalValues() {
        assertEquals("Add positive", 10, FieldUtils.safeAdd(3, 7));
        assertEquals("Add negative", -10, FieldUtils.safeAdd(-3, -7));
        assertEquals("Add mixed", 4, FieldUtils.safeAdd(-3, 7));
        assertEquals("Add zero", 5, FieldUtils.safeAdd(5, 0));
        assertEquals("Add max", Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE - 1, 1));
        assertEquals("Add min", Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Long_NormalValues() {
        assertEquals("Add positive", 10L, FieldUtils.safeAdd(3L, 7L));
        assertEquals("Add negative", -10L, FieldUtils.safeAdd(-3L, -7L));
        assertEquals("Add mixed", 4L, FieldUtils.safeAdd(-3L, 7L));
        assertEquals("Add zero", 5L, FieldUtils.safeAdd(5L, 0L));
        assertEquals("Add max", Long.MAX_VALUE, FieldUtils.safeAdd(Long.MAX_VALUE - 1, 1L));
        assertEquals("Add min", Long.MIN_VALUE, FieldUtils.safeAdd(Long.MIN_VALUE + 1, -1L));
    }

    @Test(timeout = 4000)
    public void testSafeSubtract_Long_NormalValues() {
        assertEquals("Subtract positive", 3L, FieldUtils.safeSubtract(10L, 7L));
        assertEquals("Subtract negative", -3L, FieldUtils.safeSubtract(-10L, -7L));
        assertEquals("Subtract mixed", 10L, FieldUtils.safeSubtract(3L, -7L));
        assertEquals("Subtract zero", 5L, FieldUtils.safeSubtract(5L, 0L));
        assertEquals("Subtract max", Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE - 1, -1L));
        assertEquals("Subtract min", Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE + 1, 1L));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_Int_NormalValues() {
        assertEquals("Multiply positive", 21, FieldUtils.safeMultiply(3, 7));
        assertEquals("Multiply negative", 21, FieldUtils.safeMultiply(-3, -7));
        assertEquals("Multiply mixed", -21, FieldUtils.safeMultiply(3, -7));
        assertEquals("Multiply zero", 0, FieldUtils.safeMultiply(0, 7));
        assertEquals("Multiply one", 7, FieldUtils.safeMultiply(1, 7));
        assertEquals("Multiply max", Integer.MAX_VALUE, FieldUtils.safeMultiply(1, Integer.MAX_VALUE));
        assertEquals("Multiply min", Integer.MIN_VALUE, FieldUtils.safeMultiply(1, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongInt_NormalValues() {
        assertEquals("Multiply positive", 21L, FieldUtils.safeMultiply(3L, 7));
        assertEquals("Multiply negative", 21L, FieldUtils.safeMultiply(-3L, -7));
        assertEquals("Multiply mixed", -21L, FieldUtils.safeMultiply(3L, -7));
        assertEquals("Multiply zero", 0L, FieldUtils.safeMultiply(0L, 7));
        assertEquals("Multiply one", 7L, FieldUtils.safeMultiply(7L, 1));
        assertEquals("Multiply minus one", -7L, FieldUtils.safeMultiply(7L, -1));
        assertEquals("Multiply max", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1));
        assertEquals("Multiply min", Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongLong_NormalValues() {
        assertEquals("Multiply positive", 21L, FieldUtils.safeMultiply(3L, 7L));
        assertEquals("Multiply negative", 21L, FieldUtils.safeMultiply(-3L, -7L));
        assertEquals("Multiply mixed", -21L, FieldUtils.safeMultiply(3L, -7L));
        assertEquals("Multiply zero", 0L, FieldUtils.safeMultiply(0L, 7L));
        assertEquals("Multiply one", 7L, FieldUtils.safeMultiply(7L, 1L));
        assertEquals("Multiply minus one", -7L, FieldUtils.safeMultiply(7L, -1L));
        assertEquals("Multiply max", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1L));
        assertEquals("Multiply min", Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1L));
    }

    @Test(timeout = 4000)
    public void testSafeToInt_NormalValues() {
        assertEquals("To int positive", 5, FieldUtils.safeToInt(5L));
        assertEquals("To int negative", -5, FieldUtils.safeToInt(-5L));
        assertEquals("To int zero", 0, FieldUtils.safeToInt(0L));
        assertEquals("To int max", Integer.MAX_VALUE, FieldUtils.safeToInt(Integer.MAX_VALUE));
        assertEquals("To int min", Integer.MIN_VALUE, FieldUtils.safeToInt(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyToInt_NormalValues() {
        assertEquals("Multiply to int", 21, FieldUtils.safeMultiplyToInt(3L, 7L));
        assertEquals("Multiply to int negative", -21, FieldUtils.safeMultiplyToInt(-3L, 7L));
        assertEquals("Multiply to int zero", 0, FieldUtils.safeMultiplyToInt(0L, 7L));
        assertEquals("Multiply to int one", 7, FieldUtils.safeMultiplyToInt(7L, 1L));
    }

    @Test(timeout = 4000)
    public void testVerifyValueBounds_ValidValues() {
        // Should not throw
        FieldUtils.verifyValueBounds("testField", 5, 0, 10);
        FieldUtils.verifyValueBounds("testField", 0, 0, 10);
        FieldUtils.verifyValueBounds("testField", 10, 0, 10);
        FieldUtils.verifyValueBounds("testField", Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        FieldUtils.verifyValueBounds("testField", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test(timeout = 4000)
    public void testGetWrappedValue_NormalValues() {
        assertEquals("Wrap positive", 5, FieldUtils.getWrappedValue(5, 0, 10));
        assertEquals("Wrap positive overflow", 2, FieldUtils.getWrappedValue(12, 0, 10));
        assertEquals("Wrap negative", 9, FieldUtils.getWrappedValue(-1, 0, 10));
        assertEquals("Wrap negative exact", 0, FieldUtils.getWrappedValue(-10, 0, 10));
        assertEquals("Wrap with offset", 3, FieldUtils.getWrappedValue(13, 0, 10));
        assertEquals("Wrap with min offset", 5, FieldUtils.getWrappedValue(5, 5, 15));
        assertEquals("Wrap with min offset overflow", 7, FieldUtils.getWrappedValue(17, 5, 15));
        assertEquals("Wrap with min offset negative", 14, FieldUtils.getWrappedValue(-1, 5, 15));
    }

    @Test(timeout = 4000)
    public void testEquals_NormalValues() {
        assertTrue("Same reference", FieldUtils.equals("test", "test"));
        assertTrue("Both null", FieldUtils.equals(null, null));
        assertFalse("One null", FieldUtils.equals(null, "test"));
        assertFalse("One null reverse", FieldUtils.equals("test", null));
        assertTrue("Equal objects", FieldUtils.equals("test", new String("test")));
        assertFalse("Non-equal objects", FieldUtils.equals("test", "other"));
        assertTrue("Integer equal", FieldUtils.equals(Integer.valueOf(5), Integer.valueOf(5)));
        assertFalse("Integer non-equal", FieldUtils.equals(Integer.valueOf(5), Integer.valueOf(6)));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testSafeNegate_BoundaryValues() {
        assertEquals("Negate max", -Integer.MAX_VALUE, FieldUtils.safeNegate(Integer.MAX_VALUE));
        assertEquals("Negate min+1", Integer.MAX_VALUE, FieldUtils.safeNegate(Integer.MIN_VALUE + 1));
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Int_BoundaryValues() {
        assertEquals("Add max+0", Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE, 0));
        assertEquals("Add min+0", Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE, 0));
        assertEquals("Add max-1+1", Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE - 1, 1));
        assertEquals("Add min+1-1", Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Long_BoundaryValues() {
        assertEquals("Add max+0", Long.MAX_VALUE, FieldUtils.safeAdd(Long.MAX_VALUE, 0L));
        assertEquals("Add min+0", Long.MIN_VALUE, FieldUtils.safeAdd(Long.MIN_VALUE, 0L));
        assertEquals("Add max-1+1", Long.MAX_VALUE, FieldUtils.safeAdd(Long.MAX_VALUE - 1, 1L));
        assertEquals("Add min+1-1", Long.MIN_VALUE, FieldUtils.safeAdd(Long.MIN_VALUE + 1, -1L));
    }

    @Test(timeout = 4000)
    public void testSafeSubtract_Long_BoundaryValues() {
        assertEquals("Subtract max-0", Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE, 0L));
        assertEquals("Subtract min-0", Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE, 0L));
        assertEquals("Subtract max-(-1)", Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE - 1, -1L));
        assertEquals("Subtract min-1", Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE + 1, 1L));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_Int_BoundaryValues() {
        assertEquals("Multiply max*1", Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, 1));
        assertEquals("Multiply min*1", Integer.MIN_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, 1));
        assertEquals("Multiply max*-1", -Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, -1));
        assertEquals("Multiply min*-1", Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, -1));
        assertEquals("Multiply max*0", 0, FieldUtils.safeMultiply(Integer.MAX_VALUE, 0));
        assertEquals("Multiply min*0", 0, FieldUtils.safeMultiply(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongInt_BoundaryValues() {
        assertEquals("Multiply max*1", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1));
        assertEquals("Multiply min*1", Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1));
        assertEquals("Multiply max*-1", -Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1));
        assertEquals("Multiply min*-1", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, -1));
        assertEquals("Multiply max*0", 0L, FieldUtils.safeMultiply(Long.MAX_VALUE, 0));
        assertEquals("Multiply min*0", 0L, FieldUtils.safeMultiply(Long.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongLong_BoundaryValues() {
        assertEquals("Multiply max*1", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1L));
        assertEquals("Multiply min*1", Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1L));
        assertEquals("Multiply max*-1", -Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, -1L));
        assertEquals("Multiply min*-1", Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, -1L));
        assertEquals("Multiply max*0", 0L, FieldUtils.safeMultiply(Long.MAX_VALUE, 0L));
        assertEquals("Multiply min*0", 0L, FieldUtils.safeMultiply(Long.MIN_VALUE, 0L));
        assertEquals("Multiply 1*max", Long.MAX_VALUE, FieldUtils.safeMultiply(1L, Long.MAX_VALUE));
        assertEquals("Multiply 1*min", Long.MIN_VALUE, FieldUtils.safeMultiply(1L, Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeToInt_BoundaryValues() {
        assertEquals("To int max", Integer.MAX_VALUE, FieldUtils.safeToInt(Integer.MAX_VALUE));
        assertEquals("To int min", Integer.MIN_VALUE, FieldUtils.safeToInt(Integer.MIN_VALUE));
        assertEquals("To int max+1", Integer.MAX_VALUE, FieldUtils.safeToInt(Integer.MAX_VALUE + 1L));
        assertEquals("To int min-1", Integer.MIN_VALUE, FieldUtils.safeToInt(Integer.MIN_VALUE - 1L));
    }

    @Test(timeout = 4000)
    public void testVerifyValueBounds_BoundaryValues() {
        // Exact boundaries should not throw
        FieldUtils.verifyValueBounds("testField", 0, 0, 0);
        FieldUtils.verifyValueBounds("testField", 5, 5, 5);
        FieldUtils.verifyValueBounds("testField", Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        FieldUtils.verifyValueBounds("testField", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Test(timeout = 4000)
    public void testGetWrappedValue_BoundaryValues() {
        assertEquals("Wrap min", 0, FieldUtils.getWrappedValue(0, 0, 10));
        assertEquals("Wrap max", 10, FieldUtils.getWrappedValue(10, 0, 10));
        assertEquals("Wrap max+1", 0, FieldUtils.getWrappedValue(11, 0, 10));
        assertEquals("Wrap min-1", 10, FieldUtils.getWrappedValue(-1, 0, 10));
        assertEquals("Wrap exact range", 0, FieldUtils.getWrappedValue(10, 0, 10));
        assertEquals("Wrap negative exact range", 0, FieldUtils.getWrappedValue(-10, 0, 10));
        assertEquals("Wrap negative range+1", 9, FieldUtils.getWrappedValue(-11, 0, 10));
    }

    @Test(timeout = 4000)
    public void testEquals_BoundaryValues() {
        assertTrue("Same object", FieldUtils.equals(new Object(), new Object()));
        assertFalse("Different objects", FieldUtils.equals(new Object(), new Object()));
        assertTrue("Null equals null", FieldUtils.equals(null, null));
        assertFalse("Null equals object", FieldUtils.equals(null, new Object()));
        assertFalse("Object equals null", FieldUtils.equals(new Object(), null));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-Targeted Test:
     * The safeMultiply(long, int) method has a bug where overflow is not
     * correctly detected for certain values. Specifically, when val1 = Long.MIN_VALUE
     * and val2 = 2, the multiplication overflows but the division check
     * (total / val2 != val1) may not catch it due to overflow behavior.
     * 
     * Expected: ArithmeticException should be thrown
     * Actual (defective): May return incorrect value without throwing
     */
    @Test(timeout = 4000)
    public void testSafeMultiplyLongInt_Overflow_MinValueTimesTwo() {
        try {
            long result = FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
            fail("Expected ArithmeticException for Long.MIN_VALUE * 2, but got: " + result);
        } catch (ArithmeticException e) {
            // Expected exception
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    /**
     * Additional defect-targeted test for Long.MIN_VALUE * -1
     * This should also throw ArithmeticException
     */
    @Test(timeout = 4000)
    public void testSafeMultiplyLongInt_Overflow_MinValueTimesMinusOne() {
        try {
            long result = FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
            fail("Expected ArithmeticException for Long.MIN_VALUE * -1, but got: " + result);
        } catch (ArithmeticException e) {
            // Expected exception
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    /**
     * Additional defect-targeted test for Long.MIN_VALUE * 3
     */
    @Test(timeout = 4000)
    public void testSafeMultiplyLongInt_Overflow_MinValueTimesThree() {
        try {
            long result = FieldUtils.safeMultiply(Long.MIN_VALUE, 3);
            fail("Expected ArithmeticException for Long.MIN_VALUE * 3, but got: " + result);
        } catch (ArithmeticException e) {
            // Expected exception
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    /**
     * Additional defect-targeted test for Long.MAX_VALUE * 2
     */
    @Test(timeout = 4000)
    public void testSafeMultiplyLongInt_Overflow_MaxValueTimesTwo() {
        try {
            long result = FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
            fail("Expected ArithmeticException for Long.MAX_VALUE * 2, but got: " + result);
        } catch (ArithmeticException e) {
            // Expected exception
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    /**
     * Additional defect-targeted test for Long.MAX_VALUE * -2
     */
    @Test(timeout = 4000)
    public void testSafeMultiplyLongInt_Overflow_MaxValueTimesMinusTwo() {
        try {
            long result = FieldUtils.safeMultiply(Long.MAX_VALUE, -2);
            fail("Expected ArithmeticException for Long.MAX_VALUE * -2, but got: " + result);
        } catch (ArithmeticException e) {
            // Expected exception
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testSafeNegate_MinValue_ThrowsArithmeticException() {
        try {
            FieldUtils.safeNegate(Integer.MIN_VALUE);
            fail("Expected ArithmeticException for Integer.MIN_VALUE");
        } catch (ArithmeticException e) {
            assertEquals("Exception message", "Integer.MIN_VALUE cannot be negated", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Int_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException for Integer.MAX_VALUE + 1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException for Integer.MIN_VALUE + (-1)");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeAdd_Long_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException for Long.MAX_VALUE + 1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException for Long.MIN_VALUE + (-1)");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeSubtract_Long_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
            fail("Expected ArithmeticException for Long.MAX_VALUE - (-1)");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException for Long.MIN_VALUE - 1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_Int_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException for Integer.MAX_VALUE * 2");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeMultiply(Integer.MIN_VALUE, 2);
            fail("Expected ArithmeticException for Integer.MIN_VALUE * 2");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeMultiply(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException for Integer.MIN_VALUE * -1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongLong_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeMultiply(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException for Long.MAX_VALUE * 2");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException for Long.MIN_VALUE * 2");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException for Long.MIN_VALUE * -1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }

        try {
            FieldUtils.safeMultiply(-1L, Long.MIN_VALUE);
            fail("Expected ArithmeticException for -1 * Long.MIN_VALUE");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeToInt_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeToInt(Integer.MAX_VALUE + 1L);
            fail("Expected ArithmeticException for Integer.MAX_VALUE + 1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention cannot fit", 
                       e.getMessage().contains("cannot fit"));
        }

        try {
            FieldUtils.safeToInt(Integer.MIN_VALUE - 1L);
            fail("Expected ArithmeticException for Integer.MIN_VALUE - 1");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention cannot fit", 
                       e.getMessage().contains("cannot fit"));
        }
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyToInt_Overflow_ThrowsArithmeticException() {
        try {
            FieldUtils.safeMultiplyToInt(Integer.MAX_VALUE, 2L);
            fail("Expected ArithmeticException for Integer.MAX_VALUE * 2");
        } catch (ArithmeticException e) {
            assertTrue("Exception message should mention overflow", 
                       e.getMessage().contains("overflow"));
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBounds_OutOfBounds_ThrowsIllegalFieldValueException() {
        try {
            FieldUtils.verifyValueBounds("testField", -1, 0, 10);
            fail("Expected IllegalFieldValueException for value below lower bound");
        } catch (IllegalFieldValueException e) {
            assertEquals("Field name", "testField", e.getFieldName());
            assertEquals("Value", Integer.valueOf(-1), e.getValue());
            assertEquals("Lower bound", Integer.valueOf(0), e.getLowerBound());
            assertEquals("Upper bound", Integer.valueOf(10), e.getUpperBound());
        }

        try {
            FieldUtils.verifyValueBounds("testField", 11, 0, 10);
            fail("Expected IllegalFieldValueException for value above upper bound");
        } catch (IllegalFieldValueException e) {
            assertEquals("Field name", "testField", e.getFieldName());
            assertEquals("Value", Integer.valueOf(11), e.getValue());
            assertEquals("Lower bound", Integer.valueOf(0), e.getLowerBound());
            assertEquals("Upper bound", Integer.valueOf(10), e.getUpperBound());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBounds_WithDateTimeField_ThrowsIllegalFieldValueException() {
        DateTimeField field = new DateTimeField() {
            @Override
            public DateTimeFieldType getType() {
                return DateTimeFieldType.year();
            }

            @Override
            public String getName() {
                return "year";
            }

            @Override
            public boolean isSupported() {
                return true;
            }

            @Override
            public boolean isLenient() {
                return false;
            }

            @Override
            public int get(long instant) {
                return 0;
            }

            @Override
            public String getAsText(long instant, java.util.Locale locale) {
                return null;
            }

            @Override
            public String getAsText(int fieldValue, java.util.Locale locale) {
                return null;
            }

            @Override
            public String getAsShortText(long instant, java.util.Locale locale) {
                return null;
            }

            @Override
            public String getAsShortText(int fieldValue, java.util.Locale locale) {
                return null;
            }

            @Override
            public long add(long instant, int value) {
                return 0;
            }

            @Override
            public long add(long instant, long value) {
                return 0;
            }

            @Override
            public int[] add(org.joda.time.ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
                return new int[0];
            }

            @Override
            public int[] addWrapPartial(org.joda.time.ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
                return new int[0];
            }

            @Override
            public long addWrapField(long instant, int value) {
                return 0;
            }

            @Override
            public int[] addWrapField(org.joda.time.ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
                return new int[0];
            }

            @Override
            public int getDifference(long minuendInstant, long subtrahendInstant) {
                return 0;
            }

            @Override
            public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {
                return 0;
            }

            @Override
            public long set(long instant, int value) {
                return 0;
            }

            @Override
            public long set(long instant, String text, java.util.Locale locale) {
                return 0;
            }

            @Override
            public int[] set(org.joda.time.ReadablePartial partial, int fieldIndex, int[] values, int newValue) {
                return new int[0];
            }

            @Override
            public int[] set(org.joda.time.ReadablePartial partial, int fieldIndex, int[] values, String text, java.util.Locale locale) {
                return new int[0];
            }

            @Override
            public int getMinimumValue() {
                return 0;
            }

            @Override
            public int getMinimumValue(long instant) {
                return 0;
            }

            @Override
            public int getMinimumValue(org.joda.time.ReadablePartial partial) {
                return 0;
            }

            @Override
            public int getMinimumValue(org.joda.time.ReadablePartial partial, int[] values) {
                return 0;
            }

            @Override
            public int getMaximumValue() {
                return 0;
            }

            @Override
            public int getMaximumValue(long instant) {
                return 0;
            }

            @Override
            public int getMaximumValue(org.joda.time.ReadablePartial partial) {
                return 0;
            }

            @Override
            public int getMaximumValue(org.joda.time.ReadablePartial partial, int[] values) {
                return 0;
            }

            @Override
            public int getMaximumTextLength(java.util.Locale locale) {
                return 0;
            }

            @Override
            public int getMaximumShortTextLength(java.util.Locale locale) {
                return 0;
            }

            @Override
            public boolean isLeap(long instant) {
                return false;
            }

            @Override
            public long roundFloor(long instant) {
                return 0;
            }

            @Override
            public long roundCeiling(long instant) {
                return 0;
            }

            @Override
            public long roundHalfFloor(long instant) {
                return 0;
            }

            @Override
            public long roundHalfCeiling(long instant) {
                return 0;
            }

            @Override
            public long roundHalfEven(long instant) {
                return 0;
            }

            @Override
            public long remainder(long instant) {
                return 0;
            }
        };

        try {
            FieldUtils.verifyValueBounds(field, 2025, 1900, 2100);
            fail("Expected IllegalFieldValueException for out of bounds value");
        } catch (IllegalFieldValueException e) {
            assertEquals("Field type", DateTimeFieldType.year(), e.getFieldType());
            assertEquals("Value", Integer.valueOf(2025), e.getValue());
            assertEquals("Lower bound", Integer.valueOf(1900), e.getLowerBound());
            assertEquals("Upper bound", Integer.valueOf(2100), e.getUpperBound());
        }
    }

    @Test(timeout = 4000)
    public void testGetWrappedValue_InvalidRange_ThrowsIllegalArgumentException() {
        try {
            FieldUtils.getWrappedValue(5, 10, 10);
            fail("Expected IllegalArgumentException for minValue >= maxValue");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception message", "MIN > MAX", e.getMessage());
        }

        try {
            FieldUtils.getWrappedValue(5, 11, 10);
            fail("Expected IllegalArgumentException for minValue > maxValue");
        } catch (IllegalArgumentException e) {
            assertEquals("Exception message", "MIN > MAX", e.getMessage());
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals_ContractIntegrity() {
        // Reflexive
        Object obj = new Object();
        assertTrue("Reflexive", FieldUtils.equals(obj, obj));

        // Symmetric
        String a = "test";
        String b = new String("test");
        assertTrue("Symmetric forward", FieldUtils.equals(a, b));
        assertTrue("Symmetric reverse", FieldUtils.equals(b, a));

        // Transitive
        String c = new String("test");
        assertTrue("Transitive a-b", FieldUtils.equals(a, b));
        assertTrue("Transitive b-c", FieldUtils.equals(b, c));
        assertTrue("Transitive a-c", FieldUtils.equals(a, c));

        // Consistent
        assertTrue("Consistent", FieldUtils.equals(a, b));
        assertTrue("Consistent again", FieldUtils.equals(a, b));

        // Null handling
        assertFalse("Null vs object", FieldUtils.equals(null, a));
        assertFalse("Object vs null", FieldUtils.equals(a, null));
        assertTrue("Null vs null", FieldUtils.equals(null, null));
    }

    @Test(timeout = 4000)
    public void testGetWrappedValue_WithCurrentValueAndWrapValue() {
        // Test the overloaded method getWrappedValue(currentValue, wrapValue, minValue, maxValue)
        assertEquals("Wrap with current and wrap", 5, FieldUtils.getWrappedValue(3, 2, 0, 10));
        assertEquals("Wrap with current and wrap overflow", 2, FieldUtils.getWrappedValue(10, 2, 0, 10));
        assertEquals("Wrap with current and wrap negative", 9, FieldUtils.getWrappedValue(0, -1, 0, 10));
        assertEquals("Wrap with current and wrap negative overflow", 8, FieldUtils.getWrappedValue(0, -2, 0, 10));
        assertEquals("Wrap with current and wrap exact", 0, FieldUtils.getWrappedValue(10, 0, 0, 10));
        assertEquals("Wrap with current and wrap min", 0, FieldUtils.getWrappedValue(0, 0, 0, 10));
        assertEquals("Wrap with current and wrap max", 10, FieldUtils.getWrappedValue(10, 0, 0, 10));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongInt_EdgeCases() {
        // Test with val2 = -1, 0, 1 special cases
        assertEquals("val2 = -1", -5L, FieldUtils.safeMultiply(5L, -1));
        assertEquals("val2 = 0", 0L, FieldUtils.safeMultiply(5L, 0));
        assertEquals("val2 = 1", 5L, FieldUtils.safeMultiply(5L, 1));
        
        // Test with val1 = 0
        assertEquals("val1 = 0", 0L, FieldUtils.safeMultiply(0L, 5));
        
        // Test with negative val1
        assertEquals("negative val1", -15L, FieldUtils.safeMultiply(-3L, 5));
        
        // Test with large values that don't overflow
        assertEquals("large values", 1000000000000L, FieldUtils.safeMultiply(1000000L, 1000000));
    }

    @Test(timeout = 4000)
    public void testSafeMultiply_LongLong_EdgeCases() {
        // Test with val2 = 1
        assertEquals("val2 = 1", 5L, FieldUtils.safeMultiply(5L, 1L));
        
        // Test with val1 = 1
        assertEquals("val1 = 1", 5L, FieldUtils.safeMultiply(1L, 5L));
        
        // Test with val1 = 0 or val2 = 0
        assertEquals("val1 = 0", 0L, FieldUtils.safeMultiply(0L, 5L));
        assertEquals("val2 = 0", 0L, FieldUtils.safeMultiply(5L, 0L));
        
        // Test with negative values
        assertEquals("negative values", 15L, FieldUtils.safeMultiply(-3L, -5L));
        assertEquals("mixed signs", -15L, FieldUtils.safeMultiply(-3L, 5L));
        
        // Test with large values that don't overflow
        assertEquals("large values", 1000000000000L, FieldUtils.safeMultiply(1000000L, 1000000L));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyToInt_EdgeCases() {
        assertEquals("Positive", 21, FieldUtils.safeMultiplyToInt(3L, 7L));
        assertEquals("Negative", -21, FieldUtils.safeMultiplyToInt(-3L, 7L));
        assertEquals("Zero", 0, FieldUtils.safeMultiplyToInt(0L, 7L));
        assertEquals("One", 7, FieldUtils.safeMultiplyToInt(1L, 7L));
        assertEquals("Minus one", -7, FieldUtils.safeMultiplyToInt(-1L, 7L));
        assertEquals("Max int", Integer.MAX_VALUE, FieldUtils.safeMultiplyToInt(1L, Integer.MAX_VALUE));
        assertEquals("Min int", Integer.MIN_VALUE, FieldUtils.safeMultiplyToInt(1L, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testVerifyValueBounds_EdgeCases() {
        // Test with equal bounds
        FieldUtils.verifyValueBounds("testField", 5, 5, 5);
        
        // Test with min = max = Integer.MIN_VALUE
        FieldUtils.verifyValueBounds("testField", Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        
        // Test with min = max = Integer.MAX_VALUE
        FieldUtils.verifyValueBounds("testField", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        
        // Test with full range
        FieldUtils.verifyValueBounds("testField", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        FieldUtils.verifyValueBounds("testField", Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        FieldUtils.verifyValueBounds("testField", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test(timeout = 4000)
    public void testGetWrappedValue_EdgeCases() {
        // Test with minValue = 0, maxValue = Integer.MAX_VALUE
        assertEquals("Wrap with large max", 5, FieldUtils.getWrappedValue(5, 0, Integer.MAX_VALUE));
        assertEquals("Wrap with large max overflow", 0, FieldUtils.getWrappedValue(Integer.MAX_VALUE + 1, 0, Integer.MAX_VALUE));
        
        // Test with negative minValue
        assertEquals("Wrap with negative min", -5, FieldUtils.getWrappedValue(-5, -10, 10));
        assertEquals("Wrap with negative min overflow", -10, FieldUtils.getWrappedValue(11, -10, 10));
        
        // Test with wrapRange = 1
        assertEquals("Wrap range 1", 0, FieldUtils.getWrappedValue(0, 0, 0));
        
        // Test with large wrap values
        assertEquals("Large wrap", 5, FieldUtils.getWrappedValue(1000000005, 0, 10));
        assertEquals("Large negative wrap", 5, FieldUtils.getWrappedValue(-1000000005, 0, 10));
    }

    @Test(timeout = 4000)
    public void testEquals_EdgeCases() {
        // Test with different types
        assertFalse("Different types", FieldUtils.equals("5", Integer.valueOf(5)));
        
        // Test with arrays (reference equality)
        int[] arr1 = {1, 2, 3};
        int[] arr2 = {1, 2, 3};
        assertFalse("Arrays not equal by value", FieldUtils.equals(arr1, arr2));
        assertTrue("Arrays equal by reference", FieldUtils.equals(arr1, arr1));
        
        // Test with custom equals implementation
        Object custom1 = new Object() {
            @Override
            public boolean equals(Object obj) {
                return obj != null;
            }
        };
        Object custom2 = new Object();
        assertTrue("Custom equals", FieldUtils.equals(custom1, custom2));
    }
}