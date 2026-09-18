package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class TypeHandlerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: org.apache.commons.cli.TypeHandler
     * 
     * Branch Coverage Targets:
     * 1. createValue(String, Object) - delegates to createValue(String, Class)
     * 2. createValue(String, Class) - 10-way branch on PatternOptionBuilder constants:
     *    - STRING_VALUE, OBJECT_VALUE, NUMBER_VALUE, DATE_VALUE, CLASS_VALUE,
     *    - FILE_VALUE, EXISTING_FILE_VALUE, FILES_VALUE, URL_VALUE, else (null)
     * 3. createObject(String) - try/catch for ClassNotFoundException, Exception on newInstance
     * 4. createNumber(String) - branch on indexOf('.') != -1, NumberFormatException catch
     * 5. createClass(String) - try/catch for ClassNotFoundException
     * 6. createDate(String) - always throws UnsupportedOperationException
     * 7. createURL(String) - try/catch for MalformedURLException
     * 8. createFile(String) - always returns new File (no branch)
     * 9. createFiles(String) - always throws UnsupportedOperationException
     * 
     * Defect Analysis (from Defects4J):
     * - PatternOptionBuilderTest::testExistingFilePattern expects that when
     *   PatternOptionBuilder.EXISTING_FILE_VALUE is used, createValue returns a
     *   FileInputStream (not a File). The defect is that createValue maps
     *   EXISTING_FILE_VALUE to createFile(str) instead of opening a FileInputStream.
     * - PatternOptionBuilderTest::testExistingFilePatternFileNotExist expects that
     *   for a non-existing file, createValue with EXISTING_FILE_VALUE returns null
     *   (because FileInputStream constructor throws FileNotFoundException).
     *   The defect is that it returns a File object instead.
     * 
     * Boundary Conditions:
     * - null String for createNumber, createClass, createURL, createFile
     * - empty string for createNumber (NumberFormatException)
     * - string with '.' for createNumber (Double)
     * - string without '.' for createNumber (Long)
     * - valid/invalid class names for createClass/createObject
     * - valid/invalid URLs for createURL
     * - null Class for createValue (should hit else branch)
     */
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testCreateValueStringObjectDelegatesToClass() throws Exception {
        // Test that the Object overload delegates correctly
        Object result = TypeHandler.createValue("test", (Object) String.class);
        assertEquals("test", result);
        assertTrue(result instanceof String);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassString() throws Exception {
        Object result = TypeHandler.createValue("hello", String.class);
        assertEquals("hello", result);
        assertTrue(result instanceof String);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassObject() throws Exception {
        Object result = TypeHandler.createValue("java.lang.StringBuilder", Object.class);
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassNumber() throws Exception {
        Object result = TypeHandler.createValue("42", Number.class);
        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertEquals(42L, result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassDate() throws Exception {
        try {
            TypeHandler.createValue("2023-01-01", Date.class);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassClass() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", Class.class);
        assertNotNull(result);
        assertEquals(String.class, result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassFile() throws Exception {
        Object result = TypeHandler.createValue("/tmp/test.txt", File.class);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals(new File("/tmp/test.txt"), result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassExistingFile() throws Exception {
        // This test targets the defect: EXISTING_FILE_VALUE should return FileInputStream
        // For a non-existing file, it should return null (per testExistingFilePatternFileNotExist)
        // For an existing file, it should return FileInputStream (per testExistingFilePattern)
        
        // Test with non-existing file - defect: returns File instead of null
        Object result = TypeHandler.createValue("non-existing.file", PatternOptionBuilder.EXISTING_FILE_VALUE);
        // Expected behavior: null (because FileInputStream constructor throws FileNotFoundException)
        // Defective behavior: returns File object
        assertNull("Expected null for non-existing file with EXISTING_FILE_VALUE", result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassFiles() throws Exception {
        try {
            TypeHandler.createValue("test", PatternOptionBuilder.FILES_VALUE);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassURL() throws Exception {
        Object result = TypeHandler.createValue("http://example.com", URL.class);
        assertNotNull(result);
        assertTrue(result instanceof URL);
        assertEquals(new URL("http://example.com"), result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueStringClassUnknownType() throws Exception {
        Object result = TypeHandler.createValue("test", StringBuilder.class);
        assertNull("Unknown type should return null", result);
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testCreateValueNullClass() throws Exception {
        Object result = TypeHandler.createValue("test", (Class<?>) null);
        assertNull("Null class should return null", result);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberWithDecimal() throws Exception {
        Number result = TypeHandler.createNumber("3.14");
        assertNotNull(result);
        assertTrue(result instanceof Double);
        assertEquals(3.14, result.doubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberWithoutDecimal() throws Exception {
        Number result = TypeHandler.createNumber("123");
        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertEquals(123L, result);
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberEmptyString() throws Exception {
        try {
            TypeHandler.createNumber("");
            fail("Expected ParseException for empty string");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberInvalidString() throws Exception {
        try {
            TypeHandler.createNumber("abc");
            fail("Expected ParseException for invalid number");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateNumberNull() throws Exception {
        try {
            TypeHandler.createNumber(null);
            fail("Expected ParseException for null");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateClassValid() throws Exception {
        Class<?> result = TypeHandler.createClass("java.lang.String");
        assertNotNull(result);
        assertEquals(String.class, result);
    }
    
    @Test(timeout = 4000)
    public void testCreateClassInvalid() throws Exception {
        try {
            TypeHandler.createClass("nonexistent.Class");
            fail("Expected ParseException for invalid class");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateClassNull() throws Exception {
        try {
            TypeHandler.createClass(null);
            fail("Expected ParseException for null class name");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateURLValid() throws Exception {
        URL result = TypeHandler.createURL("http://example.com");
        assertNotNull(result);
        assertEquals(new URL("http://example.com"), result);
    }
    
    @Test(timeout = 4000)
    public void testCreateURLInvalid() throws Exception {
        try {
            TypeHandler.createURL("not a url");
            fail("Expected ParseException for invalid URL");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateFileValid() {
        File result = TypeHandler.createFile("/tmp/test.txt");
        assertNotNull(result);
        assertEquals(new File("/tmp/test.txt"), result);
    }
    
    @Test(timeout = 4000)
    public void testCreateFileNull() {
        File result = TypeHandler.createFile(null);
        assertNotNull(result);
        assertNull(result.getPath());
    }
    
    @Test(timeout = 4000)
    public void testCreateFileEmptyString() {
        File result = TypeHandler.createFile("");
        assertNotNull(result);
        assertEquals("", result.getPath());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testExistingFilePatternWithExistingFile() throws Exception {
        // Create a temporary file to test with
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        try {
            Object result = TypeHandler.createValue(tempFile.getAbsolutePath(), PatternOptionBuilder.EXISTING_FILE_VALUE);
            // Expected: FileInputStream object
            // Defect: returns File object
            assertTrue("Expected FileInputStream for existing file, but was: " + 
                      (result == null ? "null" : result.getClass().getName()), 
                      result instanceof FileInputStream);
        } finally {
            tempFile.delete();
        }
    }
    
    @Test(timeout = 4000)
    public void testExistingFilePatternWithNonExistingFile() throws Exception {
        String nonExistingPath = "non-existing-file-" + System.nanoTime() + ".txt";
        Object result = TypeHandler.createValue(nonExistingPath, PatternOptionBuilder.EXISTING_FILE_VALUE);
        // Expected: null (FileNotFoundException should be caught)
        // Defect: returns File object
        assertNull("Expected null for non-existing file, but was: " + 
                  (result == null ? "null" : result.getClass().getName()), result);
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectValidClass() throws Exception {
        Object result = TypeHandler.createObject("java.lang.StringBuilder");
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectInvalidClass() throws Exception {
        try {
            TypeHandler.createObject("nonexistent.Class");
            fail("Expected ParseException for invalid class");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateObjectClassWithoutDefaultConstructor() throws Exception {
        try {
            TypeHandler.createObject("java.lang.Integer");
            fail("Expected ParseException for class without default constructor");
        } catch (ParseException e) {
            // expected
        }
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateDateThrowsUnsupportedOperationException() {
        TypeHandler.createDate("2023-01-01");
    }
    
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateFilesThrowsUnsupportedOperationException() {
        TypeHandler.createFiles("test");
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithNullString() throws Exception {
        Object result = TypeHandler.createValue(null, String.class);
        assertNull("Null string should return null for String type", result);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithNullStringAndNumberClass() throws Exception {
        try {
            TypeHandler.createValue(null, Number.class);
            fail("Expected ParseException for null string with Number class");
        } catch (ParseException e) {
            // expected
        }
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testTypeHandlerConstructorIsPrivate() throws Exception {
        Constructor<TypeHandler> constructor = TypeHandler.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithObjectClassAndNullString() throws Exception {
        try {
            TypeHandler.createValue(null, Object.class);
            fail("Expected ParseException for null string with Object class");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithFileClassAndNullString() throws Exception {
        Object result = TypeHandler.createValue(null, File.class);
        assertNotNull("File should be created even with null string", result);
        assertTrue(result instanceof File);
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithURLClassAndInvalidString() throws Exception {
        try {
            TypeHandler.createValue("invalid url", URL.class);
            fail("Expected ParseException for invalid URL");
        } catch (ParseException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCreateValueWithClassClassAndInvalidString() throws Exception {
        try {
            TypeHandler.createValue("nonexistent.Class", Class.class);
            fail("Expected ParseException for invalid class");
        } catch (ParseException e) {
            // expected
        }
    }
}