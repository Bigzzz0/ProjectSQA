package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.mozilla.rhino.ast.EmptyExpression;
import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.jscomp.parsing.IRFactory;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * =================================================================================================
 * Target: com.google.javascript.jscomp.parsing.IRFactory
 * Known Defect: ParserTest::testDestructuringAssignForbidden4 (Rhino destructuring assignment in
 *               parenthesized expression `({x, y} = new Object());` produces an ObjectProperty with
 *               a null right-hand expression, causing NullPointerException in processObjectLiteral /
 *               transform(el.getRight())).
 *
 * Partition Matrix:
 * - Partition A: Core Functional Logic & Language Constructs
 *   * Array/Object literals (unquoted names, quoted strings, destructuring detection)
 *   * Function declarations (named vs. anonymous/unnamed with lp positioning)
 *   * Control flow statements (if-then-else, switch-case-default, loops: while, do-while, for, for-in)
 *   * Labeled statements, break/continue with and without labels
 *   * Exception handling (try-catch, try-finally, try-catch-finally, conditional catch warning)
 *   * Operators: unary (+, -, !, ~, typeof, void, delete, ++, --, number negation optimization),
 *     infix (+, -, *, /, %, ==, ===, !=, !==, <, <=, >, >=, &&, ||, bitwise, assignment ops), hook (? :)
 *   * Comments and JSDoc: fileoverview, @license propagation, node-level JSDoc
 *   * ES5 directives ('use strict' extraction and AST annotation)
 *
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * Array literals with holes/elisions (skipCount > 0, Node.SKIP_INDEXES_PROP calculation)
 *   * Single statements vs. empty statements in block contexts (wasEmptyNode vs. block wrapping)
 *   * Multiline vs. single-line position-to-charno mapping (position2charno boundary checks)
 *   * Regular expressions with and without flags
 *   * Return statements with and without return value
 *
 * - Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *   * Destructuring assignments:
 *     - Array destructuring in var: `var [x, y] = [1, 2];`
 *     - Object destructuring in var: `var {x, y} = new Object();`
 *     - Array destructuring assignment: `[x, y] = [1, 2];`
 *     - Object destructuring assignment: `({x, y} = new Object());` (Reveals NPE defect in defective AST)
 *
 * - Partition D: Exception & Defensive Guard Paths
 *   * ES5 getter/setter validation when acceptES5 is false
 *   * Unsupported syntax handling via processIllegalToken
 *   * transformTokenType default branch guard (IllegalStateException on unknown token type)
 *
 * - Partition E: Object Lifecycle & Contract Integrity
 *   * Template node propagation (SOURCENAME_PROP)
 *   * Synthetic block and parenthesized expression property integrity
 * =================================================================================================
 */
public class IRFactoryGeminiTest {

    // -------------------------------------------------------------------------
    // Test Infrastructure Helpers
    // -------------------------------------------------------------------------

    private static class RecordingErrorReporter implements ErrorReporter {
        final List<String> errors = new ArrayList<String>();
        final List<String> warnings = new ArrayList<String>();

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
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }

    private static Config createTestConfig(boolean acceptES5) {
        try {
            Class<?> configClass = Class.forName("com.google.javascript.jscomp.parsing.Config");
            Constructor<?>[] constructors = configClass.getDeclaredConstructors();
            for (Constructor<?> c : constructors) {
                c.setAccessible(true);
                Class<?>[] pTypes = c.getParameterTypes();
                Object[] args = new Object[pTypes.length];
                int boolCount = 0;
                for (int i = 0; i < pTypes.length; i++) {
                    if (pTypes[i] == boolean.class || pTypes[i] == Boolean.class) {
                        boolCount++;
                        if (boolCount == 1) {
                            args[i] = true; // isIdeMode
                        } else {
                            args[i] = acceptES5;
                        }
                    } else if (pTypes[i] == Set.class) {
                        args[i] = Collections.emptySet();
                    } else if (pTypes[i].isEnum()) {
                        Object[] constants = pTypes[i].getEnumConstants();
                        args[i] = constants.length > 0 ? constants[0] : null;
                    } else {
                        args[i] = null;
                    }
                }
                return (Config) c.newInstance(args);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct Config instance reflectively", e);
        }
        return null;
    }

    private Node parseAndTransform(String code, boolean acceptES5, RecordingErrorReporter errorReporter) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setErrorReporter(errorReporter);
        env.setRecordingComments(true);
        env.setRecordingLocalJsDocComments(true);
        Parser p = new Parser(env, errorReporter);
        AstRoot ast = p.parse(code, "testSource.js", 1);
        Config config = createTestConfig(acceptES5);
        return IRFactory.transformTree(ast, code, config, errorReporter);
    }

    private Node parseAndTransform(String code) {
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        return parseAndTransform(code, true, reporter);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFunctionDeclarationsNamedAndUnnamed() {
        String code = "function foo(a, b) { return a + b; }\n" +
                      "var fn = function(x) { return x; };";
        Node script = parseAndTransform(code);
        assertEquals(Token.SCRIPT, script.getType());

        Node namedFn = script.getFirstChild();
        assertEquals(Token.FUNCTION, namedFn.getType());
        Node fnName = namedFn.getFirstChild();
        assertEquals(Token.NAME, fnName.getType());
        assertEquals("foo", fnName.getString());
        assertEquals(1, namedFn.getLineno());

        Node varDecl = namedFn.getNext();
        assertEquals(Token.VAR, varDecl.getType());
        Node varName = varDecl.getFirstChild();
        Node anonFn = varName.getFirstChild();
        assertEquals(Token.FUNCTION, anonFn.getType());
        Node emptyName = anonFn.getFirstChild();
        assertEquals("", emptyName.getString());
        assertEquals(2, emptyName.getLineno());
    }

    @Test(timeout = 4000)
    public void testControlFlowStatements() {
        String code = "if (true) { var a = 1; } else { var a = 2; }\n" +
                      "while (false) { break; }\n" +
                      "do { continue; } while (false);\n" +
                      "for (var i = 0; i < 10; i++) {}\n" +
                      "for (var k in obj) {}\n" +
                      "switch (x) { case 1: break; default: break; }\n" +
                      "with (o) { foo(); }";
        Node script = parseAndTransform(code);
        assertNotNull(script);
        assertEquals(7, script.getChildCount());

        Node ifNode = script.getFirstChild();
        assertEquals(Token.IF, ifNode.getType());

        Node whileNode = ifNode.getNext();
        assertEquals(Token.WHILE, whileNode.getType());

        Node doNode = whileNode.getNext();
        assertEquals(Token.DO, doNode.getType());

        Node forNode = doNode.getNext();
        assertEquals(Token.FOR, forNode.getType());

        Node forInNode = forNode.getNext();
        assertEquals(Token.FOR, forInNode.getType());

        Node switchNode = forInNode.getNext();
        assertEquals(Token.SWITCH, switchNode.getType());

        Node withNode = switchNode.getNext();
        assertEquals(Token.WITH, withNode.getType());
    }

    @Test(timeout = 4000)
    public void testLabeledStatementAndBreakContinueTarget() {
        String code = "outer: for (;;) { inner: while (true) { break outer; continue inner; } }";
        Node script = parseAndTransform(code);
        Node labelNode = script.getFirstChild();
        assertEquals(Token.LABEL, labelNode.getType());
        Node labelName = labelNode.getFirstChild();
        assertEquals(Token.LABEL_NAME, labelName.getType());
        assertEquals("outer", labelName.getString());
    }

    @Test(timeout = 4000)
    public void testTryCatchFinallyVariations() {
        String code = "try { var a = 1; } catch (e) { var b = 2; }\n" +
                      "try { var c = 3; } finally { var d = 4; }\n" +
                      "try { var e = 5; } catch (err) { var f = 6; } finally { var g = 7; }";
        Node script = parseAndTransform(code);
        assertEquals(3, script.getChildCount());

        for (Node child = script.getFirstChild(); child != null; child = child.getNext()) {
            assertEquals(Token.TRY, child.getType());
        }
    }

    @Test(timeout = 4000)
    public void testUnaryAndInfixExpressions() {
        String code = "var a = -42;\n" +
                      "var b = -x;\n" +
                      "var c = +x;\n" +
                      "var d = !x;\n" +
                      "var e = ~x;\n" +
                      "var f = typeof x;\n" +
                      "var g = void 0;\n" +
                      "var h = x++;\n" +
                      "var i = ++x;\n" +
                      "var j = (x ? y : z);";
        Node script = parseAndTransform(code);

        // a = -42 should be folded into negative number node
        Node varA = script.getFirstChild().getFirstChild();
        Node numVal = varA.getFirstChild();
        assertEquals(Token.NUMBER, numVal.getType());
        assertEquals(-42.0, numVal.getDouble(), 0.0001);

        // b = -x should remain Token.NEG
        Node varB = script.getFirstChild().getNext().getFirstChild();
        assertEquals(Token.NEG, varB.getFirstChild().getType());

        // h = x++ has INCRDECR_PROP set
        Node varH = script.getChildAtIndex(7).getFirstChild();
        Node postIncr = varH.getFirstChild();
        assertEquals(Token.INC, postIncr.getType());
        assertTrue(postIncr.getBooleanProp(Node.INCRDECR_PROP));

        // j has Hook (conditional) and Parenthesized prop
        Node varJ = script.getChildAtIndex(9).getFirstChild();
        Node hook = varJ.getFirstChild();
        assertEquals(Token.HOOK, hook.getType());
        assertTrue(hook.getBooleanProp(Node.PARENTHESIZED_PROP));
    }

    @Test(timeout = 4000)
    public void testDirectivesParsing() {
        String code = "'use strict';\nvar a = 1;\nfunction f() { 'use strict'; return 2; }";
        Node script = parseAndTransform(code);
        assertNotNull(script.getDirectives());
        assertTrue(script.getDirectives().contains("use strict"));

        Node fnNode = script.getLastChild();
        assertEquals(Token.FUNCTION, fnNode.getType());
        Node fnBody = fnNode.getLastChild();
        assertNotNull(fnBody.getDirectives());
        assertTrue(fnBody.getDirectives().contains("use strict"));
    }

    @Test(timeout = 4000)
    public void testFileOverviewAndJsDocLicense() {
        String code = "/**\n" +
                      " * @fileoverview Module description\n" +
                      " * @license MIT License\n" +
                      " */\n" +
                      "/** @type {number} */\n" +
                      "var x = 10;";
        Node script = parseAndTransform(code);
        JSDocInfo fileDoc = script.getJSDocInfo();
        assertNotNull(fileDoc);
        assertEquals("MIT License", fileDoc.getLicense());

        Node varNode = script.getFirstChild();
        JSDocInfo varDoc = varNode.getJSDocInfo();
        assertNotNull(varDoc);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testArrayLiteralWithHolesSkipIndexes() {
        String code = "var arr = [1, , 3, , , 6];";
        Node script = parseAndTransform(code);
        Node varNode = script.getFirstChild();
        Node arrLit = varNode.getFirstChild().getFirstChild();
        assertEquals(Token.ARRAYLIT, arrLit.getType());

        int[] skips = (int[]) arrLit.getProp(Node.SKIP_INDEXES_PROP);
        assertNotNull(skips);
        assertEquals(3, skips.length);
        assertEquals(1, skips[0]);
        assertEquals(3, skips[1]);
        assertEquals(4, skips[2]);
    }

    @Test(timeout = 4000)
    public void testTransformBlockBranchConditioning() {
        // Condition: irNode.getType() == Token.EMPTY vs single expression statement
        String code = "if (true); else x = 1;";
        Node script = parseAndTransform(code);
        Node ifNode = script.getFirstChild();

        Node thenPart = ifNode.getChildAtIndex(1);
        assertEquals(Token.BLOCK, thenPart.getType());
        assertTrue(thenPart.wasEmptyNode());

        Node elsePart = ifNode.getChildAtIndex(2);
        assertEquals(Token.BLOCK, elsePart.getType());
        assertFalse(elsePart.wasEmptyNode());
        assertEquals(Token.EXPR_RESULT, elsePart.getFirstChild().getType());
    }

    @Test(timeout = 4000)
    public void testRegExpLiteralWithAndWithoutFlags() {
        String code = "var r1 = /pattern/gi;\n" +
                      "var r2 = /simple/;";
        Node script = parseAndTransform(code);
        Node r1Node = script.getFirstChild().getFirstChild().getFirstChild();
        assertEquals(Token.REGEXP, r1Node.getType());
        assertEquals(2, r1Node.getChildCount()); // pattern and flags
        assertEquals("gi", r1Node.getLastChild().getString());

        Node r2Node = script.getLastChild().getFirstChild().getFirstChild();
        assertEquals(Token.REGEXP, r2Node.getType());
        assertEquals(1, r2Node.getChildCount()); // pattern only
    }

    @Test(timeout = 4000)
    public void testMultilinePositionToCharno() {
        String code = "var firstLine = 1;\n" +
                      "var secondLine = 2;\n" +
                      "\n" +
                      "var fourthLine = 4;";
        Node script = parseAndTransform(code);
        Node fourthVar = script.getLastChild();
        assertEquals(4, fourthVar.getLineno());
        assertEquals(0, fourthVar.getCharno());
    }

    @Test(timeout = 4000)
    public void testObjectPropertiesQuotedVsUnquoted() {
        String code = "var obj = { a: 1, 'b': 2 };";
        Node script = parseAndTransform(code);
        Node objLit = script.getFirstChild().getFirstChild().getFirstChild();
        Node propA = objLit.getFirstChild();
        Node propB = propA.getNext();

        assertEquals(Token.STRING, propA.getType());
        assertFalse(propA.getBooleanProp(Node.QUOTED_PROP));

        assertEquals(Token.STRING, propB.getType());
        assertTrue(propB.getBooleanProp(Node.QUOTED_PROP));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden1ArrayVar() {
        String code = "var [x, y] = [1, 2];";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        parseAndTransform(code, false, reporter);
        assertTrue("Expected destructuring assignment error on array pattern in var",
                reporter.errors.contains("destructuring assignment forbidden"));
    }

    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden2ObjectVar() {
        String code = "var {x, y} = new Object();";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        parseAndTransform(code, false, reporter);
        assertTrue("Expected destructuring assignment error on object pattern in var",
                reporter.errors.contains("destructuring assignment forbidden"));
    }

    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden3ArrayAssign() {
        String code = "[x, y] = [1, 2];";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        parseAndTransform(code, false, reporter);
        assertTrue("Expected destructuring assignment error on array assignment",
                reporter.errors.contains("destructuring assignment forbidden"));
    }

    /**
     * TARGET DEFECT TEST CASE:
     * Targets `ParserTest::testDestructuringAssignForbidden4`.
     * In the defective version, parsing `({x, y} = new Object());` causes an ObjectProperty
     * with null value (`el.getRight() == null`) to be passed to `transform(null)`,
     * throwing a fatal NullPointerException instead of properly reporting the error.
     */
    @Test(timeout = 4000)
    public void testDestructuringAssignForbidden4() {
        String code = "({x, y} = new Object());";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        Node result = parseAndTransform(code, false, reporter);
        assertNotNull("Transformation must complete and return an AST node", result);
        assertTrue("Must report 'destructuring assignment forbidden'",
                reporter.errors.contains("destructuring assignment forbidden"));
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetterSetterForbiddenWhenAcceptES5IsFalse() {
        String codeGetter = "var o = { get x() { return 1; } };";
        RecordingErrorReporter getterReporter = new RecordingErrorReporter();
        parseAndTransform(codeGetter, false, getterReporter);
        assertTrue(getterReporter.errors.contains("getters are not supported in Internet Explorer"));

        String codeSetter = "var o = { set x(v) { } };";
        RecordingErrorReporter setterReporter = new RecordingErrorReporter();
        parseAndTransform(codeSetter, false, setterReporter);
        assertTrue(setterReporter.errors.contains("setters are not supported in Internet Explorer"));
    }

    @Test(timeout = 4000)
    public void testGetterSetterAcceptedWhenAcceptES5IsTrue() {
        String code = "var o = { get x() { return 1; }, set x(v) { this.val = v; } };";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        Node script = parseAndTransform(code, true, reporter);
        assertEquals(0, reporter.errors.size());
        Node objLit = script.getFirstChild().getFirstChild().getFirstChild();
        Node getProp = objLit.getFirstChild();
        assertEquals(Token.GET, getProp.getType());
        Node setProp = getProp.getNext();
        assertEquals(Token.SET, setProp.getType());
    }

    @Test(timeout = 4000)
    public void testConditionalCatchClauseReportsError() {
        String code = "try { foo(); } catch (e if e > 0) { bar(); }";
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        parseAndTransform(code, true, reporter);
        assertTrue(reporter.errors.contains("Catch clauses are not supported"));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testTransformTokenTypeDefensiveExceptionOnUnknownToken() throws Throwable {
        Method m = IRFactory.class.getDeclaredMethod("transformTokenType", int.class);
        m.setAccessible(true);
        try {
            m.invoke(null, -99999);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    @Test(timeout = 4000)
    public void testProcessIllegalToken() {
        RecordingErrorReporter reporter = new RecordingErrorReporter();
        AstRoot root = new AstRoot();
        EmptyExpression illegalNode = new EmptyExpression();
        // Set an arbitrary unknown Rhino token
        illegalNode.setType(9999);
        root.addChild(illegalNode);
        Config config = createTestConfig(true);
        Node result = IRFactory.transformTree(root, "", config, reporter);

        assertNotNull(result);
        assertEquals(1, reporter.errors.size());
        assertTrue(reporter.errors.get(0).startsWith("Unsupported syntax:"));
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTemplateNodeSourcePropertyPreservation() {
        String code = "var a = 1; var b = 2;";
        Node script = parseAndTransform(code);
        assertEquals("testSource.js", script.getSourceFileName());
        assertEquals("testSource.js", script.getFirstChild().getSourceFileName());
        assertEquals("testSource.js", script.getLastChild().getSourceFileName());
    }

    @Test(timeout = 4000)
    public void testSwitchBlockSyntheticBlockProperty() {
        String code = "switch (x) { case 1: var a = 1; break; }";
        Node script = parseAndTransform(code);
        Node switchNode = script.getFirstChild();
        Node caseNode = switchNode.getChildAtIndex(1);
        assertEquals(Token.CASE, caseNode.getType());
        Node caseBlock = caseNode.getLastChild();
        assertEquals(Token.BLOCK, caseBlock.getType());
        assertTrue(caseBlock.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
    }
}
