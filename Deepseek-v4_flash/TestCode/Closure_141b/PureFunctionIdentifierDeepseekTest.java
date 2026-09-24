package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.jscomp.graph.FixedPointGraphTraversal;
import com.google.javascript.jscomp.graph.LinkedDirectedGraph;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Test suite for PureFunctionIdentifier.
 * Targets the known defect where conditional calls (||, &&, ?:) are not handled,
 * causing pure functions to be incorrectly marked as having side effects.
 */
public class PureFunctionIdentifierDeepseekTest {

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testConstructorAndInitialState() {
    Compiler compiler = new Compiler();
    DefinitionProvider provider = new SimpleDefinitionProvider();
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, provider);
    // No direct access to fields, but we can verify process works later.
    // Just ensure no exception.
  }

  @Test(timeout = 4000)
  public void testProcessThrowsOnSecondCall() {
    Compiler compiler = new Compiler();
    DefinitionProvider provider = new SimpleDefinitionProvider();
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, provider);
    Node externs = new Node(Token.EMPTY);
    Node root = new Node(Token.EMPTY);
    identifier.process(externs, root);
    try {
      identifier.process(externs, root);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testGetDebugReportBeforeProcessThrowsNpe() {
    Compiler compiler = new Compiler();
    DefinitionProvider provider = new SimpleDefinitionProvider();
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, provider);
    try {
      identifier.getDebugReport();
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  // ==================== Partition B: FunctionInformation ====================

  @Test(timeout = 4000)
  public void testFunctionInformationInitialState() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    assertTrue(info.mayBePure());
    assertTrue(info.mayHaveSideEffects());
    assertFalse(info.isExtern());
    assertFalse(info.mutatesGlobalState());
    assertFalse(info.mutatesThis());
    assertFalse(info.functionThrows());
  }

  @Test(timeout = 4000)
  public void testFunctionInformationExtern() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(true);
    assertTrue(info.isExtern());
  }

  @Test(timeout = 4000)
  public void testSetIsPure() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setIsPure();
    assertFalse(info.mayBePure());
    assertFalse(info.mayHaveSideEffects());
  }

  @Test(timeout = 4000)
  public void testSetTaintsGlobalState() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setTaintsGlobalState();
    assertFalse(info.mayBePure());
    assertTrue(info.mayHaveSideEffects());
    assertTrue(info.mutatesGlobalState());
  }

  @Test(timeout = 4000)
  public void testSetTaintsThis() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setTaintsThis();
    assertFalse(info.mayBePure());
    assertTrue(info.mayHaveSideEffects());
    assertTrue(info.mutatesThis());
  }

  @Test(timeout = 4000)
  public void testSetFunctionThrows() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setFunctionThrows();
    assertFalse(info.mayBePure());
    assertTrue(info.mayHaveSideEffects());
    assertTrue(info.functionThrows());
  }

  @Test(timeout = 4000)
  public void testSetTaintsUnknown() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setTaintsUnknown();
    assertFalse(info.mayBePure());
    assertTrue(info.mayHaveSideEffects());
    assertTrue(info.mutatesGlobalState()); // taintsUnknown implies mutatesGlobalState
  }

  @Test(timeout = 4000)
  public void testAppendCallAndGetCalls() {
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    Node call1 = new Node(Token.CALL);
    Node call2 = new Node(Token.NEW);
    info.appendCall(call1);
    info.appendCall(call2);
    List<Node> calls = info.getCallsInFunctionBody();
    assertEquals(2, calls.size());
    assertSame(call1, calls.get(0));
    assertSame(call2, calls.get(1));
  }

  @Test(timeout = 4000)
  public void testCheckInvariantThrowsWhenBothFalse() {
    // This is tricky: we need to set both pure and side effects? Actually,
    // after setIsPure, mayBePure returns false, mayHaveSideEffects returns false.
    // That violates invariant. So calling any setter after setIsPure should throw.
    PureFunctionIdentifier.FunctionInformation info =
        new PureFunctionIdentifier.FunctionInformation(false);
    info.setIsPure();
    try {
      info.setTaintsGlobalState();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  // ==================== Partition C: getCallableDefinitions ====================

  @Test(timeout = 4000)
  public void testGetCallableDefinitionsReturnsNullForNonNameOrGetProp() {
    Node orNode = new Node(Token.OR);
    Collection<Definition> result = PureFunctionIdentifier.getCallableDefinitions(
        new SimpleDefinitionProvider(), orNode);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testGetCallableDefinitionsReturnsNullWhenNoDefinitions() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    Collection<Definition> result = PureFunctionIdentifier.getCallableDefinitions(
        new SimpleDefinitionProvider(), nameNode);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testGetCallableDefinitionsReturnsNullWhenDefinitionNotFunction() {
    Node nameNode = Node.newString(Token.NAME, "bar");
    SimpleDefinitionProvider provider = new SimpleDefinitionProvider();
    provider.addDefinition(nameNode, new Node(Token.STRING)); // not a function
    Collection<Definition> result = PureFunctionIdentifier.getCallableDefinitions(provider, nameNode);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testGetCallableDefinitionsReturnsListWhenFunction() {
    Node nameNode = Node.newString(Token.NAME, "baz");
    Node functionNode = new Node(Token.FUNCTION);
    SimpleDefinitionProvider provider = new SimpleDefinitionProvider();
    provider.addDefinition(nameNode, functionNode);
    Collection<Definition> result = PureFunctionIdentifier.getCallableDefinitions(provider, nameNode);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertSame(functionNode, result.iterator().next().getRValue());
  }

  // ==================== Partition D: SideEffectPropagationCallback ====================

  @Test(timeout = 4000)
  public void testSideEffectPropagationCallbackGlobalStatePropagation() {
    PureFunctionIdentifier.FunctionInformation callee =
        new PureFunctionIdentifier.FunctionInformation(false);
    callee.setTaintsGlobalState();
    PureFunctionIdentifier.FunctionInformation caller =
        new PureFunctionIdentifier.FunctionInformation(false);
    Node callSite = new Node(Token.CALL);
    PureFunctionIdentifier.SideEffectPropagationCallback callback =
        new PureFunctionIdentifier.SideEffectPropagationCallback();
    boolean changed = callback.traverseEdge(callee, callSite, caller);
    assertTrue(changed);
    assertTrue(caller.mutatesGlobalState());
  }

  @Test(timeout = 4000)
  public void testSideEffectPropagationCallbackThrowPropagation() {
    PureFunctionIdentifier.FunctionInformation callee =
        new PureFunctionIdentifier.FunctionInformation(false);
    callee.setFunctionThrows();
    PureFunctionIdentifier.FunctionInformation caller =
        new PureFunctionIdentifier.FunctionInformation(false);
    Node callSite = new Node(Token.CALL);
    PureFunctionIdentifier.SideEffectPropagationCallback callback =
        new PureFunctionIdentifier.SideEffectPropagationCallback();
    boolean changed = callback.traverseEdge(callee, callSite, caller);
    assertTrue(changed);
    assertTrue(caller.functionThrows());
  }

  @Test(timeout = 4000)
  public void testSideEffectPropagationCallbackThisPropagationViaCall() {
    PureFunctionIdentifier.FunctionInformation callee =
        new PureFunctionIdentifier.FunctionInformation(false);
    callee.setTaintsThis();
    PureFunctionIdentifier.FunctionInformation caller =
        new PureFunctionIdentifier.FunctionInformation(false);
    // Create a call like obj.method() where obj is 'this'
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.THIS), Node.newString("method"));
    Node callSite = new Node(Token.CALL, getProp);
    PureFunctionIdentifier.SideEffectPropagationCallback callback =
        new PureFunctionIdentifier.SideEffectPropagationCallback();
    boolean changed = callback.traverseEdge(callee, callSite, caller);
    assertTrue(changed);
    assertTrue(caller.mutatesThis());
  }

  @Test(timeout = 4000)
  public void testSideEffectPropagationCallbackThisPropagationViaCallNonThisObject() {
    PureFunctionIdentifier.FunctionInformation callee =
        new PureFunctionIdentifier.FunctionInformation(false);
    callee.setTaintsThis();
    PureFunctionIdentifier.FunctionInformation caller =
        new PureFunctionIdentifier.FunctionInformation(false);
    // Create a call like obj.method() where obj is not 'this'
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString("method"));
    Node callSite = new Node(Token.CALL, getProp);
    PureFunctionIdentifier.SideEffectPropagationCallback callback =
        new PureFunctionIdentifier.SideEffectPropagationCallback();
    boolean changed = callback.traverseEdge(callee, callSite, caller);
    assertTrue(changed);
    assertTrue(caller.mutatesGlobalState()); // because object is not 'this'
  }

  @Test(timeout = 4000)
  public void testSideEffectPropagationCallbackNoPropagationForNew() {
    PureFunctionIdentifier.FunctionInformation callee =
        new PureFunctionIdentifier.FunctionInformation(false);
    callee.setTaintsThis();
    PureFunctionIdentifier.FunctionInformation caller =
        new PureFunctionIdentifier.FunctionInformation(false);
    Node callSite = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    PureFunctionIdentifier.SideEffectPropagationCallback callback =
        new PureFunctionIdentifier.SideEffectPropagationCallback();
    boolean changed = callback.traverseEdge(callee, callSite, caller);
    assertFalse(changed); // constructor 'this' modifications are not side effects
  }

  // ==================== Partition E: getCallThisObject ====================

  @Test(timeout = 4000)
  public void testGetCallThisObjectSimpleCall() {
    Node callSite = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node result = PureFunctionIdentifier.getCallThisObject(callSite);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testGetCallThisObjectGetProp() {
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("method"));
    Node callSite = new Node(Token.CALL, getProp);
    Node result = PureFunctionIdentifier.getCallThisObject(callSite);
    assertNotNull(result);
    assertEquals(Token.NAME, result.getType());
    assertEquals("obj", result.getString());
  }

  @Test(timeout = 4000)
  public void testGetCallThisObjectCallMethod() {
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("call"));
    Node callSite = new Node(Token.CALL, getProp, Node.newString(Token.THIS));
    Node result = PureFunctionIdentifier.getCallThisObject(callSite);
    assertNotNull(result);
    assertEquals(Token.THIS, result.getType());
  }

  @Test(timeout = 4000)
  public void testGetCallThisObjectApplyMethod() {
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("apply"));
    Node callSite = new Node(Token.CALL, getProp, Node.newString(Token.THIS));
    Node result = PureFunctionIdentifier.getCallThisObject(callSite);
    assertNotNull(result);
    assertEquals(Token.THIS, result.getType());
  }

  // ==================== Partition F: Integration Tests Targeting Defect ====================

  /**
   * Test that a function calling another via logical OR is correctly identified as pure.
   * This targets the defect where conditional calls cause false side-effect marking.
   */
  @Test(timeout = 4000)
  public void testPureFunctionWithOrCall() {
    Compiler compiler = new Compiler();
    String source = "function f() { return 1; } function g() { return f() || f(); }";
    compiler.compile(
        new JSSourceFile("externs", "function f() {}"),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    // The buggy version would not list "g" as pure.
    assertTrue("Expected g to be pure, but report was: " + report,
        report.contains("g"));
  }

  @Test(timeout = 4000)
  public void testPureFunctionWithAndCall() {
    Compiler compiler = new Compiler();
    String source = "function f() { return 1; } function g() { return f() && f(); }";
    compiler.compile(
        new JSSourceFile("externs", "function f() {}"),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    assertTrue("Expected g to be pure, but report was: " + report,
        report.contains("g"));
  }

  @Test(timeout = 4000)
  public void testPureFunctionWithHookCall() {
    Compiler compiler = new Compiler();
    String source = "function f() { return 1; } function g() { return true ? f() : f(); }";
    compiler.compile(
        new JSSourceFile("externs", "function f() {}"),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    assertTrue("Expected g to be pure, but report was: " + report,
        report.contains("g"));
  }

  @Test(timeout = 4000)
  public void testPureFunctionWithOrCallAndSideEffectsInCallee() {
    // If the callee has side effects, the outer function should not be pure.
    Compiler compiler = new Compiler();
    String source = "var x = 0; function f() { x = 1; } function g() { return f() || f(); }";
    compiler.compile(
        new JSSourceFile("externs", ""),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    // g should NOT be pure because f has side effects.
    assertFalse("Expected g to have side effects, but report was: " + report,
        report.contains("g"));
  }

  @Test(timeout = 4000)
  public void testPureFunctionWithHookCallAndSideEffectsInCallee() {
    Compiler compiler = new Compiler();
    String source = "var x = 0; function f() { x = 1; } function g() { return true ? f() : f(); }";
    compiler.compile(
        new JSSourceFile("externs", ""),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    assertFalse("Expected g to have side effects, but report was: " + report,
        report.contains("g"));
  }

  @Test(timeout = 4000)
  public void testPureFunctionWithMultipleConditionalCalls() {
    Compiler compiler = new Compiler();
    String source = "function f() { return 1; } function g() { return (f() || f()) && (f() ? f() : f()); }";
    compiler.compile(
        new JSSourceFile("externs", "function f() {}"),
        new JSSourceFile("source", source));
    PureFunctionIdentifier identifier = new PureFunctionIdentifier(compiler, compiler.getDefinitionProvider());
    identifier.process(compiler.getExternsRoot(), compiler.getJsRoot());
    String report = identifier.getDebugReport();
    assertTrue("Expected g to be pure, but report was: " + report,
        report.contains("g"));
  }

  // ==================== Helper class for testing ====================

  /**
   * A simple DefinitionProvider that stores definitions in a map.
   */
  private static class SimpleDefinitionProvider implements DefinitionProvider {
    private final Map<Node, Definition> definitions = new java.util.HashMap<>();

    void addDefinition(Node name, Node rValue) {
      definitions.put(name, new SimpleDefinition(rValue));
    }

    @Override
    public Collection<Definition> getDefinitionsReferencedAt(Node node) {
      Definition def = definitions.get(node);
      if (def == null) {
        return null;
      }
      return java.util.Collections.singletonList(def);
    }

    private static class SimpleDefinition implements Definition {
      private final Node rValue;

      SimpleDefinition(Node rValue) {
        this.rValue = rValue;
      }

      @Override
      public Node getLValue() {
        return null;
      }

      @Override
      public Node getRValue() {
        return rValue;
      }
    }
  }
}