/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.ProcessClosurePrimitives
 *
 * Decision / Condition Coverage Targets:
 * 1. Token.CALL dispatch:
 *    - left is not GetProp, left.getFirstChild() not name, name != 'goog'
 *    - goog.base:
 *      * Direct call vs invalid call (not directly called)
 *      * 'this' as first argument vs missing/invalid first argument
 *      * Enclosing method lookup (constructor with goog.inherits vs prototype method vs not found)
 *      * Method name match vs mismatch
 *      * Enclosing scope in function decl, assign, var
 *    - goog.define:
 *      * verifyDefine: 2 arguments, first arg string valid qualified JS identifier,
 *        second arg present and last, @define JSDoc annotation present vs missing
 *      * Replacement of goog.define with declaration and source info
 *    - goog.require:
 *      * Single string argument vs missing arg vs non-string vs too many args
 *      * Provided vs unprovided namespace (unrecognized requires)
 *      * Explicitly provided vs implicitly provided
 *      * Cross-module require (moduleGraph dependsOn check)
 *      * Detaching require node when provided vs when missing
 *    - goog.provide:
 *      * Single string argument vs invalid JS tokens
 *      * Duplicate provide detection (explicitly provided twice vs implicit then explicit)
 *      * Prefix registration (e.g., 'a.b.c' registers 'a' and 'a.b')
 *      * Replacement: single name (var a = {}) vs dotted name (a.b = {})
 *      * Existing definition handling (var, assign, exprResult)
 *    - goog.exportSymbol:
 *      * Dotted symbol name vs non-dotted symbol name -> exportedVariables set
 *    - goog.addDependency:
 *      * Coding convention identifyTypeDeclarationCall, forwardDeclareType, replace with 0
 *    - goog.setCssNameMapping:
 *      * Missing argument, non-objectlit, extra arguments
 *      * Non-string keys/values in objectlit
 *      * Style: BY_PART (dash detection in keys), BY_WHOLE (composition check), invalid style
 * 2. Token.FUNCTION:
 *    - Global scope named function collision with provided namespace (FUNCTION_NAMESPACE_ERROR)
 *    - Function expression / inner scope ignores
 * 3. Token.GETPROP:
 *    - Dangling goog.base reference without call/assign
 * 4. Token.ASSIGN / Token.NAME / Token.EXPR_RESULT:
 *    - Candidate provide definitions, IS_NAMESPACE property, previous pass placeholder removal
 *    - Typedef definition handling (info.hasTypedefType())
 * 5. Diagnostic & Defect Zone:
 *    - Unrecognized require reporting: MISSING_PROVIDE_ERROR vs LATE_PROVIDE_ERROR
 *    - RequiresLevel.OFF vs RequiresLevel.ERROR / WARNING
 *    - Target Defect: Missing provide error on required namespace and verify require removal / error tracking
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class ProcessClosurePrimitivesGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Node parseAndProcess(String js, CheckLevel requiresLevel) {
    Node root = compiler.parseTestCode(js);
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, null, requiresLevel);
    pass.process(null, root);
    return root;
  }

  private Node parseAndProcess(String js) {
    return parseAndProcess(js, CheckLevel.ERROR);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testProvideTopLevelNamespaceDeclaration() {
    Node root = parseAndProcess("goog.provide('foo');");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());

    Node firstStatement = root.getFirstChild();
    assertNotNull(firstStatement);
    assertTrue(firstStatement.isVar());
    assertEquals("foo", firstStatement.getFirstChild().getString());
    assertTrue(firstStatement.getBooleanProp(Node.IS_NAMESPACE));
  }

  @Test(timeout = 4000)
  public void testProvideDottedNamespaceDeclaration() {
    Node root = parseAndProcess("goog.provide('foo.bar');");
    assertEquals(0, compiler.getErrorCount());

    // Expects: var foo = {}; foo.bar = {};
    Node firstStatement = root.getFirstChild();
    assertTrue(firstStatement.isVar());
    assertEquals("foo", firstStatement.getFirstChild().getString());

    Node secondStatement = firstStatement.getNext();
    assertTrue(secondStatement.isExprResult());
    assertTrue(secondStatement.getFirstChild().isAssign());
    assertEquals("foo.bar", secondStatement.getFirstChild().getFirstChild().getQualifiedName());
  }

  @Test(timeout = 4000)
  public void testProvideWithExistingCandidateDefinitionVar() {
    String js = "goog.provide('foo'); var foo = 10;";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    // goog.provide should be removed, leaving var foo = 10;
    Node first = root.getFirstChild();
    assertTrue(first.isVar());
    assertEquals("foo", first.getFirstChild().getString());
    assertNull(first.getNext());
  }

  @Test(timeout = 4000)
  public void testProvideWithExistingCandidateDefinitionAssign() {
    String js = "goog.provide('foo'); foo = {};";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());

    Node first = root.getFirstChild();
    assertTrue(first.isVar());
    assertEquals("foo", first.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testProvideAndRequireSuccess() {
    String js = "goog.provide('my.ns'); goog.require('my.ns');";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    // Require statement must be stripped
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      if (child.isExprResult() && child.getFirstChild().isCall()) {
        Node target = child.getFirstChild().getFirstChild();
        assertNotEquals("goog.require", target.getQualifiedName());
      }
    }
  }

  @Test(timeout = 4000)
  public void testExportSymbolNames() {
    String js = "goog.exportSymbol('myExport', 123); goog.exportSymbol('a.b.c', 456);";
    Node root = compiler.parseTestCode(js);
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, null, CheckLevel.ERROR);
    pass.process(null, root);

    Set<String> exported = pass.getExportedVariableNames();
    assertTrue(exported.contains("myExport"));
    assertTrue(exported.contains("a"));
    assertEquals(2, exported.size());
  }

  @Test(timeout = 4000)
  public void testAddDependencyCall() {
    String js = "goog.addDependency('foo.js', ['foo'], []);";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    Node first = root.getFirstChild();
    assertTrue(first.isExprResult());
    assertTrue(first.getFirstChild().isNumber());
    assertEquals(0.0, first.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptDelegation() {
    Node root = compiler.parseTestCode("goog.provide('hot.swap');");
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, null, CheckLevel.ERROR);
    pass.hotSwapScript(root, null);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Invalid Syntax Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testProvideWithNullArgument() {
    parseAndProcess("goog.provide();");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.NULL_ARGUMENT_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testProvideWithNonStringArgument() {
    parseAndProcess("goog.provide(123);");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testProvideWithTooManyArguments() {
    parseAndProcess("goog.provide('foo', 'bar');");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testProvideWithInvalidIdentifierName() {
    parseAndProcess("goog.provide('foo-bar');");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_PROVIDE_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExplicitProvide() {
    parseAndProcess("goog.provide('foo'); goog.provide('foo');");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testFunctionCollidesWithProvidedNamespace() {
    parseAndProcess("goog.provide('foo'); function foo() {}");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Fault)
  // =========================================================================

  /**
   * Targets the defect where an unrecognized goog.require must be reported
   * as MISSING_PROVIDE_ERROR when the namespace was never provided.
   */
  @Test(timeout = 4000)
  public void testUnrecognizedRequireReportsMissingProvideError() {
    parseAndProcess("goog.require('namespace.Class1');", CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.MISSING_PROVIDE_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertEquals("required \"namespace.Class1\" namespace never provided",
        compiler.getErrors()[0].description);
  }

  @Test(timeout = 4000)
  public void testLateProvideReportsLateProvideError() {
    parseAndProcess("goog.require('a.Late'); goog.provide('a.Late');", CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.LATE_PROVIDE_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertEquals("required \"a.Late\" namespace not provided yet",
        compiler.getErrors()[0].description);
  }

  @Test(timeout = 4000)
  public void testBrokenRequireWithChecksOffLeavesRequire() {
    Node root = parseAndProcess("goog.require('unprovided.ns');", CheckLevel.OFF);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(root.getFirstChild());
    assertTrue(root.getFirstChild().isExprResult());
    assertTrue(root.getFirstChild().getFirstChild().isCall());
  }

  // =========================================================================
  // Partition D: Goog.define, Goog.base, and CssNameMapping Edge Branches
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefineValidReplacement() {
    String js = "/** @define {boolean} */ goog.define('FLAG', true);";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());

    Node first = root.getFirstChild();
    assertTrue(first.isVar());
    Node nameNode = first.getFirstChild();
    assertEquals("FLAG", nameNode.getString());
    assertTrue(nameNode.getFirstChild().isTrue());
  }

  @Test(timeout = 4000)
  public void testDefineMissingAnnotation() {
    String js = "goog.define('FLAG', true);";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.MISSING_DEFINE_ANNOTATION.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDefineInvalidName() {
    String js = "/** @define {boolean} */ goog.define('INVALID-NAME', true);";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_DEFINE_NAME_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testBaseClassConstructorCall() {
    String js =
        "function Base() {} " +
        "function Sub() { goog.base(this); } " +
        "goog.inherits(Sub, Base);";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());

    // Expect goog.base(this) -> Base.call(this)
    Node subFn = root.getFirstChild().getNext();
    Node callNode = subFn.getLastChild().getFirstChild().getFirstChild();
    assertEquals("Base.call", callNode.getFirstChild().getQualifiedName());
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodCall() {
    String js =
        "function Foo() {} " +
        "Foo.prototype.bar = function(x) { goog.base(this, 'bar', x); };";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());

    Node assignExpr = root.getFirstChild().getNext();
    Node fn = assignExpr.getFirstChild().getLastChild();
    Node callNode = fn.getLastChild().getFirstChild().getFirstChild();
    assertEquals("Foo.superClass_.bar.call", callNode.getFirstChild().getQualifiedName());
  }

  @Test(timeout = 4000)
  public void testBaseClassWithoutThisFails() {
    String js = "function Sub() { goog.base(); }";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertTrue(compiler.getErrors()[0].description.contains("First argument must be 'this'"));
  }

  @Test(timeout = 4000)
  public void testBaseClassMissingEnclosingMethodFails() {
    String js = "goog.base(this);";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertTrue(compiler.getErrors()[0].description.contains("Could not find enclosing method"));
  }

  @Test(timeout = 4000)
  public void testBaseClassMethodNameMismatchFails() {
    String js =
        "function Foo() {} " +
        "Foo.prototype.bar = function() { goog.base(this, 'baz'); };";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertTrue(compiler.getErrors()[0].description.contains("Enclosing method does not match baz"));
  }

  @Test(timeout = 4000)
  public void testDanglingGoogBaseReference() {
    String js = "var x = goog.base;";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR.key,
        compiler.getErrors()[0].getType().key);
    assertTrue(compiler.getErrors()[0].description.contains("May only be called directly"));
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingValidByPart() {
    String js = "goog.setCssNameMapping({'button': 'b', 'active': 'a'}, 'BY_PART');";
    parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getCssRenamingMap());
    assertEquals("b", compiler.getCssRenamingMap().get("button"));
    assertEquals("a", compiler.getCssRenamingMap().get("active"));
    assertEquals("unknown", compiler.getCssRenamingMap().get("unknown"));
    assertEquals(CssRenamingMap.Style.BY_PART, compiler.getCssRenamingMap().getStyle());
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingByPartWithDashWarns() {
    String js = "goog.setCssNameMapping({'btn-active': 'ba'}, 'BY_PART');";
    parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(ProcessClosurePrimitives.INVALID_CSS_RENAMING_MAP.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingByWhole() {
    String js = "goog.setCssNameMapping({'a': 'x', 'b': 'y', 'a-b': 'x-y'}, 'BY_WHOLE');";
    parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals("x-y", compiler.getCssRenamingMap().get("a-b"));
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingByWholeInconsistentWarns() {
    String js = "goog.setCssNameMapping({'a': 'x', 'b': 'y', 'a-b': 'inconsistent'}, 'BY_WHOLE');";
    parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(ProcessClosurePrimitives.INVALID_CSS_RENAMING_MAP.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingInvalidStyle() {
    String js = "goog.setCssNameMapping({'a': 'b'}, 'UNKNOWN_STYLE');";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.INVALID_STYLE_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testSetCssNameMappingNonStringValues() {
    String js = "goog.setCssNameMapping({'a': 123});";
    parseAndProcess(js);
    assertEquals(1, compiler.getErrorCount());
    assertEquals(ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition E: SymbolTable & Multi-Module / Implicit Hierarchy Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testWithPreprocessorSymbolTable() {
    PreprocessorSymbolTable table = new PreprocessorSymbolTable(new Node(Token.SCRIPT));
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, table, CheckLevel.ERROR);
    Node root = compiler.parseTestCode("goog.provide('a.b.c'); goog.require('a.b.c');");
    pass.process(null, root);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testTypedefHandlingForProvidedName() {
    String js = "goog.provide('my.Type'); /** @typedef {number} */ my.Type;";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCrossModuleDependencyValidation() {
    JSModule[] modules = new JSModule[2];
    modules[0] = new JSModule("m0");
    modules[1] = new JSModule("m1");
    // m1 does not depend on m0
    JSModuleGraph graph = new JSModuleGraph(modules);
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node m0Node = compiler.parseTestCode("goog.provide('mod.feature');");
    Node m1Node = compiler.parseTestCode("goog.require('mod.feature');");

    Node root = IR.root(m0Node, m1Node);

    // Run traversal per-module manually to feed module contexts
    ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, null, CheckLevel.WARNING);
    NodeTraversal.traverse(compiler, m0Node, pass);
    NodeTraversal.traverse(compiler, m1Node, pass);

    // Both nodes visited, now verify requires
    pass.process(null, root);

    // Cross-module warning expected when requires cross without dependency
    assertEquals(1, compiler.getWarningCount());
    assertEquals(ProcessClosurePrimitives.XMODULE_REQUIRE_ERROR.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testImplicitProvideOrderingWithPrefixes() {
    String js = "goog.provide('alpha.beta.Gamma');";
    Node root = parseAndProcess(js);
    assertEquals(0, compiler.getErrorCount());

    // Expects: var alpha = {}; alpha.beta = {}; alpha.beta.Gamma = {};
    Node n1 = root.getFirstChild();
    assertTrue(n1.isVar());
    assertEquals("alpha", n1.getFirstChild().getString());

    Node n2 = n1.getNext();
    assertTrue(n2.isExprResult());
    assertEquals("alpha.beta", n2.getFirstChild().getFirstChild().getQualifiedName());

    Node n3 = n2.getNext();
    assertTrue(n3.isExprResult());
    assertEquals("alpha.beta.Gamma", n3.getFirstChild().getFirstChild().getQualifiedName());
  }
}