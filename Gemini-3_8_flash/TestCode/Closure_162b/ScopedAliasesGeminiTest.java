package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * ===================================================================================================
 * Target Class: com.google.javascript.jscomp.ScopedAliases
 * Test Suite  : ScopedAliasesGeminiTest
 *
 * Branch & Defect Coverage Map:
 * ---------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 * - testBasicAliasExpansion: Replacement of single alias with qualified name in expressions.
 * - testTransitiveAliases: Expansion of chained aliases (e.g., var g = goog; var d = g.dom;).
 * - testMultipleAliasesInSingleVar: Detachment of individual alias from multi-declaration VAR node.
 * - testMultipleScopesInScript: Isolation and clearance of aliases across consecutive goog.scope calls.
 * - testJsDocTypeAliased: Resolution and rewriting of JSDoc type annotation referencing alias.
 * - testJsDocTypeWithoutDot: JSDoc type name with no '.' boundary (endIndex == -1 path).
 * - testJsDocNestedType: Deep recursion across composite/generic type nodes (fixTypeNode child loop).
 * - testInnerFunctionCanUseReturnThisThrow: Scope depth >= 3 allowing return, this, and throw safely.
 * - testShadowedAliasInInnerScope: Name shadowing preventing illegal alias expansion in nested scopes.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - testNoScopeNoChange: Non-scoped standard JavaScript AST leaves code intact and reports no changes.
 * - testEmptyScope: goog.scope block with empty body merges successfully into parent.
 * - testGlobalFunctionNotTraversed: Normal global functions skipped by shouldTraverse.
 * - testPreprocessorSymbolTableReferenceAdded: Integration with PreprocessorSymbolTable reference tracking.
 * - testAliasTransformationHandlerCalled: Verify AliasTransformationHandler and AliasTransformation hooks.
 * - testHotSwapScriptDirectCall: Direct invocation of hotSwapScript contract.
 *
 * Partition C: Defect-Targeted Branch Zone (Closure Defect: testForwardJsDoc)
 * - testForwardJsDoc: CRITICAL DEFECT TARGET. Forward-referenced JSDoc types where the type annotation
 *   precedes the var alias definition within the goog.scope block. In the defective implementation,
 *   the post-order visit does not yet have the alias registered when visiting the forward JSDoc,
 *   leaving the unexpanded type string (e.g., "Foo.Bar" instead of "foo.Foo.Bar").
 *
 * Partition D: Diagnostic & Error Guard Paths
 * - testErrorScopeUsedImproperly: Scope call nested in non-expression node (var x = goog.scope(...)).
 * - testErrorScopeZeroParams: goog.scope call with 0 arguments.
 * - testErrorScopeMultipleParams: goog.scope call with >1 arguments.
 * - testErrorScopeNonFunctionParam: goog.scope called with a non-function argument (e.g., string literal).
 * - testErrorScopeNamedFunctionParam: goog.scope called with named function expression.
 * - testErrorScopeFunctionWithParameters: goog.scope called with function taking parameters.
 * - testErrorScopeReferencesThis: Direct usage of 'this' at goog.scope root depth.
 * - testErrorScopeUsesReturn: Direct usage of 'return' at goog.scope root depth.
 * - testErrorScopeUsesThrow: Direct usage of 'throw' at goog.scope root depth.
 * - testErrorScopeNonAliasLocalUninitialized: Uninitialized local variable at root depth.
 * - testErrorScopeNonAliasLocalExpression: Local variable assigned a non-qualified-name expression.
 * - testErrorScopeAliasRedefined: Re-assignment to an alias name inside the scope.
 * - testErrorPreventsAliasApplication: hasErrors guard aborts AST transformation and preserves scope.
 * ===================================================================================================
 */

import com.google.common.collect.Lists;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ScopedAliasesGeminiTest {

  // =========================================================================
  // Helper Methods for AST Navigation & Verification
  // =========================================================================

  private Node findFirstNodeWithJSDoc(Node root) {
    if (root.getJSDocInfo() != null) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNodeWithJSDoc(child);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private Node findTypeStringNode(Node typeNode) {
    if (typeNode.getType() == Token.STRING) {
      return typeNode;
    }
    for (Node child = typeNode.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findTypeStringNode(child);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private void findAllTypeStringNodes(Node typeNode, List<Node> result) {
    if (typeNode.getType() == Token.STRING) {
      result.add(typeNode);
    }
    for (Node child = typeNode.getFirstChild(); child != null; child = child.getNext()) {
      findAllTypeStringNodes(child, result);
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defect: testForwardJsDoc)
  // =========================================================================

  @Test(timeout = 4000)
  public void testForwardJsDoc() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n"
        + "  /** @type {Foo.Bar} */ var x;\n"
        + "  var Foo = foo.Foo;\n"
        + "});\n";
    Node root = compiler.parseTestCode(js);
    ScopedAliases sa = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    sa.process(null, root);

    Node nodeWithJsDoc = findFirstNodeWithJSDoc(root);
    assertNotNull("Node with JSDocInfo must exist in the AST", nodeWithJsDoc);

    JSDocInfo info = nodeWithJsDoc.getJSDocInfo();
    assertNotNull("JSDocInfo must be present on node", info);

    Collection<Node> typeNodes = Lists.newArrayList(info.getTypeNodes());
    assertFalse("JSDocInfo must contain type nodes", typeNodes.isEmpty());

    Node stringNode = findTypeStringNode(typeNodes.iterator().next());
    assertNotNull("Type string node must exist within type nodes", stringNode);

    // Defects4J failure: In the buggy implementation, "Foo.Bar" is not transformed
    // to "foo.Foo.Bar" because the alias is encountered after the JSDoc reference.
    assertEquals("foo.Foo.Bar", stringNode.getString());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicAliasExpansion() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom.createElement('div');\n"
        + "});\n";
    Node root = compiler.parseTestCode(js);
    ScopedAliases sa = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    sa.process(null, root);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue("Should expand alias to goog.dom.createElement",
        source.contains("goog.dom.createElement"));
    assertFalse("Alias declaration should be detached", source.contains("var dom ="));
  }

  @Test(timeout = 4000)
  public void testTransitiveAliases() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n"
        + "  var g = goog;\n"
        + "  var d = g.dom;\n"
        + "  d.createElement('div');\n"
        + "});\n";
    Node root = compiler.parseTestCode(js);
    ScopedAliases sa = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    sa.process(null, root);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue("Should resolve transitive alias chain to goog.dom.createElement",
        source.contains("goog.dom.createElement"));
  }

  @Test(timeout = 4000)
  public void testMultipleAliasesInSingleVar() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n"
        + "  var d = goog.dom, b = goog.base;\n"
        + "  d.create();\n"
        + "  b();\n"
        + "});\n";
    Node root = compiler.parseTestCode(js);
    ScopedAliases sa = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    sa.process(null, root);

    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("goog.dom.create()"));
    assertTrue(source.contains("goog.base()"));
    assertFalse(source.contains("var d"));
    assertFalse(source.contains("var b"));
  }

  @Test(timeout = 4000)
  public void testMultipleScopesInScript() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n"
        + "  var d = goog.dom;\n"
        + "  d.create();\n"
        + "});\n"
        + "goog.scope(function() {\n"
        + "  var b = goog.base;\n"
        + "  b();\n"
        + "});\n";
    Node root = compiler.parseTestCode(js);
    ScopedAliases sa = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_