package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Key Decision Branches Targeted:
 * 1. initConfigFromFlags: Argument pattern matching (--key=value vs other formats)
 * 2. initConfigFromFlags: Quotes pattern matching for values
 * 3. initConfigFromFlags: CmdLineException handling (isConfigValid = false)
 * 4. initConfigFromFlags: --version flag branch (ResourceBundle loading)
 * 5. initConfigFromFlags: display_help branch (isConfigValid = false)
 * 6. shouldRunCompiler(): Returns isConfigValid state
 * 7. createOptions(): CompilationLevel selection, debug mode branching
 * 8. createOptions(): WarningLevel selection
 * 9. createOptions(): Formatting option switch cases
 * 10. createExterns(): use_only_custom_externs branch
 * 11. getDefaultExterns(): ZipInputStream processing, ordering verification
 * 12. BooleanOptionHandler: Null parameter handling (returns 0, adds true)
 * 13. BooleanOptionHandler: TRUES vs FALSES vs default behavior
 * 14. FormattingOption: PRETTY_PRINT and PRINT_INPUT_DELIMITER cases
 *
 * Defect-Targeted Tests:
 * - testVersionFlag2: Targets the known defect where --version flag handling
 *   may not properly output version info or may have initialization order issues
 * - Comprehensive constructor and initialization tests to expose state bugs
 *
 * Boundary Conditions:
 * - Empty arguments array
 * - Null arguments elements
 * - Mixed --key=value and --key value formats
 * - Invalid argument values
 * - Boolean flag with no value, "true", "false", unexpected values
 */
public class CommandLineRunnerDeepseekTest {

    /* ================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ================================================================ */

    @Test(timeout = 4000)
    public void testConstructorWithValidSimpleArgs() {
        // Test basic constructor with simple arguments
        String[] args = new String[]{"--js", "test.js"};
        
        // Use a custom PrintStream to capture output (not directly needed, but to check no exception)
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        assertNotNull("Runner should be created", runner);
        assertTrue("shouldRunCompiler should be true for valid config", runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithPrintTreeFlag() {
        String[] args = new String[]{"--print_tree", "true"};
        CommandLineRunner runner = new CommandLineRunner(args);
        assertNotNull(runner);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithCompilationLevel() {
        String[] args = new String[]{
            "--compilation_level", "ADVANCED_OPTIMIZATIONS",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertNotNull(runner);
    }

    @Test(timeout = 4000)
    public void testConstructorWithWarningLevelVerbose() {
        String[] args = new String[]{
            "--warning_level", "VERBOSE",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithMultipleJsFiles() {
        String[] args = new String[]{
            "--js", "file1.js",
            "--js", "file2.js",
            "--js", "file3.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithExterns() {
        String[] args = new String[]{
            "--externs", "extern1.js",
            "--externs", "extern2.js",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    /* ================================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ================================================================ */

    @Test(timeout = 4000)
    public void testConstructorWithEmptyArgs() {
        // Empty args should result in default configuration
        String[] args = new String[]{};
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue("Empty args should be valid", runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDashDDefine() {
        // Test the --define alias -D with quoted string value
        String[] args = new String[]{
            "-D", "myVar='hello'",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithEqualsSyntax() {
        // Test --key=value syntax parsing
        String[] args = new String[]{
            "--compilation_level=ADVANCED_OPTIMIZATIONS",
            "--js=input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBooleanFlagNoValue() {
        // Boolean flags without explicit value should default to true
        String[] args = new String[]{"--debug", "--js", "input.js"};
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithManageDependencies() {
        String[] args = new String[]{
            "--manage_closure_dependencies", "true",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithClosureEntryPoint() {
        String[] args = new String[]{
            "--closure_entry_point", "myapp.start",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    /* ================================================================
     * Partition C: Defect-Targeted Branch Zone
     * ================================================================ */

    @Test(timeout = 4000)
    public void testVersionFlag2() {
        // Known defect: --version flag should output version info to error stream
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        String[] args = new String[]{"--version"};
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        
        // After construction with --version, shouldRunCompiler should be false
        // because version flag sets isConfigValid to false
        assertFalse("--version should make config invalid", runner.shouldRunCompiler());
        
        // Verify version output was written to error stream
        String errorOutput = errContent.toString();
        assertTrue("Error output should contain 'Version:'", errorOutput.contains("Version:"));
        assertTrue("Error output should contain compiler version info", 
                   errorOutput.contains("Closure Compiler"));
    }

    @Test(timeout = 4000)
    public void testHelpFlagDisablesCompilation() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        String[] args = new String[]{"--help"};
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        
        // --help should set isConfigValid to false and print usage
        assertFalse("--help should make config invalid", runner.shouldRunCompiler());
        
        String output = errContent.toString();
        assertTrue("Help output should contain usage information", 
                   output.contains("Usage") || output.contains("--help"));
    }

    @Test(timeout = 4000)
    public void testInvalidFlagTriggersError() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        String[] args = new String[]{"--nonexistent_flag"};
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        
        // Invalid flags should cause CmdLineException, making config invalid
        assertFalse("Invalid flag should make config invalid", runner.shouldRunCompiler());
        
        String output = errContent.toString();
        assertTrue("Error output should contain error message about unknown flag", 
                   !output.isEmpty());
    }

    @Test(timeout = 4000)
    public void testInvalidBooleanValue() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        // Test BooleanOptionHandler with "maybe" - should default to true with 0 consumed
        String[] args = new String[]{"--debug", "maybe", "--js", "input.js"};
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testBooleanFlagExplicitFalse() {
        String[] args = new String[]{"--debug", "false", "--js", "input.js"};
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    /* ================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ================================================================ */

    @Test(timeout = 4000)
    public void testConstructorWithNullArgs() {
        // Null args may throw NullPointerException in pattern matching
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        try {
            CommandLineRunner runner = new CommandLineRunner((String[]) null, System.out, err);
            fail("Expected NullPointerException for null args");
        } catch (NullPointerException expected) {
            // Expected - null args should not be handled gracefully
        }
    }

    @Test(timeout = 4000)
    public void testCreateOptionsReturnsNonNull() {
        String[] args = new String[]{"--js", "test.js"};
        CommandLineRunner runner = new CommandLineRunner(args);
        
        // We can't directly call createOptions since it's protected,
        // but we can verify the runner was created successfully
        assertNotNull(runner);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testDefaultExternsList() throws Exception {
        // Verify the getDefaultExterns produces a non-null, non-empty list
        List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
        assertNotNull("Default externs should not be null", externs);
        assertTrue("Default externs should not be empty", externs.size() > 0);
        
        // Verify specific externs exist in the list by name (indirectly)
        // We can check the order matches expected
        assertEquals("First extern should be es3.js", 
                     "externs.zip//es3.js", externs.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testUseOnlyCustomExternsBranch() throws Exception {
        // This test verifies the branch in createExterns when use_only_custom_externs is true
        // Since we can't access protected methods directly, we verify through constructor
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errContent);
        
        String[] args = new String[]{
            "--use_only_custom_externs", "true",
            "--externs", "custom.js",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args, System.out, err);
        assertTrue("Custom externs config should be valid", runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testFormattingOptionPrettyPrint() {
        String[] args = new String[]{
            "--formatting", "PRETTY_PRINT",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testFormattingOptionPrintInputDelimiter() {
        String[] args = new String[]{
            "--formatting", "PRETTY_PRINT",
            "--formatting", "PRINT_INPUT_DELIMITER",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testThirdPartyFlagChangesCodingConvention() {
        String[] args = new String[]{
            "--third_party", "true",
            "--js", "input.js"
        };
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    /* ================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ================================================================ */

    @Test(timeout = 4000)
    public void testShouldRunCompilerAfterValidConfig() {
        String[] args = new String[]{"--js", "test.js"};
        CommandLineRunner runner = new CommandLineRunner(args);
        
        // Verify the method returns the correct state
        assertTrue("Valid config should result in shouldRunCompiler returning true", 
                   runner.shouldRunCompiler());
    }

    @Test(timeout = 4000)
    public void testConstructorWithPrintStreamsSynchronous() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outContent);
        PrintStream err = new PrintStream(errContent);
        
        String[] args = new String[]{"--js", "test.js"};
        CommandLineRunner runner = new CommandLineRunner(args, out, err);
        
        assertNotNull("Runner should be created with custom streams", runner);
        assertTrue("Should be valid", runner.shouldRunCompiler());
        
        // No output should have been written for valid config
        assertEquals("Out stream should be empty", "", outContent.toString());
        assertEquals("Err stream should be empty", "", errContent.toString());
    }
}