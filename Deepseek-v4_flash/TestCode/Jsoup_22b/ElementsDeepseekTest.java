package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 =================================
 Target: org.jsoup.select.Elements (Defects4J)
 
 Known Defect: Sibling-related methods (siblingElements(), nextElementSibling(), previousElementSibling())
 incorrectly include the element itself, causing:
 - elementIsNotASiblingOfItself: expected 2 siblings, got 3
 - nodeIsNotASiblingOfItself: expected 2 siblings, got 3
 - orphanNodeReturnsNullForSiblingElements: NPE when no parent exists
 
 Branches to cover:
 1. attr(String) - first match with attr, no match, empty list
 2. hasAttr(String) - true/false paths
 3. attr(String,String) - set on all elements
 4. removeAttr(String) - remove from all
 5. addClass/removeClass/toggleClass/hasClass - true/false paths
 6. val() - empty vs non-empty list
 7. text() - single/multiple elements, empty list
 8. html()/outerHtml() - single/multiple elements
 9. eq(int) - valid index, out-of-bounds (negative, >= size)
 10. first()/last() - empty vs non-empty
 11. parents() - with/without parents
 12. List operations - add/remove/set/contains/indexOf
 13. clone() - deep copy verification
 14. equals/hashCode - contract
 15. subList - valid/invalid ranges
 16. toArray - both overloads
 17. traverse - null visitor (exception), normal traversal
 18. select/not/is - query filtering
 19. wrap/unwrap/empty/remove - DOM manipulation
 20. Sibling defect: verify siblingElements() excludes self, orphan returns null
 */
public class ElementsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorsAndBasicListOperations() {
        Element e1 = new Element("div");
        Element e2 = new Element("p");
        Element e3 = new Element("span");

        // Default constructor
        Elements empty = new Elements();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());

        // Collection constructor
        List<Element> list = Arrays.asList(e1, e2);
        Elements fromCollection = new Elements(list);
        assertEquals(2, fromCollection.size());
        assertTrue(fromCollection.contains(e1));
        assertTrue(fromCollection.contains(e2));

        // List constructor
        Elements fromList = new Elements(list);
        assertEquals(2, fromList.size());

        // Varargs constructor
        Elements fromVarargs = new Elements(e1, e2, e3);
        assertEquals(3, fromVarargs.size());

        // add/remove operations
        Elements elements = new Elements();
        assertTrue(elements.add(e1));
        assertTrue(elements.add(e2));
        assertEquals(2, elements.size());
        assertTrue(elements.remove(e1));
        assertEquals(1, elements.size());

        // add at index
        elements.add(0, e3);
        assertEquals(e3, elements.get(0));
        assertEquals(e2, elements.get(1));

        // set
        Element e4 = new Element("a");
        assertEquals(e2, elements.set(1, e4));
        assertEquals(e4, elements.get(1));

        // remove by index
        assertEquals(e3, elements.remove(0));
        assertEquals(1, elements.size());

        // containsAll
        Elements multi = new Elements(e1, e2, e3);
        assertTrue(multi.containsAll(Arrays.asList(e1, e2)));
        assertFalse(multi.containsAll(Arrays.asList(e1, new Element("x"))));

        // addAll
        Elements addAllTarget = new Elements();
        assertTrue(addAllTarget.addAll(Arrays.asList(e1, e2)));
        assertEquals(2, addAllTarget.size());

        // addAll at index
        Elements addAllIndex = new Elements(e3);
        assertTrue(addAllIndex.addAll(0, Arrays.asList(e1, e2)));
        assertEquals(3, addAllIndex.size());
        assertEquals(e1, addAllIndex.get(0));
        assertEquals(e2, addAllIndex.get(1));
        assertEquals(e3, addAllIndex.get(2));

        // removeAll
        Elements removeAllTarget = new Elements(e1, e2, e3);
        assertTrue(removeAllTarget.removeAll(Arrays.asList(e1, e2)));
        assertEquals(1, removeAllTarget.size());
        assertTrue(removeAllTarget.contains(e3));

        // retainAll
        Elements retainTarget = new Elements(e1, e2, e3);
        assertTrue(retainTarget.retainAll(Arrays.asList(e1, e3)));
        assertEquals(2, retainTarget.size());
        assertTrue(retainTarget.contains(e1));
        assertTrue(retainTarget.contains(e3));

        // clear
        retainTarget.clear();
        assertTrue(retainTarget.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAttributeOperations() {
        Document doc = Jsoup.parse("<div id='a' class='x y'>one</div><p id='b' class='z'>two</p>");
        Elements divs = doc.select("div");
        Elements ps = doc.select("p");

        // attr(String) - first match
        assertEquals("a", divs.attr("id"));
        assertEquals("b", ps.attr("id"));

        // attr(String) - no match
        assertEquals("", divs.attr("nonexistent"));

        // attr(String) - empty list
        Elements empty = new Elements();
        assertEquals("", empty.attr("id"));

        // hasAttr
        assertTrue(divs.hasAttr("id"));
        assertFalse(divs.hasAttr("nonexistent"));
        assertFalse(empty.hasAttr("id"));

        // attr(String, String) - set on all
        divs.attr("data-test", "value");
        assertEquals("value", divs.first().attr("data-test"));
        assertEquals("value", divs.get(0).attr("data-test"));

        // removeAttr
        divs.removeAttr("data-test");
        assertFalse(divs.hasAttr("data-test"));

        // Chaining
        Elements chained = divs.attr("data-a", "1").removeAttr("data-a").attr("data-b", "2");
        assertSame(divs, chained);
        assertTrue(divs.hasAttr("data-b"));
    }

    @Test(timeout = 4000)
    public void testClassOperations() {
        Document doc = Jsoup.parse("<div class='a b'>one</div><div class='c'>two</div><div>three</div>");
        Elements divs = doc.select("div");

        // hasClass - any match
        assertTrue(divs.hasClass("a"));
        assertTrue(divs.hasClass("c"));
        assertFalse(divs.hasClass("z"));

        // addClass to all
        divs.addClass("added");
        for (Element e : divs) {
            assertTrue(e.hasClass("added"));
        }

        // removeClass from all
        divs.removeClass("added");
        for (Element e : divs) {
            assertFalse(e.hasClass("added"));
        }

        // toggleClass - add then remove
        divs.toggleClass("toggled");
        for (Element e : divs) {
            assertTrue(e.hasClass("toggled"));
        }
        divs.toggleClass("toggled");
        for (Element e : divs) {
            assertFalse(e.hasClass("toggled"));
        }

        // Chaining
        Elements chained = divs.addClass("x").removeClass("x").toggleClass("y");
        assertSame(divs, chained);
        assertTrue(divs.hasClass("y"));
    }

    @Test(timeout = 4000)
    public void testValAndTextOperations() {
        Document doc = Jsoup.parse("<input value='hello'><textarea>world</textarea><p>text</p>");
        Elements inputs = doc.select("input");
        Elements textareas = doc.select("textarea");
        Elements ps = doc.select("p");

        // val() - first element
        assertEquals("hello", inputs.val());
        assertEquals("world", textareas.val());

        // val() - empty list
        Elements empty = new Elements();
        assertEquals("", empty.val());

        // val(String) - set on all
        inputs.val("newvalue");
        assertEquals("newvalue", inputs.first().val());

        // text() - single element
        assertEquals("text", ps.text());

        // text() - multiple elements
        Elements multi = new Elements(ps.first(), ps.first());
        assertEquals("text text", multi.text());

        // text() - empty list
        assertEquals("", empty.text());

        // hasText
        assertTrue(ps.hasText());
        assertFalse(empty.hasText());
    }

    @Test(timeout = 4000)
    public void testHtmlAndOuterHtml() {
        Document doc = Jsoup.parse("<div><span>one</span></div><p>two</p>");
        Elements divs = doc.select("div");
        Elements ps = doc.select("p");

        // html() - single element
        assertEquals("<span>one</span>", divs.html());

        // html() - multiple elements
        Elements multi = new Elements(divs.first(), ps.first());
        assertEquals("<span>one</span>\ntwo", multi.html());

        // outerHtml()
        assertEquals("<div>\n <span>one</span>\n</div>", divs.outerHtml());
        assertEquals("<p>two</p>", ps.outerHtml());

        // toString() alias
        assertEquals(ps.outerHtml(), ps.toString());

        // html(String) - set inner HTML
        divs.html("<b>new</b>");
        assertEquals("<b>new</b>", divs.html());

        // Chaining
        Elements chained = divs.html("<i>x</i>");
        assertSame(divs, chained);
    }

    @Test(timeout = 4000)
    public void testFirstLastAndEq() {
        Document doc = Jsoup.parse("<div>one</div><div>two</div><div>three</div>");
        Elements divs = doc.select("div");

        // first/last
        assertEquals("one", divs.first().text());
        assertEquals("three", divs.last().text());

        // first/last on empty
        Elements empty = new Elements();
        assertNull(empty.first());
        assertNull(empty.last());

        // eq - valid index
        Elements eq0 = divs.eq(0);
        assertEquals(1, eq0.size());
        assertEquals("one", eq0.first().text());

        Elements eq2 = divs.eq(2);
        assertEquals(1, eq2.size());
        assertEquals("three", eq2.first().text());

        // eq - out of bounds
        Elements eqNeg = divs.eq(-1);
        assertTrue(eqNeg.isEmpty());

        Elements eqHigh = divs.eq(3);
        assertTrue(eqHigh.isEmpty());

        // eq on empty
        Elements eqEmpty = empty.eq(0);
        assertTrue(eqEmpty.isEmpty());
    }

    @Test(timeout = 4000)
    public void testParentsAndTraverse() {
        Document doc = Jsoup.parse("<div><p><span>text</span></p></div>");
        Elements spans = doc.select("span");

        // parents()
        Elements parents = spans.parents();
        assertTrue(parents.size() >= 2); // p, div, body, html
        assertTrue(parents.contains(doc.select("p").first()));
        assertTrue(parents.contains(doc.select("div").first()));

        // traverse - null visitor
        try {
            spans.traverse(null);
            fail("Expected NullPointerException for null visitor");
        } catch (NullPointerException expected) {
            // expected
        }

        // traverse - normal
        final List<String> visited = new ArrayList<String>();
        spans.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.add(node.nodeName());
            }
            public void tail(Node node, int depth) {
                // no-op
            }
        });
        assertTrue(visited.contains("span"));
        assertTrue(visited.contains("#text"));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testBoundaryIndexOperations() {
        Document doc = Jsoup.parse("<div>one</div><div>two</div>");
        Elements divs = doc.select("div");

        // get(0) and get(size-1) are valid
        assertNotNull(divs.get(0));
        assertNotNull(divs.get(1));

        // get(-1) throws IndexOutOfBoundsException
        try {
            divs.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }

        // get(size) throws IndexOutOfBoundsException
        try {
            divs.get(2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }

        // subList boundaries
        List<Element> sub = divs.subList(0, 1);
        assertEquals(1, sub.size());

        try {
            divs.subList(1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            divs.subList(-1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }

        try {
            divs.subList(0, 3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullHandling() {
        Elements empty = new Elements();

        // Operations on empty list
        assertEquals("", empty.attr("id"));
        assertFalse(empty.hasAttr("id"));
        assertFalse(empty.hasClass("x"));
        assertEquals("", empty.val());
        assertEquals("", empty.text());
        assertFalse(empty.hasText());
        assertEquals("", empty.html());
        assertEquals("", empty.outerHtml());
        assertEquals("", empty.toString());
        assertNull(empty.first());
        assertNull(empty.last());

        // eq on empty
        assertTrue(empty.eq(0).isEmpty());

        // select on empty
        assertTrue(empty.select("div").isEmpty());

        // is on empty
        assertFalse(empty.is("div"));

        // parents on empty
        assertTrue(empty.parents().isEmpty());

        // List operations on empty
        assertEquals(0, empty.size());
        assertTrue(empty.isEmpty());
        assertFalse(empty.contains(new Element("div")));
        assertEquals(0, empty.toArray().length);
        assertEquals(0, empty.toArray(new Element[0]).length);
        assertFalse(empty.remove(new Element("div")));
        assertFalse(empty.containsAll(Arrays.asList(new Element("div"))));
        assertFalse(empty.removeAll(Arrays.asList(new Element("div"))));
        assertFalse(empty.retainAll(Arrays.asList(new Element("div"))));

        // indexOf/lastIndexOf on empty
        assertEquals(-1, empty.indexOf(new Element("div")));
        assertEquals(-1, empty.lastIndexOf(new Element("div")));

        // iterator on empty
        assertFalse(empty.iterator().hasNext());

        // listIterator on empty
        assertFalse(empty.listIterator().hasNext());
        assertFalse(empty.listIterator(0).hasNext());
    }

    @Test(timeout = 4000)
    public void testToArrayVariants() {
        Element e1 = new Element("div");
        Element e2 = new Element("p");
        Elements elements = new Elements(e1, e2);

        // toArray()
        Object[] array = elements.toArray();
        assertEquals(2, array.length);
        assertSame(e1, array[0]);
        assertSame(e2, array[1]);

        // toArray(T[]) - sufficient size
        Element[] typed = elements.toArray(new Element[0]);
        assertEquals(2, typed.length);
        assertSame(e1, typed[0]);
        assertSame(e2, typed[1]);

        // toArray(T[]) - larger array
        Element[] large = elements.toArray(new Element[5]);
        assertEquals(5, large.length);
        assertSame(e1, large[0]);
        assertSame(e2, large[1]);
        assertNull(large[2]); // null-terminated
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * CRITICAL DEFECT TEST: Sibling elements must NOT include the element itself.
     * 
     * The known defect causes siblingElements() to return 3 elements instead of 2
     * because it incorrectly includes the element itself. This test verifies that
     * siblingElements() correctly excludes the element itself.
     */
    @Test(timeout = 4000)
    public void testElementIsNotASiblingOfItself() {
        Document doc = Jsoup.parse("<div><p id='a'>one</p><p id='b'>two</p><p id='c'>three</p></div>");
        Element middle = doc.select("#b").first();
        
        // The middle element has exactly 2 siblings: #a and #c
        Elements siblings = middle.siblingElements();
        assertEquals("Sibling count should be 2, but was " + siblings.size(), 2, siblings.size());
        
        // Verify the siblings are correct
        assertTrue("Sibling #a should be present", siblings.select("#a").size() == 1);
        assertTrue("Sibling #c should be present", siblings.select("#c").size() == 1);
        assertFalse("Element itself should NOT be a sibling", siblings.select("#b").size() == 1);
    }

    /**
     * CRITICAL DEFECT TEST: Node sibling elements must not include the node itself.
     */
    @Test(timeout = 4000)
    public void testNodeIsNotASiblingOfItself() {
        Document doc = Jsoup.parse("<div><p>one</p><p>two</p><p>three</p></div>");
        Element middle = doc.select("p").get(1);
        
        // The middle node has exactly 2 sibling nodes
        List<Node> siblings = middle.siblingNodes();
        assertEquals("Sibling node count should be 2, but was " + siblings.size(), 2, siblings.size());
        
        // Verify none of the siblings is the element itself
        for (Node sibling : siblings) {
            assertNotSame("Element should not be its own sibling", middle, sibling);
        }
    }

    /**
     * CRITICAL DEFECT TEST: Orphan node (no parent) must return null for sibling elements.
     * The defect causes a NullPointerException when calling siblingElements() on an orphan.
     */
    @Test(timeout = 4000)
    public void testOrphanNodeReturnsNullForSiblingElements() {
        Element orphan = new Element("div");
        orphan.text("orphan");
        
        // Orphan has no parent, so siblingElements() should return null (or empty list)
        // The defect throws NullPointerException here
        Elements siblings = orphan.siblingElements();
        assertNotNull("Orphan siblingElements() should not throw NPE", siblings);
        assertTrue("Orphan should have no siblings", siblings.isEmpty());
    }

    /**
     * Additional defect-related test: nextElementSibling and previousElementSibling
     * should not return the element itself.
     */
    @Test(timeout = 4000)
    public void testNextPreviousSiblingExcludesSelf() {
        Document doc = Jsoup.parse("<div><p id='a'>one</p><p id='b'>two</p><p id='c'>three</p></div>");
        Element middle = doc.select("#b").first();
        
        // Next sibling should be #c, not #b
        Element next = middle.nextElementSibling();
        assertNotNull(next);
        assertEquals("c", next.id());
        assertNotSame("Next sibling should not be self", middle, next);
        
        // Previous sibling should be #a, not #b
        Element prev = middle.previousElementSibling();
        assertNotNull(prev);
        assertEquals("a", prev.id());
        assertNotSame("Previous sibling should not be self", middle, prev);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testTraverseWithNullVisitorThrowsNPE() {
        Document doc = Jsoup.parse("<div>text</div>");
        Elements divs = doc.select("div");
        divs.traverse(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWrapWithEmptyHtmlThrows() {
        Document doc = Jsoup.parse("<div>text</div>");
        Elements divs = doc.select("div");
        divs.wrap("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWrapWithNullHtmlThrows() {
        Document doc = Jsoup.parse("<div>text</div>");
        Elements divs = doc.select("div");
        divs.wrap(null);
    }

    @Test(timeout = 4000)
    public void testListIteratorBoundaries() {
        Element e1 = new Element("div");
        Element e2 = new Element("p");
        Elements elements = new Elements(e1, e2);

        // listIterator() - from start
        ListIterator<Element> it = elements.listIterator();
        assertTrue(it.hasNext());
        assertEquals(e1, it.next());
        assertTrue(it.hasNext());
        assertEquals(e2, it.next());
        assertFalse(it.hasNext());

        // listIterator(int) - from index 1
        ListIterator<Element> it1 = elements.listIterator(1);
        assertTrue(it1.hasPrevious());
        assertEquals(e1, it1.previous());
        assertFalse(it1.hasPrevious());

        // Invalid index
        try {
            elements.listIterator(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }

        try {
            elements.listIterator(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCloneDeepCopiesElements() {
        Document doc = Jsoup.parse("<div><p>one</p></div><div><p>two</p></div>");
        Elements original = doc.select("div");
        
        Elements cloned = original.clone();
        
        // Size should match
        assertEquals(original.size(), cloned.size());
        
        // Elements should be different instances but equal in structure
        for (int i = 0; i < original.size(); i++) {
            Element orig = original.get(i);
            Element clone = cloned.get(i);
            assertNotSame("Elements should be deep-copied", orig, clone);
            assertEquals("Tag names should match", orig.tagName(), clone.tagName());
            assertEquals("HTML should match", orig.html(), clone.html());
        }
        
        // Modifying clone should not affect original
        cloned.get(0).attr("data-cloned", "true");
        assertFalse("Original should not have cloned attribute", original.get(0).hasAttr("data-cloned"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Element e1 = new Element("div");
        Element e2 = new Element("p");
        Element e3 = new Element("span");

        Elements list1 = new Elements(e1, e2);
        Elements list2 = new Elements(e1, e2);
        Elements list3 = new Elements(e1, e3);
        Elements empty1 = new Elements();
        Elements empty2 = new Elements();

        // Reflexive
        assertEquals(list1, list1);

        // Symmetric
        assertEquals(list1, list2);
        assertEquals(list2, list1);

        // Not equal to different content
        assertNotEquals(list1, list3);
        assertNotEquals(list3, list1);

        // Empty lists
        assertEquals(empty1, empty2);
        assertNotEquals(empty1, list1);

        // Hash code consistency
        assertEquals(list1.hashCode(), list2.hashCode());
        assertEquals(empty1.hashCode(), empty2.hashCode());

        // Not equal to null or different type
        assertNotEquals(list1, null);
        assertNotEquals(list1, "not a list");
    }

    @Test(timeout = 4000)
    public void testSelectNotAndIs() {
        Document doc = Jsoup.parse("<div class='logo'>One</div><div>Two</div><p>Three</p>");
        Elements divs = doc.select("div");

        // select - find descendants
        Elements selected = divs.select("div");
        assertEquals(2, selected.size());

        // not - filter out
        Elements notLogo = divs.not(".logo");
        assertEquals(1, notLogo.size());
        assertEquals("Two", notLogo.first().text());

        // is - check if any match
        assertTrue(divs.is(".logo"));
        assertFalse(divs.is("p"));
        
        // is on empty
        Elements empty = new Elements();
        assertFalse(empty.is("div"));
    }

    @Test(timeout = 4000)
    public void testDomManipulationMethods() {
        Document doc = Jsoup.parse("<div><p>Hello <b>there</b></p> <p>now</p></div>");
        Elements ps = doc.select("p");

        // before/after
        ps.before("<i>before</i>");
        ps.after("<i>after</i>");
        assertEquals(4, doc.select("i").size());

        // wrap
        Elements bolds = doc.select("b");
        bolds.wrap("<i></i>");
        assertEquals(1, doc.select("i b").size());

        // unwrap
        Elements unwrapTarget = doc.select("b");
        unwrapTarget.unwrap();
        assertEquals(0, doc.select("b").size());

        // empty
        Elements emptyTarget = doc.select("p");
        emptyTarget.empty();
        for (Element p : emptyTarget) {
            assertEquals("", p.text());
        }

        // remove
        Elements removeTarget = doc.select("i");
        removeTarget.remove();
        assertEquals(0, doc.select("i").size());
    }

    @Test(timeout = 4000)
    public void testTagNameAndPrependAppend() {
        Document doc = Jsoup.parse("<div><p>one</p></div>");
        Elements ps = doc.select("p");

        // tagName
        ps.tagName("span");
        assertEquals("span", ps.first().tagName());

        // prepend
        ps.prepend("<b>prepended</b>");
        assertEquals("<b>prepended</b>one", ps.first().html());

        // append
        ps.append("<i>appended</i>");
        assertEquals("<b>prepended</b>one<i>appended</i>", ps.first().html());
    }

    @Test(timeout = 4000)
    public void testIndexOfAndLastIndexOf() {
        Element e1 = new Element("div");
        Element e2 = new Element("p");
        Element e3 = new Element("span");
        Elements elements = new Elements(e1, e2, e1, e3);

        // indexOf - first occurrence
        assertEquals(0, elements.indexOf(e1));
        assertEquals(1, elements.indexOf(e2));
        assertEquals(3, elements.indexOf(e3));
        assertEquals(-1, elements.indexOf(new Element("x")));

        // lastIndexOf - last occurrence
        assertEquals(2, elements.lastIndexOf(e1));
        assertEquals(1, elements.lastIndexOf(e2));
        assertEquals(3, elements.lastIndexOf(e3));
        assertEquals(-1, elements.lastIndexOf(new Element("x")));
    }
}