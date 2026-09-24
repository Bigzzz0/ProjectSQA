package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.ast.Comment;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Branches & Conditions:
 * 1. Constructor: null vs non-null commentNode, associatedNode, config
 * 2. parse(): SEARCHING_ANNOTATION state transitions, EOC/EOF/EOL handling
 * 3. parseAnnotation(): All annotation types (CONSTRUCTOR, INTERFACE, STRUCT, DICT, etc.)
 * 4. parseTypeExpression(): QMARK, BANG, STAR, LB, LC, LP, STRING branches
 * 5. parseFunctionType(): THIS/NEW context type, parameters, result type
 * 6. parseParamTypeExpressionAnnotation(): ELLIPSIS, EQUALS, RC matching
 * 7. parseSuppressTag(): LC matching, PIPE/COMMA separators, RC matching
 * 8. parseModifiesTag(): Keyword validation, parameter checking
 * 9. parseIdGeneratorTag(): unique/consistent/stable/mapped branches
 * 10. extractMultilineTextualBlock(): PRESERVE/TRIM/SINGLE_LINE options
 * 11. lookAheadForType() and lookAheadForAnnotation(): Character stream lookahead
 * 12. checkExtendedTypes(): Interface vs non-interface recording
 * 
 * Defect Target: testStructuralConstructor2/3 - extra warning on structural constructor
 * with @struct annotation. The bug is in parseAnnotation() for STRUCT/DICT/CONSTRUCTOR
 * where the order of checks and warning generation is incorrect when @struct and
 * @constructor appear together.
 */
public class JsDocInfoParserDeepseekTest {

    // ==================== Helper Methods ====================
    
    private Config createDefaultConfig() {
        return new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            false,
            LanguageMode.ECMASCRIPT3,
            false);
    }
    
    private Config createConfigWithAnnotations(Map<String, Annotation> annotations) {
        return new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            false,
            LanguageMode.ECMASCRIPT3,
            false) {
            @Override
            public Map<String, Annotation> getAnnotationNames() {
                return annotations;
            }
        };
    }
    
    private JsDocInfoParser createParser(String comment, Node associatedNode, Config config) {
        JsDocTokenStream stream = new JsDocTokenStream(comment);
        Comment commentNode = new Comment(1, 1, comment, 0);
        ErrorReporter errorReporter = NullErrorReporter.forNewRhino();
        return new JsDocInfoParser(stream, commentNode, associatedNode, config, errorReporter);
    }
    
    private JsDocInfoParser createParserWithReporter(String comment, ErrorReporter reporter) {
        JsDocTokenStream stream = new JsDocTokenStream(comment);
        Comment commentNode = new Comment(1, 1, comment, 0);
        Config config = createDefaultConfig();
        return new JsDocInfoParser(stream, commentNode, null, config, reporter);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testParseEmptyComment() {
        JsDocInfoParser parser = createParser("/** */", null, createDefaultConfig());
        assertTrue("Empty comment should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
    }
    
    @Test(timeout = 4000)
    public void testParseSimpleTypeAnnotation() {
        JsDocInfoParser parser = createParser("/** @type {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type annotation", info.hasType());
        JSTypeExpression type = info.getType();
        assertNotNull("Type expression should not be null", type);
    }
    
    @Test(timeout = 4000)
    public void testParseConstructorAnnotation() {
        JsDocInfoParser parser = createParser("/** @constructor */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constructor", info.isConstructor());
    }
    
    @Test(timeout = 4000)
    public void testParseInterfaceAnnotation() {
        JsDocInfoParser parser = createParser("/** @interface */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be interface", info.isInterface());
    }
    
    @Test(timeout = 4000)
    public void testParseStructAnnotation() {
        JsDocInfoParser parser = createParser("/** @struct */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be struct", info.makesStructs());
    }
    
    @Test(timeout = 4000)
    public void testParseDictAnnotation() {
        JsDocInfoParser parser = createParser("/** @dict */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be dict", info.makesDicts());
    }
    
    @Test(timeout = 4000)
    public void testParseParamAnnotation() {
        JsDocInfoParser parser = createParser("/** @param {string} name */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertTrue("Parameter should have type", info.getParameterType("name") != null);
    }
    
    @Test(timeout = 4000)
    public void testParseReturnAnnotation() {
        JsDocInfoParser parser = createParser("/** @return {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have return type", info.hasReturnType());
    }
    
    @Test(timeout = 4000)
    public void testParseExtendsAnnotation() {
        JsDocInfoParser parser = createParser("/** @extends {Foo} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have base type", info.hasBaseType());
    }
    
    @Test(timeout = 4000)
    public void testParseImplementsAnnotation() {
        JsDocInfoParser parser = createParser("/** @implements {Bar} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have implemented interface", info.getImplementedInterfacesCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testParsePrivateAnnotation() {
        JsDocInfoParser parser = createParser("/** @private */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be private", info.isPrivate());
    }
    
    @Test(timeout = 4000)
    public void testParseProtectedAnnotation() {
        JsDocInfoParser parser = createParser("/** @protected */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be protected", info.isProtected());
    }
    
    @Test(timeout = 4000)
    public void testParsePublicAnnotation() {
        JsDocInfoParser parser = createParser("/** @public */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be public", info.isPublic());
    }
    
    @Test(timeout = 4000)
    public void testParseDeprecatedAnnotation() {
        JsDocInfoParser parser = createParser("/** @deprecated Use newMethod instead */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be deprecated", info.isDeprecated());
        assertEquals("Should have deprecation reason", "Use newMethod instead", info.getDeprecationReason());
    }
    
    @Test(timeout = 4000)
    public void testParseSuppressAnnotation() {
        JsDocInfoParser parser = createParser("/** @suppress {checkTypes|globalThis} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        Set<String> suppressions = info.getSuppressions();
        assertTrue("Should suppress checkTypes", suppressions.contains("checkTypes"));
        assertTrue("Should suppress globalThis", suppressions.contains("globalThis"));
    }
    
    @Test(timeout = 4000)
    public void testParseEnumAnnotation() {
        JsDocInfoParser parser = createParser("/** @enum {string} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be enum", info.hasEnumParameterType());
    }
    
    @Test(timeout = 4000)
    public void testParseThisAnnotation() {
        JsDocInfoParser parser = createParser("/** @this {Foo} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have this type", info.hasThisType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeDefAnnotation() {
        JsDocInfoParser parser = createParser("/** @typedef {Object} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be typedef", info.hasTypedefType());
    }
    
    @Test(timeout = 4000)
    public void testParseTemplateAnnotation() {
        JsDocInfoParser parser = createParser("/** @template T */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have template type", info.getTemplateTypeNames().contains("T"));
    }
    
    @Test(timeout = 4000)
    public void testParseOverrideAnnotation() {
        JsDocInfoParser parser = createParser("/** @override */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be override", info.isOverride());
    }
    
    @Test(timeout = 4000)
    public void testParseConstAnnotation() {
        JsDocInfoParser parser = createParser("/** @const */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constant", info.isConstant());
    }
    
    @Test(timeout = 4000)
    public void testParseExportAnnotation() {
        JsDocInfoParser parser = createParser("/** @export */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be export", info.isExport());
    }
    
    @Test(timeout = 4000)
    public void testParseFileOverviewAnnotation() {
        JsDocInfoParser parser = createParser("/** @fileoverview This is a file overview. */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have file overview", info.hasFileOverview());
        assertEquals("File overview text", "This is a file overview.", info.getFileOverview());
    }
    
    @Test(timeout = 4000)
    public void testParseAuthorAnnotation() {
        JsDocInfoParser parser = createParser("/** @author John Doe */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have author", info.getAuthors().size() > 0);
        assertEquals("Author name", "John Doe", info.getAuthors().get(0));
    }
    
    @Test(timeout = 4000)
    public void testParseSeeAnnotation() {
        JsDocInfoParser parser = createParser("/** @see MyClass#myMethod */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have reference", info.getReferences().size() > 0);
        assertEquals("Reference", "MyClass#myMethod", info.getReferences().get(0));
    }
    
    @Test(timeout = 4000)
    public void testParseVersionAnnotation() {
        JsDocInfoParser parser = createParser("/** @version 1.0.0 */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Version", "1.0.0", info.getVersion());
    }
    
    @Test(timeout = 4000)
    public void testParseMeaningAnnotation() {
        JsDocInfoParser parser = createParser("/** @meaning myMeaning */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Meaning", "myMeaning", info.getMeaning());
    }
    
    @Test(timeout = 4000)
    public void testParseLendsAnnotation() {
        JsDocInfoParser parser = createParser("/** @lends {MyObject.prototype} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Lends", "MyObject.prototype", info.getLendsName());
    }
    
    @Test(timeout = 4000)
    public void testParseIdGeneratorAnnotation() {
        JsDocInfoParser parser = createParser("/** @idgenerator */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be id generator", info.isIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseIdGeneratorConsistent() {
        JsDocInfoParser parser = createParser("/** @idgenerator {consistent} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be consistent id generator", info.isConsistentIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseIdGeneratorStable() {
        JsDocInfoParser parser = createParser("/** @idgenerator {stable} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be stable id generator", info.isStableIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseIdGeneratorMapped() {
        JsDocInfoParser parser = createParser("/** @idgenerator {mapped} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be mapped id generator", info.isMappedIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseModifiesAnnotation() {
        JsDocInfoParser parser = createParser("/** @modifies {this} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should modify this", info.getModifies().contains("this"));
    }
    
    @Test(timeout = 4000)
    public void testParseDisposesAnnotation() {
        JsDocInfoParser parser = createParser("/** @disposes {param1, param2} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should dispose param1", info.getDisposesParameters().contains("param1"));
        assertTrue("Should dispose param2", info.getDisposesParameters().contains("param2"));
    }
    
    @Test(timeout = 4000)
    public void testParseNgInjectAnnotation() {
        JsDocInfoParser parser = createParser("/** @ngInject */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be ngInject", info.isNgInject());
    }
    
    @Test(timeout = 4000)
    public void testParseJaggerInjectAnnotation() {
        JsDocInfoParser parser = createParser("/** @jaggerInject */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be jaggerInject", info.isJaggerInject());
    }
    
    @Test(timeout = 4000)
    public void testParseJaggerModuleAnnotation() {
        JsDocInfoParser parser = createParser("/** @jaggerModule */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be jaggerModule", info.isJaggerModule());
    }
    
    @Test(timeout = 4000)
    public void testParseJaggerProvideAnnotation() {
        JsDocInfoParser parser = createParser("/** @jaggerProvide */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be jaggerProvide", info.isJaggerProvide());
    }
    
    @Test(timeout = 4000)
    public void testParseWizactionAnnotation() {
        JsDocInfoParser parser = createParser("/** @wizaction */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be wizaction", info.isWizaction());
    }
    
    @Test(timeout = 4000)
    public void testParseExposeAnnotation() {
        JsDocInfoParser parser = createParser("/** @expose */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be expose", info.isExpose());
    }
    
    @Test(timeout = 4000)
    public void testParseExternsAnnotation() {
        JsDocInfoParser parser = createParser("/** @externs */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be externs", info.isExterns());
    }
    
    @Test(timeout = 4000)
    public void testParseNoCompileAnnotation() {
        JsDocInfoParser parser = createParser("/** @noCompile */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be noCompile", info.isNoCompile());
    }
    
    @Test(timeout = 4000)
    public void testParseNoTypeCheckAnnotation() {
        JsDocInfoParser parser = createParser("/** @noTypeCheck */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be noTypeCheck", info.isNoTypeCheck());
    }
    
    @Test(timeout = 4000)
    public void testParseNoShadowAnnotation() {
        JsDocInfoParser parser = createParser("/** @noShadow */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be noShadow", info.isNoShadow());
    }
    
    @Test(timeout = 4000)
    public void testParseNoSideEffectsAnnotation() {
        JsDocInfoParser parser = createParser("/** @noSideEffects */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be noSideEffects", info.isNoSideEffects());
    }
    
    @Test(timeout = 4000)
    public void testParseImplicitCastAnnotation() {
        JsDocInfoParser parser = createParser("/** @implicitCast */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be implicitCast", info.isImplicitCast());
    }
    
    @Test(timeout = 4000)
    public void testParsePreserveTryAnnotation() {
        JsDocInfoParser parser = createParser("/** @preserveTry */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be preserveTry", info.isPreserveTry());
    }
    
    @Test(timeout = 4000)
    public void testParseNoAliasAnnotation() {
        JsDocInfoParser parser = createParser("/** @noAlias */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be noAlias", info.isNoAlias());
    }
    
    @Test(timeout = 4000)
    public void testParseHiddenAnnotation() {
        JsDocInfoParser parser = createParser("/** @hidden */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be hidden", info.isHidden());
    }
    
    @Test(timeout = 4000)
    public void testParseJavaDispatchAnnotation() {
        JsDocInfoParser parser = createParser("/** @javaDispatch */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be javaDispatch", info.isJavaDispatch());
    }
    
    @Test(timeout = 4000)
    public void testParseConsistentIdGeneratorAnnotation() {
        JsDocInfoParser parser = createParser("/** @consistentIdGenerator */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be consistentIdGenerator", info.isConsistentIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseStableIdGeneratorAnnotation() {
        JsDocInfoParser parser = createParser("/** @stableIdGenerator */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be stableIdGenerator", info.isStableIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseDefineAnnotation() {
        JsDocInfoParser parser = createParser("/** @define {boolean} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be define", info.isDefine());
    }
    
    @Test(timeout = 4000)
    public void testParseDescAnnotation() {
        JsDocInfoParser parser = createParser("/** @desc Description text */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Description", "Description text", info.getDescription());
    }
    
    @Test(timeout = 4000)
    public void testParseThrowsAnnotation() {
        JsDocInfoParser parser = createParser("/** @throws {Error} If something goes wrong */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have throw type", info.getThrownTypes().size() > 0);
    }
    
    @Test(timeout = 4000)
    public void testParseMultipleAnnotations() {
        JsDocInfoParser parser = createParser("/** @constructor @param {string} name @return {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constructor", info.isConstructor());
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertTrue("Should have return type", info.hasReturnType());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testParseNullCommentNode() {
        JsDocTokenStream stream = new JsDocTokenStream("/** @type {number} */");
        Config config = createDefaultConfig();
        ErrorReporter reporter = NullErrorReporter.forNewRhino();
        JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, reporter);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseEmptyStringComment() {
        JsDocInfoParser parser = createParser("/** */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseCommentWithOnlyWhitespace() {
        JsDocInfoParser parser = createParser("/**   */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
    }
    
    @Test(timeout = 4000)
    public void testParseCommentWithMultipleLines() {
        JsDocInfoParser parser = createParser("/**\n * @type {number}\n */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseCommentWithStarOnEachLine() {
        JsDocInfoParser parser = createParser("/**\n * @param {string} name\n * @return {number}\n */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertTrue("Should have return type", info.hasReturnType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithNullKeyword() {
        JsDocInfoParser parser = createParser("/** @type {null} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithUndefinedKeyword() {
        JsDocInfoParser parser = createParser("/** @type {undefined} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithStar() {
        JsDocInfoParser parser = createParser("/** @type {*} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithQuestionMark() {
        JsDocInfoParser parser = createParser("/** @type {?} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithBang() {
        JsDocInfoParser parser = createParser("/** @type {!Object} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithArray() {
        JsDocInfoParser parser = createParser("/** @type {Array.<string>} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithUnion() {
        JsDocInfoParser parser = createParser("/** @type {(string|number)} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithRecord() {
        JsDocInfoParser parser = createParser("/** @type {{myNum: number, myString: string}} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithFunction() {
        JsDocInfoParser parser = createParser("/** @type {function(string, number): boolean} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithFunctionThis() {
        JsDocInfoParser parser = createParser("/** @type {function(this:Object, string): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithFunctionNew() {
        JsDocInfoParser parser = createParser("/** @type {function(new:Object, string): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithFunctionVarArgs() {
        JsDocInfoParser parser = createParser("/** @type {function(string, ...[number]): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithFunctionOptional() {
        JsDocInfoParser parser = createParser("/** @type {function(string=): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithNullable() {
        JsDocInfoParser parser = createParser("/** @type {?number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithNonNullable() {
        JsDocInfoParser parser = createParser("/** @type {!number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithPostfixNullable() {
        JsDocInfoParser parser = createParser("/** @type {number?} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeWithPostfixNonNullable() {
        JsDocInfoParser parser = createParser("/** @type {number!} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseParamWithOptionalBrackets() {
        JsDocInfoParser parser = createParser("/** @param {string=} [name] */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
    }
    
    @Test(timeout = 4000)
    public void testParseParamWithDefaultValue() {
        JsDocInfoParser parser = createParser("/** @param {string} [name='default'] */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
    }
    
    @Test(timeout = 4000)
    public void testParseParamWithDotInName() {
        JsDocInfoParser parser = createParser("/** @param {string} obj.prop */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not have parameter with dot", info.hasParameter("obj.prop"));
    }
    
    @Test(timeout = 4000)
    public void testParseExtendsWithMissingType() {
        JsDocInfoParser parser = createParser("/** @extends */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not have base type", info.hasBaseType());
    }
    
    @Test(timeout = 4000)
    public void testParseImplementsWithMissingType() {
        JsDocInfoParser parser = createParser("/** @implements */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Should have no implemented interfaces", 0, info.getImplementedInterfacesCount());
    }
    
    @Test(timeout = 4000)
    public void testParseSuppressWithMissingBraces() {
        JsDocInfoParser parser = createParser("/** @suppress checkTypes */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have empty suppressions", info.getSuppressions().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseModifiesWithMissingBraces() {
        JsDocInfoParser parser = createParser("/** @modifies this */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have empty modifies", info.getModifies().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseTemplateWithEmptyNames() {
        JsDocInfoParser parser = createParser("/** @template */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have no template types", info.getTemplateTypeNames().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseDisposesWithEmptyNames() {
        JsDocInfoParser parser = createParser("/** @disposes */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have no disposes parameters", info.getDisposesParameters().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseAuthorWithEmptyName() {
        JsDocInfoParser parser = createParser("/** @author */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have no authors", info.getAuthors().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseSeeWithEmptyReference() {
        JsDocInfoParser parser = createParser("/** @see */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have no references", info.getReferences().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testParseVersionWithEmptyString() {
        JsDocInfoParser parser = createParser("/** @version */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertNull("Version should be null", info.getVersion());
    }
    
    @Test(timeout = 4000)
    public void testParseMeaningWithEmptyString() {
        JsDocInfoParser parser = createParser("/** @meaning */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertNull("Meaning should be null", info.getMeaning());
    }
    
    @Test(timeout = 4000)
    public void testParseLendsWithMissingType() {
        JsDocInfoParser parser = createParser("/** @lends */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertNull("Lends should be null", info.getLendsName());
    }
    
    @Test(timeout = 4000)
    public void testParseLendsWithMissingRC() {
        JsDocInfoParser parser = createParser("/** @lends {MyObject */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertNull("Lends should be null", info.getLendsName());
    }
    
    @Test(timeout = 4000)
    public void testParseEnumWithoutType() {
        JsDocInfoParser parser = createParser("/** @enum */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have enum parameter type (default number)", info.hasEnumParameterType());
    }
    
    @Test(timeout = 4000)
    public void testParseReturnWithoutType() {
        JsDocInfoParser parser = createParser("/** @return */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have return type (default ?)", info.hasReturnType());
    }
    
    @Test(timeout = 4000)
    public void testParseThisWithoutType() {
        JsDocInfoParser parser = createParser("/** @this */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not have this type", info.hasThisType());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeDefWithoutType() {
        JsDocInfoParser parser = createParser("/** @typedef */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not have typedef type", info.hasTypedefType());
    }
    
    @Test(timeout = 4000)
    public void testParseDefineWithoutType() {
        JsDocInfoParser parser = createParser("/** @define */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertFalse("Should not be define", info.isDefine());
    }
    
    @Test(timeout = 4000)
    public void testParsePrivateWithType() {
        JsDocInfoParser parser = createParser("/** @private {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be private", info.isPrivate());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseProtectedWithType() {
        JsDocInfoParser parser = createParser("/** @protected {string} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be protected", info.isProtected());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParsePublicWithType() {
        JsDocInfoParser parser = createParser("/** @public {boolean} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be public", info.isPublic());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseConstWithType() {
        JsDocInfoParser parser = createParser("/** @const {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constant", info.isConstant());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseInlineTypeDoc() {
        JsDocInfoParser parser = createParser("/** @type {number} */", null, createDefaultConfig());
        JSDocInfo info = parser.parseInlineTypeDoc();
        assertNotNull("Inline type doc should return JSDocInfo", info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseInlineTypeDocWithEmptyType() {
        JsDocInfoParser parser = createParser("/** @type {} */", null, createDefaultConfig());
        JSDocInfo info = parser.parseInlineTypeDoc();
        assertNull("Inline type doc with empty type should return null", info);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeString() {
        Node typeNode = JsDocInfoParser.parseTypeString("number");
        assertNotNull("parseTypeString should return a node", typeNode);
        assertEquals("Type should be number", "number", typeNode.getString());
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithUnion() {
        Node typeNode = JsDocInfoParser.parseTypeString("(string|number)");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithFunction() {
        Node typeNode = JsDocInfoParser.parseTypeString("function(string): boolean");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithRecord() {
        Node typeNode = JsDocInfoParser.parseTypeString("{myNum: number}");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithArray() {
        Node typeNode = JsDocInfoParser.parseTypeString("Array.<string>");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithNullable() {
        Node typeNode = JsDocInfoParser.parseTypeString("?number");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithNonNullable() {
        Node typeNode = JsDocInfoParser.parseTypeString("!number");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithStar() {
        Node typeNode = JsDocInfoParser.parseTypeString("*");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithNull() {
        Node typeNode = JsDocInfoParser.parseTypeString("null");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithUndefined() {
        Node typeNode = JsDocInfoParser.parseTypeString("undefined");
        assertNotNull("parseTypeString should return a node", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithEmpty() {
        Node typeNode = JsDocInfoParser.parseTypeString("");
        assertNull("parseTypeString with empty string should return null", typeNode);
    }
    
    @Test(timeout = 4000)
    public void testParseTypeStringWithInvalid() {
        Node typeNode = JsDocInfoParser.parseTypeString("invalid type syntax @#$");
        assertNull("parseTypeString with invalid syntax should return null", typeNode);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect Target: testStructuralConstructor2 and testStructuralConstructor3
     * 
     * The bug is that when @struct and @constructor appear together, the parser
     * generates an extra warning "Bad type annotation. type not recognized due to syntax error"
     * because the order of checks in parseAnnotation() for STRUCT and CONSTRUCTOR
     * cases causes incorrect state transitions.
     * 
     * Expected behavior: No warnings should be generated for valid @struct @constructor combination.
     */
    @Test(timeout = 4000)
    public void testStructuralConstructorNoExtraWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @struct @constructor */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should be struct", info.makesStructs());
        assertTrue("Should be constructor", info.isConstructor());
        
        // The bug would produce an extra warning here. We assert no warnings.
        String warningStr = warnings.toString();
        assertFalse("Should not have extra type warning: " + warningStr, 
            warningStr.contains("Bad type annotation"));
    }
    
    @Test(timeout = 4000)
    public void testStructuralConstructorWithType() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @struct @constructor @type {Object} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should be struct", info.makesStructs());
        assertTrue("Should be constructor", info.isConstructor());
        assertTrue("Should have type", info.hasType());
        
        String warningStr = warnings.toString();
        assertFalse("Should not have extra type warning: " + warningStr, 
            warningStr.contains("Bad type annotation"));
    }
    
    @Test(timeout = 4000)
    public void testDictConstructorNoExtraWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @dict @constructor */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should be dict", info.makesDicts());
        assertTrue("Should be constructor", info.isConstructor());
        
        String warningStr = warnings.toString();
        assertFalse("Should not have extra type warning: " + warningStr, 
            warningStr.contains("Bad type annotation"));
    }
    
    @Test(timeout = 4000)
    public void testInterfaceConstructorWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @interface @constructor */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should be interface", info.isInterface());
        assertTrue("Should be constructor", info.isConstructor());
        
        // This combination should generate a warning about interface and constructor
        String warningStr = warnings.toString();
        assertTrue("Should warn about interface and constructor", 
            warningStr.contains("interface") && warningStr.contains("constructor"));
    }
    
    @Test(timeout = 4000)
    public void testStructAndDictConflict() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @struct @dict */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        // Only one should be recorded (the last one processed)
        String warningStr = warnings.toString();
        assertTrue("Should warn about incompatible type", warningStr.contains("incompat.type"));
    }
    
    @Test(timeout = 4000)
    public void testMultipleFileOverviewWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @fileoverview First @fileoverview Second */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about extra fileoverview", warningStr.contains("fileoverview.extra"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateParamWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @param {string} name @param {number} name */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate variable name", warningStr.contains("dup.variable.name"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateExtendsWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @extends {Foo} @extends {Bar} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate extends", warningStr.contains("extends.duplicate"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateImplementsWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @implements {Foo} @implements {Foo} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate implements", warningStr.contains("implements.duplicate"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateSuppressWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @suppress {checkTypes} @suppress {checkTypes} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate suppress", warningStr.contains("suppress.duplicate"));
    }
    
    @Test(timeout = 4000)
    public void testUnknownSuppressWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @suppress {unknownWarning} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about unknown suppress", warningStr.contains("suppress.unknown"));
    }
    
    @Test(timeout = 4000)
    public void testUnknownModifiesWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @modifies {unknown} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about unknown modifies", warningStr.contains("modifies.unknown"));
    }
    
    @Test(timeout = 4000)
    public void testUnknownIdGeneratorWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @idgenerator {unknown} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about unknown idgen", warningStr.contains("idgen.unknown"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateIdGeneratorWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @idgenerator @idgenerator */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate idgen", warningStr.contains("idgen.duplicate"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateNgInjectWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @ngInject @ngInject */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about extra ngInject", warningStr.contains("nginject.extra"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateDescWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @desc First @desc Second */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about extra desc", warningStr.contains("desc.extra"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateMeaningWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @meaning First @meaning Second */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about extra meaning", warningStr.contains("meaning.extra"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateVersionWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @version 1.0 @version 2.0 */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about extra version", warningStr.contains("extraversion"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateTemplateWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @template T @template U */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about template at most once", warningStr.contains("template.at.most.once"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateDisposesWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @disposes {p1} @disposes {p2} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about disposes error", warningStr.contains("disposeparameter.error"));
    }
    
    @Test(timeout = 4000)
    public void testDuplicateModifiesWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @modifies {this} @modifies {arguments} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about duplicate modifies", warningStr.contains("modifies.duplicate"));
    }
    
    @Test(timeout = 4000)
    public void testBadJsDocTagWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @unknownTag */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about bad jsdoc tag", warningStr.contains("bad.jsdoc.tag"));
    }
    
    @Test(timeout = 4000)
    public void testMissingVariableNameWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @param {string} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing variable name", warningStr.contains("missing.variable.name"));
    }
    
    @Test(timeout = 4000)
    public void testMissingRCWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {number */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing rc", warningStr.contains("missing.rc"));
    }
    
    @Test(timeout = 4000)
    public void testMissingGTWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {Array.<string} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing gt", warningStr.contains("missing.gt"));
    }
    
    @Test(timeout = 4000)
    public void testMissingLPWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {function string): number} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing lp", warningStr.contains("missing.lp"));
    }
    
    @Test(timeout = 4000)
    public void testMissingRPWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {function(string: number} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing rp", warningStr.contains("missing.rp"));
    }
    
    @Test(timeout = 4000)
    public void testMissingColonWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {function(this Object): void} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing colon", warningStr.contains("missing.colon"));
    }
    
    @Test(timeout = 4000)
    public void testMissingRBWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {Array.<[string} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about missing rb", warningStr.contains("missing.rb"));
    }
    
    @Test(timeout = 4000)
    public void testFunctionVarArgsWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {function(...[number], string): void} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about function varargs", warningStr.contains("function.varargs"));
    }
    
    @Test(timeout = 4000)
    public void testTypeSyntaxWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {invalid type} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about type syntax", warningStr.contains("type.syntax"));
    }
    
    @Test(timeout = 4000)
    public void testUnexpectedEOFWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @type {number", reporter);
        assertFalse("Should fail to parse due to unexpected EOF", parser.parse());
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about unexpected eof", warningStr.contains("unexpected.eof"));
    }
    
    @Test(timeout = 4000)
    public void testEndAnnotationExpectedWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @extends {Foo} extra */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about end annotation expected", warningStr.contains("end.annotation.expected"));
    }
    
    @Test(timeout = 4000)
    public void testNoTypeNameWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @extends {{}} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about no type name", warningStr.contains("no.type.name"));
    }
    
    @Test(timeout = 4000)
    public void testLendsMissingWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @lends {} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about lends missing", warningStr.contains("lends.missing"));
    }
    
    @Test(timeout = 4000)
    public void testLendsIncompatibleWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        // Lends can only be used on an object literal, so using it on a type is incompatible
        JsDocInfoParser parser = createParserWithReporter("/** @type {Object} @lends {Foo} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about lends incompatible", warningStr.contains("lends.incompatible"));
    }
    
    @Test(timeout = 4000)
    public void testOverrideWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @override */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        // @override without a type should generate a warning
        String warningStr = warnings.toString();
        assertTrue("Should warn about override", warningStr.contains("override"));
    }
    
    @Test(timeout = 4000)
    public void testIncompatTypeWarning() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        // Trying to record both @type and @enum should cause incompat type warning
        JsDocInfoParser parser = createParserWithReporter("/** @type {number} @enum {string} */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about incompat type", warningStr.contains("incompat.type"));
    }
    
    @Test(timeout = 4000)
    public void testPreserveAnnotation() {
        JsDocInfoParser parser = createParser("/** @preserve This should be preserved */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
    }
    
    @Test(timeout = 4000)
    public void testLicenseAnnotation() {
        JsDocInfoParser parser = createParser("/** @license MIT */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
    }
    
    @Test(timeout = 4000)
    public void testNotImplementedAnnotation() {
        JsDocInfoParser parser = createParser("/** @notImplemented */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
    }
    
    @Test(timeout = 4000)
    public void testInheritDocAnnotation() {
        JsDocInfoParser parser = createParser("/** @inheritDoc */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should be override", info.isOverride());
    }
    
    @Test(timeout = 4000)
    public void testHasParsedJSDocInfo() {
        JsDocInfoParser parser = createParser("/** @type {number} */", null, createDefaultConfig());
        assertFalse("Should not have parsed JSDocInfo before parsing", parser.hasParsedJSDocInfo());
        parser.parse();
        assertTrue("Should have parsed JSDocInfo after parsing", parser.hasParsedJSDocInfo());
    }
    
    @Test(timeout = 4000)
    public void testGetFileOverviewJSDocInfo() {
        JsDocInfoParser parser = createParser("/** @fileoverview Test */", null, createDefaultConfig());
        parser.parse();
        JSDocInfo fileOverview = parser.getFileOverviewJSDocInfo();
        assertNotNull("File overview JSDocInfo should not be null", fileOverview);
        assertTrue("Should have file overview", fileOverview.hasFileOverview());
    }
    
    @Test(timeout = 4000)
    public void testSetFileOverviewJSDocInfo() {
        JsDocInfoParser parser = createParser("/** @fileoverview First */", null, createDefaultConfig());
        parser.parse();
        JSDocInfo firstOverview = parser.getFileOverviewJSDocInfo();
        assertNotNull("First file overview should not be null", firstOverview);
        
        // Parse another file overview
        JsDocInfoParser parser2 = createParser("/** @fileoverview Second */", null, createDefaultConfig());
        parser2.setFileOverviewJSDocInfo(firstOverview);
        parser2.parse();
        JSDocInfo secondOverview = parser2.getFileOverviewJSDocInfo();
        assertNotNull("Second file overview should not be null", secondOverview);
    }
    
    @Test(timeout = 4000)
    public void testSetFileLevelJsDocBuilder() {
        JsDocInfoParser parser = createParser("/** @license MIT */", null, createDefaultConfig());
        final StringBuilder builder = new StringBuilder();
        Node.FileLevelJsDocBuilder fileBuilder = new Node.FileLevelJsDocBuilder() {
            @Override
            public void append(String text) {
                builder.append(text);
            }
        };
        parser.setFileLevelJsDocBuilder(fileBuilder);
        assertTrue(parser.parse());
        assertTrue("Builder should have appended text", builder.length() > 0);
    }
    
    @Test(timeout = 4000)
    public void testParseWithDocumentationEnabled() {
        Config config = new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            true,  // parseJsDocDocumentation = true
            LanguageMode.ECMASCRIPT3,
            false);
        JsDocInfoParser parser = createParser("/** Description @param {string} name */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertTrue("Should have description", info.getOriginalCommentString() != null);
    }
    
    @Test(timeout = 4000)
    public void testParseWithDocumentationDisabled() {
        Config config = new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            false,  // parseJsDocDocumentation = false
            LanguageMode.ECMASCRIPT3,
            false);
        JsDocInfoParser parser = createParser("/** Description @param {string} name */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
        // Description should not be recorded when documentation is disabled
        assertNull("Original comment should be null", info.getOriginalCommentString());
    }
    
    @Test(timeout = 4000)
    public void testParseWithCustomAnnotations() {
        Map<String, Annotation> customAnnotations = new HashMap<>();
        customAnnotations.put("customAnnotation", Annotation.NG_INJECT);
        Config config = createConfigWithAnnotations(customAnnotations);
        JsDocInfoParser parser = createParser("/** @customAnnotation */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be ngInject", info.isNgInject());
    }
    
    @Test(timeout = 4000)
    public void testParseWithSuppressionNames() {
        Set<String> suppressionNames = new HashSet<>();
        suppressionNames.add("customSuppress");
        Config config = new Config(
            Sets.<String>newHashSet(),
            suppressionNames,
            false,
            LanguageMode.ECMASCRIPT3,
            false);
        JsDocInfoParser parser = createParser("/** @suppress {customSuppress} */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have suppression", info.getSuppressions().contains("customSuppress"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithLanguageMode() {
        Config config = new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            false,
            LanguageMode.ECMASCRIPT5,
            false);
        JsDocInfoParser parser = createParser("/** @type {number} */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithIdeMode() {
        Config config = new Config(
            Sets.<String>newHashSet(),
            Sets.<String>newHashSet(),
            false,
            LanguageMode.ECMASCRIPT3,
            true);  // isIdeMode = true
        JsDocInfoParser parser = createParser("/** @type {number} */", null, config);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithNullAssociatedNode() {
        JsDocTokenStream stream = new JsDocTokenStream("/** @type {number} */");
        Comment commentNode = new Comment(1, 1, "/** @type {number} */", 0);
        Config config = createDefaultConfig();
        ErrorReporter reporter = NullErrorReporter.forNewRhino();
        JsDocInfoParser parser = new JsDocInfoParser(stream, commentNode, null, config, reporter);
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithNullSourceFile() {
        Node associatedNode = new Node(1, 1, 1);
        JsDocInfoParser parser = createParser("/** @type {number} */", associatedNode, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithEmptyAnnotationName() {
        final StringBuilder warnings = new StringBuilder();
        ErrorReporter reporter = new NullErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                warnings.append(message).append("\n");
            }
        };
        
        JsDocInfoParser parser = createParserWithReporter("/** @ */", reporter);
        assertTrue("Should parse successfully", parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("JSDocInfo should not be null", info);
        
        String warningStr = warnings.toString();
        assertTrue("Should warn about bad jsdoc tag", warningStr.contains("bad.jsdoc.tag"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleLinesAndStars() {
        JsDocInfoParser parser = createParser("/**\n * @param {string} name\n * @param {number} age\n * @return {boolean}\n */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter name", info.hasParameter("name"));
        assertTrue("Should have parameter age", info.hasParameter("age"));
        assertTrue("Should have return type", info.hasReturnType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithComplexTypeExpression() {
        JsDocInfoParser parser = createParser("/** @type {Array.<function(string, number): boolean>} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithNestedTypeExpressions() {
        JsDocInfoParser parser = createParser("/** @type {{a: number, b: {c: string}}} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithUnionOfFunctions() {
        JsDocInfoParser parser = createParser("/** @type {(function(): void|function(): number)} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithOptionalParamInFunction() {
        JsDocInfoParser parser = createParser("/** @type {function(string=, number=): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithRestParamInFunction() {
        JsDocInfoParser parser = createParser("/** @type {function(string, ...[number]): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithVoidReturnType() {
        JsDocInfoParser parser = createParser("/** @type {function(): void} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithEmptyReturnType() {
        JsDocInfoParser parser = createParser("/** @type {function()} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithGenericType() {
        JsDocInfoParser parser = createParser("/** @type {Array.<string>} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleGenericTypes() {
        JsDocInfoParser parser = createParser("/** @type {Object.<string, number>} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllAnnotations() {
        JsDocInfoParser parser = createParser("/** @constructor @param {string} name @return {number} @deprecated Use newMethod */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constructor", info.isConstructor());
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertTrue("Should have return type", info.hasReturnType());
        assertTrue("Should be deprecated", info.isDeprecated());
        assertEquals("Deprecation reason", "Use newMethod", info.getDeprecationReason());
    }
    
    @Test(timeout = 4000)
    public void testParseWithDescriptionAfterAnnotation() {
        JsDocInfoParser parser = createParser("/** @param {string} name The name of the person */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter", info.hasParameter("name"));
        assertEquals("Parameter description", "The name of the person", info.getParameterDescription("name"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithReturnDescription() {
        JsDocInfoParser parser = createParser("/** @return {number} The count */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have return type", info.hasReturnType());
        assertEquals("Return description", "The count", info.getReturnDescription());
    }
    
    @Test(timeout = 4000)
    public void testParseWithThrowDescription() {
        JsDocInfoParser parser = createParser("/** @throws {Error} If something goes wrong */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have throw type", info.getThrownTypes().size() > 0);
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleThrows() {
        JsDocInfoParser parser = createParser("/** @throws {Error} First error @throws {TypeError} Second error */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Should have 2 throw types", 2, info.getThrownTypes().size());
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleParams() {
        JsDocInfoParser parser = createParser("/** @param {string} name @param {number} age @param {boolean} active */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have parameter name", info.hasParameter("name"));
        assertTrue("Should have parameter age", info.hasParameter("age"));
        assertTrue("Should have parameter active", info.hasParameter("active"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleExtends() {
        JsDocInfoParser parser = createParser("/** @interface @extends {Foo} @extends {Bar} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be interface", info.isInterface());
        assertEquals("Should have 2 extended interfaces", 2, info.getExtendedInterfacesCount());
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleImplements() {
        JsDocInfoParser parser = createParser("/** @implements {Foo} @implements {Bar} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Should have 2 implemented interfaces", 2, info.getImplementedInterfacesCount());
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleSuppressions() {
        JsDocInfoParser parser = createParser("/** @suppress {checkTypes|globalThis|deprecated} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        Set<String> suppressions = info.getSuppressions();
        assertEquals("Should have 3 suppressions", 3, suppressions.size());
        assertTrue("Should suppress checkTypes", suppressions.contains("checkTypes"));
        assertTrue("Should suppress globalThis", suppressions.contains("globalThis"));
        assertTrue("Should suppress deprecated", suppressions.contains("deprecated"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleModifies() {
        JsDocInfoParser parser = createParser("/** @modifies {this|arguments} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        Set<String> modifies = info.getModifies();
        assertEquals("Should have 2 modifies", 2, modifies.size());
        assertTrue("Should modify this", modifies.contains("this"));
        assertTrue("Should modify arguments", modifies.contains("arguments"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleTemplateNames() {
        JsDocInfoParser parser = createParser("/** @template T, U, V */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        List<String> templateNames = info.getTemplateTypeNames();
        assertEquals("Should have 3 template names", 3, templateNames.size());
        assertTrue("Should contain T", templateNames.contains("T"));
        assertTrue("Should contain U", templateNames.contains("U"));
        assertTrue("Should contain V", templateNames.contains("V"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleDisposes() {
        JsDocInfoParser parser = createParser("/** @disposes {param1, param2, param3} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        Set<String> disposes = info.getDisposesParameters();
        assertEquals("Should have 3 disposes parameters", 3, disposes.size());
        assertTrue("Should dispose param1", disposes.contains("param1"));
        assertTrue("Should dispose param2", disposes.contains("param2"));
        assertTrue("Should dispose param3", disposes.contains("param3"));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleAuthors() {
        JsDocInfoParser parser = createParser("/** @author John @author Jane */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        List<String> authors = info.getAuthors();
        assertEquals("Should have 2 authors", 2, authors.size());
        assertEquals("First author", "John", authors.get(0));
        assertEquals("Second author", "Jane", authors.get(1));
    }
    
    @Test(timeout = 4000)
    public void testParseWithMultipleReferences() {
        JsDocInfoParser parser = createParser("/** @see Foo @see Bar */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        List<String> references = info.getReferences();
        assertEquals("Should have 2 references", 2, references.size());
        assertEquals("First reference", "Foo", references.get(0));
        assertEquals("Second reference", "Bar", references.get(1));
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllVisibilityModifiers() {
        JsDocInfoParser parser = createParser("/** @private @protected @public */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        // The last one should win
        assertTrue("Should be public", info.isPublic());
    }
    
    @Test(timeout = 4000)
    public void testParseWithTypeAndVisibility() {
        JsDocInfoParser parser = createParser("/** @type {number} @private */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
        assertTrue("Should be private", info.isPrivate());
    }
    
    @Test(timeout = 4000)
    public void testParseWithVisibilityAndType() {
        JsDocInfoParser parser = createParser("/** @private {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be private", info.isPrivate());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithConstAndType() {
        JsDocInfoParser parser = createParser("/** @const {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be constant", info.isConstant());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithDefineAndType() {
        JsDocInfoParser parser = createParser("/** @define {boolean} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be define", info.isDefine());
        assertTrue("Should have type", info.hasType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithEnumAndType() {
        JsDocInfoParser parser = createParser("/** @enum {string} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have enum parameter type", info.hasEnumParameterType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithReturnAndType() {
        JsDocInfoParser parser = createParser("/** @return {number} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have return type", info.hasReturnType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithThisAndType() {
        JsDocInfoParser parser = createParser("/** @this {Foo} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have this type", info.hasThisType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithTypeDefAndType() {
        JsDocInfoParser parser = createParser("/** @typedef {Object} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have typedef type", info.hasTypedefType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithExtendsAndType() {
        JsDocInfoParser parser = createParser("/** @extends {Foo} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have base type", info.hasBaseType());
    }
    
    @Test(timeout = 4000)
    public void testParseWithImplementsAndType() {
        JsDocInfoParser parser = createParser("/** @implements {Bar} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should have implemented interface", info.getImplementedInterfacesCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testParseWithLendsAndType() {
        JsDocInfoParser parser = createParser("/** @lends {MyObject.prototype} */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertEquals("Lends", "MyObject.prototype", info.getLendsName());
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllStructuralAnnotations() {
        JsDocInfoParser parser = createParser("/** @struct @dict @constructor @interface */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        // The last one should win for conflicting annotations
        assertTrue("Should be interface", info.isInterface());
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllGeneratorAnnotations() {
        JsDocInfoParser parser = createParser("/** @idgenerator @consistentIdGenerator @stableIdGenerator */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        // The last one should win
        assertTrue("Should be stableIdGenerator", info.isStableIdGenerator());
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllJaggerAnnotations() {
        JsDocInfoParser parser = createParser("/** @jaggerInject @jaggerModule @jaggerProvide */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be jaggerInject", info.isJaggerInject());
        assertTrue("Should be jaggerModule", info.isJaggerModule());
        assertTrue("Should be jaggerProvide", info.isJaggerProvide());
    }
    
    @Test(timeout = 4000)
    public void testParseWithAllBooleanAnnotations() {
        JsDocInfoParser parser = createParser("/** @export @expose @externs @noCompile @noTypeCheck @noShadow @noSideEffects @implicitCast @preserveTry @noAlias @hidden @javaDispatch @wizaction */", null, createDefaultConfig());
        assertTrue(parser.parse());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull(info);
        assertTrue("Should be export", info.isExport());
        assertTrue("Should be expose", info.isExpose());
        assertTrue("Should be externs", info.isExterns());
        assertTrue("Should be noCompile", info.isNoCompile());
        assertTrue("Should be noTypeCheck", info.isNoTypeCheck());
        assertTrue("Should be noShadow", info.isNoShadow());
        assertTrue("Should be noSideEffects", info.isNoSideEffects());
        assertTrue("Should be implicitCast", info.isImplicitCast());
        assertTrue("Should be preserveTry", info.isPreserveTry());
        assertTrue("Should be noAlias", info.isNoAlias());
        assertTrue("Should be hidden", info.isHidden());
        assertTrue("Should be javaDispatch", info.isJavaDispatch());
        assertTrue("Should be wizaction", info.isWizaction());
    }
}