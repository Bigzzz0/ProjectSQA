package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/**
 * White-box tests for PeepholeSubstituteAlternateSyntax.
 *
 * [Branch & Defect Analysis Matrix]
 * - RegExp constructor folding: valid flags, invalid flags, duplicate flags,
 *   empty patterns, too many arguments, Unicode/precedence safety.
 * - Object/Array constructor folding: normalized AST, no-arg/single-arg,
 *   Array(0) versus Array(1).
 * - String(a) -> '' + a on immutable values.
 * - Function.prototype.bind immediate call rewriting.
 * - Comma splitting in EXPR_RESULT when late=false.
 * - TRUE/FALSE minimization when late=true.
 * - NOT simplification: !(a==b) -> a!=b, double negation, De Morgan.
 * - Boolean condition simplification in boolean contexts.
 * - IF simplification: expression blocks, property-assignment guard,
 *   return/else replacement, duplicate return merging.
 * - Return cleanup: return undefined / return void 0.
 * - Undefined NAME -> VOID replacement.
 * - String array literal -> template.split(delimiter) when late=true.
 * - FOR + IF-break join when late=true.
 *
 * The targeted Defects4J issue is a RegExp constructor with duplicate flags:
 * duplicate flags such as "ii" are invalid JavaScript, and must not be folded
 * into a regexp literal because that changes a runtime error into a syntax
 * error.
 */
public class PeepholeSubstituteAlternateSyntaxDeepseekTest {

  private static PeepholeSubstituteAlternateSyntax createPeephole(boolean late) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    PeepholeSubstituteAlternateSyntax peephole =
        new PeepholeSubstituteAlternateSyntax(late);
    peephole.beginTraversal(compiler);
    return peephole;
  }

  private static Node exprResult(Node expr) {
    Node n = new Node(Token.EXPR_RESULT);
    n.addChildToBack(expr);
    return n;
  }

  private static Node block(Node... stmts) {
    Node n = new Node(Token.BLOCK);
    for (Node stmt : stmts) {
      n.addChildToBack(stmt);
    }
    return n;
  }

  private static Node call(Node target, Node... args) {
    Node n = new Node(Token.CALL);
    n.addChildToBack(target);
    for (Node arg : args) {
      n.addChildToBack(arg);
    }
    return n;
  }

  private static Node newNode(Node target, Node... args) {
    Node n = new Node(Token.NEW);
    n.addChildToBack(target);
    for (Node arg : args) {
      n.addChildToBack(arg);
    }
    return n;
  }

  @Test(timeout = 4000)
  public void testIssue925_duplicateRegexpFlagsAreNotFolded() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node regExp = newNode(IR.name("RegExp"), IR.string("a"), IR.string("ii"));
    exprResult(regExp);

    Node result = opt.optimizeSubtree(regExp);

    assertNotEquals("Duplicate flags must not become a regexp literal",
        Token.REGEXP, result.getType());
    assertEquals(Token.CALL, result.getType());
    assertEquals("ii", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testRegExpValidPatternFoldsToLiteral() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node regExp = newNode(IR.name("RegExp"), IR.string("abc"), IR.string("i"));
    exprResult(regExp);

    Node result = opt.optimizeSubtree(regExp);

    assertEquals(Token.REGEXP, result.getType());
    assertEquals("abc", result.getFirstChild().getString());
    assertEquals("i", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testRegExpEmptyPatternDoesNotFold() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node regExp = newNode(IR.name("RegExp"), IR.string(""));
    exprResult(regExp);

    Node result = opt.optimizeSubtree(regExp);

    assertNotEquals(Token.REGEXP, result.getType());
    assertEquals(Token.CALL, result.getType());
    assertEquals("", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testRegExpInvalidFlagsDoNotFold() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node regExp = newNode(IR.name("RegExp"), IR.string("abc"), IR.string("z"));
    exprResult(regExp);

    Node result = opt.optimizeSubtree(regExp);

    assertNotEquals(Token.REGEXP, result.getType());
    assertEquals(Token.CALL, result.getType());
    assertEquals("z", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testRegExpTooManyArgumentsDoesNotFold() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node regExp = newNode(
        IR.name("RegExp"), IR.string("a"), IR.string("i"), IR.string("g"));
    exprResult(regExp);

    Node result = opt.optimizeSubtree(regExp);

    assertNotEquals(Token.REGEXP, result.getType());
    assertEquals(Token.CALL, result.getType());
    assertEquals(3, result.getChildCount());
  }

  @Test(timeout = 4000)
  public void testObjectConstructorFoldsToObjectLiteral() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node objectNode = newNode(IR.name("Object"));
    exprResult(objectNode);

    Node result = opt.optimizeSubtree(objectNode);

    assertEquals(Token.OBJECTLIT, result.getType());
  }

  @Test(timeout = 4000)
  public void testArrayConstructorFoldsWhenSafe() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);

    Node noArgs = newNode(IR.name("Array"));
    exprResult(noArgs);
    assertEquals(Token.ARRAYLIT, opt.optimizeSubtree(noArgs).getType());

    Node oneString = newNode(IR.name("Array"), IR.string("a"));
    exprResult(oneString);
    Node stringResult = opt.optimizeSubtree(oneString);
    assertEquals(Token.ARRAYLIT, stringResult.getType());
    assertEquals("a", stringResult.getFirstChild().getString());

    Node zero = newNode(IR.name("Array"), IR.number(0));
    exprResult(zero);
    Node zeroResult = opt.optimizeSubtree(zero);
    assertEquals(Token.ARRAYLIT, zeroResult.getType());
    assertFalse(zeroResult.hasChildren());
  }

  @Test(timeout = 4000)
  public void testArrayConstructorWithSingleNonZeroNumberDoesNotFold() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node array = newNode(IR.name("Array"), IR.number(1));
    exprResult(array);

    Node result = opt.optimizeSubtree(array);

    assertNotEquals(Token.ARRAYLIT, result.getType());
    assertEquals(Token.CALL, result.getType());
    assertEquals(1.0, result.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testStringCallOnImmutableFoldsToAddition() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node stringCall = call(IR.name("String"), IR.number(5));
    exprResult(stringCall);

    Node result = opt.optimizeSubtree(stringCall);

    assertEquals(Token.ADD, result.getType());
    assertEquals(Token.STRING, result.getFirstChild().getType());
    assertEquals("", result.getFirstChild().getString());
    assertEquals(Token.NUMBER, result.getLastChild().getType());
    assertEquals(5.0, result.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testImmediateBoundFunctionCallFoldedToCall() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node bindExpr = call(
        IR.getprop(IR.name("fn"), IR.string("bind")),
        IR.name("thisObj"),
        IR.name("a"));
    Node outerCall = call(bindExpr);
    exprResult(outerCall);

    Node result = opt.optimizeSubtree(outerCall);

    assertEquals(Token.CALL, result.getType());
    Node target = result.getFirstChild();
    assertEquals(Token.GETPROP, target.getType());
    assertEquals("call", target.getLastChild().getString());
    assertEquals("fn", target.getFirstChild().getString());
    assertEquals("thisObj", target.getNext().getString());
    assertEquals("a", target.getNext().getNext().getString());
  }

  @Test(timeout = 4000)
  public void testCommaExpressionSplitWhenNotLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(false);
    Node comma = new Node(Token.COMMA, IR.name("a"), IR.name("b"));
    Node expr = exprResult(comma);
    Node parent = block(expr);

    Node result = opt.optimizeSubtree(comma);

    assertSame(result, parent.getFirstChild().getFirstChild());
    assertTrue(parent.getFirstChild().getNext().isExprResult());
    assertEquals("a", result.getString());
  }

  @Test(timeout = 4000)
  public void testCommaExpressionNotSplitWhenLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node comma = new Node(Token.COMMA, IR.name("a"), IR.name("b"));
    exprResult(comma);

    Node result = opt.optimizeSubtree(comma);

    assertSame(comma, result);
    assertEquals(Token.COMMA, result.getType());
  }

  @Test(timeout = 4000)
  public void testTrueReducesToNotZeroWhenLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node trueNode = new Node(Token.TRUE);
    exprResult(trueNode);

    Node result = opt.optimizeSubtree(trueNode);

    assertEquals(Token.NOT, result.getType());
    assertEquals(Token.NUMBER, result.getFirstChild().getType());
    assertEquals(0.0, result.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFalseReducesToNotOneWhenLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node falseNode = new Node(Token.FALSE);
    exprResult(falseNode);

    Node result = opt.optimizeSubtree(falseNode);

    assertEquals(Token.NOT, result.getType());
    assertEquals(Token.NUMBER, result.getFirstChild().getType());
    assertEquals(1.0, result.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testNotEqualityBecomesInequality() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node eq = new Node(Token.EQ, IR.name("a"), IR.name("b"));
    Node not = new Node(Token.NOT, eq);
    exprResult(not);

    Node result = opt.optimizeSubtree(not);

    assertEquals(Token.NE, result.getType());
    assertEquals("a", result.getFirstChild().getString());
    assertEquals("b", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testDoubleNegationInConditionSimplifies() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node cond = new Node(Token.NOT, new Node(Token.NOT, IR.name("a")));
    Node whileNode = new Node(Token.WHILE, cond, block());

    opt.optimizeSubtree(whileNode);

    Node newCond = whileNode.getFirstChild();
    assertEquals(Token.NAME, newCond.getType());
    assertEquals("a", newCond.getString());
  }

  @Test(timeout = 4000)
  public void testDeMorganOrInCondition() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node or = new Node(Token.OR, IR.name("x"), IR.name("y"));
    Node cond = new Node(Token.NOT, or);
    Node whileNode = new Node(Token.WHILE, cond, block());

    opt.optimizeSubtree(whileNode);

    Node newCond = whileNode.getFirstChild();
    assertEquals(Token.AND, newCond.getType());
    assertEquals(Token.NOT, newCond.getFirstChild().getType());
    assertEquals(Token.NOT, newCond.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testBooleanOrWithTrueSimplifies() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node cond = new Node(Token.OR, IR.name("x"), new Node(Token.TRUE));
    Node whileNode = new Node(Token.WHILE, cond, block());

    opt.optimizeSubtree(whileNode);

    assertEquals(Token.TRUE, whileNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testHookTrueFalseSimplifiesInBooleanContext() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node hook = new Node(Token.HOOK,
        IR.name("x"), new Node(Token.TRUE), new Node(Token.FALSE));
    Node whileNode = new Node(Token.WHILE, hook, block());

    opt.optimizeSubtree(whileNode);

    Node newCond = whileNode.getFirstChild();
    assertEquals(Token.NAME, newCond.getType());
    assertEquals("x", newCond.getString());
  }

  @Test(timeout = 4000)
  public void testIfWithoutElseFoldsToAnd() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(false);
    Node callFoo = call(IR.name("foo"));
    Node thenBlock = block(exprResult(callFoo));
    Node ifNode = new Node(Token.IF, IR.name("x"), thenBlock);
    Node parent = block(ifNode);

    Node result = opt.optimizeSubtree(ifNode);

    assertTrue(result.isExprResult());
    Node and = result.getFirstChild();
    assertEquals(Token.AND, and.getType());
    assertEquals(Token.NAME, and.getFirstChild().getType());
    assertEquals(Token.CALL, and.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testIfPropertyAssignmentNotFoldedWhenEarly() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(false);
    Node assign = new Node(Token.ASSIGN,
        IR.getprop(IR.name("a"), IR.string("b")), IR.number(1));
    Node thenBlock = block(exprResult(assign));
    Node ifNode = new Node(Token.IF, IR.name("x"), thenBlock);
    Node parent = block(ifNode);

    Node result = opt.optimizeSubtree(ifNode);

    assertSame(ifNode, result);
    assertTrue(parent.getFirstChild().isIf());
  }

  @Test(timeout = 4000)
  public void testIfBothReturnExpressionsBecomesReturnHook() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node thenBlock = block(new Node(Token.RETURN, IR.number(1)));
    Node elseBlock = block(new Node(Token.RETURN, IR.number(2)));
    Node ifNode = new Node(Token.IF, IR.name("x"), thenBlock, elseBlock);
    Node parent = block(ifNode);

    Node result = opt.optimizeSubtree(ifNode);

    assertEquals(Token.RETURN, result.getType());
    Node hook = result.getFirstChild();
    assertEquals(Token.HOOK, hook.getType());
    assertEquals(Token.NAME, hook.getFirstChild().getType());
    assertEquals(Token.NUMBER, hook.getFirstChild().getNext().getType());
    assertEquals(Token.NUMBER, hook.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testDuplicateReturnsInFollowingIfsAreCombined() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node if1 = new Node(Token.IF, IR.name("x"),
        block(new Node(Token.RETURN, IR.number(1))));
    Node if2 = new Node(Token.IF, IR.name("y"),
        block(new Node(Token.RETURN, IR.number(1))));
    Node parent = block(if1, if2);

    Node result = opt.optimizeSubtree(parent);

    assertSame(parent, result);
    assertTrue(parent.hasOneChild());
    Node mergedIf = parent.getFirstChild();
    assertTrue(mergedIf.isIf());
    Node orCond = mergedIf.getFirstChild();
    assertEquals(Token.OR, orCond.getType());
    assertEquals("x", orCond.getFirstChild().getString());
    assertEquals("y", orCond.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testReturnUndefinedReducesToBareReturn() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node ret = new Node(Token.RETURN, IR.name("undefined"));
    block(ret);

    Node result = opt.optimizeSubtree(ret);

    assertSame(ret, result);
    assertFalse(ret.hasChildren());
  }

  @Test(timeout = 4000)
  public void testReturnVoidWithNoSideEffectsReducesToBareReturn() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node voidNode = new Node(Token.VOID, IR.number(0));
    Node ret = new Node(Token.RETURN, voidNode);
    block(ret);

    Node result = opt.optimizeSubtree(ret);

    assertSame(ret, result);
    assertFalse(ret.hasChildren());
  }

  @Test(timeout = 4000)
  public void testUndefinedNameReplacedWithVoid() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node name = IR.name("undefined");
    Node parent = exprResult(name);

    Node result = opt.optimizeSubtree(name);

    assertTrue(result.isVoid());
    assertSame(result, parent.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testStringArrayLiteralFoldsToSplitWhenLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node array = new Node(Token.ARRAYLIT);
    for (String s : new String[]{"a", "b", "c", "d", "e", "f"}) {
      array.addChildToBack(IR.string(s));
    }
    exprResult(array);

    Node result = opt.optimizeSubtree(array);

    assertEquals(Token.CALL, result.getType());
    Node getprop = result.getFirstChild();
    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals("split", getprop.getLastChild().getString());
    assertEquals("", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testJoinForConditionWithIfBreakWhenLate() {
    PeepholeSubstituteAlternateSyntax opt = createPeephole(true);
    Node forNode = new Node(Token.FOR);
    forNode.addChildToBack(new Node(Token.EMPTY));
    forNode.addChildToBack(IR.name("a"));
    forNode.addChildToBack(new Node(Token.EMPTY));

    Node breakNode = new Node(Token.BREAK);
    Node ifBody = block(breakNode);
    Node ifNode = new Node(Token.IF, IR.name("b"), ifBody);
    Node body = block(ifNode);
    forNode.addChildToBack(body);

    Node result = opt.optimizeSubtree(forNode);

    assertSame(forNode, result);
    Node condition = forNode.getFirstChild().getNext();
    assertEquals(Token.AND, condition.getType());
    assertEquals("a", condition.getFirstChild().getString());
    assertEquals(Token.NOT, condition.getLastChild().getType());
    assertFalse(body.hasChildren());
  }
}