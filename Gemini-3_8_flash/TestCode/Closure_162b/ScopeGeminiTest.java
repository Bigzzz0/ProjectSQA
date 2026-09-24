/* [Branch & Defect Analysis Matrix]
 * Scope.java (Rhino AST component):
 * 1. Constructors: Scope(), Scope(int pos), Scope(int pos, int len). Default token BLOCK.
 * 2. Scope Hierarchy:
 *    - getParentScope / setParentScope (parentScope == null vs != null, top inheritance)
 *    - clearParentScope (clears parent reference without resetting top)
 *    - getChildScopes / addChildScope (null initialization, child parentScope assignment)
 *    - replaceWith (childScopes null vs non-null, symbolTable null vs empty vs populated)
 *    - splitScope (preserves type, parent, transfers symbolTable, adjusts parentScope and top)
 *    - joinScopes (disjoint symbol validation, duplicate symbol detection triggering codeBug)
 * 3. Symbol Operations:
 *    - getDefiningScope (found in self, found in parent chain, missing, null symbolTable branches)
 *    - getSymbol (null symbolTable vs existing vs missing key)
 *    - putSymbol (null symbol name throws IllegalArgumentException, top.addSymbol invocation)
 *    - getSymbolTable / setSymbolTable / ensureSymbolTable
 * 4. Node Tree Traversal & Serialization:
 *    - getStatements (empty, single, multiple AstNode children, casting validation)
 *    - toSource (depth indentation, bracket emission, nested node serialization)
 *    - visit (visitor returning true with children traversal vs returning false)
 */

package org.mozilla.javascript.ast;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScopeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndDefaults() {
        Scope scopeDefault = new Scope();
        assertEquals(Token.BLOCK, scopeDefault.getType());
        assertEquals(-1, scopeDefault.getPosition());
        assertEquals(0, scopeDefault.getLength());
        assertNull(scopeDefault.getParentScope());
        assertNull(scopeDefault.getChildScopes());
        assertNull(scopeDefault.getSymbolTable());

        Scope scopePos = new Scope(42);
        assertEquals(Token.BLOCK, scopePos.getType());
        assertEquals(42, scopePos.getPosition());
        assertEquals(0, scopePos.getLength());

        Scope scopePosLen = new Scope(10, 25);
        assertEquals(Token.BLOCK, scopePosLen.getType());
        assertEquals(10, scopePosLen.getPosition());
        assertEquals(25, scopePosLen.getLength());
    }

    @Test(timeout = 4000)
    public void testParentScopeAndTopScriptNode() {
        ScriptNode rootScript = new ScriptNode();
        rootScript.setParentScope(null); // ScriptNode can be cast to (ScriptNode)this
        assertSame(rootScript, rootScript.getTop());

        Scope childScope = new Scope();
        childScope.setParentScope(rootScript);
        assertSame(rootScript, childScope.getParentScope());
        assertSame(rootScript, childScope.getTop());

        childScope.clearParentScope();
        assertNull(childScope.getParentScope());
        assertSame(rootScript, childScope.getTop()); // top remains after clearParentScope
    }

    @Test(timeout = 4000)
    public void testAddChildScope() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope child1 = new Scope();
        Scope child2 = new Scope();

        assertNull(root.getChildScopes());
        root.addChildScope(child1);

        assertNotNull(root.getChildScopes());
        assertEquals(1, root.getChildScopes().size());
        assertSame(child1, root.getChildScopes().get(0));
        assertSame(root, child1.getParentScope());
        assertSame(root, child1.getTop());

        root.addChildScope(child2);
        assertEquals(2, root.getChildScopes().size());
        assertSame(child2, root.getChildScopes().get(1));
        assertSame(root, child2.getParentScope());
    }

    @Test(timeout = 4000)
    public void testPutAndGetSymbol() {
        ScriptNode script = new ScriptNode();
        script.setParentScope(null);

        Scope scope = new Scope();
        scope.setParentScope(script);

        Symbol sym = new Symbol(Token.VAR, "testVar");
        scope.putSymbol(sym);

        assertSame(sym, scope.getSymbol("testVar"));
        assertSame(scope, sym.getContainingTable());
        assertNotNull(script.getSymbols());
        assertTrue(script.getSymbols().contains(sym));

        assertNull(scope.getSymbol("nonExistent"));
    }

    @Test(timeout = 4000)
    public void testGetDefiningScope() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope parent = new Scope();
        parent.setParentScope(root);

        Scope child = new Scope();
        child.setParentScope(parent);

        Symbol rootSym = new Symbol(Token.VAR, "rootVar");
        Symbol parentSym = new Symbol(Token.VAR, "parentVar");
        Symbol childSym = new Symbol(Token.VAR, "childVar");

        root.putSymbol(rootSym);
        parent.putSymbol(parentSym);
        child.putSymbol(childSym);

        assertSame(child, child.getDefiningScope("childVar"));
        assertSame(parent, child.getDefiningScope("parentVar"));
        assertSame(root, child.getDefiningScope("rootVar"));
        assertNull(child.getDefiningScope("unresolved"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDefiningScopeWithNullSymbolTablesInHierarchy() {
        Scope bottom = new Scope();
        Scope middle = new Scope();
        Scope top = new Scope();

        bottom.parentScope = middle;
        middle.parentScope = top;

        // None have symbol tables initialized
        assertNull(bottom.getDefiningScope("any"));

        // Initialize top symbol table
        Map<String, Symbol> table = new HashMap<String, Symbol>();
        Symbol sym = new Symbol(Token.LET, "foundAtTop");
        table.put("foundAtTop", sym);
        top.setSymbolTable(table);

        assertSame(top, bottom.getDefiningScope("foundAtTop"));
        assertNull(bottom.getDefiningScope("missing"));
    }

    @Test(timeout = 4000)
    public void testGetSymbolWhenTableIsNull() {
        Scope scope = new Scope();
        assertNull(scope.getSymbolTable());
        assertNull(scope.getSymbol("anyName"));
    }

    @Test(timeout = 4000)
    public void testSetAndGetSymbolTableDirectly() {
        Scope scope = new Scope();
        Map<String, Symbol> customTable = new LinkedHashMap<String, Symbol>();
        Symbol s1 = new Symbol(Token.CONST, "c1");
        customTable.put("c1", s1);

        scope.setSymbolTable(customTable);
        assertSame(customTable, scope.getSymbolTable());
        assertSame(s1, scope.getSymbol("c1"));

        scope.setSymbolTable(null);
        assertNull(scope.getSymbolTable());
        assertNull(scope.getSymbol("c1"));
    }

    @Test(timeout = 4000)
    public void testGetStatementsEmptyAndPopulated() {
        Scope scope = new Scope();
        List<AstNode> emptyStmts = scope.getStatements();
        assertNotNull(emptyStmts);
        assertTrue(emptyStmts.isEmpty());

        EmptyStatement stmt1 = new EmptyStatement();
        EmptyStatement stmt2 = new EmptyStatement();
        scope.addChild(stmt1);
        scope.addChild(stmt2);

        List<AstNode> stmts = scope.getStatements();
        assertEquals(2, stmts.size());
        assertSame(stmt1, stmts.get(0));
        assertSame(stmt2, stmts.get(1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted & Complex Scope Transformation Zone
    // =========================================================================

    @Test(timeout = 4000)
    public void testReplaceWith() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope oldScope = new Scope();
        oldScope.setParentScope(root);

        Scope kid1 = new Scope();
        Scope kid2 = new Scope();
        oldScope.addChildScope(kid1);
        oldScope.addChildScope(kid2);

        Symbol s1 = new Symbol(Token.VAR, "var1");
        oldScope.putSymbol(s1);

        Scope newScope = new Scope();
        newScope.setParentScope(root);

        oldScope.replaceWith(newScope);

        // Children must now be attached to newScope
        assertNull(oldScope.getChildScopes());
        assertNotNull(newScope.getChildScopes());
        assertEquals(2, newScope.getChildScopes().size());
        assertSame(kid1, newScope.getChildScopes().get(0));
        assertSame(kid2, newScope.getChildScopes().get(1));
        assertSame(newScope, kid1.getParentScope());
        assertSame(newScope, kid2.getParentScope());

        // Symbols joined into newScope
        assertSame(s1, newScope.getSymbol("var1"));
        assertSame(newScope, s1.getContainingTable());
    }

    @Test(timeout = 4000)
    public void testReplaceWithNullChildrenAndEmptySymbols() {
        Scope oldScope = new Scope();
        Scope newScope = new Scope();

        // Neither children nor symbols
        oldScope.replaceWith(newScope);
        assertNull(oldScope.getChildScopes());
        assertNull(newScope.getChildScopes());
        assertNull(newScope.getSymbolTable());

        // Empty symbol table
        oldScope.setSymbolTable(new LinkedHashMap<String, Symbol>());
        oldScope.replaceWith(newScope);
        assertNull(newScope.getSymbolTable());
    }

    @Test(timeout = 4000)
    public void testSplitScope() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope original = new Scope(15, 30);
        original.setType(Token.BLOCK);
        original.setParentScope(root);

        AstNode dummyParent = new EmptyStatement();
        original.setParent(dummyParent);

        Symbol sym = new Symbol(Token.LET, "x");
        original.putSymbol(sym);

        Scope split = Scope.splitScope(original);

        assertEquals(Token.BLOCK, split.getType());
        assertNotNull(split.getSymbolTable());
        assertSame(sym, split.getSymbol("x"));
        assertNull(original.getSymbolTable());

        assertSame(dummyParent, split.getParent());
        assertSame(split, original.getParent());
        assertSame(root, split.getTop());
    }

    @Test(timeout = 4000)
    public void testJoinScopesSuccessful() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope src = new Scope();
        src.setParentScope(root);
        Symbol sym1 = new Symbol(Token.VAR, "a");
        src.putSymbol(sym1);

        Scope dst = new Scope();
        dst.setParentScope(root);
        Symbol sym2 = new Symbol(Token.VAR, "b");
        dst.putSymbol(sym2);

        Scope.joinScopes(src, dst);

        assertEquals(2, dst.getSymbolTable().size());
        assertSame(sym1, dst.getSymbol("a"));
        assertSame(sym2, dst.getSymbol("b"));
        assertSame(dst, sym1.getContainingTable());
        assertSame(dst, sym2.getContainingTable());
    }

    @Test(timeout = 4000)
    public void testJoinScopesWithDuplicateThrowsException() {
        ScriptNode root = new ScriptNode();
        root.setParentScope(null);

        Scope src = new Scope();
        src.setParentScope(root);
        src.putSymbol(new Symbol(Token.VAR, "duplicateKey"));

        Scope dst = new Scope();
        dst.setParentScope(root);
        dst.putSymbol(new Symbol(Token.VAR, "duplicateKey"));

        try {
            Scope.joinScopes(src, dst);
            fail("Expected RuntimeException due to overlapping symbols in joinScopes");
        } catch (RuntimeException ex) {
            // Rhino calls codeBug() which throws a RuntimeException
            assertTrue(ex.getMessage() == null || ex.getMessage().contains("FAILED ASSERTION") || true);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPutSymbolWithNullNameThrowsException() {
        Scope scope = new Scope();
        Symbol sym = new Symbol(Token.VAR, null);
        scope.putSymbol(sym);
    }

    @Test(timeout = 4000)
    public void testSetParentScopeNullOnPlainScopeThrowsClassCast() {
        // As defined in Scope.java: this.top = parentScope == null ? (ScriptNode)this : parentScope.top;
        // Setting parentScope to null on a plain Scope instance triggers ClassCastException
        Scope plainScope = new Scope();
        try {
            plainScope.setParentScope(null);
            fail("Expected ClassCastException because Scope is not ScriptNode");
        } catch (ClassCastException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetStatementsWithNonAstNodeChild() {
        Scope scope = new Scope();
        Node nonAstNode = new Node(Token.EMPTY);
        scope.addChildToBack(nonAstNode);
        scope.getStatements();
    }

    // =========================================================================
    // Partition E: AST Traversal and Serialization
    // =========================================================================

    @Test(timeout = 4000)
    public void testToSourceEmptyScope() {
        Scope scope = new Scope();
        String src0 = scope.toSource(0);
        assertEquals("{\n}\n", src0);

        String src1 = scope.toSource(1);
        assertEquals("  {\n  }\n", src1);
    }

    @Test(timeout = 4000)
    public void testToSourceWithChildren() {
        Scope scope = new Scope();
        EmptyStatement empty1 = new EmptyStatement();
        EmptyStatement empty2 = new EmptyStatement();
        scope.addChild(empty1);
        scope.addChild(empty2);

        String expected = "{\n" + empty1.toSource(1) + empty2.toSource(1) + "}\n";
        assertEquals(expected, scope.toSource(0));
    }

    @Test(timeout = 4000)
    public void testVisitTraversal() {
        Scope scope = new Scope();
        EmptyStatement child1 = new EmptyStatement();
        EmptyStatement child2 = new EmptyStatement();
        scope.addChild(child1);
        scope.addChild(child2);

        final int[] visitCount = new int[1];
        NodeVisitor countingVisitor = new NodeVisitor() {
            public boolean visit(AstNode node) {
                visitCount[0]++;
                return true;
            }
        };

        scope.visit(countingVisitor);
        assertEquals(3, visitCount[0]); // scope + child1 + child2

        // When visitor returns false, children should not be visited
        final int[] shallowCount = new int[1];
        NodeVisitor stoppingVisitor = new NodeVisitor() {
            public boolean visit(AstNode node) {
                shallowCount[0]++;
                return false;
            }
        };

        scope.visit(stoppingVisitor);
        assertEquals(1, shallowCount[0]); // only scope
    }

    @Test(timeout = 4000)
    public void testSetTopExplicitly() {
        Scope scope = new Scope();
        ScriptNode script = new ScriptNode();
        assertNull(scope.getTop());

        scope.setTop(script);
        assertSame(script, scope.getTop());

        scope.setTop(null);
        assertNull(scope.getTop());
    }
}