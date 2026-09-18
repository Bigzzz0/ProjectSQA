package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Default attribute values (width, leftPad, descPad, etc.)
 *   - Getters/Setters for all attributes
 *   - setOptionComparator(null) -> reset to default
 *   - printHelp(PrintWriter, ...) with null/empty cmdLineSyntax -> IllegalArgumentException
 *   - printUsage(PrintWriter, width, app, Options) with OptionGroup
 *   - printOptions, printWrapped
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - createPadding(0) -> ""
 *   - createPadding(10) -> "          "
 *   - rtrim(null) -> null
 *   - rtrim("") -> ""
 *   - rtrim("  abc  ") -> "  abc"
 *   - rtrim("abc") -> "abc"
 *   - findWrapPos with newline at start, tab, text ends before wrap, whitespace before width, forward search, end of text
 *   - renderWrappedText with nextLineTabStop >= width -> IllegalStateException (defect trigger)
 * 
 * Partition C: Defect-Targeted Branch Zone (BugCLI162)
 *   - printHelp with small width, large leftPad/descPad, long description -> should NOT throw IllegalStateException (buggy version throws)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - cmdLineSyntax null -> IllegalArgumentException
 *   - cmdLineSyntax empty -> IllegalArgumentException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - OptionComparator sort options by key case-insensitive
 *   - renderOptions produces correct prefix ordering
 */
public class HelpFormatterDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultValues() {
        HelpFormatter hf = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, hf.defaultWidth);
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, hf.defaultLeftPad);
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, hf.defaultDescPad);
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, hf.defaultSyntaxPrefix);
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, hf.defaultOptPrefix);
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, hf.defaultLongOptPrefix);
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, hf.defaultArgName);
        assertNotNull(hf.defaultNewLine);
        assertNotNull(hf.getOptionComparator());
        assertTrue(hf.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        HelpFormatter hf = new HelpFormatter();

        hf.setWidth(50);
        assertEquals(50, hf.getWidth());
        assertEquals(50, hf.defaultWidth);

        hf.setLeftPadding(5);
        assertEquals(5, hf.getLeftPadding());
        assertEquals(5, hf.defaultLeftPad);

        hf.setDescPadding(7);
        assertEquals(7, hf.getDescPadding());
        assertEquals(7, hf.defaultDescPad);

        hf.setSyntaxPrefix("custom: ");
        assertEquals("custom: ", hf.getSyntaxPrefix());
        assertEquals("custom: ", hf.defaultSyntaxPrefix);

        hf.setNewLine("||");
        assertEquals("||", hf.getNewLine());
        assertEquals("||", hf.defaultNewLine);

        hf.setOptPrefix("+");
        assertEquals("+", hf.getOptPrefix());
        assertEquals("+", hf.defaultOptPrefix);

        hf.setLongOptPrefix("++");
        assertEquals("++", hf.getLongOptPrefix());
        assertEquals("++", hf.defaultLongOptPrefix);

        hf.setArgName("file");
        assertEquals("file", hf.getArgName());
        assertEquals("file", hf.defaultArgName);
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorNullResetsToDefault() {
        HelpFormatter hf = new HelpFormatter();
        hf.setOptionComparator(null);
        assertNotNull(hf.getOptionComparator());
        assertTrue(hf.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorCustom() {
        HelpFormatter hf = new HelpFormatter();
        Comparator custom = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        hf.setOptionComparator(custom);
        assertSame(custom, hf.getOptionComparator());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        hf.printHelp(null, opts);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        hf.printHelp("", opts);
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithOptionsAndFooter() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "enable verbose output");
        opts.addOption("o", "output", true, "output file");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printHelp(pw, 80, "myapp", "Header", opts, 1, 3, "Footer", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-v,--verbose"));
        assertTrue(output.contains("-o,--output"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpAutoUsage() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "alpha option");
        opts.addOption("b", "beta", true, "beta arg");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printHelp(pw, 60, "app", "Header", opts, 2, 4, "Footer", true);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a]"));
        assertTrue(output.contains("[-b <arg>]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroup() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        opts.addOptionGroup(group);
        opts.addOption("c", false, "standalone");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
        assertTrue(output.contains("[-c]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroup() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create('x'));
        group.addOption(OptionBuilder.create('y'));
        opts.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printUsage(pw, 80, "app", opts);
        pw.flush();
        String output = sw.toString();
        // Required group: no square brackets around group
        assertTrue(output.contains("-x | -y"));
        assertFalse(output.contains("[-x | -y]"));
    }

    @Test(timeout = 4000)
    public void testPrintOptions() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "Verbose mode");
        opts.addOption("o", "output", true, "Output file");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printOptions(pw, 80, opts, 2, 3);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-v,--verbose"));
        assertTrue(output.contains("-o,--output"));
        assertTrue(output.contains("Verbose mode"));
    }

    @Test(timeout = 4000)
    public void testPrintWrapped() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printWrapped(pw, 20, "This is a long text that should be wrapped");
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("This is a long"));
        assertTrue(output.contains("wrapped"));
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreatePadding() {
        HelpFormatter hf = new HelpFormatter();
        assertEquals("", hf.createPadding(0));
        assertEquals("     ", hf.createPadding(5));
    }

    @Test(timeout = 4000)
    public void testRtrim() {
        HelpFormatter hf = new HelpFormatter();
        assertNull(hf.rtrim(null));
        assertEquals("", hf.rtrim(""));
        assertEquals("abc", hf.rtrim("abc   "));
        assertEquals("abc", hf.rtrim("abc"));
        assertEquals("  abc", hf.rtrim("  abc  "));
        assertEquals("", hf.rtrim("   "));
    }

    @Test(timeout = 4000)
    public void testFindWrapPos() {
        HelpFormatter hf = new HelpFormatter();
        // newline at position <= width
        assertEquals(3, hf.findWrapPos("ab\ncd", 10, 0));
        // tab at position <= width
        assertEquals(4, hf.findWrapPos("ab\tcd", 10, 0));
        // text ends before wrap
        assertEquals(-1, hf.findWrapPos("short", 10, 0));
        // whitespace before width
        assertEquals(5, hf.findWrapPos("hello world", 10, 0));
        // no whitespace before width, must search forward
        assertEquals(10, hf.findWrapPos("abcdefghijklmno", 10, 0));
        // exactly at text length
        assertEquals(-1, hf.findWrapPos("abcdefghij", 10, 0));
        // startPos non-zero
        assertEquals(8, hf.findWrapPos("abc def ghi", 5, 4)); // from pos=4, "def ghi" => wrap at 8 (space)
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextNormal() {
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "This is a long line that should be wrapped at appropriate positions.";
        hf.renderWrappedText(sb, 20, 4, text);
        assertTrue(sb.length() > 0);
        // ensure no exception
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextNoWrapNeeded() {
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        hf.renderWrappedText(sb, 80, 0, "short");
        assertEquals("short", sb.toString());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testRenderWrappedTextNextLineTabStopTooLarge() {
        // This triggers the defect when nextLineTabStop >= width
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        // width=20, nextLineTabStop=20 (>= width)
        hf.renderWrappedText(sb, 20, 20, "some text that will be wrapped");
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (BugCLI162)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLongLineChunkingIndentIgnored() throws Exception {
        // This test directly targets the bug described in BugCLI162Test.
        // On the buggy version, this call throws IllegalStateException.
        // On the fixed version, it should complete normally.
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        // Create an option with a very long description to trigger wrapping
        Option option = new Option("x", "longopt", true,
            "This is a very long description that will cause wrapping when the width is small and the indent is large.");
        opts.addOption(option);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // Use small width and large leftPad/descPad to reproduce the bug condition
        hf.printHelp(pw, 30, "app", "Header", opts, 10, 15, "Footer", false);
        pw.flush();
        String output = sw.toString();
        // Ensure the output contains some expected parts
        assertTrue(output.contains("-x,--longopt"));
        // No exception should be thrown in a fixed version
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpWithPrintWriterNullCmdLineSyntax() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        hf.printHelp(new PrintWriter(System.out), 80, null, null, opts, 1, 3, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpWithPrintWriterEmptyCmdLineSyntax() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        hf.printHelp(new PrintWriter(System.out), 80, "", null, opts, 1, 3, null);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testOptionComparatorSorting() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        Option a = new Option("a", "aaa", false, "a");
        Option b = new Option("B", "bbb", false, "b");
        Option c = new Option("c", "ccc", false, "c");
        opts.addOption(b);
        opts.addOption(c);
        opts.addOption(a);

        // Get options list sorted by comparator (default case-insensitive)
        List<Option> sorted = new java.util.ArrayList<Option>(opts.getOptions());
        java.util.Collections.sort(sorted, hf.getOptionComparator());
        assertEquals("a", sorted.get(0).getOpt());
        assertEquals("B", sorted.get(1).getOpt());
        assertEquals("c", sorted.get(2).getOpt());
    }

    @Test(timeout = 4000)
    public void testRenderOptionsPrefixListOrder() {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        Option longOnly = new Option(null, "longOnly", false, "long only");
        Option shortOnly = new Option("s", null, false, "short only");
        Option both = new Option("b", "both", false, "both");
        opts.addOption(both);
        opts.addOption(shortOnly);
        opts.addOption(longOnly);

        StringBuffer sb = new StringBuffer();
        hf.renderOptions(sb, 80, opts, 1, 3);
        String output = sb.toString();
        // The order should be: long-only first, then both, then short-only
        // (since comparator sorts by key: "b" < "longOnly" < "s" case-insensitive)
        assertTrue(output.indexOf("--longOnly") < output.indexOf("-b,--both"));
        assertTrue(output.indexOf("-b,--both") < output.indexOf("-s"));
    }

    @Test(timeout = 4000)
    public void testAppendOptionGroupNonRequired() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        // Use reflection or direct StringBuffer test via printUsage
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        opts.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printUsage(pw, 80, "app", opts);
        pw.flush();
        assertTrue(sw.toString().contains("[-a | -b]"));
    }

    @Test(timeout = 4000)
    public void testAppendOptionGroupRequired() throws Exception {
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create('x'));
        group.addOption(OptionBuilder.create('y'));
        opts.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printUsage(pw, 80, "app", opts);
        pw.flush();
        assertTrue(sw.toString().contains("-x | -y"));
        assertFalse(sw.toString().contains("[-x | -y]"));
    }
}