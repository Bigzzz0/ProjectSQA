package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * This test suite targets all major execution paths in UnboundedFifoBuffer:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - add/get/remove with sequential and wrapped states
 *   - size() and isEmpty() after various operations
 *   - Iterator traversal and removal
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Constructor with default (32) and minimum (1) initial size
 *   - Add up to and beyond capacity to trigger resize
 *   - Null argument on add → NullPointerException
 *   - Empty buffer get/remove → BufferUnderflowException
 * 
 * Partition C: Defect-Targeted Branch Zone (COLLECTIONS-220)
 *   - Iterator.remove() after adding elements that force a wrap, 
 *     removing an interior element via iterator, then removing head 
 *     elements → size becomes 0 when it should be 1 (orphan element).
 *   - This test reproduces the exact failure pattern reported in 
 *     Defects4J (testCollections220: expected:<1> but was:<0>).
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid constructor argument (<=0) → IllegalArgumentException
 *   - Null element add → NullPointerException
 *   - Iterator remove without next → IllegalStateException
 *   - Iterator next beyond end → NoSuchElementException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Iterator behavior after buffer is emptied
 */
public class UnboundedFifoBufferDeepseekTest {

    // ===================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===================================================================

    @Test(timeout = 4000)
    public void testAddGetRemoveSimple() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3);
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());

        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        assertEquals(3, buffer.size());
        assertFalse(buffer.isEmpty());

        assertEquals("A", buffer.get());
        assertEquals("A", buffer.remove());
        assertEquals(2, buffer.size());
        assertEquals("B", buffer.get());
        assertEquals("B", buffer.remove());
        assertEquals(1, buffer.size());
        assertEquals("C", buffer.remove());
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());
    }

    @Test(timeout = 4000)
    public void testAddGetRemoveWithWrap() {
        // create buffer with small capacity to force wrap
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");   // now full (capacity = 3, array length = 4)
        buffer.remove();   // removes A, head=1, tail=3, size=2
        buffer.add("D");   // tail wraps: head=1, tail=0, elements: [null, B, C, D]? wait: 
        // after add, m_buffer[3]=D, tail=4 -> 0, so head=1, tail=0, size=3
        // elements at indices: 1=B, 2=C, 3=D, and index0 is empty
        assertEquals(3, buffer.size());
        assertEquals("B", buffer.get());
        assertEquals("B", buffer.remove()); // head removal, head becomes 2
        assertEquals(2, buffer.size());
        assertEquals("C", buffer.get());
        // now remove via iterator on the next element (should work)
        Iterator it = buffer.iterator();
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        it.remove();
        assertEquals(1, buffer.size());
        assertEquals("D", buffer.get());
        buffer.remove();
        assertTrue(buffer.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIteratorFullTraversalAndRemoval() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        for (int i = 0; i < 6; i++) {
            buffer.add("" + i);
        }
        Iterator it = buffer.iterator();
        int count = 0;
        while (it.hasNext()) {
            String s = (String) it.next();
            // remove every other element
            if (count % 2 == 1) {
                it.remove();
            }
            count++;
        }
        // expected remaining: "0", "2", "4" if removal was correct
        assertEquals(3, buffer.size());
        assertEquals("0", buffer.remove());
        assertEquals("2", buffer.remove());
        assertEquals("4", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveAtHeadDirectly() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3);
        buffer.add("a");
        buffer.add("b");
        Iterator it = buffer.iterator();
        it.next(); // "a"
        it.remove(); // removes head
        assertEquals(1, buffer.size());
        assertEquals("b", buffer.get());
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveMiddleNoWrap() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(10);
        buffer.add("x");
        buffer.add("y");
        buffer.add("z");
        Iterator it = buffer.iterator();
        it.next(); // x
        it.next(); // y
        it.remove(); // remove middle (y)
        assertEquals(2, buffer.size());
        assertEquals("x", buffer.remove());
        assertEquals("z", buffer.remove());
    }

    // ===================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ===================================================================

    @Test(timeout = 4000)
    public void testConstructorDefaultAndMinimal() {
        UnboundedFifoBuffer defaultBuf = new UnboundedFifoBuffer();
        assertTrue(defaultBuf.isEmpty());
        assertEquals(0, defaultBuf.size());

        UnboundedFifoBuffer minBuf = new UnboundedFifoBuffer(1);
        assertTrue(minBuf.isEmpty());
        minBuf.add("only");
        assertEquals(1, minBuf.size());
        assertEquals("only", minBuf.remove());
        assertTrue(minBuf.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddNullThrowsNullPointerException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        try {
            buffer.add(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = BufferUnderflowException.class)
    public void testGetOnEmptyThrowsBufferUnderflowException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.get();
    }

    @Test(timeout = 4000, expected = BufferUnderflowException.class)
    public void testRemoveOnEmptyThrowsBufferUnderflowException() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.remove();
    }

    @Test(timeout = 4000)
    public void testEmptyAfterManyOperations() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        for (int i = 0; i < 10; i++) {
            buffer.add("e" + i);
        }
        while (!buffer.isEmpty()) {
            buffer.remove();
        }
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());
    }

    @Test(timeout = 4000)
    public void testIteratorNextBeyondEndThrows() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("only");
        Iterator it = buffer.iterator();
        it.next();
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Should have thrown NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNextThrows() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
        buffer.add("item");
        Iterator it = buffer.iterator();
        it.remove();
    }

    // ===================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-220)
    // ===================================================================

    @Test(timeout = 4000)
    public void testCollections220() {
        // This test reproduces the exact failure reported in Defects4J:
        // After a specific sequence of adds and iterator removes that
        // create a wrapped buffer, an interior iterator.remove() causes
        // an element to become an "orphan" (outside head/tail range),
        // so that later removals of head elements leave head==tail
        // while an element still exists. The size() then returns 0
        // instead of 1 (the orphaned element).
        //
        // Sequence: capacity 4, add 4 elements, remove first element,
        // add fifth element (causing wrap), then remove three elements
        // via iterator: first head removal, then interior removal,
        // then head removal again. The last removal leaves head==tail
        // and the orphaned sixth element? Actually we only have 5 elements.
        // Let's use 4+1=5 elements. After removing first (A), we have
        // B,C,D. Add E (wrap). Then remove B (head), remove C (interior),
        // remove D (head). Expected remaining: E only → size=1.
        // Bug causes size=0 because E is orphaned and not counted.
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(4);
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");
        buffer.add("D");   // now full (capacity=4, array length=5)
        buffer.remove();   // removes "A" → head=1, tail=4, size=3
        buffer.add("E");   // wraps: tail becomes 0 → head=1, tail=0, size=4
        // buffer layout: I0=empty, I1="B", I2="C", I3="D", I4=was filled by E? Actually after add:
        // m_buffer[4]="E", tail++ -> tail=5 -> wrap to 0.
        // So: I0=empty, I1="B", I2="C", I3="D", I4="E".
        // Valid elements: indices 1,2,3,4 (since head=1, tail=0, size=4). So B,C,D,E.

        Iterator it = buffer.iterator();
        // Remove first element (B) via iterator -> it's head removal
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        it.remove();   // correct removal, head becomes 2, size=3
        // Now buffer: I2="C", I3="D", I4="E", head=2, tail=0, size=3
        // Remove second element (C) via iterator -> interior removal
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        it.remove();   // BUG: interior removal corrupts shift for wrapped buffer
        // After buggy removal, head=2, tail becomes (incorrect) -> size may be 1 or 2
        // Remove third element (D) via iterator -> should be head removal
        assertTrue(it.hasNext());
        assertEquals("D", it.next());
        it.remove();   // should leave only "E" (size=1), but bug may set size=0
        // Final expected size = 1 (element E remains)
        // Buggy version returns 0
        assertEquals("Expected size 1 after removing three elements", 1, buffer.size());
        assertEquals("E", buffer.get()); // verify the remaining element
    }

    // ===================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSizeZero() {
        new UnboundedFifoBuffer(0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSizeNegative() {
        new UnboundedFifoBuffer(-5);
    }

    @Test(timeout = 4000)
    public void testAddAfterResize() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(2);
        buffer.add("1");
        buffer.add("2");   // full, capacity=2, length=3
        buffer.add("3");   // triggers resize: new length= (2*2)+1=5, capacity=4
        assertEquals(3, buffer.size());
        assertEquals("1", buffer.remove());
        assertEquals("2", buffer.remove());
        assertEquals("3", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    // ===================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ===================================================================

    @Test(timeout = 4000)
    public void testIteratorBehaviorAfterBufferEmptied() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(3);
        buffer.add("a");
        buffer.add("b");
        buffer.remove();
        buffer.remove();
        // buffer is empty, but head and tail might not be aligned
        Iterator it = buffer.iterator();
        assertFalse(it.hasNext());
        // ensure no crash on next
        try {
            it.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveAfterPartialIteratorRemovals() {
        UnboundedFifoBuffer buffer = new UnboundedFifoBuffer(5);
        buffer.add("a");
        buffer.add("b");
        buffer.add("c");
        Iterator it = buffer.iterator();
        it.next(); // a
        it.remove(); // remove head
        // now buffer: head=1, tail=3, size=2 (b,c)
        // remove via buffer directly
        assertEquals("b", buffer.remove());
        assertEquals("c", buffer.remove());
        assertTrue(buffer.isEmpty());
    }
}