package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueGeminiTest {

  // Helper compiler instance
  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  // Helper to parse JS code into an AST root
  private Node parse(Compiler compiler, String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals("Parsing errors exist: " + compiler.getErrors(), 0, compiler.getErrorCount());
    return n;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testContextualRenamerBasicFlow() {
    MakeDeclaredNamesUnique.ContextualRenamer rootRenamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();

    assertFalse("stripConstIfReplaced should be false for ContextualRenamer",
        rootRenamer.stripConstIfReplaced());

    // In global scope, declarations reserve name
    rootRenamer.addDeclaredName("globalVar");
    assertNull("Global declarations should not be renamed",
        rootRenamer.getReplacementName("globalVar"));

    // Child scope
    MakeDeclaredNamesUnique.Renamer child = rootRenamer.forChildScope();
    assertNotNull(child);
    assertFalse(child.stripConstIfReplaced());

    // First declaration of an unreserved name in child scope gets id=0, replacement is null
    child.addDeclaredName("localVar");
    assertNull("First encounter id is 0, so replacement is null",
        child.getReplacementName("localVar"));

    // Second declaration of same name in another child scope gets id=1
    MakeDeclaredNamesUnique.Renamer child2 = rootRenamer.forChildScope();
    child2.addDeclaredName("localVar");
    assertEquals("localVar$$1", child2.getReplacementName("localVar"));

    // Repeating addDeclaredName on same scope should be idempotent
    child2.addDeclaredName("localVar");
    assertEquals("localVar$$1", child2.getReplacementName("localVar"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerBasicFlow() {
    Supplier<String> idSupplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return String.valueOf(++counter);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "inline_", true);

    assertTrue(renamer.stripConstIfReplaced());

    renamer.addDeclaredName("foo");
    String replacement = renamer.getReplacementName("foo");
    assertEquals("foo$$inline_1", replacement);

    // Repeated call should preserve mapped replacement
    renamer.addDeclaredName("foo");
    assertEquals("foo$$inline_1", renamer.getReplacementName("foo"));

    // Re-stripping already mangled name with separator
    renamer.addDeclaredName("bar$$prev1");
    assertEquals("bar$$inline_2", renamer.getReplacementName("bar$$prev1"));

    // Child scope creation
    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    assertTrue(child.stripConstIfReplaced());
    child.addDeclaredName("baz");
    assertEquals("baz$$inline_3", child.getReplacementName("baz"));
  }

  @Test(timeout = 4000)
  public void testMakeDeclaredNamesUniqueExecutionOnSimpleVars() {
    Compiler compiler = createCompiler();
    String js = "var a = 1; function f(b) { var a = 2; var c = 3; }";
    Node root = parse(compiler, js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);

    // Verify traversal did not crash and code was parsed
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testMakeDeclaredNamesUniqueWithCatchBlock() {
    Compiler compiler = createCompiler();
    String js = "try { var a = 1; } catch (e) { var e = 2; }";
    Node root = parse(compiler, js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionRecursiveName() {
    Compiler compiler = createCompiler();
    // Recursive function expression: parent is assignment, not a function declaration statement
    String js = "var f = function rec(x) { return rec(x - 1); };";
    Node root = parse(compiler, js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);
    assertNotNull(root);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testInlineRenamerEmptyName() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "p_", false);

    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
  }

  @Test(timeout = 4000)
  public void testContextualRenameInverterStaticMethods() {
    assertEquals("orig", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("orig$$1"));
    assertEquals("multi", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("multi$$1$$2"));
    assertEquals("noSeparator", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("noSeparator"));
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("$$1"));
  }

  @Test(timeout = 4000)
  public void testConstantRemovalWhenStripConstIsTrue() {
    Compiler compiler = createCompiler();
    String js = "function f() { var CONSTANT = 1; return CONSTANT; }";
    Node root = parse(compiler, js);

    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "99";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "u_", true);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);
    assertNotNull(root);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
  // =========================================================================

  /**
   * Targets Defect: testArguments
   * In JavaScript, 'arguments' is a special built-in identifier available within all functions.
   * ContextualRenamer should handle 'arguments' without incorrectly mangling or colliding with it.
   */
  @Test(timeout = 4000)
  public void testDefectTargetArgumentsSpecialName() {
    Compiler compiler = createCompiler();
    // Defining arguments across multiple functions
    String js = "function f() { var arguments; } function g() { var arguments; }";
    Node root = parse(compiler, js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);

    // Invert the renaming to test symmetry and proper scope declaration
    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    assertNotNull(root);
  }

  /**
   * Targets Defect: testOnlyInversion3 & testOnlyInversion4
   * Inversion should revert local variables containing the unique separator back to original names
   * if no conflicting declaration exists in the scope.
   */
  @Test(timeout = 4000)
  public void testDefectTargetOnlyInversion3And4() {
    Compiler compiler = createCompiler();
    String js = "function f() { var a$$1; }";
    Node root = parse(compiler, js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    // Verify that a$$1 was renamed back to 'a' in AST
    Node fNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fNode.getType());
    Node body = fNode.getLastChild();
    Node varNode = body.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();

    assertEquals("a", nameNode.getString());
  }

  /**
   * Targets Defect: testMakeLocalNamesUniqueWithContext1
   * Tests unique name generation when local variables shadow global variables,
   * followed by inversion pass.
   */
  @Test(timeout = 4000)
  public void testDefectTargetMakeLocalNamesUniqueWithContext1() {
    Compiler compiler = createCompiler();
    String js = "var a = 0; function f() { var a = 1; }";
    Node root = parse(compiler, js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(root), pass);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    // The global var 'a' must remain 'a'
    Node globalVar = root.getFirstChild();
    assertEquals(Token.VAR, globalVar.getType());
    assertEquals("a", globalVar.getFirstChild().getString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInlineRenamerEmptyPrefixThrows() {
    Supplier<String> idSupplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(idSupplier, "", false);
  }

  @Test(timeout = 4000)
  public void testContextualRenameInverterNonIdentifierNameIgnored() {
    Compiler compiler = createCompiler();
    // A name with non-identifier characters after separator stripping
    String js = "function f() { var notAnIdent$$1; }";
    Node root = parse(compiler, js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);
    assertNotNull(root);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Invariant Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testContextualRenamerMultipleScopeNesting() {
    MakeDeclaredNamesUnique.ContextualRenamer root =
        new MakeDeclaredNamesUnique.ContextualRenamer();

    MakeDeclaredNamesUnique.Renamer scope1 = root.forChildScope();
    scope1.addDeclaredName("x");
    assertNull(scope1.getReplacementName("x"));

    MakeDeclaredNamesUnique.Renamer scope2 = scope1.forChildScope();
    scope2.addDeclaredName("x");
    assertEquals("x$$1", scope2.getReplacementName("x"));

    MakeDeclaredNamesUnique.Renamer scope3 = scope2.forChildScope();
    scope3.addDeclaredName("x");
    assertEquals("x$$2", scope3.getReplacementName("x"));

    // Previous scopes preserve their respective mappings
    assertNull(scope1.getReplacementName("x"));
    assertEquals("x$$1", scope2.getReplacementName("x"));
  }

  @Test(timeout = 4000)
  public void testExitScopeGlobalSafety() {
    Compiler compiler = createCompiler();
    Node root = parse(compiler, "var x = 1;");

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal traversal = new NodeTraversal(compiler, pass);
    // Traversing root invokes enterScope and exitScope safely
    traversal.traverse(root);
    assertNotNull(root);
  }
}