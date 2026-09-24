package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: org.jsoup.nodes.DocumentType
 *
 * Decision / Branch Matrix:
 * 1. Constructor:
 *    - name validation: null / empty / non-empty [DEFECT TARGET: constructorValidationThrowsExceptionOnBlankName]
 *    - baseUri, publicId, systemId assignment
 * 2. outerHtmlHead(StringBuilder, int, Document.OutputSettings):
 *    - publicId: isBlank() == true vs false
 *    - systemId: isBlank() == true vs false
 *      [DEFECT TARGET: outerHtmlGeneration: systemId missing leading quote when rendered -> appends ' ' instead of ' "']
 *    - Combinations:
 *        a) publicId blank, systemId blank
 *        b) publicId present, systemId blank
 *        c) publicId present, systemId present
 *        d) publicId blank, systemId present (triggers systemId quoting bug)
 * 3. outerHtmlTail(StringBuilder, int, Document.OutputSettings):
 *    - No-op verification (accum unchanged)
 * 4. nodeName():
 *    - Returns "#doctype"
 * 5. Node lifecycle & clone integrity:
 *    - Clone copies attributes correctly
 */
public class DocumentTypeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeNameAlwaysReturnsDoctypeTag() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHtml5SimpleDoctype() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithPublicAndSystemIds() {
        DocumentType dt = new DocumentType(
            "html",
            "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd",
            "http://example.com"
        );
        assertEquals("html", dt.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", dt.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", dt.attr("systemId"));
        assertEquals("http://example.com", dt.baseUri());

        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        assertEquals(expected, dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlWithPublicIdOnly() {
        DocumentType dt = new DocumentType("html", "-//W3C//DTD HTML 4.01 Transitional//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTailDoesNotModifyAccumulator() {
        DocumentType dt = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder("preexisting");
        Document.OutputSettings settings = new Document.OutputSettings();
        dt.outerHtmlTail(accum, 0, settings);
        assertEquals("preexisting", accum.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testOuterHtmlWithWhitespaceOnlyIdsTreatedAsBlank() {
        DocumentType dt = new DocumentType("html", "   ", "\t\n ", "");
        assertEquals("<!DOCTYPE html>", dt.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadDirectCall() {
        DocumentType dt = new DocumentType("html", "pub", "sys", "");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        dt.outerHtmlHead(accum, 10, settings);
        assertTrue(accum.toString().startsWith("<!DOCTYPE html"));
        assertTrue(accum.toString().endsWith(">"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Bugs)
    // =========================================================================

    /**
     * Target Defect 1:
     * When systemId is present and publicId is blank, outerHtmlHead previously produced
     * <!DOCTYPE html http://www.ibm.com/dtd/html/"> instead of quoting the systemId
     * with an opening quote: <!DOCTYPE html "http://www.ibm.com/dtd/html/">.
     */
    @Test(timeout = 4000)
    public void testOuterHtmlWithSystemIdOnlyIncludesOpeningQuote() {
        DocumentType dt = new DocumentType("html", "", "http://www.ibm.com/dtd/html/", "");
        String expected = "<!DOCTYPE html \"http://www.ibm.com/dtd/html/\">";
        assertEquals(expected, dt.outerHtml());
    }

    /**
     * Target Defect