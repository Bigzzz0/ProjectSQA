package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli.HelpFormatter
 *
 * Coverage Target Breakdown:
 * 1. Getters / Setters & Default Configuration:
 *    - width, leftPadding, descPadding, syntaxPrefix, newLine, optPrefix, longOptPrefix, argName
 *    - optionComparator (null handling defaults to OptionComparator, custom comparator)
 * 2. printHelp variants:
 *    - printHelp(cmdLineSyntax, options)
 *    - printHelp(cmdLineSyntax, options, autoUsage)
 *    - printHelp(cmdLineSyntax, header, options, footer)
 *    - printHelp(cmdLineSyntax, header, options, footer, autoUsage)
 *    - printHelp(width, cmdLineSyntax, header, options, footer)
 *    - printHelp(width, cmdLineSyntax, header, options, footer, autoUsage)
 *    - printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer)
 *    - printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, autoUsage)
 * 3. printUsage variants:
 *    - printUsage(pw, width, app, options):
 *      - Option without OptionGroup
 *      - Option with OptionGroup (required vs non-required group)
 *      - Already processed OptionGroup suppression
 *      - Short opt vs long opt only
 *      - Option with argument (hasArg && hasArgName) vs without
 *      - Required Option vs non-required Option ([ -o <arg> ])
 *    - printUsage(pw, width, cmdLineSyntax)
 * 4. printOptions & renderOptions:
 *    - Short opt only, long opt only (getOpt() == null), both short and long opt
 *    - Option with argument (hasArgName vs default/no arg name)
 *    - Option without argument
 *    - Padding calculations (optBuf.length() < max, optBuf.length() == max)
 *    - Option with null description vs non-null description
 * 5. renderWrappedText & findWrapPos:
 *    - Text fitting completely within width (pos == -1)
 *    - Text wrapping at newline '\n' and tab '\t' before width
 *    - Text wrapping at whitespace within width
 *    - Wrapping text with no whitespace before width (breaking after width)
 *    - text.length() > width && pos == nextLineTabStop - 1 branch -> pos = width
 *    - nextLineTabStop >= width -> IllegalStateException
 * 6. Utility Methods:
 *    - createPadding(len): zero length, positive length
 *    - rtrim(s): null, empty, whitespace only, mixed trailing whitespace
 * 7. OptionComparator:
 *    - compare options case-insensitively by key
 *
 * Ground Truth Defect Targeted:
 * - BugCLI162: Calling printHelp when argument padding + indent exceeds line width.
 *   In CLI-162 (Defects4J), setting width smaller than or close to argument indent
 *   triggers IllegalStateException: "Total width is less than the width of the argument and indent".
 */
public class HelpFormatterGeminiTest
{
    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions (Getters / Setters)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGettersAndSetters()
    {
        HelpFormatter hf = new HelpFormatter();

        assertEquals(HelpFormatter.DEFAULT_WIDTH, hf.getWidth());
        hf.setWidth(120);
        assertEquals(120, hf.getWidth());

        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, hf.getLeftPadding());
        hf.setLeftPadding(5);
        assertEquals(5, hf.getLeftPadding());

        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, hf.getDescPadding());
        hf.setDescPadding(7);
        assertEquals(7, hf.getDescPadding());

        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, hf.getSyntaxPrefix());
        hf.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", hf.getSyntaxPrefix());

        assertEquals(System.getProperty("line.separator"), hf.getNewLine());
        hf.setNewLine("\n");
        assertEquals("\n", hf.getNewLine());

        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, hf.getOptPrefix());
        hf.setOptPrefix("+");
        assertEquals("+", hf.getOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, hf.getLongOptPrefix());
        hf.setLongOptPrefix("++");
        assertEquals("++", hf.getLongOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, hf.getArgName());
        hf.setArgName("parameter");
        assertEquals("parameter", hf.getArgName());
    }

    @Test(timeout = 4000)
    public void testSetOptionComparator()
    {
        HelpFormatter hf = new HelpFormatter();
        assertNotNull(hf.getOptionComparator());

        Comparator customComp = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return 0;
            }
        };

        hf.setOptionComparator(customComp);
        assertSame(customComp, hf.getOptionComparator());

        hf.setOptionComparator(null);
        assertNotNull(hf.getOptionComparator());
        assertNotSame(customComp, hf.getOptionComparator());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Text Wrapping Logic
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRtrim()
    {
        HelpFormatter hf = new HelpFormatter();

        assertNull(hf.rtrim(null));
        assertEquals("", hf.rtrim(""));
        assertEquals("", hf.rtrim("   \t \n \r  "));
        assertEquals("abc", hf.rtrim("abc"));
        assertEquals("abc", hf.rtrim("abc   \t\n"));
        assertEquals("  abc", hf.rtrim("  abc  "));
    }

    @Test(timeout = 4000)
    public void testCreatePadding()
    {
        HelpFormatter hf = new HelpFormatter();

        assertEquals("", hf.createPadding(0));
        assertEquals("   ", hf.createPadding(3));
        assertEquals(10, hf.createPadding(10).length());
    }

    @Test(timeout = 4000)
    public void testFindWrapPos()
    {
        HelpFormatter hf = new HelpFormatter();

        // 1. Text ends before width
        assertEquals(-1, hf.findWrapPos("short", 10, 0));

        // 2. Contains newline within width
        assertEquals(5, hf.findWrapPos("line1\nline2", 10, 0));

        // 3. Contains tab within width
        assertEquals(5, hf.findWrapPos("line1\tline2", 10, 0));

        // 4. Whitespace before startPos + width
        assertEquals(5, hf.findWrapPos("first second third", 8, 0));

        // 5. No whitespace before startPos + width, but whitespace after
        assertEquals(11, hf.findWrapPos("verylongword next", 5, 0));

        // 6. Completely no whitespace
        assertEquals(-1, hf.findWrapPos("unbreakableword", 5, 0));
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextSimpleAndMultiLine()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        // Single line fitting
        hf.renderWrappedText(sb, 20, 0, "hello world");
        assertEquals("hello world", sb.toString());

        // Multi-line wrapping with padding
        sb = new StringBuffer();
        hf.renderWrappedText(sb, 12, 4, "one two three four");
        String expected = "one two\n    three\n    four";
        assertEquals(expected, sb.toString());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextLongUnbrokenWordWithIndent()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        // Forces pos == nextLineTabStop - 1 branch where pos becomes width
        hf.renderWrappedText(sb, 8, 3, "abc 1234567890");
        String result = sb.toString();
        assertTrue(result.contains("abc"));
        assertTrue(result.contains("12345678"));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (CLI-162 / BugCLI162Test)
    // -------------------------------------------------------------------------

    /**
     * Targets BugCLI162: Total width is less than the width of the argument and indent.
     * When long line chunking occurs and description wraps onto multiple lines,
     * HelpFormatter should format without throwing IllegalStateException.
     */
    @Test(timeout = 4000)
    public void testLongLineChunkingIndentIgnoredDefect()
    {
        Options options = new Options();
        options.addOption("x", "extralongarg", false,
                "This description is to be popped onto more than one line. The first line should be correctly padded, the subsequent lines are not padded, but wrap correctly.");

        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Width 35 with leftPad 10 and descPad 16 triggers the defect if nextLineTabStop >= width
        formatter.printHelp(pw, 35, "foobar", "header", options, 10, 16, "footer");
        pw.flush();

        String output = sw.toString();
        assertTrue("Output should contain syntax foobar", output.contains("foobar"));
        assertTrue("Output should contain extralongarg option", output.contains("-x,--extralongarg"));
        assertTrue("Output should contain description segment", output.contains("description"));
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntaxThrowsException()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(null, new Options());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntaxThrowsException()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("", new Options());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testRenderWrappedTextTabStopGreaterThanWidthThrowsException()
    {
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        // nextLineTabStop (10) >= width (8) causes IllegalStateException
        hf.renderWrappedText(sb, 8, 10, "first line wrap text");
    }

    // -------------------------------------------------------------------------
    // Partition E: Options Rendering, Usage & OptionGroup Coverage
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderAndFooter()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("a", false, "option a description");
        options.addOption("b", "long-b", true, "option b description");

        hf.printHelp(pw, 80, "myapp", "Header text", options, 2, 2, "Footer text", true);
        pw.flush();

        String res = sw.toString();
        assertTrue(res.contains("usage: myapp"));
        assertTrue(res.contains("Header text"));
        assertTrue(res.contains("-a"));
        assertTrue(res.contains("-b,--long-b <arg>"));
        assertTrue(res.contains("Footer text"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithoutHeaderOrFooter()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("c", false, "opt c");

        hf.printHelp(pw, 80, "myapp", "   ", options, 1, 3, "", false);
        pw.flush();

        String res = sw.toString();
        assertTrue(res.contains("usage: myapp"));
        assertTrue(res.contains("-c"));
        assertFalse(res.contains("Header text"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroups()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();

        // Group 1: required
        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        Option optA = new Option("a", "alpha", false, "option a");
        Option optB = new Option("b", "beta", true, "option b");
        optB.setArgName("val");
        requiredGroup.addOption(optA);
        requiredGroup.addOption(optB);
        options.addOptionGroup(requiredGroup);

        // Group 2: optional
        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        Option optC = new Option(null, "gamma", false, "option gamma");
        Option optD = new Option("d", false, "option d");
        optionalGroup.addOption(optC);
        optionalGroup.addOption(optD);
        options.addOptionGroup(optionalGroup);

        // Standalone option: required
        Option optReq = new Option("r", "req", false, "required option");
        optReq.setRequired(true);
        options.addOption(optReq);

        // Standalone option: optional with argument
        Option optArg = new Option("x", false, "standalone opt with arg");
        optArg.setArgs(1);
        optArg.setArgName("file");
        options.addOption(optArg);

        hf.printUsage(pw, 120, "appGroupTest", options);
        pw.flush();

        String usage = sw.toString();
        assertTrue(usage.contains("appGroupTest"));
        // Required group has no outer []
        assertTrue(usage.contains("-a") || usage.contains("-b <val>"));
        assertTrue(usage.contains("|"));
        // Optional group has []
        assertTrue(usage.contains("["));
        assertTrue(usage.contains("]"));
        // Standalone required option is not bracketed: "-r"
        assertTrue(usage.contains("-r"));
        // Standalone optional with arg: "[-x <file>]"
        assertTrue(usage.contains("[-x <file>]"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsFormattingBranches()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        Options options = new Options();
        // 1. Long opt only, no short opt
        Option optLongOnly = new Option(null, "longonly", true, "Long only description");
        optLongOnly.setArgName(""); // hasArg() is true, but hasArgName() is false

        // 2. Short opt only, with null description
        Option optShortOnly = new Option("s", null);

        // 3. Both short and long opt with custom arg name
        Option optBoth = new Option("b", "both", true, "Both description");
        optBoth.setArgName("custom");

        options.addOption(optLongOnly);
        options.addOption(optShortOnly);
        options.addOption(optBoth);

        hf.renderOptions(sb, 80, options, 2, 4);

        String result = sb.toString();
        assertTrue(result.contains("--longonly"));
        assertTrue(result.contains("-s"));
        assertTrue(result.contains("-b,--both <custom>"));
        assertTrue(result.contains("Long only description"));
        assertTrue(result.contains("Both description"));
    }

    @Test(timeout = 4000)
    public void testPrintWrappedOverloads()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printWrapped(pw, 20, "1234567890 1234567890 1234567890");
        pw.flush();
        String out1 = sw.toString();
        assertTrue(out1.contains("\n"));

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        hf.printWrapped(pw, 20, 5, "1234567890 1234567890 1234567890");
        pw.flush();
        String out2 = sw.toString();
        assertTrue(out2.contains("     "));
    }

    @Test(timeout = 4000)
    public void testPrintOptionsMethod()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        hf.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("-h,--help"));
        assertTrue(out.contains("display help"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageSimpleString()
    {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printUsage(pw, 80, "myApp [options] <file>");
        pw.flush();

        String out = sw.toString();
        assertTrue(out.startsWith("usage: myApp [options] <file>"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpConsoleVariantsCoverage()
    {
        // Redirect System.out to capture and ensure no exceptions thrown by console overloads
        java.io.PrintStream originalOut = System.out;
        try
        {
            System.setOut(new java.io.PrintStream(new ByteArrayOutputStream()));

            HelpFormatter hf = new HelpFormatter();
            Options options = new Options();
            options.addOption("t", "test", false, "test opt");

            hf.printHelp("cmd", options);
            hf.printHelp("cmd", options, true);
            hf.printHelp("cmd", "header", options, "footer");
            hf.printHelp("cmd", "header", options, "footer", true);
            hf.printHelp(80, "cmd", "header", options, "footer");
            hf.printHelp(80, "cmd", "header", options, "footer", true);
        }
        finally
        {
            System.setOut(originalOut);
        }
    }

    @Test(timeout = 4000)
    public void testOptionComparatorCaseInsensitivity()
    {
        HelpFormatter hf = new HelpFormatter();
        Comparator comp = hf.getOptionComparator();

        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("A", "ALPHA", false, "desc");
        Option opt3 = new Option("b", "beta", false, "desc");

        assertEquals(0, comp.compare(opt1, opt2));
        assertTrue(comp.compare(opt1, opt3) < 0);
        assertTrue(comp.compare(opt3, opt1) > 0);
    }
}