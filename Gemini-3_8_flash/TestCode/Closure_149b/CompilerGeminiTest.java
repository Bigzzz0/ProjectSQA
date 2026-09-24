/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.Compiler
 *
 * Key Branches & Decision Coverage Paths:
 * 1. Constructors & ErrorManager setup:
 *    - Compiler(), Compiler(PrintStream), Compiler(ErrorManager)
 *    - initOptions with null vs non-null errorManager and outStream
 *    - setErrorManager null validation (Preconditions.checkNotNull)
 * 2. Module & Input Initialization:
 *    - init/initModules with empty module list (EMPTY_MODULE_LIST_ERROR)
 *    - init with singleton vs multi-module, empty root module (EMPTY_ROOT_MODULE_ERROR)
 *    - fillEmptyModules behavior on empty modules
 *    - Duplicate input detection in inputsByName map (DUPLICATE_INPUT, DUPLICATE_EXTERN_INPUT)
 *    - Module graph dependency sorting & CircularDependencyException handling
 * 3. Compilation Pipelines & Execution Options:
 *    - Single file vs module compile, compile() called once constraint (Preconditions.checkState)
 *    - disableThreads() path (runInCompilerThread on current thread vs separate Thread)
 *    - Exception propagation in runInCompilerThread
 *    - options.skipAllPasses, devMode (START_AND_END, EVERY_PASS, OFF)
 *    - options.nameAnonymousFunctionsOnly, options.removeTryCatchFinally, options.stripTypes
 *    - options.isExternExportsEnabled() / externExportsPath
 *    - CustomPassExecutionTime.BEFORE_CHECKS and BEFORE_OPTIMIZATIONS
 * 4. Parse & AST Operations:
 *    - parseInputs with existing roots (detachChildren)
 *    - JSDocInfo handling: @externs moving input to externsRoot; @nocompile dropping input
 *    - parseTestCode, parseSyntheticCode, parse(JSSourceFile)
 *    - computeCFG, getAstDotGraph, prepareAst
 * 5. Code Printing & Source Generation:
 *    - toSource(), toSourceArray(), toSource(JSModule), toSourceArray(JSModule)
 *    - CodeBuilder line/column tracking, reset(), endsWith()
 *    - options.printInputDelimiter with %name% and %num% placeholders
 *    - options.sourceMapOutputPath setting sourceMap start positions
 *    - JSDoc license comment output
 *    - Charset output handling in toSource (targeted defect test)
 * 6. Internal State & Helpers:
 *    - getState(), setState(IntermediateState) lifecycle
 *    - areNodesEqualForInlining with ambiguateProperties/disambiguateProperties
 *    - getUniqueNameIdSupplier and resetUniqueNameId
 *    - newExternInput (success vs duplicate name exception)
 *    - addIncrementalSourceAst (duplicate check)
 *    - getNodeForCodeInsertion (module null/non-null, empty/non-empty)
 *    - getSourceLine & getSourceRegion with line < 1 and nonexistent inputs
 *    - setPassConfig validation (null check, already assigned check)
 *    - startPass/endPass state validation
 *
 * Known Defect Targeted:
 * - testCharSetExpansion: Verifies that options.outputCharset is properly handled during
 *   toSource() code generation without being lost or resulting in null character encodings.
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & Compilation Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleCompileAndToSource() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1 + 2;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());

    String source = compiler.toSource();
    assertTrue(source.contains("var a=1+2") || source.contains("var a = 1 + 2"));
  }

  @Test(timeout = 4000)
  public void testCompileWithMultipleInputsAndToSourceArray() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input1.js", "var x = 10;");
    JSSourceFile input2 = JSSourceFile.fromCode("input2.js", "var y = 20;");

    Result result = compiler.compile(extern, new JSSourceFile[] { input1, input2 }, options);
    assertTrue(result.success);

    String[] sources = compiler.toSourceArray();
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("x"));
    assertTrue(sources[1].contains("y"));
  }

  @Test(timeout = 4000)
  public void testCompileModulesAndToSourceModule() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSModule mod1 = new JSModule("m1");
    mod1.add(JSSourceFile.fromCode("m1_file.js", "var mod1_val = 1;"));

    JSModule mod2 = new JSModule("m2");
    mod2.add(JSSourceFile.fromCode("m2_file.js", "var mod2_val = 2;"));
    mod2.addDependency(mod1);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    Result result = compiler.compile(extern, new JSModule[] { mod1, mod2 }, options);
    assertTrue(result.success);

    String m1Src = compiler.toSource(mod1);
    assertTrue(m1Src.contains("mod1_val"));

    String[] m2SrcArray = compiler.toSourceArray(mod2);
    assertEquals(1, m2SrcArray.length);
    assertTrue(m2SrcArray[0].contains("mod2_val"));

    assertNotNull(compiler.getModuleGraph());
    assertEquals(2, compiler.getInputsInOrder().size());
  }

  @Test(timeout = 4000)
  public void testDisableThreadsCompile() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var t = true;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCompileWithDevModeEveryPass() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.devMode = DevMode.EVERY_PASS;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var z = 42;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertNotNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testCompileWithDevModeStartAndEnd() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.devMode = DevMode.START_AND_END;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "function foo() { return 1; } foo();");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
  }

  @Test(timeout = 4000)
  public void testCompileWithNameAnonymousFunctionsOnly() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.nameAnonymousFunctionsOnly = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var f = function() {};");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
  }

  @Test(timeout = 4000)
  public void testCompileWithStripCodeAndRemoveTryCatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.removeTryCatchFinally = true;
    options.stripNameSuffixes = Sets.newHashSet("logger");

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "try { var a_logger = 1; } catch (e) {}");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & CodeBuilder / Formatting Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testCodeBuilderOperations() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertEquals("", cb.toString());

    cb.append("hello");
    assertEquals(5, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(5, cb.getColumnIndex());
    assertTrue(cb.endsWith("lo"));
    assertFalse(cb.endsWith("hello world"));

    cb.append("\nworld\n");
    assertEquals(12, cb.getLength());
    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("end");
    assertEquals(2, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex()); // Line count retained on reset
    assertEquals("", cb.toString());
  }

  @Test(timeout = 4000)
  public void testToSourceWithDelimitersAndLicense() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name% #%num%]";
    compiler.initOptions(options);

    String code = "/** @license Proprietary 2024 */ var licensed = 1;";
    Node script = compiler.parseTestCode(code);
    script.putProp(Node.SOURCENAME_PROP, "testfile.js");

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    compiler.toSource(cb, 5, script);

    String output = cb.toString();
    assertTrue(output.contains("// [testfile.js #5]"));
    assertTrue(output.contains("/*\nProprietary 2024*/\n"));
    assertTrue(output.contains("var licensed=1") || output.contains("var licensed = 1"));
  }

  @Test(timeout = 4000)
  public void testToSourceEmptyModule() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    JSModule emptyModule = new JSModule("empty");
    assertEquals("", compiler.toSource(emptyModule));
    assertEquals(0, compiler.toSourceArray(emptyModule).length);
  }

  @Test(timeout = 4000)
  public void testSourceLineAndRegionBoundaries() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    assertNull(compiler.getSourceLine("nonexistent.js", -1));
    assertNull(compiler.getSourceLine("nonexistent.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", 5));
    assertNull(compiler.getSourceRegion("nonexistent.js", -1));
    assertNull(compiler.getSourceRegion("nonexistent.js", 1));

    compiler.parseSyntheticCode("virtual.js", "line1\nline2\nline3\nline4");
    assertEquals("line2", compiler.getSourceLine("virtual.js", 2));
    assertNotNull(compiler.getSourceRegion("virtual.js", 2));
  }

  @Test(timeout = 4000)
  public void testEmptyModuleListDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<JSModule> emptyList = Lists.newArrayList();

    compiler.initModules(Lists.<JSSourceFile>newArrayList(), emptyList, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEmptyRootModuleDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule root = new JSModule("root");
    JSModule child = new JSModule("child");
    child.add(JSSourceFile.fromCode("child.js", "var c = 1;"));
    child.addDependency(root);

    compiler.initModules(Lists.<JSSourceFile>newArrayList(), Lists.newArrayList(root, child), options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputNamesDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile in1 = JSSourceFile.fromCode("duplicate.js", "var a = 1;");
    JSSourceFile in2 = JSSourceFile.fromCode("duplicate.js", "var b = 2;");

    compiler.init(new JSSourceFile[0], new JSSourceFile[] { in1, in2 }, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternNamesDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile ext1 = JSSourceFile.fromCode("ext.js", "var x;");
    JSSourceFile ext2 = JSSourceFile.fromCode("ext.js", "var y;");

    compiler.init(new JSSourceFile[] { ext1, ext2 }, new JSSourceFile[0], options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Charset Expansion Defect)
  // =========================================================================

  @Test(timeout = 4000)
  public void testCharSetExpansion() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    Charset ascii = Charset.forName("US-ASCII");
    options.outputCharset = ascii;

    compiler.initOptions(options);

    assertNotNull("outputCharset must not be null when set on options", compiler.getOptions().outputCharset);
    assertEquals("Expected US-ASCII charset", ascii, compiler.getOptions().outputCharset);

    // Verify code generation with non-ASCII characters respects outputCharset
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var greeting = 'hell\u00F3';");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);

    String output = compiler.toSource();
    assertNotNull(output);
    // Under US-ASCII, non-ASCII characters should be escaped to preserve charset integrity
    assertFalse("Output should not contain unescaped non-ASCII character", output.contains("\u00F3"));
  }

  // =========================================================================
  // Partition D: Exception, Guard Paths & Special Pass Options
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompileCalledTwiceThrowsStateException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.compile(extern, input, options);

    try {
      compiler.compile(extern, input, options);
      fail("Second compile() call should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected behavior: compilation already performed
    }
  }

  @Test(timeout = 4000)
  public void testSetErrorManagerNullThrows() {
    Compiler compiler = new Compiler();
    try {
      compiler.setErrorManager(null);
      fail("setErrorManager(null) should throw NullPointerException");
    } catch (NullPointerException expected) {
      // Preconditions.checkNotNull triggered
    }
  }

  @Test(timeout = 4000)
  public void testSetPassConfigNullOrTwiceThrows() {
    Compiler compiler = new Compiler();
    try {
      compiler.setPassConfig(null);
      fail("setPassConfig(null) should throw NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }

    PassConfig customConfig = new DefaultPassConfig(new CompilerOptions());
    compiler.setPassConfig(customConfig);
    assertEquals(customConfig, compiler.getPassConfig());

    try {
      compiler.setPassConfig(customConfig);
      fail("Assigning PassConfig twice should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testStartAndEndPassValidation() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    compiler.startPass("testPass");
    try {
      compiler.startPass("nestedPass");
      fail("Starting a pass before ending the previous should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected
    }
    compiler.endPass();

    try {
      compiler.endPass();
      fail("Ending a pass with no active pass should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testNewExternInputConflictThrows() {
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], new JSSourceFile[] { JSSourceFile.fromCode("file.js", "") }, new CompilerOptions());

    assertNotNull(compiler.newExternInput("dynamicExtern.js"));
    try {
      compiler.newExternInput("dynamicExtern.js");
      fail("Conflicting externs name must throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Conflicting externs name"));
    }
  }

  @Test(timeout = 4000)
  public void testAddIncrementalSourceAstDuplicateThrows() {
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], new JSSourceFile[] { JSSourceFile.fromCode("main.js", "var a = 1;") }, new CompilerOptions());

    JsAst ast = new JsAst(JSSourceFile.fromCode("main.js", "var a = 2;"));
    try {
      compiler.addIncrementalSourceAst(ast);
      fail("Duplicate incremental AST should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Duplicate input of name main.js"));
    }
  }

  @Test(timeout = 4000)
  public void testCustomPassExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    final boolean[] passRan = new boolean[] { false };

    CompilerPass dummyPass = new CompilerPass() {
      public void process(Node externs, Node root) {
        passRan[0] = true;
      }
    };
    options.addCustomPass(CustomPassExecutionTime.BEFORE_CHECKS, dummyPass);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var dummy = 1;");

    compiler.compile(extern, input, options);
    assertTrue("Custom pass should have been executed", passRan[0]);
  }

  @Test(timeout = 4000)
  public void testExternExportsEnabled() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.externExportsPath = "exports.js";

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "goog.exportSymbol('myExport', function() {});");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertNotNull(result.externExports);
  }

  @Test(timeout = 4000)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    try {
      compiler.throwInternalError("Failure message", new IllegalArgumentException("Root cause"));
      fail("throwInternalError should have thrown RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      assertTrue(e.getMessage().contains("Failure message"));
      assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

  // =========================================================================
  // Partition E: State Management, Inspections & AST Utilities
  // =========================================================================

  @Test(timeout = 4000)
  public void testStateSaveAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var extVar;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var localVal = 5;");

    compiler.compile(extern, input, options);
    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    compiler.setNormalized();
    assertTrue(compiler.isNormalized());

    compiler.setState(state);
    assertFalse(compiler.isNormalized());
    assertNotNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInlining() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = compiler.parseSyntheticCode("expr1", "a + b");
    Node n2 = compiler.parseSyntheticCode("expr2", "a + b");
    Node n3 = compiler.parseSyntheticCode("expr3", "a + c");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplier() {
    Compiler compiler = new Compiler();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testGetNodeForCodeInsertion() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule rootMod = new JSModule("root");
    rootMod.add(JSSourceFile.fromCode("root.js", "var rootVar = 1;"));

    compiler.init(new JSSourceFile[0], new JSModule[] { rootMod }, options);
    Node rootNode = compiler.getNodeForCodeInsertion(rootMod);
    assertNotNull(rootNode);

    Node singletonNode = compiler.getNodeForCodeInsertion(null);
    assertEquals(rootNode, singletonNode);

    JSModule emptyMod = new JSModule("emptyMod");
    try {
      compiler.getNodeForCodeInsertion(emptyMod);
      fail("Empty module should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Root module has no inputs"));
    }
  }

  @Test(timeout = 4000)
  public void testComputeCFGAndAstDotGraph() throws IOException {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "if (x) { y(); } else { z(); }");

    compiler.compile(extern, input, options);

    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    assertNotNull(cfg);

    String dotGraph = compiler.getAstDotGraph();
    assertNotNull(dotGraph);
    assertTrue(dotGraph.contains("digraph"));
  }

  @Test(timeout = 4000)
  public void testPrintStreamConstructorAndErrorManager() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);

    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    compiler.report(JSError.make("test.js", 1, 1, CheckLevel.ERROR, DiagnosticType.error("ERR", "Sample error")));
    compiler.getErrorManager().generateReport();

    String reportOutput = new String(baos.toByteArray());
    assertTrue(reportOutput.contains("Sample error"));
    assertEquals(1, compiler.getErrorCount());
    assertTrue(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testTypeRegistryAndValidatorAndInterpreter() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    JSTypeRegistry registry = compiler.getTypeRegistry();
    assertNotNull(registry);
    assertSame(registry, compiler.getTypeRegistry());

    assertNotNull(compiler.getTypeValidator());
    assertNotNull(compiler.getReverseAbstractInterpreter());

    compiler.getOptions().closurePass = true;
    // Clears cached interpreter check
    assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  @Test(timeout = 4000)
  public void testLoggingAndDebugLog() {
    Compiler.setLoggingLevel(Level.FINEST);
    Compiler compiler = new Compiler();
    compiler.addToDebugLog("Debug message 1");
    compiler.addToDebugLog("Debug message 2");

    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Result res = compiler.getResult();
    assertNotNull(res.debugLog);
    assertTrue(res.debugLog.contains("Debug message 1"));
    assertTrue(res.debugLog.contains("Debug message 2"));
  }

  @Test(timeout = 4000)
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node ast1 = compiler.parseSyntheticCode("synth.js", "var s = 100;");
    assertNotNull(ast1);
    assertEquals(Token.SCRIPT, ast1.getType());

    Node ast2 = compiler.parseTestCode("var t = 200;");
    assertNotNull(ast2);
    assertEquals(Token.SCRIPT, ast2.getType());
  }

  @Test(timeout = 4000)
  public void testJSDocExternsAndNoCompilePruning() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile normalInput = JSSourceFile.fromCode("normal.js", "var regular = 1;");
    JSSourceFile externJsDocInput = JSSourceFile.fromCode("extJsDoc.js", "/** @externs */ var extJsDoc = 2;");
    JSSourceFile noCompileInput = JSSourceFile.fromCode("noComp.js", "/** @nocompile */ var ignored = 3;");

    Result result = compiler.compile(
        new JSSourceFile[] { extern },
        new JSSourceFile[] { normalInput, externJsDocInput, noCompileInput },
        options);

    assertTrue(result.success);
    assertNotNull(compiler.getInput("extJsDoc.js"));
    assertTrue(compiler.getInput("extJsDoc.js").isExtern());
    assertNull(compiler.getInput("noComp.js"));
  }

  @Test(timeout = 4000)
  public void testNormalizeAndPrepareAst() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    Node root = compiler.parseTestCode("var a = 1; function b() { var c = 2; }");
    compiler.prepareAst(root);
    assertNotNull(root);

    compiler.jsRoot = root;
    compiler.externsRoot = new Node(Token.BLOCK);
    compiler.normalize();
    assertTrue(compiler.isNormalized());
    compiler.setUnnormalized();
    assertFalse(compiler.isNormalized());
  }

  @Test(timeout = 4000)
  public void testTracerModeCoverage() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.tracer = TracerMode.ALL;
    options.skipAllPasses = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var traced = true;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertNotNull(compiler.tracker);
  }

  @Test(timeout = 4000)
  public void testHasRegExpGlobalReferencesState() {
    Compiler compiler = new Compiler();
    assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(true);
    assertTrue(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testCodeChangeHandlerRegistration() {
    Compiler compiler = new Compiler();
    final int[] changeCounter = new int[] { 0 };
    CodeChangeHandler handler = new CodeChangeHandler() {
      public void reportChange() {
        changeCounter[0]++;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals(1, changeCounter[0]);

    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals(1, changeCounter[0]);
  }
}