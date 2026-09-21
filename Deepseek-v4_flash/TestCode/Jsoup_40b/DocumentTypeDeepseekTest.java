package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced White-Box Test Suite for DocumentType.
 * Targets all branches, boundary conditions, and the known defect
 * where constructor rejects blank names (should allow).
 */
public class DocumentTypeDeepseekTest {

    /* ---------------------------------------------------------------
     * [Branch & Defect Analysis Matrix]
     * 
     * Branches in constructor:
     *   - Validate.notEmpty(name): empty/null → throw (defect: empty should be allowed)
     *   - attr("name", name) / attr("publicId", ...) / attr("systemId", ...) → normal
     * 
     * Branches in outerHtmlHead:
     *   - if (!StringUtil.isBlank(attr("name"))) : T/F
     *   - if (!StringUtil.isBlank(attr("publicId"))) : T/F
     *   - if (!StringUtil.isBlank(attr("systemId"))) : T/F
     *   - concatenation order and quotes
     * 
     * Defect-target: constructor must accept empty name (currently throws).
     * --------------------------------------------------------------- */

    // ==================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ==================================================================

    @Test(timeout = 4000)
    public void nodeNameIsAlwaysDoctype() {
        DocumentType dt = new DocumentType("html", "PUBLIC", "SYSTEM", "http://example.com");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test(timeout = 4000)
    public void outerHtmlWithAllFieldsNonBlank() {
        DocumentType dt = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd", "http://example.com");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        dt.outerHtmlHead(accum, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">",
                accum.toString());
    }

    @Test(timeout = 4000)
    public void outerHtmlOnlyName() {
        DocumentType dt = new DocumentType("html", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    @Test(timeout = 4000)
    public void outerHtmlOnlyPublicId() {
        DocumentType dt = new DocumentType("", "publicId", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE  PUBLIC \"publicId\">", accum.toString());
    }

    @Test(timeout = 4000)
    public void outerHtmlOnlySystemId() {
        DocumentType dt = new DocumentType("", "", "systemId", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE  \"systemId\">", accum.toString());
    }

    @Test(timeout = 4000)
    public void outerHtmlAllBlank() {
        DocumentType dt = new DocumentType("", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE>", accum.toString());
    }

    @Test(timeout = 4000)
    public void outerHtmlTailDoesNothing() {
        DocumentType dt = new DocumentType("html", "public", "system", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlTail(accum, 0, new Document.OutputSettings());
        assertEquals("", accum.toString());
    }

    // ==================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ==================================================================

    @Test(timeout = 4000)
    public void constructorAcceptsNameWithOnlyWhitespace() {
        // not empty but blank – should be allowed
        DocumentType dt = new DocumentType("   ", "public", "system", "http://example.com");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test(timeout = 4000)
    public void outerHtmlNameWithSpacesIsBlank() {
        DocumentType dt = new DocumentType("    ", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder();
        dt.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE>", accum.toString());
    }

    @Test(timeout = 4000)
    public void constructorRejectsNullName() {
        try {
            new DocumentType(null, "public", "system", "http://example.com");
            fail("Expected IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be empty"));
        }
    }

    @Test(timeout = 4000)
    public void publicIdNullAllowed() {
        // constructor only validates name, null publicId is stored as attribute "null"
        DocumentType dt = new DocumentType("html", null, "system", "http://example.com");
        assertEquals("null", dt.attr("publicId"));
    }

    @Test(timeout = 4000)
    public void systemIdNullAllowed() {
        DocumentType dt = new DocumentType("html", "public", null, "http://example.com");
        assertEquals("null", dt.attr("systemId"));
    }

    // ==================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ==================================================================

    /**
     * Defect known from Defects4J: constructor should accept empty name,
     * but throws IllegalArgumentException because of Validate.notEmpty.
     * This test triggers the bug on the defective version (throws) and
     * passes on the fixed version.
     */
    @Test(timeout = 4000)
    public void constructorAllowsBlankName() {
        // This call will throw on the defective version, causing test failure.
        DocumentType dt = new DocumentType("", "public", "system", "http://example.com");
        assertNotNull(dt);
        assertEquals("#doctype", dt.nodeName());
    }

    // ==================================================================
    // Partition D: Exception & Defensive Guard Paths (already covered)
    // ==================================================================

    // ==================================================================
    // Partition E: Object Lifecycle & Contract Integrity (minimal)
    // ==================================================================

    @Test(timeout = 4000)
    public void baseUriPreserved() {
        DocumentType dt = new DocumentType("html", "", "", "http://jsoup.org");
        assertEquals("http://jsoup.org", dt.baseUri());
    }

    @Test(timeout = 4000)
    public void attributesAccessible() {
        DocumentType dt = new DocumentType("html", "public", "system", "http://example.com");
        assertEquals("html", dt.attr("name"));
        assertEquals("public", dt.attr("publicId"));
        assertEquals("system", dt.attr("systemId"));
    }
}