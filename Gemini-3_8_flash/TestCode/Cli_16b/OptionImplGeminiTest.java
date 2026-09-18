/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli2.option.OptionImpl
 * 
 * Branch Coverage Focus:
 * 1. canProcess(WriteableCommandLine, ListIterator):
 *    - arguments.hasNext() == true -> enters block, reads next, calls previous(), delegates to canProcess(cmd, String).
 *    - arguments.hasNext() == false -> returns false immediately without inspecting iterator.
 * 2. equals(Object):
 *    - thatObj not instance of OptionImpl (null, different class) -> false.
 *    - thatObj instance of OptionImpl:
 *      * id mismatch -> false
 *      * preferredName mismatch (null vs non-null, different strings) -> false
 *      * description mismatch (null vs non-null, different strings) -> false
 *      * prefixes mismatch -> false
 *      * triggers mismatch -> false
 *      * all match -> true (reflexive, symmetric).
 * 3. equals(Object left, Object right):
 *    - left == null && right == null -> true
 *    - left == null && right != null -> false
 *    - left != null && right == null -> false
 *    - left.equals(right) -> true / false
 * 4. hashCode():
 *    - preferredName == null vs != null
 *    - description == null vs != null
 *    - deterministic calculation with prefixes and triggers.
 * 5. findOption(String):
 *    - triggers.contains(trigger) == true -> returns this
 *    - triggers.contains(trigger) == false -> returns null
 * 6. checkPrefixes(Set):
 *    - prefixes.isEmpty() == true -> early exit
 *    - prefixes.isEmpty() == false:
 *      * preferredName starts with prefix -> passes
 *      * preferredName does NOT start with any prefix -> throws IllegalArgumentException
 *      * trigger starts with prefix -> passes
 *      * trigger does NOT start with any prefix -> throws IllegalArgumentException
 * 7. toString():
 *    - delegates to appendUsage(buffer, DisplaySetting.ALL, null), returns string buffer content.
 * 8. defaults(WriteableCommandLine):
 *    - no-op invocation without side effects.
 * 9. isRequired():
 *    - returns constructor flag (true / false).
 * 10. CLI Defect Context (BugCLI123 / Order / Equality in hierarchy):
 *    - Option equality and trigger identity preservation across option sets.
 */

package org.apache.commons.cli2.option;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;

public class OptionImplGeminiTest {

    /**
     * Concrete test implementation of OptionImpl for direct white-box verification.
     */
    private static class ConcreteOption extends OptionImpl {
        private String preferredName;
        private String description;
        private Set prefixes = new HashSet();
        private Set triggers = new HashSet();
        private boolean canProcessResult = true;

        public ConcreteOption(int id, boolean required) {
            super(id, required);
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPrefixes(Set prefixes) {
            this.prefixes = prefixes;
        }

        public void setTriggers(Set triggers) {
            this.triggers = triggers;
        }

        public void setCanProcessResult(boolean result) {
            this.canProcessResult = result;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public Set getTriggers() {
            return triggers;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return canProcessResult;
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            if (preferredName != null) {
                buffer.append(preferredName);
            } else {
                buffer.append("defaultUsage");
            }
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
            // No-op for testing
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
            // No-op for testing
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            return Collections.emptyList();
        }

        public void invokeCheckPrefixes(Set prefixes) {
            super.checkPrefixes(prefixes);
        }

        public int compareTo(Object o) {
            if (o instanceof Option) {
                Option other = (Option) o;
                return getPreferredName().compareTo(other.getPreferredName());
            }
            return 0;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetIdAndIsRequired() {
        ConcreteOption opt1 = new ConcreteOption(42, true);
        assertEquals(42, opt1.getId());
        assertTrue(opt1.isRequired());

        ConcreteOption opt2 = new ConcreteOption(-1, false);
        assertEquals(-1, opt2.getId());
        assertFalse(opt2.isRequired());
    }

    @Test(timeout = 4000)
    public void testToStringDelegatesToAppendUsage() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setPreferredName("--help");
        assertEquals("--help", opt.toString());

        ConcreteOption optNullName = new ConcreteOption(2, false);
        optNullName.setPreferredName(null);
        assertEquals("defaultUsage", optNullName.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultsNoOp() {
        ConcreteOption opt = new ConcreteOption(10, false);
        // Ensure defaults() executes safely with null or any command line
        opt.defaults(null);
    }

    @Test(timeout = 4000)
    public void testFindOptionMatchAndMiss() {
        ConcreteOption opt = new ConcreteOption(1, false);
        Set triggers = new HashSet();
        triggers.add("-h");
        triggers.add("--help");
        opt.setTriggers(triggers);

        Option found = opt.findOption("-h");
        assertSame("findOption should return 'this' when trigger is present", opt, found);

        Option foundLong = opt.findOption("--help");
        assertSame("findOption should return 'this' when trigger is present", opt, foundLong);

        Option notFound = opt.findOption("-v");
        assertNull("findOption should return null when trigger is absent", notFound);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & ListIterator Traversal
    // =========================================================================

    @Test(timeout = 4000)
    public void testCanProcessEmptyIteratorReturnsFalse() {
        ConcreteOption opt = new ConcreteOption(1, false);
        List args = new ArrayList();
        ListIterator it = args.listIterator();

        boolean result = opt.canProcess(null, it);
        assertFalse("canProcess on empty iterator must return false", result);
        assertFalse("Iterator must still have no elements", it.hasNext());
    }

    @Test(timeout = 4000)
    public void testCanProcessNonEmptyIteratorPreservesIteratorState() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setCanProcessResult(true);

        List args = new ArrayList();
        args.add("--test-arg");
        args.add("another-arg");
        ListIterator it = args.listIterator();

        int initialNextIndex = it.nextIndex();
        boolean result = opt.canProcess(null, it);

        assertTrue("canProcess should evaluate to true", result);
        assertEquals("Iterator position must be rolled back by previous() call",
                initialNextIndex, it.nextIndex());
        assertEquals("Next argument must still be the first argument",
                "--test-arg", it.next());
    }

    @Test(timeout = 4000)
    public void testCanProcessDelegatesCorrectlyWhenDelegateReturnsFalse() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setCanProcessResult(false);

        List args = new ArrayList();
        args.add("--unrecognized");
        ListIterator it = args.listIterator();

        boolean result = opt.canProcess(null, it);
        assertFalse("canProcess must return false if single argument check returns false", result);
        assertEquals("Iterator state must be restored", 0, it.nextIndex());
    }

    // =========================================================================
    // Partition C: Prefix Checking Logic & Defect Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckPrefixesEmptyPrefixSetAllowed() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setPreferredName("anything");
        Set triggers = new HashSet();
        triggers.add("whatever");
        opt.setTriggers(triggers);

        // An empty prefix set should return immediately without throwing
        opt.invokeCheckPrefixes(Collections.emptySet());
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesSuccessMatchingPreferredAndTriggers() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setPreferredName("--file");
        Set triggers = new HashSet();
        triggers.add("-f");
        triggers.add("--file");
        opt.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        // Both preferred name "--file" and triggers "-f", "--file" start with "-" or "--"
        opt.invokeCheckPrefixes(prefixes);
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesThrowsWhenPreferredNameLacksPrefix() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setPreferredName("invalidName");
        Set triggers = new HashSet();
        triggers.add("-i");
        opt.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("-");

        try {
            opt.invokeCheckPrefixes(prefixes);
            fail("Expected IllegalArgumentException when preferredName has no prefix");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should indicate trigger needs prefix",
                    e.getMessage() != null && e.getMessage().contains("invalidName"));
        }
    }

    @Test(timeout = 4000)
    public void testCheckPrefixesThrowsWhenTriggerLacksPrefix() {
        ConcreteOption opt = new ConcreteOption(1, false);
        opt.setPreferredName("-valid");
        Set triggers = new HashSet();
        triggers.add("-valid");
        triggers.add("missingPrefixTrigger");
        opt.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("-");

        try {
            opt.invokeCheckPrefixes(prefixes);
            fail("Expected IllegalArgumentException when a trigger has no prefix");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should mention the offending trigger",
                    e.getMessage() != null && e.getMessage().contains("missingPrefixTrigger"));
        }
    }

    // =========================================================================
    // Partition D: Object Contract Integrity (equals & hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        ConcreteOption opt1 = new ConcreteOption(1, true);
        opt1.setPreferredName("-a");
        opt1.setDescription("Option A");
        Set pref1 = new HashSet();
        pref1.add("-");
        opt1.setPrefixes(pref1);
        Set trig1 = new HashSet();
        trig1.add("-a");
        opt1.setTriggers(trig1);

        ConcreteOption opt2 = new ConcreteOption(1, true);
        opt2.setPreferredName("-a");
        opt2.setDescription("Option A");
        Set pref2 = new HashSet();
        pref2.add("-");
        opt2.setPrefixes(pref2);
        Set trig2 = new HashSet();
        trig2.add("-a");
        opt2.setTriggers(trig2);

        // Reflexive
        assertTrue(opt1.equals(opt1));
        // Symmetric
        assertTrue(opt1.equals(opt2));
        assertTrue(opt2.equals(opt1));
        // HashCode consistency
        assertEquals(opt1.hashCode(), opt2.hashCode());

        // Incompatible type / null
        assertFalse(opt1.equals(null));
        assertFalse(opt1.equals("Some String"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferences() {
        ConcreteOption base = new ConcreteOption(1, true);
        base.setPreferredName("-a");
        base.setDescription("Desc");
        base.setPrefixes(Collections.singleton("-"));
        base.setTriggers(Collections.singleton("-a"));

        // Difference 1: id
        ConcreteOption diffId = new ConcreteOption(2, true);
        diffId.setPreferredName("-a");
        diffId.setDescription("Desc");
        diffId.setPrefixes(Collections.singleton("-"));
        diffId.setTriggers(Collections.singleton("-a"));
        assertFalse(base.equals(diffId));

        // Difference 2: preferredName
        ConcreteOption diffName = new ConcreteOption(1, true);
        diffName.setPreferredName("-b");
        diffName.setDescription("Desc");
        diffName.setPrefixes(Collections.singleton("-"));
        diffName.setTriggers(Collections.singleton("-a"));
        assertFalse(base.equals(diffName));

        // Difference 3: description
        ConcreteOption diffDesc = new ConcreteOption(1, true);
        diffDesc.setPreferredName("-a");
        diffDesc.setDescription("Different Desc");
        diffDesc.setPrefixes(Collections.singleton("-"));
        diffDesc.setTriggers(Collections.singleton("-a"));
        assertFalse(base.equals(diffDesc));

        // Difference 4: prefixes
        ConcreteOption diffPrefixes = new ConcreteOption(1, true);
        diffPrefixes.setPreferredName("-a");
        diffPrefixes.setDescription("Desc");
        diffPrefixes.setPrefixes(Collections.singleton("--"));
        diffPrefixes.setTriggers(Collections.singleton("-a"));
        assertFalse(base.equals(diffPrefixes));

        // Difference 5: triggers
        ConcreteOption diffTriggers = new ConcreteOption(1, true);
        diffTriggers.setPreferredName("-a");
        diffTriggers.setDescription("Desc");
        diffTriggers.setPrefixes(Collections.singleton("-"));
        diffTriggers.setTriggers(Collections.singleton("-b"));
        assertFalse(base.equals(diffTriggers));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullFields() {
        ConcreteOption optNull1 = new ConcreteOption(10, false);
        optNull1.setPreferredName(null);
        optNull1.setDescription(null);
        optNull1.setPrefixes(Collections.emptySet());
        optNull1.setTriggers(Collections.emptySet());

        ConcreteOption optNull2 = new ConcreteOption(10, false);
        optNull2.setPreferredName(null);
        optNull2.setDescription(null);
        optNull2.setPrefixes(Collections.emptySet());
        optNull2.setTriggers(Collections.emptySet());

        assertTrue(optNull1.equals(optNull2));
        assertEquals(optNull1.hashCode(), optNull2.hashCode());

        // Left null, right not null
        ConcreteOption optNonNull = new ConcreteOption(10, false);
        optNonNull.setPreferredName("name");
        optNonNull.setDescription(null);
        optNonNull.setPrefixes(Collections.emptySet());
        optNonNull.setTriggers(Collections.emptySet());

        assertFalse(optNull1.equals(optNonNull));
        assertFalse(optNonNull.equals(optNull1));

        // Right description non-null
        ConcreteOption optNonNullDesc = new ConcreteOption(10, false);
        optNonNullDesc.setPreferredName(null);
        optNonNullDesc.setDescription("desc");
        optNonNullDesc.setPrefixes(Collections.emptySet());
        optNonNullDesc.setTriggers(Collections.emptySet());

        assertFalse(optNull1.equals(optNonNullDesc));
        assertFalse(optNonNullDesc.equals(optNull1));
    }

    @Test(timeout = 4000)