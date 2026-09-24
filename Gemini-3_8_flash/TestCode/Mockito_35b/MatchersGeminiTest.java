/* [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.Matchers
 *
 * Branch & Defect Coverage Map:
 * - Partition A: Core Functional Logic & State Transitions
 *   - anyBoolean(), anyByte(), anyChar(), anyInt(), anyLong(), anyFloat(), anyDouble(), anyShort()
 *   - anyObject(), any(Class), any(), anyVararg()
 *   - anyString(), anyList(), anyListOf(Class), anySet(), anySetOf(Class), anyMap(), anyCollection(), anyCollectionOf(Class)
 *   - isA(Class), isNull(), notNull(), isNotNull()
 *   - eq(boolean..short, Object), refEq(Object, String...), same(Object)
 *   - contains(String), matches(String), endsWith(String), startsWith(String)
 *   - *That(Matcher): argThat, charThat, booleanThat, byteThat, shortThat, intThat, longThat, floatThat, doubleThat
 *
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Primitive numeric extremes: Byte/Short/Integer/Long/Float/Double MIN_VALUE and MAX_VALUE passed to eq()
 *   - Character bounds: Character.MIN_VALUE, Character.MAX_VALUE
 *   - Empty and special strings passed to contains(), matches(), startsWith(), endsWith()
 *
 * - Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   - Targets NPEWithCertainMatchersTest:
 *       * shouldNotThrowNPEWhenIntPassed
 *       * shouldNotThrowNPEWhenIntegerPassed
 *       * shouldNotThrowNPEWhenIntegerPassedToEq
 *       * shouldNotThrowNPEWhenIntegerPassedToSame
 *   - Root Cause: Same.describeTo(Description) invoked wanted.toString() without null-checking wanted,
 *     throwing java.lang.NullPointerException when Matchers.same(null) was used and described.
 *
 * - Partition D: ArgumentMatcherStorage & LocalizedMatcher Verification
 *   - Validation of registered Hamcrest Matcher types and execution of matches() / describeTo() contracts.
 *
 * - Partition E: Object Lifecycle & Contract Integrity
 *   - Matchers class constructor instantiation and Same quoting contract verification.
 */
package org.mockito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.After;
import org.junit.Test;
import org.mockito.internal.matchers.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchersGeminiTest {

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().getArgumentMatcherStorage().reset();
    }

    private static class DummyBean {
        private final String text;
        private final int number;

        public DummyBean(String text, int number) {
            this.text = text;
            this.number = number;
        }

        public String getText() {
            return text;
        }

        public int getNumber() {
            return number;
        }
    }

    private static class DummyMatcher<T> extends BaseMatcher<T> {
        private final boolean matchResult;

        public DummyMatcher(boolean matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public boolean matches(Object item) {
            return matchResult;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("dummyMatcher");
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAnyPrimitivesReturnValues() {
        assertFalse(Matchers.anyBoolean());
        assertEquals((byte) 0, Matchers.anyByte());
        assertEquals('\u0000', Matchers.anyChar());
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0.0d, Matchers.anyDouble(), 0.0d);
        assertEquals((short) 0, Matchers.anyShort());

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(8, matchers.size());
        for (LocalizedMatcher lm : matchers) {
            assertSame(Any.ANY, lm.getMatcher());
        }
    }

    @Test(timeout = 4000)
    public void testAnyObjectAndAliases() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.any());
        assertNull(Matchers.anyVararg());

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(5, matchers.size());
        assertSame(Any.ANY, matchers.get(0).getMatcher());
        assertSame(Any.ANY, matchers.get(1).getMatcher());
        assertSame(Any.ANY, matchers.get(2).getMatcher());
        assertSame(Any.ANY, matchers.get(3).getMatcher());
        assertSame(AnyVararg.ANY_VARARG, matchers.get(4).getMatcher());
    }

    @Test(timeout = 4000)
    public void testAnyCollectionsAndString() {
        assertEquals("", Matchers.anyString());

        List list = Matchers.anyList();
        assertNotNull(list);
        assertTrue(list.isEmpty());

        List<String> stringList = Matchers.anyListOf(String.class);
        assertNotNull(stringList);
        assertTrue(stringList.isEmpty());

        Set set = Matchers.anySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());

        Set<Integer> intSet = Matchers.anySetOf(Integer.class);
        assertNotNull(intSet);
        assertTrue(intSet.isEmpty());

        Map map = Matchers.anyMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());

        Collection col = Matchers.anyCollection();
        assertNotNull(col);
        assertTrue(col.isEmpty());

        Collection<Double> doubleCol = Matchers.anyCollectionOf(Double.class);
        assertNotNull(doubleCol);
        assertTrue(doubleCol.isEmpty());

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(8, matchers.size());
        for (LocalizedMatcher lm : matchers) {
            assertSame(Any.ANY, lm.getMatcher());
        }
    }

    @Test(timeout = 4000)
    public void testEqPrimitivesReturnValuesAndReporting() {
        assertFalse(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
        assertEquals((byte) 0, Matchers.eq((byte) 5));
        assertEquals('\u0000', Matchers.eq('k'));
        assertEquals(0.0d, Matchers.eq(3.14159), 0.0d);
        assertEquals(0.0f, Matchers.eq(2.718f), 0.0f);
        assertEquals(0, Matchers.eq(42));
        assertEquals(0L, Matchers.eq(999999L));
        assertEquals((short) 0, Matchers.eq((short) 7));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(9, matchers.size());
        for (LocalizedMatcher lm : matchers) {
            assertTrue(lm.getMatcher() instanceof Equals);
        }

        assertTrue(matchers.get(0).matches(true));
        assertFalse(matchers.get(0).matches(false));
        assertTrue(matchers.get(6).matches(42));
        assertFalse(matchers.get(6).matches(43));
    }

    @Test(timeout = 4000)
    public void testEqObjectAndRefEqAndSame() {
        assertNull(Matchers.eq("targetString"));
        assertNull(Matchers.same("sameString"));
        assertNull(Matchers.isA(Number.class));

        DummyBean bean = new DummyBean("test", 123);
        assertNull(Matchers.refEq(bean));
        assertNull(Matchers.refEq(bean, "number"));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(5, matchers.size());

        assertTrue(matchers.get(0).getMatcher() instanceof Equals);
        assertTrue(matchers.get(1).getMatcher() instanceof Same);
        assertTrue(matchers.get(2).getMatcher() instanceof InstanceOf);
        assertTrue(matchers.get(3).getMatcher() instanceof ReflectionEquals);
        assertTrue(matchers.get(4).getMatcher() instanceof ReflectionEquals);

        assertTrue(matchers.get(2).matches(Integer.valueOf(10)));
        assertFalse(matchers.get(2).matches("notANumber"));
        assertFalse(matchers.get(2).matches(null));
    }

    @Test(timeout = 4000)
    public void testNullAndNotNullMatchers() {
        assertNull(Matchers.isNull());
        assertNull(Matchers.notNull());
        assertNull(Matchers.isNotNull());

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(3, matchers.size());

        assertSame(Null.NULL, matchers.get(0).getMatcher());
        assertSame(NotNull.NOT_NULL, matchers.get(1).getMatcher());
        assertSame(NotNull.NOT_NULL, matchers.get(2).getMatcher());

        assertTrue(matchers.get(0).matches(null));
        assertFalse(matchers.get(0).matches("notNull"));
        assertFalse(matchers.get(1).matches(null));
        assertTrue(matchers.get(1).matches("notNull"));
        assertFalse(matchers.get(2).matches(null));
        assertTrue(matchers.get(2).matches(new Object()));
    }

    @Test(timeout = 4000)
    public void testStringSubtypeMatchers() {
        assertEquals("", Matchers.contains("needle"));
        assertEquals("", Matchers.matches("^[0-9]+$"));
        assertEquals("", Matchers.endsWith(".txt"));
        assertEquals("", Matchers.startsWith("https://"));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(4, matchers.size());

        Matcher containsMatcher = matchers.get(0).getMatcher();
        assertTrue(containsMatcher instanceof Contains);
        assertTrue(containsMatcher.matches("haystack needle haystack"));
        assertFalse(containsMatcher.matches("haystack"));

        Matcher matchesMatcher = matchers.get(1).getMatcher();
        assertTrue(matchesMatcher instanceof Matches);
        assertTrue(matchesMatcher.matches("12345"));
        assertFalse(matchesMatcher.matches("123a45"));

        Matcher endsWithMatcher = matchers.get(2).getMatcher();
        assertTrue(endsWithMatcher instanceof EndsWith);
        assertTrue(endsWithMatcher.matches("document.txt"));
        assertFalse(endsWithMatcher.matches("document.pdf"));

        Matcher startsWithMatcher = matchers.get(3).getMatcher();
        assertTrue(startsWithMatcher instanceof StartsWith);
        assertTrue(startsWithMatcher.matches("https://mockito.org"));
        assertFalse(startsWithMatcher.matches("http://mockito.org"));
    }

    @Test(timeout = 4000)
    public void testCustomThatMatchers() {
        DummyMatcher<Object> objMatcher = new DummyMatcher<>(true);
        DummyMatcher<Boolean> boolMatcher = new DummyMatcher<>(false);
        DummyMatcher<Byte> byteMatcher = new DummyMatcher<>(true);
        DummyMatcher<Character> charMatcher = new DummyMatcher<>(true);
        DummyMatcher<Short> shortMatcher = new DummyMatcher<>(true);
        DummyMatcher<Integer> intMatcher = new DummyMatcher<>(true);
        DummyMatcher<Long> longMatcher = new DummyMatcher<>(true);
        DummyMatcher<Float> floatMatcher = new DummyMatcher<>(true);
        DummyMatcher<Double> doubleMatcher = new DummyMatcher<>(true);

        assertNull(Matchers.argThat(objMatcher));
        assertFalse(Matchers.booleanThat(boolMatcher));
        assertEquals((byte) 0, Matchers.byteThat(byteMatcher));
        assertEquals('\u0000', Matchers.charThat(charMatcher));
        assertEquals((short) 0, Matchers.shortThat(shortMatcher));
        assertEquals(0, Matchers.intThat(intMatcher));
        assertEquals(0L, Matchers.longThat(longMatcher));
        assertEquals(0.0f, Matchers.floatThat(floatMatcher), 0.0f);
        assertEquals(0.0d, Matchers.doubleThat(doubleMatcher), 0.0d);

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(9, matchers.size());

        assertSame(objMatcher, matchers.get(0).getMatcher());
        assertSame(boolMatcher, matchers.get(1).getMatcher());
        assertSame(byteMatcher, matchers.get(2).getMatcher());
        assertSame(charMatcher, matchers.get(3).getMatcher());
        assertSame(shortMatcher, matchers.get(4).getMatcher());
        assertSame(intMatcher, matchers.get(5).getMatcher());
        assertSame(longMatcher, matchers.get(6).getMatcher());
        assertSame(floatMatcher, matchers.get(7).getMatcher());
        assertSame(doubleMatcher, matchers.get(8).getMatcher());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveBoundaryValuesToEq() {
        assertEquals((byte) 0, Matchers.eq(Byte.MIN_VALUE));
        assertEquals((byte) 0, Matchers.eq(Byte.MAX_VALUE));
        assertEquals((short) 0, Matchers.eq(Short.MIN_VALUE));
        assertEquals((short) 0, Matchers.eq(Short.MAX_VALUE));
        assertEquals(0, Matchers.eq(Integer.MIN_VALUE));
        assertEquals(0, Matchers.eq(Integer.MAX_VALUE));
        assertEquals(0L, Matchers.eq(Long.MIN_VALUE));
        assertEquals(0L, Matchers.eq(Long.MAX_VALUE));
        assertEquals(0.0f, Matchers.eq(Float.MIN_VALUE), 0.0f);
        assertEquals(0.0f, Matchers.eq(Float.MAX_VALUE), 0.0f);
        assertEquals(0.0d, Matchers.eq(Double.MIN_VALUE), 0.0d);
        assertEquals(0.0d, Matchers.eq(Double.MAX_VALUE), 0.0d);
        assertEquals('\u0000', Matchers.eq(Character.MIN_VALUE));
        assertEquals('\u0000', Matchers.eq(Character.MAX_VALUE));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(14, matchers.size());

        assertTrue(matchers.get(0).matches(Byte.MIN_VALUE));
        assertTrue(matchers.get(1).matches(Byte.MAX_VALUE));
        assertTrue(matchers.get(2).matches(Short.MIN_VALUE));
        assertTrue(matchers.get(3).matches(Short.MAX_VALUE));
        assertTrue(matchers.get(4).matches(Integer.MIN_VALUE));
        assertTrue(matchers.get(5).matches(Integer.MAX_VALUE));
        assertTrue(matchers.get(6).matches(Long.MIN_VALUE));
        assertTrue(matchers.get(7).matches(Long.MAX_VALUE));
        assertTrue(matchers.get(8).matches(Float.MIN_VALUE));
        assertTrue(matchers.get(9).matches(Float.MAX_VALUE));
        assertTrue(matchers.get(10).matches(Double.MIN_VALUE));
        assertTrue(matchers.get(11).matches(Double.MAX_VALUE));
        assertTrue(matchers.get(12).matches(Character.MIN_VALUE));
        assertTrue(matchers.get(13).matches(Character.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testEmptyStringsAndNullsInStringMatchers() {
        assertEquals("", Matchers.contains(""));
        assertEquals("", Matchers.matches(""));
        assertEquals("", Matchers.endsWith(""));
        assertEquals("", Matchers.startsWith(""));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(4, matchers.size());

        assertTrue(matchers.get(0).matches("anything"));
        assertTrue(matchers.get(0).matches(""));
        assertFalse(matchers.get(0).matches(null));

        assertTrue(matchers.get(1).matches(""));
        assertFalse(matchers.get(1).matches("nonEmpty"));
        assertFalse(matchers.get(1).matches(null));

        assertTrue(matchers.get(2).matches("anything"));
        assertTrue(matchers.get(2).matches(""));
        assertFalse(matchers.get(2).matches(null));

        assertTrue(matchers.get(3).matches("anything"));
        assertTrue(matchers.get(3).matches(""));
        assertFalse(matchers.get(3).matches(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // Targets: NPEWithCertainMatchersTest failure conditions
    // =========================================================================

    @Test(timeout = 4000)
    public void shouldNotThrowNPEWhenIntegerPassedToSame() {
        assertNull(Matchers.same(null));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(1, matchers.size());
        Matcher matcher = matchers.get(0).getMatcher();
        assertTrue(matcher instanceof Same);

        StringDescription description = new StringDescription();
        matcher.describeTo(description);

        assertEquals("same(null)", description.toString());
    }

    @Test(timeout = 4000)
    public void shouldNotThrowNPEWhenIntPassed() {
        Same sameNullMatcher = new Same(null);

        assertFalse(sameNullMatcher.matches(100));

        StringDescription description = new StringDescription();
        sameNullMatcher.describeTo(description);

        assertEquals("same(null)", description.toString());
    }

    @Test(timeout = 4000)
    public void shouldNotThrowNPEWhenIntegerPassed() {
        Same sameNullMatcher = new Same(null);

        assertFalse(sameNullMatcher.matches(Integer.valueOf(100)));
        assertTrue(sameNullMatcher.matches(null));

        StringDescription description = new StringDescription();
        sameNullMatcher.describeTo(description);

        assertEquals("same(null)", description.toString());
    }

    @Test(timeout = 4000)
    public void shouldNotThrowNPEWhenIntegerPassedToEq() {
        assertNull(Matchers.eq((Object) null));

        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(1, matchers.size());
        Matcher matcher = matchers.get(0).getMatcher();
        assertTrue(matcher instanceof Equals);

        assertFalse(matcher.matches(Integer.valueOf(100)));
        assertTrue(matcher.matches(null));

        StringDescription description = new StringDescription();
        matcher.describeTo(description);

        assertFalse(description.toString().isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatcherStorageIsolationAndCleanUp() {
        Matchers.anyInt();
        Matchers.anyString();

        List<LocalizedMatcher> pulled = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertEquals(2, pulled.size());

        List<LocalizedMatcher> pulledAgain = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        assertTrue(pulledAgain.isEmpty());
    }

    @Test(timeout = 4000)
    public void testReflectionEqualsEvaluation() {
        DummyBean expectedBean = new DummyBean("common", 100);
        DummyBean sameValuesBean = new DummyBean("common", 100);
        DummyBean differentValuesBean = new DummyBean("diff", 999);

        Matchers.refEq(expectedBean);
        List<LocalizedMatcher> matchers = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        Matcher refEqMatcher = matchers.get(0).getMatcher();

        assertTrue(refEqMatcher.matches(sameValuesBean));
        assertFalse(refEqMatcher.matches(differentValuesBean));
        assertFalse(refEqMatcher.matches(null));

        Matchers.refEq(expectedBean, "text");
        List<LocalizedMatcher> matchersExcluding = new ThreadSafeMockingProgress().getArgumentMatcherStorage().pullLocalizedMatchers();
        Matcher refEqExcludingMatcher = matchersExcluding.get(0).getMatcher();

        DummyBean differentTextOnly = new DummyBean("completelyDifferent", 100);
        assertTrue(refEqExcludingMatcher.matches(differentTextOnly));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchersClassInstantiation() {
        Matchers matchersInstance = new Matchers();
        assertNotNull(matchersInstance);
    }

    @Test(timeout = 4000)
    public void testSameMatcherQuotingAndDescriptionContract() {
        Same sameString = new Same("stringValue");
        StringDescription descString = new StringDescription();
        sameString.describeTo(descString);
        assertEquals("same(\"stringValue\")", descString.toString());

        Same sameChar = new Same('q');
        StringDescription descChar = new StringDescription();
        sameChar.describeTo(descChar);
        assertEquals("same('q')", descChar.toString());

        Same sameInt = new Same(777);
        StringDescription descInt = new StringDescription();
        sameInt.describeTo(descInt);
        assertEquals("same(777)", descInt.toString());

        Object obj = new Object();
        Same sameObj = new Same(obj);
        assertTrue(sameObj.matches(obj));
        assertFalse(sameObj.matches(new Object()));
    }
}