/*
 *  Copyright 2001-2013 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.IllegalFieldValueException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.field.FieldUtils
 *
 * Decision / Condition Branch Coverage:
 * 1. safeNegate(int):
 *    - Branch: value == Integer.MIN_VALUE (throw ArithmeticException)
 *    - Branch: value != Integer.MIN_VALUE (return -value)
 * 2. safeAdd(int, int):
 *    - Branch: ((val1 ^ sum) < 0 && (val1 ^ val2) >= 0) -> overflow path
 *    - Branch: positive + positive overflow (Integer.MAX_VALUE + 1)
 *    - Branch: negative + negative overflow (Integer.MIN_VALUE + -1)
 *    - Branch: opposite signs / no overflow
 * 3. safeAdd(long, long):
 *    - Branch: positive + positive overflow (Long.MAX_VALUE + 1L)
 *    - Branch: negative + negative overflow (Long.MIN_VALUE + -1L)
 *    - Branch: normal paths
 * 4. safeSubtract(long, long):
 *    - Branch: ((val1 ^ diff) < 0 && (val1 ^ val2) < 0) -> overflow
 *    - Branch: positive - negative overflow (Long.MAX_VALUE - (-1L))
 *    - Branch: negative - positive overflow (Long.MIN_VALUE - 1L)
 *    - Branch: normal paths
 * 5. safeMultiply(int, int):
 *    - Branch: total < Integer.MIN_VALUE || total > Integer.MAX_VALUE
 *    - Boundary: MAX_VALUE * 2, MIN_VALUE * 2, normal operands
 * 6. safeMultiply(long, int):
 *    - Switch cases: val2 == -1, 0, 1, and default
 *    - DEFECT-TARGET: case -1 with val1 == Long.MIN_VALUE fails to check overflow when returning -val1
 *      resulting in Long.MIN_VALUE instead of throwing ArithmeticException
 *    - Default case overflow: (total / val2 != val1)
 * 7. safeMultiply(long, long):
 *    - Branches: val2 == 1, val1 == 1, val1 == 0 || val2 == 0
 *    - Branches: (total / val2 != val1 || (val1 == Long.MIN_VALUE && val2 == -1) || ...)
 *    - Long.MIN_VALUE * -1L, Long.MIN_VALUE * 2L, Long.MAX_VALUE * Long.MAX_VALUE
 * 8. safeToInt(long):
 *    - Branch: Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE
 *    - Branch: value > Integer.MAX_VALUE, value < Integer.MIN_VALUE
 * 9. safeMultiplyToInt(long, long):
 *    - Branch: fits in int vs overflows long vs overflows int
 * 10. verifyValueBounds:
 *    - DateTimeField, DateTimeFieldType, and String variants
 *    - Branch: value < lowerBound -> throws IllegalFieldValueException
 *    - Branch: value > upperBound -> throws IllegalFieldValueException
 *    - Branch: lowerBound <= value <= upperBound -> pass
 * 11. getWrappedValue(int currentValue, int wrapValue, int min, int max):
 *    - Overload delegation to getWrappedValue(value, min, max)
 * 12. getWrappedValue(int value, int min, int max):
 *    - Branch: minValue >= maxValue -> throws IllegalArgumentException
 *    - Branch: value - minValue >= 0 -> modulo wrap
 *    - Branch: value - minValue < 0 and remByRange == 0 vs remByRange != 0
 * 13. equals(Object, Object):
 *    - Branch: object1 == object2 -> true
 *    - Branch: object1 == null || object2 == null -> false
 *    - Branch: object1.equals(object2) -> true / false
 * 14. Private Constructor:
 *    - Validate restricted instantiation contract.
 */
public class FieldUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSafeNegateNormalValues() {
        assertEquals(0, FieldUtils.safeNegate(0));
        assertEquals(-1, FieldUtils.safeNegate(1));
        assertEquals(1, FieldUtils.safeNegate(-1));
        assertEquals(-12345, FieldUtils.safeNegate(12345));
        assertEquals(12345, FieldUtils.safeNegate(-12345));
        assertEquals(-(Integer.MAX_VALUE), FieldUtils.safeNegate(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeNegate(-Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeAddIntNormalValues() {
        assertEquals(0, FieldUtils.safeAdd(0, 0));
        assertEquals(15, FieldUtils.safeAdd(7, 8));
        assertEquals(-1, FieldUtils.safeAdd(7, -8));
        assertEquals(-15, FieldUtils.safeAdd(-7, -8));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeAdd(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeAdd(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000)
    public void testSafeAddLongNormalValues() {
        assertEquals(0L, FieldUtils.safeAdd(0L, 0L));
        assertEquals(100L, FieldUtils.safeAdd(40L, 60L));
        assertEquals(-20L, FieldUtils.safeAdd(40L, -60L));
        assertEquals(-100L, FieldUtils.safeAdd(-40L, -60L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeAdd(Long.MAX_VALUE - 1L, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeAdd(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(timeout = 4000)
    public void testSafeSubtractLongNormalValues() {
        assertEquals(0L, FieldUtils.safeSubtract(0L, 0L));
        assertEquals(50L, FieldUtils.safeSubtract(100L, 50L));
        assertEquals(-50L, FieldUtils.safeSubtract(50L, 100L));
        assertEquals(150L, FieldUtils.safeSubtract(100L, -50L));
        assertEquals(-150L, FieldUtils.safeSubtract(-100L, 50L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeSubtract(Long.MAX_VALUE - 1L, -1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeSubtract(Long.MIN_VALUE + 1L, 1L));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyIntNormalValues() {
        assertEquals(0, FieldUtils.safeMultiply(0, 0));
        assertEquals(0, FieldUtils.safeMultiply(0, 100));
        assertEquals(0, FieldUtils.safeMultiply(100, 0));
        assertEquals(42, FieldUtils.safeMultiply(6, 7));
        assertEquals(-42, FieldUtils.safeMultiply(6, -7));
        assertEquals(-42, FieldUtils.safeMultiply(-6, 7));
        assertEquals(42, FieldUtils.safeMultiply(-6, -7));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, 1));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyLongIntNormalValues() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 0));
        assertEquals(0L, FieldUtils.safeMultiply(12345L, 0));
        assertEquals(12345L, FieldUtils.safeMultiply(12345L, 1));
        assertEquals(-12345L, FieldUtils.safeMultiply(12345L, -1));
        assertEquals(-12345L, FieldUtils.safeMultiply(-12345L, 1));
        assertEquals(12345L, FieldUtils.safeMultiply(-12345L, -1));
        assertEquals(20000000000L, FieldUtils.safeMultiply(10000000000L, 2));
        assertEquals(-20000000000L, FieldUtils.safeMultiply(10000000000L, -2));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyLongLongNormalValues() {
        assertEquals(0L, FieldUtils.safeMultiply(0L, 0L));
        assertEquals(0L, FieldUtils.safeMultiply(0L, 12345L));
        assertEquals(0L, FieldUtils.safeMultiply(12345L, 0L));
        assertEquals(12345L, FieldUtils.safeMultiply(12345L, 1L));
        assertEquals(12345L, FieldUtils.safeMultiply(1L, 12345L));
        assertEquals(-12345L, FieldUtils.safeMultiply(12345L, -1L));
        assertEquals(-12345L, FieldUtils.safeMultiply(-1L, 12345L));
        assertEquals(42L, FieldUtils.safeMultiply(6L, 7L));
        assertEquals(-42L, FieldUtils.safeMultiply(6L, -7L));
        assertEquals(42L, FieldUtils.safeMultiply(-6L, -7L));
    }

    @Test(timeout = 4000)
    public void testSafeToIntNormalValues() {
        assertEquals(0, FieldUtils.safeToInt(0L));
        assertEquals(12345, FieldUtils.safeToInt(12345L));
        assertEquals(-12345, FieldUtils.safeToInt(-12345L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeToInt((long) Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeToInt((long) Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSafeMultiplyToIntNormalValues() {
        assertEquals(0, FieldUtils.safeMultiplyToInt(0L, 0L));
        assertEquals(42, FieldUtils.safeMultiplyToInt(6L, 7L));
        assertEquals(-42, FieldUtils.safeMultiplyToInt(6L, -7L));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiplyToInt((long) Integer.MAX_VALUE, 1L));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiplyToInt((long) Integer.MIN_VALUE, 1L));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeNegateIntegerMinValue() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeAddIntOverflowPositive() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeAddIntOverflowNegative() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeAddLongOverflowPositive() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeAddLongOverflowNegative() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeSubtractLongOverflowPositive() {
        FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeSubtractLongOverflowNegative() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyIntOverflowPositive() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE / 2 + 1, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyIntOverflowNegative() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE / 2 - 1, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyIntOverflowMinByMinusOne() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongIntOverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE / 2L + 1L, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongIntOverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE / 2L - 1L, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongLongOverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE / 2L + 1L, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongLongOverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE / 2L - 1L, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongLongOverflowMinByMinusOneFirstArg() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyLongLongOverflowMinByMinusOneSecondArg() {
        FieldUtils.safeMultiply(-1L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeToIntOverflowPositive() {
        FieldUtils.safeToInt((long) Integer.MAX_VALUE + 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeToIntOverflowNegative() {
        FieldUtils.safeToInt((long) Integer.MIN_VALUE - 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyToIntOverflowAtMultiplication() {
        FieldUtils.safeMultiplyToInt(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSafeMultiplyToIntOverflowAtCast() {
        FieldUtils.safeMultiplyToInt((long) Integer.MAX_VALUE, 2L);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Defects4J Defect: TestFieldUtils::testSafeMultiplyLongInt
    // FieldUtils.safeMultiply(Long.MIN_VALUE, -1) in defective code returns Long.MIN_VALUE
    // because switch(val2) case -1 returned -val1 without checking for Long.MIN_VALUE.
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDefectSafeMultiplyLongIntMinValueNegation() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
    }

    // =========================================================================
    // Partition D: Verification and Wrapping Boundaries
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerifyValueBoundsStringValid() {
        FieldUtils.verifyValueBounds("month", 5, 1, 12);
        FieldUtils.verifyValueBounds("month", 1, 1, 12);
        FieldUtils.verifyValueBounds("month", 12, 1, 12);
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsStringBelowMin() {
        try {
            FieldUtils.verifyValueBounds("month", 0, 1, 12);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals("month", ex.getFieldName());
            assertEquals(Integer.valueOf(0), ex.getIllegalNumberValue());
            assertEquals(Integer.valueOf(1), ex.getLowerBound());
            assertEquals(Integer.valueOf(12), ex.getUpperBound());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsStringAboveMax() {
        try {
            FieldUtils.verifyValueBounds("month", 13, 1, 12);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals("month", ex.getFieldName());
            assertEquals(Integer.valueOf(13), ex.getIllegalNumberValue());
            assertEquals(Integer.valueOf(1), ex.getLowerBound());
            assertEquals(Integer.valueOf(12), ex.getUpperBound());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsDateTimeFieldTypeValid() {
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 15, 1, 31);
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 1, 1, 31);
        FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 31, 1, 31);
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsDateTimeFieldTypeBelowMin() {
        try {
            FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 0, 1, 31);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals(DateTimeFieldType.dayOfMonth(), ex.getDateTimeFieldType());
            assertEquals(Integer.valueOf(0), ex.getIllegalNumberValue());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsDateTimeFieldTypeAboveMax() {
        try {
            FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth(), 32, 1, 31);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals(DateTimeFieldType.dayOfMonth(), ex.getDateTimeFieldType());
            assertEquals(Integer.valueOf(32), ex.getIllegalNumberValue());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyValueBoundsDateTimeField() {
        MockDateTimeField mockField = new MockDateTimeField(DateTimeFieldType.hourOfDay());
        FieldUtils.verifyValueBounds((DateTimeField) mockField, 12, 0, 23);
        FieldUtils.verifyValueBounds((DateTimeField) mockField, 0, 0, 23);
        FieldUtils.verifyValueBounds((DateTimeField) mockField, 23, 0, 23);

        try {
            FieldUtils.verifyValueBounds((DateTimeField) mockField, -1, 0, 23);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals(DateTimeFieldType.hourOfDay(), ex.getDateTimeFieldType());
            assertEquals(Integer.valueOf(-1), ex.getIllegalNumberValue());
        }

        try {
            FieldUtils.verifyValueBounds((DateTimeField) mockField, 24, 0, 23);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException ex) {
            assertEquals(DateTimeFieldType.hourOfDay(), ex.getDateTimeFieldType());
            assertEquals(Integer.valueOf(24), ex.getIllegalNumberValue());
        }
    }

    @Test(timeout = 4000)
    public void testGetWrappedValueIntIntIntInt() {
        assertEquals(5, FieldUtils.getWrappedValue(2, 3, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(10, 1, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(1, -1, 1, 10));
    }

    @Test(timeout = 4000)
    public void testGetWrappedValueIntIntIntNormal() {
        assertEquals(5, FieldUtils.getWrappedValue(5, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(1, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(10, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(11, 1, 10));
        assertEquals(2, FieldUtils.getWrappedValue(12, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(21, 1, 10));
    }

    @Test(timeout = 4000)
    public void testGetWrappedValueIntIntIntNegative() {
        assertEquals(10, FieldUtils.getWrappedValue(0, 1, 10));
        assertEquals(9, FieldUtils.getWrappedValue(-1, 1, 10));
        assertEquals(1, FieldUtils.getWrappedValue(-9, 1, 10));
        assertEquals(10, FieldUtils.getWrappedValue(-10, 1, 10));
        assertEquals(9, FieldUtils.getWrappedValue(-11, 1, 10));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetWrappedValueMinEqualMax() {
        FieldUtils.getWrappedValue(5, 10, 10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetWrappedValueMinGreaterThanMax() {
        FieldUtils.getWrappedValue(5, 11, 10);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        assertTrue(FieldUtils.equals(null, null));
        assertFalse(FieldUtils.equals("A", null));
        assertFalse(FieldUtils.equals(null, "A"));
        assertTrue(FieldUtils.equals("ABC", "ABC"));
        assertTrue(FieldUtils.equals(new Integer(100), new Integer(100)));
        assertFalse(FieldUtils.equals("ABC", "XYZ"));
        assertFalse(FieldUtils.equals("100", new Integer(100)));

        Object obj = new Object();
        assertTrue(FieldUtils.equals(obj, obj));
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorContract() throws Exception {
        Constructor<FieldUtils> constructor = FieldUtils.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();
        assertNotNull("Instance should be successfully created via reflection", instance);
        assertTrue(instance instanceof FieldUtils);
    }

    // =========================================================================
    // Helper Mock Class for DateTimeField testing
    // =========================================================================

    private static class MockDateTimeField extends BaseDateTimeField {
        protected MockDateTimeField(DateTimeFieldType type) {
            super(type);
        }

        @Override
        public int get(long instant) {
            return 0;
        }

        @Override
        public long set(long instant, int value) {
            return instant;
        }

        @Override
        public org.joda.time.DurationField getDurationField() {
            return null;
        }

        @Override
        public org.joda.time.DurationField getRangeDurationField() {
            return null;
        }

        @Override
        public int getMinimumValue() {
            return 0;
        }

        @Override
        public int getMaximumValue() {
            return 100;
        }

        @Override
        public long roundFloor(long instant) {
            return instant;
        }

        @Override
        public boolean isLenient() {
            return false;
        }
    }
}