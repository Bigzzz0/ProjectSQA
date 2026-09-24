package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.jsoup.parser.XmlTreeBuilder
 * Primary Goal: Achieve maximum branch coverage and explicitly expose the known case-normalization defect
 *               in popStackToClose() when discordant tag casings are processed.
 * ----------------------------------------------------------------------------------------------------
 * Decision Points & Branches Targeted:
 * 1. process(Token token) switch:
 *    - StartTag -> insert(Token.StartTag)
 *    - EndTag -> popStackToClose(Token.EndTag)
 *    - Comment -> insert(Token.Comment)
 *    - Character -> insert(Token.Character)
 *    - Doctype -> insert(Token.Doctype)
 *    - EOF -> break
 * 2. insert(Token.StartTag):
 *    - isSelfClosing() == true && !tag.isKnownTag() -> tag.setSelfClosing()
 *    - isSelfClosing() == true && tag.isKnownTag() -> tag.setSelfClosing() skipped
 *    - isSelfClosing() == false -> stack.add(el)
 * 3. insert(Token.Comment):
 *    - commentToken.bogus == false -> standard Comment node
 *    - commentToken.bogus == true:
 *      * data.length() > 1 && data.startsWith("?") -> XmlDeclaration (isProcessingInstruction = false)
 *      * data.length() > 1 && data.startsWith("!") -> XmlDeclaration (isProcessingInstruction = true)
 *      * data.length() <= 1 -> unchanged Comment node
 *      * !data.startsWith("?") && !data.startsWith("!") -> unchanged Comment node
 * 4. insert(Token.Character):
 *    - token.isCData() == true -> CDataNode
 *    - token.isCData() == false -> TextNode
 * 5. popStackToClose(Token.EndTag):
 *    - matching element found on stack -> pop down through stack to close element
 *    - element not found on stack (firstFound == null) -> early exit / no-op
 *    - [DEFECT ZONE] EndTag casing normalization under ParseSettings.htmlDefault vs ParseSettings.preserveCase
 * 6. parseFragment(...) & parse(Reader/String, baseUri):
 *    - parseFragment returns direct childNodes of doc
 *    - initialiseParse configures doc OutputSettings Syntax to xml and pushes doc to stack
 * ====================================================================================================
 */
public class XmlTreeBuilderGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testDefaultSettingsPreservesCase() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        ParseSettings settings = builder.defaultSettings();
        assertNotNull("Default settings must not be null", settings);
        assertSame("XmlTreeBuilder defaultSettings must be preserveCase", ParseSettings.preserveCase, settings);
    }

    @Test(timeout = 4000)
    public void testParseReaderAndStringEquivalence() {
        XmlTreeBuilder builder1 = new XmlTreeBuilder();
        XmlTreeBuilder builder2 = new XmlTreeBuilder();

        String xml = "<root attr=\"val\"><child>Hello</child></root>";
        String baseUri = "https://example.com/xml";

        Document docFromReader = builder1.parse(new StringReader(xml), baseUri);
        Document docFromString = builder2.parse(xml, baseUri);

        assertEquals(Document.OutputSettings.Syntax.xml, docFromReader.outputSettings().syntax());
        assertEquals(Document.OutputSettings.Syntax.xml, docFromString.outputSettings().syntax());
        assertEquals(docFromReader.outerHtml(), docFromString.outerHtml());
        assertEquals(baseUri, docFromReader.baseUri());
        assertEquals(baseUri, docFromString.baseUri());
        assertEquals(1, docFromString.children().size());
        assertEquals("root", docFromString.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testProcessEofToken() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        boolean processed = builder.process(new Token.EOF());
        assertTrue("Processing EOF token must return true", processed);
    }

    @Test(timeout = 4000)
    public void testPopStackToCloseNestedElements() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<branch><leaf>Content</leaf></branch>", "http://example.com");

        Element branch = doc.child(0);
        assertEquals("branch", branch.tagName());
        assertEquals(1, branch.children().size());

        Element leaf = branch.child(0);
        assertEquals("leaf", leaf.tagName());
        assertEquals("Content", leaf.text());
    }

    @Test(timeout = 4000)
    public void testPopStackOutOfOrderClosesIntermediates() {
        // In XML tree builder, an out-of-order closing tag pops all elements down to firstFound
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<a><b><c></a>", "http://example.com");

        // When </a> is processed, c, b, and a should all be closed and popped
        assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        assertEquals("a", a.tagName());
        assertTrue(a.children().size() > 0);
        Element b = a.child(0);
        assertEquals("b", b.tagName());
        assertTrue(b.children().size() > 0);
        Element c = b.child(0);
        assertEquals("c", c.tagName());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEmptyInputDocument() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("", "http://example.com");
        assertNotNull(doc);
        assertEquals(0, doc.children().size());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test(timeout = 4000)
    public void testSelfClosingUnknownTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<custom-element id=\"test\" />", "http://example.com");
        Element custom = doc.child(0);

        assertEquals("custom-element", custom.tagName());
        assertTrue("Unknown self-closing XML tag must have self-closing flag set", custom.tag().isSelfClosing());
        assertFalse("custom-element should be an unknown tag", custom.tag().isKnownTag());
        assertEquals("test", custom.attr("id"));
    }

    @Test(timeout = 4000)
    public void testSelfClosingKnownTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<div id=\"known\" />", "http://example.com");
        Element div = doc.child(0);

        assertEquals("div", div.tagName());
        assertTrue("Known HTML tag should be recognized as known", div.tag().isKnownTag());
        assertEquals("known", div.attr("id"));
    }

    @Test(timeout = 4000)
    public void testPopStackEndTagNotFoundIsNoOp() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        // Closing tag </nonexistent> does not match any element on stack
        Document doc = builder.parse("<root></nonexistent><child>val</child></root>", "http://example.com");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.children().size());
        assertEquals("child", root.child(0).tagName());
    }

    @Test(timeout = 4000)
    public void testCharacterNodeCDataVsTextNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><![CDATA[cdata <markup> & test]]>plain text</root>", "http://example.com");
        Element root = doc.child(0);

        List<Node> childNodes = root.childNodes();
        assertEquals(2, childNodes.size());
        assertTrue("First child should be CDataNode", childNodes.get(0) instanceof CDataNode);
        assertEquals("cdata <markup> & test", ((CDataNode) childNodes.get(0)).text());

        assertTrue("Second child should be TextNode", childNodes.get(1) instanceof TextNode);
        assertEquals("plain text", ((TextNode) childNodes.get(1)).text());
    }

    @Test(timeout = 4000)
    public void testInsertDoctypeToken() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        String xml = "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\"><book/>";
        Document doc = builder.parse(xml, "http://example.com");

        List<Node> nodes = doc.childNodes();
        assertTrue("Root should contain DocumentType node", nodes.get(0) instanceof DocumentType);

        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("book", doctype.name());
        assertEquals("-//OASIS//DTD DocBook XML//EN", doctype.attr("publicId"));
        assertEquals("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd", doctype.attr("systemId"));
    }

    @Test(timeout = 4000)
    public void testStandardCommentInsertion() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><!-- standard comment here --></root>", "http://example.com");
        Element root = doc.child(0);

        assertEquals(1, root.childNodeSize());
        assertTrue(root.childNode(0) instanceof Comment);
        Comment comment = (Comment) root.childNode(0);
        assertEquals(" standard comment here ", comment.getData());
    }

    @Test(timeout = 4000)
    public void testBogusCommentXmlDeclarationQuestionMark() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "http://example.com");

        assertTrue("First node must be XmlDeclaration", doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertFalse("XML processing instruction with ? should have isProcessingInstruction false", decl.outerHtml().startsWith("<!"));
    }

    @Test(timeout = 4000)
    public void testBogusCommentExclamationMarkDeclaration() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        Token.Comment bogusExclamation = new Token.Comment();
        bogusExclamation.bogus = true;
        bogusExclamation.getData().append("!ENTITY foo \"bar\"!");
        builder.process(bogusExclamation);

        assertEquals(1, builder.doc.childNodeSize());
        assertTrue(builder.doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) builder.doc.childNode(0);
        assertEquals("ENTITY", decl.name());
        assertEquals("\"bar\"", decl.attr("foo"));
        assertTrue("Declaration starting with ! must start with <!", decl.outerHtml().startsWith("<!"));
    }

    @Test(timeout = 4000)
    public void testBogusCommentShortDataBoundary() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        // Sub-boundary 1: data length == 1 starting with '?'
        Token.Comment shortQuestion = new Token.Comment();
        shortQuestion.bogus = true;
        shortQuestion.getData().append("?");
        builder.process(shortQuestion);

        // Sub-boundary 2: data length == 1 starting with '!'
        Token.Comment shortExcl = new Token.Comment();
        shortExcl.bogus = true;
        shortExcl.getData().append("!");
        builder.process(shortExcl);

        // Sub-boundary 3: data length > 1 but without '?' or '!' prefix
        Token.Comment noPrefix = new Token.Comment();
        noPrefix.bogus = true;
        noPrefix.getData().append("plain-bogus");
        builder.process(noPrefix);

        assertEquals(3, builder.doc.childNodeSize());
        // All three branches must fallback to regular Comment nodes (not XmlDeclaration)
        assertTrue(builder.doc.childNode(0) instanceof Comment);
        assertFalse(builder.doc.childNode(0) instanceof XmlDeclaration);
        assertEquals("?", ((Comment) builder.doc.childNode(0)).getData());

        assertTrue(builder.doc.childNode(1) instanceof Comment);
        assertFalse(builder.doc.childNode(1) instanceof XmlDeclaration);
        assertEquals("!", ((Comment) builder.doc.childNode(1)).getData());

        assertTrue(builder.doc.childNode(2) instanceof Comment);
        assertFalse(builder.doc.childNode(2) instanceof XmlDeclaration);
        assertEquals("plain-bogus", ((Comment) builder.doc.childNode(2)).getData());
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    /**
     * TARGETED DEFECT:
     * In defective XmlTreeBuilder.java, popStackToClose(Token.EndTag endTag) uses:
     *     String elName = endTag.name();
     * without normalizing tag casing via settings.normalizeTag(elName).
     * When configured with ParseSettings.htmlDefault (case normalization enabled),
     * StartTag names are normalized to lower-case, but EndTag names remain in their original case.
     * This causes discordant casing (e.g., <DIV>...</DIV> or <P>...</p>) to fail matching in
     * next.nodeName().equals(elName), leaving unclosed elements on the parse stack.
     */
    @Test(timeout = 4000)
    public void normalizesDiscordantTags() {
        Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
        Document document = Jsoup.parse("<div><P><Span><b>test</b></B></SPAN></p></DIV>", "", parser);
        assertEquals("<div>\n <p><span><b>test</b></span></p>\n</div>", document.html());
    }

    @Test(timeout = 4000)
    public void normalizesDiscordantTagsAdjacentSiblings() {
        // When end tags are discordant and not popped, adjacent siblings incorrectly become children
        Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
        Document document = Jsoup.parse("<DIV>One</DIV><DIV>Two</DIV>", "", parser);
        assertEquals("<div>\n One\n</div>\n<div>\n Two\n</div>", document.html());
    }

    @Test(timeout = 4000)
    public void testPreserveCaseSettingsRetainsOriginalCasing() {
        Parser parser = Parser.xmlParser().settings(ParseSettings.preserveCase);
        Document document = Jsoup.parse("<MixedCase ID=\"1\"><SUB>Text</SUB></MixedCase>", "", parser);
        Element root = document.child(0);

        assertEquals("MixedCase", root.tagName());
        assertTrue(root.hasAttr("ID"));
        assertFalse(root.hasAttr("id"));
        assertEquals("SUB", root.child(0).tagName());
        assertEquals("Text", root.child(0).text());
    }

    @Test(timeout = 4000)
    public void testHtmlDefaultSettingsNormalizesAllTagsAndAttributes() {
        Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
        Document document = Jsoup.parse("<MixedCase ID=\"1\"><SUB>Text</SUB></MixedCase>", "", parser);
        Element root = document.child(0);

        assertEquals("mixedcase", root.tagName());
        assertTrue(root.hasAttr("id"));
        assertFalse(root.hasAttr("ID"));
        assertEquals("sub", root.child(0).tagName());
    }

    // ================================================================================================
    // Partition D: Fragment Parsing & Edge Cases
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseFragmentProducesNodesDirectly() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(
                "<item id=\"1\">First</item><item id=\"2\">Second</item><!-- comment -->",
                "http://example.com",
                ParseErrorList.noTracking(),
                ParseSettings.preserveCase
        );

        assertNotNull(nodes);
        assertEquals(3, nodes.size());

        assertTrue(nodes.get(0) instanceof Element);
        Element item1 = (Element) nodes.get(0);
        assertEquals("item", item1.tagName());
        assertEquals("First", item1.text());

        assertTrue(nodes.get(1) instanceof Element);
        Element item2 = (Element) nodes.get(1);
        assertEquals("item", item2.tagName());
        assertEquals("Second", item2.text());

        assertTrue(nodes.get(2) instanceof Comment);
        assertEquals(" comment ", ((Comment) nodes.get(2)).getData());
    }

    @Test(timeout = 4000)
    public void testParseFragmentWithCDataAndDeclarations() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment(
                "<?xml version=\"1.0\"?><![CDATA[raw content]]>",
                "http://example.com",
                ParseErrorList.noTracking(),
                ParseSettings.preserveCase
        );

        assertEquals(2, nodes.size());
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        assertTrue(nodes.get(1) instanceof CDataNode);
        assertEquals("raw content", ((CDataNode) nodes.get(1)).text());
    }

    @Test(timeout = 4000)
    public void testUnclosedElementsAtEofPreservedInHierarchy() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<branch1><branch2><leaf>Text", "http://example.com");

        assertEquals(1, doc.children().size());
        Element b1 = doc.child(0);
        assertEquals("branch1", b1.tagName());
        Element b2 = b1.child(0);
        assertEquals("branch2", b2.tagName());
        Element leaf = b2.child(0);
        assertEquals("leaf", leaf.tagName());
        assertEquals("Text", leaf.text());
    }
}