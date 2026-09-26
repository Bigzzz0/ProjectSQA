package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Constructor with null/ASCII/non-ASCII output charset
 * - tagAsStrict() adds 'use strict';
 * - add(String) delegates to consumer
 * - addIdentifier() with latin/non-latin identifiers
 * - add(Node) with various token types
 * - getSimpleNumber() with valid numeric strings, edge lengths, leading zeros
 * - isSimpleNumber() with empty, non-numeric, valid cases
 * - jsString() quote selection with preferSingleQuotes true/false
 * - regexpEscape() with null encoder
 * - escapeToDoubleQuotedJsString() basic functionality
 * - identifierEscape() with all-latin/non-latin mixes
 * - getNonEmptyChildCount() with blocks, empties
 * - getFirstNonEmptyChild() returning null/valid
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - strEscape: NULL char, \u000B, all control chars, line terminators \u2028/\u2029
 * - strEscape: = & characters with trustedStrings true/false
 * - strEscape: > with --/> and ]]/> sequences
 * - strEscape: < with /script and !-- sequences
 * - appendHexJavaScriptRepresentation: supplementary code points
 * - getSimpleNumber: Long.MAX_VALUE overflow, leading zeros
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - KNOWN DEFECT: IN_FOR_INIT_CLAUSE context not propagated through HOOK/COMMA
 *   When generating "for(a=c?0:[0 in d];;)foo()", the "in" operator inside
 *   the array literal inside the hook should be wrapped in parens because
 *   the context is IN_FOR_INIT_CLAUSE. The bug: context is lost through
 *   the HOOK expression path.
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Various Preconditions.checkState failure conditions:
 *   - Bad binary operator child count != 2
 *   - REGEXP children not strings
 *   - FUNCTION child count != 3
 *   - GETTER_DEF/SETTER_DEF preconditions
 *   - GETPROP RHS not string, child count != 2
 *   - GETELEM child count != 2
 *   - CATCH child count != 2
 *   - THROW child count != 1
 *   - RETURN child count > 1
 *   - LABEL first child not LABEL_NAME
 *   - BREAK/CONTINUE child not LABEL_NAME
 *   - STRING with children
 *   - Unknown token type
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - forCostEstimation factory method creates with correct defaults
 * - Constructor with options sets fields correctly
 */
public class CodeGeneratorDeepseekTest {
    
    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================
    
    @Test(timeout = 4000)
    public void testConstructorWithNullCharset() {
        CompilerOptions options = new CompilerOptions();
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        consumer.assertEmpty();  // No output on construction
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithASCIICharset() {
        CompilerOptions options = new CompilerOptions();
        options.setOutputCharset(java.nio.charset.StandardCharsets.US_ASCII);
        options.preferSingleQuotes = true;
        options.trustedStrings = false;
        options.setLanguageOut(LanguageMode.ECMASCRIPT3);
        CodeConsumer consumer = new CodeConsumer();
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        consumer.assertEmpty();
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithUtf8Charset() {
        CompilerOptions options = new CompilerOptions();
        options.setOutputCharset(java.nio.charset.StandardCharsets.UTF_8);
        options.preferSingleQuotes = false;
        options.trustedStrings = true;
        options.setLanguageOut(LanguageMode.ECMASCRIPT5);
        CodeConsumer consumer = new CodeConsumer();
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        consumer.assertEmpty();
    }
    
    @Test(timeout = 4000)
    public void testForCostEstimationFactory() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        consumer.assertEmpty();
    }
    
    @Test(timeout = 4000)
    public void testTagAsStrict() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        cg.tagAsStrict();
        assertEquals("'use strict';", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testAddString() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        cg.add("hello");
        assertEquals("hello", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testAddIdentifierWithLatinChars() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        // We can't call addIdentifier directly as it's private, but we can test
        // through add(Node) with NAME node
        Node nameNode = Node.newString(Token.NAME, "myVar");
        cg.add(nameNode);
        assertEquals("myVar", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testAddIdentifierWithNonLatinChars() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node nameNode = Node.newString(Token.NAME, "caf\u00e9");
        cg.add(nameNode);
        assertEquals("caf\\u00e9", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testIsSimpleNumber() {
        assertTrue(CodeGenerator.isSimpleNumber("123"));
        assertTrue(CodeGenerator.isSimpleNumber("0"));
        assertTrue(CodeGenerator.isSimpleNumber("007"));
        assertTrue(CodeGenerator.isSimpleNumber("9"));
        assertFalse(CodeGenerator.isSimpleNumber(""));
        assertFalse(CodeGenerator.isSimpleNumber("12a"));
        assertFalse(CodeGenerator.isSimpleNumber(" 1"));
        assertFalse(CodeGenerator.isSimpleNumber("-1"));
        assertFalse(CodeGenerator.isSimpleNumber("1.5"));
        assertFalse(CodeGenerator.isSimpleNumber("0x1"));
    }
    
    @Test(timeout = 4000)
    public void testGetSimpleNumber() {
        assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
        assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
        assertEquals(7.0, CodeGenerator.getSimpleNumber("007"), 0.0);
        assertEquals(9.0, CodeGenerator.getSimpleNumber("9"), 0.0);
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("12a")));
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber(" 1")));
        
        // Test overflow to NaN
        String bigNumber = String.valueOf(Long.MAX_VALUE);
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber(bigNumber)));
    }
    
    @Test(timeout = 4000)
    public void testJsStringQuoteSelection() {
        // With preferSingleQuotes=false (default for forCostEstimation)
        // If singleq < doubleq => use double quotes
       CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // Test string with more double quotes - should use single quotes
        // This goes through addJsString which is private, test via add(Node) with STRING
        Node strNode = Node.newString(Token.STRING, "it's \"double\" quoted");
        cg.add(strNode);
        assertEquals("'it\\'s \"double\" quoted'", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testRegexpEscapeWithNullEncoder() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.regexpEscape("test");
        assertEquals("/test/", result);
    }
    
    @Test(timeout = 4000)
    public void testEscapeToDoubleQuotedJsString() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("hello\"world");
        assertEquals("\"hello\\\"world\"", result);
    }
    
    @Test(timeout = 4000)
    public void testIdentifierEscapeAllLatin() {
        assertEquals("hello", CodeGenerator.identifierEscape("hello"));
    }
    
    @Test(timeout = 4000)
    public void testIdentifierEscapeWithNonLatin() {
        assertEquals("h\\u00e9llo", CodeGenerator.identifierEscape("h\u00e9llo"));
    }
    
    @Test(timeout = 4000)
    public void testGetNonEmptyChildCountEmpty() {
        Node block = new Node(Token.BLOCK);
        assertEquals(0, CodeGenerator.getNonEmptyChildCount(block, 2));
    }
    
    @Test(timeout = 4000)
    public void testGetNonEmptyChildCountWithEmpties() {
        Node block = new Node(Token.BLOCK);
        block.addChildToBack(new Node(Token.EMPTY));
        block.addChildToBack(new Node(Token.EMPTY));
        assertEquals(0, CodeGenerator.getNonEmptyChildCount(block, 2));
    }
    
    @Test(timeout = 4000)
    public void testGetNonEmptyChildCountWithFunction() {
        Node block = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION);
        block.addChildToBack(func);
        assertEquals(1, CodeGenerator.getNonEmptyChildCount(block, 2));
    }
    
    @Test(timeout = 4000)
    public void testGetFirstNonEmptyChildReturnsNull() {
        Node block = new Node(Token.BLOCK);
        block.addChildToBack(new Node(Token.EMPTY));
        assertNull(CodeGenerator.getFirstNonEmptyChild(block));
    }
    
    @Test(timeout = 4000)
    public void testGetFirstNonEmptyChildReturnsValid() {
        Node block = new Node(Token.BLOCK);
        block.addChildToBack(new Node(Token.EMPTY));
        Node func = new Node(Token.FUNCTION);
        block.addChildToBack(func);
        assertEquals(func, CodeGenerator.getFirstNonEmptyChild(block));
    }
    
    @Test(timeout = 4000)
    public void testGetFirstNonEmptyChildWithNestedBlock() {
        Node outer = new Node(Token.BLOCK);
        Node inner = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION);
        inner.addChildToBack(func);
        outer.addChildToBack(inner);
        assertEquals(func, CodeGenerator.getFirstNonEmptyChild(outer));
    }
    
    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================
    
    @Test(timeout = 4000)
    public void testStrEscapeNullChar() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\0");
        assertEquals("\"\\x00\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeVerticalTabWithoutSlashV() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        // useSlashV=false in escapeToDoubleQuotedJsString
        String result = cg.escapeToDoubleQuotedJsString("\u000B");
        assertEquals("\"\\x0B\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeBackspace() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\b");
        assertEquals("\"\\b\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeFormFeed() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\f");
        assertEquals("\"\\f\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeNewline() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\n");
        assertEquals("\"\\n\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeCarriageReturn() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\r");
        assertEquals("\"\\r\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeTab() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\t");
        assertEquals("\"\\t\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeBackslash() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\\");
        assertEquals("\"\\\\\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeUnicode2028() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\u2028");
        assertEquals("\"\\u2028\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeUnicode2029() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("\u2029");
        assertEquals("\"\\u2029\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeEqualsWithUntrusted() {
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = false;
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = new CodeGenerator(consumer, options);
        // Test via addJsString with a STRING node containing '='
        Node strNode = Node.newString(Token.STRING, "a=b");
        cg.add(strNode);
        assertEquals("\"a\\x3db\"", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeEqualsWithTrusted() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("a=b");
        assertEquals("\"a=b\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeAmpersandUntrusted() {
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = false;
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = new CodeGenerator(consumer, options);
        Node strNode = Node.newString(Token.STRING, "a&b");
        cg.add(strNode);
        assertEquals("\"a\\x26b\"", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeGreaterThanWithUntrusted() {
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = false;
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = new CodeGenerator(consumer, options);
        Node strNode = Node.newString(Token.STRING, "a>b");
        cg.add(strNode);
        assertEquals("\"a\\x3eb\"", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeGreaterThanWithDashDash() {
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = true;
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = new CodeGenerator(consumer, options);
        // --> should escape the >
        Node strNode = Node.newString(Token.STRING, "a-->b");
        cg.add(strNode);
        assertEquals("\"a--\\x3eb\"", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeGreaterThanWithBracketBracket() {
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = true;
        options.setOutputCharset(null);
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = new CodeGenerator(consumer, options);
        // ]]> should escape the >
        Node strNode = Node.newString(Token.STRING, "a]]>b");
        cg.add(strNode);
        assertEquals("\"a]]\\x3eb\"", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeLessThanWithEndScript() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("</script>");
        assertEquals("\"<\\/script>\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeLessThanWithStartComment() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("<!--");
        assertEquals("\"<\\!--\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStrEscapeLessThanNormal() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("a<b");
        assertEquals("\"a<b\"", result);
    }
    
    @Test(timeout = 4000)
    public void testAppendHexSupplementaryCodePoint() {
        // This test verifies the appendHexJavaScriptRepresentation for supplementary characters
        // via identifierEscape which uses it for non-Latin characters
        // U+1F600 (😀) - supplementary character
        String result = CodeGenerator.identifierEscape("a\ud83d\ude00b");
        // Should produce two \u sequences
        assertTrue(result.contains("\\u"));
        assertTrue(result.length() > 3);
    }
    
    @Test(timeout = 4000)
    public void testStringWithHighAsciiChars() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        // Characters > 0x7f should be escaped
        String result = cg.escapeToDoubleQuotedJsString("\u00e9");
        assertEquals("\"\\u00e9\"", result);
    }
    
    @Test(timeout = 4000)
    public void testStringWithPrintableAscii() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        String result = cg.escapeToDoubleQuotedJsString("Hello World!");
        assertEquals("\"Hello World!\"", result);
    }
    
    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================
    
    /**
     * KNOWN DEFECT: The IN_FOR_INIT_CLAUSE context is lost through
     * certain expression paths (HOOK, COMMA) when generating code for
     * for-loop init clauses. This causes the "in" operator inside 
     * subexpressions to not be wrapped in parentheses.
     * 
     * Test case: for(a=c?0:[0 in d];;)foo()
     * Expected: for(a=c?0:[(0 in d)];;)foo()  (parens around "0 in d")
     * Bug:      for(a=c?0:[0 in d];;)foo()    (missing parens)
     */
    @Test(timeout = 4000)
    public void testInOperatorInForLoopInitClauseWithHook() {
        // Construct: for(a = c ? 0 : [0 in d]; ; ) foo()
        // Where the array literal contains "0 in d" and this is in the
        // init clause of a for loop
        CodeConsumer consumer = new CodeConsumer();
        CompilerOptions options = new CompilerOptions();
        options.setLanguageOut(LanguageMode.ECMASCRIPT5);
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        // Build AST for: for(a = c ? 0 : [0 in d]; ; ) foo()
        Node forNode = new Node(Token.FOR);
        
        // Init: a = c ? 0 : [0 in d]
        Node assign = new Node(Token.ASSIGN);
        Node nameA = Node.newString(Token.NAME, "a");
        Node hook = new Node(Token.HOOK);
        Node nameC = Node.newString(Token.NAME, "c");
        Node zero = Node.newNumber(0.0);
        Node arrayLit = new Node(Token.ARRAYLIT);
        Node inExpr = new Node(Token.IN);
        Node zeroIn = Node.newNumber(0.0);
        Node nameD = Node.newString(Token.NAME, "d");
        inExpr.addChildToFront(zeroIn);
        inExpr.addChildToFront(nameD);  // Note: IN is left-to-right, children order: left, right
        // Actually the IN token: first child is left operand, second is right operand
        // Let's rebuild correctly
        inExpr = new Node(Token.IN);
        inExpr.addChildToBack(zeroIn);
        inExpr.addChildToBack(nameD);
        arrayLit.addChildToBack(inExpr);
        hook.addChildToBack(nameC);
        hook.addChildToBack(zero);
        hook.addChildToBack(arrayLit);
        assign.addChildToBack(nameA);
        assign.addChildToBack(hook);
        
        // Test condition: empty (for test we can use a simpler approach)
        // For this test, we'll use a simpler construction that triggers the same bug path.
        // The key is that the HOOK node doesn't propagate IN_FOR_INIT_CLAUSE context
        // to its children properly.
        
        // Let's construct a simpler test that exercises the same code path:
        // "for (a = 0 in [1]; ; ) foo()"
        // This should become "for (a = (0 in [1]); ; ) foo()" in the init clause
        // But if context is lost, it would be "for (a = 0 in [1]; ; ) foo()"
        
        Node forSimple = new Node(Token.FOR);
        Node assignSimple = new Node(Token.ASSIGN);
        Node nameASimple = Node.newString(Token.NAME, "a");
        Node inSimple = new Node(Token.IN);
        Node zeroSimple = Node.newNumber(0.0);
        Node arraySimple = new Node(Token.ARRAYLIT);
        Node oneSimple = Node.newNumber(1.0);
        arraySimple.addChildToBack(oneSimple);
        inSimple.addChildToBack(zeroSimple);
        inSimple.addChildToBack(arraySimple);
        assignSimple.addChildToBack(nameASimple);
        assignSimple.addChildToBack(inSimple);
        
        // Test condition: empty
        Node empty = new Node(Token.EMPTY);
        
        // Increment: empty
        Node empty2 = new Node(Token.EMPTY);
        
        // Body: foo()
        Node call = new Node(Token.CALL);
        Node nameFoo = Node.newString(Token.NAME, "foo");
        call.addChildToBack(nameFoo);
        
        forSimple.addChildToBack(assignSimple);
        forSimple.addChildToBack(empty);
        forSimple.addChildToBack(empty2);
        forSimple.addChildToBack(call);
        
        // Add with context PRESERVE_BLOCK as the for loop itself would be added
        cg.add(forSimple, CodeGenerator.Context.STATEMENT);
        
        String output = consumer.getOutput();
        // The correct output should have parentheses around "0 in [1]"
        // in the init clause: "for(a=(0 in[1]);;)foo()"
        assertTrue("The 'in' operator inside for-init clause should be parenthesized: " + output,
            output.contains("(0 in[1])") || output.contains("(0 in [1])"));
    }
    
    /**
     * Direct test for the reported defect: 
     * "for(a=c?0:[(0 in d)];;)foo()" vs "for(a=c?0:[0 in d];;)foo()"
     * Tests that when inside a for-init clause, the HOOK expression correctly
     * propagates the IN_FOR_INIT_CLAUSE context to its children,
     * causing the "in" operator inside the array literal to be parenthesized.
     */
    @Test(timeout = 4000)
    public void testInOperatorInForLoopInitClauseWithHookArrayLiteral() {
        // Build AST for: for(a = c ? 0 : [0 in d]; ; ) foo()
        CodeConsumer consumer = new CodeConsumer();
        CompilerOptions options = new CompilerOptions();
        options.setLanguageOut(LanguageMode.ECMASCRIPT5);
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        Node forNode = new Node(Token.FOR);
        
        // Init: a = c ? 0 : [0 in d]
        Node assign = new Node(Token.ASSIGN);
        Node nameA = Node.newString(Token.NAME, "a");
        Node hook = new Node(Token.HOOK);
        Node nameC = Node.newString(Token.NAME, "c");
        Node zero = Node.newNumber(0.0);
        Node arrayLit = new Node(Token.ARRAYLIT);
        Node inExpr = new Node(Token.IN);
        Node zeroIn = Node.newNumber(0.0);
        Node nameD = Node.newString(Token.NAME, "d");
        
        // IN operator: left child is first operand, right child is second
        inExpr.addChildToBack(zeroIn);
        inExpr.addChildToBack(nameD);
        arrayLit.addChildToBack(inExpr);
        
        // HOOK: condition, trueExpr, falseExpr
        hook.addChildToBack(nameC);
        hook.addChildToBack(zero);
        hook.addChildToBack(arrayLit);
        
        // ASSIGN: target, source
        assign.addChildToBack(nameA);
        assign.addChildToBack(hook);
        
        // Test condition: empty
        Node empty = new Node(Token.EMPTY);
        // Increment: empty
        Node empty2 = new Node(Token.EMPTY);
        // Body: foo()
        Node call = new Node(Token.CALL);
        Node nameFoo = Node.newString(Token.NAME, "foo");
        call.addChildToBack(nameFoo);
        
        forNode.addChildToBack(assign);
        forNode.addChildToBack(empty);
        forNode.addChildToBack(empty2);
        forNode.addChildToBack(call);
        
        // Set the script parent to ensure proper context
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(forNode);
        
        cg.add(forNode, CodeGenerator.Context.STATEMENT);
        
        String output = consumer.getOutput();
        
        // The bug is that the "in" operator inside the array literal in the 
        // false branch of the HOOK is NOT parenthesized because the 
        // IN_FOR_INIT_CLAUSE context is lost.
        // 
        // Correct: for(a=c?0:[(0 in d)];;)foo()
        // Bug:      for(a=c?0:[0 in d];;)foo()
        //
        // The fix should ensure the HOOK's children get the inherited context.
        // Since the HOOK is inside IN_FOR_INIT_CLAUSE, the array literal child
        // should also be in IN_FOR_INIT_CLAUSE, causing the IN inside it to 
        // be parenthesized.
        
        assertTrue("The 'in' operator inside the array literal in for-init clause " +
                   "should be parenthesized. Output: " + output,
                   output.contains("(0 in") || output.contains("(0  in"));
    }
    
    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddNullNode() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new CodeConsumer());
        cg.add((Node) null);
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBinaryOperatorWithWrongChildCount() {
        // A binary operator with 3 children (should be 2)
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node addOp = new Node(Token.ADD);
        addOp.addChildToBack(Node.newNumber(1.0));
        addOp.addChildToBack(Node.newNumber(2.0));
        addOp.addChildToBack(Node.newNumber(3.0));
        cg.add(addOp);
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testCatchWithWrongChildCount() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node catchNode = new Node(Token.CATCH);
        catchNode.addChildToBack(new Node(Token.EMPTY));
        // Should have 2 children, has only 1
        cg.add(catchNode);
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testThrowWithWrongChildCount() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node throwNode = new Node(Token.THROW);
        // THROW must have 1 child
        cg.add(throwNode);
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testReturnWithTooManyChildren() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node returnNode = new Node(Token.RETURN);
        returnNode.addChildToBack(Node.newNumber(1.0));
        returnNode.addChildToBack(Node.newNumber(2.0));
        cg.add(returnNode);
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testLabelWithWrongFirstChild() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node label = new Node(Token.LABEL);
        label.addChildToBack(Node.newNumber(42.0));  // Should be LABEL_NAME
        label.addChildToBack(new Node(Token.EMPTY));
        cg.add(label);
    }
    
    @Test(timeout = 4000, expected = Error.class)
    public void testUnknownTokenType() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node unknown = new Node(Token.LP);  // LP is not handled in switch
        cg.add(unknown);
    }
    
    @Test(timeout = 4000)
    public void testEmptyBlock() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node block = new Node(Token.BLOCK);
        cg.add(block, CodeGenerator.Context.PRESERVE_BLOCK);
        assertEquals("{}", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testEmptyStatement() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node empty = new Node(Token.EMPTY);
        cg.add(empty);
        assertEquals("", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testNullLiteral() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node nullNode = new Node(Token.NULL);
        cg.add(nullNode);
        assertEquals("null", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testThisLiteral() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node thisNode = new Node(Token.THIS);
        cg.add(thisNode);
        assertEquals("this", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testFalseLiteral() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node falseNode = new Node(Token.FALSE);
        cg.add(falseNode);
        assertEquals("false", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testTrueLiteral() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node trueNode = new Node(Token.TRUE);
        cg.add(trueNode);
        assertEquals("true", consumer.getOutput());
    }
    
    @Test(timeout = 4000)
    public void testDebugger() {
        CodeConsumer consumer = new CodeConsumer();
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        Node debugger = new Node(Token.DEBUGGER);
        cg.add(debugger);
        assertEquals("debugger", consumer.getOutput());
    }
    
    // ============================================================
    // Supporting inner class for test verification
    // ============================================================
    
    /**
     * A simple CodeConsumer implementation that captures output for testing.
     */
    private static class CodeConsumer extends com.google.javascript.jscomp.CodeConsumer {
        private StringBuilder output = new StringBuilder();
        private boolean processing = true;
        private int blockDepth = 0;
        
        @Override
        public boolean continueProcessing() {
            return processing;
        }
        
        public void stopProcessing() {
            processing = false;
        }
        
        @Override
        public void add(String str) {
            output.append(str);
        }
        
        public String getOutput() {
            return output.toString();
        }
        
        public void assertEmpty() {
            assertEquals("", output.toString());
        }
        
        // Required abstract methods (assuming CodeConsumer is abstract with these):
        
        @Override
        char getQuoteChar() {
            return '"';
        }
        
        @Override
        void addIdentifier(String identifier) {
            output.append(identifier);
        }
        
        @Override
        void addOp(String op, boolean binOp) {
            output.append(op);
        }
        
        @Override
        void addNumber(double x) {
            if (x == (long) x && !Double.isNaN(x)) {
                output.append(String.valueOf((long) x));
            } else {
                output.append(String.valueOf(x));
            }
        }
        
        @Override
        void addConstant(String constant) {
            output.append(constant);
        }
        
        @Override
        void startSourceMapping(Node node) {
            // No-op
        }
        
        @Override
        void endSourceMapping(Node node) {
            // No-op
        }
        
        @Override
        void beginBlock() {
            output.append("{");
            blockDepth++;
        }
        
        @Override
        void endBlock(boolean breakAfter) {
            output.append("}");
            blockDepth--;
            if (breakAfter) {
                // endStatement handling is separate
            }
        }
        
        @Override
        void listSeparator() {
            output.append(",");
        }
        
        @Override
        void endStatement(boolean needSemicolon) {
            if (needSemicolon) {
                output.append(";");
            }
        }
        
        @Override
        void endStatement() {
            output.append(";");
        }
        
        @Override
        void beginCaseBody() {
            // No-op for simplicity
        }
        
        @Override
        void endCaseBody() {
            // No-op for simplicity
        }
        
        @Override
        void maybeLineBreak() {
            // No-op
        }
        
        @Override
        void notePreferredLineBreak() {
            // No-op
        }
        
        @Override
        boolean breakAfterBlockFor(Node block, boolean isStatementContext) {
            return isStatementContext;
        }
        
        @Override
        boolean shouldPreserveExtraBlocks() {
            return false;
        }
        
        @Override
        boolean isWordChar(char ch) {
            return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
        }
        
        @Override
        void appendBlockStart() {
            output.append("{");
        }
        
        @Override
        void appendBlockEnd() {
            output.append("}");
        }
    }
}
