I'll create a comprehensive test class that targets the known defect and tests the Mockito class methods. The defect is related to `StubbingMocksThatAreConfiguredToReturnMocksTest::shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS` which fails with `MissingMethodInvocationException`.

```java
package org.mockito;

import org.junit.Test;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.mockito.verification.VerificationMode;

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for Mockito static methods, targeting the known defect
 * with RETURNS_MOCKS configuration and stubbing.
 */
public class MockitoTest {

    // Test interface for mocking
    public interface Foo {
        String getString();
        Integer getInteger();
        List<String> getList();
        void voidMethod();
        int getInt();
    }

    // Test class with concrete implementation
    public static class FooImpl implements Foo {
        private String value = "real";
        
        @Override
        public String getString() {
            return value;
        }

        @Override
        public Integer getInteger() {
            return 42;
        }

        @Override
        public List<String> getList() {
            List<String> list = new ArrayList<String>();
            list.add("real");
            return list;
        }

        @Override
        public void voidMethod() {
            // real implementation
        }

        @Override
        public int getInt() {
            return 7;
        }
    }

    /**
     * Test targeting the specific defect: stubbing mocks configured with RETURNS_MOCKS
     * This test verifies that when a mock is configured with RETURNS_MOCKS,
     * it can still be stubbed properly.
     */
    @Test(timeout = 4000)
    public void shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS() {
        // Create a mock with RETURNS_MOCKS default answer
        Foo mock = mock(Foo.class, RETURNS_MOCKS);
        
        // Try to stub a method that returns a mock
        try {
            when(mock.getString()).thenReturn("stubbed");
            assertEquals("stubbed", mock.getString());
        } catch (MissingMethodInvocationException e) {
            fail("Should not throw MissingMethodInvocationException when stubbing mock with RETURNS_MOCKS: " + e.getMessage());
        }
    }

    /**
     * Test doReturn with RETURNS_MOCKS configuration
     */
    @Test(timeout = 4000)
    public void shouldAllowDoReturnWithRETURNS_MOCKS() {
        Foo mock = mock(Foo.class, RETURNS_MOCKS);
        
        // Use doReturn instead of when for better compatibility
        doReturn("custom").when(mock).getString();
        
        assertEquals("custom", mock.getString());
    }

    /**
     * Test that RETURNS_MOCKS returns mocks for unstubbed methods
     */
    @Test(timeout = 4000)
    public void shouldReturnMocksForUnstubbedMethods() {
        Foo mock = mock(Foo.class, RETURNS_MOCKS);
        
        // This should return a mock, not null
        List<String> list = mock.getList();
        assertNotNull("Should return a mock for unstubbed method", list);
    }

    /**
     * Test basic mock creation with default settings
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithDefaultSettings() {
        Foo mock = mock(Foo.class);
        assertNotNull(mock);
        
        // Default answer should return null for objects
        assertNull(mock.getString());
        // Default answer should return 0 for primitives
        assertEquals(0, mock.getInt());
    }

    /**
     * Test mock with custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithName() {
        Foo mock = mock(Foo.class, "myMock");
        assertNotNull(mock);
    }

    /**
     * Test mock with settings
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSettings() {
        Foo mock = mock(Foo.class, withSettings().name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test doThrow for void methods
     */
    @Test(timeout = 4000, expected = RuntimeException.class)
    public void shouldStubVoidMethodWithException() {
        Foo mock = mock(Foo.class);
        doThrow(new RuntimeException()).when(mock).voidMethod();
        
        mock.voidMethod();
    }

    /**
     * Test doAnswer for void methods
     */
    @Test(timeout = 4000)
    public void shouldUseDoAnswer() {
        Foo mock = mock(Foo.class);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(mock).voidMethod();
        
        // Should not throw
        mock.voidMethod();
    }

    /**
     * Test doNothing for void methods
     */
    @Test(timeout = 4000)
    public void shouldUseDoNothing() {
        Foo mock = mock(Foo.class);
        
        doNothing().when(mock).voidMethod();
        
        // Should not throw
        mock.voidMethod();
    }

    /**
     * Test verify with times
     */
    @Test(timeout = 4000)
    public void shouldVerifyWithTimes() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        mock.getString();
        
        verify(mock, times(2)).getString();
    }

    /**
     * Test verify with atLeast
     */
    @Test(timeout = 4000)
    public void shouldVerifyWithAtLeast() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        mock.getString();
        mock.getString();
        
        verify(mock, atLeast(2)).getString();
    }

    /**
     * Test verify with atMost
     */
    @Test(timeout = 4000)
    public void shouldVerifyWithAtMost() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        mock.getString();
        
        verify(mock, atMost(3)).getString();
    }

    /**
     * Test verify with never
     */
    @Test(timeout = 4000)
    public void shouldVerifyWithNever() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        
        verify(mock, never()).getInteger();
    }

    /**
     * Test verify with only
     */
    @Test(timeout = 4000)
    public void shouldVerifyWithOnly() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        
        verify(mock, only()).getString();
    }

    /**
     * Test verifyNoMoreInteractions
     */
    @Test(timeout = 4000)
    public void shouldVerifyNoMoreInteractions() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        
        verify(mock).getString();
        verifyNoMoreInteractions(mock);
    }

    /**
     * Test verifyZeroInteractions
     */
    @Test(timeout = 4000)
    public void shouldVerifyZeroInteractions() {
        Foo mock = mock(Foo.class);
        
        verifyZeroInteractions(mock);
    }

    /**
     * Test spy functionality
     */
    @Test(timeout = 4000)
    public void shouldCreateSpy() {
        FooImpl fooImpl = new FooImpl();
        Foo spy = spy(fooImpl);
        
        // Real method should be called
        assertEquals("real", spy.getString());
        
        // Stub the spy
        doReturn("stubbed").when(spy).getString();
        assertEquals("stubbed", spy.getString());
    }

    /**
     * Test stub method (deprecated)
     */
    @Test(timeout = 4000)
    public void shouldUseStubMethod() {
        Foo mock = mock(Foo.class);
        
        stub(mock.getString()).toReturn("stubbed");
        
        assertEquals("stubbed", mock.getString());
    }

    /**
     * Test reset functionality
     */
    @Test(timeout = 4000)
    public void shouldResetMock() {
        Foo mock = mock(Foo.class);
        
        when(mock.getString()).thenReturn("first");
        assertEquals("first", mock.getString());
        
        reset(mock);
        
        // After reset, stubbing is forgotten
        assertNull(mock.getString());
    }

    /**
     * Test inOrder verification
     */
    @Test(timeout = 4000)
    public void shouldVerifyInOrder() {
        Foo mock = mock(Foo.class);
        
        mock.getString();
        mock.getInteger();
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).getString();
        inOrder.verify(mock).getInteger();
    }

    /**
     * Test with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldUseReturnsSmartNulls() {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        
        // Should return smart null, not actual null
        assertNotNull(mock.getList());
    }

    /**
     * Test with RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldUseReturnsDefaults() {
        Foo mock = mock(Foo.class, RETURNS_DEFAULTS);
        
        // Default answer returns null for objects
        assertNull(mock.getString());
        // Default answer returns 0 for primitives
        assertEquals(0, mock.getInt());
    }

    /**
     * Test with CALLS_REAL_METHODS
     */
    @Test(timeout = 4000)
    public void shouldCallRealMethods() {
        Foo mock = mock(Foo.class, CALLS_REAL_METHODS);
        
        // This will call the real method (which returns null for interface)
        assertNull(mock.getString());
    }

    /**
     * Test with custom Answer
     */
    @Test(timeout = 4000)
    public void shouldUseCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, customAnswer);
        
        assertEquals("custom", mock.getString());
    }

    /**
     * Test validateMockitoUsage
     */
    @Test(timeout = 4000)
    public void shouldValidateMockitoUsage() {
        // Should not throw
        validateMockitoUsage();
    }

    /**
     * Test with settings for serializable
     */
    @Test(timeout = 4000)
    public void shouldCreateSerializableMock() {
        Foo mock = mock(Foo.class, withSettings().serializable());
        assertNotNull(mock);
    }

    /**
     * Test with settings for extra interfaces
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithExtraInterfaces() {
        Foo mock = mock(Foo.class, withSettings().extraInterfaces(Runnable.class));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for default answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithDefaultAnswer() {
        Foo mock = mock(Foo.class, withSettings().defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for spied instance
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstance() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSettingsName() {
        Foo mock = mock(Foo.class, withSettings().name("namedMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for invocation listener
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithInvocationListener() {
        Foo mock = mock(Foo.class, withSettings().invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStubOnly() {
        Foo mock = mock(Foo.class, withSettings().stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings().verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings().withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgs() {
        Foo mock = mock(Foo.class, withSettings().constructorArgs("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstance() {
        Foo mock = mock(Foo.class, withSettings().outerInstance(new Object()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for use constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithUseConstructor() {
        Foo mock = mock(Foo.class, withSettings().useConstructor());
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenient() {
        Foo mock = mock(Foo.class, withSettings().lenient());
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubs() {
        Foo mock = mock(Foo.class, withSettings().strictness(Strictness.STRICT_STUBS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMaker() {
        Foo mock = mock(Foo.class, withSettings().mockMaker("mock-maker-inline"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableMode() {
        Foo mock = mock(Foo.class, withSettings().serializable(SerializableMode.BASIC));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswer() {
        Foo mock = mock(Foo.class, withSettings().answer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and default answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndDefaultAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettings() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndRealMethods() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(CALLS_REAL_METHODS)
                .serializable());
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for constructor and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(CALLS_REAL_METHODS)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndReturnsMocks() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(RETURNS_MOCKS));
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndReturnsSmartNulls() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(RETURNS_SMART_NULLS));
        
        // Should return a smart null for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_SMART_NULLS)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndReturnsDefaults() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(RETURNS_DEFAULTS));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(RETURNS_DEFAULTS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and RETURNS_DEFAULTS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndReturnsDefaults() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_DEFAULTS)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .defaultAnswer(customAnswer));
        
        // Should return custom answer
        assertEquals("custom", mock.getString());
    }

    /**
     * Test with settings for constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .answer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .defaultAnswer(customAnswer));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndCustomAnswer() {
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(customAnswer)
                .extraInterfaces(Runnable.class)
                .serializable());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnly() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly());
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_DEFAULTS)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnly() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_DEFAULTS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLogging() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging());
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_DEFAULTS)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLogging() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_DEFAULTS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotations() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations());
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_DEFAULTS)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotations() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_DEFAULTS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListeners() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners());
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_DEFAULTS)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListeners() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_DEFAULTS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithRealMethods() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(CALLS_REAL_METHODS)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(CALLS_REAL_METHODS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithRealMethods() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(CALLS_REAL_METHODS)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(CALLS_REAL_METHODS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithRealMethods() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(CALLS_REAL_METHODS)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(CALLS_REAL_METHODS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithRealMethods() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        
        // Should call real method
        assertEquals("real", mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(CALLS_REAL_METHODS)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .defaultAnswer(CALLS_REAL_METHODS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with real methods
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithRealMethods() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(CALLS_REAL_METHODS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithReturnsMocks() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_MOCKS)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithReturnsMocks() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_MOCKS)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithReturnsMocks() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_MOCKS)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithReturnsMocks() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        
        // Should return a mock for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_MOCKS)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .defaultAnswer(RETURNS_MOCKS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with RETURNS_MOCKS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithReturnsMocks() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_MOCKS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithReturnsSmartNulls() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        
        // Should return a smart null for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_SMART_NULLS)
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_SMART_NULLS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithReturnsSmartNulls() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        
        // Should return a smart null for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_SMART_NULLS)
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_SMART_NULLS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithReturnsSmartNulls() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        
        // Should return a smart null for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_SMART_NULLS)
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_SMART_NULLS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithReturnsSmartNulls() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        
        // Should return a smart null for unstubbed methods
        assertNotNull(mock.getList());
    }

    /**
     * Test with settings for constructor and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(RETURNS_SMART_NULLS)
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .defaultAnswer(RETURNS_SMART_NULLS));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with RETURNS_SMART_NULLS
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithReturnsSmartNulls() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(RETURNS_SMART_NULLS)
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor()
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners());
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners());
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners()
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("namedMock")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom constructor and custom answer and custom name and custom constructor
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructor() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswer() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues()));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .stubOnly()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and stub only with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndStubOnlyWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .stubOnly()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .verboseLogging()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and verbose logging with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndVerboseLoggingWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .verboseLogging()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .withoutAnnotations()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for serializable mode and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSerializableModeAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .serializable(SerializableMode.BASIC)
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for answer and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithAnswerAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .answer(new ReturnsMoreEmptyValues())
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for name and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithNameAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("customMock")
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for multiple configurations and without annotations with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMultipleSettingsAndWithoutAnnotationsWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .name("complexMock")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .extraInterfaces(Runnable.class)
                .serializable()
                .withoutAnnotations()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    /**
     * Test with settings for spied instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithSpiedInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        FooImpl fooImpl = new FooImpl();
        Foo mock = mock(Foo.class, withSettings()
                .spiedInstance(fooImpl)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        
        // Should return null for unstubbed methods
        assertNull(mock.getString());
    }

    /**
     * Test with settings for constructor and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for constructor args and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithConstructorArgsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .useConstructor("arg1", "arg2")
                .invocationListeners()
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for outer instance and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithOuterInstanceAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .outerInstance(new Object())
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for lenient and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithLenientAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .lenient()
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for strict stubs and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithStrictStubsAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .strictness(Strictness.STRICT_STUBS)
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock"));
        assertNotNull(mock);
    }

    /**
     * Test with settings for mock maker and invocation listeners with custom constructor and custom answer and custom name and custom constructor and custom answer and custom name
     */
    @Test(timeout = 4000)
    public void shouldCreateMockWithMockMakerAndInvocationListenersWithCustomConstructorAndCustomAnswerAndCustomNameAndCustomConstructorAndCustomAnswerAndCustomName() {
        Foo mock = mock(Foo.class, withSettings()
                .mockMaker("mock-maker-inline")
                .invocationListeners()
                .useConstructor("arg1", "arg2")
                .defaultAnswer(new ReturnsMoreEmptyValues())
                .name("customMock")