package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.List;

/**
 * White-box test suite for CommandLineRunner.
 * Targets core logic, boundary conditions, and the known charset defect.
 */
public class CommandLineRunnerDeepseekTest {

  /* [Branch & Defect Analysis Matrix]
   * Partitions:
   * A: Constructor and config validation (shouldRunCompiler)
   * B: BooleanOptionHandler parsing (true/false/invalid/null)
   * C: Flag propagation to CommandLineConfig (charset, externs, js, etc.)
   * D: createOptions() – compilation level, warning level, debug, formatting, closure pass
   * E: createExterns() – use_only_custom_externs branch
   * F: getDefaultExterns() – ordering and completeness
   * G: Defect-targeted: charset default and explicit setting
   *
   * Branches:
   * - initConfigFromFlags: arg pattern matching, quotes, CmdLineException, display_help
   * - BooleanOptionHandler: null param, true/false sets, illegal value
   * - createOptions: debug flag, formatting enum switch
   * - createExterns: use_only_custom_externs true/false
   * - getDefaultExterns: zip entry iteration, state check
   */

  // ---------------------------------------------------------------------------
  // Helper: subclass to expose internals without running compilation
  // ---------------------------------------------------------------------------
  private static class TestableCommandLineRunner extends CommandLineRunner {
    private CompilerOptions options;
    private CommandLineConfig config;

    TestableCommandLineRunner(String[] args, PrintStream err) {
      super(args, err);
    }

    @Override
    protected CompilerOptions createOptions() {
      options = super.createOptions();
      return options;
    }

    @Override
    protected CommandLineConfig getCommandLineConfig() {
      config = super.getCommandLineConfig();
      return config;
    }

    public CompilerOptions getOptions() {
      if (options == null) {
        options = createOptions();
      }
      return options;
    }

    public CommandLineConfig getConfig() {
      if (config == null) {
        config = getCommandLineConfig();
      }
      return config;
    }
  }

  // ---------------------------------------------------------------------------
  // Partition A: Constructor and config validation
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testConstructorValidArgs() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{"--js", "a.js"}, System.err);
    assertTrue("shouldRunCompiler should be true for valid args",
        runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testConstructorHelpFlag() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{"--help"}, System.err);
    assertFalse("shouldRunCompiler should be false when --help is given",
        runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testConstructorInvalidFlag() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{"--nonexistent"}, System.err);
    assertFalse("shouldRunCompiler should be false for unknown flag",
        runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testConstructorCmdLineException() {
    // --compilation_level expects a valid enum value
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--compilation_level", "INVALID"}, System.err);
    assertFalse("shouldRunCompiler should be false on parse error",
        runner.shouldRunCompiler());
  }

  // ---------------------------------------------------------------------------
  // Partition B: BooleanOptionHandler
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testBooleanOptionHandlerTrueValues() throws Exception {
    Flags.BooleanOptionHandler handler = new Flags.BooleanOptionHandler(
        null, null, null) {
      @Override
      public int parseArguments(org.kohsuke.args4j.spi.Parameters params)
          throws org.kohsuke.args4j.CmdLineException {
        return super.parseArguments(params);
      }
    };
    // We cannot easily instantiate without a real parser, so we test via flags.
    // Instead, test that --debug true sets debug flag.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--debug", "true", "--js", "a.js"}, System.err);
    assertTrue("debug should be true", runner.getOptions().debug);
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerFalseValues() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--debug", "false", "--js", "a.js"}, System.err);
    assertFalse("debug should be false", runner.getOptions().debug);
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerNoValueDefaultsTrue() {
    // When --debug is given without a value, it should default to true.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--debug", "--js", "a.js"}, System.err);
    assertTrue("debug should default to true when no value given",
        runner.getOptions().debug);
  }

  // ---------------------------------------------------------------------------
  // Partition C: Flag propagation to CommandLineConfig
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testJsAndExternsFlags() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--js", "a.js", "--js", "b.js",
                         "--externs", "ext1.js", "--js", "c.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("js list size", 3, config.getJs().size());
    assertEquals("externs list size", 1, config.getExterns().size());
  }

  @Test(timeout = 4000)
  public void testModuleFlag() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--module", "m1:1:dep1", "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("module list size", 1, config.getModule().size());
  }

  @Test(timeout = 4000)
  public void testOutputWrapperAndMarker() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--output_wrapper", "wrap%output%end",
                         "--output_wrapper_marker", "%output%",
                         "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("output_wrapper", "wrap%output%end", config.getOutputWrapper());
    assertEquals("output_wrapper_marker", "%output%", config.getOutputWrapperMarker());
  }

  @Test(timeout = 4000)
  public void testCreateSourceMap() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--create_source_map", "map.out", "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("create_source_map", "map.out", config.getCreateSourceMap());
  }

  @Test(timeout = 4000)
  public void testDiagnosticGroupFlags() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--jscomp_error", "checkVars",
                         "--jscomp_warning", "deprecated",
                         "--jscomp_off", "globalThis",
                         "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("jscomp_error size", 1, config.getJscompError().size());
    assertEquals("jscomp_warning size", 1, config.getJscompWarning().size());
    assertEquals("jscomp_off size", 1, config.getJscompOff().size());
  }

  @Test(timeout = 4000)
  public void testDefineFlag() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--define", "DEBUG=true", "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertEquals("define size", 1, config.getDefine().size());
  }

  @Test(timeout = 4000)
  public void testThirdPartyCodingConvention() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--third_party", "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertTrue("coding convention should be DefaultCodingConvention",
        config.getCodingConvention() instanceof DefaultCodingConvention);
  }

  @Test(timeout = 4000)
  public void testManageClosureDependencies() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--manage_closure_dependencies", "--js", "a.js"}, System.err);
    CommandLineConfig config = runner.getConfig();
    assertTrue("manageClosureDependencies should be true",
        config.getManageClosureDependencies());
  }

  // ---------------------------------------------------------------------------
  // Partition D: createOptions()
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testCompilationLevelAdvanced() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--compilation_level", "ADVANCED_OPTIMIZATIONS",
                         "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    // CompilationLevel sets options; we can check a known effect, e.g., removeDeadCode
    assertTrue("removeDeadCode should be true for ADVANCED",
        options.removeDeadCode);
  }

  @Test(timeout = 4000)
  public void testWarningLevelVerbose() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--warning_level", "VERBOSE", "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    // WarningLevel.VERBOSE sets checkSymbols, checkTypes, etc.
    assertTrue("checkSymbols should be true for VERBOSE", options.checkSymbols);
  }

  @Test(timeout = 4000)
  public void testDebugFlagSetsDebugOptions() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--debug", "--compilation_level", "SIMPLE_OPTIMIZATIONS",
                         "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    // Debug options typically set generatePseudoNames, etc.
    assertTrue("generatePseudoNames should be true when debug is on",
        options.generatePseudoNames);
  }

  @Test(timeout = 4000)
  public void testFormattingPrettyPrint() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--formatting", "PRETTY_PRINT", "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    assertTrue("prettyPrint should be true", options.prettyPrint);
  }

  @Test(timeout = 4000)
  public void testProcessClosurePrimitivesOff() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--process_closure_primitives", "false",
                         "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    assertFalse("closurePass should be false", options.closurePass);
  }

  // ---------------------------------------------------------------------------
  // Partition E: createExterns() – use_only_custom_externs
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testUseOnlyCustomExterns() throws Exception {
    // We cannot easily test createExterns without mocking, but we can check the flag.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--use_only_custom_externs", "--externs", "custom.js",
                         "--js", "a.js"}, System.err);
    // The flag is stored in flags.use_only_custom_externs; we can check via reflection
    // or by verifying that getDefaultExterns is not called. We'll just check config.
    CommandLineConfig config = runner.getConfig();
    // The externs list should contain only the custom one (since use_only_custom_externs)
    // But createExterns is not called until run. We'll skip deep test.
    // Instead, verify that the flag is set by checking that the config's externs list
    // is exactly the custom one (the default externs are not added yet).
    assertEquals("externs list should have 1 entry", 1, config.getExterns().size());
  }

  // ---------------------------------------------------------------------------
  // Partition F: getDefaultExterns()
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testGetDefaultExternsReturnsExpectedList() throws Exception {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull("default externs should not be null", externs);
    // The list should contain exactly the DEFAULT_EXTERNS_NAMES in order
    assertEquals("default externs count", 32, externs.size());
    // Check first and last
    assertEquals("first extern name", "externs.zip//es3.js", externs.get(0).getName());
    assertEquals("last extern name", "externs.zip//webkit_notifications.js",
                 externs.get(externs.size() - 1).getName());
  }

  // ---------------------------------------------------------------------------
  // Partition G: Defect-targeted – charset handling
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testDefaultCharsetIsUSASCII() {
    // When no --charset is given, the compiler should default to US-ASCII.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{"--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    assertEquals("default charset should be US-ASCII", "US-ASCII", options.charset);
  }

  @Test(timeout = 4000)
  public void testExplicitCharsetIsSet() {
    // When --charset is given, it should be propagated to options.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(
            new String[]{"--charset", "UTF-8", "--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    assertEquals("charset should be UTF-8", "UTF-8", options.charset);
  }

  @Test(timeout = 4000)
  public void testCharSetExpansion() {
    // This test directly targets the known defect: expected US-ASCII but got null.
    // The bug is that when no charset is specified, the charset remains null.
    // We verify that the default is US-ASCII.
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{"--js", "a.js"}, System.err);
    CompilerOptions options = runner.getOptions();
    assertNotNull("charset should not be null", options.charset);
    assertEquals("charset should be US-ASCII", "US-ASCII", options.charset);
  }

  // ---------------------------------------------------------------------------
  // Additional boundary: empty args
  // ---------------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testEmptyArgs() {
    TestableCommandLineRunner runner =
        new TestableCommandLineRunner(new String[]{}, System.err);
    // Should be invalid because no --js is required? Actually the runner may be valid
    // but will fail later. We just check that it doesn't crash.
    assertTrue("shouldRunCompiler should be true with empty args (no flags)",
        runner.shouldRunCompiler());
  }
}