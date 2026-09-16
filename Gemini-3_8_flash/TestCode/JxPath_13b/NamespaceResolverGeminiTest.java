package org.apache.commons.jxpath.ri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.jxpath.ri.NamespaceResolver
 * Known Defect: Defects4J / JXPath-11 (ExternalXMLNamespaceTest::testCreateAndSetAttributeDOM)
 *               Failure: org.apache.commons.jxpath.JXPathException: Unknown namespace prefix: A
 *
 * Decision / Branch Matrix:
 * 1. registerNamespace(prefix, namespaceURI):
 *    - Branch: isSealed() == true -> Throws IllegalStateException ("Cannot register namespaces on a sealed NamespaceResolver")
 *    - Branch: isSealed() == false -> Puts into namespaceMap, invalidates reverseMap (reverseMap = null)
 * 2. getNamespaceContextPointer():
 *    - Branch: pointer == null && parent != null -> Recursively delegates to parent.getNamespaceContextPointer()
 *    - Branch: pointer == null && parent == null -> Returns null
 *    - Branch: pointer != null -> Returns pointer directly
 * 3. getNamespaceURI(prefix):
 *    - Branch: uri != null (found in local namespaceMap) -> Returns uri immediately
 *    - Branch: uri == null && pointer != null -> Queries pointer.getNamespaceURI(prefix)
 *    - Branch: uri == null && pointer == null -> Skips pointer check
 *    - Branch: uri == null && parent != null -> Queries parent.getNamespaceURI(prefix)
 *    - Branch: uri == null && parent == null -> Returns null
 * 4. getPrefix(namespaceURI):
 *    - Branch: reverseMap == null -> Lazily instantiates reverseMap:
 *      * [CRITICAL DEFECT PATH]: pointer == null dereference -> Throws NullPointerException instead of
 *        safely handling null pointer and falling back to namespaceMap / parent.
 *      * pointer != null, ni != null -> Iterates positions; checks !"".equals(prefix) (both true & false paths)
 *      * pointer != null, ni == null -> Skips pointer namespace iteration
 *      * namespaceMap entries populated into reverseMap
 *    - Branch: reverseMap != null -> Reuses cached reverseMap
 *    - Branch: prefix == null && parent != null -> Delegates to parent.getPrefix(namespaceURI)
 *    - Branch: prefix == null && parent == null -> Returns null
 *    - Branch: prefix != null -> Returns mapped prefix
 * 5. seal():
 *    - Branch: parent != null -> Sets sealed = true, recursively calls parent.seal()
 *    - Branch: parent == null -> Sets sealed = true
 * 6. clone():
 *    - Clones resolver, ensures cloned.sealed == false
 * ====================================================================================================
 */
public class NamespaceResolverGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorInitialization() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertFalse("Newly created resolver must not be sealed", resolver.isSealed());
        assertNull("Default context pointer should be null", resolver.getNamespaceContextPointer());
    }

    @Test(timeout = 4000)
    public void testRegisterAndRetrieveNamespaceLocally() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        resolver.registerNamespace("html", "http://www.w3.org/1999/xhtml");

        assertEquals("http://www.w3.org/XML/1998/namespace", resolver.getNamespaceURI("xml"));
        assertEquals("http://www.w3.org/1999/xhtml", resolver.getNamespaceURI("html"));
        assertNull("Non-registered prefix must return null", resolver.getNamespaceURI("unknown"));
    }

    @Test(timeout = 4000)
    public void testNamespaceInheritanceFromParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("common", "http://commons.apache.org");

        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("local", "http://local.apache.org");

        assertEquals("Local prefix must resolve from child", "http://local.apache.org", child.getNamespaceURI("local"));
        assertEquals("Common prefix must resolve from parent", "http://commons.apache.org", child.getNamespaceURI("common"));
        assertNull("Parent must not see child's local namespace", parent.getNamespaceURI("local"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceContextPointerHierarchy() {
        NamespaceResolver grandParent = new NamespaceResolver();
        TestNodePointer rootPointer = new TestNodePointer("http://root", null);
        grandParent.setNamespaceContextPointer(rootPointer);

        NamespaceResolver parent = new NamespaceResolver(grandParent);
        NamespaceResolver child = new NamespaceResolver(parent);

        assertSame("Child must delegate context pointer up to grandParent", rootPointer, child.getNamespaceContextPointer());

        TestNodePointer childPointer = new TestNodePointer("http://child", null);
        child.setNamespaceContextPointer(childPointer);
        assertSame("Child must return its own context pointer once explicitly set", childPointer, child.getNamespaceContextPointer());
    }

    @Test(timeout = 4000)
    public void testGetPrefixWithPointerAndEmptyPrefixBranch() {
        List<NodePointer> nsPointers = new ArrayList<NodePointer>();
        nsPointers.add(new TestNamespacePointer("ns1", "http://ns1"));
        nsPointers.add(new TestNamespacePointer("", "http://default")); // Branch: !"".equals(prefix) == false

        TestNodeIterator iterator = new TestNodeIterator(nsPointers);
        TestNodePointer pointer = new TestNodePointer(null, iterator);

        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("localNs", "http://local");

        assertEquals("Prefix should be resolved from pointer iterator", "ns1", resolver.getPrefix("http://ns1"));
        assertEquals("Prefix should be resolved from local namespaceMap", "localNs", resolver.getPrefix("http://local"));
        assertNull("Empty prefix should not be registered in reverseMap", resolver.getPrefix("http://default"));
    }

    @Test(timeout = 4000)
    public void testGetPrefixCachedReverseMapAndCacheInvalidation() {
        List<NodePointer> nsPointers = Collections.singletonList(
                (NodePointer) new TestNamespacePointer("p1", "http://uri1")
        );
        TestNodeIterator iterator = new TestNodeIterator(nsPointers);
        TestNodePointer pointer = new TestNodePointer(null, iterator);

        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("p2", "http://uri2");

        // First call populates reverseMap
        assertEquals("p1", resolver.getPrefix("http://uri1"));
        assertEquals("p2", resolver.getPrefix("http://uri2"));

        // Registering a new namespace must invalidate reverseMap
        resolver.registerNamespace("p3", "http://uri3");
        assertEquals("Newly registered namespace should be queryable after cache invalidation",
                "p3", resolver.getPrefix("http://uri3"));
    }

    @Test(timeout = 4000)
    public void testGetPrefixParentDelegation() {
        List<NodePointer> parentPointers = Collections.singletonList(
                (NodePointer) new TestNamespacePointer("parentPre", "http://parentUri")
        );
        TestNodePointer parentPointer = new TestNodePointer(null, new TestNodeIterator(parentPointers));

        NamespaceResolver parent = new NamespaceResolver();
        parent.setNamespaceContextPointer(parentPointer);

        TestNodePointer childPointer = new TestNodePointer(null, new TestNodeIterator(Collections.<NodePointer>emptyList()));
        NamespaceResolver child = new NamespaceResolver(parent);
        child.setNamespaceContextPointer(childPointer);

        assertEquals("Child should delegate prefix lookup to parent", "parentPre", child.getPrefix("http://parentUri"));
        assertNull("Unknown URI should return null", child.getPrefix("http://nonexistent"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithPointerReturningNull() {
        TestNodePointer pointer = new TestNodePointer(null, null); // getNamespaceURI returns null
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("shared", "http://shared.org");

        NamespaceResolver child = new NamespaceResolver(parent);
        child.setNamespaceContextPointer(pointer);

        // Child local: null -> pointer: null -> parent: found
        assertEquals("http://shared.org", child.getNamespaceURI("shared"));
        // Child local: null -> pointer: null -> parent: null -> null
        assertNull(child.getNamespaceURI("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetNamespaceURIWithPointerReturningValue() {
        TestNodePointer pointer = new TestNodePointer("http://pointer.org", null);
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);

        assertEquals("http://pointer.org", resolver.getNamespaceURI("anyPrefix"));
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullPrefixHandling() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("", "http://default.namespace");
        resolver.registerNamespace("nullUri", null);

        assertEquals("http://default.namespace", resolver.getNamespaceURI(""));
        assertNull("Null URI registration must return null", resolver.getNamespaceURI("nullUri"));
    }

    @Test(timeout = 4000)
    public void testPointerWithNullNamespaceIterator() {
        TestNodePointer pointer = new TestNodePointer(null, null); // namespaceIterator() returns null
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("pfx", "http://sample.com");

        assertEquals("pfx", resolver.getPrefix("http://sample.com"));
        assertNull(resolver.getPrefix("http://notfound.com"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J / JXPath-11)
    // =========================================================================

    /**
     * Target Defect: ExternalXMLNamespaceTest::testCreateAndSetAttributeDOM
     * Failure: Unknown namespace prefix: A
     *
     * In the defective implementation of NamespaceResolver.java, getPrefix(namespaceURI) executes:
     *     NodeIterator ni = pointer.namespaceIterator();
     * without validating whether 'pointer' is null. When a context / NamespaceResolver has
     * registered namespaces but no NodePointer has been bound yet (pointer == null), calling
     * getPrefix() throws a NullPointerException, breaking prefix resolution.
     */
    @Test(timeout = 4000)
    public void testDefectExternalXMLNamespacePrefixResolutionWithNullPointer() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("A", "http://foo");

        // Asserts correct behavior: should resolve prefix "A" without throwing NullPointerException
        String prefix = resolver.getPrefix("http://foo");
        assertEquals("Registered namespace prefix 'A' must be resolved when pointer is null", "A", prefix);
    }

    /**
     * Target Defect: Parent resolver prefix resolution fails due to null pointer in parent.
     */
    @Test(timeout = 4000)
    public void testDefectChildResolverParentPrefixLookupWhenParentHasNullPointer() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("A", "http://foo");
        // parent.pointer remains null

        TestNodePointer childPointer = new TestNodePointer(null, null);
        NamespaceResolver child = new NamespaceResolver(parent);
        child.setNamespaceContextPointer(childPointer);

        // Child delegates to parent whose pointer is null
        String prefix = child.getPrefix("http://foo");
        assertEquals("Child resolver must resolve parent prefix 'A' even if parent.pointer is null", "A", prefix);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testRegisterNamespaceOnSealedResolverThrowsException() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        assertTrue("Resolver should be marked as sealed", resolver.isSealed());
        resolver.registerNamespace("err", "http://err.org");
    }

    @Test(timeout = 4000)
    public void testSealPropagatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);

        assertFalse(parent.isSealed());
        assertFalse(child.isSealed());

        child.seal();

        assertTrue("Child must be sealed", child.isSealed());
        assertTrue("Parent must also be sealed when child is sealed", parent.isSealed());

        try {
            parent.registerNamespace("p", "http://p");
            fail("Expected IllegalStateException when modifying sealed parent");
        } catch (IllegalStateException expected) {
            assertEquals("Cannot register namespaces on a sealed NamespaceResolver", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneLifecycleAndSealStatus() {
        NamespaceResolver original = new NamespaceResolver();
        original.registerNamespace("k1", "http://v1");
        original.seal();
        assertTrue("Original must be sealed", original.isSealed());

        NamespaceResolver clone = (NamespaceResolver) original.clone();
        assertNotNull("Clone must not be null", clone);
        assertNotSame("Clone must be a distinct instance", original, clone);
        assertFalse("Clone must be unsealed according to clone() contract", clone.isSealed());

        // Clone should allow new registrations
        clone.registerNamespace("k2", "http://v2");
        assertEquals("http://v2", clone.getNamespaceURI("k2"));
        assertEquals("http://v1", clone.getNamespaceURI("k1"));

        // Original remains sealed
        assertTrue("Original must still be sealed", original.isSealed());
    }

    @Test(timeout = 4000)
    public void testCloneRetainsParentReference() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentKey", "http://parentVal");

        NamespaceResolver child = new NamespaceResolver(parent);
        NamespaceResolver clonedChild = (NamespaceResolver) child.clone();

        assertEquals("Cloned child must retain parent namespace resolution",
                "http://parentVal", clonedChild.getNamespaceURI("parentKey"));
    }

    // =========================================================================
    // Deterministic Test Doubles for NodePointer and NodeIterator
    // =========================================================================

    private static class TestNodePointer extends VariablePointer {
        private final String uri;
        private final NodeIterator iterator;

        TestNodePointer(String uri, NodeIterator iterator) {
            super(new QName("testNode"));
            this.uri = uri;
            this.iterator = iterator;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return uri;
        }

        @Override
        public NodeIterator namespaceIterator() {
            return iterator;
        }
    }

    private static class TestNamespacePointer extends VariablePointer {
        private final String uri;

        TestNamespacePointer(String prefix, String uri) {
            super(new QName(prefix));
            this.uri = uri;
        }

        @Override
        public String getNamespaceURI() {
            return uri;
        }
    }

    private static class TestNodeIterator implements NodeIterator {
        private final List<NodePointer> list;
        private int position = 0;

        TestNodeIterator(List<NodePointer> list) {
            this.list = list != null ? list : Collections.<NodePointer>emptyList();
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public boolean setPosition(int position) {
            this.position = position;
            return position >= 1 && position <= list.size();
        }

        @Override
        public NodePointer getNodePointer() {
            if (position >= 1 && position <= list.size()) {
                return list.get(position - 1);
            }
            return null;
        }
    }
}