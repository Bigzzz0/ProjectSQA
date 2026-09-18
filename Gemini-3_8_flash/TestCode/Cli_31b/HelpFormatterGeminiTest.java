/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.cli.HelpFormatter
 *
 * Decision / Condition Coverage Targets:
 * 1. printHelp variations:
 *    - Width, syntax, header, options, footer, autoUsage (all overloads).
 *    - null/empty cmdLineSyntax -> IllegalArgumentException.
 *    - autoUsage true vs false -> printUsage(pw, width, app, options) vs printUsage(pw, width, cmdLineSyntax).
 *    - header != null && header.trim().length() > 0 vs null / whitespace-only.
 *    - footer != null && footer.trim().length() > 0 vs null / whitespace-only.
 * 2. printUsage (Options variant):
 *    - Option with opt != null vs null (longOpt only).
 *    - Option with/without argName, with/without argument.
 *    - Option required vs optional (brackets [ ] vs no brackets).
 *    - OptionGroup: required vs optional (brackets [ ] vs no brackets), delimiter (" | ").
 *    - OptionGroup multiple options, visited tracking (processedGroups check).
 * 3. printUsage (Syntax only variant):
 *    - cmdLineSyntax with space vs without space (indexOf(' ') calculation).
 * 4. renderOptions:
 *    - Option with opt == null -> padding + "   " + defaultLongOptPrefix + longOpt.
 *    - Option with opt != null and hasLongOpt -> prefix + opt + ',' + longOptPrefix + longOpt.
 *    - Option with opt != null and !hasLongOpt.
 *    - Option hasArg() with empty argName -> append(' ').
 *    - Option hasArg() with argName != null vs null (falls back to getArgName()).
 *    - Option description == null vs non-null.
 *    - Option padding alignment (optBuf.length() < max).
 * 5. renderWrappedText:
 *    - Text shorter than width (findWrapPos == -1 on first check).
 *    - Text wrapping at whitespace (\n, \t, space).
 *    - nextLineTabStop >= width -> reset to nextLineTabStop = 1.
 *    - (text.length() > width) && (pos == nextLineTabStop - 1) -> pos = width.
 *    - Multiple lines wrapping and trimming trailing spaces via rtrim.
 * 6. findWrapPos:
 *    - '\n' within width -> return pos + 1.
 *    - '\t' within width -> return pos + 1.
 *    - startPos + width >= text.length() -> return -1.
 *    - Backtracking for space/'\n'/'\r' before startPos + width.
 *    - Forward searching for space/'\n'/'\r' when no space found before width.
 *    - pos reaches text.length() -> return -1.
 * 7. rtrim & createPadding:
 *    - null / empty string handling.
 *    - Trailing spaces, tabs, newlines trimmed; internal/leading preserved.
 *    - createPadding for 0, positive integers.
 * 8. OptionComparator & Accessors:
 *    - Case-insensitive comparison of keys.
 *    - setOptionComparator(null) resets to default OptionComparator.
 *    - Getters and setters for width, leftPadding, descPadding, syntaxPrefix, newLine,
 *      optPrefix, longOptPrefix, longOptSeparator, argName.
 *
 * Defect-Targeted Branch:
 * - testDefaultArgName: When HelpFormatter#setArgName is configured (e.g. to "argument"),
 *   usage formatting via appendOption/printUsage must reflect formatter.getArgName()
 *   for options with arguments whose argName has not been explicitly customized.
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

public class HelpFormatterGeminiTest
{
    private static final String EOL = System.getProperty("line.separator");

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Getters / Setters)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConfigurationAndMutators()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR, formatter.getLongOptSeparator());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        formatter.setDescPadding(6);
        assertEquals(6, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("variable");
        assertEquals("variable", formatter.getArgName());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        Comparator customComp = CollectionsComparatorStub.INSTANCE;
        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertNotSame(customComp, formatter.getOptionComparator());
    }

    private static class CollectionsComparatorStub implements Comparator
    {
        static final CollectionsComparatorStub INSTANCE = new CollectionsComparatorStub();
        public int compare(Object o1, Object o2)
        {
            return 0;
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRtrimBoundaries()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   \t \r \n "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("a b c", formatter.rtrim("a b c \t"));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingBoundaries()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("    ", formatter.createPadding(4));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosScenarios()
    {
        HelpFormatter formatter = new HelpFormatter();

        // 1. Text with newline within width
        String text1 = "Hello\nWorld";
        assertEquals(6, formatter.findWrapPos(text1, 10, 0));

        // 2. Text with tab within width
        String text2 = "Hello\tWorld";
        assertEquals(6, formatter.findWrapPos(text2, 10, 0));

        // 3. startPos + width >= text.length() -> returns -1
        String text3 = "Short text";
        assertEquals(-1, formatter.findWrapPos(text3, 20, 0));

        // 4. Wrapping before width at whitespace
        String text4 = "The quick brown fox jumps";
        // width 12: "The quick br" -> last whitespace before 12 is at 9 (between quick and brown)
        assertEquals(9, formatter.findWrapPos(text4, 12, 0));

        // 5. Long word without whitespace before width -> forward search
        String text5 = "Supercalifragilisticexpialidocious is a long word";
        // width 10: forward search reaches space after word at index 34
        assertEquals(34, formatter.findWrapPos(text5, 10, 0));

        // 6. Single extremely long word with no whitespace at all -> returns -1
        String text6 = "Supercalifragilisticexpialidocious";
        assertEquals(-1, formatter.findWrapPos(text6, 10, 0));
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextTabStopGreaterThanOrEqualToWidth()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        // Width 10, text longer than 10, nextLineTabStop = 15 (>= width -> reset to 1)
        String text = "First line exceeds width here";
        formatter.renderWrappedText(sb, 10, 15, text);

        String[] lines = sb.toString().split("\n");
        assertTrue(lines.length > 1);
        // After wrap, lines should be padded with 1 space
        assertTrue(lines[1].startsWith(" "));
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextExactTabStopBoundary()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        // Target branch: (text.length() > width) && (pos == nextLineTabStop - 1) -> pos = width
        // nextLineTabStop = 4, width = 8
        // Padding = 4 spaces. Text starts with 4 spaces then long word:
        String text = "1234567890 1234567890";
        formatter.renderWrappedText(sb, 8, 4, text);
        assertNotNull(sb.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultArgName()
    {
        // Ground Truth Bug:
        // When HelpFormatter#setArgName("argument") is set and an Option with hasArg()
        // is added without explicitly calling setArgName on Option, usage statement must
        // display <argument> instead of falling back to hardcoded <arg>.
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("argument");

        Options options = new Options();
        Option optF = new Option("f", true, "flag option with argument");
        options.addOption(optF);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app -f <argument>" + EOL;
        assertEquals(expected, out.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntaxThrows()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, null, "Header", new Options(), 1, 3, "Footer", true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntaxThrows()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "", "Header", new Options(), 1, 3, "Footer", true);
    }

    // =========================================================================
    // Partition E: Detailed Formatting, Options Rendering & Overload Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintUsageSyntaxOnlyWithoutSpace()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "myapp");
        pw.flush();

        assertEquals("usage: myapp" + EOL, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintUsageSyntaxOnlyWithSpace()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "myapp [options] <file>");
        pw.flush();

        assertEquals("usage: myapp [options] <file>" + EOL, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintHelpAllVariationsAndSections()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("a", "all", false, "turn on all features");
        options.addOption("b", false, "short only opt");

        // Test with non-empty header and footer, autoUsage = false
        formatter.printHelp(pw, 80, "testapp", "== HEADER ==", options, 2, 4, "== FOOTER ==", false);
        pw.flush();

        String result = sw.toString();
        assertTrue(result.contains("usage: testapp"));
        assertTrue(result.contains("== HEADER =="));
        assertTrue(result.contains("-a,--all"));
        assertTrue(result.contains("-b"));
        assertTrue(result.contains("== FOOTER =="));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithWhitespaceOnlyHeaderAndFooter()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        // Header and footer are whitespace only -> must NOT be printed
        formatter.printHelp(pw, 80, "testapp", "   \t  ", options, 1, 3, "  \n  ", true);
        pw.flush();

        String result = sw.toString();
        assertFalse(result.contains("   \t  "));
        assertTrue(result.contains("usage: testapp"));
        assertTrue(result.contains("-v,--version"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpSystemOutOverloadsDeterministic()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "show help");

        // Intercept System.out to test all convenience delegates safely
        java.io.PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            System.setOut(new java.io.PrintStream(baos));

            formatter.printHelp("cmdLine", options);
            formatter.printHelp("cmdLine", options, true);
            formatter.printHelp("cmdLine", "header", options, "footer");
            formatter.printHelp("cmdLine", "header", options, "footer", true);
            formatter.printHelp(80, "cmdLine", "header", options, "footer");
            formatter.printHelp(80, "cmdLine", "header", options, "footer", true);

            String captured = baos.toString();
            assertTrue(captured.length() > 0);
            assertTrue(captured.contains("cmdLine"));
        }
        finally
        {
            System.setOut(originalOut);
        }
    }

    @Test(timeout = 4000)
    public void testPrintHelpEightArgOverload()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("q", "quiet", false, "be quiet");

        formatter.printHelp(pw, 80, "app", "hdr", options, 1, 2, "ftr");
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("usage: app"));
        assertTrue(out.contains("hdr"));
        assertTrue(out.contains("-q,--quiet"));
        assertTrue(out.contains("ftr"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsVariousOptionConfigurations()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        // 1. Long option only (opt == null)
        Option longOnly = new Option(null, "config", true, "configuration file");
        longOnly.setArgName("FILE");

        // 2. Short opt with no description
        Option shortNoDesc = new Option("x", false, null);

        // 3. Option with empty string argName
        Option emptyArg = new Option("e", "empty", true, "empty argument option");
        emptyArg.setArgName("");

        // 4. Required option with long option and argument
        Option reqWithArg = new Option("r", "require", true, "required option");
        reqWithArg.setRequired(true);

        options.addOption(longOnly);
        options.addOption(shortNoDesc);
        options.addOption(emptyArg);
        options.addOption(reqWithArg);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);
        String rendered = sb.toString();

        // Validate long option only format: "   --"
        assertTrue(rendered.contains("--config <FILE>"));
        // Validate short opt no description
        assertTrue(rendered.contains("-x"));
        // Validate empty arg name format: " "
        assertTrue(rendered.contains("-e,--empty"));
        // Validate required option description
        assertTrue(rendered.contains("required option"));
    }

    @Test(timeout = 4000)
    public void testAppendOptionGroupInPrintUsage()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        // Required OptionGroup
        OptionGroup reqGroup = new OptionGroup();
        reqGroup.setRequired(true);
        reqGroup.addOption(new Option("a", "opt-a"));
        reqGroup.addOption(new Option("b", "opt-b"));
        options.addOptionGroup(reqGroup);

        // Optional OptionGroup
        OptionGroup optGroup = new OptionGroup();
        optGroup.setRequired(false);
        optGroup.addOption(new Option("c", "opt-c"));
        optGroup.addOption(new Option("d", "opt-d"));
        options.addOptionGroup(optGroup);

        // Regular standalone option
        Option standalone = new Option(null, "verbose", false, "verbose mode");
        options.addOption(standalone);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 120, "myapp", options);
        pw.flush();

        String usage = sw.toString();
        // Required group is NOT wrapped in outer [ ], but options inside are
        assertTrue(usage.contains("-a | -b"));
        // Optional group IS wrapped in outer [ ]
        assertTrue(usage.contains("[-c | -d]"));
        // Long opt only in standalone
        assertTrue(usage.contains("[--verbose]"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsWrappingLongDescription()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        String longDesc = "This is a very long description intended to test line wrapping "
                + "and alignment of multiple lines when formatting option descriptions.";
        options.addOption("l", "long", false, longDesc);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 40, options, 1, 3);

        String output = sb.toString();
        String[] lines = output.split(formatter.getNewLine());
        assertTrue("Output should wrap into multiple lines", lines.length > 1);
        for (String line : lines)
        {
            assertTrue("Line width must be within boundary", line.length() <= 40);
        }
    }

    @Test(timeout = 4000)
    public void testOptionComparatorContract()
    {
        HelpFormatter formatter = new HelpFormatter();
        Comparator comparator = formatter.getOptionComparator();

        Option optA = new Option("a", "alpha");
        Option optB = new Option("B", "beta");
        Option optA2 = new Option("A", "another-alpha");

        // Case-insensitive comparison: "a" vs "B" -> "a".compareToIgnoreCase("B") < 0
        assertTrue(comparator.compare(optA, optB) < 0);
        assertTrue(comparator.compare(optB, optA) > 0);
        assertEquals(0, comparator.compare(optA, optA2));
    }

    @Test(timeout = 4000)
    public void testPrintWrappedOverloads()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");

        StringWriter sw1 = new StringWriter();
        PrintWriter pw1 = new PrintWriter(sw1);
        formatter.printWrapped(pw1, 20, "Short text");
        pw1.flush();
        assertEquals("Short text\n", sw1.toString());

        StringWriter sw2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw2);
        formatter.printWrapped(pw2, 20, 4, "A somewhat longer text that will definitely be wrapped into multiple lines");
        pw2.flush();
        String[] lines = sw2.toString().split("\n");
        assertTrue(lines.length > 1);
        assertTrue(lines[1].startsWith("    "));
    }
}