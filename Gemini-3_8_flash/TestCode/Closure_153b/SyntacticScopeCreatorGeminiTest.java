/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.SyntacticScopeCreator
 *
 * 1. Scope Creation & Root Scanning (scanRoot):
 *    - parent == null -> creates global scope (scope.getParent() == null)
 *    - parent != null -> creates local scope
 *    - root is Token.FUNCTION:
 *        - sourceName retrieved from Node.SOURCENAME_PROP
 *        - fnName: empty vs non-empty; NodeUtil.isFunctionExpression(n) true vs false
 *          -> bled into scope if non-empty function expression
 *        - args (Token.LP): iterates over child NAME nodes, calls declareVar
 *        - body: calls scanVars(body, n)
 *    - root is NOT Token.FUNCTION (Global block):
 *        - Asserts scope.getParent() == null
 *        - calls scanVars(n, null)
 *
 * 2. Variable Scanning (scanVars):
 *    - Token.VAR: iterates over child NAME nodes, declares each var
 *    - Token.FUNCTION:
 *        - isFunctionExpression -> ignored/returned immediately
 *        - function declaration -> extracts fnName; if empty, ignores; else declares fnName
 *    - Token.CATCH:
 *        - First child is NAME (catch var), second is BLOCK (body)
 *        - Declares catch var, scans block
 *    - Token.SCRIPT: sets sourceName from Node.SOURCENAME_PROP
 *    - NodeUtil.isControlStructure(n) || NodeUtil.isStatementBlock(n):
 *        - Recursively scans children (BLOCK, IF, WHILE, FOR, DO, SWITCH, TRY, etc.)
 *    - Other node types (EXPR_RESULT, ASSIGN, etc.): skipped unless control/statement block
 *
 * 3. Variable Declaration & Redeclaration Handling (declareVar & DefaultRedeclarationHandler):
 *    - Initial declaration: scope.declare(name, n, declaredType, input)
 *    - Redeclaration condition: scope.isDeclared(name, false) || (scope.isLocal() && name.equals("arguments"))
 *    - onRedeclaration:
 *        - scope.isGlobal():
 *            - Both parent and origParent are CATCH -> allowed, no error
 *            - JSDoc @suppress {duplicate} on name node or parent node -> allowed
 *            - Otherwise -> compiler.report(VAR_MULTIPLY_DECLARED_ERROR)
 *        - scope.isLocal():
 *            - name.equals("arguments") && !NodeUtil.isVarDeclaration(n) ->
 *                compiler.report(VAR_ARGUMENTS_SHADOWED_ERROR)
 *            - name.equals("arguments") && NodeUtil.isVarDeclaration(n) -> allowed without report
 *    - Custom RedeclarationHandler: verified through injected handler constructor.
 *
 * 4. Defect-Targeted Zones (Closure / Normalize / Scope handling):
 *    - Shadowing of "arguments": parameter named "arguments" vs "var arguments" vs "function arguments() {}"
 *    - Multiple variable declarations in global scope with and without @suppress {duplicate}
 *    - Catch block variables in global/local scope
 *    - Function expression name bleeding into local scope
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SyntacticScopeCreatorGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Normal State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateGlobalScopeEmptyBlock() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Scope globalScope = creator.createScope(block, null);

    assertNotNull("Scope should not be null", globalScope);
    assertTrue("Scope must be global", globalScope.isGlobal());
    assertNull("Global scope must not have parent", globalScope.getParent());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCreateGlobalScopeWithVars() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node varNode = new Node(Token.VAR);
    Node nameA = Node.newString(Token.NAME, "a");
    Node nameB = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(nameA);
    varNode.addChildToBack(nameB);
    block.addChildToBack(varNode);

    Scope scope = creator.createScope(block, null);

    assertTrue(scope.isGlobal());
    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertFalse(scope.isDeclared("c", false));
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCreateGlobalScopeWithFunctionDeclaration() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node fnName = Node.newString(Token.NAME, "foo");
    Node fnArgs = new Node(Token.LP);
    Node fnBody = new Node(Token.BLOCK);
    Node fnNode = new Node(Token.FUNCTION, fnName, fnArgs, fnBody);
    block.addChildToBack(fnNode);

    Scope scope = creator.createScope(block, null);

    assertTrue(scope.isGlobal());
    assertTrue(scope.isDeclared("foo", false));
    assertEquals(fnName, scope.getVar("foo").getNameNode());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCreateLocalFunctionScopeWithParametersAndVars() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node fnName = Node.newString(Token.NAME, "myFunc");
    Node param1 = Node.newString(Token.NAME, "p1");
    Node param2 = Node.newString(Token.NAME, "p2");
    Node fnArgs = new Node(Token.LP, param1, param2);

    Node varInBody = new Node(Token.VAR, Node.newString(Token.NAME, "localVar"));
    Node fnBody = new Node(Token.BLOCK, varInBody);

    Node fnNode = new Node(Token.FUNCTION, fnName, fnArgs, fnBody);
    globalBlock.addChildToBack(fnNode);

    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope);
    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("p1", false));
    assertTrue(localScope.isDeclared("p2", false));
    assertTrue(localScope.isDeclared("localVar", false));
    assertFalse(globalScope.isDeclared("localVar", false));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionBleedingScope() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    // Named function expression: var x = function bleedingName() {}
    Node fnName = Node.newString(Token.NAME, "bleedingName");
    Node fnArgs = new Node(Token.LP);
    Node fnBody = new Node(Token.BLOCK);
    Node fnNode = new Node(Token.FUNCTION, fnName, fnArgs, fnBody);

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), fnNode);
    globalBlock.addChildToBack(assign);

    assertTrue("Must be identified as function expression", NodeUtil.isFunctionExpression(fnNode));

    Scope localScope = creator.createScope(fnNode, globalScope);

    assertTrue("Bleeding function name should be in local scope", localScope.isDeclared("bleedingName", false));
    assertFalse("Bleeding function name should NOT be in global scope", globalScope.isDeclared("bleedingName", false));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testControlStructuresTraversal() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node root = new Node(Token.BLOCK);

    // IF structure with VAR inside then and else blocks
    Node ifNode = new Node(Token.IF);
    Node cond = Node.newString(Token.NAME, "cond");
    Node thenBlock = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "varInThen")));
    Node elseBlock = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "varInElse")));
    ifNode.addChildToBack(cond);
    ifNode.addChildToBack(thenBlock);
    ifNode.addChildToBack(elseBlock);

    // FOR structure
    Node forNode = new Node(Token.FOR);
    Node forInit = new Node(Token.VAR, Node.newString(Token.NAME, "varInForInit"));
    Node forCond = Node.newString(Token.NAME, "cond2");
    Node forInc = Node.newString(Token.NAME, "inc");
    Node forBody = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "varInForBody")));
    forNode.addChildToBack(forInit);
    forNode.addChildToBack(forCond);
    forNode.addChildToBack(forInc);
    forNode.addChildToBack(forBody);

    root.addChildToBack(ifNode);
    root.addChildToBack(forNode);

    Scope scope = creator.createScope(root, null);

    assertTrue(scope.isDeclared("varInThen", false));
    assertTrue(scope.isDeclared("varInElse", false));
    assertTrue(scope.isDeclared("varInForInit", false));
    assertTrue(scope.isDeclared("varInForBody", false));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCatchScopeVariableDeclaration() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node root = new Node(Token.BLOCK);

    Node tryBlock = new Node(Token.BLOCK);
    Node catchVar = Node.newString(Token.NAME, "ex");
    Node catchBody = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "varInCatch")));
    Node catchNode = new Node(Token.CATCH, catchVar, catchBody);

    root.addChildToBack(tryBlock);
    root.addChildToBack(catchNode);

    Scope scope = creator.createScope(root, null);

    assertTrue(scope.isDeclared("ex", false));
    assertTrue(scope.isDeclared("varInCatch", false));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSourceNameTrackingWithScriptNode() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node scriptNode = new Node(Token.SCRIPT);
    String filename = "test_script.js";
    scriptNode.putProp(Node.SOURCENAME_PROP, filename);

    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "scriptScopedVar"));
    scriptNode.addChildToBack(varNode);

    Scope scope = creator.createScope(scriptNode, null);

    assertTrue(scope.isDeclared("scriptScopedVar", false));
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testAnonymousFunctionExpressionDoesNotBleed() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    // Anonymous function expression: (function() {})
    Node fnName = Node.newString(Token.NAME, "");
    Node fnArgs = new Node(Token.LP);
    Node fnBody = new Node(Token.BLOCK);
    Node fnNode = new Node(Token.FUNCTION, fnName, fnArgs, fnBody);

    Node parentExpr = new Node(Token.EXPR_RESULT, fnNode);
    globalBlock.addChildToBack(parentExpr);

    Scope localScope = creator.createScope(fnNode, globalScope);

    assertNotNull(localScope);
    assertEquals(0, localScope.getVarCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionDeclarationIgnored() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);

    // Invalid syntax: function () {} as a statement block child
    Node fnName = Node.newString(Token.NAME, "");
    Node fnArgs = new Node(Token.LP);
    Node fnBody = new Node(Token.BLOCK);
    Node fnNode = new Node(Token.FUNCTION, fnName, fnArgs, fnBody);
    globalBlock.addChildToBack(fnNode);

    assertFalse("Must not be expression", NodeUtil.isFunctionExpression(fnNode));

    Scope scope = creator.createScope(globalBlock, null);

    assertNotNull(scope);
    assertEquals(0, scope.getVarCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNestedFunctionDeclarationNotScannedInOuterVars() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);

    // function outer() { function inner() { var innerVar; } }
    Node innerName = Node.newString(Token.NAME, "inner");
    Node innerArgs = new Node(Token.LP);
    Node innerBody = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "innerVar")));
    Node innerFn = new Node(Token.FUNCTION, innerName, innerArgs, innerBody);

    Node outerName = Node.newString(Token.NAME, "outer");
    Node outerArgs = new Node(Token.LP);
    Node outerBody = new Node(Token.BLOCK, innerFn);
    Node outerFn = new Node(Token.FUNCTION, outerName, outerArgs, outerBody);

    globalBlock.addChildToBack(outerFn);

    Scope globalScope = creator.createScope(globalBlock, null);
    assertTrue(globalScope.isDeclared("outer", false));

    Scope outerScope = creator.createScope(outerFn, globalScope);
    assertTrue(outerScope.isDeclared("inner", false));
    // innerVar should not be visible in outer scope because inner is not examined
    assertFalse("innerVar should not be visible in outerScope", outerScope.isDeclared("innerVar", false));
  }

  @Test(timeout = 4000)
  public void testMultipleVarsInSingleDeclaration() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node varNode = new Node(Token.VAR);
    varNode.addChildToBack(Node.newString(Token.NAME, "x"));
    varNode.addChildToBack(Node.newString(Token.NAME, "y"));
    varNode.addChildToBack(Node.newString(Token.NAME, "z"));
    block.addChildToBack(varNode);

    Scope scope = creator.createScope(block, null);

    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("y", false));
    assertTrue(scope.isDeclared("z", false));
    assertEquals(3, scope.getVarCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Shadowing "arguments" & Duplicate Vars)
  // =========================================================================

  @Test(timeout = 4000)
  public void testShadowingArgumentsByParameterReportsError() {
    // Defect zone: Shadowing "arguments" as a function parameter is forbidden
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node fnName = Node.newString(Token.NAME, "testFn");
    Node args = new Node(Token.LP, Node.newString(Token.NAME, "arguments"));
    Node body = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION, fnName, args, body);
    globalBlock.addChildToBack(fn);

    creator.createScope(fn, globalScope);

    assertEquals("Expected error for shadowing arguments via parameter", 1, compiler.getErrorCount());
    assertEquals(SyntacticScopeCreator.VAR_ARGUMENTS_SHADOWED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testShadowingArgumentsByVarDeclarationIsAllowed() {
    // In local scope, 'var arguments;' is allowed and should NOT report VAR_ARGUMENTS_SHADOWED_ERROR
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node fnName = Node.newString(Token.NAME, "testFn");
    Node args = new Node(Token.LP);
    Node varArguments = new Node(Token.VAR, Node.newString(Token.NAME, "arguments"));
    Node body = new Node(Token.BLOCK, varArguments);
    Node fn = new Node(Token.FUNCTION, fnName, args, body);
    globalBlock.addChildToBack(fn);

    creator.createScope(fn, globalScope);

    assertEquals("var arguments should not report an error", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalDuplicateVarReportsError() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "dupVar"));
    Node var2 = new Node(Token.VAR, Node.newString(Token.NAME, "dupVar"));
    block.addChildToBack(var1);
    block.addChildToBack(var2);

    creator.createScope(block, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testGlobalDuplicateVarAllowedWithDuplicateSuppressionOnName() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "dupVar"));
    Node name2 = Node.newString(Token.NAME, "dupVar");

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordSuppressions(Collections.singleton("duplicate"));
    JSDocInfo info = builder.build(name2);
    name2.setJSDocInfo(info);

    Node var2 = new Node(Token.VAR, name2);
    block.addChildToBack(var1);
    block.addChildToBack(var2);

    creator.createScope(block, null);

    assertEquals("Suppressed duplicate should not trigger error", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalDuplicateVarAllowedWithDuplicateSuppressionOnParentVar() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "dupVar"));
    Node name2 = Node.newString(Token.NAME, "dupVar");
    Node var2 = new Node(Token.VAR, name2);

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordSuppressions(Collections.singleton("duplicate"));
    JSDocInfo info = builder.build(var2);
    var2.setJSDocInfo(info);

    block.addChildToBack(var1);
    block.addChildToBack(var2);

    creator.createScope(block, null);

    assertEquals("Suppressed duplicate on parent should not trigger error", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDuplicateCatchVariablesAllowed() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    // Two consecutive catch blocks in global scope with same name 'err'
    Node catch1 = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    Node catch2 = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    block.addChildToBack(catch1);
    block.addChildToBack(catch2);

    creator.createScope(block, null);

    assertEquals("Consecutive catch variables with same name are allowed", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCatchVariableFollowedByGlobalVarReportsError() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);

    Node catchBlock = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "err"));
    block.addChildToBack(catchBlock);
    block.addChildToBack(varNode);

    creator.createScope(block, null);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR.key,
        compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition D: Custom Redeclaration Handler & Edge Cases
  // =========================================================================

  private static class RecordingRedeclarationHandler implements SyntacticScopeCreator.RedeclarationHandler {
    final List<String> redeclaredNames = new ArrayList<>();

    @Override
    public void onRedeclaration(Scope s, String name, Node n, Node parent, Node gramps, Node nodeWithLineNumber) {
      redeclaredNames.add(name);
    }
  }

  @Test(timeout = 4000)
  public void testCustomRedeclarationHandlerInvoked() {
    RecordingRedeclarationHandler handler = new RecordingRedeclarationHandler();
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler, handler);

    Node block = new Node(Token.BLOCK);
    Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "customDup"));
    Node var2 = new Node(Token.VAR, Node.newString(Token.NAME, "customDup"));
    block.addChildToBack(var1);
    block.addChildToBack(var2);

    creator.createScope(block, null);

    assertEquals(1, handler.redeclaredNames.size());
    assertEquals("customDup", handler.redeclaredNames.get(0));
    // Default error reporting is bypassed by custom handler
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNonFunctionRootWithParentThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node nonFnNode = new Node(Token.BLOCK);
    try {
      // Trying to create a local scope where root is NOT a function should fail
      creator.createScope(nonFnNode, globalScope);
      fail("Expected IllegalStateException when creating child scope on non-function root");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(scope.getParent() == null) triggered
    }
  }

  @Test(timeout = 4000)
  public void testFunctionWithInvalidArgsNodeThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node fnName = Node.newString(Token.NAME, "badFn");
    Node invalidArgs = new Node(Token.NAME, "notLP");
    Node body = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION, fnName, invalidArgs, body);

    try {
      creator.createScope(fn, globalScope);
      fail("Expected IllegalStateException for args node that is not Token.LP");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(args.getType() == Token.LP)
    }
  }

  @Test(timeout = 4000)
  public void testFunctionWithInvalidChildInArgsThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node globalBlock = new Node(Token.BLOCK);
    Scope globalScope = creator.createScope(globalBlock, null);

    Node fnName = Node.newString(Token.NAME, "badFn2");
    Node args = new Node(Token.LP, new Node(Token.NUMBER));
    Node body = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION, fnName, args, body);

    try {
      creator.createScope(fn, globalScope);
      fail("Expected IllegalStateException for parameter node that is not Token.NAME");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(a.getType() == Token.NAME)
    }
  }

  @Test(timeout = 4000)
  public void testVarWithInvalidChildThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, new Node(Token.NUMBER));
    block.addChildToBack(varNode);

    try {
      creator.createScope(block, null);
      fail("Expected IllegalStateException for var child that is not Token.NAME");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(child.getType() == Token.NAME)
    }
  }

  @Test(timeout = 4000)
  public void testCatchWithInvalidChildCountThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"));
    block.addChildToBack(catchNode);

    try {
      creator.createScope(block, null);
      fail("Expected IllegalStateException for catch block with childCount != 2");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(n.getChildCount() == 2)
    }
  }

  @Test(timeout = 4000)
  public void testCatchWithNonNameFirstChildThrowsIllegalStateException() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, new Node(Token.BLOCK), new Node(Token.BLOCK));
    block.addChildToBack(catchNode);

    try {
      creator.createScope(block, null);
      fail("Expected IllegalStateException for catch block with first child not Token.NAME");
    } catch (IllegalStateException expected) {
      // Success: Preconditions.checkState(n.getFirstChild().getType() == Token.NAME)
    }
  }

  // =========================================================================
  // Partition E: Diagnostic Messages and Compiler Integration
  // =========================================================================

  @Test(timeout = 4000)
  public void testVarMultiplyDeclaredDiagnosticFormat() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node script = new Node(Token.SCRIPT);
    String sourceName = "my_source.js";
    script.putProp(Node.SOURCENAME_PROP, sourceName);

    Node name1 = Node.newString(Token.NAME, "z");
    Node var1 = new Node(Token.VAR, name1);
    Node name2 = Node.newString(Token.NAME, "z");
    Node var2 = new Node(Token.VAR, name2);
    script.addChildToBack(var1);
    script.addChildToBack(var2);

    compiler.putCompilerInput(new InputId(sourceName), new CompilerInput(new JsAst(new SourceFile(sourceName))));

    creator.createScope(script, null);

    assertEquals(1, compiler.getErrorCount());
    JSError error = compiler.getErrors()[0];
    assertEquals(SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR, error.getType());
    assertEquals(sourceName, error.sourceName);
    assertTrue(error.description.contains("z"));
  }

  @Test(timeout = 4000)
  public void testScopeCreatorStateResetAfterExecution() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node block1 = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "first")));
    Node block2 = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "second")));

    Scope scope1 = creator.createScope(block1, null);
    Scope scope2 = creator.createScope(block2, null);

    assertNotSame(scope1, scope2);
    assertTrue(scope1.isDeclared("first", false));
    assertFalse(scope1.isDeclared("second", false));
    assertTrue(scope2.isDeclared("second", false));
    assertFalse(scope2.isDeclared("first", false));
  }
}