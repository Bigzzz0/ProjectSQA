/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntax
 * Primary Defect Target: Issue 925 (Defects4J testIssue925)
 * Failure Condition: In control-flow exit optimization (`tryReplaceExitWithBreak`),
 *   infinite while loops (`while (true)`) have `ControlFlowAnalysis.computeFollowNode(breakTarget) == null`.
 *   The optimization mistakenly assumes that `follow == null` implies the break target is at the
 *   end of a function, rewriting `while (true) { return; }` into `while (1) break;` instead of
 *   preserving `return;` as `while (1) return;`.
 *
 * Decision / Branch Coverage Grid:
 * 1. Token.RETURN & Token.THROW:
 *    - `tryRemoveRedundantExit`: follow matching exit, exception handler check, pure vs impure expression
 *    - `tryReplaceExitWithBreak`: loop break targets vs function/script boundaries, follow == null handling (Issue 925)
 *    - `tryReduceReturn`: return undefined, return void 0, return void foo() (side effects)
 * 2. Token.NOT:
 *    - Complement operations: !(a == b) -> a != b, !(a != b) -> a == b, !(a === b) -> a !== b, !(a !== b) -> a === b
 *    - Non-invertible relations (NaN safety): !(a < b), !(a > b) remain unchanged
 * 3. Token.IF:
 *    - `tryMinimizeIf`: literal condition bail, if (!x) a() -> x || a(), if (x) a() -> x && a(),
 *      combining nested ifs, dangling else preservation, if (x) return 1; else return 2; -> return x ? 1 : 2,
 *      assignment folding: if (x) a = 1; else a = 2 -> a = x ? 1 : 2,
 *      var declaration folding: if (x) var y = 1; else y = 2 -> var y = x ? 1 : 2,
 *      repeated statement removal from then/else branches
 * 4. Token.BLOCK:
 *    - `tryReplaceIf`: if (x) return 1; if (y) return 1 -> if (x || y) return 1;
 *      if (x) return 1; if (y) f(); else return 1 -> if (!x && y) f(); else return 1;
 *      if (x) return; return 1 -> return x ? void 0 : 1;
 *      if (x) { return 1; } else { f(); } -> exit statement hoisting
 * 5. Token.FOR & Token.WHILE:
 *    - `tryJoinForCondition`: joins if (...) break into for condition when late == true
 *    - condition minimization: while(true) -> while(1)
 * 6. Token.NEW & Token.CALL:
 *    - `tryFoldStandardConstructors`: new Object() -> Object(), new Array(), new RegExp()
 *    - `tryFoldLiteralConstructor`: Object() -> {}, Array() -> [], Array(arg0, ...) -> [arg0, ...]
 *    - `tryFoldRegularExpressionConstructor`: RegExp('foo', 'i') -> /foo/i, escaping '/', line terminators, invalid flags
 *    - `tryFoldSimpleFunctionCall`: String(123) -> '' + 123
 *    - `tryFoldImmediateCallToBoundFunction`: (fn.bind(thisVal, p1))(arg) -> fn.call(thisVal, p1, arg)
 * 7. Token.COMMA & Token.NAME & Token.ARRAYLIT:
 *    - `trySplitComma`: splitting (a, b) into statements when late == false
 *    - `tryReplaceUndefined`: undefined -> void 0 when normalized
 *    - `tryMinimizeArrayLiteral`: string array joining/splitting when late == true
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class PeepholeSubstituteAlternateSyntaxGeminiTest {

  // Helper runner to parse code, run PeepholeSubstituteAlternateSyntax, and serialize to source
  private String fold(String js, boolean late) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(late);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(null, root);
    return compiler.toSource(root);
  }

  private void test(String js, String expected, boolean late) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node expectedRoot = compiler.parseTestCode(expected);
    String expectedSource = compiler.toSource(expectedRoot);
    String actualSource = fold(js, late);
    assertEquals(expectedSource, actualSource);
  }

  private void test(String js, String expected) {
    test(js, expected, true);
  }

  private void testSame(String js, boolean late) {
    test(js, js, late);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 925 & Exit Optimization)
  // =========================================================================

  /**
   * Targets Defects4J known failure condition: testIssue925.
   * `while (true) { return; }` must optimize to `while (1) return;` rather than
   * incorrectly replacing `return;` with `break;`.
   */
  @Test(timeout = 4000)
  public void testIssue925() {
    test("while (true) { return null; }", "while (1) return null;");
    test("while (true) { return; }", "while (1) return;");
    test("while (true) { return 0; }", "while (1) return 0;");
  }

  @Test(timeout = 4000)
  public void testIssue925InFunction() {
    test("function f() { while (true) { return; } }", "function f() { while (1) return; }");
    test("function f() { while (true) { return 1; } }", "function f() { while (1) return 1; }");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testMinimizeNotComplements() {
    test("!(a == b)", "a != b");
    test("!(a != b)", "a == b");
    test("!(a === b)", "a !== b");
    test("!(a !== b)", "a === b");
    // Relational comparisons should NOT be inverted due to NaN semantics
    test("!(a < b)", "!(a < b)");
    test("!(a >= b)", "!(a >= b)");
  }

  @Test(timeout = 4000)
  public void testTrueFalseReduction() {
    test("var x = true;", "var x = !0;", true);
    test("var x = false;", "var x = !1;", true);
    test("var x = true;", "var x = true;", false);
    test("var x = false;", "var x = false;", false);
  }

  @Test(timeout = 4000)
  public void testIfToHookReturns() {
    test("if (x) return 1; else return 2;", "return x ? 1 : 2;");
    test("if (x) return a(); else return b();", "return x ? a() : b();");
  }

  @Test(timeout = 4000)
  public void testIfToHookAssignments() {
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
    test("if (x) a = b(); else a = c();", "a = x ? b() : c();");
  }

  @Test(timeout = 4000)
  public void testIfToHookExpressions() {
    test("if (x) foo(); else bar();", "x ? foo() : bar();");
  }

  @Test(timeout = 4000)
  public void testIfToVarHook() {
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");
  }

  @Test(timeout = 4000)
  public void testIfSingleBranchLogical() {
    test("if (!x) bar();", "x || bar();");
    test("if (x) foo();", "x && foo();");
  }

  @Test(timeout = 4000)
  public void testInvertIfConditionWithElse() {
    test("if (!x) foo(); else bar();", "if (x) bar(); else foo();");
  }

  @Test(timeout = 4000)
  public void testBlockConsecutiveIfMerge() {
    test("if (x) return 1; if (y) return 1;", "if (x || y) return 1;");
    test("if (x) return 1; if (y) foo(); else return 1;", "if (!x && y) foo(); else return 1;");
  }

  @Test(timeout = 4000)
  public void testBlockIfReturnHook() {
    test("if (x) return; return 1;", "return x ? void 0 : 1;");
    test("if (x) return 1; return 2;", "return x ? 1 : 2;");
  }

  @Test(timeout = 4000)
  public void testReduceReturnUndefined() {
    test("function f() { return undefined; }", "function f() { return; }");
    test("function f() { return void 0; }", "function f() { return; }");
    testSame("function f() { return void foo(); }", true);
    testSame("function f() { return 1; }", true);
  }

  @Test(timeout = 4000)
  public void testConditionMinimizationDeMorgan() {
    test("while (!(x || y)) { foo(); }", "while (!x && !y) { foo(); }");
    test("while (!(x && y)) { foo(); }", "while (!x || !y) { foo(); }");
    test("while (!!x) { foo(); }", "while (x) { foo(); }");
  }

  @Test(timeout = 4000)
  public void testUselessConditionalsOptimization() {
    test("while (x || false) { foo(); }", "while (x) { foo(); }");
    test("while (x && true) { foo(); }", "while (x) { foo(); }");
    test("while (x ? true : false) { foo(); }", "while (x) { foo(); }");
    test("while (x ? false : true) { foo(); }", "while (!x) { foo(); }");
    test("while (x ? true : y) { foo(); }", "while (x || y) { foo(); }");
    test("while (x ? y : false) { foo(); }", "while (x && y) { foo(); }");
  }

  @Test(timeout = 4000)
  public void testTryFoldSimpleFunctionCallString() {
    test("var s = String('hello');", "var s = '' + 'hello';");
    test("var s = String(123);", "var s = '' + 123;");
    test("var s = String(true);", "var s = '' + true;");
    // Non-literal or multi-argument String calls must NOT be folded
    testSame("var s = String(x);", true);
    testSame("var s = String('a', 'b');", true);
    testSame("var s = String();", true);
  }

  @Test(timeout = 4000)
  public void testTryJoinForCondition() {
    test("for (; ; ) { if (x) break; }", "for (; !x; ) ;", true);
    test("for (; c; ) { if (x) break; }", "for (; c && !x; ) ;", true);
    testSame("for (; ; ) { if (x) break; }", false);
  }

  @Test(timeout = 4000)
  public void testTrySplitComma() {
    test("(a, b);", "a; b;", false);
    testSame("(a, b);", true);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testStringArraySplittingThreshold() {
    // 7 elements: 7 * 2 - 11 = 3 > 0 bytes savings -> folded to string split
    test("var a = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];", "var a = 'abcdefg'.split('');", true);
    // 3 elements: 3 * 2 - 11 = -5 <= 0 bytes savings -> kept as array literal
    testSame("var a = ['a', 'b', 'c'];", true);
    // Non-string elements -> kept as array literal
    testSame("var a = ['a', 'b', 'c', 'd', 'e', 'f', 1];", true);
    // When late is false, do not split
    testSame("var a = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];", false);
  }

  @Test(timeout = 4000)
  public void testStringArraySplittingCustomDelimiter() {
    test("var a = ['foo', 'bar', 'baz', 'qux', 'quux', 'corge', 'grault'];",
         "var a = 'foo bar baz qux quux corge grault'.split(' ');", true);
  }

  @Test(timeout = 4000)
  public void testRegexpForwardSlashAndEscapeHandling() {
    // Normal regex folding
    test("var r = new RegExp('abc', 'i');", "var r = /abc/i;");
    // Forward slashes bracket safe
    test("var r = new RegExp('/');", "var r = /\\//;");
    test("var r = new RegExp('[/]');", "var r = /[/]/;");
    // Line terminators escaping
    test("var r = new RegExp('\\n');", "var r = /\\n/;");
    test("var r = new RegExp('\\r');", "var r = /\\r/;");
    // Empty pattern should NOT fold to // (which is a comment)
    testSame("var r = new RegExp('');", true);
    // Flags validation: invalid flags must NOT fold
    testSame("var r = new RegExp('abc', 'z');", true);
  }

  @Test(timeout = 4000)
  public void testContainsUnicodeEscapeDirect() {
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\u0041"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\\\u0041"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("abc"));
  }

  @Test(timeout = 4000)
  public void testFoldArrayConstructorBoundaries() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(true);
    peephole.beginTraversal(new NodeTraversal(compiler, peephole));

    // Array() -> []
    Node callZero = IR.call(IR.name("Array"));
    Node foldedZero = peephole.optimizeSubtree(callZero);
    assertTrue(foldedZero.isArrayLit());
    assertEquals(0, foldedZero.getChildCount());

    // Array(0) -> []
    Node callNumZero = IR.call(IR.name("Array"), IR.number(0));
    Node foldedNumZero = peephole.optimizeSubtree(callNumZero);
    assertTrue(foldedNumZero.isArrayLit());
    assertEquals(0, foldedNumZero.getChildCount());

    // Array('foo') -> ['foo']
    Node callStr = IR.call(IR.name("Array"), IR.string("foo"));
    Node foldedStr = peephole.optimizeSubtree(callStr);
    assertTrue(foldedStr.isArrayLit());
    assertEquals(1, foldedStr.getChildCount());

    // Array(10) -> unsafe to fold (allocates buffer)
    Node callTen = IR.call(IR.name("Array"), IR.number(10));
    Node foldedTen = peephole.optimizeSubtree(callTen);
    assertFalse(foldedTen.isArrayLit());
  }

  @Test(timeout = 4000)
  public void testFoldObjectConstructor() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(true);
    peephole.beginTraversal(new NodeTraversal(compiler, peephole));

    // Object() -> {}
    Node callObj = IR.call(IR.name("Object"));
    Node foldedObj = peephole.optimizeSubtree(callObj);
    assertTrue(foldedObj.isObjectLit());

    // Object(arg) -> do not fold
    Node callObjWithArg = IR.call(IR.name("Object"), IR.number(1));
    Node foldedObjWithArg = peephole.optimizeSubtree(callObjWithArg);
    assertFalse(foldedObjWithArg.isObjectLit());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsPureAndExceptionPossibleGuards() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    // Null node is pure
    assertTrue(peephole.isPure(null));
    // Number literal is pure
    assertTrue(peephole.isPure(IR.number(123)));
    // Function call is impure
    assertFalse(peephole.isPure(IR.call(IR.name("fn"))));

    // Exit exception possibilities
    Node throwNode = IR.throwNode(IR.string("err"));
    assertTrue(peephole.isExceptionPossible(throwNode));

    Node returnLiteral = IR.returnNode(IR.number(1));
    assertFalse(peephole.isExceptionPossible(returnLiteral));

    Node returnCall = IR.returnNode(IR.call(IR.name("fn")));
    assertTrue(peephole.isExceptionPossible(returnCall));
  }

  @Test(timeout = 4000)
  public void testSkipFinallyNodesGuards() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    assertNull(peephole.skipFinallyNodes(null));
    Node standalone = IR.returnNode();
    assertSame(standalone, peephole.skipFinallyNodes(standalone));
  }

  @Test(timeout = 4000)
  public void testAreMatchingExitsContract() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node ret1 = IR.returnNode(IR.number(1));
    Node ret2 = IR.returnNode(IR.number(1));
    Node ret3 = IR.returnNode(IR.number(2));

    assertTrue(peephole.areMatchingExits(ret1, ret2));
    assertFalse(peephole.areMatchingExits(ret1, ret3));
  }

  @Test(timeout = 4000)
  public void testRepeatedStatementRemovalInIf() {
    test("if (a) { x = 1; return true; } else { x = 2; return true; }",
         "if (a) { x = 1; } else { x = 2; } return true;");
  }

  @Test(timeout = 4000)
  public void testUnchangedSubtreeFallback() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Node numberNode = IR.number(42);
    Node optimized = peephole.optimizeSubtree(numberNode);
    assertSame(numberNode, optimized);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testInstantiationAndLateFlag() {
    PeepholeSubstituteAlternateSyntax early = new PeepholeSubstituteAlternateSyntax(false);
    assertNotNull(early);

    PeepholeSubstituteAlternateSyntax late = new PeepholeSubstituteAlternateSyntax(true);
    assertNotNull(late);
  }

  @Test(timeout = 4000)
  public void testDontTraverseFunctionsPredicate() {
    Node fn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    assertFalse(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(fn));

    Node block = IR.block();
    assertTrue(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(block));
  }
}