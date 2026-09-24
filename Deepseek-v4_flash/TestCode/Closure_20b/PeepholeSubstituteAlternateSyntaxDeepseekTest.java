package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Regression test for the incorrect folding of {@code String(x)} into
 * {@code "" + x}. The general transformation is unsafe because {@code String}
 * and string concatenation can produce different results for objects with
 * custom {@code valueOf}/{@code toString} methods.
 */
public class PeepholeSubstituteAlternateSyntaxDeepseekTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new PeepholeOptimizationsPass(
        compiler, new PeepholeSubstituteAlternateSyntax(true));
  }

  @Test
  public void testSimpleFunctionCall() {
    // String(x) must not be rewritten to "" + x when x is not a literal.
    test("String(x)", "String(x)");
  }
}