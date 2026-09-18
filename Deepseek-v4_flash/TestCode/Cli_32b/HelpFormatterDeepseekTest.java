package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Test;

/**
 * White-box test suite for HelpFormatter, targeting maximum line/branch coverage
 * and the known defect (StringIndexOutOfBoundsException in findWrapPos and
 * renderWrappedText).
 *
 * [Branch & Defect Analysis Matrix]
 *
 * 1. findWrapPos:
 *    - Early return for newline/tab (pos <= width)
 *    - Early return for startPos+width >= text.length()
 *    - First while loop: look backwards for whitespace (space, \n, \r)
 *      - Condition pos > startPos to accept found position
 *    - Second while loop: if no whitespace found before, look forward for first whitespace
 *      - BUG: uses 'pos <= text.length()' which causes StringIndexOutOfBoundsException
 *        when pos equals text.length() (accesses charAt(pos))
 *      - Correct fix: use 'pos < text.length()'
 *    - Return -1 if no whitespace found (after second while, pos==text.length())
 *
 * 2. renderWrappedText:
 *    - Uses findWrapPos -> inherits the same bug
 *    - nextLineTabStop handling: if >= width, set to 1
 *    - padding creation and text re‑assembly in loop
 *    - Infinite loop protection: when text.length() > width and pos == nextLineTabStop-1, set pos=width
 *
 * 3. appendOption / appendOptionGroup:
 *    - Required/optional brackets
 *    - Short/long option prefix
 *    - Arg name handling (null, empty)
 *    - Long option separator
 *
 * 4. printUsage, printHelp overloads:
 *    - IllegalArgumentException for null/empty cmdLineSyntax
 *    - Auto‑usage generation
 *    - Header/footer printing when non‑empty
 *
 * 5. OptionComparator default and custom (null restores default)
 *
 * Defect targeting tests:
 *   - testFindWrapPos_NoSpaceWithinWidth: triggers out‑of‑bounds in buggy version
 *   - testRenderWrappedText_WordCut: triggers the same bug in wrapper
 */
public class HelpFormatterDeepseekTest {

    // ------------------------------------------------------------------- Helpers

    private HelpFormatter formatter = new HelpFormatter();
    private StringWriter stringWriter = new StringWriter();
    private PrintWriter printWriter = new PrintWriter(stringWriter);

    // ------------------------------------------------- Partition A: Core functional & state

    @Test(timeout = 4000)
    public void testDefaultValues() {
        assertEquals(74, formatter.getWidth());
        assertEquals(1, formatter.getLeftPadding());
        assertEquals(3, formatter.getDescPadding());
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals("-", formatter.getOptPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals(" ", formatter.getLongOptSeparator());
        assertEquals("arg", formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testSettersGetters() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());

        formatter.setSyntaxPrefix("usage: java ");
        assertEquals("usage: java ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("--");
        assertEquals("--", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("file");
        assertEquals("file", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testOptionComparatorSetNullRestoresDefault() {
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        // should be an instance of OptionComparator
        assertTrue(formatter.getOptionComparator() instanceof Comparator);
    }

    @Test(timeout = 4000)
    public void testOptionComparatorCustom() {
        Comparator custom = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(custom);
        assertSame(custom, formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testSyntaxPrefixEmpty() {
        formatter.setSyntaxPrefix("");
        assertEquals("", formatter.getSyntaxPrefix());
    }

    @Test(timeout = 4000)
    public void testNewLineCustom() {
        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
    }

    // ------------------------------------------------- Partition B: Boundary & null/empty

    @Test(timeout = 4000)
    public void testSetWidthZero() {
        formatter.setWidth(0);
        assertEquals(0, formatter.getWidth());
    }

    @Test(timeout = 4000)
    public void testSetLeftPaddingNegative() {
        formatter.setLeftPadding(-1);
        assertEquals(-1, formatter.getLeftPadding());
    }

    @Test(timeout = 4000)
    public void testSetDescPaddingZero() {
        formatter.setDescPadding(0);
        assertEquals(0, formatter.getDescPadding());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(new PrintWriter(System.out), 80, null, "header",
                new Options(), 1, 3, "footer");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(new PrintWriter(System.out), 80, "", "header",
                new Options(), 1, 3, "footer");
    }

    @Test(timeout = 4000)
    public void testPrintHelpNullOptionsWithValidSyntax() {
        // should not throw – null/empty options are handled
        formatter.printHelp(new PrintWriter(System.out), 80, "cmd", "header",
                null, 1, 3, "footer");
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsage_NoOptions() {
        Options opts = new Options();
        formatter.printHelp(printWriter, 80, "cmd", null, opts, 1, 3, null, true);
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: cmd"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsageWithOptions() {
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "verbose mode");
        formatter.printHelp(printWriter, 80, "cmd", "header", opts, 1, 3, "footer", true);
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: cmd [--verbose]"));
    }

    // ------------------------------------------------- Partition C: Defect-targeted (findWrapPos & renderWrappedText)

    /**
     * Triggers the known defect in findWrapPos: when no whitespace is found within width
     * and startPos+width < text.length(), the second while loop with 'pos <= text.length()'
     * causes a StringIndexOutOfBoundsException.
     *
     * Fixed version returns -1 correctly.
     */
    @Test(timeout = 4000)
    public void testFindWrapPos_NoSpaceWithinWidth() {
        // text has no whitespace, width=2, startPos=0 -> after first while, no position found
        // second while in buggy version tries to access charAt(4) and throws
        int pos = formatter.findWrapPos("abcd", 2, 0);
        assertEquals("Should return -1 when no whitespace and end of text reached", -1, pos);
    }

    /**
     * Normal wrap at a space.
     */
    @Test(timeout = 4000)
    public void testFindWrapPos_WrapAtSpace() {
        int pos = formatter.findWrapPos("abc def", 4, 0);
        // startPos+width=4, char at pos 4 is ' ', first while stops at pos=4, return 4
        assertEquals(4, pos);
    }

    /**
     * Wrap at newline.
     */
    @Test(timeout = 4000)
    public void testFindWrapPos_WrapAtNewline() {
        int pos = formatter.findWrapPos("ab\ncdef", 4, 0);
        // newline at pos 2, pos <= width -> return pos+1=3
        assertEquals(3, pos);
    }

    /**
     * Wrap at tab.
     */
    @Test(timeout = 4000)
    public void testFindWrapPos_WrapAtTab() {
        int pos = formatter.findWrapPos("ab\tcdef", 4, 0);
        // tab at pos 2, pos <= width -> return pos+1=3
        assertEquals(3, pos);
    }

    /**
     * Text shorter than width -> return -1.
     */
    @Test(timeout = 4000)
    public void testFindWrapPos_TextShorterThanWidth() {
        int pos = formatter.findWrapPos("abc", 5, 0);
        assertEquals(-1, pos);
    }

    /**
     * Triggers the StringIndexOutOfBoundsException in renderWrappedText.
     * Input: text length=4, width=2, nextLineTabStop=0.
     * In buggy version, findWrapPos fails, and renderWrappedText throws.
     * Fixed version produces the original text as single line (no wrap).
     */
    @Test(timeout = 4000)
    public void testRenderWrappedText_WordCut() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 2, 0, "abcd");
        String result = sb.toString();
        assertEquals("abcd", result);
    }

    @Test(timeout = 4000)
    public void testRenderWrappedText_MultiLine() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 5, 2, "hello world foo bar");
        String result = sb.toString();
        // depends on defaultNewLine
        String nl = formatter.getNewLine();
        String[] lines = result.split(nl, -1);
        assertTrue(lines.length >= 2);
        // first line should be "hello"
        assertEquals("hello", lines[0].trim());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedText_NextLineTabStopGreaterThanWidth() {
        // edge case: nextLineTabStop >= width -> set to 1
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 5, 6, "abc def ghi"); // nextLineTabStop=6 >=5
        String nl = formatter.getNewLine();
        String[] lines = sb.toString().split(nl, -1);
        assertTrue(lines.length >= 2);
        // first line should be "abc"
        assertEquals("abc", lines[0].trim());
    }

    // ------------------------------------------------- Partition D: Exception & defensive paths

    @Test(timeout = 4000)
    public void testPrintUsageWithOptions() {
        Options opts = new Options();
        opts.addOption("a", "all", false, "list all");
        opts.addOption("f", "file", true, "file name");
        formatter.printUsage(printWriter, 80, "cmd", opts);
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: cmd [--all] [--file <arg>]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroup() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        opts.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "cmd", opts);
        String output = stringWriter.toString();
        assertTrue(output.contains("-a") || output.contains("-b"));
        // group should be in square brackets because not required
        assertTrue(output.contains("["));
        assertTrue(output.contains("]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredGroup() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha"));
        opts.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "cmd", opts);
        String output = stringWriter.toString();
        // required group: no surrounding brackets
        assertFalse(output.contains("["));
        assertFalse(output.contains("]"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionHasArgBlankName() {
        Options opts = new Options();
        Option opt = new Option("o", "output", true, "output file");
        opt.setArgName(""); // blank arg name
        opts.addOption(opt);
        formatter.printUsage(printWriter, 80, "cmd", opts);
        String output = stringWriter.toString();
        // hasArg but argName blank -> append space but no <arg>
        assertTrue(output.contains("-o ") || output.contains("[ -o "));
        assertFalse(output.contains("<>"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionHasArgNullName() {
        Options opts = new Options();
        Option opt = new Option("o", "output", true, "output file");
        opt.setArgName(null);
        opts.addOption(opt);
        formatter.printUsage(printWriter, 80, "cmd", opts);
        String output = stringWriter.toString();
        // argName null – should use defaultArgName "arg"
        assertTrue(output.contains("<arg>"));
    }

    @Test(timeout = 4000)
    public void testPrintOptions() {
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "verbose mode");
        opts.addOption("o", "output", true, "output file");
        formatter.printOptions(printWriter, 80, opts, 1, 3);
        String output = stringWriter.toString();
        assertTrue(output.contains("-v,--verbose") || output.contains("-v, --verbose"));
        assertTrue(output.contains("-o,--output"));
    }

    @Test(timeout = 4000)
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 10, "long text that should wrap");
        String output = stringWriter.toString();
        String[] lines = output.split(formatter.getNewLine(), -1);
        assertTrue(lines.length >= 2);
    }

    @Test(timeout = 4000)
    public void testPrintWrappedWithNextLineTabStop() {
        formatter.printWrapped(printWriter, 10, 3, "long text that should wrap with indent");
        String output = stringWriter.toString();
        String[] lines = output.split(formatter.getNewLine(), -1);
        assertTrue(lines.length >= 2);
        // second line should start with 3 spaces (tab stop)
        if (lines.length > 1) {
            assertEquals("   ", lines[1].substring(0, 3));
        }
    }

    // ------------------------------------------------- Partition E: Object lifecycle & contract integrity

    @Test(timeout = 4000)
    public void testRtrim() {
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("", formatter.rtrim("   "));
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
    }

    @Test(timeout = 4000)
    public void testCreatePadding() {
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderAndFooter() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "print help");
        formatter.printHelp(printWriter, 80, "cmd", "HEADER", opts, 1, 3, "FOOTER", false);
        String output = stringWriter.toString();
        assertTrue(output.contains("HEADER"));
        assertTrue(output.contains("FOOTER"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpNoHeaderFooter() {
        Options opts = new Options();
        formatter.printHelp(printWriter, 80, "cmd", null, opts, 1, 3, null, false);
        String output = stringWriter.toString();
        // should not contain empty lines from header/footer
        assertFalse(output.contains("\n\n"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsageTrue() {
        Options opts = new Options();
        opts.addOption("a", "alpha", true, "alpha value");
        formatter.printHelp(printWriter, 80, "cmd", null, opts, 1, 3, null, true);
        String output = stringWriter.toString();
        assertTrue(output.contains("usage: cmd [--alpha <arg>]"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithLongOptSeparator() {
        formatter.setLongOptSeparator("=");
        Options opts = new Options();
        opts.addOption("f", "file", true, "file");
        formatter.printHelp(printWriter, 80, "cmd", null, opts, 1, 3, null, false);
        String output = stringWriter.toString();
        // usage line should display --file=<arg> (with '=' separator)
        assertTrue(output.contains("--file="));
    }

    @Test(timeout = 4000)
    public void testPrintHelpDeprecatedOverloads() {
        // these methods exist for backwards compatibility
        HelpFormatter hf = new HelpFormatter();
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "desc");
        // using deprecated public fields to set values
        hf.defaultWidth = 60;
        hf.defaultLeftPad = 2;
        hf.defaultDescPad = 4;
        hf.defaultSyntaxPrefix = "USAGE: ";
        // call printHelp with deprecation overloads (they delegate)
        hf.printHelp("cmd", opts); // uses System.out – we don't capture
        hf.printHelp("cmd", opts, true);
        hf.printHelp("cmd", "header", opts, "footer");
        hf.printHelp("cmd", "header", opts, "footer", true);
        // no assertion, just ensure no exception
    }
}