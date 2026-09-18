package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced test class for HelpFormatter targeting line/branch coverage and the known
 * defect where appendOption does not use the formatter's defaultArgName when the
 * option has an arg but no explicit arg name.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core getters/setters and default values
 * - Partition B: Boundary values (null, empty, extreme ints)
 * - Partition C: Defect-targeted test for defaultArgName usage in printUsage
 * - Partition D: Exception paths (null/empty cmdLineSyntax)
 * - Partition E: Object lifecycle (comparator, newline, padding)
 */
public class HelpFormatterDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testDefaultWidth() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
    }

    @Test(timeout = 4000)
    public void testDefaultLeftPad() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    @Test(timeout = 4000)
    public void testDefaultDescPad() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());
    }

    @Test(timeout = 4000)
    public void testSyntaxPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix("usage2: ");
        assertEquals("usage2: ", formatter.getSyntaxPrefix());
    }

    @Test(timeout = 4000)
    public void testNewLine() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
    }

    @Test(timeout = 4000)
    public void testOptPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
    }

    @Test(timeout = 4000)
    public void testLongOptPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test(timeout = 4000)
    public void testLongOptSeparator() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(" ", formatter.getLongOptSeparator());
        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());
    }

    @Test(timeout = 4000)
    public void testDefaultArgName() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        formatter.setArgName("argument");
        assertEquals("argument", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testOptionComparator() {
        HelpFormatter formatter = new HelpFormatter();
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator() instanceof Comparator);
        // set null restores default
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        // Custom comparator
        Comparator<Option> custom = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        formatter.setOptionComparator(custom);
        assertSame(custom, formatter.getOptionComparator());
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000)
    public void testWidthNegative() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(-1);
        assertEquals(-1, formatter.getWidth());
        // printUsage should handle negative width gracefully? No protect but we test boundary.
    }

    @Test(timeout = 4000)
    public void testLeftPadZero() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(0);
        assertEquals(0, formatter.getLeftPadding());
    }

    @Test(timeout = 4000)
    public void testDescPadZero() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setDescPadding(0);
        assertEquals(0, formatter.getDescPadding());
    }

    @Test(timeout = 4000)
    public void testNewLineEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("");
        assertEquals("", formatter.getNewLine());
    }

    @Test(timeout = 4000)
    public void testSyntaxPrefixNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix(null);
        assertNull(formatter.getSyntaxPrefix());
    }

    @Test(timeout = 4000)
    public void testOptPrefixNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptPrefix(null);
        assertNull(formatter.getOptPrefix());
    }

    @Test(timeout = 4000)
    public void testLongOptPrefixNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptPrefix(null);
        assertNull(formatter.getLongOptPrefix());
    }

    @Test(timeout = 4000)
    public void testLongOptSeparatorEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptSeparator("");
        assertEquals("", formatter.getLongOptSeparator());
    }

    @Test(timeout = 4000)
    public void testArgNameNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName(null);
        assertNull(formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testFindWrapPosExactWidth() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "1234567890";
        // width=10, start=0 => no wrap needed, should return -1
        assertEquals(-1, formatter.findWrapPos(text, 10, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosWithNewline() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "abc\ndef";
        // newline at position 3, width=10, start=0 => returns 4 (pos+1)
        assertEquals(4, formatter.findWrapPos(text, 10, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosWithTab() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "abc\tdef";
        // tab at position 3, width=10, start=0 => returns 4
        assertEquals(4, formatter.findWrapPos(text, 10, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosWhitespaceSearch() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "abcd efghij";
        // width=5, start=0 => last whitespace before pos=5 is at 4
        assertEquals(4, formatter.findWrapPos(text, 5, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNoWhitespaceUntilEnd() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "abcdefghij";
        // width=5, start=0 => no whitespace before or after, should return -1?
        // Actually after loop, pos=5, then while pos<=len and not whitespace, pos becomes 10, then returns (pos==len)? => -1
        assertEquals(-1, formatter.findWrapPos(text, 5, 0));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingZero() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.createPadding(0));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingNegative() {
        HelpFormatter formatter = new HelpFormatter();
        // Negative length? The method uses int len, loop from 0 to len-1, so if len<0, loop doesn't execute => empty string
        assertEquals("", formatter.createPadding(-5));
    }

    @Test(timeout = 4000)
    public void testRtrimNull() {
        HelpFormatter formatter = new HelpFormatter();
        assertNull(formatter.rtrim(null));
    }

    @Test(timeout = 4000)
    public void testRtrimEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.rtrim(""));
    }

    @Test(timeout = 4000)
    public void testRtrimNoTrailingSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("abc", formatter.rtrim("abc"));
    }

    @Test(timeout = 4000)
    public void testRtrimTrailingSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("abc", formatter.rtrim("abc   "));
    }

    @Test(timeout = 4000)
    public void testRtrimOnlySpaces() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.rtrim("   "));
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    /**
     * This test targets the known defect: when printing usage with an option that has
     * an arg but no explicit arg name, appendOption should use the formatter's defaultArgName.
     * Bug: it uses the option's own argName (which may be null) and thus prints nothing or
     * a different value.
     */
    @Test(timeout = 4000)
    public void testPrintUsageWithDefaultArgName() {
        HelpFormatter formatter = new HelpFormatter();
        // Set a custom default arg name
        formatter.setArgName("argument");

        // Create an option with an arg but no explicit arg name (so it uses default)
        Option opt = new Option("f", "foo", true, "description");
        // Ensure hasArgName returns false (since no arg name set)
        assertFalse("Option should not have arg name", opt.hasArgName());

        Options options = new Options();
        options.addOption(opt);

        // Capture output by using a PrintWriter with a StringWriter
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String usage = sw.toString();
        // Expected: "usage: app -f <argument>"
        assertTrue("Usage should contain '<argument>'", usage.contains("<argument>"));
        // The bug would produce "<arg>" or no arg at all.
    }

    // Additional variant: option with long opt only
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptDefaultArgName() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("argument");

        Option opt = new Option("f", "foo", true, "desc");
        opt.setLongOpt("foo");
        // Clear short opt? Actually we can test with long opt only: create option without short opt
        // Option can be created with just long opt? Constructor: Option(String opt, String longOpt, boolean hasArg, String description)
        Option optLong = new Option(null, "bar", true, "desc");
        Options options = new Options();
        options.addOption(optLong);

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        String usage = sw.toString();
        // With long opt and no short opt, output: --bar <argument>
        assertTrue("Usage should contain '<argument>'", usage.contains("<argument>"));
        // Bug would produce "<arg>" or missing.
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp((String) null, new Options());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("", new Options());
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsage() {
        // Ensure that autoUsage does not cause exception
        HelpFormatter formatter = new HelpFormatter();
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        Options options = new Options();
        options.addOption(new Option("a", "aa", false, "desc"));
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: app"));
    }

    @Test(timeout = 4000)
    public void testPrintOptionsWithEmptyOptions() {
        HelpFormatter formatter = new HelpFormatter();
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        Options options = new Options();
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        assertEquals("", sw.toString()); // should print nothing
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testOptionComparatorCompare() {
        Option opt1 = new Option("a", "aaa", false, ""       Option opt2 = new Option("b", "bbb", false, "");
        OptionComparator comp = new OptionComparator();
        // The comparator is private static inner class, but we can test it indirectly via getOptionComparator and using it.
        Comparator comp2 = new HelpFormatter().getOptionComparator();
        assertTrue(comp2.compare(opt1, opt2) < 0);
        assertEquals(0, comp2.compare(op1, opt1));
        assertTrue(comp2.compare(opt2, opt1) > 0);
    }

    // Helper method to instantiate OptionComparator (only via reflection? Not needed)
}