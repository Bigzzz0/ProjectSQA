/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: com.google.javascript.jscomp.AbstractCommandLineRunner
 *
 * Partition A: Core Functional Logic & State Transitions
 * - createDefineReplacements: boolean literal (implicit true, explicit true, explicit false)
 * - createDefineReplacements: string literal (single quotes, double quotes, nested quotes)
 * - createDefineReplacements: number literal (positive, negative, floating point)
 * - createJsModules: valid multi-module specs with dependency ordering
 * - parseModuleWrappers: single and multi-module valid wrapper string replacement
 * - writeOutput: without placeholder, with placeholder at start/middle/end
 * - expandCommandLinePath / expandSourceMapPath / expandManifest:
 *     * Module output mode (forModule != null)
 *     * Multi-module global mode (module config present, forModule == null)
 *     * Single js output mode (no module config, jsOutputFile present)
 * - printModuleGraphManifestTo: correct formatting of dependencies and inputs
 * - CommandLineConfig fluent builders for all flag fields
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - createDefineReplacements: empty definitions list
 * - createJsModules: modules with zero inputs (numJsFiles == 0)
 * - parseModuleWrappers: empty wrapper list with existing modules
 * - writeOutput: empty code, empty wrapper, placeholder only
 * - expandSourceMapPath / expandManifest: null or empty paths returning null
 *
 * Partition C: Defect-Targeted Branch Zone
 * - Targeted Defect: CommandLineRunnerTest::testCharSetExpansion
 *   Root Cause: setRunOptions() sets inputCharset but fails to populate options.outputCharset
 *   (leaving it null when setRunOptions is invoked outside of doRun()).
 *   Expected: options.outputCharset must be populated with Charsets.US_ASCII by default (UTF-8),
 *   or the configured custom Charset.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - createDefineReplacements: invalid syntax (missing name, invalid number/string formats)
 * - createJsModules: null specs, empty specs, null jsFiles
 * - createJsModules: invalid colon parts count (< 2 or > 4)
 * - createJsModules: invalid JS identifier in module name
 * - createJsModules: duplicate module names
 * - createJsModules: invalid/negative JS file counts
 * - createJsModules: JS file count underflow / overflow vs actual jsFiles list
 * - createJsModules: unknown module dependency or reversed dependency order
 * - parseModuleWrappers: null specs, spec without ':', unknown module name, missing '%s'
 * - setRunOptions: unsupported charset name throwing FlagUsageException
 * - FlagUsageException message verification
 * -----------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.jscomp.AbstractCommandLineRunner.CommandLineConfig;
import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractCommandLineRunnerGeminiTest {

  /**
   * Concrete subclass of AbstractCommandLineRunner to enable white-box execution
   * without invoking System.exit().
   */
  private static class TestCommandLineRunner
      extends AbstractCommandLineRunner<Compiler, CompilerOptions> {

    private Compiler customCompiler;
    private CompilerOptions customOptions;

    TestCommandLineRunner() {
      super();
    }

    TestCommandLineRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    void setCustomCompiler(Compiler compiler) {
      this.customCompiler = compiler;
    }

    void setCustomOptions(CompilerOptions options) {
      this.customOptions = options;
    }

    @Override
    protected Compiler createCompiler() {
      return customCompiler != null ? customCompiler : new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      return customOptions != null ? customOptions : new CompilerOptions();
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsBooleans() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Arrays.asList(
        "FLAG_IMPLICIT",
        "FLAG_TRUE=true",
        "FLAG_FALSE=false"
    );
    AbstractCommandLineRunner.createDefineReplacements(defs, options);

    assertEquals(true, options.getDefineBooleanLiteral("FLAG_IMPLICIT"));
    assertEquals(true, options.getDefineBooleanLiteral("FLAG_TRUE"));
    assertEquals(false, options.getDefineBooleanLiteral("FLAG_FALSE"));
  }

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsStrings() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Arrays.asList(
        "STR_SINGLE='hello world'",
        "STR_DOUBLE=\"foo bar\""
    );
    AbstractCommandLineRunner.createDefineReplacements(defs, options);

    assertEquals("hello world", options.getDefineStringLiteral("STR_SINGLE"));
    assertEquals("foo bar", options.getDefineStringLiteral("STR_DOUBLE"));
  }

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsNumbers() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Arrays.asList(
        "NUM_INT=42",
        "NUM_NEG=-15.5",
        "NUM_ZERO=0"
    );
    AbstractCommandLineRunner.createDefineReplacements(defs, options);

    assertEquals(42.0, options.getDefineDoubleLiteral("NUM_INT"), 0.0001);
    assertEquals(-15.5, options.getDefineDoubleLiteral("NUM_NEG"), 0.0001);
    assertEquals(0.0, options.getDefineDoubleLiteral("NUM_ZERO"), 0.0001);
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesValid() throws Exception {
    List<String> specs = Arrays.asList("mod1:2", "mod2:1:mod1");
    List<String> jsFiles = Arrays.asList("f1.js", "f2.js", "f3.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertNotNull(modules);
    assertEquals(2, modules.length);

    assertEquals("mod1", modules[0].getName());
    assertEquals(2, modules[0].getInputs().size());
    assertEquals(0, modules[0].getDependencies().size());

    assertEquals("mod2", modules[1].getName());
    assertEquals(1, modules[1].getInputs().size());
    assertEquals(1, modules[1].getDependencies().size());
    assertEquals(modules[0], modules[1].getDependencies().get(0));
  }

  @Test(timeout = 4000)
  public void testParseModuleWrappersValid() throws Exception {
    JSModule m1 = new JSModule("mod1");
    JSModule m2 = new JSModule("mod2");
    JSModule[] modules = new JSModule[]{m1, m2};

    List<String> specs = Collections.singletonList("mod1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);

    assertEquals(2, wrappers.size());
    assertEquals("(function(){%s})();", wrappers.get("mod1"));
    assertEquals("", wrappers.get("mod2"));
  }

  @Test(timeout = 4000)
  public void testWriteOutputNoPlaceholder() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "alert(1);", "none", "%s");
    assertEquals("alert(1);\n", sb.toString());
  }

  @Test(timeout = 4000)
  public void testWriteOutputWithPlaceholder() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 2;", "(function(){%output%})();", "%output%");
    assertEquals("(function(){var a = 2;})();\n", sb.toString());
  }

  @Test(timeout = 4000)
  public void testWriteOutputPlaceholderBoundaries() throws IOException {
    StringBuilder sbStart = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sbStart, null, "CODE", "%s/*after*/", "%s");
    assertEquals("CODE/*after*/\n", sbStart.toString());

    StringBuilder sbEnd = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sbEnd, null, "CODE", "/*before*/%s", "%s");
    assertEquals("/*before*/CODE\n", sbEnd.toString());
  }

  @Test(timeout = 4000)
  public void testExpandCommandLinePaths() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    CompilerOptions options = new CompilerOptions();
    CommandLineConfig config = runner.getCommandLineConfig();

    config.setJsOutputFile("out/bundle.js");
    options.sourceMapOutputPath = "%outname%.map";
    String expandedSourceMap = runner.expandSourceMapPath(options, null);
    assertEquals("out/bundle.js.map", expandedSourceMap);

    config.setModule(Arrays.asList("mod1:1", "mod2:1:mod1"));
    config.setModuleOutputPathPrefix("dist/mod_");
    String expandedMultiGlobal = runner.expandSourceMapPath(options, null);
    assertEquals("dist/mod_.map", expandedMultiGlobal);

    JSModule module = new JSModule("mod1");
    String expandedPerModule = runner.expandSourceMapPath(options, module);
    assertEquals("dist/mod_mod1.js.map", expandedPerModule);

    config.setOutputManifest("manifests/%outname%.txt");
    assertEquals("dist/mod_mod1.js.txt", runner.expandManifest(module));
  }

  @Test(timeout = 4000)
  public void testPrintModuleGraphManifestTo() throws IOException {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    JSModule m1 = new JSModule("base");
    JSModule m2 = new JSModule("child");
    m2.addDependency(m1);

    m1.add(JSSourceFile.fromCode("base1.js", ""));
    m2.add(JSSourceFile.fromCode("child1.js", ""));

    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});
    StringBuilder sb = new StringBuilder();
    runner.printModuleGraphManifestTo(graph, sb);

    String output = sb.toString();
    assertTrue(output.contains("{base}\nbase1.js"));
    assertTrue(output.contains("{child:base}\nchild1.js"));
  }

  @Test(timeout = 4000)
  public void testCommandLineConfigBuilders() {
    CommandLineConfig config = new CommandLineConfig();
    assertSame(config, config.setPrintTree(true));
    assertSame(config, config.setComputePhaseOrdering(true));
    assertSame(config, config.setPrintAst(true));
    assertSame(config, config.setPrintPassGraph(true));
    assertSame(config, config.setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS));
    assertSame(config, config.setLoggingLevel("FINE"));
    assertSame(config, config.setExterns(Collections.singletonList("ext.js")));
    assertSame(config, config.setJs(Collections.singletonList("src.js")));
    assertSame(config, config.setJsOutputFile("out.js"));
    assertSame(config, config.setModule(Collections.singletonList("mod:1")));
    assertSame(config, config.setVariableMapInputFile("var_in.map"));
    assertSame(config, config.setPropertyMapInputFile("prop_in.map"));
    assertSame(config, config.setVariableMapOutputFile("var_out.map"));
    assertSame(config, config.setCreateNameMapFiles(true));
    assertSame(config, config.setPropertyMapOutputFile("prop_out.map"));
    assertSame(config, config.setCodingConvention(new ClosureCodingConvention()));
    assertSame(config, config.setSummaryDetailLevel(2));
    assertSame(config, config.setOutputWrapper("(%output%)"));
    assertSame(config, config.setOutputWrapperMarker("%output%"));
    assertSame(config, config.setModuleWrapper(Collections.singletonList("mod:%s")));
    assertSame(config, config.setModuleOutputPathPrefix("mod_"));
    assertSame(config, config.setCreateSourceMap("out.map"));
    assertSame(config, config.setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS));
    assertSame(config, config.setJscompError(Collections.singletonList("checkVars")));
    assertSame(config, config.setJscompWarning(Collections.singletonList("deprecated")));
    assertSame(config, config.setJscompOff(Collections.singletonList("visibility")));
    assertSame(config, config.setDefine(Collections.singletonList("FOO=1")));
    assertSame(config, config.setCharset("UTF-8"));
    assertSame(config, config.setManageClosureDependencies(true));
    assertSame(config, config.setOutputManifest("manifest.txt"));
  }

  @Test(timeout = 4000)
  public void testDiagnosticGroupsAndOptionsIntegration() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    CommandLineConfig config = runner.getCommandLineConfig();
    config.setJscompError(Collections.singletonList("checkVars"));
    config.setJscompWarning(Collections.singletonList("deprecated"));
    config.setJscompOff(Collections.singletonList("visibility"));
    config.setManageClosureDependencies(true);
    config.setSummaryDetailLevel(3);

    CompilerOptions options = new CompilerOptions();
    runner.setRunOptions(options);

    assertTrue(options.manageClosureDependencies);
    assertEquals(3, options.summaryDetailLevel);
    assertNotNull(runner.getDiagnosticGroups());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateDefineReplacementsEmpty() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(Collections.emptyList(), options);
    // Should successfully execute without setting any defines
  }

  @Test(timeout = 4000)
  public void testCreateJsModulesZeroFileModule() throws Exception {
    List<String> specs = Arrays.asList("mod1:0", "mod2:1:mod1");
    List<String> jsFiles = Collections.singletonList("app.js");

    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    assertEquals(2, modules.length);
    assertEquals(0, modules[0].getInputs().size());
    assertEquals(1, modules[1].getInputs().size());
  }

  @Test(timeout = 4000)
  public void testParseModuleWrappersEmptySpecs() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("app")};
    Map<String, String> wrappers =
        AbstractCommandLineRunner.parseModuleWrappers(Collections.emptyList(), modules);
    assertEquals(1, wrappers.size());
    assertEquals("", wrappers.get("app"));
  }

  @Test(timeout = 4000)
  public void testWriteOutputEmptyInputs() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "", "%s", "%s");
    assertEquals("\n", sb.toString());
  }

  @Test(timeout = 4000)
  public void testExpandPathsNullAndEmpty() {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    CompilerOptions options = new CompilerOptions();

    options.sourceMapOutputPath = null;
    assertNull(runner.expandSourceMapPath(options, null));

    options.sourceMapOutputPath = "";
    assertNull(runner.expandSourceMapPath(options, null));

    runner.getCommandLineConfig().setOutputManifest(null);
    assertNull(runner.expandManifest(null));

    runner.getCommandLineConfig().setOutputManifest("");
    assertNull(runner.expandManifest(null));
  }

  @Test(timeout = 4000)
  public void testRunnerCustomPrintStreams() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream outPs = new PrintStream(out);
    PrintStream errPs = new PrintStream(err);

    TestCommandLineRunner runner = new TestCommandLineRunner(outPs, errPs);
    assertSame(errPs, runner.getErrorPrintStream());
    assertNull(runner.getCompiler());

    Compiler compiler = runner.createCompiler();
    CompilerOptions options = runner.createOptions();
    assertNotNull(compiler);
    assertNotNull(options);

    runner.initOptionsFromFlags(options);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect exhibited in CommandLineRunnerTest::testCharSetExpansion:
   * "expected:<US-ASCII> but was:<null>".
   *
   * In the defective implementation, setRunOptions() fails to propagate
   * the output charset into options.outputCharset, leaving it null.
   */
  @Test(timeout = 4000)
  public void testCharSetExpansionDefaultToAscii() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    CompilerOptions options = new CompilerOptions();

    runner.setRunOptions(options);

    // Defect condition: options.outputCharset remains null unless setRunOptions sets it.
    assertEquals(Charsets.US_ASCII, options.outputCharset);
  }

  @Test(timeout = 4000)
  public void testCharSetExpansionExplicitUtf8ToAscii() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig().setCharset("UTF-8");
    CompilerOptions options = new CompilerOptions();

    runner.setRunOptions(options);

    assertEquals(Charsets.US_ASCII, options.outputCharset);
  }

  @Test(timeout = 4000)
  public void testCharSetExpansionCustomCharset() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig().setCharset("ISO-8859-1");
    CompilerOptions options = new CompilerOptions();

    runner.setRunOptions(options);

    assertEquals(Charset.forName("ISO-8859-1"), options.outputCharset);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testCreateDefineReplacementsEmptyDefName() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(Collections.singletonList("=123"), options);
  }

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testCreateDefineReplacementsInvalidFormat() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(
        Collections.singletonList("DEF='nested'quote'"), options);
  }

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testCreateDefineReplacementsInvalidNumber() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(
        Collections.singletonList("DEF=not_a_number_or_valid_string"), options);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCreateJsModulesNullSpecs() throws Exception {
    AbstractCommandLineRunner.createJsModules(null, Collections.singletonList("a.js"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCreateJsModulesEmptySpecs() throws Exception {
    AbstractCommandLineRunner.createJsModules(Collections.emptyList(), Collections.singletonList("a.js"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCreateJsModulesNullFiles() throws Exception {
    AbstractCommandLineRunner.createJsModules(Collections.singletonList("m:1"), null);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesTooFewColonParts() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("mod1"), Collections.singletonList("a.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesTooManyColonParts() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("mod1:1:dep:extra:another"), Collections.singletonList("a.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesInvalidIdentifierName() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("123-bad:1"), Collections.singletonList("a.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesDuplicateModuleName() throws Exception {
    List<String> specs = Arrays.asList("m1:1", "m1:1");
    List<String> jsFiles = Arrays.asList("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesInvalidFileCountFormat() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("m1:two"), Collections.singletonList("a.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesNegativeFileCount() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("m1:-1"), Collections.singletonList("a.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesNotEnoughJsFiles() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("m1:3"), Arrays.asList("f1.js", "f2.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesTooManyJsFiles() throws Exception {
    AbstractCommandLineRunner.createJsModules(
        Collections.singletonList("m1:1"), Arrays.asList("f1.js", "f2.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesUnknownDependency() throws Exception {
    List<String> specs = Collections.singletonList("m1:1:unknownModule");
    AbstractCommandLineRunner.createJsModules(specs, Collections.singletonList("f1.js"));
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testCreateJsModulesReversedDependencyOrder() throws Exception {
    List<String> specs = Arrays.asList("m1:1:m2", "m2:1");
    List<String> jsFiles = Arrays.asList("f1.js", "f2.js");
    AbstractCommandLineRunner.createJsModules(specs, jsFiles);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testParseModuleWrappersNullSpecs() throws Exception {
    AbstractCommandLineRunner.parseModuleWrappers(null, new JSModule[0]);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersMissingColon() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("mod1")};
    AbstractCommandLineRunner.parseModuleWrappers(Collections.singletonList("mod1_no_colon"), modules);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersUnknownModule() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("mod1")};
    AbstractCommandLineRunner.parseModuleWrappers(
        Collections.singletonList("unknown:%s"), modules);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testParseModuleWrappersMissingPlaceholder() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("mod1")};
    AbstractCommandLineRunner.parseModuleWrappers(
        Collections.singletonList("mod1:(function(){})();"), modules);
  }

  @Test(expected = FlagUsageException.class, timeout = 4000)
  public void testSetRunOptionsUnsupportedCharset() throws Exception {
    TestCommandLineRunner runner = new TestCommandLineRunner();
    runner.getCommandLineConfig().setCharset("COMPLETELY_INVALID_CHARSET_NAME");
    CompilerOptions options = new CompilerOptions();
    runner.setRunOptions(options);
  }

  @Test(timeout = 4000)
  public void testFlagUsageExceptionMessage() {
    FlagUsageException ex = new FlagUsageException("Custom flag usage error");
    assertEquals("Custom flag usage error", ex.getMessage());
  }
}