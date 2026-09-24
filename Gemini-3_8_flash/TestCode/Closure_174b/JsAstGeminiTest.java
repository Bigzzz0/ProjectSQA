package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * [Branch & Defect Analysis Matrix]
 * ==============================================================================================
 * Target Class: com.google.javascript.jscomp.JsAst
 * Target Logic:
 *  - JsAst(SourceFile): inputId initialization, sourceFile/fileName binding.
 *  - getAstRoot(AbstractCompiler): caching branch (root == null vs root != null), inputId stamping.
 *  - clearAst(): root disposal and sourceFile cached source clearing.
 *  - setSourceFile(SourceFile): validation guard fileName.equals(file.getName()), replacement.
 *  - parse(AbstractCompiler):
 *      - Normal parsing via ParserRunner, setting old parse tree, prepareAst, staticSourceFile.
 *      - Exception handling: IOException caught, READ_ERROR reported to compiler.
 *      - Fallback branches: (root == null || compiler.hasHaltingErrors()) -> IR.script() dummy block.
 *  - Defect Targeted (Closure 110 / Issue 1103):
 *      - testIssue1103a: ScopedAliases local function variable inside goog.scope falsely detected
 *        as non-alias local (JSC_GOOG_SCOPE_NON_ALIAS_LOCAL).
 *      - testIssue1103b: ScopedAliases recursive / named function expression in goog.scope causing
 *        Internal Compiler Error.
 *      - testIssue1103c: ScopedAliases mixed aliases and non-alias function expressions.
 * ==============================================================================================
 */
public class JsAstGeminiTest {

  // ==========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================================================

  @Test(timeout = 4000)
  public void testNormalAstGenerationAndProperties() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    String code = "var answer = 42;";
    SourceFile sf = SourceFile.fromCode("main.js", code);
    JsAst ast = new JsAst(sf);

    assertEquals("main.js", ast.getInputId().getIdName());
    assertSame(sf, ast.getSourceFile());

    Node root = ast.getAstRoot(compiler);
    assertNotNull("AST root must not be null after parse", root);
    assertTrue("AST root must be a SCRIPT node", root.isScript());
    assertEquals(ast.getInputId(), root.getInputId());
    assertSame(sf, root.getStaticSourceFile());
    assertEquals(1, root.getChildCount());
  }

  @Test(timeout = 4000)
  public void testGetAstRootCachingDoesNotReparse() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    SourceFile sf = SourceFile.fromCode("cached.js", "var a = 1;");
    JsAst ast = new JsAst(sf);

    Node firstCallRoot = ast.getAstRoot(compiler);
    Node secondCallRoot = ast.getAstRoot(compiler);

    assertSame("getAstRoot must return cached root instance on subsequent invocations",
        firstCallRoot, secondCallRoot);
  }

  @Test(timeout = 4000)
  public void testClearAstForcesReparse() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    SourceFile sf = SourceFile.fromCode("clear.js", "var x = 10;");
    JsAst ast = new JsAst(sf);

    Node initialRoot = ast.getAstRoot(compiler);
    assertNotNull(initialRoot);

    ast.clearAst();

    Node regeneratedRoot = ast.getAstRoot(compiler);
    assertNotNull(regeneratedRoot);
    assertNotSame("clearAst must invalidate cached root and regenerate AST",
        initialRoot, regeneratedRoot);
    assertEquals(ast.getInputId(), regeneratedRoot.getInputId());
  }

  @Test(timeout = 4000)
  public void testSetSourceFileValid() {
    SourceFile initialFile = SourceFile.fromCode("config.js", "var v = 1;");
    JsAst ast = new JsAst(initialFile);

    SourceFile updatedFile = SourceFile.fromCode("config.js", "var v = 2;");
    ast.setSourceFile(updatedFile);

    assertSame("SourceFile should be updated when file names match",
        updatedFile, ast.getSourceFile());
  }

  @Test(timeout = 4000)
  public void testClearAstAndReplaceSourceFileReparsesNewContent() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    SourceFile sf1 = SourceFile.fromCode("module.js", "var initialVar = 1;");
    JsAst ast = new JsAst(sf1);
    Node root1 = ast.getAstRoot(compiler);
    assertEquals("initialVar", root1.getFirstChild().getFirstChild().getString());

    ast.clearAst();
    SourceFile sf2 = SourceFile.fromCode("module.js", "var updatedVar = 2;");
    ast.setSourceFile(sf2);

    Node root2 = ast.getAstRoot(compiler);
    assertNotSame(root1, root2);
    assertEquals("updatedVar", root2.getFirstChild().getFirstChild().getString());
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==========================================================================

  @Test(timeout = 4000)
  public void testEmptySourceFileYieldsEmptyScript() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    SourceFile sf = SourceFile.fromCode("empty.js", "");
    JsAst ast = new JsAst(sf);

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals("Empty script node should contain 0 children", 0, root.getChildCount());
    assertEquals(ast.getInputId(), root.getInputId());
    assertSame(sf, root.getStaticSourceFile());
  }

  @Test(timeout = 4000)
  public void testWhitespaceAndCommentsOnly() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    String code = "   \n\t // single line comment\n /* multi line\n comment */ \n";
    SourceFile sf = SourceFile.fromCode("comments.js", code);
    JsAst ast = new JsAst(sf);

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(0, root.getChildCount());
  }

  @Test(timeout = 4000)
  public void testUnicodeAndJSDocParsing() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    String code = "/** @type {string} */ var π = '3.14159';";
    SourceFile sf = SourceFile.fromCode("unicode.js", code);
    JsAst ast = new JsAst(sf);

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(1, root.getChildCount());
    assertNotNull("JSDoc info should be attached", root.getFirstChild().getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testHaltingErrorsProducesDummyScript() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    // Seed a fatal halting error on the compiler prior to parsing
    compiler.report(JSError.make("pre_existing.js", 1, 0, CheckLevel.ERROR,
        DiagnosticType.error("TEST_HALT", "Simulated halting error")));
    assertTrue("Compiler should report halting errors", compiler.hasHaltingErrors());

    SourceFile sf = SourceFile.fromCode("skipped.js", "var validCode = 123;");
    JsAst ast = new JsAst(sf);

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals("When halting errors exist, a dummy script with 0 children must be generated",
        0, root.getChildCount());
    assertEquals(ast.getInputId(), root.getInputId());
    assertSame(sf, root.getStaticSourceFile());
  }

  @Test(timeout = 4000)
  public void testParseWithSyntaxError() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    SourceFile sf = SourceFile.fromCode("syntax_error.js", "var = ;");
    JsAst ast = new JsAst(sf);

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertTrue("Compiler should record parse error", compiler.getErrorCount() > 0);
  }

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure 110 / Issue 1103)
  // ==========================================================================

  @Test(timeout = 4000)
  public void testIssue1103a_scopedAliasesLocalFunctionVariable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String code = "goog.scope(function () {\n"
        + "  var a = function () {};\n"
        + "});";
    SourceFile sourceFile = SourceFile.fromCode("testcode", code);
    JsAst ast = new JsAst(sourceFile);
    Node scriptRoot = ast.getAstRoot(compiler);
    assertNotNull(scriptRoot);

    Node externsRoot = IR.root();
    Node mainRoot = IR.root(scriptRoot);

    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(externsRoot, mainRoot);

    assertEquals("ScopedAliases should not report error for function variable in goog.scope (Issue 1103a)",
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue1103b_scopedAliasesSelfReferencingFunctionExpression() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String code = "goog.scope(function () {\n"
        + "  var a = function a() { a(); };\n"
        + "});";
    SourceFile sourceFile = SourceFile.fromCode("testcode", code);
    JsAst ast = new JsAst(sourceFile);
    Node scriptRoot = ast.getAstRoot(compiler);
    assertNotNull(scriptRoot);

    Node externsRoot = IR.root();
    Node mainRoot = IR.root(scriptRoot);

    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(externsRoot, mainRoot);

    assertEquals("ScopedAliases should not crash on self-referencing named function expression (Issue 1103b)",
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue1103c_scopedAliasesMultipleLocalsWithFunctionExpression() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String code = "goog.scope(function () {\n"
        + "  var Foo = goog.Foo;\n"
        + "  var a = function () {};\n"
        + "});";
    SourceFile sourceFile = SourceFile.fromCode("testcode", code);
    JsAst ast = new JsAst(sourceFile);
    Node scriptRoot = ast.getAstRoot(compiler);
    assertNotNull(scriptRoot);

    Node externsRoot = IR.root();
    Node mainRoot = IR.root(scriptRoot);

    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(externsRoot, mainRoot);

    assertEquals("ScopedAliases should allow function local declaration following an alias (Issue 1103c)",
        0, compiler.getErrorCount());
  }

  // ==========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ==========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorWithNullSourceFileThrowsNpe() {
    new JsAst(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetSourceFileNullThrowsNpe() {
    SourceFile sf = SourceFile.fromCode("valid.js", "var a = 1;");
    JsAst ast = new JsAst(sf);
    ast.setSourceFile(null);
  }

  @Test(timeout = 4000)
  public void testSetSourceFileMismatchThrowsIllegalStateException() {
    SourceFile sf1 = SourceFile.fromCode("name_a.js", "var a = 1;");
    JsAst ast = new JsAst(sf1);
    SourceFile sf2 = SourceFile.fromCode("name_b.js", "var a = 1;");

    try {
      ast.setSourceFile(sf2);
      fail("Expected IllegalStateException when setting SourceFile with mismatched name");
    } catch (IllegalStateException expected) {
      // Expected Preconditions failure
    }
  }

  @Test(timeout = 4000)
  public void testParseHandlesIOExceptionGracefully() {
    SourceFile errorSource = new SourceFile("disk_error.js") {
      @Override
      public String getCode() throws IOException {
        throw new IOException("Simulated I/O disk failure");
      }
    };

    JsAst ast = new JsAst(errorSource);
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    Node root = ast.getAstRoot(compiler);
    assertNotNull(root);
    assertTrue(root.isScript());
    assertEquals(0, root.getChildCount());
    assertEquals(ast.getInputId(), root.getInputId());
    assertSame(errorSource, root.getStaticSourceFile());

    assertEquals(1, compiler.getErrorCount());
    JSError error = compiler.getErrors()[0];
    assertEquals(AbstractCompiler.READ_ERROR.key, error.getType().key);
  }

  // ==========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==========================================================================

  @Test(timeout = 4000)
  public void testClearAstWithoutPriorParse() {
    SourceFile sf = SourceFile.fromCode("unparsed.js", "var untouched;");
    JsAst ast = new JsAst(sf);
    ast.clearAst();
    assertSame(sf, ast.getSourceFile());
  }

  @Test(timeout = 4000)
  public void testJavaSerializationContract() throws Exception {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    SourceFile sf = SourceFile.fromCode("serializable.js", "var ser = 123;");
    JsAst ast = new JsAst(sf);
    Node originalRoot = ast.getAstRoot(compiler);
    assertNotNull(originalRoot);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(ast);
    }

    JsAst deserialized;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
      deserialized = (JsAst) ois.readObject();
    }

    assertNotNull("Deserialized JsAst must not be null", deserialized);
    // Transient fields should be null after deserialization
    assertNull("InputId is transient and expected to be null after deserialization",
        deserialized.getInputId());
    assertNull("SourceFile is transient and expected to be null after deserialization",
        deserialized.getSourceFile());

    // Serialized AST root remains accessible
    Node deserializedRoot = deserialized.getAstRoot(compiler);
    assertNotNull("AST root is non-transient and must be restored", deserializedRoot);
    assertTrue(deserializedRoot.isScript());
  }
}