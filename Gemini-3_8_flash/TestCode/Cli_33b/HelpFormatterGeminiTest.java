/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli.HelpFormatter
 *
 * Branches & Logic Evaluated:
 * 1. Defect-Targeted Ground Truth:
 *    - testIndentedHeaderAndFooter: tests preservation of leading whitespace on wrapped/multi-line
 *      header and footer text. In the defective implementation, renderWrappedText strips leading
 *      spaces on wrapped/subsequent lines due to text.substring(pos).trim().
 * 2. Getters & Setters / State Mutators:
 *    - width, leftPad, descPad, syntaxPrefix, newLine, optPrefix, longOptPrefix, longOptSeparator, argName
 *    - setOptionComparator: non-null comparator vs null comparator (fallback to OptionComparator)
 * 3. Command Line Syntax Validation:
 *    - printHelp: cmdLineSyntax == null -> IllegalArgumentException
 *    - printHelp: cmdLineSyntax.length() == 0 -> IllegalArgumentException
 * 4. PrintHelp Overloads & AutoUsage Paths:
 *    - autoUsage = true (calls printUsage(pw, width, app, options))
 *    - autoUsage = false (calls printUsage(pw, width, cmdLineSyntax))
 *    - header != null && header.trim().length() > 0 vs null vs all whitespace
 *    - footer != null && footer.trim().length() > 0 vs null vs all whitespace
 *    - stdout-based printHelp convenience overloads
 * 5. Usage Clause Construction (printUsage & appendOption / appendOptionGroup):
 *    - OptionGroup: required vs optional (brackets [ ... ]), multiple options (| separator)
 *    - OptionGroup: already processed groups vs new groups
 *    - Standalone Options: required vs optional ([ -o ])
 *    - Short opt present vs long opt only (--long)
 *    - Argument presence: argName null (defaults to defaultArgName), argName empty (""), custom argName
 *    - Long opt separator vs space separator between opt and arg
 * 6. Option Rendering (renderOptions):
 *    - Short opt only, short + long opt, long opt only
 *    - Arg rendering: blank argName (' ') vs non-blank (<arg>)
 *    - Description: null description vs non-null description
 *    - Padding alignment to max option length
 *    - Multiple options newline handling
 * 7. Text Wrapping Logic (renderWrappedText & findWrapPos):
 *    - findWrapPos: '\n' found <= width (pos + 1)
 *    - findWrapPos: '\t' found <= width (pos + 1)
 *    - findWrapPos: startPos + width >= text.length() (-1)
 *    - findWrapPos: break on last whitespace (' ', '\n', '\r')
 *    - findWrapPos: no whitespace within width (hard break at startPos + width)
 *    - renderWrappedText: pos == -1 on first line (return rtrim(text))
 *    - renderWrappedText: nextLineTabStop >= width boundary (fallback to nextLineTabStop = 1)
 *    - renderWrappedText: text.length() > width && pos == nextLineTabStop - 1 boundary (pos = width)
 * 8. Utilities & Helpers:
 *    - createPadding: len == 0, len > 0
 *    - rtrim: null, empty, all whitespace, trailing whitespace, no whitespace
 *    - OptionComparator: compare method case-insensitive ordering
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

public class HelpFormatterGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Getters/Setters)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultFieldValues()
    {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR, formatter.getLongOptSeparator());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters()
    {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        formatter.setDescPadding(6);
        assertEquals(6, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("val");
        assertEquals("val", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorNullResetsToDefault()
    {
        HelpFormatter formatter = new HelpFormatter();
        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };

        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        // Passing null must reset to default OptionComparator instance
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertNotSame(customComp, formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testDefaultOptionComparatorSorting()
    {
        HelpFormatter formatter = new HelpFormatter();
        Comparator comp = formatter.getOptionComparator();

        Option optA = new Option("a", "Alpha option");
        Option optB = new Option("b", "Beta option");
        Option optA_upper = new Option("A", "Upper alpha option");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optA_upper));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Low-Level Utilities
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreatePadding()
    {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("     ", formatter.createPadding(5));
    }

    @Test(timeout = 4000)
    public void testRtrim()
    {
        HelpFormatter formatter = new HelpFormatter();
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   \t  \n "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc   \t"));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosBreakOnNewline()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "hello\nworld";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);
    }

    @Test(timeout = 4000)
    public void testFindWrapPosBreakOnTab()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "hello\tworld";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);
    }

    @Test(timeout = 4000)
    public void testFindWrapPosLengthWithinWidth()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "short";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);
    }

    @Test(timeout = 4000)
    public void testFindWrapPosWhitespaceBreak()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "the quick brown fox";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(9, pos); // breaks at space after "quick"
    }

    @Test(timeout = 4000)
    public void testFindWrapPosCarriageReturnBreak()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "the\rquick";
        int pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(3, pos);
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNoWhitespaceHardBreak()
    {
        HelpFormatter formatter = new HelpFormatter();
        String text = "abcdefghijklmnop";
        int pos = formatter.findWrapPos(text, 8, 0);
        assertEquals(8, pos);
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextShortText()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 50, 0, "simple line");
        assertEquals("simple line", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextTabStopsExceedingWidth()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.setNewLine("\n");
        // nextLineTabStop (10) >= width (8), forcing nextLineTabStop = 1 branch
        formatter.renderWrappedText(sb, 8, 10, "first line to wrap here");
        assertTrue(sb.toString().contains("\n"));
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextPosEqualsNextLineTabStopMinusOne()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.setNewLine("\n");
        // Carefully crafted text:
        // width = 10, nextLineTabStop = 5
        // First line splits on space: "line1 "
        // Second iteration text begins with padding(5) + "1234567890" = "     1234567890" (len 15)
        // findWrapPos will find space at index 4 (= 5 - 1 = nextLineTabStop - 1)
        // This triggers pos = width branch
        formatter.renderWrappedText(sb, 10, 5, "line1 1234567890123");
        String result = sb.toString();
        assertTrue(result.contains("\n"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Ground Truth (CLI Defects4J Regression)
    // =========================================================================

    /**
     * Targets known defect where HelpFormatter.renderWrappedText strips leading
     * indentation from multi-line/wrapped headers and footers using text.substring(pos).trim().
     * This test asserts the exact expected behavior from HelpFormatterTest.testIndentedHeaderAndFooter.
     */
    @Test(timeout = 4000)
    public void testIndentedHeaderAndFooter()
    {
        HelpFormatter formatter = new HelpFormatter();
        String header = "  Header1\n  Header2";
        String footer = "  Footer1\n  Footer2";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "foobar", header, new Options(), 2, 2, footer, true);
        pw.flush();

        String eol = formatter.getNewLine();
        String expected = "usage: foobar" + eol +
                          "  Header1" + eol +
                          "  Header2" + eol +
                          eol +
                          "  Footer1" + eol +
                          "  Footer2" + eol;

        assertEquals(expected, out.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntaxThrowsException()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    @Test(expected = IllegalArgumentException