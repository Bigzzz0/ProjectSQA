package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;

/*
 * [Branch & Defect Analysis Matrix]
 * ==================================================================================================
 * Target Class: com.google.javascript.jscomp.CheckAccessControls
 *
 * 1. Defects4J Known Failure Vectors Targeted:
 *    - testNoPrivateAccessForProperties6: Verifies property access/assignment on a subclass when
 *      overriding a private property declared on a prototype in a different input file. Triggers
 *      BAD_PRIVATE_PROPERTY_ACCESS rather than an illegal PRIVATE_OVERRIDE diagnostic.
 *    - testNoPrivateAccessForProperties8: Verifies private property initialization in a subclass
 *      constructor when the superclass declares the private property in its constructor across files.
 *      Exposes the defect where `isOverride` relies strictly on `inGlobalScope()`, leading to
 *      missing BAD_PRIVATE_PROPERTY_ACCESS diagnostics (expected: 1, was: 0).
 *
 * 2. Decision Branches Covered:
 *    - ScopedCallback scope transitions: enterScope / exitScope with deprecatedDepth, methodDepth,
 *      and currentClass resolution (Token.ASSIGN, Token.GETPROP, function declarations).
 *    - Name deprecation (checkNameDeprecation): with reason, without reason, skipped in VAR/FUNCTION/NEW,
 *      deprecations allowed inside deprecated functions / classes / static methods.
 *    - Constructor deprecation (checkConstructorDeprecation): NEW node with reason and without reason.
 *    - Property deprecation (checkPropertyDeprecation): GETPROP node with reason, without reason,
 *      ignored on assignments or NEW expressions.
 *    - Visibility checks for global names (checkNameVisibility): private global access across files,
 *      valid private constructor access (e.g. instanceof, factory calls) vs invalid new expressions.
 *    - Constant property checks (checkConstantProperty): @const reassignments via ASSIGN, INC (++),
 *      DEC (--), prototype constant property tracking, prototype inheritance traversal.
 *    - Property visibility checks (checkPropertyVisibility): private/protected access in same file vs
 *      foreign files, subclass protected access, visibility mismatches during override.
 *    - Hot-swap and lifecycle: hotSwapScript traversal and shouldTraverse predicate sanity.
 * ==================================================================================================
 */
public class CheckAccessControlsGeminiTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckAccessControls(compiler);
  }

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = super.getOptions();
    options.checkGlobalThisLevel = CheckLevel.OFF;
    return options;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  // ================================================================================================
  // Partition A: Core Functional Logic & State Transitions (Deprecation Checks)
  // ================================================================================================

  @Test(timeout = 4000)
  public void testDeprecatedNameWithoutReason() {
    test("/** @deprecated */ var x = 1; function f() { return x; }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_NAME);
  }

  @Test(timeout = 4000)
  public void testDeprecatedNameWithReason() {
    test("/** @deprecated Use y instead */ var x = 1; function f() { return x; }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_NAME_REASON);
  }

  @Test(timeout = 4000)
  public void testDeprecatedPropertyWithoutReason() {
    test("/** @constructor */ function Foo() {} " +
         "/** @deprecated */ Foo.prototype.prop = 3; " +
         "function f() { return (new Foo()).prop; }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_PROP);
  }

  @Test(timeout = 4000)
  public void testDeprecatedPropertyWithReason() {
    test("/** @constructor */ function Foo() {} " +
         "/** @deprecated Obsolete property */ Foo.prototype.prop = 3; " +
         "function f() { return (new Foo()).prop; }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_PROP_REASON);
  }

  @Test(timeout = 4000)
  public void testDeprecatedClassConstructorWithoutReason() {
    test("/** @constructor\n * @deprecated */ function Foo() {} " +
         "function f() { return new Foo(); }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_CLASS);
  }

  @Test(timeout = 4000)
  public void testDeprecatedClassConstructorWithReason() {
    test("/** @constructor\n * @deprecated Use Bar instead */ function Foo() {} " +
         "function f() { return new Foo(); }",
        (DiagnosticType) null, CheckAccessControls.DEPRECATED_CLASS_REASON);
  }

  @Test(timeout = 4000)
  public void testAccessDeprecatedInsideDeprecatedFunctionSuppressed() {
    testSame("/** @deprecated */ var oldVar = 1; " +
             "/** @deprecated */ function oldFunc() { return oldVar; }");
  }

  @Test(timeout = 4000)
  public void testAccessDeprecatedInsideDeprecatedClassMethodSuppressed() {
    testSame("/** @deprecated */ var oldVar = 1; " +
             "/** @constructor\n * @deprecated */ function DepClass() {} " +
             "DepClass.prototype.method = function() { return oldVar; };");
  }

  @Test(timeout = 4000)
  public void testAssignmentToDeprecatedPropertyAllowed() {
    testSame("/** @constructor */ function Foo() {} " +
             "/** @deprecated */ Foo.prototype.bar = 1; " +
             "function f() { var obj = new Foo(); obj.bar = 2; }");
  }

  // ================================================================================================
  // Partition B: Boundary Value Analysis & Constant Mutation Verification
  // ================================================================================================

  @Test(timeout = 4000)
  public void testConstantPropertyReassignmentDirectAssignment() {
    test("/** @constructor */ function Foo() {} " +
         "/** @const */ Foo.prototype.BAR = 1; " +
         "var f = new Foo(); f.BAR = 2;",
        (DiagnosticType) null, CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  @Test(timeout = 4000)
  public void testConstantPropertyReassignmentIncrement() {
    test("/** @constructor */ function Foo() {} " +
         "/** @const */ Foo.prototype.COUNT = 1; " +
         "var f = new Foo(); f.COUNT++;",
        (DiagnosticType) null, CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  @Test(timeout = 4000)
  public void testConstantPropertyReassignmentDecrement() {
    test("/** @constructor */ function Foo() {} " +
         "/** @const */ Foo.prototype.COUNT = 1; " +
         "var f = new Foo(); f.COUNT--;",
        (DiagnosticType) null, CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  @Test(timeout = 4000)
  public void testConstantPropertyAllowedFirstAssignment() {
    testSame("/** @constructor */ function Foo() { " +
             "  /** @const */ this.INITIALIZED_ONCE = 42; " +
             "}");
  }

  @Test(timeout = 4000)
  public void testGlobalDeprecatedReadWithoutCallIgnored() {
    testSame("/** @deprecated */ var x = 1; var y = x;");
  }

  // ================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Verification)
  // ================================================================================================

  /**
   * Targets defect corresponding to testNoPrivateAccessForProperties6.
   * Overriding a private property on a prototype from a foreign file must trigger
   * BAD_PRIVATE_PROPERTY_ACCESS.
   */
  @Test(timeout = 4000)
  public void testNoPrivateAccessForProperties6() {
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @private */ Foo.prototype.x_ = 3;",
      "/** @constructor */ function SubFoo() {} " +
      "SubFoo.prototype = new Foo(); " +
      "SubFoo.prototype.x_ = 5;"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PRIVATE_PROPERTY_ACCESS);
  }

  /**
   * Targets defect corresponding to testNoPrivateAccessForProperties8.
   * Accessing a private property inside a subclass constructor across files must trigger
   * BAD_PRIVATE_PROPERTY_ACCESS even though methodDepth > 0 (not inGlobalScope).
   */
  @Test(timeout = 4000)
  public void testNoPrivateAccessForProperties8() {
    test(new String[] {
      "/** @constructor */ function Foo() { /** @private */ this.x_ = 3; }",
      "/** @constructor */ function SubFoo() { /** @private */ this.x_ = 5; } " +
      "SubFoo.prototype = new Foo();"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PRIVATE_PROPERTY_ACCESS);
  }

  // ================================================================================================
  // Partition D: Visibility & Access Control Guard Paths
  // ================================================================================================

  @Test(timeout = 4000)
  public void testPrivateGlobalVariableCrossFileViolation() {
    test(new String[] {
      "/** @private */ var secret = 10;",
      "function leak() { return secret; }"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

  @Test(timeout = 4000)
  public void testPrivateGlobalVariableSameFileAllowed() {
    testSame("/** @private */ var secret = 10; function read() { return secret; }");
  }

  @Test(timeout = 4000)
  public void testPrivatePropertyCrossFileViolation() {
    test(new String[] {
      "/** @constructor */ function Foo() { /** @private */ this.secret_ = 10; }",
      "function leak() { return (new Foo()).secret_; }"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PRIVATE_PROPERTY_ACCESS);
  }

  @Test(timeout = 4000)
  public void testPrivatePropertySameFileAllowed() {
    testSame("/** @constructor */ function Foo() { /** @private */ this.secret_ = 10; } " +
             "function read() { return (new Foo()).secret_; }");
  }

  @Test(timeout = 4000)
  public void testProtectedPropertyCrossFileViolation() {
    test(new String[] {
      "/** @constructor */ function Foo() { /** @protected */ this.prot_ = 20; }",
      "function leak() { return (new Foo()).prot_; }"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PROTECTED_PROPERTY_ACCESS);
  }

  @Test(timeout = 4000)
  public void testProtectedPropertySubclassAccessAllowed() {
    testSame(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @protected */ Foo.prototype.prot_ = 20;",
      "/** @constructor\n * @extends {Foo} */ function SubFoo() {} " +
      "SubFoo.prototype = new Foo(); " +
      "SubFoo.prototype.method = function() { return this.prot_; };"
    });
  }

  @Test(timeout = 4000)
  public void testVisibilityMismatchOnOverride() {
    test(new String[] {
      "/** @constructor */ function Foo() {} " +
      "/** @protected */ Foo.prototype.item = 1;",
      "/** @constructor\n * @extends {Foo} */ function SubFoo() {} " +
      "SubFoo.prototype = new Foo(); " +
      "/** @public */ SubFoo.prototype.item = 2;"
    }, (DiagnosticType) null, CheckAccessControls.VISIBILITY_MISMATCH);
  }

  @Test(timeout = 4000)
  public void testPrivateConstructorValidAccess() {
    testSame(new String[] {
      "/** @constructor\n * @private */ function Singleton() {} " +
      "Singleton.instance = new Singleton(); " +
      "Singleton.getInstance = function() { return Singleton.instance; };",
      "var inst = Singleton.getInstance(); var ok = inst instanceof Singleton;"
    });
  }

  @Test(timeout = 4000)
  public void testPrivateConstructorInvalidDirectInstantiation() {
    test(new String[] {
      "/** @constructor\n * @private */ function Singleton() {}",
      "var forbidden = new Singleton();"
    }, (DiagnosticType) null, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

  // ================================================================================================
  // Partition E: Pass Lifecycle & Direct State Invariants
  // ================================================================================================

  @Test(timeout = 4000)
  public void testHotSwapScriptExecution() {
    Compiler compiler = new Compiler();
    CheckAccessControls pass = new CheckAccessControls(compiler);
    Node scriptRoot = compiler.parseTestCode("var a = 1;");
    pass.hotSwapScript(scriptRoot);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testShouldTraverseReturnsTrue() {
    Compiler compiler = new Compiler();
    CheckAccessControls pass = new CheckAccessControls(compiler);
    Node node = new Node(0);
    assertTrue(pass.shouldTraverse(null, node, null));
  }
}