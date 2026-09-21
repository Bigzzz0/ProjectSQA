package org.apache.commons.lang;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Method Under Test            | Target Branch / Scenario                 | Expected Behavior
 * =====================================================================================================
 * escape(String)               | High Unicode (codePoint > 0xFFFF)        | Single surrogate-aware entity (&#119650;),
 *                              | [DEFECTS4J DEFECT TARGET]                | triggers high unicode split surrogate bug
 * escape(String)               | Known entity mapping (e.g. '&', '<', '"')| Escapes to &amp;, &lt;, &quot;
 * escape(String)               | ASCII chars <= 0x7F without mapping      | Retained unchanged as raw chars
 * escape(String)               | Non-ASCII char > 0x7F without named ent. | Escaped to decimal entity (&#NNN;)
 * escape(Writer, String)       | Null / empty / standard characters       | Writes escaped stream to Writer
 * escape(Writer, String)       | Writer throws IOException                | Propagates IOException as expected
 * unescape(String)             | No '&' character in string               | Returns exact same string instance
 * unescape(String)             | Trailing / lone '&' (no closing ';')     | Retains '&' as literal
 * unescape(String)             | Nested ampersand before ';' (&foo&bar;)  | Leaves first '&' literal, parses next
 * unescape(String)             | Empty entity content (&;)                | Written literally as "&;"
 * unescape(String)             | Lone hash entity (&#;)                   | Written literally as "&#;"
 * unescape(String)             | Decimal numeric entity (&#160;)          | Unescapes to char '\u00A0'
 * unescape(String)             | Hex numeric entity lowercase (&#xa0;)    | Unescapes to char '\u00A0'
 * unescape(String)             | Hex numeric entity uppercase (&#XA0;)    | Unescapes to char '\u00A0'
 * unescape(String)             | Malformed numeric entity (&#xZZ;)        | NumberFormatException caught -> literal
 * unescape(String)             | Overflow numeric entity (> 0xFFFF)       | Set to -1 -> written literally
 * unescape(String)             | Unknown named entity (&unknown;)         | Written literally as "&unknown;"
 * unescape(String)             | Known named entity (&copy;)              | Unescapes to char '\u00A9'
 * unescape(Writer, String)     | Writer throws IOException                | Propagates IOException as expected
 * unescape(Writer, String)     | String without ampersand                 | Directly writes string to Writer
 * -----------------------------------------------------------------------------------------------------
 * EntityMap Implementations:
 * - PrimitiveEntityMap         | Add, lookup name, lookup value (found/-1)| Valid bidirectional map operations
 * - MapIntMap (Hash/Tree)      | add, name, value (found/-1)              | Correct mapping and miss handling
 * - LookupEntityMap            | value < LOOKUP_TABLE_SIZE (256)          | Resolves from fast lookup array
 * - LookupEntityMap            | value >= LOOKUP_TABLE_SIZE               | Falls back to super.name(value)
 * - ArrayEntityMap             | ensureCapacity trigger & search miss     | Dynamic resizing, not found -> -1/null
 * - BinaryEntityMap            | Out-of-order adds, duplicate value add   | Binary search positioning & deduplication
 * =====================================================================================================
 */
public class EntitiesGeminiTest {

    // ---------------------------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J High Unicode Escaping)
    // ---------------------------------------------------------------------------------------------

    /**
     * Targets known defect in Entities.escape: characters outside the Basic Multilingual Plane
     * (codePoint > 0xFFFF) represented as surrogate pairs in Java UTF-16 strings must be escaped
     * into a single numeric character reference &#119650;, rather than two separate surrogate entities
     * &#55348;&#57186;.
     */
    @Test(timeout = 4000)
    public void testEscapeHtmlHighUnicode() {
        int codePoint = 119650; // 0x1D362
        String highUnicode = new String(Character.toChars(codePoint));
        String escaped = Entities.HTML40.escape(highUnicode);
        assertEquals("High unicode must be escaped to a single code point entity reference",
                "&#119650;", escaped);
    }

    // ---------------------------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEscapeBasicXmlEntities() {
        Entities entities = Entities.XML;
        String raw = "Foo & \"Bar\" <Baz> 'Qux'";
        String expected = "Foo &amp; &quot;Bar&quot; &lt;Baz&gt; &apos;Qux&apos;";
        assertEquals(expected, entities.escape(raw));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml32Entities() {
        Entities entities = Entities.HTML32;
        assertEquals("&nbsp;&copy;&reg;", entities.escape("\u00A0\u00A9\u00AE"));
        // HTML 3.2 does not have &apos;
        assertEquals("'", entities.escape("'"));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml40Entities() {
        Entities entities = Entities.HTML40;
        assertEquals("&euro;&fnof;&Alpha;", entities.escape("\u20AC\u0192\u0391"));
    }

    @Test(timeout = 4000)
    public void testEscapeAsciiAndNumericEntities() {
        Entities entities = new Entities();
        entities.addEntity("alpha", 945);

        // ASCII chars without entity mapping (< 0x7F) are unchanged
        // Non-ASCII chars without entity mapping (> 0x7F) are written as &#NNN;
        // Known entities are escaped with &name;
        String input = "A \u03B1 \u03B2 \u00FF";
        String expected = "A &alpha; &#946; &#255;";
        assertEquals(expected, entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeBasicAndNamedEntities() {
        assertEquals("Foo & \"Bar\" <Baz> 'Qux'",
                Entities.XML.unescape("Foo &amp; &quot;Bar&quot; &lt;Baz&gt; &apos;Qux&apos;"));
        assertEquals("\u00A0\u00A9\u00AE", Entities.HTML32.unescape("&nbsp;&copy;&reg;"));
        assertEquals("\u20AC\u0192\u0391", Entities.HTML40.unescape("&euro;&fnof;&Alpha;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalEntities() {
        assertEquals("\u00A0", Entities.HTML40.unescape("&#160;"));
        assertEquals("\u003C\u003E", Entities.HTML40.unescape("&#60;&#62;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntities() {
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xa0;"));
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xA0;"));
        assertEquals("\u003C\u003E", Entities.HTML40.unescape("&#x3c;&#X3E;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNoAmpersandOptimization() {
        String input = "Plain string without any special characters.";
        String result = Entities.HTML40.unescape(input);
        assertSame("Unescape should return exact same instance when no ampersand is found", input, result);
    }

    @Test(timeout = 4000)
    public void testCustomEntitiesAndArrayPopulation() {
        Entities custom = new Entities();
        String[][] customDefs = {
                {"foo", "1001"},
                {"bar", "1002"}
        };
        custom.addEntities(customDefs);

        assertEquals("foo", custom.entityName(1001));
        assertEquals("bar", custom.entityName(1002));
        assertNull(custom.entityName(9999));

        assertEquals(1001, custom.entityValue("foo"));
        assertEquals(1002, custom.entityValue("bar"));
        assertEquals(-1, custom.entityValue("nonexistent"));

        assertEquals("&foo;&bar;", custom.escape("\u03E9\u03EA"));
        assertEquals("\u03E9\u03EA", custom.unescape("&foo;&bar;"));
    }

    // ---------------------------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ---------------------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        assertEquals("", Entities.HTML40.escape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.HTML40.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeMalformedAmpersands() {
        // Lone ampersand at the end of string
        assertEquals("test &", Entities.HTML40.unescape("test &"));

        // Lone ampersand in the middle with no semicolon
        assertEquals("test & something", Entities.HTML40.unescape("test & something"));

        // Semicolon before ampersand
        assertEquals(";test &", Entities.HTML40.unescape(";test &"));

        // Ampersand followed by another ampersand before semicolon
        assertEquals("& &amp;", Entities.HTML40.unescape("& &amp;"));
        assertEquals("&&amp;", Entities.HTML40.unescape("&&amp;"));
        assertEquals("&foo&amp;", Entities.HTML40.unescape("&foo&amp;"));

        // Empty entity content
        assertEquals("&;", Entities.HTML40.unescape("&;"));

        // Empty numeric entity content
        assertEquals("&#;", Entities.HTML40.unescape("&#;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownOrInvalidEntities() {
        // Unknown named entity
        assertEquals("&nonexistent;", Entities.HTML40.unescape("&nonexistent;"));

        // Malformed numeric content (NumberFormatException paths)
        assertEquals("&#xZZ;", Entities.HTML40.unescape("&#xZZ;"));
        assertEquals("&#XZZ;", Entities.HTML40.unescape("&#XZZ;"));
        assertEquals("&#abc;", Entities.HTML40.unescape("&#abc;"));

        // Value exceeding 0xFFFF (Character.MAX_VALUE)
        assertEquals("&#65536;", Entities.HTML40.unescape("&#65536;"));
        assertEquals("&#x10000;", Entities.HTML40.unescape("&#x10000;"));
    }

    // ---------------------------------------------------------------------------------------------
    // Partition D: Writer Methods & Defensive Guard Paths
    // ---------------------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEscapeToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.escape(writer, "A & B");
        assertEquals("A &amp; B", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeToWriter() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.unescape(writer, "A &amp; B");
        assertEquals("A & B", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeToWriterWithoutAmpersand() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.unescape(writer, "Pure ASCII text");
        assertEquals("Pure ASCII text", writer.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeWriterThrowsException() {
        Writer failingWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated write error");
            }
            @Override
            public void flush() throws IOException {
                throw new IOException("Simulated flush error");
            }
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated close error");
            }
            @Override
            public void write(int c) throws IOException {
                throw new IOException("Simulated write int error");
            }
        };

        try {
            Entities.HTML40.escape(failingWriter, "A & B");
            fail("Expected IOException when escaping to a failing writer");
        } catch (IOException expected) {
            assertEquals("Simulated write int error", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUnescapeWriterThrowsException() {
        Writer failingWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated write error");
            }
            @Override
            public void flush() throws IOException {
                throw new IOException("Simulated flush error");
            }
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated close error");
            }
            @Override
            public void write(String str, int off, int len) throws IOException {
                throw new IOException("Simulated write string error");
            }
            @Override
            public void write(int c) throws IOException {
                throw new IOException("Simulated write int error");
            }
        };

        try {
            Entities.HTML40.unescape(failingWriter, "A &amp; B");
            fail("Expected IOException when unescaping to a failing writer");
        } catch (IOException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            Entities.HTML40.unescape(failingWriter, "Plain text");
            fail("Expected IOException when unescaping plain text to a failing writer");
        } catch (IOException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Partition E: Internal EntityMap Implementations Verification
    // ---------------------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("testEntity", 42);

        assertEquals("testEntity", map.name(42));
        assertNull(map.name(999));
        assertEquals(42, map.value("testEntity"));
        assertEquals(-1, map.value("unknown"));
    }

    @Test(timeout = 4000)
    public void testHashEntityMap() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("hashAlpha", 101);

        assertEquals("hashAlpha", map.name(101));
        assertNull(map.name(999));
        assertEquals(101, map.value("hashAlpha"));
        assertEquals(-1, map.value("unknown"));
    }

    @Test(timeout = 4000)
    public void testTreeEntityMap() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("treeAlpha", 202);

        assertEquals("treeAlpha", map.name(202));
        assertNull(map.name(999));
        assertEquals(202, map.value("treeAlpha"));
        assertEquals(-1, map.value("unknown"));
    }

    @Test(timeout = 4000)
    public void testLookupEntityMap() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("low", 65);
        map.add("high", 300);

        // Value < LOOKUP_TABLE_SIZE (256) hits table lookup
        assertEquals("low", map.name(65));
        assertNull(map.name(66));

        // Value >= LOOKUP_TABLE_SIZE (256) falls back to super.name()
        assertEquals("high", map.name(300));
        assertNull(map.name(301));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMap() {
        // Construct with small growBy to force ensureCapacity resize
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
        map.add("item1", 1);
        map.add("item2", 2);
        map.add("item3", 3); // Triggers ensureCapacity

        assertEquals("item1", map.name(1));
        assertEquals("item2", map.name(2));
        assertEquals("item3", map.name(3));
        assertNull(map.name(4));

        assertEquals(1, map.value("item1"));
        assertEquals(2, map.value("item2"));
        assertEquals(3, map.value("item3"));
        assertEquals(-1, map.value("item4"));

        // Default constructor
        Entities.ArrayEntityMap defaultMap = new Entities.ArrayEntityMap();
        defaultMap.add("first", 10);
        assertEquals("first", defaultMap.name(10));
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMap() {
        // Test with growBy = 2
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap(2);

        // Add out of order to test binary search insertion
        map.add("thirty", 30);
        map.add("ten", 10);
        map.add("twenty", 20); // Resizes array

        assertEquals("ten", map.name(10));
        assertEquals("twenty", map.name(20));
        assertEquals("thirty", map.name(30));
        assertNull(map.name(25));
        assertNull(map.name(5));
        assertNull(map.name(35));

        // Attempt duplicate value insertion (insertAt > 0 branch)
        map.add("twentyDup", 20);
        assertEquals("Duplicate value should not overwrite original", "twenty", map.name(20));

        // Inherited value lookup
        assertEquals(10, map.value("ten"));
        assertEquals(20, map.value("twenty"));
        assertEquals(-1, map.value("missing"));

        // Default constructor
        Entities.BinaryEntityMap defaultMap = new Entities.BinaryEntityMap();
        defaultMap.add("a", 1);
        assertEquals("a", defaultMap.name(1));
    }

    @Test(timeout = 4000)
    public void testFillWithHtml40EntitiesStaticHelper() {
        Entities custom = new Entities();
        Entities.fillWithHtml40Entities(custom);

        // Checks entries from BASIC, ISO8859_1, and HTML40 arrays
        assertEquals("quot", custom.entityName(34));
        assertEquals("copy", custom.entityName(169));
        assertEquals("euro", custom.entityName(8364));
    }
}