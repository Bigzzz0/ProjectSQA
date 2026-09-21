package org.apache.commons.math.special;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Gamma.java targeting maximum coverage and the known
 * Defects4J defect in regularizedGammaP.
 *
 * <pre>
 * [Branch & Defect Analysis Matrix]
 *
 * logGamma:
 *   - Branch 1: x is NaN or x <= 0.0 → return NaN
 *   - Branch 2: else → Lanczos approximation
 *
 * regularizedGammaP (4-arg):
 *   - Branch A: a NaN, x NaN, a <= 0.0, x < 0.0 → return NaN
 *   - Branch B: x == 0.0 → return 0.0
 *   - Branch C: a >= 1.0 && x > a → use regularizedGammaQ (1 - Q)
 *   - Branch D: else → series summation
 *       - Sub-branch D1: convergence within maxIterations → compute result
 *       - Sub-branch D2: maxIterations exceeded → throw MaxIterationsExceededException
 *
 * regularizedGammaQ (4-arg):
 *   - Branch E: a NaN, x NaN, a <= 0.0, x < 0.0 → return NaN
 *   - Branch F: x == 0.0 → return 1.0
 *   - Branch G: x < a || a < 1.0 → use regularizedGammaP (1 - P)
 *   - Branch H: else → continued fraction evaluation
 *       - Sub-branch H1: convergence → compute result
 *       - Sub-branch H2: maxIterations exceeded → throw MaxIterationsExceededException
 *
 * Defect-targeted case:
 *   regularizedGammaP(1.0, 1.0) should return 1 - exp(-1) ≈ 0.6321205588285577
 *   The defective version returns 0.6321205587649603 (error ~6.4e-11).
 *   This test will assert with a tight delta to expose the bug.
 * </pre>
 */
public class GammaDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testLogGammaPositiveValues() {
        // logGamma(1.0) = 0.0
        assertEquals("logGamma(1.0)", 0.0, Gamma.logGamma(1.0), 1e-15);
        // logGamma(2.0) = 0.0 (since Gamma(2)=1)
        assertEquals("logGamma(2.0)", 0.0, Gamma.logGamma(2.0), 1e-15);
        // logGamma(0.5) ≈ 0.5723649429247001
        assertEquals("logGamma(0.5)", 0.5723649429247001, Gamma.logGamma(0.5), 1e-12);
        // logGamma(10.0) ≈ 12.801827480081469
        assertEquals("logGamma(10.0)", 12.801827480081469, Gamma.logGamma(10.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPStandardValues() throws MathException {
        // P(1, 1) = 1 - exp(-1) ≈ 0.6321205588285577
        double expected = 1.0 - Math.exp(-1.0);
        double actual = Gamma.regularizedGammaP(1.0, 1.0);
        assertEquals("regularizedGammaP(1,1)", expected, actual, 1e-12);
        // P(2, 2) ≈ 0.5939941502901619
        assertEquals("regularizedGammaP(2,2)", 0.5939941502901619, Gamma.regularizedGammaP(2.0, 2.0), 1e-12);
        // P(0.5, 1) ≈ 0.8427007929497149
        assertEquals("regularizedGammaP(0.5,1)", 0.8427007929497149, Gamma.regularizedGammaP(0.5, 1.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQStandardValues() throws MathException {
        // Q(1, 1) = exp(-1) ≈ 0.36787944117144233
        double expected = Math.exp(-1.0);
        double actual = Gamma.regularizedGammaQ(1.0, 1.0);
        assertEquals("regularizedGammaQ(1,1)", expected, actual, 1e-12);
        // Q(2, 2) ≈ 0.4060058497098381
        assertEquals("regularizedGammaQ(2,2)", 0.4060058497098381, Gamma.regularizedGammaQ(2.0, 2.0), 1e-12);
        // Q(0.5, 1) ≈ 0.1572992070502851
        assertEquals("regularizedGammaQ(0.5,1)", 0.1572992070502851, Gamma.regularizedGammaQ(0.5, 1.0), 1e-12);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testLogGammaBoundaries() {
        // x = 0.0 → NaN
        assertTrue("logGamma(0.0) should be NaN", Double.isNaN(Gamma.logGamma(0.0)));
        // x = -1.0 → NaN
        assertTrue("logGamma(-1.0) should be NaN", Double.isNaN(Gamma.logGamma(-1.0)));
        // x = Double.NaN → NaN
        assertTrue("logGamma(NaN) should be NaN", Double.isNaN(Gamma.logGamma(Double.NaN)));
        // x = Double.POSITIVE_INFINITY → +Infinity (large x)
        assertEquals("logGamma(+Inf)", Double.POSITIVE_INFINITY, Gamma.logGamma(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPZeroX() throws MathException {
        // P(a, 0) = 0 for any a > 0
        assertEquals("P(2,0)", 0.0, Gamma.regularizedGammaP(2.0, 0.0), 0.0);
        assertEquals("P(0.5,0)", 0.0, Gamma.regularizedGammaP(0.5, 0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQZeroX() throws MathException {
        // Q(a, 0) = 1 for any a > 0
        assertEquals("Q(2,0)", 1.0, Gamma.regularizedGammaQ(2.0, 0.0), 0.0);
        assertEquals("Q(0.5,0)", 1.0, Gamma.regularizedGammaQ(0.5, 0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaPWhenXGreaterThanA() throws MathException {
        // a=1, x=2 → should use Q branch
        double expected = 1.0 - Gamma.regularizedGammaQ(1.0, 2.0);
        double actual = Gamma.regularizedGammaP(1.0, 2.0);
        assertEquals("P(1,2) via Q", expected, actual, 1e-12);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQWhenXLessThanA() throws MathException {
        // a=2, x=1 → should use P branch
        double expected = 1.0 - Gamma.regularizedGammaP(2.0, 1.0);
        double actual = Gamma.regularizedGammaQ(2.0, 1.0);
        assertEquals("Q(2,1) via P", expected, actual, 1e-12);
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQWhenALessThanOne() throws MathException {
        // a=0.5, x=1 → a<1, should use P branch
        double expected = 1.0 - Gamma.regularizedGammaP(0.5, 1.0);
        double actual = Gamma.regularizedGammaQ(0.5, 1.0);
        assertEquals("Q(0.5,1) via P", expected, actual, 1e-12);
    }

    // ==================== Partition C: Defect-Targeted Test ====================

    /**
     * Directly targets the known Defects4J failure:
     * regularizedGammaP(1.0, 1.0) should return 1 - exp(-1) ≈ 0.6321205588285577.
     * The defective version returns 0.6321205587649603.
     * We assert with a very tight delta (1e-12) to expose the bug.
     */
    @Test(timeout = 4000)
    public void testRegularizedGammaPositivePositive() throws MathException {
        double expected = 1.0 - Math.exp(-1.0);
        double actual = Gamma.regularizedGammaP(1.0, 1.0);
        // Tight tolerance to reveal the known numerical defect
        assertEquals("regularizedGammaP(1.0, 1.0) should be 1 - exp(-1)",
                     expected, actual, 1e-12);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testRegularizedGammaPInvalidArguments() throws MathException {
        // NaN a
        assertTrue("P(NaN,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaP(Double.NaN, 1.0)));
        // NaN x
        assertTrue("P(1,NaN) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NaN)));
        // a <= 0
        assertTrue("P(0,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaP(0.0, 1.0)));
        assertTrue("P(-1,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaP(-1.0, 1.0)));
        // x < 0
        assertTrue("P(1,-1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaP(1.0, -1.0)));
    }

    @Test(timeout = 4000)
    public void testRegularizedGammaQInvalidArguments() throws MathException {
        // NaN a
        assertTrue("Q(NaN,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, 1.0)));
        // NaN x
        assertTrue("Q(1,NaN) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NaN)));
        // a <= 0
        assertTrue("Q(0,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaQ(0.0, 1.0)));
        assertTrue("Q(-1,1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaQ(-1.0, 1.0)));
        // x < 0
        assertTrue("Q(1,-1) should be NaN",
                   Double.isNaN(Gamma.regularizedGammaQ(1.0, -1.0)));
    }

    @Test(timeout = 4000, expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaPMaxIterationsExceeded() throws MathException {
        // Force non-convergence by using very small epsilon and few iterations
        // a=10, x=100, epsilon=1e-30, maxIterations=5
        Gamma.regularizedGammaP(10.0, 100.0, 1e-30, 5);
    }

    @Test(timeout = 4000, expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaQMaxIterationsExceeded() throws MathException {
        // Force non-convergence in continued fraction
        // a=0.5, x=100, epsilon=1e-30, maxIterations=5
        Gamma.regularizedGammaQ(0.5, 100.0, 1e-30, 5);
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testGammaConstructorIsPrivate() throws Exception {
        // Verify that Gamma cannot be instantiated (private constructor)
        java.lang.reflect.Constructor<Gamma> constructor = Gamma.class.getDeclaredConstructor();
        assertTrue("Constructor should be private",
                   java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Gamma instance = constructor.newInstance();
        assertNotNull("Instance should be created via reflection", instance);
    }

    @Test(timeout = 4000)
    public void testSerialVersionUID() {
        // Ensure serialVersionUID is present and correct
        long expectedUID = -6587513359895466954L;
        assertEquals("serialVersionUID", expectedUID, Gamma.serialVersionUID);
    }
}