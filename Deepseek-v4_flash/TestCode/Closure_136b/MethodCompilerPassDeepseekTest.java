package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * Target: MethodCompilerPass
 * 
 * Decision branches in addPossibleSignature:
 *  - node type FUNCTION -> direct signature addition
 *  - node type NAME -> scope resolution, null var handling (IDE vs IllegalStateException)
 *  - no signature added -> nonMethodProperties
 * 
 * Decision branches in GatherSignatures:
 *  - GETPROP/GETELEM with STRING dest and prototype or assignment patterns
 *  - OBJECTLIT with STRING keys and values (function or potential function name)
 * 
 * Defect targeting:
 *  - Object literal with a NAME value that is not resolvable (e.g., "alert")
 *    must not throw an IllegalStateException, and must be treated as a non-method property.
 *    In the defective version, this causes an INTERNAL COMPILER ERROR (testIssue2508576_3).
 * 
 * Boundary cases:
 *  - Empty root, null externs, methods in externs, prototype methods, static methods,
 *    object literal function definitions.
 */
public class MethodCompilerPassDeepseekTest {

  /** Concrete subclass to observe MethodCompilerPass state. */
  private static class TestMethodCompilerPass extends MethodCompilerPass {

    private final Map<String, Node> storedSignatures = new HashMap<>();
    private boolean actingCallbackCalled = false;

    TestMethodCompilerPass(AbstractCompiler compiler) {
      super(compiler);
    }

    @Override
    Callback getActingCallback() {
      // Record that the acting callback was invoked, but do nothing else.
      return new AbstractPostOrderCallback() {
        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          actingCallbackCalled = true;
        }
      };
    }

    @Override
    SignatureStore getSignatureStore() {
      return new SignatureStore() {
        @Override
        public void reset() {
          storedSignatures.clear();
        }
        @Override
        public void addSignature(String functionName, Node functionNode, String sourceFile) {
          storedSignatures.put(functionName, functionNode);
        }
        @Override
        public void removeSignature(String functionName) {
          storedSignatures.remove(functionName);
        }
      };
    }

    Map<String, Node> getStoredSignatures() {
      return storedSignatures;
    }

    boolean isActingCallbackCalled() {
      return actingCallbackCalled;
    }
  }

  @Test(timeout = 4000)
  public void testEmptyRootNoSignatures() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);
    Node root = new Node(Token.SCRIPT);
    pass.process(null, root);

    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
    assertTrue(pass.nonMethodProperties.isEmpty());
    assertTrue(pass.methodDefinitions.isEmpty());
    assertTrue(pass.getStoredSignatures().isEmpty());
    assertTrue(pass.isActingCallbackCalled());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithUndefinedFunctionNameDoesNotThrow() {
    // AST for: ({a:alert})
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node root = new Node(Token.SCRIPT);
    Node objectLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "a");
    Node value = new Node(Token.NAME, "alert"); // unresolved global
    objectLit.addChildToBack(key);
    objectLit.addChildToBack(value);
    root.addChildToBack(objectLit);

    // Defective version throws IllegalStateException here.
    pass.process(null, root);

    // Correct behavior: no crash, property is non-method.
    assertTrue(pass.nonMethodProperties.contains("a"));
    assertFalse(pass.methodDefinitions.containsKey("a"));
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.getStoredSignatures().isEmpty());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithFunctionDefinition() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    Node root = new Node(Token.SCRIPT);
    Node objectLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "method");
    Node function = new Node(Token.FUNCTION);
    objectLit.addChildToBack(key);
    objectLit.addChildToBack(function);
    root.addChildToBack(objectLit);

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("method"));
    assertTrue(pass.getStoredSignatures().containsKey("method"));
    assertFalse(pass.nonMethodProperties.contains("method"));
  }

  @Test(timeout = 4000)
  public void testStaticMethodAssignment() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Build: Foo.bar = function(){}
    Node root = new Node(Token.SCRIPT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node barStr = Node.newString(Token.STRING, "bar");
    getprop.addChildToBack(fooName);
    getprop.addChildToBack(barStr);
    Node function = new Node(Token.FUNCTION);
    assign.addChildToBack(getprop);
    assign.addChildToBack(function);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.getStoredSignatures().containsKey("bar"));
    assertFalse(pass.nonMethodProperties.contains("bar"));
  }

  @Test(timeout = 4000)
  public void testPrototypeMethodAssignment() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Build: Foo.prototype.bar = function(){}
    Node root = new Node(Token.SCRIPT);
    Node assign = new Node(Token.ASSIGN);
    Node getpropBar = new Node(Token.GETPROP);
    Node getpropPrototype = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node protoStr = Node.newString(Token.STRING, "prototype");
    getpropPrototype.addChildToBack(fooName);
    getpropPrototype.addChildToBack(protoStr);
    Node barStr = Node.newString(Token.STRING, "bar");
    getpropBar.addChildToBack(getpropPrototype);
    getpropBar.addChildToBack(barStr);
    Node function = new Node(Token.FUNCTION);
    assign.addChildToBack(getpropBar);
    assign.addChildToBack(function);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.getStoredSignatures().containsKey("bar"));
    assertFalse(pass.nonMethodProperties.contains("bar"));
  }

  @Test(timeout = 4000)
  public void testExternMethodWithSignature() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Externs: externs.Foo.bar = function(){}
    Node externs = new Node(Token.SCRIPT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node barStr = Node.newString(Token.STRING, "bar");
    getprop.addChildToBack(fooName);
    getprop.addChildToBack(barStr);
    Node function = new Node(Token.FUNCTION);
    assign.addChildToBack(getprop);
    assign.addChildToBack(function);
    externs.addChildToBack(assign);

    Node root = new Node(Token.SCRIPT);
    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("bar"));
    assertTrue(pass.methodDefinitions.containsKey("bar")); // also added to methodDefinitions
    assertTrue(pass.getStoredSignatures().containsKey("bar"));
  }

  @Test(timeout = 4000)
  public void testExternMethodWithoutSignature() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Externs: Foo.bar; (a getprop without assignment)
    Node externs = new Node(Token.SCRIPT);
    Node getprop = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node barStr = Node.newString(Token.STRING, "bar");
    getprop.addChildToBack(fooName);
    getprop.addChildToBack(barStr);
    externs.addChildToBack(getprop);

    Node root = new Node(Token.SCRIPT);
    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("bar"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("bar"));
    assertFalse(pass.methodDefinitions.containsKey("bar"));
  }

  @Test(timeout = 4000)
  public void testStaticMethodWithResolvedFunctionName() {
    // This test needs a scope where the function name is defined.
    // We build a var declaration for "baz" that is a function.
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Build: function baz(){} ; Foo.bar = baz;
    Node root = new Node(Token.SCRIPT);
    // function declaration
    Node functionNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "baz");
    functionNode.addChildToBack(nameNode);
    root.addChildToBack(functionNode);

    // assignment
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node barStr = Node.newString(Token.STRING, "bar");
    getprop.addChildToBack(fooName);
    getprop.addChildToBack(barStr);
    Node bazName = Node.newString(Token.NAME, "baz");
    assign.addChildToBack(getprop);
    assign.addChildToBack(bazName);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(pass.methodDefinitions.containsKey("bar"));
    assertTrue(pass.getStoredSignatures().containsKey("bar"));
    assertFalse(pass.nonMethodProperties.contains("bar"));
  }

  @Test(timeout = 4000)
  public void testNonMethodPropertyFromAssignmentValue() {
    AbstractCompiler compiler = new Compiler();
    TestMethodCompilerPass pass = new TestMethodCompilerPass(compiler);

    // Build: Foo.bar = 42; (non-function value)
    Node root = new Node(Token.SCRIPT);
    Node assign = new Node(Token.ASSIGN);
    Node getprop = new Node(Token.GETPROP);
    Node fooName = Node.newString(Token.NAME, "Foo");
    Node barStr = Node.newString(Token.STRING, "bar");
    getprop.addChildToBack(fooName);
    getprop.addChildToBack(barStr);
    Node number = Node.newNumber(42);
    assign.addChildToBack(getprop);
    assign.addChildToBack(number);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(pass.nonMethodProperties.contains("bar"));
    assertFalse(pass.methodDefinitions.containsKey("bar"));
  }
}