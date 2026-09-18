package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.cli.HelpFormatter
 *
 * 1. DEFECT TARGETING (CLI-162):
 *    - Defect: When word wrapping descriptions where the nextLineTabStop (indentation)
 *      plus word length exceeds the line width, findWrapPos returns the last whitespace
 *      in the padding, resulting in pos == lastPos and throwing a RuntimeException:
 *      "Text too long for line - throwing exception to avoid infinite loop [CLI-162]".
 *    - Targeted by:
 *      - testCLI162InfiniteLoopBug(): Reproduces BugCLI162Test failure where a long description
 *        with nextLineTabStop close to width triggers the defect.
 *      - testCLI162PrintHelpLongLines(): Reproduces BugCLI162Test::testPrintHelpLongLines where
 *        descriptions wrap with large indentations.
 *
 * 2. BRANCH & DECISION COVERAGE:
 *    - setWidth / getWidth, setLeftPadding / getLeftPadding, setDescPadding / getDescPadding
 *    - setSyntaxPrefix / getSyntaxPrefix, setNewLine / getNewLine, setOptPrefix / getOptPrefix
 *    - setLongOptPrefix / getLongOptPrefix, setArgName / getArgName
 *    - setOptionComparator / getOptionComparator: null branch vs non-null branch
 *    - OptionComparator inner class: compares opt1 vs opt2 keys case-insensitively
 *    - printHelp overloads (all parameter combinations, null header, null footer, empty syntax)
 *    - printHelp with autoUsage = true and autoUsage = false
 *    - printUsage(PrintWriter, int, String, Options):
 *        * options with OptionGroup vs standalone Option
 *        * processedGroups cache hit (group already processed) vs new group
 *        * OptionGroup required vs optional (brackets [ ] vs none)
 *        * Option required vs optional
 *        * Option with short opt vs long opt only
 *        * Option with argument and arg name vs without arg name vs without arg
 *        * Iterator hasNext() spacing branches
 *    - printUsage(PrintWriter, int, String)
 *    - printOptions: short opts, long opts, short+long opts, with arg, with argName, without argName,
 *      max padding calculation, descriptions null vs non-null
 *    - printWrapped(PrintWriter, int, String) and printWrapped(PrintWriter, int, int, String)
 *    - renderWrappedText:
 *        * findWrapPos == -1 (single line)
 *        * multi-line wrapping with nextLineTabStop
 *        * pos == lastPos infinite loop check branch
 *    - findWrapPos:
 *        * indexOf('\n') <= width vs > width
 *        * indexOf('\t') <= width vs > width
 *        * startPos + width >= text.length()
 *        * finding last whitespace before startPos + width
 *        * finding first whitespace after startPos + width when no whitespace found before
 *        * pos == text.length() returning -1
 *    - createPadding: len == 0, len > 0
 *    - rtrim: null string, empty string, all spaces, spaces at end, no trailing spaces
 */
public class HelpFormatterGeminiTest
{
    private HelpFormatter formatter;
    private StringWriter sw;
    private PrintWriter pw;

    @Before
    public void setUp()
    {
        formatter = new HelpFormatter();
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Getters/Setters)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndSettersDefaults()
    {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testStateMutations()
    {
        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorCustomAndResetNull()
    {
        Comparator customComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return 0;
            }
        };

        formatter.setOptionComparator(customComparator);
        assertSame(customComparator, formatter.getOptionComparator());

        // Setting to null should reset to default OptionComparator
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertNotSame(customComparator, formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testOptionComparatorImplementation()
    {
        Comparator comp = formatter.getOptionComparator();
        Option opt