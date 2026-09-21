package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.MockName;
import org.mockito.internal.util.MockCreationValidator;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.listeners.InvocationListener;
import org.mockito.invocation.Invocation;
import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.Factory;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A – Core Functional Logic & State Transitions:
 *   - createMock: normal creation, with extra interfaces, serializable, spied instance
 *   - resetMock: basic reset, reset with listeners (defect target)
 *   - getMockHandler: valid mock, null, non-mock
 *   - isMock: null, non-mock, mock
 *   - getMockName: valid mock
 * 
 * Partition B – Boundary Value Analysis & Extremes:
 *   - null arguments for getMockHandler, isMock
 *   - empty extra interfaces array
 *   - spiedInstance = null
 *   - serializable = false
 * 
 * Partition C – Defect-Targeted Branch Zone:
 *   - Listeners lost on resetMock: create mock with listener, reset, invoke, verify listener called
 * 
 * Partition D – Exception & Defensive Guard Paths:
 *   - getMockHandler(null) -> NotAMockException
 *   - getMockHandler(non-mock) -> NotAMockException
 *   - getInterceptor on non-Factory -> null
 *   - getInterceptor on Factory with non-MethodInterceptorFilter callback -> null
 * 
 * Partition E – Object Lifecycle & Contract Integrity:
 *   - isMock returns false for null and non-mock objects
 *   - isMock returns true for valid mock
 *   - getMockName returns non-null name
 */
public class MockUtilDeepseekTest {

    // Helper class for spied instance testing
    static class WithField {
        public int value = 5;
    }

    // Helper listener for defect test
    static class TestListener implements InvocationListener {
        boolean called = false;
        @Override
        public void reportInvocation(Invocation invocation) {
            called = true;
        }
    }

    // Helper Factory implementation for testing getInterceptor branches
    static class DummyFactory implements Factory {
        private Callback callback;
        public DummyFactory(Callback callback) {
            this.callback = callback;
        }
        @Override
        public Callback getCallback(int index) {
            return callback;
        }
        @Override
        public void setCallback(int index, Callback callback) {
            this.callback = callback;
        }
        @Override
        public Callback[] getCallbacks() {
            return new Callback[]{callback};
        }
        @Override
        public void setCallbacks(Callback[] callbacks) {
            if (callbacks.length > 0) this.callback = callbacks[0];
        }
        @Override
        public Object newInstance(Callback callback) {
            return null;
        }
        @Override
        public Object newInstance(Callback[] callbacks) {
            return null;
        }
    }

    // Helper non-MethodInterceptorFilter callback
    static class DummyCallback implements Callback {
        // no methods required
    }

    private MockUtil mockUtil = new MockUtil();

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testCreateMockBasic() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull("Mock should not be null", mock);
        assertTrue("Mock should be recognized as mock", mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockWithExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        settings.extraInterfaces(Serializable.class);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue("Mock should implement Serializable", mock instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testCreateMockSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        settings.serializable();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue("Mock should be Serializable", mock instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testCreateMockWithSpiedInstance() {
        WithField spied = new WithField();
        spied.value = 10;
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        settings.spiedInstance(spied);
        WithField mock = mockUtil.createMock(WithField.class, settings);
        assertNotNull("Mock with spied instance should not be null", mock);
        // The mock should have the spied instance's field value (via LenientCopyTool)
        // We can't easily verify without reflection, but at least branch is covered
    }

    @Test(timeout = 4000)
    public void testResetMockBasic() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mockUtil.isMock(mock));
        mockUtil.resetMock(mock);
        assertTrue("After reset, mock should still be a mock", mockUtil.isMock(mock));
        // Verify getMockHandler still works
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    @Test(timeout = 4000)
    public void testGetMockHandlerValid() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull("getMockHandler should return non-null handler", mockUtil.getMockHandler(mock));
    }

    @Test(timeout = 4000)
    public void testIsMockWithMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue("isMock should return true for a mock", mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testGetMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        MockName name = mockUtil.getMockName(mock);
        assertNotNull("MockName should not be null", name);
        assertNotNull("MockName.toString should not be null", name.toString());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testIsMockWithNull() {
        assertFalse("isMock(null) should be false", mockUtil.isMock(null));
    }

    @Test(timeout = 4000)
    public void testIsMockWithNonMock() {
        assertFalse("isMock on a plain object should be false", mockUtil.isMock("not a mock"));
    }

    @Test(timeout = 4000)
    public void testCreateMockWithEmptyExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        settings.extraInterfaces(); // empty
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
    }

    @Test(timeout = 4000)
    public void testCreateMockWithSerializableFalse() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        // serializable not set, so false by default
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        // Should not be Serializable
        assertFalse("Mock should not be Serializable when not set", mock instanceof Serializable);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testResetMockPreservesListeners() {
        // This test targets the known defect: listeners lost on resetMock
        TestListener listener = new TestListener();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        settings.invocationListeners(listener);
        List<?> mock = mockUtil.createMock(List.class, settings);
        // Invoke a method to ensure listener is registered before reset
        mock.size();
        assertTrue("Listener should have been called before reset", listener.called);
        // Reset the mock
        listener.called = false;
        mockUtil.resetMock(mock);
        // Invoke again after reset
        mock.size();
        // On the defective version, the listener is lost and will not be called
        assertTrue("Listener should still be called after reset (defect: listeners lost)", listener.called);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testGetMockHandlerWithNull() {
        mockUtil.getMockHandler(null);
    }

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testGetMockHandlerWithNonMock() {
        mockUtil.getMockHandler("not a mock");
    }

    @Test(timeout = 4000)
    public void testGetInterceptorWithNonFactory() {
        // getInterceptor is private, but we can test indirectly via isMock
        assertFalse("isMock on a non-Factory object should be false", mockUtil.isMock("string"));
    }

    @Test(timeout = 4000)
    public void testGetInterceptorWithFactoryButWrongCallback() {
        // Create a Factory that returns a non-MethodInterceptorFilter callback
        DummyCallback dummyCallback = new DummyCallback();
        DummyFactory factory = new DummyFactory(dummyCallback);
        // isMock checks getInterceptor -> returns null because callback is not MethodInterceptorFilter
        assertFalse("isMock on Factory with wrong callback should be false", mockUtil.isMock(factory));
    }

    @Test(timeout = 4000)
    public void testGetInterceptorWithFactoryAndCorrectCallback() {
        // This is covered by normal mock creation, but we can test explicitly
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue("isMock should be true for a properly created mock", mockUtil.isMock(mock));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testIsMockConsistency() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mockUtil.isMock(mock));
        // After reset, still a mock
        mockUtil.resetMock(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testGetMockNameNotNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
        List<?> mock = mockUtil.createMock(List.class, settings);
        MockName name = mockUtil.getMockName(mock);
        assertNotNull(name);
        // The name should be something like "list" or "mock"
        assertTrue("MockName should contain 'mock' or class name", name.toString().toLowerCase().contains("list") || name.toString().toLowerCase().contains("mock"));
    }
}