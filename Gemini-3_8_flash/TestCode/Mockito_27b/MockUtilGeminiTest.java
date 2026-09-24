package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.Factory;
import org.mockito.cglib.proxy.NoOp;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: org.mockito.internal.util.MockUtil
 *
 * Branches & Logic Covered:
 * 1. createMock:
 *    - Validates class type, extra interfaces, and spied instance via MockCreationValidator.
 *    - Initiates mock name.
 *    - Branch: settings.isSerializable() (true / false)
 *      - Sub-branch: interfaces == null vs interfaces != null
 *    - Branch: settings.getSpiedInstance() != null vs null (LenientCopyTool invocation).
 * 2. resetMock:
 *    - Validates mock handler extraction.
 *    - Replaces mock callback filter with new MockHandler.
 *    - [DEFECT ZONE] Targets Defect ListenersLostOnResetMockTest:
 *      In defective version, resetMock() drops InvocationListeners by not wrapping
 *      newMockHandler in InvocationNotifierHandler and resetting MockSettings to defaults.
 * 3. getMockHandler & isMockitoMock & getInterceptor:
 *    - mock == null -> throws NotAMockException
 *    - mock not an instance of Factory -> returns null / throws NotAMockException
 *    - mock is Factory but callback(0) is null or not MethodInterceptorFilter -> returns null / throws NotAMockException
 *    - mock is valid Mockito mock -> returns MockHandlerInterface
 * 4. isMock:
 *    - mock == null -> false
 *    - mock != null && not mockito mock -> false
 *    - mock is valid mock -> true
 * 5. getMockName:
 *    - Custom name configured vs default mock name.
 *    - Non-mock / null -> throws NotAMockException.
 */
public class MockUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateMockSimpleInterface() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockConcreteClass() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        ArrayList<?> mock = mockUtil.createMock(ArrayList.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertEquals("arrayList", mockUtil.getMockName(mock).toString());
    }

    @Test(timeout = 4000)
    public void testCreateMockWithCustomName() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customServiceMock");
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertEquals("customServiceMock", mockUtil.getMockName(mock).toString());
    }

    @Test(timeout = 4000)
    public void testConstructorWithCustomValidator() {
        MockCreationValidator validator = new MockCreationValidator();
        MockUtil mockUtil = new MockUtil(validator);
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testResetMockRetainsMockIdentity() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        mockUtil.resetMock(mock);

        assertTrue("Object should still be recognized as mock after reset", mockUtil.isMock(mock));
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Ancillary Type Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsMockNullArgument() {
        MockUtil mockUtil = new MockUtil();
        assertFalse(mockUtil.isMock(null));
    }

    @Test(timeout = 4000)
    public void testIsMockStandardObject() {
        MockUtil mockUtil = new MockUtil();
        assertFalse(mockUtil.isMock(new Object()));
        assertFalse(mockUtil.isMock("plainString"));
        assertFalse(mockUtil.isMock(new ArrayList<Object>()));
    }

    @Test(timeout = 4000)
    public void testIsMockNonMockitoCglibProxy() {
        MockUtil mockUtil = new MockUtil();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ArrayList.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object nonMockitoProxy = enhancer.create();

        assertTrue(nonMockitoProxy instanceof Factory);
        assertFalse("Non-Mockito CGLIB proxy must not be identified as mock", mockUtil.isMock(nonMockitoProxy));
    }

    @Test(timeout = 4000)
    public void testIsMockCglibProxyWithNullCallback() {
        MockUtil mockUtil = new MockUtil();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ArrayList.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Factory nonMockitoProxy = (Factory) enhancer.create();
        nonMockitoProxy.setCallback(0, null);

        assertFalse("CGLIB proxy with null callback must not be identified as mock", mockUtil.isMock(nonMockitoProxy));
    }

    @Test(timeout = 4000)
    public void testCreateMockSerializableWithoutExtraInterfaces() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();

        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mock instanceof Serializable);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockSerializableWithExtraInterfaces() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(Observer.class);

        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Observer);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockNonSerializableWithExtraInterfaces() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Observer.class);

        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mock instanceof Observer);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockWithSpiedInstanceCopiesState() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        ArrayList<String> realInstance = new ArrayList<String>();
        realInstance.add("itemA");
        realInstance.add("itemB");
        settings.spiedInstance(realInstance);

        ArrayList<?> spiedMock = mockUtil.createMock(ArrayList.class, settings);
        assertNotNull(spiedMock);
        assertTrue(mockUtil.isMock(spiedMock));
        assertEquals(2, spiedMock.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (ListenersLostOnResetMockTest)
    // =========================================================================

    @Test(timeout = 4000)
    public void testListenersNotLostOnResetMock_DefectTarget() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        final int[] invocationCount = new int[1];

        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
                invocationCount[0]++;
            }
        };
        settings.invocationListeners(listener);

        List<?> mockList = mockUtil.createMock(List.class, settings);

        // Pre-condition: listener is invoked during normal mock interaction
        mockList.size();
        assertEquals("Invocation listener should be called prior to reset", 1, invocationCount[0]);

        // Trigger reset
        mockUtil.resetMock(mockList);

        // Post-condition: listener MUST NOT be lost after resetMock
        mockList.size();
        assertEquals("Invocation listener must not be lost after resetMock", 2, invocationCount[0]);
    }

    @Test(timeout = 4000)
    public void testListenersPreservedInSettingsOnResetMock_DefectTarget() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {}
        };
        settings.invocationListeners(listener);

        List<?> mock = mockUtil.createMock(List.class, settings);
        mockUtil.resetMock(mock);

        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mock);
        assertFalse("MockSettings should retain configured invocation listeners after resetMock",
                handler.getMockSettings().getInvocationListeners().isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetMockHandlerNullMockThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockHandler(null);
            fail("Expected NotAMockException for null argument");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockHandlerNonMockThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockHandler("notAMock");
            fail("Expected NotAMockException for non-mock instance");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is: class java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockHandlerCglibNonMockitoThrowsException() {
        MockUtil mockUtil = new MockUtil();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ArrayList.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object cglibProxy = enhancer.create();

        try {
            mockUtil.getMockHandler(cglibProxy);
            fail("Expected NotAMockException for non-Mockito CGLIB proxy");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock"));
        }
    }

    @Test(timeout = 4000)
    public void testResetMockNullThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.resetMock(null);
            fail("Expected NotAMockException on resetMock(null)");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testResetMockNonMockThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.resetMock(new Object());
            fail("Expected NotAMockException on resetMock(nonMock)");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is: class java.lang.Object"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockNameNullThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockName(null);
            fail("Expected NotAMockException on getMockName(null)");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testGetMockNameNonMockThrowsException() {
        MockUtil mockUtil = new MockUtil();
        try {
            mockUtil.getMockName(12345);
            fail("Expected NotAMockException on getMockName(nonMock)");
        } catch (NotAMockException expected) {
            assertTrue(expected.getMessage().contains("Argument should be a mock, but is: class java.lang.Integer"));
        }
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testCreateMockFinalClassThrowsException() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // String is a final class and cannot be mocked
        mockUtil.createMock(String.class, settings);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testCreateMockDuplicateInterfaceThrowsException() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // An extra interface cannot be identical to the mocked type
        settings.extraInterfaces(List.class);
        mockUtil.createMock(List.class, settings);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testCreateMockIncompatibleSpyInstanceThrowsException() {
        MockUtil mockUtil = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // Incompatible spied instance type
        settings.spiedInstance("IncompatibleStringType");
        mockUtil.createMock(List.class, settings);
    }
}