/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.model.beans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer
 *
 * Branch & Coverage Matrix:
 * 1. getName() -> QName initialized with propertyName (default "*", custom string).
 * 2. setPropertyIndex(int) -> Empty body / no-op verification.
 * 3. getLength(), getBaseValue(), getImmediateNode(), isLeaf(), isActual(), isActualProperty(), isContainer()
 *    -> Fixed contract validation: 0, null, null, true, false, false, true.
 * 4. getValuePointer() -> NullPointer wrapping this pointer with QName(propertyName).
 * 5. setValue(Object):
 *    - Branch A: parent == null -> JXPathInvalidAccessException ("the target object is null").
 *    - Branch B: parent != null && parent.isContainer() -> JXPathInvalidAccessException ("the target object is null").
 *    - Branch C: parent instanceof PropertyOwnerPointer && isDynamicPropertyDeclarationSupported() == true
 *      -> PropertyPointer retrieved, propertyName set, setValue forwarded.
 *    - Branch D: parent instanceof PropertyOwnerPointer && isDynamicPropertyDeclarationSupported() == false
 *      -> JXPathInvalidAccessException ("path does not match a changeable location").
 *    - Branch E: parent not PropertyOwnerPointer && !parent.isContainer()
 *      -> JXPathInvalidAccessException ("path does not match a changeable location").
 * 6. createPath(JXPathContext):
 *    - Branch A: isAttribute() == true -> returns newParent.createAttribute(context, getName()).
 *    - Branch B: isAttribute() == false && newParent instanceof PropertyOwnerPointer
 *      -> unwraps pop.getPropertyPointer() and calls createChild(context, name, index).
 *    - Branch C: isAttribute() == false && !(newParent instanceof PropertyOwnerPointer)
 *      -> calls newParent.createChild(context, name, index) directly.
 * 7. createPath(JXPathContext, Object value):
 *    - Branch A: isAttribute() == true -> creates attribute, sets value, returns pointer.
 *    - Branch B: isAttribute() == false && newParent instanceof PropertyOwnerPointer
 *      -> unwraps pop.getPropertyPointer() and calls createChild(context, name, index, value).
 *    - Branch C: isAttribute() == false && !(newParent instanceof PropertyOwnerPointer)
 *      -> calls newParent.createChild(context, name, index, value) directly.
 * 8. createChild(context, name, index) and createChild(context, name, index, value):
 *    - Validates delegation to createPath(context).createChild(...).
 * 9. isCollection():
 *    - Branch A: index == WHOLE_COLLECTION (-1) -> false.
 *    - Branch B: index != WHOLE_COLLECTION -> true.
 * 10. asPath():
 *    - Branch A: !byNameAttribute -> delegates to super.asPath().
 *    - Branch B: byNameAttribute == true:
 *      - Escape single quotes (') -> &apos;
 *      - Escape double quotes (") -> &quot;
 *      - Whole collection vs specific index ([index + 1]).
 *
 * Known Defect Target:
 * - BadlyImplementedFactoryTest::testBadFactoryImplementation
 *   When AbstractFactory returns false during path creation involving null properties,
 *   NullPropertyPointer navigation must properly terminate and surface a JXPathException
 *   rather than failing assertions or looping indefinitely.
 */
public class NullPropertyPointerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultStateAndProperties() {
        MockNodePointer parent = new MockNodePointer("/root");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        assertEquals("*", npp.getPropertyName());
        assertEquals(new QName("*"), npp.getName());
        assertEquals(0, npp.getLength());
        assertNull(npp.getBaseValue());
        assertNull(npp.getImmediateNode());
        assertTrue(npp.isLeaf());
        assertFalse(npp.isActual());
        assertFalse(npp.isActualProperty());
        assertTrue(npp.isContainer());
        assertEquals(0, npp.getPropertyCount());
        assertNotNull(npp.getPropertyNames());
        assertEquals(0, npp.getPropertyNames().length);

        // setPropertyIndex is a no-op
        npp.setPropertyIndex(99);
        assertEquals(0, npp.getLength());
    }

    @Test(timeout = 4000)
    public void testSetPropertyNameAndValuePointer() {
        MockNodePointer parent = new MockNodePointer("/root");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        npp.setPropertyName("customProp");
        assertEquals("customProp", npp.getPropertyName());
        assertEquals(new QName("customProp"), npp.getName());

        NodePointer valuePointer = npp.getValuePointer();
        assertNotNull(valuePointer);
        assertTrue(valuePointer instanceof NullPointer);
        assertEquals(new QName("customProp"), valuePointer.getName());
        assertSame(npp, valuePointer.getImmediateParentPointer());
    }

    @Test(timeout = 4000)
    public void testIsCollectionEquivalence() {
        MockNodePointer parent = new MockNodePointer("/root");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        assertEquals(NodePointer.WHOLE_COLLECTION, npp.getIndex());
        assertFalse(npp.isCollection());

        npp.setIndex(0);
        assertTrue(npp.isCollection());

        npp.setIndex(5);
        assertTrue(npp.isCollection());

        npp.setIndex(NodePointer.WHOLE_COLLECTION);
        assertFalse(npp.isCollection());
    }

    @Test(timeout = 4000)
    public void testSetValueDynamicPropertyOwnerSupported() {
        RecordingPropertyPointer recordingPP = new RecordingPropertyPointer();
        DynamicPropertyOwnerPointerStub dynamicParent = new DynamicPropertyOwnerPointerStub(recordingPP, false);
        NullPropertyPointer npp = new NullPropertyPointer(dynamicParent);

        npp.setPropertyName("activeField");
        npp.setValue("expectedData");

        assertEquals("activeField", recordingPP.capturedName);
        assertEquals("expectedData", recordingPP.capturedValue);
    }

    @Test(timeout = 4000)
    public void testCreatePathAttribute() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer expectedAttr = new MockNodePointer("/root/@myAttr");
        parent.createAttributeResult = expectedAttr;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setAttribute(true);
        npp.setPropertyName("myAttr");

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context);

        assertSame(expectedAttr, result);
        assertEquals(new QName("myAttr"), parent.capturedName);
    }

    @Test(timeout = 4000)
    public void testCreatePathAttributeWithValue() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer expectedAttr = new MockNodePointer("/root/@valAttr");
        parent.createAttributeResult = expectedAttr;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setAttribute(true);
        npp.setPropertyName("valAttr");

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context, "attrContent");

        assertSame(expectedAttr, result);
        assertEquals(new QName("valAttr"), parent.capturedName);
        assertEquals("attrContent", expectedAttr.capturedValue);
    }

    @Test(timeout = 4000)
    public void testCreatePathNonAttributeWithNonPropertyOwnerParent() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer expectedChild = new MockNodePointer("/root/elem[2]");
        parent.createChildResult = expectedChild;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setAttribute(false);
        npp.setPropertyName("elem");
        npp.setIndex(1);

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context);

        assertSame(expectedChild, result);
        assertEquals(new QName("elem"), parent.capturedName);
        assertEquals(1, parent.capturedIndex);
    }

    @Test(timeout = 4000)
    public void testCreatePathNonAttributeWithValueNonPropertyOwnerParent() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer expectedChild = new MockNodePointer("/root/elem[3]");
        parent.createChildResult = expectedChild;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setAttribute(false);
        npp.setPropertyName("elem");
        npp.setIndex(2);

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context, "childValue");

        assertSame(expectedChild, result);
        assertEquals(new QName("elem"), parent.capturedName);
        assertEquals(2, parent.capturedIndex);
        assertEquals("childValue", parent.capturedValue);
    }

    @Test(timeout = 4000)
    public void testCreatePathWithPropertyOwnerParent() {
        RecordingPropertyPointer propertyPointer = new RecordingPropertyPointer();
        MockNodePointer expectedChild = new MockNodePointer("/root/child");
        propertyPointer.createChildResult = expectedChild;

        DynamicPropertyOwnerPointerStub pop = new DynamicPropertyOwnerPointerStub(propertyPointer, false);
        NullPropertyPointer npp = new NullPropertyPointer(pop);
        npp.setAttribute(false);
        npp.setPropertyName("child");
        npp.setIndex(0);

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context);

        assertSame(expectedChild, result);
        assertEquals(new QName("child"), propertyPointer.capturedName);
        assertEquals(0, propertyPointer.capturedIndex);
    }

    @Test(timeout = 4000)
    public void testCreatePathWithValueWithPropertyOwnerParent() {
        RecordingPropertyPointer propertyPointer = new RecordingPropertyPointer();
        MockNodePointer expectedChild = new MockNodePointer("/root/child");
        propertyPointer.createChildResult = expectedChild;

        DynamicPropertyOwnerPointerStub pop = new DynamicPropertyOwnerPointerStub(propertyPointer, false);
        NullPropertyPointer npp = new NullPropertyPointer(pop);
        npp.setAttribute(false);
        npp.setPropertyName("child");
        npp.setIndex(3);

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createPath(context, "assignedValue");

        assertSame(expectedChild, result);
        assertEquals(new QName("child"), propertyPointer.capturedName);
        assertEquals(3, propertyPointer.capturedIndex);
        assertEquals("assignedValue", propertyPointer.capturedValue);
    }

    @Test(timeout = 4000)
    public void testCreateChildDelegation() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer intermediateChild = new MockNodePointer("/root/mid");
        MockNodePointer ultimateChild = new MockNodePointer("/root/mid/sub");
        parent.createChildResult = intermediateChild;
        intermediateChild.createChildResult = ultimateChild;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("mid");

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createChild(context, new QName("sub"), 4);

        assertSame(ultimateChild, result);
        assertEquals(new QName("sub"), intermediateChild.capturedName);
        assertEquals(4, intermediateChild.capturedIndex);
    }

    @Test(timeout = 4000)
    public void testCreateChildWithValueDelegation() {
        MockNodePointer parent = new MockNodePointer("/root");
        MockNodePointer intermediateChild = new MockNodePointer("/root/mid");
        MockNodePointer ultimateChild = new MockNodePointer("/root/mid/sub");
        parent.createChildResult = intermediateChild;
        intermediateChild.createChildResult = ultimateChild;

        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("mid");

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = npp.createChild(context, new QName("sub"), 7, "finalPayload");

        assertSame(ultimateChild, result);
        assertEquals(new QName("sub"), intermediateChild.capturedName);
        assertEquals(7, intermediateChild.capturedIndex);
        assertEquals("finalPayload", intermediateChild.capturedValue);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Path Escaping
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsPathStandardDelegation() {
        MockNodePointer parent = new MockNodePointer("/parent");
        NullPropertyPointer npp = new NullPropertyPointer(parent);
        npp.setPropertyName("plainProperty");

        assertEquals("/parent/plainProperty", npp.asPath());

        npp.setIndex(2);
        assertEquals("/parent/plainProperty[3]", npp.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathByNameAttributeEscaping() {
        MockNodePointer parent = new MockNodePointer("/context");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        // Simple name
        npp.setNameAttributeValue("simple");
        assertEquals("/context[@name='simple']", npp.asPath());

        // Single quote escaping
        npp.setNameAttributeValue("single'quote");
        assertEquals("/context[@name='single&apos;quote']", npp.asPath());

        // Multiple single quotes
        npp.setNameAttributeValue("a'b'c'");
        assertEquals("/context[@name='a&apos;b&apos;c&apos;']", npp.asPath());

        // Double quote escaping
        npp.setNameAttributeValue("double\"quote");
        assertEquals("/context[@name='double&quot;quote']", npp.asPath());

        // Multiple double quotes
        npp.setNameAttributeValue("\"a\"b\"");
        assertEquals("/context[@name='&quot;a&quot;b&quot;']", npp.asPath());

        // Mixed single and double quotes
        npp.setNameAttributeValue("'mixed\"quotes'");
        assertEquals("/context[@name='&apos;mixed&quot;quotes&apos;']", npp.asPath());

        // Empty string
        npp.setNameAttributeValue("");
        assertEquals("/context[@name='']", npp.asPath());
    }

    @Test(timeout = 4000)
    public void testAsPathByNameAttributeWithIndex() {
        MockNodePointer parent = new MockNodePointer("/container");
        NullPropertyPointer npp = new NullPropertyPointer(parent);

        npp.setNameAttributeValue("entry");
        npp.setIndex(0);
        assertEquals("/container[@name='entry'][1]", npp.asPath());

        npp.setIndex(10);
        assertEquals("/container[@name='entry'][11]", npp.asPath());

        npp.setIndex(NodePointer.WHOLE_COLLECTION);
        assertEquals("/container[@name='entry']", npp.asPath());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (BadlyImplementedFactoryTest)
    // =========================================================================

    public static class ComplexBean {
        private ComplexNestedBean nestedBean;

        public ComplexNestedBean getNestedBean() {
            return nestedBean;
        }

        public void setNestedBean(ComplexNestedBean nestedBean) {
            this.nestedBean = nestedBean;
        }
    }

    public static class ComplexNestedBean {
        private String[] strings;

        public String[] getStrings() {
            return strings;
        }

        public void setStrings(String[] strings) {
            this.strings = strings;
        }
    }

    @Test(timeout = 4000)
    public void testBadFactoryImplementationThrowsException() {
        ComplexBean bean = new ComplexBean();
        JXPathContext context = JXPathContext.newContext(bean);

        context.setFactory(new AbstractFactory() {
            public boolean createObject(
                    JXPathContext ctx,
                    Pointer pointer,
                    Object parent,
                    String name,
                    int index) {
                return false;
            }
        });

        try {
            context.createPath("nestedBean/strings[2]");
            fail("Expected JXPathException when AbstractFactory returns false for intermediate path");
        } catch (JXPathException expected) {
            // Success: Exception is required per contract
        }
    }

    @Test(timeout = 4000)
    public void testBadFactoryImplementationOnSetValue() {
        ComplexBean bean = new ComplexBean();
        JXPathContext context = JXPathContext.newContext(bean);

        context.setFactory(new AbstractFactory() {
            public boolean createObject(
                    JXPathContext ctx,
                    Pointer pointer,
                    Object parent,
                    String name,
                    int index) {
                return false;
            }
        });

        try {
            context.createPathAndSetValue("nestedBean/strings[1]", "failureTarget");
            fail("Expected JXPathException when AbstractFactory returns false during createPathAndSetValue");
        } catch (JXPathException expected) {
            // Success: Path cannot be created when factory rejects object creation
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetValueThrowsWhenParentIsNull() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setPropertyName("field");

        try {
            npp.setValue("value");
            fail("Expected JXPathInvalidAccessException when parent is null");
        } catch (JXPathInvalidAccessException ex) {
            assertTrue(ex.getMessage().contains("the target object is null"));
        }
    }

    @Test(timeout = 4000)
    public void testSetValueThrowsWhenParentIsContainer() {
        NullPointer containerParent = new NullPointer(Locale.getDefault());
        assertTrue(containerParent.isContainer());

        NullPropertyPointer npp = new NullPropertyPointer(containerParent);
        npp.setPropertyName("field");

        try {
            npp.setValue("value");
            fail("Expected JXPathInvalidAccessException when parent is a container");
        } catch (JXPathInvalidAccessException ex) {
            assertTrue(ex.getMessage().contains("the target object is null"));
        }
    }

    @Test(timeout = 4000)
    public void testSetValueThrowsWhenParentDoesNotSupportDynamicProperties() {
        RecordingPropertyPointer recordingPP = new RecordingPropertyPointer();
        NonDynamicPropertyOwnerStub nonDynamicParent = new NonDynamicPropertyOwnerStub(recordingPP);

        NullPropertyPointer npp = new NullPropertyPointer(nonDynamicParent);
        npp.setPropertyName("immutableField");

        try {
            npp.setValue("testValue");
            fail("Expected JXPathInvalidAccessException when dynamic property declaration is unsupported");
        } catch (JXPathInvalidAccessException ex) {
            assertTrue(ex.getMessage().contains("path does not match a changeable location"));
        }
    }

    @Test(timeout = 4000)
    public void testSetValueThrowsWhenParentIsNotPropertyOwnerPointer() {
        MockNodePointer plainParent = new MockNodePointer("/plain");

        NullPropertyPointer npp = new NullPropertyPointer(plainParent);
        npp.setPropertyName("nonOwnerField");

        try {
            npp.setValue("testValue");
            fail("Expected JXPathInvalidAccessException when parent is not a PropertyOwnerPointer");
        } catch (JXPathInvalidAccessException ex) {
            assertTrue(ex.getMessage().contains("path does not match a changeable location"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Structural Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullPropertyPointerHierarchy() {
        MockNodePointer root = new MockNodePointer("/base");
        NullPropertyPointer npp = new NullPropertyPointer(root);

        assertSame(root, npp.getParent());
        assertSame(root, npp.getImmediateParentPointer());
        assertFalse(npp.isRoot());
    }

    // =========================================================================
    // Test Doubles & Helper Stubs
    // =========================================================================

    private static class MockNodePointer extends NodePointer {
        NodePointer createPathResult;
        NodePointer createAttributeResult;
        NodePointer createChildResult;
        QName capturedName;
        int capturedIndex;
        Object capturedValue;
        private final String path;

        MockNodePointer(String path) {
            super(null, Locale.getDefault());
            this.path = path;
        }

        public NodePointer createPath(JXPathContext context) {
            return createPathResult != null ? createPathResult : this;
        }

        public NodePointer createAttribute(JXPathContext context, QName name) {
            this.capturedName = name;
            return createAttributeResult != null ? createAttributeResult : this;
        }

        public NodePointer createChild(JXPathContext context, QName name, int index) {
            this.capturedName = name;
            this.capturedIndex = index;
            return createChildResult != null ? createChildResult : this;
        }

        public NodePointer createChild(JXPathContext context, QName name, int index, Object value) {
            this.capturedName = name;
            this.capturedIndex = index;
            this.capturedValue = value;
            return createChildResult != null ? createChildResult : this;
        }

        public void setValue(Object value) {
            this.capturedValue = value;
        }

        public boolean isLeaf() { return false; }
        public boolean isCollection() { return false; }
        public int getLength() { return 1; }
        public QName getName() { return new QName("mock"); }
        public Object getBaseValue() { return null; }
        public Object getImmediateNode() { return null; }
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) { return 0; }
        public String asPath() { return path != null ? path : "/mock"; }
    }

    private static class DynamicPropertyOwnerPointerStub extends PropertyOwnerPointer {
        private final PropertyPointer propertyPointer;
        private final boolean containerFlag;

        DynamicPropertyOwnerPointerStub(PropertyPointer pp, boolean container) {
            super(null, Locale.getDefault());
            this.propertyPointer = pp;
            this.containerFlag = container;
        }

        public boolean isDynamicPropertyDeclarationSupported() {
            return true;
        }

        public PropertyPointer getPropertyPointer() {
            return propertyPointer;
        }

        public boolean isContainer() {
            return containerFlag;
        }

        public NodePointer createPath(JXPathContext context) {
            return this;
        }

        public QName getName() { return new QName("dynamicStub"); }
        public Object getBaseValue() { return null; }
        public Object getImmediateNode() { return null; }
        public boolean isLeaf() { return false; }
        public boolean isCollection() { return false; }
        public int getLength() { return 1; }
        public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }
        public String asPath() { return "/dynamicStub"; }
    }

    private static class NonDynamicPropertyOwnerStub extends PropertyOwnerPointer {
        private final PropertyPointer propertyPointer;

        NonDynamicPropertyOwnerStub(PropertyPointer pp) {
            super(null, Locale.getDefault());
            this.propertyPointer = pp;
        }

        public boolean isDynamicPropertyDeclarationSupported() {
            return false;
        }

        public PropertyPointer getPropertyPointer() {
            return propertyPointer;
        }

        public boolean isContainer() {
            return false;
        }

        public QName getName() { return new QName("nonDynamicStub"); }
        public Object getBaseValue() { return null; }
        public Object getImmediateNode() { return null; }
        public boolean isLeaf() { return false; }
        public boolean isCollection() { return false; }
        public int getLength() { return 1; }
        public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }
        public String asPath() { return "/nonDynamicStub"; }
    }

    private static class RecordingPropertyPointer extends PropertyPointer {
        String capturedName;
        Object capturedValue;
        int capturedIndex;
        NodePointer createChildResult;

        RecordingPropertyPointer() {
            super(null);
        }

        public int getPropertyCount() { return 1; }
        public String[] getPropertyNames() { return new String[]{"field"}; }
        protected boolean isActualProperty() { return true; }
        public void setPropertyName(String name) { this.capturedName = name; }
        public String getPropertyName() { return capturedName; }
        public void setPropertyIndex(int index) { this.capturedIndex = index; }
        public int getLength() { return 1; }
        public Object getBaseValue() { return null; }
        public Object getImmediateNode() { return null; }
        public void setValue(Object value) { this.capturedValue = value; }
        public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }

        public NodePointer createChild(JXPathContext context, QName name, int index) {
            this.capturedName = name != null ? name.getName() : null;
            this.capturedIndex = index;
            return createChildResult != null ? createChildResult : this;
        }

        public NodePointer createChild(JXPathContext context, QName name, int index, Object value) {
            this.capturedName = name != null ? name.getName() : null;
            this.capturedIndex = index;
            this.capturedValue = value;
            return createChildResult != null ? createChildResult : this;
        }
    }
}