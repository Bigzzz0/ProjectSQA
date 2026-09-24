package org.mozilla.javascript;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mozilla.javascript.CodeGenerator
 * Known Defect Target: testManyAdds -> Deep expression recursion causing StackOverflowError (Defects4J).
 * 
 * Major Decision Points & Execution Branches Covered:
 * 1. compile() & generateFunctionICode():
 *    - returnFunction = true vs false
 *    - Strict mode detection (((AstRoot)tree).isInStrictMode())
 *    - theFunction.isGenerator() -> addIcode(Icode_GENERATOR) & addUint16
 *    - theFunction.getFunctionName() != null vs null
 *    - itsFunctionType != 0 vs 0 (Token.RETURN_RESULT appended only to script)
 * 2. visitStatement():
 *    - Token.FUNCTION: statement vs expression statement vs script-level closure result
 *    - Token.LABEL, LOOP, BLOCK, EMPTY, WITH, SCRIPT
 *    - Token.ENTERWITH, LEAVEWITH
 *    - Token.LOCAL_BLOCK: local variable allocation and release
 *    - Token.DEBUGGER
 *    - Token.SWITCH: cases, fall-through, duplicate removal via Icode_IFEQ_POP
 *    - Token.IFEQ, IFNE, GOTO, JSR, FINALLY, TARGET (forward & backward branch resolution)
 *    - Token.TRY, CATCH_SCOPE, THROW, RETHROW
 *    - Token.RETURN: generator return (GENERATOR_END_PROP) vs expr return vs void return (RETUNDEF)
 *    - Token.ENUM_INIT_KEYS, ENUM_INIT_VALUES, ENUM_INIT_ARRAY
 * 3. visitExpression():
 *    - Token.CALL, NEW, REF_CALL: special call (eval), tail call optimization (ECF_TAIL) vs normal call
 *    - Token.AND, OR, HOOK (? :)
 *    - Token.GETPROP, GETPROPNOWARN, DELPROP (isName vs property)
 *    - Token.GETELEM, SETELEM, SETELEM_OP
 *    - Token.SETPROP, SETPROP_OP
 *    - Binary ops: ADD, SUB, MUL, DIV, MOD, BITAND, BITOR, BITXOR, LSH, RSH, URSH,
 *                  EQ, NE, SHEQ, SHNE, IN, INSTANCEOF, LE, LT, GE, GT
 *    - Unary ops: POS, NEG, NOT, BITNOT, TYPEOF, VOID
 *    - Token.INC, DEC: GETVAR, NAME, GETPROP, GETELEM
 *    - Token.NUMBER: 0 (and -0.0 check), 1, short numbers, int numbers, double table indexing
 *    - Token.GETVAR, SETVAR, SETCONSTVAR (< 128 vs >= 128 indices)
 *    - Token.NULL, THIS, FALSE, TRUE
 *    - Token.REGEXP: RegExpProxy invocation and array caching
 *    - Token.ARRAYLIT, OBJECTLIT: sparse array (SKIP_INDEXES_PROP), getters, setters
 *    - Token.ARRAYCOMP (JS 1.7 Array Comprehensions)
 *    - Token.YIELD
 * 4. Boundary & Table Expansion Paths:
 *    - increaseICodeCapacity(): large code buffers (> initial array size)
 *    - getDoubleIndex(): double table expansion (> 64 doubles)
 *    - addExceptionHandler(): exception table expansion (> 2 exceptions)
 *    - labelTable & fixupTable expansion (> 32 labels, > 40 forward jumps)
 *    - String index encoding (< 4, <= 0xFF, <= 0xFFFF)
 *    - Index operand encoding (< 6, <= 0xFF, <= 0xFFFF)
 * 5. Defensive / Exceptional Paths:
 *    - badTree() on unexpected node type in statement context
 * ====================================================================================================
 */
public class CodeGeneratorGeminiTest {

    private Context context;

    @Before
    public void setUp() {
        context = Context.enter();
    }

    @After
    public void tearDown() {
        Context.exit();
    }

    private InterpreterData compileScript(String source, int langVersion) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.initFromContext(context);
        if (langVersion != 0) {
            env.setLanguageVersion(langVersion);
        }
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(source, "test.js", 1);
        CodeGenerator gen = new CodeGenerator();
        return gen.compile(env, root, root.getEncodedSource(), false);
    }

    private InterpreterData compileScript(String source) {
        return compileScript(source, 0);
    }

    private InterpreterData compileFunction(String source, int langVersion) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.initFromContext(context);
        if (langVersion != 0) {
            env.setLanguageVersion(langVersion);
        }
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(source, "test.js", 1);
        CodeGenerator gen = new CodeGenerator();
        return gen.compile(env, root, root.getEncodedSource(), true);
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testCompileBasicScript() {
        InterpreterData data = compileScript("var a = 1; var b = 2; var c = a + b;");
        assertNotNull(data);
        assertTrue(data.topLevel);
        assertEquals(0, data.itsFunctionType);
        assertEquals(3, data.itsMaxVars);
        assertTrue(data.itsICode.length > 0);
        assertNotNull(data.itsStringTable);
        assertTrue(data.itsStringTable.length >= 3);
    }

    @Test(timeout = 4000)
    public void testCompileNamedFunction() {
        InterpreterData data = compileFunction("function myNamedFunc(p1, p2) { return p1 * p2; }", 0);
        assertNotNull(data);
        assertEquals("myNamedFunc", data.itsName);
        assertEquals(2, data.argCount);
        assertEquals(2, data.itsMaxVars);
        assertTrue(data.itsFunctionType != 0);
    }

    @Test(timeout = 4000)
    public void testCompileAnonymousFunction() {
        InterpreterData data = compileFunction("(function(x) { return x; });", 0);
        assertNotNull(data);
        assertNull(data.itsName);
        assertEquals(1, data.argCount);
    }

    @Test(timeout = 4000)
    public void testNestedFunctions() {
        String js = "function outer() { function inner1() { return 1; } function inner2() { return 2; } return inner1() + inner2(); }";
        InterpreterData data = compileScript(js);
        assertNotNull(data.itsNestedFunctions);
        assertEquals(1, data.itsNestedFunctions.length);
        InterpreterData outerData = data.itsNestedFunctions[0];
        assertNotNull(outerData.itsNestedFunctions);
        assertEquals(2, outerData.itsNestedFunctions.length);
    }

    @Test(timeout = 4000)
    public void testStrictModeScript() {
        InterpreterData data = compileScript("\"use strict\"; var x = 42;", 0);
        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testGeneratorFunctionAndYield() {
        String js = "function myGen() { yield 10; yield 20; return 30; }";
        InterpreterData data = compileFunction(js, Context.VERSION_1_7);
        assertNotNull(data);
        assertEquals("myGen", data.itsName);
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Table Expansions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testNumericLiteralsBoundaries() {
        // Zero, Negative Zero, 1, Short, Int, Double
        String js = "var z = 0; var nz = -0.0; var o = 1; var s = 32000; var i = 100000; var d = 3.14159265;";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
        assertNotNull(data.itsDoubleTable);
        assertTrue(data.itsDoubleTable.length > 0);
    }

    @Test(timeout = 4000)
    public void testDoubleTableExpansion() {
        // Exceed initial double table capacity of 64
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 75; i++) {
            sb.append("var d").append(i).append(" = ").append(i).append(".12345;\n");
        }
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data.itsDoubleTable);
        assertTrue(data.itsDoubleTable.length >= 75);
    }

    @Test(timeout = 4000)
    public void testStringTableAndPrefixEncodings() {
        // Generate > 256 distinct identifiers to exercise REG_STR1 and REG_STR2
        StringBuilder sb = new StringBuilder("var obj = {};\n");
        for (int i = 0; i < 270; i++) {
            sb.append("obj.prop_").append(i).append(" = ").append(i).append(";\n");
        }
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data.itsStringTable);
        assertTrue(data.itsStringTable.length >= 270);
    }

    @Test(timeout = 4000)
    public void testVarIndexThreshold128() {
        // Inside a function, local variables < 128 use specialized opcodes, >= 128 use general opcodes
        StringBuilder sb = new StringBuilder("function wideVars() {\n");
        for (int i = 0; i < 135; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";\n");
        }
        for (int i = 0; i < 135; i++) {
            sb.append("v").append(i).append("++;\n");
            sb.append("v").append(i).append(" = v").append(i).append(" + 1;\n");
        }
        sb.append("}\n");
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data.itsNestedFunctions);
        InterpreterData fnData = data.itsNestedFunctions[0];
        assertTrue(fnData.itsMaxVars >= 135);
    }

    @Test(timeout = 4000)
    public void testLabelAndFixupTableExpansion() {
        // Generate > 40 forward jumps to trigger MIN_FIXUP_TABLE_SIZE (40) expansion
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("if (i == ").append(i).append(") { a = ").append(i).append("; }\n");
        }
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testExceptionTableExpansion() {
        // More than 2 try-catch blocks to expand exception table
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("try { throw ").append(i).append("; } catch (e) { var x").append(i).append(" = e; }\n");
        }
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data.itsExceptionTable);
        assertTrue(data.itsExceptionTable.length >= 5 * Interpreter.EXCEPTION_SLOT_SIZE);
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Many Adds / Deep Recursion)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testManyAdds() {
        // Direct reproduction target for deep binary expression chain (Defects4J StackOverflowError)
        StringBuilder sb = new StringBuilder("var sum = 0");
        for (int i = 1; i <= 1200; i++) {
            sb.append(" + ").append(i);
        }
        sb.append(";");
        InterpreterData data = compileScript(sb.toString());
        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testBadTreeInVisitStatement() {
        AstRoot root = new AstRoot();
        // A GETPROP node placed directly as a statement cannot be visited by visitStatement
        Node badNode = new Node(Token.GETPROP);
        root.addChildToBack(badNode);
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator gen = new CodeGenerator();
        try {
            gen.compile(env, root, "", false);
            fail("Expected RuntimeException from badTree");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains(String.valueOf(Token.GETPROP)));
        }
    }

    @Test(timeout = 4000)
    public void testBadTreeInVisitExpression() {
        AstRoot root = new AstRoot();
        // Construct an EXPR_VOID wrapping an invalid expression node type (e.g. Token.IFEQ)
        Node exprVoid = new Node(Token.EXPR_VOID, new Node(Token.IFEQ));
        root.addChildToBack(exprVoid);
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator gen = new CodeGenerator();
        try {
            gen.compile(env, root, "", false);
            fail("Expected RuntimeException from badTree in visitExpression");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains(String.valueOf(Token.IFEQ)));
        }
    }

    // ================================================================================================
    // Partition E: Control Structures, Expressions, Literals & Calls
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSwitchStatement() {
        String js = "switch (x) { case 1: y = 1; break; case 2: y = 2; break; default: y = 0; }";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testTryCatchFinally() {
        String js = "try { throw new Error('fail'); } catch (e) { y = e; } finally { z = 10; }";
        InterpreterData data = compileScript(js);
        assertNotNull(data.itsExceptionTable);
        assertTrue(data.itsExceptionTable.length > 0);
    }

    @Test(timeout = 4000)
    public void testLoopsAndLabels() {
        String js = "outer: while (true) { for (var i = 0; i < 10; i++) { if (i == 5) continue outer; else break outer; } }";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testForInLoop() {
        String js = "var obj = {a: 1, b: 2}; for (var k in obj) { var val = obj[k]; }";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testWithStatement() {
        String js = "var o = { x: 1 }; with (o) { x = 2; }";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testDebuggerStatement() {
        String js = "var a = 1; debugger; var b = 2;";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testAllBinaryAndUnaryOperators() {
        String js = "var a = 5, b = 2;\n" +
                    "var res = +(a & b | a ^ b << 1 >> 1 >>> 1 + a - b * a / b % 2);\n" +
                    "var cmp = (a == b) && (a != b) || (a === b) && (a !== b) || (a <= b) || (a >= b) || (a < b) || (a > b);\n" +
                    "var un = -a; var bit = ~a; var not = !a; var typ = typeof a; var voi = void a;\n" +
                    "var inInst = (a in {}) || (a instanceof Object);\n";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testTernaryAndLogicalShortCircuit() {
        String js = "var a = true ? 1 : 2; var b = false ? 3 : 4; var c = a && b || false;";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testCompoundAssignments() {
        String js = "var obj = { x: 10, y: [1, 2] };\n" +
                    "obj.x += 5;\n" +
                    "obj.y[0] *= 2;\n";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testIncrementDecrementVariants() {
        String js = "var a = 1; a++; ++a; a--; --a;\n" +
                    "var o = { x: 1 }; o.x++; ++o.x; o.x--; --o.x;\n" +
                    "var arr = [1]; arr[0]++; ++arr[0]; arr[0]--; --arr[0];\n" +
                    "function f() { var v = 1; v++; ++v; v--; --v; }\n";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testDeleteOperators() {
        String js = "var o = { x: 1 }; delete o.x; delete o['x'];";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testArrayAndObjectLiterals() {
        // Normal array, sparse array, object with properties, getters and setters
        String js = "var a1 = [1, 2, 3];\n" +
                    "var a2 = [1, , , 4];\n" +
                    "var o = { p1: 'val', get x() { return 1; }, set x(v) { } };\n";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
        assertNotNull(data.literalIds);
        assertTrue(data.literalIds.length >= 2);
    }

    @Test(timeout = 4000)
    public void testArrayComprehension() {
        String js = "var comp = [x * 2 for (x in [1, 2, 3])];";
        InterpreterData data = compileScript(js, Context.VERSION_1_7);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testFunctionCallsAndTailCall() {
        String js = "function target(x) { return x; }\n" +
                    "function caller() {\n" +
                    "    target(1);\n" +
                    "    target.call(null, 1);\n" +
                    "    new target(1);\n" +
                    "    return target(2);\n" + // Tail call candidate
                    "}\n";
        CompilerEnvirons env = new CompilerEnvirons();
        env.initFromContext(context);
        env.setGenerateDebugInfo(false); // Enable tail call optimization
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(js, "calls.js", 1);
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, root.getEncodedSource(), false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testSpecialCallEval() {
        String js = "eval('1 + 1');";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testCommaExpression() {
        String js = "function testComma() { return (1, 2, 3 + 4); }";
        InterpreterData data = compileScript(js);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testRegExpLiteral() {
        String js = "var re = /abc[0-9]+/gi;";
        InterpreterData data = compileScript(js);
        assertNotNull(data.itsRegExpLiterals);
        assertEquals(1, data.itsRegExpLiterals.length);
    }

    @Test(timeout = 4000)
    public void testFunctionRequiresActivationDueToEval() {
        String js = "function act(param) { eval('param = 2'); return typeof param; }";
        InterpreterData data = compileFunction(js, 0);
        assertTrue(data.itsNeedsActivation);
    }
}