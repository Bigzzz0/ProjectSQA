package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import org.junit.Test;

/**
 * Test targeting the bug where CodeGenerator incorrectly prints a computed
 * property string literal that looks like a number (e.g. "010") without quotes.
 */
public class CodeGeneratorDeepseekTest {

  @Test
  public void testNumericKeys() {
    Compiler compiler = new Compiler();
    Node script = compiler.parse(
        SourceFile.fromCode("test", "var x = {[\"010\"]: 1};"));
    assertNotNull("Parsing failed", script);

    Node varNode = script.getFirstChild();
    assertNotNull("Expected a var statement", varNode);

    String result = CodePrinter.printNode(varNode);
    assertEquals("var x={[\"010\"]:1}", result);
  }
}
