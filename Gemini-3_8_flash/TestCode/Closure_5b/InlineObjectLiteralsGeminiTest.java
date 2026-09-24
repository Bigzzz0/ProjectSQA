package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.InlineObjectLiterals
 * Primary Defects4J Failure: InlineObjectLiteralsTest::testNoInlineDeletedProperties
 *
 * Decision / Condition Matrix:
 * 1. isVarInlineForbidden(Var var):
 *    - var.isGlobal()                                -> TRUE: do not inline global objects.
 *    - var.isExtern()                                -> TRUE: do not inline externs.
 *    - compiler.getCodingConvention().isExported()   -> TRUE: do not inline exported variables.
 *    - RENAME_PROPERTY_FUNCTION_NAME                 -> TRUE: do not inline JSCompiler_renameProperty.
 *    - staleVars.contains(var)                       -> TRUE: do not inline already invalidated vars.
 *
 * 2. isInlinableObject(List<Reference> refs):
 *    - parent.isGetProp():
 *      * gramps.isCall() && gramps.getFirstChild() == parent -> Call target may use object as 'this' -> FALSE.
 *      * parent.getParent().isDelProp() [DEFECT ZONE]        -> Deleting property has different semantics -> FALSE.
 *      * !validProperties.contains(propName):
 *        - isVarOrSimpleAssignLhs(parent, gramps)            -> TRUE: new property assigned -> validProperties.add.
 *        - else                                              -> FALSE: reading undefined/prototype prop -> bail out.
 *    - !isVarOrAssignExprLhs(name)                           -> Full object reference / arg / return -> FALSE.
 *    - val == null                                           -> Uninitialized var -> continue.
 *    - !val.isObjectLit()                                    -> Non-object literal reassignment -> FALSE.
 *    - child.isGetterDef() || child.isSetterDef()            -> ES5 getter/setter -> FALSE.
 *    - refNode == childVal                                   -> Self-referential assignment -> FALSE.
 *
 * 3. replaceAssignmentExpression(Var, Reference, Map<String, String>):
 *    - nodes.isEmpty()                                       -> Replace with IR.trueNode().
 *    - nodes.size() > 0                                      -> Comma-separated assignment tree ending in true.
 *    - properties missing in reassignment                    -> Replaced with undefined (void 0).
 *
 * 4. splitObject(Var, Reference, Reference, ReferenceCollection):
 *    - defined && init.getParent().isVar()                   -> Variable initialization split in-place.
 *    - !defined || !init.getParent().isVar()                 -> Split moved to function start; assignment converted.
 * -------------------------------------------------------------------------------------------------------
 */
public class InlineObjectLiteralsGeminiTest {

  private String compile(String js) {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node externs = new Node(Token.SCRIPT);
    Node main = compiler.parseTestCode(js);
    assertEquals("Compilation errors encountered", 0, compiler.getErrorCount());

    final int[] id = {0};
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(id[0]++);
      }
    };

    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, idSupplier);
    pass.process(externs, main);

    return compiler.toSource(main);
  }

  private void test(String js, String expected) {
    String actual = compile(js);

    Compiler expectedCompiler = new Compiler();
    expectedCompiler.initCompilerOptionsIfTesting();
    Node expectedNode = expectedCompiler.parseTestCode(expected);
    String expectedSource = expectedCompiler.toSource(expectedNode);

    assertEquals(expectedSource, actual);
  }

  private void testSame(String js) {
    test(js, js);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleObjectInliningSingleProperty() {
    test(
        "function f() { var a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }");
  }

  @Test(timeout = 4000)
  public void testSimpleObjectInliningMultipleProperties() {
    test(
        "function f() { var a = {x: 1, y: 2}; return a.x + a.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test(timeout = 4000)
  public void testObjectPropertyWrite() {
    test(
        "function f() { var a = {x: 1}; a.x = 2; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2; return JSCompiler_object_inline_x_0; }");
  }

  @Test(timeout = 4000)
  public void testObjectNewPropertyAssignment() {
    test(
        "function f() { var a = {x: 1}; a.y = 2; return a.x + a.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1; JSCompiler_object_inline_y_1 = 2; return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1; }");
  }

  @Test(timeout = 4000)
  public void testObjectReassignmentAllProperties() {
    test(
        "function f() { var a = {x: 1}; a = {x: 2}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = 2, true; return JSCompiler_object_inline_x_0; }");
  }

  @Test(timeout = 4000)
  public void testObjectPropertyPassedAsFunctionArgument() {
    test(
        "function f() { var a = {x: 1}; g(a.x); }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; g(JSCompiler_object_inline_x_0); }");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnusedObjectLiteral() {
    test(
        "function f() { var a = {x: 1}; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; }");
  }

  @Test(timeout = 4000)
  public void testEmptyObjectLiteralReassignment() {
    test(
        "function f() { var a = {}; a = {}; }",
        "function f() { true; }");
  }

  @Test(timeout = 4000)
  public void testObjectReassignmentMissingPropertyBecomesUndefined() {
    test(
        "function f() { var a = {x: 1}; a = {}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; JSCompiler_object_inline_x_0 = void 0, true; return JSCompiler_object_inline_x_0; }");
  }

  @Test(timeout = 4000)
  public void testObjectDeclaredWithoutInitialAssignment() {
    test(
        "function f() { var a; a = {x: 1}; return a.x; }",
        "function f() { var JSCompiler_object_inline_x_0; JSCompiler_object_inline_x_0 = 1, true; return JSCompiler_object_inline_x_0; }");
  }

  @Test(timeout = 4000)
  public void testMultiplePropertiesReassignmentCommaTree() {
    test(
        "function f() { var a = {x: 1, y: 2, z: 3}; a = {x: 4, y: 5, z: 6}; return a.x + a.y + a.z; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var JSCompiler_object_inline_y_1 = 2; var JSCompiler_object_inline_z_2 = 3; " +
            "JSCompiler_object_inline_x_0 = 4, JSCompiler_object_inline_y_1 = 5, JSCompiler_object_inline_z_2 = 6, true; " +
            "return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1 + JSCompiler_object_inline_z_2; }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
  // =========================================================================

  /**
   * Defects4J Target Defect:
   * com.google.javascript.jscomp.InlineObjectLiteralsTest::testNoInlineDeletedProperties
   *
   * Deleting an object property (e.g. `delete foo.bar`) has fundamentally different semantics
   * from deleting a variable (`delete JSCompiler_object_inline_bar_0`). Variables with deleted
   * properties MUST NOT be inlined.
   */
  @Test(timeout = 4000)
  public void testNoInlineDeletedProperties() {
    testSame("function f() { var foo = {bar: 1}; delete foo.bar; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineDeletedPropertiesMultipleProps() {
    testSame("function f() { var foo = {bar: 1, baz: 2}; delete foo.bar; return foo.baz; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineDeletedPropertiesInCondition() {
    testSame("function f(cond) { var foo = {bar: 1}; if (cond) { delete foo.bar; } return foo.bar; }");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testNoInlineMethodCallTargetUsingThis() {
    testSame("function f() { var a = {fn: function() { return 1; }}; return a.fn(); }");
  }

  @Test(timeout = 4000)
  public void testNoInlineFullObjectReferenceAsArgument() {
    testSame("function f() { var a = {x: 1}; g(a); }");
  }

  @Test(timeout = 4000)
  public void testNoInlineFullObjectReferenceReturned() {
    testSame("function f() { var a = {x: 1}; return a; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineUnknownPropertyRead() {
    testSame("function f() { var a = {x: 1}; return a.y; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineNonObjectReassignment() {
    testSame("function f() { var a = {x: 1}; a = 2; return a.x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineSelfReferentialObjectLiteral() {
    testSame("function f() { var x = {a: x.a}; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineSelfReferentialObjectLiteralOrderable() {
    testSame("function f() { var x = {a: 1, b: x.a}; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineES5Getter() {
    testSame("function f() { var a = { get x() { return 1; } }; return a.x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineES5Setter() {
    testSame("function f() { var a = { set x(v) { } }; a.x = 1; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineGlobalVariables() {
    testSame("var a = {x: 1}; function f() { return a.x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineSpecialRenamePropertyFunctionName() {
    testSame("function f() { var JSCompiler_renameProperty = {x: 1}; return JSCompiler_renameProperty.x; }");
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testVarPrefixConstantValue() {
    assertEquals("JSCompiler_object_inline_", InlineObjectLiterals.VAR_PREFIX);
  }

  @Test(timeout = 4000)
  public void testEmptyScriptDoesNotThrow() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    Node externs = new Node(Token.SCRIPT);
    Node root = compiler.parseTestCode("");

    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, new Supplier<String>() {
      @Override
      public String get() {
        return "0";
      }
    });

    pass.process(externs, root);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBlacklistVarReferencesInTreeDoesNotInlineStaleVars() {
    test(
        "function f() { var a = {x: 1}; var b = {y: a.x}; return b.y; }",
        "function f() { var JSCompiler_object_inline_x_0 = 1; var b = {y: JSCompiler_object_inline_x_0}; return b.y; }");
  }
}