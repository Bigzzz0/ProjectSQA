package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class NodeTraversalDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Decision Branches targeted:
     * - traverseRoots: empty roots (returns early) -> testEmptyRoots
     * - traverseFunction: child count != 3 -> triggers IllegalStateException -> internal error -> testIncompleteFunction
     * - traverseBranch: Token.SCRIPT -> sets inputId/sourceName
     * - traverseBranch: Token.FUNCTION -> calls traverseFunction
     * - shouldTraverse returning false -> skip children (test via ShallowCallback)
     * - pushScope(Node) vs pushScope(Scope): both branches in popScope and getScope
     * - getScope: when scopeRoots not empty, iterate and create scopes (test via ScopedCallback)
     * - getEnclosingFunction: scopes.size+scopeRoots.size < 2 -> null; else peek scopeRoots or scopes
     * - getControlFlowGraph: if null, run ControlFlowAnalysis
     * - getLineNumber: recurse to parent if current line < 0
     * 
     * Defect trigger: FUNCTION node with < 3 children leads to Preconditions failure,
     * which is caught and wrapped as internal error. Test verifies RuntimeException with "INTERNAL COMPILER ERROR".
     */

    // Helper to create a simple compiler for tests
    private Compiler createCompiler() {
        return new Compiler();
    }

    // Helper to create a simple script node with a block and an expression
    private Node createSimpleScriptTree() {
        Node script = new Node(Token.SCRIPT);
        script.setInputId(new InputId("test.js"));
        script.setSourceFileName("test.js");
        Node block = new Node(Token.BLOCK);
        Node expr = new Node(Token.EXPR_RESULT);
        block.addChildToBack(expr);
        script.addChildToBack(block);
        return script;
    }

    // Helper callback that records visited nodes
    private static class RecordingCallback implements NodeTraversal.Callback {
        final StringBuilder visited = new StringBuilder();

        @Override
        public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return true;
        }

        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
            visited.append(n.getType()).append(",");
        }
    }

    @Test(timeout = 4000)
    public void testBasicTraversalWithPostOrderCallback() {
        Compiler compiler = createCompiler();
        Node root = createSimpleScriptTree();
        RecordingCallback cb = new RecordingCallback();
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverse(root);
        // Expect post-order: EXPR_RESULT, BLOCK, SCRIPT
        assertEquals(Token.EXPR_RESULT + "," + Token.BLOCK + "," + Token.SCRIPT + ",", cb.visited.toString());
    }

    @Test(timeout = 4000)
    public void testTraversalWithScopedCallback() {
        Compiler compiler = createCompiler();
        Node root = createSimpleScriptTree();
        final StringBuilder scopeEvents = new StringBuilder();
        NodeTraversal.ScopedCallback scoped = new NodeTraversal.ScopedCallback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // no-op
            }

            @Override
            public void enterScope(NodeTraversal t) {
                scopeEvents.append("enter,");
            }

            @Override
            public void exitScope(NodeTraversal t) {
                scopeEvents.append("exit,");
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, scoped);
        t.traverse(root);
        // One global scope: enter then exit
        assertEquals("enter,exit,", scopeEvents.toString());
    }

    @Test(timeout = 4000)
    public void testTraversalWithShallowCallback() {
        Compiler compiler = createCompiler();
        // Create a function node with proper structure
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        function.addChildrenToFront(name);
        function.addChildAfter(params, name);
        function.addChildAfter(body, params);
        // Put function inside a script
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(function);
        script.setInputId(new InputId("test.js"));

        final StringBuilder visited = new StringBuilder();
        NodeTraversal.Callback shallow = new NodeTraversal.AbstractShallowCallback() {
            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                visited.append(n.getType()).append(",");
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, shallow);
        t.traverse(script);
        // Should visit: SCRIPT, FUNCTION, NAME (first child) but not traverse arguments or body
        assertTrue(visited.toString().contains(Integer.toString(Token.FUNCTION)));
        assertTrue(visited.toString().contains(Integer.toString(Token.NAME)));
        // Body and params should not be visited because shouldTraverse returns false for them
        assertFalse(visited.toString().contains(Integer.toString(Token.BLOCK)));
        assertFalse(visited.toString().contains(Integer.toString(Token.PARAM_LIST)));
    }

    @Test(timeout = 4000)
    public void testTraversalWithShallowStatementCallback() {
        Compiler compiler = createCompiler();
        Node script = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        Node expr = new Node(Token.EXPR_RESULT);
        block.addChildToBack(expr);
        script.addChildToBack(block);

        final StringBuilder visited = new StringBuilder();
        NodeTraversal.Callback stmtCb = new NodeTraversal.AbstractShallowStatementCallback() {
            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                visited.append(n.getType()).append(",");
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, stmtCb);
        t.traverse(script);
        // Should only visit structure/statement nodes: SCRIPT, BLOCK (which is a statement block), and maybe EXPR_RESULT? Actually EXPR_RESULT is control structure? NodeUtil.isControlStructure returns false for it. So only SCRIPT and BLOCK.
        assertTrue(visited.toString().contains(Integer.toString(Token.SCRIPT)));
        assertTrue(visited.toString().contains(Integer.toString(Token.BLOCK)));
        // EXPR_RESULT should not be visited because parent is BLOCK (statement block) but isExpression? It's a statement node, but AbstractShallowStatementCallback only goes into control structure or statement block parents. The child of BLOCK is EXPR_RESULT which is not a control structure, so shouldTraverse might return false? Actually the callback's shouldTraverse returns true if parent is null or NodeUtil.isControlStructure(parent) or NodeUtil.isStatementBlock(parent). For BLOCK, isStatementBlock returns true, so it will traverse into EXPR_RESULT. But EXPR_RESULT itself is not a control structure, so when visiting its children, parent is EXPR_RESULT which is not a control structure nor statement block, so traversal stops. So we should see EXPR_RESULT visited. Let's check: will visit visit for EXPR_RESULT? Yes, because it is visited in post order after children (none). So visited should include EXPR_RESULT. So the test should include it.
        assertTrue(visited.toString().contains(Integer.toString(Token.EXPR_RESULT)));
    }

    @Test(timeout = 4000)
    public void testTraversalWithNodeTypePruningCallbackInclude() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        java.util.Set<Integer> includeTypes = java.util.Collections.singleton(Token.BLOCK);
        NodeTraversal.Callback pruningCb = new NodeTraversal.AbstractNodeTypePruningCallback(includeTypes, true) {
            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // no-op
            }
        };
        final StringBuilder traversed = new StringBuilder();
        NodeTraversal.Callback recording = new RecordingCallback();
        // We can't easily see which nodes are traversed; override shouldTraverse to track? Use a wrapper.
        // Instead, we can test that shouldTraverse returns true only for BLOCK nodes.
        NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                boolean should = pruningCb.shouldTraverse(t, n, parent);
                if (should) {
                    traversed.append(n.getType()).append(",");
                }
                return should;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // no-op
            }
        });
        t.traverse(script);
        // Only BLOCK should be traversed (children visited)
        assertEquals(Token.BLOCK + ",", traversed.toString());
    }

    @Test(timeout = 4000)
    public void testTraversalWithNodeTypePruningCallbackExclude() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        java.util.Set<Integer> excludeTypes = java.util.Collections.singleton(Token.BLOCK);
        NodeTraversal.Callback pruningCb = new NodeTraversal.AbstractNodeTypePruningCallback(excludeTypes, false) {
            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // no-op
            }
        };
        final StringBuilder traversed = new StringBuilder();
        NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                boolean should = pruningCb.shouldTraverse(t, n, parent);
                if (should) {
                    traversed.append(n.getType()).append(",");
                }
                return should;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // no-op
            }
        });
        t.traverse(script);
        // Should traverse everything except BLOCK: SCRIPT and EXPR_RESULT
        assertTrue(traversed.toString().contains(Integer.toString(Token.SCRIPT)));
        assertTrue(traversed.toString().contains(Integer.toString(Token.EXPR_RESULT)));
        assertFalse(traversed.toString().contains(Integer.toString(Token.BLOCK)));
    }

    @Test(timeout = 4000)
    public void testTraverseRootsMultipleRoots() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        Node root1 = script.getFirstChild(); // block
        Node root2 = script.getFirstChild().getFirstChild(); // expr
        // traverseRoots expects all roots to have same parent
        final StringBuilder visited = new StringBuilder();
        NodeTraversal.Callback cb = new RecordingCallback();
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverseRoots(java.util.Arrays.asList(root1, root2));
        // Should traverse block and expr under the same scope (the script's parent is null? Actually scopeRoot is set to roots.get(0).getParent() which is script)
        // Visited order: block, expr
        String result = cb.visited.toString();
        assertTrue(result.contains(Integer.toString(Token.BLOCK)));
        assertTrue(result.contains(Integer.toString(Token.EXPR_RESULT)));
    }

    @Test(timeout = 4000)
    public void testTraverseRootsEmptyList() {
        Compiler compiler = createCompiler();
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        // Should not throw
        t.traverseRoots(new java.util.ArrayList<Node>());
        // No exception is good
    }

    @Test(timeout = 4000)
    public void testGetScopeWhenScopeRootsNotEmpty() {
        Compiler compiler = createCompiler();
        Node root = createSimpleScriptTree();
        final NodeTraversal[] trap = new NodeTraversal[1];
        NodeTraversal.ScopedCallback scoped = new NodeTraversal.ScopedCallback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                // inside traversal, call getScope() to trigger creation
                trap[0] = t;
                t.getScope(); // this should create scope from scopeRoots
            }

            @Override
            public void enterScope(NodeTraversal t) {}
            @Override
            public void exitScope(NodeTraversal t) {}
        };
        NodeTraversal t = new NodeTraversal(compiler, scoped);
        t.traverse(root);
        assertNotNull(trap[0].getScope());
    }

    @Test(timeout = 4000)
    public void testGetEnclosingFunctionAtGlobalScopeReturnsNull() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        t.traverse(script);
        assertNull(t.getEnclosingFunction());
    }

    @Test(timeout = 4000)
    public void testGetEnclosingFunctionInsideFunction() {
        Compiler compiler = createCompiler();
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        function.addChildrenToFront(name);
        function.addChildAfter(params, name);
        function.addChildAfter(body, params);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(function);
        script.setInputId(new InputId("test.js"));

        final NodeTraversal[] trap = new NodeTraversal[1];
        NodeTraversal.Callback cb = new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                if (n == body) {
                    trap[0] = t;
                }
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverse(script);
        // Inside function body, getEnclosingFunction should return the function node
        Node enclosing = trap[0].getEnclosingFunction();
        assertNotNull(enclosing);
        assertEquals(Token.FUNCTION, enclosing.getType());
    }

    @Test(timeout = 4000)
    public void testGetControlFlowGraphForScope() {
        Compiler compiler = createCompiler();
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        Node stmt = new Node(Token.EXPR_RESULT);
        body.addChildToBack(stmt);
        function.addChildrenToFront(name);
        function.addChildAfter(params, name);
        function.addChildAfter(body, params);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(function);
        script.setInputId(new InputId("test.js"));

        final ControlFlowGraph<Node>[] cfgHolder = new ControlFlowGraph[1];
        NodeTraversal.Callback cb = new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                if (t.getScopeDepth() > 1) { // inside function scope
                    cfgHolder[0] = t.getControlFlowGraph();
                }
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverse(script);
        assertNotNull(cfgHolder[0]);
    }

    @Test(timeout = 4000)
    public void testGetLineNumber() {
        Compiler compiler = createCompiler();
        Node script = new Node(Token.SCRIPT, 10, 0); // line 10
        Node block = new Node(Token.BLOCK, 11, 0);
        script.addChildToBack(block);
        script.setInputId(new InputId("test.js"));
        final int[] line = new int[1];
        NodeTraversal.Callback cb = new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                if (n.getType() == Token.BLOCK) {
                    line[0] = t.getLineNumber();
                }
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverse(script);
        assertEquals(11, line[0]);
    }

    @Test(timeout = 4000)
    public void testMakeErrorMethods() {
        Compiler compiler = createCompiler();
        Node n = new Node(Token.SCRIPT);
        n.setSourceFileName("test.js");
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        // We need to have sourceName set; traverse first to set inputId? But makeError doesn't require traversal.
        // Actually makeError uses getSourceName() which returns sourceName field (initially ""). So these should work without traversal.
        JSError error1 = t.makeError(n, DiagnosticType.error("TEST", "msg"));
        assertEquals("msg", error1.description);
        JSError error2 = t.makeError(n, CheckLevel.WARNING, DiagnosticType.warning("TEST2", "msg2"));
        assertEquals("msg2", error2.description);
    }

    @Test(timeout = 4000)
    public void testReport() {
        Compiler compiler = createCompiler();
        Node n = new Node(Token.SCRIPT);
        n.setSourceFileName("test.js");
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        DiagnosticType dt = DiagnosticType.warning("TEST", "Message {0}");
        t.report(n, dt, "arg1");
        // We can't check the compiler's error list easily, but at least no exception
    }

    @Test(timeout = 4000)
    public void testGetCurrentNode() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        final Node[] captured = new Node[1];
        NodeTraversal.Callback cb = new NodeTraversal.Callback() {
            @Override
            public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
                return true;
            }

            @Override
            public void visit(NodeTraversal t, Node n, Node parent) {
                captured[0] = t.getCurrentNode();
            }
        };
        NodeTraversal t = new NodeTraversal(compiler, cb);
        t.traverse(script);
        // The last visited node is SCRIPT
        assertEquals(Token.SCRIPT, captured[0].getType());
    }

    @Test(timeout = 4000)
    public void testGetSourceName() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        t.traverse(script);
        assertEquals("test.js", t.getSourceName());
    }

    @Test(timeout = 4000)
    public void testGetInput() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        t.traverse(script);
        CompilerInput input = t.getInput();
        assertNotNull(input);
    }

    @Test(timeout = 4000)
    public void testGetModule() {
        Compiler compiler = createCompiler();
        Node script = createSimpleScriptTree();
        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        t.traverse(script);
        // No module set, so should be null
        assertNull(t.getModule());
    }

    // Defect-targeted test: incomplete function (less than 3 children)
    @Test(timeout = 4000)
    public void testIncompleteFunctionDefect() {
        Compiler compiler = createCompiler();
        // Create a FUNCTION node with only 2 children (missing body)
        Node function = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        function.addChildrenToFront(name);
        function.addChildAfter(params, name);
        // Only 2 children - body is missing
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(function);
        script.setInputId(new InputId("test.js"));

        NodeTraversal t = new NodeTraversal(compiler, new RecordingCallback());
        try {
            t.traverse(script);
            fail("Expected RuntimeException with INTERNAL COMPILER ERROR");
        } catch (RuntimeException e) {
            assertTrue("Exception message should contain INTERNAL COMPILER ERROR",
                    e.getMessage().contains("INTERNAL COMPILER ERROR"));
            // The root cause should be an IllegalStateException from Preconditions.checkState
            Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
        }
    }
}