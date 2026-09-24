package org.mozilla.javascript;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mozilla.javascript.ast.*;
import org.mozilla.javascript.ast.FunctionNode;

/**
 * White-box test suite for CodeGenerator.
 * Targets core compilation paths, binary/unary operators, control flow,
 * exception handling, and the known defect related to operator precedence/parentheses.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths (compile script, function, nested functions)
 * - Partition B: BVA for numbers (zero, negative zero, int boundaries, doubles)
 * - Partition C: Defect-targeted: nested logical operators (||, &&) with parentheses
 * - Partition D: Exception paths (try-catch, finally, throw, rethrow)
 * - Partition E: Object lifecycle (literal arrays/objects, regexp, string table)
 */
public class CodeGeneratorDeepseekTest {

    private static final int TIMEOUT = 4000;

    // Helper to compile and execute a script, returning the result
    private Object execScript(String source) {
        Context cx = Context.enter();
        try {
            Script script = cx.compileString(source, "test", 1, null);
            Scriptable scope = cx.initStandardObjects();
            return script.exec(cx, scope);
        } finally {
            Context.exit();
        }
    }

    // Helper to compile and execute a function expression
    private Object execFunction(String source, Object... args) {
        Context cx = Context.enter();
        try {
            Script script = cx.compileString("(" + source + ")", "test", 1, null);
            Scriptable scope = cx.initStandardObjects();
            Function f = (Function) script.exec(cx, scope);
            return f.call(cx, scope, scope, args);
        } finally {
            Context.exit();
        }
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = TIMEOUT)
    public void testCompileEmptyScript() {
        Object result = execScript("");
        assertEquals("undefined", Context.toString(result));
    }

    @Test(timeout = TIMEOUT)
    public void testCompileSimpleExpression() {
        Object result = execScript("1+2");
        assertEquals(3.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testCompileFunctionStatement() {
        Object result = execScript("function f() { return 42; } f()");
        assertEquals(42.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testCompileFunctionExpression() {
        Object result = execScript("var f = function() { return 7; }; f()");
        assertEquals(7.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testCompileNestedFunctions() {
        Object result = execScript("function outer() { function inner() { return 1; } return inner(); } outer()");
        assertEquals(1.0, result);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = TIMEOUT)
    public void testNumberZero() {
        assertEquals(0.0, execScript("0"));
    }

    @Test(timeout = TIMEOUT)
    public void testNumberNegativeZero() {
        Object result = execScript("1/(-0)");
        assertEquals(Double.NEGATIVE_INFINITY, result);
    }

    @Test(timeout = TIMEOUT)
    public void testNumberOne() {
        assertEquals(1.0, execScript("1"));
    }

    @Test(timeout = TIMEOUT)
    public void testNumberShort() {
        assertEquals(127.0, execScript("127"));
        assertEquals(-128.0, execScript("-128"));
    }

    @Test(timeout = TIMEOUT)
    public void testNumberInt() {
        assertEquals(32768.0, execScript("32768"));
        assertEquals(-32769.0, execScript("-32769"));
    }

    @Test(timeout = TIMEOUT)
    public void testNumberDouble() {
        assertEquals(3.14159, execScript("3.14159"));
    }

    @Test(timeout = TIMEOUT)
    public void testStringEmpty() {
        assertEquals("", execScript("''"));
    }

    @Test(timeout = TIMEOUT)
    public void testStringNonEmpty() {
        assertEquals("hello", execScript("'hello'"));
    }

    @Test(timeout = TIMEOUT)
    public void testNull() {
        assertNull(execScript("null"));
    }

    @Test(timeout = TIMEOUT)
    public void testBooleanTrue() {
        assertEquals(true, execScript("true"));
    }

    @Test(timeout = TIMEOUT)
    public void testBooleanFalse() {
        assertEquals(false, execScript("false"));
    }

    // ==================== Partition C: Defect-Targeted: Nested Logical Operators ====================

    @Test(timeout = TIMEOUT)
    public void testNestedLogicalOrWithParentheses() {
        // The known defect: missing parentheses in code generation for nested || and &&
        // This test verifies correct evaluation of a || (b || c) vs (a || b) || c
        // Both should produce same result but the generated icode must handle short-circuit correctly.
        assertEquals(true, execScript("var a=false,b=false,c=true; a || (b || c)"));
        assertEquals(false, execScript("var a=false,b=false,c=false; a || (b || c)"));
        assertEquals(true, execScript("var a=true,b=false,c=false; (a || b) || c"));
        assertEquals(false, execScript("var a=false,b=false,c=false; (a || b) || c"));
    }

    @Test(timeout = TIMEOUT)
    public void testNestedLogicalAndWithParentheses() {
        assertEquals(true, execScript("var a=true,b=true,c=true; a && (b && c)"));
        assertEquals(false, execScript("var a=true,b=true,c=false; a && (b && c)"));
        assertEquals(false, execScript("var a=false,b=true,c=true; (a && b) && c"));
    }

    @Test(timeout = TIMEOUT)
    public void testMixedLogicalOperators() {
        assertEquals(true, execScript("var a=true,b=false,c=true; a || (b && c)"));
        assertEquals(false, execScript("var a=false,b=false,c=true; a || (b && c)"));
        assertEquals(true, execScript("var a=true,b=true,c=false; a && (b || c)"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = TIMEOUT)
    public void testTryCatch() {
        Object result = execScript("try { throw 42; } catch(e) { e+1 }");
        assertEquals(43.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testTryFinally() {
        Object result = execScript("var x=0; try { x=1; } finally { x=2; } x");
        assertEquals(2.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testTryCatchFinally() {
        Object result = execScript("var x=''; try { throw 'err'; } catch(e) { x+='caught'; } finally { x+='finally'; } x");
        assertEquals("caughtfinally", result);
    }

    @Test(timeout = TIMEOUT)
    public void testThrow() {
        try {
            execScript("throw 'error'");
            fail("Expected RhinoException");
        } catch (RhinoException e) {
            // expected
        }
    }

    @Test(timeout = TIMEOUT)
    public void testRethrow() {
        try {
            execScript("try { throw 1; } catch(e) { throw e+1; }");
            fail("Expected RhinoException");
        } catch (RhinoException e) {
            assertTrue(e.getMessage().contains("2"));
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = TIMEOUT)
    public void testArrayLiteral() {
        Object result = execScript("[1,2,3]");
        assertTrue(result instanceof NativeArray);
        NativeArray arr = (NativeArray) result;
        assertEquals(3, arr.getLength());
        assertEquals(1.0, arr.get(0, null));
        assertEquals(2.0, arr.get(1, null));
        assertEquals(3.0, arr.get(2, null));
    }

    @Test(timeout = TIMEOUT)
    public void testObjectLiteral() {
        Object result = execScript("({a:1, b:'hello'})");
        assertTrue(result instanceof NativeObject);
        NativeObject obj = (NativeObject) result;
        assertEquals(1.0, obj.get("a", null));
        assertEquals("hello", obj.get("b", null));
    }

    @Test(timeout = TIMEOUT)
    public void testRegExpLiteral() {
        Object result = execScript("/test/");
        assertTrue(result instanceof NativeRegExp);
    }

    @Test(timeout = TIMEOUT)
    public void testGenerator() {
        Object result = execScript("function* gen() { yield 1; yield 2; } var g = gen(); g.next().value");
        assertEquals(1.0, result);
    }

    // ==================== Additional Branch Coverage ====================

    @Test(timeout = TIMEOUT)
    public void testBinaryOperators() {
        assertEquals(5.0, execScript("2+3"));
        assertEquals(-1.0, execScript("2-3"));
        assertEquals(6.0, execScript("2*3"));
        assertEquals(2.0, execScript("5/2"));
        assertEquals(1.0, execScript("5%2"));
        assertEquals(1, execScript("2&3"));
        assertEquals(3, execScript("2|3"));
        assertEquals(1, execScript("2^3"));
        assertEquals(8, execScript("2<<2"));
        assertEquals(2, execScript("8>>2"));
        assertEquals(2, execScript("8>>>2"));
    }

    @Test(timeout = TIMEOUT)
    public void testUnaryOperators() {
        assertEquals(-5.0, execScript("-5"));
        assertEquals(5.0, execScript("+5"));
        assertEquals(false, execScript("!true"));
        assertEquals(-6, execScript("~5"));
        assertEquals("number", execScript("typeof 5"));
        assertEquals(undefined, execScript("void 0"));
    }

    @Test(timeout = TIMEOUT)
    public void testPropertyAccess() {
        assertEquals(2, execScript("var o={x:2}; o.x"));
        assertEquals(3, execScript("var o={x:{y:3}}; o.x.y"));
    }

    @Test(timeout = TIMEOUT)
    public void testFunctionCall() {
        assertEquals(9, execScript("function f(a,b){return a+b;} f(4,5)"));
    }

    @Test(timeout = TIMEOUT)
    public void testIfElse() {
        assertEquals(1, execScript("var x=0; if(true) x=1; else x=2; x"));
        assertEquals(2, execScript("var x=0; if(false) x=1; else x=2; x"));
    }

    @Test(timeout = TIMEOUT)
    public void testWhileLoop() {
        assertEquals(10, execScript("var i=0,sum=0; while(i<4){sum+=i; i++} sum"));
    }

    @Test(timeout = TIMEOUT)
    public void testSwitch() {
        assertEquals(2, execScript("var x=1; switch(x){case 1: 2; break; default: 3}"));
    }

    @Test(timeout = TIMEOUT)
    public void testWithStatement() {
        assertEquals(3, execScript("var o={x:3}; with(o) x"));
    }

    @Test(timeout = TIMEOUT)
    public void testLabelBreakContinue() {
        assertEquals(6, execScript("var sum=0; outer: for(var i=0;i<3;i++){ for(var j=0;j<3;j++){ if(j==1) continue outer; sum++; } } sum"));
    }

    @Test(timeout = TIMEOUT)
    public void testDebugger() {
        // debugger statement should not throw
        execScript("debugger;");
    }

    @Test(timeout = TIMEOUT)
    public void testReturnUndefined() {
        assertEquals(undefined, execScript("function f(){} f()"));
    }

    @Test(timeout = TIMEOUT)
    public void testReturnValue() {
        assertEquals(7, execScript("function f(){return 7} f()"));
    }

    @Test(timeout = TIMEOUT)
    public void testVarDeclaration() {
        assertEquals(5, execScript("var a=5; a"));
    }

    @Test(timeout = TIMEOUT)
    public void testConstDeclaration() {
        assertEquals(10, execScript("const a=10; a"));
    }

    @Test(timeout = TIMEOUT)
    public void testIncrementDecrement() {
        assertEquals(1, execScript("var a=0; a++"));
        assertEquals(2, execScript("var a=1; ++a"));
        assertEquals(1, execScript("var a=2; a--"));
        assertEquals(0, execScript("var a=1; --a"));
    }

    @Test(timeout = TIMEOUT)
    public void testCompoundAssignment() {
        assertEquals(5, execScript("var a=2; a+=3; a"));
        assertEquals(6, execScript("var a=2; a*=3; a"));
    }

    @Test(timeout = TIMEOUT)
    public void testDeleteProperty() {
        assertEquals(true, execScript("var o={x:1}; delete o.x"));
    }

    @Test(timeout = TIMEOUT)
    public void testInstanceOf() {
        assertEquals(true, execScript("[] instanceof Array"));
    }

    @Test(timeout = TIMEOUT)
    public void testInOperator() {
        assertEquals(true, execScript("'x' in {x:1}"));
    }

    @Test(timeout = TIMEOUT)
    public void testConditionalOperator() {
        assertEquals(1, execScript("true?1:2"));
        assertEquals(2, execScript("false?1:2"));
    }

    @Test(timeout = TIMEOUT)
    public void testCommaOperator() {
        assertEquals(2, execScript("(1,2)"));
    }

    @Test(timeout = TIMEOUT)
    public void testArrayAccess() {
        assertEquals(2, execScript("var a=[1,2,3]; a[1]"));
    }

    @Test(timeout = TIMEOUT)
    public void testNewExpression() {
        Object result = execScript("new String('test')");
        assertTrue(result instanceof NativeString);
    }

    @Test(timeout = TIMEOUT)
    public void testTypeofName() {
        assertEquals("undefined", execScript("typeof nonexistentVar"));
    }

    @Test(timeout = TIMEOUT)
    public void testSetPropOp() {
        assertEquals(3, execScript("var o={x:1}; o.x+=2; o.x"));
    }

    @Test(timeout = TIMEOUT)
    public void testSetElemOp() {
        assertEquals(5, execScript("var a=[1]; a[0]+=4; a[0]"));
    }

    @Test(timeout = TIMEOUT)
    public void testSetRefOp() {
        // Using __defineGetter__ to test set ref
        Object result = execScript("var o={}; o.__defineGetter__('x', function(){return 1;}); o.x");
        assertEquals(1.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testYield() {
        Object result = execScript("function* g(){ yield 1; } var it = g(); it.next().value");
        assertEquals(1.0, result);
    }

    @Test(timeout = TIMEOUT)
    public void testGeneratorEnd() {
        Object result = execScript("function* g(){ yield 1; return 2; } var it = g(); it.next(); it.next().value");
        assertEquals(2.0, result);
    }
}