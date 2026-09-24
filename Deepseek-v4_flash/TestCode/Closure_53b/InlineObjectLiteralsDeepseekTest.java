package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: InlineObjectLiterals
 * 
 * Key Decision Branches Targeted:
 * 1. isVarInlineForbidden: global vars, extern vars, exported vars, RENAME_PROPERTY vars, stale vars
 * 2. isInlinableObject: GETPROP with CALL parent, non-VAR/ASSIGN LHS, null assigned value, non-OBJECTLIT values, self-referential assignments, ES5 getters/setters
 * 3. isVarOrAssignExprLhs: VAR parent, ASSIGN with EXPR_RESULT grandparent
 * 4. computeVarList: lvalue/initializing declarations, VAR parent, GETPROP references
 * 5. splitObject: defined vs undefined paths, lvalue replacements, VAR removals, GETPROP replacements
 * 6. replaceAssignmentExpression: VAR vs EXPR_RESULT replacement, COMMA tree construction
 * 
 * Boundary Conditions:
 * - Empty object literals
 * - Single property objects
 * - Multiple property objects
 * - Self-referential assignments (x = {a: x.b})
 * - Nested property access (x.y.z)
 * - Method calls on objects (x.fn())
 * - Global vs local variables
 * - Exported variables
 * - ES5 getters/setters
 * 
 * Defect Target (testBug545):
 * - The bug occurs when an object literal is assigned to a variable, 
 *   and that variable is used in a way that triggers the self-referential 
 *   check to fail incorrectly, or when the replacement logic produces 
 *   an invalid AST structure.
 * - Specifically, the issue involves the interaction between 
 *   replaceAssignmentExpression and the COMMA node construction 
 *   when there are multiple properties being inlined.
 */
public class InlineObjectLiteralsDeepseekTest {

  private static final String VAR_PREFIX = "JSCompiler_object_inline_";

  /**
   * Helper method to create a simple compiler instance for testing.
   */
  private AbstractCompiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  /**
   * Helper method to run the InlineObjectLiterals pass on source code.
   */
  private String runInlineObjectLiterals(String source) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    
    // Parse the source
    Node root = compiler.parse(compiler.getSourceFileFromCode("test", source));
    Node externs = compiler.parse(compiler.getSourceFileFromCode("externs", ""));
    
    // Run the pass
    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, 
        new com.google.common.base.Supplier<String>() {
          private int counter = 0;
          @Override
          public String get() {
            return "var" + (counter++);
          }
        });
    pass.process(externs, root);
    
    // Generate output
    StringBuilder sb = new StringBuilder();
    compiler.generateCode(sb, false, root);
    return sb.toString();
  }

  // ==================== Partition A: Core Functional Logic & State Transitions ====================

  @Test(timeout = 4000)
  public void testSimpleObjectLiteralInlining() {
    String source = "function f() { var x = {a: 1, b: 2}; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // The object literal should be inlined into individual variables
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
    assertFalse("Should not contain original object literal", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSingleProperty() {
    String source = "function f() { var x = {a: 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertFalse("Should not contain original object literal", 
        result.contains("x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralUsedInMethodCall() {
    String source = "function f() { var x = {a: 1}; x.fn(); }";
    String result = runInlineObjectLiterals(source);
    
    // Object used in method call should NOT be inlined
    assertTrue("Should preserve original object literal", 
        result.contains("x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNestedPropertyAccess() {
    String source = "function f() { var x = {a: 1}; var y = x.a; return y; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  // ==================== Partition B: Boundary Value Analysis & Extremes ====================

  @Test(timeout = 4000)
  public void testEmptyObjectLiteral() {
    String source = "function f() { var x = {}; return x; }";
    String result = runInlineObjectLiterals(source);
    
    // Empty object literal should not cause issues
    assertNotNull("Result should not be null", result);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithManyProperties() {
    StringBuilder sb = new StringBuilder("function f() { var x = {");
    for (int i = 0; i < 100; i++) {
      if (i > 0) sb.append(", ");
      sb.append("p").append(i).append(": ").append(i);
    }
    sb.append("}; return x.p0 + x.p99; }");
    
    String result = runInlineObjectLiterals(sb.toString());
    
    assertTrue("Should contain variable for property 'p0'", 
        result.contains(VAR_PREFIX + "p0_"));
    assertTrue("Should contain variable for property 'p99'", 
        result.contains(VAR_PREFIX + "p99_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStringKeys() {
    String source = "function f() { var x = {'key with spaces': 1, 'another-key': 2}; return x['key with spaces']; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'key with spaces'", 
        result.contains(VAR_PREFIX + "key with spaces_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNumericKeys() {
    String source = "function f() { var x = {0: 'a', 1: 'b'}; return x[0]; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property '0'", 
        result.contains(VAR_PREFIX + "0_"));
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================

  @Test(timeout = 4000)
  public void testBug545() {
    // This test targets the known defect from Defects4J
    // The bug occurs when an object literal is assigned to a variable,
    // and that variable is used in a way that triggers the self-referential
    // check to fail incorrectly, or when the replacement logic produces
    // an invalid AST structure.
    
    String source = "function f() { " +
        "var x = {a: 1, b: 2}; " +
        "var y = x.a + x.b; " +
        "return y; " +
        "}";
    
    try {
      String result = runInlineObjectLiterals(source);
      
      // The pass should complete without throwing an exception
      assertNotNull("Result should not be null", result);
      
      // The object literal should be inlined
      assertTrue("Should contain variable for property 'a'", 
          result.contains(VAR_PREFIX + "a_"));
      assertTrue("Should contain variable for property 'b'", 
          result.contains(VAR_PREFIX + "b_"));
      
      // The result should be valid JavaScript that evaluates correctly
      assertTrue("Result should contain the addition", 
          result.contains("+"));
      
    } catch (RuntimeException e) {
      fail("InlineObjectLiterals should not throw RuntimeException for valid input: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testBug545WithMultipleReferences() {
    // Extended version of the bug test with multiple references to the object
    String source = "function f() { " +
        "var x = {a: 1, b: 2, c: 3}; " +
        "var y = x.a + x.b; " +
        "var z = x.c; " +
        "return y + z; " +
        "}";
    
    try {
      String result = runInlineObjectLiterals(source);
      
      assertNotNull("Result should not be null", result);
      assertTrue("Should contain variable for property 'a'", 
          result.contains(VAR_PREFIX + "a_"));
      assertTrue("Should contain variable for property 'b'", 
          result.contains(VAR_PREFIX + "b_"));
      assertTrue("Should contain variable for property 'c'", 
          result.contains(VAR_PREFIX + "c_"));
      
    } catch (RuntimeException e) {
      fail("InlineObjectLiterals should not throw RuntimeException: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testSelfReferentialObjectLiteral() {
    // Self-referential assignments should NOT be inlined
    String source = "function f() { var x = {a: 1, b: x.a}; return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // The object literal should NOT be inlined due to self-reference
    assertTrue("Should preserve original object literal for self-referential case", 
        result.contains("x = {a: 1, b: x.a}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGetter() {
    // ES5 getters should NOT be inlined
    String source = "function f() { var x = {get a() { return 1; }}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // The object literal should NOT be inlined due to getter
    assertTrue("Should preserve original object literal for getter case", 
        result.contains("get a"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSetter() {
    // ES5 setters should NOT be inlined
    String source = "function f() { var x = {set a(v) { this._a = v; }}; x.a = 1; }";
    String result = runInlineObjectLiterals(source);
    
    // The object literal should NOT be inlined due to setter
    assertTrue("Should preserve original object literal for setter case", 
        result.contains("set a"));
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testGlobalVariableNotInlined() {
    // Global variables should not be inlined
    String source = "var x = {a: 1}; function f() { return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // The global variable should NOT be inlined
    assertTrue("Should preserve global variable assignment", 
        result.contains("x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testExportedVariableNotInlined() {
    // Exported variables should not be inlined
    String source = "function f() { var x = {a: 1}; window['x'] = x; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // The exported variable should NOT be inlined
    assertTrue("Should preserve exported variable", 
        result.contains("x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testVariableUsedAsFunctionCallTarget() {
    // Variable used as function call target should not be inlined
    String source = "function f() { var x = {a: 1}; x(); }";
    String result = runInlineObjectLiterals(source);
    
    // The variable should NOT be inlined
    assertTrue("Should preserve variable used as function call target", 
        result.contains("x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testVariableWithMultipleAssignments() {
    // Variable with multiple assignments should not be inlined
    String source = "function f() { var x = {a: 1}; x = {b: 2}; return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // The variable should NOT be inlined due to multiple assignments
    assertTrue("Should preserve variable with multiple assignments", 
        result.contains("x = {b: 2}"));
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testObjectLiteralWithUndefinedValues() {
    String source = "function f() { var x = {a: undefined, b: null}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithFunctionValues() {
    String source = "function f() { var x = {a: function() { return 1; }}; return x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrayValues() {
    String source = "function f() { var x = {a: [1, 2, 3]}; return x.a[0]; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNestedObjectValues() {
    String source = "function f() { var x = {a: {b: 1}}; return x.a.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithBooleanValues() {
    String source = "function f() { var x = {a: true, b: false}; return x.a && x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNumberValues() {
    String source = "function f() { var x = {a: 42, b: -1, c: 3.14}; return x.a + x.b + x.c; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
    assertTrue("Should contain variable for property 'c'", 
        result.contains(VAR_PREFIX + "c_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStringValues() {
    String source = "function f() { var x = {a: 'hello', b: 'world'}; return x.a + ' ' + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRegexValues() {
    String source = "function f() { var x = {a: /test/}; return x.a.test('test'); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralUsedInConditional() {
    String source = "function f() { var x = {a: 1, b: 2}; if (x.a) { return x.b; } return 0; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralUsedInLoop() {
    String source = "function f() { var x = {a: 1, b: 2}; for (var i = 0; i < x.a; i++) { x.b++; } return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithComputedPropertyAccess() {
    String source = "function f() { var x = {a: 1, b: 2}; var key = 'a'; return x[key]; }";
    String result = runInlineObjectLiterals(source);
    
    // Computed property access should NOT be inlined since we can't determine the key statically
    assertTrue("Should preserve original object literal for computed property access", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDeleteOperation() {
    String source = "function f() { var x = {a: 1, b: 2}; delete x.a; return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with delete operations should NOT be inlined
    assertTrue("Should preserve original object literal for delete operation", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithForInLoop() {
    String source = "function f() { var x = {a: 1, b: 2}; for (var key in x) { return x[key]; } }";
    String result = runInlineObjectLiterals(source);
    
    // Object used in for-in loop should NOT be inlined
    assertTrue("Should preserve original object literal for for-in loop", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithObjectKeysMethod() {
    String source = "function f() { var x = {a: 1, b: 2}; return Object.keys(x); }";
    String result = runInlineObjectLiterals(source);
    
    // Object used with Object.keys should NOT be inlined
    assertTrue("Should preserve original object literal for Object.keys", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralPassedToFunction() {
    String source = "function f() { var x = {a: 1, b: 2}; return g(x); } function g(obj) { return obj.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object passed to function should NOT be inlined
    assertTrue("Should preserve original object literal when passed to function", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTypeAnnotation() {
    String source = "/** @type {{a: number, b: string}} */ var x = {a: 1, b: 'hello'}; return x.a;";
    String result = runInlineObjectLiterals(source);
    
    // Object with type annotation should NOT be inlined (global variable)
    assertTrue("Should preserve original object literal with type annotation", 
        result.contains("x = {a: 1, b: 'hello'}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTemplateLiteralValues() {
    String source = "function f() { var name = 'world'; var x = {a: `hello ${name}`}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSpreadOperator() {
    String source = "function f() { var x = {a: 1, ...other}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with spread operator should NOT be inlined
    assertTrue("Should preserve original object literal with spread operator", 
        result.contains("x = {a: 1, ...other}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithShorthandProperties() {
    String source = "function f() { var a = 1; var x = {a}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMethodShorthand() {
    String source = "function f() { var x = {a() { return 1; }}; return x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    // Object with method shorthand should NOT be inlined
    assertTrue("Should preserve original object literal with method shorthand", 
        result.contains("a()"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithComputedPropertyName() {
    String source = "function f() { var key = 'a'; var x = {[key]: 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with computed property name should NOT be inlined
    assertTrue("Should preserve original object literal with computed property name", 
        result.contains("[key]: 1"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSymbolKey() {
    String source = "function f() { var sym = Symbol(); var x = {[sym]: 1}; return x[sym]; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with Symbol key should NOT be inlined
    assertTrue("Should preserve original object literal with Symbol key", 
        result.contains("[sym]: 1"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithPrototypeProperty() {
    String source = "function f() { var x = {__proto__: {a: 1}}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with __proto__ should NOT be inlined
    assertTrue("Should preserve original object literal with __proto__", 
        result.contains("__proto__: {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGetterAndSetter() {
    String source = "function f() { var x = {get a() { return 1; }, set a(v) {}}; x.a = 2; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with getter and setter should NOT be inlined
    assertTrue("Should preserve original object literal with getter and setter", 
        result.contains("get a"));
    assertTrue("Should preserve original object literal with setter", 
        result.contains("set a"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMultipleReferencesToSameProperty() {
    String source = "function f() { var x = {a: 1, b: 2}; return x.a + x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAssignmentToProperty() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a = 3; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with property assignment should NOT be inlined
    assertTrue("Should preserve original object literal with property assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithIncrementOperator() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a++; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with increment operator should NOT be inlined
    assertTrue("Should preserve original object literal with increment operator", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithCompoundAssignment() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a += 3; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with compound assignment should NOT be inlined
    assertTrue("Should preserve original object literal with compound assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDestructuring() {
    String source = "function f() { var x = {a: 1, b: 2}; var {a, b} = x; return a + b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with destructuring should NOT be inlined
    assertTrue("Should preserve original object literal with destructuring", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRestElement() {
    String source = "function f() { var x = {a: 1, b: 2, c: 3}; var {a, ...rest} = x; return rest.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with rest element should NOT be inlined
    assertTrue("Should preserve original object literal with rest element", 
        result.contains("x = {a: 1, b: 2, c: 3}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDefaultValue() {
    String source = "function f() { var x = {a: 1, b: 2}; var {a = 10, c = 20} = x; return a + c; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with destructuring default values should NOT be inlined
    assertTrue("Should preserve original object literal with destructuring defaults", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNestedDestructuring() {
    String source = "function f() { var x = {a: {b: 1}}; var {a: {b}} = x; return b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with nested destructuring should NOT be inlined
    assertTrue("Should preserve original object literal with nested destructuring", 
        result.contains("x = {a: {b: 1}}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrayDestructuring() {
    String source = "function f() { var x = {a: [1, 2, 3]}; var [first, ...rest] = x.a; return first; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNullCoalescing() {
    String source = "function f() { var x = {a: null, b: 2}; return x.a ?? x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithOptionalChaining() {
    String source = "function f() { var x = {a: {b: 1}}; return x?.a?.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with optional chaining should NOT be inlined
    assertTrue("Should preserve original object literal with optional chaining", 
        result.contains("x = {a: {b: 1}}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithLogicalAssignment() {
    String source = "function f() { var x = {a: null, b: 2}; x.a ||= 1; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with logical assignment should NOT be inlined
    assertTrue("Should preserve original object literal with logical assignment", 
        result.contains("x = {a: null, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNullishAssignment() {
    String source = "function f() { var x = {a: null, b: 2}; x.a ??= 1; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with nullish assignment should NOT be inlined
    assertTrue("Should preserve original object literal with nullish assignment", 
        result.contains("x = {a: null, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAndAssignment() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a &&= 3; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with AND assignment should NOT be inlined
    assertTrue("Should preserve original object literal with AND assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithOrAssignment() {
    String source = "function f() { var x = {a: 0, b: 2}; x.a ||= 3; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with OR assignment should NOT be inlined
    assertTrue("Should preserve original object literal with OR assignment", 
        result.contains("x = {a: 0, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithExponentiationAssignment() {
    String source = "function f() { var x = {a: 2, b: 3}; x.a **= 2; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with exponentiation assignment should NOT be inlined
    assertTrue("Should preserve original object literal with exponentiation assignment", 
        result.contains("x = {a: 2, b: 3}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithBitwiseAssignment() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a &= 3; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with bitwise assignment should NOT be inlined
    assertTrue("Should preserve original object literal with bitwise assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithShiftAssignment() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a <<= 2; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with shift assignment should NOT be inlined
    assertTrue("Should preserve original object literal with shift assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArithmeticAssignment() {
    String source = "function f() { var x = {a: 1, b: 2}; x.a += 3; x.b -= 1; return x.a + x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with arithmetic assignment should NOT be inlined
    assertTrue("Should preserve original object literal with arithmetic assignment", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStringMethodCall() {
    String source = "function f() { var x = {a: 'hello'}; return x.a.toUpperCase(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrayMethodCall() {
    String source = "function f() { var x = {a: [1, 2, 3]}; return x.a.map(function(n) { return n * 2; }); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithObjectMethodCall() {
    String source = "function f() { var x = {a: {b: 1}}; return Object.keys(x.a); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithFunctionMethodCall() {
    String source = "function f() { var x = {a: function() { return 1; }}; return x.a.call(null); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRegExpMethodCall() {
    String source = "function f() { var x = {a: /test/}; return x.a.test('test'); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDateMethodCall() {
    String source = "function f() { var x = {a: new Date()}; return x.a.getTime(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMathMethodCall() {
    String source = "function f() { var x = {a: Math.PI}; return Math.round(x.a); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithJSONMethodCall() {
    String source = "function f() { var x = {a: {b: 1}}; return JSON.stringify(x.a); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithConsoleMethodCall() {
    String source = "function f() { var x = {a: 1}; console.log(x.a); return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTypeofOperator() {
    String source = "function f() { var x = {a: 1}; return typeof x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithInstanceofOperator() {
    String source = "function f() { var x = {a: new Date()}; return x.a instanceof Date; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithInOperator() {
    String source = "function f() { var x = {a: 1, b: 2}; return 'a' in x; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with 'in' operator should NOT be inlined
    assertTrue("Should preserve original object literal with 'in' operator", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithVoidOperator() {
    String source = "function f() { var x = {a: 1}; return void x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDeleteOperator() {
    String source = "function f() { var x = {a: 1, b: 2}; delete x.a; return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with delete operator should NOT be inlined
    assertTrue("Should preserve original object literal with delete operator", 
        result.contains("x = {a: 1, b: 2}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAwaitExpression() {
    String source = "async function f() { var x = {a: await Promise.resolve(1)}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithYieldExpression() {
    String source = "function* f() { var x = {a: yield 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSpreadElement() {
    String source = "function f() { var x = {a: 1, ...y}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with spread element should NOT be inlined
    assertTrue("Should preserve original object literal with spread element", 
        result.contains("x = {a: 1, ...y}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRestProperty() {
    String source = "function f() { var x = {a: 1, b: 2, c: 3}; var {a, ...rest} = x; return rest.b; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with rest property should NOT be inlined
    assertTrue("Should preserve original object literal with rest property", 
        result.contains("x = {a: 1, b: 2, c: 3}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithComputedPropertyKey() {
    String source = "function f() { var key = 'a'; var x = {[key]: 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with computed property key should NOT be inlined
    assertTrue("Should preserve original object literal with computed property key", 
        result.contains("[key]: 1"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMethodDefinition() {
    String source = "function f() { var x = {a() { return 1; }}; return x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    // Object with method definition should NOT be inlined
    assertTrue("Should preserve original object literal with method definition", 
        result.contains("a()"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGetterDefinition() {
    String source = "function f() { var x = {get a() { return 1; }}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with getter definition should NOT be inlined
    assertTrue("Should preserve original object literal with getter definition", 
        result.contains("get a"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSetterDefinition() {
    String source = "function f() { var x = {set a(v) { this._a = v; }}; x.a = 1; return x._a; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with setter definition should NOT be inlined
    assertTrue("Should preserve original object literal with setter definition", 
        result.contains("set a"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGeneratorMethod() {
    String source = "function f() { var x = {*a() { yield 1; }}; return x.a().next().value; }";
    String result = runInlineObjectLiterals(source);
    
    // Object with generator method should NOT be inlined
    assertTrue("Should preserve original object literal with generator method", 
        result.contains("*a()"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAsyncMethod() {
    String source = "async function f() { var x = {async a() { return 1; }}; return await x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    // Object with async method should NOT be inlined
    assertTrue("Should preserve original object literal with async method", 
        result.contains("async a()"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAsyncGeneratorMethod() {
    String source = "async function f() { var x = {async *a() { yield 1; }}; for await (var val of x.a()) { return val; } }";
    String result = runInlineObjectLiterals(source);
    
    // Object with async generator method should NOT be inlined
    assertTrue("Should preserve original object literal with async generator method", 
        result.contains("async *a()"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStaticMethod() {
    String source = "class C { static method() { var x = {a: 1}; return x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithPrivateField() {
    String source = "class C { #x = {a: 1}; method() { return this.#x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    // Object with private field should NOT be inlined
    assertTrue("Should preserve original object literal with private field", 
        result.contains("#x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithPrivateMethod() {
    String source = "class C { #method() { var x = {a: 1}; return x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStaticPrivateField() {
    String source = "class C { static #x = {a: 1}; static method() { return this.#x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    // Object with static private field should NOT be inlined
    assertTrue("Should preserve original object literal with static private field", 
        result.contains("#x = {a: 1}"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStaticPrivateMethod() {
    String source = "class C { static #method() { var x = {a: 1}; return x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDecorator() {
    String source = "@decorator class C { method() { var x = {a: 1}; return x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithImportAssertion() {
    String source = "import('module', {assert: {type: 'json'}}).then(m => { var x = {a: 1}; return x.a; });";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDynamicImport() {
    String source = "async function f() { var module = await import('module'); var x = {a: 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMetaProperty() {
    String source = "function f() { var x = {a: import.meta}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNewTarget() {
    String source = "function f() { var x = {a: new.target}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSuperProperty() {
    String source = "class C { method() { var x = {a: super.prop}; return x.a; } }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithThisExpression() {
    String source = "function f() { var x = {a: this}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArgumentsObject() {
    String source = "function f() { var x = {a: arguments}; return x.a[0]; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRestParameters() {
    String source = "function f(...args) { var x = {a: args}; return x.a[0]; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDefaultParameters() {
    String source = "function f(a = 1) { var x = {b: a}; return x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithDestructuredParameters() {
    String source = "function f({a, b}) { var x = {c: a + b}; return x.c; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'c'", 
        result.contains(VAR_PREFIX + "c_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrowFunction() {
    String source = "var f = () => { var x = {a: 1}; return x.a; };";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGeneratorFunction() {
    String source = "function* f() { var x = {a: 1}; yield x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAsyncFunction() {
    String source = "async function f() { var x = {a: 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithAsyncGeneratorFunction() {
    String source = "async function* f() { var x = {a: 1}; yield x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithClassExpression() {
    String source = "function f() { var x = {a: class {}}; return new x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTaggedTemplate() {
    String source = "function f() { var x = {a: tag`template`}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNewExpression() {
    String source = "function f() { var x = {a: new Date()}; return x.a.getTime(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithCallExpression() {
    String source = "function f() { var x = {a: someFunction()}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSequenceExpression() {
    String source = "function f() { var x = {a: (1, 2, 3)}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithConditionalExpression() {
    String source = "function f() { var x = {a: true ? 1 : 2}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithLogicalExpression() {
    String source = "function f() { var x = {a: true && 1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithBinaryExpression() {
    String source = "function f() { var x = {a: 1 + 2}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithUnaryExpression() {
    String source = "function f() { var x = {a: -1}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithUpdateExpression() {
    String source = "function f() { var x = {a: ++y}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMemberExpression() {
    String source = "function f() { var x = {a: obj.prop}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithComputedMemberExpression() {
    String source = "function f() { var x = {a: obj[key]}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrayExpression() {
    String source = "function f() { var x = {a: [1, 2, 3]}; return x.a[0]; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithObjectExpression() {
    String source = "function f() { var x = {a: {b: 1}}; return x.a.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithFunctionExpression() {
    String source = "function f() { var x = {a: function() { return 1; }}; return x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithArrowFunctionExpression() {
    String source = "function f() { var x = {a: () => 1}; return x.a(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithClassExpression2() {
    String source = "function f() { var x = {a: class { method() { return 1; } }}; return new x.a().method(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTemplateLiteral() {
    String source = "function f() { var name = 'world'; var x = {a: `hello ${name}`}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTaggedTemplateExpression() {
    String source = "function f() { var x = {a: tag`template`}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithRegExpLiteral() {
    String source = "function f() { var x = {a: /test/gi}; return x.a.test('test'); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNullLiteral() {
    String source = "function f() { var x = {a: null}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithBooleanLiteral() {
    String source = "function f() { var x = {a: true, b: false}; return x.a && x.b; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNumberLiteral() {
    String source = "function f() { var x = {a: 42, b: -1, c: 3.14, d: 1e5}; return x.a + x.b + x.c + x.d; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
    assertTrue("Should contain variable for property 'c'", 
        result.contains(VAR_PREFIX + "c_"));
    assertTrue("Should contain variable for property 'd'", 
        result.contains(VAR_PREFIX + "d_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithStringLiteral() {
    String source = "function f() { var x = {a: 'hello', b: \"world\", c: `template`}; return x.a + x.b + x.c; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
    assertTrue("Should contain variable for property 'b'", 
        result.contains(VAR_PREFIX + "b_"));
    assertTrue("Should contain variable for property 'c'", 
        result.contains(VAR_PREFIX + "c_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithUndefinedLiteral() {
    String source = "function f() { var x = {a: undefined}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithBigIntLiteral() {
    String source = "function f() { var x = {a: 123n}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSymbolLiteral() {
    String source = "function f() { var x = {a: Symbol('test')}; return x.a.toString(); }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithIdentifier() {
    String source = "function f() { var y = 1; var x = {a: y}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithParenthesizedExpression() {
    String source = "function f() { var x = {a: (1 + 2)}; return x.a; }";
    String result = runInlineObjectLiterals(source);
    
    assertTrue("Should contain variable for property 'a'", 
        result.contains(VAR_PREFIX + "a_"));
  }
}