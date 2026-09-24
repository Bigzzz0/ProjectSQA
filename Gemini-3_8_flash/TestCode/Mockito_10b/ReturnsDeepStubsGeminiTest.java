package org.mockito.internal.stubbing.defaultanswers;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
 * Defects4J Ground Truth Defect: Mockito-10 / DeepStubsWronglyReportsSerializationProblemsTest
 *
 * Decision / Branch Matrix:
 * 1. isTypeMockable(rawType):
 *    - TRUE: Proceed to deepStub() mock creation or retrieval.
 *    - FALSE (Primitives, String, Final classes, Void): Delegate to ReturnsEmptyValues.
 * 2. container.getStubbedInvocations() match:
 *    - Matched: Returns previously stubbed mock instance (re-invocation idempotence).
 *    - Unmatched: Creates new deep stub mock via newDeepStubMock() and records answer.
 * 3. returnTypeGenericMetadata.hasRawExtraInterfaces():
 *    - TRUE: mockSettings.extraInterfaces() populated with multiple upper bounds.
 *    - FALSE: standard mockSettings without extra interfaces.
 * 4. Generic return type resolution:
 *    - Resolves type variables (e.g., GenericContainer<String>) down to concrete raw types.
 * 5. ReturnsDeepStubsSerializationFallback & writeReplace():
 *    - Serializing fallback answer replaces instance with Mockito.RETURNS_DEEP_STUBS.
 * 6. DEFECT TARGET (Mockito-10):
 *    - Unconditionally marking deep stubs with .serializable() causes MockitoException
 *      when intermediate return type is a class not implementing java.io.Serializable.
 *      Revealed by: should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub().
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Mockito;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReturnsDeepStubsGeminiTest {

    // =========================================================================
    // Test Double Hierarchy Definitions
    // =========================================================================

    static class NotSerializableShouldWork {
        BeCreative becreative() {
            return new BeCreative();
        }
    }

    static class BeCreative {
        Concrete concrete() {
            return new Concrete();
        }
    }

    static class Concrete {
    }

    interface Level1 {
        Level2 getLevel2();
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        String getString();
        FinalClass getFinalClass();
        void getVoid();
    }

    interface Level2 {
        Level3 getLevel3();
    }

    interface Level3 {
        String getValue();
    }

    static final class FinalClass {
    }

    interface BoundedGenerics<T extends List<?> & Cloneable> {
        T getBounded();
    }

    interface GenericContainer<T> {
        T getElement();
    }

    interface StringContainer extends GenericContainer<String> {
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Accessing deep stubs whose intermediate classes do not implement
     * java.io.Serializable must not trigger a MockitoException about serialization.
     * The defective implementation unconditionally adds .serializable() to deep stub settings.
     */
    @Test(timeout = 4000)
    public void should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub() {
        NotSerializableShouldWork notSerializable = Mockito.mock(
                NotSerializableShouldWork.class,
                Mockito.RETURNS_DEEP_STUBS
        );
        Concrete concrete = notSerializable.becreative().concrete();
        assertNotNull("Deep stub access must succeed and return a mock instance", concrete);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void deepStub_shouldReturnMockOnFirstLevelInvocation() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level2 level2 = mock.getLevel2();
        assertNotNull("Deep stub should instantiate intermediate mock", level2);
    }

    @Test(timeout = 4000)
    public void deepStub_shouldReturnMockOnMultiLevelChain() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level3 level3 = mock.getLevel2().getLevel3();
        assertNotNull("Deep stub should instantiate multi-level intermediate mocks", level3);
    }

    @Test(timeout = 4000)
    public void deepStub_shouldReturnSameMockInstanceOnRepeatedInvocation() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level2 firstCall = mock.getLevel2();
        Level2 secondCall = mock.getLevel2();

        assertNotNull(firstCall);
        assertSame("Repeated deep stub invocation must return the identical cached mock", firstCall, secondCall);
    }

    @Test(timeout = 4000)
    public void deepStub_shouldRespectExplicitUserStubbing() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level2 customMock = Mockito.mock(Level2.class);
        Mockito.when(mock.getLevel2()).thenReturn(customMock);

        assertSame("Explicit stubbing must override deep stubs default answer", customMock, mock.getLevel2());
    }

    @Test(timeout = 4000)
    public void deepStub_shouldDelegateToEmptyValuesWhenTypeIsNotMockable() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);

        assertEquals("Primitive int should return default 0", 0, mock.getPrimitiveInt());
        assertFalse("Primitive boolean should return default false", mock.getPrimitiveBoolean());
        assertEquals("String should return empty string from ReturnsEmptyValues", "", mock.getString());
        assertNull("Final class should return null from ReturnsEmptyValues", mock.getFinalClass());
        mock.getVoid(); // Verification that void methods do not fail
    }

    @Test(timeout = 4000)
    public void deepStub_shouldResolveGenericTypeVariablesToConcreteTypes() {
        StringContainer container = Mockito.mock(StringContainer.class, Mockito.RETURNS_DEEP_STUBS);
        assertEquals("Resolved generic return type String should delegate to ReturnsEmptyValues", "", container.getElement());
    }

    @Test(timeout = 4000)
    public void deepStub_shouldResolveNestedGenericHierarchy() {
        GenericsNest<?> nestMock = Mockito.mock(GenericsNest.class, Mockito.RETURNS_DEEP_STUBS);
        Set<?> numberSet = nestMock.entrySet().iterator().next().getValue();
        assertNotNull("Nested generic hierarchy should successfully deep stub", numberSet);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Multiple Bounds
    // =========================================================================

    @Test(timeout = 4000)
    public void deepStub_shouldAddExtraInterfacesWhenBoundedByMultipleInterfaces() {
        BoundedGenerics<?> mock = Mockito.mock(BoundedGenerics.class, Mockito.RETURNS_DEEP_STUBS);
        Object bounded = mock.getBounded();

        assertNotNull("Bounded return type must produce a mock", bounded);
        assertTrue("Mock must implement raw upper bound List", bounded instanceof List);
        assertTrue("Mock must implement additional interface bound Cloneable", bounded instanceof Cloneable);
    }

    @Test(timeout = 4000)
    public void actualParameterizedType_shouldInferTypeFromMockSettings() {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        Level1 mock = Mockito.mock(Level1.class, deepStubs);

        GenericMetadataSupport metadata = deepStubs.actualParameterizedType(mock);
        assertNotNull("Generic metadata should be successfully inferred", metadata);
        assertEquals("Inferred raw type must match the mock type", Level1.class, metadata.rawType());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void actualParameterizedType_shouldThrowExceptionWhenPassedNonMock() {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        deepStubs.actualParameterizedType(new Object());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Serialization)
    // =========================================================================

    @Test(timeout = 4000)
    public void returnsDeepStubs_shouldBeSerializable() throws Exception {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        Object deserialized = serializeAndDeserialize(deepStubs);

        assertNotNull("Deserialized ReturnsDeepStubs instance must not be null", deserialized);
        assertTrue("Deserialized instance must be of type ReturnsDeepStubs", deserialized instanceof ReturnsDeepStubs);
    }

    @Test(timeout = 4000)
    public void returnsDeepStubsSerializationFallback_writeReplaceResolvesToSingleton() throws Exception {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level2 intermediateMock = mock.getLevel2();

        Answer<?> intermediateAnswer = new MockUtil()
                .getMockHandler(intermediateMock)
                .getMockSettings()
                .getDefaultAnswer();

        assertNotNull("Intermediate mock should have a non-null default answer", intermediateAnswer);
        Object deserialized = serializeAndDeserialize(intermediateAnswer);

        assertSame("Serialization fallback writeReplace must resolve to Mockito.RETURNS_DEEP_STUBS",
                Mockito.RETURNS_DEEP_STUBS, deserialized);
    }

    @Test(timeout = 4000)
    public void serializationFallback_actualParameterizedTypeShouldReturnStoredMetadata() {
        Level1 mock = Mockito.mock(Level1.class, Mockito.RETURNS_DEEP_STUBS);
        Level2 intermediateMock = mock.getLevel2();

        ReturnsDeepStubs fallback = (ReturnsDeepStubs) new MockUtil()
                .getMockHandler(intermediateMock)
                .getMockSettings()
                .getDefaultAnswer();

        GenericMetadataSupport metadata = fallback.actualParameterizedType(intermediateMock);
        assertNotNull("Metadata from fallback must not be null", metadata);
        assertEquals("Metadata raw type must match intermediate mock type", Level2.class, metadata.rawType());
    }

    // =========================================================================
    // Serialization Helper Utility
    // =========================================================================

    private static Object serializeAndDeserialize(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}