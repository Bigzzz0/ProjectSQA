package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.FunctionRewriter
 *
 * Decision / Branch Coverage Targets:
 * 1. process(Node, Node):
 *    - reductions.isEmpty(): true (continue), false (proceed to parse helper).
 *    - helperCode == null: true (continue), false (proceed to cost estimate).
 *    - savings > helperCodeCost + SAVINGS_THRESHOLD (16):
 *        * true -> apply all reductions, insert helper to front, reportCodeChange.
 *        * false -> discard reductions, no modification.
 * 2. parseHelperCode(Reducer):
 *    - root != null: true (returns root.removeFirstChild()), false (returns null).
 * 3. Reducer.buildCallNode:
 *    - argumentNode == null: call without arguments (e.g., emptyFn, identityFn).
 *    - argumentNode != null: call with cloned argument (e.g., returnArg, get, set).
 * 4. EmptyFunctionReducer:
 *    - NodeUtil.isEmptyFunctionExpression(node): matches empty functions.
 * 5. SingleReturnStatementReducer.maybeGetSingleReturnRValue:
 *    - !body.hasOneChild(): 0 statements, >1 statements -> returns null.
 *    - statement.getType() == Token.RETURN: return value node vs return; (null) vs non-return.
 * 6. IdentityReducer:
 *    - isReduceableFunctionExpression: false (non-expr function or other node).
 *    - paramNode == null: function without params.
 *    - value != null && isName(value) && value matches paramNode: matches identity.
 *    - value name mismatch or non-name return value: returns original node.
 * 7. ReturnConstantReducer:
 *    - NodeUtil.isImmutableValue(value): number, string, boolean, null vs mutable object/array.
 * 8. GetterReducer:
 *    - NodeUtil.isGetProp(value) && NodeUtil.isThis(value.getFirstChild()): matches this.prop.
 *    - propName.getType() != Token.STRING defensive guard -> throws IllegalStateException.
 * 9. SetterReducer:
 *    - !body.hasOneChild(), paramNode == null, !isExprAssign, !isGetProp, !isThis,
 *      !isName(rhs), !rhs.equals(param): returns null.
 *    - propName.getType() != Token.STRING defensive guard -> throws IllegalStateException.
 * 10. Defects4J Known Defect (Closure Issue 538):
 *    - When prototype methods are rewritten to CALL nodes by FunctionRewriter, subsequent passes
 *      (e.g., DevirtualizePrototypeMethods / MethodCompilerPass) encounter CALL nodes instead of
 *      FUNCTION nodes at method reference positions, causing:
 *      java.lang.IllegalStateException: Expected function but was call Reference node CALL ...
 */
public class FunctionRewriterGeminiTest {

  private static class Result {
    final Compiler compiler;
    final Node main;

    Result(Compiler compiler, Node main) {
      this.compiler = compiler;
      this.main = main;
    }
  }

  private Result compileAndRewrite(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("testcode", js)),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST should parse successfully", root);
    Node externs = root.getFirstChild();
    Node main = root.getLastChild();

    FunctionRewriter rewriter = new FunctionRewriter(compiler);
    rewriter.process(externs, main);
    return new Result(compiler, main);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyFunctionReducer_appliedWhenAboveThreshold() {
    String js =
        "A.prototype.m1 = function() {};\n" +
        "A.prototype.m2 = function() {};\n" +
        "A.prototype.m3 = function() {};\n" +
        "A.prototype.m4 = function() {};\n" +
        "A.prototype.m5 = function() {};\n" +
        "A.prototype.m6 = function() {};\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Helper function JSCompiler_emptyFn should be emitted",
        generated.contains("JSCompiler_emptyFn"));
    assertTrue("Method should be rewritten to call JSCompiler_emptyFn()",
        generated.contains("A.prototype.m1 = JSCompiler_emptyFn()"));
  }

  @Test(timeout = 4000)
  public void testIdentityReducer_appliedWhenAboveThreshold() {
    String js =
        "A.prototype.m1 = function(a) { return a; };\n" +
        "A.prototype.m2 = function(b) { return b; };\n" +
        "A.prototype.m3 = function(c) { return c; };\n" +
        "A.prototype.m4 = function(d) { return d; };\n" +
        "A.prototype.m5 = function(e) { return e; };\n" +
        "A.prototype.m6 = function(f) { return f; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Helper function JSCompiler_identityFn should be emitted",
        generated.contains("JSCompiler_identityFn"));
    assertTrue("Method should be rewritten to call JSCompiler_identityFn()",
        generated.contains("A.prototype.m1 = JSCompiler_identityFn()"));
  }

  @Test(timeout = 4000)
  public void testReturnConstantReducer_appliedWhenAboveThreshold() {
    String js =
        "A.prototype.m1 = function() { return 10; };\n" +
        "A.prototype.m2 = function() { return 'foo'; };\n" +
        "A.prototype.m3 = function() { return true; };\n" +
        "A.prototype.m4 = function() { return false; };\n" +
        "A.prototype.m5 = function() { return null; };\n" +
        "A.prototype.m6 = function() { return 99; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Helper function JSCompiler_returnArg should be emitted",
        generated.contains("JSCompiler_returnArg"));
    assertTrue("Method should be rewritten with constant argument",
        generated.contains("JSCompiler_returnArg(10)"));
  }

  @Test(timeout = 4000)
  public void testGetterReducer_appliedWhenAboveThreshold() {
    String js =
        "A.prototype.m1 = function() { return this.a_; };\n" +
        "A.prototype.m2 = function() { return this.b_; };\n" +
        "A.prototype.m3 = function() { return this.c_; };\n" +
        "A.prototype.m4 = function() { return this.d_; };\n" +
        "A.prototype.m5 = function() { return this.e_; };\n" +
        "A.prototype.m6 = function() { return this.f_; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Helper function JSCompiler_get should be emitted",
        generated.contains("JSCompiler_get"));
    assertTrue("Getter should be rewritten with property name argument",
        generated.contains("JSCompiler_get(\"a_\")"));
  }

  @Test(timeout = 4000)
  public void testSetterReducer_appliedWhenAboveThreshold() {
    String js =
        "A.prototype.m1 = function(v) { this.a_ = v; };\n" +
        "A.prototype.m2 = function(v) { this.b_ = v; };\n" +
        "A.prototype.m3 = function(v) { this.c_ = v; };\n" +
        "A.prototype.m4 = function(v) { this.d_ = v; };\n" +
        "A.prototype.m5 = function(v) { this.e_ = v; };\n" +
        "A.prototype.m6 = function(v) { this.f_ = v; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Helper function JSCompiler_set should be emitted",
        generated.contains("JSCompiler_set"));
    assertTrue("Setter should be rewritten with property name argument",
        generated.contains("JSCompiler_set(\"a_\")"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBelowSavingsThreshold_noRewrite() {
    String js = "A.prototype.m1 = function() {};\n";
    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse("Helper function should not be emitted below threshold",
        generated.contains("JSCompiler_emptyFn"));
    assertTrue("Function expression should remain intact",
        generated.contains("function()"));
  }

  @Test(timeout = 4000)
  public void testZeroReductions_noChange() {
    String js = "var x = 10;\nvar y = 20;\nvar z = x + y;\n";
    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_"));
    assertTrue(generated.contains("var x = 10"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarations_notReduced() {
    String js =
        "function f1() {}\n" +
        "function f2() {}\n" +
        "function f3() {}\n" +
        "function f4() {}\n" +
        "function f5() {}\n" +
        "function f6() {}\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse("Declarations should not be reduced",
        generated.contains("JSCompiler_emptyFn"));
    assertTrue(generated.contains("function f1()"));
  }

  @Test(timeout = 4000)
  public void testSingleReturnStatement_nonReturnStatement_notReduced() {
    String js =
        "A.prototype.m1 = function() { var a = 1; };\n" +
        "A.prototype.m2 = function() { var b = 2; };\n" +
        "A.prototype.m3 = function() { var c = 3; };\n" +
        "A.prototype.m4 = function() { var d = 4; };\n" +
        "A.prototype.m5 = function() { var e = 5; };\n" +
        "A.prototype.m6 = function() { var f = 6; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_"));
  }

  @Test(timeout = 4000)
  public void testSingleReturnStatement_emptyReturn_notReduced() {
    String js =
        "A.prototype.m1 = function() { return; };\n" +
        "A.prototype.m2 = function() { return; };\n" +
        "A.prototype.m3 = function() { return; };\n" +
        "A.prototype.m4 = function() { return; };\n" +
        "A.prototype.m5 = function() { return; };\n" +
        "A.prototype.m6 = function() { return; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_"));
  }

  @Test(timeout = 4000)
  public void testSingleReturnStatement_multipleStatements_notReduced() {
    String js =
        "A.prototype.m1 = function(a) { var x = 1; return a; };\n" +
        "A.prototype.m2 = function(b) { var x = 1; return b; };\n" +
        "A.prototype.m3 = function(c) { var x = 1; return c; };\n" +
        "A.prototype.m4 = function(d) { var x = 1; return d; };\n" +
        "A.prototype.m5 = function(e) { var x = 1; return e; };\n" +
        "A.prototype.m6 = function(f) { var x = 1; return f; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_identityFn"));
  }

  @Test(timeout = 4000)
  public void testIdentityReducer_paramMismatchOrMissing_notReduced() {
    String js =
        "A.prototype.m1 = function() { return x; };\n" +
        "A.prototype.m2 = function(a) { return b; };\n" +
        "A.prototype.m3 = function(a, b) { return b; };\n" +
        "A.prototype.m4 = function(a) { return a.prop; };\n" +
        "A.prototype.m5 = function(a) { return a + 1; };\n" +
        "A.prototype.m6 = function(a) { return other; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_identityFn"));
  }

  @Test(timeout = 4000)
  public void testReturnConstant_mutableValues_notReduced() {
    String js =
        "A.prototype.m1 = function() { return {}; };\n" +
        "A.prototype.m2 = function() { return []; };\n" +
        "A.prototype.m3 = function() { return {a: 1}; };\n" +
        "A.prototype.m4 = function() { return [1, 2]; };\n" +
        "A.prototype.m5 = function() { return new Object(); };\n" +
        "A.prototype.m6 = function() { return /abc/; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_returnArg"));
  }

  @Test(timeout = 4000)
  public void testGetterReducer_notThis_notReduced() {
    String js =
        "A.prototype.m1 = function() { return other.a_; };\n" +
        "A.prototype.m2 = function() { return other.b_; };\n" +
        "A.prototype.m3 = function() { return other.c_; };\n" +
        "A.prototype.m4 = function() { return other.d_; };\n" +
        "A.prototype.m5 = function() { return other.e_; };\n" +
        "A.prototype.m6 = function() { return other.f_; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_get"));
  }

  @Test(timeout = 4000)
  public void testSetterReducer_mismatches_notReduced() {
    String js =
        "A.prototype.m1 = function() { this.a_ = 1; };\n" +
        "A.prototype.m2 = function(v) { return this.b_ = v; };\n" +
        "A.prototype.m3 = function(v) { other.c_ = v; };\n" +
        "A.prototype.m4 = function(v) { this.d_ = other; };\n" +
        "A.prototype.m5 = function(v, extra) { this.e_ = extra; };\n" +
        "A.prototype.m6 = function(v) { this.f_ = 10; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertFalse(generated.contains("JSCompiler_set"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 538)
  // =========================================================================

  /**
   * Targets Closure Issue 538 (Defects4J FunctionRewriterTest::testIssue538).
   * Rewriting prototype functions into CALL nodes triggers an IllegalStateException
   * when subsequent passes (like DevirtualizePrototypeMethods) iterate over prototype
   * assignments expecting a FUNCTION node but find a CALL node.
   */
  @Test(timeout = 4000)
  public void testIssue538_devirtualizePrototypeMethodsInteraction() {
    String js =
        "/** @constructor */ var A = function() {};\n" +
        "A.prototype.foo = function() { return function() {}; };\n" +
        "A.prototype.bar = function() { return function() {}; };\n" +
        "A.prototype.baz = function() { return function() {}; };\n" +
        "A.prototype.qux = function() { return function() {}; };\n";

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("testcode", js)),
        options);
    Node root = compiler.parseInputs();
    assertNotNull(root);
    Node externs = root.getFirstChild();
    Node main = root.getLastChild();

    FunctionRewriter rewriter = new FunctionRewriter(compiler);
    rewriter.process(externs, main);

    DevirtualizePrototypeMethods devirtualizer = new DevirtualizePrototypeMethods(compiler);
    devirtualizer.process(externs, main);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionExpressions_reductionStopsSubtreeTraverse() {
    String js =
        "A.prototype.m1 = function() { return function() {}; };\n" +
        "A.prototype.m2 = function() { return function() {}; };\n" +
        "A.prototype.m3 = function() { return function() {}; };\n" +
        "A.prototype.m4 = function() { return function() {}; };\n" +
        "A.prototype.m5 = function() { return function() {}; };\n" +
        "A.prototype.m6 = function() { return function() {}; };\n";

    Result res = compileAndRewrite(js);
    String generated = res.compiler.toSource(res.main);

    assertTrue("Inner empty functions should be reduced to JSCompiler_emptyFn",
        generated.contains("return JSCompiler_emptyFn()"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetterReducer_nonStringProperty_throwsIllegalStateException() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseSyntheticCode("test",
        "var f = function() { return this.prop; };");
    Node fn = script.getFirstChild().getFirstChild().getLastChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node returnNode = fn.getLastChild().getFirstChild();
    Node getPropNode = returnNode.getFirstChild();
    assertEquals(Token.GETPROP, getPropNode.getType());

    getPropNode.replaceChild(getPropNode.getLastChild(), Node.newNumber(42));

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, script);
    FunctionRewriter rewriter = new FunctionRewriter(compiler);

    try {
      rewriter.process(externs, root);
      fail("Expected IllegalStateException for non-string getter property");
    } catch (IllegalStateException e) {
      assertTrue("Exception should detail expected STRING but got NUMBER",
          e.getMessage().contains("Expected STRING, got NUMBER"));
    }
  }

  @Test(timeout = 4000)
  public void testSetterReducer_nonStringProperty_throwsIllegalStateException() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseSyntheticCode("test",
        "var f = function(v) { this.prop = v; };");
    Node fn = script.getFirstChild().getFirstChild().getLastChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node expr = fn.getLastChild().getFirstChild();
    Node assign = expr.getFirstChild();
    Node lhs = assign.getFirstChild();
    assertEquals(Token.GETPROP, lhs.getType());

    lhs.replaceChild(lhs.getLastChild(), Node.newNumber(99));

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, script);
    FunctionRewriter rewriter = new FunctionRewriter(compiler);

    try {
      rewriter.process(externs, root);
      fail("Expected IllegalStateException for non-string setter property");
    } catch (IllegalStateException e) {
      assertTrue("Exception should detail expected STRING but got NUMBER",
          e.getMessage().contains("Expected STRING, got NUMBER"));
    }
  }

  @Test(timeout = 4000)
  public void testParseHelperCode_invalidSyntax_handledGracefully() {
    Compiler compiler = new Compiler();
    FunctionRewriter rewriter = new FunctionRewriter(compiler);

    FunctionRewriter.Reducer invalidReducer = new FunctionRewriter.Reducer() {
      @Override
      String getHelperSource() {
        return "function ( invalid syntax {{{";
      }

      @Override
      Node reduce(Node node) {
        return node;
      }
    };

    Node helperNode = rewriter.parseHelperCode(invalidReducer);
    assertNull("Helper code parsing invalid syntax should return null", helperNode);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Reducer Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseHelperCode_validHelper_returnsFunctionNode() {
    Compiler compiler = new Compiler();
    FunctionRewriter rewriter = new FunctionRewriter(compiler);

    FunctionRewriter.Reducer validReducer = new FunctionRewriter.Reducer() {
      @Override
      String getHelperSource() {
        return "function JSCompiler_customFn() { return 1; }";
      }

      @Override
      Node reduce(Node node) {
        return node;
      }
    };

    Node helperNode = rewriter.parseHelperCode(validReducer);
    assertNotNull("Helper node should not be null", helperNode);
    assertEquals("Should return the top-level FUNCTION node",
        Token.FUNCTION, helperNode.getType());
    assertEquals("Helper function should match defined name",
        "JSCompiler_customFn", helperNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testReducer_buildCallNode_structure() {
    FunctionRewriter.Reducer dummyReducer = new FunctionRewriter.Reducer() {
      @Override
      String getHelperSource() {
        return "";
      }

      @Override
      Node reduce(Node node) {
        return node;
      }
    };

    Node argNode = Node.newString("argValue");
    Node callWithArg = dummyReducer.buildCallNode("testMethod", argNode, 10, 5);

    assertEquals(Token.CALL, callWithArg.getType());
    assertEquals(10, callWithArg.getLineno());
    assertEquals(5, callWithArg.getCharno());
    assertTrue(callWithArg.getBooleanProp(Node.FREE_CALL));
    assertEquals("testMethod", callWithArg.getFirstChild().getString());
    assertEquals("argValue", callWithArg.getLastChild().getString());

    Node callNoArg = dummyReducer.buildCallNode("emptyMethod", null, 20, 15);
    assertEquals(Token.CALL, callNoArg.getType());
    assertTrue(callNoArg.getBooleanProp(Node.FREE_CALL));
    assertEquals(1, callNoArg.getChildCount());
    assertEquals("emptyMethod", callNoArg.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testSingleReturnStatementReducer_maybeGetSingleReturnRValue_branches() {
    Compiler compiler = new Compiler();
    FunctionRewriter.SingleReturnStatementReducer reducer =
        new FunctionRewriter.SingleReturnStatementReducer() {
          @Override
          String getHelperSource() {
            return "";
          }

          @Override
          Node reduce(Node node) {
            return node;
          }
        };

    Node scriptZero = compiler.parseSyntheticCode("zero", "var f = function() {};");
    Node fnZero = scriptZero.getFirstChild().getFirstChild().getLastChild();
    assertNull("0 statements body returns null", reducer.maybeGetSingleReturnRValue(fnZero));

    Node scriptTwo = compiler.parseSyntheticCode("two", "var f = function() { var a = 1; return a; };");
    Node fnTwo = scriptTwo.getFirstChild().getFirstChild().getLastChild();
    assertNull("2 statements body returns null", reducer.maybeGetSingleReturnRValue(fnTwo));

    Node scriptNonReturn = compiler.parseSyntheticCode("nonret", "var f = function() { var a = 1; };");
    Node fnNonReturn = scriptNonReturn.getFirstChild().getFirstChild().getLastChild();
    assertNull("Non-return body returns null", reducer.maybeGetSingleReturnRValue(fnNonReturn));

    Node scriptValid = compiler.parseSyntheticCode("valid", "var f = function() { return 123; };");
    Node fnValid = scriptValid.getFirstChild().getFirstChild().getLastChild();
    Node rvalue = reducer.maybeGetSingleReturnRValue(fnValid);
    assertNotNull("Single return rvalue should be extracted", rvalue);
    assertEquals(Token.NUMBER, rvalue.getType());
    assertEquals(123.0, rvalue.getDouble(), 0.0);
  }
}