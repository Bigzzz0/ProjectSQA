package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.cli.HelpFormatter
 *
 * Core Decision Branches Analyzed:
 * 1. printHelp(...) parameter validation:
 *    - (cmdLineSyntax == null || cmdLineSyntax.length() == 0) -> IllegalArgumentException
 * 2. autoUsage branch:
 *    - autoUsage == true  -> printUsage(pw, width, cmdLineSyntax, options)
 *    - autoUsage == false -> printUsage(pw, width, cmdLineSyntax)
 * 3. Header / Footer formatting:
 *    - header != null && header.trim().length() > 0 vs null/blank header
 *    - footer != null && footer.trim().length() > 0 vs null/blank footer
 * 4. Option comparator sorting:
 *    - setOptionComparator(null) resets to default OptionComparator
 *    - setOptionComparator(customComparator) uses custom order
 *    - OptionComparator.compare(o1, o2) comparing keys case-insensitively
 * 5. appendOptionGroup(...) & OptionGroup handling:
 *    - group.isRequired() == true vs false (delimiters '[' and ']')
 *    - multiple options in group separated by " | "
 *    - processedGroups caching (avoid duplicate display of grouped options)
 * 6. appendOption(...) formatting:
 *    - option.isRequired() == true vs false
 *    - option.getOpt() != null (short) vs opt == null (long-only)
 *    - option.hasArg() && option.hasArgName() vs option.hasArg() without argName
 * 7. renderOptions(...) alignment:
 *    - opt == null (uses defaultLongOptPrefix) vs opt != null
 *    - option.hasLongOpt() appending comma and longOpt
 *    - option.hasArg() with argName vs without argName (appends space)
 *    - optBuf.length() < max (padding calculation)
 *    - option.getDescription() != null vs null
 * 8. renderWrappedText(...) & findWrapPos(...):
 *    - text fits within width (pos == -1)
 *    - nextLineTabStop >= width adjustment (nextLineTabStop = width - 1)
 *    - findWrapPos finds '\n' or '\t' <= width
 *    - startPos + width >= text.length() -> -1
 *    - backward whitespace search (finds ' ', '\n', '\r')
 *    - forward whitespace search when no whitespace before startPos + width
 *    - CLI-162 Defect: (text.length() > width) && (pos == nextLineTabStop - 1) -> pos = width
 *      Chunking long unbroken arguments with left/desc indentation.
 * ---------------------------------------------------------------------------------------------------------
 */
public class HelpFormatterGeminiTest
{
    private static final String EOL = System.getProperty("line.separator");

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndSettersConfiguration()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());

        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        formatter.setDescPadding(6);
        assertEquals(6, formatter.getDescPadding());

        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());

        assertEquals(EOL, formatter.getNewLine());
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testPrintHelpSimpleCommandLineSyntax()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "myApp", "Header line", options, 2, 4, "Footer line", false);

        String output = sw.toString();
        assertTrue("Output should contain syntax", output.contains("usage: myApp"));
        assertTrue("Output should contain header", output.contains("Header line"));
        assertTrue("Output should contain option short opt", output.contains("-h"));
        assertTrue("Output should contain option long opt", output.contains("--help"));
        assertTrue("Output should contain option description", output.contains("display help"));
        assertTrue("Output should contain footer", output.contains("Footer line"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpAutoUsageWithRequiredAndOptionalOptions()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        Option optReq = new Option("r", "required-opt", true, "Required option");
        optReq.setRequired(true);
        optReq.setArgName("REQ_ARG");

        Option optOpt = new Option("o", "optional-opt", false, "Optional flag");
        optOpt.setRequired(false);

        options.addOption(optReq);
        options.addOption(optOpt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 100, "sampleApp", null, options, 1, 3, null, true);

        String output = sw.toString();
        assertTrue("Usage must include required option without brackets", output.contains("-r <REQ_ARG>"));
        assertTrue("Usage must include optional option with brackets", output.contains("[-o]"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpConvenienceOverloadsCoverage()
    {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try
        {
            System.setOut(new PrintStream(bytes));
            HelpFormatter formatter = new HelpFormatter();
            Options options = new Options();
            options.addOption("v", "version", false, "show version");

            formatter.printHelp("app1", options);
            formatter.printHelp("app2", options, true);
            formatter.printHelp("app3", "header", options, "footer");
            formatter.printHelp("app4", "header", options, "footer", true);
            formatter.printHelp(80, "app5", "header", options, "footer");
            formatter.printHelp(80, "app6", "header", options, "footer", true);

            String stdout = bytes.toString();
            assertTrue(stdout.contains("app1"));
            assertTrue(stdout.contains("app2"));
            assertTrue(stdout.contains("app3"));
            assertTrue(stdout.contains("app4"));
            assertTrue(stdout.contains("app5"));
            assertTrue(stdout.contains("app6"));
        }
        finally
        {
            System.setOut(originalOut);
        }
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithPrintWriterOverload()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("d", "debug", false, "turn on debugging");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 60, "debugApp", "headerText", options, 2, 2, "footerText");

        String result = sw.toString();
        assertTrue(result.contains("usage: debugApp"));
        assertTrue(result.contains("headerText"));
        assertTrue(result.contains("-d,--debug"));
        assertTrue(result.contains("footerText"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBlankHeadersAndFootersIgnored()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "target file");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "myApp", "   ", options, 1, 3, "   \t\n  ", false);

        String result = sw.toString();
        assertFalse("Blank header should not render blank line block", result.startsWith("   "));
        assertFalse("Blank footer should not be rendered", result.endsWith("   \t\n  " + EOL));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingBoundaries()
    {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    @Test(timeout = 4000)
    public void testRtrimBoundaries()
    {
        HelpFormatter formatter = new HelpFormatter();
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   \t\r\n"));
        assertEquals("test", formatter.rtrim("test"));
        assertEquals("test", formatter.rtrim("test   "));
        assertEquals("  test", formatter.rtrim("  test  \t "));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosSpecialCharacters()
    {
        HelpFormatter formatter = new HelpFormatter();

        int posLf = formatter.findWrapPos("line1\nline2", 10, 0);
        assertEquals("Should break after newline", 6, posLf);

        int posTab = formatter.findWrapPos("col1\tcol2", 10, 0);
        assertEquals("Should break after tab", 5, posTab);

        int posEnd = formatter.findWrapPos("short", 10, 0);
        assertEquals("Text shorter than width should return -1", -1, posEnd);

        int posNormal = formatter.findWrapPos("one two three four", 10, 0);
        assertEquals("Should break at space preceding width limit", 7, posNormal);

        int posLookahead = formatter.findWrapPos("unbrokenlongword next", 10, 0);
        assertEquals("Should look ahead for whitespace when unbroken word exceeds width", 16, posLookahead);
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextWithNarrowWidthAndLargeTabstop()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();

        formatter.renderWrappedText(sb, 10, 15, "This is a sentence that should wrap gracefully even with large tab");

        String result = sb.toString();
        String[] lines = result.split(formatter.getNewLine());
        for (String line : lines)
        {
            assertTrue("Lines should not exceed width excessively", line.length() <= 10 || !line.contains(" "));
        }
    }

    @Test(timeout = 4000)
    public void testRenderOptionsFormatVariations()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        Option optLongOnly = new Option(null, "long-only", false, "Description of long only");
        Option optShortOnly = new Option("s", false, "Description of short only");
        Option optBoth = new Option("b", "both", true, "Description of both");
        optBoth.setArgName("ARG_NAME");
        Option optNoArgName = new Option("n", true, "Description with empty argName");
        optNoArgName.setArgName("");
        Option optNoDesc = new Option("x", "no-desc", false, null);

        options.addOption(optLongOnly);
        options.addOption(optShortOnly);
        options.addOption(optBoth);
        options.addOption(optNoArgName);
        options.addOption(optNoDesc);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);

        String rendered = sb.toString();
        assertTrue(rendered.contains("--long-only"));
        assertTrue(rendered.contains("-s"));
        assertTrue(rendered.contains("-b,--both <ARG_NAME>"));
        assertTrue(rendered.contains("-n "));
        assertTrue(rendered.contains("-x,--no-desc"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageVariants()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "SingleCommandNoSpaces");
        formatter.printUsage(pw, 80, "Command With Arguments");

        String result = sw.toString();
        assertTrue(result.contains("usage: SingleCommandNoSpaces"));
        assertTrue(result.contains("usage: Command With Arguments"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupHandlingInUsage()
    {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        Option optA = new Option("a", "alpha", false, "Option Alpha");
        Option optB = new Option("b", "beta", true, "Option Beta");
        optionalGroup.addOption(optA);
        optionalGroup.addOption(optB);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        Option optC = new Option("c", false, "Option C");
        Option optD = new Option(null, "delta", false, "Option Delta");
        requiredGroup.addOption(optC);
        requiredGroup.addOption(optD);

        options.addOptionGroup(optionalGroup);
        options.addOptionGroup(requiredGroup);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 120, "groupApp", options);

        String output = sw.toString();
        assertTrue("Optional group must be enclosed in brackets", output.contains("[-a | -b <arg>]"));
        assertTrue("Required group must not be enclosed in brackets", output.contains("-c | --delta"));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (CLI-162 / BugCLI162Test)
    // =========================================================================

    /**
     * Targets BugCLI162: Long argument description indentation ignoring descPadding
     * when an unbroken long argument name wraps onto the subsequent lines.
     */
    @Test(timeout = 4000)
    public void testLongLineChunkingIndentIgnored()
    {
        Options options = new Options();
        options.addOption("x", "extralongarg", false,
                "Thisisatestofanextremelylongargumentnamethatwillforcethewrapperstooperateandscaleforeverytackonanotherwordthatwillsomehowendupbeingreallywideandforceabandtodisappearwhenwrappedsoletstestthisoutandseemagichappenormaybenot");

        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 35, "app", "header", options, 0, 5, "footer");

        String expected = "usage: app" + EOL +
                "header" + EOL +
                "-x,--extralongarg" + EOL +
                "     Thisisatestofanextremelylongarg" + EOL +
                "     umentnamethatwillforcethewrappe" + EOL +
                "     rstooperateandscaleforeverytack" + EOL +
                "     onanotherwordthatwillsomehowend" + EOL +
                "     upbeingreallywideandforceabandt" + EOL +
                "     odisappearwhenwrappedsoletstest" + EOL +
                "     thisoutandseemagichappenormaybe" + EOL +
                "     not" + EOL +
                "footer" + EOL;

        assertEquals("Long arguments did not split as expected", expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testLongLineChunkingZeroPadding()
    {
        Options options = new Options();
        options.addOption("x", "extralongarg", false,
                "Thisisatestofanextremelylongargumentnamethatwillforcethewrapperstooperateandscaleforeverytackonanotherwordthatwillsomehowendupbeingreallywideandforceabandtodisappearwhenwrappedsoletstestthisoutandseemagichappenormaybenot");

        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 35, "app", "header", options, 0, 0, "footer");

        String expected = "usage: app" + EOL +
                "header" + EOL +
                "-x,--extralongarg" + EOL +
                "Thisisatestofanextremelylongargument" + EOL +
                "namethatwillforcethewrapperstooperat" + EOL +
                "eandscaleforeverytackonanotherwordth" + EOL +
                "atwillsomehowendupbeingreallywideand" + EOL +
                "forceabandtodisappearwhenwrappedsole" + EOL +
                "ttestthisoutandseemagichappenormaybe" + EOL +
                "not" + EOL +
                "footer" + EOL;

        assertEquals(expected, sw.toString());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntaxThrowsException()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntaxThrowsException()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer");
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, Sorting & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultOptionComparatorCaseInsensitiveOrder()
    {
        HelpFormatter formatter = new HelpFormatter();
        Comparator comp = formatter.getOptionComparator();
        assertNotNull("Default comparator must not be null", comp);

        Option optA = new Option("a", "alpha", false, "desc");
        Option optB = new Option("B", "beta", false, "desc");
        Option optLongOnly = new Option(null, "zeta", false, "desc");

        assertTrue("a should precede B in case-insensitive comparison", comp.compare(optA, optB) < 0);
        assertTrue("B should follow a in case-insensitive comparison", comp.compare(optB, optA) > 0);
        assertEquals("Identical keys should evaluate to 0", 0, comp.compare(optA, new Option("A", false, null)));
        assertTrue("Option with short opt 'a' should precede longOpt 'zeta'", comp.compare(optA, optLongOnly) < 0);
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorCustomAndResetNull()
    {
        HelpFormatter formatter = new HelpFormatter();

        Comparator reverseComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        };

        formatter.setOptionComparator(reverseComparator);
        assertSame("Should use custom comparator", reverseComparator, formatter.getOptionComparator());

        Options options = new Options();
        options.addOption("a", false, "desc a");
        options.addOption("z", false, "desc z");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);

        String usage = sw.toString();
        int posZ = usage.indexOf("-z");
        int posA = usage.indexOf("-a");
        assertTrue("Custom reverse comparator must list -z before -a", posZ != -1 && posA != -1 && posZ < posA);

        formatter.setOptionComparator(null);
        assertNotNull("Resetting comparator with null should reinstate default comparator", formatter.getOptionComparator());
        assertNotSame(reverseComparator, formatter.getOptionComparator());
    }

    @Test(timeout = 4000)
    public void testPrintWrappedOverloadsDirect()
    {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 20, "Simple wrapped text test");
        formatter.printWrapped(pw, 20, 4, "Tabbed wrapped text line goes here");

        String result = sw.toString();
        assertTrue(result.contains("Simple wrapped text"));
        assertTrue(result.contains("Tabbed"));
    }
}