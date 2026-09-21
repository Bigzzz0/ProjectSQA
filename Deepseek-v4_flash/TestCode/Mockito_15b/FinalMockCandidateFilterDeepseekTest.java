package org.mockito.internal.configuration.injection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Decision points in FinalMockCandidateFilter.filterCandidate():
 * - D1: mocks.size() == 1 (TRUE/FALSE)
 *   - TRUE: creates injector that sets field; thenInject() returns true (path1)
 *   - FALSE: creates injector that returns false (path2)
 *
 * Sub-branches inside path1:
 * - FieldSetter.set() may succeed or throw Exception -> catch block wraps in MockitoException
 *
 * Defect (CVE-like description):
 *   The injector returned when mocks.size()==1 directly sets the field via reflection,
 *   bypassing any available property setter. The correct behavior (according to the
 *   bug report) is to try the setter first. Our test "shouldInjectUsingSetterIfAvailable"
 *   verifies that a setter IS called; on the defective version the setter is skipped,
 *   causing an assertion failure.
 *
 * Boundary/edge conditions:
 * - Empty collection, multiple candidates (covers path2)
 * - Null field, null fieldInstance (NullPointerException expected)
 * - Incompatible field type causing FieldSetter to throw (MockitoException expected)
 *
 * Partitions:
 * A: Core functional logic – single mock injection (success path)
 * B: BVA – empty, multiple candidates; null arguments
 * C: Defect-targeted – setter vs field access ordering
 * D: Exception paths – injection failure (bad type)
 * E: Contract – injector return value, boolean contracts
 */
public class FinalMockCandidateFilterDeepseekTest {

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------
    static class WithSetter {
        private String value;
        private boolean setterCalled = false;

        public void setValue(String value) {
            this.value = value;
            this.setterCalled = true;
        }

        public String getValue() {
            return value;
        }
    }

    static class WithoutSetter {
        private String value;
    }

    static class IntField {
        private int number;
    }

    // ------------------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testInjectSingleMock_shouldSetFieldSuccessfully() throws Exception {
        // Given: exactly one mock
        WithSetter obj = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("injected");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, field, obj);

        // When
        boolean result = injecter.thenInject();

        // Then: field set directly (buggy behavior: setter not called)
        assertTrue("Injection should have succeeded", result);
        assertEquals("Field should be set", "injected", obj.value);
        // Note: In the buggy version, setterCalled remains false (defect revealed)
        // We'll test setter invocation separately in the defect-targeted section.
    }

    // ------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyMocksCollection_shouldReturnFalse() throws Exception {
        WithSetter obj = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");
        Collection<Object> empty = new ArrayList<>();

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(empty, field, obj);

        boolean result = injecter.thenInject();
        assertFalse("No mocks should yield no injection", result);
    }

    @Test(timeout = 4000)
    public void testMultipleMocksCollection_shouldReturnFalse() throws Exception {
        WithSetter obj = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");
        Collection<Object> multi = new ArrayList<>();
        multi.add("mock1");
        multi.add("mock2");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(multi, field, obj);

        boolean result = injecter.thenInject();
        assertFalse("Multiple mocks should yield no injection", result);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullField_shouldThrowNullPointer() {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Collection<Object> mocks = Collections.singletonList("mock");
        // field == null
        filter.filterCandidate(mocks, null, new WithSetter());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullFieldInstance_shouldThrowNullPointer() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Collection<Object> mocks = Collections.singletonList("mock");
        Field field = WithSetter.class.getDeclaredField("value");
        // fieldInstance == null
        filter.filterCandidate(mocks, field, null);
    }

    // ------------------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (setter vs field access)
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void shouldInjectUsingSetterIfAvailable() throws Exception {
        // This test directly targets the known defect:
        // In the buggy version, FieldSetter bypasses the setter.
        // In a correct implementation, the setter should be invoked.
        // We verify that the setter was called by checking a flag.
        WithSetter obj = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("injected");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, field, obj);
        injecter.thenInject();

        // The defective version sets the field directly, leaving setterCalled == false.
        // This assertion will fail on the buggy version, revealing the defect.
        assertTrue("Property setter should have been invoked", obj.setterCalled);
        // Also check the value was set (via setter)
        assertEquals("injected", obj.getValue());
    }

    // ------------------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ------------------------------------------------------------------------

    @Test(timeout = 4000, expected = org.mockito.exceptions.base.MockitoException.class)
    public void testIncompatibleFieldType_shouldThrowMockitoException() throws Exception {
        // Field type int, mock is String -> FieldSetter will throw
        IntField obj = new IntField();
        Field field = IntField.class.getDeclaredField("number");
        Collection<Object> mocks = Collections.singletonList("notAnInt");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter = filter.filterCandidate(mocks, field, obj);
        injecter.thenInject(); // Should throw MockitoException
    }

    // ------------------------------------------------------------------------
    // Partition E: Object lifecycle & contract integrity
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMultipleInjectors_independent() throws Exception {
        // Verify that each call creates a fresh injector with correct behaviour
        WithSetter obj1 = new WithSetter();
        WithSetter obj2 = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        OngoingInjecter injecter1 = filter.filterCandidate(Collections.singletonList("a"), field, obj1);
        OngoingInjecter injecter2 = filter.filterCandidate(Collections.emptyList(), field, obj2);

        assertTrue(injecter1.thenInject());
        assertFalse(injecter2.thenInject());
        assertEquals("a", obj1.value);
        assertNull(obj2.value);
    }

    @Test(timeout = 4000)
    public void testInjectorReturnContracts() throws Exception {
        WithSetter obj = new WithSetter();
        Field field = WithSetter.class.getDeclaredField("value");

        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();

        // Single mock -> true
        OngoingInjecter injecterTrue = filter.filterCandidate(Collections.singletonList("x"), field, obj);
        assertTrue(injecterTrue.thenInject());

        // No mock -> false
        OngoingInjecter injecterFalse = filter.filterCandidate(Collections.emptyList(), field, obj);
        assertFalse(injecterFalse.thenInject());
    }
}