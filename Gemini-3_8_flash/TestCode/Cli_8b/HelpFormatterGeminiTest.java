package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.cli.HelpFormatter
 * Known Defect: Defects4J CLI (HelpFormatterTest::testPrintWrapped failure on "single line padded text 2")
 * Root Cause:   In renderWrappedText/findWrapPos, when formatting wrapped lines with nextLineTabStop
 *               padding, wrapping calculation (startPos + width) causes words that should wrap to line 1
 *               or line 2 to be wrapped incorrectly relative to column boundaries.
 *
 * Decision / Branch Coverage Targets:
 * 1. findWrapPos:
 *    - \n found before max wrap position (pos <= width) -> return pos + 1
 *    - \t found before max wrap position (pos <= width) -> return pos + 1
 *    - (startPos + width) >= text.length() -> return -1 (no wrap needed)
 *    - Search backward from startPos + width for whitespace (' ', '\n', '\r'):
 *        * Found at pos > startPos -> return pos
 *        * Not found before startPos -> search forward past startPos + width:
 *            - Whitespace found before end -> return pos
 *            - No whitespace found (pos == text.length()) -> return -1
 * 2. renderWrappedText:
 *    - Initial findWrapPos == -1 -> rtrim and return immediately
 *    - Initial findWrapPos > 0 -> append first line, then loop with nextLineTabStop padding
 *    - Loop termination when findWrapPos == -1
 * 3. printHelp (all overloads):
 *    - cmdLineSyntax == null -> throws IllegalArgumentException
 *    - cmdLineSyntax == "" -> throws IllegalArgumentException
 *    - autoUsage true -> calls printUsage(pw, width, app, options)
 *    - autoUsage false -> calls printUsage(pw, width, cmdLineSyntax)
 *    - header != null && header.trim().length() > 0 vs null vs whitespace-only
 *    - footer != null && footer.trim().length() > 0 vs null vs whitespace-only
 * 4. printUsage (Options variant):
 *    - Option in OptionGroup vs standalone Option
 *    - OptionGroup required vs not required (brackets '[' and ']')
 *    - OptionGroup already processed vs first encounter
 *    - Option required vs not required
 *    - Short option only vs long option only vs both
 *    - Option with argument and argument name vs argument without name vs no argument
 * 5. renderOptions:
 *    - Option with opt == null (long-only) vs opt != null
 *    - Option with hasLongOpt() true vs false
 *    - Option with hasArg() true && hasArgName() true vs hasArgName() false
 *    - Option with description != null vs null
 *    - Padding between optBuf and description based on max width
 * 6. rtrim & createPadding:
 *    - rtrim null, empty, whitespace-only, no whitespace, trailing whitespace
 *    - createPadding 0, positive
 * 7. OptionComparator:
 *    - Case-insensitive ordering of options by key
 * --------------------------------------------------------------------------------------------------
 */
public class HelpFormatterGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Getters / Setters)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultFieldValues() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testGettersAndSettersStateMutations() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());
        assertEquals(120, formatter.defaultWidth);

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());
        assertEquals(4, formatter.defaultLeftPad);

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());
        assertEquals(8, formatter.defaultDescPad);

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());
        assertEquals("Syntax: ", formatter.defaultSyntaxPrefix);

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
        assertEquals("\n", formatter.defaultNewLine);

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("/", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("//", formatter.defaultLongOptPrefix);

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
        assertEquals("parameter", formatter.defaultArgName);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Helper Method Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreatePaddingBoundaries() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("        ", formatter.createPadding(8));
    }

    @Test(timeout = 4000)
    public void testRtrimBoundaries() {
        HelpFormatter formatter = new HelpFormatter();
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   "));
        assertEquals("", formatter.rtrim("\t \n \r "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc   \t"));
        assertEquals("a b c", formatter.rtrim("a b c  \n"));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosDirectBranches() {
        HelpFormatter formatter = new HelpFormatter();

        // Branch 1: text contains \n before width
        int pos1 = formatter.findWrapPos("hello\nworld", 10, 0);
        assertEquals(6, pos1);

        // Branch 2: text contains \t before width
        int pos2 = formatter.findWrapPos("hello\tworld", 10, 0);
        assertEquals(6, pos2);

        // Branch 3: startPos + width >= text.length()
        int pos3 = formatter.findWrapPos("short", 10, 0);
        assertEquals(-1, pos3);

        int pos4 = formatter.findWrapPos("padshort", 10, 3);
        assertEquals(-1, pos4);

        // Branch 4: backward whitespace search finds ' '
        int pos5 = formatter.findWrapPos("hello world test", 8, 0);
        assertEquals(5, pos5);

        // Branch 5: backward search finds '\r'
        int pos6 = formatter.findWrapPos("hello\rworld test", 8, 0);
        assertEquals(5, pos6);

        // Branch 6: forward whitespace search when no whitespace before width
        // "abcdefghij klmn" width 5 -> no space in [0..5], forward search finds space at 10
        int pos7 = formatter.findWrapPos("abcdefghij klmn", 5, 0);
        assertEquals(10, pos7);

        // Branch 7: forward search finds no whitespace at all -> returns -1
        int pos8 = formatter.findWrapPos("abcdefghijklmn", 5, 0);
        assertEquals(-1, pos8);
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextShortNoWrap() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        formatter.renderWrappedText(sb, 20, 0, "Short text");
        assertEquals("Short text", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextMultiLineInput() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        formatter.renderWrappedText(sb, 20, 2, "Line1\nLine2\nLine3");
        String expected = "Line1\n  Line2\n  Line3";
        assertEquals(expected, sb.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Trigger)
    // =========================================================================

    /**
     * Targets known defect: ComparisonFailure on "single line padded text 2".
     * Validates that renderWrappedText properly wraps padded text containing complex clauses.
     */
    @Test(timeout = 4000)
    public void testPrintWrappedDefectSingleLinePaddedText2() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();

        String text = "format: [DATE[-DATE]] where DATE: XX/XX/XXXX smartcompose(default) swap=swapv";
        int width = 30;
        int padding = 3;
        formatter.renderWrappedText(sb, width, padding, text);

        String expected = "format: [DATE[-DATE]] where DATE: XX/XX/XXXX" + formatter.getNewLine() +
                          "   smartcompose(default)" + formatter.getNewLine() +
                          "   swap=swapv";

        assertEquals("single line padded text 2", expected, sb.toString());
    }

    /**
     * Additional wrapped text precision tests for multi-line formatting.
     */
    @Test(timeout = 4000)
    public void testRenderWrappedTextExactFitAndWordBoundaries() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        String text = "fee fi fo fum";
        int width = 8;
        int padding = 0;
        formatter.renderWrappedText(sb, width, padding, text);
        assertEquals("fee fi\nfo fum", sb.toString());

        sb.setLength(0);
        text = "line1\nline2\nline3";
        width = 8;
        padding = 3;
        formatter.renderWrappedText(sb, width, padding, text);
        assertEquals("line1\n   line2\n   line3", sb.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntaxThrowsException() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntaxThrowsException() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    // =========================================================================
    // Partition E: Options Rendering & Usage Clauses (Complex Decision Trees)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintUsageSimpleSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "myapp -a -b <file>");
        pw.flush();

        assertEquals("usage: myapp -a -b <file>\n", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionsAndOptionGroups() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();

        // 1. Regular required short option
        Option optA = new Option("a", "alpha", false, "alpha option");
        optA.setRequired(true);
        options.addOption(optA);

        // 2. Regular non-required option with arg name
        Option optB = new Option("b", "beta", true, "beta option");
        optB.setArgName("val");
        options.addOption(optB);

        // 3. Long option only without short opt
        Option optLongOnly = new Option(null, "gamma", true, "gamma option");
        optLongOnly.setArgName("garg");
        options.addOption(optLongOnly);

        // 4. Non-required OptionGroup
        OptionGroup group1 = new OptionGroup();
        group1.setRequired(false);
        Option optX = new Option("x", "option X");
        Option optY = new Option("y", "option Y");
        group1.addOption(optX);
        group1.addOption(optY);
        options.addOptionGroup(group1);

        // 5. Required OptionGroup
        OptionGroup group2 = new OptionGroup();
        group2.setRequired(true);
        Option optM = new Option("m", "option M");
        Option optN = new Option("n", "option N");
        group2.addOption(optM);
        group2.addOption(optN);
        options.addOptionGroup(group2);

        formatter.printUsage(pw, 120, "app", options);
        pw.flush();

        String usage = sw.toString();
        assertTrue("Usage must start with syntax prefix", usage.startsWith("usage: app "));
        assertTrue("Must contain required -a", usage.contains("-a"));
        assertTrue("Must contain optional [-b <val>]", usage.contains("[-b <val>]"));
        assertTrue("Must contain optional long opt [--gamma <garg>]", usage.contains("[--gamma <garg>]"));
        assertTrue("Must contain non-required group in brackets [-x | -y]", usage.contains("[-x | -y]"));
        assertTrue("Must contain required group without outer brackets -m | -n", usage.contains("-m | -n"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsAllBranches() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        Options options = new Options();

        // Option 1: short opt only, no long opt, no arg, description present
        Option optA = new Option("a", "Short only");
        options.addOption(optA);

        // Option 2: long opt only, arg with custom name
        Option optLong = new Option(null, "config", true, "Config file path");
        optLong.setArgName("file");
        options.addOption(optLong);

        // Option 3: short and long opt, arg without custom name (defaults to empty argName check)
        Option optBoth = new Option("o", "output", true, "Output file");
        optBoth.setArgName(null); // hasArg() true, hasArgName() false branch
        options.addOption(optBoth);

        // Option 4: option with null description
        Option optNoDesc = new Option("n", "no-desc", false, null);
        options.addOption(optNoDesc);

        formatter.renderOptions(sb, 80, options, 2, 4);
        String rendered = sb.toString();

        assertTrue("Must render short opt -a", rendered.contains("  -a"));
        assertTrue("Must render long opt only   --config <file>", rendered.contains("  --config <file>"));
        assertTrue("Must render combined -o,--output", rendered.contains("  -o,--output"));
        assertTrue("Must render description", rendered.contains("Config file path"));
        assertTrue("Must handle null description without failure", rendered.contains("  -n,--no-desc"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpFullLifecycleHeaderAndFooterVariations() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("v", "verbose", false, "Turn on verbose mode");

        // Case 1: Header and footer populated
        formatter.printHelp(pw, 80, "mycmd", "=== Header ===", options, 1, 3, "=== Footer ===", true);
        pw.flush();
        String output1 = sw.toString();

        assertTrue(output1.contains("usage: mycmd"));
        assertTrue(output1.contains("=== Header ==="));
        assertTrue(output1.contains("-v,--verbose"));
        assertTrue(output1.contains("=== Footer ==="));

        // Case 2: Header and footer null/empty, autoUsage false
        sw.getBuffer().setLength(0);
        formatter.printHelp(pw, 80, "mycmd", null, options, 1, 3, "   ", false);
        pw.flush();
        String output2 = sw.toString();

        assertTrue(output2.contains("usage: mycmd"));
        assertFalse(output2.contains("=== Header ==="));
        assertFalse(output2.contains("=== Footer ==="));
    }

    @Test(timeout = 4000)
    public void testPrintWrappedWithNextLineTabStop() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 20, 4, "This is a long line of text that must be wrapped at twenty columns.");
        pw.flush();

        String[] lines = sw.toString().split("\n");
        assertTrue("Must produce at least two lines", lines.length > 1);
        assertTrue("Continuation line must start with 4 spaces", lines[1].startsWith("    "));
    }

    @Test(timeout = 4000)
    public void testPrintWrappedOverload() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 15, "Word1 Word2 Word3 Word4 Word5");
        pw.flush();

        String[] lines = sw.toString().split("\n");
        assertTrue("Must wrap into multiple lines", lines.length > 1);
    }

    @Test(timeout = 4000)
    public void testPrintHelpConvenienceDelegates() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // 8-param overload without autoUsage (defaults to false)
        formatter.printHelp(pw, 60, "app", "Header", options, 2, 2, "Footer");
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("usage: app"));
        assertTrue(out.contains("Header"));
        assertTrue(out.contains("-h,--help"));
        assertTrue(out.contains("Footer"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpSystemOutDelegates() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("t", false, "test opt");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));

            // Exercise all System.out overloads to ensure zero exceptions and full line coverage
            formatter.printHelp("cmd1", options);
            formatter.printHelp("cmd2", options, true);
            formatter.printHelp("cmd3", "head3", options, "foot3");
            formatter.printHelp("cmd4", "head4", options, "foot4", true);
            formatter.printHelp(70, "cmd5", "head5", options, "foot5");
            formatter.printHelp(70, "cmd6", "head6", options, "foot6", false);

            String captured = baos.toString();
            assertTrue(captured.contains("cmd1"));
            assertTrue(captured.contains("cmd2"));
            assertTrue(captured.contains("cmd3"));
            assertTrue(captured.contains("cmd4"));
            assertTrue(captured.contains("cmd5"));
            assertTrue(captured.contains("cmd6"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test(timeout = 4000)
    public void testOptionComparatorCaseInsensitiveSorting() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        // Add options in non-alphabetical mixed-case order
        options.addOption("z", "zebra", false, "desc");
        options.addOption("A", "Alpha", false, "desc");
        options.addOption("b", "beta", false, "desc");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String usage = sw.toString();
        int idxA = usage.indexOf("-A");
        int idxB = usage.indexOf("-b");
        int idxZ = usage.indexOf("-z");

        assertTrue("-A should appear before -b", idxA < idxB);
        assertTrue("-b should appear before -z", idxB < idxZ);
    }
}