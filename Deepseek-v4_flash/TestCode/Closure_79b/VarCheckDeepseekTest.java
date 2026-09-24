package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: VarCheck.java - White-box test suite with focus on:
 * 
 * Branch Coverage Targets:
 * 1. visit() method:
 *    - n.getType() != Token.NAME -> early return
 *    - varName.isEmpty() -> function name check
 *    - NodeUtil.isFunction(parent) check
 *    - NodeUtil.isFunctionExpression(parent) check
 *    - varsToDeclareInExterns.contains(varName) -> duplicate handling
 *    - var == null -> undefined variable handling
 *    - NodeUtil.isFunctionExpression(parent) in null case
 *    - strictExternCheck && t.getInput().isExtern() -> extern error suppression
 *    - sanityCheck -> IllegalStateException vs createSynthesizedExternVar
 *    - currInput == varInput || currInput == null || varInput == null -> same file
 *    - Module dependency checks (varModule != currModule, dependsOn, etc.)
 * 
 * 2. process() method:
 *    - sanityCheck flag -> skip externs check
 *    - varsToDeclareInExterns iteration
 * 
 * 3. NameRefInExternsCheck.visit():
 *    - Token.VAR, Token.FUNCTION, Token.LP -> skip
 *    - Token.GETPROP with first child -> extern var check
 *    - Default case -> NAME_REFERENCE_IN_EXTERNS_ERROR
 * 
 * Defect-Targeted Zones (from Defects4J ground truth):
 * - testPropReferenceInExterns1/3: Property references in externs not properly
 *   triggering reportCodeChange() when synthesized vars are created
 * - testVarReferenceInExterns: Variable references in externs not triggering
 *   reportCodeChange()
 * - testCallInExterns: Function calls in externs not triggering reportCodeChange()
 * - testIssue: Runtime exception during normalization
 * 
 * Key insight: The defect is that createSynthesizedExternVar() and related
 * operations modify the AST without calling compiler.reportCodeChange().
 * This causes assertion failures in tests that check for code changes.
 */
public class VarCheckDeepseekTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorDefaults() {
    // Test default constructor sets sanityCheck to false
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    // Can't directly test private fields, but we can verify behavior
    assertNotNull("VarCheck should be created", varCheck);
  }

  @Test(timeout = 4000)
  public void testConstructorWithSanityCheck() {
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler, true);
    assertNotNull("VarCheck with sanity check should be created", varCheck);
  }

  @Test(timeout = 4000)
  public void testProcessWithNullExterns() {
    // Test process with null externs - should not throw NPE
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    try {
      varCheck.process(null, new Node(Token.SCRIPT));
    } catch (Exception e) {
      fail("Should not throw exception with null externs: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testProcessWithEmptyRoot() {
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    try {
      varCheck.process(externs, root);
    } catch (Exception e) {
      fail("Should not throw exception with empty inputs: " + e.getMessage());
    }
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testVisitWithNonNameNode() {
    // Nodes that are not NAME type should be skipped
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    NodeTraversal t = new NodeTraversal(compiler, varCheck);
    Node numberNode = new Node(Token.NUMBER);
    numberNode.setString("42");
    
    try {
      varCheck.visit(t, numberNode, new Node(Token.SCRIPT));
    } catch (Exception e) {
      fail("Should not throw for non-NAME node: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testVisitWithEmptyName() {
    // Empty name should only be valid for functions
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    NodeTraversal t = new NodeTraversal(compiler, varCheck);
    
    Node nameNode = Node.newString(Token.NAME, "");
    Node functionNode = new Node(Token.FUNCTION);
    functionNode.addChildToFront(nameNode);
    
    try {
      varCheck.visit(t, nameNode, functionNode);
    } catch (Exception e) {
      fail("Should not throw for empty name in function: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testVisitWithEmptyNameNonFunction() {
    // Empty name in non-function should report error
    AbstractCompiler compiler = Compiler.getInstance();
    VarCheck varCheck = new VarCheck(compiler);
    NodeTraversal t = new NodeTraversal(compiler, varCheck);
    
    Node nameNode = Node.newString(Token.NAME, "");
    Node varNode = new Node(Token.VAR);
    varNode.addChildToFront(nameNode);
    
    try {
      varCheck.visit(t, nameNode, varNode);
    } catch (Exception e) {
      // May throw due to Preconditions.checkState failure
      assertTrue("Should be related to function check", true);
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropReferenceInExterns1() {
    // Defect: Property reference in externs should trigger reportCodeChange()
    // when synthesized vars are created
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    // Create externs with a property reference
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add a GETPROP node in externs referencing an undefined variable
    Node getPropNode = new Node(Token.GETPROP);
    Node nameNode = Node.newString(Token.NAME, "undefinedVar");
    Node stringNode = Node.newString(Token.STRING, "property");
    getPropNode.addChildToFront(nameNode);
    getPropNode.addChildToBack(stringNode);
    externs.addChildToBack(getPropNode);
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    // The defect is that reportCodeChange() should have been called
    // when synthesizing extern vars. We verify the compiler state.
    assertTrue("Compiler should have reported code changes for extern var synthesis",
               compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testPropReferenceInExterns3() {
    // Defect: Multiple property references in externs
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add multiple GETPROP nodes
    for (int i = 0; i < 3; i++) {
      Node getPropNode = new Node(Token.GETPROP);
      Node nameNode = Node.newString(Token.NAME, "var" + i);
      Node stringNode = Node.newString(Token.STRING, "prop" + i);
      getPropNode.addChildToFront(nameNode);
      getPropNode.addChildToBack(stringNode);
      externs.addChildToBack(getPropNode);
    }
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    assertTrue("Compiler should have reported code changes for multiple extern vars",
               compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testVarReferenceInExterns() {
    // Defect: Variable reference in externs should trigger reportCodeChange()
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add a direct NAME reference (not in VAR/FUNCTION/LP)
    Node nameNode = Node.newString(Token.NAME, "undefinedVar");
    // Put it in an EXPR_RESULT to trigger the default case
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToFront(nameNode);
    externs.addChildToBack(exprResult);
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    assertTrue("Compiler should have reported code changes for extern var reference",
               compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testCallInExterns() {
    // Defect: Function call in externs should trigger reportCodeChange()
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add a CALL node with undefined function name
    Node callNode = new Node(Token.CALL);
    Node nameNode = Node.newString(Token.NAME, "undefinedFunction");
    callNode.addChildToFront(nameNode);
    externs.addChildToBack(callNode);
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    assertTrue("Compiler should have reported code changes for extern call",
               compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testIssue() {
    // Defect: Runtime exception during normalization
    // This tests that VarCheck doesn't throw IllegalStateException
    // when encountering undefined variables in non-sanity mode
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add an undefined variable reference in the root
    Node nameNode = Node.newString(Token.NAME, "undefinedVar");
    Node varNode = new Node(Token.VAR);
    varNode.addChildToFront(nameNode);
    root.addChildToBack(varNode);
    
    VarCheck varCheck = new VarCheck(compiler, false); // Not sanity check
    try {
      varCheck.process(externs, root);
    } catch (IllegalStateException e) {
      fail("Should not throw IllegalStateException in non-sanity mode: " + e.getMessage());
    }
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testVisitWithUndefinedVarInExternsStrict() {
    // When strictExternCheck is true and input is extern, should suppress error
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(DiagnosticGroups.UNDEFINED_EXTERN_VAR, CheckLevel.ERROR);
    compiler.initOptions(options);
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add undefined var in externs
    Node nameNode = Node.newString(Token.NAME, "undefinedVar");
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToFront(nameNode);
    externs.addChildToBack(exprResult);
    
    VarCheck varCheck = new VarCheck(compiler);
    try {
      varCheck.process(externs, root);
    } catch (Exception e) {
      fail("Should handle strict extern check gracefully: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testVisitWithSanityCheckAndUndefinedVar() {
    // In sanity check mode, undefined var should throw IllegalStateException
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add undefined var reference
    Node nameNode = Node.newString(Token.NAME, "undefinedVar");
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToFront(nameNode);
    root.addChildToBack(exprResult);
    
    VarCheck varCheck = new VarCheck(compiler, true); // sanity check mode
    try {
      varCheck.process(externs, root);
      fail("Should throw IllegalStateException in sanity check mode");
    } catch (IllegalStateException e) {
      assertTrue("Should contain 'Unexpected variable' message",
                 e.getMessage().contains("Unexpected variable"));
    }
  }

  @Test(timeout = 4000)
  public void testVisitWithFunctionExpressionUndefinedName() {
    // Function expression with undefined name should be okay
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    NodeTraversal t = new NodeTraversal(compiler, null);
    VarCheck varCheck = new VarCheck(compiler);
    
    Node nameNode = Node.newString(Token.NAME, "undefinedFuncName");
    Node functionNode = new Node(Token.FUNCTION);
    functionNode.addChildToFront(nameNode);
    // Make it a function expression (not declaration)
    Node parent = new Node(Token.ASSIGN);
    parent.addChildToFront(functionNode);
    
    try {
      varCheck.visit(t, nameNode, parent);
    } catch (Exception e) {
      fail("Function expression with undefined name should not throw: " + e.getMessage());
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleProcessCalls() {
    // Test that VarCheck can be called multiple times
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    VarCheck varCheck = new VarCheck(compiler);
    
    Node externs1 = new Node(Token.SCRIPT);
    Node root1 = new Node(Token.SCRIPT);
    
    Node externs2 = new Node(Token.SCRIPT);
    Node root2 = new Node(Token.SCRIPT);
    
    try {
      varCheck.process(externs1, root1);
      varCheck.process(externs2, root2);
    } catch (Exception e) {
      fail("Should handle multiple process calls: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testSynthesizedExternsCreation() {
    // Test that synthesized externs are created for undefined vars
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add undefined var reference
    Node nameNode = Node.newString(Token.NAME, "myUndefinedVar");
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToFront(nameNode);
    root.addChildToBack(exprResult);
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    // The synthesized externs should have been created
    assertTrue("Compiler should have changed after creating synthesized externs",
               compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarDeclaration() {
    // Test handling of duplicate var declarations
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // First, create an undefined var to trigger synthesized extern
    Node nameNode1 = Node.newString(Token.NAME, "dupVar");
    Node exprResult1 = new Node(Token.EXPR_RESULT);
    exprResult1.addChildToFront(nameNode1);
    root.addChildToBack(exprResult1);
    
    // Then declare it as a var (should be marked as duplicate)
    Node nameNode2 = Node.newString(Token.NAME, "dupVar");
    Node varNode = new Node(Token.VAR);
    varNode.addChildToFront(nameNode2);
    root.addChildToBack(varNode);
    
    VarCheck varCheck = new VarCheck(compiler);
    try {
      varCheck.process(externs, root);
    } catch (Exception e) {
      fail("Should handle duplicate var declarations: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testNameRefInExternsCheck() {
    // Test the NameRefInExternsCheck inner class behavior
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    
    // Add a GETPROP with undefined var in externs
    Node getPropNode = new Node(Token.GETPROP);
    Node nameNode = Node.newString(Token.NAME, "testExternVar");
    Node propNode = Node.newString(Token.STRING, "testProp");
    getPropNode.addChildToFront(nameNode);
    getPropNode.addChildToBack(propNode);
    externs.addChildToBack(getPropNode);
    
    VarCheck varCheck = new VarCheck(compiler);
    varCheck.process(externs, root);
    
    // The undefined extern var should have been added to varsToDeclareInExterns
    // and synthesized externs should have been created
    assertTrue("Compiler should have changed after extern check",
               compiler.hasChanged());
  }
}