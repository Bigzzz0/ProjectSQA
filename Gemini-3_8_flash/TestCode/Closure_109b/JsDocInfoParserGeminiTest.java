package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.Sets;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Branch Point / Logic Area           | Condition / Test Vector                 | Expected Behavior
 * -------------------------------------------------------------------------------------------------------
 * parseContextTypeExpression (Defect) | token = QMARK ('?') in function(new:?)  | Type node QMARK; valid AST
 * parseContextTypeExpression (Defect) | token = STAR ('*') in function(new:*, n)| Type node STAR; valid AST
 * parseContextTypeExpression (Defect) | token = QMARK ('?') in function(this:?) | Type node QMARK; valid AST
 * parseAnnotation - NG_INJECT         | single vs duplicate                     | Record flag; warn duplicate
 * parseAnnotation - STRUCT / DICT     | @struct, @dict, @struct+@dict           | Record flag; warn incompat
 * parseAnnotation - CONSTRUCTOR/INTER | @constructor vs @interface conflict     | Warn incompat constructor/inter
 * parseAnnotation - DEPRECATED        | with reason vs without reason           | Deprecation reason recorded
 * parseAnnotation - FILE_OVERVIEW     | single vs duplicate overview            | Record overview; warn duplicate
 * parseAnnotation - LICENSE/PRESERVE  | preserve whitespace block               | Appended to fileLevelBuilder
 * parseAnnotation - ENUM              | @enum {type} vs default @enum           | Record parameter type
 * parseAnnotation - EXTENDS/IMPLEMENTS| single, multiple interface, duplicate   | Record extended/base types
 * parseAnnotation - PARAM             | standard, [bracketed], dotted, duplicate| Record param, ignore dotted
 * parseAnnotation - MODIFIES          | this, arguments, param, unknown, dup    | Record modifies; warn unknown
 * parseAnnotation - SUPPRESS          | recognized vs unknown vs duplicate      | Record suppress; warn unknown
 * parseAnnotation - IDGENERATOR       | unique, consistent, stable, mapped, bad | Record idgen kind; warn bad
 * parseAnnotation - TEMPLATE/DISPOSES | valid comma list vs empty               | Record names; warn missing
 * parseAnnotation - ACCESS CONTROLS   | @private, @protected, @public with type | Record visibility and type
 * parseTypeExpression                 | ?T, !T, T?, T!, ?, *, primitives, unions| Correct AST tree structure
 * parseFunctionType                   | context (this/new), params, varargs, ret| Function AST with PARAM_LIST
 * parseArrayType / parseRecordType    | [T, ...T], {a: T, b: T}                 | Correct LB/LC AST hierarchy
 * parseInlineTypeDoc                  | inline type AST extraction              | JSDocInfo with recorded type
 * EOF Guard                           | Unterminated comment (missing */)       | Returns false, logs EOF warning
 * -------------------------------------------------------------------------------------------------------
 */
public class JsDocInfoParserGeminiTest {

  // Test error reporter capturing warnings and errors deterministically.
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
      return new EvaluatorException(message);
    }

    boolean hasWarning(String sub) {
      for (String w : warnings) {
        if (w.contains(sub)) {
          return true;
        }
      }
      return false;
    }
  }

  private Config createConfig(boolean parseDocumentation) {
    Set<String> extraAnnotations = Sets.newHashSet();
    Set<String> extraSuppressions = Sets.newHashSet("checkTypes", "accessControls");
    return new Config(
        extraAnnotations,
        extraSuppressions,
        parseDocumentation,
        LanguageMode.ECMASCRIPT3,
        false);
  }

  private JsDocInfo parse(String comment) {
    return parse(comment, new TestErrorReporter(), false);
  }

  private JsDocInfo parse(String comment, TestErrorReporter errorReporter, boolean parseDoc) {
    Config config = createConfig(parseDoc);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    JsDocInfoParser parser = new JsDocInfoParser(
        stream,
        null,
        null,
        config,
        errorReporter);
    parser.parse();
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  private JSDocInfo parseInline(String comment) {
    Config config = createConfig(false);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    JsDocInfoParser parser = new JsDocInfoParser(
        stream,
        null,
        null,
        config,
        NullErrorReporter.forNewRhino());
    return parser.parseInlineTypeDoc();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Fixes)
  // =========================================================================

  /**
   * Targets Defects4J defect in testStructuralConstructor2:
   * function (new:?) should successfully parse without syntax error warning.
   */
  @Test(timeout = 4000)
  public void testStructuralConstructor2_unknownNew() {
    Node type = JsDocInfoParser.parseTypeString("function (new:?)");
    assertNotNull("function (new:?) must parse successfully", type);
    assertEquals(Token.FUNCTION, type.getType());
    Node context = type.getFirstChild();
    assertEquals(Token.NEW, context.getType());
    assertNotNull(context.getFirstChild());
    assertEquals(Token.QMARK, context.getFirstChild().getType());
  }

  /**
   * Targets Defects4J defect in testStructuralConstructor3:
   * function (new:*, number) should successfully parse without syntax error warning.
   */
  @Test(timeout = 4000)
  public void testStructuralConstructor3_allTypeNewWithParams() {
    Node type = JsDocInfoParser.parseTypeString("function (new:*, number)");
    assertNotNull("function (new:*, number) must parse successfully", type);
    assertEquals(Token.FUNCTION, type.getType());
    Node context = type.getFirstChild();
    assertEquals(Token.NEW, context.getType());
    assertNotNull(context.getFirstChild());
    assertEquals(Token.STAR, context.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testStructuralConstructor_unknownThisContext() {
    Node type = JsDocInfoParser.parseTypeString("function (this:?, string): number");
    assertNotNull("function (this:?, string): number must parse successfully", type);
    assertEquals(Token.FUNCTION, type.getType());
    Node context = type.getFirstChild();
    assertEquals(Token.THIS, context.getType());
    assertEquals(Token.QMARK, context.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testStructuralConstructor_noWarningViaFullParser() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@type {function (new:?)} */", reporter, false);
    assertFalse("Should not emit bad type annotation warning for function (new:?)",
        reporter.hasWarning("Bad type annotation. type not recognized due to syntax error"));
    assertNotNull(info);
    assertNotNull(info.getType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions (Annotations)
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorAndInterfaceMutuallyExclusive() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@constructor\n@interface */", reporter, false);
    assertNotNull(info);
    assertTrue(info.isConstructor());
    assertFalse(info.isInterface());
    assertTrue(reporter.hasWarning("cannot be both"));
  }

  @Test(timeout = 4000)
  public void testStructAndDictMutuallyExclusive() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@struct\n@dict */", reporter, false);
    assertNotNull(info);
    assertTrue(info.makesStructs());
    assertFalse(info.makesDicts());
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testNgInjectAndDuplicates() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@ngInject\n@ngInject */", reporter, false);
    assertNotNull(info);
    assertTrue(info.isNgInject());
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testJaggerAnnotations() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@jaggerInject\n@jaggerModule\n@jaggerProvide */", reporter, false);
    assertNotNull(info);
    assertTrue(info.isJaggerInject());
    assertTrue(info.isJaggerModule());
    assertTrue(info.isJaggerProvide());

    // Duplicate check
    parse("@jaggerInject\n@jaggerInject */", reporter, false);
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testAuthorTag() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@author John Doe <john@example.com> */", reporter, true);
    assertNotNull(info);
    assertEquals(1, info.getAuthors().size());
    assertTrue(info.getAuthors().contains("John Doe <john@example.com>"));

    // Missing author text
    parse("@author\n */", reporter, true);
    assertTrue(reporter.hasWarning("@author"));
  }

  @Test(timeout = 4000)
  public void testDeprecatedTag() {
    JsDocInfo info = parse("@deprecated Use newFunction() instead. */", new TestErrorReporter(), true);
    assertNotNull(info);
    assertTrue(info.isDeprecated());
    assertEquals("Use newFunction() instead.", info.getDeprecationReason());
  }

  @Test(timeout = 4000)
  public void testDescAndMeaning() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@desc Description of element\n@meaning Meaning of element */", reporter, true);
    assertNotNull(info);
    assertEquals("Description of element", info.getDescription());
    assertEquals("Meaning of element", info.getMeaning());

    // Duplicate desc warning
    parse("@desc First\n@desc Second */", reporter, true);
    assertTrue(reporter.hasWarning("@desc"));
  }

  @Test(timeout = 4000)
  public void testFileOverviewAndDuplicateFileOverview() {
    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(true);
    JsDocTokenStream stream = new JsDocTokenStream("@fileoverview File description.\n */");
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, reporter);
    assertTrue(parser.parse());
    JSDocInfo fileDoc = parser.getFileOverviewJSDocInfo();
    assertNotNull(fileDoc);
    assertEquals("File description.", fileDoc.getFileOverview());

    // Second fileoverview warning
    JsDocTokenStream stream2 = new JsDocTokenStream("@fileoverview Second description.\n */");
    JsDocInfoParser parser2 = new JsDocInfoParser(stream2, null, null, config, reporter);
    parser2.setFileOverviewJSDocInfo(fileDoc);
    assertTrue(parser2.parse());
    assertTrue(reporter.hasWarning("@fileoverview"));
  }

  @Test(timeout = 4000)
  public void testLicenseAndPreserve() {
    Config config = createConfig(true);
    JsDocTokenStream stream = new JsDocTokenStream("@preserve Copyright 2023 Acme Corp. */");
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, NullErrorReporter.forNewRhino());
    Node root = IR.script();
    Node.FileLevelJsDocBuilder fileBuilder = root.getJsDocBuilderForNode();
    parser.setFileLevelJsDocBuilder(fileBuilder);
    assertTrue(parser.parse());
  }

  @Test(timeout = 4000)
  public void testEnumParameterType() {
    JsDocInfo info = parse("@enum {string} */");
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());

    // Default enum type is number
    JsDocInfo defaultEnumInfo = parse("@enum */");
    assertNotNull(defaultEnumInfo);
    assertNotNull(defaultEnumInfo.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testExportExposeExternsJavaDispatch() {
    JsDocInfo info = parse("@export\n@expose\n@externs\n@javadispatch */");
    assertNotNull(info);
    assertTrue(info.isExport());
    assertTrue(info.isExpose());
    assertTrue(info.isExterns());
    assertTrue(info.isJavaDispatch());
  }

  @Test(timeout = 4000)
  public void testExtendsAndImplements() {
    JsDocInfo info = parse("@constructor\n@extends {BaseClass}\n@implements {InterfaceOne} */");
    assertNotNull(info);
    assertNotNull(info.getBaseType());
    assertEquals(1, info.getImplementedInterfaceCount());
  }

  @Test(timeout = 4000)
  public void testInterfaceMultipleExtends() {
    JsDocInfo info = parse("@interface\n@extends {SuperInterfaceA}\n@extends {SuperInterfaceB} */");
    assertNotNull(info);
    assertTrue(info.isInterface());
    assertEquals(2, info.getExtendedInterfacesCount());
  }

  @Test(timeout = 4000)
  public void testLendsTag() {
    JsDocInfo info = parse("@lends {MyClass.prototype} */");
    assertNotNull(info);
    assertEquals("MyClass.prototype", info.getLendsName());

    TestErrorReporter reporter = new TestErrorReporter();
    parse("@lends */", reporter, false);
    assertTrue(reporter.hasWarning("missing"));
  }

  @Test(timeout = 4000)
  public void testThrowsTag() {
    JsDocInfo info = parse("@throws {InvalidArgumentException} When arg is null */", new TestErrorReporter(), true);
    assertNotNull(info);
    assertEquals(1, info.getThrownTypes().size());
  }

  @Test(timeout = 4000)
  public void testParamTagVariants() {
    JsDocInfo info = parse(
        "@param {string} valid Normal param\n" +
        "@param [opt=1] Optional param\n" +
        "@param ignored.dotted Dotted property\n */",
        new TestErrorReporter(),
        true);
    assertNotNull(info);
    assertTrue(info.hasParameter("valid"));
    assertTrue(info.hasParameter("opt"));
    assertFalse(info.hasParameter("ignored.dotted"));
    assertEquals("Normal param", info.getParameterDescription("valid"));
  }

  @Test(timeout = 4000)
  public void testDuplicateParamWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@param {string} dup\n@param {number} dup */", reporter, false);
    assertNotNull(info);
    assertTrue(reporter.hasWarning("dup"));
  }

  @Test(timeout = 4000)
  public void testModifiesTag() {
    JsDocInfo info = parse("@param {Object} x\n@modifies {this|arguments|x} */");
    assertNotNull(info);
    Set<String> modifies = info.getModifies();
    assertTrue(modifies.contains("this"));
    assertTrue(modifies.contains("arguments"));
    assertTrue(modifies.contains("x"));

    TestErrorReporter reporter = new TestErrorReporter();
    parse("@modifies {unknownTarget} */", reporter, false);
    assertTrue(reporter.hasWarning("unknownTarget"));
  }

  @Test(timeout = 4000)
  public void testSuppressTag() {
    JsDocInfo info = parse("@suppress {checkTypes, accessControls} */");
    assertNotNull(info);
    Set<String> suppressions = info.getSuppressions();
    assertTrue(suppressions.contains("checkTypes"));
    assertTrue(suppressions.contains("accessControls"));

    TestErrorReporter reporter = new TestErrorReporter();
    parse("@suppress {nonExistentWarning} */", reporter, false);
    assertTrue(reporter.hasWarning("unknown"));
  }

  @Test(timeout = 4000)
  public void testIdGeneratorTags() {
    JsDocInfo uniqueId = parse("@idgenerator */");
    assertNotNull(uniqueId);
    assertTrue(uniqueId.isIdGenerator());

    JsDocInfo consistent = parse("@idgenerator {consistent} */");
    assertNotNull(consistent);
    assertTrue(consistent.isConsistentIdGenerator());

    JsDocInfo stable = parse("@idgenerator {stable} */");
    assertNotNull(stable);
    assertTrue(stable.isStableIdGenerator());

    JsDocInfo mapped = parse("@idgenerator {mapped} */");
    assertNotNull(mapped);
    assertTrue(mapped.isMappedIdGenerator());

    TestErrorReporter reporter = new TestErrorReporter();
    parse("@idgenerator {invalidKind} */", reporter, false);
    assertTrue(reporter.hasWarning("invalidKind"));
  }

  @Test(timeout = 4000)
  public void testTemplateAndDisposesTags() {
    JsDocInfo info = parse("@template T, U\n@disposes {item} */");
    assertNotNull(info);
    assertEquals(2, info.getTemplateTypeNames().size());

    TestErrorReporter reporter = new TestErrorReporter();
    parse("@template \n */", reporter, false);
    assertTrue(reporter.warnings.size() > 0);

    parse("@disposes \n */", reporter, false);
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testVisibilityAndReturnAnnotations() {
    JsDocInfo priv = parse("@private {string} Private property */", new TestErrorReporter(), true);
    assertNotNull(priv);
    assertEquals(Visibility.PRIVATE, priv.getVisibility());
    assertNotNull(priv.getType());

    JsDocInfo prot = parse("@protected */");
    assertNotNull(prot);
    assertEquals(Visibility.PROTECTED, prot.getVisibility());

    JsDocInfo pub = parse("@public */");
    assertNotNull(pub);
    assertEquals(Visibility.PUBLIC, pub.getVisibility());

    JsDocInfo ret = parse("@return {boolean} Description of return. */", new TestErrorReporter(), true);
    assertNotNull(ret);
    assertNotNull(ret.getReturnType());
    assertEquals("Description of return.", ret.getReturnDescription());
  }

  @Test(timeout = 4000)
  public void testDefineAndTypedefAndThis() {
    JsDocInfo defineInfo = parse("@define {boolean} Flag */", new TestErrorReporter(), true);
    assertNotNull(defineInfo);
    assertTrue(defineInfo.isConstant());

    JsDocInfo typedefInfo = parse("@typedef {(string|number)} */");
    assertNotNull(typedefInfo);
    assertNotNull(typedefInfo.getTypedefType());

    JsDocInfo thisInfo = parse("@this {HTMLElement} */");
    assertNotNull(thisInfo);
    assertNotNull(thisInfo.getThisType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Type Grammar Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseTypeString_PrimitivesAndWildcards() {
    assertEquals(Token.STAR, JsDocInfoParser.parseTypeString("*").getType());
    assertEquals(Token.QMARK, JsDocInfoParser.parseTypeString("?").getType());
    assertEquals("null", JsDocInfoParser.parseTypeString("null").getString());
    assertEquals("undefined", JsDocInfoParser.parseTypeString("undefined").getString());
    assertEquals("string", JsDocInfoParser.parseTypeString("string").getString());
    assertEquals("number", JsDocInfoParser.parseTypeString("number").getString());
    assertEquals("boolean", JsDocInfoParser.parseTypeString("boolean").getString());
  }

  @Test(timeout = 4000)
  public void testParseTypeString_PrefixAndPostfixNullability() {
    Node prefixQ = JsDocInfoParser.parseTypeString("?number");
    assertEquals(Token.QMARK, prefixQ.getType());
    assertEquals("number", prefixQ.getFirstChild().getString());

    Node postfixQ = JsDocInfoParser.parseTypeString("number?");
    assertEquals(Token.QMARK, postfixQ.getType());
    assertEquals("number", postfixQ.getFirstChild().getString());

    Node prefixBang = JsDocInfoParser.parseTypeString("!Object");
    assertEquals(Token.BANG, prefixBang.getType());
    assertEquals("Object", prefixBang.getFirstChild().getString());

    Node postfixBang = JsDocInfoParser.parseTypeString("Object!");
    assertEquals(Token.BANG, postfixBang.getType());
    assertEquals("Object", postfixBang.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testParseTypeString_UnionTypes() {
    Node unionPipe = JsDocInfoParser.parseTypeString("(number|string)");
    assertNotNull(unionPipe);
    assertEquals(Token.PIPE, unionPipe.getType());
    assertEquals(2, unionPipe.getChildCount());

    Node unionDoublePipe = JsDocInfoParser.parseTypeString("(number||string)");
    assertNotNull(unionDoublePipe);
    assertEquals(Token.PIPE, unionDoublePipe.getType());

    Node topLevelUnion = JsDocInfoParser.parseTypeString("number|string");
    assertNotNull(topLevelUnion);
    assertEquals(Token.PIPE, topLevelUnion.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeString_ArrayAndRecordTypes() {
    Node arrayNode = JsDocInfoParser.parseTypeString("[number, ...string]");
    assertNotNull(arrayNode);
    assertEquals(Token.LB, arrayNode.getType());

    Node recordNode = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    assertNotNull(recordNode);
    assertEquals(Token.LC, recordNode.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeString_TypeApplication() {
    Node genericArray = JsDocInfoParser.parseTypeString("Array.<string>");
    assertNotNull(genericArray);
    assertEquals("Array", genericArray.getString());
    assertEquals(Token.BLOCK, genericArray.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeString_FunctionSignatures() {
    Node fn = JsDocInfoParser.parseTypeString("function(string, ...[number]): boolean");
    assertNotNull(fn);
    assertEquals(Token.FUNCTION, fn.getType());

    Node fnVoid = JsDocInfoParser.parseTypeString("function(): void");
    assertNotNull(fnVoid);
    assertEquals(Token.VOID, fnVoid.getLastChild().getType());

    Node fnOptional = JsDocInfoParser.parseTypeString("function(string=)");
    assertNotNull(fnOptional);
    assertEquals(Token.FUNCTION, fnOptional.getType());
  }

  @Test(timeout = 4000)
  public void testParseInlineTypeDoc() {
    JSDocInfo info = parseInline("number */");
    assertNotNull(info);
    assertNotNull(info.getType());

    JSDocInfo bracedInfo = parseInline("{string} */");
    assertNotNull(bracedInfo);
    assertNotNull(bracedInfo.getType());

    JSDocInfo invalid = parseInline("*/");
    assertNull(invalid);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths / Malformed Comments
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnexpectedEOFProducesWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createConfig(false);
    JsDocTokenStream stream = new JsDocTokenStream("/** @type {number}");
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, reporter);
    assertFalse("Parser should return false on unexpected EOF", parser.parse());
    assertTrue(reporter.hasWarning("unexpected.eof") || reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testUnknownBadJsDocTag() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfo info = parse("@unknownCustomTag some value */", reporter, false);
    assertNotNull(info);
    assertTrue(reporter.hasWarning("unknownCustomTag"));
  }

  @Test(timeout = 4000)
  public void testMissingClosingBracesInTypes() {
    assertNull(JsDocInfoParser.parseTypeString("{missingCloseBrace"));
    assertNull(JsDocInfoParser.parseTypeString("Array.<string"));
    assertNull(JsDocInfoParser.parseTypeString("[number"));
    assertNull(JsDocInfoParser.parseTypeString("function(number"));
  }

  @Test(timeout = 4000)
  public void testMalformedParamMissingName() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@param {number} */", reporter, false);
    assertTrue(reporter.warnings.size() > 0);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Parser State Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testParserStateResetAndHasParsedInfo() {
    Config config = createConfig(false);
    JsDocTokenStream stream = new JsDocTokenStream("/** @type {string} */");
    JsDocInfoParser parser = new JsDocInfoParser(
        stream,
        null,
        null,
        config,
        NullErrorReporter.forNewRhino());
    assertTrue(parser.parse());
    assertTrue(parser.hasParsedJSDocInfo());

    JSDocInfo firstBuild = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(firstBuild);
    assertNotNull(firstBuild.getType());

    // After reset, subsequent build should not retain stale parsed types
    JSDocInfo secondBuild = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(secondBuild);
    assertNull(secondBuild.getType());
  }

  @Test(timeout = 4000)
  public void testCreateJSTypeExpressionHandling() {
    Config config = createConfig(false);
    JsDocTokenStream stream = new JsDocTokenStream("/** */");
    JsDocInfoParser parser = new JsDocInfoParser(
        stream,
        null,
        null,
        config,
        NullErrorReporter.forNewRhino());
    assertNull(parser.createJSTypeExpression(null));

    Node sampleNode = IR.string("test");
    assertNotNull(parser.createJSTypeExpression(sampleNode));
  }
}