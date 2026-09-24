/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.NodeTraversal
 *
 * 1. Callback Subclasses & Predicates:
 *    - AbstractPostOrderCallback: shouldTraverse always returns true.
 *    - AbstractScopedCallback: default empty hooks (enterScope/exitScope), shouldTraverse true.
 *    - AbstractShallowCallback:
 *        * parent == null -> true
 *        * !parent.isFunction() -> true
 *        * parent.isFunction() && n == parent.getFirstChild() -> true (fn name)
 *        * parent.isFunction() && n != parent.getFirstChild() -> false (params / body skipped)
 *    - AbstractShallowStatementCallback:
 *        * parent == null -> true
 *        * NodeUtil.isControlStructure(parent) -> true (IF, WHILE, FOR, etc.)
 *        * NodeUtil.isStatementBlock(parent) -> true (BLOCK)
 *        * expression / other parent -> false
 *    - AbstractNodeTypePruningCallback:
 *        * Single-arg constructor (include = true)
 *        * Two-arg constructor (include = true vs include = false)
 *
 * 2. NodeTraversal Lifecycle & Scope Management:
 *    - traverse(Node root):
 *        * SCRIPT node: updates inputId and sourceName from AST attributes.
 *        * Non-SCRIPT node traversal.
 *        * Pruned traversal: callback returns false in shouldTraverse (short-circuits children).
 *    - traverseRoots(Node... / List<Node>):
 *        * Empty roots list early return.
 *        * Multi-roots with valid shared parent.
 *        * Roots with null parent -> triggers Preconditions check failure / throwUnexpectedException.
 *        * Roots with mismatched parent -> triggers Preconditions check failure.
 *    - traverseWithScope:
 *        * Global scope check (s.isGlobal() precondition).
 *    - traverseAtScope:
 *        * Function scope branch (n.isFunction() == true, sets inputId, sourceName, traverses args/body).
 *        * Non-function scope branch (calls traverseWithScope).
 *    - traverseInnerNode:
 *        * Preconditions.checkNotNull(parent) violation (null parent).
 *        * refinedScope == null or equal to current scope (direct branch traversal).
 *        * refinedScope != null && getScope() != refinedScope (pushScope -> traverse -> popScope).
 *    - Function Traversal (traverseFunction):
 *        * Function Declaration (parent is not function expr) -> fnName traversed in outer scope.
 *        * Function Expression (parent is VAR or ASSIGN) -> fnName traversed in function scope.
 *        * Incomplete function AST (child count != 3 or missing block) -> triggers Defect condition.
 *
 * 3. Scope Inspection & Queries:
 *    - getScope(): lazy materialization from scopeRoots deque via descendingIterator.
 *    - getScopeRoot(): scopeRoots non-empty vs empty (falls back to scopes.peek()).
 *    - getScopeDepth(): scopes.size() + scopeRoots.size().
 *    - hasScope(): empty vs non-empty.
 *    - inGlobalScope(): scopeDepth <= 1.
 *    - getEnclosingFunction(): scope count < 2 (returns null) vs function scope.
 *    - getControlFlowGraph(): lazy CFA generation and caching in cfgs stack.
 *
 * 4. Diagnostics & Source Info:
 *    - getLineNumber(): lazy upward traversal of curNode; handles line >= 0 and line < 0.
 *    - getSourceName(), getInput(), getModule(): resolution from inputId via Compiler.
 *    - report(), makeError(): diagnostic error creation overloads.
 *
 * 5. Defect-Targeted Zone (D4J Ground Truth - testIncompleteFunction):
 *    - Malformed/incomplete function nodes traversed without required children or block body.
 *    - Proper exception wrapping via throwUnexpectedException and internal error reporting.
 */

package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeTraversalGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTraverseScriptAndVisitOrder() {
    Node script = new Node(Token.SCRIPT);
    script.setInputId(new InputId("test.js"));
    script.setSourceFileName("test.js");
    script.setLineno(10);
    script.setCharno(2);

    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    varNode.setLineno(11);
    script.addChildToBack(varNode);

    final List<String> visited = new ArrayList<String>();
    NodeTraversal.Callback cb = new NodeTraversal.Callback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(Token.name(n.getType()));
        assertEquals("test.js", t.getSourceName());
        assertTrue(t.getLineNumber() > 0);
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(script);

    // Post-order: NAME, then VAR, then SCRIPT
    assertEquals(3, visited.size());
    assertEquals("NAME", visited.get(0));
    assertEquals("VAR", visited.get(1));
    assertEquals("SCRIPT", visited.get(2));
    assertFalse(t.hasScope());
    assertEquals(0, t.getScopeDepth());
  }

  @Test(timeout = 4000)
  public void testTraverseFunctionDeclarationVsExpression() {
    // Function declaration: parent is SCRIPT
    Node script = new Node(Token.SCRIPT);
    Node fnDecl = createFunctionNode("fnDecl", true);
    script.addChildToBack(fnDecl);

    // Function expression: parent is VAR
    Node fnExpr = createFunctionNode("fnExpr", true);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "myFn"));
    varNode.getFirstChild().addChildToBack(fnExpr);
    script.addChildToBack(varNode);

    final List<String> enteredScopes = new ArrayList<String>();
    final List<String> exitedScopes = new ArrayList<String>();

    NodeTraversal.ScopedCallback scb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        Node root = t.getScopeRoot();
        enteredScopes.add(root.isFunction() ? "FUNCTION" : Token.name(root.getType()));
      }

      @Override
      public void exitScope(NodeTraversal t) {
        Node root = t.getScopeRoot();
        exitedScopes.add(root.isFunction() ? "FUNCTION" : Token.name(root.getType()));
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, scb);
    t.traverse(script);

    // Should enter global (SCRIPT), function fnDecl, and function fnExpr scopes
    assertEquals(3, enteredScopes.size());
    assertEquals("SCRIPT", enteredScopes.get(0));
    assertEquals("FUNCTION", enteredScopes.get(1));
    assertEquals("FUNCTION", enteredScopes.get(2));

    assertEquals(3, exitedScopes.size());
  }

  @Test(timeout = 4000)
  public void testLazyScopeMaterializationAndEnclosingFunction() {
    Node script = new Node(Token.SCRIPT);
    final Node fn = createFunctionNode("foo", true);
    script.addChildToBack(fn);

    final List<Scope> scopesCaptured = new ArrayList<Scope>();
    final List<Node> enclosingFnCaptured = new ArrayList<Node>();
    final List<Boolean> globalChecks = new ArrayList<Boolean>();

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isReturn()) {
          scopesCaptured.add(t.getScope());
          enclosingFnCaptured.add(t.getEnclosingFunction());
          globalChecks.add(t.inGlobalScope());
          assertNotNull(t.getControlFlowGraph());
        }
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(script);

    assertEquals(1, scopesCaptured.size());
    assertNotNull(scopesCaptured.get(0));
    assertEquals(fn, enclosingFnCaptured.get(0));
    assertFalse(globalChecks.get(0));
  }

  @Test(timeout = 4000)
  public void testTraverseRootsNormal() {
    Node parent = new Node(Token.BLOCK);
    Node child1 = new Node(Token.EXPR_RESULT, new Node(Token.TRUE));
    Node child2 = new Node(Token.EXPR_RESULT, new Node(Token.FALSE));
    parent.addChildToBack(child1);
    parent.addChildToBack(child2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {
        visited.add(n);
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverseRoots(child1, child2);

    assertTrue(visited.contains(child1));
    assertTrue(visited.contains(child2));
    assertFalse(visited.contains(parent));
  }

  @Test(timeout = 4000)
  public void testTraverseWithScopeAndTraverseAtScope() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node script = new Node(Token.SCRIPT);
    Node fn = createFunctionNode("target", true);
    script.addChildToBack(fn);

    Scope globalScope = creator.createScope(script, null);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb, creator);
    t.traverseWithScope(script, globalScope);
    assertTrue(visited.contains(script));

    visited.clear();
    Scope fnScope = creator.createScope(fn, globalScope);
    t.traverseAtScope(fnScope);
    assertFalse(visited.isEmpty());
  }

  @Test(timeout = 4000)
  public void testTraverseInnerNodeWithRefinedScope() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);

    Scope globalScope = creator.createScope(script, null);
    Scope refinedScope = creator.createScope(block, globalScope);

    final List<Scope> seenScopes = new ArrayList<Scope>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        seenScopes.add(t.getScope());
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb, creator);
    t.traverseWithScope(script, globalScope);

    // Call traverseInnerNode with refinedScope
    t.traverseInnerNode(block, script, refinedScope);
    assertTrue(seenScopes.contains(refinedScope));

    // Call traverseInnerNode with same scope (no push)
    t.traverseInnerNode(block, script, null);
  }

  @Test(timeout = 4000)
  public void testStaticTraversalConvenienceMethods() {
    Node script = new Node(Token.SCRIPT);
    final int[] count = new int[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        count[0]++;
      }
    };

    NodeTraversal.traverse(compiler, script, cb);
    assertEquals(1, count[0]);

    Node parent = new Node(Token.BLOCK);
    Node c1 = new Node(Token.EMPTY);
    Node c2 = new Node(Token.EMPTY);
    parent.addChildToBack(c1);
    parent.addChildToBack(c2);

    count[0] = 0;
    NodeTraversal.traverseRoots(compiler, cb, c1, c2);
    assertEquals(2, count[0]);

    count[0] = 0;
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(c1, c2), cb);
    assertEquals(2, count[0]);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testTraverseRootsEmptyList() {
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });

    List<Node> empty = Collections.emptyList();
    t.traverseRoots(empty);
    assertFalse(t.hasScope());
    assertEquals(0, t.getScopeDepth());
  }

  @Test(timeout = 4000)
  public void testLineNumberResolutionWalksUpHierarchy() {
    Node root = new Node(Token.SCRIPT);
    root.setLineno(42);

    Node child = new Node(Token.EXPR_RESULT);
    child.setLineno(-1); // Unknown line number
    root.addChildToBack(child);

    final int[] recordedLine = new int[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isExprResult()) {
          recordedLine[0] = t.getLineNumber();
        }
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);
    assertEquals(42, recordedLine[0]);
  }

  @Test(timeout = 4000)
  public void testLineNumberWhenCompletelyUnknown() {
    Node root = new Node(Token.BLOCK);
    root.setLineno(-1);

    final int[] recordedLine = new int[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        recordedLine[0] = t.getLineNumber();
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);
    assertEquals(0, recordedLine[0]);
  }

  @Test(timeout = 4000)
  public void testAbstractShallowCallbackBoundaries() {
    NodeTraversal.AbstractShallowCallback shallow = new NodeTraversal.AbstractShallowCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    };

    Node dummy = new Node(Token.NAME);
    // Boundary 1: parent == null -> should traverse
    assertTrue(shallow.shouldTraverse(null, dummy, null));

    // Boundary 2: parent is not a function -> should traverse
    Node blockParent = new Node(Token.BLOCK);
    assertTrue(shallow.shouldTraverse(null, dummy, blockParent));

    // Boundary 3: parent is function, n IS firstChild (fn name) -> should traverse
    Node fn = createFunctionNode("f", false);
    Node nameChild = fn.getFirstChild();
    assertTrue(shallow.shouldTraverse(null, nameChild, fn));

    // Boundary 4: parent is function, n IS NOT firstChild (params or body) -> should NOT traverse
    Node paramList = nameChild.getNext();
    assertFalse(shallow.shouldTraverse(null, paramList, fn));
  }

  @Test(timeout = 4000)
  public void testAbstractShallowStatementCallbackBoundaries() {
    NodeTraversal.AbstractShallowStatementCallback shallowStmt =
        new NodeTraversal.AbstractShallowStatementCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {}
        };

    Node dummy = new Node(Token.NAME);
    // Boundary 1: parent == null -> true
    assertTrue(shallowStmt.shouldTraverse(null, dummy, null));

    // Boundary 2: control structure (IF) -> true
    Node ifNode = new Node(Token.IF);
    assertTrue(shallowStmt.shouldTraverse(null, dummy, ifNode));

    // Boundary 3: statement block (BLOCK) -> true
    Node blockNode = new Node(Token.BLOCK);
    assertTrue(shallowStmt.shouldTraverse(null, dummy, blockNode));

    // Boundary 4: expression (ADD) -> false
    Node exprNode = new Node(Token.ADD);
    assertFalse(shallowStmt.shouldTraverse(null, dummy, exprNode));
  }

  @Test(timeout = 4000)
  public void testAbstractNodeTypePruningCallbackBoundaries() {
    Set<Integer> pruneSet = ImmutableSet.of(Token.VAR, Token.FUNCTION);

    // Case 1: include = true (pruning keeps only specified types)
    NodeTraversal.AbstractNodeTypePruningCallback inclusion =
        new NodeTraversal.AbstractNodeTypePruningCallback(pruneSet) {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {}
        };
    assertTrue(inclusion.shouldTraverse(null, new Node(Token.VAR), null));
    assertFalse(inclusion.shouldTraverse(null, new Node(Token.IF), null));

    // Case 2: include = false (pruning excludes specified types)
    NodeTraversal.AbstractNodeTypePruningCallback exclusion =
        new NodeTraversal.AbstractNodeTypePruningCallback(pruneSet, false) {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {}
        };
    assertFalse(exclusion.shouldTraverse(null, new Node(Token.VAR), null));
    assertTrue(exclusion.shouldTraverse(null, new Node(Token.IF), null));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (D4J Ground Truth)
  // =========================================================================

  /**
   * Targets com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction
   * An incomplete function AST (missing parameters or body) must cause traverseFunction
   * to fail preconditions and be handled by throwUnexpectedException.
   */
  @Test(timeout = 4000)
  public void testDefectIncompleteFunctionAstThrowsInternalCompilerError() {
    Node script = new Node(Token.SCRIPT);
    script.setInputId(new InputId("incomplete.js"));
    script.setSourceFileName("incomplete.js");

    // Construct an incomplete function node with only 2 children instead of 3
    Node incompleteFn = new Node(Token.FUNCTION);
    incompleteFn.addChildToBack(Node.newString(Token.NAME, "incomplete"));
    incompleteFn.addChildToBack(new Node(Token.PARAM_LIST));
    // Missing Token.BLOCK body!
    script.addChildToBack(incompleteFn);

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });

    try {
      t.traverse(script);
      fail("Expected RuntimeException due to AST invariant failure on incomplete function.");
    } catch (RuntimeException expected) {
      // Must wrap as internal error
      assertTrue(expected.getMessage().contains("INTERNAL COMPILER ERROR")
          || expected.getMessage().contains("Preconditions"));
    }
  }

  @Test(timeout = 4000)
  public void testDefectFunctionWithNonBlockBodyThrows() {
    Node script = new Node(Token.SCRIPT);
    Node malformedFn = new Node(Token.FUNCTION);
    malformedFn.addChildToBack(Node.newString(Token.NAME, "badBody"));
    malformedFn.addChildToBack(new Node(Token.PARAM_LIST));
    // Third child is EXPR_RESULT instead of BLOCK
    malformedFn.addChildToBack(new Node(Token.EXPR_RESULT));
    script.addChildToBack(malformedFn);

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });

    try {
      t.traverse(script);
      fail("Expected RuntimeException because function body is not a BLOCK.");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("INTERNAL COMPILER ERROR")
          || expected.getMessage().contains("Preconditions"));
    }
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testTraverseRootsWithNullParentThrowsInternalError() {
    Node rootWithoutParent = new Node(Token.EXPR_RESULT);
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });

    try {
      t.traverseRoots(rootWithoutParent);
      fail("Expected RuntimeException when root has null parent.");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("INTERNAL COMPILER ERROR")
          || expected.getMessage().contains("Preconditions"));
    }
  }

  @Test(timeout = 4000)
  public void testTraverseRootsWithMismatchedParentThrows() {
    Node parent1 = new Node(Token.BLOCK);
    Node parent2 = new Node(Token.BLOCK);
    Node child1 = new Node(Token.EXPR_RESULT);
    Node child2 = new Node(Token.EXPR_RESULT);
    parent1.addChildToBack(child1);
    parent2.addChildToBack(child2);

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });

    try {
      t.traverseRoots(child1, child2);
      fail("Expected RuntimeException when roots have differing parents.");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("INTERNAL COMPILER ERROR")
          || expected.getMessage().contains("Preconditions"));
    }
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testTraverseInnerNodeWithNullParentThrowsNpe() {
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });
    t.traverseInnerNode(new Node(Token.EMPTY), null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTraverseWithScopeNonGlobalThrows() {
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Node script = new Node(Token.SCRIPT);
    Node fn = createFunctionNode("f", true);
    script.addChildToBack(fn);

    Scope globalScope = creator.createScope(script, null);
    Scope localScope = creator.createScope(fn, globalScope);

    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });

    // Should fail checkState(s.isGlobal())
    t.traverseWithScope(fn, localScope);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & State Inspection Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetCompilerAndEnclosingFunctionNullRoot() {
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {}
    });

    assertEquals(compiler, t.getCompiler());
    assertNull(t.getEnclosingFunction());
    assertEquals("", t.getSourceName());
    assertNull(t.getCurrentNode());
    assertNull(t.getInput());
    assertNull(t.getModule());
    assertNull(t.getInputId());
  }

  @Test(timeout = 4000)
  public void testDiagnosticCreationAndReporting() {
    final Node dummy = new Node(Token.EMPTY);
    dummy.setLineno(5);
    dummy.setCharno(10);

    final JSError[] createdError = new JSError[2];
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node p) {
        createdError[0] = t.makeError(n, CheckLevel.WARNING, NodeTraversal.NODE_TRAVERSAL_ERROR, "msg1");
        createdError[1] = t.makeError(n, NodeTraversal.NODE_TRAVERSAL_ERROR, "msg2");
        t.report(n, NodeTraversal.NODE_TRAVERSAL_ERROR, "reported");
      }
    });

    Node script = new Node(Token.SCRIPT, dummy);
    t.traverse(script);

    assertNotNull(createdError[0]);
    assertEquals(CheckLevel.WARNING, createdError[0].level);
    assertNotNull(createdError[1]);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPruningCallbackShortCircuitsTraversal() {
    Node root = new Node(Token.BLOCK);
    Node child = new Node(Token.EXPR_RESULT, new Node(Token.NAME));
    root.addChildToBack(child);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.Callback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        // Reject all children of BLOCK
        return parent == null;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);

    // Only root is visited; child was pruned
    assertEquals(1, visited.size());
    assertEquals(root, visited.get(0));
  }

  @Test(timeout = 4000)
  public void testAbstractScopedCallbackDefaultHooksDoNotThrow() {
    NodeTraversal.AbstractScopedCallback defaultScoped =
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {}
        };

    NodeTraversal t = new NodeTraversal(compiler, defaultScoped);
    Node script = new Node(Token.SCRIPT);
    // Should execute default enterScope / exitScope without exceptions
    t.traverse(script);
  }

  // =========================================================================
  // Helper Methods
  // =========================================================================

  private static Node createFunctionNode(String name, boolean withBody) {
    Node fn = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, name);
    Node paramList = new Node(Token.PARAM_LIST);
    Node body = new Node(Token.BLOCK);
    if (withBody) {
      body.addChildToBack(new Node(Token.RETURN, Node.newString(Token.NAME, "x")));
    }
    fn.addChildToBack(fnName);
    fn.addChildToBack(paramList);
    fn.addChildToBack(body);
    return fn;
  }
}