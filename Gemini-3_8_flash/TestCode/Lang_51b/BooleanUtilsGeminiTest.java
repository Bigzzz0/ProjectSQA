/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.BooleanUtils
 * Defects4J Bug: Lang-39 / Lang-47 variant (StringIndexOutOfBoundsException in toBoolean(String)).
 *
 * Specific Defect Mechanism:
 * In `toBoolean(String str)`:
 *   switch (str.length()) {
 *       case 3:
 *           if (ch == 'y') ...
 *           if (ch == 'Y') ...
 *           // MISSING break or return when ch != 'y' and ch != 'Y' (e.g. str = "tru" or "TRU")
 *       case 4:
 *           if (ch == 't') return ... && (str.charAt(3) == 'e' ...); // Throws SIOOBE on index 3!
 *           if (ch == 'T') return ... && (str.charAt(3) == 'E' ...); // Throws SIOOBE on index 3!
 *   }
 * When a 3-character string starts with 't' or 'T' (e.g., "tru", "TRU", "tax"), case 3 falls through
 * into case 4, leading to charAt(3) which throws StringIndexOutOfBoundsException: String index out of range: 3.
 *
 * Matrix Coverage:
 * - Partition A: Core Functional Logic & State Transitions (Primitives, Objects, Int, String mappings)
 * - Partition B: Boundary Value Analysis (BVA) & Extremes (null inputs, empty strings/arrays, min/max int)
 * - Partition C: Defect-Targeted Branch Zone (3-char string starting with 't'/'T', fall-through tests)
 * - Partition D: Exception & Defensive Guard Paths (xor invalid arrays, toBoolean mismatch exceptions)
 * - Partition E: Object Lifecycle & Contract Integrity (public constructor instantiation)
 * ----------------------------------------------------------------------------------------------------
 */
public class BooleanUtilsGeminiTest {

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        BooleanUtils utils = new BooleanUtils();
        assertNotNull("Instance of BooleanUtils should not be null", utils);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNegate() {
        assertNull(BooleanUtils.negate(null));
        assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
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
    public void testToBooleanObject_boolean() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

    @Test(timeout = 4000)
    public void testToBoolean_Boolean() {
        assertTrue(BooleanUtils.toBoolean(Boolean.TRUE));
        assertFalse(BooleanUtils.toBoolean(Boolean.FALSE));
        assertFalse(BooleanUtils.toBoolean((Boolean) null));
    }

    @Test(timeout = 4000)
    public void testToBooleanDefaultIfNull() {
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, false));
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(null, false));
    }

    @Test(timeout = 4000)
    public void testToBoolean_int() {
        assertFalse(BooleanUtils.toBoolean(0));
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(-1));
        assertTrue(BooleanUtils.toBoolean(Integer.MAX_VALUE));
        assertTrue(BooleanUtils.toBoolean(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_int() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(-1));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_Integer() {
        assertNull(BooleanUtils.toBooleanObject((Integer) null));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(-1)));
    }

    @Test(timeout = 4000)
    public void testToBoolean_int_int_int() {
        assertTrue(BooleanUtils.toBoolean(1, 1, 2));
        assertFalse(BooleanUtils.toBoolean(2, 1, 2));
    }

    @Test(timeout = 4000)
    public void testToBoolean_Integer_Integer_Integer() {
        Integer i1 = Integer.valueOf(1);
        Integer i2 = Integer.valueOf(2);

        assertTrue(BooleanUtils.toBoolean(i1, i1, i2));
        assertFalse(BooleanUtils.toBoolean(i2, i1, i2));
        assertTrue(BooleanUtils.toBoolean((Integer) null, null, i2));
        assertFalse(BooleanUtils.toBoolean((Integer) null, i1, null));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_int_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 2, 3));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(2, 1, 2, 3));
        assertNull(BooleanUtils.toBooleanObject(3, 1, 2, 3));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_Integer_Integer_Integer_Integer() {
        Integer i1 = Integer.valueOf(1);
        Integer i2 = Integer.valueOf(2);
        Integer i3 = Integer.valueOf(3);

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(i1, i1, i2, i3));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(i2, i1, i2, i3));
        assertNull(BooleanUtils.toBooleanObject(i3, i1, i2, i3));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, i2, i3));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, i1, null, i3));
        assertNull(BooleanUtils.toBooleanObject(null, i1, i2, null));
    }

    @Test(timeout = 4000)
    public void testToInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObject_boolean() {
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(true));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(false));
    }

    @Test(timeout = 4000)
    public void testToIntegerObject_Boolean() {
        assertNull(BooleanUtils.toIntegerObject((Boolean) null));
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testToInteger_boolean_int_int() {
        assertEquals(10, BooleanUtils.toInteger(true, 10, 20));
        assertEquals(20, BooleanUtils.toInteger(false, 10, 20));
    }

    @Test(timeout = 4000)
    public void testToInteger_Boolean_int_int_int() {
        assertEquals(10, BooleanUtils.toInteger(Boolean.TRUE, 10, 20, 30));
        assertEquals(20, BooleanUtils.toInteger(Boolean.FALSE, 10, 20, 30));
        assertEquals(30, BooleanUtils.toInteger(null, 10, 20, 30));
    }

    @Test(timeout = 4000)
    public void testToIntegerObject_boolean_Integer_Integer() {
        Integer i1 = Integer.valueOf(10);
        Integer i2 = Integer.valueOf(20);
        assertEquals(i1, BooleanUtils.toIntegerObject(true, i1, i2));
        assertEquals(i2, BooleanUtils.toIntegerObject(false, i1, i2));
    }

    @Test(timeout = 4000)
    public void testToIntegerObject_Boolean_Integer_Integer_Integer() {
        Integer i1 = Integer.valueOf(10);
        Integer i2 = Integer.valueOf(20);
        Integer i3 = Integer.valueOf(30);
        assertEquals(i1, BooleanUtils.toIntegerObject(Boolean.TRUE, i1, i2, i3));
        assertEquals(i2, BooleanUtils.toIntegerObject(Boolean.FALSE, i1, i2, i3));
        assertEquals(i3, BooleanUtils.toIntegerObject(null, i1, i2, i3));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_String() {
        assertNull(BooleanUtils.toBooleanObject((String) null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("True"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("False"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("On"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("oFf"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yEs"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("No"));
        assertNull(BooleanUtils.toBooleanObject("blue"));
        assertNull(BooleanUtils.toBooleanObject(""));
    }

    @Test(timeout = 4000)
    public void testToBooleanObject_String_String_String_String() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("T", "T", "F", "N"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("F", "T", "F", "N"));
        assertNull(BooleanUtils.toBooleanObject("N", "T", "F", "N"));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, "F", "N"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, "T", null, "N"));
        assertNull(BooleanUtils.toBooleanObject(null, "T", "F", null));
    }

    @Test(timeout = 4000)
    public void testToBoolean_String_String_String() {
        assertTrue(BooleanUtils.toBoolean("Y", "Y", "N"));
        assertFalse(BooleanUtils.toBoolean("N", "Y", "N"));

        assertTrue(BooleanUtils.toBoolean((String) null, null, "N"));
        assertFalse(BooleanUtils.toBoolean((String) null, "Y", null));
    }

    @Test(timeout = 4000)
    public void testToStringTrueFalse_Boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
        assertNull(BooleanUtils.toStringTrueFalse(null));
    }

    @Test(timeout = 4000)
    public void testToStringOnOff_Boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        assertNull(BooleanUtils.toStringOnOff(null));
    }

    @Test(timeout = 4000)
    public void testToStringYesNo_Boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
        assertNull(BooleanUtils.toStringYesNo(null));
    }

    @Test(timeout = 4000)
    public void testToString_Boolean_String_String_String() {
        assertEquals("T", BooleanUtils.toString(Boolean.TRUE, "T", "F", "N"));
        assertEquals("F", BooleanUtils.toString(Boolean.FALSE, "T", "F", "N"));
        assertEquals("N", BooleanUtils.toString(null, "T", "F", "N"));
    }

    @Test(timeout = 4000)
    public void testToStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

    @Test(timeout = 4000)
    public void testToStringOnOff_boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

    @Test(timeout = 4000)
    public void testToStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

    @Test(timeout = 4000)
    public void testToString_boolean_String_String() {
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

    @Test(timeout = 4000)
    public void testXor_primitiveArray() {
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, true, true}));
    }

    @Test(timeout = 4000)
    public void testXor_BooleanArray() {
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.FALSE}));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Full Path Decision Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testToBoolean_String_ComprehensiveBranches() {
        // Interned "true" branch check
        assertTrue(BooleanUtils.toBoolean("true"));

        // Null check
        assertFalse(BooleanUtils.toBoolean((String) null));

        // Non-matching lengths
        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("a"));
        assertFalse(BooleanUtils.toBoolean("abcde"));

        // Length 2: on / ON / oN / On vs others
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("oN"));
        assertTrue(BooleanUtils.toBoolean("On"));
        assertFalse(BooleanUtils.toBoolean("ox"));
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("in"));
        assertFalse(BooleanUtils.toBoolean("NN"));

        // Length 3: yes / YES / various cases
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("yEs"));
        assertTrue(BooleanUtils.toBoolean("YeS"));
        assertTrue(BooleanUtils.toBoolean("yeS"));
        assertTrue(BooleanUtils.toBoolean("YEs"));
        assertTrue(BooleanUtils.toBoolean("yES"));
        assertTrue(BooleanUtils.toBoolean("Yes"));

        // Length 3: starts with 'y' or 'Y' but false suffix
        assertFalse(BooleanUtils.toBoolean("yep"));
        assertFalse(BooleanUtils.toBoolean("YEP"));
        assertFalse(BooleanUtils.toBoolean("yaS"));
        assertFalse(BooleanUtils.toBoolean("YAs"));

        // Length 4: true / TRUE / various cases
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("tRUE"));
        assertTrue(BooleanUtils.toBoolean("True"));
        assertTrue(BooleanUtils.toBoolean("tRUe"));
        assertTrue(BooleanUtils.toBoolean("TRUe"));
        assertTrue(BooleanUtils.toBoolean("TRue"));
        assertTrue(BooleanUtils.toBoolean("trUE"));
        assertTrue(BooleanUtils.toBoolean("truE"));

        // Length 4: starts with 't' or 'T' but false suffix
        assertFalse(BooleanUtils.toBoolean("tree"));
        assertFalse(BooleanUtils.toBoolean("TREE"));
        assertFalse(BooleanUtils.toBoolean("tram"));
        assertFalse(BooleanUtils.toBoolean("TRAM"));
        assertFalse(BooleanUtils.toBoolean("trip"));
        assertFalse(BooleanUtils.toBoolean("TRIP"));
        assertFalse(BooleanUtils.toBoolean("blue"));
        assertFalse(BooleanUtils.toBoolean("walk"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Targeting Defects4J Bug)
    // =========================================================================

    /**
     * TARGET DEFECT TEST:
     * Defects4J Lang bug triggers StringIndexOutOfBoundsException: String index out of range: 3
     * when a 3-character string starts with 't' or 'T' (e.g., "tru", "TRU", "tax", "The").
     * Due to missing break/return statements in case 3, execution falls through to case 4,
     * which attempts to inspect str.charAt(3) on a 3-character string!
     */
    @Test(timeout = 4000)
    public void testToBoolean_String_DefectTrigger_Length3StartsWithT() {
        assertFalse("3-character string 'tru' should return false without exception", BooleanUtils.toBoolean("tru"));
        assertFalse("3-character string 'TRU' should return false without exception", BooleanUtils.toBoolean("TRU"));
        assertFalse("3-character string 'tRU' should return false without exception", BooleanUtils.toBoolean("tRU"));
        assertFalse("3-character string 'TrU' should return false without exception", BooleanUtils.toBoolean("TrU"));
        assertFalse("3-character string 'the' should return false without exception", BooleanUtils.toBoolean("the"));
        assertFalse("3-character string 'TAX' should return false without exception", BooleanUtils.toBoolean("TAX"));
        assertFalse("3-character string 'foo' should return false without exception", BooleanUtils.toBoolean("foo"));
        assertFalse("3-character string 'bar' should return false without exception", BooleanUtils.toBoolean("bar"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBoolean_int_int_int_noMatch() {
        BooleanUtils.toBoolean(3, 1, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBoolean_Integer_Integer_Integer_noMatch() {
        BooleanUtils.toBoolean(Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBoolean_Integer_Integer_Integer_nullMismatch() {
        BooleanUtils.toBoolean(null, Integer.valueOf(1), Integer.valueOf(2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObject_int_int_int_int_noMatch() {
        BooleanUtils.toBooleanObject(4, 1, 2, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObject_Integer_Integer_Integer_Integer_noMatch() {
        BooleanUtils.toBooleanObject(Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObject_Integer_Integer_Integer_Integer_nullMismatch() {
        BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObject_String_4Args_noMatch() {
        BooleanUtils.toBooleanObject("M", "T", "F", "N");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBooleanObject_String_4Args_nullMismatch() {
        BooleanUtils.toBooleanObject(null, "T", "F", "N");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBoolean_String_3Args_noMatch() {
        BooleanUtils.toBoolean("M", "T", "F");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToBoolean_String_3Args_nullMismatch() {
        BooleanUtils.toBoolean(null, "T", "F");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXor_primitiveArray_null() {
        BooleanUtils.xor((boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXor_primitiveArray_empty() {
        BooleanUtils.xor(new boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXor_BooleanArray_null() {
        BooleanUtils.xor((Boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXor_BooleanArray_empty() {
        BooleanUtils.xor(new Boolean[]{});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testXor_BooleanArray_containsNull() {
        BooleanUtils.xor(new Boolean[]{Boolean.TRUE, null});
    }
}