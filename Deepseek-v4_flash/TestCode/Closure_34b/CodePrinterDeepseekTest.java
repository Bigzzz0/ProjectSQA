package org.mozilla.javascript.tools.idswitch;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Target Class: CodePrinter (org.mozilla.javascript.tools.idswitch)
 * 
 * Known Defect: StackOverflowError in com.google.javascript.jscomp.CodePrinterTest::testManyAdds
 * This is a cross-project reference; for this CodePrinter class, the equivalent defect pattern
 * would be a StackOverflowError when processing many string concatenations or nested operations
 * via qstring() or p(String). The root cause is likely unchecked recursion or buffer overflow
 * in put_string_literal_char() or insufficient stack handling for large operations.
 * Dedicated test: testManyAddsStackOverflow() - repeatedly adds string fragments to trigger stack overflow.
 * 
 * Key Branches & Conditions:
 * 1. put_string_literal_char(): switch on c for special chars (\b, \t, \n, \f, \r, \', \")
 *    - backslash_symbol logic: \' when !in_string, \" when in_string
 *    - default branch (no special): check ' ' <= c <= 126 for direct char, else \uXXXX escape
 * 2. indent(): indentTabSize <= 0 vs > 0, tab_count = visible_size / indentTabSize
 * 3. ensure_area(): buffer.length doubling, end > new_capacity handling
 * 4. erase(): System.arraycopy with offset adjustment
 * 5. getLastChar(): offset == 0 ? -1 : buffer[offset-1]
 * 6. clear(): reset offset to 0
 * 7. p(char[] array, int begin, int end): l = end - begin, add_area(l), arraycopy
 * 8. qchar(int c): ensure_area(2 + LITERAL_CHAR_MAX_SIZE), put_string_literal_char, closing quote
 * 9. qstring(String s): ensure_area(2 + LITERAL_CHAR_MAX_SIZE * l), loop put_string_literal_char
 * 10. getLineTerminator(), setLineTerminator(), getIndentStep(), setIndentStep(), getIndentTabSize(), setIndentTabSize()
 * 
 * Partitions:
 * A: Core functional - p(char), p(String), p(char[]), p(int), qchar, qstring, indent, nl, line, clear, getOffset, getLastChar, toString
 * B: Boundary - null/empty strings, negative indent level, zero indentTabSize, negative indentStep, max char values, large strings
 * C: Defect-targeted - many repeated p() calls via loop to trigger StackOverflowError from deep recursion in put_string_literal_char
 * D: Exception - defensive: note that CodePrinter doesn't throw exceptions; test edge cases that might cause internal errors
 * E: Lifecycle - multiple clear(), state consistency after operations
 */

public class CodePrinterDeepseekTest {

    // ===================== Partition A: Core Functional Logic =====================

    @Test(timeout = 4000)
    public void testClearAndInitialState() {
        CodePrinter cp = new CodePrinter();
        cp.p("hello");
        assertEquals("hello", cp.toString());
        assertEquals(5, cp.getOffset());
        cp.clear();
        assertEquals("", cp.toString());
        assertEquals(0, cp.getOffset());
    }

    @Test(timeout = 4000)
    public void testPChar() {
        CodePrinter cp = new CodePrinter();
        cp.p('a');
        assertEquals("a", cp.toString());
        assertEquals(1, cp.getOffset());
        assertEquals('a', cp.getLastChar());
    }

    @Test(timeout = 4000)
    public void testPString() {
        CodePrinter cp = new CodePrinter();
        cp.p("abc");
        assertEquals("abc", cp.toString());
        assertEquals(3, cp.getOffset());
    }

    @Test(timeout = 4000)
    public void testPCharArray() {
        CodePrinter cp = new CodePrinter();
        char[] arr = {'x', 'y', 'z'};
        cp.p(arr);
        assertEquals("xyz", cp.toString());
    }

    @Test(timeout = 4000)
    public void testPCharArrayPartial() {
        CodePrinter cp = new CodePrinter();
        char[] arr = {'a', 'b', 'c', 'd', 'e'};
        cp.p(arr, 1, 4);
        assertEquals("bcd", cp.toString());
    }

    @Test(timeout = 4000)
    public void testPInt() {
        CodePrinter cp = new CodePrinter();
        cp.p(123);
        assertEquals("123", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQcharSimple() {
        CodePrinter cp = new CodePrinter();
        cp.qchar('a');
        assertEquals("'a'", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQcharSpecialChars() {
        CodePrinter cp = new CodePrinter();
        cp.qchar('\n');
        assertEquals("'\\n'", cp.toString());
        cp.clear();
        cp.qchar('\t');
        assertEquals("'\\t'", cp.toString());
        cp.clear();
        cp.qchar('\'');
        assertEquals("'\\''", cp.toString());
        cp.clear();
        cp.qchar('\\');
        assertEquals("'\\\\'", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQcharUnicode() {
        CodePrinter cp = new CodePrinter();
        cp.qchar(0x00E9); // é
        assertEquals("'\\u00E9'", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringSimple() {
        CodePrinter cp = new CodePrinter();
        cp.qstring("hello");
        assertEquals("\"hello\"", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringWithQuotes() {
        CodePrinter cp = new CodePrinter();
        cp.qstring("he\"llo");
        assertEquals("\"he\\\"llo\"", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringWithSpecialChars() {
        CodePrinter cp = new CodePrinter();
        cp.qstring("line1\nline2");
        assertEquals("\"line1\\nline2\"", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringWithUnicode() {
        CodePrinter cp = new CodePrinter();
        cp.qstring("\u00E9\u00E0");
        assertEquals("\"\\u00E9\\u00E0\"", cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithTabs() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.indent(2);
        // visible_size = 4*2 = 8, tab_count = 8/8 = 1, indent_size = 1 + 0 = 1
        assertEquals("\t", cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithSpaces() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.indent(1);
        // visible_size = 4*1 = 4, tab_count = 4/8 = 0, indent_size = 0 + 4 = 4
        assertEquals("    ", cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentMixed() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.indent(3);
        // visible_size = 12, tab_count = 1, indent_size = 1 + 4 = 5 (1 tab + 4 spaces)
        String expected = "\t    ";
        assertEquals(expected, cp.toString());
    }

    @Test(timeout = 4000)
    public void testNl() {
        CodePrinter cp = new CodePrinter();
        cp.nl();
        assertEquals("\n", cp.toString());
    }

    @Test(timeout = 4000)
    public void testLine() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.line(1, "text");
        assertEquals("    text\n", cp.toString());
    }

    @Test(timeout = 4000)
    public void testGetLastCharEmpty() {
        CodePrinter cp = new CodePrinter();
        assertEquals(-1, cp.getLastChar());
    }

    @Test(timeout = 4000)
    public void testGetLastCharAfterP() {
        CodePrinter cp = new CodePrinter();
        cp.p('x');
        assertEquals('x', cp.getLastChar());
    }

    @Test(timeout = 4000)
    public void testErase() {
        CodePrinter cp = new CodePrinter();
        cp.p("abcdef");
        cp.erase(2, 4);
        assertEquals("abef", cp.toString());
        assertEquals(4, cp.getOffset());
    }

    @Test(timeout = 4000)
    public void testEraseFromBeginning() {
        CodePrinter cp = new CodePrinter();
        cp.p("abcde");
        cp.erase(0, 2);
        assertEquals("cde", cp.toString());
    }

    @Test(timeout = 4000)
    public void testEraseToEnd() {
        CodePrinter cp = new CodePrinter();
        cp.p("abcde");
        cp.erase(3, 5);
        assertEquals("abc", cp.toString());
    }

    @Test(timeout = 4000)
    public void testToStringAfterMultipleOps() {
        CodePrinter cp = new CodePrinter();
        cp.p("Hello, ");
        cp.p("World!");
        assertEquals("Hello, World!", cp.toString());
    }

    // ===================== Partition B: Boundary Value Analysis =====================

    @Test(timeout = 4000)
    public void testPEmptyString() {
        CodePrinter cp = new CodePrinter();
        cp.p("");
        assertEquals("", cp.toString());
        assertEquals(0, cp.getOffset());
    }

    @Test(timeout = 4000)
    public void testQcharBoundaryMin() {
        CodePrinter cp = new CodePrinter();
        cp.qchar(0);
        assertEquals("'\\u0000'", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQcharBoundaryMax() {
        CodePrinter cp = new CodePrinter();
        cp.qchar(0xFFFF);
        assertEquals("'\\uFFFF'", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringEmpty() {
        CodePrinter cp = new CodePrinter();
        cp.qstring("");
        assertEquals("\"\"", cp.toString());
    }

    @Test(timeout = 4000)
    public void testQstringVeryLong() {
        CodePrinter cp = new CodePrinter();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        cp.qstring(sb.toString());
        String result = cp.toString();
        assertTrue(result.startsWith("\""));
        assertTrue(result.endsWith("\""));
        assertEquals(2 + 1000, result.length());
    }

    @Test(timeout = 4000)
    public void testIndentNegativeLevel() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.indent(-2);
        // visible_size = 4 * -2 = -8, tab_count = -8/8 = -1, indent_size = -1 + 0 = -1
        // This leads to negative array allocation? Let's test behavior: should not crash, but may produce empty or error.
        // Since add_area uses negative size which could cause issues. We test defensive behavior.
        // Depending on implementation, the result may be unpredictable. We just verify no exception.
        assertNotNull(cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentZeroTabSize() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(0);
        cp.indent(2);
        // visible_size = 8, tab_count = 0 (since indentTabSize <= 0), indent_size = 8
        assertEquals("        ", cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentNegativeTabSize() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(-1);
        cp.indent(2);
        // visible_size = 8, tab_count = 0, indent_size = 8
        assertEquals("        ", cp.toString());
    }

    @Test(timeout = 4000)
    public void testIndentLargeLevel() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(4);
        cp.setIndentTabSize(8);
        cp.indent(100);
        // visible_size = 400, tab_count = 50, indent_size = 50 + 0 = 50
        assertEquals(50, cp.toString().length());
    }

    @Test(timeout = 4000)
    public void testSetIndentStepNegative() {
        CodePrinter cp = new CodePrinter();
        cp.setIndentStep(-5);
        // No immediate effect, but indent will use negative step
        cp.indent(1);
        // visible_size = -5, tab_count = 0 (if tabSize > 0, but negative / positive may be 0)
        // This may produce unexpected behavior; just test it doesn't crash.
        assertNotNull(cp.toString());
    }

    @Test(timeout = 4000)
    public void testBufferExpansion() {
        CodePrinter cp = new CodePrinter();
        // buffer initially 4096 chars, so we add more than that
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append('x');
        }
        cp.p(sb.toString());
        assertEquals(5000, cp.getOffset());
        assertEquals(sb.toString(), cp.toString());
    }

    @Test(timeout = 4000)
    public void testPCharArrayEmpty() {
        CodePrinter cp = new CodePrinter();
        char[] arr = {};
        cp.p(arr);
        assertEquals("", cp.toString());
    }

    @Test(timeout = 4000)
    public void testPCharArrayNull() {
        CodePrinter cp = new CodePrinter();
        try {
            cp.p((char[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected behavior - array.length throws NPE
        }
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    @Test(timeout = 4000)
    public void testManyAddsStackOverflow() {
        // This test targets the known StackOverflowError defect reported in 
        // com.google.javascript.jscomp.CodePrinterTest::testManyAdds.
        // We simulate many repeated p() calls within the same operation to trigger
        // potential stack overflow in put_string_literal_char recursion.
        CodePrinter cp = new CodePrinter();
        StringBuilder sb = new StringBuilder();