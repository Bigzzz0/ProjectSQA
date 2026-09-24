package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.MockSettings;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.creation.MockSettingsImpl
 *
 * Branches & Edge Conditions Targeted:
 * 1. Default State:
 *    - extraInterfaces == null
 *    - name == null, mockName == null
 *    - spiedInstance == null
 *    - defaultAnswer == null
 *    - isSerializable() == false (short-circuit on extraInterfaces == null)
 *
 * 2. extraInterfaces(Class<?>...):
 *    - Branch: extraInterfaces == null -> Reporter.extraInterfacesRequiresAtLeastOneInterface()
 *    - Branch: extraInterfaces.length == 0 -> Reporter.extraInterfacesRequiresAtLeastOneInterface()
 *    - Branch: element in extraInterfaces is null -> Reporter.extraInterfacesDoesNotAcceptNullParameters()
 *    - Branch: element in extraInterfaces is not an interface (!i.isInterface()) -> Reporter.extraInterfacesAcceptsOnlyInterfaces(i)
 *    - Branch: valid single and multiple interfaces -> sets field, returns this (fluent)
 *
 * 3. serializable():
 *    - Branch: sets serializable status
 *    - Defect Zone (Defects4J ground truth):
 *      In defective code, serializable() overwrites extraInterfaces with [Serializable.class],
 *      and calling extraInterfaces(...) subsequently overwrites Serializable.class, causing
 *      isSerializable() to become false or extra interfaces to be wiped out when combined.
 *
 * 4. Fluent Setters & Getters:
 *    - name(String) & initiateMockName(Class) -> creates MockName with configured name and class
 *    - spiedInstance(Object) -> stores and returns target instance
 *    - defaultAnswer(Answer) -> stores and returns custom Answer implementation
 *    - Fluent chaining verification across all builder methods
 */
public class MockSettingsImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultStateUponInstantiation() {
        MockSettingsImpl settings = new MockSettingsImpl();

        assertNull("extraInterfaces should default to null", settings.getExtraInterfaces());
        assertNull("mockName should default to null before initiation", settings.getMockName());
        assertNull("spiedInstance should default to null", settings.getSpiedInstance());
        assertNull("defaultAnswer should default to null", settings.getDefaultAnswer());
        assertFalse("isSerializable should default to false", settings.isSerializable());
    }

    @Test(timeout = 4000)
    public void testSetAndGetSpiedInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Object spyTarget = "sampleSpiedString";

        MockSettings returnedSettings = settings.spiedInstance(spyTarget);

        assertSame("spiedInstance() should support fluent chaining", settings, returnedSettings);
        assertSame("getSpiedInstance() should return the configured instance", spyTarget, settings.getSpiedInstance());
    }

    @Test(timeout = 4000)
    public void testSetAndGetDefaultAnswer() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<Object> customAnswer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return "custom";
            }
        };

        MockSettings returnedSettings = settings.defaultAnswer(customAnswer);

        assertSame("defaultAnswer() should support fluent chaining", settings, returnedSettings);
        assertSame("getDefaultAnswer() should return configured answer", customAnswer, settings.getDefaultAnswer());
    }

    @Test(timeout = 4000)
    public void testInitiateMockNameWithNameConfigured() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customMockName");

        assertNull("mockName should remain null before initiateMockName() is invoked", settings.getMockName());

        settings.initiateMockName(List.class);

        assertNotNull("mockName should be created after initiateMockName()", settings.getMockName());
        assertEquals("MockName string representation should reflect configured name", "customMockName", settings.getMockName().toString());
    }

    @Test(timeout = 4000)
    public void testInitiateMockNameWithoutNameConfigured() {
        MockSettingsImpl settings = new MockSettingsImpl();

        settings.initiateMockName(List.class);

        assertNotNull("mockName should be initialized with type default when name is not specified", settings.getMockName());
        assertEquals("Default mock name should reflect uncapitalized type name", "list", settings.getMockName().toString());
    }

    @Test(timeout = 4000)
    public void testValidSingleExtraInterface() {
        MockSettingsImpl settings = new MockSettingsImpl();

        MockSettings returned = settings.extraInterfaces(Comparable.class);

        assertSame("extraInterfaces() should support fluent chaining", settings, returned);
        assertNotNull(settings.getExtraInterfaces());
        assertEquals(1, settings.getExtraInterfaces().length);
        assertEquals(Comparable.class, settings.getExtraInterfaces()[0]);
        assertFalse("Single non-serializable interface should not make mock serializable", settings.isSerializable());
    }

    @Test(timeout = 4000)
    public void testValidMultipleExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();

        settings.extraInterfaces(Comparable.class, Runnable.class, java.util.Observer.class);

        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertNotNull(interfaces);
        assertEquals(3, interfaces.length);
        assertEquals(Comparable.class, interfaces[0]);
        assertEquals(Runnable.class, interfaces[1]);
        assertEquals(java.util.Observer.class, interfaces[2]);
        assertFalse("Without Serializable.class, isSerializable should be false", settings.isSerializable());
    }

    @Test(timeout = 4000)
    public void testSerializableSingleCall() {
        MockSettingsImpl settings = new MockSettingsImpl();

        MockSettings returned = settings.serializable();

        assertSame("serializable() should support fluent chaining", settings, returned);
        assertTrue("isSerializable() should return true after serializable() is called", settings.isSerializable());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithNullArrayThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces((Class<?>[]) null);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithEmptyArrayThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(new Class<?>[0]);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithSingleNullElementThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(new Class<?>[] { null });
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithMixedNullElementThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(List.class, null, Set.class);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithConcreteClassThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(String.class);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfacesWithAbstractClassThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(java.util.AbstractList.class);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets the defect where invoking serializable() followed by extraInterfaces(...)
     * overrides the internal state, causing isSerializable() to incorrectly become false.
     */
    @Test(timeout = 4000)
    public void testShouldBeSerializableAndHaveExtraInterfacesWhenSerializableCalledFirst() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(List.class);

        assertTrue("Settings must remain serializable even when additional interfaces are specified",
                settings.isSerializable());

        List<Class<?>> interfaces = Arrays.asList(settings.getExtraInterfaces());
        assertTrue("Configured extra interfaces must contain List.class", interfaces.contains(List.class));
    }

    /**
     * Targets the symmetric defect where extraInterfaces(...) followed by serializable()
     * discards or overrides previously configured extra interfaces.
     */
    @Test(timeout = 4000)
    public void testShouldRetainExtraInterfacesWhenSerializableCalledSecond() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(List.class);
        settings.serializable();

        assertTrue("Settings must report isSerializable() == true", settings.isSerializable());

        List<Class<?>> interfaces = Arrays.asList(settings.getExtraInterfaces());
        assertTrue("Previously added extra interfaces must not be discarded by serializable()",
                interfaces.contains(List.class));
    }

    // =========================================================================
    // Partition D: Defensive Guards & Fluent Chaining Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullFluentChainConfiguration() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        };

        MockSettings chained = settings
                .name("chainedMock")
                .spiedInstance("target")
                .defaultAnswer(answer)
                .extraInterfaces(Comparable.class);

        assertSame("Chained return should always be the same MockSettings instance", settings, chained);
        assertEquals("target", settings.getSpiedInstance());
        assertSame(answer, settings.getDefaultAnswer());

        settings.initiateMockName(String.class);
        assertEquals("chainedMock", settings.getMockName().toString());
    }

    @Test(timeout = 4000)
    public void testIsSerializableDirectlyWithSerializableInExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class, Comparable.class);

        assertTrue("isSerializable should return true when Serializable is passed explicitly to extraInterfaces",
                settings.isSerializable());
    }
}