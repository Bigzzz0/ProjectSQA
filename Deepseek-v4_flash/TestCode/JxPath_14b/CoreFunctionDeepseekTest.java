package org.apache.commons.jxpath.ri.compiler;

/* [Branch & Defect Analysis Matrix]
 *
 * Target: org.apache.commons.jxpath.ri.compiler.CoreFunction
 *
 * Known defect:
 *   round('NaN') must return NaN according to XPath semantics.
 *   The defective code uses Math.round(Double.NaN), which yields 0.0.
 *
 * Branch/decision zones exercised:
 *   - getFunctionName switch: every named function plus unknown code fallback.
 *   - computeContextDependent: super-dependent path, context-free groups,
 *     zero-arg dependent group, and format-number 0/1/2/3 argument cases.
 *   - toString: null argument array, empty argument array, populated array.
 *   - assertArgCount / assertArgRange: valid and invalid argument counts.
 *   - core numeric functions: round, floor, ceiling, number, sum.
 *   - string functions: string, concat, starts-with, contains,
 *     substring-before/after, substring, string-length, normalize-space,
 *     translate.
 *   - boolean functions: boolean, not, true, false, null.
 *   - context functions: last, position, count, lang, local-name,
 *     namespace-uri, name, id, key, format-number.
 *
 * Boundary values and extreme inputs:
 *   NaN, positive/negative fractions, zero, empty strings, null values,
 *   empty collections, singleton collections, empty EvalContexts,
 *   absent pointers, and unknown function codes.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

public class CoreFunctionDeepseekTest {

    private static Expression expr(final Object value) {
        return new Expression() {
            public Object compute(EvalContext context) {
                return value;
            }

            public Object computeValue(EvalContext context) {
                return value;
            }

            public boolean isContextDependent() {
                return false;
            }

            public String toString() {
                return value == null ? "null" : value.toString();
            }
        };
    }

    private static Expression dependentExpr(final Object value) {
        return new Expression() {
            public Object compute(EvalContext context) {
                return value;
            }

            public Object computeValue(EvalContext context) {
                return value;
            }

            public boolean isContextDependent() {
                return true;
            }
        };
    }

    private static CoreFunction function(int code, Expression... args) {
        return new CoreFunction(code, args);
    }

    private static CoreFunction functionNullArgs(int code) {
        return new CoreFunction(code, (Expression[]) null);
    }

    private static JXPathContext newContext(Object bean) {
        JXPathContext context = JXPathContext.newContext(bean);
        context.setLocale(Locale.US);
        return context;
    }

    private static NodePointer pointerOf(Object bean) {
        return (NodePointer) newContext(bean).getContextPointer();
    }

    private static class StubEvalContext extends EvalContext {
        private final JXPathContext jxpathContext;
        private final NodePointer nodePointer;
        private final Object nextValue;
        private final int size;
        private int position;
        private int nextIndex;

        StubEvalContext(JXPathContext jxpathContext, NodePointer nodePointer, int size) {
            this(jxpathContext, nodePointer, size, 0, nodePointer);
        }

        StubEvalContext(JXPathContext jxpathContext, NodePointer nodePointer, int size, int position) {
            this(jxpathContext, nodePointer, size, position, nodePointer);
        }

        private StubEvalContext(
                JXPathContext jxpathContext,
                NodePointer nodePointer,
                int size,
                int position,
                Object nextValue) {
            super(null);
            this.jxpathContext = jxpathContext;
            this.nodePointer = nodePointer;
            this.size = size;
            this.position = position;
            this.nextIndex = 0;
            this.nextValue = nextValue;
        }

        public JXPathContext getJXPathContext() {
            return jxpathContext;
        }

        public int getCurrentPosition() {
            return position;
        }

        public boolean setPosition(int position) {
            this.position = position;
            return true;
        }

        public void reset() {
            this.position = 0;
            this.nextIndex = 0;
        }

        public boolean nextNode() {
            if (position < size) {
                position++;
                return true;
            }
            return false;
        }

        public boolean nextSet() {
            return false;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public Object next() {
            if (nextIndex < size) {
                nextIndex++;
                return nextValue;
            }
            return null;
        }

        public Object getValue() {
            return nodePointer != null ? nodePointer : nextValue;
        }

        public NodePointer getCurrentNodePointer() {
            return nodePointer;
        }

        public NodePointer getSingleNodePointer() {
            return nodePointer;
        }
    }

    @Test(timeout = 4000)
    public void testDefectRoundNaN() {
        CoreFunction round = function(Compiler.FUNCTION_ROUND, expr("NaN"));
        Object result = round.computeValue(null);
        assertTrue(result instanceof Double);
        double value = ((Double) result).doubleValue();
        assertTrue("round('NaN') must be NaN, but was " + value, Double.isNaN(value));
    }

    @Test(timeout = 4000)
    public void testRoundBoundaries() {
        assertEquals(3.0, ((Double) function(
                Compiler.FUNCTION_ROUND, expr(2.6)).computeValue(null)).doubleValue(), 0.0);
        assertEquals(-3.0, ((Double) function(
                Compiler.FUNCTION_ROUND, expr(-2.6)).computeValue(null)).doubleValue(), 0.0);
        assertEquals(0.0, ((Double) function(
                Compiler.FUNCTION_ROUND, expr(0.0)).computeValue(null)).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testFloorCeiling() {
        assertEquals(3.0, ((Double) function(
                Compiler.FUNCTION_FLOOR, expr(3.7)).computeValue(null)).doubleValue(), 0.0);
        assertEquals(4.0, ((Double) function(
                Compiler.FUNCTION_CEILING, expr(3.2)).computeValue(null)).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNumber() {
        assertEquals(5.0, ((Double) function(
                Compiler.FUNCTION_NUMBER, expr("5")).computeValue(null)).doubleValue(), 0.0);

        NodePointer pointer = pointerOf(new Integer(5));
        StubEvalContext context = new StubEvalContext(null, pointer, 0);
        Object result = function(Compiler.FUNCTION_NUMBER).computeValue(context);
        assertTrue(result instanceof Double);
        assertEquals(5.0, ((Double) result).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testString() {
        assertEquals("abc", function(
                Compiler.FUNCTION_STRING, expr("abc")).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_STRING, expr(null)).computeValue(null));

        NodePointer pointer = pointerOf("hello");
        StubEvalContext context = new StubEvalContext(null, pointer, 0);
        assertEquals("hello", function(Compiler.FUNCTION_STRING).computeValue(context));
    }

    @Test(timeout = 4000)
    public void testStringLength() {
        assertEquals(5.0, ((Double) function(
                Compiler.FUNCTION_STRING_LENGTH, expr("hello")).computeValue(null)).doubleValue(), 0.0);

        NodePointer pointer = pointerOf("hello");
        StubEvalContext context = new StubEvalContext(null, pointer, 0);
        assertEquals(5.0, ((Double) function(
                Compiler.FUNCTION_STRING_LENGTH).computeValue(context)).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConcat() {
        assertEquals("ab", function(
                Compiler.FUNCTION_CONCAT, expr("a"), expr("b")).computeValue(null));
        assertEquals("ac", function(
                Compiler.FUNCTION_CONCAT,
                expr("a"),
                expr(null),
                expr("c")).computeValue(null));
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testConcatInvalidArgumentCount() {
        function(Compiler.FUNCTION_CONCAT, expr("a")).computeValue(null);
    }

    @Test(timeout = 4000)
    public void testStartsWithContains() {
        assertEquals(Boolean.TRUE, function(
                Compiler.FUNCTION_STARTS_WITH, expr("hello"), expr("he")).computeValue(null));
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_STARTS_WITH, expr("hello"), expr("lo")).computeValue(null));
        assertEquals(Boolean.TRUE, function(
                Compiler.FUNCTION_CONTAINS, expr("hello"), expr("ell")).computeValue(null));
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_CONTAINS, expr("hello"), expr("x")).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAfter() {
        assertEquals("a", function(
                Compiler.FUNCTION_SUBSTRING_BEFORE, expr("abc"), expr("b")).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_SUBSTRING_BEFORE, expr("abc"), expr("x")).computeValue(null));
        assertEquals("c", function(
                Compiler.FUNCTION_SUBSTRING_AFTER, expr("abc"), expr("b")).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_SUBSTRING_AFTER, expr("abc"), expr("x")).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testSubstring() {
        assertEquals("", substringCall(expr("12345"), expr(Double.NaN)));
        assertEquals("", substringCall(expr("12345"), expr(7)));
        assertEquals("12345", substringCall(expr("12345"), expr(0)));
        assertEquals("2345", substringCall(expr("12345"), expr(2)));
        assertEquals("", substringCall(expr("12345"), expr(2), expr(-1)));
        assertEquals("", substringCall(expr("12345"), expr(-5), expr(2)));
        assertEquals("1", substringCall(expr("12345"), expr(0), expr(2)));
        assertEquals("123", substringCall(expr("12345"), expr(1), expr(3)));
        assertEquals("1234", substringCall(expr("12345"), expr(-5), expr(10)));
        assertEquals("5", substringCall(expr("12345"), expr(5), expr(3)));
        assertEquals("234", substringCall(expr("12345"), expr(2), expr(3)));
    }

    private static Object substringCall(Expression... args) {
        return function(Compiler.FUNCTION_SUBSTRING, args).computeValue(null);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testSubstringInvalidArgumentCount() {
        function(Compiler.FUNCTION_SUBSTRING, expr("a")).computeValue(null);
    }

    @Test(timeout = 4000)
    public void testNormalizeSpace() {
        assertEquals("a b", function(
                Compiler.FUNCTION_NORMALIZE_SPACE, expr("  a   b  ")).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_NORMALIZE_SPACE, expr(" \t\n ")).computeValue(null));
        assertEquals("single", function(
                Compiler.FUNCTION_NORMALIZE_SPACE, expr("single")).computeValue(null));
        assertEquals("a b", function(
                Compiler.FUNCTION_NORMALIZE_SPACE, expr("a \n b")).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testTranslate() {
        assertEquals("ABC", function(
                Compiler.FUNCTION_TRANSLATE,
                expr("abc"),
                expr("abc"),
                expr("ABC")).computeValue(null));
        assertEquals("aZ", function(
                Compiler.FUNCTION_TRANSLATE,
                expr("ab"),
                expr("b"),
                expr("Z")).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_TRANSLATE,
                expr("a"),
                expr("a"),
                expr("")).computeValue(null));
        assertEquals("abc", function(
                Compiler.FUNCTION_TRANSLATE,
                expr("abc"),
                expr("xyz"),
                expr("XYZ")).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testBooleanNotTrueFalseNull() {
        assertEquals(Boolean.TRUE, function(
                Compiler.FUNCTION_BOOLEAN, expr("true")).computeValue(null));
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_NOT, expr("true")).computeValue(null));
        assertEquals(Boolean.TRUE, function(
                Compiler.FUNCTION_TRUE).computeValue(null));
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_FALSE).computeValue(null));
        assertNull(function(Compiler.FUNCTION_NULL).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testCount() {
        assertEquals(0.0, ((Double) function(
                Compiler.FUNCTION_COUNT, expr(null)).computeValue(null)).doubleValue(), 0.0);
        assertEquals(1.0, ((Double) function(
                Compiler.FUNCTION_COUNT, expr("x")).computeValue(null)).doubleValue(), 0.0);
        assertEquals(1.0, ((Double) function(
                Compiler.FUNCTION_COUNT,
                expr(Collections.singletonList("x"))).computeValue(null)).doubleValue(), 0.0);
        assertEquals(0.0, ((Double) function(
                Compiler.FUNCTION_COUNT,
                expr(Collections.emptyList())).computeValue(null)).doubleValue(), 0.0);

        StubEvalContext context = new StubEvalContext(null, null, 2);
        assertEquals(2.0, ((Double) function(
                Compiler.FUNCTION_COUNT, expr(context)).computeValue(null)).doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSum() {
        assertEquals(0.0, ((Double) function(
                Compiler.FUNCTION_SUM, expr(null)).computeValue(null)).doubleValue(), 0.0);

        NodePointer pointer = pointerOf(new Integer(5));
        StubEvalContext context = new StubEvalContext(null, pointer, 1);
        assertEquals(5.0, ((Double) function(
                Compiler.FUNCTION_SUM, expr(context)).computeValue(null)).doubleValue(), 0.0);
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testSumInvalidType() {
        function(Compiler.FUNCTION_SUM, expr("x")).computeValue(null);
    }

    @Test(timeout = 4000)
    public void testLastAndPosition() {
        StubEvalContext lastContext = new StubEvalContext(null, null, 3, 2);
        Object lastResult = function(Compiler.FUNCTION_LAST).computeValue(lastContext);
        assertEquals(3.0, ((Double) lastResult).doubleValue(), 0.0);
        assertEquals(2, lastContext.getCurrentPosition());

        StubEvalContext zeroContext = new StubEvalContext(null, null, 2, 0);
        assertEquals(2.0, ((Double) function(
                Compiler.FUNCTION_LAST).computeValue(zeroContext)).doubleValue(), 0.0);

        StubEvalContext posContext = new StubEvalContext(null, null, 0, 7);
        Object posResult = function(Compiler.FUNCTION_POSITION).computeValue(posContext);
        assertEquals(7, ((Integer) posResult).intValue());
    }

    @Test(timeout = 4000)
    public void testLang() {
        StubEvalContext noPointer = new StubEvalContext(null, null, 0);
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_LANG, expr("en")).computeValue(noPointer));

        NodePointer pointer = pointerOf("hello");
        StubEvalContext withPointer = new StubEvalContext(null, pointer, 0);
        assertEquals(Boolean.FALSE, function(
                Compiler.FUNCTION_LANG, expr("en")).computeValue(withPointer));
    }

    @Test(timeout = 4000)
    public void testNamespaceUriLocalNameName() {
        NodePointer pointer = pointerOf("hello");
        StubEvalContext context = new StubEvalContext(null, pointer, 0);

        Object namespace = function(Compiler.FUNCTION_NAMESPACE_URI).computeValue(context);
        assertTrue(namespace instanceof String);

        Object localName = function(Compiler.FUNCTION_LOCAL_NAME).computeValue(context);
        assertTrue(localName == null || localName instanceof String);

        Object name = function(Compiler.FUNCTION_NAME).computeValue(context);
        assertNotNull(name);

        StubEvalContext empty = new StubEvalContext(null, null, 0);
        assertEquals("", function(
                Compiler.FUNCTION_NAMESPACE_URI, expr(empty)).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_LOCAL_NAME, expr(empty)).computeValue(null));
        assertEquals("", function(
                Compiler.FUNCTION_NAME, expr(empty)).computeValue(null));

        StubEvalContext nonEmptyNs = new StubEvalContext(null, pointer, 1);
        assertTrue(function(
                Compiler.FUNCTION_NAMESPACE_URI, expr(nonEmptyNs)).computeValue(null) instanceof String);
        StubEvalContext nonEmptyLn = new StubEvalContext(null, pointer, 1);
        assertNotNull(function(
                Compiler.FUNCTION_LOCAL_NAME, expr(nonEmptyLn)).computeValue(null));
        StubEvalContext nonEmptyName = new StubEvalContext(null, pointer, 1);
        assertNotNull(function(
                Compiler.FUNCTION_NAME, expr(nonEmptyName)).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testId() {
        JXPathContext jxpathContext = newContext(new Object());
        StubEvalContext context = new StubEvalContext(jxpathContext, null, 0);
        Object result = function(Compiler.FUNCTION_ID, expr("some-id")).computeValue(context);
        assertTrue(result == null || result instanceof NodePointer);
    }

    @Test(timeout = 4000)
    public void testKey() {
        JXPathContext jxpathContext = newContext(new Object());
        StubEvalContext context = new StubEvalContext(jxpathContext, null, 0);

        Object scalarResult = function(
                Compiler.FUNCTION_KEY, expr("key"), expr("value")).computeValue(context);
        assertTrue(scalarResult instanceof NodeSetContext);

        StubEvalContext empty = new StubEvalContext(jxpathContext, null, 0);
        Object emptyResult = function(
                Compiler.FUNCTION_KEY, expr("key"), expr(empty)).computeValue(context);
        assertTrue(emptyResult instanceof BasicNodeSet);

        NodePointer pointer = pointerOf("value");
        StubEvalContext one = new StubEvalContext(jxpathContext, pointer, 1);
        Object oneResult = function(
                Compiler.FUNCTION_KEY, expr("key"), expr(one)).computeValue(context);
        assertTrue(oneResult instanceof NodeSetContext);

        NodePointer pointer2 = pointerOf("value2");
        StubEvalContext two = new StubEvalContext(jxpathContext, pointer2, 2);
        Object twoResult = function(
                Compiler.FUNCTION_KEY, expr("key"), expr(two)).computeValue(context);
        assertTrue(twoResult instanceof NodeSetContext);
    }

    @Test(timeout = 4000)
    public void testFormatNumber() {
        JXPathContext jxpathContext = newContext(new Object());
        StubEvalContext context = new StubEvalContext(jxpathContext, null, 0);

        Object result = function(
                Compiler.FUNCTION_FORMAT_NUMBER,
                expr(1234.5),
                expr("#,##0.00")).computeValue(context);
        assertEquals("1,234.50", result);

        jxpathContext.setDecimalFormatSymbols(
                "sym", DecimalFormatSymbols.getInstance(Locale.US));
        Object symbolResult = function(
                Compiler.FUNCTION_FORMAT_NUMBER,
                expr(1234.5),
                expr("#,##0.00"),
                expr("sym")).computeValue(context);
        assertEquals("1,234.50", symbolResult);

        NodePointer pointer = pointerOf("hello");
        StubEvalContext pointerContext = new StubEvalContext(jxpathContext, pointer, 0);
        Object pointerResult = function(
                Compiler.FUNCTION_FORMAT_NUMBER,
                expr(1.23),
                expr("0.00")).computeValue(pointerContext);
        assertEquals("1.23", pointerResult);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testFormatNumberInvalidArgumentCount() {
        function(Compiler.FUNCTION_FORMAT_NUMBER, expr("1")).computeValue(null);
    }

    @Test(timeout = 4000)
    public void testGetFunctionCode() {
        CoreFunction function = function(Compiler.FUNCTION_LAST);
        assertEquals(Compiler.FUNCTION_LAST, function.getFunctionCode());
    }

    @Test(timeout = 4000)
    public void testGetFunctionName() {
        assertName(Compiler.FUNCTION_LAST, "last");
        assertName(Compiler.FUNCTION_POSITION, "position");
        assertName(Compiler.FUNCTION_COUNT, "count");
        assertName(Compiler.FUNCTION_ID, "id");
        assertName(Compiler.FUNCTION_LOCAL_NAME, "local-name");
        assertName(Compiler.FUNCTION_NAMESPACE_URI, "namespace-uri");
        assertName(Compiler.FUNCTION_NAME, "name");
        assertName(Compiler.FUNCTION_STRING, "string");
        assertName(Compiler.FUNCTION_CONCAT, "concat");
        assertName(Compiler.FUNCTION_STARTS_WITH, "starts-with");
        assertName(Compiler.FUNCTION_CONTAINS, "contains");
        assertName(Compiler.FUNCTION_SUBSTRING_BEFORE, "substring-before");
        assertName(Compiler.FUNCTION_SUBSTRING_AFTER, "substring-after");
        assertName(Compiler.FUNCTION_SUBSTRING, "substring");
        assertName(Compiler.FUNCTION_STRING_LENGTH, "string-length");
        assertName(Compiler.FUNCTION_NORMALIZE_SPACE, "normalize-space");
        assertName(Compiler.FUNCTION_TRANSLATE, "translate");
        assertName(Compiler.FUNCTION_BOOLEAN, "boolean");
        assertName(Compiler.FUNCTION_NOT, "not");
        assertName(Compiler.FUNCTION_TRUE, "true");
        assertName(Compiler.FUNCTION_FALSE, "false");
        assertName(Compiler.FUNCTION_LANG, "lang");
        assertName(Compiler.FUNCTION_NUMBER, "number");
        assertName(Compiler.FUNCTION_SUM, "sum");
        assertName(Compiler.FUNCTION_FLOOR, "floor");
        assertName(Compiler.FUNCTION_CEILING, "ceiling");
        assertName(Compiler.FUNCTION_ROUND, "round");
        assertName(Compiler.FUNCTION_KEY, "key");
        assertName(Compiler.FUNCTION_FORMAT_NUMBER, "format-number");
        assertName(999999, "unknownFunction999999()");
    }

    private static void assertName(int code, String expected) {
        assertEquals(expected, new CoreFunction(code, new Expression[0]).getFunctionName());
    }

    @Test(timeout = 4000)
    public void testUnknownFunctionCodeComputeValueReturnsNull() {
        assertNull(new CoreFunction(999999, new Expression[0]).computeValue(null));
    }

    @Test(timeout = 4000)
    public void testComputeContextDependent() {
        int[] alwaysContextDependent = {
            Compiler.FUNCTION_LAST,
            Compiler.FUNCTION_POSITION
        };
        for (int code : alwaysContextDependent) {
            assertTrue(function(code).computeContextDependent());
        }

        int[] zeroArgContextDependent = {
            Compiler.FUNCTION_BOOLEAN,
            Compiler.FUNCTION_LOCAL_NAME,
            Compiler.FUNCTION_NAME,
            Compiler.FUNCTION_NAMESPACE_URI,
            Compiler.FUNCTION_STRING,
            Compiler.FUNCTION_LANG,
            Compiler.FUNCTION_NUMBER
        };
        for (int code : zeroArgContextDependent) {
            assertTrue(function(code).computeContextDependent());
            assertFalse(function(code, expr("x")).computeContextDependent());
        }

        int[] neverContextDependent = {
            Compiler.FUNCTION_COUNT,
            Compiler.FUNCTION_ID,
            Compiler.FUNCTION_CONCAT,
            Compiler.FUNCTION_STARTS_WITH,
            Compiler.FUNCTION_CONTAINS,
            Compiler.FUNCTION_SUBSTRING_BEFORE,
            Compiler.FUNCTION_SUBSTRING_AFTER,
            Compiler.FUNCTION_SUBSTRING,
            Compiler.FUNCTION_STRING_LENGTH,
            Compiler.FUNCTION_NORMALIZE_SPACE,
            Compiler.FUNCTION_TRANSLATE,
            Compiler.FUNCTION_NOT,
            Compiler.FUNCTION_TRUE,
            Compiler.FUNCTION_FALSE,
            Compiler.FUNCTION_SUM,
            Compiler.FUNCTION_FLOOR,
            Compiler.FUNCTION_CEILING,
            Compiler.FUNCTION_ROUND,
            Compiler.FUNCTION_NULL
        };
        for (int code : neverContextDependent) {
            assertFalse(function(code).computeContextDependent());
        }

        assertTrue(function(
                Compiler.FUNCTION_COUNT, dependentExpr("x")).computeContextDependent());

        assertFalse(function(Compiler.FUNCTION_FORMAT_NUMBER).computeContextDependent());
        assertFalse(function(
                Compiler.FUNCTION_FORMAT_NUMBER, expr("x")).computeContextDependent());
        assertTrue(function(
                Compiler.FUNCTION_FORMAT_NUMBER,
                expr("x"),
                expr("y")).computeContextDependent());
        assertFalse(function(
                Compiler.FUNCTION_FORMAT_NUMBER,
                expr("x"),
                expr("y"),
                expr("z")).computeContextDependent());
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("true()", functionNullArgs(Compiler.FUNCTION_TRUE).toString());
        assertEquals("concat()", new CoreFunction(
                Compiler.FUNCTION_CONCAT, new Expression[0]).toString());
        assertEquals("concat(a, b)", function(
                Compiler.FUNCTION_CONCAT, expr("a"), expr("b")).toString());
    }

    @Test(timeout = 4000)
    public void testGetArgumentCountAndAccessors() {
        assertEquals(0, functionNullArgs(Compiler.FUNCTION_TRUE).getArgumentCount());
        assertEquals(0, new CoreFunction(
                Compiler.FUNCTION_TRUE, new Expression[0]).getArgumentCount());

        Expression a = expr("a");
        Expression b = expr("b");
        Expression c = expr("c");
        CoreFunction function = function(Compiler.FUNCTION_CONCAT, a, b, c);
        assertEquals(3, function.getArgumentCount());
        assertSame(a, function.getArg1());
        assertSame(b, function.getArg2());
        assertSame(c, function.getArg3());
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testLastInvalidArgumentCount() {
        function(Compiler.FUNCTION_LAST, expr("x")).computeValue(
                new StubEvalContext(null, null, 0));
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testRoundInvalidArgumentCount() {
        function(Compiler.FUNCTION_ROUND).computeValue(null);
    }
}