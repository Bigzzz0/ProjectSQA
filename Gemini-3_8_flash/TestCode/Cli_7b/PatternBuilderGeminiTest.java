package org.apache.commons.cli2.builder;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli2.builder.PatternBuilder
 *
 * 1. Constructor Branching:
 *    - PatternBuilder(): Default builders (GroupBuilder, DefaultOptionBuilder, ArgumentBuilder).
 *    - PatternBuilder(gbuilder, obuilder, abuilder): Explicit dependency injection.
 *
 * 2. create() Logic:
 *    - options.size() == 1: Direct single Option unwrapped return, verify reset() invocation.
 *    - options.size() == 0: Group created with 0 options.
 *    - options.size() > 1: GroupBuilder populated with all options, reset() invocation.
 *
 * 3. reset() Method:
 *    - Clears internal options Set, ensures fluent return of this.
 *
 * 4. withPattern(String) Branches & State Transitions:
 *    - Empty string & whitespace strings: No options created.
 *    - Single flag without arguments ('a').
 *    - Modifier '!' (required = true).
 *    - Type modifiers (@, :, %, +, #, <, >, *, /): Assigns argument type.
 *    - Default char transitions: Multiple option definitions, space separations.
 *    - Trailing option creation (after loop if opt != ' ').
 *
 * 5. createOption(char type, boolean required, char opt) Branches:
 *    - type == ' ': No argument attached.
 *    - type != ' ':
 *      - validator(type) applied.
 *      - required == true: argument minimum set to 1.
 *      - required == false: argument minimum default (0).
 *      - type == '*': maximum argument count unbounded (withMaximum(1) NOT called).
 *      - type != '*': withMaximum(1) called.
 *
 * 6. validator(char c) Switch Coverage:
 *    - '@': ClassValidator with instance=true.
 *    - '+': ClassValidator with instance=false.
 *    - ':': No validator (null).
 *    - '%': NumberValidator.
 *    - '#': DateValidator.
 *    - '<': FileValidator with existing=true and file=true.
 *    - '>': FileValidator default.
 *    - '*': FileValidator default.
 *    - '/': UrlValidator.
 *    - default: null.
 *
 * 7. Defect-Targeted Branch Zone (Defects4J Bug27575):
 *    - Pattern "c:!h": In defective implementation, encountering '!' before 'h'
 *      erroneously assigns required=true to option 'c' (with argument ':')
 *      when transitioning in default case, resetting required=false for 'h'.
 *      Ground truth: option 'c' must NOT be required, and option 'h' MUST be required.
 */

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.validation.ClassValidator;
import org.apache.commons.cli2.validation.DateValidator;
import org.apache.commons.cli2.validation.FileValidator;
import org.apache.commons.cli2.validation.NumberValidator;
import org.apache.commons.cli2.validation.UrlValidator;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class PatternBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndSingleOption() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("v");
        final Option option = builder.create();

        assertNotNull("Option should not be null", option);
        assertFalse("Single option should not be wrapped in a Group", option instanceof Group);
        assertEquals("-v", option.getPreferredName());
        assertFalse("Option should not be required", option.isRequired());
    }

    @Test(timeout = 4000)
    public void testCustomBuildersConstructor() {
        final GroupBuilder gb = new GroupBuilder();
        final DefaultOptionBuilder ob = new DefaultOptionBuilder();
        final ArgumentBuilder ab = new ArgumentBuilder();

        final PatternBuilder builder = new PatternBuilder(gb, ob, ab);
        builder.withPattern("k");
        final Option option = builder.create();

        assertNotNull("Option should not be null", option);
        assertEquals("-k", option.getPreferredName());
    }

    @Test(timeout = 4000)
    public void testMultipleOptionsCreatesGroup() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("ab");
        final Option option = builder.create();

        assertTrue("Multiple options must produce a Group", option instanceof Group);
        final Group group = (Group) option;
        final Option optA = group.findOption("-a");
        final Option optB = group.findOption("-b");

        assertNotNull("Option -a must exist in group", optA);
        assertNotNull("Option -b must exist in group", optB);
    }

    @Test(timeout = 4000)
    public void testResetClearsBuilderState() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("abc");
        final PatternBuilder fluent = builder.reset();
        assertSame("reset() must return this for fluent chaining", builder, fluent);

        final Option option = builder.create();
        assertTrue("Option after reset should be an empty Group", option instanceof Group);
        final Group group = (Group) option;
        assertEquals(0, group.getOptions().size());
    }

    @Test(timeout = 4000)
    public void testConsecutiveCreateInvocations() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("x");
        final Option first = builder.create();
        assertEquals("-x", first.getPreferredName());

        // create() automatically calls reset(), so second call creates an empty Group
        final Option second = builder.create();
        assertTrue("Second create() call should produce an empty Group", second instanceof Group);
        final Group group = (Group) second;
        assertEquals(0, group.getOptions().size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPatternProducesEmptyGroup() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("");
        final Option option = builder.create();

        assertTrue("Empty pattern must create a Group", option instanceof Group);
        final Group group = (Group) option;
        assertEquals(0, group.getOptions().size());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyPatternProducesEmptyGroup() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("   ");
        final Option option = builder.create();

        assertTrue("Whitespace-only pattern must produce empty Group", option instanceof Group);
        final Group group = (Group) option;
        assertEquals(0, group.getOptions().size());
    }

    @Test(timeout = 4000)
    public void testPatternWithSpacesBetweenOptions() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("a b c");
        final Option option = builder.create();

        assertTrue("Should produce a Group", option instanceof Group);
        final Group group = (Group) option;
        assertEquals(3, group.getOptions().size());
        assertNotNull(group.findOption("-a"));
        assertNotNull(group.findOption("-b"));
        assertNotNull(group.findOption("-c"));
    }

    @Test(timeout = 4000)
    public void testPatternWithLeadingAndTrailingSpaces() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("  x  ");
        final Option option = builder.create();

        assertFalse("Single option with whitespace padding should be DefaultOption", option instanceof Group);
        assertEquals("-x", option.getPreferredName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug27575)
    // =========================================================================

    /**
     * Targets Bug 27575 (Defects4J ground truth):
     * When pattern contains "c:!h", 'c' takes a string argument ':' and is NOT required,
     * whereas 'h' is a flag preceded by '!' and MUST be required.
     * The defective PatternBuilder incorrectly sets 'c' as required and 'h' as optional.
     */
    @Test(timeout = 4000)
    public void testBug27575RequiredOptions() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("c:!h");
        final Option option = builder.create();

        assertTrue("Pattern 'c:!h' must create a Group", option instanceof Group);
        final Group group = (Group) option;

        final Option optC = group.findOption("-c");
        final Option optH = group.findOption("-h");

        assertNotNull("Option -c must be created", optC);
        assertNotNull("Option -h must be created", optH);

        // Ground truth assertions revealing the defect:
        assertFalse("Option -c must NOT be required", optC.isRequired());
        assertTrue("Option -h MUST be required", optH.isRequired());
    }

    /**
     * Targets Bug 27575 through the Parser integration path:
     * When parsing an empty command line, missing required option -h must cause OptionException,
     * not option -c.
     */
    @Test(timeout = 4000)
    public void testBug27575RequiredOptionParsingFailure() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("c:!h");
        final Group group = (Group) builder.create();

        final Parser parser = new Parser();
        parser.setGroup(group);

        try {
            parser.parse(new String[]{});
            fail("Parsing empty arguments must fail because required option -h is missing");
        } catch (final OptionException exp) {
            assertEquals("Missing required option must be -h", "-h", exp.getOption().getPreferredName());
        }
    }

    // =========================================================================
    // Partition D: Type Modifiers & Validator Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringArgumentTypeColon() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("s:");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-s", "helloWorld"});
        assertTrue(cl.hasOption("-s"));
        assertEquals("helloWorld", cl.getValue("-s"));
    }

    @Test(timeout = 4000)
    public void testNumberValidatorPercent() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("n%");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-n", "12345"});
        assertTrue(cl.hasOption("-n"));
        assertEquals("12345", cl.getValue("-n"));

        try {
            parser.parse(new String[]{"-n", "notANumber"});
            fail("Expected OptionException for invalid number argument");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUrlValidatorSlash() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("u/");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-u", "http://commons.apache.org"});
        assertTrue(cl.hasOption("-u"));

        try {
            parser.parse(new String[]{"-u", "invalid-url-without-protocol"});
            fail("Expected OptionException for invalid URL");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testClassValidatorPlus() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("c+");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-c", "java.util.List"});
        assertTrue(cl.hasOption("-c"));

        try {
            parser.parse(new String[]{"-c", "com.nonexistent.FakeClass12345"});
            fail("Expected OptionException for non-existent class");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testClassInstanceValidatorAt() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("i@");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-i", "java.util.ArrayList"});
        assertTrue(cl.hasOption("-i"));

        // java.util.List is an interface and cannot be instantiated
        try {
            parser.parse(new String[]{"-i", "java.util.List"});
            fail("Expected OptionException because interface cannot be instantiated");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDateValidatorHash() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("d#");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        try {
            parser.parse(new String[]{"-d", "invalid-date-format"});
            fail("Expected OptionException for unparseable date");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testExistingFileValidatorLessThan() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("f<");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        try {
            parser.parse(new String[]{"-f", "non_existing_file_987654321.tmp"});
            fail("Expected OptionException for non-existent file");
        } catch (final OptionException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFileValidatorGreaterThan() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("f>");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        // File is not required to exist
        final CommandLine cl = parser.parse(new String[]{"-f", "any_file.txt"});
        assertTrue(cl.hasOption("-f"));
    }

    @Test(timeout = 4000)
    public void testUnlimitedArgumentsAsterisk() throws Exception {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("m*");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        final CommandLine cl = parser.parse(new String[]{"-m", "f1.txt", "f2.txt", "f3.txt"});
        assertTrue(cl.hasOption("-m"));
        final List values = cl.getValues("-m");
        assertEquals(3, values.size());
        assertEquals("f1.txt", values.get(0));
        assertEquals("f2.txt", values.get(1));
        assertEquals("f3.txt", values.get(2));
    }

    @Test(timeout = 4000)
    public void testRequiredFlagWithArgumentMinMaxConfiguration() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!r:");
        final Option option = builder.create();

        assertTrue(option instanceof DefaultOption);
        final DefaultOption defOpt = (DefaultOption) option;
        assertTrue(defOpt.isRequired());

        final Argument arg = defOpt.getArgument();
        assertNotNull(arg);
        assertEquals("Required option argument must have minimum 1", 1, arg.getMinimum());
        assertEquals("Non-asterisk type must have maximum 1", 1, arg.getMaximum());
    }

    @Test(timeout = 4000)
    public void testRequiredFlagWithAsteriskUnboundedMax() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!a*");
        final Option option = builder.create();

        assertTrue(option instanceof DefaultOption);
        final DefaultOption defOpt = (DefaultOption) option;
        assertTrue(defOpt.isRequired());

        final Argument arg = defOpt.getArgument();
        assertNotNull(arg);
        assertEquals("Required asterisk argument must have minimum 1", 1, arg.getMinimum());
        assertEquals("Asterisk type must have default Integer.MAX_VALUE maximum", Integer.MAX_VALUE, arg.getMaximum());
    }

    @Test(timeout = 4000)
    public void testSingleRequiredFlagWithoutArgument() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("!q");
        final Option option = builder.create();

        assertTrue(option instanceof DefaultOption);
        final DefaultOption defOpt = (DefaultOption) option;
        assertTrue(defOpt.isRequired());
        assertNull("Flag without argument type should have null argument", defOpt.getArgument());
    }

    @Test(timeout = 4000)
    public void testMultipleTypeModifiersOverwritesType() throws Exception {
        // Specifying multiple types in sequence updates type variable to the latest char
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern("x%:");
        final Option option = builder.create();

        final Parser parser = new Parser();
        final GroupBuilder gb = new GroupBuilder();
        gb.withOption(option);
        parser.setGroup(gb.create());

        // Since ':' was the last modifier, string is accepted and number validation is skipped
        final CommandLine cl = parser.parse(new String[]{"-x", "textString"});
        assertTrue(cl.hasOption("-x"));
        assertEquals("textString", cl.getValue("-x"));
    }

    // =========================================================================
    // Partition E: Internal Validator Helper White-Box Reflection
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidatorMethodExhaustiveReflection() throws Exception {
        final Method validatorMethod = PatternBuilder.class.getDeclaredMethod("validator", char.class);
        validatorMethod.setAccessible(true);

        final Validator vAt = (Validator) validatorMethod.invoke(null, '@');
        assertTrue(vAt instanceof ClassValidator);
        assertTrue(((ClassValidator) vAt).isInstance());

        final Validator vPlus = (Validator) validatorMethod.invoke(null, '+');
        assertTrue(vPlus instanceof ClassValidator);
        assertFalse(((ClassValidator) vPlus).isInstance());

        final Validator vColon = (Validator) validatorMethod.invoke(null, ':');
        assertNull("Colon should have no validator (null)", vColon);

        final Validator vNum = (Validator) validatorMethod.invoke(null, '%');
        assertTrue(vNum instanceof NumberValidator);

        final Validator vDate = (Validator) validatorMethod.invoke(null, '#');
        assertTrue(vDate instanceof DateValidator);

        final Validator vExistingFile = (Validator) validatorMethod.invoke(null, '<');
        assertTrue(vExistingFile instanceof FileValidator);
        assertTrue(((FileValidator) vExistingFile).isExisting());
        assertTrue(((FileValidator) vExistingFile).isFile());

        final Validator vFile = (Validator) validatorMethod.invoke(null, '>');
        assertTrue(vFile instanceof FileValidator);
        assertFalse(((FileValidator) vFile).isExisting());

        final Validator vFiles = (Validator) validatorMethod.invoke(null, '*');
        assertTrue(vFiles instanceof FileValidator);

        final Validator vUrl = (Validator) validatorMethod.invoke(null, '/');
        assertTrue(vUrl instanceof UrlValidator);

        final Validator vDefault = (Validator) validatorMethod.invoke(null, 'z');
        assertNull("Unknown char in validator switch must return null", vDefault);
    }

    // =========================================================================
    // Partition F: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testWithPatternNullThrowsNullPointerException() {
        final PatternBuilder builder = new PatternBuilder();
        builder.withPattern(null);
    }
}