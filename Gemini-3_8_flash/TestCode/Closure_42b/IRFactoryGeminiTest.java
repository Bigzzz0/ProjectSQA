package com.google.javascript.jscomp.parsing;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.parsing.IRFactory
 * Benchmark Ground Truth Defect: Defects4J Closure-81 / ParserTest::testForEach
 * 
 * Major Decision Branches & Condition Matrix:
 * 1. ForInLoop & Language Extensions (Defect Hotspot):
 *    - loopNode.isForEach() == true  --> MUST trigger "unsupported language extension: for each"
 *                                        error and synthesize Token.EXPR_RESULT bare minimum.
 *    - loopNode.isForEach() == false --> Standard Token.FOR AST generation.
 * 2. Directive Parsing (parseDirectives):
 *    - Single / Multiple "use strict" directives encoded onto Script/Function Node and removed.
 *    - Non-directive expressions preserved.
 * 3. Reserved Keywords Policy across Language Modes:
 *    - ECMASCRIPT3: reservedKeywords == null.
 *    - ECMASCRIPT5: ES5_RESERVED_KEYWORDS checked on Name identifiers.
 *    - ECMASCRIPT5_STRICT: ES5_STRICT_RESERVED_KEYWORDS checked.
 * 4. Validation Guards:
 *    - Assignment targets: NAME, GETPROP, GETELEM allowed; others trigger "invalid assignment target".
 *    - Increment/Decrement operands: invalid targets trigger "invalid increment/decrement target".
 *    - Delete operands: only properties and names allowed; others trigger "Invalid delete operand...".
 *    - Unnamed function statements (functionType != FUNCTION_EXPRESSION) trigger "unnamed function statement".
 * 5. ES5 Object Literal Getters / Setters:
 *    - LanguageMode.ECMASCRIPT3 triggers IE warnings ("getters/setters are not supported in Internet Explorer").
 *    - Getter with parameters triggers "getters may not have parameters".
 *    - Setter with != 1 parameter triggers "setters must have exactly one parameter".
 * 6. Lexical & Literal Transformations:
 *    - Unary NEG on NumberLiteral folds directly into negated double value.
 *    - StringLiteral with '\u000B' and "\v" in source maps to Node.SLASH_V property.
 *    - Suspicious block comments (/* @ or \n * @) trigger SUSPICIOUS_COMMENT_WARNING.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.Token.CommentType;
import com.google.javascript.rhino.head.ast.*;
import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IRFactoryGeminiTest {

  private static class RecordingErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<>();
    final List<String> errors = new ArrayList<>();

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

  private Config createConfig(LanguageMode mode, boolean isIdeMode, boolean acceptConst) {
    for (Constructor<?> c : Config.class.getDeclaredConstructors()) {
      c.setAccessible(true);
      Class<?>[] pTypes = c.getParameterTypes();
      Object[] args = new Object[pTypes.length];
      int boolCount = 0;
      for (int i = 0; i < pTypes.length; i++) {
        if (pTypes[i] == Set.class) {
          args[i] = Collections.emptySet();
        } else if (pTypes[i] == boolean.class) {
          if (boolCount == 0) {
            args[i] = isIdeMode;
          } else {
            args[i] = acceptConst;
          }
          boolCount++;
        } else if (pTypes[i] == LanguageMode.class) {
          args[i] = mode;
        } else {
          args[i] = null;
        }
      }
      try {
        return (Config) c.newInstance(args);
      } catch (Exception ignored) {
      }
    }
    throw new IllegalStateException("Unable to construct Config instance reflectively.");
  }

  private Node transformTree(AstRoot root, String sourceString, Config config, ErrorReporter errorReporter) {
    try {
      Method m = Class.forName("com.google.javascript.jscomp.parsing.IRFactory")
          .getDeclaredMethod("transformTree", AstRoot.class,
              Class.forName("com.google.javascript.rhino.jstype.StaticSourceFile"),
              String.class, Config.class, ErrorReporter.class);
      m.setAccessible(true);
      return (Node) m.invoke(null, root, null, sourceString, config, errorReporter);
    } catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof RuntimeException) {
        throw (RuntimeException) target;
      }
      throw new RuntimeException(target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // =========================================================================
  // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Closure-81: testForEach)
  // =========================================================================

  /**
   * Targets the defect in IRFactory.processForInLoop where loopNode.isForEach()
   * is not checked and reported. The expected behavior is an explicit error
   * "unsupported language extension: for each" and returning an EXPR_RESULT.
   */
  @Test(timeout = 4000)
  public void testForEach_TriggersUnsupportedLanguageExtensionDefect() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT3, false, true);

    AstRoot root = new AstRoot();
    ForInLoop forEachLoop = new ForInLoop();
    forEachLoop.setIsForEach(true);
    forEachLoop.setLineno(1);

    VariableDeclaration varDecl = new VariableDeclaration();
    varDecl.setLineno(1);
    VariableInitializer varInit = new VariableInitializer();
    varInit.setLineno(1);
    varInit.setTarget(new Name(0, "item"));
    varDecl.addVariable(varInit);

    forEachLoop.setIterator(varDecl);
    forEachLoop.setIteratedObject(new Name(0, "collection"));
    Block body = new Block();
    body.setLineno(1);
    forEachLoop.setBody(body);

    root.addChild(forEachLoop);

    Node result = transformTree(root, "for each (var item in collection) {}", config, errorReporter);

    assertNotNull("Transformed result should not be null", result);
    // On the defective version, errorReporter.errors is empty because the check is missing!
    assertEquals("Should report exactly 1 error for unsupported 'for each'", 1, errorReporter.errors.size());
    assertTrue("Error message must indicate 'for each' extension failure",
        errorReporter.errors.get(0).contains("unsupported language extension: for each"));

    Node firstChild = result.getFirstChild();
    assertNotNull("Root SCRIPT should contain transformed child", firstChild);
    // On the defective version, Token.FOR is returned instead of Token.EXPR_RESULT
    assertEquals("Transformed 'for each' must produce EXPR_RESULT placeholder",
        Token.EXPR_RESULT, firstChild.getType());
  }

  // =========================================================================
  // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
  // =========================================================================

  @Test(timeout = 4000)
  public void testScriptDirectivesExtraction() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ExpressionStatement exprStmt = new ExpressionStatement();
    StringLiteral directive = new StringLiteral();
    directive.setValue("use strict");
    exprStmt.setExpression(directive);
    root.addChild(exprStmt);

    ExpressionStatement normalStmt = new ExpressionStatement();
    normalStmt.setExpression(new NumberLiteral(42.0));
    root.addChild(normalStmt);

    Node scriptNode = transformTree(root, "'use strict'; 42;", config, errorReporter);

    assertEquals(Token.SCRIPT, scriptNode.getType());
    Set<String> directives = scriptNode.getDirectives();
    assertNotNull("Directives set should not be null", directives);
    assertTrue("Directives should contain 'use strict'", directives.contains("use strict"));
    assertEquals("Only the non-directive statement should remain as child",
        1, scriptNode.getChildCount());
  }

  @Test(timeout = 4000)
  public void testFunctionWithDirectivesAndParameters() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    FunctionNode fn = new FunctionNode();
    fn.setFunctionName(new Name(0, "compute"));
    fn.addParam(new Name(0, "x"));
    fn.addParam(new Name(0, "y"));

    Block body = new Block();
    ExpressionStatement strictStmt = new ExpressionStatement();
    StringLiteral str = new StringLiteral();
    str.setValue("use strict");
    strictStmt.setExpression(str);
    body.addChild(strictStmt);

    ReturnStatement ret = new ReturnStatement();
    InfixExpression add = new InfixExpression(com.google.javascript.rhino.head.Token.ADD,
        new Name(0, "x"), new Name(0, "y"), 0);
    ret.setReturnValue(add);
    body.addChild(ret);

    fn.setBody(body);
    root.addChild(fn);

    Node result = transformTree(root, "function compute(x, y) { 'use strict'; return x + y; }", config, errorReporter);
    Node fnNode = result.getFirstChild();

    assertEquals(Token.FUNCTION, fnNode.getType());
    Node nameNode = fnNode.getFirstChild();
    assertEquals("compute", nameNode.getString());

    Node paramList = nameNode.getNext();
    assertEquals(Token.PARAM_LIST, paramList.getType());
    assertEquals(2, paramList.getChildCount());

    Node bodyNode = paramList.getNext();
    assertEquals(Token.BLOCK, bodyNode.getType());
    assertNotNull("Function body should have directives", bodyNode.getDirectives());
    assertTrue(bodyNode.getDirectives().contains("use strict"));
  }

  @Test(timeout = 4000)
  public void testIfElseTransformation() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    IfStatement ifStmt = new IfStatement();
    ifStmt.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.TRUE));
    ifStmt.setThenPart(new ExpressionStatement(new NumberLiteral(1.0)));
    ifStmt.setElsePart(new ExpressionStatement(new NumberLiteral(2.0)));
    root.addChild(ifStmt);

    Node script = transformTree(root, "if (true) 1; else 2;", config, errorReporter);
    Node ifNode = script.getFirstChild();

    assertEquals(Token.IF, ifNode.getType());
    assertEquals(Token.TRUE, ifNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, ifNode.getFirstChild().getNext().getType());
    assertEquals(Token.BLOCK, ifNode.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testDoWhileAndWhileLoops() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    DoLoop doLoop = new DoLoop();
    doLoop.setBody(new ExpressionStatement(new NumberLiteral(1.0)));
    doLoop.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.FALSE));
    root.addChild(doLoop);

    WhileLoop whileLoop = new WhileLoop();
    whileLoop.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.TRUE));
    whileLoop.setBody(new Block());
    root.addChild(whileLoop);

    Node script = transformTree(root, "do 1; while(false); while(true){}", config, errorReporter);
    assertEquals(Token.DO, script.getFirstChild().getType());
    assertEquals(Token.WHILE, script.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testForInNormalLoop() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ForInLoop forIn = new ForInLoop();
    forIn.setIsForEach(false);
    forIn.setIterator(new Name(0, "k"));
    forIn.setIteratedObject(new Name(0, "obj"));
    forIn.setBody(new Block());
    root.addChild(forIn);

    Node script = transformTree(root, "for (k in obj) {}", config, errorReporter);
    assertEquals(0, errorReporter.errors.size());
    assertEquals(Token.FOR, script.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testBreakAndContinueWithLabels() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    LabeledStatement labeled = new LabeledStatement();
    Label label = new Label(0, 0, "outer");
    labeled.addLabel(label);

    WhileLoop loop = new WhileLoop();
    loop.setCondition(new KeywordLiteral(com.google.javascript.rhino.head.Token.TRUE));
    Block body = new Block();

    BreakStatement brk = new BreakStatement();
    brk.setBreakLabel(new Name(0, "outer"));
    body.addChild(brk);

    ContinueStatement cont = new ContinueStatement();
    cont.setLabel(new Name(0, "outer"));
    body.addChild(cont);

    loop.setBody(body);
    labeled.setStatement(loop);
    root.addChild(labeled);

    Node script = transformTree(root, "outer: while(true) { break outer; continue outer; }", config, errorReporter);
    Node labelNode = script.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals(Token.LABEL_NAME, labelNode.getFirstChild().getType());
  }

  // =========================================================================
  // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
  // =========================================================================

  @Test(timeout = 4000)
  public void testNumberLiteralStringFormatting() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty prop1 = new ObjectProperty();
    prop1.setLeft(new NumberLiteral(10.0));
    prop1.setRight(new StringLiteral());
    obj.addElement(prop1);

    ObjectProperty prop2 = new ObjectProperty();
    prop2.setLeft(new NumberLiteral(10.5));
    prop2.setRight(new StringLiteral());
    obj.addElement(prop2);

    root.addChild(new ExpressionStatement(obj));

    Node script = transformTree(root, "({10: '', 10.5: ''})", config, errorReporter);
    Node objLit = script.getFirstChild().getFirstChild();

    Node key1 = objLit.getFirstChild();
    assertEquals("10", key1.getString());
    assertTrue(key1.getBooleanProp(Node.QUOTED_PROP));

    Node key2 = key1.getNext();
    assertEquals("10.5", key2.getString());
    assertTrue(key2.getBooleanProp(Node.QUOTED_PROP));
  }

  @Test(timeout = 4000)
  public void testVerticalTabStringLiteral() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    String source = "'hello\\vworld'";
    AstRoot root = new AstRoot();
    StringLiteral strLit = new StringLiteral();
    strLit.setValue("hello\u000Bworld");
    strLit.setAbsolutePosition(0);
    strLit.setLength(source.length());
    root.addChild(new ExpressionStatement(strLit));

    Node script = transformTree(root, source, config, errorReporter);
    Node strNode = script.getFirstChild().getFirstChild();

    assertEquals(Token.STRING, strNode.getType());
    assertTrue("Should set SLASH_V prop when \\v is in source", strNode.getBooleanProp(Node.SLASH_V));
  }

  @Test(timeout = 4000)
  public void testUnaryNegationFoldsNumber() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    UnaryExpression neg = new UnaryExpression();
    neg.setType(com.google.javascript.rhino.head.Token.NEG);
    neg.setOperand(new NumberLiteral(25.0));
    root.addChild(new ExpressionStatement(neg));

    Node script = transformTree(root, "-25;", config, errorReporter);
    Node numNode = script.getFirstChild().getFirstChild();

    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(-25.0, numNode.getDouble(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testUnaryPostfixProperty() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    UnaryExpression inc = new UnaryExpression();
    inc.setType(com.google.javascript.rhino.head.Token.INC);
    inc.setIsPostfix(true);
    inc.setOperand(new Name(0, "counter"));
    root.addChild(new ExpressionStatement(inc));

    Node script = transformTree(root, "counter++;", config, errorReporter);
    Node incNode = script.getFirstChild().getFirstChild();

    assertEquals(Token.INC, incNode.getType());
    assertTrue("Postfix unary should have INCRDECR_PROP set", incNode.getBooleanProp(Node.INCRDECR_PROP));
  }

  @Test(timeout = 4000)
  public void testRegExpLiteralWithAndWithoutFlags() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    RegExpLiteral re1 = new RegExpLiteral();
    re1.setValue("abc");
    re1.setFlags("gi");
    root.addChild(new ExpressionStatement(re1));

    RegExpLiteral re2 = new RegExpLiteral();
    re2.setValue("xyz");
    root.addChild(new ExpressionStatement(re2));

    Node script = transformTree(root, "/abc/gi; /xyz/;", config, errorReporter);

    Node n1 = script.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, n1.getType());
    assertEquals("abc", n1.getFirstChild().getString());
    assertEquals("gi", n1.getLastChild().getString());

    Node n2 = script.getLastChild().getFirstChild();
    assertEquals(Token.REGEXP, n2.getType());
    assertEquals(1, n2.getChildCount());
  }

  @Test(timeout = 4000)
  public void testSwitchStatementWithDefaultAndCases() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    SwitchStatement switchStmt = new SwitchStatement();
    switchStmt.setExpression(new Name(0, "val"));

    SwitchCase case1 = new SwitchCase();
    case1.setExpression(new NumberLiteral(1.0));
    case1.addStatement(new BreakStatement());
    switchStmt.addCase(case1);

    SwitchCase defaultCase = new SwitchCase();
    defaultCase.addStatement(new BreakStatement());
    switchStmt.addCase(defaultCase);

    root.addChild(switchStmt);

    Node script = transformTree(root, "switch(val) { case 1: break; default: break; }", config, errorReporter);
    Node switchNode = script.getFirstChild();

    assertEquals(Token.SWITCH, switchNode.getType());
    Node caseNode = switchNode.getFirstChild().getNext();
    assertEquals(Token.CASE, caseNode.getType());
    Node defNode = caseNode.getNext();
    assertEquals(Token.DEFAULT_CASE, defNode.getType());
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyVariations() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    TryStatement tryFinally = new TryStatement();
    tryFinally.setTryBlock(new Block());
    tryFinally.setFinallyBlock(new Block());
    root.addChild(tryFinally);

    Node script = transformTree(root, "try {} finally {}", config, errorReporter);
    Node tryNode = script.getFirstChild();

    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(Token.BLOCK, tryNode.getFirstChild().getType()); // try block
    assertEquals(Token.BLOCK, tryNode.getFirstChild().getNext().getType()); // catch container
    assertEquals(Token.BLOCK, tryNode.getLastChild().getType()); // finally block
  }

  // =========================================================================
  // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
  // =========================================================================

  @Test(timeout = 4000)
  public void testInvalidAssignmentTargetReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    Assignment assign = new Assignment();
    assign.setType(com.google.javascript.rhino.head.Token.ASSIGN);
    assign.setLeft(new NumberLiteral(5.0)); // Invalid LHS target
    assign.setRight(new Name(0, "x"));
    root.addChild(new ExpressionStatement(assign));

    transformTree(root, "5 = x;", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("invalid assignment target"));
  }

  @Test(timeout = 4000)
  public void testInvalidDeleteOperandReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    UnaryExpression del = new UnaryExpression();
    del.setType(com.google.javascript.rhino.head.Token.DELPROP);
    del.setOperand(new NumberLiteral(10.0));
    root.addChild(new ExpressionStatement(del));

    transformTree(root, "delete 10;", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("Invalid delete operand"));
  }

  @Test(timeout = 4000)
  public void testInvalidIncrementDecrementTargetReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    UnaryExpression inc = new UnaryExpression();
    inc.setType(com.google.javascript.rhino.head.Token.INC);
    inc.setOperand(new StringLiteral());
    root.addChild(new ExpressionStatement(inc));

    UnaryExpression dec = new UnaryExpression();
    dec.setType(com.google.javascript.rhino.head.Token.DEC);
    dec.setOperand(new StringLiteral());
    root.addChild(new ExpressionStatement(dec));

    transformTree(root, "'a'++; 'b'--;", config, errorReporter);

    assertEquals(2, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("invalid increment target"));
    assertTrue(errorReporter.errors.get(1).contains("invalid decrement target"));
  }

  @Test(timeout = 4000)
  public void testCatchClauseConditionNotSupported() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    TryStatement tryStmt = new TryStatement();
    tryStmt.setTryBlock(new Block());
    CatchClause catchClause = new CatchClause();
    catchClause.setVarName(new Name(0, "e"));
    catchClause.setCatchCondition(new Name(0, "condition"));
    catchClause.setBody(new Block());
    tryStmt.addCatchClause(catchClause);
    root.addChild(tryStmt);

    transformTree(root, "try {} catch(e if condition) {}", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("Catch clauses are not supported"));
  }

  @Test(timeout = 4000)
  public void testReservedKeywordInES5ReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(new Name(0, "class")));

    transformTree(root, "class;", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("identifier is a reserved word"));
  }

  @Test(timeout = 4000)
  public void testReservedKeywordInES5StrictReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5_STRICT, false, true);

    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(new Name(0, "yield")));

    transformTree(root, "yield;", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("identifier is a reserved word"));
  }

  @Test(timeout = 4000)
  public void testES3GettersAndSettersReportError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT3, false, true);

    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();

    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "x"));
    getter.setRight(new FunctionNode());
    obj.addElement(getter);

    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "y"));
    setter.setRight(new FunctionNode());
    obj.addElement(setter);

    root.addChild(new ExpressionStatement(obj));

    transformTree(root, "({get x() {}, set y(v) {}})", config, errorReporter);

    assertEquals(2, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("getters are not supported in Internet Explorer"));
    assertTrue(errorReporter.errors.get(1).contains("setters are not supported in Internet Explorer"));
  }

  @Test(timeout = 4000)
  public void testGetterWithParamsAndSetterWithoutParamsReportErrors() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ObjectLiteral obj = new ObjectLiteral();

    // Invalid getter with a param
    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "val"));
    FunctionNode fnGetter = new FunctionNode();
    fnGetter.setBody(new Block());
    fnGetter.addParam(new Name(0, "unexpectedArg"));
    getter.setRight(fnGetter);
    obj.addElement(getter);

    // Invalid setter with 0 params
    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "val"));
    FunctionNode fnSetter = new FunctionNode();
    fnSetter.setBody(new Block());
    setter.setRight(fnSetter);
    obj.addElement(setter);

    root.addChild(new ExpressionStatement(obj));

    transformTree(root, "({get val(x) {}, set val() {}})", config, errorReporter);

    assertEquals(2, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("getters may not have parameters"));
    assertTrue(errorReporter.errors.get(1).contains("setters must have exactly one parameter"));
  }

  @Test(timeout = 4000)
  public void testDestructuringAssignmentReportsForbidden() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ArrayLiteral arr = new ArrayLiteral();
    arr.setIsDestructuring(true);
    root.addChild(new ExpressionStatement(arr));

    ObjectLiteral obj = new ObjectLiteral();
    obj.setIsDestructuring(true);
    root.addChild(new ExpressionStatement(obj));

    transformTree(root, "[] = []; ({}) = {};", config, errorReporter);

    assertEquals(2, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("destructuring assignment forbidden"));
    assertTrue(errorReporter.errors.get(1).contains("destructuring assignment forbidden"));
  }

  @Test(timeout = 4000)
  public void testConstKeywordWhenForbiddenReportsError() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    // acceptConstKeyword = false
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);

    AstRoot root = new AstRoot();
    VariableDeclaration constDecl = new VariableDeclaration();
    constDecl.setType(com.google.javascript.rhino.head.Token.CONST);
    VariableInitializer init = new VariableInitializer();
    init.setTarget(new Name(0, "X"));
    constDecl.addVariable(init);
    root.addChild(constDecl);

    transformTree(root, "const X = 1;", config, errorReporter);

    assertEquals(1, errorReporter.errors.size());
    assertTrue(errorReporter.errors.get(0).contains("Unsupported syntax: const"));
  }

  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentsEmitWarning() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    Comment comment1 = new Comment(0, 15, CommentType.BLOCK_COMMENT, "/* @type {number} */");
    Comment comment2 = new Comment(20, 20, CommentType.BLOCK_COMMENT, "/*\n * @param {string} x\n */");
    root.addComment(comment1);
    root.addComment(comment2);

    transformTree(root, "/* @type {number} */\n/*\n * @param {string} x\n */", config, errorReporter);

    assertEquals(2, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Non-JSDoc comment has annotations"));
    assertTrue(errorReporter.warnings.get(1).contains("Non-JSDoc comment has annotations"));
  }

  // =========================================================================
  // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
  // =========================================================================

  @Test(timeout = 4000)
  public void testParenthesizedExpressionPropMaintained() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    ParenthesizedExpression paren = new ParenthesizedExpression(new NumberLiteral(10.0));
    root.addChild(new ExpressionStatement(paren));

    Node script = transformTree(root, "(10);", config, errorReporter);
    Node expr = script.getFirstChild().getFirstChild();

    assertEquals(Boolean.TRUE, expr.getProp(Node.PARENTHESIZED_PROP));
  }

  @Test(timeout = 4000)
  public void testIdeModeLengthTracking() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    // isIdeMode = true
    Config config = createConfig(LanguageMode.ECMASCRIPT5, true, true);

    AstRoot root = new AstRoot();
    NumberLiteral num = new NumberLiteral(123.0);
    num.setLength(3);
    root.addChild(new ExpressionStatement(num));

    Node script = transformTree(root, "123;", config, errorReporter);
    Node numNode = script.getFirstChild().getFirstChild();

    assertEquals(3, numNode.getLength());
  }

  @Test(timeout = 4000)
  public void testEmptyAstRootProducesScript() {
    RecordingErrorReporter errorReporter = new RecordingErrorReporter();
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, true);

    AstRoot root = new AstRoot();
    Node script = transformTree(root, "", config, errorReporter);

    assertNotNull(script);
    assertEquals(Token.SCRIPT, script.getType());
    assertEquals(0, script.getChildCount());
  }
}
