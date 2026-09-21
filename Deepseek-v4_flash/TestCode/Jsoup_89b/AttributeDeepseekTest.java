package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: org.jsoup.nodes.Attribute
     * 
     * Key Branches & Conditions:
     * 1. Constructor: Validate.notNull(key) -> NPE if null; key.trim(); Validate.notEmpty(key) -> IAE if empty/whitespace
     * 2. setKey: Validate.notNull(key) -> NPE; trim; Validate.notEmpty -> IAE; parent != null branch; parent.indexOfKey != NotFound branch
     * 3. setValue: parent.get(this.key) called BEFORE null check on parent -> NPE when parent == null (THE DEFECT)
     * 4. getValue: Attributes.checkNotNull(val) -> NPE if val is null
     * 5. shouldCollapseAttribute: out.syntax() == html; val == null; "".equals(val); val.equalsIgnoreCase(key); isBooleanAttribute(key)
     * 6. isBooleanAttribute (instance): Arrays.binarySearch >= 0 || val == null
     * 7. isBooleanAttribute (static): Arrays.binarySearch >= 0
     * 8. isDataAttribute: key.startsWith(dataPrefix) && key.length() > dataPrefix.length()
     * 9. equals: this == o; o == null; getClass() != o.getClass(); key null checks; val null checks
     * 10. hashCode: key != null ? key.hashCode() : 0; val != null ? val.hashCode() : 0
     * 11. clone: super.clone() -> CloneNotSupportedException catch
     * 12. html(): StringUtil.borrowBuilder; html(sb, out); IOException catch -> SerializationException
     * 13. createFromEncoded: Entities.unescape(encodedValue, true)
     * 
     * Defect Targeted (settersOnOrphanAttribute):
     * - setValue() calls parent.get(this.key) BEFORE checking if parent is null
     * - When an Attribute is created without a parent (orphan), setValue() throws NullPointerException
     * - Expected: setValue() should work on orphan attributes (return old value, update val)
     * - The bug is in the order of operations: parent.get() is called before the null check
     * 
     * Test Strategy:
     * - Partition A: Core functional tests (getKey, getValue, setKey, setValue on parented attributes)
     * - Partition B: Boundary tests (null/empty keys, null values, boolean attributes)
     * - Partition C: Defect-targeted test for orphan setValue (the known defect)
     * - Partition D: Exception tests (null key in constructor, empty key after trim)
     * - Partition E: equals/hashCode/clone/html tests
     */

    // ==================== PARTITION A: CORE FUNCTIONAL LOGIC ====================

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        Attribute attr = new Attribute("key", "value");
        assertEquals("key", attr.getKey());
        assertEquals("value", attr.getValue());
        assertEquals("key=\"value\"", attr.html());
        assertEquals("key=\"value\"", attr.toString());
    }

    @Test(timeout = 4000)
    public void testSetKeyOnParentedAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        Attribute attr = attrs.get("key");
        
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
        assertEquals("newKey", attrs.get("newKey").getKey());
        assertNull(attrs.get("key")); // old key removed
    }

    @Test(timeout = 4000)
    public void testSetValueOnParentedAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("key", "oldValue");
        Attribute attr = attrs.get("key");
        
        String oldVal = attr.setValue("newValue");
        assertEquals("oldValue", oldVal);
        assertEquals("newValue", attr.getValue());
        assertEquals("newValue", attrs.get("key").getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueReturnsOldValue() {
        Attribute attr = new Attribute("key", "old");
        String oldVal = attr.setValue("new");
        assertEquals("old", oldVal);
        assertEquals("new", attr.getValue());
    }

    // ==================== PARTITION B: BOUNDARY VALUE ANALYSIS ====================

    @Test(timeout = 4000)
    public void testNullValue() {
        Attribute attr = new Attribute("key", null);
        assertNull(attr.getValue()); // checkNotNull returns null for null input
        assertEquals("key", attr.html()); // boolean-like collapse for null value
    }

    @Test(timeout = 4000)
    public void testEmptyValue() {
        Attribute attr = new Attribute("key", "");
        assertEquals("", attr.getValue());
        assertEquals("key=\"\"", attr.html());
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeWithEmptyValue() {
        Attribute attr = new Attribute("disabled", "");
        assertEquals("disabled", attr.html()); // collapses to just key
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeWithSameValue() {
        Attribute attr = new Attribute("disabled", "disabled");
        assertEquals("disabled", attr.html()); // collapses
    }

    @Test(timeout = 4000)
    public void testBooleanAttributeWithDifferentValue() {
        Attribute attr = new Attribute("disabled", "true");
        assertEquals("disabled=\"true\"", attr.html());
    }

    @Test(timeout = 4000)
    public void testNonBooleanAttributeWithEmptyValue() {
        Attribute attr = new Attribute("class", "");
        assertEquals("class=\"\"", attr.html()); // does not collapse
    }

    @Test(timeout = 4000)
    public void testKeyWithWhitespace() {
        Attribute attr = new Attribute("  key  ", "value");
        assertEquals("key", attr.getKey()); // trimmed
    }

    @Test(timeout = 4000)
    public void testDataAttribute() {
        Attribute attr = new Attribute("data-custom", "value");
        assertTrue(attr.isDataAttribute());
        
        Attribute nonData = new Attribute("custom", "value");
        assertFalse(nonData.isDataAttribute());
    }

    @Test(timeout = 4000)
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("key", "value&amp;");
        assertEquals("key", attr.getKey());
        assertEquals("value&", attr.getValue());
    }

    // ==================== PARTITION C: DEFECT-TARGETED BRANCH ZONE ====================

    /**
     * DEFECT: settersOnOrphanAttribute
     * 
     * When setValue() is called on an Attribute with no parent (orphan),
     * the method calls parent.get(this.key) BEFORE checking if parent is null.
     * This causes a NullPointerException.
     * 
     * Expected behavior: setValue() should work on orphan attributes,
     * returning the old value and updating the internal val field.
     */
    @Test(timeout = 4000)
    public void testSetValueOnOrphanAttribute() {
        Attribute attr = new Attribute("key", "oldValue");
        assertNull(attr.parent); // verify orphan
        
        // This should NOT throw NPE - it should work like a normal setValue
        String oldVal = attr.setValue("newValue");
        
        assertEquals("oldValue", oldVal);
        assertEquals("newValue", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetValueOnOrphanAttributeWithNullOldValue() {
        Attribute attr = new Attribute("key", null);
        
        String oldVal = attr.setValue("newValue");
        
        assertNull(oldVal);
        assertEquals("newValue", attr.getValue());
    }

    @Test(timeout = 4000)
    public void testSetKeyOnOrphanAttribute() {
        Attribute attr = new Attribute("oldKey", "value");
        attr.setKey("newKey");
        assertEquals("newKey", attr.getKey());
        assertEquals("value", attr.getValue());
    }

    // ==================== PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullKey() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithEmptyKey() {
        new Attribute("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithWhitespaceOnlyKey() {
        new Attribute("   ", "value");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSetKeyWithNull() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyWithEmpty() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetKeyWithWhitespaceOnly() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("   ");
    }

    @Test(timeout = 4000)
    public void testHtmlWithSpecialCharacters() {
        Attribute attr = new Attribute("key", "va\"lue&<>");
        String html = attr.html();
        assertTrue(html.contains("va&quot;lue&amp;&lt;&gt;"));
    }

    // ==================== PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "other");
        Attribute attr4 = new Attribute("other", "value");
        
        assertEquals(attr1, attr1); // same object
        assertEquals(attr1, attr2); // equal values
        assertNotEquals(attr1, attr3); // different value
        assertNotEquals(attr1, attr4); // different key
        assertNotEquals(attr1, null); // null
        assertNotEquals(attr1, "string"); // different type
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullValues() {
        Attribute attr1 = new Attribute("key", null);
        Attribute attr2 = new Attribute("key", null);
        Attribute attr3 = new Attribute("key", "value");
        
        assertEquals(attr1, attr2);
        assertNotEquals(attr1, attr3);
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "other");
        
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1.hashCode(), attr3.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullValue() {
        Attribute attr1 = new Attribute("key", null);
        Attribute attr2 = new Attribute("key", null);
        
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Attribute attr = new Attribute("key", "value");
        Attribute clone = attr.clone();
        
        assertNotSame(attr, clone);
        assertEquals(attr, clone);
        assertEquals(attr.getKey(), clone.getKey());
        assertEquals(attr.getValue(), clone.getValue());
    }

    @Test(timeout = 4000)
    public void testCloneWithNullValue() {
        Attribute attr = new Attribute("key", null);
        Attribute clone = attr.clone();
        
        assertEquals(attr, clone);
        assertNull(clone.getValue());
    }

    @Test(timeout = 4000)
    public void testHtmlMethod() {
        Attribute attr = new Attribute("key", "value");
        assertEquals("key=\"value\"", attr.html());
    }

    @Test(timeout = 4000)
    public void testHtmlWithNullValue() {
        Attribute attr = new Attribute("disabled", null);
        assertEquals("disabled", attr.html());
    }

    @Test(timeout = 4000)
    public void testShouldCollapseAttribute() {
        Document.OutputSettings htmlOut = new Document.OutputSettings();
        htmlOut.syntax(Document.OutputSettings.Syntax.html);
        
        Document.OutputSettings xmlOut = new Document.OutputSettings();
        xmlOut.syntax(Document.OutputSettings.Syntax.xml);
        
        // Boolean attribute with null value in HTML
        assertTrue(Attribute.shouldCollapseAttribute("disabled", null, htmlOut));
        // Boolean attribute with empty value in HTML
        assertTrue(Attribute.shouldCollapseAttribute("disabled", "", htmlOut));
        // Boolean attribute with same value in HTML
        assertTrue(Attribute.shouldCollapseAttribute("disabled", "disabled", htmlOut));
        // Boolean attribute with different value in HTML
        assertFalse(Attribute.shouldCollapseAttribute("disabled", "true", htmlOut));
        // Non-boolean attribute with empty value in HTML
        assertFalse(Attribute.shouldCollapseAttribute("class", "", htmlOut));
        // Boolean attribute in XML mode
        assertFalse(Attribute.shouldCollapseAttribute("disabled", "", xmlOut));
    }

    @Test(timeout = 4000)
    public void testIsBooleanAttribute() {
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertFalse(Attribute.isBooleanAttribute("class"));
        assertFalse(Attribute.isBooleanAttribute(""));
    }

    @Test(timeout = 4000)
    public void testInstanceIsBooleanAttribute() {
        Attribute boolAttr = new Attribute("disabled", null);
        assertTrue(boolAttr.isBooleanAttribute());
        
        Attribute nonBoolAttr = new Attribute("class", "value");
        assertFalse(nonBoolAttr.isBooleanAttribute());
    }

    @Test(timeout = 4000)
    public void testIsDataAttributeStatic() {
        assertTrue(Attribute.isDataAttribute("data-custom"));
        assertFalse(Attribute.isDataAttribute("custom"));
        assertFalse(Attribute.isDataAttribute("data")); // exactly "data" is not a data attribute
    }

    @Test(timeout = 4000)
    public void testParentUpdateOnSetKey() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        Attribute attr = attrs.get("key");
        
        attr.setKey("newKey");
        
        assertNull(attrs.get("key"));
        assertEquals("value", attrs.get("newKey").getValue());
    }

    @Test(timeout = 4000)
    public void testParentUpdateOnSetValue() {
        Attributes attrs = new Attributes();
        attrs.put("key", "oldValue");
        Attribute attr = attrs.get("key");
        
        attr.setValue("newValue");
        
        assertEquals("newValue", attrs.get("key").getValue());
    }
}