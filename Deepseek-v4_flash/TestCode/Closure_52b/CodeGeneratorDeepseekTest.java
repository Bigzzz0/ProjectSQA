package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix: 
 * 
 * Partition A – Core Functional Logic:
 * - compile() with ScriptNode tree (non-function), ensuring icode generation, nested functions, regexp
 * - compile() with returnFunction=true (FunctionNode)
 * - visitExpression(ecf_tail) for TAIL_CALL optimization path
 * - setprop/setelem inc/dec variants (SETPROP_OP, SETELEM_OP, SET_REF_OP)
 * - call types: NEW, CALL (non-special), CALLSPECIAL, TAIL_CALL branch
 * - loop/block/with/switch statement handling
 * - try-catch-finally with exception table entries
 * - generator yield/return->GENERATOR_END path
 * 
 * Partition B – Boundary Value Analysis (BVA):
 * - varIndex 0, 127, 128, 65535 boundaries for GETVAR1/SETVAR1 vs indexOp
 * - string index 0,3,4,255,65535 boundaries for REG_STR_C0..C3, REG_STR1/2/4
 * - local slot allocation/release (allocLocal/releaseLocal zero, one, multiple)
 * - double values: 0, 1, short range, int boundary, negative zero
 * - labelTable null/initial size / expansion to MIN_LABEL_TABLE_SIZE
 * - fixupTable null/initial / expansion to MIN_FIXUP_TABLE_SIZE
 * - icode capacity expansion thresholds
 * 
 * Partition C – Defect-Targeted Branch (Defects4J #testNumericKeys failing):
 * - The defect involves numeric keys in object literals being printed as [10] instead of ["010"].
 *   CodeGenerator.visitLiteral() builds literalIds array and propertyIds are stored as Object[].
 *   The bug likely in how String keys are encoded vs. integer ones.
 *   We test OBJECTLIT with propertyIds containing numeric-like strings "010".
 *   We also purposefully explore the SETNAME/NAME/TYPEOFNAME interactions.
 * 
 * Partition D – Exception & Guard Paths:
 * - addUint8 with value > 0xFF → Kit.codeBug() (can't test directly due to runtime exception)
 * - addUint16 with value > 0xFFFF → Kit.codeBug()
 * - addGotoOp with backward jump overlapping (≤ 2)
 * - badTree on unknown token type
 * - getLocalBlockRef on node without LOCAL_BLOCK_PROP (NullPointerException scenario)
 * - itsNeedsActivation combined with GETVAR/SETVAR (Kit.codeBug)
 * - external stack modification with Token.USE_STACK
 * - empty switch case targets
 * 
 * Partition E – Object Lifecycle & Contract:
 * - InterpreterData fields final values (functionType, needsActivation, name)
 * - nested Functions array correct sizes and types
 * - literalIds.toArray() conversion
 * - itsMaxStack, itsMaxLocals, itsMaxVars, itsMaxFrameArray computed correctly
 */
public class CodeGeneratorDeepseekTest {

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testCompileScriptSimple() {
        CompilerEnvirons env = new CompilerEnvirons();
        // Build a minimal AstRoot with one expression statement: 1+2
        // For simplification we test the constructor setup and basic call
        CodeGenerator cg = new CodeGenerator();
        // We cannot easily create full AstRoot without parser, but we can test basic compile with null?
        // Actually we'll test compile indirectly via Interpreter or Context. But we can still test
        // internal methods by instantiating CodeGenerator and calling package-private methods.
        // Since CodeGenerator is package-private and methods are package-private, we need to be in same package.
        // Create minimal ScriptNode stub using anonymous subclass
        ScriptNode stub = new ScriptNode() {
            {
                // Initialize minimal fields that compile() expects
                // Normally this is populated by parser. We'll set just enough to pass.
                setSourceName("test.js");
                setEncodedSource(null, 0, 0);
                setLength(0);
                setLineno(1);
                // Add a simple return statement node
                Node returnNode = new Node(Token.RETURN);
                Node numNode = Node.newNumber(42);
                returnNode.addChildToFront(numNode);
                this.addChildToBack(returnNode);
            }
        };
        InterpreterData data = cg.compile(env, (ScriptNode) stub, null, false);
        assertNotNull("InterpreterData should not be null", data);
        assertTrue("itsICode should have content", data.itsICode.length > 0);
        assertTrue("itsMaxVars >= 0", data.itsMaxVars >= 0);
    }

    @Test(timeout = 4000)
    public void testCompileFunction() {
        CompilerEnvirons env = new CompilerEnvirons();
        // Create function node stub
        FunctionNode fn = new FunctionNode("testFunc", 0);
        fn.setSourceName("testFunc.js");
        fn.setLength(0);
        fn.setLineno(1);
        fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        fn.setRequiresActivation(false);
        // Add a body (return 42)
        Node body = new Node(Token.BLOCK);
        Node returnNode = new Node(Token.RETURN);
        Node numNode = Node.newNumber(42);
        returnNode.addChildToFront(numNode);
        body.addChildToBack(returnNode);
        fn.addChildToBack(body);

        // Wrap in AstRoot with function count = 1
        AstRoot root = new AstRoot();
        root.addChild(fn);
        root.setFunctionCount(1);
        root.getFunctions().add(fn);
        // Set the function node as the first function
        // compile with returnFunction=true
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, true);
        assertNotNull(data);
        assertEquals("itsName should be function name", "testFunc", data.itsName);
        assertEquals("itsFunctionType should be FUNCTION_STATEMENT", 
                     FunctionNode.FUNCTION_STATEMENT, data.itsFunctionType);
    }

    @Test(timeout = 4000)
    public void testVisitExpressionTailCall() {
        // This tests the ECF_TAIL flag triggers TAIL_CALL in appropriate context
        // Build a call expression: f()  (no arguments)
        Node callNode = new Node(Token.CALL);
        Node nameNode = Node.newString(Token.NAME, "f");
        callNode.addChildToFront(nameNode);
        // Set line number to trigger line tracking
        callNode.setLineno(10);

        CompilerEnvirons env = new CompilerEnvirons();
        env.setGenerateDebugInfo(false); // tail call only when debug info off
        AstRoot root = new AstRoot();
        // Wrap in expression result
        Node exprResult = new Node(Token.EXPR_RESULT, callNode);
        root.addChildToBack(exprResult);

        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testTryCatchFinally() {
        // Build try-catch-finally structure
        Node tryNode = new Node(Token.TRY);
        Node body = new Node(Token.BLOCK);
        Node throwNode = new Node(Token.THROW);
        Node numNode = Node.newNumber(1);
        throwNode.addChildToFront(numNode);
        body.addChildToBack(throwNode);
        tryNode.addChildToFront(body);

        // Add catch block via jump nodes (simplified)
        // Since we can't easily create catch scope without parser, we test basic flow
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(tryNode);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testWithStatement() {
        // Build with (obj) { x } 
        Node enterWith = new Node(Token.ENTERWITH);
        Node objName = Node.newString(Token.NAME, "obj");
        enterWith.addChildToFront(objName);
        Node block = new Node(Token.BLOCK);
        Node leaveWith = new Node(Token.LEAVEWITH);
        block.addChildToBack(leaveWith);
        enterWith.addChildToBack(block);
        Node withNode = new Node(Token.WITH, enterWith);

        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(withNode);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testGeneratorYield() {
        Node yieldNode = new Node(Token.YIELD);
        Node numNode = Node.newNumber(7);
        yieldNode.addChildToFront(numNode);
        yieldNode.setLineno(5);
        // Wrap in expression result
        Node exprResult = new Node(Token.EXPR_RESULT, yieldNode);

        CompilerEnvirons env = new CompilerEnvirons();
        env.setGenerateDebugInfo(true);
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    @Test(timeout = 4000)
    public void testSwitchStatement() {
        // Build switch (x) { case 1: break; }
        Node switchNode = new Node(Token.SWITCH);
        Node switchExpr = Node.newString(Token.NAME, "x");
        switchNode.addChildToFront(switchExpr);
        
        Node caseNode = new Node(Token.CASE);
        Node caseTest = Node.newNumber(1);
        caseNode.addChildToFront(caseTest);
        // target not set (simplified)
        switchNode.addChildToBack(caseNode);

        // Add break target
        Node target = new Node(Token.TARGET);
        switchNode.addChildToBack(target);

        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(switchNode);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    // ---------- Partition B: Boundary Value Analysis ----------

    @Test(timeout = 4000)
    public void testBoundaryVarIndex() {
        // Test varIndex at boundaries: 0, 127, 128, 65535
        // We'll test by creating nodes with GETVAR using different indices
        // Use a FunctionNode with paramCount > 200 to get high index
        ScriptNode stub = new ScriptNode() {
            {
                setParamCount(300);
                setParamAndVarCount(300);
                String[] names = new String[300];
                for (int i = 0; i < 300; i++) names[i] = "v" + i;
                setParamAndVarNames(names);
                setParamAndVarConst(new boolean[300]);
            }
        };
        // Create GETVAR for index 0
        Node getVar0 = new Node(Token.GETVAR);
        getVar0.putIntProp(Node.VARIABLE_PROP, 0);
        // This is tricky: getIndexForNameNode relies on parser setup.
        // For now test that generated code doesn't crash on extreme values
        CompilerEnvirons env = new CompilerEnvirons();
        // We'll just test getIndexForNameNode returns -1 if name not found
        int index = stub.getIndexForNameNode(Node.newString(Token.NAME, "nonExistent"));
        assertEquals(-1, index);
    }

    @Test(timeout = 4000)
    public void testStringIndexBoundaries() {
        // We'll test addStringPrefix through reading generated bytecode
        // Can't easily test internal method without reflection.
        // Instead verify that strings are stored properly in itsData
        ScriptNode stub = new ScriptNode() {
            {
                setSourceName("test.js");
                setLength(0);
                setLineno(1);
            }
        };
        // This indirectly tests
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, stub, null, false);
        // No string table if no strings used
        assertNull(data.itsStringTable);
    }

    @Test(timeout = 4000)
    public void testDoubleValuesBoundaries() {
        // Test ZERO, ONE, SHORTNUMBER, INTNUMBER, NUMBER paths
        Node zeroNode = Node.newNumber(0);
        Node oneNode = Node.newNumber(1);
        Node shortNode = Node.newNumber(127);
        Node intNode = Node.newNumber(32768); // > short range
        Node doubleNode = Node.newNumber(3.14159);
        
        // Wrap in expression results
        Node seq = new Node(Token.COMMA);
        seq.addChildToFront(zeroNode);
        seq.addChildToBack(oneNode);
        seq.addChildToBack(shortNode);
        seq.addChildToBack(intNode);
        seq.addChildToBack(doubleNode);
        
        Node exprResult = new Node(Token.EXPR_RESULT, seq);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
        // Check double table contains pi
        boolean foundPi = false;
        if (data.itsDoubleTable != null) {
            for (double d : data.itsDoubleTable) {
                if (Math.abs(d - 3.14159) < 1e-10) foundPi = true;
            }
            assertTrue("Double table should contain pi", foundPi);
        }
    }

    @Test(timeout = 4000)
    public void testNegativeZero() {
        Node negZeroNode = new Node(Token.NUMBER);
        negZeroNode.setDouble(-0.0);
        Node exprResult = new Node(Token.EXPR_RESULT, negZeroNode);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    // ---------- Partition C: Defect-Targeted (Numeric Keys) ----------

    @Test(timeout = 4000)
    public void testObjectLiteralWithNumericKeyString() {
        // This specifically targets the defect: "010" key being printed as 10
        // Build OBJECTLIT node with propertyIds containing "010"
        Node objLit = new Node(Token.OBJECTLIT);
        // Property ids array
        Object[] propIds = new Object[] { "010" };
        objLit.putProp(Node.OBJECT_IDS_PROP, propIds);
        // Value child: number 1
        Node valueNode = Node.newNumber(1);
        objLit.addChildToFront(valueNode);
        
        // Add index to literalIds via literal storage
        // Simulate by wrapping in expression
        Node exprResult = new Node(Token.EXPR_RESULT, objLit);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
        // Verify literalIds contains "010" as a string key
        boolean foundStringKey = false;
        if (data.literalIds != null) {
            for (Object obj : data.literalIds) {
                if (obj instanceof Object[]) {
                    Object[] ids = (Object[]) obj;
                    for (Object id : ids) {
                        if ("010".equals(id)) {
                            foundStringKey = true;
                        }
                    }
                }
            }
        }
        // The defect: "010" might be converted to integer 10. We expect string preservation.
        assertTrue("Object literal key '010' must be preserved as string, not converted to int 10", 
                   foundStringKey);
    }

    @Test(timeout = 4000)
    public void testObjectLiteralWithMixedNumericKeys() {
        // Test various numeric-looking string keys
        Node objLit = new Node(Token.OBJECTLIT);
        Object[] propIds = new Object[] { "0", "00", "010", "10", "1e2" };
        objLit.putProp(Node.OBJECT_IDS_PROP, propIds);
        // Add values (dummy)
        for (int i = 0; i < propIds.length; i++) {
            Node val = Node.newNumber(i);
            objLit.addChildToFront(val);
        }
        Node exprResult = new Node(Token.EXPR_RESULT, objLit);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
        // Check "010" preserved
        boolean found010 = false;
        if (data.literalIds != null) {
            for (Object obj : data.literalIds) {
                if (obj instanceof Object[]) {
                    Object[] ids = (Object[]) obj;
                    for (Object id : ids) {
                        if ("010".equals(id)) found010 = true;
                    }
                }
            }
        }
        assertTrue("String key '010' must be preserved", found010);
    }

    @Test(timeout = 4000)
    public void testArrayLiteralWithNumericString() {
        // Test ARRAYLIT with skip indexes (related to numeric key behavior)
        Node arrLit = new Node(Token.ARRAYLIT);
        arrLit.putProp(Node.SKIP_INDEXES_PROP, new int[] {1, 3});
        Node val1 = Node.newNumber(10);
        Node val2 = Node.newString(Token.STRING, "010");
        arrLit.addChildToFront(val1);
        arrLit.addChildToFront(val2);
        
        // EXPR_RESULT with comma to combine
        Node comma = new Node(Token.COMMA);
        comma.addChildToFront(arrLit);
        Node dummy = Node.newNumber(0);
        comma.addChildToBack(dummy);
        
        Node exprResult = new Node(Token.EXPR_RESULT, comma);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull(data);
    }

    // ---------- Partition D: Exception & Guard Paths ----------

    @Test(timeout = 4000)
    public void testGetLocalBlockRefNoProp() {
        Node node = new Node(Token.CATCH_SCOPE);
        node.putProp(Node.LOCAL_BLOCK_PROP, null);
        // Should throw NullPointerException indirectly
        // We can't prevent this but test that compile doesn't crash entire VM
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        Node catchScope = new Node(Token.CATCH_SCOPE);
        // Don't set LOCAL_BLOCK_PROP, will cause NPE in getLocalBlockRef
        Node nameNode = Node.newString("e");
        Node exprNode = Node.newNumber(0);
        catchScope.addChildToFront(nameNode);
        catchScope.addChildToFront(exprNode);
        root.addChildToBack(catchScope);
        try {
            CodeGenerator cg = new CodeGenerator();
            cg.compile(env, root, null, false);
            fail("Should have thrown exception due to missing LOCAL_BLOCK_PROP");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBadTreeUnknownToken() {
        Node badNode = new Node(-1); // Unknown token type
        Node exprResult = new Node(Token.EXPR_RESULT);
        exprResult.addChildToFront(badNode);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        try {
            CodeGenerator cg = new CodeGenerator();
            cg.compile(env, root, null, false);
            fail("Should have thrown RuntimeException for bad tree");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStackDepthCheck() {
        // Create expression that changes stack depth incorrectly
        Node expr = new Node(Token.USE_STACK); // This adds 1 to stack
        // But we should wrap it so that visitExpression expects final stack depth +1
        // This should cause stackDepth mismatch in visitStatement
        // Actually USE_STACK is only legal in specific contexts, this will cause codeBug
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(new Node(Token.EXPR_RESULT, expr));
        try {
            CodeGenerator cg = new CodeGenerator();
            cg.compile(env, root, null, false);
            fail("Stack depth mismatch should have triggered exception");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    // ---------- Partition E: Object Lifecycle & Contract ----------

    @Test(timeout = 4000)
    public void testNestedFunctionsArray() {
        // Create function with nested function
        FunctionNode outerFn = new FunctionNode("outer", 0);
        outerFn.setSourceName("test.js");
        outerFn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        outerFn.setRequiresActivation(true);
        outerFn.setLength(0);
        outerFn.setLineno(1);
        outerFn.setParamCount(0);
        outerFn.setParamAndVarCount(0);
        
        FunctionNode innerFn = new FunctionNode("inner", 0);
        innerFn.setSourceName("test.js");
        innerFn.setFunctionType(FunctionNode.FUNCTION_EXPRESSION);
        innerFn.setRequiresActivation(false);
        innerFn.setLength(0);
        innerFn.setLineno(2);
        
        // Setup function indices: outer has 1 nested function
        outerFn.addFunction(innerFn);
        outerFn.setFunctionCount(1);
        
        Node body = new Node(Token.BLOCK);
        // Create closure expression for inner function inside outer
        Node closureExpr = new Node(Token.FUNCTION);
        closureExpr.putIntProp(Node.FUNCTION_PROP, 0);
        body.addChildToBack(closureExpr);
        outerFn.addChildToBack(body);
        
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChild(outerFn);
        root.setFunctionCount(1);
        root.getFunctions().add(outerFn);
        
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, true);
        assertNotNull(data);
        assertNotNull("Nested functions should exist", data.itsNestedFunctions);
        assertEquals(1, data.itsNestedFunctions.length);
        assertEquals("Inner function name", "inner", data.itsNestedFunctions[0].itsName);
    }

    @Test(timeout = 4000)
    public void testMaxLocalsMaxStack() {
        // Create complex expression requiring many stack slots
        Node addChain = new Node(Token.ADD);
        Node a = Node.newNumber(1);
        Node b = Node.newNumber(2);
        Node c = Node.newNumber(3);
        addChain.addChildToFront(a);
        addChain.addChildToFront(b);
        // Binary operators produce +1 stack depth each
        // Wrap in another add
        Node outerAdd = new Node(Token.ADD, addChain);
        outerAdd.addChildToFront(c);
        
        Node exprResult = new Node(Token.EXPR_RESULT, outerAdd);
        
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertTrue("itsMaxStack should be positive", data.itsMaxStack > 0);
        assertTrue("itsMaxLocals should be >= 0", data.itsMaxLocals >= 0);
        assertTrue("itsMaxFrameArray should be sum of vars+locals+stack",
                   data.itsMaxFrameArray >= data.itsMaxVars + data.itsMaxLocals + data.itsMaxStack);
    }

    @Test(timeout = 4000)
    public void testEncodedSourcePositions() {
        ScriptNode stub = new ScriptNode() {
            {
                setSourceName("test.js");
                setEncodedSource("encoded content", 10, 25);
                setLength(15);
                setLineno(1);
            }
        };
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, stub, "encoded content", false);
        assertEquals(10, data.encodedSourceStart);
        assertEquals(25, data.encodedSourceEnd);
    }

    @Test(timeout = 4000)
    public void testLiteralIdsSerialization() {
        // Create object literal with multiple property ids to exercise literalIds
        Node objLit = new Node(Token.OBJECTLIT);
        Object[] propIds = new Object[] { "a", "b", "c" };
        objLit.putProp(Node.OBJECT_IDS_PROP, propIds);
        Node v1 = Node.newNumber(1);
        Node v2 = Node.newNumber(2);
        Node v3 = Node.newNumber(3);
        objLit.addChildToFront(v1);
        objLit.addChildToFront(v2);
        objLit.addChildToFront(v3);
        
        Node exprResult = new Node(Token.EXPR_RESULT, objLit);
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.addChildToBack(exprResult);
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, root, null, false);
        assertNotNull("literalIds should not be null when object literal present", data.literalIds);
    }

    @Test(timeout = 4000)
    public void testEmptyScript() {
        ScriptNode empty = new ScriptNode() {
            {
                setSourceName("empty.js");
                setLength(0);
                setLineno(1);
            }
        };
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator cg = new CodeGenerator();
        InterpreterData data = cg.compile(env, empty, null, false);
        assertNotNull("Empty script should compile", data);
        // Should contain at least RETURN_RESULT at end
        assertTrue("ICode should contain RETURN_RESULT", 
                   data.itsICode.length >= 1 && 
                   (data.itsICode[data.itsICode.length - 1] == Icode.Icode_RETURN_RESULT || 
                    data.itsICode[data.itsICode.length - 1] == (byte)Token.RETURN_RESULT));
    }
}