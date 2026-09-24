package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.PrepareAst
 * Defect Reference: Defects4J / Closure Issue 937 (testIssue937)
 *
 * Branches & Logic Targeted:
 * 1. Constructor Coverage:
 *    - PrepareAst(AbstractCompiler) -> default checkOnly = false
 *    - PrepareAst(AbstractCompiler, boolean) -> parameterized checkOnly
 *
 * 2. process(Node externs, Node root):
 *    - Branch checkOnly == true  -> normalizeNodeTypes(root)
 *    - Branch checkOnly == false -> externs traversal, root traversal
 *    - BVA Null Handling: externs == null, root == null, externs != null, root != null
 *
 * 3. normalizeNodeTypes(Node n) & reportChange():
 *    - Preconditions.checkState(child.getParent() == n) -> valid parent pointers vs violated pointer
 *    - Recursive traversal on children
 *    - reportChange() when checkOnly == true -> throws IllegalStateException("normalizeNodeType constraints violated")
 *    - reportChange() when checkOnly == false -> no-op
 *
 * 4. normalizeBlocks(Node n):
 *    - NodeUtil.isControlStructure(n) -> false (skipped) vs true
 *    - Exclusions: n.isLabel() == true (skipped), n.isSwitch() == true (skipped)
 *    - NodeUtil.isControlStructureCodeBlock(n, c):
 *        - c.isBlock() == true -> no-op
 *        - c.isBlock() == false -> wraps in new block, replaces child, invokes reportChange()
 *        - Sub-branch !c.isEmpty() -> newBlock.addChildrenToFront(c)
 *        - Sub-branch c.isEmpty()  -> newBlock.setWasEmptyNode(true)
 *    - Control structures tested: IF, WHILE, DO, FOR
 *
 * 5. PrepareAnnotations.shouldTraverse(NodeTraversal, Node, Node):
 *    - n.isObjectLit() == true  -> normalizeObjectLiteralAnnotations(n)
 *    - n.isObjectLit() == false -> returns true directly
 *
 * 6. PrepareAnnotations.visit(NodeTraversal, Node, Node):
 *    - switch (n.getType()):
 *        - Token.CALL     -> annotateCalls(n)
 *        - Token.FUNCTION -> annotateDispatchers(n, parent)
 *        - Default tokens -> no-op
 *
 * 7. annotateCalls(Node n):
 *    - !NodeUtil.isGet(first) == true  -> sets Node.FREE_CALL prop
 *    - !NodeUtil.isGet(first) == false (GETPROP, GETELEM) -> does not set Node.FREE_CALL
 *    - first.isName() && "eval".equals(first.getString()) -> sets Node.DIRECT_EVAL prop
 *    - Non-eval names or non-name call targets -> DIRECT_EVAL remains false
 *
 * 8. annotateDispatchers(Node n, Node parent):
 *    - parent.getJSDocInfo() == null -> skipped
 *    - parent.getJSDocInfo() != null && !isJavaDispatch() -> skipped
 *    - parent.getJSDocInfo() != null && isJavaDispatch():
 *        - parent.isAssign() == true:
 *            - parent.getLastChild() == n -> sets Node.IS_DISPATCHER
 *            - parent.getLastChild() != n -> throws IllegalStateException
 *        - parent.isAssign() == false -> skipped
 *
 * 9. normalizeObjectLiteralKeyAnnotations(Node objlit, Node key, Node value) [CRITICAL DEFECT ZONE]:
 *    - key.getJSDocInfo() != null && value.isFunction():
 *        - Fixed: value != null && value.isFunction() copies JSDocInfo to function value
 *        - DEFECT TARGET (Issue 937): key.getJSDocInfo() != null && value == null
 *          In defective version: throws NullPointerException on value.isFunction()!
 *          In correct version: safe evaluation without NPE
 *    - key.getJSDocInfo() == null -> skipped
 *    - value is non-function -> value JSDoc remains untouched
 */

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrepareAstGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAnnotateCalls_freeCall() {
    Compiler compiler = new Compiler();
    Node target = IR.name("myFunction");
    Node call = IR.call(target, IR.string("arg"));
    Node root = IR.script(IR.exprResult(call));

    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertTrue("A call without explicit 'this' must be marked as FREE_CALL",
        call.getBooleanProp(Node.FREE_CALL));
    assertFalse("Direct eval should not be set for arbitrary function",
        target.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test(timeout = 4000)
  public void testAnnotateCalls_directEval() {
    Compiler compiler = new Compiler();
    Node target = IR.name("eval");
    Node call = IR.call(target, IR.string("alert(1)"));
    Node root = IR.script(IR.exprResult(call));

    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertTrue("eval() call should be marked as FREE_CALL",
        call.getBooleanProp(Node.FREE_CALL));
    assertTrue("Target of eval() call must be marked as DIRECT_EVAL",
        target.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test(timeout = 4000)
  public void testAnnotateCalls_propertyCallNotFreeCall() {
    Compiler compiler = new Compiler();
    Node getprop = IR.getprop(IR.name("window"), IR.string("eval"));
    Node call = IR.call(getprop, IR.string("alert(1)"));
    Node root = IR.script(IR.exprResult(call));

    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertFalse("Method call on property should not be FREE_CALL",
        call.getBooleanProp(Node.FREE_CALL));
    assertFalse("window.eval is not a DIRECT_EVAL",
        getprop.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test(timeout = 4000)
  public void testAnnotateCalls_elementCallNotFreeCall() {
    Compiler compiler = new Compiler();
    Node getelem = IR.getelem(IR.name("obj"), IR.string("fn"));
    Node call = IR.call(getelem);
    Node root = IR.script(IR.exprResult(call));

    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertFalse("Method call via getelem should not be FREE_CALL",
        call.getBooleanProp(Node.FREE_CALL));
  }

  @Test(timeout = 4000)
  public void testAnnotateCalls_commaExpressionEvalNotDirectEval() {
    Compiler compiler = new Compiler();
    // AST representation of: (0, eval)("x")
    Node comma = new Node(Token.COMMA, IR.number(0), IR.name("eval"));
    Node call = IR.call(comma, IR.string("x"));
    Node root = IR.script(IR.exprResult(call));

    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertTrue("Call to comma expression is a FREE_CALL",
        call.getBooleanProp(Node.FREE_CALL));
    assertFalse("(0, eval) should not be flagged as DIRECT_EVAL",
        comma.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test(timeout = 4000)
  public void testNormalizeObjectLiteralKeyAnnotations_functionValueCopiesDoc() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node key = IR.string("myMethod");
    JSDocInfo info = new JSDocInfo();
    key.setJSDocInfo(info);
    Node fn = IR.function(IR.name(""), IR.paramList(), IR.block());
    key.addChildToBack(fn);
    objlit.addChildToBack(key);

    Node root = IR.script(IR.exprResult(objlit));
    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertSame("Function value child must receive key's JSDocInfo",
        info, fn.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testNormalizeObjectLiteralKeyAnnotations_nonFunctionValueIgnoresDoc() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node key = IR.string("prop");
    JSDocInfo info = new JSDocInfo();
    key.setJSDocInfo(info);
    Node num = IR.number(42);
    key.addChildToBack(num);
    objlit.addChildToBack(key);

    Node root = IR.script(IR.exprResult(objlit));
    PrepareAst preparer = new PrepareAst(compiler);
    preparer.process(null, root);

    assertNull("Non-function value child should not receive JSDocInfo",
        num.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testJavaDispatchAnnotationOnFunction() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @javadispatch */ x = function() {};");
    assertNotNull("Parsed AST should not be null", root);

    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    Node exprResult = root.getFirstChild();
    Node assign = exprResult.getFirstChild();
    Node fn = assign.getLastChild();

    assertTrue("Target child should be a function", fn.isFunction());
    assertTrue("Java dispatch function should have IS_DISPATCHER property set",
        fn.getBooleanProp(Node.IS_DISPATCHER));
  }

  @Test(timeout = 4000)
  public void testJavaDispatchNonAssignParentDoesNotSetDispatcher() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @javadispatch */ var x = function() {};");
    assertNotNull(root);

    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    Node var = root.getFirstChild();
    Node name = var.getFirstChild();
    Node fn = name.getFirstChild();

    assertTrue(fn.isFunction());
    assertFalse("Dispatcher should not be set when parent is not an assign node",
        fn.getBooleanProp(Node.IS_DISPATCHER));
  }

  @Test(timeout = 4000)
  public void testCheckOnlyWithValidBlocksPasses() {
    Compiler compiler = new Compiler();
    Node cond = IR.name("cond");
    Node block = IR.block(IR.exprResult(IR.name("x")));
    Node ifNode = IR.ifNode(cond, block);
    Node script = IR.script(ifNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);

    assertTrue("Valid block in ifNode should remain a block",
        ifNode.getFirstChild().getNext().isBlock());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcess_nullExternsAndNullRoot() {
    Compiler compiler = new Compiler();
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, null);
  }

  @Test(timeout = 4000)
  public void testProcess_externsOnly() {
    Compiler compiler = new Compiler();
    Node externs = IR.script(IR.exprResult(IR.call(IR.name("extFn"))));
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(externs, null);

    Node call = externs.getFirstChild().getFirstChild();
    assertTrue("Extern call should be annotated", call.getBooleanProp(Node.FREE_CALL));
  }

  @Test(timeout = 4000)
  public void testProcess_rootOnly() {
    Compiler compiler = new Compiler();
    Node root = IR.script(IR.exprResult(IR.call(IR.name("rootFn"))));
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    Node call = root.getFirstChild().getFirstChild();
    assertTrue("Root call should be annotated", call.getBooleanProp(Node.FREE_CALL));
  }

  @Test(timeout = 4000)
  public void testProcess_bothExternsAndRoot() {
    Compiler compiler = new Compiler();
    Node externs = IR.script(IR.exprResult(IR.call(IR.name("extFn"))));
    Node root = IR.script(IR.exprResult(IR.call(IR.name("rootFn"))));
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(externs, root);

    assertTrue(externs.getFirstChild().getFirstChild().getBooleanProp(Node.FREE_CALL));
    assertTrue(root.getFirstChild().getFirstChild().getBooleanProp(Node.FREE_CALL));
  }

  @Test(timeout = 4000)
  public void testEmptyObjectLiteralNoOp() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node root = IR.script(IR.exprResult(objlit));

    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    assertFalse("Object literal should remain empty", objlit.hasChildren());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyWithoutDoc() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node key = IR.string("m");
    Node fn = IR.function(IR.name(""), IR.paramList(), IR.block());
    key.addChildToBack(fn);
    objlit.addChildToBack(key);

    Node root = IR.script(IR.exprResult(objlit));
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    assertNull("Function child should not have JSDocInfo when key has none",
        fn.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyWithoutDocAndNullValue() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node key = IR.string("m"); // no doc, no children
    objlit.addChildToBack(key);

    Node root = IR.script(IR.exprResult(objlit));
    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    assertNull(key.getJSDocInfo());
    assertNull(key.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testCheckOnlyIgnoresExterns() {
    Compiler compiler = new Compiler();
    // Malformed externs that would fail parent check if processed
    Node externs = IR.script();
    Node badChild = IR.block();
    externs.addChildToBack(badChild);
    badChild.setParent(IR.block());

    Node root = IR.script(IR.block());
    PrepareAst preparer = new PrepareAst(compiler, true);
    // externs is ignored when checkOnly is true, so no exception from externs
    preparer.process(externs, root);
  }

  @Test(timeout = 4000)
  public void testNormalizeBlocksExemptions_labelAndSwitch() {
    Compiler compiler = new Compiler();
    Node labelNode = new Node(Token.LABEL, IR.name("lbl"), IR.exprResult(IR.name("x")));
    Node switchNode = new Node(Token.SWITCH, IR.name("val"));
    Node script = IR.script(labelNode, switchNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    // Labels and Switches are control structures but excluded from block normalization
    preparer.process(null, script);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 937 / testIssue937)
  // =========================================================================

  /**
   * Targets the defect where an Object Literal key has attached JSDocInfo but
   * has no value child (i.e. value == null, such as in IDE mode or malformed AST).
   *
   * In defective PrepareAst:
   *   normalizeObjectLiteralKeyAnnotations calls value.isFunction() unconditionally
   *   when key.getJSDocInfo() != null, throwing NullPointerException.
   *
   * In corrected PrepareAst:
   *   Guarded with value != null && value.isFunction(), successfully completing.
   */
  @Test(timeout = 4000)
  public void testIssue937_objectLiteralKeyWithDocAndNullValueDoesNotThrowNpe() {
    Compiler compiler = new Compiler();
    Node objlit = IR.objectlit();
    Node key = IR.string("annotatedKeyWithoutValue");
    JSDocInfo docInfo = new JSDocInfo();
    key.setJSDocInfo(docInfo);
    // Key has NO child nodes (value is null)
    objlit.addChildToBack(key);

    Node root = IR.script(IR.exprResult(objlit));

    PrepareAst preparer = new PrepareAst(compiler, false);
    preparer.process(null, root);

    assertSame("Key's JSDocInfo should remain intact", docInfo, key.getJSDocInfo());
    assertNull("Key must still have no value child", key.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testIssue937_directPrepareAnnotationsTraversalWithNullValue() {
    Node objlit = IR.objectlit();
    Node key = IR.string("prop");
    key.setJSDocInfo(new JSDocInfo());
    objlit.addChildToBack(key);

    PrepareAst.PrepareAnnotations annotations = new PrepareAst.PrepareAnnotations();
    boolean shouldTraverse = annotations.shouldTraverse(null, objlit, null);

    assertTrue("shouldTraverse must return true", shouldTraverse);
    assertNotNull("Key should retain its JSDocInfo", key.getJSDocInfo());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCheckOnlyViolatedThrows_ifWithoutBlock() {
    Compiler compiler = new Compiler();
    Node cond = IR.name("cond");
    Node thenStmt = IR.exprResult(IR.name("x")); // Not a BLOCK!
    Node ifNode = IR.ifNode(cond, thenStmt);
    Node script = IR.script(ifNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCheckOnlyViolatedThrows_ifWithEmptyElse() {
    Compiler compiler = new Compiler();
    Node cond = IR.name("cond");
    Node thenBlock = IR.block(IR.exprResult(IR.name("x")));
    Node emptyElse = IR.empty(); // Not a BLOCK, isEmpty() == true
    Node ifNode = IR.ifNode(cond, thenBlock, emptyElse);
    Node script = IR.script(ifNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCheckOnlyViolatedThrows_whileWithoutBlock() {
    Compiler compiler = new Compiler();
    Node whileNode = IR.whileNode(IR.name("cond"), IR.exprResult(IR.name("x")));
    Node script = IR.script(whileNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCheckOnlyViolatedThrows_doWithoutBlock() {
    Compiler compiler = new Compiler();
    Node doNode = IR.doNode(IR.exprResult(IR.name("x")), IR.name("cond"));
    Node script = IR.script(doNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCheckOnlyViolatedThrows_forWithoutBlock() {
    Compiler compiler = new Compiler();
    Node forNode = IR.forNode(
        IR.var(IR.name("i")),
        IR.name("cond"),
        IR.name("inc"),
        IR.exprResult(IR.name("x"))); // Body not a BLOCK
    Node script = IR.script(forNode);

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNormalizeNodeTypesParentCheckFails() {
    Compiler compiler = new Compiler();
    Node script = IR.script();
    Node child = IR.block();
    script.addChildToBack(child);
    child.setParent(IR.block()); // Corrupt parent link

    PrepareAst preparer = new PrepareAst(compiler, true);
    preparer.process(null, script);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testJavaDispatchInvalidAssignOrderThrows() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @javadispatch */ x = function() {};");
    Node assign = root.getFirstChild().getFirstChild();
    JSDocInfo dispatchInfo = assign.getJSDocInfo();
    assertNotNull(dispatchInfo);
    assertTrue(dispatchInfo.isJavaDispatch());

    Node fn = IR.function(IR.name(""), IR.paramList(), IR.block());
    // Create invalid assign where function is first child instead of last child
    Node badAssign = IR.assign(fn, IR.name("x"));
    badAssign.setJSDocInfo(dispatchInfo);

    PrepareAst.PrepareAnnotations annotations = new PrepareAst.PrepareAnnotations();
    annotations.visit(null, fn, badAssign);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorVariations() {
    Compiler compiler = new Compiler();
    PrepareAst preparerDefault = new PrepareAst(compiler);
    assertNotNull(preparerDefault);

    PrepareAst preparerFalse = new PrepareAst(compiler, false);
    assertNotNull(preparerFalse);

    PrepareAst preparerTrue = new PrepareAst(compiler, true);
    assertNotNull(preparerTrue);
  }

  @Test(timeout = 4000)
  public void testPrepareAnnotations_shouldTraverseAndVisitNoOps() {
    PrepareAst.PrepareAnnotations annotations = new PrepareAst.PrepareAnnotations();

    Node varNode = IR.var(IR.name("x"));
    boolean shouldTraverse = annotations.shouldTraverse(null, varNode, null);
    assertTrue("Non-objectlit should still return true", shouldTraverse);

    // Tokens other than Token.CALL and Token.FUNCTION should be a no-op in visit
    annotations.visit(null, varNode, null);
    annotations.visit(null, IR.name("y"), null);
    annotations.visit(null, IR.string("str"), null);
  }

  @Test(timeout = 4000)
  public void testPrepareAnnotations_standaloneFunctionVisitNoOp() {
    PrepareAst.PrepareAnnotations annotations = new PrepareAst.PrepareAnnotations();
    Node fn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    Node script = IR.script(fn);

    // Parent is script without JSDocInfo
    annotations.visit(null, fn, script);
    assertFalse("IS_DISPATCHER should be false", fn.getBooleanProp(Node.IS_DISPATCHER));
  }
}