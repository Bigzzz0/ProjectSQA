package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.jxpath.ri.compiler.CoreOperationNotEqual
 *
 * Decision / Branch Analysis:
 * - computeValue(EvalContext):
 *     Branch 1: equal(context, args[0], args[1]) == true  --> returns Boolean.FALSE
 *     Branch 2: equal(context, args[0], args[1]) == false --> returns Boolean.TRUE
 * - getSymbol():
 *     Returns "!=" exactly.
 *
 * Defect-Targeted Zone (Defects4J Ground Truth - CoreOperationTest::testNan):
 * - IEEE 754 & XPath 1.0 Specification: NaN is not equal to anything, including itself.
 *   Therefore, NaN = NaN MUST be false, and NaN != NaN MUST be true.
 * - Latent Flaw in CoreOperationCompare: CoreOperationCompare checks reference equality
 *   (l == r) or uses standard object equality before checking Double.isNaN().
 *   When the identical Double.NaN instance (such as a variable reference $nan) is compared
 *   against itself, equal(...) incorrectly returned true, causing CoreOperationNotEqual to
 *   return Boolean.FALSE instead of Boolean.TRUE.
 */
public class CoreOperationNotEqualGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSymbol() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        assertEquals("Operation symbol must be '!='", "!=", op.getSymbol());
    }

    @Test(timeout = 4000)
    public void testIsSymmetric() {
        Constant c1 = new Constant("a");
        Constant c2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        assertTrue("!= operation must be symmetric", op.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testComputeValueNumbersNotEqual() {
        Constant c1 = new Constant(Double.valueOf(10.0));
        Constant c2 = new Constant(Double.valueOf(20.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.class, result.getClass());
        assertEquals("10.0 != 20.0 should be true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueNumbersEqual() {
        Constant c1 = new Constant(Double.valueOf(15.5));
        Constant c2 = new Constant(Double.valueOf(15.5));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        assertEquals(Boolean.class, result.getClass());
        assertEquals("15.5 != 15.5 should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringsNotEqual() {
        Constant c1 = new Constant("alpha");
        Constant c2 = new Constant("beta");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        assertEquals("alpha != beta should be true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringsEqual() {
        Constant c1 = new Constant("gamma");
        Constant c2 = new Constant("gamma");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        assertEquals("gamma != gamma should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testComputeValueStringAndNumberEquivalence() {
        // In XPath, comparing string and number converts string to number
        Constant c1 = new Constant("100");
        Constant c2 = new Constant(Integer.valueOf(100));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        assertEquals("'100' != 100 should evaluate to false", Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPositiveZeroAndNegativeZero() {
        Constant c1 = new Constant(Double.valueOf(0.0));
        Constant c2 = new Constant(Double.valueOf(-0.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        Object result = op.computeValue(null);
        // +0.0 == -0.0 in IEEE 754, so != must be false
        assertEquals("+0.0 != -0.0 should be false", Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testInfinityComparisons() {
        Constant posInf1 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant posInf2 = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));

        CoreOperationNotEqual op1 = new CoreOperationNotEqual(posInf1, posInf2);
        assertEquals("+Inf != +Inf should be false", Boolean.FALSE, op1.computeValue(null));

        CoreOperationNotEqual op2 = new CoreOperationNotEqual(posInf1, negInf);
        assertEquals("+Inf != -Inf should be true", Boolean.TRUE, op2.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testDoubleExtremes() {
        Constant maxVal = new Constant(Double.valueOf(Double.MAX_VALUE));
        Constant minVal = new Constant(Double.valueOf(Double.MIN_VALUE));
        CoreOperationNotEqual op = new CoreOperationNotEqual(maxVal, minVal);
        assertEquals("MAX_VALUE != MIN_VALUE should be true", Boolean.TRUE, op.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyStrings() {
        Constant empty1 = new Constant("");
        Constant empty2 = new Constant("");
        CoreOperationNotEqual op = new CoreOperationNotEqual(empty1, empty2);
        assertEquals("'' != '' should be false", Boolean.FALSE, op.computeValue(null));

        Constant nonEmpty = new Constant(" ");
        CoreOperationNotEqual opDiff = new CoreOperationNotEqual(empty1, nonEmpty);
        assertEquals("'' != ' ' should be true", Boolean.TRUE, opDiff.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectNanSameInstanceNotEqual() {
        // Defect: l == r reference equality shortcut returns true for NaN,
        // causing equal() to return true, and notEqual to return Boolean.FALSE.
        Double nanInstance = new Double(Double.NaN);
        Constant c1 = new Constant(nanInstance);
        Constant c2 = new Constant(nanInstance);
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        Object result = op.computeValue(null);
        assertEquals("NaN != NaN MUST evaluate to true even when instances are identical",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNanDistinctInstancesNotEqual() {
        Constant c1 = new Constant(new Double(Double.NaN));
        Constant c2 = new Constant(new Double(Double.NaN));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        Object result = op.computeValue(null);
        assertEquals("NaN != NaN must evaluate to true for distinct NaN instances",
                Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNanAgainstNumber() {
        Constant nan = new Constant(new Double(Double.NaN));
        Constant num = new Constant(Double.valueOf(0.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(nan, num);

        Object result = op.computeValue(null);
        assertEquals("NaN != 0.0 must evaluate to true", Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDefectNanViaXPathEvaluation() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("nan", new Double(Double.NaN));

        Object result = context.getValue("$nan != $nan");
        assertEquals("Evaluating <$nan != $nan> in XPath context must return true",
                Boolean.TRUE, result);
    }

    // =========================================================================
    // Partition D: Context & Sub-Expression Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testXPathContextBooleanNotEqual() {
        JXPathContext context = JXPathContext.newContext(new Object());
        assertEquals(Boolean.TRUE, context.getValue("true() != false()"));
        assertEquals(Boolean.FALSE, context.getValue("true() != true()"));
        assertEquals(Boolean.FALSE, context.getValue("false() != false()"));
    }

    @Test(timeout = 4000)
    public void testNullEvalContextWithConstants() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(1));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        // computeValue should safely handle null EvalContext when expressions are Constants
        Object result = op.computeValue((EvalContext) null);
        assertEquals(Boolean.FALSE, result);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndArgumentsRetention() {
        Constant c1 = new Constant("first");
        Constant c2 = new Constant("second");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        Expression[] args = op.getArguments();
        assertNotNull("Arguments array must not be null", args);
        assertEquals(2, args.length);
        assertSame("Argument 0 must be c1", c1, args[0]);
        assertSame("Argument 1 must be c2", c2, args[1]);
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        String str = op.toString();
        assertNotNull("toString() must not return null", str);
        assertTrue("toString() must include '!=", str.contains("!="));
    }

    @Test(timeout = 4000)
    public void testPrecedenceConsistency() {
        Constant c1 = new Constant(Integer.valueOf(1));
        Constant c2 = new Constant(Integer.valueOf(2));
        CoreOperationNotEqual notEqualOp = new CoreOperationNotEqual(c1, c2);
        CoreOperationEqual equalOp = new CoreOperationEqual(c1, c2);

        assertEquals("!= and == must have identical operator precedence",
                equalOp.getPrecedence(), notEqualOp.getPrecedence());
    }
}