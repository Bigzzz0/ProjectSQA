package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.jsoup.nodes.DocumentType
 * 
 * Decision branches in outerHtmlHead():
 * 1. out.syntax() == Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID) -> true/false
 * 2. has(NAME) -> true/false
 * 3. has(PUBLIC_ID) -> true/false
 * 4. has(SYSTEM_ID) -> true/false
 * 
 * has() method branches:
 * - StringUtil.isBlank(attr(attribute)) -> true/false for each attribute
 * 
 * Boundary values:
 * - null vs empty string vs whitespace-only vs non-blank for name/publicId/systemId
 * - Syntax.html vs Syntax.xml for output settings
 * - Combinations of presence/absence of attributes
 * 
 * Known defect (from Defects4J):
 * - testRoundTrip: expected "<!DOCTYPE html [SYSTEM ]"exampledtdfile.dtd"...>" 
 *   but was "<!DOCTYPE html []"exampledtdfile.dtd"...>"
 * - Root cause: When SYSTEM_ID is present but PUBLIC_ID is absent, the SYSTEM keyword
 *   is not emitted. The condition `if (has(PUBLIC_ID))` only emits "PUBLIC" when publicId
 *   exists, but when only systemId exists, it should emit "SYSTEM" keyword before the systemId.
 *   The bug is in outerHtmlHead(): missing else-if branch for SYSTEM_ID without PUBLIC_ID.
 * 
 * Test strategy:
 * - Partition A: Core functional - normal doctypes with various attribute combinations
 * - Partition B: Boundary - null, empty, whitespace, special characters
 * - Partition C: Defect-targeted - systemId without publicId (triggers the bug)
 * - Partition D: Exception/guard - null baseUri, IOException propagation
 * - Partition E: Contract - nodeName, toString, outerHtml output
 */
public class DocumentTypeDeepseekTest {

    /* ==================== Partition A: Core Functional Logic ==================== */

    @Test(timeout = 4000)
    public void testNodeName() {
        DocumentType doctype = new DocumentType("html", null, null, "http://example.com");
        assertEquals("#doctype", doctype.nodeName());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadHtml5DoctypeNoAttributes() throws Exception {
        DocumentType doctype = new DocumentType("html", null, null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadXmlSyntaxWithName() throws Exception {
        DocumentType doctype = new DocumentType("HTML", null, null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE HTML>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithPublicIdOnly() throws Exception {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0//EN", null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0//EN\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithSystemIdOnly() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "exampledtdfile.dtd", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        // Expected correct behavior: SYSTEM keyword should appear before systemId
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithBothPublicAndSystemId() throws Exception {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithAllAttributes() throws Exception {
        DocumentType doctype = new DocumentType("html", "public-id", "system-id", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\" \"system-id\">", sb.toString());
    }

    /* ==================== Partition B: Boundary Value Analysis ==================== */

    @Test(timeout = 4000)
    public void testNullName() throws Exception {
        DocumentType doctype = new DocumentType(null, null, null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyName() throws Exception {
        DocumentType doctype = new DocumentType("", null, null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWhitespaceName() throws Exception {
        DocumentType doctype = new DocumentType("   ", null, null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyPublicId() throws Exception {
        DocumentType doctype = new DocumentType("html", "", null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWhitespacePublicId() throws Exception {
        DocumentType doctype = new DocumentType("html", "   ", null, null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testEmptySystemId() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWhitespaceSystemId() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "   ", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSpecialCharactersInAttributes() throws Exception {
        DocumentType doctype = new DocumentType("html", "pub\"lic", "sys\"tem", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html PUBLIC \"pub\"lic\" \"sys\"tem\">", sb.toString());
    }

    /* ==================== Partition C: Defect-Targeted Branch Zone ==================== */

    /**
     * Defect-targeted test: systemId without publicId should emit SYSTEM keyword.
     * This directly tests the known Defects4J failure where SYSTEM was missing.
     */
    @Test(timeout = 4000)
    public void testSystemIdWithoutPublicIdEmitsSystemKeyword() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "exampledtdfile.dtd", null);
        String result = doctype.toString();
        assertTrue("Expected SYSTEM keyword in output but was: " + result, 
                   result.contains("SYSTEM"));
        assertEquals("<!doctype html SYSTEM \"exampledtdfile.dtd\">", result);
    }

    @Test(timeout = 4000)
    public void testSystemIdWithoutPublicIdXmlSyntax() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "exampledtdfile.dtd", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSystemIdWithEmptyPublicId() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "exampledtdfile.dtd", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html SYSTEM \"exampledtdfile.dtd\">", sb.toString());
    }

    /* ==================== Partition D: Exception & Defensive Guard Paths ==================== */

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithNullAppendable() throws Exception {
        DocumentType doctype = new DocumentType("html", null, null, null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        try {
            doctype.outerHtmlHead(null, 0, out);
            fail("Expected NullPointerException for null Appendable");
        } catch (NullPointerException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testOuterHtmlHeadWithNullOutputSettings() throws Exception {
        DocumentType doctype = new DocumentType("html", null, null, null);
        StringBuilder sb = new StringBuilder();
        try {
            doctype.outerHtmlHead(sb, 0, null);
            fail("Expected NullPointerException for null OutputSettings");
        } catch (NullPointerException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTailDoesNothing() throws Exception {
        DocumentType doctype = new DocumentType("html", null, null, null);
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlTail(sb, 0, new Document.OutputSettings());
        assertEquals("", sb.toString());
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testToStringWithSystemIdOnly() {
        DocumentType doctype = new DocumentType("html", null, "exampledtdfile.dtd", null);
        assertEquals("<!doctype html SYSTEM \"exampledtdfile.dtd\">", doctype.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWithPublicIdOnly() {
        DocumentType doctype = new DocumentType("html", "public-id", null, null);
        assertEquals("<!doctype html PUBLIC \"public-id\">", doctype.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWithBothIds() {
        DocumentType doctype = new DocumentType("html", "public-id", "system-id", null);
        assertEquals("<!doctype html PUBLIC \"public-id\" \"system-id\">", doctype.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWithNoIds() {
        DocumentType doctype = new DocumentType("html", null, null, null);
        assertEquals("<!doctype html>", doctype.toString());
    }

    @Test(timeout = 4000)
    public void testBaseUriPreserved() {
        DocumentType doctype = new DocumentType("html", null, null, "http://example.com/base");
        assertEquals("http://example.com/base", doctype.baseUri());
    }

    @Test(timeout = 4000)
    public void testNullBaseUri() {
        DocumentType doctype = new DocumentType("html", null, null, null);
        assertEquals("", doctype.baseUri());
    }

    @Test(timeout = 4000)
    public void testAttributesAccessible() {
        DocumentType doctype = new DocumentType("html", "pub", "sys", null);
        assertEquals("html", doctype.attr("name"));
        assertEquals("pub", doctype.attr("publicId"));
        assertEquals("sys", doctype.attr("systemId"));
    }

    @Test(timeout = 4000)
    public void testAttributesWithNullValues() {
        DocumentType doctype = new DocumentType(null, null, null, null);
        assertEquals("", doctype.attr("name"));
        assertEquals("", doctype.attr("publicId"));
        assertEquals("", doctype.attr("systemId"));
    }

    @Test(timeout = 4000)
    public void testHasMethodWithBlankValues() {
        DocumentType doctype = new DocumentType("", "  ", null, null);
        // has() is private, but we can verify through outerHtmlHead behavior
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        try {
            doctype.outerHtmlHead(sb, 0, out);
            assertEquals("<!doctype>", sb.toString());
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMultipleCallsToOuterHtmlHead() throws Exception {
        DocumentType doctype = new DocumentType("html", "pub", "sys", null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        
        StringBuilder sb1 = new StringBuilder();
        doctype.outerHtmlHead(sb1, 0, out);
        String first = sb1.toString();
        
        StringBuilder sb2 = new StringBuilder();
        doctype.outerHtmlHead(sb2, 0, out);
        String second = sb2.toString();
        
        assertEquals(first, second);
        assertEquals("<!doctype html PUBLIC \"pub\" \"sys\">", first);
    }

    @Test(timeout = 4000)
    public void testDepthParameterIgnored() throws Exception {
        DocumentType doctype = new DocumentType("html", null, null, null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        
        StringBuilder sb1 = new StringBuilder();
        doctype.outerHtmlHead(sb1, 0, out);
        
        StringBuilder sb2 = new StringBuilder();
        doctype.outerHtmlHead(sb2, 5, out);
        
        assertEquals(sb1.toString(), sb2.toString());
    }

    @Test(timeout = 4000)
    public void testXmlSyntaxWithSystemIdOnly() throws Exception {
        DocumentType doctype = new DocumentType("html", null, "system.dtd", null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html SYSTEM \"system.dtd\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testHtmlSyntaxWithPublicIdOnly() throws Exception {
        DocumentType doctype = new DocumentType("html", "public-id", null, null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype html PUBLIC \"public-id\">", sb.toString());
    }

    @Test(timeout = 4000)
    public void testUppercaseDoctypeNameInHtmlSyntax() throws Exception {
        DocumentType doctype = new DocumentType("HTML", null, null, null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.html);
        
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!doctype HTML>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testUppercaseDoctypeNameInXmlSyntax() throws Exception {
        DocumentType doctype = new DocumentType("HTML", null, null, null);
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(Document.OutputSettings.Syntax.xml);
        
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE HTML>", sb.toString());
    }
}