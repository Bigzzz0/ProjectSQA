package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Test suite for {@link Compiler} targeting key branches, boundary conditions,
 * and the known defect in ES5 strict multi-input handling.
 *
 * <p>[Branch & Defect Analysis Matrix]
 * <ul>
 *   <li>Constructors: default, stream, error manager</li>
 *   <li>Error manager null checks</li>
 *   <li>initOptions with various option configurations</li>
 *   <li>compile with empty inputs, single input, multiple inputs</li>
 *   <li>toSource / toSourceArray / module-specific outputs</li>
 *   <li>State management: getState/setState, pass config lifecycle</li>
 *   <li>CodeBuilder: append, line/column tracking, endsWith</li>
 *   <li>Input handling: getInput, removeInput, newExternInput</li>
 *   <li>Parser setup: parseSyntheticCode, initCompilerOptionsIfTesting</li>
 *   <li>RegExp global references toggle</li>
 *   <li>ES5 strict defect: multiple inputs must emit exactly one 'use strict'</li>
 * </ul>
 */
public class CompilerDeepseekTest {

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private CompilerOptions createOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5);
    return options;
  }

  private Compiler compileSimple(String... inputs) {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    JSSourceFile[] sourceInputs = new JSSourceFile[inputs.length];
    for (int i = 0; i < inputs.length; i++) {
      sourceInputs[i] = JSSourceFile.fromCode("input" + i, inputs[i]);
    }
    compiler.compile(new JSSourceFile[0], sourceInputs, options);
    return compiler;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testDefaultConstructor() {
    Compiler compiler = new Compiler();
    assertNotNull(compiler);
    assertNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstructorWithStream() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(baos);
    Compiler compiler = new Compiler(stream);
    assertNotNull(compiler);
    // Error manager should be created lazily via initOptions
    compiler.initOptions(createOptions());
    assertNotNull(compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testConstructorWithErrorManager() {
    ErrorManager em = new LoggerErrorManager(
        new PlainTextFormatter(), java.util.logging.Logger.getAnonymousLogger());
    Compiler compiler = new Compiler(em);
    assertNotNull(compiler);
    assertEquals(em, compiler.getErrorManager());
  }

  // -------------------------------------------------------------------------
  // Error manager and option initialization
  // -------------------------------------------------------------------------

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    new Compiler().setErrorManager(null);
  }

  @Test(timeout = 4000)
  public void testInitOptionsWithDefaultOptions() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    assertNotNull(compiler.getErrorManager());
    assertNotNull(compiler.getWarningsGuardForTesting());
  }

  @Test(timeout = 4000)
  public void testInitOptionsSetsWarningsGuard() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    compiler.initOptions(options);
    assertNotNull(compiler.getWarningsGuardForTesting());
  }

  // -------------------------------------------------------------------------
  // Compilation basics
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCompileEmptyInputsReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    Result result = compiler.compile(
        new JSSourceFile[0], new JSSourceFile[0], options);
    assertNotNull(result);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCompileWithSingleInput() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    JSSourceFile input = JSSourceFile.fromCode("test", "var x = 1;");
    Result result = compiler.compile(new JSSourceFile[0], new JSSourceFile[]{input}, options);
    assertFalse(compiler.hasErrors());
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.toSource());
  }

  @Test(timeout = 4000)
  public void testCompileWithMultipleInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    JSSourceFile[] inputs = new JSSourceFile[]{
        JSSourceFile.fromCode("a", "var a = 1;"),
        JSSourceFile.fromCode("b", "var b = 2;")
    };
    Result result = compiler.compile(new JSSourceFile[0], inputs, options);
    assertFalse(compiler.hasErrors());
    String[] sources = compiler.toSourceArray();
    assertEquals(2, sources.length);
  }

  @Test(timeout = 4000)
  public void testCompileWithNoExterns() {
    Compiler compiler = new Compiler();
    compiler.compile(
        JSSourceFile.fromCode("ext", "var ext;"),
        JSSourceFile.fromCode("main", "var x;"),
        createOptions());
    assertFalse(compiler.hasErrors());
  }

  // -------------------------------------------------------------------------
  // ES5 strict defect (FAILS on defective version)
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testEs5StrictUseStrictMultipleInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5_STRICT);
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[]{
        JSSourceFile.fromCode("in1", "var a = 1;"),
        JSSourceFile.fromCode("in2", "var b = 2;")
    };
    compiler.compile(externs, inputs, options);
    assertFalse("Compilation should succeed", compiler.hasErrors());
    String output = compiler.toSource();
    int useStrictCount = 0;
    int idx = output.indexOf("use strict");
    while (idx != -1) {
      useStrictCount++;
      idx = output.indexOf("use strict", idx + 1);
    }
    assertEquals("Exactly one 'use strict' directive should be emitted", 1, useStrictCount);
    assertTrue("'use strict' should appear at the very beginning",
        output.startsWith("\"use strict\"") || output.startsWith("'use strict'"));
  }

  // -------------------------------------------------------------------------
  // Source output methods
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testToSourceArray() {
    Compiler compiler = compileSimple("var a=1;", "var b=2;");
    String[] sources = compiler.toSourceArray();
    assertEquals(2, sources.length);
    assertFalse(sources[0].isEmpty());
    assertFalse(sources[1].isEmpty());
  }

  @Test(timeout = 4000)
  public void testToSourceWithModule() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    JSModule module = new JSModule("m");
    module.add(JSSourceFile.fromCode("a", "var a=1;"));
    module.add(JSSourceFile.fromCode("b", "var b=2;"));
    compiler.compileModules(new JSSourceFile[0], java.util.Arrays.asList(module), options);
    String src = compiler.toSource(module);
    assertNotNull(src);
    assertTrue(src.contains("var a=1"));
    assertTrue(src.contains("var b=2"));
  }

  @Test(timeout = 4000)
  public void testToSourceArrayWithModule() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    JSModule module = new JSModule("m");
    module.add(JSSourceFile.fromCode("a", "var a=1;"));
    module.add(JSSourceFile.fromCode("b", "var b=2;"));
    compiler.compileModules(new JSSourceFile[0], java.util.Arrays.asList(module), options);
    String[] sources = compiler.toSourceArray(module);
    assertEquals(2, sources.length);
  }

  @Test(timeout = 4000)
  public void testToSourceArrayWithEmptyModule() {
    Compiler compiler = new Compiler();
    compiler.initOptions(createOptions());
    JSModule module = new JSModule("empty");
    String[] sources = compiler.toSourceArray(module);
    assertEquals(0, sources.length);
  }

  // -------------------------------------------------------------------------
  // CodeBuilder inner class
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCodeBuilderAppendAndCounters() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("hello\nworld");
    assertEquals(11, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(5, cb.getColumnIndex());
    assertTrue(cb.endsWith("world"));
    assertFalse(cb.endsWith("hello"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex()); // reset does not affect line count
    assertEquals(0, cb.getColumnIndex());
  }

  @Test(timeout = 4000)
  public void testCodeBuilderAppendNoNewLine() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("abc");
    assertEquals(0, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
  }

  // -------------------------------------------------------------------------
  // Input management
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testGetInput() {
    Compiler compiler = compileSimple("var a=1;", "var b=2;");
    CompilerInput input = compiler.getInput("input0");
    assertNotNull(input);
    assertEquals("input0", input.getName());
  }

  @Test(timeout = 4000)
  public void testRemoveInput() {
    Compiler compiler = compileSimple("var a=1;");
    compiler.removeInput("input0");
    assertNull(compiler.getInput("input0"));
  }

  @Test(timeout = 4000)
  public void testRemoveInputNonExistent() {
    Compiler compiler = compileSimple("var a=1;");
    compiler.removeInput("nonexistent");
    // should not throw
  }

  @Test(timeout = 4000)
  public void testNewExternInput() {
    Compiler compiler = new Compiler();
    compiler.initOptions(createOptions());
    CompilerInput extern = compiler.newExternInput("testExtern");
    assertNotNull(extern);
    assertEquals("testExtern", extern.getName());
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNewExternInputDuplicateThrows() {
    Compiler compiler = new Compiler();
    compiler.initOptions(createOptions());
    compiler.newExternInput("dup");
    compiler.newExternInput("dup");
  }

  // -------------------------------------------------------------------------
  // Parsing and synthetic code
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testParseSyntheticCode() {
    Compiler compiler = new Compiler();
    Node node = compiler.parseSyntheticCode("var a=1;");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
  }

  @Test(timeout = 4000)
  public void testParseSyntheticCodeWithName() {
    Compiler compiler = new Compiler();
    Node node = compiler.parseSyntheticCode("myFile", "var a=1;");
    assertNotNull(node);
  }

  @Test(timeout = 4000)
  public void testInitCompilerOptionsIfTesting() {
    Compiler compiler = new Compiler();
    assertNull(compiler.options);
    compiler.parseSyntheticCode("var a;");
    assertNotNull(compiler.options);
  }

  // -------------------------------------------------------------------------
  // State and pass configuration
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testSetPassConfig() {
    Compiler compiler = new Compiler();
    PassConfig custom = new DefaultPassConfig(createOptions());
    compiler.setPassConfig(custom);
    assertSame(custom, compiler.getPassConfig());
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testSetPassConfigTwiceThrows() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(new DefaultPassConfig(createOptions()));
    compiler.setPassConfig(new DefaultPassConfig(createOptions()));
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetPassConfigNullThrows() {
    new Compiler().setPassConfig(null);
  }

  @Test(timeout = 4000)
  public void testGetStateSetState() {
    Compiler compiler = compileSimple("var a=1;");
    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);
    Compiler compiler2 = new Compiler();
    compiler2.initOptions(createOptions());
    compiler2.setState(state);
    assertNotNull(compiler2.getRoot());
  }

  // -------------------------------------------------------------------------
  // Misc getters and configuration
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testHasRegExpGlobalReferencesDefault() {
    Compiler compiler = new Compiler();
    assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testAcceptEcmaScript5ForES5() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
  }

  @Test(timeout = 4000)
  public void testAcceptEcmaScript5ForES3() {
    Compiler compiler = new Compiler();
    CompilerOptions options = createOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());
  }

  @Test(timeout = 4000)
  public void testLanguageMode() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
  }

  @Test(timeout = 4000)
  public void testGetSourceLine() {
    Compiler compiler = compileSimple("var a=1;\nvar b=2;");
    String line = compiler.getSourceLine("input0", 2);
    assertNotNull(line);
    assertEquals("var b=2;", line.trim());
  }

  @Test(timeout = 4000)
  public void testGetSourceLineInvalidNumber() {
    Compiler compiler = compileSimple("var a=1;");
    assertNull(compiler.getSourceLine("input0", 0));
    assertNull(compiler.getSourceLine("input0", -1));
  }

  @Test(timeout = 4000)
  public void testGetSourceRegion() {
    Compiler compiler = compileSimple("var a=1;");
    Region region = compiler.getSourceRegion("input0", 1);
    assertNotNull(region);
    assertEquals("var a=1;", region.getSourceExcerpt());
  }

  @Test(timeout = 4000)
  public void testGetNodeForCodeInsertion() {
    Compiler compiler = compileSimple("var a=1;");
    Node node = compiler.getNodeForCodeInsertion(null);
    assertNotNull(node);
  }

  // -------------------------------------------------------------------------
  // Error and warning counters
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testHasErrorsWhenNoErrors() {
    Compiler compiler = compileSimple("var a=1;");
    assertFalse(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testHasErrorsWhenSyntaxError() {
    Compiler compiler = new Compiler();
    compiler.compile(new JSSourceFile[0],
        new JSSourceFile[]{JSSourceFile.fromCode("bad", "var =")},
        createOptions());
    assertTrue(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testGetErrorCount() {
    Compiler compiler = new Compiler();
    compiler.compile(new JSSourceFile[0],
        new JSSourceFile[]{JSSourceFile.fromCode("bad", "var =")},
        createOptions());
    assertTrue(compiler.getErrorCount() > 0);
  }

  // -------------------------------------------------------------------------
  // Misc internal package-private methods
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testGetInputsInOrder() {
    Compiler compiler = compileSimple("var a=1;", "var b=2;");
    List<CompilerInput> inputs = compiler.getInputsInOrder();
    assertEquals(2, inputs.size());
  }

  @Test(timeout = 4000)
  public void testGetInputsForTesting() {
    Compiler compiler = compileSimple("var a=1;");
    List<CompilerInput> inputs = compiler.getInputsForTesting();
    assertEquals(1, inputs.size());
  }

  @Test(timeout = 4000)
  public void testGetExternsForTesting() {
    Compiler compiler = compileSimple("var a=1;");
    List<CompilerInput> externs = compiler.getExternsForTesting();
    assertNotNull(externs);
    assertEquals(0, externs.size());
  }

  @Test(timeout = 4000)
  public void testGetTypeRegistry() {
    Compiler compiler = compileSimple("var a=1;");
    assertNotNull(compiler.getTypeRegistry());
  }

  @Test(timeout = 4000)
  public void testGetTopScope() {
    Compiler compiler = compileSimple("var a=1;");
    // Top scope should be available after compilation
    assertNotNull(compiler.getTopScope());
  }
}