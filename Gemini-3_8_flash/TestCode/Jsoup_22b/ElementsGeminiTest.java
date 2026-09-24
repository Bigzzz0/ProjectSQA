package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: org.jsoup.select.Elements
 *
 * Decision / Branch Coverage Targets:
 * - Constructors:
 *     - Elements()
 *     - Elements(Collection<Element>)
 *     - Elements(List<Element>)
 *     - Elements(Element...)
 * - clone(): creates deep copy of elements; verifies state independence.
 * - attr(attributeKey):
 *     - empty contents -> returns ""
 *     - element with attr -> returns attr value
 *     - element without attr -> falls through to next / returns ""
 * - hasAttr(attributeKey):
 *     - true if any has attr, false otherwise (including empty contents)
 * - attr(k, v), removeAttr(k): executes on all elements in contents.
 * - addClass(cls), removeClass(cls), toggleClass(cls), hasClass(cls):
 *     - hasClass: true if any has class, false if none/empty
 *     - modifications applied to all elements
 * - val():
 *     - size() > 0 -> returns first().val()
 *     - size() == 0 -> returns ""
 * - val(String): sets value on all elements
 * - text():
 *     - joins elements text with single space separator
 *     - empty contents returns ""
 * - hasText():
 *     - true if any element hasText(), false if none or empty
 * - html(), outerHtml(), toString():
 *     - joins inner/outer html with newline separator "\n"
 *     - empty contents returns ""
 * - tagName(String), html(String), prepend(String), append(String),
 *   before(String), after(String):
 *     - applied across all elements
 * - wrap(html):
 *     - Validate.notEmpty(html) -> checks non-null and non-empty
 *     - applies wrap to all elements
 * - unwrap(), empty(), remove():
 *     - applied across all elements
 * - select(query), not(query):
 *     - filter integration with Selector
 * - eq(index):
 *     - contents.size() > index -> Elements(get(index))
 *     - contents.size() <= index -> empty Elements()
 * - is(query):
 *     - selector matches: return true; no match or empty: return false
 * - parents():
 *     - deduplicated collection of ancestors (LinkedHashSet) across all elements
 * - first(), last():
 *     - empty -> returns null
 *     - non-empty -> returns first / last element
 * - traverse(NodeVisitor):
 *     - Validate.notNull(visitor) check
 *     - traverses all elements
 * - List delegation methods:
 *     - size, isEmpty, contains, iterator, toArray(), toArray(T[]), add, remove(Object),
 *       containsAll, addAll, addAll(index, c), removeAll, retainAll, clear, equals,
 *       hashCode, get, set, add(index), remove(index), indexOf, lastIndexOf,
 *       listIterator(), listIterator(index), subList.
 *
 * Known Defect Coverage:
 * - Element.siblingElements() / siblingNodes() counting issue where element considered
 *   sibling of itself (Defects4J ground truth: expected 2 siblings, was 3; orphan node NPE).
 *   Targeted via Elements interacting with sibling/parent DOM trees.
 */
public class ElementsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndListOperations() {
        Elements emptyList = new Elements();
        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());

        Element p1 = new Element("p");
        Element p2 = new Element("p");
        Elements fromArray = new Elements(p1, p2);
        assertEquals(2, fromArray.size());
        assertSame(p1, fromArray.get(0));
        assertSame(p2, fromArray.get(1));

        List<Element> list = new ArrayList<Element>();
        list.add(p1);
        Elements fromList = new Elements(list);
        assertEquals(1, fromList.size());

        Collection<Element> col = new HashSet<Element>(list);
        Elements fromCol = new Elements(col);
        assertEquals(1, fromCol.size());
    }

    @Test(timeout = 4000)
    public void testAttributesAndClasses() {
        Document doc = Jsoup.parse("<div id='d1' class='c1'>A</div><div id='d2'>B</div>");
        Elements divs = doc.select("div");

        assertTrue(divs.hasAttr("id"));
        assertEquals("d1", divs.attr("id"));
        assertFalse(divs.hasAttr("nonexistent"));
        assertEquals("", divs.attr("nonexistent"));

        divs.attr("data-test", "val1");
        for (Element el : divs) {
            assertEquals("val1", el.attr("data-test"));
        }

        divs.removeAttr("data-test");
        for (Element el : divs) {
            assertFalse(el.hasAttr("data-test"));
        }

        assertTrue(divs.hasClass("c1"));
        assertFalse(divs.hasClass("c2"));

        divs.addClass("c2");
        assertTrue(divs.hasClass("c2"));

        divs.removeClass("c1");
        assertFalse(divs.hasClass("c1"));

        divs.toggleClass("toggled");
        for (Element el : divs) {
            assertTrue(el.hasClass("toggled"));
        }
        divs.toggleClass("toggled");
        for (Element el : divs) {
            assertFalse(el.hasClass("toggled"));
        }
    }

    @Test(timeout = 4000)
    public void testFormValAndTextMethods() {
        Document doc = Jsoup.parse("<input value='first'/><textarea>second</textarea>");
        Elements inputs = doc.select("input, textarea");

        assertEquals("first", inputs.val());

        inputs.val("updated");
        for (Element el : inputs) {
            assertEquals("updated", el.val());
        }

        Document textDoc = Jsoup.parse("<p>Hello</p><p>World</p><div></div>");
        Elements paragraphs = textDoc.select("p");
        assertTrue(paragraphs.hasText());
        assertEquals("Hello World", paragraphs.text());

        Elements emptyDiv = textDoc.select("div");
        assertFalse(emptyDiv.hasText());
        assertEquals("", emptyDiv.text());
    }

    @Test(timeout = 4000)
    public void testHtmlManipulations() {
        Document doc = Jsoup.parse("<div id='1'><i>1</i></div><div id='2'><i>2</i></div>");
        Elements divs = doc.select("div");

        assertEquals("<i>1</i>\n<i>2</i>", divs.html());
        assertEquals("<div id=\"1\">\n <i>1</i>\n</div>\n<div id=\"2\">\n <i>2</i>\n</div>", divs.outerHtml());
        assertEquals(divs.outerHtml(), divs.toString());

        divs.tagName("section");
        assertEquals(2, doc.select("section").size());
        assertEquals(0, doc.select("div").size());

        divs.html("<span>span-content</span>");
        for (Element el : divs) {
            assertEquals("<span>span-content</span>", el.html());
        }

        divs.prepend("<b>before-span</b>");
        divs.append("<b>after-span</b>");
        for (Element el : divs) {
            assertEquals("<b>before-span</b><span>span-content</span><b>after-span</b>", el.html());
        }
    }

    @Test(timeout = 4000)
    public void testBeforeAfterWrapUnwrapEmptyRemove() {
        Document doc = Jsoup.parse("<div id='root'><p id='p1'>Para 1</p><p id='p2'>Para 2</p></div>");
        Elements ps = doc.select("p");

        ps.before("<span class='before'>Pre</span>");
        assertEquals(2, doc.select("span.before").size());

        ps.after("<span class='after'>Post</span>");
        assertEquals(2, doc.select("span.after").size());

        ps.wrap("<div class='wrapper'></div>");
        assertEquals(2, doc.select("div.wrapper").size());

        ps.unwrap();
        assertEquals(0, doc.select("div.wrapper").size());

        ps.empty();
        for (Element p : ps) {
            assertEquals("", p.html());
            assertEquals(0, p.childNodeSize());
        }

        ps.remove();
        assertEquals(0, doc.select("p").size());
    }

    @Test(timeout = 4000)
    public void testFiltersAndQueries() {
        Document doc = Jsoup.parse("<div class='active' id='d1'>One</div><div id='d2'>Two</div><div class='active' id='d3'>Three</div>");
        Elements allDivs = doc.select("div");

        Elements activeDivs = allDivs.select(".active");
        assertEquals(2, activeDivs.size());

        Elements inactiveDivs = allDivs.not(".active");
        assertEquals(1, inactiveDivs.size());
        assertEquals("d2", inactiveDivs.get(0).id());

        assertTrue(allDivs.is(".active"));
        assertFalse(allDivs.is(".non-existing-class"));

        Elements eq0 = allDivs.eq(0);
        assertEquals(1, eq0.size());
        assertEquals("d1", eq0.get(0).id());

        Elements eqOutOfBounds = allDivs.eq(10);
        assertNotNull(eqOutOfBounds);
        assertEquals(0, eqOutOfBounds.size());
    }

    @Test(timeout = 4000)
    public void testParents() {
        Document doc = Jsoup.parse("<html><body><div id='grandparent'><div id='parent'><span id='child1'></span><span id='child2'></span></div></div></body></html>");
        Elements spans = doc.select("span");
        Elements parents = spans.parents();

        assertTrue(parents.size() >= 3);
        List<String> ids = new ArrayList<String>();
        for (Element parent : parents) {
            if (parent.hasAttr("id")) {
                ids.add(parent.id());
            }
        }
        assertTrue(ids.contains("parent"));
        assertTrue(ids.contains("grandparent"));
    }

    @Test(timeout = 4000)
    public void testFirstAndLast() {
        Element e1 = new Element("span").text("first");
        Element e2 = new Element("span").text("second");
        Elements els = new Elements(e1, e2);

        assertSame(e1, els.first());
        assertSame(e2, els.last());
    }

    @Test(timeout = 4000)
    public void testNodeTraversorIntegration() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Elements divs = doc.select("div");
        final List<String> visitedNodes = new ArrayList<String>();

        divs.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visitedNodes.add("H:" + node.nodeName());
            }

            public void tail(Node node, int depth) {
                visitedNodes.add("T:" + node.nodeName());
            }
        });

        assertTrue(visitedNodes.contains("H:div"));
        assertTrue(visitedNodes.contains("H:p"));
        assertTrue(visitedNodes.contains("H:span"));
        assertTrue(visitedNodes.contains("T:div"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyElementsBehavior() {
        Elements empty = new Elements();

        assertEquals("", empty.attr("key"));
        assertFalse(empty.hasAttr("key"));
        assertFalse(empty.hasClass("cls"));
        assertEquals("", empty.val());
        assertEquals("", empty.text());
        assertFalse(empty.hasText());
        assertEquals("", empty.html());
        assertEquals("", empty.outerHtml());
        assertEquals("", empty.toString());
        assertNull(empty.first());
        assertNull(empty.last());
        assertFalse(empty.is("div"));
        assertEquals(0, empty.parents().size());
        assertEquals(0, empty.eq(0).size());
        assertEquals(0, empty.not("div").size());
        assertEquals(0, empty.select("div").size());

        assertSame(empty, empty.attr("key", "val"));
        assertSame(empty, empty.removeAttr("key"));
        assertSame(empty, empty.addClass("cls"));
        assertSame(empty, empty.removeClass("cls"));
        assertSame(empty, empty.toggleClass("cls"));
        assertSame(empty, empty.val("test"));
        assertSame(empty, empty.tagName("div"));
        assertSame(empty, empty.html("<span></span>"));
        assertSame(empty, empty.prepend("<span></span>"));
        assertSame(empty, empty.append("<span></span>"));
        assertSame(empty, empty.before("<span></span>"));
        assertSame(empty, empty.after("<span></span>"));
        assertSame(empty, empty.unwrap());
        assertSame(empty, empty.empty());
        assertSame(empty, empty.remove());
    }

    @Test(timeout = 4000)
    public void testAttrShortCircuitAndFallthrough() {
        Element e1 = new Element("div");
        Element e2 = new Element("div");
        e2.attr("testAttr", "foundValue");
        Element e3 = new Element("div");
        e3.attr("testAttr", "thirdValue");

        Elements list = new Elements(e1, e2, e3);
        assertTrue(list.hasAttr("testAttr"));
        assertEquals("foundValue", list.attr("testAttr"));

        assertFalse(list.hasAttr("missingAttr"));
        assertEquals("", list.attr("missingAttr"));
    }

    @Test(timeout = 4000)
    public void testEqBoundaryConditions() {
        Element e1 = new Element("div");
        Elements list = new Elements(e1);

        assertEquals(1, list.eq(0).size());
        assertSame(e1, list.eq(0).get(0));

        assertEquals(0, list.eq(1).size());
        assertEquals(0, list.eq(-1).size());
        assertEquals(0, list.eq(Integer.MAX_VALUE).size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targeting Ground Truth Defect:
     * - org.jsoup.nodes.ElementTest::elementIsNotASiblingOfItself
     *   --> junit.framework.AssertionFailedError: expected:<2> but was:<3>
     * - org.jsoup.nodes.NodeTest::orphanNodeReturnsNullForSiblingElements / NPE
     *
     * Validates that sibling operations on DOM elements wrapped inside Elements
     * correctly treat the element as NOT being a sibling of itself, and orphan
     * elements do not throw NPE or mistakenly calculate siblings.
     */
    @Test(timeout = 4000)
    public void testDefectElementIsNotASiblingOfItself() {
        Document doc = Jsoup.parse("<div><p id='1'>One</p><p id='2'>Two</p><p id='3'>Three</p></div>");
        Elements ps = doc.select("p");
        assertEquals(3, ps.size());

        Element firstP = ps.first();
        assertNotNull(firstP);
        Elements siblings = firstP.siblingElements();

        // If the defect is present, siblings count will be 3 (itself included) rather than 2
        assertEquals(2, siblings.size());
        for (Element sibling : siblings) {
            assertNotEquals("1", sibling.id());
        }

        // Test orphan element: should return empty Elements list, NOT throw NPE
        Element orphan = new Element("orphan");
        Elements orphanSiblings = orphan.siblingElements();
        assertNotNull(orphanSiblings);
        assertEquals(0, orphanSiblings.size());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWrapEmptyHtmlThrowsException() {
        Elements els = new Elements(new Element("p"));
        els.wrap("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWrapNullHtmlThrowsException() {
        Elements els = new Elements(new Element("p"));
        els.wrap(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTraverseNullVisitorThrowsException() {
        Elements els = new Elements(new Element("p"));
        els.traverse(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Clone & List Delegation Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneDeepCopyIntegrity() {
        Document doc = Jsoup.parse("<div id='orig'>Text</div>");
        Elements original = doc.select("div");
        Elements cloned = original.clone();

        assertEquals(original.size(), cloned.size());
        assertEquals(original.outerHtml(), cloned.outerHtml());

        // Must be distinct element instances
        assertNotSame(original.get(0), cloned.get(0));

        // Mutating cloned element must not affect original
        cloned.attr("id", "modified");
        assertEquals("orig", original.first().id());
        assertEquals("modified", cloned.first().id());
    }

    @Test(timeout = 4000)
    public void testListDelegatesContract() {
        Element e1 = new Element("p").text("1");
        Element e2 = new Element("p").text("2");
        Element e3 = new Element("p").text("3");

        Elements list = new Elements();
        list.add(e1);
        list.add(0, e2); // [e2, e1]
        assertEquals(2, list.size());
        assertSame(e2, list.get(0));
        assertSame(e1, list.get(1));

        assertTrue(list.contains(e1));
        assertEquals(1, list.indexOf(e1));
        assertEquals(0, list.indexOf(e2));
        assertEquals(-1, list.indexOf(e3));

        list.add(e1); // [e2, e1, e1]
        assertEquals(2, list.lastIndexOf(e1));

        Element removed = list.remove(0);
        assertSame(e2, removed);
        assertEquals(2, list.size());

        boolean removedObj = list.remove(e1);
        assertTrue(removedObj);
        assertEquals(1, list.size());

        Element setOld = list.set(0, e3);
        assertSame(e1, setOld);
        assertSame(e3, list.get(0));

        List<Element> additions = Arrays.asList(e1, e2);
        list.addAll(additions);
        assertEquals(3, list.size());
        assertTrue(list.containsAll(additions));

        list.addAll(1, Collections.singletonList(new Element("div")));
        assertEquals(4, list.size());

        Object[] arr = list.toArray();
        assertEquals(4, arr.length);
        Element[] typedArr = list.toArray(new Element[0]);
        assertEquals(4, typedArr.length);

        Iterator<Element> it = list.iterator();
        assertTrue(it.hasNext());
        assertNotNull(it.next());

        ListIterator<Element> lit = list.listIterator();
        assertTrue(lit.hasNext());
        lit.next();
        ListIterator<Element> litIdx = list.listIterator(1);
        assertTrue(litIdx.hasNext());

        List<Element> sub = list.subList(0, 2);
        assertEquals(2, sub.size());

        list.retainAll(Collections.singletonList(e3));
        assertEquals(1, list.size());
        assertSame(e3, list.get(0));

        list.removeAll(Collections.singletonList(e3));
        assertTrue(list.isEmpty());

        list.add(e1);
        list.clear();
        assertEquals(0, list.size());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Element e1 = new Element("span").attr("class", "test");
        Element e2 = new Element("span").attr("class", "test");

        Elements list1 = new Elements(e1);
        Elements list2 = new Elements(e1);
        Elements list3 = new Elements(e2);

        assertEquals(list1, list2);
        assertEquals(list1.hashCode(), list2.hashCode());

        // Equals contract
        assertTrue(list1.equals(list1));
        assertFalse(list1.equals(null));
        assertFalse(list1.equals("StringObject"));

        list2.add(new Element("p"));
        assertNotEquals(list1, list2);
    }
}