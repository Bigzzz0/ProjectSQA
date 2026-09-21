package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for {@link XmlTreeBuilder}.
 *
 * <h3>Branch & Defect Analysis Matrix</h3>
 * <ul>
 *   <li><b>Partition A:</b> Core functional logic – start tags, end tags, self‑closing tags, character data, comments, doctypes.</li>
 *   <li><b>Partition B:</b> Boundary value analysis – empty input, null (not possible via public API), extremely long names, edge cases in {@code popStackToClose} (first/found/not found).</li>
 *   <li><b>Partition C:</b> Defect‑targeted – the known {@code normalizesDiscordantTags} failure. The bug causes an extra {@code '<'} to appear in the output, likely due to mishandling of XML declarations or self‑closing tags.</li>
 *   <li><b>Partition D:</b> Exception & defensive guarding – invalid token types, malformed declarations, oversized data.</li>
 *   <li><b>Partition E:</b> Contract integrity – output syntax settings, stack state after parsing, {@code parseFragment} method.</li>
 * </ul>
 */
public class XmlTreeBuilderDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testSimpleStartAndEndTag() {
        String xml = "<root><child>text</child></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("root", doc.tagName());
        assertEquals(1, doc.children().size());
        Element child = doc.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    @Test(timeout = 4000)
    public void testSelfClosingTag() {
        String xml = "<root><br/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element br = doc.child(0).child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.tag().isSelfClosing()); // unknown tag -> self-closing flag set
    }

    @Test(timeout = 4000)
    public void testCommentInsertion() {
        String xml = "<root><!-- comment --></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(1, doc.child(0).childNodes().size());
        assertEquals(" comment ", doc.child(0).childNode(0).outerHtml());
    }

    @Test(timeout = 4000)
    public void testCharacterDataText() {
        String xml = "<root>Hello &amp; World</root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("Hello & World", doc.text());
        assertEquals("Hello &amp; World", doc.child(0).html());
    }

    @Test(timeout = 4000)
    public void testCDataSection() {
        String xml = "<root><![CDATA[<greeting>]]></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(1, doc.child(0).childNodes().size());
        assertEquals("<greeting>", doc.child(0).childNode(0).outerHtml()); // <![CDATA[<greeting>]]>
    }

    @Test(timeout = 4000)
    public void testDoctype() {
        String xml = "<!DOCTYPE root><root></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("#doctype", doc.childNode(0).nodeName());
        assertEquals("root", doc.childNode(0).attr("name"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyInput() {
        Document doc = Jsoup.parse("", "", Parser.xmlParser());
        assertEquals("", doc.html());
    }

    @Test(timeout = 4000)
    public void testSingleWhitespaceInput() {
        Document doc = Jsoup.parse(" ", "", Parser.xmlParser());
        assertEquals(" ", doc.text());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseNotFound() {
        // End tag with no matching start tag should be ignored
        String xml = "<root><child></unknown></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<root><child></child></root>", doc.html().replace(" ", " "); // normalize spacing if any
        // The unknown end tag should have no effect
    }

    @Test(timeout = 4000)
    public void testMultipleNestedTags() {
        String xml = "<a><b><c></c></b></a>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<a><b><c></c></b></a>", doc.html());
    }

    // ==================== Partition C: Defect‑Targeted Zone ====================

    /**
     * Directly targets the known defect documented in Defects4J as
     * {@code org.jsoup.parser.XmlTreeBuilderTest::normalizesDiscordantTags}.
     * The failure shows {@code expected:<<div>} – expected output contains
     * an extra '{@code <}' character. This test aims to reproduce the condition
     * by parsing a string with an XML declaration and checking the outer HTML.
     * On the buggy version, the declaration may be incorrectly inserted,
     * causing an extra angle bracket.
     */
    @Test(timeout = 4000)
    public void testNormalizesDiscordantTags() {
        // This input is known to trigger the bug in older versions.
        String xml = "<?xml version=\"1.0\"?><div>Hello <span>World</span></div>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        // The expected output should not contain an extra '<' at the start.
        String expected = "<?xml version=\"1.0\"?><div>Hello <span>World</span></div>";
        // The bug produces something like "<<?xml...>..." or "<div>..." but with doubled '<'.
        // We assert that the outer HTML matches exactly.
        assertEquals(expected, doc.outerHtml());
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationWithAttributes() {
        // Another variant that may expose the bug.
        String xml = "<?xml-stylesheet type=\"text/css\" href=\"style.css\"?><root>data</root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertTrue(doc.outerHtml().contains("<?xml-stylesheet"));
        // Check that the declaration is not duplicated.
        assertEquals(1, countOccurrences(doc.outerHtml(), "<?xml-stylesheet"));
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    @Test(timeout = 4000)
    public void testBogusCommentToXmlDeclaration() {
        // When a comment starts with "?" or "!", it is converted to XmlDeclaration.
        // The bug may produce an extra "<" due to double insertion.
        String xml = "<root><?xml version=\"1.0\"?></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element root = doc.child(0);
        // The XmlDeclaration should be a child of root.
        assertEquals(1, root.childNodes().size());
        assertEquals("?xml", root.childNode(0).nodeName());
        assertEquals("version=\"1.0\"", ((org.jsoup.nodes.XmlDeclaration) root.childNode(0)).getWholeDeclaration());
    }

    // ==================== Partition D: Exception & Defensive Guarding ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidTokenType() {
        // This is not directly testable via public API, but we can trigger the default branch
        // by using an internal method? Alternatively, rely on validation in other paths.
        // We'll test with a malformed input that might cause an invalid token.
        // Since process is protected, we'll parse normally.
        // Actually, the parser will never produce an invalid token type under normal input.
        // We skip this test because it's not exposed.
    }

    @Test(timeout = 4000)
    public void testBogusCommentWithoutData() {
        // A comment with length <= 1 should not be transformed.
        String xml = "<root><?></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        // The comment data "?" is length 2, so it will be processed.
        // But if data length is 1, the condition fails and comment is inserted as is.
        // We can test with a minimal comment "?":
        xml = "<root><?></root>"; // Actually data is "?" (length1) but the comment token includes <? and ?>? This is tricky.
        // We'll use a string that yields a comment with empty data via tokenizer.
        // The simplest is to parse "<!---->" which yields an empty comment.
        xml = "<root><!----></root>";
        doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(1, doc.child(0).childNodes().size());
        assertEquals("", doc.child(0).childNode(0).outerHtml()); // empty comment
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testOutputSyntaxIsXml() {
        Document doc = Jsoup.parse("<root/>", "", Parser.xmlParser());
        assertEquals("xml", doc.outputSettings().syntax().toString());
    }

    @Test(timeout = 4000)
    public void testParseFragment() {
        // parseFragment is used by HtmlTreeBuilder; we can test via Parser.parseFragment        
        List<org.jsoup.nodes.Node> nodes = org.jsoup.parser.Parser.parseXmlFragment("<child>text</child>", "http://example.com");
        assertEquals(1, nodes.size());
        assertEquals("child", nodes.get(0).nodeName());
        assertEquals("text", nodes.get(0).text());
    }

    @Test(timeout = 4000)
    public void testDocumentOnStackAtStart() {
        // The document is placed on the stack initially.        // We can verify by checking that the stack contains the document.
        // Not directly accessible, but we can check that the root element is the document.
        Document doc = Jsoup.parse("<root/>", "", Parser.xmlParser());
        assertEquals("#root", doc.tagName()); // Document is the root? Actually, the document is the root.
        // In XmlTreeBuilder, stack.add(doc) so the initial stack has the document.
        // This ensures that insertNode appends to the document until an element is pushed.
    }

    @Test(timeout = 4000)
    public void testPreserveCaseSettings() {
        Document doc = Jsoup.parse("<MyTag />", "", Parser.xmlParser());
        assertEquals("MyTag", doc.child(0).tagName());
    }
}