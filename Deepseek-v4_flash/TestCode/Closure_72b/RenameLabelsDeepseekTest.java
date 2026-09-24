package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: RenameLabels - label renaming and removal pass.
 *
 * Decision branches targeted:
 *   - shouldTraverse: LABEL node branch (add to renameMap, generate new name)
 *   - visit: LABEL, BREAK, CONTINUE branches
 *   - visitLabel: referenced vs unreferenced label
 *   - visitBreakOrContinue: null vs non-null name node (named break/continue)
 *   - getNameForId: index bounds (size check not explicit, but implicitly via names.get(id-1))
 *   - getLabelInfo: key existent vs non-existent in renameMap
 *
 * Boundary conditions:
 *   - Empty label names (not possible due to parser)
 *   - Single label without reference
 *   - Nested labels with/without cross-references
 *   - Labels sharing names across scopes (handled by namespace stack)
 *   - removeUnused flag ignored bug: when false, unreferenced labels should NOT be removed
 *
 * Defect-targeted: The field 'removeUnused' is stored but never read. Thus,
 *   unreferenced labels are always removed regardless of the flag value.
 *   This test suite exposes that by setting removeUnused=false and asserting
 *   that an unreferenced label survives.
 */
public class RenameLabelsDeepseekTest {

  /**
   * Test that a referenced label with a non-default name is renamed.
   * "myLabel" should become the first generated name ("a").
   */
  @Test(timeout = 4000)
  public void testReferencedLabelRenamed() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseSyntheticCode("test.js", "myLabel: { break myLabel; }");
    assertNotNull("Parsing failed", root);

    RenameLabels renamer = new RenameLabels(compiler);
    renamer.process(null, root);

    // Find the label node after processing
    Node labelNode = findLabelNode(root, "a"); // expected new name
    assertNotNull("Label 'a' should exist after rename", labelNode);
    assertEquals("Label name should be 'a'",
        "a", labelNode.getFirstChild().getString());
  }

  /**
   * Test that an unreferenced label is removed (default removeUnused=true).
   */
  @Test(timeout = 4000)
  public void testUnreferencedLabelRemoved() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseSyntheticCode("test.js", "x: var a = 1;");
    assertNotNull("Parsing failed", root);

    RenameLabels renamer = new RenameLabels(compiler);
    renamer.process(null, root);

    // Label should be gone; the var node should be direct child of script
    Node varNode = root.getFirstChild();
    assertNotNull("Root should have a child", varNode);
    assertNotEquals("Label node should not be present",
        Token.LABEL, varNode.getType());
    assertEquals("Child should be VAR", Token.VAR, varNode.getType());
  }

  /**
   * Defect-revealing test: even if removeUnused=false, the buggy version
   * still removes the unreferenced label. This test asserts the label
   * remains, exposing the defect.
   */
  @Test(timeout = 4000)
  public void testRemoveUnusedFlagIgnored() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseSyntheticCode("test.js", "x: var a = 1;");
    assertNotNull("Parsing failed", root);

    // Use a custom supplier to avoid interference (default supplier is fine)
    RenameLabels renamer = new RenameLabels(
        compiler, new RenameLabels.DefaultNameSupplier(), false);
    renamer.process(null, root);

    // The label should NOT have been removed because removeUnused=false
    Node labelNode = findLabelNode(root, "x"); // original name unchanged if not removed
    // Alternatively, find any LABEL node and check its name
    Node child = root.getFirstChild();
    boolean labelFound = false;
    while (child != null) {
      if (child.getType() == Token.LABEL) {
        labelFound = true;
        break;
      }
      child = child.getNext();
    }
    assertTrue("Label should not have been removed when removeUnused=false", labelFound);
  }

  /**
   * Test nested labels: outer referenced, inner unreferenced.
   * Outer label should be renamed to "a", inner removed.
   */
  @Test(timeout = 4000)
  public void testNestedLabels() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    // Outer label "outer" referenced, inner "inner" unreferenced
    Node root = compiler.parseSyntheticCode("test.js",
        "outer: { inner: break outer; }");
    assertNotNull("Parsing failed", root);

    RenameLabels renamer = new RenameLabels(compiler);
    renamer.process(null, root);

    // After processing, outer becomes "a", inner removed.
    // The structure should be: script -> LABEL (a) -> BLOCK -> BREAK (with name "a")
    Node labelNode = findLabelNode(root, "a");
    assertNotNull("Outer label should be renamed to 'a'", labelNode);

    // Inner label should be gone
    Node innerLabel = findLabelNode(labelNode.getLastChild(), "inner");
    assertNull("Inner label should have been removed", innerLabel);
  }

  /**
   * Test that a named continue is renamed correctly.
   */
  @Test(timeout = 4000)
  public void testNamedContinue() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    // Use a non-default name to force rename
    Node root = compiler.parseSyntheticCode("test.js",
        "myLabel: for(;;) { continue myLabel; }");
    assertNotNull("Parsing failed", root);

    RenameLabels renamer = new RenameLabels(compiler);
    renamer.process(null, root);

    // The label should be renamed to "a"
    Node labelNode = findLabelNode(root, "a");
    assertNotNull("Label should be renamed to 'a'", labelNode);

    // The continue node's first child should also be "a"
    Node continueNode = findNodeWithType(root, Token.CONTINUE);
    assertNotNull("Continue node should exist", continueNode);
    Node nameNode = continueNode.getFirstChild();
    assertNotNull("Named continue should have name child", nameNode);
    assertEquals("Continue target should be renamed to 'a'",
        "a", nameNode.getString());
  }

  // ---- Helper methods ----

  /**
   * Recursively finds the first LABEL node with the given name string.
   */
  private Node findLabelNode(Node root, String name) {
    if (root == null) return null;
    if (root.getType() == Token.LABEL) {
      Node nameNode = root.getFirstChild();
      if (nameNode != null && name.equals(nameNode.getString())) {
        return root;
      }
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findLabelNode(child, name);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Recursively finds the first node of the given token type.
   */
  private Node findNodeWithType(Node root, int type) {
    if (root == null) return null;
    if (root.getType() == type) return root;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findNodeWithType(child, type);
      if (found != null) return found;
    }
    return null;
  }
}