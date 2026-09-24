package org.mozilla.javascript.tools.idswitch;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.mozilla.javascript.tools.idswitch.CodePrinter
 *
 * 1. Buffer Management & Growth (ensure_area, add_area):
 *    - Branch: end <= buffer.length (no reallocation required)
 *    - Branch: end > buffer.length && end <= buffer.length * 2 (standard geometric doubling)
 *    - Branch: end > buffer.length * 2 (large chunk leap beyond doubled capacity)
 *    - State: offset tracking, buffer expansion preservation via System.arraycopy
 *
 * 2. Character & String Escaping (put_string_literal_char, digit_to_hex_letter):
 *    - Escaped control characters: '\b', '\t', '\n', '\f', '\r'
 *    - Context-dependent quoting:
 *      * Single quote in qchar: backslash_symbol = true -> \'
 *      * Single quote in qstring: backslash_symbol = false -> '
 *      * Double quote in qchar: backslash_symbol = false -> "
 *      * Double quote in qstring: backslash_symbol = true -> \"
 *    - Printable ASCII boundary: [' ' (32), 126] -> verbatim emission
 *    - Unicode Hex formatting: c < 32 or c > 126 -> \uXXXX
 *    - Hex digit conversion: d < 10 ('0'-'9') vs d >= 10 ('A'-'F') across all 4 nibbles
 *
 * 3. Indentation Logic (indent, line):
 *    - indentTabSize <= 0: tab_count = 0, indent_size = visible_size (all spaces)
 *    - indentTabSize > 0:
 *      * visible_size < indentTabSize: tab_count = 0, spaces only
 *      * visible_size % indentTabSize == 0: tabs only, 0 spaces
 *      * visible_size % indentTabSize > 0: mixed tabs and remainder spaces
 *    - indent level = 0: no spaces or tabs added
 *
 * 4. Boundary Values & State Inspection:
 *    - getLastChar(): empty printer (offset == 0) returns -1; non-empty returns buffer[offset-1]
 *    - clear(): resets offset to 0; verifies reset state across getters
 *    - erase(): shifts tail region to left and recalculates offset
 *    - Primitive output (p(int)): Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE
 *
 * 5. Defect Verification:
 *    - testManyAdds: repeated additions validating sustained iterative append, preventing
 *      potential StackOverflowError or allocation regressions on high-frequency writes.
 */
public class CodePrinterGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndGettersSetters() {
        CodePrinter printer = new CodePrinter();
        assertEquals("\n", printer.getLineTerminator());
        assertEquals(4, printer.getIndentStep());
        assertEquals(8, printer.getIndentTabSize());
        assertEquals(0, printer.getOffset());
        assertEquals(-1, printer.getLastChar());
        assertEquals("", printer.toString());

        printer.setLineTerminator("\r\n");
        assertEquals("\r\n", printer.getLineTerminator());

        printer.setIndentStep(2);
        assertEquals(2, printer.getIndentStep());

        printer.setIndentTabSize(4);
        assertEquals(4, printer.getIndentTabSize());
    }

    @Test(timeout = 4000)
    public void testPrintCharAndLastChar() {
        CodePrinter printer = new CodePrinter();
        printer.p('x');
        assertEquals(1, printer.getOffset());
        assertEquals('x', printer.getLastChar());
        assertEquals("x", printer.toString());

        printer.p('y');
        assertEquals(2, printer.getOffset());
        assertEquals('y', printer.getLastChar());
        assertEquals("xy", printer.toString());
    }

    @Test(timeout = 4000)
    public void testPrintStringAndCharArray() {
        CodePrinter printer = new CodePrinter();
        printer.p("Hello");
        assertEquals("Hello", printer.toString());
        assertEquals('o', printer.getLastChar());
        assertEquals(5, printer.getOffset());

        char[] chars = new char[]{' ', 'W', 'o', 'r', 'l', 'd'};
        printer.p(chars);
        assertEquals("Hello World", printer.toString());
        assertEquals(11, printer.getOffset());
    }

    @Test(timeout = 4000)
    public void testPrintCharArraySubslice() {
        CodePrinter printer = new CodePrinter();
        char[] chars = new char[]{'A', 'B', 'C', 'D', 'E'};
        printer.p(chars, 1, 4);
        assertEquals("BCD", printer.toString());
        assertEquals(3, printer.getOffset());
    }

    @Test(timeout = 4000)
    public void testPrintIntValues() {
        CodePrinter printer = new CodePrinter();
        printer.p(0);
        printer.p(42);
        printer.p(-123);
        printer.p(Integer.MAX_VALUE);
        printer.p(Integer.MIN_VALUE);
        String expected = "0" + "42" + "-123" + Integer.MAX_VALUE + Integer.MIN_VALUE;
        assertEquals(expected, printer.toString());
    }

    @Test(timeout = 4000)
    public void testNewlineAndLine() {
        CodePrinter printer = new CodePrinter();
        printer.nl();
        assertEquals("\n", printer.toString());
        assertEquals('\n', printer.getLastChar());

        printer.clear();
        printer.setIndentStep(2);
        printer.setIndentTabSize(4);
        printer.line(1, "int x = 1;");
        assertEquals("  int x = 1;\n", printer.toString());
    }

    // =========================================================================
    // Partition B: Quoting, Character Escapes & Boundary Value Analysis (BVA)
    // =========================================================================

    @Test(timeout = 4000)
    public void testQcharEscapeSequences() {
        CodePrinter printer = new CodePrinter();
        printer.qchar('\b');
        printer.qchar('\t');
        printer.qchar('\n');
        printer.qchar('\f');
        printer.qchar('\r');
        assertEquals("'\\b''\\t''\\n''\\f''\\r'", printer.toString());
    }

    @Test(timeout = 4000)
    public void testQcharQuotesDistinction() {
        CodePrinter printer = new CodePrinter();
        // In qchar, single quote is escaped, double quote is not
        printer.qchar('\'');
        printer.qchar('"');
        assertEquals("'\\'''\"'", printer.toString());
    }

    @Test(timeout = 4000)
    public void testQcharPrintableAscii() {
        CodePrinter printer = new CodePrinter();
        printer.qchar(' '); // Lower bound of printable ASCII
        printer.qchar('~'); // Upper bound of printable ASCII (126)
        printer.qchar('A');
        assertEquals("' ''~''A'", printer.toString());
    }

    @Test(timeout = 4000)
    public void testQcharUnicodeFormatting() {
        CodePrinter printer = new CodePrinter();
        // c < 32 (non-standard escape control char, e.g. 0)
        printer.qchar((char) 0);
        // c > 126 (e.g. 127 DEL, and upper hex digits)
        printer.qchar((char) 127);
        printer.qchar((char) 0x12AB);
        printer.qchar((char) 0xFC09);
        assertEquals("'\\u0000''\\u007F''\\u12AB''\\uFC09'", printer.toString());
    }

    @Test(timeout = 4000)
    public void testQstringEscaping() {
        CodePrinter printer = new CodePrinter();
        // In qstring: double quote is escaped, single quote is NOT escaped
        printer.qstring("a\"b'c\b\t\n\f\r\u0001\u00A5");
        assertEquals("\"a\\\"b'c\\b\\t\\n\\f\\r\\u0001\\u00A5\"", printer.toString());
    }

    @Test(timeout = 4000)
    public void testQstringEmpty() {
        CodePrinter printer = new CodePrinter();
        printer.qstring("");
        assertEquals("\"\"", printer.toString());
        assertEquals('"', printer.getLastChar());
    }

    // =========================================================================
    // Partition C: Indentation Variations & Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testIndentWithZeroOrNegativeTabSize() {
        CodePrinter printer = new CodePrinter();
        printer.setIndentStep(3);
        printer.setIndentTabSize(0);
        printer.indent(2); // 3 * 2 = 6 spaces, 0 tabs
        assertEquals("      ", printer.toString());

        printer.clear();
        printer.setIndentTabSize(-4);
        printer.indent(1); // 3 spaces, 0 tabs
        assertEquals("   ", printer.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithTabsOnly() {
        CodePrinter printer = new CodePrinter();
        printer.setIndentStep(4);
        printer.setIndentTabSize(4);
        printer.indent(2); // 8 positions -> 2 tabs, 0 spaces
        assertEquals("\t\t", printer.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithMixedTabsAndSpaces() {
        CodePrinter printer = new CodePrinter();
        printer.setIndentStep(3);
        printer.setIndentTabSize(8);
        printer.indent(3); // visible_size = 9 -> 1 tab (8) + 1 space (1)
        assertEquals("\t ", printer.toString());
    }

    @Test(timeout = 4000)
    public void testIndentZeroLevel() {
        CodePrinter printer = new CodePrinter();
        printer.indent(0);
        assertEquals(0, printer.getOffset());
        assertEquals("", printer.toString());
    }

    // =========================================================================
    // Partition D: Buffer Growth, Erase & State Mutations
    // =========================================================================

    @Test(timeout = 4000)
    public void testBufferDoublingGrowth() {
        CodePrinter printer = new CodePrinter();
        // Initial capacity is 4096 (1 << 12)
        // Add 3000 chars, then add 2000 chars to trigger doubling (3000 + 2000 > 4096, <= 8192)
        char[] chunk1 = new char[3000];
        for (int i = 0; i < chunk1.length; i++) {
            chunk1[i] = 'a';
        }
        printer.p(chunk1);

        char[] chunk2 = new char[2000];
        for (int i = 0; i < chunk2.length; i++) {
            chunk2[i] = 'b';
        }
        printer.p(chunk2);

        assertEquals(5000, printer.getOffset());
        assertEquals('b', printer.getLastChar());
        assertEquals('a', printer.toString().charAt(0));
        assertEquals('b', printer.toString().charAt(4999));
    }

    @Test(timeout = 4000)
    public void testBufferHugeLeapGrowth() {
        CodePrinter printer = new CodePrinter();
        // Add a chunk exceeding buffer.length * 2 directly (e.g. > 8192)
        char[] hugeChunk = new char[10000];
        for (int i = 0; i < hugeChunk.length; i++) {
            hugeChunk[i] = 'z';
        }
        printer.p(hugeChunk);
        assertEquals(10000, printer.getOffset());
        assertEquals('z', printer.getLastChar());
        assertEquals(10000, printer.toString().length());
    }

    @Test(timeout = 4000)
    public void testEraseMiddle() {
        CodePrinter printer = new CodePrinter();
        printer.p("0123456789");
        printer.erase(3, 7); // Removes "3456" -> leaves "012789"
        assertEquals("012789", printer.toString());
        assertEquals(6, printer.getOffset());
        assertEquals('9', printer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testErasePrefixAndSuffix() {
        CodePrinter printer = new CodePrinter();
        printer.p("abcdef");
        printer.erase(0, 2); // Removes "ab" -> "cdef"
        assertEquals("cdef", printer.toString());
        assertEquals(4, printer.getOffset());

        printer.erase(2, 4); // Removes "ef" -> "cd"
        assertEquals("cd", printer.toString());
        assertEquals(2, printer.getOffset());
        assertEquals('d', printer.getLastChar());

        printer.erase(0, 2); // Removes all
        assertEquals("", printer.toString());
        assertEquals(0, printer.getOffset());
        assertEquals(-1, printer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testClearResetsBufferState() {
        CodePrinter printer = new CodePrinter();
        printer.p("Non-empty content");
        assertEquals(17, printer.getOffset());
        printer.clear();
        assertEquals(0, printer.getOffset());
        assertEquals(-1, printer.getLastChar());
        assertEquals("", printer.toString());

        // Can append again after clear
        printer.p("Reused");
        assertEquals("Reused", printer.toString());
    }

    // =========================================================================
    // Partition E: Defect-Targeted Branch Zone (Many Sequential Additions)
    // =========================================================================

    @Test(timeout = 4000)
    public void testManyAdds() {
        // Targets stress scenarios with deep iterative adds across buffer expansions
        CodePrinter printer = new CodePrinter();
        final int iterations = 15000;
        for (int i = 0; i < iterations; i++) {
            printer.p('+');
        }
        assertEquals(iterations, printer.getOffset());
        assertEquals('+', printer.getLastChar());
        assertEquals(iterations, printer.toString().length());
    }
}