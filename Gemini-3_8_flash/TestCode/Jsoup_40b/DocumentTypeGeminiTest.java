/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.DocumentType
 *
 * Decision / Condition Matrix:
 * 1. Constructor:
 *    - name: non-empty, empty string (""), blank string ("   "), null
 *      * DEFECT: Validate.notEmpty(name) fails on empty/blank name, despite outerHtmlHead explicitly handling blank names.
 *    - publicId: non-empty, empty, blank, null
 *    - systemId: non-empty, empty, blank, null
 *    - baseUri: valid URI, empty, null
 *
 * 2. outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out):
 *    - Branch: !StringUtil.isBlank(attr("name"))
 *      * True: appends " " + name
 *      * False: skips appending name
 *    - Branch: !StringUtil.isBlank(attr("publicId"))
 *      * True: appends " PUBLIC \"" + publicId + "\""
 *      * False: skips public identifier clause
 *    - Branch: !StringUtil.isBlank(attr("systemId"))
 *      * True: appends " \"" + systemId + "\""
 *      * False: skips system identifier clause
 *
 * 3. outerHtmlTail:
 *    - No-op implementation; verify it leaves StringBuilder unmodified.
 *
 * 4. nodeName:
 *    - Returns "#doctype".
 */

package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardHtml5Doctype() {
        DocumentType dt = new DocumentType("html", "", "", "http://example.com/");
        assertEquals("#doctype", dt.nodeName());
        assertEquals("html", dt.attr("name"));
        assertEquals("", dt.attr("publicId"));
        assertEquals("", dt.attr("systemId"));
        assertEquals("http://example.com/", dt.baseUri());
        assertEquals("<!DOCTYPE html>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testFullDoctypeWithPublicAndSystemIds() {
        DocumentType dt = new DocumentType(
                "html",
                "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd",
                "http://example.com"
        );
        assertEquals("html", dt.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", dt.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", dt.attr("systemId"));

        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        assertEquals(expected, dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithOnlySystemId() {
        DocumentType dt = new DocumentType("html", "", "about:legacy-compat", "");
        assertEquals("html", dt.attr("name"));
        assertEquals("", dt.attr("publicId"));
        assertEquals("about:legacy-compat", dt.attr("systemId"));
        assertEquals("<!DOCTYPE html \"about:legacy-compat\">", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypeWithOnlyPublicId() {
        DocumentType dt = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("html", dt.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", dt.attr("publicId"));
        assertEquals("", dt.attr("systemId"));
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", dt.outerHtml());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBlankPublicAndSystemIdsIgnoredInOutput() {
        DocumentType dt = new DocumentType("html", "   ", "   ", "");
        // Blank attributes should not trigger PUBLIC or system id output
        assertEquals("<!DOCTYPE html>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDirectOuterHtmlHeadAndTailInvocations() {
        DocumentType dt = new DocumentType("html", "pub", "sys", "");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();

        dt.outerHtmlHead(accum, 0, settings);
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", accum.toString());

        // outerHtmlTail is a no-op; should not append anything
        dt.outerHtmlTail(accum, 0, settings);
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", accum.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeStateModifications() {
        DocumentType dt = new DocumentType("html", "", "", "");
        dt.attr("name", "xml");
        dt.attr("publicId", "pubId");
        dt.attr("systemId", "sysId");

        assertEquals("xml", dt.attr("name"));
        assertEquals("pubId", dt.attr("publicId"));
        assertEquals("sysId", dt.attr("systemId"));
        assertEquals("<!DOCTYPE xml PUBLIC \"pubId\" \"sysId\">", dt.outerHtml());

        dt.attr("name", "");
        assertEquals("<!DOCTYPE PUBLIC \"pubId\" \"sysId\">", dt.outerHtml());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: DocumentType constructor throws IllegalArgumentException
     * when name is empty or blank, even though HTML doctypes may be anonymous (<!DOCTYPE>)
     * and outerHtmlHead explicitly supports blank name.
     */
    @Test(timeout = 4000)
    public void constructorValidationOkWithBlankName() {
        DocumentType dt = new DocumentType("", "", "", "");
        assertEquals("", dt.attr("name"));
        assertEquals("<!DOCTYPE>", dt.outerHtml());
    }

    /**
     * Targets known defect: White-space blank doctype name should also be accepted
     * rather than failing constructor validation.
     */
    @Test(timeout = 4000)
    public void constructorValidationOkWithWhitespaceName() {
        DocumentType dt = new DocumentType("   ", "", "", "");
        assertEquals("   ", dt.attr("name"));
        assertEquals("<!DOCTYPE>", dt.outerHtml());
    }

    /**
     * Targets known defect: Blank name with valid public and system IDs.
     */
    @Test(timeout = 4000)
    public void constructorValidationOkWithBlankNameAndValidIds() {
        DocumentType dt = new DocumentType("", "public_val", "system_val", "http://base.org");
        assertEquals("", dt.attr("name"));
        assertEquals("<!DOCTYPE PUBLIC \"public_val\" \"system_val\">", dt.outerHtml());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullBaseUriThrowsException() {
        new DocumentType("html", "publicId", "systemId", null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringMatchesOuterHtml() {
        DocumentType dt = new DocumentType("html", "public", "system", "http://example.com");
        assertEquals(dt.outerHtml(), dt.toString());
    }

    @Test(timeout = 4000)
    public void testNodeNameInvariant() {
        DocumentType dt = new DocumentType("HTML", "pub", "sys", "");
        assertEquals("#doctype", dt.nodeName());
    }
}