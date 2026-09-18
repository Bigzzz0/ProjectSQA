package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: org.apache.commons.collections.map.CaseInsensitiveMap
 *
 * Branch & Condition Analysis:
 * 1. Constructor Branching:
 *    - Default constructor () -> default capacity, load factor, threshold.
 *    - Single int constructor (capacity) -> capacity >= 1 (normal) vs < 1 (IllegalArgumentException).
 *    - Int & float constructor (capacity, loadFactor) -> loadFactor <= 0 (IllegalArgumentException).
 *    - Map constructor (Map map) -> map != null (normal copy & key conversion) vs null (NullPointerException).
 * 2. convertKey(Object key) Branching:
 *    - key == null -> returns AbstractHashedMap.NULL.
 *    - key != null -> converts key.toString() to lower-case representation.
 * 3. Lifecycle, Cloning, & Serialization:
 *    - clone() -> shallow copy, verified independent bucket/entry structure.
 *    - writeObject / readObject -> round-trip object serialization preserving entries and lookup.
 * 4. Known Ground-Truth Defect (Locale Independence):
 *    - Specification: "converted to all lowercase in a locale-independent fashion".
 *    - Defect in defective implementation: key.toString().toLowerCase() depends on Locale.getDefault().
 *    - In locales with special case mapping (e.g. Turkish 'I' -> '\u0131' instead of 'i'), changing locale
 *      or inter-casing fails to retrieve or retain entries across locales (e.g. "I" and "i" mismatch).
 * =========================================================================
 */
public class CaseInsensitiveMapGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicPutAndGetCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("One", "ValueOne");
        map.put("two", "ValueTwo");
        map.put("THREE", "ValueThree");

        assertEquals(3, map.size());
        assertEquals("ValueOne", map.get("one"));
        assertEquals("ValueOne", map.get("ONE"));
        assertEquals("ValueOne", map.get("One"));
        assertEquals("ValueTwo", map.get("TWO"));
        assertEquals("ValueThree", map.get("three"));
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsKey("ONE"));
        assertTrue(map.containsKey("three"));
        assertFalse(map.containsKey("four"));
    }

    @Test(timeout = 4000)
    public void testPutOverwriteWithDifferentCase() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Object old1 = map.put("key", "val1");
        assertNull(old1);
        assertEquals(1, map.size());

        Object old2 = map.put("KEY", "val2");
        assertEquals("val1", old2);
        assertEquals(1, map.size());
        assertEquals("val2", map.get("key"));
        assertEquals("val2", map.get("kEy"));
    }

    @Test(timeout = 4000)
    public void testRemoveCaseInsensitive() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("TargetKey", "found");

        assertTrue(map.containsKey("targetkey"));
        Object removed = map.remove("TARGETKEY");
        assertEquals("found", removed);
        assertEquals(0, map.size());
        assertNull(map.get("targetkey"));
        assertNull(map.remove("targetkey"));
    }

    @Test(timeout = 4000)
    public void testKeySetContainsConvertedKeys() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("Alpha", "A");
        map.put("BETA", "B");

        Set keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("alpha"));
        assertTrue(keys.contains("beta"));
        assertFalse(keys.contains("Alpha"));
        assertFalse(keys.contains("BETA"));
    }

    @Test(timeout = 4000)
    public void testNonStringKeysToStringConversion() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        Integer numKey = new Integer(12345);
        map.put(numKey, "NumberValue");

        assertEquals("NumberValue", map.get(numKey));
        assertEquals("NumberValue", map.get("12345"));
        assertTrue(map.containsKey("12345"));
        assertTrue(map.containsKey(numKey));

        Object customObj = new Object() {
            public String toString() {
                return "CustomOBJECT";
            }
        };
        map.put(customObj, "ObjValue");
        assertEquals("ObjValue", map.get("customobject"));
        assertEquals("ObjValue", map.get("CUSTOMOBJECT"));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullKeySupport() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertNull(map.get(null));
        assertFalse(map.containsKey(null));

        map.put(null, "NullValue");
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("NullValue", map.get(null));

        Set keys = map.keySet();
        assertTrue(keys.contains(null));

        Object removed = map.remove(null);
        assertEquals("NullValue", removed);
        assertEquals(0, map.size());
        assertFalse(map.containsKey(null));
    }

    @Test(timeout = 4000)
    public void testEmptyKeyString() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("", "EmptyStringVal");

        assertEquals(1, map.size());
        assertTrue(map.containsKey(""));
        assertEquals("EmptyStringVal", map.get(""));
    }

    @Test(timeout = 4000)
    public void testCopyConstructorWithExistingMap() {
        Map source = new HashMap();
        source.put("ItemA", "1");
        source.put("ITEMA", "2"); // Overlaps in case-insensitive world
        source.put("ItemB", "3");
        source.put(null, "4");

        CaseInsensitiveMap map = new CaseInsensitiveMap(source);
        assertEquals(3, map.size()); // "itema", "itemb", and null
        assertTrue(map.containsKey("itema"));
        assertTrue(map.containsKey("itemb"));
        assertTrue(map.containsKey(null));
        assertEquals("4", map.get(null));
        assertEquals("3", map.get("ITEMB"));
    }

    @Test(timeout = 4000)
    public void testValidCapacities() {
        CaseInsensitiveMap map1 = new CaseInsensitiveMap(1);
        map1.put("k", "v");
        assertEquals(1, map1.size());

        CaseInsensitiveMap map2 = new CaseInsensitiveMap(16, 0.5f);
        map2.put("k", "v");
        assertEquals(1, map2.size());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets the defect where convertKey() relies on Locale.getDefault().
     * In Turkish locale, capital 'I' converts to dotless '\u0131' instead of 'i'.
     * A case-insensitive map must be locale-independent as specified in its contract.
     */
    @Test(timeout = 4000)
    public void testLocaleIndependence() {
        Locale orig = Locale.getDefault();
        try {
            Locale[] locales = new Locale[] {
                new Locale("es", "", ""),
                new Locale("tr", "", ""),
                Locale.ENGLISH,
                Locale.getDefault()
            };

            for (int i = 0; i < locales.length; i++) {
                Locale.setDefault(locales[i]);
                Map map = new CaseInsensitiveMap();
                map.put("I", "value");

                // In Turkish, "I".toLowerCase() is '\u0131' and "i".toLowerCase() is 'i'.
                // If locale-sensitive, map.get("i") returns null, breaking case-insensitivity.
                assertEquals(locales[i] + ": 1", "value", map.get("I"));
                assertEquals(locales[i] + ": 2", "value", map.get("i"));
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

    @Test(timeout = 4000)
    public void testCrossLocaleKeyLookup() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "", ""));
            Map map = new CaseInsensitiveMap();
            map.put("I", "value");

            Locale.setDefault(Locale.ENGLISH);
            assertEquals("en: 1", "value", map.get("I"));
            assertEquals("en: 2", "value", map.get("i"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorZeroCapacity() {
        new CaseInsensitiveMap(0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNegativeCapacity() {
        new CaseInsensitiveMap(-10);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorZeroCapacityWithLoadFactor() {
        new CaseInsensitiveMap(0, 0.75f);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNegativeLoadFactor() {
        new CaseInsensitiveMap(16, -0.5f);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorZeroLoadFactor() {
        new CaseInsensitiveMap(16, 0.0f);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new CaseInsensitiveMap((Map) null);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCloneMethodIntegrity() {
        CaseInsensitiveMap original = new CaseInsensitiveMap();
        original.put("Alpha", "1");
        original.put("Beta", "2");

        CaseInsensitiveMap clone = (CaseInsensitiveMap) original.clone();
        assertNotNull(clone);
        assertNotSame(original, clone);
        assertEquals(original.size(), clone.size());
        assertEquals("1", clone.get("ALPHA"));
        assertEquals("2", clone.get("beta"));

        // Modifying clone does not mutate original
        clone.put("Gamma", "3");
        assertEquals(3, clone.size());
        assertEquals(2, original.size());
        assertFalse(original.containsKey("gamma"));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        CaseInsensitiveMap original = new CaseInsensitiveMap();
        original.put("Hello", "World");
        original.put("FOO", "BAR");
        original.put(null, "NULL_VALUE");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof CaseInsensitiveMap);
        CaseInsensitiveMap copy = (CaseInsensitiveMap) deserialized;
        assertEquals(3, copy.size());
        assertEquals("World", copy.get("hello"));
        assertEquals("BAR", copy.get("foo"));
        assertEquals("NULL_VALUE", copy.get(null));
        assertTrue(copy.containsKey("HELLO"));
        assertTrue(copy.containsKey(null));
    }

    @Test(timeout = 4000)
    public void testConvertKeyDirectCall() {
        // Subclass to directly verify protected convertKey method
        CaseInsensitiveMap map = new CaseInsensitiveMap() {
            public Object exposeConvertKey(Object k) {
                return convertKey(k);
            }
        };

        assertEquals(AbstractHashedMap.NULL, map.exposeConvertKey(null));
        assertEquals("abc", map.exposeConvertKey("ABC"));
        assertEquals("abc", map.exposeConvertKey("abc"));
    }
}