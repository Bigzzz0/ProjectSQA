/* [Branch & Defect Analysis Matrix]
 * Target: com.google.javascript.jscomp.TypeCheck
 *
 * Defect Ground Truth:
 * - Defects4J tests: TypeCheckTest::testThisTypeOfFunction2, 3, 4
 * - Failure condition: When a function specifies an explicit '@this' type (e.g., function(this:Object)),
 *   calling it directly as a free function 'f()' without a receiver should issue the EXPECTED_THIS_TYPE
 *   warning ("\"f\" must be called with a \"this\" type"). The buggy version fails to enforce this rule
 *   during Token.CALL checking in visitCall().
 *
 * Branch Zones Targeted:
 * 1. Token.CALL & Function Types:
 *    - Functions with explicit @this type called without receiver -> targets EXPECTED_THIS_TYPE
 *    - Function calls with correct this type (e.g., o.f() or f.call(o)) -> should not warn
 *    - Constructor calls without new -> CONSTRUCTOR_NOT_CALLABLE
 *    - Non-callable expressions -> NOT_CALLABLE
 *    - Argument count validation (too few, too many, varargs) -> WRONG_ARGUMENT_COUNT
 * 2. Token.NEW & Instantiations:
 *    - Instantiating non-constructors -> NOT_A_CONSTRUCTOR
 *    - Instantiating valid constructor with correct/incorrect argument types
 * 3. Token.GETPROP & Token.GETELEM:
 *    - Accessing inexistent property on known types -> INEXISTENT_PROPERTY
 *    - Property tests in conditions (IF, WHILE, HOOK, AND) -> should suppress missing property warnings
 *    - Accessing inexistent element on EnumType -> INEXISTENT_ENUM_ELEMENT
 * 4. Token.ASSIGN & Property Overrides:
 *    - Overriding prototype with non-object -> OVERRIDING_PROTOTYPE_WITH_NON_OBJECT
 *    - Superclass property override mismatch -> HIDDEN_SUPERCLASS_PROPERTY_MISMATCH
 *    - Unknown superclass override -> UNKNOWN_OVERRIDE
 * 5. Interfaces & Classes:
 *    - Interface extending non-interface -> CONFLICTING_EXTENDED_TYPE
 *    - Interface implementing interface -> CONFLICTING_IMPLEMENTED_TYPE
 *    - Class implementing non-interface -> BAD_IMPLEMENTED_TYPE
 *    - Interface function body not empty -> INTERFACE_FUNCTION_NOT_EMPTY
 *    - Interface member invalid declaration -> INVALID_INTERFACE_MEMBER_DECLARATION
 * 6. Binary Operators & Unary Operators:
 *    - Bitwise operators with non-numeric/non-int32 context -> BIT_OPERATION
 *    - Equality and shallow equality deterministic warnings -> DETERMINISTIC_TEST, DETERMINISTIC_TEST_NO_RESULT
 *    - Bad delete operand -> BAD_DELETE
 * 7. State Transitions & Statistics:
 *    - @notypecheck annotation handling (suppression of type checks)
 *    - getTypedPercent calculation (nullCount, unknownCount, typedCount)
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Test;

public class TypeCheckGeminiTest {

  private static final String DEFAULT_EXTERNS =
      "var Object = function() {};\n"
      + "var Function = function() {};\n"
      + "var String = function() {};\n"
      + "var Number = function() {};\n"
      + "var Boolean = function() {};\n"
      + "var Array = function() {};\n"
      + "var RegExp = function() {};\n"
      + "var undefined;\n";

  /**
   * Helper to compile JS and return the compiler instance for assertions.
   */
  private Compiler compile(String js) {
    return compile(DEFAULT_EXTERNS, js);
  }

  private Compiler compile(String externs, String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    JSSourceFile[] externFiles = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", externs)
    };
    JSSourceFile[] inputFiles = new JSSourceFile[] {
        JSSourceFile.fromCode("input.js", js)
    };

    compiler.compile(externFiles, inputFiles, options);
    return compiler;
  }

  private boolean hasWarning(Compiler compiler, DiagnosticType diagnosticType) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == diagnosticType) {
        return true;
      }
    }
    return false;
  }

  private boolean hasError(Compiler compiler, DiagnosticType diagnosticType) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == diagnosticType) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Bugs)
  // Target: EXPECTED_THIS_TYPE on free calls to functions requiring a 'this'
  // =========================================================================

  @Test(timeout = 4000)
  public void testThisTypeOfFunction_defectTargetFreeCall1() {
    String js = "/** @type {function(this:Object)} */ function f() {} f();";
    Compiler compiler = compile(js);
    assertTrue("Should warn EXPECTED_THIS_TYPE when function requiring @this is called directly",
        hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testThisTypeOfFunction_defectTargetFreeCall2() {
    String js = "/** @this {Object} */ function f() {} f();";
    Compiler compiler = compile(js);
    assertTrue("Should warn EXPECTED_THIS_TYPE when @this function is invoked as a free function",
        hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testThisTypeOfFunction_defectTargetFreeCallWithArgs() {
    String js = "/** @type {function(this:Object, number): number} */ var f; f(1);";
    Compiler compiler = compile(js);
    assertTrue("Should warn EXPECTED_THIS_TYPE when invoked without receiver",
        hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testThisTypeOfFunction_validCallWithThisObject() {
    String js = "var obj = {}; /** @this {Object} */ function f() {} f.call(obj);";
    Compiler compiler = compile(DEFAULT_EXTERNS + "Function.prototype.call = function(thisArg, var_args) {};", js);
    assertFalse("Should not warn EXPECTED_THIS_TYPE when called via .call(obj)",
        hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFunctionCall_notCallable() {
    String js = "var x = 42; x();";
    Compiler compiler = compile(js);
    assertTrue("Calling a number should warn NOT_CALLABLE",
        hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testConstructorCall_withoutNew() {
    String js = "/** @constructor */ function Foo() {} Foo();";
    Compiler compiler = compile(js);
    assertTrue("Calling constructor without new should warn CONSTRUCTOR_NOT_CALLABLE",
        hasWarning(compiler, TypeCheck.CONSTRUCTOR_NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testNewOperator_notAConstructor() {
    String js = "var x = 123; new x();";
    Compiler compiler = compile(js);
    assertTrue("Instantiating non-constructor should warn NOT_A_CONSTRUCTOR",
        hasWarning(compiler, TypeCheck.NOT_A_CONSTRUCTOR));
  }

  @Test(timeout = 4000)
  public void testFunctionCall_argumentCountMismatch() {
    String js = "function add(a, b) { return a + b; } add(1);";
    Compiler compiler = compile(js);
    assertTrue("Calling function with missing args should warn WRONG_ARGUMENT_COUNT",
        hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testFunctionCall_varargsPassesCountCheck() {
    String js = "/** @param {...number} var_args */ function sum(var_args) {} sum(1, 2, 3); sum();";
    Compiler compiler = compile(js);
    assertFalse("Varargs function should allow varying argument count",
        hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testDeterministicEquality_primitiveTypes() {
    String js = "var b = (1 === 'string');";
    Compiler compiler = compile(js);
    assertTrue("Comparing number and string with === should warn DETERMINISTIC_TEST_NO_RESULT",
        hasWarning(compiler, TypeCheck.DETERMINISTIC_TEST_NO_RESULT));
  }

  @Test(timeout = 4000)
  public void testBitwiseOperation_invalidType() {
    String js = "var obj = {}; var res = ~obj;";
    Compiler compiler = compile(js);
    assertTrue("Bitwise NOT on an object should warn BIT_OPERATION",
        hasWarning(compiler, TypeCheck.BIT_OPERATION));
  }

  @Test(timeout = 4000)
  public void testBadDeleteOperand() {
    String js = "delete 123;";
    Compiler compiler = compile(js);
    assertTrue("Deleting a literal should warn BAD_DELETE",
        hasWarning(compiler, TypeCheck.BAD_DELETE));
  }

  @Test(timeout = 4000)
  public void testInterface_extendingNonInterface() {
    String js = "/** @constructor */ function Foo() {}\n"
              + "/** @interface \n * @extends {Foo} */ function MyInterface() {}";
    Compiler compiler = compile(js);
    assertTrue("Interface extending constructor should warn CONFLICTING_EXTENDED_TYPE",
        hasWarning(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  @Test(timeout = 4000)
  public void testInterface_nonEmptyBody() {
    String js = "/** @interface */ function MyInterface() { var a = 1; }";
    Compiler compiler = compile(js);
    assertTrue("Interface with non-empty body should warn INTERFACE_FUNCTION_NOT_EMPTY",
        hasWarning(compiler, TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY));
  }

  @Test(timeout = 4000)
  public void testClass_implementingNonInterface() {
    String js = "/** @constructor */ function Super() {}\n"
              + "/** @constructor \n * @implements {Super} */ function Sub() {}";
    Compiler compiler = compile(js);
    assertTrue("Class implementing a non-interface should warn BAD_IMPLEMENTED_TYPE",
        hasWarning(compiler, TypeCheck.BAD_IMPLEMENTED_TYPE));
  }

  @Test(timeout = 4000)
  public void testSuperclassOverrideMismatch() {
    String js = "/** @constructor */ function Parent() {}\n"
              + "Parent.prototype.bar = function() { return 1; };\n"
              + "/** @constructor \n * @extends {Parent} */ function Child() {}\n"
              + "/** @override \n * @return {string} */ Child.prototype.bar = function() { return 'a'; };";
    Compiler compiler = compile(js);
    assertTrue("Overriding method with incompatible return type should warn HIDDEN_SUPERCLASS_PROPERTY_MISMATCH",
        hasWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH));
  }

  @Test(timeout = 4000)
  public void testUnknownOverride() {
    String js = "/** @constructor */ function Parent() {}\n"
              + "/** @constructor \n * @extends {Parent} */ function Child() {}\n"
              + "/** @override */ Child.prototype.nonExistent = function() {};";
    Compiler compiler = compile(js);
    assertTrue("Overriding non-existent superclass method should warn UNKNOWN_OVERRIDE",
        hasWarning(compiler, TypeCheck.UNKNOWN_OVERRIDE));
  }

  @Test(timeout = 4000)
  public void testOverridingPrototypeWithNonObject() {
    String js = "/** @constructor */ function Foo() {}\n"
              + "Foo.prototype = 42;";
    Compiler compiler = compile(js);
    assertEquals("Should produce a type warning for non-object prototype assignment",
        1, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Control Flow Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropertyTestSuppression_inIfCondition() {
    String js = "var o = {}; if (o.maybeProp) { var a = 1; }";
    Compiler compiler = compile(js);
    assertFalse("Property existence check in IF condition should not warn INEXISTENT_PROPERTY",
        hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testPropertyTestSuppression_inHookCondition() {
    String js = "var o = {}; var res = o.maybeProp ? o.maybeProp : null;";
    Compiler compiler = compile(js);
    assertFalse("Property check in hook condition should not warn INEXISTENT_PROPERTY",
        hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSection() {
    String js = "/** @notypecheck */ function untyped() { var x = 123; x(); }";
    Compiler compiler = compile(js);
    assertEquals("Code under @notypecheck should suppress type errors",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testReturn_inconsistentReturnType() {
    String js = "/** @return {number} */ function f() { return 'not a number'; }";
    Compiler compiler = compile(js);
    assertEquals("Inconsistent return type should trigger type validator warning",
        1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testReturn_outsideFunctionIgnored() {
    // Return outside of function (misplaced) is ignored by TypeCheck's visitReturn
    Compiler compiler = new Compiler();
    Node returnNode = new Node(Token.RETURN);
    NodeTraversal t = new NodeTraversal(compiler, new TypeCheck(compiler, null, compiler.getTypeRegistry()));
    // Running visit on returnNode without enclosing function should safely return
    t.traverse(returnNode);
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Direct API Invariants & Exception Guards
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypedPercentInitialState() {
    Compiler compiler = new Compiler();
    TypeCheck checker = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    assertEquals("Initial typed percent should be 0.0", 0.0, checker.getTypedPercent(), 0.001);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesChaining() {
    Compiler compiler = new Compiler();
    TypeCheck checker = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    assertSame("reportMissingProperties should return same instance for chaining",
        checker, checker.reportMissingProperties(false));
  }

  @Test(timeout = 4000, expected = RuntimeException.class)
  public void testProcess_nullExternsAndJs_throwsException() {
    Compiler compiler = new Compiler();
    TypeCheck checker = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    Node jsRoot = new Node(Token.SCRIPT);
    // Should throw exception because scopeCreator and topScope are not initialized
    checker.process(null, jsRoot);
  }

  @Test(timeout = 4000, expected = RuntimeException.class)
  public void testCheck_nullNode_throwsException() {
    Compiler compiler = new Compiler();
    TypeCheck checker = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    checker.check(null, false);
  }

  @Test(timeout = 4000)
  public void testAllDiagnosticsGroupCompleteness() {
    assertNotNull("ALL_DIAGNOSTICS group must not be null", TypeCheck.ALL_DIAGNOSTICS);
    assertTrue("ALL_DIAGNOSTICS must contain EXPECTED_THIS_TYPE",
        TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.EXPECTED_THIS_TYPE));
    assertTrue("ALL_DIAGNOSTICS must contain BAD_DELETE",
        TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.BAD_DELETE));
    assertTrue("ALL_DIAGNOSTICS must contain WRONG_ARGUMENT_COUNT",
        TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.WRONG_ARGUMENT_COUNT));
    assertTrue("ALL_DIAGNOSTICS must contain NOT_CALLABLE",
        TypeCheck.ALL_DIAGNOSTICS.getTypes().contains(TypeCheck.NOT_CALLABLE));
  }
}