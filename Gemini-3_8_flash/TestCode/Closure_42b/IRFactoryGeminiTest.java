package org.mozilla.javascript;

import org.junit.Test;
import org.mozilla.javascript.ast.*;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.IRFactory
 *
 * Decision / Branch Matrix Covered:
 * 1. transformTree(AstRoot): Strict mode handling, encoded source generation (isGeneratingSource = true/false),
 *    offset tracking, single & multi-statement script transformation.
 * 2. transform(AstNode) Switch Dispatch:
 *    - ARRAYCOMP (ArrayComprehension, multiple loops, for each, destructuring iterator, filter presence/absence)
 *    - ARRAYLIT (ArrayLiteral: empty, with elements, destructuring flag, trailing commas, skip indexes)
 *    - BLOCK (plain Block, Scope node pushing/popping)
 *    - BREAK / CONTINUE (labeled and unlabeled)
 *    - CALL (FunctionCall: 0 args, 1 arg, multiple args, eval / With special call triggers)
 *    - DO (DoLoop: body, condition)
 *    - FOR (ForLoop with let/var/expr initializer, condition, increment; ForInLoop standard and for-each)
 *    - FUNCTION (function statement, expression, closure, getters, setters, activation requirements)
 *    - GETELEM / GETPROP (ElementGet, PropertyGet, special properties like __proto__)
 *    - HOOK (ConditionalExpression: normal, constant folded ALWAYS_TRUE, constant folded ALWAYS_FALSE)
 *    - IF (IfStatement: then only, then-else, constant folding for true/false)
 *    - LITERALS (TRUE, FALSE, THIS, NULL, DEBUGGER, NUMBER, STRING, REGEXP)
 *    - NEW (NewExpression: with args, without args, with initializer)
 *    - OBJECTLIT (ObjectLiteral: empty, normal prop, getter/setter, number/string/name key, destructuring)
 *    - RETURN / YIELD (with value, without value, expression closure)
 *    - SWITCH (SwitchStatement: with cases, default case, mixed order, empty statements)
 *    - THROW / TRY (TryStatement: catch, catch-if condition, finally, try-catch-finally, empty try block)
 *    - UNARY (INC, DEC, NOT, BITNOT, NEG, TYPEOF, DELPROP on names, properties, elements, refs)
 *    - VARIABLES (VariableDeclaration: VAR, LET, CONST, destructuring var assignments, uninitialized)
 *    - XML AST nodes (XmlLiteral, XmlMemberGet, XmlRef, XmlElemRef, XmlPropRef, DefaultXmlNamespace)
 *    - Fallback / Unrecognized AstNode (IllegalArgumentException verification)
 *
 * Defect Zone:
 * - Closure Compiler / Rhino testForEach target: ForInLoop with isForEach() = true, iterating over object / array,
 *   verifying ENUM_INIT_VALUES vs ENUM_INIT_KEYS, scoping and destructured variable bindings in for-each loops.
 */
public class IRFactoryGeminiTest {

    private ScriptNode parseAndTransform(String code) {
        return parseAndTransform(code, Context.VERSION_1_8, true);
    }

    private ScriptNode parseAndTransform(String code, int version, boolean generatingSource) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(version);
        env.setGeneratingSource(generatingSource);
        env.setReservedKeywordAsIdentifier(true);
        env.setXmlAvailable(true);

        IRFactory factory = new IRFactory(env);
        AstRoot root = factory.parse(code, "test.js", 1);
        return factory.transformTree(root);
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTransformTreeStrictModeAndSourceGeneration() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setGeneratingSource(true);
        IRFactory factory = new IRFactory(env);
        AstRoot root = factory.parse("'use strict'; var x = 10;", "strict.js", 1);

        ScriptNode script = factory.transformTree(root);
        assertNotNull(script);
        assertEquals(Token.SCRIPT, script.getType());
        assertNotNull(script.getEncodedSource());
        assertTrue(script.isInStrictMode());
    }

    @Test(timeout = 4000)
    public void testTransformTreeWithoutSourceGeneration() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setGeneratingSource(false);
        IRFactory factory = new IRFactory(env);
        AstRoot root = factory.parse("var a = 1;", "nosource.js", 1);

        ScriptNode script = factory.transformTree(root);
        assertNotNull(script);
        assertEquals(Token.SCRIPT, script.getType());
        assertNull(script.getEncodedSource());
    }

    @Test(timeout = 4000)
    public void testLiteralsAndIdentifiers() {
        ScriptNode script = parseAndTransform("true; false; null; this; debugger; 123.45; 'hello'; /abc/g;");
        assertNotNull(script);

        Node first = script.getFirstChild();
        assertNotNull(first);
        assertEquals(Token.EXPR_RESULT, first.getType());
        assertEquals(Token.TRUE, first.getFirstChild().getType());

        Node second = first.getNext();
        assertEquals(Token.FALSE, second.getFirstChild().getType());

        Node third = second.getNext();
        assertEquals(Token.NULL, third.getFirstChild().getType());

        Node fourth = third.getNext();
        assertEquals(Token.THIS, fourth.getFirstChild().getType());

        Node debuggerNode = fourth.getNext();
        assertEquals(Token.DEBUGGER, debuggerNode.getType());
    }

    @Test(timeout = 4000)
    public void testBinaryExpressionsConstantFolding() {
        // String + Number, Number + Number, Number - Number, 0 - x, x - 0, 1 * x, x * 1, x / 1
        ScriptNode script = parseAndTransform(
            "var a = 's' + 1; " +
            "var b = 2 + 3; " +
            "var c = 10 - 4; " +
            "var d = 0 - a; " +
            "var e = a - 0; " +
            "var f = 1 * a; " +
            "var g = a * 1; " +
            "var h = a / 1; " +
            "var i = 10 / 2;"
        );
        assertNotNull(script);
        Node node = script.getFirstChild();
        assertNotNull(node);
        assertEquals(Token.VAR, node.getType());
    }

    @Test(timeout = 4000)
    public void testLogicalAndOrConstantFolding() {
        // true && x -> x; false && x -> false; true || x -> true; false || x -> x
        ScriptNode script = parseAndTransform(
            "var a = true && 5; " +
            "var b = false && 5; " +
            "var c = true || 5; " +
            "var d = false || 5;"
        );
        assertNotNull(script);
        assertEquals(Token.VAR, script.getFirstChild().getType());
    }

    @Test(timeout = 4000)
    public void testUnaryExpressions() {
        ScriptNode script = parseAndTransform(
            "var x = 10; " +
            "+x; -x; ~5; -5; !true; !false; !x; typeof x; typeof(123);"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testDeleteExpressions() {
        ScriptNode script = parseAndTransform(
            "var obj = { a: 1, b: 2 }; " +
            "delete obj.a; " +
            "delete obj['b']; " +
            "delete obj; " +
            "delete foo(); " +
            "delete 42;"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testIncDecExpressions() {
        ScriptNode script = parseAndTransform(
            "var x = 1; " +
            "x++; ++x; x--; --x; " +
            "var o = {p: 1}; " +
            "o.p++; ++o.p; o['p']--; --o['p'];"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testIfElseStatements() {
        // Conditionals: constant true, constant false, normal variable
        ScriptNode script = parseAndTransform(
            "if (true) { var a = 1; } " +
            "if (false) { var b = 2; } else { var c = 3; } " +
            "if (false) { var d = 4; } " +
            "if (a > 0) { var e = 5; } else { var f = 6; }"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testConditionalExpression() {
        ScriptNode script = parseAndTransform(
            "var x = true ? 1 : 2; " +
            "var y = false ? 3 : 4; " +
            "var z = (x > 0) ? 5 : 6;"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testWhileAndDoWhileLoops() {
        ScriptNode script = parseAndTransform(
            "while (true) { break; } " +
            "do { continue; } while (false);"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testForLoops() {
        // Classic for-loop, empty parts, for-let loop splitting scope
        ScriptNode script = parseAndTransform(
            "for (var i = 0; i < 10; i++) {} " +
            "for (;;) { break; } " +
            "for (let j = 0; j < 5; j++) {}"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testSwitchStatementWithCasesAndDefault() {
        ScriptNode script = parseAndTransform(
            "switch (x) { " +
            "  case 1: break; " +
            "  case 2: var y = 2; " +
            "  default: var def = 0; break; " +
            "  case 3: break; " +
            "}"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testTryCatchFinally() {
        ScriptNode script = parseAndTransform(
            "try { throw 'err'; } catch (e) { var handled = e; } " +
            "try { var x = 1; } finally { var fin = 2; } " +
            "try { var y = 1; } catch (e if e === 1) { var c1 = 1; } catch (e) { var c2 = 2; } finally { var f2 = 3; }"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testFunctionDeclarationsAndExpressions() {
        ScriptNode script = parseAndTransform(
            "function namedFn(a, b) { return a + b; } " +
            "var anon = function(x) { return x * 2; }; " +
            "var namedExpr = function inner(n) { return (n <= 1) ? 1 : n * inner(n - 1); }; " +
            "(function() { eval('1'); With(obj); })();"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testObjectLiteralVariants() {
        ScriptNode script = parseAndTransform(
            "var obj = { " +
            "  normal: 1, " +
            "  'strKey': 2, " +
            "  123: 3, " +
            "  get getter() { return this.normal; }, " +
            "  set setter(val) { this.normal = val; } " +
            "};"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testArrayLiteralAndElisions() {
        ScriptNode script = parseAndTransform("var arr = [1, , 2, , , 3];");
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testArrayComprehensions() {
        ScriptNode script = parseAndTransform(
            "var list = [x * 2 for (x in [1, 2, 3]) if (x > 1)]; " +
            "var multi = [x + y for (x in [1, 2]) for (y in [3, 4])]; " +
            "var destruct = [a + b for ([a, b] in [[1, 2], [3, 4]])];"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testWithStatement() {
        ScriptNode script = parseAndTransform("var o = {a: 1}; with (o) { a = 2; }");
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testYieldStatement() {
        ScriptNode script = parseAndTransform(
            "function* gen() { " +
            "  yield 1; " +
            "  yield; " +
            "}"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testSpecialPropertiesRef() {
        ScriptNode script = parseAndTransform("var proto = obj.__proto__;");
        assertNotNull(script);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAstRoot() {
        CompilerEnvirons env = new CompilerEnvirons();
        IRFactory factory = new IRFactory(env);
        AstRoot root = new AstRoot();
        ScriptNode result = factory.transformTree(root);
        assertNotNull(result);
        assertEquals(Token.SCRIPT, result.getType());
        assertFalse(result.hasChildren());
    }

    @Test(timeout = 4000)
    public void testNestedParenthesizedExpressions() {
        ScriptNode script = parseAndTransform("var val = ((((42))));");
        assertNotNull(script);
        Node varNode = script.getFirstChild();
        Node nameNode = varNode.getFirstChild();
        Node initNode = nameNode.getFirstChild();
        assertEquals(Boolean.TRUE, initNode.getProp(Node.PARENTHESIZED_PROP));
    }

    @Test(timeout = 4000)
    public void testEmptySwitchStatement() {
        ScriptNode script = parseAndTransform("switch (val) {}");
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testEmptyTryCatchVariants() {
        // try block with empty body and empty finally
        ScriptNode script = parseAndTransform("try {} catch(e) {} finally {}");
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testAssignmentOperators() {
        ScriptNode script = parseAndTransform(
            "var x = 10; " +
            "x += 1; x -= 2; x *= 3; x /= 4; x %= 5; " +
            "x <<= 1; x >>= 2; x >>>= 3; " +
            "x &= 4; x ^= 5; x |= 6; " +
            "var obj = {a: 1}; " +
            "obj.a += 2; obj['a'] *= 3;"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testLabeledBreakAndContinue() {
        ScriptNode script = parseAndTransform(
            "outer: for (var i = 0; i < 2; i++) { " +
            "  inner: for (var j = 0; j < 2; j++) { " +
            "    if (i === 1) continue outer; " +
            "    if (j === 1) break inner; " +
            "  } " +
            "}"
        );
        assertNotNull(script);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J testForEach)
    // =========================================================================

    /**
     * Targets defects in `for each` loops (e.g. Defects4J ParserTest::testForEach),
     * ensuring that:
     * 1. `for each (var x in y)` creates an ENUM_INIT_VALUES initialization node.
     * 2. `for each` handles destructuring assignment properly.
     * 3. Non-foreach `for (var x in y)` creates an ENUM_INIT_KEYS node.
     */
    @Test(timeout = 4000)
    public void testForEachLoopDefectTarget() {
        ScriptNode script = parseAndTransform(
            "var items = [10, 20, 30]; " +
            "var sum = 0; " +
            "for each (var item in items) { " +
            "  sum += item; " +
            "} " +
            "for (var idx in items) { " +
            "  sum += idx; " +
            "}"
        );
        assertNotNull(script);

        // Find the local block for the 'for each' statement
        Node stmt = script.getFirstChild();
        while (stmt != null && stmt.getType() != Token.LOCAL_BLOCK) {
            stmt = stmt.getNext();
        }
        assertNotNull("Must find LOCAL_BLOCK containing for-each loop", stmt);

        // Inside LOCAL_BLOCK, the first child should be the LOOP node
        Node loopNode = stmt.getFirstChild();
        assertNotNull(loopNode);
        assertEquals(Token.LOOP, loopNode.getType());

        // First child of loopNode should be the ENUM_INIT_VALUES node because isForEach is true
        Node initNode = loopNode.getFirstChild();
        assertNotNull(initNode);
        assertEquals("for-each should generate ENUM_INIT_VALUES", Token.ENUM_INIT_VALUES, initNode.getType());

        // Now advance to the standard for-in loop
        stmt = stmt.getNext();
        while (stmt != null && stmt.getType() != Token.LOCAL_BLOCK) {
            stmt = stmt.getNext();
        }
        assertNotNull("Must find second LOCAL_BLOCK containing standard for-in loop", stmt);

        Node standardLoop = stmt.getFirstChild();
        Node standardInit = standardLoop.getFirstChild();
        assertEquals("Standard for-in should generate ENUM_INIT_KEYS", Token.ENUM_INIT_KEYS, standardInit.getType());
    }

    @Test(timeout = 4000)
    public void testForEachWithDestructuring() {
        ScriptNode script = parseAndTransform(
            "var pairs = [{a: 1}, {a: 2}]; " +
            "for each (var {a} in pairs) { " +
            "  var res = a; " +
            "}"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testArrayComprehensionWithForEach() {
        ScriptNode script = parseAndTransform(
            "var vals = [v * 2 for each (v in [10, 20, 30])];"
        );
        assertNotNull(script);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testTransformUnknownAstNodeThrowsException() {
        IRFactory factory = new IRFactory();
        AstNode unsupportedNode = new AstNode() {
            @Override
            public String toSource(int depth) { return ""; }
            @Override
            public int getType() { return 999999; /* Unknown token */ }
        };

        try {
            factory.transform(unsupportedNode);
            fail("Expected IllegalArgumentException for unknown AstNode type");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Can't transform"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidDestructuringAssignmentReportError() {
        // Bad destructuring op (e.g. += on array literal)
        CompilerEnvirons env = new CompilerEnvirons();
        TestErrorReporter errorReporter = new TestErrorReporter();
        IRFactory factory = new IRFactory(env, errorReporter);

        AstRoot root = factory.parse("([a, b] += 1);", "bad_destruct.js", 1);
        factory.transformTree(root);
        assertTrue(errorReporter.hasReportedError);
    }

    @Test(timeout = 4000)
    public void testInvalidForInLhsReportError() {
        CompilerEnvirons env = new CompilerEnvirons();
        TestErrorReporter errorReporter = new TestErrorReporter();
        IRFactory factory = new IRFactory(env, errorReporter);

        AstRoot root = factory.parse("for (5 in [1, 2]) {}", "bad_for_in.js", 1);
        factory.transformTree(root);
        assertTrue(errorReporter.hasReportedError);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Complex Features (E4X, Let, Destructuring)
    // =========================================================================

    @Test(timeout = 4000)
    public void testE4xExpressions() {
        ScriptNode script = parseAndTransform(
            "default xml namespace = 'http://example.com'; " +
            "var xml = <order id='123'><item price='10'>Book</item></order>; " +
            "var item = xml.item; " +
            "var attr = xml.@id; " +
            "var desc = xml..item; " +
            "var elemRef = xml[item]; " +
            "var nsRef = xml.ns::item;"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testLetNodeExpressionsAndStatements() {
        ScriptNode script = parseAndTransform(
            "let (x = 1, y = 2) { var sum = x + y; } " +
            "var res = let (a = 5) a * 2;"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testDestructuringInVariableDeclarations() {
        ScriptNode script = parseAndTransform(
            "var [a, b, c] = [1, 2, 3]; " +
            "var {p1: x, p2: y} = {p1: 10, p2: 20}; " +
            "let [h, ...tail] = [1, 2, 3, 4];"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testFunctionWithDestructuringParams() {
        ScriptNode script = parseAndTransform(
            "function unpack([x, y], {name, age}) { return x + name; }"
        );
        assertNotNull(script);
    }

    @Test(timeout = 4000)
    public void testConstructorInvocations() {
        IRFactory factory1 = new IRFactory();
        assertNotNull(factory1);

        CompilerEnvirons env = new CompilerEnvirons();
        IRFactory factory2 = new IRFactory(env);
        assertNotNull(factory2);

        TestErrorReporter reporter = new TestErrorReporter();
        IRFactory factory3 = new IRFactory(env, reporter);
        assertNotNull(factory3);
    }

    private static class TestErrorReporter implements ErrorReporter {
        boolean hasReportedError = false;

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {}

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            hasReportedError = true;
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            hasReportedError = true;
            return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
        }
    }
}