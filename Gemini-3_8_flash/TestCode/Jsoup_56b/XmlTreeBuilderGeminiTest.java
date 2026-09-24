package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.parser.XmlTreeBuilder
 * Primary Responsibilities: Parse XML inputs into DOM Trees without HTML semantics, manage tag casing, self-closing
 * tags, XML declarations, bogus comments, and DocumentType definitions.
 *
 * Branch Coverage & Decision Points:
 * 1. defaultSettings() -> Verifies preserveCase (preserve tag case and attribute case).
 * 2. initialiseParse() -> Ensures Document node is initially pushed to the stack and OutputSettings syntax is XML.
 * 3. process(Token):
 *    - StartTag -> Delegates to insert(Token.StartTag).
 *    - EndTag   -> Delegates to popStackToClose(Token.EndTag).
 *    - Comment  -> Delegates to insert(Token.Comment).
 *    - Character-> Delegates to insert(Token.Character).
 *    - Doctype  -> Delegates to insert(Token.Doctype).
 *    - EOF      -> Handled cleanly (no-op normalization).
 * 4. insert(Token.StartTag):
 *    - isSelfClosing() == true  -> calls acknowledgeSelfClosingFlag(), checks !tag.isKnownTag() to mark self-closing.
 *    - isSelfClosing() == false -> pushes element to the builder stack.
 * 5. insert(Token.Comment):
 *    - commentToken.bogus == false -> creates Comment node.
 *    - commentToken.bogus == true:
 *      * data.length() <= 1 -> falls back to regular Comment node.
 *      * data.length() > 1 && !(startsWith("!") || startsWith("?")) -> falls back to regular Comment node.
 *      * data.length() > 1 && startsWith("?") -> creates XmlDeclaration (isProcessingInstruction = false).
 *      * data.length() > 1 && startsWith("!") -> creates XmlDeclaration (isProcessingInstruction = true).
 * 6. insert(Token.Doctype):
 *    - Normalizes tag name according to settings, sets publicId, systemId, and baseUri.
 * 7. popStackToClose(Token.EndTag):
 *    - Matching tag found at top of stack -> pops 1 element.
 *    - Matching tag found deeper in stack -> pops all intermediate elements down to firstFound.
 *    - Matching tag not found on stack -> early return, leaves stack untouched.
 * 8. parseFragment():
 *    - Parses an isolated XML snippet and returns the document's direct childNodes list.
 *
 * Defect-Targeted Ground Truth (Defects4J):
 * - org.jsoup.nodes.DocumentTypeTest::testRoundTrip
 *   Failure: expected:<<!DOCTYPE html [SYSTEM ]"exampledtdfile.dtd"...> but was:<<!DOCTYPE html []"exampledtdfile.dtd"...>
 *   Targeted via parsing DOCTYPE with only SYSTEM identifier in XML mode and asserting exact round-trip serialization.
 * -------------------------------------------------------------------------------------------------------------
 */
public class XmlTreeBuilderGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultSettingsPreservesCase() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        ParseSettings settings = builder.defaultSettings();
        assertNotNull(settings);
        assertTrue("Tag case must be preserved by default", settings.preserveTagCase());
        assertTrue("Attribute case must be preserved by default", settings.preserveAttributeCase());
    }

    @Test(timeout = 4000)
    public void testInitialiseParseConfiguresXmlSyntaxAndStack() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("<root/>", "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        
        assertNotNull("Document must be created", builder.doc);
        assertEquals("Syntax must be set to XML", Document.OutputSettings.Syntax.xml, builder.doc.outputSettings().syntax());
        assertFalse("Stack must not be empty", builder.stack.isEmpty());
        assertSame("Doc must be first element on stack", builder.doc, builder.stack.get(0));
    }

    @Test(timeout = 4000)
    public void testBasicXmlStructureParsing() {
        String xml = "<note><to>User</to><from>Admin</from><heading>Alert</heading><body>Message</body></note>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Element note = doc.selectFirst("note");
        assertNotNull(note);
        assertEquals(4, note.children().size());
        assertEquals("User", note.selectFirst("to").text());
        assertEquals("Admin", note.selectFirst("from").text());
        assertEquals("Alert", note.selectFirst("heading").text());
        assertEquals("Message", note.selectFirst("body").text());
    }

    @Test(timeout = 4000)
    public void testCasePreservationInTagsAndAttributes() {
        String xml = "<MixedCaseTag CamelAttribute=\"Value1\" lowercase=\"val2\">InnerContent</MixedCaseTag>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element el = doc.child(0);
        assertEquals("MixedCaseTag", el.tagName());
        assertTrue(el.hasAttr("CamelAttribute"));
        assertEquals("Value1", el.attr("CamelAttribute"));
        assertTrue(el.hasAttr("lowercase"));
        assertEquals("val2", el.attr("lowercase"));
    }

    @Test(timeout = 4000)
    public void testXmlDeclarationParsing() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        List<Node> nodes = doc.childNodes();
        assertTrue("First node should be XmlDeclaration", nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", decl.outerHtml().trim());
    }

    @Test(timeout = 4000)
    public void testProcessingInstructionParsing() {
        String xml = "<?php echo 'hello'; ?><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Node firstNode = doc.childNode(0);
        assertTrue(firstNode instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) firstNode;
        assertEquals("php", decl.name());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document emptyDoc = builder.parse("", "http://example.com");
        assertEquals(0, emptyDoc.childNodes().size());

        Document wsDoc = builder.parse("   \n\t   ", "http://example.com");
        // Trailing whitespace or whitespace node may be parsed
        for (Node n : wsDoc.childNodes()) {
            assertTrue(n instanceof TextNode);
            assertTrue(((TextNode) n).isBlank());
        }
    }

    @Test(timeout = 4000)
    public void testSelfClosingUnknownTag() {
        String xml = "<custom-tag id=\"123\" />";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element custom = doc.selectFirst("custom-tag");
        assertNotNull(custom);
        assertTrue("Unknown tag marked self-closing should be self-closing", custom.tag().isSelfClosing());
        assertEquals("<custom-tag id=\"123\" />", custom.outerHtml());
    }

    @Test(timeout = 4000)
    public void testSelfClosingKnownTag() {
        String xml = "<br/><img src=\"test.jpg\"/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        assertEquals(2, doc.children().size());
        Element br = doc.child(0);
        Element img = doc.child(1);
        assertEquals("br", br.tagName());
        assertEquals("img", img.tagName());
        assertEquals("test.jpg", img.attr("src"));
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedXml() {
        StringBuilder sb = new StringBuilder();
        int depth = 50;
        for (int i = 0; i < depth; i++) {
            sb.append("<level").append(i).append(">");
        }
        sb.append("DeepText");
        for (int i = depth - 1; i >= 0; i--) {
            sb.append("</level").append(i).append(">");
        }

        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(sb.toString(), "http://example.com");
        Element current = doc.child(0);
        for (int i = 1; i < depth; i++) {
            current = current.child(0);
            assertEquals("level" + i, current.tagName());
        }
        assertEquals("DeepText", current.text());
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseWithMismatchedAndOrphanEndTags() {
        // Tag </orphan> has no open start tag; <child> is prematurely closed by </root>
        String xml = "<root><parent><child>Text</orphan></parent></root>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element root = doc.selectFirst("root");
        assertNotNull(root);
        Element parent = root.selectFirst("parent");
        assertNotNull(parent);
        Element child = parent.selectFirst("child");
        assertNotNull(child);
        assertEquals("Text", child.text());
    }

    @Test(timeout = 4000)
    public void testPopStackToClosePopsIntermediateUnclosedTags() {
        // </root> closes both <leaf> and <mid>
        String xml = "<root><mid><leaf>Dangling content</root>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element root = doc.selectFirst("root");
        assertNotNull(root);
        Element mid = root.selectFirst("mid");
        assertNotNull(mid);
        Element leaf = mid.selectFirst("leaf");
        assertNotNull(leaf);
        assertEquals("Dangling content", leaf.text());
    }

    @Test(timeout = 4000)
    public void testEndTagWhenStackOnlyHasDoc() {
        // Stray end tags after root tag closure
        String xml = "<root></root></extra></another>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        assertEquals(1, doc.children().size());
        assertEquals("root", doc.child(0).tagName());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoctypeWithSystemIdentifierRoundTripDefect() {
        /*
         * Target Defect: org.jsoup.nodes.DocumentTypeTest::testRoundTrip
         * In affected versions, parsing a DOCTYPE with SYSTEM identifier but no PUBLIC identifier
         * caused the SYSTEM keyword to be omitted during serialization:
         * expected:<<!DOCTYPE html [SYSTEM ]"exampledtdfile.dtd"...> but was:<<!DOCTYPE html []"exampledtdfile.dtd"...>
         */
        String xml = "<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\"><root />";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        List<Node> childNodes = doc.childNodes();
        assertTrue("Document must have children", childNodes.size() >= 2);
        assertTrue("First node must be DocumentType", childNodes.get(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) childNodes.get(0);
        assertEquals("html", doctype.nodeName());
        assertEquals("exampledtdfile.dtd", doctype.attr("systemId"));
        assertEquals("", doctype.attr("publicId"));
        assertEquals("<!DOCTYPE html SYSTEM \"exampledtdfile.dtd\">", doctype.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypePublicAndSystemIdentifiers() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.nodeName());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", doctype.outerHtml());
    }

    @Test(timeout = 4000)
    public void testDoctypeNameOnly() {
        String xml = "<!DOCTYPE html><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.nodeName());
        assertEquals("", doctype.attr("publicId"));
        assertEquals("", doctype.attr("systemId"));
        assertEquals("<!DOCTYPE html>", doctype.outerHtml());
    }

    // =========================================================================
    // PARTITION D: Exception, Bogus Comments, & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testRegularCommentNode() {
        String xml = "<!-- This is a regular comment --><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Node first = doc.childNode(0);
        assertTrue(first instanceof Comment);
        Comment comment = (Comment) first;
        assertEquals(" This is a regular comment ", comment.getData());
        assertEquals("<!-- This is a regular comment -->", comment.outerHtml());
    }

    @Test(timeout = 4000)
    public void testBogusCommentStartingWithExclamation() {
        // Tokenizer treats <!foo attr='bar'> as bogus comment
        String xml = "<!foo bar=\"baz\"!><root/>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Node first = doc.childNode(0);
        assertTrue(first instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) first;
        assertEquals("foo", decl.name());
        assertEquals("baz", decl.attr("bar"));
        assertEquals("<!foo bar=\"baz\"!>", decl.outerHtml().trim());
    }

    @Test(timeout = 4000)
    public void testBogusCommentDirectBranchCoverage() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("<dummy/>", "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        // Branch 1: Bogus comment with length <= 1
        Token.Comment shortBogus = new Token.Comment();
        shortBogus.bogus = true;
        shortBogus.data.append("?");
        builder.insert(shortBogus);

        // Branch 2: Bogus comment with length > 1 but starts with neither '!' nor '?'
        Token.Comment nonDeclBogus = new Token.Comment();
        nonDeclBogus.bogus = true;
        nonDeclBogus.data.append("normal text");
        builder.insert(nonDeclBogus);

        // Branch 3: Non-bogus regular comment
        Token.Comment regular = new Token.Comment();
        regular.bogus = false;
        regular.data.append("just comment");
        builder.insert(regular);

        Element dummy = builder.doc.selectFirst("dummy");
        assertNotNull(dummy);
        assertEquals(3, dummy.childNodes().size());
        assertTrue(dummy.childNode(0) instanceof Comment);
        assertEquals("?", ((Comment) dummy.childNode(0)).getData());
        assertTrue(dummy.childNode(1) instanceof Comment);
        assertEquals("normal text", ((Comment) dummy.childNode(1)).getData());
        assertTrue(dummy.childNode(2) instanceof Comment);
        assertEquals("just comment", ((Comment) dummy.childNode(2)).getData());
    }

    @Test(timeout = 4000)
    public void testProcessAllTokenTypesDirectly() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("<container/>", "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        // 1. Character token
        Token.Character charToken = new Token.Character();
        charToken.data("Sample Text");
        assertTrue(builder.process(charToken));

        // 2. StartTag token (non-self-closing)
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("openTag", new Attributes());
        assertTrue(builder.process(startTag));

        // 3. EndTag token
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("openTag");
        assertTrue(builder.process(endTag));

        // 4. Doctype token
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("custom-doc");
        assertTrue(builder.process(doctypeToken));

        // 5. EOF token
        Token.EOF eofToken = new Token.EOF();
        assertTrue(builder.process(eofToken));
    }

    // =========================================================================
    // PARTITION E: Fragment Parsing & Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFragmentPreservesCase() {
        String fragment = "<FirstChild attrName=\"Value1\">Text1</FirstChild><SecondChild>Text2</SecondChild>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(fragment, "http://example.com/base/", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        assertNotNull(nodes);
        assertEquals(2, nodes.size());

        assertTrue(nodes.get(0) instanceof Element);
        Element first = (Element) nodes.get(0);
        assertEquals("FirstChild", first.tagName());
        assertEquals("Value1", first.attr("attrName"));
        assertEquals("Text1", first.text());
        assertEquals("http://example.com/base/", first.baseUri());

        assertTrue(nodes.get(1) instanceof Element);
        Element second = (Element) nodes.get(1);
        assertEquals("SecondChild", second.tagName());
        assertEquals("Text2", second.text());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithHtmlDefaultSettingsNormalizes() {
        String fragment = "<UPPER TAG=\"VALUE\">Text</UPPER>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(fragment, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        assertEquals(1, nodes.size());
        Element el = (Element) nodes.get(0);
        assertEquals("upper", el.tagName());
        assertTrue(el.hasAttr("tag"));
        assertEquals("VALUE", el.attr("tag"));
    }

    @Test(timeout = 4000)
    public void testCDataSectionParsedAsCharacterData() {
        String xml = "<data><![CDATA[<unescaped & content>]]></data>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element data = doc.selectFirst("data");
        assertNotNull(data);
        assertEquals("<unescaped & content>", data.text());
    }

    @Test(timeout = 4000)
    public void testAdjacentSelfClosingAndTextNodes() {
        String xml = "<root><leaf id=\"1\"/>SiblingText<leaf id=\"2\"/></root>";
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(xml, "http://example.com");

        Element root = doc.selectFirst("root");
        assertNotNull(root);
        assertEquals(3, root.childNodeSize());
        assertEquals("leaf", ((Element) root.childNode(0)).tagName());
        assertEquals("SiblingText", ((TextNode) root.childNode(1)).text());
        assertEquals("leaf", ((Element) root.childNode(2)).tagName());
    }
}