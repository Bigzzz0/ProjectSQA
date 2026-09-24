package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targets exercised:
 * - splitVarDeclarations: multi-child VARs, no-init VARs, empty-VAR assertion,
 *   and VARs nested inside function bodies.
 * - WHILE -> FOR conversion and labeled WHILE/FOR handling.
 * - FOR initializer extraction: VAR initializers, expression initializers,
 *   empty initializers, FOR-IN initializers, and initializers under LABELs.
 * - LABEL normalization: expression labels wrapped in BLOCK, LABEL/BLOCK/FOR/
 *   WHILE/DO preserved, nested labels.
 * - moveNamedFunctions: function declarations moved to the top of a body.
 * - removeDuplicateDeclarations: initialized duplicates become assignments,
 *   uninitialized duplicates are removed, and duplicate FOR-IN vars are
 *   converted to plain FOR-IN names.
 * - Defect target: the known Normalize ordering bug where duplicate removal
 *   runs before MakeDeclaredNamesUnique. A catch-block exception name must not
 *   cause a later `var e = 1` to be rewritten as `e = 1`.
 * - PropogateConstantAnnotations: @const names receive IS_CONSTANT_NAME and
 *   assertOnChange mode throws on a new constant annotation.
 */
public class NormalizeDeepseekTest {

  private Node process(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertEquals("Unexpected parse errors", 0, compiler.getErrorCount());
    new Normalize(compiler, false).process(new Node(Token.SCRIPT), root);
    return root;
  }

  private void processAssert(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertEquals("Unexpected parse errors", 0, compiler.getErrorCount());
    new Normalize(compiler, true).process(new Node(Token.SCRIPT), root);
  }

  private int countNodes(Node n, int type) {
    if (n == null) {
      return 0;
    }
    int count = (n.getType() == type) ? 1 : 0;
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      count += countNodes(c, type);
    }
    return count;
  }

  private Node findFirst(Node n, int type) {
    if (n == null) {
      return null;
    }
    if (n.getType() == type) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node result = findFirst(c, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private boolean hasVarDeclaration(Node n) {
    if (n == null) {
      return false;
    }
    if (n.getType() == Token.VAR
        && n.getFirstChild() != null
        && n.getFirstChild().getType() == Token.NAME) {
      return true;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (hasVarDeclaration(c)) {
        return true;
      }
    }
    return false;
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarations() {
    Node root = process("var a=0,b=foo();");
    assertEquals(2, countNodes(root, Token.VAR));
    Node first = root.getFirstChild();
    assertNotNull(first);
    assertEquals(Token.VAR, first.getType());
    assertEquals(Token.VAR, first.getNext().getType());
    assertEquals(1, first.getChildCount());
    assertEquals("a", first.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testSplitVarDeclarationsInsideFunction() {
    Node root = process("function f(){var a=1,b=2;}");
    Node function = findFirst(root, Token.FUNCTION);
    assertNotNull(function);
    Node body = function.getLastChild();
    assertEquals(2, countNodes(body, Token.VAR));
  }

  @Test(timeout = 4000)
  public void testWhileConvertedToFor() {
    Node root = process("while(x){x=1;}");
    assertEquals(0, countNodes(root, Token.WHILE));
    assertEquals(1, countNodes(root, Token.FOR));
  }

  @Test(timeout = 4000)
  public void testForInitializerVarMovedOut() {
    Node root = process("for(var a=0;a<10;a++){foo();}");
    Node forNode = findFirst(root, Token.FOR);
    assertNotNull(forNode);
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
    assertEquals(Token.VAR, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testForInitializerExpressionMovedOut() {
    Node root = process("for(a=0;a<10;a++){foo();}");
    Node forNode = findFirst(root, Token.FOR);
    assertNotNull(forNode);
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
    assertEquals(Token.EXPR_RESULT, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testEmptyForInitializerStays() {
    Node root = process("for(;;){break;}");
    Node forNode = findFirst(root, Token.FOR);
    assertNotNull(forNode);
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
    assertEquals(Token.FOR, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testForInInitializerStays() {
    Node root = process("for(var a in obj){foo();}");
    assertEquals(Token.FOR, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testLabelExpressionWrappedInBlock() {
    Node root = process("a:foo();");
    Node label = findFirst(root, Token.LABEL);
    assertNotNull(label);
    assertEquals(Token.BLOCK, label.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testLabelBlockNotWrapped() {
    Node root = process("a:{foo();}");
    Node label = findFirst(root, Token.LABEL);
    assertNotNull(label);
    assertEquals(Token.BLOCK, label.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testLabelWhileConvertedToFor() {
    Node root = process("a:while(x){x=1;}");
    Node label = findFirst(root, Token.LABEL);
    assertNotNull(label);
    assertEquals(Token.FOR, label.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testLabelDoNotWrapped() {
    Node root = process("a:do{x++;}while(x);");
    Node label = findFirst(root, Token.LABEL);
    assertNotNull(label);
    assertEquals(Token.DO, label.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testNestedLabelWrapping() {
    Node root = process("a:b:foo();");
    Node label = findFirst(root, Token.LABEL);
    assertNotNull(label);
    assertEquals(Token.LABEL, label.getLastChild().getType());
    Node inner = label.getLastChild();
    assertEquals(Token.BLOCK, inner.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testLabeledForInitializerMovedBeforeLabel() {
    Node root = process("a:for(var i=0;i<10;i++){foo();}");
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    Node label = first.getNext();
    assertNotNull(label);
    assertEquals(Token.LABEL, label.getType());
    Node forNode = label.getLastChild();
    assertEquals(Token.FOR, forNode.getType());
    assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMoveNamedFunctionDeclarations() {
    Node root = process("function f(){foo();function g(){}}");
    Node function = findFirst(root, Token.FUNCTION);
    assertNotNull(function);
    Node body = function.getLastChild();
    Node first = body.getFirstChild();
    assertNotNull(first);
    assertEquals(Token.FUNCTION, first.getType());
    assertEquals("g", first.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarationsWithInit() {
    Node root = process("var a=1;var a=2;");
    assertEquals(1, countNodes(root, Token.VAR));
    assertEquals(1, countNodes(root, Token.ASSIGN));
    Node first = root.getFirstChild();
    assertEquals(Token.VAR, first.getType());
    Node second = first.getNext();
    assertNotNull(second);
    assertEquals(Token.EXPR_RESULT, second.getType());
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarationsNoInit() {
    Node root = process("var a;var a;");
    assertEquals(1, countNodes(root, Token.VAR));
    Node var = root.getFirstChild();
    assertEquals(Token.VAR, var.getType());
    assertEquals(1, var.getChildCount());
    assertFalse(var.getFirstChild().hasChildren());
  }

  @Test(timeout = 4000)
  public void testRemoveDuplicateVarDeclarationsForIn() {
    Node root = process("var a;for(var a in b){}");
    assertEquals(1, countNodes(root, Token.VAR));
    Node forNode = findFirst(root, Token.FOR);
    assertNotNull(forNode);
    assertEquals(Token.NAME, forNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testCatchBlockExceptionNameDoesNotRemoveVarDeclaration() {
    Node root = process(
        "function f(){try{throw 0;}catch(e){e;}var e=1;return e;}");
    assertTrue("var e declaration must be preserved", hasVarDeclaration(root));
  }

  @Test(timeout = 4000)
  public void testProcessIsIdempotent() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a=1;");
    assertEquals(0, compiler.getErrorCount());
    Node externs = new Node(Token.SCRIPT);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, root);
    assertEquals(1, countNodes(root, Token.VAR));
    normalize.process(externs, root);
    assertEquals(1, countNodes(root, Token.VAR));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeThrowsForVarSplit() {
    processAssert("var a=0,b=1;");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeThrowsForWhileConversion() {
    processAssert("while(x){x=1;}");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssertOnChangeThrowsForEmptyVar() {
    Compiler compiler = new Compiler();
    Node root = new Node(Token.SCRIPT);
    root.addChildToFront(new Node(Token.VAR));
    new Normalize(compiler, true).process(new Node(Token.SCRIPT), root);
  }

  @Test(timeout = 4000)
  public void testAssertOnChangeDoesNotThrowForNormalizedCode() {
    processAssert("var a=1;");
  }

  @Test(timeout = 4000)
  public void testConstantAnnotationPropagated() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var x = 1;");
    assertEquals(0, compiler.getErrorCount());
    new Normalize(compiler, false).process(new Node(Token.SCRIPT), root);
    Node name = findFirst(root, Token.NAME);
    assertNotNull(name);
    assertTrue(name.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstantAnnotationAssertOnChangeThrows() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("/** @const */ var x = 1;");
    assertEquals(0, compiler.getErrorCount());
    new Normalize(compiler, true).process(new Node(Token.SCRIPT), root);
  }
}