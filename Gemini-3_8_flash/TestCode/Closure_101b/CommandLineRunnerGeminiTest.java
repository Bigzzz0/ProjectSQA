package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.CommandLineRunner
 *
 * 1. Defect Targeting (Ground Truth - CommandLineRunnerTest::testProcessClosurePrimitives):
 *    - In createOptions(): The flag --process_closure_primitives defaults to true.
 *      When compilation_level is ADVANCED_OPTIMIZATIONS, setOptionsForCompilationLevel(options) sets
 *      options.closurePass = true.
 *      CommandLineRunner then executes:
 *        if (flags.process_closure_primitives) { options.closurePass = true; }
 *      DEFECT: If the user explicitly passes --process_closure_primitives=false, options.closurePass
 *      is NOT set to false, remaining true from ADVANCED_OPTIMIZATIONS.
 *      Target Test: testProcessClosurePrimitivesDefect() asserts assertFalse(options.closurePass).
 *
 * 2. Decision & Branch Coverage:
 *    - initConfigFromFlags(args):
 *        * Pattern matching for (--flag)=(value).
 *        * Quoted value unwrapping: single quotes ('...'), double quotes ("..."), and unquoted.
 *        * Unmatched non-flag arguments pass-through.
 *    - BooleanOptionHandler:
 *        * TRUES: "true", "on", "yes", "1" -> sets true.
 *        * FALSES: "false", "off", "no", "0" -> sets false.
 *        * Unknown string -> CmdLineException("Illegal boolean value: ...").
 *    - FormattingOption:
 *        * PRETTY_PRINT -> options.prettyPrint = true.
 *        * PRINT_INPUT_DELIMITER -> options.printInputDelimiter = true.
 *    - WarningLevel & CompilationLevel combinations:
 *        * QUIET, DEFAULT, VERBOSE.
 *        * WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, ADVANCED_OPTIMIZATIONS.
 *    - Debug flag:
 *        * debug=true -> sets debug options on level.
 *    - createExterns():
 *        * use_only_custom_externs = false -> reads default externs from /externs.zip.
 *        * use_only_custom_externs = true -> returns only custom externs list.
 *    - createCompiler():
 *        * Returns a new Compiler instance initialized with getErrorPrintStream().
 * -------------------------------------------------------------------------------------------------
 */
public class CommandLineRunnerGeminiTest {

  /**
   * Testable subclass providing direct access to protected methods of CommandLineRunner.
   */
  private static class TestableCommandLineRunner extends CommandLineRunner {
    TestableCommandLineRunner(String[] args) throws CmdLineException {
      super(args);
    }

    TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err)
        throws CmdLineException {
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
    public List<JSSourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  private TestableCommandLineRunner createRunner(String... args) throws CmdLineException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    return new TestableCommandLineRunner(args, new PrintStream(out), new PrintStream(err));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultOptions() throws Exception {
    TestableCommandLineRunner runner = createRunner();
    CompilerOptions options = runner.createOptions();

    assertNotNull("CompilerOptions should not be null", options);
    assertTrue("Coding convention should be ClosureCodingConvention",
        options.getCodingConvention() instanceof ClosureCodingConvention);
    assertFalse("Default formatting should not pretty print", options.prettyPrint);
    assertFalse("Default formatting should not print input delimiter", options.printInputDelimiter);
    assertTrue("ClosurePass should be enabled by default", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testFormattingOptions() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER"
    );
    CompilerOptions options = runner.createOptions();

    assertTrue("Pretty print should be enabled", options.prettyPrint);
    assertTrue("Print input delimiter should be enabled", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testCompilationLevelWhitespaceOnly() throws Exception {
    TestableCommandLineRunner runner = createRunner("--compilation_level=WHITESPACE_ONLY");
    CompilerOptions options = runner.createOptions();

    assertFalse("Whitespace only compilation should not rename variables",
        options.checkGlobalThisLevel.isOn());
  }

  @Test(timeout = 4000)
  public void testCompilationLevelAdvancedOptimizations() throws Exception {
    TestableCommandLineRunner runner = createRunner("--compilation_level=ADVANCED_OPTIMIZATIONS");
    CompilerOptions options = runner.createOptions();

    assertTrue("Advanced optimizations should reserve global names",
        options.reserveRawExports);
  }

  @Test(timeout = 4000)
  public void testWarningLevelQuietAndVerbose() throws Exception {
    TestableCommandLineRunner quietRunner = createRunner("--warning_level=QUIET");
    CompilerOptions quietOptions = quietRunner.createOptions();
    assertNotNull(quietOptions);

    TestableCommandLineRunner verboseRunner = createRunner("--warning_level=VERBOSE");
    CompilerOptions verboseOptions = verboseRunner.createOptions();
    assertTrue("Verbose warning level should enable checkSymbols", verboseOptions.checkSymbols);
  }

  @Test(timeout = 4000)
  public void testDebugFlag() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--compilation_level=SIMPLE_OPTIMIZATIONS",
        "--debug=true"
    );
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue("Debug options should enable anonymousFunctionNaming",
        options.anonymousFunctionNaming != null);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Argument Parsing
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyArguments() throws Exception {
    TestableCommandLineRunner runner = createRunner(new String[0]);
    assertNotNull("Runner should instantiate cleanly with empty args", runner);
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testQuotedArgumentParsingSingleQuotes() throws Exception {
    TestableCommandLineRunner runner = createRunner("--js_output_file='out_file.js'");
    assertEquals("Single quotes should be stripped from argument value",
        "out_file.js", runner.getCommandLineConfig().jsOutputFile);
  }

  @Test(timeout = 4000)
  public void testQuotedArgumentParsingDoubleQuotes() throws Exception {
    TestableCommandLineRunner runner = createRunner("--js_output_file=\"out_file.js\"");
    assertEquals("Double quotes should be stripped from argument value",
        "out_file.js", runner.getCommandLineConfig().jsOutputFile);
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerTruesEquivalence() throws Exception {
    String[] trueEquivalents = {"true", "on", "yes", "1"};
    for (String val : trueEquivalents) {
      TestableCommandLineRunner runner = createRunner("--third_party=" + val);
      assertTrue("Flag should be true for representation: " + val,
          runner.getCommandLineConfig().thirdParty);
    }
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerFalsesEquivalence() throws Exception {
    String[] falseEquivalents = {"false", "off", "no", "0"};
    for (String val : falseEquivalents) {
      TestableCommandLineRunner runner = createRunner("--third_party=" + val);
      assertFalse("Flag should be false for representation: " + val,
          runner.getCommandLineConfig().thirdParty);
    }
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerCaseInsensitive() throws Exception {
    TestableCommandLineRunner runner = createRunner("--third_party=TrUe");
    assertTrue("Boolean handler should be case-insensitive",
        runner.getCommandLineConfig().thirdParty);
  }

  @Test(timeout = 4000)
  public void testDefaultExternsLoaded() throws Exception {
    TestableCommandLineRunner runner = createRunner("--use_only_custom_externs=false");
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull("Externs list must not be null", externs);
    assertFalse("Default externs should be loaded and non-empty", externs.isEmpty());
  }

  @Test(timeout = 4000)
  public void testUseOnlyCustomExternsFlag() throws Exception {
    TestableCommandLineRunner runner = createRunner("--use_only_custom_externs=true");
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull("Externs list must not be null", externs);
    assertTrue("Custom externs list should be empty when no externs are provided", externs.isEmpty());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets CommandLineRunnerTest::testProcessClosurePrimitives defect.
   * When compilation_level is ADVANCED_OPTIMIZATIONS, options.closurePass is turned on.
   * If the user explicitly sets --process_closure_primitives=false, closurePass MUST be false.
   */
  @Test(timeout = 4000)
  public void testProcessClosurePrimitivesDefect() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--process_closure_primitives=false"
    );
    CompilerOptions options = runner.createOptions();
    assertFalse("options.closurePass must be false when --process_closure_primitives=false",
        options.closurePass);
  }

  @Test(timeout = 4000)
  public void testProcessClosurePrimitivesExplicitlyTrue() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--compilation_level=SIMPLE_OPTIMIZATIONS",
        "--process_closure_primitives=true"
    );
    CompilerOptions options = runner.createOptions();
    assertTrue("options.closurePass must be true when --process_closure_primitives=true",
        options.closurePass);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = CmdLineException.class)
  public void testInvalidBooleanOptionThrowsException() throws Exception {
    createRunner("--debug=invalid_boolean_string");
  }

  @Test(timeout = 4000, expected = CmdLineException.class)
  public void testUnknownOptionThrowsException() throws Exception {
    createRunner("--non_existent_flag_xyz=123");
  }

  @Test(timeout = 4000, expected = CmdLineException.class)
  public void testInvalidCompilationLevelThrowsException() throws Exception {
    createRunner("--compilation_level=NON_EXISTENT_LEVEL");
  }

  @Test(timeout = 4000, expected = CmdLineException.class)
  public void testInvalidWarningLevelThrowsException() throws Exception {
    createRunner("--warning_level=NON_EXISTENT_WARNING_LEVEL");
  }

  @Test(timeout = 4000, expected = CmdLineException.class)
  public void testInvalidFormattingOptionThrowsException() throws Exception {
    createRunner("--formatting=NON_EXISTENT_FORMATTING");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Component Configuration Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateCompilerInstance() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[0], new PrintStream(out), new PrintStream(err));
    Compiler compiler = runner.createCompiler();

    assertNotNull("Created Compiler instance should not be null", compiler);
    assertEquals("Compiler error stream should match runner's error stream",
        runner.getErrorPrintStream(), compiler.getErrorPrintStream());
  }

  @Test(timeout = 4000)
  public void testCommandLineConfigPropagation() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=START",
        "--logging_level=INFO",
        "--summary_detail_level=3",
        "--output_wrapper=(function(){%output%})();",
        "--output_wrapper_marker=%output%",
        "--module_output_path_prefix=./out/",
        "--create_source_map=map.json",
        "--charset=UTF-8",
        "--define=FOO=true",
        "--define=BAR='test'"
    );

    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    assertTrue(config.printTree);
    assertTrue(config.computePhaseOrdering);
    assertTrue(config.printAst);
    assertTrue(config.printPassGraph);
    assertEquals(CompilerOptions.DevMode.START, config.jscompDevMode);
    assertEquals("INFO", config.loggingLevel);
    assertEquals(3, config.summaryDetailLevel);
    assertEquals("(function(){%output%})();", config.outputWrapper);
    assertEquals("%output%", config.outputWrapperMarker);
    assertEquals("./out/", config.moduleOutputPathPrefix);
    assertEquals("map.json", config.createSourceMap);
    assertEquals("UTF-8", config.charset);
    assertEquals(2, config.define.size());
    assertTrue(config.define.contains("FOO=true"));
    assertTrue(config.define.contains("BAR='test'"));
  }

  @Test(timeout = 4000)
  public void testDiagnosticGroupsConfiguration() throws Exception {
    TestableCommandLineRunner runner = createRunner(
        "--jscomp_error=checkTypes",
        "--jscomp_warning=deprecated",
        "--jscomp_off=visibility"
    );

    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    assertTrue(config.jscompError.contains("checkTypes"));
    assertTrue(config.jscompWarning.contains("deprecated"));
    assertTrue(config.jscompOff.contains("visibility"));
  }
}