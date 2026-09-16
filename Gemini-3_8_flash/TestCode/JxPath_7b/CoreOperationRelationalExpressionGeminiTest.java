/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;

/* [Branch & Defect Analysis Matrix]
 * Target Class: CoreOperationRelationalExpression (and subclasses: >, >=, <, <=)
 *
 * Decision / Branch Matrix:
 * 1. getPrecedence(): Must return 3.
 * 2. isSymmetric(): Must return false.
 * 3. compute(Object left, Object right):
 *    - left and/or right reduction (reduce(Object)):
 *      * SelfContext -> node pointer
 *      * NodePointer / VariablePointer -> ValueUtils.getValue() -> unwrapped value
 *      * Collection -> Iterator
 *      * Array (e.g. String[], Integer[]) -> ValueUtils unwrapping & Iterator
 *    - InitialContext check & reset: left is InitialContext, right is InitialContext
 *    - left is Iterator && right is Iterator -> findMatch(Iterator, Iterator)
 *    - left is Iterator (right is scalar) -> containsMatch(Iterator, scalar)
 *    - right is Iterator (left is scalar) -> containsMatch(Iterator, scalar) [inverted compare]
 *    - Double.isNaN(l) -> false
 *    - Double.isNaN(r) -> false
 *    - Double.compare(l, r) -> evaluateCompare()
 *
 * Defect Targeting:
 * - Defects4J JXPath Defect: NodeSet / array variable evaluation in relational operations
 *   Evaluation of <$array > 0> failed (returned false instead of true) because arrays
 *   were not properly reduced to collections/iterators before relational comparison.
 * - Targeted Tests:
 *   * testDefectArrayGreaterThanZero
 *   * testDefectArrayRelationalInverted
 *   * testDefectArrayVsArrayComparison
 */
public class CoreOperationRelationalExpressionGeminiTest {

    public static class TestBean {
        private final List<Integer> values;

        public TestBean(List<Integer> values) {
            this.values = values;
        }

        public List<Integer> getValues() {
            return values;
        }
    }

    private static class CustomRelationalExpression extends CoreOperationRelationalExpression {
        CustomRelationalExpression(Expression arg1, Expression arg2) {
            super(new Expression[] { arg1, arg2 });
        }

        public String getSymbol() {
            return "==";
        }

        protected boolean evaluateCompare(int compare) {
            return compare == 0;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Inspection
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrecedenceAndSymmetryContracts() {
        Constant c1 = new Constant(Double.valueOf(1.0));
        Constant c2 = new Constant(Double.valueOf(2.0));

        CoreOperationGreaterThan gt = new CoreOperationGreaterThan(c1, c2);
        CoreOperationGreaterThanOrEqual gte = new CoreOperationGreaterThanOrEqual(c1, c2);
        CoreOperationLessThan lt = new CoreOperationLessThan(c1, c2);
        CoreOperationLessThanOrEqual lte = new CoreOperationLessThanOrEqual(c1, c2);
        CustomRelationalExpression custom = new CustomRelationalExpression(c1, c2);

        assertEquals("Precedence of > must be 3", 3, gt.getPrecedence());
        assertEquals("Precedence of >= must be 3", 3, gte.getPrecedence());
        assertEquals("Precedence of < must be 3", 3, lt.getPrecedence());
        assertEquals("Precedence of <= must be 3", 3, lte.getPrecedence());
        assertEquals("Precedence of custom relational expression must be 3", 3, custom.getPrecedence());

        assertFalse("Relational expressions must not be symmetric", gt.isSymmetric());
        assertFalse("Relational expressions must not be symmetric", gte.isSymmetric());
        assertFalse("Relational expressions must not be symmetric", lt.isSymmetric());
        assertFalse("Relational expressions must not be symmetric", lte.isSymmetric());
        assertFalse("Relational expressions must not be symmetric", custom.isSymmetric());
    }

    @Test(timeout = 4000)
    public void testDirectASTScalarComparisons() {
        Constant c10 = new Constant(Double.valueOf(10.0));
        Constant c5 = new Constant(Double.valueOf(5.0));
        Constant c10Dup = new Constant(Double.valueOf(10.0));

        // Greater Than
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(c10, c5).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(c5, c10).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(c10, c10Dup).computeValue(null));

        // Greater Than Or Equal
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(c10, c5).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(c5, c10).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThanOrEqual(c10, c10Dup).computeValue(null));

        // Less Than
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(c10, c5).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThan(c5, c10).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(c10, c10Dup).computeValue(null));

        // Less Than Or Equal
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(c10, c5).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(c5, c10).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThanOrEqual(c10, c10Dup).computeValue(null));

        // Custom Expression
        assertEquals(Boolean.FALSE, new CustomRelationalExpression(c10, c5).computeValue(null));
        assertEquals(Boolean.TRUE, new CustomRelationalExpression(c10, c10Dup).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testXPathContextRelationalBasics() {
        JXPathContext context = JXPathContext.newContext(new Object());

        assertEquals(Boolean.TRUE, context.getValue("5.0 > 4.9"));
        assertEquals(Boolean.FALSE, context.getValue("5.0 > 5.0"));
        assertEquals(Boolean.TRUE, context.getValue("5.0 >= 5.0"));
        assertEquals(Boolean.TRUE, context.getValue("4.9 < 5.0"));
        assertEquals(Boolean.FALSE, context.getValue("5.0 < 4.9"));
        assertEquals(Boolean.TRUE, context.getValue("5.0 <= 5.0"));

        // XPath numeric conversions for string literals
        assertEquals(Boolean.TRUE, context.getValue("'10' > '2'"));
        assertEquals(Boolean.TRUE, context.getValue("'05' = 5"));
        assertEquals(Boolean.TRUE, context.getValue("'05' >= 5"));
        assertEquals(Boolean.FALSE, context.getValue("'05' < 5"));

        // Boolean operands (true() = 1, false() = 0)
        assertEquals(Boolean.TRUE, context.getValue("true() > false()"));
        assertEquals(Boolean.TRUE, context.getValue("true() >= 1"));
        assertEquals(Boolean.FALSE, context.getValue("false() > 0"));
        assertEquals(Boolean.TRUE, context.getValue("false() <= 0"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNaNAndInfinityBoundaries() {
        Constant nan = new Constant(Double.valueOf(Double.NaN));
        Constant validNum = new Constant(Double.valueOf(100.0));
        Constant invalidStr = new Constant("NotANumber");

        // NaN vs Number
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(nan, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(validNum, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(nan, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(validNum, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(nan, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(validNum, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(nan, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(validNum, nan).computeValue(null));

        // NaN vs NaN
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(nan, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThanOrEqual(nan, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(nan, nan).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThanOrEqual(nan, nan).computeValue(null));

        // Invalid string representation yielding NaN
        assertEquals(Boolean.FALSE, new CoreOperationGreaterThan(invalidStr, validNum).computeValue(null));
        assertEquals(Boolean.FALSE, new CoreOperationLessThan(validNum, invalidStr).computeValue(null));

        // Infinite boundaries
        Constant posInf = new Constant(Double.valueOf(Double.POSITIVE_INFINITY));
        Constant negInf = new Constant(Double.valueOf(Double.NEGATIVE_INFINITY));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(posInf, validNum).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationLessThan(negInf, validNum).computeValue(null));
        assertEquals(Boolean.TRUE, new CoreOperationGreaterThan(posInf, negInf).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullCollections() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("emptyList", Collections.emptyList());
        context.getVariables().declareVariable("emptyArray", new Integer[0]);

        assertEquals(Boolean.FALSE, context.getValue("$emptyList > 0"));
        assertEquals(Boolean.FALSE, context.getValue("$emptyList < 0"));
        assertEquals(Boolean.FALSE, context.getValue("0 < $emptyList"));
        assertEquals(Boolean.FALSE, context.getValue("0 > $emptyList"));

        assertEquals(Boolean.FALSE, context.getValue("$emptyArray > 0"));
        assertEquals(Boolean.FALSE, context.getValue("$emptyArray < 0"));
        assertEquals(Boolean.FALSE, context.getValue("0 < $emptyArray"));
        assertEquals(Boolean.FALSE, context.getValue("0 > $emptyArray"));

        assertEquals(Boolean.FALSE, context.getValue("$emptyList > $emptyArray"));
        assertEquals(Boolean.FALSE, context.getValue("$emptyArray < $emptyList"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Directly reproduces the defect in CoreOperationRelationalExpression where
     * arrays (e.g. String[], Integer[]) wrapped in VariablePointer / InitialContext
     * fail to reduce into Iterators, causing <$array > 0> to evaluate to false instead of true.
     */
    @Test(timeout = 4000)
    public void testDefectArrayGreaterThanZero() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new String[] { "1", "2", "3" });

        assertEquals("Evaluating <$array > 0> must be true", Boolean.TRUE, context.getValue("$array > 0"));
        assertEquals("Evaluating <$array >= 0> must be true", Boolean.TRUE, context.getValue("$array >= 0"));
        assertEquals("Evaluating <$array < 2> must be true", Boolean.TRUE, context.getValue("$array < 2"));
        assertEquals("Evaluating <$array <= 2> must be true", Boolean.TRUE, context.getValue("$array <= 2"));
        assertEquals("Evaluating <$array > 4> must be false", Boolean.FALSE, context.getValue("$array > 4"));
        assertEquals("Evaluating <$array >= 4> must be false", Boolean.FALSE, context.getValue("$array >= 4"));
        assertEquals("Evaluating <$array < 0> must be false", Boolean.FALSE, context.getValue("$array < 0"));
        assertEquals("Evaluating <$array <= 0> must be false", Boolean.FALSE, context.getValue("$array <= 0"));
    }

    @Test(timeout = 4000)
    public void testDefectArrayRelationalInverted() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("array", new String[] { "1", "2", "3" });

        assertEquals("Evaluating <0 < $array> must be true", Boolean.TRUE, context.getValue("0 < $array"));
        assertEquals("Evaluating <0 <= $array> must be true", Boolean.TRUE, context.getValue("0 <= $array"));
        assertEquals("Evaluating <2 > $array> must be true", Boolean.TRUE, context.getValue("2 > $array"));
        assertEquals("Evaluating <2 >= $array> must be true", Boolean.TRUE, context.getValue("2 >= $array"));
        assertEquals("Evaluating <4 < $array> must be false", Boolean.FALSE, context.getValue("4 < $array"));
        assertEquals("Evaluating <4 <= $array> must be false", Boolean.FALSE, context.getValue("4 <= $array"));
        assertEquals("Evaluating <0 > $array> must be false", Boolean.FALSE, context.getValue("0 > $array"));
        assertEquals("Evaluating <0 >= $array> must be false", Boolean.FALSE, context.getValue("0 >= $array"));
    }

    @Test(timeout = 4000)
    public void testDefectArrayVsArrayComparison() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("arrA", new Integer[] { 1, 10 });
        context.getVariables().declareVariable("arrB", new Integer[] { 5, 20 });

        // 1 < 5 is true
        assertEquals(Boolean.TRUE, context.getValue("$arrA < $arrB"));
        // 10 > 5 is true
        assertEquals(Boolean.TRUE, context.getValue("$arrA > $arrB"));

        context.getVariables().declareVariable("arrSmall", new Integer[] { 1, 2 });
        context.getVariables().declareVariable("arrBig", new Integer[] { 10, 20 });

        assertEquals(Boolean.TRUE, context.getValue("$arrSmall < $arrBig"));
        assertEquals(Boolean.FALSE, context.getValue("$arrSmall > $arrBig"));
        assertEquals(Boolean.TRUE, context.getValue("$arrBig > $arrSmall"));
        assertEquals(Boolean.FALSE, context.getValue("$arrBig < $arrSmall"));
    }

    // =========================================================================
    // Partition D: Complex NodeSet and Context Iteration Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNodeSetFromBeanProperties() {
        TestBean bean = new TestBean(Arrays.asList(10, 20, 30));
        JXPathContext context = JXPathContext.newContext(bean);

        assertEquals(Boolean.TRUE, context.getValue("values > 25"));
        assertEquals(Boolean.TRUE, context.getValue("values < 15"));
        assertEquals(Boolean.FALSE, context.getValue("values > 50"));
        assertEquals(Boolean.FALSE, context.getValue("values < 5"));

        assertEquals(Boolean.TRUE, context.getValue("25 < values"));
        assertEquals(Boolean.TRUE, context.getValue("15 > values"));
        assertEquals(Boolean.FALSE, context.getValue("50 < values"));
        assertEquals(Boolean.FALSE, context.getValue("5 > values"));
    }

    @Test(timeout = 4000)
    public void testNodeSetWithMixedInvalidTypes() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("mixed", new Object[] { "invalidNumber", Integer.valueOf(42) });

        // Despite one element resolving to NaN, the other element (42) should satisfy > 40
        assertEquals(Boolean.TRUE, context.getValue("$mixed > 40"));
        assertEquals(Boolean.TRUE, context.getValue("40 < $mixed"));
        assertEquals(Boolean.FALSE, context.getValue("$mixed > 50"));
    }

    // =========================================================================
    // Partition E: Determinism, State Reset & Lifecycle Guard
    // =========================================================================

    @Test(timeout = 4000)
    public void testRepeatedEvaluationDeterminism() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.getVariables().declareVariable("arr", new Integer[] { 5, 10, 15 });

        CompiledExpression compiledGt = JXPathContext.compile("$arr > 7");
        CompiledExpression compiledLt = JXPathContext.compile("7 < $arr");

        for (int i = 0; i < 5; i++) {
            assertEquals("Execution should be 100% deterministic on iteration " + i,
                    Boolean.TRUE, compiledGt.getValue(context));
            assertEquals("Execution should be 100% deterministic on iteration " + i,
                    Boolean.TRUE, compiledLt.getValue(context));
        }
    }
}