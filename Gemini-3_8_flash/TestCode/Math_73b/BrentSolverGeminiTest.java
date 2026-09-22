package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.analysis.solvers.BrentSolver
 *
 * Decision / Branch Matrix:
 * 1. Constructor:
 *    - Deprecated constructor BrentSolver(UnivariateRealFunction)
 *    - Default constructor BrentSolver()
 * 2. solve(min, max) & solve(min, max, initial) [Deprecated methods delegating to solve(f, ...)]
 * 3. solve(f, min, max, initial):
 *    - verifySequence(min, initial, max) failure (min >= initial or initial >= max) -> IllegalArgumentException
 *    - |yInitial| <= functionValueAccuracy -> return initial
 *    - |yMin| <= functionValueAccuracy -> return min (checks regression in result value)
 *    - yInitial * yMin < 0 -> interval [min, initial] brackets root
 *    - |yMax| <= functionValueAccuracy -> return max
 *    - yInitial * yMax < 0 -> interval [initial, max] brackets root
 *    - Defect Path: Neither sub-interval brackets root (yMin, yInitial, yMax have identical signs)
 *      -> Expected behavior per contract & Defects4J: IllegalArgumentException (non-bracketing)
 * 4. solve(f, min, max):
 *    - verifyInterval(min, max) failure (min >= max) -> IllegalArgumentException
 *    - sign = yMin * yMax > 0:
 *        * |yMin| <= functionValueAccuracy -> return min
 *        * |yMax| <= functionValueAccuracy -> return max
 *        * Neither <= accuracy -> throws IllegalArgumentException (NON_BRACKETING_MESSAGE)
 *    - sign < 0 -> standard solve bracket
 *    - sign == 0:
 *        * yMin == 0.0 -> return min
 *        * yMax == 0.0 -> return max
 * 5. Private solve(f, x0, y0, x1, y1, x2, y2) algorithm branches:
 *    - Math.abs(y2) < Math.abs(y1): swap bracket point with approximation
 *    - Math.abs(y1) <= functionValueAccuracy -> early convergence
 *    - Math.abs(dx) <=