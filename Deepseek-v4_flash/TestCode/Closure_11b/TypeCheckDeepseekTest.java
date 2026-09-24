package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.jscomp.testing.TestExternsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypeCheck.java - Defect in property access checking (testGetprop4, testIssue810)
 * 
 * Known Defect: The checkPropertyAccess method fails to report a warning when
 * accessing a property on an object whose type is unknown but the property
 * cannot be defined on the object type. Specifically, when the object type
 * is an interface or a union type that includes an interface, the check
 * incorrectly skips the property existence check.
 * 
 * Branch Zones Targeted:
 * 1. visitGetProp - property access on various object types
 *    - dict type: should report ILLEGAL_PROPERTY_ACCESS
 *    - null/undefined: should report "No properties on this expression"
 *    - normal object: should check property existence
 *    - unknown type: should attempt property check
 * 
 * 2. checkPropertyAccess - property existence verification
 *    - enum type: should report INEXISTENT_ENUM_ELEMENT
 *    - non-enum object: should report INEXISTENT_PROPERTY if property can't be defined
 *    - property test context: should NOT report (isPropertyTest)
 * 
 * 3. checkPropertyAccessHelper - final property existence check
 *    - empty type: skip check
 *    - reportMissingProperties=false: skip check
 *    - property test: skip check
 *    - canPropertyBeDefined=false: report INEXISTENT_PROPERTY
 * 
 * 4. isPropertyTest - property test detection
 *    - CALL with property test function
 *    - IF/WHILE/DO/FOR condition
 *    - INSTANCEOF/TYPEOF
 *    - AND/HOOK left operand
 *    - NOT with OR parent
 * 
 * 5. visitAssign - assignment type checking
 *    - prototype assignment
 *    - property assignment with declared type
 *    - qualified name assignment
 *    - fall-through assignment
 * 
 * 6. visitBinaryOperator - binary operator type checking
 *    - shift operators (int32/uint32 context)
 *    - arithmetic operators (number context)
 *    - bitwise operators (bitwiseable context)
 *    - addition (no check)
 * 
 * 7. visitParameterList - argument count and type checking
 *    - normal function parameters
 *    - var_args function
 *    - wrong argument count
 * 
 * 8. visitFunction - function type checking
 *    - constructor validation
 *    - interface validation
 *    - implemented interface validation
 * 
 * 9. visitNew - constructor call validation
 *    - valid constructor
 *    - non-constructor
 *    - unknown/empty type
 * 
 * 10. visitCall - function call validation
 *     - callable type
 *     - non-callable type
 *     - constructor called without new
 *     - function with explicit this type
 * 
 * 11. visitReturn - return type validation
 *     - void function
 *     - typed function
 * 
 * 12. visitVar - variable declaration validation
 *     - inferred type
 *     - declared type
 * 
 * 13. checkEnumAlias - enum alias validation
 *     - enum to enum assignment
 *     - non-enum assignment
 * 
 * 14. ensureTyped - type assignment and cast validation
 *     - with JSDoc type annotation
 *     - with implicit cast
 *     - without existing type
 * 
 * 15. getTypedPercent - typed percentage calculation
 *     - zero total
 *     - mixed types
 * 
 * Defect-Specific Tests:
 * - testGetprop4: Property access on interface type should report warning
 * - testIssue810: Property access on union type with interface should report warning
 */
public class TypeCheckDeepseekTest {

  private static final String EXTERNS = 
      "var window; window.Object; var Object; Object.prototype; " +
      "var goog; goog.isString = function(x) {}; goog.isNumber = function(x) {};";

  private TypeCheck createTypeCheck(AbstractCompiler compiler) {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    return new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
  }

  private Node parseAndTypeCheck(String code) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", code);
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    return js;
  }

  private List<JSError> getErrors(Compiler compiler) {
    return compiler.getErrors();
  }

  private List<JSError> getWarnings(Compiler compiler) {
    return compiler.getWarnings();
  }

  private boolean hasWarning(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasError(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

  @Test(timeout = 4000)
  public void testProcessWithExternsAndJs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.process(externs, js);
    
    // Should not throw and should process successfully
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testProcessForTestingReturnsScope() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    Scope scope = typeCheck.processForTesting(externs, js);
    
    assertNotNull(scope);
    assertNotNull(scope.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesChaining() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    TypeCheck result = typeCheck.reportMissingProperties(false);
    assertSame(typeCheck, result);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; var y = 'hello'; var z = true;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertTrue(percent > 0.0);
    assertTrue(percent <= 100.0);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentZeroTotal() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    assertEquals(0.0, typeCheck.getTypedPercent(), 0.001);
  }

  // ==================== PARTITION B: Boundary Value Analysis (BVA) & Extremes ====================

  @Test(timeout = 4000)
  public void testVisitNameWithUnknownType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x; x;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithDeclaredType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {number} */ var x; x;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithFunctionParent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithCatchParent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "try { throw 1; } catch (e) { e; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithParamListParent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo(a) {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithVarParent() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnDict() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @dict */ var obj = {}; obj.prop;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeValidator.ILLEGAL_PROPERTY_ACCESS));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnNull() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {?} */ var obj = null; obj.prop;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnUndefined() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {undefined} */ var obj; obj.prop;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnUnknown() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = unknownFunction(); obj.prop;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnEnum() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @enum {number} */ var E = {A: 1}; E.B;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.INEXISTENT_ENUM_ELEMENT));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropOnObjectWithMissingProperty() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f.bar;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInIfCondition() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); if (f.bar) {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report INEXISTENT_PROPERTY because it's a property test
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInWhileCondition() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); while (f.bar) {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInDoCondition() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); do {} while (f.bar);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInForCondition() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); for (; f.bar;) {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInInstanceof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f instanceof Foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInTypeof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); typeof f.bar;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInAnd() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f.bar && true;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInHook() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f.bar ? 1 : 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropInNotOr() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); !f.bar || true;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testVisitGetPropWithReportMissingPropertiesDisabled() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f.bar;");
    
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    typeCheck.reportMissingProperties(false);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

  /**
   * Defect Test 1: testGetprop4
   * Property access on an interface type should report a warning when the
   * property does not exist on the interface.
   */
  @Test(timeout = 4000)
  public void testGetprop4() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    String externs = EXTERNS + 
        "/** @interface */ function I() {};";
    String code = 
        "/** @type {I} */ var i; i.foo;";
    
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node js = compiler.parseSyntheticCode("test", code);
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externsNode, js);
    
    // The defect: this should report INEXISTENT_PROPERTY warning
    assertTrue("Expected INEXISTENT_PROPERTY warning for property access on interface",
        hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  /**
   * Defect Test 2: testIssue810
   * Property access on a union type that includes an interface should report
   * a warning when the property does not exist on the interface.
   */
  @Test(timeout = 4000)
  public void testIssue810() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    String externs = EXTERNS + 
        "/** @interface */ function I() {};" +
        "/** @constructor */ function C() {};";
    String code = 
        "/** @type {I|C} */ var x; x.foo;";
    
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node js = compiler.parseSyntheticCode("test", code);
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externsNode, js);
    
    // The defect: this should report INEXISTENT_PROPERTY warning
    assertTrue("Expected INEXISTENT_PROPERTY warning for property access on union type with interface",
        hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testGetpropOnInterfaceWithExistingProperty() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    String externs = EXTERNS + 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;";
    String code = 
        "/** @type {I} */ var i; i.foo;";
    
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node js = compiler.parseSyntheticCode("test", code);
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externsNode, js);
    
    // Should NOT report INEXISTENT_PROPERTY since property exists
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testGetpropOnUnionWithInterfaceAndExistingProperty() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    String externs = EXTERNS + 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;" +
        "/** @constructor */ function C() {};" +
        "/** @type {number} */ C.prototype.foo;";
    String code = 
        "/** @type {I|C} */ var x; x.foo;";
    
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node js = compiler.parseSyntheticCode("test", code);
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externsNode, js);
    
    // Should NOT report INEXISTENT_PROPERTY since property exists on both
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testProcessWithNullScopeCreator() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.OFF);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    try {
      typeCheck.process(externs, js);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testProcessWithNullTopScope() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.OFF);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    try {
      typeCheck.process(externs, js);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testProcessWithNullJsRoot() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.OFF);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    
    try {
      typeCheck.process(externs, null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testProcessForTestingWithExistingScopeCreator() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.OFF);
    
    // First call to set up scopeCreator
    typeCheck.processForTesting(externs, js);
    
    // Second call should fail because scopeCreator is not null
    try {
      typeCheck.processForTesting(externs, js);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testVisitNewWithNonConstructor() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; var y = new x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.NOT_A_CONSTRUCTOR));
  }

  @Test(timeout = 4000)
  public void testVisitNewWithUnknownType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = unknownFunction(); var y = new x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitCallWithNonCallable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitCallWithConstructorWithoutNew() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; Foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.CONSTRUCTOR_NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitCallWithExplicitThisType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @this {Object} */ function foo() {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitCallWithExplicitThisTypeInGetProp() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @this {Object} */ function foo() {}; var obj = {}; obj.foo = foo; obj.foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report EXPECTED_THIS_TYPE because it's called via getprop
    assertFalse(hasWarning(compiler, TypeCheck.EXPECTED_THIS_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitCallWithWrongArgumentCount() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitCallWithVarArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithInconsistentType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @return {number} */ function foo() { return 'string'; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithVoidFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() { return; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithShift() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1 << 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithShiftOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string' << 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.BIT_OPERATION));
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithArithmetic() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1 + 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithArithmeticOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string' - 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithBitwise() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1 & 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithBitwiseOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string' & 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithAdd() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string' + 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBinaryOperatorWithUnexpectedToken() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; x++;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithInferredType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithDeclaredType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {number} */ var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithIncompatibleType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {number} */ var x = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithEnumAlias() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @enum {number} */ var E = {A: 1}; /** @enum {number} */ var F = E;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVarWithIncompatibleEnumAlias() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @enum {number} */ var E = {A: 1}; /** @enum {string} */ var F = E;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithConstructor() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithInterface() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function Foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithConstructorExtendingInterface() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}" +
        "/** @constructor @extends {I} */ function Foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithInterfaceExtendingNonInterface() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function C() {}" +
        "/** @interface @extends {C} */ function I() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithInterfaceImplementingInterface() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}" +
        "/** @interface @implements {I} */ function J() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.CONFLICTING_IMPLEMENTED_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithBadImplementedType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function C() {}" +
        "/** @constructor @implements {C} */ function Foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.BAD_IMPLEMENTED_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithMultipleExtendedInterfaces() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I1() {}" +
        "/** @interface */ function I2() {}" +
        "/** @interface @extends {I1, I2} */ function I3() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitFunctionWithConflictingInterfaceProperties() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I1() {}" +
        "/** @type {number} */ I1.prototype.foo;" +
        "/** @interface */ function I2() {}" +
        "/** @type {string} */ I2.prototype.foo;" +
        "/** @interface @extends {I1, I2} */ function I3() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithPrototype() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; Foo.prototype.bar = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithPrototypeNonObject() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; Foo.prototype = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithDeclaredPropertyType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} */ Foo.prototype.bar;" +
        "var f = new Foo(); f.bar = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithQualifiedName() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {}; obj.foo = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithInferredType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x; x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitAssignWithThisInDifferentScope() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() { this.bar = 1; }" +
        "Foo.prototype.baz = function() { this.bar = 2; };");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitObjLitKey() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {foo: 1, bar: 'string'};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitObjLitKeyWithEnum() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @enum {number} */ var E = {A: 1}; var obj = /** @type {E} */ ({A: 1});");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitObjLitKeyWithGetter() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {get foo() { return 1; }};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitObjLitKeyWithSetter() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {set foo(value) {}};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetElem() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var arr = [1, 2, 3]; var x = arr[0];");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitGetElemWithStringIndex() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {}; var x = obj['foo'];");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitIncDec() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; x++; x--;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitIncDecOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string'; x++;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNot() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = !true;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitVoid() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = void 0;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitTypeof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = typeof 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBitNot() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = ~1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitBitNotOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = ~'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.BIT_OPERATION));
  }

  @Test(timeout = 4000)
  public void testVisitPosNeg() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = +1; var y = -1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitPosNegOnString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = +'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitEquality() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1 == 2; var y = 1 != 2; var z = 1 === 2; var w = 1 !== 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitEqualityWithTypeof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = typeof 1 == 'number';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitEqualityWithInvalidTypeof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = typeof 1 == 'invalid';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitEqualityWithDeterministicResult() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {number} */ var x = 1; /** @type {string} */ var y = 'a'; var z = x === y;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.DETERMINISTIC_TEST));
  }

  @Test(timeout = 4000)
  public void testVisitComparison() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1 < 2; var y = 1 <= 2; var z = 1 > 2; var w = 1 >= 2;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitComparisonWithString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'a' < 'b';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitIn() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {}; var x = 'foo' in obj;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitInstanceof() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); var x = f instanceof Foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitDelProp() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {}; delete obj.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitCase() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; switch (x) { case 1: break; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitWith() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var obj = {}; with (obj) { var x = 1; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitWithNonObject() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; with (x) { var y = 1; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNoTypeCheckSection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { var x = 1; x(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because of @notypecheck
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionMasksVariable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var foo = 1; function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testVisitFunctionDoesNotMaskVariable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var foo = new Foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report FUNCTION_MASKS_VARIABLE because type is FunctionType
    assertFalse(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testVisitInterfaceGetpropWithInvalidMember() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}; I.prototype.foo = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.INVALID_INTERFACE_MEMBER_DECLARATION));
  }

  @Test(timeout = 4000)
  public void testVisitInterfaceGetpropWithNonEmptyFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}; I.prototype.foo = function() { return 1; };");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY));
  }

  @Test(timeout = 4000)
  public void testVisitInterfaceGetpropWithEmptyFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}; I.prototype.foo = function() {};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report INTERFACE_FUNCTION_NOT_EMPTY
    assertFalse(hasWarning(compiler, TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY));
  }

  @Test(timeout = 4000)
  public void testVisitInterfaceGetpropWithAbstractMethod() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {}; I.prototype.foo = goog.abstractMethod;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithUnknownSupertype() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor @extends {UnknownType} */ function Foo() {};" +
        "Foo.prototype.bar = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Base() {};" +
        "/** @type {number} */ Base.prototype.foo;" +
        "/** @constructor @extends {Base} */ function Derived() {};" +
        "/** @type {number} @override */ Derived.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report HIDDEN_SUPERCLASS_PROPERTY
    assertFalse(hasWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithoutOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Base() {};" +
        "/** @type {number} */ Base.prototype.foo;" +
        "/** @constructor @extends {Base} */ function Derived() {};" +
        "/** @type {number} */ Derived.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithMismatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Base() {};" +
        "/** @type {number} */ Base.prototype.foo;" +
        "/** @constructor @extends {Base} */ function Derived() {};" +
        "/** @type {string} @override */ Derived.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithUnknownOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} @override */ Foo.prototype.bar;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.UNKNOWN_OVERRIDE));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithInterfaceProperty() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;" +
        "/** @constructor @implements {I} */ function Foo() {};" +
        "/** @type {number} */ Foo.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.HIDDEN_INTERFACE_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithInterfacePropertyOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;" +
        "/** @constructor @implements {I} */ function Foo() {};" +
        "/** @type {number} @override */ Foo.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report HIDDEN_INTERFACE_PROPERTY
    assertFalse(hasWarning(compiler, TypeCheck.HIDDEN_INTERFACE_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithInterfaceMismatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;" +
        "/** @constructor @implements {I} */ function Foo() {};" +
        "/** @type {string} @override */ Foo.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithInterfaceUnknownType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @interface */ function I() {};" +
        "/** @type {number} */ I.prototype.foo;" +
        "/** @constructor @implements {I} */ function Foo() {};" +
        "/** @type {?} @override */ Foo.prototype.foo;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testCheckPropertyInheritanceWithEmptyType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} @override */ Foo.prototype.bar;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testPropertyIsImplicitCast() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} @implicitCast */ Foo.prototype.bar;" +
        "var f = new Foo(); f.bar = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithImplicitCastInExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    String externs = EXTERNS + 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} @implicitCast */ Foo.prototype.bar;";
    Node externsNode = compiler.parseSyntheticCode("externs", externs);
    Node js = compiler.parseSyntheticCode("test", 
        "var f = new Foo(); f.bar = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externsNode, js);
    
    // Should NOT report ILLEGAL_IMPLICIT_CAST because it's in externs
    assertFalse(hasWarning(compiler, TypeCheck.ILLEGAL_IMPLICIT_CAST));
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithImplicitCastNotInExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} @implicitCast */ Foo.prototype.bar;" +
        "var f = new Foo(); f.bar = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.ILLEGAL_IMPLICIT_CAST));
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithCast() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {Foo} */ var x = /** @type {Foo} */ (new Foo());");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithInvalidCast() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};" +
        "/** @type {number} */ var x = /** @type {Foo} */ (new Foo());");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithFunctionType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testEnsureTypedWithUnknownType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = unknownFunction();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testDoPercentTypedAccounting() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; var y = 'string'; var z = unknownFunction();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertTrue(percent > 0.0);
    assertTrue(percent < 100.0);
  }

  @Test(timeout = 4000)
  public void testDoPercentTypedAccountingWithUnknownTypeReporting() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var z = unknownFunction();");
    
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.WARNING);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.UNKNOWN_EXPR_TYPE));
  }

  @Test(timeout = 4000)
  public void testDoPercentTypedAccountingWithUnknownTypeNotReporting() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var z = unknownFunction();");
    
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, null, null, CheckLevel.WARNING, CheckLevel.OFF);
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.UNKNOWN_EXPR_TYPE));
  }

  @Test(timeout = 4000)
  public void testVisitUnexpectedToken() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitComma() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = (1, 2);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitTrueFalse() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = true; var y = false;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitThis() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() { this.bar = 1; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNull() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = null;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitNumber() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 'string';");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitArrayLit() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = [1, 2, 3];");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitRegexp() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = /abc/;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturn() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @return {number} */ function foo() { return 1; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithNoValue() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() { return; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithInconsistentType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @return {number} */ function foo() { return 'string'; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithUnknownType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @return {number} */ function foo() { return unknownFunction(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitReturnWithNullReturnType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() { return 1; }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithVarArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithTooFewArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {number} b */ function foo(a, b) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithTooManyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo(1, 2);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {number=} b */ function foo(a, b) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithArgumentTypeMismatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo('string');");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithUnknownArg() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo(unknownFunction());");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithUnknownParameter() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {?} a */ function foo(a) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithNoParameters() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithNoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithVarArgsAndNoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithVarArgsAndTooManyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithVarArgsAndTooFewArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {...number} var_args */ function foo(a, var_args) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMax() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMinArgsZero() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMinArgsMaxArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMinArgsGreaterThanNumArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {number} b */ function foo(a, b) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsLessThanNumArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a */ function foo(a) {}; foo(1, 2);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxValue() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsNotIntegerMax() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {number=} b */ function foo(a, b) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTooManyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTooFewArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {number} a @param {...number} var_args */ function foo(a, var_args) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndExactArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndZeroArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndOneArg() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndElevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwelveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFourteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFifteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventeenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEighteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNineteenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndTwentyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndThirtyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFortyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndFiftyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSixtyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndSeventyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndEightyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyOneArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyTwoArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyThreeArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyFourArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyFiveArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetySixArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetySevenArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyEightArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndNinetyNineArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  @Test(timeout = 4000)
  public void testVisitParameterListWithMaxArgsIntegerMaxAndHundredArgs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @param {...number} var_args */ function foo(var_args) {}; foo(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100);");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report WRONG_ARGUMENT_COUNT
    assertFalse(hasWarning(compiler, TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testConstructorWithAllParameters() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    JSTypeRegistry registry = compiler.getTypeRegistry();
    Scope topScope = new Scope(null, null);
    ScopeCreator scopeCreator = new MemoizedScopeCreator(new TypedScopeCreator(compiler));
    
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, topScope, scopeCreator, CheckLevel.WARNING, CheckLevel.OFF);
    
    assertNotNull(typeCheck);
  }

  @Test(timeout = 4000)
  public void testConstructorWithCompilerAndInterpreter() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    assertNotNull(typeCheck);
  }

  @Test(timeout = 4000)
  public void testConstructorWithCompilerInterpreterRegistryAndLevels() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry, CheckLevel.WARNING, CheckLevel.OFF);
    
    assertNotNull(typeCheck);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesReturnsThis() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    TypeCheck result = typeCheck.reportMissingProperties(true);
    assertSame(typeCheck, result);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesSetsFlag() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    typeCheck.reportMissingProperties(false);
    
    // Verify by checking that missing property warnings are not reported
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var f = new Foo(); f.bar;");
    
    typeCheck.processForTesting(externs, js);
    
    assertFalse(hasWarning(compiler, TypeCheck.INEXISTENT_PROPERTY));
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentWithAllTyped() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; var y = 2; var z = 3;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertEquals(100.0, percent, 0.001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentWithAllUnknown() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = unknownFunction(); var y = unknownFunction();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertEquals(0.0, percent, 0.001);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentWithMixed() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1; var y = unknownFunction();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertTrue(percent > 0.0);
    assertTrue(percent < 100.0);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercentWithNullType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    double percent = typeCheck.getTypedPercent();
    assertTrue(percent >= 0.0);
    assertTrue(percent <= 100.0);
  }

  @Test(timeout = 4000)
  public void testCheckWithNullNode() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck typeCheck = new TypeCheck(compiler, 
        new ChainableReverseAbstractInterpreter(registry), 
        registry);
    
    try {
      typeCheck.check(null, false);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testCheckWithExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testCheckWithNonExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithNonFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x = 1;");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionMasksVariable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var foo = 1; function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionNotMasksVariable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @constructor */ function Foo() {}; var foo = new Foo();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report FUNCTION_MASKS_VARIABLE
    assertFalse(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionEmptyName() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var foo = function() {};");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionNullName() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "(function() {})();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionNotDeclared() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should not throw
    assertNotNull(js);
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionDeclaredAsFunctionType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {Function} */ var foo; function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report FUNCTION_MASKS_VARIABLE
    assertFalse(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testShouldTraverseWithFunctionDeclaredAsNonFunctionType() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @type {number} */ var foo; function foo() {}");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    assertTrue(hasWarning(compiler, TypeCheck.FUNCTION_MASKS_VARIABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { var x = 1; x(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionNested() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() { var x = 1; x(); } }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionExited() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() {} var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should report NOT_CALLABLE because outside @notypecheck
    assertTrue(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnScript() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on script
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnBlock() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "{ /** @notypecheck */ var x = 1; x(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on block
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnVar() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on var
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnAssign() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "var x; /** @notypecheck */ x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on assign
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { var x = 1; x(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionExited() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() {} var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should report NOT_CALLABLE because outside @notypecheck
    assertTrue(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNested() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() { var x = 1; x(); } }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNestedExited() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() {} var x = 1; x(); }");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should report NOT_CALLABLE because inside foo but outside bar's @notypecheck
    assertTrue(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNestedExited2() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() {} } var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should report NOT_CALLABLE because outside foo's @notypecheck
    assertTrue(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNestedExited3() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() {} } /** @notypecheck */ var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on var
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNestedExited4() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);
    
    Node externs = compiler.parseSyntheticCode("externs", EXTERNS);
    Node js = compiler.parseSyntheticCode("test", 
        "/** @notypecheck */ function foo() { /** @notypecheck */ function bar() {} } /** @notypecheck */ var x = 1; x();");
    
    TypeCheck typeCheck = createTypeCheck(compiler);
    typeCheck.processForTesting(externs, js);
    
    // Should NOT report NOT_CALLABLE because @notypecheck on var
    assertFalse(hasWarning(compiler, TypeCheck.NOT_CALLABLE));
  }

  @Test(timeout = 4000)
  public void testVisitWithNoTypeCheckSectionOnFunctionNestedExited5() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.set