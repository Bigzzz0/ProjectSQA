/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.MethodCompilerPass
 *
 * Decision / Branch Coverage Targets:
 * 1. process(Node externs, Node root):
 *    - externs != null (traverse externs with GetExternMethods) vs externs == null.
 *    - SignatureStore reset, externMethods and methodDefinitions cleared.
 *    - traverseRoots with GatherSignatures and actingCallback.
 *
 * 2. GetExternMethods:
 *    - Token.GETPROP / Token.GETELEM:
 *      * dest.getType() == Token.STRING vs dest.getType() != Token.STRING (e.g. computed GETELEM).
 *      * parent.getType() == Token.ASSIGN && parent.getFirstChild() == n && n.getNext().getType() == Token.FUNCTION
 *        (extern method with function signature) -> addSignature.
 *      * else branch (non-function assignment or bare property access in externs)
 *        -> removeSignature and externMethodsWithoutSignatures.add(name).
 *      * externMethods.add(name).
 *    - Token.OBJECTLIT:
 *      * key.getType() == Token.STRING:
 *        - value.getType() == Token.FUNCTION -> addSignature.
 *        - value.getType() != Token.FUNCTION -> removeSignature, externMethodsWithoutSignatures.add(name).
 *        - externMethods.add(name).
 *      * non-string keys skipped.
 *    - Other token types ignored.
 *
 * 3. GatherSignatures:
 *    - Token.GETPROP / Token.GETELEM:
 *      * dest.getString().equals("prototype") -> processPrototypeParent(t, parent).
 *      * else (static method assignment):
 *        - parent.getType() == Token.ASSIGN && parent.getFirstChild() == n -> addPossibleSignature.
 *        - parent not ASSIGN -> ignored.
 *    - Token.OBJECTLIT:
 *      * loops key = key.getNext().getNext(), if key is STRING -> addPossibleSignature(key.getString(), value, t).
 *    - processPrototypeParent:
 *      * switch on n.getType(): Token.GETPROP / Token.GETELEM.
 *      * dest.getType() == Token.STRING && parent.getType() == Token.ASSIGN -> addPossibleSignature.
 *
 * 4. addPossibleSignature:
 *    - node.getType() == Token.FUNCTION -> addSignature directly, signatureAdded = true.
 *    - node.getType() == Token.NAME:
 *      * v == null && compiler.isIdeMode() -> return directly.
 *      * v == null && !compiler.isIdeMode() -> throws IllegalStateException("VarCheck should have caught this undefined function").
 *        (Defects4J Issue 2508576 / InlineGettersTest::testIssue2508576_3 defect trigger).
 *      * v != null && v.getInitialValue() != null && initialValue.getType() == Token.FUNCTION -> addSignature.
 *      * v != null && (initialValue == null || initialValue.getType() != Token.FUNCTION) -> signatureAdded remains false.
 *    - node.getType() != FUNCTION && node.getType() != NAME -> signatureAdded = false.
 *    - if (!signatureAdded) -> nonMethodProperties.add(name).
 *
 * 5. addSignature:
 *    - externMethodsWithoutSignatures.contains(name) -> returns early, doesn't add to store or definitions.
 *    - else -> getSignatureStore().addSignature, methodDefinitions.put(name, function).
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MethodCompilerPassGeminiTest {

  private Compiler compiler;

  /**
   * Concrete test implementation of MethodCompilerPass.SignatureStore.
   */
  private static class DummySignatureStore implements MethodCompilerPass.SignatureStore {
    final Map<String, List<Node>> signatures = new HashMap<>();
    int resetCount = 0;
    final List<String> removedSignatures = new ArrayList<>();

    @Override
    public void reset() {
      resetCount++;
      signatures.clear();
      removedSignatures.clear();
    }

    @Override
    public void addSignature(String functionName, Node functionNode, String sourceFile) {
      if (!signatures.containsKey(functionName)) {
        signatures.put(functionName, new ArrayList<Node>());
      }
      signatures.get(functionName).add(functionNode);
    }

    @Override
    public void removeSignature(String functionName) {
      removedSignatures.add(functionName);
      signatures.remove(functionName);
    }
  }

  /**
   * Concrete test harness subclass of MethodCompilerPass.
   */
  private static class TestMethodCompilerPass extends MethodCompilerPass {
    private final DummySignatureStore store = new DummySignatureStore();
    private final Callback actingCallback;

    TestMethodCompilerPass(AbstractCompiler compiler) {
      this(compiler, new AbstractPostOrderCallback() {
        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          // No-op acting callback by default
        }
      });
    }

    TestMethodCompilerPass(AbstractCompiler compiler, Callback actingCallback) {
      super(compiler);
      this.actingCallback = actingCallback;
    }

    @Override
    Callback getActingCallback() {
      return actingCallback;
    }

    @Override
    SignatureStore getSignatureStore() {
      return store;
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessExternFunctionAssignment() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Extern: window.setTimeout = function(fn, ms) {};
    Node externs = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "window"), Node.newString("setTimeout"));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assign.addChildToBack(getprop);
    assign.addChildToBack(fn);
    expr.addChildToBack(assign);
    externs.addChildToBack(expr);

    Node root = new Node(Token.BLOCK);

    pass.process(externs, root);

    assertEquals(1, pass.store.resetCount);
    assertTrue("setTimeout should be tracked in externMethods", pass.externMethods.contains("setTimeout"));
    assertFalse("setTimeout has a signature, so it should not be in externMethodsWithoutSignatures",
        pass.externMethodsWithoutSignatures.contains("setTimeout"));
    assertTrue("Signature should be added to store", pass.store.signatures.containsKey("setTimeout"));
    assertEquals(1, pass.methodDefinitions.get("setTimeout").size());
  }

  @Test(timeout = 4000)
  public void testProcessExternWithoutSignature() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Extern: window.location = "some_url"; (Assign non-function)
    Node externs = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "window"), Node.newString("location"));
    Node strVal = Node.newString("some_url");
    assign.addChildToBack(getprop);
    assign.addChildToBack(strVal);
    expr.addChildToBack(assign);
    externs.addChildToBack(expr);

    Node root = new Node(Token.BLOCK);

    pass.process(externs, root);

    assertTrue("location should be in externMethods", pass.externMethods.contains("location"));
    assertTrue("location has no function signature, should be in externMethodsWithoutSignatures",
        pass.externMethodsWithoutSignatures.contains("location"));
    assertTrue("store.removeSignature should have been invoked for location",
        pass.store.removedSignatures.contains("location"));
    assertFalse(pass.store.signatures.containsKey("location"));
  }

  @Test(timeout = 4000)
  public void testProcessExternObjectLiteral() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Extern: ({ myFn: function() {}, nonFn: 42 })
    Node externs = new Node(Token.BLOCK);
    Node objLit = new Node(Token.OBJECTLIT);

    Node key1 = Node.newString("myFn");
    Node val1 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    key1.addChildToBack(val1);

    Node key2 = Node.newString("nonFn");
    Node val2 = Node.newNumber(42);
    key2.addChildToBack(val2);

    objLit.addChildToBack(key1);
    objLit.addChildToBack(val1);
    objLit.addChildToBack(key2);
    objLit.addChildToBack(val2);
    externs.addChildToBack(objLit);

    Node root = new Node(Token.BLOCK);

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("myFn"));
    assertTrue(pass.externMethods.contains("nonFn"));
    assertFalse(pass.externMethodsWithoutSignatures.contains("myFn"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("nonFn"));
    assertTrue(pass.store.signatures.containsKey("myFn"));
    assertFalse(pass.store.signatures.containsKey("nonFn"));
  }

  @Test(timeout = 4000)
  public void testProcessUserStaticMethodAndObjectLiteral() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs = new Node(Token.BLOCK);

    // User JS:
    // Foo.bar = function() {};
    // ({ baz: function() {}, numProp: 100 })
    Node root = new Node(Token.BLOCK);

    Node expr1 = new Node(Token.EXPR_RESULT);
    Node assign1 = new Node(Token.ASSIGN);
    Node getpropBar = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("bar"));
    Node fnBar = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assign1.addChildToBack(getpropBar);
    assign1.addChildToBack(fnBar);
    expr1.addChildToBack(assign1);
    root.addChildToBack(expr1);

    Node expr2 = new Node(Token.EXPR_RESULT);
    Node objLit = new Node(Token.OBJECTLIT);
    Node keyBaz = Node.newString("baz");
    Node valBaz = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    keyBaz.addChildToBack(valBaz);
    Node keyNum = Node.newString("numProp");
    Node valNum = Node.newNumber(100);
    keyNum.addChildToBack(valNum);

    objLit.addChildToBack(keyBaz);
    objLit.addChildToBack(valBaz);
    objLit.addChildToBack(keyNum);
    objLit.addChildToBack(valNum);
    expr2.addChildToBack(objLit);
    root.addChildToBack(expr2);

    pass.process(externs, root);

    assertTrue(pass.store.signatures.containsKey("bar"));
    assertTrue(pass.store.signatures.containsKey("baz"));
    assertTrue("numProp is not a function, should be in nonMethodProperties",
        pass.nonMethodProperties.contains("numProp"));
    assertFalse(pass.store.signatures.containsKey("numProp"));
  }

  @Test(timeout = 4000)
  public void testProcessPrototypeMethodDeclaration() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs = new Node(Token.BLOCK);

    // Foo.prototype.sayHello = function() {};
    // AST:
    // EXPR_RESULT
    //   ASSIGN
    //     GETPROP (Foo.prototype.sayHello)
    //       GETPROP (Foo.prototype)
    //         NAME Foo
    //         STRING prototype
    //       STRING sayHello
    //     FUNCTION
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node assign = new Node(Token.ASSIGN);

    Node protoProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("prototype"));
    Node sayHelloProp = new Node(Token.GETPROP, protoProp, Node.newString("sayHello"));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));

    assign.addChildToBack(sayHelloProp);
    assign.addChildToBack(fn);
    expr.addChildToBack(assign);
    root.addChildToBack(expr);

    pass.process(externs, root);

    assertTrue("sayHello should be gathered", pass.store.signatures.containsKey("sayHello"));
    assertEquals(1, pass.methodDefinitions.get("sayHello").size());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessNullExterns() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node root = new Node(Token.BLOCK);
    // externs is null
    pass.process(null, root);

    assertEquals(1, pass.store.resetCount);
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
    assertTrue(pass.methodDefinitions.isEmpty());
  }

  @Test(timeout = 4000)
  public void testEmptyExternsAndEmptyRoot() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    pass.process(externs, root);

    assertEquals(1, pass.store.resetCount);
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
    assertTrue(pass.methodDefinitions.isEmpty());
    assertTrue(pass.nonMethodProperties.isEmpty());
  }

  @Test(timeout = 4000)
  public void testExternGetElemNonStringIgnored() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Extern: window[0] = function() {}; (computed index 0 instead of STRING token)
    Node externs = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node assign = new Node(Token.ASSIGN);
    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "window"), Node.newNumber(0));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assign.addChildToBack(getelem);
    assign.addChildToBack(fn);
    expr.addChildToBack(assign);
    externs.addChildToBack(expr);

    Node root = new Node(Token.BLOCK);

    pass.process(externs, root);

    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.store.signatures.isEmpty());
  }

  @Test(timeout = 4000)
  public void testSignatureNotAddedIfInExternMethodsWithoutSignatures() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Extern: window.myMethod = 1; (marks myMethod in externMethodsWithoutSignatures)
    Node externs = new Node(Token.BLOCK);
    Node exprExtern = new Node(Token.EXPR_RESULT);
    Node assignExtern = new Node(Token.ASSIGN);
    Node getpropExtern = new Node(Token.GETPROP, Node.newString(Token.NAME, "window"), Node.newString("myMethod"));
    assignExtern.addChildToBack(getpropExtern);
    assignExtern.addChildToBack(Node.newNumber(1));
    exprExtern.addChildToBack(assignExtern);
    externs.addChildToBack(exprExtern);

    // Code: Foo.myMethod = function() {};
    Node root = new Node(Token.BLOCK);
    Node exprCode = new Node(Token.EXPR_RESULT);
    Node assignCode = new Node(Token.ASSIGN);
    Node getpropCode = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("myMethod"));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assignCode.addChildToBack(getpropCode);
    assignCode.addChildToBack(fn);
    exprCode.addChildToBack(assignCode);
    root.addChildToBack(exprCode);

    pass.process(externs, root);

    assertTrue(pass.externMethodsWithoutSignatures.contains("myMethod"));
    // Even though code provided a signature, externMethodsWithoutSignatures prevents adding it
    assertFalse("Signature store must not contain method without signature from externs",
        pass.store.signatures.containsKey("myMethod"));
    assertFalse("methodDefinitions must not contain method without signature from externs",
        pass.methodDefinitions.containsKey("myMethod"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 2508576)
  // =========================================================================

  /**
   * Targets Defects4J issue (InlineGettersTest::testIssue2508576_3):
   * When an object literal refers to an undefined variable/function name (e.g. `({a: alert})` where
   * alert was not caught/defined in scope) and ideMode is false, addPossibleSignature attempts
   * `t.getScope().getVar(functionName)` which returns null, throwing IllegalStateException.
   */
  @Test(timeout = 4000)
  public void testDefectIssue2508576UndefinedNameThrowsIllegalStateExceptionWhenNotIdeMode() {
    compiler.setIdeMode(false);
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs = new Node(Token.BLOCK);
    // User JS: ({ a: undeclaredFunction })
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("a");
    Node val = Node.newString(Token.NAME, "undeclaredFunction");
    key.addChildToBack(val);
    objLit.addChildToBack(key);
    objLit.addChildToBack(val);
    expr.addChildToBack(objLit);
    root.addChildToBack(expr);

    try {
      pass.process(externs, root);
      fail("Expected IllegalStateException for undefined function reference in non-IDE mode");
    } catch (IllegalStateException e) {
      assertTrue("Exception message should indicate VarCheck check failure",
          e.getMessage().contains("VarCheck should have caught this undefined function"));
    }
  }

  /**
   * When compiler is in IDE mode, the undefined function reference must NOT throw an
   * exception and should silently return.
   */
  @Test(timeout = 4000)
  public void testDefectIssue2508576UndefinedNameSilentlyReturnsInIdeMode() {
    compiler.setIdeMode(true);
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs = new Node(Token.BLOCK);
    // User JS: ({ a: undeclaredFunction })
    Node root = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("a");
    Node val = Node.newString(Token.NAME, "undeclaredFunction");
    key.addChildToBack(val);
    objLit.addChildToBack(key);
    objLit.addChildToBack(val);
    expr.addChildToBack(objLit);
    root.addChildToBack(expr);

    // In IDE mode, this must complete without throwing IllegalStateException
    pass.process(externs, root);
    assertFalse(pass.store.signatures.containsKey("a"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testActingCallbackIsInvokedDuringProcess() {
    final boolean[] callbackInvoked = new boolean[] { false };
    Callback actingCallback = new AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        callbackInvoked[0] = true;
      }
    };

    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler, actingCallback);
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(new Node(Token.EMPTY));

    pass.process(externs, root);

    assertTrue("Acting callback should have been invoked during process traversal",
        callbackInvoked[0]);
  }

  @Test(timeout = 4000)
  public void testProcessStateClearingAcrossMultipleInvocations() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node externs1 = new Node(Token.BLOCK);
    Node root1 = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "A"), Node.newString("fn1"));
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assign.addChildToBack(getprop);
    assign.addChildToBack(fn);
    expr.addChildToBack(assign);
    root1.addChildToBack(expr);

    pass.process(externs1, root1);
    assertTrue(pass.store.signatures.containsKey("fn1"));

    // Run a second time with empty trees
    Node externs2 = new Node(Token.BLOCK);
    Node root2 = new Node(Token.BLOCK);
    pass.process(externs2, root2);

    assertEquals(2, pass.store.resetCount);
    assertTrue("Signatures should be cleared on second process call", pass.store.signatures.isEmpty());
    assertTrue("Method definitions should be cleared on second process call", pass.methodDefinitions.isEmpty());
    assertTrue("Extern methods should be cleared", pass.externMethods.isEmpty());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPassInstantiationAndReferences() {
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    assertSame(compiler, pass.compiler);
    assertNotNull(pass.getActingCallback());
    assertNotNull(pass.getSignatureStore());
    assertNotNull(pass.externMethods);
    assertNotNull(pass.externMethodsWithoutSignatures);
    assertNotNull(pass.nonMethodProperties);
    assertNotNull(pass.methodDefinitions);
  }
}