package org.apache.commons.collections4;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.collections4.ComparatorUtils;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Collections_25b_IPOTest {

    @Test(timeout = 4000)
    public void test_collated_iterator_001() throws Exception {
        // Native IPO combination: comparator=natural, left=odd, right=even
        Comparator<Integer> comparator = null;
        List<Integer> left = new ArrayList<Integer>(Arrays.asList(1, 3, 5));
        List<Integer> right = new ArrayList<Integer>(Arrays.asList(2, 4, 6));
        if (comparator != null) { Collections.sort(left, comparator); Collections.sort(right, comparator); }
        Iterator<Integer> iterator = IteratorUtils.collatedIterator(comparator, left.iterator(), right.iterator());
        List<Integer> actual = IteratorUtils.toList(iterator);
        List<Integer> expected = new ArrayList<Integer>();
        expected.addAll(left); expected.addAll(right);
        if (comparator == null) { Collections.sort(expected); } else { Collections.sort(expected, comparator); }
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void test_collated_iterator_002() throws Exception {
        // Native IPO combination: comparator=natural, left=empty, right=single
        Comparator<Integer> comparator = null;
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>(Arrays.asList(4));
        if (comparator != null) { Collections.sort(left, comparator); Collections.sort(right, comparator); }
        Iterator<Integer> iterator = IteratorUtils.collatedIterator(comparator, left.iterator(), right.iterator());
        List<Integer> actual = IteratorUtils.toList(iterator);
        List<Integer> expected = new ArrayList<Integer>();
        expected.addAll(left); expected.addAll(right);
        if (comparator == null) { Collections.sort(expected); } else { Collections.sort(expected, comparator); }
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void test_collated_iterator_003() throws Exception {
        // Native IPO combination: comparator=reverse, left=odd, right=single
        Comparator<Integer> comparator = ComparatorUtils.reversedComparator(ComparatorUtils.<Integer>naturalComparator());
        List<Integer> left = new ArrayList<Integer>(Arrays.asList(1, 3, 5));
        List<Integer> right = new ArrayList<Integer>(Arrays.asList(4));
        if (comparator != null) { Collections.sort(left, comparator); Collections.sort(right, comparator); }
        Iterator<Integer> iterator = IteratorUtils.collatedIterator(comparator, left.iterator(), right.iterator());
        List<Integer> actual = IteratorUtils.toList(iterator);
        List<Integer> expected = new ArrayList<Integer>();
        expected.addAll(left); expected.addAll(right);
        if (comparator == null) { Collections.sort(expected); } else { Collections.sort(expected, comparator); }
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void test_collated_iterator_004() throws Exception {
        // Native IPO combination: comparator=reverse, left=empty, right=even
        Comparator<Integer> comparator = ComparatorUtils.reversedComparator(ComparatorUtils.<Integer>naturalComparator());
        List<Integer> left = new ArrayList<Integer>();
        List<Integer> right = new ArrayList<Integer>(Arrays.asList(2, 4, 6));
        if (comparator != null) { Collections.sort(left, comparator); Collections.sort(right, comparator); }
        Iterator<Integer> iterator = IteratorUtils.collatedIterator(comparator, left.iterator(), right.iterator());
        List<Integer> actual = IteratorUtils.toList(iterator);
        List<Integer> expected = new ArrayList<Integer>();
        expected.addAll(left); expected.addAll(right);
        if (comparator == null) { Collections.sort(expected); } else { Collections.sort(expected, comparator); }
        assertEquals(expected, actual);
    }

}
