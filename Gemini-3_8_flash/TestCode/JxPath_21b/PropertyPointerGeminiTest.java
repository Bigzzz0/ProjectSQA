package org.apache.commons.jxpath.ri.model.beans;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Class Under Test: org.apache.commons.jxpath.ri.model.beans.PropertyPointer (Defects4J JXPath-21)
 * Target Bug: JXPath151Test::testMapValueEquality / MixedModelTest::testNull
 *
 * Decision / Condition Matrix:
 * 1. getLength():
 *    - Base value is null: Buggy code returns ValueUtils.getLength(null) == 0.
 *      Contract & Specification dictate length should be 1 (scalar property holding null value).
 *      Returning 0 causes XPath value iteration ($testnull/nothing[1]) to return empty and
 *      equality comparison (<map/b != map/a>) to incorrectly evaluate to false.
 *    - Base value is collection/array: returns ValueUtils.getLength(collection).
 *    - Base value is non-null scalar: returns 1.
 * 2. setPropertyIndex(int index):
 *    - propertyIndex != index (true): propertyIndex updated, setIndex(WHOLE_COLLECTION).
 *    - propertyIndex != index (false): no state change, index preserved.
 * 3. getBean():
 *    - bean == null: calls getImmediateParentPointer().getNode().
 *    - bean != null: returns cached bean reference.
 * 4. isActual():
 *    - !isActualProperty(): returns false.
 *    - isActualProperty(): delegates to super.isActual() (bounds check).
 * 5. getImmediateNode():
 *    - value == UNINITIALIZED && index == WHOLE_COLLECTION: ValueUtils.getValue(getBaseValue()).
 *    - value == UNINITIALIZED && index != WHOLE_COLLECTION: ValueUtils.getValue(getBaseValue(), index).
 *    - value != UNINITIALIZED: returns cached value.
 * 6. isCollection():
 *    - getBaseValue() == null: returns false.
 *    - getBaseValue() is scalar: returns false.
 *    - getBaseValue() is List/Array: returns true.
 * 7. isLeaf():
 *    - getNode() == null: returns true.
 *    - getNode() is atomic type (String, Integer): returns true.
 *    - getNode() is complex JavaBean: returns false.
 * 8. createPath(JXPathContext):
 *    - getImmediateNode() != null: returns this immediately without factory call.
 *    - getImmediateNode() == null, factory returns true: creates object, returns this.
 *    - getImmediateNode() == null, factory returns false: throws JXPathAbstractFactoryException.
 * 9. createPath(JXPathContext, Object):
 *    - index != WHOLE_COLLECTION && index >= getLength(): invokes createPath(context).
 *    - else: assigns value directly and returns this.
 * 10. equals(Object):
 *    - object == this: returns true.
 *    - !(object instanceof PropertyPointer): returns false.
 *    - parent mismatch (parent == null or !parent.equals): returns false.
 *    - propertyIndex or propertyName mismatch: returns false.
 *    - normalized index match (WHOLE_COLLECTION mapped to 0): returns true / false.
 * -------------------------------------------------------------------------------------------------------
 */

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class PropertyPointerGeminiTest {

    // =========================================================================
    // Test Double Infrastructure
    // =========================================================================

    public static class ComplexBean {
        private String name = "complexBean";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TestBean {
        private String text = "hello";
        private String nothing = null;
        private ComplexBean complex = new ComplexBean();

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getNothing() {
            return nothing;
        }

        public void setNothing(String nothing) {
            this.nothing = nothing;
        }

        public ComplexBean getComplex() {
            return complex;
        }

        public void setComplex(ComplexBean complex) {
            this.complex = complex;
        }
    }

    public static class TestPropertyPointer extends PropertyPointer {
        private String propertyName = "testProp";
        private Object baseValue;
        private int propertyCount = 1;
        private String[] propertyNames = new String[]{"testProp"};
        private boolean actual = true;

        public TestPropertyPointer(NodePointer parent) {
            super(parent);
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public int getPropertyCount() {
            return propertyCount;
        }

        public void setPropertyCount(int propertyCount) {
            this.propertyCount = propertyCount;
        }

        public String[] getPropertyNames() {
            return propertyNames;
        }

        public void setPropertyNames(String[] propertyNames) {
            this.propertyNames = propertyNames;
        }

        protected boolean isActualProperty() {
            return actual;
        }

        public void setActual(boolean actual) {
            this.actual = actual;
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        public void setValue(Object value) {
            this.baseValue = value;
        }

        public String asPath() {
            return (parent == null ? "" : parent.asPath()) + "/" + propertyName;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertyIndexLifecycleAndCollectionReset() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);

        assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, pointer.getPropertyIndex());
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());

        pointer.setIndex(3);
        assertEquals(3, pointer.getIndex());

        // Changing property index triggers reset to WHOLE_COLLECTION
        pointer.setPropertyIndex(1);
        assertEquals(1, pointer.getPropertyIndex());
        assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());

        // Setting same property index does not reset index
        pointer.setIndex(5);
        pointer.setPropertyIndex(1);
        assertEquals(1, pointer.getPropertyIndex());
        assertEquals(5, pointer.getIndex());
    }

    @Test(timeout = 4000)
    public void testGetBeanCaching() {
        TestBean testBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), testBean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);

        Object bean1 = pointer.getBean();
        assertSame(testBean, bean1);

        // Verify cached reference is returned
        Object bean2 = pointer.getBean();
        assertSame(testBean, bean2);
    }

    @Test(timeout = 4000)
    public void testGetName() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setPropertyName("customField");

        QName name = pointer.getName();
        assertNotNull(name);
        assertNull(name.getPrefix());
        assertEquals("customField", name.getName());
    }

    @Test(timeout = 4000)
    public void testIsActualBranches() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);

        // Branch 1: isActualProperty returns false
        pointer.setActual(false);
        assertFalse(pointer.isActual());

        // Branch 2: isActualProperty returns true, super.isActual evaluated
        pointer.setActual(true);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertTrue(pointer.isActual());
    }

    @Test(timeout = 4000)
    public void testGetImmediateNodeWholeCollectionAndIndexedCaching() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);

        // Subcase 1: Whole collection
        TestPropertyPointer pointer1 = new TestPropertyPointer(parent);
        pointer1.setBaseValue("initialValue");
        pointer1.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals("initialValue", pointer1.getImmediateNode());
        // Verify caching: changing baseValue does not update cached value
        pointer1.setBaseValue("updatedValue");
        assertEquals("initialValue", pointer1.getImmediateNode());

        // Subcase 2: Indexed element
        TestPropertyPointer pointer2 = new TestPropertyPointer(parent);
        pointer2.setBaseValue(new String[]{"element0", "element1"});
        pointer2.setIndex(1);
        assertEquals("element1", pointer2.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testIsCollectionEvaluation() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);

        pointer.setBaseValue(null);
        assertFalse(pointer.isCollection());

        pointer.setBaseValue("scalarString");
        assertFalse(pointer.isCollection());

        pointer.setBaseValue(new String[]{"item1", "item2"});
        assertTrue(pointer.isCollection());

        List<String> list = new ArrayList<String>();
        list.add("entry");
        pointer.setBaseValue(list);
        assertTrue(pointer.isCollection());
    }

    @Test(timeout = 4000)
    public void testIsLeafEvaluation() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);

        // Null value is a leaf
        TestPropertyPointer pointerNull = new TestPropertyPointer(parent);
        pointerNull.setBaseValue(null);
        assertTrue(pointerNull.isLeaf());

        // Atomic value (String) is a leaf
        TestPropertyPointer pointerAtomic = new TestPropertyPointer(parent);
        pointerAtomic.setBaseValue("text");
        assertTrue(pointerAtomic.isLeaf());

        // Complex bean is not a leaf
        TestPropertyPointer pointerComplex = new TestPropertyPointer(parent);
        pointerComplex.setBaseValue(new ComplexBean());
        assertFalse(pointerComplex.isLeaf());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetLengthCollectionAndScalar() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);

        pointer.setBaseValue(new String[]{"a", "b", "c"});
        assertEquals(3, pointer.getLength());

        pointer.setBaseValue(new Object[0]);
        assertEquals(0, pointer.getLength());

        pointer.setBaseValue("singleScalar");
        assertEquals(1, pointer.getLength());
    }

    @Test(timeout = 4000)
    public void testGetImmediateValuePointerIntegrity() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setPropertyName("sampleProp");
        pointer.setBaseValue("sampleVal");

        NodePointer immediateValPointer = pointer.getImmediateValuePointer();
        assertNotNull(immediateValPointer);
        assertEquals(new QName(null, "sampleProp"), immediateValPointer.getName());
        assertEquals("sampleVal", immediateValPointer.getImmediateNode());
    }

    @Test(timeout = 4000)
    public void testCompareChildNodePointers() {
        TestBean bean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), bean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue(new String[]{"item0", "item1"});

        NodePointer child0 = NodePointer.newChildNodePointer(pointer.getValuePointer(), new QName("elem"), "item0");
        child0.setIndex(0);
        NodePointer child1 = NodePointer.newChildNodePointer(pointer.getValuePointer(), new QName("elem"), "item1");
        child1.setIndex(1);

        int comparison = pointer.compareChildNodePointers(child0, child1);
        assertTrue("Child 0 must precede Child 1", comparison < 0);
        assertEquals(0, pointer.compareChildNodePointers(child0, child0));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J JXPath-21)
    // =========================================================================

    /**
     * Targets JXPath Bug 21 / JXPath151Test / MixedModelTest.
     * In the defective implementation:
     *   public int getLength() { return ValueUtils.getLength(getBaseValue()); }
     * When getBaseValue() is null, ValueUtils.getLength(null) returns 0.
     * However, in JXPath, a scalar property with a null value is not an empty collection;
     * its length MUST be 1. Returning 0 breaks path evaluation for single-element index
     * access (e.g. $testnull/nothing[1]) and causes equality comparisons to fail.
     */
    @Test(timeout = 4000)
    public void testGetLengthWhenBaseValueIsNullDefect() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue(null);

        // Ground-Truth Expectation: Length of a null-valued property is 1.
        // On defective version, this returns 0 and triggers AssertionError.
        assertEquals("A property with a null value must have length 1, not 0", 1, pointer.getLength());
    }

    /**
     * End-to-end manifestation of defect: iterating over a property with null value
     * at 1-based index [1] should yield an iterator containing a single null element.
     */
    @Test(timeout = 4000)
    public void testNullPropertyIterationDefect() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("nothing", null);
        JXPathContext context = JXPathContext.newContext(map);

        Iterator<?> iterator = context.iterate("nothing[1]");
        assertTrue("Evaluating value iterator <nothing[1]> must contain 1 element", iterator.hasNext());
        assertNull("The iterated element value must be null", iterator.next());
        assertFalse("The iterator must have no more elements", iterator.hasNext());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreatePathAlreadyInitializedReturnsSelf() {
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), new TestBean(), Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue("alreadyPresent");

        JXPathContext context = JXPathContext.newContext(new TestBean());
        NodePointer result = pointer.createPath(context);
        assertSame(pointer, result);
    }

    @Test(timeout = 4000)
    public void testCreatePathUsingAbstractFactorySuccess() {
        TestBean parentBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), parentBean, Locale.US);
        final TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue(null);
        pointer.setPropertyName("text");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);

        JXPathContext context = JXPathContext.newContext(parentBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer ptr, Object parentObj, String name, int index) {
                assertEquals(0, index);
                assertEquals("text", name);
                pointer.setBaseValue("factoryCreated");
                return true;
            }
        });

        NodePointer result = pointer.createPath(context);
        assertSame(pointer, result);
    }

    @Test(expected = JXPathAbstractFactoryException.class, timeout = 4000)
    public void testCreatePathAbstractFactoryFailureThrowsException() {
        TestBean parentBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), parentBean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue(null);
        pointer.setPropertyName("uncreatable");

        JXPathContext context = JXPathContext.newContext(parentBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext ctx, NodePointer ptr, Object parentObj, String name, int index) {
                return false;
            }
        });

        pointer.createPath(context);
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testCreatePathWithoutFactoryThrowsException() {
        TestBean parentBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), parentBean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setBaseValue(null);

        JXPathContext context = JXPathContext.newContext(parentBean);
        pointer.createPath(context);
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueDirect() {
        TestBean parentBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), parentBean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);

        JXPathContext context = JXPathContext.newContext(parentBean);
        NodePointer result = pointer.createPath(context, "injectedValue");
        assertSame(pointer, result);
        assertEquals("injectedValue", pointer.getBaseValue());
    }

    @Test(timeout = 4000)
    public void testCreateChildWithAndWithoutQName() {
        TestBean parentBean = new TestBean();
        NodePointer parent = NodePointer.newNodePointer(new QName("parent"), parentBean, Locale.US);
        TestPropertyPointer pointer = new TestPropertyPointer(parent);
        pointer.setPropertyName("originalProp");

        JXPathContext context = JXPathContext.newContext(parentBean);

        // Case 1: createChild with explicit QName
        NodePointer childWithName = pointer.createChild(context, new QName("childProp"), 2, "childVal");
        assertNotSame(pointer, childWithName);
        assertEquals("childProp", childWithName.getName().getName());
        assertEquals(2, childWithName.getIndex());

        // Case 2: createChild with null QName keeps existing property name
        NodePointer childNullName = pointer.createChild(context, null, 1, "childVal2");
        assertEquals("originalProp", childNullName.getName().getName());
        assertEquals(1, childNullName.getIndex());

        // Case 3: createChild without value overload
        pointer.setBaseValue("existing");
        NodePointer childWithoutValue = pointer.createChild(context, new QName("childProp3"), 0);
        assertEquals("childProp3", childWithoutValue.getName().getName());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeExhaustiveBranches() {
        TestBean bean1 = new TestBean();
        NodePointer p1 = NodePointer.newNodePointer(new QName("root"), bean1, Locale.US);
        NodePointer p2 = NodePointer.newNodePointer(new QName("root"), bean1, Locale.US);
        NodePointer pDifferent = NodePointer.newNodePointer(new QName("different"), bean1, Locale.US);

        TestPropertyPointer tp1 = new TestPropertyPointer(p1);
        tp1.setPropertyIndex(0);
        tp1.setPropertyName("propA");
        tp1.setIndex(0);

        // 1. Identity
        assertTrue(tp1.equals(tp1));

        // 2. Null and Incompatible Type
        assertFalse(tp1.equals(null));
        assertFalse(tp1.equals("nonPropertyPointer"));

        // 3. Parent comparison branches
        TestPropertyPointer tpNullParent = new TestPropertyPointer(null);
        tpNullParent.setPropertyIndex(0);
        tpNullParent.setPropertyName("propA");
        assertFalse(tp1.equals(tpNullParent));
        assertFalse(tpNullParent.equals(tp1));

        TestPropertyPointer tpDifferentParent = new TestPropertyPointer(pDifferent);
        tpDifferentParent.setPropertyIndex(0);
        tpDifferentParent.setPropertyName("propA");
        assertFalse(tp1.equals(tpDifferentParent));

        // Equal parent instances
        TestPropertyPointer tp2 = new TestPropertyPointer(p2);
        tp2.setPropertyIndex(0);
        tp2.setPropertyName("propA");
        tp2.setIndex(0);
        assertTrue(tp1.equals(tp2));
        assertEquals(tp1.hashCode(), tp2.hashCode());

        // 4. PropertyIndex mismatch
        tp2.setPropertyIndex(1);
        assertFalse(tp1.equals(tp2));
        tp2.setPropertyIndex(0);

        // 5. PropertyName mismatch
        tp2.setPropertyName("propB");
        assertFalse(tp1.equals(tp2));
        tp2.setPropertyName("propA");

        // 6. Index normalization: WHOLE_COLLECTION (-1) normalizes to 0, matching index 0
        tp1.setIndex(NodePointer.WHOLE_COLLECTION);
        tp2.setIndex(0);
        assertTrue(tp1.equals(tp2));

        // 7. Distinct non-zero indexes
        tp1.setIndex(1);
        tp2.setIndex(2);
        assertFalse(tp1.equals(tp2));
    }
}