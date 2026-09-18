package org.apache.commons.cli;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.cli.TypeHandler
 *
 * 1. createValue(String, Class<?> / Object):
 *    - PatternOptionBuilder.STRING_VALUE -> returns str directly
 *    - PatternOptionBuilder.OBJECT_VALUE -> delegates to createObject(str)
 *    - PatternOptionBuilder.NUMBER_VALUE -> delegates to createNumber(str)
 *    - PatternOptionBuilder.DATE_VALUE -> delegates to createDate(str)
 *    - PatternOptionBuilder.CLASS_VALUE -> delegates to createClass(str)
 *    - PatternOptionBuilder.FILE_VALUE -> delegates to createFile(str)
 *    - PatternOptionBuilder.EXISTING_FILE_VALUE -> [DEFECT ZONE] Expected to open/return FileInputStream
 *      or throw ParseException if missing; defective version calls createFile(str).
 *    - PatternOptionBuilder.FILES_VALUE -> delegates to createFiles(str)
 *    - PatternOptionBuilder.URL_VALUE -> delegates to createURL(str)
 *    - Unknown / unhandled Class -> returns null
 *    - Object overload: casting obj to Class<?>
 *
 * 2. createObject(String classname):
 *    - Class.forName succeeds, newInstance() succeeds -> instantiated Object
 *    - Class.forName fails (ClassNotFoundException) -> throws ParseException
 *    - newInstance() fails (e.g., interface/abstract class / private constructor) -> throws ParseException
 *
 * 3. createNumber(String str):
 *    - str contains '.' -> Double.valueOf(str)
 *    - str does not contain '.' -> Long.valueOf(str)
 *    - NumberFormatException (invalid string format) -> throws ParseException
 *
 * 4. createClass(String classname):
 *    - Valid class name -> returns Class<?>
 *    - ClassNotFoundException -> throws ParseException
 *
 * 5. createDate(String str):
 *    - Unconditionally throws UnsupportedOperationException
 *
 * 6. createURL(String str):
 *    - Valid protocol and format -> returns java.net.URL
 *    - MalformedURLException -> throws ParseException
 *
 * 7. createFile(String str):
 *    - Returns new File(str)
 *
 * 8. createFiles(String str):
 *    - Unconditionally throws UnsupportedOperationException
 * ====================================================================================================
 */
public class TypeHandlerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateValueWithStringValue() throws Exception {
        Object result = TypeHandler.createValue("hello world", PatternOptionBuilder.STRING_VALUE);
        assertEquals(String.class, result.getClass());
        assertEquals("hello world", result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithObjectValue() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertEquals(String.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testCreateValueWithNumberValueInteger() throws Exception {
        Object result = TypeHandler.createValue("12345", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Long);
        assertEquals(12345L, ((Long) result).longValue());
    }

    @Test(timeout = 4000)
    public void testCreateValueWithNumberValueDouble() throws Exception {
        Object result = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Double);
        assertEquals(123.45, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithClassValue() throws Exception {
        Object result = TypeHandler.createValue("java.lang.Integer", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(Class.class, result.getClass());
        assertEquals(Integer.class, result);
    }

    @Test(timeout = 4000)
    public void testCreateValueWithFileValue() throws Exception {
        Object result = TypeHandler.createValue("some-arbitrary-path.txt", PatternOptionBuilder.FILE_VALUE);
        assertTrue(result instanceof File);
        assertEquals("some-arbitrary-path.txt", ((File) result).getPath());
    }

    @Test(timeout = 4000)
    public void testCreateValueWithUrlValue() throws Exception {
        Object result = TypeHandler.createValue("https://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertTrue(result instanceof URL);
        assertEquals("https://commons.apache.org", ((URL) result).toExternalForm());
    }

    @Test(timeout = 4000)
    public void testCreateValueWithObjectOverload() throws Exception {
        Object typeAsObject = PatternOptionBuilder.STRING_VALUE;
        Object result = TypeHandler.createValue("test-string", typeAsObject);
        assertEquals("test-string", result);
    }

    @Test(timeout = 4000)
    public void testCreateFile() {
        File f = TypeHandler.createFile("myFile.txt");
        assertNotNull(f);
        assertEquals("myFile.txt", f.getPath());
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidLong() throws Exception {
        Number number = TypeHandler.createNumber("987654321");
        assertTrue(number instanceof Long);
        assertEquals(987654321L, number.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidDouble() throws Exception {
        Number number = TypeHandler.createNumber("-42.5");
        assertTrue(number instanceof Double);
        assertEquals(-42.5, number.doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateClassSuccess() throws Exception {
        Class<?> clazz = TypeHandler.createClass("java.lang.String");
        assertEquals(String.class, clazz);
    }

    @Test(timeout = 4000)
    public void testCreateURLSuccess() throws Exception {
        URL url = TypeHandler.createURL("http://localhost:8080");
        assertNotNull(url);
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberZero() throws Exception {
        Number n = TypeHandler.createNumber("0");
        assertTrue(n instanceof Long);
        assertEquals(0L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeLong() throws Exception {
        Number n = TypeHandler.createNumber("-1");
        assertTrue(n instanceof Long);
        assertEquals(-1L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberLongMaxMin() throws Exception {
        Number max = TypeHandler.createNumber(String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, max.longValue());

        Number min = TypeHandler.createNumber(String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, min.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateValueUnknownClassReturnsNull() throws Exception {
        Object result = TypeHandler.createValue("some-val", Integer.class);
        assertNull(result);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (EXISTING_FILE_VALUE Faults)
    // =========================================================================

    /**
     * Ground Truth Defect: PatternOptionBuilderTest#testExistingFilePattern
     * CLI fails to return a FileInputStream when PatternOptionBuilder.EXISTING_FILE_VALUE is used.
     * The defective implementation returns a java.io.File instead of opening a FileInputStream.
     */
    @Test(timeout = 4000)
    public void testDefectExistingFilePatternReturnsFileInputStream() throws Exception {
        File tempFile = File.createTempFile("d4j_cli_test", ".tmp");
        tempFile.deleteOnExit();
        try {
            Object result = TypeHandler.createValue(tempFile.getAbsolutePath(), PatternOptionBuilder.EXISTING_FILE_VALUE);
            assertNotNull("Result should not be null for existing file", result);
            assertTrue("Expected result to be instance of FileInputStream but was: " + result.getClass().getName(),
                    result instanceof FileInputStream);
            ((FileInputStream) result).close();
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Ground Truth Defect: PatternOptionBuilderTest#testExistingFilePatternFileNotExist
     * CLI fails when the file specified by EXISTING_FILE_VALUE does not exist.
     * The defective implementation returns a File object for non-existent files instead
     * of signaling a failure (e.g. throwing ParseException).
     */
    @Test(timeout = 4000, expected = ParseException.class)
    public void testDefectExistingFilePatternFileNotExistThrowsParseException() throws Exception {
        TypeHandler.createValue("non-existing-file-" + System.nanoTime() + ".txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateObjectClassNotFound() throws Exception {
        TypeHandler.createObject("org.apache.commons.cli.NonExistentClass123");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateObjectInstantiationFailure() throws Exception {
        // List is an interface; Class.forName succeeds, but newInstance() throws InstantiationException
        TypeHandler.createObject("java.util.List");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateNumberInvalidFormat() throws Exception {
        TypeHandler.createNumber("not_a_number");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateNumberMultipleDots() throws Exception {
        TypeHandler.createNumber("1.2.3");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateClassFailure() throws Exception {
        TypeHandler.createClass("org.apache.commons.cli.UnknownClass");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateDateUnsupported() {
        TypeHandler.createDate("2023-01-01");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateValueDateUnsupported() throws Exception {
        TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testCreateURLMalformed() throws Exception {
        TypeHandler.createURL("malformed:url::string");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateFilesUnsupported() {
        TypeHandler.createFiles("some-path");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testCreateValueFilesUnsupported() throws Exception {
        TypeHandler.createValue("some-path", PatternOptionBuilder.FILES_VALUE);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeHandlerConstructor() {
        TypeHandler handler = new TypeHandler();
        assertNotNull(handler);
    }
}