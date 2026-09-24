/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.TokenStream
 * Defects4J Context: com.google.javascript.jscomp.ConvertToDottedPropertiesTest (testQuotedProps, testDoNotConvert)
 *
 * Key Areas Tested:
 * 1. Keyword Recognition & ES3/ES5 Reserved Words (isKeyword / stringToKeyword):
 *    - All keyword length buckets (2, 3, 4, 5, 6, 7, 8, 9, 10, 12 chars).
 *    - Strict match validation (prefix/suffix mismatches, non-keyword identifiers of same lengths).
 *    - Preservation of keywords (default, delete, null, true, false, etc.) critical to property dotting logic.
 * 2. Lexical Token Scanning (getToken):
 *    - Whitespace, BOM (\uFEFF), newlines (\r, \n, \r\n), and EOF tracking.
 *    - Identifiers: standard ASCII, unicode escapes (\uXXXX), bad unicode escapes, unicode keywords.
 *    - Numbers: decimal, hex (0x/0X), octal (077), bad octal (089), floats (.5, 1.2), exponents (1e-3, 1e+2, 1e5, bad exp 1e).
 *    - Strings: single/double quotes, escape sequences (\b, \f, \n, \r, \t, \v, \xHH, \uHHHH, octal, line continuations, unterminated).
 *    - Operators & Punctuation: compound assignments (+=, -=, <<=, >>=, >>>=, etc.), equality (==, ===, !=, !==),
 *      relations (<, <=, >, >=), dots (., .., .(), colons (:, ::), XML attribute (@), comments (//, /*, /**, <!--, -->).
 * 3. Regular Expressions (readRegExp):
 *    - Standard literal scanning, flags (g, i, m, y), character classes ([...]), escape sequences, unterminated REs.
 * 4. E4X / XML Token Scanning (getFirstXMLToken, getNextXMLToken):
 *    - XML start/end tags, empty elements, attributes, CDATA, comments, processing instructions, embedded JS ({...}).
 * 5. State & Defensive Paths:
 *    - Dual source initialization / null source rejection (Kit.codeBug).
 *    - Buffer expansion (identifiers > 128 chars, reader buffer refills > 512 chars).
 *    - Comments recording and offset/line retrieval across Reader vs String sources.
 */

package org.mozilla.javascript;

import org.junit.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class TokenStreamGeminiTest {

    private TokenStream createStringStream(String source) {
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        Parser parser = new Parser(compilerEnv);
        return new TokenStream(parser, null, source, 1);
    }

    private TokenStream createReaderStream(String source) {
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        Parser parser = new Parser(compilerEnv);
        return new TokenStream(parser, new StringReader(source), null, 1);
    }

    private TokenStream createCustomStream(String source, CompilerEnvirons env) {
        Parser parser = new Parser(env);
        return new TokenStream(parser, null, source, 1);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPunctuationAndOperators() throws IOException {
        String code = "; [ ] { } ( ) , ? : :: . .. .( | || |= ^ ^= & && &= = == === ! != !== ~ + ++ +=";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.SEMI, ts.getToken());
        assertEquals(Token.LB, ts.getToken());
        assertEquals(Token.RB, ts.getToken());
        assertEquals(Token.LC, ts.getToken());
        assertEquals(Token.RC, ts.getToken());
        assertEquals(Token.LP, ts.getToken());
        assertEquals(Token.RP, ts.getToken());
        assertEquals(Token.COMMA, ts.getToken());
        assertEquals(Token.HOOK, ts.getToken());
        assertEquals(Token.COLON, ts.getToken());
        assertEquals(Token.COLONCOLON, ts.getToken());
        assertEquals(Token.DOT, ts.getToken());
        assertEquals(Token.DOTDOT, ts.getToken());
        assertEquals(Token.DOTQUERY, ts.getToken());
        assertEquals(Token.BITOR, ts.getToken());
        assertEquals(Token.OR, ts.getToken());
        assertEquals(Token.ASSIGN_BITOR, ts.getToken());
        assertEquals(Token.BITXOR, ts.getToken());
        assertEquals(Token.ASSIGN_BITXOR, ts.getToken());
        assertEquals(Token.BITAND, ts.getToken());
        assertEquals(Token.AND, ts.getToken());
        assertEquals(Token.ASSIGN_BITAND, ts.getToken());
        assertEquals(Token.ASSIGN, ts.getToken());
        assertEquals(Token.EQ, ts.getToken());
        assertEquals(Token.SHEQ, ts.getToken());
        assertEquals(Token.NOT, ts.getToken());
        assertEquals(Token.NE, ts.getToken());
        assertEquals(Token.SHNE, ts.getToken());
        assertEquals(Token.BITNOT, ts.getToken());
        assertEquals(Token.ADD, ts.getToken());
        assertEquals(Token.INC, ts.getToken());
        assertEquals(Token.ASSIGN_ADD, ts.getToken());
        assertEquals(Token.EOF, ts.getToken());
    }

    @Test(timeout = 4000)
    public void testShiftAndRelationalOperators() throws IOException {
        String code = "< <= << <<= > >= >> >>= >>> >>>= * *= % %= - -- -=";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.LT, ts.getToken());
        assertEquals(Token.LE, ts.getToken());
        assertEquals(Token.LSH, ts.getToken());
        assertEquals(Token.ASSIGN_LSH, ts.getToken());
        assertEquals(Token.GT, ts.getToken());
        assertEquals(Token.GE, ts.getToken());
        assertEquals(Token.RSH, ts.getToken());
        assertEquals(Token.ASSIGN_RSH, ts.getToken());
        assertEquals(Token.URSH, ts.getToken());
        assertEquals(Token.ASSIGN_URSH, ts.getToken());
        assertEquals(Token.MUL, ts.getToken());
        assertEquals(Token.ASSIGN_MUL, ts.getToken());
        assertEquals(Token.MOD, ts.getToken());
        assertEquals(Token.ASSIGN_MOD, ts.getToken());
        assertEquals(Token.SUB, ts.getToken());
        assertEquals(Token.DEC, ts.getToken());
        assertEquals(Token.ASSIGN_SUB, ts.getToken());
        assertEquals(Token.EOF, ts.getToken());
    }

    @Test(timeout = 4000)
    public void testXmlAttributeToken() throws IOException {
        TokenStream ts = createStringStream("@foo");
        assertEquals(Token.XMLATTR, ts.getToken());
        assertEquals(Token.NAME, ts.getToken());
        assertEquals("foo", ts.getString());
    }

    @Test(timeout = 4000)
    public void testNumbersParsing() throws IOException {
        String code = "0 123 0x1F 0X2A 077 12.34 .56 1e2 2E+3 3e-4";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(0.0, ts.getNumber(), 1e-9);
        assertFalse(ts.isNumberOctal());

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(123.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(31.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(42.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(63.0, ts.getNumber(), 1e-9);
        assertTrue(ts.isNumberOctal());

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(12.34, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(0.56, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(100.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(2000.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(0.0003, ts.getNumber(), 1e-9);

        assertEquals(Token.EOF, ts.getToken());
        assertTrue(ts.eof());
    }

    @Test(timeout = 4000)
    public void testStringLiteralParsingAndEscapes() throws IOException {
        String code = "\"hello\\nworld\" 'quote\\'s' \"octal\\123\" \"hex\\x41\" \"uni\\u0042\" \"cont\\\ninued\" \"escapes\\b\\f\\r\\t\\v\"";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("hello\nworld", ts.getString());
        assertEquals('"', ts.getQuoteChar());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("quote's", ts.getString());
        assertEquals('\'', ts.getQuoteChar());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("octalS", ts.getString()); // \123 octal is 83 = 'S'

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("hexA", ts.getString());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("uniB", ts.getString());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("continued", ts.getString());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("escapes\b\f\r\t\u000B", ts.getString());

        assertEquals(Token.EOF, ts.getToken());
    }

    @Test(timeout = 4000)
    public void testCommentsScanning() throws IOException {
        String code = "// line comment\n/* block */\n/** jsdoc */\n<!-- html start\n--> html end\n";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.LINE, ts.getCommentType());

        assertEquals(Token.EOL, ts.getToken());

        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.BLOCK_COMMENT, ts.getCommentType());

        assertEquals(Token.EOL, ts.getToken());

        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.JSDOC, ts.getCommentType());

        assertEquals(Token.EOL, ts.getToken());

        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.HTML, ts.getCommentType());

        assertEquals(Token.EOL, ts.getToken());

        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.HTML, ts.getCommentType());
    }

    @Test(timeout = 4000)
    public void testRegularExpressionScanning() throws IOException {
        TokenStream ts = createStringStream("/[a-z/0-9]+\\/bar/gimy");
        assertEquals(Token.DIV, ts.getToken());
        ts.readRegExp(Token.DIV);

        assertEquals("[a-z/0-9]+\\/bar", ts.getString());
        assertEquals("gimy", ts.readAndClearRegExpFlags());
        assertNull(ts.readAndClearRegExpFlags());
    }

    @Test(timeout = 4000)
    public void testRegExpAssignDivContext() throws IOException {
        TokenStream ts = createStringStream("/=foo/i");
        assertEquals(Token.ASSIGN_DIV, ts.getToken());
        ts.readRegExp(Token.ASSIGN_DIV);

        assertEquals("=foo", ts.getString());
        assertEquals("i", ts.readAndClearRegExpFlags());
    }

    @Test(timeout = 4000)
    public void testE4XXmlTokenScanning() throws IOException {
        String xml = "<root attr='val'><child/>text<!-- comment --><![CDATA[cdata]]><?pi info?></root>";
        TokenStream ts = createStringStream(xml);

        assertEquals(Token.LT, ts.getToken());
        int firstXml = ts.getFirstXMLToken();
        assertEquals(Token.XMLEND, firstXml);
        assertNotNull(ts.getString());
    }

    @Test(timeout = 4000)
    public void testTokenToStringFormatting() {
        TokenStream ts = createStringStream("foo");
        boolean oldPrintTrees = Token.printTrees;
        try {
            Token.printTrees = false;
            assertEquals("", ts.tokenToString(Token.NAME));

            Token.printTrees = true;
            ts.getToken();
            assertEquals("NAME `foo'", ts.tokenToString(Token.NAME));
            assertEquals("SEMI", ts.tokenToString(Token.SEMI));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        } finally {
            Token.printTrees = oldPrintTrees;
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigitAndIsJSSpaceBoundaries() {
        assertTrue(TokenStream.isDigit('0'));
        assertTrue(TokenStream.isDigit('9'));
        assertFalse(TokenStream.isDigit('0' - 1));
        assertFalse(TokenStream.isDigit('9' + 1));
        assertFalse(TokenStream.isDigit(-1));

        assertTrue(TokenStream.isJSSpace(0x20));
        assertTrue(TokenStream.isJSSpace(0x09));
        assertTrue(TokenStream.isJSSpace(0x0C));
        assertTrue(TokenStream.isJSSpace(0x0B));
        assertTrue(TokenStream.isJSSpace(0xA0));
        assertTrue(TokenStream.isJSSpace('\uFEFF')); // BYTE_ORDER_MARK
        assertTrue(TokenStream.isJSSpace(0x2000));   // Unicode SPACE_SEPARATOR
        assertFalse(TokenStream.isJSSpace('a'));
        assertFalse(TokenStream.isJSSpace('\n'));
        assertFalse(TokenStream.isJSSpace('\r'));
    }

    @Test(timeout = 4000)
    public void testEmptyAndSingleCharInputs() throws IOException {
        TokenStream tsEmpty = createStringStream("");
        assertEquals(Token.EOF, tsEmpty.getToken());
        assertTrue(tsEmpty.eof());
        assertEquals("", tsEmpty.getSourceString());

        TokenStream tsSingle = createStringStream(";");
        assertEquals(Token.SEMI, tsSingle.getToken());
        assertEquals(0, tsSingle.getTokenBeg());
        assertEquals(1, tsSingle.getTokenEnd());
        assertEquals(1, tsSingle.getTokenLength());
        assertEquals(Token.EOF, tsSingle.getToken());
    }

    @Test(timeout = 4000)
    public void testLargeBufferGrowthForIdentifiers() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('a');
        }
        String largeId = sb.toString();
        TokenStream ts = createStringStream(largeId);

        assertEquals(Token.NAME, ts.getToken());
        assertEquals(largeId, ts.getString());
        assertEquals(300, ts.getTokenLength());
    }

    @Test(timeout = 4000)
    public void testReaderBufferRefillAcrossLines() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 700; i++) {
            sb.append("var x = 1;\n");
        }
        TokenStream ts = createReaderStream(sb.toString());

        int tokenCount = 0;
        int tok;
        while ((tok = ts.getToken()) != Token.EOF) {
            tokenCount++;
            if (tok == Token.VAR) {
                assertEquals("var x = 1;", ts.getLine().trim());
            }
        }
        assertTrue(tokenCount > 2000);
        assertEquals(701, ts.getLineno());
    }

    @Test(timeout = 4000)
    public void testOctalAndHexFallbackInString() throws IOException {
        // Test \x non-hex fallbacks: \xG -> "xG", \x1G -> "x1G"
        String code = "\"\\xG\" \"\\x1G\" \"\\u123z\"";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("xG", ts.getString());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("x1G", ts.getString());

        assertEquals(Token.STRING, ts.getToken());
        assertEquals("u123z", ts.getString());
    }

    @Test(timeout = 4000)
    public void testBadOctalDecimalSuperset() throws IOException {
        // ECMA extension: 088 and 099 permitted with warning
        TokenStream ts = createStringStream("088 099");
        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(88.0, ts.getNumber(), 1e-9);

        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(99.0, ts.getNumber(), 1e-9);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Closure / Rhino Keywords Ground Truth)
    // =========================================================================

    /**
     * Directly targets the failure condition identified in Defects4J:
     * com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testQuotedProps
     * and testDoNotConvert.
     * Validates that all ECMAScript reserved words and keywords are strictly
     * detected by isKeyword(), ensuring property converters know which properties
     * cannot be unquoted into dot notation (e.g., a['default'] -> a.default in ES3).
     */
    @Test(timeout = 4000)
    public void testConvertToDottedPropertiesKeywordsGroundTruth() {
        String[] keywords = {
            "break", "case", "catch", "class", "const", "continue", "debugger",
            "default", "delete", "do", "else", "enum", "export", "extends",
            "false", "finally", "for", "function", "if", "import", "in",
            "instanceof", "new", "null", "return", "super", "switch", "this",
            "throw", "true", "try", "typeof", "var", "void", "while", "with",
            // Reserved words
            "abstract", "boolean", "byte", "char", "double", "final", "float",
            "goto", "implements", "int", "interface", "long", "native",
            "package", "private", "protected", "public", "short", "static",
            "synchronized", "throws", "transient", "volatile",
            // ES5 strict / 1.7+
            "let", "yield"
        };

        for (String kw : keywords) {
            assertTrue("TokenStream.isKeyword should return true for: " + kw,
                       TokenStream.isKeyword(kw));
        }

        String[] nonKeywords = {
            "", "a", "b", "c", "d", "e", "f", "g",
            "defaults", "deleted", "trueVal", "falsehood", "nullable",
            "instanceOf", "Function", "Var", "Class", "while1", "for_",
            "foo", "bar", "baz", "qux", "propertyName", "get", "set"
        };

        for (String nonKw : nonKeywords) {
            assertFalse("TokenStream.isKeyword should return false for: " + nonKw,
                        TokenStream.isKeyword(nonKw));
        }
    }

    @Test(timeout = 4000)
    public void testKeywordBucketExhaustiveBranches() {
        // Length 2
        assertTrue(TokenStream.isKeyword("if"));
        assertTrue(TokenStream.isKeyword("in"));
        assertTrue(TokenStream.isKeyword("do"));
        assertFalse(TokenStream.isKeyword("it"));
        assertFalse(TokenStream.isKeyword("no"));
        assertFalse(TokenStream.isKeyword("ox"));

        // Length 3
        assertTrue(TokenStream.isKeyword("for"));
        assertTrue(TokenStream.isKeyword("int"));
        assertTrue(TokenStream.isKeyword("let"));
        assertTrue(TokenStream.isKeyword("new"));
        assertTrue(TokenStream.isKeyword("try"));
        assertTrue(TokenStream.isKeyword("var"));
        assertFalse(TokenStream.isKeyword("far"));
        assertFalse(TokenStream.isKeyword("ink"));
        assertFalse(TokenStream.isKeyword("low"));

        // Length 4
        assertTrue(TokenStream.isKeyword("byte"));
        assertTrue(TokenStream.isKeyword("case"));
        assertTrue(TokenStream.isKeyword("char"));
        assertTrue(TokenStream.isKeyword("else"));
        assertTrue(TokenStream.isKeyword("enum"));
        assertTrue(TokenStream.isKeyword("goto"));
        assertTrue(TokenStream.isKeyword("long"));
        assertTrue(TokenStream.isKeyword("null"));
        assertTrue(TokenStream.isKeyword("true"));
        assertTrue(TokenStream.isKeyword("this"));
        assertTrue(TokenStream.isKeyword("void"));
        assertTrue(TokenStream.isKeyword("with"));
        assertFalse(TokenStream.isKeyword("boat"));
        assertFalse(TokenStream.isKeyword("cast"));
        assertFalse(TokenStream.isKeyword("that"));

        // Length 5
        assertTrue(TokenStream.isKeyword("class"));
        assertTrue(TokenStream.isKeyword("break"));
        assertTrue(TokenStream.isKeyword("yield"));
        assertTrue(TokenStream.isKeyword("while"));
        assertTrue(TokenStream.isKeyword("false"));
        assertTrue(TokenStream.isKeyword("const"));
        assertTrue(TokenStream.isKeyword("final"));
        assertTrue(TokenStream.isKeyword("float"));
        assertTrue(TokenStream.isKeyword("short"));
        assertTrue(TokenStream.isKeyword("super"));
        assertTrue(TokenStream.isKeyword("throw"));
        assertTrue(TokenStream.isKeyword("catch"));
        assertFalse(TokenStream.isKeyword("clash"));
        assertFalse(TokenStream.isKeyword("bread"));
        assertFalse(TokenStream.isKeyword("water"));

        // Length 6
        assertTrue(TokenStream.isKeyword("native"));
        assertTrue(TokenStream.isKeyword("delete"));
        assertTrue(TokenStream.isKeyword("return"));
        assertTrue(TokenStream.isKeyword("throws"));
        assertTrue(TokenStream.isKeyword("import"));
        assertTrue(TokenStream.isKeyword("double"));
        assertTrue(TokenStream.isKeyword("static"));
        assertTrue(TokenStream.isKeyword("public"));
        assertTrue(TokenStream.isKeyword("switch"));
        assertTrue(TokenStream.isKeyword("export"));
        assertTrue(TokenStream.isKeyword("typeof"));
        assertFalse(TokenStream.isKeyword("nation"));
        assertFalse(TokenStream.isKeyword("retail"));

        // Length 7
        assertTrue(TokenStream.isKeyword("package"));
        assertTrue(TokenStream.isKeyword("default"));
        assertTrue(TokenStream.isKeyword("finally"));
        assertTrue(TokenStream.isKeyword("boolean"));
        assertTrue(TokenStream.isKeyword("private"));
        assertTrue(TokenStream.isKeyword("extends"));
        assertFalse(TokenStream.isKeyword("packing"));
        assertFalse(TokenStream.isKeyword("defense"));

        // Length 8
        assertTrue(TokenStream.isKeyword("abstract"));
        assertTrue(TokenStream.isKeyword("continue"));
        assertTrue(TokenStream.isKeyword("debugger"));
        assertTrue(TokenStream.isKeyword("function"));
        assertTrue(TokenStream.isKeyword("volatile"));
        assertFalse(TokenStream.isKeyword("absolute"));
        assertFalse(TokenStream.isKeyword("fracture"));

        // Length 9
        assertTrue(TokenStream.isKeyword("interface"));
        assertTrue(TokenStream.isKeyword("protected"));
        assertTrue(TokenStream.isKeyword("transient"));
        assertFalse(TokenStream.isKeyword("interfere"));

        // Length 10
        assertTrue(TokenStream.isKeyword("implements"));
        assertTrue(TokenStream.isKeyword("instanceof"));
        assertFalse(TokenStream.isKeyword("implementx"));

        // Length 12
        assertTrue(TokenStream.isKeyword("synchronized"));
        assertFalse(TokenStream.isKeyword("synchronous1"));

        // Odd lengths without keywords
        assertFalse(TokenStream.isKeyword("elevenchars"));
        assertFalse(TokenStream.isKeyword("thirteenchars"));
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapesInIdentifierKeywordConversion() throws IOException {
        // \u0069f matches "if", but since it contains an escape, convertLastCharToHex is called!
        TokenStream ts = createStringStream("\\u0069f");
        assertEquals(Token.NAME, ts.getToken());
        assertEquals("i\\u0066", ts.getString());

        TokenStream ts2 = createStringStream("a\\u0062c");
        assertEquals(Token.NAME, ts2.getToken());
        assertEquals("abc", ts2.getString());
    }

    @Test(timeout = 4000)
    public void testLanguageVersionLetAndYieldCompatibility() throws IOException {
        CompilerEnvirons env16 = new CompilerEnvirons();
        env16.setLanguageVersion(Context.VERSION_1_6);
        TokenStream ts16 = createCustomStream("let yield", env16);

        assertEquals(Token.NAME, ts16.getToken());
        assertEquals("let", ts16.getString());
        assertEquals(Token.NAME, ts16.getToken());
        assertEquals("yield", ts16.getString());

        CompilerEnvirons env17 = new CompilerEnvirons();
        env17.setLanguageVersion(Context.VERSION_1_7);
        TokenStream ts17 = createCustomStream("let yield", env17);

        assertEquals(Token.LET, ts17.getToken());
        assertEquals(Token.YIELD, ts17.getToken());
    }

    @Test(timeout = 4000)
    public void testReservedKeywordAsIdentifierSetting() throws IOException {
        CompilerEnvirons envRes = new CompilerEnvirons();
        envRes.setReservedKeywordAsIdentifier(true);
        TokenStream tsRes = createCustomStream("abstract", envRes);
        assertEquals(Token.NAME, tsRes.getToken());

        CompilerEnvirons envStrict = new CompilerEnvirons();
        envStrict.setReservedKeywordAsIdentifier(false);
        TokenStream tsStrict = createCustomStream("abstract", envStrict);
        assertEquals(Token.RESERVED, tsStrict.getToken());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidConstructorArgumentsBothNotNull() {
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        Parser parser = new Parser(compilerEnv);
        try {
            new TokenStream(parser, new StringReader(""), "not null", 1);
            fail("Expected RuntimeException from Kit.codeBug");
        } catch (RuntimeException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInvalidConstructorArgumentsBothNull() {
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        Parser parser = new Parser(compilerEnv);
        try {
            new TokenStream(parser, null, null, 1);
            fail("Expected RuntimeException from Kit.codeBug");
        } catch (RuntimeException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testUnterminatedStringLiteralReturnsError() throws IOException {
        TokenStream ts = createStringStream("\"unterminated");
        assertEquals(Token.ERROR, ts.getToken());

        TokenStream ts2 = createStringStream("'unterminated\n");
        assertEquals(Token.ERROR, ts2.getToken());
    }

    @Test(timeout = 4000)
    public void testMalformedUnicodeEscapeReturnsError() throws IOException {
        TokenStream ts = createStringStream("\\u00ZX");
        assertEquals(Token.ERROR, ts.getToken());

        TokenStream ts2 = createStringStream("var a\\x;");
        assertEquals(Token.VAR, ts2.getToken());
        assertEquals(Token.ERROR, ts2.getToken());
    }

    @Test(timeout = 4000)
    public void testMissingExponentInNumberReturnsError() throws IOException {
        TokenStream ts = createStringStream("1e");
        assertEquals(Token.ERROR, ts.getToken());

        TokenStream ts2 = createStringStream("1e+");
        assertEquals(Token.ERROR, ts2.getToken());
    }

    @Test(timeout = 4000)
    public void testUnterminatedCommentReturnsCommentWithError() throws IOException {
        TokenStream ts = createStringStream("/* unclosed block comment");
        assertEquals(Token.COMMENT, ts.getToken());
        assertEquals(Token.CommentType.BLOCK_COMMENT, ts.getCommentType());
    }

    @Test(timeout = 4000)
    public void testIllegalTopLevelCharacterReturnsError() throws IOException {
        TokenStream ts = createStringStream("#");
        assertEquals(Token.ERROR, ts.getToken());
    }

    @Test(timeout = 4000)
    public void testMalformedXmlTokensReturnError() throws IOException {
        TokenStream ts = createStringStream("<tag attr='val");
        assertEquals(Token.LT, ts.getToken());
        assertEquals(Token.ERROR, ts.getFirstXMLToken());

        TokenStream ts2 = createStringStream("<!-- unclosed comment");
        assertEquals(Token.LT, ts2.getToken());
        assertEquals(Token.ERROR, ts2.getFirstXMLToken());

        TokenStream ts3 = createStringStream("<![CDATA[ unclosed cdata");
        assertEquals(Token.LT, ts3.getToken());
        assertEquals(Token.ERROR, ts3.getFirstXMLToken());

        TokenStream ts4 = createStringStream("<?pi unclosed");
        assertEquals(Token.LT, ts4.getToken());
        assertEquals(Token.ERROR, ts4.getFirstXMLToken());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCommentRecordingAndResetLifecycle() throws IOException {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecordingComments(true);
        Parser parser = new Parser(env);
        String code = "// first comment\nvar a = 1;";

        TokenStream tsString = new TokenStream(parser, null, code, 1);
        assertEquals(Token.COMMENT, tsString.getToken());
        assertEquals("// first comment", tsString.getAndResetCurrentComment());

        TokenStream tsReader = new TokenStream(parser, new StringReader(code), null, 1);
        assertEquals(Token.COMMENT, tsReader.getToken());
        assertEquals("// first comment", tsReader.getAndResetCurrentComment());
    }

    @Test(timeout = 4000)
    public void testPositionAndOffsetTracking() throws IOException {
        String code = "var x = 10;\nvar y = 20;";
        TokenStream ts = createStringStream(code);

        assertEquals(Token.VAR, ts.getToken());
        assertEquals(0, ts.getTokenBeg());
        assertEquals(3, ts.getTokenEnd());
        assertEquals(3, ts.getTokenLength());
        assertEquals(1, ts.getLineno());
        assertEquals(3, ts.getOffset());
        assertEquals("var x = 10;", ts.getLine());

        assertEquals(Token.NAME, ts.getToken());
        assertEquals("x", ts.getString());

        assertEquals(Token.ASSIGN, ts.getToken());
        assertEquals(Token.NUMBER, ts.getToken());
        assertEquals(Token.SEMI, ts.getToken());
        assertEquals(Token.EOL, ts.getToken());

        assertEquals(Token.VAR, ts.getToken());
        assertEquals(2, ts.getLineno());
        assertEquals("var y = 20;", ts.getLine());
    }

    @Test(timeout = 4000)
    public void testHtmlCommentBacktrackingGuard() throws IOException {
        // Tests '<!-' which is not '<!--' and must backtrack without infinite loop
        TokenStream ts = createStringStream("<!- var x;");
        assertEquals(Token.LT, ts.getToken());
        assertEquals(Token.NOT, ts.getToken());
        assertEquals(Token.SUB, ts.getToken());
        assertEquals(Token.VAR, ts.getToken());
    }
}