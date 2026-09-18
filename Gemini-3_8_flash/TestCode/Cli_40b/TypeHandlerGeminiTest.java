package org.apache.commons.cli;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli.TypeHandler
 *
 * Coverage Target Analysis:
 * 1. createValue(String, Object) -> Delegates to createValue(String, Class<?>) by casting.
 * 2. createValue(String, Class<T>) -> Multi-branch dispatching via PatternOptionBuilder types:
 *    - STRING_VALUE (String.class) -> Returns unmodified string.
 *    - OBJECT_VALUE (Object.class) -> Delegates to createObject(str).
 *    - NUMBER_VALUE (Number.class) -> Delegates to createNumber(str).
 *    - DATE_VALUE (Date.class) -> Delegates to createDate(str) -> UnsupportedOperationException.
 *    - CLASS_VALUE (Class.class) -> Delegates to createClass(str).
 *    - FILE_VALUE (File.class) -> Delegates to createFile(str).
 *    - EXISTING_FILE_VALUE (FileInputStream.class) -> Delegates to openFile(str).
 *    - FILES_VALUE (File[].class) -> Delegates to createFiles(str) -> UnsupportedOperationException.
 *    - URL_VALUE (URL.class) -> Delegates to createURL(str).
 *    - Fallthrough / Unknown Type -> Returns null (or throws ParseException when type is unsupported/invalid).
 * 3. createObject(String classname):
 *    - Success: instantiates class via empty constructor (e.g., ArrayList, String).
 *    - ClassNotFoundException: converted to ParseException.
 *    - Instantiation failure (abstract/private/no empty constructor): converted to ParseException.
 * 4. createNumber(String str):
 *    - Contains '.' -> Double.valueOf(str).
 *    - No '.' -> Long.valueOf(str).
 *    - Invalid formatting -> ParseException.
 * 5. createClass(String classname):
 *    - Class.forName() success -> Class instance.
 *    - ClassNotFoundException -> ParseException.
 * 6. createURL(String str):
 *    - Valid URL -> URL instance.
 *    - MalformedURLException -> ParseException.
 * 7. createFile(String str) & openFile(String str):
 *    - createFile -> File instance.
 *    - openFile -> FileInputStream or ParseException (FileNotFoundException).
 * 8. createDate(String str) & createFiles(String str):
 *    - Always throw UnsupportedOperationException.
 *
 * Defect Zone (Defects4J ground truth):
 * - TypeHandlerTest::testCreateValueInteger_failure: Calling createValue with an unsupported/invalid
 *   class like Integer.class or an unparseable integer must throw ParseException instead of silently
 *   returning null or ignoring the parsing failure.
 */
public class TypeHandlerGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateValueString() throws Exception
    {
        final Object result = TypeHandler.createValue("helloWorld", PatternOptionBuilder.STRING_VALUE);
        assertEquals("helloWorld", result);
        assertTrue(result instanceof String);
    }

    @Test(timeout = 4000)
    public void testCreateValueObject() throws Exception
    {
        final Object result = TypeHandler.createValue("java.util.ArrayList", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
    }

    @Test(timeout = 4000)
    public void testCreateValueNumberInteger() throws Exception
    {
        final Object result = TypeHandler.createValue("12345", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertEquals(12345L, ((Long) result).longValue());
    }

    @Test(timeout = 4000)
    public void testCreateValueNumberDouble() throws Exception
    {
        final Object result = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof Double);
        assertEquals(123.45, (Double) result, 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateValueClass() throws Exception
    {
        final Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void testCreateValueFile() throws Exception
    {
        final Object result = TypeHandler.createValue("pom.xml", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("pom.xml", ((File) result).getPath());
    }

    @Test(timeout = 4000)
    public void testCreateValueExistingFile() throws Exception
    {
        final File tempFile = File.createTempFile("TypeHandlerTest", ".tmp");
        tempFile.deleteOnExit();

        FileInputStream fis = null;
        try
        {
            final Object result = TypeHandler.createValue(tempFile.getAbsolutePath(), PatternOptionBuilder.EXISTING_FILE_VALUE);
            assertNotNull(result);
            assertTrue(result instanceof FileInputStream);
            fis = (FileInputStream) result;
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testCreateValueURL() throws Exception
    {
        final Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof URL);
        assertEquals("http://commons.apache.org", result.toString());
    }

    @Test(timeout = 4000)
    public void testCreateValueWithObjectPattern() throws Exception
    {
        final Object objPattern = PatternOptionBuilder.STRING_VALUE;
        final Object result = TypeHandler.createValue("testValue", objPattern);
        assertEquals("testValue", result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberNegativeLong() throws Exception
    {
        final Number result = TypeHandler.createNumber("-987654321");
        assertTrue(result instanceof Long);
        assertEquals(-987654321L, result.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroLong() throws Exception
    {
        final Number result = TypeHandler.createNumber("0");
        assertTrue(result instanceof Long);
        assertEquals(0L, result.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeDouble() throws Exception
    {
        final Number result = TypeHandler.createNumber("-0.001");
        assertTrue(result instanceof Double);
        assertEquals(-0.001, result.doubleValue(), 0.00001);
    }

    @Test(timeout = 4000)
    public void testCreateNumberBoundaryValues() throws Exception
    {
        final Number maxLong = TypeHandler.createNumber(String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, maxLong.longValue());

        final Number minLong = TypeHandler.createNumber(String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, minLong.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberLeadingDot() throws Exception
    {
        final Number result = TypeHandler.createNumber(".5");
        assertTrue(result instanceof Double);
        assertEquals(0.5, result.doubleValue(), 0.00001);
    }

    @Test(timeout = 4000)
    public void testCreateNumberTrailingDot() throws Exception
    {
        final Number result = TypeHandler.createNumber("5.");
        assertTrue(result instanceof Double);
        assertEquals(5.0, result.doubleValue(), 0.00001);
    }

    @Test(timeout = 4000)
    public void testCreateFileEmptyString()
    {
        final File file = TypeHandler.createFile("");
        assertNotNull(file);
        assertEquals("", file.getPath());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Ground Truth Defect:
     * org.apache.commons.cli.TypeHandlerTest::testCreateValueInteger_failure
     * Fails when passing an invalid string with Integer.class expecting ParseException.
     */
    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateValueInteger_failure() throws Exception
    {
        TypeHandler.createValue("just-a-string", Integer.class);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateObjectClassNotFound() throws Exception
    {
        TypeHandler.createObject("org.apache.commons.cli.NonExistingClassXYZ");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateObjectNoDefaultConstructor() throws Exception
    {
        // Integer does not have a no-arg constructor
        TypeHandler.createObject("java.lang.Integer");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateObjectAbstractClass() throws Exception
    {
        // java.util.AbstractList cannot be instantiated
        TypeHandler.createObject("java.util.AbstractList");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateNumberNotANumber() throws Exception
    {
        TypeHandler.createNumber("not_a_number");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateNumberMultipleDots() throws Exception
    {
        TypeHandler.createNumber("1.2.3");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateNumberEmptyString() throws Exception
    {
        TypeHandler.createNumber("");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateClassUnknown() throws Exception
    {
        TypeHandler.createClass("org.apache.commons.cli.UnknownClass");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCreateDateThrowsUnsupported()
    {
        TypeHandler.createDate("2023-01-01");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCreateValueDateThrowsUnsupported() throws Exception
    {
        TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateURLMalformed() throws Exception
    {
        TypeHandler.createURL("malformed://url with spaces:bad");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testOpenFileNonExisting() throws Exception
    {
        TypeHandler.openFile("non_existing_file_path_xyz_12345.txt");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testCreateValueExistingFileNonExisting() throws Exception
    {
        TypeHandler.createValue("non_existing_file_path_xyz_12345.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCreateFilesThrowsUnsupported()
    {
        TypeHandler.createFiles("path1;path2");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testCreateValueFilesThrowsUnsupported() throws Exception
    {
        TypeHandler.createValue("path1;path2", PatternOptionBuilder.FILES_VALUE);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation()
    {
        final TypeHandler handler = new TypeHandler();
        assertNotNull(handler);
    }

    @Test(timeout = 4000)
    public void testCreateValueUnregisteredClassReturnsNull() throws Exception
    {
        final Void result = TypeHandler.createValue("someString", Void.class);
        assertNull(result);
    }
}