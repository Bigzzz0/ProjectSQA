package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CaseInsensitiveMapDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Decision branches in convertKey():
     *   - Branch 1: key != null (true) -> key.toString().toLowerCase()
     *   - Branch 2: key != null (false) -> return AbstractHashedMap.NULL
     * 
     * Boundary conditions:
     *   - null key
     *   - empty string key
     *   - mixed case keys
     *   - keys with special Unicode characters (Turkish I, German ß, etc.)
     *   - keys that are not Strings (Integer, Object)
     *   - duplicate keys differing only by case
     *   - large number of entries (resize/load factor)
     *   - negative/zero initial capacity
     *   - clone() operation
     *   - serialization/deserialization
     * 
     * Known defect (Defects4J): Locale-dependent toLowerCase() fails for Turkish locale
     *   - "en" key with Turkish locale produces wrong result due to special I handling
     *   - Test must explicitly set default locale to Turkish and verify behavior
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testBasicPutAndGet() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertNull(map.put("Key1", "value1"));
        assertEquals("value1", map.get("key1"));
        assertEquals("value1", map.get("KEY1"));
        assertEquals("value1", map.get("Key1"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testPutOverwritesCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "first");
        assertEquals("first", map.put("ONE", "second"));
        assertEquals("second", map.get("one"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testContainsKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("TestKey", "value");
        assertTrue(map.containsKey("testkey"));
        assertTrue(map.containsKey("TESTKEY"));
        assertTrue(map.containsKey("TestKey"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRemove() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "value");
        assertEquals("value", map.remove("key"));
        assertNull(map.get("Key"));
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testClear() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test(timeout = 4000)
    public void testKeySet() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "1");
        map.put("Two", "2");
        map.put(null, "null");
        assertTrue(map.keySet().contains("one"));
        assertTrue(map.keySet().contains("two"));
        assertTrue(map.keySet().contains(null));
        assertEquals(3, map.keySet().size());
    }

    @Test(timeout = 4000)
    public void testValues() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("A", "alpha");
        map.put("B", "beta");
        assertTrue(map.values().contains("alpha"));
        assertTrue(map.values().contains("beta"));
        assertEquals(2, map.values().size());
    }

    @Test(timeout = 4000)
    public void testEntrySet() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Key", "value");
        assertEquals(1, map.entrySet().size());
        Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
        assertEquals("key", entry.getKey());
        assertEquals("value", entry.getValue());
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertTrue(map.isEmpty());
        map.put("a", "1");
        assertFalse(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPutAll() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Map<String, String> source = new HashMap<String, String>();
        source.put("Hello", "world");
        source.put("Foo", "bar");
        map.putAll(source);
        assertEquals("world", map.get("hello"));
        assertEquals("bar", map.get("FOO"));
        assertEquals(2, map.size());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(null, "nullValue");
        assertEquals("nullValue", map.get(null));
        assertTrue(map.containsKey(null));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testEmptyStringKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("", "empty");
        assertEquals("empty", map.get(""));
        assertEquals("empty", map.get(" ")); // space is different
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testNonStringKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put(123, "integer");
        assertEquals("integer", map.get("123"));
        assertEquals("integer", map.get(123));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testObjectKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object key = new Object();
        map.put(key, "object");
        assertEquals("object", map.get(key.toString()));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfEntries() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        for (int i = 0; i < 1000; i++) {
            map.put("Key" + i, "value" + i);
        }
        assertEquals(1000, map.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals("value" + i, map.get("key" + i));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacity() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10);
        assertTrue(map.isEmpty());
        map.put("test", "value");
        assertEquals("value", map.get("TEST"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacityAndLoadFactor() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(20, 0.75f);
        assertTrue(map.isEmpty());
        map.put("key", "val");
        assertEquals("val", map.get("KEY"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithMap() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("Alpha", "1");
        source.put("BETA", "2");
        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals("1", map.get("alpha"));
        assertEquals("2", map.get("beta"));
        assertEquals(2, map.size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithMapDuplicateKeys() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("One", "first");
        source.put("ONE", "second");
        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals("second", map.get("one"));
        assertEquals(1, map.size());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testLocaleIndependenceTurkishI() {
        // This test targets the known Defects4J defect: toLowerCase() is locale-dependent
        // Turkish locale treats 'I' and 'i' differently, causing "en" to fail
        Locale defaultLocale = Locale.getDefault();
        try {
            // Set Turkish locale to expose the bug
            Locale.setDefault(new Locale("tr", "TR"));
            
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            map.put("en", "value");
            
            // The key "en" should be retrievable regardless of locale
            assertEquals("value", map.get("en"));
            assertEquals("value", map.get("EN"));
            assertEquals("value", map.get("En"));
            
            // Also test with Turkish-specific characters
            map.put("Istanbul", "city");
            assertEquals("city", map.get("istanbul"));
            assertEquals("city", map.get("İstanbul")); // Turkish dotted I
            
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test(timeout = 4000)
    public void testLocaleIndependenceGermanSharpS() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("de", "DE"));
            
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            map.put("straße", "street");
            
            // German ß is converted to "ss" in some locales, but toLowerCase() should be consistent
            assertEquals("street", map.get("STRASSE"));
            assertEquals("street", map.get("Strasse"));
            assertEquals("street", map.get("straße"));
            
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test(timeout = 4000)
    public void testLocaleIndependenceGreekSigma() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("el", "GR"));
            
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            map.put("ΟΔΟΣ", "street"); // Greek uppercase
            
            // Greek sigma has different forms (ς vs σ) but toLowerCase() should handle it
            assertEquals("street", map.get("οδός"));
            assertEquals("street", map.get("ΟΔΟΣ"));
            
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeCapacity() {
        new CaseInsensitiveMap(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroCapacity() {
        new CaseInsensitiveMap(0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeLoadFactor() {
        new CaseInsensitiveMap(10, -0.5f);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullMap() {
        new CaseInsensitiveMap(null);
    }

    @Test(timeout = 4000)
    public void testPutNullValue() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("key", null);
        assertNull(map.get("KEY"));
        assertTrue(map.containsKey("key"));
    }

    @Test(timeout = 4000)
    public void testGetNonExistentKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertNull(map.get("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRemoveNonExistentKey() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertNull(map.remove("nonexistent"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testClone() {
        CaseInsensitiveMap original = new CaseInsensitiveMap();
        original.put("Key1", "value1");
        original.put("Key2", "value2");
        
        CaseInsensitiveMap cloned = (CaseInsensitiveMap) original.clone();
        assertEquals(original.size(), cloned.size());
        assertEquals("value1", cloned.get("key1"));
        assertEquals("value2", cloned.get("KEY2"));
        
        // Verify independence
        cloned.put("Key3", "value3");
        assertEquals(2, original.size());
        assertEquals(3, cloned.size());
    }

    @Test(timeout = 4000)
    public void testEquals() {
        CaseInsensitiveMap map1 = new CaseInsensitiveMap();
        map1.put("Key", "value");
        
        CaseInsensitiveMap map2 = new CaseInsensitiveMap();
        map2.put("KEY", "value");
        
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullKey() {
        CaseInsensitiveMap map1 = new CaseInsensitiveMap();
        map1.put(null, "value");
        
        CaseInsensitiveMap map2 = new CaseInsensitiveMap();
        map2.put(null, "value");
        
        assertEquals(map1, map2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("key", "value");
        String str = map.toString();
        assertTrue(str.contains("key"));
        assertTrue(str.contains("value"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        CaseInsensitiveMap original = new CaseInsensitiveMap();
        original.put("Key1", "value1");
        original.put("Key2", "value2");
        original.put(null, "nullValue");
        
        // Serialize
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        CaseInsensitiveMap deserialized = (CaseInsensitiveMap) ois.readObject();
        ois.close();
        
        assertEquals(original.size(), deserialized.size());
        assertEquals("value1", deserialized.get("key1"));
        assertEquals("value2", deserialized.get("KEY2"));
        assertEquals("nullValue", deserialized.get(null));
    }
}