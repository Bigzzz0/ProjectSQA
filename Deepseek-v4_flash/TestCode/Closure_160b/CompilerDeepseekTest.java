package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import org.junit.Test;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

/**
 * White-box test suite for Compiler.java targeting constructors, core public APIs,
 * key decision branches, and the known defect from Defects4J.
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructors: default, PrintStream, ErrorManager; null/valid args.
 * - setErrorManager: null check (Preconditions).
 * - initOptions: checkTypes enable/disable/override; checkGlobalThis;
 *   checkSymbols false -> add CHECK_VARIABLES guard; quiet flag interactions.
 * - initModules: empty module list -> error; first module empty -> error;
 *   module graph creation.
 * - initInputsByNameMap: duplicate inputs/externs -> error.
 * - hasErrors: after report, ideMode flag.
 * - getResult: state fields.
 * - parseSyntheticCode: with/without compiler options.
 * - toSource: null root, empty root.
 * - resetUniqueNameId: id counter.
 * - compile() pipeline: precheck, check, optimize; error early exit.
 * - Defect-specific: When options.checkSymbols is false AND a warning level
 *   for CHECK_VARIABLES is explicitly set, the compiler should respect the
 *   explicit level. The known defect causes a warning to be suppressed
 *   incorrectly (expected 1 warning, got 0).
 */
public class CompilerDeepseekTest {

  // --- Helper to create a Compiler with a recording ErrorManager ---
  private static class RecordingErrorManager extends BasicErrorManager {
    @Override
    public void report(CheckLevel level, JSError error) {
      super.report(level, error);
    }
  }

  private RecordingErrorManager createErrorManager() {
    return new RecordingErrorManager();
  }

  private Compiler createCompilerWithRecordingManager() {
    RecordingErrorManager em = createErrorManager();
    Compiler compiler = new Compiler(em);
    return compiler;
  }

  // ========== Partition A: Core Functional Logic & State ==========

  @Test(timeout = 4000)
  public void testDefaultConstructor() {
    Compiler c = new Compiler();
    assertNotNull(c);
    // No error manager set initially, but it will be created lazily
    assertNotNull(c.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testConstructorWithPrintStream() {
    PrintStream ps = System.out;
    Compiler c = new Compiler(ps);
    assertNotNull(c);
  }

  @Test(timeout = 4000)
  public void testConstructorWithErrorManager() {
    RecordingErrorManager em = createErrorManager();
    Compiler c = new Compiler(em);
    assertSame(em, c.getErrorManager());
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetErrorManagerNullThrows() {
    Compiler c = new Compiler();
    c.setErrorManager(null);
  }

  @Test(timeout = 4000)
  public void testInitOptionsCheckTypesEnabled() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkTypes = true;
    c.initOptions(opts);
    assertTrue(opts.checkTypes);
  }

  @Test(timeout = 4000)
  public void testInitOptionsCheckTypesDisabledByDiagnosticGroup() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkTypes = true;
    opts.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    c.initOptions(opts);
    assertFalse(opts.checkTypes);
  }

  @Test(timeout = 4000)
  public void testInitOptionsCheckGlobalThis() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkGlobalThisLevel = CheckLevel.WARNING;
    c.initOptions(opts);
    // The warning level for GLOBAL_THIS should be set to WARNING
    assertEquals(CheckLevel.WARNING,
        opts.getWarningLevel(DiagnosticGroups.GLOBAL_THIS));
  }

  @Test(timeout = 4000)
  public void testInitOptionsCheckSymbolsFalseAddsGuard() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkSymbols = false;
    c.initOptions(opts);
    // The warningsGuard should have a guard that turns off CHECK_VARIABLES
    CheckLevel level = c.getErrorLevel(
        JSError.make("test", -1, -1, DiagnosticGroups.CHECK_VARIABLES, "msg"));
    assertNull("When checkSymbols is false, CHECK_VARIABLES should be off (level null)", level);
  }

  @Test(timeout = 4000)
  public void testInitOptionsCheckSymbolsFalseWithExplicitWarning() {
    // This directly targets the known defect: when checkSymbols is false but
    // the user explicitly sets CHECK_VARIABLES to WARNING, the compiler
    // should respect that and not suppress the warning.
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkSymbols = false;
    opts.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
    c.initOptions(opts);
    CheckLevel level = c.getErrorLevel(
        JSError.make("test", -1, -1, DiagnosticGroups.CHECK_VARIABLES, "msg"));
    assertEquals("Explicit warning level should override the guard",
        CheckLevel.WARNING, level);
  }

  // ========== Partition B: Boundary Value Analysis ==========

  @Test(timeout = 4000, expected = RuntimeException.class)
  public void testInitWithEmptyModuleList() {
    Compiler c = new Compiler();
    List<JSSourceFile> externs = Collections.emptyList();
    List<JSModule> modules = Collections.emptyList();
    c.initModules(externs, modules, new CompilerOptions());
    // Should report an error and set errors
    assertTrue(c.hasErrors());
  }

  @Test(timeout = 4000)
  public void testInitWithSingleModule() {
    Compiler c = new Compiler();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    List<JSSourceFile> externs = ImmutableList.of(extern);
    List<JSModule> modules = ImmutableList.of(
        new JSModule("m1").add(input));
    c.initModules(externs, modules, new CompilerOptions());
    assertFalse("No errors expected", c.hasErrors());
    assertEquals(1, c.getInputsForTesting().size());
  }

  @Test(timeout = 4000)
  public void testInitWithMultipleModules() {
    Compiler c = new Compiler();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("a.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("b.js", "var b = 2;");
    JSModule m1 = new JSModule("m1").add(input1);
    JSModule m2 = new JSModule("m2").add(input2);
    // Set dependency: m1 before m2
    m2.addDependency(m1);
    List<JSSourceFile> externs = ImmutableList.of(extern);
    List<JSModule> modules = ImmutableList.of(m1, m2);
    c.initModules(externs, modules, new CompilerOptions());
    assertFalse("No errors expected", c.hasErrors());
    assertNotNull(c.getModuleGraph());
  }

  @Test(timeout = 4000)
  public void testInitWithDuplicateInput() {
    Compiler c = new Compiler();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("same.js", "var x = 1;");
    List<JSSourceFile> externs = ImmutableList.of(extern);
    // Duplicate input: same file in two modules
    JSModule m1 = new JSModule("m1").add(input);
    JSModule m2 = new JSModule("m2").add(input);
    List<JSModule> modules = ImmutableList.of(m1, m2);
    c.initModules(externs, modules, new CompilerOptions());
    assertTrue("Should report duplicate input error", c.hasErrors());
  }

  @Test(timeout = 4000)
  public void testParseSyntheticCode() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    Node n = c.parseSyntheticCode("var a = 1;");
    assertNotNull(n);
    assertEquals(Token.SCRIPT, n.getType());
  }

  @Test(timeout = 4000)
  public void testParseSyntheticCodeWithFileName() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    Node n = c.parseSyntheticCode("test.js", "var b = 2;");
    assertNotNull(n);
    assertEquals("test.js", n.getSourceFileName());
  }

  @Test(timeout = 4000)
  public void testResetUniqueNameId() {
    Compiler c = new Compiler();
    c.resetUniqueNameId();
    // After reset, next id should be 0
    // Access via getUniqueNameIdSupplier()
    String id = c.getUniqueNameIdSupplier().get();
    assertEquals("0", id);
  }

  // ========== Partition C: Defect-Targeted Branch Zone ==========

  @Test(timeout = 4000)
  public void testCheckSymbolsOverrideForQuiet() {
    // Reproduce the defect: when checkSymbols is false, but we explicitly
    // set a warning level for CHECK_VARIABLES, the compiler should still
    // report warnings for undefined variables. The known bug suppresses
    // the warning.
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkSymbols = false;
    opts.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
    c.initOptions(opts);

    // Simulate reporting an undefined variable error
    JSError error = JSError.make("test.js", 1, 0,
        DiagnosticGroups.CHECK_VARIABLES, "variable x is undefined");
    c.report(error);

    // The error manager should have exactly one warning
    assertEquals("Expected 1 warning", 1, c.getWarningCount());
    assertEquals("Expected 0 errors", 0, c.getErrorCount());
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000)
  public void testHasErrorsAfterReportingError() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    c.report(JSError.make("test", -1, -1, DiagnosticGroups.CHECK_VARIABLES, "err"));
    assertTrue(c.hasErrors());
  }

  @Test(timeout = 4000)
  public void testHasErrorsFalseInIdeMode() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.ideMode = true;
    c.initOptions(opts);
    c.report(JSError.make("test", -1, -1, DiagnosticGroups.CHECK_VARIABLES, "err"));
    assertFalse("IDE mode should not halt on errors", c.hasErrors());
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testGetNodeForCodeInsertionWithNullModuleAndNoInputs() {
    Compiler c = new Compiler();
    c.getNodeForCodeInsertion(null);
  }

  @Test(timeout = 4000)
  public void testToSourceNullJsRoot() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    // No parsing, jsRoot is null
    String src = c.toSource();
    assertEquals("", src);
  }

  // ========== Partition E: Object Lifecycle & Contract ==========

  @Test(timeout = 4000)
  public void testGetResultNotNullAfterInit() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    Result result = c.getResult();
    assertNotNull(result);
    assertNotNull(result.errors);
    assertNotNull(result.warnings);
  }

  @Test(timeout = 4000)
  public void testGetStateAndSetState() {
    Compiler c = new Compiler();
    // Minimal initialization
    CompilerOptions opts = new CompilerOptions();
    c.initOptions(opts);
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    c.init(ImmutableList.of(extern), ImmutableList.of(input), opts);
    // Parse
    Node root = c.parseInputs();
    assertNotNull(root);
    // Capture state
    Compiler.IntermediateState state = c.getState();
    assertNotNull(state);
    // Set state back (should work)
    c.setState(state);
    assertNotNull(c.getRoot());
  }

  @Test(timeout = 4000)
  public void testGetSourceLineAndRegion() {
    Compiler c = new Compiler();
    c.initCompilerOptionsIfTesting();
    String sourceName = "test.js";
    String code = "var x = 1;\nvar y = 2;";
    c.parseSyntheticCode(sourceName, code);
    assertEquals("var x = 1;", c.getSourceLine(sourceName, 1));
    assertEquals("var y = 2;", c.getSourceLine(sourceName, 2));
    assertNull(c.getSourceLine(sourceName, 0));
    Region region = c.getSourceRegion(sourceName, 1);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
  }

  @Test(timeout = 4000)
  public void testCompileWithError() {
    // Compile a file that will cause a parse error
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.setLanguageIn(LanguageMode.ECMASCRIPT5);
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = ;"); // syntax error
    Result result = c.compile(extern, input, opts);
    assertTrue("Expected compilation errors", result.success == false);
    assertTrue(result.errors.length > 0);
  }

  @Test(timeout = 4000)
  public void testCompileSuccess() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    Result result = c.compile(extern, input, opts);
    assertTrue("Compilation should succeed", result.success);
  }

  @Test(timeout = 4000)
  public void testSetPassConfigTwiceThrows() {
    Compiler c = new Compiler();
    PassConfig pc = new DefaultPassConfig(new CompilerOptions());
    c.setPassConfig(pc);
    try {
      c.setPassConfig(pc);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }
}