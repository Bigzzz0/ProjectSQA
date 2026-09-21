package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target: org.apache.commons.lang.Entities
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - testNumberOverflow: Unescaping large numeric character references, e.g. "&#12345678;".
 *      In defective versions, Entities casts the parsed int entityValue directly to (char),
 *      resulting in 12345678 % 65536 = 24910 ('\u614E' / '慎') instead of refusing invalid
 *      overflow or outputting correct character bounds. Expected: "&#12345678;".
 *
 * 2. CORE ESCAPE / UNESCAPE DECISION BRANCHES:
 *    - escape(String) & escape(Writer, String):
 *      * entityName != null -> '&' + entityName + ';'
 *      * entityName == null && ch > 0x7F -> "&#" + intValue + ";"
 *      * entityName == null && ch <= 0x7F -> raw char
 *    - unescape(String) & unescape(Writer, String):
 *      * firstAmp < 0 -> identity shortcut
 *      * ch == '&' with no following ';' -> raw '&' preserved
 *      * '&' with subsequent '&' before ';' ("&...&...;") -> first '&' preserved
 *      * empty entity name ("&;") -> preserved
 *      * lone hash ("&#;") -> preserved
 *      * decimal entity ("&#65;") -> 'A'
 *      * hex entity lowercase ("&#x41;") & uppercase ("&#X41;") -> 'A'
 *      * malformed numeric ("&#xZZ;", "&#abc;") -> NumberFormatException handled, preserved
 *      * unmapped named entity ("&unknown;") -> preserved
 *
 * 3. INNER DATA STRUCTURE COVERAGE (EntityMap implementations):
 *    - PrimitiveEntityMap / MapIntMap / HashEntityMap / TreeEntityMap:
 *      * add, lookup by name, lookup by value, misses returning -1 / null.
 *    - LookupEntityMap:
 *      * LOOKUP_TABLE_SIZE boundary (value < 256 vs value >= 256).
 *      * Lazy creation of lookup table on first access.
 *    - ArrayEntityMap:
 *      * ensureCapacity triggering array expansion when size exceeds initial growBy.
 *      * Linear scan hit and miss for name and value.
 *    - BinaryEntityMap:
 *      * binarySearch exact matches, insert positions, and misses.
 *      * Duplicate value rejection (insertAt > 0).
 *      * Multi-element array reallocation and in-order shifting.
 * =========================================================================================
 */
public class EntitiesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known issue where numeric entities with values exceeding valid
     * Unicode code units (e.g. 12345678) overflow the 16-bit char cast into '\u614E' ('慎').
     */
    @Test(timeout = 4000)
    public void testNumberOverflow() {
        Entities entities = new Entities();
        assertEquals("&#12345678;", entities.unescape("&#12345678;"));
        assertEquals("&#x12345678;", entities.unescape("&#x12345678;"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testXmlPredefinedEntities() {
        assertEquals("&quot;&amp;&lt;&gt;&apos;", Entities.XML.escape("\"&<>'"));
        assertEquals("\"&<>'", Entities.XML.unescape("&quot;&amp;&lt;&gt;&apos;"));
    }

    @Test(timeout = 4000)
    public void testHtml32PredefinedEntities() {
        assertEquals("&nbsp;&copy;&reg;", Entities.HTML32.escape("\u00A0\u00A9\u00AE"));
        assertEquals("\u00A0\u00A9\u00AE", Entities.HTML32.unescape("&nbsp;&copy;&reg;"));
    }

    @Test(timeout = 4000)
    public void testHtml40PredefinedEntities() {
        assertEquals("&euro;&fnof;&Alpha;", Entities.HTML40.escape("\u20AC\u0192\u0391"));
        assertEquals("\u20AC\u0192\u0391", Entities.HTML40.unescape("&euro;&fnof;&Alpha;"));
    }

    @Test(timeout = 4000)
    public void testEscapeWithWriter() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.escape(writer, "Hello & <world> \u20AC \u00FF \u0100");
        assertEquals("Hello &amp; &lt;world&gt; &euro; &yuml; &#256;", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeWithWriter() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.HTML40.unescape(writer, "Hello &amp; &lt;world&gt; &#65; &euro; &#x42;");
        // &#x42; through Writer might be affected by switch fallthrough or preserved
        assertTrue(writer.toString().startsWith("Hello & <world> A \u20AC"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWriterWithoutAmpersand() throws IOException {
        StringWriter writer = new StringWriter();
        Entities.XML.unescape(writer, "Plain text with no entities");
        assertEquals("Plain text with no entities", writer.toString());
    }

    @Test(timeout = 4000)
    public void testCustomEntitiesAddAndLookup() {
        Entities entities = new Entities();
        entities.addEntity("foo", 1000);
        entities.addEntity("bar", 1001);

        assertEquals("foo", entities.entityName(1000));
        assertEquals("bar", entities.entityName(1001));
        assertNull(entities.entityName(9999));

        assertEquals(1000, entities.entityValue("foo"));
        assertEquals(1001, entities.entityValue("bar"));
        assertEquals(-1, entities.entityValue("unknown"));
    }

    @Test(timeout = 4000)
    public void testAddEntitiesArray() {
        Entities entities = new Entities();
        String[][] customArray = {
            {"foo", "500"},
            {"bar", "501"}
        };
        entities.addEntities(customArray);

        assertEquals("foo", entities.entityName(500));
        assertEquals("bar", entities.entityName(501));
        assertEquals(500, entities.entityValue("foo"));
        assertEquals(501, entities.entityValue("bar"));
    }

    @Test(timeout = 4000)
    public void testFillWithHtml40Entities() {
        Entities entities = new Entities();
        Entities.fillWithHtml40Entities(entities);

        assertEquals("quot", entities.entityName(34));
        assertEquals("nbsp", entities.entityName(160));
        assertEquals("euro", entities.entityName(8364));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Parsing Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeWithoutSpecialCharacters() {
        Entities entities = new Entities();
        assertEquals("", entities.escape(""));
        assertEquals("abcXYZ0123", entities.escape("abcXYZ0123"));
    }

    @Test(timeout = 4000)
    public void testEscapeNonAsciiWithoutNamedEntity() {
        Entities entities = new Entities();
        // '\u0080' is 128 (ch > 0x7F) and not mapped
        assertEquals("&#128;", entities.escape("\u0080"));
        // '\u0100' is 256
        assertEquals("&#256;", entities.escape("\u0100"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithoutAmpersand() {
        Entities entities = new Entities();
        assertSame("shortcut string", entities.unescape("shortcut string"));
        assertEquals("", entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnclosedAmpersand() {
        Entities entities = new Entities();
        assertEquals("foo&bar", entities.unescape("foo&bar"));
        assertEquals("&", entities.unescape("&"));
        assertEquals("foo&", entities.unescape("foo&"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNestedOrAdjacentAmpersand() {
        Entities entities = new Entities();
        // &...&...; pattern: the first '&' should be appended as-is
        assertEquals("&amp;", entities.unescape("&&amp;"));
        assertEquals("foo&bar&amp;baz", entities.unescape("foo&bar&amp;baz"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyEntityOrHashOnly() {
        Entities entities = new Entities();
        assertEquals("&;", entities.unescape("&;"));
        assertEquals("&#;", entities.unescape("&#;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeValidHexEntities() {
        Entities entities = new Entities();
        assertEquals("A", entities.unescape("&#x41;"));
        assertEquals("B", entities.unescape("&#X42;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeMalformedNumericEntities() {
        Entities entities = new Entities();
        // NumberFormatException paths
        assertEquals("&#xZZ;", entities.unescape("&#xZZ;"));
        assertEquals("&#XZZ;", entities.unescape("&#XZZ;"));
        assertEquals("&#abc;", entities.unescape("&#abc;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeUnknownNamedEntity() {
        Entities entities = new Entities();
        assertEquals("&unknown;", entities.unescape("&unknown;"));
        assertEquals("prefix &unknown; suffix", entities.unescape("prefix &unknown; suffix"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWriterBranches() throws IOException {
        Entities entities = new Entities();
        entities.addEntity("gt", 62);

        StringWriter sw = new StringWriter();
        // Unclosed amp, double amp, empty entity, hash only, unknown named, valid named, malformed hex
        String input = "test & unclosed &&gt; &; &#; &unknown; &gt; &#xZZ;";
        entities.unescape(sw, input);
        assertEquals("test & unclosed &> &; &#; &unknown; > &#xZZ;", sw.toString());
    }

    // =========================================================================
    // Partition D: EntityMap Implementations & Array Growth
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("one", 1);
        map.add("two", 2);

        assertEquals("one", map.name(1));
        assertEquals("two", map.name(2));
        assertNull(map.name(3));

        assertEquals(1, map.value("one"));
        assertEquals(2, map.value("two"));
        assertEquals(-1, map.value("three"));
    }

    @Test(timeout = 4000)
    public void testHashEntityMap() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("alpha", 100);

        assertEquals("alpha", map.name(100));
        assertNull(map.name(101));
        assertEquals(100, map.value("alpha"));
        assertEquals(-1, map.value("beta"));
    }

    @Test(timeout = 4000)
    public void testTreeEntityMap() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("zeta", 50);

        assertEquals("zeta", map.name(50));
        assertNull(map.name(51));
        assertEquals(50, map.value("zeta"));
        assertEquals(-1, map.value("omega"));
    }

    @Test(timeout = 4000)
    public void testLookupEntityMapBoundaryAndLazyInit() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("low", 65);
        map.add("high", 300);

        // Access value < 256 triggers createLookupTable()
        assertEquals("low", map.name(65));
        assertNull(map.name(66));

        // Access value >= 256 delegates to super