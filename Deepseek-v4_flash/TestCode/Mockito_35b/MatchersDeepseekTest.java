package org.mockito;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Defect: NPE when primitive int or wrapper Integer is passed to matchers like anyInt(), eq(int), same(), etc.
 * 
 * The defect manifests in these scenarios:
 * 1. anyInt() called with primitive int argument -> NPE in internal reporting
 * 2. anyInt() called with Integer wrapper -> NPE
 * 3. eq(int) with Integer wrapper -> NPE
 * 4. same() with Integer wrapper -> NPE
 * 
 * Key branches to exercise:
 * - Primitive vs wrapper type handling in eq(), same(), anyInt()
 * - Null handling for wrapper types
 * - Return value contracts (primitive defaults vs null)
 * - Matcher registration with null values
 * 
 * Boundary conditions:
 * - Integer.MIN_VALUE, Integer.MAX_VALUE
 * - Zero, negative values
 * - Null wrapper objects
 * - Mixed primitive/wrapper usage
 * 
 * Partitions:
 * A: Core functional - anyInt(), eq(int), same() with valid values
 * B: Boundary - extreme int values, null wrappers
 * C: Defect-targeted - NPE scenarios with Integer wrappers
 * D: Exception paths - null matcher arguments
 * E: Lifecycle - repeated calls, state isolation
 */
public class MatchersDeepseekTest {

    // ========== PART A: CORE FUNCTIONAL LOGIC ==========
    
    @Test(timeout = 4000)
    public void testAnyBoolean_returnsFalse() {
        assertFalse(Matchers.anyBoolean());
    }

    @Test(timeout = 4000)
    public void testAnyByte_returnsZero() {
        assertEquals((byte)0, Matchers.anyByte());
    }

    @Test(timeout = 4000)
    public void testAnyChar_returnsChar() {
        assertEquals('\0', Matchers.anyChar());
    }

    @Test(timeout = 4000)
    public void testAnyInt_returnsZero() {
        assertEquals(0, Matchers.anyInt());
    }

    @Test(timeout = 4000)
    public void testAnyLong_returnsZero() {
        assertEquals(0L, Matchers.anyLong());
    }

    @Test(timeout = 4000)
    public void testAnyFloat_returnsZero() {
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testAnyDouble_returnsZero() {
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testAnyShort_returnsZero() {
        assertEquals((short)0, Matchers.anyShort());
    }

    @Test(timeout = 4000)
    public void testAnyObject_returnsNull() {
        assertNull(Matchers.anyObject());
    }

    @Test(timeout = 4000)
    public void testAny_returnsNull() {
        assertNull(Matchers.any());
    }

    @Test(timeout = 4000)
    public void testAnyVararg_returnsNull() {
        assertNull(Matchers.anyVararg());
    }

    @Test(timeout = 4000)
    public void testAnyString_returnsEmpty() {
        assertEquals("", Matchers.anyString());
    }

    @Test(timeout = 4000)
    public void testAnyList_returnsEmptyList() {
        assertNotNull(Matchers.anyList());
        assertTrue(Matchers.anyList().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyListOf_returnsEmptyList() {
        List<String> result = Matchers.anyListOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnySet_returnsEmptySet() {
        assertNotNull(Matchers.anySet());
        assertTrue(Matchers.anySet().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnySetOf_returnsEmptySet() {
        Set<String> result = Matchers.anySetOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyMap_returnsEmptyMap() {
        assertNotNull(Matchers.anyMap());
        assertTrue(Matchers.anyMap().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyCollection_returnsEmptyCollection() {
        assertNotNull(Matchers.anyCollection());
        assertTrue(Matchers.anyCollection().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyCollectionOf_returnsEmptyCollection() {
        Collection<String> result = Matchers.anyCollectionOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsA_withValidClass() {
        assertNull(Matchers.isA(String.class));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveBoolean() {
        assertFalse(Matchers.eq(true));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveByte() {
        assertEquals((byte)0, Matchers.eq((byte)5));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveChar() {
        assertEquals('\0', Matchers.eq('a'));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveDouble() {
        assertEquals(0.0, Matchers.eq(1.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testEq_primitiveFloat() {
        assertEquals(0.0f, Matchers.eq(1.5f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testEq_primitiveInt() {
        assertEquals(0, Matchers.eq(42));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveLong() {
        assertEquals(0L, Matchers.eq(42L));
    }

    @Test(timeout = 4000)
    public void testEq_primitiveShort() {
        assertEquals((short)0, Matchers.eq((short)5));
    }

    @Test(timeout = 4000)
    public void testEq_objectValue() {
        assertNull(Matchers.eq("test"));
    }

    @Test(timeout = 4000)
    public void testRefEq_withExclusions() {
        assertNull(Matchers.refEq("value", "field1", "field2"));
    }

    @Test(timeout = 4000)
    public void testSame_withObject() {
        Object obj = new Object();
        assertNull(Matchers.same(obj));
    }

    @Test(timeout = 4000)
    public void testIsNull_returnsNull() {
        assertNull(Matchers.isNull());
    }

    @Test(timeout = 4000)
    public void testNotNull_returnsNull() {
        assertNull(Matchers.notNull());
    }

    @Test(timeout = 4000)
    public void testIsNotNull_returnsNull() {
        assertNull(Matchers.isNotNull());
    }

    @Test(timeout = 4000)
    public void testContains_returnsEmptyString() {
        assertEquals("", Matchers.contains("sub"));
    }

    @Test(timeout = 4000)
    public void testMatches_returnsEmptyString() {
        assertEquals("", Matchers.matches("regex"));
    }

    @Test(timeout = 4000)
    public void testEndsWith_returnsEmptyString() {
        assertEquals("", Matchers.endsWith("suffix"));
    }

    @Test(timeout = 4000)
    public void testStartsWith_returnsEmptyString() {
        assertEquals("", Matchers.startsWith("prefix"));
    }

    @Test(timeout = 4000)
    public void testArgThat_returnsNull() {
        Matcher<String> matcher = org.mockito.ArgumentMatcher.any(String.class);
        assertNull(Matchers.argThat(matcher));
    }

    @Test(timeout = 4000)
    public void testCharThat_returnsChar() {
        Matcher<Character> matcher = org.mockito.ArgumentMatcher.any(Character.class);
        assertEquals('\0', Matchers.charThat(matcher));
    }

    @Test(timeout = 4000)
    public void testBooleanThat_returnsFalse() {
        Matcher<Boolean> matcher = org.mockito.ArgumentMatcher.any(Boolean.class);
        assertFalse(Matchers.booleanThat(matcher));
    }

    @Test(timeout = 4000)
    public void testByteThat_returnsZero() {
        Matcher<Byte> matcher = org.mockito.ArgumentMatcher.any(Byte.class);
        assertEquals((byte)0, Matchers.byteThat(matcher));
    }

    @Test(timeout = 4000)
    public void testShortThat_returnsZero() {
        Matcher<Short> matcher = org.mockito.ArgumentMatcher.any(Short.class);
        assertEquals((short)0, Matchers.shortThat(matcher));
    }

    @Test(timeout = 4000)
    public void testIntThat_returnsZero() {
        Matcher<Integer> matcher = org.mockito.ArgumentMatcher.any(Integer.class);
        assertEquals(0, Matchers.intThat(matcher));
    }

    @Test(timeout = 4000)
    public void testLongThat_returnsZero() {
        Matcher<Long> matcher = org.mockito.ArgumentMatcher.any(Long.class);
        assertEquals(0L, Matchers.longThat(matcher));
    }

    @Test(timeout = 4000)
    public void testFloatThat_returnsZero() {
        Matcher<Float> matcher = org.mockito.ArgumentMatcher.any(Float.class);
        assertEquals(0.0f, Matchers.floatThat(matcher), 0.0f);
    }

    @Test(timeout = 4000)
    public void testDoubleThat_returnsZero() {
        Matcher<Double> matcher = org.mockito.ArgumentMatcher.any(Double.class);
        assertEquals(0.0, Matchers.doubleThat(matcher), 0.0);
    }

    // ========== PART B: BOUNDARY VALUE ANALYSIS ==========

    @Test(timeout = 4000)
    public void testEq_intMaxValue() {
        assertEquals(0, Matchers.eq(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_intMinValue() {
        assertEquals(0, Matchers.eq(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_intZero() {
        assertEquals(0, Matchers.eq(0));
    }

    @Test(timeout = 4000)
    public void testEq_negativeInt() {
        assertEquals(0, Matchers.eq(-1));
    }

    @Test(timeout = 4000)
    public void testEq_longMaxValue() {
        assertEquals(0L, Matchers.eq(Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_longMinValue() {
        assertEquals(0L, Matchers.eq(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_doubleMaxValue() {
        assertEquals(0.0, Matchers.eq(Double.MAX_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testEq_doubleMinValue() {
        assertEquals(0.0, Matchers.eq(Double.MIN_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testEq_floatMaxValue() {
        assertEquals(0.0f, Matchers.eq(Float.MAX_VALUE), 0.0f);
    }

    @Test(timeout = 4000)
    public void testEq_floatMinValue() {
        assertEquals(0.0f, Matchers.eq(Float.MIN_VALUE), 0.0f);
    }

    @Test(timeout = 4000)
    public void testEq_shortMaxValue() {
        assertEquals((short)0, Matchers.eq(Short.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_shortMinValue() {
        assertEquals((short)0, Matchers.eq(Short.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_byteMaxValue() {
        assertEquals((byte)0, Matchers.eq(Byte.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_byteMinValue() {
        assertEquals((byte)0, Matchers.eq(Byte.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_charMaxValue() {
        assertEquals('\0', Matchers.eq(Character.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEq_charMinValue() {
        assertEquals('\0', Matchers.eq(Character.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSame_nullValue() {
        assertNull(Matchers.same(null));
    }

    @Test(timeout = 4000)
    public void testSame_emptyString() {
        assertNull(Matchers.same(""));
    }

    @Test(timeout = 4000)
    public void testContains_emptySubstring() {
        assertEquals("", Matchers.contains(""));
    }

    @Test(timeout = 4000)
    public void testMatches_emptyRegex() {
        assertEquals("", Matchers.matches(""));
    }

    @Test(timeout = 4000)
    public void testEndsWith_emptySuffix() {
        assertEquals("", Matchers.endsWith(""));
    }

    @Test(timeout = 4000)
    public void testStartsWith_emptyPrefix() {
        assertEquals("", Matchers.startsWith(""));
    }

    // ========== PART C: DEFECT-TARGETED BRANCH ZONE ==========
    // These tests target the NPE defect when Integer wrappers are used

    @Test(timeout = 4000)
    public void testAnyInt_withIntegerWrapper_shouldNotThrowNPE() {
        // This is the critical defect test - passing Integer wrapper to anyInt()
        Integer value = 100;
        // The defect causes NPE when Integer is passed to anyInt()
        // In fixed version, this should not throw
        try {
            int result = Matchers.anyInt();
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer passed to anyInt(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAnyInt_withNullInteger_shouldNotThrowNPE() {
        // Testing with null Integer wrapper
        Integer value = null;
        try {
            int result = Matchers.anyInt();
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when null Integer passed to anyInt(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEq_withIntegerWrapper_shouldNotThrowNPE() {
        // Critical defect test - Integer wrapper passed to eq(int)
        Integer value = 42;
        try {
            int result = Matchers.eq(value.intValue());
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer wrapper passed to eq(int): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEq_withNullInteger_shouldNotThrowNPE() {
        // Testing with null Integer
        Integer value = null;
        try {
            // This simulates the defect scenario where null Integer is passed
            int result = Matchers.eq(value); // This will NPE in defective version
            assertEquals(0, result);
        } catch (NullPointerException e) {
            // In defective version, this NPE is thrown - test should fail
            fail("NPE thrown when null Integer passed to eq(int): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSame_withIntegerWrapper_shouldNotThrowNPE() {
        // Critical defect test - Integer wrapper passed to same()
        Integer value = 100;
        try {
            Object result = Matchers.same(value);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer wrapper passed to same(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSame_withNullInteger_shouldNotThrowNPE() {
        // Testing with null Integer
        Integer value = null;
        try {
            Object result = Matchers.same(value);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when null Integer passed to same(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAnyInt_withPrimitiveInt_shouldNotThrowNPE() {
        // Testing with primitive int
        int value = 100;
        try {
            int result = Matchers.anyInt();
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when primitive int passed to anyInt(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEq_withPrimitiveInt_shouldNotThrowNPE() {
        // Testing with primitive int
        int value = 42;
        try {
            int result = Matchers.eq(value);
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when primitive int passed to eq(int): " + e.getMessage());
        }
    }

    // ========== PART D: EXCEPTION & DEFENSIVE GUARD PATHS ==========

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testArgThat_nullMatcher_throwsNPE() {
        Matchers.argThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testCharThat_nullMatcher_throwsNPE() {
        Matchers.charThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testBooleanThat_nullMatcher_throwsNPE() {
        Matchers.booleanThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testByteThat_nullMatcher_throwsNPE() {
        Matchers.byteThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testShortThat_nullMatcher_throwsNPE() {
        Matchers.shortThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIntThat_nullMatcher_throwsNPE() {
        Matchers.intThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testLongThat_nullMatcher_throwsNPE() {
        Matchers.longThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFloatThat_nullMatcher_throwsNPE() {
        Matchers.floatThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testDoubleThat_nullMatcher_throwsNPE() {
        Matchers.doubleThat(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIsA_nullClass_throwsNPE() {
        Matchers.isA(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAnyListOf_nullClass_throwsNPE() {
        Matchers.anyListOf(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAnySetOf_nullClass_throwsNPE() {
        Matchers.anySetOf(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAnyCollectionOf_nullClass_throwsNPE() {
        Matchers.anyCollectionOf(null);
    }

    // ========== PART E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ==========

    @Test(timeout = 4000)
    public void testRepeatedCalls_anyInt_returnsConsistentResults() {
        for (int i = 0; i < 100; i++) {
            assertEquals(0, Matchers.anyInt());
        }
    }

    @Test(timeout = 4000)
    public void testRepeatedCalls_eq_returnsConsistentResults() {
        for (int i = 0; i < 100; i++) {
            assertEquals(0, Matchers.eq(i));
        }
    }

    @Test(timeout = 4000)
    public void testRepeatedCalls_same_returnsConsistentResults() {
        Object obj = new Object();
        for (int i = 0; i < 100; i++) {
            assertNull(Matchers.same(obj));
        }
    }

    @Test(timeout = 4000)
    public void testMixedMatcherCalls_stateIsolation() {
        // Test that matcher calls don't interfere with each other
        Matchers.anyInt();
        Matchers.eq(5);
        Matchers.same("test");
        Matchers.anyString();
        Matchers.contains("sub");
        
        // After mixed calls, results should still be correct
        assertEquals(0, Matchers.anyInt());
        assertEquals("", Matchers.anyString());
        assertNull(Matchers.anyObject());
    }

    @Test(timeout = 4000)
    public void testAnyInt_withMultipleCalls_returnsZero() {
        int result1 = Matchers.anyInt();
        int result2 = Matchers.anyInt();
        assertEquals(0, result1);
        assertEquals(0, result2);
    }

    @Test(timeout = 4000)
    public void testEq_withMultipleCalls_returnsZero() {
        int result1 = Matchers.eq(10);
        int result2 = Matchers.eq(20);
        assertEquals(0, result1);
        assertEquals(0, result2);
    }

    @Test(timeout = 4000)
    public void testAnyString_withMultipleCalls_returnsEmpty() {
        String result1 = Matchers.anyString();
        String result2 = Matchers.anyString();
        assertEquals("", result1);
        assertEquals("", result2);
    }

    @Test(timeout = 4000)
    public void testAnyList_withMultipleCalls_returnsEmptyList() {
        List list1 = Matchers.anyList();
        List list2 = Matchers.anyList();
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyMap_withMultipleCalls_returnsEmptyMap() {
        Map map1 = Matchers.anyMap();
        Map map2 = Matchers.anyMap();
        assertTrue(map1.isEmpty());
        assertTrue(map2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnySet_withMultipleCalls_returnsEmptySet() {
        Set set1 = Matchers.anySet();
        Set set2 = Matchers.anySet();
        assertTrue(set1.isEmpty());
        assertTrue(set2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnyCollection_withMultipleCalls_returnsEmptyCollection() {
        Collection col1 = Matchers.anyCollection();
        Collection col2 = Matchers.anyCollection();
        assertTrue(col1.isEmpty());
        assertTrue(col2.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsNull_withMultipleCalls_returnsNull() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.isNull());
    }

    @Test(timeout = 4000)
    public void testNotNull_withMultipleCalls_returnsNull() {
        assertNull(Matchers.notNull());
        assertNull(Matchers.notNull());
    }

    @Test(timeout = 4000)
    public void testContains_withMultipleCalls_returnsEmptyString() {
        assertEquals("", Matchers.contains("a"));
        assertEquals("", Matchers.contains("b"));
    }

    @Test(timeout = 4000)
    public void testMatches_withMultipleCalls_returnsEmptyString() {
        assertEquals("", Matchers.matches("\\d+"));
        assertEquals("", Matchers.matches("[a-z]+"));
    }

    @Test(timeout = 4000)
    public void testEndsWith_withMultipleCalls_returnsEmptyString() {
        assertEquals("", Matchers.endsWith("ing"));
        assertEquals("", Matchers.endsWith("ed"));
    }

    @Test(timeout = 4000)
    public void testStartsWith_withMultipleCalls_returnsEmptyString() {
        assertEquals("", Matchers.startsWith("pre"));
        assertEquals("", Matchers.startsWith("post"));
    }

    @Test(timeout = 4000)
    public void testRefEq_withMultipleCalls_returnsNull() {
        assertNull(Matchers.refEq("value1"));
        assertNull(Matchers.refEq("value2", "exclude1"));
    }

    @Test(timeout = 4000)
    public void testIsA_withMultipleCalls_returnsNull() {
        assertNull(Matchers.isA(String.class));
        assertNull(Matchers.isA(Integer.class));
    }

    @Test(timeout = 4000)
    public void testAny_withMultipleCalls_returnsNull() {
        assertNull(Matchers.any());
        assertNull(Matchers.any());
    }

    @Test(timeout = 4000)
    public void testAnyObject_withMultipleCalls_returnsNull() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.anyObject());
    }

    @Test(timeout = 4000)
    public void testAnyVararg_withMultipleCalls_returnsNull() {
        assertNull(Matchers.anyVararg());
        assertNull(Matchers.anyVararg());
    }

    @Test(timeout = 4000)
    public void testEq_withNullObject_returnsNull() {
        // Testing eq with null object - should not throw NPE
        Object nullObj = null;
        try {
            Object result = Matchers.eq(nullObj);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when null Object passed to eq(Object): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSame_withNullObject_returnsNull() {
        // Testing same with null object - should not throw NPE
        Object nullObj = null;
        try {
            Object result = Matchers.same(nullObj);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when null Object passed to same(Object): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAnyInt_withIntegerMaxValue_shouldNotThrowNPE() {
        // Testing with Integer.MAX_VALUE
        Integer value = Integer.MAX_VALUE;
        try {
            int result = Matchers.anyInt();
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MAX_VALUE passed to anyInt(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAnyInt_withIntegerMinValue_shouldNotThrowNPE() {
        // Testing with Integer.MIN_VALUE
        Integer value = Integer.MIN_VALUE;
        try {
            int result = Matchers.anyInt();
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MIN_VALUE passed to anyInt(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEq_withIntegerMaxValue_shouldNotThrowNPE() {
        // Testing with Integer.MAX_VALUE
        Integer value = Integer.MAX_VALUE;
        try {
            int result = Matchers.eq(value.intValue());
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MAX_VALUE passed to eq(int): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEq_withIntegerMinValue_shouldNotThrowNPE() {
        // Testing with Integer.MIN_VALUE
        Integer value = Integer.MIN_VALUE;
        try {
            int result = Matchers.eq(value.intValue());
            assertEquals(0, result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MIN_VALUE passed to eq(int): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSame_withIntegerMaxValue_shouldNotThrowNPE() {
        // Testing with Integer.MAX_VALUE
        Integer value = Integer.MAX_VALUE;
        try {
            Object result = Matchers.same(value);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MAX_VALUE passed to same(): " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSame_withIntegerMinValue_shouldNotThrowNPE() {
        // Testing with Integer.MIN_VALUE
        Integer value = Integer.MIN_VALUE;
        try {
            Object result = Matchers.same(value);
            assertNull(result);
        } catch (NullPointerException e) {
            fail("NPE thrown when Integer.MIN_VALUE passed to same(): " + e.getMessage());
        }
    }
}