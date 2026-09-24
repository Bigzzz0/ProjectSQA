/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.SourceMap
 *
 * 1. Branch & Decision Logic Covered:
 *  - addMapping:
 *    - node sourceFile == null vs non-null
 *    - node.getLineno() < 0 (unmapped line) vs >= 0
 *    - lastSourceFile == sourceFile (caching check) vs lastSourceFile != sourceFile
 *    - node.getProp(Node.ORIGINALNAME_PROP) != null vs null
 *    - startPosition.getLineNumber() > 0 vs == 0 (offset character position branch)
 *    - endPosition.getLineNumber() > 0 vs == 0 (offset character position branch)
 *  - setWrapperPrefix:
 *    - prefix contains '\n' (increment prefixLine, reset prefixIndex) vs other characters (increment prefixIndex)
 *  - setStartingPosition:
 *    - changes offsetPosition line/column
 *  - reset:
 *    - restores mappings, offsetPosition, and prefixPosition to clean state
 *  - findLastLine:
 *    - maxLine calculation over all mappings and adding prefixPosition.getLineNumber()
 *  - appendTo:
 *    - output structure matching LavaBug format: "/** Begin line maps. **/", file count,
 *      "/** Begin file information. **/", and "/** Begin mapping definitions. **/"
 *  - LineMapper & traversal stack:
 *    - Empty mappings: Preconditions.checkState(!mappings.isEmpty())
 *    - Overlapping mappings: isOverlapped (same line end >= start, or endLine > startLine)
 *    - Non-overlapping sibling mappings: popping stack and closing mappings
 *    - Gaps between mappings: writeCharsBetween with parent id vs UNMAPPED (-1)
 *    - Multiline spans and line breaks in writeCharsUpTo
 *    - First char vs subsequent chars in addCharEntry (comma separation)
 *
 * 2. Defect-Targeted Branches (Defects4J SourceMap failures):
 *  - Mapping definition generation and accurate character span tracking
 *  - Traversal with single vs multiple nested/overlapping nodes
 *  - Multiline generated output spans and offsets
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SourceMapGeminiTest {

  private SourceMap sourceMap;

  @Before
  public void setUp() {
    sourceMap = new SourceMap();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSingleMappingBasicOutput() throws IOException {
    Node node = Node.newString("foo");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "test.js");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 5));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    assertTrue(output.contains("/** Begin line maps. **/{ \"file\" : \"out.js\", \"count\": 1 }"));
    assertTrue(output.contains("[0,0,0,0,0]"));
    assertTrue(output.contains("/** Begin file information. **/"));
    assertTrue(output.contains("/** Begin mapping definitions. **/"));
    assertTrue(output.contains("[\"test.js\",1,0]"));
  }

  @Test(timeout = 4000)
  public void testMappingWithOriginalName() throws IOException {
    Node node = Node.newString("bar");
    node.setLineno(10);
    node.setCharno(4);
    node.putProp(Node.SOURCEFILE_PROP, "source.js");
    node.putProp(Node.ORIGINALNAME_PROP, "origBar");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "generated.js");
    String output = sb.toString();

    assertTrue(output.contains("[\"source.js\",10,4,\"origBar\"]"));
  }

  @Test(timeout = 4000)
  public void testSequentialSiblingMappings() throws IOException {
    Node node1 = Node.newString("a");
    node1.setLineno(1);
    node1.setCharno(0);
    node1.putProp(Node.SOURCEFILE_PROP, "src.js");

    Node node2 = Node.newString("b");
    node2.setLineno(1);
    node2.setCharno(5);
    node2.putProp(Node.SOURCEFILE_PROP, "src.js");

    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 2));
    sourceMap.addMapping(node2, new Position(0, 2), new Position(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    assertTrue(output.contains("[0,0,1,1]"));
    assertTrue(output.contains("[\"src.js\",1,0]"));
    assertTrue(output.contains("[\"src.js\",1,5]"));
  }

  @Test(timeout = 4000)
  public void testResetClearsState() throws IOException {
    Node node = Node.newString("var");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "src.js");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));
    sourceMap.setWrapperPrefix("prefix\n");
    sourceMap.setStartingPosition(5, 5);

    sourceMap.reset();

    // After reset, mappings is empty, so appendTo should throw IllegalStateException
    try {
      sourceMap.appendTo(new StringBuilder(), "out.js");
      fail("Expected IllegalStateException for empty mappings after reset");
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNodeWithNullSourceFileIgnored() {
    Node node = Node.newString("no_file");
    node.setLineno(1);
    node.setCharno(0);
    // SOURCEFILE_PROP not set

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 5));

    try {
      sourceMap.appendTo(new StringBuilder(), "out.js");
      fail("Expected IllegalStateException because no valid mapping was added");
    } catch (IllegalStateException | IOException expected) {
      assertTrue(expected instanceof IllegalStateException);
    }
  }

  @Test(timeout = 4000)
  public void testNodeWithNegativeLineNumberIgnored() {
    Node node = Node.newString("negative_line");
    node.setLineno(-1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "src.js");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 5));

    try {
      sourceMap.appendTo(new StringBuilder(), "out.js");
      fail("Expected IllegalStateException because node with line < 0 is ignored");
    } catch (IllegalStateException | IOException expected) {
      assertTrue(expected instanceof IllegalStateException);
    }
  }

  @Test(timeout = 4000)
  public void testSameSourceFileCachedBranch() throws IOException {
    String filename = new String("shared.js");
    Node n1 = Node.newString("x");
    n1.setLineno(1);
    n1.setCharno(1);
    n1.putProp(Node.SOURCEFILE_PROP, filename);

    Node n2 = Node.newString("y");
    n2.setLineno(2);
    n2.setCharno(2);
    // Reuse identical string reference to hit lastSourceFile == sourceFile
    n2.putProp(Node.SOURCEFILE_PROP, filename);

    sourceMap.addMapping(n1, new Position(0, 0), new Position(0, 1));
    sourceMap.addMapping(n2, new Position(0, 1), new Position(0, 2));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    assertTrue(out.contains("[0,1]"));
    assertTrue(out.contains("[\"shared.js\",1,1]"));
    assertTrue(out.contains("[\"shared.js\",2,2]"));
  }

  @Test(timeout = 4000)
  public void testDifferentSourceFilesUpdated() throws IOException {
    Node n1 = Node.newString("x");
    n1.setLineno(1);
    n1.setCharno(1);
    n1.putProp(Node.SOURCEFILE_PROP, "file1.js");

    Node n2 = Node.newString("y");
    n2.setLineno(2);
    n2.setCharno(2);
    n2.putProp(Node.SOURCEFILE_PROP, "file2.js");

    sourceMap.addMapping(n1, new Position(0, 0), new Position(0, 1));
    sourceMap.addMapping(n2, new Position(0, 1), new Position(0, 2));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    assertTrue(out.contains("[\"file1.js\",1,1]"));
    assertTrue(out.contains("[\"file2.js\",2,2]"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Nested Mappings & Hierarchy)
  // =========================================================================

  @Test(timeout = 4000)
  public void testNestedMappingHierarchy() throws IOException {
    // Parent mapping covers [0, 0] to [0, 10]
    Node parent = Node.newString("parent");
    parent.setLineno(1);
    parent.setCharno(0);
    parent.putProp(Node.SOURCEFILE_PROP, "source.js");

    // Child mapping covers [0, 2] to [0, 6]
    Node child = Node.newString("child");
    child.setLineno(1);
    child.setCharno(2);
    child.putProp(Node.SOURCEFILE_PROP, "source.js");

    sourceMap.addMapping(parent, new Position(0, 0), new Position(0, 10));
    sourceMap.addMapping(child, new Position(0, 2), new Position(0, 6));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    // Line 0 should be:
    // 0..1: parent (0,0)
    // 2..5: child (1,1,1,1)
    // 6..9: parent resumed (0,0,0,0)
    assertTrue(out.contains("[0,0,1,1,1,1,0,0,0,0]"));
  }

  @Test(timeout = 4000)
  public void testUnmappedGapBeforeFirstMapping() throws IOException {
    // Generated code starts with an unmapped section from col 0 to col 3
    Node node = Node.newString("token");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "file.js");

    sourceMap.addMapping(node, new Position(0, 3), new Position(0, 6));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    // 0..2 should be unmapped (-1)
    // 3..5 should be mapping 0
    assertTrue(out.contains("[-1,-1,-1,0,0,0]"));
  }

  @Test(timeout = 4000)
  public void testOffsetPositionHandling() throws IOException {
    // Set starting position at offset line 2, character 4
    sourceMap.setStartingPosition(2, 4);

    Node node1 = Node.newString("firstLine");
    node1.setLineno(1);
    node1.setCharno(0);
    node1.putProp(Node.SOURCEFILE_PROP, "file.js");
    // Start at line 0: startOffsetPosition = 4, endOffsetPosition = 4
    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 3));

    Node node2 = Node.newString("secondLine");
    node2.setLineno(2);
    node2.setCharno(0);
    node2.putProp(Node.SOURCEFILE_PROP, "file.js");
    // Start at line 1 (>0): startOffsetPosition = 0, endOffsetPosition = 0
    sourceMap.addMapping(node2, new Position(1, 0), new Position(1, 2));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    assertTrue(out.contains("\"count\": 4 }"));
    assertTrue(out.contains("[-1,-1,-1,-1,0,0,0]"));
  }

  @Test(timeout = 4000)
  public void testWrapperPrefixSingleLineAndMultiLine() throws IOException {
    sourceMap.setWrapperPrefix("(function(){\n  ");

    Node node = Node.newString("expr");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "src.js");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    assertTrue(out.contains("\"count\": 2 }"));
    // First line should be empty/unmapped before closing
    assertTrue(out.contains("[]\n[-1,-1,0,0,0,0]\n"));
  }

  // =========================================================================
  // Partition D: Multi-line and Complex Structural Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultiLineMappingSpan() throws IOException {
    Node node = Node.newString("multiline");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "src.js");

    // Mapping spanning across line boundaries: line 0 col 0 to line 2 col 2
    sourceMap.addMapping(node, new Position(0, 0), new Position(2, 2));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String out = sb.toString();

    assertTrue(out.contains("\"count\": 3 }"));
    assertTrue(out.contains("[0,0]\n"));
  }

  @Test(timeout = 4000)
  public void testEmptyMappingsThrowsException() throws IOException {
    try {
      sourceMap.appendTo(new StringBuilder(), "test.js");
      fail("Expected IllegalStateException when appendTo is called on empty SourceMap");
    } catch (IllegalStateException expected) {
      assertNotNull(expected);
    }
  }

  @Test(timeout = 4000)
  public void testSpecialCharactersInSourceAndOriginalNameEscaped() throws IOException {
    Node node = Node.newString("val");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.SOURCEFILE_PROP, "path/with\"quotes\".js");
    node.putProp(Node.ORIGINALNAME_PROP, "name\"with\"quotes");

    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output\"quoted\".js");
    String out = sb.toString();

    // Verify proper JSON/JS escaping in definitions and header
    assertTrue(out.contains("\"output\\\"quoted\\\".js\""));
    assertTrue(out.contains("\"path/with\\\"quotes\\\".js\""));
    assertTrue(out.contains("\"name\\\"with\\\"quotes\""));
  }

  // =========================================================================
  // Partition E: Direct Mapping Object State & appendTo Contract
  // =========================================================================

  @Test(timeout = 4000)
  public void testMappingClassDirectAppendTo() throws IOException {
    SourceMap.Mapping mapping = new SourceMap.Mapping();
    mapping.sourceFile = "\"unit_test.js\"";
    mapping.originalPosition = new Position(42, 7);
    mapping.startPosition = new Position(0, 0);
    mapping.endPosition = new Position(0, 10);
    mapping.originalName = null;

    StringBuilder sb = new StringBuilder();
    mapping.appendTo(sb);
    assertEquals("[\"unit_test.js\",42,7]", sb.toString());

    mapping.originalName = "\"token\"";
    sb = new StringBuilder();
    mapping.appendTo(sb);
    assertEquals("[\"unit_test.js\",42,7,\"token\"]", sb.toString());
  }

  @Test(timeout = 4000)
  public void testMultipleSequentialDisjointMappingsOnDifferentLines() throws IOException {
    Node node1 = Node.newString("line0");
    node1.setLineno(1);
    node1.setCharno(0);
    node1.putProp(Node.SOURCEFILE_PROP, "app.js");

    Node node2 = Node.newString("line1");
    node2.setLineno(2);
    node2.setCharno(0);
    node2.putProp(Node.SOURCEFILE_PROP, "app.js");

    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 4));
    sourceMap.addMapping(node2, new Position(1, 0), new Position(1, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "bundle.js");
    String out = sb.toString();

    assertTrue(out.contains("[0,0,0,0]\n[1,1,1,1]\n"));
    assertTrue(out.contains("/** Begin file information. **/\n[]\n[]\n"));
  }
}