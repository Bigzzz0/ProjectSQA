package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.List;

public class NodeDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * Target: Node.java - key methods: attr, hasAttr, absUrl, parent, child, sibling, etc.
     * Known defect: hasAttr and attr for "abs:" prefix not correctly handled.
     * Branches:
     * - attr(String): if attributes.hasKey -> return value; else if startsWith("abs:") -> absUrl; else ""
     * - hasAttr(String): only checks attributes.hasKey; does not handle "abs:" prefix -> defect.
     * - absUrl: branches for base URI malformed, relUrl startsWith "?", etc.
     * - childNode, childNodes, parent, ownerDocument, remove, addChildren, etc.
     * Coverage: all public methods, including boundary conditions (null, empty, index out of bounds).
     */

    // Partition A: Core Functional Logic & State Transitions
    @Test(timeout = 4000)
    public void testAttrNormal() {
        Document doc = Jsoup.parse("<div id='foo'>text</div>");
        Element div = doc.select("div").first();
        assertEquals("foo", div.attr("id"));
        assertEquals("", div.attr("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefix() {
        Document doc = Jsoup.parse("<a href='/path'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/path", a.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefixMissingAttr() {
        Document doc = Jsoup.parse("<a>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("", a.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefixWithBaseMalformed() {
        Document doc = Jsoup.parse("<a href='/path'>link</a>", "invalid://");
        Element a = doc.select("a").first();
        assertEquals("", a.attr("abs:href"));
    }

    @Test(timeout = 4000)
    public void testAttrAbsPrefixWithAbsUrlInAttr() {
        Document doc = Jsoup.parse("<a href='http://other.com/path'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://other.com/path", a.attr("abs:href"));
    }

    // Partition B: Boundary Values (null, empty)
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.attr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.hasAttr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        Document doc = Jsoup.parse("<a href='x'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        a.absUrl("");
    }

    // Partition C: Defect-Targeted Branch Zone (covers known abs: prefix bug)
    @Test(timeout = 4000)
    public void handlesAbsPrefixOnHasAttr() {
        Document doc = Jsoup.parse("<a href='/foo'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertTrue("hasAttr with abs: prefix should return true if attribute exists", a.hasAttr("abs:href"));
    }

    @Test(timeout = 4000)
    public void handlesAbsPrefixOnMissingAttrHasAttr() {
        Document doc = Jsoup.parse("<a>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertFalse(a.hasAttr("abs:href"));
    }

    @Test(timeout = 4000)
    public void handlesAbsPrefixAttr() {
        Document doc = Jsoup.parse("<a href='/foo'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/foo", a.attr("abs:href"));
    }

    // Partition D: Exception & Defensive Guard Paths
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.removeAttr(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.setBaseUri(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBeforeNullHtml() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Element p = doc.select("p").first();
        p.before((String)null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAfterNullNode() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Element p = doc.select("p").first();
        p.after((Node)null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWrapEmptyHtml() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        div.wrap("");
    }

    // Partition E: Object Lifecycle & Contract Integrity
    @Test(timeout = 4000)
    public void testClone() {
        Document doc = Jsoup.parse("<div id='x'><p>text</p></div>");
        Element div = doc.select("div").first();
        Node clone = div.clone();
        assertNotNull(clone);
        assertTrue(clone instanceof Element);
        Element cloneEl = (Element) clone;
        assertEquals("div", cloneEl.tagName());
        assertEquals("x", cloneEl.id());
        assertEquals(1, cloneEl.children().size());
        assertNull(cloneEl.parent());
        // Ensure modifications to clone do not affect original
        cloneEl.attr("id", "y");
        assertEquals("x", div.id());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Document doc = Jsoup.parse("<div id='x'></div><div id='x'></div>");
        List<Element> divs = doc.select("div");
        Element div1 = divs.get(0);
        Element div2 = divs.get(1);
        // They are different objects, equals returns false (Node.equals only returns true if same reference)
        assertFalse(div1.equals(div2));
        // Same reference
        assertTrue(div1.equals(div1));
        // hashCode consistency
        assertEquals(div1.hashCode(), div1.hashCode());
    }

    @Test(timeout = 4000)
    public void testOuterHtml() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        assertEquals("<div>\n <p>text</p>\n</div>", doc.select("div").first().outerHtml());
    }

    @Test(timeout = 4000)
    public void testParentChildSibling() {
        Document doc = Jsoup.parse("<ul><li id='a'>A</li><li id='b'>B</li></ul>");
        Element ul = doc.select("ul").first();
        Element liA = doc.select("#a").first();
        Element liB = doc.select("#b").first();
        assertSame(ul, liA.parent());
        assertSame(liA, ul.childNode(0));
        assertSame(liB, ul.childNode(1));
        assertEquals(0, liA.siblingIndex());
        assertEquals(1, liB.siblingIndex());
        assertSame(liB, liA.nextSibling());
        assertSame(liA, liB.previousSibling());
        assertNull(liA.previousSibling());
        assertNull(liB.nextSibling());
    }

    @Test(timeout = 4000)
    public void testOwnerDocument() {
        Document doc = Jsoup.parse("<div>text</div>");
        Element div = doc.select("div").first();
        assertSame(doc, div.ownerDocument());
        // Orphan node
        Element orphan = new Element("p");
        assertNull(orphan.ownerDocument());
    }

    @Test(timeout = 4000)
    public void testRemove() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Element p = doc.select("p").first();
        p.remove();
        assertEquals(0, doc.select("p").size());
    }

    @Test(timeout = 4000)
    public void testBeforeAfterHtml() {
        Document doc = Jsoup.parse("<p>text</p>");
        Element p = doc.select("p").first();
        p.before("<b>before</b>");
        p.after("<i>after</i>");
        assertEquals("<b>before</b><p>text</p><i>after</i>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testWrap() {
        Document doc = Jsoup.parse("<p>text</p>");
        Element p = doc.select("p").first();
        p.wrap("<div class='wrapper'></div>");
        assertEquals("<div class=\"wrapper\"><p>text</p></div>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testReplaceWith() {
        Document doc = Jsoup.parse("<p>text</p>");
        Element p = doc.select("p").first();
        Element newSpan = new Element("span").text("new");
        p.replaceWith(newSpan);
        assertEquals("<span>new</span>", doc.body().html().replaceAll("\\s+", ""));
    }

    @Test(timeout = 4000)
    public void testAbsUrlNormal() {
        Document doc = Jsoup.parse("<a href='/foo'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/foo", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlMissingAttr() {
        Document doc = Jsoup.parse("<a>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testAbsUrlWithQueryStartingWithQuestion() {
        Document doc = Jsoup.parse("<a href='?foo=bar'>link</a>", "http://example.com/path");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/path?foo=bar", a.absUrl("href"));
    }

    @Test(timeout = 4000)
    public void testChildNodeIndexOutOfBounds() {
        Document doc = Jsoup.parse("<div></div>");
        Element div = doc.select("div").first();
        try {
            div.childNode(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
}