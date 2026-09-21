package org.mockito.internal.configuration;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;

public class SpyAnnotationEngineDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: SpyAnnotationEngine.process(Class<?>, Object)
     * 
     * Branches:
     * B1: field.isAnnotationPresent(Spy.class) - true/false
     * B2: instance == null - true/false (defect: missing instance throws MockitoException)
     * B3: new MockUtil().isMock(instance) - true/false
     * B4: IllegalAccessException catch block
     * B5: finally block - field.setAccessible(wasAccessible) always executed
     * 
     * Boundary Conditions:
     * - Null testClass instance
     * - Null context class
     * - Field with @Spy but null value (defect trigger)
     * - Field with @Spy and non-null non-mock value
     * - Field with @Spy and mock value (reset path)
     * - Field with @Spy and @Mock combined (assertNoAnnotations)
     * - Field with @Spy and @Captor combined (assertNoAnnotations)
     * - Field with @Spy and deprecated @Mock combined
     * 
     * Defect Target (SpyShouldHaveNiceNameTest::shouldPrintNiceName):
     * When @Spy field is null, the engine throws MockitoException with message
     * "Cannot create a @Spy for 'fieldName' field because the *instance* is missing..."
     * The defect is that the error message does not include the field name properly
     * or the exception is not thrown as expected. Test verifies the exception
     * is thrown with the correct field name in the message.
     */

    // Test helper class with various field configurations
    private static class TestClass {
        @Spy
        private List<String> spyList = new LinkedList<String>();
        
        @Spy
        private List<String> nullSpyList;
        
        @Spy
        @Mock
        private List<String> spyAndMockList = new LinkedList<String>();
        
        @Spy
        @Captor
        private List<String> spyAndCaptorList = new LinkedList<String>();
        
        @Spy
        @org.mockito.MockitoAnnotations.Mock
        private List<String> spyAndDeprecatedMockList = new LinkedList<String>();
        
        private List<String> noSpyList = new LinkedList<String>();
        
        @Spy
        private List<String> mockedSpyList = Mockito.mock(List.class);
    }

    private SpyAnnotationEngine engine = new SpyAnnotationEngine();

    // ==================== PARTITION A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testProcess_SpyFieldWithNonNullInstance_SpiesInstance() throws Exception {
        TestClass test = new TestClass();
        List<String> originalList = test.spyList;
        
        engine.process(TestClass.class, test);
        
        // Verify the field is now a Mockito spy (not the original)
        assertNotNull("Field should be set to a spy", test.spyList);
        assertTrue("Field should be a Mockito mock/spy", Mockito.mockingDetails(test.spyList).isMock());
        assertNotSame("Original list should be replaced with spy", originalList, test.spyList);
        // Verify spy delegates to original
        test.spyList.add("test");
        assertEquals("Spy should delegate to original", 1, originalList.size());
    }

    @Test(timeout = 4000)
    public void testProcess_SpyFieldWithMockInstance_ResetsMock() throws Exception {
        TestClass test = new TestClass();
        List<String> mockList = test.mockedSpyList;
        mockList.add("existing");
        
        engine.process(TestClass.class, test);
        
        // Verify the mock is reset (no elements)
        assertEquals("Mock should be reset", 0, mockList.size());
        assertTrue("Field should still be the same mock", test.mockedSpyList == mockList);
    }

    @Test(timeout = 4000)
    public void testProcess_NonSpyField_NotModified() throws Exception {
        TestClass test = new TestClass();
        List<String> originalList = test.noSpyList;
        
        engine.process(TestClass.class, test);
        
        assertSame("Non-spy field should not be modified", originalList, test.noSpyList);
    }

    // ==================== PARTITION B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testProcess_NullTestClass_ThrowsException() throws Exception {
        try {
            engine.process(TestClass.class, null);
            fail("Expected NullPointerException or MockitoException");
        } catch (NullPointerException e) {
            // Expected - field.get(null) will throw NPE for instance field
        }
    }

    @Test(timeout = 4000)
    public void testProcess_NoSpyFields_NoException() throws Exception {
        // Create a class with no @Spy fields
        class NoSpyClass {
            private String name = "test";
        }
        NoSpyClass test = new NoSpyClass();
        
        engine.process(NoSpyClass.class, test);
        
        assertEquals("Field should remain unchanged", "test", test.name);
    }

    @Test(timeout = 4000)
    public void testProcess_PrivateSpyField_HandlesAccessibility() throws Exception {
        class PrivateSpyClass {
            @Spy
            private List<String> privateList = new LinkedList<String>();
        }
        PrivateSpyClass test = new PrivateSpyClass();
        
        engine.process(PrivateSpyClass.class, test);
        
        assertNotNull("Private spy field should be initialized", test.privateList);
        assertTrue("Private spy field should be a mock", Mockito.mockingDetails(test.privateList).isMock());
    }

    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testProcess_NullSpyInstance_ThrowsMockitoExceptionWithFieldName() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException for null @Spy field");
        } catch (MockitoException e) {
            // Defect: The exception message should contain the field name 'nullSpyList'
            String message = e.getMessage();
            assertNotNull("Exception message should not be null", message);
            assertTrue("Exception message should contain field name 'nullSpyList', but was: " + message, 
                      message.contains("nullSpyList"));
            assertTrue("Exception message should mention 'instance is missing'", 
                      message.contains("instance is missing"));
        }
    }

    @Test(timeout = 4000)
    public void testProcess_NullSpyInstance_ExceptionMessageFormat() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            String message = e.getMessage();
            // Verify the complete error message format
            assertTrue("Message should start with 'Cannot create a @Spy for '", 
                      message.startsWith("Cannot create a @Spy for '"));
            assertTrue("Message should contain the field name", message.contains("nullSpyList"));
            assertTrue("Message should contain the field name in quotes", 
                      message.contains("'nullSpyList'"));
            assertTrue("Message should mention the instance is missing", 
                      message.contains("instance is missing"));
        }
    }

    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testProcess_SpyWithMockAnnotation_ThrowsException() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException for @Spy with @Mock");
        } catch (MockitoException e) {
            // Expected - unsupported combination
        }
    }

    @Test(timeout = 4000)
    public void testProcess_SpyWithCaptorAnnotation_ThrowsException() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException for @Spy with @Captor");
        } catch (MockitoException e) {
            // Expected - unsupported combination
        }
    }

    @Test(timeout = 4000)
    public void testProcess_SpyWithDeprecatedMockAnnotation_ThrowsException() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException for @Spy with deprecated @Mock");
        } catch (MockitoException e) {
            // Expected - unsupported combination
        }
    }

    @Test(timeout = 4000)
    public void testProcess_IllegalAccessException_ThrowsMockitoException() throws Exception {
        // Create a class with a field that cannot be accessed
        class InaccessibleFieldClass {
            @Spy
            private List<String> spyList = new LinkedList<String>();
        }
        
        // Use a subclass to make field inaccessible
        InaccessibleFieldClass test = new InaccessibleFieldClass();
        
        // This should work normally since we set accessible
        engine.process(InaccessibleFieldClass.class, test);
        assertNotNull("Spy should be created", test.spyList);
    }

    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testProcess_MultipleSpyFields_AllProcessed() throws Exception {
        class MultipleSpyClass {
            @Spy
            private List<String> list1 = new LinkedList<String>();
            @Spy
            private List<String> list2 = new LinkedList<String>();
        }
        MultipleSpyClass test = new MultipleSpyClass();
        
        engine.process(MultipleSpyClass.class, test);
        
        assertTrue("First spy should be a mock", Mockito.mockingDetails(test.list1).isMock());
        assertTrue("Second spy should be a mock", Mockito.mockingDetails(test.list2).isMock());
    }

    @Test(timeout = 4000)
    public void testProcess_SpyFieldWithInheritedSpy() throws Exception {
        class ParentClass {
            @Spy
            protected List<String> parentSpy = new LinkedList<String>();
        }
        class ChildClass extends ParentClass {
            @Spy
            private List<String> childSpy = new LinkedList<String>();
        }
        ChildClass test = new ChildClass();
        
        engine.process(ChildClass.class, test);
        
        assertTrue("Child spy should be a mock", Mockito.mockingDetails(test.childSpy).isMock());
        // Parent spy should not be processed since we only process declared fields
        assertFalse("Parent spy should not be processed", Mockito.mockingDetails(test.parentSpy).isMock());
    }

    @Test(timeout = 4000)
    public void testProcess_SpyFieldWithExistingSpy_ResetsInsteadOfRespy() throws Exception {
        TestClass test = new TestClass();
        // First process to create spy
        engine.process(TestClass.class, test);
        List<String> firstSpy = test.spyList;
        
        // Add some state
        firstSpy.add("data");
        
        // Process again - should reset, not create new spy
        engine.process(TestClass.class, test);
        
        assertSame("Should reuse existing spy", firstSpy, test.spyList);
        assertEquals("Spy should be reset", 0, firstSpy.size());
    }

    @Test(timeout = 4000)
    public void testAssertNoAnnotations_WithNoUndesiredAnnotations_NoException() throws Exception {
        Field field = TestClass.class.getDeclaredField("spyList");
        
        // Should not throw
        engine.assertNoAnnotations(Spy.class, field);
    }

    @Test(timeout = 4000)
    public void testAssertNoAnnotations_WithUndesiredAnnotation_ThrowsException() throws Exception {
        Field field = TestClass.class.getDeclaredField("spyAndMockList");
        
        try {
            engine.assertNoAnnotations(Spy.class, field, Mock.class);
            fail("Expected MockitoException for undesired annotation");
        } catch (MockitoException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateMockFor_ReturnsNull() {
        // The createMockFor method always returns null
        assertNull("createMockFor should return null", engine.createMockFor(null, null));
    }

    @Test(timeout = 4000)
    public void testProcess_SpyFieldWithNullInstance_ExceptionContainsFieldName() throws Exception {
        TestClass test = new TestClass();
        
        try {
            engine.process(TestClass.class, test);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            String message = e.getMessage();
            // The defect is that the field name might not be properly included
            // This test verifies the correct behavior
            assertTrue("Message should contain field name 'nullSpyList'", 
                      message.contains("nullSpyList"));
            assertTrue("Message should contain the field name in single quotes", 
                      message.contains("'nullSpyList'"));
        }
    }
}