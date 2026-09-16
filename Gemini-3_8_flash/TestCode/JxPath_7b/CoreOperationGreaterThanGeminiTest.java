package org.apache.commons.jxpath.ri.compiler;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan
 *
 * ---------------------------------------------------------------------------------------------------------------
 * Branch / Condition Coverage Target               | Expected Behavior                   | Test Method
 * ---------------------------------------------------------------------------------------------------------------
 * getSymbol()                                      | Returns string ">"                  | testGetSymbol
 * computeValue: l > r (true path)                  | Returns Boolean.TRUE                | testComputeValueTrue
 * computeValue: l <= r (false path: l < r)         | Returns Boolean.FALSE               | testComputeValueFalseLessThan
 * computeValue: l <= r (false path: l == r)        | Returns Boolean.FALSE               | testComputeValueFalseEqual
 * computeValue: Boundary & NaN (l is NaN)          | Returns Boolean.FALSE               | testComputeValueWithNaNLeft
 * computeValue: Boundary & NaN (r is NaN)          | Returns Boolean.FALSE               | testComputeValueWithNaNRight
 * computeValue: Boundary & NaN (both NaN)          | Returns Boolean.FALSE               | testComputeValueWithBothNaN
 * computeValue: Boundary Infinities                | POS_INF > NEG_INF -> TRUE           | testComputeValueInfinities
 * computeValue: Boundary Signed Zeros              | 0.0 > -0.0 -> FALSE                 | testComputeValueSignedZeros
 * computeValue: String numeric coercion            | "10.5" > "9.2" -> TRUE              | testComputeValueStringCoercion
 * computeValue: Non-numeric string coercion        | "abc" > 0 -> FALSE (NaN > 0 is F)   | testComputeValueNonNumericString
 * computeValue: Boolean coercion                   | true (1) > false (0) -> TRUE        | testComputeValueBooleanCoercion
 * computeValue: Null expression in arguments       | Throws NullPointerException         | testComputeValueWithNullArgs
 * CoreOperation Contract: isSymmetric()            | Returns false (relational op)       | testIsSymmetric
 * Defect-Targeted Branch: NodeSet > Number         | Array {-1, 1} > 0 -> TRUE (XPath)   | testDefectNodeSetOperations_ArrayGreaterThanZero
 * Defect-Targeted Branch: NodeSet > Number (all <) | Array {-5, -1} > 0 -> FALSE         | testDefectNodeSetOperations_AllElementsLessThan
 * Defect-Targeted Branch: Number < NodeSet (direct)| Direct AST node-set comparison      | testDirectAstNodeSetEvaluation
 * ---------------------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.RootContext;

public class CoreOperationGreaterThanGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(2), new Constant(1));
        assertEquals("Exact operator symbol must be '>'", ">", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testComputeValueTrue() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(10), new Constant(5));
        Object result = op.computeValue(null);
        assertSame("10 > 5 must return Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueFalseLessThan() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(3), new Constant(7));
        Object result = op.computeValue(null);
        assertSame("3 > 7 must return Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueFalseEqual() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(42), new Constant(42));
        Object result = op.computeValue(null);
        assertSame("42 > 42 must return Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueDecimals() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(new Constant(1.0001), new Constant(1.0));
        assertSame("1.0001 > 1.0 must return Boolean.TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(new Constant(1.0), new Constant(1.0001));
        assertSame("1.0 > 1.0001 must return Boolean.FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueStringCoercion() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant("20.5"), new Constant("3.1"));
        Object result = op.computeValue(null);
        assertSame("'20.5' > '3.1' must evaluate numerically to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueBooleanCoercion() {
        // XPath specs: true evaluates to 1.0, false evaluates to 0.0
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new CoreOperationGreaterThan(new Constant(2), new Constant(1)), // true (1.0)
            new CoreOperationGreaterThan(new Constant(1), new Constant(2))  // false (0.0)
        );
        assertSame("true (1.0) > false (0.0) must be Boolean.TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new CoreOperationGreaterThan(new Constant(1), new Constant(2)), // false (0.0)
            new CoreOperationGreaterThan(new Constant(2), new Constant(1))  // true (1.0)
        );
        assertSame("false (0.0) > true (1.0) must be Boolean.FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueWithNaNLeft() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(Double.NaN), new Constant(0.0));
        assertSame("NaN > 0.0 must be Boolean.FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueWithNaNRight() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(0.0), new Constant(Double.NaN));
        assertSame("0.0 > NaN must be Boolean.FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueWithBothNaN() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(Double.NaN), new Constant(Double.NaN));
        assertSame("NaN > NaN must be Boolean.FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueInfinities() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new Constant(Double.POSITIVE_INFINITY),
            new Constant(Double.MAX_VALUE)
        );
        assertSame("+Infinity > Double.MAX_VALUE must be Boolean.TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new Constant(Double.NEGATIVE_INFINITY),
            new Constant(-Double.MAX_VALUE)
        );
        assertSame("-Infinity > -Double.MAX_VALUE must be Boolean.FALSE", Boolean.FALSE, op2.computeValue(null));

        CoreOperationGreaterThan op3 = new CoreOperationGreaterThan(
            new Constant(Double.POSITIVE_INFINITY),
            new Constant(Double.NEGATIVE_INFINITY)
        );
        assertSame("+Infinity > -Infinity must be Boolean.TRUE", Boolean.TRUE, op3.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueSignedZeros() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(new Constant(0.0), new Constant(-0.0));
        assertSame("0.0 > -0.0 must be Boolean.FALSE", Boolean.FALSE, op1.computeValue(null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(new Constant(-0.0), new Constant(0.0));
        assertSame("-0.0 > 0.0 must be Boolean.FALSE", Boolean.FALSE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueNonNumericString() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant("notANumber"), new Constant(0));
        assertSame("'notANumber' coerced to NaN > 0 must be Boolean.FALSE", Boolean.FALSE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueMinMaxDoubles() {
        CoreOperationGreaterThan op1 = new CoreOperationGreaterThan(
            new Constant(Double.MAX_VALUE),
            new Constant(Double.MIN_VALUE)
        );
        assertSame("Double.MAX_VALUE > Double.MIN_VALUE must be Boolean.TRUE", Boolean.TRUE, op1.computeValue(null));

        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(
            new Constant(Double.MIN_VALUE),
            new Constant(0.0)
        );
        assertSame("Double.MIN_VALUE > 0.0 must be Boolean.TRUE", Boolean.TRUE, op2.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
    // Targeting: CoreOperationTest::testNodeSetOperations -> <$array > 0>
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectNodeSetOperations_ArrayGreaterThanZero() {
        // XPath 1.0 Section 3.4: A comparison between a node-set and a number is true
        // if and only if there exists ANY node in the node-set for which the comparison holds true.
        // Array: [-1, 1]. Since 1 > 0 is true, '$array > 0' MUST evaluate to true.
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { -1, 1 });

        Object result = context.getValue("$array > 0");
        assertEquals("Evaluating <$array > 0> where array=[-1, 1] must yield Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperations_ListGreaterThanZero() {
        // Ensure List collections also adhere to the relational node-set specification
        JXPathContext context = JXPathContext.newContext(new Object());
        List<Integer> list = Arrays.asList(-10, 0, 5);
        context.getVariables().declareVariable("list", list);

        Object result = context.getValue("$list > 2");
        assertEquals("Evaluating <$list > 2> where list=[-10, 0, 5] must yield Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperations_AllElementsLessThan() {
        // All elements are strictly less than or equal to 0, so result must be false
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { -5, -1, 0 });

        Object result = context.getValue("$array > 0");
        assertEquals("Evaluating <$array > 0> where array=[-5, -1, 0] must yield Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNodeSetOperations_EmptyCollection() {
        // Node-set is empty -> no node satisfies the condition -> must be false
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("emptyArray", new Integer[0]);

        Object result = context.getValue("$emptyArray > 0");
        assertEquals("Evaluating <$emptyArray > 0> on empty array must yield Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDirectAstNodeSetEvaluation() {
        // Test direct execution against CoreOperationGreaterThan AST instance with a variable reference
        JXPathContextReferenceImpl context = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new Integer[] { 0, 10 });

        CoreOperationGreaterThan op = new CoreOperationGreaterThan(
            new VariableReference(new QName("array")),
            new Constant(5)
        );

        RootContext rootContext = context.getEvalContext();
        Object result = op.computeValue(rootContext);
        assertEquals("Direct AST execution of $array > 5 with array=[0, 10] must yield Boolean.TRUE", Boolean.TRUE, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testComputeValueWithNullArgs() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(null, null);
        op.computeValue(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testComputeValueWithSingleNullArg() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), null);
        op.computeValue(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsSymmetric() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertFalse("CoreOperationGreaterThan is non-symmetric", op.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testGetPrecedence() {
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        // Relational expressions have a defined non-negative precedence level
        assertTrue("Operator precedence must be greater than or equal to 0", op.getPrecedence() >= 0);
    }

    @Test(timeout = 4000)
    public void testConstructorAndArgumentsIntegrity() {
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(20);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(c1, c2);

        assertNotNull("Arguments array must not be null", op.getArguments());
        assertEquals("Arguments count must be exactly 2", 2, op.getArguments().length);
        assertSame("Argument 0 must match first expression passed", c1, op.getArguments()[0]);
        assertSame("Argument 1 must match second expression passed", c2, op.getArguments()[1]);
    }
}