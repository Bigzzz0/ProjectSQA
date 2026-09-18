package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for ArgumentImpl.
 * Targets all branches, boundaries, and the known Defects4J defect
 * (BugLoopingOptionLookAlikeTest).
 */
public class ArgumentImplDeepseekTest {

    /* ================================================================
       Branch & Defect Analysis Matrix
       ================================================================
       Partition A: Core functional logic – processValues, validate, defaults
       Partition B: Boundary values – null/empty arguments, min=0, max=Integer.MAX_VALUE
       Partition C: Defect-targeted – look-alike option handling, consumeRemaining interaction
       Partition D: Exception paths – constructor (min>max, default counts), validate (missing/unexpected)
       Partition E: Object contract – stripBoundaryQuotes, isRequired, getMinimum/Maximum
       Known defect: processValues incorrectly breaks when a value looks like an option,
                     causing an OptionException with a wrong message format.
       ================================================================ */

    // --- Helper: stub WriteableCommandLine for deterministic testing ---
    private static class StubCommandLine implements WriteableCommandLine {
        private final List<String> values = new ArrayList<>();
        private final List<String> lookLikeOptions = new ArrayList<>();
        private boolean defaultLooksLikeOption = false;

        public void addLookLikeOption(String arg) {
            lookLikeOptions.add(arg);
        }

        public void setDefaultLooksLikeOption(boolean val) {
            defaultLooksLikeOption = val;
        }

        @Override
        public void addValue(Option option, Object value) {
            values.add((String) value);
        }

        @Override
        public boolean looksLikeOption(String arg) {
            if (lookLikeOptions.contains(arg)) return true;
            return defaultLooksLikeOption;
        }

        @Override
        public List getValues(Option option) {
            return values;
        }

        @Override
        public void setDefaultValues(Option option, List defaults) {
            // no-op
        }

        // All other WriteableCommandLine methods – minimal stubs
        @Override public void addOption(Option option) {}
        @Override public List getOptions() { return Collections.emptyList(); }
        @Override public Option getOption(String trigger) { return null; }
        @Override public List getUndefaultedValues(Option option) { return Collections.emptyList(); }
        @Override public boolean isDefaultOption(Option option) { return false; }
        @Override public boolean isOption(Option option) { return false; }
        @Override public boolean hasOption(Option option) { return false; }
        @Override public void removeOption(Option option) {}
        @Override public void clearValues(Option option) {}
        @Override public List getProperties(Option option) { return Collections.emptyList(); }
        @Override public void addProperty(Option option, String property, Object value) {}
    }

    private static final char NUL = '\0';
    private static final String DEFAULT_CONSUME_REMAINING = "--";

    // ========================================================================
    // Partition A: Core Functional Logic – processValues normal paths
    // ========================================================================
    @Test(timeout = 4000)
    public void testProcessValuesSimpleValue() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("file", null, 0, 10, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("test.txt"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(1, cl.getValues(arg).size());
        assertEquals("test.txt", cl.getValues(arg).get(0));
    }

    @Test(timeout = 4000)
    public void testProcessValuesMultipleValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("file", null, 0, 5, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(3, cl.getValues(arg).size());
    }

    @Test(timeout = 4000)
    public void testProcessValuesMaxLimit() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("n", null, 0, 2, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("1", "2", "3"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(2, cl.getValues(arg).size());
        assertTrue(it.hasNext()); // value "3" remains
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ========================================================================
    @Test(timeout = 4000)
    public void testProcessValuesMinimumZero() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", null, 0, Integer.MAX_VALUE, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> emptyList = new ArrayList<>();
        ListIterator it = emptyList.listIterator();
        arg.processValues(cl, it, arg);
        assertTrue(cl.getValues(arg).isEmpty());
    }

    @Test(timeout = 4000)
    public void testProcessValuesMaximumInfinite() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("x", null, 0, Integer.MAX_VALUE, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 1000; i++) many.add("v" + i);
        ListIterator it = many.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(1000, cl.getValues(arg).size());
    }

    @Test(timeout = 4000)
    public void testProcessValuesWithQuotedBoundaries() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("q", null, 0, 10, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("\"quoted\"", "plain"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(2, cl.getValues(arg).size());
        assertEquals("quoted", cl.getValues(arg).get(0)); // quotes stripped
        assertEquals("plain", cl.getValues(arg).get(1));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone – look-alike option handling
    // ========================================================================
    @Test(timeout = 4000)
    public void testProcessValuesDoesNotBreakOnLookAlikeOption() throws OptionException {
        // Simulate the known bug: a value that looks like an option should be added as a value,
        // not cause a premature break. The defective version breaks and leaves the value unprocessed.
        ArgumentImpl arg = new ArgumentImpl("file", null, 0, 10, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        cl.addLookLikeOption("-f"); // mark "-f" as looking like an option
        List<String> argsList = new ArrayList<>(Arrays.asList("-f", "testfile.txt"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        // On fixed version, "-f" is added as a value.
        // On defective version, the loop breaks before adding it.
        List values = cl.getValues(arg);
        assertFalse("The value that looks like an option should have been consumed",
                     values.isEmpty());
        assertEquals("-f", values.get(0));
        // Additionally, "testfile.txt" should still be in the iterator (or added later by caller)
        assertTrue(it.hasNext());
        assertEquals("testfile.txt", it.next());
    }

    @Test(timeout = 4000)
    public void testProcessValuesConsumeRemainingWithLookAlike() throws OptionException {
        // When consumeRemaining token is used, subsequent values must be consumed
        // even if they look like options.
        ArgumentImpl arg = new ArgumentImpl("rest", null, 0, 10, NUL, NUL, null, DEFAULT_CONSUME_REMAINING, null, 0);
        StubCommandLine cl = new StubCommandLine();
        cl.addLookLikeOption("-x"); // "-x" looks like an option
        List<String> argsList = new ArrayList<>(Arrays.asList("--", "-x", "data.txt"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        List values = cl.getValues(arg);
        // The "--" token itself is not added; only the following arguments are consumed.
        // Expected: "-x" and "data.txt" should both be added.
        assertEquals(2, values.size());
        assertEquals("-x", values.get(0));
        assertEquals("data.txt", values.get(1));
    }

    @Test(timeout = 4000)
    public void testProcessValuesConsumeRemainingExceedMaximum() throws OptionException {
        // When consumeRemaining token is present but maximum is smaller than remaining args,
        // only up to maximum should be consumed.
        ArgumentImpl arg = new ArgumentImpl("rest", null, 0, 2, NUL, NUL, null, DEFAULT_CONSUME_REMAINING, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("--", "a", "b", "c"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(2, cl.getValues(arg).size());
        assertTrue(it.hasNext()); // "c" remains
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorMinExceedsMax() {
        new ArgumentImpl("bad", null, 5, 2, NUL, NUL, null, null, null, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorTooFewDefaults() {
        new ArgumentImpl("bad", null, 3, 5, NUL, NUL, null, null, Arrays.asList("a", "b"), 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorTooManyDefaults() {
        new ArgumentImpl("bad", null, 1, 2, NUL, NUL, null, null, Arrays.asList("a", "b", "c"), 0);
    }

    @Test(timeout = 4000, expected = OptionException.class)
    public void testValidateTooFewValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("req", null, 2, 5, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        cl.addValue(arg, "onlyOne");
        arg.validate(cl, arg);
    }

    @Test(timeout = 4000, expected = OptionException.class)
    public void testValidateTooManyValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("limit", null, 0, 1, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        cl.addValue(arg, "first");
        cl.addValue(arg, "second");
        arg.validate(cl, arg);
    }

    @Test(timeout = 4000, expected = OptionException.class)
    public void testValidateValidatorThrowsException() throws OptionException {
        Validator failingValidator = new Validator() {
            @Override
            public void validate(List values) throws InvalidArgumentException {
                throw new InvalidArgumentException("validation error");
            }
        };
        ArgumentImpl arg = new ArgumentImpl("v", null, 1, 1, NUL, NUL, failingValidator, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        cl.addValue(arg, "value");
        arg.validate(cl, arg);
    }

    @Test(timeout = 4000)
    public void testProcessValuesSubsequentSplitExceedMax() {
        ArgumentImpl arg = new ArgumentImpl("split", null, 0, 2, NUL, ',', null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("a,b,c"));
        ListIterator it = argsList.listIterator();
        try {
            arg.processValues(cl, it, arg);
            fail("Expected OptionException for too many split values");
        } catch (OptionException e) {
            // Expected
        }
        // Only two values should have been added
        assertEquals(2, cl.getValues(arg).size());
    }

    @Test(timeout = 4000)
    public void testProcessValuesSubsequentSplitNormal() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("split", null, 0, 5, NUL, ',', null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("x,y,z"));
        ListIterator it = argsList.listIterator();
        arg.processValues(cl, it, arg);
        assertEquals(3, cl.getValues(arg).size());
        assertEquals("x", cl.getValues(arg).get(0));
        assertEquals("y", cl.getValues(arg).get(1));
        assertEquals("z", cl.getValues(arg).get(2));
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================
    @Test(timeout = 4000)
    public void testStripBoundaryQuotesHandlesNullUnquoted() {
        ArgumentImpl arg = new ArgumentImpl("s", null, 0, 1, NUL, NUL, null, null, null, 0);
        assertEquals("hello", arg.stripBoundaryQuotes("hello"));
        assertEquals("\"hello", arg.stripBoundaryQuotes("\"hello")); // only leading quote
        assertEquals("hello\"", arg.stripBoundaryQuotes("hello\"")); // only trailing quote
        assertEquals("", arg.stripBoundaryQuotes("\"\"")); // both quotes, empty inside
        assertEquals("test", arg.stripBoundaryQuotes("\"test\""));
    }

    @Test(timeout = 4000)
    public void testIsRequired() {
        ArgumentImpl optional = new ArgumentImpl("opt", null, 0, 5, NUL, NUL, null, null, null, 0);
        assertFalse(optional.isRequired());
        ArgumentImpl required = new ArgumentImpl("req", null, 1, 5, NUL, NUL, null, null, null, 0);
        assertTrue(required.isRequired());
    }

    @Test(timeout = 4000)
    public void testGetMinimumMaximum() {
        ArgumentImpl arg = new ArgumentImpl("m", null, 2, 10, NUL, NUL, null, null, null, 0);
        assertEquals(2, arg.getMinimum());
        assertEquals(10, arg.getMaximum());
    }

    @Test(timeout = 4000)
    public void testDefaults() {
        List<String> defaults = Arrays.asList("def1", "def2");
        ArgumentImpl arg = new ArgumentImpl("d", null, 2, 5, NUL, NUL, null, null, defaults, 0);
        StubCommandLine cl = new StubCommandLine();
        arg.defaults(cl);
        // defaultValues method sets default values on command line. Since our stub does not track them,
        // we just verify no exception.
    }

    @Test(timeout = 4000)
    public void testAppendUsageMinimal() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, NUL, NUL, null, null, null, 0);
        StringBuffer buf = new StringBuffer();
        Set settings = new HashSet();
        arg.appendUsage(buf, settings, null);
        assertTrue(buf.length() > 0);
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithOptions() {
        ArgumentImpl arg = new ArgumentImpl("f", null, 1, Integer.MAX_VALUE, NUL, NUL, null, null, null, 0);
        StringBuffer buf = new StringBuffer();
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        arg.appendUsage(buf, settings, null);
        String result = buf.toString();
        assertTrue(result.contains("<f1>"));
        assertTrue(result.contains(" ..."));
    }

    @Test(timeout = 4000)
    public void testHelpLines() {
        ArgumentImpl arg = new ArgumentImpl("h", "helpful", 0, 1, NUL, NUL, null, null, null, 0);
        List lines = arg.helpLines(0, Collections.emptySet(), null);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0) instanceof HelpLine);
    }

    @Test(timeout = 4000)
    public void testGetPreferredName() {
        ArgumentImpl arg = new ArgumentImpl("myname", null, 0, 1, NUL, NUL, null, null, null, 0);
        assertEquals("myname", arg.getPreferredName());
    }

    @Test(timeout = 4000)
    public void testGetConsumeRemaining() {
        ArgumentImpl arg = new ArgumentImpl("c", null, 0, 10, NUL, NUL, null, DEFAULT_CONSUME_REMAINING, null, 0);
        assertEquals(DEFAULT_CONSUME_REMAINING, arg.getConsumeRemaining());
    }

    @Test(timeout = 4000)
    public void testCanProcessAlwaysTrue() {
        ArgumentImpl arg = new ArgumentImpl("x", null, 0, 1, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        assertTrue(arg.canProcess(cl, "anything"));
    }

    @Test(timeout = 4000)
    public void testGetPrefixesEmpty() {
        ArgumentImpl arg = new ArgumentImpl("p", null, 0, 1, NUL, NUL, null, null, null, 0);
        assertTrue(arg.getPrefixes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetTriggersEmpty() {
        ArgumentImpl arg = new ArgumentImpl("t", null, 0, 1, NUL, NUL, null, null, null, 0);
        assertTrue(arg.getTriggers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetInitialSeparator() {
        ArgumentImpl arg = new ArgumentImpl("s", null, 0, 1, '=', NUL, null, null, null, 0);
        assertEquals('=', arg.getInitialSeparator());
    }

    @Test(timeout = 4000)
    public void testGetSubsequentSeparator() {
        ArgumentImpl arg = new ArgumentImpl("s", null, 0, 1, NUL, ',', null, null, null, 0);
        assertEquals(',', arg.getSubsequentSeparator());
    }

    @Test(timeout = 4000)
    public void testGetValidator() {
        Validator v = new Validator() {
            public void validate(List values) {}
        };
        ArgumentImpl arg = new ArgumentImpl("v", null, 0, 1, NUL, NUL, v, null, null, 0);
        assertSame(v, arg.getValidator());
    }

    @Test(timeout = 4000)
    public void testGetDefaultValues() {
        List<String> defs = Arrays.asList("d1", "d2");
        ArgumentImpl arg = new ArgumentImpl("d", null, 0, 5, NUL, NUL, null, null, defs, 0);
        assertSame(defs, arg.getDefaultValues());
    }

    @Test(timeout = 4000)
    public void testConstructorNullNameDefaultsToArg() {
        ArgumentImpl arg = new ArgumentImpl(null, null, 0, 1, NUL, NUL, null, null, null, 0);
        assertEquals("arg", arg.getPreferredName());
    }

    @Test(timeout = 4000)
    public void testGetDescription() {
        ArgumentImpl arg = new ArgumentImpl("name", "desc", 0, 1, NUL, NUL, null, null, null, 0);
        assertEquals("desc", arg.getDescription());
    }

    @Test(timeout = 4000)
    public void testProcessWithThisOption() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("proc", null, 0, 3, NUL, NUL, null, null, null, 0);
        StubCommandLine cl = new StubCommandLine();
        List<String> argsList = new ArrayList<>(Arrays.asList("val1"));
        ListIterator it = argsList.listIterator();
        arg.process(cl, it);
        assertEquals(1, cl.getValues(arg).size());
        assertEquals("val1", cl.getValues(arg).get(0));
    }
}