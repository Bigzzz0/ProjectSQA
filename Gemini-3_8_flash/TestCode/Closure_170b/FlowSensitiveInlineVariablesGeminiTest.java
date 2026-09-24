/*
 * Copyright 2009 The Closure Compiler Authors.
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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: FlowSensitiveInlineVariables
 * Target Pass: Flow-sensitive inlining of local variables using reaching defs and reaching uses.
 *
 * Key Decision Branches & Safeguards:
 * 1. Scope Guard:
 *    - Global scope bypass: `t.inGlobalScope() -> return`
 *    - Scope variable limit: `LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE < t.getScope().getVarCount() -> return`
 * 2. Candidate Gathering:
 *    - CFG node check: `cfg.getDirectedGraphNode(n) == null -> return`
 *    - Name read-only check: assignment LHS, var declaration, inc/dec, paramList, catch name -> skip
 *    - Exported name check: `compiler.getCodingConvention().isExported(name) -> skip`
 *    - Outer scope dependency check: `reachingDef.dependsOnOuterScopeVars(def) -> skip`
 * 3. Candidate Inlining Preconditions (`Candidate#canInline`):
 *    - Function param check: `getDefCfgNode().isFunction() -> false`
 *    - Inlined dependency invalidation: `inlinedNewDependencies.contains(dep) -> false`
 *    - Non-expr assignment R-Value: `def.isAssign() && !NodeUtil.isExprAssign(def.getParent()) -> false`
 *    - Side effect in RHS/LHS expressions: `checkRightOf`, `checkLeftOf`
 *    - May-have-side-effects check on RHS: `NodeUtil.mayHaveSideEffects(def.getLastChild())`
 *    - Use count in CFG node != 1 -> false
 *    - Loop guard: `NodeUtil.isWithinLoop(use) -> false`
 *    - Reaching uses count != 1 -> false
 *    - Prohibited RHS nodes: GETELEM, GETPROP, ARRAYLIT, OBJECTLIT, REGEXP, NEW, CATCH-var
 *    - Intervening side-effects on CFG paths: `CheckPathsBetweenNodes` with `SIDE_EFFECT_PREDICATE`
 * 4. Ground Truth Defect:
 *    - Defects4J / Issue 965: `testVarAssinInsideHookIssue965` (or assignment inside hook/ternary conditional).
 *      When an assignment is inside a conditional/hook expression (e.g. `(x) ? a = 1 : a = 2`),
 *      the assignment is not a top-level expression statement (`isExprAssign` check fails or path check
 *      misidentifies reaching definition/side effects).
 */
public class FlowSensitiveInlineVariablesGeminiTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 3;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 965 & Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Issue 965: Variable assignment nested inside a hook (conditional ternary operator)
   * must not cause incorrect inlining or assertion errors.
   */
  @Test(timeout = 4000)
  public void testVarAssinInsideHookIssue965() {
    test(
        "function f(x) { " +
        "  var a; " +
        "  (x) ? a = 1 : a = 2; " +
        "  return a; " +
        "}",
        "function f(x) { " +
        "  var a; " +
        "  (x) ? a = 1 : a = 2; " +
        "  return a; " +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testVarAssignInsideHookCompound() {
    testSame(
        "function f(x, y) { " +
        "  var a; " +
        "  x ? (y ? a = 1 : a = 2) : a = 3; " +
        "  return a; " +
        "}"
    );
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions (Basic Inlining)
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleVariableInlining() {
    test(
        "function f() { var x = 1; return x; }",
        "function f() { var x; return 1; }"
    );
  }

  @Test(timeout = 4000)
  public void testSimpleAssignmentInlining() {
    test(
        "function f() { var x; x = 1; return x; }",
        "function f() { var x; return 1; }"
    );
  }

  @Test(timeout = 4000)
  public void testInliningWithMultipleStatements() {
    test(
        "function f() { var x = 2; var y = 3; return x + y; }",
        "function f() { var x; var y; return 2 + 3; }"
    );
  }

  @Test(timeout = 4000)
  public void testInliningChainedAssignments() {
    test(
        "function f() { var x = 1; var y = x; return y; }",
        "function f() { var x; var y; return 1; }"
    );
  }

  @Test(timeout = 4000)
  public void testLabeledAssignmentInlining() {
    test(
        "function f() { var x; label: x = 1; return x; }",
        "function f() { var x; return 1; }"
    );
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Control Flow Constraints
  // =========================================================================

  @Test(timeout = 4000)
  public void testNoInlineInGlobalScope() {
    // Global scope variables should not be inlined by this pass
    testSame("var x = 1; var y = x;");
  }

  @Test(timeout = 4000)
  public void testNoInlineMultipleUses() {
    // Variable used more than once should not be inlined
    testSame("function f() { var x = 1; return x + x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineWithinLoop() {
    // Variable used inside a loop should not be inlined
    testSame("function f() { var x = 1; while (true) { alert(x); } }");
    testSame("function f() { var x = 1; for (var i = 0; i < 10; i++) { alert(x); } }");
    testSame("function f() { var x = 1; do { alert(x); } while (true); }");
  }

  @Test(timeout = 4000)
  public void testNoInlineFunctionParam() {
    // Function parameters cannot be inlined
    testSame("function f(x) { return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineCatchVar() {
    // Catch variable references should not be inlined
    testSame("function f() { try {} catch (e) { var x = e; return x; } }");
  }

  @Test(timeout = 4000)
  public void testNoInlineObjectLiteralsAndArrays() {
    // Object and array literals create new references, not safe to inline
    testSame("function f() { var x = {}; return x; }");
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineNewAndRegExp() {
    testSame("function f() { var x = new Object(); return x; }");
    testSame("function f() { var x = /abc/; return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlinePropertyAccess() {
    // Property access can be modified by alias
    testSame("function f(a) { var x = a.b; return x; }");
    testSame("function f(a, i) { var x = a[i]; return x; }");
  }

  // =========================================================================
  // Partition D: Side-Effect Guards & Path Checks
  // =========================================================================

  @Test(timeout = 4000)
  public void testNoInlineSideEffectOnRhs() {
    // RHS that may have side effects must not be inlined
    testSame("function f() { var x = externalCall(); return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineInterveningSideEffects() {
    // Intervening function call that might mutate environment
    testSame("function f() { var x = 1; g(); return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectOnRightOfDefinition() {
    // Side effect to the right in the same expression
    testSame("function f(a) { var x; (x = 1), g(); return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectOnLeftOfUse() {
    // Side effect to the left in the use expression
    testSame("function f() { var x = 1; return g(), x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineAssignmentUsedAsRValue() {
    // Assignment used as an R-value
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  @Test(timeout = 4000)
  public void testNoInlineModifiedVariable() {
    // Increment / decrement / reassignment before use
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; x--; return x; }");
    testSame("function f() { var x = 1; x += 1; return x; }");
  }

  @Test(timeout = 4000)
  public void testConditionalBranchingDifferentDefinitions() {
    // Multiple definitions reaching the use
    testSame("function f(cond) { var x; if (cond) { x = 1; } else { x = 2; } return x; }");
  }

  // =========================================================================
  // Partition E: Dependency Invalidation & Complex Interactions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDependencyBackOff() {
    // When inlining introduces new dependency, subsequent candidate must back off safely
    test(
        "function f() { var a = 1; var b = a; var c = b; return c; }",
        "function f() { var a; var b; var c; return 1; }"
    );
  }

  @Test(timeout = 4000)
  public void testEmptyBlockAndInnerFunction() {
    testSame("function f() {}");
    test(
        "function f() { var x = 10; function inner() { return 20; } return x; }",
        "function f() { var x; function inner() { return 20; } return 10; }"
    );
  }
}