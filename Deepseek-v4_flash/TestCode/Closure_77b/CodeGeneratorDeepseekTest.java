package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for CodeGenerator.
 * Targets all major branches, boundary conditions, and the known defect
 * where a null character in a string is incorrectly escaped as \\u0000.
 */
public class CodeGeneratorDeepseekTest {

    // ---- Helper CodeConsumer implementation for capturing output ----
    private static class RecordingConsumer extends CodeConsumer {
        final StringBuilder sb = new StringBuilder();

        @Override
        void add(String str) {
            sb.append(str);
        }

        @Override
        void addIdentifier(String identifier) {
            sb.append(identifier);
        }

        @Override
        void addNumber(double x) {
            sb.append(x);
        }

        @Override
        void addOp(String op, boolean binOp) {
            sb.append(op);
        }

        @Override
        void listSeparator() {
            sb.append(", ");
        }

        @Override
        void beginBlock() {
            sb.append("{");
        }

        @Override
        void endBlock(boolean b) {
            sb.append("}");
        }

        @Override
        void endFunction(boolean b) {
            sb.append(";");
        }

        @Override
        void endStatement() {
            sb.append(";");
        }

        @Override
        void endStatement(boolean b) {
            sb.append(";");
        }

        @Override
        void beginCaseBody() {
            sb.append("{");
        }

        @Override
        void endCaseBody() {
            sb.append("}");
        }

        @Override
        void startSourceMapping(Node n) {}

        @Override
        void endSourceMapping(Node n) {}

        @Override
        boolean continueProcessing() { return true; }

        @Override
        boolean shouldPreserveExtraBlocks() { return false; }

        @Override
        void notePreferredLineBreak() {}

        @Override
        void maybeLineBreak() {}

        @Override
        boolean breakAfterBlockFor(Node n, boolean b) { return false; }
    }

    // ================== Constructor Tests ==================
    @Test(timeout = 4000)
    public void testConstructorWithNullCharset() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer, null);
        assertNotNull(gen);
    }

    @Test(timeout = 4000)
    public void testConstructorWithUTF8Charset() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer, StandardCharsets.UTF_8);
        assertNotNull(gen);
        // Trigger code path with encoder
        gen.add(Node.newString("\u00e9")); // é
        assertTrue(consumer.sb.toString().contains("é") || consumer.sb.toString().contains("\\u00e9"));
    }

    // ================== Public API Tests ==================
    @Test(timeout = 4000)
    public void testTagAsStrict() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        gen.tagAsStrict();
        assertEquals("'use strict';", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddStringDirect() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        gen.add("hello");
        assertEquals("hello", consumer.sb.toString());
    }

    // ================== Node Tests ==================
    @Test(timeout = 4000)
    public void testAddStringNode() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node strNode = Node.newString("test");
        gen.add(strNode);
        assertEquals("\"test\"", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddNumberNode() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node numNode = Node.newNumber(42.5);
        gen.add(numNode);
        assertEquals("42.5", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddNumberZero() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node numNode = Node.newNumber(0.0);
        gen.add(numNode);
        assertEquals("0.0", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddNumberNegative() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node numNode = Node.newNumber(-3.14);
        gen.add(numNode);
        assertEquals("-3.14", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddNameNodeSimple() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node nameNode = Node.newString(Token.NAME, "x");
        gen.add(nameNode);
        assertEquals("x", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testAddNameNodeWithAssignment() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node assignNode = Node.newString(Token.NAME, "y"); // first child
        nameNode.addChildToFront(assignNode);
        gen.add(nameNode);
        // Expect: "x=y"
        assertEquals("x=y", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testArrayLiteral() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node arrayLit = new Node(Token.ARRAYLIT);
        arrayLit.addChildToBack(Node.newNumber(1));
        arrayLit.addChildToBack(Node.newString("a"));
        gen.add(arrayLit);
        assertEquals("[1, \"a\"]", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testObjectLiteral() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node objLit = new Node(Token.OBJECTLIT);
        Node prop1 = Node.newString("foo");
        prop1.addChildToBack(Node.newNumber(42));
        objLit.addChildToBack(prop1);
        // context START_OF_EXPR to test parentheses
        gen.add(objLit, CodeGenerator.Context.START_OF_EXPR);
        assertEquals("({foo: 42})", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testGetProp() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node getProp = new Node(Token.GETPROP);
        getProp.addChildToBack(Node.newString(Token.NAME, "obj"));
        getProp.addChildToBack(Node.newString("prop"));
        gen.add(getProp);
        assertEquals("obj.prop", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testGetElem() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node getElem = new Node(Token.GETELEM);
        getElem.addChildToBack(Node.newString(Token.NAME, "arr"));
        getElem.addChildToBack(Node.newNumber(0));
        gen.add(getElem);
        assertEquals("arr[0]", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testFunction() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        // function f(x) { return x; }
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.LP);
        params.addChildToBack(Node.newString(Token.NAME, "x"));
        Node body = new Node(Token.BLOCK);
        Node ret = new Node(Token.RETURN);
        ret.addChildToBack(Node.newString(Token.NAME, "x"));
        body.addChildToBack(ret);
        Node func = new Node(Token.FUNCTION, name, params, body);
        gen.add(func);
        assertEquals("function f(x) {return x;};", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testIfStatementWithElse() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node cond = Node.newString(Token.NAME, "c");
        Node thenBlock = new Node(Token.BLOCK);
        thenBlock.addChildToBack(new Node(Token.EMPTY));
        Node elseBlock = new Node(Token.BLOCK);
        elseBlock.addChildToBack(new Node(Token.EMPTY));
        Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);
        gen.add(ifNode);
        assertEquals("if(c){;};", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testSimpleVar() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "x");
        name.addChildToFront(Node.newNumber(1));
        var.addChildToBack(name);
        gen.add(var);
        assertEquals("var x=1", consumer.sb.toString());
    }

    // ================== Known Defect: Null Character ==================
    @Test(timeout = 4000)
    public void testNullCharacterInString() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        // Create a string containing the null character
        Node strNode = Node.newString("\0");
        gen.add(strNode);
        // The buggy version outputs "\u0000", the correct output is "\x00"
        // Assert that we get the compact hex escape
        assertEquals("\"\\x00\"", consumer.sb.toString());
    }

    // ================== Static Methods ==================
    @Test(timeout = 4000)
    public void testJsStringEmpty() {
        assertEquals("\"\"", CodeGenerator.jsString("", null));
    }

    @Test(timeout = 4000)
    public void testJsStringWithEscape() {
        // Contains both quotes to test optimal quoting
        assertEquals("\"\\\"'\"", CodeGenerator.jsString("\"'", null));
    }

    @Test(timeout = 4000)
    public void testIdentifierEscapeLatin() {
        assertEquals("hello", CodeGenerator.identifierEscape("hello"));
    }

    @Test(timeout = 4000)
    public void testIdentifierEscapeNonLatin() {
        String escaped = CodeGenerator.identifierEscape("\u00e9");
        assertEquals("\\u00e9", escaped);
    }

    @Test(timeout = 4000)
    public void testRegexpEscape() {
        assertEquals("/hello/", CodeGenerator.regexpEscape("hello", null));
    }

    @Test(timeout = 4000)
    public void testEscapeToDoubleQuotedJsString() {
        assertEquals("\"a\\\"b\"", CodeGenerator.escapeToDoubleQuotedJsString("a\"b"));
    }

    // ================== Exception/Error Paths ==================
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinaryOperatorBadChildCount() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newNumber(1)); // Only one child -> violates precondition
        gen.add(addNode);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStringNodeWithChildren() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node strNode = Node.newString("bad");
        strNode.addChildToBack(Node.newString("child")); // Should not have children
        gen.add(strNode);
    }

    // ================== Edge Cases ==================
    @Test(timeout = 4000)
    public void testAddEmptyBlock() {
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node block = new Node(Token.BLOCK);
        gen.add(block);
        assertEquals("", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testContextInForInitClause() {
        // Test that 'in' operator inside for-init is parenthesized
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node inNode = new Node(Token.IN);
        inNode.addChildToBack(Node.newString(Token.NAME, "x"));
        inNode.addChildToBack(Node.newString(Token.NAME, "y"));
        gen.addExpr(inNode, 0, CodeGenerator.Context.IN_FOR_INIT_CLAUSE);
        assertEquals("(x in y)", consumer.sb.toString());
    }

    @Test(timeout = 4000)
    public void testContextInForInitClauseCleared() {
        // After parenthesization, context should be cleared
        RecordingConsumer consumer = new RecordingConsumer();
        CodeGenerator gen = new CodeGenerator(consumer);
        Node addNode = new Node(Token.ADD);
        addNode.addChildToBack(Node.newString(Token.NAME, "a"));
        addNode.addChildToBack(Node.newString(Token.NAME, "b"));
        gen.addExpr(addNode, 0, CodeGenerator.Context.IN_FOR_INIT_CLAUSE);
        assertEquals("a+b", consumer.sb.toString());
    }
}