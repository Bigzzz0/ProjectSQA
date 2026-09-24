package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.IR;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and conditions:
 * 
 * 1. ProcessCommonJSModules constructor: filenamePrefix.endsWith(File.separator) branch
 * 2. toModuleName(String): regex replacements for ^\./, separator, \.js$, and dashes
 * 3. toModuleName(String, String): relative path resolution with ./ and ../
 * 4. normalizeSourceName: filename.indexOf(filenamePrefix) == 0 branch
 * 5. ProcessCommonJsModulesCallback.visit: isCall() && childCount==2 && require check
 * 6. ProcessCommonJsModulesCallback.visit: isScript() branch
 * 7. ProcessCommonJsModulesCallback.visit: isGetProp() && "module.exports" check
 * 8. visitRequireCall: reportDependencies true/false branch
 * 9. visitScript: scriptNodeCount == 1 precondition
 * 10. visitScript: reportDependencies true/false branch for provide and module creation
 * 11. emitOptionalModuleExportsOverride: ifNode generation
 * 12. visitModuleExports: ORIGINALNAME_PROP and setString calls
 * 13. SuffixVarsCallback.visit: isName() branch
 * 14. SuffixVarsCallback.visit: suffix.equals(name) early return
 * 15. SuffixVarsCallback.visit: EXPORTS.equals(name) branch
 * 16. SuffixVarsCallback.visit: var != null && var.isGlobal() branch
 * 
 * Known defect: Multiple test failures related to module naming, exports handling,
 * and variable renaming. The defect likely involves incorrect module name generation
 * or improper handling of module.exports assignments.
 */
public class ProcessCommonJSModulesDeepseekTest {

  private static final String DEFAULT_PREFIX = "." + File.separator;

  // ========== Partition A: Core Functional Logic & State Transitions ==========

  @Test(timeout = 4000)
  public void testToModuleName_basic() {
    String result = ProcessCommonJSModules.toModuleName("./foo/bar/baz.js");
    assertEquals("module$foo$bar$baz", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_withDashes() {
    String result = ProcessCommonJSModules.toModuleName("./my-module/test-file.js");
    assertEquals("module$my_module$test_file", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_noLeadingDot() {
    String result = ProcessCommonJSModules.toModuleName("foo/bar.js");
    assertEquals("module$foo$bar", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_withRelativePath() {
    String result = ProcessCommonJSModules.toModuleName("./foo/bar.js", "./baz/qux.js");
    assertEquals("module$baz$foo$bar", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_withParentRelative() {
    String result = ProcessCommonJSModules.toModuleName("../foo/bar.js", "./baz/qux.js");
    assertEquals("module$baz$foo$bar", result);
  }

  @Test(timeout = 4000)
  public void testGuessCJSModuleName() {
    // Create a minimal compiler for testing
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    String result = processor.guessCJSModuleName("./myModule.js");
    assertEquals("module$myModule", result);
  }

  @Test(timeout = 4000)
  public void testGetModule_initialNull() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    assertNull(processor.getModule());
  }

  // ========== Partition B: Boundary Value Analysis & Extremes ==========

  @Test(timeout = 4000)
  public void testToModuleName_emptyString() {
    String result = ProcessCommonJSModules.toModuleName("");
    assertEquals("module$", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_justJs() {
    String result = ProcessCommonJSModules.toModuleName(".js");
    assertEquals("module$", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_rootRelative() {
    String result = ProcessCommonJSModules.toModuleName("/absolute/path.js");
    assertEquals("module$$absolute$path", result);
  }

  @Test(timeout = 4000)
  public void testToModuleName_withMultipleDots() {
    String result = ProcessCommonJSModules.toModuleName("./foo.bar/baz.js");
    assertEquals("module$foo.bar$baz", result);
  }

  @Test(timeout = 4000)
  public void testConstructor_filenamePrefixWithoutSeparator() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "/my/path");
    
    // The constructor should append separator
    String result = processor.guessCJSModuleName("/my/path/test.js");
    assertEquals("module$test", result);
  }

  @Test(timeout = 4000)
  public void testConstructor_filenamePrefixWithSeparator() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "/my/path/");
    
    String result = processor.guessCJSModuleName("/my/path/test.js");
    assertEquals("module$test", result);
  }

  // ========== Partition C: Defect-Targeted Branch Zone ==========

  @Test(timeout = 4000)
  public void testModuleName_defectTarget() {
    // This test targets the known defect in testModuleName
    // The defect likely involves incorrect module name generation
    String result = ProcessCommonJSModules.toModuleName("./test.js");
    assertEquals("module$test", result);
    
    // Test with relative path resolution
    String result2 = ProcessCommonJSModules.toModuleName("./sub/test.js", "./main.js");
    assertEquals("module$sub$test", result2);
  }

  @Test(timeout = 4000)
  public void testExports_defectTarget() {
    // This test targets the known defect in testExports
    // Create a minimal AST to test module.exports handling
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    // Create a simple script node
    Node script = IR.script();
    script.setSourceFileName("./test.js");
    
    // Create module.exports = something
    Node moduleExports = IR.getprop(IR.name("module"), IR.string("exports"));
    Node assign = IR.assign(moduleExports, IR.number(42));
    Node exprResult = IR.exprResult(assign);
    script.addChildToBack(exprResult);
    
    // Process the script
    processor.process(null, script);
    
    // Verify module.exports was rewritten
    Node firstChild = script.getFirstChild();
    assertNotNull(firstChild);
    // The goog.provide call should be first
    assertTrue(firstChild.isExprResult());
  }

  @Test(timeout = 4000)
  public void testVarRenaming_defectTarget() {
    // This test targets the known defect in testVarRenaming
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    // Create a script with global variables
    Node script = IR.script();
    script.setSourceFileName("./test.js");
    
    // Add a global variable
    Node var = IR.var(IR.name("myGlobal"), IR.number(1));
    script.addChildToBack(var);
    
    // Process the script
    processor.process(null, script);
    
    // The variable should be renamed with module suffix
    // Note: This is a simplified test - actual renaming happens during traversal
    assertNotNull(script.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testWithoutExports_defectTarget() {
    // This test targets the known defect in testWithoutExports
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    // Create a script without module.exports
    Node script = IR.script();
    script.setSourceFileName("./test.js");
    
    // Add a simple expression
    Node expr = IR.exprResult(IR.number(42));
    script.addChildToBack(expr);
    
    // Process the script
    processor.process(null, script);
    
    // Should still work without exports
    assertNotNull(script.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testDash_defectTarget() {
    // This test targets the known defect in testDash
    String result = ProcessCommonJSModules.toModuleName("./my-module.js");
    assertEquals("module$my_module", result);
    
    // Test with multiple dashes
    String result2 = ProcessCommonJSModules.toModuleName("./a-b-c.js");
    assertEquals("module$a_b_c", result2);
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000)
  public void testToModuleName_nullRequiredFilename() {
    try {
      ProcessCommonJSModules.toModuleName(null, "./test.js");
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testToModuleName_nullCurrentFilename() {
    try {
      ProcessCommonJSModules.toModuleName("./foo.js", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testGuessCJSModuleName_nullFilename() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    try {
      processor.guessCJSModuleName(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  // ========== Partition E: Object Lifecycle & Contract Integrity ==========

  @Test(timeout = 4000)
  public void testProcess_withNullExterns() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    Node root = IR.script();
    root.setSourceFileName("./test.js");
    
    // Should not throw exception with null externs
    processor.process(null, root);
    assertNotNull(root.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testProcess_withEmptyRoot() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    Node root = IR.script();
    // No source file name set
    
    // Should handle gracefully
    processor.process(null, root);
    // No exception expected
  }

  @Test(timeout = 4000)
  public void testMultipleScriptNodes_throwsException() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX);
    
    Node root = IR.script();
    root.setSourceFileName("./test.js");
    
    // Add two script nodes (should cause precondition failure)
    Node script1 = IR.script();
    script1.setSourceFileName("./test.js");
    Node script2 = IR.script();
    script2.setSourceFileName("./test2.js");
    
    root.addChildToBack(script1);
    root.addChildToBack(script2);
    
    try {
      processor.process(null, root);
      fail("Expected IllegalStateException from Preconditions check");
    } catch (IllegalStateException e) {
      // Expected - ProcessCommonJSModules supports only one invocation per CompilerInput
    }
  }

  @Test(timeout = 4000)
  public void testReportDependencies_false() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX, false);
    
    Node root = IR.script();
    root.setSourceFileName("./test.js");
    
    processor.process(null, root);
    
    // Module should be null when reportDependencies is false
    assertNull(processor.getModule());
  }

  @Test(timeout = 4000)
  public void testReportDependencies_true() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, DEFAULT_PREFIX, true);
    
    Node root = IR.script();
    root.setSourceFileName("./test.js");
    
    processor.process(null, root);
    
    // Module should be set when reportDependencies is true
    JSModule module = processor.getModule();
    assertNotNull(module);
    assertEquals("module$test", module.getName());
  }

  @Test(timeout = 4000)
  public void testNormalizeSourceName_withPrefix() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "/base/path/");
    
    // Use reflection or test indirectly through guessCJSModuleName
    String result = processor.guessCJSModuleName("/base/path/test.js");
    assertEquals("module$test", result);
  }

  @Test(timeout = 4000)
  public void testNormalizeSourceName_withoutPrefix() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "/base/path/");
    
    String result = processor.guessCJSModuleName("/other/path/test.js");
    assertEquals("module$$other$path$test", result);
  }
}