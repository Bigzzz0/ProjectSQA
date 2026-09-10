package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Time_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_partial_field_order_001() throws Exception {
        // Native IPO combination: field_order=day_year_month, values=ones, chronology=iso
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.dayOfMonth(), DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() };
        int[] values = new int[] { 1, 1, 1 };
        try {
            new Partial(types, values, ISOChronology.getInstanceUTC());
            fail("Expected invalid largest-to-smallest field order");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().indexOf("order") >= 0);
        }
    }

    @Test(timeout = 4000)
    public void test_partial_field_order_002() throws Exception {
        // Native IPO combination: field_order=day_year_month, values=mixed, chronology=gregorian
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.dayOfMonth(), DateTimeFieldType.year(), DateTimeFieldType.monthOfYear() };
        int[] values = new int[] { 2, 2000, 3 };
        try {
            new Partial(types, values, GregorianChronology.getInstanceUTC());
            fail("Expected invalid largest-to-smallest field order");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().indexOf("order") >= 0);
        }
    }

    @Test(timeout = 4000)
    public void test_partial_field_order_003() throws Exception {
        // Native IPO combination: field_order=year_day_era, values=ones, chronology=gregorian
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.era() };
        int[] values = new int[] { 1, 1, 1 };
        try {
            new Partial(types, values, GregorianChronology.getInstanceUTC());
            fail("Expected invalid largest-to-smallest field order");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().indexOf("order") >= 0);
        }
    }

    @Test(timeout = 4000)
    public void test_partial_field_order_004() throws Exception {
        // Native IPO combination: field_order=year_day_era, values=mixed, chronology=iso
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth(), DateTimeFieldType.era() };
        int[] values = new int[] { 2, 2000, 3 };
        try {
            new Partial(types, values, ISOChronology.getInstanceUTC());
            fail("Expected invalid largest-to-smallest field order");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().indexOf("order") >= 0);
        }
    }

}
