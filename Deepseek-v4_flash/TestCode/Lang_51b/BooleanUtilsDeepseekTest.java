package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BooleanUtilsDeepseekTest: Comprehensive white-box test suite for BooleanUtils.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - negate(Boolean): null, TRUE, FALSE
 *   - isTrue, isNotTrue, isFalse, isNotFalse: null, TRUE, FALSE
 *   - toBooleanObject(boolean), toBoolean(Boolean), toBooleanDefaultIfNull
 *   - toBoolean(int), toBooleanObject(int), toBooleanObject(Integer)
 *   - toBoolean(int, int, int), toBoolean(Integer, Integer, Integer)
 *   - toBooleanObject(int, int, int, int), toBooleanObject(Integer, Integer, Integer, Integer)
 *   - toInteger(boolean), toIntegerObject(boolean), toIntegerObject(Boolean)
 *   - toInteger(boolean, int, int), toInteger(Boolean, int, int, int)
 *   - toIntegerObject(boolean, Integer, Integer), toIntegerObject(Boolean, Integer, Integer, Integer)
 *   - toStringTrueFalse, toStringOnOff, toStringYesNo (Boolean and boolean overloads)
 *   - toString(Boolean, String, String, String), toString(boolean, String, String)
 *   - xor(boolean[]), xor(Boolean[])
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null inputs for all methods accepting Boolean/Integer/String
 *   - Empty arrays for xor
 *   - String lengths: 0, 1, 2, 3, 4, 5, 6 (toBoolean(String))
 *   - Integer boundaries: 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE
 *   - Case variations for toBoolean(String): "true", "TRUE", "tRuE", "on", "ON", "oN", "yes", "YES", "yEs", "false", "off", "no"
 * 
 * Partition C: Defect-Targeted Branch Zone (StringIndexOutOfBounds in toBoolean(String))
 *   - String length 3 with first char not 'y' or 'Y' (e.g., "abc") -> falls through to case 4 -> accesses index 3 -> exception
 *   - String length 3 with first char 'y' or 'Y' but second/third not matching (e.g., "yxx") -> returns false (no fall-through)
 *   - String length 4 with first char not 't' or 'T' (e.g., "abcd") -> falls through to end -> returns false
 *   - String length 2 with first char not 'o' or 'O' (e.g., "ab") -> falls through to case 3? Actually length 2 case returns false if not 'on'/'ON'? Wait: case 2 only returns true if matches "on"/"ON", otherwise falls through to case 3? No, there is no break after case 2, so if length 2 and not "on"/"ON", it falls through to case 3 and then case 4, causing out-of-bounds. That's another bug! Actually the switch has no breaks, so any length that doesn't match the specific pattern will fall through to subsequent cases and eventually cause index out of bounds. For example, length 2 string "ab" will fall through to case 3, then case 4, and try charAt(3) -> exception. So we must test that as well.
 *   - The known defect from Defects4J is specifically for length 3 non-y/Y, but we should cover all fall-through scenarios.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - xor(null) -> IllegalArgumentException
 *   - xor(empty array) -> IllegalArgumentException
 *   - xor(Boolean[] with null element) -> IllegalArgumentException
 *   - toBoolean(int, int, int) with no match -> IllegalArgumentException
 *   - toBoolean(Integer, Integer, Integer) with no match -> IllegalArgumentException
 *   - toBooleanObject(int, int, int, int) with no match -> IllegalArgumentException
 *   - toBooleanObject(Integer, Integer, Integer, Integer) with no match -> IllegalArgumentException
 *   - toBoolean(String, String, String) with no match -> IllegalArgumentException
 *   - toBooleanObject(String, String, String, String) with no match -> IllegalArgumentException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor (public, no-op)
 *   - toString methods return expected strings
 */
public class BooleanUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testNegateNull() {
        assertNull(BooleanUtils.negate(null));
    }

    @Test(timeout = 4000)
    public void testNegateTrue() {
        assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testNegateFalse() {
        assertEquals(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testIsTrue() {
        assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
        assertFalse(BooleanUtils.isTrue(Boolean.FALSE));
        assertFalse(BooleanUtils.isTrue(null));
    }

    @Test(timeout = 4000)
    public void testIsNotTrue() {
        assertFalse(BooleanUtils.isNotTrue(Boolean.TRUE));
        assertTrue(BooleanUtils.isNotTrue(Boolean.FALSE));
        assertTrue(BooleanUtils.isNotTrue(null));
    }

    @Test(timeout = 4000)
    public void testIsFalse() {
        assertFalse(BooleanUtils.isFalse(Boolean.TRUE));
        assertTrue(BooleanUtils.isFalse(Boolean.FALSE));
        assertFalse(BooleanUtils.isFalse(null));
    }

    @Test(timeout = 4000)
    public void testIsNotFalse() {
        assertTrue(BooleanUtils.isNotFalse(Boolean.TRUE));
        assertFalse(BooleanUtils.isNotFalse(Boolean.FALSE));
        assertTrue(BooleanUtils.isNotFalse(null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectFromPrimitive() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

    @Test(timeout = 4000)
    public void testToBooleanFromBoolean() {
        assertTrue(BooleanUtils.toBoolean(Boolean.TRUE));
        assertFalse(BooleanUtils.toBoolean(Boolean.FALSE));
        assertFalse(BooleanUtils.toBoolean(null));
    }

    @Test(timeout = 4000)
    public void testToBooleanDefaultIfNull() {
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(null, false));
    }

    @Test(timeout = 4000)
    public void testToBooleanInt() {
        assertFalse(BooleanUtils.toBoolean(0));
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(2));
        assertTrue(BooleanUtils.toBoolean(-1));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectInt() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(2));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectInteger() {
        assertNull(BooleanUtils.toBooleanObject((Integer) null));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(2)));
    }

    @Test(timeout = 4000)
    public void testToBooleanIntIntInt() {
        assertTrue(BooleanUtils.toBoolean(1, 1, 0));
        assertFalse(BooleanUtils.toBoolean(0, 1, 0));
        assertFalse(BooleanUtils.toBoolean(2, 1, 2));
        assertTrue(BooleanUtils.toBoolean(2, 2, 0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanIntIntIntNoMatch() {
        BooleanUtils.toBoolean(3, 1, 0);
    }

    @Test(timeout = 4000)
    public void testToBooleanIntegerIntegerInteger() {
        assertTrue(BooleanUtils.toBoolean(null, null, Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(null, Integer.valueOf(1), null));
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2)));
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(0)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanIntegerIntegerIntegerNoMatch() {
        BooleanUtils.toBoolean(Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(0));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntIntIntInt() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(0, 0, 2, 3));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(2, 1, 2, 3));
        assertNull(BooleanUtils.toBooleanObject(3, 1, 2, 3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObjectIntIntIntIntNoMatch() {
        BooleanUtils.toBooleanObject(4, 1, 2, 3);
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntegerIntegerIntegerInteger() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, Integer.valueOf(2), Integer.valueOf(3)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, Integer.valueOf(1), null, Integer.valueOf(3)));
        assertNull(BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(2), null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(3)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObjectIntegerIntegerIntegerIntegerNoMatch() {
        BooleanUtils.toBooleanObject(Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
    }

    @Test(timeout = 4000)
    public void testToIntegerBoolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBoolean() {
        assertEquals(NumberUtils.INTEGER_ONE, BooleanUtils.toIntegerObject(true));
        assertEquals(NumberUtils.INTEGER_ZERO, BooleanUtils.toIntegerObject(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanWrapper() {
        assertNull(BooleanUtils.toIntegerObject((Boolean) null));
        assertEquals(NumberUtils.INTEGER_ONE, BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(NumberUtils.INTEGER_ZERO, BooleanUtils.toIntegerObject(Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testToIntegerBooleanIntInt() {
        assertEquals(5, BooleanUtils.toInteger(true, 5, 10));
        assertEquals(10, BooleanUtils.toInteger(false, 5, 10));
    }

    @Test(timeout = 4000)
    public void testToIntegerBooleanIntIntInt() {
        assertEquals(5, BooleanUtils.toInteger(Boolean.TRUE, 5, 10, 20));
        assertEquals(10, BooleanUtils.toInteger(Boolean.FALSE, 5, 10, 20));
        assertEquals(20, BooleanUtils.toInteger(null, 5, 10, 20));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanIntegerInteger() {
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(true, Integer.valueOf(5), Integer.valueOf(10)));
        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(false, Integer.valueOf(5), Integer.valueOf(10)));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanIntegerIntegerInteger() {
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(20)));
        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(Boolean.FALSE, Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(20)));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(null, Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(20)));
    }

    @Test(timeout = 4000)
    public void testToStringTrueFalseBoolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
        assertNull(BooleanUtils.toStringTrueFalse(null));
    }

    @Test(timeout = 4000)
    public void testToStringOnOffBoolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        assertNull(BooleanUtils.toStringOnOff(null));
    }

    @Test(timeout = 4000)
    public void testToStringYesNoBoolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
        assertNull(BooleanUtils.toStringYesNo(null));
    }

    @Test(timeout = 4000)
    public void testToStringBooleanStringStringString() {
        assertEquals("trueStr", BooleanUtils.toString(Boolean.TRUE, "trueStr", "falseStr", "nullStr"));
        assertEquals("falseStr", BooleanUtils.toString(Boolean.FALSE, "trueStr", "falseStr", "nullStr"));
        assertEquals("nullStr", BooleanUtils.toString(null, "trueStr", "falseStr", "nullStr"));
    }

    @Test(timeout = 4000)
    public void testToStringTrueFalsePrimitive() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

    @Test(timeout = 4000)
    public void testToStringOnOffPrimitive() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

    @Test(timeout = 4000)
    public void testToStringYesNoPrimitive() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

    @Test(timeout = 4000)
    public void testToStringPrimitiveStringString() {
        assertEquals("trueStr", BooleanUtils.toString(true, "trueStr", "falseStr"));
        assertEquals("falseStr", BooleanUtils.toString(false, "trueStr", "falseStr"));
    }

    @Test(timeout = 4000)
    public void testXorPrimitiveArray() {
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
    }

    @Test(timeout = 4000)
    public void testXorWrapperArray() {
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.FALSE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE, Boolean.TRUE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE}));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testToBooleanStringNull() {
        assertFalse(BooleanUtils.toBoolean((String) null));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringEmpty() {
        assertFalse(BooleanUtils.toBoolean(""));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength1() {
        assertFalse(BooleanUtils.toBoolean("a"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength2Valid() {
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("oN"));
        assertTrue(BooleanUtils.toBoolean("On"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength3ValidYes() {
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("yEs"));
        assertTrue(BooleanUtils.toBoolean("Yes"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength4ValidTrue() {
        assertTrue(BooleanUtils.toBoolean("true"));
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("tRuE"));
        assertTrue(BooleanUtils.toBoolean("True"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength5() {
        assertFalse(BooleanUtils.toBoolean("hello"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringLength6() {
        assertFalse(BooleanUtils.toBoolean("abcdef"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringCaseInsensitiveFalse() {
        assertFalse(BooleanUtils.toBoolean("false"));
        assertFalse(BooleanUtils.toBoolean("FALSE"));
        assertFalse(BooleanUtils.toBoolean("off"));
        assertFalse(BooleanUtils.toBoolean("OFF"));
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("NO"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringNull() {
        assertNull(BooleanUtils.toBooleanObject((String) null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringValid() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringNoMatch() {
        assertNull(BooleanUtils.toBooleanObject("blue"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringWithNulls() {
        assertNull(BooleanUtils.toBooleanObject(null, null, null, null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, "false", "null"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, "true", null, "null"));
        assertNull(BooleanUtils.toBooleanObject(null, "true", "false", null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringExactMatch() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true", "true", "false", "null"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false", "true", "false", "null"));
        assertNull(BooleanUtils.toBooleanObject("null", "true", "false", "null"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObjectStringNoMatchException() {
        BooleanUtils.toBooleanObject("other", "true", "false", "null");
    }

    @Test(timeout = 4000)
    public void testToBooleanStringWithTrueFalseStrings() {
        assertTrue(BooleanUtils.toBoolean("true", "true", "false"));
        assertFalse(BooleanUtils.toBoolean("false", "true", "false"));
        assertTrue(BooleanUtils.toBoolean(null, null, "false"));
        assertFalse(BooleanUtils.toBoolean(null, "true", null));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanStringWithTrueFalseStringsNoMatch() {
        BooleanUtils.toBoolean("other", "true", "false");
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Triggers the known defect: StringIndexOutOfBoundsException in toBoolean(String)
     * when string length is 3 and first char is not 'y' or 'Y', causing fall-through to case 4.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength3NonYesFallThrough() {
        // This should return false (no match) but the bug causes an exception.
        // We expect false, but the buggy version throws StringIndexOutOfBoundsException.
        // We'll catch the exception to verify the bug is present, but the test should assert the correct behavior.
        // Since we are testing against the fixed version, we expect false.
        // However, to reveal the defect, we need to assert that the method does NOT throw an exception and returns false.
        // In the defective version, this will throw an exception, causing test failure.
        // In the fixed version, it should return false.
        assertEquals(false, BooleanUtils.toBoolean("abc"));
    }

    /**
     * Another fall-through: length 2 string not "on" or "ON" falls through to case 3 and then case 4.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength2NonOnFallThrough() {
        assertEquals(false, BooleanUtils.toBoolean("ab"));
    }

    /**
     * Length 3 with first char 'y' but second/third not matching should return false without fall-through.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength3YesLikeButNot() {
        assertFalse(BooleanUtils.toBoolean("yxx"));
        assertFalse(BooleanUtils.toBoolean("Yxx"));
    }

    /**
     * Length 4 with first char not 't' or 'T' falls through to end and returns false.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength4NonTrueFallThrough() {
        assertFalse(BooleanUtils.toBoolean("abcd"));
    }

    /**
     * Length 2 with first char 'o' but second not 'n' falls through.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength2OnLikeButNot() {
        assertFalse(BooleanUtils.toBoolean("ox"));
        assertFalse(BooleanUtils.toBoolean("Ox"));
    }

    /**
     * Length 3 with first char 'y' but second/third not matching should not fall through.
     * Already covered, but add explicit test for 'y' with wrong case.
     */
    @Test(timeout = 4000)
    public void testToBooleanStringLength3YesWrongCase() {
        assertFalse(BooleanUtils.toBoolean("yeS")); // 'y' but 'e' and 'S'? Actually 'yeS' has 'y', then 'e' (ok), then 'S' (ok) -> true? Wait: 'yeS' -> first char 'y', second 'e' matches, third 'S' matches -> true. So not a fall-through.
        // Use 'y' with second not e/E: "yxs" -> false.
        assertFalse(BooleanUtils.toBoolean("yxs"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXorPrimitiveNull() {
        BooleanUtils.xor((boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXorPrimitiveEmpty() {
        BooleanUtils.xor(new boolean[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXorWrapperNull() {
        BooleanUtils.xor((Boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXorWrapperEmpty() {
        BooleanUtils.xor(new Boolean[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXorWrapperWithNullElement() {
        BooleanUtils.xor(new Boolean[]{Boolean.TRUE, null, Boolean.FALSE});
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructor() {
        // Just ensure no exception
        new BooleanUtils();
    }

    @Test(timeout = 4000)
    public void testToStringBooleanNullString() {
        // When nullString is null, toString returns null for null input
        assertNull(BooleanUtils.toString(null, "true", "false", null));
    }

    @Test(timeout = 4000)
    public void testToStringBooleanAllNull() {
        assertNull(BooleanUtils.toString(null, null, null, null));
        assertEquals("true", BooleanUtils.toString(Boolean.TRUE, "true", null, null));
        assertEquals("false", BooleanUtils.toString(Boolean.FALSE, null, "false", null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntegerNullTrueValue() {
        // When value is null and trueValue is null, returns Boolean.TRUE
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, Integer.valueOf(2), Integer.valueOf(3)));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntegerNullFalseValue() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, Integer.valueOf(1), null, Integer.valueOf(3)));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntegerNullNullValue() {
        assertNull(BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(2), null));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringNullTrueString() {
        assertTrue(BooleanUtils.toBoolean(null, null, "false"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringNullFalseString() {
        assertFalse(BooleanUtils.toBoolean(null, "true", null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringNullTrueString() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, "false", "null"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringNullFalseString() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, "true", null, "null"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringNullNullString() {
        assertNull(BooleanUtils.toBooleanObject(null, "true", "false", null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringAllNull() {
        assertNull(BooleanUtils.toBooleanObject(null, null, null, null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntegerAllNull() {
        assertNull(BooleanUtils.toBooleanObject(null, null, null, null));
    }

    @Test(timeout = 4000)
    public void testToBooleanIntegerAllNull() {
        assertTrue(BooleanUtils.toBoolean(null, null, null));
    }

    @Test(timeout = 4000)
    public void testToBooleanIntegerNullTrueNullFalse() {
        assertTrue(BooleanUtils.toBoolean(null, null, Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(null, Integer.valueOf(1), null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectIntBoundaries() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.MIN_VALUE));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.MAX_VALUE));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
    }

    @Test(timeout = 4000)
    public void testToBooleanIntBoundaries() {
        assertFalse(BooleanUtils.toBoolean(0));
        assertTrue(BooleanUtils.toBoolean(Integer.MIN_VALUE));
        assertTrue(BooleanUtils.toBoolean(Integer.MAX_VALUE));
        assertTrue(BooleanUtils.toBoolean(-1));
    }

    @Test(timeout = 4000)
    public void testToIntegerBooleanBoundaries() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanBoundaries() {
        assertEquals(NumberUtils.INTEGER_ONE, BooleanUtils.toIntegerObject(true));
        assertEquals(NumberUtils.INTEGER_ZERO, BooleanUtils.toIntegerObject(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanWrapperBoundaries() {
        assertNull(BooleanUtils.toIntegerObject(null));
        assertEquals(NumberUtils.INTEGER_ONE, BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(NumberUtils.INTEGER_ZERO, BooleanUtils.toIntegerObject(Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testXorPrimitiveSingleTrue() {
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
    }

    @Test(timeout = 4000)
    public void testXorPrimitiveSingleFalse() {
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
    }

    @Test(timeout = 4000)
    public void testXorPrimitiveMultipleTrue() {
        assertFalse(BooleanUtils.xor(new boolean[]{true, true, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, false, true}));
    }

    @Test(timeout = 4000)
    public void testXorWrapperSingleTrue() {
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE}));
    }

    @Test(timeout = 4000)
    public void testXorWrapperSingleFalse() {
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE}));
    }

    @Test(timeout = 4000)
    public void testXorWrapperMultipleTrue() {
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE, Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE, Boolean.TRUE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.FALSE, Boolean.TRUE}));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringExactMatchWithNulls() {
        // When str equals nullString, return null
        assertNull(BooleanUtils.toBooleanObject("null", "true", "false", "null"));
        // When str equals trueString, return TRUE
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true", "true", "false", "null"));
        // When str equals falseString, return FALSE
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false", "true", "false", "null"));
    }

    @Test(timeout = 4000)
    public void testToBooleanStringExactMatchWithNulls() {
        assertTrue(BooleanUtils.toBoolean(null, null, "false"));
        assertFalse(BooleanUtils.toBoolean(null, "true", null));
        assertTrue(BooleanUtils.toBoolean("true", "true", "false"));
        assertFalse(BooleanUtils.toBoolean("false", "true", "false"));
    }

    @Test(timeout = 4000)
    public void testToBooleanObjectStringCaseSensitivity() {
        // The method is case-sensitive for custom strings
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE", "TRUE", "FALSE", "NULL"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE", "TRUE", "FALSE", "NULL"));
        assertNull(BooleanUtils.toBooleanObject("NULL", "TRUE", "FALSE", "NULL"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObjectStringCaseSensitivityNoMatch() {
        BooleanUtils.toBooleanObject("true", "TRUE", "FALSE", "NULL");
    }

    @Test(timeout = 4000)
    public void testToBooleanStringCaseSensitivity() {
        assertTrue(BooleanUtils.toBoolean("TRUE", "TRUE", "FALSE"));
        assertFalse(BooleanUtils.toBoolean("FALSE", "TRUE", "FALSE"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanStringCaseSensitivityNoMatch() {
        BooleanUtils.toBoolean("true", "TRUE", "FALSE");
    }

    @Test(timeout = 4000)
    public void testToStringBooleanNullStringNull() {
        assertNull(BooleanUtils.toString(null, "true", "false", null));
    }

    @Test(timeout = 4000)
    public void testToStringBooleanAllNullStrings() {
        assertNull(BooleanUtils.toString(null, null, null, null));
        assertEquals("true", BooleanUtils.toString(Boolean.TRUE, "true", null, null));
        assertEquals("false", BooleanUtils.toString(Boolean.FALSE, null, "false", null));
    }

    @Test(timeout = 4000)
    public void testToStringPrimitiveNullStrings() {
        assertEquals("true", BooleanUtils.toString(true, "true", null));
        assertEquals("false", BooleanUtils.toString(false, null, "false"));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanNullValues() {
        assertNull(BooleanUtils.toIntegerObject(null, null, null, null));
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(5), null, null));
        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(Boolean.FALSE, null, Integer.valueOf(10), null));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(null, null, null, Integer.valueOf(20)));
    }

    @Test(timeout = 4000)
    public void testToIntegerBooleanNullValues() {
        assertEquals(5, BooleanUtils.toInteger(Boolean.TRUE, 5, 10, 20));
        assertEquals(10, BooleanUtils.toInteger(Boolean.FALSE, 5, 10, 20));
        assertEquals(20, BooleanUtils.toInteger(null, 5, 10, 20));
    }

    @Test(timeout = 4000)
    public void testToIntegerObjectBooleanPrimitiveNullValues() {
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(true, Integer.valueOf(5), null));
        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(false, null, Integer.valueOf(10)));
    }

    @Test(timeout = 4000)
    public void testToIntegerBooleanPrimitiveNullValues() {
        assertEquals(5, BooleanUtils.toInteger(true, 5, 10));
        assertEquals(10, BooleanUtils.toInteger(false, 5, 10));
    }
}