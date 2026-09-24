/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =================================================================================================
 * Target Class: com.google.javascript.jscomp.CommandLineRunner
 *
 * 1. Branch / Condition Coverage Vectors:
 *   - initConfigFromFlags:
 *       * argPattern matches ("--flag=val") vs doesn't match ("--flag", "posArg")
 *       * quotesPattern matches single quotes ('val'), double quotes ("val") vs unquoted
 *       * isConfigValid: valid arguments -> true, CmdLineException -> false
 *       * flags.display_help: true vs false
 *       * Coding convention selection: flags.third_party (DefaultCodingConvention) vs ClosureCodingConvention
 *   - createOptions:
 *       * compilation_level: WHITESPACE_ONLY, SIMPLE_OPTIMIZATIONS, ADVANCED_OPTIMIZATIONS
 *       * flags.debug: true (apply debug options) vs false
 *       * warning_level: QUIET, DEFAULT, VERBOSE
 *       * formatting: PRETTY_PRINT, PRINT_INPUT_DELIMITER, multiple formatting flags
 *       * process_closure_primitives: true vs false
 *   - createExterns:
 *       * flags.use_only_custom_externs: true (custom only) vs false (default externs + custom)
 *   - getDefaultExterns:
 *       * zip loading, stream reading, size limits, and DEFAULT_EXTERNS_NAMES validation
 *   - BooleanOptionHandler:
 *       * TRUES: "true", "on", "yes", "1" -> setter.addValue(true)
 *       * FALSES: "false", "off", "no", "0" -> setter.addValue(false)
 *       * Invalid value -> throws CmdLineException
 *       * Param without explicit value -> setter.addValue(true)
 *
 * 2. Ground Truth Defect Verification:
 *   - Defects4J defect: CommandLineRunnerTest::testCharSetExpansion
 *     Issue: Default outputCharset should default to "US-ASCII" when --charset is omitted,
 *     as specified in the flag documentation ("accept UTF-8 as input and output US_ASCII").
 *     On defective version, options.outputCharset is left null, causing:
 *     junit.framework.AssertionFailedError: expected:<US-ASCII> but was:<null>
 * =================================================================================================
 */
public class CommandLineRunnerGeminiTest {

  /**
   * Subclass to expose protected methods of CommandLineRunner for white-box inspection.
   */
  private static class SubCommandLineRunner extends CommandLineRunner {
    private Compiler lastCompiler;

    SubCommandLineRunner(String[] args) {
      super(args);
    }

    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    @Override
    protected Compiler createCompiler() {
      lastCompiler = super.createCompiler();
      return lastCompiler;
    }

    @Override
    public CompilerOptions createOptions() {
      return super.createOptions();
    }

    @Override
    public List<JSSourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateOptionsDefault() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {});
    assertTrue("Configuration should be valid for default args", runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull("CompilerOptions must not be null", options);
    assertTrue("process_closure_primitives should default to true", options.closurePass);
    assertFalse("prettyPrint should default to false", options.prettyPrint);
    assertFalse("printInputDelimiter should default to false", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testCompilationLevelWhitespaceOnly() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--compilation_level=WHITESPACE_ONLY"});
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse("WHITESPACE_ONLY should disable closurePass", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testCompilationLevelAdvancedOptimizationsWithDebug() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--compilation_level=ADVANCED_OPTIMIZATIONS",
            "--debug=true"
        });
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    // ADVANCED_OPTIMIZATIONS activates reserveRawExports and variable renaming
    assertTrue(options.reserveRawExports);
  }

  @Test(timeout = 4000)
  public void testWarningLevels() {
    SubCommandLineRunner runnerQuiet = new SubCommandLineRunner(
        new String[] {"--warning_level=QUIET"});
    assertTrue(runnerQuiet.shouldRunCompiler());
    CompilerOptions optionsQuiet = runnerQuiet.createOptions();
    assertNotNull(optionsQuiet);

    SubCommandLineRunner runnerVerbose = new SubCommandLineRunner(
        new String[] {"--warning_level=VERBOSE"});
    assertTrue(runnerVerbose.shouldRunCompiler());
    CompilerOptions optionsVerbose = runnerVerbose.createOptions();
    assertNotNull(optionsVerbose);
    assertTrue("VERBOSE warning level should turn on checkSymbols", optionsVerbose.checkSymbols);
  }

  @Test(timeout = 4000)
  public void testFormattingOptionsPrettyPrintAndDelimiter() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--formatting=PRETTY_PRINT",
            "--formatting=PRINT_INPUT_DELIMITER"
        });
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue("prettyPrint should be enabled", options.prettyPrint);
    assertTrue("printInputDelimiter should be enabled", options.printInputDelimiter);
  }

  @Test(timeout = 4000)
  public void testProcessClosurePrimitivesDisabled() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--process_closure_primitives=false"});
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse("closurePass should be disabled", options.closurePass);
  }

  @Test(timeout = 4000)
  public void testDiagnosticGroupsFlags() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--jscomp_error=checkVars",
            "--jscomp_warning=undefinedVars",
            "--jscomp_off=fileoverviewTags"
        });
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testDefineFlags() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--define=FLAG_BOOL=true",
            "--D=FLAG_NUM=123",
            "-D=FLAG_STR='custom'"
        });
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testThirdPartyCodingConvention() {
    SubCommandLineRunner runnerThirdParty = new SubCommandLineRunner(
        new String[] {"--third_party=true"});
    assertTrue(runnerThirdParty.shouldRunCompiler());
    CompilerOptions optionsThirdParty = runnerThirdParty.createOptions();
    assertNotNull(optionsThirdParty);

    SubCommandLineRunner runnerDefault = new SubCommandLineRunner(
        new String[] {"--third_party=false"});
    assertTrue(runnerDefault.shouldRunCompiler());
    CompilerOptions optionsDefault = runnerDefault.createOptions();
    assertNotNull(optionsDefault);
  }

  @Test(timeout = 4000)
  public void testCreateCompiler() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {}, System.out, new PrintStream(err));
    Compiler compiler = runner.createCompiler();
    assertNotNull("createCompiler() must return a valid Compiler instance", compiler);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyArgs() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[0]);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testQuotesHandlingInArguments() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--js_output_file=\"out_double.js\"",
            "--variable_map_input_file='vars_single.out'",
            "--property_map_input_file=props_plain.out",
            "--output_wrapper=\"(function(){%output%})();\""
        });
    assertTrue("Runner should successfully parse arguments with various quotes", runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerTruesEquivalenceClass() {
    String[] trues = new String[] {"true", "on", "yes", "1"};
    for (String trueVal : trues) {
      SubCommandLineRunner runner = new SubCommandLineRunner(
          new String[] {"--debug=" + trueVal});
      assertTrue("BooleanOptionHandler failed to parse truthy value: " + trueVal, runner.shouldRunCompiler());
    }
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerFalsesEquivalenceClass() {
    String[] falses = new String[] {"false", "off", "no", "0"};
    for (String falseVal : falses) {
      SubCommandLineRunner runner = new SubCommandLineRunner(
          new String[] {"--debug=" + falseVal});
      assertTrue("BooleanOptionHandler failed to parse falsy value: " + falseVal, runner.shouldRunCompiler());
    }
  }

  @Test(timeout = 4000)
  public void testBooleanOptionHandlerFlagWithoutExplicitValue() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {"--debug"});
    assertTrue("BooleanOptionHandler should support bare flag without explicit value", runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testSummaryDetailLevels() {
    int[] levels = new int[] {0, 1, 2, 3};
    for (int lvl : levels) {
      SubCommandLineRunner runner = new SubCommandLineRunner(
          new String[] {"--summary_detail_level=" + lvl});
      assertTrue("Failed for summary_detail_level=" + lvl, runner.shouldRunCompiler());
    }
  }

  @Test(timeout = 4000)
  public void testModuleConfigurationOptions() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--module=mod1:1:",
            "--module_wrapper=mod1:%s",
            "--module_output_path_prefix=./bin/"
        });
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testMapFileConfigurations() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--variable_map_output_file=vars.map",
            "--property_map_output_file=props.map"
        });
    assertTrue(runner.shouldRunCompiler());
  }

  @Test(timeout = 4000)
  public void testDevModeAliases() {
    SubCommandLineRunner runnerDevMode = new SubCommandLineRunner(
        new String[] {"--dev_mode=EVERY_PASS"});
    assertTrue(runnerDevMode.shouldRunCompiler());

    SubCommandLineRunner runnerJscompDevMode = new SubCommandLineRunner(
        new String[] {"--jscomp_dev_mode=START"});
    assertTrue(runnerJscompDevMode.shouldRunCompiler());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known Defect:
   * com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
   * -> junit.framework.AssertionFailedError: expected:<US-ASCII> but was:<null>
   *
   * As documented in the --charset flag usage:
   * "By default, we accept UTF-8 as input and output US_ASCII"
   * The compiler options must default outputCharset to "US-ASCII" when --charset is omitted.
   */
  @Test(timeout = 4000)
  public void testCharSetExpansion() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {});
    CompilerOptions options = runner.createOptions();
    assertEquals("US-ASCII", options.outputCharset);
  }

  @Test(timeout = 4000)
  public void testCustomCharsetExplicit() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--charset=UTF-8"});
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertEquals("UTF-8", options.outputCharset);
  }

  @Test(timeout = 4000)
  public void testCustomCharsetQuoted() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--charset=\"ISO-8859-1\""});
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertEquals("ISO-8859-1", options.outputCharset);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidFlagTriggersError() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--non_existent_unsupported_flag"},
        System.out,
        new PrintStream(err));
    assertFalse("Runner should reject unrecognized flags", runner.shouldRunCompiler());
    assertTrue("Error stream should report unrecognized option",
        err.toString().contains("is not a valid option"));
  }

  @Test(timeout = 4000)
  public void testHelpFlagPrintsUsageAndRejectsRun() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--help"},
        System.out,
        new PrintStream(err));
    assertFalse("Compiler should not run when --help is supplied", runner.shouldRunCompiler());
    assertTrue("Usage information must be written to stderr",
        err.toString().contains("--help"));
  }

  @Test(timeout = 4000)
  public void testInvalidBooleanOptionValue() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--debug=not_a_valid_boolean"},
        System.out,
        new PrintStream(err));
    assertFalse("Runner should fail with invalid boolean string", runner.shouldRunCompiler());
    assertTrue("Error stream must detail illegal boolean error",
        err.toString().contains("Illegal boolean value"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Externs Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetDefaultExternsIntegrity() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    assertNotNull("Default externs must be loaded from resources", defaultExterns);
    assertFalse("Default externs list must not be empty", defaultExterns.isEmpty());
    // Verify first standard entry matches the ordered list
    assertEquals("externs.zip//es3.js", defaultExterns.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testCreateExternsDefaultVsCustomOnly() throws Exception {
    // Branch 1: !use_only_custom_externs -> includes default externs
    SubCommandLineRunner runnerWithDefaults = new SubCommandLineRunner(new String[] {});
    List<JSSourceFile> externsWithDefaults = runnerWithDefaults.createExterns();
    assertNotNull(externsWithDefaults);
    assertFalse("Should contain bundled default externs", externsWithDefaults.isEmpty());

    // Branch 2: use_only_custom_externs -> excludes bundled default externs
    SubCommandLineRunner runnerCustomOnly = new SubCommandLineRunner(
        new String[] {"--use_only_custom_externs=true"});
    List<JSSourceFile> externsCustomOnly = runnerCustomOnly.createExterns();
    assertNotNull(externsCustomOnly);
    assertTrue("Should be empty since no custom externs were provided", externsCustomOnly.isEmpty());
  }
}