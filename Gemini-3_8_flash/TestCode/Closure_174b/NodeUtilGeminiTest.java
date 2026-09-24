/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.NodeUtil
 * Targeted Decision Branches & Boundary Coverage:
 * - getImpureBooleanValue / getPureBooleanValue: ASSIGN, COMMA, NOT, AND, OR, HOOK (identical vs divergent arms),
 *   ARRAYLIT, OBJECTLIT (with/without side-effects), VOID, STRING (empty vs non-empty), NUMBER (0, non-zero, NaN),
 *   NAME ("undefined", "NaN", "Infinity", other), REGEXP, TRUE, FALSE, NULL.
 * - getStringValue / getNumberValue / getStringNumberValue: Hex strings (0x..., 0X...), signed hex (+0x, -0x),
 *   "infinity", "-infinity", "+infinity", whitespace trimming (VT '\u000B', space separator, BOM, etc.),
 *   double-to-string integer preservation, array to string conversion (empty elements, null/undefined in arrays).
 * - Function Utilities: getFunctionName, getNearestFunctionName (SETTER_DEF, GETTER_DEF, STRING_KEY, NUMBER key),
 *   isFunctionDeclaration vs isFunctionExpression, isBleedingFunctionName, isEmptyFunctionExpression, isVarArgsFunction.
 * - Operator Classification & Precedence: isSymmetricOperation, isRelationalOperation, getInverseOperator,
 *   isAssociative, isCommutative, isSimpleOperator, isAssignmentOp, getOpFromAssignmentOp, precedence & precedenceWithDefault.
 * - State Changes & Side Effects: mayHaveSideEffects, mayEffectMutableState, constructorCallHasSideEffects,
 *   functionCallHasSideEffects (builtins without side effects, Object methods, Math.floor, RegExp methods).
 * - AST Query & Mutation: removeChild (try-finally, catch container, block detach, statement block, single vs multi var,
 *   label removal, for loop with empty child), tryMergeBlock, maybeAddFinally, isLValue, getBestLValue,
 *   getBestLValueName, getBestLValueOwner, isExpressionResultUsed, isExecutedExactlyOnce.
 * - Defect Zone (Issue 1103 / ScopedAliases context): Bleeding function names in var assignments, hoisted vs unhoisted
 *   function declarations, redeclareVarsInsideBranch, L-value resolution for functions/vars/params.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBooleanEvaluationPureAndImpure() {
    Node trueNode = IR.trueNode();
    Node falseNode = IR.falseNode();
    Node nullNode = IR.nullNode();
    Node numZero = IR.number(0);
    Node numOne = IR.number(1);
    Node strEmpty = IR.string("");
    Node strNonEmpty = IR.string("hello");

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(trueNode));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(falseNode));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nullNode));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(numZero));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(numOne));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(strEmpty));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(strNonEmpty));

    Node nameUndef = IR.name("undefined");
    Node nameNaN = IR.name("NaN");
    Node nameInf = IR.name("Infinity");
    Node nameOther = IR.name("customVar");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameUndef));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNaN));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nameInf));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(nameOther));

    Node voidZero = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidZero));

    Node notTrue = new Node(Token.NOT, IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));

    Node assign = new Node(Token.ASSIGN, IR.name("x"), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, IR.name("x"), IR.string(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(comma));

    Node andNode = new Node(Token.AND, IR.trueNode(), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, IR.falseNode(), IR.string("a"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookSame = new Node(Token.HOOK, IR.name("cond"), IR.trueNode(), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, IR.name("cond"), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));

    Node arrayLit = IR.arraylit();
    Node objectLit = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrayLit));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objectLit));
  }

  @Test(timeout = 4000)
  public void testStringAndNumberConversions() {
    assertEquals("1", NodeUtil.getStringValue(1.0));
    assertEquals("1.5", NodeUtil.getStringValue(1.5));
    assertEquals("-5", NodeUtil.getStringValue(-5.0));

    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    assertEquals("undefined", NodeUtil.getStringValue(IR.voidNode(IR.number(0))));
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    assertNull(NodeUtil.getStringValue(IR.name("unknown")));

    Node notNode = new Node(Token.NOT, IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(notNode));

    Node arrayLit = IR.arraylit(IR.number(1), IR.nullNode(), IR.string("abc"));
    assertEquals("1,,abc", NodeUtil.getStringValue(arrayLit));

    Node objLit = IR.objectlit();
    assertEquals("[object Object]", NodeUtil.getStringValue(objLit));

    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(IR.number(42.5)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));

    Node negInf = new Node(Token.NEG, IR.name("Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

    Node notZero = new Node(Token.NOT, IR.number(0));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notZero));
  }

  @Test(timeout = 4000)
  public void testStringNumberParsingBoundaries() {
    assertNull(NodeUtil.getStringNumberValue("123\u000b456"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xGG")));

    assertNull(NodeUtil.getStringNumberValue("+0x12"));
    assertNull(NodeUtil.getStringNumberValue("-0x12"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));

    assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45  "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_number")));
  }

  @Test(timeout = 4000)
  public void testWhitespaceHelper() {
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\r'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000C'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('A'));

    assertEquals("abc", NodeUtil.trimJsWhiteSpace(" \t abc \n\r "));
  }

  @Test(timeout = 4000)
  public void testFunctionNamingConventions() {
    Node fnDecl = IR.function(IR.name("myFunc"), IR.paramList(), IR.block());
    assertEquals("myFunc", NodeUtil.getFunctionName(fnDecl));
    assertEquals("myFunc", NodeUtil.getNearestFunctionName(fnDecl));

    Node fnExpr = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node varName = IR.name("varFunc");
    varName.addChildToBack(fnExpr);
    Node varDecl = IR.var(varName);
    assertEquals("varFunc", NodeUtil.getFunctionName(fnExpr));

    Node fnAssign = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node getProp = IR.getprop(IR.name("obj"), IR.string("prop"));
    Node assign = IR.assign(getProp, fnAssign);
    assertEquals("obj.prop", NodeUtil.getFunctionName(fnAssign));

    Node anonFn = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node strKey = Node.newString(Token.STRING_KEY, "keyName");
    strKey.addChildToBack(anonFn);
    assertEquals("keyName", NodeUtil.getNearestFunctionName(anonFn));

    assertNull(NodeUtil.getNearestFunctionName(IR.number(123)));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testOperatorInversionAndClassification() {
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.EQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.NE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHEQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHNE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSymmetricOperation(new Node(Token.ADD)));

    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GE)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LE)));
    assertFalse(NodeUtil.isRelationalOperation(new Node(Token.EQ)));

    assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
    assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
    assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
    assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
    assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.ADD));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITXOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
  }

  @Test(timeout = 4000)
  public void testPrecedenceRules() {
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
    assertEquals(15, NodeUtil.precedence(Token.NAME));
    assertEquals(16, NodeUtil.precedence(Token.CAST));

    assertEquals(-1, NodeUtil.precedenceWithDefault(Token.BLOCK));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceUnknownThrows() {
    NodeUtil.precedence(Token.BLOCK);
  }

  @Test(timeout = 4000)
  public void testOpToStrMapping() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.BLOCK));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnNonOp() {
    NodeUtil.opToStrNoFail(Token.BLOCK);
  }

  @Test(timeout = 4000)
  public void testAssignmentOpHelpers() {
    Node assignBitOr = new Node(Token.ASSIGN_BITOR);
    assertTrue(NodeUtil.isAssignmentOp(assignBitOr));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(assignBitOr));

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

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromNonAssignmentThrows() {
    NodeUtil.getOpFromAssignmentOp(IR.name("x"));
  }

  @Test(timeout = 4000)
  public void testLatinAndIdentifierValidation() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("unicode\u0100"));

    assertTrue(NodeUtil.isValidSimpleName("validVar"));
    assertTrue(NodeUtil.isValidSimpleName("$test_123"));
    assertFalse(NodeUtil.isValidSimpleName("class"));
    assertFalse(NodeUtil.isValidSimpleName("123bad"));

    assertTrue(NodeUtil.isValidQualifiedName("a.b.c"));
    assertTrue(NodeUtil.isValidQualifiedName("foo"));
    assertFalse(NodeUtil.isValidQualifiedName(".bad"));
    assertFalse(NodeUtil.isValidQualifiedName("bad."));
    assertFalse(NodeUtil.isValidQualifiedName("a..b"));
    assertFalse(NodeUtil.isValidQualifiedName("a.class.c"));

    assertTrue(NodeUtil.isValidPropertyName("propName"));
    assertFalse(NodeUtil.isValidPropertyName("default"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (ScopedAliases / Issue 1103)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue1103FunctionExpressionsAndBleedingNames() {
    // Bleeding function: var a = function a() {};
    Node fnInnerName = IR.name("a");
    Node fn = IR.function(fnInnerName, IR.paramList(), IR.block());
    Node varName = IR.name("a");
    varName.addChildToBack(fn);
    Node varStmt = IR.var(varName);
    Node script = IR.script(varStmt);

    assertTrue(NodeUtil.isVarDeclaration(varName));
    assertTrue(NodeUtil.isLValue(varName));
    assertTrue(NodeUtil.isFunctionExpression(fn));
    assertFalse(NodeUtil.isFunctionDeclaration(fn));
    assertTrue(NodeUtil.isBleedingFunctionName(fnInnerName));
    assertTrue(NodeUtil.isLValue(fnInnerName));

    // Non-bleeding anonymous function: var b = function() {};
    Node anonFnName = IR.name("");
    Node anonFn = IR.function(anonFnName, IR.paramList(), IR.block());
    Node varB = IR.name("b");
    varB.addChildToBack(anonFn);
    IR.var(varB);
    assertFalse(NodeUtil.isBleedingFunctionName(anonFnName));

    // Hoisted function declaration at script root: function f() {}
    Node hoistedName = IR.name("f");
    Node hoistedFn = IR.function(hoistedName, IR.paramList(), IR.block());
    script.addChildToBack(hoistedFn);
    assertTrue(NodeUtil.isFunctionDeclaration(hoistedFn));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(hoistedFn));
    assertFalse(NodeUtil.isBleedingFunctionName(hoistedName));
  }

  @Test(timeout = 4000)
  public void testIssue1103VarsRedeclarationAndBranchCollection() {
    Node fnBody = IR.block();
    Node fn = IR.function(IR.name("scoped"), IR.paramList(), fnBody);
    Node script = IR.script(IR.exprResult(fn));

    Node varA = IR.var(IR.name("a"));
    Node varB = IR.var(IR.name("b"));
    fnBody.addChildToBack(varA);
    fnBody.addChildToBack(varB);

    Collection<Node> declared = NodeUtil.getVarsDeclaredInBranch(fnBody);
    assertEquals(2, declared.size());

    // Redeclare inside function scope
    NodeUtil.redeclareVarsInsideBranch(fnBody);
    // After redeclaration, a new var node is prepended to the function body
    Node first = fnBody.getFirstChild();
    assertEquals(Token.VAR, first.getType());
  }

  @Test(timeout = 4000)
  public void testLValueClassifications() {
    Node name = IR.name("target");
    Node assign = IR.assign(name, IR.number(1));
    assertTrue(NodeUtil.isLValue(name));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, assign));

    Node forIn = new Node(Token.FOR, IR.name("x"), IR.name("arr"), IR.block());
    assertTrue(NodeUtil.isLValue(forIn.getFirstChild()));

    Node decNode = new Node(Token.DEC, IR.name("y"));
    assertTrue(NodeUtil.isLValue(decNode.getFirstChild()));

    Node incNode = new Node(Token.INC, IR.name("z"));
    assertTrue(NodeUtil.isLValue(incNode.getFirstChild()));

    Node param = IR.name("p");
    Node params = IR.paramList(param);
    assertTrue(NodeUtil.isLValue(param));

    Node catchBlock = new Node(Token.CATCH, IR.name("e"), IR.block());
    assertTrue(NodeUtil.isLValue(catchBlock.getFirstChild()));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsNonNewThrows() {
    NodeUtil.constructorCallHasSideEffects(IR.call(IR.name("f")));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsNonCallThrows() {
    NodeUtil.functionCallHasSideEffects(IR.name("notACall"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetFunctionNameOnNonFunctionThrows() {
    NodeUtil.getFunctionName(IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildInvalidNodeStructureThrows() {
    Node parent = IR.exprResult(IR.assign(IR.name("x"), IR.number(1)));
    NodeUtil.removeChild(parent, parent.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testRemoveChildSafeMutations() {
    // Statement in block
    Node stmt = IR.exprResult(IR.number(1));
    Node block = IR.block(stmt);
    NodeUtil.removeChild(block, stmt);
    assertEquals(0, block.getChildCount());

    // Multi-var removal
    Node var1 = IR.name("v1");
    Node var2 = IR.name("v2");
    Node varStmt = IR.var(var1, var2);
    Node script = IR.script(varStmt);
    NodeUtil.removeChild(varStmt, var1);
    assertEquals(1, varStmt.getChildCount());
    assertEquals("v2", varStmt.getFirstChild().getString());

    // Single-var removal triggers parent removal
    Node singleVarName = IR.name("solo");
    Node singleVar = IR.var(singleVarName);
    script.addChildToBack(singleVar);
    NodeUtil.removeChild(singleVar, singleVarName);
    assertFalse(script.hasChildren());

    // FOR loop 4-child replacement
    Node forLoop = new Node(Token.FOR, IR.var(IR.name("i")), IR.name("cond"), IR.inc(IR.name("i"), true), IR.block());
    Node cond = forLoop.getChildAtIndex(1);
    NodeUtil.removeChild(forLoop, cond);
    assertEquals(Token.EMPTY, forLoop.getChildAtIndex(1).getType());
  }

  @Test(timeout = 4000)
  public void testTryFinallyAndCatchRemoval() {
    Node tryBlock = IR.block();
    Node catchBlock = IR.block(new Node(Token.CATCH, IR.name("err"), IR.block()));
    Node finallyBlock = IR.block();
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    IR.script(tryNode);

    assertTrue(NodeUtil.hasFinally(tryNode));
    assertTrue(NodeUtil.hasCatchHandler(NodeUtil.getCatchBlock(tryNode)));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));

    NodeUtil.removeChild(tryNode, finallyBlock);
    assertFalse(NodeUtil.hasFinally(tryNode));

    NodeUtil.maybeAddFinally(tryNode);
    assertTrue(NodeUtil.hasFinally(tryNode));

    Node tryCatchContainer = NodeUtil.getCatchBlock(tryNode);
    assertTrue(NodeUtil.isTryCatchNodeContainer(tryCatchContainer));
    NodeUtil.removeChild(tryNode, tryCatchContainer);
  }

  @Test(timeout = 4000)
  public void testControlStructureChecks() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertFalse(NodeUtil.isControlStructure(IR.exprResult(IR.number(1))));

    Node ifCond = IR.name("c");
    Node ifThen = IR.block();
    Node ifNode = new Node(Token.IF, ifCond, ifThen);
    assertEquals(ifCond, NodeUtil.getConditionExpression(ifNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifThen));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCond));

    Node whileCond = IR.name("c");
    Node whileBody = IR.block();
    Node whileNode = new Node(Token.WHILE, whileCond, whileBody);
    assertEquals(whileCond, NodeUtil.getConditionExpression(whileNode));
    assertEquals(whileBody, NodeUtil.getLoopCodeBlock(whileNode));

    Node doBody = IR.block();
    Node doCond = IR.name("c");
    Node doNode = new Node(Token.DO, doBody, doCond);
    assertEquals(doCond, NodeUtil.getConditionExpression(doNode));
    assertEquals(doBody, NodeUtil.getLoopCodeBlock(doNode));

    Node forIn = new Node(Token.FOR, IR.name("i"), IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testQualifiedNameGenerationAndAnalysis() {
    CodingConvention convention = new DefaultCodingConvention();
    Node simpleName = NodeUtil.newQualifiedNameNode(convention, "simple");
    assertTrue(simpleName.isName());
    assertEquals("simple", simpleName.getString());

    Node qualified = NodeUtil.newQualifiedNameNode(convention, "foo.bar.baz");
    assertTrue(qualified.isGetProp());
    assertEquals("foo.bar.baz", qualified.getQualifiedName());

    Node rootOfQName = NodeUtil.getRootOfQualifiedName(qualified);
    assertTrue(rootOfQName.isName());
    assertEquals("foo", rootOfQName.getString());

    Node thisQName = NodeUtil.newQualifiedNameNode(convention, "this.prop");
    assertTrue(NodeUtil.getRootOfQualifiedName(thisQName).isThis());

    Node declVar = NodeUtil.newQualifiedNameNodeDeclaration(convention, "x", IR.number(1), null);
    assertTrue(declVar.isVar());

    Node declAssign = NodeUtil.newQualifiedNameNodeDeclaration(convention, "obj.x", IR.number(1), null);
    assertTrue(declAssign.isExprResult());
    assertTrue(declAssign.getFirstChild().isAssign());
  }

  @Test(timeout = 4000)
  public void testSideEffectAnalysis() {
    Node num = IR.number(1);
    assertFalse(NodeUtil.mayHaveSideEffects(num));
    assertFalse(NodeUtil.mayEffectMutableState(num));

    Node throwNode = new Node(Token.THROW, IR.name("err"));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node objLit = IR.objectlit();
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));
    assertTrue(NodeUtil.mayEffectMutableState(objLit));

    Node pureCall = IR.call(IR.name("String"), IR.number(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(pureCall));

    Node impureCall = IR.call(IR.name("customFunc"), IR.number(1));
    assertTrue(NodeUtil.functionCallHasSideEffects(impureCall));

    Node pureNew = new Node(Token.NEW, IR.name("Object"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(pureNew));

    Node impureNew = new Node(Token.NEW, IR.name("CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(impureNew));
  }

  @Test(timeout = 4000)
  public void testNodeEvaluationTypeInference() {
    Node addNum = new Node(Token.ADD, IR.number(1), IR.number(2));
    assertTrue(NodeUtil.isNumericResult(addNum));
    assertFalse(NodeUtil.mayBeString(addNum));

    Node addStr = new Node(Token.ADD, IR.string("a"), IR.number(2));
    assertFalse(NodeUtil.isNumericResult(addStr));
    assertTrue(NodeUtil.mayBeString(addStr));

    Node eqNode = new Node(Token.EQ, IR.number(1), IR.number(2));
    assertTrue(NodeUtil.isBooleanResult(eqNode));
    assertFalse(NodeUtil.mayBeString(eqNode));

    Node undefNode = IR.voidNode(IR.number(0));
    assertTrue(NodeUtil.isUndefined(undefNode));
    assertTrue(NodeUtil.isNullOrUndefined(undefNode));
    assertTrue(NodeUtil.isNullOrUndefined(IR.nullNode()));

    Node nanNode = NodeUtil.numberNode(Double.NaN, null);
    assertTrue(NodeUtil.isNaN(nanNode));

    Node divZero = new Node(Token.DIV, IR.number(0), IR.number(0));
    assertTrue(NodeUtil.isNaN(divZero));
  }

  @Test(timeout = 4000)
  public void testAstTreeSearchingAndMapping() {
    Node body = IR.block(
        IR.var(IR.name("x"), IR.number(1)),
        IR.exprResult(IR.assign(IR.name("y"), IR.name("x")))
    );
    Node script = IR.script(body);

    assertTrue(NodeUtil.containsType(script, Token.VAR));
    assertFalse(NodeUtil.containsType(script, Token.THROW));
    assertEquals(2, NodeUtil.getNameReferenceCount(script, "x"));
    assertEquals(1, NodeUtil.getNameReferenceCount(script, "y"));

    Node clone = script.cloneTree();
    Map<Node, Node> mapping = NodeUtil.mapMainToClone(script, clone);
    assertEquals(clone, mapping.get(script));
    NodeUtil.verifyScopeChanges(mapping, script, true, null);
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node stmt1 = IR.exprResult(IR.number(1));
    Node stmt2 = IR.exprResult(IR.number(2));
    Node innerBlock = IR.block(stmt1, stmt2);
    Node outerBlock = IR.block(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(2, outerBlock.getChildCount());
    assertEquals(stmt1, outerBlock.getFirstChild());
    assertEquals(stmt2, outerBlock.getLastChild());

    Node isolatedBlock = IR.block();
    assertFalse(NodeUtil.tryMergeBlock(isolatedBlock));
  }
}