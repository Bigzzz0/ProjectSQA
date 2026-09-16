package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * White-box test suite for Expression class.
 * 
 * /* [Branch & Defect Analysis Matrix] */
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   Branch: isContextDependent() lazy initialization
 *   - contextDependencyKnown = false -> computeContextDependent() called, cached
 *   - contextDependencyKnown = true -> return cached value
 *   Branch: computeContextDependent() abstract, called exactly once
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - iterate() with null result, EvalContext result, non-iterable result
 *   - iteratePointers() with null result, EvalContext result, non-iterable result, empty list
 *   - PointerIterator and ValueIterator with null/empty iterators
 * 
 * Partition C: Defect-Targeted Branch Zone (Defects4J #4)
 *   - ValueIterator.next() must unwrap Pointer.getValue(), not return raw Pointer
 *   - Known bug: ValueIterator returns Pointer objects instead of unwrapped values
 *   - Test: iterate() producing EvalContext with Pointer elements -> should unwrap to values
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - UnsupportedOperationException on remove() for both iterators
 *   - iteratePointers() returning empty iterator when result is null
 *   - PointerIterator constructor with null locale
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Iterator hasNext()/next() contract compliance
 *   - State consistency across multiple calls to isContextDependent()
 */
public class ExpressionDeepseekTest {

    // ======================== Partition A: Core Functional Logic & State Transitions ========================

    @Test(timeout = 4000)
    public void testIsContextDependentInitialFalse() {
        TestExpression expr = new TestExpression(false);
        assertFalse("First call should compute and return false", expr.isContextDependent());
        assertTrue("Flag should be set to known", expr.contextDependencyKnown);
    }

    @Test(timeout = 4000)
    public void testIsContextDependentInitialTrue() {
        TestExpression expr = new TestExpression(true);
        assertTrue("First call should compute and return true", expr.isContextDependent());
        assertTrue("Flag should be set to known", expr.contextDependencyKnown);
    }

    @Test(timeout = 4000)
    public void testIsContextDependentCaching() {
        TestExpression expr = new TestExpression(true);
        expr.isContextDependent(); // first call sets cache
        expr.computeCallCount = 0;
        boolean result = expr.isContextDependent(); // second call should use cache
        assertTrue("Should use cached value", result);
        assertEquals("computeContextDependent should not be called again", 0, expr.computeCallCount);
    }

    @Test(timeout = 4000)
    public void testIsContextDependentCacheFalse() {
        TestExpression expr = new TestExpression(false);
        expr.isContextDependent(); // first call sets cache
        expr.computeCallCount = 0;
        boolean result = expr.isContextDependent(); // second call should use cache
        assertFalse("Should use cached value", result);
        assertEquals("computeContextDependent should not be called again", 0, expr.computeCallCount);
    }

    // ======================== Partition B: Boundary Value Analysis & Extremes ========================

    @Test(timeout = 4000)
    public void testIterateWithEvalContextResult() {
        // Simulate compute() returning an EvalContext
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                List<Pointer> pointers = new ArrayList<>();
                pointers.add(new TestPointer("value1"));
                pointers.add(new TestPointer("value2"));
                TestEvalContext evalCtx = new TestEvalContext(pointers);
                return evalCtx;
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iterate(ctx);
        assertTrue("Iterator should have elements from EvalContext", result.hasNext());
        assertEquals("First element should be unwrapped value", "value1", result.next());
        assertTrue("Should have second element", result.hasNext());
        assertEquals("Second element should be unwrapped value", "value2", result.next());
        assertFalse("Should have only two elements", result.hasNext());
    }

    @Test(timeout = 4000)
    public void testIterateWithNonIterableResult() {
        // Simulate compute() returning a non-iterable object that ValueUtils.iterate can handle
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                return "single value";
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iterate(ctx);
        assertTrue("Iterator should have one element from string", result.hasNext());
        assertEquals("Should return string directly", "single value", result.next());
        assertFalse("Should have only one element", result.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithNullResult() {
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                return null;
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iteratePointers(ctx);
        assertFalse("Should return empty iterator for null result", result.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithEvalContextResult() {
        // Simulate compute() returning an EvalContext directly
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                List<Pointer> pointers = new ArrayList<>();
                pointers.add(new TestPointer("p1"));
                TestEvalContext evalCtx = new TestEvalContext(pointers);
                return evalCtx;
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iteratePointers(ctx);
        assertTrue("Should return EvalContext directly", result.hasNext());
        Object first = result.next();
        assertTrue("Should be a Pointer", first instanceof Pointer);
        assertEquals("Pointer value should be p1", "p1", ((Pointer) first).getValue());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithNonIteratorResult() {
        // Simulate compute() returning a non-iterator result that gets wrapped in PointerIterator
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                return "test string";
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iteratePointers(ctx);
        assertTrue("Should have one element", result.hasNext());
        Object obj = result.next();
        assertTrue("Should be a Pointer", obj instanceof Pointer);
        assertEquals("Pointer value should be test string", "test string", ((Pointer) obj).getValue());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithEmptyListFromIterate() {
        // Simulate compute() returning an empty collection
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                return Collections.emptyList();
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iteratePointers(ctx);
        assertFalse("Should return empty iterator", result.hasNext());
    }

    // ======================== Partition C: Defect-Targeted Branch Zone ========================

    @Test(timeout = 4000)
    public void testValueIteratorUnwrapsPointers() {
        // This test targets the known Defects4J defect:
        // When iterator.next() returns a Pointer, ValueIterator should unwrap to getValue()
        // The bug: it returns the Pointer object instead of its value
        List<Pointer> pointers = new ArrayList<>();
        pointers.add(new TestPointer("value1"));
        pointers.add(new TestPointer("value2"));
        
        Iterator<Pointer> rawIter = pointers.iterator();
        ValueIterator vi = new Expression.ValueIterator(rawIter);
        
        assertTrue("Should have first element", vi.hasNext());
        Object first = vi.next();
        assertFalse("First element should not be a Pointer (should be unwrapped)", first instanceof Pointer);
        assertEquals("First element should be 'value1'", "value1", first);
        
        assertTrue("Should have second element", vi.hasNext());
        Object second = vi.next();
        assertFalse("Second element should not be a Pointer (should be unwrapped)", second instanceof Pointer);
        assertEquals("Second element should be 'value2'", "value2", second);
        
        assertFalse("Should have only two elements", vi.hasNext());
    }

    @Test(timeout = 4000)
    public void testIterateDefectRevealing() {
        // Simulate the exact scenario from Defects4J ExtensionFunctionTest::testNodeSetReturn
        // The bug: iterate() through EvalContext returns Pointer objects, not values
        TestExpression expr = new TestExpression(false) {
            @Override
            public Object compute(EvalContext context) {
                List<Pointer> pointers = new ArrayList<>();
                pointers.add(new TestPointer("Nested: Name 1"));
                pointers.add(new TestPointer("Nested: Name 2"));
                TestEvalContext evalCtx = new TestEvalContext(pointers);
                return evalCtx;
            }
        };
        EvalContext ctx = new TestEvalContext(Collections.emptyList());
        Iterator result = expr.iterate(ctx);
        assertTrue("Should have first element", result.hasNext());
        Object first = result.next();
        assertFalse("First should NOT be a Pointer (defect check)", first instanceof Pointer);
        assertEquals("First should be Nested: Name 1", "Nested: Name 1", first);
        
        assertTrue("Should have second element", result.hasNext());
        Object second = result.next();
        assertFalse("Second should NOT be a Pointer (defect check)", second instanceof Pointer);
        assertEquals("Second should be Nested: Name 2", "Nested: Name 2", second);
        
        assertFalse("Should have only two elements", result.hasNext());
    }

    @Test(timeout = 4000)
    public void testValueIteratorWithNonPointerObjects() {
        // Ensure ValueIterator works with non-Pointer objects too
        List<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        
        Iterator<String> rawIter = strings.iterator();
        ValueIterator vi = new Expression.ValueIterator(rawIter);
        
        assertEquals("First element should be 'a'", "a", vi.next());
        assertEquals("Second element should be 'b'", "b", vi.next());
        assertFalse("Should have only two elements", vi.hasNext());
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testValueIteratorRemoveThrowsUnsupported() {
        List<String> list = new ArrayList<>();
        list.add("test");
        ValueIterator vi = new Expression.ValueIterator(list.iterator());
        vi.next(); // advance to have element to remove
        vi.remove();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPointerIteratorRemoveThrowsUnsupported() {
        List<String> list = new ArrayList<>();
        list.add("test");
        PointerIterator pi = new Expression.PointerIterator(
            list.iterator(), new QName(null, "value"), Locale.US);
        pi.next(); // advance to have element to remove
        pi.remove();
    }

    @Test(timeout = 4000)
    public void testPointerIteratorWithNullLocale() {
        List<String> list = new ArrayList<>();
        list.add("test");
        PointerIterator pi = new Expression.PointerIterator(
            list.iterator(), new QName(null, "value"), null);
        assertTrue("Should have element", pi.hasNext());
        Object obj = pi.next();
        assertTrue("Should be a Pointer", obj instanceof Pointer);
        assertEquals("Pointer value should be 'test'", "test", ((Pointer) obj).getValue());
    }

    @Test(timeout = 4000)
    public void testPointerIteratorWithExistingPointer() {
        List<Pointer> pointers = new ArrayList<>();
        pointers.add(new TestPointer("existing"));
        PointerIterator pi = new Expression.PointerIterator(
            pointers.iterator(), new QName(null, "value"), Locale.US);
        assertTrue("Should have element", pi.hasNext());
        Object obj = pi.next();
        assertTrue("Should be a Pointer", obj instanceof Pointer);
        assertEquals("Should return same Pointer", pointers.get(0), obj);
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================

    @Test(timeout = 4000)
    public void testIteratorHasNextAfterFullConsumption() {
        List<String> list = new ArrayList<>();
        list.add("only");
        ValueIterator vi = new Expression.ValueIterator(list.iterator());
        assertTrue(vi.hasNext());
        vi.next();
        assertFalse("Should be exhausted", vi.hasNext());
    }

    @Test(timeout = 4000)
    public void testPointerIteratorHasNextAfterFullConsumption() {
        List<String> list = new ArrayList<>();
        list.add("only");
        PointerIterator pi = new Expression.PointerIterator(
            list.iterator(), new QName(null, "value"), Locale.US);
        assertTrue(pi.hasNext());
        pi.next();
        assertFalse("Should be exhausted", pi.hasNext());
    }

    @Test(timeout = 4000)
    public void testEmptyValueIterator() {
        List<String> empty = Collections.emptyList();
        ValueIterator vi = new Expression.ValueIterator(empty.iterator());
        assertFalse("Empty iterator should have no elements", vi.hasNext());
    }

    @Test(timeout = 4000)
    public void testEmptyPointerIterator() {
        List<String> empty = Collections.emptyList();
        PointerIterator pi = new Expression.PointerIterator(
            empty.iterator(), new QName(null, "value"), Locale.US);
        assertFalse("Empty iterator should have no elements", pi.hasNext());
    }

    @Test(timeout = 4000)
    public void testMultipleIsContextDependentCalls() {
        TestExpression expr = new TestExpression(true);
        // Multiple calls should all return same cached value
        assertTrue(expr.isContextDependent());
        assertTrue(expr.isContextDependent());
        assertTrue(expr.isContextDependent());
        assertEquals("computeContextDependent should only be called once", 1, expr.computeCallCount);
    }

    // ======================== Helper Classes ========================

    /**
     * Test implementation of abstract Expression for testing purposes.
     */
    private static class TestExpression extends Expression {
        private final boolean contextDependent;
        private int computeCallCount = 0;

        TestExpression(boolean contextDependent) {
            this.contextDependent = contextDependent;
        }

        @Override
        public boolean computeContextDependent() {
            computeCallCount++;
            return contextDependent;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return null;
        }

        @Override
        public Object compute(EvalContext context) {
            return null;
        }
    }

    /**
     * Simple Pointer implementation for testing.
     */
    private static class TestPointer implements Pointer {
        private final Object value;

        TestPointer(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getNode() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getRootNode() {
            return value;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isActual() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public Object getImmediateValue() {
            return value;
        }

        @Override
        public Pointer getImmediatePointer() {
            return this;
        }

        @Override
        public Pointer getPointer() {
            return this;
        }

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String asPath() {
            return "/test";
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    /**
     * Simplified EvalContext for testing purposes.
     */
    private static class TestEvalContext extends EvalContext {
        private final Iterator<?> iterator;
        private final List<Pointer> pointers;

        TestEvalContext(List<Pointer> pointers) {
            super(null, null); // minimal constructor for testing
            this.pointers = pointers;
            this.iterator = pointers.iterator();
        }

        @Override
        public Object getCurrentNodePointer() {
            return pointers.isEmpty() ? null : pointers.get(0);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EvalContext getRootContext() {
            return this;
        }
    }
}