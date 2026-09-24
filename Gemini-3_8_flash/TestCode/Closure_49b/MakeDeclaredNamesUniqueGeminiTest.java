package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * TARGET CLASS: MakeDeclaredNamesUnique
 *
 * PARTITIONS & DECISION BRANCHES TESTED:
 * 1. Scope Transitions & Name Stack Lifecycle:
 *    - enterScope(): global vs function scope, nameStack empty vs non-empty, declarationRoot checks.
 *    - exitScope(): global scope no-op vs non-global stack pop.
 *    - visit(): NAME replacement, FUNCTION scope pop, CATCH scope pop.
 *    - findDeclaredNames(): VAR, FUNCTION declaration, LP param recursion, body shallow traversal.
 *
 * 2. Renamer Implementations:
 *    - ContextualRenamer: global reservation, child scope incrementing, separator suffix format ($$id).
 *    - InlineRenamer: uniqueIdSupplier prefixing, removing existing $$ separator, handling empty name, const stripping.
 *    - BoilerplateRenamer: global preservation, child scope delegation to InlineRenamer.
 *    - ContextualRenameInverter: separator parsing, original name restoration, collision-free local renaming.
 *
 * 3. Defect-Targeted Branches (Closure Defects4J Ground Truth):
 *    - Handling 'arguments' identifiers:
 *      * Issue 423 / FunctionInjectorTest::testInline19b: InlineRenamer fails on Preconditions.checkState(!name.equals(ARGUMENTS))
 *      * MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext5: ContextualRenamer suppresses renaming of 'arguments'
 * --------------------------------------------------------------------------------------------------------------------
 */
public class MakeDeclaredNamesUniqueGeminiTest {

  // ==================================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==================================================================================================================

  @Test(timeout = 4000)
  public void testContextualRenamerGlobalAndChildScopes() {
    MakeDeclaredNamesUnique.ContextualRenamer rootRenamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    assertFalse("ContextualRenamer must not strip constness", rootRenamer.stripConstIfReplaced());

    // In global scope, declarations are reserved and not given replacement names
    rootRenamer.addDeclaredName("foo");
    assertNull("Global declaration must not have replacement name", rootRenamer.getReplacementName("foo"));

    // First child scope encountering "foo" (already declared globally) should get $$1
    MakeDeclaredNamesUnique.Renamer child1 = rootRenamer.forChildScope();
    child1.addDeclaredName("foo");
    assertEquals("foo$$1", child1.getReplacementName("foo"));

    // Second child scope encountering "foo" should get $$2
    MakeDeclaredNamesUnique.Renamer child2 = rootRenamer.forChildScope();
    child2.addDeclaredName("foo");
    assertEquals("foo$$2", child2.getReplacementName("foo"));

    // A variable declared only in local scope (not globally)
    MakeDeclaredNamesUnique.Renamer child3 = rootRenamer.forChildScope();
    child3.addDeclaredName("bar");
    assertNull("First encounter of local name should not be renamed", child3.getReplacementName("bar"));

    // Second local encounter of "bar" gets $$1
    MakeDeclaredNamesUnique.Renamer child4 = rootRenamer.forChildScope();
    child4.addDeclaredName("bar");
    assertEquals("bar$$1", child4.getReplacementName("bar"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerBasicRenamingAndStripping() {
    Supplier<String> supplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return String.valueOf(++counter);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", true);
    assertTrue("stripConstIfReplaced must reflect constructor argument", renamer.stripConstIfReplaced());

    renamer.addDeclaredName("myVar");
    assertEquals("myVar$$inline_1", renamer.getReplacementName("myVar"));

    // Adding existing name again should be a no-op (preserves original mapping)
    renamer.addDeclaredName("myVar");
    assertEquals("myVar$$inline_1", renamer.getReplacementName("myVar"));

    // Adding a name that already has a separator strips existing suffix before appending
    renamer.addDeclaredName("existing$$old_suffix");
    assertEquals("existing$$inline_2", renamer.getReplacementName("existing$$old_suffix"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerForChildScope() {
    Supplier<String> supplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return String.valueOf(++counter);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer parent =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p_", false);
    parent.addDeclaredName("shared");
    assertEquals("shared$$p_1", parent.getReplacementName("shared"));

    MakeDeclaredNamesUnique.Renamer child = parent.forChildScope();
    assertFalse("Child scope must retain removeConstness property", child.stripConstIfReplaced());
    assertNull("Child scope starts with empty declarations", child.getReplacementName("shared"));

    child.addDeclaredName("childVar");
    assertEquals("childVar$$p_2", child.getReplacementName("childVar"));
  }

  @Test(timeout = 4000)
  public void testBoilerplateRenamerPreservesGlobalAndDelegatesChild() {
    Supplier<String> supplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return String.valueOf(++counter);
      }
    };

    MakeDeclaredNamesUnique.BoilerplateRenamer boilerplate =
        new MakeDeclaredNamesUnique.BoilerplateRenamer(supplier, "b_");
    boilerplate.addDeclaredName("globalBp");
    assertNull("Boilerplate root leaves global names unmodified", boilerplate.getReplacementName("globalBp"));

    MakeDeclaredNamesUnique.Renamer child = boilerplate.forChildScope();
    assertTrue("Boilerplate child scope must be an InlineRenamer",
        child instanceof MakeDeclaredNamesUnique.InlineRenamer);
    assertFalse(child.stripConstIfReplaced());

    child.addDeclaredName("localBp");
    assertEquals("localBp$$b_1", child.getReplacementName("localBp"));
  }

  @Test(timeout = 4000)
  public void testContextualRenameInverterProcessSimple() {
    Compiler compiler = new Compiler();
    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    assertNotNull("Inverter CompilerPass must not be null", inverter);

    Node externs = compiler.parseTestCode("");
    Node js = compiler.parseTestCode("function f() { var x$$1 = 1; return x$$1; }");
    inverter.process(externs, js);

    // After inversion without conflict, x$$1 should revert to x
    Node fn = js.getFirstChild();
    Node block = fn.getLastChild();
    Node varNode = block.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    assertEquals("x", nameNode.getString());
  }

  @Test(timeout = 4000)
  public void testFullTraversalMakeDeclaredNamesUniqueOnFunctionsAndVars() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode(
        "var x = 1;\n" +
        "function foo(x) {\n" +
        "  var x = 2;\n" +
        "  return x;\n" +
        "}");

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, js, pass);

    // Global x remains x
    Node globalVar = js.getFirstChild();
    assertEquals("x", globalVar.getFirstChild().getString());

    // Inside function foo, param and local var x should be renamed to x$$1
    Node fooFn = globalVar.getNext();
    Node paramList = fooFn.getFirstChild().getNext();
    Node paramX = paramList.getFirstChild();
    assertEquals("x$$1", paramX.getString());

    Node body = fooFn.getLastChild();
    Node localVar = body.getFirstChild();
    assertEquals("x$$1", localVar.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testFullTraversalWithCatchBlock() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode(
        "var err = 'global';\n" +
        "try {\n" +
        "  throw 1;\n" +
        "} catch (err) {\n" +
        "  err;\n" +
        "}");

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, js, pass);

    Node tryCatch = js.getFirstChild().getNext();
    Node catchBlock = tryCatch.getFirstChild().getNext();
    Node catchNode = catchBlock.getFirstChild();
    Node catchErrName = catchNode.getFirstChild();
    assertEquals("err$$1", catchErrName.getString());
  }

  @Test(timeout = 4000)
  public void testFullTraversalRemovesConstantPropertyWhenConfigured() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode("function f() { var CONST_VAL = 10; return CONST_VAL; }");

    // Manually mark the NAME as constant
    Node fn = js.getFirstChild();
    Node body = fn.getLastChild();
    Node varNode = body.getFirstChild();
    Node constName = varNode.getFirstChild();
    constName.putProp(Node.IS_CONSTANT_NAME, Boolean.TRUE);
    assertTrue(constName.getBooleanProp(Node.IS_CONSTANT_NAME));

    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer inlineRenamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", true);
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(inlineRenamer);
    NodeTraversal.traverse(compiler, js, pass);

    assertFalse("Constant flag must be removed when stripConstIfReplaced is true",
        constName.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  // ==================================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================================

  @Test(timeout = 4000)
  public void testContextualRenameInverterGetOriginalNameBoundaries() {
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(""));
    assertEquals("plain", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("plain"));
    assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$1"));
    assertEquals("", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("$$leading"));
    assertEquals("nested$$1", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("nested$$1$$2"));
  }

  @Test(timeout = 4000)
  public void testInlineRenamerEmptyNameReturnsEmpty() {
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "99";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "bva_", false);
    renamer.addDeclaredName("");
    assertEquals("", renamer.getReplacementName(""));
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionRecursiveName() {
    Compiler compiler = new Compiler();
    // Named function expression where recursive name differs from outer variables
    Node js = compiler.parseTestCode("var fn = function recurse() { recurse(); };");
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, js, pass);

    Node varNode = js.getFirstChild();
    Node fnNode = varNode.getFirstChild().getFirstChild();
    Node fnNameNode = fnNode.getFirstChild();
    // Since recurse is declared only locally once, it should not conflict
    assertEquals("recurse", fnNameNode.getString());
  }

  // ==================================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defects4J Ground Truth)
  // ==================================================================================================================

  /**
   * Targets Issue 423 / FunctionInjector failure condition:
   * When inlining a function that has a local declaration named 'arguments' (e.g. parameter or var),
   * InlineRenamer.addDeclaredName("arguments") was throwing Preconditions.checkState(!name.equals(ARGUMENTS)).
   * In a correct implementation, shadowing 'arguments' is permitted and correctly renamed.
   */
  @Test(timeout = 4000)
  public void testInlineRenamerHandlesShadowedArgumentsWithoutException() {
    Supplier<String> supplier = new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return String.valueOf(++counter);
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", false);

    // Defect Trigger: Under defective code, this throws IllegalStateException: Preconditions.checkState(!name.equals(ARGUMENTS))
    renamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);

    String replacement = renamer.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNotNull("Replacement for shadowed 'arguments' must not be null", replacement);
    assertEquals("arguments$$inline_1", replacement);
  }

  /**
   * Targets MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext5 failure:
   * When a local function scope declares 'var arguments', ContextualRenamer previously bypassed
   * 'arguments' due to "if (!name.equals(ARGUMENTS))", causing local 'arguments' to collide with
   * the outer scope instead of receiving a unique identifier suffix (e.g., arguments$$1).
   */
  @Test(timeout = 4000)
  public void testContextualRenamerRenamesShadowedArgumentsInLocalScope() {
    MakeDeclaredNamesUnique.ContextualRenamer rootRenamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    rootRenamer.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);

    MakeDeclaredNamesUnique.Renamer childScope = rootRenamer.forChildScope();
    // Defect Trigger: In defective code, this is ignored due to `if (!name.equals(ARGUMENTS))`.
    childScope.addDeclaredName(MakeDeclaredNamesUnique.ARGUMENTS);

    String replacement = childScope.getReplacementName(MakeDeclaredNamesUnique.ARGUMENTS);
    assertNotNull("Shadowed 'arguments' in child scope must be made unique", replacement);
    assertEquals("arguments$$1", replacement);
  }

  // ==================================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ==================================================================================================================

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testInlineRenamerEmptyPrefixThrowsIllegalArgumentException() {
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(supplier, "", false);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testContextualRenamerCannotStartAtFunctionContext() {
    Compiler compiler = new Compiler();
    Node fn = compiler.parseTestCode("function standalone() {}").getFirstChild();
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    // Passing a FUNCTION node directly as traversal root with ContextualRenamer triggers checkState
    NodeTraversal.traverse(compiler, fn, pass);
  }

  // ==================================================================================================================
  // Partition E: Object Lifecycle & Inverter Edge Collisions
  // ==================================================================================================================

  @Test(timeout = 4000)
  public void testContextualRenameInverterAvoidsCollisionWithExistingLocal() {
    Compiler compiler = new Compiler();
    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);

    Node externs = compiler.parseTestCode("");
    // If 'x' already exists in the same scope, renaming 'x$$1' should avoid colliding with 'x'
    Node js = compiler.parseTestCode("function f() { var x = 0; var x$$1 = 1; return x + x$$1; }");
    inverter.process(externs, js);

    Node fn = js.getFirstChild();
    Node block = fn.getLastChild();
    Node var1 = block.getFirstChild();
    Node var2 = var1.getNext();

    assertEquals("x", var1.getFirstChild().getString());
    // x$$1 cannot revert to x because x is referenced/declared, so it becomes x$$0 or keeps suffix
    assertFalse("Variable must not collide with existing x",
        "x".equals(var2.getFirstChild().getString()));
  }

  @Test(timeout = 4000)
  public void testDeepNestedScopingIntegrity() {
    Compiler compiler = new Compiler();
    Node js = compiler.parseTestCode(
        "var a = 1;\n" +
        "function l1() {\n" +
        "  var a = 2;\n" +
        "  function l2() {\n" +
        "    var a = 3;\n" +
        "    return a;\n" +
        "  }\n" +
        "  return l2() + a;\n" +
        "}");

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, js, pass);

    Node globalVar = js.getFirstChild();
    assertEquals("a", globalVar.getFirstChild().getString());

    Node l1 = globalVar.getNext();
    Node l1Body = l1.getLastChild();
    Node l1Var = l1Body.getFirstChild();
    assertEquals("a$$1", l1Var.getFirstChild().getString());

    Node l2 = l1Var.getNext();
    Node l2Body = l2.getLastChild();
    Node l2Var = l2Body.getFirstChild();
    assertEquals("a$$2", l2Var.getFirstChild().getString());
  }
}