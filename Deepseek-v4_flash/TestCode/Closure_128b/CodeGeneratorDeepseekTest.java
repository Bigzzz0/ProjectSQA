package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * CodeGeneratorDeepseekTest - Advanced White-Box Test Suite for CodeGenerator
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Test basic code generation for all major token types
 *   - Test binary/unary operator generation
 *   - Test function/block/statement generation
 *   - Test string escaping and quote selection
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Test empty strings, null nodes, edge case numbers
 *   - Test boundary values for string escaping (0x00, 0x7F, 0x80, etc.)
 *   - Test MAX_POSITIVE_INTEGER_NUMBER boundary for getSimpleNumber
 *   - Test empty blocks, single-child blocks, multi-child blocks
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Test issue942: Object literal property names with numeric strings
 *     should NOT be quoted when they are simple numbers
 *   - Test isSimpleNumber with leading zeros, non-numeric strings
 *   - Test getSimpleNumber with various numeric string formats
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Test invalid token types
 *   - Test malformed AST structures
 *   - Test edge cases in unrollBinaryOperator
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Test forCostEstimation factory method
 *   - Test constructor with CompilerOptions
 *   - Test tagAsStrict method
 */
public class CodeGeneratorDeepseekTest {
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testAddString() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        cg.add("test");
        assertEquals("test", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testTagAsStrict() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        cg.tagAsStrict();
        assertEquals("'use strict';", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNullNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node nullNode = new Node(Token.NULL);
        cg.add(nullNode);
        assertEquals("null", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddThisNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node thisNode = new Node(Token.THIS);
        cg.add(thisNode);
        assertEquals("this", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddTrueNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node trueNode = new Node(Token.TRUE);
        cg.add(trueNode);
        assertEquals("true", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddFalseNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node falseNode = new Node(Token.FALSE);
        cg.add(falseNode);
        assertEquals("false", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNumberNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node numberNode = Node.newNumber(42.0);
        cg.add(numberNode);
        assertEquals("42", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddStringNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("hello");
        cg.add(stringNode);
        assertEquals("\"hello\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddStringNodeWithSpecialChars() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("hello\nworld");
        cg.add(stringNode);
        assertEquals("\"hello\\nworld\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddStringNodeWithQuotes() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // String with double quotes should use single quotes
        Node stringNode = Node.newString("he\"llo");
        cg.add(stringNode);
        assertEquals("'he\"llo'", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddStringNodeWithSingleQuotes() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // String with single quotes should use double quotes
        Node stringNode = Node.newString("he'llo");
        cg.add(stringNode);
        assertEquals("\"he'llo\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNameNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node nameNode = Node.newString(Token.NAME, "x");
        cg.add(nameNode);
        assertEquals("x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNameNodeWithAssignment() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node nameNode = Node.newString(Token.NAME, "x");
        Node valueNode = Node.newNumber(5.0);
        nameNode.addChildToFront(valueNode);
        cg.add(nameNode);
        assertEquals("x=5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddVarNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node valueNode = Node.newNumber(10.0);
        nameNode.addChildToFront(valueNode);
        varNode.addChildToFront(nameNode);
        cg.add(varNode);
        assertEquals("var x=10", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddReturnNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node returnNode = new Node(Token.RETURN);
        Node valueNode = Node.newNumber(42.0);
        returnNode.addChildToFront(valueNode);
        cg.add(returnNode);
        assertEquals("return42", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddReturnNodeNoValue() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node returnNode = new Node(Token.RETURN);
        cg.add(returnNode);
        assertEquals("return", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddThrowNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node throwNode = new Node(Token.THROW);
        Node errorNode = Node.newString(Token.NAME, "Error");
        throwNode.addChildToFront(errorNode);
        cg.add(throwNode);
        assertEquals("throwError", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddBreakNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node breakNode = new Node(Token.BREAK);
        cg.add(breakNode);
        assertEquals("break", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddContinueNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node continueNode = new Node(Token.CONTINUE);
        cg.add(continueNode);
        assertEquals("continue", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDebuggerNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node debuggerNode = new Node(Token.DEBUGGER);
        cg.add(debuggerNode);
        assertEquals("debugger", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddEmptyNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node emptyNode = new Node(Token.EMPTY);
        cg.add(emptyNode);
        assertEquals("", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddArrayLitNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node arrayNode = new Node(Token.ARRAYLIT);
        Node elem1 = Node.newNumber(1.0);
        Node elem2 = Node.newNumber(2.0);
        arrayNode.addChildToFront(elem2);
        arrayNode.addChildToFront(elem1);
        cg.add(arrayNode);
        assertEquals("[1,2]", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddObjectLitNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        Node keyNode = Node.newString(Token.STRING_KEY, "key");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        cg.add(objectNode);
        assertEquals("{key:1}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddFunctionNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "f");
        Node paramNode = new Node(Token.PARAM_LIST);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToFront(bodyNode);
        functionNode.addChildToFront(paramNode);
        functionNode.addChildToFront(nameNode);
        cg.add(functionNode);
        assertEquals("function f(){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNewNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node newNode = new Node(Token.NEW);
        Node constructorNode = Node.newString(Token.NAME, "Array");
        newNode.addChildToFront(constructorNode);
        cg.add(newNode);
        assertEquals("new Array", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNewNodeWithArgs() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node newNode = new Node(Token.NEW);
        Node constructorNode = Node.newString(Token.NAME, "Array");
        Node argNode = Node.newNumber(5.0);
        newNode.addChildToFront(argNode);
        newNode.addChildToFront(constructorNode);
        cg.add(newNode);
        assertEquals("new Array(5)", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDelPropNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node delNode = new Node(Token.DELPROP);
        Node propNode = Node.newString(Token.NAME, "x");
        delNode.addChildToFront(propNode);
        cg.add(delNode);
        assertEquals("delete x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddTypeofNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node typeofNode = new Node(Token.TYPEOF);
        Node operandNode = Node.newString(Token.NAME, "x");
        typeofNode.addChildToFront(operandNode);
        cg.add(typeofNode);
        assertEquals("typeof x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddVoidNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node voidNode = new Node(Token.VOID);
        Node operandNode = Node.newNumber(0.0);
        voidNode.addChildToFront(operandNode);
        cg.add(voidNode);
        assertEquals("void 0", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNotNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node notNode = new Node(Token.NOT);
        Node operandNode = Node.newString(Token.NAME, "x");
        notNode.addChildToFront(operandNode);
        cg.add(notNode);
        assertEquals("!x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddBitnotNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node bitnotNode = new Node(Token.BITNOT);
        Node operandNode = Node.newNumber(5.0);
        bitnotNode.addChildToFront(operandNode);
        cg.add(bitnotNode);
        assertEquals("~5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddPosNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node posNode = new Node(Token.POS);
        Node operandNode = Node.newNumber(5.0);
        posNode.addChildToFront(operandNode);
        cg.add(posNode);
        assertEquals("+5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNegNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node negNode = new Node(Token.NEG);
        Node operandNode = Node.newNumber(5.0);
        negNode.addChildToFront(operandNode);
        cg.add(negNode);
        assertEquals("-5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddNegNodeWithNegativeNumber() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node negNode = new Node(Token.NEG);
        Node operandNode = Node.newNumber(-2.0);
        negNode.addChildToFront(operandNode);
        cg.add(negNode);
        assertEquals("2", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddIncPreNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node incNode = new Node(Token.INC);
        incNode.putIntProp(Node.INCRDECR_PROP, 0); // pre-increment
        Node operandNode = Node.newString(Token.NAME, "x");
        incNode.addChildToFront(operandNode);
        cg.add(incNode);
        assertEquals("++x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddIncPostNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node incNode = new Node(Token.INC);
        incNode.putIntProp(Node.INCRDECR_PROP, 1); // post-increment
        Node operandNode = Node.newString(Token.NAME, "x");
        incNode.addChildToFront(operandNode);
        cg.add(incNode);
        assertEquals("x++", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDecPreNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node decNode = new Node(Token.DEC);
        decNode.putIntProp(Node.INCRDECR_PROP, 0); // pre-decrement
        Node operandNode = Node.newString(Token.NAME, "x");
        decNode.addChildToFront(operandNode);
        cg.add(decNode);
        assertEquals("--x", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDecPostNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node decNode = new Node(Token.DEC);
        decNode.putIntProp(Node.INCRDECR_PROP, 1); // post-decrement
        Node operandNode = Node.newString(Token.NAME, "x");
        decNode.addChildToFront(operandNode);
        cg.add(decNode);
        assertEquals("x--", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddHookNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node hookNode = new Node(Token.HOOK);
        Node condNode = Node.newString(Token.NAME, "a");
        Node trueNode = Node.newString(Token.NAME, "b");
        Node falseNode = Node.newString(Token.NAME, "c");
        hookNode.addChildToFront(falseNode);
        hookNode.addChildToFront(trueNode);
        hookNode.addChildToFront(condNode);
        cg.add(hookNode);
        assertEquals("a?b:c", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddCommaNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node commaNode = new Node(Token.COMMA);
        Node leftNode = Node.newNumber(1.0);
        Node rightNode = Node.newNumber(2.0);
        commaNode.addChildToFront(rightNode);
        commaNode.addChildToFront(leftNode);
        cg.add(commaNode);
        assertEquals("1,2", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddAssignNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node assignNode = new Node(Token.ASSIGN);
        Node leftNode = Node.newString(Token.NAME, "x");
        Node rightNode = Node.newNumber(5.0);
        assignNode.addChildToFront(rightNode);
        assignNode.addChildToFront(leftNode);
        cg.add(assignNode);
        assertEquals("x=5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddGetPropNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node getPropNode = new Node(Token.GETPROP);
        Node objNode = Node.newString(Token.NAME, "obj");
        Node propNode = Node.newString("prop");
        getPropNode.addChildToFront(propNode);
        getPropNode.addChildToFront(objNode);
        cg.add(getPropNode);
        assertEquals("obj.prop", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddGetElemNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node getElemNode = new Node(Token.GETELEM);
        Node objNode = Node.newString(Token.NAME, "arr");
        Node indexNode = Node.newNumber(0.0);
        getElemNode.addChildToFront(indexNode);
        getElemNode.addChildToFront(objNode);
        cg.add(getElemNode);
        assertEquals("arr[0]", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddCallNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node callNode = new Node(Token.CALL);
        Node funcNode = Node.newString(Token.NAME, "f");
        Node argNode = Node.newNumber(1.0);
        callNode.addChildToFront(argNode);
        callNode.addChildToFront(funcNode);
        cg.add(callNode);
        assertEquals("f(1)", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddIfNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node ifNode = new Node(Token.IF);
        Node condNode = Node.newString(Token.NAME, "a");
        Node thenNode = new Node(Token.BLOCK);
        Node elseNode = new Node(Token.BLOCK);
        ifNode.addChildToFront(elseNode);
        ifNode.addChildToFront(thenNode);
        ifNode.addChildToFront(condNode);
        cg.add(ifNode);
        assertEquals("if(a){}else{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddIfNodeNoElse() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node ifNode = new Node(Token.IF);
        Node condNode = Node.newString(Token.NAME, "a");
        Node thenNode = new Node(Token.BLOCK);
        ifNode.addChildToFront(thenNode);
        ifNode.addChildToFront(condNode);
        cg.add(ifNode);
        assertEquals("if(a){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddWhileNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node whileNode = new Node(Token.WHILE);
        Node condNode = Node.newString(Token.NAME, "a");
        Node bodyNode = new Node(Token.BLOCK);
        whileNode.addChildToFront(bodyNode);
        whileNode.addChildToFront(condNode);
        cg.add(whileNode);
        assertEquals("while(a){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDoNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node doNode = new Node(Token.DO);
        Node bodyNode = new Node(Token.BLOCK);
        Node condNode = Node.newString(Token.NAME, "a");
        doNode.addChildToFront(condNode);
        doNode.addChildToFront(bodyNode);
        cg.add(doNode);
        assertEquals("do{}while(a)", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddForNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node forNode = new Node(Token.FOR);
        Node initNode = Node.newString(Token.NAME, "i");
        Node condNode = Node.newString(Token.NAME, "j");
        Node incrNode = Node.newString(Token.NAME, "k");
        Node bodyNode = new Node(Token.BLOCK);
        forNode.addChildToFront(bodyNode);
        forNode.addChildToFront(incrNode);
        forNode.addChildToFront(condNode);
        forNode.addChildToFront(initNode);
        cg.add(forNode);
        assertEquals("for(i;j;k){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddForInNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node forInNode = new Node(Token.FOR);
        Node varNode = Node.newString(Token.NAME, "x");
        Node objNode = Node.newString(Token.NAME, "obj");
        Node bodyNode = new Node(Token.BLOCK);
        forInNode.addChildToFront(bodyNode);
        forInNode.addChildToFront(objNode);
        forInNode.addChildToFront(varNode);
        cg.add(forInNode);
        assertEquals("for(xin obj){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddWithNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node withNode = new Node(Token.WITH);
        Node objNode = Node.newString(Token.NAME, "obj");
        Node bodyNode = new Node(Token.BLOCK);
        withNode.addChildToFront(bodyNode);
        withNode.addChildToFront(objNode);
        cg.add(withNode);
        assertEquals("with(obj){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddSwitchNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node switchNode = new Node(Token.SWITCH);
        Node exprNode = Node.newString(Token.NAME, "x");
        switchNode.addChildToFront(exprNode);
        cg.add(switchNode);
        assertEquals("switch(x){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddCaseNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node caseNode = new Node(Token.CASE);
        Node valueNode = Node.newNumber(1.0);
        Node bodyNode = new Node(Token.BLOCK);
        caseNode.addChildToFront(bodyNode);
        caseNode.addChildToFront(valueNode);
        cg.add(caseNode);
        assertEquals("case 1{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddDefaultCaseNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node defaultCaseNode = new Node(Token.DEFAULT_CASE);
        Node bodyNode = new Node(Token.BLOCK);
        defaultCaseNode.addChildToFront(bodyNode);
        cg.add(defaultCaseNode);
        assertEquals("default{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddLabelNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node labelNode = new Node(Token.LABEL);
        Node labelNameNode = Node.newString(Token.LABEL_NAME, "loop");
        Node bodyNode = new Node(Token.BLOCK);
        labelNode.addChildToFront(bodyNode);
        labelNode.addChildToFront(labelNameNode);
        cg.add(labelNode);
        assertEquals("loop:{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddCastNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node castNode = new Node(Token.CAST);
        Node exprNode = Node.newNumber(5.0);
        castNode.addChildToFront(exprNode);
        cg.add(castNode);
        assertEquals("(5)", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddExprResultNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node exprResultNode = new Node(Token.EXPR_RESULT);
        Node exprNode = Node.newNumber(5.0);
        exprResultNode.addChildToFront(exprNode);
        cg.add(exprResultNode);
        assertEquals("5", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddBlockNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node blockNode = new Node(Token.BLOCK);
        Node stmtNode = new Node(Token.EXPR_RESULT);
        stmtNode.addChildToFront(Node.newNumber(1.0));
        blockNode.addChildToFront(stmtNode);
        cg.add(blockNode);
        assertEquals("1", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddScriptNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node scriptNode = new Node(Token.SCRIPT);
        Node stmtNode = new Node(Token.EXPR_RESULT);
        stmtNode.addChildToFront(Node.newNumber(1.0));
        scriptNode.addChildToFront(stmtNode);
        cg.add(scriptNode);
        assertEquals("1", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddTryCatchNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlock = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH);
        Node catchVar = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK);
        catchNode.addChildToFront(catchBody);
        catchNode.addChildToFront(catchVar);
        catchBlock.addChildToFront(catchNode);
        tryNode.addChildToFront(catchBlock);
        tryNode.addChildToFront(tryBlock);
        cg.add(tryNode);
        assertEquals("try{}catch(e){}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddTryFinallyNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlock = new Node(Token.BLOCK);
        Node finallyBlock = new Node(Token.BLOCK);
        tryNode.addChildToFront(finallyBlock);
        tryNode.addChildToFront(catchBlock);
        tryNode.addChildToFront(tryBlock);
        cg.add(tryNode);
        assertEquals("try{}finally{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddTryCatchFinallyNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlock = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH);
        Node catchVar = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK);
        catchNode.addChildToFront(catchBody);
        catchNode.addChildToFront(catchVar);
        catchBlock.addChildToFront(catchNode);
        Node finallyBlock = new Node(Token.BLOCK);
        tryNode.addChildToFront(finallyBlock);
        tryNode.addChildToFront(catchBlock);
        tryNode.addChildToFront(tryBlock);
        cg.add(tryNode);
        assertEquals("try{}catch(e){}finally{}", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddRegExpNode() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node regexpNode = new Node(Token.REGEXP);
        Node patternNode = Node.newString("test");
        Node flagsNode = Node.newString("g");
        regexpNode.addChildToFront(flagsNode);
        regexpNode.addChildToFront(patternNode);
        cg.add(regexpNode);
        assertEquals("/test/g", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddRegExpNodeNoFlags() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node regexpNode = new Node(Token.REGEXP);
        Node patternNode = Node.newString("test");
        regexpNode.addChildToFront(patternNode);
        cg.add(regexpNode);
        assertEquals("/test/", sb.toString());
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testIsSimpleNumber() {
        assertTrue(CodeGenerator.isSimpleNumber("123"));
        assertTrue(CodeGenerator.isSimpleNumber("0"));
        assertTrue(CodeGenerator.isSimpleNumber("9999999999999999"));
        assertFalse(CodeGenerator.isSimpleNumber(""));
        assertFalse(CodeGenerator.isSimpleNumber("0123")); // leading zero
        assertFalse(CodeGenerator.isSimpleNumber("12.3"));
        assertFalse(CodeGenerator.isSimpleNumber("abc"));
        assertFalse(CodeGenerator.isSimpleNumber("12a"));
        assertFalse(CodeGenerator.isSimpleNumber("-123"));
    }
    
    @Test(timeout = 4000)
    public void testGetSimpleNumber() {
        assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
        assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("0123")));
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("12.3")));
        
        // Test boundary near MAX_POSITIVE_INTEGER_NUMBER
        // This should return NaN since it's too large
        assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("99999999999999999999")));
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeNullChar() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\0");
        cg.add(stringNode);
        assertEquals("\"\\x00\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeBackspace() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\b");
        cg.add(stringNode);
        assertEquals("\"\\b\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeFormFeed() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\f");
        cg.add(stringNode);
        assertEquals("\"\\f\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeNewline() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\n");
        cg.add(stringNode);
        assertEquals("\"\\n\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeCarriageReturn() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\r");
        cg.add(stringNode);
        assertEquals("\"\\r\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeTab() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\t");
        cg.add(stringNode);
        assertEquals("\"\\t\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeVerticalTab() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u000B");
        stringNode.putBooleanProp(Node.SLASH_V, true);
        cg.add(stringNode);
        assertEquals("\"\\v\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeVerticalTabNoSlashV() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u000B");
        cg.add(stringNode);
        assertEquals("\"\\x0B\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeLineSeparator() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u2028");
        cg.add(stringNode);
        assertEquals("\"\\u2028\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeParagraphSeparator() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u2029");
        cg.add(stringNode);
        assertEquals("\"\\u2029\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeBackslash() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\\");
        cg.add(stringNode);
        assertEquals("\"\\\\\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeNonLatinCharacters() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u00E9"); // é
        cg.add(stringNode);
        assertEquals("\"\\u00e9\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeLatinCharacters() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("abc");
        cg.add(stringNode);
        assertEquals("\"abc\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeHighAscii() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u0080");
        cg.add(stringNode);
        assertEquals("\"\\u0080\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testStringEscapeDelChar() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node stringNode = Node.newString("\u007F");
        cg.add(stringNode);
        assertEquals("\"\\u007f\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testIdentifierEscape() {
        assertEquals("abc", CodeGenerator.identifierEscape("abc"));
        assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00E9"));
        assertEquals("a\\u00e9b", CodeGenerator.identifierEscape("a\u00E9b"));
    }
    
    @Test(timeout = 4000)
    public void testEscapeToDoubleQuotedJsString() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new SimpleCodeConsumer(new StringBuilder()));
        assertEquals("\"hello\"", cg.escapeToDoubleQuotedJsString("hello"));
        assertEquals("\"he\\\"llo\"", cg.escapeToDoubleQuotedJsString("he\"llo"));
        assertEquals("\"he'llo\"", cg.escapeToDoubleQuotedJsString("he'llo"));
    }
    
    @Test(timeout = 4000)
    public void testRegexpEscape() {
        CodeGenerator cg = CodeGenerator.forCostEstimation(new SimpleCodeConsumer(new StringBuilder()));
        assertEquals("/test/", cg.regexpEscape("test"));
        assertEquals("/test\\/g/", cg.regexpEscape("test/g"));
    }
    
    @Test(timeout = 4000)
    public void testAddArrayListWithEmptyTrailing() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node arrayNode = new Node(Token.ARRAYLIT);
        Node elem1 = Node.newNumber(1.0);
        Node emptyNode = new Node(Token.EMPTY);
        arrayNode.addChildToFront(emptyNode);
        arrayNode.addChildToFront(elem1);
        cg.add(arrayNode);
        assertEquals("[1,]", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddArrayListWithMultipleEmpties() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node arrayNode = new Node(Token.ARRAYLIT);
        Node empty1 = new Node(Token.EMPTY);
        Node empty2 = new Node(Token.EMPTY);
        Node elem1 = Node.newNumber(1.0);
        arrayNode.addChildToFront(empty2);
        arrayNode.addChildToFront(empty1);
        arrayNode.addChildToFront(elem1);
        cg.add(arrayNode);
        assertEquals("[1,,]", sb.toString());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Test for issue942: Object literal property names that are simple numbers
     * should NOT be quoted. The bug causes numeric property names like "0" to be
     * output as ["0"] instead of [0].
     */
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitNumericPropertyName() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // Create object literal with numeric property name "0"
        Node objectNode = new Node(Token.OBJECTLIT);
        Node keyNode = Node.newString(Token.STRING_KEY, "0");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        // The expected output should be {0:1} not {["0"]:1}
        // This is the bug from issue942
        String result = sb.toString();
        assertEquals("Object literal with numeric property name should use unquoted number", 
                     "{0:1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitMultipleNumericPropertyNames() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        Node key1 = Node.newString(Token.STRING_KEY, "0");
        key1.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(key1);
        
        Node key2 = Node.newString(Token.STRING_KEY, "1");
        key2.addChildToFront(Node.newNumber(2.0));
        objectNode.addChildToFront(key2);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with multiple numeric property names", 
                     "{0:1,1:2}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitMixedPropertyNames() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // Numeric property name
        Node key1 = Node.newString(Token.STRING_KEY, "0");
        key1.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(key1);
        
        // String property name
        Node key2 = Node.newString(Token.STRING_KEY, "foo");
        key2.addChildToFront(Node.newNumber(2.0));
        objectNode.addChildToFront(key2);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with mixed property names", 
                     "{0:1,foo:2}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitNumericPropertyNameWithLeadingZero() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // "01" is not a simple number (leading zero), so it should be quoted
        Node keyNode = Node.newString(Token.STRING_KEY, "01");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with leading zero property name should be quoted", 
                     "{\"01\":1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitNumericPropertyNameLargeNumber() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // Large number that exceeds MAX_POSITIVE_INTEGER_NUMBER should be quoted
        Node keyNode = Node.newString(Token.STRING_KEY, "99999999999999999999");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with large numeric property name should be quoted", 
                     "{\"99999999999999999999\":1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitNonNumericStringProperty() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // Non-numeric string property should be quoted if it's not a valid identifier
        Node keyNode = Node.newString(Token.STRING_KEY, "foo-bar");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with non-identifier property name should be quoted", 
                     "{\"foo-bar\":1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitKeywordProperty() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // JavaScript keyword should be quoted
        Node keyNode = Node.newString(Token.STRING_KEY, "if");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with keyword property name should be quoted", 
                     "{\"if\":1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitQuotedStringProperty() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // Quoted string property should always be quoted
        Node keyNode = Node.newString(Token.STRING_KEY, "foo");
        keyNode.setQuotedString();
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with quoted string property should be quoted", 
                     "{\"foo\":1}", result);
    }
    
    @Test(timeout = 4000)
    public void testIssue942_ObjectLitNonLatinProperty() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node objectNode = new Node(Token.OBJECTLIT);
        
        // Non-Latin property name should be quoted
        Node keyNode = Node.newString(Token.STRING_KEY, "\u00E9");
        keyNode.addChildToFront(Node.newNumber(1.0));
        objectNode.addChildToFront(keyNode);
        
        cg.add(objectNode);
        
        String result = sb.toString();
        assertEquals("Object literal with non-Latin property name should be quoted", 
                     "{\"\\u00e9\":1}", result);
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddInvalidBinaryOperator() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // Create a binary operator node with wrong number of children
        Node addNode = new Node(Token.ADD);
        Node leftNode = Node.newNumber(1.0);
        addNode.addChildToFront(leftNode);
        // Only one child, should throw
        cg.add(addNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddUnknownTokenType() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // Create a node with an unknown token type
        Node unknownNode = new Node(9999);
        cg.add(unknownNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddRegExpWithNonStringChildren() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node regexpNode = new Node(Token.REGEXP);
        Node nonStringNode = Node.newNumber(1.0);
        regexpNode.addChildToFront(nonStringNode);
        cg.add(regexpNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddFunctionWithNonNodeClass() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // This would require a subclass of Node, which is hard to create
        // Just verify the precondition check works
        Node functionNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "f");
        Node paramNode = new Node(Token.PARAM_LIST);
        Node bodyNode = new Node(Token.BLOCK);
        functionNode.addChildToFront(bodyNode);
        functionNode.addChildToFront(paramNode);
        functionNode.addChildToFront(nameNode);
        cg.add(functionNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddBreakWithNonLabelChild() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node breakNode = new Node(Token.BREAK);
        Node nonLabelNode = Node.newNumber(1.0);
        breakNode.addChildToFront(nonLabelNode);
        cg.add(breakNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddContinueWithNonLabelChild() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node continueNode = new Node(Token.CONTINUE);
        Node nonLabelNode = Node.newNumber(1.0);
        continueNode.addChildToFront(nonLabelNode);
        cg.add(continueNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddLabelWithNonLabelNameChild() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node labelNode = new Node(Token.LABEL);
        Node nonLabelNameNode = Node.newNumber(1.0);
        Node bodyNode = new Node(Token.BLOCK);
        labelNode.addChildToFront(bodyNode);
        labelNode.addChildToFront(nonLabelNameNode);
        cg.add(labelNode);
    }
    
    @Test(expected = Error.class, timeout = 4000)
    public void testAddNonEmptyStatementWithNonBlockChild() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // This should trigger the error in addNonEmptyStatement
        Node ifNode = new Node(Token.IF);
        Node condNode = Node.newString(Token.NAME, "a");
        Node nonBlockNode = Node.newNumber(1.0);
        ifNode.addChildToFront(nonBlockNode);
        ifNode.addChildToFront(condNode);
        cg.add(ifNode);
    }
    
    @Test(timeout = 4000)
    public void testAddWithContinueProcessingFalse() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new StoppingCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node numberNode = Node.newNumber(42.0);
        cg.add(numberNode);
        // Should not add anything because continueProcessing returns false
        assertEquals("", sb.toString());
    }
    
    // ==================== Partition E: Object Lifecycle & Contract ====================
    
    @Test(timeout = 4000)
    public void testForCostEstimation() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        assertNotNull(cg);
        cg.add("test");
        assertEquals("test", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithOptions() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CompilerOptions options = new CompilerOptions();
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        assertNotNull(cg);
        cg.add("test");
        assertEquals("test", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithOptionsPreferSingleQuotes() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CompilerOptions options = new CompilerOptions();
        options.preferSingleQuotes = true;
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        // String with more double quotes should use single quotes
        Node stringNode = Node.newString("he\"\"llo");
        cg.add(stringNode);
        assertEquals("'he\"\"llo'", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithOptionsTrustedStringsFalse() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CompilerOptions options = new CompilerOptions();
        options.trustedStrings = false;
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        // With trustedStrings=false, < and > should be escaped
        Node stringNode = Node.newString("<script>");
        cg.add(stringNode);
        assertEquals("\"\\x3cscript\\x3e\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithOptionsCharsetEncoder() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CompilerOptions options = new CompilerOptions();
        options.setOutputCharset(java.nio.charset.Charset.forName("ISO-8859-1"));
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        // Characters encodable in ISO-8859-1 should pass through
        Node stringNode = Node.newString("hello");
        cg.add(stringNode);
        assertEquals("\"hello\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithOptionsCharsetEncoderNonEncodable() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CompilerOptions options = new CompilerOptions();
        options.setOutputCharset(java.nio.charset.Charset.forName("US-ASCII"));
        
        CodeGenerator cg = new CodeGenerator(consumer, options);
        
        // Non-ASCII characters should be escaped
        Node stringNode = Node.newString("\u00E9");
        cg.add(stringNode);
        assertEquals("\"\\u00e9\"", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddListMethod() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node firstNode = Node.newNumber(1.0);
        Node secondNode = Node.newNumber(2.0);
        firstNode.setNext(secondNode);
        
        cg.addList(firstNode);
        assertEquals("1,2", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddListWithIsArrayArgument() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node firstNode = Node.newNumber(1.0);
        Node secondNode = Node.newNumber(2.0);
        firstNode.setNext(secondNode);
        
        cg.addList(firstNode, true);
        assertEquals("1,2", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddAllSiblings() {
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        Node firstNode = Node.newNumber(1.0);
        Node secondNode = Node.newNumber(2.0);
        firstNode.setNext(secondNode);
        
        cg.addAllSiblings(firstNode);
        assertEquals("12", sb.toString());
    }
    
    @Test(timeout = 4000)
    public void testGetContextForNoInOperator() {
        // This is a private method, but we can test it indirectly through code generation
        StringBuilder sb = new StringBuilder();
        CodeConsumer consumer = new SimpleCodeConsumer(sb);
        CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
        
        // Create a for-in loop which uses IN_FOR_INIT_CLAUSE context
        Node forNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node valueNode = Node.newNumber(10.0);
        nameNode.addChildToFront(valueNode);
        varNode.addChildToFront(nameNode);
        Node objNode = Node.newString(Token.NAME, "obj");
        Node bodyNode = new Node(Token.BLOCK);
        forNode.addChildToFront(bodyNode);
        forNode.addChildToFront(objNode);
        forNode.addChildToFront(varNode);
        
        cg.add(forNode);
        assertEquals("for(var x=10in obj){}", sb.toString());
    }
    
    // ==================== Helper CodeConsumer Implementations ====================
    
    /**
     * Simple CodeConsumer that appends to a StringBuilder.
     */
    private static class SimpleCodeConsumer implements CodeConsumer {
        private final StringBuilder sb;
        
        SimpleCodeConsumer(StringBuilder sb) {
            this.sb = sb;
        }
        
        @Override
        public void add(String str) {
            sb.append(str);
        }
        
        @Override
        public void addIdentifier(String identifier) {
            sb.append(identifier);
        }
        
        @Override
        public void addOp(String op, boolean binOp) {
            sb.append(op);
        }
        
        @Override
        public void addNumber(double x) {
            if (x == (long) x) {
                sb.append(String.valueOf((long) x));
            } else {
                sb.append(String.valueOf(x));
            }
        }
        
        @Override
        public void addConstant(String constant) {
            sb.append(constant);
        }
        
        @Override
        public void startSourceMapping(Node node) {
            // No-op
        }
        
        @Override
        public void endSourceMapping(Node node) {
            // No-op
        }
        
        @Override
        public void beginBlock() {
            sb.append("{");
        }
        
        @Override
        public void endBlock(boolean breakAfter) {
            sb.append("}");
        }
        
        @Override
        public void listSeparator() {
            sb.append(",");
        }
        
        @Override
        public void endStatement(boolean needSemicolon) {
            if (needSemicolon) {
                sb.append(";");
            }
        }
        
        @Override
        public void endStatement() {
            sb.append(";");
        }
        
        @Override
        public void endFunction(boolean statementContext) {
            // No-op
        }
        
        @Override
        public void beginCaseBody() {
            // No-op
        }
        
        @Override
        public void endCaseBody() {
            // No-op
        }
        
        @Override
        public void maybeLineBreak() {
            // No-op
        }
        
        @Override
        public void notePreferredLineBreak() {
            // No-op
        }
        
        @Override
        public boolean breakAfterBlockFor(Node block, boolean isStatementContext) {
            return false;
        }
        
        @Override
        public boolean continueProcessing() {
            return true;
        }
        
        @Override
        public boolean shouldPreserveExtraBlocks() {
            return false;
        }
    }
    
    /**
     * CodeConsumer that stops processing after first call.
     */
    private static class StoppingCodeConsumer implements CodeConsumer {
        private final StringBuilder sb;
        private boolean stopped = false;
        
        StoppingCodeConsumer(StringBuilder sb) {
            this.sb = sb;
        }
        
        @Override
        public void add(String str) {
            if (!stopped) {
                sb.append(str);
            }
        }
        
        @Override
        public void addIdentifier(String identifier) {
            if (!stopped) {
                sb.append(identifier);
            }
        }
        
        @Override
        public void addOp(String op, boolean binOp) {
            if (!stopped) {
                sb.append(op);
            }
        }
        
        @Override
        public void addNumber(double x) {
            if (!stopped) {
                if (x == (long) x) {
                    sb.append(String.valueOf((long) x));
                } else {
                    sb.append(String.valueOf(x));
                }
            }
        }
        
        @Override
        public void addConstant(String constant) {
            if (!stopped) {
                sb.append(constant);
            }
        }
        
        @Override
        public void startSourceMapping(Node node) {}
        
        @Override
        public void endSourceMapping(Node node) {}
        
        @Override
        public void beginBlock() {
            if (!stopped) sb.append("{");
        }
        
        @Override
        public void endBlock(boolean breakAfter) {
            if (!stopped) sb.append("}");
        }
        
        @Override
        public void listSeparator() {
            if (!stopped) sb.append(",");
        }
        
        @Override
        public void endStatement(boolean needSemicolon) {
            if (!stopped && needSemicolon) sb.append(";");
        }
        
        @Override
        public void endStatement() {
            if (!stopped) sb.append(";");
        }
        
        @Override
        public void endFunction(boolean statementContext) {}
        
        @Override
        public void beginCaseBody() {}
        
        @Override
        public void endCaseBody() {}
        
        @Override
        public void maybeLineBreak() {}
        
        @Override
        public void notePreferredLineBreak() {}
        
        @Override
        public boolean breakAfterBlockFor(Node block, boolean isStatementContext) {
            return false;
        }
        
        @Override
        public boolean continueProcessing() {
            if (!stopped) {
                stopped = true;
                return true;
            }
            return false;
        }
        
        @Override
        public boolean shouldPreserveExtraBlocks() {
            return false;
        }
    }
}
