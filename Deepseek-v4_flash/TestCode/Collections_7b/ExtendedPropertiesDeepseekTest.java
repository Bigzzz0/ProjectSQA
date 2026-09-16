package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.io.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Target class: ExtendedProperties
 * Known defect: Issues with key set iteration when properties are added via addProperty/putAll
 *   - testKeySet2: NoSuchElementException when iterating keys
 *   - testKeySet3: NoSuchElementException when iterating keys after operations
 *   - testKeySet4: Wrong ordering of keys (expected [c] but got [b])
 * 
 * Key branches targeted:
 * 1. addProperty: String with commas vs without commas; non-String values
 * 2. addPropertyInternal: String -> Vector conversion, List appending, new key insertion
 * 3. clearProperty: Removing key from keysAsListed + map
 * 4. setProperty: clearProperty + addProperty chain
 * 5. putAll: ExtendedProperties vs regular Map ordering
 * 6. addPropertyDirect: key list maintenance
 * 7. combine: Iteration and setProperty
 * 8. getKeys: Iterator from keysAsListed
 * 9. getKeys(String prefix): filtering logic
 * 10. subset: Prefix extraction with edge cases
 * 11. getString, getBoolean, getByte, etc: Type conversion and defaults
 * 12. interpolate: Recursive variable substitution with loop detection
 * 13. load: File inclusion logic
 * 
 * Boundary conditions:
 * - Null keys/values
 * - Empty strings
 * - Single vs multiple values under same key
 * - Comma-separated values with escaped commas
 * - Backslash escaping
 * - File separator handling in include
 * - Defaults propagation
 * - isInitialized flag
 * - Duplicate property addition
 */
public class ExtendedPropertiesDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddAndGetSimpleProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        assertEquals("value1", ep.getProperty("key1"));
        assertTrue(ep.isInitialized());
    }

    @Test(timeout = 4000)
    public void testAddPropertyDuplicateKeyCreatesVector() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.addProperty("key", "value2");
        Object val = ep.getProperty("key");
        assertTrue("Should be a List", val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("value1", list.get(0));
        assertEquals("value2", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithCommaSeparatedValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a,b,c");
        Object val = ep.getProperty("key");
        assertTrue("Should be a List", val instanceof List);
        List list = (List) val;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithEscapedComma() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a\\,b,c");
        Object val = ep.getProperty("key");
        assertTrue("Should be a List", val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));  // escaped comma preserved
        assertEquals("c", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddPropertyNonStringValue() {
        ExtendedProperties ep = new ExtendedProperties();
        Integer intVal = Integer.valueOf(42);
        ep.addProperty("key", intVal);
        assertEquals(intVal, ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.addProperty("key", "value2");
        ep.setProperty("key", "newvalue");
        assertEquals("newvalue", ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testSetPropertyOnSingleValueReplaces() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.setProperty("key", "newvalue");
        assertEquals("newvalue", ep.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testClearPropertyRemovesKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        ep.addProperty("key2", "value2");
        ep.clearProperty("key1");
        assertNull(ep.getProperty("key1"));
        assertEquals("value2", ep.getProperty("key2"));
        // Verify key is removed from iteration
        Iterator it = ep.getKeys();
        assertTrue(it.hasNext());
        assertEquals("key2", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testClearPropertyOnListValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.addProperty("key", "value2");
        ep.clearProperty("key");
        assertNull(ep.getProperty("key"));
        assertFalse(ep.getKeys().hasNext());
    }

    @Test(timeout = 4000)
    public void testGetKeysReturnsOrderedKeys() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("c", "3");
        ep.addProperty("a", "1");
        ep.addProperty("b", "2");
        Iterator it = ep.getKeys();
        assertEquals("c", it.next());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("app.name", "test");
        ep.addProperty("app.version", "1.0");
        ep.addProperty("other", "value");
        Iterator it = ep.getKeys("app.");
        List<String> keys = new ArrayList<>();
        while (it.hasNext()) keys.add(it.next());
        assertEquals(2, keys.size());
        assertTrue(keys.contains("app.name"));
        assertTrue(keys.contains("app.version"));
    }

    @Test(timeout = 4000)
    public void testGetKeysWithEmptyPrefixReturnsAll() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "1");
        ep.addProperty("b", "2");
        Iterator it = ep.getKeys("");
        List<String> keys = new ArrayList<>();
        while (it.hasNext()) keys.add(it.next());
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
    }

    @Test(timeout = 4000)
    public void testGetKeysWithNoMatch() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        Iterator it = ep.getKeys("nonexistent");
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefaults() {
        ExtendedProperties ep = new ExtendedProperties();
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key1", "default1");
        ep.defaults = defaults;
        
        assertEquals("default1", ep.getProperty("key1"));
        // User value overrides default
        ep.addProperty("key1", "user1");
        assertEquals("user1", ep.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithMultipleDefaults() {
        ExtendedProperties ep = new ExtendedProperties();
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("key1", "default1");
        defaults.addProperty("key2", "default2");
        ep.defaults = defaults;
        
        assertEquals("default1", ep.getProperty("key1"));
        assertEquals("default2", ep.getProperty("key2"));
        assertNull(ep.getProperty("nonexistent"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());
        assertFalse(ep.getKeys().hasNext());
        assertNull(ep.getProperty("anything"));
    }

    @Test(timeout = 4000)
    public void testNullKeyInGetProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getProperty(null));
    }

    @Test(timeout = 4000)
    public void testGetStringWithNullDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.getString("nonexistent", null));
    }

    @Test(timeout = 4000)
    public void testGetStringOnListValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.addProperty("key", "value2");
        assertEquals("value1", ep.getString("key"));  // returns first element
    }

    @Test(timeout = 4000)
    public void testGetBooleanStringValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("true1", "true");
        ep.addProperty("true2", "on");
        ep.addProperty("true3", "yes");
        ep.addProperty("false1", "false");
        ep.addProperty("false2", "off");
        ep.addProperty("false3", "no");
        
        assertTrue(ep.getBoolean("true1"));
        assertTrue(ep.getBoolean("true2"));
        assertTrue(ep.getBoolean("true3"));
        assertFalse(ep.getBoolean("false1"));
        assertFalse(ep.getBoolean("false2"));
        assertFalse(ep.getBoolean("false3"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanInvalidStringReturnsNull() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "invalid");
        assertNull(ep.testBoolean("invalid"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertTrue(ep.getBoolean("nonexistent", true));
        assertFalse(ep.getBoolean("nonexistent", false));
    }

    @Test(timeout = 4000)
    public void testGetByteWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals((byte) 42, ep.getByte("nonexistent", (byte) 42));
        ep.addProperty("key", "100");
        assertEquals((byte) 100, ep.getByte("key"));
    }

    @Test(timeout = 4000)
    public void testGetShortWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals((short) 1000, ep.getShort("nonexistent", (short) 1000));
        ep.addProperty("key", "2000");
        assertEquals((short) 2000, ep.getShort("key"));
    }

    @Test(timeout = 4000)
    public void testGetIntWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(42, ep.getInt("nonexistent", 42));
        assertEquals(100, ep.getInt("key", 42));  // no key yet
        ep.addProperty("key", "100");
        assertEquals(100, ep.getInt("key"));
    }

    @Test(timeout = 4000)
    public void testGetIntegerAndGetIntConsistency() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "42");
        assertEquals(42, ep.getInt("key"));
        assertEquals(42, ep.getInteger("key"));
    }

    @Test(timeout = 4000)
    public void testGetLongWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(100000L, ep.getLong("nonexistent", 100000L));
        ep.addProperty("key", "999999");
        assertEquals(999999L, ep.getLong("key"));
    }

    @Test(timeout = 4000)
    public void testGetFloatWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(3.14f, ep.getFloat("nonexistent", 3.14f), 0.001f);
        ep.addProperty("key", "2.718");
        assertEquals(2.718f, ep.getFloat("key"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDoubleWithDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(3.14159, ep.getDouble("nonexistent", 3.14159), 0.00001);
        ep.addProperty("key", "2.71828");
        assertEquals(2.71828, ep.getDouble("key"), 0.00001);
    }

    @Test(timeout = 4000)
    public void testGetStringArrayWithSingleValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        String[] arr = ep.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("value", arr[0]);
    }

    @Test(timeout = 4000)
    public void testGetStringArrayWithMultipleValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value1");
        ep.addProperty("key", "value2");
        String[] arr = ep.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("value1", arr[0]);
        assertEquals("value2", arr[1]);
    }

    @Test(timeout = 4000)
    public void testGetStringArrayWithNoValue() {
        ExtendedProperties ep = new ExtendedProperties();
        String[] arr = ep.getStringArray("nonexistent");
        assertEquals(0, arr.length);
    }

    @Test(timeout = 4000)
    public void testGetVectorWithSingleValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "value");
        Vector v = ep.getVector("key");
        assertEquals(1, v.size());
        assertEquals("value", v.get(0));
    }

    @Test(timeout = 4000)
    public void testGetVectorWithMultipleValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "a");
        ep.addProperty("key", "b");
        Vector v = ep.getVector("key");
        assertEquals(2, v.size());
        assertTrue(v.contains("a"));
        assertTrue(v.contains("b"));
    }

    @Test(timeout