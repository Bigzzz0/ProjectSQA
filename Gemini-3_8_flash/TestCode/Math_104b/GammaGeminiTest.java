package org.apache.commons.math.special;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.math.special.Gamma
 *
 * 1. logGamma(double x)
 *    - Branch: x is NaN or x <= 0.0 -> returns Double.NaN
 *    - Branch: x > 0.0 -> executes Lanczos series approximation
 *    - Boundaries: x = Double.NaN, x = -10.0, x = -1.0, x = 0.0, x = Double.MIN_VALUE,
 *                  x = 1.0, x = 2.0, x = 3.0, x = 4.0, x = 5.0, x = Double.MAX_VALUE
 *
 * 2. regularizedGammaP(double a, double x, double epsilon, int maxIterations)
 *    - Branch: a is NaN, x is NaN, a <= 0.0, x < 0.0 -> returns Double.NaN
 *    - Branch: x == 0.0 -> returns 0.0
 *    - Branch: (a >= 1.0 && x > a) -> computes 1.0 - regularizedGammaQ(a, x, ...)
 *    - Branch: series branch (a < 1.0 || x <= a):
 *        - While loop convergence (|an| <= epsilon) -> returns exp(...) * sum
 *        - While loop maxIterations exceeded (n >= maxIterations) -> throws MaxIterationsExceededException
 *    - Overload: regularizedGammaP(double a, double x) delegates with DEFAULT_EPSILON (10e-9)
 *
 * 3. regularizedGammaQ(double a, double x, double epsilon, int maxIterations)
 *    - Branch: a is NaN, x is NaN, a <= 0.0, x < 0.0 -> returns Double.NaN
 *    - Branch: x == 0.0 -> returns 1.0
 *    - Branch: (x < a || a < 1.0) -> computes 1.0 - regularizedGammaP(a, x, ...)
 *    - Branch: ContinuedFraction branch (a >= 1.0 && x >= a):
 *        - Evaluates ContinuedFraction anon class: getA(n, x) = 2n + 1 - a + x, getB(n, x) = n(a - n)
 *        - ContinuedFraction maxIterations exceeded -> throws MaxIterationsExceededException
 *    - Overload: regularizedGammaQ(double a, double x) delegates with DEFAULT_EPSILON (10e-9)
 *
 * 4. Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *    - Defect: DEFAULT_EPSILON was defined as 10e-9 (= 1e-8) instead of 10e-15 or 1e-14, causing
 *      premature series termination and numerical inaccuracy in Gamma.regularizedGammaP(1.0, 1.0).
 *    - Targeted Test: testRegularizedGammaPositivePositiveBug reveals this inaccuracy by asserting
 *      expected value 0.632120558828558 with high precision (delta <= 1e-14).
 * ====================================================================================================
 */
public class GammaGeminiTest {

    private static final double EPSILON = 1e-15;

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & Normal Operations
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLogGammaPositiveIntegers() {
        // Gamma(1) = 0! = 1 => log(Gamma(1)) = 0.0
        assertEquals(0.0, Gamma.logGamma(1.0), 1e-14);
        // Gamma(2) = 1! = 1 => log(Gamma(2)) = 0.0
        assertEquals(0.0, Gamma.logGamma(2.0), 1e-14);
        // Gamma(3) = 2! = 2 => log(Gamma(3)) = log(2)
        assertEquals(Math.log(2.0), Gamma.logGamma(3.0), 1e-14);
        // Gamma(4) = 3! = 6 => log(Gamma(4)) = log(6)
        assertEquals(Math.log(6.0), Gamma.logGamma(4.0), 1e-14);
        // Gamma(5) = 4! = 24 => log(Gamma(5)) = log(24)
        assertEquals(Math.log(24.0), Gamma.logGamma(5.0), 1e-14);
    }

    @Test(timeout = 4000)
    public void testLogGammaHalfIntegers() {
        // Gamma(0.5) = sqrt(PI) => log(Gamma(0.5)) = 0.5 * log(PI)
        double expected = 0.5 * Math.log(Math.PI);
        assertEquals(expected, Gamma.logGamma(0.5), 1e-14);

        // Gamma(1.5) = 0.5 * sqrt(PI) => log(Gamma(1.5)) = log(0.5) + 0.5 * log(PI)
        double expected15 = Math.log(0.5) + (0.5 * Math.log(Math.PI));
        assertEquals(expected15, Gamma.logGamma(1.5), 1e-14);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPZero() throws MathException {
        // When x == 0.0 and a > 0, P(a, 0) = 0.0
        assertEquals(0.0, Gamma.regularizedGammaP(1.0, 0.0), 1e-15);
        assertEquals(0.0, Gamma.regularizedGammaP(5.0, 0.0), 1e-15);
        assertEquals(0.0, Gamma.regularizedGammaP(0.5, 0.0), 1e-15);
        assertEquals(0.0, Gamma.regularizedGammaP(2.0, 0.0, 1e-10, 100), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQZero() throws MathException {
        // When x == 0.0 and a > 0, Q(a, 0) = 1.0
        assertEquals(1.0, Gamma.regularizedGammaQ(1.0, 0.0), 1e-15);
        assertEquals(1.0, Gamma.regularizedGammaQ(5.0, 0.0), 1e-15);
        assertEquals(1.0, Gamma.regularizedGammaQ(0.5, 0.0), 1e-15);
        assertEquals(1.0, Gamma.regularizedGammaQ(2.0, 0.0, 1e-10, 100), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPAndQSumToOne() throws MathException {
        // For any a > 0 and x >= 0, P(a, x) + Q(a, x) == 1.0
        double[][] testCases = {
            { 0.5, 0.2 },
            { 0.5, 1.5 },
            { 1.0, 2.0 },
            { 2.0, 1.0 },
            { 2.0, 2.0 },
            { 3.0, 5.0 },
            { 10.0, 12.0 }
        };

        for (double[] pair : testCases) {
            double a = pair[0];
            double x = pair[1];
            double p = Gamma.regularizedGammaP(a, x);
            double q = Gamma.regularizedGammaQ(a, x);
            assertEquals("P + Q must sum to 1.0 for a=" + a + ", x=" + x,
                         1.0, p + q, 1e-8);
        }
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPBranchConditions() throws MathException {
        // Branch 1: a >= 1.0 && x > a -> invokes regularizedGammaQ
        double p1 = Gamma.regularizedGammaP(2.0, 3.0, 1e-10, 1000);
        assertTrue(p1 > 0.0 && p1 < 1.0);

        // Branch 2: a >= 1.0 && x <= a -> calculates series directly
        double p2 = Gamma.regularizedGammaP(2.0, 1.0, 1e-10, 1000);
        assertTrue(p2 > 0.0 && p2 < 1.0);

        // Branch 3: a < 1.0 && x > a -> calculates series directly
        double p3 = Gamma.regularizedGammaP(0.5, 1.5, 1e-10, 1000);
        assertTrue(p3 > 0.0 && p3 < 1.0);

        // Branch 4: a < 1.0 && x <= a -> calculates series directly
        double p4 = Gamma.regularizedGammaP(0.5, 0.2, 1e-10, 1000);
        assertTrue(p4 > 0.0 && p4 < 1.0);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQBranchConditions() throws MathException {
        // Branch 1: x < a -> invokes regularizedGammaP
        double q1 = Gamma.regularizedGammaQ(3.0, 2.0, 1e-10, 1000);
        assertTrue(q1 > 0.0 && q1 < 1.0);

        // Branch 2: a < 1.0 (even if x >= a) -> invokes regularizedGammaP
        double q2 = Gamma.regularizedGammaQ(0.5, 1.0, 1e-10, 1000);
        assertTrue(q2 > 0.0 && q2 < 1.0);

        // Branch 3: a >= 1.0 && x >= a -> executes ContinuedFraction branch
        double q3 = Gamma.regularizedGammaQ(2.0, 2.0, 1e-10, 1000);
        assertTrue(q3 > 0.0 && q3 < 1.0);

        double q4 = Gamma.regularizedGammaQ(2.0, 4.0, 1e-10, 1000);
        assertTrue(q4 > 0.0 && q4 < 1.0);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLogGammaNaNAndNonPositive() {
        assertTrue(Double.isNaN(Gamma.logGamma(Double.NaN)));
        assertTrue(Double.isNaN(Gamma.logGamma(0.0)));
        assertTrue(Double.isNaN(Gamma.logGamma(-0.0)));
        assertTrue(Double.isNaN(Gamma.logGamma(-1.0)));
        assertTrue(Double.isNaN(Gamma.logGamma(-100.5)));
        assertTrue(Double.isNaN(Gamma.logGamma(Double.NEGATIVE_INFINITY)));
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPInvalidInputs() throws MathException {
        // a is NaN or x is NaN
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NaN)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(Double.NaN, Double.NaN)));

        // a <= 0.0
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(0.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(-1.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(-0.001, 1.0)));

        // x < 0.0
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, -0.0001)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, -1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NEGATIVE_INFINITY)));

        // Parameterized overload check
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(0.0, 1.0, 1e-10, 100)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, -1.0, 1e-10, 100)));
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQInvalidInputs() throws MathException {
        // a is NaN or x is NaN
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NaN)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, Double.NaN)));

        // a <= 0.0
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(0.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(-1.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(-0.001, 1.0)));

        // x < 0.0
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, -0.0001)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, -1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NEGATIVE_INFINITY)));

        // Parameterized overload check
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(0.0, 1.0, 1e-10, 100)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, -1.0, 1e-10, 100)));
    }

    @Test(timeout = 4000)
    public void testLogGammaVeryLargeValue() {
        double result = Gamma.logGamma(1000.0);
        assertFalse(Double.isNaN(result));
        assertTrue(result > 0.0);
        assertFalse(Double.isInfinite(result));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J bug:
     * GammaTest::testRegularizedGammaPositivePositive
     * expected:<0.632120558828558> but was:<0.6321205587649603>
     *
     * In the unpatched version, DEFAULT_EPSILON = 10e-9 (which evaluates to 1e-8),
     * causing premature termination of the series expansion and resulting in 0.6321205587649603.
     * The true mathematical value of regularizedGammaP(1.0, 1.0) = 1 - e^(-1) is approximately
     * 0.6321205588285577.
     */
    @Test(timeout = 4000)
    public void testRegularizedGammaPositivePositiveBug() throws MathException {
        double actual = Gamma.regularizedGammaP(1.0, 1.0);
        assertEquals("Reveals default epsilon precision defect in regularizedGammaP(1.0, 1.0)",
                     0.632120558828558, actual, 1e-14);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQPositivePositiveBug() throws MathException {
        double actual = Gamma.regularizedGammaQ(1.0, 1.0);
        assertEquals("Reveals default epsilon precision defect in regularizedGammaQ(1.0, 1.0)",
                     1.0 - 0.632120558828558, actual, 1e-14);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaPSeriesMaxIterationsExceeded() throws MathException {
        // Force series branch: a >= 1.0 && x <= a, with maxIterations = 1
        Gamma.regularizedGammaP(2.0, 1.0, 1e-15, 1);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPSeriesMaxIterationsExceededExplicit() {
        try {
            Gamma.regularizedGammaP(2.0, 1.0, 1e-15, 1);
            fail("Expected MaxIterationsExceededException was not thrown");
        } catch (MaxIterationsExceededException e) {
            assertEquals(1, e.getMaxIterations());
        } catch (MathException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000, expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaQContinuedFractionMaxIterationsExceeded() throws MathException {
        // Force continued fraction branch: a >= 1.0 && x >= a, with maxIterations = 1
        Gamma.regularizedGammaQ(2.0, 2.0, 1e-15, 1);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQContinuedFractionMaxIterationsExceededExplicit() {
        try {
            Gamma.regularizedGammaQ(2.0, 3.0, 1e-15, 1);
            fail("Expected MaxIterationsExceededException was not thrown");
        } catch (MaxIterationsExceededException e) {
            assertEquals(1, e.getMaxIterations());
        } catch (MathException e) {
            fail("Unexpected exception: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Architectural Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<Gamma> constructor = Gamma.class.getDeclaredConstructor();
        assertTrue("Constructor must be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Gamma instance = constructor.newInstance();
        assertNotNull("Instance should be successfully created", instance);
        assertTrue("Gamma should implement Serializable", instance instanceof java.io.Serializable);
    }
}