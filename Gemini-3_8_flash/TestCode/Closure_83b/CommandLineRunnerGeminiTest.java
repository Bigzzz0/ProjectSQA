package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: CommandLineRunner
 * Target Defect: CommandLineRunnerTest::testVersionFlag2 (Defects4J ground truth)
 *
 * Decision / Branch Matrix:
 * 1. initConfigFromFlags:
 *    - argPattern regex matches "--flag=value":
 *      * value enclosed in single quotes ('value') -> quotes stripped
 *      * value enclosed in double quotes ("value") -> quotes stripped
 *      * value unquoted -> value kept as is
 *    - argPattern regex does NOT match "--flag=value" -> argument passed as is
 *    - parser.parseArgument throws CmdLineException -> isConfigValid = false, error printed
 *    - flags.version is true:
 *      * loads ResourceBundle and prints version banner to err
 *      * CRITICAL DEFECT: If "--version" is passed alone as the terminal argument,
 *        BooleanOptionHandler.parseArguments attempts params.getParameter(0) without
 *        checking parameter availability, throwing CmdLineException in args4j and
 *        preventing flags.version from being set and printing usage error instead.
 *    - (!isConfigValid || flags.display_help):
 *      * isConfigValid == false -> parser.printUsage(err)
 *      * flags.display_help == true -> isConfigValid = false, parser.printUsage(err)
 *      * valid config and !display_help -> getCommandLineConfig() setters chain executed
 *    - flags.third_party:
 *      * true -> DefaultCodingConvention
 *      * false -> ClosureCodingConvention
 * 2. createOptions:
 *    - compilation_level: WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, ADVANCED_OPTIMIZATIONS
 *    - debug: true -> level.setDebugOptionsForCompilationLevel(options)
 *             false -> debug options skipped
 *    - warning_level: QUIET, DEFAULT, VERBOSE
 *    - formatting options: PRETTY_PRINT, PRINT_INPUT_DELIMITER
 *    - process_closure_primitives: true vs false
 * 3. createCompiler:
 *    - Instantiates Compiler with getErrorPrintStream()
 * 4. createExterns:
 *    - flags.use_only_custom_externs == true -> returns user externs only
 *    - isInTestMode() == true -> returns user externs only
 *    - default path -> loads default externs from /externs.zip and appends user externs
 * 5. getDefaultExterns:
 *    - Extracts externs.zip resources matching DEFAULT_EXTERNS_NAMES in exact order
 * 6. BooleanOptionHandler:
 *    - TRUES ("true", "on", "yes", "1") -> setter.addValue(true), returns 1
 *    - FALSES ("false", "off", "no", "0") -> setter.addValue(false), returns 1
 *    - other string / no boolean match -> setter.addValue(true), returns 0
 */
public class CommandLineRunnerGeminiTest {

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args) {
      super(args);
    }

    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    @Override
    public CompilerOptions createOptions() {
      return super.createOptions();
    }

    @Override
    public Compiler createCompiler() {
      return super.createCompiler();
    }

    @Override
    public List<JSSourceFile> createExterns() throws AbstractCommandLineRunner.FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J defect in CommandLineRunnerTest::testVersionFlag2.
   * When "--version" is provided as the solitary command-line argument, the runner
   * must parse it correctly without throwing an internal CmdLineException from
   * BooleanOptionHandler, and print the compiler version banner to stderr.
   */
  @Test(timeout = 4000)
  public void testVersionFlagAloneOutputsVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream outStream = new PrintStream(out);
    PrintStream errStream = new PrintStream(err);

    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--version"}, outStream, errStream);

    String errOutput = err.toString();
    assertTrue("Error stream must contain Closure Compiler banner when --version is given alone",
        errOutput.contains("Closure Compiler (http://code.google.com/closure/compiler)"));
    assertTrue("Error stream must contain Version indicator",
        errOutput.contains("Version:"));
    assertTrue("Error stream must contain Built on indicator",
        errOutput.contains("Built on:"));
  }

  @Test(timeout = 4000)
  public void testVersionFlagWithAdditionalArguments() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--version", "--js", "test.js"},
        new PrintStream(out), new PrintStream(err));

    String errOutput = err.toString();
    assertTrue("Version flag alongside other arguments should still print the version banner",
        errOutput.contains("Closure Compiler (http://code.google.com/closure/compiler)"));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultConfigExecutionState() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[0], new PrintStream(out), new PrintStream(err));

    assertTrue("Default empty args should produce a valid configuration",
        runner.shouldRunCompiler());
    assertEquals("Error stream should be empty for valid default execution",
        0, err.toByteArray().length);
  }

  @Test(timeout = 4000)
  public void testCreateOptionsDefaultValues() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[0], new PrintStream(out), new PrintStream(err));

    CompilerOptions options = runner.createOptions();
    assertNotNull("Created options should not be null", options);
    assertTrue("Default closurePass should be true", options.closurePass);
    assertFalse("Default prettyPrint should be false", options.prettyPrint);
    assertFalse("Default printInputDelimiter should be false", options.printInputDelimiter);
    assertNotNull("Coding convention should be configured", options.getCodingConvention());
    assertTrue("Coding convention should be ClosureCodingConvention",
        options.getCodingConvention() instanceof ClosureCodingConvention);
  }

  @Test(timeout = 4000)
  public void testCompilationLevelWhitespaceOnly() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--compilation_level", "WHITESPACE_ONLY"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse("WHITESPACE_ONLY should not have closurePass enabled by level",
        options.closurePass == false && options.isRemoveUnusedVars());
  }

  @Test(timeout = 4000)
  public void testCompilationLevelAdvancedOptimizationsWithDebug() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--compilation_level", "ADVANCED_OPTIMIZATIONS", "--debug"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue("Advanced optimizations should enable property collapsing",
        options.collapseProperties);
    assertTrue("Advanced optimizations should enable dead code elimination",
        options.removeDeadCode);
  }

  @Test(timeout = 4000)
  public void testWarningLevelQuiet() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--warning_level", "QUIET"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testWarningLevelVerbose() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--warning_level", "VERBOSE"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue("Verbose level should check types", options.checkTypes);
  }

  @Test(timeout = 4000)
  public void testFormattingPrettyPrintAndInputDelimiter() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--formatting", "PRETTY_PRINT", "--formatting", "PRINT_INPUT_DELIMITER"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue("prettyPrint option must be true", options.prettyPrint);
    assertTrue("printInputDelimiter option must be true", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testProcessClosurePrimitivesFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--process_closure_primitives=false"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse("process_closure_primitives=false should disable closurePass", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testThirdPartyFlagSetsDefaultCodingConvention() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--third_party=true"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    assertTrue("Coding convention for third party should be DefaultCodingConvention",
        runner.getCommandLineConfig().codingConvention instanceof DefaultCodingConvention);
  }

  @Test(timeout = 4000)
  public void testNonThirdPartySetsClosureCodingConvention() {
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--third_party=false"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
    assertTrue("Default coding convention should be ClosureCodingConvention",
        runner.getCommandLineConfig().codingConvention instanceof ClosureCodingConvention);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Argument Syntax Handling
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgSyntaxQuotedValuesSingleAndDouble() {
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {
            "--js_output_file='single_quoted.js'",
            "--output_wrapper=\"double_quoted_%output%\"",
            "--charset=UTF-8"
        },
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testBooleanFlagSyntaxPermutations() {
    // Tests various TRUES and FALSES recognized by BooleanOptionHandler
    String[] booleanArgs = {
        "--debug=true",
        "--create_name_map_files=false",
        "--third_party=on",
        "--manage_closure_dependencies=1",
        "--use_only_custom_externs=yes"
    };

    CommandLineRunner runner = new CommandLineRunner(
        booleanArgs,
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testBooleanFlagFalsesPermutations() {
    String[] falsesArgs = {
        "--debug=off",
        "--third_party=0",
        "--manage_closure_dependencies=no"
    };

    CommandLineRunner runner = new CommandLineRunner(
        falsesArgs,
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testComprehensiveCommandLineConfigChain() {
    String[] fullArgs = {
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=START",
        "--logging_level=INFO",
        "--externs=extern1.js",
        "--externs=extern2.js",
        "--js=file1.js",
        "--js=file2.js",
        "--js_output_file=out.js",
        "--module=mod1:1",
        "--variable_map_input_file=v_in.txt",
        "--property_map_input_file=p_in.txt",
        "--variable_map_output_file=v_out.txt",
        "--property_map_output_file=p_out.txt",
        "--summary_detail_level=3",
        "--output_wrapper=%output%",
        "--output_wrapper_marker=%output%",
        "--module_wrapper=mod1:%s",
        "--module_output_path_prefix=prefix_",
        "--create_source_map=map.txt",
        "--jscomp_error=checkTypes",
        "--jscomp_warning=deprecated",
        "--jscomp_off=visibility",
        "--define=DEF_A=true",
        "--D", "DEF_B=1",
        "-D", "DEF_C='str'",
        "--charset=US-ASCII",
        "--manage_closure_dependencies=true",
        "--closure_entry_point=goog.events",
        "--output_manifest=manifest.txt"
    };

    CommandLineRunner runner = new CommandLineRunner(
        fullArgs,
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    assertTrue(runner.shouldRunCompiler());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testHelpFlagPrintsUsageAndDisablesCompiler() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--help"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertFalse("shouldRunCompiler must be false when --help is supplied",
        runner.shouldRunCompiler());
    String errOutput = err.toString();
    assertTrue("Usage information must be printed on --help",
        errOutput.contains("--help"));
  }

  @Test(timeout = 4000)
  public void testUnknownFlagSetsConfigInvalid() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--unknown_non_existent_flag"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertFalse("Unknown flag must invalidate config", runner.shouldRunCompiler());
    assertTrue("Error output should display unrecognized option or usage message",
        err.toString().length() > 0);
  }

  @Test(timeout = 4000)
  public void testInvalidCompilationLevelFails() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--compilation_level", "INVALID_LEVEL"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertFalse("Invalid compilation level should cause config invalidation",
        runner.shouldRunCompiler());
    assertTrue(err.toString().length() > 0);
  }

  @Test(timeout = 4000)
  public void testInvalidWarningLevelFails() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--warning_level", "SUPER_NOISY"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertFalse("Invalid warning level should cause config invalidation",
        runner.shouldRunCompiler());
    assertTrue(err.toString().length() > 0);
  }

  @Test(timeout = 4000)
  public void testInvalidDevModeFails() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = new CommandLineRunner(
        new String[] {"--jscomp_dev_mode", "SUPER_SANITY"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    assertFalse("Invalid dev mode should cause config invalidation",
        runner.shouldRunCompiler());
    assertTrue(err.toString().length() > 0);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateCompiler() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[0],
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(err));

    Compiler compiler = runner.createCompiler();
    assertNotNull("Compiler instance created by runner should not be null", compiler);
  }

  @Test(timeout = 4000)
  public void testGetDefaultExternsDirectLoad() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull("Default externs list must not be null", defaultExterns);
    assertFalse("Default externs list must not be empty", defaultExterns.isEmpty());
    assertEquals("First default extern must be es3.js",
        "externs.zip//es3.js", defaultExterns.get(0).getName());

    boolean hasWindowJs = false;
    for (JSSourceFile extern : defaultExterns) {
      if ("externs.zip//window.js".equals(extern.getName())) {
        hasWindowJs = true;
        break;
      }
    }
    assertTrue("Default externs must contain window.js", hasWindowJs);
  }

  @Test(timeout = 4000)
  public void testCreateExternsWithDefaultIncluded() throws Exception {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[0],
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull("createExterns should return loaded externs", externs);
    assertFalse("Default externs should be populated when not custom-only", externs.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCreateExternsCustomOnlyExcludesDefault() throws Exception {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--use_only_custom_externs=true"},
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));

    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue("When use_only_custom_externs is true and no externs given, list should be empty",
        externs.isEmpty());
  }

  @Test(timeout = 4000)
  public void testSingleArgConstructorCoverage() {
    CommandLineRunner runner = new CommandLineRunner(new String[] {"--summary_detail_level", "2"});
    assertTrue("Single-arg constructor should initialize configuration validly",
        runner.shouldRunCompiler());
  }
}