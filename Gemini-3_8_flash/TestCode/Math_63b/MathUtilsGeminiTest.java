package org.apache.commons.math.util;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.util.MathUtils
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. addAndCheck(int, int) & addAndCheck(long, long):
 *    - Positive and negative overflow detection.
 *    - Symmetry branching (a > b vs a <= b).
 *    - Long.MIN_VALUE edge cases.
 * 2. binomialCoefficient(int, int), Double, Log:
 *    - Invalid parameters (n < k, n < 0).
 *    - Identity and border values (k == 0, k == n, k == 1, k == n - 1, k > n / 2).
 *    - Threshold ranges: n <= 61, 61 < n <= 66, n > 66 (with mulAndCheck overflow).
 *    - Log branch: n < 67, 67 <= n < 1030, n >= 1030.
 * 3. Array and Scalar equals / equalsIncludingNaN / ULP comparison:
 *    - Both null, one null, different lengths, identical and mismatched elements.
 *    - KNOWN DEFECT: MathUtils.equals(double[], double[]) incorrectly considered
 *      NaN == NaN because it invoked equals(double, double), returning true instead of false.
 * 4. factorial, factorialDouble, factorialLog:
 *    - Negative inputs, lookup array range (0..20), overflow beyond 20 for long.
 * 5. gcd(int, int) & gcd(long, long) Stein's Binary GCD:
 *    - u == 0 || v == 0 including MIN_VALUE overflow.
 *    - Power of 2 reduction, shifting, and even/odd exchange loops.
 * 6. lcm(int, int) & lcm(long, long):
 *    - Zero parameters, normal calculation, and overflow detection.
 * 7. mulAndCheck & subAndCheck:
 *    - All sign combinations (+/+, -/-, +/-) and Integer/Long MIN/MAX boundaries.
 * 8. pow methods (int, long, BigInteger):
 *    - Exponent == 0, positive odd/even exponents, negative exponent exceptions.
 * 9. Distance functions (L1, L2, Linf for double[] and int[]):
 *    - Multi-dimensional points, absolute differences, zero distance.
 * 10. checkOrder(double[], OrderDirection, boolean):
 *     - INCREASING vs DECREASING, strict vs non-strict monotonicity, exceptions.
 * 11. safeNorm(double[]):
 *     - Giant numbers (> agiant), dwarf numbers (< rdwarf), mixed scale, zero values.
 * 12. normalizeArray, normalizeAngle, round, sign, indicator, scalb, cosh, sinh.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import