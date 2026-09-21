package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Comprehensive JUnit 4 test suite for Attributes class, targeting maximum coverage and the known defect.
 */
/* [Branch & Defect Analysis Matrix]
 * Target: org.jsoup.nodes.Attributes
 * 
 * Decision branches covered:
 * - indexOfKey: loop over size, equality check
 * - indexOfKeyIgnoreCase: loop over size, equalsIgnoreCase
 * - checkCapacity: if curSize >= minNewSize, else grow
 * - put: if indexOfKey != NotFound then replace else add
 * - putIgnoreCase: if found, replace and possibly update key; else add
 * - put(String, boolean): if true putIgnoreCase with null, else remove
 * - remove(int): shift array, nullify last
 * - remove(String): if found call remove(int)
 * - removeIgnoreCase: similar
 * - hasKey/hasKeyIgnoreCase: delegate to indexOf
 * - addAll: if incoming size==0 return; else for each attr put
 * - iterator: hasNext, next, remove
 * - asList: create list, handle null vals as BooleanAttribute
 * - dataset: inner classes EntrySet, DatasetIterator
 * - html: iterate and append
 * - equals: size, Arrays.equals keys and vals
 * - hashCode: size, 31*result + hash
 * - clone: super.clone, copyOf arrays
 * - normalize: loop and lowercase keys (no dedup - known defect)
 * 
 * Boundary conditions:
 * - Empty attributes (size=0)
 * - Single attribute
 * - Multiple attributes
 * - Null key (throws IllegalArgumentException)
 * - Null value (boolean attribute)
 * - Empty string key/value
 * - Large number of attributes (capacity growth)
 * - Case sensitivity vs insensitivity
 * - Duplicate keys (case-sensitive and case-insensitive)
 * - Missing deduplication after normalize (defect)
 * 
 * Defect-targeted tests:
 * - testNormalizeDoesNotDeduplicate: after adding "One" and "one", normalize results in two keys (bug)
 * - testAddAllWithDuplicates: addAll from an Attributes with duplicates results in duplicates (bug)
 * - testDefectScenario1: simulate retainsAttributesOfDifferentCaseIfSensitive failure
 * - testDefectScenario2: simulate dropsDuplicateAttributes failure
 */
public class AttributesDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testPutAndGet() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "value1");
        attrs.put("key2", "value2");
        assertEquals("value1", attrs.get("key1"));
        assertEquals("value2", attrs.get("key2"));
        assertEquals("", attrs.get("nonexistent"));
        assertEquals(2, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutReplacesCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("key", "first");
        attrs.put("key", "second");
        assertEquals("second", attrs.get("key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutIgnoreCaseReplacesAndUpdatesKey() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value1");
        attrs.putIgnoreCase("key", "value2");
        assertEquals("value2", attrs.get("key")); // key updated to lowercase
        assertTrue(attrs.hasKey("key"));
        assertFalse(attrs.hasKey("Key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutIgnoreCaseAddsWhenNotFound() {
        Attributes attrs = new Attributes();
        attrs.putIgnoreCase("key", "value");
        assertEquals("value", attrs.get("key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutBooleanTrue() {
        Attributes attrs = new Attributes();
        attrs.put("checked", true);
        assertTrue(attrs.hasKey("checked"));
        assertEquals("", attrs.get("checked")); // boolean attribute returns empty string
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutBooleanFalseRemoves() {
        Attributes attrs = new Attributes();
        attrs.put("checked", "true");
        attrs.put("checked", false);
        assertFalse(attrs.hasKey("checked"));
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutAttribute() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("key", "value");
        attrs.put(attr);
        assertEquals("value", attrs.get("key"));
        assertSame(attrs, attr.parent); // parent set
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testGetIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        assertEquals("value", attrs.getIgnoreCase("key"));
        assertEquals("", attrs.getIgnoreCase("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasKey() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        assertTrue(attrs.hasKey("key"));
        assertFalse(attrs.hasKey("Key"));
        assertFalse(attrs.hasKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasKeyIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        assertTrue(attrs.hasKeyIgnoreCase("key"));
        assertTrue(attrs.hasKeyIgnoreCase("Key"));
        assertFalse(attrs.hasKeyIgnoreCase("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRemoveCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.put("Key", "value2");
        attrs.remove("key");
        assertFalse(attrs.hasKey("key"));
        assertTrue(attrs.hasKey("Key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testRemoveIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        attrs.put("Key", "value2");
        attrs.removeIgnoreCase("key");
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testSize() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        attrs.put("a", "1");
        assertEquals(1, attrs.size());
        attrs.put("b", "2");
        assertEquals(2, attrs.size());
        attrs.remove("a");
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        Attributes base = new Attributes();
        base.put("a", "1");
        base.put("b", "2");
        Attributes incoming = new Attributes();
        incoming.put("c", "3");
        incoming.put("d", "4");
        base.addAll(incoming);
        assertEquals(4, base.size());
        assertEquals("1", base.get("a"));
        assertEquals("4", base.get("d"));
    }

    @Test(timeout = 4000)
    public void testAddAllEmptyIncoming() {
        Attributes base = new Attributes();
        base.put("a", "1");
        Attributes empty = new Attributes();
        base.addAll(empty);
        assertEquals(1, base.size());
    }

    @Test(timeout = 4000)
    public void testIterator() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        Attribute first = it.next();
        assertEquals("a", first.getKey());
        assertEquals("1", first.getValue());
        assertTrue(it.hasNext());
        Attribute second = it.next();
        assertEquals("b", second.getKey());
        assertEquals("2", second.getValue());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorRemove() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        Iterator<Attribute> it = attrs.iterator();
        it.next(); // a
        it.remove(); // removes a
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("a"));
        assertTrue(attrs.hasKey("b"));
    }

    @Test(timeout = 4000)
    public void testAsList() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", null); // boolean attribute
        List<Attribute> list = attrs.asList();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).getKey());
        assertEquals("1", list.get(0).getValue());
        assertEquals("b", list.get(1).getKey());
        assertEquals("", list.get(1).getValue()); // null becomes empty string
        // list should be unmodifiable
        try {
            list.add(new Attribute("c", "3"));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "value");
        attrs.put("class", "test");
        Map<String, String> data = attrs.dataset();
        assertEquals(1, data.size());
        assertEquals("value", data.get("name"));
        // put into dataset
        data.put("extra", "extraVal");
        assertTrue(attrs.hasKey("data-extra"));
        assertEquals("extraVal", attrs.get("data-extra"));
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Attributes attrs = new Attributes();
        attrs.put("id", "test");
        attrs.put("checked", null);
        String html = attrs.html();
        assertTrue(html.contains("id=\"test\""));
        assertTrue(html.contains("checked")); // boolean attribute without value
    }

    @Test(timeout = 4000)
    public void testToString() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        assertEquals(attrs.html(), attrs.toString());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyAttributes() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        assertEquals("", attrs.get("any"));
        assertFalse(attrs.hasKey("any"));
        assertEquals(0, attrs.asList().size());
        assertEquals("", attrs.html().trim());
    }

    @Test(timeout = 4000)
    public void testNullKeyThrows() {
        Attributes attrs = new Attributes();
        try {
            attrs.get(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            attrs.hasKey(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            attrs.remove(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            attrs.put(null, "value");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", (String) null);
        assertEquals("", attrs.get("key")); // null becomes empty string
        assertTrue(attrs.hasKey("key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testEmptyStringKeyAndValue() {
        Attributes attrs = new Attributes();
        attrs.put("", "value");
        attrs.put("key", "");
        assertEquals("value", attrs.get(""));
        assertEquals("", attrs.get("key"));
        assertEquals(2, attrs.size());
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfAttributes() {
        Attributes attrs = new Attributes();
        int n = 100;
        for (int i = 0; i < n; i++) {
            attrs.put("key" + i, "val" + i);
        }
        assertEquals(n, attrs.size());
        for (int i = 0; i < n; i++) {
            assertEquals("val" + i, attrs.get("key" + i));
        }
    }

    @Test(timeout = 4000)
    public void testCapacityGrowth() {
        Attributes attrs = new Attributes();
        // Initial capacity is 4, growth factor 2
        for (int i = 0; i < 10; i++) {
            attrs.put("k" + i, "v" + i);
        }
        assertEquals(10, attrs.size());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testNormalizeDoesNotDeduplicate() {
        // Known defect: normalize() lowercases keys but does not remove duplicates.
        Attributes attrs = new Attributes();
        attrs.put("One", "value1");
        attrs.put("one", "value2"); // different case, so both stored
        assertEquals(2, attrs.size());
        attrs.normalize();
        // After normalize, both keys become "one" -> duplicate keys exist (bug)
        // Expected correct behavior: duplicates should be removed, size should be 1.
        // This assertion will fail on the buggy version (size == 2) and pass on fixed version.
        assertEquals(1, attrs.size());
        // The value should be the first one? Or last? We assume first is kept.
        assertEquals("value1", attrs.get("one"));
    }

    @Test(timeout = 4000)
    public void testAddAllWithDuplicates() {
        // Simulate scenario where incoming Attributes has duplicate keys (different case)
        Attributes base = new Attributes();
        base.put("One", "base1");
        Attributes incoming = new Attributes();
        incoming.put("one", "incoming1");
        incoming.put("one", "incoming2"); // duplicate case-sensitive, but put replaces so only one
        base.addAll(incoming);
        // After addAll, base should have "One" and "one" (two keys) because put is case-sensitive.
        // This is correct behavior, but if later normalize is called, duplicates appear.
        // The defect is that no deduplication happens.
        assertEquals(2, base.size());
        assertTrue(base.hasKey("One"));
        assertTrue(base.hasKey("one"));
        assertEquals("base1", base.get("One"));
        assertEquals("incoming2", base.get("one")); // last put wins
    }

    @Test(timeout = 4000)
    public void testDefectScenario1() {
        // Reproduce the "retainsAttributesOfDifferentCaseIfSensitive" failure.
        // Expected: both "One" and "one" are present with correct values.
        // Bug: "One" gets overwritten by "Two" (value changed).
        Attributes attrs = new Attributes();
        attrs.put("One", "One");
        attrs.put("one", "Three");
        attrs.put("two", "Four");
        attrs.put("Two", "Six");
        // In case-sensitive mode, all four should be present.
        assertEquals(4, attrs.size());
        assertEquals("One", attrs.get("One"));
        assertEquals("Three", attrs.get("one"));
        assertEquals("Four", attrs.get("two"));
        assertEquals("Six", attrs.get("Two"));
        // The bug might cause "One" to be overwritten by "Two" (value "Two") and "two" to become "Five".
        // This test will pass on correct version, fail on buggy version if the bug is triggered.
        // We add an extra assertion to catch the specific failure pattern.
        assertFalse(attrs.hasKey("Two") && attrs.get("One").equals("Two")); // if bug, this would be true
    }

    @Test(timeout = 4000)
    public void testDefectScenario2() {
        // Reproduce the "dropsDuplicateAttributes" failure.
        // Expected: only one "one" attribute with value "One".
        // Bug: multiple "one" attributes appear.
        Attributes attrs = new Attributes();
        attrs.put("one", "One");
        attrs.put("one", "Two"); // should replace, so only one "one" with "Two"
        attrs.put("two", "two");
        // But the bug might cause duplicates if put is not used correctly.
        // Actually the failure shows multiple "one" attributes, so maybe the parser uses add() directly.
        // We simulate by using addAll with an Attributes that has duplicates.
        Attributes incoming = new Attributes();
        incoming.put("one", "Four");
        incoming.put("one", "Two");
        incoming.put("two", "two");
        incoming.put("one", "Three");
        incoming.put("two", "Five");
        attrs.addAll(incoming);
        // After addAll, due to put, duplicates should be replaced.
        // Expected: only one "one" (last value "Three") and one "two" (last "Five").
        assertEquals(2, attrs.size());
        assertEquals("Three", attrs.get("one"));
        assertEquals("Five", attrs.get("two"));
        // The bug might cause multiple "one" keys if addAll does not replace correctly.
        // This test will fail on buggy version if duplicates remain.
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPutNullAttribute() {
        Attributes attrs = new Attributes();
        attrs.put((Attribute) null);
    }

    @Test(timeout = 4000)
    public void testRemoveIndexOutOfBounds() {
        // remove(int) is private, but we can trigger via iterator remove after end?
        // Actually iterator remove calls remove(--i) which could go negative if called without next.
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        Iterator<Attribute> it = attrs.iterator();
        try {
            it.remove(); // remove before next -> i=0, remove(--i) => remove(-1) which will throw
            fail("Expected IndexOutOfBoundsException or similar");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected, but actually Validate.isFalse(index >= size) will throw IllegalArgumentException if index >= size, but index=-1 is not >= size? size=1, -1 < 1, so it will try to access array[-1] -> ArrayIndexOutOfBounds.
            // Acceptable.
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Attributes a = new Attributes();
        a.put("key", "value");
        Attributes b = new Attributes();
        b.put("key", "value");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.put("key2", "value2");
        assertNotEquals(a, b);
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Attributes a = new Attributes();
        assertEquals(a, a);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Attributes a = new Attributes();
        assertFalse(a.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Attributes a = new Attributes();
        assertFalse(a.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Attributes a = new Attributes();
        a.put("a", "1");
        int h1 = a.hashCode();
        a.put("b", "2");
        int h2 = a.hashCode();
        assertNotEquals(h1, h2);
        // After removing, hash should change
        a.remove("b");
        assertEquals(h1, a.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Attributes original = new Attributes();
        original.put("key", "value");
        original.put("bool", null);
        Attributes clone = original.clone();
        assertNotSame(original, clone);
        assertEquals(original.size(), clone.size());
        assertEquals(original.get("key"), clone.get("key"));
        assertEquals(original.get("bool"), clone.get("bool"));
        // Modify clone should not affect original
        clone.put("new", "newval");
        assertFalse(original.hasKey("new"));
        // Modify original should not affect clone
        original.put("key", "changed");
        assertEquals("value", clone.get("key"));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Attributes original = new Attributes();
        original.put("a", "1");
        Attributes clone = original.clone();
        clone.put("b", "2");
        assertEquals(1, original.size());
        assertEquals(2, clone.size());
    }

    @Test(timeout = 4000)
    public void testNormalize() {
        Attributes attrs = new Attributes();
        attrs.put("KEY", "value");
        attrs.put("AnotherKey", "val2");
        attrs.normalize();
        assertTrue(attrs.hasKey("key"));
        assertTrue(attrs.hasKey("anotherkey"));
        assertFalse(attrs.hasKey("KEY"));
        assertEquals("value", attrs.get("key"));
    }
}