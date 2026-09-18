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
package org.joda.time.base;

import static org.junit.Assert.*;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.MutablePeriod;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.YearMonth;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: BasePeriod.java (Defects4J Time-22)
 *
 * Decision / Condition Coverage Targets:
 * 1. BasePeriod(8 ints, PeriodType):
 *    - type == null vs type != null (checkPeriodType)
 *    - supported vs unsupported non-zero fields (checkAndUpdate: index == -1)
 * 2. BasePeriod(long, long, PeriodType, Chronology):
 *    - type/chrono null vs non-null
 * 3. BasePeriod(ReadableInstant, ReadableInstant, PeriodType):
 *    - start == null && end == null (initializes zero-values array)
 *    - start != null && end != null
 *    - start == null ^ end != null (one null, one non-null)
 * 4. BasePeriod(ReadablePartial, ReadablePartial, PeriodType):
 *    - start == null || end == null -> IllegalArgumentException
 *    - start & end instanceof BaseLocal && same class (fast-path LocalDate, LocalTime)
 *    - partial size mismatch (start.size() != end.size()) -> IllegalArgumentException
 *    - partial field type mismatch (start.getFieldType(i) != end.getFieldType(i)) -> IllegalArgumentException
 *    - contiguous vs non-contiguous partials (DateTimeUtils.isContiguous) -> IllegalArgumentException
 * 5. BasePeriod(ReadableInstant, ReadableDuration, PeriodType) and (ReadableDuration, ReadableInstant, PeriodType):
 *    - start/end null vs non-null, duration null vs non-null
 * 6. BasePeriod(long duration):
 *    - DEFECT ZONE (Time-22 / Bug 3264409):
 *      When DateTimeZone is fixed (e.g. +01:00), default ISOChronology has precise week/day fields.
 *      Calling this(duration, null, null) erroneously populates weeks/days with standard PeriodType,
 *      causing expected:<0> but was:<64> on getWeeks().
 * 7. BasePeriod(Object, PeriodType, Chronology):
 *    - this instanceof ReadWritablePeriod (MutablePeriod) vs not (Period/Mock)
 *    - type == null vs type != null
 * 8. Internal modification routines (setField, addField, mergePeriod, addPeriod, setValue, setValues):
 *    - index == -1 && value != 0 -> IllegalArgumentException
 *    - index == -1 && value == 0 -> ignored / no-op
 *    - index == -1 && field == null -> IllegalArgumentException
 *    - safeAdd overflow in addField / addPeriod -> ArithmeticException
 *    - null period in setPeriod / mergePeriod / addPeriod (branch safety)
 *    - setValue / getValue index bounds checks
 */
public class BasePeriodGeminiTest {

    /**
     * Concrete harness to expose BasePeriod's protected constructors and methods.
     */
    private static class ConcreteBasePeriod extends BasePeriod {
        private static final long serialVersionUID = 1L;

        ConcreteBasePeriod(int y, int m, int w, int d, int h, int min, int s, int ms, PeriodType type) {
            super(y, m, w, d, h, min, s, ms, type);
        }

        ConcreteBasePeriod(long start, long end, PeriodType type, Chronology chrono) {
            super(start, end, type, chrono);
        }

        ConcreteBasePeriod(ReadableInstant start, ReadableInstant end, PeriodType type) {
            super(start, end, type);
        }

        ConcreteBasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type) {
            super(start, end, type);
        }

        ConcreteBasePeriod(ReadableInstant start, ReadableDuration dur, PeriodType type) {
            super(start, dur, type);
        }

        ConcreteBasePeriod(ReadableDuration dur, ReadableInstant end, PeriodType type) {
            super(dur, end, type);
        }

        ConcreteBasePeriod(long dur) {
            super(dur);
        }

        ConcreteBasePeriod(long dur, PeriodType type, Chronology chrono) {
            super(dur, type, chrono);
        }

        ConcreteBasePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }

        ConcreteBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }

        public void testSetField(DurationFieldType field, int value) {
            setField(field, value);
        }

        public void testSetFieldInto(int[] values, DurationFieldType field, int value) {
            setFieldInto(values, field, value);
        }

        public void testAddField(DurationFieldType field, int value) {
            addField(field, value);
        }

        public void testAddFieldInto(int[] values, DurationFieldType field, int value) {
            addFieldInto(values, field, value);
        }

        public void testMergePeriod(ReadablePeriod period) {
            mergePeriod(period);
        }

        public int[] testMergePeriodInto(int[] values, ReadablePeriod period) {
            return mergePeriodInto(values, period);
        }

        public void testAddPeriod(ReadablePeriod period) {
            addPeriod(period);
        }

        public int[] testAddPeriodInto(int[] values, ReadablePeriod period) {
            return addPeriodInto(values, period);
        }

        public void testSetValue(int index, int value) {
            setValue(index, value);
        }

        public void testSetValues(int[] values) {
            setValues(values);
        }

        public void testSetPeriod(ReadablePeriod period) {
            setPeriod(period);
        }

        public void testSetPeriod(int y, int m, int w, int d, int h, int min, int s, int ms) {
            setPeriod(y, m, w, d, h, min, s, ms);
        }

        public PeriodType testCheckPeriodType(PeriodType type) {
            return checkPeriodType(type);
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullFieldConstructorAndGetters() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(8, p.size());
        assertEquals(1, p.getValue(0));
        assertEquals(2, p.getValue(1));
        assertEquals(3, p.getValue(2));
        assertEquals(4, p.getValue(3));
        assertEquals(5, p.getValue(4));
        assertEquals(6, p.getValue(5));
        assertEquals(7, p.getValue(6));
        assertEquals(8, p.getValue(7));
        assertEquals(DurationFieldType.years(), p.getFieldType(0));
        assertEquals(DurationFieldType.millis(), p.getFieldType(7));
    }

    @Test(timeout = 4000)
    public void testTrustedConstructor() {
        int[] vals = new int[] { 10, 20, 30, 40, 50, 60, 70, 80 };
        ConcreteBasePeriod p = new ConcreteBasePeriod(vals, PeriodType.standard());
        assertSame(vals, p.getValues());
        assertEquals(10, p.getValue(0));
        assertEquals(80, p.getValue(7));
    }

    @Test(timeout = 4000)
    public void testInstantsConstructor() {
        Instant start = new Instant(100000L);
        Instant end = new Instant(250000L);
        ConcreteBasePeriod p = new ConcreteBasePeriod(start, end, PeriodType.standard());
        assertEquals(150, p.getValue(p.indexOf(DurationFieldType.seconds())));
    }

    @Test(timeout = 4000)
    public void testPartialsFastPathBaseLocal() {
        LocalDate start = new LocalDate(2010, 1, 1);
        LocalDate end = new LocalDate(2012, 3, 15);
        ConcreteBasePeriod p = new ConcreteBasePeriod(start, end, PeriodType.yearMonthDayTime());
        assertEquals(2, p.getValue(p.indexOf(DurationFieldType.years())));
        assertEquals(2, p.getValue(p.indexOf(DurationFieldType.months())));
        assertEquals(14, p.getValue(p.indexOf(DurationFieldType.days())));
    }

    @Test(timeout = 4000)
    public void testPartialsContiguousFallback() {
        Partial start = new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour() },
            new int[] { 10, 20 }
        );
        Partial end = new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.hourOfDay(), DateTimeFieldType.minuteOfHour() },
            new int[] { 12, 50 }
        );
        ConcreteBasePeriod p = new ConcreteBasePeriod(start, end, PeriodType.time());
        assertEquals(2, p.getValue(p.indexOf(DurationFieldType.hours())));
        assertEquals(30, p.getValue(p.indexOf(DurationFieldType.minutes())));
    }

    @Test(timeout = 4000)
    public void testInstantAndDurationConstructors() {
        Instant start = new Instant(1000L);
        Duration dur = new Duration(5000L);
        ConcreteBasePeriod p1 = new ConcreteBasePeriod(start, dur, PeriodType.standard());
        assertEquals(5, p1.getValue(p1.indexOf(DurationFieldType.seconds())));

        Instant end = new Instant(6000L);
        ConcreteBasePeriod p2 = new ConcreteBasePeriod(dur, end, PeriodType.standard());
        assertEquals(5, p2.getValue(p2.indexOf(DurationFieldType.seconds())));
    }

    @Test(timeout = 4000)
    public void testToDurationFromAndTo() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        Instant instant = new Instant(10000L);
        Duration durFrom = p.toDurationFrom(instant);
        assertEquals(3600000L, durFrom.getMillis());

        Duration durTo = p.toDurationTo(instant);
        assertEquals(3600000L, durTo.getMillis());
    }

    @Test(timeout = 4000)
    public void testToDurationWithNullInstants() {
        DateTimeUtils.setCurrentMillisFixed(50000000L);
        try {
            ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 10, 0, 0, PeriodType.standard());
            Duration durFrom = p.toDurationFrom(null);
            assertEquals(600000L, durFrom.getMillis());

            Duration durTo = p.toDurationTo(null);
            assertEquals(600000L, durTo.getMillis());
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test(timeout = 4000)
    public void testSetFieldAndAddFieldOperations() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.testSetField(DurationFieldType.hours(), 5);
        assertEquals(5, p.getValue(p.indexOf(DurationFieldType.hours())));

        p.testAddField(DurationFieldType.hours(), 3);
        assertEquals(8, p.getValue(p.indexOf(DurationFieldType.hours())));

        p.testAddField(DurationFieldType.hours(), -2);
        assertEquals(6, p.getValue(p.indexOf(DurationFieldType.hours())));

        int[] arr = new int[p.size()];
        p.testSetFieldInto(arr, DurationFieldType.minutes(), 45);
        assertEquals(45, arr[p.indexOf(DurationFieldType.minutes())]);

        p.testAddFieldInto(arr, DurationFieldType.minutes(), 10);
        assertEquals(55, arr[p.indexOf(DurationFieldType.minutes())]);
    }

    @Test(timeout = 4000)
    public void testSetPeriodAndMergePeriodOperations() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(1, 1, 1, 1, 1, 1, 1, 1, PeriodType.standard());
        Period toMerge = new Period(0, 0, 0, 0, 5, 0, 0, 0, PeriodType.standard());
        p.testMergePeriod(toMerge);
        assertEquals(5, p.getValue(p.indexOf(DurationFieldType.hours())));

        Period toAdd = new Period(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard());
        p.testAddPeriod(toAdd);
        assertEquals(7, p.getValue(p.indexOf(DurationFieldType.hours())));

        p.testSetPeriod(2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(2, p.getValue(0));
        assertEquals(9, p.getValue(7));

        p.testSetPeriod((ReadablePeriod) null);
        for (int i = 0; i < p.size(); i++) {
            assertEquals(0, p.getValue(i));
        }

        p.testSetPeriod(new Period(10, 20, 0, 0, 0, 0, 0, 0));
        assertEquals(10, p.getValue(0));
        assertEquals(20, p.getValue(1));
    }

    @Test(timeout = 4000)
    public void testSetValueAndSetValues() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.testSetValue(4, 99);
        assertEquals(99, p.getValue(4));

        int[] newVals = new int[] { 8, 7, 6, 5, 4, 3, 2, 1 };
        p.testSetValues(newVals);
        assertSame(newVals, p.getValues());
        assertEquals(8, p.getValue(0));
        assertEquals(1, p.getValue(7));
    }

    @Test(timeout = 4000)
    public void testConverterConstructorReadWritableBranch() {
        MutablePeriod mp = new MutablePeriod("P1Y2M3DT4H");
        assertEquals(1, mp.getYears());
        assertEquals(2, mp.getMonths());
        assertEquals(3, mp.getDays());
        assertEquals(4, mp.getHours());
    }

    @Test(timeout = 4000)
    public void testConverterConstructorNonReadWritableBranch() {
        ConcreteBasePeriod p = new ConcreteBasePeriod("P1Y2M3DT4H", PeriodType.standard(), null);
        assertEquals(1, p.getValue(p.indexOf(DurationFieldType.years())));
        assertEquals(2, p.getValue(p.indexOf(DurationFieldType.months())));
        assertEquals(3, p.getValue(p.indexOf(DurationFieldType.days())));
        assertEquals(4, p.getValue(p.indexOf(DurationFieldType.hours())));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Null / Empty Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullPeriodTypeResolvesToStandard() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, null);
        assertEquals(PeriodType.standard(), p.getPeriodType());
        assertEquals(PeriodType.standard(), p.testCheckPeriodType(null));
    }

    @Test(timeout = 4000)
    public void testBothInstantsNullProducesAllZeros() {
        ConcreteBasePeriod p = new ConcreteBasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.standard());
        assertEquals(8, p.size());
        for (int i = 0; i < p.size(); i++) {
            assertEquals(0, p.getValue(i));
        }
    }

    @Test(timeout = 4000)
    public void testOneInstantNullWithDeterministicClock() {
        DateTimeUtils.setCurrentMillisFixed(100000000L);
        try {
            Instant end = new Instant(100000000L + 5000L);
            ConcreteBasePeriod p = new ConcreteBasePeriod(null, end, PeriodType.standard());
            assertEquals(5, p.getValue(p.indexOf(DurationFieldType.seconds())));

            Instant start = new Instant(100000000L - 10000L);
            ConcreteBasePeriod p2 = new ConcreteBasePeriod(start, null, PeriodType.standard());
            assertEquals(10, p2.getValue(p2.indexOf(DurationFieldType.seconds())));
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test(timeout = 4000)
    public void testNullDurationInInstantDurationConstructors() {
        DateTimeUtils.setCurrentMillisFixed(20000000L);
        try {
            Instant instant = new Instant(20000000L);
            ConcreteBasePeriod p1 = new ConcreteBasePeriod(instant, (ReadableDuration) null, PeriodType.standard());
            for (int i = 0; i < p1.size(); i++) {
                assertEquals(0, p1.getValue(i));
            }

            ConcreteBasePeriod p2 = new ConcreteBasePeriod((ReadableDuration) null, instant, PeriodType.standard());
            for (int i = 0; i < p2.size(); i++) {
                assertEquals(0, p2.getValue(i));
            }

            ConcreteBasePeriod p3 = new ConcreteBasePeriod((ReadableInstant) null, (ReadableDuration) null, PeriodType.standard());
            for (int i = 0; i < p3.size(); i++) {
                assertEquals(0, p3.getValue(i));
            }

            ConcreteBasePeriod p4 = new ConcreteBasePeriod((ReadableDuration) null, (ReadableInstant) null, PeriodType.standard());
            for (int i = 0; i < p4.size(); i++) {
                assertEquals(0, p4.getValue(i));
            }
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test(timeout = 4000)
    public void testMergePeriodAndAddPeriodNullIsNoOp() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        p.testMergePeriod(null);
        assertEquals(1, p.getValue(0));

        p.testAddPeriod(null);
        assertEquals(1, p.getValue(0));
    }

    @Test(timeout = 4000)
    public void testUnsupportedFieldZeroValueIgnored() {
        // PeriodType.time() supports only hours, minutes, seconds, millis
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 2, 3, 4, PeriodType.time());
        // Setting an unsupported field to 0 should succeed without error
        p.testSetField(DurationFieldType.years(), 0);
        p.testAddField(DurationFieldType.years(), 0);

        int[] arr = new int[p.size()];
        p.testSetFieldInto(arr, DurationFieldType.years(), 0);
        p.testAddFieldInto(arr, DurationFieldType.years(), 0);

        Period emptyStandard = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.testMergePeriod(emptyStandard);
        p.testAddPeriod(emptyStandard);
    }

    @Test(timeout = 4000)
    public void testZeroDurationConstructor() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        for (int i = 0; i < p.size(); i++) {
            assertEquals(0, p.getValue(i));
        }
    }

    @Test(timeout = 4000)
    public void testCustomChronologyConstructor() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0L, 10000000L, PeriodType.standard(), CopticChronology.getInstanceUTC());
        assertTrue(p.getValue(p.indexOf(DurationFieldType.hours())) > 0 || p.getValue(p.indexOf(DurationFieldType.minutes())) > 0);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Time-22 / Bug 3264409)
    // =========================================================================

    /**
     * Targets Defects4J Time-22:
     * org.joda.time.TestPeriod_Constructors::testConstructor_long_fixedZone
     * BasePeriod(long duration) is documented to create a period with time-only rules
     * (years, months, weeks, days must remain 0).
     * In the defective version, when DateTimeZone is fixed, ISOChronology has precise weeks/days,
     * leading to weeks being calculated as 64 instead of 0.
     */
    @Test(timeout = 4000)
    public void testConstructor_long_fixedZone_targetsDefect() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(1));
            // 64 weeks expressed in milliseconds
            long duration = 64L * 7L * 24L * 60L * 60L * 1000L;
            ConcreteBasePeriod test = new ConcreteBasePeriod(duration);

            assertEquals(0, test.getValue(0)); // years must be 0
            assertEquals(0, test.getValue(1)); // months must be 0
            assertEquals(0, test.getValue(2)); // weeks must be 0 (defective code produces 64!)
            assertEquals(0, test.getValue(3)); // days must be 0
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    /**
     * Direct replication of org.joda.time.TestPeriod_Constructors::testConstructor_long_fixedZone
     * using public Period subclass.
     */
    @Test(timeout = 4000)
    public void testConstructor_long_fixedZone_period() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(1));
            long duration = 64L * 7L * 24L * 60L * 60L * 1000L;
            Period test = new Period(duration);

            assertEquals(0, test.getYears());
            assertEquals(0, test.getMonths());
            assertEquals(0, test.getWeeks());
            assertEquals(0, test.getDays());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    /**
     * Direct replication of org.joda.time.TestDuration_Basics::testToPeriod_fixedZone.
     */
    @Test(timeout = 4000)
    public void testToPeriod_fixedZone() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forOffsetHours(1));
            long duration = 64L * 7L * 24L * 60L * 60L * 1000L;
            Duration dur = new Duration(duration);
            Period test = dur.toPeriod();

            assertEquals(0, test.getYears());
            assertEquals(0, test.getMonths());
            assertEquals(0, test.getWeeks());
            assertEquals(0, test.getDays());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    // =========================================================================
    // PARTITION D: Defensive Guard Paths & Exception Handling
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnsupportedFieldNonZeroInConstructorThrows() {
        // PeriodType.time() does not support years; non-zero value must throw
        new ConcreteBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadablePartialNullStartThrows() {
        new ConcreteBasePeriod((ReadablePartial) null, new LocalDate(2020, 1, 1), PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadablePartialNullEndThrows() {
        new ConcreteBasePeriod(new LocalDate(2020, 1, 1), (ReadablePartial) null, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadablePartialDifferentSizesThrows() {
        LocalDate ld = new LocalDate(2020, 1, 1); // size 3
        LocalTime lt = new LocalTime(12, 0);       // size 4
        new ConcreteBasePeriod(ld, lt, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadablePartialDifferentFieldTypesThrows() {
        YearMonth ym = new YearMonth(2020, 1); // fields: year, monthOfYear
        MonthDay md = new MonthDay(1, 1);       // fields: monthOfYear, dayOfMonth
        new ConcreteBasePeriod(ym, md, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadablePartialNonContiguousThrows() {
        Partial nonContiguous = new Partial(
            new DateTimeFieldType[] { DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth() },
            new int[] { 2020, 15 }
        );
        new ConcreteBasePeriod(nonContiguous, nonContiguous, PeriodType.standard());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetFieldUnsupportedNonZeroThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        p.testSetField(DurationFieldType.years(), 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetFieldNullFieldThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        p.testSetField(null, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetFieldNullFieldZeroValueThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        p.testSetField(null, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddFieldUnsupportedNonZeroThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        p.testAddField(DurationFieldType.years(), 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddFieldNullFieldThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        p.testAddField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddPeriodUnsupportedNonZeroThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        Period standardWithYears = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        p.testAddPeriod(standardWithYears);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMergePeriodUnsupportedNonZeroThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        Period standardWithYears = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        p.testMergePeriod(standardWithYears);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddFieldIntegerOverflowThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, PeriodType.standard());
        p.testAddField(DurationFieldType.hours(), 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddPeriodIntegerOverflowThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, PeriodType.standard());
        Period toAdd = new Period(0, 0, 0, 0, 1, 0, 0, 0);
        p.testAddPeriod(toAdd);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueNegativeIndexThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueExceedingIndexThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.getValue(8);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetValueIndexOutOfBoundsThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.testSetValue(10, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFieldTypeIndexOutOfBoundsThrows() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        p.getFieldType(99);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        ConcreteBasePeriod p1 = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        ConcreteBasePeriod p2 = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        ConcreteBasePeriod p3 = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 9, PeriodType.standard());
        Period pOther = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());

        assertEquals(p1, p1);
        assertEquals(p1, p2);
        assertEquals(p2, p1);
        assertEquals(p1.hashCode(), p2.hashCode());

        // BasePeriod equals ReadablePeriod contract
        assertEquals(p1, pOther);
        assertEquals(pOther, p1);

        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "NotAPeriod");
    }

    @Test(timeout = 4000)
    public void testToStringMatchesPeriodFormat() {
        ConcreteBasePeriod p = new ConcreteBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        String str = p.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("P"));
        assertTrue(str.contains("1Y"));
        assertTrue(str.contains("2M"));
    }
}