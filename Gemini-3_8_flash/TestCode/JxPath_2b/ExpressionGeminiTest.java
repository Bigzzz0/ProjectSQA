package org.apache.commons.jxpath.ri.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.Expression
 *
 * Branch Matrix:
 * 1. isContextDependent():
 *    - branch (!contextDependencyKnown == true): computes and caches contextDependent flag.
 *    - branch (!contextDependencyKnown == false): returns cached contextDependent flag.
 * 2. iterate(EvalContext context):
 *    - branch (result instanceof EvalContext == true): wraps in ValueIterator.
 *    - branch (result instanceof EvalContext == false): delegates to ValueUtils.iterate(result).
 *    - DEFECT BRANCH (result instanceof NodeSet): In defective version, NodeSet is NOT unwrapped by ValueIterator,
 *      causing iterate() to yield Pointer objects instead of pointer values (Ground truth: ExtensionFunctionTest.testNodeSetReturn).
 * 3. iteratePointers(EvalContext context):
 *    - branch (result == null): returns Collections.EMPTY_LIST.iterator().
 *    - branch (result instanceof EvalContext == true): casts and returns result directly.
 *    - branch (else): constructs PointerIterator with ValueUtils.iterate(result), QName("value"), and root context locale.
 * 4. PointerIterator:
 *    - hasNext(): delegates to underlying iterator.
 *    - next(): branch (o instanceof Pointer) returns o; branch (!(o instanceof Pointer)) constructs NodePointer.
 *    - remove(): unconditionally throws UnsupportedOperationException.
 * 5. ValueIterator:
 *    - hasNext(): delegates to underlying iterator.
 *    - next(): branch (o instanceof Pointer) extracts getValue(); branch (!(o instanceof Pointer)) returns o as-is.
 *    - remove(): unconditionally throws UnsupportedOperationException.
 */
public class ExpressionGeminiTest {

    private static class ConcreteTestExpression extends Expression {
        private Object computeResult;
        private Object computeValueResult;
        private boolean contextDependent;
        private int computeContextDependentCount = 0;

        public ConcreteTestExpression(Object computeResult, boolean contextDependent) {
            this.computeResult = computeResult;
            this.contextDependent = contextDependent;
        }

        public void setComputeResult(Object computeResult) {
            this.computeResult = computeResult;
        }

        public void setComputeValueResult(Object computeValueResult) {
            this.computeValueResult = computeValueResult;
        }

        public int getComputeContextDependentCount() {
            return computeContextDependentCount;
        }

        @Override
        public boolean computeContextDependent() {
            computeContextDependentCount++;
            return contextDependent;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return computeValueResult;
        }

        @Override
        public Object compute(EvalContext context) {
            return computeResult;
        }
    }

    private EvalContext createTestEvalContext(Object rootBean) {
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) JXPathContext.newContext(rootBean);
        return jxContext.getAbsoluteRootContext();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsContextDependentCachingTrue() {
        ConcreteTestExpression expr = new ConcreteTestExpression("test", true);
        assertEquals("Initial call count should be 0", 0, expr.getComputeContextDependentCount());

        boolean firstCall = expr.isContextDependent();
        assertTrue("Expected context dependent to be true", firstCall);
        assertEquals("computeContextDependent should be called once", 1, expr.getComputeContextDependentCount());

        boolean secondCall = expr.isContextDependent();
        assertTrue("Cached result should be true", secondCall);
        assertEquals("computeContextDependent should NOT be called again", 1, expr.getComputeContextDependentCount());
    }

    @Test(timeout = 4000)
    public void testIsContextDependentCachingFalse() {
        ConcreteTestExpression expr = new ConcreteTestExpression("test", false);
        assertEquals("Initial call count should be 0", 0, expr.getComputeContextDependentCount());

        boolean firstCall = expr.isContextDependent();
        assertFalse("Expected context dependent to be false", firstCall);
        assertEquals("computeContextDependent should be called once", 1, expr.getComputeContextDependentCount());

        boolean secondCall = expr.isContextDependent();
        assertFalse("Cached result should be false", secondCall);
        assertEquals("computeContextDependent should NOT be called again", 1, expr.getComputeContextDependentCount());
    }

    @Test(timeout = 4000)
    public void testIterateWithEvalContextResult() {
        EvalContext rootContext = createTestEvalContext("beanValue");
        ConcreteTestExpression expr = new ConcreteTestExpression(rootContext, false);

        Iterator it = expr.iterate(rootContext);
        assertTrue("Iterator should be an Expression.ValueIterator", it instanceof Expression.ValueIterator);
        assertTrue("Should have next element", it.hasNext());
        Object value = it.next();
        assertEquals("ValueIterator should unwrap root pointer value", "beanValue", value);
        assertFalse("Should be exhausted", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIterateWithStandardCollectionResult() {
        List<String> data = Arrays.asList("alpha", "beta");
        ConcreteTestExpression expr = new ConcreteTestExpression(data, false);
        EvalContext context = createTestEvalContext("root");

        Iterator it = expr.iterate(context);
        assertTrue("Should have next element", it.hasNext());
        assertEquals("alpha", it.next());
        assertTrue("Should have second element", it.hasNext());
        assertEquals("beta", it.next());
        assertFalse("Iterator should be exhausted", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithEvalContextResult() {
        EvalContext rootContext = createTestEvalContext("root");
        ConcreteTestExpression expr = new ConcreteTestExpression(rootContext, false);

        Iterator it = expr.iteratePointers(rootContext);
        assertSame("When result is EvalContext, iteratePointers must return it directly", rootContext, it);
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithStandardCollectionResult() {
        List<String> data = Arrays.asList("firstValue", "secondValue");
        ConcreteTestExpression expr = new ConcreteTestExpression(data, false);
        EvalContext context = createTestEvalContext("rootBean");

        Iterator it = expr.iteratePointers(context);
        assertTrue("Should return a PointerIterator instance", it instanceof Expression.PointerIterator);
        assertTrue(it.hasNext());

        Object p1 = it.next();
        assertTrue("Elements should be converted to NodePointer", p1 instanceof NodePointer);
        NodePointer np1 = (NodePointer) p1;
        assertEquals("firstValue", np1.getValue());
        assertEquals("value", np1.getName().getName());

        Object p2 = it.next();
        assertTrue(p2 instanceof NodePointer);
        assertEquals("secondValue", ((NodePointer) p2).getValue());

        assertFalse(it.hasNext());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIterateWithNullResult() {
        ConcreteTestExpression expr = new ConcreteTestExpression(null, false);
        EvalContext context = createTestEvalContext("root");

        Iterator it = expr.iterate(context);
        assertNotNull("Iterate on null should return non-null empty iterator", it);
        assertFalse("Empty iterator hasNext should be false", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratePointersWithNullResult() {
        ConcreteTestExpression expr = new ConcreteTestExpression(null, false);
        EvalContext context = createTestEvalContext("root");

        Iterator it = expr.iteratePointers(context);
        assertNotNull("iteratePointers on null should return Collections.EMPTY_LIST.iterator()", it);
        assertFalse("Empty iterator hasNext should be false", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testPointerIteratorWithExistingPointer() {
        NodePointer existing = NodePointer.newNodePointer(new QName("existingName"), "pointerContent", Locale.GERMANY);
        Iterator input = Collections.singletonList(existing).iterator();

        Expression.PointerIterator it = new Expression.PointerIterator(input, new QName("fallback"), Locale.FRANCE);
        assertTrue(it.hasNext());
        Object result = it.next();
        assertSame("PointerIterator should return Pointer instances as-is", existing, result);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testPointerIteratorWithNonPointerAndNull() {
        Iterator input = Arrays.asList("stringValue", null).iterator();
        QName qname = new QName("testQName");
        Locale locale = Locale.JAPAN;

        Expression.PointerIterator it = new Expression.PointerIterator(input, qname, locale);
        assertTrue(it.hasNext());
        Object first = it.next();
        assertTrue("Non-pointer must be wrapped in NodePointer", first instanceof NodePointer);
        NodePointer npFirst = (NodePointer) first;
        assertEquals("stringValue", npFirst.getValue());
        assertEquals(locale, npFirst.getLocale());

        assertTrue(it.hasNext());
        Object second = it.next();
        assertTrue("Null value must also be wrapped in NodePointer", second instanceof NodePointer);
        NodePointer npSecond = (NodePointer) second;
        assertNull(npSecond.getValue());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testValueIteratorWithPointerAndNonPointer() {
        NodePointer pointer = NodePointer.newNodePointer(new QName("test"), "unwrappedValue", Locale.US);
        Iterator input = Arrays.asList(pointer, "literalValue", 12345, null).iterator();

        Expression.ValueIterator it = new Expression.ValueIterator(input);
        assertTrue(it.hasNext());
        assertEquals("Pointer should be unwrapped to its value", "unwrappedValue", it.next());
        assertTrue(it.hasNext());
        assertEquals("Literal value should be returned as-is", "literalValue", it.next());
        assertTrue(it.hasNext());
        assertEquals("Integer value should be returned as-is", 12345, it.next());
        assertTrue(it.hasNext());
        assertNull("Null value should be returned as-is", it.next());
        assertFalse(it.hasNext());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth: ExtensionFunctionTest.testNodeSetReturn)
    // =========================================================================

    /**
     * Defects4J Target Defect:
     * When Expression.compute(context) returns a NodeSet, Expression.iterate(context) in the defective
     * implementation does not wrap the pointers in a ValueIterator. As a result, the value iterator yields
     * Pointer instances (e.g. [/beans[1], /beans[2]]) instead of their unwrapped values
     * (e.g. [Nested: Name 1, Nested: Name 2]).
     */
    @Test(timeout = 4000)
    public void testNodeSetReturn_DefectTarget() {
        final NodePointer p1 = NodePointer.newNodePointer(new QName("beans"), "Nested: Name 1", Locale.US);
        final NodePointer p2 = NodePointer.newNodePointer(new QName("beans"), "Nested: Name 2", Locale.US);

        NodeSet nodeSet = new NodeSet() {
            public List getPointers() {
                return Arrays.asList(p1, p2);
            }

            public List getNodes() {
                return Arrays.asList("Nested: Name 1", "Nested: Name 2");
            }

            public List getValues() {
                return Arrays.asList("Nested: Name 1", "Nested: Name 2");
            }
        };

        ConcreteTestExpression expr = new ConcreteTestExpression(nodeSet, false);
        EvalContext context = createTestEvalContext("root");

        Iterator it = expr.iterate(context);
        assertNotNull("Iterate should return a valid iterator", it);
        assertTrue("Iterator should have first element", it.hasNext());

        Object first = it.next();
        // Fixed version: unwraps to "Nested: Name 1"
        // Defective version: returns NodePointer p1, failing this assertion
        assertEquals("Evaluating value iterator expected unwrapped value", "Nested: Name 1", first);

        assertTrue("Iterator should have second element", it.hasNext());
        Object second = it.next();
        assertEquals("Evaluating value iterator expected second unwrapped value", "Nested: Name 2", second);
        assertFalse("Iterator should be exhausted", it.hasNext());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testPointerIteratorRemoveThrowsException() {
        Expression.PointerIterator it = new Expression.PointerIterator(
                Collections.singletonList("item").iterator(),
                new QName("q"),
                Locale.US
        );
        it.remove();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testValueIteratorRemoveThrowsException() {
        Expression.ValueIterator it = new Expression.ValueIterator(
                Collections.singletonList("item").iterator()
        );
        it.remove();
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testPointerIteratorNextOnEmptyThrowsException() {
        Expression.PointerIterator it = new Expression.PointerIterator(
                Collections.emptyList().iterator(),
                new QName("q"),
                Locale.US
        );
        assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testValueIteratorNextOnEmptyThrowsException() {
        Expression.ValueIterator it = new Expression.ValueIterator(
                Collections.emptyList().iterator()
        );
        assertFalse(it.hasNext());
        it.next();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsIntegrity() {
        assertNotNull("ZERO should not be null", Expression.ZERO);
        assertEquals("ZERO value check", 0.0, Expression.ZERO.doubleValue(), 0.0);

        assertNotNull("ONE should not be null", Expression.ONE);
        assertEquals("ONE value check", 1.0, Expression.ONE.doubleValue(), 0.0);

        assertNotNull("NOT_A_NUMBER should not be null", Expression.NOT_A_NUMBER);
        assertTrue("NOT_A_NUMBER should be NaN", Double.isNaN(Expression.NOT_A_NUMBER.doubleValue()));
    }

    @Test(timeout = 4000)
    public void testComputeValueDelegation() {
        ConcreteTestExpression expr = new ConcreteTestExpression(null, false);
        expr.setComputeValueResult("customValueResult");
        EvalContext context = createTestEvalContext("root");

        Object valueResult = expr.computeValue(context);
        assertEquals("computeValue should return configured result", "customValueResult", valueResult);
    }
}