package org.apache.commons.cli2.option;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * White-box test suite for OptionImpl.
 * Targets all concrete methods, branches, and known defects.
 *
 * [Branch & Defect Analysis Matrix]
 * - canProcess(ListIterator): branch on hasNext() true/false; delegates to canProcess(String)
 * - equals: branches on instanceof, then field comparisons with null handling
 * - hashCode: branches on null preferredName/description; no null check for prefixes/triggers (defect)
 * - findOption: branch on triggers.contains(trigger)
 * - checkPrefixes: branch on prefixes.isEmpty(); then checks preferredName and triggers; calls checkPrefix
 * - checkPrefix: iterates prefixes; returns if trigger starts with prefix; else throws IllegalArgumentException
 * - Known defect: hashCode throws NPE if getPrefixes() or getTriggers() returns null (missing null guard)
 * - Known defect: equals may not consider required field (but not in spec)
 * - Targeting BugCLI123 and ordering issues via hashCode/equals contract
 */
public class OptionImplDeepseekTest {

    // Concrete subclass for testing
    private static class TestOptionImpl extends OptionImpl {
        private final String preferredName;
        private final String description;
        private final Set<String> prefixes;
        private final Set<String> triggers;

        public TestOptionImpl(int id, boolean required,
                              String preferredName, String description,
                              Set<String> prefixes, Set<String> triggers) {
            super(id, required);
            this.preferredName = preferredName;
            this.description = description;
            this.prefixes = prefixes;
            this.triggers = triggers;
        }

        @Override
        public String getPreferredName() { return preferredName; }

        @Override
        public String getDescription() { return description; }

        @Override
        public Set<String> getPrefixes() { return prefixes; }

        @Override
        public Set<String> getTriggers() { return triggers; }

        @Override
        public void appendUsage(StringBuffer buffer, Set displaySettings, Comparator comparator) {
            buffer.append(preferredName);
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return triggers.contains(argument);
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator arguments) {
            // no-op
        }

        @Override
        public void validate(WriteableCommandLine commandLine, int id) {
            // no-op
        }

        @Override
        public void process(WriteableCommandLine commandLine, String argument) {
            // no-op
        }
    }

    // Helper to create a basic option
    private TestOptionImpl createBasicOption(int id, boolean required) {
        Set<String> prefixes = new HashSet<>(Arrays.asList("--", "-"));
        Set<String> triggers = new HashSet<>(Arrays.asList("--foo", "-f"));
        return new TestOptionImpl(id, required, "foo", "description", prefixes, triggers);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        TestOptionImpl opt = new TestOptionImpl(42, true, "bar", "desc", null, null);
        assertEquals(42, opt.getId());
        assertTrue(opt.isRequired());
    }

    @Test(timeout = 4000)
    public void testCanProcessWithListIteratorHasNextTrueAndCanProcessTrue() {
        TestOptionImpl opt = createBasicOption(1, false);
        WriteableCommandLine wcl = null; // not used in this canProcess
        ListIterator<String> args = Arrays.asList("--foo", "other").listIterator();
        assertTrue(opt.canProcess(wcl, args));
        // After canProcess, iterator should be at previous position
        assertEquals("--foo", args.next());
    }

    @Test(timeout = 4000)
    public void testCanProcessWithListIteratorHasNextTrueAndCanProcessFalse() {
        TestOptionImpl opt = createBasicOption(1, false);
        WriteableCommandLine wcl = null;
        ListIterator<String> args = Arrays.asList("--bar", "other").listIterator();
        assertFalse(opt.canProcess(wcl, args));
        assertEquals("--bar", args.next());
    }

    @Test(timeout = 4000)
    public void testCanProcessWithListIteratorHasNextFalse() {
        TestOptionImpl opt = createBasicOption(1, false);
        WriteableCommandLine wcl = null;
        ListIterator<String> args = Collections.emptyList().listIterator();
        assertFalse(opt.canProcess(wcl, args));
    }

    @Test(timeout = 4000)
    public void testToString() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertEquals("foo", opt.toString());
    }

    @Test(timeout = 4000)
    public void testFindOptionFound() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertSame(opt, opt.findOption("--foo"));
    }

    @Test(timeout = 4000)
    public void testFindOptionNotFound() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertNull(opt.findOption("--baz"));
    }

    @Test(timeout = 4000)
    public void testDefaultsDoesNothing() {
        TestOptionImpl opt = createBasicOption(1, false);
        opt.defaults(null); // should not throw
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertTrue(opt.equals(opt));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertFalse(opt.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertFalse(opt.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsIdenticalFields() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = createBasicOption(1, false);
        assertTrue(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentId() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = createBasicOption(2, false);
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPreferredName() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "bar", "description",
                new HashSet<>(Arrays.asList("--", "-")),
                new HashSet<>(Arrays.asList("--foo", "-f")));
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDescription() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "other",
                new HashSet<>(Arrays.asList("--", "-")),
                new HashSet<>(Arrays.asList("--foo", "-f")));
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPrefixes() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "description",
                new HashSet<>(Arrays.asList("+")),
                new HashSet<>(Arrays.asList("--foo", "-f")));
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTriggers() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "description",
                new HashSet<>(Arrays.asList("--", "-")),
                new HashSet<>(Arrays.asList("--bar")));
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullFields() {
        TestOptionImpl opt1 = new TestOptionImpl(1, false, null, null, null, null);
        TestOptionImpl opt2 = new TestOptionImpl(1, false, null, null, null, null);
        assertTrue(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullPreferredName() {
        TestOptionImpl opt1 = new TestOptionImpl(1, false, null, "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        TestOptionImpl opt = createBasicOption(1, false);
        int h1 = opt.hashCode();
        int h2 = opt.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualObjects() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = createBasicOption(1, false);
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullPreferredName() {
        TestOptionImpl opt = new TestOptionImpl(1, false, null, "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        opt.hashCode(); // should not throw
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullDescription() {
        TestOptionImpl opt = new TestOptionImpl(1, false, "foo", null,
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        opt.hashCode(); // should not throw
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets known defect: hashCode throws NullPointerException when getPrefixes() returns null.
     * This test expects no exception; on buggy version it fails.
     */
    @Test(timeout = 4000)
    public void testHashCodeWithNullPrefixes() {
        TestOptionImpl opt = new TestOptionImpl(1, false, "foo", "desc", null, new HashSet<String>());
        try {
            opt.hashCode();
        } catch (NullPointerException e) {
            fail("hashCode should not throw NPE when getPrefixes() is null");
        }
    }

    /**
     * Targets known defect: hashCode throws NullPointerException when getTriggers() returns null.
     */
    @Test(timeout = 4000)
    public void testHashCodeWithNullTriggers() {
        TestOptionImpl opt = new TestOptionImpl(1, false, "foo", "desc", new HashSet<String>(), null);
        try {
            opt.hashCode();
        } catch (NullPointerException e) {
            fail("hashCode should not throw NPE when getTriggers() is null");
        }
    }

    /**
     * Targets ordering defect: equals/hashCode contract must be consistent.
     * If two objects are equal, they must have same hashCode.
     */
    @Test(timeout = 4000)
    public void testEqualsHashCodeContract() {
        TestOptionImpl opt1 = createBasicOption(1, false);
        TestOptionImpl opt2 = createBasicOption(1, false);
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCheckPrefixesThrowsWhenTriggerHasNoPrefix() {
        // triggers that do not start with any prefix
        Set<String> prefixes = new HashSet<>(Arrays.asList("--", "-"));
        Set<String> triggers = new HashSet<>(Arrays.asList("foo")); // no prefix
        TestOptionImpl opt = new TestOptionImpl(1, false, "foo", "desc", prefixes, triggers);
        // checkPrefixes is protected, we can call it via reflection or make it accessible?
        // Since it's protected, we can call it from within the same package.
        // We'll create a subclass that exposes it.
        // But we are already in the same package, so we can call it directly.
        opt.checkPrefixes(prefixes);
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesWithEmptyPrefixes() {
        Set<String> emptyPrefixes = Collections.emptySet();
        TestOptionImpl opt = createBasicOption(1, false);
        opt.checkPrefixes(emptyPrefixes); // should return immediately
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesWithValidPrefix() {
        Set<String> prefixes = new HashSet<>(Arrays.asList("--", "-"));
        TestOptionImpl opt = createBasicOption(1, false);
        opt.checkPrefixes(prefixes); // should not throw
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesWithValidPrefixForTriggerOnly() {
        Set<String> prefixes = new HashSet<>(Arrays.asList("--"));
        // preferredName starts with "--", triggers also start with "--"
        TestOptionImpl opt = new TestOptionImpl(1, false, "--foo", "desc", prefixes,
                new HashSet<>(Arrays.asList("--bar")));
        opt.checkPrefixes(prefixes);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCheckPrefixesThrowsForPreferredNameWithoutPrefix() {
        Set<String> prefixes = new HashSet<>(Arrays.asList("--"));
        TestOptionImpl opt = new TestOptionImpl(1, false, "foo", "desc", prefixes,
                new HashSet<>(Arrays.asList("--bar")));
        opt.checkPrefixes(prefixes);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsWithDifferentRequired() {
        // required is not part of equals, so these should be equal
        TestOptionImpl opt1 = new TestOptionImpl(1, true, "foo", "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        assertTrue(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testHashCodeWithDifferentRequired() {
        TestOptionImpl opt1 = new TestOptionImpl(1, true, "foo", "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        TestOptionImpl opt2 = new TestOptionImpl(1, false, "foo", "desc",
                new HashSet<>(Arrays.asList("--")),
                new HashSet<>(Arrays.asList("--x")));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testFindOptionWithNullTrigger() {
        TestOptionImpl opt = createBasicOption(1, false);
        assertNull(opt.findOption(null));
    }

    @Test(timeout = 4000)
    public void testCanProcessWithNullArgument() {
        TestOptionImpl opt = createBasicOption(1, false);
        WriteableCommandLine wcl = null;
        ListIterator<String> args = Arrays.asList((String) null).listIterator();
        // canProcess will call canProcess(wcl, null) which will check triggers.contains(null)
        // triggers set does not contain null, so should return false
        assertFalse(opt.canProcess(wcl, args));
    }
}