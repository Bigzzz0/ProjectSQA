package org.apache.commons.cli2.option;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * canProcess:
 *   - null argument
 *   - exact trigger match in optionMap
 *   - reverse tailMap prefix bursting
 *   - looksLikeOption short-circuit
 *   - anonymous argument acceptance
 *
 * process:
 *   - exact option found
 *   - tailMap member option found
 *   - tailMap member option not found
 *   - anonymous argument processed
 *   - anonymous argument cannot process
 *   - no anonymous argument and non-option token
 *   - empty argument list
 *
 * validate:
 *   - required option present / absent
 *   - maximum exceeded
 *   - minimum not met
 *   - nested group recursive validation
 *   - anonymous argument validation
 *
 * appendUsage / helpLines:
 *   - named, expanded, optional, outer, argument, comparator-sorted
 *
 * Defect target (CLI-123):
 *   GroupImpl.validate() only counts a nested group as present when
 *   commandLine.hasOption(group) is true.  If a child option is processed,
 *   the parent/root group is not marked present, so an outer minimum throws
 *   "Missing option parentOptions".  The dedicated test below asserts that a
 *   selected child option satisfies the outer group minimum.
 */
public class GroupImplDeepseekTest {

    @Test(timeout = 4000)
    public void testConstructorSeparatesArgumentsAndOptions() {
        final Option help = createOption("--help", Collections.singleton("-h"), Collections.singleton("-"));
        final Argument target = createArgument("target");
        final List<Option> input = new ArrayList<Option>();
        input.add(help);
        input.add(target);

        final GroupImpl group = new GroupImpl(input, "g", "desc", 0, 2);

        assertEquals(Arrays.asList(help), group.getOptions());
        assertEquals(Arrays.asList(target), group.getAnonymous());
        assertEquals("g", group.getPreferredName());
        assertEquals("desc", group.getDescription());
        assertEquals(0, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertFalse(group.isRequired());
    }

    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiable() {
        final Option opt = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(opt), "g", null, 0, 1);

        try {
            group.getOptions().add(createOption("-b", Collections.singleton("-b"), Collections.singleton("-")));
            fail("getOptions() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void testGetAnonymousUnmodifiable() {
        final Argument arg = createArgument("target");
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        try {
            group.getAnonymous().add(createArgument("other"));
            fail("getAnonymous() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void testCanProcessNullArgReturnsFalse() {
        final GroupImpl group = new GroupImpl(new ArrayList<Option>(), "g", null, 0, 0);
        assertFalse(group.canProcess(createCommandLine(false), null));
    }

    @Test(timeout = 4000)
    public void testCanProcessExactMatch() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertTrue(group.canProcess(createCommandLine(false), "-a"));
    }

    @Test(timeout = 4000)
    public void testCanProcessViaTailMapBurst() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertTrue(group.canProcess(createCommandLine(false), "-abc"));
    }

    @Test(timeout = 4000)
    public void testCanProcessReturnsFalseForLooksLikeOptionWhenNoOptionCanProcess() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, false);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertFalse(group.canProcess(createCommandLine(true), "-abc"));
    }

    @Test(timeout = 4000)
    public void testCanProcessReturnsFalseForLooksLikeOptionEvenWithAnonymous() {
        final Argument arg = createArgument("target");
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        assertFalse(group.canProcess(createCommandLine(true), "-x"));
    }

    @Test(timeout = 4000)
    public void testCanProcessAcceptsAnonymousArgument() {
        final Argument arg = createArgument("target");
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        assertTrue(group.canProcess(createCommandLine(false), "file.txt"));
    }

    @Test(timeout = 4000)
    public void testCanProcessReturnsFalseForUnknownNonOption() {
        final GroupImpl group = new GroupImpl(new ArrayList<Option>(), "g", null, 0, 0);

        assertFalse(group.canProcess(createCommandLine(false), "file.txt"));
    }

    @Test(timeout = 4000)
    public void testProcessEmptyListDoesNothing() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        group.process(createCommandLine(false), new ArrayList<String>().listIterator());

        assertFalse(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessExactOptionCallsOptionProcess() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("-a", "extra"));
        group.process(createCommandLine(false), arguments.listIterator());

        assertTrue(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessTailMapMemberOptionCallsOptionProcess() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("-abc"));
        group.process(createCommandLine(true), arguments.listIterator());

        assertTrue(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessUnknownLooksLikeOptionWithNoMemberOptionReturns() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, false, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("-abc"));
        group.process(createCommandLine(true), arguments.listIterator());

        assertFalse(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessNonOptionAnonymousArgument() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Argument arg = createArgument("target", true, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("file.txt"));
        group.process(createCommandLine(false), arguments.listIterator());

        assertTrue(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessNonOptionAnonymousCannotProcess() {
        final AtomicBoolean processed = new AtomicBoolean(false);
        final Argument arg = createArgument("target", false, false, setter(processed), null);
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("file.txt"));
        group.process(createCommandLine(false), arguments.listIterator());

        assertFalse(processed.get());
    }

    @Test(timeout = 4000)
    public void testProcessNonOptionNoAnonymousStops() {
        final GroupImpl group = new GroupImpl(new ArrayList<Option>(), "g", null, 0, 0);

        final List<String> arguments = new ArrayList<String>(Arrays.asList("file.txt"));
        group.process(createCommandLine(false), arguments.listIterator());
    }

    @Test(timeout = 4000)
    public void testValidateAcceptsPresentOptionForMinimum() throws Exception {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 1, 1);

        group.validate(createCommandLine(false, a));
    }

    @Test(timeout = 4000)
    public void testValidateAcceptsAbsentOptionalOption() throws Exception {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        group.validate(createCommandLine(false));
    }

    @Test(timeout = 4000)
    public void testValidateThrowsMissingOptionWhenBelowMinimum() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                true, true);
        final GroupImpl group = new GroupImpl(list(a), "g", null, 1, 1);

        try {
            group.validate(createCommandLine(false));
            fail("Expected OptionException for missing option");
        } catch (OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateThrowsUnexpectedTokenWhenAboveMaximum() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"),
                false, true);
        final Option b = createOption("-b", Collections.singleton("-b"), Collections.singleton("-"),
                false, true);
        final GroupImpl group = new GroupImpl(list(a, b), "g", null, 0, 1);

        try {
            group.validate(createCommandLine(false, a, b));
            fail("Expected OptionException for unexpected token");
        } catch (OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateDelegatesToAnonymousArguments() throws Exception {
        final AtomicBoolean validated = new AtomicBoolean(false);
        final Argument arg = createArgument("target", true, false, null, setter(validated));
        final GroupImpl group = new GroupImpl(list(arg), "g", null, 0, 1);

        group.validate(createCommandLine(false));

        assertTrue(validated.get());
    }

    @Test(timeout = 4000)
    public void testNestedGroupValidatesMaximumForMultipleChildOptions() {
        final Option child1 = createOption("-c1", Collections.singleton("-c1"), Collections.singleton("-"),
                false, true);
        final Option child2 = createOption("-c2", Collections.singleton("-c2"), Collections.singleton("-"),
                false, true);
        final GroupImpl parent = new GroupImpl(list(child1, child2), "parentOptions", null, 0, 1);
        final GroupImpl root = new GroupImpl(list(parent), "root", null, 0, Integer.MAX_VALUE);

        try {
            root.validate(createCommandLine(false, child1, child2));
            fail("Expected OptionException for parent maximum exceeded");
        } catch (OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNestedChildOptionSatisfiesOuterMinimum() {
        final Option child = createOption("-child", Collections.singleton("-child"), Collections.singleton("-"),
                false, true);
        final GroupImpl parent = new GroupImpl(list(child), "parentOptions", "parent", 1, 1);
        final GroupImpl root = new GroupImpl(list(parent), "root", null, 1, 1);

        try {
            root.validate(createCommandLine(false, child));
        } catch (OptionException e) {
            fail("A processed child option should satisfy the outer group minimum, but got: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAppendUsageNamedExpanded() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final Option b = createOption("B", Collections.singleton("-b"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a, b), "g", null, 0, 2);

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("g (A|B)", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testAppendUsageNameOnlyWhenNotExpanded() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("g", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testAppendUsageOptionalOuter() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), null, null, 0, 1);

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("[A]", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testAppendUsageOptionalNoOuterWithAnonymous() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final Argument target = createArgument("target");
        final GroupImpl group = new GroupImpl(list(a, target), null, null, 0, 2);

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("[A target]", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithComparator() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final Option b = createOption("B", Collections.singleton("-b"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a, b), null, null, 0, 2);

        final Comparator<Option> reverse = new Comparator<Option>() {
            public int compare(final Option o1, final Option o2) {
                return o2.getPreferredName().compareTo(o1.getPreferredName());
            }
        };

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, reverse);

        assertEquals("B|A", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithGroupName() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        final Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        assertEquals(1, group.helpLines(0, settings, null).size());
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithoutSettings() {
        final Option a = createOption("A", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertEquals(0, group.helpLines(0, Collections.<DisplaySetting>emptySet(), null).size());
    }

    @Test(timeout = 4000)
    public void testDefaultsDelegatesWithoutError() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"));
        final Argument target = createArgument("target");
        final GroupImpl group = new GroupImpl(list(a, target), "g", null, 0, 2);

        group.defaults(createCommandLine(false));
    }

    @Test(timeout = 4000)
    public void testGetPrefixesAndTriggers() {
        final Option a = createOption("-a", Collections.singleton("-a"), new HashSet<String>(Arrays.asList("-", "--")));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertTrue(group.getPrefixes().contains("-"));
        assertTrue(group.getPrefixes().contains("--"));
        assertTrue(group.getTriggers().contains("-a"));
    }

    @Test(timeout = 4000)
    public void testIsRequired() {
        final GroupImpl optional = new GroupImpl(new ArrayList<Option>(), "g", null, 0, 0);
        final GroupImpl required = new GroupImpl(new ArrayList<Option>(), "g", null, 1, 1);

        assertFalse(optional.isRequired());
        assertTrue(required.isRequired());
    }

    @Test(timeout = 4000)
    public void testFindOptionReturnsNullWhenNoChildMatches() {
        final Option a = createOption("-a", Collections.singleton("-a"), Collections.singleton("-"));
        final GroupImpl group = new GroupImpl(list(a), "g", null, 0, 1);

        assertNull(group.findOption("not-present"));
    }

    /*
     * Helpers
     */

    private static List<Option> list(final Option... options) {
        return new ArrayList<Option>(Arrays.asList(options));
    }

    private static Set<String> set(final String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private static Runnable setter(final AtomicBoolean flag) {
        return new Runnable() {
            public void run() {
                flag.set(true);
            }
        };
    }

    private static Option createOption(final String name,
                                       final Set<String> triggers,
                                       final Set<String> prefixes) {
        return createOption(name, triggers, prefixes, false, true, false, null, null);
    }

    private static Option createOption(final String name,
                                       final Set<String> triggers,
                                       final Set<String> prefixes,
                                       final boolean required,
                                       final boolean canProcess) {
        return createOption(name, triggers, prefixes, required, canProcess, false, null, null);
    }

    private static Option createOption(final String name,
                                       final Set<String> triggers,
                                       final Set<String> prefixes,
                                       final boolean required,
                                       final boolean canProcess,
                                       final boolean addSelfOnProcess,
                                       final Runnable onProcess,
                                       final Runnable onValidate) {
        final InvocationHandler handler = new InvocationHandler() {
            public Object invoke(final Object proxy, final Method method, final Object[] args) {
                final String m = method.getName();

                if ("getPreferredName".equals(m)) {
                    return name;
                }
                if ("getDescription".equals(m)) {
                    return "description of " + name;
                }
                if ("getTriggers".equals(m)) {
                    return triggers;
                }
                if ("getPrefixes".equals(m)) {
                    return prefixes;
                }
                if ("isRequired".equals(m)) {
                    return required;
                }
                if ("getMinimum".equals(m)) {
                    return required ? 1 : 0;
                }
                if ("getMaximum".equals(m)) {
                    return 1;
                }
                if ("canProcess".equals(m)) {
                    return canProcess;
                }
                if ("process".equals(m)) {
                    if (onProcess != null) {
                        onProcess.run();
                    }
                    if (addSelfOnProcess) {
                        ((WriteableCommandLine) args[0]).addOption((Option) proxy);
                    }
                    return null;
                }
                if ("validate".equals(m)) {
                    if (onValidate != null) {
                        onValidate.run();
                    }
                    return null;
                }
                if ("defaults".equals(m)) {
                    return null;
                }
                if ("appendUsage".equals(m)) {
                    ((StringBuffer) args[0]).append(name);
                    return null;
                }
                if ("helpLines".equals(m)) {
                    return Collections.emptyList();
                }
                if ("findOption".equals(m)) {
                    return null;
                }
                if ("getId".equals(m)) {
                    return 0;
                }
                if ("toString".equals(m)) {
                    return name;
                }
                return defaultValue(method.getReturnType());
            }
        };

        return (Option) Proxy.newProxyInstance(
                GroupImplDeepseekTest.class.getClassLoader(),
                new Class<?>[]{Option.class},
                handler);
    }

    private static Argument createArgument(final String name) {
        return createArgument(name, true, false, null, null);
    }

    private static Argument createArgument(final String name,
                                           final boolean canProcessIterator,
                                           final boolean addSelfOnProcess,
                                           final Runnable onProcess,
                                           final Runnable onValidate) {
        final InvocationHandler handler = new InvocationHandler() {
            public Object invoke(final Object proxy, final Method method, final Object[] args) {
                final String m = method.getName();

                if ("getPreferredName".equals(m)) {
                    return name;
                }
                if ("getDescription".equals(m)) {
                    return "argument " + name;
                }
                if ("getTriggers".equals(m)) {
                    return Collections.emptySet();
                }
                if ("getPrefixes".equals(m)) {
                    return Collections.emptySet();
                }
                if ("isRequired".equals(m)) {
                    return false;
                }
                if ("getMinimum".equals(m)) {
                    return 0;
                }
                if ("getMaximum".equals(m)) {
                    return Integer.MAX_VALUE;
                }
                if ("canProcess".equals(m)) {
                    if (args != null && args.length == 2 && args[1] instanceof ListIterator) {
                        return canProcessIterator;
                    }
                    return false;
                }
                if ("process".equals(m)) {
                    if (onProcess != null) {
                        onProcess.run();
                    }
                    if (addSelfOnProcess) {
                        ((WriteableCommandLine) args[0]).addOption((Option) proxy);
                    }
                    return null;
                }
                if ("validate".equals(m)) {
                    if (onValidate != null) {
                        onValidate.run();
                    }
                    return null;
                }
                if ("defaults".equals(m)) {
                    return null;
                }
                if ("appendUsage".equals(m)) {
                    ((StringBuffer) args[0]).append(name);
                    return null;
                }
                if ("helpLines".equals(m)) {
                    return Collections.emptyList();
                }
                if ("findOption".equals(m)) {
                    return null;
                }
                if ("getId".equals(m)) {
                    return 0;
                }
                if ("getInitialSeparator".equals(m)) {
                    return method.getReturnType() == Character.TYPE ? '\0' : "";
                }
                if ("getSubsequentSeparator".equals(m)) {
                    return method.getReturnType() == Character.TYPE ? '\0' : "";
                }
                if ("isSubsequentSeparator".equals(m)) {
                    return false;
                }
                if ("toString".equals(m)) {
                    return name;
                }
                return defaultValue(method.getReturnType());
            }
        };

        return (Argument) Proxy.newProxyInstance(
                GroupImplDeepseekTest.class.getClassLoader(),
                new Class<?>[]{Argument.class},
                handler);
    }

    private static WriteableCommandLine createCommandLine(final boolean looksLikeOption,
                                                          final Option... present) {
        final Set<Option> presentSet = new HashSet<Option>(Arrays.asList(present));

        final InvocationHandler handler = new InvocationHandler() {
            public Object invoke(final Object proxy, final Method method, final Object[] args) {
                final String m = method.getName();

                if ("looksLikeOption".equals(m)) {
                    return looksLikeOption;
                }
                if ("hasOption".equals(m)) {
                    if (args != null && args.length == 1 && args[0] instanceof Option) {
                        return presentSet.contains((Option) args[0]);
                    }
                    return false;
                }
                if ("addOption".equals(m)) {
                    if (args != null && args.length == 1 && args[0] instanceof Option) {
                        presentSet.add((Option) args[0]);
                    }
                    return null;
                }
                if ("toString".equals(m)) {
                    return "MockCommandLine";
                }
                return defaultValue(method.getReturnType());
            }
        };

        return (WriteableCommandLine) Proxy.newProxyInstance(
                GroupImplDeepseekTest.class.getClassLoader(),
                new Class<?>[]{WriteableCommandLine.class},
                handler);
    }

    private static Object defaultValue(final Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        if (type == Integer.TYPE) {
            return 0;
        }
        if (type == Long.TYPE) {
            return 0L;
        }
        if (type == Short.TYPE) {
            return (short) 0;
        }
        if (type == Byte.TYPE) {
            return (byte) 0;
        }
        if (type == Character.TYPE) {
            return '\0';
        }
        if (type == Float.TYPE) {
            return 0.0f;
        }
        if (type == Double.TYPE) {
            return 0.0d;
        }
        return null;
    }
}