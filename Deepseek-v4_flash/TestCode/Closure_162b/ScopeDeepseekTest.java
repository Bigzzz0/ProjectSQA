package org.mozilla.javascript.ast;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

/**
 * Advanced White-Box JUnit 4 test suite for Scope.java (Rhino AST).
 * 
 * Branch & Defect Analysis Matrix:
 * - Branching in setParentScope: null parent (casts this to ScriptNode, potential ClassCastException bug)
 * - addChildScope: null/empty childScopes vs existing list; sets parentScope.
 * - replaceWith: childScopes null/non-null, symbolTable null/empty/nonempty; joinScopes call.
 * - splitScope: symbolTable transfer, parent reassignment.
 * - joinScopes: disjoint vs overlapping symbol sets (codeBug on overlap).
 * - getDefiningScope: null symbolTable, containsKey, chain traversal.
 * - putSymbol: null name → IllegalArgumentException, ensureSymbolTable, top.addSymbol.
 * - getSymbol: symbolTable null → null, else lookup.
 * - ensureSymbolTable: creates LinkedHashMap if null.
 * - getStatements: iterate over Node children, cast to AstNode.
 * - toSource/visit: iteration over children.
 * 
 * Known Defect Targeting (from Defects4J ScopedAliasesTest):
 *   Failure indicates incorrect symbol resolution/aliasing. We test scope chain
 *   replacement and symbol lookup to expose potential broken parent/child linkage.
 */
public class ScopeDeepseekTest {

    // Helper: a minimal AstNode subclass for testing statements/visits
    private static class MockAstNode extends AstNode {
        public MockAstNode() { super(0, 0); }
        @Override
        public String toSource(int depth) { return ""; }
        @Override
        public void visit(NodeVisitor v) { v.visit(this); }
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Scope s = new Scope();
        assertEquals(org.mozilla.javascript.Token.BLOCK, s.getType());
        assertEquals(0, s.getPosition());
        assertEquals(0, s.getLength());
        assertNull(s.getParentScope());
        assertNull(s.getSymbolTable());
        assertNull(s.getChildScopes());
        assertNull(s.getTop());
    }

    @Test(timeout = 4000)
    public void testPosConstructor() {
        Scope s = new Scope(42);
        assertEquals(42, s.getPosition());
        assertEquals(0, s.getLength());
    }

    @Test(timeout = 4000)
    public void testPosLenConstructor() {
        Scope s = new Scope(10, 20);
        assertEquals(10, s.getPosition());
        assertEquals(20, s.getLength());
    }

    @Test(timeout = 4000)
    public void testSetParentScopeNonNull() {
        Scope parent = new Scope();
        Scope child = new Scope();
        child.setParentScope(parent);
        assertSame(parent, child.getParentScope());
        assertNull(parent.getTop()); // parent's top is null
        assertNull(child.getTop());  // child's top becomes parent.top (null)
    }

    // This test targets the known defect: calling setParentScope(null) on a plain Scope
    // triggers ClassCastException (bug). It should instead set top to this (or null).
    // The test expects normal behavior -> fails on buggy version.
    @Test(timeout = 4000)
    public void testSetParentScopeNullBug() {
        Scope s = new Scope();
        s.setParentScope(null);
        // If bug exists, the previous line throws ClassCastException; test fails here.
        assertNotNull(s.getTop());
    }

    @Test(timeout = 4000)
    public void testAddChildScope() {
        Scope parent = new Scope();
        Scope child = new Scope();
        parent.addChildScope(child);
        List<Scope> children = parent.getChildScopes();
        assertNotNull(children);
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
        assertSame(parent, child.getParentScope());
    }

    @Test(timeout = 4000)
    public void testAddChildScopeMultiple() {
        Scope parent = new Scope();
        Scope child1 = new Scope();
        Scope child2 = new Scope();
        parent.addChildScope(child1);
        parent.addChildScope(child2);
        assertEquals(2, parent.getChildScopes().size());
    }

    @Test(timeout = 4000)
    public void testGetChildScopesInitiallyNull() {
        Scope s = new Scope();
        assertNull(s.getChildScopes());
    }

    @Test(timeout = 4000)
    public void testClearParentScope() {
        Scope parent = new Scope();
        Scope child = new Scope();
        child.setParentScope(parent);
        child.clearParentScope();
        assertNull(child.getParentScope());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testGetSymbolTableInitiallyNull() {
        Scope s = new Scope();
        assertNull(s.getSymbolTable());
    }

    @Test(timeout = 4000)
    public void testGetSymbolWithNullTable() {
        Scope s = new Scope();
        assertNull(s.getSymbol("x"));
    }

    @Test(timeout = 4000)
    public void testPutSymbolNullName() {
        Scope s = new Scope();
        Symbol sym = new Symbol(0, null); // name is null
        try {
            s.putSymbol(sym);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPutSymbolAndGetSymbol() {
        ScriptNode top = new ScriptNode();
        top.setParentScope(null); // avoid cast bug – setParentScope(null) on ScriptNode? ScriptNode extends Scope, same issue – but we need top to be valid.
        // Instead, set top via setTop after creating a plain Scope. We'll set a ScriptNode as top.
        // Actually, we can create a ScriptNode and then set parent scope to null? That would trigger the same bug.
        // To avoid, we'll set top manually after construction.
        Scope s = new Scope();
        Symbol sym = new Symbol(0, "foo");
        // should set top first to avoid NullPointerException from top.addSymbol
        ScriptNode scriptTop = new ScriptNode();
        s.setTop(scriptTop);
        s.putSymbol(sym);
        Map<String, Symbol> table = s.getSymbolTable();
        assertNotNull(table);
        assertTrue(table.containsKey("foo"));
        assertSame(sym, table.get("foo"));
        assertSame(s, sym.getContainingTable());
        // also top.addSymbol should have been called – we can't easily verify without extending ScriptNode
    }

    @Test(timeout = 4000)
    public void testGetDefiningScopeNotFound() {
        Scope s1 = new Scope();
        s1.setTop(new ScriptNode()); // set a dummy top to avoid issues
        Scope s2 = new Scope();
        s2.setParentScope(s1);
        assertNull(s2.getDefiningScope("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetDefiningScopeFoundInParent() {
        Scope s1 = new Scope();
        Symbol sym = new Symbol(0, "x");
        s1.setTop(new ScriptNode());
        s1.putSymbol(sym);
        Scope s2 = new Scope();
        s2.setParentScope(s1);
        assertSame(s1, s2.getDefiningScope("x"));
    }

    @Test(timeout = 4000)
    public void testGetDefiningScopeFoundInSelf() {
        Scope s = new Scope();
        Symbol sym = new Symbol(0, "y");
        s.setTop(new ScriptNode());
        s.putSymbol(sym);
        assertSame(s, s.getDefiningScope("y"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // Test replaceWith with children and symbols, mimicking alias replacement.
    @Test(timeout = 4000)
    public void testReplaceWithTransfer() {
        ScriptNode top = new ScriptNode();
        Scope oldScope = new Scope();
        oldScope.setTop(top);
        // add a child scope
        Scope child = new Scope();
        oldScope.addChildScope(child);
        // add a symbol
        Symbol sym = new Symbol(0, "alias");
        oldScope.putSymbol(sym);
        // new scope
        Scope newScope = new Scope();
        newScope.setTop(top);
        oldScope.replaceWith(newScope);
        // child should be moved to newScope
        assertNotNull(newScope.getChildScopes());
        assertEquals(1, newScope.getChildScopes().size());
        assertSame(child, newScope.getChildScopes().get(0));
        // symbol should be present in newScope
        assertNotNull(newScope.getSymbolTable());
        assertTrue(newScope.getSymbolTable().containsKey("alias"));
        // oldScope should have no children
        assertNull(oldScope.getChildScopes());
    }

    @Test(timeout = 4000)
    public void testReplaceWithNoChildrenNoSymbols() {
        Scope old = new Scope();
        Scope parent = new Scope();
        old.setParentScope(parent);
        Scope newScope = new Scope();
        old.replaceWith(newScope);
        assertNull(newScope.getChildScopes());
        assertNull(newScope.getSymbolTable());
    }

    @Test(timeout = 4000)
    public void testSplitScopeWithSymbols() {
        ScriptNode top = new ScriptNode();
        Scope original = new Scope();
        original.setTop(top);
        Symbol sym = new Symbol(0, "a");
        original.putSymbol(sym);
        Scope parent = new Scope();
        parent.setTop(top);
        original.setParentScope(parent);
        Scope split = Scope.splitScope(original);
        // original lost symbol table
        assertNull(original.getSymbolTable());
        // split has the symbol table
        assertNotNull(split.getSymbolTable());
        assertTrue(split.getSymbolTable().containsKey("a"));
        // parent chain: split.parent = original.parent? Actually result.parent = original.parent after splitScope
        assertSame(parent, split.getParentScope());
        // original's parent should now be split? In splitScope: result.parent = scope.parent; scope.parent = result; etc.
        // So original.parent becomes result (split)
        assertSame(split, original.getParent());
        // Also result.setParentScope(scope.getParentScope()) and result.setParentScope(result) ? That line seems odd.
        // Let's check the code: result.setParentScope(scope.getParentScope()); result.setParentScope(result); // second call overwrites!
        // That's likely a bug in the original code (the last line sets parentScope to result itself, creating a cycle).
        // We'll test that parentScope of split is split itself (cycle). This is a known oddity.
        assertSame(split, split.getParentScope()); // because the code does result.setParentScope(result) after setting to scope.getParentScope().
    }

    @Test(timeout = 4000)
    public void testJoinScopesDisjoint() {
        Scope src = new Scope();
        Scope dst = new Scope();
        Symbol s1 = new Symbol(0, "x");
        Symbol s2 = new Symbol(0, "y");
        src.setTop(new ScriptNode());
        dst.setTop(new ScriptNode());
        src.putSymbol(s1);
        dst.putSymbol(s2);
        Scope.joinScopes(src, dst);
        assertEquals(2, dst.getSymbolTable().size());
        assertTrue(dst.getSymbolTable().containsKey("x"));
        assertTrue(dst.getSymbolTable().containsKey("y"));
        // src still has its symbols
        assertTrue(src.getSymbolTable().containsKey("x"));
    }

    @Test(timeout = 4000)
    public void testJoinScopesOverlapping() {
        Scope src = new Scope();
        Scope dst = new Scope();
        Symbol s = new Symbol(0, "key");
        src.setTop(new ScriptNode());
        dst.setTop(new ScriptNode());
        src.putSymbol(s);
        dst.putSymbol(new Symbol(0, "key"));
        try {
            Scope.joinScopes(src, dst);
            fail("Expected codeBug error (overlap)");
        } catch (RuntimeException e) {
            // codeBug() throws a RuntimeException (likely AssertionError or similar)
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testGetStatementsEmpty() {
        Scope s = new Scope();
        List<AstNode> stmts = s.getStatements();
        assertNotNull(stmts);
        assertTrue(stmts.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetStatementsWithChildren() {
        Scope s = new Scope();
        AstNode child1 = new MockAstNode();
        AstNode child2 = new MockAstNode();
        s.addChildToBack(child1);
        s.addChildToBack(child2);
        List<AstNode> stmts = s.getStatements();
        assertEquals(2, stmts.size());
        assertSame(child1, stmts.get(0));
        assertSame(child2, stmts.get(1));
    }

    @Test(timeout = 4000)
    public void testToSource() {
        Scope s = new Scope();
        AstNode child = new MockAstNode();
        s.addChildToBack(child);
        String src = s.toSource(0);
        assertTrue(src.startsWith("{\n"));
        assertTrue(src.endsWith("}\n"));
    }

    @Test(timeout = 4000)
    public void testVisit() {
        Scope s = new Scope();
        final boolean[] visited = {false};
        AstNode child = new MockAstNode();
        s.addChildToBack(child);
        s.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode node) {
                if (node instanceof MockAstNode) visited[0] = true;
                return true;
            }
        });
        assertTrue("Child should be visited", visited[0]);
    }

    @Test(timeout = 4000)
    public void testSetSymbolTable() {
        Scope s = new Scope();
        Map<String, Symbol> table = new java.util.LinkedHashMap<>();
        s.setSymbolTable(table);
        assertSame(table, s.getSymbolTable());
        s.setSymbolTable(null);
        assertNull(s.getSymbolTable());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetSetTop() {
        ScriptNode top = new ScriptNode();
        Scope s = new Scope();
        s.setTop(top);
        assertSame(top, s.getTop());
    }

    @Test(timeout = 4000)
    public void testGetParentScopeNotNullAfterSet() {
        Scope parent = new Scope();
        Scope child = new Scope();
        child.setParentScope(parent);
        assertNotNull(child.getParentScope());
    }

    @Test(timeout = 4000)
    public void testAddChildScopeSetsParent() {
        Scope parent = new Scope();
        Scope child = new Scope();
        parent.addChildScope(child);
        assertSame(parent, child.getParentScope());
    }

    @Test(timeout = 4000)
    public void testReplaceWithNoSideEffectsOnSource() {
        Scope src = new Scope();
        Scope dst = new Scope();
        src.replaceWith(dst);
        assertNull(src.getChildScopes());
        assertNull(src.getSymbolTable());
    }

    @Test(timeout = 4000)
    public void testSymbolTableInsertionOrder() {
        Scope s = new Scope();
        s.setTop(new ScriptNode());
        Symbol a = new Symbol(0, "a");
        Symbol b = new Symbol(0, "b");
        s.putSymbol(a);
        s.putSymbol(b);
        String[] keys = s.getSymbolTable().keySet().toArray(new String[0]);
        assertArrayEquals(new String[]{"a", "b"}, keys);
    }
}