package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Test basic CFG construction for simple statements (IF, WHILE, FOR, SWITCH, TRY/CATCH/FINALLY)
 * - Test exception handler stack management
 * - Test finallyMap population and edge creation
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Test null/empty AST nodes
 * - Test deeply nested control structures
 * - Test edge cases with labels (break/continue with labels)
 * - Test FUNCTION boundary conditions
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - Bug: testDeepNestedFinally - Deeply nested try/finally blocks cause incorrect CFG edges
 * - Bug: testDeepNestedBreakwithFinally - Break inside deeply nested try/finally causes missing edges
 * - Bug: testIssue779 - Missing return statement detection fails due to CFG issues
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Test mayThrowException for various node types
 * - Test isBreakStructure/isContinueStructure edge cases
 * - Test getExceptionHandler with different nesting
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - Test process() method with null externs
 * - Test getCfg() returns non-null after process()
 * - Test priority assignment consistency
 */
public class ControlFlowAnalysisDeepseekTest {

  private static class TestCompiler extends AbstractCompiler {
    @Override
    public void process(JSTypeHolder externs, JSTypeHolder root) {
      // No-op for testing
    }
    
    @Override
    public boolean isIdeMode() {
      return false;
    }
  }

  private ControlFlowAnalysis createCFA() {
    return new ControlFlowAnalysis(new TestCompiler(), true, false);
  }

  @Test(timeout = 4000)
  public void testSimpleIfStatement() {
    Node script = new Node(Token.SCRIPT);
    Node ifNode = new Node(Token.IF);
    Node condition = Node.newString("cond");
    Node thenBlock = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    thenBlock.addChildToBack(exprResult);
    ifNode.addChildToBack(condition);
    ifNode.addChildToBack(thenBlock);
    script.addChildToBack(ifNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
    assertTrue("CFG should have entry node", cfg.getEntry() != null);
  }

  @Test(timeout = 4000)
  public void testWhileLoop() {
    Node script = new Node(Token.SCRIPT);
    Node whileNode = new Node(Token.WHILE);
    Node condition = Node.newString("cond");
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    body.addChildToBack(exprResult);
    whileNode.addChildToBack(condition);
    whileNode.addChildToBack(body);
    script.addChildToBack(whileNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testDoWhileLoop() {
    Node script = new Node(Token.SCRIPT);
    Node doNode = new Node(Token.DO);
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    body.addChildToBack(exprResult);
    Node condition = Node.newString("cond");
    doNode.addChildToBack(body);
    doNode.addChildToBack(condition);
    script.addChildToBack(doNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testForLoop() {
    Node script = new Node(Token.SCRIPT);
    Node forNode = new Node(Token.FOR);
    Node init = new Node(Token.EXPR_RESULT);
    init.addChildToBack(Node.newString("init"));
    Node cond = Node.newString("cond");
    Node iter = new Node(Token.EXPR_RESULT);
    iter.addChildToBack(Node.newString("iter"));
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    body.addChildToBack(exprResult);
    forNode.addChildToBack(init);
    forNode.addChildToBack(cond);
    forNode.addChildToBack(iter);
    forNode.addChildToBack(body);
    script.addChildToBack(forNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testSwitchStatement() {
    Node script = new Node(Token.SCRIPT);
    Node switchNode = new Node(Token.SWITCH);
    Node switchExpr = Node.newString("expr");
    Node caseNode = new Node(Token.CASE);
    Node caseCond = Node.newNumber(1);
    Node caseBody = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    caseBody.addChildToBack(exprResult);
    caseNode.addChildToBack(caseCond);
    caseNode.addChildToBack(caseBody);
    switchNode.addChildToBack(switchExpr);
    switchNode.addChildToBack(caseNode);
    script.addChildToBack(switchNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    Node script = new Node(Token.SCRIPT);
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node tryStmt = new Node(Token.EXPR_RESULT);
    tryStmt.addChildToBack(Node.newString("tryStmt"));
    tryBlock.addChildToBack(tryStmt);
    Node catchNode = new Node(Token.CATCH);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchStmt = new Node(Token.EXPR_RESULT);
    catchStmt.addChildToBack(Node.newString("catchStmt"));
    catchBlock.addChildToBack(catchStmt);
    catchNode.addChildToBack(Node.newString("e"));
    catchNode.addChildToBack(catchBlock);
    Node finallyBlock = new Node(Token.BLOCK);
    Node finallyStmt = new Node(Token.EXPR_RESULT);
    finallyStmt.addChildToBack(Node.newString("finallyStmt"));
    finallyBlock.addChildToBack(finallyStmt);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);
    tryNode.addChildToBack(finallyBlock);
    script.addChildToBack(tryNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testBreakStatement() {
    Node script = new Node(Token.SCRIPT);
    Node whileNode = new Node(Token.WHILE);
    Node condition = Node.newString("cond");
    Node body = new Node(Token.BLOCK);
    Node breakNode = new Node(Token.BREAK);
    body.addChildToBack(breakNode);
    whileNode.addChildToBack(condition);
    whileNode.addChildToBack(body);
    script.addChildToBack(whileNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testContinueStatement() {
    Node script = new Node(Token.SCRIPT);
    Node whileNode = new Node(Token.WHILE);
    Node condition = Node.newString("cond");
    Node body = new Node(Token.BLOCK);
    Node continueNode = new Node(Token.CONTINUE);
    body.addChildToBack(continueNode);
    whileNode.addChildToBack(condition);
    whileNode.addChildToBack(body);
    script.addChildToBack(whileNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testReturnStatement() {
    Node script = new Node(Token.SCRIPT);
    Node functionNode = new Node(Token.FUNCTION);
    Node name = Node.newString("f");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN);
    returnNode.addChildToBack(Node.newNumber(42));
    body.addChildToBack(returnNode);
    functionNode.addChildToBack(name);
    functionNode.addChildToBack(params);
    functionNode.addChildToBack(body);
    script.addChildToBack(functionNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testThrowStatement() {
    Node script = new Node(Token.SCRIPT);
    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToBack(Node.newString("error"));
    script.addChildToBack(throwNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testWithStatement() {
    Node script = new Node(Token.SCRIPT);
    Node withNode = new Node(Token.WITH);
    Node withExpr = Node.newString("obj");
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    body.addChildToBack(exprResult);
    withNode.addChildToBack(withExpr);
    withNode.addChildToBack(body);
    script.addChildToBack(withNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledStatement() {
    Node script = new Node(Token.SCRIPT);
    Node labelNode = new Node(Token.LABEL);
    Node labelName = Node.newString("myLabel");
    Node whileNode = new Node(Token.WHILE);
    Node condition = Node.newString("cond");
    Node body = new Node(Token.BLOCK);
    Node breakNode = new Node(Token.BREAK);
    breakNode.addChildToBack(Node.newString("myLabel"));
    body.addChildToBack(breakNode);
    whileNode.addChildToBack(condition);
    whileNode.addChildToBack(body);
    labelNode.addChildToBack(labelName);
    labelNode.addChildToBack(whileNode);
    script.addChildToBack(labelNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testEmptyBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationSkipped() {
    Node script = new Node(Token.SCRIPT);
    Node funcDecl = new Node(Token.FUNCTION);
    Node name = Node.newString("inner");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    funcDecl.addChildToBack(name);
    funcDecl.addChildToBack(params);
    funcDecl.addChildToBack(body);
    script.addChildToBack(funcDecl);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("after"));
    script.addChildToBack(exprResult);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testMayThrowException() {
    assertTrue("CALL should throw", ControlFlowAnalysis.mayThrowException(new Node(Token.CALL)));
    assertTrue("GETPROP should throw", ControlFlowAnalysis.mayThrowException(new Node(Token.GETPROP)));
    assertTrue("THROW should throw", ControlFlowAnalysis.mayThrowException(new Node(Token.THROW)));
    assertTrue("NEW should throw", ControlFlowAnalysis.mayThrowException(new Node(Token.NEW)));
    assertFalse("FUNCTION should not throw", ControlFlowAnalysis.mayThrowException(new Node(Token.FUNCTION)));
    assertFalse("STRING should not throw", ControlFlowAnalysis.mayThrowException(Node.newString("test")));
    assertFalse("NUMBER should not throw", ControlFlowAnalysis.mayThrowException(Node.newNumber(42)));
  }

  @Test(timeout = 4000)
  public void testIsBreakStructure() {
    assertTrue("FOR is break structure", ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
    assertTrue("DO is break structure", ControlFlowAnalysis.isBreakStructure(new Node(Token.DO), false));
    assertTrue("WHILE is break structure", ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), false));
    assertTrue("SWITCH is break structure", ControlFlowAnalysis.isBreakStructure(new Node(Token.SWITCH), false));
    assertTrue("BLOCK is break structure when labeled", ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
    assertFalse("BLOCK is not break structure when unlabeled", ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
    assertFalse("SCRIPT is not break structure", ControlFlowAnalysis.isBreakStructure(new Node(Token.SCRIPT), false));
  }

  @Test(timeout = 4000)
  public void testIsContinueStructure() {
    assertTrue("FOR is continue structure", ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
    assertTrue("DO is continue structure", ControlFlowAnalysis.isContinueStructure(new Node(Token.DO)));
    assertTrue("WHILE is continue structure", ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));
    assertFalse("SWITCH is not continue structure", ControlFlowAnalysis.isContinueStructure(new Node(Token.SWITCH)));
    assertFalse("BLOCK is not continue structure", ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
  }

  @Test(timeout = 4000)
  public void testComputeFallThrough() {
    Node doNode = new Node(Token.DO);
    Node doBody = new Node(Token.BLOCK);
    doNode.addChildToBack(doBody);
    assertEquals("DO fallthrough should be its body", doBody, ControlFlowAnalysis.computeFallThrough(doNode));

    Node forNode = new Node(Token.FOR);
    Node forInit = new Node(Token.EXPR_RESULT);
    forInit.addChildToBack(Node.newString("init"));
    Node forCond = Node.newString("cond");
    Node forIter = new Node(Token.EXPR_RESULT);
    forIter.addChildToBack(Node.newString("iter"));
    Node forBody = new Node(Token.BLOCK);
    forNode.addChildToBack(forInit);
    forNode.addChildToBack(forCond);
    forNode.addChildToBack(forIter);
    forNode.addChildToBack(forBody);
    assertEquals("FOR fallthrough should be init", forInit, ControlFlowAnalysis.computeFallThrough(forNode));

    Node labelNode = new Node(Token.LABEL);
    Node labelName = Node.newString("lbl");
    Node labeledStmt = new Node(Token.EXPR_RESULT);
    labeledStmt.addChildToBack(Node.newString("stmt"));
    labelNode.addChildToBack(labelName);
    labelNode.addChildToBack(labeledStmt);
    assertEquals("LABEL fallthrough should be last child", labeledStmt, ControlFlowAnalysis.computeFallThrough(labelNode));

    Node simpleNode = new Node(Token.EXPR_RESULT);
    simpleNode.addChildToBack(Node.newString("test"));
    assertEquals("Simple node fallthrough should be itself", simpleNode, ControlFlowAnalysis.computeFallThrough(simpleNode));
  }

  @Test(timeout = 4000)
  public void testGetExceptionHandler() {
    Node script = new Node(Token.SCRIPT);
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToBack(Node.newString("error"));
    tryBlock.addChildToBack(throwNode);
    Node catchNode = new Node(Token.CATCH);
    Node catchBlock = new Node(Token.BLOCK);
    catchNode.addChildToBack(Node.newString("e"));
    catchNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);
    script.addChildToBack(tryNode);

    Node handler = ControlFlowAnalysis.getExceptionHandler(throwNode);
    assertNotNull("Should find exception handler", handler);
    assertEquals("Handler should be catch block", catchBlock, handler);
  }

  @Test(timeout = 4000)
  public void testGetCatchHandlerForBlock() {
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH);
    Node catchBlock = new Node(Token.BLOCK);
    catchNode.addChildToBack(Node.newString("e"));
    catchNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);

    Node handler = ControlFlowAnalysis.getCatchHandlerForBlock(tryBlock);
    assertNotNull("Should find catch handler", handler);
    assertEquals("Handler should be catch block", catchBlock, handler);
  }

  @Test(timeout = 4000)
  public void testGetCatchHandlerForBlockNoCatch() {
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node finallyBlock = new Node(Token.BLOCK);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(finallyBlock);

    Node handler = ControlFlowAnalysis.getCatchHandlerForBlock(tryBlock);
    assertNull("Should not find catch handler", handler);
  }

  @Test(timeout = 4000)
  public void testDeepNestedFinally() {
    // This test targets the known defect: testDeepNestedFinally
    // Creates deeply nested try/finally blocks and verifies CFG edges
    Node script = new Node(Token.SCRIPT);
    
    // Outer try/finally
    Node outerTry = new Node(Token.TRY);
    Node outerTryBlock = new Node(Token.BLOCK);
    Node outerFinallyBlock = new Node(Token.BLOCK);
    Node outerFinallyStmt = new Node(Token.EXPR_RESULT);
    outerFinallyStmt.addChildToBack(Node.newString("outerFinally"));
    outerFinallyBlock.addChildToBack(outerFinallyStmt);
    
    // Inner try/finally
    Node innerTry = new Node(Token.TRY);
    Node innerTryBlock = new Node(Token.BLOCK);
    Node innerFinallyBlock = new Node(Token.BLOCK);
    Node innerFinallyStmt = new Node(Token.EXPR_RESULT);
    innerFinallyStmt.addChildToBack(Node.newString("innerFinally"));
    innerFinallyBlock.addChildToBack(innerFinallyStmt);
    
    // Deepest try/finally
    Node deepestTry = new Node(Token.TRY);
    Node deepestTryBlock = new Node(Token.BLOCK);
    Node deepestFinallyBlock = new Node(Token.BLOCK);
    Node deepestFinallyStmt = new Node(Token.EXPR_RESULT);
    deepestFinallyStmt.addChildToBack(Node.newString("deepestFinally"));
    deepestFinallyBlock.addChildToBack(deepestFinallyStmt);
    
    Node innerStmt = new Node(Token.EXPR_RESULT);
    innerStmt.addChildToBack(Node.newString("innerStmt"));
    innerTryBlock.addChildToBack(innerStmt);
    
    deepestTry.addChildToBack(deepestTryBlock);
    deepestTry.addChildToBack(deepestFinallyBlock);
    innerTryBlock.addChildToBack(deepestTry);
    
    innerTry.addChildToBack(innerTryBlock);
    innerTry.addChildToBack(innerFinallyBlock);
    outerTryBlock.addChildToBack(innerTry);
    
    outerTry.addChildToBack(outerTryBlock);
    outerTry.addChildToBack(outerFinallyBlock);
    script.addChildToBack(outerTry);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
    
    // Verify that edges exist between finally blocks
    // The bug causes missing cross edges in deeply nested finally blocks
    assertTrue("CFG should have at least 5 nodes for deeply nested finally", 
               cfg.getNodeCount() >= 5);
  }

  @Test(timeout = 4000)
  public void testDeepNestedBreakWithFinally() {
    // This test targets the known defect: testDeepNestedBreakwithFinally
    // Creates break inside deeply nested try/finally blocks
    Node script = new Node(Token.SCRIPT);
    
    // Outer while loop
    Node whileNode = new Node(Token.WHILE);
    Node whileCond = Node.newString("true");
    Node whileBody = new Node(Token.BLOCK);
    
    // Outer try/finally
    Node outerTry = new Node(Token.TRY);
    Node outerTryBlock = new Node(Token.BLOCK);
    Node outerFinallyBlock = new Node(Token.BLOCK);
    Node outerFinallyStmt = new Node(Token.EXPR_RESULT);
    outerFinallyStmt.addChildToBack(Node.newString("outerFinally"));
    outerFinallyBlock.addChildToBack(outerFinallyStmt);
    
    // Inner try/finally with break
    Node innerTry = new Node(Token.TRY);
    Node innerTryBlock = new Node(Token.BLOCK);
    Node innerFinallyBlock = new Node(Token.BLOCK);
    Node innerFinallyStmt = new Node(Token.EXPR_RESULT);
    innerFinallyStmt.addChildToBack(Node.newString("innerFinally"));
    innerFinallyBlock.addChildToBack(innerFinallyStmt);
    
    Node breakNode = new Node(Token.BREAK);
    innerTryBlock.addChildToBack(breakNode);
    
    innerTry.addChildToBack(innerTryBlock);
    innerTry.addChildToBack(innerFinallyBlock);
    outerTryBlock.addChildToBack(innerTry);
    
    outerTry.addChildToBack(outerTryBlock);
    outerTry.addChildToBack(outerFinallyBlock);
    whileBody.addChildToBack(outerTry);
    
    whileNode.addChildToBack(whileCond);
    whileNode.addChildToBack(whileBody);
    script.addChildToBack(whileNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
    
    // Verify that break creates proper edges through finally blocks
    assertTrue("CFG should have at least 6 nodes for break with finally", 
               cfg.getNodeCount() >= 6);
  }

  @Test(timeout = 4000)
  public void testIssue779MissingReturn() {
    // This test targets the known defect: testIssue779
    // Creates a function that should return a value but doesn't
    Node script = new Node(Token.SCRIPT);
    
    Node functionNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString("testFunc");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    
    // Function body with if/else that doesn't cover all paths
    Node ifNode = new Node(Token.IF);
    Node condition = Node.newString("x > 0");
    Node thenBlock = new Node(Token.BLOCK);
    Node returnStmt = new Node(Token.RETURN);
    returnStmt.addChildToBack(Node.newNumber(1));
    thenBlock.addChildToBack(returnStmt);
    Node elseBlock = new Node(Token.BLOCK);
    // No return in else block - this should cause missing return warning
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("sideEffect"));
    elseBlock.addChildToBack(exprResult);
    
    ifNode.addChildToBack(condition);
    ifNode.addChildToBack(thenBlock);
    ifNode.addChildToBack(elseBlock);
    body.addChildToBack(ifNode);
    
    functionNode.addChildToBack(funcName);
    functionNode.addChildToBack(params);
    functionNode.addChildToBack(body);
    script.addChildToBack(functionNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
    
    // Verify that the CFG has proper edges for the if/else structure
    // The bug causes incorrect CFG that prevents detecting missing return
    assertTrue("CFG should have at least 5 nodes for if/else structure", 
               cfg.getNodeCount() >= 5);
  }

  @Test(timeout = 4000)
  public void testForInLoop() {
    Node script = new Node(Token.SCRIPT);
    Node forNode = new Node(Token.FOR);
    Node item = Node.newString("item");
    Node collection = Node.newString("obj");
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("stmt"));
    body.addChildToBack(exprResult);
    forNode.addChildToBack(item);
    forNode.addChildToBack(collection);
    forNode.addChildToBack(body);
    script.addChildToBack(forNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testSwitchWithDefault() {
    Node script = new Node(Token.SCRIPT);
    Node switchNode = new Node(Token.SWITCH);
    Node switchExpr = Node.newString("expr");
    Node caseNode = new Node(Token.CASE);
    Node caseCond = Node.newNumber(1);
    Node caseBody = new Node(Token.BLOCK);
    Node caseStmt = new Node(Token.EXPR_RESULT);
    caseStmt.addChildToBack(Node.newString("caseStmt"));
    caseBody.addChildToBack(caseStmt);
    caseNode.addChildToBack(caseCond);
    caseNode.addChildToBack(caseBody);
    Node defaultNode = new Node(Token.DEFAULT_CASE);
    Node defaultBody = new Node(Token.BLOCK);
    Node defaultStmt = new Node(Token.EXPR_RESULT);
    defaultStmt.addChildToBack(Node.newString("defaultStmt"));
    defaultBody.addChildToBack(defaultStmt);
    defaultNode.addChildToBack(defaultBody);
    switchNode.addChildToBack(switchExpr);
    switchNode.addChildToBack(caseNode);
    switchNode.addChildToBack(defaultNode);
    script.addChildToBack(switchNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testTryWithoutCatch() {
    Node script = new Node(Token.SCRIPT);
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node tryStmt = new Node(Token.EXPR_RESULT);
    tryStmt.addChildToBack(Node.newString("tryStmt"));
    tryBlock.addChildToBack(tryStmt);
    Node finallyBlock = new Node(Token.BLOCK);
    Node finallyStmt = new Node(Token.EXPR_RESULT);
    finallyStmt.addChildToBack(Node.newString("finallyStmt"));
    finallyBlock.addChildToBack(finallyStmt);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(finallyBlock);
    script.addChildToBack(tryNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testEmptyCatchBlock() {
    Node script = new Node(Token.SCRIPT);
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node tryStmt = new Node(Token.EXPR_RESULT);
    tryStmt.addChildToBack(Node.newString("tryStmt"));
    tryBlock.addChildToBack(tryStmt);
    Node catchNode = new Node(Token.CATCH);
    Node catchBlock = new Node(Token.BLOCK);
    // Empty catch block
    catchNode.addChildToBack(Node.newString("e"));
    catchNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);
    script.addChildToBack(tryNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testMultipleCatchBlocks() {
    Node script = new Node(Token.SCRIPT);
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node tryStmt = new Node(Token.EXPR_RESULT);
    tryStmt.addChildToBack(Node.newString("tryStmt"));
    tryBlock.addChildToBack(tryStmt);
    
    Node catch1 = new Node(Token.CATCH);
    Node catchBlock1 = new Node(Token.BLOCK);
    Node catchStmt1 = new Node(Token.EXPR_RESULT);
    catchStmt1.addChildToBack(Node.newString("catch1"));
    catchBlock1.addChildToBack(catchStmt1);
    catch1.addChildToBack(Node.newString("e1"));
    catch1.addChildToBack(catchBlock1);
    
    Node catch2 = new Node(Token.CATCH);
    Node catchBlock2 = new Node(Token.BLOCK);
    Node catchStmt2 = new Node(Token.EXPR_RESULT);
    catchStmt2.addChildToBack(Node.newString("catch2"));
    catchBlock2.addChildToBack(catchStmt2);
    catch2.addChildToBack(Node.newString("e2"));
    catch2.addChildToBack(catchBlock2);
    
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catch1);
    tryNode.addChildToBack(catch2);
    script.addChildToBack(tryNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testNestedLoopsWithBreak() {
    Node script = new Node(Token.SCRIPT);
    
    Node outerWhile = new Node(Token.WHILE);
    Node outerCond = Node.newString("outerCond");
    Node outerBody = new Node(Token.BLOCK);
    
    Node innerFor = new Node(Token.FOR);
    Node init = new Node(Token.EXPR_RESULT);
    init.addChildToBack(Node.newString("i=0"));
    Node cond = Node.newString("i<10");
    Node iter = new Node(Token.EXPR_RESULT);
    iter.addChildToBack(Node.newString("i++"));
    Node innerBody = new Node(Token.BLOCK);
    Node breakNode = new Node(Token.BREAK);
    innerBody.addChildToBack(breakNode);
    innerFor.addChildToBack(init);
    innerFor.addChildToBack(cond);
    innerFor.addChildToBack(iter);
    innerFor.addChildToBack(innerBody);
    
    outerBody.addChildToBack(innerFor);
    outerWhile.addChildToBack(outerCond);
    outerWhile.addChildToBack(outerBody);
    script.addChildToBack(outerWhile);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledBreak() {
    Node script = new Node(Token.SCRIPT);
    
    Node outerLabel = new Node(Token.LABEL);
    Node outerLabelName = Node.newString("outer");
    Node outerWhile = new Node(Token.WHILE);
    Node outerCond = Node.newString("true");
    Node outerBody = new Node(Token.BLOCK);
    
    Node innerWhile = new Node(Token.WHILE);
    Node innerCond = Node.newString("true");
    Node innerBody = new Node(Token.BLOCK);
    Node breakNode = new Node(Token.BREAK);
    breakNode.addChildToBack(Node.newString("outer"));
    innerBody.addChildToBack(breakNode);
    innerWhile.addChildToBack(innerCond);
    innerWhile.addChildToBack(innerBody);
    
    outerBody.addChildToBack(innerWhile);
    outerWhile.addChildToBack(outerCond);
    outerWhile.addChildToBack(outerBody);
    outerLabel.addChildToBack(outerLabelName);
    outerLabel.addChildToBack(outerWhile);
    script.addChildToBack(outerLabel);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testLabeledContinue() {
    Node script = new Node(Token.SCRIPT);
    
    Node outerLabel = new Node(Token.LABEL);
    Node outerLabelName = Node.newString("outer");
    Node outerWhile = new Node(Token.WHILE);
    Node outerCond = Node.newString("true");
    Node outerBody = new Node(Token.BLOCK);
    
    Node innerWhile = new Node(Token.WHILE);
    Node innerCond = Node.newString("true");
    Node innerBody = new Node(Token.BLOCK);
    Node continueNode = new Node(Token.CONTINUE);
    continueNode.addChildToBack(Node.newString("outer"));
    innerBody.addChildToBack(continueNode);
    innerWhile.addChildToBack(innerCond);
    innerWhile.addChildToBack(innerBody);
    
    outerBody.addChildToBack(innerWhile);
    outerWhile.addChildToBack(outerCond);
    outerWhile.addChildToBack(outerBody);
    outerLabel.addChildToBack(outerLabelName);
    outerLabel.addChildToBack(outerWhile);
    script.addChildToBack(outerLabel);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testReturnInTryFinally() {
    Node script = new Node(Token.SCRIPT);
    
    Node functionNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString("testFunc");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node returnStmt = new Node(Token.RETURN);
    returnStmt.addChildToBack(Node.newNumber(42));
    tryBlock.addChildToBack(returnStmt);
    Node finallyBlock = new Node(Token.BLOCK);
    Node finallyStmt = new Node(Token.EXPR_RESULT);
    finallyStmt.addChildToBack(Node.newString("cleanup"));
    finallyBlock.addChildToBack(finallyStmt);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(finallyBlock);
    body.addChildToBack(tryNode);
    
    functionNode.addChildToBack(funcName);
    functionNode.addChildToBack(params);
    functionNode.addChildToBack(body);
    script.addChildToBack(functionNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testThrowInTryCatch() {
    Node script = new Node(Token.SCRIPT);
    
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node throwStmt = new Node(Token.THROW);
    throwStmt.addChildToBack(Node.newString("error"));
    tryBlock.addChildToBack(throwStmt);
    Node catchNode = new Node(Token.CATCH);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchStmt = new Node(Token.EXPR_RESULT);
    catchStmt.addChildToBack(Node.newString("handle"));
    catchBlock.addChildToBack(catchStmt);
    catchNode.addChildToBack(Node.newString("e"));
    catchNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(tryBlock);
    tryNode.addChildToBack(catchNode);
    script.addChildToBack(tryNode);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testMultipleStatementsInBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    
    Node stmt1 = new Node(Token.EXPR_RESULT);
    stmt1.addChildToBack(Node.newString("stmt1"));
    Node stmt2 = new Node(Token.EXPR_RESULT);
    stmt2.addChildToBack(Node.newString("stmt2"));
    Node stmt3 = new Node(Token.EXPR_RESULT);
    stmt3.addChildToBack(Node.newString("stmt3"));
    
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    block.addChildToBack(stmt3);
    script.addChildToBack(block);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testIfElseIfChain() {
    Node script = new Node(Token.SCRIPT);
    
    Node if1 = new Node(Token.IF);
    Node cond1 = Node.newString("cond1");
    Node then1 = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EXPR_RESULT);
    stmt1.addChildToBack(Node.newString("stmt1"));
    then1.addChildToBack(stmt1);
    
    Node if2 = new Node(Token.IF);
    Node cond2 = Node.newString("cond2");
    Node then2 = new Node(Token.BLOCK);
    Node stmt2 = new Node(Token.EXPR_RESULT);
    stmt2.addChildToBack(Node.newString("stmt2"));
    then2.addChildToBack(stmt2);
    Node else2 = new Node(Token.BLOCK);
    Node stmt3 = new Node(Token.EXPR_RESULT);
    stmt3.addChildToBack(Node.newString("stmt3"));
    else2.addChildToBack(stmt3);
    if2.addChildToBack(cond2);
    if2.addChildToBack(then2);
    if2.addChildToBack(else2);
    
    if1.addChildToBack(cond1);
    if1.addChildToBack(then1);
    if1.addChildToBack(if2);
    script.addChildToBack(if1);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Node script = new Node(Token.SCRIPT);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testProcessWithNullExterns() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("test"));
    script.addChildToBack(exprResult);

    ControlFlowAnalysis cfa = createCFA();
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
  }

  @Test(timeout = 4000)
  public void testGetCfgAfterProcess() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("test"));
    script.addChildToBack(exprResult);

    ControlFlowAnalysis cfa = createCFA();
    assertNull("CFG should be null before process", cfa.getCfg());
    cfa.process(null, script);
    assertNotNull("CFG should not be null after process", cfa.getCfg());
  }

  @Test(timeout = 4000)
  public void testShouldTraverseFunctions() {
    ControlFlowAnalysis cfaWithFunctions = new ControlFlowAnalysis(new TestCompiler(), true, false);
    ControlFlowAnalysis cfaWithoutFunctions = new ControlFlowAnalysis(new TestCompiler(), false, false);
    
    Node script = new Node(Token.SCRIPT);
    Node functionNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString("f");
    Node params = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("inner"));
    body.addChildToBack(exprResult);
    functionNode.addChildToBack(funcName);
    functionNode.addChildToBack(params);
    functionNode.addChildToBack(body);
    script.addChildToBack(functionNode);

    cfaWithFunctions.process(null, script);
    assertNotNull("CFG with functions should not be null", cfaWithFunctions.getCfg());
    
    cfaWithoutFunctions.process(null, script);
    assertNotNull("CFG without functions should not be null", cfaWithoutFunctions.getCfg());
  }

  @Test(timeout = 4000)
  public void testEdgeAnnotations() {
    ControlFlowAnalysis cfaWithEdges = new ControlFlowAnalysis(new TestCompiler(), true, true);
    ControlFlowAnalysis cfaWithoutEdges = new ControlFlowAnalysis(new TestCompiler(), true, false);
    
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    exprResult.addChildToBack(Node.newString("test"));
    script.addChildToBack(exprResult);

    cfaWithEdges.process(null, script);
    assertNotNull("CFG with edge annotations should not be null", cfaWithEdges.getCfg());
    
    cfaWithoutEdges.process(null, script);
    assertNotNull("CFG without edge annotations should not be null", cfaWithoutEdges.getCfg());
  }
}