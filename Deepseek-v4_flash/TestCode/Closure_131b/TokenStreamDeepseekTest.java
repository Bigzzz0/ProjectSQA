package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for TokenStream.
 *
 * Branch & Defect Analysis Matrix:
 * - isKeyword: 12 length cases (0,2,3,4,5,6,7,8,9,10,12, other) with nested character checks.
 * - Known defect: isKeyword incorrectly returns true for Java keywords not in JavaScript (e.g., "byte", "goto", "long", "final", "float", "short", "native", "throws", "double", "static", "public", "package", "boolean", "private", "abstract", "volatile", "interface", "protected", "transient", "implements", "synchronized").
 * - isJSIdentifier: boundary conditions (empty, null, first char, subsequent chars, Unicode).
 * - Target defect: isKeyword("byte") returns true instead of false.
 */
public class TokenStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testIsKeyword_Length2_Valid() {
        assertTrue("if should be keyword", TokenStream.isKeyword("if"));
        assertTrue("in should be keyword", TokenStream.isKeyword("in"));
        assertTrue("do should be keyword", TokenStream.isKeyword("do"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length2_Invalid() {
        assertFalse("is is not keyword", TokenStream.isKeyword("is"));
        assertFalse("it is not keyword", TokenStream.isKeyword("it"));
        assertFalse("on is not keyword", TokenStream.isKeyword("on"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length3_Valid() {
        assertTrue("for should be keyword", TokenStream.isKeyword("for"));
        assertTrue("int should be keyword", TokenStream.isKeyword("int"));
        assertTrue("new should be keyword", TokenStream.isKeyword("new"));
        assertTrue("try should be keyword", TokenStream.isKeyword("try"));
        assertTrue("var should be keyword", TokenStream.isKeyword("var"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length3_Invalid() {
        assertFalse("foo is not keyword", TokenStream.isKeyword("foo"));
        assertFalse("bar is not keyword", TokenStream.isKeyword("bar"));
        assertFalse("xyz is not keyword", TokenStream.isKeyword("xyz"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length4_Valid() {
        assertTrue("case should be keyword", TokenStream.isKeyword("case"));
        assertTrue("char should be keyword", TokenStream.isKeyword("char"));
        assertTrue("else should be keyword", TokenStream.isKeyword("else"));
        assertTrue("enum should be keyword", TokenStream.isKeyword("enum"));
        assertTrue("null should be keyword", TokenStream.isKeyword("null"));
        assertTrue("true should be keyword", TokenStream.isKeyword("true"));
        assertTrue("this should be keyword", TokenStream.isKeyword("this"));
        assertTrue("void should be keyword", TokenStream.isKeyword("void"));
        assertTrue("with should be keyword", TokenStream.isKeyword("with"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length4_Invalid() {
        // These are Java keywords, not JavaScript – defect should return false
        assertFalse("byte is not JS keyword", TokenStream.isKeyword("byte"));
        assertFalse("goto is not JS keyword", TokenStream.isKeyword("goto"));
        assertFalse("long is not JS keyword", TokenStream.isKeyword("long"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length5_Valid() {
        assertTrue("class should be keyword", TokenStream.isKeyword("class"));
        assertTrue("break should be keyword", TokenStream.isKeyword("break"));
        assertTrue("while should be keyword", TokenStream.isKeyword("while"));
        assertTrue("false should be keyword", TokenStream.isKeyword("false"));
        assertTrue("const should be keyword", TokenStream.isKeyword("const"));
        assertTrue("super should be keyword", TokenStream.isKeyword("super"));
        assertTrue("throw should be keyword", TokenStream.isKeyword("throw"));
        assertTrue("catch should be keyword", TokenStream.isKeyword("catch"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length5_Invalid() {
        assertFalse("final is not JS keyword", TokenStream.isKeyword("final"));
        assertFalse("float is not JS keyword", TokenStream.isKeyword("float"));
        assertFalse("short is not JS keyword", TokenStream.isKeyword("short"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length6_Valid() {
        assertTrue("delete should be keyword", TokenStream.isKeyword("delete"));
        assertTrue("return should be keyword", TokenStream.isKeyword("return"));
        assertTrue("import should be keyword", TokenStream.isKeyword("import"));
        assertTrue("switch should be keyword", TokenStream.isKeyword("switch"));
        assertTrue("export should be keyword", TokenStream.isKeyword("export"));
        assertTrue("typeof should be keyword", TokenStream.isKeyword("typeof"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length6_Invalid() {
        assertFalse("native is not JS keyword", TokenStream.isKeyword("native"));
        assertFalse("throws is not JS keyword", TokenStream.isKeyword("throws"));
        assertFalse("double is not JS keyword", TokenStream.isKeyword("double"));
        assertFalse("static is not JS keyword", TokenStream.isKeyword("static"));
        assertFalse("public is not JS keyword", TokenStream.isKeyword("public"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length7_Valid() {
        assertTrue("default should be keyword", TokenStream.isKeyword("default"));
        assertTrue("finally should be keyword", TokenStream.isKeyword("finally"));
        assertTrue("extends should be keyword", TokenStream.isKeyword("extends"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length7_Invalid() {
        assertFalse("package is not JS keyword", TokenStream.isKeyword("package"));
        assertFalse("boolean is not JS keyword", TokenStream.isKeyword("boolean"));
        assertFalse("private is not JS keyword", TokenStream.isKeyword("private"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length8_Valid() {
        assertTrue("continue should be keyword", TokenStream.isKeyword("continue"));
        assertTrue("debugger should be keyword", TokenStream.isKeyword("debugger"));
        assertTrue("function should be keyword", TokenStream.isKeyword("function"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length8_Invalid() {
        assertFalse("abstract is not JS keyword", TokenStream.isKeyword("abstract"));
        assertFalse("volatile is not JS keyword", TokenStream.isKeyword("volatile"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length9_Valid() {
        // No valid JS keywords of length 9? Actually "interface" is not JS, "protected" not, "transient" not.
        // So all should be false.
        assertFalse("interface is not JS keyword", TokenStream.isKeyword("interface"));
        assertFalse("protected is not JS keyword", TokenStream.isKeyword("protected"));
        assertFalse("transient is not JS keyword", TokenStream.isKeyword("transient"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length10_Valid() {
        assertTrue("instanceof should be keyword", TokenStream.isKeyword("instanceof"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length10_Invalid() {
        assertFalse("implements is not JS keyword", TokenStream.isKeyword("implements"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_Length12_Invalid() {
        assertFalse("synchronized is not JS keyword", TokenStream.isKeyword("synchronized"));
    }

    @Test(timeout = 4000)
    public void testIsKeyword_OtherLengths() {
        assertFalse("length 1", TokenStream.isKeyword("a"));
        assertFalse("length 11", TokenStream.isKeyword("abcdefghijk"));
        assertFalse("length 13", TokenStream.isKeyword("abcdefghijklm"));
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testIsKeyword_Null() {
        TokenStream.isKeyword(null);
    }

    @Test(timeout = 4000)
    public void testIsKeyword_EmptyString() {
        assertFalse("empty string", TokenStream.isKeyword(""));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testIsJSIdentifier_Valid() {
        assertTrue("simple identifier", TokenStream.isJSIdentifier("foo"));
        assertTrue("underscore start", TokenStream.isJSIdentifier("_foo"));
        assertTrue("dollar start", TokenStream.isJSIdentifier("$foo"));
        assertTrue("contains digits", TokenStream.isJSIdentifier("foo123"));
        assertTrue("single char", TokenStream.isJSIdentifier("a"));
        assertTrue("unicode letter", TokenStream.isJSIdentifier("\u00e9")); // é
        assertTrue("unicode with underscore", TokenStream.isJSIdentifier("_ \u00e9"));
    }

    @Test(timeout = 4000)
    public void testIsJSIdentifier_Invalid() {
        assertFalse("empty string", TokenStream.isJSIdentifier(""));
        assertFalse("starts with digit", TokenStream.isJSIdentifier("1foo"));
        assertFalse("starts with invalid char", TokenStream.isJSIdentifier("@foo"));
        assertFalse("contains invalid char", TokenStream.isJSIdentifier("foo bar"));
        assertFalse("contains hyphen", TokenStream.isJSIdentifier("foo-bar"));
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testIsJSIdentifier_Null() {
        TokenStream.isJSIdentifier(null);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect: isKeyword incorrectly returns true for
     * Java keywords that are not JavaScript keywords.
     * The buggy version returns true for "byte", "goto", "long", "final", etc.
     * This test asserts false for a representative set.
     */
    @Test(timeout = 4000)
    public void testIsKeyword_NonJSKeywordsReturnFalse() {
        // These are all Java keywords that should NOT be JS keywords
        assertFalse("byte", TokenStream.isKeyword("byte"));
        assertFalse("goto", TokenStream.isKeyword("goto"));
        assertFalse("long", TokenStream.isKeyword("long"));
        assertFalse("final", TokenStream.isKeyword("final"));
        assertFalse("float", TokenStream.isKeyword("float"));
        assertFalse("short", TokenStream.isKeyword("short"));
        assertFalse("native", TokenStream.isKeyword("native"));
        assertFalse("throws", TokenStream.isKeyword("throws"));
        assertFalse("double", TokenStream.isKeyword("double"));
        assertFalse("static", TokenStream.isKeyword("static"));
        assertFalse("public", TokenStream.isKeyword("public"));
        assertFalse("package", TokenStream.isKeyword("package"));
        assertFalse("boolean", TokenStream.isKeyword("boolean"));
        assertFalse("private", TokenStream.isKeyword("private"));
        assertFalse("abstract", TokenStream.isKeyword("abstract"));
        assertFalse("volatile", TokenStream.isKeyword("volatile"));
        assertFalse("interface", TokenStream.isKeyword("interface"));
        assertFalse("protected", TokenStream.isKeyword("protected"));
        assertFalse("transient", TokenStream.isKeyword("transient"));
        assertFalse("implements", TokenStream.isKeyword("implements"));
        assertFalse("synchronized", TokenStream.isKeyword("synchronized"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    // Already covered by null tests above.

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    // TokenStream has no instance state; static methods only. No lifecycle tests needed.
}
