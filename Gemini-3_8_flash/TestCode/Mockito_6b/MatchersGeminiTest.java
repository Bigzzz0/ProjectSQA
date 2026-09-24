package org.mockito;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mockito.Matchers
 * Known Defects4J Failure:
 *   - AnyXMatchersAcceptNullsTest::shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers
 *   - AnyXMatchersAcceptNullsTest::shouldNotAcceptNullInAnyXMatchers
 *   - MatchersTest::anyStringMatcher
 *   - NewMatchersTest::shouldAllowAnyCollection / shouldAllowAnyList / shouldAllowAnyMap / shouldAllowAnySet
 * Root Cause:
 *   Matchers like anyString(), anyList(), anySet(), anyMap(), anyCollection(), anyInt(), etc., in the
 *   defective version erroneously report Any.ANY instead of instance-type-checked matchers. Any.ANY
 *   matches null, causing incorrect invocation matching during stubbing/verification when null is passed.
 *
 * Test Partitions:
 *   - Partition A: Core Functional Logic & State Transitions (Static matcher invocation, dummy return values)
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes (null inputs, empty values, min/max values)
 *   - Partition C: Defect-Targeted Branch Zone (Explicit verification that anyX matchers reject null)
 *   - Partition D: Defensive & Semantic Matcher Behavior (Matching truth tables on reported matchers)
 *   - Partition E: Object Lifecycle & Contract Integrity (Class instantiation, state isolation)
 * ====================================================================================================
 */
public class MatchersGeminiTest {

    private final MockingProgress mockingProgress = new ThreadSafeMockingProgress();

    @Before
    public void setUp() {
        mockingProgress.getArgumentMatcherStorage().reset();
    }

    @After
    public void tearDown() {
        mockingProgress.getArgumentMatcherStorage().reset();
    }

    private Matcher<?> popMatcher() {
        List<?> matchers = mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers();
        assertNotNull("Matchers list must not be null", matchers);
        assertEquals("Matcher stack must contain exactly one matcher", 1, matchers.size());
        return (Matcher<?>) matchers.get(0);
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & Return Value Contracts
    // ================================================================================================

    @Test(timeout = 4000)
    public void testPrimitiveAnyReturnDefaults() {
        assertFalse(Matchers.anyBoolean());
        assertEquals(0, Matchers.anyByte());
        assertEquals('\u0000', Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0001f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0001d);
        assertEquals((short) 0, Matchers.anyShort());

        List<?> matchers = mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(8, matchers.size());
    }

    @Test(timeout = 4000)
    public void testObjectAnyReturnDefaults() {
        assertNull(Matchers.anyObject());
        popMatcher();

        assertNull(Matchers.any());
        popMatcher();

        assertNull(Matchers.anyVararg());
        popMatcher();

        assertNull(Matchers.any(String.class));
        popMatcher();

        assertEquals(Integer.valueOf(0), (Integer) Matchers.any(int.class));
        popMatcher();
    }

    @Test(timeout = 4000)
    public void testStringAndCollectionAnyReturnDefaults() {
        assertEquals("", Matchers.anyString());
        popMatcher();

        List<?> list = Matchers.anyList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
        popMatcher();

        List<String> typedList = Matchers.anyListOf(String.class);
        assertNotNull(typedList);
        assertTrue(typedList.isEmpty());
        popMatcher();

        Set<?> set = Matchers.anySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());
        popMatcher();

        Set<String> typedSet = Matchers.anySetOf(String.class);
        assertNotNull(typedSet);
        assertTrue(typedSet.isEmpty());
        popMatcher();

        Map<?, ?> map = Matchers.anyMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
        popMatcher();

        Map<String, Integer> typedMap = Matchers.anyMapOf(String.class, Integer.class);
        assertNotNull(typedMap);
        assertTrue(typedMap.isEmpty());
        popMatcher();

        Collection<?> col = Matchers.anyCollection();
        assertNotNull(col);
        assertTrue(col.isEmpty());
        popMatcher();

        Collection<String> typedCol = Matchers.anyCollectionOf(String.class);
        assertNotNull(typedCol);
        assertTrue(typedCol.isEmpty());
        popMatcher();
    }

    @Test(timeout = 4000)
    public void testStringPatternsReturnDefaults() {
        assertEquals("", Matchers.contains("sub"));
        popMatcher();

        assertEquals("", Matchers.matches("^[a-z]+$"));
        popMatcher();

        assertEquals("", Matchers.startsWith("pre"));
        popMatcher();

        assertEquals("", Matchers.endsWith("suf"));
        popMatcher();
    }

    @Test(timeout = 4000)
    public void testNullCheckReturnDefaults() {
        assertNull(Matchers.isNull());
        popMatcher();

        assertNull(Matchers.isNull(String.class));
        popMatcher();

        assertNull(Matchers.notNull());
        popMatcher();

        assertNull(Matchers.notNull(String.class));
        popMatcher();

        assertNull(Matchers.isNotNull());
        popMatcher();

        assertNull(Matchers.isNotNull(String.class));
        popMatcher();
    }

    @Test(timeout = 4000)
    public void testCustomThatReturnDefaults() {
        Matcher<Object> dummy = new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                return true;
            }
        };

        assertNull(Matchers.argThat(dummy));
        popMatcher();

        assertFalse(Matchers.booleanThat(null));
        popMatcher();

        assertEquals(0, Matchers.byteThat(null));
        popMatcher();

        assertEquals('\u0000', Matchers.charThat(null));
        popMatcher();

        assertEquals(0, Matchers.intThat(null));
        popMatcher();

        assertEquals(0L, Matchers.longThat(null));
        popMatcher();

        assertEquals(0.0f, Matchers.floatThat(null), 0.0001f);
        popMatcher();

        assertEquals(0.0d, Matchers.doubleThat(null), 0.0001d);
        popMatcher();

        assertEquals((short) 0, Matchers.shortThat(null));
        popMatcher();
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEqPrimitiveBoundaries() {
        assertFalse(Matchers.eq(false));
        Matcher<?> mBool = popMatcher();
        assertTrue(mBool.matches(false));
        assertFalse(mBool.matches(true));

        assertEquals(0, Matchers.eq(Byte.MIN_VALUE));
        Matcher<?> mByte = popMatcher();
        assertTrue(mByte.matches(Byte.MIN_VALUE));
        assertFalse(mByte.matches(Byte.MAX_VALUE));

        assertEquals('\u0000', Matchers.eq(Character.MAX_VALUE));
        Matcher<?> mChar = popMatcher();
        assertTrue(mChar.matches(Character.MAX_VALUE));
        assertFalse(mChar.matches('\u0000'));

        assertEquals(0, Matchers.eq(Integer.MAX_VALUE));
        Matcher<?> mInt = popMatcher();
        assertTrue(mInt.matches(Integer.MAX_VALUE));
        assertFalse(mInt.matches(Integer.MIN_VALUE));

        assertEquals(0L, Matchers.eq(Long.MIN_VALUE));
        Matcher<?> mLong = popMatcher();
        assertTrue(mLong.matches(Long.MIN_VALUE));
        assertFalse(mLong.matches(0L));

        assertEquals(0.0f, Matchers.eq(Float.MAX_VALUE), 0.0001f);
        Matcher<?> mFloat = popMatcher();
        assertTrue(mFloat.matches(Float.MAX_VALUE));
        assertFalse(mFloat.matches(Float.MIN_VALUE));

        assertEquals(0.0d, Matchers.eq(Double.MIN_VALUE), 0.0001d);
        Matcher<?> mDouble = popMatcher();
        assertTrue(mDouble.matches(Double.MIN_VALUE));
        assertFalse(mDouble.matches(Double.MAX_VALUE));

        assertEquals((short) 0, Matchers.eq(Short.MAX_VALUE));
        Matcher<?> mShort = popMatcher();
        assertTrue(mShort.matches(Short.MAX_VALUE));
        assertFalse(mShort.matches((short) 0));
    }

    @Test(timeout = 4000)
    public void testEqObjectBoundaries() {
        String testStr = "mockito_test";
        assertEquals(testStr, Matchers.eq(testStr));
        Matcher<?> mEq = popMatcher();
        assertTrue(mEq.matches(new String("mockito_test")));
        assertFalse(mEq.matches("other"));
        assertFalse(mEq.matches(null));

        assertNull(Matchers.eq((Object) null));
        Matcher<?> mNull = popMatcher();
        assertTrue(mNull.matches(null));
        assertFalse(mNull.matches("not_null"));
    }

    @Test(timeout = 4000)
    public void testSameIdentityBoundaries() {
        String s1 = new String("identical");
        String s2 = new String("identical");

        assertSame(s1, Matchers.same(s1));
        Matcher<?> mSame = popMatcher();
        assertTrue(mSame.matches(s1));
        assertFalse(mSame.matches(s2));
        assertFalse(mSame.matches(null));

        assertNull(Matchers.same((Object) null));
        Matcher<?> mSameNull = popMatcher();
        assertTrue(mSameNull.matches(null));
        assertFalse(mSameNull.matches(s1));
    }

    @Test(timeout = 4000)
    public void testRefEqExclusions() {
        class SampleBean {
            final int id;
            final String name;

            SampleBean(int id, String name) {
                this.id = id;
                this.name = name;
            }
        }

        SampleBean expected = new SampleBean(1, "Alpha");
        assertNull(Matchers.refEq(expected, "name"));
        Matcher<?> mRef = popMatcher();

        assertTrue(mRef.matches(new SampleBean(1, "DifferentName")));
        assertFalse(mRef.matches(new SampleBean(2, "Alpha")));
        assertFalse(mRef.matches(null));
    }

    @Test(timeout = 4000)
    public void testStringSearchMatchersBoundaries() {
        Matchers.contains("target");
        Matcher<?> mContains = popMatcher();
        assertTrue(mContains.matches("prefix_target_suffix"));
        assertFalse(mContains.matches("other"));
        assertFalse(mContains.matches(null));

        Matchers.startsWith("start");
        Matcher<?> mStart = popMatcher();
        assertTrue(mStart.matches("start_of_string"));
        assertFalse(mStart.matches("middle_start"));
        assertFalse(mStart.matches(null));

        Matchers.endsWith("end");
        Matcher<?> mEnd = popMatcher();
        assertTrue(mEnd.matches("string_end"));
        assertFalse(mEnd.matches("end_string"));
        assertFalse(mEnd.matches(null));

        Matchers.matches("\\d{3}");
        Matcher<?> mRegex = popMatcher();
        assertTrue(mRegex.matches("123"));
        assertFalse(mRegex.matches("12a"));
        assertFalse(mRegex.matches(null));
    }

    @Test(timeout = 4000)
    public void testNullAndNotNullMatcherTruthTables() {
        Matchers.isNull();
        Matcher<?> mNull = popMatcher();
        assertTrue(mNull.matches(null));
        assertFalse(mNull.matches("val"));

        Matchers.isNull(String.class);
        Matcher<?> mNullTyped = popMatcher();
        assertTrue(mNullTyped.matches(null));
        assertFalse(mNullTyped.matches("val"));

        Matchers.notNull();
        Matcher<?> mNotNull = popMatcher();
        assertFalse(mNotNull.matches(null));
        assertTrue(mNotNull.matches("val"));

        Matchers.isNotNull();
        Matcher<?> mIsNotNull = popMatcher();
        assertFalse(mIsNotNull.matches(null));
        assertTrue(mIsNotNull.matches("val"));

        Matchers.isNotNull(String.class);
        Matcher<?> mIsNotNullTyped = popMatcher();
        assertFalse(mIsNotNullTyped.matches(null));
        assertTrue(mIsNotNullTyped.matches("val"));
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Null-Safety of Any-Family Matchers)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testAnyStringMustNotAcceptNull() {
        Matchers.anyString();
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyString() must NOT match null", matcher.matches(null));
        assertTrue("anyString() must match non-null String", matcher.matches("validString"));
        assertFalse("anyString() must NOT match non-String instances", matcher.matches(12345));
    }

    @Test(timeout = 4000)
    public void testAnyListMustNotAcceptNull() {
        Matchers.anyList();
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyList() must NOT match null", matcher.matches(null));
        assertTrue("anyList() must match empty List", matcher.matches(Collections.emptyList()));
        assertFalse("anyList() must NOT match non-List instance", matcher.matches("stringNotList"));
    }

    @Test(timeout = 4000)
    public void testAnyListOfClassMustNotAcceptNull() {
        Matchers.anyListOf(String.class);
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyListOf(Class) must NOT match null", matcher.matches(null));
        assertTrue("anyListOf(Class) must match empty List", matcher.matches(Collections.emptyList()));
    }

    @Test(timeout = 4000)
    public void testAnySetMustNotAcceptNull() {
        Matchers.anySet();
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anySet() must NOT match null", matcher.matches(null));
        assertTrue("anySet() must match empty Set", matcher.matches(Collections.emptySet()));
        assertFalse("anySet() must NOT match non-Set instance", matcher.matches("stringNotSet"));
    }

    @Test(timeout = 4000)
    public void testAnySetOfClassMustNotAcceptNull() {
        Matchers.anySetOf(String.class);
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anySetOf(Class) must NOT match null", matcher.matches(null));
        assertTrue("anySetOf(Class) must match empty Set", matcher.matches(Collections.emptySet()));
    }

    @Test(timeout = 4000)
    public void testAnyMapMustNotAcceptNull() {
        Matchers.anyMap();
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyMap() must NOT match null", matcher.matches(null));
        assertTrue("anyMap() must match empty Map", matcher.matches(Collections.emptyMap()));
        assertFalse("anyMap() must NOT match non-Map instance", matcher.matches("stringNotMap"));
    }

    @Test(timeout = 4000)
    public void testAnyMapOfClassesMustNotAcceptNull() {
        Matchers.anyMapOf(String.class, Integer.class);
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyMapOf(Class, Class) must NOT match null", matcher.matches(null));
        assertTrue("anyMapOf(Class, Class) must match empty Map", matcher.matches(Collections.emptyMap()));
    }

    @Test(timeout = 4000)
    public void testAnyCollectionMustNotAcceptNull() {
        Matchers.anyCollection();
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyCollection() must NOT match null", matcher.matches(null));
        assertTrue("anyCollection() must match empty List as Collection", matcher.matches(Collections.emptyList()));
        assertFalse("anyCollection() must NOT match non-Collection", matcher.matches(12345));
    }

    @Test(timeout = 4000)
    public void testAnyCollectionOfClassMustNotAcceptNull() {
        Matchers.anyCollectionOf(String.class);
        Matcher<?> matcher = popMatcher();
        assertFalse("Defect: anyCollectionOf(Class) must NOT match null", matcher.matches(null));
        assertTrue("anyCollectionOf(Class) must match empty Collection", matcher.matches(Collections.emptyList()));
    }

    @Test(timeout = 4000)
    public void testAnyPrimitiveWrapperMatchersMustNotAcceptNull() {
        Matchers.anyInt();
        Matcher<?> mInt = popMatcher();
        assertFalse("Defect: anyInt() must NOT match null", mInt.matches(null));
        assertTrue("anyInt() must match Integer", mInt.matches(42));

        Matchers.anyBoolean();
        Matcher<?> mBool = popMatcher();
        assertFalse("Defect: anyBoolean() must NOT match null", mBool.matches(null));
        assertTrue("anyBoolean() must match Boolean", mBool.matches(Boolean.TRUE));

        Matchers.anyByte();
        Matcher<?> mByte = popMatcher();
        assertFalse("Defect: anyByte() must NOT match null", mByte.matches(null));
        assertTrue("anyByte() must match Byte", mByte.matches((byte) 7));

        Matchers.anyChar();
        Matcher<?> mChar = popMatcher();
        assertFalse("Defect: anyChar() must NOT match null", mChar.matches(null));
        assertTrue("anyChar() must match Character", mChar.matches('k'));

        Matchers.anyShort();
        Matcher<?> mShort = popMatcher();
        assertFalse("Defect: anyShort() must NOT match null", mShort.matches(null));
        assertTrue("anyShort() must match Short", mShort.matches((short) 13));

        Matchers.anyLong();
        Matcher<?> mLong = popMatcher();
        assertFalse("Defect: anyLong() must NOT match null", mLong.matches(null));
        assertTrue("anyLong() must match Long", mLong.matches(123456789L));

        Matchers.anyFloat();
        Matcher<?> mFloat = popMatcher();
        assertFalse("Defect: anyFloat() must NOT match null", mFloat.matches(null));
        assertTrue("anyFloat() must match Float", mFloat.matches(3.1415f));

        Matchers.anyDouble();
        Matcher<?> mDouble = popMatcher();
        assertFalse("Defect: anyDouble() must NOT match null", mDouble.matches(null));
        assertTrue("anyDouble() must match Double", mDouble.matches(2.71828d));
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testIsAMatcherTypeChecking() {
        assertNull(Matchers.isA(String.class));
        Matcher<?> mIsA = popMatcher();

        assertTrue(mIsA.matches("test string"));
        assertFalse(mIsA.matches(12345));
        assertFalse(mIsA.matches(null));

        assertEquals(Integer.valueOf(0), (Integer) Matchers.isA(int.class));
        Matcher<?> mIsAPrimitive = popMatcher();
        assertTrue(mIsAPrimitive.matches(100));
        assertFalse(mIsAPrimitive.matches("not an int"));
        assertFalse(mIsAPrimitive.matches(null));
    }

    @Test(timeout = 4000)
    public void testAnyObjectAndAnyMatchesNullPerContract() {
        // anyObject() and any() retain permissive behavior matching null per contract specification
        assertNull(Matchers.anyObject());
        Matcher<?> mAnyObj = popMatcher();
        assertTrue(mAnyObj.matches(null));
        assertTrue(mAnyObj.matches("any value"));

        assertNull(Matchers.any());
        Matcher<?> mAny = popMatcher();
        assertTrue(mAny.matches(null));
        assertTrue(mAny.matches(new Object()));
    }

    @Test(timeout = 4000)
    public void testCustomArgumentMatcherFiltering() {
        ArgumentMatcher<Integer> isPositive = new ArgumentMatcher<Integer>() {
            @Override
            public boolean matches(Object argument) {
                return (argument instanceof Integer) && ((Integer) argument) > 0;
            }
        };

        assertEquals(0, Matchers.intThat(isPositive));
        Matcher<?> matcher = popMatcher();

        assertTrue(matcher.matches(10));
        assertFalse(matcher.matches(-5));
        assertFalse(matcher.matches(0));
        assertFalse(matcher.matches(null));
        assertFalse(matcher.matches("notInteger"));
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testMatchersInstantiation() {
        Matchers matchers = new Matchers();
        assertNotNull("Matchers instance should be constructible", matchers);
    }
}