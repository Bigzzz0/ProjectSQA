package org.mockito;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.matchers.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsInstanceOf;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: Matchers (static methods)
 * Internal matchers tested: Any, Null, NotNull, Equals, Same, InstanceOf,
 *                           Contains, Matches, EndsWith, StartsWith, ReflectionEquals, AnyVararg
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Each static matcher method invokes reportMatcher and returns a dummy value.
 *   - Test all dummy return values for correctness (javadoc specification).
 *   - Validate that reportMatcher is called (indirectly by internal matcher state).
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for all matchers that accept objects.
 *   - Empty strings for string matchers.
 *   - Edge case: isNull/isNotNull on null and non-null.
 *   - ReflectionEquals with empty excludeFields.
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Defect: any* matchers (Any, AnyVararg) fail to match null arguments.
 *   - Test: Any.ANY.matches(null) should be true.
 *   - Test: AnyVararg.ANY_VARARG.matches(null) should be true.
 *   - Test: All primitive wrapper any* matchers (via Any) also accept null.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - equals/hashCode not required for these classes, but we rely on default.
 *   - IllegalArgumentException expected from: none (all inputs are valid).
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (stateless utility class).
 */
public class MatchersDeepseekTest {

    // ======================== Partition A: Core Return Values ========================

    @Test(timeout = 4000)
    public void anyBoolean_returnsFalse() {
        assertFalse(Matchers.anyBoolean());
    }

    @Test(timeout = 4000)
    public void anyByte_returnsZero() {
        assertEquals((byte) 0, Matchers.anyByte());
    }

    @Test(timeout = 4000)
    public void anyChar_returnsZero() {
        assertEquals((char) 0, Matchers.anyChar());
    }

    @Test(timeout = 4000)
    public void anyInt_returnsZero() {
        assertEquals(0, Matchers.anyInt());
    }

    @Test(timeout = 4000)
    public void anyLong_returnsZero() {
        assertEquals(0L, Matchers.anyLong());
    }

    @Test(timeout = 4000)
    public void anyFloat_returnsZero() {
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
    }

    @Test(timeout = 4000)
    public void anyDouble_returnsZero() {
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void anyShort_returnsZero() {
        assertEquals((short) 0, Matchers.anyShort());
    }

    @Test(timeout = 4000)
    public void anyObject_returnsNull() {
        assertNull(Matchers.anyObject());
    }

    @Test(timeout = 4000)
    public void anyVararg_returnsNull() {
        assertNull(Matchers.anyVararg());
    }

    @Test(timeout = 4000)
    public void any_withClass_returnsNull() {
        assertNull(Matchers.any(String.class));
    }

    @Test(timeout = 4000)
    public void any_returnsNullAlias() {
        assertNull(Matchers.any());
    }

    @Test(timeout = 4000)
    public void anyString_returnsEmpty() {
        assertEquals("", Matchers.anyString());
    }

    @Test(timeout = 4000)
    public void anyList_returnsEmptyList() {
        assertTrue(Matchers.anyList().isEmpty());
    }

    @Test(timeout = 4000)
    public void anyListOf_returnsEmptyList() {
        assertTrue(Matchers.anyListOf(String.class).isEmpty());
    }

    @Test(timeout = 4000)
    public void anySet_returnsEmptySet() {
        assertTrue(Matchers.anySet().isEmpty());
    }

    @Test(timeout = 4000)
    public void anySetOf_returnsEmptySet() {
        assertTrue(Matchers.anySetOf(String.class).isEmpty());
    }

    @Test(timeout = 4000)
    public void anyMap_returnsEmptyMap() {
        assertTrue(Matchers.anyMap().isEmpty());
    }

    @Test(timeout = 4000)
    public void anyMapOf_returnsEmptyMap() {
        assertTrue(Matchers.anyMapOf(String.class, Integer.class).isEmpty());
    }

    @Test(timeout = 4000)
    public void anyCollection_returnsEmptyList() {
        assertTrue(Matchers.anyCollection().isEmpty());
    }

    @Test(timeout = 4000)
    public void anyCollectionOf_returnsEmptyList() {
        assertTrue(Matchers.anyCollectionOf(String.class).isEmpty());
    }

    @Test(timeout = 4000)
    public void eq_boolean_returnsFalse() {
        assertFalse(Matchers.eq(true));
    }

    @Test(timeout = 4000)
    public void eq_byte_returnsZero() {
        assertEquals((byte) 0, Matchers.eq((byte) 42));
    }

    @Test(timeout = 4000)
    public void eq_char_returnsZero() {
        assertEquals((char) 0, Matchers.eq('A'));
    }

    @Test(timeout = 4000)
    public void eq_double_returnsZero() {
        assertEquals(0.0, Matchers.eq(3.14), 0.0);
    }

    @Test(timeout = 4000)
    public void eq_float_returnsZero() {
        assertEquals(0.0f, Matchers.eq(2.71f), 0.0f);
    }

    @Test(timeout = 4000)
    public void eq_int_returnsZero() {
        assertEquals(0, Matchers.eq(123));
    }

    @Test(timeout = 4000)
    public void eq_long_returnsZero() {
        assertEquals(0L, Matchers.eq(999L));
    }

    @Test(timeout = 4000)
    public void eq_short_returnsZero() {
        assertEquals((short) 0, Matchers.eq((short) 7));
    }

    @Test(timeout = 4000)
    public void eq_object_returnsNull() {
        assertNull(Matchers.eq("hello"));
    }

    @Test(timeout = 4000)
    public void refEq_returnsNull() {
        assertNull(Matchers.refEq("test"));
    }

    @Test(timeout = 4000)
    public void same_returnsNull() {
        assertNull(Matchers.same("x"));
    }

    @Test(timeout = 4000)
    public void isNull_returnsNull() {
        assertNull(Matchers.isNull());
    }

    @Test(timeout = 4000)
    public void isNull_withClass_returnsNull() {
        assertNull(Matchers.isNull(Object.class));
    }

    @Test(timeout = 4000)
    public void notNull_returnsNull() {
        assertNull(Matchers.notNull());
    }

    @Test(timeout = 4000)
    public void notNull_withClass_returnsNull() {
        assertNull(Matchers.notNull(Object.class));
    }

    @Test(timeout = 4000)
    public void isNotNull_returnsNull() {
        assertNull(Matchers.isNotNull());
    }

    @Test(timeout = 4000)
    public void isNotNull_withClass_returnsNull() {
        assertNull(Matchers.isNotNull(Object.class));
    }

    @Test(timeout = 4000)
    public void contains_returnsEmpty() {
        assertEquals("", Matchers.contains("sub"));
    }

    @Test(timeout = 4000)
    public void matches_returnsEmpty() {
        assertEquals("", Matchers.matches("regex"));
    }

    @Test(timeout = 4000)
    public void endsWith_returnsEmpty() {
        assertEquals("", Matchers.endsWith("suffix"));
    }

    @Test(timeout = 4000)
    public void startsWith_returnsEmpty() {
        assertEquals("", Matchers.startsWith("prefix"));
    }

    @Test(timeout = 4000)
    public void argThat_returnsNull() {
        assertNull(Matchers.argThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void charThat_returnsZero() {
        assertEquals((char) 0, Matchers.charThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void booleanThat_returnsFalse() {
        assertFalse(Matchers.booleanThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void byteThat_returnsZero() {
        assertEquals((byte) 0, Matchers.byteThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void shortThat_returnsZero() {
        assertEquals((short) 0, Matchers.shortThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void intThat_returnsZero() {
        assertEquals(0, Matchers.intThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void longThat_returnsZero() {
        assertEquals(0L, Matchers.longThat(org.hamcrest.CoreMatchers.anything()));
    }

    @Test(timeout = 4000)
    public void floatThat_returnsZero() {
        assertEquals(0.0f, Matchers.floatThat(org.hamcrest.CoreMatchers.anything()), 0.0f);
    }

    @Test(timeout = 4000)
    public void doubleThat_returnsZero() {
        assertEquals(0.0, Matchers.doubleThat(org.hamcrest.CoreMatchers.anything()), 0.0);
    }

    // ======================== Partition B: Boundary & Null Handling (Internal Matchers) ========================

    @Test(timeout = 4000)
    public void any_MatchesNullReturnsTrue() {
        assertTrue("Any matcher must match null", Any.ANY.matches(null));
    }

    @Test(timeout = 4000)
    public void any_MatchesNonNullReturnsTrue() {
        assertTrue(Any.ANY.matches("anything"));
    }

    @Test(timeout = 4000)
    public void anyVararg_MatchesNullReturnsTrue() {
        assertTrue("AnyVararg matcher must match null", AnyVararg.ANY_VARARG.matches(null));
    }

    @Test(timeout = 4000)
    public void anyVararg_MatchesNonNullReturnsTrue() {
        assertTrue(AnyVararg.ANY_VARARG.matches(new Object[]{1,2}));
    }

    @Test(timeout = 4000)
    public void null_MatchesNullReturnsTrue() {
        assertTrue(Null.NULL.matches(null));
    }

    @Test(timeout = 4000)
    public void null_MatchesNonNullReturnsFalse() {
        assertFalse(Null.NULL.matches("non-null"));
    }

    @Test(timeout = 4000)
    public void notNull_MatchesNullReturnsFalse() {
        assertFalse(NotNull.NOT_NULL.matches(null));
    }

    @Test(timeout = 4000)
    public void notNull_MatchesNonNullReturnsTrue() {
        assertTrue(NotNull.NOT_NULL.matches("any"));
    }

    @Test(timeout = 4000)
    public void equals_WithNullMatchesNull() {
        Equals eq = new Equals(null);
        assertTrue(eq.matches(null));
    }

    @Test(timeout = 4000)
    public void same_WithNullMatchesNull() {
        Same same = new Same(null);
        assertTrue(same.matches(null));
    }

    @Test(timeout = 4000)
    public void instanceOf_MatchesNullReturnsFalse() {
        InstanceOf instance = new InstanceOf(String.class);
        assertFalse(instance.matches(null));
    }

    @Test(timeout = 4000)
    public void instanceOf_MatchesInstance() {
        InstanceOf instance = new InstanceOf(String.class);
        assertTrue(instance.matches("test"));
    }

    @Test(timeout = 4000)
    public void contains_MatchesSubstring() {
        Contains c = new Contains("abc");
        assertTrue(c.matches("xyzabc123"));
    }

    @Test(timeout = 4000)
    public void contains_DoesNotMatchNull() {
        Contains c = new Contains("abc");
        assertFalse(c.matches(null));
    }

    @Test(timeout = 4000)
    public void matches_RegexMatches() {
        Matches m = new Matches("\\d+");
        assertTrue(m.matches("123"));
    }

    @Test(timeout = 4000)
    public void matches_RegexDoesNotMatchNull() {
        Matches m = new Matches(".*");
        assertFalse(m.matches(null));
    }

    @Test(timeout = 4000)
    public void endsWith_MatchesSuffix() {
        EndsWith e = new EndsWith("suffix");
        assertTrue(e.matches("prefixsuffix"));
    }

    @Test(timeout = 4000)
    public void endsWith_DoesNotMatchNull() {
        EndsWith e = new EndsWith("suffix");
        assertFalse(e.matches(null));
    }

    @Test(timeout = 4000)
    public void startsWith_MatchesPrefix() {
        StartsWith s = new StartsWith("pre");
        assertTrue(s.matches("prefix"));
    }

    @Test(timeout = 4000)
    public void startsWith_DoesNotMatchNull() {
        StartsWith s = new StartsWith("pre");
        assertFalse(s.matches(null));
    }

    @Test(timeout = 4000)
    public void reflectionEquals_NullObject() {
        ReflectionEquals r = new ReflectionEquals(null);
        assertTrue(r.matches(null));
    }

    @Test(timeout = 4000)
    public void reflectionEquals_WithExcludeFields() {
        // Just ensure it doesn't throw
        ReflectionEquals r = new ReflectionEquals("test", "nonExistentField");
        assertFalse(r.matches(null));
    }

    // ======================== Partition C: Defect-Targeted Tests (Null Acceptance) ========================

    /**
     * Core defect: Any matcher and its derivations must accept null.
     * This test directly validates the fix for the known failing test cases.
     */
    @Test(timeout = 4000)
    public void anyMatcher_AcceptsNull_DefectRevealing() {
        // Simulate the behavior of any* matchers for wrapper types
        assertTrue("Any.ANY must match null", Any.ANY.matches(null));
        assertTrue("AnyVararg.ANY_VARARG must match null", AnyVararg.ANY_VARARG.matches(null));

        // Additional check: The dummy return values from any* methods should be
        // consistent with the matcher's ability to accept null.
        // For object-returning matchers, the dummy value is null.
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.anyVararg());

        // The dummy return for anyString is empty string; but the matcher must still accept null.
        // We test that via Any.ANY directly.
        assertTrue(Any.ANY.matches(null));
        assertTrue(Any.ANY.matches(""));
    }
}