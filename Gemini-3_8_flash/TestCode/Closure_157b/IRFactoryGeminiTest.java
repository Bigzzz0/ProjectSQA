package com.google.javascript.rhino;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.google.javascript.rhino.IRFactory
 *
 * Targeted Decision Branches & Conditions:
 * 1. initScript:
 *    - body has children (removeChildren != null) vs body has no children (removeChildren == null)
 * 2. addSwitchCase:
 *    - switchNode.getType() != Token.SWITCH (codeBug exception)
 *    - caseExpression != null (Token.CASE) vs null (Token.DEFAULT)
 * 3. createExprStatement:
 *    - parser.insideFunction() == true (Token.EXPR_VOID) vs false (Token.EXPR_RESULT)
 * 4. createTaggedName:
 *    - info != null (setJSDocInfo called) vs info == null
 * 5. createCatch:
 *    - catchCond == null (Token.EMPTY placeholder) vs catchCond != null
 * 6. createReturn:
 *    - expr == null vs expr != null
 * 7. createBreak / createContinue:
 *    - label == null (no child) vs label != null (Token.NAME child attached)
 * 8. initFunction:
 *    - parser.getSourceName() != null vs null
 *    - info != null vs null
 *    - functionCount != 0:
 *      - nested fn type == FUNCTION_EXPRESSION_STATEMENT with name != null & length != 0 (removeParamOrVar)
 *      - nested fn type != FUNCTION_EXPRESSION_STATEMENT or name == null / length == 0
 * 9. createTryCatchFinally:
 *    - finallyBlock == null (2 children) vs finallyBlock != null (3 children)
 * 10. createArrayLiteral:
 *     - skipCount == 0 vs skipCount != 0 (SKIP_INDEXES_PROP populated with null element indices)
 * 11. createObjectLiteral:
 *     - Key preservation for String vs Number keys (Defects4J object literal numeric key bug)
 * 12. createRegExp:
 *     - flags.length() == 0 (1 child) vs flags.length() > 0 (2 children)
 * 13. createIf:
 *     - ifFalse == null (2 children) vs ifFalse != null (3 children)
 * 14. createCallOrNew:
 *     - child is Token.NAME ("eval" -> SPECIALCALL_EVAL, "With" -> SPECIALCALL_WITH, other -> NON_SPECIALCALL)
 *     - child is Token.GETPROP (last child "eval" -> SPECIALCALL_EVAL, other -> NON_SPECIALCALL)
 *     - child is other node -> NON_SPECIALCALL
 * 15. createIncDec:
 *     - makeReference(child) returns null (reports error msg.bad.decr or msg.bad.incr, returns null)
 *     - makeReference valid for NAME, GETPROP, GETELEM, GET_REF, CALL (post ? 1 : 0)
 * 16. createPropertyGet:
 *     - namespace == null && memberTypeFlags == 0:
 *       - target == null (createName)
 *       - ScriptRuntime.isSpecialProperty ("__proto__" / "__parent__" -> REF_SPECIAL, GET_REF)
 *       - normal property (GETPROP)
 *     - namespace != null || memberTypeFlags != 0:
 *       - namespace == "*" (Token.NULL nsNode) vs custom namespace (Token.NAME nsNode)
 *       - target == null vs target != null
 * 17. createElementGet:
 *     - namespace == null && memberTypeFlags == 0:
 *       - target == null (codeBug exception)
 *       - target != null (GETELEM)
 *     - namespace != null || memberTypeFlags != 0 (delegates to createMemberRefGet)
 * 18. createBinary:
 *     - Token.DOT (converted to GETPROP, right node changed to STRING)
 *     - Token.LB (converted to GETELEM)
 *     - other tokens (preserved)
 * 19. createAssignment:
 *     - left is valid target (NAME, GETPROP, GETELEM)
 *     - left is invalid target (reports error msg.bad.assign.left, returns ASSIGN node)
 * 20. checkActivationName:
 *     - insideFunction == true:
 *       - "arguments" -> activation required
 *       - activationNames contains name -> activation required
 *       - "length" with Token.GETPROP and Context.VERSION_1_2 -> activation required
 *       - "length" with other token or version -> no activation
 */
public class IRFactoryGeminiTest {

    private CompilerEnvirons compilerEnv;
    private TestErrorReporter errorReporter;
    private Parser parser;
    private IRFactory factory;

    private static class TestErrorReporter implements ErrorReporter {
        final List<String> errors = new ArrayList<String>();
        final List<String> warnings = new ArrayList<String>();

        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            warnings.add(message);
        }

        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            errors.add(message);
        }

        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
        }
    }

    @Before
    public void setUp() {
        compilerEnv = new CompilerEnvirons();
        errorReporter = new TestErrorReporter();
        compilerEnv.setErrorReporter(errorReporter);
        parser = new Parser(compilerEnv, errorReporter);
        factory = new IRFactory(parser);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateScriptAndInitScriptWithChildren() {
        ScriptOrFnNode scriptNode = factory.createScript();
        assertNotNull(scriptNode);
        assertEquals(Token.SCRIPT, scriptNode.getType());

        Node body = new Node(Token.BLOCK);
        Node child1 = new Node(Token.TRUE);
        Node child2 = new Node(Token.FALSE);
        body.addChildToBack(child1);
        body.addChildToBack(child2);

        factory.initScript(scriptNode, body);
        assertNull(body.getFirstChild());
        assertEquals(child1, scriptNode.getFirstChild());
        assertEquals(child2, scriptNode.getLastChild());
    }

    @Test(timeout = 4000)
    public void testInitScriptWithEmptyBody() {
        ScriptOrFnNode scriptNode = factory.createScript();
        Node emptyBody = new Node(Token.BLOCK);
        factory.initScript(scriptNode, emptyBody);
        assertNull(scriptNode.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testCreateLeafVariants() {
        Node leaf1 = factory.createLeaf(Token.EMPTY);
        assertEquals(Token.EMPTY, leaf1.getType());

        Node leaf2 = factory.createLeaf(Token.VOID, 12, 34);
        assertEquals(Token.VOID, leaf2.getType());
        assertEquals(12, leaf2.getLineno());
        assertEquals(34, leaf2.getCharno());
    }

    @Test(timeout = 4000)
    public void testCreateVariablesAndErrorName() {
        Node vars = factory.createVariables(Token.VAR, 5, 10);
        assertEquals(Token.VAR, vars.getType());
        assertEquals(5, vars.getLineno());
        assertEquals(10, vars.getCharno());

        Node errorName = factory.createErrorName();
        assertEquals(Token.NAME, errorName.getType());
        assertEquals("error", errorName.getString());
    }

    @Test(timeout = 4000)
    public void testCreateStringAndNumber() {
        Node str1 = factory.createString("testLiteral");
        assertEquals(Token.STRING, str1.getType());
        assertEquals("testLiteral", str1.getString());

        Node str2 = factory.createString("testWithPos", 3, 7);
        assertEquals(Token.STRING, str2.getType());
        assertEquals("testWithPos", str2.getString());
        assertEquals(3, str2.getLineno());
        assertEquals(7, str2.getCharno());

        Node num1 = factory.createNumber(42.5);
        assertEquals(Token.NUMBER, num1.getType());
        assertEquals(42.5, num1.getDouble(), 0.0001);

        Node num2 = factory.createNumber(-99.0, 8, 12);
        assertEquals(Token.NUMBER, num2.getType());
        assertEquals(-99.0, num2.getDouble(), 0.0001);
        assertEquals(8, num2.getLineno());
        assertEquals(12, num2.getCharno());
    }

    @Test(timeout = 4000)
    public void testControlFlowStatements() {
        Node cond = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK);

        Node whileNode = factory.createWhile(cond, body, 1, 2);
        assertEquals(Token.WHILE, whileNode.getType());
        assertEquals(cond, whileNode.getFirstChild());
        assertEquals(body, whileNode.getLastChild());

        Node doWhileNode = factory.createDoWhile(body, cond, 3, 4);
        assertEquals(Token.DO, doWhileNode.getType());
        assertEquals(body, doWhileNode.getFirstChild());
        assertEquals(cond, doWhileNode.getLastChild());

        Node init = new Node(Token.EMPTY);
        Node incr = new Node(Token.EMPTY);
        Node forNode = factory.createFor(init, cond, incr, body, 5, 6);
        assertEquals(Token.FOR, forNode.getType());
        assertEquals(4, forNode.getChildCount());

        Node lhs = factory.createName("k", 7, 0);
        Node obj = factory.createName("o", 7, 5);
        Node forInNode = factory.createForIn(lhs, obj, body, 7, 0);
        assertEquals(Token.FOR, forInNode.getType());
        assertEquals(3, forInNode.getChildCount());

        Node withNode = factory.createWith(obj, body, 8, 0);
        assertEquals(Token.WITH, withNode.getType());

        Node dbgNode = factory.createDebugger(9, 1);
        assertEquals(Token.DEBUGGER, dbgNode.getType());

        Node blkNode = factory.createBlock(10, 2);
        assertEquals(Token.BLOCK, blkNode.getType());
    }

    @Test(timeout = 4000)
    public void testCreateIfAndCondExpr() {
        Node cond = new Node(Token.TRUE);
        Node ifTrue = new Node(Token.EMPTY);

        Node ifWithoutElse = factory.createIf(cond, ifTrue, null, 1, 0);
        assertEquals(Token.IF, ifWithoutElse.getType());
        assertEquals(2, ifWithoutElse.getChildCount());

        Node ifFalse = new Node(Token.EMPTY);
        Node ifWithElse = factory.createIf(cond, ifTrue, ifFalse, 2, 0);
        assertEquals(Token.IF, ifWithElse.getType());
        assertEquals(3, ifWithElse.getChildCount());

        Node hook = factory.createCondExpr(cond, ifTrue, ifFalse, 3, 0);
        assertEquals(Token.HOOK, hook.getType());
        assertEquals(3, hook.getChildCount());
    }

    @Test(timeout = 4000)
    public void testCreateReturnBreakContinueThrow() {
        Node retEmpty = factory.createReturn(null, 1, 0);
        assertEquals(Token.RETURN, retEmpty.getType());
        assertFalse(retEmpty.hasChildren());

        Node expr = factory.createNumber(10);
        Node retExpr = factory.createReturn(expr, 2, 0);
        assertEquals(Token.RETURN, retExpr.getType());
        assertEquals(expr, retExpr.getFirstChild());

        Node brkNoLabel = factory.createBreak(null, 3, 0);
        assertEquals(Token.BREAK, brkNoLabel.getType());
        assertFalse(brkNoLabel.hasChildren());

        Node brkLabel = factory.createBreak("myLoop", 4, 0);
        assertEquals(Token.BREAK, brkLabel.getType());
        assertTrue(brkLabel.hasChildren());
        assertEquals("myLoop", brkLabel.getFirstChild().getString());

        Node contNoLabel = factory.createContinue(null, 5, 0);
        assertEquals(Token.CONTINUE, contNoLabel.getType());
        assertFalse(contNoLabel.hasChildren());

        Node contLabel = factory.createContinue("outer", 6, 0);
        assertEquals(Token.CONTINUE, contLabel.getType());
        assertEquals("outer", contLabel.getFirstChild().getString());

        Node throwNode = factory.createThrow(expr, 7, 0);
        assertEquals(Token.THROW, throwNode.getType());
        assertEquals(expr, throwNode.getFirstChild());

        Node labelNode = factory.createLabel("loopStart", 8, 0);
        assertEquals(Token.LABEL, labelNode.getType());
        assertEquals("loopStart", labelNode.getFirstChild().getString());
    }

    @Test(timeout = 4000)
    public void testAddChildToBack() {
        Node parent = new Node(Token.BLOCK);
        Node child = new Node(Token.EMPTY);
        factory.addChildToBack(parent, child);
        assertEquals(child, parent.getFirstChild());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Literals (RegExp, Array, Switch)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateRegExp() {
        Node reEmptyFlags = factory.createRegExp("abc", "", 1, 0);
        assertEquals(Token.REGEXP, reEmptyFlags.getType());
        assertEquals(1, reEmptyFlags.getChildCount());
        assertEquals("abc", reEmptyFlags.getFirstChild().getString());

        Node reWithFlags = factory.createRegExp("abc", "gi", 2, 0);
        assertEquals(Token.REGEXP, reWithFlags.getType());
        assertEquals(2, reWithFlags.getChildCount());
        assertEquals("abc", reWithFlags.getFirstChild().getString());
        assertEquals("gi", reWithFlags.getLastChild().getString());
    }

    @Test(timeout = 4000)
    public void testCreateArrayLiteralWithoutSkips() {
        ObjArray elems = new ObjArray();
        elems.add(factory.createNumber(1));
        elems.add(factory.createNumber(2));

        Node arr = factory.createArrayLiteral(elems, 0, 1, 0);
        assertEquals(Token.ARRAYLIT, arr.getType());
        assertEquals(2, arr.getChildCount());
        assertNull(arr.getProp(Node.SKIP_INDEXES_PROP));
    }

    @Test(timeout = 4000)
    public void testCreateArrayLiteralWithSkips() {
        ObjArray elems = new ObjArray();
        elems.add(factory.createNumber(1));
        elems.add(null); // elision
        elems.add(factory.createNumber(3));
        elems.add(null); // elision

        Node arr = factory.createArrayLiteral(elems, 2, 1, 0);
        assertEquals(Token.ARRAYLIT, arr.getType());
        assertEquals(2, arr.getChildCount()); // only non-null added as children

        int[] skips = (int[]) arr.getProp(Node.SKIP_INDEXES_PROP);
        assertNotNull(skips);
        assertEquals(2, skips.length);
        assertEquals(1, skips[0]);
        assertEquals(3, skips[1]);
    }

    @Test(timeout = 4000)
    public void testSwitchCaseAndDefault() {
        Node switchNode = factory.createSwitch(1, 0);
        assertEquals(Token.SWITCH, switchNode.getType());

        Node caseExpr = factory.createNumber(1);
        Node caseStmts = new Node(Token.BLOCK);
        factory.addSwitchCase(switchNode, caseExpr, caseStmts, 2, 0);

        Node defaultStmts = new Node(Token.BLOCK);
        factory.addSwitchCase(switchNode, null, defaultStmts, 3, 0);

        factory.closeSwitch(switchNode);

        assertEquals(2, switchNode.getChildCount());
        Node firstChild = switchNode.getFirstChild();
        assertEquals(Token.CASE, firstChild.getType());
        assertEquals(caseExpr, firstChild.getFirstChild());
        assertEquals(caseStmts, firstChild.getLastChild());

        Node secondChild = firstChild.getNext();
        assertEquals(Token.DEFAULT, secondChild.getType());
        assertEquals(defaultStmts, secondChild.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testCreateCatchVariants() {
        Node catchCond = factory.createName("Error", 1, 0);
        Node stmts = new Node(Token.BLOCK);

        Node catchWithCond = factory.createCatch("e", 1, 0, catchCond, stmts, 1, 0);
        assertEquals(Token.CATCH, catchWithCond.getType());
        assertEquals(catchCond, catchWithCond.getFirstChild().getNext());

        Node catchNoCond = factory.createCatch("e", 1, 0, null, stmts, 1, 0);
        assertEquals(Token.CATCH, catchNoCond.getType());
        Node generatedCond = catchNoCond.getFirstChild().getNext();
        assertEquals(Token.EMPTY, generatedCond.getType());
    }

    @Test(timeout = 4000)
    public void testCreateTryCatchFinally() {
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlocks = new Node(Token.BLOCK);
        Node finallyBlock = new Node(Token.BLOCK);

        Node tryCatch = factory.createTryCatchFinally(tryBlock, catchBlocks, null, 1, 0);
        assertEquals(Token.TRY, tryCatch.getType());
        assertEquals(2, tryCatch.getChildCount());

        Node tryCatchFinally = factory.createTryCatchFinally(tryBlock, catchBlocks, finallyBlock, 2, 0);
        assertEquals(Token.TRY, tryCatchFinally.getType());
        assertEquals(3, tryCatchFinally.getChildCount());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Object Literals & Activation)
    // =========================================================================

    /**
     * Target Defect: Object literals with numeric and string keys must preserve
     * their types and positions without corruption or improper stringifying.
     */
    @Test(timeout = 4000)
    public void testDefectTargetedObjectLiteralWithNumericAndStringKeys() {
        ObjArray obj = new ObjArray();
        Node numKey = factory.createNumber(1.0, 1, 0);
        Node val1 = factory.createString("val1", 1, 4);
        Node strKey = factory.createString("prop", 2, 0);
        Node val2 = factory.createNumber(42.0, 2, 6);

        obj.add(numKey);
        obj.add(val1);
        obj.add(strKey);
        obj.add(val2);

        Node objLit = factory.createObjectLiteral(obj, 1, 0);
        assertEquals(Token.OBJECTLIT, objLit.getType());
        assertEquals(4, objLit.getChildCount());

        Node k1 = objLit.getFirstChild();
        assertEquals(Token.NUMBER, k1.getType());
        assertEquals(1.0, k1.getDouble(), 0.0001);

        Node v1 = k1.getNext();
        assertEquals(Token.STRING, v1.getType());
        assertEquals("val1", v1.getString());

        Node k2 = v1.getNext();
        assertEquals(Token.STRING, k2.getType());
        assertEquals("prop", k2.getString());

        Node v2 = k2.getNext();
        assertEquals(Token.NUMBER, v2.getType());
        assertEquals(42.0, v2.getDouble(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateTaggedName() {
        Node untagged = factory.createTaggedName("var1", null, 1, 0);
        assertEquals(Token.NAME, untagged.getType());
        assertEquals("var1", untagged.getString());
        assertNull(untagged.getJSDocInfo());

        JSDocInfo info = new JSDocInfo();
        Node tagged = factory.createTaggedName("var2", info, 2, 5);
        assertEquals(Token.NAME, tagged.getType());
        assertEquals("var2", tagged.getString());
        assertSame(info, tagged.getJSDocInfo());
    }

    @Test(timeout = 4000)
    public void testInsideFunctionAndActivationEffects() {
        Node expr = factory.createNumber(10);
        Node resultStmt = factory.createExprStatement(expr, 1, 0);
        assertEquals(Token.EXPR_RESULT, resultStmt.getType());

        Node voidStmt = factory.createExprStatementNoReturn(expr, 1, 0);
        assertEquals(Token.EXPR_VOID, voidStmt.getType());

        FunctionNode currentFn = new FunctionNode("testFn", 1, 0);
        parser.currentScriptOrFn = currentFn;
        assertTrue(parser.insideFunction());

        Node insideFnStmt = factory.createExprStatement(expr, 2, 0);
        assertEquals(Token.EXPR_VOID, insideFnStmt.getType());

        assertFalse(currentFn.itsNeedsActivation);
        factory.createName("arguments", 3, 0);
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        compilerEnv.activationNames = new HashMap<String, Boolean>();
        compilerEnv.activationNames.put("customActivatedVar", Boolean.TRUE);
        factory.createName("customActivatedVar", 4, 0);
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        compilerEnv.setLanguageVersion(Context.VERSION_1_2);
        Node target = factory.createName("arr", 5, 0);
        factory.createPropertyGet(target, null, "length", 0, 5, 3, 5, 4);
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        compilerEnv.setLanguageVersion(Context.VERSION_1_3);
        factory.createPropertyGet(target, null, "length", 0, 6, 3, 6, 4);
        assertFalse(currentFn.itsNeedsActivation);

        factory.createDotQuery(target, expr, 7, 0);
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        factory.createDefaultNamespace(expr, 8, 0);
        assertTrue(currentFn.itsNeedsActivation);

        parser.currentScriptOrFn = null;
    }

    @Test(timeout = 4000)
    public void testCreateCallOrNewSpecialForms() {
        FunctionNode currentFn = new FunctionNode("enclosing", 1, 0);
        parser.currentScriptOrFn = currentFn;

        Node evalName = factory.createName("eval", 1, 0);
        Node callEval = factory.createCallOrNew(Token.CALL, evalName, 1, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callEval.getIntProp(Node.SPECIALCALL_PROP));
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        Node withName = factory.createName("With", 2, 0);
        Node callWith = factory.createCallOrNew(Token.CALL, withName, 2, 0);
        assertEquals(Node.SPECIALCALL_WITH, callWith.getIntProp(Node.SPECIALCALL_PROP));
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        Node normalName = factory.createName("regularFn", 3, 0);
        Node callNormal = factory.createCallOrNew(Token.CALL, normalName, 3, 0);
        assertEquals(0, callNormal.getIntProp(Node.SPECIALCALL_PROP));
        assertFalse(currentFn.itsNeedsActivation);

        Node obj = factory.createName("window", 4, 0);
        Node getpropEval = new Node(Token.GETPROP, obj, factory.createString("eval"));
        Node callObjEval = factory.createCallOrNew(Token.CALL, getpropEval, 4, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callObjEval.getIntProp(Node.SPECIALCALL_PROP));
        assertTrue(currentFn.itsNeedsActivation);

        currentFn.itsNeedsActivation = false;
        Node getpropOther = new Node(Token.GETPROP, obj, factory.createString("foo"));
        Node callObjOther = factory.createCallOrNew(Token.CALL, getpropOther, 5, 0);
        assertEquals(0, callObjOther.getIntProp(Node.SPECIALCALL_PROP));
        assertFalse(currentFn.itsNeedsActivation);

        parser.currentScriptOrFn = null;
    }

    @Test(timeout = 4000)
    public void testFunctionCreationAndInitFunction() {
        parser.sourceURI = "file.js";

        FunctionNode fnNode = factory.createFunction("myFunc", 10, 5);
        assertEquals("myFunc", fnNode.getFunctionName());
        assertEquals(1, fnNode.getChildCount());
        assertEquals("myFunc", fnNode.getFirstChild().getString());

        FunctionNode nestedFn = new FunctionNode("nestedExpStmt", 12, 0);
        nestedFn.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        fnNode.addFunction(nestedFn);

        FunctionNode nestedNoName = new FunctionNode("", 14, 0);
        nestedNoName.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        fnNode.addFunction(nestedNoName);

        FunctionNode regularNested = new FunctionNode("regularNested", 16, 0);
        fnNode.addFunction(regularNested);

        Node args = new Node(Token.LP);
        Node stmts = new Node(Token.BLOCK);
        JSDocInfo info = new JSDocInfo();

        factory.initFunction(fnNode, 42, args, info, stmts, FunctionNode.FUNCTION_EXPRESSION);

        assertEquals(FunctionNode.FUNCTION_EXPRESSION, fnNode.getFunctionType());
        assertSame(info, fnNode.getJSDocInfo());
        assertEquals(42, fnNode.getIntProp(Node.FUNCTION_PROP));
        assertEquals("file.js", fnNode.getProp(Node.SOURCENAME_PROP));
        assertTrue(fnNode.itsNeedsActivation);
    }

    // =========================================================================
    // Partition D: Inc/Dec, Binary, Assignment & Member Reference Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateIncDecValid() {
        Node name = factory.createName("x", 1, 0);
        Node postInc = factory.createIncDec(Token.INC, true, name, 1, 0);
        assertNotNull(postInc);
        assertEquals(Token.INC, postInc.getType());
        assertEquals(1, postInc.getIntProp(Node.INCRDECR_PROP));

        Node preDec = factory.createIncDec(Token.DEC, false, name, 1, 0);
        assertNotNull(preDec);
        assertEquals(Token.DEC, preDec.getType());
        assertEquals(0, preDec.getIntProp(Node.INCRDECR_PROP));

        Node getProp = new Node(Token.GETPROP, name, factory.createString("y"));
        assertNotNull(factory.createIncDec(Token.INC, true, getProp, 2, 0));

        Node getElem = new Node(Token.GETELEM, name, factory.createNumber(0));
        assertNotNull(factory.createIncDec(Token.INC, true, getElem, 3, 0));

        Node getRef = new Node(Token.GET_REF, name);
        assertNotNull(factory.createIncDec(Token.INC, true, getRef, 4, 0));

        Node call = new Node(Token.CALL, name);
        assertNotNull(factory.createIncDec(Token.INC, true, call, 5, 0));
    }

    @Test(timeout = 4000)
    public void testCreateIncDecInvalid() {
        Node invalidOperand = factory.createNumber(123.0);
        Node resDec = factory.createIncDec(Token.DEC, false, invalidOperand, 1, 0);
        assertNull(resDec);
        assertTrue(errorReporter.errors.contains("msg.bad.decr"));

        Node resInc = factory.createIncDec(Token.INC, false, invalidOperand, 2, 0);
        assertNull(resInc);
        assertTrue(errorReporter.errors.contains("msg.bad.incr"));
    }

    @Test(timeout = 4000)
    public void testCreateBinary() {
        Node left = factory.createName("a", 1, 0);
        Node right = factory.createName("b", 1, 2);

        Node dotBinary = factory.createBinary(Token.DOT, left, right, 1, 1);
        assertEquals(Token.GETPROP, dotBinary.getType());
        assertEquals(Token.STRING, right.getType());

        Node lbBinary = factory.createBinary(Token.LB, left, right, 1, 1);
        assertEquals(Token.GETELEM, lbBinary.getType());

        Node addBinary = factory.createBinary(Token.ADD, left, right, 1, 1);
        assertEquals(Token.ADD, addBinary.getType());
    }

    @Test(timeout = 4000)
    public void testCreateAssignmentValidAndInvalid() throws Exception {
        Node val = factory.createNumber(10);

        Node leftName = factory.createName("x", 1, 0);
        Node assignName = factory.createAssignment(Token.ASSIGN, leftName, val, 1, 0);
        assertEquals(Token.ASSIGN, assignName.getType());
        assertTrue(errorReporter.errors.isEmpty());

        Node leftProp = new Node(Token.GETPROP, leftName, factory.createString("p"));
        Node assignProp = factory.createAssignment(Token.ASSIGN, leftProp, val, 2, 0);
        assertEquals(Token.ASSIGN, assignProp.getType());

        Node leftElem = new Node(Token.GETELEM, leftName, factory.createNumber(0));
        Node assignElem = factory.createAssignment(Token.ASSIGN, leftElem, val, 3, 0);
        assertEquals(Token.ASSIGN, assignElem.getType());

        Node invalidLeft = factory.createNumber(99);
        Node assignInvalid = factory.createAssignment(Token.ASSIGN, invalidLeft, val, 4, 0);
        assertEquals(Token.ASSIGN, assignInvalid.getType());
        assertTrue(errorReporter.errors.contains("msg.bad.assign.left"));
    }

    @Test(timeout = 4000)
    public void testCreatePropertyGetSpecialPropertiesAndNamespaces() {
        Node target = factory.createName("obj", 1, 0);

        Node specialRef = factory.createPropertyGet(target, null, "__proto__", 0, 1, 3, 1, 4);
        assertEquals(Token.GET_REF, specialRef.getType());
        Node refSpecial = specialRef.getFirstChild();
        assertEquals(Token.REF_SPECIAL, refSpecial.getType());
        assertEquals("__proto__", refSpecial.getProp(Node.NAME_PROP));

        Node normalProp = factory.createPropertyGet(target, null, "prop", 0, 2, 3, 2, 4);
        assertEquals(Token.GETPROP, normalProp.getType());

        Node standaloneName = factory.createPropertyGet(null, null, "myVar", 0, 3, 0, 3, 0);
        assertEquals(Token.NAME, standaloneName.getType());
        assertEquals("myVar", standaloneName.getString());

        Node wildcardNs = factory.createPropertyGet(null, "*", "elem", 0, 4, 0, 4, 0);
        assertEquals(Token.GET_REF, wildcardNs.getType());
        Node refNs = wildcardNs.getFirstChild();
        assertEquals(Token.REF_NS_NAME, refNs.getType());
        assertEquals(Token.NULL, refNs.getFirstChild().getType());

        Node customNs = factory.createPropertyGet(null, "myNS", "elem", 0, 5, 0, 5, 0);
        assertEquals(Token.GET_REF, customNs.getType());
        assertEquals(Token.REF_NS_NAME, customNs.getFirstChild().getType());
        assertEquals(Token.NAME, customNs.getFirstChild().getFirstChild().getType());

        Node memberRefWithTarget = factory.createPropertyGet(target, null, "elem", 2, 6, 0, 6, 0);
        assertEquals(Token.GET_REF, memberRefWithTarget.getType());
        assertEquals(Token.REF_MEMBER, memberRefWithTarget.getFirstChild().getType());
        assertTrue((memberRefWithTarget.getFirstChild().getIntProp(Node.MEMBER_TYPE_PROP) & Node.PROPERTY_FLAG) != 0);

        Node memberRefWithTargetAndNs = factory.createPropertyGet(target, "ns2", "elem", 0, 7, 0, 7, 0);
        assertEquals(Token.GET_REF, memberRefWithTargetAndNs.getType());
        assertEquals(Token.REF_NS_MEMBER, memberRefWithTargetAndNs.getFirstChild().getType());
    }

    @Test(timeout = 4000)
    public void testCreateElementGetVariants() {
        Node target = factory.createName("arr", 1, 0);
        Node elem = factory.createNumber(0);

        Node elemGet = factory.createElementGet(target, null, elem, 0, 1, 0);
        assertEquals(Token.GETELEM, elemGet.getType());

        Node memberElemGet = factory.createElementGet(target, "ns", elem, 1, 2, 0);
        assertEquals(Token.GET_REF, memberElemGet.getType());
        assertEquals(Token.REF_NS_MEMBER, memberElemGet.getFirstChild().getType());

        Node targetNullWithNs = factory.createElementGet(null, null, elem, 2, 3, 0);
        assertEquals(Token.GET_REF, targetNullWithNs.getType());
        assertEquals(Token.REF_NAME, targetNullWithNs.getFirstChild().getType());
    }

    // =========================================================================
    // Partition E: Defensive Guard & Exception Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddSwitchCaseThrowsOnInvalidSwitchNodeType() {
        Node invalidSwitchNode = new Node(Token.BLOCK);
        Node caseExpr = factory.createNumber(1);
        Node stmts = new Node(Token.BLOCK);

        try {
            factory.addSwitchCase(invalidSwitchNode, caseExpr, stmts, 1, 0);
            fail("Expected RuntimeException from Kit.codeBug()");
        } catch (RuntimeException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateElementGetThrowsOnNullTargetWithoutNamespace() {
        Node elem = factory.createNumber(0);
        try {
            factory.createElementGet(null, null, elem, 0, 1, 0);
            fail("Expected RuntimeException from Kit.codeBug() for null target in createElementGet");
        } catch (RuntimeException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}