package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.debugging.sourcemap.FilePosition;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceMapDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Decision branches in SourceMap:
     * - addMapping:
     *   (1) sourceFile == null -> return
     *   (2) node.getLineno() < 0 -> return
     *   (3) else -> fixupSourceLocation and generator.addMapping
     *
     * - fixupSourceLocation:
     *   (4) prefixMappings.isEmpty() -> return sourceFile
     *   (5) cache hit -> return cached fixed
     *   (6) loop over prefixMappings:
     *       (6a) sourceFile.startsWith(mapping.prefix) -> compute fixed, break
     *   (7) if fixed == null -> fixed = sourceFile
     *   (8) put in cache, return fixed
     *
     * Boundary conditions:
     * - sourceFile: null, empty string, with/without prefix
     * - line number: negative, zero, positive
     * - prefixMappings: empty list, list with matching/non-matching entries, multiple entries
     *
     * Defect (SourceMapGeneratorV3 off-by-one): line numbers for V3 should be zero-based (convert by subtracting 1 from Rhino's one-based). The bug is that SourceMap does not perform this conversion.
     *
     * This test suite targets all branches, boundary values, and the known defect.
     */

    // ========================= Partition A: Core Functional Logic & State Transitions =========================

    @Test(timeout = 4000)
    public void testAddMappingValid() throws IOException {
        // Valid addMapping with no prefix mappings
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile("test.js");
        node.setLineno(5);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // The output should contain a mappings field (non-empty)
        assertTrue("Mapping should be present", result.contains("\"mappings\""));
        // Note: the exact encoding depends on the generator; we just verify something was added
    }

    @Test(timeout = 4000)
    public void testAddMappingWithPrefixMapping() throws IOException {
        // Fixup source location with a matching prefix
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        List<SourceMap.LocationMapping> mappings = new ArrayList<>();
        mappings.add(new SourceMap.LocationMapping("/src/", "/remapped/"));
        sourceMap.setPrefixMappings(mappings);

        Node node = new Node(Node.Token.SCRIPT);
        node.setSourceFile("/src/foo.js");
        node.setLineno(1);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // Expected source file after remapping: /remapped/foo.js
        assertTrue("Source file should be remapped", result.contains("/remapped/foo.js"));
    }

    @Test(timeout = 4000)
    public void testAddMappingWithPrefixNoMatch() throws IOException {
        // Prefix mapping does not match source file
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        List<SourceMap.LocationMapping> mappings = new ArrayList<>();
        mappings.add(new SourceMap.LocationMapping("/src/", "/remapped/"));
        sourceMap.setPrefixMappings(mappings);

        Node node = new Node(Node.Token.SCRIPT);
        node.setSourceFile("/other/bar.js");
        node.setLineno(1);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // Source file should remain unchanged
        assertTrue("Source file should remain unchanged", result.contains("/other/bar.js"));
    }

    @Test(timeout = 4000)
    public void testFixupSourceLocationCache() throws IOException {
        // Two mappings with same source file to trigger caching
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        List<SourceMap.LocationMapping> mappings = new ArrayList<>();
        mappings.add(new SourceMap.LocationMapping("/a/", "/b/"));
        sourceMap.setPrefixMappings(mappings);

        Node node1 = new Node(Node.Token.SCRIPT);
        node1.setSourceFile("/a/x.js");
        node1.setLineno(1);
        node1.setCharno(0);

        Node node2 = new Node(Node.Token.SCRIPT);
        node2.setSourceFile("/a/x.js");
        node2.setLineno(2);
        node2.setCharno(0);

        sourceMap.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 0));
        sourceMap.addMapping(node2, new FilePosition(0, 1), new FilePosition(0, 1));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // Source file should appear only once (caching does not affect output directly,
        // but the cache ensures fixup only called once; we verify the result is consistent)
        assertTrue("Source file should be remapped", result.contains("/b/x.js"));
        // We cannot easily count occurrences from string, but if cache caused a bug, it would be visible as duplicate source entry.
        // For V3, the generator deduplicates sources, so it's fine.
    }

    // ========================= Partition B: Boundary Value Analysis (BVA) & Extremes =========================

    @Test(timeout = 4000)
    public void testAddMappingNullSourceFile() throws IOException {
        // Node with null source file should cause early return (no mapping)
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile(null); // This might not be possible via setSourceFile? Actually it can be null.
        // The node's getSourceFileName() will return null.
        node.setLineno(1);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // The output should not contain any "mappings" (or mapping is empty)
        // In V3, even if no mappings, it still has an empty mappings string? Actually JSON might have "mappings": "",
        // but we can check that the "sources" array is empty because no source file was added.
        assertFalse("Source file should not be present", result.contains("\"sources\""));
    }

    @Test(timeout = 4000)
    public void testAddMappingNegativeLine() throws IOException {
        // Node with negative line number should cause early return
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile("test.js");
        node.setLineno(-1);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // No mapping should be added; source file may appear but no mapping
        assertFalse("Mappings should be empty or absent", result.contains("\"mappings\":\""));
    }

    @Test(timeout = 4000)
    public void testAddMappingZeroLine() throws IOException {
        // Line 0 is allowed (boundary case, though Rhino lines are 1-based)
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile("test.js");
        node.setLineno(0);
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // Map should be present
        assertTrue("Mapping should be present for line=0", result.contains("\"mappings\""));
    }

    // ========================= Partition C: Defect-Targeted Branch Zone =========================

    @Test(timeout = 4000)
    public void testDefectTargetedLineNumber() throws IOException {
        // Known defect: SourceMap does not subtract 1 from node line number for V3.
        // Input line 5 (one-based) should become 4 (zero-based) in the generated source map.
        // The first mapping for generated line 0, column 0, original line 4, column 0
        // encodes as VLQ: generatedColumn=0, sourceIndex=0, originalLine=4, originalColumn=0
        // => Base64: 0,0,4,0 => "A","A","E","A" => "AAEA"
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        Node node = new Node(Node.Token.NAME, "test");
        node.setSourceFile("test.js");
        node.setLineno(5);   // one-based
        node.setCharno(0);

        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();

        // The escaped JSON mapping string should contain "AAEA"
        assertTrue("Mappings should encode zero-based line=4 as 'AAEA'",
                   result.contains("\"AAEA\""));
        // This assertion will fail on the buggy version (encoding "AAFA" for line=5)
    }

    // ========================= Partition D: Exception & Defensive Guard Paths =========================

    @Test(timeout = 4000)
    public void testResetClearsCache() throws IOException {
        // Ensure reset clears the sourceLocationFixupCache and generator
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        List<SourceMap.LocationMapping> mappings = new ArrayList<>();
        mappings.add(new SourceMap.LocationMapping("/a/", "/b/"));
        sourceMap.setPrefixMappings(mappings);

        Node node1 = new Node(Node.Token.SCRIPT);
        node1.setSourceFile("/a/x.js");
        node1.setLineno(1);
        node1.setCharno(0);

        Node node2 = new Node(Node.Token.SCRIPT);
        node2.setSourceFile("/a/y.js");
        node2.setLineno(2);
        node2.setCharno(0);

        // Add mapping and get output
        sourceMap.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 0));
        StringBuilder out1 = new StringBuilder();
        sourceMap.appendTo(out1, "out1.js");
        String beforeReset = out1.toString();
        assertTrue("Before reset source should be remapped", beforeReset.contains("/b/x.js"));

        // Reset
        sourceMap.reset();

        // After reset, add a different mapping
        sourceMap.addMapping(node2, new FilePosition(0, 0), new FilePosition(0, 0));
        StringBuilder out2 = new StringBuilder();
        sourceMap.appendTo(out2, "out2.js");
        String afterReset = out2.toString();
        // The source file should be remapped again (cache was cleared)
        assertTrue("After reset source should still be remapped", afterReset.contains("/b/y.js"));
        // Additionally, the first mapping should not leak into second output (generator reset)
        assertFalse("After reset should not contain first mapping source", afterReset.contains("/b/x.js"));
    }

    @Test(timeout = 4000)
    public void testSetStartingPosition() throws IOException {
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        sourceMap.setStartingPosition(10, 5);
        // No side effect observable easily, but at least no exception
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile("test.js");
        node.setLineno(1);
        node.setCharno(0);
        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));
        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        assertNotNull(out.toString());
    }

    @Test(timeout = 4000)
    public void testSetWrapperPrefix() throws IOException {
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        sourceMap.setWrapperPrefix("(function(){");
        // No direct observable effect but should not throw
        Node node = new Node(Node.Token.NAME, "x");
        node.setSourceFile("test.js");
        node.setLineno(1);
        node.setCharno(0);
        sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 0));
        StringBuilder out = new StringBuilder();
        sourceMap.appendTo(out, "output.js");
        String result = out.toString();
        // The prefix might appear in the output depending on generator implementation
        // Since we cannot guarantee, we just verify that the method was called without error.
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testValidate() {
        SourceMap sourceMap = SourceMap.Format.V3.getInstance();
        sourceMap.validate(true);
        // Should not throw
    }

    // ========================= Partition E: Object Lifecycle & Contract Integrity =========================

    // No equals/hashCode to test. SourceMap is not cloneable.
    // However, we can test that multiple instances are independent.

    @Test(timeout = 4000)
    public void testMultipleInstances() throws IOException {
        SourceMap map1 = SourceMap.Format.V3.getInstance();
        SourceMap map2 = SourceMap.Format.V3.getInstance();

        Node node1 = new Node(Node.Token.NAME, "a");
        node1.setSourceFile("f1.js");
        node1.setLineno(1);
        node1.setCharno(0);

        Node node2 = new Node(Node.Token.NAME, "b");
        node2.setSourceFile("f2.js");
        node2.setLineno(2);
        node2.setCharno(0);

        map1.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 0));
        map2.addMapping(node2, new FilePosition(0, 0), new FilePosition(0, 0));

        StringBuilder out1 = new StringBuilder();
        map1.appendTo(out1, "out1.js");
        String result1 = out1.toString();

        StringBuilder out2 = new StringBuilder();
        map2.appendTo(out2, "out2.js");
        String result2 = out2.toString();

        assertTrue("map1 should contain f1.js", result1.contains("f1.js"));
        assertTrue("map2 should contain f2.js", result2.contains("f2.js"));
        assertFalse("map1 should not contain f2", result1.contains("f2.js"));
        assertFalse("map2 should not contain f1", result2.contains("f1.js"));
    }
}