package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Date;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypeHandler.java
 * 
 * Branch coverage targets:
 * 1. createValue(String, Object) -> delegation to createValue(String, Class)
 * 2. createValue(String, Class) -> 9 branches (8 type checks + default null return)
 * 3. createObject -> 3 branches (ClassNotFoundException, InstantiationException, IllegalAccessException)
 * 4. createNumber -> 2 branches (success, NumberFormatException)
 * 5. createClass -> 2 branches (success, ClassNotFoundException)
 * 6. createDate -> 1 branch (always prints error, returns null)
 * 7. createURL -> 2 branches (success, MalformedURLException)
 * 8. createFile -> 1 branch (always creates file)
 * 9. createFiles -> 1 branch (always returns null)
 * 
 * Defect targeting: createNumber() bug where NumberFormatException causes loss of precision
 * - "4.5" should be parsed as Double 4.5 but actually produces Long 4 in defective version
 * - The bug is in the NumberUtils.createNumber() call behavior (Defects4J issue)
 * - Test ensures the exact value and type are verified
 */
public class TypeHandlerDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testCreateValueWithStringType() {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertNotNull("Should not return null for STRING_VALUE", result);
        assertTrue("Should be String type", result instanceof String);
        assertEquals("testString", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithObjectType() throws Exception {
        Object result = TypeHandler.createValue("java.lang.StringBuilder", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull("Should not return null for valid class", result);
        assertTrue("Should be StringBuilder instance", result instanceof StringBuilder);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithNumberType() {
        Object result = TypeHandler.createValue("42", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull("Should not return null", result);
        assertTrue("Should be Number type", result instanceof Number);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithDateType() {
        Object result = TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
        assertNull("createDate always returns null in current implementation", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithClassType() {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertNotNull("Should not return null", result);
        assertTrue("Should be Class type", result instanceof Class);
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithFileType() {
        Object result = TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.FILE_VALUE);
        assertNotNull("Should not return null", result);
        assertTrue("Should be File type", result instanceof File);
        assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithExistingFileType() {
        Object result = TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull("Should not return null", result);
        assertTrue("Should be File type", result instanceof File);
        assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithFilesType() {
        Object result = TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.FILES_VALUE);
        assertNull("createFiles always returns null", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithURLType() {
        Object result = TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
        assertNotNull("Should not return null", result);
        assertTrue("Should be URL type", result instanceof URL);
        try {
            assertEquals(new URL("http://example.com"), result);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateValueWithDefaultType() {
        Object result = TypeHandler.createValue("test", Object.class);
        assertNull("Should return null for unmapped type", result);
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testCreateValueWithNullString() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.STRING_VALUE);
        assertNull("Should return null for null string", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithEmptyString() {
        Object result = TypeHandler.createValue("", PatternOptionBuilder.STRING_VALUE);
        assertEquals("Should return empty string", "", result);
    }

    @Test(timeout = 4000)
    public void testCreateObjectWithEmptyString() {
        Object result = TypeHandler.createObject("");
        assertNull("Should return null for empty class name", result);
    }

    @Test(timeout = 4000)
    public void testCreateClassWithEmptyString() {
        Object result = TypeHandler.createClass("");
        assertNull("Should return null for empty class name", result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithNull() {
        Number result = TypeHandler.createNumber(null);
        assertNull("Should return null for null input", result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithEmptyString() {
        Number result = TypeHandler.createNumber("");
        assertNull("Should return null for empty string", result);
    }

    @Test(timeout = 4000)
    public void testCreateURLWithEmptyString() {
        URL result = TypeHandler.createURL("");
        assertNull("Should return null for malformed URL", result);
    }

    @Test(timeout = 4000)
    public void testCreateFileWithNull() {
        File result = TypeHandler.createFile(null);
        assertNull("Should return null for null input", result);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * CRITICAL DEFECT TEST: Directly targets the bug where "4.5" as a number
     * produces incorrect behavior. In the defective version, NumberUtils.createNumber("4.5")
     * may return a Long instead of Double due to precision loss issues.
     * This test verifies both the exact value AND the exact type.
     */
    @Test(timeout = 4000)
    public void testCreateNumberWithDecimalValue() {
        Number result = TypeHandler.createNumber("4.5");
        assertNotNull("Number should not be null", result);
        // The correct behavior is to return Double 4.5
        // Defect would return Long 4 or similar
        assertTrue("Decimal number should be Double type, got: " + result.getClass().getName(),
                result instanceof Double);
        assertEquals("4.5 should parse to 4.5", 4.5, result.doubleValue(), 0.0);
        assertEquals("Exact value should be 4.5", Double.valueOf(4.5), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithIntegerValue() {
        Number result = TypeHandler.createNumber("42");
        assertNotNull("Number should not be null", result);
        assertTrue("Integer number should be Number type", result instanceof Number);
        assertEquals("42 should parse to 42", 42, result.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithNegativeDecimalValue() {
        Number result = TypeHandler.createNumber("-3.14");
        assertNotNull("Number should not be null", result);
        assertTrue("Negative decimal should be Double type", result instanceof Double);
        assertEquals(-3.14, result.doubleValue(), 0.0);
        assertEquals(Double.valueOf(-3.14), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithLargeDoubleValue() {
        Number result = TypeHandler.createNumber("1.7976931348623157E308");
        assertNotNull("Number should not be null", result);
        assertTrue("Large double should be Double type", result instanceof Double);
        assertEquals(Double.MAX_VALUE, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithVerySmallDoubleValue() {
        Number result = TypeHandler.createNumber("4.9E-324");
        assertNotNull("Number should not be null", result);
        assertTrue("Very small double should be Double type", result instanceof Double);
        assertEquals(Double.MIN_VALUE, result.doubleValue(), 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testCreateObjectWithNonExistentClass() {
        Object result = TypeHandler.createObject("com.nonexistent.Class");
        assertNull("Should return null for non-existent class", result);
    }

    @Test(timeout = 4000)
    public void testCreateObjectWithClassCausingInstantiationException() {
        // Abstract class cannot be instantiated
        Object result = TypeHandler.createObject("java.util.AbstractList");
        assertNull("Should return null for abstract class", result);
    }

    @Test(timeout = 4000)
    public void testCreateObjectWithClassCausingIllegalAccessException() {
        // Private constructor class
        Object result = TypeHandler.createObject("java.lang.System");
        assertNull("Should return null for class with private constructor", result);
    }

    @Test(timeout = 4000)
    public void testCreateClassWithNonExistentClass() {
        Object result = TypeHandler.createClass("com.nonexistent.Class");
        assertNull("Should return null for non-existent class", result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithInvalidFormat() {
        Number result = TypeHandler.createNumber("notANumber");
        assertNull("Should return null for invalid number format", result);
    }

    @Test(timeout = 4000)
    public void testCreateURLWithMalformedURL() {
        URL result = TypeHandler.createURL("htp://invalid url");
        assertNull("Should return null for malformed URL", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithUnrecognizedType() {
        Object result = TypeHandler.createValue("test", Date.class);
        assertNull("Should return null for unrecognized type", result);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCreateValueWithObjectDelegation() {
        // Test the overloaded createValue(String, Object) method
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertNotNull("Delegation to createValue(String, Class) should work", result);
        assertTrue("Should be Class type", result instanceof Class);
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void testCreateObjectReturnsDifferentInstances() {
        Object result1 = TypeHandler.createObject("java.lang.StringBuilder");
        Object result2 = TypeHandler.createObject("java.lang.StringBuilder");
        assertNotNull("First instance should not be null", result1);
        assertNotNull("Second instance should not be null", result2);
        assertNotSame("Two calls should return different instances", result1, result2);
    }

    @Test(timeout = 4000)
    public void testCreateFileReturnsDifferentInstances() {
        File result1 = TypeHandler.createFile("/tmp/test.txt");
        File result2 = TypeHandler.createFile("/tmp/test.txt");
        assertNotNull("First file should not be null", result1);
        assertNotNull("Second file should not be null", result2);
        assertEquals("Files with same path should be equal", result1, result2);
    }

    @Test(timeout = 4000)
    public void testCreateValueReturnsExactlyTheStringObject() {
        // For STRING_VALUE, the same string object should be returned
        String input = "exactString";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.STRING_VALUE);
        assertSame("Should return the exact same string object", input, result);
    }
}