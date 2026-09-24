package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * White-box JUnit 4 test suite for AbstractCommandLineRunner.
 * Targets static utility methods and inner CommandLineConfig configuration.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: createDefineReplacements – valid boolean, number, string defines;
 *              invalid syntax (empty name, missing '=', internal quotes, non-numeric, etc.)
 * Partition B: createJsModules – valid module specs; invalid format (wrong # parts);
 *              duplicate module names; dependency on unknown module; too few/many JS files.
 * Partition C: parseModuleWrappers – valid wrappers; missing colon; unknown module;
 *              missing '%s' placeholder.
 * Partition D: writeOutput – wrapper with placeholder at start/middle/end; no placeholder.
 * Partition E: CommandLineConfig – setter methods return this (fluent) and store values.
 * 
 * Defect target: createDefineReplacements with define "FOO=\"x'\"" must throw RuntimeException.
 */
public class AbstractCommandLineRunnerDeepseekTest {

    // ========== Partition A: createDefineReplacements ==========

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_booleanTrue() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("DEBUG=true");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
        // No exception expected; we just verify it doesn't throw.
        // Further verification of the options is not directly possible without
        // accessing the internal representation, but the test passes if no error.
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_booleanFalse() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("DEBUG=false");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_booleanImplicitTrue() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("DEBUG");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_number() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("VERSION=1.5");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_string() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("NAME='hello'");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCreateDefineReplacements_invalidSyntaxInternalQuote() {
        // Defect test: define with string containing a single quote inside
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("FOO=\"x'\"");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCreateDefineReplacements_emptyName() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("=value");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCreateDefineReplacements_nonNumeric() {
        CompilerOptions options = new CompilerOptions();
        List<String> defines = Arrays.asList("X=abc");
        AbstractCommandLineRunner.createDefineReplacements(defines, options);
    }

    // ========== Partition B: createJsModules ==========

    @Test(timeout = 4000)
    public void testCreateJsModules_validSingleModule() throws Exception {
        List<String> specs = Arrays.asList("m1:2");
        List<String> jsFiles = Arrays.asList("a.js", "b.js");
        JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
        assertEquals(1, modules.length);
        assertEquals("m1", modules[0].getName());
        // No dependencies
    }

    @Test(timeout = 4000)
    public void testCreateJsModules_validWithDependencies() throws Exception {
        List<String> specs = Arrays.asList("m1:1", "m2:1:m1");
        List<String> jsFiles = Arrays.asList("a.js", "b.js");
        JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
        assertEquals(2, modules.length);
        assertEquals("m1", modules[0].getName());
        assertEquals("m2", modules[1].getName());
        assertTrue(modules[1].getDependencies().contains(modules[0]));
    }

    @Test(timeout = 4000)
    public void testCreateJsModules_zeroInputModule() throws Exception {
        List<String> specs = Arrays.asList("m1:0");
        List<String> jsFiles = Collections.emptyList();
        JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
        assertEquals(1, modules.length);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_invalidPartsCount() throws Exception {
        List<String> specs = Arrays.asList("m1:1:2:3:4");
        List<String> jsFiles = Arrays.asList("a.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_invalidName() throws Exception {
        List<String> specs = Arrays.asList("123bad:1");
        List<String> jsFiles = Arrays.asList("a.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_duplicateName() throws Exception {
        List<String> specs = Arrays.asList("m1:1", "m1:1");
        List<String> jsFiles = Arrays.asList("a.js", "b.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_dependencyUnknown() throws Exception {
        List<String> specs = Arrays.asList("m1:1:unknown");
        List<String> jsFiles = Arrays.asList("a.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_tooFewFiles() throws Exception {
        List<String> specs = Arrays.asList("m1:3");
        List<String> jsFiles = Arrays.asList("a.js", "b.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_tooManyFiles() throws Exception {
        List<String> specs = Arrays.asList("m1:1");
        List<String> jsFiles = Arrays.asList("a.js", "b.js");
        AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    }

    // ========== Partition C: parseModuleWrappers ==========

    @Test(timeout = 4000)
    public void testParseModuleWrappers_valid() throws Exception {
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        JSModule[] modules = new JSModule[]{m1, m2};
        List<String> specs = Arrays.asList("m1:(function(){ %s })()", "m2:%s");
        Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
        assertEquals("(function(){ %s })()", wrappers.get("m1"));
        assertEquals("%s", wrappers.get("m2"));
    }

    @Test(timeout = 4000)
    public void testParseModuleWrappers_emptySpecs() throws Exception {
        JSModule m1 = new JSModule("m1");
        JSModule[] modules = new JSModule[]{m1};
        List<String> specs = Collections.emptyList();
        Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
        assertEquals("", wrappers.get("m1"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_missingColon() throws Exception {
        JSModule m1 = new JSModule("m1");
        JSModule[] modules = new JSModule[]{m1};
        List<String> specs = Arrays.asList("m1wrapper");
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_unknownModule() throws Exception {
        JSModule m1 = new JSModule("m1");
        JSModule[] modules = new JSModule[]{m1};
        List<String> specs = Arrays.asList("unknown:%s");
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_missingPlaceholder() throws Exception {
        JSModule m1 = new JSModule("m1");
        JSModule[] modules = new JSModule[]{m1};
        List<String> specs = Arrays.asList("m1:no_placeholder");
        AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    }

    // ========== Partition D: writeOutput ==========

    @Test(timeout = 4000)
    public void testWriteOutput_withPlaceholderAtStart() {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        Compiler compiler = null; // not needed for basic test
        String code = "var x = 1;";
        String wrapper = "%s\n";
        String placeholder = "%s";
        AbstractCommandLineRunner.writeOutput(out, compiler, code, wrapper, placeholder);
        out.flush();
        String output = baos.toString();
        assertEquals("var x = 1;\n", output);
    }

    @Test(timeout = 4000)
    public void testWriteOutput_withPlaceholderMiddle() {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        String code = "code";
        String wrapper = "before%safter";
        AbstractCommandLineRunner.writeOutput(out, null, code, wrapper, "%s");
        out.flush();
        assertEquals("beforecodeafter\n", baos.toString());
    }

    @Test(timeout = 4000)
    public void testWriteOutput_noPlaceholder() {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        String code = "code";
        String wrapper = "nowrapper";
        AbstractCommandLineRunner.writeOutput(out, null, code, wrapper, "%s");
        out.flush();
        assertEquals("code\n", baos.toString());
    }

    // ========== Partition E: CommandLineConfig fluent setters ==========

    @Test(timeout = 4000)
    public void testCommandLineConfig_settersReturnThis() {
        AbstractCommandLineRunner.CommandLineConfig config = new AbstractCommandLineRunner.CommandLineConfig();
        assertSame(config, config.setPrintTree(true));
        assertSame(config, config.setComputePhaseOrdering(true));
        assertSame(config, config.setPrintAst(false));
        assertSame(config, config.setPrintPassGraph(false));
        assertSame(config, config.setJscompDevMode(CompilerOptions.DevMode.OFF));
        assertSame(config, config.setLoggingLevel("INFO"));
        assertSame(config, config.setExterns(Arrays.asList("e.js")));
        assertSame(config, config.setJs(Arrays.asList("a.js")));
        assertSame(config, config.setJsOutputFile("out.js"));
        assertSame(config, config.setModule(Arrays.asList("m:1")));
        assertSame(config, config.setVariableMapInputFile(""));
        assertSame(config, config.setPropertyMapInputFile(""));
        assertSame(config, config.setVariableMapOutputFile(""));
        assertSame(config, config.setCreateNameMapFiles(true));
        assertSame(config, config.setPropertyMapOutputFile(""));
        assertSame(config, config.setCodingConvention(new DefaultCodingConvention()));
        assertSame(config, config.setSummaryDetailLevel(2));
        assertSame(config, config.setOutputWrapper(""));
        assertSame(config, config.setOutputWrapperMarker("%output%"));
        assertSame(config, config.setModuleWrapper(Arrays.asList("m:%s")));
        assertSame(config, config.setModuleOutputPathPrefix("dir/"));
        assertSame(config, config.setCreateSourceMap("map"));
        assertSame(config, config.setJscompError(Arrays.asList("checkTypes")));
        assertSame(config, config.setJscompWarning(Arrays.asList("deprecated")));
        assertSame(config, config.setJscompOff(Arrays.asList("globalThis")));
        assertSame(config, config.setDefine(Arrays.asList("DEBUG=true")));
        assertSame(config, config.setCharset("UTF-8"));
    }
}