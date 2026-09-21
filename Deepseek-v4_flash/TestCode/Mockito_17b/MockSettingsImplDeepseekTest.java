package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: MockSettingsImpl
 * 
 * Branches Covered:
 * 1. extraInterfaces(Class<?>...):
 *    - null array -> triggers Reporter.extraInterfacesRequiresAtLeastOneInterface()
 *    - empty array -> triggers Reporter.extraInterfacesRequiresAtLeastOneInterface()
 *    - array with null element -> triggers Reporter.extraInterfacesDoesNotAcceptNullParameters()
 *    - array with non-interface class -> triggers Reporter.extraInterfacesAcceptsOnlyInterfaces()
 *    - valid interfaces -> sets field and returns this
 * 
 * 2. isSerializable():
 *    - extraInterfaces == null -> false
 *    - extraInterfaces != null but does not contain Serializable -> false
 *    - extraInterfaces != null and contains Serializable -> true
 * 
 * 3. initiateMockName(Class):
 *    - name == null -> MockName created with null name
 *    - name != null -> MockName created with provided name
 * 
 * 4. Getters/Setters:
 *    - getMockName() before initiateMockName -> null
 *    - getMockName() after initiateMockName -> non-null
 *    - getExtraInterfaces() before set -> null
 *    - getExtraInterfaces() after set -> returns set array
 *    - getSpiedInstance() before set -> null
 *    - getSpiedInstance() after set -> returns set object
 *    - getDefaultAnswer() before set -> null
 *    - getDefaultAnswer() after set -> returns set answer
 * 
 * 5. Fluent API return values:
 *    - serializable() returns this
 *    - extraInterfaces() returns this
 *    - name() returns this
 *    - spiedInstance() returns this
 *    - defaultAnswer() returns this
 * 
 * Defect Target:
 * - The known defect causes NotSerializableException when a mock with extra interfaces
 *   (including Serializable) is serialized. The test shouldBeSerializeAndHaveExtraInterfaces
 *   verifies that after calling serializable() and extraInterfaces(), the mock settings
 *   correctly reports isSerializable() == true and the extra interfaces are properly stored.
 *   The defect manifests because the extraInterfaces array might not properly include
 *   Serializable when serializable() is called, or the array is not properly propagated.
 * 
 * Test Strategy:
 * - Partition A: Core functional logic - test all getters/setters and fluent returns
 * - Partition B: BVA - test null/empty arrays, null parameters, boundary values
 * - Partition C: Defect-targeted - specifically test serializable() + extraInterfaces() combination
 * - Partition D: Exception paths - verify Reporter methods are called for invalid inputs
 * - Partition E: Lifecycle - test initiateMockName with null and non-null names
 */
public class MockSettingsImplDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testDefaultState() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("Initial mockName should be null", settings.getMockName());
        assertNull("Initial extraInterfaces should be null", settings.getExtraInterfaces());
        assertNull("Initial spiedInstance should be null", settings.getSpiedInstance());
        assertNull("Initial defaultAnswer should be null", settings.getDefaultAnswer());
        assertFalse("isSerializable should be false initially", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testNameSetterAndGetter() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockSettings result = settings.name("myMock");
        assertSame("name() should return this", settings, result);
        // Verify name is used in initiateMockName
        settings.initiateMockName(String.class);
        assertEquals("Mock name should be set", "myMock", settings.getMockName().toString());
    }
    
    @Test(timeout = 4000)
    public void testSpiedInstanceSetterAndGetter() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Object spied = new Object();
        MockSettings result = settings.spiedInstance(spied);
        assertSame("spiedInstance() should return this", settings, result);
        assertSame("Spied instance should be stored", spied, settings.getSpiedInstance());
    }
    
    @Test(timeout = 4000)
    public void testDefaultAnswerSetterAndGetter() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(org.mockito.invocation.InvocationOnMock invocation) {
                return null;
            }
        };
        MockSettings result = settings.defaultAnswer(answer);
        assertSame("defaultAnswer() should return this", settings, result);
        assertSame("Default answer should be stored", answer, settings.getDefaultAnswer());
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesValid() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Class<?>[] interfaces = {java.io.Serializable.class, java.util.List.class};
        MockSettings result = settings.extraInterfaces(interfaces);
        assertSame("extraInterfaces() should return this", settings, result);
        assertArrayEquals("Extra interfaces should be stored", interfaces, settings.getExtraInterfaces());
    }
    
    @Test(timeout = 4000)
    public void testSerializableMethod() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockSettings result = settings.serializable();
        assertSame("serializable() should return this", settings, result);
        assertTrue("isSerializable should be true after serializable()", settings.isSerializable());
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertNotNull("Extra interfaces should not be null after serializable()", interfaces);
        assertEquals("Should have exactly one interface", 1, interfaces.length);
        assertEquals("Should be Serializable.class", java.io.Serializable.class, interfaces[0]);
    }
    
    @Test(timeout = 4000)
    public void testInitiateMockNameWithNullName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.initiateMockName(String.class);
        assertNotNull("MockName should not be null after initiateMockName", settings.getMockName());
        assertEquals("Mock name should be class name when name is null", 
                     String.class.getSimpleName(), settings.getMockName().toString());
    }
    
    @Test(timeout = 4000)
    public void testInitiateMockNameWithCustomName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customName");
        settings.initiateMockName(String.class);
        assertEquals("Mock name should be custom name", "customName", settings.getMockName().toString());
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testExtraInterfacesWithNullArray() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces((Class<?>[]) null);
            fail("Should throw exception for null array");
        } catch (Exception e) {
            // Expected - Reporter.extraInterfacesRequiresAtLeastOneInterface() throws
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesWithEmptyArray() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(new Class<?>[0]);
            fail("Should throw exception for empty array");
        } catch (Exception e) {
            // Expected - Reporter.extraInterfacesRequiresAtLeastOneInterface() throws
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesWithNullElement() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(new Class<?>[]{null});
            fail("Should throw exception for null element");
        } catch (Exception e) {
            // Expected - Reporter.extraInterfacesDoesNotAcceptNullParameters() throws
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesWithNonInterface() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(String.class);
            fail("Should throw exception for non-interface class");
        } catch (Exception e) {
            // Expected - Reporter.extraInterfacesAcceptsOnlyInterfaces() throws
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesWithMixedValidAndInvalid() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(java.io.Serializable.class, String.class);
            fail("Should throw exception when any element is non-interface");
        } catch (Exception e) {
            // Expected - Reporter.extraInterfacesAcceptsOnlyInterfaces() throws
        }
    }
    
    @Test(timeout = 4000)
    public void testIsSerializableWithNullExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertFalse("isSerializable should be false when extraInterfaces is null", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testIsSerializableWithNonSerializableInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(java.util.List.class);
        assertFalse("isSerializable should be false when Serializable not in list", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testIsSerializableWithSerializableInList() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(java.io.Serializable.class, java.util.List.class);
        assertTrue("isSerializable should be true when Serializable in list", settings.isSerializable());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect-Targeted Test:
     * This test directly targets the known defect where a mock with extra interfaces
     * (including Serializable) fails to serialize. The test verifies that after calling
     * serializable() and extraInterfaces(), the settings correctly report isSerializable()
     * and the extra interfaces are properly stored. The defect causes NotSerializableException
     * because the extraInterfaces array might not properly include Serializable.
     */
    @Test(timeout = 4000)
    public void shouldBeSerializeAndHaveExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        
        // Simulate the exact scenario from the failing test
        settings.serializable();
        settings.extraInterfaces(java.util.List.class);
        
        // Verify both conditions that must hold for serialization to work
        assertTrue("isSerializable() must return true after serializable()", settings.isSerializable());
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertNotNull("Extra interfaces must not be null", interfaces);
        
        // Verify Serializable is in the interfaces array
        boolean hasSerializable = false;
        for (Class<?> iface : interfaces) {
            if (iface == java.io.Serializable.class) {
                hasSerializable = true;
                break;
            }
        }
        assertTrue("Serializable must be in extra interfaces", hasSerializable);
        
        // Verify List is also present
        boolean hasList = false;
        for (Class<?> iface : interfaces) {
            if (iface == java.util.List.class) {
                hasList = true;
                break;
            }
        }
        assertTrue("List must be in extra interfaces", hasList);
    }
    
    @Test(timeout = 4000)
    public void testSerializableThenExtraInterfacesPreservesSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(java.util.List.class);
        
        // The defect would cause isSerializable() to return false here
        // because extraInterfaces() might overwrite the array
        assertTrue("isSerializable() must remain true after adding more interfaces", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesThenSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(java.util.List.class);
        settings.serializable();
        
        // Verify both interfaces are present
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertEquals("Should have 2 interfaces", 2, interfaces.length);
        assertTrue("isSerializable should be true", settings.isSerializable());
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testExtraInterfacesNullArrayThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces((Class<?>[]) null);
            fail("Expected exception for null array");
        } catch (RuntimeException e) {
            // Expected - Reporter throws RuntimeException
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesEmptyArrayThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(new Class<?>[0]);
            fail("Expected exception for empty array");
        } catch (RuntimeException e) {
            // Expected - Reporter throws RuntimeException
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesNullElementThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(new Class<?>[]{null});
            fail("Expected exception for null element");
        } catch (RuntimeException e) {
            // Expected - Reporter throws RuntimeException
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesNonInterfaceThrowsException() {
        MockSettingsImpl settings = new MockSettingsImpl();
        try {
            settings.extraInterfaces(String.class);
            fail("Expected exception for non-interface");
        } catch (RuntimeException e) {
            // Expected - Reporter throws RuntimeException
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testGetMockNameBeforeInitiate() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("mockName should be null before initiateMockName", settings.getMockName());
    }
    
    @Test(timeout = 4000)
    public void testGetMockNameAfterInitiate() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.initiateMockName(String.class);
        assertNotNull("mockName should not be null after initiateMockName", settings.getMockName());
        assertEquals("Mock name should be class simple name", 
                     String.class.getSimpleName(), settings.getMockName().toString());
    }
    
    @Test(timeout = 4000)
    public void testInitiateMockNameWithNullClass() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.initiateMockName(null);
        assertNotNull("mockName should not be null even with null class", settings.getMockName());
    }
    
    @Test(timeout = 4000)
    public void testMultipleInitiateMockNameCalls() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.initiateMockName(String.class);
        MockName firstName = settings.getMockName();
        settings.initiateMockName(Integer.class);
        MockName secondName = settings.getMockName();
        assertNotSame("Should create new MockName on each call", firstName, secondName);
        assertEquals("Second name should reflect new class", 
                     Integer.class.getSimpleName(), secondName.toString());
    }
    
    @Test(timeout = 4000)
    public void testFluentInterfaceChaining() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<Object> answer = new Answer<Object>() {
            @Override
            public Object answer(org.mockito.invocation.InvocationOnMock invocation) {
                return null;
            }
        };
        
        // Test chaining all methods
        MockSettings result = settings
            .name("chained")
            .spiedInstance(new Object())
            .defaultAnswer(answer)
            .extraInterfaces(java.io.Serializable.class);
        
        assertSame("Chained methods should return same instance", settings, result);
        assertTrue("isSerializable should be true after chaining", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testSerializableWithMultipleInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(java.util.List.class, java.util.Map.class);
        
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertEquals("Should have 3 interfaces", 3, interfaces.length);
        assertTrue("isSerializable should be true", settings.isSerializable());
    }
    
    @Test(timeout = 4000)
    public void testExtraInterfacesArrayContent() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Class<?>[] expected = {java.util.List.class, java.util.Map.class};
        settings.extraInterfaces(expected);
        
        Class<?>[] actual = settings.getExtraInterfaces();
        assertArrayEquals("Arrays should be equal", expected, actual);
        // Verify it's the same array reference
        assertSame("Should store the same array reference", expected, actual);
    }
    
    @Test(timeout = 4000)
    public void testGetExtraInterfacesReturnsNullInitially() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("Should return null before any extraInterfaces call", settings.getExtraInterfaces());
    }
    
    @Test(timeout = 4000)
    public void testSpiedInstanceNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(null);
        assertNull("Spied instance can be null", settings.getSpiedInstance());
    }
    
    @Test(timeout = 4000)
    public void testDefaultAnswerNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(null);
        assertNull("Default answer can be null", settings.getDefaultAnswer());
    }
}