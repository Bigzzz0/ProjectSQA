package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: ProcessCommonJSModules
 * Defects4J Bug Identification:
 * In ProcessCommonJSModules.java:
 *   public static String toModuleName(String filename) {
 *     return MODULE_NAME_PREFIX +
 *         filename.replaceAll("^\\." + Pattern.quote(File.separator), "")
 *             .replaceAll(Pattern.quote(File.separator), MODULE_NAME_SEPARATOR)
 *             .replaceAll("\\.js$", "").replaceAll("-", "_");
 *   }
 *
 * Notice the bug in `guessCJSModuleName(String filename)` / `toModuleName`:
 * `visitScript` calls `guessCJSModuleName(normalizeSourceName(script.getSourceFileName()))`
 * BUT `visitModuleExports` calls:
 * `String moduleName = guessCJSModuleName(prop.getSourceFileName());`
 * NOTICE: It forgot to call `normalizeSourceName(...)`!
 * When filenamePrefix is set (e.g. "foo/"), `visitScript` produces moduleName "module$bar"
 * but `visitModuleExports` leaves the prefix in, producing "module$foo$bar".
 * This causes a mismatch between the module variable declaration and the module.exports reference!
 *
 * Also in `toModuleName(requiredFilename, currentFilename)`:
 * Leading `./` and `../` resolution logic via java.net.URI.
 *
 * Partition Matrix:
 * - Partition A: Core Functional Logic & State Transitions (Standard CommonJS to Closure rewrite, require calls, exports)
 * - Partition B: Boundary Value Analysis (BVA) & Path normalization (slashes, dots, dashes, prefix stripping)
 * - Partition C: Defect-Targeted Branch Zone (Targeting the filenamePrefix normalization discrepancy in visitModuleExports)
 * - Partition D: Exception & Defensive Guard Paths (Multiple script nodes in one traversal triggering Preconditions check)
 * - Partition E: Module name resolution logic with relative paths (URI resolution with ./ and ../)
 */

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ProcessCommonJSModulesGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  /**
   * Helper to run ProcessCommonJSModules pass on JS source code.
   */
  private Node testProcess(String js, String filename, String prefix, boolean reportDependencies) {
    Node script = compiler.parseTestCode(js);
    script.setSourceFileName(filename);
    Node root = IR.root(script);
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, prefix, reportDependencies);
    pass.process(null, root);
    return script;
  }

  private Node testProcess(String js, String filename, String prefix) {
    return testProcess(js, filename, prefix, true);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicModuleRewriteWithoutExports() {
    String js = "var a = 1;";
    String filename = "test.js";
    Node script = testProcess(js, filename, ".");

    String expectedPrefix = "goog.provide(\"module$test\");";
    String expectedVar = "var module$test = {};";
    String expectedRenamed = "var a$$module$test = 1;";

    String result = compiler.toSource(script);
    assertTrue("Should contain goog.provide", result.contains(expectedPrefix));
    assertTrue("Should contain module object initialization", result.contains(expectedVar));
    assertTrue("Should rename global variable", result.contains(expectedRenamed));
  }

  @Test(timeout = 4000)
  public void testRequireCallRewrite() {
    String js = "var other = require('./other');";
    String filename = "main.js";
    Node script = testProcess(js, filename, ".");

    String result = compiler.toSource(script);
    assertTrue("Should emit goog.require", result.contains("goog.require(\"module$other\");"));
    assertTrue("Should replace require call with module name identifier",
        result.contains("var other$$module$main = module$other;"));
  }

  @Test(timeout = 4000)
  public void testModuleExportsRewrite() {
    String js = "var num = 42; module.exports = num;";
    String filename = "exporter.js";
    Node script = testProcess(js, filename, ".");

    String result = compiler.toSource(script);
    assertTrue("Should set module$exports on the module object",
        result.contains("module$exporter.module$exports = num$$module$exporter;"));
    assertTrue("Should contain exports override block",
        result.contains("if(module$exporter.module$exports)module$exporter = module$exporter.module$exports;"));
  }

  @Test(timeout = 4000)
  public void testGetModuleReturnsModuleWhenReportingDependencies() {
    Compiler comp = new Compiler();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(comp, "dir", true);
    assertNull("Module should initially be null", pass.getModule());

    Node script = comp.parseTestCode("var x = 1;");
    script.setSourceFileName("dir" + File.separator + "foo.js");
    Node root = IR.root(script);
    pass.process(null, root);

    JSModule mod = pass.getModule();
    assertNotNull("Module should be created when reportDependencies=true", mod);
    assertEquals("module$foo", mod.getName());
  }

  @Test(timeout = 4000)
  public void testReportDependenciesFalseDoesNotSetModule() {
    Compiler comp = new Compiler();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(comp, "dir", false);

    Node script = comp.parseTestCode("var x = 1;");
    script.setSourceFileName("dir" + File.separator + "foo.js");
    Node root = IR.root(script);
    pass.process(null, root);

    assertNull("Module should be null when reportDependencies=false", pass.getModule());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Path Normalization
  // =========================================================================

  @Test(timeout = 4000)
  public void testToModuleNameStaticConversions() {
    // Leading ./ removed
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("." + File.separator + "foo.js"));
    // Trailing .js removed
    assertEquals("module$bar", ProcessCommonJSModules.toModuleName("bar.js"));
    // Subdirectories converted to $
    assertEquals("module$dir$sub$mod",
        ProcessCommonJSModules.toModuleName("dir" + File.separator + "sub" + File.separator + "mod.js"));
    // Dash replaced with underscore
    assertEquals("module$my_cool_lib", ProcessCommonJSModules.toModuleName("my-cool-lib.js"));
  }

  @Test(timeout = 4000)
  public void testToModuleNameWithRelativePaths() {
    String current = "foo" + File.separator + "bar.js";
    // Sibling resolution
    String req1 = "." + File.separator + "baz";
    assertEquals("module$foo$baz", ProcessCommonJSModules.toModuleName(req1, current));

    // Parent folder resolution
    String req2 = ".." + File.separator + "sibling";
    assertEquals("module$sibling", ProcessCommonJSModules.toModuleName(req2, current));

    // Non-relative require
    String req3 = "external_pkg";
    assertEquals("module$external_pkg", ProcessCommonJSModules.toModuleName(req3, current));
  }

  @Test(timeout = 4000)
  public void testConstructorFilenamePrefixTrailingSeparator() {
    Compiler comp = new Compiler();
    // Test constructor when prefix does NOT end with separator
    ProcessCommonJSModules passNoSep = new ProcessCommonJSModules(comp, "path");
    assertEquals("module$file", passNoSep.guessCJSModuleName("path" + File.separator + "file.js"));

    // Test constructor when prefix ALREADY ends with separator
    ProcessCommonJSModules passWithSep = new ProcessCommonJSModules(comp, "path" + File.separator);
    assertEquals("module$file", passWithSep.guessCJSModuleName("path" + File.separator + "file.js"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Targeting Defects4J Bug)
  // =========================================================================

  /**
   * CRITICAL DEFECT TEST:
   * When filenamePrefix is supplied (e.g., "base/"), visitScript strips it
   * via normalizeSourceName(script.getSourceFileName()) -> "foo.js" -> "module$foo",
   * BUT visitModuleExports() calls guessCJSModuleName(prop.getSourceFileName()) directly
   * WITHOUT normalizeSourceName, which yields "module$base$foo" instead of "module$foo"!
   *
   * This test enforces that the module.exports assignment matches the declared module identifier
   * when a filenamePrefix is used.
   */
  @Test(timeout = 4000)
  public void testDefectExportsWithFilenamePrefix() {
    String prefix = "base" + File.separator;
    String filename = prefix + "worker.js";
    String js = "module.exports = function() {};";

    Node script = testProcess(js, filename, prefix);
    String result = compiler.toSource(script);

    // Expected module name after prefix normalization is "module$worker"
    assertTrue("Script should provide module$worker",
        result.contains("goog.provide(\"module$worker\");"));
    assertTrue("Script should initialize var module$worker",
        result.contains("var module$worker = {};"));

    // Buggy implementation produces: "module$base$worker.module$exports = function() {};"
    // Correct implementation must produce: "module$worker.module$exports = function() {};"
    assertFalse("Bug detected: module.exports used non-normalized prefix path!",
        result.contains("module$base$worker"));
    assertTrue("module.exports must be rewritten using the normalized moduleName",
        result.contains("module$worker.module$exports = function(){};"));
  }

  /**
   * Defects4J testDash failure condition:
   * Modules with dashes in their names when exports are assigned.
   */
  @Test(timeout = 4000)
  public void testDefectDashWithExports() {
    String prefix = "root" + File.separator;
    String filename = prefix + "my-dash-module.js";
    String js = "exports.action = function() {};";

    Node script = testProcess(js, filename, prefix);
    String result = compiler.toSource(script);

    assertTrue("Dash should be transformed to underscore in module name",
        result.contains("goog.provide(\"module$my_dash_module\");"));
    assertTrue("exports keyword should be rewritten to module name",
        result.contains("module$my_dash_module.action = function(){};"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleScriptNodesThrowsException() {
    // ProcessCommonJSModules only supports one invocation per script node
    Compiler comp = new Compiler();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(comp, ".");

    Node script1 = comp.parseTestCode("var a = 1;");
    script1.setSourceFileName("a.js");
    Node script2 = comp.parseTestCode("var b = 2;");
    script2.setSourceFileName("b.js");

    Node root = IR.root(script1, script2);

    try {
      pass.process(null, root);
      fail("Expected IllegalArgumentException due to multiple script nodes");
    } catch (IllegalArgumentException expected) {
      assertTrue("Exception message should reflect single-invocation constraint",
          expected.getMessage().contains("ProcessCommonJSModules supports only one invocation"));
    }
  }

  @Test(timeout = 4000)
  public void testInvalidUriSyntaxThrowsRuntimeException() {
    try {
      // Illegal characters in URI (e.g. unescaped illegal characters)
      ProcessCommonJSModules.toModuleName("." + File.separator + "illegal:path\u0000", "test.js");
      fail("Expected RuntimeException wrapping URISyntaxException");
    } catch (RuntimeException expected) {
      assertTrue("Cause should be URISyntaxException",
          expected.getCause() instanceof java.net.URISyntaxException);
    }
  }

  // =========================================================================
  // Partition E: Scoping & Complex Var Renaming
  // =========================================================================

  @Test(timeout = 4000)
  public void testLocalVarsNotRenamedOnlyGlobals() {
    String js = "var globalVar = 10; function foo() { var localVar = 20; return localVar; }";
    String filename = "scoping.js";
    Node script = testProcess(js, filename, ".");

    String result = compiler.toSource(script);
    assertTrue("Global variable must be renamed with module suffix",
        result.contains("var globalVar$$module$scoping = 10;"));
    assertTrue("Local variable must not be renamed",
        result.contains("var localVar = 20;"));
  }

  @Test(timeout = 4000)
  public void testExportsNameDirectAssignment() {
    String js = "var exports = {}; exports.foo = 1;";
    String filename = "exports_assign.js";
    Node script = testProcess(js, filename, ".");

    String result = compiler.toSource(script);
    assertTrue("var exports should be rewritten to module name",
        result.contains("module$exports_assign.foo = 1;"));
  }
}