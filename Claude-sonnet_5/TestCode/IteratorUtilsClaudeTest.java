package org.apache.commons.collections4;

import java.util.*;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.iterators.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class IteratorUtilsClaudeTest {

    /**
     * @target IteratorUtils.collatedIterator(Comparator, Iterator, Iterator)
     * @scenario null comparator with two sorted iterators of Integers
     * @defectRisk NullPointerException thrown when comparator is null instead of using natural order
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_NullComparator_NaturalOrder_COLLECTIONS566() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();

        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2);

        List<Integer> result = new ArrayList<Integer>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);

        // array variant
        Iterator<Integer> itA = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> itB = Arrays.asList(2, 4, 6).iterator();
        @SuppressWarnings("unchecked")
        Iterator<Integer> collatedArray = IteratorUtils.collatedIterator(null, new Iterator[] { itA, itB });
        List<Integer> resultArray = new ArrayList<Integer>();
        while (collatedArray.hasNext()) {
            resultArray.add(collatedArray.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), resultArray);

        // collection variant
        Iterator<Integer> itC = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> itD = Arrays.asList(2, 4, 6).iterator();
        Collection<Iterator<? extends Integer>> coll = new ArrayList<Iterator<? extends Integer>>();
        coll.add(itC);
        coll.add(itD);
        Iterator<Integer> collatedColl = IteratorUtils.collatedIterator(null, coll);
        List<Integer> resultColl = new ArrayList<Integer>();
        while (collatedColl.hasNext()) {
            resultColl.add(collatedColl.next());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), resultColl);
    }

    /**
     * @target IteratorUtils.emptyIterator
     * @scenario invoking emptyIterator returns iterator with no elements
     * @defectRisk hasNext() incorrectly returning true for empty iterator
     */
    @Test(timeout = 4000)
    public void testEmptyIterator() {
        ResettableIterator<Object> it = IteratorUtils.emptyIterator();
        assertFalse(it.hasNext());
        it.reset();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.emptyListIterator
     * @scenario invoking emptyListIterator returns list iterator with no elements
     * @defectRisk hasPrevious/hasNext incorrectly returning true
     */
    @Test(timeout = 4000)
    public void testEmptyListIterator() {
        ResettableListIterator<Object> it = IteratorUtils.emptyListIterator();
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        it.reset();
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.emptyOrderedIterator
     * @scenario invoking emptyOrderedIterator returns ordered iterator with no elements
     * @defectRisk hasPrevious/hasNext incorrectly returning true for empty ordered iterator
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedIterator() {
        OrderedIterator<Object> it = IteratorUtils.emptyOrderedIterator();
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    /**
     * @target IteratorUtils.emptyMapIterator
     * @scenario invoking emptyMapIterator returns map iterator with no elements
     * @defectRisk hasNext incorrectly returning true for empty map iterator
     */
    @Test(timeout = 4000)
    public void testEmptyMapIterator() {
        MapIterator<Object, Object> it = IteratorUtils.emptyMapIterator();
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.emptyOrderedMapIterator
     * @scenario invoking emptyOrderedMapIterator returns ordered map iterator with no elements
     * @defectRisk hasPrevious/hasNext incorrectly returning true for empty ordered map iterator
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedMapIterator() {
        OrderedMapIterator<Object, Object> it = IteratorUtils.emptyOrderedMapIterator();
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    /**
     * @target IteratorUtils.singletonIterator
     * @scenario iterate over a single object then verify exhaustion
     * @defectRisk singleton iterator returning more than one element or none
     */
    @Test(timeout = 4000)
    public void testSingletonIterator() {
        ResettableIterator<String> it = IteratorUtils.singletonIterator("hello");
        assertTrue(it.hasNext());
        assertEquals("hello", it.next());
        assertFalse(it.hasNext());
        it.reset();
        assertTrue(it.hasNext());
        assertEquals("hello", it.next());
    }

    /**
     * @target IteratorUtils.singletonListIterator
     * @scenario iterate forward and backward over single object
     * @defectRisk incorrect hasPrevious/hasNext state transitions
     */
    @Test(timeout = 4000)
    public void testSingletonListIterator() {
        ListIterator<String> it = IteratorUtils.singletonListIterator("world");
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());
        assertEquals("world", it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals("world", it.previous());
    }

    /**
     * @target IteratorUtils.arrayIterator(E...)
     * @scenario iterate varargs array of Strings
     * @defectRisk incorrect element order or bounds handling
     */
    @Test(timeout = 4000)
    public void testArrayIteratorVarargs() {
        ResettableIterator<String> it = IteratorUtils.arrayIterator("a", "b", "c");
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
        it.reset();
        assertTrue(it.hasNext());
    }

    /**
     * @target IteratorUtils.arrayIterator(Object)
     * @scenario iterate over primitive int array wrapped as Object
     * @defectRisk primitive array not wrapped correctly into Integer objects
     */
    @Test(timeout = 4000)
    public void testArrayIteratorPrimitive() {
        int[] arr = {1, 2, 3};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator((Object) arr);
        List<Integer> result = new ArrayList<Integer>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    /**
     * @target IteratorUtils.arrayIterator(E[], int)
     * @scenario iterate starting from an offset within an object array
     * @defectRisk start index off-by-one error
     */
    @Test(timeout = 4000)
    public void testArrayIteratorWithStart() {
        String[] arr = {"a", "b", "c", "d"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr, 2);
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("c", "d"), result);
    }

    /**
     * @target IteratorUtils.arrayIterator(Object, int)
     * @scenario iterate primitive array starting at offset
     * @defectRisk start index applied incorrectly to primitive array
     */
    @Test(timeout = 4000)
    public void testArrayIteratorObjectWithStart() {
        int[] arr = {10, 20, 30, 40};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator((Object) arr, 1);
        List<Integer> result = new ArrayList<Integer>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(20, 30, 40), result);
    }

    /**
     * @target IteratorUtils.arrayIterator(E[], int, int)
     * @scenario iterate a sub-range [start,end) of an object array
     * @defectRisk end boundary exclusive/inclusive mishandling
     */
    @Test(timeout = 4000)
    public void testArrayIteratorWithStartEnd() {
        String[] arr = {"a", "b", "c", "d", "e"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr, 1, 3);
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("b", "c"), result);
    }

    /**
     * @target IteratorUtils.arrayIterator(Object, int, int)
     * @scenario iterate a sub-range of primitive array using Object signature
     * @defectRisk incorrect handling of primitive array bounds
     */
    @Test(timeout = 4000)
    public void testArrayIteratorObjectWithStartEnd() {
        int[] arr = {1, 2, 3, 4, 5};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator((Object) arr, 1, 4);
        List<Integer> result = new ArrayList<Integer>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(2, 3, 4), result);
    }

    /**
     * @target IteratorUtils.arrayListIterator(E...)
     * @scenario list iterator over varargs array supporting bidirectional traversal
     * @defectRisk previous()/next() out of sync after traversal
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorVarargs() {
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator("x", "y", "z");
        assertEquals("x", it.next());
        assertEquals("y", it.next());
        assertEquals("y", it.previous());
        assertEquals("x", it.previous());
        it.reset();
        assertTrue(it.hasNext());
    }

    /**
     * @target IteratorUtils.arrayListIterator(Object)
     * @scenario list iterator over primitive array
     * @defectRisk wrapper class conversion incorrect
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorPrimitive() {
        double[] arr = {1.1, 2.2};
        ResettableListIterator<Double> it = IteratorUtils.arrayListIterator((Object) arr);
        assertEquals(Double.valueOf(1.1), it.next());
        assertEquals(Double.valueOf(2.2), it.next());
    }

    /**
     * @target IteratorUtils.arrayListIterator(E[], int)
     * @scenario list iterator with start offset over object array
     * @defectRisk start offset applied incorrectly
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorWithStart() {
        String[] arr = {"a", "b", "c"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr, 1);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.arrayListIterator(Object, int)
     * @scenario list iterator with start offset over primitive array
     * @defectRisk start offset applied incorrectly to primitive array
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorObjectWithStart() {
        long[] arr = {100L, 200L, 300L};
        ResettableListIterator<Long> it = IteratorUtils.arrayListIterator((Object) arr, 1);
        assertEquals(Long.valueOf(200L), it.next());
        assertEquals(Long.valueOf(300L), it.next());
    }

    /**
     * @target IteratorUtils.arrayListIterator(E[], int, int)
     * @scenario list iterator over a sub-range of an object array
     * @defectRisk end boundary mishandled
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorWithStartEnd() {
        String[] arr = {"a", "b", "c", "d"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr, 1, 3);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.arrayListIterator(Object, int, int)
     * @scenario list iterator over sub-range of primitive array
     * @defectRisk incorrect index calc for primitive sub-range
     */
    @Test(timeout = 4000)
    public void testArrayListIteratorObjectWithStartEnd() {
        int[] arr = {1, 2, 3, 4, 5};
        ResettableListIterator<Integer> it = IteratorUtils.arrayListIterator((Object) arr, 1, 4);
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.chainedIterator(Iterator, Iterator)
     * @scenario chain two iterators and verify combined sequence
     * @defectRisk elements missing or duplicated at chain boundary
     */
    @Test(timeout = 4000)
    public void testChainedIteratorTwo() {
        Iterator<String> it1 = Arrays.asList("a", "b").iterator();
        Iterator<String> it2 = Arrays.asList("c", "d").iterator();
        Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2);
        List<String> result = new ArrayList<String>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    /**
     * @target IteratorUtils.chainedIterator(Iterator...)
     * @scenario chain array of iterators varargs
     * @defectRisk array variant iterating incorrectly
     */
    @Test(timeout = 4000)
    public void testChainedIteratorArray() {
        Iterator<String> it1 = Arrays.asList("a").iterator();
        Iterator<String> it2 = Arrays.asList("b").iterator();
        Iterator<String> it3 = Arrays.asList("c").iterator();
        @SuppressWarnings("unchecked")
        Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2, it3);
        List<String> result = new ArrayList<String>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    /**
     * @target IteratorUtils.chainedIterator(Collection)
     * @scenario chain a collection of iterators
     * @defectRisk collection based chaining not preserving order
     */
    @Test(timeout = 4000)
    public void testChainedIteratorCollection() {
        Iterator<String> it1 = Arrays.asList("x").iterator();
        Iterator<String> it2 = Arrays.asList("y").iterator();
        Collection<Iterator<? extends String>> coll = new ArrayList<Iterator<? extends String>>();
        coll.add(it1);
        coll.add(it2);
        Iterator<String> chained = IteratorUtils.chainedIterator(coll);
        List<String> result = new ArrayList<String>();
        while (chained.hasNext()) {
            result.add(chained.next());
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    /**
     * @target IteratorUtils.filteredIterator
     * @scenario filter iterator to include only even numbers
     * @defectRisk filter predicate not correctly applied, elements leak through
     */
    @Test(timeout = 4000)
    public void testFilteredIterator() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5, 6).iterator();
        Predicate<Integer> evenPredicate = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        Iterator<Integer> filtered = IteratorUtils.filteredIterator(it, evenPredicate);
        List<Integer> result = new ArrayList<Integer>();
        while (filtered.hasNext()) {
            result.add(filtered.next());
        }
        assertEquals(Arrays.asList(2, 4, 6), result);
    }

    /**
     * @target IteratorUtils.filteredIterator
     * @scenario filteredIterator with null iterator throws NullPointerException
     * @defectRisk missing null-check leads to NPE with unclear message
     */
    @Test(timeout = 4000)
    public void testFilteredIteratorNullIterator() {
        try {
            IteratorUtils.filteredIterator(null, TruePredicate.truePredicate());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.filteredIterator
     * @scenario filteredIterator with null predicate throws NullPointerException
     * @defectRisk missing null-check for predicate parameter
     */
    @Test(timeout = 4000)
    public void testFilteredIteratorNullPredicate() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        try {
            IteratorUtils.filteredIterator(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.filteredListIterator
     * @scenario filter list iterator to include only elements matching predicate
     * @defectRisk filtered list iterator incorrect traversal
     */
    @Test(timeout = 4000)
    public void testFilteredListIterator() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        ListIterator<Integer> baseIt = list.listIterator();
        Predicate<Integer> oddPredicate = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object % 2 != 0;
            }
        };
        ListIterator<Integer> filtered = IteratorUtils.filteredListIterator(baseIt, oddPredicate);
        List<Integer> result = new ArrayList<Integer>();
        while (filtered.hasNext()) {
            result.add(filtered.next());
        }
        assertEquals(Arrays.asList(1, 3, 5), result);
    }

    /**
     * @target IteratorUtils.filteredListIterator
     * @scenario null list iterator throws NullPointerException
     * @defectRisk missing null check for listIterator parameter
     */
    @Test(timeout = 4000)
    public void testFilteredListIteratorNullListIterator() {
        try {
            IteratorUtils.filteredListIterator(null, TruePredicate.truePredicate());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.filteredListIterator
     * @scenario null predicate throws NullPointerException
     * @defectRisk missing null check for predicate parameter
     */
    @Test(timeout = 4000)
    public void testFilteredListIteratorNullPredicate() {
        List<Integer> list = Arrays.asList(1, 2);
        ListIterator<Integer> baseIt = list.listIterator();
        try {
            IteratorUtils.filteredListIterator(baseIt, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.loopingIterator
     * @scenario looping iterator repeats collection elements continuously
     * @defectRisk infinite loop or termination too early
     */
    @Test(timeout = 4000)
    public void testLoopingIterator() {
        List<String> coll = Arrays.asList("a", "b");
        ResettableIterator<String> it = IteratorUtils.loopingIterator(coll);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("a", it.next()); // loops back
        assertEquals("b", it.next());
        it.reset();
        assertEquals("a", it.next());
    }

    /**
     * @target IteratorUtils.loopingIterator
     * @scenario null collection throws NullPointerException
     * @defectRisk missing null check for collection parameter
     */
    @Test(timeout = 4000)
    public void testLoopingIteratorNullCollection() {
        try {
            IteratorUtils.loopingIterator(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.loopingListIterator
     * @scenario looping list iterator supports both forward looping and backward looping
     * @defectRisk incorrect wraparound logic for previous()/next()
     */
    @Test(timeout = 4000)
    public void testLoopingListIterator() {
        List<String> list = Arrays.asList("a", "b", "c");
        ResettableListIterator<String> it = IteratorUtils.loopingListIterator(list);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertEquals("a", it.next()); // loop
        it.reset();
        assertEquals("a", it.next());
    }

    /**
     * @target IteratorUtils.loopingListIterator
     * @scenario null list throws NullPointerException
     * @defectRisk missing null check for list parameter
     */
    @Test(timeout = 4000)
    public void testLoopingListIteratorNullList() {
        try {
            IteratorUtils.loopingListIterator(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.peekingIterator
     * @scenario peek at next element without advancing then consume normally
     * @defectRisk peek() advancing underlying iterator incorrectly
     */
    @Test(timeout = 4000)
    public void testPeekingIterator() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        PeekingIterator<String> peekIt = (PeekingIterator<String>) IteratorUtils.peekingIterator(it);
        assertEquals("a", peekIt.peek());
        assertEquals("a", peekIt.peek()); // peek again, same element
        assertEquals("a", peekIt.next());
        assertEquals("b", peekIt.next());
        assertEquals("c", peekIt.peek());
        assertEquals("c", peekIt.next());
        assertFalse(peekIt.hasNext());
    }

    /**
     * @target IteratorUtils.pushbackIterator
     * @scenario push back a value and re-read it
     * @defectRisk pushback not returning pushed value first on next call
     */
    @Test(timeout = 4000)
    public void testPushbackIterator() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        PushbackIterator<String> pushIt = (PushbackIterator<String>) IteratorUtils.pushbackIterator(it);
        assertEquals("a", pushIt.next());
        pushIt.pushback("a");
        assertEquals("a", pushIt.next());
        assertEquals("b", pushIt.next());
        assertFalse(pushIt.hasNext());
    }

    /**
     * @target IteratorUtils.size
     * @scenario count size of a populated iterator
     * @defectRisk off-by-one counting error
     */
    @Test(timeout = 4000)
    public void testSizeNonEmpty() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        assertEquals(3, IteratorUtils.size(it));
    }

    /**
     * @target IteratorUtils.size
     * @scenario null iterator returns size 0
     * @defectRisk NPE thrown instead of returning 0 for null iterator
     */
    @Test(timeout = 4000)
    public void testSizeNullIterator() {
        assertEquals(0, IteratorUtils.size(null));
    }

    /**
     * @target IteratorUtils.size
     * @scenario empty iterator returns size 0
     * @defectRisk incorrect handling of already-exhausted iterator
     */
    @Test(timeout = 4000)
    public void testSizeEmptyIterator() {
        Iterator<Integer> it = Collections.<Integer>emptyList().iterator();
        assertEquals(0, IteratorUtils.size(it));
    }

    /**
     * @target IteratorUtils.isEmpty
     * @scenario null iterator is considered empty
     * @defectRisk NPE instead of returning true
     */
    @Test(timeout = 4000)
    public void testIsEmptyNull() {
        assertTrue(IteratorUtils.isEmpty(null));
    }

    /**
     * @target IteratorUtils.isEmpty
     * @scenario populated iterator is not empty
     * @defectRisk incorrect hasNext delegation
     */
    @Test(timeout = 4000)
    public void testIsEmptyNonEmpty() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        assertFalse(IteratorUtils.isEmpty(it));
    }

    /**
     * @target IteratorUtils.isEmpty
     * @scenario exhausted iterator is empty
     * @defectRisk incorrect state check after exhaustion
     */
    @Test(timeout = 4000)
    public void testIsEmptyExhausted() {
        Iterator<Integer> it = Collections.<Integer>emptyList().iterator();
        assertTrue(IteratorUtils.isEmpty(it));
    }

    /**
     * @target IteratorUtils.contains
     * @scenario iterator containing target object returns true
     * @defectRisk equality check failing for valid match
     */
    @Test(timeout = 4000)
    public void testContainsTrue() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertTrue(IteratorUtils.contains(it, "b"));
    }

    /**
     * @target IteratorUtils.contains
     * @scenario iterator not containing target object returns false
     * @defectRisk false positive due to faulty equality logic
     */
    @Test(timeout = 4000)
    public void testContainsFalse() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        assertFalse(IteratorUtils.contains(it, "z"));
    }

    /**
     * @target IteratorUtils.contains
     * @scenario null iterator returns false for contains
     * @defectRisk NPE instead of returning false gracefully
     */
    @Test(timeout = 4000)
    public void testContainsNullIterator() {
        assertFalse(IteratorUtils.contains(null, "x"));
    }

    /**
     * @target IteratorUtils.get
     * @scenario retrieve element at valid index
     * @defectRisk off-by-one index calculation
     */
    @Test(timeout = 4000)
    public void testGetValidIndex() {
        Iterator<String> it = Arrays.asList("a", "b", "c", "d").iterator();
        String value = IteratorUtils.get(it, 2);
        assertEquals("c", value);
    }

    /**
     * @target IteratorUtils.get
     * @scenario index beyond iterator size throws IndexOutOfBoundsException
     * @defectRisk incorrect exception handling or infinite loop
     */
    @Test(timeout = 4000)
    public void testGetIndexOutOfBounds() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        try {
            IteratorUtils.get(it, 10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.get
     * @scenario negative index throws IndexOutOfBoundsException
     * @defectRisk missing bounds check for negative index
     */
    @Test(timeout = 4000)
    public void testGetNegativeIndex() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        try {
            IteratorUtils.get(it, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toList(Iterator)
     * @scenario convert iterator to list preserving order
     * @defectRisk element order not preserved during conversion
     */
    @Test(timeout = 4000)
    public void testToList() {
        Iterator<String> it = Arrays.asList("x", "y", "z").iterator();
        List<String> result = IteratorUtils.toList(it);
        assertEquals(Arrays.asList("x", "y", "z"), result);
    }

    /**
     * @target IteratorUtils.toList(Iterator, int)
     * @scenario convert iterator to list with specified estimated size
     * @defectRisk estimated size affecting correctness of final list
     */
    @Test(timeout = 4000)
    public void testToListWithEstimatedSize() {
        Iterator<String> it = Arrays.asList("p", "q").iterator();
        List<String> result = IteratorUtils.toList(it, 5);
        assertEquals(Arrays.asList("p", "q"), result);
    }

    /**
     * @target IteratorUtils.toList
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check
     */
    @Test(timeout = 4000)
    public void testToListNullIterator() {
        try {
            IteratorUtils.toList(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toList
     * @scenario estimatedSize less than 1 throws IllegalArgumentException
     * @defectRisk missing bounds validation for estimatedSize
     */
    @Test(timeout = 4000)
    public void testToListInvalidEstimatedSize() {
        Iterator<String> it = Arrays.asList("a").iterator();
        try {
            IteratorUtils.toList(it, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toArray(Iterator)
     * @scenario convert iterator to Object array
     * @defectRisk element order or count mismatch in resulting array
     */
    @Test(timeout = 4000)
    public void testToArraySimple() {
        Iterator<String> it = Arrays.asList("m", "n").iterator();
        Object[] arr = IteratorUtils.toArray(it);
        assertArrayEquals(new Object[]{"m", "n"}, arr);
    }

    /**
     * @target IteratorUtils.toArray
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check for iterator
     */
    @Test(timeout = 4000)
    public void testToArrayNullIterator() {
        try {
            IteratorUtils.toArray(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toArray(Iterator, Class)
     * @scenario convert iterator to typed array using specified class
     * @defectRisk incorrect array type or ClassCastException
     */
    @Test(timeout = 4000)
    public void testToArrayWithClass() {
        Iterator<String> it = Arrays.asList("foo", "bar").iterator();
        String[] arr = IteratorUtils.toArray(it, String.class);
        assertArrayEquals(new String[]{"foo", "bar"}, arr);
    }

    /**
     * @target IteratorUtils.toArray(Iterator, Class)
     * @scenario null arrayClass throws NullPointerException
     * @defectRisk missing null check for arrayClass parameter
     */
    @Test(timeout = 4000)
    public void testToArrayNullClass() {
        Iterator<String> it = Arrays.asList("a").iterator();
        try {
            IteratorUtils.toArray(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toArray(Iterator, Class)
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check for iterator parameter
     */
    @Test(timeout = 4000)
    public void testToArrayWithClassNullIterator() {
        try {
            IteratorUtils.toArray(null, String.class);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario null object returns empty iterator
     * @defectRisk incorrect handling of null input
     */
    @Test(timeout = 4000)
    public void testGetIteratorNull() {
        Iterator<?> it = IteratorUtils.getIterator(null);
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario Iterator instance passed returns itself
     * @defectRisk unnecessary wrapping of existing iterator
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromIterator() {
        Iterator<String> original = Arrays.asList("a").iterator();
        Iterator<?> result = IteratorUtils.getIterator(original);
        assertSame(original, result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario Collection input returns its iterator
     * @defectRisk collection branch not correctly delegating
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromCollection() {
        Collection<String> coll = Arrays.asList("x", "y");
        Iterator<?> it = IteratorUtils.getIterator(coll);
        List<Object> result = new ArrayList<Object>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario Object array input returns array iterator
     * @defectRisk incorrect array detection branch logic
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromObjectArray() {
        String[] arr = {"a", "b"};
        Iterator<?> it = IteratorUtils.getIterator(arr);
        List<Object> result = new ArrayList<Object>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario Map input returns iterator over map values
     * @defectRisk map values not correctly extracted
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromMap() {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        Iterator<?> it = IteratorUtils.getIterator(map);
        List<Object> result = new ArrayList<Object>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(1, 2), result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario Enumeration input wrapped into iterator
     * @defectRisk enumeration wrapping incorrect element ordering
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromEnumeration() {
        Vector<String> vector = new Vector<String>();
        vector.add("p");
        vector.add("q");
        Enumeration<String> enumeration = vector.elements();
        Iterator<?> it = IteratorUtils.getIterator(enumeration);
        List<Object> result = new ArrayList<Object>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("p", "q"), result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario primitive array input returns array iterator via reflection
     * @defectRisk isArray() branch not triggered correctly for primitive types
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromPrimitiveArray() {
        int[] arr = {7, 8, 9};
        Iterator<?> it = IteratorUtils.getIterator(arr);
        List<Object> result = new ArrayList<Object>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList(7, 8, 9), result);
    }

    /**
     * @target IteratorUtils.getIterator
     * @scenario plain object without iterator method returns singleton iterator
     * @defectRisk falling through to wrong branch or missing singleton fallback
     */
    @Test(timeout = 4000)
    public void testGetIteratorFromPlainObject() {
        Object obj = new Object();
        Iterator<?> it = IteratorUtils.getIterator(obj);
        assertTrue(it.hasNext());
        assertSame(obj, it.next());
        assertFalse(it.hasNext());
    }

    /**
     * @target IteratorUtils.apply
     * @scenario apply closure to every element in iterator
     * @defectRisk closure not invoked for every element or invoked extra times
     */
    @Test(timeout = 4000)
    public void testApply() {
        final List<Integer> collected = new ArrayList<Integer>();
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Closure<Integer> closure = new Closure<Integer>() {
            @Override
            public void execute(Integer input) {
                collected.add(input);
            }
        };
        IteratorUtils.apply(it, closure);
        assertEquals(Arrays.asList(1, 2, 3), collected);
    }

    /**
     * @target IteratorUtils.apply
     * @scenario null iterator with valid closure does nothing but does not throw
     * @defectRisk NPE incorrectly thrown for null iterator
     */
    @Test(timeout = 4000)
    public void testApplyNullIterator() {
        Closure<Integer> closure = new Closure<Integer>() {
            @Override
            public void execute(Integer input) {
                fail("Should not be called");
            }
        };
        IteratorUtils.apply(null, closure);
    }

    /**
     * @target IteratorUtils.apply
     * @scenario null closure throws NullPointerException
     * @defectRisk missing null check for closure parameter
     */
    @Test(timeout = 4000)
    public void testApplyNullClosure() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.apply(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.find
     * @scenario find first element matching predicate
     * @defectRisk returns wrong element or null when match exists
     */
    @Test(timeout = 4000)
    public void testFindMatch() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4).iterator();
        Predicate<Integer> greaterThanTwo = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object > 2;
            }
        };
        Integer found = IteratorUtils.find(it, greaterThanTwo);
        assertEquals(Integer.valueOf(3), found);
    }

    /**
     * @target IteratorUtils.find
     * @scenario no element matches predicate, returns null
     * @defectRisk incorrectly returning non-null when no match exists
     */
    @Test(timeout = 4000)
    public void testFindNoMatch() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        Predicate<Integer> greaterThanTen = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object > 10;
            }
        };
        Integer found = IteratorUtils.find(it, greaterThanTen);
        assertNull(found);
    }

    /**
     * @target IteratorUtils.find
     * @scenario null iterator returns null
     * @defectRisk NPE instead of returning null gracefully
     */
    @Test(timeout = 4000)
    public void testFindNullIterator() {
        Integer found = IteratorUtils.find(null, TruePredicate.<Integer>truePredicate());
        assertNull(found);
    }

    /**
     * @target IteratorUtils.find
     * @scenario null predicate throws NullPointerException
     * @defectRisk missing null check for predicate
     */
    @Test(timeout = 4000)
    public void testFindNullPredicate() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.find(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.matchesAny
     * @scenario at least one element matches predicate returns true
     * @defectRisk incorrect short-circuit logic causing false negative
     */
    @Test(timeout = 4000)
    public void testMatchesAnyTrue() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Predicate<Integer> isThree = EqualPredicate.equalPredicate(3);
        assertTrue(IteratorUtils.matchesAny(it, isThree));
    }

    /**
     * @target IteratorUtils.matchesAny
     * @scenario no elements match predicate returns false
     * @defectRisk false positive on non-matching iterator
     */
    @Test(timeout = 4000)
    public void testMatchesAnyFalse() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Predicate<Integer> isTen = EqualPredicate.equalPredicate(10);
        assertFalse(IteratorUtils.matchesAny(it, isTen));
    }

    /**
     * @target IteratorUtils.matchesAny
     * @scenario null iterator returns false
     * @defectRisk NPE instead of returning false for null iterator
     */
    @Test(timeout = 4000)
    public void testMatchesAnyNullIterator() {
        assertFalse(IteratorUtils.matchesAny(null, TruePredicate.truePredicate()));
    }

    /**
     * @target IteratorUtils.matchesAny
     * @scenario null predicate throws NullPointerException
     * @defectRisk missing null check for predicate
     */
    @Test(timeout = 4000)
    public void testMatchesAnyNullPredicate() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.matchesAny(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.matchesAll
     * @scenario every element matches predicate returns true
     * @defectRisk incorrect early termination causing false negative
     */
    @Test(timeout = 4000)
    public void testMatchesAllTrue() {
        Iterator<Integer> it = Arrays.asList(2, 4, 6).iterator();
        Predicate<Integer> isEven = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        assertTrue(IteratorUtils.matchesAll(it, isEven));
    }

    /**
     * @target IteratorUtils.matchesAll
     * @scenario at least one element fails predicate returns false
     * @defectRisk false positive when not all elements match
     */
    @Test(timeout = 4000)
    public void testMatchesAllFalse() {
        Iterator<Integer> it = Arrays.asList(2, 3, 6).iterator();
        Predicate<Integer> isEven = new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        assertFalse(IteratorUtils.matchesAll(it, isEven));
    }

    /**
     * @target IteratorUtils.matchesAll
     * @scenario null iterator returns true (vacuous truth)
     * @defectRisk incorrect handling for null returns false instead of true
     */
    @Test(timeout = 4000)
    public void testMatchesAllNullIterator() {
        assertTrue(IteratorUtils.matchesAll(null, TruePredicate.truePredicate()));
    }

    /**
     * @target IteratorUtils.matchesAll
     * @scenario null predicate throws NullPointerException
     * @defectRisk missing null check for predicate
     */
    @Test(timeout = 4000)
    public void testMatchesAllNullPredicate() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.matchesAll(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toString(Iterator)
     * @scenario convert populated iterator to default bracketed comma string
     * @defectRisk incorrect delimiter or bracket placement
     */
    @Test(timeout = 4000)
    public void testToStringDefault() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        String result = IteratorUtils.toString(it);
        assertEquals("[a, b, c]", result);
    }

    /**
     * @target IteratorUtils.toString(Iterator)
     * @scenario empty iterator produces empty brackets
     * @defectRisk trailing delimiter left in output for empty iterator
     */
    @Test(timeout = 4000)
    public void testToStringEmpty() {
        Iterator<String> it = Collections.<String>emptyList().iterator();
        String result = IteratorUtils.toString(it);
        assertEquals("[]", result);
    }

    /**
     * @target IteratorUtils.toString(Iterator, Transformer)
     * @scenario custom transformer applied to each element
     * @defectRisk transformer not properly used for element conversion
     */
    @Test(timeout = 4000)
    public void testToStringWithTransformer() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return "N" + input;
            }
        };
        String result = IteratorUtils.toString(it, transformer);
        assertEquals("[N1, N2, N3]", result);
    }

    /**
     * @target IteratorUtils.toString(Iterator, Transformer, String, String, String)
     * @scenario custom delimiter, prefix, suffix applied
     * @defectRisk incorrect assembly of custom prefix/suffix/delimiter
     */
    @Test(timeout = 4000)
    public void testToStringWithCustomFormat() {
        Iterator<Integer> it = Arrays.asList(1, 2).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return String.valueOf(input);
            }
        };
        String result = IteratorUtils.toString(it, transformer, "-", "<", ">");
        assertEquals("<1-2>", result);
    }

    /**
     * @target IteratorUtils.toString
     * @scenario null iterator with valid transformer produces empty brackets
     * @defectRisk NPE thrown instead of gracefully handling null iterator
     */
    @Test(timeout = 4000)
    public void testToStringNullIterator() {
        String result = IteratorUtils.toString(null);
        assertEquals("[]", result);
    }

    /**
     * @target IteratorUtils.toString
     * @scenario null transformer throws NullPointerException
     * @defectRisk missing null check for transformer parameter
     */
    @Test(timeout = 4000)
    public void testToStringNullTransformer() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.toString(it, null, ",", "[", "]");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toString
     * @scenario null delimiter throws NullPointerException
     * @defectRisk missing null check for delimiter parameter
     */
    @Test(timeout = 4000)
    public void testToStringNullDelimiter() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return String.valueOf(input);
            }
        };
        try {
            IteratorUtils.toString(it, transformer, null, "[", "]");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toString
     * @scenario null prefix throws NullPointerException
     * @defectRisk missing null check for prefix parameter
     */
    @Test(timeout = 4000)
    public void testToStringNullPrefix() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return String.valueOf(input);
            }
        };
        try {
            IteratorUtils.toString(it, transformer, ",", null, "]");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toString
     * @scenario null suffix throws NullPointerException
     * @defectRisk missing null check for suffix parameter
     */
    @Test(timeout = 4000)
    public void testToStringNullSuffix() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return String.valueOf(input);
            }
        };
        try {
            IteratorUtils.toString(it, transformer, ",", "[", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.unmodifiableIterator
     * @scenario wrapped iterator throws UnsupportedOperationException on remove
     * @defectRisk remove() not properly blocked
     */
    @Test(timeout = 4000)
    public void testUnmodifiableIterator() {
        List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
        Iterator<String> baseIt = list.iterator();
        Iterator<String> unmodIt = IteratorUtils.unmodifiableIterator(baseIt);
        assertTrue(unmodIt.hasNext());
        assertEquals("a", unmodIt.next());
        try {
            unmodIt.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.unmodifiableListIterator
     * @scenario wrapped list iterator throws exception on add/set/remove
     * @defectRisk mutating operations not properly blocked
     */
    @Test(timeout = 4000)
    public void testUnmodifiableListIterator() {
        List<String> list = new ArrayList<String>(Arrays.asList("x", "y"));
        ListIterator<String> baseIt = list.listIterator();
        ListIterator<String> unmodIt = IteratorUtils.unmodifiableListIterator(baseIt);
        assertEquals("x", unmodIt.next());
        try {
            unmodIt.set("z");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        try {
            unmodIt.add("z");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.unmodifiableMapIterator
     * @scenario wrapped map iterator throws exception on setValue/remove
     * @defectRisk mutating operations not properly blocked for map iterator
     */
    @Test(timeout = 4000)
    public void testUnmodifiableMapIterator() {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("k1", 1);
        MapIterator<String, Integer> baseIt = new org.apache.commons.collections4.iterators.EntrySetMapIterator<String, Integer>(map);
        MapIterator<String, Integer> unmodIt = IteratorUtils.unmodifiableMapIterator(baseIt);
        assertTrue(unmodIt.hasNext());
        unmodIt.next();
        try {
            unmodIt.setValue(99);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asIterator(Enumeration)
     * @scenario wrap enumeration and read all elements as iterator
     * @defectRisk enumeration wrapping producing wrong sequence
     */
    @Test(timeout = 4000)
    public void testAsIteratorFromEnumeration() {
        Vector<String> vector = new Vector<String>(Arrays.asList("a", "b"));
        Enumeration<String> enumeration = vector.elements();
        Iterator<String> it = IteratorUtils.asIterator(enumeration);
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target IteratorUtils.asIterator(Enumeration)
     * @scenario null enumeration throws NullPointerException
     * @defectRisk missing null check
     */
    @Test(timeout = 4000)
    public void testAsIteratorNullEnumeration() {
        try {
            IteratorUtils.asIterator(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asIterator(Enumeration, Collection)
     * @scenario remove() delegates removal to backing collection
     * @defectRisk remove not properly linked to removeCollection
     */
    @Test(timeout = 4000)
    public void testAsIteratorWithRemoveCollection() {
        Vector<String> vector = new Vector<String>(Arrays.asList("a", "b"));
        Enumeration<String> enumeration = vector.elements();
        List<String> removeColl = new ArrayList<String>(Arrays.asList("a", "b"));
        Iterator<String> it = IteratorUtils.asIterator(enumeration, removeColl);
        assertEquals("a", it.next());
        it.remove();
        assertFalse(removeColl.contains("a"));
    }

    /**
     * @target IteratorUtils.asIterator(Enumeration, Collection)
     * @scenario null enumeration throws NullPointerException
     * @defectRisk missing null check for enumeration parameter
     */
    @Test(timeout = 4000)
    public void testAsIteratorWithRemoveCollectionNullEnumeration() {
        try {
            IteratorUtils.asIterator(null, new ArrayList<String>());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asIterator(Enumeration, Collection)
     * @scenario null removeCollection throws NullPointerException
     * @defectRisk missing null check for removeCollection parameter
     */
    @Test(timeout = 4000)
    public void testAsIteratorWithRemoveCollectionNullCollection() {
        Vector<String> vector = new Vector<String>(Arrays.asList("a"));
        Enumeration<String> enumeration = vector.elements();
        try {
            IteratorUtils.asIterator(enumeration, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asEnumeration
     * @scenario wrap iterator into enumeration and read elements
     * @defectRisk enumeration not correctly delegating to iterator
     */
    @Test(timeout = 4000)
    public void testAsEnumeration() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Enumeration<String> enumeration = IteratorUtils.asEnumeration(it);
        List<String> result = new ArrayList<String>();
        while (enumeration.hasMoreElements()) {
            result.add(enumeration.nextElement());
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target IteratorUtils.asEnumeration
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check for iterator parameter
     */
    @Test(timeout = 4000)
    public void testAsEnumerationNullIterator() {
        try {
            IteratorUtils.asEnumeration(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asIterable
     * @scenario wrap iterator into single-use iterable and traverse with for-each
     * @defectRisk iterable not correctly wrapping underlying iterator
     */
    @Test(timeout = 4000)
    public void testAsIterable() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Iterable<String> iterable = IteratorUtils.asIterable(it);
        List<String> result = new ArrayList<String>();
        for (String s : iterable) {
            result.add(s);
        }
        assertEquals(Arrays.asList("a", "b"), result);
    }

    /**
     * @target IteratorUtils.asIterable
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check
     */
    @Test(timeout = 4000)
    public void testAsIterableNullIterator() {
        try {
            IteratorUtils.asIterable(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.asMultipleUseIterable
     * @scenario wrap iterator to allow multiple iterations
     * @defectRisk second iteration failing to reproduce elements
     */
    @Test(timeout = 4000)
    public void testAsMultipleUseIterable() {
        Iterator<String> it = Arrays.asList("a", "b").iterator();
        Iterable<String> iterable = IteratorUtils.asMultipleUseIterable(it);
        List<String> firstPass = new ArrayList<String>();
        for (String s : iterable) {
            firstPass.add(s);
        }
        List<String> secondPass = new ArrayList<String>();
        for (String s : iterable) {
            secondPass.add(s);
        }
        assertEquals(Arrays.asList("a", "b"), firstPass);
        assertEquals(Arrays.asList("a", "b"), secondPass);
    }

    /**
     * @target IteratorUtils.asMultipleUseIterable
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check
     */
    @Test(timeout = 4000)
    public void testAsMultipleUseIterableNullIterator() {
        try {
            IteratorUtils.asMultipleUseIterable(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.toListIterator
     * @scenario wrap simple iterator into list iterator supporting bidirectional traversal
     * @defectRisk previous()/next() not properly cached in wrapper
     */
    @Test(timeout = 4000)
    public void testToListIterator() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        ListIterator<String> listIt = IteratorUtils.toListIterator(it);
        assertEquals("a", listIt.next());
        assertEquals("b", listIt.next());
        assertEquals("b", listIt.previous());
        assertEquals("a", listIt.previous());
    }

    /**
     * @target IteratorUtils.toListIterator
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check
     */
    @Test(timeout = 4000)
    public void testToListIteratorNullIterator() {
        try {
            IteratorUtils.toListIterator(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.boundedIterator(Iterator, long)
     * @scenario limit iterator to max elements
     * @defectRisk bounded iterator returning too many or too few elements
     */
    @Test(timeout = 4000)
    public void testBoundedIteratorMax() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        BoundedIterator<Integer> bounded = IteratorUtils.boundedIterator(it, 3);
        List<Integer> result = new ArrayList<Integer>();
        while (bounded.hasNext()) {
            result.add(bounded.next());
        }
        assertEquals(Arrays.asList(1, 2, 3), result);
    }

    /**
     * @target IteratorUtils.boundedIterator(Iterator, long, long)
     * @scenario offset and limit applied together
     * @defectRisk offset skip logic incorrect
     */
    @Test(timeout = 4000)
    public void testBoundedIteratorOffsetMax() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        BoundedIterator<Integer> bounded = IteratorUtils.boundedIterator(it, 1, 2);
        List<Integer> result = new ArrayList<Integer>();
        while (bounded.hasNext()) {
            result.add(bounded.next());
        }
        assertEquals(Arrays.asList(2, 3), result);
    }

    /**
     * @target IteratorUtils.skippingIterator
     * @scenario skip first N elements before returning rest
     * @defectRisk skip offset miscount
     */
    @Test(timeout = 4000)
    public void testSkippingIterator() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3, 4, 5).iterator();
        SkippingIterator<Integer> skipIt = IteratorUtils.skippingIterator(it, 2);
        List<Integer> result = new ArrayList<Integer>();
        while (skipIt.hasNext()) {
            result.add(skipIt.next());
        }
        assertEquals(Arrays.asList(3, 4, 5), result);
    }

    /**
     * @target IteratorUtils.zippingIterator(Iterator, Iterator)
     * @scenario interleave elements from two iterators
     * @defectRisk incorrect interleaving order
     */
    @Test(timeout = 4000)
    public void testZippingIteratorTwo() {
        Iterator<String> it1 = Arrays.asList("a", "c").iterator();
        Iterator<String> it2 = Arrays.asList("b", "d").iterator();
        ZippingIterator<String> zipIt = IteratorUtils.zippingIterator(it1, it2);
        List<String> result = new ArrayList<String>();
        while (zipIt.hasNext()) {
            result.add(zipIt.next());
        }
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    /**
     * @target IteratorUtils.zippingIterator(Iterator, Iterator, Iterator)
     * @scenario interleave elements from three iterators
     * @defectRisk incorrect interleaving order for three sources
     */
    @Test(timeout = 4000)
    public void testZippingIteratorThree() {
        Iterator<String> it1 = Arrays.asList("a").iterator();
        Iterator<String> it2 = Arrays.asList("b").iterator();
        Iterator<String> it3 = Arrays.asList("c").iterator();
        ZippingIterator<String> zipIt = IteratorUtils.zippingIterator(it1, it2, it3);
        List<String> result = new ArrayList<String>();
        while (zipIt.hasNext()) {
            result.add(zipIt.next());
        }
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    /**
     * @target IteratorUtils.zippingIterator(Iterator...)
     * @scenario interleave elements from array of iterators varargs
     * @defectRisk incorrect interleaving for varargs array signature
     */
    @Test(timeout = 4000)
    public void testZippingIteratorVarargs() {
        Iterator<String> it1 = Arrays.asList("x").iterator();
        Iterator<String> it2 = Arrays.asList("y").iterator();
        @SuppressWarnings("unchecked")
        ZippingIterator<String> zipIt = IteratorUtils.zippingIterator(it1, it2);
        List<String> result = new ArrayList<String>();
        while (zipIt.hasNext()) {
            result.add(zipIt.next());
        }
        assertEquals(Arrays.asList("x", "y"), result);
    }

    /**
     * @target IteratorUtils.objectGraphIterator
     * @scenario traverse simple object graph using transformer
     * @defectRisk traversal logic yielding wrong sequence of leaf elements
     */
    @Test(timeout = 4000)
    public void testObjectGraphIterator() {
        List<Object> root = new ArrayList<Object>();
        root.add("leaf1");
        List<Object> nested = new ArrayList<Object>();
        nested.add("leaf2");
        nested.add("leaf3");
        root.add(nested);

        Transformer<Object, Object> transformer = new Transformer<Object, Object>() {
            @Override
            public Object transform(Object input) {
                if (input instanceof List) {
                    return ((List<?>) input).iterator();
                }
                return input;
            }
        };

        Iterator<Object> graphIt = IteratorUtils.objectGraphIterator(root, transformer);
        List<Object> result = new ArrayList<Object>();
        while (graphIt.hasNext()) {
            result.add(graphIt.next());
        }
        assertEquals(Arrays.asList("leaf1", "leaf2", "leaf3"), result);
    }

    /**
     * @target IteratorUtils.transformedIterator
     * @scenario transform elements from Integer to String
     * @defectRisk transformation not applied correctly to each element
     */
    @Test(timeout = 4000)
    public void testTransformedIterator() {
        Iterator<Integer> it = Arrays.asList(1, 2, 3).iterator();
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return "val" + input;
            }
        };
        Iterator<String> transformedIt = IteratorUtils.transformedIterator(it, transformer);
        List<String> result = new ArrayList<String>();
        while (transformedIt.hasNext()) {
            result.add(transformedIt.next());
        }
        assertEquals(Arrays.asList("val1", "val2", "val3"), result);
    }

    /**
     * @target IteratorUtils.transformedIterator
     * @scenario null iterator throws NullPointerException
     * @defectRisk missing null check for iterator parameter
     */
    @Test(timeout = 4000)
    public void testTransformedIteratorNullIterator() {
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            @Override
            public String transform(Integer input) {
                return String.valueOf(input);
            }
        };
        try {
            IteratorUtils.transformedIterator(null, transformer);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.transformedIterator
     * @scenario null transformer throws NullPointerException
     * @defectRisk missing null check for transformer parameter
     */
    @Test(timeout = 4000)
    public void testTransformedIteratorNullTransformer() {
        Iterator<Integer> it = Arrays.asList(1).iterator();
        try {
            IteratorUtils.transformedIterator(it, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.nodeListIterator(NodeList)
     * @scenario null NodeList throws NullPointerException
     * @defectRisk missing null check for NodeList parameter
     */
    @Test(timeout = 4000)
    public void testNodeListIteratorNullNodeList() {
        try {
            IteratorUtils.nodeListIterator((org.w3c.dom.NodeList) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.nodeListIterator(Node)
     * @scenario null Node throws NullPointerException
     * @defectRisk missing null check for Node parameter
     */
    @Test(timeout = 4000)
    public void testNodeListIteratorNullNode() {
        try {
            IteratorUtils.nodeListIterator((org.w3c.dom.Node) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @target IteratorUtils.EMPTY_ITERATOR constant
     * @scenario verify constant behaves as empty iterator
     * @defectRisk static constant improperly initialized
     */
    @Test(timeout = 4000)
    public void testEmptyIteratorConstant() {
        assertFalse(IteratorUtils.EMPTY_ITERATOR.hasNext());
    }

    /**
     * @target IteratorUtils.EMPTY_LIST_ITERATOR constant
     * @scenario verify constant behaves as empty list iterator
     * @defectRisk static constant improperly initialized
     */
    @Test(timeout = 4000)
    public void testEmptyListIteratorConstant() {
        assertFalse(IteratorUtils.EMPTY_LIST_ITERATOR.hasNext());
        assertFalse(IteratorUtils.EMPTY_LIST_ITERATOR.hasPrevious());
    }

    /**
     * @target IteratorUtils.EMPTY_ORDERED_ITERATOR constant
     * @scenario verify constant behaves as empty ordered iterator
     * @defectRisk static constant improperly initialized
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedIteratorConstant() {
        assertFalse(IteratorUtils.EMPTY_ORDERED_ITERATOR.hasNext());
    }

    /**
     * @target IteratorUtils.EMPTY_MAP_ITERATOR constant
     * @scenario verify constant behaves as empty map iterator
     * @defectRisk static constant improperly initialized
     */
    @Test(timeout = 4000)
    public void testEmptyMapIteratorConstant() {
        assertFalse(IteratorUtils.EMPTY_MAP_ITERATOR.hasNext());
    }

    /**
     * @target IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR constant
     * @scenario verify constant behaves as empty ordered map iterator
     * @defectRisk static constant improperly initialized
     */
    @Test(timeout = 4000)
    public void testEmptyOrderedMapIteratorConstant() {
        assertFalse(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasNext());
        assertFalse(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasPrevious());
    }

    /**
     * @target IteratorUtils.collatedIterator with explicit comparator
     * @scenario merge two sorted iterators using explicit natural-order Comparator
     * @defectRisk explicit comparator path producing incorrect merge order
     */
    @Test(timeout = 4000)
    public void testCollatedIteratorWithExplicitComparator() {
        Iterator<Integer> it1 = Arrays.asList(1, 4, 7).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 5, 8).iterator();
        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        Iterator<Integer> collated = IteratorUtils.collatedIterator(comparator, it1, it2);
        List<Integer> result = new ArrayList<Integer>();
        while (collated.hasNext()) {
            result.add(collated.next());
        }
        assertEquals(Arrays.asList(1, 2, 4, 5, 7, 8), result);
    }
}