package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jsoup.SerializationException;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Attributes
 * ---------------------------------------------------------------------------------------------------------
 * Target Branch / Method          | Input Condition / Scenario                   | Target Behavior / Invariant
 * ---------------------------------------------------------------------------------------------------------
 * Defect: html() boolean collapse | Boolean attr with val = "" or val = key      | Collapse to key only in HTML syntax;
 * (Defects4J Ground Truth Bug)    | e.g. noshade="", async="async", nohref=""    | DO NOT emit noshade="" in HTML!
 * checkCapacity(int)              | 0, 1, InitialCapacity (4), Expansion (x2)    | Array doubles correctly, handles bounds
 * indexOfKey(String)              | null key, missing key, exact case, dup key   | IllegalArgumentException on null; -1 if missing
 * indexOfKeyIgnoreCase(String)    | null key, mismatched casing, missing key     | Finds index ignoring ASCII/Unicode casing
 * get(String) / getIgnoreCase     | Key present with value, null value (boolean) | Returns value or empty string (never null)
 * put(String, String)             | New entry, update existing entry             | Updates value in-place or increments size
 * putIgnoreCase(String, String)   | Key with changed case                        | Updates existing key casing and value
 * put(String, boolean)            | true (putIgnoreCase key, null), false (del)  | Removes if false; sets key with null val if true
 * put(Attribute)                  | null attribute, new attr, existing attr      | Replaces value and sets parent back-reference
 * remove(int) & remove(String)    | First item, middle, last item (boundary shift)| Correct arraycopy shift, clears dangling ref
 * removeIgnoreCase(String)        | Key present in different casing              | Removes correctly
 * addAll(Attributes)              | Empty incoming, incoming requiring resize    | All attributes merged, capacity expanded
 * iterator()                      | hasNext, next, remove() sequence             | Cursor rewinds properly on remove(); shifts correctly
 * asList()                        | Regular attributes and boolean attributes    | Immutable list; BooleanAttribute for null vals
 * dataset()                       | Entries with "data-", non-data entries       | Map view stripped prefix; put adds "data-"
 * dataset().iterator().remove()   | Iterating and removing dataset items         | Underlying Attributes keys are removed
 * html(Appendable, OutputSettings)| HTML vs XML syntax, null vs empty values     | Escaping of entities, XML collapses nothing
 * equals() & hashCode()           | Identity, null, different types, diff values | Full reflexive, symmetric, hash consistency
 * clone()                         | Deep copy verification                       | Cloned arrays independent of original
 * normalize()                     | Mixed case keys                              | In-place lowercasing of all keys
 * IOException in html()           | Failing Appendable                           | Correctly throws checked IOException
 * ---------------------------------------------------------------------------------------------------------
 */
public class AttributesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where HTML boolean attributes with empty string ("")
     * or case-insensitive matching name were rendered as attr="" instead of collapsing to attr.
     */
    @Test(timeout = 4000)
    public void testBooleanAttributeOutputCollapseEmptyString() {
        Attributes attributes = new Attributes();
        attributes.put("noshade", "");
        attributes.put("async", "");
        attributes.put("autofocus", "");

        // In HTML syntax, boolean attributes set to empty string MUST collapse to just the attribute name
        assertEquals(" noshade async autofocus", attributes.html());
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeOutputCollapseMatchingName() {
        Attributes attributes = new Attributes();
        attributes.put("checked", "checked");
        attributes.put("disabled", "DISABLED"); // case-insensitive match for HTML boolean

        assertEquals(" checked disabled", attributes.html());
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeInXmlSyntaxDoesNotCollapse() {
        Attributes attributes = new Attributes();
        attributes.put("noshade", "");
        attributes.put("checked", "checked");

        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        StringBuilder accum = new StringBuilder();
        try {
            attributes.html(accum, doc.outputSettings());
        } catch (IOException e) {
            fail("Should not throw IOException: " + e.getMessage());
        }

        // XML requires full key="val" syntax
        assertEquals(" noshade=\"\" checked=\"checked\"", accum.toString());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutAndGetStandardWorkflow() {
        Attributes attributes = new Attributes();
        assertEquals(0, attributes.size());

        attributes.put("href", "https://jsoup.org");
        attributes.put("title", "Jsoup Project");

        assertEquals(2, attributes.size());
        assertEquals("https://jsoup.org", attributes.get("href"));
        assertEquals("Jsoup Project", attributes.get("title"));
        assertTrue(attributes.hasKey("href"));
        assertTrue(attributes.hasKey("title"));
        assertFalse(attributes.hasKey("target"));

        // Overwrite existing key
        attributes.put("href", "https://example.com");
        assertEquals(2, attributes.size());
        assertEquals("https://example.com", attributes.get("href"));
    }

    @Test(timeout = 4000)
    public void testPutBooleanTrueAndFalse() {
        Attributes attributes = new Attributes();
        attributes.put("required", true);

        assertEquals(1, attributes.size());
        assertTrue(attributes.hasKey("required"));
        // Boolean attribute returns empty string on get()
        assertEquals("", attributes.get("required"));

        // Setting false removes the key
        attributes.put("required", false);
        assertEquals(0, attributes.size());
        assertFalse(attributes.hasKey("required"));

        // Setting false on absent key is a no-op
        attributes.put("absent", false);
        assertEquals(0, attributes.size());
    }

    @Test(timeout = 4000)
    public void testPutIgnoreCaseUpdatesKeyCasing() {
        Attributes attributes = new Attributes();
        attributes.putIgnoreCase("SRC", "image.png");
        assertEquals("image.png", attributes.get("SRC"));

        // Updating with different casing updates key to new casing
        attributes.putIgnoreCase("src", "image2.png");
        assertEquals(1, attributes.size());
        assertEquals("image2.png", attributes.get("src"));
        assertFalse(attributes.hasKey("SRC"));
        assertTrue(attributes.hasKey("src"));
    }

    @Test(timeout = 4000)
    public void testGetIgnoreCaseAndHasKeyIgnoreCase() {
        Attributes attributes = new Attributes();
        attributes.put("Content-Type", "text/html");

        assertTrue(attributes.hasKeyIgnoreCase("content-type"));
        assertTrue(attributes.hasKeyIgnoreCase("CONTENT-TYPE"));
        assertEquals("text/html", attributes.getIgnoreCase("content-type"));
        assertEquals("text/html", attributes.getIgnoreCase("CONTENT-TYPE"));

        // Non-existent keys
        assertFalse(attributes.hasKeyIgnoreCase("accept"));
        assertEquals("", attributes.getIgnoreCase("accept"));
    }

    @Test(timeout = 4000)
    public void testRemoveByKeyCaseSensitive() {
        Attributes attributes = new Attributes();
        attributes.put("Key", "Value");

        attributes.remove("key"); // Case mismatch, should not remove
        assertEquals(1, attributes.size());
        assertTrue(attributes.hasKey("Key"));

        attributes.remove("Key");
        assertEquals(0, attributes.size());
        assertFalse(attributes.hasKey("Key"));
    }

    @Test(timeout = 4000)
    public void testRemoveIgnoreCase() {
        Attributes attributes = new Attributes();
        attributes.put("MiXeD", "val");

        attributes.removeIgnoreCase("mixed");
        assertEquals(0, attributes.size());
        assertFalse(attributes.hasKeyIgnoreCase("MiXeD"));

        // Removing non-existent key has no effect
        attributes.removeIgnoreCase("not-found");
        assertEquals(0, attributes.size());
    }

    @Test(timeout = 4000)
    public void testPutAttributeObjectBindsParent() {
        Attributes attributes = new Attributes();
        Attribute attr = new Attribute("alt", "logo");
        attributes.put(attr);

        assertEquals("logo", attributes.get("alt"));
        assertSame(attributes, attr.parent);

        // Updating via Attribute.setValue updates parent Attributes map
        attr.setValue("new-logo");
        assertEquals("new-logo", attributes.get("alt"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA), Resizing & Removal Shifts
    // =========================================================================

    @Test(timeout = 4000)
    public void testCapacityExpansionBeyondInitial() {
        Attributes attributes = new Attributes();
        // Initial capacity is 4. Add 10 attributes to trigger multiple resizes.
        for (int i = 0; i < 10; i++) {
            attributes.put("key" + i, "val" + i);
        }

        assertEquals(10, attributes.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("val" + i, attributes.get("key" + i));
        }
    }

    @Test(timeout = 4000)
    public void testRemoveElementAtBoundaries() {
        Attributes attributes = new Attributes();
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");
        attributes.put("k3", "v3");
        attributes.put("k4", "v4");

        // Remove head
        attributes.remove("k1");
        assertEquals(3, attributes.size());
        assertFalse(attributes.hasKey("k1"));
        assertEquals("v2", attributes.get("k2"));

        // Remove middle
        attributes.remove("k3");
        assertEquals(2, attributes.size());
        assertFalse(attributes.hasKey("k3"));
        assertEquals("v2", attributes.get("k2"));
        assertEquals("v4", attributes.get("k4"));

        // Remove tail
        attributes.remove("k4");
        assertEquals(1, attributes.size());
        assertFalse(attributes.hasKey("k4"));
        assertEquals("v2", attributes.get("k2"));

        // Remove last remaining
        attributes.remove("k2");
        assertEquals(0, attributes.size());
        assertFalse(attributes.hasKey("k2"));
    }

    @Test(timeout = 4000)
    public void testAddAllEmptyAndPopulated() {
        Attributes target = new Attributes();
        Attributes empty = new Attributes();

        target.addAll(empty);
        assertEquals(0, target.size());

        Attributes source = new Attributes();
        source.put("k1", "v1");
        source.put("k2", "v2");

        target.addAll(source);
        assertEquals(2, target.size());
        assertEquals("v1", target.get("k1"));
        assertEquals("v2", target.get("k2"));

        // AddAll with overlapping keys updates existing
        Attributes overlap = new Attributes();
        overlap.put("k2", "v2_updated");
        overlap.put("k3", "v3");

        target.addAll(overlap);
        assertEquals(3, target.size());
        assertEquals("v2_updated", target.get("k2"));
        assertEquals("v3", target.get("k3"));
    }

    // =========================================================================
    // Partition D: Iteration, Dataset, and View Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorTraversalAndRemoval() {
        Attributes attributes = new Attributes();
        attributes.put("a", "1");
        attributes.put("b", "2");
        attributes.put("c", "3");

        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next().getKey());

        it.remove(); // removes "a"
        assertEquals(2, attributes.size());
        assertFalse(attributes.hasKey("a"));

        assertTrue(it.hasNext());
        assertEquals("b", it.next().getKey());

        assertTrue(it.hasNext());
        assertEquals("c", it.next().getKey());

        it.remove(); // removes "c"
        assertEquals(1, attributes.size());
        assertFalse(attributes.hasKey("c"));
        assertFalse(it.hasNext());
        assertTrue(attributes.hasKey("b"));
    }

    @Test(timeout = 4000)
    public void testAsListImmutabilityAndBooleanRepresentation() {
        Attributes attributes = new Attributes();
        attributes.put("class", "btn");
        attributes.put("disabled", true); // val is null in internal array

        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("class", list.get(0).getKey());
        assertEquals("btn", list.get(0).getValue());
        assertEquals("disabled", list.get(1).getKey());

        // Verify that the list returned is unmodifiable
        try {
            list.remove(0);
            fail("Expected UnsupportedOperationException when mutating asList view");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testDatasetViewOperations() {
        Attributes attributes = new Attributes();
        attributes.put("id", "main");
        attributes.put("data-user-id", "12345");
        attributes.put("data-role", "admin");

        Map<String, String> dataset = attributes.dataset();
        assertEquals(2, dataset.size());
        assertEquals("12345", dataset.get("user-id"));
        assertEquals("admin", dataset.get("role"));
        assertNull(dataset.get("id"));

        // Put new entry through dataset
        String oldVal = dataset.put("status", "active");
        assertNull(oldVal);
        assertEquals("active", dataset.get("status"));
        assertEquals("active", attributes.get("data-status"));

        // Overwrite through dataset
        oldVal = dataset.put("role", "superuser");
        assertEquals("admin", oldVal);
        assertEquals("superuser", attributes.get("data-role"));

        // Iterate and remove from dataset
        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (entry.getKey().equals("user-id")) {
                it.remove();
            }
        }

        assertFalse(attributes.hasKey("data-user-id"));
        assertEquals(2, dataset.size());
    }

    // =========================================================================
    // Partition E: Output, Serialization, & Normalization
    // =========================================================================

    @Test(timeout = 4000)
    public void testHtmlEscapingInValues() {
        Attributes attributes = new Attributes();
        attributes.put("href", "http://example.com/search?q=foo&bar=\"baz\"'<qux>");

        String html = attributes.html();
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&quot;"));
        assertEquals(html, attributes.toString());
    }

    @Test(timeout = 4000)
    public void testHtmlIOExceptionHandledProperly() {
        Attributes attributes = new Attributes();
        attributes.put("test", "value");

        Appendable brokenAppendable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException("Simulated IO failure");
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException("Simulated IO failure");
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("Simulated IO failure");
            }
        };

        try {
            attributes.html(brokenAppendable, new Document("").outputSettings());
            fail("Expected IOException from broken appendable");
        } catch (IOException expected) {
            assertEquals("Simulated IO failure", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNormalize() {
        Attributes attributes = new Attributes();
        attributes.put("HTTP-EQUIV", "refresh");
        attributes.put("CONTENT", "30");

        attributes.normalize();

        assertTrue(attributes.hasKey("http-equiv"));
        assertTrue(attributes.hasKey("content"));
        assertFalse(attributes.hasKey("HTTP-EQUIV"));
        assertFalse(attributes.hasKey("CONTENT"));
        assertEquals("refresh", attributes.get("http-equiv"));
    }

    // =========================================================================
    // Partition F: Object Lifecycle & Contract Integrity (equals, hashCode, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Attributes attr1 = new Attributes();
        Attributes attr2 = new Attributes();

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertEquals(attr1, attr1);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "non-attributes-object");

        attr1.put("k1", "v1");
        assertNotEquals(attr1, attr2);

        attr2.put("k1", "v1");
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        // Same size, different keys
        Attributes attr3 = new Attributes();
        attr3.put("k2", "v1");
        assertNotEquals(attr1, attr3);

        // Same size and key, different values
        Attributes attr4 = new Attributes();
        attr4.put("k1", "v2");
        assertNotEquals(attr1, attr4);
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        Attributes original = new Attributes();
        original.put("k1", "v1");
        original.put("k2", "v2");

        Attributes clone = original.clone();
        assertEquals(original, clone);
        assertEquals(original.size(), clone.size());

        // Mutating clone does not mutate original
        clone.put("k3", "v3");
        clone.put("k1", "v1_mutated");

        assertEquals(2, original.size());
        assertEquals("v1", original.get("k1"));
        assertFalse(original.hasKey("k3"));

        assertEquals(3, clone.size());
        assertEquals("v1_mutated", clone.get("k1"));
        assertTrue(clone.hasKey("k3"));
    }

    // =========================================================================
    // Partition G: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIndexOfKeyRejectsNull() {
        Attributes attributes = new Attributes();
        attributes.indexOfKey(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPutNullAttributeRejectsNull() {
        Attributes attributes = new Attributes();
        attributes.put((Attribute) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHasKeyRejectsNull() {
        Attributes attributes = new Attributes();
        attributes.hasKey(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHasKeyIgnoreCaseRejectsNull() {
        Attributes attributes = new Attributes();
        attributes.hasKeyIgnoreCase(null);
    }
}