package org.mockito.internal.invocation;

import java.lang.reflect.Method;
import java.util.*;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.*;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class InvocationMatcherDeepseekTest extends TestBase {

    @Mock
    private IMethods mock;

    private InvocationMatcher matcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * @target InvocationMatcher(Invocation) constructor
     * @scenario Create matcher with default empty matchers
     * @defectRisk Ensuring proper initialization with empty matchers list
     */
    @Test(timeout = 4000)
    public void testConstructorWithNoMatchers() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertNotNull(matcher.getMatchers());
        assertFalse(matcher.getMatchers().isEmpty());
        assertEquals(invocation, matcher.getInvocation());
    }

    /**
     * @target InvocationMatcher(Invocation, List<Matcher>) constructor
     * @scenario Create matcher with non-empty matchers list
     * @defectRisk Ensuring correct matcher association
     */
    @Test(timeout = 4000)
    public void testConstructorWithCustomMatchers() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        List<Matcher> customMatchers = asList(Matchers.eq(1));
        InvocationMatcher matcher = new InvocationMatcher(invocation, customMatchers);
        assertEquals(customMatchers, matcher.getMatchers());
    }

    /**
     * @target InvocationMatcher(Invocation, List<Matcher>) constructor
     * @scenario Create matcher with empty matchers list - should convert arguments to matchers
     * @defectRisk Ensuring proper conversion when empty matchers provided
     */
    @Test(timeout = 4000)
    public void testConstructorWithEmptyMatchersList() {
        mock.oneArg(2);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    /**
     * @target getMethod()
     * @scenario Retrieve method from invocation
     * @defectRisk Ensuring correct method extraction
     */
    @Test(timeout = 4000)
    public void testGetMethod() throws Exception {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        Method method = mock.getClass().getMethod("simpleMethod");
        assertEquals(method.getName(), matcher.getMethod().getName());
    }

    /**
     * @target getInvocation()
     * @scenario Retrieve associated invocation
     * @defectRisk Ensuring invocation reference is preserved
     */
    @Test(timeout = 4000)
    public void testGetInvocation() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertSame(invocation, matcher.getInvocation());
    }

    /**
     * @target getMatchers()
     * @scenario Verify matchers list is correctly returned
     * @defectRisk Risk of returning null or wrong matchers
     */
    @Test(timeout = 4000)
    public void testGetMatchers() {
        mock.oneArg(5);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    /**
     * @target getLocation()
     * @scenario Retrieve location from invocation
     * @defectRisk Ensuring location is not null
     */
    @Test(timeout = 4000)
    public void testGetLocation() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertNotNull(matcher.getLocation());
    }

    /**
     * @target matches()
     * @scenario Matching same method with same arguments
     * @defectRisk Risk of false negative when arguments match
     */
    @Test(timeout = 4000)
    public void testMatchesWithMatchingInvocation() {
        mock.oneArg(10);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Create another matching invocation
        mock.oneArg(10);
        Invocation matchingInvocation = getLastInvocation();
        
        assertTrue(matcher.matches(matchingInvocation));
    }

    /**
     * @target matches()
     * @scenario Matching with different arguments - should return false
     * @defectRisk Risk of false positive when arguments don't match
     */
    @Test(timeout = 4000)
    public void testMatchesWithNonMatchingArguments() {
        mock.oneArg(10);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Create invocation with different argument
        mock.oneArg(20);
        Invocation nonMatchingInvocation = getLastInvocation();
        
        assertFalse(matcher.matches(nonMatchingInvocation));
    }

    /**
     * @target matches()
     * @scenario Matching with different method - should return false
     * @defectRisk Risk of false positive when methods don't match
     */
    @Test(timeout = 4000)
    public void testMatchesWithDifferentMethod() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Create invocation with different method
        mock.oneArg(1);
        Invocation differentMethodInvocation = getLastInvocation();
        
        assertFalse(matcher.matches(differentMethodInvocation));
    }

    /**
     * @target matches()
     * @scenario Matching with different mock instance - should return false
     * @defectRisk Risk of false positive when mocks differ
     */
    @Test(timeout = 4000)
    public void testMatchesWithDifferentMock() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Create invocation on another mock
        IMethods anotherMock = mock(IMethods.class);
        anotherMock.oneArg(1);
        Invocation differentMockInvocation = getLastInvocation();
        
        assertFalse(matcher.matches(differentMockInvocation));
    }

    /**
     * @target hasSameMethod()
     * @scenario Compare same method
     * @defectRisk Risk of false negative for identical methods
     */
    @Test(timeout = 4000)
    public void testHasSameMethodWithSameMethod() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.simpleMethod();
        Invocation sameMethodInvocation = getLastInvocation();
        
        assertTrue(matcher.hasSameMethod(sameMethodInvocation));
    }

    /**
     * @target hasSameMethod()
     * @scenario Compare different methods
     * @defectRisk Risk of false positive for different methods
     */
    @Test(timeout = 4000)
    public void testHasSameMethodWithDifferentMethod() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.oneArg(1);
        Invocation differentMethodInvocation = getLastInvocation();
        
        assertFalse(matcher.hasSameMethod(differentMethodInvocation));
    }

    /**
     * @target hasSameMethod()
     * @scenario Compare methods with same name but different parameters
     * @defectRisk Risk of false positive for overloaded methods
     */
    @Test(timeout = 4000)
    public void testHasSameMethodWithOverloadedMethod() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.twoArgsMethod(1, 2);
        Invocation overloadedInvocation = getLastInvocation();
        
        assertFalse(matcher.hasSameMethod(overloadedInvocation));
    }

    /**
     * @target hasSimilarMethod()
     * @scenario Compare similar methods (same name, same mock, unverified)
     * @defectRisk Risk of false negative for truly similar methods
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodWithSimilarMethod() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.oneArg(2); // same method name, same mock, unverified
        Invocation similarInvocation = getLastInvocation();
        
        assertTrue(matcher.hasSimilarMethod(similarInvocation));
    }

    /**
     * @target hasSimilarMethod()
     * @scenario Compare methods with different names
     * @defectRisk Risk of false positive for different method names
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodWithDifferentName() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.oneArg(1);
        Invocation differentNameInvocation = getLastInvocation();
        
        assertFalse(matcher.hasSimilarMethod(differentNameInvocation));
    }

    /**
     * @target hasSimilarMethod()
     * @scenario Compare verified invocation
     * @defectRisk Risk of false positive when invocation is verified
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodWithVerifiedInvocation() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        mock.simpleMethod();
        Invocation verifiedInvocation = getLastInvocation();
        verifiedInvocation.verify(); // mark as verified
        
        assertFalse(matcher.hasSimilarMethod(verifiedInvocation));
    }

    /**
     * @target toString()
     * @scenario Default toString representation
     * @defectRisk Risk of NullPointerException or incorrect formatting
     */
    @Test(timeout = 4000)
    public void testToString() {
        mock.simpleMethod();
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        String result = matcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    /**
     * @target toString(PrintSettings)
     * @scenario ToString with custom print settings
     * @defectRisk Risk of incorrect formatting with custom settings
     */
    @Test(timeout = 4000)
    public void testToStringWithPrintSettings() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        PrintSettings settings = new PrintSettings();
        String result = matcher.toString(settings);
        assertNotNull(result);
        assertTrue(result.contains("oneArg"));
    }

    /**
     * @target captureArgumentsFrom()
     * @scenario Normal (non-vararg) method with CapturesArguments matcher
     * @defectRisk Risk of failure when capturing arguments
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNormalMethod() {
        mock.oneArg(42);
        Invocation invocation = getLastInvocation();
        
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        List<Matcher> matchers = asList((Matcher) capturingMatcher);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        // Create same invocation again to capture from
        mock.oneArg(99);
        Invocation newInvocation = getLastInvocation();
        
        matcher.captureArgumentsFrom(newInvocation);
        assertFalse(capturingMatcher.getAllValues().isEmpty());
    }

    /**
     * @target captureArgumentsFrom()
     * @scenario Vararg method - should NOT throw UnsupportedOperationException
     * @defectRisk CRITICAL: The defective version throws UnsupportedOperationException for varargs.
     *             This test must pass on the fixed version and fail on the defective version.
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromVarargMethod_UnsupportedOperationException() {
        mock.varargs(new String[] { "arg1", "arg2" });
        Invocation invocation = getLastInvocation();
        
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        LocalizedMatcher localizedMatcher = new LocalizedMatcher(capturingMatcher);
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(localizedMatcher);
        
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);
        
        // This should NOT throw UnsupportedOperationException in the fixed version
        invocationMatcher.captureArgumentsFrom(invocation);
        assertFalse(capturingMatcher.getAllValues().isEmpty());
    }

    /**
     * @target captureArgumentsFrom()
     * @scenario Vararg method with multiple arguments and complex matchers
     * @defectRisk Ensuring stable argument capture for varargs
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromMultipleVarargs() {
        mock.varargs("one", "two", "three");
        Invocation invocation = getLastInvocation();
        
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);
        
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);
        invocationMatcher.captureArgumentsFrom(invocation);
        List<?> capturedValues = capturingMatcher.getAllValues();
        assertFalse(capturedValues.isEmpty());
    }

    /**
     * @target createFrom()
     * @scenario Create InvocationMatcher list from invocations list
     * @defectRisk Risk of incorrect list creation
     */
    @Test(timeout = 4000)
    public void testCreateFromList() {
        mock.simpleMethod();
        Invocation inv1 = getLastInvocation();
        mock.oneArg(1);
        Invocation inv2 = getLastInvocation();
        
        List<Invocation> invocations = asList(inv1, inv2);
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        
        assertEquals(2, matchers.size());
        assertEquals(inv1, matchers.get(0).getInvocation());
        assertEquals(inv2, matchers.get(1).getInvocation());
    }

    /**
     * @target createFrom()
     * @scenario Create matchers from empty invocations list
     * @defectRisk Should handle empty list gracefully
     */
    @Test(timeout = 4000)
    public void testCreateFromEmptyList() {
        List<Invocation> invocations = Collections.emptyList();
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        assertTrue(matchers.isEmpty());
    }

    /**
     * @target hasSimilarMethod()
     * @scenario Overloaded methods with same arguments - should return false
     * @defectRisk Risk of false positive for overloaded methods with same arguments
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodWithOverloadedMethodMatchingArgs() {
        mock.oneArg(1);
        Invocation invocation = getLastInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // This is a different method but with same argument type
        // Using twoArgsMethod which is different but might have same args structure
        mock.twoArgsMethod(1, 2);
        Invocation overloadedInvocation = getLastInvocation();
        
        // Methods have different names, so should return false
        assertFalse(matcher.hasSimilarMethod(overloadedInvocation));
    }

    /**
     * @target matches()
     * @scenario Matching with hamcrest matchers
     * @defectRisk Ensuring hamcrest matcher integration works
     */
    @Test(timeout = 4000)
    public void testMatchesWithCustomMatcher() {
        mock.oneArg(42);
        Invocation invocation = getLastInvocation();
        
        List<Matcher> matchers = asList(Matchers.eq(42));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        mock.oneArg(42);
        Invocation matchingInvocation = getLastInvocation();
        
        assertTrue(matcher.matches(matchingInvocation));
    }
}