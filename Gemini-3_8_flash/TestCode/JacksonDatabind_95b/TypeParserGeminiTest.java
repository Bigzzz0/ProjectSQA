package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: com.fasterxml.jackson.databind.type.TypeParser
 * Target Defect   : TestTypeFactory::testCanonicalNames -> NullPointerException in type resolution
 *
 * Decision Branches & Boundary Conditions Targeted:
 * 1. withFactory(TypeFactory f):
 *    - Branch A: (f == _factory) -> returns existing `this` instance
 *    - Branch B: (f != _factory) -> instantiates new TypeParser(f)
 * 2. parse(String canonical):
 *    - Leading/trailing whitespace trimming via canonical.trim()
 *    - Branch A: (!tokens.hasMoreTokens()) -> valid single/generic type parsed completely
 *    - Branch B: (tokens.hasMoreTokens()) -> throws IllegalArgumentException ("Unexpected tokens after complete type")
 * 3. parseType(MyTokenizer tokens):
 *    - Branch A: (!tokens.hasMoreTokens()) -> throws IllegalArgumentException ("Unexpected end-of-string")
 *    - Branch B: (tokens.hasMoreTokens() && "<".equals(token)) -> generic type parameters parsed via parseTypes
 *    - Branch C: (tokens.hasMoreTokens() && !"<".equals(token)) -> tokens.pushBack(token), non-generic type returned
 *    - Branch D: (!tokens.hasMoreTokens() after findClass) -> non-generic type returned directly
 * 4. parseTypes(MyTokenizer tokens):
 *    - While loop iteration for 1..N generic type parameters
 *    - Branch A: (!tokens.hasMoreTokens()) -> break -> throws IllegalArgumentException ("Unexpected end-of-string")
 *    - Branch B: (">".equals(token)) -> successfully returns parsed List<JavaType>
 *    - Branch C: (",".equals(token)) -> continue loop for next type parameter
 *    - Branch D: (!",".equals(token) && !">".equals(token)) -> throws IllegalArgumentException ("Unexpected token...")
 * 5. findClass(String className, MyTokenizer tokens):
 *    - Normal: _factory.findClass(className) succeeds
 *    - Checked Exception: ClassNotFoundException wrapped into IllegalArgumentException with location & problem msg
 *    - RuntimeException: rethrown directly without wrapping (instanceof RuntimeException)
 * 6. MyTokenizer Tokenization & Pushback Contract:
 *    - constructor: delimiters "<,>" with returnDelims=true
 *    - hasMoreTokens: _pushbackToken != null || super.hasMoreTokens()
 *    - nextToken: retrieves and clears _pushbackToken or advances tokenizer and updates _index
 *    - pushBack: stores token without altering _index
 *    - getAllInput & getRemainingInput: boundary verification at start, mid, and end of stream
 * ====================================================================================================
 */
public class TypeParserGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseSimpleNonGenericClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType type = parser.parse("java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testParseGenericSingleParameter() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType type = parser.parse("java.util.ArrayList<java.lang.Integer>");
        assertNotNull(type);
        assertEquals(java.util.ArrayList.class, type.getRawClass());
        assertTrue(type.isContainerType());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Integer.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseGenericMultipleParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType type = parser.parse("java.util.HashMap<java.lang.String,java.lang.Double>");
        assertNotNull(type);
        assertEquals(java.util.HashMap.class, type.getRawClass());
        assertTrue(type.isContainerType());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Double.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseNestedGenericParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType type = parser.parse("java.util.Map<java.lang.String,java.util.List<java.lang.Long>>");
        assertNotNull(type);
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());

        JavaType valueType = type.containedType(1);
        assertEquals(java.util.List.class, valueType.getRawClass());
        assertEquals(1, valueType.containedTypeCount());
        assertEquals(Long.class, valueType.containedType(0).getRawClass());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseWithExtensiveWhitespace() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType type = parser.parse("   java.util.Map <  java.lang.String ,   java.lang.Integer   >   ");
        assertNotNull(type);
        assertEquals(java.util.Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseEmptyStringThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("");
            fail("Expected IllegalArgumentException for empty input string");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected end-of-string'",
                    e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test(timeout = 4000)
    public void testParseWhitespaceOnlyStringThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("     ");
            fail("Expected IllegalArgumentException for whitespace-only input");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected end-of-string'",
                    e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test(timeout = 4000)
    public void testParseExtraTrailingTokensSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.lang.String extra");
            fail("Expected IllegalArgumentException for trailing tokens");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected tokens after complete type'",
                    e.getMessage().contains("Unexpected tokens after complete type"));
        }
    }

    @Test(timeout = 4000)
    public void testParseExtraTrailingTokensAfterGeneric() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.util.List<java.lang.String> trailing");
            fail("Expected IllegalArgumentException for trailing tokens after generic type");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected tokens after complete type'",
                    e.getMessage().contains("Unexpected tokens after complete type"));
        }
    }

    @Test(timeout = 4000)
    public void testParseUnbalancedClosingToken() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.lang.String>");
            fail("Expected IllegalArgumentException for unbalanced closing bracket");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected tokens after complete type'",
                    e.getMessage().contains("Unexpected tokens after complete type"));
        }
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (TestTypeFactory::testCanonicalNames Defect)
    // ================================================================================================

    /**
     * Directly targets the failure condition identified in Defects4J
     * `TestTypeFactory::testCanonicalNames` where canonical type resolution failed with NullPointerException.
     */
    @Test(timeout = 4000)
    public void testCanonicalNamesDefectTarget() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType cal = parser.parse("java.util.Calendar");
        assertNotNull(cal);
        assertEquals(java.util.Calendar.class, cal.getRawClass());
        assertFalse(cal.isContainerType());

        JavaType list = parser.parse("java.util.ArrayList<java.lang.String>");
        assertNotNull(list);
        assertEquals(java.util.ArrayList.class, list.getRawClass());
        assertTrue(list.isContainerType());
        assertEquals(1, list.containedTypeCount());
        assertEquals(String.class, list.containedType(0).getRawClass());

        JavaType map = parser.parse("java.util.Map<java.lang.String,java.lang.Long>");
        assertNotNull(map);
        assertEquals(java.util.Map.class, map.getRawClass());
        assertTrue(map.isContainerType());
        assertEquals(2, map.containedTypeCount());
        assertEquals(String.class, map.containedType(0).getRawClass());
        assertEquals(Long.class, map.containedType(1).getRawClass());

        JavaType lmap = parser.parse("java.util.LinkedHashMap<java.lang.String,java.lang.Integer>");
        assertNotNull(lmap);
        assertEquals(java.util.LinkedHashMap.class, lmap.getRawClass());
        assertTrue(lmap.isContainerType());

        JavaType tmap = parser.parse("java.util.TreeMap<java.lang.String,java.lang.Integer>");
        assertNotNull(tmap);
        assertEquals(java.util.TreeMap.class, tmap.getRawClass());
        assertTrue(tmap.isContainerType());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedCanonicalNames() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        JavaType deepType = parser.parse(
                "java.util.Map<java.lang.String,java.util.Map<java.lang.Integer,java.util.List<java.lang.Boolean>>>");
        assertNotNull(deepType);
        assertEquals(java.util.Map.class, deepType.getRawClass());
        JavaType nestedMap = deepType.containedType(1);
        assertEquals(java.util.Map.class, nestedMap.getRawClass());
        JavaType nestedList = nestedMap.containedType(1);
        assertEquals(java.util.List.class, nestedList.getRawClass());
        assertEquals(Boolean.class, nestedList.containedType(0).getRawClass());
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseUnclosedGenericBracketThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.util.List<java.lang.String");
            fail("Expected IllegalArgumentException for unclosed generic bracket");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected end-of-string'",
                    e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test(timeout = 4000)
    public void testParseEmptyGenericParametersThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.util.List<>");
            fail("Expected IllegalArgumentException for empty generic parameters");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention cannot locate class or unexpected token",
                    e.getMessage().contains("Can not locate class ''") || e.getMessage().contains("Unexpected token"));
        }
    }

    @Test(timeout = 4000)
    public void testParseTrailingCommaInGenericsThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("java.util.Map<java.lang.String,>");
            fail("Expected IllegalArgumentException for trailing comma in generics");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention cannot locate class ''",
                    e.getMessage().contains("Can not locate class ''"));
        }
    }

    @Test(timeout = 4000)
    public void testParseUnexpectedTokenInGenericsList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        // Input where after parsing the inner generic type, the next separator is '<' instead of ',' or '>'
        try {
            parser.parse("java.util.List<java.lang.String < java.lang.Integer > < >");
            fail("Expected IllegalArgumentException for unexpected token in generics list");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention unexpected token and expected ',' or '>'",
                    e.getMessage().contains("expected ',' or '>'"));
        }
    }

    @Test(timeout = 4000)
    public void testFindClassNotFoundThrowsIllegalArgumentException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parse("com.nonexistent.package.FakeClass");
            fail("Expected IllegalArgumentException for non-existent class");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should indicate class cannot be located",
                    e.getMessage().contains("Can not locate class 'com.nonexistent.package.FakeClass'"));
        }
    }

    @Test(timeout = 4000)
    public void testFindClassRethrowsRuntimeExceptionDirectly() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            // Passing null className causes TypeFactory.findClass(null) to throw NullPointerException
            parser.findClass(null, new TypeParser.MyTokenizer("test"));
            fail("Expected NullPointerException to be rethrown directly");
        } catch (NullPointerException e) {
            // Correct: runtime exceptions must not be wrapped in _problem
            assertNotNull(e);
        }
    }

    @Test(timeout = 4000)
    public void testParseTypeDirectEmptyTokensThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parseType(new TypeParser.MyTokenizer(""));
            fail("Expected IllegalArgumentException on empty tokenizer");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected end-of-string'",
                    e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test(timeout = 4000)
    public void testParseTypesDirectEmptyTokensThrowsProblem() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        try {
            parser.parseTypes(new TypeParser.MyTokenizer(""));
            fail("Expected IllegalArgumentException on empty tokenizer in parseTypes");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should mention 'Unexpected end-of-string'",
                    e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test(timeout = 4000)
    public void testProblemMessageFormatting() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("java.util.List<foo>");
        assertEquals("java.util.List", tokenizer.nextToken()); // consumes 14 characters

        IllegalArgumentException problem = parser._problem(tokenizer, "Custom explanation");
        String message = problem.getMessage();

        assertTrue(message.contains("Failed to parse type 'java.util.List<foo>'"));
        assertTrue(message.contains("remaining: '<foo>'"));
        assertTrue(message.contains("Custom explanation"));
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testWithFactoryIdentityOptimization() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeParser parser = new TypeParser(tf);

        TypeParser sameParser = parser.withFactory(tf);
        assertSame("withFactory must return 'this' when passed the same TypeFactory instance",
                parser, sameParser);
    }

    @Test(timeout = 4000)
    public void testWithFactoryDifferentInstance() {
        TypeFactory tf1 = TypeFactory.defaultInstance();
        TypeFactory tf2 = tf1.withClassLoader(new ClassLoader() {});
        TypeParser parser = new TypeParser(tf1);

        TypeParser newParser = parser.withFactory(tf2);
        assertNotSame("withFactory must return a new TypeParser instance when passed a different factory",
                parser, newParser);
    }

    @Test(timeout = 4000)
    public void testMyTokenizerCompleteLifecycleAndPushback() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("java.util.Map<java.lang.String,java.lang.Integer>");

        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", tokenizer.getAllInput());
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", tokenizer.getRemainingInput());

        assertTrue(tokenizer.hasMoreTokens());
        String token1 = tokenizer.nextToken();
        assertEquals("java.util.Map", token1);

        // Pushback verification
        tokenizer.pushBack(token1);
        assertTrue(tokenizer.hasMoreTokens());
        String token1AfterPushback = tokenizer.nextToken();
        assertEquals("java.util.Map", token1AfterPushback);

        assertEquals("<", tokenizer.nextToken());
        assertEquals("java.lang.String", tokenizer.nextToken());
        assertEquals(",", tokenizer.nextToken());
        assertEquals("java.lang.Integer", tokenizer.nextToken());
        assertEquals(">", tokenizer.nextToken());

        assertFalse(tokenizer.hasMoreTokens());
        assertEquals("", tokenizer.getRemainingInput());
    }

    @Test(timeout = 4000)
    public void testMyTokenizerRemainingInputTrackingAcrossTokens() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("a,b");
        assertEquals("a,b", tokenizer.getRemainingInput());

        String t1 = tokenizer.nextToken();
        assertEquals("a", t1);
        assertEquals(",b", tokenizer.getRemainingInput());

        tokenizer.pushBack(t1);
        // Pushback preserves the index without changing it
        assertEquals(",b", tokenizer.getRemainingInput());
        assertEquals("a", tokenizer.nextToken());
        assertEquals(",b", tokenizer.getRemainingInput());

        String t2 = tokenizer.nextToken();
        assertEquals(",", t2);
        assertEquals("b", tokenizer.getRemainingInput());

        String t3 = tokenizer.nextToken();
        assertEquals("b", t3);
        assertEquals("", tokenizer.getRemainingInput());
        assertFalse(tokenizer.hasMoreTokens());
    }
}