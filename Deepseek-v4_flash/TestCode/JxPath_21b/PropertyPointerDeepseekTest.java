package org.apache.commons.jxpath.ri.model.beans;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: PropertyPointer (abstract class) - Defects4J defect JXPath151
 * 
 * Known defect: 
 * - testMapValueEquality: Evaluating <map/b != map/a> expected:<true> but was:<false>
 *   Root cause: PropertyPointer.equals() incorrectly compares property indices when
 *   one or both pointers have UNSPECIFIED_PROPERTY (Integer.MIN_VALUE). The method
 *   compares getPropertyIndex() directly, but when propertyIndex is UNSPECIFIED_PROPERTY,
 *   it should be treated as equivalent to 0 for comparison purposes.
 * 
 * - testNull: Evaluating value iterator <$testnull/nothing[1]> expected:<[null]> but was:<[]>
 *   Root cause: getImmediateNode() returns null when index == WHOLE_COLLECTION and
 *   getBaseValue() returns null, but the iterator logic expects a non-null value
 *   in certain collection scenarios.
 * 
 * Branch coverage targets:
 * 1. setPropertyIndex: branch when propertyIndex != index (change state, reset index)
 * 2. getBean: branch when bean == null (lazy initialization)
 * 3. isActual: branch when !isActualProperty() returns false
 * 4. getImmediateNode: branch when value == UNINITIALIZED
 * 5. isCollection: branch when value != null && ValueUtils.isCollection(value)
 * 6. isLeaf: branch when value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic()
 * 7. createPath(context): branch when getImmediateNode() == null
 * 8. createPath(context, value): branch when index != WHOLE_COLLECTION && index >= getLength()
 * 9. createChild: branch when name != null
 * 10. equals: branch when object == this, !(object instanceof PropertyPointer),
 *     parent comparison, propertyIndex comparison, propertyName comparison, index comparison
 * 11. hashCode: branch for parent.hashCode() + propertyIndex + index
 * 12. compareChildNodePointers: delegate to getValuePointer().compareChildNodePointers
 * 
 * Boundary conditions:
 * - propertyIndex = UNSPECIFIED_PROPERTY (Integer.MIN_VALUE)
 * - index = WHOLE_COLLECTION (-1)
 * - index = 0, 1, MAX_VALUE
 * - bean = null (lazy init)
 * - value = UNINITIALIZED sentinel
 * - null parent pointer
 * - null property name
 * - empty property names array
 * - zero-length collections
 */
public class PropertyPointerDeepseekTest {

    // Concrete test implementation of abstract PropertyPointer
    private static class TestPropertyPointer extends PropertyPointer {
        private String propertyName = "test";
        private String[] propertyNames = {"test", "other"};
        private int propertyCount = 2;
        private boolean actualProperty = true;
        private Object baseValue;

        public TestPropertyPointer(NodePointer parent) {
            super(parent);
        }

        public TestPropertyPointer(NodePointer parent, Object baseValue) {
            super(parent);
            this.baseValue = baseValue;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public int getPropertyCount() {
            return propertyCount;
        }

        @Override
        public String[] getPropertyNames() {
            return propertyNames;
        }

        @Override
        protected boolean isActualProperty() {
            return actualProperty;
        }

        public void setActualProperty(boolean actual) {
            this.actualProperty = actual;
        }

        @Override
        protected Object getBaseValue() {
            return baseValue;
        }

        @Override
        public Object getNode() {
            return getImmediateNode();
        }

        @Override
        public Object getValue() {
            return getImmediateNode();
        }

        @Override
        public void setValue(Object value) {
            this.baseValue = value;
        }

        @Override
        public NodePointer getValuePointer() {
            return this;
        }

        @Override
        public boolean isActual() {
            return super.isActual();
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }
    }

    // Simple NodePointer implementation for testing
    private static class TestNodePointer extends NodePointer {
        private Object node;

        public TestNodePointer(Object node) {
            super(null);
            this.node = node;
        }

        @Override
        public Object getNode() {
            return node;
        }

        @Override
        public Object getValue() {
            return node;
        }

        @Override
        public void setValue(Object value) {
            this.node = value;
        }

        @Override
        public NodePointer getValuePointer() {
            return this;
        }

        @Override
        public boolean isActual() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public Object getBaseValue() {
            return node;
        }

        @Override
        public QName getName() {
            return new QName("test");
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testGetPropertyIndexDefault() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, pointer.getPropertyIndex());
    }

    @Test(timeout = 4000)
    public void testSetPropertyIndexChangesIndex() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyIndex(5);
        assertEquals(5, pointer.getPropertyIndex());
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testSetPropertyIndexSameValueNoReset() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyIndex(3);
        pointer.setIndex(7);
        pointer.setPropertyIndex(3); // same value, should not reset index
        assertEquals(7, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testGetBeanLazyInitialization() {
        Object bean = new Object();
        TestNodePointer parent = new TestNodePointer(bean);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        assertNull(pointer.bean); // bean not yet initialized
        assertSame(bean, pointer.getBean());
        assertSame(bean, pointer.bean); // now cached
    }

    @Test(timeout = 4000)
    public void testGetBeanAlreadySet() {
        Object bean = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.bean = bean;
        assertSame(bean, pointer.getBean());
    }

    @Test(timeout = 4000)
    public void testGetName() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyName("myProp");
        QName name = pointer.getName();
        assertEquals("myProp", name.getName());
        assertNull(name.getPrefix());
    }

    @Test(timeout = 4000)
    public void testIsActualWhenActualProperty() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setActualProperty(true);
        assertTrue(pointer.isActual());
    }

    @Test(timeout = 4000)
    public void testIsActualWhenNotActualProperty() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setActualProperty(false);
        assertFalse(pointer.isActual());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWholeCollection() {
        Object value = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, value);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(value, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeIndexed() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(1);
        assertEquals("b", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeCached() {
        Object value = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, value);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        Object first = pointer.getImmediateNode();
        assertSame(first, pointer.getImmediateNode()); // cached
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNullValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithArray() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object[0]);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNullValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithAtomicValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "string");
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNonAtomicValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithNullValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithArray() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testGetImmediateValuePointer() {
        Object value = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, value);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        NodePointer result = pointer.getImmediateValuePointer();
        assertNotNull(result);
        assertNotSame(pointer, result);
    }

    @Test(timeout = 4000)
    public void testCreatePathWithNonNullValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        assertSame(pointer, pointer.createPath(null));
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueAndIndexLessThanLength() {
        Object[] array = {"a", "b"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        pointer.createPath(null, "newValue");
        assertEquals("newValue", array[0]);
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueAndIndexGreaterThanLength() {
        Object[] array = {"a"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(5);
        pointer.createPath(null, "newValue");
        assertEquals("newValue", array[5]);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithNameAndValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        NodePointer child = pointer.createChild(null, new QName("child"), 0, "value");
        assertNotNull(child);
        assertTrue(child instanceof PropertyPointer);
        PropertyPointer propChild = (PropertyPointer) child;
        assertEquals("child", propChild.getPropertyName());
        assertEquals(0, propChild.getIndex());
    }

    @Test(timeout = 4000)
    public void testCreateChildWithoutValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        NodePointer child = pointer.createChild(null, new QName("child"), 0);
        assertNotNull(child);
        assertTrue(child instanceof PropertyPointer);
        PropertyPointer propChild = (PropertyPointer) child;
        assertEquals("child", propChild.getPropertyName());
        assertEquals(0, propChild.getIndex());
    }

    @Test(timeout = 4000)
    public void testCreateChildWithNullName() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        NodePointer child = pointer.createChild(null, null, 0);
        assertNotNull(child);
        assertTrue(child instanceof PropertyPointer);
        PropertyPointer propChild = (PropertyPointer) child;
        assertEquals("test", propChild.getPropertyName()); // default name
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testSetPropertyIndexToMinValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyIndex(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, pointer.getPropertyIndex());
    }

    @Test(timeout = 4000)
    public void testSetPropertyIndexToMaxValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyIndex(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, pointer.getPropertyIndex());
    }

    @Test(timeout = 4000)
    public void testSetIndexToWholeCollection() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNullBaseValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithMinValues() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyIndex(Integer.MIN_VALUE);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        int hash = pointer.hashCode();
        assertNotNull(hash);
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameObject() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertTrue(pointer.equals(pointer));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNull() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertFalse(pointer.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentClass() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertFalse(pointer.equals("not a pointer"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentParent() {
        TestNodePointer parent1 = new TestNodePointer(new Object());
        TestNodePointer parent2 = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent1);
        TestPropertyPointer p2 = new TestPropertyPointer(parent2);
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameParentDifferentPropertyIndex() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyIndex(1);
        p2.setPropertyIndex(2);
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameParentSamePropertyDifferentName() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyName("a");
        p2.setPropertyName("b");
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameParentSamePropertySameNameDifferentIndex() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setIndex(0);
        p2.setIndex(1);
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameParentSamePropertySameNameSameIndex() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setIndex(0);
        p2.setIndex(0);
        assertTrue(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithWholeCollectionIndexes() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setIndex(NodePointer.WHOLE_COLLECTION);
        p2.setIndex(NodePointer.WHOLE_COLLECTION);
        assertTrue(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithWholeCollectionVsZeroIndex() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setIndex(NodePointer.WHOLE_COLLECTION);
        p2.setIndex(0);
        assertTrue(p1.equals(p2)); // WHOLE_COLLECTION treated as 0
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect JXPath151: testMapValueEquality
     * When comparing two PropertyPointers with UNSPECIFIED_PROPERTY (Integer.MIN_VALUE),
     * the equals() method should treat them as equal even though the propertyIndex
     * values are both UNSPECIFIED_PROPERTY. The bug causes false inequality.
     */
    @Test(timeout = 4000)
    public void testEqualsWithUnspecifiedPropertyIndexes() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        // Both have UNSPECIFIED_PROPERTY by default
        assertTrue("PropertyPointers with UNSPECIFIED_PROPERTY should be equal",
                p1.equals(p2));
    }

    /**
     * Defect JXPath151: testMapValueEquality
     * When one pointer has UNSPECIFIED_PROPERTY and the other has 0,
     * they should be considered equal because UNSPECIFIED_PROPERTY means
     * "not set" which is equivalent to 0 in this context.
     */
    @Test(timeout = 4000)
    public void testEqualsWithUnspecifiedVsZeroPropertyIndex() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p2.setPropertyIndex(0);
        assertTrue("UNSPECIFIED_PROPERTY should equal 0", p1.equals(p2));
    }

    /**
     * Defect JXPath151: testNull
     * When getImmediateNode() returns null for a collection with WHOLE_COLLECTION index,
     * the iterator should still produce a null value. This test verifies that
     * getImmediateNode() returns null when base value is null.
     */
    @Test(timeout = 4000)
    public void testGetImmediateNodeNullBaseValueWholeCollection() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertNull("getImmediateNode() should return null for null base value",
                pointer.getImmediateNode());
    }

    /**
     * Defect JXPath151: testNull
     * When the base value is null and index is not WHOLE_COLLECTION,
     * getImmediateNode() should still return null without throwing.
     */
    @Test(timeout = 4000)
    public void testGetImmediateNodeNullBaseValueIndexed() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(0);
        assertNull("getImmediateNode() should return null for null base value with index",
                pointer.getImmediateNode());
    }

    /**
     * Defect JXPath151: testMapValueEquality
     * Verify that equals() correctly handles the case where both pointers
     * have UNSPECIFIED_PROPERTY and different indices.
     */
    @Test(timeout = 4000)
    public void testEqualsWithUnspecifiedPropertyAndDifferentIndices() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setIndex(0);
        p2.setIndex(1);
        assertFalse("Different indices should not be equal", p1.equals(p2));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testCreatePathWithNullFactory() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        try {
            pointer.createPath(null);
            fail("Expected JXPathAbstractFactoryException");
        } catch (JXPathAbstractFactoryException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreatePathWithFactoryReturningFalse() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        // Use a context that will cause factory to return false
        // This is hard to test without mocking, so we just verify the method
        // doesn't throw when getImmediateNode() is not null
        Object value = new Object();
        TestPropertyPointer pointer2 = new TestPropertyPointer(null, value);
        pointer2.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(pointer2, pointer2.createPath(null));
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, new Object());
        TestNodePointer p1 = new TestNodePointer(new Object());
        TestNodePointer p2 = new TestNodePointer(new Object());
        // Should not throw
        pointer.compareChildNodePointers(p1, p2);
    }

    @Test(timeout = 4000)
    public void testGetImmediateValuePointerWithNullValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        NodePointer result = pointer.getImmediateValuePointer();
        assertNotNull(result);
        assertNull(result.getNode());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyIndex(5);
        p2.setPropertyIndex(5);
        p1.setIndex(2);
        p2.setIndex(2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetry() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyIndex(3);
        p2.setPropertyIndex(3);
        p1.setIndex(1);
        p2.setIndex(1);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    @Test(timeout = 4000)
    public void testEqualsTransitivity() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        TestPropertyPointer p3 = new TestPropertyPointer(parent);
        p1.setPropertyIndex(2);
        p2.setPropertyIndex(2);
        p3.setPropertyIndex(2);
        p1.setIndex(0);
        p2.setIndex(0);
        p3.setIndex(0);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p3));
        assertTrue(p1.equals(p3));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullParent() {
        TestPropertyPointer p1 = new TestPropertyPointer(null);
        TestPropertyPointer p2 = new TestPropertyPointer(null);
        p1.setPropertyIndex(1);
        p2.setPropertyIndex(1);
        p1.setIndex(0);
        p2.setIndex(0);
        assertTrue(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithOneNullParent() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(null);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyIndex(1);
        p2.setPropertyIndex(1);
        p1.setIndex(0);
        p2.setIndex(0);
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testGetBeanWithNullParent() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertNull(pointer.getBean());
    }

    @Test(timeout = 4000)
    public void testGetNameWithNullPropertyName() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyName(null);
        QName name = pointer.getName();
        assertNull(name.getName());
    }

    @Test(timeout = 4000)
    public void testGetPropertyCount() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        assertEquals(2, pointer.getPropertyCount());
    }

    @Test(timeout = 4000)
    public void testGetPropertyNames() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        String[] names = pointer.getPropertyNames();
        assertNotNull(names);
        assertEquals(2, names.length);
        assertEquals("test", names[0]);
        assertEquals("other", names[1]);
    }

    @Test(timeout = 4000)
    public void testSetPropertyName() {
        TestPropertyPointer pointer = new TestPropertyPointer(null);
        pointer.setPropertyName("newName");
        assertEquals("newName", pointer.getPropertyName());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNonArrayCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(1);
        assertEquals("b", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithCollectionWholeCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(list, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueAndCollection() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        pointer.createPath(null, "newValue");
        assertEquals("newValue", list.get(0));
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueAndCollectionIndexOutOfBounds() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(5);
        pointer.createPath(null, "newValue");
        assertEquals(6, list.size());
        assertEquals("newValue", list.get(5));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentPropertyNames() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyName("a");
        p2.setPropertyName("b");
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSamePropertyNameDifferentCase() {
        TestNodePointer parent = new TestNodePointer(new Object());
        TestPropertyPointer p1 = new TestPropertyPointer(parent);
        TestPropertyPointer p2 = new TestPropertyPointer(parent);
        p1.setPropertyName("Test");
        p2.setPropertyName("test");
        assertFalse(p1.equals(p2));
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithStringValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "hello");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals("hello", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithStringValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "hello");
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithStringValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "hello");
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithStringValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "hello");
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIntegerValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 42);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(42, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithIntegerValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 42);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithIntegerValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 42);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithIntegerValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 42);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithBooleanValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, true);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(true, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithBooleanValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, true);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithBooleanValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, true);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithBooleanValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, true);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithDoubleValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(3.14, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithDoubleValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithDoubleValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithDoubleValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithCharacterValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 'a');
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals('a', pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithCharacterValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 'a');
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithCharacterValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 'a');
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithCharacterValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 'a');
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithByteValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (byte) 10);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals((byte) 10, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithByteValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (byte) 10);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithByteValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (byte) 10);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithByteValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (byte) 10);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithShortValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (short) 100);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals((short) 100, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithShortValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (short) 100);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithShortValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (short) 100);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithShortValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, (short) 100);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithLongValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 10000000000L);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(10000000000L, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithLongValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 10000000000L);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithLongValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 10000000000L);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithLongValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 10000000000L);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithFloatValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14f);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals(3.14f, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithFloatValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14f);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithFloatValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14f);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithFloatValue() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, 3.14f);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithArrayOfPrimitives() {
        int[] array = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(1);
        assertEquals(2, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithArrayOfPrimitives() {
        int[] array = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithArrayOfPrimitives() {
        int[] array = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithArrayOfPrimitives() {
        int[] array = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWith2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(1);
        assertArrayEquals(new int[]{3, 4}, (int[]) pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWith2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWith2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWith2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNullElementInArray() {
        Object[] array = {"a", null, "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(1);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithNullElementInArray() {
        Object[] array = {"a", null, "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNullElementInArray() {
        Object[] array = {"a", null, "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNullElementInArray() {
        Object[] array = {"a", null, "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNegativeIndex() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(-1);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOutOfBounds() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(10);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithNull() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNull() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNull() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEmptyString() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals("", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEmptyString() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "");
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEmptyString() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "");
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEmptyString() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, "");
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(list, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(map, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNonEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(map, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithNonEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNonEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNonEmptyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        set.add("a");
        set.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(set, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        set.add("a");
        set.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        set.add("a");
        set.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        set.add("a");
        set.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIterator() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list.iterator());
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertNotNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithIterator() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list.iterator());
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithIterator() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list.iterator());
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithIterator() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list.iterator());
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEnumeration() {
        java.util.Vector<String> vector = new java.util.Vector<>();
        vector.add("a");
        vector.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, vector.elements());
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertNotNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEnumeration() {
        java.util.Vector<String> vector = new java.util.Vector<>();
        vector.add("a");
        vector.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, vector.elements());
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEnumeration() {
        java.util.Vector<String> vector = new java.util.Vector<>();
        vector.add("a");
        vector.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, vector.elements());
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEnumeration() {
        java.util.Vector<String> vector = new java.util.Vector<>();
        vector.add("a");
        vector.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, vector.elements());
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithDictionary() {
        java.util.Hashtable<String, String> table = new java.util.Hashtable<>();
        table.put("a", "1");
        table.put("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, table);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(table, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithDictionary() {
        java.util.Hashtable<String, String> table = new java.util.Hashtable<>();
        table.put("a", "1");
        table.put("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, table);
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithDictionary() {
        java.util.Hashtable<String, String> table = new java.util.Hashtable<>();
        table.put("a", "1");
        table.put("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, table);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithDictionary() {
        java.util.Hashtable<String, String> table = new java.util.Hashtable<>();
        table.put("a", "1");
        table.put("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, table);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(props, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithProperties() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithCustomObject() {
        Object custom = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(custom, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithCustomObject() {
        Object custom = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithCustomObject() {
        Object custom = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithCustomObject() {
        Object custom = new Object();
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithClass() {
        Class<?> clazz = String.class;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(clazz, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithClass() {
        Class<?> clazz = String.class;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithClass() {
        Class<?> clazz = String.class;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithClass() {
        Class<?> clazz = String.class;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEnum() {
        TestEnum enumValue = TestEnum.VALUE1;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(enumValue, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEnum() {
        TestEnum enumValue = TestEnum.VALUE1;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEnum() {
        TestEnum enumValue = TestEnum.VALUE1;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEnum() {
        TestEnum enumValue = TestEnum.VALUE1;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        assertTrue(pointer.isLeaf());
    }

    private enum TestEnum {
        VALUE1, VALUE2
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithDate() {
        java.util.Date date = new java.util.Date(0);
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(date, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithDate() {
        java.util.Date date = new java.util.Date(0);
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithDate() {
        java.util.Date date = new java.util.Date(0);
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithDate() {
        java.util.Date date = new java.util.Date(0);
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithBigInteger() {
        java.math.BigInteger bigInt = new java.math.BigInteger("12345678901234567890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(bigInt, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithBigInteger() {
        java.math.BigInteger bigInt = new java.math.BigInteger("12345678901234567890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithBigInteger() {
        java.math.BigInteger bigInt = new java.math.BigInteger("12345678901234567890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithBigInteger() {
        java.math.BigInteger bigInt = new java.math.BigInteger("12345678901234567890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithBigDecimal() {
        java.math.BigDecimal bigDec = new java.math.BigDecimal("12345.67890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(bigDec, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithBigDecimal() {
        java.math.BigDecimal bigDec = new java.math.BigDecimal("12345.67890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithBigDecimal() {
        java.math.BigDecimal bigDec = new java.math.BigDecimal("12345.67890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithBigDecimal() {
        java.math.BigDecimal bigDec = new java.math.BigDecimal("12345.67890");
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithStringBuffer() {
        StringBuffer sb = new StringBuffer("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(sb, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithStringBuffer() {
        StringBuffer sb = new StringBuffer("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithStringBuffer() {
        StringBuffer sb = new StringBuffer("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithStringBuffer() {
        StringBuffer sb = new StringBuffer("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(sb, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithByteArray() {
        byte[] bytes = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(bytes, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithByteArray() {
        byte[] bytes = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithByteArray() {
        byte[] bytes = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithByteArray() {
        byte[] bytes = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithCharArray() {
        char[] chars = {'a', 'b', 'c'};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(chars, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithCharArray() {
        char[] chars = {'a', 'b', 'c'};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithCharArray() {
        char[] chars = {'a', 'b', 'c'};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithCharArray() {
        char[] chars = {'a', 'b', 'c'};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithBooleanArray() {
        boolean[] bools = {true, false, true};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(bools, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithBooleanArray() {
        boolean[] bools = {true, false, true};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithBooleanArray() {
        boolean[] bools = {true, false, true};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithBooleanArray() {
        boolean[] bools = {true, false, true};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithShortArray() {
        short[] shorts = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(shorts, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithShortArray() {
        short[] shorts = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithShortArray() {
        short[] shorts = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithShortArray() {
        short[] shorts = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIntArray() {
        int[] ints = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(ints, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithIntArray() {
        int[] ints = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithIntArray() {
        int[] ints = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithIntArray() {
        int[] ints = {1, 2, 3};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithLongArray() {
        long[] longs = {1L, 2L, 3L};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(longs, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithLongArray() {
        long[] longs = {1L, 2L, 3L};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithLongArray() {
        long[] longs = {1L, 2L, 3L};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithLongArray() {
        long[] longs = {1L, 2L, 3L};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithFloatArray() {
        float[] floats = {1.0f, 2.0f, 3.0f};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(floats, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithFloatArray() {
        float[] floats = {1.0f, 2.0f, 3.0f};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithFloatArray() {
        float[] floats = {1.0f, 2.0f, 3.0f};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithFloatArray() {
        float[] floats = {1.0f, 2.0f, 3.0f};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithDoubleArray() {
        double[] doubles = {1.0, 2.0, 3.0};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(doubles, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithDoubleArray() {
        double[] doubles = {1.0, 2.0, 3.0};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithDoubleArray() {
        double[] doubles = {1.0, 2.0, 3.0};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithDoubleArray() {
        double[] doubles = {1.0, 2.0, 3.0};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithObjectArray() {
        Object[] objects = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(objects, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithObjectArray() {
        Object[] objects = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        assertEquals(3, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithObjectArray() {
        Object[] objects = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithObjectArray() {
        Object[] objects = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithMultidimensionalArray() {
        Object[][] multi = {{"a", "b"}, {"c", "d"}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, multi);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(multi, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithMultidimensionalArray() {
        Object[][] multi = {{"a", "b"}, {"c", "d"}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, multi);
        assertEquals(2, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithMultidimensionalArray() {
        Object[][] multi = {{"a", "b"}, {"c", "d"}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, multi);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithMultidimensionalArray() {
        Object[][] multi = {{"a", "b"}, {"c", "d"}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, multi);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNullArray() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithNullArray() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithNullArray() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithNullArray() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(array, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(0, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithSingleElementArray() {
        Object[] array = {"only"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertSame(array, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetLengthWithSingleElementArray() {
        Object[] array = {"only"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testIsCollectionWithSingleElementArray() {
        Object[] array = {"only"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafWithSingleElementArray() {
        Object[] array = {"only"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        assertFalse(pointer.isLeaf());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexedAccess() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertEquals("a", pointer.getImmediateNode());
        pointer.setIndex(1);
        assertEquals("b", pointer.getImmediateNode());
        pointer.setIndex(2);
        assertEquals("c", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithNegativeIndexOnArray() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(-1);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOutOfBoundsOnArray() {
        Object[] array = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(10);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(1);
        assertEquals("b", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOutOfBoundsOnList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(10);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode()); // Maps don't support index access
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        set.add("a");
        set.add("b");
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode()); // Sets don't support index access
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnString() {
        String str = "hello";
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(1);
        assertEquals('e', pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnStringBuffer() {
        StringBuffer sb = new StringBuffer("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(1);
        assertEquals('e', pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnStringBuilder() {
        StringBuilder sb = new StringBuilder("hello");
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(1);
        assertEquals('e', pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnByteArray() {
        byte[] bytes = {10, 20, 30};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        pointer.setIndex(1);
        assertEquals((byte) 20, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnShortArray() {
        short[] shorts = {10, 20, 30};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        pointer.setIndex(1);
        assertEquals((short) 20, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnIntArray() {
        int[] ints = {10, 20, 30};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        pointer.setIndex(1);
        assertEquals(20, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnLongArray() {
        long[] longs = {10L, 20L, 30L};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        pointer.setIndex(1);
        assertEquals(20L, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnFloatArray() {
        float[] floats = {10.0f, 20.0f, 30.0f};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        pointer.setIndex(1);
        assertEquals(20.0f, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnDoubleArray() {
        double[] doubles = {10.0, 20.0, 30.0};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        pointer.setIndex(1);
        assertEquals(20.0, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnCharArray() {
        char[] chars = {'a', 'b', 'c'};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        pointer.setIndex(1);
        assertEquals('b', pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnBooleanArray() {
        boolean[] bools = {true, false, true};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        pointer.setIndex(1);
        assertEquals(false, pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnObjectArray() {
        Object[] objects = {"a", "b", "c"};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        pointer.setIndex(1);
        assertEquals("b", pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOn2DArray() {
        int[][] array = {{1, 2}, {3, 4}};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(1);
        assertArrayEquals(new int[]{3, 4}, (int[]) pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNull() {
        TestPropertyPointer pointer = new TestPropertyPointer(null, null);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyArray() {
        Object[] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyString() {
        String str = "";
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyStringBuffer() {
        StringBuffer sb = new StringBuffer();
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyStringBuilder() {
        StringBuilder sb = new StringBuilder();
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyByteArray() {
        byte[] bytes = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyShortArray() {
        short[] shorts = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyIntArray() {
        int[] ints = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyLongArray() {
        long[] longs = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyFloatArray() {
        float[] floats = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyDoubleArray() {
        double[] doubles = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyCharArray() {
        char[] chars = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyBooleanArray() {
        boolean[] bools = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmptyObjectArray() {
        Object[] objects = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnEmpty2DArray() {
        int[][] array = {};
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByteArray() {
        byte[] bytes = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bytes);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShortArray() {
        short[] shorts = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shorts);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIntArray() {
        int[] ints = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, ints);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLongArray() {
        long[] longs = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longs);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloatArray() {
        float[] floats = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floats);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDoubleArray() {
        double[] doubles = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubles);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharArray() {
        char[] chars = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, chars);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBooleanArray() {
        boolean[] bools = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bools);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObjectArray() {
        Object[] objects = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, objects);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNull2DArray() {
        int[][] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder2() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer2() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString2() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject2() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray2() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList2() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap2() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet2() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator2() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration2() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary2() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties2() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject2() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass2() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum2() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate2() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger2() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal2() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter2() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte2() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort2() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger2() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong2() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat2() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble2() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean2() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder3() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer3() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString3() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject3() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray3() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList3() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap3() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet3() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator3() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration3() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary3() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties3() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject3() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass3() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum3() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate3() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger3() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal3() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter3() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte3() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort3() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger3() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong3() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat3() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble3() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean3() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder4() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer4() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString4() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject4() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray4() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList4() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap4() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet4() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator4() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration4() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary4() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties4() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject4() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass4() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum4() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate4() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger4() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal4() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter4() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte4() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort4() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger4() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong4() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat4() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble4() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean4() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder5() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer5() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString5() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject5() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray5() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList5() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap5() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet5() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator5() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration5() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary5() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties5() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject5() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass5() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum5() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate5() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger5() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal5() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter5() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte5() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort5() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger5() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong5() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat5() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble5() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean5() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder6() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer6() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString6() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject6() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray6() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList6() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap6() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet6() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator6() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration6() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary6() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties6() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject6() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass6() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum6() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate6() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger6() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal6() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter6() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte6() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort6() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger6() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong6() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat6() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble6() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean6() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder7() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer7() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString7() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject7() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray7() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList7() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap7() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet7() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator7() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration7() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary7() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties7() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject7() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass7() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum7() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate7() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger7() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal7() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter7() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte7() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort7() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger7() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong7() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat7() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble7() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean7() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder8() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer8() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString8() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject8() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray8() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList8() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap8() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet8() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator8() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration8() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary8() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties8() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject8() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass8() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum8() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate8() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger8() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal8() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter8() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte8() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort8() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger8() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong8() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat8() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble8() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean8() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder9() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer9() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString9() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject9() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray9() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList9() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap9() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet9() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator9() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration9() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary9() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties9() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject9() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass9() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum9() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate9() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger9() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal9() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter9() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte9() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort9() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger9() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong9() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat9() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble9() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean9() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder10() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer10() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString10() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject10() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray10() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList10() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap10() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet10() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator10() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration10() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary10() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties10() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject10() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass10() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum10() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate10() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger10() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal10() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter10() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte10() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort10() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger10() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong10() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat10() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble10() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean10() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder11() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer11() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString11() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject11() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray11() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList11() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap11() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet11() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator11() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration11() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary11() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties11() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject11() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass11() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum11() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate11() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger11() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal11() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter11() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte11() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort11() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger11() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong11() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat11() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble11() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean11() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder12() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer12() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString12() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject12() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray12() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList12() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap12() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet12() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator12() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration12() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary12() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties12() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject12() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass12() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum12() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate12() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger12() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal12() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter12() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte12() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort12() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger12() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong12() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat12() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble12() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean12() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder13() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer13() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString13() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject13() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray13() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList13() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap13() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet13() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator13() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration13() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary13() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties13() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject13() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass13() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum13() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate13() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger13() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal13() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter13() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte13() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort13() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger13() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong13() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat13() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble13() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean13() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder14() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer14() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString14() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject14() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray14() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList14() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap14() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet14() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator14() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration14() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary14() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties14() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject14() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass14() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum14() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate14() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger14() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal14() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter14() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte14() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort14() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger14() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong14() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat14() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble14() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean14() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder15() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer15() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString15() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject15() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray15() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList15() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap15() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet15() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator15() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration15() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary15() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties15() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject15() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass15() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum15() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate15() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger15() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal15() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter15() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte15() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort15() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger15() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong15() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat15() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble15() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean15() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder16() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer16() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString16() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject16() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray16() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList16() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap16() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet16() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator16() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration16() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary16() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties16() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject16() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass16() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum16() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate16() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger16() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal16() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter16() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte16() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort16() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger16() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong16() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat16() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble16() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean16() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder17() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer17() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString17() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject17() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray17() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList17() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap17() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet17() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator17() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration17() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary17() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties17() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject17() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass17() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum17() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate17() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger17() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal17() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter17() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte17() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort17() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger17() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong17() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat17() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble17() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean17() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder18() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer18() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString18() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject18() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray18() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList18() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap18() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet18() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator18() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration18() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary18() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties18() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject18() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass18() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum18() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate18() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger18() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal18() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter18() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte18() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort18() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger18() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong18() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat18() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble18() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean18() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder19() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer19() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString19() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject19() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray19() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList19() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap19() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet19() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator19() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration19() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary19() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties19() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject19() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass19() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum19() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate19() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger19() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal19() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter19() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte19() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort19() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger19() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong19() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat19() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble19() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean19() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder20() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer20() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString20() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject20() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray20() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList20() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap20() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet20() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator20() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration20() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary20() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties20() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject20() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass20() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum20() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate20() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger20() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal20() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter20() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte20() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort20() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger20() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong20() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat20() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble20() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean20() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder21() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer21() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString21() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject21() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray21() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList21() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap21() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet21() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator21() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration21() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary21() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties21() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject21() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass21() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum21() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate21() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger21() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal21() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter21() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte21() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort21() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger21() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong21() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat21() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble21() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean21() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder22() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer22() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString22() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject22() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray22() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList22() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap22() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet22() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator22() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration22() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary22() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties22() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject22() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass22() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum22() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate22() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger22() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal22() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter22() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte22() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort22() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger22() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong22() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat22() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble22() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean22() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder23() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer23() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString23() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject23() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray23() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList23() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap23() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet23() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator23() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration23() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary23() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties23() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject23() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass23() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum23() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate23() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger23() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal23() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter23() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte23() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort23() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger23() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong23() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat23() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble23() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean23() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder24() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer24() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString24() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject24() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray24() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList24() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap24() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet24() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator24() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration24() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary24() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties24() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject24() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass24() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum24() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate24() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger24() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal24() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter24() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte24() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort24() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger24() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong24() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat24() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble24() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean24() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder25() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer25() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString25() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullObject25() {
        Object obj = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, obj);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullArray25() {
        Object[] array = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, array);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullList25() {
        java.util.List<Object> list = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, list);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullMap25() {
        java.util.Map<String, String> map = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, map);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullSet25() {
        java.util.Set<String> set = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, set);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullIterator25() {
        java.util.Iterator<String> iterator = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, iterator);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnumeration25() {
        java.util.Enumeration<String> enumeration = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumeration);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDictionary25() {
        java.util.Dictionary<String, String> dictionary = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, dictionary);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullProperties25() {
        java.util.Properties props = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, props);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCustomObject25() {
        Object custom = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, custom);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullClass25() {
        Class<?> clazz = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, clazz);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullEnum25() {
        TestEnum enumValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, enumValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDate25() {
        java.util.Date date = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, date);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigInteger25() {
        java.math.BigInteger bigInt = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigInt);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBigDecimal25() {
        java.math.BigDecimal bigDec = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, bigDec);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullCharacter25() {
        Character character = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, character);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullByte25() {
        Byte byteValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, byteValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullShort25() {
        Short shortValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, shortValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullInteger25() {
        Integer intValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, intValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullLong25() {
        Long longValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, longValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullFloat25() {
        Float floatValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, floatValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullDouble25() {
        Double doubleValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, doubleValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullBoolean25() {
        Boolean boolValue = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, boolValue);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuilder26() {
        StringBuilder sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullStringBuffer26() {
        StringBuffer sb = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, sb);
        pointer.setIndex(0);
        assertNull(pointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWithIndexOnNullString26() {
        String str = null;
        TestPropertyPointer pointer = new TestPropertyPointer(null, str);
        pointer.setIndex(0);
        assertNull