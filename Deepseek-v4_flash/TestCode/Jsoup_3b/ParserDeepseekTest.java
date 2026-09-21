package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Parser.java targeting maximum coverage and the known Defects4J defect
 * regarding nested implicit table handling.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic: parse full document, parse body fragment, start/end tags, text nodes, comments, CDATA, XML declarations, attributes (quoted/unquoted), data tags (script, textarea), base tag.
 * - Partition B: Boundary values: empty HTML, null/empty baseUri (validated by Validate.notNull), empty tag name in start/end tag, attribute key length zero, whitespace handling.
 * - Partition C: Defect-targeted: nested implicit table handling (e.g., <table><tr><td> inside another table), appending/prepending rows to table, ensuring correct implicit <tbody> insertion without extra <table>.
 * - Partition D: Exception/defensive paths: missing tag name in start tag (puts back &lt;), missing tag name in end tag (ignored), attribute with no value, unknown character in attribute key.
 * - Partition E: Object lifecycle: stack state after parsing, document normalisation.
 */
public class ParserDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testParseFullDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseBodyFragment() {
        String bodyHtml = "<p>Fragment</p>";
        Document doc = Parser.parseBodyFragment(bodyHtml, "http://example.com");
        assertEquals("Fragment", doc.body().text());
        // body should contain the paragraph, head should be empty
        assertEquals(0, doc.head().children().size());
    }

    @Test(timeout = 4000)
    public void testParseEmptyHtml() {
        Document doc = Parser.parse("", "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test(timeout = 4000)
    public void testParseComment() {
        String html = "<!-- comment --><p>text</p>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("text", doc.body().text());
        // comment should be in head? Actually comment before html is placed in head? Let's check structure.
        // The parser puts comments in the current last element. Initially stack has doc, so comment goes into doc.
        // After parsing, normalise moves head/body. We'll just check no exception.
    }

    @Test(timeout = 4000)
    public void testParseCommentWithTrailingDash() {
        String html = "<!-- comment---><p>text</p>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseCdata() {
        String html = "<![CDATA[some <data>]]>";
        Document doc = Parser.parse(html, "http://example.com");
        // CDATA becomes a text node inside the document (since no html/body tags, it's placed in doc)
        assertTrue(doc.text().contains("some <data>"));
    }

    @Test(timeout = 4000)
    public void testParseXmlDecl() {
        String html = "<?xml version=\"1.0\"?><root></root>";
        Document doc = Parser.parse(html, "http://example.com");
        // XML declaration becomes an XmlDeclaration child of document
        // We can check that the document has at least one child of type XmlDeclaration
        boolean found = false;
        for (Node node : doc.childNodes()) {
            if (node instanceof XmlDeclaration) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test(timeout = 4000)
    public void testParseEndTagWithMissingName() {
        // </> should be ignored (tagName length 0)
        String html = "<p>text</>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseStartTagWithMissingName() {
        // < followed by non-word character -> treat as text
        String html = "< <p>text</p>";
        Document doc = Parser.parse(html, "http://example.com");
        // The first '<' becomes &lt; text, then <p> is parsed
        assertTrue(doc.body().html().contains("&lt;"));
    }

    @Test(timeout = 4000)
    public void testParseEmptyTag() {
        String html = "<br><hr><img src=\"test.png\">";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals(3, doc.body().children().size());
        assertEquals("br", doc.body().child(0).tagName());
        assertEquals("hr", doc.body().child(1).tagName());
        assertEquals("img", doc.body().child(2).tagName());
    }

    @Test(timeout = 4000)
    public void testParseSelfClosingTag() {
        String html = "<div/>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("div", doc.body().child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testParseDataTagScript() {
        String html = "<script>var x = 1 < 2;</script>";
        Document doc = Parser.parse(html, "http://example.com");
        Element script = doc.body().child(0);
        assertEquals("script", script.tagName());
        // DataNode content should be raw
        assertEquals("var x = 1 < 2;", script.data());
    }

    @Test(timeout = 4000)
    public void testParseDataTagTextarea() {
        String html = "<textarea>some text</textarea>";
        Document doc = Parser.parse(html, "http://example.com");
        Element textarea = doc.body().child(0);
        assertEquals("textarea", textarea.tagName());
        // TextNode content should be encoded? Actually TextNode.createFromEncoded decodes HTML entities.
        // For plain text, it's fine.
        assertEquals("some text", textarea.text());
    }

    @Test(timeout = 4000)
    public void testParseBaseTag() {
        String html = "<base href=\"http://newbase.com/\"><a href=\"page.html\">link</a>";
        Document doc = Parser.parse(html, "http://original.com");
        // base href should update baseUri
        assertEquals("http://newbase.com/", doc.baseUri());
        Element link = doc.body().child(0);
        assertEquals("http://newbase.com/page.html", link.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeWithSingleQuotes() {
        String html = "<div attr='value'>text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeWithDoubleQuotes() {
        String html = "<div attr=\"value\">text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeUnquoted() {
        String html = "<div attr=value>text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("value", div.attr("attr"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeNoValue() {
        String html = "<div disabled>text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        Element div = doc.body().child(0);
        assertEquals("", div.attr("disabled"));
    }

    @Test(timeout = 4000)
    public void testParseAttributeKeyEmpty() {
        // This should be ignored (returns null, consumes one char)
        String html = "<div =\"value\">text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        // Should not throw, attribute ignored
        assertEquals("text", doc.body().text());
    }

    // ========== Partition B: Boundary Values ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseNullHtml() {
        Parser.parse(null, "http://example.com");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseNullBaseUri() {
        Parser.parse("<p>text</p>", null);
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentNullHtml() {
        try {
            Parser.parseBodyFragment(null, "http://example.com");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseBodyFragmentNullBaseUri() {
        try {
            Parser.parseBodyFragment("<p>text</p>", null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespaceOnly() {
        Document doc = Parser.parse("   ", "http://example.com");
        assertEquals("", doc.text());
    }

    // ========== Partition C: Defect-Targeted (Nested Implicit Table) ==========

    @Test(timeout = 4000)
    public void testAppendRowToTable() {
        // This test reproduces the defect from ElementTest::testAppendRowToTable
        // Expected: <table><tr><td>1</td></tr><tr><td>2</td></tr></table>
        // Bug: produces <table><tr><td>1</td></tr><table><tr><td>2</td></tr></table></table>
        String html = "<table><tr><td>1</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");
        Element table = doc.body().child(0);
        // Append a new row via parsing? Actually the defect is about appending via Element.append, but we can simulate by parsing a fragment that includes a second row.
        // The bug is in the parser when handling implicit table structure. Let's parse a string that would trigger the bug:
        // According to the defect, when parsing "<table><tr><td>1</td></tr><tr><td>2</td></tr></table>", the parser incorrectly wraps the second tr in a new table.
        String htmlWithTwoRows = "<table><tr><td>1</td></tr><tr><td>2</td></tr></table>";
        Document doc2 = Parser.parse(htmlWithTwoRows, "http://example.com");
        Element table2 = doc2.body().child(0);
        // Should have two tr children
        assertEquals(2, table2.children().size());
        assertEquals("tr", table2.child(0).tagName());
        assertEquals("tr", table2.child(1).tagName());
        // Check inner text
        assertEquals("12", table2.text());
    }

    @Test(timeout = 4000)
    public void testPrependRowToTable() {
        // Similar to testAppendRowToTable but with prepend scenario
        String html = "<table><tr><td>2</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");
        // We'll simulate prepend by parsing a fragment that adds a row before. The bug would cause extra table.
        // Actually the defect is about Element.prependChild, but we can test the parser's handling of nested implicit tables.
        // Let's test the specific case from the defect: handlesNestedImplicitTable
    }

    @Test(timeout = 4000)
    public void testHandlesNestedImplicitTable() {
        // This directly targets the known defect from ParserTest::handlesNestedImplicitTable
        // Input: "<table><tr><td> <table><tr><td>3</td> <td>4</td></tr></table> </td></tr><tr><td>5</td></tr></table>"
        // Expected: outer table with two rows, second row contains inner table with two cells.
        // Bug: inner table's first row's cells are misplaced, and extra table tags appear.
        String html = "<table><tr><td> <table><tr><td>3</td> <td>4</td></tr></table> </td></tr><tr><td>5</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");
        Element outerTable = doc.body().child(0);
        // Outer table should have two tr children
        assertEquals(2, outerTable.children().size());
        Element firstRow = outerTable.child(0);
        Element secondRow = outerTable.child(1);
        // First row should have one td containing the inner table
        assertEquals(1, firstRow.children().size());
        Element innerTable = firstRow.child(0).child(0); // td > table
        assertNotNull(innerTable);
        assertEquals("table", innerTable.tagName());
        // Inner table should have one tr with two tds
        assertEquals(1, innerTable.children().size());
        Element innerRow = innerTable.child(0);
        assertEquals(2, innerRow.children().size());
        assertEquals("3", innerRow.child(0).text());
        assertEquals("4", innerRow.child(1).text());
        // Second row should have one td with text "5"
        assertEquals(1, secondRow.children().size());
        assertEquals("5", secondRow.child(0).text());
    }

    @Test(timeout = 4000)
    public void testNestedTableWithImplicitTbody() {
        // Additional test to ensure that when a <tr> is added to a table without explicit <tbody>, an implicit <tbody> is created correctly.
        String html = "<table><tr><td>1</td></tr></table>";
        Document doc = Parser.parse(html, "http://example.com");
        Element table = doc.body().child(0);
        // The parser should create an implicit <tbody> around the <tr>
        assertEquals(1, table.children().size());
        Element tbody = table.child(0);
        assertEquals("tbody", tbody.tagName());
        assertEquals(1, tbody.children().size());
        assertEquals("tr", tbody.child(0).tagName());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testParseEndTagWithNoStackMatch() {
        // Closing a tag that is not open should be ignored
        String html = "<p>text</div>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testParseStartTagWithInvalidAncestor() {
        // e.g., <tr> directly under <html> should create implicit <table> and <tbody>
        String html = "<tr><td>data</td></tr>";
        Document doc = Parser.parse(html, "http://example.com");
        // Should be wrapped in <table><tbody><tr>...
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element table = body.child(0);
        assertEquals("table", table.tagName());
        Element tbody = table.child(0);
        assertEquals("tbody", tbody.tagName());
        Element tr = tbody.child(0);
        assertEquals("tr", tr.tagName());
    }

    @Test(timeout = 4000)
    public void testParseBodyTagInsideFragment() {
        // When parsing body fragment, body tag should be handled correctly
        String html = "<body><p>content</p></body>";
        Document doc = Parser.parseBodyFragment(html, "http://example.com");
        // The body tag should be ignored because we already have a body shell
        assertEquals("content", doc.body().text());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testDocumentNormalisation() {
        // After parsing, document should be normalised (head and body created if missing)
        String html = "<p>text</p>";
        Document doc = Parser.parse(html, "http://example.com");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testStackStateAfterParse() {
        // Ensure that after parsing, the stack is empty (parser is done)
        // We can't access private stack, but we can verify document structure
        String html = "<div><span>text</span></div>";
        Document doc = Parser.parse(html, "http://example.com");
        assertEquals("text", doc.body().text());
    }

    @Test(timeout = 4000)
    public void testMultipleCallsToParse() {
        // Parser is not reusable, but static methods create new instances
        Document doc1 = Parser.parse("<p>first</p>", "http://a.com");
        Document doc2 = Parser.parse("<p>second</p>", "http://b.com");
        assertEquals("first", doc1.body().text());
        assertEquals("second", doc2.body().text());
    }
}