/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.util.MockUtil
 *
 * Decision / Condition Matrix:
 * 1. createMock(Class, MockSettingsImpl):
 *    - Validates type, extra interfaces, and spied instance via CreationValidator.
 *    - Branch: interfaces == null -> ancillaryTypes = empty array.
 *    - Branch: interfaces != null -> ancillaryTypes = interfaces.
 *    - Branch: spiedInstance != null -> LenientCopyTool copies state to mock.
 *    - Branch: spiedInstance == null -> no copy operation performed.
 *    - Defect Zone (Defects4J): When settings.isSerializable() is configured with
 *      extra interfaces (or non-serializable type), ancillaryTypes must ensure
 *      java.io.Serializable is properly applied so that serialization does not fail with
 *      NotSerializableException.
 * 2. resetMock(T mock):
 *    - Extracts existing handler via getMockHandler(mock).
 *    - Instantiates new handler and MethodInterceptorFilter with default answers.
 *    - Invokes ((Factory) mock).setCallback(0, newFilter).
 * 3. getMockHandler(T mock):
 *    - Branch: mock == null -> throws NotAMockException("Argument should be a mock, but is null!").
 *    - Branch: isMockitoMock(mock) == true -> returns MethodInterceptorFilter's handler.
 *    - Branch: isMockitoMock(mock) == false -> throws NotAMockException("Argument should be a mock, but is: ...").
 * 4. isMockitoMock(T mock):
 *    - Branch: Enhancer.isEnhanced(mock.getClass()) is false -> false.
 *    - Branch: Enhancer.isEnhanced(mock.getClass()) is true, getInterceptor(mock) == null (foreign CGLIB proxy) -> false.
 *    - Branch: Enhancer.isEnhanced(mock.getClass()) is true, getInterceptor(mock) != null -> true.
 * 5. isMock(Object mock):
 *    - Branch: mock == null -> false.
 *    - Branch: mock != null && isMockitoMock(mock) -> true / false based on enhancer/callback.
 * 6. getInterceptor(T mock):
 *    - Branch: callback instanceof MethodInterceptorFilter -> returns filter.
 *    - Branch: callback not instanceof MethodInterceptorFilter -> returns null.
 * 7. getMockName(Object mock):
 *    - Retrieves MockName from handler settings.
 */
package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.NoOp;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

public class MockUtilGeminiTest {

    public interface SampleInterface {
        void execute();
    }

    public static class SampleClass {
        private String value = "initial";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class AnotherClass {}

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateMockStandardClass() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull("Mock instance should not be null", mock);
        assertTrue("isMock should return true for created mock", mockUtil.isMock(mock));

        MockHandlerInterface<SampleClass> handler = mockUtil.getMockHandler(mock);
        assertNotNull("Mock handler should not be null", handler);
        assertEquals("Handler settings should match original settings", settings, handler.getMockSettings());
    }

    @Test(timeout = 4000)
    public void testCreateMockWithExtraInterfaces() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Comparable.class);

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull("Mock should be created with extra interfaces", mock);
        assertTrue("Mock should implement requested extra interface", mock instanceof Comparable);
        assertTrue("isMock should report true", mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockWithSpiedInstance() {
        MockUtil mockUtil = new MockUtil();
        SampleClass realInstance = new SampleClass();
        realInstance.setValue("spiedValue");

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(realInstance);
        settings.defaultAnswer(new CallsRealMethods());

        SampleClass spiedMock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull("Spied mock should be instantiated", spiedMock);
        assertTrue("Should be recognized as a mock", mockUtil.isMock(spiedMock));
        assertEquals("Spied mock should copy state from original instance", "spiedValue", spiedMock.getValue());
    }

    @Test(timeout = 4000)
    public void testResetMockCreatesNewHandler() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        MockHandlerInterface<SampleClass> handlerBefore = mockUtil.getMockHandler(mock);
        mockUtil.resetMock(mock);
        MockHandlerInterface<SampleClass> handlerAfter = mockUtil.getMockHandler(mock);

        assertNotNull("Handler after reset must not be null", handlerAfter);
        assertNotSame("Reset must replace the MockHandler with a new instance", handlerBefore, handlerAfter);
        assertTrue("Mock should remain recognized as a mock after reset", mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testGetMockName() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        MockName mockName = mockUtil.getMockName(mock);
        assertNotNull("MockName must not be null", mockName);
        assertTrue("MockName string representation should contain class name",
                mockName.toString().toLowerCase().contains("sampleclass"));
    }

    @Test(timeout = 4000)
    public void testCustomCreationValidatorInvocation() {
        final boolean[] validatorCalled = new boolean[1];
        CreationValidator customValidator = new CreationValidator() {
            @Override
            public void validateType(Class classToMock) {
                validatorCalled[0] = true;
                super.validateType(classToMock);
            }
        };

        MockUtil customMockUtil = new MockUtil(customValidator);
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = customMockUtil.createMock(SampleClass.class, settings);

        assertNotNull("Mock should be created using custom validator", mock);
        assertTrue("Custom CreationValidator should have been invoked", validatorCalled[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsMockWithNullArgument() {
        MockUtil mockUtil = new MockUtil();
        assertFalse("isMock(null) must return false", mockUtil.isMock(null));
    }

    @Test(timeout = 4000)
    public void testIsMockWithStandardObject() {
        MockUtil mockUtil = new MockUtil();
        assertFalse("isMock on non-enhanced object must return false", mockUtil.isMock("plainString"));
        assertFalse("isMock on new Object() must return false", mockUtil.isMock(new Object()));
    }

    @Test(timeout = 4000)
    public void testIsMockWithForeignCglibProxy() {
        MockUtil mockUtil = new MockUtil();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(AnotherClass.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object foreignProxy = enhancer.create();

        assertTrue("Foreign CGLIB object is enhanced", Enhancer.isEnhanced(foreignProxy.getClass()));
        assertFalse("Foreign CGLIB object without Mockito filter must return false for isMock",
                mockUtil.isMock(foreignProxy));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J issue where a mock created with settings.serializable()
     * and extra interfaces fails to be serialized because Serializable.class is
     * omitted from ancillaryTypes when extra interfaces are configured.
     */
    @Test(timeout = 4000)
    public void testMockSerializationWithSerializableAndExtraInterfaces() throws Exception {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(SampleInterface.class);

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertTrue("Mock with serializable() settings must implement java.io.Serializable",
                mock instanceof Serializable);
        assertTrue("Mock must implement extra interfaces", mock instanceof SampleInterface);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mock);
        oos.flush();
        oos.close();

        assertTrue("Serialized output stream must contain byte data", baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testMockSerializationWithSerializableOnInterfaceMock() throws Exception {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();

        SampleInterface mock = mockUtil.createMock(SampleInterface.class, settings);

        assertTrue("Interface mock configured as serializable must implement Serializable",
                mock instanceof Serializable);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mock);
        oos.flush();
        oos.close();

        assertTrue("Serialized bytes should be generated for interface mock", baos.size() > 0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetMockHandlerThrowsOnNull() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockHandler(null);
            fail("Expected NotAMockException on null argument");
        } catch (NotAMockException expected) {
            assertEquals("Argument should be a mock, but is null!", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetMockHandlerThrowsOnNonMockObject() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockHandler("notAMock");
            fail("Expected NotAMockException on standard object");
        } catch (NotAMockException expected) {
            assertTrue("Exception message should state object type",
                    expected.getMessage().contains("Argument should be a mock, but is: class java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockHandlerThrowsOnForeignCglibObject() {
        MockUtil mockUtil = new MockUtil();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(AnotherClass.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object foreignProxy = enhancer.create();

        try {
            mockUtil.getMockHandler(foreignProxy);
            fail("Expected NotAMockException on non-Mockito CGLIB proxy");
        } catch (NotAMockException expected) {
            assertTrue("Exception message should describe non-mock class",
                    expected.getMessage().contains("Argument should be a mock, but is:"));
        }
    }

    @Test(timeout = 4000)
    public void testResetMockThrowsOnNull() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.resetMock(null);
            fail("Expected NotAMockException on resetting null");
        } catch (NotAMockException expected) {
            assertEquals("Argument should be a mock, but is null!", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testResetMockThrowsOnNonMock() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.resetMock(new Object());
            fail("Expected NotAMockException on resetting non-mock");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is:"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockNameThrowsOnNonMock() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockName("notAMock");
            fail("Expected NotAMockException on getMockName for non-mock");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is:"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateMockFailsOnFinalClass() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            mockUtil.createMock(String.class, settings);
            fail("Expected MockitoException when mocking final class");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateMockFailsOnPrimitiveType() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            mockUtil.createMock(int.class, settings);
            fail("Expected MockitoException when mocking primitive type");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMockSettingsAndHandlerIntegrity() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock1 = mockUtil.createMock(SampleClass.class, settings);
        SampleClass mock2 = mockUtil.createMock(SampleClass.class, settings);

        assertNotSame("Consecutive mock creations must yield distinct instances", mock1, mock2);
        assertNotSame("Distinct mocks must maintain distinct MockHandlers",
                mockUtil.getMockHandler(mock1), mockUtil.getMockHandler(mock2));
    }
}