package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 - Defect Targeted: Defects4J selector parsing failure on ':matchText' (e.g. 'p:matchText', 'p:matchText:first-child').
   Targeting via query execution asserting expected element matches on text-node pseudo selector.
 - Evaluator.Tag: case-insensitivity on element.tagName() vs tagName; toString().
 - Evaluator.TagEndsWith: tag ends with suffix check; toString().
 - Evaluator.Id: exact match on id; toString().
 - Evaluator.Class: hasClass match and toString().
 - Evaluator.Attribute: hasAttr checks; toString().
 - Evaluator.AttributeStarting: prefix validation (notEmpty), case normalization, iterating attributes, match vs no match; toString().
 - Evaluator.AttributeKeyPair: quotes stripping ("..." or '...'), key/value normalization; Validate.notEmpty guards.
 - Evaluator.AttributeWithValue: trimmed case-insensitive comparison, missing attribute check; toString().
 - Evaluator.AttributeWithValueNot: inequality check when attribute present vs absent; toString().
 - Evaluator.AttributeWithValueStarting: value prefix check; toString().
 - Evaluator.AttributeWithValueEnding: value suffix check; toString().
 - Evaluator.AttributeWithValueContaining: value substring check; toString().
 - Evaluator.AttributeWithValueMatching: pattern regex matcher find() against attribute value; toString().
 - Evaluator.AllElements: always true; toString() = "*".
 - Evaluator.IndexLessThan: root == element vs root != element; index comparisons; toString().
 - Evaluator.IndexGreaterThan: elementSiblingIndex > index; toString().
 - Evaluator.IndexEquals: elementSiblingIndex == index; toString().
 - Evaluator.IsLastChild: parent == null, parent is Document, last sibling index check; toString().
 - Evaluator.IsFirstChild: parent == null, parent is Document, sibling index == 0 check; toString().
 - Evaluator.IsRoot: root is Document vs regular Element; matches target; toString().
 - Evaluator.IsOnlyChild: parent null, parent is Document, siblingElements.isEmpty(); toString().
 - Evaluator.IsOnlyOfType: parent null, parent is Document, count of matching tag type == 1; toString().
 - Evaluator.IsEmpty: childNodes scanning ignoring Comment, XmlDeclaration, DocumentType; non-empty with TextNode or Element; toString().
 - Evaluator.CssNthEvaluator & Subclasses (IsNthChild, IsNthLastChild, IsNthOfType, IsNthLastOfType, IsFirstOfType, IsLastOfType):
   - parent null or parent is Document returning false.
   - a == 0 vs a != 0; (pos-b)*a >= 0 and modulo condition ((pos-b)%a == 0).
   - toString() formatting for a == 0, b == 0, and non-zero a and b (positive and negative b).
   - calculatePosition() variants for nth-child, nth-last-child, nth-of-type, and nth-last-of-type.
 - Evaluator.ContainsText: element text lowerCase contains; toString().
 - Evaluator.ContainsData: element data lowerCase contains; toString().
 - Evaluator.ContainsOwnText: element ownText lowerCase contains; toString().
 - Evaluator.Matches: regex Pattern matching element.text(); toString().
 - Evaluator.MatchesOwn: regex Pattern matching element.ownText(); toString().
*/
public class EvaluatorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTagEvaluatorMatchesAndToString() {
        Evaluator.Tag eval = new Evaluator.Tag("DIV");
        assertEquals("DIV", eval.toString());

        Element root = new Element("body");
        Element div = new Element("div");
        Element span = new Element("span");

        assertTrue(eval.matches(root, div));
        assertFalse(eval.matches(root, span));
    }

    @Test(timeout = 4000)
    public void testTagEndsWithEvaluatorMatchesAndToString() {
        Evaluator.TagEndsWith eval = new Evaluator.TagEndsWith("script");
        assertEquals("script", eval.toString());

        Element root = new Element("body");
        Element customScript = new Element("myscript");
        Element div = new Element("div");

        assertTrue(eval.matches(root, customScript));
        assertFalse(eval.matches(root, div));
    }

    @Test(timeout = 4000)
    public void testIdEvaluatorMatchesAndToString() {
        Evaluator.Id eval = new Evaluator.Id("main");
        assertEquals("#main", eval.toString());

        Element root = new Element("body");
        Element elWithId = new Element("div").attr("id", "main");
        Element elOtherId = new Element("div").attr("id", "other");

        assertTrue(eval.matches(root, elWithId));
        assertFalse(eval.matches(root, elOtherId));
    }

    @Test(timeout = 4000)
    public void testClassEvaluatorMatchesAndToString() {
        Evaluator.Class eval = new Evaluator.Class("active");
        assertEquals(".active", eval.toString());

        Element root = new Element("body");
        Element el = new Element("div").addClass("btn").addClass("active");
        Element elNoMatch = new Element("div").addClass("btn");

        assertTrue(eval.matches(root, el));
        assertFalse(eval.matches(root, elNoMatch));
    }

    @Test(timeout = 4000)
    public void testAttributeEvaluator() {
        Evaluator.Attribute eval = new Evaluator.Attribute("disabled");
        assertEquals("[disabled]", eval.toString());

        Element root = new Element("body");
        Element el = new Element("button").attr("disabled", "");
        Element elNo = new Element("button");

        assertTrue(eval.matches(root, el));
        assertFalse(eval.matches(root, elNo));
    }

    @Test(timeout = 4000)
    public void testAttributeStartingEvaluator() {
        Evaluator.AttributeStarting eval = new Evaluator.AttributeStarting("data-");
        assertEquals("[^data-]", eval.toString());

        Element root = new Element("body");
        Element el = new Element("div").attr("data-role", "test");
        Element elUpper = new Element("div").attr("DATA-USER", "admin");
        Element elNo = new Element("div").attr("href", "http://example.com");

        assertTrue(eval.matches(root, el));
        assertTrue(eval.matches(root, elUpper));
        assertFalse(eval.matches(root, elNo));
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueVariations() {
        Evaluator.AttributeWithValue eval = new Evaluator.AttributeWithValue("title", "Hello World");
        assertEquals("[title=hello world]", eval.toString());

        Element root = new Element("body");
        Element el1 = new Element("a").attr("title", "  hello world  ");
        Element el2 = new Element("a").attr("title", "other");
        Element elNoAttr = new Element("a");

        assertTrue(eval.matches(root, el1));
        assertFalse(eval.matches(root, el2));
        assertFalse(eval.matches(root, elNoAttr));
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueNot() {
        Evaluator.AttributeWithValueNot eval = new Evaluator.AttributeWithValueNot("type", "hidden");
        assertEquals("[type!=hidden]", eval.toString());

        Element root = new Element("body");
        Element elText = new Element("input").attr("type", "text");
        Element elHidden = new Element("input").attr("type", "hidden");
        Element elNoAttr = new Element("input");

        assertTrue(eval.matches(root, elText));
        assertFalse(eval.matches(root, elHidden));
        assertTrue(eval.matches(root, elNoAttr));
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueStartingEndingContaining() {
        Evaluator.AttributeWithValueStarting startEval = new Evaluator.AttributeWithValueStarting("href", "https");
        assertEquals("[href^=https]", startEval.toString());

        Evaluator.AttributeWithValueEnding endEval = new Evaluator.AttributeWithValueEnding("href", ".pdf");
        assertEquals("[href$=.pdf]", endEval.toString());

        Evaluator.AttributeWithValueContaining contEval = new Evaluator.AttributeWithValueContaining("href", "report");
        assertEquals("[href*=report]", contEval.toString());

        Element root = new Element("body");
        Element target = new Element("a").attr("href", "https://site.org/report-2023.pdf");
        Element nonTarget = new Element("a").attr("href", "http://site.org/index.html");
        Element missingAttr = new Element("a");

        assertTrue(startEval.matches(root, target));
        assertFalse(startEval.matches(root, nonTarget));
        assertFalse(startEval.matches(root, missingAttr));

        assertTrue(endEval.matches(root, target));
        assertFalse(endEval.matches(root, nonTarget));
        assertFalse(endEval.matches(root, missingAttr));

        assertTrue(contEval.matches(root, target));
        assertFalse(contEval.matches(root, nonTarget));
        assertFalse(contEval.matches(root, missingAttr));
    }

    @Test(timeout = 4000)
    public void testAttributeWithValueMatching() {
        Pattern pattern = Pattern.compile("^v\\d+$");
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("data-version", pattern);
        assertEquals("[data-version~=^v\\d+$]", eval.toString());

        Element root = new Element("body");
        Element match = new Element("div").attr("data-version", "v12");
        Element noMatch = new Element("div").attr("data-version", "ver12");
        Element missing = new Element("div");

        assertTrue(eval.matches(root, match));
        assertFalse(eval.matches(root, noMatch));
        assertFalse(eval.matches(root, missing));
    }

    @Test(timeout = 4000)
    public void testAllElements() {
        Evaluator.AllElements eval = new Evaluator.AllElements();
        assertEquals("*", eval.toString());

        Element root = new Element("body");
        Element el = new Element("div");
        assertTrue(eval.matches(root, el));
        assertTrue(eval.matches(null, null));
    }

    @Test(timeout = 4000)
    public void testIndexEvaluators() {
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li></ul>");
        Element ul = doc.selectFirst("ul");
        Elements children = ul.children();

        Evaluator.IndexLessThan lt = new Evaluator.IndexLessThan(2);
        assertEquals(":lt(2)", lt.toString());
        assertTrue(lt.matches(ul, children.get(0)));
        assertTrue(lt.matches(ul, children.get(1)));
        assertFalse(lt.matches(ul, children.get(2)));
        assertFalse(lt.matches(children.get(0), children.get(0))); // root == element returns false

        Evaluator.IndexGreaterThan gt = new Evaluator.IndexGreaterThan(1);
        assertEquals(":gt(1)", gt.toString());
        assertFalse(gt.matches(ul, children.get(0)));
        assertFalse(gt.matches(ul, children.get(1)));
        assertTrue(gt.matches(ul, children.get(2)));
        assertTrue(gt.matches(ul, children.get(3)));

        Evaluator.IndexEquals eq = new Evaluator.IndexEquals(2);
        assertEquals(":eq(2)", eq.toString());
        assertFalse(eq.matches(ul, children.get(1)));
        assertTrue(eq.matches(ul, children.get(2)));
        assertFalse(eq.matches(ul, children.get(3)));
    }

    @Test(timeout = 4000)
    public void testFirstAndLastChild() {
        Document doc = Jsoup.parse("<div><span>First</span><p>Second</p><span>Last</span></div>");
        Element div = doc.selectFirst("div");
        Elements children = div.children();

        Evaluator.IsFirstChild firstChild = new Evaluator.IsFirstChild();
        assertEquals(":first-child", firstChild.toString());
        assertTrue(firstChild.matches(div, children.get(0)));
        assertFalse(firstChild.matches(div, children.get(1)));

        Evaluator.IsLastChild lastChild = new Evaluator.IsLastChild();
        assertEquals(":last-child", lastChild.toString());
        assertFalse(lastChild.matches(div, children.get(0)));
        assertTrue(lastChild.matches(div, children.get(2)));

        Element orphan = new Element("div");
        assertFalse(firstChild.matches(null, orphan));
        assertFalse(lastChild.matches(null, orphan));

        // When parent is Document
        assertFalse(firstChild.matches(doc, doc.child(0)));
        assertFalse(lastChild.matches(doc, doc.child(0)));
    }

    @Test(timeout = 4000)
    public void testIsRoot() {
        Document doc = Jsoup.parse("<html><body><div>Root Test</div></body></html>");
        Element html = doc.child(0);
        Element div = doc.selectFirst("div");

        Evaluator.IsRoot rootEval = new Evaluator.IsRoot();
        assertEquals(":root", rootEval.toString());

        assertTrue(rootEval.matches(doc, html));
        assertFalse(rootEval.matches(doc, div));

        Element standaloneRoot = new Element("section");
        Element standaloneChild = standaloneRoot.appendElement("p");
        assertTrue(rootEval.matches(standaloneRoot, standaloneRoot));
        assertFalse(rootEval.matches(standaloneRoot, standaloneChild));
    }

    @Test(timeout = 4000)
    public void testIsOnlyChildAndIsOnlyOfType() {
        Document doc = Jsoup.parse(
                "<div id='single'><span>Only</span></div>" +
                "<div id='multi'><span>First</span><p>Only Type P</p><span>Second</span></div>"
        );
        Element singleSpan = doc.selectFirst("#single > span");
        Element multiP = doc.selectFirst("#multi > p");
        Element multiSpan = doc.selectFirst("#multi > span");

        Evaluator.IsOnlyChild onlyChild = new Evaluator.IsOnlyChild();
        assertEquals(":only-child", onlyChild.toString());
        assertTrue(onlyChild.matches(doc, singleSpan));
        assertFalse(onlyChild.matches(doc, multiP));

        Evaluator.IsOnlyOfType onlyOfType = new Evaluator.IsOnlyOfType();
        assertEquals(":only-of-type", onlyOfType.toString());
        assertTrue(onlyOfType.matches(doc, singleSpan));
        assertTrue(onlyOfType.matches(doc, multiP));
        assertFalse(onlyOfType.matches(doc, multiSpan));

        Element orphan = new Element("div");
        assertFalse(onlyChild.matches(null, orphan));
        assertFalse(onlyOfType.matches(null, orphan));

        assertFalse(onlyChild.matches(doc, doc.child(0)));
        assertFalse(onlyOfType.matches(doc, doc.child(0)));
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        Document doc = Jsoup.parse(
                "<div id='empty1'></div>" +
                "<div id='empty2'><!-- comment --><?xml version='1.0'?></div>" +
                "<div id='notEmptyText'>text</div>" +
                "<div id='notEmptyChild'><span></span></div>"
        );
        Evaluator.IsEmpty isEmpty = new Evaluator.IsEmpty();
        assertEquals(":empty", isEmpty.toString());

        assertTrue(isEmpty.matches(doc, doc.selectFirst("#empty1")));
        assertTrue(isEmpty.matches(doc, doc.selectFirst("#empty2")));
        assertFalse(isEmpty.matches(doc, doc.selectFirst("#notEmptyText")));
        assertFalse(isEmpty.matches(doc, doc.selectFirst("#notEmptyChild")));
    }

    @Test(timeout = 4000)
    public void testContainsAndMatchesFamily() {
        Document doc = Jsoup.parse("<div><p id='target'>Hello <b>World</b>!</p><script id='sc'>var x = 42;</script></div>");
        Element p = doc.selectFirst("#target");
        Element sc = doc.selectFirst("#sc");

        Evaluator.ContainsText containsText = new Evaluator.ContainsText("HELLO WORLD");
        assertEquals(":contains(hello world)", containsText.toString());
        assertTrue(containsText.matches(doc, p));
        assertFalse(containsText.matches(doc, sc));

        Evaluator.ContainsOwnText containsOwnText = new Evaluator.ContainsOwnText("HELLO");
        assertEquals(":containsOwn(hello)", containsOwnText.toString());
        assertTrue(containsOwnText.matches(doc, p));

        Evaluator.ContainsOwnText containsOwnTextFail = new Evaluator.ContainsOwnText("WORLD");
        assertFalse(containsOwnTextFail.matches(doc, p)); // "World" is inside <b>, not ownText

        Evaluator.ContainsData containsData = new Evaluator.ContainsData("var x");
        assertEquals(":containsData(var x)", containsData.toString());
        assertTrue(containsData.matches(doc, sc));
        assertFalse(containsData.matches(doc, p));

        Pattern regexAll = Pattern.compile("H[e-l]+o");
        Evaluator.Matches matches = new Evaluator.Matches(regexAll);
        assertEquals(":matches(" + regexAll + ")", matches.toString());
        assertTrue(matches.matches(doc, p));

        Pattern regexOwn = Pattern.compile("^Hello\\s+!$");
        Evaluator.MatchesOwn matchesOwn = new Evaluator.MatchesOwn(regexOwn);
        assertEquals(":matchesOwn(" + regexOwn + ")", matchesOwn.toString());
        assertTrue(matchesOwn.matches(doc, p));

        Pattern regexOwnFail = Pattern.compile("World");
        Evaluator.MatchesOwn matchesOwnFail = new Evaluator.MatchesOwn(regexOwnFail);
        assertFalse(matchesOwnFail.matches(doc, p));
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorsLogicAndToString() {
        // String formatting branches for CssNthEvaluator:
        // a == 0 -> :pseudo(b)
        // b == 0 -> :pseudo(an)
        // a != 0, b != 0 -> :pseudo(an+b) or :pseudo(an-b)
        Evaluator.IsNthChild c1 = new Evaluator.IsNthChild(0, 3);
        assertEquals(":nth-child(3)", c1.toString());

        Evaluator.IsNthChild c2 = new Evaluator.IsNthChild(2, 0);
        assertEquals(":nth-child(2n)", c2.toString());

        Evaluator.IsNthChild c3 = new Evaluator.IsNthChild(2, 1);
        assertEquals(":nth-child(2n+1)", c3.toString());

        Evaluator.IsNthChild c4 = new Evaluator.IsNthChild(2, -1);
        assertEquals(":nth-child(2n-1)", c4.toString());

        Document doc = Jsoup.parse(
                "<ol>" +
                "<li>1</li><li>2</li><li>3</li><li>4</li><li>5</li><li>6</li>" +
                "</ol>"
        );
        Element ol = doc.selectFirst("ol");
        Elements items = ol.children();

        // 2n+1 matches pos 1 (item 0), pos 3 (item 2), pos 5 (item 4)
        assertTrue(c3.matches(ol, items.get(0)));
        assertFalse(c3.matches(ol, items.get(1)));
        assertTrue(c3.matches(ol, items.get(2)));
        assertFalse(c3.matches(ol, items.get(3)));
        assertTrue(c3.matches(ol, items.get(4)));
        assertFalse(c3.matches(ol, items.get(5)));

        // parent null or Document returns false
        assertFalse(c3.matches(null, new Element("li")));
        assertFalse(c3.matches(doc, doc.child(0)));

        // IsNthLastChild
        Evaluator.IsNthLastChild lastChildEval = new Evaluator.IsNthLastChild(0, 2);
        assertEquals(":nth-last-child(2)", lastChildEval.toString());
        // 6 elements total: index 4 has position 6 - 4 = 2 from last
        assertTrue(lastChildEval.matches(ol, items.get(4)));
        assertFalse(lastChildEval.matches(ol, items.get(5)));
    }

    @Test(timeout = 4000)
    public void testOfTypeEvaluators() {
        Document doc = Jsoup.parse(
                "<div>" +
                "<p>P1</p><span>S1</span><p>P2</p><p>P3</p><span>S2</span>" +
                "</div>"
        );
        Element div = doc.selectFirst("div");
        Elements children = div.children();
        // children: 0: P1, 1: S1, 2: P2, 3: P3, 4: S2

        Evaluator.IsFirstOfType firstOfType = new Evaluator.IsFirstOfType();
        assertEquals(":first-of-type", firstOfType.toString());
        assertTrue(firstOfType.matches(div, children.get(0))); // P1
        assertTrue(firstOfType.matches(div, children.get(1))); // S1
        assertFalse(firstOfType.matches(div, children.get(2))); // P2

        Evaluator.IsLastOfType lastOfType = new Evaluator.IsLastOfType();
        assertEquals(":last-of-type", lastOfType.toString());
        assertTrue(lastOfType.matches(div, children.get(3))); // P3
        assertTrue(lastOfType.matches(div, children.get(4))); // S2
        assertFalse(lastOfType.matches(div, children.get(0))); // P1

        Evaluator.IsNthOfType nthOfType = new Evaluator.IsNthOfType(0, 2);
        assertEquals(":nth-of-type(2)", nthOfType.toString());
        assertFalse(nthOfType.matches(div, children.get(0))); // P1 (pos 1)
        assertTrue(nthOfType.matches(div, children.get(2)));  // P2 (pos 2)
        assertTrue(nthOfType.matches(div, children.get(4)));  // S2 (pos 2 of span)

        Evaluator.IsNthLastOfType nthLastOfType = new Evaluator.IsNthLastOfType(0, 1);
        assertEquals(":nth-last-of-type(1)", nthLastOfType.toString());
        assertTrue(nthLastOfType.matches(div, children.get(3))); // P3 (last p)
        assertTrue(nthLastOfType.matches(div, children.get(4))); // S2 (last span)
        assertFalse(nthLastOfType.matches(div, children.get(0))); // P1
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAttributeKeyPairQuotingVariations() {
        // Test single quotes, double quotes, and unquoted normalization
        Evaluator.AttributeWithValue doubleQuoted = new Evaluator.AttributeWithValue("key", "\"my value\"");
        assertEquals("[key=my value]", doubleQuoted.toString());

        Evaluator.AttributeWithValue singleQuoted = new Evaluator.AttributeWithValue("key", "'my value'");
        assertEquals("[key=my value]", singleQuoted.toString());

        Evaluator.AttributeWithValue mixedQuote = new Evaluator.AttributeWithValue("key", "\"my value'");
        assertEquals("[key=\"my value']", mixedQuote.toString());

        Evaluator.AttributeWithValue shortQuote = new Evaluator.AttributeWithValue("key", "\"");
        assertEquals("[key=\"]", shortQuote.toString());
    }

    @Test(timeout = 4000)
    public void testCssNthEvaluatorNegativeStepAndPosBoundaries() {
        // Step calculation where (pos-b)*a < 0 or (pos-b)%a != 0
        // -2n + 5: for n=1 -> 3, n=2 -> 1. pos <= 5
        Evaluator.IsNthChild eval = new Evaluator.IsNthChild(-2, 5);
        assertEquals(":nth-child(-2n+5)", eval.toString());

        Document doc = Jsoup.parse("<ul><li>1</li><li>2</li><li>3</li><li>4</li><li>5</li><li>6</li></ul>");
        Element ul = doc.selectFirst("ul");
        Elements lis = ul.children();

        // pos 1: (1-5)*(-2) = (-4)*(-2) = 8 >= 0, (1-5)%(-2) == 0 -> match
        assertTrue(eval.matches(ul, lis.get(0)));
        // pos 2: (2-5)%(-2) != 0 -> no match
        assertFalse(eval.matches(ul, lis.get(1)));
        // pos 3: (3-5)*(-2) = 4 >= 0, (-2)%(-2) == 0 -> match
        assertTrue(eval.matches(ul, lis.get(2)));
        // pos 5: (5-5)*(-2) = 0 >= 0, 0%(-2) == 0 -> match
        assertTrue(eval.matches(ul, lis.get(4)));
        // pos 6: (6-5)*(-2) = -2 < 0 -> no match
        assertFalse(eval.matches(ul, lis.get(5)));
    }

    @Test(timeout = 4000)
    public void testIsEmptyWithDocumentTypeAndWhitespace() {
        Element el = new Element("div");
        el.appendChild(new DocumentType("html", "", ""));
        Evaluator.IsEmpty isEmpty = new Evaluator.IsEmpty();
        assertTrue(isEmpty.matches(null, el));

        // Adding text node causes isEmpty to be false
        el.appendChild(new TextNode(" "));
        assertFalse(isEmpty.matches(null, el));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (SelectorParseException on :matchText)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchTextPseudoSelectorOnTextNodesBuggyInTarget() {
        // Ground truth defect specifies SelectorParseException: Could not parse query 'p:matchText'
        // and 'p:matchText:first-child'.
        // Testing that the selector engine correctly parses and evaluates :matchText query.
        Document doc = Jsoup.parse("<div><p>Hello<br>World</p></div>");
        Elements elements = doc.select("p:matchText");
        assertNotNull(elements);
        assertEquals(2, elements.size());
        assertEquals("Hello", elements.get(0).text());
        assertEquals("World", elements.get(1).text());

        Elements firstChildText = doc.select("p:matchText:first-child");
        assertNotNull(firstChildText);
        assertEquals(1, firstChildText.size());
        assertEquals("Hello", firstChildText.first().text());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeStartingThrowsOnEmptyPrefix() {
        new Evaluator.AttributeStarting("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeStartingThrowsOnNullPrefix() {
        new Evaluator.AttributeStarting(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairThrowsOnEmptyKey() {
        new Evaluator.AttributeWithValue("", "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairThrowsOnNullKey() {
        new Evaluator.AttributeWithValue(null, "val");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairThrowsOnEmptyValue() {
        new Evaluator.AttributeWithValue("key", "");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAttributeKeyPairThrowsOnNullValue() {
        new Evaluator.AttributeWithValue("key", null);
    }

    // =========================================================================
    // Partition E: Object Hierarchy & Additional Constructor Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleArgCssNthEvaluatorConstructor() {
        Evaluator.CssNthEvaluator eval = new Evaluator.CssNthEvaluator(4) {
            @Override
            protected String getPseudoClass() {
                return "custom";
            }

            @Override
            protected int calculatePosition(Element root, Element element) {
                return 4;
            }
        };
        assertEquals(":custom(4)", eval.toString());

        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        assertTrue(eval.matches(parent, child));
    }
}