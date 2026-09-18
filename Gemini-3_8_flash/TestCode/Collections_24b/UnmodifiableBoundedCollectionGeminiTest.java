package org.apache.commons.collections4.collection;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.collections4.collection.UnmodifiableBoundedCollection
 * Defects4J Defect Focus:
 *  - Failure: testDecorateFactory and testUnmodifiable fail with AssertionFailedError: expected same:<...> was not:<...>
 *  - Root Cause: Factory methods unmodifiableBoundedCollection(BoundedCollection) and
 *    unmodifiableBoundedCollection(Collection) re-wrap an already unmodifiable bounded collection in a
 *    new UnmodifiableBoundedCollection instance instead of returning the identical instance.
 *
 * Branch & Boundary Decision Matrix:
 * 1. unmodifiableBoundedCollection(BoundedCollection coll):
 *    - coll == null: Defensive check path (NullPointerException / IllegalArgumentException)
 *    - coll instanceof UnmodifiableBoundedCollection: Expected to return coll directly (Defect Zone)
 *    - coll is standard BoundedCollection: Wraps in UnmodifiableBoundedCollection
 * 2. unmodifiableBoundedCollection(Collection coll):
 *    - coll == null: Explicit branch throws IllegalArgumentException
 *    - Loop unwrapping (up to 1000 depth):
 *      - Branch: coll instanceof BoundedCollection -> terminates loop
 *      - Branch: coll instanceof AbstractCollectionDecorator -> coll.decorated()
 *      - Branch: coll instanceof SynchronizedCollection -> coll.decorated()
 *      - Loop guard (i >= 1000): Infinite decorator chain prevention
 *    - Post-loop branch:
 *      - !(coll instanceof BoundedCollection): throws IllegalArgumentException
 *      - coll instanceof BoundedCollection: wraps/returns unmodifiable instance
 * 3. Mutator Operations (Guaranteed UnsupportedOperationException):
 *    - add(E), addAll(Collection), clear(), remove(Object), removeAll(Collection), retainAll(Collection)
 * 4. Read-Only Delegate Pass-Through:
 *    - iterator(): Returns UnmodifiableIterator (remove() throws UnsupportedOperationException)
 *    - isFull(): Returns decorated().isFull()
 *    - maxSize(): Returns decorated().maxSize()
 * 5. Lifecycle & Serialization:
 *    - Round-trip serialization preserves elements, bounds, and immutability invariants.
 */

import org.apache.commons.collections4.BoundedCollection;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class UnmodifiableBoundedCollectionGeminiTest {

    // =========================================================================
    // Test Doubles (Deterministic Bounded Collection & Decorator Helpers)
    // =========================================================================

    private static class SimpleBoundedCollection<E> extends ArrayList<E> implements BoundedCollection<E> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        public SimpleBoundedCollection(final int maxSize) {
            super();
            this.maxSize = maxSize;
        }

        public SimpleBoundedCollection(final int maxSize, final Collection<? extends E> coll) {
            super(coll);
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

    private static class SimpleCollectionDecorator<E> extends AbstractCollectionDecorator<E> {
        private static final long serialVersionUID = 1L;

        public SimpleCollectionDecorator(final Collection<E> coll) {
            super(coll);
        }
    }

    private static class LoopingDecorator<E> extends AbstractCollectionDecorator<E> {
        private static final long serialVersionUID = 1L;

        public LoopingDecorator() {
            super(new ArrayList<E>());
        }

        @Override
        protected Collection<E> decorated() {
            return this;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecorateBoundedCollectionBasic() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(5);
        bounded.add("alpha");
        bounded.add("beta");

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);

        assertEquals(2, unmod.size());
        assertEquals(5, unmod.maxSize());
        assertFalse(unmod.isFull());
        assertFalse(unmod.isEmpty());
        assertTrue(unmod.contains("alpha"));
        assertTrue(unmod.contains("beta"));
        assertFalse(unmod.contains("gamma"));
        assertTrue(unmod.containsAll(Arrays.asList("alpha", "beta")));

        final Object[] array = unmod.toArray();
        assertEquals(2, array.length);
        assertEquals("alpha", array[0]);
        assertEquals("beta", array[1]);

        final String[] typedArray = unmod.toArray(new String[2]);
        assertArrayEquals(new String[]{"alpha", "beta"}, typedArray);
    }

    @Test(timeout = 4000)
    public void testIsFullStateTransition() {
        final SimpleBoundedCollection<Integer> bounded = new SimpleBoundedCollection<Integer>(2);
        bounded.add(1);

        final BoundedCollection<Integer> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertFalse("Collection should not be full when size < maxSize", unmod.isFull());

        // Modify underlying collection to verify dynamic delegation
        bounded.add(2);
        assertTrue("Collection should be full when size == maxSize", unmod.isFull());
    }

    @Test(timeout = 4000)
    public void testIteratorTraversalAndUnmodifiability() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("x");
        bounded.add("y");

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        final Iterator<String> it = unmod.iterator();

        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        assertTrue(it.hasNext());
        assertEquals("y", it.next());
        assertFalse(it.hasNext());

        try {
            it.remove();
            fail("Iterator.remove() must throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyBoundedCollection() {
        final SimpleBoundedCollection<String> emptyBounded = new SimpleBoundedCollection<String>(0);
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(emptyBounded);

        assertEquals(0, unmod.size());
        assertEquals(0, unmod.maxSize());
        assertTrue(unmod.isEmpty());
        assertTrue("Size 0 with maxSize 0 must be full", unmod.isFull());
        assertFalse(unmod.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testBoundedCollectionMaxIntegerCapacity() {
        final SimpleBoundedCollection<String> largeBounded =
                new SimpleBoundedCollection<String>(Integer.MAX_VALUE);
        largeBounded.add("single");

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(largeBounded);

        assertEquals(Integer.MAX_VALUE, unmod.maxSize());
        assertFalse(unmod.isFull());
        assertEquals(1, unmod.size());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedDecoratorsUnder1000() {
        final SimpleBoundedCollection<String> base = new SimpleBoundedCollection<String>(10);
        base.add("deepValue");

        Collection<String> current = base;
        for (int i = 0; i < 50; i++) {
            current = new SimpleCollectionDecorator<String>(current);
        }

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(current);

        assertNotNull(unmod);
        assertEquals(10, unmod.maxSize());
        assertEquals(1, unmod.size());
        assertTrue(unmod.contains("deepValue"));
    }

    @Test(timeout = 4000)
    public void testLoopCounterBoundaryMax1000ThrowsException() {
        final LoopingDecorator<String> infiniteDecorator = new LoopingDecorator<String>();
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(infiniteDecorator);
            fail("Looping decorator exceeding 1000 unwraps must throw IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            assertEquals("The collection is not a bounded collection", ex.getMessage());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectDecorateFactoryReturnsSameInstanceForBoundedCollection() {
        final SimpleBoundedCollection<String> base = new SimpleBoundedCollection<String>(5);
        base.add("One");
        base.add("2");

        final BoundedCollection<String> unmod1 =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(base);

        // Core defect trigger: Decorating an already unmodifiable bounded collection
        // must return the identical instance rather than wrapping it again.
        final BoundedCollection<String> unmod2 =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(unmod1);

        assertSame("Expected unmodifiableBoundedCollection(BoundedCollection) to return the same instance",
                unmod1, unmod2);
    }

    @Test(timeout = 4000)
    public void testDefectDecorateFactoryReturnsSameInstanceForCollectionOverload() {
        final SimpleBoundedCollection<String> base = new SimpleBoundedCollection<String>(5);
        base.add("item");

        final BoundedCollection<String> unmod1 =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(base);

        final Collection<String> unmodAsCollection = unmod1;
        final BoundedCollection<String> unmod2 =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(unmodAsCollection);

        assertSame("Expected unmodifiableBoundedCollection(Collection) to return the same instance",
                unmod1, unmod2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullBoundedCollectionThrowsException() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<String>) null);
            fail("null BoundedCollection should throw IllegalArgumentException or NullPointerException");
        } catch (final IllegalArgumentException | NullPointerException expected) {
            // Defensive behavior verified
        }
    }

    @Test(timeout = 4000)
    public void testNullCollectionThrowsIllegalArgumentException() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((Collection<String>) null);
            fail("null Collection must throw IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            assertEquals("The collection must not be null", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNonBoundedCollectionThrowsIllegalArgumentException() {
        final Collection<String> standardList = new ArrayList<String>();
        standardList.add("notBounded");

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(standardList);
            fail("Non-bounded Collection must throw IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            assertEquals("The collection is not a bounded collection", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNonBoundedDecoratorThrowsIllegalArgumentException() {
        final Collection<String> nonBoundedDecorator =
                new SimpleCollectionDecorator<String>(new ArrayList<String>());

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(nonBoundedDecorator);
            fail("Decorator wrapping non-bounded Collection must throw IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            assertEquals("The collection is not a bounded collection", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSynchronizedNonBoundedCollectionThrowsIllegalArgumentException() {
        final Collection<String> syncNonBounded =
                SynchronizedCollection.synchronizedCollection(new ArrayList<String>());

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(syncNonBounded);
            fail("SynchronizedCollection wrapping non-bounded Collection must throw IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            assertEquals("The collection is not a bounded collection", ex.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAddThrowsUnsupportedOperationException() {
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(new SimpleBoundedCollection<String>(3));
        unmod.add("fail");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAddAllThrowsUnsupportedOperationException() {
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(new SimpleBoundedCollection<String>(3));
        unmod.addAll(Collections.singletonList("fail"));
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testClearThrowsUnsupportedOperationException() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("elem");
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmod.clear();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRemoveThrowsUnsupportedOperationException() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("elem");
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmod.remove("elem");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRemoveAllThrowsUnsupportedOperationException() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("elem");
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmod.removeAll(Collections.singletonList("elem"));
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRetainAllThrowsUnsupportedOperationException() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(3);
        bounded.add("elem");
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        unmod.retainAll(Collections.singletonList("elem"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSynchronizedCollectionUnwrapping() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(4);
        bounded.add("syncVal");

        final Collection<String> syncColl = SynchronizedCollection.synchronizedCollection(bounded);
        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(syncColl);

        assertNotNull(unmod);
        assertEquals(4, unmod.maxSize());
        assertEquals(1, unmod.size());
        assertTrue(unmod.contains("syncVal"));
    }

    @Test(timeout = 4000)
    public void testMixedDecoratorChainUnwrapping() {
        final SimpleBoundedCollection<String> bounded = new SimpleBoundedCollection<String>(8);
        bounded.add("deepNested");

        // Chain: SynchronizedCollection -> SimpleCollectionDecorator -> SynchronizedCollection -> bounded
        final Collection<String> sync1 = SynchronizedCollection.synchronizedCollection(bounded);
        final Collection<String> decorator = new SimpleCollectionDecorator<String>(sync1);
        final Collection<String> sync2 = SynchronizedCollection.synchronizedCollection(decorator);

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(sync2);

        assertNotNull(unmod);
        assertEquals(8, unmod.maxSize());
        assertEquals(1, unmod.size());
        assertTrue(unmod.contains("deepNested"));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationRoundTrip() throws Exception {
        final SimpleBoundedCollection<String> original = new SimpleBoundedCollection<String>(7);
        original.add("serializedItem1");
        original.add("serializedItem2");

        final BoundedCollection<String> unmod =
                UnmodifiableBoundedCollection.unmodifiableBoundedCollection(original);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(unmod);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof UnmodifiableBoundedCollection);
        final BoundedCollection<String> restored = (BoundedCollection<String>) deserialized;

        assertEquals(unmod.size(), restored.size());
        assertEquals(unmod.maxSize(), restored.maxSize());
        assertEquals(unmod.isFull(), restored.isFull());
        assertTrue(restored.contains("serializedItem1"));
        assertTrue(restored.contains("serializedItem2"));

        try {
            restored.add("illegal");
            fail("Deserialized collection must remain unmodifiable");
        } catch (final UnsupportedOperationException expected) {
            // Success
        }
    }
}