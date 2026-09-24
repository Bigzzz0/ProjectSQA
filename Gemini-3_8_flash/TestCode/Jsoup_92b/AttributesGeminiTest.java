package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Attributes
 * -------------------------------------------------------------------------------------------------------------------
 * Branch / Zone                      Condition / Execution Path                           Targeted Test Method
 * -------------------------------------------------------------------------------------------------------------------
 * Defect: Case-Sensitivity Handling  Preserve distinct case-sensitive keys until normal- testCaseSensitiveAttributeRetention
 *                                    ization, track overwrite vs append.                 testNormalizeDuplicatesBehavior
 * Defect: Duplicate Attr Accumulation Verify behavior when identical/cased keys added;   testDuplicateKeyScenarios
 *                                    check normalization impacts on duplicate names.
 * Partition A: Capacity Growth       checkCapacity: initial < 4, growth 2x, large addAll  testCapacityExpansionAndThresholds
 * Partition A: CRUD Operations       put, get, remove (exact case)                        testPutGetRemoveStandard
 * Partition A: Case-Insensitive CRUD putIgnoreCase, getIgnoreCase, removeIgnoreCase       testCaseInsensitiveCrud
 * Partition A: Boolean Attributes    put(key, true) sets null val; put(key, false) removes testBooleanAttributeHandling
 * Partition A: Attribute Insertion   put(Attribute) with parent reparenting              testPutAttributeObject
 * Partition A: Mass Modification     addAll() with empty vs populated incoming           testAddAllScenarios
 * Partition B: Boundary Value (BVA)  Index shift bounds on remove: first, middle, last   testRemovalBoundariesAndShifting
 * Partition B: Empty & Missing Keys  get() on missing returns EmptyString ("")            testMissingKeyBehaviors
 * Partition C: Dataset Subsystem     dataset() Map view, put, size, iterator, remove     testDatasetOperations
 * Partition D: Defensive Guards      Null keys, null attributes check with Validate      testDefensiveGuardsNullHandling
 * Partition E: Iteration & AsList    Iterator hasNext, next, remove; asList unmodifiable testIteratorAndAsListContract
 * Partition E: Serialization & HTML  html(), custom Appendable, entity escaping, collapse testHtmlSerialization
 * Partition E: Contract Integrity    equals, hashCode, clone independence                testContractAndCloneIntegrity
 * -------------------------------------------------------------------------------------------------------------------
 */
public class AttributesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Deduplication & Case Sensitivity)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCaseSensitiveAttributeRetention() {
        Attributes attrs = new Attributes();
        attrs.put("One", "One");
        attrs.put("one", "Three");
        attrs.put("two", "Four");
        attrs.put("Two", "Six");

        // Ground truth: Attributes class is case-sensitive by default
        assertEquals(4, attrs.size());
        assertEquals("One", attrs.get("One"));
        assertEquals("Three", attrs.get("one"));
        assertEquals("Four", attrs.get("two"));
        assertEquals("Six", attrs.get("Two"));

        assertTrue(attrs.hasKey("One"));
        assertTrue(attrs.hasKey("one"));
    }

    @Test(timeout = 4000)
    public void testNormalizeDuplicatesBehavior() {
        Attributes attrs = new Attributes();
        attrs.put("One", "OneVal");
        attrs.put("one", "ThreeVal");

        assertEquals(2, attrs.size());
        attrs.normalize();

        // Normalizing lowercases all keys in place
        assertEquals(2, attrs.size());
        // get("one") locates the first occurrence
        assertEquals("OneVal", attrs.get("one"));

        String html = attrs.html();
        assertTrue(html.contains("one=\"OneVal\""));
        assertTrue(html.contains("one=\"ThreeVal\""));
    }

    @Test(timeout = 4000)
    public void testDuplicateKeyScenarios() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val1");
        attrs.put("key", "val2");

        // Overwrite same case
        assertEquals(1, attrs.size());
        assertEquals("val2", attrs.get("key"));

        // Case variation overwrites when using putIgnoreCase
        attrs.putIgnoreCase("KEY", "val3");
        assertEquals(1, attrs.size());
        assertEquals("val3", attrs.getIgnoreCase("key"));
        assertEquals("val3", attrs.get("KEY"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutGetRemoveStandard() {
        Attributes attrs = new Attributes();
        assertEquals(0, attrs.size());

        attrs.put("href", "https://jsoup.org");
        attrs.put("title", "Jsoup Home");

        assertEquals(2, attrs.size());
        assertEquals("https://jsoup.org", attrs.get("href"));
        assertEquals("Jsoup Home", attrs.get("title"));
        assertTrue(attrs.hasKey("href"));
        assertFalse(attrs.hasKey("nonexistent"));

        attrs.remove("href");
        assertEquals(1, attrs.size());
        assertFalse(attrs.hasKey("href"));
        assertEquals("", attrs.get("href"));

        // Remove non-existent is a no-op
        attrs.remove("nonexistent");
        assertEquals(1, attrs.size());
    }

    @Test(timeout = 4000)
    public void testCaseInsensitiveCrud() {
        Attributes attrs = new Attributes();
        attrs.putIgnoreCase("Content-Type", "text/html");

        assertTrue(attrs.hasKeyIgnoreCase("content-type"));
        assertTrue(attrs.hasKeyIgnoreCase("CONTENT-TYPE"));
        assertEquals("text/html", attrs.getIgnoreCase("content-type"));

        // Update with changed case key
        attrs.putIgnoreCase("CONTENT-TYPE", "application/json");
        assertEquals(1, attrs.size());
        assertEquals("application/json", attrs.get("CONTENT-TYPE"));
        assertEquals("", attrs.get("Content-Type")); // old key replaced

        attrs.removeIgnoreCase("content-type");
        assertEquals(0, attrs.size());
        assertFalse(attrs.hasKeyIgnoreCase("CONTENT-TYPE"));
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeHandling() {
        Attributes attrs = new Attributes();

        // Adding boolean true sets value to null internally, represented as EmptyString
        attrs.put("required", true);
        assertEquals(1, attrs.size());
        assertTrue(attrs.hasKey("required"));
        assertEquals("", attrs.get("required"));

        // Setting false removes the attribute
        attrs.put("required", false);
        assertEquals(0, attrs.size());
        assertFalse(attrs.hasKey("required"));

        // Setting false on a non-existent key is safe
        attrs.put("disabled", false);
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testPutAttributeObject() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("target", "_blank");

        attrs.put(attr);
        assertEquals(1, attrs.size());
        assertEquals("_blank", attrs.get("target"));
        assertSame(attrs, attr.parent);
    }

    @Test(timeout = 4000)
    public void testCapacityExpansionAndThresholds() {
        Attributes attrs = new Attributes();
        // Add beyond InitialCapacity (4) to trigger growth
        for (int i = 0; i < 10; i++) {
            attrs.put("key" + i, "val" + i);
        }
        assertEquals(10, attrs.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("val" + i, attrs.get("key" + i));
        }
    }

    @Test(timeout = 4000)
    public void testAddAllScenarios() {
        Attributes source = new Attributes();
        source.put("k1", "v1");
        source.put("k2", "v2");

        Attributes target = new Attributes();
        target.put("k0", "v0");
        target.put("k1", "v_old");

        // Test non-empty addition
        target.addAll(source);
        assertEquals(3, target.size());
        assertEquals("v1", target.get("k1")); // k1 overwritten
        assertEquals("v2", target.get("k2"));

        // Test adding empty incoming
        int sizeBefore = target.size();
        target.addAll(new Attributes());
        assertEquals(sizeBefore, target.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRemovalBoundariesAndShifting() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        attrs.put("c", "3");
        attrs.put("d", "4");

        // Remove from head
        attrs.remove("a");
        assertEquals(3, attrs.size());
        assertEquals("1", attrs.get("a").isEmpty() ? "1" : "error"); // a removed
        assertEquals("2", attrs.get("b"));

        // Remove from middle
        attrs.remove("c");
        assertEquals(2, attrs.size());
        assertFalse(attrs.hasKey("c"));

        // Remove from tail
        attrs.remove("d");
        assertEquals(1, attrs.size());
        assertEquals("2", attrs.get("b"));

        // Remove last remaining element
        attrs.remove("b");
        assertEquals(0, attrs.size());
    }

    @Test(timeout = 4000)
    public void testMissingKeyBehaviors() {
        Attributes attrs = new Attributes();
        assertEquals("", attrs.get("not_set"));
        assertEquals("", attrs.getIgnoreCase("not_set"));
        assertFalse(attrs.hasKey("not_set"));
        assertFalse(attrs.hasKeyIgnoreCase("not_set"));
    }

    @Test(timeout = 4000)
    public void testStaticCheckNotNull() {
        assertEquals("", Attributes.checkNotNull(null));
        assertEquals("valid", Attributes.checkNotNull("valid"));
        assertEquals("", Attributes.checkNotNull(""));
    }

    // =========================================================================
    // Partition C: Dataset Subsystem Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testDatasetOperations() {
        Attributes attrs = new Attributes();
        attrs.put("data-name", "Jsoup");
        attrs.put("data-version", "1.0");
        attrs.put("id", "main");

        Map<String, String> dataset = attrs.dataset();
        assertEquals(2, dataset.size());

        // Put new entry into dataset view
        String oldVal = dataset.put("author", "Jonathan");
        assertNull(oldVal);
        assertEquals(3, dataset.size());
        assertEquals("Jonathan", attrs.get("data-author"));

        // Update existing entry via dataset view
        oldVal = dataset.put("name", "JsoupNew");
        assertEquals("Jsoup", oldVal);
        assertEquals("JsoupNew", attrs.get("data-name"));

        // Iterate and remove via dataset
        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        assertNotNull(entry.getKey());
        it.remove();

        assertEquals(2, dataset.size());
    }

    @Test(timeout = 4000)
    public void testDatasetEmpty() {
        Attributes attrs = new Attributes();
        attrs.put("class", "container");
        attrs.put("style", "display:none;");

        Map<String, String> dataset = attrs.dataset();
        assertEquals(0, dataset.size());
        assertFalse(dataset.entrySet().iterator().hasNext());
    }

    // =========================================================================
    // Partition D: Defensive Guard & Exception Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIndexOfKeyNullThrows() {
        Attributes attrs = new Attributes();
        attrs.indexOfKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPutNullAttributeThrows() {
        Attributes attrs = new Attributes();
        attrs.put(null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAsListIsUnmodifiable() {
        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        List<Attribute> list = attrs.asList();
        list.add(new Attribute("fail", "fail"));
    }

    // =========================================================================
    // Partition E: Iteration, HTML Serialization & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorAndAsListContract() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", null); // BooleanAttribute internally

        List<Attribute> list = attrs.asList();
        assertEquals(2, list.size());
        assertTrue(list.get(1) instanceof BooleanAttribute);

        Iterator<Attribute> it = attrs.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next().getKey());
        it.remove();

        assertEquals(1, attrs.size());
        assertEquals("b", attrs.iterator().next().getKey());
    }

    @Test(timeout = 4000)
    public void testHtmlSerialization() {
        Attributes attrs = new Attributes();
        attrs.put("checked", null);
        attrs.put("class", "btn & primary");

        String html = attrs.html();
        assertEquals(" checked class=\"btn &amp; primary\"", html);
        assertEquals(html, attrs.toString());

        StringBuilder sb = new StringBuilder();
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        try {
            attrs.html(sb, doc.outputSettings());
            // In XML syntax, boolean attributes are not collapsed
            assertTrue(sb.toString().contains("checked=\"\""));
        } catch (IOException e) {
            fail("IOException should not be thrown on StringBuilder");
        }
    }

    @Test(timeout = 4000)
    public void testContractAndCloneIntegrity() {
        Attributes a1 = new Attributes();
        a1.put("k1", "v1");
        a1.put("k2", "v2");

        Attributes a2 = new Attributes();
        a2.put("k1", "v1");
        a2.put("k2", "v2");

        // Reflexive, Symmetric, Transitive
        assertTrue(a1.equals(a1));
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertEquals(a1.hashCode(), a2.hashCode());

        assertFalse(a1.equals(null));
        assertFalse(a1.equals("different type"));

        a2.put("k3", "v3");
        assertFalse(a1.equals(a2));

        // Test Clone
        Attributes clone = a1.clone();
        assertNotNull(clone);
        assertEquals(a1.size(), clone.size());
        assertEquals(a1.get("k1"), clone.get("k1"));
        assertEquals(a1.get("k2"), clone.get("k2"));

        // Mutating original after clone does not affect clone's state values
        a1.put("k1", "mutated");
        assertEquals("v1", clone.get("k1"));
    }
}