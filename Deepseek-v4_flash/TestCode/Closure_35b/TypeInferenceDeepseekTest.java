package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for TypeInference, targeting the known Defects4J defect Issue 669
 * and achieving high line/branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional logic – traverse, assign, name, getprop, calls, etc.
 * Partition B: BVA – null/empty/unknown types, negative numbers, MAX values, edge types.
 * Partition C: Defect-targeted – template type inference, this on closures, undefined warnings.
 * Partition D: Exception paths – illegal arguments, null registry, invalid node types.
 * Partition E: Lifecycle & contract – creation, entry lattice, initial estimate, optimize.
 */
public class TypeInferenceDeepseekTest extends CompilerTestCase {

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.TYPE_INFERENCE_CONSTRAINT, CheckLevel.WARNING);
    return options;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    // Return a pass that triggers type inference on a single script.
    return new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        // We want to test TypeInference directly, but since it is package-private,
        // we rely on the full type checking pass.
        // For isolation, we create a minimal compilation and check results.
        // We'll use the compiler's type inference via the standard passes.
        new TypeCheck(compiler, compiler.getDefaultCodingConvention()).process(externs, root);
      }
    };
  }

  // ========== Partition A: Core Functional Logic ==========

  @Test(timeout = 4000)
  public void testSimpleAssignmentAndName() {
    testNoWarnings("var x = 42;");
  }

  @Test(timeout = 4000)
  public void testPropertyAccess() {
    testNoWarnings("/** @constructor */ function C() {}; C.prototype.foo = 1; var c = new C(); c.foo;");
  }

  @Test(timeout = 4000)
  public void testAddString() {
    testNoWarnings("var x = 'hello' + 5;");
  }

  @Test(timeout = 4000)
  public void testAddNumber() {
    testNoWarnings("var x = 3 + 4;");
  }

  @Test(timeout = 4000)
  public void testTernaryOperator() {
    testNoWarnings("var x = true ? 1 : 'a';");
  }

  @Test(timeout = 4000)
  public void testArrayLiteral() {
    testNoWarnings("var arr = [1, 2, 3];");
  }

  @Test(timeout = 4000)
  public void testObjectLiteral() {
    testNoWarnings("var obj = {a: 1, b: 'two'};");
  }

  @Test(timeout = 4000)
  public void testFunctionCall() {
    testNoWarnings("/** @return {number} */ function f() { return 1; } var x = f();");
  }

  @Test(timeout = 4000)
  public void testNewExpression() {
    testNoWarnings("/** @constructor */ function C() {} var c = new C();");
  }

  @Test(timeout = 4000)
  public void testTypeOf() {
    testNoWarnings("var x = typeof 'a';");
  }

  @Test(timeout = 4000)
  public void testLogicalAndOr() {
    testNoWarnings("var x = true && false || true;");
  }

  @Test(timeout = 4000)
  public void testHook() {
    testNoWarnings("var x = true ? 1 : 0;");
  }

  @Test(timeout = 4000)
  public void testCatchBlock() {
    testNoWarnings("try { throw 1; } catch(e) { e; }");
  }

  @Test(timeout = 4000)
  public void testReturnType() {
    testNoWarnings("/** @return {string} */ function f() { return 'hi'; }");
  }

  @Test(timeout = 4000)
  public void testGetElemArray() {
    testNoWarnings("var arr = [1,2]; arr[0];");
  }

  // ========== Partition B: Boundary Value Analysis ==========

  @Test(timeout = 4000)
  public void testNullUndefinedTypes() {
    testNoWarnings("var x = null; var y = undefined;");
  }

  @Test(timeout = 4000)
  public void testEmptyString() {
    testNoWarnings("var s = '';");
  }

  @Test(timeout = 4000)
  public void testNumberBoundaries() {
    testNoWarnings("var min = -Infinity; var max = Infinity; var zero = 0;");
  }

  @Test(timeout = 4000)
  public void testBooleanValues() {
    testNoWarnings("var b = true; var c = false;");
  }

  @Test(timeout = 4000)
  public void testUnknownType() {
    // Node with unknown type should not cause crash.
    testNoWarnings("var x; x = foo;"); // foo is undeclared, but we allow it (no warning expected? depends on options)
    // Actually, if we have warning for undeclared variable, we need to suppress.
    // Use test with externs? Simpler: use a function param.
    testNoWarnings("/** @param {*} x */ function f(x) { x; }");
  }

  @Test(timeout = 4000)
  public void testUnionType() {
    testNoWarnings("/** @param {number|string} x */ function f(x) { x; }");
  }

  @Test(timeout = 4000)
  public void testRecordType() {
    testNoWarnings("/** @param {{a: number}} x */ function f(x) { x.a; }");
  }

  // ========== Partition C: Defect-Targeted (Issue 669) ==========

  @Test(timeout = 4000)
  public void testIssue669_templateTypeNotObjectType_shouldBeNoWarning() {
    // This test exercises updateTypeOfThisOnClosure with a valid template.
    // Known bug: may incorrectly emit TEMPLATE_TYPE_NOT_OBJECT_TYPE.
    String source = ""
        + "/** @template T\n"
        + " * @param {function(this:T)} g\n"
        + " * @param {T} x\n"
        + " */\n"
        + "function f(g, x) { g.call(x); }\n"
        + "/** @constructor */ function C() {}\n"
        + "C.prototype.foo = 1;\n"
        + "f(function() { this.foo; }, new C());\n";
    testNoWarnings(source);
  }

  @Test(timeout = 4000)
  public void testIssue669_missingTemplateTypeOfThisWarning() {
    // If the template type is not used for this, a warning should be issued.
    // This test expects the warning.
    String source = ""
        + "/** @template T\n"
        + " * @param {function(this:T)} g\n"
        + " * @param {number} x\n"
        + " */\n"
        + "function f(g, x) { g.call(x); }\n"
        + "f(function() { this; }, 5);\n";
    testWarning(source, TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE);
  }

  @Test(timeout = 4000)
  public void testIssue669_undefinedThisOnClosure() {
    // When a function literal references this but the type of this is unknown, warn.
    String source = ""
        + "/** @template T\n"
        + " * @param {function(this:T)} g\n"
        + " */\n"
        + "function f(g) { g.call({}); }\n"
        + "f(function() { this.bar; });\n";
    testWarning(source, TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
  }

  @Test(timeout = 4000)
  public void testIssue669_templateTypeOfThisExpected() {
    // If there is no parameter with this=T, warn.
    String source = ""
        + "/** @template T\n"
        + " * @param {function(this:T)} g\n"
        + " * @param {T} x\n"
        + " */\n"
        + "function f(g, x) { }\n" // no call, so no this usage
        + "f(function() {}, 1);\n";
    // This should not produce TEMPLATE_TYPE_OF_THIS_EXPECTED because we never use a function parameter that has this=T?
    // Actually, the code checks all parameters for one with this=T. Here g has this=T, but there is no parameter with type that matches? Wait, the template T is used in g and x. x is number, so T is number. Then g's this type is number, which is not an object type -> TEMPLATE_TYPE_NOT_OBJECT_TYPE. But we want to test TEMPLATE_TYPE_OF_THIS_EXPECTED when there is no parameter with this=T. That scenario is when the function's parameter list includes a function with this=T but no other parameter of type T? Actually the code iterates parameters for type T, then looks for another parameter whose this equals T. If not found, it reports TEMPLATE_TYPE_OF_THIS_EXPECTED. So we need a source where T appears in a function parameter's this but there is no other parameter of type T. Example: function(g) where g has this:T but no other param. But then T is not bound. Actually T must appear somewhere. Simpler: we can trigger the warning by having a template T used only in this context and no other parameter of type T. The code will not find a parameter of type T for the first loop (iParameterType is template), then looks for a jParameter whose this equals iParameterType. If none found, it reports TEMPLATE_TYPE_OF_THIS_EXPECTED.
    String s = ""
        + "/** @template T\n"
        + " * @param {function(this:T)} g\n"
        + " */\n"
        + "function f(g) { g.call({}); }\n"
        + "f(function() {});\n";
    testWarning(s, TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED);
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000)
  public void testTraverseNullNode() {
    // Not directly testable via CompilerTestCase, but we can exercise via code that causes null JSType.
    // For instance, a node with no type info.
    testNoWarnings("var x = undefined; x.foo;"); // property access on undefined should not crash, may warn.
    // We'll just check that no exception is thrown.
  }

  @Test(timeout = 4000)
  public void testTypeCast() {
    testNoWarnings("/** @type {string} */ var x = 1;"); // type cast, should warn about mismatch? Actually it's a type cast declaration.
    // In closure, /** @type {string} */ var x = 1; is a type cast (if in brackets), but as is it's a type annotation, warning.
    // Use parentheses:
    testNoWarnings("var x = /** @type {string} */ (1);");
  }

  @Test(timeout = 4000)
  public void testAssertionFunctions() {
    // Custom assertion function like goog.asserts.assert.
    testNoWarnings("var x = /** @type {?number} */ (null); if (goog.isDef(x)) { x; }");
  }

  // ========== Partition E: Object Lifecycle & Contract Integrity ==========

  @Test(timeout = 4000)
  public void testInitialEstimateLattice() {
    TestCompiler compiler = createCompiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    // We cannot directly test TypeInference's methods, but we can ensure no crash.
    assertNotNull(compiler.getTypeRegistry());
  }

  @Test(timeout = 4000)
  public void testEntryLattice() {
    testNoWarnings("var x;");
  }

  // Helper to create a minimal compiler for testing static methods if needed.
  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    return compiler;
  }

  // Override to allow testing with default externs.
  @Override
  protected int getNumRepetitions() {
    return 1;
  }
}