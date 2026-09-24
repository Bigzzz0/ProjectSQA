package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * ReferenceCollectingCallbackDeepseekTest - White‑box JUnit4 test suite.
 *
 * [Branch & Defect Analysis Matrix]
 * Targets:
 *  - ReferenceCollection logic: isAssignedOnceInLifetime, isWellDefined,
 *    isEscaped, getInitializingReference, getInitializingReferenceForConstants,
 *    isNeverAssigned, firstReferenceIsAssigningDeclaration.
 *  - Reference predicate methods: isDeclaration, isVarDeclaration,
 *    isInitializingDeclaration, isSimpleAssignmentToName, isLvalue,
 *    getAssignedValue.
 *  - BasicBlock.provablyExecutesBefore, hoisting detection.
 *  - Callback traversal: process, visit, enterScope, exitScope, shouldTraverse.
 *
 * Defect targeted: isAssignedOnceInLifetime returns true for an assignment
 * inside a loop, causing erroneous inlining. (InlineVariablesTest failure)
 */
public class ReferenceCollectingCallbackDeepseekTest {

  private Compiler compiler;
  private Node root;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
  }

  // ----------------------------------------------------------------------
  // Helper: parse code and run ReferenceCollectingCallback, then obtain
  // the ReferenceCollection for the given variable name.
  // ----------------------------------------------------------------------
  private ReferenceCollection getReferencesForVar(String code, String var) {
    root = compiler.parseSyntheticCode(code);
    ReferenceCollectingCallback cb = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    cb.process(null, root);
    Scope topScope = compiler.getTopScope();
    Var v = topScope.getVar(var);
    assertNotNull("Variable '" + var + "' should be declared", v);
    return cb.getReferenceCollection(v);
  }

  // ----------------------------------------------------------------------
  // Helper: construct a Reference for an assignment "name = value" with the
  // given BasicBlock, using lightweight Rhino nodes.
  // ----------------------------------------------------------------------
  private Reference makeAssignmentRef(String name, BasicBlock block) {
    Node nameNode = Node.newString(Token.NAME, name);
    Node value = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, nameNode, value);
    Node blockNode = new Node(Token.BLOCK);
    // Create a simple scope (minimal for Reference constructor)
    Scope scope = new Scope(blockNode, null); // top scope
    return new Reference(nameNode, assign, blockNode, block, scope, "test.js");
  }

  // ----------------------------------------------------------------------
  // Test methods
  // ----------------------------------------------------------------------

  // --- Defect-targeted test: assignment inside a loop -------------------
  @Test(timeout = 4000)
  public void testIsAssignedOnceInLifetime_WithLoopAssignment_ReturnsFalse() {
    // This is the known defect: isAssignedOnceInLifetime incorrectly returns
    // true when the only assignment resides inside a loop.
    ReferenceCollection refs = getReferencesForVar(
        "var x; for(var i=0;i<10;i++){ x = i; }", "x");
    assertFalse("Assignment inside a loop must not be considered assigned once",
        refs.isAssignedOnceInLifetime());
  }

  // --- Core functional tests --------------------------------------------
  @Test(timeout = 4000)
  public void testIsAssignedOnceInLifetime_NoLoop_ReturnsTrue() {
    ReferenceCollection refs = getReferencesForVar("var x; x = 1;", "x");
    assertTrue("Single assignment outside loop", refs.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testIsAssignedOnceInLifetime_MultipleAssignments_ReturnsFalse() {
    ReferenceCollection refs = getReferencesForVar("var x; x=1; x=2;", "x");
    assertFalse("Multiple assignments", refs.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testIsNeverAssigned_NoAssignment_ReturnsTrue() {
    ReferenceCollection refs = getReferencesForVar("var x;", "x");
    assertTrue("Never assigned", refs.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testIsNeverAssigned_HasAssignment_ReturnsFalse() {
    ReferenceCollection refs = getReferencesForVar("var x; x=1;", "x");
    assertFalse("Has assignment", refs.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testGetInitializingReference_DeclarationOnly() {
    ReferenceCollection refs = getReferencesForVar("var x = 1; x;", "x");
    Reference init = refs.getInitializingReference();
    assertNotNull(init);
    assertTrue("Initializing declaration at index 0", init.isInitializingDeclaration());
  }

  @Test(timeout = 4000)
  public void testGetInitializingReference_AssignmentAfterDeclaration() {
    ReferenceCollection refs = getReferencesForVar("var x; x = 1; x;", "x");
    Reference init = refs.getInitializingReference();
    assertNotNull(init);
    assertTrue("Assignment after declaration", init.isSimpleAssignmentToName());
  }

  @Test(timeout = 4000)
  public void testGetInitializingReference_NoInitializer() {
    ReferenceCollection refs = getReferencesForVar("var x; x;", "x");
    assertNull("No initializing reference", refs.getInitializingReference());
  }

  @Test(timeout = 4000)
  public void testGetInitializingReferenceForConstants_LateInitialization() {
    ReferenceCollection refs = getReferencesForVar(
        "var x; use(x); x = 1;", "x"); // synthetic 'use' call
    Reference init = refs.getInitializingReferenceForConstants();
    assertNotNull("Constants can have late initialization", init);
  }

  @Test(timeout = 4000)
  public void testFirstReferenceIsAssigningDeclaration_WithInit() {
    ReferenceCollection refs = getReferencesForVar("var x = 1;", "x");
    assertTrue("First reference is assigning declaration", refs.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testFirstReferenceIsAssigningDeclaration_WithoutInit() {
    ReferenceCollection refs = getReferencesForVar("var x; x = 1;", "x");
    assertFalse("First reference is not assigning declaration", refs.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testIsEscaped_InnerScopeReference() {
    ReferenceCollection refs = getReferencesForVar(
        "var x; function f() { x = 1; }", "x");
    assertTrue("Variable escaped into inner scope", refs.isEscaped());
  }

  @Test(timeout = 4000)
  public void testIsEscaped_NoInnerScopeReference() {
    ReferenceCollection refs = getReferencesForVar("var x; x = 1;", "x");
    assertFalse("Variable not escaped", refs.isEscaped());
  }

  @Test(timeout = 4000)
  public void testIsWellDefined_WithInitializer() {
    ReferenceCollection refs = getReferencesForVar("var x = 1; x;", "x");
    assertTrue("Variable well-defined", refs.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testIsWellDefined_WithoutInitializer() {
    ReferenceCollection refs = getReferencesForVar("var x; x;", "x");
    assertFalse("Variable not well-defined", refs.isWellDefined());
  }

  // --- Reference predicate methods --------------------------------------
  @Test(timeout = 4000)
  public void testReferenceIsDeclaration() {
    Node name = Node.newString(Token.NAME, "a");
    Node var = new Node(Token.VAR, name);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, var, script, block, scope, "test.js");
    assertTrue(ref.isDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsVarDeclaration() {
    Node name = Node.newString(Token.NAME, "a");
    Node var = new Node(Token.VAR, name);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, var, script, block, scope, "test.js");
    assertTrue(ref.isVarDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsInitializingDeclaration_WithValue() {
    Node name = Node.newString(Token.NAME, "a");
    Node value = Node.newNumber(1);
    Node var = new Node(Token.VAR, name, value);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, var, script, block, scope, "test.js");
    assertTrue(ref.isInitializingDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsInitializingDeclaration_WithoutValue() {
    Node name = Node.newString(Token.NAME, "a");
    Node var = new Node(Token.VAR, name);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, var, script, block, scope, "test.js");
    assertFalse(ref.isInitializingDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsSimpleAssignmentToName() {
    Node name = Node.newString(Token.NAME, "a");
    Node value = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, name, value);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, assign, script, block, scope, "test.js");
    assertTrue(ref.isSimpleAssignmentToName());
  }

  @Test(timeout = 4000)
  public void testReferenceIsSimpleAssignmentToName_Compound() {
    Node name = Node.newString(Token.NAME, "a");
    Node value = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN_ADD, name, value); // a += 1
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, assign, script, block, scope, "test.js");
    assertFalse(ref.isSimpleAssignmentToName());
  }

  @Test(timeout = 4000)
  public void testReferenceIsLvalue() {
    Node name = Node.newString(Token.NAME, "a");
    Node value = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, name, value);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, assign, script, block, scope, "test.js");
    assertTrue("Assignment is an lvalue", ref.isLvalue());
  }

  @Test(timeout = 4000)
  public void testReferenceNotLvalue_Read() {
    Node name = Node.newString(Token.NAME, "a");
    Node expr = new Node(Token.EXPR_RESULT, name);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, expr, script, block, scope, "test.js");
    assertFalse("Read is not an lvalue", ref.isLvalue());
  }

  @Test(timeout = 4000)
  public void testReferenceGetAssignedValue_Assignment() {
    Node name = Node.newString(Token.NAME, "a");
    Node value = Node.newNumber(42);
    Node assign = new Node(Token.ASSIGN, name, value);
    Node script = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, script);
    Scope scope = new Scope(script, null);
    Reference ref = new Reference(name, assign, script, block, scope, "test.js");
    Node assignedValue = ref.getAssignedValue();
    assertNotNull(assignedValue);
    assertEquals(Token.NUMBER, assignedValue.getType());
    assertEquals(42.0, assignedValue.getDouble(), 0.0);
  }

  // --- BasicBlock.provablyExecutesBefore --------------------------------
  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBefore_SameBlock() {
    Node rootNode = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, rootNode);
    assertTrue("Block provably executes before itself", block.provablyExecutesBefore(block));
  }

  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBefore_Ancestor() {
    Node rootNode = new Node(Token.SCRIPT);
    BasicBlock parent = new BasicBlock(null, rootNode);
    BasicBlock child = new BasicBlock(parent, new Node(Token.BLOCK));
    assertTrue("Parent block executes before child", parent.provablyExecutesBefore(child));
  }

  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBefore_Unrelated() {
    Node rootNode1 = new Node(Token.SCRIPT);
    Node rootNode2 = new Node(Token.SCRIPT);
    BasicBlock block1 = new BasicBlock(null, rootNode1);
    BasicBlock block2 = new BasicBlock(null, rootNode2);
    assertFalse("Unrelated blocks", block1.provablyExecutesBefore(block2));
  }

  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBefore_Hoisted() {
    Node rootNode = new Node(Token.SCRIPT);
    BasicBlock parent = new BasicBlock(null, rootNode);
    // Create a hoisted block (root is a function declaration)
    Node funcExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"));
    Node funcBlock = new Node(Token.BLOCK);
    funcExpr.addChildToBack(funcBlock);
    BasicBlock hoisted = new BasicBlock(parent, funcExpr);
    // descendant of hoisted
    BasicBlock descendant = new BasicBlock(hoisted, new Node(Token.BLOCK));
    assertFalse("Hoisted block prevents execution guarantee",
        parent.provablyExecutesBefore(descendant));
  }

  // --- Callback traversal behavior --------------------------------------
  @Test(timeout = 4000)
  public void testShouldTraverse_PushesBlockForNewBlock() {
    Node parent = new Node(Token.SCRIPT);
    Node ifNode = new Node(Token.IF, new Node(Token.NAME, "cond"),
        new Node(Token.BLOCK));
    parent.addChildToBack(ifNode);
    ReferenceCollectingCallback cb = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    // We'll test shouldTraverse indirectly by invoking process and checking refs.
    // We'll just ensure the traversal doesn't throw and refs exist.
    ReferenceCollection refs = getReferencesForVar("var a; if(a){ a=1; }", "a");
    assertNotNull(refs);
    assertTrue("References added for simple if", refs.references.size() >= 1);
  }

  @Test(timeout = 4000)
  public void testExitScope_CallsBehavior() {
    final List<String> calls = new ArrayList<>();
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t,
          java.util.Map<ReferenceCollectingCallback.Var,
              ReferenceCollection> referenceMap) {
        calls.add("afterExitScope");
      }
    };
    Compiler localCompiler = new Compiler();
    localCompiler.initOptions(new CompilerOptions());
    Node localRoot = localCompiler.parseSyntheticCode("var x;");
    ReferenceCollectingCallback cb = new ReferenceCollectingCallback(localCompiler, behavior);
    cb.process(null, localRoot);
    assertEquals("Behavior afterExitScope should be called once", 1, calls.size());
  }

  @Test(timeout = 4000)
  public void testGetReferenceCollection_ReturnsNonNull() {
    ReferenceCollection refs = getReferencesForVar("var a;", "a");
    assertNotNull("ReferenceCollection should exist", refs);
  }

  // --- Var filter constructor -------------------------------------------
  @Test(timeout = 4000)
  public void testVarFilter_ExcludesVariable() {
    Predicate<ReferenceCollectingCallback.Var> filter = new Predicate<ReferenceCollectingCallback.Var>() {
      @Override
      public boolean apply(ReferenceCollectingCallback.Var var) {
        return !var.getName().equals("hidden");
      }
    };
    Compiler localCompiler = new Compiler();
    localCompiler.initOptions(new CompilerOptions());
    Node localRoot = localCompiler.parseSyntheticCode("var visible; var hidden; hidden = 1;");
    ReferenceCollectingCallback cb = new ReferenceCollectingCallback(
        localCompiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);
    cb.process(null, localRoot);
    Scope scope = localCompiler.getTopScope();
    Var visibleVar = scope.getVar("visible");
    ReferenceCollection visibleRefs = cb.getReferenceCollection(visibleVar);
    assertNotNull(visibleRefs);
    Var hiddenVar = scope.getVar("hidden");
    assertNull("Filtered variable should not have references", cb.getReferenceCollection(hiddenVar));
  }

  // ----------------------------------------------------------------------
  // Additional unit test for the defect using manual construction
  // ----------------------------------------------------------------------
  @Test(timeout = 4000)
  public void testIsAssignedOnceInLifetime_ManualLoopBlock_FailsOnBug() {
    // Build a BasicBlock tree that mimics a loop body.
    Node rootScript = new Node(Token.SCRIPT);
    BasicBlock outerBlock = new BasicBlock(null, rootScript);
    Node loopNode = new Node(Token.FOR, new Node(Token.BLOCK)); // dummy loop
    BasicBlock loopBodyBlock = new BasicBlock(outerBlock, loopNode);
    // Create an assignment reference inside the loop body.
    Reference assignRef = makeAssignmentRef("x", loopBodyBlock);
    ReferenceCollection coll = new ReferenceCollection();
    coll.add(assignRef, null, null);
    assertFalse("Assignment in loop body must not be assigned once",
        coll.isAssignedOnceInLifetime());
  }
}