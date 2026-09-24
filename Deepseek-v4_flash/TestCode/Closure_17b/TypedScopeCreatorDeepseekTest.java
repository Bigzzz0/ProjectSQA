package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Deep tests for {@link TypedScopeCreator}, including a regression test
 * for issue #688 where {@code @lends} caused the JSDoc on object literal
 * properties to be ignored, leading to a false "inconsistent return type"
 * warning.
 */
@RunWith(JUnit4.class)
public class TypedScopeCreatorDeepTest {

  @Test
  public void testIssue688() throws Exception {
    // The @return {*} annotation on the lent property must be respected.
    // If TypedScopeCreator ignores the property JSDoc, the function body
    // (which returns both number and undefined) triggers an
    // "inconsistent return type" warning.
    String code =
        "/** @constructor */ function Foo() {}\n" +
        "/** @lends {Foo.prototype} */\n" +
        "var x = {\n" +
        "  /** @return {*} */\n" +
        "  f: function(a) {\n" +
        "    if (a) { return 1; } else { return; }\n" +
        "  }\n" +
        "};";
    assertNoWarnings(code);
  }

  @Test
  public void testLendsWithReturnType() throws Exception {
    String code =
        "/** @constructor */ function Foo() {}\n" +
        "/** @lends {Foo.prototype} */\n" +
        "var x = {\n" +
        "  /** @return {number} */\n" +
        "  f: function() { return 1; }\n" +
        "};";
    assertNoWarnings(code);
  }

  @Test
  public void testEnum() throws Exception {
    assertNoWarnings(
        "/** @enum {number} */ var E = {A: 1, B: 2};");
  }

  @Test
  public void testTypedef() throws Exception {
    assertNoWarnings(
        "/** @typedef {number|string} */ var T;");
  }

  @Test
  public void testConstructor() throws Exception {
    assertNoWarnings(
        "/** @constructor */ function Foo() {}\n" +
        "var f = new Foo();");
  }

  @Test
  public void testInterface() throws Exception {
    assertNoWarnings(
        "/** @interface */ function Foo() {}");
  }

  @Test
  public void testCatchParam() throws Exception {
    assertNoWarnings(
        "try { throw 1; } catch (e) { }");
  }

  private void assertNoWarnings(String code) throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);

    compiler.compile(
        SourceFile.fromCode("externs", ""),
        SourceFile.fromCode("test", code),
        options);

    JSError[] errors = compiler.getErrors();
    assertEquals(
        "Unexpected errors: " + Arrays.toString(errors),
        0, errors.length);

    JSError[] warnings = compiler.getWarnings();
    assertEquals(
        "Unexpected warnings: " + Arrays.toString(warnings),
        0, warnings.length);
  }
}