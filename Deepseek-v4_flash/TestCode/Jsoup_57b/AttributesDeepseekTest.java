package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Attributes.java from Defects4J
 * Known Defect: java.util.ConcurrentModificationException when iterating and removing attributes
 *   - Chain: removeIgnoreCase() uses iterator() in for-each but calls attributes.remove() directly,
 *     causing ConcurrentModificationException when called from within an iteration over the same map
 *   - Affected branches: removeIgnoreCase() loop, iterator() usage in chained scenarios
 * 
 * Key Decision Branches/Paths Targeted:
 * 1. attributes == null guards in get(), getIgnoreCase(), remove(), removeIgnoreCase(), hasKey(), 
 *    hasKeyIgnoreCase(), size(), addAll(), iterator(), asList(), html(), equals(), hashCode(), clone()
 * 2. Non-null attributes path with single/multiple entries
 * 3. BVA for null/empty/whitespace keys: Validate.notEmpty(key) throws IllegalArgumentException
 * 4. Boolean attribute put with true/false values
 * 5. Case-sensitive vs case-insensitive operations
 * 6. dataset() with data- attributes creation and iteration
 * 7. equals/hashCode with null and non-null attributes
 * 8. clone() deep copy verification
 * 9. Edge cases: empty attributes, repeated put, remove non-existent key
 * 
 * Targeted Defect Reproduction:
 * - Chained operations that iterate and remove: removeIgnoreCase used in loop over iterator
 * - The bug occurs when removeIgnoreCase is called on an Attributes that is being iterated externally,
 *   causing concurrent modification via direct attributes.remove() inside iterator loop
 */

public class AttributesDeepseekTest {

    /* ======== Partition A: Core Functional Logic & State Transitions ======== */

    @Test(timeout = 4000)
    public void testPutAndGetString() {
        Attributes attrs = new Attributes();
        assertEquals("", attrs.get("key1"));
        attrs.put("key1", "value1");
        assertEquals("value1", attrs.get("key1"));
        attrs.put("key1", "value2");
        assertEquals("value2", attrs.get("key1"));
    }

    @Test(timeout = 4000)
    public void testPutBooleanAttributeTrue() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        assertTrue(attrs.hasKey("disabled"));
        assertEquals("", attrs.get("disabled")); // BooleanAttribute has empty value
    }

    @Test(timeout = 4000)
    public void testPutBooleanAttributeFalseRemoves() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        assertTrue(attrs.hasKey("disabled"));
        attrs.put("disabled", false);
        assertFalse(attrs.hasKey("disabled"));
    }

    @Test(timeout = 4000)
    public void testPutAttributeObject() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("class", "main");
        attrs.put(attr);
        assertEquals("main", attrs.get("class"));
        assertTrue(attrs.hasKey("class"));
    }

    @Test(timeout = 4000)
    public void testRemoveCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value1");
        attrs.put("key", "value2");
        attrs.remove("Key");
        assertFalse(attrs.hasKey("Key"));
        assertTrue(attrs.hasKey("key"));
        assertEquals("value2", attrs.get("key"));
    }

    @Test(timeout = 4000)
    public void testRemoveIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("KEY", "value1");
        attrs.put("key", "value2");
        attrs.removeIgnoreCase("key");
        assertFalse(attrs.hasKey("KEY"));
        assertFalse(attrs.hasKey("key"));
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testHasKeyCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("id", "123");
        assertTrue(attrs.hasKey("id"));
        assertFalse(attrs.hasKey("ID"));
        assertFalse(attrs.hasKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasKeyIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("id", "123");
        assertTrue(attrs.hasKeyIgnoreCase("ID"));
        assertTrue(attrs.hasKeyIgnoreCase("id"));
        assertFalse(attrs.hasKeyIgnoreCase("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testSize() {
        Attributes empty = new Attributes();
        assertEquals(0, empty.size());
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        assertEquals(1, attrs.size());
        attrs.put("b", "2");
        assertEquals(2, attrs.size());
        attrs.remove("a");
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testIterator() {
        Attributes attrs = new Attributes();
        assertFalse(attrs.iterator().hasNext());
        attrs.put("first", "1");
        attrs.put("second", "2");
        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        assertEquals("first", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("second", it.next().getKey());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testAsList() {
        Attributes empty = new Attributes();
        assertTrue(empty.asList().isEmpty());
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        assertEquals(2, attrs.asList().size());
        assertEquals("1", attrs.asList().get(0).getValue());
        assertThrows(UnsupportedOperationException.class, () -> attrs.asList().add(new Attribute("x", "y")));
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        Attributes source = new Attributes();
        source.put("a", "1");
        source.put("b", "2");
        Attributes target = new Attributes();
        target.addAll(source);
        assertEquals(2, target.size());
        assertEquals("1", target.get("a"));
        assertEquals("2", target.get("b"));
        target.addAll(new Attributes()); // empty should do nothing
        assertEquals(2, target.size());
    }

    @Test(timeout = 4000)
    public void testHtmlRepresentation() {
        Attributes attrs = new Attributes();
        assertEquals("", attrs.html());
        attrs.put("class", "main");
        String html = attrs.html();
        assertTrue(html.contains("class=\"main\""));
        assertTrue(html.startsWith(" "));
    }

    @Test(timeout = 4000)
    public void testToString() {
        Attributes attrs = new Attributes();
        attrs.put("id", "x");
        assertEquals(attrs.html(), attrs.toString());
    }

    /* ======== Partition B: Boundary Value Analysis (BVA) & Extremes ======== */

    @Test(timeout = 4000)
    public void testGetWithEmptyKeyThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.get(""));
        assertThrows(IllegalArgumentException.class, () -> attrs.get(null));
    }

    @Test(timeout = 4000)
    public void testPutWithNullKeyThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.put((String)null, "value"));
    }

    @Test(timeout = 4000)
    public void testPutWithNullAttributeThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.put((Attribute)null));
    }

    @Test(timeout = 4000)
    public void testRemoveWithEmptyKeyThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.remove(""));
        assertThrows(IllegalArgumentException.class, () -> attrs.remove(null));
    }

    @Test(timeout = 4000)
    public void testRemoveIgnoreCaseWithEmptyKeyThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.removeIgnoreCase(""));
        assertThrows(IllegalArgumentException.class, () -> attrs.removeIgnoreCase(null));
    }

    @Test(timeout = 4000)
    public void testGetIgnoreCaseWithEmptyKeyThrows() {
        Attributes attrs = new Attributes();
        assertThrows(IllegalArgumentException.class, () -> attrs.getIgnoreCase(""));
        assertThrows(IllegalArgumentException.class, () -> attrs.getIgnoreCase(null));
    }

    @Test(timeout = 4000)
    public void testGetNonexistentKeyReturnsEmptyString() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        assertEquals("", attrs.get("b"));
    }

    @Test(timeout = 4000)
    public void testDatasetCreatesAttributesIfNull() {
        Attributes empty = new Attributes();
        Map<String, String> ds = empty.dataset();
        assertTrue(ds.isEmpty());
        assertEquals(0, empty.size()); // dataset constructor created map but no entries
        ds.put("custom", "value");
        assertTrue(empty.hasKey("data-custom"));
        assertEquals("value", empty.get("data-custom"));
    }

    /* ======== Partition C: Defect-Targeted Branch Zone ======== */

    /**
     * Directly targets the known ConcurrentModificationException defect in Defects4J.
     * Scenario: iterating over attributes and calling removeIgnoreCase() on the same Attributes object
     * triggers CME because removeIgnoreCase() uses iterator() internally but then calls 
     * attributes.remove() directly within the loop, modifying the map while iterating.
     * 
     * The test expects the operation to complete without throwing CME (which would be the fixed behavior)
     * or we assert the correct final state. If the bug is present, test may throw CME or fail assertion.
     */
    @Test(timeout = 4000)
    public void testChainedRemoveAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("A", "1");
        attrs.put("B", "2");
        attrs.put("C", "3");
        
        // Simulate chained removal pattern that triggered CME in ElementTest
        // This iterates over the keySet via iterator in removeIgnoreCase while modifying
        for (Attribute attr : attrs) {
            attrs.removeIgnoreCase(attr.getKey());
        }
        // If no CME, we expect all attributes removed
        assertEquals(0, attrs.size());
    }

    /**
     * Alternative scenario: using iterator and calling removeIgnoreCase on same object from loop.
     * This variant uses explicit iterator to trigger the bug more directly.
     */
    @Test(timeout = 4000)
    public void testRemoveWhileIteratingExternally() {
        Attributes attrs = new Attributes();
        attrs.put("x", "1");
        attrs.put("y", "2");
        attrs.put("z", "3");
        
        Iterator<Attribute> it = attrs.iterator();
        while (it.hasNext()) {
            Attribute a = it.next();
            if (a.getKey().equals("y")) {
                attrs.removeIgnoreCase("y"); // This should cause CME due to structural modification
            }
        }
        // If no exception thrown, verify remaining attributes
        assertTrue(attrs.hasKey("x"));
        assertFalse(attrs.hasKey("y"));
        assertTrue(attrs.hasKey("z"));
    }

    /* ======== Partition D: Exception & Defensive Guard Paths ======== */

    @Test(timeout = 4000)
    public void testGetFromEmptyAttributes() {
        Attributes empty = new Attributes();
        assertEquals("", empty.get("anything"));
        assertEquals("", empty.getIgnoreCase("anything"));
    }

    @Test(timeout = 4000)
    public void testRemoveFromEmptyAttributes() {
        Attributes empty = new Attributes();
        empty.remove("key"); // should not throw
        empty.removeIgnoreCase("key"); // should not throw
    }

    @Test(timeout = 4000)
    public void testHasKeyOnEmpty() {
        Attributes empty = new Attributes();
        assertFalse(empty.hasKey("anything"));
        assertFalse(empty.hasKeyIgnoreCase("anything"));
    }

    @Test(timeout = 4000)
    public void testIteratorOnEmpty() {
        Attributes empty = new Attributes();
        assertFalse(empty.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testHtmlOnEmpty() {
        Attributes empty = new Attributes();
        assertEquals("", empty.html());
    }

    /* ======== Partition E: Object Lifecycle & Contract Integrity ======== */

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        Attributes attrs = new Attributes();
        assertTrue(attrs.equals(attrs));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualContent() {
        Attributes a = new Attributes();
        a.put("key", "val");
        Attributes b = new Attributes();
        b.put("key", "val");
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentContent() {
        Attributes a = new Attributes();
        a.put("key1", "val1");
        Attributes b = new Attributes();
        b.put("key2", "val2");
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullAttributes() {
        Attributes empty1 = new Attributes();
        Attributes empty2 = new Attributes();
        assertTrue(empty1.equals(empty2));
        assertEquals(empty1.hashCode(), empty2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistentWithEquals() {
        Attributes a = new Attributes();
        a.put("a", "1");
        a.put("b", "2");
        Attributes b = new Attributes();
        b.put("b", "2");
        b.put("a", "1"); // Different insertion order but same content
        // LinkedHashMap.equals() compares by entry order, so these are NOT equal
        assertFalse(a.equals(b));
        // hashCodes may be same or different depending on LinkedHashMap implementation
    }

    @Test(timeout = 4000)
    public void testCloneEmpty() {
        Attributes empty = new Attributes();
        Attributes cloned = empty.clone();
        assertTrue(empty.equals(cloned));
        assertEquals(0, cloned.size());
        assertNotSame(empty, cloned);
    }

    @Test(timeout = 4000)
    public void testCloneDeepCopy() {
        Attributes original = new Attributes();
        original.put("class", "main");
        original.put("id", "test");
        Attributes cloned = original.clone();
        assertEquals(original, cloned);
        // Modify original to ensure deep copy
        original.put("class", "changed");
        assertEquals("main", cloned.get("class"));
        assertFalse(original.equals(cloned));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Attributes original = new Attributes();
        original.put("a", "1");
        Attributes cloned = original.clone();
        cloned.put("b", "2");
        assertEquals(1, original.size());
        assertEquals(2, cloned.size());
    }

    @Test(timeout = 4000)
    public void testDatasetEntrySet() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "John");
        attrs.put("class", "user");
        Map<String, String> ds = attrs.dataset();
        assertEquals(1, ds.size());
        assertEquals("John", ds.get("name"));
        Set<Map.Entry<String,String>> entries = ds.entrySet();
        assertEquals(1, entries.size());
    }

    @Test(timeout = 4000)
    public void testDatasetIteratorRemove() {
        Attributes attrs = new Attributes();
        attrs.put("data-x", "1");
        attrs.put("data-y", "2");
        Map<String, String> ds = attrs.dataset();
        Iterator<Map.Entry<String,String>> it = ds.entrySet().iterator();
        it.next();
        it.remove();
        assertEquals(1, ds.size());
        assertFalse(attrs.hasKey("data-x"));
        assertTrue(attrs.hasKey("data-y"));
    }
}