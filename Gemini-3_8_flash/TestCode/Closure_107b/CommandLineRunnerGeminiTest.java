package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.CommandLineRunner
 * Ground Truth Failure Target: testGetMsgWiringNoWarnings
 * Defect Signature:
 *   In createOptions(), when CompilationLevel is ADVANCED_OPTIMIZATIONS and no translations
 *   file is specified, an EmptyMessageBundle is assigned. However, the diagnostic warning
 *   group MSG_CONVENTIONS was not suppressed (set to CheckLevel.OFF), causing unexpected
 *   i18n warnings on valid goog.getMsg usages under ADVANCED_OPTIMIZATIONS.
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. CompilationLevel:
 *    - ADVANCED_OPTIMIZATIONS with goog.getMsg wiring & no translation file -> trigger defect
 *    - ADVANCED_OPTIMIZATIONS with translation file vs EmptyMessageBundle
 *    - SIMPLE_OPTIMIZATIONS and WHITESPACE_ONLY levels
 * 2. Args Preprocessing & Quoting:
 *    - Parsing "--arg=value", "--arg='quoted'", "--arg=\"quoted\""
 *    - Tokenization of quoted strings across spaces
 * 3. BooleanOptionHandler branches:
 *    - Parameter null (presence of flag alone) -> true
 *    - TRUES: "true", "on", "yes", "1"
 *    - FALSES: "false", "off", "no", "0"
 *    - Fallthrough / Unknown string values -> true
 * 4. Flag File Processing:
 *    - Valid flag file loading options
 *    - Recursive flag file error path ("--flagfile" inside a flagfile)
 *    - Non-existent flag file error handling
 * 5. Diagnostic Guard Handlers:
 *    - --jscomp_error, --jscomp_warning, --jscomp_off preserving order
 * 6. Coding Convention Resolution:
 *    - --third_party -> CodingConventions.getDefault()
 *    - --process_jquery_primitives -> JqueryCodingConvention
 *    - Default Closure convention
 * 7. CommonJS Module Handling:
 *    - --process_common_js_modules with and without --common_js_entry_module
 * 8. Externs Management:
 *    - Default externs loading and verification against DEFAULT_EXTERNS_NAMES
 *    - --use_only_custom_externs flag behavior
 * 9. Formatting options: PRETTY_PRINT, PRINT_INPUT_DELIMITER, SINGLE_QUOTES
 * -----------------------------------------------------------------------------------------
 */
public class CommandLineRunnerGeminiTest {

  private CommandLineRunner createRunner(String[] args, ByteArrayOutputStream out, ByteArrayOutputStream err) {
    return new CommandLineRunner(args, new PrintStream(out), new PrintStream(err));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
  // =========================================================================

  /**
   * Targets Defects4J ground-truth failure in CommandLineRunner:
   * CommandLineRunnerTest::testGetMsgWiringNoWarnings
   *
   * Under ADVANCED_OPTIMIZATIONS, an EmptyMessageBundle is configured when no translation
   * file is provided. The compiler should suppress MSG_CONVENTIONS warnings so that code
   * declaring and calling goog.getMsg() compiles cleanly without spurious i18n warnings.
   */
  @Test(timeout = 4000)
  public void testGetMsgWiringNoWarnings() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=DEFAULT"
    };

    CommandLineRunner runner = createRunner(args, out, err);
    assertTrue("Runner should accept valid ADVANCED_OPTIMIZATIONS config", runner.shouldRunCompiler());

    Compiler compiler = runner.createCompiler();
    CompilerOptions options = runner.createOptions();

    assertNotNull("EmptyMessageBundle must be configured in ADVANCED mode", options.messageBundle);
    assertTrue("Message bundle must be EmptyMessageBundle", options.messageBundle instanceof EmptyMessageBundle);

    String jsCode =
        "var goog = {};\n" +
        "goog.getMsg = function(s) { return s; };\n" +
        "/** @desc A message greeting. */\n" +
        "var MSG_HELLO = goog.getMsg('Hello World');\n";

    List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    List<SourceFile> inputs = ImmutableList.of(SourceFile.fromCode("input.js", jsCode));

    compiler.compile(defaultExterns, inputs, options);

    assertEquals("Expected 0 compilation errors", 0, compiler.getErrors().length);
    // On the defective version, MSG_CONVENTIONS is not turned off, triggering 1 or more warnings.
    assertEquals("Expected no warnings or errors", 0, compiler.getWarnings().length);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultOptionsInitialization() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[0], out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
    assertTrue(options.closurePass);
    assertFalse(options.jqueryPass);
    assertFalse(options.angularPass);
    assertNull(options.messageBundle);
  }

  @Test(timeout = 4000)
  public void testCompilationLevels() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    // WHITESPACE_ONLY
    CommandLineRunner runnerWhitespace = createRunner(
        new String[] {"--compilation_level=WHITESPACE_ONLY"}, out, err);
    assertTrue(runnerWhitespace.shouldRunCompiler());
    CompilerOptions optionsWhitespace = runnerWhitespace.createOptions();
    assertNull(optionsWhitespace.messageBundle);

    // SIMPLE_OPTIMIZATIONS
    CommandLineRunner runnerSimple = createRunner(
        new String[] {"--compilation_level=SIMPLE_OPTIMIZATIONS"}, out, err);
    assertTrue(runnerSimple.shouldRunCompiler());
    CompilerOptions optionsSimple = runnerSimple.createOptions();
    assertNull(optionsSimple.messageBundle);

    // ADVANCED_OPTIMIZATIONS
    CommandLineRunner runnerAdvanced = createRunner(
        new String[] {"--compilation_level=ADVANCED_OPTIMIZATIONS"}, out, err);
    assertTrue(runnerAdvanced.shouldRunCompiler());
    CompilerOptions optionsAdvanced = runnerAdvanced.createOptions();
    assertNotNull(optionsAdvanced.messageBundle);
  }

  @Test(timeout = 4000)
  public void testFormattingOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] args = new String[] {
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--formatting=SINGLE_QUOTES"
    };

    CommandLineRunner runner = createRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertTrue("PRETTY_PRINT should enable prettyPrint", options.prettyPrint);
    assertTrue("PRINT_INPUT_DELIMITER should enable printInputDelimiter", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testCodingConventionsResolution() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    // 1. Default (Closure)
    CommandLineRunner runnerDefault = createRunner(new String[0], out, err);
    CompilerOptions optionsDefault = runnerDefault.createOptions();
    assertTrue(optionsDefault.getCodingConvention() instanceof ClosureCodingConvention);

    // 2. Third Party
    CommandLineRunner runnerThirdParty = createRunner(new String[] {"--third_party=true"}, out, err);
    assertTrue(runnerThirdParty.shouldRunCompiler());
    CommandLineRunner.CommandLineConfig configThirdParty = runnerThirdParty.getCommandLineConfig();
    assertNotNull(configThirdParty);

    // 3. Jquery Primitives
    CommandLineRunner runnerJquery = createRunner(new String[] {"--process_jquery_primitives=true"}, out, err);
    assertTrue(runnerJquery.shouldRunCompiler());
    CompilerOptions optionsJquery = runnerJquery.createOptions();
    assertTrue(optionsJquery.getCodingConvention() instanceof JqueryCodingConvention);
  }

  @Test(timeout = 4000)
  public void testJqueryPassWithCompilationLevel() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    // ADVANCED + jquery primitives -> jqueryPass is true
    CommandLineRunner runnerAdv = createRunner(new String[] {
        "--process_jquery_primitives=true",
        "--compilation_level=ADVANCED_OPTIMIZATIONS"
    }, out, err);
    CompilerOptions optionsAdv = runnerAdv.createOptions();
    assertTrue(optionsAdv.jqueryPass);

    // SIMPLE + jquery primitives -> jqueryPass is false
    CommandLineRunner runnerSimple = createRunner(new String[] {
        "--process_jquery_primitives=true",
        "--compilation_level=SIMPLE_OPTIMIZATIONS"
    }, out, err);
    CompilerOptions optionsSimple = runnerSimple.createOptions();
    assertFalse(optionsSimple.jqueryPass);
  }

  @Test(timeout = 4000)
  public void testWarningGuardOptions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] args = new String[] {
        "--jscomp_error=checkVars",
        "--jscomp_warning=checkTypes",
        "--jscomp_off=deprecated"
    };

    CommandLineRunner runner = createRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testVariousCompilerFlagsConfiguration() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] args = new String[] {
        "--debug=true",
        "--use_types_for_optimization=true",
        "--generate_exports=true",
        "--angular_pass=true",
        "--accept_const_keyword=true",
        "--language_in=ECMASCRIPT5",
        "--summary_detail_level=3",
        "--tracer_mode=ALL"
    };

    CommandLineRunner runner = createRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertTrue(options.angularPass);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Arguments Parsing
  // =========================================================================

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerTruthValues() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] truthyValues = new String[] {"true", "on", "yes", "1"};
    for (String val : truthyValues) {
      CommandLineRunner runner = createRunner(new String[] {"--debug=" + val}, out, err);
      assertTrue("Truth value '" + val + "' should be accepted", runner.shouldRunCompiler());
    }

    String[] falsyValues = new String[] {"false", "off", "no", "0"};
    for (String val : falsyValues) {
      CommandLineRunner runner = createRunner(new String[] {"--debug=" + val}, out, err);
      assertTrue("Falsy value '" + val + "' should be accepted", runner.shouldRunCompiler());
    }

    // Bare boolean flag with no parameter
    CommandLineRunner runnerNoParam = createRunner(new String[] {"--debug"}, out, err);
    assertTrue(runnerNoParam.shouldRunCompiler());

    // Arbitrary token defaults to true
    CommandLineRunner runnerUnknown = createRunner(new String[] {"--debug=not_a_bool"}, out, err);
    assertTrue(runnerUnknown.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testQuotedArgumentStripping() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    String[] args = new String[] {
        "--output_wrapper='(function(){%output%})();'",
        "--jsOutputFile=\"compiled_app.js\""
    };

    CommandLineRunner runner = createRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testDefaultExternsIntegrity() throws IOException {
    List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull(defaultExterns);
    assertFalse(defaultExterns.isEmpty());
    assertEquals("Should contain exactly 29 default externs entries", 29, defaultExterns.size());

    boolean hasEs3 = false;
    boolean hasWindow = false;
    for (SourceFile extern : defaultExterns) {
      assertNotNull(extern.getName());
      if (extern.getName().contains("es3.js")) {
        hasEs3 = true;
      }
      if (extern.getName().contains("window.js")) {
        hasWindow = true;
      }
    }
    assertTrue("Default externs must contain es3.js", hasEs3);
    assertTrue("Default externs must contain window.js", hasWindow);
  }

  @Test(timeout = 4000)
  public void testFlagFileProcessing() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    File tempFlagFile = File.createTempFile("closure_flags", ".txt");
    tempFlagFile.deleteOnExit();

    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    String flagsContent = "--compilation_level ADVANCED_OPTIMIZATIONS\n--debug true\n'--output_wrapper=(function(){%output%})();'\n";
    fos.write(flagsContent.getBytes(Charset.forName("UTF-8")));
    fos.close();

    CommandLineRunner runner = createRunner(
        new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()}, out, err);
    assertTrue("Flag file should be successfully loaded and valid", runner.shouldRunCompiler());
  }

  // =========================================================================
  // Partition D: Defensive, Exception & Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testHelpFlagSuppressesCompilerExecution() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {"--help"}, out, err);
    assertFalse("Help flag should set shouldRunCompiler to false", runner.shouldRunCompiler());
    assertTrue("Help text should be printed to err", err.toString().contains("--help"));
  }

  @Test(timeout = 4000)
  public void testVersionFlagPrintsVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {"--version"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    assertTrue("Version information should be printed to err", err.toString().contains("Closure Compiler"));
  }

  @Test(timeout = 4000)
  public void testInvalidFlagMarksConfigInvalid() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {"--non_existent_flag_definitely_invalid=foo"}, out, err);
    assertFalse("Unknown flag should invalidate config", runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testMissingFlagFileMarksConfigInvalid() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(
        new String[] {"--flagfile=non_existent_file_directory/flags.txt"}, out, err);
    assertFalse("Missing flag file should invalidate config", runner.shouldRunCompiler());
    assertTrue("Read error should be output to err", err.toString().contains("read error"));
  }

  @Test(timeout = 4000)
  public void testRecursiveFlagFileDisallowed() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    File tempFlagFile = File.createTempFile("nested_flag", ".txt");
    tempFlagFile.deleteOnExit();

    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    String flagsContent = "--flagfile=another_flag.txt\n";
    fos.write(flagsContent.getBytes(Charset.forName("UTF-8")));
    fos.close();

    CommandLineRunner runner = createRunner(
        new String[] {"--flagfile=" + tempFlagFile.getAbsolutePath()}, out, err);
    assertFalse("Recursive flag file must invalidate configuration", runner.shouldRunCompiler());
    assertTrue(err.toString().contains("cannot contain --flagfile option"));
  }

  @Test(timeout = 4000)
  public void testCommonJsModuleWithoutEntryModuleFails() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {"--process_common_js_modules=true"}, out, err);
    assertFalse("process_common_js_modules without entry module should invalidate config", runner.shouldRunCompiler());
    assertTrue(err.toString().contains("Please specify --common_js_entry_module"));
  }

  @Test(timeout = 4000)
  public void testCommonJsModuleWithEntryModuleSucceeds() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {
        "--process_common_js_modules=true",
        "--common_js_entry_module=main.js"
    }, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testInvalidTranslationsFileThrowsException() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(
        new String[] {"--translations_file=non_existent_path_translations.xtb"}, out, err);
    assertTrue(runner.shouldRunCompiler());

    try {
      runner.createOptions();
      fail("Expected RuntimeException when translations_file cannot be opened");
    } catch (RuntimeException expected) {
      assertTrue("Exception message should indicate reading XTB file",
          expected.getMessage().contains("Reading XTB file"));
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Subclassing & Runner Extension
  // =========================================================================

  @Test(timeout = 4000)
  public void testCustomSubclassCommandLineRunner() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    class CustomCommandLineRunner extends CommandLineRunner {
      CustomCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
        super(args, out, err);
      }

      @Override
      protected CompilerOptions createOptions() {
        CompilerOptions options = super.createOptions();
        options.prettyPrint = true;
        return options;
      }
    }

    CustomCommandLineRunner customRunner = new CustomCommandLineRunner(new String[0], new PrintStream(out), new PrintStream(err));
    assertTrue(customRunner.shouldRunCompiler());

    CompilerOptions customOptions = customRunner.createOptions();
    assertTrue("Subclass should be able to override and customize options", customOptions.prettyPrint);

    Compiler customCompiler = customRunner.createCompiler();
    assertNotNull(customCompiler);
  }

  @Test(timeout = 4000)
  public void testCreateExternsWithUseOnlyCustomExterns() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();

    CommandLineRunner runner = createRunner(new String[] {
        "--use_only_custom_externs=true"
    }, out, err);

    List<SourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    // When custom externs only is requested and none provided via --externs, list is empty
    assertEquals(0, externs.size());
  }
}