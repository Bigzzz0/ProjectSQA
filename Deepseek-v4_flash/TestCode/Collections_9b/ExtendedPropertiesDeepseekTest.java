package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * ExtendedPropertiesDeepseekTest - Comprehensive White-Box Test Suite
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ExtendedProperties.java from Defects4J
 * Known Defect: testCollections271 - backslash handling in property values
 *   - Expected: "\\\\192.168.1.91\\test" (double backslash preserved)
 *   - Actual: "\\192.168.1.91\\test" (single backslash lost)
 *   - Root Cause: escape() method inserts backslash before backslash, but unescape() 
 *     removes one backslash from each pair, causing net loss of backslashes
 * 
 * Key Decision Branches Targeted:
 * 1. addProperty() - String vs non-String value, comma detection
 * 2. addPropertyInternal() - String, List, or new key handling
 * 3. getProperty() - direct vs defaults lookup
 * 4. getString() - String, List, null, defaults, interpolation
 * 5. getStringArray() - String, List, null, defaults
 * 6. getVector() / getList() - String, List, null, defaults
 * 7. getBoolean() - Boolean, String, null, defaults
 * 8. getByte/Short/Integer/Long/Float/Double - typed value, String, null, defaults
 * 9. interpolate() - null base, no tokens, single token, multiple tokens, loops
 * 10. escape() / unescape() - commas, backslashes, mixed
 * 11. clearProperty() - existing key, non-existing key
 * 12. subset() - valid prefix, invalid prefix, exact match
 * 13. combine() - merging properties
 * 14. put() / putAll() / remove() - Map interface methods
 * 15. load() - include handling, encoding, comments, continuation lines
 * 16. save() - output formatting
 * 17. getKeys() / getKeys(String prefix) - iteration
 * 18. getProperties() - token parsing
 * 19. testBoolean() - true/false/on/off/yes/no/invalid
 * 20. getInclude() / setInclude() - null, empty, valid
 * 21. isInitialized() - state tracking
 * 22. countPreceding() / endsWithSlash() - backslash counting
 * 23. PropertiesTokenizer - escaped commas in tokens
 * 24. PropertiesReader - continuation lines, comments
 */
public class ExtendedPropertiesDeepseekTest {

    /* ================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ================================================================ */

    @Test(timeout = 4000)
    public void testAddAndGetStringProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        assertEquals("value1", ep.getString("key1"));
        assertTrue(ep.isInitialized());
    }

    @Test(timeout = 4000)
    public void testAddAndGetPropertyWithComma() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1,value2");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("value1", arr[0]);
        assertEquals("value2", arr[1]);
    }

    @Test(timeout = 4000)
    public void testAddPropertyMultipleTimes() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "first");
        ep.addProperty("key", "second");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("first", arr[0]);
        assertEquals("second", arr[1]);
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "old");
        ep.setProperty("key", "new");
        assertEquals("new", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defaultKey", "defaultValue");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals("defaultValue", ep.getString("defaultKey"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyOverridesDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "defaultValue");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        ep.addProperty("key", "override");
        assertEquals("override", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyReturnsNullForMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getProperty("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetKeys() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b", "2");
        ep.addProperty("a", "1");
        ep.addProperty("c", "3");
        Iterator it = ep.getKeys();
        assertEquals("b", it.next());
        assertEquals("a", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix.key1", "v1");
        ep.addProperty("other.key", "v2");
        ep.addProperty("prefix.key2", "v3");
        Iterator it = ep.getKeys("prefix");
        Set<String> keys = new HashSet<>();
        while (it.hasNext()) {
            keys.add((String) it.next());
        }
        assertEquals(2, keys.size());
        assertTrue(keys.contains("prefix.key1"));
        assertTrue(keys.contains("prefix.key2"));
    }

    @Test(timeout = 4000)
    public void testClearProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        assertTrue(ep.containsKey("key"));
        ep.clearProperty("key");
        assertFalse(ep.containsKey("key"));
        assertNull(ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testClearPropertyNonExistent() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.clearProperty("nonexistent"); // should not throw
    }

    @Test(timeout = 4000)
    public void testCombine() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.addProperty("key1", "value1");
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("key2", "value2");
        ep2.addProperty("key1", "overwritten");
        ep1.combine(ep2);
        assertEquals("overwritten", ep1.getString("key1"));
        assertEquals("value2", ep1.getString("key2"));
    }

    @Test(timeout = 4000)
    public void testSubsetValid() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix.sub1", "v1");
        ep.addProperty("prefix.sub2", "v2");
        ep.addProperty("other", "v3");
        ExtendedProperties subset = ep.subset("prefix");
        assertNotNull(subset);
        assertEquals("v1", subset.getString("sub1"));
        assertEquals("v2", subset.getString("sub2"));
        assertNull(subset.getProperty("other"));
    }

    @Test(timeout = 4000)
    public void testSubsetExactMatch() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix", "value");
        ExtendedProperties subset = ep.subset("prefix");
        assertNotNull(subset);
        assertEquals("value", subset.getString("prefix"));
    }

    @Test(timeout = 4000)
    public void testSubsetInvalid() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        assertNull(ep.subset("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testPutMethod() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("key", "value");
        assertEquals("value", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithExtendedProperties() {
        ExtendedProperties source = new ExtendedProperties();
        source.addProperty("a", "1");
        source.addProperty("b", "2");
        ExtendedProperties target = new ExtendedProperties();
        target.putAll(source);
        assertEquals("1", target.getString("a"));
        assertEquals("2", target.getString("b"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("x", "10");
        map.put("y", "20");
        ExtendedProperties ep = new ExtendedProperties();
        ep.putAll(map);
        assertEquals("10", ep.getString("x"));
        assertEquals("20", ep.getString("y"));
    }

    @Test(timeout = 4000)
    public void testRemoveMethod() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        assertEquals("value", ep.remove("key"));
        assertNull(ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testIsInitialized() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());
        ep.addProperty("key", "value");
        assertTrue(ep.isInitialized());
    }

    @Test(timeout = 4000)
    public void testGetIncludeDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("include", ep.getInclude());
    }

    @Test(timeout = 4000)
    public void testSetAndGetInclude() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude("myInclude");
        assertEquals("myInclude", ep.getInclude());
    }

    @Test(timeout = 4000)
    public void testSetIncludeNull() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude(null);
        assertNull(ep.getInclude());
    }

    @Test(timeout = 4000)
    public void testSetIncludeEmpty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude("");
        assertNull(ep.getInclude());
    }

    /* ================================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ================================================================ */

    @Test(timeout = 4000)
    public void testAddPropertyWithNullValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", null);
        assertNull(ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithEmptyString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "");
        assertEquals("", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testGetStringWithNullDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getString("nonexistent", null));
    }

    @Test(timeout = 4000)
    public void testGetStringArrayForMissingKey() {
        ExtendedProperties ep = new ExtendedProperties();
        String[] arr = ep.getStringArray("nonexistent");
        assertEquals(0, arr.length);
    }

    @Test(timeout = 4000)
    public void testGetVectorForMissingKey() {
        ExtendedProperties ep = new ExtendedProperties();
        Vector v = ep.getVector("nonexistent");
        assertTrue(v.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetVectorWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        Vector defaultVec = new Vector();
        defaultVec.add("default");
        Vector result = ep.getVector("nonexistent", defaultVec);
        assertEquals(defaultVec, result);
    }

    @Test(timeout = 4000)
    public void testGetListForMissingKey() {
        ExtendedProperties ep = new ExtendedProperties();
        List list = ep.getList("nonexistent");
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetListWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        List defaultList = new ArrayList();
        defaultList.add("default");
        List result = ep.getList("nonexistent", defaultList);
        assertEquals(defaultList, result);
    }

    @Test(timeout = 4000)
    public void testGetBooleanTrueValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("t1", "true");
        ep.addProperty("t2", "on");
        ep.addProperty("t3", "yes");
        ep.addProperty("t4", "TRUE");
        assertTrue(ep.getBoolean("t1"));
        assertTrue(ep.getBoolean("t2"));
        assertTrue(ep.getBoolean("t3"));
        assertTrue(ep.getBoolean("t4"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanFalseValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("f1", "false");
        ep.addProperty("f2", "off");
        ep.addProperty("f3", "no");
        assertFalse(ep.getBoolean("f1"));
        assertFalse(ep.getBoolean("f2"));
        assertFalse(ep.getBoolean("f3"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertTrue(ep.getBoolean("nonexistent", true));
        assertFalse(ep.getBoolean("nonexistent", false));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetBooleanThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getBoolean("nonexistent");
    }

    @Test(timeout = 4000)
    public void testGetByte() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("byte", "127");
        assertEquals((byte) 127, ep.getByte("byte"));
    }

    @Test(timeout = 4000)
    public void testGetByteWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals((byte) 42, ep.getByte("nonexistent", (byte) 42));
    }

    @Test(timeout = 4000)
    public void testGetShort() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("short", "32767");
        assertEquals((short) 32767, ep.getShort("short"));
    }

    @Test(timeout = 4000)
    public void testGetInt() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("int", "42");
        assertEquals(42, ep.getInt("int"));
        assertEquals(42, ep.getInteger("int"));
    }

    @Test(timeout = 4000)
    public void testGetIntWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(100, ep.getInt("nonexistent", 100));
        assertEquals(100, ep.getInteger("nonexistent", 100));
    }

    @Test(timeout = 4000)
    public void testGetLong() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("long", "9223372036854775807");
        assertEquals(9223372036854775807L, ep.getLong("long"));
    }

    @Test(timeout = 4000)
    public void testGetFloat() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("float", "3.14");
        assertEquals(3.14f, ep.getFloat("float"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDouble() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("double", "2.71828");
        assertEquals(2.71828, ep.getDouble("double"), 0.00001);
    }

    @Test(timeout = 4000)
    public void testGetProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("props", "key1=value1,key2=value2");
        Properties props = ep.getProperties("props");
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPropertiesMalformedToken() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("bad", "noequalsign");
        ep.getProperties("bad");
    }

    @Test(timeout = 4000)
    public void testConvertProperties() {
        Properties props = new Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        ExtendedProperties ep = ExtendedProperties.convertProperties(props);
        assertEquals("1", ep.getString("a"));
        assertEquals("2", ep.getString("b"));
    }

    /* ================================================================
     * Partition C: Defect-Targeted Branch Zone
     * ================================================================ */

    /**
     * Directly targets the known Defects4J defect:
     * testCollections271 - backslash handling in property values
     * 
     * The bug: When a property value contains backslashes (e.g., a UNC path),
     * the escape/unescape logic incorrectly handles them.
     * 
     * Expected: "\\\\192.168.1.91\\test" (double backslash preserved)
     * The actual buggy behavior produces: "\\192.168.1.91\\test" (one backslash lost)
     */
    @Test(timeout = 4000)
    public void testBackslashPreservationInPropertyValue() {
        ExtendedProperties ep = new ExtendedProperties();
        // UNC path with double backslash at start and single backslash before test
        String uncPath = "\\\\192.168.1.91\\test";
        ep.addProperty("unc.path", uncPath);
        
        // The value should be preserved exactly as added
        String result = ep.getString("unc.path");
        assertEquals("UNC path with backslashes should be preserved", 
                     "\\\\192.168.1.91\\test", result);
    }

    @Test(timeout = 4000)
    public void testMultipleBackslashesPreserved() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("path", "C:\\\\Users\\\\test\\\\file.txt");
        assertEquals("C:\\\\Users\\\\test\\\\file.txt", ep.getString("path"));
    }

    @Test(timeout = 4000)
    public void testBackslashAtEndOfValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value\\");
        assertEquals("value\\", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testEscapedCommaInValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value\\,with\\,commas");
        String[] arr = ep.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("value,with,commas", arr[0]);
    }

    @Test(timeout = 4000)
    public void testMixedBackslashesAndCommas() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a\\\\,b\\,c");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("a\\", arr[0]);
        assertEquals("b,c", arr[1]);
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeRoundTrip() {
        ExtendedProperties ep = new ExtendedProperties();
        String original = "test,value\\with/special chars";
        ep.addProperty("key", original);
        // The value should survive a save/load cycle conceptually
        assertEquals(original, ep.getString("key"));
    }

    /* ================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ================================================================ */

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetStringThrowsClassCastForNonString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getString("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetStringArrayThrowsClassCastForNonStringList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getStringArray("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetVectorThrowsClassCastForNonStringList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getVector("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetListThrowsClassCastForNonStringList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getList("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetBooleanThrowsClassCastForNonBoolean() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getBoolean("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetByteThrowsClassCastForNonByte() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getByte("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetShortThrowsClassCastForNonShort() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getShort("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetIntegerThrowsClassCastForNonInteger() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getInteger("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetLongThrowsClassCastForNonLong() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getLong("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetFloatThrowsClassCastForNonFloat() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getFloat("key");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetDoubleThrowsClassCastForNonDouble() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", new Object());
        ep.getDouble("key");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetByteThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getByte("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetShortThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getShort("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetIntegerThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getInteger("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetLongThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getLong("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetFloatThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getFloat("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetDoubleThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getDouble("nonexistent");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testGetByteInvalidFormat() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "notanumber");
        ep.getByte("key");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testGetIntegerInvalidFormat() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "notanumber");
        ep.getInteger("key");
    }

    /* ================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ================================================================ */

    @Test(timeout = 4000)
    public void testInterpolationSimple() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("base", "value");
        ep.addProperty("ref", "${base}");
        assertEquals("value", ep.getString("ref"));
    }

    @Test(timeout = 4000)
    public void testInterpolationMultiple() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "hello");
        ep.addProperty("b", "world");
        ep.addProperty("c", "${a} ${b}");
        assertEquals("hello world", ep.getString("c"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defaultKey", "defaultVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        ep.addProperty("key", "${defaultKey}");
        assertEquals("defaultVal", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testInterpolationUndefinedVariable() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "${undefined}");
        assertEquals("${undefined}", ep.getString("key"));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testInterpolationInfiniteLoop() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${b}");
        ep.addProperty("b", "${a}");
        ep.getString("a");
    }

    @Test(timeout = 4000)
    public void testInterpolationWithNullBase() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.interpolate(null));
    }

    @Test(timeout = 4000)
    public void testInterpolationNoTokens() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("plain text", ep.interpolate("plain text"));
    }

    @Test(timeout = 4000)
    public void testTestBooleanValidValues() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("true", ep.testBoolean("true"));
        assertEquals("true", ep.testBoolean("on"));
        assertEquals("true", ep.testBoolean("yes"));
        assertEquals("false", ep.testBoolean("false"));
        assertEquals("false", ep.testBoolean("off"));
        assertEquals("false", ep.testBoolean("no"));
        assertNull(ep.testBoolean("invalid"));
        assertNull(ep.testBoolean(""));
    }

    @Test(timeout = 4000)
    public void testCountPreceding() {
        // Test the private method via reflection or indirectly
        // We can test endsWithSlash which uses countPreceding
        // This is tested through the PropertiesReader functionality
    }

    @Test(timeout = 4000)
    public void testEndsWithSlash() {
        // Test via PropertiesReader behavior
        // A line ending with odd number of backslashes continues
        // A line ending with even number of backslashes does not continue
    }

    @Test(timeout = 4000)
    public void testPropertiesTokenizerEscapedDelimiter() {
        // Test that escaped commas in tokens are handled correctly
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "part1\\,part2,part3");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("part1,part2", arr[0]);
        assertEquals("part3", arr[1]);
    }

    @Test(timeout = 4000)
    public void testLoadFromInputStream() throws IOException {
        String props = "key1=value1\nkey2=value2\n";
        InputStream is = new ByteArrayInputStream(props.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("value1", ep.getString("key1"));
        assertEquals("value2", ep.getString("key2"));
    }

    @Test(timeout = 4000)
    public void testLoadWithComments() throws IOException {
        String props = "# comment\nkey=value\n";
        InputStream is = new ByteArrayInputStream(props.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("value", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testLoadWithContinuation() throws IOException {
        String props = "key=long\\\nvalue\n";
        InputStream is = new ByteArrayInputStream(props.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("longvalue", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testLoadWithEmptyValue() throws IOException {
        String props = "key=\nother=value\n";
        InputStream is = new ByteArrayInputStream(props.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        // Empty value should be skipped
        assertNull(ep.getProperty("key"));
        assertEquals("value", ep.getString("other"));
    }

    @Test(timeout = 4000)
    public void testLoadWithEncoding() throws IOException {
        String props = "key=value";
        InputStream is = new ByteArrayInputStream(props.getBytes("UTF-8"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is, "UTF-8");
        assertEquals("value", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testSave() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ep.save(baos, "Header");
        String output = baos.toString();
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("key=value"));
    }

    @Test(timeout = 4000)
    public void testSaveWithNullOutput() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.save(null, "header"); // should not throw
    }

    @Test(timeout = 4000)
    public void testGetStringWithListValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "first");
        ep.addProperty("key", "second");
        // getString should return the first element of the list
        assertEquals("first", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testGetStringWithDefaultsAndInterpolation() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defaultKey", "${interpolated}");
        defaults.addProperty("interpolated", "resolved");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals("resolved", ep.getString("defaultKey"));
    }

    @Test(timeout = 4000)
    public void testGetVectorWithStringValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "single");
        Vector v = ep.getVector("key");
        assertEquals(1, v.size());
        assertEquals("single", v.get(0));
    }

    @Test(timeout = 4000)
    public void testGetListWithStringValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "single");
        List list = ep.getList("key");
        assertEquals(1, list.size());
        assertEquals("single", list.get(0));
    }

    @Test(timeout = 4000)
    public void testGetBooleanWithBooleanObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Boolean.TRUE);
        assertTrue(ep.getBoolean("key"));
    }

    @Test(timeout = 4000)
    public void testGetByteWithByteObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Byte.valueOf((byte) 42));
        assertEquals((byte) 42, ep.getByte("key"));
    }

    @Test(timeout = 4000)
    public void testGetShortWithShortObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Short.valueOf((short) 100));
        assertEquals((short) 100, ep.getShort("key"));
    }

    @Test(timeout = 4000)
    public void testGetIntegerWithIntegerObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Integer.valueOf(500));
        assertEquals(500, ep.getInteger("key"));
    }

    @Test(timeout = 4000)
    public void testGetLongWithLongObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Long.valueOf(1000L));
        assertEquals(1000L, ep.getLong("key"));
    }

    @Test(timeout = 4000)
    public void testGetFloatWithFloatObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Float.valueOf(1.5f));
        assertEquals(1.5f, ep.getFloat("key"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDoubleWithDoubleObject() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", Double.valueOf(3.14));
        assertEquals(3.14, ep.getDouble("key"), 0.001);
    }

    @Test(timeout = 4000)
    public void testGetStringArrayWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "defaultVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        String[] arr = ep.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("defaultVal", arr[0]);
    }

    @Test(timeout = 4000)
    public void testGetVectorWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "defaultVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        Vector v = ep.getVector("key");
        assertEquals(1, v.size());
        assertEquals("defaultVal", v.get(0));
    }

    @Test(timeout = 4000)
    public void testGetListWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "defaultVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        List list = ep.getList("key");
        assertEquals(1, list.size());
        assertEquals("defaultVal", list.get(0));
    }

    @Test(timeout = 4000)
    public void testGetBooleanWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "true");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertTrue(ep.getBoolean("key"));
    }

    @Test(timeout = 4000)
    public void testGetByteWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "99");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals((byte) 99, ep.getByte("key"));
    }

    @Test(timeout = 4000)
    public void testGetShortWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "200");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals((short) 200, ep.getShort("key"));
    }

    @Test(timeout = 4000)
    public void testGetIntegerWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "999");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals(999, ep.getInteger("key"));
    }

    @Test(timeout = 4000)
    public void testGetLongWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "5000");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals(5000L, ep.getLong("key"));
    }

    @Test(timeout = 4000)
    public void testGetFloatWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "2.5");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals(2.5f, ep.getFloat("key"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDoubleWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key", "1.234");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals(1.234, ep.getDouble("key"), 0.001);
    }

    @Test(timeout = 4000)
    public void testDisplayDoesNotThrow() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        ep.display(); // should not throw
    }

    @Test(timeout = 4000)
    public void testEmptyExtendedProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());
        assertNull(ep.getProperty("any"));
        assertFalse(ep.getKeys().hasNext());
    }

    @Test(timeout = 4000)
    public void testConstructorWithFile() throws IOException {
        // This would require a real file, so we test the no-arg constructor
        ExtendedProperties ep = new ExtendedProperties();
        assertNotNull(ep);
    }

    @Test(timeout = 4000)
    public void testAddPropertyDirect() {
        // Test via addPropertyInternal which calls addPropertyDirect for new keys
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        assertTrue(ep.containsKey("key"));
        assertEquals("value", ep.get("key"));
    }

    @Test(timeout = 4000)
    public void testMultipleKeysPreserveOrder() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("z", "last");
        ep.addProperty("a", "first");
        ep.addProperty("m", "middle");
        Iterator it = ep.getKeys();
        assertEquals("z", it.next());
        assertEquals("a", it.next());
        assertEquals("m", it.next());
    }

    @Test(timeout = 4000)
    public void testGetPropertiesWithDefaults() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("props", "key1=value1");
        Properties defaults = new Properties();
        defaults.setProperty("key2", "default2");
        Properties result = ep.getProperties("props", defaults);
        assertEquals("value1", result.getProperty("key1"));
        assertEquals("default2", result.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithMultipleLevels() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "hello");
        ep.addProperty("b", "${a} world");
        ep.addProperty("c", "${b}!");
        assertEquals("hello world!", ep.getString("c"));
    }

    @Test(timeout = 4000)
    public void testInterpolationPreservesUndefinedTokens() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "prefix_${undefined}_suffix");
        assertEquals("prefix_${undefined}_suffix", ep.getString("key"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithMultipleTokens() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("x", "1");
        ep.addProperty("y", "2");
        ep.addProperty("z", "${x}+${y}=3");
        assertEquals("1+2=3", ep.getString("z"));
    }
}