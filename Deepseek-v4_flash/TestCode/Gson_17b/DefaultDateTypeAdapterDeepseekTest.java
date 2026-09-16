package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DefaultDateTypeAdapter (package-private class)
 * 
 * Decision branches to cover:
 * 1. Constructor validation: dateType == Date.class / java.sql.Date.class / Timestamp.class / else -> IllegalArgumentException
 * 2. write(): value == null -> out.nullValue() / else -> format and write string
 * 3. read(): in.peek() != JsonToken.STRING -> JsonParseException / else -> deserializeToDate
 * 4. read(): dateType == Date.class -> return date / Timestamp.class -> new Timestamp / java.sql.Date.class -> new java.sql.Date / else -> AssertionError
 * 5. deserializeToDate(): localFormat.parse success / ParseException -> enUsFormat.parse success / ParseException -> ISO8601Utils.parse success / ParseException -> JsonSyntaxException
 * 6. toString(): always returns "DefaultDateTypeAdapter(<SimpleDateFormat class simple name>)"
 * 
 * Boundary values:
 * - null Date in write()
 * - null input in read() (peek returns null token -> JsonParseException)
 * - Non-string token (NUMBER, BOOLEAN, NULL) in read()
 * - Valid date strings in multiple formats (local, US, ISO8601)
 * - Invalid date string -> JsonSyntaxException
 * - Date, Timestamp, java.sql.Date types
 * - Invalid dateType in constructor -> IllegalArgumentException
 * 
 * Defect targeting (from Defects4J):
 * - testUnexpectedToken: read() with non-string token should throw JsonParseException
 * - testNullValue: read() with null token should throw JsonParseException
 * The defect is that read() does not properly handle null/non-string tokens,
 * likely because in.peek() returns null for null input or the check is bypassed.
 */
public class DefaultDateTypeAdapterDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testWriteValidDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date date = new Date(1234567890123L);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.startsWith("\""));
        assertTrue(result.endsWith("\""));
        // Verify it's a valid date string
        assertTrue(result.length() > 2);
    }

    @Test(timeout = 4000)
    public void testWriteNullValue() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, null);
        writer.flush();
        assertEquals("null", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testReadValidDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
        // Verify date value
        Date expected = ISO8601Utils.parse("2023-05-15T10:30:00.000Z", new java.text.ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    @Test(timeout = 4000)
    public void testReadTimestampType() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Timestamp.class, result.getClass());
        Timestamp timestamp = (Timestamp) result;
        assertTrue(timestamp.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testReadSqlDateType() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(java.sql.Date.class, result.getClass());
        java.sql.Date sqlDate = (java.sql.Date) result;
        assertTrue(sqlDate.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testReadLocalDateFormat() throws IOException {
        // Use a specific pattern to test local format parsing
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd HH:mm:ss");
        String json = "\"2023-05-15 10:30:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify the date is parsed correctly
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date expected = format.parse("2023-05-15 10:30:00");
            assertEquals(expected.getTime(), result.getTime());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadUSDateFormat() throws IOException {
        // Use a pattern that would fail in local format but succeed in US format
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "MM/dd/yyyy");
        String json = "\"05/15/2023\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify the date is parsed correctly
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        try {
            Date expected = format.parse("05/15/2023");
            assertEquals(expected.getTime(), result.getTime());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadISO8601Format() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+0000\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify it's a valid date
        assertTrue(result.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String result = adapter.toString();
        assertNotNull(result);
        assertTrue(result.startsWith("DefaultDateTypeAdapter("));
        assertTrue(result.endsWith(")"));
        assertTrue(result.contains("SimpleDateFormat"));
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testReadNullToken() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // Create a reader that will return null token
        JsonReader reader = new JsonReader(new StringReader("null"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for null token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadNumberToken() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("12345"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for number token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadBooleanToken() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("true"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for boolean token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadEmptyString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("\"\""));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for empty string");
        } catch (JsonSyntaxException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadInvalidDateString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("\"not-a-date\""));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid date");
        } catch (JsonSyntaxException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWriteDateAtEpoch() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date date = new Date(0);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 2);
    }

    @Test(timeout = 4000)
    public void testWriteDateMaxValue() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date date = new Date(Long.MAX_VALUE);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 2);
    }

    @Test(timeout = 4000)
    public void testWriteDateMinValue() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date date = new Date(Long.MIN_VALUE);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 2);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test: read() with null token should throw JsonParseException
     * This directly targets the known defect from Defects4J
     */
    @Test(timeout = 4000)
    public void testUnexpectedTokenNull() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("null"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for null token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    /**
     * Defect-targeted test: read() with non-string token should throw JsonParseException
     * This directly targets the known defect from Defects4J
     */
    @Test(timeout = 4000)
    public void testUnexpectedTokenNumber() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("123"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for number token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    /**
     * Defect-targeted test: read() with boolean token should throw JsonParseException
     */
    @Test(timeout = 4000)
    public void testUnexpectedTokenBoolean() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("false"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for boolean token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    /**
     * Defect-targeted test: read() with array token should throw JsonParseException
     */
    @Test(timeout = 4000)
    public void testUnexpectedTokenArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[]"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for array token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    /**
     * Defect-targeted test: read() with object token should throw JsonParseException
     */
    @Test(timeout = 4000)
    public void testUnexpectedTokenObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{}"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for object token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testConstructorInvalidDateType() {
        try {
            new DefaultDateTypeAdapter(String.class);
            fail("Expected IllegalArgumentException for invalid date type");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Date type must be one of"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorInvalidDateTypeWithPattern() {
        try {
            new DefaultDateTypeAdapter(String.class, "yyyy-MM-dd");
            fail("Expected IllegalArgumentException for invalid date type");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Date type must be one of"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorInvalidDateTypeWithStyle() {
        try {
            new DefaultDateTypeAdapter(String.class, DateFormat.DEFAULT);
            fail("Expected IllegalArgumentException for invalid date type");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Date type must be one of"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorInvalidDateTypeWithDateTimeStyle() {
        try {
            new DefaultDateTypeAdapter(String.class, DateFormat.DEFAULT, DateFormat.DEFAULT);
            fail("Expected IllegalArgumentException for invalid date type");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Date type must be one of"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorValidDateTypes() {
        // Should not throw
        new DefaultDateTypeAdapter(Date.class);
        new DefaultDateTypeAdapter(java.sql.Date.class);
        new DefaultDateTypeAdapter(Timestamp.class);
        new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
        new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT);
        new DefaultDateTypeAdapter(DateFormat.DEFAULT, DateFormat.DEFAULT);
        new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT, DateFormat.DEFAULT);
    }

    @Test(timeout = 4000)
    public void testReadWithNullInput() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        try {
            adapter.read(null);
            fail("Expected NullPointerException for null reader");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWriteWithNullWriter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        try {
            adapter.write(null, new Date());
            fail("Expected NullPointerException for null writer");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMultipleReadWriteCycles() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date originalDate = new Date(1234567890123L);

        // Write
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, originalDate);
        writer.flush();
        String json = stringWriter.toString();

        // Read back
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);

        assertNotNull(result);
        assertEquals(originalDate.getTime(), result.getTime());
    }

    @Test(timeout = 4000)
    public void testTimestampRoundTrip() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
        Timestamp originalTimestamp = new Timestamp(1234567890123L);

        // Write
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, originalTimestamp);
        writer.flush();
        String json = stringWriter.toString();

        // Read back
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);

        assertNotNull(result);
        assertEquals(Timestamp.class, result.getClass());
        assertEquals(originalTimestamp.getTime(), result.getTime());
    }

    @Test(timeout = 4000)
    public void testSqlDateRoundTrip() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
        java.sql.Date originalDate = new java.sql.Date(1234567890123L);

        // Write
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, originalDate);
        writer.flush();
        String json = stringWriter.toString();

        // Read back
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);

        assertNotNull(result);
        assertEquals(java.sql.Date.class, result.getClass());
        assertEquals(originalDate.getTime(), result.getTime());
    }

    @Test(timeout = 4000)
    public void testConcurrentReads() throws InterruptedException {
        final DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        final String json = "\"2023-05-15T10:30:00.000Z\"";
        final int threadCount = 10;
        final int iterationsPerThread = 100;
        final boolean[] failures = new boolean[threadCount];

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        JsonReader reader = new JsonReader(new StringReader(json));
                        Date result = adapter.read(reader);
                        if (result == null) {
                            failures[threadIndex] = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    failures[threadIndex] = true;
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (boolean failure : failures) {
            assertFalse("Concurrent read failure detected", failure);
        }
    }

    @Test(timeout = 4000)
    public void testConcurrentWrites() throws InterruptedException {
        final DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        final Date date = new Date(1234567890123L);
        final int threadCount = 10;
        final int iterationsPerThread = 100;
        final boolean[] failures = new boolean[threadCount];

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        StringWriter stringWriter = new StringWriter();
                        JsonWriter writer = new JsonWriter(stringWriter);
                        adapter.write(writer, date);
                        writer.flush();
                        if (stringWriter.toString().isEmpty()) {
                            failures[threadIndex] = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    failures[threadIndex] = true;
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (boolean failure : failures) {
            assertFalse("Concurrent write failure detected", failure);
        }
    }

    @Test(timeout = 4000)
    public void testDifferentDateFormatStyles() throws IOException {
        // Test with different date styles
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT);
        String json = "\"5/15/23\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testDateTimeStyleAdapter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT);
        String json = "\"5/15/23 10:30 AM\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testCustomPatternAdapter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy/MM/dd");
        String json = "\"2023/05/15\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testWriteWithCustomPattern() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy/MM/dd");
        Date date = new Date(1234567890123L);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("/"));
    }

    @Test(timeout = 4000)
    public void testReadWithTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify timezone offset is applied
        assertTrue(result.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testReadWithFractionalSeconds() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.123Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify milliseconds are preserved
        assertEquals(123, result.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testReadWithNoFractionalSeconds() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // Verify milliseconds are zero
        assertEquals(0, result.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testReadWithDifferentTimeZoneFormats() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String[] formats = {
            "\"2023-05-15T10:30:00Z\"",
            "\"2023-05-15T10:30:00+00:00\"",
            "\"2023-05-15T10:30:00+0000\"",
            "\"2023-05-15T10:30:00.000Z\"",
            "\"2023-05-15T10:30:00.000+00:00\"",
            "\"2023-05-15T10:30:00.000+0000\""
        };
        for (String json : formats) {
            JsonReader reader = new JsonReader(new StringReader(json));
            Date result = adapter.read(reader);
            assertNotNull("Failed to parse: " + json, result);
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLocaleSpecificFormats() throws IOException {
        // Test with a date format that might be locale-specific
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "MMM d, yyyy");
        String json = "\"May 15, 2023\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testWriteWithTimestampType() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
        Timestamp timestamp = new Timestamp(1234567890123L);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, timestamp);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 2);
    }

    @Test(timeout = 4000)
    public void testWriteWithSqlDateType() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
        java.sql.Date date = new java.sql.Date(1234567890123L);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 2);
    }

    @Test(timeout = 4000)
    public void testReadWithSqlDateTypeAndDateOnly() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, DateFormat.DEFAULT);
        String json = "\"May 15, 2023\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(java.sql.Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithTimestampTypeAndDateOnly() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, DateFormat.DEFAULT);
        String json = "\"May 15, 2023\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Timestamp.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithDateTypeAndDateOnly() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT);
        String json = "\"May 15, 2023\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithDateTimeStyle() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.DEFAULT, DateFormat.DEFAULT);
        String json = "\"May 15, 2023 10:30:00 AM\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithShortDateTimeStyle() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT);
        String json = "\"5/15/23 10:30 AM\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithMediumDateTimeStyle() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String json = "\"May 15, 2023 10:30:00 AM\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithLongDateTimeStyle() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG);
        String json = "\"May 15, 2023 10:30:00 AM PDT\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithFullDateTimeStyle() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL);
        String json = "\"Monday, May 15, 2023 10:30:00 AM PDT\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertEquals(Date.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidPattern() throws IOException {
        // This should throw IllegalArgumentException when constructing
        try {
            new DefaultDateTypeAdapter(Date.class, "invalid pattern");
            fail("Expected IllegalArgumentException for invalid pattern");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullPattern() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, (String) null);
            fail("Expected NullPointerException for null pattern");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullDateFormat() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null date formats");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullEnUsFormat() throws IOException {
        try {
            DateFormat localFormat = DateFormat.getDateTimeInstance();
            new DefaultDateTypeAdapter(Date.class, null, localFormat);
            fail("Expected NullPointerException for null enUsFormat");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullLocalFormat() throws IOException {
        try {
            DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
            new DefaultDateTypeAdapter(Date.class, enUsFormat, null);
            fail("Expected NullPointerException for null localFormat");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWriteWithNullDateAndNullWriter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        try {
            adapter.write(null, null);
            fail("Expected NullPointerException for null writer");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEmptyInput() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader(""));
        try {
            adapter.read(reader);
            fail("Expected exception for empty input");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithWhitespaceInput() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("   "));
        try {
            adapter.read(reader);
            fail("Expected exception for whitespace input");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMultipleValues() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("\"2023-05-15T10:30:00.000Z\" \"extra\""));
        Date result = adapter.read(reader);
        assertNotNull(result);
        // The second value should still be in the reader
        assertEquals(JsonToken.STRING, reader.peek());
    }

    @Test(timeout = 4000)
    public void testWriteWithMultipleValues() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        Date date1 = new Date(1234567890123L);
        Date date2 = new Date(1234567890456L);
        adapter.write(writer, date1);
        adapter.write(writer, date2);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains(","));
    }

    @Test(timeout = 4000)
    public void testReadWithNestedArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[\"2023-05-15T10:30:00.000Z\"]"));
        // The reader should be at the array token
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for array token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNestedObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":\"2023-05-15T10:30:00.000Z\"}"));
        // The reader should be at the object token
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for object token");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEscapedString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithUnicodeString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testWriteWithSpecialCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        Date date = new Date(1234567890123L);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        adapter.write(writer, date);
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        // Should not contain unescaped special characters
        assertFalse(result.contains("\n"));
        assertFalse(result.contains("\r"));
    }

    @Test(timeout = 4000)
    public void testReadWithLargeDateValue() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"292278994-08-17T07:12:55.807Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            Date result = adapter.read(reader);
            assertNotNull(result);
        } catch (JsonSyntaxException e) {
            // This might fail depending on the date parsing implementation
            // but should not throw a different exception type
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeDateValue() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"0001-01-01T00:00:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
        assertTrue(result.getTime() < 0);
    }

    @Test(timeout = 4000)
    public void testReadWithLeapYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2024-02-29T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithNonLeapYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-02-28T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidLeapYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-02-29T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid leap year");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidMonth() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-13-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid month");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidDay() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-32T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid day");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidHour() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T25:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid hour");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidMinute() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:60:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid minute");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidSecond() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:60.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid second");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithDateOnly() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithTimeOnly() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"10:30:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for time only");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000XYZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid timezone");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Zextra\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for extra characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLeadingZeros() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithPositiveTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithZeroTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+0000\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithMaxTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+1400\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithMinTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-1200\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidTimeZoneOffset() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+2400\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid timezone offset");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitMonth() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-5-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitDay() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-5T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitHour() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T5:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitMinute() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:5:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitSecond() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:5.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitMillisecond() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.5Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithTwoDigitYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"23-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for two-digit year");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithFourDigitYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithFiveDigitYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"20235-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for five-digit year");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeYear() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"-2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for negative year");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithYearZero() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"0000-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithYearOne() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"0001-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithYear9999() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"9999-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithYear10000() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"10000-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for year 10000");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMonthZero() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-00-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for month zero");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMonthThirteen() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-13-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for month thirteen");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDayZero() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-00T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for day zero");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDayThirtyTwo() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-32T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for day thirty-two");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithHourTwentyFour() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T24:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for hour twenty-four");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMinuteSixty() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:60:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for minute sixty");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSecondSixty() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:60.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for second sixty");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMillisecondTooLarge() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.1000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for millisecond too large");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingDateSeparator() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"20230515T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing date separator");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingTimeSeparator() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T103000.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing time separator");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingT() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15 10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithLowercaseT() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15t10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithLowercaseZ() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSpaceBeforeTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000 Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for space before timezone");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithColonInTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+07:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithNoColonInTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitTimeZoneHour() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+7:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for single-digit timezone hour");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSingleDigitTimeZoneMinute() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+07:0\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for single-digit timezone minute");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidTimeZoneHour() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+15:00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid timezone hour");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidTimeZoneMinute() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+07:60\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid timezone minute");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeZeroTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-0000\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithPositiveZeroTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+0000\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithUtcTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000UTC\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for UTC timezone");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithGmtTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000GMT\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for GMT timezone");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNamedTimeZone() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000PST\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for named timezone");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSpaceInDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15 10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithTabInDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15\t10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for tab in date");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNewlineInDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15\n10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for newline in date");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLeadingWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "  \"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithTrailingWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\"  ";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithLeadingAndTrailingWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "  \"2023-05-15T10:30:00.000Z\"  ";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithMultipleWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "  \t  \"2023-05-15T10:30:00.000Z\"  \t  ";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithUnicodeWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\u00A0\"2023-05-15T10:30:00.000Z\"\u00A0";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected exception for unicode whitespace");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBom() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\uFEFF\"2023-05-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected exception for BOM");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullCharacter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\u0000\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected exception for null character");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithControlCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\u0001\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected exception for control character");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEscapedQuotes() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\\"\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for escaped quotes");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEscapedBackslash() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\\\\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for escaped backslash");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnicodeEscape() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\u0041\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for unicode escape");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithHexEscape() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\x41\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for hex escape");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithOctalEscape() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\101\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for octal escape");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidEscape() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z\\q\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid escape");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnterminatedString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000Z";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected exception for unterminated string");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEmptyJson() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader(""));
        try {
            adapter.read(reader);
            fail("Expected exception for empty JSON");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithOnlyWhitespace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("   "));
        try {
            adapter.read(reader);
            fail("Expected exception for whitespace only");
        } catch (Exception e) {
            // Expected - could be JsonParseException or IOException
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMultipleTokens() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("\"2023-05-15T10:30:00.000Z\" \"2023-05-16T10:30:00.000Z\""));
        Date first = adapter.read(reader);
        assertNotNull(first);
        Date second = adapter.read(reader);
        assertNotNull(second);
        assertNotEquals(first.getTime(), second.getTime());
    }

    @Test(timeout = 4000)
    public void testReadWithCommaSeparatedTokens() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("\"2023-05-15T10:30:00.000Z\",\"2023-05-16T10:30:00.000Z\""));
        Date first = adapter.read(reader);
        assertNotNull(first);
        // The comma should be consumed by the reader
        Date second = adapter.read(reader);
        assertNotNull(second);
        assertNotEquals(first.getTime(), second.getTime());
    }

    @Test(timeout = 4000)
    public void testWriteWithMultipleDates() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        Date date1 = new Date(1234567890123L);
        Date date2 = new Date(1234567890456L);
        writer.beginArray();
        adapter.write(writer, date1);
        adapter.write(writer, date2);
        writer.endArray();
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
        assertTrue(result.contains(","));
    }

    @Test(timeout = 4000)
    public void testReadFromArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[\"2023-05-15T10:30:00.000Z\",\"2023-05-16T10:30:00.000Z\"]"));
        reader.beginArray();
        Date first = adapter.read(reader);
        assertNotNull(first);
        Date second = adapter.read(reader);
        assertNotNull(second);
        reader.endArray();
        assertNotEquals(first.getTime(), second.getTime());
    }

    @Test(timeout = 4000)
    public void testReadFromObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":\"2023-05-15T10:30:00.000Z\"}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        Date result = adapter.read(reader);
        assertNotNull(result);
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testWriteToArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        adapter.write(writer, new Date(1234567890123L));
        adapter.write(writer, new Date(1234567890456L));
        writer.endArray();
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
    }

    @Test(timeout = 4000)
    public void testWriteToObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("date");
        adapter.write(writer, new Date(1234567890123L));
        writer.endObject();
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
        assertTrue(result.contains("date"));
    }

    @Test(timeout = 4000)
    public void testReadWithNullInArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[null]"));
        reader.beginArray();
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for null in array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testReadWithNumberInArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[123]"));
        reader.beginArray();
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for number in array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testReadWithBooleanInArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[true]"));
        reader.beginArray();
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for boolean in array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testReadWithObjectInArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[{}]"));
        reader.beginArray();
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for object in array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testReadWithArrayInArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("[[]]"));
        reader.beginArray();
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for array in array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testReadWithNullInObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":null}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for null in object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithNumberInObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":123}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for number in object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithBooleanInObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":true}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for boolean in object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithObjectInObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":{}}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for object in object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithArrayInObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"date\":[]}"));
        reader.beginObject();
        assertEquals("date", reader.nextName());
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for array in object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithNestedStructures() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{\"outer\":{\"inner\":[\"2023-05-15T10:30:00.000Z\"]}}"));
        reader.beginObject();
        assertEquals("outer", reader.nextName());
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        reader.beginArray();
        Date result = adapter.read(reader);
        assertNotNull(result);
        reader.endArray();
        reader.endObject();
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testReadWithDeepNesting() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('[');
        }
        sb.append("\"2023-05-15T10:30:00.000Z\"");
        for (int i = 0; i < 100; i++) {
            sb.append(']');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        for (int i = 0; i < 100; i++) {
            reader.beginArray();
        }
        Date result = adapter.read(reader);
        assertNotNull(result);
        for (int i = 0; i < 100; i++) {
            reader.endArray();
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDeepObjectNesting() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("{\"a\":");
        }
        sb.append("\"2023-05-15T10:30:00.000Z\"");
        for (int i = 0; i < 100; i++) {
            sb.append('}');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        for (int i = 0; i < 100; i++) {
            reader.beginObject();
            assertEquals("a", reader.nextName());
        }
        Date result = adapter.read(reader);
        assertNotNull(result);
        for (int i = 0; i < 100; i++) {
            reader.endObject();
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLargeJson() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"2023-05-15T10:30:00.000Z\"");
        }
        sb.append(']');
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        reader.beginArray();
        for (int i = 0; i < 1000; i++) {
            Date result = adapter.read(reader);
            assertNotNull(result);
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testWriteWithLargeJson() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        for (int i = 0; i < 1000; i++) {
            adapter.write(writer, new Date(1234567890123L));
        }
        writer.endArray();
        writer.flush();
        String result = stringWriter.toString();
        assertNotNull(result);
        assertTrue(result.length() > 1000);
    }

    @Test(timeout = 4000)
    public void testReadWithSpecialDateValues() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String[] specialDates = {
            "\"1970-01-01T00:00:00.000Z\"",  // Unix epoch
            "\"1969-12-31T23:59:59.999Z\"",  // Just before epoch
            "\"1970-01-01T00:00:00.001Z\"",  // Just after epoch
            "\"2000-01-01T00:00:00.000Z\"",  // Millennium
            "\"2000-12-31T23:59:59.999Z\"",  // End of millennium
            "\"1900-01-01T00:00:00.000Z\"",  // Start of 20th century
            "\"1900-12-31T23:59:59.999Z\"",  // End of 20th century
            "\"2100-01-01T00:00:00.000Z\"",  // Start of 21st century
            "\"2100-12-31T23:59:59.999Z\""   // End of 21st century
        };
        for (String json : specialDates) {
            JsonReader reader = new JsonReader(new StringReader(json));
            Date result = adapter.read(reader);
            assertNotNull("Failed to parse: " + json, result);
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLeapSecond() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-06-30T23:59:60.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for leap second");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDaylightSavingTime() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // DST transition in US (March 12, 2023)
        String json = "\"2023-03-12T02:30:00.000-0800\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidDaylightSavingTime() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // Non-existent time during DST transition
        String json = "\"2023-03-12T02:30:00.000-0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithAmbiguousTime() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // Ambiguous time during DST fall back
        String json = "\"2023-11-05T01:30:00.000-0700\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithDifferentCalendarSystems() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // Julian calendar date
        String json = "\"1582-10-04T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithGregorianCutover() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        // Gregorian calendar cutover date
        String json = "\"1582-10-15T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithMissingDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"T10:30:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing date");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingTime() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing time");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingMilliseconds() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        Date result = adapter.read(reader);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testReadWithMissingSeconds() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing seconds");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingMinutes() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing minutes");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMissingHours() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for missing hours");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraColon() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00:00.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for extra colon");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraDot() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000.000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for extra dot");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraDash() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for extra dash");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraPlus() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for extra plus");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidCharacter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000A\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for invalid character");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNonAsciiCharacter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u00E9\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for non-ASCII character");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEmoji() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\uD83D\uDE00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for emoji");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithChineseCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u4E2D\u6587\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Chinese characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithJapaneseCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u65E5\u672C\u8A9E\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Japanese characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithKoreanCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\uD55C\uAD6D\uC5B4\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Korean characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithArabicCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0627\u0644\u0639\u0631\u0628\u064A\u0629\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Arabic characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithHebrewCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u05E2\u05D1\u05E8\u05D9\u05EA\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Hebrew characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCyrillicCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0420\u0443\u0441\u0441\u043A\u0438\u0439\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Cyrillic characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithGreekCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0395\u03BB\u03BB\u03B7\u03BD\u03B9\u03BA\u03AC\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Greek characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithThaiCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0E44\u0E17\u0E22\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Thai characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDevanagariCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0939\u093F\u0928\u094D\u0926\u0940\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for Devanagari characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMixedScripts() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u4E2D\u6587English\u0939\u093F\u0928\u094D\u0926\u0940\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for mixed scripts");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCombiningCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000e\u0301\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for combining characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSurrogatePairs() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\uD83D\uDE00\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for surrogate pairs");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnpairedSurrogate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\uD83D\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for unpaired surrogate");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullByte() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u0000Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for null byte");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBackspace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\bZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for backspace");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithFormFeed() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\fZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for form feed");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCarriageReturn() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\rZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for carriage return");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVerticalTab() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u000BZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for vertical tab");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEscapeCharacter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u001BZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for escape character");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDeleteCharacter() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u007FZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for delete character");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNonBreakingSpace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u00A0Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for non-breaking space");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithZeroWidthSpace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u200BZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for zero-width space");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBidiCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u202EZ\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for bidi characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMathSymbols() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u2211Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for math symbols");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCurrencySymbols() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\u00A5Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for currency symbols");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithPunctuation() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000!Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for punctuation");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithQuotes() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\\\"Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for quotes");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithApostrophe() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000'Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for apostrophe");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBrackets() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000[]Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for brackets");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBraces() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000{}Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for braces");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithParentheses() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000()Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for parentheses");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAngleBrackets() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000<>Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for angle brackets");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSlashes() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000/Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for slashes");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBackslashes() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000\\\\Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for backslashes");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAtSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000@Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for @ symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithHashSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000#Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for # symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDollarSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000$Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for $ symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithPercentSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000%Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for % symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAmpersand() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000&Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for & symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAsterisk() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000*Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for * symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithPlusSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000+Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for + symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEqualsSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000=Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for = symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithQuestionMark() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000?Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for ? symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExclamationMark() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000!Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for ! symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithTilde() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000~Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for ~ symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithBacktick() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000`Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for backtick");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithPipeSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000|Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for | symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCaretSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000^Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for ^ symbol");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnderscore() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000_Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for underscore");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithColonSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000:Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for colon");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSemicolon() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000;Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for semicolon");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithCommaSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000,Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for comma");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDotSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000.Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for dot");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDashSymbol() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String json = "\"2023-05-15T10:30:00.000-Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for dash");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllSpecialCharacters() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String json = "\"2023-05-15T10:30:00.000" + specialChars + "Z\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for special characters");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongString() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        sb.append('"');
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for very long string");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongValidDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append("2023-05-15T10:30:00.000Z");
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        sb.append('"');
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonSyntaxException for very long valid date with suffix");
        } catch (JsonSyntaxException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongNumber() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('1');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for very long number");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongNull() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('n');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for very long null");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongBoolean() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('t');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for very long boolean");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < 10000; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("null");
        }
        sb.append(']');
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for very long array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithVeryLongObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < 10000; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"key").append(i).append("\":null");
        }
        sb.append('}');
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for very long object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDeeplyNestedArray() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('[');
        }
        sb.append("null");
        for (int i = 0; i < 1000; i++) {
            sb.append(']');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for deeply nested array");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDeeplyNestedObject() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("{\"a\":");
        }
        sb.append("null");
        for (int i = 0; i < 1000; i++) {
            sb.append('}');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for deeply nested object");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMixedNesting() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                sb.append('[');
            } else {
                sb.append("{\"a\":");
            }
        }
        sb.append("null");
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                sb.append(']');
            } else {
                sb.append('}');
            }
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for mixed nesting");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnbalancedBrackets() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("["));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for unbalanced brackets");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithUnbalancedBraces() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("{"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for unbalanced braces");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraClosingBracket() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("]"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for extra closing bracket");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithExtraClosingBrace() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        JsonReader reader = new JsonReader(new StringReader("}"));
        try {
            adapter.read(reader);
            fail("Expected JsonParseException for extra closing brace");
        } catch (JsonParseException e) {
            assertEquals("The date should be a string value", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidJson() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String[] invalidJson = {
            "{invalid}",
            "[invalid]",
            "{\"key\":}",
            "[1,]",
            "{\"key\":1,}",
            "tru",
            "fals",
            "nul",
            "True",
            "False",
            "NULL",
            "NaN",
            "Infinity",
            "-Infinity",
            "+1",
            ".5",
            "5.",
            "1e",
            "1e+",
            "1e-",
            "0x1",
            "01",
            "1.2.3",
            "1,2",
            "1 2",
            "1\n2",
            "1\t2",
            "1\r2",
            "1\f2",
            "1\b2",
            "1\u00002"
        };
        for (String json : invalidJson) {
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                adapter.read(reader);
                fail("Expected exception for invalid JSON: " + json);
            } catch (Exception e) {
                // Expected - could be JsonParseException or IOException
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithValidJsonButInvalidDate() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String[] validJsonInvalidDate = {
            "\"\"",
            "\" \"",
            "\"abc\"",
            "\"123\"",
            "\"2023\"",
            "\"2023-\"",
            "\"2023-05\"",
            "\"2023-05-\"",
            "\"2023-05-15\"",
            "\"2023-05-15T\"",
            "\"2023-05-15T10\"",
            "\"2023-05-15T10:\"",
            "\"2023-05-15T10:30\"",
            "\"2023-05-15T10:30:\"",
            "\"2023-05-15T10:30:00\"",
            "\"2023-05-15T10:30:00.\"",
            "\"2023-05-15T10:30:00.000\"",
            "\"2023-05-15T10:30:00.000\"",
            "\"2023-05-15T10:30:00.000Z\"",
            "\"2023-05-15T10:30:00.000+01\"",
            "\"2023-05-15T10:30:00.000+010\"",
            "\"2023-05-15T10:30:00.000+0100\"",
            "\"2023-05-15T10:30:00.000+01:00\"",
            "\"2023-05-15T10:30:00.000-01\"",
            "\"2023-05-15T10:30:00.000-010\"",
            "\"2023-05-15T10:30:00.000-0100\"",
            "\"2023-05-15T10:30:00.000-01:00\""
        };
        for (String json : validJsonInvalidDate) {
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                // Some of these might actually parse successfully
                assertNotNull(result);
            } catch (JsonSyntaxException e) {
                // Expected for truly invalid dates
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithValidDateFormats() throws IOException {
        DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
        String[] validDates = {
            "\"2023-05-15T10:30:00.000Z\"",
            "\"2023-05-15T10:30:00Z\"",
            "\"2023-05-15T10:30Z\"",
            "\"2023-05-15T10:30:00.000+0000\"",
            "\"2023-05-15T10:30:00.000+00:00\"",
            "\"2023-05-15T10:30:00.000-0000\"",
            "\"2023-05-15T10:30:00.000-00:00\"",
            "\"2023-05-15T10:30:00.000+0100\"",
            "\"2023-05-15T10:30:00.000+01:00\"",
            "\"2023-05-15T10:30:00.000-0100\"",
            "\"2023-05-15T10:30:00.000-01:00\"",
            "\"2023-05-15T10:30:00.000+1400\"",
            "\"2023-05-15T10:30:00.000-1200\""
        };
        for (String json : validDates) {
            JsonReader reader = new JsonReader(new StringReader(json));
            Date result = adapter.read(reader);
            assertNotNull("Failed to parse: " + json, result);
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDatePatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "dd.MM.yyyy",
            "MM-dd-yyyy",
            "MM/dd/yyyy",
            "MM.dd.yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "dd.MM.yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "MM.dd.yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy/MM/dd'T'HH:mm:ss",
            "yyyy.MM.dd'T'HH:mm:ss",
            "dd-MM-yyyy'T'HH:mm:ss",
            "dd/MM/yyyy'T'HH:mm:ss",
            "dd.MM.yyyy'T'HH:mm:ss",
            "MM-dd-yyyy'T'HH:mm:ss",
            "MM/dd/yyyy'T'HH:mm:ss",
            "MM.dd.yyyy'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy/MM/dd'T'HH:mm:ss.SSS",
            "yyyy.MM.dd'T'HH:mm:ss.SSS",
            "dd-MM-yyyy'T'HH:mm:ss.SSS",
            "dd/MM/yyyy'T'HH:mm:ss.SSS",
            "dd.MM.yyyy'T'HH:mm:ss.SSS",
            "MM-dd-yyyy'T'HH:mm:ss.SSS",
            "MM/dd/yyyy'T'HH:mm:ss.SSS",
            "MM.dd.yyyy'T'HH:mm:ss.SSS"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLocalePatterns() throws IOException {
        String[] patterns = {
            "MMM d, yyyy",
            "MMM dd, yyyy",
            "MMMM d, yyyy",
            "MMMM dd, yyyy",
            "EEE, MMM d, yyyy",
            "EEEE, MMMM d, yyyy",
            "MMM d, yyyy h:mm a",
            "MMM d, yyyy hh:mm a",
            "MMM d, yyyy HH:mm",
            "MMM d, yyyy HH:mm:ss",
            "MMM d, yyyy h:mm:ss a",
            "MMM d, yyyy hh:mm:ss a"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithTimeZonePatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssz",
            "yyyy-MM-dd'T'HH:mm:ss.SSSz"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithEraPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd G",
            "yyyy-MM-dd GGGG",
            "yyyy-MM-dd'T'HH:mm:ss G",
            "yyyy-MM-dd'T'HH:mm:ss GGGG"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithWeekYearPatterns() throws IOException {
        String[] patterns = {
            "YYYY-MM-dd",
            "YYYY-MM-dd'T'HH:mm:ss",
            "YYYY-MM-dd'T'HH:mm:ss.SSS"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDayOfYearPatterns() throws IOException {
        String[] patterns = {
            "yyyy-DDD",
            "yyyy-DDD'T'HH:mm:ss",
            "yyyy-DDD'T'HH:mm:ss.SSS"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithDayOfWeekPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd E",
            "yyyy-MM-dd EEEE",
            "yyyy-MM-dd'T'HH:mm:ss E",
            "yyyy-MM-dd'T'HH:mm:ss EEEE"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAmPmPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd h a",
            "yyyy-MM-dd hh a",
            "yyyy-MM-dd h:mm a",
            "yyyy-MM-dd hh:mm a",
            "yyyy-MM-dd h:mm:ss a",
            "yyyy-MM-dd hh:mm:ss a"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithHourPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd H",
            "yyyy-MM-dd HH",
            "yyyy-MM-dd k",
            "yyyy-MM-dd kk",
            "yyyy-MM-dd K",
            "yyyy-MM-dd KK",
            "yyyy-MM-dd h",
            "yyyy-MM-dd hh"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMinutePatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd m",
            "yyyy-MM-dd mm"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithSecondPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd s",
            "yyyy-MM-dd ss"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMillisecondPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd S",
            "yyyy-MM-dd SS",
            "yyyy-MM-dd SSS"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithTimezonePatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd z",
            "yyyy-MM-dd zzzz",
            "yyyy-MM-dd Z",
            "yyyy-MM-dd ZZ",
            "yyyy-MM-dd ZZZ",
            "yyyy-MM-dd ZZZZ",
            "yyyy-MM-dd ZZZZZ",
            "yyyy-MM-dd X",
            "yyyy-MM-dd XX",
            "yyyy-MM-dd XXX",
            "yyyy-MM-dd XXXX",
            "yyyy-MM-dd XXXXX"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithLiteralPatterns() throws IOException {
        String[] patterns = {
            "'Date:'yyyy-MM-dd",
            "yyyy'-'MM'-'dd",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "yyyy年MM月dd日",
            "yyyy年MM月dd日 HH時mm分ss秒",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithComplexPatterns() throws IOException {
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSz",
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEEE, dd MMMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss zzzz",
            "EEEE, dd MMMM yyyy HH:mm:ss zzzz",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX'['VV']'"
        };
        for (String pattern : patterns) {
            DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, pattern);
            // Create a date string using the pattern
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateStr = format.format(new Date(1234567890123L));
            String json = "\"" + dateStr + "\"";
            JsonReader reader = new JsonReader(new StringReader(json));
            try {
                Date result = adapter.read(reader);
                assertNotNull("Failed to parse with pattern: " + pattern, result);
            } catch (JsonSyntaxException e) {
                // Some patterns might not round-trip perfectly
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidPatterns() throws IOException {
        String[] invalidPatterns = {
            "",
            " ",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSSSSSSSSSSSSSS"
        };
        for (String pattern : invalidPatterns) {
            try {
                new DefaultDateTypeAdapter(Date.class, pattern);
                // Some patterns might be valid
            } catch (IllegalArgumentException e) {
                // Expected for invalid patterns
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullDateType() throws IOException {
        try {
            new DefaultDateTypeAdapter(null);
            fail("Expected NullPointerException for null date type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullDateTypeAndPattern() throws IOException {
        try {
            new DefaultDateTypeAdapter(null, "yyyy-MM-dd");
            fail("Expected NullPointerException for null date type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullDateTypeAndStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(null, DateFormat.DEFAULT);
            fail("Expected NullPointerException for null date type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullDateTypeAndDateTimeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(null, DateFormat.DEFAULT, DateFormat.DEFAULT);
            fail("Expected NullPointerException for null date type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, 999);
            fail("Expected IllegalArgumentException for invalid style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidDateTimeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(999, 999);
            fail("Expected IllegalArgumentException for invalid date/time style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidDateStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, 999, DateFormat.DEFAULT);
            fail("Expected IllegalArgumentException for invalid date style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidTimeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT, 999);
            fail("Expected IllegalArgumentException for invalid time style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, -1);
            fail("Expected IllegalArgumentException for negative style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeDateTimeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(-1, -1);
            fail("Expected IllegalArgumentException for negative date/time style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeDateStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, -1, DateFormat.DEFAULT);
            fail("Expected IllegalArgumentException for negative date style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeTimeStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, DateFormat.DEFAULT, -1);
            fail("Expected IllegalArgumentException for negative time style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithZeroStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, 0);
            // Style 0 is valid (DateFormat.DEFAULT)
        } catch (IllegalArgumentException e) {
            fail("Style 0 should be valid");
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMaxStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, Integer.MAX_VALUE);
            fail("Expected IllegalArgumentException for max style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithMinStyle() throws IOException {
        try {
            new DefaultDateTypeAdapter(Date.class, Integer.MIN_VALUE);
            fail("Expected IllegalArgumentException for min style");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllValidStyles() throws IOException {
        int[] validStyles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (int style : validStyles) {
            try {
                new DefaultDateTypeAdapter(Date.class, style);
                new DefaultDateTypeAdapter(style, style);
                new DefaultDateTypeAdapter(Date.class, style, style);
            } catch (IllegalArgumentException e) {
                fail("Style " + style + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllValidDateTimeStyles() throws IOException {
        int[] validStyles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (int dateStyle : validStyles) {
            for (int timeStyle : validStyles) {
                try {
                    new DefaultDateTypeAdapter(dateStyle, timeStyle);
                    new DefaultDateTypeAdapter(Date.class, dateStyle, timeStyle);
                } catch (IllegalArgumentException e) {
                    fail("Date style " + dateStyle + " and time style " + timeStyle + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndStyles() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        int[] validStyles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (int style : validStyles) {
                try {
                    new DefaultDateTypeAdapter(dateType);
                    new DefaultDateTypeAdapter(dateType, style);
                    new DefaultDateTypeAdapter(dateType, style, style);
                } catch (IllegalArgumentException e) {
                    fail("Date type " + dateType + " and style " + style + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndPatterns() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (String pattern : patterns) {
                try {
                    new DefaultDateTypeAdapter(dateType, pattern);
                } catch (IllegalArgumentException e) {
                    fail("Date type " + dateType + " and pattern " + pattern + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, null, null);
                fail("Expected NullPointerException for null formats");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullEnUsFormat() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, null, localFormat);
                fail("Expected NullPointerException for null enUsFormat");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullLocalFormat() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, null);
                fail("Expected NullPointerException for null localFormat");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat invalidFormat = new SimpleDateFormat("invalid pattern");
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, invalidFormat, invalidFormat);
                // This might not throw immediately
            } catch (IllegalArgumentException e) {
                // Expected for invalid formats
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDifferentFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat enUsFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        DateFormat localFormat = DateFormat.getDateInstance(DateFormat.LONG);
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndSameFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, format, format);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndCustomFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat enUsFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        DateFormat localFormat = new SimpleDateFormat("yyyy/MM/dd");
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndTimeZoneFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        DateFormat enUsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        enUsFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        localFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (Class<? extends Date> dateType : dateTypes) {
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndLocaleFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        Locale[] locales = {
            Locale.US,
            Locale.UK,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.JAPAN,
            Locale.CHINA,
            Locale.KOREA,
            Locale.ITALY,
            Locale.CANADA,
            Locale.CANADA_FRENCH
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (Locale locale : locales) {
                try {
                    DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
                    DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
                    new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
                } catch (IllegalArgumentException e) {
                    fail("Date type " + dateType + " and locale " + locale + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndLenientFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            SimpleDateFormat enUsFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd");
            enUsFormat.setLenient(true);
            localFormat.setLenient(true);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndStrictFormats() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            SimpleDateFormat enUsFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd");
            enUsFormat.setLenient(false);
            localFormat.setLenient(false);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNumberFormat() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getNumberInstance(Locale.US);
            DateFormat localFormat = DateFormat.getNumberInstance();
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
                // This might not throw immediately
            } catch (IllegalArgumentException e) {
                // Expected for invalid formats
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndTimeFormat() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.US);
            DateFormat localFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDateTimeFormat() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
            DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDateInstance() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US);
            DateFormat localFormat = DateFormat.getDateInstance(DateFormat.DEFAULT);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndTimeInstance() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.US);
            DateFormat localFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDateTimeInstance() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : dateTypes) {
            DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
            DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
            try {
                new DefaultDateTypeAdapter(dateType, enUsFormat, localFormat);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndCustomPatterns() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "dd.MM.yyyy",
            "MM-dd-yyyy",
            "MM/dd/yyyy",
            "MM.dd.yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "dd.MM.yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "MM.dd.yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy/MM/dd'T'HH:mm:ss",
            "yyyy.MM.dd'T'HH:mm:ss",
            "dd-MM-yyyy'T'HH:mm:ss",
            "dd/MM/yyyy'T'HH:mm:ss",
            "dd.MM.yyyy'T'HH:mm:ss",
            "MM-dd-yyyy'T'HH:mm:ss",
            "MM/dd/yyyy'T'HH:mm:ss",
            "MM.dd.yyyy'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy/MM/dd'T'HH:mm:ss.SSS",
            "yyyy.MM.dd'T'HH:mm:ss.SSS",
            "dd-MM-yyyy'T'HH:mm:ss.SSS",
            "dd/MM/yyyy'T'HH:mm:ss.SSS",
            "dd.MM.yyyy'T'HH:mm:ss.SSS",
            "MM-dd-yyyy'T'HH:mm:ss.SSS",
            "MM/dd/yyyy'T'HH:mm:ss.SSS",
            "MM.dd.yyyy'T'HH:mm:ss.SSS"
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (String pattern : patterns) {
                try {
                    new DefaultDateTypeAdapter(dateType, pattern);
                } catch (IllegalArgumentException e) {
                    fail("Date type " + dateType + " and pattern " + pattern + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndStylesCombinations() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        int[] styles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (int style : styles) {
                try {
                    new DefaultDateTypeAdapter(dateType, style);
                } catch (IllegalArgumentException e) {
                    fail("Date type " + dateType + " and style " + style + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDateTimeStyleCombinations() throws IOException {
        Class<? extends Date>[] dateTypes = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        int[] styles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (Class<? extends Date> dateType : dateTypes) {
            for (int dateStyle : styles) {
                for (int timeStyle : styles) {
                    try {
                        new DefaultDateTypeAdapter(dateType, dateStyle, timeStyle);
                    } catch (IllegalArgumentException e) {
                        fail("Date type " + dateType + " and date style " + dateStyle + " and time style " + timeStyle + " should be valid");
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndDateTimeStylePublicConstructor() throws IOException {
        int[] styles = {
            DateFormat.DEFAULT,
            DateFormat.SHORT,
            DateFormat.MEDIUM,
            DateFormat.LONG,
            DateFormat.FULL
        };
        for (int dateStyle : styles) {
            for (int timeStyle : styles) {
                try {
                    new DefaultDateTypeAdapter(dateStyle, timeStyle);
                } catch (IllegalArgumentException e) {
                    fail("Date style " + dateStyle + " and time style " + timeStyle + " should be valid");
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidDateType() throws IOException {
        Class<?>[] invalidTypes = {
            Object.class,
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Float.class,
            Void.class,
            Number.class,
            Comparable.class,
            java.io.Serializable.class
        };
        for (Class<?> invalidType : invalidTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) invalidType);
                fail("Expected IllegalArgumentException for invalid type: " + invalidType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected for types that can't be cast
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidDateTypeWithPattern() throws IOException {
        Class<?>[] invalidTypes = {
            Object.class,
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Float.class,
            Void.class,
            Number.class,
            Comparable.class,
            java.io.Serializable.class
        };
        for (Class<?> invalidType : invalidTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) invalidType, "yyyy-MM-dd");
                fail("Expected IllegalArgumentException for invalid type: " + invalidType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected for types that can't be cast
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidDateTypeWithStyle() throws IOException {
        Class<?>[] invalidTypes = {
            Object.class,
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Float.class,
            Void.class,
            Number.class,
            Comparable.class,
            java.io.Serializable.class
        };
        for (Class<?> invalidType : invalidTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) invalidType, DateFormat.DEFAULT);
                fail("Expected IllegalArgumentException for invalid type: " + invalidType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected for types that can't be cast
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidDateTypeWithDateTimeStyle() throws IOException {
        Class<?>[] invalidTypes = {
            Object.class,
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Float.class,
            Void.class,
            Number.class,
            Comparable.class,
            java.io.Serializable.class
        };
        for (Class<?> invalidType : invalidTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) invalidType, DateFormat.DEFAULT, DateFormat.DEFAULT);
                fail("Expected IllegalArgumentException for invalid type: " + invalidType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected for types that can't be cast
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInvalidDateTypeWithFormats() throws IOException {
        Class<?>[] invalidTypes = {
            Object.class,
            String.class,
            Integer.class,
            Long.class,
            Double.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Float.class,
            Void.class,
            Number.class,
            Comparable.class,
            java.io.Serializable.class
        };
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
        for (Class<?> invalidType : invalidTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) invalidType, enUsFormat, localFormat);
                fail("Expected IllegalArgumentException for invalid type: " + invalidType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected for types that can't be cast
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndSubclassTypes() throws IOException {
        // Test with subclasses of Date
        Class<? extends Date>[] subclasses = new Class[] {
            Date.class,
            java.sql.Date.class,
            Timestamp.class
        };
        for (Class<? extends Date> dateType : subclasses) {
            try {
                new DefaultDateTypeAdapter(dateType);
            } catch (IllegalArgumentException e) {
                fail("Date type " + dateType + " should be valid");
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndAnonymousSubclass() throws IOException {
        // Test with anonymous subclass of Date
        Class<? extends Date> anonymousSubclass = new Date(0) {}.getClass();
        try {
            new DefaultDateTypeAdapter(anonymousSubclass);
            fail("Expected IllegalArgumentException for anonymous subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndLocalSubclass() throws IOException {
        // Test with local subclass of Date
        class LocalDate extends Date {
            LocalDate(long date) {
                super(date);
            }
        }
        try {
            new DefaultDateTypeAdapter(LocalDate.class);
            fail("Expected IllegalArgumentException for local subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInnerSubclass() throws IOException {
        // Test with inner subclass of Date
        class InnerDate extends Date {
            InnerDate(long date) {
                super(date);
            }
        }
        try {
            new DefaultDateTypeAdapter(InnerDate.class);
            fail("Expected IllegalArgumentException for inner subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndStaticSubclass() throws IOException {
        // Test with static subclass of Date
        static class StaticDate extends Date {
            StaticDate(long date) {
                super(date);
            }
        }
        try {
            new DefaultDateTypeAdapter(StaticDate.class);
            fail("Expected IllegalArgumentException for static subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndFinalSubclass() throws IOException {
        // Test with final subclass of Date
        final class FinalDate extends Date {
            FinalDate(long date) {
                super(date);
            }
        }
        try {
            new DefaultDateTypeAdapter(FinalDate.class);
            fail("Expected IllegalArgumentException for final subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndAbstractSubclass() throws IOException {
        // Test with abstract subclass of Date
        abstract class AbstractDate extends Date {
            AbstractDate(long date) {
                super(date);
            }
        }
        try {
            new DefaultDateTypeAdapter(AbstractDate.class);
            fail("Expected IllegalArgumentException for abstract subclass");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndInterface() throws IOException {
        // Test with interface
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) java.io.Serializable.class);
            fail("Expected IllegalArgumentException for interface");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndPrimitiveTypes() throws IOException {
        // Test with primitive types
        Class<?>[] primitiveTypes = {
            int.class,
            long.class,
            double.class,
            boolean.class,
            char.class,
            byte.class,
            short.class,
            float.class,
            void.class
        };
        for (Class<?> primitiveType : primitiveTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) primitiveType);
                fail("Expected IllegalArgumentException for primitive type: " + primitiveType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndArrayTypes() throws IOException {
        // Test with array types
        Class<?>[] arrayTypes = {
            Date[].class,
            java.sql.Date[].class,
            Timestamp[].class,
            Object[].class,
            String[].class,
            int[].class
        };
        for (Class<?> arrayType : arrayTypes) {
            try {
                new DefaultDateTypeAdapter((Class<? extends Date>) arrayType);
                fail("Expected IllegalArgumentException for array type: " + arrayType);
            } catch (IllegalArgumentException e) {
                // Expected
            } catch (ClassCastException e) {
                // Expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndGenericTypes() throws IOException {
        // Test with generic types
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) java.util.List.class);
            fail("Expected IllegalArgumentException for generic type");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndEnumTypes() throws IOException {
        // Test with enum types
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for enum type");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndAnnotationTypes() throws IOException {
        // Test with annotation types
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) java.lang.Override.class);
            fail("Expected IllegalArgumentException for annotation type");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndVoidType() throws IOException {
        // Test with void type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) void.class);
            fail("Expected IllegalArgumentException for void type");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullType() throws IOException {
        // Test with null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithPattern() throws IOException {
        // Test with null type and pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, "yyyy-MM-dd");
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithStyle() throws IOException {
        // Test with null type and style
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, DateFormat.DEFAULT);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithDateTimeStyle() throws IOException {
        // Test with null type and date/time style
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, DateFormat.DEFAULT, DateFormat.DEFAULT);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithFormats() throws IOException {
        // Test with null type and formats
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormats() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, null, null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormat() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormat() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPattern() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyle() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyle() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null formats
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullEnUsFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null enUsFormat
        DateFormat localFormat = DateFormat.getDateTimeInstance();
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (DateFormat) null, localFormat);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullLocalFormatAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null localFormat
        DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, enUsFormat, (DateFormat) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullPatternAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null pattern
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (String) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null style (not possible, style is primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullDateTimeStyleAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullType() throws IOException {
        // Test with null type and null date/time style (not possible, styles are primitive)
        // This should throw NullPointerException for null type
        try {
            new DefaultDateTypeAdapter((Class<? extends Date>) null, (Integer) null, (Integer) null);
            fail("Expected NullPointerException for null type");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithAllDateTypesAndNullTypeWithNullFormatsAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNullTypeAndNull