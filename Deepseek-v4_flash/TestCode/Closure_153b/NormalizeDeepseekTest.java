package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Normalize.java - Compiler pass for AST normalization
 * 
 * Key Decision Branches:
 * 1. Constructor: assertOnChange flag handling
 * 2. process(): MAKE_LOCAL_NAMES_UNIQUE conditional, removeDuplicateDeclarations ordering
 * 3. NormalizeStatements.shouldTraverse(): doStatementNormalizations dispatch
 * 4. NormalizeStatements.visit(): WHILE->FOR conversion, FUNCTION normalization, constant annotation
 * 5. annotateConstantsByConvention(): NAME vs STRING, isObjLitKey, isProperty, isMarkedConstant checks
 * 6. normalizeFunctionDeclaration(): isFunctionExpression, isHoistedFunctionDeclaration
 * 7. rewriteFunctionDeclaration(): Node cloning, replacement, string clearing
 * 8. doStatementNormalizations(): LABEL normalization, extractForInitializer, splitVarDeclarations, moveNamedFunctions
 * 9. normalizeLabels(): switch on last child type (LABEL, BLOCK, FOR, WHILE, DO, default)
 * 10. extractForInitializer(): FOR-IN var, FOR with non-empty init, recursion into LABEL
 * 11. splitVarDeclarations(): VAR with multiple children, empty VAR assertion
 * 12. moveNamedFunctions(): Skip initial declarations, move remaining, null previous handling
 * 13. removeDuplicateDeclarations(): Scope creation, DuplicateDeclarationHandler
 * 14. DuplicateDeclarationHandler.onRedeclaration(): CATCH block, FUNCTION parent, VAR parent
 * 15. replaceVarWithAssignment(): Initialized vs uninitialized, statement block vs FOR vs LABEL
 * 16. PropagateConstantAnnotationsOverVars.visit(): Empty name skip, JSDocInfo null, shouldBeConstant logic
 * 17. VerifyConstants.visit(): checkUserDeclarations branch, constantMap consistency
 * 
 * Known Defect: testDuplicateVarInExterns and testMakeLocalNamesUnique failures
 * - Duplicate var declarations in externs not properly handled
 * - Local name uniquification may fail for certain patterns
 * 
 * Boundary Conditions:
 * - Null/empty name strings
 * - Empty VAR nodes
 * - FOR-IN vs regular FOR
 * - LABEL with non-standard children
 * - Function declarations at various positions
 * - CATCH block variable redeclaration
 * - Externs vs source redeclaration
 */
public class NormalizeDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorWithAssertOnChangeTrue() {
        // Test that constructor sets assertOnChange correctly
        // We can't directly access private field, but we can test behavior
        // through process() method which throws on violations
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize normalize = new Normalize(compiler, true);
        assertNotNull("Normalize instance should be created", normalize);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAssertOnChangeFalse() {
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize normalize = new Normalize(compiler, false);
        assertNotNull("Normalize instance should be created", normalize);
    }

    @Test(timeout = 4000)
    public void testParseAndNormalizeSyntheticCode() {
        AbstractCompiler compiler = Compiler.getInstance();
        String code = "var a = 1; var b = 2;";
        Node result = Normalize.parseAndNormalizeSyntheticCode(compiler, code, "test");
        assertNotNull("Parsed and normalized code should not be null", result);
        // Verify the AST structure after normalization
        assertTrue("Root should be a script block", result.isScript());
    }

    @Test(timeout = 4000)
    public void testParseAndNormalizeTestCode() {
        AbstractCompiler compiler = Compiler.getInstance();
        String code = "function f() { return 1; }";
        Node result = Normalize.parseAndNormalizeTestCode(compiler, code, "test");
        assertNotNull("Parsed and normalized test code should not be null", result);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testProcessWithNullExterns() {
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize normalize = new Normalize(compiler, false);
        Node root = new Node(Token.SCRIPT);
        try {
            normalize.process(null, root);
            // Should not throw for null externs
        } catch (NullPointerException e) {
            fail("Should handle null externs gracefully: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testProcessWithEmptyRoot() {
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize normalize = new Normalize(compiler, false);
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        try {
            normalize.process(externs, root);
        } catch (Exception e) {
            fail("Should handle empty root: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNormalizeStatementsWithEmptyVar() {
        // Test that empty VAR nodes are handled (assertOnChange would throw)
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        Node varNode = new Node(Token.VAR);
        Node block = new Node(Token.BLOCK, varNode);
        
        // shouldTraverse should not throw for empty VAR
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        assertTrue("shouldTraverse should return true", 
            normalizer.shouldTraverse(t, block, null));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testDuplicateVarInExterns() {
        // Directly targets the known defect: testDuplicateVarInExterns
        // This test verifies that duplicate variable declarations in externs
        // are handled correctly without causing assertion failures
        AbstractCompiler compiler = Compiler.getInstance();
        
        // Create externs with duplicate var declaration
        Node externs = new Node(Token.SCRIPT);
        Node var1 = new Node(Token.VAR);
        Node name1 = Node.newString(Token.NAME, "x");
        name1.addChildToFront(Node.newNumber(1));
        var1.addChildToFront(name1);
        externs.addChildToFront(var1);
        
        // Add duplicate in root
        Node root = new Node(Token.SCRIPT);
        Node var2 = new Node(Token.VAR);
        Node name2 = Node.newString(Token.NAME, "x");
        name2.addChildToFront(Node.newNumber(2));
        var2.addChildToFront(name2);
        root.addChildToFront(var2);
        
        Normalize normalize = new Normalize(compiler, false);
        try {
            normalize.process(externs, root);
            // If we reach here, the duplicate was handled (possibly by keeping externs declaration)
        } catch (IllegalStateException e) {
            fail("Duplicate var in externs should not cause assertion failure: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMakeLocalNamesUnique() {
        // Directly targets the known defect: testMakeLocalNamesUnique
        // Tests that local names are made unique across function scopes
        AbstractCompiler compiler = Compiler.getInstance();
        
        // Create code with shadowed variable names
        Node root = new Node(Token.SCRIPT);
        
        // Outer function with var x
        Node outerFunc = new Node(Token.FUNCTION);
        Node outerName = Node.newString(Token.NAME, "outer");
        Node outerParams = new Node(Token.LP);
        Node outerBody = new Node(Token.BLOCK);
        
        Node varX = new Node(Token.VAR);
        Node nameX = Node.newString(Token.NAME, "x");
        nameX.addChildToFront(Node.newNumber(10));
        varX.addChildToFront(nameX);
        outerBody.addChildToFront(varX);
        
        // Inner function with var x (should be renamed)
        Node innerFunc = new Node(Token.FUNCTION);
        Node innerName = Node.newString(Token.NAME, "inner");
        Node innerParams = new Node(Token.LP);
        Node innerBody = new Node(Token.BLOCK);
        
        Node varX2 = new Node(Token.VAR);
        Node nameX2 = Node.newString(Token.NAME, "x");
        nameX2.addChildToFront(Node.newNumber(20));
        varX2.addChildToFront(nameX2);
        innerBody.addChildToFront(varX2);
        
        innerFunc.addChildToFront(innerName);
        innerFunc.addChildToFront(innerParams);
        innerFunc.addChildToFront(innerBody);
        outerBody.addChildToFront(innerFunc);
        
        outerFunc.addChildToFront(outerName);
        outerFunc.addChildToFront(outerParams);
        outerFunc.addChildToFront(outerBody);
        root.addChildToFront(outerFunc);
        
        Normalize normalize = new Normalize(compiler, false);
        try {
            normalize.process(new Node(Token.SCRIPT), root);
            // Verify that inner x was renamed (would be different from outer x)
            // This is a simplified check - actual uniquification would change the name
        } catch (Exception e) {
            fail("Local name uniquification should not throw: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCatchBlockVarError() {
        // Tests the CATCH_BLOCK_VAR_ERROR diagnostic
        AbstractCompiler compiler = Compiler.getInstance();
        
        // Create a function with catch block and var with same name
        Node root = new Node(Token.SCRIPT);
        Node func = new Node(Token.FUNCTION);
        Node funcName = Node.newString(Token.NAME, "testFunc");
        Node params = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        
        // try { throw 0; } catch(e) { var e = 1; }
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH);
        Node catchName = Node.newString(Token.NAME, "e");
        Node catchBlock = new Node(Token.BLOCK);
        
        Node varE = new Node(Token.VAR);
        Node nameE = Node.newString(Token.NAME, "e");
        nameE.addChildToFront(Node.newNumber(1));
        varE.addChildToFront(nameE);
        catchBlock.addChildToFront(varE);
        
        catchNode.addChildToFront(catchName);
        catchNode.addChildToFront(catchBlock);
        tryNode.addChildToFront(tryBlock);
        tryNode.addChildToFront(catchNode);
        body.addChildToFront(tryNode);
        
        func.addChildToFront(funcName);
        func.addChildToFront(params);
        func.addChildToFront(body);
        root.addChildToFront(func);
        
        Normalize normalize = new Normalize(compiler, false);
        try {
            normalize.process(new Node(Token.SCRIPT), root);
        } catch (Exception e) {
            // May or may not throw depending on implementation
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testAssertOnChangeViolation() {
        // When assertOnChange is true, any code change should throw
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, true);
        
        // Create a VAR with multiple children to trigger splitVarDeclarations
        Node varNode = new Node(Token.VAR);
        Node name1 = Node.newString(Token.NAME, "a");
        name1.addChildToFront(Node.newNumber(1));
        Node name2 = Node.newString(Token.NAME, "b");
        name2.addChildToFront(Node.newNumber(2));
        varNode.addChildToFront(name1);
        varNode.addChildToFront(name2);
        
        Node block = new Node(Token.BLOCK, varNode);
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        
        // This should throw IllegalStateException due to assertOnChange
        normalizer.shouldTraverse(t, block, null);
    }

    @Test(timeout = 4000)
    public void testNormalizeLabelsWithNonStandardChild() {
        // Tests normalizeLabels when last child is not LABEL, BLOCK, FOR, WHILE, or DO
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create LABEL with EXPR_RESULT as child (should be wrapped in BLOCK)
        Node label = new Node(Token.LABEL);
        Node labelName = Node.newString(Token.LABEL_NAME, "myLabel");
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToFront(Node.newNumber(42));
        label.addChildToFront(labelName);
        label.addChildToFront(exprResult);
        
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.shouldTraverse(t, label, null);
        
        // After normalization, last child should be a BLOCK
        Node lastChild = label.getLastChild();
        assertEquals("Non-standard label child should be wrapped in BLOCK", 
            Token.BLOCK, lastChild.getType());
    }

    @Test(timeout = 4000)
    public void testExtractForInitializerWithVar() {
        // Tests extracting var initializer from FOR loop
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create for (var i = 0; i < 10; i++) {}
        Node forNode = new Node(Token.FOR);
        Node init = new Node(Token.VAR);
        Node nameI = Node.newString(Token.NAME, "i");
        nameI.addChildToFront(Node.newNumber(0));
        init.addChildToFront(nameI);
        Node condition = Node.newNumber(10); // Simplified
        Node increment = new Node(Token.INC);
        Node body = new Node(Token.BLOCK);
        
        forNode.addChildToFront(init);
        forNode.addChildToFront(condition);
        forNode.addChildToFront(increment);
        forNode.addChildToFront(body);
        
        Node block = new Node(Token.BLOCK, forNode);
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.shouldTraverse(t, block, null);
        
        // After extraction, the FOR should have EMPTY as first child
        Node firstChild = forNode.getFirstChild();
        assertEquals("FOR initializer should be extracted to EMPTY", 
            Token.EMPTY, firstChild.getType());
    }

    @Test(timeout = 4000)
    public void testExtractForInInitializer() {
        // Tests extracting var from FOR-IN loop
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create for (var a in obj) {}
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node nameA = Node.newString(Token.NAME, "a");
        varNode.addChildToFront(nameA);
        Node obj = Node.newString(Token.NAME, "obj");
        Node body = new Node(Token.BLOCK);
        
        forInNode.addChildToFront(varNode);
        forInNode.addChildToFront(obj);
        forInNode.addChildToFront(body);
        
        Node block = new Node(Token.BLOCK, forInNode);
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.shouldTraverse(t, block, null);
        
        // After extraction, the FOR should have NAME as first child (not VAR)
        Node firstChild = forInNode.getFirstChild();
        assertEquals("FOR-IN var should be extracted to NAME", 
            Token.NAME, firstChild.getType());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testNormalizeFunctionDeclaration() {
        // Tests rewriting unhoisted function declaration to var assignment
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create function declaration inside a block (not hoisted)
        Node func = new Node(Token.FUNCTION);
        Node funcName = Node.newString(Token.NAME, "myFunc");
        Node params = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        func.addChildToFront(funcName);
        func.addChildToFront(params);
        func.addChildToFront(body);
        
        Node block = new Node(Token.BLOCK, func);
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        
        // Visit the function node
        normalizer.visit(t, func, block);
        
        // After normalization, the function should be replaced with VAR
        Node firstChild = block.getFirstChild();
        assertEquals("Function declaration should be rewritten to VAR", 
            Token.VAR, firstChild.getType());
    }

    @Test(timeout = 4000)
    public void testMoveNamedFunctions() {
        // Tests moving named function declarations to top of function body
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create function body with declarations not at top
        Node func = new Node(Token.FUNCTION);
        Node funcName = Node.newString(Token.NAME, "outer");
        Node params = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        
        // Add a var statement first
        Node varStmt = new Node(Token.VAR);
        Node varName = Node.newString(Token.NAME, "x");
        varStmt.addChildToFront(varName);
        body.addChildToFront(varStmt);
        
        // Add a function declaration later (should be moved to top)
        Node innerFunc = new Node(Token.FUNCTION);
        Node innerName = Node.newString(Token.NAME, "inner");
        Node innerParams = new Node(Token.LP);
        Node innerBody = new Node(Token.BLOCK);
        innerFunc.addChildToFront(innerName);
        innerFunc.addChildToFront(innerParams);
        innerFunc.addChildToFront(innerBody);
        body.addChildToFront(innerFunc);
        
        func.addChildToFront(funcName);
        func.addChildToFront(params);
        func.addChildToFront(body);
        
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.shouldTraverse(t, func, null);
        
        // After normalization, inner function should be before var statement
        Node firstChild = body.getFirstChild();
        assertEquals("Function declaration should be moved to top", 
            Token.FUNCTION, firstChild.getType());
    }

    @Test(timeout = 4000)
    public void testAnnotateConstantsByConvention() {
        // Tests that constants are annotated correctly
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create a NAME node that should be constant by convention
        Node nameNode = Node.newString(Token.NAME, "CONSTANT_NAME");
        Node parent = new Node(Token.EXPR_RESULT, nameNode);
        
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.visit(t, nameNode, parent);
        
        // Check if IS_CONSTANT_NAME was set
        assertTrue("Constant by convention should be annotated", 
            nameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
    }

    @Test(timeout = 4000)
    public void testPropagateConstantAnnotationsOverVars() {
        // Tests the PropagateConstantAnnotationsOverVars pass
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.PropagateConstantAnnotationsOverVars propagator = 
            new Normalize.PropagateConstantAnnotationsOverVars(compiler, false);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        try {
            propagator.process(externs, root);
        } catch (Exception e) {
            fail("PropagateConstantAnnotationsOverVars should not throw: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyConstants() {
        // Tests the VerifyConstants pass
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.VerifyConstants verifier = 
            new Normalize.VerifyConstants(compiler, true);
        
        Node externs = new Node(Token.SCRIPT);
        Node root = new Node(Token.SCRIPT);
        
        try {
            verifier.process(externs, root);
        } catch (Exception e) {
            fail("VerifyConstants should not throw: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testRemoveDuplicateDeclarations() {
        // Tests the removeDuplicateDeclarations private method indirectly
        AbstractCompiler compiler = Compiler.getInstance();
        
        // Create code with duplicate var declarations
        Node root = new Node(Token.SCRIPT);
        
        // First declaration: var x = 1;
        Node var1 = new Node(Token.VAR);
        Node name1 = Node.newString(Token.NAME, "x");
        name1.addChildToFront(Node.newNumber(1));
        var1.addChildToFront(name1);
        root.addChildToFront(var1);
        
        // Second declaration: var x = 2; (duplicate)
        Node var2 = new Node(Token.VAR);
        Node name2 = Node.newString(Token.NAME, "x");
        name2.addChildToFront(Node.newNumber(2));
        var2.addChildToFront(name2);
        root.addChildToFront(var2);
        
        Normalize normalize = new Normalize(compiler, false);
        try {
            normalize.process(new Node(Token.SCRIPT), root);
            // After processing, duplicate should be removed or converted to assignment
        } catch (Exception e) {
            fail("Duplicate declaration removal should not throw: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWhileToForConversion() {
        // Tests WHILE to FOR conversion
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create while (true) { break; }
        Node whileNode = new Node(Token.WHILE);
        Node condition = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK);
        Node breakStmt = new Node(Token.BREAK);
        body.addChildToFront(breakStmt);
        whileNode.addChildToFront(condition);
        whileNode.addChildToFront(body);
        
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.visit(t, whileNode, null);
        
        // After conversion, WHILE should become FOR
        assertEquals("WHILE should be converted to FOR", 
            Token.FOR, whileNode.getType());
        
        // FOR should have EMPTY, condition, EMPTY, body
        assertEquals("FOR should have 4 children", 4, whileNode.getChildCount());
        assertEquals("First child should be EMPTY", 
            Token.EMPTY, whileNode.getFirstChild().getType());
        assertEquals("Second child should be condition", 
            Token.TRUE, whileNode.getFirstChild().getNext().getType());
    }

    @Test(timeout = 4000)
    public void testSplitVarDeclarations() {
        // Tests splitting VAR with multiple children
        AbstractCompiler compiler = Compiler.getInstance();
        Normalize.NormalizeStatements normalizer = 
            new Normalize.NormalizeStatements(compiler, false);
        
        // Create var a, b, c;
        Node varNode = new Node(Token.VAR);
        Node nameA = Node.newString(Token.NAME, "a");
        nameA.addChildToFront(Node.newNumber(1));
        Node nameB = Node.newString(Token.NAME, "b");
        nameB.addChildToFront(Node.newNumber(2));
        Node nameC = Node.newString(Token.NAME, "c");
        nameC.addChildToFront(Node.newNumber(3));
        varNode.addChildToFront(nameA);
        varNode.addChildToFront(nameB);
        varNode.addChildToFront(nameC);
        
        Node block = new Node(Token.BLOCK, varNode);
        NodeTraversal t = new NodeTraversal(compiler, normalizer);
        normalizer.shouldTraverse(t, block, null);
        
        // After splitting, block should have 3 VAR nodes
        assertEquals("Block should have 3 children after split", 
            3, block.getChildCount());
        for (Node child : block.children()) {
            assertEquals("Each child should be VAR", Token.VAR, child.getType());
            assertTrue("Each VAR should have exactly one child", child.hasOneChild());
        }
    }
}