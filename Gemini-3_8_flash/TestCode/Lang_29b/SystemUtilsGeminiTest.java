package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang3.SystemUtils
 * Defect Reference: Defects4J Lang (testJavaVersionAsInt fails with expected:<0> but was:<0.0>)
 * Root Cause: SystemUtils.toJavaVersionInt(String) was declared returning float instead of int,
 *             causing callers expecting an Integer/int representation to receive a Float boxed object.
 *
 * Decision / Branch Matrix:
 * 1. toJavaVersionIntArray(String, int):
 *    - Branch: version == null -> returns ArrayUtils.EMPTY_INT_ARRAY
 *    - Branch: version != null -> splits by [^\d], filters empty substrings, respects limit
 *    - Branch: substrings with length > 0 vs == 0 (leading/trailing/consecutive delimiters)
 * 2. toVersionFloat(int[]):
 *    - Branch: javaVersions == null || length == 0 -> returns 0f
 *    - Branch: length == 1 -> returns javaVersions[0]
 *    - Branch: length > 1 -> constructs float string (javaVersions[0].javaVersions[1]...)
 *    - Branch: Float.parseFloat exception catch block -> returns 0f
 * 3. toVersionInt(int[]):
 *    - Branch: javaVersions == null || length == 0 -> returns 0
 *    - Branch: length >= 1 -> version[0] * 100
 *    - Branch: length >= 2 -> + version[1] * 10
 *    - Branch: length >= 3 -> + version[2]
 * 4. isJavaVersionMatch(String, String):
 *    - Branch: version == null -> false
 *    - Branch: version != null -> startsWith(prefix) [true/false]
 * 5. isOSMatch(String, String, String, String):
 *    - Branch: osName == null || osVersion == null -> false
 *    - Branch: osName != null && osVersion != null -> startsWith name && startsWith version
 * 6. isOSNameMatch(String, String):
 *    - Branch: osName == null -> false
 *    - Branch: osName != null -> startsWith prefix [true/false]
 * 7. isJavaAwtHeadless():
 *    - Branch: JAVA_AWT_HEADLESS == null -> false
 *    - Branch: JAVA_AWT_HEADLESS != null -> equals("true") [true/false]
 * 8. isJavaVersionAtLeast(float / int):
 *    - Branch: actual >= requiredVersion [true/false]
 */
public class SystemUtilsGeminiTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetJavaHomeDirectory() {
        File dir = SystemUtils.getJavaHome();
        assertNotNull(dir);
        assertTrue("Java home directory must exist", dir.exists());
        assertTrue("Java home path must be a directory", dir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetJavaIoTmpDirDirectory() {
        File dir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(dir);
        assertTrue("Java IO tmpdir must exist", dir.exists());
        assertTrue("Java IO tmpdir must be a directory", dir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetUserDirDirectory() {
        File dir = SystemUtils.getUserDir();
        assertNotNull(dir);
        assertTrue("User dir must exist", dir.exists());
        assertTrue("User dir must be a directory", dir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetUserHomeDirectory() {
        File dir = SystemUtils.getUserHome();
        assertNotNull(dir);
        assertTrue("User home must exist", dir.exists());
        assertTrue("User home must be a directory", dir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testIsJavaAwtHeadless() {
        boolean headless = SystemUtils.isJavaAwtHeadless();
        String headlessProp = System.getProperty("java.awt.headless");
        if (headlessProp != null && "true".equals(headlessProp)) {
            assertTrue(headless);
        } else {
            assertFalse(headless);
        }
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionAtLeastFloat() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.0f));
        assertTrue(SystemUtils.isJavaVersionAtLeast(SystemUtils.JAVA_VERSION_FLOAT));
        assertFalse(SystemUtils.isJavaVersionAtLeast(SystemUtils.JAVA_VERSION_FLOAT + 100.0f));
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionAtLeastInt() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0));
        assertTrue(SystemUtils.isJavaVersionAtLeast(SystemUtils.JAVA_VERSION_INT));
        assertFalse(SystemUtils.isJavaVersionAtLeast(SystemUtils.JAVA_VERSION_INT + 1000));
    }

    @Test(timeout = 4000)
    public void testToJavaVersionFloat() {
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.0001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.0001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.0001f);
        assertEquals(2.0f, SystemUtils.toJavaVersionFloat("2"), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testToJavaVersionIntArray() {
        assertArrayEquals(new int[]{1, 2}, SystemUtils.toJavaVersionIntArray("1.2"));
        assertArrayEquals(new int[]{1, 3, 1}, SystemUtils.toJavaVersionIntArray("1.3.1"));
        assertArrayEquals(new int[]{1, 5, 0, 21}, SystemUtils.toJavaVersionIntArray("1.5.0_21"));
        assertArrayEquals(new int[]{1, 6, 0, 20}, SystemUtils.toJavaVersionIntArray("1.6.0_20-b02"));
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testToJavaVersionIntArrayBoundary() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(null));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(""));
        assertArrayEquals(new int[]{1}, SystemUtils.toJavaVersionIntArray("1"));
        assertArrayEquals(new int[]{1, 2}, SystemUtils.toJavaVersionIntArray("..1..2.."));
        assertArrayEquals(new int[]{0}, SystemUtils.toJavaVersionIntArray("abc"));
    }

    @Test(timeout = 4000)
    public void testToJavaVersionFloatBoundary() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.0001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(""), 0.0001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat("abc"), 0.0001f);
        assertEquals(9.0f, SystemUtils.toJavaVersionFloat("9"), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionMatch() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.5"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.5.0_22", "1.5"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0", "1.6"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.6.0", "1.5"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.4.2", "1.5"));
    }

    @Test(timeout = 4000)
    public void testIsOSMatch() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "5.1"));
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "6"));
        assertFalse(SystemUtils.isOSMatch("Linux", "2.6", "Windows", "2"));
    }

    @Test(timeout = 4000)
    public void testIsOSNameMatch() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Linux", "Linux"));
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Windows"));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // -----------------------------------------------------------------------

    /**
     * Targets the defect where SystemUtils.toJavaVersionInt returned float instead of int.
     * When boxed to Object or asserted as an integer value, the defective version produces
     * a Float (0.0f) causing assertion mismatch expected:<0> but was:<0.0>.
     */
    @Test(timeout = 4000)
    public void testJavaVersionAsInt() {
        assertEquals(0, (Object) SystemUtils.toJavaVersionInt(null));
        assertEquals(0, (Object) SystemUtils.toJavaVersionInt(""));
        assertEquals(120, (Object) SystemUtils.toJavaVersionInt("1.2"));
        assertEquals(131, (Object) SystemUtils.toJavaVersionInt("1.3.1"));
        assertEquals(160, (Object) SystemUtils.toJavaVersionInt("1.6.0_20"));
        assertEquals(200, (Object) SystemUtils.toJavaVersionInt("2"));
    }

    // -----------------------------------------------------------------------
    // Partition D: System Constants & Consistency Checks
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStandardSystemPropertyConstants() {
        assertNotNull(SystemUtils.FILE_SEPARATOR);
        assertNotNull(SystemUtils.LINE_SEPARATOR);
        assertNotNull(SystemUtils.PATH_SEPARATOR);
        assertNotNull(SystemUtils.JAVA_VERSION);
        assertNotNull(SystemUtils.JAVA_VERSION_TRIMMED);
        assertTrue(SystemUtils.JAVA_VERSION_FLOAT > 0f);
        assertTrue(SystemUtils.JAVA_VERSION_INT > 0);
    }

    @Test(timeout = 4000)
    public void testJavaVersionConstantsConsistency() {
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.1"), SystemUtils.IS_JAVA_1_1);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.2"), SystemUtils.IS_JAVA_1_2);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.3"), SystemUtils.IS_JAVA_1_3);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.4"), SystemUtils.IS_JAVA_1_4);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.5"), SystemUtils.IS_JAVA_1_5);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.6"), SystemUtils.IS_JAVA_1_6);
        assertEquals(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1.7"), SystemUtils.IS_JAVA_1_7);
    }

    @Test(timeout = 4000)
    public void testOSConstantsConsistency() {
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "AIX"), SystemUtils.IS_OS_AIX);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "HP-UX"), SystemUtils.IS_OS_HP_UX);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Irix"), SystemUtils.IS_OS_IRIX);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Linux") || SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "LINUX"), SystemUtils.IS_OS_LINUX);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Mac"), SystemUtils.IS_OS_MAC);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Mac OS X"), SystemUtils.IS_OS_MAC_OSX);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "OS/2"), SystemUtils.IS_OS_OS2);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Solaris"), SystemUtils.IS_OS_SOLARIS);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "SunOS"), SystemUtils.IS_OS_SUN_OS);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Windows"), SystemUtils.IS_OS_WINDOWS);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows", "5.0"), SystemUtils.IS_OS_WINDOWS_2000);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows 9", "4.0"), SystemUtils.IS_OS_WINDOWS_95);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows 9", "4.1"), SystemUtils.IS_OS_WINDOWS_98);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows", "4.9"), SystemUtils.IS_OS_WINDOWS_ME);
        assertEquals(SystemUtils.isOSNameMatch(SystemUtils.OS_NAME, "Windows NT"), SystemUtils.IS_OS_WINDOWS_NT);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows", "5.1"), SystemUtils.IS_OS_WINDOWS_XP);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows", "6.0"), SystemUtils.IS_OS_WINDOWS_VISTA);
        assertEquals(SystemUtils.isOSMatch(SystemUtils.OS_NAME, SystemUtils.OS_VERSION, "Windows", "6.1"), SystemUtils.IS_OS_WINDOWS_7);

        boolean expectedUnix = SystemUtils.IS_OS_AIX || SystemUtils.IS_OS_HP_UX || SystemUtils.IS_OS_IRIX
                || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_SOLARIS
                || SystemUtils.IS_OS_SUN_OS;
        assertEquals(expectedUnix, SystemUtils.IS_OS_UNIX);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorPublicForTools() {
        SystemUtils utils = new SystemUtils();
        assertNotNull("SystemUtils instance must be constructible for JavaBean tools", utils);
    }
}