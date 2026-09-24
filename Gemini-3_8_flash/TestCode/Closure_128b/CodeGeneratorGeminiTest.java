package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Jump;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: org.mozilla.javascript.CodeGenerator
 *
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - compile(..., returnFunction = false): Script bytecode generation, RETURN_RESULT
 *    - compile(..., returnFunction = true): Function bytecode generation, argument handling
 *    - generateNestedFunctions: Zero vs multiple nested closures
 *    - generateRegExpLiterals: Zero vs multiple regex patterns with flags
 *    - Line number updates: firstLinePC tracking and uint16 encoding
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Numeric literal boundaries: 0 (ZERO), -0.0 (ZERO + NEG), 1 (ONE),
 *      short int (-32768 to 32767), standard 32-bit int, and IEEE-754 double table
 *    - Variable indexing thresholds: < 128 (GETVAR1/SETVAR1/SETCONSTVAR1) vs >= 128 (GETVAR/SETVAR/SETCONSTVAR)
 *    - String & Index pool prefixes: < 4 / < 6 (REG_C0), <= 0xFF (REG1), <= 0xFFFF (REG2), > 0xFFFF (REG4)
 *    - Label and fixup table expansion: > MIN_LABEL_TABLE_SIZE (32), > MIN_FIXUP_TABLE_SIZE (40)
 *    - Double and Exception table dynamic array growth (> 64 doubles, > 2 exception slots)
 *    - Long jump threshold: Offset > Short.MAX_VALUE (32767) triggering UintMap longJumps
 *
 * 3. Partition C: Defect-Targeted Branch Zone (Issue 942 / Object Literal Keys)
 *    - testIssue942: Numeric keys in object literal vs string keys `{0: 1}` vs `{"0": 1}`
 *    - Spare array literals with skip indexes vs dense array literals
 *    - Object literal getters and setters (Icode_LITERAL_GETTER / SETTER)
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - badTree in visitStatement (unrecognized AST statement node)
 *    - badTree in visitExpression (unrecognized expression token)
 *    - badTree in visitIncDec (invalid child operand)
 *    - badTree in SWITCH (child is not Token.CASE)
 *    - Generator functions: Icode_GENERATOR, Icode_GENERATOR_END, YIELD
 *
 * 5. Partition E: Control Flow & Contextual Optimization
 *    - Tail call optimization: enabled (!debug && !inTry) vs disabled (debug || inTry)
 *    - Special calls: eval (SPECIALCALL_EVAL) and constructor new eval
 *    - Short-circuiting operators: Token.AND, Token.OR, Token.HOOK with tail flags
 *    - Compound assignment & reference operations: SETPROP_OP, SETELEM_OP, DELPROP
 *    - Try-catch-finally, JSR/STARTSUB/RETSUB, and CATCH_SCOPE local allocation
 * -----------------------------------------------------------------------------------------
 */
public class CodeGeneratorGeminiTest {

    private InterpreterData compile(String source, boolean returnFunction, boolean debug) {
        Context cx = Context.enter();
        try {
            CompilerEnvirons env = new CompilerEnvirons();
            env.setLanguageVersion(Context.VERSION_1_8);
            env.setGenerateDebugInfo(debug);
            Parser parser = new Parser(env);
            AstRoot root = parser.parse(source, "test.js", 1);
            CodeGenerator gen = new CodeGenerator();
            return gen.compile(env, root, root.getEncodedSource(), returnFunction);
        } finally {
            Context.exit();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicScriptCompilation() {
        InterpreterData data = compile("var x = 10; x + 5;", false, false);
        assertNotNull(data);
        assertTrue(data.topLevel);
        assertEquals(0, data.itsFunctionType);
        assertNotNull(data.itsICode);
        assertTrue(data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testFunctionCompilationDirect() {
        InterpreterData data = compile("function sum(a, b) { return a + b; }", true, false);
        assertNotNull(data);
        assertEquals("sum", data.itsName);
        assertEquals(2, data.argCount);
        assertNotNull(data.argNames);
        assertEquals("a", data.argNames[0]);
        assertEquals("b", data.argNames[1]);
    }

    @Test(timeout = 4000)
    public void testNestedFunctions() {
        String script = "function outer() {\n" +
                        "  function inner1() { return 1; }\n" +
                        "  function inner2() { return 2; }\n" +
                        "  return inner1() + inner2();\n" +
                        "}";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data.itsNestedFunctions);
        assertEquals(1, data.itsNestedFunctions.length);
        InterpreterData outerData = data.itsNestedFunctions[0];
        assertNotNull(outerData.itsNestedFunctions);
        assertEquals(2, outerData.itsNestedFunctions.length);
    }

    @Test(timeout = 4000)
    public void testRegExpLiterals() {
        InterpreterData data = compile("var r1 = /abc/g; var r2 = /xyz/i;", false, false);
        assertNotNull(data.itsRegExpLiterals);
        assertEquals(2, data.itsRegExpLiterals.length);
    }

    @Test(timeout = 4000)
    public void testLineNumberUpdates() {
        String script = "\n\nvar x = 1;\nvar y = 2;\n";
        InterpreterData data = compile(script, false, false);
        assertEquals(3, data.firstLinePC);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumberLiteralsBoundaries() {
        String script = "var n0 = 0;\n" +
                        "var nNeg0 = -0.0;\n" +
                        "var n1 = 1;\n" +
                        "var nShort = 32000;\n" +
                        "var nInt = 100000;\n" +
                        "var nDouble = 3.141592653589793;\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data.itsDoubleTable);
        assertTrue(data.itsDoubleTable.length >= 1);
        assertEquals(3.141592653589793, data.itsDoubleTable[0], 0.000000001);
    }

    @Test(timeout = 4000)
    public void testDoubleTableExpansion() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            sb.append("var d").append(i).append(" = ").append(i).append(".1234567;\n");
        }
        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data.itsDoubleTable);
        assertTrue(data.itsDoubleTable.length >= 70);
    }

    @Test(timeout = 4000)
    public void testVariablesBoundaryThreshold128() {
        StringBuilder sb = new StringBuilder();
        sb.append("function testLotsOfVars() {\n");
        for (int i = 0; i < 135; i++) {
            sb.append("  var v").append(i).append(" = ").append(i).append(";\n");
        }
        sb.append("  const c130 = 999;\n");
        sb.append("  v127 = 1; v128 = 2; v134 = 3;\n");
        sb.append("  return v0 + v127 + v128 + v134 + c130;\n");
        sb.append("}\n");

        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data.itsNestedFunctions);
        InterpreterData fnData = data.itsNestedFunctions[0];
        assertTrue(fnData.itsMaxVars >= 135);
    }

    @Test(timeout = 4000)
    public void testStringTableAndIndexOpExpansion() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 265; i++) {
            sb.append("var prop_").append(i).append(" = \"str_val_").append(i).append("\";\n");
        }
        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data.itsStringTable);
        assertTrue(data.itsStringTable.length >= 265);
    }

    @Test(timeout = 4000)
    public void testLabelAndFixupTableExpansion() {
        StringBuilder sb = new StringBuilder();
        sb.append("var x = 0;\n");
        sb.append("while (true) {\n");
        for (int i = 0; i < 50; i++) {
            sb.append("  if (x === ").append(i).append(") break;\n");
        }
        sb.append("  break;\n");
        sb.append("}\n");

        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testExceptionTableExpansion() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append("try { throw ").append(i).append("; } catch(e) { x = e; } finally { y = 0; }\n");
        }
        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data.itsExceptionTable);
        assertTrue(data.itsExceptionTable.length >= 6 * Interpreter.EXCEPTION_SLOT_SIZE);
    }

    @Test(timeout = 4000)
    public void testLongJumpBranch() {
        StringBuilder sb = new StringBuilder();
        sb.append("var x = 0;\n");
        sb.append("if (x === 0) {\n");
        for (int i = 0; i < 6000; i++) {
            sb.append("  x = ").append(i).append(";\n");
        }
        sb.append("}\n");

        InterpreterData data = compile(sb.toString(), false, false);
        assertNotNull(data);
        assertNotNull(data.longJumps);
        assertTrue(data.longJumps.size() > 0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue 942 / Literals)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIssue942() {
        // Targets numeric key handling in object literals vs string literals: {0: 1} vs {"0": 1}
        String srcNumeric = "var x = {0: 1};";
        InterpreterData dataNum = compile(srcNumeric, false, false);
        assertNotNull(dataNum);
        assertNotNull(dataNum.literalIds);
        assertEquals(1, dataNum.literalIds.length);
        Object[] idsNum = (Object[]) dataNum.literalIds[0];
        assertEquals(1, idsNum.length);

        String srcString = "var x = {\"0\": 1};";
        InterpreterData dataStr = compile(srcString, false, false);
        assertNotNull(dataStr);
        assertNotNull(dataStr.literalIds);
        assertEquals(1, dataStr.literalIds.length);
        Object[] idsStr = (Object[]) dataStr.literalIds[0];
        assertEquals(1, idsStr.length);

        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            Object resNum = cx.evaluateString(scope, "var x = {0: 1}; x[0];", "test.js", 1, null);
            assertEquals(1, ((Number) resNum).intValue());
            Object resStr = cx.evaluateString(scope, "var x = {\"0\": 1}; x[\"0\"];", "test.js", 1, null);
            assertEquals(1, ((Number) resStr).intValue());
        } finally {
            Context.exit();
        }
    }

    @Test(timeout = 4000)
    public void testArrayLiteralsDenseAndSparse() {
        String dense = "var arr = [1, 2, 3];";
        InterpreterData dataDense = compile(dense, false, false);
        assertNotNull(dataDense);

        String sparse = "var arr = [1, , , 4];";
        InterpreterData dataSparse = compile(sparse, false, false);
        assertNotNull(dataSparse);
        assertNotNull(dataSparse.literalIds);
        assertEquals(1, dataSparse.literalIds.length);
        assertTrue(dataSparse.literalIds[0] instanceof int[]);
    }

    @Test(timeout = 4000)
    public void testObjectLiteralGetterSetter() {
        String script = "var o = {\n" +
                        "  val: 42,\n" +
                        "  get x() { return this.val; },\n" +
                        "  set x(v) { this.val = v; }\n" +
                        "};";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
        assertNotNull(data.literalIds);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBadTreeInVisitStatement() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(new Node(Token.SUB));
        CodeGenerator gen = new CodeGenerator();
        gen.compile(env, root, null, false);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBadTreeInVisitExpression() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        Node exprStmt = new Node(Token.EXPR_VOID, new Node(Token.ERROR));
        root.addChildToBack(exprStmt);
        CodeGenerator gen = new CodeGenerator();
        gen.compile(env, root, null, false);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBadTreeInVisitIncDec() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        Node incNode = new Node(Token.INC, new Node(Token.NUMBER));
        incNode.putIntProp(Node.INCRDECR_PROP, 0);
        Node exprStmt = new Node(Token.EXPR_VOID, incNode);
        root.addChildToBack(exprStmt);
        CodeGenerator gen = new CodeGenerator();
        gen.compile(env, root, null, false);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBadTreeInSwitch() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        Jump switchNode = new Jump(Token.SWITCH);
        switchNode.addChildToBack(new Node(Token.NUMBER));
        switchNode.addChildToBack(new Jump(Token.EXPR_VOID));
        root.addChildToBack(switchNode);
        CodeGenerator gen = new CodeGenerator();
        gen.compile(env, root, null, false);
    }

    @Test(timeout = 4000)
    public void testGeneratorAndYield() {
        String script = "function gen() {\n" +
                        "  yield 1;\n" +
                        "  yield;\n" +
                        "  return 2;\n" +
                        "}";
        InterpreterData data = compile(script, true, false);
        assertNotNull(data);
    }

    // =========================================================================
    // Partition E: Control Flow & Contextual Optimization
    // =========================================================================

    @Test(timeout = 4000)
    public void testTailCallOptimizationConditions() {
        String script = "function testTail() {\n" +
                        "  return other(1, 2);\n" +
                        "}";
        InterpreterData dataOpt = compile(script, true, false);
        assertNotNull(dataOpt);

        InterpreterData dataDebug = compile(script, true, true);
        assertNotNull(dataDebug);

        String tryScript = "function testTryTail() {\n" +
                           "  try {\n" +
                           "    return other(1, 2);\n" +
                           "  } catch(e) {}\n" +
                           "}";
        InterpreterData dataTry = compile(tryScript, true, false);
        assertNotNull(dataTry);
    }

    @Test(timeout = 4000)
    public void testSpecialCalls() {
        String script = "var r1 = eval('1 + 1');\n" +
                        "var r2 = new eval('2 + 2');\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testCompoundAssignmentsAndDeletions() {
        String script = "var obj = { a: 1, b: 2 };\n" +
                        "var k = 'a';\n" +
                        "obj.a += 10;\n" +
                        "obj[k] *= 5;\n" +
                        "delete obj.a;\n" +
                        "delete obj[k];\n" +
                        "delete globalName;\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testIncrementDecrementVariants() {
        String script = "function incDec(x) {\n" +
                        "  x++; ++x; x--; --x;\n" +
                        "  var o = { p: 1 };\n" +
                        "  o.p++; ++o.p; o.p--; --o.p;\n" +
                        "  var k = 'p';\n" +
                        "  o[k]++; ++o[k]; o[k]--; --o[k];\n" +
                        "  eval('');\n" +
                        "  var v = 1;\n" +
                        "  v++; ++v; v--; --v;\n" +
                        "}";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testShortCircuitAndHookWithTailCalls() {
        String script = "function logic(a, b, c) {\n" +
                        "  var x = a && b && c;\n" +
                        "  var y = a || b || c;\n" +
                        "  return a ? b() : c();\n" +
                        "}";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testControlFlowStatements() {
        String script = "var i = 0;\n" +
                        "label1: while (i < 10) {\n" +
                        "  i++;\n" +
                        "  if (i === 2) continue label1;\n" +
                        "  if (i === 8) break label1;\n" +
                        "}\n" +
                        "do { i--; } while (i > 0);\n" +
                        "for (var j = 0; j < 5; j++) {}\n" +
                        "switch (i) {\n" +
                        "  case 0: break;\n" +
                        "  case 1: break;\n" +
                        "  default: break;\n" +
                        "}\n" +
                        "debugger;\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testBinaryAndUnaryOperators() {
        String script = "var a = 10, b = 3;\n" +
                        "var r1 = a + b - a * b / a % b;\n" +
                        "var r2 = a & b | a ^ b;\n" +
                        "var r3 = a << 1 >> 1 >>> 1;\n" +
                        "var r4 = (a == b) && (a != b) && (a === b) && (a !== b);\n" +
                        "var r5 = (a < b) || (a <= b) || (a > b) || (a >= b);\n" +
                        "var r6 = (a in {}) || (a instanceof Object);\n" +
                        "var r7 = +a + -b + !a + ~b + typeof a + void a;\n" +
                        "var r8 = (1, 2, 3);\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testFunctionExpressionStatementsAndClosures() {
        String script = "(function() { return 1; });\n" +
                        "(function named() { return 2; });\n" +
                        "var f = function expr(x) { return x; };\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testForInAndForEachIteration() {
        String script = "var obj = { a: 1, b: 2 };\n" +
                        "for (var k in obj) { var v = obj[k]; }\n" +
                        "for each (var val in obj) { var v2 = val; }\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testWithAndLocalBlock() {
        String script = "var o = { x: 10 };\n" +
                        "with (o) {\n" +
                        "  var y = x + 1;\n" +
                        "}\n" +
                        "let (z = 20) {\n" +
                        "  var w = z * 2;\n" +
                        "}\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testStrictSetname() {
        String script = "\"use strict\";\n" +
                        "function strictFn() {\n" +
                        "  var s = 1;\n" +
                        "  return s;\n" +
                        "}\n";
        InterpreterData data = compile(script, false, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testE4xXmlNodesCompilation() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        Node escAttr = new Node(Token.ESCXMLATTR, new Node(Token.TRUE));
        root.addChildToBack(new Node(Token.EXPR_VOID, escAttr));
        Node escText = new Node(Token.ESCXMLTEXT, new Node(Token.FALSE));
        root.addChildToBack(new Node(Token.EXPR_VOID, escText));
        Node defNs = new Node(Token.DEFAULTNAMESPACE, new Node(Token.NULL));
        root.addChildToBack(new Node(Token.EXPR_VOID, defNs));

        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        assertNotNull(data);
    }
}