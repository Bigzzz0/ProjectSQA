/*
 * Copyright 2024 The Closure Compiler Authors.
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

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: PeepholeSubstituteAlternateSyntax
 *
 * Defect Profile (Defects4J Ground Truth):
 * - Associated Issue 1062 / Operator Associativity:
 *   Parenthesized associative expressions (such as `a || (b || c)`, `a * (b * c)`,
 *   `a | (b | c)`, and `3 * (4 % 3 * 5)`) must retain their necessary parentheses
 *   or structural associations during peephole passes and code generation.
 *
 * Targeted Decision Branches & Boundaries:
 * 1. Token.TRUE / Token.FALSE:
 *    - late = false: return unchanged
 *    - late = true: fold to !0 / !1 with node info cloning
 * 2. Token.NEW / Token.CALL (Standard & Literal Constructors):
 *    - Standard constructors (Object, Array, RegExp, Error) when AST is normalized -> converted to CALL
 *    - Non-standard constructor names or non-name targets -> retain NEW
 *    - AST not normalized -> no folding
 *    - Object(): 0 args -> {} ; >0 args -> no fold
 *    - Array(): 0 args -> [] ; >=2 args -> [arg0, arg1, ...]
 *    - Array(1 arg): STRING -> ['s']; NUMBER(0) -> []; NUMBER(n!=0) -> no fold; ARRAYLIT -> [[...]]; other -> no fold
 * 3. Token.CALL (RegExp Constructor):
 *    - 0 args or >2 args -> no fold
 *    - Empty pattern "" -> no fold (prevent comment `//`)
 *    - Pattern length >= 100 -> no fold (Opera 9.2 safeguard)
 *    - Non-string pattern or non-string flags -> no fold
 *    - Forward slash & newline bracket safety (makeForwardSlashBracketSafe):
 *      Literal '/', escaped '/', '/' inside charset '[/]', brackets '[' and ']',
 *      LineTerminators '\r', '\n', '\u2028', '\u2029' (both raw and preceded by escape)
 *    - Unicode escapes: ES5 vs ES3 (containsUnicodeEscape check)
 *    - Flags: Invalid flags (warning reported), Safe flags (ES3 with 'g' disallowed, ES5 allowed)
 * 4. Token.CALL (Simple Function Calls & Bound Functions):
 *    - String(literal) -> "" + literal (for immutable literals: string, number, boolean)
 *    - Bound function immediate call: fn.bind(thisVal, a, b)() -> fn.call(thisVal, a, b) / fn(a, b)
 * 5. Token.COMMA (trySplitComma):
 *    - late = true: no split
 *    - late = false: parent is ExprResult and grandparent not Label -> split comma
 *    - parent is not ExprResult or grandparent is Label -> no split
 * 6. Token.NAME (tryReplaceUndefined):
 *    - normalized AST, undefined name, not LValue -> void 0
 *    - AST not normalized or is LValue -> retain undefined
 * 7. Token.RETURN (tryReduceReturn):
 *    - return undefined / return void 0 -> return
 *    - return void with side effects -> retain return void expr
 *    - return val -> retain
 * 8. Token.ARRAYLIT (tryMinimizeArrayLiteral):
 *    - Non-string elements -> no fold
 *    - late = false -> no fold
 *    - Element savings <= 0 -> no fold
 *    - Strings all length 1 -> delimiter ""
 *    - Candidate delimiters (" ", ";", ",", "{", "}") -> pick first unused
 *    - All delimiters exhausted -> return null, no fold
 */
public class PeepholeSubstituteAlternateSyntaxGeminiTest {

  private Compiler createCompiler(boolean normalized, boolean es5) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(es5 ? CompilerOptions.LanguageMode.ECMASCRIPT5
        : CompilerOptions.LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    if (normalized) {
      compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    }
    return compiler;
  }

  private Node parseAndProcess(Compiler compiler, PeepholeSubstituteAlternateSyntax peephole, String js) {
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(null, root);
    return root;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testReduceTrueFalseWhenLate() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(true);
    peephole.beginTraversal(compiler);

    Node trueNode = IR.trueNode();
    Node parentTrue = IR.exprResult(trueNode);
    Node resultTrue = peephole.optimizeSubtree(trueNode);
    assertEquals(Token.NOT, resultTrue.getType());
    assertEquals(0.0, resultTrue.getFirstChild().getDouble(), 0.0);

    Node falseNode = IR.falseNode();
    Node parentFalse = IR.exprResult(falseNode);
    Node resultFalse = peephole.optimizeSubtree(falseNode);
    assertEquals(Token.NOT, resultFalse.getType());
    assertEquals(1.0, resultFalse.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldStandardConstructorsWhenNormalized() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var x = new Object();");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx={};", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array();");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=[];", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array('x', 'y');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=['x','y'];", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array('single');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=['single'];", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array(0);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=[];", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array([1, 2]);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=[[1,2]];", output);
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructor() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var r = new RegExp('abc', 'i');");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/abc/i;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('hello');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/hello/;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('/', '');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\//;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('[/]');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/[/]/;", output);
  }

  @Test(timeout = 4000)
  public void testFoldSimpleStringFunctionCall() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var s = String('test');");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vars=''+'test';", output);

    root = parseAndProcess(compiler, peephole, "var s = String(42);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vars=''+42;", output);

    root = parseAndProcess(compiler, peephole, "var s = String(true);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vars=''+true;", output);
  }

  @Test(timeout = 4000)
  public void testFoldBoundFunctionImmediateCall() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "(fn.bind(obj, 1, 2))();");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("fn.call(obj,1,2);", output);

    root = parseAndProcess(compiler, peephole, "(fn.bind(null, 1))();");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("fn(1);", output);
  }

  @Test(timeout = 4000)
  public void testSplitCommaWhenEarly() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "a, b;");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("a;b;", output);
  }

  @Test(timeout = 4000)
  public void testReplaceUndefinedWithVoidZero() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var x = undefined;");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx=void 0;", output);
  }

  @Test(timeout = 4000)
  public void testReduceReturn() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "function f() { return undefined; }");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("functionf(){return;}", output);

    root = parseAndProcess(compiler, peephole, "function f() { return void 0; }");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("functionf(){return;}", output);
  }

  @Test(timeout = 4000)
  public void testMinimizeStringArrayLiteral() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(true);

    Node root = parseAndProcess(compiler, peephole, "var a = ['a', 'b', 'c', 'd', 'e', 'f'];");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara='abcdef'.split('');", output);

    root = parseAndProcess(compiler, peephole, "var a = ['foo', 'bar', 'baz', 'qux', 'xyz', 'abc'];");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara='foo bar baz qux xyz abc'.split(' ');", output);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testReduceTrueFalseWhenNotLate() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    peephole.beginTraversal(compiler);

    Node trueNode = IR.trueNode();
    Node parent = IR.exprResult(trueNode);
    Node result = peephole.optimizeSubtree(trueNode);
    assertSame(trueNode, result);

    Node falseNode = IR.falseNode();
    Node parentFalse = IR.exprResult(falseNode);
    Node resultFalse = peephole.optimizeSubtree(falseNode);
    assertSame(falseNode, resultFalse);
  }

  @Test(timeout = 4000)
  public void testArrayConstructorUnsafeSingleArguments() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var a = new Array(5);");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=Array(5);", output);

    root = parseAndProcess(compiler, peephole, "var a = new Array(n);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=Array(n);", output);
  }

  @Test(timeout = 4000)
  public void testRegExpConstructorBoundaryConditions() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var r = new RegExp();");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=RegExp();", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('', 'g');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=RegExp('','g');", output);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 101; i++) {
      sb.append('a');
    }
    String longPattern = sb.toString();
    root = parseAndProcess(compiler, peephole, "var r = new RegExp('" + longPattern + "');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=RegExp('" + longPattern + "');", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('a', 'b', 'c');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=new RegExp('a','b','c');", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp(foo);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=new RegExp(foo);", output);
  }

  @Test(timeout = 4000)
  public void testRegExpSpecialCharactersAndLineTerminators() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var r = new RegExp('\\n');");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\n/;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('\\r');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\r/;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('\\u2028');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\u2028/;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('\\u2029');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\u2029/;", output);

    root = parseAndProcess(compiler, peephole, "var r = new RegExp('\\\\[/');");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=/\\[\\//;", output);
  }

  @Test(timeout = 4000)
  public void testRegExpInEs3DisallowsGlobalFlagFolding() {
    Compiler compilerEs3 = createCompiler(true, false);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compilerEs3, peephole, "var r = new RegExp('abc', 'g');");
    String output = compilerEs3.toSource(root).replaceAll("\\s+", "");
    assertEquals("varr=RegExp('abc','g');", output);
  }

  @Test(timeout = 4000)
  public void testRegExpContainsUnicodeEscape() {
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\u0041"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("plainText"));
    assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\\\u0041"));
  }

  @Test(timeout = 4000)
  public void testCommaSplitGuards() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peepholeLate = new PeepholeSubstituteAlternateSyntax(true);
    Node root = parseAndProcess(compiler, peepholeLate, "a, b;");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("a,b;", output);

    PeepholeSubstituteAlternateSyntax peepholeEarly = new PeepholeSubstituteAlternateSyntax(false);
    root = parseAndProcess(compiler, peepholeEarly, "lbl: (a, b);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("lbl:a,b;", output);

    root = parseAndProcess(compiler, peepholeEarly, "var x = (a, b);");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx=(a,b);", output);
  }

  @Test(timeout = 4000)
  public void testReplaceUndefinedGuards() {
    Compiler compilerUnnormalized = createCompiler(false, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Node root = parseAndProcess(compilerUnnormalized, peephole, "var x = undefined;");
    String output = compilerUnnormalized.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx=undefined;", output);

    Compiler compilerNormalized = createCompiler(true, true);
    root = parseAndProcess(compilerNormalized, peephole, "var undefined = 1;");
    output = compilerNormalized.toSource(root).replaceAll("\\s+", "");
    assertEquals("varundefined=1;", output);
  }

  @Test(timeout = 4000)
  public void testReduceReturnSideEffects() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "function f() { return void foo(); }");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("functionf(){return void foo();}", output);

    root = parseAndProcess(compiler, peephole, "function f() { return 123; }");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("functionf(){return 123;}", output);

    root = parseAndProcess(compiler, peephole, "function f() { return; }");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("functionf(){return;}", output);
  }

  @Test(timeout = 4000)
  public void testStringArrayLiteralSplittingBoundaries() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peepholeLate = new PeepholeSubstituteAlternateSyntax(true);

    Node root = parseAndProcess(compiler, peepholeLate, "var a = ['1', '2', '3'];");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=['1','2','3'];", output);

    root = parseAndProcess(compiler, peepholeLate, "var a = ['a', 2, 'c', 'd', 'e', 'f'];");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=['a',2,'c','d','e','f'];", output);

    PeepholeSubstituteAlternateSyntax peepholeEarly = new PeepholeSubstituteAlternateSyntax(false);
    root = parseAndProcess(compiler, peepholeEarly, "var a = ['a', 'b', 'c', 'd', 'e', 'f'];");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("vara=['a','b','c','d','e','f'];", output);

    String allDelimiters = " ;_{,}";
    root = parseAndProcess(compiler, peepholeLate,
        "var a = ['" + allDelimiters + "1', '" + allDelimiters + "2', '"
            + allDelimiters + "3', '" + allDelimiters + "4', '"
            + allDelimiters + "5', '" + allDelimiters + "6'];");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertTrue("Should not fold when all candidate delimiters are exhausted",
        output.contains("["));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth: Issue 1062 & Associativity)
  // =========================================================================

  @Test(timeout = 4000)
  public void testAssocitivity() {
    Compiler compiler = createCompiler(false, true);
    String js = "var a,b,c; a || (b || c); a * (b * c); a | (b | c);";
    Node root = parseAndProcess(compiler, new PeepholeSubstituteAlternateSyntax(false), js);
    String printed = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("var a,b,c; a || (b || c); a * (b * c); a | (b | c);",
        "var a,b,c;a||(b||c);a*(b*c);a|(b|c);", printed);
  }

  @Test(timeout = 4000)
  public void testAssocitivityWithAssignment() {
    Compiler compiler = createCompiler(false, true);
    String js = "var a,b,c; x = a || (b || c); x = a * (b * c); x = a | (b | c);";
    Node root = parseAndProcess(compiler, new PeepholeSubstituteAlternateSyntax(false), js);
    String printed = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("var a,b,c; x = a || (b || c); x = a * (b * c); x = a | (b | c);",
        "var a,b,c;x=a||(b||c);x=a*(b*c);x=a|(b|c);", printed);
  }

  @Test(timeout = 4000)
  public void testIssue1062() {
    Compiler compiler = createCompiler(false, true);
    String js = "3 * (4 % 3 * 5);";
    Node root = parseAndProcess(compiler, new PeepholeSubstituteAlternateSyntax(false), js);
    String printed = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("3 * (4 % 3 * 5);", "3*(4%3*5);", printed);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidRegularExpressionFlagsEmitsWarning() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var r = new RegExp('abc', 'invalid_flag');");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testOptimizeSubtreeDefaultPassthrough() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    peephole.beginTraversal(compiler);

    Node emptyNode = IR.empty();
    assertSame(emptyNode, peephole.optimizeSubtree(emptyNode));

    Node blockNode = IR.block();
    assertSame(blockNode, peephole.optimizeSubtree(blockNode));

    Node varNode = IR.var(IR.name("x"));
    assertSame(varNode, peephole.optimizeSubtree(varNode));
  }

  @Test(timeout = 4000)
  public void testNonMatchingStandardConstructors() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);

    Node root = parseAndProcess(compiler, peephole, "var x = new CustomConstructor();");
    String output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx=new CustomConstructor();", output);

    root = parseAndProcess(compiler, peephole, "var x = new (foo.bar)();");
    output = compiler.toSource(root).replaceAll("\\s+", "");
    assertEquals("varx=new (foo.bar)();", output);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLifecycleAndInstanceCreation() {
    Compiler compiler = createCompiler(true, true);
    PeepholeSubstituteAlternateSyntax peepholeEarly = new PeepholeSubstituteAlternateSyntax(false);
    PeepholeSubstituteAlternateSyntax peepholeLate = new PeepholeSubstituteAlternateSyntax(true);

    peepholeEarly.beginTraversal(compiler);
    peepholeEarly.endTraversal(compiler);

    peepholeLate.beginTraversal(compiler);
    peepholeLate.endTraversal(compiler);

    assertNotNull(PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
  }
}