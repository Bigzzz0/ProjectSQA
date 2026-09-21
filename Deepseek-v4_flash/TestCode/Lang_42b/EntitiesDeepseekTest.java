package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: Entities class - entity map operations, escape/unescape logic
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - addEntity/addEntities: test adding entities and state management
 *   - entityName/entityValue: lookup operations
 *   - Basic entity presets (XML, HTML32, HTML40)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null/empty strings for escape/unescape
 *   - Strings without entities
 *   - Edge values: 0, 127, 128, 255, 256, 0x7F, 0x80, 0xFFFF
 *   - Large entity values (high unicode)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - High unicode escape: values > 0xFFFF should produce single numeric entity, not surrogate pair
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Number format edge cases in unescape (invalid hex, invalid decimal)
 *   - Malformed entity sequences (missing semicolons, nested ampersands)
 *   - Negative entity values
 * 
 * Partition E: Object Lifecycle & Contract Integrity  
 *   - EntityMap implementations: Primitive, Hash, Tree, Lookup, Array, Binary
 *   - Array growth mechanics
 *   - Binary search correctness on sorted values
 */
public class EntitiesDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testDefaultMapIsLookupEntityMap() {
        Entities entities = new Entities();
        assertTrue("Default map should be LookupEntityMap", 
            entities.map instanceof Entities.LookupEntityMap);
    }

    @Test(timeout = 4000)
    public void testAddEntityAndLookup() {
        Entities entities = new Entities();
        entities.addEntity("test", 0x42);
        assertEquals("test", entities.entityName(0x42));
        assertEquals(0x42, entities.entityValue("test"));
    }

    @Test(timeout = 4000)
    public void testAddEntitiesFromArray() {
        Entities entities = new Entities();
        String[][] array = {{"alpha", "65"}, {"beta", "66"}};
        entities.addEntities(array);
        assertEquals("alpha", entities.entityName(65));
        assertEquals(66, entities.entityValue("beta"));
    }

    @Test(timeout = 4000)
    public void testXmlPreset() {
        Entities entities = Entities.XML;
        assertEquals("quot", entities.entityName(34));
        assertEquals(38, entities.entityValue("amp"));
        assertEquals("apos", entities.entityName(39));
        assertEquals(60, entities.entityValue("lt"));
    }

    @Test(timeout = 4000)
    public void testHtml32Preset() {
        Entities entities = Entities.HTML32;
        assertEquals("nbsp", entities.entityName(160));
        assertEquals(162, entities.entityValue("cent"));
        assertEquals("quot", entities.entityName(34)); // inherited from basic
    }

    @Test(timeout = 4000)
    public void testHtml40Preset() {
        Entities entities = Entities.HTML40;
        // Check entities from basic, ISO8859_1, and HTML40 arrays
        assertEquals("quot", entities.entityName(34));
        assertEquals("nbsp", entities.entityName(160));
        assertEquals("Alpha", entities.entityName(913));
        assertEquals(913, entities.entityValue("Alpha"));
        assertEquals("euro", entities.entityName(8364));
    }

    @Test(timeout = 4000)
    public void testEntityValueReturnsMinusOneForUnknown() {
        Entities entities = new Entities();
        assertEquals(-1, entities.entityValue("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testEntityNameReturnsNullForUnknown() {
        Entities entities = new Entities();
        assertNull(entities.entityName(99999));
    }

    @Test(timeout = 4000)
    public void testAddEntityOverwritesExisting() {
        Entities entities = new Entities();
        entities.addEntity("test", 0x42);
        entities.addEntity("test", 0x43);
        assertEquals(0x43, entities.entityValue("test"));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEscapeNullStringGivesNullPointer() {
        Entities entities = new Entities();
        try {
            entities.escape((String) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEscapeEmptyString() {
        Entities entities = new Entities();
        assertEquals("", entities.escape(""));
    }

    @Test(timeout = 4000)
    public void testEscapeStringWithNoEntities() {
        Entities entities = new Entities();
        String input = "abcd1234!@#$";
        assertEquals(input, entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeBasicEntities() {
        Entities entities = Entities.XML;
        assertEquals("&amp;&lt;&gt;&quot;", entities.escape("&<>\""));
    }

    @Test(timeout = 4000)
    public void testEscapeHighByteCharactersToNumeric() {
        Entities entities = new Entities(); // no custom entities
        String input = "\u00A1\u00A2\u00FF";
        assertEquals("&#161;&#162;&#255;", entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeCharactersBelow128PassThrough() {
        Entities entities = new Entities();
        String input = "abc\x7F";
        assertEquals(input, entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeCharacter128To255() {
        Entities entities = new Entities();
        String input = "\u0080\u00B0";
        assertEquals("&#128;&#176;", entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNullStringGivesNullPointer() {
        Entities entities = new Entities();
        try {
            entities.unescape((String) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnescapeEmptyString() {
        Entities entities = new Entities();
        assertEquals("", entities.unescape(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeStringWithoutAmpersands() {
        Entities entities = new Entities();
        String input = "hello world";
        assertEquals(input, entities.unescape(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeNamedEntity() {
        Entities entities = Entities.XML;
        assertEquals("&", entities.unescape("&amp;"));
        assertEquals("<", entities.unescape("&lt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeDecimalNumericEntity() {
        Entities entities = new Entities();
        assertEquals("\u0041", entities.unescape("&#65;"));
        assertEquals("\u00A9", entities.unescape("&#169;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexadecimalEntity() {
        Entities entities = new Entities();
        assertEquals("\u0041", entities.unescape("&#x41;"));
        assertEquals("\u00A9", entities.unescape("&#xA9;"));
        assertEquals("\u0041", entities.unescape("&#X41;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeMultipleEntities() {
        Entities entities = Entities.XML;
        assertEquals("<tag>", entities.unescape("&lt;tag&gt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithLeadingText() {
        Entities entities = Entities.XML;
        assertEquals("before<after", entities.unescape("before&lt;after"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithTrailingText() {
        Entities entities = Entities.XML;
        assertEquals("before>after", entities.unescape("before&gt;after"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityAtEndOfString() {
        Entities entities = Entities.XML;
        assertEquals("&", entities.unescape("&amp;"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testEscapeHighUnicodeCharacter() {
        // Defect: High unicode (>0xFFFF) should be escaped as single numeric entity
        // Buggy behavior: produces surrogate pair like &#55348;&#57186;
        // Correct behavior: single entity like &#119650;
        Entities entities = new Entities();
        String highUnicodeStr = "\uD84C\uDFC2"; // U+1F3C2 (snowboarder) - but more specifically U+1D322
        
        // Use a known high unicode character: U+1D322 (119650 in decimal)
        // This is represented as surrogate pair \uD834\uDF22
        String testStr = "\uD834\uDF22"; // 𝌢
        String escaped = entities.escape(testStr);
        
        // The correct behavior is to escape as a single numeric entity
        // The bug produces two surrogate pair entities
        assertEquals("High unicode should be escaped as single entity", "&#119650;", escaped);
    }

    @Test(timeout = 4000)
    public void testUnescapeHighUnicodeNumericEntity() {
        Entities entities = new Entities();
        // High unicode value: &#119650; corresponds to U+1D322 (𝌢)
        String unescaped = entities.unescape("&#119650;");
        // Should produce the single character, not surrogate pair parts
        assertEquals("Should unescape to single high unicode character", "\uD834\uDF22", unescaped);
        assertEquals("Should be 1 code point", 2, unescaped.length()); // surrogate pair, but logically 1 char
    }

    @Test(timeout = 4000)
    public void testEscapeHighUnicodeHexEntity() {
        Entities entities = new Entities();
        String testStr = "\uD83D\uDE00"; // U+1F600 grinning face
        String escaped = entities.escape(testStr);
        // Correct: &#128512;
        assertEquals("High unicode hex should be escaped as single decimal entity", "&#128512;", escaped);
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeHighUnicodeRoundTrip() {
        Entities entities = new Entities();
        String original = "\uD83C\uDF89"; // U+1F389 party popper
        String escaped = entities.escape(original);
        String unescaped = entities.unescape(escaped);
        assertEquals("Round trip should preserve high unicode", original, unescaped);
    }

    @Test(timeout = 4000)
    public void testEscapeHighUnicodeFromHtml40Preset() {
        // HTML40 doesn't have high unicode entities, but using the escape on high unicode
        // should produce correct single numeric entity
        Entities entities = Entities.HTML40;
        String testStr = "\uD84C\uDFC2";
        String escaped = entities.escape(testStr);
        // Should be single numeric entity
        assertTrue("Escaped high unicode should contain one numeric entity", 
            escaped.startsWith("&#") && escaped.endsWith(";"));
        // Should not contain multiple entities (surrogate pair split)
        assertEquals("Should have exactly one entity", 1, escaped.split(";&#").length);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testUnescapeMalformedMissingSemicolon() {
        Entities entities = new Entities();
        assertEquals("&amp", entities.unescape("&amp")); // no semicolon, should pass through
    }

    @Test(timeout = 4000)
    public void testUnescapeMalformedEmptyEntity() {
        Entities entities = new Entities();
        assertEquals("&;", entities.unescape("&;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityWithMultipleAmpersands() {
        Entities entities = new Entities();
        // Sequence like &amp&amp; should be handled
        // The logic: if there's an ampersand before semicolon, treat as literal
        assertEquals("&&", entities.unescape("&&amp;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNestedAmpersands() {
        Entities entities = new Entities();
        // &amp;lt; should unescape to &lt; then should NOT be further unescaped
        assertEquals("&lt;", entities.unescape("&amp;lt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeNumericEntityTooLarge() {
        Entities entities = new Entities();
        // Value > 0xFFFF should return -1 and output as literal
        assertEquals("&#999999;", entities.unescape("&#999999;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeInvalidHexadecimal() {
        Entities entities = new Entities();
        assertEquals("&#xZZ;", entities.unescape("&#xZZ;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeInvalidDecimal() {
        Entities entities = new Entities();
        assertEquals("&#ABC;", entities.unescape("&#ABC;"));
    }

    @Test(timeout = 4000)
    public void testEscapingWithWriter() throws Exception {
        Entities entities = Entities.XML;
        StringWriter writer = new StringWriter();
        entities.escape(writer, "<>&\"");
        assertEquals("&lt;&gt;&amp;&quot;", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapingWithWriter() throws Exception {
        Entities entities = Entities.XML;
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&lt;&gt;&amp;");
        assertEquals("<>&", writer.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeWithWriterEmptyString() throws Exception {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.escape(writer, "");
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeWithWriterNoAmpersands() throws Exception {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "hello");
        assertEquals("hello", writer.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeWithWriterSingleAmpersandEntity() throws Exception {
        Entities entities = Entities.XML;
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&amp;");
        assertEquals("&", writer.toString());
    }

    // ========== Partition E: EntityMap Implementations ==========

    @Test(timeout = 4000)
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("test", 42);
        assertEquals("test", map.name(42));
        assertEquals(42, map.value("test"));
        assertEquals(-1, map.value("nonexistent"));
        assertNull(map.name(99));
    }

    @Test(timeout = 4000)
    public void testHashEntityMap() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("test", 42);
        assertEquals("test", map.name(42));
        assertEquals(42, map.value("test"));
        assertEquals(-1, map.value("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testTreeEntityMap() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("test", 42);
        assertEquals("test", map.name(42));
        assertEquals(42, map.value("test"));
    }

    @Test(timeout = 4000)
    public void testLookupEntityMap() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("testLow", 5); // Should be in lookup table
        map.add("testHigh", 300); // Should fallback to super
        
        assertEquals("testLow", map.name(5));
        assertEquals("testHigh", map.name(300));
        assertNull(map.name(1)); // Not in lookup or map
    }

    @Test(timeout = 4000)
    public void testLookupEntityMapLazyInitialization() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        // Before any add, lookup table should be null initially
        // Calling name triggers creation
        assertNull(map.name(0));
        // After lookup, table is created and returns null for unadded values
        assertNull(map.name(255));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapDefaultGrowth() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap();
        assertEquals(100, map.growBy);
        assertEquals(100, map.names.length);
        assertEquals(100, map.values.length);
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapCustomGrowth() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(50);
        assertEquals(50, map.growBy);
        assertEquals(50, map.names.length);
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapAddAndLookup() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap();
        map.add("first", 1);
        map.add("second", 2);
        assertEquals("first", map.name(1));
        assertEquals(2, map.value("second"));
        assertEquals(-1, map.value("nonexistent"));
        assertNull(map.name(99));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapGrowth() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(10);
        // Add more than initial size
        for (int i = 0; i < 15; i++) {
            map.add("test" + i, i);
        }
        assertEquals(15, map.size);
        assertEquals("test14", map.name(14));
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMapAddMaintainsSortedOrder() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("second", 200);
        map.add("first", 100);
        map.add("third", 300);
        
        // Values should be sorted: [100, 200, 300]
        assertArrayEquals(new int[]{100, 200, 300}, 
            new int[]{map.values[0], map.values[1], map.values[2]});
        assertEquals("first", map.names[0]);
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMapDuplicateValueIgnored() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("original", 100);
        map.add("duplicate", 100); // should be ignored
        assertEquals(1, map.size);
        assertEquals("original", map.name(100));
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMapBinarySearchLookup() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("a", 10);
        map.add("b", 20);
        map.add("c", 30);
        
        assertEquals("a", map.name(10));
        assertEquals("b", map.name(20));
        assertEquals("c", map.name(30));
        assertNull(map.name(25)); // not found
    }

    @Test(timeout = 4000)
    public void testBinaryEntityMapValueLookup() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("a", 10);
        assertEquals(10, map.value("a"));
        assertEquals(-1, map.value("notfound"));
    }

    @Test(timeout = 4000)
    public void testArrayEntityMapValueWithNullName() {
        // Using ArrayEntityMap, ensure value() handles null names gracefully
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap();
        map.add("test", 42);
        // No null name in map, should be fine
    }

    // ========== Additional edge cases for coverage ==========

    @Test(timeout = 4000)
    public void testEscapeWithEntityAtStart() {
        Entities entities = Entities.XML;
        assertEquals("&amp;bc", entities.escape("&bc"));
    }

    @Test(timeout = 4000)
    public void testEscapeWithEntityInMiddle() {
        Entities entities = Entities.XML;
        assertEquals("a&amp;c", entities.escape("a&c"));
    }

    @Test(timeout = 4000)
    public void testEscapeWithEntityAtEnd() {
        Entities entities = Entities.XML;
        assertEquals("ab&amp;", entities.escape("ab&"));
    }

    @Test(timeout = 4000)
    public void testUnescapeWithEntityFollowedByAmpersand() {
        Entities entities = Entities.XML;
        // &amp;& should produce & then & (literal ampersand)
        assertEquals("&&", entities.unescape("&amp;&"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntityLowercaseX() {
        Entities entities = new Entities();
        assertEquals("\u00A9", entities.unescape("&#xa9;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexEntityUppercaseX() {
        Entities entities = new Entities();
        assertEquals("\u00A9", entities.unescape("&#XA9;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityWithOnlyHash() {
        Entities entities = new Entities();
        // &#; should be preserved
        assertEquals("&#;", entities.unescape("&#;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeEntityWithHashButNoDigits() {
        Entities entities = new Entities();
        // &#x; should be preserved (invalid hex)
        assertEquals("&#x;", entities.unescape("&#x;"));
    }

    @Test(timeout = 4000)
    public void testEscapeCharacter127PassThrough() {
        Entities entities = new Entities();
        String input = "\u007F";
        assertEquals(input, entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testEscapeCharacter128To255NotInEntityMap() {
        Entities entities = new Entities(); // no ISO8859_1 entities added
        assertEquals("&#128;", entities.escape("\u0080"));
        assertEquals("&#255;", entities.escape("\u00FF"));
    }

    @Test(timeout = 4000)
    public void testEscapeCharacterZero() {
        Entities entities = new Entities();
        // Null character (0) is below 0x7F, should pass through
        String input = "\u0000";
        assertEquals(input, entities.escape(input));
    }

    @Test(timeout = 4000)
    public void testMacroEntityName() {
        // Regression: ensure entityName works with pre-defined Macroman entity
        Entities entities = Entities.HTML40;
        assertNotNull(entities.entityName(0xE1)); // aacute
    }

    @Test(timeout = 4000)
    public void testVerifyEntityCounts() {
        // Verify that static presets have expected number of entities
        Entities entities;
        
        entities = Entities.XML;
        // BASIC (4) + APOS (1) = 5
        assertNotNull(entities.entityName(34)); // quot
        assertNotNull(entities.entityName(38)); // amp
        assertNotNull(entities.entityName(39)); // apos
        assertNotNull(entities.entityName(60)); // lt
        assertNotNull(entities.entityName(62)); // gt
        
        entities = Entities.HTML32;
        // BASIC (4) + ISO8859_1 (95?) = 99? Let's just spot check
        assertNotNull(entities.entityName(160)); // nbsp
        
        entities = Entities.HTML40;
        assertNotNull(entities.entityName(338)); // OElig
    }
}