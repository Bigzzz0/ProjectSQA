package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypeParser.parse(String) - recursive descent parser for canonical type strings.
 * 
 * Decision branches exercised:
 * 1. parse(): trim input, create tokenizer, parseType, check no trailing tokens.
 *    - Branch: tokens.hasMoreTokens() after parseType -> true (error path) / false (normal).
 * 2. parseType(): 
 *    - Branch: !tokens.hasMoreTokens() -> true (error: empty input) / false.
 *    - Branch: tokens.hasMoreTokens() after nextToken() -> true (check generics) / false (simple type).
 *    - Branch: token.equals("<") -> true (parse generics) / false (pushback and return simple).
 * 3. parseTypes():
 *    - Loop: while tokens.hasMoreTokens().
 *    - Branch: !tokens.hasMoreTokens() after parseType -> true (break) / false.
 *    - Branch: token.equals(">") -> true (return types) / false.
 *    - Branch: !token.equals(",") -> true (error) / false (continue).
 * 4. findClass(): 
 *    - Branch: exception caught -> if RuntimeException rethrow, else wrap in IllegalArgumentException.
 * 5. MyTokenizer:
 *    - hasMoreTokens(): pushback != null OR super.hasMoreTokens().
 *    - nextToken(): if pushback != null consume it, else super.nextToken() and trim.
 *    - pushBack(): set pushback token.
 * 
 * Defect targeting (from Defects4J): 
 * - testCanonicalNames triggers NullPointerException. 
 *   Root cause: When parsing a type with generics that has no type parameters (e.g., "java.util.List<>"), 
 *   parseTypes() returns an empty list, but TypeBindings.create(base, emptyList) may produce NPE 
 *   because the factory's _fromClass with empty bindings might not handle empty list correctly.
 *   The bug is in the interaction between parseTypes() and TypeBindings.create() when parameterTypes is empty.
 *   Test: parse "java.util.List<>" should either throw a meaningful IllegalArgumentException or return a valid type,
 *   but NOT throw NullPointerException.
 * 
 * Additional edge cases:
 * - Null input to parse() -> NPE (not handled, but we test for it).
 * - Empty string input -> IllegalArgumentException.
 * - Whitespace handling.
 * - Nested generics.
 * - Multiple type parameters.
 * - Invalid tokens.
 * - Class not found.
 * - Pushback logic in tokenizer.
 * 
 * Partitions:
 * A. Core functional: simple types, generic types with one/multiple params, nested generics.
 * B. Boundaries: empty string, null, whitespace-only, "List<>", "List< >", "List<,>", "List<Integer,>".
 * C. Defect-targeted: "java.util.List<>" (empty type params) - should not NPE.
 * D. Exceptions: invalid class names, malformed generics, unexpected tokens.
 * E. Lifecycle: withFactory() returns same instance if same factory, new instance otherwise.
 */

public class TypeParserDeepseekTest {

    private TypeFactory factory = TypeFactory.defaultInstance();
    private TypeParser parser = new TypeParser(factory);

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testParseSimpleType() {
        JavaType type = parser.parse("java.lang.String");
        assertNotNull(type);
        assertEquals("java.lang.String", type.toCanonical());
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseGenericType() {
        JavaType type = parser.parse("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals("java.util.List<java.lang.String>", type.toCanonical());
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseMultipleTypeParams() {
        JavaType type = parser.parse("java.util.Map<java.lang.String,java.lang.Integer>");
        assertNotNull(type);
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", type.toCanonical());
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseNestedGenerics() {
        JavaType type = parser.parse("java.util.List<java.util.List<java.lang.Integer>>");
        assertNotNull(type);
        assertEquals("java.util.List<java.util.List<java.lang.Integer>>", type.toCanonical());
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        JavaType inner = type.containedType(0);
        assertEquals(List.class, inner.getRawClass());
        assertEquals(Integer.class, inner.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespace() {
        JavaType type = parser.parse("  java.util.List < java.lang.String >  ");
        assertNotNull(type);
        assertEquals("java.util.List<java.lang.String>", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testParseArrayType() {
        JavaType type = parser.parse("java.lang.String[]");
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseEmptyString() {
        parser.parse("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseWhitespaceOnly() {
        parser.parse("   ");
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testParseNull() {
        parser.parse(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseEmptyGeneric() {
        // This should not NPE, but should throw a meaningful exception
        parser.parse("java.util.List<>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseGenericWithSpaces() {
        parser.parse("java.util.List< >");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseGenericWithOnlyComma() {
        parser.parse("java.util.List<,>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseGenericWithTrailingComma() {
        parser.parse("java.util.List<java.lang.String,>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseGenericWithLeadingComma() {
        parser.parse("java.util.List<,java.lang.String>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseUnclosedGeneric() {
        parser.parse("java.util.List<java.lang.String");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseExtraClosingBracket() {
        parser.parse("java.util.List<java.lang.String>>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseUnexpectedToken() {
        parser.parse("java.util.List<java.lang.String;>");
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testParseEmptyTypeParameters_ShouldNotThrowNPE() {
        // This is the defect: "java.util.List<>" caused NullPointerException
        // Expected behavior: either throw IllegalArgumentException (meaningful error)
        // or return a valid type, but NOT NPE.
        try {
            JavaType type = parser.parse("java.util.List<>");
            // If it returns a type, it should be a valid List type with no type params
            assertNotNull(type);
            assertEquals(List.class, type.getRawClass());
            assertEquals(0, type.containedTypeCount());
        } catch (IllegalArgumentException e) {
            // Acceptable - meaningful error message
            assertTrue(e.getMessage().contains("Failed to parse type"));
        }
        // If NPE is thrown, the test fails (which is the defect)
    }

    @Test(timeout = 4000)
    public void testParseNestedEmptyTypeParameters() {
        try {
            JavaType type = parser.parse("java.util.Map<java.lang.String,java.util.List<>>");
            assertNotNull(type);
            assertEquals(Map.class, type.getRawClass());
            assertEquals(2, type.containedTypeCount());
            JavaType inner = type.containedType(1);
            assertEquals(List.class, inner.getRawClass());
            assertEquals(0, inner.containedTypeCount());
        } catch (IllegalArgumentException e) {
            // Acceptable if meaningful error
            assertTrue(e.getMessage().contains("Failed to parse type"));
        }
    }

    @Test(timeout = 4000)
    public void testParseWithFactoryThatReturnsNull() {
        // Edge case: factory.findClass might return null for some reason
        TypeFactory customFactory = new TypeFactory() {
            @Override
            public Class<?> findClass(String className) throws ClassNotFoundException {
                if ("java.lang.NullClass".equals(className)) {
                    return null;
                }
                return super.findClass(className);
            }
        };
        TypeParser customParser = new TypeParser(customFactory);
        try {
            JavaType type = customParser.parse("java.lang.NullClass");
            // If null class is returned, behavior is undefined but should not NPE in parser
            assertNotNull(type); // Might fail if factory doesn't handle null
        } catch (IllegalArgumentException e) {
            // Acceptable
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseNonExistentClass() {
        parser.parse("com.nonexistent.NonExistentClass");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseInvalidClassName() {
        parser.parse("123Invalid");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParsePrimitiveType() {
        // Primitives are not valid canonical type names
        parser.parse("int");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseVoidType() {
        parser.parse("void");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseGenericWithInvalidClass() {
        parser.parse("java.util.List<com.nonexistent.NonExistentClass>");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseTrailingGarbage() {
        parser.parse("java.lang.String garbage");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseMultipleTopLevelTypes() {
        parser.parse("java.lang.String,java.lang.Integer");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseNestedGenericWithMissingComma() {
        parser.parse("java.util.Map<java.lang.String java.lang.Integer>");
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testWithFactorySameInstance() {
        TypeParser sameParser = parser.withFactory(factory);
        assertSame("Should return same instance when factory is same", parser, sameParser);
    }

    @Test(timeout = 4000)
    public void testWithFactoryDifferentInstance() {
        TypeFactory newFactory = TypeFactory.defaultInstance();
        TypeParser newParser = parser.withFactory(newFactory);
        assertNotSame("Should return new instance when factory is different", parser, newParser);
        assertNotNull(newParser);
    }

    @Test(timeout = 4000)
    public void testWithFactoryNull() {
        // withFactory(null) should return new instance with null factory (though not recommended)
        TypeParser newParser = parser.withFactory(null);
        assertNotSame(parser, newParser);
        assertNotNull(newParser);
    }

    @Test(timeout = 4000)
    public void testParseTypeWithTypeBindings() {
        // Test that type bindings are correctly created
        JavaType type = parser.parse("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals(1, type.containedTypeCount());
        assertEquals("java.lang.String", type.containedType(0).toCanonical());
    }

    @Test(timeout = 4000)
    public void testParseDeeplyNestedGenerics() {
        String canonical = "java.util.List<java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>>";
        JavaType type = parser.parse(canonical);
        assertNotNull(type);
        assertEquals(canonical, type.toCanonical());
        assertEquals(List.class, type.getRawClass());
        JavaType mapType = type.containedType(0);
        assertEquals(Map.class, mapType.getRawClass());
        JavaType listType = mapType.containedType(1);
        assertEquals(List.class, listType.getRawClass());
        assertEquals(Integer.class, listType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseWithLeadingAndTrailingWhitespaceInClassName() {
        JavaType type = parser.parse("  java.lang.String  ");
        assertNotNull(type);
        assertEquals("java.lang.String", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespaceAroundComma() {
        JavaType type = parser.parse("java.util.Map< java.lang.String , java.lang.Integer >");
        assertNotNull(type);
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testParseWithWhitespaceAroundBrackets() {
        JavaType type = parser.parse("java.util.List < java.lang.String >");
        assertNotNull(type);
        assertEquals("java.util.List<java.lang.String>", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleNestedLevels() {
        String canonical = "java.util.Map<java.lang.String,java.util.Map<java.lang.Integer,java.util.List<java.lang.Boolean>>>";
        JavaType type = parser.parse(canonical);
        assertNotNull(type);
        assertEquals(canonical, type.toCanonical());
        assertEquals(Map.class, type.getRawClass());
        JavaType innerMap = type.containedType(1);
        assertEquals(Map.class, innerMap.getRawClass());
        JavaType innerList = innerMap.containedType(1);
        assertEquals(List.class, innerList.getRawClass());
        assertEquals(Boolean.class, innerList.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testParseWithTypeVariable() {
        // Type variables are not resolved by TypeParser, but should not crash
        try {
            JavaType type = parser.parse("java.util.List<T>");
            assertNotNull(type);
            assertEquals(List.class, type.getRawClass());
        } catch (IllegalArgumentException e) {
            // Acceptable if type variable not found
            assertTrue(e.getMessage().contains("Can not locate class"));
        }
    }
}