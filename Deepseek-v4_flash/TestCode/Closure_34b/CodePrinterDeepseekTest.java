package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: com.google.javascript.jscomp.CodePrinter
 *
 * Known Defect: StackOverflowError when printing deeply nested ADD nodes
 * (testManyAdds). The recursive traversal in CodeGenerator.add does not
 * handle deep ASTs, causing stack overflow.
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Compact printing of simple expressions
 *   - Pretty printing with indentation
 *   - Line break insertion (maybeLineBreak, maybeCutLine)
 *   - Block start/end, list separator, operator appending
 *   - End-of-file handling (endFile)
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null root node → IllegalStateException
 *   - Zero/negative line length threshold → treated as Integer.MAX_VALUE
 *   - Very long lines with small threshold
 *   - Empty AST (single leaf node)
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Deeply nested ADD chain (depth > 5000) → StackOverflowError in buggy version
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Null sourceMapDetailLevel → Preconditions check (not directly testable)
 *   - Invalid line cut undo (convertPosition with insertion=false on previous line)
 *     → IllegalStateException (tested via endFile with preferLineBreakAtEndOfFile)
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Builder returns consistent code string
 *   - Source map generation (basic smoke test)
 */
public class CodePrinterDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleCompact() {
        Node ast = createSimpleAdd();
        String code = new CodePrinter.Builder(ast).build();
        assertNotNull(code);
        assertTrue(code.contains("x + y") || code.contains("x+y"));
    }

    @Test(timeout = 4000)
    public void testSimplePretty() {
        Node ast = createSimpleAdd();
        String code = new CodePrinter.Builder(ast)
                .setPrettyPrint(true)
                .build();
        assertNotNull(code);
        // Pretty printing adds spaces around operators
        assertTrue(code.contains("x + y"));
    }

    @Test(timeout = 4000)
    public void testLineBreakEnabled() {
        Node ast = createBlockWithStatements();
        String code = new CodePrinter.Builder(ast)
                .setLineBreak(true)
                .setLineLengthThreshold(10)
                .build();
        assertNotNull(code);
        // With small threshold, lines should be broken
        assertTrue(code.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testBlockStartEnd() {
        // Create a simple IF block
        Node cond = new Node(Token.NAME, "a");
        Node body = new Node(Token.BLOCK);
        body.addChild(new Node(Token.NAME, "b"));
        Node ifNode = new Node(Token.IF, cond, body);
        String code = new CodePrinter.Builder(ifNode)
                .setPrettyPrint(true)
                .build();
        assertNotNull(code);
        assertTrue(code.contains("{"));
        assertTrue(code.contains("}"));
    }

    @Test(timeout = 4000)
    public void testListSeparator() {
        // Create a function call with multiple arguments
        Node arg1 = new Node(Token.NAME, "x");
        Node arg2 = new Node(Token.NAME, "y");
        Node call = new Node(Token.CALL, new Node(Token.NAME, "f"), arg1, arg2);
        String code = new CodePrinter.Builder(call).build();
        assertNotNull(code);
        assertTrue(code.contains(","));
    }

    @Test(timeout = 4000)
    public void testOperatorAppending() {
        Node ast = new Node(Token.ASSIGN, new Node(Token.NAME, "a"), new Node(Token.NUMBER, 1.0));
        String code = new CodePrinter.Builder(ast).build();
        assertNotNull(code);
        assertTrue(code.contains("="));
    }

    @Test(timeout = 4000)
    public void testEndFile() {
        Node ast = new Node(Token.NAME, "x");
        String code = new CodePrinter.Builder(ast).build();
        assertNotNull(code);
        // Should not throw
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNullRoot() {
        new CodePrinter.Builder(null).build();
    }

    @Test(timeout = 4000)
    public void testZeroLineLengthThreshold() {
        Node ast = createSimpleAdd();
        // Threshold <= 0 should be treated as MAX_VALUE (no line breaks)
        String code = new CodePrinter.Builder(ast)
                .setLineLengthThreshold(0)
                .build();
        assertNotNull(code);
        // No newlines expected
        assertFalse(code.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testNegativeLineLengthThreshold() {
        Node ast = createSimpleAdd();
        String code = new CodePrinter.Builder(ast)
                .setLineLengthThreshold(-1)
                .build();
        assertNotNull(code);
        assertFalse(code.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testVeryLongLineWithSmallThreshold() {
        // Create a long chain of additions to force line breaks
        Node leaf = new Node(Token.NAME, "x");
        Node root = leaf;
        for (int i = 0; i < 100; i++) {
            root = new Node(Token.ADD, root, leaf);
        }
        String code = new CodePrinter.Builder(root)
                .setLineLengthThreshold(20)
                .build();
        assertNotNull(code);
        // Should contain newlines due to threshold
        assertTrue(code.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testEmptyAst() {
        // Single leaf node
        Node ast = new Node(Token.NAME, "a");
        String code = new CodePrinter.Builder(ast).build();
        assertEquals("a", code.trim());
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (StackOverflowError)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testManyAdds() {
        // Build a deeply nested ADD tree to trigger StackOverflowError in buggy version
        Node leaf = new Node(Token.NAME, "x");
        Node root = new Node(Token.ADD, leaf, leaf);
        int depth = 5000; // Adjust if needed; should cause overflow on typical Java stacks
        for (int i = 0; i < depth; i++) {
            root = new Node(Token.ADD, root, leaf);
        }
        // This should complete without throwing StackOverflowError in fixed version
        String code = new CodePrinter.Builder(root).build();
        assertNotNull(code);
        assertTrue(code.length() > 0);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEndFileWithPreferLineBreakAtEndOfFile() {
        // This exercises the endFile logic in CompactCodePrinter
        Node ast = new Node(Token.NAME, "x");
        String code = new CodePrinter.Builder(ast)
                .setPreferLineBreakAtEndOfFile(true)
                .setLineLengthThreshold(10)
                .build();
        assertNotNull(code);
        // The method may add a newline at end; just ensure no exception
    }

    @Test(timeout = 4000)
    public void testEndFileWithPreviousCut() {
        // Create a long line that forces a cut, then endFile with preferLineBreak
        Node leaf = new Node(Token.NAME, "x");
        Node root = leaf;
        for (int i = 0; i < 50; i++) {
            root = new Node(Token.ADD, root, leaf);
        }
        String code = new CodePrinter.Builder(root)
                .setPreferLineBreakAtEndOfFile(true)
                .setLineLengthThreshold(30)
                .build();
        assertNotNull(code);
        // Should not throw IllegalStateException from convertPosition
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBuilderReturnsConsistentCode() {
        Node ast = createSimpleAdd();
        CodePrinter.Builder builder = new CodePrinter.Builder(ast);
        String code1 = builder.build();
        String code2 = builder.build();
        assertEquals(code1, code2);
    }

    @Test(timeout = 4000)
    public void testSourceMapGeneration() {
        // Basic smoke test: source map should not cause errors
        Node ast = new Node(Token.NAME, "x");
        // Use a simple SourceMap implementation (anonymous class)
        SourceMap dummyMap = new SourceMap() {
            @Override
            public void addMapping(Node node, FilePosition start, FilePosition end) {
                // no-op
            }
            // Other methods not needed for this test
        };
        String code = new CodePrinter.Builder(ast)
                .setSourceMap(dummyMap)
                .build();
        assertNotNull(code);
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private Node createSimpleAdd() {
        Node x = new Node(Token.NAME, "x");
        Node y = new Node(Token.NAME, "y");
        return new Node(Token.ADD, x, y);
    }

    private Node createBlockWithStatements() {
        Node block = new Node(Token.BLOCK);
        block.addChild(new Node(Token.EXPR_RESULT, new Node(Token.NAME, "a")));
        block.addChild(new Node(Token.EXPR_RESULT, new Node(Token.NAME, "b")));
        return block;
    }
}
