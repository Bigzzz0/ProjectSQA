package com.google.javascript.jscomp;

import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TypedScopeCreator} with a focus on the {@code @lends}
 * annotation and its interaction with type checking.
 */
public class TypedScopeCreatorTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setCheckTypes(true);
  }

  private Result compile(String code) {
    return compiler.compile(
        SourceFile.fromCode("test.js", ""),
        SourceFile.fromCode("test.js", code),
        options);
  }

  /**
   * The core regression test: when {@code @lends} is used, a function property
   * with an inconsistent return type must still be type-checked and produce a
   * warning at the correct line.
   */
  @Test
  public void testLendsInconsistentReturnType() {
    String code = "/** @constructor */ function Foo() {}\n"
        + "/** @lends {Foo.prototype} */\n"
        + "var x = {\n"
        + "  /** @return {string} */\n"
        + "  method: function() {\n"
        + "    return 1;\n"
        + "  }\n"
        + "};\n";

    Result result = compile(code);

    assertEquals("expected exactly one warning", 1, result.warnings.length);
    assertTrue("warning should mention inconsistent return type: "
            + result.warnings[0].getDescription(),
        result.warnings[0].getDescription().contains("inconsistent return type"));
    assertEquals("warning should be reported on the return statement",
        6, result.warnings[0].getLineno());
  }

  /** When the return type is consistent, no warning should be produced. */
  @Test
  public void testLendsConsistentReturnType() {
    String code = "/** @constructor */ function Foo() {}\n"
        + "/** @lends {Foo.prototype} */\n"
        + "var x = {\n"
        + "  /** @return {number} */\n"
        + "  method: function() {\n"
        + "    return 1;\n"
        + "  }\n"
        + "};\n";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }

  /**
   * Verifies that {@code @lends} actually declares the function property on the
   * lent prototype, so the property is visible to the type checker.
   */
  @Test
  public void testLendsDeclaresPropertyOnPrototype() {
    String code = "/** @constructor */ function Foo() {}\n"
        + "/** @lends {Foo.prototype} */\n"
        + "var x = { /** @return {number} */ method: function() { return 1; } };";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);

    FunctionType foo = (FunctionType) compiler.getTypeRegistry().getType("Foo");
    assertNotNull("Foo constructor not found", foo);

    ObjectType proto = foo.getPrototype();
    FunctionType method = (FunctionType) proto.getPropertyType("method");
    assertNotNull("method property was not declared on Foo.prototype", method);
    assertEquals("number", method.getReturnType().toString());
  }

  /** Sanity check: a normal (non-@lends) inconsistent return type is caught. */
  @Test
  public void testBasicInconsistentReturnType() {
    String code = "/** @return {string} */ function f() { return 1; }";

    Result result = compile(code);

    assertEquals("expected exactly one warning", 1, result.warnings.length);
    assertTrue(result.warnings[0].getDescription().contains("inconsistent return type"));
  }

  /** A simple constructor should compile without warnings or errors. */
  @Test
  public void testConstructorDeclaration() {
    String code = "/** @constructor */ function Foo() {}";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }

  /** An enum declaration should compile cleanly. */
  @Test
  public void testEnumDeclaration() {
    String code = "/** @enum {number} */ var E = {A: 1, B: 2};";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }

  /** A typedef declaration should compile cleanly. */
  @Test
  public void testTypedefDeclaration() {
    String code = "/** @typedef {number} */ var T;";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }

  /** A function with a local variable should compile cleanly. */
  @Test
  public void testFunctionWithLocalVar() {
    String code = "function f() { var x = 1; return x; }";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }

  /** A try/catch block should compile cleanly. */
  @Test
  public void testCatchBlock() {
    String code = "try { throw 1; } catch (e) {}";

    Result result = compile(code);

    assertEquals("expected no warnings", 0, result.warnings.length);
    assertEquals("expected no errors", 0, result.errors.length);
  }
}