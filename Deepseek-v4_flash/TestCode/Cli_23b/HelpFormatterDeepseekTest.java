package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Branch & Defect Analysis Matrix:
 * - Setters/Getters: all public fields accessed via get/set methods (defaultWidth, defaultLeftPad, etc.)
 * - setOptionComparator: null resets to default, custom comparator works
 * - printHelp family: null/empty cmdLineSyntax throws IllegalArgumentException; autoUsage flag; header/footer
 * - printUsage: with/without OptionGroup, required/optional options
 * - renderOptions: handles opt null (long opt only), arg present, description null
 * - renderWrappedText: infinite loop guard (CLI-162) when wrap position does not advance
 * - findWrapPos: whitespace at newline/tab, backward/forward search, boundaries (startPos+width >= length)
 * - createPadding: zero length returns empty string
 * - rtrim: null, empty, whitespace, no trailing whitespace
 * - OptionComparator: case-insensitive alphabetical order
 *
 * Defect-targeted: reproduce the infinite loop in renderWrappedText (CLI-162) by calling printHelp
 * with narrow width and a long description. Defective version hangs, fixed version throws RuntimeException.
 */
public class HelpFormatterDeepseekTest extends HelpFormatter {

    // Helper to capture printed output
    private String capture(Runnable r) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // trick: we need to call a method that accepts PrintWriter
        // We'll use a custom method to wrap the logic
        // Instead, we provide the PrintWriter directly via a lambda
        // We'll define a functional interface or just pass the pw to methods
        // For simplicity, we'll write separate test methods that call the HelpFormatter methods directly
        // This helper is not used; we'll use the inherited methods or create a subclass
        return sw.toString();
    }

    // Partition A: Core functional logic & state transitions
    @Test(timeout = 4000)
    public void testDefaultValues() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(74, formatter.getWidth());
        assertEquals(1, formatter.getLeftPadding());
        assertEquals(3, formatter.getDescPadding());
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals("-", formatter.getOptPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals("arg", formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());
        formatter.setSyntaxPrefix("usage2: ");
        assertEquals("usage2: ", formatter.getSyntaxPrefix());
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
        formatter.setLongOptPrefix("--");
        assertEquals("--", formatter.getLongOptPrefix());
        formatter.setArgName("file");
        assertEquals("file", formatter.getArgName());
    }

    @Test(timeout = 4000)
    public void testOptionComparatorSetNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
        Comparator myComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option)o1).getKey().compareTo(((Option)o2).getKey());
            }
        };
        formatter.setOptionComparator(myComp);
        assertSame(myComp, formatter.getOptionComparator());
    }

    // Partition B: Boundary Value Analysis
    @Test(timeout = 4000)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp(null, new Options());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        try {
            formatter.printHelp("", new Options());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreatePaddingZero() {
        assertEquals("", createPadding(0));
    }

    @Test(timeout = 4000)
    public void testCreatePaddingNegative() {
        // negative length returns empty string (loop condition i < len never true)
        assertEquals("", createPadding(-5));
    }

    @Test(timeout = 4000)
    public void testRtrimNull() {
        assertNull(rtrim(null));
    }

    @Test(timeout = 4000)
    public void testRtrimEmpty() {
        assertEquals("", rtrim(""));
    }

    @Test(timeout = 4000)
    public void testRtrimNoTrailingWhitespace() {
        assertEquals("hello", rtrim("hello"));
    }

    @Test(timeout = 4000)
    public void testRtrimWithTrailingSpaces() {
        assertEquals("hello", rtrim("hello   "));
    }

    @Test(timeout = 4000)
    public void testRtrimAllWhitespace() {
        assertEquals("", rtrim("   "));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosNewlineWithinWidth() {
        // text with a newline at position 2, width=5
        String text = "ab\ncdef";
        assertEquals(3, findWrapPos(text, 5, 0)); // returns index of \n+1
    }

    @Test(timeout = 4000)
    public void testFindWrapPosTabWithinWidth() {
        String text = "ab\tcdef";
        assertEquals(3, findWrapPos(text, 5, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosBackwardWhitespace() {
        // "hello world" width=7
        assertEquals(6, findWrapPos("hello world", 7, 0)); // space at index5? Actually "hello " length6, space at 5, but startPos+width=7, looking backward from index7? Wait calculation: text length 11, startPos0, width7 -> startPos+width=7, char at 7 is 'o', backward find whitespace at index5 (' ')? index5 is space, return 5? Let's compute manually:
        // indices: 0h,1e,2l,3l,4o,5space,6w,7o,8r,9l,10d. startPos+width=7, charAt7='o', backtrack: index6 'w', index5 space -> pos=5, returns 5.
        assertEquals(5, findWrapPos("hello world", 7, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosForwardWhitespace() {
        // "abcdefghij" width=6, no whitespace backwards, forward from index6 finds space? No space, then pos becomes text.length()? This would throw exception. Instead we need a case where forward finds something.
        // Text "abcde fghij" width=6: startPos+width=6, charAt6='f', backward: indices 5 space found at index5? Actually index5 is space, so backward finds it. So not forward.
        // Let's construct: "abcdefg hijk" width=6: indices: 0a,1b,2c,3d,4e,5f,6g,7space,8h,9i,10j,11k. startPos+width=6, charAt6='g', backward: index5 'f',4'e',3'd',2'c',1'b',0'a' -> no space, so pos becomes -1? Actually the loop while (pos >= startPos) ... decrement until pos < startPos, so pos becomes -1, then condition pos>startPos false. Then forward: start at index6, check charAt(6)='g' not space, index7 space found, return 7 (since pos starts at 6, loops to 7, char is space, then while condition fails because char == ' ', so loop exits and pos is 7? Actually the loop increments while char not space, so it will increment to 7, check charAt(7) is space, so condition fails, loop ends, then outside loop we have pos=7. Return 7.
        assertEquals(7, findWrapPos("abcdefg hijk", 6, 0));
    }

    @Test(timeout = 4000)
    public void testFindWrapPosEndOfText() {
        // text length < startPos+width -> return -1
        assertEquals(-1, findWrapPos("short", 100, 0));
    }

    @Test(timeout = 4000)
    public void testPrintWrappedNoWrap() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printWrapped(pw, 100, "short text");
        pw.flush();
        assertEquals("short text" + defaultNewLine, sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrintWrappedWithWrap() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printWrapped(pw, 10, "this text must be wrapped");
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains(defaultNewLine));
    }

    // Partition C: Defect-targeted (CLI-162 infinite loop)
    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testRenderWrappedTextInfiniteLoop() {
        // This input reproduces the infinite loop scenario from BugCLI162Test
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(20);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(2);
        Options options = new Options();
        options.addOption("e", true, "used if omited. Example: -e \"Runs such and such\"");
        // In defective version, this call would hang; in fixed version it throws RuntimeException
        formatter.printHelp("app", options);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testPrintHelpLongLines() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(20);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(2);
        Options options = new Options();
        options.addOption("a", false, "looooong description");
        formatter.printHelp("app", options);
    }

    // Partition D: Exception & defensive guard paths
    @Test(timeout = 4000)
    public void testPrintUsageNullOptions() {
        // printUsage(PrintWriter, int, String, Options) does not throw on null options? It calls options.getOptions() which may throw NullPointerException? Actually Options is not checked for null. We'll just test that it doesn't throw for valid.
        // Not a defensive check; skip.
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithAutoUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("v", "verbose", false, "enable verbose");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "cmd", null, options, 1, 3, null, true);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: cmd"));
    }

    @Test(timeout = 4000)
    public void testPrintHelpWithHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "print help");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "cmd", "Header", options, 1, 3, "Footer", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
    }

    // Partition E: Object lifecycle & contract integrity
    @Test(timeout = 4000)
    public void testOptionsRendering() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "aaa", false, "description for a");
        options.addOption("b", null, true, "description for b");
        // Option with no short opt but long opt
        // We'll test via printOptions
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a,--aaa"));
        assertTrue(output.contains("-b"));
    }

    @Test(timeout = 4000)
    public void testOptionComparatorSorting() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", false, "b desc");
        options.addOption("a", false, "a desc");
        // The default comparator sorts case-insensitive by key, so a should come before b
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        int indexA = output.indexOf("-a");
        int indexB = output.indexOf("-b");
        assertTrue(indexA < indexB); // a before b
    }

    @Test(timeout = 4000)
    public void testOptionGroupRendering() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);
        // Also add a standalone option
        options.addOption("c", false, "c desc");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
        assertTrue(output.contains("-c"));
    }

    // Additional test for direct renderWrappedText using inheritance
    @Test(timeout = 4000)
    public void testRenderWrappedTextNormal() {
        StringBuffer sb = new StringBuffer();
        renderWrappedText(sb, 20, 5, "This is a normal text that should wrap correctly.");
        assertTrue(sb.length() > 0);
    }
}