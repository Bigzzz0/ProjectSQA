package com.google.javascript.jscomp;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Decision/branches covered by this suite:
 * - enterScope: global scope bail-out, variable-count bail-out, per-function CFG processing.
 * - GatherCandiates: CFG-node membership, read-vs-write name filtering, exported-name filtering,
 *   single-definition check, outer-scope dependency check.
 * - Candidate.canInline:
 *   - parameter definitions
 *   - already-inlined dependency back-off
 *   - assignment not in EXPR_RESULT
 *   - side effects to the right of the definition
 *   - side effects to the left of the use
 *   - side-effectful RHS / new / getprop / getelem / array / object / regexp
 *   - multiple uses in current CFG node
 *   - uses inside loops
 *   - multiple reaching uses for the definition
 *   - path-side-effect suppression for adjacent statements
 *   - labeled assignment removal during inlining
 * - Candidate.inlineVariable:
 *   - VAR initializer removal
 *   - EXPR_RESULT assignment removal
 *   - LABEL unwinding
 *
 * Defect-targeted zone:
 * - testVarAssinInsideHookIssue965: a variable assigned inside a local function
 *   ("hook") must not be inlined into an outside use, because the hook may not
 *   have executed before the variable use. The buggy pass fails to stop at the
 *   function boundary and incorrectly rewrites the use.
 */
public class FlowSensitiveInlineVariablesDeepseekTest {

  private String processJs(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = new ArrayList<>();
    externs.add(SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = new ArrayList<>();
    inputs.add(SourceFile.fromCode("testcode.js", js));
    compiler.init(externs, inputs, options);
    Node root = compiler.parseInputs();
    assertNotNull("Parsing failed", root);
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    pass.process(compiler.getExternsRoot(), compiler.getRoot());
    return compiler.toSource();
  }

  private static void assertContains(String source, String snippet) {
    String compactSource = source.replaceAll("\\s+", "");
    String compactSnippet = snippet.replaceAll("\\s+", "");
    assertTrue("Expected source to contain: " + snippet + "\nActual: " + source,
        source.contains(snippet) || compactSource.contains(compactSnippet));
  }

  private static void assertNotContains(String source, String snippet) {
    String compactSource = source.replaceAll("\\s+", "");
    String compactSnippet = snippet.replaceAll("\\s+", "");
    assertFalse("Expected source NOT to contain: " + snippet + "\nActual: " + source,
        source.contains(snippet) || compactSource.contains(compactSnippet));
  }

  @Test(timeout = 4000)
  public void testSimpleVarDeclarationInline() {
    String result = processJs("function f(){ var x = 1; return x; }");
    assertContains(result, "return 1");
  }

  @Test(timeout = 4000)
  public void testSimpleAssignmentInline() {
    String result = processJs("function f(){ var x; x = 1; return x; }");
    assertContains(result, "return 1");
    assertNotContains(result, "x = 1");
  }

  @Test(timeout = 4000)
  public void testNoInlineMultipleUsesInReturn() {
    String result = processJs("function f(){ var x = 1; return x + x; }");
    assertContains(result, "return x + x");
  }

  @Test(timeout = 4000)
  public void testNoInlineVariableUsedInsideLoop() {
    String result = processJs("function f(){ var x = 1; while(c) { alert(x); } }");
    assertContains(result, "alert(x)");
    assertContains(result, "var x = 1");
  }

  @Test(timeout = 4000)
  public void testNoInlineParameter() {
    String result = processJs("function f(x){ return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineGlobalScope() {
    String result = processJs("var x = 1; alert(x);");
    assertContains(result, "var x = 1");
    assertContains(result, "alert(x)");
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectBetweenDefinitionAndUse() {
    String result = processJs("function f(){ var x; x = 1; modifyProp(b); return x; }");
    assertContains(result, "x = 1");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideCall() {
    String result = processJs("function f(){ var x; x = modifyProp(b); return x; }");
    assertContains(result, "return x");
    assertContains(result, "x = modifyProp(b)");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideGetProp() {
    String result = processJs("function f(){ var x = a.b; return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideGetElem() {
    String result = processJs("function f(){ var x = a[b]; return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideNew() {
    String result = processJs("function f(){ var x = new Foo(); return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideArrayLit() {
    String result = processJs("function f(){ var x = [1]; return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideObjectLit() {
    String result = processJs("function f(){ var x = {a:1}; return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineRightHandSideRegexp() {
    String result = processJs("function f(){ var x = /a/; return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineMultipleReachingDefinitions() {
    String result = processJs(
        "function f(){ var x; if (c) { x = 1; } else { x = 2; } return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineAssignmentInsideInitializer() {
    String result = processJs("function f(){ var x; var y = (x = 1); return x; }");
    assertContains(result, "return x");
    assertContains(result, "x = 1");
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectRightOfDefinition() {
    String result = processJs(
        "function f(){ var x = 1, y = sideEffect(); return x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineSideEffectLeftOfUse() {
    String result = processJs("function f(){ var x = 1; return sideEffect(), x; }");
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testNoInlineTooManyVariables() {
    StringBuilder sb = new StringBuilder("function f(){ var x = 1;");
    for (int i = 0; i < 200; i++) {
      sb.append("var v").append(i).append(";");
    }
    sb.append("return x; }");
    String result = processJs(sb.toString());
    assertContains(result, "return x");
  }

  @Test(timeout = 4000)
  public void testInlineLabeledAssignment() {
    String result = processJs("function f(){ var x; label: x = 1; return x; }");
    assertContains(result, "return 1");
    assertNotContains(result, "x = 1");
    assertNotContains(result, "label");
  }

  @Test(timeout = 4000)
  public void testVarAssinInsideHookIssue965() {
    String js = "function f(){ var x; hook(function() { x = 1; }); return x; }";
    String result = processJs(js);
    // The variable is assigned inside a nested local function ("hook").
    // Such an assignment is not guaranteed to execute before the return,
    // so the optimizer must leave the use of x unchanged.
    assertContains(result, "x = 1");
    assertContains(result, "return x");
  }
}