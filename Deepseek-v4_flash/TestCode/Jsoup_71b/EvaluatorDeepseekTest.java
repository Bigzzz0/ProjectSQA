package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: org.jsoup.select.Evaluator (abstract) and its nested classes.
 * 
 * Known Defect (from Defects4J):
 * - SelectorTest::splitOnBr -> SelectorParseException for query 'p:matchText'
 * - SelectorTest::textAsElements -> SelectorParseException for query 'p:matchText:first-child'
 * 
 * The defect is that the pseudo-class ':matchText' is not recognized by the selector parser.
 * This test suite includes a dedicated test method `testMatchTextPseudoClassIsSupported` that
 * directly targets this failure by attempting to select elements using ':matchText' and
 * asserting the expected behavior (i.e., no exception is thrown and the correct elements are returned).
 * 
 * Branch Coverage Targets:
 * - Evaluator.Tag.matches: tagName equalsIgnoreCase (true/false)
 * - Evaluator.TagEndsWith.matches: endsWith (true/false)
 * - Evaluator.Id.matches: id.equals (true/false)
 * - Evaluator.Class.matches: hasClass (true/false)
 * - Evaluator.Attribute.matches: hasAttr (true/false)
 * - Evaluator.AttributeStarting.matches: startsWith (true/false), empty attribute list
 * - Evaluator.AttributeWithValue.matches: hasAttr && value.equalsIgnoreCase(trimmed)
 * - Evaluator.AttributeWithValueNot.matches: !value.equalsIgnoreCase
 * - Evaluator.AttributeWithValueStarting.matches: hasAttr && startsWith
 * - Evaluator.AttributeWithValueEnding.matches: hasAttr && endsWith
 * - Evaluator.AttributeWithValueContaining.matches: hasAttr && contains
 * - Evaluator.AttributeWithValueMatching.matches: hasAttr && pattern.find()
 * - Evaluator.AttributeKeyPair constructor: empty key/value, quoted value normalization
 * - Evaluator.AllElements.matches: always true
 * - Evaluator.IndexLessThan.matches: root != element && siblingIndex < index
 * - Evaluator.IndexGreaterThan.matches: siblingIndex > index
 * - Evaluator.IndexEquals.matches: siblingIndex == index
 * - Evaluator.IsLastChild.matches: parent != null, not Document, siblingIndex == size-1
 * - Evaluator.IsRoot.matches: root is Document ? root.child(0) : root, element == r
 * - Evaluator.IsOnlyChild.matches: parent != null, not Document, siblingElements().size() == 0
 * - Evaluator.IsOnlyOfType.matches: parent != null, not Document, count of same tag == 1
 * - Evaluator.IsEmpty.matches: all child nodes are Comment/XmlDeclaration/DocumentType
 * - Evaluator.CssNthEvaluator.matches: parent null/Document, a==0, (pos-b)*a >= 0 && (pos-b)%a==0
 * - Evaluator.IsNthChild.calculatePosition: elementSiblingIndex()+1
 * - Evaluator.IsNthLastChild.calculatePosition: parent.children().size() - elementSiblingIndex()
 * - Evaluator.IsNthOfType.calculatePosition: count of same tag before element
 * - Evaluator.IsNthLastOfType.calculatePosition: count of same tag from element to end
 * - Evaluator.ContainsText.matches: lowerCase(element.text()).contains(searchText)
 * - Evaluator.ContainsData.matches: lowerCase(element.data()).contains(searchText)
 * - Evaluator.ContainsOwnText.matches: lowerCase(element.ownText()).contains(searchText)
 * - Evaluator.Matches.matches: pattern.matcher(element.text()).find()
 * - Evaluator.MatchesOwn.matches: pattern.matcher(element.ownText()).find()
 * 
 * Boundary Values:
 * - IndexEvaluator: negative index, zero, positive index, Integer.MAX_VALUE
 * - CssNthEvaluator: a=0, a>0, a<0, b=0, b>0, b<0
 * - AttributeKeyPair: empty key, empty value, quoted value with spaces
 * - IsEmpty: empty element, element with only comments, element with text
 * - IsOnlyChild: only child, multiple children, no parent
 * - IsOnlyOfType: only of type, multiple of type, no parent
 * - IsLastChild: last child, not last child, no parent
 * - IsRoot: root element, non-root element, document root
 */
public class EvaluatorDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testTagMatches() {
        Evaluator.Tag tag = new Evaluator.Tag("div");
        Element div = Jsoup.parse("<div></div>").selectFirst("div");
        Element span = Jsoup.parse("<span></span>").selectFirst("span");
        assertTrue(tag.matches(null, div));
        assertFalse(tag.matches(null, span));
        assertEquals("div", tag.toString());
    }

    @Test(timeout = 4000)
    public void testTagEndsWithMatches() {
        Evaluator.TagEndsWith tag = new Evaluator.TagEndsWith("iv");
        Element div = Jsoup.parse("<div></div>").selectFirst("div");
        Element span = Jsoup.parse("<span></span>").selectFirst("span");
        assertTrue(tag.matches(null, div));
        assertFalse(tag.matches(null, span));
        assertEquals("iv", tag.toString());
    }

    @Test(timeout = 4000)
    public void testIdMatches() {
        Evaluator.Id id = new Evaluator.Id("test");
        Element withId = Jsoup.parse("<div id='test'></div>").selectFirst("div");
        Element withoutId = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(id.matches(null, withId));
        assertFalse(id.matches(null, withoutId));
        assertEquals("#test", id.toString());
    }

    @Test(timeout = 4000)
    public void testClassMatches() {
        Evaluator.Class clazz = new Evaluator.Class("foo");
        Element withClass = Jsoup.parse("<div class='foo'></div>").selectFirst("div");
        Element withoutClass = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(clazz.matches(null, withClass));
        assertFalse(clazz.matches(null, withoutClass));
        assertEquals(".foo", clazz.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeMatches() {
        Evaluator.Attribute attr = new Evaluator.Attribute("href");
        Element withAttr = Jsoup.parse("<a href='#'></a>").selectFirst("a");
        Element withoutAttr = Jsoup.parse("<a></a>").selectFirst("a");
        assertTrue(attr.matches(null, withAttr));
        assertFalse(attr.matches(null, withoutAttr));
        assertEquals("[href]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeStartingMatches() {
        Evaluator.AttributeStarting attr = new Evaluator.AttributeStarting("data-");
        Element withData = Jsoup.parse("<div data-x='1'></div>").selectFirst("div");
        Element withoutData = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(attr.matches(null, withData));
        assertFalse(attr.matches(null, withoutData));
        assertEquals("[^data-]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueMatches() {
        Evaluator.AttributeWithValue attr = new Evaluator.AttributeWithValue("href", "http://example.com");
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='http://other.com'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href=http://example.com]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueNotMatches() {
        Evaluator.AttributeWithValueNot attr = new Evaluator.AttributeWithValueNot("href", "http://example.com");
        Element match = Jsoup.parse("<a href='http://other.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href!=http://example.com]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueStartingMatches() {
        Evaluator.AttributeWithValueStarting attr = new Evaluator.AttributeWithValueStarting("href", "http");
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='https://example.com'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href^=http]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueEndingMatches() {
        Evaluator.AttributeWithValueEnding attr = new Evaluator.AttributeWithValueEnding("href", ".com");
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='http://example.org'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href$=.com]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueContainingMatches() {
        Evaluator.AttributeWithValueContaining attr = new Evaluator.AttributeWithValueContaining("href", "example");
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='http://other.org'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href*=example]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueMatchingMatches() {
        Evaluator.AttributeWithValueMatching attr = new Evaluator.AttributeWithValueMatching("href", Pattern.compile("example\\.com"));
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        Element noMatch = Jsoup.parse("<a href='http://other.org'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
        assertFalse(attr.matches(null, noMatch));
        assertEquals("[href~=example\\.com]", attr.toString());
    }

    @Test(timeout = 4000)
    public void testAllElementsMatches() {
        Evaluator.AllElements all = new Evaluator.AllElements();
        Element div = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(all.matches(null, div));
        assertEquals("*", all.toString());
    }

    @Test(timeout = 4000)
    public void testIndexLessThanMatches() {
        Evaluator.IndexLessThan eval = new Evaluator.IndexLessThan(2);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p></div>").selectFirst("div");
        Elements ps = root.children();
        assertTrue(eval.matches(root, ps.get(0)));
        assertTrue(eval.matches(root, ps.get(1)));
        assertFalse(eval.matches(root, ps.get(2)));
        assertEquals(":lt(2)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIndexGreaterThanMatches() {
        Evaluator.IndexGreaterThan eval = new Evaluator.IndexGreaterThan(1);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p></div>").selectFirst("div");
        Elements ps = root.children();
        assertFalse(eval.matches(root, ps.get(0)));
        assertFalse(eval.matches(root, ps.get(1)));
        assertTrue(eval.matches(root, ps.get(2)));
        assertEquals(":gt(1)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIndexEqualsMatches() {
        Evaluator.IndexEquals eval = new Evaluator.IndexEquals(1);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p></div>").selectFirst("div");
        Elements ps = root.children();
        assertFalse(eval.matches(root, ps.get(0)));
        assertTrue(eval.matches(root, ps.get(1)));
        assertFalse(eval.matches(root, ps.get(2)));
        assertEquals(":eq(1)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIsLastChildMatches() {
        Evaluator.IsLastChild eval = new Evaluator.IsLastChild();
        Element root = Jsoup.parse("<div><p></p><p></p></div>").selectFirst("div");
        Elements ps = root.children();
        assertFalse(eval.matches(root, ps.get(0)));
        assertTrue(eval.matches(root, ps.get(1)));
        assertEquals(":last-child", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIsRootMatches() {
        Evaluator.IsRoot eval = new Evaluator.IsRoot();
        Document doc = Jsoup.parse("<html><body></body></html>");
        Element html = doc.selectFirst("html");
        Element body = doc.selectFirst("body");
        assertTrue(eval.matches(doc, html));
        assertFalse(eval.matches(doc, body));
        assertEquals(":root", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIsOnlyChildMatches() {
        Evaluator.IsOnlyChild eval = new Evaluator.IsOnlyChild();
        Element root = Jsoup.parse("<div><p></p></div>").selectFirst("div");
        Element onlyChild = root.child(0);
        assertTrue(eval.matches(root, onlyChild));
        Element root2 = Jsoup.parse("<div><p></p><p></p></div>").selectFirst("div");
        assertFalse(eval.matches(root2, root2.child(0)));
        assertEquals(":only-child", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIsOnlyOfTypeMatches() {
        Evaluator.IsOnlyOfType eval = new Evaluator.IsOnlyOfType();
        Element root = Jsoup.parse("<div><p></p><span></span></div>").selectFirst("div");
        assertTrue(eval.matches(root, root.child(0)));
        assertTrue(eval.matches(root, root.child(1)));
        Element root2 = Jsoup.parse("<div><p></p><p></p></div>").selectFirst("div");
        assertFalse(eval.matches(root2, root2.child(0)));
        assertEquals(":only-of-type", eval.toString());
    }

    @Test(timeout = 4000)
    public void testIsEmptyMatches() {
        Evaluator.IsEmpty eval = new Evaluator.IsEmpty();
        Element empty = Jsoup.parse("<div></div>").selectFirst("div");
        assertTrue(eval.matches(null, empty));
        Element withText = Jsoup.parse("<div>text</div>").selectFirst("div");
        assertFalse(eval.matches(null, withText));
        Element withComment = Jsoup.parse("<div><!-- comment --></div>").selectFirst("div");
        assertTrue(eval.matches(null, withComment));
        assertEquals(":empty", eval.toString());
    }

    @Test(timeout = 4000)
    public void testContainsTextMatches() {
        Evaluator.ContainsText eval = new Evaluator.ContainsText("hello");
        Element withText = Jsoup.parse("<div>Hello World</div>").selectFirst("div");
        Element withoutText = Jsoup.parse("<div>Goodbye</div>").selectFirst("div");
        assertTrue(eval.matches(null, withText));
        assertFalse(eval.matches(null, withoutText));
        assertEquals(":contains(hello)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testContainsDataMatches() {
        Evaluator.ContainsData eval = new Evaluator.ContainsData("data");
        Element withData = Jsoup.parse("<div>some data here</div>").selectFirst("div");
        Element withoutData = Jsoup.parse("<div>other</div>").selectFirst("div");
        assertTrue(eval.matches(null, withData));
        assertFalse(eval.matches(null, withoutData));
        assertEquals(":containsData(data)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testContainsOwnTextMatches() {
        Evaluator.ContainsOwnText eval = new Evaluator.ContainsOwnText("own");
        Element withOwn = Jsoup.parse("<div>own <span>child</span></div>").selectFirst("div");
        Element withoutOwn = Jsoup.parse("<div><span>own</span></div>").selectFirst("div");
        assertTrue(eval.matches(null, withOwn));
        assertFalse(eval.matches(null, withoutOwn));
        assertEquals(":containsOwn(own)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesRegex() {
        Evaluator.Matches eval = new Evaluator.Matches(Pattern.compile("\\d+"));
        Element withDigits = Jsoup.parse("<div>abc 123</div>").selectFirst("div");
        Element withoutDigits = Jsoup.parse("<div>abc</div>").selectFirst("div");
        assertTrue(eval.matches(null, withDigits));
        assertFalse(eval.matches(null, withoutDigits));
        assertEquals(":matches(\\d+)", eval.toString());
    }

    @Test(timeout = 4000)
    public void testMatchesOwnRegex() {
        Evaluator.MatchesOwn eval = new Evaluator.MatchesOwn(Pattern.compile("\\d+"));
        Element withOwnDigits = Jsoup.parse("<div>123 <span>abc</span></div>").selectFirst("div");
        Element withoutOwnDigits = Jsoup.parse("<div><span>123</span></div>").selectFirst("div");
        assertTrue(eval.matches(null, withOwnDigits));
        assertFalse(eval.matches(null, withoutOwnDigits));
        assertEquals(":matchesOwn(\\d+)", eval.toString());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testIndexEvaluatorBoundaries() {
        Evaluator.IndexLessThan lt = new Evaluator.IndexLessThan(0);
        Evaluator.IndexGreaterThan gt = new Evaluator.IndexGreaterThan(Integer.MAX_VALUE);
        Evaluator.IndexEquals eq = new Evaluator.IndexEquals(-1);
        Element root = Jsoup.parse("<div><p></p></div>").selectFirst("div");
        Element p = root.child(0);
        assertFalse(lt.matches(root, p));
        assertFalse(gt.matches(root, p));
        assertFalse(eq.matches(root, p));
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorBoundaries() {
        Evaluator.IsNthChild nth = new Evaluator.IsNthChild(0, 1);
        Element root = Jsoup.parse("<div><p></p><p></p></div>").selectFirst("div");
        assertTrue(nth.matches(root, root.child(0)));
        assertFalse(nth.matches(root, root.child(1)));
        assertEquals(":nth-child(1)", nth.toString());
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorABoundary() {
        Evaluator.IsNthChild nth = new Evaluator.IsNthChild(2, 0);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p><p></p></div>").selectFirst("div");
        assertFalse(nth.matches(root, root.child(0)));
        assertTrue(nth.matches(root, root.child(1)));
        assertFalse(nth.matches(root, root.child(2)));
        assertTrue(nth.matches(root, root.child(3)));
        assertEquals(":nth-child(2n)", nth.toString());
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorNegativeA() {
        Evaluator.IsNthChild nth = new Evaluator.IsNthChild(-1, 3);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p><p></p></div>").selectFirst("div");
        assertFalse(nth.matches(root, root.child(0)));
        assertFalse(nth.matches(root, root.child(1)));
        assertTrue(nth.matches(root, root.child(2)));
        assertFalse(nth.matches(root, root.child(3)));
        assertEquals(":nth-child(-1n+3)", nth.toString());
    }

    @Test(timeout = 4000)
    public void testAttributeKeyPairQuotedValue() {
        Evaluator.AttributeWithValue attr = new Evaluator.AttributeWithValue("href", "\"http://example.com\"");
        Element match = Jsoup.parse("<a href='http://example.com'></a>").selectFirst("a");
        assertTrue(attr.matches(null, match));
    }

    @Test(timeout = 4000)
    public void testIsEmptyWithOnlyComments() {
        Evaluator.IsEmpty eval = new Evaluator.IsEmpty();
        Element withComment = Jsoup.parse("<div><!-- comment --></div>").selectFirst("div");
        assertTrue(eval.matches(null, withComment));
    }

    @Test(timeout = 4000)
    public void testIsOnlyChildNoParent() {
        Evaluator.IsOnlyChild eval = new Evaluator.IsOnlyChild();
        Element detached = new Element("div");
        assertFalse(eval.matches(null, detached));
    }

    @Test(timeout = 4000)
    public void testIsOnlyOfTypeNoParent() {
        Evaluator.IsOnlyOfType eval = new Evaluator.IsOnlyOfType();
        Element detached = new Element("div");
        assertFalse(eval.matches(null, detached));
    }

    @Test(timeout = 4000)
    public void testIsLastChildNoParent() {
        Evaluator.IsLastChild eval = new Evaluator.IsLastChild();
        Element detached = new Element("div");
        assertFalse(eval.matches(null, detached));
    }

    @Test(timeout = 4000)
    public void testIsRootDocumentRoot() {
        Evaluator.IsRoot eval = new Evaluator.IsRoot();
        Document doc = Jsoup.parse("<html><body></body></html>");
        Element html = doc.selectFirst("html");
        assertTrue(eval.matches(doc, html));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Dedicated test for the known defect: ':matchText' pseudo-class is not supported.
     * This test will fail on the defective version because the selector parser throws
     * SelectorParseException when encountering ':matchText'.
     */
    @Test(timeout = 4000)
    public void testMatchTextPseudoClassIsSupported() {
        // This is the exact query from the defect report
        String query = "p:matchText";
        try {
            Elements result = Jsoup.parse("<p>text</p>").select(query);
            // If the parser supports :matchText, it should return the paragraph
            assertEquals(1, result.size());
            assertEquals("text", result.first().text());
        } catch (Exception e) {
            fail("Selector parser should support ':matchText' but threw: " + e.getMessage());
        }
    }

    /**
     * Additional test for the combined query from the defect report.
     */
    @Test(timeout = 4000)
    public void testMatchTextWithFirstChildPseudoClassIsSupported() {
        String query = "p:matchText:first-child";
        try {
            Elements result = Jsoup.parse("<div><p>text</p></div>").select(query);
            assertEquals(1, result.size());
            assertEquals("text", result.first().text());
        } catch (Exception e) {
            fail("Selector parser should support ':matchText:first-child' but threw: " + e.getMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeStartingEmptyPrefix() {
        new Evaluator.AttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairEmptyKey() {
        new Evaluator.AttributeWithValue("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairEmptyValue() {
        new Evaluator.AttributeWithValue("key", "");
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorNoParent() {
        Evaluator.IsNthChild nth = new Evaluator.IsNthChild(0, 1);
        Element detached = new Element("div");
        assertFalse(nth.matches(null, detached));
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorDocumentParent() {
        Evaluator.IsNthChild nth = new Evaluator.IsNthChild(0, 1);
        Document doc = Jsoup.parse("<html><body></body></html>");
        Element html = doc.selectFirst("html");
        assertFalse(nth.matches(doc, html));
    }

    @Test(timeout = 4000)
    public void testIsNthLastChildCalculation() {
        Evaluator.IsNthLastChild nth = new Evaluator.IsNthLastChild(0, 1);
        Element root = Jsoup.parse("<div><p></p><p></p><p></p></div>").selectFirst("div");
        assertFalse(nth.matches(root, root.child(0)));
        assertFalse(nth.matches(root, root.child(1)));
        assertTrue(nth.matches(root, root.child(2)));
        assertEquals(":nth-last-child(1)", nth.toString());
    }

    @Test(timeout = 4000)
    public void testIsNthOfTypeCalculation() {
        Evaluator.IsNthOfType nth = new Evaluator.IsNthOfType(0, 1);
        Element root = Jsoup.parse("<div><p></p><span></span><p></p></div>").selectFirst("div");
        assertTrue(nth.matches(root, root.child(0)));
        assertFalse(nth.matches(root, root.child(1)));
        assertFalse(nth.matches(root, root.child(2)));
        assertEquals(":nth-of-type(1)", nth.toString());
    }

    @Test(timeout = 4000)
    public void testIsNthLastOfTypeCalculation() {
        Evaluator.IsNthLastOfType nth = new Evaluator.IsNthLastOfType(0, 1);
        Element root = Jsoup.parse("<div><p></p><span></span><p></p></div>").selectFirst("div");
        assertFalse(nth.matches(root, root.child(0)));
        assertFalse(nth.matches(root, root.child(1)));
        assertTrue(nth.matches(root, root.child(2)));
        assertEquals(":nth-last-of-type(1)", nth.toString());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToStringRepresentations() {
        assertEquals("div", new Evaluator.Tag("div").toString());
        assertEquals("#id", new Evaluator.Id("id").toString());
        assertEquals(".class", new Evaluator.Class("class").toString());
        assertEquals("[attr]", new Evaluator.Attribute("attr").toString());
        assertEquals("[^prefix]", new Evaluator.AttributeStarting("prefix").toString());
        assertEquals("[key=value]", new Evaluator.AttributeWithValue("key", "value").toString());
        assertEquals("[key!=value]", new Evaluator.AttributeWithValueNot("key", "value").toString());
        assertEquals("[key^=value]", new Evaluator.AttributeWithValueStarting("key", "value").toString());
        assertEquals("[key$=value]", new Evaluator.AttributeWithValueEnding("key", "value").toString());
        assertEquals("[key*=value]", new Evaluator.AttributeWithValueContaining("key", "value").toString());
        assertEquals("*", new Evaluator.AllElements().toString());
        assertEquals(":first-child", new Evaluator.IsFirstChild().toString());
        assertEquals(":last-child", new Evaluator.IsLastChild().toString());
        assertEquals(":root", new Evaluator.IsRoot().toString());
        assertEquals(":only-child", new Evaluator.IsOnlyChild().toString());
        assertEquals(":only-of-type", new Evaluator.IsOnlyOfType().toString());
        assertEquals(":empty", new Evaluator.IsEmpty().toString());
        assertEquals(":contains(text)", new Evaluator.ContainsText("text").toString());
        assertEquals(":containsData(data)", new Evaluator.ContainsData("data").toString());
        assertEquals(":containsOwn(own)", new Evaluator.ContainsOwnText("own").toString());
        assertEquals(":matches(\\d+)", new Evaluator.Matches(Pattern.compile("\\d+")).toString());
        assertEquals(":matchesOwn(\\d+)", new Evaluator.MatchesOwn(Pattern.compile("\\d+")).toString());
    }

    @Test(timeout = 4000)
    public void testIndexEvaluatorToString() {
        assertEquals(":lt(2)", new Evaluator.IndexLessThan(2).toString());
        assertEquals(":gt(2)", new Evaluator.IndexGreaterThan(2).toString());
        assertEquals(":eq(2)", new Evaluator.IndexEquals(2).toString());
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorToString() {
        assertEquals(":nth-child(1)", new Evaluator.IsNthChild(0, 1).toString());
        assertEquals(":nth-child(2n)", new Evaluator.IsNthChild(2, 0).toString());
        assertEquals(":nth-child(2n+1)", new Evaluator.IsNthChild(2, 1).toString());
        assertEquals(":nth-last-child(1)", new Evaluator.IsNthLastChild(0, 1).toString());
        assertEquals(":nth-of-type(1)", new Evaluator.IsNthOfType(0, 1).toString());
        assertEquals(":nth-last-of-type(1)", new Evaluator.IsNthLastOfType(0, 1).toString());
    }

    @Test(timeout = 4000)
    public void testIsFirstOfTypeToString() {
        assertEquals(":first-of-type", new Evaluator.IsFirstOfType().toString());
    }

    @Test(timeout = 4000)
    public void testIsLastOfTypeToString() {
        assertEquals(":last-of-type", new Evaluator.IsLastOfType().toString());
    }

    @Test(timeout = 4000)
    public void testIsFirstChildToString() {
        assertEquals(":first-child", new Evaluator.IsFirstChild().toString());
    }

    @Test(timeout = 4000)
    public void testIsNthChildToString() {
        assertEquals(":nth-child(1)", new Evaluator.IsNthChild(0, 1).toString());
    }

    @Test(timeout = 4000)
    public void testIsNthLastChildToString() {
        assertEquals(":nth-last-child(1)", new Evaluator.IsNthLastChild(0, 1).toString());
    }

    @Test(timeout = 4000)
    public void testIsNthOfTypeToString() {
        assertEquals(":nth-of-type(1)", new Evaluator.IsNthOfType(0, 1).toString());
    }

    @Test(timeout = 4000)
    public void testIsNthLastOfTypeToString() {
        assertEquals(":nth-last-of-type(1)", new Evaluator.IsNthLastOfType(0, 1).toString());
    }
}