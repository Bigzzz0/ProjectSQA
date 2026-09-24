package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Test suite for InlineObjectLiterals pass.
 * Targets maximum line/branch coverage and the known defect
 * regarding deleted properties.
 *
 * [Branch & Defect Analysis Matrix]
 * - isVarInlineForbidden: global, extern, exported, stale vars
 * - isInlinableObject: getprop references, call targets, delete, self-ref, getters/setters
 * - splitObject: defined vs undefined, lvalue assignments, var declarations
 * - replaceAssignmentExpression: empty nodes, comma tree construction
 * - computeVarList: lvalue, initializing declaration, getprop references
 * - fillInitialValues: object literal keys
 * - blacklistVarReferencesInTree: name nodes
 * - Known defect: testNoInlineDeletedProperties – property deletion should prevent inlining
 */
public class InlineObjectLiteralsDeepseekTest {

  private static final String EXTERNS = "var window;";

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    // Enable the pass
    options.setInlineObjectLiterals(true);
    compiler.initOptions(options);
    return compiler;
  }

  private Node parseAndRun(String js) {
    Compiler compiler = createCompiler();
    SourceFile input = SourceFile.fromCode("test", js);
    compiler.compile(
        SourceFile.fromCode("externs", EXTERNS),
        input);
    return compiler.getRoot(); // returns the AST root
  }

  // Helper to get the last script node
  private Node getScript(Node root) {
    return root.getLastChild();
  }

  // Helper to count variable declarations
  private int countVarDeclarations(Node script) {
    int count = 0;
    for (Node child = script.getFirstChild(); child != null; child = child.getNext()) {
      if (child.isVar()) count++;
    }
    return count;
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testSimpleObjectLiteralInlined() {
    String js = "function f() { var x = {a: 1, b: 2}; return x.a + x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // After inlining, there should be no object literal assignment, but individual vars.
    // The function body should contain var declarations for a and b.
    // We check that the original var x is gone and new vars appear.
    // This is a basic sanity test.
    // We can also check that the return uses names directly.
    // For simplicity, we just ensure no object literal node remains.
    assertFalse("Object literal should have been inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  private boolean containsNodeOfType(Node node, int type) {
    if (node.getType() == type) return true;
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      if (containsNodeOfType(child, type)) return true;
    }
    return false;
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMultipleReferences() {
    String js = "function f() { var x = {a: 1, b: 2}; x.a = 3; return x.a + x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // Should still inline because x is only assigned once.
    assertFalse("Object literal should be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralUsedAsCallTarget() {
    // If x is used as a call target (x.fn()), it should NOT be inlined.
    String js = "function f() { var x = {fn: function() {}}; x.fn(); }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The object literal should remain because the call target uses 'this'.
    assertTrue("Object literal should NOT be inlined when used as call target",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGetter() {
    // ES5 getters should prevent inlining.
    String js = "function f() { var x = {get a() { return 1; }}; return x.a; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal with getter should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithSetter() {
    String js = "function f() { var x = {set a(v) {}}; x.a = 1; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal with setter should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  // ==================== Partition B: Boundary Value Analysis ====================

  @Test(timeout = 4000)
  public void testEmptyObjectLiteral() {
    String js = "function f() { var x = {}; return x; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // Empty object literal should be replaced with true (since no properties)
    // The var x should be removed.
    assertFalse("Empty object literal should be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
    // There should be no var declaration for x.
    // The return should be a name reference to something? Actually x is replaced by true.
    // But the pass replaces the assignment with true, and x is no longer used.
    // So the return might become 'return true;' or something.
    // We just check no object literal.
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNullProperty() {
    String js = "function f() { var x = {a: null}; return x.a; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertFalse("Object literal with null should be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithUndefinedProperty() {
    String js = "function f() { var x = {a: undefined}; return x.a; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertFalse("Object literal with undefined should be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithNestedObject() {
    // Nested object literals should not be inlined (only top-level)
    String js = "function f() { var x = {a: {b: 1}}; return x.a.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The outer object literal should be inlined, but the inner remains.
    // Actually the pass only inlines the top-level object literal, not nested ones.
    // So after inlining, there should be a var for a, and its value is the inner object literal.
    // So we should still see an OBJECTLIT for the inner.
    assertTrue("Inner object literal should remain",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================

  @Test(timeout = 4000)
  public void testNoInlineDeletedProperties() {
    // Known defect: property deletion should prevent inlining of that property.
    // The pass should not inline if a property is deleted.
    String js = "function f() { var x = {a: 1, b: 2}; delete x.a; return x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The object literal should NOT be inlined because the delete operation
    // changes the semantics (the property 'a' is removed).
    // The pass's isInlinableObject should return false when it sees a delete.
    // However, the current implementation might not handle delete correctly.
    // We assert that the object literal remains.
    assertTrue("Object literal should NOT be inlined when a property is deleted",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testNoInlineDeletedPropertyViaComputed() {
    // Delete with computed property name (ES6) – but we test simple case.
    String js = "function f() { var x = {a: 1}; delete x['a']; return x.a; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal should NOT be inlined when property deleted via bracket",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testSelfReferentialObjectLiteral() {
    // Self-referential assignment should prevent inlining.
    String js = "function f() { var x = {a: 1, b: x.a}; return x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Self-referential object literal should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithExportedVar() {
    // Exported variables should not be inlined.
    String js = "var x = {a: 1}; window['x'] = x;";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // x is exported via window['x'], so it should not be inlined.
    assertTrue("Exported variable should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithGlobalVar() {
    // Global variables are excluded from inlining.
    String js = "var x = {a: 1};";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Global variable should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testObjectLiteralWithNoAssignment() {
    // Variable declared but not assigned (var x;)
    String js = "function f() { var x; return x; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // No object literal, nothing to inline.
    assertFalse("No object literal present",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithMultipleAssignments() {
    // Variable assigned more than once should not be inlined.
    String js = "function f() { var x = {a: 1}; x = {b: 2}; return x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The first assignment is an object literal, but the second assignment
    // makes the variable not inlinable (multiple assignments).
    assertTrue("Multiple assignments should prevent inlining",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithPropertyReferenceNotDefined() {
    // If a property is referenced that is not defined in the object literal,
    // the pass should bail out.
    String js = "function f() { var x = {a: 1}; return x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The property 'b' is not defined, so the pass should not inline.
    assertTrue("Property not defined should prevent inlining",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithPropertyReferenceAfterAssignment() {
    // If a property is assigned later, it's okay as long as it's defined.
    String js = "function f() { var x = {a: 1}; x.b = 2; return x.a + x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The property 'b' is assigned later, but it's not in the initial literal.
    // The pass should see that 'b' is not in validProperties initially,
    // but the assignment to x.b is a simple assign LHS, so it adds 'b' to validProperties.
    // So it should be inlinable.
    assertFalse("Object literal should be inlined when property added later",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testObjectLiteralUsedInExpressionStatement() {
    // Object literal assigned and then used in expression statement (e.g., x.a;)
    String js = "function f() { var x = {a: 1}; x.a; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // Should be inlined.
    assertFalse("Object literal should be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithFunctionCall() {
    // If the object literal is passed to a function, it should not be inlined
    // because the function might modify it.
    String js = "function f() { var x = {a: 1}; g(x); }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    // The variable x is used in a call, so it's not inlinable.
    assertTrue("Object literal passed to function should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithForInLoop() {
    // If the object is used in a for-in loop, it should not be inlined.
    String js = "function f() { var x = {a: 1}; for (var k in x) {} }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal used in for-in should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralWithTypeof() {
    // typeof x should not prevent inlining? Actually typeof x is a reference to x,
    // but it's not a getprop. The pass treats it as a direct reference.
    // If x is only used in typeof, it might still be inlinable? But the pass
    // requires that the variable is only assigned once and used in getprop or var/assign.
    // typeof x is a Name reference, not a getprop, so isInlinableObject will return false
    // because it's not a var or assign expr LHS.
    String js = "function f() { var x = {a: 1}; return typeof x; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal used in typeof should NOT be inlined",
        containsNodeOfType(script, Token.OBJECTLIT));
  }

  // Additional test for the known defect: ensure that the pass does not inline
  // when a property is deleted even if the variable is otherwise inlinable.
  @Test(timeout = 4000)
  public void testNoInlineDeletedPropertiesWithMultipleRefs() {
    String js = "function f() { var x = {a: 1, b: 2}; delete x.a; x.b = 3; return x.b; }";
    Node root = parseAndRun(js);
    Node script = getScript(root);
    assertTrue("Object literal should NOT be inlined when a property is deleted",
        containsNodeOfType(script, Token.OBJECTLIT));
  }
}