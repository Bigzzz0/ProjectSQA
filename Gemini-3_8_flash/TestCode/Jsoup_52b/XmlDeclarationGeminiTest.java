package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

/* [Branch & Defect Analysis Matrix]
 * Class under test: org.jsoup.nodes.XmlDeclaration
 *
 * Decision / Condition Coverage Matrix:
 * 1. Constructor:
 *    - name == null -> Validate.notNull throws IllegalArgumentException
 *    - name != null -> successfully initializes name, baseUri, isProcessingInstruction
 * 2. nodeName():
 *    - returns constant "#declaration"
 * 3. name():
 *    - returns constructor-provided name
 * 4. getWholeDeclaration():
 *    - Branch 1: decl.equals("xml") [True] && attributes.size() > 1 [True]
 *      - Sub-branch 1a: version != null [True/False]
 *      - Sub-branch 1b: encoding != null [True/False]
 *      - Defect: When size == 1 (e.g. only version or only encoding), branch fails and drops attributes!
 *      - Defect: Non-standard attributes (e.g. standalone) are omitted from getWholeDeclaration!
 *    - Branch 2: decl.equals("xml") [False] -> returns name directly, ignores any attributes
 *    - Branch 3: attributes.size() <= 1 [True] -> returns name directly
 * 5. outerHtmlHead():
 *    - isProcessingInstruction == true -> emits "<!" prefix and ">" suffix
 *    - isProcessingInstruction == false -> emits "<?" prefix
 *      - Defect (Defects4J Ground Truth): XML declarations must terminate with "?>", but the defective
 *        implementation appends only ">", causing syntax invalidity and test regressions across
 *        DocumentTest::testMetaCharsetUpdateXml* and XmlTreeBuilderTest.
 *    - Appendable throws IOException -> exception propagated
 * 6. outerHtmlTail():
 *    - No-op method, must not append to accumulator
 * 7. toString():
 *    - Delegates to outerHtml()
 */
public class XmlDeclarationGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeNameReturnsDeclarationIdentifier() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        assertEquals("#declaration", decl.nodeName());
    }

    @Test(timeout = 4000)
    public void testNameGetterReturnsAssignedName() {
        XmlDeclaration decl = new XmlDeclaration("custom-decl", "http://example.com", true);
        assertEquals("custom-decl", decl.name());
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionOuterHtmlFormatting() {
        XmlDeclaration decl = new XmlDeclaration("!DOCTYPE html", "http://example.com", true);
        assertEquals("<!DOCTYPE html>", decl.outerHtml());
    }

    @Test(timeout = 4000)
    public void testOuterHtmlTailIsNoOp() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        StringBuilder sb = new StringBuilder("preset");
        decl.outerHtmlTail(sb, 0, new Document.OutputSettings());
        assertEquals("preset", sb.toString());
    }

    @Test(timeout = 4000)
    public void testToStringMatchesOuterHtml() {
        XmlDeclaration decl = new XmlDeclaration("!target data", "http://example.com", true);
        assertEquals(decl.outerHtml(), decl.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyNameDeclaration() {
        XmlDeclaration decl = new XmlDeclaration("", "http://example.com", false);
        assertEquals("", decl.name());
        assertEquals("", decl.getWholeDeclaration());
    }

    @Test(timeout = 4000)
    public void testEmptyBaseUri() {
        XmlDeclaration decl = new XmlDeclaration("xml", "", false);
        assertEquals("", decl.baseUri());
    }

    @Test(timeout = 4000)
    public void testGetWholeDeclarationWithXmlNameAndBothAttributesPresent() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");

        String wholeDecl = decl.getWholeDeclaration();
        assertEquals("xml version=\"1.0\" encoding=\"UTF-8\"", wholeDecl);
    }

    @Test(timeout = 4000)
    public void testGetWholeDeclarationWithNonXmlNameIgnoresAttributes() {
        XmlDeclaration decl = new XmlDeclaration("custom", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");

        assertEquals("custom", decl.getWholeDeclaration());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETS DEFECT: XML declarations (isProcessingInstruction == false) must close with "?>".
     * Defective implementation outputs "<?" + getWholeDeclaration() + ">" missing the terminal "?".
     * Ground truth: DocumentTest::testMetaCharsetUpdateXml* failure: expected:<....0" encoding="UTF-8"[?]>
     */
    @Test(timeout = 4000)
    public void testXmlDeclarationOuterHtmlMustEndWithQuestionMarkGreater() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");

        String expectedHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        assertEquals(expectedHtml, decl.outerHtml());
    }

    /**
     * TARGETS DEFECT: An XML declaration with only a single attribute (size == 1) should still
     * render its attribute in getWholeDeclaration().
     * Defective implementation checks attributes.size() > 1, so a declaration with only "version"
     * drops the version attribute entirely and returns just "xml".
     */
    @Test(timeout = 4000)
    public void testGetWholeDeclarationWithSingleVersionAttribute() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");

        assertEquals("xml version=\"1.0\"", decl.getWholeDeclaration());
        assertEquals("<?xml version=\"1.0\"?>", decl.outerHtml());
    }

    /**
     * TARGETS DEFECT: An XML declaration with only an "encoding" attribute (size == 1).
     * Defective implementation drops encoding when attributes.size() <= 1.
     */
    @Test(timeout = 4000)
    public void testGetWholeDeclarationWithSingleEncodingAttribute() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("encoding", "ISO-8859-1");

        assertEquals("xml encoding=\"ISO-8859-1\"", decl.getWholeDeclaration());
        assertEquals("<?xml encoding=\"ISO-8859-1\"?>", decl.outerHtml());
    }

    /**
     * TARGETS DEFECT: XML declarations containing additional valid attributes (e.g., standalone).
     * Defective implementation only checks for "version" and "encoding" and discards any other attribute.
     */
    @Test(timeout = 4000)
    public void testGetWholeDeclarationPreservesStandaloneAndCustomAttributes() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");
        decl.attr("standalone", "yes");

        String wholeDecl = decl.getWholeDeclaration();
        assertTrue("getWholeDeclaration must include standalone attribute", wholeDecl.contains("standalone=\"yes\""));
        assertTrue("outerHtml must end with '?>'", decl.outerHtml().endsWith("?>"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsNullName() {
        new XmlDeclaration(null, "http://example.com", false);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testOuterHtmlHeadPropagatesIOException() throws IOException {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        Appendable throwingAppendable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException("Forced disk failure");
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException("Forced disk failure");
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("Forced disk failure");
            }
        };

        decl.outerHtmlHead(throwingAppendable, 0, new Document.OutputSettings());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneIndependence() {
        XmlDeclaration decl = new XmlDeclaration("xml", "http://example.com", false);
        decl.attr("version", "1.0");
        decl.attr("encoding", "UTF-8");

        XmlDeclaration clone = (XmlDeclaration) decl.clone();

        assertNotSame(decl, clone);
        assertEquals(decl.name(), clone.name());
        assertEquals(decl.baseUri(), clone.baseUri());
        assertEquals(decl.attributes().get("version"), clone.attributes().get("version"));

        // Mutating original must not affect clone
        decl.attr("version", "2.0");
        assertEquals("1.0", clone.attributes().get("version"));
    }
}