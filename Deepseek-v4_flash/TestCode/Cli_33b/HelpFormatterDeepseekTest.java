package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * White-box test suite for HelpFormatter targeting known defect and high coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Defect: `testIndentedHeaderAndFooter` fails due to leading whitespace being
 *   stripped in `renderWrappedText` when wrapping text (the `trim()` call on
 *   the remainder removes indentation).
 * - Partitions:
 *   A) Core functional getters/setters (width, padding, prefixes, etc.)
 *   B) Helper methods: createPadding, rtrim, findWrapPos
 *   C) Text rendering: renderWrappedText, renderOptions, printWrapped, printUsage
 *   D) printHelp: variations with/without header/footer/autoUsage
 *   E) OptionGroup handling in printUsage
 *   F) Edge cases: null/empty options, blank argName, longOpt only, optional/required
 *   G) Exception paths: null/empty cmdLineSyntax
 *   H) Comparator management (null reset)
 *   I) Defect-specific test for indented header/footer
 */
public class HelpFormatterDeepseekTest {

    // ---------------------------------------------------------
    // Partition A: Core Getters/Setters and Property Access
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testSetGetWidth() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, f.getWidth());
        f.setWidth(100);
        assertEquals(100, f.getWidth());
    }

    @Test(timeout = 4000)
    public void testSetGetLeftPadding() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, f.getLeftPadding());
        f.setLeftPadding(5);
        assertEquals(5, f.getLeftPadding());
    }

    @Test(timeout = 4000)
    public void testSetGetDescPadding() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, f.getDescPadding());
        f.setDescPadding(7);
        assertEquals(7, f.getDescPadding());
    }

    @Test(timeout = 4000)
    public void testSetGetSyntaxPrefix() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, f.getSyntaxPrefix());
        f.setSyntaxPrefix("usage: ");
        assertEquals("usage: ", f.getSyntaxPrefix());
        f.setSyntaxPrefix("USAGE: ");
        assertEquals("USAGE: ", f.getSyntaxPrefix());
    }

    @Test(timeout = 4000)
    public void testSetGetNewLine() {
        HelpFormatter f = new HelpFormatter();
        String defaultNewLine = System.getProperty("line.separator");
        assertEquals(defaultNewLine, f.getNewLine());
        f.setNewLine("\n");
        assertEquals("\n", f.getNewLine());
    }

    @Test(timeout = 4000)
    public void testSetGetOptPrefix() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, f.getOptPrefix());
        f.setOptPrefix("+");
        assertEquals("+", f.getOptPrefix());
    }

    @Test(timeout = 4000)
    public void testSetGetLongOptPrefix() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, f.getLongOptPrefix());
        f.setLongOptPrefix("---");
        assertEquals("---", f.getLongOptPrefix());
    }

    @Test(timeout = 4000)
    public void testSetGetLongOptSeparator() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR, f.getLongOptSeparator());
        f.setLongOptSeparator("=");
        assertEquals("=", f.getLongOptSeparator());
    }

    @Test(timeout = 4000)
    public void testSetGetArgName() {
        HelpFormatter f = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, f.getArgName());
        f.setArgName("file");
        assertEquals("file", f.getArgName());
    }

    @Test(timeout = 4000)
    public void testOptionComparatorDefault() {
        HelpFormatter f = new HelpFormatter();
        Comparator comp = f.getOptionComparator();
        assertNotNull(comp);
        assertTrue(comp instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorNullResetsToDefault() {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(null);
        assertNotNull(f.getOptionComparator());
        assertTrue(f.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSetOptionComparatorCustom() {
        HelpFormatter f = new HelpFormatter();
        Comparator reverse = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option) o2).getKey().compareToIgnoreCase(((Option) o1).getKey());
            }
        };
        f.setOptionComparator(reverse);
        assertSame(reverse, f.getOptionComparator());
    }

    // ---------------------------------------------------------
    // Partition B: Helper Methods createPadding, rtrim, findWrapPos
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreatePaddingZero() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("", f.createPadding(0));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingPositive() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("   ", f.createPadding(3));
    }

    @Test(timeout = 4000)
    public void testRtrimNull() {
        HelpFormatter f = new HelpFormatter();
        assertNull(f.rtrim(null));
    }

    @Test(timeout = 4000)
    public void testRtrimEmpty() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("", f.rtrim(""));
    }

    @Test(timeout = 4000)
    public void testRtrimNoTrailing() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("hello", f.rtrim("hello"));
    }

    @Test(timeout = 4000)
    public void testRtrimTrailingSpaces() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("hello", f.rtrim("hello   "));
    }

    @Test(timeout = 4000)
    public void testRtrimTrailingTabsNewlines() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("hello", f.rtrim("hello \t\n"));
    }

    @Test(timeout = 4000)
    public void testRtrimAllSpaces() {
        HelpFormatter f = new HelpFormatter();
        assertEquals("", f.rtrim("   "));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNewlineWithinWidth() {
        HelpFormatter f = new HelpFormatter();
        // text: "ab\ncd" width=4, start=0 -> newline at pos 2 <= width => returns 3
        assertEquals(3, f.findWrapPos("ab\ncd", 4, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosTabWithinWidth() {
        HelpFormatter f = new HelpFormatter();
        // "ab\tcd" width=4, start=0 -> tab at pos 2 <= width => returns 3
        assertEquals(3, f.findWrapPos("ab\tcd", 4, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosEndOfText() {
        HelpFormatter f = new HelpFormatter();
        // text length = 5, start+width >= length => -1
        assertEquals(-1, f.findWrapPos("hello", 10, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosWhitespaceBeforeBound() {
        HelpFormatter f = new HelpFormatter();
        // "abc de fgh" width=4, start=0 => last whitespace before pos=4 is at pos 3 (space)
        assertEquals(3, f.findWrapPos("abc de fgh", 4, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNoWhitespaceChopAtWidth() {
        HelpFormatter f = new HelpFormatter();
        // "abcdef" width=4, start=0 => no whitespace, chop at 4, but pos=4 != text.length => returns 4
        assertEquals(4, f.findWrapPos("abcdef", 4, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosChopAtTextLength() {
        HelpFormatter f = new HelpFormatter();
        // "abcdef" width=5, start=0 => chops at 5 but that is text.length => -1
        assertEquals(-1, f.findWrapPos("abcde", 5, 0));
    }

    // ---------------------------------------------------------
    // Partition C: renderWrappedText and printWrapped
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testRenderWrappedTextNoWrap() {
        HelpFormatter f = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        f.renderWrappedText(sb, 10, 0, "short");
        assertEquals("short", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextSimpleWrap() {
        HelpFormatter f = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        f.renderWrappedText(sb, 5, 0, "hello world");
        // expect "hello" + newline + " world" (but world is trimmed? Let's compute)
        // first pos = findWrapPos("hello world",5,0) -> whitespace at 5? Actually "hello world": after 5 chars is space at pos5? Let's simulate: text "hello world" indices: 0h,1e,2l,3l,4o,5(space),6w,7o,8r,9l,10d.
        // width=5, startPos=0 => pos=startPos+width=5, look backwards for whitespace: pos=5 is space, so returns 5. Then sb.append(rtrim("hello"))->"hello", newline.
        // then text = padding(0) + text.substring(5).trim() -> " world".trim()="world". Now findWrapPos("world",5,0) => text length 5, startPos+width>=length => -1, so sb.append("world") => "helloworld"? Actually no, we appended "hello"+newline then "world". So result "hello\nworld".
        // newline is f.getNewLine() which is system default, likely \n. So assertEquals("hello\nworld",...);
        assertEquals("hello" + f.getNewLine() + "world", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextNextLineTabStopTooLarge() {
        HelpFormatter f = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        // width=5, nextLineTabStop=8 => after first wrap, nextLineTabStop >= width triggers reset to 1.
        f.renderWrappedText(sb, 5, 8, "this is a long text");
        String result = sb.toString();
        assertTrue(result.contains(f.getNewLine()));
        // we just ensure no infinite loop; check it finishes
    }

    @Test(timeout = 4000)
    public void testRenderWrappedTextEdgeCasePosEqualsNextLineTabStopMinusOne() {
        HelpFormatter f = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        // This exercises the branch: (text.length() > width) && (pos == nextLineTabStop - 1)
        // We need a scenario where after wrapping, the next line's first wrap pos equals nextLineTabStop-1.
        f.setNewLine("\n");
        // For simplicity, just call with some text that triggers it; it's internal.
        f.renderWrappedText(sb, 10, 3, "abcdefghijklmnop");
        // The loop will handle it; no exception is enough.
    }

    @Test(timeout = 4000)
    public void testPrintWrapped() {
        HelpFormatter f = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printWrapped(pw, 10, "hello world");
        pw.flush();
        // printWrapped calls renderWrappedText(sb, width, 0, text) then pw.println(sb)
        // So output = sb + line separator (println adds line separator)
        StringBuffer sb = new StringBuffer();
        f.renderWrappedText(sb, 10, 0, "hello world");
        String expected = sb.toString() + f.getNewLine();
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWrappedWithNextLineTabStop() {
        HelpFormatter f = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printWrapped(pw, 10, 3, "hello world");
        pw.flush();
        // similar test but with nextLineTabStop
        StringBuffer sb = new StringBuffer();
        f.renderWrappedText(sb, 10, 3, "hello world");
        assertEquals(sb.toString() + f.getNewLine(), sw.toString());
    }

    // ---------------------------------------------------------
    // Partition D: printUsage (both overloads)
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintUsageSimple() {
        HelpFormatter f = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printUsage(pw, 50, "app");
        pw.flush();
        // usage: app ...? Actually printUsage(PrintWriter,int,String) prints defaultSyntaxPrefix+app wrapped at argPos.
        // Let's compute: defaultSyntaxPrefix = "usage: ", app "app" => "usage: app"
        // It calls printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos, ...)
        // Where argPos = app.indexOf(' ')+1 = app has no space => -1+1=0? Actually indexOf(' ') returns -1, +1=0. So nextLineTabStop = defaultSyntaxPrefix.length() + 0 = 7.
        // It prints the string "usage: app" wrapped with that indent? Hard to predict exactly. Check that output contains "usage: app"
        assertTrue(sw.toString().contains("usage: app"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptions() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha desc");
        options.addOption("b", "beta", true, "beta desc");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printUsage(pw, 50, "app", options);
        pw.flush();
        String out = sw.toString();
        // Should contain usage clause with -a and -b <arg>
        assertTrue(out.contains("-a"));
        assertTrue(out.contains("-b"));
        assertTrue(out.contains("<arg>"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroup() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("x").longOpt("xray").desc("x option").build());
        group.addOption(Option.builder("y").longOpt("yankee").desc("y option").build());
        options.addOptionGroup(group);
        options.addOption("z", "zulu", false, "standalone");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printUsage(pw, 50, "app", options);
        pw.flush();
        String out = sw.toString();
        // Should contain "[-x | -y]" (since group not required) and "-z"
        assertTrue(out.contains("[-x | -y]"));
        assertTrue(out.contains("-z"));
    }

    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroup() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").desc("A").build());
        group.addOption(Option.builder("b").desc("B").build());
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printUsage(pw, 50, "app", options);
        pw.flush();
        String out = sw.toString();
        // Required group => no brackets; should contain "-a | -b"
        assertTrue(out.contains("-a | -b"));
        assertFalse(out.contains("[-a | -b]"));
    }

    // ---------------------------------------------------------
    // Partition E: renderOptions and printOptions
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintOptionsSimple() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta with arg");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("-a"));
        assertTrue(out.contains("--alpha"));
        assertTrue(out.contains("-b"));
        assertTrue(out.contains("--beta"));
        assertTrue(out.contains("<arg>"));
    }

    @Test(timeout = 4000)
    public void testPrintOptionsWithBlankArgName() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        Option opt = Option.builder("c").longOpt("charlie").hasArg().argName("").desc("blank arg").build();
        options.addOption(opt);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String out = sw.toString();
        // The argName is blank, so it should not append "<>", just a space? Actually code:
        // if (option.hasArg()) { String argName = option.getArgName(); if (argName != null && argName.length() == 0) { optBuf.append(' '); } else { ... append "<argname>" }
        // So it should just have a space after the option(s), no "<>". So line might be "... -c, --charlie  ..." (two spaces? one from else? Actually getArgName() returns "" so condition true -> append ' ') so there will be an extra space.
        assertTrue(out.contains("--charlie"));
        // Check no "<>" appears
        assertFalse(out.contains("<>"));
    }

    @Test(timeout = 4000)
    public void testPrintOptionsWithNullArgName() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        // Option with hasArg but argName null -> will use defaultArgName
        Option opt = Option.builder("d").longOpt("delta").hasArg().argName(null).desc("null arg").build();
        options.addOption(opt);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("<arg>"));
    }

    // ---------------------------------------------------------
    // Partition F: printHelp variations
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintHelpBasic() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("usage:"));
        assertTrue(out.contains("app"));
        assertTrue(out.contains("-v"));
        assertTrue(out.contains("--verbose"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderFooter() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "aaa", false, "desc");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, "cmd", "Header", options, 1, 3, "Footer", false);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("Header"));
        assertTrue(out.contains("Footer"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpAutoUsage() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("o", false, "opt");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        String out = sw.toString();
        // autoUsage should print usage with options
        assertTrue(out.contains("[-o]"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpConvenienceMethods() {
        // We just call the convenience overloads to ensure no exception; output to System.out is fine but we can redirect after? We'll just call and ensure they don't throw.
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "xxx", false, "xxx desc");
        // These write to System.out; we can't easily capture, but just verify no exception.
        f.printHelp("app", options);
        f.printHelp("app", options, false);
        f.printHelp("app", "Header", options, "Footer");
        f.printHelp("app", "Header", options, "Footer", true);
        f.printHelp(80, "app", "Header", options, "Footer");
        f.printHelp(80, "app", "Header", options, "Footer", true);
    }

    // ---------------------------------------------------------
    // Partition G: Exception Handling
    // ---------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, null, null, options, 1, 3, null, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, "", null, options, 1, 3, null, false);
    }

    // ---------------------------------------------------------
    // Partition H: Defect-Specific Test (indented header/footer)
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testIndentedHeaderAndFooter() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String header = "   Header with indent";
        String footer = "   Footer with indent";
        f.printHelp(pw, 80, "cmd", header, options, 1, 3, footer, false);
        pw.flush();
        String result = sw.toString();
        // The header and footer should appear with their leading whitespace preserved.
        // The known bug causes the leading whitespace to be stripped after the first wrap.
        assertTrue("Header indentation preserved", result.contains("   Header with indent"));
        assertTrue("Footer indentation preserved", result.contains("   Footer with indent"));
    }

    // ---------------------------------------------------------
    // Partition I: Additional edge cases for renderOptions
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testRenderOptionsOptionWithNullLongOpt() {
        // Option with only short opt (no long) and no arg
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("s", null, false, "short only");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("-s"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsOptionWithOnlyLongOpt() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "long-only", false, "long only desc");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String out = sw.toString();
        assertTrue(out.contains("--long-only"));
    }

    @Test(timeout = 4000)
    public void testRenderOptionsOptionWithArgAndNoArgName() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        Option opt = Option.builder("x").longOpt("xray").hasArg().argName(null).desc("arg but null name").build();
        options.addOption(opt);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String out = sw.toString();
        // Should use default arg name "arg"
        assertTrue(out.contains("<arg>"));
    }

    // ---------------------------------------------------------
    // Partition J: AppendOption (via printUsage) for long opt only
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrintUsageLongOptOnly() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "verbose", false, "verbose flag");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printUsage(pw, 50, "app", options);
        pw.flush();
        String out = sw.toString();
        // Should show --verbose
        assertTrue(out.contains("--verbose"));
    }

    // ---------------------------------------------------------
    // Partition K: Option ordering via comparator
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testOptionsSortedByComparator() {
        HelpFormatter f = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "beta");
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("c", "gamma", false, "gamma");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String out = sw.toString();
        // Default comparator sorts by key case-insensitive: a, b, c.
        int idxA = out.indexOf("-a");
        int idxB = out.indexOf("-b");
        int idxC = out.indexOf("-c");
        assertTrue(idxA >= 0 && idxB >= 0 && idxC >= 0);
        assertTrue(idxA < idxB && idxB < idxC);
    }

    // ---------------------------------------------------------
    // Partition L: setNewLine changes output
    // ---------------------------------------------------------

    @Test(timeout = 4000)
    public void testNewLineCustom() {
        HelpFormatter f = new HelpFormatter();
        f.setNewLine("<br>");
        Options options = new Options();
        options.addOption("v", false, "verbose");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        f.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        String out = sw.toString();
        // Should contain "<br>" instead of system line separator
        assertTrue(out.contains("<br>"));
    }
}