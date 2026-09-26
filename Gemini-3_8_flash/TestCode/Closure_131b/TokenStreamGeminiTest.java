package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.TokenStream;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.TokenStream
 * Methods Under Test:
 *   - boolean isKeyword(String name)
 *   - boolean isJSIdentifier(String s)
 *
 * Branch & Coverage Matrix:
 * 1. isKeyword:
 *    - Length 2: 'if' (char(1)=='f', char(0)=='i'), 'in' (char(1)=='n', char(0)=='i'), 'do' (char(1)=='o', char(0)=='d')
 *      Negatives: char(1) match but char(0) mismatch ("af", "an", "to"), char(1) mismatch ("ax")
 *    - Length 3: 'for' ('f'), 'int' ('i'), 'new' ('n'), 'try' ('t'), 'var' ('v')
 *      Negatives: char(0) match but char(1)/char(2) mismatch ("foo", "far", "inn", "ice", "net", "now", "tri", "toy", "vat", "von"), char(0) mismatch ("xyz")
 *    - Length 4: 'byte' ('b'), 'case'/'char' ('c'), 'else'/'enum' ('e'), 'goto' ('g'), 'long' ('l'), 'null' ('n'), 'true'/'this' ('t'), 'void' ('v'), 'with' ('w')
 *      Negatives: "barn", "cake", "cure", "cool", "czar", "cher", "ease", "elme", "exam", "etum", "echo", "good", "line", "none", "tree", "tune", "thus", "toys", "test", "very", "word", "zing"
 *    - Length 5: 'class' (char(2)=='a'), 'break' ('e'), 'while' ('i'), 'false' ('l'), 'const'/'final' ('n'), 'float'/'short' ('o'), 'super' ('p'), 'throw' ('r'), 'catch' ('t')
 *      Negatives: "chart", "bleed", "white", "folio", "clone", "fancy", "sound", "flock", "shoot", "block", "apple", "arrow", "match", "zebra"
 *    - Length 6: 'native' (char(1)=='a'), 'delete'/'return' ('e'), 'throws' ('h'), 'import' ('m'), 'double' ('o'), 'static' ('t'), 'public' ('u'), 'switch' ('w'), 'export' ('x'), 'typeof' ('y')
 *      Negatives: "banana", "device", "recipe", "letter", "phrase", "empire", "socket", "status", "summer", "awards", "expert", "system", "orange"
 *    - Length 7: 'package' (char(1)=='a'), 'default' ('e'), 'finally' ('i'), 'boolean' ('o'), 'private' ('r'), 'extends' ('x')
 *      Negatives: "palaces", "decimal", "picture", "brother", "profile", "example", "monkeys"
 *    - Length 8: 'abstract' ('a'), 'continue' ('c'), 'debugger' ('d'), 'function' ('f'), 'volatile' ('v')
 *      Negatives: "alphabet", "calendar", "diameter", "fraction", "velocity", "elephant"
 *    - Length 9: 'interface' ('i'), 'protected' ('p'), 'transient' ('t')
 *      Negatives: "important", "principal", "telephone", "universal"
 *    - Length 10: 'implements' (char(1)=='m'), 'instanceof' ('n')
 *      Negatives: "employment", "university", "california"
 *    - Length 12: 'synchronized'
 *      Negatives: "unauthorized"
 *    - Other Lengths: 0 (""), 1 ("a"), 11 ("abcdefghijk"), 13 ("abcdefghijklm"), >13 ("supercalifragilistic")
 * 2. isJSIdentifier:
 *    - Length 0: returns false
 *    - Length >= 1:
 *      * Start character valid: letters, '$', '_'
 *      * Start character invalid: digits ('0'-'9'), punctuation, operators, whitespace
 *      * Subsequent characters valid: letters, digits, '$', '_'
 *      * Subsequent characters invalid: operators, spaces, punctuation
 *
 * Known Defect (Defects4J - ConvertToDottedPropertiesTest / QuotedProps & DoNotConvert):
 *    - In the defective version, TokenStream.isJSIdentifier returns true for keywords like "default", "delete",
 *      "class", "for", "while", "null", "true", "false", causing property accesses like a['default'] to be
 *      illegally transformed to a.default in ES3 contexts.
 *    - Ground Truth: A reserved JavaScript keyword is NOT a valid JavaScript identifier.
 * ---------------------------------------------------------------------------------------------------------
 */
public class TokenStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Keywords by Length)
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeywordsLength2() {
        assertTrue("Expected 'if' to be a keyword", TokenStream.isKeyword("if"));
        assertTrue("Expected 'in' to be a keyword", TokenStream.isKeyword("in"));
        assertTrue("Expected 'do' to be a keyword", TokenStream.isKeyword("do"));

        assertFalse("Expected 'af' not to be a keyword", TokenStream.isKeyword("af"));
        assertFalse("Expected 'an' not to be a keyword", TokenStream.isKeyword("an"));
        assertFalse("Expected 'to' not to be a keyword", TokenStream.isKeyword("to"));
        assertFalse("Expected 'ax' not to be a keyword", TokenStream.isKeyword("ax"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength3() {
        assertTrue("Expected 'for' to be a keyword", TokenStream.isKeyword("for"));
        assertTrue("Expected 'int' to be a keyword", TokenStream.isKeyword("int"));
        assertTrue("Expected 'new' to be a keyword", TokenStream.isKeyword("new"));
        assertTrue("Expected 'try' to be a keyword", TokenStream.isKeyword("try"));
        assertTrue("Expected 'var' to be a keyword", TokenStream.isKeyword("var"));

        assertFalse("Expected 'foo' not to be a keyword", TokenStream.isKeyword("foo"));
        assertFalse("Expected 'far' not to be a keyword", TokenStream.isKeyword("far"));
        assertFalse("Expected 'inn' not to be a keyword", TokenStream.isKeyword("inn"));
        assertFalse("Expected 'ice' not to be a keyword", TokenStream.isKeyword("ice"));
        assertFalse("Expected 'net' not to be a keyword", TokenStream.isKeyword("net"));
        assertFalse("Expected 'now' not to be a keyword", TokenStream.isKeyword("now"));
        assertFalse("Expected 'tri' not to be a keyword", TokenStream.isKeyword("tri"));
        assertFalse("Expected 'toy' not to be a keyword", TokenStream.isKeyword("toy"));
        assertFalse("Expected 'vat' not to be a keyword", TokenStream.isKeyword("vat"));
        assertFalse("Expected 'von' not to be a keyword", TokenStream.isKeyword("von"));
        assertFalse("Expected 'xyz' not to be a keyword", TokenStream.isKeyword("xyz"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength4() {
        assertTrue("Expected 'byte' to be a keyword", TokenStream.isKeyword("byte"));
        assertTrue("Expected 'case' to be a keyword", TokenStream.isKeyword("case"));
        assertTrue("Expected 'char' to be a keyword", TokenStream.isKeyword("char"));
        assertTrue("Expected 'else' to be a keyword", TokenStream.isKeyword("else"));
        assertTrue("Expected 'enum' to be a keyword", TokenStream.isKeyword("enum"));
        assertTrue("Expected 'goto' to be a keyword", TokenStream.isKeyword("goto"));
        assertTrue("Expected 'long' to be a keyword", TokenStream.isKeyword("long"));
        assertTrue("Expected 'null' to be a keyword", TokenStream.isKeyword("null"));
        assertTrue("Expected 'true' to be a keyword", TokenStream.isKeyword("true"));
        assertTrue("Expected 'this' to be a keyword", TokenStream.isKeyword("this"));
        assertTrue("Expected 'void' to be a keyword", TokenStream.isKeyword("void"));
        assertTrue("Expected 'with' to be a keyword", TokenStream.isKeyword("with"));

        assertFalse("Expected 'barn' not to be a keyword", TokenStream.isKeyword("barn"));
        assertFalse("Expected 'cake' not to be a keyword", TokenStream.isKeyword("cake"));
        assertFalse("Expected 'cure' not to be a keyword", TokenStream.isKeyword("cure"));
        assertFalse("Expected 'czar' not to be a keyword", TokenStream.isKeyword("czar"));
        assertFalse("Expected 'cher' not to be a keyword", TokenStream.isKeyword("cher"));
        assertFalse("Expected 'cool' not to be a keyword", TokenStream.isKeyword("cool"));
        assertFalse("Expected 'ease' not to be a keyword", TokenStream.isKeyword("ease"));
        assertFalse("Expected 'elme' not to be a keyword", TokenStream.isKeyword("elme"));
        assertFalse("Expected 'exam' not to be a keyword", TokenStream.isKeyword("exam"));
        assertFalse("Expected 'etum' not to be a keyword", TokenStream.isKeyword("etum"));
        assertFalse("Expected 'echo' not to be a keyword", TokenStream.isKeyword("echo"));
        assertFalse("Expected 'good' not to be a keyword", TokenStream.isKeyword("good"));
        assertFalse("Expected 'line' not to be a keyword", TokenStream.isKeyword("line"));
        assertFalse("Expected 'none' not to be a keyword", TokenStream.isKeyword("none"));
        assertFalse("Expected 'tree' not to be a keyword", TokenStream.isKeyword("tree"));
        assertFalse("Expected 'tune' not to be a keyword", TokenStream.isKeyword("tune"));
        assertFalse("Expected 'thus' not to be a keyword", TokenStream.isKeyword("thus"));
        assertFalse("Expected 'toys' not to be a keyword", TokenStream.isKeyword("toys"));
        assertFalse("Expected 'test' not to be a keyword", TokenStream.isKeyword("test"));
        assertFalse("Expected 'very' not to be a keyword", TokenStream.isKeyword("very"));
        assertFalse("Expected 'word' not to be a keyword", TokenStream.isKeyword("word"));
        assertFalse("Expected 'zing' not to be a keyword", TokenStream.isKeyword("zing"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength5() {
        assertTrue("Expected 'class' to be a keyword", TokenStream.isKeyword("class"));
        assertTrue("Expected 'break' to be a keyword", TokenStream.isKeyword("break"));
        assertTrue("Expected 'while' to be a keyword", TokenStream.isKeyword("while"));
        assertTrue("Expected 'false' to be a keyword", TokenStream.isKeyword("false"));
        assertTrue("Expected 'const' to be a keyword", TokenStream.isKeyword("const"));
        assertTrue("Expected 'final' to be a keyword", TokenStream.isKeyword("final"));
        assertTrue("Expected 'float' to be a keyword", TokenStream.isKeyword("float"));
        assertTrue("Expected 'short' to be a keyword", TokenStream.isKeyword("short"));
        assertTrue("Expected 'super' to be a keyword", TokenStream.isKeyword("super"));
        assertTrue("Expected 'throw' to be a keyword", TokenStream.isKeyword("throw"));
        assertTrue("Expected 'catch' to be a keyword", TokenStream.isKeyword("catch"));

        assertFalse("Expected 'chart' not to be a keyword", TokenStream.isKeyword("chart"));
        assertFalse("Expected 'bleed' not to be a keyword", TokenStream.isKeyword("bleed"));
        assertFalse("Expected 'white' not to be a keyword", TokenStream.isKeyword("white"));
        assertFalse("Expected 'folio' not to be a keyword", TokenStream.isKeyword("folio"));
        assertFalse("Expected 'clone' not to be a keyword", TokenStream.isKeyword("clone"));
        assertFalse("Expected 'fancy' not to be a keyword", TokenStream.isKeyword("fancy"));
        assertFalse("Expected 'sound' not to be a keyword", TokenStream.isKeyword("sound"));
        assertFalse("Expected 'flock' not to be a keyword", TokenStream.isKeyword("flock"));
        assertFalse("Expected 'shoot' not to be a keyword", TokenStream.isKeyword("shoot"));
        assertFalse("Expected 'block' not to be a keyword", TokenStream.isKeyword("block"));
        assertFalse("Expected 'apple' not to be a keyword", TokenStream.isKeyword("apple"));
        assertFalse("Expected 'arrow' not to be a keyword", TokenStream.isKeyword("arrow"));
        assertFalse("Expected 'match' not to be a keyword", TokenStream.isKeyword("match"));
        assertFalse("Expected 'zebra' not to be a keyword", TokenStream.isKeyword("zebra"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength6() {
        assertTrue("Expected 'native' to be a keyword", TokenStream.isKeyword("native"));
        assertTrue("Expected 'delete' to be a keyword", TokenStream.isKeyword("delete"));
        assertTrue("Expected 'return' to be a keyword", TokenStream.isKeyword("return"));
        assertTrue("Expected 'throws' to be a keyword", TokenStream.isKeyword("throws"));
        assertTrue("Expected 'import' to be a keyword", TokenStream.isKeyword("import"));
        assertTrue("Expected 'double' to be a keyword", TokenStream.isKeyword("double"));
        assertTrue("Expected 'static' to be a keyword", TokenStream.isKeyword("static"));
        assertTrue("Expected 'public' to be a keyword", TokenStream.isKeyword("public"));
        assertTrue("Expected 'switch' to be a keyword", TokenStream.isKeyword("switch"));
        assertTrue("Expected 'export' to be a keyword", TokenStream.isKeyword("export"));
        assertTrue("Expected 'typeof' to be a keyword", TokenStream.isKeyword("typeof"));

        assertFalse("Expected 'banana' not to be a keyword", TokenStream.isKeyword("banana"));
        assertFalse("Expected 'device' not to be a keyword", TokenStream.isKeyword("device"));
        assertFalse("Expected 'recipe' not to be a keyword", TokenStream.isKeyword("recipe"));
        assertFalse("Expected 'letter' not to be a keyword", TokenStream.isKeyword("letter"));
        assertFalse("Expected 'phrase' not to be a keyword", TokenStream.isKeyword("phrase"));
        assertFalse("Expected 'empire' not to be a keyword", TokenStream.isKeyword("empire"));
        assertFalse("Expected 'socket' not to be a keyword", TokenStream.isKeyword("socket"));
        assertFalse("Expected 'status' not to be a keyword", TokenStream.isKeyword("status"));
        assertFalse("Expected 'summer' not to be a keyword", TokenStream.isKeyword("summer"));
        assertFalse("Expected 'awards' not to be a keyword", TokenStream.isKeyword("awards"));
        assertFalse("Expected 'expert' not to be a keyword", TokenStream.isKeyword("expert"));
        assertFalse("Expected 'system' not to be a keyword", TokenStream.isKeyword("system"));
        assertFalse("Expected 'orange' not to be a keyword", TokenStream.isKeyword("orange"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength7() {
        assertTrue("Expected 'package' to be a keyword", TokenStream.isKeyword("package"));
        assertTrue("Expected 'default' to be a keyword", TokenStream.isKeyword("default"));
        assertTrue("Expected 'finally' to be a keyword", TokenStream.isKeyword("finally"));
        assertTrue("Expected 'boolean' to be a keyword", TokenStream.isKeyword("boolean"));
        assertTrue("Expected 'private' to be a keyword", TokenStream.isKeyword("private"));
        assertTrue("Expected 'extends' to be a keyword", TokenStream.isKeyword("extends"));

        assertFalse("Expected 'palaces' not to be a keyword", TokenStream.isKeyword("palaces"));
        assertFalse("Expected 'decimal' not to be a keyword", TokenStream.isKeyword("decimal"));
        assertFalse("Expected 'picture' not to be a keyword", TokenStream.isKeyword("picture"));
        assertFalse("Expected 'brother' not to be a keyword", TokenStream.isKeyword("brother"));
        assertFalse("Expected 'profile' not to be a keyword", TokenStream.isKeyword("profile"));
        assertFalse("Expected 'example' not to be a keyword", TokenStream.isKeyword("example"));
        assertFalse("Expected 'monkeys' not to be a keyword", TokenStream.isKeyword("monkeys"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength8() {
        assertTrue("Expected 'abstract' to be a keyword", TokenStream.isKeyword("abstract"));
        assertTrue("Expected 'continue' to be a keyword", TokenStream.isKeyword("continue"));
        assertTrue("Expected 'debugger' to be a keyword", TokenStream.isKeyword("debugger"));
        assertTrue("Expected 'function' to be a keyword", TokenStream.isKeyword("function"));
        assertTrue("Expected 'volatile' to be a keyword", TokenStream.isKeyword("volatile"));

        assertFalse("Expected 'alphabet' not to be a keyword", TokenStream.isKeyword("alphabet"));
        assertFalse("Expected 'calendar' not to be a keyword", TokenStream.isKeyword("calendar"));
        assertFalse("Expected 'diameter' not to be a keyword", TokenStream.isKeyword("diameter"));
        assertFalse("Expected 'fraction' not to be a keyword", TokenStream.isKeyword("fraction"));
        assertFalse("Expected 'velocity' not to be a keyword", TokenStream.isKeyword("velocity"));
        assertFalse("Expected 'elephant' not to be a keyword", TokenStream.isKeyword("elephant"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength9() {
        assertTrue("Expected 'interface' to be a keyword", TokenStream.isKeyword("interface"));
        assertTrue("Expected 'protected' to be a keyword", TokenStream.isKeyword("protected"));
        assertTrue("Expected 'transient' to be a keyword", TokenStream.isKeyword("transient"));

        assertFalse("Expected 'important' not to be a keyword", TokenStream.isKeyword("important"));
        assertFalse("Expected 'principal' not to be a keyword", TokenStream.isKeyword("principal"));
        assertFalse("Expected 'telephone' not to be a keyword", TokenStream.isKeyword("telephone"));
        assertFalse("Expected 'universal' not to be a keyword", TokenStream.isKeyword("universal"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength10() {
        assertTrue("Expected 'implements' to be a keyword", TokenStream.isKeyword("implements"));
        assertTrue("Expected 'instanceof' to be a keyword", TokenStream.isKeyword("instanceof"));

        assertFalse("Expected 'employment' not to be a keyword", TokenStream.isKeyword("employment"));
        assertFalse("Expected 'university' not to be a keyword", TokenStream.isKeyword("university"));
        assertFalse("Expected 'california' not to be a keyword", TokenStream.isKeyword("california"));
    }

    @Test(timeout = 4000)
    public void testKeywordsLength12() {
        assertTrue("Expected 'synchronized' to be a keyword", TokenStream.isKeyword("synchronized"));
        assertFalse("Expected 'unauthorized' not to be a keyword", TokenStream.isKeyword("unauthorized"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeywordsLengthBoundaries() {
        assertFalse("Empty string cannot be a keyword", TokenStream.isKeyword(""));
        assertFalse("Length 1 'a' is not a keyword", TokenStream.isKeyword("a"));
        assertFalse("Length 1 'x' is not a keyword", TokenStream.isKeyword("x"));
        assertFalse("Length 11 is not handled as keyword", TokenStream.isKeyword("abcdefghijk"));
        assertFalse("Length 13 is not handled as keyword", TokenStream.isKeyword("abcdefghijklm"));
        assertFalse("Length > 13 is not handled as keyword", TokenStream.isKeyword("supercalifragilistic"));
    }

    @Test(timeout = 4000)
    public void testJSIdentifierValidScenarios() {
        assertTrue("Single ASCII letter must be valid", TokenStream.isJSIdentifier("a"));
        assertTrue("Single uppercase letter must be valid", TokenStream.isJSIdentifier("Z"));
        assertTrue("Dollar sign must be valid start", TokenStream.isJSIdentifier("$"));
        assertTrue("Underscore must be valid start", TokenStream.isJSIdentifier("_"));
        assertTrue("Multi-char identifier with digits must be valid", TokenStream.isJSIdentifier("myVar123"));
        assertTrue("Identifier starting with $ and containing _", TokenStream.isJSIdentifier("$foo_bar"));
        assertTrue("Identifier starting with _ and containing $", TokenStream.isJSIdentifier("_foo$bar"));
    }

    @Test(timeout = 4000)
    public void testJSIdentifierInvalidScenarios() {
        assertFalse("Empty string is not a valid JS identifier", TokenStream.isJSIdentifier(""));
        assertFalse("Digit start '0' is not a valid JS identifier", TokenStream.isJSIdentifier("0"));
        assertFalse("Digit start '1abc' is not a valid JS identifier", TokenStream.isJSIdentifier("1abc"));
        assertFalse("Hyphen is not a valid identifier character", TokenStream.isJSIdentifier("foo-bar"));
        assertFalse("Dot is not a valid identifier character", TokenStream.isJSIdentifier("foo.bar"));
        assertFalse("Whitespace is not a valid identifier character", TokenStream.isJSIdentifier("foo bar"));
        assertFalse("Leading whitespace is not allowed", TokenStream.isJSIdentifier(" foo"));
        assertFalse("Trailing whitespace is not allowed", TokenStream.isJSIdentifier("foo "));
        assertFalse("Special symbols like '@' are not allowed", TokenStream.isJSIdentifier("@bar"));
        assertFalse("Special symbols like '#' are not allowed", TokenStream.isJSIdentifier("#bar"));
        assertFalse("Embedded invalid char", TokenStream.isJSIdentifier("abc+def"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Closure Defect: Keywords as Identifiers)
    // =========================================================================

    /**
     * Targets Defects4J regression in ConvertToDottedPropertiesTest::testQuotedProps
     * and ConvertToDottedPropertiesTest::testDoNotConvert.
     *
     * In JavaScript, reserved words (keywords) are not valid Identifier names in AST contexts
     * where bare identifiers are required without quotes. TokenStream.isJSIdentifier(s)
     * must return false for all reserved keywords.
     */
    @Test(timeout = 4000)
    public void testDefectReservedKeywordsMustNotBeJSIdentifiers() {
        assertFalse("Reserved keyword 'default' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("default"));
        assertFalse("Reserved keyword 'delete' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("delete"));
        assertFalse("Reserved keyword 'class' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("class"));
        assertFalse("Reserved keyword 'while' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("while"));
        assertFalse("Reserved keyword 'for' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("for"));
        assertFalse("Reserved keyword 'null' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("null"));
        assertFalse("Reserved keyword 'true' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("true"));
        assertFalse("Reserved keyword 'false' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("false"));
        assertFalse("Reserved keyword 'function' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("function"));
        assertFalse("Reserved keyword 'return' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("return"));
    }

    @Test(timeout = 4000)
    public void testDefectAdditionalKeywordsMustNotBeJSIdentifiers() {
        assertFalse("Reserved keyword 'case' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("case"));
        assertFalse("Reserved keyword 'catch' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("catch"));
        assertFalse("Reserved keyword 'var' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("var"));
        assertFalse("Reserved keyword 'if' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("if"));
        assertFalse("Reserved keyword 'in' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("in"));
        assertFalse("Reserved keyword 'do' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("do"));
        assertFalse("Reserved keyword 'this' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("this"));
        assertFalse("Reserved keyword 'typeof' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("typeof"));
        assertFalse("Reserved keyword 'void' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("void"));
        assertFalse("Reserved keyword 'with' must not be a valid JS identifier",
                TokenStream.isJSIdentifier("with"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testIsKeywordNullArgumentThrowsException() {
        TokenStream.isKeyword(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testIsJSIdentifierNullArgumentThrowsException() {
        TokenStream.isJSIdentifier(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenStreamInstantiation() {
        TokenStream stream = new TokenStream();
        assertNotNull("TokenStream instance should be successfully created", stream);
    }
}
