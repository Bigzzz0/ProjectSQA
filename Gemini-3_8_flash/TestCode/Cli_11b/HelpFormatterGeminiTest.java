package org.apache.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: HelpFormatter
 *
 * 1. DEFECT UNDER TEST (Defects4J ground truth):
 *    - testPrintOptionWithEmptyArgNameUsage:
 *      appendOption(StringBuffer, Option, boolean) checks:
 *        if (option.hasArg() && (option.getArgName() != null))
 *      When an option has an empty argument name (""), it improperly appends " <>".
 *      It should check option.hasArgName() (or argName.length() > 0) instead of just != null.
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - Option comparison & sorting:
 *        * OptionComparator default case-insensitive sorting by key
 *        * setOptionComparator(null) resets to default OptionComparator
 *        * setOptionComparator(customComparator)
 *    - Argument & Syntax Validation:
 *        * printHelp with null / empty cmdLineSyntax -> IllegalArgumentException
 *        * autoUsage = true vs autoUsage = false
 *        * header != null && trim().length() > 0 vs null / empty header
 *        * footer != null && trim().length() > 0 vs null / empty footer
 *    - Usage Generation (printUsage / appendOption / appendOptionGroup):
 *        * OptionGroup: required vs optional (brackets [ ... ])
 *        * Multiple options in OptionGroup with separator " | "
 *        * Deduplication of OptionGroup occurrences
 *        * Standalone Option: short-only, long-only, short+long
 *        * Option: required vs optional (brackets [ ... ])
 *        * Option: hasArg() with non-null argName, default argName, empty argName
 *        * printUsage(pw, width, cmdLineSyntax) with space vs without space
 *    - Options Rendering (renderOptions):
 *        * Option with null opt (long opt only, prefixed with 3 spaces + defaultLongOptPrefix)
 *        * Option with opt and longOpt (separated by comma)
 *        * Option with opt only
 *        * Option with hasArg() and hasArgName() -> " <arg>"
 *        * Option with hasArg() but not hasArgName() -> " "
 *        * Padding alignment (max calculation and difference padding)
 *        * Option description null vs multi-line description wrapping
 *    - Text Wrapping (renderWrappedText & findWrapPos):
 *        * pos == -1 on initial text (fits within width)
 *        * indexOf('\n') <= width -> break at newline
 *        * indexOf('\t') <= width -> break at tab
 *        * startPos + width >= text.length() -> returns -1
 *        * backward search for whitespace (' ', '\n', '\r')
 *        * forward search when word exceeds width
 *        * word boundary at text length
 *    - Helper Functions:
 *        * rtrim: null, empty, trailing whitespace, non-trailing whitespace, only whitespace
 *        * createPadding: length 0, positive lengths
 *    - Getters, Setters & Deprecated Fields:
 *        * width, leftPadding, descPadding, syntaxPrefix, newLine, optPrefix, longOptPrefix, argName
 *    - PrintHelp overloads invoking System.out:
 *        * Safe redirection and restoration of System.out
 * -----------------------------------------------------------------------------------------
 */
public class HelpFormatterGeminiTest {

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where an Option with an empty string as argName
     * (option.setArgName("")) causes HelpFormatter to render "<>" in usage output.
     * Expected: "usage: app -f\n"
     * Defective: "usage: app -f <>\n"
     */
    @Test(timeout = 4000)
    public void testPrintOptionWithEmptyArgNameUsage() {
        Option option = new Option("f", true, null);
        option.setArgName("");
        Options options = new Options();
        options.addOption(option);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        assertEquals("usage: app -f" + formatter.getNewLine(), out.toString());
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndSetters() {
        HelpFormatter hf = new HelpFormatter();

        hf.setWidth(100);
        assertEquals(100, hf.getWidth());

        hf.setLeftPadding(5);
        assertEquals(5, hf.getLeftPadding());

        hf.setDescPadding(7);
        assertEquals(7, hf.getDescPadding());

        hf.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", hf.getSyntaxPrefix());

        hf.setNewLine("\r\n");
        assertEquals("\r\n", hf.getNewLine());

        hf.setOptPrefix("/");
        assertEquals("/", hf.getOptPrefix());

        hf.setLongOptPrefix("//");
        assertEquals("//", hf.getLongOptPrefix());

        hf.setArgName("parameter");
        assertEquals("parameter", hf.getArgName());

        Comparator customComp = Collections.reverseOrder();
        hf.setOptionComparator(customComp);
        assertSame(customComp, hf.getOptionComparator());

        hf.setOptionComparator(null);
        assertNotNull(hf.getOptionComparator());
        assertNotSame(customComp, hf.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testDefaultOptionComparatorSorting() {
        HelpFormatter hf = new HelpFormatter();
        Comparator comp = hf.getOptionComparator();

        Option optA = new Option("a", "alpha", false, "desc");
        Option optB = new Option("b", "beta", false, "desc");
        Option optUpperA = new Option("A", "Alpha", false, "desc");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optUpperA));
    }

    @Test(timeout = 4000)
    public void testPrintUsageSimpleCommandLineSyntax() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printUsage(pw, 60, "myApp <input> <output>");
        pw.flush();

        String expected = "usage: myApp <input> <output>" + System.getProperty("line.separator");
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintUsageSimpleCommandLineSyntaxWithoutSpace() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printUsage(pw, 60, "myAppNoSpace");
        pw.flush();

        String expected = "usage: myAppNoSpace" + System.getProperty("line.separator");
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroups() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Option opt1 = new Option("a", "all", false, "turn on all");
        Option opt2 = new Option("b", "brief", false, "turn on brief");

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        optionalGroup.addOption(opt1);
        optionalGroup.addOption(opt2);

        Option opt3 = new Option("x", "exclusive1", false, "exclusive 1");
        Option opt4 = new Option("y", "exclusive2", false, "exclusive 2");

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(opt3);
        requiredGroup.addOption(opt4);

        Option standAlone = new Option("c", false, "standalone");
        standAlone.setRequired(true);

        Options options = new Options();
        options.addOptionGroup(optionalGroup);
        options.addOptionGroup(requiredGroup);
        options.addOption(standAlone);

        hf.printUsage(pw, 120, "app", options);
        pw.flush();

        String result = sw.toString();
        assertTrue(result.contains("[-a | -b]"));
        assertTrue(result.contains("-x | -y"));
        assertFalse(result.contains("[-x | -y]"));
        assertTrue(result.contains("-c"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptOnlyAndArgument() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Option longOnly = new Option(null, "config", true, "configuration file");
        longOnly.setArgName("FILE");
        Options options = new Options();
        options.addOption(longOnly);

        hf.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app [--config <FILE>]" + System.getProperty("line.separator");
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintHelpFullLifecycle() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("h", "help", false, "print help");
        options.addOption(new Option("v", "version", false, "print version"));

        hf.printHelp(pw, 80, "myApp", "HEADER_SECTION", options, 2, 4, "FOOTER_SECTION", true);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.startsWith("usage: myApp"));
        assertTrue(output.contains("HEADER_SECTION"));
        assertTrue(output.contains("-h,--help"));
        assertTrue(output.contains("-v,--version"));
        assertTrue(output.contains("FOOTER_SECTION"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpNonAutoUsage() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("f", false, "flag");

        hf.printHelp(pw, 80, "myApp -f <file>", null, options, 1, 3, null, false);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.startsWith("usage: myApp -f <file>"));
        assertTrue(output.contains("-f"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsCombinations() {
        HelpFormatter hf = new HelpFormatter();
        Options options = new Options();

        Option optLongOnly = new Option(null, "verbose", false, "verbose mode");
        Option optShortOnly = new Option("s", false, "short only");
        Option optBothWithArg = new Option("o", "output", true, "output destination");
        optBothWithArg.setArgName("PATH");
        Option optArgNoName = new Option("k", true, "key without arg name");
        optArgNoName.setArgName("");
        Option optNoDesc = new Option("n", "no-desc", false, null);

        options.addOption(optLongOnly);
        options.addOption(optShortOnly);
        options.addOption(optBothWithArg);
        options.addOption(optArgNoName);
        options.addOption(optNoDesc);

        StringBuffer sb = new StringBuffer();
        hf.renderOptions(sb, 80, options, 2, 4);

        String result = sb.toString();
        assertTrue(result.contains("--verbose"));
        assertTrue(result.contains("-s"));
        assertTrue(result.contains("-o,--output <PATH>"));
        assertTrue(result.contains("-k "));
        assertTrue(result.contains("-n,--no-desc"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRtrimBoundaries() {
        HelpFormatter hf = new HelpFormatter();

        assertNull(hf.rtrim(null));
        assertEquals("", hf.rtrim(""));
        assertEquals("", hf.rtrim("   "));
        assertEquals("", hf.rtrim("\t\r\n "));
        assertEquals("abc", hf.rtrim("abc"));
        assertEquals("abc", hf.rtrim("abc   "));
        assertEquals("  abc", hf.rtrim("  abc  \t"));
        assertEquals("a b c", hf.rtrim("a b c "));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingBoundaries() {
        HelpFormatter hf = new HelpFormatter();

        assertEquals("", hf.createPadding(0));
        assertEquals(" ", hf.createPadding(1));
        assertEquals("     ", hf.createPadding(5));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosBoundaries() {
        HelpFormatter hf = new HelpFormatter();

        // Fits within width
        assertEquals(-1, hf.findWrapPos("short text", 20, 0));

        // Exactly matches text length
        assertEquals(-1, hf.findWrapPos("12345", 5, 0));

        // Break on newline character
        assertEquals(6, hf.findWrapPos("line1\nline2", 10, 0));

        // Break on tab character
        assertEquals(5, hf.findWrapPos("tab1\ttab2", 10, 0));

        // Break backwards on whitespace
        String text1 = "The quick brown fox jumps";
        int pos1 = hf.findWrapPos(text1, 10, 0);
        assertEquals(9, pos1); // "The quick" is 9 chars; ' ' at 9

        // Break forwards on long unbreakable word
        String text2 = "Supercalifragilisticexpialidocious extra";
        int pos2 = hf.findWrapPos(text2, 10, 0);
        assertEquals(34, pos2);

        // Entire string is unbreakable and wider than width
        String text3 = "Supercalifragilisticexpialidocious";
        int pos3 = hf.findWrapPos(text3, 10, 0);
        assertEquals(-1, pos3);
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextMultiLine() {
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();

        String input = "This is a long description text that needs to be wrapped multiple times across lines.";
        hf.renderWrappedText(sb, 20, 4, input);

        String[] lines = sb.toString().split(hf.getNewLine());
        assertTrue(lines.length >= 4);

        for (int i = 1; i < lines.length; i++) {
            assertTrue("Line " + i + " must start with tab stop padding", lines[i].startsWith("    "));
        }
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextWithTabsAndNewlines() {
        HelpFormatter hf = new HelpFormatter();
        StringBuffer sb = new StringBuffer();

        String input = "Line1\nLine2\tLine3";
        hf.renderWrappedText(sb, 30, 2, input);

        String result = sb.toString();
        assertTrue(result.contains("Line1"));
        assertTrue(result.contains("Line2"));
        assertTrue(result.contains("Line3"));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntaxThrowsException() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printHelp(pw, 80, null, "header", new Options(), 1, 1, "footer", true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntaxThrowsException() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        hf.printHelp(pw, 80, "", "header", new Options(), 1, 1, "footer", false);
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithBlankHeaderAndFooter() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("a", "all", false, "all options");

        // Blank ("   ") header and footer should not be wrapped/printed
        hf.printHelp(pw, 80, "app", "   ", options, 1, 1, "   ", true);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("usage: app"));
        assertFalse(output.contains("   \n"));
    }

    // =========================================================================
    // PARTITION E: System.out Overloads & Facade Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintHelpSystemOutOverloads() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));

            HelpFormatter hf = new HelpFormatter();
            Options options = new Options();
            options.addOption("t", false, "test option");

            hf.printHelp("testApp", options);
            hf.printHelp("testApp", options, true);
            hf.printHelp("testApp", "header", options, "footer");
            hf.printHelp("testApp", "header", options, "footer", true);
            hf.printHelp(80, "testApp", "header", options, "footer");
            hf.printHelp(80, "testApp", "header", options, "footer", true);

            String output = baos.toString();
            assertTrue(output.contains("usage: testApp"));
            assertTrue(output.contains("-t"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test(timeout = 4000)
    public void testPrintWrappedOverloads() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        hf.printWrapped(pw, 40, "Single line wrapped text");
        hf.printWrapped(pw, 40, 5, "Indented wrapped text that should go to next line if long enough");
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("Single line wrapped text"));
        assertTrue(output.contains("Indented wrapped text"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithoutAutoUsagePrintWriter() {
        HelpFormatter hf = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("x", false, "flag x");

        hf.printHelp(pw, 80, "customApp -x", "head", options, 2, 2, "foot");
        pw.flush();

        String output = sw.toString();
        assertTrue(output.startsWith("usage: customApp -x"));
        assertTrue(output.contains("head"));
        assertTrue(output.contains("-x"));
        assertTrue(output.contains("foot"));
    }
}