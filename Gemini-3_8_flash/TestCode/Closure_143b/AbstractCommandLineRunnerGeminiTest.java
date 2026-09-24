package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.AbstractCommandLineRunner
 *
 * 1. Defect-Targeted Branch Zone (Defects4J Known Fault: CommandLineRunnerTest::testDefineFlag3):
 *    - In `createDefineReplacements(List<String>, CompilerOptions)`:
 *      Parsing of string definitions enclosed in double quotes (e.g. `FOO="x'"`).
 *      The defective implementation only inspects single-quote bounding characters (`'`),
 *      causing double-quoted definitions to fail `Double.parseDouble()` and throw:
 *      `RuntimeException: --define flag syntax invalid: FOO="x'"`.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - `CommandLineConfig`: fluent configuration getters and setters.
 *    - `initOptionsFromFlags`: sets warning levels (ERROR, WARNING, OFF) and definitions.
 *    - `setRunOptions`: configures jsOutputFile, sourceMap, inputVariableMap, etc.
 *    - `createInputs`, `createSourceInputs`, `createExternInputs`: stdin handling, dev/null defaults.
 *    - `writeOutput`: wrapping formats with placeholder replacement and boundary checks.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - `createJsModules`:
 *      * parts.length < 2, parts.length > 4, invalid identifiers.
 *      * 0 js files count, negative counts, non-integer counts.
 *      * Underflow / overflow of available JS files.
 *      * Dependency order resolution and missing dependencies.
 *    - `parseModuleWrappers`:
 *      * Missing colon delimiter, unknown module lookup, missing `%s` placeholder.
 *      * Empty spec lists and multiple module mappings.
 *
 * 4. Partition C: Defensive & Exception Paths:
 *    - FlagUsageException on stdin specified twice or when stdin is disallowed.
 *    - Incompatible flags (`createNameMapFiles` with `variableMapOutputFile` or `propertyMapOutputFile`).
 *    - Invalid charset names handling in `getInputCharset`.
 */
public class AbstractCommandLineRunnerGeminiTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream testOut;
  private PrintStream testErr;

  private static class TestableCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private final Compiler compilerInstance;
    private final CompilerOptions optionsInstance;

    TestableCommandLineRunner(Compiler compiler, CompilerOptions options,
                              PrintStream out, PrintStream err) {
      super(out, err);
      this.compilerInstance = compiler;
      this.optionsInstance = options;
    }

    @Override
    protected Compiler createCompiler() {
      return compilerInstance;
    }

    @Override
    protected CompilerOptions createOptions() {
      return optionsInstance;
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    testOut = new PrintStream(outStream);
    testErr = new PrintStream(errStream);
  }

  @After
  public void tearDown() {
    testOut.close();
    testErr.close();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefineFlagDoubleQuotedStringSupport() {
    // Targets Defects4J bug: --define flag syntax invalid for double-quoted strings
    CompilerOptions options = new CompilerOptions();
    List<String> definitions = Collections.singletonList("FOO=\"x'\"");

    AbstractCommandLineRunner.createDefineReplacements(definitions, options);
    // On the unfixed runner, this throws RuntimeException: --define flag syntax invalid: FOO="x'"
    // In expected behavior, "FOO" is registered as a string literal definition with value "x'"
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsStandardValues() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList(
        "BOOL_TRUE=true",
        "BOOL_FALSE=false",
        "IMPLICIT_TRUE",
        "NUM_INT=42",
        "NUM_DOUBLE=3.1415",
        "STR_VAL='hello'"
    );

    AbstractCommandLineRunner.createDefineReplacements(defs, options);
    // If successfully processed without throwing runtime exceptions, definitions are accepted
  }

  @Test(timeout = 4000)
  public void testCommandLineConfigChainingAndGetters() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);
    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();

    assertSame(runner.getCommandLineConfig(), config);
    config.setPrintTree(true)
          .setComputePhaseOrdering(true)
          .setPrintAst(true)
          .setPrintPassGraph(true)
          .setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS)
          .setLoggingLevel("FINE")
          .setJsOutputFile("out.js")
          .setVariableMapInputFile("vars.in")
          .setPropertyMapInputFile("props.in")
          .setVariableMapOutputFile("vars.out")
          .setPropertyMapOutputFile("props.out")
          .setCreateNameMapFiles(true)
          .setSummaryDetailLevel(2)
          .setOutputWrapper("(function(){%output%})();")
          .setOutputWrapperMarker("%output%")
          .setModuleOutputPathPrefix("mod_")
          .setCreateSourceMap("map.out")
          .setCharset("UTF-8");

    assertSame(testErr, runner.getErrorPrintStream());
  }

  @Test(timeout = 4000)
  public void testWriteOutputWithWrapperAndPlaceholders() {
    String wrapper = "/* begin */%output%/* end */";
    AbstractCommandLineRunner.writeOutput(
        testOut, null, "var a = 1;", wrapper, "%output%");
    testOut.flush();

    String result = outStream.toString().trim();
    assertEquals("/* begin */var a = 1;/* end */", result);
  }

  @Test(timeout = 4000)
  public void testWriteOutputNoPlaceholder() {
    AbstractCommandLineRunner.writeOutput(
        testOut, null, "console.log(1);", "", "%marker%");
    testOut.flush();

    String result = outStream.toString().trim();
    assertEquals("console.log(1);", result);
  }

  @Test(timeout = 4000)
  public void testWriteOutputPlaceholderAtEnd() {
    String wrapper = "prefix:%marker%";
    AbstractCommandLineRunner.writeOutput(
        testOut, null, "code", wrapper, "%marker%");
    testOut.flush();

    String result = outStream.toString().trim();
    assertEquals("prefix:code", result);
  }

  @Test(timeout = 4000)
  public void testParseModuleWrappersValid() throws Exception {
    JSModule m1 = new JSModule("mod1");
    JSModule m2 = new JSModule("mod2");
    JSModule[] modules = new JSModule[]{m1, m2};

    List<String> specs = ImmutableList.of("mod1:(function(){%s})()");
    Map<String, String> wrapperMap =
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);

    assertNotNull(wrapperMap);
    assertEquals(2, wrapperMap.size());
    assertEquals("(function(){%s})()", wrapperMap.get("mod1"));
    assertEquals("", wrapperMap.get("mod2"));
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesValidSingle() throws Exception {
    List<String> specs = ImmutableList.of("m1:1");
    List<String> files = ImmutableList.of("test_gemini_dummy.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    assertNotNull(modules);
    assertEquals(1, modules.length);
    assertEquals("m1", modules[0].getName());
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesWithDependencies() throws Exception {
    List<String> specs = ImmutableList.of("base:1", "child:1:base");
    List<String> files = ImmutableList.of("f1.js", "f2.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    assertEquals(2, modules.length);
    assertEquals("base", modules[0].getName());
    assertEquals("child", modules[1].getName());
    assertTrue(modules[1].getDependencies().contains(modules[0]));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateJsModulesZeroFilesAllowed() throws Exception {
    List<String> specs = ImmutableList.of("mEmpty:0");
    List<String> files = Collections.emptyList();

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    assertEquals(1, modules.length);
    assertEquals("mEmpty", modules[0].getName());
  }

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsEmptyList() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(
        Collections.<String>emptyList(), options);
  }

  @Test(timeout = 4000)
  public void testInitOptionsFromFlags() {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);

    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    config.setJscompError(ImmutableList.of("checkVars"));
    config.setJscompWarning(ImmutableList.of("checkTypes"));
    config.setJscompOff(ImmutableList.of("deprecated"));
    config.setDefine(ImmutableList.of("DEBUG=false"));

    CompilerOptions options = new CompilerOptions();
    runner.initOptionsFromFlags(options);

    assertNotNull(runner.getDiagnosticGroups());
  }

  @Test(timeout = 4000)
  public void testSetRunOptionsBasic() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);

    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    config.setJsOutputFile("custom_out.js");
    config.setCreateSourceMap("source.map");
    config.setSummaryDetailLevel(3);

    CompilerOptions options = new CompilerOptions();
    runner.setRunOptions(options);

    assertEquals("custom_out.js", options.jsOutputFile);
    assertEquals("source.map", options.sourceMapOutputPath);
    assertEquals(3, options.summaryDetailLevel);
  }

  // =========================================================================
  // Partition D: Defensive & Exception Paths
  // =========================================================================

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testCreateDefineReplacementsInvalidFormatNoName() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(
        ImmutableList.of("=123"), options);
  }

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testCreateDefineReplacementsInvalidValue() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(
        ImmutableList.of("VAR=not_a_valid_number_or_quoted_str"), options);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesInvalidPartsCount() throws Exception {
    List<String> specs = ImmutableList.of("invalid_module_only_one_part");
    List<String> files = ImmutableList.of("file.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesInvalidPartsTooMany() throws Exception {
    List<String> specs = ImmutableList.of("mod:1:dep1:extra:toomany");
    List<String> files = ImmutableList.of("file.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesInvalidIdentifier() throws Exception {
    List<String> specs = ImmutableList.of("123badIdentifier:0");
    List<String> files = Collections.emptyList();
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesDuplicateModuleName() throws Exception {
    List<String> specs = ImmutableList.of("modA:0", "modA:0");
    List<String> files = Collections.emptyList();
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesNegativeFileCount() throws Exception {
    List<String> specs = ImmutableList.of("modA:-2");
    List<String> files = Collections.emptyList();
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesNotEnoughFiles() throws Exception {
    List<String> specs = ImmutableList.of("modA:5");
    List<String> files = ImmutableList.of("onlyOne.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesTooManyFilesSpecified() throws Exception {
    List<String> specs = ImmutableList.of("modA:1");
    List<String> files = ImmutableList.of("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesUnknownDependency() throws Exception {
    List<String> specs = ImmutableList.of("modA:1:nonExistentDep");
    List<String> files = ImmutableList.of("f1.js");
    AbstractCommandLineRunner.createJsModules(specs, files);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersMissingColon() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    List<String> specs = ImmutableList.of("m1_wrapper_no_colon");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersUnknownModule() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    List<String> specs = ImmutableList.of("mUnknown:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersMissingPlaceholder() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    List<String> specs = ImmutableList.of("m1:missing_placeholder");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testSetRunOptionsInvalidCharset() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME_XYZ");
    CompilerOptions options = new CompilerOptions();
    runner.setRunOptions(options);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testConflictingNameMapFlagsVariable() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);
    runner.getCommandLineConfig()
        .setCreateNameMapFiles(true)
        .setVariableMapOutputFile("vars.out");

    CompilerOptions options = new CompilerOptions();
    Result result = new Result(new JSError[0], new JSError[0], "none", null);
    runner.processResults(result, null, options);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class, timeout = 4000)
  public void testConflictingNameMapFlagsProperty() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);
    runner.getCommandLineConfig()
        .setCreateNameMapFiles(true)
        .setPropertyMapOutputFile("props.out");

    CompilerOptions options = new CompilerOptions();
    Result result = new Result(new JSError[0], new JSError[0], "none", null);
    runner.processResults(result, null, options);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Diagnostics Integration
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessResultsWithoutErrors() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        compiler, options, testOut, testErr);

    Result result = new Result(new JSError[0], new JSError[0], "externs", null);
    int exitCode = runner.processResults(result, null, options);

    assertEquals(0, exitCode);
  }

  @Test(timeout = 4000)
  public void testProcessResultsWithCompilationErrors() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        compiler, options, testOut, testErr);

    JSError error = JSError.make("test.js", 1, 1, CheckLevel.ERROR,
        DiagnosticType.error("TEST_ERR", "Sample error"));
    Result result = new Result(new JSError[]{error}, new JSError[0], null, null);

    int exitCode = runner.processResults(result, null, options);
    assertEquals(1, exitCode);
  }

  @Test(timeout = 4000)
  public void testCreateExternsDefaultsToDevNull() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new Compiler(), new CompilerOptions(), testOut, testErr);

    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertEquals(1, externs.size());
    assertEquals("/dev/null", externs.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testFlagUsageExceptionMessageIntegrity() {
    AbstractCommandLineRunner.FlagUsageException ex =
        new AbstractCommandLineRunner.FlagUsageException("Custom Flag Error");
    assertEquals("Custom Flag Error", ex.getMessage());
  }
}