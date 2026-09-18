package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.PosixParser
 * 
 * Decision Branches Covered:
 * 1. flatten() entry & iterator:
 *    - Empty arguments array -> while loop bypassed.
 *    - Successive invocations -> verifies init() resets eatTheRest to false and clears tokens.
 * 2. Long Option Handling (token.startsWith("--")):
 *    - token with '=' where option exists (options.hasOption(opt) == true) -> adds opt and value separately.
 *    - token with '=' where option does not exist -> calls processNonOptionToken(), sets eatTheRest, gobbles rest.
 *    - token without '=' where option exists -> adds opt only.
 *    - token without '=' where option does not exist -> calls processNonOptionToken(), adds "--", sets eatTheRest.
 *    - token exactly "--" -> treated as unknown long option, processNonOptionToken adds "--", eatTheRest triggers.
 * 3. Single Hyphen ("-".equals(token)):
 *    - adds "-" directly without modifying eatTheRest.
 * 4. Short / Single-Hyphen Option Handling (token.startsWith("-")):
 *    - token.length() == 2, option exists in options -> processOptionToken adds token.
 *    - token.length() == 2, option does not exist, stopAtNonOption == true -> processOptionToken sets eatTheRest, gobbles.
 *    - token.length() == 2, option does not exist, stopAtNonOption == false -> processOptionToken adds token without eatTheRest.
 *    - token.length() > 2, option recognized as complete option id (e.g., -help) -> processOptionToken called directly.
 *    - token.length() > 2, requires bursting:
 *      * Option exists, hasArg == false -> continues bursting next characters.
 *      * Option exists, hasArg == true, remaining characters present -> bursts option and consumes remainder as argument.
 *      * Option exists, hasArg == true, no remaining characters (token.length() == i + 1) -> does not burst remainder.
 *      * Option does not exist, stopAtNonOption == true -> processNonOptionToken called with substring, eatTheRest triggers.
 *      * Option does not exist, stopAtNonOption == false -> entire token added, bursts halts.
 * 5. Non-Option Tokens (!token.startsWith("-")):
 *    - stopAtNonOption == true -> processNonOptionToken adds "--", adds token, eatTheRest triggers gobble.
 *    - stopAtNonOption == false -> adds token normally, continues processing next tokens.
 * 6. gobble(iter):
 *    - eatTheRest == true with remaining tokens -> all remaining items added directly to tokens.
 *    - eatTheRest == true with no remaining tokens -> no-op.
 *    - eatTheRest == false -> no-op.
 *
 * Known Defects4J Defect Targeted (Partition C):
 * - ApplicationTest::testGroovy / PosixParserTest::testStopAtExpectedArg:
 *   When stopAtNonOption is true, PosixParser fails to recognize that an option expects an argument,
 *   prematurely treating the argument value as a non-option token and prepending "--".
 */
public class PosixParserGeminiTest
{
    private PosixParser parser;
    private Options standardOptions;

    @Before
    public void setUp()
    {
        parser = new PosixParser();
        standardOptions = new Options();
        standardOptions.addOption("a", false, "option a without arg");
        standardOptions.addOption("b", true, "option b with arg");
        standardOptions.addOption("c", false, "option c without arg");
        standardOptions.addOption("d", false, "option d without arg");
        standardOptions.addOption("f", "file", true, "file option with arg");
        standardOptions.addOption("h", "help", false, "help option without arg");
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenSingleShortOptionsStandard() throws Exception
    {
        String[] args = new String[]{"-a", "-c", "-d"};
        String[] expected = new String[]{"-a", "-c", "-d"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphenToken() throws Exception
    {
        String[] args = new String[]{"-a", "-", "-c"};
        String[] expected = new String[]{"-a", "-", "-c"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithoutEquals() throws Exception
    {
        String[] args = new String[]{"--help", "--file", "test.txt"};
        String[] expected = new String[]{"--help", "--file", "test.txt"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEquals() throws Exception
    {
        String[] args = new String[]{"--file=output.log"};
        String[] expected = new String[]{"--file", "output.log"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testFlattenRecognizedLongOptionWithSingleHyphen() throws Exception
    {
        Options options = new Options();
        options.addOption("help", false, "help option specified with long name");

        String[] args = new String[]{"-help"};
        String[] expected = new String[]{"-help"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testBurstMultipleNonArgOptions() throws Exception
    {
        String[] args = new String[]{"-acd"};
        String[] expected = new String[]{"-a", "-c", "-d"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testBurstOptionWithAttachedArgument() throws Exception
    {
        String[] args = new String[]{"-abmyValue"};
        String[] expected = new String[]{"-a", "-b", "myValue"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testBurstOptionWithArgAtEndBoundary() throws Exception
    {
        String[] args = new String[]{"-ab"};
        String[] expected = new String[]{"-a", "-b"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testBurstUnrecognizedOptionStopAtNonOptionFalse() throws Exception
    {
        // 'a' is known, 'z' is unknown. When stopAtNonOption is false, it bursts 'a' then appends "-az"
        String[] args = new String[]{"-az"};
        String[] expected = new String[]{"-a", "-az"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testBurstUnrecognizedOptionStopAtNonOptionTrue() throws Exception
    {
        // 'a' is known, 'z' is unknown. When stopAtNonOption is true, processNonOptionToken is called for "z"
        String[] args = new String[]{"-az", "extra1", "extra2"};
        String[] expected = new String[]{"-a", "--", "z", "extra1", "extra2"};

        String[] result = parser.flatten(standardOptions, args, true);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWithoutStopAtNonOption() throws Exception
    {
        String[] args = new String[]{"nonOption1", "-a", "nonOption2"};
        String[] expected = new String[]{"nonOption1", "-a", "nonOption2"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWithStopAtNonOption() throws Exception
    {
        String[] args = new String[]{"nonOption1", "-a", "nonOption2"};
        String[] expected = new String[]{"--", "nonOption1", "-a", "nonOption2"};

        String[] result = parser.flatten(standardOptions, args, true);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnknownLongOptionWithStopAtNonOption() throws Exception
    {
        String[] args = new String[]{"--unknownOpt=value", "remaining"};
        String[] expected = new String[]{"--", "--unknownOpt=value", "remaining"};

        String[] result = parser.flatten(standardOptions, args, true);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenOnlyToken() throws Exception
    {
        String[] args = new String[]{"--", "afterDoubleHyphen"};
        String[] expected = new String[]{"--", "--", "afterDoubleHyphen"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnknownShortOptionStopAtNonOptionTrue() throws Exception
    {
        String[] args = new String[]{"-z", "follow1", "follow2"};
        String[] expected = new String[]{"-z", "follow1", "follow2"};

        String[] result = parser.flatten(standardOptions, args, true);

        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testUnknownShortOptionStopAtNonOptionFalse() throws Exception
    {
        String[] args = new String[]{"-z", "-a"};
        String[] expected = new String[]{"-z", "-a"};

        String[] result = parser.flatten(standardOptions, args, false);

        assertArrayEquals(expected, result);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenEmptyArgumentsArray() throws Exception
    {
        String[] args = new String[0];
        String[] result = parser.flatten(standardOptions, args, false);

        assertNotNull("Flattened result must not be null", result);
        assertEquals("Flattened result must be empty", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenInstanceReusabilityAndReset() throws Exception
    {
        // First execution triggers eatTheRest
        String[] args1 = new String[]{"unknown", "rem1", "rem2"};
        String[] res1 = parser.flatten(standardOptions, args1, true);
        assertEquals(4, res1.length);
        assertEquals("--", res1[0]);

        // Second execution must start with fresh state (eatTheRest = false, tokens empty)
        String[] args2 = new String[]{"-a"};
        String[] res2 = parser.flatten(standardOptions, args2, false);
        assertArrayEquals(new String[]{"-a"}, res2);
    }

    @Test(timeout = 4000)
    public void testEatTheRestWithNoRemainingTokens() throws Exception
    {
        String[] args = new String[]{"unknownOnly"};
        String[] expected = new String[]{"--", "unknownOnly"};

        String[] result = parser.flatten(standardOptions, args, true);

        assertArrayEquals(expected, result);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectStopAtExpectedArg() throws Exception
    {
        // Ground truth defect: PosixParserTest::testStopAtExpectedArg
        // In the presence of stopAtNonOption == true, an option with an argument
        // must not have its argument mistaken for a non-option token ("--").
        Options options = new Options();
        options.addOption("b", true, "option b with arg");

        String[] args = new String[]{"-b", "foo"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertEquals("Confirm -b value is expected argument foo", "foo", cl.getOptionValue("b"));
        assertEquals("Confirm NO extra args remain", 0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testDefectGroovyCommandLinePattern() throws Exception
    {
        // Ground truth defect: ApplicationTest::testGroovy
        // Executing groovy -e "println 'hello'" with stopAtNonOption == true
        Options options = new Options();
        options.addOption("e", true, "specify script to execute");

        String[] args = new String[]{"-e", "println 'hello'"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue("Confirm -e is recognized", cl.hasOption("e"));
        assertEquals("Confirm -e holds the script value rather than '--'", "println 'hello'", cl.getOptionValue("e"));
        assertEquals("Args list must not contain the script as unparsed", 0, cl.getArgs().length);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFlattenWithNullArgumentsThrowsNPE() throws Exception
    {
        parser.flatten(standardOptions, null, false);
    }

    @Test(timeout = 4000)
    public void testParseUnrecognizedOptionThrowsUnrecognizedOptionException()
    {
        String[] args = new String[]{"-unknownOption"};
        try
        {
            parser.parse(standardOptions, args, false);
            fail("Expected UnrecognizedOptionException was not thrown");
        }
        catch (UnrecognizedOptionException expected)
        {
            assertNotNull("Exception message should be populated", expected.getMessage());
        }
        catch (ParseException unexpected)
        {
            fail("Unexpected ParseException subtype: " + unexpected.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testParseMissingArgumentThrowsMissingArgumentException()
    {
        String[] args = new String[]{"-b"}; // -b requires an argument
        try
        {
            parser.parse(standardOptions, args, false);
            fail("Expected MissingArgumentException was not thrown");
        }
        catch (MissingArgumentException expected)
        {
            assertEquals("Missing argument for option: b", "b", expected.getOption().getOpt());
        }
        catch (ParseException unexpected)
        {
            fail("Unexpected ParseException subtype: " + unexpected.getClass().getName());
        }
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPosixParserInstantiationAndTypeIntegrity()
    {
        PosixParser localParser = new PosixParser();
        assertNotNull("PosixParser instance must be created", localParser);
        assertTrue("PosixParser must be an instance of Parser", localParser instanceof Parser);
    }

    @Test(timeout = 4000)
    public void testFullParseLifecycleWithProperties() throws Exception
    {
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("a", "true");

        String[] args = new String[]{"-b", "file.dat"};
        CommandLine cl = parser.parse(standardOptions, args, properties);

        assertTrue("Option -a should be set via properties", cl.hasOption("a"));
        assertTrue("Option -b should be set via command-line args", cl.hasOption("b"));
        assertEquals("file.dat", cl.getOptionValue("b"));
    }
}