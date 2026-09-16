package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationEqual
 *
 * 1. Targeted Defect (Defects4J Ground Truth):
 *    - Defect: CoreOperationTest::testNan -> Evaluating <$nan = $nan> expected:<false> but was:<true>.
 *    - Root Cause: CoreOperationCompare/CoreOperationEqual logic evaluated Double.NaN equality using standard
 *      object equality (Double.equals), where Double.NaN.equals(Double.NaN) yields true, violating the IEEE 754
 *      and W3C XPath 1.0 specifications stating that NaN = NaN must evaluate to false.
 *    - Targeted Branches: equal(context, args[0], args[1]) where args contain NaN (both sides or single side).
 *
 * 2. Equivalence Partitions & Boundary Conditions:
 *    - Partition A (Core Logic):
 *      * Equality of identical numbers (1 == 1) -> Boolean.TRUE
 *      * Inequality of different numbers (1 == 2) -> Boolean.FALSE
 *      * Equality of identical strings ("foo" == "foo") -> Boolean.TRUE
 *      * Inequality of different strings ("foo" == "bar") -> Boolean.FALSE
 *      * String-to-number automatic type coercion ("42" == 42) -> Boolean.TRUE
 *      * Boolean-to-number/string evaluation
 *    - Partition B (BVA & Extremes):
 *      * Positive & Negative Infinity (INF == INF, -INF == INF)
 *      * Zero boundary: -0.0 == +0.0 -> true
 *      * Extreme values: Double.MAX_VALUE, Double.MIN_VALUE
 *      * Empty strings: "" == "" -> true, "" == "a" -> false
 *    - Partition C (Defect-Targeted Zone):
 *      * Direct evaluation: NaN == NaN -> false
 *      * Contextual evaluation: $nan == $nan -> false
 *      * Mixed evaluation: NaN == 0.0 -> false, 1.0 == NaN -> false, NaN == "NaN" -> false
 *    - Partition D (Defensive & Exception Paths):
 *      * Null expression arguments causing expected NullPointerException during computeValue
 *    - Partition E (Contract & Metadata):
 *      * getSymbol() contract check ("=")
 *      * isSymmetric() contract check (true)
 *      * toString() representation formatting
 */
public class CoreOperationEqualGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueNumericEquality() {
        Constant c1 = new Constant(Double.valueOf(42.0));
        Constant c2 = new Constant(Double.valueOf(42.0));
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        Object result = op.computeValue(null);
        assertSame("Identical numbers must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueNumericInequality() {
        Constant c1 = new Constant(Double.valueOf(42.0));
        Constant c2 = new Constant(Double.valueOf(43.0));
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        Object result = op.computeValue(null);
        assertSame("Distinct numbers must evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringEquality() {
        Constant c1 = new Constant("jxpath");
        Constant c2 = new Constant("jxpath");
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        Object result = op.computeValue(null);
        assertSame("Identical strings must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringInequality() {
        Constant c1 = new Constant("alpha");
        Constant c2 = new Constant("beta");
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        Object result = op.computeValue(null);
        assertSame("Distinct strings must evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringNumberCoercion() {
        Constant c1 = new Constant("123.45");
        Constant c2 = new Constant(Double.valueOf(123.45));
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        Object result = op.computeValue(null);
        assertSame("Numeric string and number should coerce and evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeValueZeroAndNegativeZero() {
        Constant posZero = new Constant(Double.valueOf(0.0));
        Constant negZero = new Constant(Double.valueOf(-0.0));
        CoreOperationEqual op = new CoreOperationEqual(posZero, negZero);

        Object result = op.computeValue(null);
        assertSame("+0.0 and -0.0 must evaluate to equal per IEEE 754", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValuePositiveInfinity() {
        Constant inf1 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant inf2 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        CoreOperationEqual op = new CoreOperationEqual(inf1, inf2);

        Object result = op.computeValue(null);
        assertSame("+Infinity == +Infinity must evaluate to Boolean.TRUE", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValuePositiveAndNegativeInfinity() {
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        CoreOperationEqual op = new CoreOperationEqual(posInf, negInf);

        Object result = op.computeValue(null);
        assertSame("+Infinity == -Infinity must evaluate to Boolean.FALSE", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueDoubleExtremes() {
        Constant maxVal = new Constant(Double.valueOf(Double.MAX_VALUE));
        Constant minVal = new Constant(Double.valueOf(Double.MIN_VALUE));
        CoreOperationEqual op1 = new CoreOperationEqual(maxVal, maxVal);
        CoreOperationEqual op2 = new CoreOperationEqual(maxVal, minVal);

        assertSame(Boolean.TRUE, op1.computeValue(null));
        assertSame(Boolean.FALSE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeValueEmptyStrings() {
        Constant empty1 = new Constant("");
        Constant empty2 = new Constant("");
        Constant nonEmpty = new Constant(" ");
        CoreOperationEqual op1 = new CoreOperationEqual(empty1, empty2);
        CoreOperationEqual op2 = new CoreOperationEqual(empty1, nonEmpty);

        assertSame(Boolean.TRUE, op1.computeValue(null));
        assertSame(Boolean.FALSE, op2.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (NaN Equalities)
    // =========================================================================

    /**
     * Defects4J Ground Truth targeted test.
     * In XPath 1.0, NaN is not equal to any value, including NaN itself.
     * The defective implementation returns true for NaN = NaN.
     */
    @Test(timeout = 4000)
    public void testDefectBothOperandsNaNReturnsFalse() {
        Constant nan1 = new Constant(Double.valueOf(Double.NaN));
        Constant nan2 = new Constant(Double.valueOf(Double.NaN));
        CoreOperationEqual op = new CoreOperationEqual(nan1, nan2);

        Object result = op.computeValue(null);
        assertEquals("Evaluating NaN = NaN expected:<false> but was:<true>", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectLeftOperandNaNReturnsFalse() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        Constant zero = new Constant(Double.valueOf(0.0));
        CoreOperationEqual op = new CoreOperationEqual(nan, zero);

        Object result = op.computeValue(null);
        assertEquals("Evaluating NaN = 0.0 expected:<false>", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectRightOperandNaNReturnsFalse() {
        Constant number = new Constant(Double.valueOf(100.5));
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        CoreOperationEqual op = new CoreOperationEqual(number, nan);

        Object result = op.computeValue(null);
        assertEquals("Evaluating 100.5 = NaN expected:<false>", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNaNStringComparisonReturnsFalse() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        Constant nanString = new Constant("NaN");
        CoreOperationEqual op = new CoreOperationEqual(nan, nanString);

        Object result = op.computeValue(null);
        assertEquals("Evaluating NaN = 'NaN' expected:<false>", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNanViaJXPathContextEvaluation() {
        JXPathContext context = JXPathContext.newContext(new HashMap<String, Object>());
        context.getVariables().declareVariable("nan", Double.valueOf(Double.NaN));

        Object result = context.getValue("$nan = $nan");
        assertEquals("Evaluating <$nan = $nan> expected:<false> but was:<true>", Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testComputeValueWithNullOperandThrowsNpe() {
        CoreOperationEqual op = new CoreOperationEqual(null, new Constant(1));
        op.computeValue(null);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullArguments() {
        CoreOperationEqual op = new CoreOperationEqual(null, null);
        assertNotNull("Instance should be successfully constructed", op);
        Expression[] args = op.getArguments();
        assertNotNull("Arguments array must not be null", args);
        assertEquals("Should have exactly 2 argument slots", 2, args.length);
        assertNull("Arg1 should be null", args[0]);
        assertNull("Arg2 should be null", args[1]);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSymbol() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(1), new Constant(2));
        assertEquals("Operation symbol for CoreOperationEqual must be '='", "=", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testIsSymmetric() {
        CoreOperationEqual op = new CoreOperationEqual(new Constant(1), new Constant(2));
        assertTrue("Equality operation must be symmetric", op.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        Constant left = new Constant(Double.valueOf(10.0));
        Constant right = new Constant(Double.valueOf(20.0));
        CoreOperationEqual op = new CoreOperationEqual(left, right);

        String repr = op.toString();
        assertNotNull("toString() must not return null", repr);
        assertTrue("toString() must contain the '=' symbol", repr.contains("="));
        assertTrue("toString() must contain left operand", repr.contains("10"));
        assertTrue("toString() must contain right operand", repr.contains("20"));
    }
}