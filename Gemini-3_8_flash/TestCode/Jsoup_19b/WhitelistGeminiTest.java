package org.jsoup.safety;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.safety.Whitelist
 *
 * 1. Defect-Targeted Zone:
 *    - Defects4J CleanerTest::handlesCustomProtocols
 *    - Custom/anchor protocols (e.g., "cid:12345", "data:...") where el.absUrl() returns "" due to
 *      unrecognized protocol scheme or empty baseUri, causing testValidProtocol to mistakenly
 *      blank out attribute values and fail validation.
 *
 * 2. Decision & Branch Coverage Zones:
 *    - isSafeTag(): present in tagNames set vs absent.
 *    - isSafeAttribute():
 *        * tag in attributes map:
 *            - key present in tag's allowed set:
 *                * tag in protocols map:
 *                    - key in protocols map -> testValidProtocol() matches / fails.
 *                    - key not in protocols map -> returns true.
 *                * tag not in protocols map -> returns true.
 *            - key not present in tag's allowed set -> returns false.
 *        * tag not in attributes map:
 *            - tag != ":all" -> delegates to isSafeAttribute(":all", ...)
 *            - tag == ":all" -> recursion guard, returns false.
 *    - testValidProtocol():
 *        * preserveRelativeLinks: true (attr value preserved) vs false (attr value updated to absUrl).
 *        * protocol match: starts with protocol + ":" (case-insensitive check).
 *        * protocol mismatch / no matching protocol in set.
 *    - addAttributes():
 *        * attributes map contains tag (merges into existing set).
 *        * attributes map does not contain tag (creates new set).
 *    - addEnforcedAttribute():
 *        * enforcedAttributes map contains tag (merges key/val).
 *        * enforcedAttributes map does not contain tag (creates new map).
 *    - addProtocols():
 *        * protocols map contains tag vs creates new tag map.
 *        * attrMap contains key vs creates new protocol set.
 *    - getEnforcedAttributes(): tag found vs tag not found (returns empty Attributes).
 *    - Built-in configurations: none(), simpleText(), basic(), basicWithImages(), relaxed().
 *    - TypedValue (TagName, AttributeKey, AttributeValue, Protocol):
 *        * equals: this == obj, null obj, diff class, diff value, same value.
 *        * hashCode: contract consistency.
 *        * toString: returns underlying string.
 *    - Defensive validation guard branches: null or empty tag, keys, protocols, values.
 */

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import static org.junit.Assert.*;

public class WhitelistGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J CleanerTest::handlesCustomProtocols.
     * When custom protocols like "cid:" or "data:" are allowed on an element attribute,
     * el.absUrl(...) may fail to resolve (returning "") if not recognized as standard hierarchical URI
     * or if base URI is not present. The whitelist must recognize the custom protocol and not strip it.
     */
    @Test(timeout = 4000)
    public void testHandlesCustomProtocolsCidAndData() {
        Whitelist wl = Whitelist.none()
                .addTags("img")
                .addAttributes("img", "src")
                .addProtocols("img", "src", "cid", "data");

        Element el = new Element(Tag.valueOf("img"), "");
        Attribute attrCid = new Attribute("src", "cid:12345");
        el.attributes().put(attrCid);

        boolean isSafeCid = wl.isSafeAttribute("img", el, attrCid);
        assertTrue("Custom protocol 'cid:' must be recognized as safe", isSafeCid);
        assertEquals("cid:12345", attrCid.getValue());

        Attribute attrData = new Attribute("src", "data:image/png;base64,iVBORw0KGgo=");
        el.attributes().put(attrData);

        boolean isSafeData = wl.isSafeAttribute("img", el, attrData);
        assertTrue("Custom protocol 'data:' must be recognized as safe", isSafeData);
        assertEquals("data:image/png;base64,iVBORw0KGgo=", attrData.getValue());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Built-in Whitelists
    // =========================================================================

    @Test(timeout = 4000)
    public void testNoneWhitelist() {
        Whitelist wl = Whitelist.none();
        assertFalse(wl.isSafeTag("p"));
        assertFalse(wl.isSafeTag("b"));
        Element el = new Element(Tag.valueOf("p"), "");
        Attribute attr = new Attribute("class", "test");
        assertFalse(wl.isSafeAttribute("p", el, attr));
        assertEquals(0, wl.getEnforcedAttributes("p").size());
    }

    @Test(timeout = 4000)
    public void testSimpleTextWhitelist() {
        Whitelist wl = Whitelist.simpleText();
        assertTrue(wl.isSafeTag("b"));
        assertTrue(wl.isSafeTag("em"));
        assertTrue(wl.isSafeTag("i"));
        assertTrue(wl.isSafeTag("strong"));
        assertTrue(wl.isSafeTag("u"));
        assertFalse(wl.isSafeTag("a"));
        assertFalse(wl.isSafeTag("p"));
    }

    @Test(timeout = 4000)
    public void testBasicWhitelistStructureAndProtocols() {
        Whitelist wl = Whitelist.basic();
        assertTrue(wl.isSafeTag("a"));
        assertTrue(wl.isSafeTag("blockquote"));
        assertFalse(wl.isSafeTag("img"));

        Attributes enforcedA = wl.getEnforcedAttributes("a");
        assertEquals("nofollow", enforcedA.get("rel"));

        Element elA = new Element(Tag.valueOf("a"), "http://example.com/");
        Attribute safeHref = new Attribute("href", "http://example.com/test");
        elA.attributes().put(safeHref);
        assertTrue(wl.isSafeAttribute("a", elA, safeHref));

        Attribute unsafeHref = new Attribute("href", "javascript:alert(1)");
        elA.attributes().put(unsafeHref);
        assertFalse(wl.isSafeAttribute("a", elA, unsafeHref));

        Attribute mailtoHref = new Attribute("href", "mailto:user@example.com");
        elA.attributes().put(mailtoHref);
        assertTrue(wl.isSafeAttribute("a", elA, mailtoHref));
    }

    @Test(timeout = 4000)
    public void testBasicWithImagesWhitelist() {
        Whitelist wl = Whitelist.basicWithImages();
        assertTrue(wl.isSafeTag("img"));
        assertTrue(wl.isSafeTag("a"));

        Element elImg = new Element(Tag.valueOf("img"), "http://example.com/");
        Attribute srcAttr = new Attribute("src", "https://example.com/pic.png");
        elImg.attributes().put(srcAttr);
        assertTrue(wl.isSafeAttribute("img", elImg, srcAttr));

        Attribute widthAttr = new Attribute("width", "100");
        elImg.attributes().put(widthAttr);
        assertTrue(wl.isSafeAttribute("img", elImg, widthAttr));

        Attribute onerrorAttr = new Attribute("onerror", "alert(1)");
        elImg.attributes().put(onerrorAttr);
        assertFalse(wl.isSafeAttribute("img", elImg, onerrorAttr));
    }

    @Test(timeout = 4000)
    public void testRelaxedWhitelist() {
        Whitelist wl = Whitelist.relaxed();
        assertTrue(wl.isSafeTag("table"));
        assertTrue(wl.isSafeTag("thead"));
        assertTrue(wl.isSafeTag("tbody"));
        assertTrue(wl.isSafeTag("th"));
        assertTrue(wl.isSafeTag("td"));
        assertTrue(wl.isSafeTag("h1"));
        assertTrue(wl.isSafeTag("div"));

        assertEquals(0, wl.getEnforcedAttributes("a").size());

        Element tableEl = new Element(Tag.valueOf("table"), "");
        Attribute summaryAttr = new Attribute("summary", "table summary");
        tableEl.attributes().put(summaryAttr);
        assertTrue(wl.isSafeAttribute("table", tableEl, summaryAttr));
    }

    // =========================================================================
    // Partition B: Branch & Decision Coverage: Tag, Attribute, Protocol, :all
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddTagsChainingAndDuplicates() {
        Whitelist wl = new Whitelist();
        assertFalse(wl.isSafeTag("div"));
        wl.addTags("div", "span");
        assertTrue(wl.isSafeTag("div"));
        assertTrue(wl.isSafeTag("span"));
        wl.addTags("div"); // duplicate addition
        assertTrue(wl.isSafeTag("div"));
    }

    @Test(timeout = 4000)
    public void testAddAttributesExistingAndNew() {
        Whitelist wl = new Whitelist();
        Element el = new Element(Tag.valueOf("p"), "");

        wl.addAttributes("p", "class");
        Attribute classAttr = new Attribute("class", "lead");
        el.attributes().put(classAttr);
        assertTrue(wl.isSafeAttribute("p", el, classAttr));

        // Add additional attribute to existing tag
        wl.addAttributes("p", "id", "title");
        Attribute idAttr = new Attribute("id", "main");
        el.attributes().put(idAttr);
        assertTrue(wl.isSafeAttribute("p", el, idAttr));

        Attribute titleAttr = new Attribute("title", "hover text");
        el.attributes().put(titleAttr);
        assertTrue(wl.isSafeAttribute("p", el, titleAttr));

        // Tag is present in attributes map, but attribute key is not allowed
        Attribute styleAttr = new Attribute("style", "color:red;");
        el.attributes().put(styleAttr);
        assertFalse(wl.isSafeAttribute("p", el, styleAttr));
    }

    @Test(timeout = 4000)
    public void testPseudoTagAllAttributes() {
        Whitelist wl = new Whitelist();
        wl.addAttributes(":all", "class", "dir");

        // Tag "span" is not in attributes map, should fallback to :all
        Element spanEl = new Element(Tag.valueOf("span"), "");
        Attribute classAttr = new Attribute("class", "highlight");
        spanEl.attributes().put(classAttr);
        assertTrue(wl.isSafeAttribute("span", spanEl, classAttr));

        Attribute unallowedAttr = new Attribute("style", "color:black");
        spanEl.attributes().put(unallowedAttr);
        assertFalse(wl.isSafeAttribute("span", spanEl, unallowedAttr));

        // Verify direct check on :all when :all has attributes
        Element allEl = new Element(Tag.valueOf(":all"), "");
        assertTrue(wl.isSafeAttribute(":all", allEl, classAttr));
    }

    @Test(timeout = 4000)
    public void testPseudoTagAllWhenNotConfigured() {
        Whitelist wl = new Whitelist();
        Element el = new Element(Tag.valueOf(":all"), "");
        Attribute attr = new Attribute("class", "any");
        // Tag is :all, but :all is not in attributes -> !tagName.equals(":all") is false -> returns false
        assertFalse(wl.isSafeAttribute(":all", el, attr));
    }

    @Test(timeout = 4000)
    public void testAddEnforcedAttributeNewAndOverwrite() {
        Whitelist wl = new Whitelist();
        assertEquals(0, wl.getEnforcedAttributes("a").size());

        wl.addEnforcedAttribute("a", "rel", "nofollow");
        Attributes attrs = wl.getEnforcedAttributes("a");
        assertEquals(1, attrs.size());
        assertEquals("nofollow", attrs.get("rel"));

        // Add another enforced attribute to the same tag
        wl.addEnforcedAttribute("a", "target", "_blank");
        attrs = wl.getEnforcedAttributes("a");
        assertEquals(2, attrs.size());
        assertEquals("nofollow", attrs.get("rel"));
        assertEquals("_blank", attrs.get("target"));

        // Overwrite existing enforced attribute
        wl.addEnforcedAttribute("a", "rel", "noopener");
        attrs = wl.getEnforcedAttributes("a");
        assertEquals("noopener", attrs.get("rel"));
    }

    @Test(timeout = 4000)
    public void testAddProtocolsNewAndExistingBranches() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("a", "href");

        // First call: creates tag map and key set
        wl.addProtocols("a", "href", "http", "https");

        // Second call on same tag and same key: appends to existing protSet
        wl.addProtocols("a", "href", "ftp");

        // Call on same tag but different key: creates new key set in existing tag map
        wl.addProtocols("a", "ping", "https");

        Element el = new Element(Tag.valueOf("a"), "");
        Attribute ftpAttr = new Attribute("href", "ftp://files.example.com");
        el.attributes().put(ftpAttr);
        assertTrue(wl.isSafeAttribute("a", el, ftpAttr));

        Attribute gopherAttr = new Attribute("href", "gopher://gopher.example.com");
        el.attributes().put(gopherAttr);
        assertFalse(wl.isSafeAttribute("a", el, gopherAttr));
    }

    @Test(timeout = 4000)
    public void testPreserveRelativeLinksTrue() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");
        wl.preserveRelativeLinks(true);

        Element el = new Element(Tag.valueOf("a"), "http://example.com/");
        Attribute relHref = new Attribute("href", "/relative/path");
        el.attributes().put(relHref);

        boolean safe = wl.isSafeAttribute("a", el, relHref);
        assertTrue(safe);
        // Attribute value must remain relative
        assertEquals("/relative/path", relHref.getValue());
    }

    @Test(timeout = 4000)
    public void testPreserveRelativeLinksFalseResolvesAbsolute() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");
        wl.preserveRelativeLinks(false); // default

        Element el = new Element(Tag.valueOf("a"), "http://example.com/base/");
        Attribute relHref = new Attribute("href", "sub/page.html");
        el.attributes().put(relHref);

        boolean safe = wl.isSafeAttribute("a", el, relHref);
        assertTrue(safe);
        // Attribute value must be resolved to absolute URI
        assertEquals("http://example.com/base/sub/page.html", relHref.getValue());
    }

    @Test(timeout = 4000)
    public void testRelativeLinkWithoutBaseUriFailsProtocolCheck() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http", "https");

        Element el = new Element(Tag.valueOf("a"), ""); // empty baseUri
        Attribute relHref = new Attribute("href", "relative/path");
        el.attributes().put(relHref);

        assertFalse(wl.isSafeAttribute("a", el, relHref));
    }

    @Test(timeout = 4000)
    public void testProtocolCaseInsensitivity() {
        Whitelist wl = new Whitelist();
        wl.addAttributes("a", "href");
        wl.addProtocols("a", "href", "http");

        Element el = new Element(Tag.valueOf("a"), "");
        Attribute upperHref = new Attribute("href", "HTTP://UPPERCASE.EXAMPLE.COM");
        el.attributes().put(upperHref);

        assertTrue(wl.isSafeAttribute("a", el, upperHref));
    }

    // =========================================================================
    // Partition D: Defensive Guard Paths & Exception Validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddTagsNullArrayThrows() {
        new Whitelist().addTags((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddTagsEmptyTagThrows() {
        new Whitelist().addTags("");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAttributesNullTagThrows() {
        new Whitelist().addAttributes(null, "href");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAttributesEmptyTagThrows() {
        new Whitelist().addAttributes("", "href");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAttributesNullKeysThrows() {
        new Whitelist().addAttributes("a", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAttributesEmptyKeyThrows() {
        new Whitelist().addAttributes("a", "");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddEnforcedAttributeNullTagThrows() {
        new Whitelist().addEnforcedAttribute(null, "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddEnforcedAttributeEmptyTagThrows() {
        new Whitelist().addEnforcedAttribute("", "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddEnforcedAttributeEmptyKeyThrows() {
        new Whitelist().addEnforcedAttribute("a", "", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddEnforcedAttributeEmptyValueThrows() {
        new Whitelist().addEnforcedAttribute("a", "rel", "");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddProtocolsEmptyTagThrows() {
        new Whitelist().addProtocols("", "href", "http");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddProtocolsEmptyKeyThrows() {
        new Whitelist().addProtocols("a", "", "http");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddProtocolsNullProtocolsThrows() {
        new Whitelist().addProtocols("a", "href", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddProtocolsEmptyProtocolThrows() {
        new Whitelist().addProtocols("a", "href", "");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & TypedValue Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypedValueEqualsAndHashCodeContracts() {
        Whitelist.TagName tag1 = Whitelist.TagName.valueOf("div");
        Whitelist.TagName tag2 = Whitelist.TagName.valueOf("div");
        Whitelist.TagName tag3 = Whitelist.TagName.valueOf("span");
        Whitelist.AttributeKey key = Whitelist.AttributeKey.valueOf("div");

        // Identity and null checks
        assertTrue(tag1.equals(tag1));
        assertFalse(tag1.equals(null));
        assertFalse(tag1.equals("div")); // different class
        assertFalse(tag1.equals(key));   // different TypedValue subclass

        // Equivalence
        assertTrue(tag1.equals(tag2));
        assertTrue(tag2.equals(tag1));
        assertEquals(tag1.hashCode(), tag2.hashCode());

        // Inequivalence
        assertFalse(tag1.equals(tag3));
        assertNotEquals(tag1.hashCode(), tag3.hashCode());

        // toString
        assertEquals("div", tag1.toString());
        assertEquals("div", key.toString());
    }

    @Test(timeout = 4000)
    public void testAllTypedValueTypesInstantiation() {
        Whitelist.TagName tag = Whitelist.TagName.valueOf("a");
        Whitelist.AttributeKey key = Whitelist.AttributeKey.valueOf("href");
        Whitelist.AttributeValue val = Whitelist.AttributeValue.valueOf("val");
        Whitelist.Protocol prot = Whitelist.Protocol.valueOf("http");

        assertEquals("a", tag.toString());
        assertEquals("href", key.toString());
        assertEquals("val", val.toString());
        assertEquals("http", prot.toString());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTypedValueNullThrows() {
        Whitelist.TagName.valueOf(null);
    }
}