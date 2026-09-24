package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Comprehensive White-Box JUnit 4 test suite for NodeUtil.
 *
 * [Branch & Defect Analysis Matrix]
 * - Covers all public/package-private static methods in NodeUtil.
 * - Targets high line/branch coverage with equivalence partitioning & BVA.
 * - Dedicated defect-targeted tests for isValidDefineValue to expose the
 *   known false-negative on string literals and other valid define initializers.
 * - Partitions: A (Core functional), B (Boundary & extremes), C (Defect zone),
 *   D (Exception paths), E (Lifecycle & contract integrity).
 */
public class NodeUtilDeepseekTest {

  // ==================== Helper Methods ====================

  private static Node createNode(int type) {
    return new Node(type);
  }

  private static Node createNode(int type, String str) {
    Node n = new Node(type);
    n.setString(str);
    return n;
  }

  private static Node createNumberNode(double value) {
    Node n = new Node(Token.NUMBER);
    n.setDouble(value);
    return n;
  }

  private static Node createNameNode(String name) {
    return createNode(Token.NAME, name);
  }

  private static Node createStringNode(String str) {
    return createNode(Token.STRING, str);
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    // True cases
    assertTrue(NodeUtil.isImmutableValue(createStringNode("x")));
    assertTrue(NodeUtil.isImmutableValue(createNumberNode(3.14)));
    assertTrue(NodeUtil.isImmutableValue(createNode(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(createNode(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(createNode(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(createNode(Token.VOID)));
    Node neg = new Node(Token.NEG, createNumberNode(1));
    assertTrue(NodeUtil.isImmutableValue(neg));
    assertTrue(NodeUtil.isImmutableValue(createNameNode("undefined")));
    assertTrue(NodeUtil.isImmutableValue(createNameNode("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(createNameNode("NaN")));
    // False cases
    assertFalse(NodeUtil.isImmutableValue(createNameNode("a")));
    assertFalse(NodeUtil.isImmutableValue(createNode(Token.ARRAYLIT)));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ADD, createNumberNode(1), createNumberNode(2))));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue() {
    // ARRAYLIT with no children is literal
    Node arrLit = createNode(Token.ARRAYLIT);
    assertTrue(NodeUtil.isLiteralValue(arrLit, false));

    // ARRAYLIT with immutable child
    Node arrLitWithChild = new Node(Token.ARRAYLIT, createNumberNode(1));
    assertTrue(NodeUtil.isLiteralValue(arrLitWithChild, false));

    // ARRAYLIT with non-literal child (NAME)
    Node arrLitWithName = new Node(Token.ARRAYLIT, createNameNode("x"));
    assertFalse(NodeUtil.isLiteralValue(arrLitWithName, false));

    // OBJECTLIT with immutable
    Node objLit = new Node(Token.OBJECTLIT, createStringNode("key"), createNumberNode(1));
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    // REGEXP
    assertTrue(NodeUtil.isLiteralValue(createNode(Token.REGEXP), false));

    // FUNCTION with includeFunctions=false
    Node func = createNode(Token.FUNCTION);
    assertFalse(NodeUtil.isLiteralValue(func, false));
    // FUNCTION with includeFunctions=true and isFunctionExpression
    // Need to make it a function expression by setting parent to something not a statement
    // For simplicity, test the code path: includeFunctions=true, not a declaration.
    // We can create a function node without parent and assume it's expression.
    assertTrue(NodeUtil.isLiteralValue(func, true));

    // isFunctionDeclaration check: if parent is SCRIPT it is declaration
    Node script = createNode(Token.SCRIPT);
    script.addChildToFront(func);
    // Now func is a function declaration (parent is SCRIPT)
    assertFalse(NodeUtil.isLiteralValue(func, true));
  }

  @Test(timeout = 4000)
  public void testGetBooleanValue() {
    // STRING non-empty
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createStringNode("a")));
    // STRING empty
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createStringNode("")));

    // NUMBER non-zero
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNumberNode(1.5)));
    // NUMBER zero
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNumberNode(0.0)));
    // NUMBER negative zero? -0 is false
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNumberNode(-0.0)));

    // NULL, FALSE, VOID
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNode(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNode(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNode(Token.VOID)));

    // NAME "undefined"
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNameNode("undefined")));
    // NAME "NaN"
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(createNameNode("NaN")));
    // NAME "Infinity"
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNameNode("Infinity")));
    // Other NAME
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(createNameNode("x")));

    // TRUE, ARRAYLIT, OBJECTLIT, REGEXP
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNode(Token.TRUE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNode(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNode(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(createNode(Token.REGEXP)));
  }

  @Test(timeout = 4000)
  public void testGetExpressionBooleanValue() {
    // ASSIGN: result is value of RHS
    Node assign = new Node(Token.ASSIGN, createNameNode("a"), createNode(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    // COMMA: value is last child
    Node comma = new Node(Token.COMMA, createNumberNode(1), createNode(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(comma));

    // NOT
    Node notFalse = new Node(Token.NOT, createNode(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(notFalse));

    // AND: both true -> true
    Node andTrue = new Node(Token.AND, createNode(Token.TRUE), createNode(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(andTrue));
    // AND: one false -> false
    Node andFalse = new Node(Token.AND, createNode(Token.TRUE), createNode(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(andFalse));

    // OR: both false -> false
    Node orFalse = new Node(Token.OR, createNode(Token.FALSE), createNode(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(orFalse));
    // OR: one true -> true
    Node orTrue = new Node(Token.OR, createNode(Token.FALSE), createNode(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orTrue));

    // HOOK: both branches equal
    Node hookEqual = new Node(Token.HOOK, createNode(Token.TRUE), createNode(Token.TRUE), createNode(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookEqual));
    // HOOK: branches different -> UNKNOWN
    Node hookDiff = new Node(Token.HOOK, createNode(Token.FALSE), createNode(Token.TRUE), createNode(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));

    // Default: delegate to getBooleanValue
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(createStringNode("")));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    // NAME
    assertEquals("foo", NodeUtil.getStringValue(createNameNode("foo")));
    // STRING
    assertEquals("bar", NodeUtil.getStringValue(createStringNode("bar")));
    // NUMBER integer
    assertEquals("5", NodeUtil.getStringValue(createNumberNode(5)));
    // NUMBER decimal
    assertEquals("3.14", NodeUtil.getStringValue(createNumberNode(3.14)));
    // FALSE, TRUE, NULL, VOID
    assertEquals("false", NodeUtil.getStringValue(createNode(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(createNode(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(createNode(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(createNode(Token.VOID)));
    // Other types return null
    assertNull(NodeUtil.getStringValue(createNode(Token.ARRAYLIT)));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    Node blockEmpty = createNode(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(blockEmpty));

    Node blockWithEmpty = new Node(Token.BLOCK, createNode(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithNonEmpty = new Node(Token.BLOCK, createNumberNode(1));
    assertFalse(NodeUtil.isEmptyBlock(blockWithNonEmpty));

    // Non-block returns false
    assertFalse(NodeUtil.isEmptyBlock(createNode(Token.SCRIPT)));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETPROP)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.NOT)));

    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.FUNCTION)));
  }

  @Test(timeout = 4000)
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test(timeout = 4000)
  public void testIsGet() {
    assertTrue(NodeUtil.isGet(new Node(Token.GETPROP)));
    assertTrue(NodeUtil.isGet(new Node(Token.GETELEM)));
    assertFalse(NodeUtil.isGet(new Node(Token.NAME)));
  }

  @Test(timeout = 4000)
  public void testIsName() {
    assertTrue(NodeUtil.isName(new Node(Token.NAME)));
    assertFalse(NodeUtil.isName(new Node(Token.STRING)));
  }

  @Test(timeout = 4000)
  public void testIsFunction() {
    assertTrue(NodeUtil.isFunction(new Node(Token.FUNCTION)));
    assertFalse(NodeUtil.isFunction(new Node(Token.NAME)));
  }

  @Test(timeout = 4000)
  public void testIsCall() {
    assertTrue(NodeUtil.isCall(new Node(Token.CALL)));
    assertFalse(NodeUtil.isCall(new Node(Token.NEW)));
  }

  @Test(timeout = 4000)
  public void testIsVar() {
    assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    assertFalse(NodeUtil.isVar(new Node(Token.NAME)));
  }

  @Test(timeout = 4000)
  public void testIsNew() {
    assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    assertFalse(NodeUtil.isNew(new Node(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testIsFunctionExpression() {
    // Function with parent that is a statement block -> declaration
    Node script = createNode(Token.SCRIPT);
    Node func = new Node(Token.FUNCTION);
    script.addChildToFront(func);
    assertFalse(NodeUtil.isFunctionExpression(func));

    // Function without parent or parent is expression -> expression
    Node funcExpr = new Node(Token.FUNCTION);
    assertTrue(NodeUtil.isFunctionExpression(funcExpr));
  }

  @Test(timeout = 4000)
  public void testIsFunctionDeclaration() {
    // Function inside SCRIPT
    Node script = createNode(Token.SCRIPT);
    Node func = new Node(Token.FUNCTION);
    script.addChildToFront(func);
    assertTrue(NodeUtil.isFunctionDeclaration(func));

    // Function inside expression
    Node funcExpr = new Node(Token.FUNCTION);
    assertFalse(NodeUtil.isFunctionDeclaration(funcExpr));
  }

  // ==================== Partition B: Boundary & Extremes ====================

  @Test(timeout = 4000)
  public void testPrecedence() {
    // Test boundary values
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(6, NodeUtil.precedence(Token.BITXOR));
    assertEquals(7, NodeUtil.precedence(Token.BITAND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    // High precedence tokens
    assertEquals(15, NodeUtil.precedence(Token.CALL));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testPrecedenceInvalidType() {
    NodeUtil.precedence(Token.EMPTY); // not in switch, falls to default -> throws Error
  }

  @Test(timeout = 4000)
  public void testOpToStr() {
    // Valid operators
    assertEquals("|", NodeUtil.opToStr(Token.BITOR));
    assertEquals("||", NodeUtil.opToStr(Token.OR));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testOpToStrNoFailInvalid() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  @Test(timeout = 4000)
  public void testIsLatin() {
    assertTrue(NodeUtil.isLatin("hello"));
    assertFalse(NodeUtil.isLatin("héllo"));
    // Boundary: character 0x7F is still Latin? Max is 0x7F, so 0x7F is Latin.
    assertTrue(NodeUtil.isLatin("a" + (char)0x7F));
    assertFalse(NodeUtil.isLatin("a" + (char)0x80));
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================
  // Targeting known defect in isValidDefineValue (false negative for valid string literals and other valid values)

  @Test(timeout = 4000)
  public void testIsValidDefineValue_Strings() {
    Set<String> defines = new HashSet<>();
    // String literals should always be valid
    assertTrue(NodeUtil.isValidDefineValue(createStringNode("hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(createStringNode(""), defines));

    // Number literals
    assertTrue(NodeUtil.isValidDefineValue(createNumberNode(0), defines));
    assertTrue(NodeUtil.isValidDefineValue(createNumberNode(42), defines));

    // Boolean literals
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_DefinesName() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_OVERRIDE_STRING"));
    // A NAME node that is in defines set should be valid
    Node nameInDefines = createNameNode("DEF_OVERRIDE_STRING");
    assertTrue(NodeUtil.isValidDefineValue(nameInDefines, defines));

    // A NAME node not in defines should be invalid
    Node nameNotInDefines = createNameNode("OTHER_VAR");
    assertFalse(NodeUtil.isValidDefineValue(nameNotInDefines, defines));

    // A GETPROP that is qualified and in defines
    Node getProp = new Node(Token.GETPROP, createNameNode("a"), createStringNode("b"));
    NodeUtil.setDebugInformation(getProp, getProp, "a.b");
    / / Set qualified name property? Actually, GETPROP isQualifiedName() relies on children.
    // The set of defines should contain the qualified name.
    Set<String> definesWithQualified = new HashSet<>(Arrays.asList("a.b"));
    assertTrue(NodeUtil.isValidDefineValue(getProp, definesWithQualified));

    // GETPROP not in defines -> false
    assertFalse(NodeUtil.isValidDefineValue(getProp, defines));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_UnaryOperators() {
    Set<String> defines = new HashSet<>();
    // NOT with TRUE child -> valid
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(notTrue, defines));

    // NEG with number child
    Node negNumber = new Node(Token.NEG, createNumberNode(5));
    assertTrue(NodeUtil.isValidDefineValue(negNumber, defines));

    // BITNOT with number child
    Node bitnotNumber = new Node(Token.BITNOT, createNumberNode(7));
    assertTrue(NodeUtil.isValidDefineValue(bitnotNumber, defines));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_BinaryOperators() {
    Set<String> defines = new HashSet<>();
    // BITOR with both children valid
    Node bitOr = new Node(Token.BITOR, createNumberNode(1), createNumberNode(2));
    assertTrue(NodeUtil.isValidDefineValue(bitOr, defines));

    // BITAND with one invalid child (NAME not in defines) -> invalid
    Node bitAndInvalid = new Node(Token.BITAND, createNumberNode(1), createNameNode("x"));
    assertFalse(NodeUtil.isValidDefineValue(bitAndInvalid, defines));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_UnqualifiedName() {
    Set<String> defines = new HashSet<>();
    // NAME with empty string is not qualified name -> should return false
    Node nameEmpty = createNode(Token.NAME, "");
    assertFalse(NodeUtil.isValidDefineValue(nameEmpty, defines));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue_OtherTypesReturnFalse() {
    Set<String> defines = new HashSet<>();
    // ARRAYLIT is not a valid define value
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
    // FUNCTION
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.FUNCTION), defines));
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testGetBooleanValueNull() {
    NodeUtil.getBooleanValue(null);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testIsImmutableValueNull() {
    NodeUtil.isImmutableValue(null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testConstructorCallHasSideEffectsNonNew() {
    NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testFunctionCallHasSideEffectsNonCall() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
  }

  @Test(timeout = 4000)
  public void testCheckForStateChangeHelperBranchCoverage() {
    // Tests many branches of checkForStateChangeHelper via mayHaveSideEffects
    // Token.THROW is always side-effect
    Node throwNode = new Node(Token.THROW, createStringNode("error"));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    // Token.OBJECTLIT with checkForNewObjects=true -> side effect
    assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.OBJECTLIT)));

    // Token.NAME with children -> side effect (variable declaration)
    Node varDecl = new Node(Token.NAME);
    varDecl.addChildToFront(createNumberNode(1));
    assertTrue(NodeUtil.mayHaveSideEffects(varDecl));

    // Token.FUNCTION declaration (parent is SCRIPT) with checkForNewObjects=false -> no side effect from function expression? Actually, function declaration does not cause side effects at declaration time
    Node script = createNode(Token.SCRIPT);
    Node func = new Node(Token.FUNCTION);
    script.addChildToFront(func);
    // mayHaveSideEffects returns false for function declarations? Let's check code: for FUNCTION, returns checkForNewObjects || !isFunctionExpression(n). Since isFunctionExpression is false (it is a declaration), !isFunctionExpression is true, so returns true if checkForNewObjects is false? Wait: return checkForNewObjects || !isFunctionExpression(n);
    // checkForNewObjects is false, and !isFunctionExpression is true, so returns true. So declaration has side effects (affects namespace). So mayHaveSideEffects should return true.
    assertTrue(NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testNewExpr() {
    Node expr = NodeUtil.newExpr(createNumberNode(1));
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertSame(expr.getFirstChild().getType(), Token.NUMBER);
  }

  @Test(timeout = 4000)
  public void testNewUndefinedNode() {
    Node undef = NodeUtil.newUndefinedNode(null);
    assertEquals(Token.VOID, undef.getType());
    assertEquals(Token.NUMBER, undef.getFirstChild().getType());
    assertEquals(0.0, undef.getFirstChild().getDouble(), 0.0);

    Node src = createNumberNode(42);
    Node undefWithSrc = NodeUtil.newUndefinedNode(src);
    // Should copy source info
    assertTrue(undefWithSrc != null);
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
  // Static utility methods, no object state to test.

  @Test(timeout = 4000)
  public void testGetFunctionName() {
    // case: function name() ...
    Node func = new Node(Token.FUNCTION);
    func.addChildToFront(createNameNode("foo")); // name
    func.addChildToFront(new Node(Token.LP)); // params
    func.addChildToFront(new Node(Token.BLOCK)); // body
    // parent is null -> default: return name if not empty
    assertEquals("foo", NodeUtil.getFunctionName(func));

    // case: var name = function() ...
    Node var = new Node(Token.VAR, createNameNode("bar"));
    var.getFirstChild().addChildToFront(func);
    func.setParent(var);
    // parent is NAME -> returns parent string "bar"
    assertEquals("bar", NodeUtil.getFunctionName(func));

    // case: qualified.name = function() ...
    Node assign = new Node(Token.ASSIGN, new Node(Token.GETPROP, createNameNode("a"), createStringNode("b")), func);
    func.setParent(assign);
    assertEquals("a.b", NodeUtil.getFunctionName(func));

    // case: function with no name (empty string)
    Node funcNoName = new Node(Token.FUNCTION);
    funcNoName.addChildToFront(createNameNode(""));
    funcNoName.addChildToFront(new Node(Token.LP));
    funcNoName.addChildToFront(new Node(Token.BLOCK));
    // default case: name is empty -> returns null
    assertNull(NodeUtil.getFunctionName(funcNoName));
  }

  @Test(timeout = 4000)
  public void testGetNearestFunctionName() {
    // fallback to getFunctionName first, then OBJECTLIT
    Node func = new Node(Token.FUNCTION);
    func.addChildToFront(createNameNode("")); // no name
    func.addChildToFront(new Node(Token.LP));
    func.addChildToFront(new Node(Token.BLOCK));
    // parent is OBJECTLIT: {key: function() {}}
    Node objLit = new Node(Token.OBJECTLIT, createStringNode("key"), func);
    func.setParent(objLit);
    assertEquals("key", NodeUtil.getNearestFunctionName(func));

    // If both fail, return null
    Node func2 = new Node(Token.FUNCTION);
    func2.addChildToFront(createNameNode(""));
    func2.addChildToFront(new Node(Token.LP));
    func2.addChildToFront(new Node(Token.BLOCK));
    // no parent -> null
    assertNull(NodeUtil.getNearestFunctionName(func2));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableStateComplex() {
    // Assignment to property of local value: no side effect
    Node localName = createNameNode("x");
    Node assign = new Node(Token.ASSIGN, new Node(Token.GETPROP, localName, createStringNode("p")), createNumberNode(1));
    // localName evaluates to local value? Not defined as local, so we need to use locals predicate? The code uses evaluatesToLocalValue which checks for locals. Since we haven't set a scope, it's not local. So mayEffectMutableState will consider it side effect because isGet on LHS and current (localName) not evaluated to local. So we can't easily test that branch without proper AST.
    // For coverage, we can still test that the method runs.
    boolean result = NodeUtil.mayEffectMutableState(assign);
    // We just assert it's not throwing.
    assertNotNull(result);
  }

  // Additional test to ensure coverage of rarely used branches
  @Test(timeout = 4000)
  public void testIsExprAssign() {
    Node expr = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN));
    assertTrue(NodeUtil.isExprAssign(expr));

    Node exprNotAssign = new Node(Token.EXPR_RESULT, new Node(Token.CALL));
    assertFalse(NodeUtil.isExprAssign(exprNotAssign));
  }

  @Test(timeout = 4000)
  public void testIsExprCall() {
    Node expr = new Node(Token.EXPR_RESULT, new Node(Token.CALL));
    assertTrue(NodeUtil.isExprCall(expr));

    Node exprNotCall = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN));
    assertFalse(NodeUtil.isExprCall(exprNotCall));
  }

  @Test(timeout = 4000)
  public void testIsForIn() {
    // FOR with 3 children
    Node forIn = new Node(Token.FOR, new Node(Token.IN), new Node(Token.NAME), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));

    // FOR with 4 children (regular for)
    Node forRegular = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isForIn(forRegular));
  }

  // Additional coverage for isAssignmentOp
  @Test(timeout = 4000)
  public void testIsAssignmentOpAllTypes() {
    int[] assignTypes = {
        Token.ASSIGN, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
        Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH, Token.ASSIGN_ADD,
        Token.ASSIGN_SUB, Token.ASSIGN_MUL, Token.ASSIGN_DIV, Token.ASSIGN_MOD
    };
    for (int type : assignTypes) {
      assertTrue("Failed for type " + type, NodeUtil.isAssignmentOp(new Node(type)));
    }
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  // Coverage for getOpFromAssignmentOp
  @Test(timeout = 4000)
  public void testGetOpFromAssignmentOp() {
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetOpFromAssignmentOpInvalid() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN));
  }

  // Test isConstantName
  @Test(timeout = 4000)
  public void testIsConstantName() {
    Node name = createNameNode("CONST");
    name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(name));

    Node nameNotConstant = createNameNode("var");
    assertFalse(NodeUtil.isConstantName(nameNotConstant));
  }

  // Test isReferenceName
  @Test(timeout = 4000)
  public void testIsReferenceName() {
    assertTrue(NodeUtil.isReferenceName(createNameNode("x")));
    assertFalse(NodeUtil.isReferenceName(createNode(Token.NAME, "")));
  }

  // Test isVarDeclaration with proper parent
  @Test(timeout = 4000)
  public void testIsVarDeclaration() {
    Node name = createNameNode("x");
    Node var = new Node(Token.VAR, name);
    name.setParent(var);
    assertTrue(NodeUtil.isVarDeclaration(name));

    // Not under VAR
    Node other = createNameNode("y");
    assertFalse(NodeUtil.isVarDeclaration(other));
  }

  // Test getAssignedValue
  @Test(timeout = 4000)
  public void testGetAssignedValue() {
    // In var declaration: var x = 1;
    Node name = createNameNode("x");
    Node val = createNumberNode(1);
    Node var = new Node(Token.VAR, name);
    name.addChildToFront(val);
    assertSame(val, NodeUtil.getAssignedValue(name));

    // In assignment: x = 2;
    Node assign = new Node(Token.ASSIGN, name, createNumberNode(2));
    name.setParent(assign);
    // getAssignedValue checks if parent is assign and first child is n
    assertSame(assign.getLastChild(), NodeUtil.getAssignedValue(name));

    // Not in any supported parent
    Node name2 = createNameNode("y");
    assertNull(NodeUtil.getAssignedValue(name2));
  }

  // Test isString
  @Test(timeout = 4000)
  public void testIsString() {
    assertTrue(NodeUtil.isString(new Node(Token.STRING)));
    assertFalse(NodeUtil.isString(new Node(Token.NAME)));
  }

  // Test isLoopStructure and related
  @Test(timeout = 4000)
  public void testIsLoopStructure() {
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));
  }

  @Test(timeout = 4000)
  public void testGetLoopCodeBlock() {
    // FOR: last child is block
    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertSame(forNode.getLastChild(), NodeUtil.getLoopCodeBlock(forNode));

    // DO: first child is block
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.EMPTY));
    assertSame(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));

    // Not a loop
    assertNull(NodeUtil.getLoopCodeBlock(new Node(Token.IF)));
  }

  // Test isWithinLoop (need loop ancestor)
  @Test(timeout = 4000)
  public void testIsWithinLoop() {
    // Build simple tree: WHILE -> BLOCK -> NAME
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    Node block = whileNode.getLastChild();
    Node name = createNameNode("x");
    block.addChildToFront(name);
    // name is inside loop
    assertTrue(NodeUtil.isWithinLoop(name));

    // Outside loop
    Node outerName = createNameNode("y");
    assertFalse(NodeUtil.isWithinLoop(outerName));

    // Loop inside function: outerName inside function but not loop
    Node func = new Node(Token.FUNCTION);
    func.addChildToFront(createNameNode("f"));
    func.addChildToFront(new Node(Token.LP));
    func.addChildToFront(new Node(Token.BLOCK));
    func.getLastChild().addChildToFront(outerName);
    // outerName is inside function but no loop ancestor -> false
    assertFalse(NodeUtil.isWithinLoop(outerName));
  }

  // Test isControlStructure
  @Test(timeout = 4000)
  public void testIsControlStructure() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.BLOCK)));
  }

  // Test isStatementBlock
  @Test(timeout = 4000)
  public void testIsStatementBlock() {
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.SCRIPT)));
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.BLOCK)));
    assertFalse(NodeUtil.isStatementBlock(new Node(Token.IF)));
  }

  // Test isStatement (with proper parent)
  @Test(timeout = 4000)
  public void testIsStatement() {
    Node block = new Node(Token.BLOCK);
    Node func = new Node(Token.FUNCTION);
    block.addChildToFront(func);
    assertTrue(NodeUtil.isStatement(func));

    // Not a statement
    Node exprFunc = new Node(Token.FUNCTION);
    // parent is null -> Precondition check will fail? Actually isStatement calls Preconditions.checkState(parent != null) so we need to provide a parent.
    // We'll set parent to something not a block/script/label:
    Node assign = new Node(Token.ASSIGN, exprFunc);
    exprFunc.setParent(assign);
    // parent is ASSIGN, which is not a statement container, so isStatement returns false.
    assertFalse(NodeUtil.isStatement(exprFunc));
  }

  // Test removeChild various branches (requires proper AST with parents)
  @Test(timeout = 4000)
  public void testRemoveChildFromBlock() {
    Node block = new Node(Token.BLOCK);
    Node child = createNumberNode(1);
    block.addChildToFront(child);
    // Should not throw
    NodeUtil.removeChild(block, child);
    assertFalse(block.hasChildren());
  }

  @Test(timeout = 4000)
  public void testRemoveChildFromVarLastChild() {
    Node var = new Node(Token.VAR);
    Node name = createNameNode("x");
    var.addChildToFront(name);
    // parent of var is block
    Node block = new Node(Token.BLOCK);
    block.addChildToFront(var);
    NodeUtil.removeChild(var, name);
    // Now var has no children, so should remove var from block
    assertFalse(block.hasChildren());
  }

  // Test tryMergeBlock
  @Test(timeout = 4000)
  public void testTryMergeBlockInParentBlock() {
    Node outerBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    outerBlock.addChildToFront(innerBlock);
    innerBlock.addChildToFront(createNumberNode(1));
    innerBlock.addChildToFront(createNumberNode(2));
    boolean merged = NodeUtil.tryMergeBlock(innerBlock);
    assertTrue(merged);
    assertFalse(innerBlock.hasChildren()); // innerBlock detached
    assertEquals(2, outerBlock.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlockNonBlockParent() {
    // Only merges if parent is a statement block
    Node assign = new Node(Token.ASSIGN);
    Node block = new Node(Token.BLOCK);
    assign.addChildToFront(block);
    assertFalse(NodeUtil.tryMergeBlock(block));
  }

  // Test isObjectLitKey
  @Test(timeout = 4000)
  public void testIsObjectLitKey() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = createStringNode("key");
    Node value = createNumberNode(1);
    objLit.addChildToFront(key);
    objLit.addChildToFront(value);
    assertTrue(NodeUtil.isObjectLitKey(key, objLit));
    assertFalse(NodeUtil.isObjectLitKey(value, objLit));
  }

  // Test isString (already covered)

  // Test isLhs
  @Test(timeout = 4000)
  public void testIsLhs() {
    Node assign = new Node(Token.ASSIGN);
    Node lhs = createNameNode("x");
    Node rhs = createNumberNode(1);
    assign.addChildToFront(lhs);
    assign.addChildToFront(rhs);
    assertTrue(NodeUtil.isLhs(lhs, assign));
    assertFalse(NodeUtil.isLhs(rhs, assign));

    // In VAR
    Node var = new Node(Token.VAR, lhs);
    assertTrue(NodeUtil.isLhs(lhs, var));
  }

  // coverage for isPrototypeProperty and related
  @Test(timeout = 4000)
  public void testIsPrototypeProperty() {
    String name = "a.prototype.b";
    Node qName = NodeUtil.newQualifiedNameNode(name, -1, -1);
    assertTrue(NodeUtil.isPrototypeProperty(qName));

    String nonProto = "a.b";
    Node nonProtoQName = NodeUtil.newQualifiedNameNode(nonProto, -1, -1);
    assertFalse(NodeUtil.isPrototypeProperty(nonProtoQName));
  }

  @Test(timeout = 4000)
  public void testIsPrototypePropertyDeclaration() {
    // Expr assign: EXPR_RESULT -> ASSIGN -> GETPROP (prototype) = value
    Node assign = new Node(Token.ASSIGN);
    String qName = "Foo.prototype.bar";
    Node lhs = NodeUtil.newQualifiedNameNode(qName, -1, -1);
    Node rhs = createNumberNode(42);
    assign.addChildToFront(lhs);
    assign.addChildToFront(rhs);
    Node exprResult = new Node(Token.EXPR_RESULT, assign);
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprResult));
  }

  // Test newQualifiedNameNode and getRootOfQualifiedName
  @Test(timeout = 4000)
  public void testNewQualifiedNameNodeAndRoot() {
    Node qName = NodeUtil.newQualifiedNameNode("a.b.c", -1, -1);
    assertEquals(Token.GETPROP, qName.getType());
    assertEquals("a", qName.getFirstChild().getString());
    assertEquals("b", qName.getFirstChild().getNext().getString());
    // Root should be NAME a
    Node root = NodeUtil.getRootOfQualifiedName(qName);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());

    // Simple name
    Node simple = NodeUtil.newQualifiedNameNode("x", -1, -1);
    assertEquals(Token.NAME, simple.getType());
    root = NodeUtil.getRootOfQualifiedName(simple);
    assertSame(simple, root);
  }

  // Test setDebugInformation
  @Test(timeout = 4000)
  public void testSetDebugInformation() {
    Node basis = createNode(Token.NAME, "original");
    basis.setSourceEncodedPosition(10);
    Node target = createNode(Token.NAME, "newName");
    NodeUtil.setDebugInformation(target, basis, "originalName");
    // copyInformationFromForTree should copy source info
    assertEquals(10, target.getSourcePosition());
    assertEquals("originalName", target.getProp(Node.ORIGINALNAME_PROP));
  }

  // Test newName methods
  @Test(timeout = 4000)
  public void testNewName() {
    Node basis = createNode(Token.NAME, "old");
    Node newName = NodeUtil.newName("new", basis);
    assertEquals("new", newName.getString());
    // source info copied from basis
    // No original name prop set

    Node newNameWithOrig = NodeUtil.newName("new2", basis, "origName");
    assertEquals("origName", newNameWithOrig.getProp(Node.ORIGINALNAME_PROP));
  }

  // Test isValidPropertyName
  @Test(timeout = 4000)
  public void testIsValidPropertyName() {
    assertTrue(NodeUtil.isValidPropertyName("validKey"));
    assertFalse(NodeUtil.isValidPropertyName("invalid key"));
    assertFalse(NodeUtil.isValidPropertyName("for")); // reserved keyword
    // Unicode non-latin
    assertFalse(NodeUtil.isValidPropertyName("héllo"));
  }

  // Test containsFunction and others
  @Test(timeout = 4000)
  public void testContainsFunction() {
    Node root = new Node(Token.BLOCK);
    root.addChildToFront(createNode(Token.FUNCTION));
    assertTrue(NodeUtil.containsFunction(root));

    Node noFunc = new Node(Token.BLOCK, createNumberNode(1));
    assertFalse(NodeUtil.containsFunction(noFunc));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffectsSideEffectFreeConstructors() {
    // Known constructor without side effects: "Array"
    Node newArray = new Node(Token.NEW, createNameNode("Array"));
    assertFalse(NodeUtil.mayHaveSideEffects(newArray));

    // Unknown constructor: "MyClass"
    Node newMyClass = new Node(Token.NEW, createNameNode("MyClass"));
    assertTrue(NodeUtil.mayHaveSideEffects(newMyClass));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffectsBuiltins() {
    Node callBuiltin = new Node(Token.CALL, createNameNode("String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));

    Node callMath = new Node(Token.CALL, new Node(Token.GETPROP, createNameNode("Math"), createStringNode("abs")));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMath));

    // Non-builtin
    Node callUnknown = new Node(Token.CALL, createNameNode("foo"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callUnknown));
  }

  // Additional coverage for getExpressionBooleanValue with ASSIGN and COMMA
  @Test(timeout = 4000)
  public void testGetExpressionBooleanValueAssignComma() {
    Node assign = new Node(Token.ASSIGN, createNameNode("x"), createNode(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(assign));

    Node comma = new Node(Token.COMMA, createNumberNode(1), createNode(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(comma));
  }

  // Test isSimpleOperatorType all true types
  @Test(timeout = 4000)
  public void testIsSimpleOperatorTypeAllTrue() {
    int[] simpleTypes = {
        Token.ADD, Token.BITAND, Token.BITNOT, Token.BITOR, Token.BITXOR,
        Token.COMMA, Token.DIV, Token.EQ, Token.GE, Token.GETELEM, Token.GETPROP,
        Token.GT, Token.INSTANCEOF, Token.LE, Token.LSH, Token.LT, Token.MOD,
        Token.MUL, Token.NE, Token.NOT, Token.RSH, Token.SHEQ, Token.SHNE,
        Token.SUB, Token.TYPEOF, Token.VOID, Token.POS, Token.NEG, Token.URSH
    };
    for (int type : simpleTypes) {
      assertTrue("Simple operator type " + type, NodeUtil.isSimpleOperatorType(type));
    }
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
  }

  // Test isAssociative
  @Test(timeout = 4000)
  public void testIsAssociative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
  }

  // Test containsType with predicate
  @Test(timeout = 4000)
  public void testContainsTypeWithPredicate() {
    Node root = new Node(Token.BLOCK, new Node(Token.FUNCTION));
    assertTrue(NodeUtil.containsType(root, Token.FUNCTION, new NodeUtil.MatchNotFunction()));
  }

  // Test getVarsDeclaredInBranch
  @Test(timeout = 4000)
  public void testGetVarsDeclaredInBranch() {
    Node root = new Node(Token.BLOCK);
    Node var = new Node(Token.VAR, createNameNode("a"));
    root.addChildToFront(var);
    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(root);
    assertEquals(1, vars.size());
    assertEquals("a", vars.iterator().next().getString());
  }

  // Test isSwitchCase
  @Test(timeout = 4000)
  public void testIsSwitchCase() {
    assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));
    assertFalse(NodeUtil.isSwitchCase(new Node(Token.IF)));
  }

  // Test getCatchBlock, hasFinally, hasCatchHandler
  @Test(timeout = 4000)
  public void testTryBlockHelpers() {
    Node tryNode = new Node(Token.TRY);
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node finallyBlock = new Node(Token.BLOCK);
    tryNode.addChildrenToBack(tryBlock);
    tryNode.addChildrenToBack(catchBlock);
    tryNode.addChildrenToBack(finallyBlock);
    assertTrue(NodeUtil.hasFinally(tryNode));
    assertSame(catchBlock, NodeUtil.getCatchBlock(tryNode));
    // catchBlock has no child CATCH -> hasCatchHandler false
    assertFalse(NodeUtil.hasCatchHandler(catchBlock));
    // Add a CATCH node
    catchBlock.addChildToFront(new Node(Token.CATCH));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
  }

  // Test getFnParameters
  @Test(timeout = 4000)
  public void testGetFnParameters() {
    Node func = new Node(Token.FUNCTION);
    func.addChildToFront(createNameNode("f"));
    Node lp = new Node(Token.LP);
    lp.addChildToFront(createNameNode("arg1"));
    func.addChildToBack(lp);
    func.addChildToBack(new Node(Token.BLOCK));
    assertSame(lp, NodeUtil.getFnParameters(func));
  }

  // Test newVarNode
  @Test(timeout = 4000)
  public void testNewVarNode() {
    Node var = NodeUtil.newVarNode("x", createNumberNode(5));
    assertEquals(Token.VAR, var.getType());
    assertEquals("x", var.getFirstChild().getString());
    assertEquals(5.0, var.getFirstChild().getFirstChild().getDouble(), 0.0);
  }

  // Test newCallNode
  @Test(timeout = 4000)
  public void testNewCallNode() {
    Node callTarget = createNameNode("foo");
    Node param1 = createNumberNode(1);
    Node param2 = createNumberNode(2);
    Node call = NodeUtil.newCallNode(callTarget, param1, param2);
    assertEquals(Token.CALL, call.getType());
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
    assertEquals(2, call.getChildCount() - 1); // children after target
    assertSame(param1, call.getFirstChild().getNext());
    assertSame(param2, call.getLastChild());
  }

  // Test evaluatesToLocalValue basic cases
  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    // Immutable value is local
    assertTrue(NodeUtil.evaluatesToLocalValue(createStringNode("hello")));
    // NEW node is local
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.NEW, createNameNode("Array"))));
    // CALL with local result? Need a node with side effect flags set. Not easily testable.
    // We'll just test that it doesn't throw.
    Node call = new Node(Token.CALL, createNameNode("foo"));
    // Without proper flags, it may throw? The method calls callHasLocalResult, which returns false, then goes to default, and since isAssignmentOp false, isSimpleOperator false, isImmutableValue false, it throws IllegalStateException.
    try {
      NodeUtil.evaluatesToLocalValue(call);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  // Test getExpressionBooleanValue with HOOK that goes to default
  // Already covered.

  // Additional test for isReferenceName with empty string (should be false)
  @Test(timeout = 4000)
  public void testIsReferenceNameEmpty() {
    Node n = createNode(Token.NAME, "");
    assertFalse(NodeUtil.isReferenceName(n));
  }
}