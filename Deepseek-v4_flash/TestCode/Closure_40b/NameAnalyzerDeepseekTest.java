package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Regression test for Issue 284: NameAnalyzer must not remove a variable
 * that is used as the object of a for-in loop.
 */
@RunWith(JUnit4.class)
public class NameAnalyzerDeepseekTest {

  @Test
  public void testIssue284ForInLoopVariableNotRemoved() {
    String code = "var x = {}; for (var k in x) {}";
    String result = process(code);

    // The variable x is used in the for-in loop and must be preserved.
    assertTrue("Expected 'var x = {};' to be preserved, but got: " + result,
        result.contains("var x = {};"));
  }

  private String process(String code) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(false);
    options.setCheckSymbols(false);
    options.setOptimizationLevel(CompilerOptions.OptimizationLevel.NO_OPTIMIZATION);
    compiler.initOptions(options);

    List<SourceFile> externs =
        Collections.singletonList(SourceFile.fromCode("externs", ""));
    List<SourceFile> inputs =
        Collections.singletonList(SourceFile.fromCode("test", code));

    Result result = compiler.compile(externs, inputs, options);
    assertEquals("Compilation failed", 0, result.errors.length);

    Node root = compiler.getRoot();
    Node externsRoot = compiler.getExternsRoot();
    new NameAnalyzer(compiler, true).process(externsRoot, root);

    return compiler.toSource();
  }
}