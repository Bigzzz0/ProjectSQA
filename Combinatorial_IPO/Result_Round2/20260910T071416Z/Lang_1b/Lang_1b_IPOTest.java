package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Lang_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_001() throws Exception {
        // Native IPO combination: prefix=lower_0x, leading_zeroes=none, magnitude=int_max
        String text = "0x" + "" + "7FFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_002() throws Exception {
        // Native IPO combination: prefix=lower_0x, leading_zeroes=two, magnitude=int_overflow
        String text = "0x" + "00" + "80000000";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_003() throws Exception {
        // Native IPO combination: prefix=upper_0x, leading_zeroes=none, magnitude=int_overflow
        String text = "0X" + "" + "80000000";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_004() throws Exception {
        // Native IPO combination: prefix=upper_0x, leading_zeroes=two, magnitude=int_max
        String text = "0X" + "00" + "7FFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_005() throws Exception {
        // Native IPO combination: prefix=hash, leading_zeroes=none, magnitude=unsigned_int
        String text = "#" + "" + "FFFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_006() throws Exception {
        // Native IPO combination: prefix=hash, leading_zeroes=two, magnitude=int_max
        String text = "#" + "00" + "7FFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_007() throws Exception {
        // Native IPO combination: prefix=lower_0x, leading_zeroes=two, magnitude=unsigned_int
        String text = "0x" + "00" + "FFFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_008() throws Exception {
        // Native IPO combination: prefix=upper_0x, leading_zeroes=none, magnitude=unsigned_int
        String text = "0X" + "" + "FFFFFFFF";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_009() throws Exception {
        // Native IPO combination: prefix=hash, leading_zeroes=none, magnitude=int_overflow
        String text = "#" + "" + "80000000";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

    @Test(timeout = 4000)
    public void test_hexadecimal_boundaries_010() throws Exception {
        // Native IPO combination: prefix=lower_0x, leading_zeroes=none, magnitude=int_overflow
        String text = "0x" + "" + "80000000";
        Number actual = NumberUtils.createNumber(text);
        String hex = text.startsWith("#") ? text.substring(1) : text.substring(2);
        assertEquals(new BigInteger(hex, 16), new BigInteger(String.valueOf(actual)));
    }

}
