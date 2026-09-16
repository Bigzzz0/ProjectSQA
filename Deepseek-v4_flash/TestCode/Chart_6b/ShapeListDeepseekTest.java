package org.jfree.chart.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ShapeList extends AbstractObjectList
 * 
 * Decision branches in ShapeList:
 * 1. getShape(int) - delegates to get(index), null vs non-null return
 * 2. setShape(int, Shape) - delegates to set(index, shape), null shape allowed
 * 3. clone() - delegates to super.clone(), CloneNotSupportedException path
 * 4. equals(Object):
 *    - obj == this -> true
 *    - obj instanceof ShapeList -> false
 *    - super.equals(obj) -> true/false (delegates to AbstractObjectList)
 * 5. hashCode() - delegates to super.hashCode()
 * 6. writeObject/readObject serialization:
 *    - null shape vs non-null shape
 *    - index -1 sentinel for null
 *    - sparse indices (non-contiguous)
 *    - empty list
 * 
 * Known defect (from Defects4J):
 * - testSerialization: ShapeList@cef18a3f vs ShapeList@e657ea8d
 *   -> Serialization round-trip produces unequal object (equals() fails)
 * - testEquals: AssertionFailedError
 *   -> equals() fails for equal ShapeList objects
 * 
 * Root cause hypothesis: 
 * - equals() in AbstractObjectList may not properly compare Shape objects
 *   (Shape interface lacks equals() contract enforcement)
 * - Serialization may lose shape data or produce different object identity
 * 
 * Boundary conditions:
 * - Empty list
 * - Single element at index 0
 * - Sparse indices (0, 5, 10)
 * - Null shapes
 * - Different Shape implementations (Ellipse2D, Line2D, Rectangle2D)
 * - Large index values
 * - Negative index (should throw IndexOutOfBoundsException)
 * - Clone with non-cloneable shapes
 * - Serialization with null and non-null shapes
 */
public class ShapeListDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testGetShape_EmptyList_ReturnsNull() {
        ShapeList list = new ShapeList();
        assertNull("getShape on empty list should return null", list.getShape(0));
    }

    @Test(timeout = 4000)
    public void testSetShape_GetShape_RoundTrip() {
        ShapeList list = new ShapeList();
        Shape shape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        list.setShape(0, shape);
        Shape result = list.getShape(0);
        assertNotNull("getShape should return non-null after set", result);
        assertEquals("Shape should be equal after round-trip", shape, result);
        assertSame("Shape should be same object reference", shape, result);
    }

    @Test(timeout = 4000)
    public void testSetShape_NullShape_GetShapeReturnsNull() {
        ShapeList list = new ShapeList();
        list.setShape(0, null);
        assertNull("getShape should return null for null shape", list.getShape(0));
    }

    @Test(timeout = 4000)
    public void testSetShape_SparseIndices() {
        ShapeList list = new ShapeList();
        Shape shape1 = new Ellipse2D.Double(0, 0, 10, 10);
        Shape shape2 = new Line2D.Double(0, 0, 5, 5);
        list.setShape(0, shape1);
        list.setShape(5, shape2);
        list.setShape(10, null);
        
        assertEquals("Index 0 should have shape1", shape1, list.getShape(0));
        assertEquals("Index 5 should have shape2", shape2, list.getShape(5));
        assertNull("Index 10 should be null", list.getShape(10));
        assertNull("Index 3 should be null (gap)", list.getShape(3));
    }

    @Test(timeout = 4000)
    public void testSetShape_OverwriteExisting() {
        ShapeList list = new ShapeList();
        Shape original = new Rectangle2D.Double(1, 1, 1, 1);
        Shape replacement = new Ellipse2D.Double(2, 2, 2, 2);
        list.setShape(0, original);
        list.setShape(0, replacement);
        assertEquals("Shape should be replaced", replacement, list.getShape(0));
        assertNotSame("Should not return original", original, list.getShape(0));
    }

    /* ========== Partition B: Boundary Value Analysis (BVA) & Extremes ========== */

    @Test(timeout = 4000)
    public void testGetShape_NegativeIndex_ThrowsIndexOutOfBounds() {
        ShapeList list = new ShapeList();
        try {
            list.getShape(-1);
            fail("Expected IndexOutOfBoundsException for negative index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetShape_NegativeIndex_ThrowsIndexOutOfBounds() {
        ShapeList list = new ShapeList();
        try {
            list.setShape(-1, new Rectangle2D.Double());
            fail("Expected IndexOutOfBoundsException for negative index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetShape_LargeIndex_ReturnsNull() {
        ShapeList list = new ShapeList();
        assertNull("Large index should return null", list.getShape(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testSetShape_LargeIndex_ThenGet() {
        ShapeList list = new ShapeList();
        Shape shape = new Line2D.Double(0, 0, 100, 100);
        list.setShape(1000, shape);
        assertEquals("Shape at large index", shape, list.getShape(1000));
        assertNull("Intermediate index should be null", list.getShape(500));
    }

    @Test(timeout = 4000)
    public void testSetShape_MaxIntIndex() {
        ShapeList list = new ShapeList();
        Shape shape = new Ellipse2D.Double(0, 0, 1, 1);
        list.setShape(Integer.MAX_VALUE, shape);
        assertEquals("Shape at MAX_VALUE index", shape, list.getShape(Integer.MAX_VALUE));
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * Targets the known serialization defect.
     * The bug causes equals() to fail after serialization round-trip.
     * This test verifies that a serialized and deserialized ShapeList
     * is equal to the original.
     */
    @Test(timeout = 4000)
    public void testSerialization_RoundTrip_Equals() throws Exception {
        ShapeList original = new ShapeList();
        original.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        original.setShape(1, new Ellipse2D.Double(5.0, 6.0, 7.0, 8.0));
        original.setShape(2, null);
        original.setShape(5, new Line2D.Double(0, 0, 10, 10));

        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();

        // The defect: equals() fails after serialization
        assertEquals("Serialized ShapeList should equal original", original, deserialized);
        assertEquals("Hash codes should match", original.hashCode(), deserialized.hashCode());
        
        // Verify contents
        assertEquals("Shape at index 0", original.getShape(0), deserialized.getShape(0));
        assertEquals("Shape at index 1", original.getShape(1), deserialized.getShape(1));
        assertNull("Null shape at index 2", deserialized.getShape(2));
        assertEquals("Shape at index 5", original.getShape(5), deserialized.getShape(5));
    }

    /**
     * Targets the known equals() defect.
     * Two ShapeLists with identical shapes should be equal.
     */
    @Test(timeout = 4000)
    public void testEquals_IdenticalLists_ShouldBeEqual() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        
        Shape shape1 = new Rectangle2D.Double(1, 2, 3, 4);
        Shape shape2 = new Ellipse2D.Double(5, 6, 7, 8);
        
        list1.setShape(0, shape1);
        list1.setShape(1, shape2);
        list2.setShape(0, shape1);
        list2.setShape(1, shape2);
        
        // The defect: equals() fails for identical lists
        assertEquals("Identical ShapeLists should be equal", list1, list2);
        assertEquals("Hash codes should match", list1.hashCode(), list2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentShapes_ShouldNotBeEqual() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        
        list1.setShape(0, new Rectangle2D.Double(1, 1, 1, 1));
        list2.setShape(0, new Rectangle2D.Double(2, 2, 2, 2));
        
        assertNotEquals("Different shapes should not be equal", list1, list2);
    }

    @Test(timeout = 4000)
    public void testEquals_SameReference_ShouldBeEqual() {
        ShapeList list = new ShapeList();
        list.setShape(0, new Ellipse2D.Double());
        assertEquals("Same reference should be equal", list, list);
    }

    @Test(timeout = 4000)
    public void testEquals_NullObject_ShouldNotBeEqual() {
        ShapeList list = new ShapeList();
        assertFalse("Null should not be equal", list.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentType_ShouldNotBeEqual() {
        ShapeList list = new ShapeList();
        Object obj = new Object();
        assertFalse("Different type should not be equal", list.equals(obj));
    }

    @Test(timeout = 4000)
    public void testEquals_EmptyLists_ShouldBeEqual() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        assertEquals("Empty lists should be equal", list1, list2);
        assertEquals("Empty list hash codes should match", list1.hashCode(), list2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentSizes_ShouldNotBeEqual() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double());
        assertNotEquals("Different sizes should not be equal", list1, list2);
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(timeout = 4000)
    public void testClone_WithCloneableShapes() throws CloneNotSupportedException {
        ShapeList list = new ShapeList();
        list.setShape(0, new Rectangle2D.Double(1, 2, 3, 4));
        list.setShape(1, new Ellipse2D.Double(5, 6, 7, 8));
        
        ShapeList cloned = (ShapeList) list.clone();
        assertNotNull("Clone should not be null", cloned);
        assertNotSame("Clone should be different object", list, cloned);
        assertEquals("Clone should be equal to original", list, cloned);
        assertEquals("Clone hash code should match", list.hashCode(), cloned.hashCode());
        
        // Verify contents are equal
        assertEquals("Shape at index 0", list.getShape(0), cloned.getShape(0));
        assertEquals("Shape at index 1", list.getShape(1), cloned.getShape(1));
    }

    @Test(timeout = 4000)
    public void testClone_EmptyList() throws CloneNotSupportedException {
        ShapeList list = new ShapeList();
        ShapeList cloned = (ShapeList) list.clone();
        assertNotNull("Clone should not be null", cloned);
        assertEquals("Empty clone should be equal", list, cloned);
    }

    @Test(timeout = 4000)
    public void testClone_WithNullShapes() throws CloneNotSupportedException {
        ShapeList list = new ShapeList();
        list.setShape(0, null);
        list.setShape(1, new Line2D.Double(0, 0, 1, 1));
        
        ShapeList cloned = (ShapeList) list.clone();
        assertNull("Null shape should remain null in clone", cloned.getShape(0));
        assertEquals("Non-null shape should be equal", list.getShape(1), cloned.getShape(1));
    }

    @Test(timeout = 4000)
    public void testSerialization_EmptyList() throws Exception {
        ShapeList original = new ShapeList();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();
        
        assertEquals("Empty list serialization should preserve equality", original, deserialized);
    }

    @Test(timeout = 4000)
    public void testSerialization_AllNullShapes() throws Exception {
        ShapeList original = new ShapeList();
        original.setShape(0, null);
        original.setShape(1, null);
        original.setShape(2, null);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();
        
        assertEquals("All-null list serialization should preserve equality", original, deserialized);
        assertNull("Index 0 should be null", deserialized.getShape(0));
        assertNull("Index 1 should be null", deserialized.getShape(1));
        assertNull("Index 2 should be null", deserialized.getShape(2));
    }

    @Test(timeout = 4000)
    public void testSerialization_SparseIndices() throws Exception {
        ShapeList original = new ShapeList();
        original.setShape(0, new Rectangle2D.Double(1, 1, 1, 1));
        original.setShape(10, new Ellipse2D.Double(2, 2, 2, 2));
        original.setShape(100, null);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();
        
        assertEquals("Sparse list serialization should preserve equality", original, deserialized);
        assertEquals("Index 0 shape", original.getShape(0), deserialized.getShape(0));
        assertEquals("Index 10 shape", original.getShape(10), deserialized.getShape(10));
        assertNull("Index 100 should be null", deserialized.getShape(100));
        assertNull("Index 50 should be null (gap)", deserialized.getShape(50));
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testHashCode_ConsistentWithEquals() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        list1.setShape(0, shape);
        list2.setShape(0, shape);
        
        assertEquals("Equal objects must have equal hash codes", 
                list1.hashCode(), list2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCode_StableAcrossCalls() {
        ShapeList list = new ShapeList();
        list.setShape(0, new Ellipse2D.Double(1, 1, 2, 2));
        int hash1 = list.hashCode();
        int hash2 = list.hashCode();
        assertEquals("Hash code should be stable", hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testEquals_Reflexive() {
        ShapeList list = new ShapeList();
        list.setShape(0, new Line2D.Double(0, 0, 1, 1));
        assertEquals("Equals should be reflexive", list, list);
    }

    @Test(timeout = 4000)
    public void testEquals_Symmetric() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        
        Shape shape = new Rectangle2D.Double(1, 1, 1, 1);
        list1.setShape(0, shape);
        list2.setShape(0, shape);
        
        assertEquals("Equals should be symmetric", list1.equals(list2), list2.equals(list1));
    }

    @Test(timeout = 4000)
    public void testEquals_Transitive() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();
        ShapeList list3 = new ShapeList();
        
        Shape shape = new Ellipse2D.Double(2, 2, 3, 3);
        list1.setShape(0, shape);
        list2.setShape(0, shape);
        list3.setShape(0, shape);
        
        assertEquals("Equals should be transitive", list1, list2);
        assertEquals("Equals should be transitive", list2, list3);
        assertEquals("Equals should be transitive", list1, list3);
    }

    @Test(timeout = 4000)
    public void testGetShape_AfterClone_Independent() throws CloneNotSupportedException {
        ShapeList original = new ShapeList();
        Shape shape = new Rectangle2D.Double(1, 2, 3, 4);
        original.setShape(0, shape);
        
        ShapeList cloned = (ShapeList) original.clone();
        
        // Modify original
        original.setShape(0, new Ellipse2D.Double(9, 9, 9, 9));
        
        // Clone should still have original shape
        assertEquals("Clone should be independent", shape, cloned.getShape(0));
        assertNotEquals("Original should be modified", shape, original.getShape(0));
    }

    @Test(timeout = 4000)
    public void testSetShape_AfterSerialization_Independent() throws Exception {
        ShapeList original = new ShapeList();
        original.setShape(0, new Rectangle2D.Double(1, 1, 1, 1));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        oos.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();
        
        // Modify original
        original.setShape(0, new Ellipse2D.Double(5, 5, 5, 5));
        
        // Deserialized should still have original shape
        assertEquals("Deserialized should be independent", 
                new Rectangle2D.Double(1, 1, 1, 1), deserialized.getShape(0));
    }

    @Test(timeout = 4000)
    public void testMultipleShapes_DifferentTypes() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle2D.Double(1, 2, 3, 4);
        Shape ellipse = new Ellipse2D.Double(5, 6, 7, 8);
        Shape line = new Line2D.Double(0, 0, 10, 10);
        
        list.setShape(0, rect);
        list.setShape(1, ellipse);
        list.setShape(2, line);
        
        assertEquals("Rectangle shape", rect, list.getShape(0));
        assertEquals("Ellipse shape", ellipse, list.getShape(1));
        assertEquals("Line shape", line, list.getShape(2));
    }

    @Test(timeout = 4000)
    public void testSetShape_GetShape_AllShapeTypes() {
        ShapeList list = new ShapeList();
        
        Shape[] shapes = {
            new Rectangle2D.Double(1, 1, 1, 1),
            new Ellipse2D.Double(2, 2, 2, 2),
            new Line2D.Double(0, 0, 3, 3),
            new java.awt.geom.RoundRectangle2D.Double(4, 4, 4, 4, 1, 1),
            new java.awt.geom.Arc2D.Double(5, 5, 5, 5, 0, 90, Arc2D.PIE)
        };
        
        for (int i = 0; i < shapes.length; i++) {
            list.setShape(i, shapes[i]);
        }
        
        for (int i = 0; i < shapes.length; i++) {
            assertEquals("Shape at index " + i, shapes[i], list.getShape(i));
        }
    }
}