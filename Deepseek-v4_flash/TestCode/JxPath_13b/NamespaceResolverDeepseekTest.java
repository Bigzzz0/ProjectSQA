package org.apache.commons.jxpath.ri;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: NamespaceResolver
 * 
 * Decision branches covered:
 * 1. registerNamespace: sealed check (line with isSealed())
 * 2. getNamespaceContextPointer: pointer==null && parent!=null -> parent; pointer!=null -> pointer; else null.
 * 3. getNamespaceURI: 
 *    - a) namespaceMap.get(prefix) != null -> return that URI.
 *    - b) uri==null && pointer!=null -> pointer.getNamespaceURI(prefix)
 *    - c) uri still null && parent!=null -> parent.getNamespaceURI(prefix)
 *    - d) return null if all above fail.
 * 4. getPrefix: 
 *    - a) reverseMap==null -> build from pointer.namespaceIterator() (+ fallback to empty if pointer==null)
 *    - b) Then add entries from namespaceMap.
 *    - c) reverseMap.get(namespaceURI) != null -> return prefix.
 *    - d) prefix==null && parent!=null -> parent.getPrefix(namespaceURI)
 *    - e) return null.
 * 5. seal(): sets sealed=true; if parent!=null -> parent.seal()
 * 6. clone(): creates new NamespaceResolver, sealed=false.
 * 
 * Boundary conditions:
 * - null prefix / null namespaceURI as arguments.
 * - pointer == null vs pointer != null.
 * - parent == null vs parent != null.
 * - reverseMap == null (trigger building) vs already built.
 * - namespaceMap empty vs contains entries.
 * 
 * Defect target (Defects4J): When an external namespace prefix "A" is defined on a node (via pointer's getNamespaceURI), 
 * the resolver fails to find it and returns null, causing "Unknown namespace prefix: A". 
 * This test uses a custom NodePointer that returns a namespace URI for prefix "A" and verifies getNamespaceURI("A") returns the expected URI.
 * In the defective version, the lookup fails either because pointer is ignored or the parent delegation is incorrect.
 */
public class NamespaceResolverDeepseekTest {

    // ---- Helper NodePointer stubs ----
    
    // A NodePointer that provides a fixed namespace URI for a given prefix via getNamespaceURI and namespaceIterator
    private static class SimpleNamespaceNodePointer extends NodePointer {
        private final Map<String, String> nsMap;
        private final NodeIterator iterator;
        
        SimpleNamespaceNodePointer(Map<String, String> nsMap) {
            super(null);  // parent is null for simplicity
            this.nsMap = nsMap;
            // Build a NodeIterator over the entries of nsMap (only for non-empty prefix)
            this.iterator = nsMap.isEmpty() ? null : new NodeIterator() {
                private int pos = 0;
                private final String[] prefixes = nsMap.keySet().toArray(new String[0]);
                
                @Override
                public boolean setPosition(int position) {
                    if (position < 1 || position > prefixes.length) {
                        return false;
                    }
                    pos = position;
                    return true;
                }
                
                @Override
                public NodePointer getNodePointer() {
                    if (pos < 1 || pos > prefixes.length) {
                        return null;
                    }
                    String prefix = prefixes[pos-1];
                    // Return a pointer that represents this namespace declaration
                    return new NodePointer(null) {
                        @Override
                        public String getNamespaceURI() {
                            return nsMap.get(prefix);
                        }
                        
                        @Override
                        public QName getName() {
                            // Name is the prefix itself
                            return new QName(prefix);
                        }
                        
                        // Minimal overrides to satisfy abstract methods (not used)
                        @Override
                        public Object getValue() { return null; }
                        @Override
                        public Object getBaseValue() { return null; }
                        @Override
                        public boolean isLeaf() { return false; }
                        @Override
                        public boolean isCollection() { return false; }
                        @Override
                        public int getLength() { return 0; }
                        @Override
                        public Object getImmediateNode() { return null; }
                        @Override
                        public String asPath() { return ""; }
                        @Override
                        public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }
                        @Override
                        public boolean isActual() { return true; }
                        @Override
                        public boolean isContainer() { return false; }
                    };
                }
            };
        }
        
        @Override
        public String getNamespaceURI(String prefix) {
            return nsMap.get(prefix);
        }
        
        @Override
        public NodeIterator namespaceIterator() {
            return iterator;
        }
        
        // Required abstract methods - not used for these tests
        @Override public Object getValue() { return null; }
        @Override public Object getBaseValue() { return null; }
        @Override public boolean isLeaf() { return false; }
        @Override public boolean isCollection() { return false; }
        @Override public int getLength() { return 0; }
        @Override public Object getImmediateNode() { return null; }
        @Override public String asPath() { return ""; }
        @Override public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }
        @Override public boolean isActual() { return true; }
        @Override public boolean isContainer() { return false; }
    }
    
    // A NodePointer that returns null for namespaceIterator (to test fallback when pointer == null)
    private static class NullIteratorNodePointer extends NodePointer {
        NullIteratorNodePointer() { super(null); }
        
        @Override
        public String getNamespaceURI(String prefix) {
            return null;  // always returns null
        }
        
        @Override
        public NodeIterator namespaceIterator() {
            return null;
        }
        
        // Required abstract methods as above
        @Override public Object getValue() { return null; }
        @Override public Object getBaseValue() { return null; }
        @Override public boolean isLeaf() { return false; }
        @Override public boolean isCollection() { return false; }
        @Override public int getLength() { return 0; }
        @Override public Object getImmediateNode() { return null; }
        @Override public String asPath() { return ""; }
        @Override public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }
        @Override public boolean isActual() { return true; }
        @Override public boolean isContainer() { return false; }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testRegisterAndRetrieveNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("pre", "http://example.com/pre");
        assertEquals("http://example.com/pre", resolver.getNamespaceURI("pre"));
        assertEquals("pre", resolver.getPrefix("http://example.com/pre"));
    }
    
    @Test(timeout = 4000)
    public void testRegisterMultipleNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "uriA");
        resolver.registerNamespace("b", "uriB");
        assertEquals("uriA", resolver.getNamespaceURI("a"));
        assertEquals("uriB", resolver.getNamespaceURI("b"));
        assertEquals("a", resolver.getPrefix("uriA"));
        assertEquals("b", resolver.getPrefix("uriB"));
    }
    
    @Test(timeout = 4000)
    public void testGetNamespaceContextPointerNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceContextPointer());
    }
    
    @Test(timeout = 4000)
    public void testSetNamespaceContextPointer() {
        NamespaceResolver resolver = new NamespaceResolver();
        NullIteratorNodePointer ptr = new NullIteratorNodePointer();
        resolver.setNamespaceContextPointer(ptr);
        assertSame(ptr, resolver.getNamespaceContextPointer());
    }
    
    @Test(timeout = 4000)
    public void testGetNamespaceContextPointerDelegatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NullIteratorNodePointer ptr = new NullIteratorNodePointer();
        parent.setNamespaceContextPointer(ptr);
        NamespaceResolver child = new NamespaceResolver(parent);
        assertSame(ptr, child.getNamespaceContextPointer());
    }
    
    @Test(timeout = 4000)
    public void testGetNamespaceContextPointerNoParent() {
        NamespaceResolver child = new NamespaceResolver(null);
        assertNull(child.getNamespaceContextPointer());
    }
    
    @Test(timeout = 4000)
    public void testCloneUnsealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("x", "uriX");
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertNotNull(clone);
        assertFalse(clone.isSealed());
        assertEquals("uriX", clone.getNamespaceURI("x"));
        // Verify clone is independent
        clone.registerNamespace("y", "uriY");
        assertNull(resolver.getNamespaceURI("y"));
    }
    
    // ========== Partition B: Boundary Value Analysis & Extremes ==========
    
    @Test(timeout = 4000)
    public void testNullPrefixInGetNamespaceURI() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceURI(null));
    }
    
    @Test(timeout = 4000)
    public void testNullNamespaceURIInGetPrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "uri");
        // getPrefix(null) – should return null (reverseMap may contain null key? but not in this scenario)
        assertNull(resolver.getPrefix(null));
    }
    
    @Test(timeout = 4000)
    public void testEmptyStringPrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        // register an empty prefix? The code allows it.
        resolver.registerNamespace("", "defaultURI");
        assertEquals("defaultURI", resolver.getNamespaceURI(""));
        // getPrefix for that URI: the reverseMap is built from namespaceMap, entry: (uri, prefix) with prefix=""
        assertEquals("", resolver.getPrefix("defaultURI"));
    }
    
    @Test(timeout = 4000)
    public void testUnknownPrefixReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("known", "uriK");
        assertNull(resolver.getNamespaceURI("unknown"));
    }
    
    @Test(timeout = 4000)
    public void testUnknownURIReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "uri");
        assertNull(resolver.getPrefix("nonexistentURI"));
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testGetNamespaceURIWithPointer() {
        // Create a pointer that defines prefix "A" -> "http://example.com/A"
        Map<String, String> nsDefs = new HashMap<>();
        nsDefs.put("A", "http://example.com/A");
        SimpleNamespaceNodePointer pointer = new SimpleNamespaceNodePointer(nsDefs);
        
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        
        // This should return the URI from the pointer
        assertEquals("http://example.com/A", resolver.getNamespaceURI("A"));
    }
    
    @Test(timeout = 4000)
    public void testGetNamespaceURIWithPointerAndExplicitRegistration() {
        // Explicit registration overrides pointer's definition (since namespaceMap check happens first)
        Map<String, String> nsDefs = new HashMap<>();
        nsDefs.put("A", "http://example.com/pointerA");
        SimpleNamespaceNodePointer pointer = new SimpleNamespaceNodePointer(nsDefs);
        
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        resolver.registerNamespace("A", "http://example.com/explicitA");
        
        assertEquals("http://example.com/explicitA", resolver.getNamespaceURI("A"));
    }
    
    @Test(timeout = 4000)
    public void testGetPrefixWithPointer() {
        // pointer defines namespace "http://example.com/A" with prefix "A"
        Map<String, String> nsDefs = new HashMap<>();
        nsDefs.put("A", "http://example.com/A");
        SimpleNamespaceNodePointer pointer = new SimpleNamespaceNodePointer(nsDefs);
        
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceContextPointer(pointer);
        
        // getPrefix should find via reverseMap built from pointer's namespaceIterator
        assertEquals("A", resolver.getPrefix("http://example.com/A"));
    }
    
    @Test(timeout = 4000)
    public void testGetPrefixWithParentDelegation() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("B", "http://example.com/B");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("B", child.getPrefix("http://example.com/B"));
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testRegisterNamespaceOnSealedResolver() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.registerNamespace("x", "uri");  // should throw
    }
    
    @Test(timeout = 4000)
    public void testSealPropagatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        NamespaceResolver child = new NamespaceResolver(parent);
        child.seal();
        assertTrue(child.isSealed());
        assertTrue(parent.isSealed());
        // try register on parent
        try {
            parent.registerNamespace("y", "uriY");
            fail("Expected IllegalStateException on sealed parent");
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testCloneNotSealed() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertFalse(clone.isSealed());
        // can register
        clone.registerNamespace("ok", "uriOK");
        assertEquals("uriOK", clone.getNamespaceURI("ok"));
    }
    
    @Test(timeout = 4000)
    public void testGetPrefixWithNullPointerNoReverseMap() {
        // pointer is null, reverseMap not yet built, but pointer is null so namespaceIterator not called
        NamespaceResolver resolver = new NamespaceResolver();
        // no pointer, no parent, no registrations
        assertNull(resolver.getPrefix("anyURI"));
    }
    
    @Test(timeout = 4000)
    public void testGetPrefixReverseMapIsBuiltOnce() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("x", "uriX");
        // first call builds reverseMap
        assertEquals("x", resolver.getPrefix("uriX"));
        // second call uses cached map
        assertEquals("x", resolver.getPrefix("uriX"));
    }
    
    @Test(timeout = 4000)
    public void testGetNamespaceURIDelegatesToParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("parentPrefix", "parentURI");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("parentURI", child.getNamespaceURI("parentPrefix"));
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testClonePreservesStateButUnsealed() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "uriP");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("c", "uriC");
        child.setNamespaceContextPointer(new NullIteratorNodePointer());
        child.seal();
        
        NamespaceResolver clone = (NamespaceResolver) child.clone();
        assertFalse(clone.isSealed());
        assertEquals("uriC", clone.getNamespaceURI("c"));
        assertEquals("uriP", clone.getNamespaceURI("p"));
        // parent relationship is also cloned? clone() uses super.clone() which does shallow copy,
        // so parent reference is shared with original. That is expected.
        assertSame(parent, clone.parent);
    }
    
    @Test(timeout = 4000)
    public void testMultipleGettersConsistency() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "nsA");
        resolver.registerNamespace("b", "nsB");
        assertEquals("nsA", resolver.getNamespaceURI("a"));
        assertEquals("nsB", resolver.getNamespaceURI("b"));
        assertEquals("a", resolver.getPrefix("nsA"));
        assertEquals("b", resolver.getPrefix("nsB"));
        assertNull(resolver.getNamespaceURI("nonexistent"));
        assertNull(resolver.getPrefix("nonexistentURI"));
    }
    
    @Test(timeout = 4000)
    public void testReverseMapUpdatedAfterNewRegistration() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("x", "uriX");
        // prime reverseMap
        assertEquals("x", resolver.getPrefix("uriX"));
        // now register new mapping
        resolver.registerNamespace("y", "uriY");
        // reverseMap should be reset by registerNamespace
        assertNull(resolver.getPrefix("uriY"));  // reverseMap was reset to null, so it will be rebuilt
        // actually after registerNamespace, reverseMap is set to null, so next getPrefix will rebuild
        assertEquals("y", resolver.getPrefix("uriY"));  // now it should work
        assertEquals("x", resolver.getPrefix("uriX"));  // still works because rebuilt map includes both
    }
}