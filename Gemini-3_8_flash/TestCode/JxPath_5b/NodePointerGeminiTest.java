package org.apache.commons.jxpath.ri.model;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.apache.commons.jxpath.ri.model.NodePointer
 *
 * KNOWN DEFECT TARGET (from Defects4J):
 * - org.apache.commons.jxpath.ri.compiler.VariableTest::testUnionOfVariableAndNode
 *   Failure: JXPathException ("Cannot compare pointers that do not belong to the same tree: '' and '$var'")
 *   Mechanism: compareTo() invokes compareNodePointers() which terminates with an exception when depth == 1
 *   and pointers originate from distinct trees (e.g. VariablePointer vs BeanPointer/Root pointer).
 *   Targeted in Partition C: testUnionOfVariableAndNodeDefect() and testComparePointersDifferentTreesDefectDirect().
 *
 * BRANCH & STATE COVERAGE MATRIX:
 * 1. Factory Allocation:
 *    - newNodePointer: bean == null (NullPointer allocation), bean != null (registered factory dispatch).
 *    - newChildNodePointer: valid parent + child bean delegation.
 * 2. Tree Navigation & Hierarchy:
 *    - getParent(): direct parent, skip intermediate containers (while loop), null root.
 *    - getImmediateParentPointer(): returns immediate raw parent.
 *    - isRoot(): parent == null (true) vs parent != null (false).
 *    - getRootNode(): cached root, single-node root, multi-level parent traversal.
 * 3. Container & Value Resolution:
 *    - isContainer() == true vs false; isNode() == !isContainer().
 *    - getValuePointer() / getImmediateValuePointer(): unwinding recursive container hierarchy.
 *    - getValue(): valuePointer == this vs valuePointer != this.
 *    - getNode() & getNodeValue(): delegation to valuePointer.getImmediateNode().
 * 4. Indexing & Actuality:
 *    - isActual(): index == WHOLE_COLLECTION (true), 0 <= index < length (true),
 *      index >= length (false), negative index != WHOLE_COLLECTION (false).
 * 5. Node Matching (testNode):
 *    - test == null (true).
 *    - NodeNameTest: container check (false), name == null (false), prefix matching / NS URI resolution
 *      (equalStrings on prefixes, fallback to equalStrings on namespace URIs), wildcard matching, local name equality.
 *    - NodeTypeTest: NODE_TYPE_NODE && isNode() (true), non-NODE type (false), container node (false).
 *    - Other NodeTest types: returns false.
 * 6. Path Representation & Attribute Axis:
 *    - asPath(): root with/without leading '/', attribute prefix '@', collection index format '[n]',
 *      parent container delegation.
 * 7. Ordering (compareTo):
 *    - Identical parent: null parent comparison vs parent.compareChildNodePointers().
 *    - Asymmetric depths: depth1 < depth2 vs depth1 > depth2 recursive unwinding.
 *    - Same tree, common ancestor branch resolution.
 *    - Clones & object contracts: deep clone of parent pointer chain, toString() alignment.
 * ----------------------------------------------------------------------------------------------------
 */
public class NodePointerGeminiTest {

    // =========================================================================
    // Concrete Test Spy / Stub Harness for NodePointer
    // =========================================================================
    private static class TestNodePointer extends NodePointer {
        private Object node;
        private QName name;
        private boolean leaf = true;
        private boolean collection = false;
        private int length = 1;
        private boolean container = false;
        private NodePointer immediateValuePointer = this;
        private Map<String, String> namespaces = new HashMap<String, String>();
        private String defaultNamespaceUri = null;

        TestNodePointer(NodePointer parent, QName name, Object node) {
            super(parent);
            this.name = name;
            this.node = node;
        }

        TestNodePointer(NodePointer parent, Locale locale, QName name, Object node) {
            super(parent, locale);
            this.name = name;
            this.node = node;
        }

        public void setLeaf(boolean leaf) { this.leaf = leaf; }
        public void setCollection(boolean collection) { this.collection = collection; }
        public void setLength(int length) { this.length = length; }
        public void setContainer(boolean container) { this.container = container; }
        public void setImmediateValuePointer(NodePointer ivp) { this.immediateValuePointer = ivp; }
        public void registerNamespace(String prefix, String uri) { this.namespaces.put(prefix, uri); }
        public void setDefaultNamespaceUri(String uri) { this.defaultNamespaceUri = uri; }

        public boolean callIsDefaultNamespace(String prefix) {
            return super.isDefaultNamespace(prefix);
        }

        @Override
        public boolean isLeaf() { return leaf; }

        @Override
        public boolean isCollection() { return collection; }

        @Override
        public int getLength() { return length; }

        @Override
        public boolean isContainer() { return container; }

        @Override
        public QName getName() { return name; }

        @Override
        public Object getBaseValue() { return node; }

        @Override
        public Object getImmediateNode() { return node; }

        @Override
        public void setValue(Object value) { this.node = value; }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            if (pointer1 == pointer2) return 0;
            if (pointer1 == null) return -1;
            if (pointer2 == null) return 1;
            String n1 = pointer1.getName() != null ? pointer1.getName().toString() : "";
            String n2 = pointer2.getName() != null ? pointer2.getName().toString() : "";
            return n1.compareTo(n2);
        }

        @Override
        public NodePointer getImmediateValuePointer() {
            return immediateValuePointer;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }

        @Override
        protected String getDefaultNamespaceURI() {
            return defaultNamespaceUri;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestNodePointer)) return false;
            TestNodePointer other = (TestNodePointer) obj;
            return (name == null ? other.name == null : name.equals(other.name))
                    && (node == null ? other.node == null : node.equals(other.node));
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    private static class CustomTestNodeIterator implements NodeIterator {
        private int pos = 0;
        @Override public int getPosition() { return pos; }
        @Override public boolean setPosition(int position) { this.pos = position; return true; }
        @Override public NodePointer getNodePointer() { return null; }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryAllocationNullAndNonNull() {
        QName qName = new QName("testRoot");
        NodePointer nullPtr = NodePointer.newNodePointer(qName, null, Locale.ENGLISH);
        assertNotNull(nullPtr);
        assertTrue("Null bean must yield NullPointer instance", nullPtr instanceof NullPointer);
        assertEquals(qName, nullPtr.getName());

        NodePointer beanPtr = NodePointer.newNodePointer(qName, "literalString", Locale.GERMAN);
        assertNotNull(beanPtr);
        assertEquals(Locale.GERMAN, beanPtr.getLocale());
        assertEquals("literalString", beanPtr.getNode());

        NodePointer childPtr = NodePointer.newChildNodePointer(beanPtr, new QName("child"), 12345);
        assertNotNull(childPtr);
        assertSame(beanPtr, childPtr.getParent());
    }

    @Test(timeout = 4000)
    public void testParentTraversalAndContainerBypassing() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "rootObj");
        assertTrue(root.isRoot());
        assertNull(root.getParent());
        assertNull(root.getImmediateParentPointer());

        TestNodePointer container = new TestNodePointer(root, new QName("container"), "containerObj");
        container.setContainer(true);
        assertFalse(container.isNode());

        TestNodePointer leaf = new TestNodePointer(container, new QName("leaf"), "leafObj");
        assertSame("Immediate parent must return the direct container", container, leaf.getImmediateParentPointer());
        assertSame("getParent() must bypass container and return root", root, leaf.getParent());
    }

    @Test(timeout = 4000)
    public void testAttributeFlagAndPathRepresentation() {
        TestNodePointer root = new TestNodePointer(null, new QName("elem"), "elemObj");
        assertFalse(root.isAttribute());
        assertEquals("/elem", root.asPath());
        assertEquals("/elem", root.toString());

        TestNodePointer attr = new TestNodePointer(root, new QName("attr"), "attrVal");
        attr.setAttribute(true);
        assertTrue(attr.isAttribute());
        assertEquals("/elem/@attr", attr.asPath());

        TestNodePointer rootAttr = new TestNodePointer(null, new QName("standaloneAttr"), "val");
        rootAttr.setAttribute(true);
        assertEquals("/@standaloneAttr", rootAttr.asPath());
    }

    @Test(timeout = 4000)
    public void testIndexHandlingAndCollectionPath() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("items"), "colObj");
        assertEquals(NodePointer.WHOLE_COLLECTION, ptr.getIndex());
        ptr.setCollection(true);
        assertEquals("/items", ptr.asPath());

        ptr.setIndex(0);
        assertEquals(0, ptr.getIndex());
        assertEquals("/items[1]", ptr.asPath());

        ptr.setIndex(4);
        assertEquals("/items[5]", ptr.asPath());

        // When not marked as collection, index should not be appended
        ptr.setCollection(false);
        assertEquals("/items", ptr.asPath());
    }

    @Test(timeout = 4000)
    public void testContainerParentAsPathDelegation() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "val");
        TestNodePointer container = new TestNodePointer(root, new QName("c"), "cVal");
        container.setContainer(true);

        TestNodePointer child = new TestNodePointer(container, new QName("child"), "childVal");
        assertEquals("Parent container is responsible for asPath", container.asPath(), child.asPath());
    }

    @Test(timeout = 4000)
    public void testRootNodeResolutionAndCaching() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "rootData");
        TestNodePointer mid = new TestNodePointer(root, new QName("mid"), "midData");
        TestNodePointer child = new TestNodePointer(mid, new QName("child"), "childData");

        assertEquals("rootData", child.getRootNode());
        // Verify caching: calling again returns exact same cached instance
        assertSame(child.getRootNode(), child.getRootNode());
        assertEquals("rootData", root.getRootNode());
    }

    @Test(timeout = 4000)
    public void testValuePointerUnwrappingRecursion() {
        TestNodePointer p3 = new TestNodePointer(null, new QName("p3"), "deepValue");
        TestNodePointer p2 = new TestNodePointer(null, new QName("p2"), "midValue");
        p2.setImmediateValuePointer(p3);
        TestNodePointer p1 = new TestNodePointer(null, new QName("p1"), "shallowValue");
        p1.setImmediateValuePointer(p2);

        assertSame(p3, p1.getValuePointer());
        assertEquals("deepValue", p1.getValue());
        assertEquals("deepValue", p1.getNode());
        assertEquals("deepValue", p1.getNodeValue());
    }

    @Test(timeout = 4000)
    public void testNamespaceResolverInheritance() {
        TestNodePointer parent = new TestNodePointer(null, new QName("parent"), "p");
        NamespaceResolver resolver = new NamespaceResolver(null);
        parent.setNamespaceResolver(resolver);
        assertSame(resolver, parent.getNamespaceResolver());

        TestNodePointer child = new TestNodePointer(parent, new QName("child"), "c");
        assertSame("Child must lazily inherit namespace resolver from parent", resolver, child.getNamespaceResolver());

        TestNodePointer independent = new TestNodePointer(null, new QName("indep"), "i");
        assertNull(independent.getNamespaceResolver());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsActualBoundaries() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("item"), "data");
        ptr.setLength(3);

        ptr.setIndex(NodePointer.WHOLE_COLLECTION);
        assertTrue("WHOLE_COLLECTION must always be actual", ptr.isActual());

        ptr.setIndex(0);
        assertTrue("Index 0 should be actual for length 3", ptr.isActual());

        ptr.setIndex(2);
        assertTrue("Index length-1 should be actual", ptr.isActual());

        ptr.setIndex(3);
        assertFalse("Index == length should not be actual", ptr.isActual());

        ptr.setIndex(4);
        assertFalse("Index > length should not be actual", ptr.isActual());

        ptr.setIndex(-1);
        assertFalse("Negative index other than WHOLE_COLLECTION must be non-actual", ptr.isActual());

        ptr.setLength(0);
        ptr.setIndex(0);
        assertFalse("Index 0 for length 0 must be non-actual", ptr.isActual());
    }

    @Test(timeout = 4000)
    public void testLocaleInheritanceAndLanguageMatching() {
        TestNodePointer root = new TestNodePointer(null, Locale.US, new QName("root"), "data");
        TestNodePointer child = new TestNodePointer(root, null, new QName("child"), "childData");

        assertEquals(Locale.US, child.getLocale());
        assertTrue(child.isLanguage("en"));
        assertTrue(child.isLanguage("EN-US"));
        assertTrue(child.isLanguage("en_us"));
        assertFalse(child.isLanguage("fr"));

        TestNodePointer orphanNoLocale = new TestNodePointer(null, null, new QName("orphan"), "none");
        assertNull(orphanNoLocale.getLocale());
    }

    @Test(timeout = 4000)
    public void testDefaultNamespaceEvaluation() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("elem"), "data");
        assertTrue("Null prefix represents default namespace", ptr.callIsDefaultNamespace(null));

        assertFalse("Unbound prefix cannot be default namespace", ptr.callIsDefaultNamespace("xmlns"));

        ptr.registerNamespace("prefix1", "http://example.com/ns");
        assertFalse("Prefix with non-matching default URI", ptr.callIsDefaultNamespace("prefix1"));

        ptr.setDefaultNamespaceUri("http://example.com/ns");
        assertTrue("Prefix with matching default URI must evaluate to true", ptr.callIsDefaultNamespace("prefix1"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets the defect where comparing pointers from different trees
     * (e.g. VariablePointer and Root BeanPointer) failed with JXPathException.
     */
    @Test(timeout = 4000)
    public void testUnionOfVariableAndNodeDefect() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "value");
        JXPathContext context = JXPathContext.newContext(map);
        context.getVariables().declareVariable("var", "varValue");

        Iterator<?> iterator = context.iteratePointers("$var | /key");
        assertNotNull("Iterator should not be null", iterator);
        assertTrue("Iterator should yield at least one pointer", iterator.hasNext());

        Pointer p1 = (Pointer) iterator.next();
        assertNotNull(p1);
        assertTrue("Iterator should yield two pointers in union", iterator.hasNext());

        Pointer p2 = (Pointer) iterator.next();
        assertNotNull(p2);
        assertFalse("Iterator should have no further elements", iterator.hasNext());
    }

    @Test(timeout = 4000)
    public void testComparePointersDifferentTreesDefectDirect() {
        Map<String, Object> map = new HashMap<String, Object>();
        JXPathContext context = JXPathContext.newContext(map);
        context.getVariables().declareVariable("var", "val");

        NodePointer varPtr = (NodePointer) context.getPointer("$var");
        NodePointer rootPtr = (NodePointer) context.getPointer("/");

        int c1 = varPtr.compareTo(rootPtr);
        int c2 = rootPtr.compareTo(varPtr);
        assertTrue("Comparison between distinct pointer trees must satisfy asymmetric order contract",
                (c1 < 0 && c2 > 0) || (c1 > 0 && c2 < 0) || (c1 == 0 && c2 == 0));
    }

    // =========================================================================
    // Partition D: Decision / Branch Coverage & Defensive Guards
    // =========================================================================

    @Test(timeout = 4000)
    public void testTestNodeBranching() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("pre", "item"), "elemData");
        ptr.registerNamespace("pre", "http://ns1");

        // 1. null NodeTest -> true
        assertTrue(ptr.testNode(null));

        // 2. NodeNameTest container check
        ptr.setContainer(true);
        assertFalse(ptr.testNode(new NodeNameTest(new QName("pre", "item"))));
        ptr.setContainer(false);

        // 3. NodeNameTest null name
        TestNodePointer nullNamePtr = new TestNodePointer(null, null, "elemData");
        assertFalse(nullNamePtr.testNode(new NodeNameTest(new QName("any"))));

        // 4. NodeNameTest prefix matching with namespace URI
        NodeNameTest samePrefixMatch = new NodeNameTest(new QName("pre", "item"));
        assertTrue(ptr.testNode(samePrefixMatch));

        NodeNameTest sameNsDiffPrefixMatch = new NodeNameTest(new QName("otherPrefix", "item"));
        ptr.registerNamespace("otherPrefix", "http://ns1");
        assertTrue(ptr.testNode(sameNsDiffPrefixMatch));

        NodeNameTest diffNsMatch = new NodeNameTest(new QName("different", "item"));
        ptr.registerNamespace("different", "http://ns2");
        assertFalse(ptr.testNode(diffNsMatch));

        // 5. NodeNameTest wildcard
        NodeNameTest wildcard = new NodeNameTest(new QName("pre", "*"));
        assertTrue(ptr.testNode(wildcard));

        // 6. NodeNameTest local name mismatch
        NodeNameTest mismatchName = new NodeNameTest(new QName("pre", "differentName"));
        assertFalse(ptr.testNode(mismatchName));

        // 7. NodeTypeTest
        assertTrue(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        assertFalse(ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));

        ptr.setContainer(true);
        assertFalse("Containers are not nodes", ptr.testNode(new NodeTypeTest(Compiler.NODE_TYPE_NODE)));
        ptr.setContainer(false);

        // 8. Unhandled NodeTest implementation
        NodeTest customTest = new NodeTest() {};
        assertFalse(ptr.testNode(customTest));
    }

    @Test(timeout = 4000)
    public void testCreatePathAndChildExceptions() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("node"), "val");
        JXPathContext context = JXPathContext.newContext(new Object());

        // createPath(context) & createPath(context, value)
        assertSame(ptr, ptr.createPath(context));
        assertSame(ptr, ptr.createPath(context, "newVal"));
        assertEquals("newVal", ptr.getBaseValue());

        // remove is a no-op
        ptr.remove();

        // Unsupported createChild/createAttribute methods
        try {
            ptr.createChild(context, new QName("c"), 0, "val");
            fail("Expected JXPathException on createChild with value");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Cannot create an object for path"));
        }

        try {
            ptr.createChild(context, new QName("c"), 0);
            fail("Expected JXPathException on createChild without value");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Cannot create an object for path"));
        }

        try {
            ptr.createAttribute(context, new QName("attr"));
            fail("Expected JXPathException on createAttribute");
        } catch (JXPathException expected) {
            assertTrue(expected.getMessage().contains("Cannot create an attribute for path"));
        }
    }

    @Test(timeout = 4000)
    public void testContextPointerLookupDelegation() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("node"), "val");
        JXPathContext context = JXPathContext.newContext(new HashMap<String, Object>());

        try {
            ptr.getPointerByID(context, "targetId");
            fail("Expected JXPathException since no IdentityManager is configured");
        } catch (JXPathException expected) {
            // Expected
        }

        try {
            ptr.getPointerByKey(context, "key", "val");
            fail("Expected JXPathException since no KeyManager is configured");
        } catch (JXPathException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testIteratorsAndNamespaceDefaults() {
        TestNodePointer ptr = new TestNodePointer(null, new QName("node"), "val");
        assertNull("Self valuePointer yields null child iterator", ptr.childIterator(null, false, null));
        assertNull("Self valuePointer yields null attribute iterator", ptr.attributeIterator(new QName("a")));
        assertNull(ptr.namespaceIterator());
        assertNull(ptr.namespacePointer("any"));
        assertNull(ptr.getNamespaceURI());
        assertNull(ptr.getNamespaceURI("any"));
        assertNull(ptr.getDefaultNamespaceURI());

        // Container forwarding iterator test
        final CustomTestNodeIterator dummyIterator = new CustomTestNodeIterator();
        TestNodePointer targetPtr = new TestNodePointer(null, new QName("target"), "t") {
            @Override
            public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
                return dummyIterator;
            }
            @Override
            public NodeIterator attributeIterator(QName qname) {
                return dummyIterator;
            }
        };

        ptr.setImmediateValuePointer(targetPtr);
        assertSame(dummyIterator, ptr.childIterator(null, false, null));
        assertSame(dummyIterator, ptr.attributeIterator(new QName("a")));
    }

    @Test(timeout = 4000)
    public void testCompareToSameTreeScenarios() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "r");
        TestNodePointer childA = new TestNodePointer(root, new QName("childA"), "a");
        TestNodePointer childB = new TestNodePointer(root, new QName("childB"), "b");

        // 1. Two root nodes comparison
        assertEquals(0, root.compareTo(root));

        // 2. Siblings comparison via parent.compareChildNodePointers
        assertTrue(childA.compareTo(childB) < 0);
        assertTrue(childB.compareTo(childA) > 0);
        assertEquals(0, childA.compareTo(childA));

        // 3. Ancestor vs descendant (depth1 < depth2 and depth1 > depth2)
        TestNodePointer grandChildA = new TestNodePointer(childA, new QName("grandChildA"), "gca");
        assertTrue("Ancestor must precede descendant", root.compareTo(grandChildA) < 0);
        assertTrue("Descendant must follow ancestor", grandChildA.compareTo(root) > 0);

        // 4. Cousins comparison (same depth, different immediate parents)
        TestNodePointer grandChildB = new TestNodePointer(childB, new QName("grandChildB"), "gcb");
        assertTrue(grandChildA.compareTo(grandChildB) < 0);
        assertTrue(grandChildB.compareTo(grandChildA) > 0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneDeepParentIntegrity() {
        TestNodePointer parent = new TestNodePointer(null, new QName("parent"), "parentData");
        TestNodePointer child = new TestNodePointer(parent, new QName("child"), "childData");

        TestNodePointer clonedChild = (TestNodePointer) child.clone();
        assertNotNull(clonedChild);
        assertNotSame("Clone must produce a new instance", child, clonedChild);
        assertEquals(child.getName(), clonedChild.getName());
        assertEquals(child.getBaseValue(), clonedChild.getBaseValue());

        assertNotNull(clonedChild.getImmediateParentPointer());
        assertNotSame("Parent must be recursively cloned", child.getImmediateParentPointer(), clonedChild.getImmediateParentPointer());
        assertEquals(parent.getName(), clonedChild.getImmediateParentPointer().getName());
    }

    @Test(timeout = 4000)
    public void testPrintPointerChainExecution() {
        TestNodePointer root = new TestNodePointer(null, new QName("root"), "r");
        TestNodePointer child = new TestNodePointer(root, new QName("child"), "c");

        // Validates safe diagnostic logging execution without null pointer exceptions
        root.printPointerChain();
        child.printPointerChain();
    }
}