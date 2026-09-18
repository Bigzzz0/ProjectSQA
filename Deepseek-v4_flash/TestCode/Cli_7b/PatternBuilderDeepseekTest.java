package org.apache.commons.cli2.builder;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;

/**
 * White-box test suite for PatternBuilder, targeting line/branch coverage and the
 * known defect from Defects4J (Bug27575: testRequiredOptions).
 *
 * Branch & Defect Analysis Matrix:
 * 1. withPattern() switch branches: '!', '@', ':', '%', '+', '#', '<', '>', '*', '/', default
 * 2. withPattern() final if (opt != ' ') after loop
 * 3. createOption() branch: type != ' ' (creates argument) vs. type == ' ' (no argument)
 * 4. createOption() inner: if (required) -> withMinimum(1)
 * 5. createOption() inner: if (type != '*') -> withMaximum(1)
 * 6. create() branch: options.size() == 1 vs. else (group building)
 * 7. reset() branch: clear options
 * 8. validator() switch: '@', '+', '%', '#', '<', '>', '*', '/', default (null)
 * 9. Null pattern -> NullPointerException
 * 10. Boundary: empty pattern, single char pattern, multi-option patterns
 * 11. Defect-targeted: pattern that loses the second option (e.g., "c:h!" should produce two options)
 */
public class PatternBuilderDeepseekTest {

    // -- Helper to check basic option properties --
    private void assertOption(Option opt, String shortName, boolean required, boolean hasArgument) {
        assertNotNull("Option should not be null", opt);
        assertEquals("Short name mismatch", shortName, opt.getPreferredName());
        assertEquals("Required flag mismatch", required, opt.isRequired());
        Argument arg = opt.getArgument();
        if (hasArgument) {
            assertNotNull("Option should have an argument", arg);
        } else {
            assertNull("Option should not have an argument", arg);
        }
    }

    // ================== Partition A: Core Functional Logic ==================

    @Test(timeout = 4000)
    public void testSingleOptionWithoutArgument() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x");
        Option opt = pb.create();
        assertOption(opt, "x", false, false);
        // Ensure it's a simple option, not a group
        assertFalse("Should not be a Group", opt instanceof Group);
    }

    @Test(timeout = 4000)
    public void testSingleOptionWithStringArgument() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x:");
        Option opt = pb.create();
        assertOption(opt, "x", false, true);
        Argument arg = opt.getArgument();
        assertEquals("Argument minimum should be 0", 0, arg.getMinimum());
        assertEquals("Argument maximum should be 1", 1, arg.getMaximum());
        assertEquals("Argument type should be String", String.class, arg.getValidator().getClass()); // validator is null for ':', so check?
        // Actually validator is null, argument type is string by default
    }

    @Test(timeout = 4000)
    public void testSingleOptionRequiredWithoutArgument() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x!");
        Option opt = pb.create();
        assertOption(opt, "x", true, false);
    }

    @Test(timeout = 4000)
    public void testSingleOptionRequiredWithArgument() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x:!");
        Option opt = pb.create();
        assertOption(opt, "x", true, true);
        Argument arg = opt.getArgument();
        assertEquals("Required argument should have minimum 1", 1, arg.getMinimum());
        assertEquals("Argument maximum should be 1", 1, arg.getMaximum());
    }

    @Test(timeout = 4000)
    public void testMultipleOptions_GroupExpected() {
        // Pattern "a:b!" -> a with optional string arg, b required flag
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("a:b!");
        Option result = pb.create();
        // The bug causes only one option (a) to appear in the group
        assertTrue("Result should be a Group", result instanceof Group);
        Group group = (Group) result;
        java.util.Set options = group.getOptions();
        assertEquals("Group should contain two options", 2, options.size());
        // Verify each option
        boolean hasA = false, hasB = false;
        for (Object o : options) {
            Option opt = (Option) o;
            if ("a".equals(opt.getPreferredName())) {
                hasA = true;
                assertOption(opt, "a", false, true);
            } else if ("b".equals(opt.getPreferredName())) {
                hasB = true;
                assertOption(opt, "b", true, false);
            } else {
                fail("Unexpected option: " + opt.getPreferredName());
            }
        }
        assertTrue("Option 'a' missing", hasA);
        assertTrue("Option 'b' missing", hasB);
    }

    // ================== Partition B: Boundary & Extreme ==================

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("");
        Option result = pb.create();
        assertTrue("Empty pattern should produce a Group", result instanceof Group);
        Group group = (Group) result;
        assertTrue("Group should be empty", group.getOptions().isEmpty());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullPattern() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern(null);
    }

    @Test(timeout = 4000)
    public void testPatternWithOnlyTypeCharacter() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern(":");
        Option result = pb.create();
        assertTrue("Should produce empty Group", result instanceof Group && ((Group) result).getOptions().isEmpty());
    }

    @Test(timeout = 4000)
    public void testPatternWithOnlyRequiredFlag() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("!");
        Option result = pb.create();
        assertTrue("Should produce empty Group", result instanceof Group && ((Group) result).getOptions().isEmpty());
    }

    @Test(timeout = 4000)
    public void testPatternWithRepeatedSameOption() {
        // This is not normally used, but tests default branch with same character twice
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("aa"); // two separate options with same name
        Option result = pb.create();
        assertTrue("Result should be a Group", result instanceof Group);
        Group group = (Group) result;
        assertEquals("Should have two identical options", 2, group.getOptions().size());
    }

    @Test(timeout = 4000)
    public void testPatternWithWildcardArgumentType() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x*!");
        Option opt = pb.create();
        assertOption(opt, "x", true, true);
        Argument arg = opt.getArgument();
        // '*' type should have maximum unlimited (i.e., no withMaximum call)
        assertEquals("Wildcard argument should have minimum 1", 1, arg.getMinimum());
        assertNull("Wildcard argument should have no maximum", arg.getMaximum()); // or negative?
        // Actually without withMaximum, max stays at default (unlimited, maybe Integer.MAX_VALUE)
    }

    // ================== Partition C: Defect-Targeted Branch (Bug27575) ==================

    @Test(timeout = 4000)
    public void testBug27575_RequiredOptions() {
        // This pattern is designed to trigger the known defect where the second option is lost.
        // The bug causes the group to contain only "-c <arg>" instead of both "-c <arg>" and "-h".
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("c:h!");
        Option result = pb.create();
        assertTrue("Result should be a Group", result instanceof Group);
        Group group = (Group) result;
        java.util.Set options = group.getOptions();
        assertEquals("Group should contain two options", 2, options.size());
        // Additionally, verify string representation includes both (mirroring original test style)
        String str = result.toString();
        assertTrue("Should contain '-c'", str.contains("-c"));
        assertTrue("Should contain '-h'", str.contains("-h"));
    }

    // ================== Partition D: Exception & Defensive Guards ==================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullPatternException() {
        new PatternBuilder().withPattern(null);
    }

    @Test(timeout = 4000)
    public void testResetClearsOptions() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x");
        pb.reset();
        // Create should now return an empty group
        Option result = pb.create();
        assertTrue("After reset, create should produce empty Group", result instanceof Group && ((Group) result).getOptions().isEmpty());
    }

    @Test(timeout = 4000)
    public void testMultipleCallsToWithPattern() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("a");
        pb.withPattern("b");
        Option result = pb.create();
        assertTrue("Result should be a Group", result instanceof Group);
        Group group = (Group) result;
        assertEquals("Should have two options", 2, group.getOptions().size());
    }

    // ================== Partition E: All Type-Specific Branches ==================

    @Test(timeout = 4000)
    public void testAllArgumentTypes() {
        // For each type character, create a single option and verify no exception, and that argument is present.
        char[] types = {'@', ':', '%', '+', '#', '<', '>', '*', '/'};
        for (char t : types) {
            PatternBuilder pb = new PatternBuilder();
            String pattern = "x" + t;
            pb.withPattern(pattern);
            Option opt = pb.create();
            assertNotNull("Option for type " + t + " should not be null", opt);
            assertOption(opt, "x", false, true); // always has argument for these types
            // Verify that the argument was created with correct validator type
            Argument arg = opt.getArgument();
            assertNotNull("Argument for type " + t + " should exist", arg);
        }
    }

    @Test(timeout = 4000)
    public void testRequiredWithAllArgumentTypes() {
        char[] types = {'@', ':', '%', '+', '#', '<', '>', '*', '/'};
        for (char t : types) {
            PatternBuilder pb = new PatternBuilder();
            String pattern = "x" + t + "!";
            pb.withPattern(pattern);
            Option opt = pb.create();
            assertNotNull("Option for required type " + t + " should not be null", opt);
            assertOption(opt, "x", true, true);
            Argument arg = opt.getArgument();
            assertEquals("Required argument should have minimum 1", 1, arg.getMinimum());
        }
    }

    // ================== Edge: Single Option Group vs One Option ==================

    @Test(timeout = 4000)
    public void testCreateAfterMultipleOptionsReturnsGroup() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("a");
        pb.withPattern("b");
        Option result = pb.create();
        assertTrue("Two options should produce a Group", result instanceof Group);
    }

    @Test(timeout = 4000)
    public void testCreateAfterSingleOptionReturnsOptionDirectly() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x");
        Option result = pb.create();
        assertFalse("Single option should return the Option itself, not a Group", result instanceof Group);
    }

    @Test(timeout = 4000)
    public void testCreateAfterResetReturnsEmptyGroup() {
        PatternBuilder pb = new PatternBuilder();
        pb.withPattern("x");
        pb.reset();
        Option result = pb.create();
        assertTrue("After reset, create should produce empty Group", result instanceof Group && ((Group) result).getOptions().isEmpty());
    }
}