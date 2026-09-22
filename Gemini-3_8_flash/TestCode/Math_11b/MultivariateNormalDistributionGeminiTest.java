package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: MultivariateNormalDistribution
 *
 * 1. Defect Targeting (Ground Truth - Math Defect / Integer Division Bug):
 *    - In `density(double[] vals)`: FastMath.pow(2 * FastMath.PI, -dim / 2) uses integer division `-dim / 2`.
 *      For any odd dimension (e.g. dim = 1, dim = 3), `-dim / 2` evaluates to -0 or -1 instead of -0.5 or -1.5,
 *      resulting in incorrect probability density calculations (e.g., dim = 1 expected 0.23644016... but was 0.59266759...).
 *      Targeted by: `testUnivariateDistributionDefectMath99` and `testOddDimensionDensityCalculation`.
 *
 * 2. Decision Branches & Edge Conditions:
 *    - Constructor:
 *      * covariances.length != dim -> DimensionMismatchException.
 *      * covariances[i].length != dim (non-square row) -> DimensionMismatchException.
 *      * covMatEigenvalues[i] <