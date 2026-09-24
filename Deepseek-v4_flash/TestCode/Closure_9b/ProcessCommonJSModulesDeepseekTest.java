package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * Test suite for ProcessCommonJSModules.
 * Targets the known defect in toModuleName relative resolution and covers
 * all public static methods and key instance methods.
 */
public class ProcessCommonJSModulesDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Partition A: Core Functional Logic & State Transitions
     *   - toModuleName(String): normal, with "./", with "/", with ".js", with "-"
     *   - toModuleName(String, String): relative resolution, absolute, edge cases
     *   - guessCJSModuleName: with prefix stripping, without
     *   - Constructor and getModule integration
     * Partition B: Boundary Value Analysis & Extremes
     *   - Empty strings, null (expect NPE), root-relative paths
     * Partition C: Defect-Targeted Branch Zone
     *   - Known defect: toModuleName("./baz", "foo/bar.js") should return "module$baz"
     *     but currently returns "module$foo$baz" due to incorrect URI resolution base.
     * Partition D: Exception & Defensive Guard Paths
     *   - URISyntaxException handling (via invalid URI characters)
     * Partition E: Object Lifecycle & Contract Integrity
     *   - Not applicable (no equals/hashCode/clone)
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testToModuleNameSimple() {
        assertEquals("module$baz", ProcessCommonJSModules.toModuleName("baz.js"));
        assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar.js"));
        assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
        assertEquals("module$foo$bar_baz", ProcessCommonJSModules.toModuleName("foo/bar-baz.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameWithLeadingDotSlash() {
        assertEquals("module$baz", ProcessCommonJSModules.toModuleName("./baz.js"));
        assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("./foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameWithoutJsSuffix() {
        assertEquals("module$baz", ProcessCommonJSModules.toModuleName("baz"));
        assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameWithHyphenReplacement() {
        assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar"));
        assertEquals("module$a_b_c", ProcessCommonJSModules.toModuleName("a-b-c"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameWithMultipleSlashes() {
        assertEquals("module$a$b$c", ProcessCommonJSModules.toModuleName("a/b/c.js"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToModuleNameNull() {
        ProcessCommonJSModules.toModuleName(null);
    }

    @Test(timeout = 4000)
    public void testToModuleNameEmptyString() {
        assertEquals("module$", ProcessCommonJSModules.toModuleName(""));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRootRelative() {
        assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("/foo/bar.js"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Reproduces the known defect: toModuleName("./baz", "foo/bar.js") should return
     * "module$baz" (the module name without the directory prefix) but currently returns
     * "module$foo$baz" because the URI resolution uses the file as base instead of its directory.
     */
    @Test(timeout = 4000)
    public void testToModuleNameRelativeResolutionDefect() {
        // Expected: module$baz (relative path resolved to same directory as current file)
        // Actual (buggy): module$foo$baz
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("./baz", "foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeResolutionUpDir() {
        // ../baz from foo/bar.js should resolve to baz (since foo/../baz = baz)
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("../baz", "foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeResolutionSameDir() {
        // ./baz from baz.js should resolve to baz
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("./baz", "baz.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeResolutionNoDot() {
        // Without ./ or ../, no resolution should occur
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("baz", "foo/bar.js"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testToModuleNameInvalidURI() {
        // URI with invalid characters should throw RuntimeException wrapping URISyntaxException
        ProcessCommonJSModules.toModuleName("./baz", "invalid uri space");
    }

    // ==================== Partition E: guessCJSModuleName and normalizeSourceName ====================

    @Test(timeout = 4000)
    public void testGuessCJSModuleNameDefaultPrefix() {
        // With default prefix "./", guessCJSModuleName should strip it
        ProcessCommonJSModules p = new ProcessCommonJSModules(null, "./");
        assertEquals("module$baz", p.guessCJSModuleName("./baz.js"));
        assertEquals("module$foo$bar", p.guessCJSModuleName("./foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testGuessCJSModuleNameCustomPrefix() {
        ProcessCommonJSModules p = new ProcessCommonJSModules(null, "src/");
        assertEquals("module$baz", p.guessCJSModuleName("src/baz.js"));
        assertEquals("module$foo$bar", p.guessCJSModuleName("src/foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testGuessCJSModuleNameNoPrefixMatch() {
        ProcessCommonJSModules p = new ProcessCommonJSModules(null, "./");
        // Filename does not start with prefix, so no stripping
        assertEquals("module$foo$baz", p.guessCJSModuleName("foo/baz.js"));
    }

    @Test(timeout = 4000)
    public void testGuessCJSModuleNamePrefixWithoutSlash() {
        // Constructor appends slash if missing
        ProcessCommonJSModules p = new ProcessCommonJSModules(null, "src");
        assertEquals("module$baz", p.guessCJSModuleName("src/baz.js"));
    }

    // ==================== Integration: Constructor and getModule ====================

    @Test(timeout = 4000)
    public void testConstructorAndGetModule() {
        // Use a real compiler with a simple script to test process and getModule
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        // Create a simple source file
        SourceFile input = SourceFile.fromCode("test.js", "var x = 1;");
        compiler.compile(
                new SourceFile[] {},
                new SourceFile[] { input },
                options);

        ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./");
        pass.process(null, compiler.getRoot());

        JSModule module = pass.getModule();
        assertNotNull("Module should not be null after processing", module);
        assertEquals("module$test", module.getName());
        assertEquals(1, module.getInputs().size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithReportDependenciesFalse() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        SourceFile input = SourceFile.fromCode("test.js", "var x = 1;");
        compiler.compile(
                new SourceFile[] {},
                new SourceFile[] { input },
                options);

        ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", false);
        pass.process(null, compiler.getRoot());

        // When reportDependencies is false, getModule should still be set? Actually it is set only if reportDependencies is true.
        // The code sets module only if reportDependencies is true.
        assertNull("Module should be null when reportDependencies is false", pass.getModule());
    }

    // ==================== Additional edge cases for toModuleName(String, String) ====================

    @Test(timeout = 4000)
    public void testToModuleNameRelativeWithJsSuffix() {
        // Both inputs have .js suffix, should be stripped before resolution
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("./baz.js", "foo/bar.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeCurrentWithoutJs() {
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("./baz", "foo/bar"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeUpDirMultiple() {
        // ../../baz from a/b/c.js should resolve to baz
        assertEquals("module$baz",
                ProcessCommonJSModules.toModuleName("../../baz", "a/b/c.js"));
    }

    @Test(timeout = 4000)
    public void testToModuleNameRelativeComplex() {
        // ./../baz from a/b/c.js should resolve to a/baz
        assertEquals("module$a$baz",
                ProcessCommonJSModules.toModuleName("./../baz", "a/b/c.js"));
    }
}