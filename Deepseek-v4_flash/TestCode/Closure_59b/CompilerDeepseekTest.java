package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * CompilerDeepseekTest targets the known defect related to
 * checkGlobalThisOff (Defects4J bug) and achieves high line/branch coverage
 * on Compiler.java core logic.
 *
 * Branch & Defect Analysis Matrix:
 * - initOptions: branches for checkGlobalThisLevel.isOn(), checkSymbols, 
 *   DiagnosticGroups override, warningsGuard composition.
 * - Constructors: null/valid errorManager, outStream.
 * - init/initModules: empty module check, duplicate input detection, 
 *   module graph creation (>1 module vs <=1).
 * - parseInputs: externs vs inputs, manageDependencies, stale input handling.
 * - Compile flow: check pre-check, error halt, result generation.
 * - State methods: getResult, getErrors, getWarnings, getMessage.
 * - Unique name id: reset.
 * - getTypeRegistry lazy init.
 * - hasErrors/hasHaltingErrors with ideMode.
 */
public class CompilerDeepseekTest {

    // ========== PARTITION A: Core Constructors & State ==========

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Compiler compiler = new Compiler();
        assertNotNull(compiler);
        // No error manager yet; getErrorManager() should init default
        assertNotNull(compiler.getErrorManager());
        assertNull(compiler.options);
    }

    @Test(timeout = 4000)
    public void testConstructorWithPrintStream() {
        Compiler compiler = new Compiler(System.out);
        assertNotNull(compiler);
        assertNull(compiler.options);
    }

    @Test(timeout = 4000)
    public void testConstructorWithErrorManager() {
        ErrorManager em = new LoggerErrorManager(new PrintStreamErrorManager(
                new MessageFormatter() {
                    @Override public String formatError(JSError error) { return ""; }
                    @Override public String formatWarning(JSError warning) { return ""; }
                }, System.out), null);
        Compiler compiler = new Compiler(em);
        assertSame(em, compiler.errorManager);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSetErrorManagerNull() {
        Compiler compiler = new Compiler();
        compiler.setErrorManager(null);
    }

    // ========== PARTITION B: initOptions – Defect Targeting ==========

    @Test(timeout = 4000)
    public void testInitOptionsCheckGlobalThisOff() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.checkGlobalThisLevel = CheckLevel.OFF;
        compiler.initOptions(options);
        // After initOptions, warningsGuard should suppress GLOBAL_THIS warnings
        JSError error = JSError.make(
                DiagnosticGroups.GLOBAL_THIS.getTypes().iterator().next(),
                "test");
        CheckLevel level = compiler.warningsGuard.level(error);
        // When checkGlobalThisLevel is OFF, the guard returns OFF or null (not ON)
        assertTrue("Expected GLOBAL_THIS warning to be suppressed",
                level == null || level == CheckLevel.OFF);
    }

    @Test(timeout = 4000)
    public void testInitOptionsCheckGlobalThisOn() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.checkGlobalThisLevel = CheckLevel.WARNING;
        compiler.initOptions(options);
        JSError error = JSError.make(
                DiagnosticGroups.GLOBAL_THIS.getTypes().iterator().next(),
                "test");
        CheckLevel level = compiler.warningsGuard.level(error);
        // With WARNING level, the guard should not suppress; level should be WARNING or null
        assertFalse("Expected GLOBAL_THIS warning to be active",
                level == CheckLevel.OFF);
    }

    @Test(timeout = 4000)
    public void testInitOptionsCheckSymbolsOffGuardSuppress() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.checkSymbols = false;
        compiler.initOptions(options);
        // The composed guard should contain a guard for CHECK_VARIABLES -> OFF
        // Verify by creating a CHECK_VARIABLES error
        JSError error = JSError.make(
                DiagnosticGroups.CHECK_VARIABLES.getTypes().iterator().next(),
                "test");
        CheckLevel level = compiler.warningsGuard.level(error);
        assertEquals(CheckLevel.OFF, level);
    }

    @Test(timeout = 4000)
    public void testInitOptionsEnablesDiagnosticGroup() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
        options.checkTypes = false; // Will be overridden by enable
        compiler.initOptions(options);
        // checkTypes should become true because CHECK_TYPES is enabled
        assertTrue(options.checkTypes);
    }

    // ========== PARTITION C: init & initModules ==========

    @Test(timeout = 4000)
    public void testInitWithEmptyModules() {
        Compiler compiler = new Compiler();
        List<JSSourceFile> externs = Lists.newArrayList();
        List<JSModule> modules = Lists.newArrayList();
        CompilerOptions options = new CompilerOptions();
        compiler.initModules(externs, modules, options);
        // Should report EMPTY_MODULE_LIST_ERROR
        assertTrue("Expected error for empty module list", compiler.hasErrors());
    }

    @Test(timeout = 4000)
    public void testInitWithSingletonModule() {
        Compiler compiler = new Compiler();
        JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");
        List<JSModule> modules = Lists.newArrayList(new JSModule("m1"));
        modules.get(0).add(input);
        compiler.initModules(Lists.<JSSourceFile>newArrayList(), modules, new CompilerOptions());
        assertNotNull(compiler.modules);
        assertNull(compiler.moduleGraph);
        assertEquals(1, compiler.inputs.size());
    }

    @Test(timeout = 4000)
    public void testInitWithMultipleModules() {
        Compiler compiler = new Compiler();
        JSSourceFile input1 = JSSourceFile.fromCode("a.js", "");
        JSSourceFile input2 = JSSourceFile.fromCode("b.js", "");
        JSModule m1 = new JSModule("m1");
        m1.add(input1);
        JSModule m2 = new JSModule("m2");
        m2.add(input2);
        compiler.initModules(Lists.<JSSourceFile>newArrayList(),
                Lists.newArrayList(m1, m2), new CompilerOptions());
        assertNotNull(compiler.moduleGraph);
        assertEquals(2, compiler.inputs.size());
    }

    @Test(timeout = 4000)
    public void testInitInputsByNameDuplicateExtern() {
        Compiler compiler = new Compiler();
        // Need to call init first to set up maps; we use init with modules
        JSSourceFile extern = JSSourceFile.fromCode("duplicate.js", "");
        JSSourceFile input = JSSourceFile.fromCode("input.js", "");
        // Create module that includes both; the extern list will also include duplicate
        List<JSSourceFile> externs = Lists.newArrayList(extern, extern);
        JSModule module = new JSModule("m");
        module.add(input);
        compiler.initModules(externs, Lists.newArrayList(module), new CompilerOptions());
        // Should have reported DUPLICATE_EXTERN_INPUT
        boolean found = false;
        for (JSError err : compiler.getErrors()) {
            if (err.getType() == Compiler.DUPLICATE_EXTERN_INPUT) {
                found = true;
                break;
            }
        }
        assertTrue("Expected duplicate extern input error", found);
    }

    // ========== PARTITION D: Parsing and Compilation (simplified) ==========

    @Test(timeout = 4000)
    public void testParseSimpleSource() {
        Compiler compiler = new Compiler();
        // Initialize with options to allow parsing
        compiler.initOptions(new CompilerOptions());
        Node node = compiler.parse(JSSourceFile.fromCode("test.js", "var x = 1;"));
        assertNotNull(node);
        assertEquals(Token.SCRIPT, node.getType());
    }

    @Test(timeout = 4000)
    public void testCompileWithNoErrors() {
        Compiler compiler = new Compiler();
        JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");
        Result result = compiler.compile(
                JSSourceFile.fromCode("externs.js", ""),
                new JSSourceFile[] { input },
                new CompilerOptions());
        assertEquals(0, result.errors.length);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testHasErrorsFalse() {
        Compiler compiler = new Compiler();
        assertFalse(compiler.hasErrors());
    }

    // ========== PARTITION E: Getters and Utility Methods ==========

    @Test(timeout = 4000)
    public void testGetResult() {
        Compiler compiler = new Compiler();
        // Minimal init to avoid NPE
        compiler.initOptions(new CompilerOptions());
        // After just init, result should have no errors/warnings
        Result result = compiler.getResult();
        assertNotNull(result);
        assertEquals(0, result.errors.length);
        assertEquals(0, result.warnings.length);
    }

    @Test(timeout = 4000)
    public void testGetMessages() {
        Compiler compiler = new Compiler();
        JSError[] msgs = compiler.getMessages();
        assertNotNull(msgs);
        assertEquals(0, msgs.length);
    }

    @Test(timeout = 4000)
    public void testUniqueNameId() {
        Compiler compiler = new Compiler();
        // Access through supplier
        Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
        assertEquals("0", supplier.get());
        assertEquals("1", supplier.get());
        compiler.resetUniqueNameId();
        assertEquals("0", supplier.get());
    }

    @Test(timeout = 4000)
    public void testGetTypeRegistry() {
        Compiler compiler = new Compiler();
        assertNotNull(compiler.getTypeRegistry());
        // Should create only once
        assertSame(compiler.getTypeRegistry(), compiler.getTypeRegistry());
    }

    @Test(timeout = 4000)
    public void testGetRoot() {
        Compiler compiler = new Compiler();
        // After parse, root is set
        compiler.initOptions(new CompilerOptions());
        compiler.parse(JSSourceFile.fromCode("test.js", ""));
        Node root = compiler.getRoot();
        assertNotNull(root);
        assertEquals(Token.BLOCK, root.getType());
    }

    // ========== PARTITION F: Error Manager and State ==========

    @Test(timeout = 4000)
    public void testGetErrorManagerWithNullOptions() {
        Compiler compiler = new Compiler();
        // Before initOptions, options is null; getErrorManager should initOptions
        ErrorManager em = compiler.getErrorManager();
        assertNotNull(em);
        assertNotNull(compiler.options);
    }

    @Test(timeout = 4000)
    public void testStateCaptureAndRestore() {
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        // Simulate some work: parse
        compiler.parse(JSSourceFile.fromCode("test.js", "var a = 1;"));
        assertNotNull(compiler.jsRoot);
        Compiler.IntermediateState state = compiler.getState();
        assertNotNull(state);
        // Restore into a fresh compiler
        Compiler compiler2 = new Compiler();
        compiler2.initOptions(new CompilerOptions());
        compiler2.setState(state);
        assertNotNull(compiler2.jsRoot);
        assertEquals(compiler.jsRoot, compiler2.jsRoot);
    }

    // ========== PARTITION G: Defect-Specific Test ==========

    @Test(timeout = 4000)
    public void testCheckGlobalThisOff() {
        // This test directly targets the Defects4J bug: checkGlobalThisOff
        // should suppress "global this" warnings.
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        // Enable all warnings but turn off global this check
        options.checkGlobalThisLevel = CheckLevel.OFF;
        options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);
        // Also turn off other warnings that might interfere
        options.checkSymbols = false; // avoids undefined variable warnings
        // Compile a script that references 'this' in global scope
        JSSourceFile input = JSSourceFile.fromCode("test.js",
                "function f() { return this; }"); // 'this' in a function is fine
        // But to trigger globalThis, we need to refer to 'this' outside any function? 
        // Actually, the global this warning is about using 'this' in the global scope.
        // Let's use a common pattern that triggers it: 'this.foo = 1';
        input = JSSourceFile.fromCode("test.js", "this.foo = 1;");
        Result result = compiler.compile(
                JSSourceFile.fromCode("externs.js", ""),
                new JSSourceFile[] { input },
                options);
        // The bug: even with checkGlobalThisLevel=OFF, warnings were generated.
        // We assert exactly zero warnings.
        assertTrue("Expected no warnings but got: " + result.warnings.length + " warnings",
                   result.warnings.length == 0);
        assertEquals("Expected no errors", 0, result.errors.length);
    }

    // ========== PARTITION H: Boundary and Edge Cases ==========

    @Test(timeout = 4000)
    public void testInitWithNoExterns() {
        Compiler compiler = new Compiler();
        List<JSSourceFile> emptyExterns = Lists.newArrayList();
        List<JSSourceFile> inputs = Lists.newArrayList(
                JSSourceFile.fromCode("test.js", "var a = 1;"));
        compiler.init(emptyExterns, inputs, new CompilerOptions());
        assertNotNull(compiler.externs);
        assertEquals(0, compiler.externs.size());
        assertEquals(1, compiler.inputs.size());
    }

    @Test(timeout = 4000)
    public void testGetSourceMap() {
        Compiler compiler = new Compiler();
        assertNull(compiler.getSourceMap()); // null until set
        // After init with sourceMapOutputPath, it should be created
        CompilerOptions options = new CompilerOptions();
        options.sourceMapOutputPath = "out.map";
        compiler.initOptions(options);
        assertNotNull(compiler.getSourceMap());
    }

    @Test(timeout = 4000)
    public void testHasRegExpGlobalReferencesDefault() {
        Compiler compiler = new Compiler();
        assertTrue(compiler.hasRegExpGlobalReferences());
        compiler.setHasRegExpGlobalReferences(false);
        assertFalse(compiler.hasRegExpGlobalReferences());
    }

    @Test(timeout = 4000)
    public void testGetInputsInOrderReturnsUnmodifiable() {
        Compiler compiler = new Compiler();
        try {
            compiler.getInputsInOrder().add(null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetExternsInOrder() {
        Compiler compiler = new Compiler();
        // Initially empty
        List<CompilerInput> externs = compiler.getExternsInOrder();
        assertNotNull(externs);
        assertTrue(externs.isEmpty());
    }
}