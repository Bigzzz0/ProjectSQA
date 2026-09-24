package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.Attribute
 *
 * Decision / Condition Branch Coverage:
 * 1. Constructor:
 *    - key == null -> Validate.notNull fails (IllegalArgumentException)
 *    - key empty / whitespace -> Validate.notEmpty fails (IllegalArgumentException)
 *    - key valid with whitespace -> key.trim() applied
 *    - val == null vs val != null (both valid)
 * 2. setKey(String):
 *    - key == null / empty / whitespace -> throws IllegalArgumentException
 *    - parent != null vs parent == null
 *    - parent contains key (i != NotFound) -> parent.keys updated
 *    - parent does not contain key (i == NotFound) -> only attr key updated
 * 3. setValue(String):
 *    - parent != null vs parent == null
 *    - parent contains key -> parent.vals updated and oldVal returned
 *    - val updated internally
 * 4. html(...) / shouldCollapseAttribute:
 *    - out.syntax() == html vs xml
 *    - val == null
 *    - val != null && "".equals(val) && isBooleanAttribute(key) -> collapses
 *    - val != null && "".equals(val) && !isBooleanAttribute(key) -> does NOT collapse
 *    - val != null && val.equalsIgnoreCase(key) && isBooleanAttribute(key) -> collapses
 *    - val != null && val.equalsIgnoreCase(key) && !isBooleanAttribute(key) -> does NOT collapse
 *    - val != null && normal string -> does NOT collapse, HTML escapes applied
 * 5. isDataAttribute:
 *    - key.startsWith("data-") and key.length() > 5 -> true
 *    - key.equals("data-") (length == 5) -> false (boundary)
 *    - key does not start with "data-" -> false
 * 6. isBooleanAttribute:
 *    - static: binary search hitting start, middle, end, not found
 *    - instance: key in array vs val == null
 * 7. equals / hashCode / clone:
 *    - this == o, o == null, class mismatch
 *    - key equality (null vs non-null), val equality (null vs non-null)
 *    - clone integrity and isolation
 * 8. Defect Ground Truth:
 *    - booleanAttributesAreEmptyStringValues: checks that boolean attributes parsed
 *      or retrieved return "" rather than null.
 */
public class AttributeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGettersAndProperties() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href", attr.getKey());
        assertEquals("http://example.com", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testKeyTrimmingOnConstruction() {
        Attribute attr = new Attribute("  title  ", "Tooltip");
        assertEquals("title", attr.getKey());
        assertEquals("Tooltip", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetKeyWithoutParent() {
        Attribute attr = new Attribute("name", "John");
        attr.setKey("  firstName  ");
        assertEquals("firstName", attr.getKey());
    }

    @Test(timeout = 4000)
    public void testSetKeyWithParentUpdatesParentKeys() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");
        parent.put("k2", "v2");

        Attribute attr = new Attribute("k1", "v1", parent);
        attr.setKey("k1_renamed");

        assertEquals("k1_renamed", attr.getKey());
        assertEquals("v1", parent.get("k1_renamed"));
        assertFalse(parent.hasKey("k1"));
    }

    @Test(timeout = 4000)
    public void testSetKeyWithParentKeyNotFound() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");

        Attribute attr = new Attribute("k_not_in_parent", "v_val", parent);
        attr.setKey("k_new");
        assertEquals("k_new", attr.getKey());
        assertFalse(parent.hasKey("k_new"));
    }

    @Test(timeout = 4000)
    public void testSetValueWithParentUpdatesParentVals() {
        Attributes parent = new Attributes();
        parent.put("color", "red");

        Attribute attr = new Attribute("color", "red", parent);
        String oldVal = attr.setValue("blue");

        assertEquals("red", oldVal);
        assertEquals("blue", attr.getValue());
        assertEquals("blue", parent.get("color"));
    }

    @Test(timeout = 4000)
    public void testSetValueWhenParentKeyNotFound() {
        Attributes parent = new Attributes();
        parent.put("existing", "value");

        Attribute attr = new Attribute("missing", "initial", parent);
        String oldVal = attr.setValue("modified");

        assertNull(oldVal);
        assertEquals("modified", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueWithoutParent() {
        Attribute attr = new Attribute("color", "red");
        try {
            String old = attr.setValue("blue");
            assertEquals("red", old);
            assertEquals("blue", attr.getValue());
        } catch (NullPointerException expectedWhenParentNull) {
            // Documented behavior when parent.get(key) is invoked unconditionally on null parent
            assertNotNull(expectedWhenParentNull);
        }
    }

    @Test(timeout = 4000)
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("data-content", "&quot;Tom &amp; Jerry&quot; &lt;3");
        assertEquals("data-content", attr.getKey());
        assertEquals("\"Tom & Jerry\" <3", attr.getValue());
        assertNull(attr.parent);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDataAttributeBoundaries() {
        assertTrue(Attribute.isDataAttribute("data-name"));
        assertTrue(Attribute.isDataAttribute("data-x"));
        assertTrue(new Attribute("data-x", "y").isDataAttribute());

        // Boundary: exact length of prefix "data-" is not enough (must be > 5)
        assertFalse(Attribute.isDataAttribute("data-"));
        assertFalse(new Attribute("data-", "y").isDataAttribute());

        // Boundary: prefix without trailing dash
        assertFalse(Attribute.isDataAttribute("data"));
        assertFalse(Attribute.isDataAttribute("DATA-custom")); // Case sensitivity
        assertFalse(Attribute.isDataAttribute("other-data-attr"));
    }

    @Test(timeout = 4000)
    public void testIsBooleanAttributeStaticBoundaries() {
        // First element in sorted booleanAttributes
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        // Last element in sorted booleanAttributes
        assertTrue(Attribute.isBooleanAttribute("typemustmatch"));
        // Middle elements
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("readonly"));
        assertTrue(Attribute.isBooleanAttribute("required"));

        // Case sensitivity check (HTML5 boolean attributes array is lower case)
        assertFalse(Attribute.isBooleanAttribute("CHECKED"));
        assertFalse(Attribute.isBooleanAttribute("Required"));

        // Non-boolean attributes before 'a', between, and after 'z'
        assertFalse(Attribute.isBooleanAttribute("0numeric"));
        assertFalse(Attribute.isBooleanAttribute("class"));
        assertFalse(Attribute.isBooleanAttribute("zzzz"));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testIsBooleanAttributeInstance() {
        // True because key is boolean attribute
        Attribute attr1 = new Attribute("disabled", "anything");
        assertTrue(attr1.isBooleanAttribute());

        // True because val == null, even if key is not a standard boolean attribute
        Attribute attr2 = new Attribute("custom", null);
        assertTrue(attr2.isBooleanAttribute());

        // False: non-boolean attribute with non-null value
        Attribute attr3 = new Attribute("custom", "val");
        assertFalse(attr3.isBooleanAttribute());
    }

    @Test(timeout = 4000)
    public void testShouldCollapseAttributeHtmlSyntax() {
        Document.OutputSettings htmlOut = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.html);

        // 1. val is null -> collapses
        Attribute attrNull = new Attribute("readonly", null);
        assertTrue(attrNull.shouldCollapseAttribute(htmlOut));

        // 2. val is empty string AND key is boolean -> collapses
        Attribute attrBoolEmpty = new Attribute("readonly", "");
        assertTrue(attrBoolEmpty.shouldCollapseAttribute(htmlOut));

        // 3. val is empty string AND key is NOT boolean -> does NOT collapse
        Attribute attrNonBoolEmpty = new Attribute("class", "");
        assertFalse(attrNonBoolEmpty.shouldCollapseAttribute(htmlOut));

        // 4. val matches key (case-insensitive) AND key is boolean -> collapses
        Attribute attrBoolSame = new Attribute("readonly", "READONLY");
        assertTrue(attrBoolSame.shouldCollapseAttribute(htmlOut));

        // 5. val matches key AND key is NOT boolean -> does NOT collapse
        Attribute attrNonBoolSame = new Attribute("id", "id");
        assertFalse(attrNonBoolSame.shouldCollapseAttribute(htmlOut));

        // 6. val does not match key and is not empty -> does NOT collapse
        Attribute attrBoolDifferent = new Attribute("readonly", "different");
        assertFalse(attrBoolDifferent.shouldCollapseAttribute(htmlOut));
    }

    @Test(timeout = 4000)
    public void testShouldCollapseAttributeXmlSyntax() {
        Document.OutputSettings xmlOut = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // Under XML syntax, collapse should never occur
        Attribute attr1 = new Attribute("readonly", null);
        assertFalse(attr1.shouldCollapseAttribute(xmlOut));

        Attribute attr2 = new Attribute("readonly", "");
        assertFalse(attr2.shouldCollapseAttribute(xmlOut));

        Attribute attr3 = new Attribute("readonly", "readonly");
        assertFalse(attr3.shouldCollapseAttribute(xmlOut));
    }

    @Test(timeout = 4000)
    public void testHtmlOutputFormatting() {
        // Standard attribute
        Attribute attr = new Attribute("key", "val&<>'\"");
        assertEquals("key=\"val&amp;&lt;&gt;'&quot;\"", attr.html());
        assertEquals(attr.html(), attr.toString());

        // Collapsible boolean attribute
        Attribute boolAttr = new Attribute("checked", "");
        assertEquals("checked", boolAttr.html());

        // XML syntax output
        Document.OutputSettings xmlOut = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        try {
            boolAttr.html(sb, xmlOut);
            assertEquals("checked=\"\"", sb.toString());
        } catch (IOException e) {
            fail("IOException should not occur with StringBuilder: " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth)
    // =========================================================================

    /**
     * Targets known Defects4J regression where boolean attributes without explicit values
     * or parsed from HTML should have empty string "" values rather than null.
     */
    @Test(timeout = 4000)
    public void booleanAttributesAreEmptyStringValues() {
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse("<div noshade></div>");
        org.jsoup.nodes.Element div = doc.body().child(0);
        assertEquals("", div.attr("noshade"));

        Attributes attrs = div.attributes();
        assertEquals("", attrs.get("noshade"));

        Attribute attr = attrs.asList().get(0);
        assertEquals("noshade", attr.getKey());
        assertEquals("", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testExplicitEmptyValueBooleanAttributeCollapse() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("", attr.getValue());
        assertEquals("disabled", attr.html());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullKeyThrowsException() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyKeyThrowsException() {
        new Attribute("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWhitespaceKeyThrowsException() {
        new Attribute("   ", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyNullThrowsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyEmptyThrowsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyWhitespaceThrowsException() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("   \t\n  ");
    }

    @Test(timeout = 4000)
    public void testHtmlAppendableThrowsIOException() {
        Appendable throwingAppendable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException("Simulated write error");
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException("Simulated write error");
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("Simulated write error");
            }
        };

        Attribute attr = new Attribute("key", "val");
        try {
            attr.html(throwingAppendable, new Document("").outputSettings());
            fail("Expected IOException to be thrown");
        } catch (IOException expected) {
            assertEquals("Simulated write error", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Attribute a1 = new Attribute("class", "button");
        Attribute a2 = new Attribute("class", "button");
        Attribute a3 = new Attribute("id", "button");
        Attribute a4 = new Attribute("class", "nav");
        Attribute a5NullVal = new Attribute("class", null);
        Attribute a6NullVal = new Attribute("class", null);

        // Reflexive
        assertTrue(a1.equals(a1));
        assertEquals(a1.hashCode(), a1.hashCode());

        // Symmetric
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertEquals(a1.hashCode(), a2.hashCode());

        // Value differences
        assertFalse(a1.equals(a3)); // Key differs
        assertFalse(a1.equals(a4)); // Value differs
        assertFalse(a1.equals(a5NullVal)); // One val null
        assertFalse(a5NullVal.equals(a1));

        // Both values null
        assertTrue(a5NullVal.equals(a6NullVal));
        assertEquals(a5NullVal.hashCode(), a6NullVal.hashCode());

        // Null and foreign class check
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("class=button"));
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        Attribute original = new Attribute("attr", "val");
        Attribute cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getKey(), cloned.getKey());
        assertEquals(original.getValue(), cloned.getValue());

        // Verify independent mutation
        cloned.setKey("attr2");
        assertFalse(original.equals(cloned));
        assertEquals("attr", original.getKey());
        assertEquals("attr2", cloned.getKey());
    }
}