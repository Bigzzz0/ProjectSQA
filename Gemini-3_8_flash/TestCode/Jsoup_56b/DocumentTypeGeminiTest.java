package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.nodes.DocumentType
 *
 * 1. Defect Specification (Defects4J):
 *    - In outerHtmlHead(), when 'systemId' is present and 'publicId' is absent/blank,
 *      the output should include the "SYSTEM " keyword preceding systemId:
 *      `<!DOCTYPE html SYSTEM "exampledtdfile.dtd">`.
 *      The defective code emits `<!DOCTYPE html "exampledtdfile.dtd">` omitting "SYSTEM ".
 *
 * 2. Decision Logic & Branch Coverage:
 *    - Syntax & ID Decision:
 *      * (syntax == Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID)) -> lowercase "<!doctype"
 *      * (syntax == Syntax.xml) -> uppercase "<!DOCTYPE"
 *      * (syntax == Syntax.html && has(PUBLIC_ID)) -> uppercase "<!DOCTYPE"
 *      * (syntax == Syntax.html && has(SYSTEM_ID)) -> uppercase "<!DOCTYPE"
 *    - Attribute Emission Branches:
 *      * has(NAME) == true / false
 *      * has(PUBLIC_ID) == true / false
 *      * has(SYSTEM_ID) == true / false
 *    - Combination Matrix for (PUBLIC_ID, SYSTEM_ID):
 *      * (null/blank, null/blank) -> HTML5 doctype
 *      * (set, null/blank) -> PUBLIC doctype with no system id
 *      * (set, set) -> PUBLIC doctype with system id
 *      * (null/blank, set) -> SYSTEM doctype (Bug Triggering Path)
 *    - Node Lifecycle:
 *      * nodeName() contract: returns "#doctype"
 *      * outerHtmlTail(): no-op execution verification
 */
public class DocumentTypeGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug Trigger)
    // =========================================================================

    /**
     * Targets the defect where systemId is supplied without a publicId.
     * The specification requires: `<!DOCTYPE html SYSTEM "example.dtd">`.
     * Defective implementation outputs: `<!DOCTYPE html "example.dtd">`.
     */
    @Test(timeout = 4000)
    public void testSystemIdWithoutPublicIdIncludesSystemKeyword() {
        DocumentType dt = new DocumentType("html", "", "exampledtdfile.dtd", "http://example.com");
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", dt.outerHtml());
    }

    /**
     * Targets the defect variant when publicId is null and systemId is present.
     */
    @Test(timeout = 4000)
    public void testSystemIdWithNullPublicIdIncludesSystemKeyword() {
        DocumentType dt = new DocumentType("html", null, "system.dtd", "");
        assertEquals("<!DOCTYPE html SYSTEM \"system.dtd\">", dt.outerHtml());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeName() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test(timeout = 4000)
    public void testHtml5DocTypeOutput() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("<!doctype html>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testPublicAndSystemIdPresent() {
        DocumentType dt = new DocumentType(
            "html",
            "-//W3C//DTD XHTML 1.0 Strict//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
            ""
        );
        assertEquals(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
            dt.outerHtml()
        );
    }

    @Test(timeout = 4000)
    public void testPublicIdPresentWithoutSystemId() {
        DocumentType dt = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testXmlSyntaxForcesUppercaseDocType() {
        DocumentType dt = new DocumentType("html", "", "", "");
        OutputSettings settings = new OutputSettings().syntax(Syntax.xml);

        StringBuilder sb = new StringBuilder();
        try {
            dt.outerHtmlHead(sb, 0, settings);
            dt.outerHtmlTail(sb, 0, settings);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }

        assertEquals("<!DOCTYPE html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testXmlSyntaxWithPublicAndSystemId() {
        DocumentType dt = new DocumentType("svg", "-//W3C//DTD SVG 1.1//EN", "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd", "");
        OutputSettings settings = new OutputSettings().syntax(Syntax.xml);

        StringBuilder sb = new StringBuilder();
        try {
            dt.outerHtmlHead(sb, 0, settings);
            dt.outerHtmlTail(sb, 0, settings);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }

        assertEquals("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">", sb.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyNameDoctype() {
        DocumentType dt = new DocumentType("", "", "", "");
        assertEquals("<!doctype>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testNullNameDoctype() {
        DocumentType dt = new DocumentType(null, null, null, "");
        assertEquals("<!doctype>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyAttributesAreTreatedAsBlank() {
        DocumentType dt = new DocumentType("   ", "   ", "   ", "");
        // Blank name, publicId, and systemId should behave like empty strings
        assertEquals("<!doctype>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testBaseUriPreserved() {
        String base = "https://jsoup.org/spec/";
        DocumentType dt = new DocumentType("html", "", "", base);
        assertEquals(base, dt.baseUri());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testOuterHtmlTailDoesNotModifyAccumulator() {
        DocumentType dt = new DocumentType("html", "", "", "");
        OutputSettings settings = new OutputSettings();
        StringBuilder sb = new StringBuilder("preset");
        dt.outerHtmlTail(sb, 0, settings);
        assertEquals("preset", sb.toString());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPublicConstantKeysIntegrity() {
        assertEquals("PUBLIC", DocumentType.PUBLIC_KEY);
        assertEquals("SYSTEM", DocumentType.SYSTEM_KEY);
    }

    @Test(timeout = 4000)
    public void testAttributesAccessViaNodeMethods() {
        DocumentType dt = new DocumentType("html", "pubVal", "sysVal", "http://base.org");
        assertEquals("html", dt.attr("name"));
        assertEquals("pubVal", dt.attr("publicId"));
        assertEquals("sysVal", dt.attr("systemId"));
    }
}