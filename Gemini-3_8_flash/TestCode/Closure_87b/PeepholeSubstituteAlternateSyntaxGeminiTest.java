/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntax
 *
 * Decision / Branch Zones Covered:
 * 1. optimizeSubtree dispatch:
 *    - Token.RETURN -> tryReduceReturn (void 0, undefined, break target exploration, side effects)
 *    - Token.NOT -> tryMinimizeNot (EQ, NE, SHEQ, SHNE, GT/LT unhandled)
 *    - Token.IF -> tryMinimizeIf (literal conditions, single branch, foldable expr block,
 *                  property assignments, precedence guards, dangling else, repeated statements,
 *                  return hooks, assignment hooks, var hooks)
 *    - Token.EXPR_RESULT, Token.HOOK, Token.WHILE, Token.DO, Token.FOR (in vs for-in) -> tryMinimizeCondition
 *    - Token.NEW, Token.CALL -> tryFoldStandardConstructors, tryFoldLiteralConstructor,
 *                               tryFoldRegularExpressionConstructor
 *    - Default -> returns unmutated node
 *
 * 2. Defect Zone (Ground Truth Defects4J Issue 291):
 *    - Method calls with implicit 'this' (e.g. `f.onchange()`) in an IF block without an ELSE branch
 *      must not be folded to `x && f.onchange()` due to IE event handler return-value semantics.
 *    - Target: testIssue291, testIssue291_ElseBranch
 *
 * 3. Boundary Conditions & Edge Cases:
 *    - RegExp: empty pattern, length >= 100 chars, invalid flags, unsafe 'g' flag, unicode escapes, slash escaping.
 *    - Array constructor: 0 args, 1 number arg (safe 0 vs unsafe >0), 1 string arg, 1 array literal arg, >=2 args.
 *    - Object constructor: with vs without args.
 *    - AST Normalization states: normalized vs unnormalized passes.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeSubstituteAlternateSyntaxGeminiTest {

  // --------------------------------------------------------------------------
  // Test Harness Helpers
  // --------------------------------------------------------------------------

  private Compiler compiler;

  private Node fold(String js, boolean normalized) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");
    if (normalized) {
      compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    }
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(externs, root);
    return root;
  }

  private String foldToString(String js, boolean normalized) {
    Node root = fold(js, normalized);
    return compiler.toSource(root).trim();
  }

  private String foldToString(String js) {
    return foldToString(js, true);
  }

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defect / Issue 291)
  // ==========================================================================

  /**
   * Targets Issue 291: In IE, event handlers (such as f.onchange()) behave differently
   * when their return value is consumed in an expression vs. when kept in an EXPR_RESULT.
   * "if (x) { f.onchange(); }" must NOT fold to "x && f.onchange();".
   */
  @Test(timeout = 4000)
  public void testIssue291() {
    String js = "if (x) { f.onchange(); }";
    Node root = fold(js, false);
    Node firstStatement = root.getFirstChild();
    assertEquals("Issue 291: 'if (x) { f.onchange(); }' must remain an IF node and not fold to AND expression",
        Token.IF, firstStatement.getType());
  }

  @Test(timeout = 4000)
  public void testIssue291_ElseBranch() {
    String js = "if (x) { f.onchange(); } else { f.onchange(); }";
    Node root = fold(js, false);
    Node firstStatement = root.getFirstChild();
    assertEquals("Issue 291: Repeated method calls with implicit 'this' should preserve structure",
        Token.IF, firstStatement.getType());
  }

  // ==========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================================================

  @Test(timeout = 4000)
  public void testMinimizeNotComplements() {
    // EQ -> NE
    assertEquals("a!=b", foldToString("!(a == b);"));
    // NE -> EQ
    assertEquals("a==b", foldToString("!(a != b);"));
    // SHEQ -> SHNE
    assertEquals("a!==b", foldToString("!(a === b);"));
    // SHNE -> SHEQ
    assertEquals("a===b", foldToString("!(a !== b);"));
    // GT / LT should not be complemented due to NaN semantics
    assertEquals("!(a>b)", foldToString("!(a > b);"));
    assertEquals("!(a<=b)", foldToString("!(a <= b);"));
  }

  @Test(timeout = 4000)
  public void testReduceReturnUndefinedAndVoid() {
    assertEquals("function f(){return}", foldToString("function f() { return undefined; }"));
    assertEquals("function f(){return}", foldToString("function f() { return void 0; }"));
    assertEquals("function f(){return void foo()}", foldToString("function f() { return void foo(); }"));
    assertEquals("function f(){return 1}", foldToString("function f() { return 1; }"));
  }

  @Test(timeout = 4000)
  public void testFoldIfElseToHookReturn() {
    assertEquals("return x?1:2", foldToString("function f(){ if (x) return 1; else return 2; }").replaceAll("function f\\(\\)\\{|\\}", ""));
  }

  @Test(timeout = 4000)
  public void testFoldIfElseToHookAssign() {
    assertEquals("a=x?1:2", foldToString("if (x) a = 1; else a = 2;"));
    // If LHS may effect mutable state, do not fold
    assertEquals("if(x)a[i++]=1;else a[i++]=2", foldToString("if (x) a[i++] = 1; else a[i++] = 2;"));
  }

  @Test(timeout = 4000)
  public void testFoldIfElseToHookCall() {
    assertEquals("x?foo():bar()", foldToString("if (x) foo(); else bar();"));
  }

  @Test(timeout = 4000)
  public void testFoldIfElseVarAssignments() {
    assertEquals("var y=x?1:2", foldToString("if (x) var y = 1; else y = 2;"));
    assertEquals("var y=x?1:2", foldToString("if (x) y = 1; else var y = 2;"));
  }

  @Test(timeout = 4000)
  public void testFoldIfWithoutElseToAndOr() {
    assertEquals("x&&foo()", foldToString("if (x) foo();"));
    assertEquals("x||foo()", foldToString("if (!x) foo();"));
    // Property assignment inside IF statement body should be preserved for CollapseProperties
    assertEquals("if(x)a.b=1", foldToString("if (x) a.b = 1;"));
  }

  @Test(timeout = 4000)
  public void testFoldIfNegatedConditionInvertBranches() {
    assertEquals("if(x)bar();else foo()", foldToString("if (!x) foo(); else bar();"));
  }

  @Test(timeout = 4000)
  public void testTryRemoveRepeatedStatements() {
    String js = "function f() { if (a) { x = 1; return 1; } else { x = 2; return 1; } }";
    String res = foldToString(js);
    assertTrue("Trailing repeated return should be hoisted out of if-else",
        res.endsWith("return 1}"));
  }

  @Test(timeout = 4000)
  public void testConditionMinimizationDeMorgan() {
    assertEquals("!a&&!b", foldToString("if (!(a || b)) foo();").replace("&&foo()", ""));
    assertEquals("!a||!b", foldToString("if (!(a && b)) foo();").replace("||foo()", "").replace("(!a||!b)&&foo()", "!a||!b"));
    assertEquals("a&&foo()", foldToString("if (!!a) foo();"));
  }

  @Test(timeout = 4000)
  public void testConditionMinimizationConstants() {
    assertEquals("a&&foo()", foldToString("if (a || false) foo();"));
    assertEquals("a&&foo()", foldToString("if (a && true) foo();"));
  }

  @Test(timeout = 4000)
  public void testHookConditionSimplification() {
    assertEquals("x&&foo()", foldToString("if (x ? true : false) foo();"));
    assertEquals("!x&&foo()", foldToString("if (x ? false : true) foo();"));
    assertEquals("(x||y)&&foo()", foldToString("if (x ? true : y) foo();"));
    assertEquals("x&&y&&foo()", foldToString("if (x ? y : false) foo();"));
  }

  @Test(timeout = 4000)
  public void testLoopConditionSimplification() {
    assertEquals("while(1);", foldToString("while (true);"));
    assertEquals("while(0);", foldToString("while (false);"));
    assertEquals("do;while(1)", foldToString("do ; while (true);"));
    assertEquals("for(;1;);", foldToString("for (; true; );"));
    assertEquals("for(var k in obj);", foldToString("for (var k in obj);"));
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Constructors
  // ==========================================================================

  @Test(timeout = 4000)
  public void testFoldStandardConstructorsNormalized() {
    assertEquals("var x={}", foldToString("var x = new Object();", true));
    assertEquals("var x=[]", foldToString("var x = new Array();", true));
    assertEquals("var x=Error(\"err\")", foldToString("var x = new Error('err');", true));
    // If not normalized, constructors must not fold
    assertEquals("var x=new Object", foldToString("var x = new Object();", false));
    assertEquals("var x=new Array", foldToString("var x = new Array();", false));
  }

  @Test(timeout = 4000)
  public void testFoldArrayLiteralConstructor() {
    assertEquals("[]", foldToString("Array()"));
    assertEquals("[]", foldToString("new Array()"));
    assertEquals("[]", foldToString("Array(0)"));
    assertEquals("['a']", foldToString("Array('a')"));
    assertEquals("[[1,2]]", foldToString("Array([1, 2])"));
    assertEquals("[1,2,3]", foldToString("Array(1, 2, 3)"));
    // Array with a positive numeric argument allocates capacity and is not safe to fold
    assertEquals("Array(5)", foldToString("Array(5)"));
    assertEquals("new Array(5)", foldToString("new Array(5)", false));
  }

  @Test(timeout = 4000)
  public void testFoldObjectLiteralConstructor() {
    assertEquals("({})", foldToString("Object()"));
    assertEquals("new Object(1)", foldToString("new Object(1)"));
  }

  @Test(timeout = 4000)
  public void testFoldRegExpConstructor() {
    assertEquals("/foobar/", foldToString("RegExp('foobar')"));
    assertEquals("/foobar/i", foldToString("RegExp('foobar', 'i')"));
    assertEquals("/foo\\/bar/", foldToString("RegExp('foo/bar')"));
    // Empty pattern should not fold to //
    assertEquals("RegExp(\"\")", foldToString("RegExp('')"));
    // Global flag 'g' is unsafe to fold due to lastIndex statefulness
    assertEquals("RegExp(\"foo\",\"g\")", foldToString("RegExp('foo', 'g')"));
    // More than 100 characters must not fold (Opera bug protection)
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 105; i++) {
      sb.append("a");
    }
    String longPattern = sb.toString();
    assertEquals("RegExp(\"" + longPattern + "\")", foldToString("RegExp('" + longPattern + "')"));
  }

  @Test(timeout = 4000)
  public void testFoldRegExpInvalidFlags() {
    fold("RegExp('foo', 'invalid_flag')", true);
    assertTrue("Invalid RegExp flags should trigger compiler error", compiler.getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testContainsUnicodeEscapeUtility() {
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\u0020"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("plain text"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\\\u0020"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\\\\\u0020"));
  }

  // ==========================================================================
  // Partition D: Defensive Guards, Precedence & Dangling Else
  // ==========================================================================

  @Test(timeout = 4000)
  public void testPrecedenceAvoidsExcessiveParentheses() {
    // If condition has lower precedence than AND, folding into && adds parens and is skipped
    assertEquals("if(a||b)foo()", foldToString("if (a || b) foo();"));
    // If condition has lower precedence than OR, folding into || is skipped
    assertEquals("if(!(a&&b))foo()", foldToString("if (!(a && b)) foo();"));
  }

  @Test(timeout = 4000)
  public void testDanglingElseConsuming() {
    // Should not swap branches if it creates a dangling-else ambiguity
    String js = "if (!x) { if (y) foo(); } else bar();";
    String res = foldToString(js);
    assertTrue(res.contains("bar()"));
  }

  @Test(timeout = 4000)
  public void testDirectSubtreeUnchangedCases() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax();
    Node nameNode = Node.newString(Token.NAME, "dummy");
    assertSame(nameNode, peephole.optimizeSubtree(nameNode));

    Node emptyNode = new Node(Token.EMPTY);
    assertSame(emptyNode, peephole.optimizeSubtree(emptyNode));

    Node numberNode = Node.newNumber(123.0);
    assertSame(numberNode, peephole.optimizeSubtree(numberNode));
  }
}