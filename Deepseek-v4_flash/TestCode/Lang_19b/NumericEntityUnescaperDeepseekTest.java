package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringWriter;
import java.io.IOException;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: NumericEntityUnescaper.translate(CharSequence, int, Writer)
 *
 * Branches:
 * 1. Outer if: input.charAt(index) == '&' && index < seqEnd -1 && input.charAt(index+1) == '#'
 *    - True: process entity
 *    - False: return 0
 * 2. Inner if: firstChar == 'x' || firstChar == 'X' -> isHex = true, start++
 *    - True: hex mode
 *    - False: decimal mode
 * 3. While loop: while(input.charAt(end) != ';') { end++; } // NO boundary check on seqEnd (DEFECT)
 *    - Could cause StringIndexOutOfBoundsException if no ';' before end of input
 * 4. try-catch: NumberFormatException -> returns 0
 * 5. if(entityValue > 0xFFFF) -> write supplementary chars
 *    - else -> write single char
 * 6. Return value: 2 + (end - start) + (isHex ? 1 : 0) + 1 (assumes semicolon always present)
 *
 * Defect: Missing end-of-input check in while loop leads to StringIndexOutOfBoundsException
 * for unfinished entities (e.g., "&#65", "&#x41", "&#", "&#x").
 * Also, return value calculation incorrectly assumes semicolon always consumed.
 *
 * Test strategy:
 * - Partition A: Normal functional paths (decimal, hex, supplementary)
 * - Partition B: Boundary values (empty, missing semicolon, just "#", "&#", "&#x")
 * - Partition C: Defect-targeted (unfinished entity, out-of-bounds)
 * - Partition D: Exception paths (invalid number format)
 * - Partition E: Multiple entities, edge index values
 */
public class NumericEntityUnescaperDeepseekTest {

    private final NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
    private final StringWriter writer = new StringWriter();

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testDecimalEntityWithSemicolon() throws IOException {
        int consumed = unescaper.translate("&#65;", 0, writer);
        assertEquals("Should consume 5 chars (&#65;)", 5, consumed);
        assertEquals("Should write 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testHexEntityWithSemicolon() throws IOException {
        int consumed = unescaper.translate("&#x41;", 0, writer);
        assertEquals("Should consume 6 chars (&#x41;)", 6, consumed);
        assertEquals("Should write 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testSupplementaryCodePointDecimal() throws IOException {
        // U+1D306 = 119558 decimal
        int consumed = unescaper.translate("&#119558;", 0, writer);
        assertEquals("Should consume 9 chars", 9, consumed);
        assertEquals("Should write musical symbol '𝌆'", "\uD834\uDF06", writer.toString());
    }

    @Test(timeout = 4000)
    public void testSupplementaryCodePointHex() throws IOException {
        // U+1D306 = 0x1D306
        int consumed = unescaper.translate("&#x1D306;", 0, writer);
        assertEquals("Should consume 9 chars", 9, consumed);
        assertEquals("Should write musical symbol '𝌆'", "\uD834\uDF06", writer.toString());
    }

    @Test(timeout = 4000)
    public void testNotStartWithAmpersand() throws IOException {
        int consumed = unescaper.translate("XYZ", 0, writer);
        assertEquals("Should return 0 for non-entity start", 0, consumed);
        assertTrue("Should write nothing", writer.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEntityAtNonZeroIndex() throws IOException {
        // Input with prefix, translate starting at index 4
        int consumed = unescaper.translate("abc &#65;", 4, writer);
        assertEquals("Should consume 5 chars from index 4", 5, consumed);
        assertEquals("Should write 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleEntitiesInSequence() throws IOException {
        // Process two entities separately manually simulate? Not needed.
        // Just test that method works when called multiple times on same writer.
        unescaper.translate("&#65;", 0, writer);
        writer.flush();
        unescaper.translate("&#66;", 0, writer);
        assertEquals("Should concatenate 'AB'", "AB", writer.toString());
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testZeroLengthInput() throws IOException {
        int consumed = unescaper.translate("", 0, writer);
        assertEquals("Should return 0 on empty input", 0, consumed);
    }

    @Test(timeout = 4000)
    public void testIndexAtEnd() throws IOException {
        int consumed = unescaper.translate("abc", 3, writer);
        assertEquals("Should return 0 when index at end", 0, consumed);
    }

    @Test(timeout = 4000)
    public void testEntityAtLastCharacter() throws IOException {
        int consumed = unescaper.translate("&#65", 2, writer);
        // At index=2, char is '6', not '&', so returns 0
        assertEquals("Should return 0 when not at '&'", 0, consumed);
    }

    @Test(timeout = 4000)
    public void testOnlyAmpersand() throws IOException {
        int consumed = unescaper.translate("&", 0, writer);
        assertEquals("Should return 0 for single '&'", 0, consumed);
    }

    @Test(timeout = 4000)
    public void testAmpersandHashOnly() throws IOException {
        int consumed = unescaper.translate("&#", 0, writer);
        // This will reach charAt(start) with start=2, seqEnd=2 -> StringIndexOutOfBoundsException (BUG)
        // On fixed version should handle gracefully, e.g. return 0
        // We expect test to fail on buggy version; catch exception to make test fail.
        try {
            unescaper.translate("&#", 0, writer);
            // If no exception, assume fixed; we check no output
            assertTrue("Should have no output for invalid entity", writer.toString().isEmpty());
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for "&#"");
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testUnfinishedEntityNoSemicolon_Decimal() throws IOException {
        // Input "&#65" without semicolon
        try {
            unescaper.translate("&#65", 0, writer);
            // If no exception, assume fixed; verify translation occurred (optional semi)
            // According to documentation ';' is optional, so should translate 'A'
            assertEquals("Should have written 'A'", "A", writer.toString());
            // Return value calculation is also buggy: returns 2+(2)+0+1=5 but consumed only 4 chars? (&#65 length 4)
            // Not testing return value here as it's secondary defect.
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for unfinished decimal entity");
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedEntityNoSemicolon_Hex() throws IOException {
        // Input "&#x41" without semicolon
        try {
            unescaper.translate("&#x41", 0, writer);
            assertEquals("Should have written 'A'", "A", writer.toString());
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for unfinished hex entity");
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedEntityJustHashX() throws IOException {
        // Input "&#x" – no digits at all
        try {
            unescaper.translate("&#x", 0, writer);
            // Should return 0 (invalid entity) without exception
            assertTrue("Should have no output", writer.toString().isEmpty());
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for &#x without digits");
        }
    }

    @Test(timeout = 4000)
    public void testOutOfBoundsAtStart() throws IOException {
        // Input "&#" (two chars) leads to start=2 = seqEnd -> immediate charAt(start) out of bounds
        try {
            unescaper.translate("&#", 0, writer);
            assertTrue("Should have no output for invalid entity", writer.toString().isEmpty());
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw StringIndexOutOfBoundsException for &#");
        }
    }

    // Additional: test sequence that ends with incomplete entity in middle of string
    @Test(timeout = 4000)
    public void testStringEndsAfterHashX() throws IOException {
        // Already covered by testUnfinishedEntityJustHashX, but add variant
        try {
            unescaper.translate("&#X", 0, writer);  // X uppercase
            assertTrue("Should have no output", writer.toString().isEmpty());
        } catch (StringIndexOutOfBoundsException e) {
            fail("Should not throw exception for &#X");
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testInvalidNumberFormat_Decimal() throws IOException {
        int consumed = unescaper.translate("&#xyz;", 0, writer);
        assertEquals("Should return 0 on NumberFormatException", 0, consumed);
        assertTrue("Should write nothing", writer.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testInvalidNumberFormat_Hex() throws IOException {
        int consumed = unescaper.translate("&#xGGG;", 0, writer);
        assertEquals("Should return 0 on NumberFormatException", 0, consumed);
        assertTrue("Should write nothing", writer.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyNumberAfterHash() throws IOException {
        // "&#;" – start=2, firstChar=';', while loop: charAt(2) == ';' -> end stays 2,
        // then parseInt("") throws NumberFormatException.
        int consumed = unescaper.translate("&#;", 0, writer);
        assertEquals("Should return 0 for empty number", 0, consumed);
        assertTrue("Should write nothing", writer.toString().isEmpty());
    }

    // ===== Partition E: Object Lifecycle (trivial) =====

    @Test(timeout = 4000)
    public void testMultipleCallsDifferentEntities() throws IOException {
        unescaper.translate("&#65;", 0, writer);
        writer.getBuffer().setLength(0); // clear writer
        unescaper.translate("&#66;", 0, writer);
        assertEquals("Should write 'B'", "B", writer.toString());
    }
}