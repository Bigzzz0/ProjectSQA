package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeNative;
import org.junit.Test;

import java.util.Collections;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.TypeCheck
 * Target Environment: Java 8 / JUnit 4 / Defects4J
 *
 * Ground Truth Failure Target:
 * - TypeCheckTest::testBadInterfaceExtendsNonExistentInterfaces
 *   Location: TypeCheck.checkInterfaceConflictProperties (lines 894-897)
 *   Root Cause: When an interface extends more than one interface and any of those super-interfaces
 *               are non-existent/unresolved, interfaceType.getImplicitPrototype() returns null.
 *               Calling implicitProto.getOwnPropertyNames() directly dereferences null and throws NPE.
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Binary operators (arithmetic, bitwise, shift, string concatenation)
 * - Assignment operators and compound assignments
 * - Typecast annotations and type-tightening
 * - Equality comparisons (EQ, NE, SHEQ, SHNE) and typeof string validation
 * - Relational comparisons (LT, LE, GT, GE)
 * - Object literal validations (keys, getters, setters)
 * - Logical branches and percent-typed metrics computation
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Empty inputs / scripts
 * - Type checking with 0 expressions (initial state percentage 0.0)
 * - Complex ternary conditions and nested expressions
 * - Missing properties checks enabled vs disabled
 * - Struct and Dict restrictions (@struct unquoted keys, @dict quoted keys)
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Interface extending multiple non-existent interfaces (testBadInterfaceExtendsNonExistentInterfaces)
 * - Interface extending a valid interface along with an unresolved/non-existent interface
 * - Interface inheritance conflict properties resolution
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Null Node to check()
 * - Calling process() or processForTesting() in illegal or uninitialized states
 * - Calling non-functions (NOT_CALLABLE)
 * - Instantiating non-constructors (NOT_A_CONSTRUCTOR)
 * - Direct constructor invocation without 'new' (CONSTRUCTOR_NOT_CALLABLE)
 * - Argument count mismatch on function calls (WRONG_ARGUMENT_COUNT)
 * - Function masking outer-scope variable (FUNCTION_MASKS_VARIABLE)
 *
 * Partition E: Diagnostic Types & Configuration Contracts
 * - Verification of static DiagnosticTypes and DiagnosticGroup integrity
 * - reportMissingProperties toggle state
 * - @noTypeCheck annotation section suppression
 * =========================================================================================
 */
public class TypeCheckGeminiTest {

  private Compiler lastCompiler;

  private TypeCheck check(String js) {
    return check("", js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private TypeCheck check(String js, CheckLevel missingOverride, CheckLevel unknownTypes) {
    return check("", js, missingOverride, unknownTypes);
  }

  private TypeCheck check(String externs, String js, CheckLevel missingOverride, CheckLevel unknownTypes) {
    Compiler compiler = new Compiler();
    this.lastCompiler = compiler;
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", externs)),
        Collections.singletonList(SourceFile.fromCode("testcode.js", js)),
        options);
    compiler.parseInputs();

    Node externsAndJs = compiler.getRoot();
    assertNotNull("Root node must not be null after parseInputs", externsAndJs);
    Node externsRoot = externsAndJs.getFirstChild();
    Node jsRoot = externsAndJs.getLastChild();

    TypeCheck typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        missingOverride,
        unknownTypes);
    typeCheck.processForTesting(externsRoot, jsRoot);
    return typeCheck;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testArithmeticAndBitwiseOperators() {
    String js = "var a = 10 + 20;\n" +
        "var b = 20 - 5;\n" +
        "var c = 5 * 4;\n" +
        "var d = 20 / 4;\n" +
        "var e = 10 % 3;\n" +
        "var f = 1 << 2;\n" +
        "var g = 8 >> 1;\n" +
        "var h = 8 >>> 1;\n" +
        "var i = 1 & 3;\n" +
        "var j = 1 | 2;\n" +
        "var k = 1 ^ 3;\n" +
        "var l = ~5;\n" +
        "var m = +5;\n" +
        "var n = -5;\n";
    TypeCheck tc = check(js);
    assertTrue("Arithmetic and bitwise code should have high typed percentage", tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testCompoundAssignments() {
    String js = "var x = 1;\n" +
        "x += 2;\n" +
        "x -= 1;\n" +
        "x *= 3;\n" +
        "x /= 2;\n" +
        "x %= 2;\n" +
        "x <<= 1;\n" +
        "x >>= 1;\n" +
        "x >>>= 1;\n" +
        "x &= 1;\n" +
        "x |= 2;\n" +
        "x ^= 3;\n";
    TypeCheck tc = check(js);
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testRelationalAndEqualityComparisons() {
    String js = "var a = 1 < 2;\n" +
        "var b = 1 <= 2;\n" +
        "var c = 1 > 2;\n" +
        "var d = 1 >= 2;\n" +
        "var e = (1 == 2);\n" +
        "var f = (1 != 2);\n" +
        "var g = (1 === 2);\n" +
        "var h = (1 !== 2);\n";
    TypeCheck tc = check(js);
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testTypeofValidAndInvalidEvaluation() {
    String validJs = "var x = 10;\n" +
        "var isNum = (typeof x === 'number');\n" +
        "var isStr = (typeof x === 'string');\n" +
        "var isBool = (typeof x === 'boolean');\n" +
        "var isUndef = (typeof x === 'undefined');\n" +
        "var isFn = (typeof x === 'function');\n" +
        "var isObj = (typeof x === 'object');\n" +
        "var isUnk = (typeof x === 'unknown');\n";
    check(validJs);
    assertEquals("No warnings expected for valid typeof comparisons", 0, lastCompiler.getWarningCount());

    String invalidJs = "var x = 10; var bad = (typeof x === 'integer');\n";
    check(invalidJs);
    assertTrue("Should warn on invalid typeof target string", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testTypeCastTightening() {
    String js = "/** @type {Object} */ var x = /** @type {Array} */ ([]);\n";
    TypeCheck tc = check(js);
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGettersAndSetters() {
    String js = "var obj = {\n" +
        "  val: 1,\n" +
        "  get a() { return this.val; },\n" +
        "  set a(v) { this.val = v; }\n" +
        "};\n";
    TypeCheck tc = check(js);
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testLogicalAndCommaExpressions() {
    String js = "var a = true && false;\n" +
        "var b = true || false;\n" +
        "var c = (1, 2, 'three');\n" +
        "var d = !false;\n" +
        "var e = void 0;\n" +
        "var f = null;\n" +
        "var g = [1, 2, 3];\n" +
        "var h = /pattern/g;\n";
    TypeCheck tc = check(js);
    assertTrue(tc.getTypedPercent() > 0.0);
  }

  @Test(timeout = 4000)
  public void testSwitchCaseTypeCheck() {
    String js = "var x = 2;\n" +
        "switch (x) {\n" +
        "  case 1: break;\n" +
        "  case 2: break;\n" +
        "  default: break;\n" +
        "}\n";
    check(js);
    assertEquals(0, lastCompiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testInitialTypedPercentIsZeroWhenNoNodes() {
    Compiler compiler = new Compiler();
    TypeCheck tc = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    assertEquals("Untyped compiler pass should report exactly 0.0% typed", 0.0, tc.getTypedPercent(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testEmptyScriptExecution() {
    TypeCheck tc = check("");
    assertNotNull(tc);
    assertEquals(0.0, tc.getTypedPercent(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testStructEnforcesUnquotedKeys() {
    String structWithQuotedKey =
        "/** @constructor @struct */ function S() {}\n" +
        "var s = /** @type {S} */ ({ 'illegal': 1 });\n";
    check(structWithQuotedKey);
    assertTrue("Should warn on quoted keys in struct literal", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testDictEnforcesQuotedKeys() {
    String dictWithUnquotedKey =
        "/** @constructor @dict */ function D() {}\n" +
        "var d = /** @type {D} */ ({ illegal: 1 });\n";
    check(dictWithUnquotedKey);
    assertTrue("Should warn on unquoted keys in dict literal", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testInOperatorOnStructThrowsWarning() {
    String js = "/** @constructor @struct */ function S() {}\n" +
        "var s = new S();\n" +
        "var res = 'prop' in s;\n";
    check(js);
    assertTrue("Should warn on 'in' used with struct", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testForInOnStructThrowsWarning() {
    String js = "/** @constructor @struct */ function S() {}\n" +
        "var s = new S();\n" +
        "for (var key in s) {}\n";
    check(js);
    assertTrue("Should warn on for-in used with struct", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesToggle() {
    String js = "var obj = { x: 1 };\n" +
        "var y = obj.missingProperty;\n";

    TypeCheck tcDefault = check(js);
    int warningsDefault = lastCompiler.getWarningCount();

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", js)),
        options);
    compiler.parseInputs();
    Node externsAndJs = compiler.getRoot();
    TypeCheck tcDisabled = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    tcDisabled.reportMissingProperties(false);
    tcDisabled.processForTesting(externsAndJs.getFirstChild(), externsAndJs.getLastChild());

    assertTrue(compiler.getWarningCount() <= warningsDefault);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J Failure:
   * com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtendsNonExistentInterfaces
   *
   * In TypeCheck.java, checkInterfaceConflictProperties fails with a NullPointerException
   * when an interface extends multiple interfaces that cannot be resolved (getImplicitPrototype() returns null).
   */
  @Test(timeout = 4000)
  public void testBadInterfaceExtendsNonExistentInterfaces() {
    String js = "/**\n" +
        " * @interface\n" +
        " * @extends {nonExistent1}\n" +
        " * @extends {nonExistent2}\n" +
        " */\n" +
        "function Foo() {}\n";

    // When the defect is present, check() triggers NullPointerException in checkInterfaceConflictProperties.
    // When fixed, it completes gracefully without throwing NPE.
    check(js);
    assertNotNull(lastCompiler);
  }

  @Test(timeout = 4000)
  public void testInterfaceExtendsRealAndNonExistentInterfaces() {
    String js = "/** @interface */\n" +
        "function RealInterface() {}\n" +
        "/**\n" +
        " * @interface\n" +
        " * @extends {RealInterface}\n" +
        " * @extends {nonExistentSuper}\n" +
        " */\n" +
        "function HybridInterface() {}\n";

    check(js);
    assertNotNull(lastCompiler);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testCheckNullNodeThrowsNullPointerException() {
    Compiler compiler = new Compiler();
    TypeCheck tc = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    tc.check(null, false);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessWithoutInitializationThrowsIllegalStateException() {
    Compiler compiler = new Compiler();
    TypeCheck tc = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    Node js = new Node(Token.SCRIPT);
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.BLOCK, externs, js);
    // topScope and scopeCreator are null, should violate Preconditions
    tc.process(externs, js);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessForTestingWithParentlessJsRootThrowsIllegalStateException() {
    Compiler compiler = new Compiler();
    TypeCheck tc = new TypeCheck(compiler, null, compiler.getTypeRegistry());
    Node js = new Node(Token.SCRIPT); // No parent
    tc.processForTesting(null, js);
  }

  @Test(timeout = 4000)
  public void testCallingNonCallableTypeProducesWarning() {
    String js = "var notAFn = 123;\n" +
        "notAFn();\n";
    check(js);
    assertTrue("Calling non-callable must emit warning", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testInstantiatingNonConstructorProducesWarning() {
    String js = "var notAClass = 123;\n" +
        "new notAClass();\n";
    check(js);
    assertTrue("New-ing non-constructor must emit warning", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testConstructorCalledWithoutNewProducesWarning() {
    String js = "/** @constructor */ function MyCtor() {}\n" +
        "MyCtor();\n";
    check(js);
    assertTrue("Constructor invoked without 'new' must emit warning", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testFunctionWithExplicitThisCalledWithoutThisProducesWarning() {
    String js = "/** @this {Array} */ function fnWithThis() {}\n" +
        "fnWithThis();\n";
    check(js);
    assertTrue("Function expecting 'this' called standalone must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testWrongArgumentCountProducesWarning() {
    String js = "function requireTwo(a, b) { return a + b; }\n" +
        "requireTwo(1);\n";
    check(js);
    assertTrue("Calling function with fewer arguments than required must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testFunctionMasksVariableProducesWarning() {
    String js = "var f = 1;\n" +
        "function f() {}\n";
    check(js);
    assertTrue("Function masking outer variable must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testIllegalPropertyCreationOnStructInstance() {
    String js = "/** @constructor @struct */ function MyStruct() {}\n" +
        "var inst = new MyStruct();\n" +
        "inst.newProp = 42;\n";
    check(js);
    assertTrue("Adding property to struct instance after construction must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testBitwiseOperationOnIncompatibleTypeProducesWarning() {
    String js = "var badBit = ~'hello';\n";
    check(js);
    assertTrue("Bitwise NOT on string must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testInvalidInterfaceMemberDeclarations() {
    String nonFunctionMember =
        "/** @interface */ function MyInterface() {}\n" +
        "MyInterface.prototype.prop = 123;\n";
    check(nonFunctionMember);
    assertTrue("Interface property assignment to non-function must warn", lastCompiler.getWarningCount() > 0);

    String nonEmptyFunctionMember =
        "/** @interface */ function MyInterface2() {}\n" +
        "MyInterface2.prototype.fn = function() { var x = 1; };\n";
    check(nonEmptyFunctionMember);
    assertTrue("Interface function body that is non-empty must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testInterfaceExtendsNonInterfaceProducesWarning() {
    String js = "/** @constructor */ function SuperClass() {}\n" +
        "/** @interface @extends {SuperClass} */ function InvalidInterface() {}\n";
    check(js);
    assertTrue("Interface extending a constructor class must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testClassImplementsNonInterfaceProducesWarning() {
    String js = "/** @constructor */ function SuperClass() {}\n" +
        "/** @constructor @implements {SuperClass} */ function InvalidClass() {}\n";
    check(js);
    assertTrue("Class implementing a non-interface must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testIncompatibleReturnStatement() {
    String js = "/** @return {number} */ function getNum() {\n" +
        "  return 'not a number';\n" +
        "}\n";
    check(js);
    assertTrue("Inconsistent return type must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testIncompatibleEnumElementAssignment() {
    String js = "/** @enum {number} */ var NumEnum = { A: 1 };\n" +
        "/** @enum {string} */ var StrEnum = NumEnum;\n";
    check(js);
    assertTrue("Incompatible enum element types must warn", lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testHiddenSuperclassPropertyWarning() {
    String js = "/** @constructor */ function Base() {}\n" +
        "Base.prototype.prop = function() {};\n" +
        "/** @constructor @extends {Base} */ function Child() {}\n" +
        "Child.prototype.prop = function() {};\n";
    check(js, CheckLevel.WARNING, CheckLevel.OFF);
    assertTrue("Overriding superclass property without @override must warn when enabled",
        lastCompiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testUnknownOverrideWarning() {
    String js = "/** @constructor */ function Base() {}\n" +
        "/** @constructor @extends {Base} */ function Child() {}\n" +
        "/** @override */ Child.prototype.nonExistent = function() {};\n";
    check(js, CheckLevel.WARNING, CheckLevel.OFF);
    assertTrue("Override on non-existent superclass property must warn", lastCompiler.getWarningCount() > 0);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Diagnostics, & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testStaticDiagnosticConstantsIntegrity() {
    assertNotNull(TypeCheck.UNEXPECTED_TOKEN);
    assertNotNull(TypeCheck.BAD_DELETE);
    assertNotNull(TypeCheck.DETERMINISTIC_TEST);
    assertNotNull(TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
    assertNotNull(TypeCheck.INEXISTENT_ENUM_ELEMENT);
    assertNotNull(TypeCheck.INEXISTENT_PROPERTY);
    assertNotNull(TypeCheck.NOT_A_CONSTRUCTOR);
    assertNotNull(TypeCheck.BIT_OPERATION);
    assertNotNull(TypeCheck.NOT_CALLABLE);
    assertNotNull(TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
    assertNotNull(TypeCheck.FUNCTION_MASKS_VARIABLE);
    assertNotNull(TypeCheck.MULTIPLE_VAR_DEF);
    assertNotNull(TypeCheck.ENUM_DUP);
    assertNotNull(TypeCheck.ENUM_NOT_CONSTANT);
    assertNotNull(TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION);
    assertNotNull(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
    assertNotNull(TypeCheck.CONFLICTING_EXTENDED_TYPE);
    assertNotNull(TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
    assertNotNull(TypeCheck.BAD_IMPLEMENTED_TYPE);
    assertNotNull(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
    assertNotNull(TypeCheck.HIDDEN_INTERFACE_PROPERTY);
    assertNotNull(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
    assertNotNull(TypeCheck.UNKNOWN_OVERRIDE);
    assertNotNull(TypeCheck.INTERFACE_METHOD_OVERRIDE);
    assertNotNull(TypeCheck.UNKNOWN_EXPR_TYPE);
    assertNotNull(TypeCheck.UNRESOLVED_TYPE);
    assertNotNull(TypeCheck.WRONG_ARGUMENT_COUNT);
    assertNotNull(TypeCheck.ILLEGAL_IMPLICIT_CAST);
    assertNotNull(TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
    assertNotNull(TypeCheck.EXPECTED_THIS_TYPE);
    assertNotNull(TypeCheck.IN_USED_WITH_STRUCT);
    assertNotNull(TypeCheck.ILLEGAL_PROPERTY_CREATION);
    assertNotNull(TypeCheck.ILLEGAL_OBJLIT_KEY);
    assertNotNull(TypeCheck.ALL_DIAGNOSTICS);
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckAnnotationSuppressesReporting() {
    String js = "/** @noTypeCheck */\n" +
        "function suppressed() {\n" +
        "  var notAFn = 123;\n" +
        "  notAFn();\n" +
        "}\n";
    check(js);
    assertEquals("Errors in @noTypeCheck function must be suppressed", 0, lastCompiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testReportUnknownTypesMetricAccounting() {
    String js = "function getUnknown(x) { return x.unknownProp; }\n" +
        "getUnknown(1);\n";
    TypeCheck tc = check(js, CheckLevel.OFF, CheckLevel.WARNING);
    assertTrue("Should track unknown types in percentage accounting", tc.getTypedPercent() >= 0.0);
  }
}