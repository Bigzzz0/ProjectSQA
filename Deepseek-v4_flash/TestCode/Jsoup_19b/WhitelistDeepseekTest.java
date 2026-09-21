package org.jsoup.safety;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Covered branches and conditions in Whitelist:
 * - addTags: validation (null/empty) and insertion into tagNames set
 * - addAttributes: validation, handling existing vs new tag entry, :all pseudo-tag
 * - addEnforcedAttribute: validation, handling existing vs new tag entry
 * - addProtocols: validation, nested map creation and population
 * - preserveRelativeLinks: setter and effect on testValidProtocol
 * - isSafeTag: membership check
 * - isSafeAttribute: 
 *     • tag present in attributes map → key present → protocol check or no protocol defined
 *     • tag not present → fallback to :all tag (recursive call)
 *     • final return false
 * - testValidProtocol:
 *     • compute absUrl (with/without base URI)
 *     • preserveRelativeLinks flag modifies attribute value
 *     • loop over protocols, case-insensitive prefix check
 * - getEnforcedAttributes: return Attributes based on enforcedAttributes map
 *
 * Boundary conditions:
 * - Empty/null tag/key/value arguments → IllegalArgumentException
 * - Duplicate additions (sets prevent duplicates)
 * - :all pseudo-tag for attributes and protocols
 * - Relative vs absolute URLs, with and without base URI
 * - Custom protocols (e.g., "cid") that are not standard web protocols
 *
 * Targeted Defect (Defects4J org.jsoup.safety.CleanerTest#handlesCustomProtocols):
 * - Bug: custom protocol "cid" added via addProtocols is not correctly recognized,
 *   causing isSafeAttribute to return false for attribute src="cid:12345".
 * - Test: CustomWhitelist with img tag, src attribute, and cid protocol must report attribute as safe.
 */

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WhitelistDeepseekTest {

    // =========================== Partition A: Core Functional Logic ===========================

    @Test(timeout = 4000)
    public void testNoneWhitelist() {
        Whitelist w = Whitelist.none();
        assertFalse("none whitelist should have no tags", w.isSafeTag("a"));
        assertFalse("none whitelist should reject attribute", 
                w.isSafeAttribute("a", createSimpleElement("a", "href", "http://example.com"), 
                    createAttribute("href", "http://example.com")));
    }

    @Test(timeout = 4000)
    public void testSimpleTextWhitelist() {
        Whitelist w = Whitelist.simpleText();
        assertTrue("b should be allowed", w.isSafeTag("b"));
        assertTrue("em should be allowed", w.isSafeTag("em"));
        assertFalse("a should not be allowed", w.isSafeTag("a"));
    }

    @Test(timeout = 4000)
    public void testBasicWhitelist() {
        Whitelist w = Whitelist.basic();
        assertTrue("a tag should be safe", w.isSafeTag("a"));
        assertTrue("href attribute on a should be safe", 
                w.isSafeAttribute("a", createSimpleElement("a", "href", "http://jsoup.org"),
                    createAttribute("href", "http://jsoup.org")));
        // enforced attribute rel=nofollow
        Attributes enforced = w.getEnforcedAttributes("a");
        assertEquals("enforced rel should exist", "nofollow", enforced.get("rel"));
        // protocol ftp not in basic? basic includes ftp
        assertTrue("ftp protocol should be allowed", 
                w.isSafeAttribute("a", createSimpleElement("a", "href", "ftp://files.com"),
                    createAttribute("href", "ftp://files.com")));
    }

    @Test(timeout = 4000)
    public void testAddTags() {
        Whitelist w = new Whitelist().addTags("div", "span");
        assertTrue(w.isSafeTag("div"));
        assertTrue(w.isSafeTag("span"));
        assertFalse(w.isSafeTag("p"));
    }

    @Test(timeout = 4000)
    public void testAddAttributes() {
        Whitelist w = new Whitelist().addTags("div").addAttributes("div", "class", "id");
        Element div = createSimpleElement("div", "class", "myclass");
        assertTrue(w.isSafeAttribute("div", div, createAttribute("class", "myclass")));
        assertTrue(w.isSafeAttribute("div", div, createAttribute("id", "myid")));
        // :all pseudo-tag
        Whitelist w2 = new Whitelist().addTags("span").addAttributes(":all", "style");
        Element span = createSimpleElement("span", "style", "color:red");
        assertTrue(w2.isSafeAttribute("span", span, createAttribute("style", "color:red")));
        // attribute not allowed
        assertFalse(w2.isSafeAttribute("span", span, createAttribute("onclick", "alert(1)")));
    }

    @Test(timeout = 4000)
    public void testAddEnforcedAttribute() {
        Whitelist w = new Whitelist().addTags("a").addEnforcedAttribute("a", "rel", "nofollow");
        Attributes enforced = w.getEnforcedAttributes("a");
        assertEquals("nofollow", enforced.get("rel"));
        // override
        w.addEnforcedAttribute("a", "rel", "noopener");
        enforced = w.getEnforcedAttributes("a");
        assertEquals("value should be updated", "noopener", enforced.get("rel"));
    }

    @Test(timeout = 4000)
    public void testAddProtocols() {
        Whitelist w = new Whitelist().addTags("img").addAttributes("img", "src")
                .addProtocols("img", "src", "http", "https");
        Element imgHttp = createSimpleElement("img", "src", "http://example.com/pic.png");
        assertTrue("http protocol should be allowed", 
                w.isSafeAttribute("img", imgHttp, createAttribute("src", "http://example.com/pic.png")));
        Element imgFtp = createSimpleElement("img", "src", "ftp://files.com");
        assertFalse("ftp protocol should be rejected", 
                w.isSafeAttribute("img", imgFtp, createAttribute("src", "ftp://files.com")));
    }

    @Test(timeout = 4000)
    public void testPreserveRelativeLinks() {
        Whitelist w = new Whitelist().addTags("a").addAttributes("a", "href")
                .addProtocols("a","href","http")
                .preserveRelativeLinks(true);
        // Without base URI, relative URL should be removed because cannot resolve
        Element a = createSimpleElement("a","href","/relative");
        assertFalse("relative without base should be unsafe",
                w.isSafeAttribute("a", a, createAttribute("href","/relative")));
        
        // With base URI, relative becomes absolute
        // Creating element with base URI via parse
        String html = "<a href='/relative'>link</a>";
        Document doc = Jsoup.parseBodyFragment(html, "http://example.com");
        Element a2 = doc.body().child(0);
        Attribute hrefAttr = null;
        for (Attribute attr : a2.attributes()) {
            if (attr.getKey().equals("href")) {
                hrefAttr = attr;
                break;
            }
        }
        assertNotNull(hrefAttr);
        assertTrue("relative link with base should be allowed when preserveRelativeLinks=true",
                w.isSafeAttribute("a", a2, hrefAttr));
    }

    // =========================== Partition B: Boundary & Extremes ============================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddTagsNull() {
        new Whitelist().addTags((String) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddTagsEmpty() {
        new Whitelist().addTags("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAttributesNullTag() {
        new Whitelist().addAttributes(null, "class");
    }

   @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyTag() {
        new Whitelist().addAttributes("", "class");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAttributesNullKeys() {
        new Whitelist().addAttributes("a", (String[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyKey() {
        new Whitelist().addAttributes("a", "");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullTag() {
        new Whitelist().addEnforcedAttribute(null, "key", "val");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullKey() {
        new Whitelist().addEnforcedAttribute("a", null, "val");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullValue() {
        new Whitelist().addEnforcedAttribute("a", "key", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddProtocolsNullTag() {
        new Whitelist().addProtocols(null, "src", "http");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddProtocolsNullKey() {
        new Whitelist().addProtocols("img", null, "http");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddProtocolsNullProtocols() {
        new Whitelist().addProtocols("img", "src", (String[]) null);
    }

    // =========================== Partition C: Defect-Targeted Branch Zone ======================

    @Test(timeout = 4000)
    public void testCustomProtocolCid() {
        // This test directly targets the Defects4J defect: handlesCustomProtocols
        // Bug: custom protocol "cid" added via addProtocols is not properly recognized,
        // causing isSafeAttribute to return false.
        Whitelist w = new Whitelist()
            .addTags("img")
            .addAttributes("img", "src")
            .addProtocols("img", "src", "cid");
        
        // Create an img element with src="cid:12345"
        String html = "<img src='cid:12345' />";
        Document doc = Jsoup.parseBodyFragment(html);
        Element img = doc.body().child(0);
        Attribute srcAttr = null;
        for (Attribute attr : img.attributes()) {
            if (attr.getKey().equals("src")) {
                srcAttr = attr;
                break;
            }
        }
        assertNotNull("src attribute must be present", srcAttr);
        assertTrue("Custom protocol 'cid' should be safe on img[src]",
                w.isSafeAttribute("img", img, srcAttr));
    }

    @Test(timeout = 4000)
    public void testCustomProtocolCidNotAllowed() {
        // Without adding the protocol, cid: should be unsafe
        Whitelist w = new Whitelist()
            .addTags("img")
            .addAttributes("img", "src")
            .addProtocols("img", "src", "http"); // only http, no cid
        
        String html = "<img src='cid:12345' />";
        Document doc = Jsoup.parseBodyFragment(html);
        Element img = doc.body().child(0);
        Attribute srcAttr = null;
        for (Attribute attr : img.attributes()) {
            if (attr.getKey().equals("src")) {
                srcAttr = attr;
                break;
            }
        }
        assertNotNull(srcAttr);
        assertFalse("Custom protocol 'cid' should be unsafe when not added",
                w.isSafeAttribute("img", img, srcAttr));
    }

    @Test(timeout = 4000)
    public void testRelativeLinkResolvedWithBase() {
        // Ensure that when preserveRelativeLinks is false (default), relative links are converted to absolute
        Whitelist w = new Whitelist().addTags("a").addAttributes("a","href")
                .addProtocols("a","href","http")
                .preserveRelativeLinks(false);
        String html = "<a href='/page'>link</a>";
        Document doc = Jsoup.parseBodyFragment(html, "http://example.com");
        Element a = doc.body().child(0);
        Attribute hrefAttr = null;
        for (Attribute attr : a.attributes()) {
            if (attr.getKey().equals("href")) {
                hrefAttr = attr;
                break;
            }
        }
        // After testValidProtocol, the value should be absolute (http://example.com/page)
        assertTrue(w.isSafeAttribute("a", a, hrefAttr));
        assertEquals("Relative should be converted to absolute", "http://example.com/page", hrefAttr.getValue()));
    }

    // ============================= Partition D: Exception & Defensive Guard Paths ======================

    @Test(timeout=4000, expected = IllegalArgumentException.class)
    public void testIsSafeTagNull() {
        new Whitelist().isSafeTag(null);
    }

    // isSafeAttribute and testValidProtocol are tested via other methods; no public direct calls needed.

    // =========================== Partition E: Object Lifecycle & Contract =====================

    @Test(timeout = 4000)
    public void testGetEnforcedAttributesEmpty() {
        Whitelist w = new Whitelist();
        Attributes attrs = w.getEnforcedAttributes("a");
        assertTrue("enforced attributes should be empty", attrs.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMultipleAdditionsReturnThis() {
        Whitelist w = new Whitelist();
        assertSame(w, w.addTags("p"));
        assertSame(w, w.addAttributes("p", "class"));
        assertSame(w, w.addEnforcedAttribute("p", "style", "color:red"));
        assertSame(w, w.addProtocols("p", "dir", "http"));
        assertSame(w, w.preserveRelativeLinks(true));
    }

    // =========================== Helper Methods ===============================================

    private Element createSimpleElement(String tag, String attrKey, String attrValue) {
        Element el = new Element(tag);
        el.attr(attrKey, attrValue);
        return el;
    }

    private Attribute createAttribute(String key, String value) {
        return new Attribute(key, value);
    }
}