/*
 * Copyright 2011 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Method / Branch                              | Input Condition / Partition               | Expected Behavior
 * -------------------------------------------------------------------------------------------------------
 * tryFoldSimpleFunctionCall (Defect Target: Issue 759)| String(variable) [Non-literal argument]   | MUST NOT fold to "" + y; stay String(y)
 * tryFoldSimpleFunctionCall                           | String('literal'), String(123)            | Fold to "" + 'literal', "" + 123
 * tryFoldSimpleFunctionCall                           | String() [no argument]                    | Retain Call node without change
 * tryFoldSimpleFunctionCall                           | String(arg1, arg2)                        | Retain Call node without change
 * reduceTrueFalse                                     | late = true vs late = false               | !0 / !1 when late; true / false otherwise
 * tryMinimizeNot                                      | !(a == b), !(a != b), !(a === b)          | Fold to !=, ==, !==, ===; ignore <, <=, >, >=
 * tryMinimizeIf: Hook Return                          | if (x) return 1; else return 2;           | return x ? 1 : 2;
 * tryMinimizeIf: Hook Assign                          | if (x) a = 1; else a = 2;                 | a = x ? 1 : 2;
 * tryMinimizeIf: Hook Expr                            | if (x) foo(); else bar();                 | x ? foo() : bar();
 * tryMinimizeIf: Single Branch &                      | if (x) foo();                             | x && foo();
 * tryMinimizeIf: Single Branch ||                     | if (!x) foo();                            | x || foo();
 * tryMinimizeIf: Nested IF Join                       | if (x) { if (y) { foo(); } }              | if (x && y) foo();
 * tryReplaceIf: Block Consecutive Return              | if (x) return 1; if (y) return 1;        | if (x || y) return 1;
 * tryReplaceIf: Block Consecutive Return Alt          | if (x) return 1; if (y) f(); else ret 1;  | if (!x && y) f(); else return 1;
 * tryReplaceIf: If Return followed by Return          | if (x) return; return 1;                  | return x ? void 0 : 1;
 * tryReplaceIf: Must Exit Parent                      | if (x) return 1; else foo();              | if (x) return 1; foo();
 * tryMinimizeCondition: Hook & DeMorgan               | x ? true : false, x ? false : true, etc.  | x, !x, x || y, x && y; !(a||b)->!a&&!b
 * tryJoinForCondition                                 | for(;;) { if(x) break; f(); } (late)      | for(;!x;) f();
 * trySplitComma                                       | (a, b); (late = false vs true)            | Split into a; b; when not late; retain if late
 * tryFoldLiteralConstructor: Object, Array, RegExp    | Object(), Array(), RegExp('a', 'i')       | {}, [], /a/i
 * RegExp Flag Validation & Escaping                   | Invalid flags, '/', '\n', '[' safe        | Report error, escape slashes and newlines
 * tryReduceReturn & tryRemoveRedundantExit            | return undefined;, return void 0;         | Reduced to return; or removed at block tail
 * DONT_TRAVERSE_FUNCTIONS_PREDICATE                   | Function node vs Non-function node        | false for Function; true otherwise
 * -------------------------------------------------------------------------------------------------------
 */
public class PeepholeSubstituteAlternateSyntaxGeminiTest {

  private Compiler lastCompiler;

  private Node parseAndFold(String js, boolean late, boolean normalized) {
    lastCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    lastCompiler.initOptions(options);
    Node root = lastCompiler.parseTestCode(js);
    if (normalized) {
      lastCompiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    }
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(late);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(lastCompiler, peephole);
    pass.process(null, root);
    return root;
  }

  private Node findNode(Node root, int tokenType) {
    if (root == null) {
      return null;
    }
    if (root.getType() == tokenType) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node match = findNode(child, tokenType);
      if (match != null) {
        return match;
      }
    }
    return null;
  }

  private int countNodes(Node root, int tokenType) {
    if (root == null) {
      return 0;
    }
    int count = (root.getType() == tokenType) ? 1 : 0;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      count += countNodes(child, tokenType);
    }
    return count;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 759 & testSimpleFunctionCall)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectSimpleFunctionCallWithVariableShouldNotFold() {
    // Ground truth defect: String(y) cannot be folded to ("" + y) because
    // String(y) invokes toString() first, while ("" + y) invokes valueOf() first.
    Node root = parseAndFold("var x = String(y);", false, false);
    Node callNode = findNode(root, Token.CALL);
    assertNotNull("String(y) with variable parameter must remain a CALL node", callNode);
    Node addNode = findNode(root, Token.ADD);
    assertNull("String(y) must NOT be transformed into string addition ('')", addNode);
  }

  @Test(timeout = 4000)
  public void testSimpleFunctionCallWithImmutableStringLiteral() {
    Node root = parseAndFold("var x = String('closure');", false, false);
    Node addNode = findNode(root, Token.ADD);
    assertNotNull("String('closure') on string literal should fold to '' + 'closure'", addNode);
  }

  @Test(timeout = 4000)
  public void testSimpleFunctionCallWithImmutableNumberLiteral() {
    Node root = parseAndFold("var x = String(42);", false, false);
    Node addNode = findNode(root, Token.ADD);
    assertNotNull("String(42) on numeric literal should fold to '' + 42", addNode);
  }

  @Test(timeout = 4000)
  public void testSimpleFunctionCallNoArgumentsShouldNotFold() {
    Node root = parseAndFold("var x = String();", false, false);
    Node callNode = findNode(root, Token.CALL);
    assertNotNull("String() with no arguments must remain a CALL node", callNode);
    assertNull(findNode(root, Token.ADD));
  }

  @Test(timeout = 4000)
  public void testSimpleFunctionCallNonStringTargetShouldNotFold() {
    Node root = parseAndFold("var x = Number(123);", false, false);
    Node callNode = findNode(root, Token.CALL);
    assertNotNull(callNode);
    assertNull(findNode(root, Token.ADD));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testReduceTrueFalseWhenLate() {
    Node root = parseAndFold("var a = true; var b = false;", true, false);
    assertEquals("Both booleans should be folded to NOT nodes (!0, !1)", 2, countNodes(root, Token.NOT));
    assertEquals(0, countNodes(root, Token.TRUE));
    assertEquals(0, countNodes(root, Token.FALSE));
  }

  @Test(timeout = 4000)
  public void testReduceTrueFalseWhenNotLate() {
    Node root = parseAndFold("var a = true; var b = false;", false, false);
    assertEquals(1, countNodes(root, Token.TRUE));
    assertEquals(1, countNodes(root, Token.FALSE));
    assertEquals(0, countNodes(root, Token.NOT));
  }

  @Test(timeout = 4000)
  public void testMinimizeNotOperators() {
    Node root = parseAndFold("var r1 = !(a == b); var r2 = !(a != b); var r3 = !(a === b); var r4 = !(a !== b);", false, false);
    assertNotNull("!(a == b) should fold to !=", findNode(root, Token.NE));
    assertNotNull("!(a != b) should fold to ==", findNode(root, Token.EQ));
    assertNotNull("!(a === b) should fold to !==", findNode(root, Token.SHNE));
    assertNotNull("!(a !== b) should fold to ===", findNode(root, Token.SHEQ));
    assertEquals("All 4 NOT expressions should be inverted without outer NOT", 0, countNodes(root, Token.NOT));
  }

  @Test(timeout = 4000)
  public void testMinimizeNotPreservesRelationalOperators() {
    Node root = parseAndFold("var r = !(a < b);", false, false);
    assertNotNull("Relational operators must not invert due to NaN edge cases", findNode(root, Token.NOT));
    assertNotNull(findNode(root, Token.LT));
  }

  @Test(timeout = 4000)
  public void testFoldIfToHookReturn() {
    Node root = parseAndFold("function f(x) { if (x) return 1; else return 2; }", false, false);
    assertNotNull("if (x) return 1; else return 2; should fold to return HOOK", findNode(root, Token.HOOK));
    assertEquals("Should only have 1 RETURN node", 1, countNodes(root, Token.RETURN));
    assertNull("IF node should be eliminated", findNode(root, Token.IF));
  }

  @Test(timeout = 4000)
  public void testFoldIfToHookAssignment() {
    Node root = parseAndFold("function f(x) { if (x) a = 1; else a = 2; }", false, false);
    assertNotNull("Assignment should wrap HOOK", findNode(root, Token.HOOK));
    assertEquals(1, countNodes(root, Token.ASSIGN));
    assertNull(findNode(root, Token.IF));
  }

  @Test(timeout = 4000)
  public void testFoldIfToHookExpressions() {
    Node root = parseAndFold("function f(x) { if (x) foo(); else bar(); }", false, false);
    assertNotNull("Expression branch should fold to HOOK", findNode(root, Token.HOOK));
    assertNull(findNode(root, Token.IF));
  }

  @Test(timeout = 4000)
  public void testFoldIfVarAssignVariants() {
    Node root1 = parseAndFold("function f(x) { if (x) var a = 1; else a = 2; }", false, false);
    assertNotNull("var a = x ? 1 : 2 should be produced", findNode(root1, Token.HOOK));
    assertEquals(1, countNodes(root1, Token.VAR));

    Node root2 = parseAndFold("function f(x) { if (x) a = 1; else var a = 2; }", false, false);
    assertNotNull("var a = x ? 1 : 2 should be produced", findNode(root2, Token.HOOK));
    assertEquals(1, countNodes(root2, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testSingleBranchIfToAndOr() {
    Node rootAnd = parseAndFold("if (x) foo();", false, false);
    assertNotNull("if (x) foo(); should fold to x && foo();", findNode(rootAnd, Token.AND));
    assertNull(findNode(rootAnd, Token.IF));

    Node rootOr = parseAndFold("if (!x) foo();", false, false);
    assertNotNull("if (!x) foo(); should fold to x || foo();", findNode(rootOr, Token.OR));
    assertNull(findNode(rootOr, Token.IF));
  }

  @Test(timeout = 4000)
  public void testNestedIfCombination() {
    Node root = parseAndFold("if (x) { if (y) { foo(); } }", false, false);
    Node andNode = findNode(root, Token.AND);
    assertNotNull("Nested IFs should be joined by AND", andNode);
    assertEquals("Should only be 1 IF node remaining", 1, countNodes(root, Token.IF));
  }

  @Test(timeout = 4000)
  public void testRemoveRepeatedStatementsInIfBranches() {
    Node root = parseAndFold("function f(x) { if (x) { a = 1; return true; } else { a = 2; return true; } }", false, false);
    assertEquals("Common exit statement should be hoisted out", 1, countNodes(root, Token.RETURN));
  }

  @Test(timeout = 4000)
  public void testConditionMinimizationDeMorgan() {
    Node root1 = parseAndFold("if (!(a || b)) foo();", false, false);
    assertNotNull("!(a || b) should become !a && !b", findNode(root1, Token.AND));

    Node root2 = parseAndFold("if (!(a && b)) foo();", false, false);
    assertNotNull("!(a && b) should become !a || !b", findNode(root2, Token.OR));

    Node root3 = parseAndFold("if (!!a) foo();", false, false);
    assertEquals("Double NOT should be eliminated", 0, countNodes(root3, Token.NOT));
  }

  @Test(timeout = 4000)
  public void testHookConditionMinimization() {
    Node root1 = parseAndFold("var z = x ? true : false;", false, false);
    assertEquals("x ? true : false should reduce directly to x", 0, countNodes(root1, Token.HOOK));

    Node root2 = parseAndFold("var z = x ? false : true;", false, false);
    assertNotNull("x ? false : true should reduce to !x", findNode(root2, Token.NOT));

    Node root3 = parseAndFold("var z = x ? true : y;", false, false);
    assertNotNull("x ? true : y should reduce to x || y", findNode(root3, Token.OR));

    Node root4 = parseAndFold("var z = x ? y : false;", false, false);
    assertNotNull("x ? y : false should reduce to x && y", findNode(root4, Token.AND));
  }

  @Test(timeout = 4000)
  public void testImmediateCallToBoundFunction() {
    Node root = parseAndFold("(fn.bind(obj, 1, 2))();", false, false);
    Node getProp = findNode(root, Token.GETPROP);
    assertNotNull("Bound function call should be rewritten to fn.call(obj, ...)", getProp);
    assertEquals("call", getProp.getLastChild().getString());

    Node rootNull = parseAndFold("(fn.bind(null, 1))();", false, false);
    Node freeCall = findNode(rootNull, Token.CALL);
    assertNotNull("Bound function call with null this should rewrite to regular call", freeCall);
  }

  @Test(timeout = 4000)
  public void testSplitCommaWhenNotLate() {
    Node root = parseAndFold("(a, b);", false, false);
    assertEquals("Comma expression in statement should be split into 2 statements", 2, countNodes(root, Token.EXPR_RESULT));
    assertEquals(0, countNodes(root, Token.COMMA));
  }

  @Test(timeout = 4000)
  public void testNoSplitCommaWhenLate() {
    Node root = parseAndFold("(a, b);", true, false);
    assertEquals("Comma should NOT be split when late is true", 1, countNodes(root, Token.COMMA));
  }

  @Test(timeout = 4000)
  public void testReplaceUndefinedWhenNormalized() {
    Node rootNormalized = parseAndFold("var x = undefined;", false, true);
    assertNotNull("Normalized undefined should become void 0", findNode(rootNormalized, Token.VOID));

    Node rootRaw = parseAndFold("var x = undefined;", false, false);
    assertNull("Unnormalized undefined should NOT become void 0", findNode(rootRaw, Token.VOID));
  }

  @Test(timeout = 4000)
  public void testReduceReturnUndefinedAndVoid() {
    Node root1 = parseAndFold("function f() { return undefined; }", false, false);
    Node ret1 = findNode(root1, Token.RETURN);
    assertNotNull(ret1);
    assertFalse("return undefined should reduce to bare return", ret1.hasChildren());

    Node root2 = parseAndFold("function f() { return void 0; }", false, false);
    Node ret2 = findNode(root2, Token.RETURN);
    assertNotNull(ret2);
    assertFalse("return void 0 should reduce to bare return", ret2.hasChildren());

    Node root3 = parseAndFold("function f() { return void foo(); }", false, false);
    Node ret3 = findNode(root3, Token.RETURN);
    assertNotNull(ret3);
    assertTrue("return void foo() has side-effects and must NOT be stripped", ret3.hasChildren());
  }

  @Test(timeout = 4000)
  public void testRedundantReturnAtEndOfFunctionRemoved() {
    Node root = parseAndFold("function f() { var x = 1; return; }", false, false);
    assertEquals("Redundant trailing return should be removed", 0, countNodes(root, Token.RETURN));
  }

  @Test(timeout = 4000)
  public void testReplaceExitWithBreak() {
    Node root = parseAndFold("while (a) { return; }", false, false);
    assertNotNull("Trailing return in loop should be replaced with break", findNode(root, Token.BREAK));
    assertEquals(0, countNodes(root, Token.RETURN));
  }

  @Test(timeout = 4000)
  public void testJoinForConditionLate() {
    Node root = parseAndFold("for (;;) { if (x) break; foo(); }", true, false);
    Node forNode = findNode(root, Token.FOR);
    assertNotNull(forNode);
    Node cond = NodeUtil.getConditionExpression(forNode);
    assertNotNull("IF-break in for body should be joined into loop condition", cond);
    assertEquals(Token.NOT, cond.getType());

    Node rootAnd = parseAndFold("for (;a;) { if (x) break; foo(); }", true, false);
    Node forNodeAnd = findNode(rootAnd, Token.FOR);
    Node condAnd = NodeUtil.getConditionExpression(forNodeAnd);
    assertEquals(Token.AND, condAnd.getType());
  }

  @Test(timeout = 4000)
  public void testTryReplaceIfBlockVariants() {
    Node root1 = parseAndFold("function f() { if (x) return 1; if (y) return 1; }", false, false);
    assertNotNull("Consecutive if-returns with identical bodies fold to OR", findNode(root1, Token.OR));

    Node root2 = parseAndFold("function f() { if (x) return 1; if (y) foo(); else return 1; }", false, false);
    assertNotNull("Consecutive if-returns with else-body fold to AND", findNode(root2, Token.AND));

    Node root3 = parseAndFold("function f() { if (x) return; return 1; }", false, false);
    assertNotNull("if(x) return; return 1; should fold to return x ? void 0 : 1", findNode(root3, Token.HOOK));

    Node root4 = parseAndFold("function f() { if (x) return 1; else { foo(); } }", false, false);
    assertNull("Else branch should be detached after an exiting then-branch", findNode(root4, Token.BLOCK));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testFoldStandardObjectConstructor() {
    Node root = parseAndFold("var x = new Object();", false, true);
    assertNotNull("new Object() should fold to object literal {}", findNode(root, Token.OBJECTLIT));
    assertNull(findNode(root, Token.NEW));

    Node rootWithArgs = parseAndFold("var x = new Object(1);", false, true);
    assertNull("new Object(1) should NOT fold to object literal", findNode(rootWithArgs, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testFoldStandardArrayConstructor() {
    Node rootEmpty = parseAndFold("var x = new Array();", false, true);
    assertNotNull("new Array() should fold to []", findNode(rootEmpty, Token.ARRAYLIT));

    Node rootZero = parseAndFold("var x = new Array(0);", false, true);
    assertNotNull("new Array(0) should fold to []", findNode(rootZero, Token.ARRAYLIT));

    Node rootSingleStr = parseAndFold("var x = new Array('a');", false, true);
    assertNotNull("new Array('a') should fold to ['a']", findNode(rootSingleStr, Token.ARRAYLIT));

    Node rootMultiple = parseAndFold("var x = new Array(1, 2);", false, true);
    assertNotNull("new Array(1, 2) should fold to [1, 2]", findNode(rootMultiple, Token.ARRAYLIT));

    Node rootSizeAlloc = parseAndFold("var x = new Array(5);", false, true);
    assertNull("new Array(5) allocates size and must NOT fold to [5]", findNode(rootSizeAlloc, Token.ARRAYLIT));
  }

  @Test(timeout = 4000)
  public void testFoldRegExpConstructor() {
    Node rootSimple = parseAndFold("var x = new RegExp('abc', 'i');", false, true);
    assertNotNull("new RegExp('abc', 'i') should fold to /abc/i", findNode(rootSimple, Token.REGEXP));

    Node rootEmpty = parseAndFold("var x = new RegExp('');", false, true);
    assertNull("Empty pattern RegExp should NOT fold to regex literal //", findNode(rootEmpty, Token.REGEXP));

    Node rootSlash = parseAndFold("var x = new RegExp('/');", false, true);
    Node regexNode = findNode(rootSlash, Token.REGEXP);
    assertNotNull("RegExp('/') should fold and escape slash", regexNode);
    assertEquals("\\/", regexNode.getFirstChild().getString());

    Node rootCharset = parseAndFold("var x = new RegExp('[/]');", false, true);
    Node regexCharsetNode = findNode(rootCharset, Token.REGEXP);
    assertNotNull(regexCharsetNode);
    assertEquals("[/]", regexCharsetNode.getFirstChild().getString());

    StringBuilder longPattern = new StringBuilder();
    for (int i = 0; i < 105; i++) {
      longPattern.append("a");
    }
    Node rootLong = parseAndFold("var x = new RegExp('" + longPattern.toString() + "');", false, true);
    assertNull("Patterns over 100 characters must not be folded for browser safety", findNode(rootLong, Token.REGEXP));
  }

  @Test(timeout = 4000)
  public void testMinimizeStringArrayLiteralWhenLate() {
    Node root = parseAndFold("var arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'];", true, false);
    Node callNode = findNode(root, Token.CALL);
    assertNotNull("Large string array literal should fold to .split() call", callNode);
    Node getProp = findNode(callNode, Token.GETPROP);
    assertNotNull(getProp);
    assertEquals("split", getProp.getLastChild().getString());

    Node rootNotLate = parseAndFold("var arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'];", false, false);
    assertNull("String array literal should NOT fold to .split() when not late", findNode(rootNotLate, Token.CALL));
  }

  @Test(timeout = 4000)
  public void testMinimizeArrayLiteralMixedTypesShouldNotFold() {
    Node root = parseAndFold("var arr = ['a', 1, 'c', 'd', 'e', 'f', 'g', 'h'];", true, false);
    assertNull("Non-homogenous string array must not fold to split()", findNode(root, Token.CALL));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidRegExpFlagsEmitsError() {
    parseAndFold("var x = new RegExp('abc', 'invalidflags');", false, true);
    assertTrue("Invalid regular expression flags must report a compiler error", lastCompiler.getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testOptimizeSubtreeFallbackUnchanged() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Node num = IR.number(12345);
    Node result = peephole.optimizeSubtree(num);
    assertSame("Unrecognized or non-optimizable token should return exact same node", num, result);
  }

  @Test(timeout = 4000)
  public void testIsPureDefensiveHandling() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    assertTrue("null node is safely pure", peephole.isPure(null));
    assertTrue("Numeric literal is pure", peephole.isPure(IR.number(1)));
    assertTrue("String literal is pure", peephole.isPure(IR.string("test")));
    assertFalse("Function call is not pure", peephole.isPure(IR.call(IR.name("fn"))));
  }

  @Test(timeout = 4000)
  public void testSkipFinallyNodesDefensive() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    assertNull("Null node returns null without exception", peephole.skipFinallyNodes(null));
    Node n = IR.number(1);
    assertSame("Non-try node returns itself", n, peephole.skipFinallyNodes(n));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDontTraverseFunctionsPredicate() {
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    assertFalse("Predicate should return false for functions",
        PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(fn));

    Node nonFn = IR.number(100);
    assertTrue("Predicate should return true for non-functions",
        PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(nonFn));
  }

  @Test(timeout = 4000)
  public void testContainsUnicodeEscape() {
    assertTrue("String with Unicode escape should be detected",
        PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\u1234"));
    assertFalse("Plain ASCII string contains no unicode escape",
        PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("abcXYZ"));
  }

  @Test(timeout = 4000)
  public void testAreMatchingExitsContract() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Node retA = IR.returnNode(IR.number(42));
    Node retB = IR.returnNode(IR.number(42));
    Node retDiff = IR.returnNode(IR.number(99));

    assertTrue("Equivalent return statements are matching exits", peephole.areMatchingExits(retA, retB));
    assertFalse("Different return statements are not matching exits", peephole.areMatchingExits(retA, retDiff));
  }
}