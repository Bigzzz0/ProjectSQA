package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import java.util.List;

/**
 * Deep structural and defect-targeted test suite for {@link Node}.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Core branches in addChildren(int, Node...): reparentChild loop, index calculation, reindexChildren.
 * - after(Node)/before(Node): use addChildren(int, Node...), triggers move-within-same-parent bug.
 * - Defect: when moving a child inside the same parent, removal shifts indices, insertion uses original index -> wrong order.
 * - Boundary conditions: EMPTY_NODES -> ArrayList init, null parent, single child, siblings extremes.
 * - Equals/hashCode: deep content comparison correctly ignores tree position.
 */
public class NodeDeepseekTest {

    // ---------------------------------------------------------------
    //  Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAttrBasic() {
        Document doc = Jsoup.parse("<div id='test' class='foo'>Hello</div>");
        Element div = doc.select("div").first();
        assertEquals("foo", div.attr("class"));
        assertEquals("test", div.attr("id"));
        assertEquals("", div.attr("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasAttr() {
        Document doc = Jsoup.parse("<a href='http://example.com' rel='nofollow'>link</a>");
        Element a = doc.select("a").first();
        assertTrue(a.hasAttr("href"));
        assertTrue(a.hasAttr("rel"));
        assertFalse(a.hasAttr("target"));
        
        // abs: prefix
        a.attr("href", "/relative");
        doc.setBaseUri("http://base.com");
        assertTrue(a.hasAttr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrl() {
        Document doc = Jsoup.parse("<a href='/path'>link</a>");
        doc.setBaseUri("http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/path", a.absUrl("href"));
        
        // missing attribute
        assertEquals("", a.absUrl("nonexistent"));
        
        // already absolute
        a.attr("href", "http://other.com/page");
        assertEquals("http://other.com/page", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildAccess() {
        Document doc = Jsoup.parse("<ul><li>A</li><li>B</li></ul>");
        Element ul = doc.select("ul").first();
        assertEquals(2, ul.childNodeSize());
        
        Node first = ul.childNode(0);
        assertTrue(first instanceof Element);
        assertEquals("li", ((Element) first).tagName());
        
        List<Node> children = ul.childNodes();
        assertEquals(2, children.size());
        assertTrue(children.get(0) == first);
        
        List<Node> copy = ul.childNodesCopy();
        assertEquals(2, copy.size());
        assertNotSame(children.get(0), copy.get(0)); // deep copy
    }

    @Test(timeout = 4000)
    public void testSiblings() {
        Document doc = Jsoup.parse("<div><span>1</span><span>2</span><span>3</span></div>");
        Element parent = doc.select("div").first();
        List<Element> spans = parent.children();
        Element mid = spans.get(1);
        Element first = spans.get(0);
        Element last = spans.get(2);
        
        assertEquals(first, mid.previousSibling());
        assertEquals(last, mid.nextSibling());
        assertNull(first.previousSibling());
        assertNull(last.nextSibling());
        
        List<Node> siblings = mid.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(first));
        assertTrue(siblings.contains(last));
        assertFalse(siblings.contains(mid));
        
        assertEquals(1, mid.siblingIndex());
    }

    @Test(timeout = 4000)
    public void testOwnerDocument() {
        Document doc = Jsoup.parse("<p>Text</p>");
        Element p = doc.select("p").first();
        assertSame(doc, p.ownerDocument());
        
        Node orphan = new TextNode("orphan", "");
        assertNull(orphan.ownerDocument());
        
        // Document itself
        assertSame(doc, doc.ownerDocument());
    }

    // ---------------------------------------------------------------
    //  Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyChildNodes() {
        Document doc = Jsoup.parse("<div></div>");
        Element div = doc.select("div").first();
        assertTrue(div.childNodes().isEmpty());
        assertEquals(0, div.childNodeSize());
    }

    @Test(timeout = 4000)
    public void testSingleChild() {
        Document doc = Jsoup.parse("<ul><li>Only</li></ul>");
        Element ul = doc.select("ul").first();
        assertEquals(1, ul.childNodeSize());
        Node child = ul.childNode(0);
        assertEquals("Only", ((TextNode) child.childNode(0)).getWholeText());
    }

    @Test(timeout = 4000)
    public void testNullAndEmptyAttrKey() {
        Document doc = Jsoup.parse("<div>test</div>");
        Element div = doc.select("div").first();
        try {
            div.attr((String) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            div.hasAttr(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        try {
            div.removeAttr(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAbsUrlWithEmptyBase() {
        Document doc = Jsoup.parse("<a href='page.html'>link</a>");
        Element a = doc.select("a").first();
        // baseUri is empty, resolve returns empty
        assertEquals("", a.absUrl("href"));
    }

    // ---------------------------------------------------------------
    //  Partition C: Defect-Targeted Branch Zone (move within same parent)
    // ---------------------------------------------------------------

    /**
     * Targets the known Defects4J defect where moving a child inside the same parent
     * using before/after gives incorrect ordering.
     * Initial order: A, B, C. Move C before A -> expected: C, A, B.
     * Bug: C stays at the end due to index miscalculation in addChildren(int, Node...).
     */
    @Test(timeout = 4000)
    public void testMoveChildBeforeSibling_SameParent() {
        String html = "<div id='parent'>" +
                      "<div id='a'>A</div>" +
                      "<div id='b'>B</div>" +
                      "<div id='c'>C</div>" +
                      "</div>";
        Document doc = Jsoup.parse(html);
        Element parent = doc.select("#parent").first();
        Element a = doc.select("#a").first();
        Element b = doc.select("#b").first();
        Element c = doc.select("#c").first();

        // Move C before A
        c.before(a);

        // Fetch fresh order
        List<Element> children = parent.children();
        assertEquals(3, children.size());
        // Now order should be: C, A, B
        assertEquals("c", children.get(0).id());
        assertEquals("a", children.get(1).id());
        assertEquals("b", children.get(2).id());
    }

    @Test(timeout = 4000)
    public void testMoveChildAfterSibling_SameParent() {
        String html = "<div id='parent'>" +
                      "<div id='a'>A</div>" +
                      "<div id='b'>B</div>" +
                      "<div id='c'>C</div>" +
                      "</div>";
        Document doc = Jsoup.parse(html);
        Element parent = doc.select("#parent").first();
        Element a = doc.select("#a").first();
        Element b = doc.select("#b").first();
        Element c = doc.select("#c").first();

        // Move A after C -> expected: B, C, A
        a.after(c);

        List<Element> children = parent.children();
        assertEquals(3, children.size());
        assertEquals("b", children.get(0).id());
        assertEquals("c", children.get(1).id());
        assertEquals("a", children.get(2).id());
    }

    @Test(timeout = 4000)
    public void testMoveChildToDifferentParent() {
        Document doc = Jsoup.parse("<div id='p1'><span id='child'>X</span></div><div id='p2'></div>");
        Element p1 = doc.select("#p1").first();
        Element p2 = doc.select("#p2").first();
        Element child = doc.select("#child").first();

        // Move child from p1 to p2
        p2.appendChild(child); // uses addChildren (non-indexed)

        assertEquals(0, p1.children().size());
        assertEquals(1, p2.children().size());
        assertSame(child, p2.child(0));
    }

    // ---------------------------------------------------------------
    //  Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWrapWithEmptyHtml() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        Element p = doc.select("p").first();
        p.wrap(""); // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnwrapWithNoParent() {
        Node orphan = new TextNode("orphan", "");
        orphan.unwrap(); // parent is null
    }

    @Test(timeout = 4000)
    public void testRemoveWithNullParent() {
        Node orphan = new TextNode("orphan", "");
        try {
            orphan.remove();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testBeforeWithNullNode() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.before((Node) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testReplaceWithNull() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.replaceWith(null);
    }

    // ---------------------------------------------------------------
    //  Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Document doc1 = Jsoup.parse("<p id='x'>Text</p>");
        Document doc2 = Jsoup.parse("<p id='x'>Text</p>");
        Element e1 = doc1.select("p").first();
        Element e2 = doc2.select("p").first();

        // same content -> equal
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertEquals(e1.hashCode(), e2.hashCode());

        // different attribute
        e2.attr("class", "foo");
        assertFalse(e1.equals(e2));
        assertNotEquals(e1.hashCode(), e2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneDeepStructure() {
        Document doc = Jsoup.parse("<ul><li>A</li><li>B</li></ul>");
        Element ul = doc.select("ul").first();
        Node clone = ul.clone();

        assertNotSame(ul, clone);
        assertNull(clone.parentNode()); // orphan
        assertEquals(ul.outerHtml(), clone.outerHtml()); // same content

        // modify original, ensure clone unaffected
        ul.childNode(0).remove();
        assertNotEquals(ul.outerHtml(), clone.outerHtml());
    }

    @Test(timeout = 4000)
    public void testSetBaseUriPropagates() {
        Document doc = Jsoup.parse("<div><span><a href='page.html'>link</a></span></div>");
        Element div = doc.select("div").first();
        div.setBaseUri("http://newbase.com");
        assertEquals("http://newbase.com", doc.baseUri()); // document base updated via traversal
        // check descendant
        Element a = doc.select("a").first();
        assertEquals("http://newbase.com", a.baseUri());
    }

    @Test(timeout = 4000)
    public void testTraverse() {
        Document doc = Jsoup.parse("<div><span>Hello</span><p>World</p></div>");
        final StringBuilder sb = new StringBuilder();
        NodeVisitor visitor = new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                sb.append(node.nodeName()).append(",");
            }
            @Override
            public void tail(Node node, int depth) {
                // ignored
            }
        };
        doc.traverse(visitor);
        // order: #root, html, head, body, div, span, #text, p, #text
        assertTrue(sb.toString().contains("span"));
        assertTrue(sb.toString().contains("p"));
    }

    @Test(timeout = 4000)
    public void testWrapAndUnwrap() {
        Document doc = Jsoup.parse("<span>Inner</span>");
        Element span = doc.select("span").first();
        span.wrap("<div id='wrapper'></div>");
        Element wrapper = doc.select("#wrapper").first();
        assertNotNull(wrapper);
        assertEquals("<div id=\"wrapper\"><span>Inner</span></div>", wrapper.outerHtml());

        // unwrap
        Node unwrapped = wrapper.unwrap();
        assertNotNull(unwrapped);
        assertEquals("<span>Inner</span>", doc.body().html());
    }
}