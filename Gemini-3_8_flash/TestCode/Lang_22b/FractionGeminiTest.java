/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang3.math.Fraction
 *
 * 1. Factory Methods:
 *    - getFraction(int, int):
 *      * denom == 0 (ArithmeticException)
 *      * denom < 0, numer/denom == Integer.MIN_VALUE (overflow ArithmeticException)
 *      * denom < 0 (negates both)
 *    - getFraction(int, int, int):
 *      * denom <= 0 (ArithmeticException), numer < 0 (ArithmeticException)
 *      * whole < 0 vs whole >= 0 calculation & overflow checks (< MIN_VALUE or > MAX_VALUE)
 *    - getReducedFraction(int, int):
 *      * denom == 0, numer == 0 (returns ZERO)
 *      * denom == Integer.MIN_VALUE with even numer
 *      * denom < 0 with MIN_VALUE checks
 *      * Defect check: numer == Integer.MIN_VALUE where Math.abs() overflows in GCD!
 *    - getFraction(double):
 *      * value < 0 (sign handling)
 *      * value > Integer.MAX_VALUE or Double.isNaN(value)
 *      * continued fraction algorithm convergence (i == 25 error)
 *    - getFraction(String):
 *      * null check
 *      * contains '.' (double format)
 *      * contains ' ' (whole number + fraction format, check missing '/')
 *      * contains '/' (fraction format)
 *      * integer only
 *
 * 2. Proper & Representation:
 *    - getProperNumerator(), getProperWhole()
 *    - toString(), toProperString() (0, 1, -1, proper fractions, improper positive/negative, MIN_VALUE)
 *
 * 3. Arithmetic & Transformations:
 *    - reduce(), invert() (zero, MIN_VALUE checks), negate() (MIN_VALUE check), abs()
 *    - pow() (power == 0, power == 1, negative, Integer.MIN_VALUE, even/odd positive)
 *    - add(), subtract():
 *      * Identity checks (numer == 0, fraction.numer == 0)
 *      * gcd(d1, d2) == 1 vs d1 > 1 (BigInteger logic)
 *      * Overflow checks in addAndCheck, subAndCheck, mulAndCheck, mulPosAndCheck
 *    - multiplyBy(), divideBy() (null checks, zero checks, overflow checks)
 *
 * 4. Object Contract & Ordering:
 *    - equals (identity, non-Fraction, exact equality vs reduced difference)
 *    - hashCode (consistency and equality contract)
 *    - compareTo (exact equals, value equals, less than, greater than)
 *
 * 5. Defects4J Known Defect Targeted:
 *    - testReducedFactory_int_int & testReduce:
 *      * Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE <= 1 evaluates to true in greatestCommonDivisor!
 *      * Causes greatestCommonDivisor(Integer.MIN_VALUE, 2) to return 1 instead of 2.
 *      * Asserts getReducedFraction(Integer.MIN_VALUE, 2) yields numer = -1073741824 and denom = 1.
 * ---------------------------------------------------------------------------------------------------------
 */
public class FractionGeminiTest {

    private static final double EPSILON = 0.00001;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(1, Fraction.ONE_HALF.getNumerator());
        assertEquals(2, Fraction.ONE_HALF.getDenominator());

        assertEquals(1, Fraction.ONE_THIRD.getNumerator());
        assertEquals(3, Fraction.ONE_THIRD.getDenominator());

        assertEquals(2, Fraction.TWO_THIRDS.getNumerator());
        assertEquals(3, Fraction.TWO_THIRDS.getDenominator());

        assertEquals(1, Fraction.ONE_QUARTER.getNumerator());
        assertEquals(4, Fraction.ONE_QUARTER.getDenominator());

        assertEquals(2, Fraction.TWO_QUARTERS.getNumerator());
        assertEquals(4, Fraction.TWO_QUARTERS.getDenominator());

        assertEquals(3, Fraction.THREE_QUARTERS.getNumerator());