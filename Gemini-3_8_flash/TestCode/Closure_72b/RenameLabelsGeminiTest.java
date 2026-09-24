/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.RenameLabels
 * Known Defect (Closure-72 / InlineFunctionsTest#testInlineFunctions31):
 * - Location: RenameLabels#visitLabel(Node, Node)
 * - Condition: When removeUnused is set to false in constructor, unreferenced labels should
 *              be preserved and renamed (i.e. 'if (li.referenced || !removeUnused)').
 *              The defect unconditionally enters the removal branch because it checks only
 *              'if (li.referenced)', discarding unreferenced labels even when removeUnused=false.
 * -----------------------------------------------------------------------------------------
 * Decision / Branch Coverage Target:
 * 1. RenameLabels constructors: default vs custom (AbstractCompiler, Supplier, boolean).
 * 2. DefaultNameSupplier: generator iteration and safe name generation ("a", "b", etc.).
 * 3. ProcessLabels#enterScope / exitScope: proper stack management on function boundaries.
 * 4. ProcessLabels#shouldTraverse:
 *    - Node is Token.LABEL: currentDepth calculation, nameSupplier invocation if names list
 *      needs expansion vs reuse of existing names.
 *    - Preconditions check: duplicate label in the same active scope throws IllegalStateException.
 * 5. ProcessLabels#visit:
 *    - Token.LABEL -> visitLabel
 *    - Token.BREAK / Token.CONTINUE -> visitBreakOrContinue
 *    - Other token types -> ignored.
 * 6. ProcessLabels#visitBreakOrContinue:
 *    - nameNode == null (unnamed break / continue) -> cleanly bypassed.
 *    - nameNode != null (named break / continue):
 *      - name.length() == 0 check (Preconditions).
 *      - li != null vs li == null (unresolved / external label target).
 *      - !name.equals(newName) true (renamed) vs false (already matches generated name).
 * 7. ProcessLabels#visitLabel:
 *    - li.referenced == true -> renamed to newName (and !name.equals(newName) condition).
 *    - li.referenced == false && removeUnused == true -> label node stripped, replaced with child:
 *      - newChild.getType() == Token.BLOCK (NodeUtil.tryMergeBlock branch).
 *      - newChild.getType() != Token.BLOCK (non-block statement branch).
 *    - li.referenced == false && removeUnused == false [DEFECT TRIGGER] -> label preserved & renamed.
 * -----------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class RenameLabelsGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultConstructorAndReferencedLabelRenaming() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("foo: while (true) { break foo; }");
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);

    Node labelNode = root.getFirstChild();
    assertNotNull(labelNode);
    assertEquals(Token.LABEL, labelNode.getType());

    Node nameNode = labelNode.getFirstChild();
    assertEquals("a", nameNode.getString());

    Node whileNode = labelNode.getLastChild();
    assertEquals(Token.WHILE, whileNode.getType());

    Node blockNode = whileNode.getLastChild();
    Node breakNode = blockNode.getFirstChild();
    assertEquals(Token.BREAK, breakNode.getType());
    assertEquals("a", breakNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testContinueTargetingLabel() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("myLoop: for (;;) { continue myLoop; }");
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals("a", labelNode.getFirstChild().getString());

    Node forNode = labelNode.getLastChild();
    Node blockNode = forNode.getLastChild();
    Node continueNode = blockNode.getFirstChild();
    assertEquals(Token.CONTINUE, continueNode.getType());
    assertEquals("a", continueNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testNestedLabelsDifferentDepths() {
    Compiler compiler = new Compiler();
    String code = "outer: while (true) { inner: while (true) { break outer; } }";
    Node root = compiler.parseTestCode(code);
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);

    // outer is referenced -> renamed to "a"
    Node outerLabel = root.getFirstChild();
    assertEquals(Token.LABEL, outerLabel.getType());
    assertEquals("a", outerLabel.getFirstChild().getString());

    // inner is unreferenced -> stripped, leaving the inner while loop
    Node outerWhile = outerLabel.getLastChild();
    Node outerBlock = outerWhile.getLastChild();
    Node innerWhile = outerBlock.getFirstChild();
    assertEquals(Token.WHILE, innerWhile.getType());
  }

  @Test(timeout = 4000)
  public void testSiblingScopesReuseLabelNames() {
    Compiler compiler = new Compiler();
    String code = "function f1() { L1: while (true) { break L1; } }\n"
                + "function f2() { L2: while (true) { break L2; } }";
    Node root = compiler.parseTestCode(code);
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);

    Node f1 = root.getFirstChild();
    Node f1Label = f1.getLastChild().getFirstChild();
    assertEquals(Token.LABEL, f1Label.getType());
    assertEquals("a", f1Label.getFirstChild().getString());

    Node f2 = f1.getNext();
    Node f2Label = f2.getLastChild().getFirstChild();
    assertEquals(Token.LABEL, f2Label.getType());
    assertEquals("a", f2Label.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnreferencedLabelRemovedAndBlockMerged() {
    Compiler compiler = new Compiler();
    // Label wrapping a BLOCK with removeUnused=true
    Node root = compiler.parseTestCode("unused: { var x = 1; var y = 2; }");
    RenameLabels renamer = new RenameLabels(compiler, new RenameLabels.DefaultNameSupplier(), true);

    renamer.process(null, root);

    // Label should be stripped and the block merged into parent script
    Node first = root.getFirstChild();
    assertNotNull(first);
    assertNotEquals(Token.LABEL, first.getType());
    assertEquals(Token.VAR, first.getType());
  }

  @Test(timeout = 4000)
  public void testUnreferencedLabelRemovedNonBlockChild() {
    Compiler compiler = new Compiler();
    // Label wrapping a non-BLOCK statement
    Node root = compiler.parseTestCode("unused: var x = 1;");
    RenameLabels renamer = new RenameLabels(compiler, new RenameLabels.DefaultNameSupplier(), true);

    renamer.process(null, root);

    Node first = root.getFirstChild();
    assertNotNull(first);
    assertEquals(Token.VAR, first.getType());
  }

  @Test(timeout = 4000)
  public void testAlreadyRenamedLabelSkipsReportCodeChange() {
    Compiler compiler = new Compiler();
    // Label already named "a", so newName ("a") equals current name
    Node root = compiler.parseTestCode("a: while (true) { break a; }");
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);

    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals("a", labelNode.getFirstChild().getString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultNameSupplierSequentialGeneration() {
    RenameLabels.DefaultNameSupplier supplier = new RenameLabels.DefaultNameSupplier();
    assertEquals("a", supplier.get());
    assertEquals("b", supplier.get());
    assertEquals("c", supplier.get());
  }

  @Test(timeout = 4000)
  public void testCustomNameSupplier() {
    Supplier<String> customSupplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return "customLabel_" + (++counter);
      }
    };

    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("lbl: while(true) { break lbl; }");
    RenameLabels renamer = new RenameLabels(compiler, customSupplier, true);

    renamer.process(null, root);

    Node labelNode = root.getFirstChild();
    assertEquals("customLabel_1", labelNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnnamedBreakAndContinueStatements() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("while (true) { break; continue; }");
    RenameLabels renamer = new RenameLabels(compiler);

    // Unnamed break and continue have no name child; must process cleanly without NPE
    renamer.process(null, root);

    Node whileNode = root.getFirstChild();
    assertEquals(Token.WHILE, whileNode.getType());
  }

  @Test(timeout = 4000)
  public void testEmptyAstRoot() {
    Compiler compiler = new Compiler();
    Node emptyRoot = new Node(Token.BLOCK);
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, emptyRoot);
    assertFalse(emptyRoot.hasChildren());
  }

  @Test(timeout = 4000)
  public void testBreakToUndefinedLabelGracefullyHandled() {
    Compiler compiler = new Compiler();
    RenameLabels renamer = new RenameLabels(compiler);
    RenameLabels.ProcessLabels pl = renamer.new ProcessLabels();

    // Directly test visitBreakOrContinue when label info is null
    Node breakNode = new Node(Token.BREAK, Node.newString("nonExistentLabel"));
    pl.visit(null, breakNode, new Node(Token.BLOCK));

    // Name node string must remain untouched when label info is not found
    assertEquals("nonExistentLabel", breakNode.getFirstChild().getString());
    assertNull(pl.getLabelInfo("nonExistentLabel"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-72 Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnreferencedLabelPreservedWhenRemoveUnusedIsFalse() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("myLabel: while (true) { foo(); }");

    // removeUnused is explicitly configured to FALSE
    RenameLabels renamer = new RenameLabels(compiler, new RenameLabels.DefaultNameSupplier(), false);
    renamer.process(null, root);

    // DEFECT REVELATION:
    // When removeUnused is false, the unreferenced label MUST NOT be removed.
    // Instead, it must be renamed to the generated name ("a").
    // On the defective version, li.referenced is false and removeUnused is ignored,
    // causing the LABEL node to be discarded and replaced by the WHILE node.
    Node scriptChild = root.getFirstChild();
    assertNotNull("Root child should not be null", scriptChild);
    assertEquals("Expected label node to be preserved when removeUnused=false",
        Token.LABEL, scriptChild.getType());
    assertEquals("Expected preserved label to be renamed to 'a'",
        "a", scriptChild.getFirstChild().getString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testDuplicateLabelInSameScopeThrowsIllegalState() {
    Compiler compiler = new Compiler();
    // Nested label with the same name within identical scope violates unique renameMap constraint
    Node root = compiler.parseTestCode("dup: { dup: { break dup; } }");
    RenameLabels renamer = new RenameLabels(compiler);

    renamer.process(null, root);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testBreakWithEmptyNameThrowsIllegalState() {
    Compiler compiler = new Compiler();
    RenameLabels renamer = new RenameLabels(compiler);
    RenameLabels.ProcessLabels pl = renamer.new ProcessLabels();

    Node emptyNameBreak = new Node(Token.BREAK, Node.newString(""));
    pl.visit(null, emptyNameBreak, new Node(Token.BLOCK));
  }

  // =========================================================================
  // Partition E: Internal Helper Contract & State Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testProcessLabelsDirectApi() {
    Compiler compiler = new Compiler();
    RenameLabels renamer = new RenameLabels(compiler);
    RenameLabels.ProcessLabels pl = renamer.new ProcessLabels();

    // Verify initial state
    assertEquals(1, pl.namespaceStack.size());
    assertNull(pl.getLabelInfo("nonExistent"));

    // Enter and exit child scope
    pl.enterScope(null);
    assertEquals(2, pl.namespaceStack.size());

    pl.exitScope(null);
    assertEquals(1, pl.namespaceStack.size());
  }
}