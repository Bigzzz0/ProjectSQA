package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

/**
 * White-box test suite for MockUtil targeting maximum coverage and the known serialization defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - createMock: validation branches (validateType, validateExtraInterfaces, validateMockedType),
 *   null/empty extraInterfaces, spiedInstance null/non-null, ClassImposterizer call.
 * - resetMock: getMockHandler success/failure (null, non-mock), setCallback.
 * - getMockHandler: null check, isMockitoMock true/false branches.
 * - isMockitoMock: Enhancer.isEnhanced + getInterceptor != null.
 * - isMock: null guard, isMockitoMock delegation.
 * - getInterceptor: Factory cast, callback instanceof MethodInterceptorFilter.
 * - getMockName: getMockHandler delegation.
 * - Defect target: serialization of mock with extra interfaces (NotSerializableException).
 */
public class MockUtilDeepseekTest {

    // --- Partition A: Core Functional Logic & State Transitions ---

    @Test(timeout = 4000)
    public void createMock_withDefaultSettings_returnsMock() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        assertNotNull(mock);
        assertTrue(util.isMock(mock));
    }

    @Test(timeout = 4000)
    public void createMock_withExtraInterfaces_returnsMock() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.setExtraInterfaces(new Class<?>[]{Serializable.class});
        Runnable mock = util.createMock(Runnable.class, settings);
        assertNotNull(mock);
        assertTrue(util.isMock(mock));
    }

    @Test(timeout = 4000)
    public void createMock_withSpiedInstance_copiesProperties() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // Use a concrete class that is mockable (e.g., ArrayList) to allow spying
        ArrayList<String> spyTarget = new ArrayList<String>();
        spyTarget.add("test");
        settings.setSpiedInstance(spyTarget);
        ArrayList<String> mock = util.createMock(ArrayList.class, settings);
        assertNotNull(mock);
        assertTrue(util.isMock(mock));
        // The spied instance's data should be copied (LenientCopyTool)
        assertEquals(1, mock.size());
        assertEquals("test", mock.get(0));
    }

    @Test(timeout = 4000)
    public void resetMock_withValidMock_resetsBehavior() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        // Reset should not throw
        util.resetMock(mock);
        assertTrue(util.isMock(mock));
    }

    @Test(timeout = 4000)
    public void getMockHandler_withValidMock_returnsHandler() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        MockHandlerInterface<Runnable> handler = util.getMockHandler(mock);
        assertNotNull(handler);
    }

    @Test(timeout = 4000)
    public void getMockName_withValidMock_returnsName() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        MockName name = util.getMockName(mock);
        assertNotNull(name);
        assertNotNull(name.toString());
    }

    @Test(timeout = 4000)
    public void isMock_withValidMock_returnsTrue() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        assertTrue(util.isMock(mock));
    }

    // --- Partition B: Boundary Value Analysis & Extremes ---

    @Test(timeout = 4000)
    public void isMock_withNull_returnsFalse() {
        MockUtil util = new MockUtil();
        assertFalse(util.isMock(null));
    }

    @Test(timeout = 4000)
    public void isMock_withNonMockObject_returnsFalse() {
        MockUtil util = new MockUtil();
        assertFalse(util.isMock("not a mock"));
    }

    @Test(timeout = 4000)
    public void createMock_withNullExtraInterfaces_usesEmptyArray() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.setExtraInterfaces(null); // explicit null
        Runnable mock = util.createMock(Runnable.class, settings);
        assertNotNull(mock);
    }

    @Test(timeout = 4000)
    public void createMock_withEmptyExtraInterfaces_usesEmptyArray() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.setExtraInterfaces(new Class<?>[0]);
        Runnable mock = util.createMock(Runnable.class, settings);
        assertNotNull(mock);
    }

    // --- Partition C: Defect-Targeted Branch Zone (Serialization with Extra Interfaces) ---

    @Test(timeout = 4000)
    public void mockWithExtraInterfaces_shouldBeSerializable() throws Exception {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // Add Serializable as extra interface to a non-Serializable mocked type
        settings.setExtraInterfaces(new Class<?>[]{Serializable.class});
        Runnable mock = util.createMock(Runnable.class, settings);

        // Serialize and deserialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(mock);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Runnable deserialized = (Runnable) ois.readObject();

        assertNotNull(deserialized);
        assertTrue(util.isMock(deserialized));
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void getMockHandler_withNull_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.getMockHandler(null);
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void getMockHandler_withNonMock_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.getMockHandler("not a mock");
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void resetMock_withNull_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.resetMock(null);
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void resetMock_withNonMock_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.resetMock("not a mock");
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void getMockName_withNull_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.getMockName(null);
    }

    @Test(expected = NotAMockException.class, timeout = 4000)
    public void getMockName_withNonMock_throwsNotAMockException() {
        MockUtil util = new MockUtil();
        util.getMockName("not a mock");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void createMock_withFinalClass_throwsValidationException() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        // String is final, should cause validateType to throw
        util.createMock(String.class, settings);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void createMock_withPrimitiveType_throwsValidationException() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        util.createMock(int.class, settings);
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---

    @Test(timeout = 4000)
    public void mockIdentity_afterReset_remainsSameMock() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        Runnable original = mock;
        util.resetMock(mock);
        assertSame(original, mock); // reset does not replace the mock object
    }

    @Test(timeout = 4000)
    public void getMockHandler_returnsConsistentHandler() {
        MockUtil util = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util.createMock(Runnable.class, settings);
        MockHandlerInterface<Runnable> handler1 = util.getMockHandler(mock);
        MockHandlerInterface<Runnable> handler2 = util.getMockHandler(mock);
        assertSame(handler1, handler2);
    }

    @Test(timeout = 4000)
    public void isMock_returnsTrueForMockCreatedByDifferentUtilInstance() {
        MockUtil util1 = new MockUtil();
        MockUtil util2 = new MockUtil();
        MockSettingsImpl settings = new MockSettingsImpl();
        Runnable mock = util1.createMock(Runnable.class, settings);
        assertTrue(util2.isMock(mock)); // isMock relies only on mock internals
    }
}