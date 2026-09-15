package org.apache.commons.collections4;

import java.util.*;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.iterators.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for IteratorUtils targeting maximum coverage and defect detection.
 */
public class IteratorUtilsDeepseekTest {

    // ==================== Empty Iterator Tests ====================

    /**
     * @target emptyIterator()
     * @scenario Normal usage
     * @defectRisk Returns non-empty iterator
     */
    @Test(timeout = 4000)
    public void testEmptyIterator() {
        ResettableIterator<String> it = IteratorUtils.emptyIterator();
        assertFalse("Empty iterator should have no elements", it.hasNext());
    }

    /**
     * @target emptyListIterator()
     * @scenario Normal usage
     * @defectRisk Returns non-empty list iterator
     */
    @Test(timeout = 4000)
    public void testEmptyListIterator() {
        ResettableListIterator<String> it = IteratorUtils.emptyListIterator();
        assertFalse("Empty list iterator should have no elements", it.hasNext());
        assertFalse("Empty list iterator should have no previous", it.hasPrevious());
    }

    /**
     * @target emptyOrderedIterator()
     * @scenario Normal usage
     * @defectRisk Returns non-empty ordered iterator
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedIterator() {
        OrderedIterator<String> it = IteratorUtils.emptyOrderedIterator();
        assertFalse("Empty ordered iterator should have no elements", it.hasNext());
    }

    /**
     * @target emptyMapIterator()
     * @scenario Normal usage
     * @defectRisk Returns non-empty map iterator
     */
    @Test(timeout = 4000)
    public void testEmptyMapIterator() {
        MapIterator<String, String> it = IteratorUtils.emptyMapIterator();
        assertFalse("Empty map iterator should have no elements", it.hasNext());
    }

    /**
     * @target emptyOrderedMapIterator()
     * @scenario Normal usage
     * @defectRisk Returns non-empty ordered map iterator
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedMapIterator() {
        OrderedMapIterator<String, String> it = IteratorUtils.emptyOrderedMapIterator();
        assertFalse("Empty ordered map iterator should have no elements", it.hasNext());
    }

    // ==================== Singleton Iterator Tests ====================

    /**
     * @target singletonIterator()
     * @scenario Normal usage with non-null object
     * @defectRisk Returns iterator with wrong element
     */
    @Test(timeout = 4000)
    public void testSingletonIterator() {
        ResettableIterator<String> it = IteratorUtils.singletonIterator("test");
        assertTrue("Singleton iterator should have one element", it.hasNext());
        assertEquals("Element should be 'test'", "test", it.next());
        assertFalse("Singleton iterator should be exhausted", it.hasNext());
    }

    /**
     * @target singletonListIterator()
     * @scenario Normal usage with non-null object
     * @defectRisk Returns list iterator with wrong element
     */
    @Test(timeout = 4000)
    public void testSingletonListIterator() {
        ListIterator<String> it = IteratorUtils.singletonListIterator("test");
        assertTrue("Singleton list iterator should have one element", it.hasNext());
        assertEquals("Element should be 'test'", "test", it.next());
        assertFalse("Singleton list iterator should be exhausted", it.hasNext());
        assertTrue("Should have previous", it.hasPrevious());
    }

    // ==================== Array Iterator Tests ====================

    /**
     * @target arrayIterator(E...)
     * @scenario Normal usage with varargs
     * @defectRisk Returns iterator with wrong elements
     */
    @Test(timeout = 4000)
    public void testArrayIteratorVarargs() {
        ResettableIterator<String> it = IteratorUtils.arrayIterator("a", "b", "c");
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    /**
     * @target arrayIterator(Object)
     * @scenario Normal usage with primitive array
     * @defectRisk Fails to handle primitive arrays
     */
    @Test(timeout = 4000)
    public void testArrayIteratorPrimitive() {
        int[] arr = {1, 2, 3};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator(arr);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    /**
     * @target arrayIterator(E[], int)
     * @scenario Normal usage with start index
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testArrayIteratorWithStart() {
        ResettableIterator<String> it = IteratorUtils.arrayIterator(new String[]{"a", "b", "c"}, 1);
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("b", "c"), result);
    }

    /**
     * @target arrayIterator(E[], int, int)
     * @scenario Normal usage with start and end
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testArrayIteratorWithStartEnd() {
        ResettableIterator<String> it = IteratorUtils.arrayIterator(new String[]{"a", "b", "c", "d"}, 1, 3);
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("b", "c"), result);
    }

    /**
     * @target arrayListIterator(E...)
     * @scenario Normal usage with varargs
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorVarargs() {
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator("a", "b", "c");
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    /**
     * @target arrayListIterator(Object)
     * @scenario Normal usage with primitive array
     * @defectRisk Fails to handle primitive arrays
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorPrimitive() {
        int[] arr = {1, 2, 3};
        ResettableListIterator<Integer> it = IteratorUtils.arrayListIterator(arr);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    // ==================== Chained Iterator Tests ====================

    /**
     * @target chainedIterator(Iterator, Iterator)
     * @scenario Two iterators
     * @defectRisk Returns wrong order
     */
    @Test(timeout = 4000)
    public void testChainedIteratorTwo() {
        Iterator<String> it1 = Arrays.asList("a", "b").iterator();
        Iterator<String> it2 = Arrays.asList("c", "d").iterator();
        Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2);
        List<String> result = new ArrayList<>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    /**
     * @target chainedIterator(Iterator...)
     * @scenario Varargs iterators
     * @defectRisk Returns wrong order
     */
    @Test(timeout = 4000)
    public void testChainedIteratorVarargs() {
        Iterator<String> it1 = Arrays.asList("a").iterator();
        Iterator<String> it2 = Arrays.asList("b").iterator();
        Iterator<String> it3 = Arrays.asList("c").iterator();
        Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2, it3);
        List<String> result = new ArrayList<>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    /**
     * @target chainedIterator(Collection)
     * @scenario Collection of iterators
     * @defectRisk Returns wrong order
     */
    @Test(timeout = 4000)
    public void testChainedIteratorCollection() {
        Collection<Iterator<String>> iterators = new ArrayList<>();
        iterators.add(Arrays.asList("x", "y").iterator());
        iterators.add(Arrays.asList("z").iterator());
        Iterator<String> chained = IteratorUtils.chainedIterator(iterators);
        List<String> result = new ArrayList<>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("x", "y", "z"), result);
    }

    // ==================== Filtered Iterator Tests ====================

    /**
     * @target filteredIterator()
     * @scenario Filter with predicate
     * @defectRisk Returns unfiltered elements
     */
    @Test(timeout = 4000)
    public void testFilteredIterator() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        Predicate<Integer> evenPredicate = value -> value % 2 == 0;
        Iterator<Integer> filtered = IteratorUtils.filteredIterator(it, evenPredicate);
        List<Integer> result = new ArrayList<>();
        while (filtered.hasNext()) {
            result.add(filtered.next());
        }
        assertEquals(Arrays.asList(2, 4), result);
    }

    /**
     * @target filteredListIterator()
     * @scenario Filter with predicate
     * @defectRisk Returns unfiltered elements
     */
    @Test(timeout = 4000)
    public void testFilteredListIterator() {
        ListIterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).listIterator();
        Predicate<Integer> evenPredicate = value -> value % 2 == 0;
        ListIterator<Integer> filtered = IteratorUtils.filteredListIterator(it, evenPredicate);
        List<Integer> result = new ArrayList<>();
        while (filtered.hasNext()) {
            result.add(filtered.next());
        }
        assertEquals(Arrays.asList(2, 4), result);
    }

    // ==================== Looping Iterator Tests ====================

    /**
     * @target loopingIterator()
     * @scenario Loop over collection
     * @defectRisk Does not loop correctly
     */
    @Test(timeout = 4000)
    public void testLoopingIterator() {
        Collection<String> coll = Arrays.asList("a", "b");
        ResettableIterator<String> it = IteratorUtils.loopingIterator(coll);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("a", it.next()); // loops back
        assertEquals("b", it.next());
    }

    /**
     * @target loopingListIterator()
     * @scenario Loop over list
     * @defectRisk Does not loop correctly
     */
    @Test(timeout = 4000)
    public void testLoopingListIterator() {
        List<String> list = Arrays.asList("x", "y");
        ResettableListIterator<String> it = IteratorUtils.loopingListIterator(list);
        assertEquals("x", it.next());
        assertEquals("y", it.next());
        assertEquals("x", it.next()); // loops back
        assertEquals("y", it.next());
    }

    // ==================== Peeking Iterator Tests ====================

    /**
     * @target peekingIterator()
     * @scenario Peek at elements
     * @defectRisk Peek returns wrong element
     */
    @Test(timeout = 4000)
    public void testPeekingIterator() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        Iterator<String> peeking = IteratorUtils.peekingIterator(it);
        assertTrue(peeking instanceof PeekingIterator);
        PeekingIterator<String> pIt = (PeekingIterator<String>) peeking;
        assertEquals("a", pIt.peek());
        assertEquals("a", pIt.next());
        assertEquals("b", pIt.peek());
        assertEquals("b", pIt.next());
    }

    // ==================== Pushback Iterator Tests ====================

    /**
     * @target pushbackIterator()
     * @scenario Push back elements
     * @defectRisk Pushback does not work
     */
    @Test(timeout = 4000)
    public void testPushbackIterator() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        PushbackIterator<String> pushback = (PushbackIterator<String>) IteratorUtils.pushbackIterator(it);
        assertEquals("a", pushback.next());
        pushback.pushback("pushed");
        assertEquals("pushed", pushback.next());
        assertEquals("b", pushback.next());
    }

    // ==================== Collated Iterator Tests (Defect Target) ====================

    /**
     * @target collatedIterator(Comparator, Iterator, Iterator)
     * @scenario Null comparator with natural order
     * @defectRisk COLLECTIONS-566: NullPointerException when comparator is null
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_NullComparator_NaturalOrder_COLLECTIONS566() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2);
        List<Integer> result = new ArrayList<>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    /**
     * @target collatedIterator(Comparator, Iterator...)
     * @scenario Null comparator with varargs
     * @defectRisk COLLECTIONS-566: NullPointerException when comparator is null
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_NullComparator_Varargs_COLLECTIONS566() {
        Iterator<Integer> it1 = Arrays.asList(1, 4).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 5).iterator();
        Iterator<Integer> it3 = Arrays.asList(3, 6).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2, it3);
        List<Integer> result = new ArrayList<>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    /**
     * @target collatedIterator(Comparator, Collection)
     * @scenario Null comparator with collection
     * @defectRisk COLLECTIONS-566: NullPointerException when comparator is null
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_NullComparator_Collection_COLLECTIONS566() {
        Collection<Iterator<Integer>> iterators = new ArrayList<>();
        iterators.add(Arrays.asList(1, 3, 5).iterator());
        iterators.add(Arrays.asList(2, 4, 6).iterator());
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, iterators);
        List<Integer> result = new ArrayList<>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    /**
     * @target collatedIterator(Comparator, Iterator, Iterator)
     * @scenario Custom comparator
     * @defectRisk Wrong ordering
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_CustomComparator() {
        Comparator<Integer> reverse = Comparator.reverseOrder();
        Iterator<Integer> it1 = Arrays.asList(5, 3, 1).iterator();
        Iterator<Integer> it2 = Arrays.asList(6, 4, 2).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(reverse, it1, it2);
        List<Integer> result = new ArrayList<>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(6, 5, 4, 3, 2, 1), result);
    }

    // ==================== Utility Method Tests ====================

    /**
     * @target size()
     * @scenario Non-null iterator
     * @defectRisk Returns wrong size
     */
    @Test(timeout = 4000)
    public void testSize() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertEquals(3, IteratorUtils.size(it));
    }

    /**
     * @target size()
     * @scenario Null iterator
     * @defectRisk Throws exception
     */
    @Test(timeout = 4000)
    public void testSizeNull() {
        assertEquals(0, IteratorUtils.size(null));
    }

    /**
     * @target isEmpty()
     * @scenario Non-empty iterator
     * @defectRisk Returns wrong result
     */
    @Test(timeout = 4000)
    public void testIsEmptyFalse() {
        Iterator<String> it = Arrays.asList("a").iterator();
        assertFalse(IteratorUtils.isEmpty(it));
    }

    /**
     * @target isEmpty()
     * @scenario Empty iterator
     * @defectRisk Returns wrong result
     */
    @Test(timeout = 4000)
    public void testIsEmptyTrue() {
        Iterator<String> it = Collections.emptyIterator();
        assertTrue(IteratorUtils.isEmpty(it));
    }

    /**
     * @target isEmpty()
     * @scenario Null iterator
     * @defectRisk Throws exception
     */
    @Test(timeout = 4000)
    public void testIsEmptyNull() {
        assertTrue(IteratorUtils.isEmpty(null));
    }

    /**
     * @target contains()
     * @scenario Element present
     * @defectRisk Returns false when element present
     */
    @Test(timeout = 4000)
    public void testContainsTrue() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertTrue(IteratorUtils.contains(it, "b"));
    }

    /**
     * @target contains()
     * @scenario Element absent
     * @defectRisk Returns true when element absent
     */
    @Test(timeout = 4000)
    public void testContainsFalse() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        assertFalse(IteratorUtils.contains(it, "z"));
    }

    /**
     * @target get()
     * @scenario Valid index
     * @defectRisk Returns wrong element
     */
    @Test(timeout = 4000)
    public void testGet() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertEquals("b", IteratorUtils.get(it, 1));
    }

    /**
     * @target get()
     * @scenario Index out of bounds
     * @defectRisk Does not throw IndexOutOfBoundsException
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        Iterator<String> it = Arrays.asList("a").iterator();
        IteratorUtils.get(it, 5);
    }

    /**
     * @target toList()
     * @scenario Normal usage
     * @defectRisk Returns wrong list
     */
    @Test(timeout = 4000)
    public void testToList() {
        Iterator<String> it = Arrays.asList("x", "y", "z").iterator();
        List<String> list = IteratorUtils.toList(it);
        assertEquals(Arrays.asList("x", "y", "z"), list);
    }

    /**
     * @target toList(Iterator, int)
     * @scenario With estimated size
     * @defectRisk Returns wrong list
     */
    @Test(timeout = 4000)
    public void testToListWithEstimatedSize() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        List<String> list = IteratorUtils.toList(it, 5);
        assertEquals(Arrays.asList("a", "b"), list);
    }

    /**
     * @target toArray()
     * @scenario Normal usage
     * @defectRisk Returns wrong array
     */
    @Test(timeout = 4000)
    public void testToArray() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Object[] arr = IteratorUtils.toArray(it);
        assertArrayEquals(new Object[]{"a", "b"}, arr);
    }

    /**
     * @target toArray(Iterator, Class)
     * @scenario With type
     * @defectRisk Returns wrong array
     */
    @Test(timeout = 4000)
    public void testToArrayWithClass() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        String[] arr = IteratorUtils.toArray(it, String.class);
        assertArrayEquals(new String[]{"a", "b"}, arr);
    }

    // ==================== Bounded Iterator Tests ====================

    /**
     * @target boundedIterator()
     * @scenario With max only
     * @defectRisk Returns wrong number of elements
     */
    @Test(timeout = 4000)
    public void testBoundedIteratorMax() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        BoundedIterator<Integer> bounded = IteratorUtils.boundedIterator(it, 3);
        List<Integer> result = new ArrayList<>();
        while (bounded.hasNext()) {
            result.add(bounded.next());
        }
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    /**
     * @target boundedIterator()
     * @scenario With offset and max
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testBoundedIteratorOffsetMax() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        BoundedIterator<Integer> bounded = IteratorUtils.boundedIterator(it, 1, 3);
        List<Integer> result = new ArrayList<>();
        while (bounded.hasNext()) {
            result.add(bounded.next());
        }
        assertEquals(Arrays.asList(2, 3, 4), result);
    }

    // ==================== Skipping Iterator Tests ====================

    /**
     * @target skippingIterator()
     * @scenario Skip elements
     * @defectRisk Skips wrong elements
     */
    @Test(timeout = 4000)
    public void testSkippingIterator() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        SkippingIterator<Integer> skipping = IteratorUtils.skippingIterator(it, 2);
        List<Integer> result = new ArrayList<>();
        while (skipping.hasNext()) {
            result.add(skipping.next());
        }
        assertEquals(Arrays.asList(3, 4, 5), result);
    }

    // ==================== Zipping Iterator Tests ====================

    /**
     * @target zippingIterator(Iterator, Iterator)
     * @scenario Two iterators
     * @defectRisk Wrong interleaving
     */
    @Test(timeout = 4000)
    public void testZippingIteratorTwo() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();
        ZippingIterator<Integer> zipping = IteratorUtils.zippingIterator(it1, it2);
        List<Integer> result = new ArrayList<>();
        while (zipping.hasNext()) {
            result.add(zipping.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    /**
     * @target zippingIterator(Iterator, Iterator, Iterator)
     * @scenario Three iterators
     * @defectRisk Wrong interleaving
     */
    @Test(timeout = 4000)
    public void testZippingIteratorThree() {
        Iterator<Integer> it1 = Arrays.asList(1, 4).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 5).iterator();
        Iterator<Integer> it3 = Arrays.asList(3, 6).iterator();
        ZippingIterator<Integer> zipping = IteratorUtils.zippingIterator(it1, it2, it3);
        List<Integer> result = new ArrayList<>();
        while (zipping.hasNext()) {
            result.add(zipping.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    // ==================== Unmodifiable Iterator Tests ====================

    /**
     * @target unmodifiableIterator()
     * @scenario Remove attempt
     * @defectRisk Does not throw UnsupportedOperationException
     */
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testUnmodifiableIteratorRemove() {
        Iterator<String> it = Arrays.asList("a").iterator();
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(it);
        unmod.next();
        unmod.remove();
    }

    /**
     * @target unmodifiableListIterator()
     * @scenario Remove attempt
     * @defectRisk Does not throw UnsupportedOperationException
     */
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testUnmodifiableListIteratorRemove() {
        ListIterator<String> it = Arrays.asList("a").listIterator();
        ListIterator<String> unmod = IteratorUtils.unmodifiableListIterator(it);
        unmod.next();
        unmod.remove();
    }

    // ==================== View Iterator Tests ====================

    /**
     * @target asIterator(Enumeration)
     * @scenario Normal usage
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testAsIteratorFromEnumeration() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        Iterator<String> it = IteratorUtils.asIterator(v.elements());
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target asEnumeration()
     * @scenario Normal usage
     * @defectRisk Returns wrong elements
     */
    @Test(timeout = 4000)
    public void testAsEnumeration() {
        Iterator<String> it = Arrays.asList("x", "y").iterator();
        Enumeration<String> en = IteratorUtils.asEnumeration(it);
        List<String> result = new ArrayList<>();
        while (en.hasMoreElements()) {
            result.add(en.nextElement());
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    /**
     * @target asIterable()
     * @scenario Single use
     * @defectRisk Cannot iterate
     */
    @Test(timeout = 4000)
    public void testAsIterable() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Iterable<String> iterable = IteratorUtils.asIterable(it);
        List<String> result = new ArrayList<>();
        for (String s : iterable) {
            result.add(s);
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target toListIterator()
     * @scenario Normal usage
     * @defectRisk Returns wrong list iterator
     */
    @Test(timeout = 4000)
    public void testToListIterator() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        ListIterator<String> listIt = IteratorUtils.toListIterator(it);
        assertTrue(listIt.hasNext());
        assertEquals("a", listIt.next());
        assertTrue(listIt.hasPrevious());
        assertEquals("a", listIt.previous());
    }

    // ==================== getIterator Tests ====================

    /**
     * @target getIterator()
     * @scenario Null input
     * @defectRisk Returns non-empty iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorNull() {
        Iterator<?> it = IteratorUtils.getIterator(null);
        assertFalse(it.hasNext());
    }

    /**
     * @target getIterator()
     * @scenario Iterator input
     * @defectRisk Returns wrong iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorIterator() {
        Iterator<String> input = Arrays.asList("a").iterator();
        Iterator<?> it = IteratorUtils.getIterator(input);
        assertSame(input, it);
    }

    /**
     * @target getIterator()
     * @scenario Iterable input
     * @defectRisk Returns wrong iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorIterable() {
        Iterable<String> input = Arrays.asList("a", "b");
        Iterator<?> it = IteratorUtils.getIterator(input);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
    }

    /**
     * @target getIterator()
     * @scenario Array input
     * @defectRisk Returns wrong iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorArray() {
        Iterator<?> it = IteratorUtils.getIterator(new String[]{"x", "y"});
        List<Object> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    /**
     * @target getIterator()
     * @scenario Map input
     * @defectRisk Returns wrong iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorMap() {
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        Iterator<?> it = IteratorUtils.getIterator(map);
        List<Object> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertTrue(result.contains("v1"));
        assertTrue(result.contains("v2"));
    }

    // ==================== apply, find, matchesAny, matchesAll Tests ====================

    /**
     * @target apply()
     * @scenario Apply closure to elements
     * @defectRisk Closure not applied
     */
    @Test(timeout = 4000)
    public void testApply() {
        List<String> result = new ArrayList<>();
        Closure<String> closure = result::add;
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        IteratorUtils.apply(it, closure);
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target find()
     * @scenario Element found
     * @defectRisk Returns null when element exists
     */
    @Test(timeout = 4000)
    public void testFindFound() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Integer found = IteratorUtils.find(it, value -> value > 1);
        assertEquals(Integer.valueOf(2), found);
    }

    /**
     * @target find()
     * @scenario Element not found
     * @defectRisk Returns non-null when element absent
     */
    @Test(timeout = 4000)
    public void testFindNotFound() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        Integer found = IteratorUtils.find(it, value -> value > 10);
        assertNull(found);
    }

    /**
     * @target matchesAny()
     * @scenario Match exists
     * @defectRisk Returns false when match exists
     */
    @Test(timeout = 4000)
    public void testMatchesAnyTrue() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        assertTrue(IteratorUtils.matchesAny(it, value -> value == 2));
    }

    /**
     * @target matchesAny()
     * @scenario No match
     * @defectRisk Returns true when no match
     */
    @Test(timeout = 4000)
    public void testMatchesAnyFalse() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        assertFalse(IteratorUtils.matchesAny(it, value -> value > 10));
    }

    /**
     * @target matchesAll()
     * @scenario All match
     * @defectRisk Returns false when all match
     */
    @Test(timeout = 4000)
    public void testMatchesAllTrue() {
        Iterator<Integer> it = Arrays.asList(2, 4, 6).iterator();
        assertTrue(IteratorUtils.matchesAll(it, value -> value % 2 == 0));
    }

    /**
     * @target matchesAll()
     * @scenario Not all match
     * @defectRisk Returns true when not all match
     */
    @Test(timeout = 4000)
    public void testMatchesAllFalse() {
        Iterator<Integer> it = Arrays.asList(2, 3, 4).iterator();
        assertFalse(IteratorUtils.matchesAll(it, value -> value % 2 == 0));
    }

    // ==================== toString Tests ====================

    /**
     * @target toString(Iterator)
     * @scenario Normal usage
     * @defectRisk Returns wrong string
     */
    @Test(timeout = 4000)
    public void testToString() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        String result = IteratorUtils.toString(it);
        assertEquals("[a, b, c]", result);
    }

    /**
     * @target toString(Iterator, Transformer)
     * @scenario With transformer
     * @defectRisk Returns wrong string
     */
    @Test(timeout = 4000)
    public void testToStringWithTransformer() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Transformer<Integer, String> transformer = Object::toString;
        String result = IteratorUtils.toString(it, transformer);
        assertEquals("[1, 2, 3]", result);
    }

    /**
     * @target toString(Iterator, Transformer, String, String, String)
     * @scenario Custom delimiters
     * @defectRisk Returns wrong string
     */
    @Test(timeout = 4000)
    public void testToStringCustomDelimiters() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        String result = IteratorUtils.toString(it, Object::toString, "|", "(", ")");
        assertEquals("(a|b)", result);
    }

    // ==================== Null/Exception Tests ====================

    /**
     * @target filteredIterator()
     * @scenario Null iterator
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilteredIteratorNullIterator() {
        IteratorUtils.filteredIterator(null, TruePredicate.truePredicate());
    }

    /**
     * @target filteredIterator()
     * @scenario Null predicate
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilteredIteratorNullPredicate() {
        IteratorUtils.filteredIterator(Collections.emptyIterator(), null);
    }

    /**
     * @target filteredListIterator()
     * @scenario Null list iterator
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilteredListIteratorNullIterator() {
        IteratorUtils.filteredListIterator(null, TruePredicate.truePredicate());
    }

    /**
     * @target filteredListIterator()
     * @scenario Null predicate
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilteredListIteratorNullPredicate() {
        IteratorUtils.filteredListIterator(Collections.emptyListIterator(), null);
    }

    /**
     * @target loopingIterator()
     * @scenario Null collection
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testLoopingIteratorNull() {
        IteratorUtils.loopingIterator(null);
    }

    /**
     * @target loopingListIterator()
     * @scenario Null list
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testLoopingListIteratorNull() {
        IteratorUtils.loopingListIterator(null);
    }

    /**
     * @target toList(Iterator, int)
     * @scenario Invalid estimated size
     * @defectRisk Does not throw IllegalArgumentException
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToListInvalidEstimatedSize() {
        IteratorUtils.toList(Collections.emptyIterator(), 0);
    }

    /**
     * @target toArray(Iterator, Class)
     * @scenario Null class
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToArrayNullClass() {
        IteratorUtils.toArray(Collections.emptyIterator(), null);
    }

    /**
     * @target apply()
     * @scenario Null closure
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testApplyNullClosure() {
        IteratorUtils.apply(Collections.emptyIterator(), null);
    }

    /**
     * @target find()
     * @scenario Null predicate
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFindNullPredicate() {
        IteratorUtils.find(Collections.emptyIterator(), null);
    }

    /**
     * @target matchesAny()
     * @scenario Null predicate
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testMatchesAnyNullPredicate() {
        IteratorUtils.matchesAny(Collections.emptyIterator(), null);
    }

    /**
     * @target matchesAll()
     * @scenario Null predicate
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testMatchesAllNullPredicate() {
        IteratorUtils.matchesAll(Collections.emptyIterator(), null);
    }

    /**
     * @target toString(Iterator, Transformer, String, String, String)
     * @scenario Null transformer
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToStringNullTransformer() {
        IteratorUtils.toString(Collections.emptyIterator(), null, ",", "[", "]");
    }

    /**
     * @target toString(Iterator, Transformer, String, String, String)
     * @scenario Null delimiter
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToStringNullDelimiter() {
        IteratorUtils.toString(Collections.emptyIterator(), Object::toString, null, "[", "]");
    }

    /**
     * @target toString(Iterator, Transformer, String, String, String)
     * @scenario Null prefix
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToStringNullPrefix() {
        IteratorUtils.toString(Collections.emptyIterator(), Object::toString, ",", null, "]");
    }

    /**
     * @target toString(Iterator, Transformer, String, String, String)
     * @scenario Null suffix
     * @defectRisk Does not throw NullPointerException
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToStringNullSuffix() {
        IteratorUtils.toString(Collections.emptyIterator(), Object::toString, ",", "[", null);
    }
}