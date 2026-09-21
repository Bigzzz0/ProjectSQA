package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Attributes class.
 * Targets line/branch coverage and the known defect in boolean attribute HTML output.
 *
 * [Branch & Defect Analysis Matrix]
 * - Branch: html() method condition for collapsing boolean attributes:
 *   `if (!(out.syntax() == Document.OutputSettings.Syntax.html
 *         && (val == null || val.equals(key) && Attribute.isBooleanAttribute(key))))`
 *   - Defect: When val == null and key is a boolean attribute, the condition should be false (so no "=\"\"" is appended).
 *     Bug: Possibly the condition fails because `val.equals(key)` is false when val is null, or the parentheses are wrong.
 * - Branch: indexOfKey, indexOfKeyIgnoreCase: null key validation.
 * - Branch: checkCapacity: growth factor, initial capacity, minNewSize > size.
 * - Branch: remove(int index): index >= size validation.
 * - Branch: put(String key, boolean value): true -> putIgnoreCase with null; false -> remove.
 * - Branch: putIgnoreCase: key exists and case changed -> update key.
 * - Branch: addAll: incoming size 0 -> return early.
 * - Branch: iterator: remove() decrements i.
 * - Branch: asList: val == null -> BooleanAttribute else Attribute.
 * - Branch: Dataset: entrySet, size, iterator, put, remove.
 * - Branch: normalize: lowerCase each key.
 * - Branch: equals: size, keys, vals arrays.
 * - Branch: hashCode: size, keys, vals hash.
 * - Branch: clone: size, copyOf keys/vals.
 * - Boundary: empty attributes, single attribute, multiple attributes, null/empty key/value.
 * - Boundary: large number of attributes to trigger growth.
 * - Boundary: boolean attribute with null value, empty string value, same as key value.
 * - Exception: null key in indexOfKey -> Validate.notNull throws IllegalArgumentException.
 */
public class AttributesDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testPutAndGet() {
        Attributes attrs = new Attributes();
        attrs.put("key1", "value1");
        assertEquals("value1", attrs.get("key1"));
        assertEquals("", attrs.get("nonexistent"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutReplace() {
        Attributes attrs = new Attributes();
        attrs.put("key", "old");
        attrs.put("key", "new");
        assertEquals("new", attrs.get("key"));
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "value");
        attrs.putIgnoreCase("key", "newValue");
        assertEquals("newValue", attrs.get("Key")); // key unchanged because case same? Actually putIgnoreCase updates key if case changed
        // Since "Key" and "key" differ in case, the key should be updated to "key"
        assertTrue(attrs.hasKey("key"));
        assertFalse(attrs.hasKey("Key"));
    }

    @Test(timeout = 4000)
    public void testPutIgnoreCaseNewKey() {
        Attributes attrs = new Attributes();
        attrs.putIgnoreCase("newKey", "val");
        assertEquals("val", attrs.get("newKey"));
    }

    @Test(timeout = 4000)
    public void testPutBooleanTrue() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        assertTrue(attrs.hasKey("disabled"));
        assertEquals("", attrs.get("disabled")); // boolean attribute -> null value -> empty string
    }

    @Test(timeout = 4000)
    public void testPutBooleanFalse() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", true);
        attrs.put("disabled", false);
        assertFalse(attrs.hasKey("disabled"));
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutAttribute() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("class", "main");
        attrs.put(attr);
        assertEquals("main", attrs.get("class"));
        assertSame(attrs, attr.parent); // parent set
    }

    @Test(timeout = 4000)
    public void testHasKey() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        assertTrue(attrs.hasKey("key"));
        assertFalse(attrs.hasKey("Key"));
        assertTrue(attrs.hasKeyIgnoreCase("Key"));
    }

    @Test(timeout = 4000)
    public void testRemove() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        attrs.remove("a");
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("a"));
        assertEquals("2", attrs.get("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("Key", "val");
        attrs.removeIgnoreCase("key");
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testSize() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        attrs.put("a", "1");
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testIterator() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        int count = 0;
        for (Attribute attr : attrs) {
            count++;
            assertNotNull(attr.getKey());
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testIteratorRemove() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        Iterator<Attribute> it = attrs.iterator();
        it.next();
        it.remove();
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("a"));
    }

    @Test(timeout = 4000)
    public void testAsList() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", null); // boolean attribute
        attrs.put("c", "3");
        java.util.List<Attribute> list = attrs.asList();
        assertEquals(3, list.size());
        assertTrue(list.get(0) instanceof Attribute);
        assertTrue(list.get(1) instanceof BooleanAttribute);
        assertTrue(list.get(2) instanceof Attribute);
    }

    @Test(timeout = 4000)
    public void testHtml() {
        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        attrs.put("id", "test");
        String html = attrs.html();
        assertTrue(html.contains("class=\"main\""));
        assertTrue(html.contains("id=\"test\""));
        assertTrue(html.startsWith(" ")); // leading space
    }

    @Test(timeout = 4000)
    public void testDataset() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "value");
        attrs.put("class", "ignore");
        java.util.Map<String, String> ds = attrs.dataset();
        assertEquals(1, ds.size());
        assertEquals("value", ds.get("name"));
    }

    @Test(timeout = 4000)
    public void testDatasetPut() {
        Attributes attrs = new Attributes();
        attrs.dataset().put("custom", "data");
        assertTrue(attrs.hasKey("data-custom"));
        assertEquals("data", attrs.get("data-custom"));
    }

    @Test(timeout = 4000)
    public void testEquals() {
        Attributes a = new Attributes();
        a.put("key", "val");
        Attributes b = new Attributes();
        b.put("key", "val");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testNotEquals() {
        Attributes a = new Attributes();
        a.put("key", "val");
        Attributes b = new Attributes();
        b.put("key", "different");
        assertNotEquals(a, b);
    }

    @Test(timeout = 4000)
    public void testClone() {
        Attributes original = new Attributes();
        original.put("a", "1");
        original.put("b", "2");
        Attributes clone = original.clone();
        assertEquals(original, clone);
        clone.put("c", "3");
        assertNotEquals(original.size(), clone.size());
    }

    @Test(timeout = 4000)
    public void testNormalize() {
        Attributes attrs = new Attributes();
        attrs.put("KEY", "val");
        attrs.put("Another", "value");
        attrs.normalize();
        assertTrue(attrs.hasKey("key"));
        assertTrue(attrs.hasKey("another"));
        assertFalse(attrs.hasKey("KEY"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyAttributes() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());
        assertEquals("", attrs.get("any"));
        assertFalse(attrs.hasKey("any"));
        assertEquals("", attrs.html().trim()); // html() returns just a space? Actually empty attributes produce empty string? Let's check: html() builds from size=0, so accum is empty. So html() should be "".
        assertEquals("", attrs.html());
    }

    @Test(timeout = 4000)
    public void testNullKeyInGet() {
        Attributes attrs = new Attributes();
        // indexOfKey calls Validate.notNull(key) -> IllegalArgumentException
        try {
            attrs.get(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullKeyInHasKey() {
        Attributes attrs = new Attributes();
        try {
            attrs.hasKey(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullKeyInRemove() {
        Attributes attrs = new Attributes();
        try {
            attrs.remove(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyKey() {
        Attributes attrs = new Attributes();
        attrs.put("", "emptykey");
        assertTrue(attrs.hasKey(""));
        assertEquals("emptykey", attrs.get(""));
    }

    @Test(timeout = 4000)
    public void testNullValue() {
        Attributes attrs = new Attributes();
        attrs.put("bool", null);
        assertEquals("", attrs.get("bool")); // checkNotNull returns empty string
        assertTrue(attrs.hasKey("bool"));
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfAttributes() {
        Attributes attrs = new Attributes();
        int n = 100;
        for (int i = 0; i < n; i++) {
            attrs.put("key" + i, "val" + i);
        }
        assertEquals(n, attrs.size());
        assertEquals("val99", attrs.get("key99"));
    }

    @Test(timeout = 4000)
    public void testAddAllEmpty() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        Attributes empty = new Attributes();
        attrs.addAll(empty);
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testAddAllNonEmpty() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        Attributes incoming = new Attributes();
        incoming.put("b", "2");
        incoming.put("c", "3");
        attrs.addAll(incoming);
        assertEquals(3, attrs.size());
        assertEquals("2", attrs.get("b"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect: boolean attributes should not have =""
     * when value is null. The bug causes output like `noshade=""` instead of `noshade`.
     */
    @Test(timeout = 4000)
    public void testBooleanAttributeOutput() {
        Attributes attrs = new Attributes();
        // Set boolean attributes with null value (as done by put(key, true))
        attrs.put("noshade", true);
        attrs.put("nohref", true);
        attrs.put("async", true);
        attrs.put("autofocus", true);
        // Also add a normal attribute to ensure spacing
        attrs.put("src", "foo");

        String html = attrs.html();
        // The expected output should have: src="foo" noshade nohref async autofocus
        // The defect produces: src="foo" noshade="" nohref="" async="" autofocus=""
        // So we assert that the string does NOT contain =""
        assertFalse("Boolean attribute should not have empty value", html.contains("=\"\""));
        // Also verify that the boolean attributes appear without value
        assertTrue(html.contains(" noshade"));
        assertTrue(html.contains(" nohref"));
        assertTrue(html.contains(" async"));
        assertTrue(html.contains(" autofocus"));
        // And the normal attribute is correctly quoted
        assertTrue(html.contains("src=\"foo\""));
    }

    /**
     * Edge case: boolean attribute with value equal to key (e.g., disabled="disabled")
     * should also be collapsed in HTML syntax.
     */
    @Test(timeout = 4000)
    public void testBooleanAttributeWithSameValue() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", "disabled");
        // In HTML syntax, this should be collapsed to just "disabled"
        String html = attrs.html();
        assertFalse("Boolean attribute with same value should not have value", html.contains("=\"disabled\""));
        assertTrue(html.contains(" disabled"));
    }

    /**
     * Boolean attribute with non-null, non-matching value should be output normally.
     */
    @Test(timeout = 4000)
    public void testBooleanAttributeWithDifferentValue() {
        Attributes attrs = new Attributes();
        attrs.put("disabled", "yes");
        String html = attrs.html();
        assertTrue(html.contains("disabled=\"yes\""));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPutNullAttribute() {
        Attributes attrs = new Attributes();
        attrs.put((Attribute) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIndexOfKeyNull() {
        Attributes attrs = new Attributes();
        attrs.indexOfKey(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIndexOfKeyIgnoreCaseNull() {
        Attributes attrs = new Attributes();
        attrs.indexOfKeyIgnoreCase(null);
    }

    @Test(timeout = 4000)
    public void testRemoveInvalidIndex() {
        Attributes attrs = new Attributes();
        // private method remove(int) is not directly accessible, but we can trigger via iterator remove after next?
        // Actually iterator remove calls remove(--i) which could be invalid if i==0? Let's test.
        // We'll test via iterator: call remove without next -> should throw? Actually iterator's remove() calls remove(--i) where i=0 initially, so --i = -1, then remove(-1) will throw ArrayIndexOutOfBounds? Actually remove(int index) does Validate.isFalse(index >= size) but index=-1 is not >= size (size=0), so it passes validation, then System.arraycopy with negative index? That would cause ArrayIndexOutOfBoundsException. So we expect an exception.
        Iterator<Attribute> it = attrs.iterator();
        try {
            it.remove();
            fail("Expected exception from remove without next");
        } catch (Exception e) {
            // expected (ArrayIndexOutOfBoundsException or similar)
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        int h1 = attrs.hashCode();
        attrs.put("b", "2");
        int h2 = attrs.hashCode();
        assertNotEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Attributes original = new Attributes();
        original.put("key", "val");
        Attributes clone = original.clone();
        clone.put("key", "changed");
        assertEquals("val", original.get("key"));
    }

    @Test(timeout = 4000)
    public void testToStringEqualsHtml() {
        Attributes attrs = new Attributes();
        attrs.put("a", "b");
        assertEquals(attrs.html(), attrs.toString());
    }

    @Test(timeout = 4000)
    public void testDatasetEntrySetSize() {
        Attributes attrs = new Attributes();
        attrs.put("data-x", "1");
        attrs.put("data-y", "2");
        attrs.put("normal", "3");
        assertEquals(2, attrs.dataset().size());
    }

    @Test(timeout = 4000)
    public void testDatasetIterator() {
        Attributes attrs = new Attributes();
        attrs.put("data-a", "A");
        attrs.put("data-b", "B");
        java.util.Map<String, String> ds = attrs.dataset();
        java.util.Set<java.util.Map.Entry<String, String>> entries = ds.entrySet();
        assertEquals(2, entries.size());
        for (java.util.Map.Entry<String, String> e : entries) {
            assertTrue(e.getKey().matches("[ab]"));
        }
    }

    @Test(timeout = 4000)
    public void testDatasetRemove() {
        Attributes attrs = new Attributes();
        attrs.put("data-remove", "value");
        attrs.dataset().remove("remove");
        assertFalse(attrs.hasKey("data-remove"));
    }

    @Test(timeout = 4000)
    public void testNormalizeEmpty() {
        Attributes attrs = new Attributes();
        attrs.normalize(); // should not throw
        assertEquals(0, attrs.size());
    }
}