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

package org.apache.commons.math.complex;

import org.apache.commons.math.exception.NotPositiveException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.math.complex.Complex
 *
 * Branches Analyzed:
 * 1. abs():
 *    - isNaN branch -> returns Double.NaN
 *    - isInfinite() branch -> returns Double.POSITIVE_INFINITY
 *    - |real| < |imaginary|: imaginary == 0.0 vs non-zero scaling
 *    - |real| >= |imaginary|: real == 0.0 vs non-zero scaling
 * 2. add(Complex), add(double), subtract(Complex), subtract(double):
 *    - Null check guards (checkNotNull)
 *    - NaN handling for this and/or arguments
 *    - Normal additions and subtractions
 * 3. conjugate():
 *    - isNaN branch vs normal conjugate
 * 4. divide(Complex), divide(double), reciprocal():
 *    - Null checks
 *    - NaN checks
 *    - Zero divisor (c == 0.0 && d == 0.0) -> returns NaN
 *    - Infinite divisor with finite dividend -> returns ZERO
 *    - Magnitude comparison |c| < |d| vs |c| >= |d|
 *    - reciprocal() on 0.0+0.0i -> NaN, infinite -> ZERO, |real| < |imaginary| vs |real| >= |imag