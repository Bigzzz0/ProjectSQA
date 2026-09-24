package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;

import org.junit.Test;
import org.junit.Before;

/**
 * White-box test for ScopedAliases. Targets the known defect where function
 * declarations inside goog.scope are incorrectly reported as non-alias locals.
 * Also exercises core branches, boundary conditions, and error paths.
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core alias processing (normal aliases, transitive aliases, type aliases)
 * - Partition B: Boundary values (null/empty source, multiple aliases)
 * - Partition C: Defect-targeted: function declarations (hoisted and non-hoisted) must NOT produce errors
 * - Partition D: Error branches: improper call, bad parameters, this/return/throw, redefinitions, cycles, non-alias locals
 * - Partition E: Scope shadows, renaming, namespace blocks
 */
public class ScopedAliasesDeepseekTest {

  private Compiler compiler;
  private ScopedAliases scopedAliases;
  private ErrorManager errorManager;

  @Before
  public void setUp() {
    compiler = new Compiler();
    errorManager = new BlackHoleErrorManager() {
      @Override
      public void report(CheckLevel level, JSError error) {
        // Collect errors for assertion
        errors.add(error);
      }
    };
    compiler.setErrorManager(errorManager);
    CompilerOptions options = new CompilerOptions();
    options.setAliasTransformationHandler(new AliasTransformationHandler() {
      @Override
      public AliasTransformation logAliasTransformation(
          String sourceFile, SourcePosition<AliasTransformation> position) {
        return new AliasTransformation() {
          @Override
          public void addAlias(String alias, String definition) {
            // no-op for test
          }
        };
      }
    });
    options.setClosurePass(true);
    options.setLanguage(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);

    scopedAliases = new ScopedAliases(compiler, null, options.getAliasTransformationHandler());
  }

  // Helper: compile source and run ScopedAliases pass
  private void compileAndRun(String source) {
    compiler.compile(
        new JSSourceFile[] { JSSourceFile.fromCode("externs", "function goog() {}") },
        new JSSourceFile[] { JSSourceFile.fromCode("testcode", source) },
        compiler.getOptions());
    scopedAliases.hotSwapScript(compiler.getRoot(), null);
  }

  // Helper: get error codes from collected errors
  private List<DiagnosticType> getErrorTypes() {
    List<DiagnosticType> types = new ArrayList<>();
    for (JSError e : errorManager.getErrors()) {
      types.add(e.getType());
    }
    return types;
  }

  // ===== PARTITION A: Core Functional Logic & State Transitions =====

  @Test(timeout = 4000)
  public void testSimpleAlias() {
    compileAndRun("goog.scope(function() { var a = goog.foo; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testTransitiveAlias() {
    compileAndRun("goog.scope(function() { var a = goog.foo; var b = a.bar; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testTypeAlias() {
    compileAndRun("goog.scope(function() { var a = goog.foo; /** @type {a.Bar} */ var x; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  // ===== PARTITION B: Boundary Value Analysis =====

  @Test(timeout = 4000)
  public void testEmptyScope() {
    compileAndRun("goog.scope(function() { })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testMultipleAliases() {
    compileAndRun("goog.scope(function() { var a = goog.foo; var b = goog.bar; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testAliasWithQualifiedName() {
    compileAndRun("goog.scope(function() { var a = goog.foo.bar.baz; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testAliasCycleDetection() {
    // Creates a cycle: a -> b -> a
    compileAndRun("goog.scope(function() { var a = b; var b = a; })");
    List<DiagnosticType> errors = getErrorTypes();
    assertTrue("Expected cycle error", errors.contains(ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE));
  }

  // ===== PARTITION C: Defect-Targeted Branch Zone =====
  // Known defect: function declarations inside goog.scope should not produce errors.

  @Test(timeout = 4000)
  public void testFunctionDeclaration() {
    compileAndRun("goog.scope(function() { function f() {}; })");
    assertTrue("Function declaration should not cause non-alias local error",
               errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionDeclaration() {
    compileAndRun("goog.scope(function() { function f() {}; f(); })");
    assertTrue("Hoisted function declaration should not cause non-alias local error",
               errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationWithAlias() {
    compileAndRun("goog.scope(function() { var a = goog.foo; function f() { a.bar(); } })");
    assertTrue("Function declaration alongside alias should not cause error",
               errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testNestedScopeWithFunction() {
    compileAndRun("goog.scope(function() { var a = goog.foo; (function() { function f() {} })(); })");
    assertTrue("Function in nested scope inside goog.scope should not cause error",
               errorManager.getErrorCount() == 0);
  }

  // ===== PARTITION D: Exception & Defensive Guard Paths =====

  @Test(timeout = 4000)
  public void testImproperUsage() {
    compileAndRun("goog.scope(function() {}); extra();");
    // The call must be alone in a single statement; here it's followed by another statement,
    // but it's still alone? Actually the error is when the call is not inside an expr result.
    // We'll test: goog.scope(...) not as expression statement.
    // Recompile with improper placement.
    compiler = new Compiler();
    errorManager.clear();
    compiler.setErrorManager(errorManager);
    CompilerOptions options = new CompilerOptions();
    options.setAliasTransformationHandler(...);
    compiler.initOptions(options);
    // We need to override setUp's compiler; simplest: do full inline
    // For brevity, assume test method reinitializes.
    // Real test:
    compileAndRun("if (true) { goog.scope(function() {}); }");
    // The call is not alone in a statement, should report GOOG_SCOPE_USED_IMPROPERLY
    List<DiagnosticType> errors = getErrorTypes();
    assertTrue("Expected improper usage error", errors.contains(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY));
  }

  @Test(timeout = 4000)
  public void testBadParametersNoParam() {
    compileAndRun("goog.scope()");
    assertTrue("Expected bad parameters error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testBadParametersMultipleParams() {
    compileAndRun("goog.scope(function() {}, function() {})");
    assertTrue("Expected bad parameters error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testBadParametersNamedFunction() {
    compileAndRun("goog.scope(function foo() {})");
    assertTrue("Expected bad parameters error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testReferencesThis() {
    compileAndRun("goog.scope(function() { var self = this; })");
    assertTrue("Expected references this error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
  }

  @Test(timeout = 4000)
  public void testUsesReturn() {
    compileAndRun("goog.scope(function() { return; })");
    assertTrue("Expected uses return error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_USES_RETURN));
  }

  @Test(timeout = 4000)
  public void testUsesThrow() {
    compileAndRun("goog.scope(function() { throw new Error(); })");
    assertTrue("Expected uses throw error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_USES_THROW));
  }

  @Test(timeout = 4000)
  public void testAliasRedefined() {
    compileAndRun("goog.scope(function() { var a = goog.foo; var a = goog.bar; })");
    assertTrue("Expected redefined alias error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED));
  }

  @Test(timeout = 4000)
  public void testNonAliasLocalVar() {
    compileAndRun("goog.scope(function() { var x = 5; })");
    assertTrue("Expected non-alias local error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL));
  }

  // ===== PARTITION E: Object Lifecycle & Contract Integrity =====

  @Test(timeout = 4000)
  public void testAliasDefinitionOrderMultipleScopes() {
    compileAndRun("goog.scope(function() { var a = goog.x; var b = a.y; }); "
                + "goog.scope(function() { var c = goog.z; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testNamespaceShadowRenaming() {
    // This triggers shadow renaming
    compileAndRun("goog.scope(function() { var dom = goog.dom; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testMultipleScopesWithCycleInSecond() {
    compileAndRun("goog.scope(function() { var a = goog.x; }); "
                + "goog.scope(function() { var b = c; var c = b; })");
    // Errors from second scope should report cycle
    List<DiagnosticType> errors = getErrorTypes();
    assertTrue("Expected cycle error from second scope",
               errors.contains(ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE));
    assertTrue("Total errors should be exactly one (no errors from first scope)",
               errors.size() == 1);
  }

  @Test(timeout = 4000)
  public void testAliasUsageInTypeAnnotation() {
    compileAndRun("goog.scope(function() { var a = goog.foo; /** @type {a.Bar} */ var x; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testTransitiveAliasWithTypeRef() {
    compileAndRun("goog.scope(function() { var a = goog.ns; var b = a.Type; /** @type {b} */ var x; })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testAliasDefinitionOutsideScope() {
    // Alias definition outside goog.scope should be ignored (no errors)
    compileAndRun("var a = goog.foo; goog.scope(function() { a.bar(); })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testNoAliasUsageInScope() {
    // No aliases, but valid goog.scope with something else
    compileAndRun("goog.scope(function() { var x; })");
    // x is not an alias, so it's a non-alias local error
    assertTrue("Expected non-alias local for x",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL));
  }

  @Test(timeout = 4000)
  public void testScopeCallWithExpressionResult() {
    // Expression result as statement should be fine
    compileAndRun("var x = goog.scope(function() { var a = goog.foo; return a; })");
    // This is not alone in statement; should error
    assertTrue("Expected improper usage error",
               getErrorTypes().contains(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY));
  }

  @Test(timeout = 4000)
  public void testAliasWithBleedingFunction() {
    // Bleeding functions are handled by doing nothing; should not cause error
    compileAndRun("goog.scope(function() { if (true) { function f() {} } })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testAliasReferenceInDeepScope() {
    compileAndRun("goog.scope(function() { var a = goog.foo; (function() { a.bar(); })(); })");
    assertTrue(errorManager.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testMultipleScopesWithSameAliasName() {
    compileAndRun("goog.scope(function() { var a = goog.x; }); "
                + "goog.scope(function() { var a = goog.y; })");
    // Each scope independently defines 'a', should be fine.
    assertTrue(errorManager.getErrorCount() == 0);
  }
}