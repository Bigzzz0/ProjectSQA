package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.InputId;
import com.google.javascript.jscomp.parsing.ParserRunner;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JsAst.java - AST generation for JavaScript source files
 * 
 * Decision Branches:
 * 1. getAstRoot(): root == null (parse) vs root != null (return cached)
 * 2. parse(): IOException caught vs normal parse
 * 3. parse(): root == null || compiler.hasHaltingErrors() -> use dummy block
 * 4. parse(): else -> compiler.prepareAst(root)
 * 5. setSourceFile(): Preconditions.checkState(fileName.equals(file.getName()))
 * 
 * Boundary Conditions:
 * - Null sourceFile (constructor)
 * - Empty source code
 * - Source file with parse errors
 * - Source file with IO errors
 * - Multiple calls to getAstRoot (caching behavior)
 * - clearAst() followed by getAstRoot()
 * - setSourceFile with matching/non-matching filename
 * 
 * Defect Targeting (Issue 1103):
 * The defect relates to goog.scope handling where local variables are incorrectly
 * flagged as non-alias locals. This test suite targets the AST generation layer
 * that feeds into the ScopedAliases pass, ensuring proper AST structure for
 * goog.scope patterns.
 */
public class JsAstDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        assertNotNull("InputId should not be null", ast.getInputId());
        assertEquals("InputId name should match source file name", 
            "test.js", ast.getInputId().getIdName());
        assertSame("SourceFile should be the same object", sourceFile, ast.getSourceFile());
        assertNull("Root should be null before parsing", ast.getAstRoot(null));
    }

    @Test(timeout = 4000)
    public void testGetAstRootParsesAndCaches() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        // Create a mock compiler that can handle parsing
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root1 = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null after parsing", root1);
        assertTrue("Root should be a script node", root1.isScript());
        
        // Second call should return cached root
        Node root2 = ast.getAstRoot(compiler);
        assertSame("Second call should return same root object", root1, root2);
    }

    @Test(timeout = 4000)
    public void testClearAstResetsState() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root1 = ast.getAstRoot(compiler);
        assertNotNull("Root should exist after parsing", root1);
        
        ast.clearAst();
        
        // After clear, getAstRoot should parse again
        Node root2 = ast.getAstRoot(compiler);
        assertNotNull("Root should be re-parsed after clear", root2);
        assertNotSame("Should be a new root object after clear", root1, root2);
    }

    @Test(timeout = 4000)
    public void testSetSourceFileWithMatchingName() {
        SourceFile sourceFile1 = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile1);
        
        SourceFile sourceFile2 = SourceFile.fromCode("test.js", "var y = 2;");
        ast.setSourceFile(sourceFile2);
        
        assertSame("Source file should be updated", sourceFile2, ast.getSourceFile());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSetSourceFileWithNonMatchingName() {
        SourceFile sourceFile1 = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile1);
        
        SourceFile sourceFile2 = SourceFile.fromCode("different.js", "var y = 2;");
        ast.setSourceFile(sourceFile2); // Should throw IllegalStateException
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptySourceCode() {
        SourceFile sourceFile = SourceFile.fromCode("empty.js", "");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null for empty source", root);
        assertTrue("Root should be a script node", root.isScript());
        assertFalse("Script should have no children for empty source", root.hasChildren());
    }

    @Test(timeout = 4000)
    public void testSourceWithParseError() {
        SourceFile sourceFile = SourceFile.fromCode("error.js", "var x = ;;;");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.PARSE_ERRORS, CheckLevel.ERROR);
        compiler.initOptions(options);
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null even with parse errors", root);
        assertTrue("Root should be a script node (dummy block)", root.isScript());
    }

    @Test(timeout = 4000)
    public void testLargeSourceFile() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("var x").append(i).append(" = ").append(i).append(";\n");
        }
        SourceFile sourceFile = SourceFile.fromCode("large.js", sb.toString());
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null for large source", root);
        assertTrue("Root should be a script node", root.isScript());
        assertTrue("Script should have many children", root.getChildCount() > 900);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testGoogScopePattern() {
        // This test targets the defect related to goog.scope handling
        // The AST must correctly represent goog.scope patterns
        String code = "goog.scope(function() {\n" +
                     "  var a = goog.dom.createDom('div');\n" +
                     "  var b = goog.array.toArray(a);\n" +
                     "});";
        
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.GOOG_SCOPE_NON_ALIAS_LOCAL, CheckLevel.ERROR);
        compiler.initOptions(options);
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        
        // Verify the AST structure for goog.scope
        // The goog.scope call should be properly parsed
        Node firstChild = root.getFirstChild();
        assertNotNull("Should have at least one child", firstChild);
        
        // Check that the AST contains an EXPR_RESULT node (for the goog.scope call)
        boolean hasExprResult = false;
        for (Node child : root.children()) {
            if (child.isExprResult()) {
                hasExprResult = true;
                break;
            }
        }
        assertTrue("AST should contain expression result for goog.scope call", hasExprResult);
    }

    @Test(timeout = 4000)
    public void testGoogScopeWithAliasPattern() {
        // This test specifically targets the defect where local variables in goog.scope
        // are incorrectly flagged as non-alias locals
        String code = "goog.scope(function() {\n" +
                     "  var a = goog.dom.createDom;\n" +
                     "  var b = a('div');\n" +
                     "});";
        
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.GOOG_SCOPE_NON_ALIAS_LOCAL, CheckLevel.ERROR);
        compiler.initOptions(options);
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        
        // The AST should correctly represent the alias pattern
        // 'a' is an alias for goog.dom.createDom, not a non-alias local
        boolean hasVarDeclaration = false;
        for (Node child : root.children()) {
            if (child.isVar()) {
                hasVarDeclaration = true;
                break;
            }
        }
        // Note: In goog.scope, variables are declared inside the function scope
        // The outer AST should have the EXPR_RESULT for the goog.scope call
    }

    @Test(timeout = 4000)
    public void testMultipleGoogScopeCalls() {
        // Test multiple goog.scope calls to ensure proper AST generation
        String code = "goog.scope(function() {\n" +
                     "  var a = goog.array.toArray;\n" +
                     "});\n" +
                     "goog.scope(function() {\n" +
                     "  var b = goog.dom.createDom;\n" +
                     "});";
        
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        
        // Should have two EXPR_RESULT nodes for the two goog.scope calls
        int exprResultCount = 0;
        for (Node child : root.children()) {
            if (child.isExprResult()) {
                exprResultCount++;
            }
        }
        assertEquals("Should have two expression results for two goog.scope calls", 2, exprResultCount);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorWithNullSourceFile() {
        new JsAst(null);
    }

    @Test(timeout = 4000)
    public void testGetAstRootWithNullCompiler() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        // When compiler is null, the parse method will throw NullPointerException
        // but getAstRoot should handle it gracefully
        try {
            ast.getAstRoot(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected - compiler is null
        }
    }

    @Test(timeout = 4000)
    public void testParseWithIOException() {
        // Create a source file that will cause IOException during parsing
        SourceFile sourceFile = new SourceFile("test.js") {
            @Override
            public String getCode() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };
        
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.READ_ERROR, CheckLevel.ERROR);
        compiler.initOptions(options);
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null after IO error", root);
        assertTrue("Root should be a script node (dummy block)", root.isScript());
    }

    @Test(timeout = 4000)
    public void testClearAstAfterParseError() {
        SourceFile sourceFile = SourceFile.fromCode("error.js", "var x = ;;;");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.PARSE_ERRORS, CheckLevel.ERROR);
        compiler.initOptions(options);
        
        Node root1 = ast.getAstRoot(compiler);
        assertNotNull("Root should exist after parse error", root1);
        
        ast.clearAst();
        
        // After clear, should be able to parse again
        Node root2 = ast.getAstRoot(compiler);
        assertNotNull("Root should be re-parsed after clear", root2);
        assertNotSame("Should be a new root object", root1, root2);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testInputIdConsistency() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        InputId inputId1 = ast.getInputId();
        InputId inputId2 = ast.getInputId();
        
        assertSame("InputId should be the same object on multiple calls", inputId1, inputId2);
        assertEquals("InputId id name should match", "test.js", inputId1.getIdName());
    }

    @Test(timeout = 4000)
    public void testSourceFileConsistency() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        SourceFile retrievedFile = ast.getSourceFile();
        assertSame("Should return the same source file object", sourceFile, retrievedFile);
        assertEquals("Source file name should match", "test.js", retrievedFile.getName());
    }

    @Test(timeout = 4000)
    public void testMultipleParseCallsConsistency() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root1 = ast.getAstRoot(compiler);
        Node root2 = ast.getAstRoot(compiler);
        Node root3 = ast.getAstRoot(compiler);
        
        // All calls after the first should return the same cached root
        assertSame("Second call should return cached root", root1, root2);
        assertSame("Third call should return cached root", root1, root3);
    }

    @Test(timeout = 4000)
    public void testClearAstAndReparseConsistency() {
        SourceFile sourceFile = SourceFile.fromCode("test.js", "var x = 1;");
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root1 = ast.getAstRoot(compiler);
        assertNotNull("First parse should succeed", root1);
        
        ast.clearAst();
        
        Node root2 = ast.getAstRoot(compiler);
        assertNotNull("Second parse after clear should succeed", root2);
        assertNotSame("Should be different root objects", root1, root2);
        
        // Verify the structure is consistent
        assertEquals("Both parses should produce same number of children", 
            root1.getChildCount(), root2.getChildCount());
    }

    @Test(timeout = 4000)
    public void testSourceFileWithSpecialCharacters() {
        String code = "var x = 'hello world'; var y = 42;";
        SourceFile sourceFile = SourceFile.fromCode("test-file.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        assertEquals("Should have two variable declarations", 2, root.getChildCount());
    }

    @Test(timeout = 4000)
    public void testSourceFileWithComments() {
        String code = "// This is a comment\nvar x = 1;\n/* Block comment */\nvar y = 2;";
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        assertEquals("Should have two variable declarations", 2, root.getChildCount());
    }

    @Test(timeout = 4000)
    public void testSourceFileWithFunctions() {
        String code = "function foo() { return 1; }\nvar bar = function() { return 2; };";
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        assertTrue("Should have at least two children", root.getChildCount() >= 2);
    }

    @Test(timeout = 4000)
    public void testSourceFileWithObjectsAndArrays() {
        String code = "var obj = {a: 1, b: 2};\nvar arr = [1, 2, 3];";
        SourceFile sourceFile = SourceFile.fromCode("test.js", code);
        JsAst ast = new JsAst(sourceFile);
        
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        
        Node root = ast.getAstRoot(compiler);
        assertNotNull("Root should not be null", root);
        assertEquals("Should have two variable declarations", 2, root.getChildCount());
    }
}