package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

/**
 * White-Box Test Suite for MinimizeExitPoints.
 * 
 * /* [Branch & Defect Analysis Matrix] */
 * 
 * Decision branches targeted:
 * - LABEL: tryMinimizeExits with Token.BREAK and label name
 * - FOR/WHILE: tryMinimizeExits with Token.CONTINUE and null label
 * - DO: tryMinimizeExits with Token.CONTINUE and null label, plus FALSE condition branch for BREAK
 * - FUNCTION: tryMinimizeExits with Token.RETURN and null label
 * - tryMinimizeExits recursive cases: exit node, if, try/catch/finally, label, block children
 * - matchingExitNode: RETURN no children, RETURN with children, BREAK/CONTINUE with/without label
 * - tryMinimizeIfBlockExits: srcBlock is Block with/without children, is single statement, exit match
 * - moveAllFollowing: normal moves, function declaration moves to front
 * 
 * Boundary conditions:
 * - Null labelName for non-label breaks/continues
 * - Empty blocks, single-child blocks, multi-child blocks
 * - Try without catch, try with catch, try with finally, try with catch and finally
 * - DO with FALSE condition (TernaryValue.FALSE)
 * - IF with no else block, IF with empty else block, IF with non-empty else block
 * - Nested labels, nested loops
 * 
 * Defect-targeted zones (Defects4J ground truth):
 * - testDontRemoveBreakInTryFinally: break inside try-finally should NOT be removed (ECMA 262)
 * - testFunctionReturnOptimization: return optimization in functions
 */
public class MinimizeExitPointsDeepseekTest {

    // Helper to create a simple compiler for testing
    private AbstractCompiler createTestCompiler() {
        CompilerOptions options = new CompilerOptions();
        Compiler compiler = new Compiler();
        compiler.initOptions(options);
        return compiler;
    }

    // Helper to parse JavaScript code into AST
    private Node parseScript(AbstractCompiler compiler, String code) {
        return compiler.parseSyntheticCode(code);
    }

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testFunctionReturnRemoval() {
        // Function with if-return at end should be optimized
        String code = "function f() { if (x) return; else y(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        // After optimization, the return should be gone
        String result = compiler.toSource(root);
        assertTrue("Should not contain 'return'", !result.contains("return"));
    }

    @Test(timeout = 4000)
    public void testLabelBreakRemoval() {
        // Label with break at end should be optimized
        String code = "label: { if (x) break label; }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        assertTrue("Should not contain 'break'", !result.contains("break"));
    }

    @Test(timeout = 4000)
    public void testWhileContinueRemoval() {
        // While loop with continue at end should be optimized
        String code = "while (true) { if (x) continue; y(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        assertTrue("Should not contain 'continue'", !result.contains("continue"));
    }

    @Test(timeout = 4000)
    public void testDoWhileFalseConditionBreakRemoval() {
        // Do-while with false condition: break should be treated as continue
        String code = "do { if (x) break; y(); } while (false);";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Break should be removed because condition is always false
        assertTrue("Should not contain 'break'", !result.contains("break"));
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testMatchingExitNodeReturnWithChildren() {
        // Matching exit node should return false for return with expression
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newString("x"));
        assertFalse("RETURN with children should not match",
            MinimizeExitPoints.matchingExitNode(returnNode, Token.RETURN, null));
    }

    @Test(timeout = 4000)
    public void testMatchingExitNodeReturnWithoutChildren() {
        // Matching exit node should return true for return without expression
        Node returnNode = new Node(Token.RETURN);
        assertTrue("RETURN without children should match",
            MinimizeExitPoints.matchingExitNode(returnNode, Token.RETURN, null));
    }

    @Test(timeout = 4000)
    public void testMatchingExitNodeBreakWithoutLabel() {
        // BREAK without children and null label should match
        Node breakNode = new Node(Token.BREAK);
        assertTrue("BREAK without label should match",
            MinimizeExitPoints.matchingExitNode(breakNode, Token.BREAK, null));
    }

    @Test(timeout = 4000)
    public void testMatchingExitNodeBreakWithLabel() {
        // BREAK with matching label should match
        Node breakNode = new Node(Token.BREAK);
        breakNode.addChildToBack(Node.newString("mylabel"));
        assertTrue("BREAK with matching label should match",
            MinimizeExitPoints.matchingExitNode(breakNode, Token.BREAK, "mylabel"));
        assertFalse("BREAK with non-matching label should not match",
            MinimizeExitPoints.matchingExitNode(breakNode, Token.BREAK, "other"));
    }

    @Test(timeout = 4000)
    public void testEmptyBlock() {
        // Empty block should not cause issues
        String code = "function f() { }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        assertEquals("Should not change empty function", "function f(){}", result);
    }

    @Test(timeout = 4000)
    public void testNullLabelNameForBreak() {
        // BREAK with non-null label on non-label context should not match
        Node breakNode = new Node(Token.BREAK);
        breakNode.addChildToBack(Node.newString("label1"));
        assertFalse("BREAK with label but null labelName should not match",
            MinimizeExitPoints.matchingExitNode(breakNode, Token.BREAK, null));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ========================================================================

    @Test(timeout = 4000)
    public void testDontRemoveBreakInTryFinally() {
        // KNOWN DEFECT: break inside try-finally should NOT be removed
        String code = "label: { try { if (x) break label; } finally { cleanup(); } }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // The break should remain because finally block changes completion type
        assertTrue("Break in try-finally should NOT be removed", result.contains("break"));
    }

    @Test(timeout = 4000)
    public void testFunctionReturnOptimization() {
        // KNOWN DEFECT: Function return optimization should work correctly
        String code = "function f() { if (true) return; g(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // The return should be optimized out, and g() should remain
        assertTrue("Should contain g() call", result.contains("g"));
        assertFalse("Should not contain return (optimized)", result.contains("return"));
    }

    @Test(timeout = 4000)
    public void testBreakInTryCatchOnly() {
        // Break in try-catch (without finally) can be optimized
        String code = "label: { try { if (x) break label; } catch(e) { } }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Break can be removed since there's no finally
        assertTrue("Break in try-catch should be removable", !result.contains("break"));
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000)
    public void testNonBlockNode() {
        // Non-block node with no last child should return early
        Node numNode = Node.newNumber(42);
        // This should not throw - just returns
        MinimizeExitPoints.matchingExitNode(numNode, Token.RETURN, null);
        // If we reach here, no exception occurred
        assertTrue("Should not throw on non-block node", true);
    }

    @Test(timeout = 4000)
    public void testNestedLabels() {
        // Nested labels should work correctly
        String code = "outer: inner: { if (x) break outer; }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        // Should not throw exception
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testIfWithNoElseBlock() {
        // IF without else block should not cause NPE
        String code = "function f() { if (x) return; }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Should be optimized
        assertFalse("Should not contain return", result.contains("return"));
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testProcessMultipleTimes() {
        // Process should be idempotent
        String code = "function f() { if (x) return; y(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        String firstResult = compiler.toSource(root);
        
        // Process again
        m.process(null, root);
        String secondResult = compiler.toSource(root);
        
        assertEquals("Processing twice should give same result", firstResult, secondResult);
    }

    @Test(timeout = 4000)
    public void testPreservesFunctionDeclarations() {
        // Function declarations should be moved to front in moveAllFollowing
        String code = "function f() { if (x) return; function inner() {} g(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // inner function should be hoisted to front of block
        assertTrue("Should contain inner function", result.contains("function inner"));
        assertTrue("Should contain g()", result.contains("g"));
    }

    @Test(timeout = 4000)
    public void testMultipleIfExits() {
        // Multiple if-exits should be converted in a single pass
        String code = "function f() { if (x) return; if (y) return; z(); }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Both returns should be removed
        assertFalse("Should not contain return", result.contains("return"));
        assertTrue("Should contain z()", result.contains("z"));
    }

    @Test(timeout = 4000)
    public void testTryWithCatchAndFinally() {
        // Try with both catch and finally - only finally block prevents optimization
        String code = "label: { try { if (x) break label; } catch(e) { } finally { cleanup(); } }";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Break should remain because finally block is present
        assertTrue("Break in try-catch-finally should NOT be removed", result.contains("break"));
    }

    @Test(timeout = 4000)
    public void testDoWhileTrueCondition() {
        // Do-while with true condition should NOT optimize break as continue
        String code = "do { if (x) break; y(); } while (true);";
        AbstractCompiler compiler = createTestCompiler();
        Node root = parseScript(compiler, code);
        
        MinimizeExitPoints m = new MinimizeExitPoints(compiler);
        m.process(null, root);
        
        String result = compiler.toSource(root);
        // Break should remain because condition is not always false
        assertTrue("Break in do-while with true condition should remain", result.contains("break"));
    }
}