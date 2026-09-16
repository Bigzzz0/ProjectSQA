package org.apache.commons.jxpath.ri.compiler;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.jxpath.ri.compiler.CoreFunction
 *
 * Decision / Condition Coverage Targets:
 * 1. getFunctionName() - 29 switch cases and default fallback ("unknownFunction...").
 * 2. getArgumentCount(), getArg1(), getArg2(), getArg3() - null vs populated args array.
 * 3. computeContextDependent() - super check; last, position; 0-arg context dependents
 *    (boolean, local-name, name, namespace-uri, string, lang, number); format-number (2 args);
 *    non-context dependent functions.
 * 4. toString() - null args, 0 args, single arg, multiple args (comma joining).
 * 5. compute() / computeValue() - dispatch to 30 function handlers + default null return.
 * 6. functionLast() & functionPosition() - 0-arg guard; old position == 0 vs != 0 restoration.
 * 7. functionCount() - 1-arg guard; NodePointer unwrapping; EvalContext iteration; Collection sizing;
 *    null (returns 0); primitive/other (returns 1).
 * 8. functionLang() - singleNodePointer null vs non-null; pointer.isLanguage() true/false.
 * 9. functionID() - context pointer resolution; pointer.getPointerByID().
 * 10. functionKey() - 2-arg guard; EvalContext empty (BasicNodeSet) vs non-empty accumulation;
 *     non-EvalContext value.
 * 11. functionNamespaceURI(), functionLocalName(), functionName() - 0-arg (context pointer) vs
 *     1-arg (EvalContext with/without elements) vs non-EvalContext fallback.
 * 12. functionString() - 0-arg vs 1-arg InfoSetUtil conversion.
 * 13. functionConcat() - < 2 args throws exception; >= 2 args string concatenation.
 * 14. functionStartsWith() & functionContains() - matches vs non-matches.
 * 15. functionSubstringBefore() & functionSubstringAfter() - match found vs missing match ("").
 * 16. functionSubstring() - invalid arg counts (<2 or >3); NaN start; from > len + 1;
 *     2-arg branch (from < 1 clamp); 3-arg branch (length < 0, to < 1, to > len + 1, normal slice).
 * 17. functionStringLength() - 0-arg vs 1-arg.
 * 18. functionNormalizeSpace() - leading/middle/trailing whitespace, tabs (0x9), returns (0xD), newlines (0xA).
 * 19. functionTranslate() - character matching, replacement, truncation when s2 length > s3 length.
 * 20. functionBoolean(), functionNot(), functionTrue(), functionFalse(), functionNull().
 * 21. functionNumber() - 0-arg vs 1-arg.
 * 22. functionSum() - null argument -> ZERO; EvalContext elements accumulation; non-EvalContext throws JXPathException.
 * 23. functionFloor(), functionCeiling(), functionRound().
 * 24. functionFormatNumber() - 2-arg (pointer locale vs context locale); 3-arg (custom symbol table); invalid arg count.
 * 25. assertArgRange() / assertArgCount() - underflow/overflow triggers JXPathInvalidSyntaxException.
 *
 * Known Defect Target (Defects4J):
 * - round('NaN') in XPath 1.0 specification MUST return NaN, but Math.round(Double.NaN) returns 0L,
 *   erroneously yielding 0.0 instead of NaN.
 */

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreFunctionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetFunctionCodeAndArgs() {
        Constant c1 = new Constant("arg1");
        Constant c2 = new Constant(new Double(2));
        Constant c3 = new Constant("arg3");
        CoreFunction cf = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{c1, c2, c3});

        assertEquals(Compiler.FUNCTION_SUBSTRING, cf.getFunctionCode());
        assertEquals(3, cf.getArgumentCount());
        assertSame(c1, cf.getArg1());
        assertSame(c2, cf.getArg2());
        assertSame(c3, cf.getArg3());
    }

    @Test(timeout = 4000)
    public void testGetArgumentCountWithNullArgs() {
        CoreFunction cf = new CoreFunction(Compiler.FUNCTION_TRUE, null);
        assertEquals(0, cf.getArgumentCount());
        assertNull(cf.getArguments());
    }

    @Test(timeout = 4000)
    public void testGetFunctionNameAllCases() {
        int[] codes = {
            Compiler.FUNCTION_LAST, Compiler.FUNCTION_POSITION, Compiler.FUNCTION_COUNT,
            Compiler.FUNCTION_ID, Compiler.FUNCTION_LOCAL_NAME, Compiler.FUNCTION_NAMESPACE_URI,
            Compiler.FUNCTION_NAME, Compiler.FUNCTION_STRING, Compiler.FUNCTION_CONCAT,
            Compiler.FUNCTION_STARTS_WITH, Compiler.FUNCTION_CONTAINS, Compiler.FUNCTION_SUBSTRING_BEFORE,
            Compiler.FUNCTION_SUBSTRING_AFTER, Compiler.FUNCTION_SUBSTRING, Compiler.FUNCTION_STRING_LENGTH,
            Compiler.FUNCTION_NORMALIZE_SPACE, Compiler.FUNCTION_TRANSLATE, Compiler.FUNCTION_BOOLEAN,
            Compiler.FUNCTION_NOT, Compiler.FUNCTION_TRUE, Compiler.FUNCTION_FALSE,
            Compiler.FUNCTION_LANG, Compiler.FUNCTION_NUMBER, Compiler.FUNCTION_SUM,
            Compiler.FUNCTION_FLOOR, Compiler.FUNCTION_CEILING, Compiler.FUNCTION_ROUND,
            Compiler.FUNCTION_KEY, Compiler.FUNCTION_FORMAT_NUMBER
        };

        String[] expectedNames = {
            "last", "position", "count", "id", "local-name", "namespace-uri", "name",
            "string", "concat", "starts-with", "contains", "substring-before", "substring-after",
            "substring", "string-length", "normalize-space", "translate", "boolean", "not",
            "true", "false", "lang", "number", "sum", "floor", "ceiling", "round", "key", "format-number"
        };

        for (int i = 0; i < codes.length; i++) {
            CoreFunction cf = new CoreFunction(codes[i], null);
            assertEquals(expectedNames[i], cf.getFunctionName());
        }

        CoreFunction unknown = new CoreFunction(99999, null);
        assertEquals("unknownFunction99999()", unknown.getFunctionName());
    }

    @Test(timeout = 4000)
    public void testToString() {
        CoreFunction noArgs = new CoreFunction(Compiler.FUNCTION_TRUE, null);
        assertEquals("true()", noArgs.toString());

        CoreFunction emptyArgs = new CoreFunction(Compiler.FUNCTION_FALSE, new Expression[0]);
        assertEquals("false()", emptyArgs.toString());

        CoreFunction oneArg = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[]{new Constant("x")});
        assertEquals("not('x')", oneArg.toString());

        CoreFunction multiArgs = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("a"), new Constant("b"), new Constant("c")});
        assertEquals("concat('a', 'b', 'c')", multiArgs.toString());
    }

    @Test(timeout = 4000)
    public void testComputeContextDependent() {
        CoreFunction lastNoArg = new CoreFunction(Compiler.FUNCTION_LAST, null);
        assertTrue(lastNoArg.computeContextDependent());

        CoreFunction posNoArg = new CoreFunction(Compiler.FUNCTION_POSITION, new Expression[0]);
        assertTrue(posNoArg.computeContextDependent());

        int[] zeroArgDependents = {
            Compiler.FUNCTION_BOOLEAN, Compiler.FUNCTION_LOCAL_NAME, Compiler.FUNCTION_NAME,
            Compiler.FUNCTION_NAMESPACE_URI, Compiler.FUNCTION_STRING, Compiler.FUNCTION_LANG,
            Compiler.FUNCTION_NUMBER
        };

        for (int code : zeroArgDependents) {
            CoreFunction cfNull = new CoreFunction(code, null);
            assertTrue("Expected null args to be context dependent for code: " + code, cfNull.computeContextDependent());
            CoreFunction cfEmpty = new CoreFunction(code, new Expression[0]);
            assertTrue("Expected 0 args to be context dependent for code: " + code, cfEmpty.computeContextDependent());
            CoreFunction cfWithArg = new CoreFunction(code, new Expression[]{new Constant("val")});
            assertFalse("Expected non-zero args not to be context dependent for code: " + code, cfWithArg.computeContextDependent());
        }

        CoreFunction formatNumber2Args = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(1), new Constant("#")});
        assertTrue(formatNumber2Args.computeContextDependent());

        CoreFunction formatNumber3Args = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(1), new Constant("#"), new Constant("fmt")});
        assertFalse(formatNumber3Args.computeContextDependent());

        CoreFunction countFn = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{new Constant("x")});
        assertFalse(countFn.computeContextDependent());

        CoreFunction unknown = new CoreFunction(99999, null);
        assertFalse(unknown.computeContextDependent());
    }

    @Test(timeout = 4000)
    public void testComputeValueBooleanNotTrueFalseNull() {
        CoreFunction trueFn = new CoreFunction(Compiler.FUNCTION_TRUE, null);
        assertEquals(Boolean.TRUE, trueFn.compute(null));

        CoreFunction falseFn = new CoreFunction(Compiler.FUNCTION_FALSE, null);
        assertEquals(Boolean.FALSE, falseFn.compute(null));

        CoreFunction nullFn = new CoreFunction(Compiler.FUNCTION_NULL, null);
        assertNull(nullFn.compute(null));

        CoreFunction boolFn = new CoreFunction(Compiler.FUNCTION_BOOLEAN, new Expression[]{new Constant("true")});
        assertEquals(Boolean.TRUE, boolFn.compute(null));

        CoreFunction notFn = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[]{new Constant(Boolean.TRUE)});
        assertEquals(Boolean.FALSE, notFn.compute(null));

        CoreFunction notFn2 = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[]{new Constant(Boolean.FALSE)});
        assertEquals(Boolean.TRUE, notFn2.compute(null));
    }

    @Test(timeout = 4000)
    public void testStringFunctions() {
        // Concat
        CoreFunction concat = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("A"), new Constant("B"), new Constant("C")});
        assertEquals("ABC", concat.computeValue(null));

        // Starts-with
        CoreFunction startsWithTrue = new CoreFunction(Compiler.FUNCTION_STARTS_WITH,
                new Expression[]{new Constant("hello world"), new Constant("hello")});
        assertEquals(Boolean.TRUE, startsWithTrue.computeValue(null));

        CoreFunction startsWithFalse = new CoreFunction(Compiler.FUNCTION_STARTS_WITH,
                new Expression[]{new Constant("hello world"), new Constant("world")});
        assertEquals(Boolean.FALSE, startsWithFalse.computeValue(null));

        // Contains
        CoreFunction containsTrue = new CoreFunction(Compiler.FUNCTION_CONTAINS,
                new Expression[]{new Constant("hello world"), new Constant("lo wo")});
        assertEquals(Boolean.TRUE, containsTrue.computeValue(null));

        CoreFunction containsFalse = new CoreFunction(Compiler.FUNCTION_CONTAINS,
                new Expression[]{new Constant("hello world"), new Constant("xyz")});
        assertEquals(Boolean.FALSE, containsFalse.computeValue(null));

        // Substring-before
        CoreFunction beforeMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE,
                new Expression[]{new Constant("1999/04/01"), new Constant("/")});
        assertEquals("1999", beforeMatch.computeValue(null));

        CoreFunction beforeNoMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE,
                new Expression[]{new Constant("1999/04/01"), new Constant("?")});
        assertEquals("", beforeNoMatch.computeValue(null));

        // Substring-after
        CoreFunction afterMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER,
                new Expression[]{new Constant("1999/04/01"), new Constant("/")});
        assertEquals("04/01", afterMatch.computeValue(null));

        CoreFunction afterNoMatch = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER,
                new Expression[]{new Constant("1999/04/01"), new Constant("?")});
        assertEquals("", afterNoMatch.computeValue(null));

        // Translate
        CoreFunction translate = new CoreFunction(Compiler.FUNCTION_TRANSLATE,
                new Expression[]{new Constant("bar"), new Constant("abc"), new Constant("ABC")});
        assertEquals("BAr", translate.computeValue(null));

        // Translate with truncation (s2 longer than s3)
        CoreFunction translateTrunc = new CoreFunction(Compiler.FUNCTION_TRANSLATE,
                new Expression[]{new Constant("--aaa--"), new Constant("abc-"), new Constant("ABC")});
        assertEquals("AAA", translateTrunc.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testNormalizeSpace() {
        CoreFunction norm = new CoreFunction(Compiler.FUNCTION_NORMALIZE_SPACE,
                new Expression[]{new Constant(" \t\r\n hello   world \r\n ")});
        assertEquals("hello world", norm.computeValue(null));

        CoreFunction normEmpty = new CoreFunction(Compiler.FUNCTION_NORMALIZE_SPACE,
                new Expression[]{new Constant("   \t  ")});
        assertEquals("", normEmpty.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testMathFunctions() {
        CoreFunction floor = new CoreFunction(Compiler.FUNCTION_FLOOR, new Expression[]{new Constant(new Double(2.7))});
        assertEquals(new Double(2.0), floor.computeValue(null));

        CoreFunction ceiling = new CoreFunction(Compiler.FUNCTION_CEILING, new Expression[]{new Constant(new Double(2.3))});
        assertEquals(new Double(3.0), ceiling.computeValue(null));

        CoreFunction round = new CoreFunction(Compiler.FUNCTION_ROUND, new Expression[]{new Constant(new Double(2.5))});
        assertEquals(new Double(3.0), round.computeValue(null));

        CoreFunction number = new CoreFunction(Compiler.FUNCTION_NUMBER, new Expression[]{new Constant("123.45")});
        assertEquals(new Double(123.45), number.computeValue(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubstringBoundaries() {
        // from is NaN
        CoreFunction subNan = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(Double.NaN))});
        assertEquals("", subNan.computeValue(null));

        // from > length + 1
        CoreFunction subBeyond = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(10))});
        assertEquals("", subBeyond.computeValue(null));

        // 2-arg: from < 1 clamped to 1
        CoreFunction subZeroFrom2Arg = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(-2))});
        assertEquals("hello", subZeroFrom2Arg.computeValue(null));

        // 3-arg: length < 0 -> empty
        CoreFunction subNegLen = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(2)), new Constant(new Double(-1))});
        assertEquals("", subNegLen.computeValue(null));

        // 3-arg: to < 1 -> empty
        CoreFunction subToUnder1 = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(-5)), new Constant(new Double(2))});
        assertEquals("", subToUnder1.computeValue(null));

        // 3-arg: to > s1.length() + 1
        CoreFunction subToBeyond = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(0)), new Constant(new Double(10))});
        assertEquals("hello", subToBeyond.computeValue(null));

        // 3-arg: normal slice with from < 1
        CoreFunction subNormalFromUnder1 = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(0)), new Constant(new Double(3))});
        assertEquals("he", subNormalFromUnder1.computeValue(null));

        // 3-arg: normal slice fully within range
        CoreFunction subNormal = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("hello"), new Constant(new Double(2)), new Constant(new Double(3))});
        assertEquals("ell", subNormal.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testStringLengthFunction() {
        CoreFunction strLen1Arg = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH,
                new Expression[]{new Constant("test-string")});
        assertEquals(new Double(11), strLen1Arg.computeValue(null));

        CoreFunction strLenEmpty = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH,
                new Expression[]{new Constant("")});
        assertEquals(new Double(0), strLenEmpty.computeValue(null));
    }

    @Test(timeout = 4000)
    public void testCountFunctionVariations() {
        // Value is Collection
        CoreFunction countColl = new CoreFunction(Compiler.FUNCTION_COUNT,
                new Expression[]{new Constant(Arrays.asList("a", "b", "c"))});
        assertEquals(new Double(3.0), countColl.computeValue(null));

        // Value is null
        CoreFunction countNull = new CoreFunction(Compiler.FUNCTION_COUNT,
                new Expression[]{new Constant((String) null)});
        assertEquals(new Double(0.0), countNull.computeValue(null));

        // Value is primitive / other object
        CoreFunction countScalar = new CoreFunction(Compiler.FUNCTION_COUNT,
                new Expression[]{new Constant(new Double(42))});
        assertEquals(new Double(1.0), countScalar.computeValue(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * CRITICAL REQUIREMENT FOR FAULT DETECTION:
     * According to XPath 1.0 section 4.4, round('NaN') MUST return NaN.
     * In the defective implementation:
     * functionRound() calls Math.round(v) where v = Double.NaN.
     * Math.round(Double.NaN) returns 0L, producing 0.0 instead of NaN.
     */
    @Test(timeout = 4000)
    public void testRoundNaNYieldsNaN() {
        CoreFunction roundNaN = new CoreFunction(Compiler.FUNCTION_ROUND,
                new Expression[]{new Constant("NaN")});
        Object result = roundNaN.computeValue(null);
        assertNotNull("Result should not be null", result);
        assertTrue("Result must be a Double", result instanceof Double);
        assertEquals(new Double(Double.NaN), result);
    }

    // =========================================================================
    // Partition D: Context-Aware Execution Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testContextAwarePositionAndLast() {
        List<String> list = Arrays.asList("alpha", "beta", "gamma");
        JXPathContext context = JXPathContext.newContext(list);
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) context;
        RootContext rootContext = jxContext.getAbsoluteRootContext();
        InitialContext initialContext = new InitialContext(rootContext);

        // Position when initialContext has not moved
        CoreFunction posFn = new CoreFunction(Compiler.FUNCTION_POSITION, new Expression[0]);
        assertEquals(new Integer(0), posFn.computeValue(initialContext));

        // Last function iterating and restoring position
        CoreFunction lastFn = new CoreFunction(Compiler.FUNCTION_LAST, new Expression[0]);
        assertEquals(new Double(1.0), lastFn.computeValue(initialContext));

        // Move position and verify last restores old position
        initialContext.setPosition(1);
        assertEquals(new Integer(1), posFn.computeValue(initialContext));
        assertEquals(new Double(1.0), lastFn.computeValue(initialContext));
        assertEquals(new Integer(1), posFn.computeValue(initialContext));
    }

    @Test(timeout = 4000)
    public void testContextNodePointerFunctions() {
        JXPathContext context = JXPathContext.newContext("nodeValue");
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) context;
        EvalContext evalContext = jxContext.getAbsoluteRootContext();

        // string() with 0 args
        CoreFunction strFn0 = new CoreFunction(Compiler.FUNCTION_STRING, new Expression[0]);
        assertEquals("nodeValue", strFn0.computeValue(evalContext));

        // string() with 1 arg
        CoreFunction strFn1 = new CoreFunction(Compiler.FUNCTION_STRING, new Expression[]{new Constant(new Double(100))});
        assertEquals("100", strFn1.computeValue(evalContext));

        // string-length() with 0 args
        CoreFunction lenFn0 = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH, new Expression[0]);
        assertEquals(new Double(9.0), lenFn0.computeValue(evalContext));

        // number() with 0 args
        CoreFunction numFn0 = new CoreFunction(Compiler.FUNCTION_NUMBER, new Expression[0]);
        assertTrue(((Double) numFn0.computeValue(evalContext)).isNaN());

        // name(), local-name(), namespace-uri() with 0 args
        CoreFunction nameFn0 = new CoreFunction(Compiler.FUNCTION_NAME, new Expression[0]);
        assertNotNull(nameFn0.computeValue(evalContext));

        CoreFunction localNameFn0 = new CoreFunction(Compiler.FUNCTION_LOCAL_NAME, new Expression[0]);
        assertNull(localNameFn0.computeValue(evalContext));

        CoreFunction nsUriFn0 = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, new Expression[0]);
        assertEquals("", nsUriFn0.computeValue(evalContext));

        // lang() with 1 arg
        CoreFunction langFn = new CoreFunction(Compiler.FUNCTION_LANG, new Expression[]{new Constant("en")});
        Object langRes = langFn.computeValue(evalContext);
        assertNotNull(langRes);
        assertTrue(langRes instanceof Boolean);
    }

    @Test(timeout = 4000)
    public void testNameFunctionsWithEvalContextArgs() {
        BasicNodeSet nodeSet = new BasicNodeSet();
        JXPathContext context = JXPathContext.newContext("root");
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) context;
        RootContext rootContext = jxContext.getAbsoluteRootContext();
        NodeSetContext nsc = new NodeSetContext(rootContext, nodeSet);

        // When EvalContext has no elements -> return ""
        CoreFunction nameFn1 = new CoreFunction(Compiler.FUNCTION_NAME, new Expression[]{new Constant("x") {
            @Override
            public Object compute(EvalContext c) {
                return nsc;
            }
        }});
        assertEquals("", nameFn1.computeValue(rootContext));

        CoreFunction localNameFn1 = new CoreFunction(Compiler.FUNCTION_LOCAL_NAME, new Expression[]{new Constant("x") {
            @Override
            public Object compute(EvalContext c) {
                return nsc;
            }
        }});
        assertEquals("", localNameFn1.computeValue(rootContext));

        CoreFunction nsUriFn1 = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, new Expression[]{new Constant("x") {
            @Override
            public Object compute(EvalContext c) {
                return nsc;
            }
        }});
        assertEquals("", nsUriFn1.computeValue(rootContext));

        // When arg computes to a non-EvalContext object -> return ""
        CoreFunction nameFnNonCtx = new CoreFunction(Compiler.FUNCTION_NAME, new Expression[]{new Constant("simpleString")});
        assertEquals("", nameFnNonCtx.computeValue(rootContext));
    }

    @Test(timeout = 4000)
    public void testFunctionSum() {
        // null argument evaluates to ZERO (0.0)
        CoreFunction sumNull = new CoreFunction(Compiler.FUNCTION_SUM, new Expression[]{new Constant((String) null) {
            @Override
            public Object compute(EvalContext c) {
                return null;
            }
        }});
        assertEquals(new Double(0.0), sumNull.computeValue(null));

        // EvalContext elements sum
        JXPathContext context = JXPathContext.newContext(Arrays.asList(new Double(10.5), new Double(20.5)));
        Double sumVal = (Double) context.getValue("sum(.)");
        assertEquals(31.0, sumVal.doubleValue(), 0.0001);
    }

    @Test(expected = JXPathException.class, timeout = 4000)
    public void testFunctionSumThrowsOnInvalidArgType() {
        CoreFunction sumInvalid = new CoreFunction(Compiler.FUNCTION_SUM, new Expression[]{new Constant("invalid")});
        sumInvalid.computeValue(null);
    }

    @Test(timeout = 4000)
    public void testFunctionKeyAndID() {
        JXPathContext context = JXPathContext.newContext(Collections.singletonMap("k1", "v1"));
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) context;
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        // id() function
        CoreFunction idFn = new CoreFunction(Compiler.FUNCTION_ID, new Expression[]{new Constant("someId")});
        Object idResult = idFn.computeValue(rootContext);
        assertNull(idResult);

        // key() function with empty EvalContext
        BasicNodeSet emptySet = new BasicNodeSet();
        NodeSetContext emptyNsc = new NodeSetContext(rootContext, emptySet);
        CoreFunction keyEmptyFn = new CoreFunction(Compiler.FUNCTION_KEY,
                new Expression[]{new Constant("keyName"), new Constant("dummy") {
                    @Override
                    public Object compute(EvalContext c) {
                        return emptyNsc;
                    }
                }});
        Object keyEmptyRes = keyEmptyFn.computeValue(rootContext);
        assertTrue(keyEmptyRes instanceof BasicNodeSet);

        // key() function with normal string value
        CoreFunction keyNormalFn = new CoreFunction(Compiler.FUNCTION_KEY,
                new Expression[]{new Constant("keyName"), new Constant("valName")});
        Object keyNormalRes = keyNormalFn.computeValue(rootContext);
        assertTrue(keyNormalRes instanceof NodeSetContext);
    }

    @Test(timeout = 4000)
    public void testFunctionFormatNumber() {
        JXPathContext context = JXPathContext.newContext(new Object());
        context.setDecimalFormatSymbols("custom", new DecimalFormatSymbols(Locale.US));
        JXPathContextReferenceImpl jxContext = (JXPathContextReferenceImpl) context;
        RootContext rootContext = jxContext.getAbsoluteRootContext();

        // 2 args: format-number(1234.56, '#,##0.00')
        CoreFunction fn2 = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(new Double(1234.56)), new Constant("#,##0.00")});
        String res2 = (String) fn2.computeValue(rootContext);
        assertNotNull(res2);
        assertTrue(res2.contains("1") && res2.contains("234"));

        // 3 args: format-number(1234.56, '#,##0.00', 'custom')
        CoreFunction fn3 = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(new Double(1234.56)), new Constant("#,##0.00"), new Constant("custom")});
        String res3 = (String) fn3.computeValue(rootContext);
        assertEquals("1,234.56", res3);
    }

    // =========================================================================
    // Partition E: Defensive Guard & Exception Paths
    // =========================================================================

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testAssertArgCountUnderflow() {
        CoreFunction notNoArgs = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[0]);
        notNoArgs.computeValue(null);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testAssertArgCountOverflow() {
        CoreFunction trueWithArg = new CoreFunction(Compiler.FUNCTION_TRUE,
                new Expression[]{new Constant(Boolean.TRUE)});
        trueWithArg.computeValue(null);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testConcatUnderflow() {
        CoreFunction concatOneArg = new CoreFunction(Compiler.FUNCTION_CONCAT,
                new Expression[]{new Constant("onlyOne")});
        concatOneArg.computeValue(null);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testSubstringInvalidArgCount() {
        CoreFunction subOneArg = new CoreFunction(Compiler.FUNCTION_SUBSTRING,
                new Expression[]{new Constant("onlyOne")});
        subOneArg.computeValue(null);
    }

    @Test(expected = JXPathInvalidSyntaxException.class, timeout = 4000)
    public void testFormatNumberInvalidArgCount() {
        CoreFunction fn1Arg = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER,
                new Expression[]{new Constant(1)});
        fn1Arg.computeValue(null);
    }

    @Test(timeout = 4000)
    public void testComputeValueDefaultBranchReturnsNull() {
        CoreFunction unknown = new CoreFunction(99999, null);
        assertNull(unknown.computeValue(null));
    }
}