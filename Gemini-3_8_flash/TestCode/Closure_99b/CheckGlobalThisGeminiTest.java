package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Decision Point / Condition                        | Target Input / Scenario           | Expected Branch Path
 * ----------------------------------------------------------------------------------------------------
 * 1. n.getType() == Token.FUNCTION
 *    - jsDoc.isConstructor()                        | /** @constructor * / function F()  | shouldTraverse -> false
 *    - jsDoc.hasThisType()                          | /** @this {Foo} * / function f()   | shouldTraverse -> false
 *    - jsDoc.isOverride()                           | /** @override * / function f()     | shouldTraverse -> false
 *    - jsDoc.isInterface() (DEFECT: Closure/D4J)    | /** @interface * / function IFoo() | SHOULD -> false, BUG -> true
 *    - parent is Token.BLOCK / SCRIPT / NAME / ASSIGN| function f() { this.x = 1; }      | shouldTraverse -> true
 *    - parent is Token.RETURN / CALL / ARRAYLIT     | return function() { this.x = 1; };| shouldTraverse -> false
 * ----------------------------------------------------------------------------------------------------
 * 2. parent.getType() == Token.ASSIGN
 *    - n == lhs (outer/inner nested assignments)    | (a = this).b = 1;                 | assignLhsChild tracking
 *    - n != lhs && lhs.getLastChild() == "prototype"| Foo.prototype = { bar: ... };     | shouldTraverse -> false
 *    - n != lhs && lhs.getQualifiedName().contains  | Foo.prototype.bar = function()... | shouldTraverse -> false
 *      (".prototype.") [DEFECT: Subproperty]        | Foo.prototype.m.prop = fn...      | SHOULD -> true, BUG -> false
 *    - n != lhs && lhs is GETELEM [DEFECT: Bracket] | a.b.c.prototype['m'] = fn...      | SHOULD -> false, BUG -> true
 * ----------------------------------------------------------------------------------------------------
 * 3. visit: n.getType() == Token.THIS
 *    - assignLhsChild != null                       | this.foo = 3;                     | report error (LHS)
 *    - NodeUtil.isGet(parent)                       | var x = this.foo;                 | report error (Property access)
 *    - neither (e.g. standalone this assignment)    | var x = this;                     | do not report
 *    - n == assignLhsChild                          | post-order visit of assign LHS    | reset assignLhsChild = null
 * ----------------------------------------------------------------------------------------------------
 * 4. getFunctionJsDocInfo
 *    - direct jsdoc on FUNCTION node                | /** @constructor * / function F()  | returned directly
 *    - jsdoc on parent NAME                         | var /** @constructor * / F = fn() | found on parent
 *    - jsdoc on grandparent VAR                     | /** @constructor * / var F = fn() | found on grandparent
 *    - jsdoc on parent ASSIGN                       | /** @constructor * / F = fn()     | found on parent assign
 * ====================================================================================================
 */
public class CheckGlobalThisGeminiTest {

  private Compiler runCheck(String js, CheckLevel level) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckGlobalThis callback = new CheckGlobalThis(compiler, level);
    NodeTraversal.traverse(compiler, root, callback);
    return compiler;
  }

  // ==================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testConstructorFunctionDoesNotTriggerGlobalThis() {
    Compiler compiler = runCheck("/** @constructor */ function MyClass() { this.x = 10; }", CheckLevel.ERROR);
    assertEquals("Constructor functions should not flag 'this'", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithThisAnnotationDoesNotTrigger() {
    Compiler compiler = runCheck("/** @this {MyClass} */ function helper() { this.x = 10; }", CheckLevel.ERROR);
    assertEquals("Functions with @this annotation should not flag 'this'", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithOverrideAnnotationDoesNotTrigger() {
    Compiler compiler = runCheck("/** @override */ function overridden() { this.x = 10; }", CheckLevel.ERROR);
    assertEquals("Functions with @override annotation should not flag 'this'", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodAssignmentDoesNotTrigger() {
    Compiler compiler = runCheck("MyClass.prototype.foo = function() { this.val = 42; };", CheckLevel.ERROR);
    assertEquals("Assignment to standard prototype property should not flag 'this'", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPrototypeObjectLiteralDoesNotTrigger() {
    Compiler compiler = runCheck("MyClass.prototype = { method: function() { this.val = 42; } };", CheckLevel.ERROR);
    assertEquals("Assignment of object literal to prototype should not flag 'this'", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDangerousGlobalThisInRootAssignmentTriggersError() {
    Compiler compiler = runCheck("this.globalProp = 99;", CheckLevel.ERROR);
    assertEquals("Global 'this' on LHS of assignment must be reported", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDangerousGlobalThisInUnannotatedFunction() {
    Compiler compiler = runCheck("function regularFunction() { this.dangerous = 1; }", CheckLevel.ERROR);
    assertEquals("Global 'this' inside an unannotated function must be reported", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDangerousGlobalThisPropertyRead() {
    Compiler compiler = runCheck("var x = this.foo;", CheckLevel.ERROR);
    assertEquals("Property access on global 'this' must be reported", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDangerousGlobalThisElementRead() {
    Compiler compiler = runCheck("var x = this['foo'];", CheckLevel.ERROR);
    assertEquals("Element access on global 'this' must be reported", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  // ==================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptDoesNotProduceErrors() {
    Compiler compiler = runCheck("", CheckLevel.ERROR);
    assertEquals("Empty script should produce zero errors", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBareThisWithoutPropertyAccessOrAssignLhsIgnored() {
    Compiler compiler = runCheck("var a = this;", CheckLevel.ERROR);
    assertEquals("Assigning bare 'this' to variable without property access should not be flagged", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBareThisInBinaryExpressionIgnored() {
    Compiler compiler = runCheck("var same = (a === this);", CheckLevel.ERROR);
    assertEquals("Bare 'this' comparison should not be flagged", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testJsDocPropagationFromVarStatement() {
    Compiler compiler = runCheck("/** @constructor */ var A = function() { this.x = 1; };", CheckLevel.ERROR);
    assertEquals("JSDoc on VAR statement should propagate to function", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testJsDocPropagationFromNameNode() {
    Compiler compiler = runCheck("var /** @constructor */ A = function() { this.x = 1; };", CheckLevel.ERROR);
    assertEquals("JSDoc on NAME node should propagate to function", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testJsDocPropagationFromAssignNode() {
    Compiler compiler = runCheck("/** @constructor */ A = function() { this.x = 1; };", CheckLevel.ERROR);
    assertEquals("JSDoc on ASSIGN node should propagate to function", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNestedAssignLhsChildPreservation() {
    // (a = this).property = 5;
    // 'this' is on the RHS of the inner assign, but on the LHS of the outer property access
    Compiler compiler = runCheck("(a = this).property = 5;", CheckLevel.ERROR);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMultipleThisAssignmentsInSingleScript() {
    Compiler compiler = runCheck("this.a = 1; this.b = 2; this.c = 3;", CheckLevel.ERROR);
    assertEquals("Three separate global 'this' writes should report 3 errors", 3, compiler.getErrorCount());
  }

  // ==================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ==================================================================================================

  /**
   * Targets Defects4J CheckGlobalThisTest::testInterface1 failure.
   * Functions annotated with @interface must not report 'this' usage.
   */
  @Test(timeout = 4000)
  public void testInterface1() {
    Compiler compiler = runCheck("/** @interface */ function Foo() { /** @type {number} */ this.m; }", CheckLevel.ERROR);
    assertEquals("Should not report global this in an @interface definition", 0, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J CheckGlobalThisTest::testMethod4 failure.
   * Prototype method assignment using bracket notation (GETELEM) must not report 'this'.
   */
  @Test(timeout = 4000)
  public void testMethod4() {
    Compiler compiler = runCheck("a.b.c.prototype['foo'] = function() { this.foo = 3; };", CheckLevel.ERROR);
    assertEquals("Should not report global this in prototype element assignment", 0, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J CheckGlobalThisTest::testPropertyOfMethod failure.
   * Assigning a function to a subproperty of a prototype property (property of a method)
   * does not bind 'this' to the class instance and MUST be flagged.
   */
  @Test(timeout = 4000)
  public void testPropertyOfMethod() {
    Compiler compiler = runCheck("Foo.prototype.method.prop = function() { this.m = 1; };", CheckLevel.ERROR);
    assertEquals("Should report global this when assigning to a property of a prototype method", 1, compiler.getErrorCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getErrors()[0].getType());
  }

  // ==================================================================================================
  // Partition D: Exception, Skip Paths & Defensive Guards
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testFunctionWithNonTargetParentSkipped() {
    // Function passed as call argument has CALL parent, which is not BLOCK/SCRIPT/NAME/ASSIGN
    Compiler compiler = runCheck("doSomething(function() { this.x = 1; });", CheckLevel.ERROR);
    assertEquals("Anonymous function as argument should not be traversed for global this", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInsideArrayLiteralSkipped() {
    Compiler compiler = runCheck("var arr = [function() { this.x = 1; }];", CheckLevel.ERROR);
    assertEquals("Function expression inside array literal should not be traversed", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionInReturnStatementSkipped() {
    Compiler compiler = runCheck("function outer() { return function() { this.x = 1; }; }", CheckLevel.ERROR);
    assertEquals("Function expression in return statement should not be traversed", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testLevelWarningReportsWarningsInsteadOfErrors() {
    Compiler compiler = runCheck("this.x = 1;", CheckLevel.WARNING);
    assertEquals("CheckLevel.WARNING should produce 0 errors", 0, compiler.getErrorCount());
    assertEquals("CheckLevel.WARNING should produce 1 warning", 1, compiler.getWarningCount());
    assertEquals(CheckGlobalThis.GLOBAL_THIS, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testLevelOffSuppressesReports() {
    Compiler compiler = runCheck("this.x = 1;", CheckLevel.OFF);
    assertEquals("CheckLevel.OFF should produce 0 errors", 0, compiler.getErrorCount());
    assertEquals("CheckLevel.OFF should produce 0 warnings", 0, compiler.getWarningCount());
  }

  // ==================================================================================================
  // Partition E: Direct AST Method Invocations & Defensive Invariants
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testDirectCallbackShouldTraverseWithNullParent() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);
    Node standaloneNode = new Node(Token.EMPTY);
    assertTrue("shouldTraverse should return true for root node with null parent",
        callback.shouldTraverse(null, standaloneNode, null));
  }

  @Test(timeout = 4000)
  public void testDirectCallbackVisitWithNonThisNode() {
    Compiler compiler = new Compiler();
    CheckGlobalThis callback = new CheckGlobalThis(compiler, CheckLevel.ERROR);
    Node nameNode = Node.newString(Token.NAME, "foo");
    callback.visit(null, nameNode, null);
    assertEquals("Visiting a non-THIS node must not report anything", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDiagnosticTypeProperties() {
    assertNotNull("GLOBAL_THIS diagnostic type must not be null", CheckGlobalThis.GLOBAL_THIS);
    assertEquals("JSC_USED_GLOBAL_THIS", CheckGlobalThis.GLOBAL_THIS.key);
  }
}