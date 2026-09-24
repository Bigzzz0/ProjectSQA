package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.nodes.Attribute
 *
 * Targeted Defects & Regressions:
 * 1. Defects4J Known Defect: settersOnOrphanAttribute (org.jsoup.nodes.AttributeTest::settersOnOrphanAttribute)
 *    - In `setValue(String val)`: Defective implementation attempts `parent.get(this.key)` before verifying
 *      whether `parent != null`. For orphan attributes (where `parent == null`), this immediately triggers a
 *      fatal `NullPointerException`.
 *    - In `setKey(String key)`: Proper null guard exists, but orphan state transitions must be verified.
 *
 * Branch & Condition Coverage Matrix:
 * 1. Constructor:
 *    - null key -> Validate.notNull fails (IllegalArgumentException)
 *    - empty or whitespace-only key -> Validate.notEmpty fails after trim (IllegalArgumentException)
 *    - valid key with leading/trailing spaces -> key trimmed
 *    - parent assignment (null vs non-null)
 * 2. setKey(String key):
 *    - null / empty / whitespace key validations
 *    - parent == null branch
 *    - parent != null && parent.indexOfKey != NotFound branch (parent.keys[i] updated)
 *    - parent != null && parent.indexOfKey == NotFound branch
 * 3. setValue(String val):
 *    - parent == null branch (defect target)
 *    - parent != null && parent.indexOfKey != NotFound branch (parent.vals[i] updated)
 *    - parent != null && parent.indexOfKey == NotFound branch
 * 4. getValue():
 *    - val != null vs val == null (via Attributes.checkNotNull returning "")
 * 5. html() / toString():
 *    - Standard output rendering
 *    - HTML syntax vs XML syntax
 *    - Boolean collapse conditions:
 *      * HTML syntax + val == null -> collapsed
 *      * HTML syntax + (val == "" || val.equalsIgnoreCase(key)) && isBooleanAttribute(key) -> collapsed
 *      * HTML syntax + non-boolean attribute -> not collapsed
 *      * XML syntax -> never collapsed
 * 6. isDataAttribute():
 *    - startsWith "data-" AND length > 5 ("data-foo" -> true)
 *    - startsWith "data-" BUT length == 5 ("data-" -> false)
 *    - does not start with "data-" ("dat-foo", "other", uppercase "DATA-foo" -> false)
 * 7. isBooleanAttribute():
 *    - binary search hit in booleanAttributes array -> true
 *    - binary search miss -> false
 *    - deprecated instance isBooleanAttribute(): val == null branch -> true
 * 8. equals() and hashCode():
 *    - identity comparison (this == o)
 *    - null and type mismatch checks
 *    - key == null vs key != null
 *    - val == null vs val != null
 * 9. clone():
 *    - deep field equality, reference independence
 * 10. Exception handling:
 *    - html(Appendable, OutputSettings) propagation of IOException
 * ====================================================================================================
 */
public class AttributeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGettersAndSettersWithParent() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");

        Attribute attr = new Attribute("k1", "v1", parent);
        assertEquals("k1", attr.getKey());
        assertEquals("v1", attr.getValue());

        attr.setKey("k2");
        assertEquals("k2", attr.getKey());
        assertEquals("v1", parent.get("k2"));
        assertFalse(parent.hasKey("k1"));

        String oldVal = attr.setValue("v2");
        assertEquals("v1", oldVal);
        assertEquals("v2", attr.getValue());
        assertEquals("v2", parent.get("k2"));
    }

    @Test(timeout = 4000)
    public void testKeyTrimming() {
        Attribute attr = new Attribute("   attrKey   ", "value");
        assertEquals("attrKey", attr.getKey());

        attr.setKey("   newKey   ");
        assertEquals("newKey", attr.getKey());
    }

    @Test(timeout = 4000)
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("href", "http://example.com?foo=1&amp;bar=2");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com?foo=1&bar=2", attr.getValue());
        assertNull(attr.parent);
    }

    @Test(timeout = 4000)
    public void testHtmlOutputStandard() {
        Attribute attr = new Attribute("title", "Hello & Welcome");
        assertEquals("title=\"Hello &amp; Welcome\"", attr.html());
        assertEquals("title=\"Hello &amp; Welcome\"", attr.toString());
    }

    @Test(timeout = 4000)
    public void testSetKeyAndValueWhenParentDoesNotContainKey() {
        Attributes parent = new Attributes();
        parent.put("other", "val");

        // Attribute not actually registered inside parent's key array
        Attribute attr = new Attribute("untracked", "initVal", parent);
        attr.setKey("newUntracked");
        assertEquals("newUntracked", attr.getKey());

        String oldVal = attr.setValue("updatedVal");
        assertEquals("", oldVal);
        assertEquals("updatedVal", attr.getValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDataAttributeBoundaries() {
        assertTrue(Attribute.isDataAttribute("data-custom"));
        assertTrue(new Attribute("data-custom", "value").isDataAttribute());

        // Exact boundary: "data-" has length == 5, so length > 5 is false
        assertFalse(Attribute.isDataAttribute("data-"));
        assertFalse(new Attribute("data-", "value").isDataAttribute());

        // Less than prefix length
        assertFalse(Attribute.isDataAttribute("data"));
        assertFalse(Attribute.isDataAttribute("dat-"));

        // Case sensitivity
        assertFalse(Attribute.isDataAttribute("DATA-custom"));
        assertFalse(Attribute.isDataAttribute("Data-custom"));
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeCollapseInHtml() {
        Document.OutputSettings htmlSettings = new Document("").outputSettings();
        htmlSettings.syntax(Document.OutputSettings.Syntax.html);

        // Boolean attribute with empty string value -> collapsible
        Attribute emptyChecked = new Attribute("checked", "");
        assertTrue(emptyChecked.shouldCollapseAttribute(htmlSettings));
        assertEquals("checked", emptyChecked.html());

        // Boolean attribute with same-case name as value -> collapsible
        Attribute sameChecked = new Attribute("checked", "checked");
        assertTrue(sameChecked.shouldCollapseAttribute(htmlSettings));
        assertEquals("checked", sameChecked.html());

        // Boolean attribute with different-case name as value -> collapsible (equalsIgnoreCase)
        Attribute upperChecked = new Attribute("checked", "CHECKED");
        assertTrue(upperChecked.shouldCollapseAttribute(htmlSettings));
        assertEquals("checked", upperChecked.html());

        // Boolean attribute with null value -> collapsible
        Attribute nullChecked = new Attribute("checked", null);
        assertTrue(nullChecked.shouldCollapseAttribute(htmlSettings));
        assertEquals("checked", nullChecked.html());

        // Boolean attribute with arbitrary non-matching value -> NOT collapsible
        Attribute explicitChecked = new Attribute("checked", "true");
        assertFalse(explicitChecked.shouldCollapseAttribute(htmlSettings));
        assertEquals("checked=\"true\"", explicitChecked.html());

        // Non-boolean attribute with empty string -> NOT collapsible
        Attribute nonBoolEmpty = new Attribute("class", "");
        assertFalse(nonBoolEmpty.shouldCollapseAttribute(htmlSettings));
        assertEquals("class=\"\"", nonBoolEmpty.html());

        // Non-boolean attribute with same name as value -> NOT collapsible
        Attribute nonBoolSame = new Attribute("class", "class");
        assertFalse(nonBoolSame.shouldCollapseAttribute(htmlSettings));
        assertEquals("class=\"class\"", nonBoolSame.html());
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeNoCollapseInXml() {
        Document.OutputSettings xmlSettings = new Document("").outputSettings();
        xmlSettings.syntax(Document.OutputSettings.Syntax.xml);

        Attribute checked = new Attribute("checked", "");
        assertFalse(checked.shouldCollapseAttribute(xmlSettings));
        assertEquals("checked=\"\"", checked.html());

        Attribute checkedSame = new Attribute("checked", "checked");
        assertFalse(checkedSame.shouldCollapseAttribute(xmlSettings));
        assertEquals("checked=\"checked\"", checkedSame.html());
    }

    @Test(timeout = 4000)
    public void testIsBooleanAttributeStaticAndDeprecatedInstance() {
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertTrue(Attribute.isBooleanAttribute("required"));
        assertTrue(Attribute.isBooleanAttribute("typemustmatch"));

        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(Attribute.isBooleanAttribute("src"));
        assertFalse(Attribute.isBooleanAttribute("unknown"));

        // Deprecated instance method
        assertTrue(new Attribute("disabled", "anything").isBooleanAttribute());
        assertFalse(new Attribute("href", "http://example.com").isBooleanAttribute());
        // val == null branch in deprecated isBooleanAttribute returns true even if not a boolean attribute
        assertTrue(new Attribute("href", null).isBooleanAttribute());
    }

    @Test(timeout = 4000)
    public void testNullAttributeValueHandling() {
        Attribute attr = new Attribute("novalue", null);
        assertEquals("", attr.getValue()); // Attributes.checkNotNull returns empty string

        Document.OutputSettings out = new Document("").outputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        // In XML with null value, should not collapse, value treated as empty string
        assertEquals("novalue=\"\"", attr.html());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * org.jsoup.nodes.AttributeTest::settersOnOrphanAttribute
     * Calling setValue on an attribute constructed without an Attributes parent
     * (orphan attribute) must NOT throw a NullPointerException.
     */
    @Test(timeout = 4000)
    public void testSettersOnOrphanAttribute() {
        Attribute attr = new Attribute("key", "val");
        assertNull(attr.parent);

        // Defect trigger: in buggy version, setValue accesses parent.get(key) when parent is null -> NPE
        String oldVal = attr.setValue("newVal");
        assertEquals("val", oldVal);
        assertEquals("newVal", attr.getValue());

        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
    }

    @Test(timeout = 4000)
    public void testSetKeyOnOrphanAttribute() {
        Attribute orphan = new Attribute("orphanKey", "val");
        orphan.setKey("updatedOrphanKey");
        assertEquals("updatedOrphanKey", orphan.getKey());
        assertEquals("val", orphan.getValue());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsNullKey() {
        new Attribute(null, "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsEmptyKey() {
        new Attribute("", "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsWhitespaceOnlyKey() {
        new Attribute("     ", "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyRejectsNullKey() {
        Attribute attr = new Attribute("valid", "val");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyRejectsEmptyKey() {
        Attribute attr = new Attribute("valid", "val");
        attr.setKey("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyRejectsWhitespaceOnlyKey() {
        Attribute attr = new Attribute("valid", "val");
        attr.setKey("   ");
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testHtmlAppendableThrowsIOException() throws IOException {
        Attribute attr = new Attribute("key", "value");
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

        attr.html(brokenAppendable, new Document("").outputSettings());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Attribute a1 = new Attribute("key", "value");
        Attribute a2 = new Attribute("key", "value");
        Attribute a3 = new Attribute("key", "differentValue");
        Attribute a4 = new Attribute("differentKey", "value");
        Attribute a5 = new Attribute("key", null);
        Attribute a6 = new Attribute("key", null);

        // Reflexive
        assertTrue(a1.equals(a1));
        assertEquals(a1.hashCode(), a1.hashCode());

        // Symmetric
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertEquals(a1.hashCode(), a2.hashCode());

        // Value differences
        assertFalse(a1.equals(a3));
        assertFalse(a1.equals(a4));
        assertFalse(a1.equals(a5));

        // Null value equality
        assertTrue(a5.equals(a6));
        assertEquals(a5.hashCode(), a6.hashCode());
        assertFalse(a5.equals(a1));

        // Incompatible types and null
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("Some String"));
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Attribute original = new Attribute("k", "v");
        Attribute cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getKey(), cloned.getKey());
        assertEquals(original.getValue(), cloned.getValue());

        // Modifying cloned object does not affect original
        cloned.setKey("newK");
        cloned.setValue("newV");
        assertEquals("k", original.getKey());
        assertEquals("v", original.getValue());
    }
}