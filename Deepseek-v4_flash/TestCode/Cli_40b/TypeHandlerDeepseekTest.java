package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class TypeHandlerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: TypeHandler (static utility class)
     * 
     * Decision branches to cover:
     * 1. createValue(String, Object) - delegates to createValue(String, Class)
     * 2. createValue(String, Class) - 10-way branch on PatternOptionBuilder constants:
     *    - STRING_VALUE, OBJECT_VALUE, NUMBER_VALUE, DATE_VALUE, CLASS_VALUE,
     *      FILE_VALUE, EXISTING_FILE_VALUE, FILES_VALUE, URL_VALUE, else (null)
     * 3. createObject(String) - try-catch for ClassNotFoundException, then try-catch for instantiation
     * 4. createNumber(String) - branch on presence of '.', catch NumberFormatException
     * 5. createClass(String) - try-catch for ClassNotFoundException
     * 6. createDate(String) - always throws UnsupportedOperationException
     * 7. createURL(String) - try-catch for MalformedURLException
     * 8. createFile(String) - always returns new File
     * 9. openFile(String) - try-catch for FileNotFoundException
     * 10. createFiles(String) - always throws UnsupportedOperationException
     * 
     * Boundary conditions:
     * - null String for createValue, createObject, createNumber, createClass, createDate, createURL, createFile, openFile
     * - empty string for createNumber, createURL, createFile
     * - string with '.' at start/end for createNumber
     * - non-numeric strings for createNumber
     * - valid/invalid class names for createObject/createClass
     * - valid/invalid URLs for createURL
     * - non-existent files for openFile
     * 
     * Known defect (from Defects4J):
     * - testCreateValueInteger_failure: Expected ParseException when creating Integer value
     *   The bug is that createValue with Integer.class (or NUMBER_VALUE) should throw ParseException
     *   for invalid number strings, but the current implementation may not handle it correctly.
     *   Specifically, when str is not a valid number, createNumber should throw ParseException,
     *   but the defect may cause it to return null or throw a different exception.
     *   We target this by testing createValue with NUMBER_VALUE and invalid number strings.
     */
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testCreateValueStringType() throws ParseException {
        // Test STRING_VALUE
        Object result = TypeHandler.createValue("test", PatternOptionBuilder.STRING_VALUE);
        assertEquals("test", result);
        assertTrue(result instanceof String);
        
        // Test OBJECT_VALUE with valid class
        result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertTrue(result instanceof String);
        assertEquals("", ((String) result));
        
        // Test NUMBER_VALUE with valid long
        result = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Long);
        assertEquals(123L, result);
        
        // Test NUMBER_VALUE with valid double
        result = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Double);
        assertEquals(123.45, ((Double) result), 0.001);
        
        // Test CLASS_VALUE
        result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
        
        // Test FILE_VALUE
        result = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        assertTrue(result instanceof File);
        assertEquals(new File("test.txt"), result);
        
        // Test URL_VALUE
        result = TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
        assertTrue(result instanceof URL);
        assertEquals(new URL("http://example.com"), result);
        
        // Test unknown type returns null
        assertNull(TypeHandler.createValue("test", Object.class));
    }
    
    @Test(timeout = 4000)
    public void testCreateValueObjectOverload() throws ParseException {
        Object result = TypeHandler.createValue("test", (Object) PatternOptionBuilder.STRING_VALUE);
        assertEquals("test", result);
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectValidClass() throws ParseException {
        Object obj = TypeHandler.createObject("java.lang.StringBuilder");
        assertNotNull(obj);
        assertTrue(obj instanceof StringBuilder);
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectClassNotFound() {
        try {
            TypeHandler.createObject("nonexistent.ClassName");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertEquals("Unable to find the class: nonexistent.ClassName", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectInstantiationFailure() {
        // Test with a class that has no default constructor
        try {
            TypeHandler.createObject("java.lang.Integer");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Unable to create an instance of"));
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberLong() throws ParseException {
        Number num = TypeHandler.createNumber("123");
        assertTrue(num instanceof Long);
        assertEquals(123L, num);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberDouble() throws ParseException {
        Number num = TypeHandler.createNumber("123.45");
        assertTrue(num instanceof Double);
        assertEquals(123.45, num.doubleValue(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberInvalid() {
        try {
            TypeHandler.createNumber("abc");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateClassValid() throws ParseException {
        Class<?> clazz = TypeHandler.createClass("java.lang.String");
        assertEquals(String.class, clazz);
    }
    
    @Test(timeout = 4000)
    public void testCreateClassInvalid() {
        try {
            TypeHandler.createClass("nonexistent.Class");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertEquals("Unable to find the class: nonexistent.Class", e.getMessage());
        }
    }
    
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateDateUnsupported() {
        TypeHandler.createDate("2023-01-01");
    }
    
    @Test(timeout = 4000)
    public void testCreateURLValid() throws ParseException {
        URL url = TypeHandler.createURL("http://example.com");
        assertEquals(new URL("http://example.com"), url);
    }
    
    @Test(timeout = 4000)
    public void testCreateURLInvalid() {
        try {
            TypeHandler.createURL("not a url");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertEquals("Unable to parse the URL: not a url", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateFile() {
        File file = TypeHandler.createFile("test.txt");
        assertNotNull(file);
        assertEquals(new File("test.txt"), file);
    }
    
    @Test(timeout = 4000)
    public void testOpenFileValid() throws ParseException, IOException {
        // Create a temporary file
        File tempFile = File.createTempFile("test", ".txt");
        try {
            FileInputStream fis = TypeHandler.openFile(tempFile.getAbsolutePath());
            assertNotNull(fis);
            fis.close();
        } finally {
            tempFile.delete();
        }
    }
    
    @Test(timeout = 4000)
    public void testOpenFileInvalid() {
        try {
            TypeHandler.openFile("/nonexistent/path/to/file.txt");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertEquals("Unable to find file: /nonexistent/path/to/file.txt", e.getMessage());
        }
    }
    
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateFilesUnsupported() {
        TypeHandler.createFiles("test");
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testCreateValueNullString() throws ParseException {
        // Test with null string for various types
        assertNull(TypeHandler.createValue(null, PatternOptionBuilder.STRING_VALUE));
        
        try {
            TypeHandler.createValue(null, PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for null number");
        } catch (ParseException e) {
            // Expected - NullPointerException might be thrown, but should be ParseException
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueNullClass() throws ParseException {
        // Null class should return null
        assertNull(TypeHandler.createValue("test", (Class<?>) null));
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberEmptyString() {
        try {
            TypeHandler.createNumber("");
            fail("Expected ParseException for empty string");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberDotOnly() {
        try {
            TypeHandler.createNumber(".");
            fail("Expected ParseException for dot only");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberLeadingDot() throws ParseException {
        Number num = TypeHandler.createNumber(".5");
        assertTrue(num instanceof Double);
        assertEquals(0.5, num.doubleValue(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberTrailingDot() throws ParseException {
        Number num = TypeHandler.createNumber("5.");
        assertTrue(num instanceof Double);
        assertEquals(5.0, num.doubleValue(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberMaxLong() throws ParseException {
        Number num = TypeHandler.createNumber(String.valueOf(Long.MAX_VALUE));
        assertTrue(num instanceof Long);
        assertEquals(Long.MAX_VALUE, num);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberMinLong() throws ParseException {
        Number num = TypeHandler.createNumber(String.valueOf(Long.MIN_VALUE));
        assertTrue(num instanceof Long);
        assertEquals(Long.MIN_VALUE, num);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberOverflow() {
        try {
            TypeHandler.createNumber("9223372036854775808"); // Long.MAX_VALUE + 1
            fail("Expected ParseException for overflow");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateURLNull() {
        try {
            TypeHandler.createURL(null);
            fail("Expected ParseException for null URL");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateFileNull() {
        File file = TypeHandler.createFile(null);
        assertNotNull(file);
        assertNull(file.getPath());
    }
    
    @Test(timeout = 4000)
    public void testOpenFileNull() {
        try {
            TypeHandler.openFile(null);
            fail("Expected ParseException for null file");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect-targeted test: This test directly targets the known defect where
     * createValue with NUMBER_VALUE and an invalid number string should throw
     * ParseException, but the defect causes it to fail.
     * 
     * The defect is: testCreateValueInteger_failure - Expected exception: 
     * org.apache.commons.cli.ParseException
     */
    @Test(timeout = 4000)
    public void testCreateValueInteger_failure() {
        // This is the exact test case from Defects4J that fails
        // The bug is that createValue with NUMBER_VALUE and invalid string
        // should throw ParseException, but it doesn't in the defective version
        try {
            TypeHandler.createValue("invalid-number", PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for invalid number in createValue");
        } catch (ParseException e) {
            // Expected - this is the correct behavior
            assertNotNull(e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueIntegerWithNullString() {
        // Testing with null string for NUMBER_VALUE
        try {
            TypeHandler.createValue(null, PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for null number string");
        } catch (ParseException e) {
            // Expected - should throw ParseException, not NullPointerException
        } catch (NullPointerException e) {
            fail("Should not throw NullPointerException, should throw ParseException");
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueIntegerWithEmptyString() {
        try {
            TypeHandler.createValue("", PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for empty number string");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueIntegerWithSpecialChars() {
        try {
            TypeHandler.createValue("12a34", PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for invalid number format");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueIntegerWithSpaces() {
        try {
            TypeHandler.createValue(" 123 ", PatternOptionBuilder.NUMBER_VALUE);
            fail("Expected ParseException for number with spaces");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testCreateValueWithUnsupportedType() throws ParseException {
        // Test with a type that is not in the PatternOptionBuilder
        Object result = TypeHandler.createValue("test", Integer.class);
        assertNull(result);
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectWithNullClassname() {
        try {
            TypeHandler.createObject(null);
            fail("Expected ParseException for null classname");
        } catch (ParseException e) {
            // Expected - Class.forName(null) throws NullPointerException, but should be caught
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectWithEmptyClassname() {
        try {
            TypeHandler.createObject("");
            fail("Expected ParseException for empty classname");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateClassWithNullClassname() {
        try {
            TypeHandler.createClass(null);
            fail("Expected ParseException for null classname");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateURLWithMalformedURL() {
        try {
            TypeHandler.createURL("htp://invalid");
            fail("Expected ParseException for malformed URL");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testOpenFileWithDirectory() throws IOException {
        // Create a temporary directory
        File tempDir = File.createTempFile("test", ".dir");
        tempDir.delete();
        tempDir.mkdir();
        try {
            // Opening a directory as FileInputStream should fail on most systems
            try {
                TypeHandler.openFile(tempDir.getAbsolutePath());
                // On some systems this might succeed, so we don't fail
            } catch (ParseException e) {
                // Expected on most systems
            }
        } finally {
            tempDir.delete();
        }
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testTypeHandlerConstructorIsPrivate() throws Exception {
        // Test that the constructor is private (utility class pattern)
        Constructor<TypeHandler> constructor = TypeHandler.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
        
        // Test that we can't instantiate it
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Should not be able to instantiate utility class");
        } catch (Exception e) {
            // Expected - either IllegalAccessException or InvocationTargetException
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithAllPatternTypes() throws ParseException {
        // Test all PatternOptionBuilder types to ensure no unexpected exceptions
        // STRING_VALUE
        assertEquals("test", TypeHandler.createValue("test", PatternOptionBuilder.STRING_VALUE));
        
        // OBJECT_VALUE - will try to instantiate
        try {
            Object obj = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
            assertNotNull(obj);
        } catch (ParseException e) {
            // Expected if class can't be instantiated
        }
        
        // NUMBER_VALUE
        try {
            Object num = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
            assertNotNull(num);
        } catch (ParseException e) {
            fail("Should not throw ParseException for valid number");
        }
        
        // DATE_VALUE - always throws UnsupportedOperationException
        try {
            TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
            fail("Expected UnsupportedOperationException for DATE_VALUE");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // CLASS_VALUE
        try {
            Object clazz = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
            assertEquals(String.class, clazz);
        } catch (ParseException e) {
            fail("Should not throw ParseException for valid class");
        }
        
        // FILE_VALUE
        Object file = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        assertTrue(file instanceof File);
        
        // EXISTING_FILE_VALUE - may throw ParseException if file doesn't exist
        try {
            Object fis = TypeHandler.createValue("test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
            if (fis != null) {
                assertTrue(fis instanceof FileInputStream);
                ((FileInputStream) fis).close();
            }
        } catch (ParseException e) {
            // Expected if file doesn't exist
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
        
        // FILES_VALUE - always throws UnsupportedOperationException
        try {
            TypeHandler.createValue("test", PatternOptionBuilder.FILES_VALUE);
            fail("Expected UnsupportedOperationException for FILES_VALUE");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // URL_VALUE
        try {
            Object url = TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
            assertTrue(url instanceof URL);
        } catch (ParseException e) {
            fail("Should not throw ParseException for valid URL");
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithUnknownClass() throws ParseException {
        // Test with a class that is not in PatternOptionBuilder
        Object result = TypeHandler.createValue("test", String.class);
        assertNull("Should return null for unknown class type", result);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberWithScientificNotation() throws ParseException {
        // Test scientific notation
        Number num = TypeHandler.createNumber("1.23e10");
        assertTrue(num instanceof Double);
        assertEquals(1.23e10, num.doubleValue(), 0.001);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberWithHexFormat() {
        try {
            TypeHandler.createNumber("0x1F");
            fail("Expected ParseException for hex format");
        } catch (ParseException e) {
            // Expected - hex not supported by Long.valueOf
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateURLWithProtocol() throws ParseException {
        URL url = TypeHandler.createURL("ftp://example.com/file");
        assertNotNull(url);
        assertEquals("ftp", url.getProtocol());
    }
    
    @Test(timeout = 4000)
    public void testCreateFileWithPath() {
        File file = TypeHandler.createFile("/tmp/test.txt");
        assertNotNull(file);
        assertEquals("/tmp/test.txt", file.getPath());
    }
    
    @Test(timeout = 4000)
    public void testOpenFileWithExistingFile() throws IOException, ParseException {
        // Create a temporary file
        File tempFile = File.createTempFile("test", ".txt");
        try {
            FileInputStream fis = TypeHandler.openFile(tempFile.getAbsolutePath());
            assertNotNull(fis);
            assertTrue(fis.getFD().valid());
            fis.close();
        } finally {
            tempFile.delete();
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectWithInterface() {
        // Interfaces cannot be instantiated
        try {
            TypeHandler.createObject("java.lang.Runnable");
            fail("Expected ParseException for interface");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectWithAbstractClass() {
        // Abstract classes cannot be instantiated
        try {
            TypeHandler.createObject("java.lang.AbstractStringBuilder");
            fail("Expected ParseException for abstract class");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectWithPrimitiveType() {
        // Primitive types cannot be found by Class.forName
        try {
            TypeHandler.createObject("int");
            fail("Expected ParseException for primitive type");
        } catch (ParseException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateClassWithArrayType() throws ParseException {
        // Array types can be created
        Class<?> clazz = TypeHandler.createClass("[Ljava.lang.String;");
        assertTrue(clazz.isArray());
    }
    
    @Test(timeout = 4000)
    public void testCreateURLWithLocalhost() throws ParseException {
        URL url = TypeHandler.createURL("http://localhost:8080/path");
        assertNotNull(url);
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
    }
    
    @Test(timeout = 4000)
    public void testCreateFileWithEmptyPath() {
        File file = TypeHandler.createFile("");
        assertNotNull(file);
        assertEquals("", file.getPath());
    }
    
    @Test(timeout = 4000)
    public void testOpenFileWithEmptyPath() {
        try {
            TypeHandler.openFile("");
            fail("Expected ParseException for empty file path");
        } catch (ParseException e) {
            // Expected
        }
    }
}