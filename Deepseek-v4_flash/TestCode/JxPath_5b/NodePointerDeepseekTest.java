package org.apache.commons.jxpath.ri.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: NodePointer abstract class - core pointer hierarchy in JXPath.
 * 
 * Key branches and boundary conditions exercised:
 * 1. newNodePointer: null bean -> NullPointer; non-null bean -> factory loop; 
 *    no factory found -> JXPathException; factory returns null -> continue loop.
 * 2. newChildNodePointer: factory loop, null pointer -> continue, non-null -> return.
 * 3. getNamespaceResolver: null resolver + null parent -> null; null resolver + 
 *    non-null parent -> parent.getNamespaceResolver(); non-null resolver -> return.
 * 4. setNamespaceResolver: sets field (both null and non-null).
 * 5. getParent: null parent -> null; non-null parent, non-container -> parent; 
 *    non-null parent, container -> skip containers.
 * 6. getImmediateParentPointer: returns parent field directly.
 * 7. setAttribute / isAttribute: boolean toggle.
 * 8. isRoot: parent == null.
 * 9. isNode: !isContainer() (deprecated).
 * 10. isContainer: abstract - tested via concrete subclass.
 * 11. getIndex / setIndex: default WHOLE_COLLECTION; set/get roundtrip.
 * 12. getValue: delegates to getValuePointer; if valuePointer != this -> 
 *     valuePointer.getValue(); else getImmediateNode().
 * 13. getValuePointer: getImmediateValuePointer; if ivp != this -> 
 *     ivp.getValuePointer(); else this.
 * 14. getImmediateValuePointer: abstract - tested via subclass.
 * 15. isActual: abstract - tested via subclass.
 * 16. getRootNode: rootNode field null -> getImmediateNode(); non-null -> field.
 * 17. testNode: null test -> true; NodeNameTest with isContainer -> 
 *     getImmediateNode() instanceof Node; NodeNameTest non-container -> 
 *     name matching with prefix/namespace/wildcard logic; NodeTypeTest -> 
 *     NODE_TYPE_NODE && isNode(); other -> false.
 * 18. equalStrings: both null -> true; s1 null, s2 non-null -> false; 
 *     s1 non-null, s2 null -> false; both non-null -> equals.
 * 19. createPath(context, value): default setValue(value) then return this.
 * 20. remove: no-op (default).
 * 21. createPath(context): default throws JXPathException.
 * 22. createChild(context, name, index, value): default throws JXPathException.
 * 23. createChild(context, name, index): default throws JXPathException.
 * 24. createAttribute: default throws JXPathException.
 * 25. getLocale: locale null + parent null -> null; locale null + parent 
 *     non-null -> parent.getLocale(); locale non-null -> locale.
 * 26. isLanguage: locale null -> NPE; locale non-null -> case-insensitive 
 *     prefix match with '_' replaced by '-'.
 * 27. childIterator: delegates to valuePointer.childIterator.
 * 28. attributeIterator: abstract - tested via subclass.
 * 29. namespaceIterator: abstract - tested via subclass.
 * 30. namespacePointer: abstract - tested via subclass.
 * 31. getNamespaceURI(prefix): abstract - tested via subclass.
 * 32. getNamespaceURI(): abstract - tested via subclass.
 * 33. isDefaultNamespace(prefix): prefix null -> false; namespace null -> 
 *     false; namespace equals default -> true.
 * 34. getPointerByID: delegates to context.getPointerByID.
 * 35. getPointerByKey: delegates to context.getPointerByKey.
 * 36. asPath: parent container -> parent.asPath(); empty buffer -> "/"; 
 *     attribute -> "@" + name; index != WHOLE_COLLECTION && isCollection -> 
 *     "[" + (index+1) + "]".
 * 37. clone: super.clone; parent clone; CloneNotSupportedException -> 
 *     JXPathException.
 * 38. compareTo: parent == pointer.parent -> compareChildNodePointers; 
 *     otherwise compareNodePointers with depth calculation.
 * 39. compareNodePointers: depth1 < depth2 -> -1; depth1 > depth2 -> 1; 
 *     p1 == null && p2 == null -> 0; p1 != null && p1.equals(p2) -> 
 *     depth1 == 1 ? 0 : compareNodePointers(parents); p1 == null || 
 *     p2 == null -> JXPathException; r != 0 -> r; else 0.
 * 40. printPointerChain / printDeep: debug output.
 * 
 * Defect-targeted test (Partition C):
 * - VariableTest::testUnionOfVariableAndNode triggers JXPathException:
 *   "Cannot compare pointers that do not belong to the same tree: '' and '$var'"
 *   This occurs in compareNodePointers when p1 or p2 is null after walking 
 *   up the tree, indicating pointers from different trees are being compared.
 *   The correct behavior should be to return a deterministic comparison 
 *   result (e.g., based on asPath()) rather than throwing an exception.
 *   We test that comparing pointers from different trees does NOT throw 
 *   JXPathException and returns a consistent ordering.
 */
public class NodePointerDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testNewNodePointerNullBean() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), null, Locale.US);
        assertNotNull("Null bean should produce NullPointer", pointer);
        assertTrue("Should be NullPointer instance", pointer instanceof NullPointer);
        assertEquals("Name should match", "test", pointer.getName().getName());
        assertEquals("Locale should match", Locale.US, pointer.getLocale());
    }

    @Test(timeout = 4000)
    public void testNewNodePointerNonNullBean() {
        // Use a String bean - should find a factory
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "hello", Locale.US);
        assertNotNull("Non-null bean should produce a pointer", pointer);
        assertFalse("Should not be NullPointer", pointer instanceof NullPointer);
    }

    @Test(timeout = 4000)
    public void testNewChildNodePointer() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        assertNotNull("Child pointer should not be null", child);
        assertEquals("Parent should be set", parent, child.getParent());
    }

    @Test(timeout = 4000)
    public void testGetSetNamespaceResolver() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertNull("Initial namespace resolver should be null", 
            pointer.getNamespaceResolver());
        
        NamespaceResolver resolver = new NamespaceResolver() {
            // Minimal implementation for testing
        };
        pointer.setNamespaceResolver(resolver);
        assertSame("Resolver should be set", resolver, 
            pointer.getNamespaceResolver());
    }

    @Test(timeout = 4000)
    public void testGetParentAndImmediateParent() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        assertSame("Immediate parent should be parent", parent, 
            child.getImmediateParentPointer());
        assertSame("Parent should be parent", parent, child.getParent());
        assertNull("Root parent should be null", parent.getParent());
        assertNull("Root immediate parent should be null", 
            parent.getImmediateParentPointer());
    }

    @Test(timeout = 4000)
    public void testIsRoot() {
        NodePointer root = NodePointer.newNodePointer(
            new QName("root"), "rootValue", Locale.US);
        assertTrue("Root should have no parent", root.isRoot());
        
        NodePointer child = NodePointer.newChildNodePointer(
            root, new QName("child"), "childValue");
        assertFalse("Child should not be root", child.isRoot());
    }

    @Test(timeout = 4000)
    public void testSetAttributeAndIsAttribute() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertFalse("Default should not be attribute", pointer.isAttribute());
        
        pointer.setAttribute(true);
        assertTrue("Should be attribute after set", pointer.isAttribute());
        
        pointer.setAttribute(false);
        assertFalse("Should not be attribute after reset", pointer.isAttribute());
    }

    @Test(timeout = 4000)
    public void testGetSetIndex() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertEquals("Default index should be WHOLE_COLLECTION", 
            NodePointer.WHOLE_COLLECTION, pointer.getIndex());
        
        pointer.setIndex(5);
        assertEquals("Index should be 5 after set", 5, pointer.getIndex());
        
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals("Index should reset to WHOLE_COLLECTION", 
            NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testGetValueDelegation() {
        // Use a concrete subclass - PropertyPointer for testing
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        // getValue should delegate to getValuePointer/getImmediateNode
        Object value = child.getValue();
        assertNotNull("Value should not be null", value);
    }

    @Test(timeout = 4000)
    public void testGetRootNode() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        Object root = pointer.getRootNode();
        assertNotNull("Root node should not be null", root);
        assertEquals("Root node should be the immediate node", 
            pointer.getImmediateNode(), root);
    }

    @Test(timeout = 4000)
    public void testTestNodeNull() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertTrue("Null test should match", pointer.testNode(null));
    }

    @Test(timeout = 4000)
    public void testTestNodeNameTest() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodeNameTest test = new NodeNameTest(new QName("test"));
        assertTrue("Matching name should pass", pointer.testNode(test));
        
        NodeNameTest nonMatching = new NodeNameTest(new QName("other"));
        assertFalse("Non-matching name should fail", pointer.testNode(nonMatching));
    }

    @Test(timeout = 4000)
    public void testTestNodeTypeTest() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        // isNode() returns !isContainer() - for a simple pointer, 
        // isContainer() is false, so isNode() is true
        assertTrue("Node type test should pass for node", pointer.testNode(test));
    }

    @Test(timeout = 4000)
    public void testEqualStrings() throws Exception {
        // Use reflection to test private static method
        java.lang.reflect.Method method = NodePointer.class.getDeclaredMethod(
            "equalStrings", String.class, String.class);
        method.setAccessible(true);
        
        assertTrue("Both null should be equal", 
            (Boolean) method.invoke(null, (String) null, (String) null));
        assertFalse("First null, second non-null", 
            (Boolean) method.invoke(null, (String) null, "test"));
        assertFalse("First non-null, second null", 
            (Boolean) method.invoke(null, "test", (String) null));
        assertTrue("Both same string", 
            (Boolean) method.invoke(null, "test", "test"));
        assertFalse("Different strings", 
            (Boolean) method.invoke(null, "test", "other"));
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValue() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodePointer result = pointer.createPath(null, "newValue");
        assertSame("Should return same pointer", pointer, result);
        assertEquals("Value should be set", "newValue", pointer.getValue());
    }

    @Test(timeout = 4000)
    public void testRemoveNoOp() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        // Should not throw
        pointer.remove();
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreatePathNoValueThrows() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        pointer.createPath(null);
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateChildWithValueThrows() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        pointer.createChild(null, new QName("child"), 0, "value");
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateChildNoValueThrows() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        pointer.createChild(null, new QName("child"), 0);
    }

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testCreateAttributeThrows() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        pointer.createAttribute(null, new QName("attr"));
    }

    @Test(timeout = 4000)
    public void testGetLocale() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertEquals("Locale should be US", Locale.US, pointer.getLocale());
        
        NodePointer noLocale = NodePointer.newNodePointer(
            new QName("test"), "value", null);
        assertNull("Locale should be null", noLocale.getLocale());
    }

    @Test(timeout = 4000)
    public void testIsLanguage() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertTrue("Should match en-US", pointer.isLanguage("en"));
        assertTrue("Should match EN", pointer.isLanguage("EN"));
        assertFalse("Should not match fr", pointer.isLanguage("fr"));
    }

    @Test(timeout = 4000)
    public void testChildIterator() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodeIterator iterator = pointer.childIterator(null, false, null);
        // May be null for simple values
        // Just verify no exception
    }

    @Test(timeout = 4000)
    public void testGetPointerByID() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        Pointer result = pointer.getPointerByID(context, "id");
        // May be null, just verify no exception
    }

    @Test(timeout = 4000)
    public void testGetPointerByKey() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        JXPathContext context = JXPathContext.newContext(new Object());
        Pointer result = pointer.getPointerByKey(context, "key", "value");
        // May be null, just verify no exception
    }

    @Test(timeout = 4000)
    public void testAsPath() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        String path = pointer.asPath();
        assertNotNull("Path should not be null", path);
        assertTrue("Path should not be empty", path.length() > 0);
    }

    @Test(timeout = 4000)
    public void testClone() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodePointer clone = (NodePointer) pointer.clone();
        assertNotNull("Clone should not be null", clone);
        assertNotSame("Clone should be different instance", pointer, clone);
        assertEquals("Clone should have same name", pointer.getName(), 
            clone.getName());
    }

    @Test(timeout = 4000)
    public void testCompareToSameParent() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child1 = NodePointer.newChildNodePointer(
            parent, new QName("child1"), "value1");
        NodePointer child2 = NodePointer.newChildNodePointer(
            parent, new QName("child2"), "value2");
        
        // Same parent, should use compareChildNodePointers
        int result = child1.compareTo(child2);
        // Result depends on implementation, just verify no exception
    }

    @Test(timeout = 4000)
    public void testCompareToDifferentTrees() {
        NodePointer pointer1 = NodePointer.newNodePointer(
            new QName("test1"), "value1", Locale.US);
        NodePointer pointer2 = NodePointer.newNodePointer(
            new QName("test2"), "value2", Locale.US);
        
        // Different trees - should not throw
        try {
            int result = pointer1.compareTo(pointer2);
            // If it doesn't throw, verify it returns a consistent value
            int result2 = pointer2.compareTo(pointer1);
            assertEquals("Comparison should be antisymmetric", 
                -result, result2);
        } catch (JXPathException e) {
            // This is the defect - should not throw for different trees
            fail("Should not throw JXPathException for different trees: " + 
                e.getMessage());
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testNewNodePointerNullName() {
        try {
            NodePointer pointer = NodePointer.newNodePointer(
                null, "value", Locale.US);
            // May or may not throw, just verify no crash
        } catch (Exception e) {
            // Acceptable
        }
    }

    @Test(timeout = 4000)
    public void testNewNodePointerNullLocale() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", null);
        assertNotNull("Should still create pointer", pointer);
        assertNull("Locale should be null", pointer.getLocale());
    }

    @Test(timeout = 4000)
    public void testIndexBoundaries() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        
        pointer.setIndex(Integer.MAX_VALUE);
        assertEquals("Max index", Integer.MAX_VALUE, pointer.getIndex());
        
        pointer.setIndex(Integer.MIN_VALUE);
        assertEquals("Min index (WHOLE_COLLECTION)", 
            Integer.MIN_VALUE, pointer.getIndex());
        
        pointer.setIndex(0);
        assertEquals("Zero index", 0, pointer.getIndex());
        
        pointer.setIndex(-1);
        assertEquals("Negative index", -1, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testIsLanguageBoundaries() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        
        assertTrue("Empty language should match", pointer.isLanguage(""));
        assertTrue("Null language should match", pointer.isLanguage(null));
        assertFalse("Long language should not match", 
            pointer.isLanguage("verylonglanguagename"));
    }

    @Test(timeout = 4000)
    public void testAsPathEmpty() {
        // Root pointer with no name
        NodePointer pointer = NodePointer.newNodePointer(
            new QName(""), "value", Locale.US);
        String path = pointer.asPath();
        assertNotNull("Path should not be null", path);
    }

    @Test(timeout = 4000)
    public void testCompareToSelf() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        assertEquals("Comparing to self should be 0", 0, pointer.compareTo(pointer));
    }

    @Test(timeout = 4000)
    public void testCompareToNull() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        try {
            pointer.compareTo(null);
            fail("Should throw NullPointerException or ClassCastException");
        } catch (NullPointerException e) {
            // Expected
        } catch (ClassCastException e) {
            // Expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Defect-targeted test for the known issue:
     * "Cannot compare pointers that do not belong to the same tree: '' and '$var'"
     * 
     * This test verifies that comparing pointers from different trees 
     * does NOT throw JXPathException. The correct behavior should be 
     * to return a deterministic comparison result.
     */
    @Test(timeout = 4000)
    public void testComparePointersFromDifferentTrees() {
        // Create two pointers from completely different trees
        NodePointer pointer1 = NodePointer.newNodePointer(
            new QName("root1"), "value1", Locale.US);
        NodePointer pointer2 = NodePointer.newNodePointer(
            new QName("root2"), "value2", Locale.US);
        
        // This should NOT throw JXPathException
        try {
            int result1 = pointer1.compareTo(pointer2);
            int result2 = pointer2.compareTo(pointer1);
            
            // Verify antisymmetry
            assertEquals("Comparison should be antisymmetric", 
                -result1, result2);
            
            // Verify consistency
            int result3 = pointer1.compareTo(pointer2);
            assertEquals("Comparison should be deterministic", 
                result1, result3);
        } catch (JXPathException e) {
            fail("Defect: Should not throw JXPathException when comparing " +
                "pointers from different trees: " + e.getMessage());
        }
    }

    /**
     * Additional defect-targeted test simulating the variable vs node 
     * comparison scenario from VariableTest::testUnionOfVariableAndNode.
     */
    @Test(timeout = 4000)
    public void testCompareVariableAndNodePointers() {
        // Simulate a variable pointer (root pointer with empty path)
        NodePointer variablePointer = NodePointer.newNodePointer(
            new QName("var"), "variableValue", Locale.US);
        
        // Simulate a node pointer (root pointer with a path)
        NodePointer nodePointer = NodePointer.newNodePointer(
            new QName("node"), "nodeValue", Locale.US);
        
        // The defect occurs when comparing pointers that don't share 
        // a common ancestor. This should not throw.
        try {
            int result = variablePointer.compareTo(nodePointer);
            // If it returns, verify it's a valid comparison
            assertTrue("Comparison result should be -1, 0, or 1", 
                result >= -1 && result <= 1);
        } catch (JXPathException e) {
            fail("Defect: Comparing variable and node pointers should not " +
                "throw JXPathException: " + e.getMessage());
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = JXPathException.class)
    public void testNewNodePointerNoFactory() {
        // Use an object type that has no factory registered
        // This should throw JXPathException
        NodePointer.newNodePointer(
            new QName("test"), new Object(), Locale.US);
    }

    @Test(timeout = 4000)
    public void testGetNamespaceResolverWithParent() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NamespaceResolver resolver = new NamespaceResolver() {
            // Minimal implementation
        };
        parent.setNamespaceResolver(resolver);
        
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        assertSame("Child should inherit parent's resolver", 
            resolver, child.getNamespaceResolver());
    }

    @Test(timeout = 4000)
    public void testGetLocaleWithParent() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.UK);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        assertEquals("Child should inherit parent's locale", 
            Locale.UK, child.getLocale());
    }

    @Test(timeout = 4000)
    public void testPrintPointerChain() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        // Should not throw
        child.printPointerChain();
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        NodePointer pointer1 = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        NodePointer pointer2 = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        
        // Different instances, may or may not be equal
        // Just verify no exception and consistency
        boolean eq1 = pointer1.equals(pointer2);
        boolean eq2 = pointer2.equals(pointer1);
        assertEquals("Equals should be symmetric", eq1, eq2);
        
        if (eq1) {
            assertEquals("Hash codes should match if equal", 
                pointer1.hashCode(), pointer2.hashCode());
        }
    }

    @Test(timeout = 4000)
    public void testToString() {
        NodePointer pointer = NodePointer.newNodePointer(
            new QName("test"), "value", Locale.US);
        String str = pointer.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should not be empty", str.length() > 0);
    }

    @Test(timeout = 4000)
    public void testCloneDeep() {
        NodePointer parent = NodePointer.newNodePointer(
            new QName("parent"), "parentValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            parent, new QName("child"), "childValue");
        
        NodePointer clone = (NodePointer) child.clone();
        assertNotNull("Clone should not be null", clone);
        assertNotSame("Clone should be different instance", child, clone);
        
        NodePointer cloneParent = clone.getParent();
        assertNotNull("Clone parent should not be null", cloneParent);
        assertNotSame("Clone parent should be different instance", 
            parent, cloneParent);
    }

    @Test(timeout = 4000)
    public void testCompareToDifferentDepth() {
        NodePointer root = NodePointer.newNodePointer(
            new QName("root"), "rootValue", Locale.US);
        NodePointer child = NodePointer.newChildNodePointer(
            root, new QName("child"), "childValue");
        NodePointer grandchild = NodePointer.newChildNodePointer(
            child, new QName("grandchild"), "grandchildValue");
        
        // Different depths - should not throw
        try {
            int result = root.compareTo(grandchild);
            // Result should be -1 (root is shallower)
            assertTrue("Root should be less than grandchild", result < 0);
        } catch (JXPathException e) {
            fail("Should not throw for same tree different depth: " + 
                e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCompareToSameTreeDifferentBranches() {
        NodePointer root = NodePointer.newNodePointer(
            new QName("root"), "rootValue", Locale.US);
        NodePointer child1 = NodePointer.newChildNodePointer(
            root, new QName("child1"), "value1");
        NodePointer child2 = NodePointer.newChildNodePointer(
            root, new QName("child2"), "value2");
        
        // Same tree, different branches - should not throw
        try {
            int result = child1.compareTo(child2);
            // Result depends on implementation
        } catch (JXPathException e) {
            fail("Should not throw for same tree different branches: " + 
                e.getMessage());
        }
    }
}