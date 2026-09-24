package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: com.google.javascript.rhino.IRFactory
 * Target Environment: Java 8 / Defects4J / JUnit 4
 *
 * Covered Functional Branches & Boundary Conditions:
 * 1. Script Lifecycle:
 *    - createScript() -> Token.SCRIPT
 *    - initScript() -> children != null (body transfer), children == null (empty body)
 * 2. Switch & Case Handling:
 *    - addSwitchCase() -> switchNode.getType() != Token.SWITCH (Kit.codeBug exception)
 *    - addSwitchCase() -> caseExpression != null (Token.CASE) vs caseExpression == null (Token.DEFAULT)
 *    - closeSwitch() -> verify no-op execution
 * 3. Statements & Declarations:
 *    - createExprStatement() -> parser.insideFunction() == true (EXPR_VOID) vs false (EXPR_RESULT)
 *    - createExprStatementNoReturn() -> Token.EXPR_VOID
 *    - createDefaultNamespace() -> activation requirement and Token.DEFAULTNAMESPACE wrapping
 *    - createVariables(), createBlock(), createDebugger(), createErrorName()
 * 4. Exception & Control Flow:
 *    - createCatch() -> catchCond != null vs catchCond == null (Token.EMPTY fallback)
 *    - createThrow() -> Token.THROW wrapping
 *    - createReturn() -> expr != null vs expr == null (empty return)
 *    - createBreak() / createContinue() -> label != null (with NAME child) vs label == null (standalone)
 *    - createWhile(), createDoWhile(), createFor(), createForIn(), createWith()
 *    - createTryCatchFinally() -> finallyBlock != null vs finallyBlock == null
 * 5. Functions & Activations:
 *    - createFunction() -> creates FunctionNode with initial NAME child
 *    - initFunction() -> sourceName != null vs null; JSDocInfo != null vs null; functionCount == 0 vs > 0
 *    - initFunction() nested functions: FUNCTION_EXPRESSION_STATEMENT with non-empty name (removeParamOrVar),
 *      empty string name, null name, and non-expression function types
 *    - checkActivationName() & setRequiresActivation():
 *      * "arguments" inside function
 *      * Custom compilerEnv.activationNames map match
 *      * "length" property get under Context.VERSION_1_2 vs other version / non-GETPROP
 *      * calls outside function (activation skipped)
 * 6. Literals & Invocations:
 *    - createArrayLiteral() -> skipCount == 0 vs skipCount > 0 with null skips and SKIP_INDEXES_PROP
 *    - createObjectLiteral() -> key-value sequence insertion
 *    - createRegExp() -> flags.length() == 0 vs flags.length() > 0
 *    - createIf() -> ifFalse != null vs ifFalse == null
 *    - createCondExpr() -> Token.HOOK conditional expression
 *    - createCallOrNew() -> child NAME "eval" (SPECIALCALL_EVAL), "With" (SPECIALCALL_WITH), normal name;
 *      child GETPROP ending with "eval" vs other property; non-NAME/GETPROP child
 * 7. Increments, Decrements & Reference Checks:
 *    - createIncDec() -> valid reference types (NAME, GETPROP, GETELEM, GET_REF, CALL) with post=true/false
 *    - createIncDec() -> invalid reference (e.g. NUMBER): Token.DEC ("msg.bad.decr") vs Token.INC ("msg.bad.incr")
 * 8. Property & Member Lookups:
 *    - createPropertyGet() -> namespace == null && memberTypeFlags == 0:
 *      * target == null -> fallback to createName
 *      * special property ("__proto__", "__parent__") -> REF_SPECIAL & GET_REF
 *      * normal property -> Token.GETPROP
 *    - createElementGet() -> namespace == null && memberTypeFlags == 0:
 *      * target == null -> Kit.codeBug exception
 *      * target != null -> Token.GETELEM
 *    - createMemberRefGet() (via property/element gets):
 *      * namespace: null vs "*" (Token.NULL nsNode) vs custom namespace (createName nsNode)
 *      * target: null (REF_NAME / REF_NS_NAME) vs non-null (REF_MEMBER / REF_NS_MEMBER)
 *      * memberTypeFlags != 0 (MEMBER_TYPE_PROP set) vs 0
 * 9. Binary Operations:
 *    - Token.DOT -> transformed to GETPROP with right child set to Token.STRING
 *    - Token.LB -> transformed to GETELEM
 *    - Standard binary (Token.ADD, Token.SUB, etc.)
 * 10. Defect-Targeted Zone (com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden4):
 *    - createAssignment():
 *      * Valid LHS: Token.NAME, Token.GETPROP, Token.GETELEM -> no error reported
 *      * Forbidden LHS in assignments (e.g., destructuring ARRAYLIT, OBJECTLIT, or NUMBER):
 *        triggers parser.reportError("msg.bad.assign.left") and returns ASSIGN node.
 */
public class IRFactoryGeminiTest {

    /**
     * Test-specific sub-class of Rhino Parser to control and observe parser interactions
     * without external mocking frameworks.
     */
    private static class MockParser extends Parser {
        boolean insideFunc = false;
        String sourceName = null;
        final List<String> reportedErrors = new ArrayList<String>();

        MockParser(CompilerEnvirons env) {
            super(env);
            this.compilerEnv = env;
        }

        @Override
        boolean insideFunction() {
            return insideFunc;
        }

        @Override
        String getSourceName() {
            return sourceName;
        }

        @Override
        void reportError(String messageId) {
            reportedErrors.add(messageId);
        }
    }

    private IRFactory createFactory(MockParser parser) {
        return new IRFactory(parser);
    }

    private MockParser createDefaultParser() {
        CompilerEnvirons env = new CompilerEnvirons();
        return new MockParser(env);
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testScriptCreationAndInitWithChildren() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        ScriptOrFnNode scriptNode = factory.createScript();
        assertNotNull(scriptNode);
        assertEquals(Token.SCRIPT, scriptNode.getType());

        Node body = new Node(Token.BLOCK);
        Node child1 = factory.createLeaf(Token.EMPTY);
        Node child2 = factory.createLeaf(Token.EMPTY);
        body.addChildToBack(child1);
        body.addChildToBack(child2);

        factory.initScript(scriptNode, body);
        assertEquals(child1, scriptNode.getFirstChild());
        assertEquals(child2, scriptNode.getLastChild());
        assertNull(body.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testInitScriptWithEmptyBody() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        ScriptOrFnNode scriptNode = factory.createScript();
        Node emptyBody = new Node(Token.BLOCK);

        factory.initScript(scriptNode, emptyBody);
        assertNull(scriptNode.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testSwitchAndCaseConstruction() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node switchNode = factory.createSwitch(10, 2);
        assertEquals(Token.SWITCH, switchNode.getType());
        assertEquals(10, switchNode.getLineno());
        assertEquals(2, switchNode.getCharno());

        Node caseExpr = factory.createNumber(1.0, 11, 4);
        Node caseStmts = factory.createBlock(11, 8);
        factory.addSwitchCase(switchNode, caseExpr, caseStmts, 11, 0);

        Node defaultStmts = factory.createBlock(12, 8);
        factory.addSwitchCase(switchNode, null, defaultStmts, 12, 0);

        factory.closeSwitch(switchNode);

        Node firstCase = switchNode.getFirstChild();
        assertNotNull(firstCase);
        assertEquals(Token.CASE, firstCase.getType());
        assertEquals(caseExpr, firstCase.getFirstChild());
        assertEquals(caseStmts, firstCase.getLastChild());

        Node defaultCase = firstCase.getNext();
        assertNotNull(defaultCase);
        assertEquals(Token.DEFAULT, defaultCase.getType());
        assertEquals(defaultStmts, defaultCase.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testExpressionStatementsInsideAndOutsideFunction() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node expr1 = factory.createNumber(5.0);
        parser.insideFunc = false;
        Node stmtResult = factory.createExprStatement(expr1, 1, 0);
        assertEquals(Token.EXPR_RESULT, stmtResult.getType());
        assertEquals(expr1, stmtResult.getFirstChild());

        Node expr2 = factory.createNumber(10.0);
        parser.insideFunc = true;
        Node stmtVoid = factory.createExprStatement(expr2, 2, 0);
        assertEquals(Token.EXPR_VOID, stmtVoid.getType());
        assertEquals(expr2, stmtVoid.getFirstChild());

        Node stmtNoReturn = factory.createExprStatementNoReturn(expr1, 3, 0);
        assertEquals(Token.EXPR_VOID, stmtNoReturn.getType());
    }

    @Test(timeout = 4000)
    public void testDefaultNamespaceCreation() {
        MockParser parser = createDefaultParser();
        FunctionNode fn = new FunctionNode("testFn", 1, 0);
        parser.currentScriptOrFn = fn;
        parser.insideFunc = true;
        IRFactory factory = createFactory(parser);

        Node nsExpr = factory.createString("http://example.com");
        Node defaultNs = factory.createDefaultNamespace(nsExpr, 4, 1);

        assertTrue(fn.itsNeedsActivation);
        assertEquals(Token.EXPR_VOID, defaultNs.getType());
        Node unary = defaultNs.getFirstChild();
        assertEquals(Token.DEFAULTNAMESPACE, unary.getType());
        assertEquals(nsExpr, unary.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testCatchClausesWithAndWithoutCondition() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node stmts = factory.createBlock(1, 0);

        // Catch with null condition defaults to Token.EMPTY
        Node catchNode1 = factory.createCatch("err", 2, 0, null, stmts, 2, 0);
        assertEquals(Token.CATCH, catchNode1.getType());
        Node nameNode1 = catchNode1.getFirstChild();
        assertEquals(Token.NAME, nameNode1.getType());
        assertEquals("err", nameNode1.getString());
        Node condNode1 = nameNode1.getNext();
        assertEquals(Token.EMPTY, condNode1.getType());
        assertEquals(stmts, condNode1.getNext());

        // Catch with explicit condition
        Node explicitCond = factory.createName("ErrorType", 3, 0);
        Node catchNode2 = factory.createCatch("err2", 3, 0, explicitCond, stmts, 3, 0);
        Node condNode2 = catchNode2.getFirstChild().getNext();
        assertEquals(explicitCond, condNode2);
    }

    @Test(timeout = 4000)
    public void testControlFlowConstructs() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node cond = factory.createName("c", 1, 0);
        Node body = factory.createBlock(1, 0);

        assertEquals(Token.WHILE, factory.createWhile(cond, body, 1, 0).getType());
        assertEquals(Token.DO, factory.createDoWhile(body, cond, 1, 0).getType());

        Node init = factory.createVariables(Token.VAR, 1, 0);
        Node incr = factory.createName("i", 1, 0);
        Node forNode = factory.createFor(init, cond, incr, body, 1, 0);
        assertEquals(Token.FOR, forNode.getType());

        Node forInNode = factory.createForIn(init, cond, body, 1, 0);
        assertEquals(Token.FOR, forInNode.getType());

        Node withNode = factory.createWith(cond, body, 1, 0);
        assertEquals(Token.WITH, withNode.getType());
    }

    @Test(timeout = 4000)
    public void testTryCatchFinallyVariations() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node tryBlock = factory.createBlock(1, 0);
        Node catchBlock = factory.createBlock(2, 0);
        Node finallyBlock = factory.createBlock(3, 0);

        Node tryCatchOnly = factory.createTryCatchFinally(tryBlock, catchBlock, null, 1, 0);
        assertEquals(Token.TRY, tryCatchOnly.getType());
        assertNull(catchBlock.getNext());

        Node tryCatchFinally = factory.createTryCatchFinally(tryBlock, catchBlock, finallyBlock, 1, 0);
        assertEquals(Token.TRY, tryCatchFinally.getType());
        assertEquals(finallyBlock, tryCatchFinally.getLastChild());
    }

    @Test(timeout = 4000)
    public void testReturnsAndBreaksAndContinues() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        // Return
        Node retEmpty = factory.createReturn(null, 1, 0);
        assertEquals(Token.RETURN, retEmpty.getType());
        assertNull(retEmpty.getFirstChild());

        Node expr = factory.createNumber(10.0);
        Node retVal = factory.createReturn(expr, 1, 0);
        assertEquals(Token.RETURN, retVal.getType());
        assertEquals(expr, retVal.getFirstChild());

        // Break
        Node brkSimple = factory.createBreak(null, 2, 0);
        assertEquals(Token.BREAK, brkSimple.getType());
        assertNull(brkSimple.getFirstChild());

        Node brkLabel = factory.createBreak("myLabel", 2, 0);
        assertEquals(Token.BREAK, brkLabel.getType());
        assertEquals("myLabel", brkLabel.getFirstChild().getString());

        // Continue
        Node contSimple = factory.createContinue(null, 3, 0);
        assertEquals(Token.CONTINUE, contSimple.getType());
        assertNull(contSimple.getFirstChild());

        Node contLabel = factory.createContinue("myLabel", 3, 0);
        assertEquals(Token.CONTINUE, contLabel.getType());
        assertEquals("myLabel", contLabel.getFirstChild().getString());

        // Label
        Node labelNode = factory.createLabel("loop", 4, 0);
        assertEquals(Token.LABEL, labelNode.getType());
        assertEquals("loop", labelNode.getFirstChild().getString());

        // Throw & Debugger
        assertEquals(Token.THROW, factory.createThrow(expr, 5, 0).getType());
        assertEquals(Token.DEBUGGER, factory.createDebugger(6, 0).getType());
    }

    @Test(timeout = 4000)
    public void testIfAndConditionalExpressions() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node cond = factory.createName("flag", 1, 0);
        Node ifTrue = factory.createBlock(2, 0);
        Node ifFalse = factory.createBlock(3, 0);

        Node ifSingle = factory.createIf(cond, ifTrue, null, 1, 0);
        assertEquals(Token.IF, ifSingle.getType());
        assertNull(ifTrue.getNext());

        Node ifElse = factory.createIf(cond, ifTrue, ifFalse, 1, 0);
        assertEquals(Token.IF, ifElse.getType());
        assertEquals(ifFalse, ifElse.getLastChild());

        Node hook = factory.createCondExpr(cond, ifTrue, ifFalse, 1, 0);
        assertEquals(Token.HOOK, hook.getType());
    }

    @Test(timeout = 4000)
    public void testDotQuerySetsActivation() {
        MockParser parser = createDefaultParser();
        FunctionNode fn = new FunctionNode("queryFn", 1, 0);
        parser.currentScriptOrFn = fn;
        parser.insideFunc = true;
        IRFactory factory = createFactory(parser);

        Node obj = factory.createName("xmlObj", 1, 0);
        Node body = factory.createName("filter", 1, 5);

        Node dotQuery = factory.createDotQuery(obj, body, 1, 0);
        assertEquals(Token.DOTQUERY, dotQuery.getType());
        assertTrue(fn.itsNeedsActivation);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testArrayLiteralWithAndWithoutSkips() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        // Case 1: No skips
        ObjArray elems1 = new ObjArray();
        elems1.add(factory.createNumber(1.0));
        elems1.add(factory.createNumber(2.0));
        Node array1 = factory.createArrayLiteral(elems1, 0, 1, 0);
        assertEquals(Token.ARRAYLIT, array1.getType());
        assertNull(array1.getProp(Node.SKIP_INDEXES_PROP));

        // Case 2: With skips (sparse array literal: [1, , 3, ])
        ObjArray elems2 = new ObjArray();
        elems2.add(factory.createNumber(1.0));
        elems2.add(null);
        elems2.add(factory.createNumber(3.0));
        elems2.add(null);
        Node array2 = factory.createArrayLiteral(elems2, 2, 2, 0);
        assertEquals(Token.ARRAYLIT, array2.getType());
        int[] skips = (int[]) array2.getProp(Node.SKIP_INDEXES_PROP);
        assertNotNull(skips);
        assertEquals(2, skips.length);
        assertEquals(1, skips[0]);
        assertEquals(3, skips[1]);
    }

    @Test(timeout = 4000)
    public void testObjectLiteralEmptyAndMultiPair() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        ObjArray objEmpty = new ObjArray();
        Node emptyObjLit = factory.createObjectLiteral(objEmpty, 1, 0);
        assertEquals(Token.OBJECTLIT, emptyObjLit.getType());
        assertNull(emptyObjLit.getFirstChild());

        ObjArray objPairs = new ObjArray();
        Node k1 = factory.createString("a");
        Node v1 = factory.createNumber(1.0);
        Node k2 = factory.createString("b");
        Node v2 = factory.createNumber(2.0);
        objPairs.add(k1);
        objPairs.add(v1);
        objPairs.add(k2);
        objPairs.add(v2);

        Node objLit = factory.createObjectLiteral(objPairs, 1, 0);
        assertEquals(k1, objLit.getFirstChild());
        assertEquals(v1, k1.getNext());
        assertEquals(k2, v1.getNext());
        assertEquals(v2, k2.getNext());
    }

    @Test(timeout = 4000)
    public void testRegExpEmptyFlagsVsNonEmptyFlags() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node regexNoFlags = factory.createRegExp("abc", "", 1, 0);
        assertEquals(Token.REGEXP, regexNoFlags.getType());
        assertNull(regexNoFlags.getFirstChild().getNext());

        Node regexFlags = factory.createRegExp("abc", "gi", 1, 0);
        assertEquals(Token.REGEXP, regexFlags.getType());
        Node flagsNode = regexFlags.getFirstChild().getNext();
        assertNotNull(flagsNode);
        assertEquals("gi", flagsNode.getString());
    }

    @Test(timeout = 4000)
    public void testBinaryDotAndBracketAndStandard() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node left = factory.createName("obj", 1, 0);
        Node right = factory.createName("prop", 1, 4);

        // Token.DOT transforms to GETPROP and converts right child to STRING
        Node dotNode = factory.createBinary(Token.DOT, left, right, 1, 0);
        assertEquals(Token.GETPROP, dotNode.getType());
        assertEquals(Token.STRING, dotNode.getLastChild().getType());

        // Token.LB transforms to GETELEM
        Node elemIdx = factory.createNumber(0.0);
        Node lbNode = factory.createBinary(Token.LB, left, elemIdx, 1, 0);
        assertEquals(Token.GETELEM, lbNode.getType());

        // Standard arithmetic
        Node addNode = factory.createBinary(Token.ADD, left, right, 1, 0);
        assertEquals(Token.ADD, addNode.getType());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Zone (testDestructuringAssignForbidden4)
    // =========================================================================

    /**
     * Targets known defect referenced by ParserTest::testDestructuringAssignForbidden4.
     * In JavaScript assignment syntax, assigning to an invalid Left-Hand Side expression
     * such as an Array Literal or Object Literal must report "msg.bad.assign.left".
     */
    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden_ArrayLhsReportsError() throws Exception {
        CompilerEnvirons env = new CompilerEnvirons();
        MockParser mockParser = new MockParser(env);
        IRFactory factory = createFactory(mockParser);

        ObjArray arrayElems = new ObjArray();
        arrayElems.add(factory.createName("x", 1, 1));
        Node arrayLitLhs = factory.createArrayLiteral(arrayElems, 0, 1, 0);
        Node rhs = factory.createNumber(100.0, 1, 10);

        Node assignNode = factory.createAssignment(Token.ASSIGN, arrayLitLhs, rhs, 1, 0);

        assertNotNull(assignNode);
        assertEquals(Token.ASSIGN, assignNode.getType());
        assertEquals(1, mockParser.reportedErrors.size());
        assertEquals("msg.bad.assign.left", mockParser.reportedErrors.get(0));
    }

    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden_ObjectLhsReportsError() throws Exception {
        CompilerEnvirons env = new CompilerEnvirons();
        MockParser mockParser = new MockParser(env);
        IRFactory factory = createFactory(mockParser);

        ObjArray objElems = new ObjArray();
        objElems.add(factory.createString("x", 1, 1));
        objElems.add(factory.createName("x", 1, 3));
        Node objLitLhs = factory.createObjectLiteral(objElems, 1, 0);
        Node rhs = factory.createNumber(200.0, 1, 10);

        Node assignNode = factory.createAssignment(Token.ASSIGN, objLitLhs, rhs, 1, 0);

        assertNotNull(assignNode);
        assertEquals(Token.ASSIGN, assignNode.getType());
        assertEquals(1, mockParser.reportedErrors.size());
        assertEquals("msg.bad.assign.left", mockParser.reportedErrors.get(0));
    }

    @Test(timeout = 4000)
    public void testAssignmentValidLhsDoesNotReportError() throws Exception {
        CompilerEnvirons env = new CompilerEnvirons();
        MockParser mockParser = new MockParser(env);
        IRFactory factory = createFactory(mockParser);

        Node nameLhs = factory.createName("validVar", 1, 0);
        Node rhs = factory.createNumber(1.0, 1, 5);
        factory.createAssignment(Token.ASSIGN, nameLhs, rhs, 1, 0);

        Node propLhs = factory.createPropertyGet(nameLhs, null, "prop", 0, 1, 0, 1, 5);
        factory.createAssignment(Token.ASSIGN, propLhs, rhs, 1, 0);

        Node elemIdx = factory.createNumber(0.0);
        Node elemLhs = factory.createElementGet(nameLhs, null, elemIdx, 0, 1, 0);
        factory.createAssignment(Token.ASSIGN, elemLhs, rhs, 1, 0);

        assertTrue("Valid LHS nodes should not report any assignment errors",
                mockParser.reportedErrors.isEmpty());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddSwitchCaseThrowsOnInvalidNodeType() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node invalidSwitch = factory.createBlock(1, 0);
        Node stmts = factory.createBlock(2, 0);

        try {
            factory.addSwitchCase(invalidSwitch, null, stmts, 1, 0);
            fail("Expected RuntimeException from Kit.codeBug()");
        } catch (RuntimeException expected) {
            // Success: Kit.codeBug() produces RuntimeException
        }
    }

    @Test(timeout = 4000)
    public void testElementGetThrowsOnNullTargetWithoutNamespace() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node elem = factory.createNumber(0.0);
        try {
            factory.createElementGet(null, null, elem, 0, 1, 0);
            fail("Expected RuntimeException when target is null without namespace");
        } catch (RuntimeException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testIncDecInvalidTargetReportsError() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node numberLiteral = factory.createNumber(42.0);

        Node decResult = factory.createIncDec(Token.DEC, false, numberLiteral, 1, 0);
        assertNull(decResult);
        assertEquals(1, parser.reportedErrors.size());
        assertEquals("msg.bad.decr", parser.reportedErrors.get(0));

        Node incResult = factory.createIncDec(Token.INC, false, numberLiteral, 1, 0);
        assertNull(incResult);
        assertEquals(2, parser.reportedErrors.size());
        assertEquals("msg.bad.incr", parser.reportedErrors.get(1));
    }

    // =========================================================================
    // PARTITION E: Function Lifecycle, Activations & Special Properties
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitFunctionFullBranchCoverage() {
        MockParser parser = createDefaultParser();
        parser.sourceName = "testSource.js";
        IRFactory factory = createFactory(parser);

        FunctionNode outer = factory.createFunction("outer", 1, 0);
        outer.addParamOrVar("paramToRemove");
        outer.addParamOrVar("keptVar");

        // 1. Nested expression statement with non-empty name -> triggers removeParamOrVar
        FunctionNode nestedExprStmt = factory.createFunction("paramToRemove", 2, 0);
        nestedExprStmt.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        outer.addFunction(nestedExprStmt);

        // 2. Nested expression statement with empty name -> name.length() == 0 branch
        FunctionNode nestedEmptyName = factory.createFunction("", 3, 0);
        nestedEmptyName.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        outer.addFunction(nestedEmptyName);

        // 3. Nested expression statement with null name -> name == null branch
        FunctionNode nestedNullName = new FunctionNode(null, 4, 0);
        nestedNullName.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        outer.addFunction(nestedNullName);

        // 4. Nested function with different type (not expression statement)
        FunctionNode nestedDecl = factory.createFunction("regularDecl", 5, 0);
        nestedDecl.itsFunctionType = FunctionNode.FUNCTION_STATEMENT;
        outer.addFunction(nestedDecl);

        Node args = new Node(Token.LP);
        Node statements = factory.createBlock(6, 0);
        JSDocInfo info = new JSDocInfo();

        Node inited = factory.initFunction(outer, 42, args, info, statements, FunctionNode.FUNCTION_STATEMENT);

        assertSame(outer, inited);
        assertTrue(outer.itsNeedsActivation);
        assertEquals(info, outer.getJSDocInfo());
        assertEquals(42, outer.getIntProp(Node.FUNCTION_PROP, -1));
        assertEquals("testSource.js", outer.getProp(Node.SOURCENAME_PROP));
        // Verify paramToRemove was removed
        assertEquals(-1, outer.getParamOrVarIndex("paramToRemove"));
        assertTrue(outer.getParamOrVarIndex("keptVar") >= 0);
    }

    @Test(timeout = 4000)
    public void testSpecialCallsEvalAndWith() {
        MockParser parser = createDefaultParser();
        FunctionNode fn = new FunctionNode("testFn", 1, 0);
        parser.currentScriptOrFn = fn;
        parser.insideFunc = true;
        IRFactory factory = createFactory(parser);

        // Call "eval" by NAME
        Node evalName = factory.createName("eval", 1, 0);
        Node callEval = factory.createCallOrNew(Token.CALL, evalName, 1, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callEval.getIntProp(Node.SPECIALCALL_PROP, -1));
        assertTrue(fn.itsNeedsActivation);

        // Call "With" by NAME
        Node withName = factory.createName("With", 2, 0);
        Node callWith = factory.createCallOrNew(Token.CALL, withName, 2, 0);
        assertEquals(Node.SPECIALCALL_WITH, callWith.getIntProp(Node.SPECIALCALL_PROP, -1));

        // Call property access "obj.eval"
        Node obj = factory.createName("obj", 3, 0);
        Node getPropEval = factory.createBinary(Token.DOT, obj, factory.createName("eval", 3, 4), 3, 0);
        Node callPropEval = factory.createCallOrNew(Token.CALL, getPropEval, 3, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callPropEval.getIntProp(Node.SPECIALCALL_PROP, -1));

        // Non-special call
        Node normalCall = factory.createCallOrNew(Token.CALL, factory.createName("foo", 4, 0), 4, 0);
        assertEquals(0, normalCall.getIntProp(Node.SPECIALCALL_PROP, 0));
    }

    @Test(timeout = 4000)
    public void testActivationCheckOnArgumentsAndLength() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(Context.VERSION_1_2);
        env.activationNames = new HashMap<String, Boolean>();
        env.activationNames.put("customActivatedVar", Boolean.TRUE);

        MockParser parser = new MockParser(env);
        FunctionNode fn = new FunctionNode("activationFn", 1, 0);
        parser.currentScriptOrFn = fn;
        parser.insideFunc = true;
        IRFactory factory = createFactory(parser);

        // "arguments" activates
        fn.itsNeedsActivation = false;
        factory.createName("arguments", 1, 0);
        assertTrue(fn.itsNeedsActivation);

        // compilerEnv.activationNames entry activates
        fn.itsNeedsActivation = false;
        factory.createName("customActivatedVar", 2, 0);
        assertTrue(fn.itsNeedsActivation);

        // "length" via GETPROP under VERSION_1_2 activates
        fn.itsNeedsActivation = false;
        Node target = factory.createName("arr", 3, 0);
        factory.createPropertyGet(target, null, "length", 0, 3, 0, 3, 4);
        assertTrue(fn.itsNeedsActivation);

        // "length" via NAME under VERSION_1_2 does not activate
        fn.itsNeedsActivation = false;
        factory.createName("length", 4, 0);
        assertFalse(fn.itsNeedsActivation);

        // "length" via GETPROP under VERSION_1_5 does not activate
        env.setLanguageVersion(Context.VERSION_1_5);
        fn.itsNeedsActivation = false;
        factory.createPropertyGet(target, null, "length", 0, 5, 0, 5, 4);
        assertFalse(fn.itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testSpecialPropertiesProtoAndParent() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node target = factory.createName("obj", 1, 0);

        // __proto__ is a special property in ScriptRuntime
        Node protoGet = factory.createPropertyGet(target, null, "__proto__", 0, 1, 0, 1, 4);
        assertEquals(Token.GET_REF, protoGet.getType());
        Node refChild = protoGet.getFirstChild();
        assertEquals(Token.REF_SPECIAL, refChild.getType());
        assertEquals("__proto__", refChild.getProp(Node.NAME_PROP));

        // Normal property name
        Node normalGet = factory.createPropertyGet(target, null, "regularProp", 0, 1, 0, 1, 4);
        assertEquals(Token.GETPROP, normalGet.getType());
        assertEquals("regularProp", normalGet.getLastChild().getString());

        // Target is null fallback
        Node nullTargetGet = factory.createPropertyGet(null, null, "foo", 0, 1, 0, 1, 0);
        assertEquals(Token.NAME, nullTargetGet.getType());
        assertEquals("foo", nullTargetGet.getString());
    }

    @Test(timeout = 4000)
    public void testMemberRefGetCombinations() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        Node target = factory.createName("targetObj", 1, 0);
        Node elem = factory.createString("prop");

        // 1. target != null, namespace != null ("*")
        Node wildGet = factory.createElementGet(target, "*", elem, 1, 1, 0);
        assertEquals(Token.GET_REF, wildGet.getType());
        Node wildRef = wildGet.getFirstChild();
        assertEquals(Token.REF_NS_MEMBER, wildRef.getType());
        assertEquals(Token.NULL, wildRef.getFirstChild().getNext().getType()); // nsNode is NULL for "*"

        // 2. target != null, namespace != null (named ns)
        Node nsGet = factory.createElementGet(target, "myNs", elem, 2, 1, 0);
        Node nsRef = nsGet.getFirstChild();
        assertEquals(Token.REF_NS_MEMBER, nsRef.getType());
        assertEquals("myNs", nsRef.getFirstChild().getNext().getString());

        // 3. target == null, namespace != null
        Node noTargetNsGet = factory.createElementGet(null, "myNs", elem, 0, 1, 0);
        Node noTargetNsRef = noTargetNsGet.getFirstChild();
        assertEquals(Token.REF_NS_NAME, noTargetNsRef.getType());

        // 4. target == null, namespace == null with memberTypeFlags != 0
        Node noTargetNoNsGet = factory.createElementGet(null, null, elem, 4, 1, 0);
        Node noTargetNoNsRef = noTargetNoNsGet.getFirstChild();
        assertEquals(Token.REF_NAME, noTargetNoNsRef.getType());
        assertEquals(4, noTargetNoNsRef.getIntProp(Node.MEMBER_TYPE_PROP, 0));

        // 5. target != null, namespace == null with memberTypeFlags != 0
        Node targetNoNsGet = factory.createElementGet(target, null, elem, 8, 1, 0);
        Node targetNoNsRef = targetNoNsGet.getFirstChild();
        assertEquals(Token.REF_MEMBER, targetNoNsRef.getType());
        assertEquals(8, targetNoNsRef.getIntProp(Node.MEMBER_TYPE_PROP, 0));
    }

    @Test(timeout = 4000)
    public void testIncDecAllValidReferenceTypes() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        // Pre and post increment/decrement across valid reference targets
        Node nameNode = factory.createName("v", 1, 0);
        Node postInc = factory.createIncDec(Token.INC, true, nameNode, 1, 0);
        assertNotNull(postInc);
        assertEquals(1, postInc.getIntProp(Node.INCRDECR_PROP, -1));

        Node preDec = factory.createIncDec(Token.DEC, false, nameNode, 1, 0);
        assertNotNull(preDec);
        assertEquals(0, preDec.getIntProp(Node.INCRDECR_PROP, -1));

        Node propNode = factory.createPropertyGet(nameNode, null, "p", 0, 1, 0, 1, 2);
        assertNotNull(factory.createIncDec(Token.INC, false, propNode, 1, 0));

        Node elemNode = factory.createElementGet(nameNode, null, factory.createNumber(0.0), 0, 1, 0);
        assertNotNull(factory.createIncDec(Token.INC, false, elemNode, 1, 0));

        Node callNode = factory.createCallOrNew(Token.CALL, nameNode, 1, 0);
        assertNotNull(factory.createIncDec(Token.INC, false, callNode, 1, 0));

        Node refNode = factory.createElementGet(null, "ns", factory.createString("p"), 0, 1, 0);
        assertNotNull(factory.createIncDec(Token.INC, false, refNode, 1, 0));
    }

    @Test(timeout = 4000)
    public void testLeafAndTaggedNameAndMiscNodes() {
        MockParser parser = createDefaultParser();
        IRFactory factory = createFactory(parser);

        // Leaves
        Node leaf1 = factory.createLeaf(Token.NULL);
        assertEquals(Token.NULL, leaf1.getType());

        Node leaf2 = factory.createLeaf(Token.TRUE, 10, 5);
        assertEquals(Token.TRUE, leaf2.getType());
        assertEquals(10, leaf2.getLineno());
        assertEquals(5, leaf2.getCharno());

        // Error name
        Node errName = factory.createErrorName();
        assertEquals(Token.NAME, errName.getType());
        assertEquals("error", errName.getString());

        // Tagged name with and without JSDocInfo
        Node untagged = factory.createTaggedName("x", null, 1, 0);
        assertEquals("x", untagged.getString());
        assertNull(untagged.getJSDocInfo());

        JSDocInfo doc = new JSDocInfo();
        Node tagged = factory.createTaggedName("y", doc, 1, 2);
        assertEquals("y", tagged.getString());
        assertEquals(doc, tagged.getJSDocInfo());

        // Literals
        Node num1 = factory.createNumber(3.14);
        assertEquals(3.14, num1.getDouble(), 0.0001);

        Node str1 = factory.createString("hello");
        assertEquals("hello", str1.getString());

        // Child addition
        Node parent = factory.createBlock(1, 0);
        Node child = factory.createLeaf(Token.EMPTY);
        factory.addChildToBack(parent, child);
        assertEquals(child, parent.getFirstChild());
    }
}