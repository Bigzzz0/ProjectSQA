package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MIO Algorithm Generated Test Suite (EvoSuite SBST)
 * Target Class: org.apache.commons.lang3.math.NumberUtils
 * Managed by Member 2 (Algorithm Lead 2 - MIO Specialist)
 */
public class NumberUtils_ESTest {

    @Test(timeout = 4000)
    public void test00() throws Throwable {
        Number number0 = NumberUtils.createNumber("0");
        assertNotNull(number0);
        assertEquals(0, number0.intValue());
    }

    @Test(timeout = 4000)
    public void test01() throws Throwable {
        Number number0 = NumberUtils.createNumber("123");
        assertNotNull(number0);
        assertEquals(123, number0.intValue());
    }

    @Test(timeout = 4000)
    public void test02() throws Throwable {
        Number number0 = NumberUtils.createNumber("-123");
        assertNotNull(number0);
        assertEquals(-123, number0.intValue());
    }

    @Test(timeout = 4000)
    public void test03() throws Throwable {
        Number number0 = NumberUtils.createNumber("1.5");
        assertNotNull(number0);
        assertEquals(1.5F, number0.floatValue(), 0.01F);
    }

    @Test(timeout = 4000)
    public void test04() throws Throwable {
        Number number0 = NumberUtils.createNumber("123456789012L");
        assertNotNull(number0);
        assertEquals(123456789012L, number0.longValue());
    }

    @Test(timeout = 4000)
    public void test05() throws Throwable {
        Number number0 = NumberUtils.createNumber("0x1A");
        assertNotNull(number0);
        assertEquals(26, number0.intValue());
    }

    @Test(timeout = 4000)
    public void test06() throws Throwable {
        Number number0 = NumberUtils.createNumber((String) null);
        assertNull(number0);
    }

    @Test(timeout = 4000)
    public void test07() throws Throwable {
        try {
            NumberUtils.createNumber("");
            fail("Expecting exception: NumberFormatException");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void test08() throws Throwable {
        try {
            NumberUtils.createNumber("   ");
            fail("Expecting exception: NumberFormatException");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void test09() throws Throwable {
        int max = NumberUtils.max(1, 5, 2);
        assertEquals(5, max);
    }

    @Test(timeout = 4000)
    public void test10() throws Throwable {
        int min = NumberUtils.min(10, 2, 8);
        assertEquals(2, min);
    }
}
