package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

/**
 * Advanced White-Box Test Suite for {@link InlineCostEstimator}.
 *
 * /* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.javascript.jscomp.InlineCostEstimator
 *
 * Decision / Condition Matrix:
 * 1. CompiledSizeEstimator#append(String str):
 *    - Branch: if (maxCost <= cost) -> continueProcessing = false
 *      * True path: cost reaches or exceeds maxCost (early exit signaled to CodeGenerator)
 *      * False path: cost remains strictly below maxCost (continue processing AST)
 *    - Character state: updates 'last' with str.charAt(str.length() - 1)
 *
 * 2. CompiledSizeEstimator#addIdentifier(String identifier):
 *    - Replaces identifier name with ESTIMATED_IDENTIFIER ("ab", length = 2)
 *
 * 3. CompiledSizeEstimator#addConstant(String newcode) [KNOWN DEFECT: Closure Issue 728]:
 *    - Defective version: Fails to override addConstant, falling back to CodeConsumer#addConstant(String)
 *      which appends the full literal text ("true" -> 4, "false" -> 5, "null" -> 4, "this" -> 4).
 *    - Correct version: Overrides addConstant to append a 1-character constant ("a"), yielding cost 1.
 *
 * 4. Boundary & Extremes Matrix:
 *    - Node root: null root AST handling (returns 0 cost)
 *    - Threshold boundaries: Integer.MAX_VALUE, large values, small values (0, 1, 2, 3), and negative (-1)
 *    - Constant cost integrity: ESTIMATED_IDENTIFIER_COST == 2
 *    - Private constructor reflective instantiation
 * ====================================================================================================
 */
public class InlineCostEstimatorGeminiTest {

  private Node parse(String js) {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode(js);
    assertEquals("Parser encountered unexpected errors for: " + js, 0, compiler.getErrorCount());
    return script.getFirstChild();
  }

  private Node parseExpr(String js) {
    Node stmt = parse(js);
    assertNotNull("Parsed statement node should not be null", stmt);
    return stmt.getFirstChild();
  }

  // ==================================================================================================
  // Partition A: Core Functional Logic & Identifiers
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testIdentifierCostEstimation() {
    Node nameNode = parseExpr("longVariableName");
    int cost = InlineCostEstimator.getCost(nameNode);
    assertEquals("Identifier should be estimated using ESTIMATED_IDENTIFIER_COST (2)",
        InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, cost);
    assertEquals(2, cost);
  }

  @Test(timeout = 4000)
  public void testNumberLiteralCost() {
    Node singleDigit = parseExpr("1");
    assertEquals(1, InlineCostEstimator.getCost(singleDigit));

    Node multiDigit = parseExpr("12345");
    assertEquals(5, InlineCostEstimator.getCost(multiDigit));
  }

  @Test(timeout = 4000)
  public void testStringLiteralCost() {
    Node strNode = parseExpr("'xyz'");
    // Single quoted string 'xyz' -> length is 5 (including quotes)
    assertEquals(5, InlineCostEstimator.getCost(strNode));
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorAndAssignmentCost() {
    // "a + b" -> "ab" + "+" + "ab" = 5 chars
    Node addExpr = parseExpr("a + b");
    assertEquals(5, InlineCostEstimator.getCost(addExpr));

    // "x = 1" -> "ab" + "=" + "1" = 4 chars
    Node assignExpr = parseExpr("x = 1");
    assertEquals(4, InlineCostEstimator.getCost(assignExpr));
  }

  @Test(timeout = 4000)
  public void testCallExpressionCost() {
    // "f()" -> "ab()" = 4 chars
    Node callExpr = parseExpr("f()");
    assertEquals(4, InlineCostEstimator.getCost(callExpr));

    // "f(1, 2)" -> "ab(1,2)" = 7 chars
    Node callWithArgs = parseExpr("f(1, 2)");
    assertEquals(7, InlineCostEstimator.getCost(callWithArgs));
  }

  @Test(timeout = 4000)
  public void testStatementCost() {
    // Statement "var x = 1;"
    Node varStmt = parse("var x = 1;");
    int cost = InlineCostEstimator.getCost(varStmt);
    assertTrue("Statement cost must be positive", cost > 0);
  }

  // ==================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Threshold Branches
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testNullRootNodeReturnsZeroCost() {
    assertEquals(0, InlineCostEstimator.getCost(null));
    assertEquals(0, InlineCostEstimator.getCost(null, 10));
  }

  @Test(timeout = 4000)
  public void testCostThresholdEarlyExitBranch() {
    Node expr = parseExpr("1 + 2 + 3 + 4 + 5");
    int fullCost = InlineCostEstimator.getCost(expr);
    assertTrue("Full cost should exceed threshold", fullCost > 3);

    int thresholdCost = InlineCostEstimator.getCost(expr, 3);
    assertTrue("Thresholding must halt generation early", thresholdCost < fullCost);
    assertTrue("Thresholded cost should reach or slightly exceed threshold boundary", thresholdCost >= 3);
  }

  @Test(timeout = 4000)
  public void testZeroCostThreshold() {
    Node expr = parseExpr("1 + 2 + 3");
    int cost = InlineCostEstimator.getCost(expr, 0);
    assertTrue("Zero threshold should stop after the first token", cost >= 1);
    assertTrue("Zero threshold should be strictly smaller than full cost",
        cost < InlineCostEstimator.getCost(expr));
  }

  @Test(timeout = 4000)
  public void testNegativeCostThreshold() {
    Node expr = parseExpr("a + b");
    int cost = InlineCostEstimator.getCost(expr, -1);
    assertTrue("Negative threshold should immediately halt after first append", cost > 0);
    assertTrue(cost <= InlineCostEstimator.getCost(expr));
  }

  @Test(timeout = 4000)
  public void testMaxIntegerCostThresholdMatchesDefault() {
    Node expr = parseExpr("a + b + c");
    int defaultCost = InlineCostEstimator.getCost(expr);
    int maxThreshCost = InlineCostEstimator.getCost(expr, Integer.MAX_VALUE);
    assertEquals("Cost with Integer.MAX_VALUE threshold must equal default getCost", defaultCost, maxThreshCost);
  }

  @Test(timeout = 4000)
  public void testEmptyStringLiteralCost() {
    Node emptyStr = parseExpr("''");
    // '' -> 2 characters (the quotes)
    assertEquals(2, InlineCostEstimator.getCost(emptyStr));
  }

  // ==================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defect / Issue 728)
  // ==================================================================================================

  /**
   * Targets Defects4J bug where constants (true, false, null, this) were not treated as size 1
   * because {@code CompiledSizeEstimator} omitted {@code addConstant(String)}.
   * Expected: size 1 for each constant.
   * Defective: size 4 ("true"), 5 ("false"), 4 ("null"), 4 ("this").
   */
  @Test(timeout = 4000)
  public void testCostOfConstants_RevealsIssue728Defect() {
    Node trueNode = parseExpr("true");
    assertEquals("Cost of 'true' must be estimated as 1", 1, InlineCostEstimator.getCost(trueNode));

    Node falseNode = parseExpr("false");
    assertEquals("Cost of 'false' must be estimated as 1", 1, InlineCostEstimator.getCost(falseNode));

    Node nullNode = parseExpr("null");
    assertEquals("Cost of 'null' must be estimated as 1", 1, InlineCostEstimator.getCost(nullNode));

    Node thisNode = parseExpr("this");
    assertEquals("Cost of 'this' must be estimated as 1", 1, InlineCostEstimator.getCost(thisNode));
  }

  @Test(timeout = 4000)
  public void testFunctionReturningConstantCost() {
    // Inlining decision hinges on cost of functions returning folded constants:
    // function() { return true; }
    Node fnTrue = parseExpr("(function(){return true;})");
    Node fnA = parseExpr("(function(){return a;})");

    int costTrue = InlineCostEstimator.getCost(fnTrue);
    int costA = InlineCostEstimator.getCost(fnA);

    // Identifier 'a' costs 2 ("ab"), while constant 'true' should cost 1.
    // Therefore, returning 'true' should be cheaper than returning 'a'.
    assertTrue("Function returning 'true' (cost 1) must be strictly cheaper than returning 'a' (cost 2)",
        costTrue < costA);
  }

  // ==================================================================================================
  // Partition D: Object Lifecycle & Contract Integrity
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorReflectiveInstantiation() throws Exception {
    Constructor<InlineCostEstimator> constructor = InlineCostEstimator.class.getDeclaredConstructor();
    assertFalse("InlineCostEstimator constructor should be private", constructor.isAccessible());
    constructor.setAccessible(true);
    InlineCostEstimator instance = constructor.newInstance();
    assertNotNull("Instantiated object should not be null", instance);
  }

  @Test(timeout = 4000)
  public void testEstimatedIdentifierCostConstant() {
    assertEquals("ESTIMATED_IDENTIFIER_COST must be exactly 2",
        2, InlineCostEstimator.ESTIMATED_IDENTIFIER_COST);
  }
}