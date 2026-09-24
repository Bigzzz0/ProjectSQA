package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockitoCore;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.progress.MockingProgressImpl;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
 *
 * Decision / Condition Matrix:
 * 1. answer(InvocationOnMock):
 *    - Branch A: !mockitoCore.isTypeMockable(rawType) == true  -> Delegate to ReturnsEmptyValues (primitives, String, void).
 *    - Branch B: !mockitoCore.isTypeMockable(rawType) == false -> Delegate to getMock(...).
 * 2. getMock(InvocationOnMock, GenericMetadataSupport):
 *    - Branch A: Loop over container.getStubbedInvocations() with matches() == true -> Return previously cached mock.
 *    - Branch B: Loop finished or no match -> Create, record, and return new deep stub mock.
 * 3. withSettingsUsing(GenericMetadataSupport):
 *    - Branch A: rawExtraInterfaces().length > 0 -> withSettings().extraInterfaces(...)
 *    - Branch B: rawExtraInterfaces().length == 0 -> withSettings()
 * 4. actualParameterizedType(Object):
 *    - Extracts GenericMetadataSupport from MockUtil.getMockHandler(mock).getMockSettings().getTypeToMock().
 *    - Error path: Non-mock passed -> NotAMockException.
 *
 * Known Defect (Defects4J Ground Truth):
 * - DeepStubsSerializableTest: Deep stubbed mock serialisation fails with:
 *   java.io.NotSerializableException: org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs$2
 *   Cause: recordDeepStubMock adds an anonymous Answer instance (ReturnsDeepStubs$2) to the InvocationContainer
 *   that does not implement java.io.Serializable. When serializing the mock or container, serialization fails.
 * --------------------------------------------------------------------------------------------------------------------
 */
public class ReturnsDeepStubsGeminiTest {

    // Helper interface defining various generic and non-generic method signatures for deep stubbing
    public interface SampleComplexTree {
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        String getFinalString();
        void getVoid();
        SampleComplexTree getChild();
        <T extends Comparable<T> & Cloneable> T getBoundedType();
        <K extends Comparable<K> & Cloneable> Map<K, Set<Number>> getNestedGenerics();
    }

    private SampleComplexTree createDeepStubbedMock(boolean serializable) {
        MockitoCore core = new MockitoCore();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new ReturnsDeepStubs());
        if (serializable) {
            settings.serializable();
        }
        return core.mock(SampleComplexTree.class, settings);
    }

    // ================================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testDeepStubReturnsMockForMockableType() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        SampleComplexTree child = rootMock.getChild();

        assertNotNull("Deep stub should produce a non-null child mock", child);
        assertTrue("Deep stub child must be of expected interface type", child instanceof SampleComplexTree);
    }

    @Test(timeout = 4000)
    public void testDeepStubReturnsPreviouslyCreatedMockOnSubsequentCalls() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);

        SampleComplexTree firstCallChild = rootMock.getChild();
        SampleComplexTree secondCallChild = rootMock.getChild();

        assertNotNull("First invocation should yield a mock", firstCallChild);
        assertSame("Repeated invocation should return the cached deep stub instance", firstCallChild, secondCallChild);
    }

    @Test(timeout = 4000)
    public void testMultiLevelDeepStubbingNavigation() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);

        SampleComplexTree level1 = rootMock.getChild();
        SampleComplexTree level2 = level1.getChild();
        int primitiveValue = level2.getPrimitiveInt();

        assertNotNull("Level 1 child must not be null", level1);
        assertNotNull("Level 2 child must not be null", level2);
        assertNotSame("Level 1 and Level 2 mocks must be distinct", level1, level2);
        assertEquals("End of chain primitive must return default 0", 0, primitiveValue);
    }

    // ================================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testNonMockablePrimitiveReturnsDefaultZero() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        int result = rootMock.getPrimitiveInt();
        assertEquals("Primitive int should return default 0", 0, result);
    }

    @Test(timeout = 4000)
    public void testNonMockablePrimitiveReturnsDefaultFalse() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        boolean result = rootMock.getPrimitiveBoolean();
        assertFalse("Primitive boolean should return default false", result);
    }

    @Test(timeout = 4000)
    public void testNonMockableFinalClassReturnsEmptyValue() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        String result = rootMock.getFinalString();
        assertEquals("Final class String should return empty string from ReturnsEmptyValues", "", result);
    }

    @Test(timeout = 4000)
    public void testVoidMethodReturnsNull() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        rootMock.getVoid();
        // Void invocation succeeds without throwing exception
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesBranchTriggeredByMultipleTypeBounds() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);

        // getBoundedType() returns <T extends Comparable<T> & Cloneable>
        // Triggers branch: returnTypeGenericMetadata.rawExtraInterfaces().length > 0
        Object boundedMock = rootMock.getBoundedType();

        assertNotNull("Bounded mock must not be null", boundedMock);
        assertTrue("Should implement primary bound Comparable", boundedMock instanceof Comparable);
        assertTrue("Should implement extra interface bound Cloneable", boundedMock instanceof Cloneable);
    }

    @Test(timeout = 4000)
    public void testNestedGenericsMockResolution() {
        SampleComplexTree rootMock = createDeepStubbedMock(false);
        Map<?, ?> map = rootMock.getNestedGenerics();

        assertNotNull("Mockable generic Map should be instantiated", map);
        assertTrue("Returned value should be instance of Map", map instanceof Map);
    }

    // ================================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Trigger)
    // ================================================================================================================

    /**
     * Target Defect:
     * Deep stubbed mocks configured with serializable() fail when serialized because
     * ReturnsDeepStubs$2 (the Answer added in recordDeepStubMock) is not Serializable.
     */
    @Test(timeout = 4000)
    public void testDeepStubMockSerializationDefect() throws Exception {
        SampleComplexTree rootMock = createDeepStubbedMock(true);

        // Trigger deep stub creation and recordDeepStubMock
        SampleComplexTree child = rootMock.getChild();
        assertNotNull(child);

        // Serializing the root mock must succeed if deep stubs are properly serializable
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(rootMock);
            oos.flush();
        } finally {
            oos.close();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized mock should not be null", deserialized);
        assertTrue("Deserialized mock should be instance of SampleComplexTree", deserialized instanceof SampleComplexTree);
    }

    @Test(timeout = 4000)
    public void testRecordDeepStubMockAnswerMustImplementSerializable() throws Throwable {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        CreationSettings settings = new CreationSettings();
        InvocationContainerImpl container = new InvocationContainerImpl(new MockingProgressImpl(), settings);

        Method recordMethod = ReturnsDeepStubs.class.getDeclaredMethod("recordDeepStubMock", Object.class, InvocationContainerImpl.class);
        recordMethod.setAccessible(true);

        Object dummyMock = "mockTarget";
        recordMethod.invoke(rds, dummyMock, container);

        List<StubbedInvocationMatcher> stubbed = container.getStubbedInvocations();
        assertFalse("An invocation matcher answer should have been added", stubbed.isEmpty());

        StubbedInvocationMatcher matcher = stubbed.get(0);
        // The recorded answer itself must be serializable
        assertTrue("The answer recorded by deep stubs must implement java.io.Serializable",
                matcher instanceof Serializable);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(container);
        oos.flush();
        oos.close();
    }

    // ================================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAnswerWithNullInvocationThrowsException() throws Throwable {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        rds.answer(null);
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void testActualParameterizedTypeWithNonMockThrowsException() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        rds.actualParameterizedType("DefinitelyNotAMockInstance");
    }

    @Test(expected = Exception.class, timeout = 4000)
    public void testActualParameterizedTypeWithNullObjectThrowsException() {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        rds.actualParameterizedType(null);
    }

    // ================================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testReturnsDeepStubsItselfIsSerializable() throws Exception {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(rds);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized ReturnsDeepStubs should not be null", deserialized);
        assertTrue("Should be an instance of ReturnsDeepStubs", deserialized instanceof ReturnsDeepStubs);
    }

    @Test(timeout = 4000)
    public void testReturnsDeepStubsAnswerUsingReflectionAndDelegation() throws Exception {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        Method answerUsingMethod = ReturnsDeepStubs.class.getDeclaredMethod("returnsDeepStubsAnswerUsing", GenericMetadataSupport.class);
        answerUsingMethod.setAccessible(true);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SampleComplexTree.class);
        Object customAnswer = answerUsingMethod.invoke(rds, metadata);

        assertNotNull("ReturnsDeepStubs derivative must be returned", customAnswer);
        assertTrue("Must be an instance of ReturnsDeepStubs", customAnswer instanceof ReturnsDeepStubs);

        ReturnsDeepStubs subRds = (ReturnsDeepStubs) customAnswer;
        GenericMetadataSupport retrieved = subRds.actualParameterizedType(null);
        assertSame("actualParameterizedType on customized answer must return the preserved metadata", metadata, retrieved);
    }

    @Test(timeout = 4000)
    public void testDirectInvocationHandlingWithSubclassedAnswer() throws Throwable {
        final GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SampleComplexTree.class);
        ReturnsDeepStubs rds = new ReturnsDeepStubs() {
            @Override
            protected GenericMetadataSupport actualParameterizedType(Object mock) {
                return metadata;
            }
        };

        final Method primitiveMethod = SampleComplexTree.class.getMethod("getPrimitiveInt");
        InvocationOnMock invocation = new InvocationOnMock() {
            public Object getMock() { return "mockPlaceholder"; }
            public Method getMethod() { return primitiveMethod; }
            public Object[] getArguments() { return new Object[0]; }
            public <T> T getArgumentAt(int index, Class<T> clazz) { return null; }
            public Object callRealMethod() throws Throwable { return null; }
        };

        Object result = rds.answer(invocation);
        assertEquals("Subclassed actualParameterizedType should resolve primitive int return type to 0", 0, result);
    }

    @Test(timeout = 4000)
    public void testWithSettingsUsingReflectionWithoutExtraInterfaces() throws Exception {
        ReturnsDeepStubs rds = new ReturnsDeepStubs();
        Method withSettingsMethod = ReturnsDeepStubs.class.getDeclaredMethod("withSettingsUsing", GenericMetadataSupport.class);
        withSettingsMethod.setAccessible(true);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SampleComplexTree.class);
        Object settings = withSettingsMethod.invoke(rds, metadata);

        assertNotNull("MockSettings should be created", settings);
        assertTrue("Created settings should implement MockSettings", settings instanceof MockSettings);
    }
}