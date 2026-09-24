package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.AbstractCommandLineRunner.CommandLineConfig;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.rhino.Node;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * White-box test suite for AbstractCommandLineRunner.
 * Targets line/branch coverage and the known defect: outputCharset not set correctly.
 */
public class AbstractCommandLineRunnerDeepseekTest {

    // Helper subclass to instantiate the abstract class
    private static class TestRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
        TestRunner(PrintStream out, PrintStream err) {
            super(out, err);
        }

        @Override
        protected Compiler createCompiler() {
            return new Compiler();
        }

        @Override
        protected CompilerOptions createOptions() {
            return new CompilerOptions();
        }

        // Expose protected/package-private methods for testing
        @Override
        protected CommandLineConfig getCommandLineConfig() {
            return super.getCommandLineConfig();
        }

        @Override
        public int doRun() throws FlagUsageException, IOException {
            return super.doRun();
        }

        @Override
        public int processResults(Result result, JSModule[] modules, CompilerOptions options)
                throws FlagUsageException, IOException {
            return super.processResults(result, modules, options);
        }

        // Static methods callable via instance
        public List<JSSourceFile> publicCreateExterns() throws FlagUsageException, IOException {
            return createExterns();
        }
    }

    // ----- Partition A: Core Functional Logic -----

    @Test(timeout = 4000)
    public void testCommandLineConfigDefaults() {
        TestRunner runner = new TestRunner(System.out, System.err);
        CommandLineConfig config = runner.getCommandLineConfig();
        assertNotNull(config);
        // Check default values
        assertEquals("", config.charset);
        assertEquals(DevMode.OFF, config.jscompDevMode);
        assertEquals("WARNING", config.loggingLevel);
        assertEquals(1, config.summaryDetailLevel);
        assertFalse(config.printTree);
        assertFalse(config.printAst);
        assertFalse(config.printPassGraph);
        assertFalse(config.computePhaseOrdering);
        assertFalse(config.createNameMapFiles);
        assertFalse(config.manageClosureDependencies);
        assertEquals("", config.jsOutputFile);
        assertEquals("", config.variableMapInputFile);
        assertEquals("", config.propertyMapInputFile);
        assertEquals("", config.variableMapOutputFile);
        assertEquals("", config.propertyMapOutputFile);
        assertEquals("", config.outputWrapper);
        assertEquals("", config.outputWrapperMarker);
        assertEquals("", config.moduleOutputPathPrefix);
        assertEquals("", config.createSourceMap);
        assertEquals(SourceMap.DetailLevel.ALL, config.sourceMapDetailLevel);
        assertEquals("", config.outputManifest);
        assertTrue(config.externs.isEmpty());
        assertTrue(config.js.isEmpty());
        assertTrue(config.module.isEmpty());
        assertTrue(config.moduleWrapper.isEmpty());
        assertTrue(config.jscompError.isEmpty());
        assertTrue(config.jscompWarning.isEmpty());
        assertTrue(config.jscompOff.isEmpty());
        assertTrue(config.define.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCommandLineConfigSetters() {
        CommandLineConfig config = new CommandLineConfig();
        config.setPrintTree(true);
        assertTrue(config.printTree);
        config.setComputePhaseOrdering(true);
        assertTrue(config.computePhaseOrdering);
        config.setPrintAst(true);
        assertTrue(config.printAst);
        config.setPrintPassGraph(true);
        assertTrue(config.printPassGraph);
        config.setJscompDevMode(DevMode.EVERY_PASS);
        assertEquals(DevMode.EVERY_PASS, config.jscompDevMode);
        config.setLoggingLevel("FINE");
        assertEquals("FINE", config.loggingLevel);
        config.setExterns(Arrays.asList("a.js", "b.js"));
        assertEquals(Arrays.asList("a.js", "b.js"), config.externs);
        config.setJs(Arrays.asList("1.js", "2.js"));
        assertEquals(Arrays.asList("1.js", "2.js"), config.js);
        config.setJsOutputFile("out.js");
        assertEquals("out.js", config.jsOutputFile);
        config.setModule(Arrays.asList("m:2"));
        assertEquals(Arrays.asList("m:2"), config.module);
        config.setVariableMapInputFile("vars.in");
        assertEquals("vars.in", config.variableMapInputFile);
        config.setPropertyMapInputFile("props.in");
        assertEquals("props.in", config.propertyMapInputFile);
        config.setVariableMapOutputFile("vars.out");
        assertEquals("vars.out", config.variableMapOutputFile);
        config.setPropertyMapOutputFile("props.out");
        assertEquals("props.out", config.propertyMapOutputFile);
        config.setCreateNameMapFiles(true);
        assertTrue(config.createNameMapFiles);
        config.setCodingConvention(new DefaultCodingConvention());
        assertNotNull(config.codingConvention);
        config.setSummaryDetailLevel(2);
        assertEquals(2, config.summaryDetailLevel);
        config.setOutputWrapper("wrap(%output%)");
        assertEquals("wrap(%output%)", config.outputWrapper);
        config.setOutputWrapperMarker("%output%");
        assertEquals("%output%", config.outputWrapperMarker);
        config.setModuleWrapper(Arrays.asList("m:wrapper(%s)"));
        assertEquals(Arrays.asList("m:wrapper(%s)"), config.moduleWrapper);
        config.setModuleOutputPathPrefix("mod_");
        assertEquals("mod_", config.moduleOutputPathPrefix);
        config.setCreateSourceMap("map.out");
        assertEquals("map.out", config.createSourceMap);
        config.setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS);
        assertEquals(SourceMap.DetailLevel.SYMBOLS, config.sourceMapDetailLevel);
        config.setJscompError(Arrays.asList("checkTypes"));
        assertEquals(Arrays.asList("checkTypes"), config.jscompError);
        config.setJscompWarning(Arrays.asList("deprecated"));
        assertEquals(Arrays.asList("deprecated"), config.jscompWarning);
        config.setJscompOff(Arrays.asList("globalThis"));
        assertEquals(Arrays.asList("globalThis"), config.jscompOff);
        config.setDefine(Arrays.asList("DEBUG=true"));
        assertEquals(Arrays.asList("DEBUG=true"), config.define);
        config.setCharset("ISO-8859-1");
        assertEquals("ISO-8859-1", config.charset);
        config.setManageClosureDependencies(true);
        assertTrue(config.manageClosureDependencies);
        config.setOutputManifest("manifest.txt");
        assertEquals("manifest.txt", config.outputManifest);
    }

    // Test static method createInputs (private, but we can indirectly test via createSourceInputs which is package-private)
    // We'll test via the public createExterns? But createExterns is protected and calls createExternInputs which uses createInputs.
    // Better to test createJsModules which also uses createInputs.

    @Test(timeout = 4000)
    public void testCreateJsModules_valid() throws Exception {
        List<String> specs = Arrays.asList("m1:2", "m2:1:m1");
        List<String> jsFiles = Arrays.asList("a.js", "b.js", "c.js");
        JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
        assertEquals(2, modules.length);
        assertEquals("m1", modules[0].getName());
        assertEquals(2, modules[0].getInputs().size());
        assertEquals("a.js", modules[0].getInputs().get(0).getName());
        assertEquals("b.js", modules[0].getInputs().get(1).getName());
        assertEquals("m2", modules[1].getName());
        assertEquals(1, modules[1].getInputs().size());
        assertEquals("c.js", modules[1].getInputs().get(0).getName());
        assertEquals(1, modules[1].getSortedDependencyNames().size());
        assertEquals("m1", modules[1].getSortedDependencyNames().get(0));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_invalidFormat() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("invalid"), Arrays.asList("a.js"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_duplicateName() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("m:1", "m:1"), Arrays.asList("a.js", "b.js"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_negativeCount() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("m:-1"), Arrays.asList("a.js"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_insufficientFiles() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("m:2"), Arrays.asList("a.js"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_extraFiles() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("m:1"), Arrays.asList("a.js", "b.js"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateJsModules_unknownDependency() throws Exception {
        AbstractCommandLineRunner.createJsModules(
                Arrays.asList("m1:1:unknown"), Arrays.asList("a.js"));
    }

    @Test(timeout = 4000)
    public void testParseModuleWrappers_valid() throws Exception {
        JSModule[] modules = new JSModule[] { new JSModule("m1"), new JSModule("m2") };
        List<String> specs = Arrays.asList("m1:wrapper1(%s)", "m2:wrapper2(%s)");
        Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
        assertEquals("wrapper1(%s)", wrappers.get("m1"));
        assertEquals("wrapper2(%s)", wrappers.get("m2"));
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_missingColon() throws Exception {
        AbstractCommandLineRunner.parseModuleWrappers(
                Arrays.asList("invalid"), new JSModule[0]);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_unknownModule() throws Exception {
        JSModule[] modules = new JSModule[] { new JSModule("m") };
        AbstractCommandLineRunner.parseModuleWrappers(
                Arrays.asList("x:wrap(%s)"), modules);
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testParseModuleWrappers_noPlaceholder() throws Exception {
        JSModule[] modules = new JSModule[] { new JSModule("m") };
        AbstractCommandLineRunner.parseModuleWrappers(
                Arrays.asList("m:wrap"), modules);
    }

    // Test writeOutput
    @Test(timeout = 4000)
    public void testWriteOutput_noWrapper() throws Exception {
        StringBuilder sb = new StringBuilder();
        AbstractCommandLineRunner.writeOutput(sb, null, "code", "", "%s");
        assertEquals("code\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWriteOutput_withWrapper() throws Exception {
        StringBuilder sb = new StringBuilder();
        AbstractCommandLineRunner.writeOutput(sb, null, "code", "prefix %s suffix", "%s");
        assertEquals("prefix code suffix\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWriteOutput_wrapperOnlyPrefix() throws Exception {
        StringBuilder sb = new StringBuilder();
        AbstractCommandLineRunner.writeOutput(sb, null, "code", "prefix %s", "%s");
        assertEquals("prefix code\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testWriteOutput_wrapperOnlySuffix() throws Exception {
        StringBuilder sb = new StringBuilder();
        AbstractCommandLineRunner.writeOutput(sb, null, "code", "%s suffix", "%s");
        assertEquals("code suffix\n", sb.toString());
    }

    // ----- Partition B: Boundary Value Analysis -----

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateSourceInputs_emptyList() throws Exception {
        // empty list triggers default stdin (no exception)
        // but we can test by passing a list containing a dash that is not allowed? Actually createSourceInputs adds stdin if empty.
        // Better test createExternInputs with empty list - it returns /dev/null
        List<JSSourceFile> externs = AbstractCommandLineRunner.createExternInputs(Collections.<String>emptyList());
        assertEquals(1, externs.size());
        assertEquals("/dev/null", externs.get(0).getName());
    }

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testCreateExternInputs_invalidFlag() throws Exception {
        AbstractCommandLineRunner.createExternInputs(Arrays.asList("-"));
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_booleanTrue() {
        CompilerOptions options = new CompilerOptions();
        List<String> defs = Arrays.asList("DEBUG");
        AbstractCommandLineRunner.createDefineReplacements(defs, options);
        // Check that define is set? We can't easily inspect, but no exception means success.
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testCreateDefineReplacements_invalidSyntax() {
        CompilerOptions options = new CompilerOptions();
        List<String> defs = Arrays.asList("=");
        AbstractCommandLineRunner.createDefineReplacements(defs, options);
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_stringValue() {
        CompilerOptions options = new CompilerOptions();
        List<String> defs = Arrays.asList("NAME='value'");
        AbstractCommandLineRunner.createDefineReplacements(defs, options);
    }

    @Test(timeout = 4000)
    public void testCreateDefineReplacements_numberValue() {
        CompilerOptions options = new CompilerOptions();
        List<String> defs = Arrays.asList("COUNT=5");
        AbstractCommandLineRunner.createDefineReplacements(defs, options);
    }

    // ----- Partition C: Defect-Targeted (outputCharset) -----

    @Test(timeout = 4000)
    public void testOutputCharset_WhenDefaultUTF8_ShouldBeUSASCII() throws Exception {
        // Setup config with empty charset -> default UTF-8
        TestRunner runner = new TestRunner(System.out, System.err);
        CommandLineConfig config = runner.getCommandLineConfig();
        config.setCharset(""); // empty
        // Set minimal flags to avoid actual compilation
        config.setJs(Collections.<String>emptyList());
        config.setModule(Collections.<String>emptyList());
        config.setExterns(Collections.<String>emptyList());

        // We need to call setRunOptions which calls getInputCharset.
        // We can access the protected method via reflection or by calling doRun? But doRun is heavy.
        // Instead, we can test the private getInputCharset indirectly by
        // creating a test that exercises the doRun path with minimal work.
        // For simplicity, we rely on the known defect and test via a stub that
        // checks the options.outputCharset after setRunOptions.
        
        // However, setRunOptions is final protected, so we can call it from the subclass if we expose it.
        // We'll create a method in TestRunner to test the charset logic.
        // Actually, we can test the getInputCharset directly via reflection? Not needed.
        
        // The defect is that outputCharset is not set when it should be.
        // We'll write a test that creates a runner, sets charset to empty, then calls doRun()
        // but we must ensure it doesn't actually try to compile files.
        // That's tricky. Alternative: test the static method that parses charset? No.
        
        // Let's use a different approach: instantiate a runner with a custom PrintStream that captures output,
        // set config.jsOutputFile to a temp file, and then call doRun() with minimal externs.
        // But we'd need actual JS files. Too heavy.
        
        // Simpler: we can test the logic in setRunOptions indirectly by creating a subclass that overrides
        // createCompiler and createOptions to return mocks, and then checking options after setRunOptions.
        // But setRunOptions is final, so we can't override.
        
        // Let's inspect the code: setRunOptions does not set outputCharset; outputCharset is set in doRun().
        // So we need to test doRun().
        
        // Given the context, we'll assume that testing doRun() is acceptable.
        // We'll provide a minimal test that triggers the charset path without actual compilation.
        // We can use a TestRunner that sets jsOutputFile to a temp file, but we need to ensure files exist.
        // Better: we can create a test that checks the logic by inspecting the options after doRun().
        // But doRun() will attempt to compile and fail because no files exist.
        // That's okay, we just need to capture the state before the compile call.
        
        // Actually the outputCharset is set before compilation in doRun():
        // if (inputCharset == Charsets.UTF_8) { options.outputCharset = Charsets.US_ASCII; }
        // So we can run doRun() which will throw because of missing files? It will throw IOException? 
        // Let's check: if jsFiles is empty, createSourceInputs will add stdin, but then compiler.compile might fail.
        // We can catch the exception and then check options.
        
        // Let's implement this:
        final CompilerOptions capturedOptions = new CompilerOptions();
        TestRunner runner = new TestRunner(System.out, System.err) {
            @Override
            protected CompilerOptions createOptions() {
                return capturedOptions;
            }
        };
        runner.getCommandLineConfig().setCharset("");
        runner.getCommandLineConfig().setJs(Collections.<String>emptyList());
        runner.getCommandLineConfig().setExterns(Collections.<String>emptyList());
        // Set jsOutputFile to avoid writing to stdout (to prevent accidental writing)
        runner.getCommandLineConfig().setJsOutputFile("/dev/null");
        try {
            runner.doRun();
        } catch (Exception e) {
            // Expected: no input files or compilation failure.
            // We still capture the options state.
        }
        // Now assert the defect: outputCharset should be US_ASCII, not null.
        assertNotNull("outputCharset should not be null when default charset is UTF-8",
                capturedOptions.outputCharset);
        assertEquals(Charsets.US_ASCII, capturedOptions.outputCharset);
    }

    @Test(timeout = 4000)
    public void testOutputCharset_WhenExplicitCharset_ShouldMatch() throws Exception {
        final CompilerOptions capturedOptions = new CompilerOptions();
        TestRunner runner = new TestRunner(System.out, System.err) {
            @Override
            protected CompilerOptions createOptions() {
                return capturedOptions;
            }
        };
        runner.getCommandLineConfig().setCharset("ISO-8859-1");
        runner.getCommandLineConfig().setJs(Collections.<String>emptyList());
        runner.getCommandLineConfig().setExterns(Collections.<String>emptyList());
        runner.getCommandLineConfig().setJsOutputFile("/dev/null");
        try {
            runner.doRun();
        } catch (Exception e) {
            // expected
        }
        assertNotNull(capturedOptions.outputCharset);
        assertEquals(Charsets.ISO_8859_1, capturedOptions.outputCharset);
    }

    // ----- Partition D: Exception Paths -----

    @Test(timeout = 4000, expected = FlagUsageException.class)
    public void testInvalidCharset() throws Exception {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setCharset("invalid-charset");
        runner.getCommandLineConfig().setJs(Collections.<String>emptyList());
        runner.getCommandLineConfig().setExterns(Collections.<String>emptyList());
        runner.doRun();
    }

    @Test(timeout = 4000)
    public void testFlagUsageExceptionInCreateInputs() throws Exception {
        // Test that if allowStdIn is false and '-' is present, FlagUsageException thrown
        // We'll call the private method directly? Not possible.
        // But we can test via createExternInputs which calls createInputs with allowStdIn=false.
        try {
            AbstractCommandLineRunner.createExternInputs(Arrays.asList("-"));
            fail("Should have thrown FlagUsageException");
        } catch (FlagUsageException e) {
            assertTrue(e.getMessage().contains("Can't specify stdin"));
        }
    }

    @Test(timeout = 4000)
    public void testMultipleStdin() throws Exception {
        try {
            // createSourceInputs allows stdin only once
            // We'll test by passing two '-' to createInputs via reflection? Not needed.
            // We can test by calling createSourceInputs with list containing two '-'.
            // But createSourceInputs calls createInputs with allowStdIn=true, which will throw on second '-'.
            AbstractCommandLineRunner.createSourceInputs(Arrays.asList("-", "-"));
            fail("Should have thrown FlagUsageException");
        } catch (FlagUsageException e) {
            assertTrue(e.getMessage().contains("stdin twice"));
        }
    }

    // ----- Partition E: Misc methods -----

    @Test(timeout = 4000)
    public void testExpandCommandLinePath_singleOutput() {
        CommandLineConfig config = new CommandLineConfig();
        config.setJsOutputFile("out.js");
        // We need to access the method; it's private. We'll test via expandSourceMapPath (package-private).
        // expandSourceMapPath is protected in ACLR, but we can call via subclass.
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setJsOutputFile("out.js");
        // expandSourceMapPath requires options and forModule; we'll pass null module.
        // It uses private expandCommandLinePath. We can test indirectly:
        // Since expandSourceMapPath is package-private and we are in same package, we can call it.
        // But we need an options instance.
        CompilerOptions options = new CompilerOptions();
        options.sourceMapOutputPath = "map%outname%.map";
        String expanded = runner.expandSourceMapPath(options, null);
        // For single output jsOutputFile = "out.js", %outname% should become "out.js"?
        // Actually expandCommandLinePath for null module: if modules empty, sub = jsOutputFile
        // So expected: "mapout.js.map"
        assertEquals("mapout.js.map", expanded);
    }

    @Test(timeout = 4000)
    public void testExpandCommandLinePath_withModule() {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setModuleOutputPathPrefix("mod_");
        runner.getCommandLineConfig().setModule(Arrays.asList("m:1"));
        CompilerOptions options = new CompilerOptions();
        options.sourceMapOutputPath = "map%outname%.map";
        JSModule module = new JSModule("m1");
        String expanded = runner.expandSourceMapPath(options, module);
        // sub = moduleOutputPathPrefix + moduleName + ".js" = "mod_m1.js"
        assertEquals("mapmod_m1.js.map", expanded);
    }

    @Test(timeout = 4000)
    public void testExpandManifest() {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setOutputManifest("manifest%outname%.txt");
        runner.getCommandLineConfig().setModuleOutputPathPrefix("mod_");
        runner.getCommandLineConfig().setModule(Arrays.asList("m:1"));
        JSModule module = new JSModule("m1");
        String expanded = runner.expandManifest(module);
        assertEquals("manifestmod_m1.js.txt", expanded);
    }

    // Test processResults with various print flags (to cover early returns)
    @Test(timeout = 4000)
    public void testProcessResults_printTree_withErrors() throws Exception {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setPrintTree(true);
        // When there are errors, compiler.getRoot() returns null
        // We'll create a Result with errors to trigger the crash path? Actually the code checks compiler.getRoot().
        // We need a compiler that returns null root.
        Compiler compiler = new Compiler() {
            @Override
            public Node getRoot() {
                return null; // simulate error state
            }
        };
        // Since processResults is non-static, we need to set the compiler field.
        // We can do via reflection or by subclass. Simpler: we can use the runner's super implementation.
        // But runner.compiler is private. We'll create a subclass that overrides getCompiler().
        TestRunner runnerWithMock = new TestRunner(System.out, System.err) {
            @Override
            protected Compiler getCompiler() {
                return compiler;
            }
        };
        Result result = new Result(null, 0, null, 0, 0, 0, null, null, null);
        int exitCode = runnerWithMock.processResults(result, null, new CompilerOptions());
        // Should return 1 because root is null and printTree
        assertEquals(1, exitCode);
    }

    @Test(timeout = 4000)
    public void testProcessResults_printTree_success() throws Exception {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setPrintTree(true);
        // We need a compiler with non-null root
        Compiler compiler = new Compiler();
        // Root can be a synthetic node
        compiler.initOptions(new CompilerOptions());
        // Create a minimal AST
        Node root = new Node(1); // just a placeholder
        // We'll set it via reflection? Simpler: use a mock-like approach.
        // Since we are testing logic after root is obtained, we can create a compiler that overrides getRoot.
        Compiler mockCompiler = new Compiler() {
            @Override
            public Node getRoot() {
                return new Node(1);
            }
        };
        TestRunner runnerWithMock = new TestRunner(System.out, System.err) {
            @Override
            protected Compiler getCompiler() {
                return mockCompiler;
            }
        };
        Result result = new Result(null, 0, null, 0, 0, 0, null, null, null);
        // We'll capture output to avoid writing to System.out.
        // For simplicity, we test that it returns 0.
        int exitCode = runnerWithMock.processResults(result, null, new CompilerOptions());
        assertEquals(0, exitCode);
    }

    @Test(timeout = 4000)
    public void testProcessResults_printPassGraph_noRoot() throws Exception {
        TestRunner runner = new TestRunner(System.out, System.err);
        runner.getCommandLineConfig().setPrintPassGraph(true);
        Compiler mockCompiler = new Compiler() {
            @Override
            public Node getRoot() {
                return null;
            }
        };
        TestRunner runnerWithMock = new TestRunner(System.out, System.err) {
            @Override
            protected Compiler getCompiler() {
                return mockCompiler;
            }
        };
        Result result = new Result(null, 0, null, 0, 0, 0, null, null, null);
        int exitCode = runnerWithMock.processResults(result, null, new CompilerOptions());
        assertEquals(1, exitCode);
    }

    // Additional test for createInputs via createSourceInputs with no files (default stdin)
    // This test may require System.in input; we'll just check that it returns a list of size 1.
    @Test(timeout = 4000)
    public void testCreateSourceInputs_defaultStdin() throws Exception {
        List<JSSourceFile> inputs = AbstractCommandLineRunner.createSourceInputs(Collections.<String>emptyList());
        assertEquals(1, inputs.size());
        assertEquals("stdin", inputs.get(0).getName());
    }

    // Test for createExternInputs with empty list -> returns /dev/null
    @Test(timeout = 4000)
    public void testCreateExternInputs_defaultExtern() throws Exception {
        List<JSSourceFile> externs = AbstractCommandLineRunner.createExternInputs(Collections.<String>emptyList());
        assertEquals(1, externs.size());
        assertEquals("/dev/null", externs.get(0).getName());
    }

    // Test for maybeCreateDirsForPath (private) - we can't call directly, but it's covered when outputting module files.
}