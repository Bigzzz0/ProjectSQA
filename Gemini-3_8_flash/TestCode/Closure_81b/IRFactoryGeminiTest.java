/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class Under Test: com.google.javascript.rhino.IRFactory
 * Benchmark Ground Truth: Defects4J - ParserTest::testUnnamedFunctionStatement
 *
 * Major Branches & Execution Paths Targeted:
 * 1. Function Construction & Initialization:
 *    - Unnamed function statements/expressions (empty string vs named function).
 *    - First child NAME node hack preservation for JSCompiler compatibility.
 *    - Nested function activation: FUNCTION_EXPRESSION_STATEMENT with null/empty vs non-empty name.
 *    - Source name association, JSDocInfo binding, FUNCTION_PROP indexing.
 * 2. Control Flow & Statements:
 *    - Switch/Case: switchNode validation (Kit.codeBug branch), null vs non-null caseExpression (DEFAULT vs CASE).
 *    - Catch: null catch condition (creates EMPTY node) vs explicit condition.
 *    - Return, Break, Continue: null vs non-null argument nodes/labels.
 *    - If / Ternary (HOOK): null vs non-null ifFalse branch.
 *    - TryCatchFinally: null vs non-null finallyBlock.
 * 3. Expressions & Operators:
 *    - Array Literals: skipCount == 0 vs skipCount > 0 with null elements (SKIP_INDEXES_PROP).
 *    - Object Literals: key/value sequence iteration and empty literals.
 *    - Regular Expressions: empty flags vs non-empty flags.
 *    - Binary Expressions: DOT to GETPROP (idNode converted to STRING), LB to GETELEM, other ops.
 *    - Assignment: Valid LHS (NAME, GETPROP, GETELEM) vs invalid LHS (reports error).
 *    - Inc/Dec: Reference validation (NAME, GETPROP, GETELEM, GET_REF, CALL) vs invalid child (reports error).
 *    - Call/New: SPECIALCALL_EVAL ("eval"), SPECIALCALL_WITH ("With"), GETPROP("eval"), NON_SPECIALCALL.
 * 4. Property & Member Resolution:
 *    - PropertyGet: namespace null & flags 0 -> null target (creates NAME), special property ("__proto__",
 *      "__parent__"), standard GETPROP; namespace != null / flags != 0 -> member reference resolution.
 *    - ElementGet: namespace null & flags 0 -> null target (Kit.codeBug) vs non-null target (GETELEM).
 *    - MemberRefGet: namespace wildcard ("*") vs explicit namespace vs null namespace; null vs non-null target.
 * 5. Activation & Scoping Invariants:
 *    - Activation triggers: "arguments", compilerEnv.activationNames map lookup, "length" in Context.VERSION_1_2.
 *    - Default namespace creation and dotQuery triggering activation.
 * -------------------------------------------------------------------------------------------------
 */

package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IRFactoryGeminiTest {

    /**
     * Subclass harness to intercept parser callbacks, error reporting, and activation state deterministically.
     */
    private static class TestParser extends Parser {
        boolean inFunction = false;
        String sourceName = null;
        final List<String> reportedErrors = new ArrayList<String>();

        TestParser(CompilerEnvirons env) {
            super(env);
        }

        @Override
        public boolean insideFunction() {
            return inFunction;
        }

        @Override
        public String getSourceName() {
            return sourceName;
        }

        @Override
        public void reportError(String messageId) {
            reportedErrors.add(messageId);
        }
    }

    private TestParser createTestParser(boolean insideFunction, String sourceName) {
        CompilerEnvirons env = new CompilerEnvirons();
        TestParser p = new TestParser(env);
        p.inFunction = insideFunction;
        p.sourceName = sourceName;
        if (insideFunction) {
            FunctionNode fn = new FunctionNode("testScope", 1, 0);
            p.currentScriptOrFn = fn;
        } else {
            p.currentScriptOrFn = new ScriptOrFnNode(Token.SCRIPT);
        }
        return p;
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateScriptAndInitScript() {
        TestParser parser = createTestParser(false, "source.js");
        IRFactory factory = new IRFactory(parser);

        ScriptOrFnNode scriptNode = factory.createScript();
        assertNotNull(scriptNode);
        assertEquals(Token.SCRIPT, scriptNode.getType());

        // Test initScript with non-empty body
        Node body = new Node(Token.BLOCK);
        Node stmt1 = new Node(Token.EXPR_VOID);
        Node stmt2 = new Node(Token.EXPR_VOID);
        body.addChildToBack(stmt1);
        body.addChildToBack(stmt2);

        factory.initScript(scriptNode, body);
        assertNull(body.getFirstChild()); // children moved
        assertSame(stmt1, scriptNode.getFirstChild());
        assertSame(stmt2, scriptNode.getLastChild());

        // Test initScript with empty body
        ScriptOrFnNode emptyScript = factory.createScript();
        Node emptyBody = new Node(Token.BLOCK);
        factory.initScript(emptyScript, emptyBody);
        assertNull(emptyScript.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testCreateLeafNodes() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node leaf1 = factory.createLeaf(Token.TRUE);
        assertEquals(Token.TRUE, leaf1.getType());

        Node leaf2 = factory.createLeaf(Token.FALSE, 12, 34);
        assertEquals(Token.FALSE, leaf2.getType());
        assertEquals(12, leaf2.getLineno());
        assertEquals(34, leaf2.getCharno());
    }

    @Test(timeout = 4000)
    public void testCreateSwitchAndAddSwitchCase() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node switchNode = factory.createSwitch(10, 2);
        assertEquals(Token.SWITCH, switchNode.getType());
        assertEquals(10, switchNode.getLineno());
        assertEquals(2, switchNode.getCharno());

        // Add regular case
        Node caseExpr = factory.createNumber(1.0);
        Node caseStmt = factory.createBlock(11, 4);
        factory.addSwitchCase(switchNode, caseExpr, caseStmt, 11, 2);

        Node caseNode = switchNode.getFirstChild();
        assertNotNull(caseNode);
        assertEquals(Token.CASE, caseNode.getType());
        assertSame(caseExpr, caseNode.getFirstChild());
        assertSame(caseStmt, caseNode.getLastChild());

        // Add default case (null caseExpression)
        Node defaultStmt = factory.createBlock(12, 4);
        factory.addSwitchCase(switchNode, null, defaultStmt, 12, 2);

        Node defaultNode = switchNode.getLastChild();
        assertNotNull(defaultNode);
        assertEquals(Token.DEFAULT, defaultNode.getType());
        assertSame(defaultStmt, defaultNode.getFirstChild());

        // closeSwitch is a no-op but must execute safely
        factory.closeSwitch(switchNode);
    }

    @Test(timeout = 4000)
    public void testCreateVariables() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node varNode = factory.createVariables(Token.VAR, 5, 10);
        assertEquals(Token.VAR, varNode.getType());
        assertEquals(5, varNode.getLineno());
        assertEquals(10, varNode.getCharno());
    }

    @Test(timeout = 4000)
    public void testCreateExprStatementInsideAndOutsideFunction() {
        // Outside function -> EXPR_RESULT
        TestParser parserOutside = createTestParser(false, null);
        IRFactory factoryOutside = new IRFactory(parserOutside);
        Node num = factoryOutside.createNumber(42.0);
        Node exprResult = factoryOutside.createExprStatement(num, 1, 0);
        assertEquals(Token.EXPR_RESULT, exprResult.getType());
        assertSame(num, exprResult.getFirstChild());

        // Inside function -> EXPR_VOID
        TestParser parserInside = createTestParser(true, null);
        IRFactory factoryInside = new IRFactory(parserInside);
        Node num2 = factoryInside.createNumber(42.0);
        Node exprVoid = factoryInside.createExprStatement(num2, 1, 0);
        assertEquals(Token.EXPR_VOID, exprVoid.getType());
        assertSame(num2, exprVoid.getFirstChild());

        // createExprStatementNoReturn always EXPR_VOID
        Node exprVoidExplicit = factoryOutside.createExprStatementNoReturn(num, 1, 0);
        assertEquals(Token.EXPR_VOID, exprVoidExplicit.getType());
    }

    @Test(timeout = 4000)
    public void testCreateDefaultNamespace() {
        TestParser parser = createTestParser(true, null);
        IRFactory factory = new IRFactory(parser);

        Node nsExpr = factory.createString("http://example.com");
        Node defaultNs = factory.createDefaultNamespace(nsExpr, 3, 5);

        assertEquals(Token.EXPR_VOID, defaultNs.getType());
        Node unary = defaultNs.getFirstChild();
        assertEquals(Token.DEFAULTNAMESPACE, unary.getType());
        assertSame(nsExpr, unary.getFirstChild());
        assertTrue(((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testCreateNameAndTaggedName() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node nameNode = factory.createName("alpha", 4, 8);
        assertEquals(Token.NAME, nameNode.getType());
        assertEquals("alpha", nameNode.getString());
        assertEquals(4, nameNode.getLineno());
        assertEquals(8, nameNode.getCharno());

        // Tagged name without JSDocInfo
        Node taggedNoInfo = factory.createTaggedName("beta", null, 5, 2);
        assertEquals(Token.NAME, taggedNoInfo.getType());
        assertEquals("beta", taggedNoInfo.getString());
        assertNull(taggedNoInfo.getJSDocInfo());

        // Tagged name with JSDocInfo
        JSDocInfo info = new JSDocInfo();
        Node taggedWithInfo = factory.createTaggedName("gamma", info, 6, 2);
        assertSame(info, taggedWithInfo.getJSDocInfo());
    }

    @Test(timeout = 4000)
    public void testCreateStringAndNumber() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node str1 = factory.createString("hello");
        assertEquals(Token.STRING, str1.getType());
        assertEquals("hello", str1.getString());

        Node str2 = factory.createString("world", 7, 3);
        assertEquals(Token.STRING, str2.getType());
        assertEquals("world", str2.getString());
        assertEquals(7, str2.getLineno());

        Node num1 = factory.createNumber(3.14159);
        assertEquals(Token.NUMBER, num1.getType());
        assertEquals(3.14159, num1.getDouble(), 1e-9);

        Node num2 = factory.createNumber(2.718, 8, 4);
        assertEquals(Token.NUMBER, num2.getType());
        assertEquals(2.718, num2.getDouble(), 1e-9);
        assertEquals(8, num2.getLineno());
    }

    @Test(timeout = 4000)
    public void testCreateCatch() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node stmts = factory.createBlock(2, 0);
        // Explicit condition
        Node cond = factory.createName("Error", 1, 10);
        Node catchWithCond = factory.createCatch("e", 1, 6, cond, stmts, 1, 0);
        assertEquals(Token.CATCH, catchWithCond.getType());
        Node varNameChild = catchWithCond.getFirstChild();
        assertEquals("e", varNameChild.getString());
        assertSame(cond, varNameChild.getNext());
        assertSame(stmts, varNameChild.getNext().getNext());

        // Null condition -> defaults to Token.EMPTY
        Node catchNullCond = factory.createCatch("err", 3, 6, null, stmts, 3, 0);
        Node emptyCond = catchNullCond.getFirstChild().getNext();
        assertEquals(Token.EMPTY, emptyCond.getType());
        assertEquals(3, emptyCond.getLineno());
        assertEquals(6, emptyCond.getCharno());
    }

    @Test(timeout = 4000)
    public void testCreateThrowReturn() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node expr = factory.createNumber(100.0);
        Node throwNode = factory.createThrow(expr, 5, 2);
        assertEquals(Token.THROW, throwNode.getType());
        assertSame(expr, throwNode.getFirstChild());

        // Return with expr
        Node returnWithExpr = factory.createReturn(expr, 6, 2);
        assertEquals(Token.RETURN, returnWithExpr.getType());
        assertSame(expr, returnWithExpr.getFirstChild());

        // Return without expr (null)
        Node returnNull = factory.createReturn(null, 7, 2);
        assertEquals(Token.RETURN, returnNull.getType());
        assertNull(returnNull.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testCreateLabelBreakContinueDebuggerBlock() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node labelNode = factory.createLabel("loop", 1, 0);
        assertEquals(Token.LABEL, labelNode.getType());
        assertEquals("loop", labelNode.getFirstChild().getString());

        // Break labeled vs unlabeled
        Node breakLabeled = factory.createBreak("loop", 2, 4);
        assertEquals(Token.BREAK, breakLabeled.getType());
        assertEquals("loop", breakLabeled.getFirstChild().getString());

        Node breakUnlabeled = factory.createBreak(null, 3, 4);
        assertEquals(Token.BREAK, breakUnlabeled.getType());
        assertNull(breakUnlabeled.getFirstChild());

        // Continue labeled vs unlabeled
        Node contLabeled = factory.createContinue("loop", 4, 4);
        assertEquals(Token.CONTINUE, contLabeled.getType());
        assertEquals("loop", contLabeled.getFirstChild().getString());

        Node contUnlabeled = factory.createContinue(null, 5, 4);
        assertEquals(Token.CONTINUE, contUnlabeled.getType());
        assertNull(contUnlabeled.getFirstChild());

        // Debugger and Block
        Node dbg = factory.createDebugger(6, 0);
        assertEquals(Token.DEBUGGER, dbg.getType());

        Node blk = factory.createBlock(7, 0);
        assertEquals(Token.BLOCK, blk.getType());
    }

    @Test(timeout = 4000)
    public void testCreateLoopsAndBranches() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node cond = factory.createName("c", 1, 0);
        Node body = factory.createBlock(1, 4);

        Node whileNode = factory.createWhile(cond, body, 1, 0);
        assertEquals(Token.WHILE, whileNode.getType());
        assertSame(cond, whileNode.getFirstChild());
        assertSame(body, whileNode.getLastChild());

        Node doWhileNode = factory.createDoWhile(body, cond, 2, 0);
        assertEquals(Token.DO, doWhileNode.getType());
        assertSame(body, doWhileNode.getFirstChild());
        assertSame(cond, doWhileNode.getLastChild());

        Node init = factory.createVariables(Token.VAR, 3, 4);
        Node incr = factory.createName("i", 3, 10);
        Node forNode = factory.createFor(init, cond, incr, body, 3, 0);
        assertEquals(Token.FOR, forNode.getType());

        Node forInNode = factory.createForIn(init, cond, body, 4, 0);
        assertEquals(Token.FOR, forInNode.getType());

        // With and DotQuery
        Node obj = factory.createName("o", 5, 0);
        Node withNode = factory.createWith(obj, body, 5, 0);
        assertEquals(Token.WITH, withNode.getType());

        Node dotQuery = factory.createDotQuery(obj, body, 6, 0);
        assertEquals(Token.DOTQUERY, dotQuery.getType());

        // If with & without else
        Node ifWithElse = factory.createIf(cond, body, factory.createBlock(7, 4), 7, 0);
        assertEquals(Token.IF, ifWithElse.getType());
        assertEquals(3, ifWithElse.getChildCount());

        Node ifNoElse = factory.createIf(cond, body, null, 8, 0);
        assertEquals(Token.IF, ifNoElse.getType());
        assertEquals(2, ifNoElse.getChildCount());

        // Conditional Expression (HOOK)
        Node hook = factory.createCondExpr(cond, init, incr, 9, 0);
        assertEquals(Token.HOOK, hook.getType());
    }

    @Test(timeout = 4000)
    public void testCreateTryCatchFinally() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node tryBlk = factory.createBlock(1, 0);
        Node catchBlk = factory.createBlock(2, 0);
        Node finBlk = factory.createBlock(3, 0);

        // With finally
        Node tryCatchFin = factory.createTryCatchFinally(tryBlk, catchBlk, finBlk, 1, 0);
        assertEquals(Token.TRY, tryCatchFin.getType());
        assertEquals(3, tryCatchFin.getChildCount());

        // Without finally (null)
        Node tryCatchOnly = factory.createTryCatchFinally(tryBlk, catchBlk, null, 1, 0);
        assertEquals(Token.TRY, tryCatchOnly.getType());
        assertEquals(2, tryCatchOnly.getChildCount());
    }

    @Test(timeout = 4000)
    public void testCreateArrayAndObjectLiterals() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Array literal with skip indexes (sparse array)
        ObjArray elems = new ObjArray();
        elems.add(null); // index 0 skipped
        Node n1 = factory.createNumber(1.0);
        elems.add(n1);
        elems.add(null); // index 2 skipped
        Node n2 = factory.createNumber(2.0);
        elems.add(n2);

        Node arraySparse = factory.createArrayLiteral(elems, 2, 1, 0);
        assertEquals(Token.ARRAYLIT, arraySparse.getType());
        assertEquals(2, arraySparse.getChildCount()); // only non-nulls added
        int[] skips = (int[]) arraySparse.getProp(Node.SKIP_INDEXES_PROP);
        assertNotNull(skips);
        assertEquals(2, skips.length);
        assertEquals(0, skips[0]);
        assertEquals(2, skips[1]);

        // Array literal with zero skip count
        ObjArray denseElems = new ObjArray();
        denseElems.add(n1);
        denseElems.add(n2);
        Node arrayDense = factory.createArrayLiteral(denseElems, 0, 2, 0);
        assertEquals(Token.ARRAYLIT, arrayDense.getType());
        assertEquals(2, arrayDense.getChildCount());
        assertNull(arrayDense.getProp(Node.SKIP_INDEXES_PROP));

        // Object literal
        ObjArray objProps = new ObjArray();
        Node k1 = factory.createString("a");
        Node v1 = factory.createNumber(10.0);
        objProps.add(k1);
        objProps.add(v1);
        Node objLit = factory.createObjectLiteral(objProps, 3, 0);
        assertEquals(Token.OBJECTLIT, objLit.getType());
        assertEquals(2, objLit.getChildCount());
        assertSame(k1, objLit.getFirstChild());
        assertSame(v1, objLit.getLastChild());
    }

    @Test(timeout = 4000)
    public void testCreateRegExp() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Empty flags
        Node reEmptyFlags = factory.createRegExp("abc", "", 1, 0);
        assertEquals(Token.REGEXP, reEmptyFlags.getType());
        assertEquals(1, reEmptyFlags.getChildCount());
        assertEquals("abc", reEmptyFlags.getFirstChild().getString());

        // With flags
        Node reWithFlags = factory.createRegExp("abc", "gi", 2, 0);
        assertEquals(Token.REGEXP, reWithFlags.getType());
        assertEquals(2, reWithFlags.getChildCount());
        assertEquals("abc", reWithFlags.getFirstChild().getString());
        assertEquals("gi", reWithFlags.getLastChild().getString());
    }

    @Test(timeout = 4000)
    public void testCreateUnaryAndBinary() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node child = factory.createNumber(1.0);
        Node neg = factory.createUnary(Token.NEG, child, 1, 0);
        assertEquals(Token.NEG, neg.getType());
        assertSame(child, neg.getFirstChild());

        // Binary: Token.DOT transformed to Token.GETPROP and idNode transformed to Token.STRING
        Node left = factory.createName("obj", 2, 0);
        Node right = factory.createName("prop", 2, 4);
        Node dot = factory.createBinary(Token.DOT, left, right, 2, 3);
        assertEquals(Token.GETPROP, dot.getType());
        assertEquals(Token.STRING, dot.getLastChild().getType());

        // Binary: Token.LB transformed to Token.GETELEM
        Node elemIdx = factory.createNumber(0.0);
        Node lb = factory.createBinary(Token.LB, left, elemIdx, 3, 3);
        assertEquals(Token.GETELEM, lb.getType());

        // Binary: Standard ADD
        Node add = factory.createBinary(Token.ADD, left, right, 4, 3);
        assertEquals(Token.ADD, add.getType());
    }

    @Test(timeout = 4000)
    public void testCreateCallOrNewSpecialCalls() {
        TestParser parser = createTestParser(true, null);
        IRFactory factory = new IRFactory(parser);

        // 1. Direct "eval" NAME call
        Node evalName = factory.createName("eval", 1, 0);
        Node callEval = factory.createCallOrNew(Token.CALL, evalName, 1, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callEval.getIntProp(Node.SPECIALCALL_PROP, 0));
        assertTrue(((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation);

        // Reset activation flag
        ((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation = false;

        // 2. Direct "With" NAME call
        Node withName = factory.createName("With", 2, 0);
        Node callWith = factory.createCallOrNew(Token.CALL, withName, 2, 0);
        assertEquals(Node.SPECIALCALL_WITH, callWith.getIntProp(Node.SPECIALCALL_PROP, 0));
        assertTrue(((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation);

        ((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation = false;

        // 3. Property call "obj.eval"
        Node target = factory.createName("window", 3, 0);
        Node prop = factory.createString("eval", 3, 7);
        Node getprop = new Node(Token.GETPROP, target, prop, 3, 6);
        Node callPropEval = factory.createCallOrNew(Token.CALL, getprop, 3, 0);
        assertEquals(Node.SPECIALCALL_EVAL, callPropEval.getIntProp(Node.SPECIALCALL_PROP, 0));
        assertTrue(((FunctionNode) parser.currentScriptOrFn).itsNeedsActivation);

        // 4. Non-special call
        Node normalName = factory.createName("myFunc", 4, 0);
        Node normalCall = factory.createCallOrNew(Token.CALL, normalName, 4, 0);
        assertEquals(0, normalCall.getIntProp(Node.SPECIALCALL_PROP, 0));
    }

    @Test(timeout = 4000)
    public void testCreateIncDecValidReferences() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Token.NAME post-increment
        Node nameRef = factory.createName("counter", 1, 0);
        Node postInc = factory.createIncDec(Token.INC, true, nameRef, 1, 0);
        assertNotNull(postInc);
        assertEquals(Token.INC, postInc.getType());
        assertEquals(1, postInc.getIntProp(Node.INCRDECR_PROP, -1));

        // Token.GETPROP pre-decrement
        Node target = factory.createName("obj", 2, 0);
        Node getpropRef = factory.createPropertyGet(target, null, "x", 0, 2, 3, 2, 4);
        Node preDec = factory.createIncDec(Token.DEC, false, getpropRef, 2, 0);
        assertNotNull(preDec);
        assertEquals(Token.DEC, preDec.getType());
        assertEquals(0, preDec.getIntProp(Node.INCRDECR_PROP, -1));

        // Token.GETELEM, GET_REF, CALL
        Node elemRef = new Node(Token.GETELEM, target, factory.createNumber(0.0));
        assertNotNull(factory.createIncDec(Token.INC, false, elemRef, 3, 0));

        Node refNode = new Node(Token.GET_REF, target);
        assertNotNull(factory.createIncDec(Token.INC, false, refNode, 4, 0));

        Node callNode = new Node(Token.CALL, target);
        assertNotNull(factory.createIncDec(Token.INC, false, callNode, 5, 0));
    }

    @Test(timeout = 4000)
    public void testCreatePropertyGetSpecialPropertiesAndNamespaces() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Special properties: "__proto__" and "__parent__"
        Node target = factory.createName("obj", 1, 0);
        Node specialRef = factory.createPropertyGet(target, null, "__proto__", 0, 1, 3, 1, 4);
        assertEquals(Token.GET_REF, specialRef.getType());
        Node refChild = specialRef.getFirstChild();
        assertEquals(Token.REF_SPECIAL, refChild.getType());
        assertEquals("__proto__", refChild.getProp(Node.NAME_PROP));

        // Null target with null namespace returns NAME node
        Node standaloneName = factory.createPropertyGet(null, null, "standalone", 0, 2, 0, 2, 0);
        assertEquals(Token.NAME, standaloneName.getType());
        assertEquals("standalone", standaloneName.getString());

        // Namespace with wildcard "*"
        Node wildcardMember = factory.createPropertyGet(target, "*", "prop", 0, 3, 3, 3, 4);
        assertEquals(Token.GET_REF, wildcardMember.getType());
        assertEquals(Token.REF_NS_MEMBER, wildcardMember.getFirstChild().getType());
        assertEquals(Token.NULL, wildcardMember.getFirstChild().getFirstChild().getNext().getType());

        // Explicit namespace with target
        Node nsMember = factory.createPropertyGet(target, "myNS", "prop", 0, 4, 3, 4, 4);
        assertEquals(Token.GET_REF, nsMember.getType());
        assertEquals(Token.REF_NS_MEMBER, nsMember.getFirstChild().getType());
        assertEquals(Token.NAME, nsMember.getFirstChild().getFirstChild().getNext().getType());

        // Namespace without target
        Node nsName = factory.createPropertyGet(null, "myNS", "prop", 0, 5, 3, 5, 4);
        assertEquals(Token.GET_REF, nsName.getType());
        assertEquals(Token.REF_NS_NAME, nsName.getFirstChild().getType());

        // Null namespace with non-zero memberTypeFlags and null target
        Node flagRef = factory.createPropertyGet(null, null, "prop", Node.DESCENDANTS_FLAG, 6, 3, 6, 4);
        assertEquals(Token.GET_REF, flagRef.getType());
        assertEquals(Token.REF_NAME, flagRef.getFirstChild().getType());
        int flags = flagRef.getFirstChild().getIntProp(Node.MEMBER_TYPE_PROP, 0);
        assertTrue((flags & Node.PROPERTY_FLAG) != 0);
    }

    @Test(timeout = 4000)
    public void testCreateElementGetVariants() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node target = factory.createName("arr", 1, 0);
        Node elem = factory.createNumber(0.0);

        // Standard GETELEM (namespace null, flags 0, target != null)
        Node getElem = factory.createElementGet(target, null, elem, 0, 1, 3);
        assertEquals(Token.GETELEM, getElem.getType());
        assertSame(target, getElem.getFirstChild());
        assertSame(elem, getElem.getLastChild());

        // With flags -> createMemberRefGet
        Node refMember = factory.createElementGet(target, null, elem, Node.DESCENDANTS_FLAG, 2, 3);
        assertEquals(Token.GET_REF, refMember.getType());
        assertEquals(Token.REF_MEMBER, refMember.getFirstChild().getType());

        // With namespace wildcard and null target
        Node wildcardElem = factory.createElementGet(null, "*", elem, 0, 3, 3);
        assertEquals(Token.GET_REF, wildcardElem.getType());
        assertEquals(Token.REF_NS_NAME, wildcardElem.getFirstChild().getType());
    }

    @Test(timeout = 4000)
    public void testCreateAssignmentValidLhs() throws Exception {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node lhsName = factory.createName("x", 1, 0);
        Node rhs = factory.createNumber(5.0);
        Node assignName = factory.createAssignment(Token.ASSIGN, lhsName, rhs, 1, 2);
        assertEquals(Token.ASSIGN, assignName.getType());
        assertSame(lhsName, assignName.getFirstChild());
        assertSame(rhs, assignName.getLastChild());

        Node lhsProp = new Node(Token.GETPROP, lhsName, factory.createString("p"));
        Node assignProp = factory.createAssignment(Token.ASSIGN, lhsProp, rhs, 2, 2);
        assertEquals(Token.ASSIGN, assignProp.getType());

        Node lhsElem = new Node(Token.GETELEM, lhsName, factory.createNumber(0.0));
        Node assignElem = factory.createAssignment(Token.ASSIGN, lhsElem, rhs, 3, 2);
        assertEquals(Token.ASSIGN, assignElem.getType());

        assertTrue(parser.reportedErrors.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddChildToBack() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node parent = factory.createBlock(1, 0);
        Node child = factory.createNumber(123.0);
        factory.addChildToBack(parent, child);
        assertSame(child, parent.getFirstChild());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberExtremes() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        double[] extremeDoubles = new double[]{
                0.0, -0.0, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE
        };
        for (double d : extremeDoubles) {
            Node numNode = factory.createNumber(d);
            assertEquals(Token.NUMBER, numNode.getType());
            assertEquals(Double.doubleToLongBits(d), Double.doubleToLongBits(numNode.getDouble()));
        }
    }

    @Test(timeout = 4000)
    public void testSparseArrayLiteralBoundary() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Entirely empty elements with non-zero skipCount
        ObjArray allNulls = new ObjArray();
        allNulls.add(null);
        allNulls.add(null);
        allNulls.add(null);

        Node arrayNode = factory.createArrayLiteral(allNulls, 3, 1, 0);
        assertEquals(0, arrayNode.getChildCount());
        int[] skips = (int[]) arrayNode.getProp(Node.SKIP_INDEXES_PROP);
        assertNotNull(skips);
        assertEquals(3, skips.length);
        assertEquals(0, skips[0]);
        assertEquals(1, skips[1]);
        assertEquals(2, skips[2]);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Closure Compiler Bug Pattern)
    // =========================================================================

    /**
     * Targets known defect: ParserTest::testUnnamedFunctionStatement.
     * Verifies that unnamed functions (empty string name) properly generate a Token.NAME
     * child node as required by JSCompiler AST invariants and do not fail initialization
     * when configured as FUNCTION_EXPRESSION_STATEMENT or with createErrorName fallback.
     */
    @Test(timeout = 4000)
    public void testUnnamedFunctionStatementDefect() {
        TestParser parser = createTestParser(false, "test_input.js");
        IRFactory factory = new IRFactory(parser);

        // 1. Unnamed function creation
        FunctionNode fnNode = factory.createFunction("", 1, 0);
        assertNotNull("FunctionNode should be instantiated", fnNode);
        assertEquals(Token.FUNCTION, fnNode.getType());
        assertEquals("", fnNode.getFunctionName());

        // JSCompiler compatibility invariant: first child MUST be a Token.NAME node
        Node firstChild = fnNode.getFirstChild();
        assertNotNull("FunctionNode must preserve first child NAME node", firstChild);
        assertEquals(Token.NAME, firstChild.getType());
        assertEquals("", firstChild.getString());

        // 2. Unnamed function initialization as FUNCTION_EXPRESSION_STATEMENT
        Node args = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        JSDocInfo info = new JSDocInfo();
        Node initialized = factory.initFunction(fnNode, 42, args, info, body,
                FunctionNode.FUNCTION_EXPRESSION_STATEMENT);

        assertSame(fnNode, initialized);
        assertEquals(FunctionNode.FUNCTION_EXPRESSION_STATEMENT, fnNode.getFunctionType());
        assertEquals(42, fnNode.getIntProp(Node.FUNCTION_PROP, -1));
        assertSame(info, fnNode.getJSDocInfo());
        assertEquals("test_input.js", fnNode.getProp(Node.SOURCENAME_PROP));

        // 3. Fallback error node invariant
        Node errorName = factory.createErrorName();
        assertNotNull(errorName);
        assertEquals(Token.NAME, errorName.getType());
        assertEquals("error", errorName.getString());
    }

    @Test(timeout = 4000)
    public void testInitFunctionNestedFunctionsHandling() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        FunctionNode outerFn = factory.createFunction("outer", 1, 0);
        outerFn.addParamOrVar("shadowedVar");
        outerFn.addParamOrVar("keptVar");

        // Nested function expression statement with valid name overrides var
        FunctionNode nestedExprStmt = factory.createFunction("shadowedVar", 2, 4);
        nestedExprStmt.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        outerFn.addFunction(nestedExprStmt);

        // Nested function with empty name should not throw or remove random vars
        FunctionNode nestedAnonymous = factory.createFunction("", 3, 4);
        nestedAnonymous.itsFunctionType = FunctionNode.FUNCTION_EXPRESSION_STATEMENT;
        outerFn.addFunction(nestedAnonymous);

        // Nested function with regular statement type
        FunctionNode nestedRegular = factory.createFunction("regular", 4, 4);
        nestedRegular.itsFunctionType = FunctionNode.FUNCTION_STATEMENT;
        outerFn.addFunction(nestedRegular);

        factory.initFunction(outerFn, 1, new Node(Token.LP), null, new Node(Token.BLOCK),
                FunctionNode.FUNCTION_STATEMENT);

        // Verification
        assertTrue("Outer function must activate due to nested functions", outerFn.itsNeedsActivation);
        assertFalse("Shadowed var should be removed by nested expression statement",
                outerFn.hasParamOrVar("shadowedVar"));
        assertTrue("Kept var should remain untouched",
                outerFn.hasParamOrVar("keptVar"));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddSwitchCaseThrowsOnInvalidNodeType() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node invalidSwitch = new Node(Token.BLOCK);
        try {
            factory.addSwitchCase(invalidSwitch, null, new Node(Token.BLOCK), 1, 0);
            fail("Expected RuntimeException when node type is not Token.SWITCH");
        } catch (RuntimeException e) {
            // Success: Kit.codeBug() threw exception
            assertNotNull(e);
        }
    }

    @Test(timeout = 4000)
    public void testCreateElementGetThrowsOnNullTargetWithoutNamespace() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        try {
            factory.createElementGet(null, null, factory.createNumber(0.0), 0, 1, 0);
            fail("Expected RuntimeException when target is null in simple element get");
        } catch (RuntimeException e) {
            // Success: Kit.codeBug() triggered
            assertNotNull(e);
        }
    }

    @Test(timeout = 4000)
    public void testCreateIncDecReportsErrorOnInvalidChild() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node literal = factory.createNumber(123.0);

        // DEC invalid
        Node decResult = factory.createIncDec(Token.DEC, true, literal, 1, 0);
        assertNull(decResult);
        assertEquals(1, parser.reportedErrors.size());
        assertEquals("msg.bad.decr", parser.reportedErrors.get(0));

        // INC invalid
        Node incResult = factory.createIncDec(Token.INC, false, literal, 2, 0);
        assertNull(incResult);
        assertEquals(2, parser.reportedErrors.size());
        assertEquals("msg.bad.incr", parser.reportedErrors.get(1));
    }

    @Test(timeout = 4000)
    public void testCreateAssignmentReportsErrorOnInvalidLhs() throws Exception {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        Node invalidLhs = factory.createNumber(10.0);
        Node rhs = factory.createNumber(20.0);

        Node assign = factory.createAssignment(Token.ASSIGN, invalidLhs, rhs, 1, 0);
        assertNotNull(assign);
        assertEquals(Token.ASSIGN, assign.getType());
        assertEquals(1, parser.reportedErrors.size());
        assertEquals("msg.bad.assign.left", parser.reportedErrors.get(0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCreateFunctionThrowsOnNullName() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);
        // Null name is illegal for Node.newString(Token.NAME, null)
        factory.createFunction(null, 1, 0);
    }

    // =========================================================================
    // PARTITION E: Activation Names & Scope Management
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckActivationNameArguments() {
        TestParser parser = createTestParser(true, null);
        IRFactory factory = new IRFactory(parser);

        FunctionNode fn = (FunctionNode) parser.currentScriptOrFn;
        assertFalse(fn.itsNeedsActivation);

        factory.createName("arguments", 1, 0);
        assertTrue("Referencing 'arguments' inside function must trigger activation",
                fn.itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testCheckActivationNameCustomList() {
        TestParser parser = createTestParser(true, null);
        parser.compilerEnv.activationNames = new HashMap<String, Object>();
        parser.compilerEnv.activationNames.put("customScopedSymbol", Boolean.TRUE);

        IRFactory factory = new IRFactory(parser);
        FunctionNode fn = (FunctionNode) parser.currentScriptOrFn;

        factory.createName("normalName", 1, 0);
        assertFalse(fn.itsNeedsActivation);

        factory.createName("customScopedSymbol", 2, 0);
        assertTrue("Referencing custom activation name must trigger activation",
                fn.itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testCheckActivationNameLengthVersion12() {
        TestParser parser = createTestParser(true, null);
        parser.compilerEnv.setLanguageVersion(Context.VERSION_1_2);
        IRFactory factory = new IRFactory(parser);
        FunctionNode fn = (FunctionNode) parser.currentScriptOrFn;

        // "length" property get in JS 1.2 triggers activation
        Node target = factory.createName("arr", 1, 0);
        factory.createPropertyGet(target, null, "length", 0, 1, 3, 1, 4);
        assertTrue("Use of 'length' in Version 1.2 requires activation", fn.itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testCheckActivationNameLengthNonVersion12() {
        TestParser parser = createTestParser(true, null);
        parser.compilerEnv.setLanguageVersion(Context.VERSION_1_5);
        IRFactory factory = new IRFactory(parser);
        FunctionNode fn = (FunctionNode) parser.currentScriptOrFn;

        Node target = factory.createName("arr", 1, 0);
        factory.createPropertyGet(target, null, "length", 0, 1, 3, 1, 4);
        assertFalse("Use of 'length' in Version 1.5 must not trigger activation", fn.itsNeedsActivation);
    }

    @Test(timeout = 4000)
    public void testActivationIgnoredOutsideFunction() {
        TestParser parser = createTestParser(false, null);
        IRFactory factory = new IRFactory(parser);

        // Outside function: referencing "arguments" must not fail or activate
        Node name = factory.createName("arguments", 1, 0);
        assertEquals("arguments", name.getString());
        assertFalse(parser.insideFunction());
    }
}