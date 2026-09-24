package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DiagnosticGroups.java
 * 
 * Decision Branches Targeted:
 * 1. registerGroup(String, DiagnosticGroup) - normal registration path
 * 2. registerGroup(String, DiagnosticType...) - varargs registration path
 * 3. registerGroup(String, DiagnosticGroup...) - varargs group registration path
 * 4. getRegisteredGroups() - returns immutable copy of groupsByName
 * 5. forName(String) - lookup existing and non-existing names
 * 6. setWarningLevels() - iterates over list, calls forName, null check, setWarningLevel
 * 
 * Boundary Conditions:
 * - null name in forName
 * - empty string name in forName
 * - null list in setWarningLevels
 * - empty list in setWarningLevels
 * - null name in registerGroup
 * - null types/groups in registerGroup varargs
 * - Duplicate registration (overwrite behavior)
 * - Static field groupsByName shared across instances
 * 
 * Defect-Targeted Branch (Defects4J ground truth):
 * The defect involves setWarningLevels() where the forName() lookup may return null
 * for certain diagnostic group names, causing a NullPointerException or incorrect
 * warning level assignment. The testWarningGuardOrdering2 and testWarningGuardOrdering4
 * failures indicate that the ordering of warning guards or the lookup mechanism
 * fails to properly handle certain group names.
 * 
 * Key insight: The static groupsByName map is populated by static initializers,
 * but if a group name is not registered (e.g., due to class loading order issues
 * or missing registration), forName returns null and Preconditions.checkNotNull
 * throws NullPointerException. The defect may also involve the DIAGNOSTIC_GROUP_NAMES
 * string containing names that are not actually registered.
 */
public class DiagnosticGroupsDeepseekTest {

    // ================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================

    @Test(timeout = 4000)
    public void testRegisterGroupWithDiagnosticGroup() {
        DiagnosticGroup group = new DiagnosticGroup("testGroup");
        DiagnosticGroup result = DiagnosticGroups.registerGroup("testGroup1", group);
        assertNotNull("registerGroup should return the group", result);
        assertSame("Should return the same group instance", group, result);
        
        DiagnosticGroup retrieved = new DiagnosticGroups().forName("testGroup1");
        assertNotNull("Should retrieve registered group", retrieved);
        assertSame("Retrieved group should be the same", group, retrieved);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithDiagnosticTypes() {
        DiagnosticType type1 = DiagnosticType.warning("TEST_WARNING_1", "Test warning {0}");
        DiagnosticType type2 = DiagnosticType.warning("TEST_WARNING_2", "Test warning {0}");
        
        DiagnosticGroup result = DiagnosticGroups.registerGroup("testGroup2", type1, type2);
        assertNotNull("registerGroup should return the group", result);
        
        DiagnosticGroup retrieved = new DiagnosticGroups().forName("testGroup2");
        assertNotNull("Should retrieve registered group", retrieved);
        assertEquals("Group should have 2 types", 2, retrieved.getTypes().length);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithDiagnosticGroups() {
        DiagnosticGroup subGroup1 = new DiagnosticGroup("subGroup1");
        DiagnosticGroup subGroup2 = new DiagnosticGroup("subGroup2");
        
        DiagnosticGroup result = DiagnosticGroups.registerGroup("testGroup3", subGroup1, subGroup2);
        assertNotNull("registerGroup should return the group", result);
        
        DiagnosticGroup retrieved = new DiagnosticGroups().forName("testGroup3");
        assertNotNull("Should retrieve registered group", retrieved);
        assertEquals("Group should have 2 sub-groups", 2, retrieved.getGroups().length);
    }

    @Test(timeout = 4000)
    public void testGetRegisteredGroupsReturnsImmutableCopy() {
        DiagnosticGroups dg = new DiagnosticGroups();
        Map<String, DiagnosticGroup> groups = dg.getRegisteredGroups();
        
        assertNotNull("getRegisteredGroups should not return null", groups);
        
        // Verify it's immutable
        try {
            groups.put("newGroup", new DiagnosticGroup("newGroup"));
            fail("Should throw UnsupportedOperationException for immutable map");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testForNameExistingGroup() {
        DiagnosticGroups dg = new DiagnosticGroups();
        
        // Test with a known registered group (GLOBAL_THIS is registered in static initializer)
        DiagnosticGroup group = dg.forName("globalThis");
        assertNotNull("globalThis should be registered", group);
        assertSame("Should return GLOBAL_THIS", DiagnosticGroups.GLOBAL_THIS, group);
    }

    @Test(timeout = 4000)
    public void testForNameNonExistingGroup() {
        DiagnosticGroups dg = new DiagnosticGroups();
        DiagnosticGroup group = dg.forName("nonExistentGroup");
        assertNull("Non-existent group should return null", group);
    }

    @Test(timeout = 4000)
    public void testForNameNull() {
        DiagnosticGroups dg = new DiagnosticGroups();
        DiagnosticGroup group = dg.forName(null);
        assertNull("Null name should return null", group);
    }

    @Test(timeout = 4000)
    public void testForNameEmptyString() {
        DiagnosticGroups dg = new DiagnosticGroups();
        DiagnosticGroup group = dg.forName("");
        assertNull("Empty string name should return null", group);
    }

    @Test(timeout = 4000)
    public void testSetWarningLevelsWithValidGroups() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        List<String> groupNames = new ArrayList<>();
        groupNames.add("globalThis");
        groupNames.add("deprecated");
        
        dg.setWarningLevels(options, groupNames, CheckLevel.WARNING);
        
        // Verify warning levels were set (no exception thrown)
        assertEquals(CheckLevel.WARNING, options.getWarningLevel(DiagnosticGroups.GLOBAL_THIS));
        assertEquals(CheckLevel.WARNING, options.getWarningLevel(DiagnosticGroups.DEPRECATED));
    }

    @Test(timeout = 4000)
    public void testSetWarningLevelsWithEmptyList() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        List<String> groupNames = new ArrayList<>();
        
        dg.setWarningLevels(options, groupNames, CheckLevel.WARNING);
        // Should not throw any exception
    }

    // ================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================

    @Test(timeout = 4000)
    public void testRegisterGroupOverwriteExisting() {
        DiagnosticGroup group1 = new DiagnosticGroup("overwriteGroup1");
        DiagnosticGroup group2 = new DiagnosticGroup("overwriteGroup2");
        
        DiagnosticGroups.registerGroup("overwriteTest", group1);
        DiagnosticGroups.registerGroup("overwriteTest", group2);
        
        DiagnosticGroup retrieved = new DiagnosticGroups().forName("overwriteTest");
        assertNotNull("Should retrieve group", retrieved);
        assertSame("Should be the last registered group", group2, retrieved);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithNullName() {
        DiagnosticGroup group = new DiagnosticGroup("testGroup");
        try {
            DiagnosticGroups.registerGroup(null, group);
            fail("Should throw NullPointerException for null name");
        } catch (NullPointerException e) {
            // Expected - HashMap.put(null, ...) throws NPE
        }
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithNullTypes() {
        try {
            DiagnosticGroups.registerGroup("nullTypesTest", (DiagnosticType[]) null);
            fail("Should throw NullPointerException for null types");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithNullGroups() {
        try {
            DiagnosticGroups.registerGroup("nullGroupsTest", (DiagnosticGroup[]) null);
            fail("Should throw NullPointerException for null groups");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithEmptyTypes() {
        DiagnosticGroup result = DiagnosticGroups.registerGroup("emptyTypesTest");
        assertNotNull("Should return a group", result);
        
        DiagnosticGroup retrieved = new DiagnosticGroups().forName("emptyTypesTest");
        assertNotNull("Should retrieve group", retrieved);
        assertEquals("Group should have 0 types", 0, retrieved.getTypes().length);
    }

    // ================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ================================================================

    /**
     * This test targets the specific defect revealed by 
     * testWarningGuardOrdering2 and testWarningGuardOrdering4.
     * 
     * The defect occurs when setWarningLevels is called with a list of
     * diagnostic group names, and one of the names is not registered in
     * the static groupsByName map. The Preconditions.checkNotNull will
     * throw a NullPointerException instead of providing a meaningful error.
     * 
     * Additionally, the DIAGNOSTIC_GROUP_NAMES string may contain names
     * that are not actually registered, causing lookup failures.
     */
    @Test(timeout = 4000)
    public void testSetWarningLevelsWithUnregisteredGroupName() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        List<String> groupNames = new ArrayList<>();
        groupNames.add("nonExistentGroup");
        
        try {
            dg.setWarningLevels(options, groupNames, CheckLevel.WARNING);
            fail("Should throw NullPointerException for unregistered group name");
        } catch (NullPointerException e) {
            // Expected - Preconditions.checkNotNull throws NPE with message
            assertTrue("Exception message should contain the group name", 
                       e.getMessage().contains("nonExistentGroup"));
        }
    }

    /**
     * This test verifies that all names in DIAGNOSTIC_GROUP_NAMES are
     * actually registered. This is a common source of the defect.
     */
    @Test(timeout = 4000)
    public void testAllDiagnosticGroupNamesAreRegistered() {
        DiagnosticGroups dg = new DiagnosticGroups();
        String[] names = DiagnosticGroups.DIAGNOSTIC_GROUP_NAMES.split(",\\s*");
        
        for (String name : names) {
            DiagnosticGroup group = dg.forName(name.trim());
            assertNotNull("Diagnostic group '" + name + "' should be registered", group);
        }
    }

    /**
     * This test simulates the warning guard ordering issue by calling
     * setWarningLevels multiple times with different orderings.
     */
    @Test(timeout = 4000)
    public void testWarningGuardOrderingScenario() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        
        // First call with one set of groups
        List<String> groupNames1 = new ArrayList<>();
        groupNames1.add("globalThis");
        groupNames1.add("checkTypes");
        dg.setWarningLevels(options, groupNames1, CheckLevel.WARNING);
        
        // Second call with different ordering and additional groups
        List<String> groupNames2 = new ArrayList<>();
        groupNames2.add("checkTypes");
        groupNames2.add("globalThis");
        groupNames2.add("deprecated");
        dg.setWarningLevels(options, groupNames2, CheckLevel.ERROR);
        
        // Verify the final state - last call should override
        assertEquals(CheckLevel.ERROR, options.getWarningLevel(DiagnosticGroups.GLOBAL_THIS));
        assertEquals(CheckLevel.ERROR, options.getWarningLevel(DiagnosticGroups.CHECK_TYPES));
        assertEquals(CheckLevel.ERROR, options.getWarningLevel(DiagnosticGroups.DEPRECATED));
    }

    /**
     * This test verifies that the static groupsByName map is properly
     * initialized and accessible from multiple instances.
     */
    @Test(timeout = 4000)
    public void testStaticGroupsByNameSharedAcrossInstances() {
        DiagnosticGroups dg1 = new DiagnosticGroups();
        DiagnosticGroups dg2 = new DiagnosticGroups();
        
        DiagnosticGroup group = new DiagnosticGroup("sharedTestGroup");
        DiagnosticGroups.registerGroup("sharedTest", group);
        
        assertSame("Both instances should see the same group", 
                   dg1.forName("sharedTest"), dg2.forName("sharedTest"));
    }

    // ================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSetWarningLevelsWithNullOptions() {
        DiagnosticGroups dg = new DiagnosticGroups();
        List<String> groupNames = new ArrayList<>();
        groupNames.add("globalThis");
        dg.setWarningLevels(null, groupNames, CheckLevel.WARNING);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSetWarningLevelsWithNullList() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        dg.setWarningLevels(options, null, CheckLevel.WARNING);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSetWarningLevelsWithNullLevel() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        List<String> groupNames = new ArrayList<>();
        groupNames.add("globalThis");
        dg.setWarningLevels(options, groupNames, null);
    }

    @Test(timeout = 4000)
    public void testSetWarningLevelsWithMultipleCallsSameGroup() {
        DiagnosticGroups dg = new DiagnosticGroups();
        CompilerOptions options = new CompilerOptions();
        List<String> groupNames = new ArrayList<>();
        groupNames.add("globalThis");
        
        dg.setWarningLevels(options, groupNames, CheckLevel.WARNING);
        dg.setWarningLevels(options, groupNames, CheckLevel.ERROR);
        dg.setWarningLevels(options, groupNames, CheckLevel.OFF);
        
        assertEquals("Last call should win", CheckLevel.OFF, 
                     options.getWarningLevel(DiagnosticGroups.GLOBAL_THIS));
    }

    // ================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================

    @Test(timeout = 4000)
    public void testConstructorCreatesNewInstance() {
        DiagnosticGroups dg1 = new DiagnosticGroups();
        DiagnosticGroups dg2 = new DiagnosticGroups();
        
        assertNotNull("Instance should not be null", dg1);
        assertNotSame("Different instances should be created", dg1, dg2);
    }

    @Test(timeout = 4000)
    public void testGetRegisteredGroupsContainsExpectedGroups() {
        DiagnosticGroups dg = new DiagnosticGroups();
        Map<String, DiagnosticGroup> groups = dg.getRegisteredGroups();
        
        // Verify some of the statically registered groups are present
        assertTrue("Should contain globalThis", groups.containsKey("globalThis"));
        assertTrue("Should contain deprecated", groups.containsKey("deprecated"));
        assertTrue("Should contain checkTypes", groups.containsKey("checkTypes"));
        assertTrue("Should contain visibility", groups.containsKey("visibility"));
    }

    @Test(timeout = 4000)
    public void testGetRegisteredGroupsSize() {
        DiagnosticGroups dg = new DiagnosticGroups();
        Map<String, DiagnosticGroup> groups = dg.getRegisteredGroups();
        
        // Count the number of statically registered groups
        int expectedCount = 18; // Count of static final DiagnosticGroup fields
        assertTrue("Should have at least " + expectedCount + " groups", 
                   groups.size() >= expectedCount);
    }

    @Test(timeout = 4000)
    public void testForNameReturnsSameInstanceForSameName() {
        DiagnosticGroups dg = new DiagnosticGroups();
        
        DiagnosticGroup group1 = dg.forName("globalThis");
        DiagnosticGroup group2 = dg.forName("globalThis");
        
        assertSame("Should return the same instance", group1, group2);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupReturnsGroup() {
        DiagnosticGroup group = new DiagnosticGroup("returnTest");
        DiagnosticGroup result = DiagnosticGroups.registerGroup("returnTestGroup", group);
        
        assertNotNull("Should return non-null group", result);
        assertSame("Should return the input group", group, result);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithTypesReturnsGroup() {
        DiagnosticType type = DiagnosticType.warning("TEST_TYPE", "Test {0}");
        DiagnosticGroup result = DiagnosticGroups.registerGroup("returnTestTypes", type);
        
        assertNotNull("Should return non-null group", result);
        assertEquals("Group should have 1 type", 1, result.getTypes().length);
    }

    @Test(timeout = 4000)
    public void testRegisterGroupWithGroupsReturnsGroup() {
        DiagnosticGroup subGroup = new DiagnosticGroup("subGroup");
        DiagnosticGroup result = DiagnosticGroups.registerGroup("returnTestGroups", subGroup);
        
        assertNotNull("Should return non-null group", result);
        assertEquals("Group should have 1 sub-group", 1, result.getGroups().length);
    }
}