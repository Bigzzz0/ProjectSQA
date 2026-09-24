package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.regex.Pattern;

public class SourceMapDeepseekTest {

    // Helper to create a Node with minimal setup
    private Node createNode(String sourceFile, int lineno, int charno, String originalName) {
        Node node = new Node(0); // Dummy type
        if (sourceFile != null) {
            node.putProp(Node.SOURCEFILE_PROP, sourceFile);
        }
        if (originalName != null) {
            node.putProp(Node.ORIGINALNAME_PROP, originalName);
        }
        node.setLineno(lineno);
        node.setCharno(charno);
        return node;
    }

    // Helper to create a Position (assuming it's a simple data class)
    private Position pos(int line, int col) {
        return new Position(line, col);
    }

    // Helper to extract a section from the output
    private String extractSection(String output, String sectionHeader) {
        int start = output.indexOf(sectionHeader);
        if (start == -1) return "";
        int startBracket = output.indexOf('{', start);
        if (startBracket == -1) return output.substring(start);
        // Find matching closing brace
        int depth = 1;
        int i = startBracket + 1;
        for (; i < output.length() && depth > 0; i++) {
            char c = output.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        return output.substring(start, i);
    }

    private int parseCount(String section) {
        // Extract "count": N
        Pattern p = Pattern.compile("\"count\"\\s*:\\s*(\\d+)");
        java.util.regex.Matcher m = p.matcher(section);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddMappingNodeWithValidDetails() {
        SourceMap sm = new SourceMap();
        Node node = createNode("test.js", 5, 10, null);
        sm.addMapping(node, pos(0, 0), pos(0, 10));
        // Check that mapping was added (internally – not directly visible)
        // Instead we can verify via appendTo later
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail("IOException not expected");
        }
        String output = sb.toString();
        assertTrue("Output should contain mapping definitions", output.contains("/** Begin mapping definitions. **/"));
        assertTrue("Output should contain the source file (escaped)", output.contains("\"test.js\""));
        assertTrue("Output should contain the original line number", output.contains("5"));
    }

    @Test(timeout = 4000)
    public void testAddMappingNodeWithOriginalName() {
        SourceMap sm = new SourceMap();
        Node node = createNode("a.js", 1, 2, "originalName");
        sm.addMapping(node, pos(0, 0), pos(0, 5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        assertTrue("Mapping should include original name", output.contains("\"originalName\""));
    }

    @Test(timeout = 4000)
    public void testAddMappingNullSourceFileReturns() {
        SourceMap sm = new SourceMap();
        Node node = createNode(null, 1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 0));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // Should have no mappings
        String output = sb.toString();
        // The line maps section should be empty? Actually there will be a line map entry but with -1.
        // We check that no mapping definition appears for a source file.
        assertFalse("Should not contain mapping definition with null source file", 
            output.contains("null"));
    }

    @Test(timeout = 4000)
    public void testAddMappingNegativeLinenoReturns() {
        SourceMap sm = new SourceMap();
        Node node = createNode("b.js", -1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 0));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // Should have no mappings
        assertFalse("Mapping with negative line should not be added", 
            sb.toString().contains("b.js"));
    }

    @Test(timeout = 4000)
    public void testAddMappingPreservesOrder() {
        SourceMap sm = new SourceMap();
        Node node1 = createNode("f1.js", 1, 0, null);
        Node node2 = createNode("f2.js", 2, 0, null);
        sm.addMapping(node1, pos(0, 0), pos(0, 5));
        sm.addMapping(node2, pos(0, 5), pos(0, 10));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        int idx1 = output.indexOf("\"f1.js\"");
        int idx2 = output.indexOf("\"f2.js\"");
        assertTrue("f1 should appear before f2", idx1 >= 0 && idx2 >= 0 && idx1 < idx2);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testSetWrapperPrefixAndEffectOnLine() {
        SourceMap sm = new SourceMap();
        sm.setWrapperPrefix("\n\n"); // two newlines => prefix line = 2
        Node node = createNode("x.js", 1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // The header says "count": 3 (last line adjusted by prefix: maxLine=0, plus prefixLine=2 => 2, count = 3)
        String header = extractSection(output, "/** Begin line maps.");
        int count = parseCount(header);
        assertEquals("Expected count = 3 (0..2)", 3, count);
    }

    @Test(timeout = 4000)
    public void testSetStartingPositionOffsets() {
        SourceMap sm = new SourceMap();
        sm.setStartingPosition(10, 5);
        Node node = createNode("x.js", 1, 0, null);
        sm.addMapping(node, pos(2, 3), pos(2, 6));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // The mapping start position should be line 2+10=12, col 3+5=8 (since start line>0 so offset=0? check code)
        // Actually: startPosition line=2, offsetLine=10 => 12, startOffsetPosition=0 because line>0 => col=3+0=3
        // endPosition same line, endOffsetPosition=0 => col=6
        // So mapping definition should have line 12? No, mapping stores original position.
        // The line maps will cover lines 12 and 13 etc.
        String output = sb.toString();
        // Verify mapping is present and line range includes 12.
        assertTrue("Output should contain mapping definition", output.contains("\"x.js\""));
    }

    @Test(timeout = 4000)
    public void testResetClearsMappingsAndPositions() {
        SourceMap sm = new SourceMap();
        Node node = createNode("x.js", 1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 5));
        sm.setStartingPosition(5, 5);
        sm.setWrapperPrefix("prefix\n");
        sm.reset();
        // After reset, should be as fresh
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        assertFalse("Should not contain mapping after reset", output.contains("\"x.js\""));
        // count should be 1 (only line 0)
        String header = extractSection(output, "/** Begin line maps.");
        int count = parseCount(header);
        assertEquals("After reset count should be 1", 1, count);
    }

    @Test(timeout = 4000)
    public void testEmptyMappingsAppendTo() {
        SourceMap sm = new SourceMap();
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "empty.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // Should have the three sections, line maps with count 1, empty file info, no mapping definitions.
        assertTrue(output.contains("/** Begin line maps. **/"));
        assertTrue(output.contains("/** Begin file information. **/"));
        assertTrue(output.contains("/** Begin mapping definitions. **/"));
        // Should have one line entry: []
        String lineMaps = extractSection(output, "/** Begin line maps.");
        assertTrue("Line maps should contain '[]'", lineMaps.contains("[]"));
    }

    @Test(timeout = 4000)
    public void testMultipleMappingsDifferentLines() {
        SourceMap sm = new SourceMap();
        Node node1 = createNode("a.js", 1, 0, null);
        Node node2 = createNode("b.js", 1, 0, null);
        sm.addMapping(node1, pos(0, 0), pos(0, 5));
        sm.addMapping(node2, pos(1, 0), pos(1, 10));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // Should have two mapping definitions
        int countDefs = output.split("\"a.js\"").length - 1 + output.split("\"b.js\"").length - 1;
        assertEquals("Should have two mapping definitions", 2, countDefs);
        // Check line maps: should have entries for at least two lines
        // The max line is 1 (end position line 1) => count = 2
        String header = extractSection(output, "/** Begin line maps.");
        int count = parseCount(header);
        assertEquals("Expected count = 2", 2, count);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // Known defect: golden output tests failing. Possibly related to incorrect count or string escaping.
    // Test that the 'count' field is correctly computed when mappings span multiple lines.

    @Test(timeout = 4000)
    public void testGoldenOutputCountCorrectForSingleLine() {
        SourceMap sm = new SourceMap();
        Node node = createNode("test.js", 1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        String header = extractSection(output, "/** Begin line maps.");
        int count = parseCount(header);
        // max line = 0 (end position line 0) + prefix (0) => 0, count = 1
        assertEquals("For single line mapping, count should be 1", 1, count);
    }

    @Test(timeout = 4000)
    public void testGoldenOutputCountForMultiLineMapping() {
        SourceMap sm = new SourceMap();
        Node node = createNode("test.js", 1, 0, null);
        // start line 0, end line 2 (covers 3 lines)
        sm.addMapping(node, pos(0, 0), pos(2, 10));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String header = extractSection(sb.toString(), "/** Begin line maps.");
        int count = parseCount(header);
        assertEquals("max line = 2, count = 3", 3, count);
    }

    @Test(timeout = 4000)
    public void testEscapeStringForSourceFile() {
        // Access via addMapping; source file with special characters
        SourceMap sm = new SourceMap();
        Node node = createNode("file with spaces.js", 1, 0, null);
        sm.addMapping(node, pos(0, 0), pos(0, 5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // The escaped string should be double-quoted and have escaped characters if any
        assertTrue("Source file should be escaped", output.contains("\"file with spaces.js\""));
        // In this case no special chars, but test that escaping doesn't break
    }

    @Test(timeout = 4000)
    public void testMappingAppendToFormat() throws IOException {
        SourceMap.Mapping m = new SourceMap.Mapping();
        m.id = 0;
        m.sourceFile = "\"escaped.js\"";
        m.originalPosition = new Position(3, 5);
        m.originalName = null;
        StringBuilder sb = new StringBuilder();
        m.appendTo(sb);
        String result = sb.toString();
        assertEquals("[\"escaped.js\",3,5]", result);
    }

    @Test(timeout = 4000)
    public void testMappingAppendToWithOriginalName() throws IOException {
        SourceMap.Mapping m = new SourceMap.Mapping();
        m.id = 1;
        m.sourceFile = "test.js";
        m.originalPosition = new Position(1, 1);
        m.originalName = "myFunc";
        StringBuilder sb = new StringBuilder();
        m.appendTo(sb);
        assertEquals("[test.js,1,1,myFunc]", result);
    }

    // Test that the lastSourceFile caching works (use same file twice)
    @Test(timeout = 4000)
    public void testCachedEscapedSourceFile() {
        SourceMap sm = new SourceMap();
        Node node1 = createNode("same.js", 1, 0, null);
        Node node2 = createNode("same.js", 2, 0, null);
        sm.addMapping(node1, pos(0,0), pos(0,5));
        sm.addMapping(node2, pos(1,0), pos(1,5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // Should appear only once? Actually in mapping definitions each mapping has its own source file.
        // But the cached string ensures same escaped value. No direct observable effect.
        // Just assert no crash.
        assertTrue(sb.length() > 0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddMappingNullNodeThrows() {
        SourceMap sm = new SourceMap();
        sm.addMapping(null, pos(0,0), pos(0,0));
        // Should throw NPE because node.getProp called on null
    }

    @Test(timeout = 4000)
    public void testSetStartingPositionNegativeOffset() {
        SourceMap sm = new SourceMap();
        try {
            sm.setStartingPosition(-1, -5);
            // No exception expected, just sets negative offset
        } catch (Exception e) {
            fail("setStartingPosition should accept negative values");
        }
        Node node = createNode("a.js", 1, 0, null);
        sm.addMapping(node, pos(0,0), pos(0,5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // Should still produce output (though line numbers may become negative)
        assertTrue(sb.toString().contains("/** Begin line maps. **/"));
    }

    @Test(timeout = 4000)
    public void testSetWrapperPrefixEmptyString() {
        SourceMap sm = new SourceMap();
        sm.setWrapperPrefix("");
        // Should set prefix to (0,0)
        Node node = createNode("x.js", 0, 0, null);
        sm.addMapping(node, pos(0,0), pos(0,5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        // No offset applied
        assertTrue(sb.toString().contains("\"x.js\""));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // No clone or equals, but we can test resuse after reset.

    @Test(timeout = 4000)
    public void testSourceMapCanBeReusedAfterReset() {
        SourceMap sm = new SourceMap();
        // First use
        Node node1 = createNode("first.js", 1, 0, null);
        sm.addMapping(node1, pos(0,0), pos(0,5));
        sm.reset();
        // Second use
        Node node2 = createNode("second.js", 2, 0, null);
        sm.addMapping(node2, pos(0,0), pos(0,5));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        assertFalse("Should not contain first.js", output.contains("first.js"));
        assertTrue("Should contain second.js", output.contains("second.js"));
        assertFalse("Should not contain first mapping", output.contains("first.js"));
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfMappings() {
        SourceMap sm = new SourceMap();
        for (int i = 0; i < 100; i++) {
            Node node = createNode("f.js", i%10, i, null);
            sm.addMapping(node, pos(i/10, 0), pos(i/10, 5));
        }
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // Should have 100 mapping definitions
        int count = output.split("\"f.js\"").length - 1;
        assertEquals(100, count);
    }

    // Additional test: overlapping mappings (to exercise LineMapper stack)
    @Test(timeout = 4000)
    public void testOverlappingMappings() {
        SourceMap sm = new SourceMap();
        // Mapping1: covers line 0 col 0 to line 0 col 10
        // Mapping2: inner, starts at line 0 col 3 to line 0 col 7
        Node node1 = createNode("outer.js", 1, 0, null);
        Node node2 = createNode("inner.js", 2, 0, null);
        sm.addMapping(node1, pos(0,0), pos(0,10));
        sm.addMapping(node2, pos(0,3), pos(0,7));
        StringBuilder sb = new StringBuilder();
        try {
            sm.appendTo(sb, "out.js");
        } catch (IOException e) {
            fail();
        }
        String output = sb.toString();
        // The line maps should have entries: for col 0-2: outer.id, col 3-6: inner.id, col 7-9: outer.id again
        // We can check that the line map array contains both ids
        String lineMap = extractLineMap(output);
        assertNotNull(lineMap);
        assertTrue("Line map should contain ids", lineMap.contains("0") && lineMap.contains("1"));
    }

    // Helper to extract line map array from first line (should be one line because maxLine=0)
    private String extractLineMap(String output) {
        // Locate first line after comment "file": ...}
        int start = output.indexOf('}');
        if (start == -1) return null;
        // Find next '[' after that
        start = output.indexOf('[', start);
        if (start == -1) return null;
        int end = output.indexOf(']', start);
        if (end == -1) return null;
        return output.substring(start, end+1);
    }
}