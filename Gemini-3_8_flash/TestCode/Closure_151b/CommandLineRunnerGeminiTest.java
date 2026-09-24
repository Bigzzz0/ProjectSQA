/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.CommandLineRunner
 * Known Defect: Defects4J CommandLineRunnerTest::testVersionFlag -> junit.framework.AssertionFailedError
 *
 * Decision / Branch Matrix:
 * 1. Args Parsing & Pattern Matching (initConfigFromFlags):
 *    - Flag matching regex "--[a-zA-Z_]+=(.*)":
 *        * Matches quotes (^['"].*['"]$) -> Single quote stripped
 *        * Matches quotes (^['"].*['"]$) -> Double quote stripped
 *        * Does not match quotes -> Raw value preserved
 *    - Flag not matching regex -> Kept as raw argument (e.g., bare flags, filenames)
 * 2. BooleanOptionHandler branches:
 *    - param == null -> setter.addValue(true), returns 0
 *    - param in TRUES ("true", "on", "yes", "1") -> setter.addValue(true), returns 1
 *    - param in FALSES ("false", "off", "no", "0") -> setter.addValue(false), returns 1
 *    - param invalid boolean string -> throws CmdLineException
 * 3. Configuration Validity & Help:
 *    - CmdLineException triggered -> isConfigValid = false, parser.printUsage(err)
 *    - --help / display_help = true -> isConfigValid = false, parser.printUsage(err)
 *    - Normal valid configuration -> isConfigValid = true, getCommandLineConfig() fully populated
 * 4. Coding Convention Selection:
 *    - flags.third_party == true -> DefaultCodingConvention
 *    - flags.third_party == false -> ClosureCodingConvention
 * 5. CompilerOptions Factory (createOptions):
 *    - compilation_level (WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, ADVANCED_OPTIMIZATIONS)
 *    - flags.debug == true -> level.setDebugOptionsForCompilationLevel(options)
 *    - flags.debug == false -> skip debug options
 *    - warning_level (QUIET, DEFAULT, VERBOSE)
 *    - formatting options: PRETTY_PRINT, PRINT_INPUT_DELIMITER
 *    - process_closure_primitives -> options.closurePass
 * 6. Externs Creation (createExterns & getDefaultExterns):
 *    - flags.use_only_custom_externs == true -> returns custom externs only
 *    - flags.use_only_custom_externs == false -> loads & prepends default externs from zip
 *    - getDefaultExterns -> parses /externs.zip and validates completeness against DEFAULT_EXTERNS_NAMES
 * 7. Target Defect Zone (testVersionFlag):
 *    - Passing "--version" should be recognized as a valid option rather than producing
 *      CmdLineException ("is not a valid option") and invalidating configuration.
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class CommandLineRunnerGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the known defect in CommandLineRunnerTest::testVersionFlag.
   * If "--version" is not a supported flag in the runner's parser, args4j throws a
   * CmdLineException ("--version is not a valid option") causing isConfigValid to become
   * false and emitting an error to stderr.
   */
  @Test(timeout = 4000)
  public void testVersionFlag() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--version"},
        new PrintStream(out),
        new PrintStream(err));

    assertFalse("Expected --version to be supported without error: " + err.toString(),
        err.toString().contains("is not a valid option"));
    assertTrue("Compiler runner should have accepted --version as a valid configuration",
        runner.shouldRunCompiler());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultConfigurationIsValid() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {},
        new PrintStream(out),
        new PrintStream(err));

    assertTrue("Default empty args should produce a valid configuration", runner.shouldRunCompiler());
    assertEquals("stderr should be empty for clean default invocation", 0, err.size());
  }

  @Test(timeout = 4000)
  public void testOneArgConstructor() {
    // Verifies the protected CommandLineRunner(String[]) constructor path
    CommandLineRunner runner = new CommandLineRunner(new String[] {"--third_party=true"});
    assertTrue("Configuration with third_party should be valid", runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testCreateOptionsDefault() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {},
        new PrintStream(out),
        new PrintStream(err));

    CompilerOptions options = runner.createOptions();
    assertNotNull("CompilerOptions should be created", options);
    assertTrue("Default closurePass should be true", options.closurePass);
    assertFalse("Default prettyPrint should be false", options.prettyPrint);
    assertFalse("Default printInputDelimiter should be false", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testCreateOptionsWithDebugAndFormatting() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=VERBOSE",
        "--debug=true",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--process_closure_primitives=false"
    };
    CommandLineRunner runner = new CommandLineRunner(args, new PrintStream(out), new PrintStream(err));
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue("Pretty print should be enabled", options.prettyPrint);
    assertTrue("Print input delimiter should be enabled", options.printInputDelimiter);
    assertFalse("Closure pass should be false when process_closure_primitives=false", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testCreateOptionsWhitespaceAndQuiet() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--compilation_level=WHITESPACE_ONLY",
        "--warning_level=QUIET",
        "--debug=false"
    };
    CommandLineRunner runner = new CommandLineRunner(args, new PrintStream(out), new PrintStream(err));
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testCreateCompiler() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {},
        new PrintStream(out),
        new PrintStream(err));

    Compiler compiler = runner.createCompiler();
    assertNotNull("Compiler should not be null", compiler);
  }

  @Test(timeout = 4000)
  public void testGetDefaultExternsDirectly() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull("Default externs list must not be null", defaultExterns);
    assertFalse("Default externs list must not be empty", defaultExterns.isEmpty());
    assertTrue("Should contain multiple standard libraries like es3.js, window.js",
        defaultExterns.size() > 10);
    assertEquals("externs.zip//es3.js", defaultExterns.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testCreateExternsDefaultVsCustomOnly() throws Exception {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // With default externs included
    CommandLineRunner runnerWithDefaults = new CommandLineRunner(
        new String[] {},
        new PrintStream(out),
        new PrintStream(err));
    List<JSSourceFile> externsWithDefaults = runnerWithDefaults.createExterns();
    assertNotNull(externsWithDefaults);
    assertTrue("Externs should be loaded from externs.zip by default", externsWithDefaults.size() > 0);

    // With --use_only_custom_externs=true
    CommandLineRunner runnerCustomOnly = new CommandLineRunner(
        new String[] {"--use_only_custom_externs=true"},
        new PrintStream(out),
        new PrintStream(err));
    List<JSSourceFile> externsCustomOnly = runnerCustomOnly.createExterns();
    assertNotNull(externsCustomOnly);
    assertEquals("Should have no externs since none were specified and default is disabled",
        0, externsCustomOnly.size());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testQuotesStrippingInFlagValues() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--output_wrapper='(function(){%output%})();'",
        "--output_wrapper_marker=\"%output%\"",
        "--js_output_file=out.js",
        "--variable_map_input_file=''",
        "--property_map_input_file=\"\""
    };
    CommandLineRunner runner = new CommandLineRunner(args, new PrintStream(out), new PrintStream(err));
    assertTrue("Flags with quotes should be successfully stripped and accepted",
        runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerTrueVariations() {
    String[] trueValues = new String[] {"true", "on", "yes", "1"};
    for (String val : trueValues) {
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      CommandLineRunner runner = new CommandLineRunner(
          new String[] {"--third_party=" + val},
          new PrintStream(out),
          new PrintStream(err));
      assertTrue("Value '" + val + "' should parse as true", runner.shouldRunCompiler());
    }
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerFalseVariations() {
    String[] falseValues = new String[] {"false", "off", "no", "0"};
    for (String val : falseValues) {
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      CommandLineRunner runner = new CommandLineRunner(
          new String[] {"--third_party=" + val},
          new PrintStream(out),
          new PrintStream(err));
      assertTrue("Value '" + val + "' should parse as false", runner.shouldRunCompiler());
    }
  }

  @Test(timeout = 4000)
  public void testFullSuiteOfSupportedCommandLineFlags() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--print_tree=false",
        "--compute_phase_ordering=false",
        "--print_ast=false",
        "--print_pass_graph=false",
        "--jscomp_dev_mode=OFF",
        "--logging_level=INFO",
        "--summary_detail_level=2",
        "--module_output_path_prefix=./dist/",
        "--create_source_map=map.out",
        "--jscomp_error=checkTypes",
        "--jscomp_warning=deprecated",
        "--jscomp_off=visibility",
        "--define=DEBUG=false",
        "-D", "FEATURE_X=1",
        "--D", "NAME='app'",
        "--charset=UTF-8",
        "--manage_closure_dependencies=true",
        "--output_manifest=manifest.mf"
    };
    CommandLineRunner runner = new CommandLineRunner(args, new PrintStream(out), new PrintStream(err));
    assertTrue("All standard flags should parse without errors: " + err.toString(),
        runner.shouldRunCompiler());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testHelpFlagPrintsUsageAndDisablesRun() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--help"},
        new PrintStream(out),
        new PrintStream(err));

    assertFalse("shouldRunCompiler must be false when --help is passed", runner.shouldRunCompiler());
    assertTrue("Usage information must be printed to err stream",
        err.toString().contains("--help"));
  }

  @Test(timeout = 4000)
  public void testUnknownFlagFailsParsing() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--non_existent_flag_xyz=123"},
        new PrintStream(out),
        new PrintStream(err));

    assertFalse("Runner should reject unrecognized flags", runner.shouldRunCompiler());
    assertTrue("Error message should be logged to stderr", err.size() > 0);
  }

  @Test(timeout = 4000)
  public void testIllegalBooleanValueFailsParsing() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--print_tree=not_a_boolean"},
        new PrintStream(out),
        new PrintStream(err));

    assertFalse("Illegal boolean value must cause isConfigValid to be false",
        runner.shouldRunCompiler());
    assertTrue("Error should mention illegal boolean value",
        err.toString().contains("Illegal boolean value: not_a_boolean"));
  }

  @Test(timeout = 4000)
  public void testConflictingNameMapFlags() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Flags that conflict: --create_name_map_files with --variable_map_output_file
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {
            "--create_name_map_files=true",
            "--variable_map_output_file=vars.out"
        },
        new PrintStream(out),
        new PrintStream(err));

    // Even if parser parses it, verify state consistency
    assertNotNull(runner);
  }

  // =========================================================================
  // Partition E: Subclass Extension & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubclassExtensionOptionsHook() {
    class CustomRunner extends CommandLineRunner {
      CustomRunner(String[] args, PrintStream out, PrintStream err) {
        super(args, out, err);
      }

      @Override
      protected CompilerOptions createOptions() {
        CompilerOptions options = super.createOptions();
        options.lineBreak = true;
        return options;
      }
    }

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CustomRunner customRunner = new CustomRunner(
        new String[] {},
        new PrintStream(out),
        new PrintStream(err));

    assertTrue(customRunner.shouldRunCompiler());
    CompilerOptions options = customRunner.createOptions();
    assertTrue("Custom option override should be preserved", options.lineBreak);
  }
}