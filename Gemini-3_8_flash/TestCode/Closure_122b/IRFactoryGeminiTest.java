package com.google.javascript.jscomp.parsing;

/* [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Target: com.google.javascript.jscomp.parsing.IRFactory
 * Fault Specification (Defects4J):
 *  - testSuspiciousBlockCommentWarning3: "/* \n *@type {number} *\/" -> fails to detect annotation (no space between '*' and '@').
 *  - testSuspiciousBlockCommentWarning4: "/*  @type {number} *\/" -> fails to detect annotation (multiple spaces after '/*').
 *  - testSuspiciousBlockCommentWarning5: "/* \n *   @type {number} *\/" -> fails to detect annotation (multiple spaces after '*').
 *
 * Core Decision Logic & Branches Targeted:
 * 1. handleBlockComment:
 *    - Contains "/* @" vs multiple spaces "/*  @" [DEFECT ZONE]
 *    - Contains "\n * @" vs no space "\n *@' vs multiple spaces "\n *   @" [DEFECT ZONE]
 *    - Harmless comments without annotations -> No warning.
 * 2. parseDirectives / isDirective:
 *    - Token.EXPR_RESULT wrapping StringLiteral in ALLOWED_DIRECTIVES ("use strict") -> Encoded & removed from children.
 *    - Non-directive expressions -> Retained.
 * 3. validateTypeAnnotations & maybeInjectCastNode:
 *    - LP (ParenthesizedExpression) + JSDoc info.hasType() -> Token.CAST node injected.
 *    - Disallowed locations (e.g. Call without define) -> MISPLACED_TYPE_ANNOTATION warning.
 * 4. Property names & keywords across LanguageModes:
 *    - ECMASCRIPT3: Reserved words as property names (e.g. 'delete') -> INVALID_ES3_PROP_NAME warning.
 *    - ECMASCRIPT3: Object literal getters & setters -> GETTER_ERROR_MESSAGE / SETTER_ERROR_MESSAGE errors.
 *    - ECMASCRIPT5 / ES5_STRICT: Reserved keywords as variable names -> Reserved word error.
 *    - ES5 Object literal getters/setters: Getter with parameters or setter without exactly 1 parameter.
 * 5. Unary & Infix expressions:
 *    - Token.NEG with NumberLiteral -> Folded negation.
 *    - Token.DELPROP on non-lvalue -> Invalid delete operand error.
 *    - Token.INC / Token.DEC on non-assignment targets -> Invalid increment/decrement error.
 *    - Token.ASSIGN on invalid target -> Invalid assignment target error.
 * 6. Statements & Control Flow:
 *    - ForInLoop with isForEach=true -> Unsupported language extension error.
 *    - CatchClause with catchCondition -> Unsupported catch condition error.
 *    - FunctionNode: Unnamed statement vs anonymous expression; parameter list handling.
 *    - VariableDeclaration: CONST when acceptConstKeyword=false -> Unsupported syntax error.
 * 7. String literal edge cases:
 *    - Vertical tab '\u000B' with '\v' in source -> Sets Node.SLASH_V.
 * =====================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Token.CommentType;
import com.google.javascript.rhino.head.ast.*;
import com.google.javascript.jscomp.parsing.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IRFactoryGeminiTest {

  private static final String SUSPICIOUS_COMMENT_WARNING =
      "Non-JSDoc comment has annotations. " +
      "Did you mean to start it with '/**'?";

  private static final String MISPLACED_TYPE_ANNOTATION =
      "Type annotations are not allowed here. Are you missing parentheses?";

  private static final String INVALID_ES3_PROP_NAME =
      "Keywords and reserved words are not allowed as unquoted property " +
      "names in older versions of JavaScript. " +
      "If you are targeting newer versions of JavaScript, " +
      "set the appropriate language_in option.";

  private static final String GETTER_ERROR_MESSAGE =
      "getters are not supported in older versions of JavaScript. " +
      "If you are targeting newer versions of JavaScript, " +
      "set the appropriate language_in option.";

  private static final String SETTER_ERROR_MESSAGE =
      "setters are not supported in older versions of JavaScript. " +
      "If you are targeting newer versions of JavaScript, " +
      "set the appropriate language_in option.";

  // Recording ErrorReporter for tracking warnings and errors
  private static class TestErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  // Reflection helper to instantiate package-private IRFactory.transformTree
  private static Node transformTree(AstRoot root, String source, Config config, ErrorReporter reporter) throws Exception {
    Method m = Class.forName("com.google.javascript.jscomp.parsing.IRFactory")
        .getDeclaredMethod("transformTree", AstRoot.class, StaticSourceFile.class, String.class, Config.class, ErrorReporter.class);
    m.setAccessible(true);
    return (Node) m.invoke(null, root, null, source, config, reporter);
  }

  // Reflection helper to create Config across various Defects4J constructors
  private static Config createConfig(Config.LanguageMode mode, boolean isIdeMode, boolean acceptConst) {
    for (Constructor<?> ctor : Config.class.getDeclaredConstructors()) {
      ctor.setAccessible(true);
      Class<?>[] paramTypes = ctor.getParameterTypes();
      Object[] args = new Object[paramTypes.length];
      int boolCount = 0;
      for (int i = 0; i < paramTypes.length; i++) {
        Class<?> pt = paramTypes[i];
        if (pt == Config.LanguageMode.class) {
          args[i] = mode;
        } else if (pt == boolean.class || pt == Boolean.class) {
          if (boolCount == 0) {
            args[i] = isIdeMode;
          } else {
            args[i] = acceptConst;
          }
          boolCount++;
        } else if (pt == Set.class) {
          args[i] = Collections.emptySet();
        } else {
          args[i] = null;
        }
      }
      try {
        return (Config) ctor.newInstance(args);
      } catch (Exception ignored) {
      }
    }
    throw new IllegalStateException("Could not instantiate Config");
  }

  // =========================================================================
  // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J ParserTest::testSuspiciousBlockCommentWarning3
   * Defect: comment has no space between '*' and '@' ("\n *@type").
   */
  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWarning3_noSpaceAfterStar() throws Exception {
    AstRoot root = new AstRoot();
    String commentVal = "/* \n *@type {number} */";
    Comment comment = new Comment(0, commentVal.length(), CommentType.BLOCK_COMMENT, commentVal);
    comment.setLineno(1);
    root.addComment(comment);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT3, false, true);
    transformTree(root, commentVal + "\nvar a = 0;", config, reporter);

    assertTrue("Suspicious comment warning must be emitted for '/* \\n *@type'",
        reporter.warnings.contains(SUSPICIOUS_COMMENT_WARNING));
  }

  /**
   * Targets Defects4J ParserTest::testSuspiciousBlockCommentWarning4
   * Defect: comment has multiple spaces after '/*' ("/*  @type").
   */
  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWarning4_multipleSpacesAfterSlashStar() throws Exception {
    AstRoot root = new AstRoot();
    String commentVal = "/*  @type {number} */";
    Comment comment = new Comment(0, commentVal.length(), CommentType.BLOCK_COMMENT, commentVal);
    comment.setLineno(1);
    root.addComment(comment);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT3, false, true);
    transformTree(root, commentVal + "\nvar a = 0;", config, reporter);

    assertTrue("Suspicious comment warning must be emitted for '/*  @type'",
        reporter.warnings.contains(SUSPICIOUS_COMMENT_WARNING));
  }

  /**
   * Targets Defects4J ParserTest::testSuspiciousBlockCommentWarning5
   * Defect: comment has multiple spaces between '*' and '@' ("\n *   @type").
   */
  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWarning5_multipleSpacesAfterStar() throws Exception {
    AstRoot root = new AstRoot();
    String commentVal = "/* \n *   @type {number} */";
    Comment comment = new Comment(0, commentVal.length(), CommentType.BLOCK_COMMENT, commentVal);
    comment.setLineno(1);
    root.addComment(comment);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT3, false, true);
    transformTree(root, commentVal + "\nvar a = 0;", config, reporter);

    assertTrue("Suspicious comment warning must be emitted for '/* \\n *   @type'",
        reporter.warnings.contains(SUSPICIOUS_COMMENT_WARNING));
  }

  // =========================================================================
  // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
  // =========================================================================

  @Test(timeout = 4000)
  public void testDirectivesParsing() throws Exception {
    AstRoot root = new AstRoot();
    StringLiteral directiveLit = new StringLiteral();
    directiveLit.setValue("use strict");
    ExpressionStatement exprStmt = new ExpressionStatement(directiveLit);
    root.addChildToBack(exprStmt);

    Name varName = new Name(0, "x");
    VariableInitializer init = new VariableInitializer();
    init.setTarget(varName);
    VariableDeclaration varDecl = new VariableDeclaration();
    varDecl.addVariable(init);
    root.addChildToBack(varDecl);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "'use strict'; var x;", config, reporter);

    assertEquals(Token.SCRIPT, result.getType());
    assertNotNull(result.getDirectives());
    assertTrue(result.getDirectives().contains("use strict"));
    // The directive should be removed as a child statement
    assertEquals(1, result.getChildCount());
    assertEquals(Token.VAR, result.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testFileOverviewAndLicenseJsDoc() throws Exception {
    AstRoot root = new AstRoot();
    String docStr = "/** @fileoverview Test overview\n * @license MIT License */";
    Comment jsdoc = new Comment(0, docStr.length(), CommentType.JSDOC, docStr);
    jsdoc.setLineno(1);
    root.addComment(jsdoc);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, docStr + "\nvar x = 1;", config, reporter);

    assertNotNull("Root node must have JSDocInfo attached", result.getJSDocInfo());
    assertTrue(result.getJSDocInfo().hasFileOverview());
    assertNotNull(result.getJSDocInfo().getLicense());
  }

  @Test(timeout = 4000)
  public void testParenthesizedCastTypeAnnotation() throws Exception {
    AstRoot root = new AstRoot();
    Name target = new Name(0, "a");
    ParenthesizedExpression paren = new ParenthesizedExpression(target);
    String doc = "/** @type {string} */";
    Comment jsdoc = new Comment(0, doc.length(), CommentType.JSDOC, doc);
    jsdoc.setLineno(1);
    paren.setJsDocNode(jsdoc);
    root.addChildToBack(new ExpressionStatement(paren));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "/** @type {string} */ (a);", config, reporter);

    Node exprResult = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprResult.getType());
    Node castNode = exprResult.getFirstChild();
    assertEquals(Token.CAST, castNode.getType());
    assertEquals(Token.NAME, castNode.getFirstChild().getType());
    assertEquals("a", castNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnaryFoldingAndPostfixIncrement() throws Exception {
    AstRoot root = new AstRoot();

    // -42 folding
    UnaryExpression neg = new UnaryExpression(com.google.javascript.rhino.head.Token.NEG, 0);
    NumberLiteral num = new NumberLiteral(0, 2);
    num.setNumber(42.0);
    neg.setOperand(num);
    root.addChildToBack(new ExpressionStatement(neg));

    // i++ postfix
    UnaryExpression inc = new UnaryExpression(com.google.javascript.rhino.head.Token.INC, 0);
    inc.setIsPostfix(true);
    inc.setOperand(new Name(0, "i"));
    root.addChildToBack(new ExpressionStatement(inc));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "-42; i++;", config, reporter);

    Node firstStmt = result.getFirstChild();
    assertEquals(Token.NUMBER, firstStmt.getFirstChild().getType());
    assertEquals(-42.0, firstStmt.getFirstChild().getDouble(), 0.0001);

    Node secondStmt = firstStmt.getNext();
    Node incNode = secondStmt.getFirstChild();
    assertEquals(Token.INC, incNode.getType());
    assertTrue(incNode.getBooleanProp(Node.INCRDECR_PROP));
  }

  @Test(timeout = 4000)
  public void testStringLiteralVerticalTabPreservation() throws Exception {
    AstRoot root = new AstRoot();
    StringLiteral strLit = new StringLiteral(8, 4);
    strLit.setValue("\u000B");
    root.addChildToBack(new ExpressionStatement(strLit));

    String source = "var s = '\\v';";
    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, source, config, reporter);

    Node strNode = result.getFirstChild().getFirstChild();
    assertEquals(Token.STRING, strNode.getType());
    assertTrue("Node.SLASH_V property must be true when \\v is in source",
        strNode.getBooleanProp(Node.SLASH_V));
  }

  @Test(timeout = 4000)
  public void testControlFlowStructures() throws Exception {
    AstRoot root = new AstRoot();

    // if (true) { ; } else { ; }
    IfStatement ifStmt = new IfStatement();
    ifStmt.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.TRUE));
    ifStmt.setThenPart(new EmptyStatement());
    ifStmt.setElsePart(new EmptyStatement());
    root.addChildToBack(ifStmt);

    // while (true) { break; }
    WhileLoop whileLoop = new WhileLoop();
    whileLoop.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.TRUE));
    Block whileBody = new Block();
    whileBody.addChildToBack(new BreakStatement());
    whileLoop.setBody(whileBody);
    root.addChildToBack(whileLoop);

    // do { continue; } while (false);
    DoLoop doLoop = new DoLoop();
    doLoop.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.FALSE));
    Block doBody = new Block();
    doBody.addChildToBack(new ContinueStatement());
    doLoop.setBody(doBody);
    root.addChildToBack(doLoop);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "if(true);else; while(true)break; do continue; while(false);", config, reporter);

    Node ifNode = result.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(Token.TRUE, ifNode.getFirstChild().getType());

    Node whileNode = ifNode.getNext();
    assertEquals(Token.WHILE, whileNode.getType());

    Node doNode = whileNode.getNext();
    assertEquals(Token.DO, doNode.getType());
  }

  // =========================================================================
  // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & LANGUAGE MODES
  // =========================================================================

  @Test(timeout = 4000)
  public void testEs3DisallowsReservedWordsAsProperties() throws Exception {
    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty prop = new ObjectProperty();
    prop.setLeft(new Name(0, "delete"));
    prop.setRight(new NumberLiteral(0, 1.0));
    obj.addElement(prop);
    root.addChildToBack(new ExpressionStatement(obj));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT3, false, true);
    transformTree(root, "var o = {delete: 1};", config, reporter);

    assertTrue("ES3 mode must warn when keywords are used as unquoted property names",
        reporter.warnings.contains(INVALID_ES3_PROP_NAME));
  }

  @Test(timeout = 4000)
  public void testEs3DisallowsGettersAndSetters() throws Exception {
    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();

    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "g"));
    FunctionNode fnG = new FunctionNode();
    fnG.setBody(new Block());
    getter.setRight(fnG);
    obj.addElement(getter);

    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "s"));
    FunctionNode fnS = new FunctionNode();
    fnS.setBody(new Block());
    setter.setRight(fnS);
    obj.addElement(setter);

    root.addChildToBack(new ExpressionStatement(obj));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT3, false, true);
    transformTree(root, "var o = {get g(){}, set s(v){}};", config, reporter);

    assertTrue(reporter.errors.contains(GETTER_ERROR_MESSAGE));
    assertTrue(reporter.errors.contains(SETTER_ERROR_MESSAGE));
  }

  @Test(timeout = 4000)
  public void testEs5GetterParamAndSetterParamRules() throws Exception {
    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();

    // Invalid: Getter with a parameter
    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "g"));
    FunctionNode fnG = new FunctionNode();
    fnG.addParam(new Name(0, "param"));
    fnG.setBody(new Block());
    getter.setRight(fnG);
    obj.addElement(getter);

    // Invalid: Setter with NO parameters
    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "s"));
    FunctionNode fnS = new FunctionNode();
    fnS.setBody(new Block());
    setter.setRight(fnS);
    obj.addElement(setter);

    root.addChildToBack(new ExpressionStatement(obj));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "var o = {get g(p){}, set s(){}};", config, reporter);

    assertTrue(reporter.errors.contains("getters may not have parameters"));
    assertTrue(reporter.errors.contains("setters must have exactly one parameter"));
  }

  @Test(timeout = 4000)
  public void testReservedWordsInEs5Strict() throws Exception {
    AstRoot root = new AstRoot();
    Name yieldName = new Name(0, "yield");
    root.addChildToBack(new ExpressionStatement(yieldName));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5_STRICT, false, true);
    transformTree(root, "yield;", config, reporter);

    assertTrue(reporter.errors.contains("identifier is a reserved word"));
  }

  // =========================================================================
  // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidAssignmentTarget() throws Exception {
    AstRoot root = new AstRoot();
    Assignment assign = new Assignment(
        com.google.javascript.rhino.head.Token.ASSIGN,
        new NumberLiteral(0, 1.0),
        new NumberLiteral(0, 2.0),
        0);
    root.addChildToBack(new ExpressionStatement(assign));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "1 = 2;", config, reporter);

    assertTrue(reporter.errors.contains("invalid assignment target"));
  }

  @Test(timeout = 4000)
  public void testInvalidDeleteAndIncrementTargets() throws Exception {
    AstRoot root = new AstRoot();

    // delete 5;
    UnaryExpression del = new UnaryExpression(com.google.javascript.rhino.head.Token.DELPROP, 0);
    del.setOperand(new NumberLiteral(0, 5.0));
    root.addChildToBack(new ExpressionStatement(del));

    // 5++;
    UnaryExpression inc = new UnaryExpression(com.google.javascript.rhino.head.Token.INC, 0);
    inc.setOperand(new NumberLiteral(0, 5.0));
    root.addChildToBack(new ExpressionStatement(inc));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "delete 5; 5++;", config, reporter);

    assertTrue(reporter.errors.contains("Invalid delete operand. Only properties can be deleted."));
    assertTrue(reporter.errors.contains("invalid increment target"));
  }

  @Test(timeout = 4000)
  public void testUnsupportedLanguageExtensions() throws Exception {
    AstRoot root = new AstRoot();

    // for each (var x in obj)
    ForInLoop forIn = new ForInLoop();
    forIn.setIsForEach(true);
    forIn.setIterator(new Name(0, "x"));
    forIn.setIteratedObject(new Name(0, "obj"));
    forIn.setBody(new Block());
    root.addChildToBack(forIn);

    // catch (e if cond)
    TryStatement tryStmt = new TryStatement();
    tryStmt.setTryBlock(new Block());
    CatchClause catchClause = new CatchClause();
    catchClause.setVarName(new Name(0, "e"));
    catchClause.setCatchCondition(new Name(0, "cond"));
    catchClause.setBody(new Block());
    tryStmt.addCatchClause(catchClause);
    root.addChildToBack(tryStmt);

    // const unsupported
    VariableDeclaration constDecl = new VariableDeclaration();
    constDecl.setType(com.google.javascript.rhino.head.Token.CONST);
    root.addChildToBack(constDecl);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, false);
    transformTree(root, "for each(x in obj); try{}catch(e if cond){} const a;", config, reporter);

    assertTrue(reporter.errors.contains("unsupported language extension: for each"));
    assertTrue(reporter.errors.contains("Catch clauses are not supported"));
    assertTrue(reporter.errors.contains("Unsupported syntax: const"));
  }

  @Test(timeout = 4000)
  public void testDestructuringAssignmentForbidden() throws Exception {
    AstRoot root = new AstRoot();

    ArrayLiteral arr = new ArrayLiteral();
    arr.setIsDestructuring(true);
    root.addChildToBack(new ExpressionStatement(arr));

    ObjectLiteral obj = new ObjectLiteral();
    obj.setIsDestructuring(true);
    root.addChildToBack(new ExpressionStatement(obj));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "[a, b] = []; ({a, b} = {});", config, reporter);

    assertEquals(2, Collections.frequency(reporter.errors, "destructuring assignment forbidden"));
  }

  @Test(timeout = 4000)
  public void testUnnamedFunctionStatementError() throws Exception {
    AstRoot root = new AstRoot();
    FunctionNode fn = new FunctionNode();
    fn.setFunctionName(null);
    fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
    fn.setBody(new Block());
    root.addChildToBack(fn);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "function() {}", config, reporter);

    assertTrue(reporter.errors.contains("unnamed function statement"));
  }

  @Test(timeout = 4000)
  public void testMisplacedTypeAnnotationWarning() throws Exception {
    AstRoot root = new AstRoot();
    FunctionCall call = new FunctionCall();
    call.setTarget(new Name(0, "alert"));
    String doc = "/** @type {number} */";
    Comment jsdoc = new Comment(0, doc.length(), CommentType.JSDOC, doc);
    jsdoc.setLineno(1);
    call.setJsDocNode(jsdoc);
    root.addChildToBack(new ExpressionStatement(call));

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    transformTree(root, "/** @type {number} */ alert();", config, reporter);

    assertTrue(reporter.warnings.contains(MISPLACED_TYPE_ANNOTATION));
  }

  // =========================================================================
  // PARTITION E: OBJECT LIFECYCLE, LABELS & SWITCH STATEMENTS
  // =========================================================================

  @Test(timeout = 4000)
  public void testSwitchStatementWithCasesAndDefault() throws Exception {
    AstRoot root = new AstRoot();
    SwitchStatement switchStmt = new SwitchStatement();
    switchStmt.setExpression(new Name(0, "x"));

    SwitchCase case1 = new SwitchCase();
    case1.setExpression(new NumberLiteral(0, 1.0));
    case1.addStatement(new BreakStatement());
    switchStmt.addCase(case1);

    SwitchCase defaultCase = new SwitchCase();
    defaultCase.setExpression(null); // default case
    defaultCase.addStatement(new EmptyStatement());
    switchStmt.addCase(defaultCase);

    root.addChildToBack(switchStmt);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "switch(x) { case 1: break; default: ; }", config, reporter);

    Node swNode = result.getFirstChild();
    assertEquals(Token.SWITCH, swNode.getType());
    assertEquals(Token.NAME, swNode.getFirstChild().getType());
    Node c1 = swNode.getFirstChild().getNext();
    assertEquals(Token.CASE, c1.getType());
    Node cDefault = c1.getNext();
    assertEquals(Token.DEFAULT_CASE, cDefault.getType());
  }

  @Test(timeout = 4000)
  public void testLabeledStatementAndBreakToLabel() throws Exception {
    AstRoot root = new AstRoot();
    LabeledStatement labeled = new LabeledStatement();
    Label label = new Label(0, 3, "foo");
    labeled.addLabel(label);

    BreakStatement brk = new BreakStatement();
    brk.setBreakLabel(new Name(0, "foo"));
    labeled.setStatement(brk);

    root.addChildToBack(labeled);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(Config.LanguageMode.ECMASCRIPT5, false, true);
    Node result = transformTree(root, "foo: break foo;", config, reporter);

    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertEquals("foo", labelName.getString());

    Node brkNode = labelName.getNext();
    assertEquals(Token.BREAK, brkNode.getType());
    assertEquals(Token.LABEL_NAME, brkNode.getFirstChild().getType());
    assertEquals("foo", brkNode.getFirstChild().getString());
  }
}
