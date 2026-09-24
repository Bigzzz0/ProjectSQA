package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.SerializationException;

import java.io.IOException;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: org.jsoup.nodes.Attribute
 *
 * Known Defect (Defects4J):
 * - AttributeTest::validatesKeysNotEmpty: Creating an Attribute with a
 *   whitespace-only key (e.g. " ") failed to throw IllegalArgumentException
 *   because the constructor called Validate.notEmpty(key) prior to checking
 *   the trimmed instance key (or checked the untrimmed local variable).
 *
 * Decision / Condition Matrix:
 * 1. Constructor & Key Trimming Validation:
 *    - key == null -> NullPointerException / IllegalArgumentException (Validate.notNull)
 *    - key == ""   -> IllegalArgumentException (Validate.notEmpty)
 *    - key == " "  -> IllegalArgumentException (Target of Known Defect)
 *    - valid key   -> trimmed correctly, assigned.
 *
 * 2. setKey(String):
 *    - key == null -> IllegalArgumentException
 *    - key == "" / " " -> IllegalArgumentException
 *    - parent != null && key exists in parent -> updates parent.keys[i]
 *    - parent != null && key not in parent (NotFound) -> does not crash
 *    - parent == null -> key updated locally
 *
 * 3. setValue(String):
 *    - parent != null && key exists -> updates parent.vals[i], returns old value
 *    - parent != null && key not in parent -> retains state, returns null
 *
 * 4. shouldCollapseAttribute(key, val, out):
 *    - Syntax == HTML vs XML
 *    - HTML:
 *      * val == null -> collapsed
 *      * val == "" && isBooleanAttribute -> collapsed
 *      * val.equalsIgnoreCase(key) && isBooleanAttribute -> collapsed
 *      * val == "" && !isBooleanAttribute -> NOT collapsed
 *      * val != "" && !val.equalsIgnoreCase(key) && isBooleanAttribute -> NOT collapsed
 *    - XML:
 *      * Any key/val -> NOT collapsed
 *
 * 5. isDataAttribute():
 *    - startsWith("data-") && length > 5 -> true
 *    - equals("data-") -> false (length boundary)
 *    - regular attribute -> false
 *
 * 6. isBooleanAttribute():
 *    - static: binary search in booleanAttributes array (exact lowercase match)
 *    - instance (deprecated): binary search match OR val == null
 *
 * 7. HTML Serialization & Error Handling:
 *    - html(Appendable, OutputSettings) escaping
 *    - IOException wrapped into SerializationException via html()
 *
 * 8. Object Integrity (equals, hashCode, clone):
 *    - reflexivity, symmetry, null-safety, type check, field permutations
 *    - clone returns independent copy
 * =========================================================================
 */
public class AttributeGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Direct reproduction of Defects4J AttributeTest::validatesKeysNotEmpty.
     * Creating an Attribute with whitespace-only key must throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void validatesKeysNotEmpty() {
        new Attribute(" ", "Check");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void validatesKeysNotEmptyWithMultipleWhitespaces() {
        new Attribute("   \t \n  ", "Value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void validatesKeysNotEmptyWithEmptyString() {
        new Attribute("", "Check");
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGettersAndSettersWithoutParent() {
        Attribute attr = new Attribute("  href  ", "https://jsoup.org");
        assertEquals("href", attr.getKey());
        assertEquals("https://jsoup.org", attr.getValue());

        attr.setKey("src");
        assertEquals("src", attr.getKey());
    }

    @Test(timeout = 4000)
    public void testSetKeyWithParentSynchronizesKey() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");
        Attribute attr = new Attribute("k1", "v1", parent);

        attr.setKey("k2");
        assertEquals("k2", attr.getKey());
        assertTrue(parent.hasKey("k2"));
        assertFalse(parent.hasKey("k1"));
    }

    @Test(timeout = 4000)
    public void testSetKeyWithParentWhenKeyNotFoundDoesNotThrow() {
        Attributes parent = new Attributes();
        parent.put("existing", "val");
        Attribute attr = new Attribute("orphan", "val", parent);

        attr.setKey("newOrphan");
        assertEquals("newOrphan", attr.getKey());
        assertFalse(parent.hasKey("newOrphan"));
    }

    @Test(timeout = 4000)
    public void testSetValueWithParentSynchronizesValue() {
        Attributes parent = new Attributes();
        parent.put("attrName", "initialVal");
        Attribute attr = new Attribute("attrName", "initialVal", parent);

        String oldVal = attr.setValue("updatedVal");
        assertEquals("initialVal", oldVal);
        assertEquals("updatedVal", attr.getValue());
        assertEquals("updatedVal", parent.get("attrName"));
    }

    @Test(timeout = 4000)
    public void testSetValueWithParentWhenKeyNotFound() {
        Attributes parent = new Attributes();
        Attribute attr = new Attribute("missingKey", "val", parent);

        String oldVal = attr.setValue("newVal");
        assertNull(oldVal);
        assertEquals("newVal", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("title", "&quot;Hello &amp; World&quot;");
        assertEquals("title", attr.getKey());
        assertEquals("\"Hello & World\"", attr.getValue());
        assertEquals("title=\"&quot;Hello &amp; World&quot;\"", attr.html());
    }

    @Test(timeout = 4000)
    public void testToStringMatchesHtml() {
        Attribute attr = new Attribute("class", "main active");
        assertEquals("class=\"main active\"", attr.toString());
        assertEquals(attr.html(), attr.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Boolean / Data Attributes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDataAttributeBoundaries() {
        assertTrue(Attribute.isDataAttribute("data-name"));
        assertTrue(Attribute.isDataAttribute("data-123"));
        assertTrue(new Attribute("data-attr", "value").isDataAttribute());

        // Exact boundary: "data-" has length 5, must be > 5
        assertFalse(Attribute.isDataAttribute("data-"));
        assertFalse(new Attribute("data-", "val").isDataAttribute());

        // Less than prefix length or different prefix
        assertFalse(Attribute.isDataAttribute("data"));
        assertFalse(Attribute.isDataAttribute("DATA-name")); // Case sensitive prefix
        assertFalse(Attribute.isDataAttribute("custom"));
    }

    @Test(timeout = 4000)
    public void testIsBooleanAttributeStatic() {
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("async"));
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        assertTrue(Attribute.isBooleanAttribute("typemustmatch"));

        assertFalse(Attribute.isBooleanAttribute("CHECKED")); // Arrays.binarySearch is case-sensitive
        assertFalse(Attribute.isBooleanAttribute("unknown"));
        assertFalse(Attribute.isBooleanAttribute(""));
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testIsBooleanAttributeInstanceDeprecated() {
        Attribute boolAttr = new Attribute("checked", "true");
        assertTrue(boolAttr.isBooleanAttribute());

        Attribute nullValAttr = new Attribute("custom", null);
        assertTrue(nullValAttr.isBooleanAttribute()); // returns true when val == null

        Attribute standardAttr = new Attribute("class", "my-class");
        assertFalse(standardAttr.isBooleanAttribute());
    }

    @Test(timeout = 4000)
    public void testShouldCollapseAttributeHtmlSyntax() {
        Document.OutputSettings htmlOut = new Document("").outputSettings().syntax(Document.OutputSettings.Syntax.html);

        // 1. val is null -> collapses
        Attribute attrNull = new Attribute("custom", null);
        assertTrue(attrNull.shouldCollapseAttribute(htmlOut));
        assertEquals("custom", attrNull.html());

        // 2. val is empty AND boolean attribute -> collapses
        Attribute attrEmptyBool = new Attribute("disabled", "");
        assertTrue(attrEmptyBool.shouldCollapseAttribute(htmlOut));
        assertEquals("disabled", attrEmptyBool.html());

        // 3. val matches name case-insensitively AND boolean attribute -> collapses
        Attribute attrSameName = new Attribute("required", "required");
        assertTrue(attrSameName.shouldCollapseAttribute(htmlOut));
        assertEquals("required", attrSameName.html());

        Attribute attrSameNameCase = new Attribute("required", "REQUIRED");
        assertTrue(attrSameNameCase.shouldCollapseAttribute(htmlOut));
        assertEquals("required", attrSameNameCase.html());

        // 4. val is empty BUT NOT a boolean attribute -> does NOT collapse
        Attribute attrEmptyNonBool = new Attribute("href", "");
        assertFalse(attrEmptyNonBool.shouldCollapseAttribute(htmlOut));
        assertEquals("href=\"\"", attrEmptyNonBool.html());

        // 5. val is different on boolean attribute -> does NOT collapse
        Attribute attrDiffBool = new Attribute("checked", "notchecked");
        assertFalse(attrDiffBool.shouldCollapseAttribute(htmlOut));
        assertEquals("checked=\"notchecked\"", attrDiffBool.html());
    }

    @Test(timeout = 4000)
    public void testShouldCollapseAttributeXmlSyntax() {
        Document.OutputSettings xmlOut = new Document("").outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // In XML mode, attributes never collapse
        Attribute attrNull = new Attribute("disabled", null);
        assertFalse(attrNull.shouldCollapseAttribute(xmlOut));
        StringBuilder sb1 = new StringBuilder();
        try {
            attrNull.html(sb1, xmlOut);
        } catch (IOException e) {
            fail("Should not throw IOException: " + e.getMessage());
        }
        assertEquals("disabled=\"\"", sb1.toString());

        Attribute attrEmptyBool = new Attribute("disabled", "");
        assertFalse(attrEmptyBool.shouldCollapseAttribute(xmlOut));
        StringBuilder sb2 = new StringBuilder();
        try {
            attrEmptyBool.html(sb2, xmlOut);
        } catch (IOException e) {
            fail("Should not throw IOException: " + e.getMessage());
        }
        assertEquals("disabled=\"\"", sb2.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void constructorThrowsOnNullKey() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void setKeyThrowsOnNullKey() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void setKeyThrowsOnEmptyKey() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void setKeyThrowsOnWhitespaceKey() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("   ");
    }

    @Test(timeout = 4000)
    public void htmlAppendableThrowsIOExceptionPropagates() {
        Attribute attr = new Attribute("k", "v");
        Appendable throwingAppendable = new Appendable() {
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
            attr.html(throwingAppendable, new Document("").outputSettings());
            fail("Expected IOException");
        } catch (IOException expected) {
            assertEquals("Simulated IO failure", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        Attribute a3 = new Attribute("key", "different");
        Attribute a4 = new Attribute("different", "val");
        Attribute a5 = new Attribute("key", null);
        Attribute a6 = new Attribute("key", null);

        // Reflexivity
        assertEquals(a1, a1);

        // Symmetry & Equality
        assertEquals(a1, a2);
        assertEquals(a2, a1);
        assertEquals(a1.hashCode(), a2.hashCode());

        // Null value equality
        assertEquals(a5, a6);
        assertEquals(a5.hashCode(), a6.hashCode());

        // Dissimilarity
        assertNotEquals(a1, a3);
        assertNotEquals(a1, a4);
        assertNotEquals(a1, a5);
        assertNotEquals(a5, a1);
        assertNotEquals(null, a1);
        assertNotEquals(a1, "NotAnAttribute");
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Attribute original = new Attribute("class", "primary");
        Attribute cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.hashCode(), cloned.hashCode());

        // Modifying cloned instance must not affect original
        cloned.setKey("id");
        assertEquals("class", original.getKey());
        assertEquals("id", cloned.getKey());
    }
}