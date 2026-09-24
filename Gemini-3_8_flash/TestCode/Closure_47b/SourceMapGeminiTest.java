package com.google.javascript.jscomp;

import com.google.debugging.sourcemap.FilePosition;
import com.google.debugging.sourcemap.SourceMapConsumerV3;
import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.SourceMap
 *
 * Decision / Condition Coverage Targets:
 * 1. SourceMap.Format enums: V1, DEFAULT, V2, V3 getInstance() instantiation and delegation.
 * 2. SourceMap.DetailLevel enums:
 *    - ALL.apply(Node): always returns true.
 *    - SYMBOLS.apply(Node):
 *      * node.isCall() -> true
 *      * node.isNew() -> true
 *      * node.isFunction() -> true
 *      * node.isName() -> true
 *      * NodeUtil.isGet(node) [GETPROP, GETELEM] -> true
 *      * NodeUtil.isObjectLitKey(node, node.getParent()) -> true
 *      * (node.isString() && NodeUtil.isGet(node.getParent())) -> true
 *      * other nodes (e.g., NUMBER, ASSIGN, VAR) -> false
 * 3. SourceMap.addMapping(Node, FilePosition, FilePosition):
 *    - sourceFile == null -> branch aborts early
 *    - node.getLineno() < 0 -> branch aborts early
 *    - sourceFile != null && lineno >= 0 -> proceeds to mapping
 *    - originalName null vs non-null (Node.ORIGINALNAME_PROP)
 * 4. SourceMap.fixupSourceLocation(String):
 *    - prefixMappings.isEmpty() -> returns original sourceFile
 *    - sourceLocationFixupCache hit -> returns cached mapping
 *    - sourceLocationFixupCache miss -> loops over prefixMappings
 *      * prefix matches (sourceFile.startsWith) -> replaces prefix, breaks
 *      * no prefix matches -> retains original sourceFile
 *      * puts result into sourceLocationFixupCache
 * 5. Lifecycle and delegation methods:
 *    - appendTo(Appendable, String)
 *    - reset() -> resets generator and clears fixup cache
 *    - setStartingPosition(int, int)
 *    - setWrapperPrefix(String)
 *    - validate(boolean)
 *    - setPrefixMappings(List<LocationMapping>)
 *
 * Known Defects4J Ground Truth Defect:
 * - SourceMapGeneratorV3 line number offset regression: Original line numbers were decremented
 *   unexpectedly (e.g., expected:<10> but was:<9>) or "lineCount":1 was omitted in golden output.
 *   Targeted in Partition C tests.
 */
public class SourceMapGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFormatEnumGetInstances() {
    SourceMap v1 = SourceMap.Format.V1.getInstance();
    assertNotNull("Format.V1 must instantiate SourceMap", v1);

    SourceMap v2 = SourceMap.Format.V2.getInstance();
    assertNotNull("Format.V2 must instantiate SourceMap", v2);

    SourceMap v3 = SourceMap.Format.V3.getInstance();
    assertNotNull("Format.V3 must instantiate SourceMap", v3);

    SourceMap def = SourceMap.Format.DEFAULT.getInstance();
    assertNotNull("Format.DEFAULT must instantiate SourceMap", def);
  }

  @Test(timeout = 4000)
  public void testAddMappingBasic() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "myVariable");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("app.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 10));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "compiled.js");
    String output = writer.toString();

    assertTrue("Output sourcemap must contain source file name", output.contains("app.js"));
    assertTrue("Output sourcemap must specify compiled file name", output.contains("compiled.js"));
  }

  @Test(timeout = 4000)
  public void testAddMappingWithOriginalName() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "obfuscatedVar");
    node.setLineno(5);
    node.setCharno(2);
    node.setSourceFileName("module.js");
    node.putProp(Node.ORIGINALNAME_PROP, "originalSymbolName");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 13));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("Output must record original symbol name", output.contains("originalSymbolName"));
  }

  @Test(timeout = 4000)
  public void testPrefixMappingsSingleMatch() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    List<SourceMap.LocationMapping> mappings = new ArrayList<>();
    mappings.add(new SourceMap.LocationMapping("/root/src/", "http://example.com/src/"));
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "foo");
    node.setLineno(2);
    node.setCharno(1);
    node.setSourceFileName("/root/src/sub/file.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("Prefix should be replaced with replacement URL",
        output.contains("http://example.com/src/sub/file.js"));
    assertFalse("Original prefix path should no longer be present",
        output.contains("/root/src/sub/file.js"));
  }

  @Test(timeout = 4000)
  public void testPrefixMappingsMultipleMatchesTakesFirst() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    List<SourceMap.LocationMapping> mappings = new ArrayList<>();
    mappings.add(new SourceMap.LocationMapping("/root/src/", "http://first.com/"));
    mappings.add(new SourceMap.LocationMapping("/root/", "http://second.com/"));
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "bar");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("/root/src/file.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("First matching prefix mapping should be chosen",
        output.contains("http://first.com/file.js"));
  }

  @Test(timeout = 4000)
  public void testPrefixMappingsNoMatchRetainsOriginal() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    List<SourceMap.LocationMapping> mappings = new ArrayList<>();
    mappings.add(new SourceMap.LocationMapping("/prefix1/", "/replaced1/"));
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "baz");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("/other/path/file.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("Original path should be preserved when no prefix matches",
        output.contains("/other/path/file.js"));
  }

  @Test(timeout = 4000)
  public void testResetClearsFixupCache() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    List<SourceMap.LocationMapping> mappings = new ArrayList<>();
    mappings.add(new SourceMap.LocationMapping("/src/", "http://v1/"));
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "val");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("/src/app.js");
    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    // Reset clears both generator state and prefix fixup cache
    sourceMap.reset();

    // Now change prefix mappings
    List<SourceMap.LocationMapping> newMappings = new ArrayList<>();
    newMappings.add(new SourceMap.LocationMapping("/src/", "http://v2/"));
    sourceMap.setPrefixMappings(newMappings);

    Node node2 = Node.newString(Token.NAME, "val");
    node2.setLineno(1);
    node2.setCharno(0);
    node2.setSourceFileName("/src/app.js");
    sourceMap.addMapping(node2, new FilePosition(0, 0), new FilePosition(0, 3));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("After reset, fresh prefix mappings should take effect",
        output.contains("http://v2/app.js"));
    assertFalse("Cached prefix from before reset must not be present",
        output.contains("http://v1/app.js"));
  }

  @Test(timeout = 4000)
  public void testConfigurationDelegation() {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    sourceMap.setStartingPosition(10, 5);
    sourceMap.setWrapperPrefix("/* wrapper */\n");
    sourceMap.validate(true);
    sourceMap.validate(false);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Predicate Branching
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddMappingWithNullSourceFileSkipped() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "x");
    node.setLineno(10);
    node.setCharno(0);
    // Source file name is explicitly not set (null)

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertFalse("Node without source file should not produce mapping entry",
        output.contains("\"sources\":[\"null\"]"));
  }

  @Test(timeout = 4000)
  public void testAddMappingWithNegativeLineNumberSkipped() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "y");
    node.setLineno(-1);
    node.setCharno(0);
    node.setSourceFileName("valid.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    // In V3, sources list should not contain valid.js if mapping was skipped
    assertFalse("Node with lineno < 0 must be skipped",
        output.contains("\"valid.js\""));
  }

  @Test(timeout = 4000)
  public void testAddMappingWithZeroLineNumberAllowed() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "zeroLineNode");
    node.setLineno(0);
    node.setCharno(0);
    node.setSourceFileName("zero.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    String output = writer.toString();

    assertTrue("Node with lineno == 0 is valid and must be included",
        output.contains("zero.js"));
  }

  @Test(timeout = 4000)
  public void testDetailLevelAllAlwaysReturnsTrue() {
    SourceMap.DetailLevel all = SourceMap.DetailLevel.ALL;

    assertTrue(all.apply(new Node(Token.CALL)));
    assertTrue(all.apply(new Node(Token.NEW)));
    assertTrue(all.apply(new Node(Token.FUNCTION)));
    assertTrue(all.apply(Node.newString(Token.NAME, "varName")));
    assertTrue(all.apply(new Node(Token.GETPROP)));
    assertTrue(all.apply(new Node(Token.GETELEM)));
    assertTrue(all.apply(Node.newNumber(42.0)));
    assertTrue(all.apply(new Node(Token.ASSIGN)));
    assertTrue(all.apply(new Node(Token.VAR)));
    assertTrue(all.apply(null));
  }

  @Test(timeout = 4000)
  public void testDetailLevelSymbolsConditionCoverage() {
    SourceMap.DetailLevel symbols = SourceMap.DetailLevel.SYMBOLS;

    // 1. isCall()
    assertTrue("Call node should match SYMBOLS", symbols.apply(new Node(Token.CALL)));

    // 2. isNew()
    assertTrue("New node should match SYMBOLS", symbols.apply(new Node(Token.NEW)));

    // 3. isFunction()
    assertTrue("Function node should match SYMBOLS", symbols.apply(new Node(Token.FUNCTION)));

    // 4. isName()
    assertTrue("Name node should match SYMBOLS", symbols.apply(Node.newString(Token.NAME, "testName")));

    // 5. NodeUtil.isGet(node) - GETPROP
    assertTrue("GETPROP node should match SYMBOLS", symbols.apply(new Node(Token.GETPROP)));

    // 6. NodeUtil.isGet(node) - GETELEM
    assertTrue("GETELEM node should match SYMBOLS", symbols.apply(new Node(Token.GETELEM)));

    // 7. NodeUtil.isObjectLitKey(node, node.getParent())
    Node objLit = new Node(Token.OBJECTLIT);
    Node keyNode = Node.newString("objectKey");
    objLit.addChildToBack(keyNode);
    assertTrue("ObjectLit key should match SYMBOLS", symbols.apply(keyNode));

    // 8. (node.isString() && NodeUtil.isGet(node.getParent()))
    Node getPropParent = new Node(Token.GETPROP);
    Node strUnderGet = Node.newString("subProperty");
    getPropParent.addChildToBack(strUnderGet);
    assertTrue("String under GET parent should match SYMBOLS", symbols.apply(strUnderGet));

    // 9. Negative branches
    Node standaloneString = Node.newString("plainString");
    assertFalse("Standalone string should not match SYMBOLS", symbols.apply(standaloneString));

    Node varParent = new Node(Token.VAR);
    Node strUnderVar = Node.newString("varString");
    varParent.addChildToBack(strUnderVar);
    assertFalse("String under VAR should not match SYMBOLS", symbols.apply(strUnderVar));

    assertFalse("Number node should not match SYMBOLS", symbols.apply(Node.newNumber(99.0)));
    assertFalse("Assign node should not match SYMBOLS", symbols.apply(new Node(Token.ASSIGN)));
    assertFalse("Return node should not match SYMBOLS", symbols.apply(new Node(Token.RETURN)));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  /**
   * Targets known Defects4J defect:
   * SourceMapGeneratorV3Test::testBasicMapping1 / testMultilineMapping
   * AssertionFailedError: expected:<10> but was:<9>
   *
   * Verifies that the source line number preserved in V3 source maps correctly reflects
   * the 1-based original line number without off-by-one reduction.
   */
  @Test(timeout = 4000)
  public void testV3MappingPreservesExactSourceLineNumber() throws Exception {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "targetVar");
    node.setLineno(10);
    node.setCharno(2);
    node.setSourceFileName("source_v3.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 9));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "compiled_v3.js");
    String output = writer.toString();

    SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
    consumer.parse(output);

    OriginalMapping mapping = consumer.getMappingForLine(1, 1);
    assertNotNull("Mapping must exist at output line 1, col 1", mapping);
    assertEquals("Original line number must be exactly 10, not 9", 10, mapping.getLineNumber());
    assertEquals("Original column number must be exactly 2", 2, mapping.getColumnPosition());
    assertEquals("Original source file must match", "source_v3.js", mapping.getOriginalFile());
  }

  /**
   * Targets known Defects4J defect:
   * SourceMapGeneratorV3Test::testGoldenOutput0a
   * ComparisonFailure: expected:<...t":1,
   *
   * Verifies that V3 generator outputs the lineCount attribute as required by the spec.
   */
  @Test(timeout = 4000)
  public void testV3GoldenOutputContainsLineCount() throws Exception {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "a");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("testcode");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "testcode");
    String output = writer.toString();

    assertTrue("V3 sourcemap golden output must contain lineCount:1",
        output.contains("\"lineCount\":1") || output.contains("\"lineCount\": 1"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IOException.class, timeout = 4000)
  public void testAppendToFailsWhenAppendableThrows() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Appendable throwingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Simulated write failure");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Simulated write failure");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Simulated write failure");
      }
    };

    sourceMap.appendTo(throwingAppendable, "out.js");
  }

  @Test(timeout = 4000)
  public void testFixupSourceLocationWithEmptyPrefixList() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    sourceMap.setPrefixMappings(Collections.emptyList());

    Node node = Node.newString(Token.NAME, "z");
    node.setLineno(1);
    node.setCharno(0);
    node.setSourceFileName("untouched/file.js");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter writer = new StringWriter();
    sourceMap.appendTo(writer, "out.js");
    assertTrue("Empty prefix mappings should leave filename untouched",
        writer.toString().contains("untouched/file.js"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLocationMappingFieldAssignment() {
    SourceMap.LocationMapping lm = new SourceMap.LocationMapping("/orig/prefix/", "http://new/");
    assertEquals("Prefix must be accurately assigned", "/orig/prefix/", lm.prefix);
    assertEquals("Replacement must be accurately assigned", "http://new/", lm.replacement);
  }

  @Test(timeout = 4000)
  public void testEnumsContractIntegrity() {
    SourceMap.Format[] formats = SourceMap.Format.values();
    assertEquals("Format must have exactly 4 values", 4, formats.length);
    assertEquals(SourceMap.Format.V1, SourceMap.Format.valueOf("V1"));
    assertEquals(SourceMap.Format.DEFAULT, SourceMap.Format.valueOf("DEFAULT"));
    assertEquals(SourceMap.Format.V2, SourceMap.Format.valueOf("V2"));
    assertEquals(SourceMap.Format.V3, SourceMap.Format.valueOf("V3"));

    SourceMap.DetailLevel[] detailLevels = SourceMap.DetailLevel.values();
    assertEquals("DetailLevel must have exactly 2 values", 2, detailLevels.length);
    assertEquals(SourceMap.DetailLevel.ALL, SourceMap.DetailLevel.valueOf("ALL"));
    assertEquals(SourceMap.DetailLevel.SYMBOLS, SourceMap.DetailLevel.valueOf("SYMBOLS"));
  }

  @Test(timeout = 4000)
  public void testV1AndV2SourceMapOutputs() throws IOException {
    // Validate V1 functionality
    SourceMap v1 = SourceMap.Format.V1.getInstance();
    Node nodeV1 = Node.newString(Token.NAME, "varV1");
    nodeV1.setLineno(1);
    nodeV1.setCharno(0);
    nodeV1.setSourceFileName("v1_file.js");
    v1.addMapping(nodeV1, new FilePosition(0, 0), new FilePosition(0, 5));

    StringWriter writerV1 = new StringWriter();
    v1.appendTo(writerV1, "v1_out.js");
    assertTrue("V1 format output should not be empty", writerV1.toString().length() > 0);

    // Validate V2 functionality
    SourceMap v2 = SourceMap.Format.V2.getInstance();
    Node nodeV2 = Node.newString(Token.NAME, "varV2");
    nodeV2.setLineno(1);
    nodeV2.setCharno(0);
    nodeV2.setSourceFileName("v2_file.js");
    v2.addMapping(nodeV2, new FilePosition(0, 0), new FilePosition(0, 5));

    StringWriter writerV2 = new StringWriter();
    v2.appendTo(writerV2, "v2_out.js");
    assertTrue("V2 format output should not be empty", writerV2.toString().length() > 0);
  }
}