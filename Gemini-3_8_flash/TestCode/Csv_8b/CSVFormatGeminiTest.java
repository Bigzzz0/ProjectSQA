/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Branch / Condition Coverage Target:
 * 1. isLineBreak(char / Character):
 *    - char == LF, char == CR, char is normal char.
 *    - Character == null (false), Character is LF/CR (true), Character is normal (false).
 * 2. Constructor & with* validations for line break:
 *    - newFormat('\n'), newFormat('\r'), newFormat(',')
 *    - withDelimiter('\n'), withDelimiter('\r')
 *    - withCommentStart('\n'), withCommentStart('\r'), withCommentStart((Character) null)
 *    - withEscape('\n'), withEscape('\r'), withEscape((Character) null)
 *    - withQuoteChar('\n'), withQuoteChar('\r'), withQuoteChar((Character) null)
 * 3. validate() Decision Points:
 *    - quoteChar != null && delimiter == quoteChar.charValue() -> IllegalStateException
 *    - escape != null && delimiter == escape.charValue() -> IllegalStateException
 *    - commentStart != null && delimiter == commentStart.charValue() -> IllegalStateException
 *    - quoteChar != null && quoteChar.equals(commentStart) -> IllegalStateException
 *    - escape != null && escape.equals(commentStart) -> IllegalStateException
 *    - escape == null && quotePolicy == Quote.NONE -> IllegalStateException
 *    - header != null with duplicates -> IllegalArgumentException (Defects4J defect: throws IllegalStateException)
 *    - header != null without duplicates -> Valid
 *    - header == null -> Valid
 * 4. equals(Object) / hashCode() Full Combinatorial Branching:
 *    - this == obj, obj == null, getClass() != obj.getClass()
 *    - delimiter equality / inequality
 *    - quotePolicy: equal, non-equal
 *    - quoteChar: (null, null), (null, non-null), (non-null, null), (non-null, equal), (non-null, non-equal)
 *    - commentStart: (null, null), (null, non-null), (non-null, null), (non-null, equal), (non-null, non-equal)
 *    - escape: (null, null), (null, non-null), (non-null, null), (non-null, equal), (non-null, non-equal)
 *    - nullString: (null, null), (null, non-null), (non-null, null), (non-null, equal), (non-null, non-equal)
 *    - header: null vs null, null vs non-null, non-null vs null, arrays equal, arrays unequal
 *    - boolean flags: ignoreSurroundingSpaces, ignoreEmptyLines, skipHeaderRecord
 *    - recordSeparator: (null, null), (null, non-null), (non-null, null), (non-null, equal), (non-null, non-equal)
 * 5. toString() Branch Coverage:
 *    - isEscaping() [T/F], isQuoting() [T/F], isCommentingEnabled() [T/F], isNullHandling() [T/F]
 *    - recordSeparator != null [T/F]
 *    - getIgnoreEmptyLines() [T/F], getIgnoreSurroundingSpaces() [T/F]
 *    - header != null [T/F]
 * 6. Immutability & Defensive Copying:
 *    - withHeader array modification before/after construction
 *    - getHeader() clone check
 * 7. format() & parse() Integration:
 *    - format with values, null values, nullString substitutions
 *    - parse with Reader
 * ====================================================================================================
 */

package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import org.junit.Test;

public class CSVFormatGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteChar());
        assertNull(CSVFormat.DEFAULT.getCommentStart());
        assertNull(CSVFormat.DEFAULT.getEscape());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
        assertNull(CSVFormat.DEFAULT.getNullString());
        assertNull(CSVFormat.DEFAULT.getHeader());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());

        assertNotNull(CSVFormat.RFC4180);
        assertFalse(CSVFormat.RFC4180.getIgnoreEmptyLines());

        assertNotNull(CSVFormat.EXCEL);
        assertFalse(CSVFormat.EXCEL.getIgnoreEmptyLines());

        assertNotNull(CSVFormat.TDF);
        assertEquals('\t', CSVFormat.TDF.getDelimiter());
        assertTrue(CSVFormat.TDF.getIgnoreSurroundingSpaces());

        assertNotNull(CSVFormat.MYSQL);
        assertEquals('\t', CSVFormat.MYSQL.getDelimiter());
        assertEquals(Character.valueOf('\\'), CSVFormat.MYSQL.getEscape());
        assertFalse(CSVFormat.MYSQL.getIgnoreEmptyLines());
        assertNull(CSVFormat.MYSQL.getQuoteChar());
        assertEquals("\n", CSVFormat.MYSQL.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithMethodsFluentBuilding() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuoteChar('\'')
                .withQuotePolicy(Quote.ALL)
                .withCommentStart('#')
                .withEscape('/')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withHeader("H1", "H2")
                .withSkipHeaderRecord(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteChar());
        assertEquals(Quote.ALL, format.getQuotePolicy());
        assertEquals(Character.valueOf('#'), format.getCommentStart());
        assertEquals(Character.valueOf('/'), format.getEscape());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new String[]{"H1", "H2"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());

        assertTrue(format.isQuoting());
        assertTrue(format.isEscaping());
        assertTrue(format.isCommentingEnabled());
        assertTrue(format.isNullHandling());
    }

    @Test(timeout = 4000)
    public void testNewFormat() {
        final CSVFormat format = CSVFormat.newFormat('|');
        assertEquals('|', format.getDelimiter());
        assertNull(format.getQuoteChar());
        assertNull(format.getQuotePolicy());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.isQuoting());
        assertFalse(format.isEscaping());
        assertFalse(format.isCommentingEnabled());
        assertFalse(format.isNullHandling());
    }

    @Test(timeout = 4000)
    public void testFormatValues() {
        final String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);

        final CSVFormat formatWithNull = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("a,N/A,b", formatWithNull.format("a", null, "b"));
    }

    @Test(timeout = 4000)
    public void testParseReader() throws Exception {
        final String input = "col1,col2\nval1,val2";
        final CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(input));
        assertNotNull(parser);
        assertEquals(CSVFormat.DEFAULT, parser.getFormat());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDisableFeaturesViaNull() {
        CSVFormat format = CSVFormat.DEFAULT
                .withQuoteChar((Character) null)
                .withCommentStart((Character) null)
                .withEscape((Character) null)
                .withNullString(null)
                .withRecordSeparator((String) null)
                .withHeader((String[]) null);

        assertNull(format.getQuoteChar());
        assertFalse(format.isQuoting());
        assertNull(format.getCommentStart());
        assertFalse(format.isCommentingEnabled());
        assertNull(format.getEscape());
        assertFalse(format.isEscaping());
        assertNull(format.getNullString());
        assertFalse(format.isNullHandling());
        assertNull(format.getRecordSeparator());
        assertNull(format.getHeader());
    }

    @Test(timeout = 4000)
    public void testWithRecordSeparatorChar() {
        final CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator('\r');
        assertEquals("\r", format.getRecordSeparator());
    }

    @Test(timeout = 4000)
    public void testWithHeaderEmpty() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
    }

    @Test(timeout = 4000)
    public void testHeaderImmutabilityDefensiveCopy() {
        final String[] original = new String[]{"Alpha", "Beta"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(original);

        original[0] = "Modified";
        assertEquals("Alpha", format.getHeader()[0]);

        final String[] retrieved = format.getHeader();
        retrieved[0] = "Tampered";
        assertEquals("Alpha", format.getHeader()[0]);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Target Defect: CSVFormatTest::testDuplicateHeaderElements
     * Expected: IllegalArgumentException when duplicate header elements are validated
     * Ground Truth: The buggy implementation throws IllegalStateException instead.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicateHeaderElements() {
        final CSV