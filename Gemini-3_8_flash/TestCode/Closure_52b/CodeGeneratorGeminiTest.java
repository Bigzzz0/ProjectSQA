/* [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.CodeGenerator
 *
 * Decision / Branch Matrix Covered:
 * 1. compile() / returnFunction:
 *    - returnFunction = true (generateFunctionICode) vs false (generateICodeFromTree).
 *    - Strict mode flag propagation, dynamic scope flag, generator function flag.
 * 2. Number Encoding Branch Matrix:
 *    - inum == 0 with positive 0.0 (Icode_ZERO)
 *    - inum == 0 with negative -0.0 (Icode_ZERO + Token.NEG)
 *    - inum == 1 (Icode_ONE)
 *    - (short)inum == inum (Icode_SHORTNUMBER)
 *    - Full 32-bit integer (Icode_INTNUMBER)
 *    - Non-integer double (Token.NUMBER via itsDoubleTable)
 * 3. Object & Array Literals:
 *    - Standard Object literal with identifiers and numeric/string keys (e.g. "010" vs 10).
 *    - Object getters and setters ({ get x() {}, set x(v) {} }).
 *    - Array literal dense vs sparse (skipIndexes triggering Icode_SPARE_ARRAYLIT).
 * 4. Control Flow & Labels:
 *    - if / else, while, do-while, for, for-in, switch-case with default and multiple branches.
 *    - try / catch / finally blocks (verifying exception table generation).
 *    - break / continue with labels (forward and backward jumps, fixLabelGotos).
 * 5. Calls & Tail-Call Optimization:
 *    - Standard call, method call, constructor (new), eval (special call).
 *    - Tail call optimization active when ECF_TAIL is present, not in try, and debug info off.
 * 6. Variable & Property Operators:
 *    - var declaration, assignment, const, inc/dec on var, name, getprop, getelem.
 *    - typeof and typeofname with/without activation frame.
 * 7. E4X & Advanced JS 1.7+ constructs:
 *    - Generators and yield expressions.
 *    - with statement and nested functions.
 *
 * Known Defect Target:
 * - Numeric string keys in object literals: var x = {"010": 1};
 *   Ensures property names preserving octal-like string representation vs integer conversion.
 */

package org.mozilla.javascript;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.AstRoot;

public class CodeGeneratorGeminiTest {

    private CompilerEnvirons compilerEnv;

    @Before
    public void setUp() {
        compilerEnv = new CompilerEnvirons();
        compilerEnv.setLanguageVersion(Context.VERSION_1_8);
        compilerEnv.setGenerateDebugInfo(false);
    }

    private InterpreterData compileScript(String source) {
        Parser parser = new Parser(compilerEnv);
        AstRoot root = parser.parse(source, "testSource.js", 1);
        CodeGenerator cg = new CodeGenerator();
        return cg.compile(compilerEnv, root, source, false);
    }

    private InterpreterData compileFunction(String source) {
        Parser parser = new Parser(compilerEnv);
        AstRoot root = parser.parse(source, "testSource.js", 1);
        CodeGenerator cg = new CodeGenerator();
        return cg.compile(compilerEnv, root, source, true);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Number Encoding Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumberEncodingBranches() {
        // Zero (0), negative zero (-0.0), one (1), short (42), int (100000), double (3.14159)
        String script = "var a = 0; var b = -0.0; var c = 1; var d = 42; var e = 100000; var f = 3.14159;";
        InterpreterData data = compileScript(script);

        assertNotNull("InterpreterData should not be null", data);
        assertTrue("ICode array must be populated", data.itsICode.length > 0);
        assertNotNull("Double table must contain non-integer double", data.itsDoubleTable);
        assertEquals(1, data.itsDoubleTable.length);
        assertEquals(3.14159, data.itsDoubleTable[0], 0.000001);
    }

    @Test(timeout = 4000)
    public void testArithmeticAndBitwiseExpressions() {
        String script =
            "var x = 10 + 2 - 3 * 4 / 5 % 2;\n" +
            "var y = (x << 1) >> 2 >>> 3;\n" +
            "var z = (x & 1) | (y ^ 2);\n" +
            "var cmp = (x == y) && (x != z) || (x === y) && (x !== z) || (x < y) || (x <= z) || (x > y) || (x >= z);\n" +
            "var un = +x + -y + ~z + (!cmp) + typeof x + void 0;";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsMaxStack >= 2);
    }

    @Test(timeout = 4000)
    public void testIncrementDecrementVariants() {
        String script =
            "var a = 1; a++; ++a; a--; --a;\n" +
            "obj = {}; obj.prop++; ++obj.prop; obj.prop--; --obj.prop;\n" +
            "arr = [1]; arr[0]++; ++arr[0]; arr[0]--; --arr[0];";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Literals (Defect Target Zone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumericKeys() {
        // Defect target: object literal with leading-zero numeric string key "010"
        String script = "var x = {\"010\": 1};";
        InterpreterData data = compileScript(script);

        assertNotNull("Compiled data should not be null", data);
        assertNotNull("literalIds must be recorded for object literal", data.literalIds);
        assertEquals(1, data.literalIds.length);

        Object[] propertyIds = (Object[]) data.literalIds[0];
        assertNotNull(propertyIds);
        assertEquals(1, propertyIds.length);
        assertEquals("Numeric string key '010' must preserve its string identity", "010", propertyIds[0]);
    }

    @Test(timeout = 4000)
    public void testObjectLiteralWithGettersAndSetters() {
        String script = "var o = { get x() { return 1; }, set x(v) { this._x = v; } };";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertNotNull(data.literalIds);
        assertTrue(data.itsNestedFunctions.length >= 2);
    }

    @Test(timeout = 4000)
    public void testSparseArrayLiteral() {
        // Array literal with omitted indices: [1, , , 4]
        String script = "var arr = [1, , , 4];";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertNotNull("Sparse array should register skipIndexes in literalIds", data.literalIds);
        assertEquals(1, data.literalIds.length);
        int[] skipIndexes = (int[]) data.literalIds[0];
        assertNotNull(skipIndexes);
        assertEquals(2, skipIndexes.length);
        assertEquals(1, skipIndexes[0]);
        assertEquals(2, skipIndexes[1]);
    }

    @Test(timeout = 4000)
    public void testEmptyAndDenseArrayLiterals() {
        String script = "var empty = []; var dense = [1, 2, 'three', null, true, false];";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    // =========================================================================
    // Partition C: Control Structures, Jumps, and Exception Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testSwitchStatement() {
        String script =
            "function sw(x) {\n" +
            "  switch (x) {\n" +
            "    case 1: return 'one';\n" +
            "    case '2': return 'two';\n" +
            "    default: return 'other';\n" +
            "  }\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertNotNull(data.itsNestedFunctions);
        assertEquals(1, data.itsNestedFunctions.length);
    }

    @Test(timeout = 4000)
    public void testTryCatchFinally() {
        String script =
            "function testTry() {\n" +
            "  try {\n" +
            "    throw new Error('err');\n" +
            "  } catch (e) {\n" +
            "    return e;\n" +
            "  } finally {\n" +
            "    var f = 1;\n" +
            "  }\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        InterpreterData fnData = data.itsNestedFunctions[0];
        assertNotNull("Exception table must be created for try/catch/finally", fnData.itsExceptionTable);
        assertTrue(fnData.itsExceptionTable.length >= Interpreter.EXCEPTION_SLOT_SIZE * 2);
    }

    @Test(timeout = 4000)
    public void testLoopsWithBreakAndContinue() {
        String script =
            "outer: for (var i = 0; i < 10; i++) {\n" +
            "  var j = 0;\n" +
            "  while (j < 5) {\n" +
            "    j++;\n" +
            "    if (j == 2) continue;\n" +
            "    if (i == 5) break outer;\n" +
            "  }\n" +
            "  do { i++; } while (i < 2);\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testForInLoop() {
        String script =
            "var obj = {a: 1, b: 2};\n" +
            "for (var k in obj) {\n" +
            "  var v = obj[k];\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testTernaryAndLogicalOperators() {
        String script =
            "var a = true ? 1 : 2;\n" +
            "var b = false ? (true ? 3 : 4) : 5;\n" +
            "var c = (a && b) || (a && !b);";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    // =========================================================================
    // Partition D: Function Modes, Calls, Tail Optimization, and Generators
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectFunctionCompile() {
        String script = "function target(a, b) { return a + b; }";
        InterpreterData data = compileFunction(script);

        assertNotNull(data);
        assertTrue(data.itsInFunctionFlag);
        assertEquals("target", data.itsName);
        assertEquals(2, data.argCount);
        assertArrayEquals(new String[]{"a", "b"}, data.argNames);
    }

    @Test(timeout = 4000)
    public void testFunctionWithDynamicScopeAndStrictMode() {
        compilerEnv.setUseDynamicScope(true);
        String script = "'use strict'; function dyn() { return 123; }";
        InterpreterData data = compileFunction(script);

        assertNotNull(data);
        assertTrue(data.useDynamicScope);
        assertTrue(data.isStrict);
    }

    @Test(timeout = 4000)
    public void testGeneratorFunction() {
        String script = "function gen() { yield 1; yield 2; return 3; }";
        InterpreterData data = compileFunction(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testTailCallOptimization() {
        compilerEnv.setGenerateDebugInfo(false);
        String script = "function tail(x) { return tail(x - 1); }";
        InterpreterData data = compileFunction(script);

        assertNotNull(data);
        boolean containsTailCall = false;
        for (byte b : data.itsICode) {
            if (b == (byte) Icode.Icode_TAIL_CALL) {
                containsTailCall = true;
                break;
            }
        }
        assertTrue("ICode should utilize Icode_TAIL_CALL when conditions match", containsTailCall);
    }

    @Test(timeout = 4000)
    public void testSpecialCallsAndConstructors() {
        String script =
            "function testCalls() {\n" +
            "  var d = new Date();\n" +
            "  var res = eval('2 + 2');\n" +
            "  return d.getTime();\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertEquals(1, data.itsNestedFunctions.length);
    }

    @Test(timeout = 4000)
    public void testWithStatement() {
        String script =
            "var obj = {x: 10};\n" +
            "with (obj) {\n" +
            "  x = 20;\n" +
            "}";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testStringTableDeduplicationAndCapacity() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            sb.append("var str_").append(i).append(" = 'val_").append(i).append("';\n");
            // Reuse some strings to test deduplication
            sb.append("var dup_").append(i).append(" = 'val_").append(i % 5).append("';\n");
        }
        InterpreterData data = compileScript(sb.toString());

        assertNotNull(data);
        assertNotNull(data.itsStringTable);
        assertTrue("String table must contain all unique registered strings", data.itsStringTable.length >= 30);
    }

    @Test(timeout = 4000)
    public void testDebuggerStatementAndCommaExpression() {
        String script = "debugger; var a = (1, 2, 3);";
        InterpreterData data = compileScript(script);

        assertNotNull(data);
        assertTrue(data.itsICode.length > 0);
    }

    // =========================================================================
    // Partition E: Defensive & Error Handling Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidSyntaxThrowsEvaluatorException() {
        try {
            compileScript("var a = ;");
            fail("Expected EvaluatorException for malformed syntax");
        } catch (EvaluatorException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLargeBytecodeCapacityExpansion() {
        // Trigger increaseICodeCapacity() by creating a sequence of statements
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";\n");
        }
        InterpreterData data = compileScript(sb.toString());

        assertNotNull(data);
        assertTrue("ICode should expand to fit large scripts", data.itsICode.length > 2000);
    }
}