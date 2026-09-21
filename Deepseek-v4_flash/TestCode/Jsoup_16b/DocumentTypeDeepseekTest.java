package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core Functional Logic: nodeName(), outerHtmlHead with no public/system, only public, only system, both.
 * - Partition B: Boundary Value Analysis: blank strings, null strings for name/publicId/systemId.
 * - Partition C: Defect-Targeted Branch Zone:
 *   1) outerHtmlHead when publicId is blank but systemId is non-blank: missing opening quote before systemId (Defects4J outerHtmlGeneration).
 *   2) Constructor does not throw IllegalArgumentException on blank name (Defects4J constructorValidationThrowsExceptionOnBlankName).
 * - Partition D: Exception & Defensive Guard Paths: blank name expected exception.
 * - Partition E: Object Lifecycle: not applicable (clone/serialization not required).
 * 
 * Coverage targets: all branches in outerHtmlHead (two if statements), nodeName, constructor attribute storage.
 */
public class DocumentTypeDeepseekTest {

    // Helper to generate outerHtml
    private String generateOuterHtml(DocumentType dt) {
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings settings = new Document("http://example.com").outputSettings();
        dt.outerHtmlHead(accum, 0, settings);
        dt.outerHtmlTail(accum, 0, settings);
        return accum.toString();
    }

    // ---- Partition A: Core Functional Logic ----

    @Test(timeout = 4000)
    public void testNodeName() {
        DocumentType dt = new DocumentType("html", "", "", "http://example.com");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadNoIds() {
        DocumentType dt = new DocumentType("html", "", "", "http://example.com");
        assertEquals("<!DOCTYPE html>", generateOuterHtml(dt));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadPublicOnly() {
        DocumentType dt = new DocumentType("html", "PUBLIC_ID", "", "http://example.com");
        assertEquals("<!DOCTYPE html PUBLIC \"PUBLIC_ID\">", generateOuterHtml(dt));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadBothIds() {
        DocumentType dt = new DocumentType("html", "PUB", "SYS", "http://example.com");
        assertEquals("<!DOCTYPE html PUBLIC \"PUB\" \"SYS\">", generateOuterHtml(dt));
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTailDoesNothing() {
        DocumentType dt = new DocumentType("html", "", "", "http://example.com");
        StringBuilder accum = new StringBuilder("prefix");
        Document.OutputSettings settings = new Document("http://example.com").outputSettings();
        dt.outerHtmlTail(accum, 0, settings);
        assertEquals("prefix", accum.toString());
    }

    // ---- Partition B: Boundary Value Analysis ----

    @Test(timeout = 4000)
    public void testNullArguments() {
        // Null name, publicId, systemId (Node may handle null)
        DocumentType dt = new DocumentType(null, null, null, "http://example.com");
        assertNotNull(dt);
        // nodeName still returns correct
        assertEquals("#doctype", dt.nodeName());
        // outerHtmlHead: blank check on null? StringUtil.isBlank(null) returns true
        assertEquals("<!DOCTYPE html>", generateOuterHtml(dt));
    }

    @Test(timeout = 4000)
    public void testBlankName() {
        // Blank name should ideally throw exception (see Partition C)
        // For coverage, ensure constructor does not crash (defect: no validation)
        DocumentType dt = new DocumentType(" ", "", "", "http://example.com");
        assertNotNull(dt);
    }

    // ---- Partition C: Defect-Targeted Branch Zone ----

    /**
     * Targets the known bug in outerHtmlHead: missing opening double quote before systemId
     * when publicId is blank but systemId is not.
     * Expected: "<!DOCTYPE html \"systemId\">"
     * Actual (defective): "<!DOCTYPE html systemId\">" (missing opening quote)
     */
    @Test(timeout = 4000)
    public void testOuterHtmlHeadSystemOnly_ShouldHaveOpeningQuote() {
        DocumentType dt = new DocumentType("html", "", "http://www.ibm.com/data", "http://example.com");
        String expected = "<!DOCTYPE html \"http://www.ibm.com/data\">";
        assertEquals(expected, generateOuterHtml(dt));
    }

    /**
     * Targets the known bug in constructor: does not throw IllegalArgumentException
     * when name is blank. The fixed version should throw.
     * This test will fail on the defective version because no exception is thrown.
     */
    @Test(timeout = 4000)
    public void testConstructorValidationThrowsExceptionOnBlankName() {
        // Expect exception for blank name (use try-catch to handle both versions)
        try {
            new DocumentType("", "", "", "http://example.com");
            fail("Expected IllegalArgumentException for blank name");
        } catch (IllegalArgumentException e) {
            // Expected: fixed version throws
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass());
        }

        // Also test with whitespace-only name
        try {
            new DocumentType("   ", "", "", "http://example.com");
            fail("Expected IllegalArgumentException for whitespace-only name");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass());
        }
    }

    // ---- Partition D: Exception & Defensive Guard Paths ----

    @Test(timeout = 4000)
    public void testOuterHtmlHeadPublicBlankSystemBlank() {
        DocumentType dt = new DocumentType("html", null, null, "http://example.com");
        assertEquals("<!DOCTYPE html>", generateOuterHtml(dt));
    }

    // ---- Additional coverage for branch with both ids ----

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithSpecialCharacters() {
        DocumentType dt = new DocumentType("html", "pub\"id", "sys", "http://example.com");
        // Note: quotes inside publicId may be escaped? Not required for this test.
        assertEquals("<!DOCTYPE html PUBLIC \"pub\"id\" \"sys\">", generateOuterHtml(dt));
    }
}