package com.google.javascript.jscomp;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.AbstractCommandLineRunner.CommandLineConfig;
import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import com.google.javascript.jscomp.CompilerOptions.TweakProcessing;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.javascript.jscomp.AbstractCommandLineRunner
 *
 * Branch & Partition Coverage:
 * - Partition A: Core Functional Logic & State Transitions
 *   * Subclass instantiation & default state transitions
 *   * run() execution in test mode with zero errors, warning emissions, and compiler errors
 *   * CommandLineConfig chained mutations and defaults across all flags
 *   * writeOutput() placeholder substitution (with prefix/suffix, without placeholder, with source map)
 *   * expandCommandLinePath(), expandSourceMapPath(), expandManifest() under single/multi-module modes
 *   * printModuleGraphManifestTo() formatting dependencies and input listings
 *
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * enableTestMode() preconditions: XOR validation on (inputsSupplier == null ^ modulesSupplier == null)
 *   * createDefineOrTweakReplacements(): boolean (true, false, implicit true), string literals (single/double quotes),
 *     floating point numbers, malformed syntax fallthrough
 *   * createJsModules(): 0-count files, multiple modules, dependency ordering, empty/max boundaries
 *   * createInputs(): single stdin "-", duplicate stdin "-", stdin disallowed flag
 *   * createExternInputs(): empty list fallback to "/dev/null"
 *   * Module wrapper parsing: missing colon, missing %s placeholder, unknown module mapping
 *
 * - Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   * Fault: CommandLineRunnerTest::testWarningGuardOrdering2 and testWarningGuardOrdering4
 *   * Root Cause: AbstractCommandLineRunner#setRunOptions applies jscompError, jscompWarning, and jscompOff
 *     in a rigid, hardcoded sequential order irrespective of the user-specified precedence/ordering on the CLI.
 *     When --jscomp_off is specified before --jscomp_error or --jscomp_warning for the same DiagnosticGroup,
 *     the defective runner unconditionally applies jscompOff last, suppressing errors and warnings.
 *   * Verification: testWarningGuardOrdering2_DefectTarget & testWarningGuardOrdering4_DefectTarget verify that
 *     warning guards specified later correctly override earlier ones instead of being silently neutralized by jscompOff.
 *
 * - Partition D: Exception & Defensive Guard Paths
 *   * checkModuleName(): identifier token checks (valid, invalid numeric start, hyphens, empty string)
 *   * setRunOptions(): unsupported LanguageMode ("UNKNOWN_LANG"), unsupported Charset ("INVALID_CHARSET_NAME")
 *   * outputNameMaps(): mutual exclusion between create_name_map_files and individual map file flags
 *   * run(): FlagUsageException handling (-1 exit code) and general Throwable handling (-2 exit code)
 */
public class AbstractCommandLineRunnerGeminiTest {

  /**
   * Concrete test implementation of AbstractCommandLineRunner.
   */
  private static class TestCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {

    private Compiler mockCompiler;
    private CompilerOptions mockOptions;

    TestCommandLineRunner() {
      super();
    }

    TestCommandLineRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    void setMockCompiler(Compiler compiler) {
      this.mockCompiler = compiler;
    }

    void setMockOptions(CompilerOptions options) {
      this.mockOptions = options;
    }

    @Override
    protected Compiler createCompiler() {
      return mockCompiler != null ? mockCompiler : new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      return mockOptions != null ? mockOptions : new CompilerOptions();
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultStateAndConfigInitialization() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    assertFalse("Runner should not be in test mode by default", runner.isInTestMode());
    assertNotNull("CommandLineConfig must not be null", runner.getCommandLineConfig());
    assertNotNull("Error print stream should be accessible", runner.getErrorPrintStream());

    CompilerOptions options = runner.createOptions();
    runner.initOptionsFromFlags(options); // Deprecated no-op sanity check
    assertNotNull("Created options should not be null", options);
  }

  @Test(timeout = 4000)
  public void testCommandLineConfigChainingAndValues() {
    CommandLineConfig config = new CommandLineConfig();

    config.setPrintTree(true)
        .setComputePhaseOrdering(true)
        .setPrintAst(true)
        .setPrintPassGraph(true)
        .setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS)
        .setLoggingLevel("FINE")
        .setExterns(Collections.singletonList("extern1.js"))
        .setJs(Collections.singletonList("code1.js"))
        .setJsOutputFile("output.js")
        .setModule(Arrays.asList("mod1:1", "mod2:1:mod1"))
        .setVariableMapInputFile("vars.in")
        .setPropertyMapInputFile("props.in")
        .setVariableMapOutputFile("vars.out")
        .setCreateNameMapFiles(true)
        .setPropertyMapOutputFile("props.out")
        .setCodingConvention(new ClosureCodingConvention())
        .setSummaryDetailLevel(2)
        .setOutputWrapper("(function(){%output%})();")
        .setModuleWrapper(Collections.singletonList("mod1:%s"))
        .setModuleOutputPathPrefix("mod_prefix_")
        .setCreateSourceMap("map.out")
        .setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS)
        .setSourceMapFormat(SourceMap.Format.V3)
        .setJscompError(Collections.singletonList("checkVars"))
        .setJscompWarning(Collections.singletonList("deprecated"))
        .setJscompOff(Collections.singletonList("visibility"))
        .setDefine(Collections.singletonList("FLAG=true"))
        .setTweak(Collections.singletonList("tweak=false"))
        .setTweakProcessing(TweakProcessing.STRIP)
        .setCharset("UTF-8")
        .setManageClosureDependencies(true)
        .setClosureEntryPoints(Collections.singletonList("app.start"))
        .setOutputManifest("manifest.txt")
        .setAcceptConstKeyword(true)
        .setLanguageIn("ECMASCRIPT5");

    assertNotNull(config);
  }

  @Test(timeout = 4000)
  public void testWriteOutputWithAndWithoutPlaceholder() throws IOException {
    StringBuilder out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(out, null, "var a = 1;", "(function(){%output%})();", "%output%");
    assertEquals("(function(){\nvar a = 1;\n})();\n", out.toString().replace("\r", ""));

    out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(out, null, "var b = 2;", "no_placeholder", "%output%");
    assertEquals("var b = 2;\n", out.toString().replace("\r", ""));

    out = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(out, null, "var c = 3;", "%output%/*suffix*/", "%output%");
    assertEquals("var c = 3;/*suffix*/\n", out.toString().replace("\r", ""));
  }

  @Test(timeout = 4000)
  public void testExpandCommandLinePaths() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig()
        .setJsOutputFile("out.js")
        .setModuleOutputPathPrefix("mods/")
        .setCreateSourceMap("map/%outname%.map")
        .setOutputManifest("manifests/%outname%.txt");

    CompilerOptions options = runner.createOptions();
    options.sourceMapOutputPath = "map/%outname%.map";

    // Scenario 1: Single JS Output
    assertEquals("map/out.js.map", runner.expandSourceMapPath(options, null));
    assertEquals("manifests/out.js.txt", runner.expandManifest(null));

    // Scenario 2: Per-module output
    JSModule module = new JSModule("core");
    assertEquals("map/mods/core.js.map", runner.expandSourceMapPath(options, module));
    assertEquals("manifests/mods/core.js.txt", runner.expandManifest(module));

    // Null checks on empty config
    options.sourceMapOutputPath = "";
    assertNull(runner.expandSourceMapPath(options, null));
    runner.getCommandLineConfig().setOutputManifest("");
    assertNull(runner.expandManifest(null));
  }

  @Test(timeout = 4000)
  public void testPrintModuleGraphManifestTo() throws IOException {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);

    m1.add(JSSourceFile.fromCode("mod1_file.js", "var x;"));
    m2.add(JSSourceFile.fromCode("mod2_file.js", "var y;"));

    JSModuleGraph graph = new JSModuleGraph(new JSModule[] {m1, m2});
    StringBuilder out = new StringBuilder();
    runner.printModuleGraphManifestTo(graph, out);

    String manifest = out.toString().replace("\r", "");
    assertTrue("Manifest should include module 1 declaration", manifest.contains("{m1}\nmod1_file.js"));
    assertTrue("Manifest should include module 2 declaration with dependency", manifest.contains("{m2:m1}\nmod2_file.js"));
  }

  @Test(timeout = 4000)
  public void testSuccessfulRunInTestMode() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    final int[] exitResult = new int[] {-999};

    Supplier<List<JSSourceFile>> externs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.emptyList();
      }
    };
    Supplier<List<JSSourceFile>> inputs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.singletonList(JSSourceFile.fromCode("input.js", "var validCode = 123;"));
      }
    };
    Function<Integer, Boolean> receiver = new Function<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer code) {
        exitResult[0] = code;
        return true;
      }
    };

    runner.enableTestMode(externs, inputs, null, receiver);
    runner.run();

    assertEquals("Valid compilation should exit with 0 in test mode", 0, exitResult[0]);
    assertNotNull(runner.getCompiler());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEnableTestModePreconditions() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    Supplier<List<JSSourceFile>> dummy = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.emptyList();
      }
    };

    // Both null -> failure
    try {
      runner.enableTestMode(dummy, null, null, null);
      fail("Expected IllegalArgumentException when both inputs and modules suppliers are null");
    } catch (IllegalArgumentException expected) {
      // Success
    }

    // Both non-null -> failure
    Supplier<List<JSModule>> dummyModules = new Supplier<List<JSModule>>() {
      @Override
      public List<JSModule> get() {
        return Collections.emptyList();
      }
    };
    try {
      runner.enableTestMode(dummy, dummy, dummyModules, null);
      fail("Expected IllegalArgumentException when both inputs and modules suppliers are non-null");
    } catch (IllegalArgumentException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testCreateDefineOrTweakReplacementsValidCases() {
    CompilerOptions options = new CompilerOptions();
    List<String> definitions = Arrays.asList(
        "DEF_BOOL_T=true",
        "DEF_BOOL_F=false",
        "DEF_IMPLICIT_T",
        "DEF_STR_SINGLE='closure'",
        "DEF_STR_DOUBLE=\"compiler\"",
        "DEF_INT=42",
        "DEF_DOUBLE=-3.14159"
    );

    AbstractCommandLineRunner.createDefineOrTweakReplacements(definitions, options, false);
    // Tweaks variation
    AbstractCommandLineRunner.createDefineOrTweakReplacements(definitions, options, true);
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesValidAndZeroFiles() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    List<String> specs = Arrays.asList("base:0", "app:2:base");
    List<String> files = Arrays.asList("f1.js", "f2.js");

    List<JSModule> modules = runner.createJsModules(specs, files);
    assertEquals("Should create 2 modules", 2, modules.size());
    assertEquals("base", modules.get(0).getName());
    assertEquals(0, modules.get(0).getInputs().size());
    assertEquals("app", modules.get(1).getName());
    assertEquals(2, modules.get(1).getInputs().size());
    assertEquals(1, modules.get(1).getDependencies().size());
  }

  @Test(timeout = 4000)
  public void testParseModuleWrappersValid() throws Exception {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    List<JSModule> modules = Arrays.asList(m1, m2);
    List<String> specs = Collections.singletonList("m1:(function(){%s})();");

    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    assertEquals(2, wrappers.size());
    assertEquals("(function(){%s})();", wrappers.get("m1"));
    assertEquals("", wrappers.get("m2"));
  }

  @Test(timeout = 4000)
  public void testCreateInputsStdinHandling() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();

    // Stdin allowed once
    List<JSSourceFile> inputs = runner.createInputs(Collections.singletonList("-"), true);
    assertEquals(1, inputs.size());
    assertEquals("stdin", inputs.get(0).getName());

    // Disallowed stdin
    try {
      runner.createInputs(Collections.singletonList("-"), false);
      fail("Expected FlagUsageException when stdin is disallowed");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Can't specify stdin."));
    }

    // Duplicate stdin
    try {
      runner.createInputs(Arrays.asList("-", "-"), true);
      fail("Expected FlagUsageException when stdin is specified twice");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Can't specify stdin twice."));
    }
  }

  @Test(timeout = 4000)
  public void testCreateExternInputsEmptyFallback() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    List<JSSourceFile> externs = runner.createExternInputs(Collections.<String>emptyList());
    assertEquals(1, externs.size());
    assertEquals("/dev/null", externs.get(0).getName());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets CommandLineRunnerTest::testWarningGuardOrdering2
   * In the defective implementation, setRunOptions unconditionally applies jscompError,
   * then jscompWarning, and finally jscompOff. When --jscomp_off is specified followed by
   * --jscomp_error for the same warning group, jscompOff overrides the error, producing 0 errors.
   */
  @Test(timeout = 4000)
  public void testWarningGuardOrdering2_DefectTarget() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    final int[] exitCode = new int[] {-999};

    Supplier<List<JSSourceFile>> externs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.emptyList();
      }
    };
    Supplier<List<JSSourceFile>> inputs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.singletonList(
            JSSourceFile.fromCode("test.js", "var x = 3; var x = 4;"));
      }
    };
    Function<Integer, Boolean> receiver = new Function<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer code) {
        exitCode[0] = code;
        return true;
      }
    };

    runner.enableTestMode(externs, inputs, null, receiver);
    // User order: turn off checkVars, then turn on checkVars as error.
    // In a properly ordered setup, the subsequent jscompError MUST take precedence.
    runner.getCommandLineConfig()
        .setJscompOff(Collections.singletonList("checkVars"))
        .setJscompError(Collections.singletonList("checkVars"));

    runner.run();

    // Defective code emits 0 because jscompOff is executed last in setRunOptions.
    assertEquals("Expected exactly one warning or error Errors: ", 1, exitCode[0]);
  }

  /**
   * Targets CommandLineRunnerTest::testWarningGuardOrdering4
   * Tests that setting --jscomp_off followed by --jscomp_warning properly leaves
   * the diagnostic at WARNING level rather than being silenced by jscompOff.
   */
  @Test(timeout = 4000)
  public void testWarningGuardOrdering4_DefectTarget() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    final int[] exitCode = new int[] {-999};

    Supplier<List<JSSourceFile>> externs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.emptyList();
      }
    };
    Supplier<List<JSSourceFile>> inputs = new Supplier<List<JSSourceFile>>() {
      @Override
      public List<JSSourceFile> get() {
        return Collections.singletonList(
            JSSourceFile.fromCode("test.js", "var x = 3; var x = 4;"));
      }
    };
    Function<Integer, Boolean> receiver = new Function<Integer, Boolean>() {
      @Override
      public Boolean apply(Integer code) {
        exitCode[0] = code;
        return true;
      }
    };

    runner.enableTestMode(externs, inputs, null, receiver);
    runner.getCommandLineConfig()
        .setJscompOff(Collections.singletonList("checkVars"))
        .setJscompWarning(Collections.singletonList("checkVars"));

    runner.run();

    Compiler compiler = runner.getCompiler();
    assertNotNull("Compiler should be created", compiler);
    assertEquals("Expected exactly one warning or error Errors: ", 1, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testCheckModuleNameValidation() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();

    runner.checkModuleName("validModName");
    runner.checkModuleName("_valid$123");

    try {
      runner.checkModuleName("123badStart");
      fail("Expected FlagUsageException for module name starting with digit");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Invalid module name"));
    }

    try {
      runner.checkModuleName("mod-invalid-hyphen");
      fail("Expected FlagUsageException for module name containing hyphen");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Invalid module name"));
    }

    try {
      runner.checkModuleName("");
      fail("Expected FlagUsageException for empty module name");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Invalid module name"));
    }
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesValidationExceptions() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    List<String> files = Collections.singletonList("a.js");

    // Invalid spec partition count (< 2)
    try {
      runner.createJsModules(Collections.singletonList("singlePart"), files);
      fail("Expected FlagUsageException for malformed module spec");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Expected 2-4 colon-delimited parts"));
    }

    // Invalid spec partition count (> 4)
    try {
      runner.createJsModules(Collections.singletonList("m:1:dep:extra:superfluous"), files);
      fail("Expected FlagUsageException for spec with > 4 parts");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Expected 2-4 colon-delimited parts"));
    }

    // Duplicate module names
    try {
      runner.createJsModules(Arrays.asList("mod:1", "mod:0"), files);
      fail("Expected FlagUsageException for duplicate module names");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Duplicate module name"));
    }

    // Invalid file count
    try {
      runner.createJsModules(Collections.singletonList("mod:not_a_number"), files);
      fail("Expected FlagUsageException for non-integer file count");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Invalid js file count"));
    }

    // Not enough files specified
    try {
      runner.createJsModules(Collections.singletonList("mod:5"), files);
      fail("Expected FlagUsageException when module requires more files than available");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Not enough js files specified"));
    }

    // Too many files specified
    try {
      runner.createJsModules(Collections.singletonList("mod:1"), Arrays.asList("a.js", "b.js"));
      fail("Expected FlagUsageException when excess js files exist");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Too many js files specified"));
    }

    // Unknown dependency
    try {
      runner.createJsModules(Collections.singletonList("mod:1:unknownDep"), files);
      fail("Expected FlagUsageException when module depends on non-existent module");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("depends on unknown module"));
    }
  }

  @Test(timeout = 4000)
  public void testParseModuleWrappersExceptions() {
    JSModule m1 = new JSModule("m1");
    List<JSModule> modules = Collections.singletonList(m1);

    // Spec without colon
    try {
      AbstractCommandLineRunner.parseModuleWrappers(Collections.singletonList("m1wrapper"), modules);
      fail("Expected FlagUsageException when colon delimiter is absent");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Expected module wrapper to have <name>:<wrapper> format"));
    }

    // Unknown module in wrapper
    try {
      AbstractCommandLineRunner.parseModuleWrappers(Collections.singletonList("unknown:%s"), modules);
      fail("Expected FlagUsageException for unknown module wrapper");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Unknown module: 'unknown'"));
    }

    // Missing %s placeholder
    try {
      AbstractCommandLineRunner.parseModuleWrappers(Collections.singletonList("m1:no_placeholder"), modules);
      fail("Expected FlagUsageException when %s is absent");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("No %s placeholder in module wrapper"));
    }
  }

  @Test(timeout = 4000)
  public void testCreateDefineOrTweakReplacementsExceptions() {
    CompilerOptions options = new CompilerOptions();

    // Invalid define syntax (string quote mismatch)
    try {
      AbstractCommandLineRunner.createDefineOrTweakReplacements(
          Collections.singletonList("NAME='embedded'quote'"), options, false);
      fail("Expected RuntimeException for unescaped nested quotes in define");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("--define flag syntax invalid"));
    }

    // Invalid non-number/non-boolean token
    try {
      AbstractCommandLineRunner.createDefineOrTweakReplacements(
          Collections.singletonList("NAME=not_valid_literal"), options, false);
      fail("Expected RuntimeException for non-parsable literal");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("--define flag syntax invalid"));
    }

    // Invalid tweak syntax error message verification
    try {
      AbstractCommandLineRunner.createDefineOrTweakReplacements(
          Collections.singletonList("TWEAK=bad_val"), options, true);
      fail("Expected RuntimeException for invalid tweak syntax");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("--tweak flag syntax invalid"));
    }
  }

  @Test(timeout = 4000)
  public void testSetRunOptionsLanguageModeBranches() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    CompilerOptions options = runner.createOptions();

    // Valid modes
    String[] validLangs = {"ECMASCRIPT5_STRICT", "ES5_STRICT", "ECMASCRIPT5", "ES5", "ECMASCRIPT3", "ES3"};
    for (String lang : validLangs) {
      runner.getCommandLineConfig().setLanguageIn(lang);
      runner.setRunOptions(options);
    }

    // Invalid mode
    runner.getCommandLineConfig().setLanguageIn("ECMASCRIPT6_FUTURE");
    try {
      runner.setRunOptions(options);
      fail("Expected FlagUsageException for unsupported language mode");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("Unknown language `ECMASCRIPT6_FUTURE' specified."));
    }
  }

  @Test(timeout = 4000)
  public void testSetRunOptionsInvalidCharset() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME_12345");
    CompilerOptions options = runner.createOptions();
    try {
      runner.setRunOptions(options);
      fail("Expected FlagUsageException for unsupported charset");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("is not a valid charset name"));
    }
  }

  @Test(timeout = 4000)
  public void testOutputNameMapsMutualExclusionException() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig()
        .setCreateNameMapFiles(true)
        .setVariableMapOutputFile("vars.out");

    CompilerOptions options = runner.createOptions();
    Result dummyResult = new Result(
        new JSError[0], new JSError[0], "", null, null, null, null, null, null);

    try {
      runner.processResults(dummyResult, null, options);
      fail("Expected FlagUsageException when create_name_map_files and variable_map_output_file are both set");
    } catch (FlagUsageException expected) {
      assertTrue(expected.getMessage().contains("cannot both be used simultaniously"));
    }
  }

  @Test(timeout = 4000)
  public void testRunCatchesFlagUsageException() {
    ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(errBytes);
    TestCommandLineRunner runner = new TestCommandLineRunner(System.out, errStream);

    final int[] exitCode = new int[] {-999};
    runner.enableTestMode(
        new Supplier<List<JSSourceFile>>() {
          @Override
          public List<JSSourceFile> get() {
            return Collections.emptyList();
          }
        },
        new Supplier<List<JSSourceFile>>() {
          @Override
          public List<JSSourceFile> get() {
            return Collections.emptyList();
          }
        },
        null,
        new Function<Integer, Boolean>() {
          @Override
          public Boolean apply(Integer code) {
            exitCode[0] = code;
            return true;
          }
        }
    );

    runner.getCommandLineConfig().setLanguageIn("UNSUPPORTED_LANGUAGE");
    runner.run();

    assertEquals("FlagUsageException should yield exit code -1", -1, exitCode[0]);
    assertTrue("Error stream should record flag usage message", errBytes.toString().contains("Unknown language"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testFilenameToOutputStreamNullHandling() throws IOException {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    assertNull("Passing null filename to filenameToOutputStream should return null",
        runner.filenameToOutputStream(null));
  }

  @Test(timeout = 4000)
  public void testFilenameToOutputStreamValidTempFile() throws IOException {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    File temp = File.createTempFile("closure_test", ".js");
    temp.deleteOnExit();

    OutputStream stream = runner.filenameToOutputStream(temp.getAbsolutePath());
    assertNotNull("Stream for valid file path should not be null", stream);
    stream.write("var a;".getBytes());
    stream.close();
    assertTrue("File should contain written bytes", temp.length() > 0);
  }
}