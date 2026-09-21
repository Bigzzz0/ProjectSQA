package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for XmlDeclaration.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * ---- getWholeDeclaration() ----
 * Branch 1: decl.equals("xml") && attributes.size() > 1   -> build version/encoding string
 *   - Sub-branch A: version != null  -> append version attribute
 *   - Sub-branch B: encoding != null -> append encoding attribute
 * Branch ELSE: return this.name
 *
 * ---- outerHtmlHead() ----
 * Branch A: isProcessingInstruction == true  -> append "!" + getWholeDeclaration() + ">"
 * Branch B: isProcessingInstruction == false -> append "?" + getWholeDeclaration() + ">"
 *   (KNOWN DEFECT: for false case, expected "?>" but actual ">")
 *
 * ---- Constructor ----
 * Validate.notNull(name) throws IllegalArgumentException if name is null
 *
 * ---- Boundary values ----
 * - name: null, empty, "xml", non-xml
 * - isProcessingInstruction: true, false
 * - attributes: size 0, 1, >1, version/encoding present/missing
 * - attributes with special characters (embedded quotes, spaces) – not explicitly required by defect but covered for robustness.
 */
public class XmlDeclarationDeepseekTest {

    // ---- Partition A: Core Functional Logic ----
    @Test(timeout = 4000)
    public void testNodeName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("#declaration", decl.nodeName());
    }

    @Test(timeout = 4000)
    public void testName() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        assertEquals("xml", decl.name());
        XmlDeclaration nonXml = new XmlDeclaration("foo", "http://example.com", false);
        assertEquals("foo", nonXml.name());
    }

    @Test(timeout = 4000)
    public void testToString() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        // Expected correct: <?xml version="1.0" encoding="UTF-8"?>
        // Known defect produces <?xml version="1.0" encoding="UTF-8"> (missing '?')
        assertTrue(decl.toString().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }

    // ---- Partition B: Boundary Value Analysis ----
    @Test(timeout = 4000)
    public void testEmptyName() {
        XmlDeclaration decl = new XmlDeclaration("", "http://example.com", false);
        assertEquals("", decl.name());
        assertEquals("", decl.getWholeDeclaration());
    }

    @Test(timeout = 4000)
    public void testNonXmlName() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("attr1", "val1");
        // getWholeDeclaration returns name only for non-"xml"
        assertEquals("custom", decl.getWholeDeclaration());
    }

    @Test(timeout = 4000)
    public void testXmlDeclNoAttributes() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        // attributes size <=1 -> fallback to name
        assertEquals("xml", decl.getWholeDeclaration());
    }

    @Test(timeout = 4000)
    public void testXmlDeclSingleAttribute() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        // size == 1, not >1 => fallback
        assertEquals("xml", decl.getWholeDeclaration());
    }

    @Test(timeout = 4000)
    public void testXmlDeclVersionOnly() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("other", "ignore");  // make size >1
        assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration().trim());
    }

    @Test(timeout = 4000)
    public void testXmlDeclEncodingOnly() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "UTF-8");
        decl.attr("other", "ignore");
        assertEquals("xml encoding=\"UTF-8\"", decl.getWholeDeclaration().trim());
    }

    @Test(timeout = 4000)
    public void testXmlDeclVersionAndEncoding() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        decl.attr("other", "ignore"); // ensure size >1
        String expected = "xml version=\"1.0\" encoding=\"UTF-8\"";
        assertEquals(expected, decl.getWholeDeclaration().trim());
    }

    @Test(timeout = 4000)
    public void testXmlDeclSpecialCharsInAttr() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "ISO-8859-1");
        decl.attr("standalone", "yes");
        String expected = "xml version=\"1.0\" encoding=\"ISO-8859-1\"";
        // Note: getWholeDeclaration only includes version and encoding, not standalone
        assertEquals(expected, decl.getWholeDeclaration().trim());
    }

    // ---- Partition C: Defect-Targeted Branch Zone ----
    @Test(timeout = 4000)
    public void testProcessingInstructionTrue() {
        // isProcessingInstruction=true should produce <!...>
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", true);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        String html = decl.outerHtml();
        // Expected: <!xml version="1.0" encoding="UTF-8">
        assertTrue(html.contains("<!xml version=\"1.0\" encoding=\"UTF-8\">"));
        // Ensure no '?' in output
        assertFalse(html.contains("?xml"));
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionFalse() {
        // This test directly targets the known defect: missing '?' before '>'
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        String html = decl.outerHtml();
        // Correct behavior: <?xml version="1.0" encoding="UTF-8"?>
        assertTrue("Expected closing '?>' in XML declaration",
                   html.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        // The buggy version would contain "<?xml version=\"1.0\" encoding=\"UTF-8\">"
        // This assertion will fail on the defective version.
    }

    @Test(timeout = 4000)
    public void testOuterHtmlNoAttributes() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        // No attributes -> getWholeDeclaration returns "xml"
        String html = decl.outerHtml();
        assertTrue(html.contains("<?xml>"));
        // Buggy would produce "<?xml>" for false? Actually for false, head appends "?" + "xml" + ">" = "<?xml>"
        // But correct for false should be "<?xml?>"
        // The defect is only when there are attributes? Actually outerHtmlHead always appends ">" regardless.
        // So for no attributes, output is "<?xml>" and expected? According to XML spec, a declaration must have attributes? 
        // Typically <?xml version="1.0"?>. But here we test that even trivial case gets correct closing.
        // The defect: missing "?" before ">". So "<?xml>" should be "<?xml?>". But is that the defect? 
        // The known failures all include attributes. But the bug is in outerHtmlHead: it always does ">" instead of "?>" for non-PI.
        // So for no attributes, it outputs "<?xml>" instead of "<?xml?>". We'll assert correct:
        // However, the getWholeDeclaration returns just "xml", so head produces "<?" + "xml" + ">" => "<?xml>".
        // Correct should be "<?xml?>". Let's capture that.
        assertTrue("Expected '?>' after declaration content",
                   html.contains("<?xml?>"));
    }

    // ---- Partition D: Exception & Defensive Guard Paths ----
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullName() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    @Test(timeout = 4000)
    public void testConstructorNotNullName() {
        // just ensure no exception for non-null
        new XmlDeclaration("valid", "http://example.com", true);
    }

    // Additional coverage: verify that setting attributes after construction affects output
    @Test(timeout = 4000)
    public void testAttributeAdditionAfterConstruction() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("xml", decl.getWholeDeclaration());  // fallback
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", decl.getWholeDeclaration().trim());
    }

    // ---- Partition E: Object Lifecycle & Contract Integrity (not overridden, but minimal) ----
    @Test(timeout = 4000)
    public void testEqualsHashCodeNotOverridden() {
        XmlDeclaration d1 = new XmlDeclaration("xml", "http://example.com", false);
        XmlDeclaration d2 = new XmlDeclaration("xml", "http://other.com", false);
        // Node equals is identity-based, so not equal
        assertNotEquals(d1, d2);
        assertNotEquals(d1.hashCode(), d2.hashCode());
    }
}