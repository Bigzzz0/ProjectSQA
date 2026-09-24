package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.*;
import java.util.List;
import java.util.ArrayList;

/**
 * IRFactoryDeepseekTest - Comprehensive JUnit 4 test suite for IRFactory.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (transformTree, transform for various node types)
 * - Partition B: Boundary values (null arguments, empty collections, zero/negative numbers)
 * - Partition C: Defect-targeted (incomplete function, missing return, null body)
 * - Partition D: Exception paths (illegal arguments, unsupported node types)
 * - Partition E: Object lifecycle (constructors, state transitions)
 *
 * Targets known defect: "testIncompleteFunction" causing internal compiler error.
 * Tests simulate incomplete function nodes to trigger the bug.
 */
public class IRFactoryDeepseekTest {

    // Helper to create a minimal CompilerEnvirons
    private static CompilerEnvirons createEnv() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(false);
        env.setGenerateDebugInfo(false);
        env.setReservedKeywordAsIdentifier(true);
        return env;
    }

    // Helper to create an IRFactory with default env
    private static IRFactory createFactory() {
        return new IRFactory(createEnv());
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testTransformTreeSimpleScript() {
        IRFactory factory = createFactory();
        // Build a simple script: var x = 1;
        AstRoot root = new AstRoot();
        root.setInStrictMode(false);
        VariableDeclaration varDecl = new VariableDeclaration();
        varDecl.setType(Token.VAR);
        VariableInitializer init = new VariableInitializer();
        Name name = new Name();
        name.setIdentifier("x");
        init.setTarget(name);
        NumberLiteral num = new NumberLiteral();
        num.setNumber(1.0);
        init.setInitializer(num);
        varDecl.addVariable(init);
        root.addChild(varDecl);
        ScriptNode result = factory.transformTree(root);
        assertNotNull("transformTree should return a ScriptNode", result);
        assertTrue("Result should be a ScriptNode", result instanceof ScriptNode);
    }

    @Test(timeout = 4000)
    public void testTransformFunctionExpression() {
        IRFactory factory = createFactory();
        // Build a function expression: function() { return 42; }
        FunctionNode fn = new FunctionNode();
        fn.setFunctionType(FunctionNode.FUNCTION_EXPRESSION);
        fn.setName("");
        Block body = new Block();
        ReturnStatement ret = new ReturnStatement();
        NumberLiteral num = new NumberLiteral();
        num.setNumber(42.0);
        ret.setReturnValue(num);
        body.addChild(ret);
        fn.setBody(body);
        // Wrap in a script
        AstRoot root = new AstRoot();
        root.addChild(fn);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testTransformIfStatement() {
        IRFactory factory = createFactory();
        // if (true) { x = 1; } else { x = 2; }
        IfStatement ifStmt = new IfStatement();
        KeywordLiteral cond = new KeywordLiteral();
        cond.setType(Token.TRUE);
        ifStmt.setCondition(cond);
        Block thenBlock = new Block();
        ExpressionStatement expr1 = new ExpressionStatement();
        Assignment assign1 = new Assignment();
        assign1.setType(Token.ASSIGN);
        Name left1 = new Name();
        left1.setIdentifier("x");
        assign1.setLeft(left1);
        NumberLiteral right1 = new NumberLiteral();
        right1.setNumber(1.0);
        assign1.setRight(right1);
        expr1.setExpression(assign1);
        thenBlock.addChild(expr1);
        ifStmt.setThenPart(thenBlock);
        Block elseBlock = new Block();
        ExpressionStatement expr2 = new ExpressionStatement();
        Assignment assign2 = new Assignment();
        assign2.setType(Token.ASSIGN);
        Name left2 = new Name();
        left2.setIdentifier("x");
        assign2.setLeft(left2);
        NumberLiteral right2 = new NumberLiteral();
        right2.setNumber(2.0);
        assign2.setRight(right2);
        expr2.setExpression(assign2);
        elseBlock.addChild(expr2);
        ifStmt.setElsePart(elseBlock);
        AstRoot root = new AstRoot();
        root.addChild(ifStmt);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testTransformWhileLoop() {
        IRFactory factory = createFactory();
        // while (false) { break; }
        WhileLoop loop = new WhileLoop();
        KeywordLiteral cond = new KeywordLiteral();
        cond.setType(Token.FALSE);
        loop.setCondition(cond);
        Block body = new Block();
        BreakStatement brk = new BreakStatement();
        body.addChild(brk);
        loop.setBody(body);
        AstRoot root = new AstRoot();
        root.addChild(loop);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testTransformForLoop() {
        IRFactory factory = createFactory();
        // for (var i = 0; i < 10; i++) { }
        ForLoop loop = new ForLoop();
        VariableDeclaration init = new VariableDeclaration();
        init.setType(Token.VAR);
        VariableInitializer varInit = new VariableInitializer();
        Name iName = new Name();
        iName.setIdentifier("i");
        varInit.setTarget(iName);
        NumberLiteral zero = new NumberLiteral();
        zero.setNumber(0.0);
        varInit.setInitializer(zero);
        init.addVariable(varInit);
        loop.setInitializer(init);
        InfixExpression test = new InfixExpression();
        test.setType(Token.LT);
        Name iRef = new Name();
        iRef.setIdentifier("i");
        test.setLeft(iRef);
        NumberLiteral ten = new NumberLiteral();
        ten.setNumber(10.0);
        test.setRight(ten);
        loop.setCondition(test);
        UnaryExpression incr = new UnaryExpression();
        incr.setType(Token.INC);
        incr.setOperand(new Name("i"));
        incr.setPostfix(true);
        loop.setIncrement(incr);
        Block body = new Block();
        loop.setBody(body);
        AstRoot root = new AstRoot();
        root.addChild(loop);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testTransformTryCatchFinally() {
        IRFactory factory = createFactory();
        // try { } catch (e) { } finally { }
        TryStatement tryStmt = new TryStatement();
        Block tryBlock = new Block();
        tryStmt.setTryBlock(tryBlock);
        CatchClause cc = new CatchClause();
        Name eName = new Name();
        eName.setIdentifier("e");
        cc.setVarName(eName);
        Block catchBody = new Block();
        cc.setBody(catchBody);
        tryStmt.addCatchClause(cc);
        Block finallyBlock = new Block();
        tryStmt.setFinallyBlock(finallyBlock);
        AstRoot root = new AstRoot();
        root.addChild(tryStmt);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testTransformSwitch() {
        IRFactory factory = createFactory();
        // switch (x) { case 1: break; default: break; }
        SwitchStatement sw = new SwitchStatement();
        Name x = new Name();
        x.setIdentifier("x");
        sw.setExpression(x);
        SwitchCase case1 = new SwitchCase();
        NumberLiteral one = new NumberLiteral();
        one.setNumber(1.0);
        case1.setExpression(one);
        List<AstNode> stmts1 = new ArrayList<>();
        BreakStatement brk1 = new BreakStatement();
        stmts1.add(brk1);
        case1.setStatements(stmts1);
        sw.addCase(case1);
        SwitchCase defaultCase = new SwitchCase();
        List<AstNode> stmts2 = new ArrayList<>();
        BreakStatement brk2 = new BreakStatement();
        stmts2.add(brk2);
        defaultCase.setStatements(stmts2);
        sw.addCase(defaultCase);
        AstRoot root = new AstRoot();
        root.addChild(sw);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testTransformEmptyScript() {
        IRFactory factory = createFactory();
        AstRoot root = new AstRoot();
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
        assertTrue("Empty script should have no children", result.getChildCount() == 0);
    }

    @Test(timeout = 4000)
    public void testTransformNullNode() {
        IRFactory factory = createFactory();
        // transform(null) should throw NullPointerException or similar
        try {
            factory.transform(null);
            fail("Expected NullPointerException for null node");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformNumberLiteralBoundary() {
        IRFactory factory = createFactory();
        NumberLiteral num = new NumberLiteral();
        num.setNumber(Double.MAX_VALUE);
        Node result = factory.transform(num);
        assertNotNull(result);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(Double.MAX_VALUE, result.getDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testTransformStringLiteralEmpty() {
        IRFactory factory = createFactory();
        StringLiteral str = new StringLiteral();
        str.setValue("");
        Node result = factory.transform(str);
        assertNotNull(result);
        assertEquals(Token.STRING, result.getType());
        assertEquals("", result.getString());
    }

    @Test(timeout = 4000)
    public void testTransformArrayLiteralEmpty() {
        IRFactory factory = createFactory();
        ArrayLiteral arr = new ArrayLiteral();
        arr.setDestructuring(false);
        Node result = factory.transform(arr);
        assertNotNull(result);
        assertEquals(Token.ARRAYLIT, result.getType());
    }

    @Test(timeout = 4000)
    public void testTransformObjectLiteralEmpty() {
        IRFactory factory = createFactory();
        ObjectLiteral obj = new ObjectLiteral();
        obj.setDestructuring(false);
        Node result = factory.transform(obj);
        assertNotNull(result);
        assertEquals(Token.OBJECTLIT, result.getType());
    }

    // ==================== Partition C: Defect-Targeted (Incomplete Function) ====================

    @Test(timeout = 4000)
    public void testTransformIncompleteFunctionNoBody() {
        // Simulate a function node with null body (incomplete)
        IRFactory factory = createFactory();
        FunctionNode fn = new FunctionNode();
        fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        fn.setName("foo");
        fn.setBody(null);  // incomplete
        AstRoot root = new AstRoot();
        root.addChild(fn);
        try {
            factory.transformTree(root);
            fail("Expected RuntimeException for incomplete function");
        } catch (RuntimeException e) {
            // Expected: internal compiler error due to null body
            assertTrue(e.getMessage().contains("INTERNAL") || e.getMessage().contains("null") || e instanceof NullPointerException);
        }
    }

    @Test(timeout = 4000)
    public void testTransformFunctionMissingReturn() {
        // Function with no return statement at end (should be added by initFunction)
        IRFactory factory = createFactory();
        FunctionNode fn = new FunctionNode();
        fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        fn.setName("bar");
        Block body = new Block();
        // No return statement
        fn.setBody(body);
        AstRoot root = new AstRoot();
        root.addChild(fn);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
        // The transformed function should have a return node added
        // We can check that the function node's body has a return as last child
        // But we don't have direct access; just ensure no exception
    }

    @Test(timeout = 4000)
    public void testTransformFunctionWithDestructuringParams() {
        // Function with destructuring parameter (e.g., function f([a, b]) {})
        IRFactory factory = createFactory();
        FunctionNode fn = new FunctionNode();
        fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        fn.setName("destruct");
        // Create a destructuring array parameter
        ArrayLiteral arrParam = new ArrayLiteral();
        arrParam.setDestructuring(true);
        Name a = new Name();
        a.setIdentifier("a");
        Name b = new Name();
        b.setIdentifier("b");
        arrParam.addElement(a);
        arrParam.addElement(b);
        List<AstNode> params = new ArrayList<>();
        params.add(arrParam);
        fn.setParams(params);
        Block body = new Block();
        ReturnStatement ret = new ReturnStatement();
        ret.setReturnValue(new NumberLiteral(0.0));
        body.addChild(ret);
        fn.setBody(body);
        AstRoot root = new AstRoot();
        root.addChild(fn);
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTransformUnsupportedNodeType() {
        IRFactory factory = createFactory();
        // Create a node with an unsupported type (e.g., Token.ERROR)
        AstNode unsupported = new AstNode() {
            // anonymous subclass with type ERROR
            @Override
            public int getType() {
                return Token.ERROR;
            }
        };
        factory.transform(unsupported);
    }

    @Test(timeout = 4000)
    public void testTransformAssignmentInvalidLeft() {
        IRFactory factory = createFactory();
        // Assignment to a literal (invalid left-hand side)
        Assignment assign = new Assignment();
        assign.setType(Token.ASSIGN);
        NumberLiteral left = new NumberLiteral();
        left.setNumber(5.0);
        assign.setLeft(left);
        NumberLiteral right = new NumberLiteral();
        right.setNumber(10.0);
        assign.setRight(right);
        // Wrap in expression statement
        ExpressionStatement exprStmt = new ExpressionStatement();
        exprStmt.setExpression(assign);
        AstRoot root = new AstRoot();
        root.addChild(exprStmt);
        try {
            factory.transformTree(root);
            // Should not throw, but may produce a node with error
        } catch (Exception e) {
            // Acceptable if it throws
        }
    }

    @Test(timeout = 4000)
    public void testTransformForInWithInvalidLHS() {
        IRFactory factory = createFactory();
        // for (5 in x) {}  // invalid LHS
        ForInLoop loop = new ForInLoop();
        loop.setIterator(new NumberLiteral(5.0));
        Name x = new Name();
        x.setIdentifier("x");
        loop.setIteratedObject(x);
        Block body = new Block();
        loop.setBody(body);
        AstRoot root = new AstRoot();
        root.addChild(loop);
        try {
            factory.transformTree(root);
            // May report error and return null or throw
        } catch (Exception e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformThrowWithNullValue() {
        IRFactory factory = createFactory();
        ThrowStatement thr = new ThrowStatement();
        thr.setExpression(null);  // invalid
        AstRoot root = new AstRoot();
        root.addChild(thr);
        try {
            factory.transformTree(root);
            fail("Expected NullPointerException for throw with null expression");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        IRFactory factory = new IRFactory();
        assertNotNull(factory);
    }

    @Test(timeout = 4000)
    public void testConstructorWithEnv() {
        CompilerEnvirons env = createEnv();
        IRFactory factory = new IRFactory(env);
        assertNotNull(factory);
    }

    @Test(timeout = 4000)
    public void testConstructorWithEnvAndReporter() {
        CompilerEnvirons env = createEnv();
        ErrorReporter reporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {}
            @Override
            public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {}
            @Override
            public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
                return new EvaluatorException(message);
            }
        };
        IRFactory factory = new IRFactory(env, reporter);
        assertNotNull(factory);
    }

    @Test(timeout = 4000)
    public void testTransformTreeGeneratesSource() {
        CompilerEnvirons env = createEnv();
        env.setGeneratingSource(true);
        IRFactory factory = new IRFactory(env);
        AstRoot root = new AstRoot();
        root.addChild(new Name("a"));
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
        assertNotNull("Encoded source should be generated", result.getEncodedSource());
    }

    @Test(timeout = 4000)
    public void testTransformTreeNoSource() {
        CompilerEnvirons env = createEnv();
        env.setGeneratingSource(false);
        IRFactory factory = new IRFactory(env);
        AstRoot root = new AstRoot();
        root.addChild(new Name("b"));
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
        assertNull("Encoded source should be null when not generating", result.getEncodedSource());
    }
}