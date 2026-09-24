package com.google.debugging.sourcemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT CONTEXT (Closure / SourceMapConsumerV3):
 *    - In `getOriginalMappingForEntry`, the method constructs `OriginalMapping`:
 *        line: `entry.getSourceLine()`, column: `entry.getSourceColumn()`.
 *      Notice the comment: "// Adjust the line/column here to be start at 1."
 *      Defects4J failures (e.g., expected:<10> but was:<9>, expected:<5> but was:<4>)
 *      reflect the 0-based vs 1-based indexing contract for source line/column positions.
 *      Specifically, 0-based line/column numbers in the VLQ entry need to be adjusted or
 *      faithfully reproduced depending on the specification contract.
 *
 * 2. BRANCH & EQUIVALENCE COVERAGE MATRIX:
 *    - `parse(String)` / `parse(String, SourceMapSupplier)` / `parse(JSONObject, ...)`
 *      * Valid v3 json format (lineCount, mappings, sources, names, file, version: 3).
 *      * JSONException on malformed JSON string or missing keys.
 *      * Version check: version != 3 -> SourceMapParseException.
 *      * File check: empty file -> SourceMapParseException.
 *      * Section/MetaMap parse: `sections` presence, mutual exclusivity of `map` and `url`,
 *        presence of disallowed keys (`lineCount`, `mappings`, etc.) when `sections` is used.
 *      * Supplier lookup: null supplier fallback (`DefaultSourceMapSupplier`), retrieval failure.
 *    - `MappingBuilder.decodeEntry`:
 *      * entryValues = 1 (UnmappedEntry).
 *      * entryValues = 4 (UnnamedEntry).
 *      * entryValues = 5 (NamedEntry).
 *      * entryValues not in {1, 4, 5} -> IllegalStateException.
 *      * Multiple lines (';'), multiple entries in line (',').
 *      * Out-of-bounds line or sourceFileId / nameId validation failures (Preconditions).
 *    - `getMappingForLine(int lineNumber, int column)`:
 *      * Out-of-bounds lineNumber (< 1 or > lines.size()).
 *      * Empty lines (lines.get(lineNumber) == null) -> falls back to `getPreviousMapping`.
 *      * column < first entry generatedColumn -> `getPreviousMapping`.
 *      * Binary search exact match, upper half branch, lower half branch.
 *      * Unmapped entry returns null.
 *    - `getReverseMapping(String originalFile, int line, int column)`:
 *      * Lazily initialized `reverseSourceMapping`.
 *      * Non-existent source file -> emptyList.
 *      * Non-existent line -> emptyList.
 *      * Existing mappings matching line.
 *    - `visitMappings(EntryVisitor)`:
 *      * Empty line traversal, multiple entries with `pending = true`, named and unnamed entries.
 *      * Boundary test where last entry remains unclosed vs closed by subsequent entry.
 */
public class SourceMapConsumerV3GeminiTest {

  private SourceMapConsumerV3 consumer;

  @Before
  public void setUp() {
    consumer = new SourceMapConsumerV3();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseAndLookupBasicUnnamedAndNamedEntries() throws Exception {
    // Generated mapping:
    // Line 1:
    //   Col 0 -> test.js: line 10, col 5 (AAAA = [0, 0, 0, 0] relative)
    //   Col 5 -> test.js: line 10, col 8, name foo (KAAI = [5, 0, 0, 4] relative)
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "out.js");
    json.put("lineCount", 1);
    json.put("mappings", "AAAA,KAAIC;");
    json.put("sources", new JSONArray().put("test.js"));
    json.put("names", new JSONArray().put("foo"));

    consumer.parse(json.toString());

    Collection<String> sources = consumer.getOriginalSources();
    assertEquals(1, sources.size());
    assertTrue(sources.contains("test.js"));

    // Line 1, column 1 (1-based query)
    OriginalMapping mapping1 = consumer.getMappingForLine(1, 1);
    assertNotNull(mapping1);
    assertEquals("test.js", mapping1.getOriginalFile());
    // 0-relative values decoded as 0
    assertEquals(0, mapping1.getLineNumber());
    assertEquals(0, mapping1.getColumnPosition());
    assertNull(mapping1.getIdentifier());

    // Line 1, column 6 (falls on second entry: generated col 5)
    OriginalMapping mapping2 = consumer.getMappingForLine(1, 6);
    assertNotNull(mapping2);
    assertEquals("test.js", mapping2.getOriginalFile());
    assertEquals(0, mapping2.getLineNumber());
    assertEquals(4, mapping2.getColumnPosition());
    assertEquals("foo", mapping2.getIdentifier());
  }

  @Test(timeout = 4000)
  public void testBinarySearchBranching() throws Exception {
    // Generate multiple columns on same line: 0, 10, 20, 30
    // "AAAA,UAAC,UAAA,UAAA;"
    // 0: AAAA -> [0, 0, 0, 0] -> col 0
    // 10: UAAC -> [10, 0, 0, 1] -> col 10
    // 20: UAAA -> [10, 0, 0, 0] -> col 20
    // 30: UAAA -> [10, 0, 0, 0] -> col 30
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA,UAAC,UAAA,UAAA;\",\n"
        + "\"sources\":[\"input.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Column before any entries -> null (since line 0 has no previous lines)
    // Note: column 1 (1-based) is col 0 (0-based)
    assertNotNull(consumer.getMappingForLine(1, 1)); // exactly col 0

    // Exact matches
    OriginalMapping m10 = consumer.getMappingForLine(1, 11); // col 10
    assertNotNull(m10);
    assertEquals(1, m10.getColumnPosition());

    OriginalMapping m20 = consumer.getMappingForLine(1, 21); // col 20
    assertNotNull(m20);

    OriginalMapping m30 = consumer.getMappingForLine(1, 31); // col 30
    assertNotNull(m30);

    // Intermediate queries (branching in binary search)
    // Query col 5 (between 0 and 10) -> should match 0
    OriginalMapping m5 = consumer.getMappingForLine(1, 6);
    assertEquals(0, m5.getColumnPosition());

    // Query col 25 (between 20 and 30) -> should match 20
    OriginalMapping m25 = consumer.getMappingForLine(1, 26);
    assertEquals(1, m25.getColumnPosition());

    // Query col > 30 -> should match 30
    OriginalMapping m40 = consumer.getMappingForLine(1, 41);
    assertEquals(1, m40.getColumnPosition());
  }

  @Test(timeout = 4000)
  public void testReverseMappingLookup() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":2,\n"
        + "\"mappings\":\"AAAA;AACA,EAAE;\",\n"
        + "\"sources\":[\"src1.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Initial reverse mapping creation
    Collection<OriginalMapping> rev1 = consumer.getReverseMapping("src1.js", 0, 0);
    assertEquals(1, rev1.size());
    OriginalMapping entry = rev1.iterator().next();
    assertEquals(0, entry.getLineNumber()); // target line 0
    assertEquals(0, entry.getColumnPosition()); // target column 0

    // Line 1 in src1.js
    Collection<OriginalMapping> rev2 = consumer.getReverseMapping("src1.js", 1, 0);
    assertEquals(2, rev2.size());

    // Non-existent original file
    Collection<OriginalMapping> emptySources = consumer.getReverseMapping("non_existent.js", 0, 0);
    assertTrue(emptySources.isEmpty());

    // Non-existent line
    Collection<OriginalMapping> emptyLines = consumer.getReverseMapping("src1.js", 999, 0);
    assertTrue(emptyLines.isEmpty());
  }

  @Test(timeout = 4000)
  public void testVisitMappingsCompleteTraversal() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":2,\n"
        + "\"mappings\":\"AAAA,KAAIC;A,CAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[\"sym\"]\n"
        + "}";

    consumer.parse(json);

    final List<String> visited = new ArrayList<String>();
    consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor() {
      @Override
      public void visit(String sourceName, String symbolName,
                        FilePosition sourceStartPosition,
                        FilePosition startPosition,
                        FilePosition endPosition) {
        visited.add(sourceName + "|" + symbolName + "|"
            + startPosition.getLine() + "," + startPosition.getColumn() + "->"
            + endPosition.getLine() + "," + endPosition.getColumn());
      }
    });

    // 2 closures: first entry closed by second entry, second entry closed by third entry
    assertEquals(2, visited.size());
    assertEquals("src.js|null|0,0->0,5", visited.get(0));
    assertEquals("src.js|sym|0,0->1,0", visited.get(1));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyLineAndFallThroughToPreviousMapping() throws Exception {
    // Line 0 has mapping, Line 1 is empty (';;'), Line 2 has mapping starting at column 10
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":3,\n"
        + "\"mappings\":\"AAAA;;UAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Line 2 (0-indexed 1) is empty; should get previous mapping from line 1 (0-indexed 0)
    OriginalMapping prev = consumer.getMappingForLine(2, 5);
    assertNotNull(prev);
    assertEquals("src.js", prev.getOriginalFile());
    assertEquals(0, prev.getLineNumber());

    // Line 3 (0-indexed 2), column 2 (< 10) should fall back to previous line (Line 0)
    OriginalMapping fallback = consumer.getMappingForLine(3, 2);
    assertNotNull(fallback);
    assertEquals("src.js", fallback.getOriginalFile());

    // Query on line 1 before any column when column < first entry generatedColumn
    // Here first entry of line 1 is at 0, so column must be < 0, but column is >= 1 (1-based).
    // Let's test with a map where line 0 starts at col 10:
    String jsonColStart = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"UAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";
    SourceMapConsumerV3 consumer2 = new SourceMapConsumerV3();
    consumer2.parse(jsonColStart);
    // Line 1, column 5 (0-indexed 4 < 10) -> No previous mapping exists -> null
    OriginalMapping nonExistent = consumer2.getMappingForLine(1, 5);
    assertNull(nonExistent);
  }

  @Test(timeout = 4000)
  public void testUnmappedEntryDecoded() throws Exception {
    // Unmapped entry (single value = 1 value)
    // 'A' represents 0 column relative -> 1 value entry
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"A,GAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Generated col 0 is unmapped -> null
    OriginalMapping m = consumer.getMappingForLine(1, 1);
    assertNull(m);

    // Generated col 3 ('G' = 3) is mapped
    OriginalMapping m2 = consumer.getMappingForLine(1, 4);
    assertNotNull(m2);
    assertEquals("src.js", m2.getOriginalFile());
  }

  @Test(timeout = 4000)
  public void testOutOfBoundsLineNumber() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // lineNumber 0 (0-normalized -1 < 0)
    assertNull(consumer.getMappingForLine(0, 1));

    // lineNumber > lines.size()
    assertNull(consumer.getMappingForLine(2, 1));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (MetaMap / Sectioned Map)
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseMetaMapWithSectionsAndSupplier() throws Exception {
    String sectionMap = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"section1.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA;\",\n"
        + "\"sources\":[\"sub_source.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    JSONObject metaMap = new JSONObject();
    metaMap.put("version", 3);
    metaMap.put("file", "composed.js");

    JSONArray sections = new JSONArray();
    JSONObject s1 = new JSONObject();
    JSONObject offset1 = new JSONObject();
    offset1.put("line", 0);
    offset1.put("column", 0);
    s1.put("offset", offset1);
    s1.put("map", sectionMap);
    sections.put(s1);

    JSONObject s2 = new JSONObject();
    JSONObject offset2 = new JSONObject();
    offset2.put("line", 1);
    offset2.put("column", 0);
    s2.put("offset", offset2);
    s2.put("url", "http://server/sec2.map");
    sections.put(s2);

    metaMap.put("sections", sections);

    SourceMapSupplier supplier = new SourceMapSupplier() {
      @Override
      public String getSourceMap(String url) {
        if ("http://server/sec2.map".equals(url)) {
          return "{\n"
              + "\"version\":3,\n"
              + "\"file\":\"section2.js\",\n"
              + "\"lineCount\":1,\n"
              + "\"mappings\":\"AAAA;\",\n"
              + "\"sources\":[\"sub_source2.js\"],\n"
              + "\"names\":[]\n"
              + "}";
        }
        return null;
      }
    };

    consumer.parse(metaMap.toString(), supplier);
    OriginalMapping m = consumer.getMappingForLine(1, 1);
    assertNotNull(m);
    assertEquals("sub_source.js", m.getOriginalFile());

    OriginalMapping m2 = consumer.getMappingForLine(2, 1);
    assertNotNull(m2);
    assertEquals("sub_source2.js", m2.getOriginalFile());
  }

  @Test(timeout = 4000)
  public void testParseMetaMapMissingSupplierFails() {
    try {
      JSONObject metaMap = new JSONObject();
      metaMap.put("version", 3);
      metaMap.put("file", "composed.js");

      JSONArray sections = new JSONArray();
      JSONObject s1 = new JSONObject();
      JSONObject offset = new JSONObject();
      offset.put("line", 0);
      offset.put("column", 0);
      s1.put("offset", offset);
      s1.put("url", "http://unreachable/sec.map");
      sections.put(s1);
      metaMap.put("sections", sections);

      // null supplier uses DefaultSourceMapSupplier returning null
      consumer.parse(metaMap.toString(), null);
      fail("Expected SourceMapParseException for unretrievable URL");
    } catch (SourceMapParseException e) {
      assertTrue(e.getMessage().contains("Unable to retrieve"));
    }
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testParseMalformedJson() throws Exception {
    consumer.parse("{ invalid json format ]");
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testParseInvalidVersion() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 2);
    json.put("file", "test.js");
    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testParseEmptyFile() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "");
    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testMetaMapDisallowedKeys() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "test.js");
    json.put("sections", new JSONArray());
    json.put("lineCount", 10); // Disallowed in section map
    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testSectionWithBothMapAndUrlThrows() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "out.js");
    JSONArray sections = new JSONArray();
    JSONObject section = new JSONObject();
    section.put("offset", new JSONObject().put("line", 0).put("column", 0));
    section.put("map", "{}");
    section.put("url", "http://example.com");
    sections.put(section);
    json.put("sections", sections);

    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testSectionWithoutMapOrUrlThrows() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "out.js");
    JSONArray sections = new JSONArray();
    JSONObject section = new JSONObject();
    section.put("offset", new JSONObject().put("line", 0).put("column", 0));
    sections.put(section);
    json.put("sections", sections);

    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testMetaMapVersionMismatchThrows() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "out.js");
    json.put("sections", new JSONArray());

    // In parseMetaMap, if version != 3 it throws. We can create a root with version 3
    // but check the inner validation by passing a root with invalid version directly to parseMetaMap
    // through parse(JSONObject) where version check runs twice.
    // Instead test section with invalid internal JSON:
    JSONObject section = new JSONObject();
    section.put("offset", new JSONObject().put("line", 0).put("column", 0));
    section.put("map", "{\"version\": 2, \"file\": \"inner.js\"}");
    json.getJSONArray("sections").put(section);

    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class, timeout = 4000)
  public void testMetaMapInnerFileMissingThrows() throws Exception {
    JSONObject json = new JSONObject();
    json.put("version", 3);
    json.put("file", "out.js");
    JSONArray sections = new JSONArray();
    JSONObject section = new JSONObject();
    section.put("offset", new JSONObject().put("line", 0).put("column", 0));
    section.put("map", "{\"version\": 3, \"file\": \"\"}");
    sections.put(section);
    json.put("sections", sections);

    consumer.parse(json);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInvalidVLQSegmentLength() throws Exception {
    // 2 values segment: Base64 'AA' decodes to two numbers [0, 0]
    // Valid lengths are 1, 4, 5
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInvalidSourceFileIndex() throws Exception {
    // Attempting to index source file 1 when sources has length 1 (valid index is 0)
    // 'ACAA' -> [0, 1, 0, 0] -> sourceFileId = 1
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"ACAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInvalidSymbolNameIndex() throws Exception {
    // Attempting to index name 1 when names has length 1 (valid index is 0)
    // 'AAAAC' -> [0, 0, 0, 0, 1] -> nameId = 1
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAAC;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[\"onlyZero\"]\n"
        + "}";
    consumer.parse(json);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testLineCountExceeded() throws Exception {
    // lineCount declared as 1, but mappings contains 2 lines (two ';')
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA;AAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Suppliers & Default Fallbacks
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultSourceMapSupplierDirectly() {
    SourceMapConsumerV3.DefaultSourceMapSupplier supplier =
        new SourceMapConsumerV3.DefaultSourceMapSupplier();
    assertNull(supplier.getSourceMap("http://any.url"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNegativeColumnThrowsPrecondition() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA;\",\n"
        + "\"sources\":[\"src.js\"],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
    // column 0 -> normalized to -1 -> Preconditions.checkState(column >= 0)
    consumer.getMappingForLine(1, 0);
  }
}