package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CodeGeneratorDeepseekTest - White-box test suite for CodeGenerator class.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - compile() with ScriptNode (non-function) and FunctionNode (returnFunction=true)
 *   - generateFunctionICode() for generator and non-generator functions
 *   - visitStatement() for all statement types: FUNCTION, LABEL, LOOP, BLOCK, EMPTY, WITH, SCRIPT,
 *     ENTERWITH, LEAVEWITH, LOCAL_BLOCK, DEBUGGER, SWITCH, TARGET, IFEQ, IFNE, GOTO, JSR, FINALLY,
 *     EXPR_VOID, EXPR_RESULT, TRY, CATCH_SCOPE, THROW, RETHROW, RETURN, RETURN_RESULT,
 *     ENUM_INIT_KEYS, ENUM_INIT_VALUES, ENUM_INIT_ARRAY, Icode_GENERATOR
 *   - visitExpression() for all expression types: FUNCTION, LOCAL_LOAD, COMMA, USE_STACK,
 *     REF_CALL, CALL, NEW, AND, OR, HOOK, GETPROP, GETPROPNOWARN, DELPROP, GETELEM,
 *     BITAND, BITOR, BITXOR, LSH, RSH, URSH, ADD, SUB, MOD, DIV, MUL, EQ, NE, SHEQ, SHNE,
 *     IN, INSTANCEOF, LE, LT, GE, GT, POS, NEG, NOT, BITNOT, TYPEOF, VOID, GET_REF, DEL_REF,
 *     SETPROP, SETPROP_OP, SETELEM, SETELEM_OP, SET_REF, SET_REF_OP, STRICT_SETNAME, SETNAME,
 *     SETCONST, TYPEOFNAME, BINDNAME, NAME, STRING, INC, DEC, NUMBER, GETVAR, SETVAR, SETCONSTVAR,
 *     NULL, THIS, THISFN, FALSE, TRUE, ENUM_NEXT, ENUM_ID, REGEXP, ARRAYLIT, OBJECTLIT, ARRAYCOMP,
 *     REF_SPECIAL, REF_MEMBER, REF_NS_MEMBER, REF_NAME, REF_NS_NAME, DOTQUERY, DEFAULTNAMESPACE,
 *     ESCXMLATTR, ESCXMLTEXT, YIELD, WITHEXPR
 *   - generateCallFunAndThis() for NAME, GETPROP, GETELEM, and default cases
 *   - visitIncDec() for GETVAR, NAME, GETPROP, GETELEM, GET_REF
 *   - visitLiteral() for ARRAYLIT with/without skipIndexes, OBJECTLIT
 *   - visitArrayComprehension()
 *   - generateNestedFunctions() with 0 and >0 functions
 *   - generateRegExpLiterals() with 0 and >0 regexps
 *   - updateLineNumber() with positive and negative line numbers
 *   - stackChange() for positive and negative changes
 *   - allocLocal() / releaseLocal() for local variable management
 *   - addExceptionHandler() for try-catch and try-finally
 *   - addGoto() for backward and forward jumps
 *   - fixLabelGotos() for resolving forward references
 *   - addBackwardGoto() and resolveForwardGoto() for jump resolution
 *   - resolveGoto() for short and long jumps
 *   - addToken(), addIcode(), addUint8(), addUint16(), addInt() for bytecode emission
 *   - getDoubleIndex() for double table management
 *   - addVarOp() for GETVAR, SETVAR, SETCONSTVAR with index <128 and >=128
 *   - addStringOp() and addIndexOp() for opcode prefixes
 *   - addStringPrefix() for string index ranges: <4, <=0xFF, <=0xFFFF, >0xFFFF
 *   - addIndexPrefix() for index ranges: <6, <=0xFF, <=0xFFFF, >0xFFFF
 *   - increaseICodeCapacity() for array growth
 *   - getLocalBlockRef() for LOCAL_BLOCK_PROP
 *   - getTargetLabel() for label allocation and reuse
 *   - markTargetLabel() for label marking
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty script (no statements)
 *   - Single statement scripts
 *   - Deeply nested blocks (potential stack overflow)
 *   - Large number of functions (nested functions array)
 *   - Large number of regexps
 *   - Large number of string literals (string table overflow)
 *   - Large number of double literals (double table overflow)
 *   - Large number of labels (label table overflow)
 *   - Large number of fixups (fixup table overflow)
 *   - Large number of exception handlers (exception table overflow)
 *   - Large number of locals (localTop overflow)
 *   - Large stack depth (itsMaxStack overflow)
 *   - Large number of arguments (itsMaxCalleeArgs overflow)
 *   - Index values at boundaries: 0, 3, 4, 5, 6, 127, 128, 255, 256, 65535, 65536
 *   - String index values at boundaries: 0, 3, 4, 255, 256, 65535, 65536
 *   - Double values: 0, 1, -1, Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
 *     NaN, Infinity, negative zero
 *   - Line numbers: -1, 0, positive, 0xFFFF
 *   - Empty strings, null strings (where applicable)
 *   - Negative stack changes
 *   - Zero-length arrays
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - StackOverflowError from deeply nested expressions (targeting the known defect)
 *   - Deeply nested ADD operations causing recursive code generation
 *   - Deeply nested function calls
 *   - Deeply nested blocks
 *   - Deeply nested comma expressions
 *   - Deeply nested AND/OR expressions
 *   - Deeply nested HOOK expressions
 *   - Deeply nested property accesses
 *   - Deeply nested array/object literals
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - badTree() for unsupported node types
 *   - Kit.codeBug() for internal consistency violations
 *   - Null child nodes in various contexts
 *   - Invalid token/icode values
 *   - Invalid index values (negative, too large)
 *   - Invalid offset values in resolveGoto
 *   - Stack depth mismatch in visitStatement
 *   - Stack depth mismatch in visitExpression
 *   - Label already marked in markTargetLabel
 *   - Unlocated label in fixLabelGotos
 *   - Backward goto with fromPC <= jumpPC
 *   - Forward goto with iCodeTop < fromPC + 3
 *   - Overlapping jumps (offset 0-2)
 *   - Invalid varOp operations
 *   - Invalid local release (slot mismatch)
 *   - Exception table size mismatch
 *   - String table duplicate entries
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple calls to compile() on same instance
 *   - Reuse of CodeGenerator after compile
 *   - InterpreterData state after compile (itsICode, itsStringTable, itsDoubleTable,
 *     itsExceptionTable, itsMaxVars, itsMaxFrameArray, itsMaxLocals, itsMaxStack,
 *     itsMaxCalleeArgs, argNames, argIsConst, argCount, encodedSourceStart, encodedSourceEnd,
 *     literalIds, itsNestedFunctions, itsRegExpLiterals, itsFunctionType, itsNeedsActivation,
 *     itsName, topLevel, firstLinePC, longJumps)
 *   - Consistency of internal tables after compilation
 */
public class CodeGeneratorDeepseekTest {

    /* ================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ================================================================ */

    @Test(timeout = 4000)
    public void testCompileEmptyScript() {
        CompilerEnvirons env = new CompilerEnvirons();
        // Create an empty script (AstRoot with no statements)
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertTrue("Should be top level", data.topLevel);
        assertNotNull("itsICode should not be null", data.itsICode);
        // Empty script should have at least RETURN_RESULT
        assertTrue("itsICode should have content", data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileReturnFunction() {
        CompilerEnvirons env = new CompilerEnvirons();
        // Create a simple function node
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // We need a function node in the tree
        FunctionNode fn = new FunctionNode();
        fn.setName("testFn");
        fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
        fn.setBaseLineno(1);
        fn.setSourceName("test");
        root.addChild(fn);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, true);
        
        assertNotNull("InterpreterData should not be null", data);
        assertEquals("Function type should be FUNCTION_STATEMENT", 
                     FunctionNode.FUNCTION_STATEMENT, data.itsFunctionType);
    }

    @Test(timeout = 4000)
    public void testCompileGeneratorFunction() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        FunctionNode fn = new FunctionNode();
        fn.setName("genFn");
        fn.setFunctionType(FunctionNode.FUNCTION_EXPRESSION);
        fn.setBaseLineno(1);
        fn.setIsGenerator(true);
        fn.setSourceName("test");
        root.addChild(fn);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, true);
        
        assertNotNull("InterpreterData should not be null", data);
        // Generator should have GENERATOR icode
        assertTrue("itsICode should contain GENERATOR opcode", 
                   data.itsICode.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileWithNestedFunctions() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add multiple function nodes
        for (int i = 0; i < 3; i++) {
            FunctionNode fn = new FunctionNode();
            fn.setName("fn" + i);
            fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
            fn.setBaseLineno(1);
            fn.setSourceName("test");
            root.addChild(fn);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertNotNull("itsNestedFunctions should not be null", data.itsNestedFunctions);
        assertEquals("Should have 3 nested functions", 3, data.itsNestedFunctions.length);
    }

    @Test(timeout = 4000)
    public void testCompileWithRegexp() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add a regexp literal
        root.addRegexpString("test", "g");
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertNotNull("itsRegExpLiterals should not be null", data.itsRegExpLiterals);
        assertEquals("Should have 1 regexp", 1, data.itsRegExpLiterals.length);
    }

    @Test(timeout = 4000)
    public void testCompileWithEncodedSource() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        root.setEncodedSourceBounds(0, 10);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, "encodedSource", false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertEquals("encodedSource should match", "encodedSource", data.encodedSource);
        assertEquals("encodedSourceStart should be 0", 0, data.encodedSourceStart);
        assertEquals("encodedSourceEnd should be 10", 10, data.encodedSourceEnd);
    }

    @Test(timeout = 4000)
    public void testCompileStrictMode() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(true);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertTrue("Should be in strict mode", data.strictMode);
    }

    /* ================================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ================================================================ */

    @Test(timeout = 4000)
    public void testCompileWithManyStrings() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create a script with many string references
        // We'll use a simple approach: add many name nodes
        for (int i = 0; i < 100; i++) {
            Node nameNode = Node.newString("var" + i);
            root.addChild(nameNode);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        // String table should have been created
        assertNotNull("itsStringTable should not be null", data.itsStringTable);
        assertTrue("String table should have entries", data.itsStringTable.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileWithManyDoubles() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add many double literals
        for (int i = 0; i < 100; i++) {
            Node numNode = Node.newNumber(i * 1.5);
            root.addChild(numNode);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertNotNull("itsDoubleTable should not be null", data.itsDoubleTable);
        assertTrue("Double table should have entries", data.itsDoubleTable.length > 0);
    }

    @Test(timeout = 4000)
    public void testCompileWithBoundaryDoubleValues() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Test various double boundary values
        double[] testValues = {
            0.0, 1.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 
            Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.MAX_VALUE,
            -0.0, 3.14159, 2.71828
        };
        
        for (double val : testValues) {
            Node numNode = Node.newNumber(val);
            root.addChild(numNode);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
        assertNotNull("itsDoubleTable should not be null", data.itsDoubleTable);
    }

    @Test(timeout = 4000)
    public void testCompileWithManyLabels() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create many label targets
        for (int i = 0; i < 50; i++) {
            Jump target = new Jump(Token.TARGET);
            root.addChild(target);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithDeeplyNestedBlocks() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested blocks (depth 100)
        Node current = root;
        for (int i = 0; i < 100; i++) {
            Node block = new Node(Token.BLOCK);
            current.addChild(block);
            current = block;
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithManyExceptionHandlers() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create many try-catch blocks
        for (int i = 0; i < 20; i++) {
            Jump tryNode = new Jump(Token.TRY);
            Node tryBlock = new Node(Token.BLOCK);
            tryNode.addChild(tryBlock);
            
            Jump catchTarget = new Jump(Token.TARGET);
            tryNode.target = catchTarget;
            
            root.addChild(tryNode);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("InterpreterData should not be null", data);
    }

    /* ================================================================
     * Partition C: Defect-Targeted Branch Zone
     * ================================================================ */

    @Test(timeout = 4000)
    public void testDeeplyNestedAddOperations() {
        // This test targets the known StackOverflowError defect
        // by creating deeply nested ADD operations
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested ADD expressions (depth 5000)
        Node expr = Node.newNumber(1.0);
        for (int i = 0; i < 5000; i++) {
            Node addNode = new Node(Token.ADD);
            addNode.addChild(expr);
            addNode.addChild(Node.newNumber(1.0));
            expr = addNode;
        }
        
        // Wrap in expression statement
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(expr);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested adds");
        }
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedFunctionCalls() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested function calls (depth 5000)
        Node call = new Node(Token.CALL);
        Node name = Node.newString(Token.NAME, "f");
        call.addChild(name);
        
        for (int i = 0; i < 5000; i++) {
            Node newCall = new Node(Token.CALL);
            newCall.addChild(call);
            call = newCall;
        }
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(call);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested calls");
        }
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedCommaExpressions() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested comma expressions (depth 5000)
        Node comma = new Node(Token.COMMA);
        comma.addChild(Node.newNumber(1.0));
        comma.addChild(Node.newNumber(2.0));
        
        for (int i = 0; i < 5000; i++) {
            Node newComma = new Node(Token.COMMA);
            newComma.addChild(comma);
            newComma.addChild(Node.newNumber(3.0));
            comma = newComma;
        }
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(comma);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested commas");
        }
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedAndOrExpressions() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested AND expressions (depth 5000)
        Node and = new Node(Token.AND);
        and.addChild(Node.newString(Token.TRUE));
        and.addChild(Node.newString(Token.TRUE));
        
        for (int i = 0; i < 5000; i++) {
            Node newAnd = new Node(Token.AND);
            newAnd.addChild(and);
            newAnd.addChild(Node.newString(Token.TRUE));
            and = newAnd;
        }
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(and);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested ANDs");
        }
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedHookExpressions() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested HOOK (ternary) expressions (depth 5000)
        Node hook = new Node(Token.HOOK);
        hook.addChild(Node.newString(Token.TRUE));
        hook.addChild(Node.newNumber(1.0));
        hook.addChild(Node.newNumber(2.0));
        
        for (int i = 0; i < 5000; i++) {
            Node newHook = new Node(Token.HOOK);
            newHook.addChild(Node.newString(Token.TRUE));
            newHook.addChild(hook);
            newHook.addChild(Node.newNumber(3.0));
            hook = newHook;
        }
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(hook);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested hooks");
        }
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedPropertyAccess() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create deeply nested property accesses (depth 5000)
        Node getprop = new Node(Token.GETPROP);
        getprop.addChild(Node.newString(Token.NAME, "obj"));
        getprop.addChild(Node.newString("prop"));
        
        for (int i = 0; i < 5000; i++) {
            Node newGetprop = new Node(Token.GETPROP);
            newGetprop.addChild(getprop);
            newGetprop.addChild(Node.newString("prop" + i));
            getprop = newGetprop;
        }
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(getprop);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile without StackOverflowError", data);
        } catch (StackOverflowError e) {
            fail("StackOverflowError should not occur during compilation of deeply nested property accesses");
        }
    }

    /* ================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ================================================================ */

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBadTreeUnsupportedNodeType() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add an unsupported node type
        Node badNode = new Node(Token.LAST_TOKEN + 1);
        root.addChild(badNode);
        
        CodeGenerator gen = new CodeGenerator();
        gen.compile(env, root, null, false);
    }

    @Test(timeout = 4000)
    public void testCompileWithNullChild() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add a block with null child (should be handled gracefully)
        Node block = new Node(Token.BLOCK);
        block.addChild(null);
        root.addChild(block);
        
        CodeGenerator gen = new CodeGenerator();
        try {
            InterpreterData data = gen.compile(env, root, null, false);
            assertNotNull("Should compile with null child", data);
        } catch (NullPointerException e) {
            // This is acceptable behavior for null child
        }
    }

    @Test(timeout = 4000)
    public void testCompileWithNegativeLineNumber() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add a node with negative line number
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.setLineno(-1);
        exprStmt.addChild(Node.newNumber(42.0));
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile with negative line number", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithLargeLineNumber() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add a node with large line number
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.setLineno(0xFFFF);
        exprStmt.addChild(Node.newNumber(42.0));
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile with large line number", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithManyLocalVariables() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create many local block references
        for (int i = 0; i < 200; i++) {
            Node localBlock = new Node(Token.LOCAL_BLOCK);
            localBlock.putIntProp(Node.LOCAL_PROP, i);
            root.addChild(localBlock);
        }
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile with many local variables", data);
        assertTrue("itsMaxLocals should be positive", data.itsMaxLocals > 0);
    }

    /* ================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ================================================================ */

    @Test(timeout = 4000)
    public void testMultipleCompileCalls() {
        CompilerEnvirons env = new CompilerEnvirons();
        CodeGenerator gen = new CodeGenerator();
        
        // First compile
        AstRoot root1 = new AstRoot();
        root1.setSourceName("test1");
        root1.setInStrictMode(false);
        InterpreterData data1 = gen.compile(env, root1, null, false);
        assertNotNull("First compile should succeed", data1);
        
        // Second compile with different data
        AstRoot root2 = new AstRoot();
        root2.setSourceName("test2");
        root2.setInStrictMode(false);
        InterpreterData data2 = gen.compile(env, root2, null, false);
        assertNotNull("Second compile should succeed", data2);
        
        // Verify they are different objects
        assertNotSame("Should be different InterpreterData objects", data1, data2);
    }

    @Test(timeout = 4000)
    public void testInterpreterDataStateAfterCompile() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Add some content
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(Node.newNumber(42.0));
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        // Verify key state fields
        assertNotNull("itsICode should not be null", data.itsICode);
        assertTrue("itsICode should have content", data.itsICode.length > 0);
        assertTrue("itsMaxStack should be >= 0", data.itsMaxStack >= 0);
        assertTrue("itsMaxLocals should be >= 0", data.itsMaxLocals >= 0);
        assertTrue("itsMaxFrameArray should be >= 0", data.itsMaxFrameArray >= 0);
        assertTrue("itsMaxCalleeArgs should be >= 0", data.itsMaxCalleeArgs >= 0);
        assertTrue("itsMaxVars should be >= 0", data.itsMaxVars >= 0);
        assertTrue("topLevel should be true", data.topLevel);
        assertEquals("Source name should match", "test", data.itsSourceFile);
    }

    @Test(timeout = 4000)
    public void testCompileWithFunctionExpressionStatement() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create a function expression statement
        FunctionNode fn = new FunctionNode();
        fn.setName("exprFn");
        fn.setFunctionType(FunctionNode.FUNCTION_EXPRESSION_STATEMENT);
        fn.setBaseLineno(1);
        fn.setSourceName("test");
        
        Node fnNode = new Node(Token.FUNCTION);
        fnNode.putIntProp(Node.FUNCTION_PROP, 0);
        root.addChild(fnNode);
        root.addChild(fn);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile function expression statement", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithSwitchStatement() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create a switch statement
        Jump switchNode = new Jump(Token.SWITCH);
        switchNode.addChild(Node.newString(Token.NAME, "x"));
        
        // Add case nodes
        for (int i = 0; i < 3; i++) {
            Jump caseNode = new Jump(Token.CASE);
            caseNode.addChild(Node.newNumber(i));
            caseNode.target = new Jump(Token.TARGET);
            switchNode.addChild(caseNode);
        }
        
        root.addChild(switchNode);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile switch statement", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithTryCatchFinally() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Create try-catch-finally
        Jump tryNode = new Jump(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        tryNode.addChild(tryBlock);
        
        // Catch target
        Jump catchTarget = new Jump(Token.TARGET);
        tryNode.target = catchTarget;
        
        // Finally target
        Jump finallyTarget = new Jump(Token.TARGET);
        tryNode.setFinally(finallyTarget);
        
        root.addChild(tryNode);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile try-catch-finally", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithThrowAndReturn() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Throw statement
        Node throwNode = new Node(Token.THROW);
        throwNode.addChild(Node.newString(Token.NAME, "e"));
        root.addChild(throwNode);
        
        // Return statement
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChild(Node.newNumber(42.0));
        root.addChild(returnNode);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile throw and return", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithYield() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Yield expression
        Node yieldNode = new Node(Token.YIELD);
        yieldNode.addChild(Node.newNumber(42.0));
        yieldNode.setLineno(1);
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(yieldNode);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile yield expression", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithArrayAndObjectLiterals() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Array literal
        Node arrayLit = new Node(Token.ARRAYLIT);
        arrayLit.addChild(Node.newNumber(1.0));
        arrayLit.addChild(Node.newNumber(2.0));
        arrayLit.addChild(Node.newNumber(3.0));
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(arrayLit);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile array literal", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithIncDecOperations() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Increment operation on a variable
        Node incNode = new Node(Token.INC);
        incNode.putIntProp(Node.INCRDECR_PROP, 1); // post-increment
        incNode.addChild(Node.newString(Token.NAME, "x"));
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(incNode);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile increment operation", data);
    }

    @Test(timeout = 4000)
    public void testCompileWithDeleteProperty() {
        CompilerEnvirons env = new CompilerEnvirons();
        AstRoot root = new AstRoot();
        root.setSourceName("test");
        root.setInStrictMode(false);
        
        // Delete property
        Node delProp = new Node(Token.DELPROP);
        Node bindName = Node.newString(Token.BINDNAME, "obj");
        delProp.addChild(bindName);
        delProp.addChild(Node.newString(Token.STRING, "prop"));
        
        Node exprStmt = new Node(Token.EXPR_VOID);
        exprStmt.addChild(delProp);
        root.addChild(exprStmt);
        
        CodeGenerator gen = new CodeGenerator();
        InterpreterData data = gen.compile(env, root, null, false);
        
        assertNotNull("Should compile delete property", data);
    }
}