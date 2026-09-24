package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------------------
 * Branch / Path Target                      | Input State & Scenario              | Target Defect / Expected Behavior
 * -------------------------------------------------------------------------------------------------------------------
 * Partition C: Defect-Targeted Zone         | attributes map contains multiple     | ConcurrentModificationException
 *   removeIgnoreCase() iteration            | items; remove non-final attribute    | triggered by attributes.remove()
 *                                           | via case-insensitive match           | inside keySet iterator loop
 * -------------------------------------------------------------------------------------------------------------------
 * Partition C: DatasetIterator.remove()     | DatasetIterator remove called while  | ConcurrentModificationException
 *                                           | iterating over data-* attributes     | on subsequent iterator advance
 * -------------------------------------------------------------------------------------------------------------------
 * Partition A: Core Logic & Transitions     | Put string, put boolean (true/false) | Correct addition, boolean attribute
 *                                           | Overwrite existing keys, get/has     | handling, deletion on false
 * -------------------------------------------------------------------------------------------------------------------
 * Partition B: Boundary Value Analysis      | attributes == null initial state     | Graceful defaults: "", 0, false,
 *                                           | empty strings, mixed casing          | empty iterators, unmodifiable lists
 * -------------------------------------------------------------------------------------------------------------------
 * Partition D: Defensive Guards             | null/empty keys passed to get, put,  | IllegalArgumentException thrown by
 *                                           | remove, removeIgnoreCase             | Validate.notEmpty / notNull
 * -------------------------------------------------------------------------------------------------------------------
 * Partition E: Contract & Lifecycle         | equals, hashCode, clone              | Deep equality, independent clone,
 *                                           | HTML serialization with settings     | null-safe hashCode and equals
 * -------------------------------------------------------------------------------------------------------------------
 */
public class AttributesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets Defects4J regression: ConcurrentModificationException in removeIgnoreCase
     * when removing an attribute from a multi-element map.
     */
    @Test(timeout = 4000)
    public void testRemoveIgnoreCaseConcurrentModificationDefect() {
        Attributes attrs = new Attributes();
        attrs.put("one", "1");
        attrs.put("two", "2");
        attrs.put("three", "3");

        // Defective implementation calls attributes.remove() during keySet iterator traversal
        attrs.removeIgnoreCase("one");

        assertEquals(2, attrs.size());
        assertFalse(attrs.hasKey("one"));
        assertTrue(attrs.hasKey("two"));
        assertTrue(attrs.hasKey("three"));
    }

    /**
     * Targets chained removal matching ElementTest::testChainedRemoveAttributes scenario.
     */
    @Test(timeout = 4000)
    public void testChainedRemoveIgnoreCaseSequence() {
        Attributes attrs = new Attributes();
        attrs.put("alpha", "A");
        attrs.put("BETA", "B");
        attrs.put("gamma", "C");
        attrs.put("DELTA", "D");

        attrs.removeIgnoreCase("missing");
        assertEquals(4, attrs.size());

        attrs.removeIgnoreCase("beta");
        assertFalse(attrs.hasKeyIgnoreCase("beta"));
        assertEquals(3, attrs.size());

        attrs.removeIgnoreCase("ALPHA");
        assertFalse(attrs.hasKey("alpha"));
        assertEquals(2, attrs.size());

        attrs.removeIgnoreCase("delta");
        assertFalse(attrs.hasKeyIgnoreCase("delta"));
        assertEquals(1, attrs.size());
        assertTrue(attrs.hasKey("gamma"));
    }

    /**
     * Targets DatasetIterator.remove() modification integrity during traversal.
     */
    @Test(timeout = 4000)
    public void testDatasetIteratorRemoveIntegrity() {
        Attributes attrs = new Attributes();
        attrs.put("data-first", "1");
        attrs.put("data-second", "2");
        attrs.put("other", "regular");

        Map<String, String> dataset = attrs.dataset();
        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();

        assertTrue(it.hasNext());
        Map.Entry<String, String> firstEntry = it.next();
        assertEquals("first", firstEntry.getKey());
        it.remove();

        // Ensure iterator continues properly without CME
        assertTrue(it.hasNext());
        Map.Entry<String, String> secondEntry = it.next();
        assertEquals("second", secondEntry.getKey());
        assertFalse(it.hasNext());

        assertFalse(attrs.hasKey("data-first"));
        assertTrue(attrs.hasKey("data-second"));
        assertTrue(attrs.hasKey("other"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutAndGetCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("key", "lowercase");
        attrs.put("KEY", "uppercase");

        assertEquals("lowercase", attrs.get("key"));
        assertEquals("uppercase", attrs.get("KEY"));
        assertTrue(attrs.hasKey("key"));
        assertTrue(attrs.hasKey("KEY"));
        assertFalse(attrs.hasKey("Key"));
        assertEquals(2, attrs.size());
    }

    @Test(timeout = 4000)
    public void testGetIgnoreCaseAndHasKeyIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("myAttr", "testValue");

        assertEquals("testValue", attrs.getIgnoreCase("myattr"));
        assertEquals("testValue", attrs.getIgnoreCase("MYATTR"));
        assertEquals("testValue", attrs.getIgnoreCase("myAttr"));

        assertTrue(attrs.hasKeyIgnoreCase("myattr"));
        assertTrue(attrs.hasKeyIgnoreCase("MYATTR"));
        assertFalse(attrs.hasKeyIgnoreCase("nonexistent"));
        assertEquals("", attrs.getIgnoreCase("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testPutBooleanTrueAndFalse() {
        Attributes attrs = new Attributes();
        attrs.put("required", true);

        assertTrue(attrs.hasKey("required"));
        assertEquals("", attrs.get("required"));
        assertTrue(attrs.asList().get(0) instanceof BooleanAttribute);

        // Setting false must remove the attribute
        attrs.put("required", false);
        assertFalse(attrs.hasKey("required"));
        assertEquals(0, attrs.size());

        // Setting false on an attribute that does not exist should be a safe no-op
        attrs.put("disabled", false);
        assertFalse(attrs.hasKey("disabled"));
    }

    @Test(timeout = 4000)
    public void testRemoveCaseSensitive() {
        Attributes attrs = new Attributes();
        attrs.put("item", "1");
        attrs.put("ITEM", "2");

        attrs.remove("item");
        assertFalse(attrs.hasKey("item"));
        assertTrue(attrs.hasKey("ITEM"));
        assertEquals("2", attrs.get("ITEM"));

        // Removing non-existent key
        attrs.remove("unknown");
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testAddAllMergesAttributes() {
        Attributes src = new Attributes();
        src.put("k1", "v1");
        src.put("k2", "v2");

        Attributes dst = new Attributes();
        dst.put("k2", "v2-old");
        dst.put("k3", "v3");

        dst.addAll(src);
        assertEquals(3, dst.size());
        assertEquals("v1", dst.get("k1"));
        assertEquals("v2", dst.get("k2")); // Overwritten by incoming
        assertEquals("v3", dst.get("k3"));
    }

    @Test(timeout = 4000)
    public void testDatasetPutAndEntrySet() {
        Attributes attrs = new Attributes();
        attrs.put("class", "button");
        attrs.put("data-type", "submit");

        Map<String, String> dataset = attrs.dataset();
        assertEquals(1, dataset.size());
        assertEquals("submit", dataset.get("type"));

        // Put a new entry into dataset
        String oldVal = dataset.put("action", "save");
        assertNull(oldVal);
        assertTrue(attrs.hasKey("data-action"));
        assertEquals("save", attrs.get("data-action"));
        assertEquals(2, dataset.size());

        // Overwrite through dataset
        oldVal = dataset.put("action", "update");
        assertEquals("save", oldVal);
        assertEquals("update", attrs.get("data-action"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testUninitializedAttributesState() {
        Attributes attrs = new Attributes();

        assertEquals(0, attrs.size());
        assertEquals("", attrs.get("anything"));
        assertEquals("", attrs.getIgnoreCase("anything"));
        assertFalse(attrs.hasKey("anything"));
        assertFalse(attrs.hasKeyIgnoreCase("anything"));
        assertFalse(attrs.iterator().hasNext());
        assertTrue(attrs.asList().isEmpty());
        assertEquals("", attrs.html());
        assertEquals("", attrs.toString());

        // Safe operations on empty/null state
        attrs.remove("anything");
        attrs.removeIgnoreCase("anything");
        assertEquals(0, attrs.size());

        // addAll from empty
        attrs.addAll(new Attributes());
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testAddAllIntoUninitializedAttributes() {
        Attributes src = new Attributes();
        src.put("initialKey", "initialVal");

        Attributes dst = new Attributes();
        dst.addAll(src);

        assertEquals(1, dst.size());
        assertEquals("initialVal", dst.get("initialKey"));
    }

    @Test(timeout = 4000)
    public void testAddAllWithEmptyIncoming() {
        Attributes attrs = new Attributes();
        attrs.put("exist", "val");

        attrs.addAll(new Attributes());
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testEmptyListFromIteratorWhenCleaned() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        attrs.remove("key");

        // Size is 0, iterator should be empty
        Iterator<Attribute> it = attrs.iterator();
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testDatasetOnUninitializedAttributes() {
        Attributes attrs = new Attributes();
        Map<String, String> dataset = attrs.dataset();

        assertEquals(0, dataset.size());
        assertFalse(dataset.entrySet().iterator().hasNext());

        dataset.put("created", "true");
        assertEquals(1, attrs.size());
        assertTrue(attrs.hasKey("data-created"));
        assertEquals("true", attrs.get("data-created"));
    }

    @Test(timeout = 4000)
    public void testHtmlSerializationFormatting() throws IOException {
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        attrs.put("disabled", true);
        attrs.put("data-id", "123");

        String html = attrs.html();
        assertEquals(" id=\"main\" disabled data-id=\"123\"", html);
        assertEquals(html, attrs.toString());

        StringBuilder sb = new StringBuilder();
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        attrs.html(sb, doc.outputSettings());
        assertEquals(" id=\"main\" disabled=\"\" data-id=\"123\"", sb.toString());
    }

    @Test(timeout = 4000)
    public void testHtmlOutputOnUninitialized() throws IOException {
        Attributes attrs = new Attributes();
        StringBuilder sb = new StringBuilder();
        attrs.html(sb, new Document("").outputSettings());
        assertEquals("", sb.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetNullKeyThrowsException() {
        new Attributes().get(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetEmptyKeyThrowsException() {
        new Attributes().get("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIgnoreCaseNullKeyThrowsException() {
        new Attributes().getIgnoreCase(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIgnoreCaseEmptyKeyThrowsException() {
        new Attributes().getIgnoreCase("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPutNullAttributeThrowsException() {
        new Attributes().put((Attribute) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveNullKeyThrowsException() {
        new Attributes().remove(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveEmptyKeyThrowsException() {
        new Attributes().remove("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveIgnoreCaseNullKeyThrowsException() {
        new Attributes().removeIgnoreCase(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRemoveIgnoreCaseEmptyKeyThrowsException() {
        new Attributes().removeIgnoreCase("");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAsListIsUnmodifiable() {
        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        List<Attribute> list = attrs.asList();
        list.add(new Attribute("fail", "fail"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Attributes a1 = new Attributes();
        Attributes a2 = new Attributes();

        // Reflective
        assertEquals(a1, a1);
        assertEquals(a1.hashCode(), a1.hashCode());

        // Symmetric when uninitialized
        assertEquals(a1, a2);
        assertEquals(a2, a1);
        assertEquals(a1.hashCode(), a2.hashCode());

        // Not equal to null or different types
        assertNotNull(a1);
        assertFalse(a1.equals("StringObject"));
        assertFalse(a1.equals(null));

        // Populated equality
        a1.put("k1", "v1");
        assertNotEquals(a1, a2);

        a2.put("k1", "v1");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        // Different values
        a2.put("k1", "v2");
        assertNotEquals(a1, a2);

        // Different keys
        a2.remove("k1");
        a2.put("k2", "v1");
        assertNotEquals(a1, a2);
    }

    @Test(timeout = 4000)
    public void testCloneOnUninitializedAttributes() {
        Attributes empty = new Attributes();
        Attributes cloned = empty.clone();

        assertNotSame(empty, cloned);
        assertEquals(empty, cloned);
        assertEquals(0, cloned.size());
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Attributes original = new Attributes();
        original.put("key1", "val1");
        original.put("key2", "val2");

        Attributes copy = original.clone();
        assertNotSame(original, copy);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        // Mutating copy must not affect original
        copy.put("key1", "mutated");
        copy.put("key3", "val3");

        assertEquals("val1", original.get("key1"));
        assertFalse(original.hasKey("key3"));
        assertEquals(2, original.size());
        assertEquals(3, copy.size());
        assertNotEquals(original, copy);
    }

    @Test(timeout = 4000)
    public void testIterationOrderPreserved() {
        Attributes attrs = new Attributes();
        attrs.put("z", "1");
        attrs.put("a", "2");
        attrs.put("m", "3");

        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        assertEquals("z", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("a", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("m", it.next().getKey());
        assertFalse(it.hasNext());
    }
}