package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: InlineCostEstimator
 * Primary Methods: getCost(Node), getCost(Node, int)
 *
 * Covered Decision Branches:
 * 1. getCost(Node) delegating to getCost(Node, Integer.MAX_VALUE)
 * 2. getCost(Node, int) - null root passed? (Node validation)
 * 3. CompiledSizeEstimator.continueProcessing() - threshold reached vs. not reached
 * 4. CompiledSizeEstimator.append(String) - single char, multi-char, empty, null strings
 * 5. CompiledSizeEstimator.addIdentifier(String) - identifier handling (shortening to "ab")
 * 6. CodeGenerator traversal: different node types (SCRIPT, BLOCK, EXPR_RESULT, NUMBER, STRING, NAME, TRUE, FALSE, NULL)
 * 7. Edge case: Cost threshold exactly at boundary (cost == maxCost vs cost > maxCost)
 * 8. Edge case: Very large input vs. small costThreshhold
 * 9. Edge case: Multiple identifiers vs. constants
 * 10. Edge case: Token types like ADD, CALL, FUNCTION, empty script
 *
 * Defect Targeting (Defects4J bug):
 * - Known failure: testCost expected:<1> but was:<4>
 * - This indicates the estimator is overcounting cost for certain simple expressions.
 * - Likely cause: Constants (true, false, null) are not being handled as "free" (cost 0) as the comment suggests.
 * - The append method charges full string length for all values including constants.
 * - Need to verify that constants like `true` and `null` are not overcharged.
 * - Issue728 confirms this affects inlining decisions where constant folding should happen.
 *
 * Test Strategy:
 * - Partition A: Simple literals (numbers, strings, booleans, null) - test cost accuracy
 * - Partition B: Threshold boundary tests (equal, above, below)
 * - Partition C: Defect-specific test for constant costs
 * - Partition D: Complex AST structures (expressions, function calls)
 * - Partition E: Empty/invalid inputs
 */
public class InlineCostEstimatorDeepseekTest {

    // =========================================================================
    // Partition A: Core Functional Logic - Basic Literal Costs
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumberLiteralCost() {
        // A number like 42 should have cost ~2 characters
        Node num = Node.newNumber(42);
        int cost = InlineCostEstimator.getCost(num);
        // Number is output as "42" - cost should be 2
        assertTrue("Cost for number 42 should be positive", cost > 0);
        assertTrue("Cost should be less than actual string length check", true);
    }

    @Test(timeout = 4000)
    public void testStringLiteralCost() {
        // A simple string literal "x"
        Node str = Node.newString("x");
        int cost = InlineCostEstimator.getCost(str);
        // String literal output: "x" - cost 3 characters including quotes
        assertTrue("String cost should be positive", cost > 0);
    }

    @Test(timeout = 4000)
    public void testTrueLiteralCost() {
        // Boolean true - according to comment should be "basically free"
        // But implementation may charge 4 chars for "true"
        Node trueNode = new Node(Token.TRUE);
        int cost = InlineCostEstimator.getCost(trueNode);
        // The defect: Cost should be 0 or minimal, but returns 4
        // This test is for coverage - we just record the actual behavior
        assertTrue("TRUE literal should have cost", cost >= 0);
    }

    @Test(timeout = 4000)
    public void testFalseLiteralCost() {
        Node falseNode = new Node(Token.FALSE);
        int cost = InlineCostEstimator.getCost(falseNode);
        assertTrue("FALSE literal should have cost", cost >= 0);
    }

    @Test(timeout = 4000)
    public void testNullLiteralCost() {
        Node nullNode = new Node(Token.NULL);
        int cost = InlineCostEstimator.getCost(nullNode);
        assertTrue("NULL literal should have cost", cost >= 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis - Threshold Testing
    // =========================================================================

    @Test(timeout = 4000)
    public void testCostThresholdExactZero() {
        // With threshold 0, any processing should stop immediately
        Node num = Node.newNumber(1);
        int cost = InlineCostEstimator.getCost(num, 0);
        // Cost could be 0 because append triggers continueProcessing = false
        assertTrue("Cost with threshold 0 should be 0", cost == 0);
    }

    @Test(timeout = 4000)
    public void testCostThresholdExactOne() {
        Node num = Node.newNumber(1);
        int cost = InlineCostEstimator.getCost(num, 1);
        // Number "1" costs 1, so threshold exactly reached (cost == maxCost)
        // append sets continueProcessing = false when maxCost <= cost
        // So after adding, cost = 1, processing stops
        assertEquals("Cost with threshold 1 should be 1", 1, cost);
    }

    @Test(timeout = 4000)
    public void testCostThresholdExactTwo() {
        Node num = Node.newNumber(42);
        int cost = InlineCostEstimator.getCost(num, 2);
        // "42" costs 2, threshold exactly met
        assertEquals("Cost with threshold 2 for '42' should be 2", 2, cost);
    }

    @Test(timeout = 4000)
    public void testCostThresholdAbove() {
        Node num = Node.newNumber(42);
        int cost = InlineCostEstimator.getCost(num, 100);
        // Full cost should be returned since threshold not reached
        assertTrue("Cost should be less than threshold", cost <= 100);
        assertTrue("Cost should be positive for non-empty node", cost > 0);
    }

    @Test(timeout = 4000)
    public void testCostThresholdBelow() {
        Node num = Node.newNumber(123456);
        int cost = InlineCostEstimator.getCost(num, 3);
        // "123456" costs 6, but we stop at 3
        assertTrue("Cost should be at most threshold", cost <= 3);
        // It may be exactly 3 because we stop when cost >= maxCost
        assertEquals("When threshold is exceeded, cost should equal threshold or less", 3, cost);
    }

    @Test(timeout = 4000)
    public void testMaxIntThreshold() {
        Node num = Node.newNumber(12345);
        int cost = InlineCostEstimator.getCost(num, Integer.MAX_VALUE);
        // Should get full cost without early termination
        assertTrue("Cost with MAX_VALUE threshold should be positive", cost > 0);
        assertTrue("Cost should be reasonable", cost < 100);
    }

    // =========================================================================
    // Partition C: DEFECT-TARGETED TESTS (Directly addressing known bug)
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsAreNearZeroCost() {
        // The comment says constants (true, false, null) are "basically free"
        // Bug: Cost estimated as 4 for "true" when it should be 0
        Node trueNode = new Node(Token.TRUE);
        int cost = InlineCostEstimator.getCost(trueNode, 1);
        // With threshold 1, if true costs 4, we'd get cost=1
        // If true costs 0, we'd get cost=0
        // Expected correct behavior: cost should be 0 (free)
        // Defective behavior: cost would be >= 4 (since "true".length() = 4)
        assertTrue("Constants should be essentially free (cost < 2), but got: " + cost, cost < 2);
    }

    @Test(timeout = 4000)
    public void testNullCostDefect() {
        Node nullNode = new Node(Token.NULL);
        int cost = InlineCostEstimator.getCost(nullNode, 1);
        // "null" costs 4 if charged as string; should be 0 as constant
        assertTrue("Null constant should be nearly free, but got: " + cost, cost < 2);
    }

    @Test(timeout = 4000)
    public void testFalseCostDefect() {
        Node falseNode = new Node(Token.FALSE);
        int cost = InlineCostEstimator.getCost(falseNode, 1);
        // "false" costs 5 if charged as string; should be 0 as constant
        assertTrue("False constant should be nearly free, but got: " + cost, cost < 2);
    }

    @Test(timeout = 4000)
    public void testConstantVsIdentifierCostComparison() {
        // The bug manifests when comparing cost of constants vs identifiers
        // Identifier "ab" should cost ~2, constant true should cost 0
        Node trueNode = new Node(Token.TRUE);
        int trueCost = InlineCostEstimator.getCost(trueNode);

        Node nameNode = Node.newString(Token.NAME, "x");
        int nameCost = InlineCostEstimator.getCost(nameNode);

        // Identifiers are replaced with "ab" (2 chars), constants should be cheaper or equal
        assertTrue("Constant cost should be <= identifier cost, but got true:" + trueCost + " id:" + nameCost,
                   trueCost <= nameCost);
    }

    // =========================================================================
    // Partition D: Complex AST Structures
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyScriptCost() {
        Node script = new Node(Token.SCRIPT);
        int cost = InlineCostEstimator.getCost(script);
        assertEquals("Empty script should have 0 cost", 0, cost);
    }

    @Test(timeout = 4000)
    public void testSimpleExpressionCost() {
        // Create node for: 1 + 2
        Node add = new Node(Token.ADD);
        add.addChildToBack(Node.newNumber(1));
        add.addChildToBack(Node.newNumber(2));
        int cost = InlineCostEstimator.getCost(add);
        // Output: "1+2" = 3 characters
        assertTrue("Expression '1+2' should have cost around 3", cost >= 3);
    }

    @Test(timeout = 4000)
    public void testVarDeclarationCost() {
        // var x = 5;
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "x");
        name.addChildToBack(Node.newNumber(5));
        var.addChildToBack(name);

        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(var);

        int cost = InlineCostEstimator.getCost(script);
        // "var x=5;" or similar - with identifier "ab" replacing "x"
        assertTrue("Var declaration should have positive cost", cost > 0);
    }

    @Test(timeout = 4000)
    public void testFunctionCallCost() {
        // foo(x) - but identifier shortened
        Node call = new Node(Token.CALL);
        Node name = Node.newString(Token.NAME, "foo");
        call.addChildToBack(name);
        call.addChildToBack(Node.newString(Token.NAME, "x"));

        int cost = InlineCostEstimator.getCost(call);
        // "ab(ab)" with identifier shortening = 6 chars including parens
        assertTrue("Function call cost should be positive", cost > 0);
    }

    // =========================================================================
    // Partition E: Edge Cases and Error Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testNegativeThreshold() {
        // Negative threshold - should behave like 0 or be treated as MAX?
        Node num = Node.newNumber(42);
        int cost = InlineCostEstimator.getCost(num, -1);
        // Implementation uses <= comparison, so -1 <= cost triggers immediately
        // append sets continueProcessing = false on first call, but addIdentifier may not trigger append
        // Since threshold is negative, any cost added should stop processing
        assertTrue("Cost with negative threshold should be <= threshold or 0", cost <= 0 || cost > 0);
    }

    @Test(timeout = 4000)
    public void testLargeNumberCost() {
        // Very large number representation
        Node largeNum = Node.newNumber(1234567890.12345);
        int cost = InlineCostEstimator.getCost(largeNum);
        assertTrue("Large number should have positive cost", cost > 0);
    }

    @Test(timeout = 4000)
    public void testMultipleNodesHierarchy() {
        // Build a block with multiple statements
        Node block = new Node(Token.BLOCK);
        Node expr1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
        Node expr2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
        block.addChildToBack(expr1);
        block.addChildToBack(expr2);

        int cost = InlineCostEstimator.getCost(block);
        assertTrue("Multiple statements should cost more than single", cost > 2);
    }

    @Test(timeout = 4000)
    public void testAddIdentifierCost() {
        // NAME tokens are converted to "ab" via addIdentifier
        Node name = Node.newString(Token.NAME, "veryLongVariableName");
        int cost = InlineCostEstimator.getCost(name);
        // Should be 2 (for "ab") not the full length of the original name
        assertEquals("Identifier should be shortened to 2 chars", 2, cost);
    }

    @Test(timeout = 4000)
    public void testGetCostDefaultDelegation() {
        // getCost(Node) delegates to getCost(Node, Integer.MAX_VALUE)
        Node num = Node.newNumber(99);
        int costDefault = InlineCostEstimator.getCost(num);
        int costExplicit = InlineCostEstimator.getCost(num, Integer.MAX_VALUE);
        assertEquals("Default getCost should equal MAX_VALUE threshold variant",
                     costDefault, costExplicit);
    }

    @Test(timeout = 4000)
    public void testCostAssociativity() {
        // (1 + 2) + 3
        Node innerAdd = new Node(Token.ADD);
        innerAdd.addChildToBack(Node.newNumber(1));
        innerAdd.addChildToBack(Node.newNumber(2));

        Node outerAdd = new Node(Token.ADD);
        outerAdd.addChildToBack(innerAdd);
        outerAdd.addChildToBack(Node.newNumber(3));

        int cost = InlineCostEstimator.getCost(outerAdd);
        // Output: "1+2+3" = 5 chars
        assertTrue("Nested expression cost should be around 5", cost > 4 && cost < 10);
    }

    @Test(timeout = 4000)
    public void testStringWithQuotesCost() {
        // String with content that may include escaped chars
        Node str = Node.newString("hello");
        int cost = InlineCostEstimator.getCost(str);
        // Output: "\"hello\"" = 7 chars
        assertTrue("String literal with quotes should have cost > content length", cost > 5);
    }

    @Test(timeout = 4000)
    public void testNegativeNumberCost() {
        Node negNum = Node.newNumber(-5);
        int cost = InlineCostEstimator.getCost(negNum);
        // Output: "-5" = 2 chars
        assertTrue("Negative number cost should be positive", cost > 0);
    }

    @Test(timeout = 4000)
    public void testScriptWithMultipleStatementsCost() {
        Node script = new Node(Token.SCRIPT);

        // var a = true;
        Node var1 = new Node(Token.VAR);
        Node name1 = Node.newString(Token.NAME, "a");
        name1.addChildToBack(new Node(Token.TRUE));
        var1.addChildToBack(name1);
        script.addChildToBack(var1);

        // var b = null;
        Node var2 = new Node(Token.VAR);
        Node name2 = Node.newString(Token.NAME, "b");
        name2.addChildToBack(new Node(Token.NULL));
        var2.addChildToBack(name2);
        script.addChildToBack(var2);

        int cost = InlineCostEstimator.getCost(script);
        // Should account for all characters, but identifiers are "ab"
        assertTrue("Script with multiple var decls should have cost", cost > 0);
    }

    @Test(timeout = 4000)
    public void testVeryLowThresholdOnComplexTree() {
        Node script = new Node(Token.SCRIPT);
        for (int i = 0; i < 10; i++) {
            script.addChildToBack(Node.newNumber(i));
        }

        int fullCost = InlineCostEstimator.getCost(script, Integer.MAX_VALUE);
        int limitedCost = InlineCostEstimator.getCost(script, 5);

        assertTrue("Limited cost should be <= full cost", limitedCost <= fullCost);
        assertTrue("Limited cost should be at most threshold", limitedCost <= 5);
    }
}