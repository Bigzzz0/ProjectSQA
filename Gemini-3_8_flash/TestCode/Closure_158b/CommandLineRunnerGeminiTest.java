/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.javascript.jscomp.CommandLineRunner
 *
 * 1. Defect Analysis (Defects4J - CommandLineRunnerTest):
 *    - testWarningGuardOrdering2: Flags "--jscomp_off=globalThis" followed by "--jscomp_error=globalThis".
 *      Defect: Flag ordering is disregarded because CommandLineRunner parses flags into separate lists
 *      (jscomp_off, jscomp_error, jscomp_warning) without maintaining an interleaved ordering sequence.
 *      When options are applied, 'off' overrides 'error', resulting in 0 errors instead of the expected 1 error.
 *    - testWarningGuardOrdering4: Flags "--jscomp_off=globalThis" followed by "--jscomp_warning=globalThis".
 *      Defect: 'off' overrides 'warning', resulting in 0 warnings instead of the expected 1 warning.
 *
 * 2. Branch & Decision Logic Coverage:
 *    - processArgs():
 *      * Matches regex "(--[a-zA-Z_]+)=(.*)" vs non-matching tokens.
 *      * Value enclosed in quotes ("...", '...') vs unquoted value.
 *    - initConfigFromFlags():
 *      * Invalid CLI args (CmdLineException caught -> isConfigValid = false).
 *      * flags.version == true -> prints version resource bundle to stderr.
 *      * flags.display_help == true / !isConfigValid -> parser.printUsage(err).
 *      * flags.third_party: true (DefaultCodingConvention) vs false (ClosureCodingConvention).
 *      * Configuration mappings for all options (dev_mode, logging, modules, source maps, etc.).
 *    - processFlagFile():
 *      * Normal flagfile reading and parsing.
 *      * Nested flagfile rejection: flags.flag_file != "" -> error message printed, isConfigValid = false.
 *      * Non-existent flagfile -> IOException caught, isConfigValid = false.
 *    - createOptions():
 *      * Compilation levels: WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, ADVANCED_OPTIMIZATIONS.
 *      * flags.debug: true (setDebugOptionsForCompilationLevel) vs false.
 *      * flags.generate_exports: true vs false.
 *      * Warning levels: QUIET, DEFAULT, VERBOSE.
 *      * Formatting options: PRETTY_PRINT, PRINT_INPUT_DELIMITER.
 *      * flags.process_closure_primitives: true vs false.
 *    - createExterns():
 *      * flags.use_only_custom_externs == true / isInTestMode() -> only custom externs.
 *      * Default branch -> getDefaultExterns() + custom externs.
 *    - BooleanOptionHandler:
 *      * null param (setter.addValue(true), return 0).
 *      * TRUES: "true", "on", "yes", "1" (setter.addValue(true), return 1).
 *      * FALSES: "false", "off", "no", "0" (setter.addValue(false), return 1).
 *      * Other unknown values: setter.addValue(true), return 0.
 *      * getDefaultMetaVariable() -> returns null.
 */

package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

public class CommandLineRunnerGeminiTest {

  // Subclass providing access to internal state and compiler output
  private static class TestableCommandLineRunner extends CommandLineRunner {
    private Compiler compiler;

    TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    TestableCommandLineRunner(String[] args) {
      super(args);
    }

    @Override
    protected Compiler createCompiler() {
      this.compiler = super.createCompiler();
      return this.compiler;
    }

    Compiler getCompiler() {
      return this.compiler;
    }

    CompilerOptions getOptions() {
      return super.createOptions();
    }

    List<JSSourceFile> getExternsForTesting() throws Exception {
      return super.createExterns();
    }
  }

  // Utility to create temporary source files
  private File createTempSourceFile(String prefix, String suffix, String content) throws IOException {
    File file = File.createTempFile(prefix, suffix);
    file.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(file);
    try {
      fos.write(content.getBytes("UTF-8"));
    } finally {
      fos.close();
    }
    return file;
  }

  // Utility to run a compilation with given arguments and source code
  private TestableCommandLineRunner compileWithRunner(String[] extraArgs, String jsCode) throws IOException {
    File jsFile = createTempSourceFile("test_input", ".js", jsCode);
    List<String> argsList = Lists.newArrayList();
    for (String arg : extraArgs) {
      argsList.add(arg);
    }
    argsList.add("--js=" + jsFile.getAbsolutePath());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        argsList.toArray(new String[0]),
        new PrintStream(out),
        new PrintStream(err)
    );

    if (runner.shouldRunCompiler()) {
      runner.run();
    }
    return runner;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultCommandLineRunnerInitialization() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[0], new PrintStream(out), new PrintStream(err));

    assertTrue("Runner should be valid with empty arguments", runner.shouldRunCompiler());
    CompilerOptions options = runner.getOptions();
    assertNotNull("CompilerOptions must not be null", options);
    assertTrue("closurePass should default to true", options.closurePass);
    assertFalse("prettyPrint should default to false", options.prettyPrint);
  }

  @Test(timeout = 4000)
  public void testSingleArgConstructorCoverage() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[0]);
    assertTrue("Runner with single-arg constructor should be valid", runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testCreateCompilerInitialization() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[0], new PrintStream(out), new PrintStream(err));

    Compiler comp = runner.createCompiler();
    assertNotNull("Created compiler should not be null", comp);
  }

  @Test(timeout = 4000)
  public void testCompilationLevelWhitespaceOnly() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--compilation_level=WHITESPACE_ONLY" },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.getOptions();
    assertFalse("Whitespace only should not enable variable renaming", options.checkGlobalThisLevel.isOn());
  }

  @Test(timeout = 4000)
  public void testCompilationLevelAdvancedOptimizations() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--compilation_level=ADVANCED_OPTIMIZATIONS" },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.getOptions();
    assertTrue("Advanced optimizations should enable smart name removal", options.smartNameRemoval);
  }

  @Test(timeout = 4000)
  public void testDebugAndGenerateExportsAndFormattingOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {
            "--debug=true",
            "--generate_exports=true",
            "--formatting=PRETTY_PRINT",
            "--formatting=PRINT_INPUT_DELIMITER",
            "--process_closure_primitives=false"
        },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.getOptions();
    assertTrue("Pretty print must be enabled", options.prettyPrint);
    assertTrue("Print input delimiter must be enabled", options.printInputDelimiter);
    assertFalse("Closure pass should be false", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testWarningLevelQuietAndVerbose() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner quietRunner = new TestableCommandLineRunner(
        new String[] { "--warning_level=QUIET" },
        new PrintStream(out), new PrintStream(err));
    assertTrue(quietRunner.shouldRunCompiler());

    TestableCommandLineRunner verboseRunner = new TestableCommandLineRunner(
        new String[] { "--warning_level=VERBOSE" },
        new PrintStream(out), new PrintStream(err));
    assertTrue(verboseRunner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testComprehensiveOptionPassing() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=OFF",
        "--logging_level=INFO",
        "--js_output_file=out.js",
        "--variable_map_input_file=vmap.in",
        "--property_map_input_file=pmap.in",
        "--variable_map_output_file=vmap.out",
        "--property_map_output_file=pmap.out",
        "--summary_detail_level=3",
        "--output_wrapper=%output%",
        "--module_output_path_prefix=./dist/",
        "--create_source_map=map.out",
        "--define=FOO=true",
        "--charset=UTF-8",
        "--manage_closure_dependencies=true",
        "--closure_entry_point=goog.dom",
        "--output_manifest=manifest.out",
        "--accept_const_keyword=true",
        "--language_in=ECMASCRIPT5"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        args, new PrintStream(out), new PrintStream(err));
    assertTrue("All standard flags should be parsed successfully", runner.shouldRunCompiler());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessArgsQuotationStripping() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String[] args = new String[] {
        "--js_output_file=\"custom_out.js\"",
        "--output_wrapper='(function(){%output%})();'",
        "normal_unquoted_token"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        args, new PrintStream(out), new PrintStream(err));
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testCodingConventionThirdPartyVsClosure() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner defaultRunner = new TestableCommandLineRunner(
        new String[] { "--third_party=false" },
        new PrintStream(out), new PrintStream(err));
    assertTrue(defaultRunner.shouldRunCompiler());

    TestableCommandLineRunner thirdPartyRunner = new TestableCommandLineRunner(
        new String[] { "--third_party=true" },
        new PrintStream(out), new PrintStream(err));
    assertTrue(thirdPartyRunner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerDirectly() throws Exception {
    final boolean[] target = new boolean[1];
    Setter<Boolean> setter = new Setter<Boolean>() {
      public void addValue(Boolean value) { target[0] = value; }
      public Class<Boolean> getType() { return Boolean.class; }
      public boolean isMultiValued() { return false; }
    };

    CommandLineRunner.BooleanOptionHandler handler =
        new CommandLineRunner.BooleanOptionHandler(null, null, setter);

    assertNull("Default metavariable should be null", handler.getDefaultMetaVariable());

    // 1. Missing parameter branch (CmdLineException thrown)
    Parameters emptyParams = new Parameters() {
      public String getParameter(int idx) throws CmdLineException {
        throw new CmdLineException((CmdLineParser) null, "Missing param");
      }
      public int size() { return 0; }
    };
    target[0] = false;
    assertEquals(0, handler.parseArguments(emptyParams));
    assertTrue("Missing parameter should default to true", target[0]);

    // 2. Truthy values ("true", "on", "yes", "1")
    String[] trues = new String[] { "true", "on", "yes", "1", "TRUE", "On" };
    for (final String t : trues) {
      target[0] = false;
      Parameters p = new Parameters() {
        public String getParameter(int idx) { return t; }
        public int size() { return 1; }
      };
      assertEquals("Param " + t + " should consume 1 token", 1, handler.parseArguments(p));
      assertTrue("Param " + t + " should set true", target[0]);
    }

    // 3. Falsy values ("false", "off", "no", "0")
    String[] falses = new String[] { "false", "off", "no", "0", "FALSE", "Off" };
    for (final String f : falses) {
      target[0] = true;
      Parameters p = new Parameters() {
        public String getParameter(int idx) { return f; }
        public int size() { return 1; }
      };
      assertEquals("Param " + f + " should consume 1 token", 1, handler.parseArguments(p));
      assertFalse("Param " + f + " should set false", target[0]);
    }

    // 4. Unknown/unrecognized argument branch
    target[0] = false;
    Parameters otherParams = new Parameters() {
      public String getParameter(int idx) { return "notABool"; }
      public int size() { return 1; }
    };
    assertEquals("Unknown token should consume 0 tokens", 0, handler.parseArguments(otherParams));
    assertTrue("Unknown token should set true", target[0]);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Defects4J Ground Truth: CommandLineRunnerTest::testWarningGuardOrdering2
   * Flag order: --jscomp_off=globalThis followed by --jscomp_error=globalThis.
   * Expected: Exactly 1 error because --jscomp_error comes AFTER --jscomp_off.
   * On buggy version: CommandLineRunner loses ordering and applies 'off' last, yielding 0 errors.
   */
  @Test(timeout = 4000)
  public void testWarningGuardOrdering2_offThenError_RevealsDefect() throws IOException {
    String jsCode = "function f() { this.a = 3; }";
    String[] flags = new String[] {
        "--jscomp_off=globalThis",
        "--jscomp_error=globalThis"
    };

    TestableCommandLineRunner runner = compileWithRunner(flags, jsCode);
    assertTrue("Compiler should be runnable", runner.shouldRunCompiler());

    Compiler compiler = runner.getCompiler();
    assertNotNull("Compiler must have executed", compiler);
    assertEquals("Expected exactly one warning or error Errors: " +
        (compiler.getErrors() != null && compiler.getErrors().length > 0 ? compiler.getErrors()[0].description : "none"),
        1, compiler.getErrorCount());
  }

  /**
   * Defects4J Ground Truth: CommandLineRunnerTest::testWarningGuardOrdering4
   * Flag order: --jscomp_off=globalThis followed by --jscomp_warning=globalThis.
   * Expected: Exactly 1 warning because --jscomp_warning comes AFTER --jscomp_off.
   * On buggy version: CommandLineRunner loses ordering and applies 'off' last, yielding 0 warnings.
   */
  @Test(timeout = 4000)
  public void testWarningGuardOrdering4_offThenWarning_RevealsDefect() throws IOException {
    String jsCode = "function f() { this.a = 3; }";
    String[] flags = new String[] {
        "--jscomp_off=globalThis",
        "--jscomp_warning=globalThis"
    };

    TestableCommandLineRunner runner = compileWithRunner(flags, jsCode);
    assertTrue("Compiler should be runnable", runner.shouldRunCompiler());

    Compiler compiler = runner.getCompiler();
    assertNotNull("Compiler must have executed", compiler);
    assertEquals("Expected exactly one warning or error Errors: " +
        (compiler.getWarnings() != null && compiler.getWarnings().length > 0 ? compiler.getWarnings()[0].description : "none"),
        1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering1_errorThenOff() throws IOException {
    String jsCode = "function f() { this.a = 3; }";
    String[] flags = new String[] {
        "--jscomp_error=globalThis",
        "--jscomp_off=globalThis"
    };

    TestableCommandLineRunner runner = compileWithRunner(flags, jsCode);
    assertTrue(runner.shouldRunCompiler());

    Compiler compiler = runner.getCompiler();
    assertNotNull(compiler);
    assertEquals("Expected 0 errors when off comes after error", 0, compiler.getErrorCount());
    assertEquals("Expected 0 warnings when off comes after error", 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering3_warningThenOff() throws IOException {
    String jsCode = "function f() { this.a = 3; }";
    String[] flags = new String[] {
        "--jscomp_warning=globalThis",
        "--jscomp_off=globalThis"
    };

    TestableCommandLineRunner runner = compileWithRunner(flags, jsCode);
    assertTrue(runner.shouldRunCompiler());

    Compiler compiler = runner.getCompiler();
    assertNotNull(compiler);
    assertEquals("Expected 0 errors when off comes after warning", 0, compiler.getErrorCount());
    assertEquals("Expected 0 warnings when off comes after warning", 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testHelpFlagPrintsUsageAndDisablesRun() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--help" },
        new PrintStream(out), new PrintStream(err));

    assertFalse("Runner should not run compiler when --help is present", runner.shouldRunCompiler());
    assertTrue("Usage information must be printed to err", err.toString().contains("--help"));
  }

  @Test(timeout = 4000)
  public void testInvalidCommandLineFlag() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--this_is_an_invalid_flag_xyz" },
        new PrintStream(out), new PrintStream(err));

    assertFalse("Invalid flag should make runner invalid", runner.shouldRunCompiler());
    assertTrue("Error output must contain unrecognized flag notice", err.toString().length() > 0);
  }

  @Test(timeout = 4000)
  public void testFlagFileValid() throws IOException {
    File flagFile = createTempSourceFile("flags", ".txt", "--debug true --compilation_level WHITESPACE_ONLY");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--flagfile=" + flagFile.getAbsolutePath() },
        new PrintStream(out), new PrintStream(err));

    assertTrue("Runner should be valid with proper flagfile", runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testFlagFileNestedFlagFileRejection() throws IOException {
    File flagFile = createTempSourceFile("flags_nested", ".txt", "--flagfile other.txt");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--flagfile=" + flagFile.getAbsolutePath() },
        new PrintStream(out), new PrintStream(err));

    assertFalse("Nested flagfile should be rejected", runner.shouldRunCompiler());
    assertTrue("Error should mention cannot contain --flagfile option",
        err.toString().contains("Arguments in the file cannot contain --flagfile option."));
  }

  @Test(timeout = 4000)
  public void testFlagFileNonExistent() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--flagfile=non_existent_file_path_12345.txt" },
        new PrintStream(out), new PrintStream(err));

    assertFalse("Non-existent flagfile should invalidate config", runner.shouldRunCompiler());
    assertTrue("Error message should report read error", err.toString().contains("read error."));
  }

  @Test(timeout = 4000)
  public void testVersionFlagPrintsVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] { "--version" },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    String errString = err.toString();
    assertTrue("Version output should contain Closure Compiler header",
        errString.contains("Closure Compiler (http://code.google.com/closure/compiler)"));
    assertTrue("Version output should contain Version label",
        errString.contains("Version:"));
  }

  // =========================================================================
  // Partition E: Resource & Externs Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetDefaultExternsDirectly() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull("Default externs must not be null", defaultExterns);
    assertFalse("Default externs must not be empty", defaultExterns.isEmpty());
    assertEquals("First extern should be es3.js", "externs.zip//es3.js", defaultExterns.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testCreateExternsWithOnlyCustomExterns() throws Exception {
    File customExtern = createTempSourceFile("custom_extern", ".js", "var externalVar;");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {
            "--use_only_custom_externs=true",
            "--externs=" + customExtern.getAbsolutePath()
        },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    List<JSSourceFile> externs = runner.getExternsForTesting();
    assertEquals("Should only contain the custom extern file", 1, externs.size());
    assertEquals(customExtern.getAbsolutePath(), externs.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testCreateExternsDefaultMergedWithCustom() throws Exception {
    File customExtern = createTempSourceFile("custom_extern2", ".js", "var myVar;");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[] {
            "--use_only_custom_externs=false",
            "--externs=" + customExtern.getAbsolutePath()
        },
        new PrintStream(out), new PrintStream(err));

    assertTrue(runner.shouldRunCompiler());
    List<JSSourceFile> externs = runner.getExternsForTesting();
    assertTrue("Externs should contain defaults plus custom", externs.size() > 1);
    assertEquals("Last extern should be the custom extern file",
        customExtern.getAbsolutePath(), externs.get(externs.size() - 1).getName());
  }
}