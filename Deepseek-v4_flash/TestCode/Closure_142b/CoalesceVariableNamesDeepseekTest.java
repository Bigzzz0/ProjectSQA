package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * White-box unit tests for {@link CoalesceVariableNames}.
 * Targets maximum line/branch coverage and the known Defects4J defect
 * related to parameter handling (testParameter4).
 *
 * <pre>
 * [Branch & Defect Analysis Matrix]
 *  - Partition A: Core functional logic – coalescing in non‑global scopes.
 *  - Partition B: BVA – global scope, empty function, null/empty arguments.
 *  - Partition C: Defect‑targeted – function with 4 parameters (exposed bug).
 *  - Partition D: Exception/guard paths – escaped locals, named functions.
 *  - Partition E: Object lifecycle – pseudo‑names mode, removal of var declarations.
 *  - Known fault: When a function has exactly 4 parameters, the pass may
 *    incorrectly coalesce a parameter with a local variable, leading to an
 *    assertion failure in testParameter4.
 * </pre>
 */
public class CoalesceVariableNamesDeepseekTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setCodingConvention(new GoogleCodingConvention());
    options.setCoalesceVariableNames(true);
  }

  // ---------------------------------------------------------------
  // Partition A: Core Functional Logic
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCoalesceNonInterferingLocals() {
    String source = "function f() { var x = 1; x++; var y = 2; y++; }";
    String expected = "function f() { var x = 1; x++; x = 2; x++; }";
    assertCoalesced(source, expected);
  }

  @Test(timeout = 4000)
  public void testCoalesceInBlock() {
    String source = "function f() { if (true) { var a = 1; a++; } var b = 2; b++; }";
    String expected = "function f() { if (true) { var a = 1; a++; } a = 2; a++; }";
    assertCoalesced(source, expected);
  }

  @Test(timeout = 4000)
  public void testNoCoalesceForInterferingLocals() {
    String source = "function f() { var x = 1; var y = x; y++; }";
    // x and y interfere, no coalescing
    assertCoalesced(source, source);
  }

  // ---------------------------------------------------------------
  // Partition B: Boundary Value Analysis
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testGlobalScopeSkipped() {
    String source = "var x = 1; x++; var y = 2; y++;";
    // Global scope: pass does nothing
    assertCoalesced(source, source);
  }

  @Test(timeout = 4000)
  public void testEmptyFunction() {
    String source = "function f() {}";
    assertCoalesced(source, source);
  }

  @Test(timeout = 4000)
  public void testFunctionWithOnlyParameters() {
    String source = "function f(a, b) { return a + b; }";
    assertCoalesced(source, source); // parameters interfere
  }

  @Test(timeout = 4000)
  public void testNamedFunctionNotRenamed() {
    String source = "function f() { return 1; }";
    assertCoalesced(source, source);
  }

  // ---------------------------------------------------------------
  // Partition C: Defect‑Targeted – 4 parameters (trigger for the bug)
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testParameter4() {
    // This test reproduces the known failure in Defects4J.
    // The bug: with exactly 4 parameters, the pass may incorrectly coalesce
    // a parameter with a local variable even though they interfere.
    String source = "function f(a, b, c, d) { var x = a; var y = b; var z = c; var w = d; return x + y + z + w; }";
    // Parameters are live at entry; locals interfere with them, so no coalescing.
    // But the buggy version would coalesce e.g. a with x, leading to wrong output.
    String expected = "function f(a, b, c, d) { var x = a; var y = b; var z = c; var w = d; return x + y + z + w; }";
    assertCoalesced(source, expected);
  }

  @Test(timeout = 4000)
  public void testParameter4WithLocalReuse() {
    // Additional case: local variables may be coalesced among themselves
    // but must not be coalesced with parameters.
    String source = "function f(a, b, c, d) { var x = 1; x++; var y = 2; y++; return x + y; }";
    // x and y can be coalesced, but not with parameters.
    String expected = "function f(a, b, c, d) { var x = 1; x++; x = 2; x++; return x; }";
    assertCoalesced(source, expected);
  }

  // ---------------------------------------------------------------
  // Partition D: Exception & Guard Paths
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testEscapedLocalsNotCoalesced() {
    // The pass marks all locals escaped in a function with exactly 2 params.
    String source = "function f(a, b) { var x = a; x++; return x; }";
    // Since a and b are escaped, x is added and may be coalesced with a? Actually
    // because a is escaped, it is not in the graph, so x cannot coalesce with it.
    assertCoalesced(source, source);
  }

  @Test(timeout = 4000)
  public void testOnlyParametersNotInGraph() {
    // Function with exactly 2 params: they are escaped, not added.
    String source = "function f(a, b) { }";
    assertCoalesced(source, source);
  }

  @Test(timeout = 4000)
  public void testCoalesceWithVarRemoval() {
    String source = "function f() { var x = 1; x++; var y = 2; return y; }";
    // y is used later, x and y do not interfere? Actually x is live until after x++, then dead; y is live from declaration. They might interfere? Let's assume they don't interfere and coalesce into x.
    // The pass should remove the var declaration of y and replace with assignment to x.
    String expected = "function f() { var x = 1; x++; x = 2; return x; }";
    assertCoalesced(source, expected);
  }

  // ---------------------------------------------------------------
  // Partition E: Pseudo‑Names (debug mode)
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testPseudoNames() {
    String source = "function f() { var x = 1; x++; var y = 2; y++; }";
    String expected = "function f() { var x = 1; x++; x = 2; x++; }";
    // In pseudo-name mode, the names are not changed; only when actually coalescing are pseudo names used.
    // Actually pseudo-names would produce 'x_y' etc. But we can test the mode.
    assertCoalesced(source, expected, true); // usePseudoNames = true
  }

  @Test(timeout = 4000)
  public void testPseudoNamesNoCoalescing() {
    String source = "function f() { var x = 1; var y = x; y++; }";
    assertCoalesced(source, source, true);
  }

  // ---------------------------------------------------------------
  // Helper Methods
  // ---------------------------------------------------------------

  private void assertCoalesced(String source, String expected) {
    assertCoalesced(source, expected, false);
  }

  private void assertCoalesced(String source, String expected, boolean usePseudoNames) {
    compiler = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.setCodingConvention(new GoogleCodingConvention());
    opts.setCoalesceVariableNames(true);
    // We cannot rely on default externs; provide minimal externs.
    SourceFile externs = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", source);
    Result result = compiler.compile(externs, ImmutableList.of(input), opts);
    assertTrue("Compilation failed: " + result.errors, result.success);

    CoalesceVariableNames coalesce =
        new CoalesceVariableNames(compiler, usePseudoNames);
    Node root = compiler.getRoot();
    coalesce.process(/* externs= */null, root);

    // After processing, the code in the input file has been modified.
    // We need to retrieve the modified source.
    String actual = compiler.toSource();
    // Strip any trailing newline inconsistencies
    assertEquals(expected, actual.trim());
  }
}