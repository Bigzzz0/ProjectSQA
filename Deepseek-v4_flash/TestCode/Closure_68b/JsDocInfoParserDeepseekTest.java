package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.ast.Comment;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Advanced white-box test suite for JsDocInfoParser.
 * Targets core parsing logic, state transitions, boundary conditions,
 * and the known Defects4J issue (testIssue477) that produces an extra
 * "Unexpected end of file" warning.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional paths (constructors, parse(), annotations)
 * Partition B: Boundary values (empty comments, missing braces, EOF)
 * Partition C: Defect-targeted branch (testIssue477)
 * Partition D: Exception/defensive guards (duplicate annotations, missing type)
 * Partition E: Object lifecycle (build, retrieve, file overview)
 *
 * All tests are deterministic and JUnit 4 only.
 */
public class JsDocInfoParserDeepseekTest {

    // -----------------------------------------------------------------------
    // Custom ErrorReporter for collecting warnings/errors
    // -----------------------------------------------------------------------
    private static class TestErrorReporter implements ErrorReporter {
        final List<String> warnings = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void warning(String message, String sourceName, int line,
                            String lineSource, int lineOffset) {
            warnings.add(message);
        }

        @Override
        public void error(String message, String sourceName, int line,
                          String lineSource, int lineOffset) {
            errors.add(message);
        }
    }

    // -----------------------------------------------------------------------
    // Default configuration with standard JSDoc annotations
    // -----------------------------------------------------------------------
    private static Config createDefaultConfig() {
        Map<String, Annotation> annotationNames = new HashMap<>();
        annotationNames.put("author", Annotation.AUTHOR);
        annotationNames.put("const", Annotation.CONSTANT);
        annotationNames.put("constant", Annotation.CONSTANT);
        annotationNames.put("constructor", Annotation.CONSTRUCTOR);
        annotationNames.put("deprecated", Annotation.DEPRECATED);
        annotationNames.put("interface", Annotation.INTERFACE);
        annotationNames.put("desc", Annotation.DESC);
        annotationNames.put("fileoverview", Annotation.FILE_OVERVIEW);
        annotationNames.put("license", Annotation.LICENSE);
        annotationNames.put("preserve", Annotation.PRESERVE);
        annotationNames.put("enum", Annotation.ENUM);
        annotationNames.put("export", Annotation.EXPORT);
        annotationNames.put("externs", Annotation.EXTERNS);
        annotationNames.put("javadispatch", Annotation.JAVA_DISPATCH);
        annotationNames.put("extends", Annotation.EXTENDS);
        annotationNames.put("implements", Annotation.IMPLEMENTS);
        annotationNames.put("hidden", Annotation.HIDDEN);
        annotationNames.put("lends", Annotation.LENDS);
        annotationNames.put("meaning", Annotation.MEANING);
        annotationNames.put("noalias", Annotation.NO_ALIAS);
        annotationNames.put("nocompile", Annotation.NO_COMPILE);
        annotationNames.put("notypecheck", Annotation.NO_TYPE_CHECK);
        annotationNames.put("notimplemented", Annotation.NOT_IMPLEMENTED);
        annotationNames.put("inheritDoc", Annotation.INHERIT_DOC);
        annotationNames.put("override", Annotation.OVERRIDE);
        annotationNames.put("throws", Annotation.THROWS);
        annotationNames.put("param", Annotation.PARAM);
        annotationNames.put("preservetry", Annotation.PRESERVE_TRY);
        annotationNames.put("private", Annotation.PRIVATE);
        annotationNames.put("protected", Annotation.PROTECTED);
        annotationNames.put("public", Annotation.PUBLIC);
        annotationNames.put("noshadow", Annotation.NO_SHADOW);
        annotationNames.put("nosideeffects", Annotation.NO_SIDE_EFFECTS);
        annotationNames.put("modifies", Annotation.MODIFIES);
        annotationNames.put("implicitCast", Annotation.IMPLICIT_CAST);
        annotationNames.put("see", Annotation.SEE);
        annotationNames.put("suppress", Annotation.SUPPRESS);
        annotationNames.put("template", Annotation.TEMPLATE);
        annotationNames.put("version", Annotation.VERSION);
        annotationNames.put("define", Annotation.DEFINE);
        annotationNames.put("return", Annotation.RETURN);
        annotationNames.put("this", Annotation.THIS);
        annotationNames.put("type", Annotation.TYPE);
        annotationNames.put("typedef", Annotation.TYPEDEF);

        Set<String> suppressionNames = new HashSet<>();
        suppressionNames.add("unused");
        suppressionNames.add("checkTypes");
        suppressionNames.add("warning");
        suppressionNames.add("error");

        return new Config(annotationNames, suppressionNames, true,
                          LanguageMode.ECMASCRIPT3, false);
    }

    // -----------------------------------------------------------------------
    // Helper: parse a full JSDoc comment string
    // -----------------------------------------------------------------------
    private JSDocInfo parseComment(String comment, Config config,
                                   TestErrorReporter reporter) {
        JsDocTokenStream stream = new JsDocTokenStream(comment);
        // commentNode can be null – parser extracts value from stream
        JsDocInfoParser parser = new JsDocInfoParser(
                stream, null, "test", config, reporter);
        parser.parse();
        return parser.retrieveAndResetParsedJSDocInfo();
    }

    // =======================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =======================================================================

    @Test(timeout = 4000)
    public void testEmptyComment() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** */", config, reporter);
        assertNotNull("JSDocInfo should not be null", info);
        assertFalse("Should not be populated", info.isPopulated());
        assertEquals("No warnings expected", 0, reporter.warnings.size());
        assertEquals("No errors expected", 0, reporter.errors.size());
    }

    @Test(timeout = 4000)
    public void testSimpleDescription() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** A simple description. */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have description", info.hasDescription());
        assertEquals("A simple description.", info.getDescription());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testParamAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param {string} x */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have param", info.hasParameter("x"));
        assertNotNull("Param type should exist", info.getParameterType("x"));
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testReturnAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @return {number} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have return type", info.hasReturnType());
        assertNotNull(info.getReturnType());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testConstructorAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @constructor */", config, reporter);
        assertNotNull(info);
        assertTrue("Should be constructor", info.isConstructor());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testInterfaceAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @interface */", config, reporter);
        assertNotNull(info);
        assertTrue("Should be interface", info.isInterface());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testDeprecatedWithReason() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @deprecated Use something else. */", config, reporter);
        assertNotNull(info);
        assertTrue("Should be deprecated", info.isDeprecated());
        assertEquals("Use something else.", info.getDeprecationReason());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testSuppressTag() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @suppress {warning1|warning2} */", config, reporter);
        assertNotNull(info);
        Set<String> suppressions = info.getSuppressions();
        assertTrue("Should contain warning1", suppressions.contains("warning1"));
        assertTrue("Should contain warning2", suppressions.contains("warning2"));
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testModifiesTag() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @modifies {this} */", config, reporter);
        assertNotNull(info);
        Set<String> modifies = info.getModifies();
        assertTrue("Should modify 'this'", modifies.contains("this"));
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testTypeAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @type {string} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have type", info.hasType());
        assertNotNull(info.getType());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testEnumAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @enum {number} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should be enum", info.isEnum());
        assertNotNull("Enum type should exist", info.getEnumParameterType());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testExtendsAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @extends {Foo} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have base type", info.hasBaseType());
        assertNotNull(info.getBaseType());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testImplementsAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @implements {Bar} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have implemented interface", info.getImplementedInterfaces().length > 0);
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testThatAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @this {Window} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have this type", info.hasThisType());
        assertNotNull(info.getThisType());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testSeeAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @see Reference */", config, reporter);
        assertNotNull(info);
        assertEquals("Reference", info.getReferences()[0]);
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testAuthorAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @author John Doe */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have authors", info.getAuthors().length > 0);
        assertEquals("John Doe", info.getAuthors()[0]);
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testVersionAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @version 1.0 */", config, reporter);
        assertNotNull(info);
        assertEquals("1.0", info.getVersion());
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testFileOverview() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @fileoverview Description */", config, reporter);
        assertNotNull(info);
        assertEquals("Description", info.getFileOverview());
        assertEquals(0, reporter.warnings.size());
    }

    // =======================================================================
    // Partition B: Boundary Value Analysis & Edge Cases
    // =======================================================================

    @Test(timeout = 4000)
    public void testParamWithOptionalDefault() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param {string=} x */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have param x", info.hasParameter("x"));
        // Optional type is represented as EQUALS wrapper
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testParamWithBracketsNoDefault() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param {string} [x] */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have param x", info.hasParameter("x"));
        assertEquals(0, reporter.warnings.size());
    }

    @Test(timeout = 4000)
    public void testMissingParamType() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param x */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have param x", info.hasParameter("x"));
        // Type should be unknown (null or QMARK)
        assertNotNull("Param type should be set (unknown)", info.getParameterType("x"));
        assertTrue("Should have at least one warning (missing type?)", reporter.warnings.size() > 0);
    }

    @Test(timeout = 4000)
    public void testMissingParamName() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param {string} */", config, reporter);
        assertNotNull(info);
        // No parameter recorded
        assertFalse("Should not have param", info.hasParameter("x"));
        assertTrue("Should have warning about missing variable name",
                   reporter.warnings.size() > 0);
    }

    @Test(timeout = 4000)
    public void testDuplicateParam() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @param {string} x @param {number} x */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have param x", info.hasParameter("x"));
        assertTrue("Should have duplicate warning", reporter.warnings.stream()
                .anyMatch(w -> w.contains("dup.variable.name") || w.contains("incompat.type")));
    }

    @Test(timeout = 4000)
    public void testSuppressEmptyBraces() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @suppress {} */", config, reporter);
        assertNotNull(info);
        // Should record empty suppressions; might produce a warning about suppress unknown
        assertTrue("Should have warnings", reporter.warnings.size() > 0);
    }

    @Test(timeout = 4000)
    public void testIncompleteTypeAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @type { */", config, reporter);
        assertNotNull(info);
        // Expect at least one warning (missing RC), possibly also unexpected EOF in defective version
        assertTrue("Should have warnings", reporter.warnings.size() > 0);
    }

    // =======================================================================
    // Partition C: Defect-Targeted Branch (Defects4J issue 477)
    // =======================================================================

    @Test(timeout = 4000)
    public void testIssue477() {
        // Known defect: extra "Unexpected end of file" warning on valid comment.
        // This test passes on fixed version (no warnings) and fails on defective version.
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        String comment = "/** @type {string} */";
        JSDocInfo info = parseComment(comment, config, reporter);
        assertNotNull("JSDocInfo should not be null", info);
        assertTrue("Should have type", info.hasType());
        assertEquals("No warnings expected", 0, reporter.warnings.size());
        assertEquals("No errors expected", 0, reporter.errors.size());
    }

    // =======================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =======================================================================

    @Test(timeout = 4000)
    public void testDuplicateConstructor() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @constructor @constructor */", config, reporter);
        assertNotNull(info);
        // Should have at least one warning about duplicate
        assertTrue("Should have warning", reporter.warnings.stream()
                .anyMatch(w -> w.contains("incompat.type") || w.contains("interface.constructor")));
    }

    @Test(timeout = 4000)
    public void testDuplicateInterface() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @interface @interface */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have warning", reporter.warnings.size() > 0);
    }

    @Test(timeout = 4000)
    public void testUnknownAnnotation() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        // Remove "param" from annotations to make it unknown
        Map<String, Annotation> badNames = new HashMap<>(config.annotationNames);
        badNames.remove("param");
        Config badConfig = new Config(badNames, config.suppressionNames,
                                      true, LanguageMode.ECMASCRIPT3, false);
        JSDocInfo info = parseComment("/** @param {string} x */", badConfig, reporter);
        assertNotNull(info);
        assertTrue("Should have warning about bad tag", reporter.warnings.stream()
                .anyMatch(w -> w.contains("bad.jsdoc.tag")));
    }

    @Test(timeout = 4000)
    public void testReturnWithDescription() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JSDocInfo info = parseComment("/** @return {string} The result. */", config, reporter);
        assertNotNull(info);
        assertTrue("Should have return type", info.hasReturnType());
        assertEquals("The result.", info.getReturnDescription());
        assertEquals(0, reporter.warnings.size());
    }

    // =======================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =======================================================================

    @Test(timeout = 4000)
    public void testRetrieveAfterParse() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        JsDocTokenStream stream = new JsDocTokenStream("/** @type {number} */");
        JsDocInfoParser parser = new JsDocInfoParser(
                stream, null, "test", config, reporter);
        assertFalse("Before parse, should not be populated", parser.hasParsedJSDocInfo());
        boolean success = parser.parse();
        assertTrue("Parse should succeed", success);
        assertTrue("After parse, should be populated", parser.hasParsedJSDocInfo());
        JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
        assertNotNull("Info should not be null", info);
        assertFalse("After reset, should not be populated", parser.hasParsedJSDocInfo());
    }

    @Test(timeout = 4000)
    public void testFileOverviewAfterParse() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        // Parse a file with @fileoverview
        JSDocInfo info = parseComment("/** @fileoverview Overview */", config, reporter);
        assertNotNull(info);
        assertEquals("Overview", info.getFileOverview());
    }

    @Test(timeout = 4000)
    public void testMultipleExtendedTypes() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        // Simulate multiple @extends on interface
        JSDocInfo info = parseComment(
                "/** @interface @extends {Foo} @extends {Bar} */", config, reporter);
        assertNotNull(info);
        assertTrue("Should be interface", info.isInterface());
        // Extended types are collected; may have warnings about duplicate if both are base types
        // In fixed version, should record multiple extended interfaces
        assertTrue("Should have extended interfaces", info.getExtendedInterfacesCount() > 0);
    }

    // Test that parse returns false on malformed input causing EOF
    @Test(timeout = 4000)
    public void testMalformedCommentEof() {
        TestErrorReporter reporter = new TestErrorReporter();
        Config config = createDefaultConfig();
        // Comment that ends without closing star-slash – token stream may return EOF
        JsDocTokenStream stream = new JsDocTokenStream("/** @param");
        JsDocInfoParser parser = new JsDocInfoParser(
                stream, null, "test", config, reporter);
        boolean result = parser.parse();
        assertFalse("Parse should fail", result);
        assertTrue("Should have warnings (at least unexpected EOF)",
                   reporter.warnings.size() > 0);
    }
}