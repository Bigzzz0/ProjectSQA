package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

/**
 * Comprehensive white-box test suite for HelpFormatter.
 * Targets line/branch coverage and the known defect in renderWrappedText
 * where indentation is ignored during long line chunking (BugCLI162).
 */
public class HelpFormatterDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Partitions:
     * A: Core getters/setters, default values, optionComparator null reset
     * B: Boundary/Edge values: width=0, negative padding, empty strings, null arguments
     * C: Defect-targeted: long line wrapping with indent (renderWrappedText / printWrapped)
     * D: Exception paths: null/empty cmdLineSyntax, illegal arguments in printHelp
     * E: Helper methods: createPadding, rtrim, findWrapPos through public wrappers
     *
     * Known defect: In renderWrappedText, when nextLineTabStop is set and text wraps,
     * the subsequent lines are not properly indented because the padding is applied
     * before trimming, causing the indent to be lost. This test verifies that the
     * indent is preserved.
     */

    // ---------------------------------------------------------------
    // Partition A: Core getters/setters and default values
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultValues() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("defaultWidth", 74, formatter.getWidth());
        assertEquals("defaultLeftPad", 1, formatter.getLeftPadding());
        assertEquals("defaultDescPad", 3, formatter.getDescPadding());
        assertEquals("defaultSyntaxPrefix", "usage: ", formatter.getSyntaxPrefix());
        assertEquals("defaultNewLine", System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals("defaultOptPrefix", "-", formatter.getOptPrefix());
        assertEquals("defaultLongOptPrefix", "--", formatter.getLongOptPrefix());
        assertEquals("defaultArgName", "arg", formatter.getArgName());
        assertNotNull("optionComparator", formatter.getOptionComparator());
        assertTrue("optionComparator is OptionComparator", formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSetters() {
        HelpFormatter f = new HelpFormatter();
        f.setWidth(100);
        assertEquals(100, f.getWidth());
        f.setLeftPadding(5);
        assertEquals(5, f.getLeftPadding());
        f.setDescPadding(7);
        assertEquals(7, f.getDescPadding());
        f.setSyntaxPrefix("SYNTAX: ");
        assertEquals("SYNTAX: ", f.getSyntaxPrefix());
        f.setNewLine("\n");
        assertEquals("\n", f.getNewLine());
        f.setOptPrefix("/");
        assertEquals("/", f.getOptPrefix());
        f.setLongOptPrefix("//");
        assertEquals("//", f.getLongOptPrefix());
        f.setArgName("file");
        assertEquals("file", f.getArgName());
    }

    @Test(timeout = 4000)
    public void testOptionComparatorSetNull() {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(null);
        assertNotNull("should reset to default comparator", f.getOptionComparator());
        assertTrue("should be OptionComparator", f.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testOptionComparatorCustom() {
        HelpFormatter f = new HelpFormatter();
        Comparator custom = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        f.setOptionComparator(custom);
        assertSame(custom, f.getOptionComparator());
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary and extreme values
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWidthZero() {
        HelpFormatter f = new HelpFormatter();
        f.setWidth(0);
        assertEquals(0, f.getWidth());
        // Printing with width 0 may cause infinite loop or exception; we just check state.
    }

    @Test(timeout = 4000)
    public void testNegativeLeftPad() {
        HelpFormatter f = new HelpFormatter();
        f.setLeftPadding(-1);
        assertEquals(-1, f.getLeftPadding());
        // negative padding is accepted but might cause issues in rendering; we test later
    }

    @Test(timeout = 4000)
    public void testEmptyStringsInSetters() {
        HelpFormatter f = new HelpFormatter();
        f.setSyntaxPrefix("");
        assertEquals("", f.getSyntaxPrefix());
        f.setNewLine("");
        assertEquals("", f.getNewLine());
        f.setOptPrefix("");
        assertEquals("", f.getOptPrefix());
        f.setLongOptPrefix("");
        assertEquals("", f.getLongOptPrefix());
        f.setArgName("");
        assertEquals("", f.getArgName());
    }

    @Test(timeout = 4000)
    public void testNullSyntaxPrefixSetters() {
        HelpFormatter f = new HelpFormatter();
        f.setSyntaxPrefix(null);
        assertNull(f.getSyntaxPrefix());
        // This could cause NPE later, but setters accept null
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-targeted test (BugCLI162 – indent ignored)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLongLineChunkingIndentPreserved() {
        // This test directly reproduces the bug scenario:
        // A long line is wrapped with a nextLineTabStop > 0.
        // The expected behavior is that continuation lines are indented by nextLineTabStop spaces.
        HelpFormatter f = new HelpFormatter();
        String text = "This is a very long line that should be wrapped at a small width, and the continuation lines should be indented properly.";
        int width = 30;
        int nextLineTabStop = 4;  // indent of 4 spaces
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, width, nextLineTabStop, text);
        pw.flush();
        String result = out.toString();
        // The first line should not be indented (since nextLineTabStop applies from second line)
        // Subsequent lines should start with 4 spaces
        String[] lines = result.split("\\r?\\n");
        assertTrue("result should have multiple lines", lines.length > 1);
        // Check second line starts with 4 spaces
        assertTrue("second line should start with 4 spaces", lines[1].startsWith("    "));
        // The first line should not start with spaces (unless the original text had them)
        assertFalse("first line should not start with spaces", lines[0].startsWith(" "));
    }

    @Test(timeout = 4000)
    public void testLongLineChunkingIndentEqualToWidth() {
        // Edge case: nextLineTabStop >= width – the method sets nextLineTabStop = width-1 to avoid infinite loop
        HelpFormatter f = new HelpFormatter();
        String text = "A long line that must wrap.";
        int width = 20;
        int nextLineTabStop = 25; // >= width
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, width, nextLineTabStop, text);
        pw.flush();
        String result = out.toString();
        String[] lines = result.split("\\r?\\n");
        assertTrue("result should have multiple lines", lines.length > 1);
        // Second line should be indented by width-1 = 19 spaces
        assertTrue("second line should be indented by 19 spaces", lines[1].startsWith("                   "));
    }

    // ---------------------------------------------------------------
    // Partition D: Exception and defensive guard paths
    // ---------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp(null, new Options());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("", new Options());
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithNullHeaderFooter() {
        // Should not throw exception when header/footer are null
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printHelp(pw, 80, "myapp", null, options, 1, 3, null, false);
        pw.flush();
        String result = out.toString();
        assertTrue("output should contain usage", result.contains("usage:"));
        assertTrue("output should contain option -a", result.contains("-a"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyHeaderFooter() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printHelp(pw, 74, "cmd", "   ", options, 1, 3, "   ", false);
        pw.flush();
        String result = out.toString();
        // Header and footer with only spaces should be trimmed by condition header.trim().length()>0
        // But they are not printed because trim() results in empty.
        assertFalse("header with spaces should not be printed", result.contains("   "));
    }

    // ---------------------------------------------------------------
    // Partition E: Helper methods (tested indirectly via public methods)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintWrappedWithNewlines() {
        // findWrapPos should stop at newline first
        HelpFormatter f = new HelpFormatter();
        String text = "first line\nsecond line";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, 80, text);
        pw.flush();
        String result = out.toString();
        String[] lines = result.split("\\r?\\n");
        assertEquals("two lines expected", 2, lines.length);
        assertEquals("first line", "first line", lines[0].trim());
        assertEquals("second line", "second line", lines[1].trim());
    }

    @Test(timeout = 4000)
    public void testPrintWrappedWithTabs() {
        // Tab should be treated as a wrap point
        HelpFormatter f = new HelpFormatter();
        String text = "short\ttab";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, 10, text);
        pw.flush();
        String result = out.toString();
        // Since tab position is <= width, it returns pos+1, meaning the text is split after tab
        assertTrue("output should have newline after tab", result.contains("short\n") || result.contains("short\r\n"));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNoWhitespaceAtEnd() {
        // When the text ends exactly at width, should return -1
        HelpFormatter f = new HelpFormatter();
        // Test indirectly: text exactly fits width, no wrapping should occur
        String text = "1234567890";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, 10, text);
        pw.flush();
        assertEquals("no wrapping for exact length", text, out.toString().trim());
    }

    @Test(timeout = 4000)
    public void testFindWrapPosTrimsTrailingSpaces() {
        // rtrim should be called
        HelpFormatter f = new HelpFormatter();
        String text = "hello world   ";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, 80, text);
        pw.flush();
        String result = out.toString().trim();
        assertEquals("trailing spaces removed", "hello world", result);
    }

    @Test(timeout = 4000)
    public void testCreatePaddingZero() {
        HelpFormatter f = new HelpFormatter();
        // Called indirectly; we can test via reflection or check a protected method directly via a subclass
        // Since createPadding is protected, we create an anonymous subclass to expose it
        HelpFormatter sub = new HelpFormatter() {
            public String testCreatePadding(int len) {
                return createPadding(len);
            }
        };
        assertEquals("", sub.testCreatePadding(0));
        assertEquals(" ", sub.testCreatePadding(1));
        assertEquals("  ", sub.testCreatePadding(2));
    }

    @Test(timeout = 4000)
    public void testRtrimNull() {
        HelpFormatter sub = new HelpFormatter() {
            public String testRtrim(String s) {
                return rtrim(s);
            }
        };
        assertNull(sub.testRtrim(null));
        assertEquals("", sub.testRtrim(""));
        assertEquals("abc", sub.testRtrim("abc"));
        assertEquals("abc", sub.testRtrim("abc   "));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsWithLongDescription() {
        // renderOptions must handle long descriptions that wrap
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "verylongoptionname", true, "This is a very long description that should be wrapped across multiple lines since it exceeds the available width.");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printOptions(pw, 40, options, 2, 4);
        pw.flush();
        String result = out.toString();
        assertTrue("output should contain option", result.contains("-x"));
        assertTrue("output should contain description", result.contains("very long description"));
        // Ensure that wrapping occurred (result has more than one line)
        int lineCount = result.split("\\r?\\n").length;
        assertTrue("description should wrap into multiple lines", lineCount > 1);
    }

    // ---------------------------------------------------------------
    // Additional branch coverage for printUsage and appendOption/appendOptionGroup
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupRequired() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.withLongOpt("alpha").create('a'));
        group.addOption(OptionBuilder.withLongOpt("beta").create('b'));
        options.addOptionGroup(group);
        options.addOption("c", "gamma", false, "optional");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        assertTrue("should contain usage prefix", result.startsWith("usage:"));
        // Required group should not be wrapped in brackets
        assertTrue("required group should not have brackets", result.contains("-a | -b"));
        assertFalse("should not have outer brackets for required group", result.contains("[-a | -b]"));
        // Optional option c should be in brackets
        assertTrue("optional option should be in brackets", result.contains("[-c]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupNotRequired() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(OptionBuilder.withLongOpt("xray").create('x'));
        group.addOption(OptionBuilder.withLongOpt("yankee").create('y'));
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printUsage(pw, 80, "app", options);
        pw.flush();
        String result = out.toString();
        assertTrue("non-required group should be in brackets", result.contains("[-x | -y]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionArg() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        Option opt = OptionBuilder.withLongOpt("file").hasArg().withArgName("path").create('f');
        options.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printUsage(pw, 80, "tool", options);
        pw.flush();
        String result = out.toString();
        assertTrue("should contain argument name", result.contains("<path>"));
        assertTrue("optional option should be in brackets", result.contains("[-f <path>]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOption() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        Option opt = OptionBuilder.withLongOpt("input").isRequired().hasArg().withArgName("infile").create('i');
        options.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printUsage(pw, 80, "tool", options);
        pw.flush();
        String result = out.toString();
        // Required option should not be in brackets
        assertTrue("required option not in brackets", result.contains("-i <infile>"));
        assertFalse("should not have brackets around required", result.contains("[-i <infile>]"));
    }

    @Test(timeout = 4000)
    public void testPrintOptionsWithNullDescription() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("z", "zulu", false, null);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printOptions(pw, 80, options, 2, 3);
        pw.flush();
        String result = out.toString();
        assertTrue("should contain option -z", result.contains("-z"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpAutoUsage() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printHelp(pw, 74, "prog", "Header", options, 1, 3, "Footer", true);
        pw.flush();
        String result = out.toString();
        assertTrue("should contain auto usage", result.contains("usage: prog [-a <arg>]"));
        assertTrue("should contain header", result.contains("Header"));
        assertTrue("should contain footer", result.contains("Footer"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsWithLongOptOnly() {
        // option with no short opt
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("longonly").create());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String result = out.toString();
        assertTrue("should contain long opt prefix", result.contains("--longonly"));
    }

    // ---------------------------------------------------------------
    // Additional boundary for findWrapPos: long word that exceeds width
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintWrappedLongWordExceedsWidth() {
        HelpFormatter f = new HelpFormatter();
        String text = "supercalifragilisticexpialidocious";
        int width = 10;
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, width, text);
        pw.flush();
        String result = out.toString();
        // Since no whitespace, it should wrap at exactly width and not find a break, then fallback to looking forward.
        // The result should contain the whole word (no breaking) because no whitespace found? Actually findWrapPos will return width if no whitespace after startPos+width? Let's see logic: if pos > startPos then return pos else look forward. It will look forward and find end of text -> return -1. So it should print the whole word.
        assertTrue("long word should be printed entirely", result.contains("supercalifragilisticexpialidocious"));
    }

    // ---------------------------------------------------------------
    // Edge case in renderWrappedText: when pos == nextLineTabStop-1, set pos = width
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRenderWrappedTextSpecialCasePosEqualsIndentMinusOne() {
        // This triggers the condition: (text.length() > width) && (pos == nextLineTabStop - 1)
        HelpFormatter f = new HelpFormatter();
        // Construct text such that the first wrap position falls exactly at nextLineTabStop-1
        // For example: width=10, nextLineTabStop=5, text length >10, and the first whitespace appears at position 4.
        String text = "1234 67890 1234567890";
        int width = 10;
        int nextLineTabStop = 5;
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        f.printWrapped(pw, width, nextLineTabStop, text);
        pw.flush();
        String result = out.toString();
        // We just ensure it doesn't throw and produces multiple lines.
        assertTrue("result should have newlines", result.contains("\n") || result.contains("\r\n"));
    }
}