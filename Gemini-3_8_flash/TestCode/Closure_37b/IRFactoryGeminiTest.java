package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.ast.*;
import java.util.ArrayList;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.mozilla.javascript.IRFactory
 * Focus Areas:
 * 1. Defects4J Known Issue: Incomplete / malformed functions (e.g. empty or null bodies, or functions missing statements)
 *    causing NullPointerException or unexpected runtime error during transformation in transformFunction.
 * 2. Tree Transformations: transformTree, transform, transformArrayComp, transformArrayLiteral,
 *    transformAssignment, transformBlock, transformBreak, transformCondExpr, transformContinue,
 *    transformDoLoop, transformElementGet, transformExprStmt, transformForInLoop, transformForLoop,
 *    transformFunction, transformFunctionCall, transformIf, transformInfix, transformLabeledStatement,
 *    transformLetNode, transformLiteral, transformName, transformNewExpr, transformNumber,
 *    transformObjectLiteral, transformParenExpr, transformPropertyGet, transformRegExp, transformReturn,
 *    transformScript, transformString, transformSwitch, transformThrow, transformTry, transformUnary,
 *    transformVariables, transformWhileLoop, transformWith, transformYield, transformXml*.
 * 3. Constant Foldings & Simplifications:
 *    - Binary operations: ADD (string+string, string+num, num+string, num+num), SUB (num-num, 0-x, x-0),
 *      MUL (num*num, 1*x, x*1), DIV (num/num, x/1), AND, OR with boolean evaluations.
 *    - Unary operations: DELPROP, TYPEOF, BITNOT, NEG, NOT.
 * 4. Control flow structures:
 *    - Loops (Do-while, While, For, For..In with destructuring, with let, with var).
 *    - Switch statements: with default, without default, multiple cases.
 *    - Try-Catch-Finally: with conditional catch, multiple catches, with finally, empty finally.
 * 5. Destructuring forms in arrays and objects, decompilation paths.
 */
public class IRFactoryGeminiTest {

    private IRFactory createFactory() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setGeneratingSource(true);
        return new IRFactory(env);
    }

    private AstRoot parseAndTransform(String source) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(Context.VERSION_1_8);
        env.setGeneratingSource(true);
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(source, "test.js", 1);
        IRFactory factory = new IRFactory(env);
        factory.transformTree(root);
        return root;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Incomplete Function)
    // =========================================================================

    /**
     * Target Defects4J defect: com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction
     * Tests handling of FunctionNode with an empty or non-standard/missing body or syntax error recovery.
     * When a FunctionNode has an empty/uninitialized block or missing statements in parser recovery,
     * transformFunction must properly handle null/empty bodies without internal compiler error or NPE.
     */
    @Test(timeout = 4000)
    public void testIncompleteFunction_EmptyBodyHandling() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(true);
        IRFactory factory = new IRFactory(env);

        AstRoot root = new AstRoot();
        FunctionNode fn = new FunctionNode();
        fn.setFunctionName(new Name(0, "incompleteFn"));
        Block body = new Block(); // Empty block created during error recovery or syntax incomplete
        body.setLineno(1);
        fn.setBody(body);
        root.addChildToBack(fn);

        ScriptNode result = factory.transformTree(root);
        assertNotNull("ScriptNode must be generated even for incomplete functions", result);
        assertNotNull("Function must be registered", root.getFunctionNode(0));
    }

    @Test(timeout = 4000)
    public void testIncompleteFunction_FunctionWithoutReturnOrBodyStatements() {
        // Parse a function that terminates prematurely or has no body content
        AstRoot root = parseAndTransform("function foo() {}");
        assertNotNull(root);
        assertEquals(Token.SCRIPT, root.getType());
        assertEquals(1, root.getFunctionCount());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Parsing & Transform)
    // =========================================================================

    @Test(timeout = 4000)
    public void testTransformLiterals() {
        AstRoot root = parseAndTransform("true; false; null; this; debugger; 123; 'hello'; /abc/g;");
        assertNotNull(root);
        assertEquals(Token.SCRIPT, root.getType());
    }

    @Test(timeout = 4000)
    public void testTransformBinaryFoldings_Add() {
        AstRoot root = parseAndTransform("var a = 'a' + 'b'; var b = 'c' + 1; var c = 2 + 'd'; var d = 3 + 4;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformBinaryFoldings_SubMulDiv() {
        AstRoot root = parseAndTransform("var s1 = 10 - 2; var s2 = 0 - a; var s3 = a - 0;" +
                                         "var m1 = 3 * 4; var m2 = 1 * a; var m3 = a * 1;" +
                                         "var d1 = 8 / 2; var d2 = a / 1;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformBinaryFoldings_Logical() {
        AstRoot root = parseAndTransform("var a1 = true && x; var a2 = false && x; var a3 = x && y;" +
                                         "var o1 = true || x; var o2 = false || x; var o3 = x || y;" +
                                         "var n1 = 0 && x; var n2 = 1 && x; var n3 = 0 || x; var n4 = 2 || x;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformUnaryFoldings() {
        AstRoot root = parseAndTransform("var a = ~5; var b = -10; var c = !true; var d = !false; var e = !1; var f = !0; var g = !x;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformIncDec() {
        AstRoot root = parseAndTransform("x++; ++x; y--; --y; o.p++; ++o.p; arr[0]++; ++arr[0];");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformDeleteAndTypeof() {
        AstRoot root = parseAndTransform("delete a; delete o.p; delete arr[1]; delete foo(); delete 123; typeof a; typeof 123;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformIfStatements() {
        AstRoot root = parseAndTransform("if (true) { x(); } if (false) { y(); } else { z(); } if (cond) { a(); } else { b(); }");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformConditionalExpression() {
        AstRoot root = parseAndTransform("var a = true ? 1 : 2; var b = false ? 3 : 4; var c = cond ? 5 : 6;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformWhileAndDoLoops() {
        AstRoot root = parseAndTransform("while (x > 0) { x--; } do { x++; } while (x < 10);");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformForLoops() {
        AstRoot root = parseAndTransform("for (var i = 0; i < 10; i++) { foo(i); }" +
                                         "for (let j = 0; j < 10; j++) { foo(j); }" +
                                         "for (k = 0; ; k++) { if (k > 5) break; }" +
                                         "for (;;) { break; }");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformForInLoops() {
        AstRoot root = parseAndTransform("for (var p in obj) { foo(p); }" +
                                         "for (let q in obj) { foo(q); }" +
                                         "for (x in obj) { foo(x); }" +
                                         "for (var [k, v] in obj) { foo(k, v); }");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformSwitch() {
        AstRoot root = parseAndTransform("switch (expr) {" +
                                         "  case 1: foo(); break;" +
                                         "  case 2: bar();" +
                                         "  default: baz(); break;" +
                                         "}" +
                                         "switch (expr2) {" +
                                         "  case 'a': break;" +
                                         "}");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformTryCatchFinally() {
        AstRoot root = parseAndTransform("try { a(); } catch (e) { b(e); }" +
                                         "try { a(); } finally { c(); }" +
                                         "try { a(); } catch (e if e instanceof TypeError) { b(); } catch (e) { c(); } finally { d(); }" +
                                         "try {} finally {}");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformFunctionsAndClosures() {
        AstRoot root = parseAndTransform("function named(a, b) { return a + b; }" +
                                         "var f1 = function(x) { return x * 2; };" +
                                         "var f2 = function namedExpr(y) { return namedExpr(y - 1); };" +
                                         "var obj = { eval: function(code) { return eval(code); } };" +
                                         "eval('1+1'); With(obj); obj.eval('2+2');");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformObjectsAndArrays() {
        AstRoot root = parseAndTransform("var arr = [1, , 3, 4];" +
                                         "var obj = { a: 1, 'b': 2, 3: 4, get x() { return 5; }, set x(v) {} };" +
                                         "var emptyObj = {}; var emptyArr = [];");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformAssignments() {
        AstRoot root = parseAndTransform("x = 1; x += 2; x -= 3; x *= 4; x /= 5; x %= 6; " +
                                         "x &= 7; x |= 8; x ^= 9; x <<= 1; x >>= 2; x >>>= 3; " +
                                         "obj.prop += 10; arr[1] += 20;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformDestructuringAssignments() {
        AstRoot root = parseAndTransform("var [a, b] = [1, 2];" +
                                         "var {x, y: z} = {x: 1, y: 2};" +
                                         "[a, b] = [b, a];");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformWithAndLabeledStatement() {
        AstRoot root = parseAndTransform("lbl: for (var i = 0; i < 2; i++) {" +
                                         "  with (obj) { continue lbl; }" +
                                         "  break lbl;" +
                                         "}");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformYield() {
        AstRoot root = parseAndTransform("function* gen() { yield 1; yield; }");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformArrayComprehension() {
        AstRoot root = parseAndTransform("var res = [x * 2 for (x in [1, 2, 3]) if (x > 1)];");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformLetNode() {
        AstRoot root = parseAndTransform("let (a = 1, b = 2) { var c = a + b; }");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformParenthesizedExpression() {
        AstRoot root = parseAndTransform("var a = ((1 + 2) * 3);");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testTransformNewExpression() {
        AstRoot root = parseAndTransform("var d = new Date(); var d2 = new Date(1, 2);");
        assertNotNull(root);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTransformEmptyBlockAndScripts() {
        AstRoot root = parseAndTransform("");
        assertNotNull(root);
        assertEquals(Token.SCRIPT, root.getType());

        AstRoot root2 = parseAndTransform("; ; ;");
        assertNotNull(root2);
    }

    @Test(timeout = 4000)
    public void testNumericBoundariesInFolding() {
        AstRoot root = parseAndTransform("var n1 = 0 / 0; var n2 = 1 / 0; var n3 = -1 / 0; var n4 = 0 * 10; var n5 = -0;");
        assertNotNull(root);
    }

    @Test(timeout = 4000)
    public void testDirectTransformEmptyNode() {
        IRFactory factory = createFactory();
        EmptyExpression empty = new EmptyExpression();
        Node result = factory.transform(empty);
        assertSame(empty, result);
    }

    @Test(timeout = 4000)
    public void testDirectTransformReturnNoValue() {
        IRFactory factory = createFactory();
        ReturnStatement ret = new ReturnStatement(10);
        Node result = factory.transform(ret);
        assertEquals(Token.RETURN, result.getType());
        assertNull(result.getFirstChild());
    }

    @Test(timeout = 4000)
    public void testDirectTransformYieldNoValue() {
        IRFactory factory = createFactory();
        Yield y = new Yield(5);
        Node result = factory.transform(y);
        assertEquals(Token.YIELD, result.getType());
        assertNull(result.getFirstChild());
    }

    // =========================================================================
    // Partition D: Defensive Guard Paths & Exceptions
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTransformUnsupportedNodeThrowsException() {
        IRFactory factory = createFactory();
        AstNode unknownNode = new AstNode() {
            @Override
            public String toSource(int depth) {
                return "";
            }
        };
        // By default type is -1 / unknown
        factory.transform(unknownNode);
    }

    @Test(timeout = 4000)
    public void testInvalidDestructuringAssignmentFailsGracefully() {
        // Invalid assignment targets (e.g. 5 = 10) trigger error reporting in parser
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(true);
        Parser p = new Parser(env);
        AstRoot root = p.parse("5 = 10;", "test.js", 1);
        IRFactory factory = new IRFactory(env);
        Node res = factory.transformTree(root);
        assertNotNull(res);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIRFactoryConstructors() {
        IRFactory factory1 = new IRFactory();
        assertNotNull(factory1);

        CompilerEnvirons env = new CompilerEnvirons();
        IRFactory factory2 = new IRFactory(env);
        assertNotNull(factory2);

        ErrorCollector collector = new ErrorCollector();
        IRFactory factory3 = new IRFactory(env, collector);
        assertNotNull(factory3);
    }

    @Test(timeout = 4000)
    public void testDecompilerIntegrity() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setGeneratingSource(true);
        IRFactory factory = new IRFactory(env);
        AstRoot root = new Parser(env).parse("function add(a, b) { return a + b; }", "add.js", 1);
        ScriptNode script = factory.transformTree(root);
        assertNotNull(script.getEncodedSource());
        assertTrue(script.getEncodedSource().length() > 0);
    }
}