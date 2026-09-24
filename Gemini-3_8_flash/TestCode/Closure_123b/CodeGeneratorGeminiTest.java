package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.CodeGenerator
 *
 * 1. Number Encoding Branches:
 *    - inum == 0, positive zero (+0.0) -> Icode_ZERO
 *    - inum == 0, negative zero (-0.0) -> Icode_ZERO + Token.NEG (1.0 / num < 0.0)
 *    - inum == 1 -> Icode_ONE
 *    - (short)inum == inum -> Icode_SHORTNUMBER + 2-byte operand
 *    - int number -> Icode_INTNUMBER + 4-byte operand
 *    - non-integer double -> Token.NUMBER + index into double table
 *
 * 2. Control Flow & Jumps:
 *    - SWITCH statement: DUP, SHEQ, IFEQ_POP to case target, POP for default
 *    - IFEQ / IFNE conditional branches, GOTO, TARGET
 *    - TRY / CATCH / FINALLY: exception table construction, SCOPE_SAVE, LOCAL_CLEAR, STARTSUB/RETSUB
 *    - BREAK / CONTINUE labels & fixLabelGotos
 *
 * 3. Expressions & Operators:
 *    - Logical AND (&&), OR (||) short-circuit jumps and forward goto resolution
 *    - Conditional HOOK (?:)
 *    - Property ops: GETPROP, SETPROP, SETPROP_OP (e.g. +=)
 *    - Element ops: GETELEM, SETELEM, SETELEM_OP
 *    - Inc/Dec ops on var, name, getprop, getelem
 *    - COMMA operator
 *    - Special calls (eval, super, constructor) and tail-call optimization criteria
 *    - Literals: Object literal with get/set/value, Array literal with sparse/skip indexes
 *
 * 4. Scopes & Functions:
 *    - Script compilation (returnFunction = false) vs Function compilation (returnFunction = true)
 *    - Nested functions and closures
 *    - Generator function compilation (Icode_GENERATOR, Icode_GENERATOR_END, YIELD)
 *    - RegExp literals compilation
 *
 * 5. Defect Targeted Zone:
 *    - For-loop condition containing 'in' operator inside grouping/expressions:
 *      e.g., `for(a=c?0:[(0 in d)];;)foo();`
 */
public class CodeGeneratorGeminiTest {

    private InterpreterData compileScript(String source) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(Context.VERSION_1_8);
        env.setGenerateDebugInfo(true);
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(source, "test.js", 1);
        CodeGenerator cg = new CodeGenerator();
        return cg.compile(env, root, source, false);
    }

    private InterpreterData compileFunction(String source) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(Context.VERSION_1_8);
        env.setGenerateDebugInfo(false);
        Parser parser = new Parser(env);
        AstRoot root = parser.parse(source, "testFn.js", 1);
        CodeGenerator cg = new CodeGenerator();
        return cg.compile(env, root, source, true);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompileSimpleScript() {
        InterpreterData idata = compileScript("var x = 1; var y = x + 2;");
        assertNotNull(idata);
        assertTrue(idata.topLevel);
        assertEquals("test.js", idata.getSourceName());
        assertTrue(idata.itsICode.length > 0);
        assertTrue(idata.itsMaxVars >= 2);
    }

    @Test(timeout = 4000)
    public void testCompileFunctionReturn() {
        InterpreterData idata = compileFunction("function add(a, b) { return a + b; }");
        assertNotNull(idata);
        assertTrue(idata.topLevel);
        assertEquals("add", idata.getFunctionName());
        assertEquals(2, idata.getParamCount());
    }

    @Test(timeout = 4000)
    public void testCompileNestedFunctions() {
        String js = "function outer() { function inner() { return 42; } return inner(); }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertNotNull(idata.itsNestedFunctions);
        assertEquals(1, idata.itsNestedFunctions.length);
        InterpreterData innerData = idata.itsNestedFunctions[0];
        assertNotNull(innerData);
    }

    @Test(timeout = 4000)
    public void testCompileRegExpLiterals() {
        Context cx = Context.enter();
        try {
            InterpreterData idata = compileScript("var re = /abc/g; var re2 = /def/i;");
            assertNotNull(idata);
            assertNotNull(idata.itsRegExpLiterals);
            assertEquals(2, idata.itsRegExpLiterals.length);
        } finally {
            Context.exit();
        }
    }

    @Test(timeout = 4000)
    public void testCompileSwitchStatement() {
        String js = "var x = 2; switch (x) { case 1: x = 10; break; case 2: x = 20; break; default: x = 0; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertTrue(idata.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileTryCatchFinally() {
        String js = "try { throw 1; } catch (e) { var k = e; } finally { var f = 2; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertNotNull(idata.itsExceptionTable);
        assertTrue(idata.itsExceptionTable.length >= Interpreter.EXCEPTION_SLOT_SIZE);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Number Literals
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompileNumberZeroPositiveAndNegative() {
        InterpreterData idataPos = compileScript("var zero = 0;");
        assertNotNull(idataPos);

        InterpreterData idataNeg = compileScript("var negZero = -0;");
        assertNotNull(idataNeg);
    }

    @Test(timeout = 4000)
    public void testCompileNumberOne() {
        InterpreterData idata = compileScript("var one = 1;");
        assertNotNull(idata);
        assertTrue(idata.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileNumberShortBoundary() {
        InterpreterData idataShort = compileScript("var s1 = 32767; var s2 = -32768; var mid = 250;");
        assertNotNull(idataShort);
    }

    @Test(timeout = 4000)
    public void testCompileNumberIntBoundary() {
        InterpreterData idataInt = compileScript("var largeInt = 70000; var minInt = -100000;");
        assertNotNull(idataInt);
    }

    @Test(timeout = 4000)
    public void testCompileNumberDouble() {
        InterpreterData idata = compileScript("var pi = 3.141592653589793; var fraction = 0.5;");
        assertNotNull(idata);
        assertNotNull(idata.itsDoubleTable);
        assertTrue(idata.itsDoubleTable.length >= 2);
    }

    @Test(timeout = 4000)
    public void testEmptyScript() {
        InterpreterData idata = compileScript("");
        assertNotNull(idata);
        assertEquals(0, idata.itsMaxVars);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (For-In Operator & Precedence)
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrintInOperatorInForLoop() {
        // Targets known defect where in-operator within conditional in for-loop initializer is evaluated
        String js = "var c = false, d = {0: 'val'}, a; for (a = c ? 0 : [(0 in d)];;) { break; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertTrue(idata.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testInOperatorInConditions() {
        String js = "var obj = {x: 1}; if ('x' in obj) { var found = true; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    // =========================================================================
    // Partition D: Operations, Assignments, and Complex Expressions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompileLogicalAndOrHook() {
        String js = "var a = 1, b = 2; var c = (a && b) || 3; var d = a ? b : 4;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertTrue(idata.itsMaxStack >= 1);
    }

    @Test(timeout = 4000)
    public void testCompileIncDecOps() {
        String js = "var i = 0; var obj = {k: 1}; var arr = [2];" +
                    "i++; ++i; i--; --i;" +
                    "obj.k++; ++obj.k; obj.k--; --obj.k;" +
                    "arr[0]++; ++arr[0]; arr[0]--; --arr[0];";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileCompoundAssignments() {
        String js = "var obj = {k: 1}; obj.k += 5; obj['k'] *= 2;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileDeletePropertyAndName() {
        String js = "var obj = {k: 1}; delete obj.k; delete obj['k']; delete x;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileObjectLiteralWithGettersAndSetters() {
        String js = "var o = { a: 1, get b() { return 2; }, set b(v) { this.a = v; } };";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertNotNull(idata.literalIds);
    }

    @Test(timeout = 4000)
    public void testCompileSparseArrayLiteral() {
        String js = "var arr = [1, , 3, , 5];";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertNotNull(idata.literalIds);
    }

    @Test(timeout = 4000)
    public void testCompileUnaryOperators() {
        String js = "var a = 1; var b = +a; var c = -a; var d = !a; var e = ~a; var f = typeof a; var g = void a;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileBinaryComparisons() {
        String js = "var a = 1, b = 2;" +
                    "var r1 = a == b, r2 = a != b, r3 = a === b, r4 = a !== b;" +
                    "var r5 = a < b, r6 = a <= b, r7 = a > b, r8 = a >= b;" +
                    "var r9 = a instanceof Object;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileBitwiseAndShiftOperators() {
        String js = "var a = 1 & 2 | 3 ^ 4; var b = (a << 1) >> 2 >>> 3;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileWithStatement() {
        String js = "var obj = {x: 10}; with (obj) { var y = x + 1; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileWhileAndForLoops() {
        String js = "var s = 0; for (var i = 0; i < 10; i++) { if (i === 5) continue; s += i; } " +
                    "while (s > 0) { s--; if (s === 2) break; } " +
                    "do { s++; } while (s < 5);";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileForInLoop() {
        String js = "var obj = {a: 1, b: 2}; for (var k in obj) { var v = obj[k]; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
    }

    // =========================================================================
    // Partition E: Generator Functions & Advanced Execution Modes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompileGeneratorFunction() {
        String js = "function* gen() { yield 1; yield 2; return 3; }";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertNotNull(idata.itsNestedFunctions);
        InterpreterData genData = idata.itsNestedFunctions[0];
        assertNotNull(genData);
    }

    @Test(timeout = 4000)
    public void testTailCallOptimizationFlag() {
        // Debug info disabled triggers tail call optimization path if eligible
        String js = "function recurse(n) { if (n <= 0) return 0; return recurse(n - 1); }";
        InterpreterData idata = compileFunction(js);
        assertNotNull(idata);
    }

    @Test(timeout = 4000)
    public void testCompileDebuggerStatement() {
        String js = "debugger;";
        InterpreterData idata = compileScript(js);
        assertNotNull(idata);
        assertTrue(idata.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testBadTreeNodeThrowsException() {
        CodeGenerator cg = new CodeGenerator();
        Node invalidNode = new Node(Token.ERROR);
        try {
            CompilerEnvirons env = new CompilerEnvirons();
            AstRoot root = new AstRoot();
            root.addChildToBack(invalidNode);
            cg.compile(env, root, "", false);
            fail("Expected RuntimeException for invalid AST node");
        } catch (RuntimeException expected) {
            assertNotNull(expected);
        }
    }
}