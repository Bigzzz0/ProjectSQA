package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HelpFormatterDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: org.apache.commons.cli.HelpFormatter
     * 
     * Known Defect: testPrintOptionWithEmptyArgNameUsage - When an Option has an
     * empty argName (""), the usage string incorrectly renders "-f[]" instead of "-f".
     * The bug is in appendOption() where it checks `option.getArgName() != null` but
     * does not check for empty string, causing the brackets to be appended.
     * 
     * Branches targeted:
     * 1. appendOption: required flag true/false
     * 2. appendOption: option.getOpt() null/non-null
     * 3. appendOption: option.hasLongOpt() true/false
     * 4. appendOption: option.hasArg() true/false
     * 5. appendOption: option.getArgName() null/non-null/empty
     * 6. findWrapPos: newline/tab found before width, no whitespace found, 
     *    whitespace at startPos, wrap at end of text
     * 7. renderWrappedText: pos == -1, pos > 0, text shorter than width
     * 8. printUsage: option groups, required groups, auto-usage generation
     * 9. printHelp: null/empty cmdLineSyntax, header/footer trimming
     * 10. OptionComparator: null options, case-insensitive sorting
     * 11. createPadding: zero, positive, negative lengths
     * 12. rtrim: null, empty, trailing spaces
     * 13. Setters/Getters: all public fields
     * 
     * Boundary conditions:
     * - width = 0, 1, DEFAULT_WIDTH, Integer.MAX_VALUE
     * - leftPad/descPad = 0, negative, large values
     * - text length = 0, 1, width-1, width, width+1
     * - nextLineTabStop = 0, positive, > width
     * - Option with null opt, null longOpt, null argName, empty argName
     * - OptionGroup with null options, required/non-required
     */
    
    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testDefaultValues() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());
    }
    
    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.setLeftPadding(5);
        formatter.setDescPadding(7);
        formatter.setSyntaxPrefix("SYNTAX: ");
        formatter.setNewLine("\r\n");
        formatter.setOptPrefix("/");
        formatter.setLongOptPrefix("//");
        formatter.setArgName("file");
        
        assertEquals(100, formatter.getWidth());
        assertEquals(5, formatter.getLeftPadding());
        assertEquals(7, formatter.getDescPadding());
        assertEquals("SYNTAX: ", formatter.getSyntaxPrefix());
        assertEquals("\r\n", formatter.getNewLine());
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("file", formatter.getArgName());
    }
    
    @Test(timeout = 4000)
    public void testSetOptionComparatorNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        // Should reset to default comparator
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }
    
    @Test(timeout = 4000)
    public void testSetOptionComparatorCustom() {
        HelpFormatter formatter = new HelpFormatter();
        Comparator customComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComparator);
        assertSame(customComparator, formatter.getOptionComparator());
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testPrintWrappedNullText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, null);
        pw.flush();
        assertEquals("", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedEmptyText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, "");
        pw.flush();
        assertEquals("", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedShortText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, "short");
        pw.flush();
        assertEquals("short", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedExactWidth() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 5, "12345");
        pw.flush();
        assertEquals("12345", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedLongTextNoSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 5, "abcdefghij");
        pw.flush();
        assertEquals("abcde" + formatter.getNewLine() + "fghij", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedWithSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, "hello world foo");
        pw.flush();
        assertEquals("hello" + formatter.getNewLine() + "world foo", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedWithNewLine() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 20, "line1\nline2");
        pw.flush();
        assertEquals("line1" + formatter.getNewLine() + "line2", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedWithTab() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 20, "col1\tcol2");
        pw.flush();
        assertEquals("col1" + formatter.getNewLine() + "col2", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedNextLineTabStop() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, 3, "hello world foo");
        pw.flush();
        assertEquals("hello" + formatter.getNewLine() + "   world" + formatter.getNewLine() + "   foo", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedZeroWidth() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 0, "test");
        pw.flush();
        assertEquals("test", sw.toString());
    }
    
    @Test(timeout = 4000)
    public void testPrintWrappedNegativeWidth() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, -5, "test");
        pw.flush();
        assertEquals("test", sw.toString());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testPrintOptionWithEmptyArgNameUsage() {
        // This test targets the known defect: empty argName should not produce "[]"
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "file option");
        option.setArgName(""); // Empty argName
        options.addOption(option);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        // The bug produces "usage: app -f[]" but correct is "usage: app -f"
        assertFalse("Empty argName should not produce brackets", usage.contains("[]"));
        assertTrue("Usage should contain -f", usage.contains("-f"));
        assertFalse("Usage should not contain []", usage.contains("[]"));
    }
    
    @Test(timeout = 4000)
    public void testPrintOptionWithNullArgNameUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "file option");
        option.setArgName(null);
        options.addOption(option);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Null argName should not produce brackets", usage.contains("[]"));
        assertTrue("Usage should contain -f", usage.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintOptionWithArgNameUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "file option");
        option.setArgName("file");
        options.addOption(option);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Usage should contain -f <file>", usage.contains("-f <file>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintOptionWithEmptyArgNameHelp() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option option = new Option("f", "file", true, "file option");
        option.setArgName("");
        options.addOption(option);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertFalse("Empty argName should not produce [] in help", help.contains("[]"));
        assertTrue("Help should contain -f", help.contains("-f"));
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, null, null, new Options(), 1, 3, null, false);
    }
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "", null, new Options(), 1, 3, null, false);
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpNullWriter() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp(null, 80, "app", null, new Options(), 1, 3, null, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageNullWriter() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printUsage(null, 80, "app", new Options());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        formatter.printHelp(pw, 80, "app", "Header", options, 1, 3, "Footer", false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain header", help.contains("Header"));
        assertTrue("Should contain footer", help.contains("Footer"));
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain option", help.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithBlankHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        formatter.printHelp(pw, 80, "app", "   ", options, 1, 3, "   ", false);
        pw.flush();
        
        String help = sw.toString();
        assertFalse("Blank header should not appear", help.contains("   "));
        assertFalse("Blank footer should not appear", help.contains("   "));
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testOptionComparatorSorting() throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", false, "beta option");
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("c", "gamma", false, "gamma option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        
        String help = sw.toString();
        int alphaPos = help.indexOf("alpha");
        int betaPos = help.indexOf("beta");
        int gammaPos = help.indexOf("gamma");
        
        assertTrue("Alpha should come before beta", alphaPos < betaPos);
        assertTrue("Beta should come before gamma", betaPos < gammaPos);
    }
    
    @Test(timeout = 4000)
    public void testOptionComparatorCaseInsensitive() throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("B", "Beta", false, "beta option");
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        
        String help = sw.toString();
        int alphaPos = help.indexOf("alpha");
        int betaPos = help.indexOf("beta");
        
        assertTrue("Alpha should come before beta (case-insensitive)", alphaPos < betaPos);
    }
    
    @Test(timeout = 4000)
    public void testOptionGroupRequired() throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Required group should be in brackets", usage.contains("[-a | -b]"));
    }
    
    @Test(timeout = 4000)
    public void testOptionGroupNotRequired() throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Non-required group should be in parentheses", usage.contains("[-a | -b]"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain auto usage", help.contains("usage: app [-a <arg>] [-b]"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOnlyOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "long", true, "long option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain --long", usage.contains("--long"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNoArgOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithArgOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a <arg>", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithCustomArgName() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("file");
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a <file>", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintOptionsWithNullDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, null);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain option", help.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintOptionsWithLongDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, 
            "This is a very long description that should wrap at some point in the output");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 40, options, 1, 3);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain description", help.contains("This is a very long"));
        assertTrue("Should wrap text", help.contains(formatter.getNewLine()));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithAllFeatures() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "Header", options, 2, 5, "Footer", true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain header", help.contains("Header"));
        assertTrue("Should contain footer", help.contains("Footer"));
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, null, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithOptionGroupInOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        options.addOption("c", "gamma", false, "gamma option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
        assertTrue("Should contain gamma", help.contains("gamma"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithMultipleOptionGroups() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", "alpha", false, "alpha option"));
        group1.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("c", "gamma", false, "gamma option"));
        group2.addOption(new Option("d", "delta", false, "delta option"));
        options.addOptionGroup(group2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain first group", usage.contains("[-a | -b]"));
        assertTrue("Should contain second group", usage.contains("[-c | -d]"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionAndGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "xray", false, "xray option");
        
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain xray", usage.contains("-x"));
        assertTrue("Should contain group", usage.contains("[-a | -b]"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a <arg>", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a | -b", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionAndArg() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "file option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -f <arg>", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionNoArg() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -v", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithShortOptionOnly() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", null, false, "a option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionOnly() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "long", false, "long option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app --long", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithBothOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithArgNameNull() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithArgNameEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        // This is the defect case - should be "usage: app -a" not "usage: app -a []"
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithArgNameWhitespace() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg name", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsageAndEmptyArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertFalse("Should not contain []", help.contains("[]"));
        assertTrue("Should contain -f", help.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsageAndNullArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertFalse("Should not contain []", help.contains("[]"));
        assertTrue("Should contain -f", help.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsageAndArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -f <file>", help.contains("-f <file>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithLongOptionAndArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain --file", help.contains("--file"));
        assertTrue("Should contain -f", help.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithLongOptionOnlyAndArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option(null, "file", true, "file option");
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain --file", help.contains("--file"));
        assertFalse("Should not contain -f", help.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithShortOptionOnlyAndArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", null, true, "file option");
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -f", help.contains("-f"));
        assertFalse("Should not contain --file", help.contains("--file"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNoArgOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -a", help.contains("-a"));
        assertFalse("Should not contain <arg>", help.contains("<arg>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithArgOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -a", help.contains("-a"));
        assertTrue("Should contain <arg>", help.contains("<arg>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithCustomArgName() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("file");
        Options options = new Options();
        options.addOption("a", "alpha", true, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain <file>", help.contains("<file>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, null);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -a", help.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -a", help.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithWhitespaceDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "   ");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain -a", help.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithLongDescription() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, 
            "This is a very long description that should wrap at some point in the output");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 40, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain description", help.contains("This is a very long"));
        assertTrue("Should wrap text", help.contains(formatter.getNewLine()));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithMultipleOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        options.addOption("c", "gamma", false, "gamma option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
        assertTrue("Should contain gamma", help.contains("gamma"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithRequiredOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithOptionAndGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "xray", false, "xray option");
        
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain xray", help.contains("xray"));
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithMultipleGroups() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", "alpha", false, "alpha option"));
        group1.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("c", "gamma", false, "gamma option"));
        group2.addOption(new Option("d", "delta", false, "delta option"));
        options.addOptionGroup(group2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain alpha", help.contains("alpha"));
        assertTrue("Should contain beta", help.contains("beta"));
        assertTrue("Should contain gamma", help.contains("gamma"));
        assertTrue("Should contain delta", help.contains("delta"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain alpha", help.contains("alpha"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "", options, 1, 3, "", false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain alpha", help.contains("alpha"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithWhitespaceHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "   ", options, 1, 3, "   ", false);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain alpha", help.contains("alpha"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderFooterAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "Header", options, 1, 3, "Footer", true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain header", help.contains("Header"));
        assertTrue("Should contain footer", help.contains("Footer"));
        assertTrue("Should contain usage", help.contains("usage: app"));
        assertTrue("Should contain alpha", help.contains("alpha"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullOptionsAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, null, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyOptionsAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null, true);
        pw.flush();
        
        String help = sw.toString();
        assertTrue("Should contain usage", help.contains("usage: app"));
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullCmdLineSyntaxAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp(new PrintWriter(new StringWriter()), 80, null, null, new Options(), 1, 3, null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithEmptyCmdLineSyntaxAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp(new PrintWriter(new StringWriter()), 80, "", null, new Options(), 1, 3, null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintHelpWithNullWriterAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp(null, 80, "app", null, new Options(), 1, 3, null, true);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullWriterAndAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printUsage(null, 80, "app", new Options());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullApp() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, null, new Options());
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain usage", usage.contains("usage:"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyApp() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "", new Options());
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain usage", usage.contains("usage:"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceApp() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "   ", new Options());
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain usage", usage.contains("usage:"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", null);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWidthZero() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 0, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWidthNegative() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, -10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWidthSmall() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 5, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWidthLarge() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 1000, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertEquals("usage: app -a", usage);
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        options.addOption("c", "gamma", false, "gamma option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
        assertTrue("Should contain -b", usage.contains("-b"));
        assertTrue("Should contain -c", usage.contains("-c"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
        assertTrue("Should contain -b", usage.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "xray", false, "xray option");
        
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -x", usage.contains("-x"));
        assertTrue("Should contain -a", usage.contains("-a"));
        assertTrue("Should contain -b", usage.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha option"));
        group.addOption(new Option("b", "beta", false, "beta option"));
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
        assertTrue("Should contain -b", usage.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "file option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -f", usage.contains("-f"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "long", false, "long option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain --long", usage.contains("--long"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithShortOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", null, false, "a option");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain -a", usage.contains("-a"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain <file>", usage.contains("<file>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndOptionAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("xfile");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain xfile", usage.contains("xfile"));
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        opt.setArgName("file");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file", usage.contains("file"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroupAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("filename");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain filename", usage.contains("filename"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionOnlyAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option(null, "long", true, "long option");
        opt.setArgName("lfile");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain lfile", usage.contains("lfile"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithShortOptionOnlyAndArgNameAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", null, true, "a option");
        opt.setArgName("afile");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain afile", usage.contains("afile"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithMultipleOptionsAndArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithOptionGroupAndOptionAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("xfile");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain xfile", usage.contains("xfile"));
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("file1");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithRequiredOptionGroupAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("file1");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("file2");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain file1", usage.contains("file1"));
        assertTrue("Should contain file2", usage.contains("file2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("filename");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("level");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain filename", usage.contains("filename"));
        assertTrue("Should contain level", usage.contains("level"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithLongOptionOnlyAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("lfile1");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("lfile2");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain lfile1", usage.contains("lfile1"));
        assertTrue("Should contain lfile2", usage.contains("lfile2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithShortOptionOnlyAndMultipleArgNamesAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("afile1");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("bfile2");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain afile1", usage.contains("afile1"));
        assertTrue("Should contain bfile2", usage.contains("bfile2"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", "alpha", true, "alpha option");
        opt.setRequired(true);
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("f", "file", true, "file option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option(null, "long", true, "long option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option(null, "long", true, "long option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option(null, "long", true, "long option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", null, true, "a option");
        opt.setArgName(null);
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", null, true, "a option");
        opt.setArgName("");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt = new Option("a", null, true, "a option");
        opt.setArgName("   ");
        options.addOption(opt);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("v", "verbose", true, "verbose option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndLongOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option(null, "long1", true, "long1 option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option(null, "long2", true, "long2 option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndShortOptionOnlyAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", null, true, "a option");
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", null, true, "b option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName(null);
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndOptionGroupAndOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option optX = new Option("x", "xray", true, "xray option");
        optX.setArgName("   ");
        options.addOption(optX);
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setRequired(true);
        opt1.setArgName("   ");
        options.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        options.addOption(opt2);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName(null);
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName(null);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithEmptyArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertFalse("Should not contain <>", usage.contains("<>"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithWhitespaceArgNameAndRequiredOptionGroupAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        
        Option opt1 = new Option("a", "alpha", true, "alpha option");
        opt1.setArgName("   ");
        group.addOption(opt1);
        
        Option opt2 = new Option("b", "beta", true, "beta option");
        opt2.setArgName("   ");
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 10, "app", options);
        pw.flush();
        
        String usage = sw.toString();
        assertTrue("Should contain arg", usage.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testPrintUsageWithNullArgNameAndLongOptionAndMultipleOptionsAndWidthAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOptionAndOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        
        Option opt1 = new Option("f", "file", true, "file option");
        opt1.setArgName(null);
        options.addOption(opt1);
        
        Option opt2 = new Option("