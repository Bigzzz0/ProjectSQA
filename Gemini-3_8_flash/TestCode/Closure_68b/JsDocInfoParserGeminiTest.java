/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * Target: com.google.javascript.jscomp.parsing.JsDocInfoParser
 * Defect Target: Issue 477 - JSDoc single-line constructs ending without newline before closing '星/'
 *                cause text extractors to read past the comment delimiter, triggering an extra warning:
 *                "Unexpected end of file".
 *
 * Major Decision Branches & Boundary Conditions Covered:
 * 1. Type Expressions (parseTypeString, parseTopLevelTypeExpression, parseTypeExpression, etc.):
 *    - Basic types: number, string, boolean, null, undefined, void, *, ?
 *    - Prefix / Postfix modifiers: !T, ?T, T!, T?, T=
 *    - Functions: function(), function(this:T), function(new:T), function(...[T]), function(?, number)
 *    - Type applications: Array.<T>, Object.<K, V>, dotted identifiers (a.b.C)
 *    - Unions: (A|B), A|B, A||B, commas as pipes
 *    - Records: {x: number, y: string}, record fields without type
 *    - Arrays: [number, string], varargs in array [...number]
 * 2. Annotation Handling (State.SEARCHING_ANNOTATION vs State.SEARCHING_NEWLINE):
 *    - Documentation vs Non-Documentation mode (config.parseJsDocDocumentation = true/false)
 *    - @author, @see, @template, @version: single-line blocks, empty argument checks
 *    - @param: bracketed parameters [foo], defaults [foo=bar], dotted parameter properties, rest/optional
 *    - @return: with type, without type (defaults to ?), with return description
 *    - @throws: with type, without type, with description
 *    - @suppress & @modifies: piped syntax {a|b}, unknown suppressions/modifiers, duplicate checking
 *    - @fileoverview & @license/@preserve: WhitespaceOption.TRIM vs PRESERVE, FileLevelJsDocBuilder
 *    - @enum, @const, @constructor, @interface, @extends, @implements, @lends
 *    - Visibility: @private, @protected, @public
 * 3. Error Reporter Warning Paths:
 *    - Duplicate annotations (e.g. duplicate @desc, @fileoverview, @version, @template, @implements)
 *    - Incompatible type specifications (e.g. constructor + interface)
 *    - Missing closing delimiters: missing '}', missing ']', missing ')', missing '>'
 * 4. Boundary & Defensive Paths:
 *    - Unexpected EOF handling and stream resets
 *    - Null / empty string tokens and whitespace edge cases in trimEnd()
 * ------------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.ast.Comment;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class JsDocInfoParserGeminiTest {

  private TestErrorReporter errorReporter;

  private static class TestErrorReporter implements ErrorReporter {
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> errors = new ArrayList<String>();

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
      return new EvaluatorException(message);
    }

    public List<String> getWarnings() {
      return warnings;
    }

    public boolean hasWarning(String subString) {
      for (String w : warnings) {
        if (w.contains(subString)) {
          return true;
        }
      }
      return false;
    }
  }

  private JSDocInfo parse(String comment) {
    return parse(comment, true, new HashSet<String>(), new HashSet<String>());
  }

  private JSDocInfo parse(String comment, boolean parseDocumentation) {
    return parse(comment, parseDocumentation, new HashSet<String>(), new HashSet<String>());
  }

  private JSDocInfo parse(String comment, boolean parseDocumentation,
                         Set<String> extraAnnotations, Set<String> extraSuppressions) {
    errorReporter = new TestErrorReporter();
    Config config = new Config(
        extraAnnotations,
        extraSuppressions,
        parseDocumentation,
        LanguageMode.ECMASCRIPT3,
        false);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    JsDocInfoParser parser = new JsDocInfoParser(
        stream,
        null,
        "testcode",
        config,
        errorReporter);
    parser.parse();
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  private JsDocInfoParser createParser(String comment, boolean parseDoc) {
    errorReporter = new TestErrorReporter();
    Config config = new Config(
        new HashSet<String>(),
        new HashSet<String>(),
        parseDoc,
        LanguageMode.ECMASCRIPT3,
        false);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    return new JsDocInfoParser(stream, null, "testcode", config, errorReporter);
  }

  /* =========================================================================================
   * Partition A: Core Functional Logic & State Transitions
   * ========================================================================================= */

  @Test(timeout = 4000)
  public void testParseTypeStringBasicPrimitives() {
    Node numNode = JsDocInfoParser.parseTypeString("number");
    assertNotNull(numNode);
    assertEquals(Token.NAME, numNode.getType());
    assertEquals("number", numNode.getString());

    Node strNode = JsDocInfoParser.parseTypeString("string");
    assertNotNull(strNode);
    assertEquals("string", strNode.getString());

    Node nullNode = JsDocInfoParser.parseTypeString("null");
    assertNotNull(nullNode);
    assertEquals(Token.NAME, nullNode.getType());
    assertEquals("null", nullNode.getString());

    Node undefinedNode = JsDocInfoParser.parseTypeString("undefined");
    assertNotNull(undefinedNode);
    assertEquals("undefined", undefinedNode.getString());

    Node starNode = JsDocInfoParser.parseTypeString("*");
    assertNotNull(starNode);
    assertEquals(Token.STAR, starNode.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringModifiers() {
    Node qmarkUnknown = JsDocInfoParser.parseTypeString("?");
    assertNotNull(qmarkUnknown);
    assertEquals(Token.QMARK, qmarkUnknown.getType());
    assertFalse(qmarkUnknown.hasChildren());

    Node nullableNum = JsDocInfoParser.parseTypeString("?number");
    assertNotNull(nullableNum);
    assertEquals(Token.QMARK, nullableNum.getType());
    assertTrue(nullableNum.hasChildren());
    assertEquals("number", nullableNum.getFirstChild().getString());

    Node nonNullNum = JsDocInfoParser.parseTypeString("!number");
    assertNotNull(nonNullNum);
    assertEquals(Token.BANG, nonNullNum.getType());
    assertEquals("number", nonNullNum.getFirstChild().getString());

    Node postfixQmark = JsDocInfoParser.parseTypeString("number?");
    assertNotNull(postfixQmark);
    assertEquals(Token.QMARK, postfixQmark.getType());

    Node postfixBang = JsDocInfoParser.parseTypeString("number!");
    assertNotNull(postfixBang);
    assertEquals(Token.BANG, postfixBang.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringUnionAndList() {
    Node unionTopLevel = JsDocInfoParser.parseTypeString("number|string");
    assertNotNull(unionTopLevel);
    assertEquals(Token.PIPE, unionTopLevel.getType());
    assertEquals(2, unionTopLevel.getChildCount());

    Node unionParens = JsDocInfoParser.parseTypeString("(number|boolean|string)");
    assertNotNull(unionParens);
    assertEquals(Token.PIPE, unionParens.getType());
    assertEquals(3, unionParens.getChildCount());

    Node unionDoublePipe = JsDocInfoParser.parseTypeString("number||string");
    assertNotNull(unionDoublePipe);
    assertEquals(Token.PIPE, unionDoublePipe.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringArrayAndRecord() {
    Node arrayNode = JsDocInfoParser.parseTypeString("[number, string]");
    assertNotNull(arrayNode);
    assertEquals(Token.LB, arrayNode.getType());
    assertEquals(2, arrayNode.getChildCount());

    Node arrayVarArgs = JsDocInfoParser.parseTypeString("[...number]");
    assertNotNull(arrayVarArgs);
    assertEquals(Token.LB, arrayVarArgs.getType());
    assertEquals(Token.ELLIPSIS, arrayVarArgs.getFirstChild().getType());

    Node recordNode = JsDocInfoParser.parseTypeString("{foo: number, bar: string}");
    assertNotNull(recordNode);
    assertEquals(Token.LC, recordNode.getType());
    Node fieldList = recordNode.getFirstChild();
    assertEquals(Token.LB, fieldList.getType());
    assertEquals(2, fieldList.getChildCount());

    Node recordFieldWithoutType = JsDocInfoParser.parseTypeString("{foo, bar}");
    assertNotNull(recordFieldWithoutType);
    assertEquals(Token.LC, recordFieldWithoutType.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringFunctionTypes() {
    Node fnSimple = JsDocInfoParser.parseTypeString("function(): void");
    assertNotNull(fnSimple);
    assertEquals(Token.FUNCTION, fnSimple.getType());

    Node fnParams = JsDocInfoParser.parseTypeString("function(string, number=): boolean");
    assertNotNull(fnParams);
    assertEquals(Token.FUNCTION, fnParams.getType());

    Node fnContextThis = JsDocInfoParser.parseTypeString("function(this:Object, string): void");
    assertNotNull(fnContextThis);
    assertEquals(Token.FUNCTION, fnContextThis.getType());
    assertEquals(Token.THIS, fnContextThis.getFirstChild().getType());

    Node fnContextNew = JsDocInfoParser.parseTypeString("function(new:Error): void");
    assertNotNull(fnContextNew);
    assertEquals(Token.FUNCTION, fnContextNew.getType());
    assertEquals(Token.NEW, fnContextNew.getFirstChild().getType());

    Node fnVarargs = JsDocInfoParser.parseTypeString("function(...[number]): void");
    assertNotNull(fnVarargs);
    assertEquals(Token.FUNCTION, fnVarargs.getType());

    Node fnBareVarargs = JsDocInfoParser.parseTypeString("function(...): void");
    assertNotNull(fnBareVarargs);
    assertEquals(Token.FUNCTION, fnBareVarargs.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringApplicationAndDotted() {
    Node appNode = JsDocInfoParser.parseTypeString("Array.<string>");
    assertNotNull(appNode);
    assertEquals(Token.NAME, appNode.getType());
    assertEquals("Array", appNode.getString());
    assertTrue(appNode.hasChildren());
    assertEquals(Token.BLOCK, appNode.getFirstChild().getType());

    Node dottedNode = JsDocInfoParser.parseTypeString("goog.ui.Component");
    assertNotNull(dottedNode);
    assertEquals("goog.ui.Component", dottedNode.getString());
  }

  @Test(timeout = 4000)
  public void testBasicStandardAnnotations() {
    String comment = "/**\n"
        + " * @const\n"
        + " * @constructor\n"
        + " * @export\n"
        + " * @externs\n"
        + " * @javadispatch\n"
        + " * @hidden\n"
        + " * @noalias\n"
        + " * @nocompile\n"
        + " * @notypecheck\n"
        + " * @override\n"
        + " * @preservertry\n"
        + " * @noshadow\n"
        + " * @nosideeffects\n"
        + " * @implicitcast\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertTrue(info.isExport());
    assertTrue(info.isJavaDispatch());
    assertTrue(info.isNoAlias());
    assertTrue(info.isNoCompile());
    assertTrue(info.isNoTypeCheck());
    assertTrue(info.isOverride());
    assertTrue(info.hasPreserveTry());
    assertTrue(info.isNoShadow());
    assertTrue(info.isNoSideEffects());
    assertTrue(info.isImplicitCast());
  }

  @Test(timeout = 4000)
  public void testVisibilities() {
    JSDocInfo privateInfo = parse("/** @private */\n");
    assertNotNull(privateInfo);
    assertEquals(Visibility.PRIVATE, privateInfo.getVisibility());

    JSDocInfo protectedInfo = parse("/** @protected */\n");
    assertNotNull(protectedInfo);
    assertEquals(Visibility.PROTECTED, protectedInfo.getVisibility());

    JSDocInfo publicInfo = parse("/** @public */\n");
    assertNotNull(publicInfo);
    assertEquals(Visibility.PUBLIC, publicInfo.getVisibility());
  }

  @Test(timeout = 4000)
  public void testParamAnnotations() {
    String comment = "/**\n"
        + " * @param {string} a Single param\n"
        + " * @param {number=} b Optional param\n"
        + " * @param {...boolean} c Varargs param\n"
        + " * @param [d] Bracketed optional\n"
        + " * @param [e=10] Bracketed with default\n"
        + " * @param ignore.property Ignored subproperty\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.hasParameter("a"));
    assertTrue(info.hasParameter("b"));
    assertTrue(info.hasParameter("c"));
    assertTrue(info.hasParameter("d"));
    assertTrue(info.hasParameter("e"));
    assertFalse(info.hasParameter("ignore.property"));
    assertEquals("Single param", info.getParameterDescription("a"));
  }

  @Test(timeout = 4000)
  public void testReturnAndThrowsAnnotations() {
    String comment = "/**\n"
        + " * @return {boolean} Success status\n"
        + " * @throws {Error} When failed\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertNotNull(info.getReturnType());
    assertEquals("Success status", info.getReturnDescription());
    List<JSTypeExpression> thrown = info.getThrownTypes();
    assertEquals(1, thrown.size());
    assertEquals("When failed", info.getThrowsDescription(thrown.get(0)));
  }

  @Test(timeout = 4000)
  public void testReturnWithoutTypeAnnotation() {
    String comment = "/**\n"
        + " * @return Returns something without explicit type\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertNotNull(info.getReturnType());
    assertEquals(Token.QMARK, info.getReturnType().getRoot().getType());
  }

  @Test(timeout = 4000)
  public void testFileOverviewAndDescriptions() {
    String comment = "/**\n"
        + " * Top level description block.\n"
        + " * More description.\n"
        + " * @desc Description of item.\n"
        + " * @meaning Meaning of item.\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.getBlockDescription().contains("Top level description block."));
    assertEquals("Description of item.", info.getDescription());
    assertEquals("Meaning of item.", info.getMeaning());
  }

  @Test(timeout = 4000)
  public void testAuthorSeeVersionTemplateDeprecated() {
    String comment = "/**\n"
        + " * @author Alice\n"
        + " * @see http://example.com\n"
        + " * @version 2.1\n"
        + " * @template T\n"
        + " * @deprecated Use bar() instead\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    Collection<String> authors = info.getAuthors();
    assertTrue(authors.contains("Alice"));
    Collection<String> refs = info.getReferences();
    assertTrue(refs.contains("http://example.com"));
    assertEquals("2.1", info.getVersion());
    assertEquals("T", info.getTemplateTypeName());
    assertTrue(info.isDeprecated());
    assertEquals("Use bar() instead", info.getDeprecationReason());
  }

  @Test(timeout = 4000)
  public void testExtendsAndImplements() {
    String comment = "/**\n"
        + " * @interface\n"
        + " * @extends {SuperInterface}\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.isInterface());
    assertEquals(1, info.getExtendedInterfacesCount());

    String classComment = "/**\n"
        + " * @constructor\n"
        + " * @extends SuperClass\n"
        + " * @implements {InterfaceOne}\n"
        + " */";
    JSDocInfo classInfo = parse(classComment);
    assertNotNull(classInfo);
    assertTrue(classInfo.isConstructor());
    assertTrue(classInfo.hasBaseType());
    assertEquals(1, classInfo.getImplementedInterfaces().size());
  }

  @Test(timeout = 4000)
  public void testEnumDefineTypedefThisType() {
    String comment = "/**\n"
        + " * @enum {string}\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());

    String typeComment = "/**\n"
        + " * @type {number}\n"
        + " * @this {Window}\n"
        + " */";
    JSDocInfo typeInfo = parse(typeComment);
    assertNotNull(typeInfo);
    assertNotNull(typeInfo.getType());
    assertNotNull(typeInfo.getThisType());

    String typedefComment = "/**\n"
        + " * @typedef {string|number}\n"
        + " */";
    JSDocInfo typedefInfo = parse(typedefComment);
    assertNotNull(typedefInfo);
    assertNotNull(typedefInfo.getTypedefType());

    String defineComment = "/**\n"
        + " * @define {boolean}\n"
        + " */";
    JSDocInfo defineInfo = parse(defineComment);
    assertNotNull(defineInfo);
  }

  @Test(timeout = 4000)
  public void testLendsAnnotation() {
    String comment = "/**\n"
        + " * @lends {Person.prototype}\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertEquals("Person.prototype", info.getLendsName());

    String bracelessComment = "/**\n"
        + " * @lends Person.prototype\n"
        + " */";
    JSDocInfo bracelessInfo = parse(bracelessComment);
    assertNotNull(bracelessInfo);
    assertEquals("Person.prototype", bracelessInfo.getLendsName());
  }

  @Test(timeout = 4000)
  public void testSuppressionsAndModifies() {
    Set<String> validSuppressions = new HashSet<String>();
    validSuppressions.add("checkTypes");
    validSuppressions.add("uselessCode");

    String comment = "/**\n"
        + " * @suppress {checkTypes|uselessCode}\n"
        + " * @param {Object} x\n"
        + " * @modifies {this|arguments|x}\n"
        + " */";
    JSDocInfo info = parse(comment, true, new HashSet<String>(), validSuppressions);
    assertNotNull(info);
    Set<String> supps = info.getSuppressions();
    assertTrue(supps.contains("checkTypes"));
    assertTrue(supps.contains("uselessCode"));

    Set<String> mods = info.getModifies();
    assertTrue(mods.contains("this"));
    assertTrue(mods.contains("arguments"));
    assertTrue(mods.contains("x"));
  }

  /* =========================================================================================
   * Partition B: Boundary Value Analysis (BVA) & Extremes
   * ========================================================================================= */

  @Test(timeout = 4000)
  public void testEmptyAndMinimalJSDoc() {
    JSDocInfo emptyInfo = parse("/** */\n");
    assertNull(emptyInfo);

    JSDocInfo eolOnly = parse("/**\n *\n */\n");
    assertNull(eolOnly);

    JSDocInfo starDoc = parse("/**\n * ***\n */\n");
    assertNotNull(starDoc);
    assertTrue(starDoc.getBlockDescription().contains("**"));
  }

  @Test(timeout = 4000)
  public void testDocumentationDisabledMode() {
    String comment = "/**\n"
        + " * Description to ignore\n"
        + " * @author AuthorToIgnore\n"
        + " * @see SeeToIgnore\n"
        + " * @const\n"
        + " * @return {number} Return description to ignore\n"
        + " * @fileoverview Overview to ignore\n"
        + " */";
    JSDocInfo info = parse(comment, false);
    assertNotNull(info);
    assertTrue(info.isConstant());
    assertNotNull(info.getReturnType());
    assertTrue(info.getAuthors().isEmpty());
    assertTrue(info.getReferences().isEmpty());
    assertNull(info.getReturnDescription());
    assertTrue(info.hasFileOverview());
    assertEquals("", info.getFileOverview());
  }

  @Test(timeout = 4000)
  public void testLicenseAndPreserveWithFileLevelBuilder() {
    String comment = "/**\n"
        + " * @preserve Notice 2023\n"
        + " * Line 2 of notice\n"
        + " */";
    JsDocInfoParser parser = createParser(comment, true);
    Node scriptNode = new Node(Token.SCRIPT);
    Node.FileLevelJsDocBuilder builder = scriptNode.getJsDocBuilderForNode();
    parser.setFileLevelJsDocBuilder(builder);

    assertTrue(parser.parse());
    assertNotNull(scriptNode.getJsDocInfo());
    assertTrue(scriptNode.getJsDocInfo().getLicense().contains("Notice 2023"));
  }

  @Test(timeout = 4000)
  public void testFileOverviewDocInfoHandling() {
    String comment = "/**\n"
        + " * @fileoverview File overview details\n"
        + " */";
    JsDocInfoParser parser = createParser(comment, true);
    assertTrue(parser.parse());
    JSDocInfo fileOverview = parser.getFileOverviewJSDocInfo();
    assertNotNull(fileOverview);
    assertTrue(fileOverview.getFileOverview().contains("File overview details"));
  }

  @Test(timeout = 4000)
  public void testEnumWithoutTypeDefaultsToNumber() {
    String comment = "/**\n"
        + " * @enum\n"
        + " */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());
    assertEquals("number", info.getEnumParameterType().getRoot().getString());
  }

  /* =========================================================================================
   * Partition C: Defect-Targeted Branch Zone (Issue 477 & EOF Delimiter Boundaries)
   * ========================================================================================= */

  /**
   * Targets Defects4J Issue 477:
   * Single line JSDocs or annotations terminating immediately with '* /'
   * without a trailing newline previously caused extractors to consume '* /'
   * and subsequently report an unexpected EOF warning.
   */
  @Test(timeout = 4000)
  public void testIssue477() {
    String[] testComments = new String[] {
        "/** @author John Doe */",
        "/** @see SomeClass */",
        "/** @param {string} foo */",
        "/** @return {string} */",
        "/** @throws {Error} */",
        "/** @deprecated Obsolete */",
        "/** @meaning SpecificMeaning */",
        "/** @desc A short description */",
        "/**\n * @param {string} foo\n */",
        "/**\n * @author John Doe\n * @type {string}\n */"
    };

    for (String comment : testComments) {
      errorReporter = new TestErrorReporter();
      Config config = new Config(
          new HashSet<String>(),
          new HashSet<String>(),
          true,
          LanguageMode.ECMASCRIPT3,
          false);
      JsDocTokenStream stream = new JsDocTokenStream(comment);
      JsDocInfoParser parser = new JsDocInfoParser(
          stream,
          null,
          "testcode",
          config,
          errorReporter);
      parser.parse();
      assertFalse("extra warning: Unexpected end of file on: " + comment,
          errorReporter.hasWarning("Unexpected end of file"));
    }
  }

  @Test(timeout = 4000)
  public void testIssue477ParamWithBracketsAndNoTrailingNewline() {
    String comment = "/** @param {string} [foo=defaultVal] */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.hasParameter("foo"));
    assertFalse("extra warning: Unexpected end of file",
        errorReporter.hasWarning("Unexpected end of file"));
  }

  @Test(timeout = 4000)
  public void testIssue477FileOverviewWithoutTrailingNewline() {
    String comment = "/** @fileoverview Single line overview */";
    JSDocInfo info = parse(comment);
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertFalse("extra warning: Unexpected end of file",
        errorReporter.hasWarning("Unexpected end of file"));
  }

  /* =========================================================================================
   * Partition D: Exception & Defensive Guard Paths (Syntax Errors & Warnings)
   * ========================================================================================= */

  @Test(timeout = 4000)
  public void testBadAnnotationWarns() {
    parse("/** @unknownTag */\n");
    assertTrue(errorReporter.hasWarning("bad.jsdoc.tag"));
  }

  @Test(timeout = 4000)
  public void testAuthorMissingWarns() {
    parse("/** @author \n */\n");
    assertTrue(errorReporter.hasWarning("authormissing"));
  }

  @Test(timeout = 4000)
  public void testSeeMissingWarns() {
    parse("/** @see \n */\n");
    assertTrue(errorReporter.hasWarning("seemissing"));
  }

  @Test(timeout = 4000)
  public void testVersionMissingAndDuplicateWarns() {
    parse("/** @version \n */\n");
    assertTrue(errorReporter.hasWarning("versionmissing"));

    parse("/** @version 1.0\n * @version 2.0\n */\n");
    assertTrue(errorReporter.hasWarning("extraversion"));
  }

  @Test(timeout = 4000)
  public void testTemplateMissingAndDuplicateWarns() {
    parse("/** @template \n */\n");
    assertTrue(errorReporter.hasWarning("templatemissing"));

    parse("/** @template T\n * @template U\n */\n");
    assertTrue(errorReporter.hasWarning("template.at.most.once"));
  }

  @Test(timeout = 4000)
  public void testDuplicateConstAndDescWarns() {
    parse("/** @const\n * @const\n */\n");
    assertTrue(errorReporter.hasWarning("msg.jsdoc.const"));

    parse("/** @desc first\n * @desc second\n */\n");
    assertTrue(errorReporter.hasWarning("desc.extra"));
  }

  @Test(timeout = 4000)
  public void testConstructorAndInterfaceConflictWarns() {
    parse("/** @constructor\n * @interface\n */\n");
    assertTrue(errorReporter.hasWarning("interface.constructor"));
  }

  @Test(timeout = 4000)
  public void testDuplicateFileOverviewWarns() {
    JsDocInfoParser parser = createParser("/** @fileoverview first */\n", true);
    parser.setFileOverviewJSDocInfo(new JSDocInfo());
    parser.parse();
    assertTrue(errorReporter.hasWarning("fileoverview.extra"));
  }

  @Test(timeout = 4000)
  public void testMissingVariableNameInParamWarns() {
    parse("/** @param {number} \n */\n");
    assertTrue(errorReporter.hasWarning("missing.variable.name"));
  }

  @Test(timeout = 4000)
  public void testDuplicateParamNameWarns() {
    parse("/** @param {number} a\n * @param {string} a\n */\n");
    assertTrue(errorReporter.hasWarning("dup.variable.name"));
  }

  @Test(timeout = 4000)
  public void testExtendsWithoutTypeNameWarns() {
    parse("/** @extends \n */\n");
    assertTrue(errorReporter.hasWarning("no.type.name"));
  }

  @Test(timeout = 4000)
  public void testMissingClosingBracesInTypesWarns() {
    parse("/** @type {number \n */\n");
    assertTrue(errorReporter.hasWarning("missing.rc"));

    parse("/** @type {Array.<number \n */\n");
    assertTrue(errorReporter.hasWarning("missing.gt"));

    parse("/** @type {function(number \n */\n");
    assertTrue(errorReporter.hasWarning("missing.rp"));

    parse("/** @type {[number, string \n */\n");
    assertTrue(errorReporter.hasWarning("missing.rb"));
  }

  @Test(timeout = 4000)
  public void testInvalidSuppressionSyntaxWarns() {
    parse("/** @suppress {unknownCheck} */\n");
    assertTrue(errorReporter.hasWarning("suppress.unknown"));

    parse("/** @suppress notInBraces */\n");
    assertTrue(errorReporter.hasWarning("suppress"));
  }

  @Test(timeout = 4000)
  public void testInvalidModifiesSyntaxWarns() {
    parse("/** @modifies {unknownModifier} */\n");
    assertTrue(errorReporter.hasWarning("modifies.unknown"));

    parse("/** @modifies notInBraces */\n");
    assertTrue(errorReporter.hasWarning("modifies"));
  }

  @Test(timeout = 4000)
  public void testUnexpectedEOFReturnsFalse() {
    JsDocInfoParser parser = createParser("/** @type {number", false);
    boolean result = parser.parse();
    assertFalse(result);
    assertTrue(errorReporter.hasWarning("unexpected.eof"));
  }

  /* =========================================================================================
   * Partition E: Object Lifecycle & Contract Integrity
   * ========================================================================================= */

  @Test(timeout = 4000)
  public void testOriginalCommentStringRecordedIfCommentNodePresent() {
    String commentText = "/** @const */";
    JsDocTokenStream stream = new JsDocTokenStream(commentText);
    ErrorReporter reporter = new TestErrorReporter();
    Config config = new Config(
        new HashSet<String>(),
        new HashSet<String>(),
        true,
        LanguageMode.ECMASCRIPT3,
        false);

    Comment commentNode = new Comment(0, commentText.length(), null, commentText);
    JsDocInfoParser parser = new JsDocInfoParser(stream, commentNode, "testcode", config, reporter);
    assertTrue(parser.parse());
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(commentText, info.getOriginalCommentString());
  }

  @Test(timeout = 4000)
  public void testHasParsedJSDocInfoAndReset() {
    String comment = "/** @const */";
    JsDocInfoParser parser = createParser(comment, true);
    assertFalse(parser.hasParsedJSDocInfo());
    assertTrue(parser.parse());
    assertTrue(parser.hasParsedJSDocInfo());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstant());
    assertFalse(parser.hasParsedJSDocInfo());
  }
}