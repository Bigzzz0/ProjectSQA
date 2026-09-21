package org.apache.commons.lang;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * The known Defects4J fault is numeric character reference overflow:
 *   unescape("&#12345678;") incorrectly casts the int value to char,
 *   producing U+614E ("慎") instead of preserving the malformed entity.
 * The test suite explicitly asserts the correct overflow behavior in both
 * the String and Writer based unescape paths.
 *
 * Additional branch targets:
 * - unescape(String): no '&', no ';', nested '&' before ';', empty entity,
 *   '#' only, decimal/hex parse, invalid number, negative number, named entity.
 * - unescape(Writer, String): same branches including switch hex path and
 *   NumberFormatException guard.
 * - escape(String) / escape(Writer, String): named-entity mapping, non-ASCII
 *   numeric fallback, plain ASCII passthrough.
 * - EntityMap implementations: PrimitiveEntityMap, MapIntMap (Hash/Tree),
 *   LookupEntityMap lazy table, ArrayEntityMap growth, BinaryEntityMap
 *   sorted insertion/duplicates and binary search hit/miss.
 * - Static entity sets XML, HTML32, HTML40 and fillWithHtml40Entities.
 */
public class EntitiesDeepseekTest {

    @Test(timeout = 4000)
    public void testNumberOverflowString() {
        assertEquals("&#12345678;", Entities.XML.unescape("&#12345678;"));
    }

    @Test(timeout = 4000)
    public void testNumberOverflowHexString() {
        assertEquals("&#x12345678;", Entities.XML.unescape("&#x12345678;"));
    }

    @Test(timeout = 4000)
    public void testNumberOverflowWriter() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&#12345678;");
        assertEquals("&#12345678;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeNoAmpersand() {
        assertEquals("hello", Entities.XML.unescape("hello"));
    }

    @Test(timeout = 4000)
    public void testUnescapeMissingSemicolon() {
        assertEquals("&lt", Entities.XML.unescape("&lt"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNestedAmpersand() {
        assertEquals("&amp&foo;", Entities.XML.unescape("&amp&foo;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyEntity() {
        assertEquals("&;", Entities.XML.unescape("&;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHashOnly() {
        assertEquals("&#;", Entities.XML.unescape("&#;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalNumeric() {
        assertEquals("A", Entities.XML.unescape("&#65;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexNumeric() {
        assertEquals("A", Entities.XML.unescape("&#x41;"));
        assertEquals("A", Entities.XML.unescape("&#X41;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntity() {
        assertEquals("<", Entities.XML.unescape("&lt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeMixedText() {
        assertEquals("a<b&c", Entities.XML.unescape("a&lt;b&#38;c"));
    }

    @Test(timeout = 4000)
    public void testUnescapeInvalidNumber() {
        assertEquals("&#12a34;", Entities.XML.unescape("&#12a34;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNegativeNumber() {
        assertEquals("&#-1;", Entities.XML.unescape("&#-1;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeTooLargeNumber() {
        assertEquals("&#99999999999999999999;",
                Entities.XML.unescape("&#99999999999999999999;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeInvalidHex() {
        assertEquals("&#xZZ;", Entities.XML.unescape("&#xZZ;"));
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeNoAmpersand() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "hello");
        assertEquals("hello", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeNamedAndDecimal() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&lt;&#65;&amp;");
        assertEquals("<A&", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeMissingSemicolon() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&lt");
        assertEquals("&lt", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeNestedAmpersand() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&amp&foo;");
        assertEquals("&amp&foo;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeEmptyAndHashOnly() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&;&#;");
        assertEquals("&;&#;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeHexNumericParsingPath() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&#x41;");
        assertEquals(1, sw.toString().length());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeInvalidNumberPreserved() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&#12a34;");
        assertEquals("&#12a34;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterUnescapeUnknownEntityPreserved() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.unescape(sw, "&unknown;");
        assertEquals("&unknown;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeStringBasicEntities() {
        assertEquals("&lt;&amp;&gt;&apos;&quot;", Entities.XML.escape("<&>'\""));
    }

    @Test(timeout = 4000)
    public void testEscapeStringNonAsciiUnmapped() {
        assertEquals("&#169;", Entities.XML.escape("\u00A9"));
    }

    @Test(timeout = 4000)
    public void testEscapeStringPlainAscii() {
        assertEquals("abc", Entities.XML.escape("abc"));
    }

    @Test(timeout = 4000)
    public void testEscapeWriterBasicEntities() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.escape(sw, "<&>'\"");
        assertEquals("&lt;&amp;&gt;&apos;&quot;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeWriterNonAsciiUnmapped() throws Exception {
        StringWriter sw = new StringWriter();
        Entities.XML.escape(sw, "\u00A9");
        assertEquals("&#169;", sw.toString());
    }

    @Test(timeout = 4000)
    public void testAddEntityAndEscape() {
        Entities e = new Entities();
        e.addEntity("foo", 0xA1);
        assertEquals("&foo;", e.escape("\u00A1"));
        assertEquals(0xA1, e.entityValue("foo"));
        assertEquals("foo", e.entityName(0xA1));
        assertEquals(-1, e.entityValue("nope"));
        assertNull(e.entityName(0));
    }

    @Test(timeout = 4000)
    public void testAddEntitiesArray() {
        Entities e = new Entities();
        e.addEntities(new String[][]{{"foo", "161"}, {"bar", "162"}});
        assertEquals("&foo;", e.escape("\u00A1"));
        assertEquals("&bar;", e.escape("\u00A2"));
    }

    @Test(timeout = 4000)
    public void testFillWithHtml40Entities() {
        Entities e = new Entities();
        Entities.fillWithHtml40Entities(e);
        assertEquals("&copy;", e.escape("\u00A9"));
        assertEquals("fnof", e.entityName(402));
        assertEquals(402, e.entityValue("fnof"));
    }

    @Test(timeout = 4000)
    public void testStaticEntitySets() {
        assertEquals("quot", Entities.XML.entityName(34));
        assertEquals(34, Entities.XML.entityValue("quot"));
        assertEquals("nbsp", Entities.HTML32.entityName(160));
        assertEquals(160, Entities.HTML32.entityValue("nbsp"));
        assertEquals("fnof", Entities.HTML40.entityName(402));
        assertEquals(402, Entities.HTML40.entityValue("fnof"));
    }

    @Test(timeout = 4000)
    public void testPrimitiveEntityMap() {
        Entities.EntityMap map = new Entities.PrimitiveEntityMap();
        map.add("amp", 38);
        assertEquals("amp", map.name(38));
        assertNull(map.name(39));
        assertEquals(38, map.value("amp"));
        assertEquals(-1, map.value("none"));
    }

    @Test(timeout = 4000)
    public void testHashEntityMap() {
        Entities.EntityMap map = new Entities.HashEntityMap();
        map.add("amp", 38);
        assertEquals("amp", map.name(38));
        assertNull(map.name(39));
        assertEquals(38, map.value("amp"));
        assertEquals(-1, map.value("none"));
    }

    @Test(timeout = 4000)
    public void testTreeEntityMap() {
        Entities.EntityMap map = new Entities.TreeEntityMap();
        map.add("amp", 38);
        assertEquals("amp", map.name(38));
        assertNull(map.name(39));
        assertEquals(38, map.value("amp"));
        assertEquals(-1, map.value("none"));
    }

    @Test(timeout = 4000)
    public void testLookupEntityMap() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("amp", 38);
        map.add("big", 300);
        assertEquals("amp", map.name(38));
        assertEquals("big", map.name(300));
        assertNull(map.name(39));
        assertNull(map.name(256));
        assertEquals(-1, map.value("none"));
        assertEquals(300, map.value("big"));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMap() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
        map.add("a", 1);
        map.add("b", 2);
        map.add("c", 3);
        assertEquals("a", map.name(1));
        assertEquals("c", map.name(3));
        assertEquals(2, map.value("b"));
        assertNull(map.name(4));
        assertEquals(-1, map.value("d"));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapDefaultConstructor() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap();
        map.add("x", 10);
        assertEquals("x", map.name(10));
        assertEquals(10, map.value("x"));
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMap() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("one", 1);
        map.add("three", 3);
        map.add("two", 2);
        map.add("two-dup", 2);
        map.add("three-dup", 3);
        assertEquals("one", map.name(1));
        assertEquals("two", map.name(2));
        assertEquals("three", map.name(3));
        assertNull(map.name(4));
        assertNull(map.name(0));
        assertEquals(2, map.value("two"));
        assertEquals(-1, map.value("missing"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testAddEntitiesInvalidNumberThrows() {
        new Entities().addEntities(new String[][]{{"bad", "notanumber"}});
    }
}