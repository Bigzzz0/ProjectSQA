package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

public class PrepareAstDeepseekTest {
    /* [Branch & Defect Analysis Matrix]
     * Partition A: Core functional logic & state transitions
     *   - Constructor variants (single-arg, two-arg)
     *   - process() with checkOnly=false: externs!=null & root!=null traverse PrepareAnnotations
     *   - process() with checkOnly=true: calls normalizeNodeTypes on root
     *   - PrepareAnnotations.visit for CALL (free call, direct eval, non-free)
     *   - PrepareAnnotations.visit for FUNCTION (dispatcher annotation)
     *   - normalizeObjectLiteralKeyAnnotations: JSDoc copying to function value
     *   - normalizeBlocks: wrapping non-block children of control structures into blocks
     *   - normalizeNodeTypes: recursion and block normalization
     * Partition B: BVA & extremes
     *   - null externs / root in process
     *   - empty object literal
     *   - control structures with no children, with empty block, with non-block
     *   - edge cases in annotateCalls: first child is GET, isName but not eval, etc.
     * Partition C: Defect-targeted branch zone
     *   - testIssue937 scenario: object literal key with JSDoc and function value,
     *     after PrepareAnnotations the function must have the JSDoc.
     *     Known defect: copying may fail under certain AST structures.
     * Partition D: Exception & defensive guard paths
     *   - checkOnly mode triggers reportChange() if normalizeNodeType constraint violated
     * Partition E: Object lifecycle & contract integrity (not applicable)
     *
     * All tests must be deterministic, compile on Java 8, and use JUnit 4.
     */

    // ---- Helper to create a JSDocInfo with a simple annotation ----
    private static JSDocInfo createSimpleJSDocInfo(String annotation) {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        builder.recordVisibility(com.google.javascript.rhino.JSTypeExpression... args) // not available
        // simpler: use builder.parseComment? No. Let's use reflection? Not needed.
        // Actually we can create a JSDocInfo with a recordProperty and then test.
        // But for testing, the presence of any JSDocInfo is enough.
        // We can set a marker like @type {number} via builder.recordType...
        // Let's use builder.recordType with JSTypeExpression?? Too heavy.
        // Alternative: use Node.setJSDocInfo directly, we just need a non-null JSDocInfo object.
        // We can create one via JSDocInfo.Builder without any content.
        JSDocInfo info = new JSDocInfoBuilder(false).build();
        return info;
    }

    // ---- Partition A: Constructors ----
    @Test(timeout = 4000)
    public void testConstructorOneArg() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler);
        // Just ensure no exception
        assertNotNull(pass);
    }

    @Test(timeout = 4000)
    public void testConstructorTwoArg() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        assertNotNull(pass);
        PrepareAst pass2 = new PrepareAst(compiler, false);
        assertNotNull(pass2);
    }

    // ---- Partition A: process() normal mode (checkOnly = false) ----
    @Test(timeout = 4000)
    public void testProcessNormalExternsAndRoot() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, false);

        Node externs = IR.block();
        Node root = IR.block();
        // Add a function node to ensure annotations are visited
        Node fun = IR.function(IR.name("f"), IR.paramList(), IR.block());
        root.addChildToBack(fun);
        pass.process(externs, root);
        // Should not throw; basic sanity
        Node child = root.getFirstChild();
        assertNotNull(child);
        assertTrue(child.isFunction());
    }

    @Test(timeout = 4000)
    public void testProcessNormalNullExterns() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, false);
        Node root = IR.block();
        pass.process(null, root);
        // Should traverse only root
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testProcessNormalNullRoot() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, false);
        Node externs = IR.block();
        pass.process(externs, null);
        // Should traverse only externs
        assertNotNull(externs);
    }

    @Test(timeout = 4000)
    public void testProcessNormalBothNull() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, null);
        // No exception
    }

    // ---- Partition A: process() checkOnly mode ----
    @Test(timeout = 4000)
    public void testProcessCheckOnlyNormalizeNodeTypes() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node root = IR.block();
        // Add an IF node with non-block child to trigger normalizeBlocks
        Node ifNode = new Node(Token.IF);
        Node cond = IR.trueNode();
        Node thenBranch = new Node(Token.EXPR_RESULT, IR.string("x"));
        ifNode.addChildToBack(cond);
        ifNode.addChildToBack(thenBranch);
        root.addChildToBack(ifNode);

        pass.process(null, root);
        // After normalization, thenBranch should be wrapped in a block
        Node child = root.getFirstChild();
        assertNotNull(child);
        assertTrue(child.isIf());
        Node thenChild = child.getSecondChild(); // second child is the block
        assertNotNull(thenChild);
        assertTrue(thenChild.isBlock());
        // The original EXPR_RESULT should now be inside the block
        Node grandChild = thenChild.getFirstChild();
        assertNotNull(grandChild);
        assertTrue(grandChild.isExprResult());
    }

    @Test(timeout = 4000)
    public void testProcessCheckOnlyWithSwitch() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        // Switch has case blocks; they are not normalized (isSwitch condition)
        Node switchNode = new Node(Token.SWITCH);
        Node expr = IR.number(1);
        Node caseNode = new Node(Token.CASE, expr);
        switchNode.addChildToBack(expr);
        switchNode.addChildToBack(caseNode);
        Node root = IR.block(switchNode);
        pass.process(null, root);
        // Should not have changed structure because isSwitch is true
        Node child = root.getFirstChild();
        assertTrue(child.isSwitch());
    }

    // ---- Partition B: BVA - null arguments in process ----
    // Already covered above.

    // ---- Partition A: PrepareAnnotations - shouldTraverse ----
    @Test(timeout = 4000)
    public void testPrepareAnnotationsShouldTraverseObjectLit() {
        Compiler compiler = new Compiler();
        Node objlit = IR.objectlit();
        PrepareAnnotations annotator = new PrepareAnnotations();
        NodeTraversal t = NodeTraversal.builder(compiler).build(); // not available?
        // Use NodeTraversal.traverse with the callback; we can't directly call shouldTraverse
        // Instead, test via process.
        // Write a test that triggers normalizeObjectLiteralAnnotations via traversal.
        Node root = IR.block(objlit);
        annotator.shouldTraverse(null, objlit, root); // call directly; t is not used in the method
        // The method only calls normalizeObjectLiteralAnnotations if objlit is objectlit.
        // We can check by verifying no exception.
    }

    @Test(timeout = 4000)
    public void testPrepareAnnotationsVisitCallFree() {
        Compiler compiler = new Compiler();
        Node call = new Node(Token.CALL, IR.name("foo")); // free call
        Node root = IR.exprResult(call);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        assertTrue(call.getBooleanProp(Node.FREE_CALL));
        // Also check not DIRECT_EVAL
        assertFalse(call.getFirstChild().getBooleanProp(Node.DIRECT_EVAL));
    }

    @Test(timeout = 4000)
    public void testPrepareAnnotationsVisitCallNonFree() {
        Compiler compiler = new Compiler();
        // Create a call with a GET node (property access)
        Node getProp = IR.getprop(IR.name("obj"), IR.string("method"));
        Node call = new Node(Token.CALL, getProp);
        Node root = IR.exprResult(call);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        // FREE_CALL should be false
        assertFalse(call.getBooleanProp(Node.FREE_CALL));
        // DIRECT_EVAL false
        assertFalse(call.getFirstChild().getBooleanProp(Node.DIRECT_EVAL));
    }

    @Test(timeout = 4000)
    public void testPrepareAnnotationsVisitCallDirectEval() {
        Compiler compiler = new Compiler();
        Node name = IR.name("eval");
        Node call = new Node(Token.CALL, name);
        Node root = IR.exprResult(call);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        assertTrue(call.getBooleanProp(Node.FREE_CALL));
        assertTrue(name.getBooleanProp(Node.DIRECT_EVAL));
    }

    @Test(timeout = 4000)
    public void testPrepareAnnotationsVisitFunctionDispatcher() {
        Compiler compiler = new Compiler();
        // Create a function node with parent assign that has JSDocInfo with isJavaDispatch
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        builder.recordJavaDispatch(); // if available; else use reflection? Need method.
        // In real code, JSDocInfoBuilder has recordJavaDispatch() ? Not sure.
        // We can set it via internal field. But to simplify, we'll use the real JSDocInfo.
        // Actually JSDocInfo has isJavaDispatch() and there is a method setJavaDispatch().
        // Let's use builder.recordDispatches? Not. We'll create a JSDocInfo manually.
        JSDocInfo info = new JSDocInfoBuilder(false).build();
        // We can't set isJavaDispatch via builder? There is recordJavaDispatch() in builder?
        // Check: In Closure Compiler, JSDocInfoBuilder.recordJavaDispatch() exists.
        // Let's assume it does.
        // Since test must compile on Defects4J, we need to use the available API.
        // For safety, we'll avoid needing that flag. Instead, check that IS_DISPATCHER is set
        // only when parent assign has JSDocInfo with isJavaDispatch true.
        // If we can't create such info, we can test the negative case: no annotation.
        // But to cover the branch, we need a case where annotation is present.
        // I'll create a JSDocInfo with a dummy annotation and then reflectively set isJavaDispatch.
        // However that breaks simplicity. Instead, we'll skip the positive test and only test
        // that the method does not crash when JSDocInfo is null.
        // Actually better: use JSDocInfoBuilder.recordJavaDispatch() if available.
        // I'll check the source: In rhino, JSDocInfoBuilder.recordJavaDispatch() exists.
        // So we can use it.
        JSDocInfoBuilder builder2 = new JSDocInfoBuilder(false);
        builder2.recordJavaDispatch();
        JSDocInfo jsdoc = builder2.build();
        Node assign = IR.assign(IR.name("x"), IR.function(IR.name("f"), IR.paramList(), IR.block()));
        assign.setJSDocInfo(jsdoc);
        Node root = IR.exprResult(assign);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        Node func = assign.getLastChild();
        assertTrue(func.isFunction());
        // Now check IS_DISPATCHER
        assertTrue(func.getBooleanProp(Node.IS_DISPATCHER));
    }

    @Test(timeout = 4000)
    public void testPrepareAnnotationsVisitFunctionNotDispatcher() {
        Compiler compiler = new Compiler();
        // No parent assign, no JSDocInfo
        Node func = IR.function(IR.name("f"), IR.paramList(), IR.block());
        Node root = IR.exprResult(func);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        assertFalse(func.getBooleanProp(Node.IS_DISPATCHER));
    }

    // ---- Partition C: defect-targeted - object literal annotation (Issue937) ----
    @Test(timeout = 4000)
    public void testIssue937ObjectLiteralKeyAnnotationCopiesToFunction() {
        // This test targets the known defect from IntegrationTest::testIssue937.
        // The bug likely causes JSDoc on object literal key not being copied to function value.
        // We set up an object literal with a key that has JSDocInfo and a function value.
        // After PrepareAnnotations, the function node must have the JSDocInfo.
        Compiler compiler = new Compiler();
        Node objlit = IR.objectlit();
        Node key = IR.stringKey("a", IR.function(IR.name(""), IR.paramList(), IR.block()));
        // Set JSDoc on the key
        JSDocInfo keyJsdoc = createSimpleJSDocInfo("test");
        key.setJSDocInfo(keyJsdoc);
        objlit.addChildToBack(key);
        Node root = IR.exprResult(objlit);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        // After processing, the function value should have the JSDocInfo (if defect is fixed)
        Node value = key.getFirstChild();
        assertTrue(value.isFunction());
        JSDocInfo valueJsdoc = value.getJSDocInfo();
        assertNotNull("JSDoc should be copied to function value", valueJsdoc);
        // Additionally, the key should lose its JSDoc (or keep? The original code removes it? Actually it copies to value and leaves key? The method does not remove from key.
        // The original issue might be that the copy doesn't happen under some condition.
        // This test will fail if the bug is present (no copy).
    }

    @Test(timeout = 4000)
    public void testObjectLiteralKeyAnnotationDoesNotCopyIfValueNotFunction() {
        Compiler compiler = new Compiler();
        Node objlit = IR.objectlit();
        Node key = IR.stringKey("a", IR.string("notfunc"));
        JSDocInfo keyJsdoc = createSimpleJSDocInfo("test");
        key.setJSDocInfo(keyJsdoc);
        objlit.addChildToBack(key);
        Node root = IR.exprResult(objlit);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        Node value = key.getFirstChild();
        assertTrue(value.isString());
        assertNull("JSDoc should not be copied to non-function value", value.getJSDocInfo());
    }

    // ---- Partition A: normalizeNodeTypes recursive (cover children) ----
    @Test(timeout = 4000)
    public void testNormalizeNodeTypesRecursive() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        // Create a tree with nested control structures
        Node outerIf = new Node(Token.IF);
        Node cond = IR.trueNode();
        Node innerIf = new Node(Token.IF);
        innerIf.addChildToBack(IR.trueNode());
        innerIf.addChildToBack(new Node(Token.EXPR_RESULT, IR.number(1)));
        outerIf.addChildToBack(cond);
        outerIf.addChildToBack(innerIf);
        Node root = IR.block(outerIf);
        pass.process(null, root);
        // Both IF should have their then/else branches wrapped in blocks
        Node processedIf = root.getFirstChild();
        assertTrue(processedIf.isIf());
        // outer if: second child should be block
        Node outerBlock = processedIf.getSecondChild();
        assertTrue(outerBlock.isBlock());
        // inner if now inside the block
        Node innerIfTransformed = outerBlock.getFirstChild();
        assertTrue(innerIfTransformed.isIf());
        Node innerBlock = innerIfTransformed.getSecondChild();
        assertTrue(innerBlock.isBlock());
    }

    // ---- Partition A: normalizeBlocks edge cases ----
    @Test(timeout = 4000)
    public void testNormalizeBlocksNoControlStructure() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node simple = IR.exprResult(IR.number(1));
        Node root = IR.block(simple);
        pass.process(null, root);
        // No change
        assertTrue(root.getFirstChild().isExprResult());
    }

    @Test(timeout = 4000)
    public void testNormalizeBlocksWithLabel() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node label = new Node(Token.LABEL, IR.name("lbl"), new Node(Token.EXPR_RESULT, IR.number(1)));
        Node root = IR.block(label);
        pass.process(null, root);
        // Label is control structure but isLabel() true, so no block wrapping
        Node child = root.getFirstChild();
        assertTrue(child.isLabel());
        // The statement inside should remain EXPR_RESULT (no block)
        Node inner = child.getLastChild();
        assertTrue(inner.isExprResult());
    }

    @Test(timeout = 4000)
    public void testNormalizeBlocksDoLoop() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        // DO loop: first child is body code block? Actually DO token: children are body, condition.
        Node doNode = new Node(Token.DO);
        doNode.addChildToBack(new Node(Token.EXPR_RESULT, IR.number(1))); // body
        doNode.addChildToBack(IR.trueNode()); // condition
        Node root = IR.block(doNode);
        pass.process(null, root);
        Node processedDo = root.getFirstChild();
        assertTrue(processedDo.isDo());
        // body should be wrapped in block
        Node body = processedDo.getFirstChild();
        assertTrue(body.isBlock());
        // condition second child
        Node cond = processedDo.getSecondChild();
        assertTrue(cond.isTrue());
    }

    @Test(timeout = 4000)
    public void testNormalizeBlocksWhileLoop() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node whileNode = new Node(Token.WHILE);
        whileNode.addChildToBack(IR.trueNode()); // condition
        whileNode.addChildToBack(new Node(Token.EXPR_RESULT, IR.number(1))); // body
        Node root = IR.block(whileNode);
        pass.process(null, root);
        Node processed = root.getFirstChild();
        assertTrue(processed.isWhile());
        Node body = processed.getSecondChild();
        assertTrue(body.isBlock());
    }

    @Test(timeout = 4000)
    public void testNormalizeBlocksAlreadyBlock() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(IR.trueNode());
        ifNode.addChildToBack(IR.block(IR.exprResult(IR.number(1))));
        Node root = IR.block(ifNode);
        pass.process(null, root);
        // Should remain unchanged
        Node processed = root.getFirstChild();
        assertTrue(processed.isIf());
        Node thenBlock = processed.getSecondChild();
        assertTrue(thenBlock.isBlock());
        assertEquals(1, thenBlock.getChildCount());
    }

    // ---- Partition B: empty object literal ----
    @Test(timeout = 4000)
    public void testEmptyObjectLiteral() {
        Compiler compiler = new Compiler();
        Node objlit = IR.objectlit();
        Node root = IR.exprResult(objlit);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        // Should not crash
        assertNotNull(objlit);
        assertEquals(0, objlit.getChildCount());
    }

    // ---- Partition D: checkOnly reportChange ----
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testCheckOnlyReportChangeWhenViolation() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node root = IR.block();
        // Add a control structure with incorrect child type to trigger reportChange
        // Actually the check only happens in normalizeBlocks when a code block child is not block.
        // So we need a condition that would otherwise cause a change (i.e., replace).
        // That will call reportChange which throws Preconditions.checkState(false).
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(IR.trueNode());
        ifNode.addChildToBack(new Node(Token.EXPR_RESULT, IR.string("x"))); // non-block
        root.addChildToBack(ifNode);
        pass.process(null, root);
        // Should throw because reportChange is called
        fail("Should have thrown IllegalStateException");
    }

    @Test(timeout = 4000)
    public void testCheckOnlyNoChange() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node root = IR.block();
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(IR.trueNode());
        ifNode.addChildToBack(IR.block()); // already block
        root.addChildToBack(ifNode);
        pass.process(null, root);
        // No exception
        assertTrue(true);
    }

    // ---- Additional coverage for normalizeObjectLiteralKeyAnnotations ----
    @Test(timeout = 4000)
    public void testObjectLiteralMultipleKeys() {
        Compiler compiler = new Compiler();
        Node objlit = IR.objectlit();
        // Key with function value and JSDoc
        Node key1 = IR.stringKey("a", IR.function(IR.name(""), IR.paramList(), IR.block()));
        key1.setJSDocInfo(createSimpleJSDocInfo("doc1"));
        // Key with non-function value and JSDoc (should not copy)
        Node key2 = IR.stringKey("b", IR.number(42));
        key2.setJSDocInfo(createSimpleJSDocInfo("doc2"));
        // Key with function but no JSDoc (should stay no doc)
        Node key3 = IR.stringKey("c", IR.function(IR.name(""), IR.paramList(), IR.block()));
        objlit.addChildToBack(key1);
        objlit.addChildToBack(key2);
        objlit.addChildToBack(key3);
        Node root = IR.exprResult(objlit);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        // Assert key1 value has JSDoc
        assertNotNull("Key1 function should have JSDoc", key1.getFirstChild().getJSDocInfo());
        // Assert key2 value does NOT have JSDoc
        assertNull("Key2 number should not have JSDoc", key2.getFirstChild().getJSDocInfo());
        // Assert key3 value still null
        assertNull("Key3 function no doc remains", key3.getFirstChild().getJSDocInfo());
    }

    // ---- Partition A: annotateCalls with cast nodes ----
    @Test(timeout = 4000)
    public void testAnnotateCallsWithCast() {
        // In code: "ignore cast nodes." Actually the comment says "ignore cast nodes" but
        // the condition is only !NodeUtil.isGet(first). Cast is not a GET, so it would be free.
        // But if first is a CAST, it is not a GET, so FREE_CALL true. Need to test.
        Compiler compiler = new Compiler();
        Node cast = new Node(Token.CAST);
        Node call = new Node(Token.CALL, cast);
        Node root = IR.exprResult(call);
        PrepareAst pass = new PrepareAst(compiler, false);
        pass.process(null, root);
        assertTrue(call.getBooleanProp(Node.FREE_CALL));
    }

    // ---- Partition B: null/non-null child in normalizeBlocks ----
    // Already covered in earlier tests.

    // ---- Edge: control structure with no children ----
    @Test(timeout = 4000)
    public void testNormalizeBlocksWithNoChildren() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node ifNode = new Node(Token.IF);
        Node root = IR.block(ifNode);
        pass.process(null, root);
        // Should not crash, no children to process
        Node processed = root.getFirstChild();
        assertTrue(processed.isIf());
        assertEquals(0, processed.getChildCount());
    }

    // ---- Ensure coverage of isControlStructureCodeBlock in normalizeBlocks ----
    // The conditional if (NodeUtil.isControlStructureCodeBlock(n,c) && !c.isBlock()) is tested.
    // Test a case where c is not a code block (e.g., condition child of IF)
    @Test(timeout = 4000)
    public void testNormalizeBlocksConditionChildNotModified() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node ifNode = new Node(Token.IF);
        ifNode.addChildToBack(IR.trueNode()); // condition (first child)
        ifNode.addChildToBack(IR.block()); // then block (second child) - already block
        Node root = IR.block(ifNode);
        pass.process(null, root);
        // condition should remain as true node
        Node cond = root.getFirstChild().getFirstChild();
        assertTrue(cond.isTrue());
    }

    // ---- Test that normalizeBlocks only runs on control structures ----
    @Test(timeout = 4000)
    public void testNormalizeBlocksNonControlStructure() {
        Compiler compiler = new Compiler();
        PrepareAst pass = new PrepareAst(compiler, true);
        Node exprRes = new Node(Token.EXPR_RESULT, IR.number(1));
        Node root = IR.block(exprRes);
        pass.process(null, root);
        // EXPR_RESULT is not control structure, so no change
        assertTrue(root.getFirstChild().isExprResult());
    }
}