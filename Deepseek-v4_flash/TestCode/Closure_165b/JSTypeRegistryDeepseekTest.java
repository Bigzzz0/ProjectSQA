package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import junit.framework.TestCase;

public class JSTypeRegistryTest extends TestCase {

  public void testFunctionTypeWithThis() {
    JSTypeRegistry registry = new JSTypeRegistry(null);

    // A scope that resolves no names; native types are resolved by the registry.
    StaticScope<JSType> scope = new StaticScope<JSType>() {
      @Override
      public JSType getType(String name) {
        return null;
      }

      @Override
      public StaticScope<JSType> getParentScope() {
        return null;
      }

      @Override
      public StaticSlot<JSType> getSlot(String name) {
        return null;
      }
    };

    // Build AST for: function(this:Object) : Object
    Node returnType = Node.newString("Object");
    Node thisType = Node.newString("Object");
    Node thisNode = new Node(Token.THIS);
    thisNode.addChildToBack(thisType);
    Node params = new Node(Token.LP);
    Node functionNode = new Node(Token.FUNCTION);
    functionNode.addChildToBack(returnType);
    functionNode.addChildToBack(thisNode);
    functionNode.addChildToBack(params);

    JSType result = registry.createFromTypeNodes(functionNode, "test", scope);

    assertTrue("Expected a FunctionType", result instanceof FunctionType);
    FunctionType fn = (FunctionType) result;

    // The 'this' type must not be counted as a regular parameter.
    assertEquals("this type should not be a parameter",
        0, fn.getParametersNode().getChildCount());

    // The function's 'this' type should be Object.
    assertEquals("this type should be Object",
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        fn.getTypeOfThis());
  }
}