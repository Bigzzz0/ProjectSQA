package org.apache.commons.jxpath;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.Pointer;
import org.jdom.input.SAXBuilder;

/** Generated from approved defect-focused scenarios using native IPO. */
public class JxPath_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_001() throws Exception {
        // Native IPO combination: model=dom, xpath=root, lenient=strict
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
        String path = "/";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(false);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_002() throws Exception {
        // Native IPO combination: model=dom, xpath=element, lenient=lenient
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
        String path = "/vendor/location";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(true);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_003() throws Exception {
        // Native IPO combination: model=dom, xpath=attribute, lenient=strict
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
        String path = "/vendor/location/@name";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(false);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_004() throws Exception {
        // Native IPO combination: model=jdom, xpath=root, lenient=lenient
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = new SAXBuilder().build(new StringReader(xml));
        String path = "/";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(true);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_005() throws Exception {
        // Native IPO combination: model=jdom, xpath=element, lenient=strict
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = new SAXBuilder().build(new StringReader(xml));
        String path = "/vendor/location";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(false);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_006() throws Exception {
        // Native IPO combination: model=jdom, xpath=attribute, lenient=lenient
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = new SAXBuilder().build(new StringReader(xml));
        String path = "/vendor/location/@name";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(true);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

    @Test(timeout = 4000)
    public void test_dom_and_jdom_node_types_007() throws Exception {
        // Native IPO combination: model=jdom, xpath=root, lenient=strict
        String xml = "<vendor><location name='HQ'/></vendor>";
        Object root = new SAXBuilder().build(new StringReader(xml));
        String path = "/";
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(false);
        Object node = context.getPointer(path).getNode();
        assertNotNull(node);
        if (path.equals("/")) {
            assertSame(root, node);
        } else if (path.endsWith("@name")) {
            assertTrue(node instanceof org.w3c.dom.Attr || node instanceof org.jdom.Attribute);
        } else {
            assertTrue(node instanceof org.w3c.dom.Element || node instanceof org.jdom.Element);
        }
    }

}
