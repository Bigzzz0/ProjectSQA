package org.apache.commons.collections4.collection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.BoundedCollection;
import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;

public class UnmodifiableBoundedCollectionDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Target class: UnmodifiableBoundedCollection
     * 
     * Branches to cover:
     * 1. unmodifiableBoundedCollection(BoundedCollection) - null check (implicit via constructor)
     * 2. unmodifiableBoundedCollection(Collection) - null check -> throw IllegalArgumentException
     * 3. unmodifiableBoundedCollection(Collection) - loop up to 1000 iterations
     *    - if coll instanceof BoundedCollection -> break
     *    - if coll instanceof AbstractCollectionDecorator -> unwrap decorated()
     *    - else if coll instanceof SynchronizedCollection -> unwrap decorated()
     *    - after loop: if coll instanceof BoundedCollection == false -> throw IllegalArgumentException
     * 4. Constructor - super((BoundedCollection<E>) coll) - null check via AbstractCollectionDecorator
     * 5. iterator() - returns UnmodifiableIterator wrapping decorated().iterator()
     * 6. add/addAll/clear/remove/removeAll/retainAll - all throw UnsupportedOperationException
     * 7. isFull() - delegates to decorated().isFull()
     * 8. maxSize() - delegates to decorated().maxSize()
     * 9. decorated() - returns (BoundedCollection<E>) super.decorated()
     * 
     * Defect targeting:
     * The known defect involves the factory method unmodifiableBoundedCollection(Collection)
     * when the input collection is wrapped in multiple decorators. The loop unwrapping logic
     * may fail to correctly identify the underlying BoundedCollection, causing the returned
     * collection to have incorrect ordering or content. The test testDecorateFactory expects
     * a specific order of elements, and testUnmodifiable expects all mutators to throw.
     * 
     * The defect is likely in the loop's unwrapping logic - specifically when the collection
     * is wrapped in a SynchronizedCollection that itself wraps an AbstractCollectionDecorator,
     * or when the loop exits prematurely without fully unwrapping to the BoundedCollection.
     * 
     * To trigger the defect, we need to create a collection wrapped in multiple decorators
     * (e.g., SynchronizedCollection wrapping a BoundedCollection, or AbstractCollectionDecorator
     * wrapping another decorator) and verify the factory correctly unwraps to the underlying
     * bounded collection.
     * 
     * The test must assert the exact content and order of the resulting unmodifiable collection
     * to reveal the defect.
     */

    // Helper class to create a bounded collection for testing
    private static class TestBoundedCollection<E> extends ArrayList<E> implements BoundedCollection<E> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        TestBoundedCollection(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public boolean isFull() {
            return size() >= maxSize;
        }

        @Override
        public int maxSize() {
            return maxSize;
        }
    }

    // Helper to create a SynchronizedCollection wrapper (package-private access)
    private static class TestSynchronizedCollection<E> extends SynchronizedCollection<E> {
        private static final long serialVersionUID = 1L;

        TestSynchronizedCollection(Collection<E> coll) {
            super(coll);
        }
    }

    // Helper to create an AbstractCollectionDecorator wrapper
    private static class TestDecoratorCollection<E> extends AbstractCollectionDecorator<E> {
        private static final long serialVersionUID = 1L;

        TestDecoratorCollection(Collection<E> coll) {
            super(coll);
        }
    }

    // ----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testUnmodifiableBoundedCollectionWithBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(5);
        bounded.add("A");
        bounded.add("B");
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertNotNull(unmod);
        assertTrue(unmod instanceof UnmodifiableBoundedCollection);
        assertEquals(2, unmod.size());
        assertEquals(5, unmod.maxSize());
        assertFalse(unmod.isFull());
        assertEquals("A", unmod.iterator().next());
    }

    @Test(timeout = 4000)
    public void testIsFullAndMaxSize() {
        TestBoundedCollection<Integer> bounded = new TestBoundedCollection<Integer>(3);
        bounded.add(1);
        bounded.add(2);
        bounded.add(3);
        BoundedCollection<Integer> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertTrue(unmod.isFull());
        assertEquals(3, unmod.maxSize());
    }

    @Test(timeout = 4000)
    public void testIteratorReturnsUnmodifiableIterator() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(10);
        bounded.add("x");
        bounded.add("y");
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        Iterator<String> it = unmod.iterator();
        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullBoundedCollectionFactory() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<String>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullCollectionFactory() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(0);
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertTrue(unmod.isEmpty());
        assertTrue(unmod.isFull());
        assertEquals(0, unmod.maxSize());
    }

    @Test(timeout = 4000)
    public void testMaxSizeZero() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(0);
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertEquals(0, unmod.maxSize());
        assertTrue(unmod.isFull());
    }

    // ----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // ----------------------------------------------------------------------

    /**
     * This test directly targets the known defect in testDecorateFactory.
     * The defect occurs when the factory method is called with a collection
     * wrapped in multiple decorators. The expected behavior is that the factory
     * correctly unwraps all decorators to find the underlying BoundedCollection
     * and returns an unmodifiable view with the correct element order.
     * 
     * The specific scenario from the defect: a collection with elements
     * [", One, 2, Three, null, 4, One, 5.0, 6.0, Seven, Eight, Nine, 10, 11, 12, Thirteen, 14, 15, 16"]
     * wrapped in decorators. The defective version produces a different order.
     */
    @Test(timeout = 4000)
    public void testDecorateFactoryWithMultipleDecorators() {
        // Create the exact collection from the defect report
        List<Object> baseList = new ArrayList<Object>();
        baseList.add("");
        baseList.add("One");
        baseList.add(2);
        baseList.add("Three");
        baseList.add(null);
        baseList.add(4);
        baseList.add("One");
        baseList.add(5.0);
        baseList.add(6.0);
        baseList.add("Seven");
        baseList.add("Eight");
        baseList.add("Nine");
        baseList.add(10);
        baseList.add(11);
        baseList.add(12);
        baseList.add("Thirteen");
        baseList.add(14);
        baseList.add(15);
        baseList.add(16);

        // Wrap in a BoundedCollection
        TestBoundedCollection<Object> bounded = new TestBoundedCollection<Object>(20);
        bounded.addAll(baseList);

        // Wrap in multiple decorators to test unwrapping logic
        Collection<Object> wrapped1 = new TestSynchronizedCollection<Object>(bounded);
        Collection<Object> wrapped2 = new TestDecoratorCollection<Object>(wrapped1);
        Collection<Object> wrapped3 = new TestSynchronizedCollection<Object>(wrapped2);

        // Call the factory with the heavily wrapped collection
        BoundedCollection<Object> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(wrapped3);

        // Verify the content and order matches the original
        assertEquals("Size should match", baseList.size(), unmod.size());
        Iterator<Object> it = unmod.iterator();
        for (Object expected : baseList) {
            assertTrue("Iterator should have next", it.hasNext());
            Object actual = it.next();
            if (expected == null) {
                assertNull("Expected null element", actual);
            } else {
                assertEquals("Element mismatch", expected, actual);
            }
        }
        assertFalse("Iterator should be exhausted", it.hasNext());
    }

    /**
     * This test targets the testUnmodifiable defect - all mutators must throw
     * UnsupportedOperationException.
     */
    @Test(timeout = 4000)
    public void testUnmodifiableAllMutatorsThrow() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(5);
        bounded.add("A");
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);

        // Test add
        try {
            unmod.add("B");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Test addAll
        try {
            unmod.addAll(Arrays.asList("C", "D"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Test clear
        try {
            unmod.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Test remove
        try {
            unmod.remove("A");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Test removeAll
        try {
            unmod.removeAll(Arrays.asList("A"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Test retainAll
        try {
            unmod.retainAll(Arrays.asList("A"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        // Verify collection is unchanged
        assertEquals(1, unmod.size());
        assertEquals("A", unmod.iterator().next());
    }

    // ----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFactoryWithNonBoundedCollection() {
        List<String> plainList = new ArrayList<String>();
        plainList.add("A");
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(plainList);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactoryWithAbstractDecoratorNonBounded() {
        List<String> plainList = new ArrayList<String>();
        plainList.add("A");
        Collection<String> decorated = new TestDecoratorCollection<String>(plainList);
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(decorated);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactoryWithSynchronizedNonBounded() {
        List<String> plainList = new ArrayList<String>();
        plainList.add("A");
        Collection<String> sync = new TestSynchronizedCollection<String>(plainList);
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(sync);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDecoratedReturnsBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(5);
        bounded.add("A");
        UnmodifiableBoundedCollection<String> unmod = 
            (UnmodifiableBoundedCollection<String>) UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        BoundedCollection<String> decorated = unmod.decorated();
        assertSame(bounded, decorated);
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() throws Exception {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(5);
        bounded.add("A");
        bounded.add("B");
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);

        // Test serialization round-trip
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(unmod);
        oos.close();

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof BoundedCollection);
        BoundedCollection<String> deserializedUnmod = (BoundedCollection<String>) deserialized;
        assertEquals(2, deserializedUnmod.size());
        assertEquals(5, deserializedUnmod.maxSize());
        Iterator<String> it = deserializedUnmod.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
    }

    @Test(timeout = 4000)
    public void testMultipleUnwrappingLayers() {
        // Test with multiple layers of AbstractCollectionDecorator and SynchronizedCollection
        TestBoundedCollection<Integer> bounded = new TestBoundedCollection<Integer>(10);
        bounded.add(1);
        bounded.add(2);
        bounded.add(3);

        Collection<Integer> layer1 = new TestSynchronizedCollection<Integer>(bounded);
        Collection<Integer> layer2 = new TestDecoratorCollection<Integer>(layer1);
        Collection<Integer> layer3 = new TestSynchronizedCollection<Integer>(layer2);
        Collection<Integer> layer4 = new TestDecoratorCollection<Integer>(layer3);

        BoundedCollection<Integer> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(layer4);
        assertEquals(3, unmod.size());
        assertEquals(10, unmod.maxSize());
        Iterator<Integer> it = unmod.iterator();
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
    }

    @Test(timeout = 4000)
    public void testFactoryWithDirectBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(3);
        bounded.add("x");
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertTrue(unmod instanceof UnmodifiableBoundedCollection);
        assertEquals(1, unmod.size());
        assertEquals(3, unmod.maxSize());
    }

    @Test(timeout = 4000)
    public void testFactoryWithSynchronizedBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(3);
        bounded.add("x");
        Collection<String> sync = new TestSynchronizedCollection<String>(bounded);
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(sync);
        assertEquals(1, unmod.size());
        assertEquals(3, unmod.maxSize());
    }

    @Test(timeout = 4000)
    public void testFactoryWithDecoratorBoundedCollection() {
        TestBoundedCollection<String> bounded = new TestBoundedCollection<String>(3);
        bounded.add("x");
        Collection<String> decorated = new TestDecoratorCollection<String>(bounded);
        BoundedCollection<String> unmod = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(decorated);
        assertEquals(1, unmod.size());
        assertEquals(3, unmod.maxSize());
    }
}