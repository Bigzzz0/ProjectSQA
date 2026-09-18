package org.jfree.chart.util;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Class Under Test: org.jfree.chart.util.ShapeList
 *
 * Branches & Logic Evaluated:
 * 1. getShape(int) / setShape(int, Shape):
 *    - Valid indices (0, positive offsets, expanding capacity).
 *    - Unpopulated / null indices.
 * 2. clone():
 *    - Empty list cloning integrity.
 *    - Populated list deep-copy verification (independent mutation).
 * 3. equals(Object):
 *    - Branch: obj == this -> true.
 *    - Branch: !(obj instanceof ShapeList) -> false (null, non-ShapeList types).
 *    - Branch: super.equals(obj) vs ShapeUtilities.equal:
 *      * DEFECT TARGET: Standard Shape implementations (Line2D, Ellipse2D, GeneralPath)
 *        do NOT override java.lang.Object.equals(). AbstractObjectList.equals() fails
 *        to recognize two distinct instances with identical geometry as equal unless
 *        ShapeList overrides equals() and uses ShapeUtilities.equal().
 * 4. hashCode():
 *    - Consistent with equality contract for identical and distinct instances.
 * 5. Serialization (writeObject / readObject):
 *    - Null entries within bounds (shape == null -> write -1, index == -1 branch in readObject).
 *    - Non-null entries (shape != null -> write index and shape, index != -1 branch).
 *    - Roundtrip fidelity with Line2D / Ellipse2D shapes.
 * 6. Defensive Paths:
 *    - Negative index handling (IllegalArgumentException inherited from AbstractObjectList).
 * =========================================================================
 */
public class ShapeListGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private static ShapeList roundTripSerialize(ShapeList original) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(original);
            out.flush();
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()))) {
            return (ShapeList) in.readObject();
        }
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetAndSetShapeNormal() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
        Shape line = new Line2D.Double(0.0, 0.0, 5.0, 5.0);

        list.setShape(0, rect);
        list.setShape(1, line);

        assertEquals(rect, list.getShape(0));
        assertEquals(line, list.getShape(1));
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testGetShapeUnpopulatedIndexReturnsNull() {
        ShapeList list = new ShapeList();
        assertNull(list.getShape(0));
        assertNull(list.getShape(5));
    }

    @Test(timeout = 4000)
    public void testSetShapeOverwriteExisting() {
        ShapeList list = new ShapeList();
        Shape shape1 = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape shape2 = new Rectangle2D.Double(5.0, 6.0, 7.0, 8.0);

        list.setShape(0, shape1);
        assertEquals(shape1, list.getShape(0));

        list.setShape(0, shape2);
        assertEquals(shape2, list.getShape(0));
        assertEquals(1, list.size());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSetShapeSparseExpansion() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 10.0);

        // Setting an index beyond default increment expands the list with nulls
        list.setShape(15, rect);

        assertEquals(16, list.size());
        assertNull(list.getShape(0));
        assertNull(list.getShape(14));
        assertEquals(rect, list.getShape(15));
    }

    @Test(timeout = 4000)
    public void testSetShapeNullExplicitly() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle2D.Double(1.0, 1.0, 2.0, 2.0);
        list.setShape(0, rect);
        list.setShape(1, null);

        assertEquals(2, list.size());
        assertEquals(rect, list.getShape(0));
        assertNull(list.getShape(1));
    }

    @Test(timeout = 4000)
    public void testEmptyListState() {
        ShapeList list = new ShapeList();
        assertEquals(0, list.size());
        assertNull(list.getShape(0));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects)
    // -------------------------------------------------------------------------

    /**
     * Targets defects4j failure in ShapeList.equals():
     * Line2D and Ellipse2D in Java do not implement Object.equals().
     * Two distinct instances with the same geometry are equal according to
     * ShapeUtilities.equal(), which ShapeList.equals() must use.
     */
    @Test(timeout = 4000)
    public void testEqualsWithLine2DShapesRevealingDefect() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Line2D.Double(1.0, 2.0, 3.0, 4.0));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Line2D.Double(1.0, 2.0, 3.0, 4.0));

        assertTrue("ShapeList instances containing equivalent Line2D shapes must be equal", list1.equals(list2));
        assertTrue("Equality must be symmetric", list2.equals(list1));
    }

    /**
     * Targets defects4j failure in ShapeList serialization roundtrip:
     * When deserialized, a new Line2D or Ellipse2D instance is instantiated.
     * The deserialized ShapeList must compare equal to the original.
     */
    @Test(timeout = 4000)
    public void testSerializationWithNonObjectEqualsShapes() throws Exception {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Line2D.Double(1.0, 2.0, 3.0, 4.0));
        list1.setShape(2, new Ellipse2D.Double(5.0, 6.0, 7.0, 8.0));

        ShapeList list2 = roundTripSerialize(list1);

        assertEquals("Deserialized ShapeList must equal original containing Line2D/Ellipse2D", list1, list2);
    }

    @Test(timeout = 4000)
    public void testEqualsWithEllipseAndGeneralPath() {
        ShapeList list1 = new ShapeList();
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0.0f, 0.0f);
        path1.lineTo(10.0f, 10.0f);
        list1.setShape(0, path1);
        list1.setShape(1, new Ellipse2D.Float(1.0f, 2.0f, 3.0f, 4.0f));

        ShapeList list2 = new ShapeList();
        GeneralPath path2 = new GeneralPath();
        path2.moveTo(0.0f, 0.0f);
        path2.lineTo(10.0f, 10.0f);
        list2.setShape(0, path2);
        list2.setShape(1, new Ellipse2D.Float(1.0f, 2.0f, 3.0f, 4.0f));

        assertTrue(list1.equals(list2));
        assertTrue(list2.equals(list1));
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetShapeNegativeIndexThrowsException() {
        ShapeList list = new ShapeList();
        list.setShape(-1, new Rectangle2D.Double(0, 0, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetShapeNegativeIndexThrowsException() {
        ShapeList list = new ShapeList();
        list.getShape(-1);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsBasicBranches() {
        ShapeList list1 = new ShapeList();

        // Reflexivity (obj == this)
        assertTrue(list1.equals(list1));

        // Null comparison
        assertFalse(list1.equals(null));

        // Type mismatch (!(obj instanceof ShapeList))
        assertFalse(list1.equals("A String"));
        assertFalse(list1.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsAndElements() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0));
        list2.setShape(1, new Rectangle2D.Double(1.0, 1.0, 2.0, 2.0));

        // Different sizes
        assertFalse(list1.equals(list2));
        assertFalse(list2.equals(list1));

        // Different shapes at same index
        ShapeList list3 = new ShapeList();
        list3.setShape(0, new Rectangle2D.Double(9.0, 9.0, 9.0, 9.0));
        assertFalse(list1.equals(list3));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullGaps() {
        ShapeList list1 = new ShapeList();
        list1.setShape(0, null);
        list1.setShape(1, new Rectangle2D.Double(1.0, 1.0, 1.0, 1.0));

        ShapeList list2 = new ShapeList();
        list2.setShape(0, null);
        list2.setShape(1, new Rectangle2D.Double(1.0, 1.0, 1.0, 1.0));

        assertTrue(list1.equals(list2));

        // Mismatched null vs non-null
        ShapeList list3 = new ShapeList();
        list3.setShape(0, new Rectangle2D.Double(1.0, 1.0, 1.0, 1.0));
        list3.setShape(1, null);
        assertFalse(list1.equals(list3));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();

        assertEquals(list1.hashCode(), list2.hashCode());

        list1.setShape(0, new Rectangle2D.Double(0, 0, 10, 10));
        list2.setShape(0, new Rectangle2D.Double(0, 0, 10, 10));

        assertEquals(list1.hashCode(), list2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloning() throws CloneNotSupportedException {
        ShapeList original = new ShapeList();
        Shape shape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        original.setShape(0, shape);

        ShapeList clone = (ShapeList) original.clone();
        assertNotSame(original, clone);
        assertEquals(original, clone);
        assertEquals(original.getShape(0), clone.getShape(0));

        // Verify independent mutation
        clone.setShape(0, new Rectangle2D.Double(9.0, 9.0, 9.0, 9.0));
        assertFalse(original.equals(clone));
        assertEquals(shape, original.getShape(0));
    }

    @Test(timeout = 4000)
    public void testSerializationWithNullGaps() throws Exception {
        ShapeList list = new ShapeList();
        // Index 0 has a shape, index 1 is null, index 2 has a shape
        list.setShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        list.setShape(2, new Rectangle2D.Double(5.0, 6.0, 7.0, 8.0));

        ShapeList deserialized = roundTripSerialize(list);

        assertEquals(3, deserialized.size());
        assertEquals(list.getShape(0), deserialized.getShape(0));
        assertNull(deserialized.getShape(1));
        assertEquals(list.getShape(2), deserialized.getShape(2));
        assertEquals(list, deserialized);
    }

    @Test(timeout = 4000)
    public void testSerializationEmptyList() throws Exception {
        ShapeList list = new ShapeList();
        ShapeList deserialized = roundTripSerialize(list);

        assertEquals(0, deserialized.size());
        assertEquals(list, deserialized);
    }
}