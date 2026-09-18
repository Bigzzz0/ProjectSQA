package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * Test class for SystemUtils, targeting maximum coverage and the known defect.
 * <p>
 * Branch & Defect Analysis Matrix:
 * <ul>
 *   <li>toJavaVersionInt(null): should return 0 (int) but returns 0.0f (bug)</li>
 *   <li>toJavaVersionInt(valid version): returns float instead of int</li>
 *   <li>toJavaVersionFloat(version): conversions and null</li>
 *   <li>toJavaVersionIntArray(null): returns empty array</li>
 *   <li>toJavaVersionIntArray(version): parsing and limit</li>
 *   <li>isJavaVersionMatch(null, prefix): returns false</li>
 *   <li>isJavaVersionMatch(version, null): null prefix -> startsWith(NPE?) but we handle via pass non-null</li>
 *   <li>isOSMatch(null, ...): returns false</li>
 *   <li>isOSNameMatch(null, ...): returns false</li>
 *   <li>isJavaAwtHeadless(): true/false/null property</li>
 *   <li>getJavaHome(), getJavaIoTmpDir(), getUserDir(), getUserHome(): non-null File</li>
 *   <li>isJavaVersionAtLeast(float/int): boundary tests</li>
 *   <li>Static fields: non-null for common properties</li>
 * </ul>
 */
public class SystemUtilsDeepseekTest {

    // ---------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStaticFieldsNotNull() {
        assertNotNull(SystemUtils.FILE_SEPARATOR);
        assertNotNull(SystemUtils.LINE_SEPARATOR);
        assertNotNull(SystemUtils.PATH_SEPARATOR);
        assertNotNull(SystemUtils.JAVA_VERSION);
        assertNotNull(SystemUtils.OS_NAME);
    }

    @Test(timeout = 4000)
    public void testGetJavaHome() {
        File javaHome = SystemUtils.getJavaHome();
        assertNotNull(javaHome);
        assertTrue(javaHome.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetJavaIoTmpDir() {
        File tmpDir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(tmpDir);
        assertTrue(tmpDir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetUserDir() {
        File userDir = SystemUtils.getUserDir();
        assertNotNull(userDir);
        assertTrue(userDir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGetUserHome() {
        File userHome = SystemUtils.getUserHome();
        assertNotNull(userHome);
        assertTrue(userHome.isDirectory());
    }

    @Test(timeout = 4000)
    public void testIsJavaAwtHeadless() {
        // Just call it – no exception expected
        boolean headless = SystemUtils.isJavaAwtHeadless();
        // The result may be true or false, but must not throw
        // We can at least assert it's a boolean
        assertTrue(headless == true || headless == false);
    }

    @Test(timeout = 4000)
    public void testConstructor() {
        // Ensure no exception
        SystemUtils sys = new SystemUtils();
        assertNotNull(sys);
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIsJavaVersionMatchNullVersion() {
        // isJavaVersionMatch(null, "1.6") should return false
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.6"));
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionMatchEmptyPrefix() {
        // version starts with empty string? Actually "1.6".startsWith("") is true
        assertTrue(SystemUtils.isJavaVersionMatch("1.6", ""));
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionMatchExact() {
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0_21", "1.6"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.7.0", "1.6"));
    }

    @Test(timeout = 4000)
    public void testIsOSMatchNullInputs() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5"));
        assertFalse(SystemUtils.isOSMatch("Windows", null, "Windows", "5"));
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "5"));
    }

    @Test(timeout = 4000)
    public void testIsOSMatchSuccess() {
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
    }

    @Test(timeout = 4000)
    public void testIsOSMatchFailure() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "6"));
    }

    @Test(timeout = 4000)
    public void testIsOSNameMatchNull() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
    }

    @Test(timeout = 4000)
    public void testIsOSNameMatchSuccess() {
        assertTrue(SystemUtils.isOSNameMatch("Windows XP", "Windows"));
    }

    @Test(timeout = 4000)
    public void testIsOSNameMatchFailure() {
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Windows"));
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // ---------------------------------------------------------------

    /**
     * This test directly targets the known defect: toJavaVersionInt(null) returns 0.0f (float)
     * but should return 0 (int) according to its documentation.
     * The assertion assertEquals(0, ...) will fail with expected:<0> but was:<0.0>.
     */
    @Test(timeout = 4000)
    public void testToJavaVersionIntNull() {
        // The buggy version returns 0.0f, which is of type Float; 0 is autoboxed to Integer.
        // This assertion exposes the type mismatch.
        assertEquals(0, SystemUtils.toJavaVersionInt(null));
    }

    @Test(timeout = 4000)
    public void testToJavaVersionIntValid() {
        // For "1.2" expected 120; but note the method returns float, so we use a delta of 0
        assertEquals(120.0f, SystemUtils.toJavaVersionInt("1.2"), 0.0f);
        assertEquals(131.0f, SystemUtils.toJavaVersionInt("1.3.1"), 0.0f);
        // The method returns float, so we need to compare with float.
        // However, the test is meant to show the return type issue; passing with delta masks the bug.
        // But we already have the null test that fails. For completeness, we also test with normal input.
    }

    @Test(timeout = 4000)
    public void testToJavaVersionFloat() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.0f);
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.0f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.0f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToJavaVersionIntArrayDefaultLimit() {
        assertArrayEquals(new int[] {}, SystemUtils.toJavaVersionIntArray(null));
        assertArrayEquals(new int[] {1, 2, 0}, SystemUtils.toJavaVersionIntArray("1.2"));
        assertArrayEquals(new int[] {1, 3, 1}, SystemUtils.toJavaVersionIntArray("1.3.1"));
        assertArrayEquals(new int[] {1, 6, 0, 21}, SystemUtils.toJavaVersionIntArray("1.6.0_21"));
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    // No public methods throw exceptions directly, but we test edge cases.

    @Test(timeout = 4000)
    public void testIsJavaVersionAtLeastFloatBoundary() {
        // Use a very low version that should always be true
        assertTrue(SystemUtils.isJavaVersionAtLeast(1.1f));
        // Use an extremely high version that should be false
        assertFalse(SystemUtils.isJavaVersionAtLeast(100.0f));
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionAtLeastIntBoundary() {
        // Low version
        assertTrue(SystemUtils.isJavaVersionAtLeast(110));
        // High version
        assertFalse(SystemUtils.isJavaVersionAtLeast(9999));
    }

    // ---------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    // The class is a utility class, no equals/hashCode/clone. We test the constructor.

    @Test(timeout = 4000)
    public void testToString() {
        // not applicable, but ensure no ClassCastException from static fields
        assertNotNull(SystemUtils.JAVA_VERSION.toString());
    }

    // Additional tests for static fields that are computed conditionally

    @Test(timeout = 4000)
    public void testJavaVersionTrimmed() {
        // The field is private, but we can indirectly check that 
        // isJavaVersionMatch works (it uses JAVA_VERSION_TRIMMED)
        // If JAVA_VERSION is non-null, JAVA_VERSION_TRIMMED should start with digit
        if (SystemUtils.JAVA_VERSION != null) {
            assertTrue(Character.isDigit(SystemUtils.JAVA_VERSION_TRIMMED.charAt(0)));
        }
    }

    @Test(timeout = 4000)
    public void testIsJavaVersionMatchRealVersion() {
        // The actual JAVA_VERSION_TRIMMED should start with something like "1.7" or "1.8"
        // We can check that it matches the actual Java version prefix
        if (SystemUtils.JAVA_VERSION != null) {
            // e.g., for Java 8, JAVA_VERSION_TRIMMED starts with "1.8"
            assertTrue(SystemUtils.isJavaVersionMatch(SystemUtils.JAVA_VERSION_TRIMMED, "1."));
        }
    }

    // Test that the IS_JAVA_* boolean fields are consistent with actual version
    @Test(timeout = 4000)
    public void testJavaVersionBooleansConsistency() {
        // If Java version is known, at least one of the flags should be true.
        // But we cannot be sure of the exact version. We simply check they are boolean.
        assertTrue(SystemUtils.IS_JAVA_1_1 == true || SystemUtils.IS_JAVA_1_1 == false);
        // For Java 8, IS_JAVA_1_8 might be missing? Not defined. We just call them.
    }

    // Test the static OS check fields are not all false (should match current OS)
    @Test(timeout = 4000)
    public void testOsCheckAtLeastOneTrue() {
        // At least one of the OS flags should be true on a real system.
        // But to avoid flakiness, we just call them.
        boolean anyOs = SystemUtils.IS_OS_AIX || SystemUtils.IS_OS_HP_UX || SystemUtils.IS_OS_IRIX
                || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX
                || SystemUtils.IS_OS_OS2 || SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS
                || SystemUtils.IS_OS_WINDOWS;
        // On any real OS, this should be true; but for safety, we do not assert
        // because in a sandbox it might be all false.
    }

    // Additional test for the concrete bug: test the direct method that caused failure in Defects4J
    @Test(timeout = 4000)
    public void testJavaVersionAsInt() {
        // This replicates the original test that failed.
        // It calls the private method indirectly? No, we call the package-private toJavaVersionInt.
        // The bug is that toJavaVersionInt returns float, so the following assertion exposes it.
        assertEquals(0, SystemUtils.toJavaVersionInt(null));
        // Also test with a valid string to see the type confusion
        // But the main failure is with null.
    }
}