/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.mozilla.javascript.IRFactory
 * -----------------------------------------------------------------------------------------
 * Decision Branches & Structural Coverage Targeted:
 *  1. transformTree(AstRoot)
 *     - inUseStrictDirective = root.isInStrictMode() [true/false]
 *     - Token.printTrees [true/false]
 *     - compilerEnv.isGeneratingSource() [true/false] -> setEncodedSource
 *  2. transform(AstNode) Switch Coverage:
 *     - ARRAYCOMP (with for, for each, destructuring iterator, with/without filter)
 *     - ARRAYLIT (empty, non-empty, with holes/skipIndexes, destructuring)
 *     - BLOCK (Scope instance vs plain Node)
 *     - BREAK (with/without label)
 *     - CALL (regular, eval, With, property eval)
 *     - CONTINUE (with/without label)
 *     - DO (do..while loop)
 *     - EMPTY (EmptyExpression / EmptyStatement)
 *     - FOR (ForInLoop: for..in, for..each, destructuring array/object; ForLoop: let init splitScope, standard init, empty cond)
 *     - FUNCTION (statement, expression, named expression with THISFN, closure, activation, destructuring params)
 *     - GENEXPR (generator expression with filter, multiple loops, destructuring)
 *     - GETELEM / GETPROP (normal props, special props: __proto__, __parent__)
 *     - HOOK (ternary: constant true, constant false, dynamic condition)
 *     - IF (constant true, constant false with/without else, dynamic with/without else)
 *     - Literals: TRUE, FALSE, THIS, NULL, DEBUGGER, NAME, NUMBER, REGEXP, STRING
 *     - NEW (with/without args, with object initializer)
 *     - OBJECTLIT (empty, getters, setters, standard props, destructuring)
 *     - RETURN (with value, without value, expression closure)
 *     - SCRIPT (ScriptNode transform)
 *     - SWITCH (cases, default case, break handling, without default)
 *     - THROW (expression throw)
 *     - TRY (catch with condition, catch without condition, multiple catches, finally, rethrow)
 *     - WHILE (while loop)
 *     - WITH (with statement)
 *     - YIELD (yield with value, empty yield)
 *     - Default AstNodes: ExpressionStatement, Assignment (all compound ops, invalid lhs),
 *       UnaryExpression (delete name/prop/elem/literal, typeof, bitnot, neg, not, inc/dec),
 *       XmlMemberGet, InfixExpression (constant folding: ADD, SUB, MUL, DIV, AND, OR, XmlDotQuery),
 *       VariableDeclaration, ParenthesizedExpression (nested), LabeledStatement (single/multi/block),
 *       LetNode (let statement, let expression), XmlLiteral, XmlRef.
 *     - Default fallback: unknown AstNode -> IllegalArgumentException.
 *  3. Constant Folding in createBinary:
 *     - String concat ("a" + "b", "a" + 1, 1 + "b")
 *     - Number add (1 + 2)
 *     - Number sub (2 - 1, 0 - x -> -x, x - 0 -> +x)
 *     - Number mul (2 * 3, 1 * x -> +x, x * 1 -> +x)
 *     - Number div (6 / 2, x / 1 -> +x)
 *     - Logic AND/OR with known boolean / truthy / falsy values
 *  4. Decompiler & Package-Private Helpers:
 *     - decompileArrayLiteral, decompileObjectLiteral (with and without DESTRUCTURING_SHORTHAND)
 *     - decompilePropertyGet, decompileElementGet, decompile(THIS), decompile(error)
 *     - isDestructuring
 *  5. Defects4J Known Defect Targeting:
 *     - Suspicious block comment handling in comments parsing (testSuspiciousBlockCommentWarning3/4/5)
 */

package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import org.mozilla.javascript.ast.*;

public class IRFactoryGeminiTest {

    private static class TestErrorReporter implements ErrorReporter {
        final List<String> warnings = new ArrayList<String>();
        final List<String> errors = new ArrayList<String>();

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            warnings.add(message);
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            errors.add(message);
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
        }
    }

    private CompilerEnvirons createEnv(boolean genSource) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(Context.VERSION_1_8);
        env.setReservedKeywordAsIdentifier(true);
        env.setXmlAvailable(true);
        env.setGeneratingSource(genSource);
        return env;
    }

    private ScriptNode transform(String scriptSource) {
        return transform(scriptSource, false);
    }

    private ScriptNode transform(String scriptSource, boolean genSource) {
        CompilerEnvirons env = createEnv(genSource);
        TestErrorReporter reporter = new TestErrorReporter();
        IRFactory parserFactory = new IRFactory(env, reporter);
        AstRoot root = parserFactory.parse(scriptSource, "test.js", 1);
        IRFactory transformFactory = new IRFactory(env, reporter);
        return transformFactory.transformTree(root);
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTransformTreeEnvironmentVariants() {
        CompilerEnvirons env = createEnv(true);
        TestErrorReporter reporter = new TestErrorReporter();
        IRFactory pFactory = new IRFactory(env, reporter);
        AstRoot root = pFactory.parse("var a = 1;", "source.js", 1);
        root.setInStrictMode(true);

        boolean originalPrint = Token.printTrees;
        try {
            Token.printTrees = true;
            IRFactory tFactory = new IRFactory(env);
            ScriptNode scriptNode = tFactory.transformTree(root);
            assertNotNull(scriptNode);
            assertNotNull(scriptNode.getEncodedSource());
            assertTrue(scriptNode.isInStrictMode());
        } finally {
            Token.printTrees = originalPrint;
        }
    }

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        IRFactory factory = new IRFactory();
        assertNotNull(factory);
    }

    @Test(timeout = 4000)
    public void testLoops() {
        ScriptNode node = transform(
            "while (x < 1) { x++; }\n" +
            "do { x--; } while (x > 0);\n" +
            "for (var i = 0; i < 10; i++) {}\n" +
            "for (let j = 0; j < 10; j++) {}\n" +
            "for (k = 0; k < 10; k++) {}\n" +
            "for (;;) { break; }\n"
        );
        assertNotNull(node);
        assertEquals(Token.SCRIPT, node.getType());
    }

    @Test(timeout = 4000)
    public void testForInAndForInEach() {
        ScriptNode node = transform(
            "for (var p in obj) {}\n" +
            "for (let q in obj) {}\n" +
            "for (r in obj) {}\n" +
            "for each (var v in obj) {}\n" +
            "for (var [k, v] in obj) {}\n" +
            "for each (var [a, b] in obj) {}\n" +
            "for each (var {x, y} in obj) {}\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testFunctions() {
        ScriptNode node = transform(
            "function stdFn(a, b) { return a + b; }\n" +
            "var exprFn = function(x) { return x * 2; };\n" +
            "var namedExpr = function recur(n) { return n <= 1 ? 1 : recur(n - 1); };\n" +
            "var exprClosure = function(z) z * 10;\n" +
            "function nested() { function inner() { return 42; } return inner(); }\n" +
            "function destruct([x, y]) { return x + y; }\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testComprehensionsAndGenerators() {
        ScriptNode node = transform(
            "var a1 = [x for (x in list)];\n" +
            "var a2 = [x * 2 for (x in list) if (x > 0)];\n" +
            "var a3 = [y for each (y in list)];\n" +
            "var a4 = [b for ([a, b] in pairs)];\n" +
            "var g1 = (x for (x in list));\n" +
            "var g2 = (x * 2 for (x in list) if (x > 0));\n" +
            "var g3 = (b for ([a, b] in pairs));\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testArrayLiteralsWithHolesAndDestructuring() {
        ScriptNode node = transform(
            "var arr = [1, , 3, , , 6];\n" +
            "var empty = [];\n" +
            "var [d1, d2] = [10, 20];\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testObjectLiterals() {
        ScriptNode node = transform(
            "var o1 = {};\n" +
            "var o2 = { a: 1, 'b': 2, 3: 3 };\n" +
            "var o3 = { get x() { return this._x; }, set x(v) { this._x = v; } };\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testSwitchStatements() {
        ScriptNode node = transform(
            "switch (val) {\n" +
            "  case 1: res = 'one'; break;\n" +
            "  case 2: case 3: res = 'few'; break;\n" +
            "  default: res = 'many';\n" +
            "}\n" +
            "switch (val) {\n" +
            "  case 10: res = 'ten';\n" +
            "}\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testTryCatchFinally() {
        ScriptNode node = transform(
            "try { doWork(); } catch (e) { handle(e); }\n" +
            "try { doWork(); } catch (e if e instanceof TypeError) { handleType(e); } catch (e) { handle(e); }\n" +
            "try { doWork(); } catch (e if e > 0) { handlePos(e); }\n" + // conditional catch without default -> rethrow
            "try { doWork(); } finally { cleanUp(); }\n" +
            "try { doWork(); } catch (e) { err(e); } finally { cleanUp(); }\n" +
            "try {} finally {}\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testSpecialCallsAndProperties() {
        ScriptNode node = transform(
            "eval('1 + 1');\n" +
            "With(x);\n" +
            "foo.eval('2 + 2');\n" +
            "var p = obj.__proto__;\n" +
            "var q = obj.__parent__;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testLabelsBreakContinueWithYield() {
        ScriptNode node = transform(
            "lbl: for (var i = 0; i < 2; i++) {\n" +
            "  if (i === 0) continue lbl;\n" +
            "  if (i === 1) break lbl;\n" +
            "}\n" +
            "lblBlock: { var innerVar = 1; }\n" +
            "with (scopeObj) { testMethod(); }\n" +
            "function gen() { yield 100; yield; }\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testLetDeclarationsAndExpressions() {
        ScriptNode node = transform(
            "let (x = 10, y = 20) { var sum = x + y; };\n" +
            "var res = let (z = 5) z * 2;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testXmlSupport() {
        ScriptNode node = transform(
            "default xml namespace = 'http://ns.example.com';\n" +
            "var item = <foo bar='baz'>{childVal}</foo>;\n" +
            "var emptyChild = <foo>{}</foo>;\n" +
            "var list = <>anonymous</>;\n" +
            "var attr = item.@bar;\n" +
            "var desc = item..bar;\n" +
            "var query = item.(name == 'child');\n" +
            "var star = item.*::child;\n"
        );
        assertNotNull(node);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Constant Folding
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantFoldingAddition() {
        ScriptNode node = transform(
            "var s1 = 'hello ' + 'world';\n" +
            "var s2 = 'num: ' + 123;\n" +
            "var s3 = 456 + ' is num';\n" +
            "var n1 = 10 + 20;\n" +
            "var dynamicPlus = x + y;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingSubtraction() {
        ScriptNode node = transform(
            "var n1 = 10 - 4;\n" +
            "var n2 = 0 - x;\n" +
            "var n3 = x - 0;\n" +
            "var dynamicSub = x - y;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingMultiplication() {
        ScriptNode node = transform(
            "var n1 = 3 * 4;\n" +
            "var n2 = 1 * x;\n" +
            "var n3 = x * 1;\n" +
            "var dynamicMul = x * y;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingDivision() {
        ScriptNode node = transform(
            "var n1 = 12 / 3;\n" +
            "var n2 = x / 1;\n" +
            "var dynamicDiv = x / y;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingLogicalAndOr() {
        ScriptNode node = transform(
            "var a1 = false && x;\n" +
            "var a2 = true && x;\n" +
            "var a3 = 0 && x;\n" +
            "var a4 = 1 && x;\n" +
            "var o1 = true || x;\n" +
            "var o2 = false || x;\n" +
            "var o3 = 1 || x;\n" +
            "var o4 = 0 || x;\n" +
            "var dynAnd = x && y;\n" +
            "var dynOr = x || y;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingUnary() {
        ScriptNode node = transform(
            "var b1 = !true;\n" +
            "var b2 = !false;\n" +
            "var b3 = !0;\n" +
            "var b4 = !1;\n" +
            "var b5 = !x;\n" +
            "var not1 = ~5;\n" +
            "var not2 = ~x;\n" +
            "var neg1 = -5;\n" +
            "var neg2 = -x;\n" +
            "var t1 = typeof x;\n" +
            "var t2 = typeof 123;\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testConstantFoldingConditionalsAndIf() {
        ScriptNode node = transform(
            "var c1 = true ? 1 : 2;\n" +
            "var c2 = false ? 1 : 2;\n" +
            "var c3 = x ? 1 : 2;\n" +
            "if (true) { var it = 1; }\n" +
            "if (false) { var if1 = 1; }\n" +
            "if (false) { var if2 = 1; } else { var el2 = 2; }\n" +
            "if (x) { var if3 = 1; }\n" +
            "if (x) { var if4 = 1; } else { var el4 = 2; }\n"
        );
        assertNotNull(node);
    }

    @Test(timeout = 4000)
    public void testAssignmentsAndOperators() {
        ScriptNode node = transform(
            "var a = 1;\n" +
            "a += 2; a -= 3; a *= 4; a /= 5; a %= 6;\n" +
            "a &= 7; a |= 8; a ^= 9; a <<= 1; a >>= 2; a >>>= 3;\n" +
            "obj.prop += 1;\n" +
            "arr[idx] += 1;\n" +
            "++a; a++; --a; a--;\n" +
            "delete a; delete obj.prop; delete arr[idx]; delete 42;\n"
        );
        assertNotNull(node);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Suspicious comments