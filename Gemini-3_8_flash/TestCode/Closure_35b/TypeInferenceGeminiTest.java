/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.TypeInference
 *
 * Targeted Defects & Branches:
 * 1. DEFECT ISSUE 669 (Defects4J Closure-35):
 *    - Method: inferPropertyTypesToMatchConstraint(JSType, JSType)
 *    - Flaw: When returning an object literal whose inferred property matches an object record constraint
 *      with an optional/undefined property type (e.g., {prop1: (Object|undefined)}), the inferrer did not
 *      properly widen/preserve the property type, leading to false-positive JSC_TYPE_MISMATCH warnings.
 *    - Test: testIssue669() asserts clean type checking without warnings on the ground-truth code snippet.
 *
 * 2. Short-Circuiting Binary Ops & Boolean Logic:
 *    - Method: traverseShortCircuitingBinOp, traverseAnd, traverseOr, getBooleanOutcomes
 *    - Decisions: condition == true (AND) vs false (OR); outcome scopes for left/right branches.
 *    - Exhaustive BooleanLiteralSet permutations (TRUE, FALSE, BOTH, EMPTY).
 *
 * 3. Backwards Inference from Call Sites:
 *    - Method: backwardsInferenceFromCallSite, updateTypeOfThisOnClosure, updateBind
 *    - Diagnostics:
 *      * JSC_TEMPLATE_TYPE_NOT_OBJECT_TYPE: Template type instantiated with non-object.
 *      * JSC_TEMPLATE_TYPE_OF_THIS_EXPECTED: Template declared without matching this: T param.
 *      * JSC_FUNCTION_LITERAL_UNDEFINED_THIS: Anonymous function literal referencing undefined this.
 *
 * 4. AST Traversal Coverage:
 *    - Tokens: ASSIGN, ASSIGN_ADD, ADD, POS, NEG, ARRAYLIT, OBJECTLIT, CALL, NEW, HOOK, THIS,
 *              BITWISE/ARITHMETIC ops, PARAM_LIST, COMMA, TYPEOF, RELATIONAL/EQUALITY ops,
 *              GETPROP, GETELEM, EXPR_RESULT, SWITCH, RETURN, VAR, THROW, CATCH.
 *    - Qualified name stubbed casts via JSDoc in EXPR_RESULT.
 *    - Property definition heuristics (constructors, prototypes, direct assignments).
 * -------------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import java.util.Arrays;

public class TypeInferenceGeminiTest {

  private Compiler compile(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    String externsJs =
        "var undefined;\n" +
        "/** @constructor\n * @param {*=} opt_value\n * @return {!Object} */\n" +
        "function Object(opt_value) {}\n" +
        "/** @constructor\n * @param {...*} var_args\n * @return {!Function} */\n" +
        "function Function(var_args) {}\n" +
        "/** @constructor\n * @param {...*} var_args\n * @return {!Array} */\n" +
        "function Array(var_args) {}\n" +
        "/** @constructor\n * @param {*=} opt_value\n * @return {string} */\n" +
        "function String(opt_value) {}\n" +
        "/** @constructor\n * @param {*=} opt_value\n * @return {boolean} */\n" +
        "function Boolean(opt_value) {}\n" +
        "/** @constructor\n * @param {*=} opt_value\n * @return {number} */\n" +
        "function Number(opt_value) {}\n" +
        "/** @constructor\n * @param {*=} opt_pattern\n * @param {*=} opt_flags\n * @return {!RegExp} */\n" +
        "function RegExp(opt_pattern, opt_flags) {}\n" +
        "/** @constructor\n * @param {*=} opt_message\n * @return {!Error} */\n" +
        "function Error(opt_message) {}\n";

    SourceFile externSource = SourceFile.fromCode("externs.js", externsJs);
    SourceFile inputSource = SourceFile.fromCode("input.js", js);

    compiler.compile(externSource, inputSource, options);
    return compiler;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 669 / Defects4J Closure-35)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue669() {
    String js =
        "/** @return {{prop1: (Object|undefined)}} */\n" +
        "function f() {\n" +
        "  var obj = {prop1: {}};\n" +
        "  return obj;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals("Unexpected warning(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
    assertEquals("Unexpected error(s): " + Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue669DirectReturnObjectLiteral() {
    String js =
        "/** @return {{prop1: (Object|undefined)}} */\n" +
        "function f() {\n" +
        "  return {prop1: {}};\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals("Unexpected warning(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
    assertEquals("Unexpected error(s): " + Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testInferPropertyTypesRecordTypeMissingProp() {
    String js =
        "/** @return {{prop1: (Object|undefined), prop2: (string|undefined)}} */\n" +
        "function f() {\n" +
        "  return {prop1: {}};\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals("Unexpected warning(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testInferPropertyTypesConstraintInFunctionCall() {
    String js =
        "/** @param {{prop: (number|undefined)}} opt */\n" +
        "function target(opt) {}\n" +
        "target({});\n";
    Compiler compiler = compile(js);
    assertEquals("Unexpected warning(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testVariableAssignmentAndFlow() {
    String js =
        "function f() {\n" +
        "  var x = 1;\n" +
        "  x = 2;\n" +
        "  return x;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testHookTernaryOperator() {
    String js =
        "function f(cond) {\n" +
        "  var x = cond ? 1 : 'str';\n" +
        "  return x;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testLogicalShortCircuitAndOr() {
    String js =
        "function f(a, b) {\n" +
        "  var x = a && b;\n" +
        "  var y = a || b;\n" +
        "  if (x && y) {\n" +
        "    return 1;\n" +
        "  }\n" +
        "  return 0;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForInLoopRedeclaration() {
    String js =
        "function f(obj) {\n" +
        "  for (var k in obj) {\n" +
        "    var key = k;\n" +
        "  }\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testSwitchAndCaseStatement() {
    String js =
        "function f(x) {\n" +
        "  switch (x) {\n" +
        "    case 1:\n" +
        "      return 'one';\n" +
        "    case 2:\n" +
        "      return 'two';\n" +
        "    default:\n" +
        "      return 'other';\n" +
        "  }\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testArithmeticAndBitwiseOperations() {
    String js =
        "function f(a, b) {\n" +
        "  var pos = +a;\n" +
        "  var neg = -b;\n" +
        "  var sub = a - b;\n" +
        "  var mul = a * b;\n" +
        "  var div = a / b;\n" +
        "  var mod = a % b;\n" +
        "  var inc = ++a;\n" +
        "  var dec = --b;\n" +
        "  var not = ~a;\n" +
        "  var band = a & b;\n" +
        "  var bor = a | b;\n" +
        "  var bxor = a ^ b;\n" +
        "  var lsh = a << b;\n" +
        "  var rsh = a >> b;\n" +
        "  var ursh = a >>> b;\n" +
        "  return sub + mul;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testRelationalAndEqualityOperators() {
    String js =
        "function f(a, b) {\n" +
        "  var r1 = a < b;\n" +
        "  var r2 = a <= b;\n" +
        "  var r3 = a > b;\n" +
        "  var r4 = a >= b;\n" +
        "  var r5 = a == b;\n" +
        "  var r6 = a != b;\n" +
        "  var r7 = a === b;\n" +
        "  var r8 = a !== b;\n" +
        "  var r9 = a instanceof Object;\n" +
        "  var r10 = 'key' in a;\n" +
        "  var r11 = !a;\n" +
        "  var r12 = delete a.prop;\n" +
        "  return r1 && r5;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaAndTypeofOperators() {
    String js =
        "function f(a, b) {\n" +
        "  var t = typeof a;\n" +
        "  var c = (a, b);\n" +
        "  return t;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testArrayLiteralAndGetElem() {
    String js =
        "function f() {\n" +
        "  var arr = [10, 20, 30];\n" +
        "  var first = arr[0];\n" +
        "  return first;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralAndGetProp() {
    String js =
        "function f() {\n" +
        "  var obj = {a: 1, b: 'str'};\n" +
        "  return obj.a;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testTryCatchBlock() {
    String js =
        "function f() {\n" +
        "  try {\n" +
        "    throw new Error('fail');\n" +
        "  } catch (e) {\n" +
        "    return e;\n" +
        "  }\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstructorAndNewOperator() {
    String js =
        "/** @constructor */\n" +
        "function Person(name) {\n" +
        "  this.name = name;\n" +
        "}\n" +
        "Person.prototype.getName = function() { return this.name; };\n" +
        "function test() {\n" +
        "  var p = new Person('Alice');\n" +
        "  return p.getName();\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testQualifiedNameCastInExprResult() {
    String js =
        "var ns = {};\n" +
        "/** @type {number} */ ns.prop;\n" +
        "ns.prop = 42;\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testBackwardsInferenceOnCallback() {
    String js =
        "/** @param {function(number): string} cb */\n" +
        "function execute(cb) {}\n" +
        "execute(function(x) { return '' + x; });\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionBindInference() {
    String js =
        "/** @param {number} a\n * @param {string} b\n * @return {boolean} */\n" +
        "function target(a, b) { return true; }\n" +
        "var bound = target.bind(null, 1);\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testAdditionTypeCombinations() {
    String js =
        "function f(num, str) {\n" +
        "  var n = 1 + 2;\n" +
        "  var s = 'a' + 'b';\n" +
        "  var ns = 1 + 'b';\n" +
        "  var sn = 'a' + 2;\n" +
        "  num += 5;\n" +
        "  return s;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & getBooleanOutcomes Logic
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanOutcomesConditionTrue() {
    // When condition == true: right.union(left.intersection(BooleanLiteralSet.FALSE))
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, true));
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.TRUE, true));
  }

  @Test(timeout = 4000)
  public void testGetBooleanOutcomesConditionFalse() {
    // When condition == false: right.union(left.intersection(BooleanLiteralSet.TRUE))
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, false));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, false));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, false));
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, false));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.FALSE, false));
  }

  // =========================================================================
  // Partition D: Diagnostic & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticConstantsIntegrity() {
    assertNotNull(TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE);
    assertEquals("JSC_TEMPLATE_TYPE_NOT_OBJECT_TYPE",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.key);

    assertNotNull(TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED);
    assertEquals("JSC_TEMPLATE_TYPE_OF_THIS_EXPECTED",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.key);

    assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test(timeout = 4000)
  public void testTemplateTypeNotObjectTypeWarning() {
    String js =
        "/**\n" +
        " * @param {function(this: T, ...)} fn\n" +
        " * @param {T} obj\n" +
        " * @template T\n" +
        " */\n" +
        "function withThis(fn, obj) {}\n" +
        "withThis(function() {}, 123);\n";
    Compiler compiler = compile(js);
    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Expected TEMPLATE_TYPE_NOT_OBJECT_TYPE warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testTemplateTypeOfThisExpectedWarning() {
    String js =
        "/**\n" +
        " * @param {T} obj\n" +
        " * @param {number} n\n" +
        " * @template T\n" +
        " */\n" +
        "function badTemplate(obj, n) {}\n" +
        "badTemplate({}, 1);\n";
    Compiler compiler = compile(js);
    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Expected TEMPLATE_TYPE_OF_THIS_EXPECTED warning", foundWarning);
  }

  @Test(timeout = 4000)
  public void testFunctionLiteralUndefinedThisWarning() {
    String js =
        "/**\n" +
        " * @param {function(this: T, ...)} fn\n" +
        " * @param {T} obj\n" +
        " * @template T\n" +
        " */\n" +
        "function withThis(fn, obj) {}\n" +
        "withThis(function() { return this.foo; }, null);\n";
    Compiler compiler = compile(js);
    boolean foundWarning = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS) {
        foundWarning = true;
        break;
      }
    }
    assertTrue("Expected FUNCTION_LITERAL_UNDEFINED_THIS warning", foundWarning);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testValidTemplateTypeOfThisInference() {
    String js =
        "/**\n" +
        " * @param {function(this: T, ...)} fn\n" +
        " * @param {T} obj\n" +
        " * @template T\n" +
        " */\n" +
        "function withThis(fn, obj) {}\n" +
        "withThis(function() { return this; }, {name: 'Alice'});\n";
    Compiler compiler = compile(js);
    assertEquals("Unexpected warning(s): " + Arrays.toString(compiler.getWarnings()),
        0, compiler.getWarningCount());
    assertEquals("Unexpected error(s): " + Arrays.toString(compiler.getErrors()),
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDereferencePointerNarrowsType() {
    String js =
        "function f(x) {\n" +
        "  if (x != null) {\n" +
        "    var p = x.prop;\n" +
        "    return p;\n" +
        "  }\n" +
        "  return null;\n" +
        "}\n";
    Compiler compiler = compile(js);
    assertEquals(0, compiler.getWarningCount());
  }
}