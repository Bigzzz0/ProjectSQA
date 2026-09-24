package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * Test suite for ScopedAliases class targeting maximum coverage and defect detection.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - A1: Normal goog.scope alias processing with valid aliases
 * - A2: Multiple aliases in same scope
 * - A3: Transitive alias resolution (e.g., var g = goog; var d = g.dom)
 * - A4: Type node alias replacement in JSDoc
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - B1: Empty goog.scope block (no aliases)
 * - B2: Single alias definition
 * - B3: Alias with no initial value (non-alias local)
 * - B4: Null preprocessorSymbolTable
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - C1: Non-alias local variable in goog.scope (targeting known defect)
 *   - Branch: n.hasChildren() && n.getFirstChild().isQualifiedName() == false
 *   - Expected: GOOG_SCOPE_NON_ALIAS_LOCAL error reported
 *   - Defect: In defective version, this error is NOT reported when parent.isVar() is true
 *            but the variable has no qualified name initializer
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - D1: goog.scope used improperly (not in expression statement)
 * - D2: goog.scope with bad parameters (no parameter, multiple parameters)
 * - D3: goog.scope with named function instead of anonymous
 * - D4: goog.scope with function that has parameters
 * - D5: 'this' reference inside goog.scope
 * - D6: 'return' statement inside goog.scope
 * - D7: 'throw' statement inside goog.scope
 * - D8: Alias redefinition
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - E1: Multiple goog.scope blocks processed correctly
 * - E2: Nested functions not traversed (shouldTraverse returns false)
 * - E3: Alias usage outside goog.scope depth < 2
 */
public class ScopedAliasesDeepseekTest {

  /**
   * Helper method to create a minimal compiler for testing.
   */
  private AbstractCompiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  /**
   * Helper to create a simple goog.scope AST with a non-alias local variable.
   * This targets the known defect: GOOG_SCOPE_NON_ALIAS_LOCAL should be reported
   * when a var in goog.scope has no qualified name initializer.
   */
  private Node createNonAliasLocalScope() {
    // Build AST for: goog.scope(function() { var x = 5; });
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, "")); // anonymous
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    Node var = new Node(Token.VAR);
    Node name = Node.newString(Token.NAME, "x");
    name.addChildToBack(Node.newNumber(5)); // non-qualified name initializer
    var.addChildToBack(name);
    block.addChildToBack(var);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a valid goog.scope AST with proper aliases.
   */
  private Node createValidAliasScope() {
    // Build AST for: goog.scope(function() { var dom = goog.dom; });
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, "")); // anonymous
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    Node var = new Node(Token.VAR);
    Node name = Node.newString(Token.NAME, "dom");
    Node qualifiedName = new Node(Token.GETPROP);
    qualifiedName.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName.addChildToBack(Node.newString(Token.STRING, "dom"));
    name.addChildToBack(qualifiedName);
    var.addChildToBack(name);
    block.addChildToBack(var);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with 'this' reference.
   */
  private Node createThisReferenceScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    Node thisNode = new Node(Token.THIS);
    block.addChildToBack(thisNode);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with 'return' statement.
   */
  private Node createReturnStatementScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN);
    returnNode.addChildToBack(Node.newNumber(1));
    block.addChildToBack(returnNode);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with 'throw' statement.
   */
  private Node createThrowStatementScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToBack(Node.newString(Token.NAME, "e"));
    block.addChildToBack(throwNode);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with alias redefinition.
   */
  private Node createAliasRedefinitionScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    
    // First definition: var dom = goog.dom;
    Node var1 = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "dom");
    Node qualifiedName1 = new Node(Token.GETPROP);
    qualifiedName1.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName1.addChildToBack(Node.newString(Token.STRING, "dom"));
    name1.addChildToBack(qualifiedName1);
    var1.addChildToBack(name1);
    block.addChildToBack(var1);
    
    // Second definition: dom = goog.window; (redefinition)
    Node assign = new Node(Token.ASSIGN);
    Node name2 = Node.newString(Token.NAME, "dom");
    Node qualifiedName2 = new Node(Token.GETPROP);
    qualifiedName2.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName2.addChildToBack(Node.newString(Token.STRING, "window"));
    assign.addChildToBack(name2);
    assign.addChildToBack(qualifiedName2);
    block.addChildToBack(assign);
    
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with improper usage (not in expression statement).
   */
  private Node createImproperUsageScope() {
    Node script = new Node(Token.SCRIPT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    function.addChildToBack(block);
    call.addChildToBack(function);
    script.addChildToBack(call); // Not wrapped in EXPR_RESULT
    return script;
  }

  /**
   * Helper to create a goog.scope with bad parameters (no parameter).
   */
  private Node createNoParameterScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    // No function parameter
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with named function.
   */
  private Node createNamedFunctionScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, "myFunc")); // named function
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  /**
   * Helper to create a goog.scope with function that has parameters.
   */
  private Node createFunctionWithParamsScope() {
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    paramList.addChildToBack(Node.newString(Token.NAME, "x")); // has parameter
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    return script;
  }

  // ========== Partition A: Core Functional Logic & State Transitions ==========

  @Test(timeout = 4000)
  public void testValidAliasProcessing() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null, 
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createValidAliasScope();
    scopedAliases.process(null, root);
    // No errors should be reported for valid alias
    assertTrue("No errors expected for valid alias", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testEmptyScopeBlock() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    // Create goog.scope(function() { });
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    scopedAliases.process(null, script);
    assertTrue("No errors expected for empty scope", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testTransitiveAlias() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    // Build: goog.scope(function() { var g = goog; var d = g.dom; });
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    
    // var g = goog;
    Node var1 = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "g");
    name1.addChildToBack(Node.newString(Token.NAME, "goog"));
    var1.addChildToBack(name1);
    block.addChildToBack(var1);
    
    // var d = g.dom;
    Node var2 = new Node(Token.VAR);
    Node name2 = Node.newString(Token.NAME, "d");
    Node qualifiedName = new Node(Token.GETPROP);
    qualifiedName.addChildToBack(Node.newString(Token.NAME, "g"));
    qualifiedName.addChildToBack(Node.newString(Token.STRING, "dom"));
    name2.addChildToBack(qualifiedName);
    var2.addChildToBack(name2);
    block.addChildToBack(var2);
    
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    
    scopedAliases.process(null, script);
    assertTrue("No errors expected for transitive alias", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  // ========== Partition B: Boundary Value Analysis & Extremes ==========

  @Test(timeout = 4000)
  public void testNullPreprocessorSymbolTable() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createValidAliasScope();
    scopedAliases.process(null, root);
    assertTrue("Should work with null preprocessorSymbolTable", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testSingleAliasDefinition() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createValidAliasScope();
    scopedAliases.process(null, root);
    assertTrue("No errors for single alias", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  // ========== Partition C: Defect-Targeted Branch Zone ==========

  /**
   * CRITICAL TEST: Targets the known defect where GOOG_SCOPE_NON_ALIAS_LOCAL
   * is not reported when a variable in goog.scope has no qualified name initializer.
   * 
   * The defect is in findAliases() method: when parent.isVar() is true but
   * n.getFirstChild().isQualifiedName() is false (e.g., var x = 5;), the error
   * should be reported but in the defective version it is not.
   */
  @Test(timeout = 4000)
  public void testNonAliasLocal() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createNonAliasLocalScope();
    scopedAliases.process(null, root);
    
    // The defect: In the buggy version, this assertion fails because error count is 0
    // Expected: 1 error (GOOG_SCOPE_NON_ALIAS_LOCAL)
    assertEquals("There should be one error for non-alias local variable", 
        1, compiler.getErrorManager().getErrorCount());
    
    // Verify the error is the correct type
    JSError[] errors = compiler.getErrorManager().getErrors();
    if (errors.length > 0) {
      assertEquals("Error should be GOOG_SCOPE_NON_ALIAS_LOCAL",
          "JSC_GOOG_SCOPE_NON_ALIAS_LOCAL", errors[0].getType().key);
    }
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000)
  public void testImproperUsage() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createImproperUsageScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_USED_IMPROPERLY", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testNoParameter() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createNoParameterScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_HAS_BAD_PARAMETERS", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testNamedFunction() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createNamedFunctionScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for named function", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testFunctionWithParams() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createFunctionWithParamsScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for function with params", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testThisReference() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createThisReferenceScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_REFERENCES_THIS", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testReturnStatement() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createReturnStatementScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_USES_RETURN", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testThrowStatement() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createThrowStatementScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_USES_THROW", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  @Test(timeout = 4000)
  public void testAliasRedefinition() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    Node root = createAliasRedefinitionScope();
    scopedAliases.process(null, root);
    assertTrue("Should report GOOG_SCOPE_ALIAS_REDEFINED", 
        compiler.getErrorManager().getErrorCount() > 0);
  }

  // ========== Partition E: Object Lifecycle & Contract Integrity ==========

  @Test(timeout = 4000)
  public void testMultipleScopeBlocks() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    
    // Create two goog.scope blocks
    Node script = new Node(Token.SCRIPT);
    
    // First scope
    Node exprResult1 = new Node(Token.EXPR_RESULT);
    Node call1 = new Node(Token.CALL);
    Node getProp1 = new Node(Token.GETPROP);
    getProp1.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp1.addChildToBack(Node.newString(Token.STRING, "scope"));
    call1.addChildToBack(getProp1);
    Node function1 = new Node(Token.FUNCTION);
    function1.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList1 = new Node(Token.PARAM_LIST);
    function1.addChildToBack(paramList1);
    Node block1 = new Node(Token.BLOCK);
    Node var1 = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "dom");
    Node qualifiedName1 = new Node(Token.GETPROP);
    qualifiedName1.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName1.addChildToBack(Node.newString(Token.STRING, "dom"));
    name1.addChildToBack(qualifiedName1);
    var1.addChildToBack(name1);
    block1.addChildToBack(var1);
    function1.addChildToBack(block1);
    call1.addChildToBack(function1);
    exprResult1.addChildToBack(call1);
    script.addChildToBack(exprResult1);
    
    // Second scope
    Node exprResult2 = new Node(Token.EXPR_RESULT);
    Node call2 = new Node(Token.CALL);
    Node getProp2 = new Node(Token.GETPROP);
    getProp2.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp2.addChildToBack(Node.newString(Token.STRING, "scope"));
    call2.addChildToBack(getProp2);
    Node function2 = new Node(Token.FUNCTION);
    function2.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList2 = new Node(Token.PARAM_LIST);
    function2.addChildToBack(paramList2);
    Node block2 = new Node(Token.BLOCK);
    Node var2 = new Node(Token.VAR);
    Node name2 = Node.newString(Token.NAME, "window");
    Node qualifiedName2 = new Node(Token.GETPROP);
    qualifiedName2.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName2.addChildToBack(Node.newString(Token.STRING, "window"));
    name2.addChildToBack(qualifiedName2);
    var2.addChildToBack(name2);
    block2.addChildToBack(var2);
    function2.addChildToBack(block2);
    call2.addChildToBack(function2);
    exprResult2.addChildToBack(call2);
    script.addChildToBack(exprResult2);
    
    scopedAliases.process(null, script);
    assertTrue("No errors expected for multiple valid scopes", 
        compiler.getErrorManager().getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionNotTraversed() {
    AbstractCompiler compiler = createCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
        CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    
    // Create goog.scope with nested function (should not be traversed)
    Node script = new Node(Token.SCRIPT);
    Node exprResult = new Node(Token.EXPR_RESULT);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "scope"));
    call.addChildToBack(getProp);
    Node function = new Node(Token.FUNCTION);
    function.addChildToBack(Node.newString(Token.NAME, ""));
    Node paramList = new Node(Token.PARAM_LIST);
    function.addChildToBack(paramList);
    Node block = new Node(Token.BLOCK);
    
    // var dom = goog.dom;
    Node var = new Node(Token.VAR);
    Node name = Node.newString(Token.NAME, "dom");
    Node qualifiedName = new Node(Token.GETPROP);
    qualifiedName.addChildToBack(Node.newString(Token.NAME, "goog"));
    qualifiedName.addChildToBack(Node.newString(Token.STRING, "dom"));
    name.addChildToBack(qualifiedName);
    var.addChildToBack(name);
    block.addChildToBack(var);
    
    // Nested function (should not be traversed)
    Node nestedFunction = new Node(Token.FUNCTION);
    nestedFunction.addChildToBack(Node.newString(Token.NAME, "inner"));
    Node nestedParamList = new Node(Token.PARAM_LIST);
    nestedFunction.addChildToBack(nestedParamList);
    Node nestedBlock = new Node(Token.BLOCK);
    nestedFunction.addChildToBack(nestedBlock);
    block.addChildToBack(nestedFunction);
    
    function.addChildToBack(block);
    call.addChildToBack(function);
    exprResult.addChildToBack(call);
    script.addChildToBack(exprResult);
    
    scopedAliases.process(null, script);
    assertTrue("No errors expected for nested function", 
        compiler.getErrorManager().getErrorCount() == 0);
  }
}