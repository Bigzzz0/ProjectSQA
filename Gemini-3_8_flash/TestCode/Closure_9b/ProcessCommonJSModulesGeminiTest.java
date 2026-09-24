package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Target (Closure-34/CommonJS):
 * - ProcessCommonJSModules.guessCJSModuleName failed to normalize the source filename prefix,
 *   retaining the prefix in the rewritten module identifier (e.g. "module$foo$baz" vs "module$baz").
 *   Targeted by: testGuessModuleNameDefectTarget(), testGuessModuleNameWithTrailingSlash()
 *
 * Core Decision Logic & Branches Targeted:
 * - constructor(compiler, prefix, reportDependencies):
 *   * Branch: prefix ends with MODULE_SLASH vs does not end with MODULE_SLASH.
 * - toModuleName(String):
 *   * Leading "./" stripping, '/' -> '$', trailing ".js" stripping, '-' -> '_'.
 * - toModuleName(String, String):
 *   * Branch: starts with "./" -> URI.resolve()
 *   * Branch: starts with "../" -> URI.resolve()
 *   * Branch: neither -> straight to toModuleName(requiredFilename)
 *   * Exception Branch: URISyntaxException wrapped in RuntimeException.
 * - normalizeSourceName(String):
 *   * Branch: filename.indexOf(filenamePrefix) == 0 vs != 0.
 * - visit(NodeTraversal, Node, Node):
 *   * Call node: require() with 1 string arg vs other calls / invalid arg counts / non-string args.
 *   * Script node: single script (valid) vs multiple scripts (Preconditions check fails).
 *   * GetProp node: "module.exports" vs other property accesses.
 * - visitRequireCall:
 *   * reportDependencies == true (t.getInput().addRequire()) vs false.
 *   * nested require AST parent search (getCurrentScriptNode loop).
 * - visitScript:
 *   * reportDependencies == true (addProvide, JSModule creation) vs false.
 *   * emitOptionalModuleExportsOverride: modulesWithExports.contains vs not.
 * - SuffixVarsCallback:
 *   * n.isName() == false
 *   * name.equals(suffix) -> return early
 *   * name.equals("exports") -> rename to suffix
 *   * var != null && var.isGlobal() -> rename to name$$suffix
 *   * var != null && !var.isGlobal() -> local variable unchanged
 *   * var == null -> undeclared variable unchanged
 */
public class ProcessCommonJSModulesGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultPrefixConstant() {
    assertEquals("./", ProcessCommonJSModules.DEFAULT_FILENAME_PREFIX);
  }

  @Test(timeout = 4000)
  public void testConstructorPrefixHandling() {
    Compiler compiler = new Compiler();
    // Prefix without trailing slash
    ProcessCommonJSModules pass1 = new ProcessCommonJSModules(compiler, "base");
    assertEquals("module$sub$file", pass1.guessCJSModuleName("base/sub/file.js"));

    // Prefix with trailing slash
    ProcessCommonJSModules pass2 = new ProcessCommonJSModules(compiler, "base/");
    assertEquals("module$sub$file", pass2.guessCJSModuleName("base/sub/file.js"));
  }

  @Test(timeout = 4000)
  public void testToModuleNameBasicTransformation() {
    assertEquals("module$simple", ProcessCommonJSModules.toModuleName("simple"));
    assertEquals("module$simple", ProcessCommonJSModules.toModuleName("simple.js"));
    assertEquals("module$simple", ProcessCommonJSModules.toModuleName("./simple.js"));
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar.js"));
    assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
    assertEquals("module$a$b_c$d", ProcessCommonJSModules.toModuleName("./a/b-c/d.js"));
  }

  @Test(timeout = 4000)
  public void testToModuleNameRelativeResolutionSameDir() {
    // Current: foo/bar.js, Required: ./baz.js -> foo/baz
    String result = ProcessCommonJSModules.toModuleName("./baz.js", "foo/bar.js");
    assertEquals("module$foo$baz", result);
  }

  @Test(timeout = 4000)
  public void testToModuleNameRelativeResolutionParentDir() {
    // Current: foo/bar/baz.js, Required: ../qux.js -> foo/qux
    String result = ProcessCommonJSModules.toModuleName("../qux.js", "foo/bar/baz.js");
    assertEquals("module$foo$qux", result);
  }

  @Test(timeout = 4000)
  public void testToModuleNameNonRelativeResolution() {
    // Non-relative require should ignore currentFilename
    String result = ProcessCommonJSModules.toModuleName("external-pkg", "foo/bar.js");
    assertEquals("module$external_pkg", result);
  }

  @Test(timeout = 4000)
  public void testProcessRequireCallTransformationWithDependencies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/test.js", "var bar = require('./bar');"));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", true);
    pass.process(compiler.getRoot().getFirstChild(), root);

    assertNotNull(pass.getModule());
    assertEquals("module$test", pass.getModule().getName());

    String output = compiler.toSource(root);
    assertTrue(output.contains("goog.provide(\"module$test\")"));
    assertTrue(output.contains("goog.require(\"module$bar\")"));
    assertTrue(output.contains("var module$test = {}"));
    assertTrue(output.contains("bar$$module$test = module$bar"));
  }

  @Test(timeout = 4000)
  public void testProcessWithoutDependenciesReport() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/test.js", "var x = 1;"));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    // Module should remain null when reportDependencies is false
    assertNull(pass.getModule());

    String output = compiler.toSource(root);
    assertTrue(output.contains("goog.provide(\"module$test\")"));
    assertFalse(output.contains("goog.require"));
  }

  @Test(timeout = 4000)
  public void testProcessModuleExportsOverrideEmitted() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/mod.js", "module.exports = { k: 1 };"));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    String output = compiler.toSource(root);
    assertTrue(output.contains("module$mod.module$exports = {k:1}"));
    assertTrue(output.contains("if(module$mod.module$exports)"));
    assertTrue(output.contains("module$mod = module$mod.module$exports"));
  }

  @Test(timeout = 4000)
  public void testSuffixVarsScopeDistinction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code =
        "var module$mod = 99;\n" +
        "exports.act = function() { return 1; };\n" +
        "var globalVar = 100;\n" +
        "function fn(param) {\n" +
        "  var localVar = 200;\n" +
        "  return localVar + param;\n" +
        "}\n" +
        "undeclaredVar = 300;\n";
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/mod.js", code));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    String output = compiler.toSource(root);
    // Suffix match: module$mod is not suffix-appended
    assertTrue(output.contains("module$mod = 99"));
    // exports name renamed to module$mod
    assertTrue(output.contains("module$mod.act = function()"));
    // global variable gets suffix
    assertTrue(output.contains("globalVar$$module$mod = 100"));
    // local variable should NOT get suffix
    assertTrue(output.contains("var localVar = 200"));
    // undeclared variable should NOT get suffix
    assertTrue(output.contains("undeclaredVar = 300"));
  }

  @Test(timeout = 4000)
  public void testDeeplyNestedRequireCall() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code =
        "function f1() {\n" +
        "  if (true) {\n" +
        "    while (false) {\n" +
        "      var req = require('./nested');\n" +
        "    }\n" +
        "  }\n" +
        "}\n";
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/main.js", code));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    String output = compiler.toSource(root);
    assertTrue(output.contains("goog.require(\"module$nested\")"));
    assertTrue(output.contains("req$$module$main = module$nested"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testToModuleNameEmptyAndRootBoundaries() {
    assertEquals("module$", ProcessCommonJSModules.toModuleName(""));
    assertEquals("module$", ProcessCommonJSModules.toModuleName("./"));
    assertEquals("module$", ProcessCommonJSModules.toModuleName(".js"));
  }

  @Test(timeout = 4000)
  public void testToModuleNameMultipleDashesAndSlashes() {
    assertEquals("module$a_b_c_d", ProcessCommonJSModules.toModuleName("a-b-c-d.js"));
    assertEquals("module$a$b$c$d", ProcessCommonJSModules.toModuleName("a/b/c/d.js"));
    assertEquals("module$a_b$c_d", ProcessCommonJSModules.toModuleName("./a-b/c-d.js"));
  }

  @Test(timeout = 4000)
  public void testToModuleNameWithDotJsInsidePath() {
    // .js occurring not at the end must NOT be stripped by \\.js$
    assertEquals("module$foo.js$bar", ProcessCommonJSModules.toModuleName("foo.js/bar.js"));
  }

  @Test(timeout = 4000)
  public void testUnmatchedPrefixNormalizesWithoutChange() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo");
    assertEquals("module$other$file", pass.guessCJSModuleName("other/file.js"));
  }

  @Test(timeout = 4000)
  public void testNonRequireCallSignaturesIgnored() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code =
        "var a = require();\n" +            // 0 args (childCount 1)
        "var b = require('a', 'b');\n" +     // 2 args (childCount 3)
        "var c = otherFunc('a');\n" +        // different func name
        "var d = require(123);\n" +          // non-string arg
        "var e = require(someVar);\n";       // variable arg
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/test.js", code));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    String output = compiler.toSource(root);
    // None of these should emit goog.require
    assertFalse(output.contains("goog.require"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where guessCJSModuleName fails to strip the configured
   * filenamePrefix, returning "module$foo$baz" instead of "module$baz".
   */
  @Test(timeout = 4000)
  public void testGuessModuleNameDefectTarget() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo");
    assertEquals("module$baz", pass.guessCJSModuleName("foo/baz.js"));
  }

  @Test(timeout = 4000)
  public void testGuessModuleNameWithTrailingSlash() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo/");
    assertEquals("module$baz", pass.guessCJSModuleName("foo/baz.js"));
  }

  @Test(timeout = 4000)
  public void testGuessModuleNameNestedDirectory() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(null, "foo/bar");
    assertEquals("module$baz", pass.guessCJSModuleName("foo/bar/baz.js"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleScriptNodesThrowsException() {
    Compiler compiler = new Compiler();
    Node root = IR.root();
    Node script1 = compiler.parseTestCode("var a = 1;");
    Node script2 = compiler.parseTestCode("var b = 2;");
    root.addChildToBack(script1);
    root.addChildToBack(script2);

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    try {
      pass.process(null, root);
      fail("Expected IllegalArgumentException when traversing multiple script nodes.");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("supports only one invocation per"));
    }
  }

  @Test(timeout = 4000)
  public void testToModuleNameInvalidUriSyntaxThrowsRuntimeException() {
    try {
      // Illegal character in URI path component triggers URISyntaxException
      ProcessCommonJSModules.toModuleName("./[illegal_uri]", "foo/bar.js");
      fail("Expected RuntimeException wrapping URISyntaxException.");
    } catch (RuntimeException expected) {
      assertNotNull(expected.getCause());
      assertTrue(expected.getCause() instanceof java.net.URISyntaxException);
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Initial State Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testInitialStateOfGetModule() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "base");
    assertNull("Initial module state must be null before processing", pass.getModule());
  }

  @Test(timeout = 4000)
  public void testEmptyScriptProcessing() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("foo/empty.js", ""));
    compiler.compile(Collections.<SourceFile>emptyList(), inputs, options);

    Node root = compiler.getRoot().getLastChild();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "foo", false);
    pass.process(compiler.getRoot().getFirstChild(), root);

    String output = compiler.toSource(root);
    assertTrue(output.contains("goog.provide(\"module$empty\")"));
    assertTrue(output.contains("var module$empty = {}"));
    // No module.exports override when there are no exports
    assertFalse(output.contains("module$empty = module$empty.module$exports"));
  }
}