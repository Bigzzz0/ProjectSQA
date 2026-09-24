package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
 *
 * Branches & Logic Targets:
 * 1. answer(InvocationOnMock)
 *    - Branch: !MockCreationValidator.isTypeMockable(rawType) -> delegate.returnValueFor(rawType)
 *      * Primitives (int, boolean, etc.)
 *      * Non-mockable reference types (String, final classes, arrays, collections)
 *    - Branch: MockCreationValidator.isTypeMockable(rawType) -> getMock(invocation)
 * 2. getMock(InvocationOnMock)
 *    - Branch: Stubbed invocation cache hit (container.getInvocationForStubbing().matches(...)) -> return cached mock
 *    - Branch: Stubbed invocation cache miss -> recordDeepStubMock(invocation, container)
 * 3. recordDeepStubMock(InvocationOnMock, InvocationContainerImpl)
 *    - Creates mock using Mockito.mock(clz, this) and registers Answer in container.
 * 4. actualParameterizedType(Object mock)
 *    - Resolves generic metadata support from MockSettings.
 *
 * Defects4J Targeted Faults:
 * - Nested generic metadata resolution failure:
 *   When return type is a generic type variable bound to a non-mockable type (e.g. String or Number in bounded wildcards),
 *   the defective implementation fails to propagate generic metadata, treating rawType as Object, attempting to mock it,
 *   and throwing ClassCastException upon assignment.
 * - Multiple type variable bounds failure:
 *   When return type has multiple bounds (<K extends Comparable<K> & Cloneable>), the defective implementation mocks
 *   only the erasure/first bound, throwing ClassCastException when cast to secondary bounds.
 */
public class ReturnsDeepStubsGeminiTest {

    // Interfaces for deep stub testing
    public interface LeafNode {
        String getName();
        int getCode();
    }

    public interface MiddleNode {
        LeafNode getLeaf();
        LeafNode getLeafWithParam(String param);
    }

    public interface RootNode {
        MiddleNode getMiddle();
    }

    public interface PrimitiveAndFinalHolder {
        int getPrimitiveInt();
        boolean getPrimitiveBool();
        String getString();
        int[] getIntArray();
    }

    // Generic interfaces for defect verification
    public interface GenericContainer<T> {
        T getValue();
    }

    public interface GenericRoot {
        GenericContainer<String> getStringContainer();
        GenericContainer<Integer> getIntegerContainer();
    }

    public interface MultipleBoundContainer<K extends Comparable<K> & Cloneable> {
        K getMultipleBound();
    }

    public interface WildcardContainer {
        List<? extends Number> getNumbers();
    }

    public interface ComplexGenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Map<K, Set<Number>> nestedMap();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeepStubbingMultiLevelNavigation() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        RootNode root = org.mockito.Mockito.mock(RootNode.class, rds);

        MiddleNode middle = root.getMiddle();
        assertNotNull("First level deep stub should return a mock", middle);

        LeafNode leaf = middle.getLeaf();
        assertNotNull("Second level deep stub should return a mock", leaf);

        String name = leaf.getName();
        assertEquals("Leaf non-mockable return should yield default empty string", "", name);

        int code = leaf.getCode();
        assertEquals("Leaf primitive return should yield default zero", 0, code);
    }

    @Test(timeout = 4000)
    public void testCacheHitReturnsSameMockInstance() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        RootNode root = org.mockito.Mockito.mock(RootNode.class, rds);

        MiddleNode middle1 = root.getMiddle();
        MiddleNode middle2 = root.getMiddle();
        assertSame("Subsequent calls with identical invocation should return the cached mock", middle1, middle2);

        LeafNode leaf1 = middle1.getLeaf();
        LeafNode leaf2 = middle1.getLeaf();
        assertSame("Deep leaf calls should also return the cached mock", leaf1, leaf2);
    }

    @Test(timeout = 4000)
    public void testCacheDifferentiatesParameterizedInvocations() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        MiddleNode middle = org.mockito.Mockito.mock(MiddleNode.class, rds);

        LeafNode leafA = middle.getLeafWithParam("alpha");
        LeafNode leafB = middle.getLeafWithParam("beta");
        LeafNode leafA2 = middle.getLeafWithParam("alpha");

        assertNotNull("Invocation with param 'alpha' must produce mock", leafA);
        assertNotNull("Invocation with param 'beta' must produce mock", leafB);
        assertSame("Repeated invocation with 'alpha' must return cached mock", leafA, leafA2);
        assertNotSame("Different parameters should produce different mock instances", leafA, leafB);
    }

    @Test(timeout = 4000)
    public void testDirectNonMockableReturnValues() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        PrimitiveAndFinalHolder holder = org.mockito.Mockito.mock(PrimitiveAndFinalHolder.class, rds);

        assertEquals("int should default to 0", 0, holder.getPrimitiveInt());
        assertFalse("boolean should default to false", holder.getPrimitiveBool());
        assertEquals("String should default to empty string", "", holder.getString());
        assertNotNull("Array should default to empty array", holder.getIntArray());
        assertEquals("Array length should be 0", 0, holder.getIntArray().length);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testManualStubbingOverridesDeepStubBehavior() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        RootNode root = org.mockito.Mockito.mock(RootNode.class, rds);

        MiddleNode customMiddle = org.mockito.Mockito.mock(MiddleNode.class);
        LeafNode customLeaf = org.mockito.Mockito.mock(LeafNode.class);

        org.mockito.Mockito.when(root.getMiddle()).thenReturn(customMiddle);
        org.mockito.Mockito.when(customMiddle.getLeaf()).thenReturn(customLeaf);
        org.mockito.Mockito.when(customLeaf.getName()).thenReturn("CustomName");

        assertSame("Explicitly stubbed middle should override deep stub answer", customMiddle, root.getMiddle());
        assertSame("Explicitly stubbed leaf should be returned", customLeaf, root.getMiddle().getLeaf());
        assertEquals("Explicitly stubbed return value should match", "CustomName", root.getMiddle().getLeaf().getName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWillReturnDefaultValueOnNonMockableNestedGeneric() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        GenericRoot root = org.mockito.Mockito.mock(GenericRoot.class, rds);

        // Targeted Defect:
        // On defective version, GenericMetadataSupport fails to resolve T to String.
        // T defaults to Object (mockable), causing Mockito to instantiate a mock of Object,
        // resulting in ClassCastException when assigning to String.
        // On the corrected version, rawType is correctly resolved as java.lang.String,
        // which is non-mockable, and delegate.returnValueFor(String.class) returns "".
        String stringValue = root.getStringContainer().getValue();
        assertEquals("Should resolve nested generic String and return empty string", "", stringValue);
    }

    @Test(timeout = 4000)
    public void testCanCreateMockFromMultipleTypeVariableBounds() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        @SuppressWarnings("rawtypes")
        MultipleBoundContainer container = org.mockito.Mockito.mock(MultipleBoundContainer.class, rds);

        // Targeted Defect:
        // When return type is a type variable with multiple bounds (<K extends Comparable<K> & Cloneable>),
        // the defective version only mocks the first bound (Comparable), failing to implement Cloneable.
        Object k = container.getMultipleBound();
        assertNotNull("Deep stub mock should not be null", k);
        assertTrue("Mock should implement primary bound Comparable", k instanceof Comparable);
        assertTrue("Mock should implement secondary bound Cloneable", k instanceof Cloneable);
    }

    @Test(timeout = 4000)
    public void testCanCreateMockFromReturnTypesDeclaredWithBoundedWildcard() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        WildcardContainer container = org.mockito.Mockito.mock(WildcardContainer.class, rds);

        List<? extends Number> numbers = container.getNumbers();
        assertNotNull("Wildcard list should be deep-stubbed", numbers);

        // On defective version, chained generic resolution on bounded wildcards creates invalid mock types
        Number number = numbers.get(0);
        assertNull("Deep stub resolution on element should return null/default for Number", number);
    }

    @Test(timeout = 4000)
    public void testComplexGenericDeepMockChainedMap() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        @SuppressWarnings("rawtypes")
        ComplexGenericsNest nest = org.mockito.Mockito.mock(ComplexGenericsNest.class, rds);

        Map<?, ?> map = nest.nestedMap();
        assertNotNull("Chained map call should return a mock map", map);

        Set<?> keySet = map.keySet();
        assertNotNull("KeySet should be mocked", keySet);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testActualParameterizedTypeRejectsNonMock() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        try {
            rds.actualParameterizedType("Pure String, not a mock");
            fail("actualParameterizedType must fail when supplied with a non-mock");
        } catch (RuntimeException expected) {
            assertNotNull("Exception must be thrown indicating invalid mock", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testActualParameterizedTypeRejectsNull() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        try {
            rds.actualParameterizedType(null);
            fail("actualParameterizedType must throw when passed null");
        } catch (RuntimeException expected) {
            // NullPointerException or MockitoException expected
            assertTrue(expected instanceof NullPointerException || expected.getClass().getName().contains("Mockito"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ReturnsDeepStubs original = new ReturnsDeepStubs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized instance must not be null", deserialized);
        assertTrue("Deserialized object must be ReturnsDeepStubs", deserialized instanceof ReturnsDeepStubs);

        // Verify deserialized instance is fully functional
        ReturnsDeepStubs rdsDeserialized = (ReturnsDeepStubs) deserialized;
        RootNode mock = org.mockito.Mockito.mock(RootNode.class, rdsDeserialized);
        assertNotNull("Mock created with deserialized ReturnsDeepStubs should work", mock.getMiddle());
        assertNotNull("Deep stubbing should function with deserialized instance", mock.getMiddle().getLeaf());
    }
}